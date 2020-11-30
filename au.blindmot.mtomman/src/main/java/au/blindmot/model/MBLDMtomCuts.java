package au.blindmot.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;

import org.compiere.model.MProduct;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

import java.util.logging.Level;


public class MBLDMtomCuts extends X_BLD_mtom_cuts {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1039254396462542486L;
	
	public static final String MTM_HEAD_RAIL_DEDUCTION = "Head Rail Deduction";
	public static final String MTM_FABRIC_DEDUCTION = "Fabric deduction";
	public static final String MTM_FABRIC_ADDITION = "Fabric length addition";
	public static final String MTM_BOTTOM_BAR_DEDUCTION = "Bottom bar addition";
	protected static CLogger log = CLogger.get();
	final String transName = get_TrxName();
	public MBLDMtomCuts(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		//log = CLogger.getCLogger (getClass());
		// TODO Auto-generated constructor stub
	}

	public MBLDMtomCuts(Properties ctx, int BLD_mtom_bomderived_ID, String trxName) {
		super(ctx, BLD_mtom_bomderived_ID, trxName);
		//log = CLogger.getCLogger (getClass());
		// TODO Auto-generated constructor stub
	}
	
	public static BigDecimal getDeductions(ArrayList<KeyNamePair> components, String deductionType, String trxName) {

		BigDecimal totalDeduction = Env.ZERO;
		BigDecimal aDeduction; 
		for (KeyNamePair qtyId : components) 
		{
			aDeduction = getDeduction(Integer.parseInt(qtyId.getName()), deductionType, trxName);
			int qty = qtyId.getKey();
			log.warning("---------IN getDeductions(), deduction for ProductID: " + qtyId.getName() + " is: " + aDeduction + " Deduction Type: " + deductionType  + " Qty: " + qty);
			System.out.println("---------IN getDeductions(), deduction for ProductID: " + qtyId.getName() + " is: " + aDeduction + " Deduction Type: " + deductionType  + " Qty: " + qty);
			aDeduction = aDeduction.multiply(new BigDecimal(qty));
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
			
	int intDeduction = DB.getSQLValueEx(trxName, sql.toString());
	BigDecimal deduction = new BigDecimal(intDeduction);
	if(intDeduction == -1 || intDeduction < 0)
	{
		MProduct m_Product = new MProduct(null, mProductID, trxName);
		StringBuilder msg = new StringBuilder("There is no ");
		msg.append(deductionType);
		msg.append(" for product: ");
		msg.append(m_Product.getName());
		msg.append(" or the deduction is -ve");
		log.warning(msg.toString());
		return deduction;
	}
	else
	{
		return deduction;
	}
 }

}
