/**
 * 
 */
package za.co.ntier.model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.adempiere.exceptions.DBException;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.MOrderLine;
import org.compiere.model.Query;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;

import za.co.ntier.woocommerce.WcOrder;

/**
 * @author phil
 *
 */
public class MzzWoocommerceMap extends X_ZZ_Woocommerce_Map {

	private static final long serialVersionUID = 8977967059507773501L;
	public String fieldValue = null;

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	/**
	 * @param ctx
	 * @param ZZ_Woocommerce_Map_ID
	 * @param trxName
	 */
	public MzzWoocommerceMap(Properties ctx, int ZZ_Woocommerce_Map_ID, String trxName) {
		super(ctx, ZZ_Woocommerce_Map_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MzzWoocommerceMap(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * @param ctx
	 * @param mzzWoocommerceMapID
	 * @return
	 */
	public static MzzWoocommerceMap get(Properties ctx, int mzzWoocommerceMapID) {
		return new MzzWoocommerceMap(ctx, mzzWoocommerceMapID, null);
	}
	
	/**
	 * 
	 * @param mzzWoocommerceMapID
	 * @return
	 */
	public static MzzWoocommerceMap get (int mzzWoocommerceMapID)
	{
		return get(Env.getCtx(), mzzWoocommerceMapID);
	}
	
	/**
	 * 
	 * @param mzzWoocommerceMapID
	 * @param ctx
	 * @param orderClause
	 * @param whereClause
	 * @return
	 */
	public MzzWoocommerceMapLine[] getMzzWoocommerceMapLines(int mzzWoocommerceMapID, Properties ctx, String orderClause, String whereClause) {
		
		StringBuilder whereClauseFinal = new StringBuilder(MzzWoocommerceMapLine.COLUMNNAME_ZZ_Woocommerce_Map_ID+"=? " + " AND isactive = 'Y'");
		if (!Util.isEmpty(whereClause, true))
			whereClauseFinal.append(whereClause);
		if (orderClause.length() == 0)
			orderClause = MzzWoocommerceMapLine.COLUMNNAME_Line;	
		try {
			List<MzzWoocommerceMapLine> list = new Query(getCtx(), I_ZZ_Woocommerce_Map_Line.Table_Name, whereClauseFinal.toString(), get_TrxName())
					.setParameters(get_ID())
					.setOrderBy(orderClause)
					.list();
					return list.toArray(new MzzWoocommerceMapLine [list.size()]);
					
				} catch (DBException e) {
					e.printStackTrace();
					throw new AdempiereUserError("Error.");
				} 
	}
	
		
	/**
	 * 
	 * @param mProductID
	 * @param ctx
	 * @param orderClause
	 * @return
	 */
	public static MzzWoocommerceMap[] getMzzWoocommerceMapRecords(int mProductID, Properties ctx, String orderClause) {
		StringBuilder whereClauseFinal = new StringBuilder("m_product_id = ");
		whereClauseFinal.append(mProductID);
		whereClauseFinal.append(" AND isactive = 'Y'");
		if (orderClause.length() == 0)
			orderClause = MzzWoocommerceMap.COLUMNNAME_Line;
		
		
	/*	StringBuilder sql = new StringBuilder();
		sql.append("SELECT ZZ_Woocommerce_Map_ID ");
		sql.append("FROM ZZ_Woocommerce_Map ");
		sql.append("WHERE m_product_id = ?");
		sql.append("ORDER BY ");
		sql.append(orderClause);
		Object[] params = new Object[1];
		params[0] = mProductID;
		
		int[] ids = DB.getIDsEx(null, sql.toString(), params);
		List<MzzWoocommerceMap> list = new ArrayList<>();
		for(int i =0; i < ids.length; i++)
		{
			list.add(new MzzWoocommerceMap(ctx, ids[i], null));
		}
		return list.toArray(new MzzWoocommerceMap [list.size()]); */
		
		//Doesn't work... generates POs
		try {
			List<MzzWoocommerceMap> list = new Query(ctx, I_ZZ_Woocommerce_Map.Table_Name, whereClauseFinal.toString(), null)
					.setOrderBy(orderClause)
					.list();
			//List<MzzWoocommerceMap> mapList = (List<MzzWoocommerceMap>)(List<?>) list;
			return list.toArray(new MzzWoocommerceMap [list.size()]);
			
		} catch (DBException e) {
			e.printStackTrace();
			throw new AdempiereUserError("Error.");
		} 
	}

	/**
	 * 
	 * @param mProductID
	 * @param fieldID
	 * @param fieldValue
	 * @param ctx
	 * @return
	 */
	public static MzzWoocommerceMap getMzzWoocommerceMapping(int mProductID, String fieldID, String fieldValue,
			Properties ctx, boolean wantChild) {
		//get Lines and check for matches.
		//If isParent == true then we only want to return multi
		MzzWoocommerceMap [] lines = getMzzWoocommerceMapRecords(mProductID, ctx, "");
		List<MzzWoocommerceMap> matches = new ArrayList<>();
		for(int l=0; l < lines.length; l++)
		{
			String multiSelect = lines[l].getzz_woocommerce_m_select_type();
			boolean mapIsChild = false;
		/*	if(fieldValue.equalsIgnoreCase("Stella"))
			{
				System.out.println(fieldValue);
			}*/
			if(multiSelect != null) 
				{
					mapIsChild = multiSelect.equals(WcOrder.WOOCOMMERCE_MAP_MULTISELECT_CHILD);
				}
			//We want a 'child map
			if(mapIsChild && wantChild)
			{
				if(lines[l].getwoocommerce_field_key().equals(fieldID))
				{
					matches.add(lines[l]);
				}
			}
			else if(!mapIsChild && !wantChild)//We want non children or 'parent' map records.
			{
				/*if(lines[l].getwoocommerce_field_key() == null)
				{
					System.out.println(lines[l].toString());
				}*/
				
				/*We have a field with mulitple possible values - find a match to field value and return it.
				 * PROBLEM: EG If we have multiple width fields, code tries to match on value; no value exists, null gets returned.
				 * Then an empty record gets created etc. 
				 * So: If getwoocommerce_field_key().equals(fieldID) AND the fieldvalue is null then return the record as it must be expecting a value like width or drop.
				 * Validation to ensure that the incoming data contains a value must be done in WooCommerce.
				 * */
				/*//Enable for testing if required.
				System.out.println(lines[l].toString());
				
				if(!(lines[l].getwoocommerce_field_value()==null)) System.out.println(lines[l].getwoocommerce_field_value().toString());
				if(lines[l].getwoocommerce_field_key().equalsIgnoreCase("651e63fed6d51"))//651e63fed6d51 is Width(mm) in rol1
				{
					System.out.println(lines[l].getwoocommerce_field_key().toString());
				}*/
				
				if(lines[l].getwoocommerce_field_key().equalsIgnoreCase(fieldID) && lines[l].getwoocommerce_field_value() == null)
				{
 					return lines[l];
				}
				
				if(lines[l].getwoocommerce_field_key() != null && lines[l].getwoocommerce_field_key().equals(fieldID))
				{
					matches.add(lines[l]);
				}
			}
		}
		//Just one record? return it.
		if(matches.size() == 1) return matches.get(0);
		if(matches.size() > 1)
			
		{
			for(MzzWoocommerceMap map : matches)
			{
				String multiSelect = map.getzz_woocommerce_m_select_type();
				String mapValue = map.getwoocommerce_field_value();
				if((mapValue != null && mapValue.equals(fieldValue)) || (multiSelect != null && multiSelect.equalsIgnoreCase(WcOrder.WOOCOMMERCE_MAP_MULTISELECT_PARENT)))
					{
						return map;
					}
			}
		}
		
		return null;
	}
	
	public static MzzWoocommerceMap getMzzWoocommerceMapChild (int mProductID, String fieldID, String fieldValue, Properties ctx) {
		return getMzzWoocommerceMapping (mProductID, fieldID, fieldValue, ctx, true);
	}
	
	public static MzzWoocommerceMap getMzzWoocommerceMap (int mProductID, String fieldID, String fieldValue, Properties ctx) {
		return getMzzWoocommerceMapping (mProductID, fieldID, fieldValue, ctx, false);
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
				String sql = "SELECT COALESCE(MAX(Line),0)+10 FROM zz_woocommerce_map WHERE M_Product_ID=?";
				int ii = DB.getSQLValue (get_TrxName(), sql, getM_Product_ID());
				setLine (ii);
			}
		
		return true;
		
	}
}
