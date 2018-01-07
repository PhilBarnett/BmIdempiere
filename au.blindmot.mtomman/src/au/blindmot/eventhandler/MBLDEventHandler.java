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
	
	@Override
	protected void initialize() {
		//register EventTopics and TableNames   
				registerTableEvent(IEventTopics.PO_AFTER_NEW, MProductionLine.Table_Name); 
				registerTableEvent(IEventTopics.PO_POST_UPADTE, MProductionLine.Table_Name); 
				registerTableEvent(IEventTopics.PO_AFTER_CHANGE, MProductionLine.Table_Name);
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
	}
	
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

		//System.out.println(mProductionLine.getM_Product_ID() && mToMProductionParent.getProductionQty().signum() == mProductionLine.getMovementQty().signum());
		System.out.println("mToMProductionParent.getM_Product_ID(): " + mToMProductionParent.getM_Product_ID());
		System.out.println("mProductionLine.getM_Product_ID(): " + mProductionLine.getM_Product_ID());
		System.out.println("mToMProductionParent.getProductionQty().signum(): " + mToMProductionParent.getProductionQty().signum());
		System.out.println("mProductionLine.getMovementQty().signum()" + mProductionLine.getMovementQty().signum());
		if (getmBLDMtomItemLineID() > 0) 
		{
			if ( mToMProductionParent.getM_Product_ID() == mProductionLine.getM_Product_ID())
			{
				isEndProduct = "Y";
				movementQty = BigDecimal.ONE;
			}
			else 
				isEndProduct = "N";
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
		/****May be able to leave this to MProductionLine;
		if ( !mProductionLine.isEndProduct() )
		{
			mProductionLine.setMovementQty(mProductionLine.getQtyUsed().negate());
		}
		**/
		
		
		if(isEndProduct == "Y")
		{
			mBLDMtomProductionID = mToMProductionParent.getbld_mtom_production_ID();
			if (mBLDMtomProductionID >0)
			{
				MBLDMtomProduction mtmProd = new MBLDMtomProduction(Env.getCtx(), mBLDMtomProductionID, pobj.get_TrxName());
				String status = mtmProd.getDocStatus();
				if(status.equalsIgnoreCase(DocAction.ACTION_Void))
				{
					movementQty.negate();
				}
			}
			
			StringBuilder sql = new StringBuilder("UPDATE ");
			sql.append("m_productionline SET ");
			sql.append("isendproduct = '" + isEndProduct + "'");
			sql.append(", movementqty = '" + movementQty + "'");
			sql.append(" WHERE m_productionline.bld_mtom_item_line_id = ");
			sql.append(getmBLDMtomItemLineID());
			sql.append(" AND m_product_id = ");
			sql.append(mProductionLine.getM_Product_ID());
			System.out.println(sql.toString());
			
			System.out.println("Attempting to DB.executeUpdate. Success is greater than 0: " + (DB.executeUpdate(sql.toString(), pobj.get_TrxName())>0));
			
		}
		
	}
	
	private int getmBLDMtomItemLineID() {
		return mProductionLine.get_ValueAsInt("bld_mtom_item_line_id");
	}
}
		