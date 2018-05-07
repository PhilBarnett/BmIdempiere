package au.blindmot.utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.RowSet;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

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
	private static CLogger log = CLogger.getCLogger(MtmUtils.class);
	
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

	public static int getBendingMoment(int length, int fabricProductId, int basebarProductId) {
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
	
	/**
	 * 
	 * @param fabricProductId
	 * @return fabric weight in kg/m^2
	 */
	public static int getfabricWeight(int fabricProductId) {
		//TODO: write method like getBaseBarWeight(int basebarProductId, String trxName)
		//TODO: Better: use attribute weight for all products that require it and use the same method.
		return 0;
	}
	
	
	/**
	 * 
	 * @param basebarProductId
	 * @return basebar weight in kg per metre
	 */
	public static int getBaseBarWeight(int basebarProductId, String trxName) {
		//TODO: modify to handle any product with 'weight' attribute.
		int weight = 0;
		StringBuilder sql = new StringBuilder	("	SELECT value FROM m_attributeinstance ma ");
		sql.append("WHERE ma.m_attributesetinstance_id = ");
		sql.append("(SELECT m_attributesetinstance_id FROM m_product mp WHERE mp.m_product_id = ");
		sql.append(basebarProductId);
		sql.append(") ");
		sql.append("AND ma.m_attribute_id = ");
		sql.append("(SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name = ");
		sql.append("LIKE '%Basebar weight'");
		sql.append("')");
				
		weight = DB.getSQLValueEx(trxName, sql.toString());
		
		return weight;
	}
	
	/**
	 * 
	 * @param length in mm
	 * @param fabricProductId
	 * @param basebarProductId
	 * @return
	 */
	public static int getHangingMass(int width, int length, int fabricProductId, int basebarProductId, String trxName) {
		//hanging mass = fabric weight + basebar weight
		
		
		return 0;
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


public static BigDecimal hasLengthAndWidth(int masi_id) {
	
	StringBuilder sql = new StringBuilder("SELECT ma.name, mai.value ");
	sql.append("FROM m_attribute ma ");
	sql.append("JOIN m_attributeinstance mai ON mai.m_attribute_id = ma.m_attribute_id ");
	sql.append("JOIN m_attributesetinstance masi ON masi.m_attributesetinstance_id = mai.m_attributesetinstance_id ");
	sql.append("WHERE masi.m_attributesetinstance_id = ");
	sql.append(masi_id);
	sql.append(" AND (ma.name LIKE 'Drop' OR ma.name LIKE 'Width');");
	
	RowSet rowset = DB.getRowSet(sql.toString());
	int rowCount = 0;
	int[] rowValues = new int[2];
	
	try{
		while(rowset.next())
		{
			rowValues[rowCount] = rowset.getInt(2);
			rowCount++;
			
			if(rowCount == 2 && rowValues[0] != 0 && rowValues[1] != 0)
			{
				BigDecimal area = new BigDecimal((rowValues[0] * rowValues[1])).setScale(2);
				System.out.println(area);
				BigDecimal divisor = new BigDecimal(1000000);
				BigDecimal result = area.divide(divisor, BigDecimal.ROUND_CEILING);
				return result;
			} 
			
		}
	} catch (SQLException e){
		log.severe("Could not get values from attributeinstance RowSet for width and drop " + e.getMessage());
		e.printStackTrace();
	}
	return Env.ZERO;
	
}

public static BigDecimal hasLength(int masi_id) {
	
	StringBuilder sql = new StringBuilder("SELECT ma.name, mai.value ");
	sql.append("FROM m_attribute ma ");
	sql.append("JOIN m_attributeinstance mai ON mai.m_attribute_id = ma.m_attribute_id ");
	sql.append("JOIN m_attributesetinstance masi ON masi.m_attributesetinstance_id = mai.m_attributesetinstance_id ");
	sql.append("WHERE masi.m_attributesetinstance_id = ");
	sql.append(masi_id);
	sql.append(" AND (ma.name LIKE 'Width%');");
	
	RowSet rowset = DB.getRowSet(sql.toString());
	int rowCount = 0;
	int[] rowValues = new int[2];
	
	try{
		while(rowset.next())
		{
			rowValues[rowCount] = rowset.getInt(2);
			rowCount++;
			
			if(rowCount == 1 && rowValues[0] != 0)
			{
				BigDecimal width = new BigDecimal(rowValues[0]).setScale(2);
				System.out.println("In MtmCallouts.hasLength, width is: " + width);
				return width;
			} 
			
		}
	} catch (SQLException e){
		log.severe("Could not get values from attributeinstance RowSet for width" + e.getMessage());
		e.printStackTrace();
	}
	return Env.ZERO;
	
}
}
