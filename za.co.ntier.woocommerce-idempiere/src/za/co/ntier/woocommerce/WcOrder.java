package za.co.ntier.woocommerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MUser;
import org.compiere.model.PO;
import org.compiere.model.X_C_OrderLine;
import org.compiere.model.X_C_POSPayment;
import org.compiere.model.X_C_Payment;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

import za.co.ntier.model.MzzWoocommerce;
import za.co.ntier.model.X_ZZ_Woocommerce_Match;

import org.compiere.process.DocAction;

/**
 *
 * Create Order and lines on iDempiere as received from WooCommerce
 *
 * @author yogan naidoo
 */

public final class WcOrder {
	private final Properties ctx;
	private final String trxName;
	private final int POSTENDERTYPE_ID = 1000000;
	private final int POS_ORDER = 135;
	

	// private final int priceList_ID = 101;
	final String PAYMENT_RULE = "M";
	// final String PAYMENT_RULE = "P";
	private final MOrder order;
	private Boolean isTaxInclusive;
	private static CLogger log = CLogger.getCLogger(WcOrder.class);
	private PO wcDefaults;

	public WcOrder(Properties ctx, String trxName, PO wcDefaults) {
		this.ctx = ctx;
		this.trxName = trxName;
		this.wcDefaults = wcDefaults;
		order = new MOrder(ctx, 0, trxName);
	}

	public void createOrder(Map<?, ?> orderWc) {
		order.setDocumentNo((orderWc.get("id").toString()));
		order.setAD_Org_ID((int) wcDefaults.get_Value("ad_org_id"));
		int BP_Id = getBPId(getWcCustomerEmail(orderWc), orderWc);
		order.setC_BPartner_ID(BP_Id);
		int BPLocationId = getBPLocationId(BP_Id);
		order.setC_BPartner_Location_ID(BPLocationId); // order.setAD_User_ID(101);
		order.setBill_BPartner_ID(BP_Id);
		order.setBill_Location_ID(BPLocationId);
		// order.setBill_User_ID(); order.setSalesRep_ID(101);
		isTaxInclusive = (orderWc.get("prices_include_tax").toString().equals("true")) ? true : false;
		order.setM_PriceList_ID(getPriceList(orderWc));
		order.setIsSOTrx(true);
		order.setM_Warehouse_ID((int) wcDefaults.get_Value("m_warehouse_id"));
		order.setC_DocTypeTarget_ID(POS_ORDER);
		order.setPaymentRule(PAYMENT_RULE);
		order.setDeliveryRule("F");
		order.setInvoiceRule("D");

		if (!order.save()) {
			throw new IllegalStateException("Could not create order");
		}
	}

	public String getWcCustomerEmail(Map<?, ?> orderWc) {
		Map<?, ?> billing = (Map<?, ?>) orderWc.get("billing");
		return (String) billing.get("email");
	}

	private int getPriceList(Map<?, ?> orderWc) {
		String wcCurrency = (String) orderWc.get("currency");
		String localCurrency = DB.getSQLValueString(trxName,
				"select iso_code from C_Currency " + "where C_Currency_ID = " + "(select C_Currency_ID "
						+ "from M_PriceList " + "where M_PriceList_id = ?) ",
				(int) wcDefaults.get_Value("local_incl_pricelist_id"));

		Boolean local = (wcCurrency.equals(localCurrency)) ? true : false;

		int priceList;
		if (local) {
			priceList = (isTaxInclusive) ? (int) wcDefaults.get_Value("local_incl_pricelist_id")
					: (int) wcDefaults.get_Value("local_excl_pricelist_id");
		} else {
			priceList = (isTaxInclusive) ? (int) wcDefaults.get_Value("intl_incl_pricelist_id")
					: (int) wcDefaults.get_Value("intl_excl_pricelist_id");
		}
		return (priceList);
	}

	public int getBPId(String email, Map<?, ?> orderWc) {
		int c_bpartner_id = DB.getSQLValue(trxName, "select c_bpartner_id from ad_user " + "where email like ?", email);
		if (c_bpartner_id < 0) {
			log.severe("BP with email : " + email + " does not exist on iDempiere");
			c_bpartner_id = createBP(orderWc);
		}
		return c_bpartner_id;
	}

	int createBP(Map<?, ?> orderWc) {
		Map<?, ?> billing = (Map<?, ?>) orderWc.get("billing");
		String name = (String) billing.get("first_name");
		String name2 = (String) billing.get("last_name");
		String phone = (String) billing.get("phone");
		String email = getWcCustomerEmail(orderWc);
		MBPartner businessPartner = new MBPartner(ctx, -1, trxName);
		businessPartner.setAD_Org_ID(0);
		businessPartner.setName(name);
		businessPartner.setName2(name2);
		businessPartner.setIsCustomer(true);
		businessPartner.setIsProspect(false);
		businessPartner.setIsVendor(false);
		businessPartner.saveEx();
		int C_Location_ID = createLocation(orderWc);
		int C_BPartner_Location_ID = createBPLocation(businessPartner.getC_BPartner_ID(), C_Location_ID);
		createUser(businessPartner, email, phone, C_BPartner_Location_ID);

		return businessPartner.get_ID();

	}

	private void createUser(MBPartner businessPartner, String email, String phone, int C_BPartner_Location_ID) {
		MUser user = new MUser(ctx, 0, trxName);
		user.setAD_Org_ID(0);
		user.setC_BPartner_ID(businessPartner.getC_BPartner_ID());
		user.setC_BPartner_Location_ID(C_BPartner_Location_ID);
		user.setName(businessPartner.getName());
		user.setEMail(email);
		user.setPhone(phone);
		user.saveEx();
	}

	private int createLocation(Map<?, ?> orderWc) {
		Map<?, ?> billing = (Map<?, ?>) orderWc.get("billing");
		String countryCode = (String) billing.get("country");
		int c_country_id;
		if (isBlankOrNull(countryCode))
			c_country_id = (int) wcDefaults.get_Value("c_country_id");
		else
			c_country_id = DB.getSQLValue(trxName, "select c_country_id " + "from c_country " + "where countrycode = ?",
					countryCode);
		String address1 = (String) billing.get("address_1");
		if (isBlankOrNull(address1))
			address1 = (String) wcDefaults.get_Value("c_country_id");
		String address2 = (String) billing.get("address_2");
		String city = (String) billing.get("city");
		if (isBlankOrNull(city))
			city = (String) wcDefaults.get_Value("city");
		String postal = (String) billing.get("postcode");
		MLocation location = new MLocation(ctx, c_country_id, 0, city, trxName);
		location.setAD_Org_ID(0);
		location.setAddress1(address1);
		location.setAddress2(address2);
		location.setPostal(postal);
		location.saveEx();
		return location.get_ID();
	}

	private int createBPLocation(int C_BPartner_ID, int C_Location_ID) {
		MBPartnerLocation BPartnerLocation = new MBPartnerLocation(ctx, 0, trxName);
		BPartnerLocation.setAD_Org_ID(0);
		BPartnerLocation.setC_BPartner_ID(C_BPartner_ID);
		BPartnerLocation.setC_Location_ID(C_Location_ID);
		BPartnerLocation.setIsBillTo(true);
		BPartnerLocation.setIsShipTo(true);
		BPartnerLocation.saveEx();
		return BPartnerLocation.getC_BPartner_Location_ID();
	}

	public int getBPLocationId(int bp_Id) {
		int c_bpartner_location_id = DB.getSQLValue(trxName,
				"select c_bpartner_location_id " + "from C_BPartner_Location " + "where c_bpartner_id = ?", bp_Id);
		if (c_bpartner_location_id < 0) {
			log.severe("BP with id : " + bp_Id + " does not have a C_BPartner_Location on iDempiere");
			int c_bpartner_id = (int) wcDefaults.get_Value("c_bpartner_id");
			c_bpartner_location_id = DB.getSQLValue(trxName,
					"select c_bpartner_location_id " + "from C_BPartner_Location " + "where c_bpartner_id = ?",
					c_bpartner_id);
		}
		return c_bpartner_location_id;
	}

	//Consider not running this method - at least until the system is stable.
	public void completeOrder() {
		order.setDateOrdered(new Timestamp(System.currentTimeMillis()));
		order.setDateAcct(new Timestamp(System.currentTimeMillis()));
		order.setDocAction(DocAction.ACTION_Complete);
		if (order.processIt(DocAction.ACTION_Complete)) {
			if (log.isLoggable(Level.FINE))
				log.fine("Order: " + order.getDocumentNo() + " completed fine");
		} else
			if (log.isLoggable(Level.FINE))
				log.fine("Order: " + order.getDocumentNo() + " did not complete");
			
			//throw new IllegalStateException("Order: " + order.getDocumentNo() + " Did not complete");

		order.saveEx();
	}

	public void createOrderLine(Map<?, ?> line, Map<?, ?> orderWc) {
		MOrderLine orderLine = new MOrderLine(order);
		orderLine.setAD_Org_ID(order.getAD_Org_ID());
	
		orderLine.setM_Product_ID(getProductId(((Integer) line.get("product_id")).intValue()));
		// orderLine.setC_UOM_ID(originalOLine.getC_UOM_ID());
		// orderLine.setC_Tax_ID(originalOLine.getC_Tax_ID());
		orderLine.setM_Warehouse_ID(order.getM_Warehouse_ID());
		orderLine.setC_Tax_ID(getTaxRate(orderWc));
		// orderLine.setC_Currency_ID(originalOLine.getC_Currency_ID());
		long qty = ((Number) line.get("quantity")).longValue();
		orderLine.setQty(BigDecimal.valueOf((long) qty));
		// orderLine.setC_Project_ID(originalOLine.getC_Project_ID());
		// orderLine.setC_Activity_ID(originalOLine.getC_Activity_ID());
		// orderLine.setC_Campaign_ID(originalOLine.getC_Campaign_ID());
		// String total = (String) line.get("total");
		// orderLine.setPrice(new BigDecimal(total));
		orderLine.setPrice(calcOrderLineUnitPrice(line));
		System.out.println("*********************Unit Price: " + orderLine.getPriceActual());
		
		//Added by Phil Barnett 27/5/2023
				ArrayList<LinkedHashMap<String,Object>> metaData = (ArrayList<LinkedHashMap<String, Object>>) line.get("meta_data");
				for (LinkedHashMap<String, Object> metaItem : metaData)
				{
					 if(metaItem.get("key").equals("_wapf_meta"))
						 /*_wapf_meta contains the unique id of each field that
						  * can be matched to the backend product  */
					 {
						 LinkedHashMap<String, Object> wapfMeta = (LinkedHashMap<String, Object>) metaItem.get("value");
						 for(Entry<String, Object> wapfMetaItem : wapfMeta.entrySet())
						 {
							 System.out.println(wapfMetaItem.getValue());
							 //Get a LinkedHashMap of all the fields and their IDs.
							 LinkedHashMap<String, Object> fields = (LinkedHashMap<String, Object>) wapfMetaItem.getValue();
							 for(Map.Entry<String, Object> fieldItem : fields.entrySet())
							 {
								 //This loop to call method to do the heavy lifting.
								 //System.out.println(fieldItem.getValue());
								try 
								{
									 LinkedHashMap<String, Object> field = (LinkedHashMap<String, Object>) fieldItem.getValue();
									 System.out.println(field.get("id"));
									 System.out.println(field.get("label"));
									 System.out.println(field.get("value"));
								}
								catch(java.lang.ClassCastException e)
								{
									System.out.println("Exception thrown.");
								}
							 }
							 
							 
							 //System.out.println(values.toString());
						 }
					 }
				}
				//Added by Phil

		if (!orderLine.save()) {
			throw new IllegalStateException("Could not create Order Line");
		}
	}

	public int getProductId(String name) {
		int m_Product_ID = DB.getSQLValue(trxName, "select m_product_id " + "from m_product mp " + "where name like ?",
				name);
		if (m_Product_ID < 0) {
			log.severe("Product : " + name + " does not exist on iDempiere");
			m_Product_ID = (int) wcDefaults.get_Value("m_product_id");
		}
		return m_Product_ID;
	}
	
	/**
	 * Uses the WooCommerce ProductID to match to a product in Idempiere.
	 * Overrides getProductId(String name) which relies on the name string of the WooCommerce product.
	 * @param woocommID
	 * @return
	 */
	public int getProductId(int woocommID) {
		int orgID = order.getAD_Org_ID();
		MzzWoocommerce mzzWoocommerce = MzzWoocommerce.get(orgID, ctx, trxName);
		PO[] xZZWoocommerceMatches = mzzWoocommerce.getLines("", "");
		for(int x = 0; x < xZZWoocommerceMatches.length; x++)
		{
			X_ZZ_Woocommerce_Match xZZWoocommerceMatch = new X_ZZ_Woocommerce_Match(ctx, xZZWoocommerceMatches[x].get_ID(), trxName);
			if(xZZWoocommerceMatch.getwoocommerce_key() == woocommID)
			{
				return xZZWoocommerceMatch.getM_Product_ID();
				
			}
		}
		throw new AdempiereUserError("Unable to Match WooComm product to Idempiere product. Make sure the product mapping exists and is mapped to a real product.");
	}
	

	public void createShippingCharge(Map<?, ?> orderWc) {
		MOrderLine orderLine = new MOrderLine(order);
		orderLine.setAD_Org_ID(order.getAD_Org_ID());
		orderLine.setC_Charge_ID((int) wcDefaults.get_Value("c_charge_id"));
		// orderLine.setC_UOM_ID(originalOLine.getC_UOM_ID());
		orderLine.setM_Warehouse_ID(order.getM_Warehouse_ID());
		orderLine.setC_Tax_ID(getTaxRate(orderWc));
		// orderLine.setC_Currency_ID(originalOLine.getC_Currency_ID());
		orderLine.setQty(BigDecimal.ONE);
		orderLine.setPrice(getShippingCost(orderWc));
		System.out.println("*********************Shipping Cost: " + orderLine.getPriceActual());

		if (!orderLine.save()) {
			throw new IllegalStateException("Could not create Order Line");
		}
	}

	public int getTaxRate(Map<?, ?> orderWc) {
		List<?> taxLines = (List<?>) orderWc.get("tax_lines");
		String taxRate = "" ;
		if(taxLines.size() > 1)
		{
			Map<?, ?> taxLine = (Map<?, ?>) taxLines.get(0);
			taxRate = (String) taxLine.get("label");
		}
		
		return (taxRate.equals("Standard") ? (int) wcDefaults.get_Value("standard_tax_id")
				: (int) wcDefaults.get_Value("zero_tax_id"));
	}

	public BigDecimal getShippingCost(Map<?, ?> orderWc) {
		List<?> shippingLines = (List<?>) orderWc.get("shipping_lines");
		Map<?, ?> shippingLine = (Map<?, ?>) shippingLines.get(0);
		Double total = Double.parseDouble((String) shippingLine.get("total"));
		Double totalTax = Double.parseDouble((String) shippingLine.get("total_tax"));
		BigDecimal shippingCost = isTaxInclusive ? BigDecimal.valueOf((Double) total + totalTax)
				: BigDecimal.valueOf((Double) total);
		return (shippingCost.setScale(4, RoundingMode.HALF_EVEN));
	}

	public BigDecimal calcOrderLineUnitPrice(Map<?, ?> line) {
		// Double price = (Double) line.get("price");
		Double price = ((Number) line.get("price")).doubleValue();
		BigDecimal unitPrice = new BigDecimal(price);
		if (isTaxInclusive) {
			List<?> taxList = (List<?>) line.get("taxes");
			Map<?, ?> taxes;
			if(taxList.size() > 1)
				{
					taxes = (Map<?, ?>) taxList.get(0);
					// long totalTax = ((Number) taxes.get("total")).longValue();
					Double totalTax = Double.parseDouble((String) taxes.get("total"));
					Double qty = ((Number) line.get("quantity")).doubleValue();
					Double unitTax = totalTax / qty;
					unitPrice = unitPrice.add(BigDecimal.valueOf((Double) unitTax));
				}
			
			
		}
		return (unitPrice = unitPrice.setScale(4, RoundingMode.HALF_EVEN));

	}

	public void createPosPayment(Map<?, ?> orderWc) {
		X_C_POSPayment posPayment = new X_C_POSPayment(ctx, null, trxName);
		posPayment.setC_Order_ID(order.getC_Order_ID());
		posPayment.setAD_Org_ID(order.getAD_Org_ID());
		posPayment.setPayAmt(new BigDecimal(orderWc.get("total").toString()));
		posPayment.setC_POSTenderType_ID(POSTENDERTYPE_ID); // credit card
		posPayment.setTenderType(X_C_Payment.TENDERTYPE_CreditCard); // credit card
		if (!posPayment.save())
			throw new IllegalStateException("Could not create POSPayment");
	}

	public static boolean isBlankOrNull(String str) {
		return (str == null || "".equals(str.trim()));
	}
	
	

}