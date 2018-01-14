package au.blindmot.eventhandler;

import java.math.BigDecimal;
import java.util.List;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MProductionLine;
import org.compiere.model.MQualityTest;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_M_ProductionLine;
import org.compiere.process.DocumentEngine;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.osgi.service.event.Event;
import org.compiere.process.DocAction;
import org.compiere.process.DocOptions;
import au.blindmot.model.MBLDMtomItemLine;
import au.blindmot.model.MBLDMtomProduction;
import au.blindmot.model.MProductionLine_BldMtoM;

public class MBLDEventHandler extends AbstractEventHandler {

	private CLogger log = CLogger.getCLogger(MBLDEventHandler.class);
	private X_M_ProductionLine mProductionLine = null;
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
				log.info("----------<MBLDEventHandler> .. IS NOW INITIALIZED");
				/*
				 * /TODO: Listen for and handle PO_AFTER_NEW_REPLICATION to copy the attribute
				 * instance which is not copied at this stage.
				 */
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
}

	
	/**
	 * Listens for and overrides the MProductionLine.beforeSvave() method which
	 * sets the MBLDMtomItemLine child productionlines to 0 for the end product.
	 * @param pobj
	 */
	private void handleMProductionLineEvent(PO pobj) {
		
		log.warning("---------- In handleMProductionLineEvent");
		String isEndProduct = "N";
		BigDecimal movementQty = BigDecimal.ZERO;
		int mBLDMtomProductionID = 0;
		
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
			mBLDMtomProductionID = mToMProductionParent.getbld_mtom_production_ID();
			
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
	
}
		