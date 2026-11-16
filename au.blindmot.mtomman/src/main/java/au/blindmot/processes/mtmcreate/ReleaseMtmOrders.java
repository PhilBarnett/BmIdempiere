package au.blindmot.processes.mtmcreate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.compiere.model.MAttachment;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MRole;
import org.compiere.model.MSysConfig;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import au.blindmot.model.MBLDMtomItemLine;
import au.blindmot.model.MBLDMtomProduction;
import au.blindmot.model.MBldMtmInstall;

/** Releases reviewed orders through the standard document actions. */
public class ReleaseMtmOrders extends SvrProcess {
	private static final String INSTALLATION = "Bld_Mtm_Install_ID";
	private static final String PRODUCTION = MBLDMtomProduction.Table_Name;

	private String orderNumbers;
	private int installationId;
	private boolean release;
	private int selectionIssues;
	private int client;
	private int org;
	private MRole role;

	@Override
	protected void prepare() {
		for (ProcessInfoParameter parameter : getParameter()) {
			switch (parameter.getParameterName()) {
				case "OrderNumbers" -> orderNumbers = parameter.getParameterAsString();
				case INSTALLATION -> installationId = parameter.getParameterAsInt();
				case "ReleaseReviewedOrders" -> release = parameter.getParameterAsBoolean();
				default -> throw new AdempiereUserError("Unknown parameter " + parameter.getParameterName());
			}
		}
	}

	@Override
	protected String doIt() throws Exception {
		client = Env.getAD_Client_ID(getCtx());
		org = Env.getAD_Org_ID(getCtx());
		role = MRole.getDefault(getCtx(), false);
		if (client <= 0 || org <= 0 || role == null || !role.isOrgAccess(org, release))
			throw new AdempiereUserError("Select the Blindmotion client and the job organisation.");

		Set<Integer> selected = selectOrders();
		MtmPaperwork paperwork = null;
		if (release) {
			String directory = MSysConfig.getValue("BLD_MTM_PAPERWORK_DIRECTORY", "", client, org);
			if (directory.isBlank() || !Path.of(directory).isAbsolute()
					|| !Files.isDirectory(Path.of(directory)) || !Files.isWritable(Path.of(directory)))
				throw new AdempiereUserError("Configure a writable absolute BLD_MTM_PAPERWORK_DIRECTORY before release.");
			paperwork = new MtmPaperwork(getCtx(), role, client, org, Path.of(directory));
			paperwork.checkReports();
		}

		Set<Integer> handled = new LinkedHashSet<>();
		Set<Integer> ready = new LinkedHashSet<>();
		Set<Integer> installations = new LinkedHashSet<>();
		List<Path> files = new ArrayList<>();
		int issues = selectionIssues;
		for (int orderId : selected) {
			if (handled.contains(orderId))
				continue;
			MOrder order = new MOrder(getCtx(), orderId, null);
			int job = order.get_ValueAsInt(INSTALLATION);
			if (job > 0)
				installations.add(job);
			try {
				Run run = reviewAndRelease(orderId, selected);
				handled.addAll(run.orders());
				if (release) {
					List<Path> saved = export(paperwork, run, order);
					files.addAll(saved);
					if (saved.size() == MtmPaperwork.OUTPUT_COUNT)
						ready.addAll(run.orders());
					else
						issues++;
				}
			} catch (Exception failure) {
				issues++;
				log.log(Level.WARNING, "MTM release for " + order.getDocumentNo(), failure);
				addBufferLog(0, null, null, "SO " + order.getDocumentNo() + ": " + failure.getMessage(),
						MOrder.Table_ID, orderId);
			}
		}
		if (release) {
			if (processUI != null && !files.isEmpty()) {
				try {
					processUI.download(MtmPaperwork.bundle(files).toFile());
				} catch (Exception failure) {
					issues++;
					log.log(Level.WARNING, "Paperwork download could not be offered", failure);
					addBufferLog(0, null, null, "ZIP unavailable. Individual file locations are in this log.", 0, 0);
				}
			}
			for (int job : installations) {
				if (canReadInstallation(job)) {
					MBldMtmInstall installation = new MBldMtmInstall(getCtx(), job, null);
					List<String> outstanding = ordersForInstallation(job).stream()
							.filter(order -> !ready.contains(order.get_ID()))
							.map(MOrder::getDocumentNo).toList();
					addBufferLog(0, null, null, installationName(installation) + " (" + installation.getStatus() + "): "
							+ (outstanding.isEmpty() ? "paperwork saved. Ready for manual Pre Production."
									: "outstanding orders " + outstanding), installation.get_Table_ID(), job);
				}
			}
		}
		return (release ? "Release and paperwork" : "Read-only inspection") + " finished with "
				+ issues + " issue(s). See process log.";
	}

	private List<Path> export(MtmPaperwork paperwork, Run run, MOrder order) throws Exception {
		Trx transaction = Trx.get(Trx.createTrxName("MtmPaperwork"), true);
		try {
			lock(PRODUCTION, "Bld_Mtom_Production_ID", run.productionId(), transaction.getTrxName());
			return paperwork.export(run.productionId(), run.numbers(), message -> note(order, message));
		} finally {
			transaction.rollback();
			transaction.close();
		}
	}

	private Set<Integer> selectOrders() {
		Set<Integer> selected = new LinkedHashSet<>();
		boolean byNumber = orderNumbers != null && !orderNumbers.isBlank();
		if (byNumber == (installationId > 0))
			throw new AdempiereUserError("Specify sales order numbers or an Installation, using one input.");
		if (installationId > 0) {
			if (!canReadInstallation(installationId))
				throw new AdempiereUserError("Installation is unavailable in this organisation.");
			ordersForInstallation(installationId).forEach(order -> selected.add(order.get_ID()));
		} else if (byNumber) {
			for (String number : orderNumbers.strip().split("[,;\\s]+")) {
				List<MOrder> matches = new Query(getCtx(), MOrder.Table_Name,
						"AD_Client_ID=? AND AD_Org_ID=? AND IsSOTrx='Y' AND DocumentNo=?", null)
						.setParameters(client, org, number).setOnlyActiveRecords(true).list();
				if (matches.size() != 1) {
					selectionIssues++;
					addBufferLog(0, null, null, "Sales order " + number + " is missing or ambiguous in this organisation.", 0, 0);
				} else {
					selected.add(matches.get(0).get_ID());
				}
			}
		}
		if (selected.isEmpty())
			throw new AdempiereUserError("No sales orders were selected.");
		return selected;
	}

	private List<MOrder> ordersForInstallation(int job) {
		return new Query(getCtx(), MOrder.Table_Name,
				INSTALLATION + "=? AND AD_Client_ID=? AND IsSOTrx='Y'", null)
				.setParameters(job, client).setOnlyActiveRecords(true).setOrderBy("C_Order_ID").list();
	}

	private boolean canReadInstallation(int job) {
		MBldMtmInstall installation = new MBldMtmInstall(getCtx(), job, null);
		return installation.get_ID() == job && installation.getAD_Client_ID() == client
				&& installation.getAD_Org_ID() == org
				&& role.checkAccessSQL(MTable.get(getCtx(), installation.get_Table_ID()), job, null, false);
	}

	private Run reviewAndRelease(int orderId, Set<Integer> selected) throws Exception {
		Trx transaction = Trx.get(Trx.createTrxName("ReleaseMTM"), true);
		try {
			String trx = transaction.getTrxName();
			MOrder order = new MOrder(getCtx(), orderId, trx);
			checkAccess(order);
			int job = order.get_ValueAsInt(INSTALLATION);
			if (job > 0 && !canReadInstallation(job))
				throw new AdempiereUserError("Linked Installation is unavailable in this organisation.");
			if (release) {
				// Serialize releases of orders that share an Installation, then lock each source order.
				if (job > 0)
					lock("Bld_Mtm_Install", INSTALLATION, job, trx);
				lock(MOrder.Table_Name, "C_Order_ID", orderId, trx);
				order = new MOrder(getCtx(), orderId, trx);
				checkAccess(order);
				if (order.get_ValueAsInt(INSTALLATION) != job)
					throw new AdempiereUserError("Installation link changed. Reinspect the order.");
			}
			MBLDMtomProduction production = existing(orderId, trx);
			Set<Integer> runOrders = new TreeSet<>();
			runOrders.add(orderId);
			if (production != null) {
				checkAccess(production);
				if (release) {
					lock(PRODUCTION, "Bld_Mtom_Production_ID", production.get_ID(), trx);
					production = new MBLDMtomProduction(getCtx(), production.get_ID(), trx);
					checkAccess(production);
				}
				runOrders.add(production.getC_Order_ID());
				for (MBLDMtomItemLine item : production.getLines(true, null)) {
					MOrderLine source = new MOrderLine(getCtx(), item.getC_OrderLine_ID(), trx);
					runOrders.add(source.getC_Order_ID());
				}
			}
			if (!selected.containsAll(runOrders))
				throw new AdempiereUserError("Existing run covers additional orders. Select the whole run or Installation.");

			Set<String> numbers = new TreeSet<>(MtmPaperwork.DOCUMENT_ORDER);
			Map<Integer, MOrderLine> sources = new HashMap<>();
			for (int id : runOrders) {
				if (release && id != orderId)
					lock(MOrder.Table_Name, "C_Order_ID", id, trx);
				MOrder source = new MOrder(getCtx(), id, trx);
				checkAccess(source);
				if (production != null && source.getC_BPartner_ID() != production.getC_BPartner_ID())
					throw new AdempiereUserError("Existing run and source order refer to different customers.");
				if (release)
					lockOrderLines(id, trx);
				if (source.get_ValueAsInt(INSTALLATION) != job)
					throw new AdempiereUserError("Existing run contains orders from different jobs.");
				if (id != orderId) {
					MBLDMtomProduction match = existing(id, trx);
					if (match == null || match.get_ID() != production.get_ID())
						throw new AdempiereUserError("Existing run coverage changed. Reinspect the order.");
				}
				numbers.add(source.getDocumentNo());
				sources.putAll(review(source, job));
			}
			if (numbers.size() != runOrders.size())
				throw new AdempiereUserError("Existing run has duplicate sales order document numbers.");
			String identity = "SO " + numbers + (job == 0 ? ", no Installation"
					: ", " + installationName(new MBldMtmInstall(getCtx(), job, trx))
						+ " (" + new MBldMtmInstall(getCtx(), job, trx).getStatus() + ")");
			if (!release) {
				if (production != null)
					verifyCoverage(production, sources, trx);
				note(order, identity + (production == null ? ", no MTM run" : ", MTM "
						+ production.getDocumentNo() + " (" + production.getDocStatus() + ")"));
				return new Run(0, runOrders, numbers);
			}
			if (production != null)
				verifyCoverage(production, sources, trx);
			for (int id : runOrders) {
				MOrder source = new MOrder(getCtx(), id, trx);
				if (production != null && !DocAction.STATUS_Completed.equals(source.getDocStatus()))
					throw new AdempiereUserError("Existing run has an order that is not Completed: " + source.getDocumentNo());
				if (!DocAction.STATUS_Completed.equals(source.getDocStatus())) {
					requireDraftStatus(source.getDocStatus(), "Order " + source.getDocumentNo());
					complete(source, source);
				}
			}
			if (production == null) {
				if (!role.isTableAccess(MBLDMtomProduction.Table_ID, false))
					throw new AdempiereUserError("The role cannot create MTM production.");
				production = new MBLDMtomProduction(getCtx(), null, trx, orderId);
				production.setDocStatus(DocAction.STATUS_Drafted);
				production.setDocAction(DocAction.ACTION_Prepare);
				production.saveEx();
				if (!production.processIt(DocAction.ACTION_Prepare))
					throw new AdempiereUserError("MTM preparation failed: " + production.getProcessMsg());
				production.saveEx();
				production.getLines(true, null);
				verifyCoverage(production, sources, trx);
			}
			if (!DocAction.STATUS_Completed.equals(production.getDocStatus())) {
				requireDraftStatus(production.getDocStatus(), "MTM " + production.getDocumentNo());
				complete(production, production);
			}
			verifyCoverage(production, sources, trx);
			transaction.commit(true);
			note(order, identity + ", MTM " + production.getDocumentNo() + " Completed");
			return new Run(production.get_ID(), runOrders, numbers);
		} finally {
			transaction.rollback();
			transaction.close();
		}
	}

	private void checkAccess(PO record) {
		if (record.get_ID() <= 0 || !record.isActive() || record.getAD_Client_ID() != client
				|| record.getAD_Org_ID() != org || !role.checkAccessSQL(MTable.get(getCtx(), record.get_Table_ID()), record.get_ID(), null, release))
			throw new AdempiereUserError("Access denied for " + record.get_TableName() + " " + record.get_ID());
		if (record instanceof MOrder order && !order.isSOTrx())
			throw new AdempiereUserError("Selected document is not a sales order.");
	}

	private Map<Integer, MOrderLine> review(MOrder order, int job) {
		Set<String> attachments = new LinkedHashSet<>();
		if (job > 0)
			attachmentNames(new MBldMtmInstall(getCtx(), job, order.get_TrxName())).forEach(attachments::add);
		Set<String> orderAttachments = attachmentNames(order);
		note(order, "SO " + order.getDocumentNo() + " sources, Installation first " + attachments
				+ "; sales order " + orderAttachments);
		Map<Integer, MOrderLine> lines = new HashMap<>();
		Set<String> problems = new LinkedHashSet<>();
		for (MOrderLine line : order.getLines()) {
			if (line.getM_Product_ID() <= 0)
				continue;
			MProduct product = new MProduct(getCtx(), line.getM_Product_ID(), order.get_TrxName());
			if (!product.get_ValueAsBoolean("ismadetomeasure"))
				continue;
			if (!line.get_ValueAsBoolean("ischeckmeasured"))
				problems.add("line " + line.getLine() + " unchecked");
			if (product.isManufactured()) {
				if (line.getQtyEntered().signum() <= 0)
					problems.add("line " + line.getLine() + " has non-positive quantity");
				lines.put(line.get_ID(), line);
			}
		}
		if (attachments.isEmpty() && orderAttachments.isEmpty())
			problems.add("no measurement attachment found");
		if (lines.isEmpty())
			problems.add("no manufactured MTM lines");
		if (!problems.isEmpty())
			throw new AdempiereUserError("SO " + order.getDocumentNo() + ": " + String.join("; ", problems));
		return lines;
	}

	private Set<String> attachmentNames(PO record) {
		Set<String> names = new LinkedHashSet<>();
		MAttachment attachment = record.getAttachment();
		if (attachment != null) {
			for (int index = 0; index < attachment.getEntryCount(); index++)
				names.add(attachment.getEntryName(index));
		}
		return names;
	}

	private MBLDMtomProduction existing(int orderId, String trx) {
		List<MBLDMtomProduction> runs = new Query(getCtx(), PRODUCTION,
				"DocStatus NOT IN ('VO','RE') AND (C_Order_ID=? OR EXISTS (SELECT 1"
				+ " FROM Bld_Mtom_Item_Line i JOIN C_OrderLine l ON l.C_OrderLine_ID=i.C_OrderLine_ID"
				+ " WHERE i.Bld_Mtom_Production_ID=Bld_Mtom_Production.Bld_Mtom_Production_ID AND l.C_Order_ID=?))", trx)
				.setParameters(orderId, orderId).list();
		if (runs.size() > 1)
			throw new AdempiereUserError("Multiple production records cover this order. Resolve coverage in ERP.");
		return runs.isEmpty() ? null : runs.get(0);
	}

	private void verifyCoverage(MBLDMtomProduction run, Map<Integer, MOrderLine> sources, String trx) {
		Map<Integer, List<MBLDMtomItemLine>> items = Arrays.stream(run.getLines(true, null))
				.collect(Collectors.groupingBy(MBLDMtomItemLine::getC_OrderLine_ID));
		if (!items.keySet().equals(sources.keySet()))
			throw new AdempiereUserError("MTM " + run.getDocumentNo() + " source line coverage differs from the order.");
		for (MOrderLine source : sources.values()) {
			List<MBLDMtomItemLine> actual = items.get(source.get_ID());
			Map<Integer, Long> quantities = actual.stream().map(MBLDMtomItemLine::getM_Product_ID)
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			MProduct product = new MProduct(getCtx(), source.getM_Product_ID(), trx);
			// Compare the source configuration on the primary finished item.
			if (!quantities.equals(run.expectedProducts(product, source).entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().longValue())))
					|| actual.stream().anyMatch(item -> item.getM_Product_ID() == source.getM_Product_ID()
							&& (item.getattributesetinstance_id() != source.getM_AttributeSetInstance_ID()
								|| item.getBld_Line_ProductSetInstance_ID()
										!= source.get_ValueAsInt("Bld_Line_ProductSetInstance_ID"))))
				throw new AdempiereUserError("MTM " + run.getDocumentNo() + " differs from source line " + source.getLine()
						+ " (order line ID " + source.get_ID() + ").");
		}
	}

	private void complete(DocAction document, PO record) throws Exception {
		int docType = record.get_ValueAsInt("C_DocTypeTarget_ID");
		if (docType <= 0)
			docType = record.get_ValueAsInt("C_DocType_ID");
		String[] actions = { DocAction.ACTION_Complete };
		if (role.checkActionAccess(client, docType, actions, actions.length) == 0)
			throw new AdempiereUserError("The role cannot complete " + document.getDocumentNo() + ".");
		if (!document.processIt(DocAction.ACTION_Complete) || !DocAction.STATUS_Completed.equals(document.getDocStatus()))
			throw new AdempiereUserError("Completion failed for " + document.getDocumentNo() + ": " + document.getProcessMsg());
		record.saveEx();
	}

	private void requireDraftStatus(String status, String name) {
		if (!Set.of(DocAction.STATUS_Drafted, DocAction.STATUS_InProgress, DocAction.STATUS_Invalid).contains(status))
			throw new AdempiereUserError(name + " requires manual review (status " + status + ").");
	}

	private void lock(String table, String column, int id, String trx) throws Exception {
		try (PreparedStatement statement = DB.prepareStatement(
				"SELECT " + column + " FROM " + table + " WHERE " + column + "=? FOR UPDATE", trx)) {
			statement.setInt(1, id);
			try (ResultSet result = statement.executeQuery()) {
				if (!result.next())
					throw new AdempiereUserError("Record disappeared: " + table + " " + id);
			}
		}
	}

	private void lockOrderLines(int orderId, String trx) throws Exception {
		try (PreparedStatement statement = DB.prepareStatement(
				"SELECT C_OrderLine_ID FROM C_OrderLine WHERE C_Order_ID=? ORDER BY C_OrderLine_ID FOR UPDATE", trx)) {
			statement.setInt(1, orderId);
			try (ResultSet rows = statement.executeQuery()) {
				while (rows.next()) {
					// Fetch every row to acquire every line lock before checking the flags.
				}
			}
		}
	}

	private static String installationName(MBldMtmInstall installation) {
		return "Installation " + installation.getValue() + " (ID " + installation.get_ID() + ")";
	}

	private void note(MOrder order, String message) {
		addBufferLog(0, null, null, message, MOrder.Table_ID, order.get_ID());
	}

	private record Run(int productionId, Set<Integer> orders, Set<String> numbers) {}
}
