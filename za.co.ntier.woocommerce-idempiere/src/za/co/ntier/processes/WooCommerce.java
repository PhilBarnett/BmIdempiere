package za.co.ntier.processes;

import org.compiere.model.MOrder;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Trx;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.icoderman.woocommerce.ApiVersionType;
import com.icoderman.woocommerce.EndpointBaseType;
import com.icoderman.woocommerce.Woocommerce;
import com.icoderman.woocommerce.WooCommerceAPI;
import com.icoderman.woocommerce.oauth.OAuthConfig;

import au.blindmot.model.MBldMtmInstall;
import za.co.ntier.model.X_zz_woocommerce;
import za.co.ntier.woocommerce.WcOrder;
import za.co.ntier.woocommerce.WcOrder.SampleSwatchMappingError;

/**
 *
 * Import unsynchronised WooCommerce orders into iDempiere.
 *
 * @author yogan naidoo, Phil Barnett.
 */

public class WooCommerce extends SvrProcess {

	private static final String ORDER_ID_PARAMETER = "WooCommerceOrderID";
	private static final String PROCESSING_STATUS = "processing";
	private static final String STAGING_WOO_HOST = "staging-online.blindmotion.com.au";
	private static final int CURTAIN_WOO_PRODUCT_ID = 1298;
	// Temporary rehearsal for the single unpaid production WooCommerce test order.
	private static final int LIVE_CURTAIN_QA_ORDER_ID = 10192;
	private static final String LIVE_WOO_HOST = "online.blindmotion.com.au";
	private static final String LIVE_CURTAIN_QA_LOCATION = "QA double curtain - do not fulfil";
	private Integer wooCommerceOrderId;


	/**
	 * Import each eligible order on its own, then acknowledge Woo. A later bad
	 * order must not undo either of the good orders around it.
	 */
	private String syncOrders() throws SQLException {
		PO defaults = new Query(getCtx(), X_zz_woocommerce.Table_Name, null, null)
				.setClient_ID().setOnlyActiveRecords(true).firstOnly();
		if (defaults == null) {
			throw new AdempiereUserError("WooCommerce Defaults must be configured in iDempiere.");
		}
		Woocommerce woo = new WooCommerceAPI(new OAuthConfig((String) defaults.get_Value("url"),
				(String) defaults.get_Value("consumerkey"), (String) defaults.get_Value("consumersecret")),
				ApiVersionType.V3);
		String endpoint = EndpointBaseType.ORDERS.getValue();
		int imported = 0, rejected = 0, unacknowledged = 0;
		for (Object item : ordersToSync(woo, defaults)) {
			if (!(item instanceof Map<?, ?> order) || !(order.get("id") instanceof Integer id) || id <= 0) {
				rejected++;
				addLog("Skipped a WooCommerce response without a valid order ID.");
				continue;
			}
			boolean live = isLiveCurtainRehearsal(order, defaults);
			boolean sample = isStagingSampleRehearsal(order, defaults);
			boolean rehearsal = live || sample || isMarkedCurtainRehearsal(order);
			if (rehearsal && wooCommerceOrderId == null) {
				continue;
			}
			// Rehearsals retain their separate document identifiers and are
			// deliberately excluded from batch exports and Woo updates.
			String documentNo = documentNoFor(order, rehearsal, sample);
			try {
				OrderReceipt receipt = importOrder(defaults, order, documentNo, rehearsal, live, sample);
				confirmCommittedOrder(receipt, defaults.getAD_Org_ID(), documentNo);
				imported++;
			} catch (RuntimeException | SQLException failure) {
				// Continue past bad order data, never past a known infrastructure
				// failure or a transaction whose rollback could not be confirmed.
				String message = failure instanceof ExistingOrder
						? failure.getMessage()
						: failure instanceof LineMismatch ? failure.getMessage()
						: failure instanceof SampleSwatchMappingError
								? "WooCommerce order " + id + ": " + failure.getMessage() + " WooCommerce unchanged."
						: "WooCommerce order " + id + ": ERP import not confirmed. WooCommerce unchanged.";
				report(message, failure);
				if (wooCommerceOrderId != null || isDatabaseUnavailable(failure)) {
					throw new AdempiereUserError(message + " See process log.");
				}
				rejected++;
				continue;
			}
			addLog("WooCommerce order " + id + " committed in ERP as " + documentNo
					+ (rehearsal ? " (QA draft); WooCommerce unchanged." : "."));
			if (!rehearsal) {
				// This is reachable only after this invocation committed the order.
				// An existing ERP document is not a receipt for a previous invocation.
				try {
					WooOrderAcknowledgement.apply(id, () -> woo.get(endpoint, id),
							body -> woo.update(endpoint, id, body));
				} catch (RuntimeException failure) {
					unacknowledged++;
					report("WooCommerce order " + id + ": ERP " + documentNo
							+ " is committed, but Woo acknowledgement was not confirmed. Reconcile both records; do not reimport.",
							failure);
				}
			}
		}
		if (wooCommerceOrderId != null && imported == 1) {
			return "WooCommerce order " + wooCommerceOrderId + " imported in ERP"
					+ (unacknowledged == 0 ? "." : "; Woo acknowledgement unconfirmed. See process log.");
		}
		return "WooCommerce import: " + imported + " committed, " + rejected + " not confirmed, "
				+ unacknowledged + " requiring Woo reconciliation. See process log.";
	}

	/** Owns the lock and the complete ERP write: header, lines and dependent records. */
	private OrderReceipt importOrder(PO defaults, Map<?, ?> order, String documentNo, boolean rehearsal,
			boolean live, boolean sample) throws SQLException {
		int id = (Integer) order.get("id");
		// The process transaction belongs to SvrProcess (or its caller). Do not
		// partly commit it: all this order's models must use the owned name.
		String name = Trx.createTrxName("WooOrder");
		Trx trx = Trx.get(name, true);
		Connection connection = null;
		boolean committed = false;
		String phase = "checking ERP configuration and existing order";
		try {
			connection = trx.getConnection();
			PO lockedDefaults = new Query(getCtx(), X_zz_woocommerce.Table_Name,
					X_zz_woocommerce.COLUMNNAME_zz_woocommerce_ID + "=?", name)
					.setParameters(defaults.get_ID()).setClient_ID().setOnlyActiveRecords(true)
					.setForUpdate(true).firstOnly();
			if (lockedDefaults == null || !defaults.get_Value("url").equals(lockedDefaults.get_Value("url"))
					|| defaults.getAD_Org_ID() != lockedDefaults.getAD_Org_ID()) {
				throw new ImportUnavailable("WooCommerce Defaults changed while the importer was running.");
			}
			// Serialise check + create for batches and targeted calls that use this
			// store. The lock is released before any Woo HTTP requests.
			String legacyRef = rehearsal && !live ? documentNo : Integer.toString(id);
			if (new Query(getCtx(), MOrder.Table_Name,
					"AD_Org_ID=? AND IsSOTrx='Y' AND (DocumentNo=? OR DocumentNo=? OR POReference=?)", name)
					.setParameters(lockedDefaults.getAD_Org_ID(), documentNo, legacyRef, legacyRef)
					.setClient_ID().match()) {
				throw new ExistingOrder("WooCommerce order " + id
						+ ": matching ERP document or reference already exists; reconcile it without reimporting.");
			}
			// Reject the entire order if a required row cannot be created.
			if (!(order.get("line_items") instanceof List<?> lines) || lines.isEmpty()) {
				throw new AdempiereUserError("Order has no WooCommerce items.");
			}
			phase = "saving the ERP order header";
			WcOrder wcOrder = new WcOrder(getCtx(), name, lockedDefaults);
			wcOrder.createOrder(order, documentNo);
			int erpOrderId = wcOrder.getcOrderID();
			int lineCount = 0;
			int itemNumber = 0;
			for (Object item : lines) {
				itemNumber++;
				phase = "mapping and saving Woo item " + itemNumber;
				if (item instanceof Map<?, ?> line && line.get("product_id") instanceof Integer productId) {
					phase += " (Woo product ID " + productId + ")";
				}
				if (!(item instanceof Map<?, ?> line) || !wcOrder.createOrderLine(line, order)) {
					throw new AdempiereUserError("Order " + id + " failed line mapping.");
				}
				int expected = wcOrder.getExpectedItemLineCount();
				int saved = DB.getSQLValueEx(name,
						"SELECT COUNT(*) FROM C_OrderLine WHERE C_Order_ID=? AND IsActive='Y'", erpOrderId);
				if (expected <= 0 || saved != lineCount + expected) {
					throw new LineMismatch("WooCommerce order " + id + ", item " + itemNumber
							+ ": expected " + expected + " ERP lines, found " + (saved - lineCount)
							+ ". Order rejected before commit. WooCommerce unchanged.");
				}
				lineCount = saved;
			}
			if (rehearsal) {
				phase = "saving the test draft";
				MOrder draft = new MOrder(getCtx(), wcOrder.getcOrderID(), name);
				if (!DocAction.STATUS_Drafted.equals(draft.getDocStatus()) || ((live || sample) && lineCount != 2)) {
					throw new AdempiereUserError("The QA order did not produce the expected draft order lines.");
				}
				draft.setDescription(sample ? "STAGING FABRIC SAMPLE QA - DO NOT FULFIL"
						: (live ? "LIVE" : "STAGING") + " CURTAIN QA - DO NOT FULFIL");
				draft.saveEx();
			} else {
				phase = "creating and linking the installation";
				createInstallation(wcOrder, name);
				phase = "saving shipping";
				wcOrder.createShippingCharge(order);
				phase = "saving payment";
				wcOrder.createPosPayment(order);
				phase = "processing the ERP order";
				wcOrder.completeOrder();
				lineCount++; // The existing import always adds one shipping charge, even at $0.
			}
			phase = "checking saved ERP lines before commit";
			Map<Integer, String> expectedLines = readOrderLines(erpOrderId, name);
			if (expectedLines.size() != lineCount) {
				throw new LineMismatch("WooCommerce order " + id + ": expected " + lineCount
						+ " active ERP lines including charges, found " + expectedLines.size()
						+ ". Order rejected before commit. WooCommerce unchanged.");
			}
			// Only a confirmed commit allows Woo status or customer-facing side
			// effects. A reported commit failure may still require reconciliation.
			phase = "committing the ERP order";
			commitOrder(trx, id);
			committed = true;
			return new OrderReceipt(erpOrderId, expectedLines);
		} catch (RuntimeException | SQLException failure) {
			addLog("WooCommerce order " + id + " failed while " + phase + ".");
			throw failure;
		} finally {
			finishTransaction(trx, connection, committed);
		}
	}

	private static void commitOrder(Trx trx, int wooId) throws ImportUnavailable {
		String message = "ERP commit was not confirmed for WooCommerce order " + wooId
				+ "; inspect the ERP record before retrying.";
		boolean confirmed;
		try {
			confirmed = trx.commit(true);
		} catch (RuntimeException | SQLException failure) {
			// A transaction listener can also fail after JDBC committed. A
			// successful cleanup rollback cannot settle that uncertainty.
			throw new ImportUnavailable(message, failure);
		}
		if (!confirmed) {
			throw new ImportUnavailable(message);
		}
	}

	private record OrderReceipt(int orderId, Map<Integer, String> lines) {}

	private static final class LineMismatch extends RuntimeException {
		private static final long serialVersionUID = 1L;

		LineMismatch(String message) {
			super(message);
		}
	}

	// Read line values and linked manufacturing data in one database snapshot.
	// Line IDs distinguish identical swatches or products ordered more than once.
	private static Map<Integer, String> readOrderLines(int orderId, String trxName) throws SQLException {
		String sql = """
				SELECT ol.C_OrderLine_ID, jsonb_build_array(
				    ol.Line, ol.M_Product_ID, ol.C_Charge_ID, ol.C_UOM_ID, ol.C_Tax_ID,
				    ol.QtyOrdered, ol.QtyEntered, ol.PriceActual, ol.LineNetAmt,
				    ol.M_AttributeSetInstance_ID, ol.BLD_Line_ProductSetInstance_ID,
				    (SELECT jsonb_agg(jsonb_build_array(ai.M_Attribute_ID,
				                 ai.M_AttributeValue_ID, ai.Value, ai.ValueNumber)
				                      ORDER BY ai.M_Attribute_ID)
				       FROM M_AttributeInstance ai
				      WHERE ai.M_AttributeSetInstance_ID = ol.M_AttributeSetInstance_ID),
				    (SELECT jsonb_agg(jsonb_build_array(c.M_Product_ID,
				                 c.BLD_Product_PartType_ID, c.M_AttributeSetInstance_ID,
				                 (SELECT jsonb_agg(jsonb_build_array(ca.M_Attribute_ID,
				                              ca.M_AttributeValue_ID, ca.Value, ca.ValueNumber)
				                                   ORDER BY ca.M_Attribute_ID)
				                    FROM M_AttributeInstance ca
				                   WHERE ca.M_AttributeSetInstance_ID = c.M_AttributeSetInstance_ID))
				                      ORDER BY c.BLD_Line_ProductInstance_ID)
				       FROM BLD_Line_ProductInstance c
				      WHERE c.BLD_Line_ProductSetInstance_ID = ol.BLD_Line_ProductSetInstance_ID
				        AND c.IsActive = 'Y'))::text
				FROM C_OrderLine ol WHERE ol.C_Order_ID = ? AND ol.IsActive = 'Y'
				ORDER BY ol.C_OrderLine_ID
				""";
		Map<Integer, String> lines = new LinkedHashMap<>();
		try (PreparedStatement statement = DB.prepareStatement(sql, trxName)) {
			statement.setInt(1, orderId);
			try (ResultSet result = statement.executeQuery()) {
				while (result.next()) {
					lines.put(result.getInt(1), result.getString(2));
				}
			}
		}
		return lines;
	}

	/** Read the exact record this run committed using a fresh database connection. */
	private void confirmCommittedOrder(OrderReceipt receipt, int orgId, String documentNo)
			throws ImportUnavailable {
		String message = "ERP commit returned success for " + documentNo
				+ ", but the saved order and lines could not be verified; inspect before retrying.";
		MOrder durableOrder;
		Map<Integer, String> savedLines;
		try {
			durableOrder = new Query(getCtx(), MOrder.Table_Name,
					"C_Order_ID=? AND AD_Org_ID=? AND DocumentNo=? AND IsSOTrx='Y'", null)
					.setParameters(receipt.orderId(), orgId, documentNo)
					.setClient_ID().setOnlyActiveRecords(true).firstOnly();
			savedLines = readOrderLines(receipt.orderId(), null);
		} catch (RuntimeException | SQLException failure) {
			throw new ImportUnavailable(message, failure);
		}
		if (durableOrder == null) {
			throw new ImportUnavailable(message);
		}
		if (!receipt.lines().equals(savedLines)) {
			List<Integer> missing = receipt.lines().keySet().stream()
					.filter(id -> !savedLines.containsKey(id)).toList();
			List<Integer> unexpected = savedLines.keySet().stream()
					.filter(id -> !receipt.lines().containsKey(id)).toList();
			List<Integer> changed = receipt.lines().keySet().stream()
					.filter(id -> savedLines.containsKey(id) && !receipt.lines().get(id).equals(savedLines.get(id)))
					.toList();
			throw new LineMismatch("ERP order " + documentNo + ": missing line IDs " + missing
					+ ", unexpected line IDs " + unexpected + ", changed line IDs " + changed
					+ ". WooCommerce unchanged. Reconcile the committed ERP order before retrying.");
		}
	}

	private void createInstallation(WcOrder order, String trxName) {
		if (order.orderTotalOverZero() <= 0) {
			return; // Free samples do not require an installation.
		}
		MBldMtmInstall install = new MBldMtmInstall(getCtx(), 0, trxName);
		install.setStatus("Acc");
		install.setC_BPartner_ID(order.getOrderBpID());
		install.setC_BPartner_Location_ID(order.getbPLocationId());
		install.setName("****ONLINE****");
		install.setdate_promised(Timestamp.from(Instant.now().plus(10, ChronoUnit.DAYS)));
		install.setComments(order.getCustomerNote());
		install.setis_checkmeasured(true);
		install.setcontract_received(true);
		install.saveEx();
		order.setBldMtmInstallID(install.get_ID());
	}

	private static void finishTransaction(Trx trx, Connection connection, boolean committed) throws ImportUnavailable {
		if (!committed && connection != null) {
			try {
				if (!trx.rollback(true)) {
					throw new SQLException("ERP rollback was not confirmed.");
				}
			} catch (RuntimeException | SQLException failure) {
				// iDempiere 12 close() resets auto-commit. Abort the connection
				// first so cleanup cannot commit a partially imported order.
				try {
					connection.abort(Runnable::run);
					trx.close();
				} catch (RuntimeException | SQLException abortFailure) {
					failure.addSuppressed(abortFailure);
				}
				throw new ImportUnavailable("ERP rollback unconfirmed; stop and inspect the database.", failure);
			}
		}
		try {
			trx.close();
		} catch (RuntimeException failure) {
			throw new ImportUnavailable("ERP transaction cleanup failed; inspect the ERP record before retrying.", failure);
		}
	}

	private static boolean isDatabaseUnavailable(Exception failure) {
		for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
			if (cause instanceof ImportUnavailable) {
				return true;
			}
			if (cause instanceof SQLException sql && sql.getSQLState() != null
					&& (sql.getSQLState().startsWith("08") || sql.getSQLState().startsWith("53")
							|| sql.getSQLState().startsWith("57P"))) {
				return true;
			}
		}
		return false;
	}

	private void report(String message, Exception failure) {
		// Remote exceptions can contain response bodies; log the class, not
		// customer data or credentials from exception text or payloads.
		String kind = failure.getClass().getSimpleName();
		if (failure instanceof ImportUnavailable) {
			message += " " + failure.getMessage();
		}
		String sqlState = null;
		StackTraceElement source = null;
		for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
			if (sqlState == null && cause instanceof SQLException sql && sql.getSQLState() != null) {
				sqlState = sql.getSQLState();
			}
			for (StackTraceElement frame : cause.getStackTrace()) {
				if (frame.getClassName().equals(WcOrder.class.getName())
						|| frame.getClassName().equals(WooCommerce.class.getName())) {
					if (source == null || source.getClassName().equals(WooCommerce.class.getName())) {
						source = frame;
					}
					break;
				}
			}
		}
		addLog(message + " (" + kind + (sqlState == null ? "" : ", SQLState " + sqlState)
				+ (source == null ? "" : ", " + source) + ")");
	}

	private static final class ImportUnavailable extends SQLException {
		private static final long serialVersionUID = 1L;

		ImportUnavailable(String message) {
			super(message);
		}

		ImportUnavailable(String message, Exception cause) {
			super(message, cause);
		}
	}

	private static final class ExistingOrder extends AdempiereUserError {
		private static final long serialVersionUID = 1L;

		ExistingOrder(String message) {
			super(message);
		}
	}
	// Read the same Processing queue as before unless the operator supplies an
	// exact order ID. Rehearsals still have their stricter eligibility rules.
	private List<?> ordersToSync(Woocommerce wooCommerce, PO defaults) {
		String endpoint = EndpointBaseType.ORDERS.getValue();
		if (wooCommerceOrderId == null) {
			return wooCommerce.getAll(endpoint, Map.of("per_page", "100", "offset", "0", "status", PROCESSING_STATUS));
		}

		Map<?, ?> order = wooCommerce.get(endpoint, wooCommerceOrderId);
		if (order == null || !wooCommerceOrderId.equals(order.get("id"))) {
			throw new AdempiereUserError("WooCommerce did not return order " + wooCommerceOrderId + ".");
		}
		if (!(order.get("line_items") instanceof List<?> items) || items.isEmpty()) {
			throw new AdempiereUserError("WooCommerce order " + wooCommerceOrderId + " has no order items.");
		}
		boolean liveRehearsal = isLiveCurtainRehearsal(order, defaults);
		boolean markedRehearsal = isMarkedCurtainRehearsal(order);
		boolean sampleRehearsal = isStagingSampleRehearsal(order, defaults);
		if (sampleRehearsal && (!PROCESSING_STATUS.equals(order.get("status"))
				|| !"0.00".equals(order.get("total")) || items.size() != 1
				|| !(items.get(0) instanceof Map<?, ?> line)
				|| !Integer.valueOf(128).equals(line.get("product_id"))
				|| !Integer.valueOf(1).equals(line.get("quantity")))) {
			throw new AdempiereUserError("The staging sample QA order must be Processing with one free Fabric Sample item.");
		}
		if (liveRehearsal) {
			validateLiveCurtainRehearsal(order, items);
		} else if (markedRehearsal) {
			validateCurtainRehearsal(order, items, (String) defaults.get_Value("url"));
		} else if (!PROCESSING_STATUS.equals(order.get("status"))) {
			throw new AdempiereUserError("WooCommerce order " + wooCommerceOrderId + " must be in Processing status.");
		}
		return List.of(order);
	}

	private static String documentNoFor(Map<?, ?> order, boolean rehearsal, boolean sampleRehearsal) {
		String orderId = order.get("id").toString();
		if (sampleRehearsal) {
			return "QA-SAMPLE-" + orderId;
		}
		return rehearsal ? "QA-CURTAIN-" + orderId : orderId;
	}

	// Staging and live WooCommerce can issue the same numeric ID. Isolate this
	// test fixture so the ERP copy stays a draft and the store status is untouched.
	private static boolean isStagingSampleRehearsal(Map<?, ?> order, PO defaults) {
		if (!Integer.valueOf(9230).equals(order.get("id"))
				|| !"TestERP two-swatch import QA. Do not dispatch.".equals(order.get("customer_note"))) {
			return false;
		}
		URI store = URI.create((String) defaults.get_Value("url"));
		return "https".equalsIgnoreCase(store.getScheme()) && STAGING_WOO_HOST.equalsIgnoreCase(store.getHost());
	}

	private static boolean isLiveCurtainRehearsal(Map<?, ?> order, PO defaults) {
		if (!Integer.valueOf(LIVE_CURTAIN_QA_ORDER_ID).equals(order.get("id"))) {
			return false;
		}
		URI store = URI.create((String) defaults.get_Value("url"));
		return "https".equalsIgnoreCase(store.getScheme()) && LIVE_WOO_HOST.equalsIgnoreCase(store.getHost());
	}

	private static void validateLiveCurtainRehearsal(Map<?, ?> order, List<?> items) {
		if (!"on-hold".equals(order.get("status")) || !"bacs".equals(order.get("payment_method"))
				|| order.get("date_paid") != null || !isBlankPaymentTransaction(order.get("transaction_id"))
				|| items.size() != 1 || !(items.get(0) instanceof Map<?, ?> line)
				|| !Integer.valueOf(CURTAIN_WOO_PRODUCT_ID).equals(line.get("product_id"))
				|| !Integer.valueOf(1).equals(line.get("quantity"))
				|| !(line.get("meta_data") instanceof List<?> metadata)
				|| metadata.stream().noneMatch(entry -> entry instanceof Map<?, ?> meta
						&& "Room / Location".equals(meta.get("key"))
						&& LIVE_CURTAIN_QA_LOCATION.equals(meta.get("value")))) {
			throw new AdempiereUserError("The live QA order must be the unpaid, marked curtain-only bank transfer order.");
		}
	}

	private static boolean isBlankPaymentTransaction(Object transactionId) {
		return transactionId == null || "".equals(transactionId);
	}

	private static boolean isMarkedCurtainRehearsal(Map<?, ?> order) {
		return "bm-curtain-checkout-qa".equals(order.get("created_via"))
				|| hasCurtainRehearsalMarker(order);
	}

	private static boolean hasCurtainRehearsalMarker(Map<?, ?> order) {
		return order.get("meta_data") instanceof List<?> metadata && metadata.stream()
				.anyMatch(entry -> entry instanceof Map<?, ?> meta
						&& "_bm_curtain_qa_no_fulfilment".equals(meta.get("key"))
						&& "yes".equals(meta.get("value")));
	}

	private void validateCurtainRehearsal(Map<?, ?> order, List<?> items, String storeUrl) {
		URI store = URI.create(storeUrl);
		if (!"https".equalsIgnoreCase(store.getScheme()) || !STAGING_WOO_HOST.equalsIgnoreCase(store.getHost())
				|| !"on-hold".equals(order.get("status")) || !hasCurtainRehearsalMarker(order)
				|| !"bm-curtain-checkout-qa".equals(order.get("created_via")) || order.get("date_paid") != null
				|| !"bm_curtain_qa".equals(order.get("payment_method"))) {
			throw new AdempiereUserError("Curtain QA import requires an unpaid, marked staging rehearsal that is On hold.");
		}
		if (!items.stream().allMatch(item -> item instanceof Map<?, ?> line
				&& Integer.valueOf(CURTAIN_WOO_PRODUCT_ID).equals(line.get("product_id")))) {
			throw new AdempiereUserError("The staging QA order must contain only Custom Made Curtains.");
		}
	}

	@Override
	protected void prepare() {
		wooCommerceOrderId = null;
		for (ProcessInfoParameter parameter : getParameter()) {
			if (ORDER_ID_PARAMETER.equals(parameter.getParameterName()) && parameter.getParameter() != null) {
				int id = parameter.getParameterAsInt();
				if (id <= 0) {
					throw new AdempiereUserError("Enter a positive WooCommerce Order ID or leave the parameter blank.");
				}
				wooCommerceOrderId = id;
			}
		}
	}

	@Override
	protected String doIt() throws Exception {
		return syncOrders();
	}

}
