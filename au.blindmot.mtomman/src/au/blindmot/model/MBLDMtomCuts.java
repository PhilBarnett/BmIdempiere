package au.blindmot.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;

import org.compiere.util.DB;
import org.compiere.util.Env;
import java.util.logging.Level;


public class MBLDMtomCuts extends X_BLD_mtom_cuts {
	
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
	
	public static BigDecimal getDeductions(ArrayList<Integer> components, String deductionType, String trxName) {

		BigDecimal totalDeduction = Env.ZERO;
		BigDecimal aDeduction; 
		for (Integer productId : components) {
			aDeduction = getDeduction(productId.intValue(), deductionType, trxName);
			System.out.println("---------IN getDeductions(), deduction for ProductID: " + productId.toString() + " is: " + aDeduction);
			totalDeduction = totalDeduction.add(aDeduction);
		}

		return totalDeduction;
	}

	public static BigDecimal getDeduction (int mProductID, String deductionType, String trxName) {

	StringBuffer sql = new StringBuffer	("	SELECT value FROM m_attributeinstance ma ");
	sql.append("WHERE ma.m_attributesetinstance_id = ");
	sql.append("(SELECT m_attributesetinstance_id FROM m_product mp WHERE mp.m_product_id = ");
	sql.append(mProductID);
	sql.append(") ");
	sql.append("AND ma.m_attribute_id = ");
	sql.append("(SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name = '");
	sql.append(deductionType);
	sql.append("')");
			
	BigDecimal deduction = new BigDecimal(DB.getSQLValueEx(trxName, sql.toString()));
	
	return deduction;
	}

}
