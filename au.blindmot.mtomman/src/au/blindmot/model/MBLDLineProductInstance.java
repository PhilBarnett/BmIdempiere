package au.blindmot.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBLDLineProductInstance extends X_BLD_Line_ProductInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4656285870832375097L;

	
	/**
	 * New Instance
	 * @param ctx
	 * @param BLD_Product_PartType_ID
	 * @param BLD_Line_ProductSetInstance_ID
	 * @param mProductID
	 * @param trxName
	 
	public MBLDLineProductInstance(Properties ctx, int BLD_Product_PartType_ID, 
			int BLD_Line_ProductSetInstance_ID, int mProductID, String trxName)
		{
			super(ctx, 0, trxName);
			setBLD_Product_PartType_ID (BLD_Product_PartType_ID);
			setBLD_Line_ProductSetInstance_ID (BLD_Line_ProductSetInstance_ID);
			setM_Product_ID (mProductID);
		}	//	MBLDLineProductInstance
*/
	/**
	 * Load Existing - use when mProductID is not 0 or null
	 * @param p_ctx
	 * @param rs
	 * @param get_TrxName
	 * @param BLD_Product_PartType_ID
	 * @param BLD_Line_ProductSetInstance_ID
	 */
	public MBLDLineProductInstance(Properties p_ctx, ResultSet rs, String get_TrxName, int BLD_Product_PartType_ID, int BLD_Line_ProductSetInstance_ID, int mProductID) {
		super(p_ctx, rs, get_TrxName);
		setBLD_Product_PartType_ID (BLD_Product_PartType_ID);
		setBLD_Line_ProductSetInstance_ID (BLD_Line_ProductSetInstance_ID);
		setM_Product_ID (mProductID);
	}

	public MBLDLineProductInstance(Properties p_ctx) {
		super(p_ctx);
	}

	public void setM_Product_ID(String value) {
		setM_Product_ID(Integer.parseInt(value));
	
	}
	
	public MBLDLineProductInstance(Properties ctx, int id, String trxName)
	{
		super(ctx, id, trxName);
	;
	}

	public MBLDLineProductInstance(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

}
