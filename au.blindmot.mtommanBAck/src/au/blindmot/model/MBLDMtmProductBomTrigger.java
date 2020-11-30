/**
 * 
 */
package au.blindmot.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.I_C_OrderLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.Query;
import org.compiere.util.Util;

/**
 * @author phil
 *
 */
public class MBLDMtmProductBomTrigger extends X_BLD_MTM_Product_Bom_Trigger {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param ctx
	 * @param BLD_MTM_Product_Bom_Trigger_ID
	 * @param trxName
	 */
	public MBLDMtmProductBomTrigger(Properties ctx, int BLD_MTM_Product_Bom_Trigger_ID, String trxName) {
		super(ctx, BLD_MTM_Product_Bom_Trigger_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MBLDMtmProductBomTrigger(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MBLDMtmProductBomAdd[] getLines(String whereClause, String orderClause) {
		
		StringBuilder whereClauseFinal = new StringBuilder(MBLDMtmProductBomAdd.COLUMNNAME_BLD_MTM_Product_Bom_Trigger_ID+"=? ");
		if (!Util.isEmpty(whereClause, true))
			whereClauseFinal.append(whereClause);
		if (orderClause == null || orderClause.length() == 0)
			orderClause = MBLDMtmProductBomAdd.COLUMNNAME_Line;
		//
		List<MBLDMtmProductBomAdd> list = new Query(getCtx(), I_BLD_MTM_Product_Bom_Add.Table_Name, whereClauseFinal.toString(), get_TrxName())
										.setParameters(get_ID())
										.setOrderBy(orderClause)
										.list();
		/*for (MBLDMtmProductBomAdd ol : list) {
			ol.setHeaderInfo(this);
		}*/
		
		return list.toArray(new MBLDMtmProductBomAdd[list.size()]);	
		//	getLines
		 
	}

}
