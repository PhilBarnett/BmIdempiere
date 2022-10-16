/**
 * 
 */
package au.blindmot.model;

import java.sql.ResultSet;
import java.util.Properties;

/**
 * @author phil
 *
 */
public class MBLDMtmProductBomAdd extends X_BLD_MTM_Product_Bom_Add {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param ctx
	 * @param BLD_MTM_Product_Bom_Add_ID
	 * @param trxName
	 */
	public MBLDMtmProductBomAdd(Properties ctx, int BLD_MTM_Product_Bom_Add_ID, String trxName) {
		super(ctx, BLD_MTM_Product_Bom_Add_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MBLDMtmProductBomAdd(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	


}
