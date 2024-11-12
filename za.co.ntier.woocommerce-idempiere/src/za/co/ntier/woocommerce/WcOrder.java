package za.co.ntier.woocommerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_M_AttributeInstance;
import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MDocType;
import org.compiere.model.MLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MUser;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_C_POSPayment;
import org.compiere.model.X_C_Payment;
import org.compiere.model.X_M_Attribute;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;

import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDLineProductSetInstance;
import au.blindmot.model.MBLDProductPartType;
import za.co.ntier.model.MzzWoocommerce;
import za.co.ntier.model.MzzWoocommerceMap;
import za.co.ntier.model.MzzWoocommerceMapLine;
import za.co.ntier.model.X_ZZ_Woocommerce_Match;
import org.compiere.process.DocAction;

/**
 *
 * Create Order and lines on iDempiere as received from WooCommerce
 *
 * @author yogan naidoo, modified for made to measure blinds by Phil Barnett.
 */

public final class WcOrder {
	private final Properties ctx;
	private final String trxName;
	private final int POSTENDERTYPE_ID = 1000000;
	private final String DOC_BASE_TYPE = "SOO";
	private final String ORDER_TYPE = "BM Order";
	//private final int POS_ORDER = 135;//Hard coded to Garden World, does not work in all clients.
	public static final String WOOCOMMERCE_MAP_TYPE_ATTRIBUTE = "10000003";
	public static final String WOOCOMMERCE_MAP_TYPE_PRODUCT_ADD = "10000004";
	public static final String WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE = "10000005";
	public static final String WOOCOMMERCE_MAP_MULTISELECT_PARENT = "10000006";
	public static final String WOOCOMMERCE_MAP_MULTISELECT_CHILD = "10000007";

	// private final int priceList_ID = 101;
	final String PAYMENT_RULE = "M";
	// final String PAYMENT_RULE = "P";
	private final MOrder order;
	private Boolean isTaxInclusive;
	//private ArrayList<LinkedHashMap> duplicateFields = null;
	private static CLogger log = CLogger.getCLogger(WcOrder.class);
	private PO wcDefaults;
	private ArrayList<ArrayList<MzzWoocommerceMapLine>> sortArrayAttributes;
	private ArrayList<ArrayList<MzzWoocommerceMapLine>> sortArrayProducts;
	private ArrayList<MzzWoocommerceMap> sortArrayNonDuplicateAttributes;
	private ArrayList<MzzWoocommerceMap> sortArrayNonDuplicateProductAttributes;
	private LinkedHashMap<Object, Object> fieldValues;
	private String wooCommProductName ="";
	private Integer wooCommProductID;
	private StringBuilder mapNotFound;
	private int orderBpID = 0;
	private int cOrderID = 0;
	private int bPLocationId = 0;
	private String customerNote = "";

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
		orderBpID = BP_Id;
		int BPLocationId = getBPLocationId(BP_Id);
		bPLocationId = BPLocationId;
		order.setC_BPartner_Location_ID(BPLocationId); // order.setAD_User_ID(101);
		order.setBill_BPartner_ID(BP_Id);
		order.setBill_Location_ID(BPLocationId);
		// order.setBill_User_ID(); order.setSalesRep_ID(101);
		/*isTaxInclusive = (orderWc.get("prices_include_tax").toString().equals("true")) ? true : false;//TODO:Problem: prices don't actually include tax.*/
		isTaxInclusive = false;//Hard coded 2/2/24 because WooComm returns true when the price does not include tax.
		order.setM_PriceList_ID(getPriceList(orderWc));
		order.setIsSOTrx(true);
		order.setM_Warehouse_ID((int) wcDefaults.get_Value("m_warehouse_id"));
		int cDocTypeID = 0;
		MDocType[] mDocTypes = MDocType.getOfDocBaseType(ctx, DOC_BASE_TYPE);
		for(int g = 0; g < mDocTypes.length; g++)
		{
			if(mDocTypes[g].getName().equalsIgnoreCase(ORDER_TYPE))
			{
				cDocTypeID = mDocTypes[g].get_ID();
				break;
			}
		}
		order.setC_DocTypeTarget_ID(cDocTypeID);
		order.setPaymentRule(PAYMENT_RULE);
		order.setDeliveryRule("F");
		order.setInvoiceRule("D");
		order.set_ValueOfColumn("install_bpartner_id", BP_Id);

		if (!order.save()) {
			int orgID = order.getAD_Org_ID();
			MzzWoocommerce mzzWoocommerce = MzzWoocommerce.get(orgID, ctx, trxName);
			String email = mzzWoocommerce.getnotify_email();
			StringBuilder msg = new StringBuilder("WooCommerce sync encountered a problem. Order: ");
			msg.append(order.getDocumentNo());
			msg.append(" had an Order save error. ");
			msg.append("Check logs for the error. ");
			msg.append("The error occured in method: createOrder(Map<?, ?> orderWc) ");
			msg.append(". The WooCommerce sync process may need to be run manually.");
			WcMailNotify.sendEmail(email, msg.toString(), "", ctx, trxName);//Email any issues found.
			log.warning(msg.toString());
			throw new IllegalStateException("Could not create order");
		}
		cOrderID = order.get_ID();
		this.customerNote = (String) orderWc.get("customer_note");
		String orderDescription = order.getDescription();
		if(orderDescription.equalsIgnoreCase("null"))
		{
			orderDescription = "";
		}
		else
		{
			orderDescription = orderDescription + " .";
		}
		order.setDescription(orderDescription + "Customer Note:" + customerNote);
		
	}
	
	public int getOrderLineCount() {
		return order.getLines().length;
	}
	
	public int orderTotalOverZero() {
		MOrder copyOrder = new MOrder(Env.getCtx(), order.get_ID(), null);//Created copy as this.order was returning no total;
		//if(order.getTotalLines().compareTo(Env.ZERO) > 0) 
		if(copyOrder.getGrandTotal().compareTo(Env.ZERO) > 0) 
		{
			return 1;//order.getTotalLines().;
		}
		return 0;
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
		String name = (String) billing.get("first_name") + " " + billing.get("last_name");
		//String name2 = (String) billing.get("last_name");
		String phone = (String) billing.get("phone");
		String email = getWcCustomerEmail(orderWc);
		MBPartner businessPartner = new MBPartner(ctx, -1, trxName);
		businessPartner.setAD_Org_ID(0);
		businessPartner.setName(name);
		//businessPartner.setName2(name2);
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
		//throw new IllegalStateException("Order: " + order.getDocumentNo() + " Did not complete");//Comment out to run method as below
		order.setDateOrdered(new Timestamp(System.currentTimeMillis()));
		order.setDateAcct(new Timestamp(System.currentTimeMillis()));
		order.setDocAction(DocAction.ACTION_None );//23/10/24 was ACTION_Complete
		if (order.processIt(DocAction.ACTION_None)) {//23/10/24 was ACTION_Complete
			if (log.isLoggable(Level.FINE))
				log.fine("Order: " + order.getDocumentNo() + " completed fine");
		} else {
			if (log.isLoggable(Level.FINE))
				log.fine("Order: " + order.getDocumentNo() + " did not complete");
		order.saveEx();//Comment out with line below to bypass is completed check.
			throw new IllegalStateException("Order: " + order.getDocumentNo() + " Did not complete");
		}
		//order.saveEx(); //Uncomment if 'throw new IllegalStateException("Order: " + order.getDocumentNo() + " Did not complete");' is commented out
	}

	public boolean createOrderLine(Map<?, ?> line, Map<?, ?> orderWc) {
		MOrderLine orderLine = new MOrderLine(order);
		orderLine.setAD_Org_ID(order.getAD_Org_ID());
	
		orderLine.setM_Product_ID(getProductId(((Integer) line.get("product_id")).intValue()));
		wooCommProductName = getWooCommerceProductName(((Integer) line.get("product_id")).intValue());
		wooCommProductID = (Integer) line.get("product_id");
		if(orderLine.getDescription() != null)
		{
			orderLine.setDescription(getWooCommerceProductName((Integer)line.get("product_id")) + " " + orderLine.getDescription());
		}
		else
		{
			orderLine.setDescription(getWooCommerceProductName((Integer)line.get("product_id")));
		}
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
		orderLine.set_ValueOfColumn("lockprice", "Y");
		System.out.println("*********************Unit Price: " + orderLine.getPriceActual());
		
		//Added by Phil Barnett 27/5/2023 -> process meta data for attributes and product options
		//duplicateFields = null;
		ArrayList<LinkedHashMap<String,Object>> metaData = (ArrayList<LinkedHashMap<String, Object>>) line.get("meta_data");
		ArrayList<MzzWoocommerceMapLine> mzzWoocommerceMapLines = new ArrayList<MzzWoocommerceMapLine>();//Holds the found Mapping instructions for this WC orderline
		ArrayList<MzzWoocommerceMap> masterZzWoocommerceMapList = createdMapListFromMetaData(metaData, orderLine.getM_Product_ID());
		//IF there's been an error with the MapList, abort.
		if(masterZzWoocommerceMapList == null)
		{
			return false;
		}
		
	/*	for (LinkedHashMap<String, Object> metaItem : metaData)
			{
				 if(metaItem.get("key").equals("_wapf_meta"))
					 /*_wapf_meta contains the unique id of each field that
					  * can be matched to the backend product  */
					 /*			 {
					 LinkedHashMap<String, Object> wapfMeta = (LinkedHashMap<String, Object>) metaItem.get("value");
					 //Create a list of mapping object for this WC order line
					 for(Entry<String, Object> wapfMetaItem : wapfMeta.entrySet())
					 {
						 System.out.println(wapfMetaItem.getValue());
						 //Get a LinkedHashMap of all the fields and their IDs.
						 
						 LinkedHashMap<String, Object> fields = (LinkedHashMap<String, Object>) wapfMetaItem.getValue();
						 for(Map.Entry<String, Object> fieldItem : fields.entrySet())
						 { 
							try 
							{
								 //System.out.println(fieldItem.getValue());
								if(fieldItem.getValue().getClass().equals(LinkedHashMap.class)) 
								{
									 LinkedHashMap<String, Object> field = (LinkedHashMap<String, Object>) fieldItem.getValue();
										
									 MzzWoocommerceMap zzWoocommerceMap = MzzWoocommerceMap.getMzzWoocommerceMap(orderLine.getM_Product_ID(),(String)field.get("id"), (String)field.get("value"), ctx);
									 masterZzWoocommerceMapList.add(zzWoocommerceMap); //Add all found mappings to List
									 
									 System.out.println(field.get("id"));
									 System.out.println(field.get("label"));
									 System.out.println(field.get("value"));
									 //break;//There's no more useful stuff.
									 //processWooCommMeta(orderLine, field, orderLine.getM_Product_ID(), ctx, trxName);
								}
								
							}
							catch(java.lang.ClassCastException e)
							{
								System.out.println("Exception thrown.");
							}
						 }
					 } 
				 }
			} */
						 
						 //Create MapLines (the actual instructions to create attributes, product options and product attributes), add to List
						 for(MzzWoocommerceMap zzWoocommerceMapItem : masterZzWoocommerceMapList)
						 {
							 MzzWoocommerceMapLine[] mzzWoocommerceMapLns = 
									 zzWoocommerceMapItem.getMzzWoocommerceMapLines(zzWoocommerceMapItem.get_ID(), ctx, "", "");
							 for(int ml = 0; ml < mzzWoocommerceMapLns.length; ml++)
							 {
								 mzzWoocommerceMapLines.add(mzzWoocommerceMapLns[ml]);
							 }
						 }
							//we now have all the MzzWoocommerceMapLines in an ArrayList.
							/*Find duplicate attributes - we want to find Attributes that
							 * are being created by more than one WooCommerce field.
							 * If we find one, we add it to a storage List
							 * For example, 2 lines that are both setting width.*/
					
						 //Create lists of duplicate maplines by maptype.
		/*Works*/		 ArrayList<MzzWoocommerceMapLine> duplicateAttributes = getMzzWooCommerceLineDuplicates(WOOCOMMERCE_MAP_TYPE_ATTRIBUTE, mzzWoocommerceMapLines);
			/*Works*/	 ArrayList<MzzWoocommerceMapLine> duplicateBldPartTypes = getMzzWooCommerceLineDuplicates(WOOCOMMERCE_MAP_TYPE_PRODUCT_ADD, mzzWoocommerceMapLines);
	/*Dosen't Work*/	 ArrayList<MzzWoocommerceMapLine> duplicateProductAttributes = getMzzWooCommerceLineDuplicates(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE, mzzWoocommerceMapLines);
							
						 /*We now have 3 lists of possible duplicate WC mapped fields.
						  * Create Lists of MzzWoocommerceMapLines to create the MOrderLines.
						  * There will NumberOfUniqueDuplicateAttributes X UniqueDuplicatePartTypes -> EG 3 widths, 2 fabrics -> 6 Orderlines*/
						 sortArrayNonDuplicateAttributes = new ArrayList<MzzWoocommerceMap>();
						 sortArrayNonDuplicateProductAttributes = new ArrayList<MzzWoocommerceMap>();
						 sortArrayAttributes = new ArrayList<ArrayList<MzzWoocommerceMapLine>>();
						 	ArrayList<MzzWoocommerceMapLine> nonDuplicateAttributes = removeDontAddToDuplicateAttributes(duplicateAttributes, sortArrayNonDuplicateAttributes);
						 	if(nonDuplicateAttributes.size()>0)
						 	{
						 		sortArrayAttributes = processRepeatingFields(sortArrayAttributes, nonDuplicateAttributes);
						 	}
						 	
							sortArrayProducts = new ArrayList<ArrayList<MzzWoocommerceMapLine>>();
							//processRepeatingFields(duplicateAttributes);
							sortArrayProducts = processRepeatingFields(sortArrayProducts, duplicateBldPartTypes);
							
							ArrayList<MzzWoocommerceMapLine> nonDuplicateProductAttributes = removeDontAddToDuplicateAttributes(duplicateProductAttributes, sortArrayNonDuplicateProductAttributes);
						 	if(duplicateProductAttributes.size() > 0)
						 	{
						 		processRepeatingFields(sortArrayProducts, nonDuplicateProductAttributes);//This reuses the sortArrayProducts field and adds to it.
						 	}
							
						 
						 //System.out.println(values.toString());
			
		MProduct lineProduct = MProduct.get(orderLine.getM_Product_ID());		
		if(lineProduct.getM_AttributeSetInstance_ID() > 0)
		{
			MAttributeSetInstance lineAttSetIns = new MAttributeSetInstance(ctx, orderLine.getM_AttributeSetInstance_ID(), trxName);	
			lineAttSetIns.setDescription();
			lineAttSetIns.save();
		}
		
		MBLDLineProductSetInstance lineBldInstance = new MBLDLineProductSetInstance(ctx, orderLine.get_ValueAsInt("bld_line_productsetinstance_id"), trxName);
		if(lineBldInstance != null)
		{
			lineBldInstance.setDescription(orderLine.getM_Product_ID());
			lineBldInstance.save();
		}
		if(sortArrayProducts.size()==0 && sortArrayAttributes.size() ==0)//no duplicate fields, single Orderline
		{
			//Process single order using masterMapList.
			ArrayList<ArrayList<MzzWoocommerceMap>> singleOrderLine = new ArrayList<ArrayList<MzzWoocommerceMap>>();
			singleOrderLine.add(masterZzWoocommerceMapList);
			processOrderLine(orderLine, singleOrderLine, trxName);
			
		}
		else//Process multi Orderlines
		{
			ArrayList<ArrayList<MzzWoocommerceMap>>  interimMzzWoocommerceMapListAttributes = new ArrayList<ArrayList<MzzWoocommerceMap>>();
			ArrayList<ArrayList<MzzWoocommerceMap>>  interimMzzWoocommerceMapListProduct = new ArrayList<ArrayList<MzzWoocommerceMap>>();
			ArrayList<ArrayList<MzzWoocommerceMap>>  mergedMzzWoocommerceMapListProduct = new ArrayList<ArrayList<MzzWoocommerceMap>>();
			ArrayList<MzzWoocommerceMap> filteredMaps = new ArrayList<MzzWoocommerceMap>();
			if(sortArrayAttributes.size() > 0)
			{
				//Create Mapping list for each Orderline
				//For each line in sortArrayAttributes, create a single MzzWoocommerceMap with the conflicting MzzWoocommerceMap records removed
				//Becomes a 'skeleton' to which map records for each orderline can be created.
				filteredMaps = createFilteredMapList(masterZzWoocommerceMapList, sortArrayAttributes);
			}
			if(sortArrayProducts.size() > 0)
			{
				if(filteredMaps.size() < 1)
				{
					filteredMaps = createFilteredMapList(masterZzWoocommerceMapList, sortArrayProducts);
				}
				else
				{
					filteredMaps = createFilteredMapList(filteredMaps, sortArrayProducts);
				}
				
			}
			//filteredMaps is now good to use - no conflicting entries
			
			if(sortArrayAttributes.size() > 0 && !(sortArrayProducts.size() > 0))//Create a list for attribute MOrderlIne creation only.
			{
				interimMzzWoocommerceMapListAttributes = createOrderLineMapSubList(sortArrayAttributes, filteredMaps);
				processOrderLine(orderLine, interimMzzWoocommerceMapListAttributes, trxName);
			}
			if(sortArrayProducts.size() > 0 && !(sortArrayAttributes.size() > 0))//Create a list for product related MOrderline creation only.
			{
				interimMzzWoocommerceMapListProduct = createOrderLineMapSubList(sortArrayProducts, filteredMaps);
				if(sortArrayNonDuplicateAttributes.size() > 0)
				{
					interimMzzWoocommerceMapListProduct = addNonDuplicateAttributesTointerimMzzWoocommerceMapList(interimMzzWoocommerceMapListProduct, sortArrayNonDuplicateAttributes);
				}
				/*//Duplicate product attributes are not detected - sortArrayNonDuplicateProductAttributes will always be size=0
				if(sortArrayNonDuplicateProductAttributes.size() > 0)
				{
					interimMzzWoocommerceMapListProduct = addNonDuplicateAttributesTointerimMzzWoocommerceMapList(interimMzzWoocommerceMapListProduct, sortArrayNonDuplicateProductAttributes);
				}
				*/
				processOrderLine(orderLine, interimMzzWoocommerceMapListProduct , trxName);
			}
			
			if(sortArrayProducts.size() > 0 && sortArrayAttributes.size() > 0)//Create a list for product & attribute related MOrderline creation.
			{
				ArrayList<MzzWoocommerceMap> emptyMap = new ArrayList<MzzWoocommerceMap>();//Use an empty map - basically creates a MzzWoocommerceMap instead of MzzWoocommerceMapLine
				
				interimMzzWoocommerceMapListAttributes = createOrderLineMapSubList(sortArrayAttributes, emptyMap);
				interimMzzWoocommerceMapListProduct = createOrderLineMapSubList(sortArrayProducts, emptyMap);
				mergedMzzWoocommerceMapListProduct  = mergeMaps(interimMzzWoocommerceMapListAttributes, interimMzzWoocommerceMapListProduct, filteredMaps);
				processOrderLine(orderLine, mergedMzzWoocommerceMapListProduct, trxName);
			}
			
			
			//Delete or comment out once testing complete
			for(ArrayList<MzzWoocommerceMap> mapItem : mergedMzzWoocommerceMapListProduct)
			{
				System.out.println("************Begin Orderline*************");
				
				for(MzzWoocommerceMap mzzWoocommerceMap : mapItem)
				{
					System.out.println(mzzWoocommerceMap.toString() + " " + mzzWoocommerceMap.getwoocommerce_field_label());
				}
				System.out.println("************End Orderline*************");
				
			}//End Delete or comment out once testing complete
			
	/*		{
				interimMzzWoocommerceMapList 
				= createOrderlineMapList(masterZzWoocommerceMapList, sortArrayAttributes);
				
				System.out.println( interimMzzWoocommerceMapList.toString());
				
				//For each line in sortArrayAttributes, add the parent MzzWoocommerceMap records back to the lists created above
				//for(ArrayList<MzzWoocommerceMapLine> sortedLines : sortArrayAttributes)
					for(int idx = 0; idx < sortArrayAttributes.size(); idx++)
				{
					//Adding the contents of each line in sortArrayAttributes to each line in interimMzzWoocommerceMapList
						ArrayList<MzzWoocommerceMap> mzzWoocommerceMapLn = interimMzzWoocommerceMapList.get(idx);
						ArrayList<MzzWoocommerceMapLine> sortedLines = sortArrayAttributes.get(idx);
					for(int pos = 0; pos < sortedLines.size(); pos++)
					{
						//The MzzWoocommerceMap objects are added back to the List we will use to create orderlines.
						mzzWoocommerceMapLn.add(new MzzWoocommerceMap(ctx, sortedLines.get(pos).getZZ_Woocommerce_Map_ID(), trxName));
					}
				}
					System.out.println( "interimMzzWoocommerceMapList.size(): "+ interimMzzWoocommerceMapList.size());
			}//We now have interimMzzWoocommerceMapList with enough info to create the attributes of the order lines
			//If they exist, create a multiple of sum(attribute variations) x sum(product variations)
			ArrayList<ArrayList<MzzWoocommerceMap>>  finalMzzWoocommerceMapList = new ArrayList<ArrayList<MzzWoocommerceMap>>();
			if(sortArrayProducts.size() > 0)
			{
					for(ArrayList<MzzWoocommerceMap> interimMap : interimMzzWoocommerceMapList)
				{
					finalMzzWoocommerceMapList = createOrderlineMapList(interimMap, sortArrayProducts);
				}
					System.out.println(finalMzzWoocommerceMapList.toString());
			}
			
	*/
			
			//use interimMzzWoocommerceMapList [0] -> [x] and createOrderlineMapList with sortArrayProducts as parameter, 
			//look at using interimMzzWoocommerceMapList [0] -> [x] as parameter instead of master list?
			
			
			/*ArrayList<ArrayList<MzzWoocommerceMap>>  createOrderlineMapList(
			ArrayList<MzzWoocommerceMap> mapList, 
			ArrayList<ArrayList<MzzWoocommerceMapLine>> mapLineListInProgress,
			ArrayList<ArrayList<MzzWoocommerceMap>> mapListInProgres*/
			
			
			
		}//End else
		/****************************Refactor from here**************************************************/
		/*
 		if(duplicateOrderLine)
		{
			duplicateOrderLine(orderLine, trxName);
		}
		duplicateOrderLine = false;
		duplicateFields = null;
		*/
		//Added by Phil
		/****************************Refactor to here**************************************************/
		
		if (!orderLine.save())
		 {
		
			int orgID = order.getAD_Org_ID();
			MzzWoocommerce mzzWoocommerce = MzzWoocommerce.get(orgID, ctx, trxName);
			String email = mzzWoocommerce.getnotify_email();
			StringBuilder msg = new StringBuilder("WooCommerce sync encountered a problem. Order: ");
			msg.append(order.getDocumentNo());
			msg.append(" had an Orderline save error. ");
			msg.append("Check logs for the error. ");
			msg.append(". The WooCommerce sync process may need to be run manually.");
			WcMailNotify.sendEmail(email, msg.toString(), "", ctx, trxName);//Email any issues found.
			log.warning(msg.toString());
			//throw new IllegalStateException("Could not create Order Line");//Commented out 12/11/24 to improve error handling
			return false;
		}
		
		//if (!orderLine.save()) {
			//throw new IllegalStateException("Could not create Order Line");
		//}
		return true;
	}
	
	/**
	 * We iterate through the sortArrayNonDuplicateAttributes and distribute the contents equally to all lines in the sortArrayNonDuplicateAttributes
	 * ArrayList.
	 * @param sortArrayNonDuplicateAttributes
	 * @return
	 */
	private ArrayList<ArrayList<MzzWoocommerceMap>> addNonDuplicateAttributesTointerimMzzWoocommerceMapList(ArrayList<ArrayList<MzzWoocommerceMap>> interimMzzWoocommerceMapListProduct, ArrayList<MzzWoocommerceMap> sortArrayList) {
		for(MzzWoocommerceMap mzzWoocommerceMap : sortArrayList)//Loop through both Arrays and remove any duplicates with sortArrayNonDuplicateAttributes
		{
			for(ArrayList<MzzWoocommerceMap> mzzWoocommerceMapArray : interimMzzWoocommerceMapListProduct)
			{
				for(int j = 0; j < mzzWoocommerceMapArray.size(); j++)
				{
					if(mzzWoocommerceMap.equals(mzzWoocommerceMapArray.get(j))) 
					{
						mzzWoocommerceMapArray.remove(j);
					}
				}
			}
		}
	
		int interimMapListSize = interimMzzWoocommerceMapListProduct.size();
		int sortArraySize = sortArrayList.size();
		if(interimMapListSize == sortArraySize)
		{
			for(int j = 0; j < sortArraySize; j++) 
			{
				interimMzzWoocommerceMapListProduct.get(j).add(sortArrayList.get(j));
			}
		}
		else if(interimMapListSize > sortArraySize)
		{
			int y=0;
			for(int jj = 0; jj < interimMapListSize; jj++ )
			{
				ArrayList<MzzWoocommerceMap> mzzWoocommerceMap = interimMzzWoocommerceMapListProduct.get(jj);
				mzzWoocommerceMap.add(sortArrayList.get(y));
				y++;
				if(y == sortArraySize -1) y = 0;
			}
		}
		
		else if(sortArraySize > interimMapListSize)
		{
			int y=0;
			for(int jj = 0; jj < sortArraySize; jj++ )
				{
					ArrayList<MzzWoocommerceMap> mzzWoocommerceMap = interimMzzWoocommerceMapListProduct.get(y);
					mzzWoocommerceMap.add(sortArrayList.get(jj));
					y++;
					if(y == interimMapListSize -1) y = 0;
				}
		}
		return interimMzzWoocommerceMapListProduct;
	}//addNonDuplicateAttributesTointerimMzzWoocommerceMapList
	

	/**
	 * Determines if there are any duplicate attributes that are not for the creation of duplicate order lines
	 * @param duplicateAttributes
	 * @param sortArray
	 * @return
	 */
	private ArrayList<MzzWoocommerceMapLine> removeDontAddToDuplicateAttributes(
			ArrayList<MzzWoocommerceMapLine> duplicateAttributes, ArrayList<MzzWoocommerceMap> sortArray) {
		ArrayList<MzzWoocommerceMapLine> nonDuplicateAttributes = new ArrayList<MzzWoocommerceMapLine>();
		for(MzzWoocommerceMapLine mapLine : duplicateAttributes)
		{
			if(mapLine.isParentAddToDuplicate(mapLine.get_ID()))
			{
				nonDuplicateAttributes.add(mapLine);
			}
			else
			{
				sortArray.add(new MzzWoocommerceMap(Env.getCtx(), mapLine.getZZ_Woocommerce_Map_ID(), null));
			}
		}
		//sortArrayNonDuplicateAttributes.add(nonDuplicateAttributes);
		return nonDuplicateAttributes;
	}

	/**
	 * 
	 * @param line
	 * @param mapList
	 * @param trxn
	 */
	public void processOrderLine(MOrderLine line, ArrayList<ArrayList<MzzWoocommerceMap>> mapList, String trxn /*, List<?> changeItems*/) {		
		for(ArrayList<MzzWoocommerceMap> mzzWoocommerceMapList : mapList)
		{
			processWooCommMeta(mzzWoocommerceMapList, line, ctx, trxn);
			System.out.println(mapList.indexOf(mzzWoocommerceMapList));
			line.saveEx();
			if(1 + mapList.indexOf(mzzWoocommerceMapList) < mapList.size())//Don't copy the last line.
			{
				MOrderLine duplicateOrderLine = new MOrderLine(order);
				MOrderLine.copyValues(line, duplicateOrderLine);
				if(line.getM_AttributeSetInstance_ID() > 0)
				{
						MAttributeSetInstance duplicateMAttributeSetInstance = new MAttributeSetInstance(ctx, 0 ,trxn);
						duplicateMAttributeSetInstance.save();
						duplicateOrderLine.setM_AttributeSetInstance_ID(duplicateMAttributeSetInstance.get_ID());
				}
				String sql = "SELECT COALESCE(MAX(Line),0)+10 FROM C_OrderLine WHERE C_Order_ID=?";
				int ii = DB.getSQLValue (trxn, sql, order.getC_Order_ID());
				duplicateOrderLine.setLine (ii);
				//duplicateOrderLine.setDiscount(Env.ONEHUNDRED);
				duplicateOrderLine.save();
				duplicateOrderLine.setPrice(Env.ZERO);
				duplicateOrderLine.setPriceList(Env.ZERO);//Duplicates from WooComm don't get priced, 1st line is the total.
				duplicateOrderLine.setLineNetAmt();
				duplicateOrderLine.saveEx();
				line = duplicateOrderLine;
			}
		}
		
	/*	MOrderLine duplicateOrderLine = new MOrderLine(order);
		MOrderLine.copyValues(line, duplicateOrderLine);
		//MAttributeSetInstance duplicateMAttributeSetInstance = new MAttributeSetInstance(ctx, 0 ,trxn);
		//duplicateMAttributeSetInstance.save();
		//duplicateOrderLine.setM_AttributeSetInstance_ID(duplicateMAttributeSetInstance.get_ID());
		
		
		duplicateOrderLine.save();
		for(LinkedHashMap<String, Object> duplicateField : duplicateFields)
		{
			processWooCommMeta(duplicateOrderLine,  ctx, trxn);
		}
		duplicateOrderLine.save();
	*/
	}
	
	/**
	 * 
	 * @param attributes
	 * @param products
	 * @param filteredMap
	 * @return
	 */
	private ArrayList<ArrayList<MzzWoocommerceMap>> mergeMaps(ArrayList<ArrayList<MzzWoocommerceMap>> attributes, 
			ArrayList<ArrayList<MzzWoocommerceMap>> products
			, ArrayList<MzzWoocommerceMap> filteredMap) 
	{
		ArrayList<ArrayList<MzzWoocommerceMap>> mergedMap = new ArrayList<ArrayList<MzzWoocommerceMap>>();
		for(ArrayList<MzzWoocommerceMap> attributeMapItem : attributes)
		{
			
			for(ArrayList<MzzWoocommerceMap> productItem : products)
			{
				ArrayList<MzzWoocommerceMap> filteredMapCopy = new ArrayList<MzzWoocommerceMap>(filteredMap);
				filteredMapCopy.addAll(attributeMapItem);
				System.out.println("attributeMapItem " + attributeMapItem.toString());
				System.out.println("productItem " + productItem.toString());
				filteredMapCopy.addAll(productItem);
				mergedMap.add(filteredMapCopy);
			}
		}
		return mergedMap;
	}
	
	/**
	 * For each line in sortArrayAttributes, add the parent MzzWoocommerceMap records back to the lists created above
	 * @param sortedArray
	 * @param filteredMap
	 * @return
	 */
	private ArrayList<ArrayList<MzzWoocommerceMap>> createOrderLineMapSubList(ArrayList<ArrayList<MzzWoocommerceMapLine>> sortedArray, ArrayList<MzzWoocommerceMap> filteredMap) {
		
		ArrayList<ArrayList<MzzWoocommerceMap>> mapToReturn = new ArrayList<ArrayList<MzzWoocommerceMap>>();
		//Loop through the sorted map lines, create ArrayList<MzzWoocommerceMap> for each intended orderline
		/*Note that the sortedArray parameter contains MapLines; we only want the Parents (Maps).
		 * 
		 */
		HashSet<MzzWoocommerceMap> hashMzzWoocommerceMap = new HashSet<MzzWoocommerceMap>();
		for(ArrayList<MzzWoocommerceMapLine> mplnArrayList : sortedArray)
		{
			for(MzzWoocommerceMapLine mapLine : mplnArrayList)
			{
				hashMzzWoocommerceMap.add(new MzzWoocommerceMap(Env.getCtx(), mapLine.getZZ_Woocommerce_Map_ID(), trxName));
			}
		}
		
		for(MzzWoocommerceMap deDupedMap : hashMzzWoocommerceMap)
		{
			ArrayList<MzzWoocommerceMap> returnMap = new ArrayList<MzzWoocommerceMap>(filteredMap);
			returnMap.add(deDupedMap);
			mapToReturn.add(returnMap);
		}
		
		/*
		for(ArrayList<MzzWoocommerceMapLine> sortedArrayItem : sortedArray)
		{
			ArrayList<MzzWoocommerceMap> mzzWoocommerceMapFromLine = new ArrayList<MzzWoocommerceMap>(filteredMap);
			for(MzzWoocommerceMapLine mzzWoocommerceMapLine : sortedArrayItem)
			{
				MzzWoocommerceMap mapToAdd = new MzzWoocommerceMap(ctx, mzzWoocommerceMapLine.getZZ_Woocommerce_Map_ID(), trxName);
				if(!mzzWoocommerceMapFromLine.contains(mapToAdd)) mzzWoocommerceMapFromLine.add(mapToAdd);
			}
			mapToReturn.add(mzzWoocommerceMapFromLine);
		}
		*/
		return mapToReturn;
	}
	
	/**
	 * A preparatory method to create objects parameters for sortMapLinesIntoArrayLists()
	 * @param sortArray
	 * @param duplicates
	 * @return
	 */
	public ArrayList<ArrayList<MzzWoocommerceMapLine>> processRepeatingFields(ArrayList<ArrayList<MzzWoocommerceMapLine>> sortArray, ArrayList<MzzWoocommerceMapLine> duplicates) {
		
		for(MzzWoocommerceMapLine mzzWoocommerceMapLine : duplicates)
		{
			//
			if(sortArray.size() < 1)//initialise with the first element of the duplicate list
			{
				ArrayList<MzzWoocommerceMapLine> firstEntry = new ArrayList<MzzWoocommerceMapLine>();
				firstEntry.add(mzzWoocommerceMapLine);
				sortArray.add(firstEntry);
			}
			sortArray = sortMapLinesIntoArrayLists(sortArray, mzzWoocommerceMapLine);
		}
		return sortArray;
	}
	
	/**
	 * 
	 * @param arrayListInProgress
	 * @param zzzWoocommerceMapLine
	 * @return
	 */
	public ArrayList<ArrayList<MzzWoocommerceMapLine>> sortMapLinesIntoArrayLists(ArrayList<ArrayList<MzzWoocommerceMapLine>> arrayListInProgress, MzzWoocommerceMapLine zzzWoocommerceMapLine) {
		//Is the mzzWoocommerceMapLine already on the list?
		//for(ArrayList<MzzWoocommerceMapLine> mapArrayList : arrayListInProgress)
		
	  if(!(isAlreadyInArrayListInProgress(arrayListInProgress, zzzWoocommerceMapLine)))
	  {
		for(int xx = 0; xx < arrayListInProgress.size(); xx++)//Start at the beginning and compare the mapline to contents
		{
			ArrayList<MzzWoocommerceMapLine> currentListItem = 	arrayListInProgress.get(xx);
			//for(int v =0; v < currentListItem.size(); v++)
			//{
				//switch(currentListItem.get(v).getzz_woocommerce_map_type()) 
				switch(zzzWoocommerceMapLine.getzz_woocommerce_map_type())
				{
				case WOOCOMMERCE_MAP_TYPE_ATTRIBUTE:
					//is it setting the same Attribute? if it is then do nothing, continue iterating
					if(!(isAttributeMapTypeInList(currentListItem, zzzWoocommerceMapLine)))
						{
							currentListItem.add(zzzWoocommerceMapLine);
							return arrayListInProgress;
						}
			
					break;
				case WOOCOMMERCE_MAP_TYPE_PRODUCT_ADD:
					//Is it setting the same producttype?
					if(!(isProductPartMapTypeInList(currentListItem, zzzWoocommerceMapLine)))
					{
						currentListItem.add(zzzWoocommerceMapLine);
						return arrayListInProgress;
					}
					
					break;
					
				case WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE:
					//Is it setting the same product attribute, EG fabric colour?
					if(!(isProductAttributeTypeInList(currentListItem, zzzWoocommerceMapLine)))
					{
						currentListItem.add(zzzWoocommerceMapLine);
						return arrayListInProgress;
					}
					
					
				}/*Switch end - zzzWoocommerceMapLine is not in zzzWoocommerceMapLine
				and doesn't match any of the existing map types*/
		}/*End of outer loop - default action. We're left with zzzWoocommerceMapLine that matches nothing 
			in the above tests, so add it to a NEW List */
		ArrayList<MzzWoocommerceMapLine> addedzzMapLineList = new ArrayList<MzzWoocommerceMapLine>();
		addedzzMapLineList.add(zzzWoocommerceMapLine);
		arrayListInProgress.add(addedzzMapLineList);
		
		return arrayListInProgress;
	  }
		return arrayListInProgress;
		
	}
	
	/**
	 * 
	 * @param list
	 * @param line
	 * @return
	 */
	private boolean isProductPartMapTypeInList(ArrayList<MzzWoocommerceMapLine> list, MzzWoocommerceMapLine line) {
		for(int vv =0; vv < list.size(); vv++)
		{
			if(list.get(vv).getzz_woocommerce_map_type().equals(line.getzz_woocommerce_map_type()))
			{
				if(list.get(vv).getBLD_Product_PartType_ID() == line.getBLD_Product_PartType_ID())return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param list
	 * @param line
	 * @return
	 */
	private boolean isAttributeMapTypeInList(ArrayList<MzzWoocommerceMapLine> list, MzzWoocommerceMapLine line) {
		for(int vv =0; vv < list.size(); vv++)
		{
			if(list.get(vv).getzz_woocommerce_map_type().equals(line.getzz_woocommerce_map_type()))
			{
				if(list.get(vv).getM_Attribute_ID() == line.getM_Attribute_ID())return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param list
	 * @param line
	 * @return
	 */
	private boolean isProductAttributeTypeInList(ArrayList<MzzWoocommerceMapLine> list, MzzWoocommerceMapLine line) {
		for(int vv =0; vv < list.size(); vv++)
		{
			if(list.get(vv).getzz_woocommerce_map_type().equals(line.getzz_woocommerce_map_type()))
			{
				if(list.get(vv).getM_Attribute_Product_ID() == line.getM_Attribute_Product_ID())return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param arrayListInProgress
	 * @param line
	 * @return
	 */
	private boolean isAlreadyInArrayListInProgress(ArrayList<ArrayList<MzzWoocommerceMapLine>> arrayListInProgress, MzzWoocommerceMapLine line) {
		for(int xx = 0; xx < arrayListInProgress.size(); xx++)//Start at the beginning and compare the mapline to contents
		{
			ArrayList<MzzWoocommerceMapLine> currentListItem = 	arrayListInProgress.get(xx);	
			for(int v =0; v < currentListItem.size(); v++)
			{
				//Is this element already in the arrayListInProgress? If yes, we skip and return.
				if(currentListItem.get(v).get_ID() == line.get_ID())
				{
					return true;
				}
			}
		}
		return false;
		
	}
	

	/**
	 * 
	 * @param name
	 * @return
	 */
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
		return getxZZWoocommerceMatch(woocommID).getM_Product_ID();
	}
	
	/**
	 * Returns the record for the name stored in the system for the mapped WooCommerce product.
	 * This is not synced with WooCommerce.
	 * @param woocommID
	 * @return
	 */
	public String getWooCommerceProductName(int woocommID) {
		return getxZZWoocommerceMatch(woocommID).getName();
	}
	
	public X_ZZ_Woocommerce_Match getxZZWoocommerceMatch(int woocommID) {
		
		int orgID = order.getAD_Org_ID();
		MzzWoocommerce mzzWoocommerce = MzzWoocommerce.get(orgID, ctx, trxName);
		PO[] xZZWoocommerceMatches = mzzWoocommerce.getLines("", "");
		for(int x = 0; x < xZZWoocommerceMatches.length; x++)
		{
			X_ZZ_Woocommerce_Match xZZWoocommerceMatch = new X_ZZ_Woocommerce_Match(ctx, xZZWoocommerceMatches[x].get_ID(), trxName);
			if(xZZWoocommerceMatch.getwoocommerce_key() == woocommID)
			{
				return xZZWoocommerceMatch /*.getM_Product_ID()*/;
				
			}
		}
		 StringBuilder msg = new StringBuilder("Unable to Match WooCommerce product to Idempiere product. ");
		 msg.append("WooCommerce product ID: ");
		 msg.append(woocommID);
		 msg.append(" Go to the WooCommerce Defaults window and add a record in the WooCommerce Product Mapping tab.");
		 log.warning(msg.toString());
		 String email = mzzWoocommerce.getnotify_email();
		 if(email != null) WcMailNotify.sendEmail(email, msg.toString(), "", ctx, trxName);//Email any issues found.
		 log.warning(msg.toString());
		 throw new AdempiereUserError(msg.toString());
	}
	

	/**
	 * 
	 * @param orderWc
	 */
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
			int orgID = order.getAD_Org_ID();
			MzzWoocommerce mzzWoocommerce = MzzWoocommerce.get(orgID, ctx, trxName);
			String email = mzzWoocommerce.getnotify_email();
			StringBuilder msg = new StringBuilder("WooCommerce sync encountered a problem. Order: ");
			msg.append(order.getDocumentNo());
			msg.append(" had an Orderline save error. ");
			msg.append(" The error occured in method: createShippingCharge(Map<?, ?> orderWc)");
			msg.append(" Check logs for the error. ");
			msg.append(". The WooCommerce sync process may need to be run manually.");
			WcMailNotify.sendEmail(email, msg.toString(), "", ctx, trxName);//Email any issues found.
			log.warning(msg.toString());
			throw new IllegalStateException("Could not create Order Line");
		}
	}

	public int getTaxRate(Map<?, ?> orderWc) {
		List<?> taxLines = (List<?>) orderWc.get("tax_lines");
		String taxRate = "" ;
		/*Bug? Changed by PB 1/8/24*/ /*if(taxLines.size() > 1)*/ if(taxLines.size() > 0) //Do we have a tax line?
		{
			Map<?, ?> taxLine = (Map<?, ?>) taxLines.get(0);
			taxRate = (String) taxLine.get("label");
		}
		
		return (taxRate.equals("GST") ? (int) wcDefaults.get_Value("standard_tax_id")
				: (int) wcDefaults.get_Value("zero_tax_id"));
	}

	public BigDecimal getShippingCost(Map<?, ?> orderWc) {
		List<?> shippingLines = (List<?>) orderWc.get("shipping_lines");
		Map<?, ?> shippingLine = (Map<?, ?>) shippingLines.get(0);
		Double total = Double.parseDouble((String) shippingLine.get("total"));
		Double totalTax = Double.parseDouble((String) shippingLine.get("total_tax"));
		BigDecimal shippingCost = !isTaxInclusive ? BigDecimal.valueOf((Double) total + totalTax)//Was BigDecimal shippingCost = isTaxInclusive 2/10/24
				: BigDecimal.valueOf((Double) total);
		return (shippingCost.setScale(4, RoundingMode.HALF_EVEN));
	}

	public BigDecimal calcOrderLineUnitPrice(Map<?, ?> line) {
		// Double price = (Double) line.get("price");
		Double price = ((Number) line.get("price")).doubleValue();
		BigDecimal unitPrice = new BigDecimal(price);
		if (!isTaxInclusive) {//Was if (isTaxInclusive) 2/10/24. If it is tax inclusive, why add the tax??
			List<?> taxList = (List<?>) line.get("taxes");
			Map<?, ?> taxes;
			if(taxList.size() > 0)//Was if(taxList.size() > 1) 1/8/24
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
		{
			int orgID = order.getAD_Org_ID();
			MzzWoocommerce mzzWoocommerce = MzzWoocommerce.get(orgID, ctx, trxName);
			String email = mzzWoocommerce.getnotify_email();
			StringBuilder msg = new StringBuilder("POS Payment creation failed for order: ");
			msg.append(order.getDocumentNo());
			msg.append(". Please check the order and try and create the payment manually. ");
			msg.append("Please also check that the order lines have been created as per the WooCommerce order. ");
			msg.append("The WooCommerce sync process may need to be run manually.");
			WcMailNotify.sendEmail(email, msg.toString(), "", ctx, trxName);//Email any issues found.
			log.warning(msg.toString());
			throw new IllegalStateException("Could not create POSPayment");
		}
			
	}

	public static boolean isBlankOrNull(String str) {
		return (str == null || "".equals(str.trim()));
	}
	
	/**
	 * 
	 * @param line
	 * @param field
	 * @param mProductID
	 * @param ctx
	 */
	public void processWooCommMeta(ArrayList<MzzWoocommerceMap> mapListToProcess, MOrderLine line,/* LinkedHashMap<String, Object> field, int mProductID,*/ Properties ctx, String trxn) {
		int mProductID = line.getM_Product_ID();
		
		//Get field data
		//String fieldID = (String) field.get("id");
		//String fieldLabel = (String)field.get("label");
		//String fieldValue = (String) field.get("value");
		
		
		/*Refactor to create multiple
		 * Get all the MzzWoocommerceMapLines into one List 'Lines'.
		 * Check for multiple attributes with same name, like width
		 * remove it from 'Lines', Create a separate List for the above and  add the multiples to it
		 * Remove the 
		 * 
		 * Check for multiple product adds with the same parttype, like fabric
		 * remove it from 'Lines',Create a separate List for the above and add the multiples to it
		 * "lines' should be good to make the first OrderLine once the process is complete
		 */
		
		for(MzzWoocommerceMap currentMapItem : mapListToProcess)
		{
			String whereClause = "and "+ MzzWoocommerceMapLine.COLUMNNAME_IsActive + "='Y'";
			//String fv = (String) fieldValues.get(currentMapItem.get_ID());
			currentMapItem.setFieldValue((String) fieldValues.get(currentMapItem.get_ID()));
			MzzWoocommerceMapLine[] mzzWoocommerceMapLines = mzzWoocommerceMapLines = currentMapItem.getMzzWoocommerceMapLines(currentMapItem.get_ID(), ctx, "", whereClause);
					
				
		for(int l = 0; l < mzzWoocommerceMapLines.length; l++) //Make this inner loop
		{
			
			String mapType = mzzWoocommerceMapLines[l].getzz_woocommerce_map_type();
			/*if(mzzWoocommerceMapLines[l].isAdd_To_Duplicate())
			{
				//Set flags, add duplicate field to ArrayList, skip processing the field on this MOrderLine.
				//Bad code, refactor.
				duplicateOrderLine = true;
				duplicateFields = new ArrayList<LinkedHashMap>();
				duplicateFields.add(field);
				break;
			}*/
			
			if(mapType.equals(WOOCOMMERCE_MAP_TYPE_ATTRIBUTE))
				{
				/*Check if an Attributeset instance already exists, if not create
				 Add the attribute and its value to the Attributeset instance*/
				
					MAttribute mapAttribute = new MAttribute(ctx, mzzWoocommerceMapLines[l].getM_Attribute_ID(), trxn);
					int mAttributeSetID = MProduct.get(mProductID).getM_AttributeSet_ID();
					String attributeValueType = mapAttribute.getAttributeValueType();
					MAttributeSetInstance mAttributeSetInstance = new MAttributeSetInstance(ctx, line.getM_AttributeSetInstance_ID(), trxn);
					mAttributeSetInstance.saveEx(trxn);
			
					mAttributeSetInstance.setM_AttributeSet_ID(mAttributeSetID);
					int m_AttributeSetInstance_ID = mAttributeSetInstance.get_ID();
					line.setM_AttributeSetInstance_ID(m_AttributeSetInstance_ID);
					System.out.println(currentMapItem.getFieldValue());
					setAttribute(attributeValueType, mzzWoocommerceMapLines[l], m_AttributeSetInstance_ID, currentMapItem.getFieldValue(), mapType,trxn);
					//setAttribute(String attributeValueType, MzzWoocommerceMap mzzWoocommerceMap, int m_AttributeSetInstance_ID, Properties field, String trxn)
	
					mAttributeSetInstance.saveEx();
					line.saveEx();
				}
		else if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ADD))
		{
			//Check if a bld_line_productsetinstance exists, if not, create one.
			/*Procedure to keep MBLDLineProduct Instance database integrity so the dialogue works:
			 *Create empty database records first -  Get the product partset, write blank records to DB
			 *Update the records by finding and creating an object.
			 *
			*/
			
			//Create/get the MBLDLineProductSetInstanceID
			MBLDLineProductSetInstance mBldLineProductSetInstance = new MBLDLineProductSetInstance(ctx, line.get_ValueAsInt("bld_line_productsetinstance_id"), trxn);
			mBldLineProductSetInstance.saveEx(trxn);
			int bldLineProductSetInstanceID = mBldLineProductSetInstance.get_ID();
			line.set_ValueNoCheck("bld_line_productsetinstance_id", bldLineProductSetInstanceID);
			line.save();
			
			//if bld_line_productinstance records have not been created, then create them
			MBLDLineProductInstance[] mBLDLineProductInstances = MBLDLineProductInstance.getmBLDLineProductInstance(bldLineProductSetInstanceID, trxn);
			if(mBLDLineProductInstances.length < 1 || mBLDLineProductInstances == null)
			{
				//Create empty bld_line_productinstance records.
				MBLDProductPartType [] partSet = mBldLineProductSetInstance.getProductPartSet(mProductID, trxn, true);
				for(int s = 0; s < partSet.length; s++)
				{
					MBLDLineProductInstance mBLDLineProductInstance = new MBLDLineProductInstance(ctx, 0, trxn);
					mBLDLineProductInstance.setBLD_Product_PartType_ID(partSet[s].getBLD_Product_PartType_ID());
					mBLDLineProductInstance.setBLD_Line_ProductSetInstance_ID(bldLineProductSetInstanceID);
					mBLDLineProductInstance.save();
				}
				
			}
			
			 //get a bld_line_productinstance record and add the product
			if(mBLDLineProductInstances.length < 1 || mBLDLineProductInstances == null)
			{
				mBLDLineProductInstances = MBLDLineProductInstance.getmBLDLineProductInstance(bldLineProductSetInstanceID, trxn);
			}
				int mzzWoocommerceMapBLD_Product_PartType_ID = mzzWoocommerceMapLines[l].getBLD_Product_PartType_ID();
				for(int g = 0; g < mBLDLineProductInstances.length; g++)
				{
					if(mBLDLineProductInstances[g].getBLD_Product_PartType_ID() == mzzWoocommerceMapBLD_Product_PartType_ID) 
					{
						mBLDLineProductInstances[g].setM_Product_ID(mzzWoocommerceMapLines[l].getM_Product_Line_ID());
						mBLDLineProductInstances[g].save();
						break;
					}
				}
				mBldLineProductSetInstance.setDescription(mProductID);
				mBldLineProductSetInstance.save();
		}
		
		else if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE))
		{
			//Get the MBLDLineProductInstance. This relies on the products being added to the MBLDLineProductInstance before this code runs.
			/*How to handle multiple cases of bld_line_productsetinstances containing multiple records of the same product?
			 * Check existence of m_attributesetinstance_id in bld_line_productinstance -> if it does not exist then create and add attribute & value
			 * If m_attributesetinstance_id in bld_line_productinstance > 0 then it already exists -> check if the attribute value has been set
			 * Ifattribute has not been set, then set it.
			 */
			int bldLineProductSetInstanceID = line.get_ValueAsInt("bld_line_productsetinstance_id");
			//int mzzWoocommerceMapm_attribute_product_id = mzzWoocommerceMap.getm_attribute_product_id();
			int mzzWoocommerceMapBLD_Product_PartType_ID = mzzWoocommerceMapLines[l].getBLD_Product_PartType_ID();
			MBLDLineProductInstance[] mBLDLineProductInstance = MBLDLineProductInstance.getmBLDLineProductInstance(bldLineProductSetInstanceID, trxn);
			for(int m = 0; m < mBLDLineProductInstance.length; m++)
			{
				if(mBLDLineProductInstance[m].getBLD_Product_PartType_ID() == mzzWoocommerceMapBLD_Product_PartType_ID)
				{
					//Create attributeinstance, link
					MAttributeSetInstance mAttributeSetInstance = new MAttributeSetInstance(ctx, mBLDLineProductInstance[m].getM_AttributeSetInstance_ID(), trxn);
					mAttributeSetInstance.save();
					int mAttributeSetInstanceID = mAttributeSetInstance.get_ID();
					mBLDLineProductInstance[m].setM_AttributeSetInstance_ID(mAttributeSetInstanceID);
					mBLDLineProductInstance[m].save();
					//int c = mzzWoocommerceMapLines[l].getM_AttributeValue_ID();
					mAttributeSetInstance.setM_AttributeSet_ID(MProduct.get(mzzWoocommerceMapLines[l].getM_Product_Line_ID()).getM_AttributeSet_ID());
					mAttributeSetInstance.save();
					MAttributeValue mapAttributeValue = new MAttributeValue(ctx, mzzWoocommerceMapLines[l].getM_Attribute_Product_ID(), trxn);
					MAttribute mapAttribute = new MAttribute(ctx, mapAttributeValue.getM_Attribute_ID(), trxn);
					System.out.println(currentMapItem.getFieldValue());
					setAttribute(mapAttribute.getAttributeValueType(), mzzWoocommerceMapLines[l], mAttributeSetInstanceID, currentMapItem.getwoocommerce_field_value(), mapType, trxn);
					break;
				}
			}
		}
		
			/*
			MAttribute mapAttribute = new MAttribute(ctx, mzzWoocommerceMap.getm_attribute_product_id(), trxn);
			String attributeValueType = mapAttribute.getAttributeValueType();
			int mBLDLineProductSetInstanceID = line.get_ValueAsInt("bld_line_productsetinstance_id");
			if(mBLDLineProductSetInstanceID < 1) throw new AdempiereUserError("Can't add product attrivute to line product options. Check field import order or rewrite code");
			MBLDLineProductInstance[] mBLDLineProductInstance = MBLDLineProductInstance.getmBLDLineProductInstance(mBLDLineProductSetInstanceID, trxn);
			
			//Check to see if there's more than one product that matches the mapping instruction
			List<MBLDLineProductInstance> mBLDLineProductInstanceList = new ArrayList<>();
			for(int q = 0; q < mBLDLineProductInstance.length; q++)
			{
				if(mBLDLineProductInstance[q].getM_Product_ID() == mzzWoocommerceMap.getm_product_line_id())//Instance matches mapping
				{
					mBLDLineProductInstanceList.add(mBLDLineProductInstance[q]);
				}
			}
			if(mBLDLineProductInstanceList.size() > 1)//We have more than 1 product instance to add an attribute to, need to check if an attribute record already exists.
			{
				for(MBLDLineProductInstance instance : mBLDLineProductInstanceList)
				{
					int mAttributeSetInstanceID	= instance.getM_AttributeSetInstance_ID();
					if(mAttributeSetInstanceID > 0)//The product option already has an attribute set instance - is our attribute already in the instance?
					{
						
						
						StringBuilder whereClauseFinal = new StringBuilder(MAttributeInstance.COLUMNNAME_M_AttributeSetInstance_ID+"=? ");
					
						List<MAttributeInstance> attributeInstancelist = new Query(ctx, I_M_AttributeInstance.Table_Name, whereClauseFinal.toString(), trxn)
														.setParameters(mAttributeSetInstanceID)
														.list();
						for(MAttributeInstance instanceItem : attributeInstancelist)
						{
							//Does the attributeinstance in this attribute set instance have the mapping attribute already set?
							if(instanceItem.getM_Attribute_ID() == mzzWoocommerceMap.getM_Attribute_ID())//Attribute matches the one we want to set
							{
								if(instanceItem.getM_AttributeValue() == null)//It has not yet been set. If it has been set, do nothing.
								{
									setAttribute(attributeValueType, mzzWoocommerceMap, mAttributeSetInstanceID, field, mapType,trxn);
								}
							}
						}
					}
				}
			}
			else
			{
				int m_AttributeSetInstance_ID = mBLDLineProductInstanceList.get(0).getM_AttributeSetInstance_ID();//Will be 0 if not yet created
				MAttributeSetInstance mAttributeSetInstance = new MAttributeSetInstance(ctx, m_AttributeSetInstance_ID, trxn);
				mAttributeSetInstance.saveEx();
				m_AttributeSetInstance_ID = mAttributeSetInstance.get_ID();//Get ID - will be different if was 0 before.
				setAttribute(attributeValueType, mzzWoocommerceMap, m_AttributeSetInstance_ID, field, mapType, trxn);
				mAttributeSetInstance.saveEx();
			} */
	    	}//End for(int l = 0; l < mzzWoocommerceMapLines.length; l++) loop
		   }//End for(MzzWoocommerceMap currentMapItem : currentMapList) loop
	}

	public void setAttribute(String attributeValueType, 
			MzzWoocommerceMapLine mzzWoocommerceMapLine, 
			int m_AttributeSetInstance_ID, 
			//LinkedHashMap<String, Object> field, 
			String valueOfField,
			String mapType,
			String trxn) {
		if(attributeValueType.equals("N"))//It's a number attribute
		{
			MAttributeInstance mAttributeInstance;
			//MAttributeInstance mAttributeInstance;
			if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE))
			{
				mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_Product_ID(), m_AttributeSetInstance_ID, Integer.parseInt(valueOfField), trxn);
			}
			else
			{
				
				mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_ID(), m_AttributeSetInstance_ID, Integer.parseInt(valueOfField), trxn);
			}
			
			mAttributeInstance.setValueInt(Integer.parseInt(valueOfField));
			mAttributeInstance.saveEx();
			/*MAttributeInstance (Properties ctx, int M_Attribute_ID, 
			int M_AttributeSetInstance_ID, int Value, String trxName)*/
		}
		else if(attributeValueType.equals("S"))//It's a string attribute
		{
			MAttributeInstance mAttributeInstance;
			if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE))
			{
				mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_Product_ID(), m_AttributeSetInstance_ID, valueOfField, trxn);
			}
			else
			{
				mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_ID(), m_AttributeSetInstance_ID, valueOfField, trxn);
			}
			
			mAttributeInstance.saveEx();
			/*MAttributeInstance (Properties ctx, int M_Attribute_ID, 
			int M_AttributeSetInstance_ID, String Value, String trxName)*/
		}
		else if(attributeValueType.equals("L"))//It's a list attribute
		{
			//TODO: This sets the value with the WooCOmmerce data. Will this cause unexpected results?
			MAttributeInstance mAttributeInstance;
			if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE))
			{
				 mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_Product_ID(), m_AttributeSetInstance_ID, mzzWoocommerceMapLine.getM_Attributevalue_Product_ID(), valueOfField, trxn);
			}
			else
			{
				 mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_ID(), m_AttributeSetInstance_ID, mzzWoocommerceMapLine.getM_AttributeValue_ID(), valueOfField, trxn);
			}
			mAttributeInstance.saveEx();
			/*MAttributeInstance(Properties ctx, int M_Attribute_ID, int M_AttributeSetInstance_ID,
			int M_AttributeValue_ID, String Value, String trxName)*/
		}
		else if(attributeValueType.equals("R"))//It's a Yes/No checkbox
		{
			//TODO: Implement 
		}
		MAttributeSetInstance m_AttributeSetInstance = new MAttributeSetInstance(ctx, m_AttributeSetInstance_ID, trxn);
		m_AttributeSetInstance.setDescription();
		m_AttributeSetInstance.save();
		//line.saveEx();
	}	

	/**Handles WooCommerce products that are intended 
	 * 
	 * @param mapType
	 * @param mzzWoocommerceMapLines
	 * @return
	 */
	public ArrayList<MzzWoocommerceMapLine> getMzzWooCommerceLineDuplicates(String mapType, ArrayList<MzzWoocommerceMapLine> mzzWoocommerceMapLines) {
		
		ArrayList<MzzWoocommerceMapLine> duplicatesToReturn = new ArrayList<MzzWoocommerceMapLine>();
		Map<Integer,Integer> rawAttributeMapLines = new HashMap<Integer,Integer>();
		for(MzzWoocommerceMapLine mapline : mzzWoocommerceMapLines)
		{
			int key = mapline.get_ID();
			int value = 0;
			if(mapType.equals(WOOCOMMERCE_MAP_TYPE_ATTRIBUTE) && mapline.getzz_woocommerce_map_type().equals(WOOCOMMERCE_MAP_TYPE_ATTRIBUTE)) 
			{
				value = mapline.getM_Attribute_ID();
			}
			else if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ADD)&& mapline.getzz_woocommerce_map_type().equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ADD))
			{
				value = mapline.getBLD_Product_PartType_ID();
			}
			else if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE)&& mapline.getzz_woocommerce_map_type().equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE))
			{
				value = mapline.getM_Attribute_Product_ID();
			}
			rawAttributeMapLines.put(key, value);
		}
			
		HashMap<Integer, List<Integer>> valueToKeyMapCounter = new HashMap<>();

        for (Entry<Integer, Integer> entry : rawAttributeMapLines.entrySet()) {
            if (valueToKeyMapCounter.containsKey(entry.getValue())) {
                valueToKeyMapCounter.get(entry.getValue()).add(entry.getKey());
            } else {
                List<Integer> keys = new ArrayList<>();
                keys.add(entry.getKey());
                valueToKeyMapCounter.put(entry.getValue(), keys);
            }
        }
        for (Map.Entry<Integer, List<Integer>> counterEntry : valueToKeyMapCounter.entrySet()) {
            if (counterEntry.getValue().size() > 1) 
            {
                if(counterEntry.getKey() > 0)
                {
	            	System.out.println("Duplicated Value:" + counterEntry.getKey() + " for Keys:" + counterEntry.getValue());
	            	ArrayList<Integer> MapLineIDs = (ArrayList<Integer>) counterEntry.getValue();
	                for(Integer id : MapLineIDs)
	                {
	                	duplicatesToReturn.add(new MzzWoocommerceMapLine(ctx, id, trxName));
	                }
                
                }
            }
        }
		return duplicatesToReturn;
	}
	/**
	 * 
	 * @param mapListMaster
	 * @param filteredMapLineListInProgress
	 * @param mapListInProgress
	 * @return
	 */
/*	private ArrayList<ArrayList<MzzWoocommerceMap>>  createOrderlineMapList(
			ArrayList<MzzWoocommerceMap> mapListMaster
			,ArrayList<ArrayList<MzzWoocommerceMapLine>> filteredMapLineListInProgress) {
		
		//Create a MzzWoocommerceMap based on the master list (mapList)
		ArrayList<ArrayList<MzzWoocommerceMap>> mapListInProgress = new ArrayList<ArrayList<MzzWoocommerceMap>>();
		ArrayList<MzzWoocommerceMap> orderLineMapToAdd = new ArrayList<MzzWoocommerceMap>(mapListMaster);
		//Loop through, check if any of the MzzWoocommerceMapLines - child records of the MzzWoocommerceMap object in the master list conflict.
		for(ArrayList<MzzWoocommerceMapLine> currentMapLineSubList : filteredMapLineListInProgress)
		{
			//for(MzzWoocommerceMapLine subListMapLine : currentMapLineSubList)
				for(int index = 0; index < currentMapLineSubList.size(); index ++)
			{
				//Remove from the copied list any records that have map lines with conflicting intentions
					int removeIndex = isThereConflict(orderLineMapToAdd, currentMapLineSubList.get(index));
				if(removeIndex > 0)
				{
					orderLineMapToAdd.remove(removeIndex);
				}
			}
				mapListInProgress.add(orderLineMapToAdd);
		}
		
		return mapListInProgress;
		
	}*/
	
	
	
	/**
	 * 
	 * @param mapListMaster
	 * @param filteredMapLineListInProgress
	 * @param mapListInProgress
	 * @return
	 */
	private ArrayList<MzzWoocommerceMap>  createFilteredMapList(ArrayList<MzzWoocommerceMap> mapListMaster
			,ArrayList<ArrayList<MzzWoocommerceMapLine>> filteredMapLineListInProgress) {
		
		//Create a MzzWoocommerceMap based on the master list (mapList)
		ArrayList<MzzWoocommerceMap> filteredMap = new ArrayList<MzzWoocommerceMap>(mapListMaster);
		//Loop through, check if any of the MzzWoocommerceMapLines - child records of the MzzWoocommerceMap object in the master list conflict.
		for(ArrayList<MzzWoocommerceMapLine> currentMapLineSubList : filteredMapLineListInProgress)
		{
			for(int index = 0; index < currentMapLineSubList.size(); index ++)
			{
				//Remove from the copied list any records that have map lines with conflicting intentions
					int removeIndex = isThereConflict(filteredMap, currentMapLineSubList.get(index));
				if(removeIndex > 0)
				{
					filteredMap.remove(removeIndex);
				}
			}
		}
		return filteredMap;	
	}
	
	
	
	/**
	 * 
	 * @param orderLineMapToCheck
	 * @param mapLine
	 * @return
	 */
	private int isThereConflict(ArrayList<MzzWoocommerceMap> orderLineMapToCheck, MzzWoocommerceMapLine mapLine) {
		for(int u = 0; u < orderLineMapToCheck.size(); u++)
		{
			/*There is conflict when
			 * The map type is a match AND
			 * The Attribute, Partype or Product Attribute are the same*/
			MzzWoocommerceMapLine[] mzzWoocommerceMapLines = orderLineMapToCheck.get(u).getMzzWoocommerceMapLines(orderLineMapToCheck.get(u).get_ID(), ctx, "" , "");
			for(int b = 0; b < mzzWoocommerceMapLines.length; b++)
			{
				String mapType = mzzWoocommerceMapLines[b].getzz_woocommerce_map_type();
				if(mapLine.getzz_woocommerce_map_type().equals(mapType))
				{
					if(mapType.equals(WOOCOMMERCE_MAP_TYPE_ATTRIBUTE))
					{
						if(mzzWoocommerceMapLines[b].getM_Attribute_ID() == mapLine.getM_Attribute_ID())
						{
							return u;
						}
					}
					else if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ADD))
					{
						if(mzzWoocommerceMapLines[b].getBLD_Product_PartType_ID() == mapLine.getBLD_Product_PartType_ID())
						{
							return u;
						}
					}
					else if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE))
					{
						if(mzzWoocommerceMapLines[b].getM_Attribute_Product_ID() == mapLine.getM_Attribute_Product_ID())
						{
							return u;
						}
					}
				}
			}
			//return false;
		}
		return 0;
	}
	
	public ArrayList<MzzWoocommerceMap> createdMapListFromMetaData(ArrayList<LinkedHashMap<String, Object>> metaData, int orderLineProductID) {
		ArrayList<MzzWoocommerceMap> masterZzWoocommerceMapListFromMeta = new ArrayList<>(); 
		fieldValues = new LinkedHashMap<Object, Object>();
		for (LinkedHashMap<String, Object> metaItem : metaData)
		{
			
			if(metaItem.get("key").equals("_wapf_meta"))
				 /*_wapf_meta contains the unique id of each field that
				  * can be matched to the backend product  */
			 {
				 LinkedHashMap<String, Object> wapfMeta = (LinkedHashMap<String, Object>) metaItem.get("value");
				 //Create a list of mapping object for this WC order line
				 for(Entry<String, Object> wapfMetaItem : wapfMeta.entrySet())
				 {
					 System.out.println(wapfMetaItem.getValue());
					 //Get a LinkedHashMap of all the fields and their IDs.
					 
					 LinkedHashMap<String, Object> fields = (LinkedHashMap<String, Object>) wapfMetaItem.getValue();
					 for(Map.Entry<String, Object> fieldItem : fields.entrySet())
					 { 
						try 
						{
							 //System.out.println(fieldItem.getValue());
							if(fieldItem.getValue().getClass().equals(LinkedHashMap.class)) 
							{                        
								 LinkedHashMap<String, Object> field = (LinkedHashMap<String, Object>) fieldItem.getValue();
									
								 MzzWoocommerceMap zzWoocommerceMap = MzzWoocommerceMap.getMzzWoocommerceMap(orderLineProductID,(String)field.get("id"), (String)field.get("value"), ctx);
								 if(zzWoocommerceMap == null)
								 {
									 StringBuilder msg = new StringBuilder("There was no mapping record found for Woocommerce field with ID: ");
									 msg.append(field.get("id"));
									 msg.append(" with label: ");
									 msg.append(field.get("label"));
									 msg.append(" with value: ");
									 msg.append(field.get("value"));
									 msg.append(" in product: ");
									 msg.append(MProduct.get(orderLineProductID).getName());
									 msg.append(" with WooCommerce name: ");
									 msg.append(wooCommProductName);
									 msg.append("and Woocommerce product ID: ");
									 msg.append(wooCommProductID);
									 msg.append(". Empty field mappings have been added automatically, please find them and update as needed. ");
									 msg.append(". Once the mappings are complete, delete order no. ");
									 msg.append(order.getDocumentNo());
									 msg.append(" and try running the Woocomerce sync process manually.");
									 msg.append("\n\n");
									 
									 log.warning(msg.toString());
									 
									 if(mapNotFound != null)
									 {
										 mapNotFound.append(msg);
									 }
									 else
									 {
										 mapNotFound = new StringBuilder(msg);
									 }
									 
									 zzWoocommerceMap = new MzzWoocommerceMap(ctx, 0, trxName);
									 zzWoocommerceMap.setM_Product_ID(orderLineProductID);
									 zzWoocommerceMap.setwoocommerce_field_key((String)field.get("id"));
									 if(field.get("value") != null) zzWoocommerceMap.setwoocommerce_field_value((String)field.get("value"));
									 StringBuilder description = new StringBuilder("Empty record auto created by WooCommerce Sync process.");
									 description.append(" Please either add appropriate mapping records (child records)");
									 description.append(" or check the 'Ignore No Child Records' box so the system does not throw an error on this record.");
									 zzWoocommerceMap.setDescription(description.toString());
									 zzWoocommerceMap.setAdd_To_Duplicate(false);
									 zzWoocommerceMap.setwoocommerce_field_label((String)field.get("label"));
									 zzWoocommerceMap.save(trxName);
									 
									 /*TODO: Create behaviour for unmatched WooCommerce fields as follows:
										 * 1. Create an empty StringBuilder field. DONE: private StringBuilder mapNotFound;
										 * 2. When a mapping record cannot be found, create the record in the database with no child records. DONE
										 * 3. Set the 'Ignore children' column to 'N'. DONE - that's it's default
										 * 4. Modify the above message to advise that the record has been added with empty child records, that the sync has failed and that the new record will need to have child records added 
										 * or the 'Ignore empty children' column set to 'Y'. Done
										 * 5.add the above message to the field.
										 * 5a. Modify the code that finds the mapping child records to throw an error when a field mapping is found, the children are empty and the 
										 * 'Ignore Children' field is set to 'N', send an email then throw an error.
										 * 6. Outside the loop, log the message, Email the message, then throw an error to stop the process.
										 * It's then up to the user to handle the further set up that is needed. What this does is at least do some of the setup
										 * by creating the empty records.
										 * 
										 * */
									 
								
									 
								 }
								 /*/Handle multi-select WooCommerce fields. These are fields where the user can select more than one option, like fabric.
								  * The call field.get("value") returns data like "Snow,Flint".
								  * with multiple colours selected. It is initially used for fabric sample products.
								  * Desired behaviour: created multiple orderlines for each colour.
								  * Throw error if it is more than can be currently handled?
								  */
								 
								/*Test to see if this mapping originates from a multi select field.
								 *TODO: If the record 'zz_woocommerce_multi_select_type' is set as 'Multi Select Parent'
								 * and the 'Ignore no child records' is 'Y' then the child records should be ignored, whether they exist or not.
								 * Currently the non-existent child records are loaded and throwing NPE???*/
								 String multiSelect = zzWoocommerceMap.getzz_woocommerce_m_select_type();
								 if(multiSelect != null && multiSelect.equalsIgnoreCase(WOOCOMMERCE_MAP_MULTISELECT_PARENT)) 
								 {
									 String fieldContents = ((String) field.get("value"));
									 String[] values = fieldContents.split(",");
									 for(int t =0; t < values.length; t++)
									 {
										 values[t] = values[t].trim();
									 }
									 for(int p = 0; p < values.length; p++)
									 {
										 MzzWoocommerceMap zzWoocommerceMapChild = MzzWoocommerceMap.getMzzWoocommerceMapChild(orderLineProductID,(String)field.get("id"), values[p], ctx);
										 if(zzWoocommerceMapChild.isActive())
										 {
											 masterZzWoocommerceMapListFromMeta.add(zzWoocommerceMapChild);
										 }
									 }
								 }
								 else
								 {
									 if(zzWoocommerceMap.isActive()) 
									{
										masterZzWoocommerceMapListFromMeta.add(zzWoocommerceMap); //Add all found mappings to List
									}
								 }
								
								 //Parse out the different options from the field values.
								 
								 //Create separate MzzWoocommerceMap records for each field value?
								 
								 
								
								 if(field.get("value") != null)fieldValues.put(zzWoocommerceMap.get_ID(), field.get("value"));
								 
								 System.out.println(field.get("id"));
								 System.out.println(field.get("label"));
								 System.out.println(field.get("value"));
								 //break;//There's no more useful stuff.
								 //processWooCommMeta(orderLine, field, orderLine.getM_Product_ID(), ctx, trxName);
							}
							
						}
						catch(java.lang.ClassCastException e)
						{
							System.out.println("Exception thrown.");
						}
					 }//End field mapping loop.
					 if(mapNotFound != null)//There were field mappings from WooCommerce not in the system.
					 {
						 int orgID = order.getAD_Org_ID();
						 MzzWoocommerce mzzWoocommerce = MzzWoocommerce.get(orgID, ctx, trxName);
						 String email = mzzWoocommerce.getnotify_email();
						 WcMailNotify.sendEmail(email, mapNotFound.toString(), "", ctx, trxName);//Email any issues found.
						 log.warning(mapNotFound.toString());
						 //throw new AdempiereUserError(mapNotFound.toString());//Commented out 12/11/24 to improve error handling
						 return null;//Throw a spanner in the works to trigger error
					 }
				 } 
			 }
		}
		return masterZzWoocommerceMapListFromMeta;
		
	}
	public int getOrderBpID() {
		return orderBpID;
	}

	public int getcOrderID() {
		return cOrderID;
	}

	public int getbPLocationId() {
		return bPLocationId;
	}

	public String getCustomerNote() {
		return customerNote;
	}
	
	public void setBldMtmInstallID(int bldMtmInstallID) {
		order.set_ValueOfColumn("bld_mtm_install_id", bldMtmInstallID);
		order.saveEx();
	}
	
	public void deleteOrder() {
		order.delete(true);
	}
}