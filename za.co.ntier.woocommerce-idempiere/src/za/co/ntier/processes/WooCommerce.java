package za.co.ntier.processes;

import org.compiere.model.MOrder;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.Env;

import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 *
 * Start a thread to collect unsynchronised orders from WooCommerce website
 *
 * @author yogan naidoo, Phil Barnett.
 */

public class WooCommerce extends SvrProcess {

	private static final String ORDER_ID_PARAMETER = "WooCommerceOrderID";
	private static final String PROCESSING_STATUS = "processing";
	private static final String STAGING_WOO_HOST = "staging-online.blindmotion.com.au";
	private static final int CURTAIN_WOO_PRODUCT_ID = 1298;
	private Integer wooCommerceOrderId;

	public class MyRunnable implements Runnable {

		@Override
		public void run() {

			// Get WooCommerce defaults
			final PO wcDefaults;
			String whereClause = " isactive = 'Y' AND AD_Client_ID = ?";
			wcDefaults = new Query(getCtx(), X_zz_woocommerce.Table_Name, whereClause, null)
					.setParameters(new Object[] { Env.getAD_Client_ID(getCtx()) }).firstOnly();
			if (wcDefaults == null)
				throw new IllegalStateException("/nWooCommerce Defaults need to be set on iDempiere /n");

			// Setup client
			OAuthConfig config = new OAuthConfig((String) wcDefaults.get_Value("url"),
					(String) wcDefaults.get_Value("consumerkey"), (String) wcDefaults.get_Value("consumersecret"));
			Woocommerce wooCommerce = new WooCommerceAPI(config, ApiVersionType.V3);

			List<?> wcOrders = ordersToSync(wooCommerce, wcDefaults);
			// Iterate through each order
			for (int i = 0; i < wcOrders.size(); i++) 
			{
				Map<?, ?> order = (Map<?, ?>) wcOrders.get(i);
				int id = (int) order.get("id");
				boolean rehearsal = isMarkedCurtainRehearsal(order);
				if (rehearsal && wooCommerceOrderId == null) {
					continue;
				}
				log.warning("Importing WooCommerce order " + id);
				WcOrder wcOrder = new WcOrder(getCtx(), get_TrxName(), wcDefaults);
				wcOrder.createOrder(order, documentNoFor(order));

				// Iterate through each order Line
				boolean linesSuccessful = true;
				List<?> lines = (List<?>) order.get("line_items");
				for (int j = 0; j < lines.size(); j++) 
				{
					Map<?, ?> line = (Map<?, ?>) lines.get(j);
					if(!wcOrder.createOrderLine(line, order))//Will return false if errors
					{
						linesSuccessful = false;
					}
					Object name = line.get("name");
					log.warning("Name of Product = " + name.toString());
				}
				if (!linesSuccessful) {
					throw new IllegalStateException("WooCommerce order " + id
						+ " failed line mapping; iDempiere import must be rolled back.");
				}

				// Update syncedToIdempiere to 'yes'
				//PB 06062024 Will not get executed if 'wcOrder.completeOrder()' has been disabled.
				
				if (wooCommerceOrderId != null && wcOrder.getOrderLineCount() == 0) {
					throw new AdempiereUserError("WooCommerce order " + id + " produced no sales order lines.");
				}

				if (rehearsal) {
					MOrder draft = new MOrder(getCtx(), wcOrder.getcOrderID(), get_TrxName());
					draft.setDescription("STAGING CURTAIN QA - DO NOT FULFIL");
					draft.saveEx();
					addLog("Imported staging curtain QA order " + id + " as iDempiere draft " + draft.getDocumentNo()
							+ ". WooCommerce order left On hold.");
					continue;
				}

				//Do not flag orders that have no order lines as complete and synced; they will need to be synced manually.
				if(wcOrder.getOrderLineCount() > 0 && linesSuccessful)
				{	

					//Create installation record
					if(wcOrder.orderTotalOverZero() > 0)//Sample orders are 0 total and don't require installation records.
					{
						MBldMtmInstall mBldMtmInstall = new MBldMtmInstall(Env.getCtx(), 0, get_TrxName());
						mBldMtmInstall.setStatus("Acc");
						mBldMtmInstall.setC_BPartner_ID(wcOrder.getOrderBpID());
						mBldMtmInstall.setC_BPartner_Location_ID(wcOrder.getbPLocationId());
						mBldMtmInstall.setName("****ONLINE****");
						Instant instant = Instant.now();
						instant = instant.plus(10, ChronoUnit.DAYS);//TODO: add column 'days_promised' to adempiere.zz_woocommerce and get as a setting from the 'WooCommerce Defaults' window. 
						mBldMtmInstall.setdate_promised(Timestamp.from(instant));
						mBldMtmInstall.setComments(wcOrder.getCustomerNote());
						mBldMtmInstall.setis_checkmeasured(true);
						mBldMtmInstall.setcontract_received(true);
						mBldMtmInstall.saveEx();
						wcOrder.setBldMtmInstallID(mBldMtmInstall.get_ID());
					}
					wcOrder.createShippingCharge(order);
					wcOrder.createPosPayment(order);
					wcOrder.completeOrder();//PB 06062024 wcOrder.completeOrder()' has been disabled for testing, re-enabled 23/10/24
					// Mark WooCommerce complete only after all ERP order work succeeds.
					//Pass 1 -> change status to completed. When this happens WooCommerce sets "syncedToIdempiere" to "no".
					Map<String, Object> body = new HashMap<>();
					body.put("status","completed");
					Map<?, ?> response = wooCommerce.update(EndpointBaseType.ORDERS.getValue(), id, body);
					System.out.println(response.toString());
					log.warning("---------Response from WooCommerce: " + response.toString());
					
					//Pass 2 -> Update syncedToIdempiere to 'yes', must be done separately to status=completed.
					Map<String, Object> body2 = new HashMap<>();
					List<Map<String, String>> listOfMetaData = new ArrayList<Map<String, String>>();
					Map<String, String> metaData = new HashMap<>();
					metaData.put("key", "syncedToIdempiere");
					metaData.put("value", "yes");
					listOfMetaData.add(metaData);
					body2.put("meta_data", listOfMetaData);
					Map<?, ?> response2 = wooCommerce.update(EndpointBaseType.ORDERS.getValue(), id, body2);
					System.out.println(response2.toString());
					log.warning("---------Response2 from WooCommerce: " + response2.toString());
					
				}	
			}
		}
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
		if (isMarkedCurtainRehearsal(order)) {
			validateCurtainRehearsal(order, items, (String) defaults.get_Value("url"));
		} else if (!PROCESSING_STATUS.equals(order.get("status"))) {
			throw new AdempiereUserError("WooCommerce order " + wooCommerceOrderId + " must be in Processing status.");
		}
		String documentNo = documentNoFor(order);
		boolean existing = new Query(getCtx(), MOrder.Table_Name,
				"AD_Client_ID=? AND AD_Org_ID=? AND IsSOTrx='Y' AND DocumentNo=?", get_TrxName())
				.setParameters(Env.getAD_Client_ID(getCtx()), defaults.getAD_Org_ID(), documentNo)
				.match();
		if (existing) {
			throw new AdempiereUserError("Sales order " + documentNo + " already exists in iDempiere.");
		}
		return List.of(order);
	}

	private static String documentNoFor(Map<?, ?> order) {
		String orderId = order.get("id").toString();
		return isMarkedCurtainRehearsal(order) ? "QA-CURTAIN-" + orderId : orderId;
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
		// Keep the import inside the process lifecycle so its transaction is
		// available until all line mappings have completed or an error propagates.
		new MyRunnable().run();
		return wooCommerceOrderId == null ? "Synchronisation from WooCommerce completed"
				: "WooCommerce order " + wooCommerceOrderId + " imported";
	}

}
