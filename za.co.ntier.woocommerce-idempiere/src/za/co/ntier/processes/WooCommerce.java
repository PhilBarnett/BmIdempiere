package za.co.ntier.processes;

import org.compiere.model.MOrder;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.Env;
import org.compiere.util.DB;
import org.compiere.util.Trx;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import com.icoderman.woocommerce.ApiVersionType;
import com.icoderman.woocommerce.EndpointBaseType;
import com.icoderman.woocommerce.Woocommerce;
import com.icoderman.woocommerce.WooCommerceAPI;
import com.icoderman.woocommerce.oauth.OAuthConfig;

import au.blindmot.model.MBldMtmInstall;
import za.co.ntier.model.X_zz_woocommerce;
import za.co.ntier.woocommerce.WcOrder;

/**
 *
 * Import WooCommerce orders into independent ERP transactions.
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


	private static final String RECEIPTS = "adempiere.bm_woo_import_receipt";
	private static final String STORE_ORDER = "zz_woocommerce_id=? AND woo_order_id=?";
	private static final String ENDPOINT = EndpointBaseType.ORDERS.getValue();

	private String runSync() throws SQLException {
		PO defaults = new Query(getCtx(), X_zz_woocommerce.Table_Name, "IsActive='Y' AND AD_Client_ID=?", null)
				.setParameters(Env.getAD_Client_ID(getCtx())).firstOnly();
		if (defaults == null) {
			throw new AdempiereUserError("WooCommerce Defaults must be configured in iDempiere.");
		}
		Woocommerce woo = new WooCommerceAPI(new OAuthConfig((String) defaults.get_Value("url"),
				(String) defaults.get_Value("consumerkey"), (String) defaults.get_Value("consumersecret")),
				ApiVersionType.V3);
		int storeId = defaults.get_ID();
		List<?> orders = ordersToSync(woo, defaults);
		// Use receipts, not Woo status, to find pending acknowledgements. A status
		// write can succeed while its response or the following metadata write fails.
		List<Integer> previous = pending(storeId);
		Set<Integer> attempted = new HashSet<>();
		int imported = 0, failed = 0, pending = 0;
		for (Object data : orders) {
			if (!(data instanceof Map<?, ?> order) || !(order.get("id") instanceof Integer id) || id <= 0) {
				addLog("Skipped a WooCommerce response row without a valid order ID.");
				failed++;
				continue;
			}
			boolean live = isLiveCurtainRehearsal(order, defaults);
			boolean sample = isStagingSampleRehearsal(order, defaults);
			boolean rehearsal = live || sample || isMarkedCurtainRehearsal(order);
			if (rehearsal && wooCommerceOrderId == null) {
				continue;
			}
			try {
				if (importOne(defaults, order, live, sample, rehearsal)) {
					imported++;
					addLog("WooCommerce order " + id + " committed in iDempiere"
							+ (rehearsal ? " as a QA draft. WooCommerce unchanged." : "."));
				}
			} catch (RuntimeException | SQLException error) {
				// An ambiguous commit can be recovered from its receipt next run;
				// never acknowledge Woo after this attempt reports failure.
				if (wooCommerceOrderId != null || databaseUnavailable(error)) {
					throw error;
				}
				failed++;
				attempted.add(id);
				report(id, "ERP import not confirmed; WooCommerce not changed.", error);
				continue;
			}
			if (!rehearsal && attempted.add(id) && !tryAcknowledge(woo, storeId, id)) {
				pending++;
			}
		}
		if (wooCommerceOrderId == null) {
			for (int id : previous) {
				if (attempted.add(id) && !tryAcknowledge(woo, storeId, id)) {
					pending++;
				}
			}
		}
		return "WooCommerce import: " + imported + " committed, " + failed
				+ " not confirmed, " + pending + " awaiting WooCommerce acknowledgement. See process log.";
	}

	private boolean importOne(PO defaults, Map<?, ?> order, boolean live, boolean sample, boolean rehearsal)
			throws SQLException {
		int id = (Integer) order.get("id");
		int storeId = defaults.get_ID();
		String documentNo = documentNoFor(order, rehearsal, sample);
		Trx trx = Trx.get(Trx.createTrxName("WooImport"), true);
		String name = trx.getTrxName();
		boolean committed = false;
		Connection connection = null;
		try {
			connection = trx.getConnection();
			// The unique claim serialises concurrent batches/targeted imports
			// for one store and order. It is never committed without its ERP link.
			if (!rehearsal && update(name, "INSERT INTO " + RECEIPTS
					+ " (zz_woocommerce_id, woo_order_id) VALUES (?,?) ON CONFLICT DO NOTHING",
					storeId, id) == 0) {
				return false;
			}
			if (rehearsal) {
				// QA rehearsals do not have a Woo acknowledgement receipt.
				try (PreparedStatement lock = DB.prepareStatement("SELECT pg_advisory_xact_lock(?,?)", name)) {
					lock.setInt(1, storeId);
					lock.setInt(2, id);
					try (ResultSet ignored = lock.executeQuery()) {
						// The lock is taken on execution and released at commit/rollback.
					}
				}
			} else if (!PROCESSING_STATUS.equals(order.get("status"))) {
				throw new AdempiereUserError("Order " + id + " is not Processing and has no ERP import receipt.");
			}
			String legacyRef = rehearsal && !live ? documentNo : Integer.toString(id);
			if (new Query(getCtx(), MOrder.Table_Name,
					"AD_Client_ID=? AND AD_Org_ID=? AND IsSOTrx='Y' AND (DocumentNo=? OR DocumentNo=? OR POReference=?)", name)
					.setParameters(Env.getAD_Client_ID(getCtx()), (int) defaults.get_Value("ad_org_id"), documentNo,
							legacyRef, legacyRef).match()) {
				throw new AdempiereUserError("Order " + id
						+ " has an existing ERP document without a matching import receipt. Reconcile manually.");
			}
			if (!(order.get("line_items") instanceof List<?> lines) || lines.isEmpty()) {
				throw new AdempiereUserError("Order " + id + " has no WooCommerce items.");
			}
			WcOrder wcOrder = new WcOrder(getCtx(), name, defaults);
			wcOrder.createOrder(order, documentNo);
			for (Object item : lines) {
				if (!(item instanceof Map<?, ?> line) || !wcOrder.createOrderLine(line, order)) {
					throw new AdempiereUserError("Order " + id + " failed line mapping; no order was imported.");
				}
			}
			int lineCount = wcOrder.getOrderLineCount();
			if (lineCount == 0) {
				throw new AdempiereUserError("Order " + id + " produced no sales order lines.");
			}
			if (rehearsal) {
				MOrder draft = new MOrder(getCtx(), wcOrder.getcOrderID(), name);
				if (!DocAction.STATUS_Drafted.equals(draft.getDocStatus()) || ((live || sample) && lineCount != 2)) {
					throw new AdempiereUserError("The QA order did not produce the expected draft order lines.");
				}
				draft.setDescription(sample ? "STAGING FABRIC SAMPLE QA - DO NOT FULFIL"
						: (live ? "LIVE" : "STAGING") + " CURTAIN QA - DO NOT FULFIL");
				draft.saveEx();
			} else {
				createInstallation(wcOrder, name);
				wcOrder.createShippingCharge(order);
				wcOrder.createPosPayment(order);
				wcOrder.completeOrder();
				if (update(name, "UPDATE " + RECEIPTS + " SET c_order_id=? WHERE " + STORE_ORDER,
						wcOrder.getcOrderID(), storeId, id) != 1) {
					throw new SQLException("Could not store ERP import receipt for WooCommerce order " + id);
				}
			}
			if (!trx.commit(true)) {
				throw new SQLException("ERP commit was not confirmed for WooCommerce order " + id);
			}
			committed = true;
			return true;
		} finally {
			close(trx, connection, committed);
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

	private List<Integer> pending(int storeId) throws SQLException {
		List<Integer> ids = new ArrayList<>();
		try (PreparedStatement sql = DB.prepareStatement("SELECT woo_order_id FROM " + RECEIPTS
				+ " WHERE zz_woocommerce_id=? AND c_order_id IS NOT NULL AND acknowledged_at IS NULL"
				+ " ORDER BY woo_order_id", null)) {
			sql.setInt(1, storeId);
			try (ResultSet rows = sql.executeQuery()) {
				while (rows.next()) {
					ids.add(rows.getInt(1));
				}
			}
		}
		return ids;
	}

	private boolean tryAcknowledge(Woocommerce woo, int storeId, int id) throws SQLException {
		try {
			acknowledge(woo, storeId, id);
			return true;
		} catch (RuntimeException | SQLException error) {
			if (databaseUnavailable(error)) {
				throw error;
			}
			report(id, "ERP import is committed; WooCommerce acknowledgement is pending.", error);
			return false;
		}
	}

	private void acknowledge(Woocommerce woo, int storeId, int id) throws SQLException {
		Trx trx = Trx.get(Trx.createTrxName("WooAck"), true);
		String name = trx.getTrxName();
		boolean committed = false;
		Connection connection = null;
		try {
			connection = trx.getConnection();
			try (PreparedStatement sql = DB.prepareStatement("SELECT acknowledged_at FROM " + RECEIPTS + " WHERE "
					+ STORE_ORDER + " AND c_order_id IS NOT NULL FOR UPDATE", name)) {
				sql.setInt(1, storeId);
				sql.setInt(2, id);
				try (ResultSet row = sql.executeQuery()) {
					if (!row.next()) {
						throw new SQLException("No committed ERP import receipt for WooCommerce order " + id);
					}
					if (row.getTimestamp(1) != null) {
						return;
					}
				}
			}
			WooOrderAcknowledgement.apply(id, () -> woo.get(ENDPOINT, id),
					body -> woo.update(ENDPOINT, id, body));
			if (update(name, "UPDATE " + RECEIPTS + " SET acknowledged_at=CURRENT_TIMESTAMP WHERE "
					+ STORE_ORDER, storeId, id) != 1 || !trx.commit(true)) {
				throw new SQLException("Could not commit WooCommerce acknowledgement receipt.");
			}
			committed = true;
		} finally {
			close(trx, connection, committed);
		}
	}

	private static int update(String trxName, String statement, int... args) throws SQLException {
		try (PreparedStatement sql = DB.prepareStatement(statement, trxName)) {
			for (int i = 0; i < args.length; i++) {
				sql.setInt(i + 1, args[i]);
			}
			return sql.executeUpdate();
		}
	}

	private static boolean databaseUnavailable(Exception error) {
		for (Throwable cause = error; cause != null; cause = cause.getCause()) {
			if (cause instanceof UnsafeRollback) {
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

	private static void close(Trx trx, Connection connection, boolean committed) throws UnsafeRollback {
		if (!committed && connection != null) {
			try {
				if (!trx.rollback(true)) {
					throw new SQLException("ERP rollback was not confirmed.");
				}
			} catch (RuntimeException | SQLException error) {
				// Trx.close() resets auto-commit. Do not let this commit an
				// unconfirmed partial order: first kill the connection.
				try {
					connection.abort(Runnable::run);
				} catch (RuntimeException | SQLException abortFailure) {
					error.addSuppressed(abortFailure);
					throw new UnsafeRollback(error);
				}
				trx.close();
				throw new UnsafeRollback(error);
			}
		}
		trx.close();
	}

	private static final class UnsafeRollback extends SQLException {
		private static final long serialVersionUID = 1L;

		UnsafeRollback(Exception cause) {
			super("Unsafe ERP rollback: stop the importer and inspect the database.", cause);
		}
	}

	private void report(int id, String summary, Exception error) {
		String message = "WooCommerce order " + id + ": " + summary;
		log.log(Level.WARNING, message, error);
		// Keep API responses and customer details out of the process result.
		addLog(message + " See server log for details.");
	}
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
		} else if (!PROCESSING_STATUS.equals(order.get("status"))
				&& !"completed".equals(order.get("status"))) {
			throw new AdempiereUserError("WooCommerce order " + wooCommerceOrderId
					+ " must be Processing, or Completed with an existing ERP import receipt.");
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
		return runSync();
	}
}
