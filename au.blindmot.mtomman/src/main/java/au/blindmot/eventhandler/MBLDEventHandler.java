package au.blindmot.eventhandler;



import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.sql.RowSet;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MBPartner;
import org.compiere.model.MDiscountSchema;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductionLine;
import org.compiere.model.PO;
import org.compiere.model.X_C_OrderLine;
import org.compiere.model.X_M_ProductionLine;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDLineProductSetInstance;
import au.blindmot.model.MBLDMtomItemLine;
import au.blindmot.model.MBLDMtomProduction;
import au.blindmot.model.MBLDProductPartType;
import au.blindmot.model.X_BLD_Line_ProductSetInstance;
import au.blindmot.utils.MtmUtils;

public class MBLDEventHandler extends AbstractEventHandler {

	private static final String COLUMN_GROSS_MARGIN = "grossmargin";
	private CLogger log = CLogger.getCLogger(MBLDEventHandler.class);
	private X_M_ProductionLine mProductionLine = null;
	private BLDMOrderLine bMorderLine = null;
	private MOrderLine orderLine = null;
	private MBLDMtomItemLine mToMProductionParent = null;
	private int prevOrderLineID = 0;
	private boolean DocVoidReverse = false;
	private MOrder parentOrder = null;
	String trxName = null;
	
	@Override
	protected void initialize() {
		//register EventTopics and TableNames   
		//TODO: Add registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MOrder.Table_Name); and add code to warn of uncheckmeasured MtoM items.
				registerTableEvent(IEventTopics.PO_AFTER_NEW, MProductionLine.Table_Name); 
				registerTableEvent(IEventTopics.PO_POST_UPADTE, MProductionLine.Table_Name); 
				registerTableEvent(IEventTopics.PO_AFTER_CHANGE, MProductionLine.Table_Name);
				registerTableEvent(IEventTopics.DOC_BEFORE_REVERSECORRECT, MBLDMtomProduction.Table_Name);
				registerTableEvent(IEventTopics.DOC_BEFORE_REVERSEACCRUAL, MBLDMtomProduction.Table_Name);
				//registerTableEvent(IEventTopics.PO_BEFORE_NEW, MOrderLine.Table_Name);//
				registerTableEvent(IEventTopics.PO_POST_CREATE, MOrderLine.Table_Name);
				registerTableEvent(IEventTopics.PO_AFTER_NEW, MOrderLine.Table_Name);//PO to copy MAttributeSetInstance to
				registerTableEvent(IEventTopics.PO_AFTER_CHANGE, MOrderLine.Table_Name);
				log.info("----------<MBLDEventHandler> .. IS NOW INITIALIZED");
				}
	
	@Override
	protected void doHandleEvent(Event event) {
	
	PO po = getPO(event);
	trxName = po.get_TrxName();
	MOrderLine copyFromOrderLine = null;
	
	if(po instanceof MProductionLine && po != null)
		{
			if(!DocVoidReverse) return;
		
			mProductionLine = (X_M_ProductionLine)po;
			log.warning("---------MProductionLine event triggered");
			log.warning("---------event: " + event);
			log.warning("Event topic: " + event.getTopic());
			log.warning("Event property error: " + event.getProperty("event.errorMessages"));
			log.warning("---------PO: " + po.toString());
			
			/*
			ArrayList<String> errors = (ArrayList<String>) event.getProperty("event.errorMessages");
			log.warning("Errors: " + errors.toString());
			if(errors.contains("java.lang.NullPointerException")) return;
			*/
			//handleMProductionLineEvent(po);//Handle all events with the same method
		}
	
	if(po instanceof MBLDMtomProduction && po != null)//The parent production is being voided or reversed.
	{
		DocVoidReverse = true;	
	}
	
	if(po instanceof MOrderLine && po != null)//We have an orderline
	{
		log.warning("---------MOrderLine event triggered");
		log.warning("---------event: " + event);
		trxName = po.get_TrxName();
		//po.save();
		System.out.println(Env.getCtx().toString());
		
		
		bMorderLine = new BLDMOrderLine(Env.getCtx(), po.get_ID(), trxName);//The new OrderLine to copy the attribute instances to.
		orderLine = new MOrderLine(Env.getCtx(), po.get_ID(), trxName);//The new OrderLine to copy the attribute instances to.
		log.warning("---------Line 113");
		log.warning("---------orderLine.getM_AttributeSetInstance_ID(): " + orderLine.getM_AttributeSetInstance_ID());
		
		//Attempt to exit if it's Purchase Order
		if(!parentIsSalesOrder())
		{
			return;
		}
		
		if(event.getTopic().equalsIgnoreCase(IEventTopics.PO_AFTER_NEW))//new record is saved.
		{
			//Check if there's a BLD Line ProductSetInstance; if none, create and set.
			int mProductID = orderLine.getM_Product_ID();
			int lineCopyID = orderLine.get_ValueAsInt("copypk");//
			prevOrderLineID = lineCopyID;
			bMorderLine.setPrevMLineOrderLineID(lineCopyID);
			
			MProduct currOrderLineProduct;
			boolean isMadeToMeasure = false;
			if (mProductID > 0) //Orderline has a product
				{
					currOrderLineProduct = new MProduct(Env.getCtx(), mProductID , trxName);
					isMadeToMeasure = currOrderLineProduct .get_ValueAsBoolean("ismadetomeasure");
				}
			
			log.warning("-------In MBLDEventHandler.dohandleevent -> orderLine.get_Value(bld_line_productsetinstance_id) = " + orderLine.get_Value("bld_line_productsetinstance_id"));
			if(orderLine.get_Value("bld_line_productsetinstance_id") == null && isMadeToMeasure)
				//User didn't set bLDLineProductSetInstanceID - make sure there is an orderline value for bld_line_productsetinstance_id
				{
					setBldLineProductSetInstanceID();
				}
			
			//Set calculated costs
			//if(isMadeToMeasure && (event.getTopic().equalsIgnoreCase("adempiere/po/afterNew") || event.getTopic().equalsIgnoreCase("adempiere/po/beforeNew")))
			
			
			
			log.warning("---------orderLine.getM_AttributeSetInstance_ID(): " + orderLine.getM_AttributeSetInstance_ID());
			
			if(orderLine.get_Value("copypk") == null)//It's a new record.
				{
					setCopyPK();
				}
			
			int orderLineID = orderLine.get_ID();
			if(orderLine.getM_AttributeSetInstance_ID() > 0 && lineCopyID == orderLineID)
				{
					//There's an MAttributeSet save and if lineCopyID == orderlineID then we're not copying a line.
					return;//Everything is OK.
				}
			
			log.warning("---------Line 170");
			MProduct mProduct = new MProduct(Env.getCtx(), orderLine.getM_Product_ID(), trxName);
			if(mProduct.get_ValueAsBoolean("ismadetomeasure"))
				{
				//
				copyFromOrderLine = copyAttributeInstance(orderLine, mProduct);//returns as null if fails
				BigDecimal price = Env.ZERO;
				
				Properties pCtx = Env.getCtx();
				MOrder order = new MOrder(pCtx, orderLine.getC_Order_ID(), trxName);
				MOrderLine line = (MOrderLine)po;
				BigDecimal area = null;
				int M_PriceList_ID = order.getM_PriceList_ID();
				boolean iSsalesTrx = order.isSOTrx();
				
				int m_AttributeSetInstance_ID = line.getM_AttributeSetInstance_ID();
				BigDecimal[] l_by_w = null;
				if(m_AttributeSetInstance_ID > 0)
				{
					l_by_w = MtmUtils.getLengthAndWidth(line.getM_AttributeSetInstance_ID());
				}
				
				
				if(copyFromOrderLine != null)//Then it's a copied record.
					{
					//Copy the Bldproduct set from old record to new.
					copyBldProductInstance(copyFromOrderLine.get_ValueAsInt("bld_line_productsetinstance_id"),  orderLine.get_ValueAsInt("bld_line_productsetinstance_id"), mProductID);
					System.out.println(copyFromOrderLine.get_Value("mtm_attribute"));
					orderLine.setLineNetAmt(copyFromOrderLine.getLineNetAmt());
					orderLine.saveEx();
					
					if(mProduct.get_ValueAsBoolean("isgridprice"))//We have a copied record that's a grid price
						{
						ArrayList<Integer> sellProductIDsCheck = MtmUtils.getMTMPriceProductIDs(Env.getCtx(), copyFromOrderLine);
						ArrayList<Integer> costProductIDsCheck = MtmUtils.getMTMSelectableCostProductIDs(Env.getCtx(), copyFromOrderLine);
							
						if(l_by_w != null)
						{
							area = (l_by_w[0].multiply(l_by_w[1]).divide(new BigDecimal(1000000)));
						}
						else
						{
							area = Env.ZERO;
						}
							
							BigDecimal calculatedCosts = MtmUtils.getCalculatedLineCosts(iSsalesTrx, area, sellProductIDsCheck , pCtx, copyFromOrderLine, trxName, 0);
							for(Integer num : costProductIDsCheck)
							{
								price = price.add(MtmUtils.getListPrice(num, M_PriceList_ID, pCtx, line, 0, l_by_w, iSsalesTrx));
							}
						
							BigDecimal discount = copyFromOrderLine.getDiscount();
							BigDecimal priceActual = BigDecimal.valueOf((100.0 - discount.doubleValue()) / 100.0 * price.doubleValue());
							line.setPriceActual(priceActual);
							//line.setPriceActual(price);
							line.setPriceEntered(priceActual);
							line.setPriceLimit(price.divide(Env.ONEHUNDRED));//Hard coded, fix at some point.;
							line.setPriceList(price);
							line.set_ValueOfColumn("calculated_cost", calculatedCosts);
							line.saveEx();
						}
					}
					else if(mProduct.get_ValueAsBoolean("isgridprice") && copyFromOrderLine == null)//No copied record
					{
						ArrayList<Integer> sellProductIDsCheck = MtmUtils.getMTMPriceProductIDs(Env.getCtx(), line);
						if(l_by_w != null)//
						{
							area = (l_by_w[0].multiply(l_by_w[1]).divide(new BigDecimal(1000000)));
						}
						else
						{
							area = Env.ZERO;
						}
						
						BigDecimal calculatedCosts = MtmUtils.getCalculatedLineCosts(iSsalesTrx, area, sellProductIDsCheck , pCtx, line, trxName, 0);
						for(Integer num : sellProductIDsCheck)
						{
							price = price.add(MtmUtils.getListPrice(num, M_PriceList_ID, pCtx, line, 0, l_by_w, iSsalesTrx));
						}
						
						BigDecimal discount = line.getDiscount();
						BigDecimal priceActual = BigDecimal.valueOf((100.0 - discount.doubleValue()) / 100.0 * price.doubleValue());
						line.setPriceActual(priceActual);
						line.setPriceEntered(priceActual);
						line.setPriceLimit(price.divide(Env.ONEHUNDRED));//Hard coded, fix at some point.;
						line.setPriceList(price);
						line.set_ValueOfColumn("calculated_cost", calculatedCosts);
						line.saveEx();
					}
						//Set qty field 
						int mAttributeInstanceID = orderLine.getM_AttributeSetInstance_ID();
						System.out.println("orderLine M_AttributeSetInstance_ID: " + mAttributeInstanceID);
						if(mAttributeInstanceID > 0)
							{
							
							if(copyFromOrderLine != null)
							{
								l_by_w = MtmUtils.getLengthAndWidth(copyFromOrderLine.getM_AttributeSetInstance_ID());
							}
							
							if(!mProduct.get_ValueAsBoolean("isgridprice"))//Don't set the qtyentered for grid price items
							{ 
								
								if(l_by_w != null)
								{
									BigDecimal area1 = l_by_w[0].multiply(l_by_w[1]).setScale(2);
									System.out.println(area1);	
									orderLine.setQtyEntered(area1);	
								}
							}
							
							if(l_by_w == null)//Check if it has length only
								{
									BigDecimal length = MtmUtils.hasLength((int)mAttributeInstanceID).setScale(2, BigDecimal.ROUND_HALF_EVEN);
									if(length != Env.ZERO.setScale(2))
										{
											orderLine.setQtyEntered(length);
										}
									}
								}
						}
			
			if(!parentOrder.isSOTrx())
			{
				log.warning("Parent Order is a purchase order; exiting MBLDEventHandler");
				bMorderLine.save(trxName);
				parentOrder.saveEx(trxName);
				
				return;//We don't want to mess with Purchase orders.
			}
				
				//set MTM discount here
				/*
				 * If it's a BP flat discount, get it and use it, if it's not, get 
				 * MDiscountSchema flat discount and set it.
				 */
				
				BigDecimal flatDiscount = MtmUtils.calculateDiscount(orderLine, mProductID);
				bMorderLine.setDiscount(flatDiscount);
				log.warning("-set_ValueOfColumn---line 218-----orderLine.get_ID: " + orderLine.get_ID());
				
		
				bMorderLine.set_ValueOfColumn("copypk", orderLine.get_ID());
				bMorderLine.save(trxName);
	}//if(event.....PO_AFTER_NEW)
			
			
			//This code runs by default for all events...
			if(copyFromOrderLine != null)//It's a copied line; ensures that on copied lines, any user set discount is retained.
			{
				setDiscount(copyFromOrderLine, bMorderLine);
				log.warning("BMorderLine discount: " + bMorderLine.getDiscount());
				bMorderLine.beforeSave(po.is_new());
				BigDecimal priceActual = bMorderLine.getPriceList().subtract(bMorderLine.getDiscount().divide(Env.ONEHUNDRED).multiply(bMorderLine.getPriceList()));
			//	BMorderLine.setPrice(priceActual.setScale(2, BigDecimal.ROUND_HALF_UP));
				bMorderLine.setPrice(copyFromOrderLine.getPriceActual());
				//BMorderLine.setLineNetAmt(BMorderLine.getQtyEntered().multiply(priceActual).setScale(2, BigDecimal.ROUND_HALF_UP));
				bMorderLine.setLineNetAmt(copyFromOrderLine.getLineNetAmt());
				bMorderLine.setPriceList(copyFromOrderLine.getPriceList());
				bMorderLine.save(trxName);
				copyFromOrderLine.save(trxName);
			}
			
			if(event.getTopic().equalsIgnoreCase(IEventTopics.PO_AFTER_NEW) || event.getTopic().equalsIgnoreCase(IEventTopics.PO_AFTER_CHANGE))
			{
				//Update gross margin
				//Calculate Gross margin... totallines - total cost / totallines
				StringBuilder sql = new StringBuilder("SELECT SUM(c_orderline.calculated_cost) ");
				sql.append("FROM c_orderline ");
				sql.append("WHERE c_orderline.c_order_id = ?");
				int cOrderID = orderLine.getC_Order_ID();
				MOrder parentOrder = new MOrder(Env.getCtx(), cOrderID, trxName);
				
				if(!parentOrder.isSOTrx())
				{
					log.warning("Parent Order is a purchase order; exiting MBLDEventHandler");
					return;//We don't want to mess with Purchase orders.
				}
				
				String cost = DB.getSQLValueString(trxName, sql.toString(), cOrderID);
				if(cost == null)
				{
					log.warning("Cost can't be determined; exiting MBLDEventHandler");
					return;
				}
				BigDecimal bigValue = new BigDecimal(parentOrder.get_Value(MOrder.COLUMNNAME_TotalLines).toString());
				BigDecimal bigCost = new BigDecimal(cost);
				BigDecimal grossMargin =  bigValue.subtract(bigCost).divide(bigValue, 2, RoundingMode.HALF_UP).multiply(Env.ONEHUNDRED);
				//setField(grossMargin, mTab, COLUMN_GROSS_MARGIN);
				parentOrder.set_ValueNoCheck(COLUMN_GROSS_MARGIN, grossMargin);
				parentOrder.saveEx();
			}
			return;
		}
		
	}

	
	private void setCopyPK() {
		//Set the value of the 'copypk' column so we know where to copy from.
		orderLine.set_ValueOfColumn("copypk", orderLine.get_ID());
		log.warning("-------Setting column copypk with value: " + orderLine.get_ID());
		orderLine.saveEx(trxName);
	}

	private void setBldLineProductSetInstanceID() {
		X_BLD_Line_ProductSetInstance xBLDProdSetIns = new X_BLD_Line_ProductSetInstance(Env.getCtx(), 0, trxName);
		xBLDProdSetIns.saveEx(trxName);
		orderLine.set_ValueNoCheck("bld_line_productsetinstance_id", xBLDProdSetIns.get_ID());
		orderLine.save(trxName);
	}

	private boolean parentIsSalesOrder() {
		parentOrder = new MOrder(Env.getCtx(), orderLine.getC_Order_ID(), trxName);
		log.warning("Parent Order is a purchase order check....");
		if(parentOrder.isSOTrx())
			{
				log.warning("Parent Order is a sales order, continuing....");
				return true;//We don't want to mess with Purchase orders.
			}
		log.warning("Parent Order is a purchase order; exiting MBLDEventHandler");
		
		return false;
	}

	/**
	 * Listens for and overrides the MProductionLine.beforeSave() method which n
	 * sets the MBLDMtomItemLine child productionlines to 0 for the end product.
	 * @param pobj
	 */
	private void handleMProductionLineEvent(PO pobj) {
		
		log.warning("---------- In handleMProductionLineEvent");
		String isEndProduct = "N";
		BigDecimal movementQty = BigDecimal.ZERO;
		
		log.warning("---------getmBLDMtomItemLineID() = " + getmBLDMtomItemLineID());
		
		if(!(getmBLDMtomItemLineID() > 0))//It's not an MTMProduction
		{
			log.warning("---------Not an MTM product");
			return;
		}
		
		
		if (getmBLDMtomItemLineID() > 0)//What does this actually test for?
		{
			log.warning("getmBLDMtomItemLineID() is > 0.");
			mToMProductionParent = new MBLDMtomItemLine(pobj.getCtx(), getmBLDMtomItemLineID(), pobj.get_TrxName());
		}
		/*	
		log.warning("~Line 209 ");
		//Lines below left in for future debugging convenience, commented out for production.
		//system.out.println(mProductionLine.getM_Product_ID() && mToMProductionParent.getProductionQty().signum() == mProductionLine.getMovementQty().signum());
		log.warning("mToMProductionParent.getM_Product_ID(): " + mToMProductionParent.getM_Product_ID());
		log.warning("mProductionLine.getM_Product_ID(): " + mProductionLine.getM_Product_ID());
		log.warning("mToMProductionParent.getProductionQty().signum(): " + mToMProductionParent.getProductionQty().signum());
		log.warning("mProductionLine.getMovementQty().signum()" + mProductionLine.getMovementQty().signum());
		//
		
		*/
		
		if (getmBLDMtomItemLineID() > 0) 
		{
			int parentProductId = mToMProductionParent.getM_Product_ID();
			int mProductionLineProductID = mProductionLine.getM_Product_ID();
			log.warning("parentProductId = " + parentProductId + " mProductionLineProductID = " + mProductionLineProductID);
			
			
			if (parentProductId == mProductionLineProductID)//It's an end product
			{
				isEndProduct = "Y";
				log.warning("Is end product: " + isEndProduct);
				movementQty = BigDecimal.ONE;
				log.warning("----------movementQty rnd 1: " + movementQty);
				if(DocVoidReverse)
					{
						movementQty = movementQty.negate();
						log.warning("----------movementQty rnd 2: " + movementQty);
						if(isEndProduct == "Y")
						{
							mToMProductionParent = new MBLDMtomItemLine(pobj.getCtx(), getmBLDMtomItemLineID(), pobj.get_TrxName());
							StringBuilder sql = new StringBuilder("UPDATE ");
							sql.append("m_productionline SET ");
							sql.append("isendproduct = '" + isEndProduct + "'");
							sql.append(", movementqty = '" + movementQty + "'");
							sql.append(" WHERE m_productionline.bld_mtom_item_line_id = ");
							sql.append(getmBLDMtomItemLineID());
							sql.append(" AND m_product_id = ");
							sql.append(mProductionLine.getM_Product_ID());
							log.warning("Attempting to DB.executeUpdate. Success is greater than 0 or Yes: " + (DB.executeUpdate(sql.toString(), pobj.get_TrxName())>0));
							
						}
						
					}
			}
			else isEndProduct = "N";
		} 
		
		/*
		if ( (isEndProduct == "Y") && mProductionLine.getM_AttributeSetInstance_ID() != 0 )
		{
			String where = "M_QualityTest_ID IN (SELECT M_QualityTest_ID " +
			"FROM M_Product_QualityTest WHERE M_Product_ID=?) " +
			"AND M_QualityTest_ID NOT IN (SELECT M_QualityTest_ID " +
			"FROM M_QualityTestResult WHERE M_AttributeSetInstance_ID=?)";

			List<MQualityTest> tests = new Query(Env.getCtx(), MQualityTest.Table_Name, where, pobj.get_TrxName())
			.setOnlyActiveRecords(true).setParameters(mProductionLine.getM_Product_ID(), mProductionLine.getM_AttributeSetInstance_ID()).list();
			// create quality control results
			for (MQualityTest test : tests)
			{
				test.createResult(mProductionLine.getM_AttributeSetInstance_ID());
			}
		}
		
		log.warning("Line 220: Is end product: " + isEndProduct);
		*/
		
		log.warning("At end of handleMProductionLineEvent(PO pobj)");
	}
	
	private int getmBLDMtomItemLineID() {
		int mBLDMtomItemLineID = mProductionLine.get_ValueAsInt("bld_mtom_item_line_id");
		log.warning("---------In getmBLDMtomItemLineID(). mBLDMtomItemLineID = " + mBLDMtomItemLineID);
		return mBLDMtomItemLineID;
	}
	
	private MOrderLine copyAttributeInstance(X_C_OrderLine toOrderLine, MProduct mProduct) {
		
		log.warning("---------In MBLDEventHandler.copyAttributeInstance()");
		X_C_OrderLine fromOrderLine = null;
		int fromOrderLineID = 0;
		String trxName = toOrderLine.get_TrxName();
		int toMproductID = mProduct.getM_Product_ID();
		log.warning("---------Line 489 toMproductID = " + toMproductID);
		
			fromOrderLineID = toOrderLine.get_ValueAsInt("copypk");
			log.warning("---------Line 344 fromOrderLineID = " + fromOrderLineID);
		   if(fromOrderLineID!= 0)
				{
				   log.warning("--------- line 347 fromOrderLineID does not equal 0");
				   fromOrderLine = new MOrderLine(Env.getCtx(),prevOrderLineID, trxName);
					int fromMProductID = fromOrderLine.getM_Product_ID();
					if(fromMProductID != toMproductID)
					{
						log.warning("--------- line 500...fromMProductID != toMproductID. fromMProductID:" + fromMProductID + "toMproductID: " + toMproductID);
						fromOrderLine = null;
						return (MOrderLine)fromOrderLine;//Wrong product, don't copy MAI.
					}
					log.warning("--------- line 504");
						
			MAttributeSetInstance fromMAttributeSetInstance = MAttributeSetInstance.get(Env.getCtx(), fromOrderLine.getM_AttributeSetInstance_ID(), mProduct.getM_Product_ID());
			MAttributeSetInstance toMAttributeSetInstance = new MAttributeSetInstance(Env.getCtx(),0,toOrderLine.get_TrxName());
			toMAttributeSetInstance.save(toOrderLine.get_TrxName());
			
			int fromAttributeSetId = fromMAttributeSetInstance.getM_AttributeSet_ID();
			
			int toAttributeSetInstanceId = toMAttributeSetInstance.getM_AttributeSetInstance_ID();
			toMAttributeSetInstance.setM_AttributeSet_ID(fromAttributeSetId);
			toMAttributeSetInstance.saveEx();
			
			RowSet fromAttributeInstances = getmAttributeInstances(fromMAttributeSetInstance.getM_AttributeSetInstance_ID());
			
			
		    try {
				while (fromAttributeInstances.next()) {  
					
				    int m_attribute_id = fromAttributeInstances.getInt(1);
				    String value = fromAttributeInstances.getString(2);
				    int mAttributeValueID = fromAttributeInstances.getInt(3);
				    
				    MAttribute mAttribute = new MAttribute(Env.getCtx(), m_attribute_id, toOrderLine.get_TrxName());
				    String attributeType = mAttribute.getAttributeValueType();
				    
				    if(attributeType.equalsIgnoreCase("N"))
				    {
				    	log.warning("--------- line 542");
				    	//Constructor for numeric attributes
				    	BigDecimal bigValue = Env.ZERO;
				    	if(value!=null) 
				    	{
				    		bigValue = new BigDecimal(value);
				    	}
				    	MAttributeInstance toMAttributeInstance = new MAttributeInstance(Env.getCtx(), m_attribute_id, 
				    	toAttributeSetInstanceId, bigValue.setScale(1), toOrderLine.get_TrxName());
				    	toMAttributeInstance.setM_AttributeValue_ID(mAttributeValueID);
				    	//toMAttributeInstance.setValueNumber(new BigDecimal(value));
				    	toMAttributeInstance.saveEx();
				    }
				    else	
				    {	
				    	log.warning("--------- line 551");
				    	//Constructor for String attributes
					    MAttributeInstance toMAttributeInstance = new MAttributeInstance(Env.getCtx(),
					    m_attribute_id, toAttributeSetInstanceId, value, toOrderLine.get_TrxName());
					    toMAttributeInstance.setM_AttributeValue_ID(mAttributeValueID);
					    toMAttributeInstance.saveEx();
				    }
			   }
					} catch (SQLException e) {
						log.severe("Could not get values from attributeinstance RowSet " + e.getMessage());
						e.printStackTrace();
					}  
					    
					    File tempFile = new File("/tmp/ignoreNewMAttributeInstance");
					    tempFile.delete();
					    log.warning("--------- line 557");
					    toOrderLine.setM_AttributeSetInstance_ID(toAttributeSetInstanceId);
					    toOrderLine.saveEx();
					    toMAttributeSetInstance.setDescription();
						toMAttributeSetInstance.saveEx();
		}
		   log.warning("--------- line 526");
		   if(fromOrderLine != null)fromOrderLine.saveEx();
		   return (MOrderLine)fromOrderLine;
	}
	
	private MBLDLineProductInstance copyAttributeSetInstances(MBLDLineProductInstance  fromPO, MBLDLineProductInstance  toPO, int mProductID)
	{
		
		MAttributeSetInstance fromMAttributeSetInstance = MAttributeSetInstance.get(Env.getCtx(), fromPO.getM_AttributeSetInstance_ID(), mProductID);
		MAttributeSetInstance toMAttributeSetInstance = new MAttributeSetInstance(Env.getCtx(),0,toPO.get_TrxName());
		toMAttributeSetInstance.save(toPO.get_TrxName());
		
		int fromAttributeSetId = fromMAttributeSetInstance.getM_AttributeSet_ID();
		
		int toAttributeSetInstanceId = toMAttributeSetInstance.getM_AttributeSetInstance_ID();
		toMAttributeSetInstance.setM_AttributeSet_ID(fromAttributeSetId);
		toMAttributeSetInstance.saveEx();
		
		RowSet fromAttributeInstances = getmAttributeInstances(fromMAttributeSetInstance.getM_AttributeSetInstance_ID());
		
		
	    try {
			while (fromAttributeInstances.next()) {  
				
			    int m_attribute_id = fromAttributeInstances.getInt(1);
			    String value = fromAttributeInstances.getString(2);
			    int mAttributeValueID = fromAttributeInstances.getInt(3);
			    
			    MAttribute mAttribute = new MAttribute(Env.getCtx(), m_attribute_id, toPO.get_TrxName());
			    String attributeType = mAttribute.getAttributeValueType();
			    
			    if(attributeType.equalsIgnoreCase("N"))
			    {
			    	log.warning("--------- line 265");
			    	//Constructor for numeric attributes
			    	MAttributeInstance toMAttributeInstance = new MAttributeInstance(Env.getCtx(), m_attribute_id, 
			    	toAttributeSetInstanceId, new BigDecimal(value).setScale(1), toPO.get_TrxName());
			    	toMAttributeInstance.setM_AttributeValue_ID(mAttributeValueID);
			    	//toMAttributeInstance.setValueNumber(new BigDecimal(value));
			    	toMAttributeInstance.saveEx();
			    }
			    else	
			    {	
			    	log.warning("--------- line 275");
			    	//Constructor for String attributes
				    MAttributeInstance toMAttributeInstance = new MAttributeInstance(Env.getCtx(),
				    m_attribute_id, toAttributeSetInstanceId, value, toPO.get_TrxName());
				    toMAttributeInstance.setM_AttributeValue_ID(mAttributeValueID);
				    toMAttributeInstance.saveEx();
			    }
   }
		} catch (SQLException e) {
			log.severe("Could not get values from attributeinstance RowSet " + e.getMessage());
			e.printStackTrace();
		}  
	    
	    File tempFile = new File("/tmp/ignoreNewMAttributeInstance");
	    tempFile.delete();
	    log.warning("--------- line 290");
	    toPO.setM_AttributeSetInstance_ID(toAttributeSetInstanceId);
	    toPO.saveEx();
	    toMAttributeSetInstance.setDescription();
		toMAttributeSetInstance.saveEx();
		
		if(fromPO != null)fromPO.saveEx();
		return fromPO;
	}
	
	private RowSet getmAttributeInstances(int mAttributeSetinstanceID)
	{
		StringBuilder sql = new StringBuilder("SELECT m_attribute_id, value, m_attributevalue_id ");
		sql.append("FROM m_attributeinstance mai ");
		sql.append(" WHERE mai.m_attributesetinstance_id = ");
		sql.append(mAttributeSetinstanceID);
		
		RowSet rowset = DB.getRowSet(sql.toString());
		return rowset;
	}
	
	private void copyBldProductInstance(int fromBldPIID,  int toBldPIID, int mProductID) {
		
		/*get the from psi
		 * create a new Instance from the from psi
		 * 
		 */
		log.warning("--------In MBLDEventHandler.copyBldProductInstance()");
		log.warning("fromBldPIID = " + fromBldPIID + ", toBldPIID = " + toBldPIID + ", mProductID = " + mProductID);
		StringBuilder sb = new StringBuilder();
		MBLDLineProductInstance[] fromInstance = MBLDProductPartType.getmBLDLineProductInstance(fromBldPIID, trxName);
		if(toBldPIID > 0)
			{
				for(int i = 0; i < fromInstance.length; i++)
				{
					MBLDLineProductInstance toInstance = new MBLDLineProductInstance(Env.getCtx(), 0, trxName);
					toInstance.setBLD_Product_PartType_ID(fromInstance[i].getBLD_Product_PartType_ID());
					toInstance.setBLD_Line_ProductSetInstance_ID(toBldPIID);
					toInstance.setM_Product_ID(fromInstance[i].getM_Product_ID());
					if(fromInstance[i].getM_AttributeSetInstance_ID() > 0)
					{
						toInstance = copyAttributeSetInstances(fromInstance[i], toInstance, mProductID);
					}
					
					toInstance.saveEx(trxName);
					
					if (fromInstance[i]!= null && fromInstance[i].getM_Product_ID() > 0)
					{
						MProduct nameAdd = new MProduct(Env.getCtx(), fromInstance[i].getM_Product_ID(), trxName);
						if (sb.length() > 0)
							sb.append("_");
						sb.append(nameAdd.getName());
					}
				}
				MBLDLineProductSetInstance toBLdpsi = new MBLDLineProductSetInstance(Env.getCtx(), toBldPIID, trxName);
	
				toBLdpsi.setDescription(sb.toString());
				toBLdpsi.save();
			}
		
		
	}
	
	private void setDiscount(MOrderLine fromOrderLine, X_C_OrderLine toOrderLine) {
		int mProductID = fromOrderLine.getM_Product_ID();
		MProduct fromProduct = new MProduct(Env.getCtx(), mProductID, trxName);
		if(fromProduct.get_ValueAsBoolean("isgridprice"))
		{
			toOrderLine.setDiscount(fromOrderLine.getDiscount());
		}
	}
	
}
		