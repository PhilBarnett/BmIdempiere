package za.co.ntier.processes;

import org.compiere.model.MOrderLine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;

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

			// Get all with request parameters
			Map<String, String> params = new HashMap<>();
			params.put("per_page", "100");
			params.put("offset", "0");
			//params.put("meta_key", "syncedToIdempiere");
			//params.put("meta_value", "no");
			params.put("status", "processing");//See comment below
			//params.put("status", "completed");//See comment below
			
			/*Current sync behaviour:
			 * Orders with status 'processing' are retrieved from WooComm.
			 * Once orders are processed, orders with orderlines have their status set to 'completed' and 
			 * 'syncedToIdempiere' flag set to yes.
			 * Orders that have no lines will almost certainly throw errors so they can be attended to.
			 * Currently there is no use of the 'syncedToIdempiere' flag
			 */

			List<?> wcOrders = wooCommerce.getAll(EndpointBaseType.ORDERS.getValue(), params);
			// Iterate through each order
			for (int i = 0; i < wcOrders.size(); i++) 
			{
				Map<?, ?> order = (Map<?, ?>) wcOrders.get(i);
				int id = (int) order.get("id");
				log.warning("Order- " + order.get("id") + ": " + order);
				WcOrder wcOrder = new WcOrder(getCtx(), get_TrxName(), wcDefaults);
				wcOrder.createOrder(order);

				// Iterate through each order Line
				List<?> lines = (List<?>) order.get("line_items");
				for (int j = 0; j < lines.size(); j++) 
				{
					Map<?, ?> line = (Map<?, ?>) lines.get(j);
					wcOrder.createOrderLine(line, order);
					Object name = line.get("name");
					log.warning("Name of Product = " + name.toString());
				}
				wcOrder.createShippingCharge(order);
				wcOrder.createPosPayment(order);
				wcOrder.completeOrder();//PB 06062024 wcOrder.completeOrder()' has been disabled for testing, re-enabled 23/10/24

				// Update syncedToIdempiere to 'yes'
				//PB 06062024 Will not get executed if 'wcOrder.completeOrder()' has been disabled.
				
				//Do not flag orders that have no order lines as complete and synced; they will need to be synced manually.
				if(wcOrder.getOrderLineCount() > 0)
				{	
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
					
					//Create installation record
					if(wcOrder.getOrderTotal() > 0)//Sample orders are 0 total and don't require installation records.
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
				}	
			}
		}
	}

	@Override
	protected void prepare() {
	}

	@Override
	protected String doIt() throws Exception {
		Thread thread = new Thread(new MyRunnable());
		thread.start();

		return "Synchronisation from WooCommerce initiated";
	}

}
