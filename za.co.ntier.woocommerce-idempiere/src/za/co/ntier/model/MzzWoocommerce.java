/**
 * 
 */
package za.co.ntier.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.adempiere.exceptions.DBException;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.Util;

/**
 * @author phil
 *
 */
public class MzzWoocommerce extends X_zz_woocommerce {

	private static final long serialVersionUID = 1L;

	/**
	 * @param ctx
	 * @param zz_woocommerce_ID
	 * @param trxName
	 */
	public MzzWoocommerce(Properties ctx, int zz_woocommerce_ID, String trxName) {
		super(ctx, zz_woocommerce_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MzzWoocommerce(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public static MzzWoocommerce get(int orgID, Properties ctx, String trxnName)
	{
		StringBuilder whereClauseFinal = new StringBuilder("ad_org_id = ");
		whereClauseFinal.append(orgID);
		
		int zzWoocommerceID;
		try {
			zzWoocommerceID = new Query(ctx, I_zz_woocommerce.Table_Name, whereClauseFinal.toString(), trxnName)
					.firstIdOnly();
		} catch (DBException e) {
			e.printStackTrace();
			throw new AdempiereUserError("More than one WooCommerce Defaults record found for this client. Check the WooCommerce Defaults window and check the number of records.");
		}
		
		return new MzzWoocommerce(ctx, zzWoocommerceID, trxnName);
		
	}
	
	public PO[] getLines (String whereClause, String orderClause)
	{
		//red1 - using new Query class from Teo / Victor's MDDOrder.java implementation
		StringBuilder whereClauseFinal = new StringBuilder(X_ZZ_Woocommerce_Match.COLUMNNAME_zz_woocommerce_ID+"=? " + " AND isactive = 'Y'");
		if (!Util.isEmpty(whereClause, true))
			whereClauseFinal.append(whereClause);
		if (orderClause.length() == 0)
			orderClause = X_zz_woocommerce.COLUMNNAME_zz_woocommerce_ID;
		//
		List<PO> list = new Query(getCtx(), I_ZZ_Woocommerce_Match.Table_Name, whereClauseFinal.toString(), get_TrxName())
										.setParameters(get_ID())
										.setOrderBy(orderClause)
										.list();
		//
		return list.toArray(new PO[list.size()]);		
	}	//	getLines
}
