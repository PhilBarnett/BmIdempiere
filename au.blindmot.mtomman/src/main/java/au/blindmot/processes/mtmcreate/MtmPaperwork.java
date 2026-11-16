package au.blindmot.processes.mtmcreate;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.model.MQuery;
import org.compiere.model.MRole;
import org.compiere.model.MTable;
import org.compiere.model.Query;
import org.compiere.print.MPrintFormat;
import org.compiere.model.PrintInfo;
import org.compiere.print.ReportEngine;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfo;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.Env;
import org.adempiere.util.ProcessUtil;
import org.compiere.util.Trx;

import au.blindmot.model.MBLDMtomProduction;
import au.blindmot.mtmlabels.processes.MtmLabels;

final class MtmPaperwork {
	private static final String RUN_COLUMN = "Bld_Mtom_Production_ID";
	private static final Logger LOG = Logger.getLogger(MtmPaperwork.class.getName());
	static final Comparator<String> DOCUMENT_ORDER = (left, right) -> {
		boolean leftNumber = left.matches("[0-9]+");
		boolean rightNumber = right.matches("[0-9]+");
		if (leftNumber != rightNumber)
			return leftNumber ? -1 : 1;
		if (leftNumber) {
			int comparison = new BigInteger(left).compareTo(new BigInteger(right));
			if (comparison != 0)
				return comparison;
		}
		return left.compareTo(right);
	};

	private enum Output {
		LABELS("Print Mtm Labels", "-labels.buz"),
		ALUMINIUM("MTM Aluminium cuts", "-aluminium-cuts.pdf"),
		FABRIC("MTM Fabric cuts", "-fabric-cuts.pdf"),
		ASSEMBLY("MTM Assembly", "-assembly.pdf"),
		INFO("MTM Info Summary", "-info-summary.pdf");

		final String process;
		final String suffix;

		Output(String process, String suffix) {
			this.process = process;
			this.suffix = suffix;
		}
	}

	static final int OUTPUT_COUNT = Output.values().length;

	private final Properties ctx;
	private final MRole role;
	private final int client;
	private final int org;
	private final Path root;
	private final EnumMap<Output, MProcess> jasperReports = new EnumMap<>(Output.class);
	private final EnumMap<Output, MPrintFormat> formats = new EnumMap<>(Output.class);

	MtmPaperwork(Properties ctx, MRole role, int client, int org, Path root) {
		this.ctx = ctx;
		this.role = role;
		this.client = client;
		this.org = org;
		this.root = root;
	}

	void checkReports() {
		for (Output output : Output.values()) {
			List<MProcess> matches = new Query(ctx, MProcess.Table_Name,
					"UPPER(Name)=UPPER(?) AND AD_Client_ID IN (0,?) AND AD_Org_ID IN (0,?)", null)
					.setParameters(output.process, client, org).setOnlyActiveRecords(true).list();
			if (matches.size() != 1 || !Boolean.TRUE.equals(role.getProcessAccess(matches.get(0).get_ID())))
				throw new AdempiereUserError("Missing, ambiguous or inaccessible process: " + output.process);
			MProcess process = matches.get(0);
			if (output == Output.LABELS)
				continue;
			if (!process.isReport() || present(process.getProcedureName()))
				throw new AdempiereUserError("Unsupported report process: " + output.process);
			if (present(process.getJasperReport())) {
				if (present(process.getClassname()) && !ProcessUtil.JASPER_STARTER_CLASS.equals(process.getClassname()))
					throw new AdempiereUserError("Unsupported Jasper preprocessor: " + output.process);
				jasperReports.put(output, process);
			} else {
				if (present(process.getClassname()))
					throw new AdempiereUserError("Unsupported native report preprocessor: " + output.process);
				formats.put(output, printFormat(process));
			}
		}
	}

	List<Path> export(int runId, Set<String> numbers, Consumer<String> log) throws IOException {
		MBLDMtomProduction run = new MBLDMtomProduction(ctx, runId, null);
		if (!DocAction.STATUS_Completed.equals(run.getDocStatus()) || run.getAD_Client_ID() != client
				|| run.getAD_Org_ID() != org)
			throw new AdempiereUserError("MTM completion or scope could not be verified.");
		String number = safeSegment(run.getDocumentNo());
		String prefix = "SO-" + String.join("-", numbers.stream().map(MtmPaperwork::safeSegment).toList())
				+ "-MTM-" + number;
		Path directory = Files.createDirectories(root.resolve(Integer.toString(client))
				.resolve(Integer.toString(org)).resolve(runId + "-" + number));
		List<Path> saved = new ArrayList<>();
		for (Output output : Output.values()) {
			Path target = directory.resolve(prefix + output.suffix);
			try {
				Path checksum = directory.resolve(target.getFileName() + ".sha256");
				if (valid(target, output, number) && Files.isRegularFile(checksum)
						&& digest(target).equals(Files.readString(checksum))) {
					saved.add(target);
					log.accept("Verified existing " + target);
					continue;
				}
				Path temporary = Files.createTempFile(directory, ".mtm-", output.suffix);
				try {
					if (output == Output.LABELS)
						Files.writeString(temporary, MtmLabels.renderProduction(run), Charset.defaultCharset());
					else if (jasperReports.containsKey(output))
						renderJasper(jasperReports.get(output), runId, temporary);
					else
						renderPdf(formats.get(output), runId, temporary);
					if (!valid(temporary, output, number))
						throw new IOException("Generated file was empty, invalid or for a different run.");
					String hash = digest(temporary);
					Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
					Path checksumTemp = Files.createTempFile(directory, ".mtm-", ".sha256");
					try {
						Files.writeString(checksumTemp, hash);
						Files.move(checksumTemp, checksum, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
					} finally {
						Files.deleteIfExists(checksumTemp);
					}
				} finally {
					Files.deleteIfExists(temporary);
				}
				saved.add(target);
				log.accept("Saved and verified " + target);
			} catch (Exception failure) {
				LOG.log(Level.WARNING, "Failed to export " + target.getFileName(), failure);
				log.accept("Missing or unverified " + target.getFileName() + ": " + failure.getMessage());
			}
		}
		return saved;
	}

	static Path bundle(List<Path> saved) throws IOException {
		Path directory = Files.createTempDirectory("mtm-paperwork-");
		Path zip = directory.resolve("MTM-factory-paperwork.zip");
		directory.toFile().deleteOnExit();
		zip.toFile().deleteOnExit();
		try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(zip))) {
			for (Path file : saved) {
				String name = file.getParent().getFileName() + "/" + file.getFileName();
				output.putNextEntry(new ZipEntry(name));
				Files.copy(file, output);
				output.closeEntry();
			}
		}
		return zip;
	}

	private static String digest(Path path) throws IOException {
		try (InputStream input = Files.newInputStream(path)) {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] buffer = new byte[8192];
			int count;
			while ((count = input.read(buffer)) != -1)
				digest.update(buffer, 0, count);
			return HexFormat.of().formatHex(digest.digest());
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

	private MPrintFormat printFormat(MProcess process) {
		MPrintFormat format;
		if (process.getAD_ReportView_ID() > 0) {
			format = new Query(ctx, MPrintFormat.Table_Name,
					"AD_ReportView_ID=? AND AD_Client_ID IN (0,?)", null)
					.setParameters(process.getAD_ReportView_ID(), client).setOnlyActiveRecords(true)
					.setOrderBy("AD_Client_ID DESC, IsDefault DESC, AD_PrintFormat_ID").first();
		} else {
			format = MPrintFormat.get(ctx, process.getAD_PrintFormat_ID(), false);
		}
		if (format == null || !format.isActive() || format.getItemCount() <= 0
				|| (format.getAD_Client_ID() != 0 && format.getAD_Client_ID() != client))
			throw new AdempiereUserError("No usable print format for " + process.getName());
		MTable table = MTable.get(ctx, format.getAD_Table_ID());
		if (table == null || table.getColumnIndex(RUN_COLUMN) < 0 || !role.isCanReport(table.get_ID()))
			throw new AdempiereUserError("Report is inaccessible or lacks " + RUN_COLUMN + ": " + process.getName());
		return format;
	}

	private void renderPdf(MPrintFormat format, int runId, Path target) throws IOException {
		MQuery query = new MQuery(format.getAD_Table_ID());
		query.addRestriction(RUN_COLUMN, MQuery.EQUAL, runId);
		PrintInfo info = new PrintInfo(format.getName(), format.getAD_Table_ID(), runId, 0);
		ReportEngine engine = new ReportEngine(ctx, format, query, info);
		if (engine.getRowCount() <= 0 || !engine.createPDF(target.toFile()))
			throw new IOException("The run returned no report data or PDF export failed.");
	}

	private void renderJasper(MProcess report, int runId, Path target) throws Exception {
		MPInstance instance = new MPInstance(report, runId);
		instance.saveEx();
		for (MProcessPara parameter : report.getParameters()) {
			if (RUN_COLUMN.equalsIgnoreCase(parameter.getColumnName()))
				instance.createParameter(parameter.getSeqNo(), parameter.getColumnName(), runId);
		}
		ProcessInfo info = new ProcessInfo(report.getName(), report.get_ID(), MBLDMtomProduction.Table_ID, runId);
		info.setAD_PInstance_ID(instance.get_ID());
		info.setAD_Client_ID(client);
		info.setAD_User_ID(Env.getAD_User_ID(ctx));
		info.setClassName(ProcessUtil.JASPER_STARTER_CLASS);
		info.setPrintPreview(false);
		info.setIsBatch(true);
		Trx transaction = Trx.get(Trx.createTrxName("MtmReport"), true);
		try {
			ProcessUtil.startJavaProcess(ctx, info, transaction, false);
			if (info.isError() || info.getRowCount() <= 0 || info.getPDFReport() == null)
				throw new IOException("The Jasper report returned no data or PDF: " + info.getSummary());
			Path generated = info.getPDFReport().toPath();
			try {
				Files.copy(generated, target, StandardCopyOption.REPLACE_EXISTING);
			} finally {
				Files.deleteIfExists(generated);
			}
		} finally {
			transaction.rollback();
			transaction.close();
		}
	}

	private static boolean present(String value) {
		return value != null && !value.isBlank();
	}

	private static String safeSegment(String value) {
		if (value == null || !value.matches("[A-Za-z0-9][A-Za-z0-9._-]*"))
			throw new AdempiereUserError("Document number is unsuitable for the paperwork filename: " + value);
		return value;
	}

	private static boolean valid(Path path, Output output, String run) throws IOException {
		if (!Files.isRegularFile(path) || Files.size(path) == 0)
			return false;
		if (output == Output.LABELS) {
			String content = Files.readString(path, Charset.defaultCharset());
			return content.startsWith("^XA") && content.stripTrailing().endsWith("^XZ")
					&& content.contains("^FD" + run + "->");
		}
		try (InputStream input = Files.newInputStream(path)) {
			return "%PDF-".equals(new String(input.readNBytes(5), StandardCharsets.US_ASCII));
		}
	}
}
