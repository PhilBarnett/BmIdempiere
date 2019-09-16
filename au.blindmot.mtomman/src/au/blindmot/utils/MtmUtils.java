package au.blindmot.utils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.RowSet;

import org.compiere.model.I_C_OrderLine;
import org.compiere.model.MBPartner;
import org.compiere.model.MDiscountSchema;
import org.compiere.model.MProduct;
import org.compiere.util.AdempiereUserError;
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
	public static final String MTM_BOTTOM_BAR_DEDUCTION = "Bottom bar addition";
	public static final String MTM_DROP_DEDUCTION = "Drop deduction";
	public static final String MTM_CLOCKWISE = "Clockwise";
	public static final String MTM_ANTI_CLOCKWISE = "Anti clockwise";
	public static final String MTM_IS_DUAL = "Is dual";
	public static final String MTM_DROP_DEDUCTION_ADJUST = "Drop deduction adjust";
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
//TODO: Add transactions to this method
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

	public static BigDecimal getBendingMoment(int length, int width, int fabricProductId, int basebarProductId, String trxName) {
		BigDecimal hangingMass = getHangingMass(width, length, fabricProductId, basebarProductId, trxName);
		BigDecimal moment = Env.ZERO;
		BigDecimal bigWidth = new BigDecimal(width);
		moment = ((hangingMass.divide(bigWidth).multiply(bigWidth).pow(2).divide(new BigDecimal(8))));
		/*
		 * The bending moment is max in the centre of the beam (blind). The
		 * formula is: Mmax = wl^2/8 Where: w = kg per lineal metre =
		 * total weight of basebar and fabric divided by length l = length of
		 * tube Mmax will be in kg-metres
		 * 
		 * See: http://www.totalconstructionhelp.com/deflection.html
		 */
		if(moment.compareTo(Env.ZERO) > 0) return moment;
		return moment;

	}
	
	/**
	 * 
	 * @param length
	 * @param width
	 * @param fabricID
	 * @param trxname
	 * @return
	 */
	public static BigDecimal getFabWeight(int length, int width, int fabricID, String trxname) {
		if(!(fabricID > 0))
		{
			throw new AdempiereUserError("No fabric specified for a line item. is the fabric a mandatory Part Type?");
		}
		
		MProduct mProduct = new MProduct(Env.getCtx(), fabricID, trxname);
		BigDecimal  fabweightsqm = mProduct.getWeight();
		if(fabweightsqm.equals(Env.ZERO))//TODO: Fabric weight defaults
		{
			throw new AdempiereUserError("No Weight for product: " + mProduct.getName() + " " + mProduct.getDescription());
		}
		return new BigDecimal((length/1000) * (width/1000)).multiply(fabweightsqm);
	}
	
	/**
	 * 
	 * @param length
	 * @param productID
	 * @param trxname
	 * @return
	 */
	public static BigDecimal getLengthWeight(int length, int productID, String trxname) {
		MProduct mProduct = new MProduct(Env.getCtx(), productID, trxname);
		BigDecimal  prodWeight = mProduct.getWeight();
		
		if(prodWeight.equals(Env.ZERO))
		{
			throw new AdempiereUserError("No Weight for product: " + mProduct.getName() + " " + mProduct.getDescription());
		}
		return new BigDecimal(length/1000).multiply(prodWeight);
	}
	
	/**
	 * deprecate, refactor delete: 
	 * @param basebarProductId
	 * @return basebar weight in kg per metre
	 */
	/*public static int getWeight(int productId, String trxName) {
		//TODO: modify to handle any product with 'weight' attribute.
		
		int weight = 0;
		if(!(productId > 0))return weight;
		StringBuilder sql = new StringBuilder	("	SELECT value FROM m_attributeinstance ma ");
		sql.append("WHERE ma.m_attributesetinstance_id = ");
		sql.append("(SELECT m_attributesetinstance_id FROM m_product mp WHERE mp.m_product_id = ");
		sql.append(productId);
		sql.append(") ");
		sql.append("AND ma.m_attribute_id = ");
		sql.append("(SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name ");
		sql.append("LIKE '%eight'");
		sql.append(")");
				
		weight = DB.getSQLValueEx(trxName, sql.toString());
		
		return weight;
	}
	*/
	
	/**
	 * 
	 * @param length in mm
	 * @param fabricProductId
	 * @param basebarProductId
	 * @return
	 */
	public static BigDecimal getHangingMass(int width, int length, int fabricProductId, int basebarProductId, String trxName) {
		//hanging mass = fabric weight + basebar weight
		BigDecimal fabWeight = getFabWeight(length, width, fabricProductId, trxName);
		BigDecimal baseBarWeight = getLengthWeight(length, basebarProductId, trxName);
		return fabWeight.add(baseBarWeight);
		
	}
	
	public static String getRotation(String rollTypeIns, String controlSide) {
		log.warning("RollTypeIns: " + rollTypeIns + " ControlSide: " + controlSide);
		if(rollTypeIns==null || controlSide==null)return "";
		if(rollTypeIns.equalsIgnoreCase("NR")||rollTypeIns.equalsIgnoreCase("Normal roll")&&controlSide.equalsIgnoreCase("Left")) return MTM_CLOCKWISE;
		if(rollTypeIns.equalsIgnoreCase("RR")||rollTypeIns.equalsIgnoreCase("Reverse roll")&&controlSide.equalsIgnoreCase("Left")) return MTM_ANTI_CLOCKWISE;
		if(rollTypeIns.equalsIgnoreCase("NR")||rollTypeIns.equalsIgnoreCase("Normal roll")&&controlSide.equalsIgnoreCase("Right")) return MTM_ANTI_CLOCKWISE;
		if(rollTypeIns.equalsIgnoreCase("RR")||rollTypeIns.equalsIgnoreCase("Reverse roll")&&controlSide.equalsIgnoreCase("Right"))return MTM_CLOCKWISE;
		return "";
	}

	public int getDeductions(ArrayList<Integer> components, String deductionType) {

		int totalDeduction = 0;
		for (Integer productId : components) {
			totalDeduction = totalDeduction + getDeduction(productId.intValue(), deductionType);
		}

		return totalDeduction;
	}

	public static int getDeduction (int mProductID, String deductionType) {
	log.warning("mProductID: " + mProductID + "DeductionType: " + deductionType);
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

/**
 * Returns the area of a made to measure product if it has length and width.
 * @param masi_id
 * @return
 */
public static BigDecimal[] hasLengthAndWidth(int masi_id) {
	
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
				BigDecimal area[];
				area = new BigDecimal[] {new BigDecimal(rowValues[0]),new BigDecimal(rowValues[1])};
				System.out.println(area);
				//BigDecimal divisor = new BigDecimal(1000000);
				//BigDecimal result = area.divide(divisor, BigDecimal.ROUND_CEILING);
				return area;
			} 
			
		}
	} catch (SQLException e){
		log.severe("Could not get values from attributeinstance RowSet for width and drop " + e.getMessage());
		e.printStackTrace();
	}
	return null;
	
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

public static Object getMattributeInstanceValue(int mProductID, String mAttributeName, String trxName) {
	
List<Object> params = new ArrayList<Object>();
	params.add(mProductID);
	params.add(mAttributeName);
	StringBuilder sql = new StringBuilder("SELECT mai.value ");
	sql.append("FROM m_product mp ");
	sql.append("JOIN m_attributesetinstance masi ON masi.m_attributesetinstance_id = mp.m_attributesetinstance_id ");
	sql.append("JOIN m_attributeset mas ON mas.m_attributeset_id = masi.m_attributeset_id ");
	sql.append("JOIN m_attributeinstance mai ON mai.m_attributesetinstance_id = masi.m_attributesetinstance_id ");
	sql.append("JOIN m_attribute ma ON ma.m_attribute_id = mai.m_attribute_id ");
	//sql.append("JOIN m_attributevalue mav ON mav.m_attributevalue_id = mai.m_attributevalue_id ");
	sql.append("WHERE mp.m_product_id = ? AND ma.name LIKE ?");
 
 Object object = DB.getSQLValueStringEx(trxName, sql.toString(), params);
 return object;
	}

public static int getattributeWeight(int productId, String trxName) {
	//TODO: modify to handle any product with 'weight' attribute.
	int weight = 0;
	if(!(productId > 0))return weight;
	StringBuilder sql = new StringBuilder	("	SELECT value FROM m_attributeinstance ma ");
	sql.append("WHERE ma.m_attributesetinstance_id = ");
	sql.append("(SELECT m_attributesetinstance_id FROM m_product mp WHERE mp.m_product_id = ");
	sql.append(productId);
	sql.append(") ");
	sql.append("AND ma.m_attribute_id = ");
	sql.append("(SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name ");
	sql.append("LIKE '%eight'");
	sql.append(")");
			
	weight = DB.getSQLValueEx(trxName, sql.toString());
	
	return weight;
	}

public static void attributePreCheck(String attributeType) {
	 StringBuilder sql = new StringBuilder();
	 sql.append("SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name = '");
	 sql.append(attributeType);
	 sql.append("' ");
	 sql.append("AND m_attribute.isactive = 'Y'");
	 
	 if(getRowCount(sql.toString()) != 1)
		 {
		 StringBuilder msg = new StringBuilder("There is NONE or more than one Attribute for the attibute type: ");
		 msg.append(attributeType);
		 msg.append(". Check the Attributes that match or are close to ");
		 msg.append(attributeType);
		 msg.append(" and reconfigure so there is one only.");
		 throw new AdempiereUserError(msg.toString());
		 }
	 
	
	}

private static int getRowCount(String sql) {
	 RowSet rowset = DB.getRowSet(sql.toString());
	 int rowCount = 0;
	 try
	 {
		 while(rowset.next() )
			{
			 rowCount++;
			}
	 }
	 catch (SQLException e)
	 {
		 log.severe("Could not get values for query" + e.getMessage());
			e.printStackTrace();
	 } 
	 return rowCount;
}

public static BigDecimal calculateDiscount(I_C_OrderLine orderLine, int m_M_Product_ID)
{
	//BigDecimal m_PriceStd;
	int m_C_BPartner_ID = orderLine.getC_BPartner_ID();
	if (m_C_BPartner_ID == 0 || m_M_Product_ID == 0)
		return Env.ZERO;
	
	MBPartner bPartner = new MBPartner(Env.getCtx(), orderLine.getC_BPartner_ID(), null);
	MDiscountSchema mDiscountSchema = (MDiscountSchema) bPartner.getM_DiscountSchema();
	boolean isBPDiscount = false;
	if(mDiscountSchema == null) return Env.ZERO;
	
	isBPDiscount = mDiscountSchema.isBPartnerFlatDiscount();
	BigDecimal flatDiscount = Env.ZERO;
	
	if(isBPDiscount)
	{
		flatDiscount = bPartner.getFlatDiscount();
	}
	else
	{
		flatDiscount = mDiscountSchema.getFlatDiscount();
	}
	
	return flatDiscount;
	
  }	//	calculateDiscount

public static String getMPartype(int Mproductid) {
	StringBuilder sql = new StringBuilder("SELECT mpt.name FROM m_parttype mpt ");
	sql.append("JOIN m_product mp ON mp.m_parttype_id = mpt.m_parttype_id ");
	sql.append("WHERE mp.m_product_id = ?");
	
	String mPartType = DB.getSQLValueString(null, sql.toString(), Mproductid);
	if(mPartType != null)
		{
			return mPartType;
		}
	 return "";
	}

}
