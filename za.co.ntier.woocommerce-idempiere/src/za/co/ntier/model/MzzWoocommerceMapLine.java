package za.co.ntier.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.util.DB;
import org.compiere.util.Env;

public class MzzWoocommerceMapLine extends X_ZZ_Woocommerce_Map_Line {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MzzWoocommerceMapLine(Properties ctx, int ZZ_Woocommerce_Map_Line_ID, String trxName) {
		super(ctx, ZZ_Woocommerce_Map_Line_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MzzWoocommerceMapLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	
	/**************************************************************************
	 * 	Before Save
	 *	@param newRecord
	 *	@return true if it can be saved
	 */
	protected boolean beforeSave (boolean newRecord)
	{
//		Get Line No
			if (getLine() == 0)
			{
				String sql = "SELECT COALESCE(MAX(Line),0)+10 FROM zz_woocommerce_map_line WHERE ZZ_Woocommerce_Map_ID=?";
				int ii = DB.getSQLValue (get_TrxName(), sql, getZZ_Woocommerce_Map_ID());
				setLine (ii);
			}
		
		return true;
		
	}
	
	public boolean isParentAddToDuplicate(int mapLineID) {
		MzzWoocommerceMapLine mZzWoocommerceMapLine = new MzzWoocommerceMapLine(Env.getCtx(), mapLineID, null);
		MzzWoocommerceMap mZzWoocommerceMap = new MzzWoocommerceMap(Env.getCtx(), mZzWoocommerceMapLine.getZZ_Woocommerce_Map_ID(), null);
		return mZzWoocommerceMap.isAdd_To_Duplicate();
	}


}
