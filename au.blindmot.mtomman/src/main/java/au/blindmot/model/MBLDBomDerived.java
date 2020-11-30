package au.blindmot.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Reference;
import org.compiere.model.X_M_PartType;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MBLDBomDerived extends X_BLD_mtom_bomderived {

	/**
	 * This MCkass may not be necessary
	 */
	private static final long serialVersionUID = -6034231174542629519L;
	
	public MBLDBomDerived(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public MBLDBomDerived(Properties ctx, int BLD_mtom_bomderived_ID, String trxName) {
		super(ctx, BLD_mtom_bomderived_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public String getMPartType() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT mpt.name ");
		sql.append("FROM m_parttype mpt ");
		sql.append("JOIN m_product mp ON mp.m_parttype_id = mpt.m_parttype_id ");
		sql.append("WHERE mp.m_product_id = ?");
		
		return DB.getSQLValueString(get_TrxName(), sql.toString(), getM_Product_ID());
	}
	
	public boolean hasDeduction(String deductionType) {

			StringBuffer sql = new StringBuffer	("	SELECT value FROM m_attributeinstance ma ");
			sql.append("WHERE ma.m_attributesetinstance_id = ");
			sql.append("(SELECT m_attributesetinstance_id FROM m_product mp WHERE mp.m_product_id = ");
			sql.append(getM_Product_ID());
			sql.append(") ");
			sql.append("AND ma.m_attribute_id = ");
			sql.append("(SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name = '");
			sql.append(deductionType);
			sql.append("')");
					
			BigDecimal deduction = new BigDecimal(DB.getSQLValueEx(get_TrxName(), sql.toString()));
			if(deduction == null || deduction.compareTo(Env.ZERO) < 0)
			{
				return false;
			}
		
		return true;
		
	}

}
