package au.blindmot.model;

import java.util.Properties;

public class MBLDLineProductInstance extends X_BLD_Line_ProductInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4656285870832375097L;

	public MBLDLineProductInstance(Properties ctx, int BLD_Line_ProductInstance_ID, String trxName) {
		super(ctx, BLD_Line_ProductInstance_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MBLDLineProductInstance(Properties ctx, int BLD_Product_PartType_ID, 
			int BLD_Line_ProductSetInstance_ID, int mProductID, String trxName)
		{
			super(ctx, 0, trxName);
			setBLD_Product_PartType_ID (BLD_Product_PartType_ID);
			setBLD_Line_ProductSetInstance_ID (BLD_Line_ProductSetInstance_ID);
			setM_Product_ID (mProductID);
		}	//	MBLDLineProductInstance

}
