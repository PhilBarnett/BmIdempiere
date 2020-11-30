/**
 * 
 */
package au.blindmot.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MProductionLine;
import org.compiere.model.MQualityTest;
import org.compiere.model.Query;
import org.compiere.util.CLogger;

/**
 * @author phil
 *
 */
public class MProductionLine_BldMtoM extends MProductionLine {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5733241681486931626L;
	private MBLDMtomItemLine mToMProductionParent = new MBLDMtomItemLine(getCtx(), getmBLDMtomItemLineID(), get_TrxName());//The MBLDMtomItemLine that the ProductionLine records belong to

	public MProductionLine_BldMtoM(Properties ctx, int M_ProductionLine_ID, String trxName) {
		super(ctx, M_ProductionLine_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MProductionLine_BldMtoM(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	CLogger log = CLogger.getCLogger(MProductionLine_BldMtoM.class);

	@Override
	protected boolean beforeSave(boolean newRecord) 
	{
		log.warning("---------- In MProductionLine_BldMtoM beforeSave(boolean newRecord)");
		
		if(!(getmBLDMtomItemLineID() > 0))//It's not an MTMProduction
		{
			log.warning("---------Not an MTM product");
			super.beforeSave(newRecord);
		}
		/*
		if (mToMProductionParent == null && getmBLDMtomItemLineID() > 0)
			mToMProductionParent = new MBLDMtomItemLine(getCtx(), getmBLDMtomItemLineID(), get_TrxName());

		if (getmBLDMtomItemLineID() > 0) 
		{
			if ( mToMProductionParent.getM_Product_ID() == getM_Product_ID() && mToMProductionParent.getProductionQty().signum() == getMovementQty().signum())
				setIsEndProduct(true);
			else 
				setIsEndProduct(false);
		} 
		/********************************
		else 
		{
			I_M_ProductionPlan plan = getM_ProductionPlan();
			if (plan.getM_Product_ID() == getM_Product_ID() && plan.getProductionQty().signum() == getMovementQty().signum())
				setIsEndProduct(true);
			else 
				setIsEndProduct(false);
		}
		**********************************/
		/*
		if ( isEndProduct() && getM_AttributeSetInstance_ID() != 0 )
		{
			String where = "M_QualityTest_ID IN (SELECT M_QualityTest_ID " +
			"FROM M_Product_QualityTest WHERE M_Product_ID=?) " +
			"AND M_QualityTest_ID NOT IN (SELECT M_QualityTest_ID " +
			"FROM M_QualityTestResult WHERE M_AttributeSetInstance_ID=?)";

			List<MQualityTest> tests = new Query(getCtx(), MQualityTest.Table_Name, where, get_TrxName())
			.setOnlyActiveRecords(true).setParameters(getM_Product_ID(), getM_AttributeSetInstance_ID()).list();
			// create quality control results
			for (MQualityTest test : tests)
			{
				test.createResult(getM_AttributeSetInstance_ID());
			}
		}
		
		if ( !isEndProduct() )
		{
			setMovementQty(getQtyUsed().negate());
		}
		*/
		return true;
	
	}
	
	private int getmBLDMtomItemLineID() {
		return get_ValueAsInt("bld_mtom_item_line_id");
		
	}

}

