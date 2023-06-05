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

import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_M_AttributeInstance;
import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
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
 * @author yogan naidoo
 */

public final class WcOrder {
	private final Properties ctx;
	private final String trxName;
	private final int POSTENDERTYPE_ID = 1000000;
	private final int POS_ORDER = 135;
	private static final String WOOCOMMERCE_MAP_TYPE_ATTRIBUTE = "10000003";
	private static final String WOOCOMMERCE_MAP_TYPE_PRODUCT_ADD = "10000004";
	private static final String WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE = "10000005";

	// private final int priceList_ID = 101;
	final String PAYMENT_RULE = "M";
	// final String PAYMENT_RULE = "P";
	private final MOrder order;
	private Boolean isTaxInclusive;
	private Boolean duplicateOrderLine;
	private ArrayList<LinkedHashMap> duplicateFields = null;
	private static CLogger log = CLogger.getCLogger(WcOrder.class);
	private PO wcDefaults;
	private ArrayList<ArrayList<MzzWoocommerceMapLine>> sortArrayAttributes;
	private ArrayList<ArrayList<MzzWoocommerceMapLine>> sortArrayProducts;

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
		throw new IllegalStateException("Order: " + order.getDocumentNo() + " Did not complete");
		/*
		
		order.setDateOrdered(new Timestamp(System.currentTimeMillis()));
		order.setDateAcct(new Timestamp(System.currentTimeMillis()));
		order.setDocAction(DocAction.ACTION_Complete);
		if (order.processIt(DocAction.ACTION_Complete)) {
			if (log.isLoggable(Level.FINE))
				log.fine("Order: " + order.getDocumentNo() + " completed fine");
		} else
			if (log.isLoggable(Level.FINE))
				log.fine("Order: " + order.getDocumentNo() + " did not complete");
		order.saveEx();//Comment out with line below to bypass is completed check.
			throw new IllegalStateException("Order: " + order.getDocumentNo() + " Did not complete");
			
			*/

		//order.saveEx(); Uncomment if 'throw new IllegalStateException("Order: " + order.getDocumentNo() + " Did not complete");' is commented out
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
		
		//Added by Phil Barnett 27/5/2023 -> process meta data for attributes and product options
		duplicateFields = null;
		duplicateOrderLine = false;
		ArrayList<LinkedHashMap<String,Object>> metaData = (ArrayList<LinkedHashMap<String, Object>>) line.get("meta_data");
		ArrayList<MzzWoocommerceMapLine> mzzWoocommerceMapLines = new ArrayList<MzzWoocommerceMapLine>();//Holds the found Mapping instructions for this WC orderline
		ArrayList<MzzWoocommerceMap> lineZzWoocommerceMapList = new ArrayList<>();//Holds Map records for this WC orderline
		
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
										
									 MzzWoocommerceMap zzWoocommerceMap = MzzWoocommerceMap.getMzzWoocommerceMap(orderLine.getM_Product_ID(),(String)field.get("id"), (String)field.get("value"), ctx);
									 lineZzWoocommerceMapList.add(zzWoocommerceMap); //Add all found mappings to List
									 
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
			}
						 
						 //Create MapLines (the actual instructions to create attributes, product options and product attributes), add to List
						 for(MzzWoocommerceMap zzWoocommerceMapItem : lineZzWoocommerceMapList)
						 {
							 MzzWoocommerceMapLine[] mzzWoocommerceMapLns = 
									 zzWoocommerceMapItem.getMzzWoocommerceMapLines(zzWoocommerceMapItem.get_ID(), ctx, "", "");
							 for(int ml = 0; ml < mzzWoocommerceMapLns.length; ml++)
							 {
								 mzzWoocommerceMapLines.add(mzzWoocommerceMapLns[ml]);
							 }
						 }
							//we now have all the MzzWoocommerceMapLines in an ArrayList.
							/*Find duplicate attributes - we want to find Attribute that are being created by more than one WooCommerce field.
							 * If we find one, we add it to a storage List
							 * For example, a line that is setting width twice.*/
						 
					
							ArrayList<MzzWoocommerceMapLine> duplicateAttributes = new ArrayList<MzzWoocommerceMapLine>();
							HashMap<Integer,Integer> filteredAttributeMapLines = new HashMap<Integer,Integer>();
							for(MzzWoocommerceMapLine mapline : mzzWoocommerceMapLines)
							{
								
								int key = mapline.getM_Attribute_ID();
								if(key > 0)//This mapline is mapping a WC field to BLDParttype
								{
									int value = mapline.getZZ_Woocommerce_Map_ID();
									System.out.println("Key: " + key + " Value: " + value);
									if(filteredAttributeMapLines.put(key, value)!=null)//pair is already in HashMap
									{
										duplicateAttributes.add(mapline);
									}
								}
							}
							
							//Find duplicate Part adds - we want to find BldMParttypes that are being created by more than oneWooCommerce field. If we find one, we add it to a storage List
							ArrayList<MzzWoocommerceMapLine> duplicateBldPartTypes = new ArrayList<MzzWoocommerceMapLine>();
							HashMap<Integer,Integer> filteredPartMapLines = new HashMap<Integer,Integer>();
							for(MzzWoocommerceMapLine mapline : mzzWoocommerceMapLines)
							{
								
								int key = mapline.getBLD_Product_PartType_ID();
								System.out.println(mapline.getzz_woocommerce_map_type());
								if(key > 0 && mapline.getzz_woocommerce_map_type().equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ADD))//This mapline is mapping a WC field to BLDParttype
								{
									int value = mapline.getZZ_Woocommerce_Map_ID();
									System.out.println("Key: " + key + " Value: " + value);
									if(filteredPartMapLines.put(key, value)!=null)//pair is already in HashMap
									{
										duplicateBldPartTypes.add(mapline);
									}
								}
							}
							/*Find duplicate product attribute creation. We want to fid product attributes that are being set by differnt WC fields,
							 * For example fabric colour being set by more than one WC field.
							 * The opening if() statement assumes there will only be duplicate product attribute adds if there are duplicate parttype adds.
							 */
							HashMap<Integer,Integer> filteredProductAttributes = new HashMap<Integer,Integer>();
							ArrayList<MzzWoocommerceMapLine> duplicateProductAttributes = new ArrayList<MzzWoocommerceMapLine>();
							if(duplicateBldPartTypes.size()>0)
							{
								for(MzzWoocommerceMapLine mapline : mzzWoocommerceMapLines)
								{
									
									int key = mapline.getM_Attribute_Product_ID();
									if(key > 0 && mapline.getzz_woocommerce_map_type().equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE))//This mapline is mapping a WC field to BLDParttype
									{
										int value = mapline.getZZ_Woocommerce_Map_ID();
										System.out.println("Key: " + key + " Value: " + value);
										if(filteredProductAttributes.put(key, value)!=null)//pair is already in HashMap
										{
											duplicateProductAttributes.add(mapline);
										}
									}
								}
							}
						 
						 /*We now have 3 lists of possible duplicate WC mapped fields.
						  * Create Lists of MzzWoocommerceMapLines to create the MOrderLines.
						  * The operation to create the duplicate MzzWoocommerceMap Lists leaves
						  * There will NumberOfUniqueDuplicateAttributes X UniqueDuplicatePartTypes -> 3 widths, 2 fabrics -> 6 Orderlines*/
							sortArrayAttributes = new ArrayList<ArrayList<MzzWoocommerceMapLine>>();
							//sortArrayProducts = new ArrayList<ArrayList<MzzWoocommerceMapLine>>();
							//processRepeatingFields(duplicateAttributes);
							processRepeatingFields(duplicateBldPartTypes);
							processRepeatingFields(duplicateProductAttributes);
						 
						 //System.out.println(values.toString());
			
				
		MAttributeSetInstance	lineAttSetIns = new MAttributeSetInstance(ctx, orderLine.getM_AttributeSetInstance_ID(), trxName);	
		lineAttSetIns.setDescription();
		lineAttSetIns.save();
		MBLDLineProductSetInstance lineBldInstance = new MBLDLineProductSetInstance(ctx, orderLine.get_ValueAsInt("bld_line_productsetinstance_id"), trxName);
		lineBldInstance.setDescription(orderLine.getM_Product_ID());
		lineBldInstance.save();
		if(duplicateOrderLine)
		{
			createMultipleProduct(orderLine, trxName);
		}
		duplicateOrderLine = false;
		duplicateFields = null;
		//Added by Phil
		
		if (!orderLine.save()) {
			throw new IllegalStateException("Could not create Order Line");
		}
	}
	
	public /*ArrayList<?>*/ void processRepeatingFields(ArrayList<MzzWoocommerceMapLine> duplicates) {
		
		for(MzzWoocommerceMapLine mzzWoocommerceMapLine : duplicates)
		{
			//create sublist
			//Add the first element of duplicates to the list
			//increment counter variable
			//Move to next element in duplicates. Is it the same object no -> is it setting the same attribute, product or product attribute as the previous?
			//If yes, create new arrayList and add the element to it.
			//
			if(sortArrayAttributes.size() < 1)//initialise with the first element of the duplicate list
			{
				ArrayList<MzzWoocommerceMapLine> firstEntry = new ArrayList<MzzWoocommerceMapLine>();
				firstEntry.add(mzzWoocommerceMapLine);
				sortArrayAttributes.add(firstEntry);
			}
			sortArrayAttributes = sortThisMess(sortArrayAttributes, mzzWoocommerceMapLine);
			//ArrayList<MzzWoocommerceMapLine> subList = new ArrayList<MzzWoocommerceMapLine>();
			//subList.add(mzzWoocommerceMapLine);
		}
		
		
		//return sortArray;
	}
	
	public ArrayList<ArrayList<MzzWoocommerceMapLine>> sortThisMess(ArrayList<ArrayList<MzzWoocommerceMapLine>> mess, MzzWoocommerceMapLine zzzWoocommerceMapLine) {
		//Is the mzzWoocommerceMapLine already on the list?
		//for(ArrayList<MzzWoocommerceMapLine> mapArrayList : mess)
		boolean exit = false;
		for(int xx = 0; xx < mess.size(); xx++)
		{
			//for(MzzWoocommerceMapLine mzzLineToExamine : mapArrayList)
				//if(exit) break;
				for(int v =0; v < mess.get(xx).size(); v++)
			{
				//Is this the same element?
				if(!(mess.get(xx).get(v).get_ID() == zzzWoocommerceMapLine.get_ID()))
				{
					//Is it the same maptype?
					if(mess.get(xx).get(v).getzz_woocommerce_map_type().equals(zzzWoocommerceMapLine.getzz_woocommerce_map_type()))
					{//The above if stops any maptypes aprt from the one used to initialise the map
						//Is it setting the same stuff?
						switch(mess.get(xx).get(v).getzz_woocommerce_map_type()) {
						case WOOCOMMERCE_MAP_TYPE_ATTRIBUTE:
							//is it setting the same Attribute?
							if(mess.get(xx).get(v).getM_Attribute_ID() == zzzWoocommerceMapLine.getM_Attribute_ID())
							{
								//Add it to the mess in new ArrayList
								ArrayList<MzzWoocommerceMapLine> addedzzMapLineList = new ArrayList<MzzWoocommerceMapLine>();
								addedzzMapLineList.add(zzzWoocommerceMapLine);
								mess.add(addedzzMapLineList);
							}
							else
							{
								//Add to current list
								mess.get(xx).add(zzzWoocommerceMapLine);
							}
							return mess;
							//break;
						case WOOCOMMERCE_MAP_TYPE_PRODUCT_ADD:
							//Is it setting the same producttype?
							if(mess.get(xx).get(v).getBLD_Product_PartType_ID() == zzzWoocommerceMapLine.getBLD_Product_PartType_ID())
							{
								//Add it to the mess in new ArrayList
								ArrayList<MzzWoocommerceMapLine> addedzzMapLineList = new ArrayList<MzzWoocommerceMapLine>();
								addedzzMapLineList.add(zzzWoocommerceMapLine);
								mess.add(addedzzMapLineList);
							}
							else
							{
								//Add to current list
								mess.get(xx).add(zzzWoocommerceMapLine);
							}
							return mess;
							//break;
							
						case WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE:
							//Is it setting the same product attribute, EG fabric colour?
							if(mess.get(xx).get(v).getM_Attribute_Product_ID() == zzzWoocommerceMapLine.getM_Attribute_Product_ID())
							{
								///Add it to the mess in new ArrayList
								ArrayList<MzzWoocommerceMapLine> addedzzMapLineList = new ArrayList<MzzWoocommerceMapLine>();
								addedzzMapLineList.add(zzzWoocommerceMapLine);
								mess.add(addedzzMapLineList);
							}
							else
							{
								//Add to current list
								mess.get(xx).add(zzzWoocommerceMapLine);
							}
							return mess;
							default:
							
								//Add to current list
								mess.get(xx).add(zzzWoocommerceMapLine);
								return mess;
							//break;
							
						}
					}
				}
				//mess.get(xx).add(zzzWoocommerceMapLine);//Add to current list
			}
		}
		
		return mess;
		
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
	
	/**
	 * 
	 * @param line
	 * @param field
	 * @param mProductID
	 * @param ctx
	 */
	public void processWooCommMeta(MOrderLine line, LinkedHashMap<String, Object> field, int mProductID, Properties ctx, String trxn) {
		//Get field data
		String fieldID = (String) field.get("id");
		//String fieldLabel = (String)field.get("label");
		String fieldValue = (String) field.get("value");
		
		
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
		
		//Process field data
		MzzWoocommerceMap mzzWoocommerceMapMatch = MzzWoocommerceMap.getMzzWoocommerceMap(mProductID, fieldID, fieldValue, ctx);
		//System.out.println(mzzWoocommerceMapMatch.getzz_woocommerce_map_type());
		if(mzzWoocommerceMapMatch == null)
			{
				throw new AdempiereUserError("No mapping found for WooCommerce fieldID " + fieldID + ". Check mapping on product: " + MProduct.get(mProductID).getName());
			}
				String whereClause = "and "+ MzzWoocommerceMapLine.COLUMNNAME_IsActive + "='Y'";
				MzzWoocommerceMapLine[] mzzWoocommerceMapLines = mzzWoocommerceMapMatch.getMzzWoocommerceMapLines(mzzWoocommerceMapMatch.get_ID(), ctx, "", whereClause);
		
		for(int l = 0; l < mzzWoocommerceMapLines.length; l++)
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
					setAttribute(attributeValueType, mzzWoocommerceMapLines[l], m_AttributeSetInstance_ID, field, mapType,trxn);
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
		}
		
		else if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE))
		{
			//Get the MBLDLineProductInstance. This relies on the products being added to the MBLDLineProductInstance before this code runs.
			/*How to handle multiple cases of bld_line_productsetinstances containing multiple records of the same product?
			 * Check existence of m_attributesetinstance_id in bld_line_productinstance -> if it does not exist then create and add attribute & value
			 * If m_attributesetinstance_id in bld_line_productinstance > 0 then it already exists -> check if the attribute value has been set
			 * Ifattribute has not been set, then set it.
			 * Get the */
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
					int c = mzzWoocommerceMapLines[l].getM_AttributeValue_ID();
					mAttributeSetInstance.setM_AttributeSet_ID(MProduct.get(mzzWoocommerceMapLines[l].getM_Product_Line_ID()).getM_AttributeSet_ID());
					mAttributeSetInstance.save();
					MAttributeValue mapAttributeValue = new MAttributeValue(ctx, mzzWoocommerceMapLines[l].getM_Attribute_Product_ID(), trxn);
					MAttribute mapAttribute = new MAttribute(ctx, mapAttributeValue.getM_Attribute_ID(), trxn);
					setAttribute(mapAttribute.getAttributeValueType(), mzzWoocommerceMapLines[l], mAttributeSetInstanceID, field, mapType, trxn);
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
		} 
		
	}

	public void setAttribute(String attributeValueType, 
			MzzWoocommerceMapLine mzzWoocommerceMapLine, 
			int m_AttributeSetInstance_ID, 
			LinkedHashMap<String, Object> field, 
			String mapType,
			String trxn) {
		if(attributeValueType.equals("N"))//It's a number attribute
		{
			MAttributeInstance mAttributeInstance;
			//MAttributeInstance mAttributeInstance;
			if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE))
			{
				mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_Product_ID(), m_AttributeSetInstance_ID, Integer.parseInt((String)field.get("value")), trxn);
			}
			else
			{
				mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_ID(), m_AttributeSetInstance_ID, Integer.parseInt((String)field.get("value")), trxn);
			}
			
			mAttributeInstance.setValueInt(Integer.parseInt((String)field.get("value")));
			mAttributeInstance.saveEx();
			/*MAttributeInstance (Properties ctx, int M_Attribute_ID, 
			int M_AttributeSetInstance_ID, int Value, String trxName)*/
		}
		else if(attributeValueType.equals("S"))//It's a string attribute
		{
			MAttributeInstance mAttributeInstance;
			if(mapType.equals(WOOCOMMERCE_MAP_TYPE_PRODUCT_ATTRIBUTE))
			{
				mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_Product_ID(), m_AttributeSetInstance_ID, (String) field.get("value"), trxn);
			}
			else
			{
				mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_ID(), m_AttributeSetInstance_ID, (String) field.get("value"), trxn);
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
				 mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_Product_ID(), m_AttributeSetInstance_ID, mzzWoocommerceMapLine.getM_Attributevalue_Product_ID(), (String) field.get("value"), trxn);
			}
			else
			{
				 mAttributeInstance = new MAttributeInstance(ctx, mzzWoocommerceMapLine.getM_Attribute_ID(), m_AttributeSetInstance_ID, mzzWoocommerceMapLine.getM_AttributeValue_ID(), (String) field.get("value"), trxn);
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
	
	public void createMultipleProduct(MOrderLine line, String trxn /*, List<?> changeItems*/) {
		MOrderLine duplicateOrderLine = new MOrderLine(order);
		MOrderLine.copyValues(line, duplicateOrderLine);
		//MAttributeSetInstance duplicateMAttributeSetInstance = new MAttributeSetInstance(ctx, 0 ,trxn);
		//duplicateMAttributeSetInstance.save();
		//duplicateOrderLine.setM_AttributeSetInstance_ID(duplicateMAttributeSetInstance.get_ID());
		duplicateOrderLine.save();
		for(LinkedHashMap<String, Object> duplicateField : duplicateFields)
		{
			processWooCommMeta(duplicateOrderLine, duplicateField, duplicateOrderLine.getM_Product_ID(), ctx, trxn);
		}
		duplicateOrderLine.save();
		
		//Copy order line just created.
		//Change items - could have the meta objects to set as different?
		/*Perhaps examine the WooCommerce meta to determine the duplicated parttypes to determine what should be different in
		 the second product? EG 2 fabric = dual blind or curtain, with different fabric? Chain & motor means a roller with a chain
		 and a roller with a motor?
		 Create a maptype add to dual? create dual?
		 How to dual linked blinds? -> same a above?
		 Attributes - should overwrite themselves, products -> fetch the BLDproductSetInstance and replace product by parttype?*/
		//What has to change? for dual products, it may be the fabric, roll type, curtain position
	}
	
}