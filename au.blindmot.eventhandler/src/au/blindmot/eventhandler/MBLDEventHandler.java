	package au.blindmot.eventhandler;



import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.RowSet;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductionLine;
import org.compiere.model.MQualityTest;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_C_OrderLine;
import org.compiere.model.X_M_ProductionLine;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;

import au.blindmot.model.MBLDMtomItemLine;
import au.blindmot.model.MBLDMtomProduction;

public class MBLDEventHandler extends AbstractEventHandler {

	private CLogger log = CLogger.getCLogger(MBLDEventHandler.class);
	private X_M_ProductionLine mProductionLine = null;
	private X_C_OrderLine orderLine = null;
	private MBLDMtomItemLine mToMProductionParent = null;
	private boolean DocVoidReverse = false; 
	
	@Override
	protected void initialize() {
		//register EventTopics and TableNames   
				registerTableEvent(IEventTopics.PO_AFTER_NEW, MProductionLine.Table_Name); 
				registerTableEvent(IEventTopics.PO_POST_UPADTE, MProductionLine.Table_Name); 
				registerTableEvent(IEventTopics.PO_AFTER_CHANGE, MProductionLine.Table_Name);
				registerTableEvent(IEventTopics.DOC_BEFORE_REVERSECORRECT, MBLDMtomProduction.Table_Name);
				registerTableEvent(IEventTopics.DOC_BEFORE_REVERSEACCRUAL, MBLDMtomProduction.Table_Name);
				registerTableEvent(IEventTopics.PO_AFTER_NEW, MOrderLine.Table_Name);//PO to copy MAttributeSetInstance to
				registerTableEvent(IEventTopics.PO_POST_CREATE, MOrderLine.Table_Name);
				log.info("----------<MBLDEventHandler> .. IS NOW INITIALIZED");
				}
	
	@Override
	protected void doHandleEvent(Event event) {
	
	PO po = getPO(event);
	
	if(po instanceof MProductionLine)
		{
			mProductionLine = (X_M_ProductionLine)po;
			log.warning("---------MProductionLine event triggered");
			log.warning("---------event: " + event);
			
			handleMProductionLineEvent(po);//Handle all events with the same method
		}
	
	if(po instanceof MBLDMtomProduction)//The parent production is being voided or reversed.
	{
		DocVoidReverse = true;	
	}
	
	if(po instanceof MOrderLine)
	{
		log.warning("---------MOrderLine event triggered");
		log.warning("---------event: " + event);
		String trxName = po.get_TrxName();
		orderLine = (MOrderLine)po;//The new OrderLine to copy the atrribute instances to.
		MProduct mProduct = new MProduct(Env.getCtx(), orderLine.getM_Product_ID(), trxName);
		if(mProduct.get_ValueAsBoolean("ismadetomeasure"))
			{
				
				
			MOrderLine copyFromOrderLine = copyAttributeInstance(orderLine, mProduct, event);
			if(copyFromOrderLine != null)
				{	
				System.out.println(copyFromOrderLine.get_Value("mtm_attribute"));
				orderLine.set_ValueOfColumn("mtm_attribute", copyFromOrderLine.get_Value("mtm_attribute"));
				orderLine.saveEx();
				}
			}
	}
}
	/**
	 * Listens for and overrides the MProductionLine.beforeSave() method which
	 * sets the MBLDMtomItemLine child productionlines to 0 for the end product.
	 * @param pobj
	 */
	private void handleMProductionLineEvent(PO pobj) {
		
		log.warning("---------- In handle MProductionLineEvent");
		String isEndProduct = "N";
		BigDecimal movementQty = BigDecimal.ZERO;
		if(!(getmBLDMtomItemLineID() > 0))//It's not an MTMProduction
		{
			log.warning("---------Not an MTM product");
			return;
		}
		
		if (getmBLDMtomItemLineID() > 0)
			mToMProductionParent = new MBLDMtomItemLine(pobj.getCtx(), getmBLDMtomItemLineID(), pobj.get_TrxName());

		/*Lines below left in for future debugging convenience, commented out for production.
		 * /System.out.println(mProductionLine.getM_Product_ID() && mToMProductionParent.getProductionQty().signum() == mProductionLine.getMovementQty().signum());
		System.out.println("mToMProductionParent.getM_Product_ID(): " + mToMProductionParent.getM_Product_ID());
		System.out.println("mProductionLine.getM_Product_ID(): " + mProductionLine.getM_Product_ID());
		System.out.println("mToMProductionParent.getProductionQty().signum(): " + mToMProductionParent.getProductionQty().signum());
		System.out.println("mProductionLine.getMovementQty().signum()" + mProductionLine.getMovementQty().signum());
		*/
		if (getmBLDMtomItemLineID() > 0) 
		{
			if ( mToMProductionParent.getM_Product_ID() == mProductionLine.getM_Product_ID())//It's an end product
			{
				isEndProduct = "Y";
				movementQty = BigDecimal.ONE;
				System.out.println("----------movementQty rnd 1: " + movementQty);
				if(DocVoidReverse)
					{
						movementQty = movementQty.negate();
						System.out.println("----------movementQty rnd 2: " + movementQty);
					}
			}
			else isEndProduct = "N";
		} 
		
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
			//System.out.println(sql.toString());
			//System.out.println("----------movementQty before  DB.executeUpdate rnd 3: " + movementQty);
			System.out.println("Attempting to DB.executeUpdate. Success is greater than 0: " + (DB.executeUpdate(sql.toString(), pobj.get_TrxName())>0));
			
		}
		
	}
	
	private int getmBLDMtomItemLineID() {
		return mProductionLine.get_ValueAsInt("bld_mtom_item_line_id");
	}
	
	private MOrderLine copyAttributeInstance(X_C_OrderLine toOrderLine, MProduct mProduct, Event theEvent) {
		
		X_C_OrderLine fromOrderLine = null;
		int fromOrderLineID = 0;
		String trxName = toOrderLine.get_TrxName();
		int toMproductID = mProduct.getM_Product_ID();
			
		   Timestamp timeStamp = null;
		   
		   StringBuilder sql = new StringBuilder("SELECT MAX(updated) as maxupdate ");
		   sql.append("FROM c_orderline ");
		   sql.append("WHERE updatedby = ? ");
		   sql.append("AND m_product_id = ?");
		   sql.append("AND c_orderline_id < ?");
		  
		   List<Object> params = new  ArrayList<Object>();
		   params.add(new Integer(Env.getAD_User_ID(Env.getCtx())));
		   params.add(new Integer(mProduct.getM_Product_ID()));
		   params.add(new Integer(toOrderLine.getC_OrderLine_ID()));
		   
		   try
			{
			   timeStamp = DB.getSQLValueTSEx(trxName, sql.toString(), params);
			}
			catch(Exception e)
			{
				log.warning("Could not get C_OrderLine_ID to copy attributes, error: " + e.getMessage() );
			}
		   
		   
		   if(timeStamp != null)
		   {
			   StringBuilder sql1 = new StringBuilder("SELECT co.c_orderline_id ");
				sql1.append("FROM c_orderline co ");
				sql1.append("WHERE co.updated = ");
				sql1.append("'" + timeStamp + "'");
				
				try
				{
					fromOrderLineID = DB.getSQLValue(trxName, sql1.toString());
				}
				catch(Exception e)
				{
					log.warning("Could not get C_OrderLine_ID to copy attributes, error: " + e.getMessage() );
				}
		   }
		  
		   if(fromOrderLineID != 0)
			{
				fromOrderLine = new MOrderLine(Env.getCtx(),fromOrderLineID, trxName);
				int fromMProductID = fromOrderLine.getM_Product_ID();
				if(fromMProductID != toMproductID)
				{
					fromOrderLine = null;
					return (MOrderLine)fromOrderLine;//Wrong product, don't copy MAI.
				}
		   
		   
		
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
			    
			    MAttribute mAttribute = new MAttribute(Env.getCtx(), m_attribute_id, toOrderLine.get_TrxName());
			    String attributeType = mAttribute.getAttributeValueType();
			    
			    if(attributeType.equalsIgnoreCase("N"))
			    {
			    	//Constructor for numeric attributes
			    	MAttributeInstance toMAttributeInstance = new MAttributeInstance(Env.getCtx(), m_attribute_id, 
			    	toAttributeSetInstanceId, new BigDecimal(value), toOrderLine.get_TrxName());
			    	toMAttributeInstance.saveEx();
			    }
			    else	
			    {	
				    //Constructor for String attributes
				    MAttributeInstance toMAttributeInstance = new MAttributeInstance(Env.getCtx(),
				    m_attribute_id, toAttributeSetInstanceId, value, toOrderLine.get_TrxName());
				    toMAttributeInstance.saveEx();
			    }
   }
		} catch (SQLException e) {
			log.severe("Could not get values from attributeinstance RowSet " + e.getMessage());
			e.printStackTrace();
		}  
	    
	    File tempFile = new File("/tmp/ignoreNewMAttributeInstance");
	    tempFile.delete();
	    
	    toOrderLine.setM_AttributeSetInstance_ID(toAttributeSetInstanceId);
	    toOrderLine.saveEx();
	    toMAttributeSetInstance.setDescription();
		toMAttributeSetInstance.saveEx();
		}
		   return (MOrderLine)fromOrderLine;
	}
	
	private RowSet getmAttributeInstances(int mAttributeSetinstanceID)
	{
		StringBuilder sql = new StringBuilder("SELECT m_attribute_id, value ");
		sql.append("FROM m_attributeinstance mai ");
		sql.append(" WHERE mai.m_attributesetinstance_id = ");
		sql.append(mAttributeSetinstanceID);
		
		RowSet rowset = DB.getRowSet(sql.toString());
		return rowset;
	}
	
}
		