package au.blindmot.utils;

import java.util.ArrayList;

import org.compiere.util.DB;

public class MtmUtils {

	
	/*
	 * Currently this class is unused as it was causing massive system slowdown.
	 */
	// Prefixes for bar codes based on table_id - to shorten barcodes
	public static final String MTM_PRODUCTION_PREFIX = "01";
	public static final String MTM_PRODUCTION_ITEM_PREFIX = "02";
	public static final String MTM_PRODUCTION_ASSEMBLEY_ITEM = "03";
	public static final String MTM_HEAD_RAIL_DEDUCTION = "Head Rail Deduction";
	public static final String MTM_FABRIC_DEDUCTION = "Fabric deduction";
	public static final String MTM_FABRIC_ADDITION = "Fabric length addition";
	public static final String MTM_BOTTOM_BAR_DEDUCTION = "Bottom bar deduction";
	
	public MtmUtils() {
		// TODO Auto-generated constructor stub
	}

	public static String getBarcode(int table_id, int record_id) {

		String prefix = getBarcodePrefix(table_id);
		if (prefix != null) {
			StringBuffer barCode = new StringBuffer(prefix);
			return barCode.append(record_id).toString();
		} else
			return null;

	}

	private static String getBarcodePrefix(int table_id) {

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT name  ");
		sql.append("FROM ad_table ");
		sql.append("WHERE ad_table_id = ?");

		String tableName = DB.getSQLValueStringEx(null, sql.toString(), table_id);
		String prefix = null;

		if (tableName.equalsIgnoreCase("Made to measure production")) {
			prefix = MTM_PRODUCTION_PREFIX;
		} else if (tableName.equalsIgnoreCase("Made to measure items")) {
			prefix = MTM_PRODUCTION_ITEM_PREFIX;
		} else if (tableName.equalsIgnoreCase("Made to measure cuts")) {
			prefix = MTM_PRODUCTION_ASSEMBLEY_ITEM;
		}

		return prefix;
	}

	public int getBendingMoment(int length, int fabricProductId, int basebarProductId) {
		// TODO: get the weight in kg/m^2 of the fabric
		// TODO: get the weight of the base bar
		/*
		 * The bending moment is max in the centre of the beam (blind). The
		 * formula is: wl^2 Mmax = ------- 8 Where: w = kg per lineal metre =
		 * total weight of basebar and fabric divided by length l = length of
		 * tube Mmax will be in kg-metres
		 * 
		 * See: http://www.totalconstructionhelp.com/deflection.html
		 */
		int moment = 0;
		return moment;

	}

	public int getDeductions(ArrayList<Integer> components, String deductionType) {

		int totalDeduction = 0;
		for (Integer productId : components) {
			totalDeduction = totalDeduction + getDeduction(productId.intValue(), deductionType);
		}

		return totalDeduction;
	}

	public int getDeduction (int mProductID, String deductionType) {

	StringBuffer sql = new StringBuffer	("	SELECT value FROM m_attributeinstance ma ");
	sql.append("WHERE ma.m_attributesetinstance_id = ");
	sql.append("(SELECT m_attributesetinstance_id FROM m_product mp WHERE mp.m_product_id = ");
	sql.append(mProductID);
	sql.append(") ");
	sql.append("AND ma.m_attribute_id = ");
	sql.append("(SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name = '");
	sql.append(deductionType);
	sql.append("')");
			
	int deduction = DB.getSQLValueEx(null, sql.toString());
	
	return deduction;
	}

}
