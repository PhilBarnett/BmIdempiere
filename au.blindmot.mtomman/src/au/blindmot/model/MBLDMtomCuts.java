package au.blindmot.model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;

import org.compiere.util.DB;


public class MBLDMtomCuts extends X_BLD_mtom_cuts{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1039254396462542486L;
	
	public static final String MTM_HEAD_RAIL_DEDUCTION = "Head Rail Deduction";
	public static final String MTM_FABRIC_DEDUCTION = "Fabric deduction";
	public static final String MTM_FABRIC_ADDITION = "Fabric length addition";
	public static final String MTM_BOTTOM_BAR_DEDUCTION = "Bottom bar deduction";
	final String transName = get_TrxName();
	public MBLDMtomCuts(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public MBLDMtomCuts(Properties ctx, int BLD_mtom_bomderived_ID, String trxName) {
		super(ctx, BLD_mtom_bomderived_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public static int getDeductions(ArrayList<Integer> components, String deductionType, String trxName) {

		int totalDeduction = 0;
		for (Integer productId : components) {
			totalDeduction = totalDeduction + getDeduction(productId.intValue(), deductionType, trxName);
		}

		return totalDeduction;
	}

	public static int getDeduction (int mProductID, String deductionType, String trxName) {
		
	StringBuffer sql = new StringBuffer	("	SELECT value FROM m_attributeinstance ma ");
	sql.append("WHERE ma.m_attributesetinstance_id = ");
	sql.append("(SELECT m_attributesetinstance_id FROM m_product mp WHERE mp.m_product_id = ");
	sql.append(mProductID);
	sql.append(") ");
	sql.append("AND ma.m_attribute_id = ");
	sql.append("(SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name = '");
	sql.append(deductionType);
	sql.append("')");
			
	int deduction = DB.getSQLValueEx(trxName, sql.toString());
	
	return deduction;
	}

}
