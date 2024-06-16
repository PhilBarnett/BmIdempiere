package au.blindmot.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.DB;
import org.compiere.util.Env;

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
	
	/**
	 * 
	 * @param bld_Line_ProductSetInstance_ID
	 * @param trxn
	 * @return
	 */
	public static MBLDLineProductInstance[] getmBLDLineProductInstance(int bld_Line_ProductSetInstance_ID, String trxn) {
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT bld_line_productinstance_id ");
		sql.append("FROM bld_line_productinstance ");
		sql.append("WHERE bld_line_productsetinstance_id = ? ");
		sql.append("ORDER BY bld_product_parttype_id");
		MBLDLineProductInstance[] retValueArray  = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<MBLDLineProductInstance> mBPSI = new ArrayList<MBLDLineProductInstance>();
		
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, bld_Line_ProductSetInstance_ID);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				int mBLDLineProductInstanceID = rs.getInt(1);
				MBLDLineProductInstance addValue = new MBLDLineProductInstance (Env.getCtx(), mBLDLineProductInstanceID, trxn);
				//Does this need to be saved? addValue.saveEx();
				mBPSI.add(addValue);
			}
		}
		catch (SQLException ex)
		{
			MBLDProductPartType.log.log(Level.SEVERE, sql.toString(), ex);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		retValueArray = new MBLDLineProductInstance[mBPSI.size()];
		return mBPSI.toArray(retValueArray);
		
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
