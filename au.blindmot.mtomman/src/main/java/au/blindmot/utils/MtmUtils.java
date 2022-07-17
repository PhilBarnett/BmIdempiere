package au.blindmot.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.RowSet;

import org.adempiere.base.Core;
import org.adempiere.base.IProductPricing;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.GridTab;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MBPartner;
import org.compiere.model.MCost;
import org.compiere.model.MDiscountSchema;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MPPProductBOM;
import org.eevolution.model.MPPProductBOMLine;

import au.blindmot.make.Curtain.CurtainConfig;
import au.blindmot.model.I_BLD_MTM_Product_Bom_Trigger;
import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDMtmProductBomTrigger;
import au.blindmot.model.MBLDProductPartType;

public class MtmUtils {

	// Prefixes for bar codes based on table_id - to shorten barcodes
	public static final String MTM_PRODUCTION_PREFIX = "01";
	public static final String MTM_PRODUCTION_ITEM_PREFIX = "02";
	public static final String MTM_PRODUCTION_ASSEMBLEY_ITEM = "03";
	public static final String MTM_HEAD_RAIL_DEDUCTION = "Head Rail Deduction";
	public static final String MTM_FABRIC_DEDUCTION = "Fabric deduction";
	public static final String MTM_FABRIC_ADDITION = "Fabric length addition";
	public static final String MTM_BOTTOM_BAR_DEDUCTION = "Bottom bar addition";
	public static final String MTM_DROP_DEDUCTION = "Drop deduction";
	public static final String MTM_OVERALL_DEDUCTION = "Overall deduction";
	public static final String MTM_WIDTH_ADDITION = "Width addition";
	public static final String MTM_WIDTH_MULTIPLIER = "Width multiplier";
	public static final String MTM_DROP_MULTIPLIER = "Drop multiplier";
	//public static final String MTM_HOOK_CLEARANCE_FF = "Hook clearance face fix";
	//public static final String MTM_HOOK_CLEARANCE_FF_SW = "Hook clearance face fix Swave";
	//public static final String MTM_HOOK_CLEARANCE_TF_SW = "Hook clearance top fix Swave";
	//public static final String MTM_HOOK_CLEARANCE_TF = "Hook clearance top fix";
	//public static final String MTM_FLOOR_CLEARANCE = "Floor Clearance (mm)";
	//public static final String MTM_FULLNESS_LOW = "Fullness low";
	//public static final String MTM_FULLNESS_TARGET = "Fullness target";
	//public static final String MTM_FULLNESS_HIGH = "Fullness high";
	//public static final String MTM_CURTAIN_POSITION = "Curtain position";
	//public static final String MTM_CURTAIN_HEADING = "Heading type";
	//public static final String MTM_CURTAIN_Fit = "Fit";
	//public static final String MTM_CURTAIN_CARRIER_SWAVE = "Swave";
	//public static final String MTM_CURTAIN_CARRIER_SFOLD = "Sfold";
	//public static final String MTM_CURTAIN_CARRIER_STANDARD = "Standard";
	public static final String ROLL_WIDTH = "Roll width";
	//public static final String MTM_HEADER_FF = "Header face fix";
	//public static final String MTM_HEADER_TF = "Header top fix";
	public static final String MTM_CLOCKWISE = "Clockwise";
	public static final String MTM_ANTI_CLOCKWISE = "Anti clockwise";
	public static final String MTM_IS_DUAL = "Is dual";
	public static final String MTM_DROP_DEDUCTION_ADJUST = "Drop deduction adjust";
	public static final String COLUMN_BLD_SELL_PRICE_COPY_ID = "bld_sellprice_copy_id";
	public static final String COLUMN_BLD_COST_PRICE_COPY_ID = "bld_costprice_copy_id";
	public static final String COLUMN_ADDPRICE = "addprice";
	public static final String COLUMN_ADDCOST = "addcost";
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
	if (deduction < 0)log.warning("------getDeduction returned: " + deduction);
	
	return deduction;
	}

/**
 * Returns the area of a made to measure product if it has length and width.
 * @param masi_id
 * @return
 * @throws SQLException 
 */
public static BigDecimal[] getLengthAndWidth(int masi_id) {
	
	StringBuilder sql = new StringBuilder("SELECT ma.name, mai.value ");
	sql.append("FROM m_attribute ma ");
	sql.append("JOIN m_attributeinstance mai ON mai.m_attribute_id = ma.m_attribute_id ");
	sql.append("JOIN m_attributesetinstance masi ON masi.m_attributesetinstance_id = mai.m_attributesetinstance_id ");
	sql.append("WHERE masi.m_attributesetinstance_id = ");
	sql.append(masi_id);
	sql.append(" AND (ma.name LIKE 'Width' OR ma.name LIKE 'Drop') ORDER BY ma.name DESC;");
	
	RowSet rowset = DB.getRowSet(sql.toString());
	int rowCount = 0;
	int[] rowValues = new int[2];
	
	try{
		while(rowset.next())
		{
			rowValues[rowCount] = rowset.getInt(2);
			rowCount++;
		}
			
			if(rowCount == 2 && rowValues[0] != 0 && rowValues[1] != 0)
			{
				BigDecimal area[];
				area = new BigDecimal[] {new BigDecimal(rowValues[0]),new BigDecimal(rowValues[1])};
				System.out.println(area);
				//BigDecimal divisor = new BigDecimal(1000000);
				//BigDecimal result = area.divide(divisor, BigDecimal.ROUND_CEILING);
				try {
					if(!rowset.isClosed())
					{
						rowset.close();
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return area;
			} 
			else if(rowCount == 1 && rowValues[0] != 0)
			{
				BigDecimal area[];
				area = new BigDecimal[] {new BigDecimal(rowValues[0]),new BigDecimal(0)};
				System.out.println(area);
				//BigDecimal divisor = new BigDecimal(1000000);
				//BigDecimal result = area.divide(divisor, BigDecimal.ROUND_CEILING);
				try {
					if(!rowset.isClosed())
					{
						rowset.close();
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return area;
			} 
			
		
		
		
	} catch (SQLException e){
		log.severe("Could not get values from attributeinstance RowSet for width and drop " + e.getMessage());
		e.printStackTrace();
	}
	try {
		if(!rowset.isClosed())
		{
			rowset.close();
		}
	} catch (SQLException e) {
		// TODO Auto-generated catch block
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
				rowset.close();
				return width;
			} 
			rowset.close();
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
	 StringBuilder sql = getSql(attributeType);
	 
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

public static boolean attributeExists(String attributeType) {
	 StringBuilder sql = getSql(attributeType);
		 
		 if(getRowCount(sql.toString()) != 1)
			 {
			 return false;
			 }
		 else
		 {
			 return true;
		 }
	}

/**
 * 
 * @param attributeType
 * @return
 */
private static StringBuilder getSql(String attributeType) {
	 StringBuilder sql = new StringBuilder();
	 sql.append("SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name = '");
	 sql.append(attributeType);
	 sql.append("' ");
	 sql.append("AND m_attribute.isactive = 'Y'");
	 return sql;
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
		 rowset.close();
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
public static MBLDMtmProductBomTrigger[] getMBLDMtmProductBomTrigger(int mProductID) {

	StringBuilder whereClauseFinal = new StringBuilder(MBLDMtmProductBomTrigger.COLUMNNAME_M_Product_ID +"=? ");
	
		String orderClause = MBLDMtmProductBomTrigger.COLUMNNAME_BLD_MTM_Product_Bom_Trigger_ID;
	//
	List<MBLDMtmProductBomTrigger> list = new Query(Env.getCtx(), I_BLD_MTM_Product_Bom_Trigger.Table_Name, whereClauseFinal.toString(), null)
									.setParameters(mProductID)
									.setOrderBy(orderClause)
									.list();
	/*for (MBLDMtmProductBomAdd ol : list) {
		ol.setHeaderInfo(this);
	}*/
	
	return list.toArray(new MBLDMtmProductBomTrigger[list.size()]);	
	//	getLines
}

/**
 * Gets the quantity for various UOM.
 * Assumes width will be in mm
 * @param productToGet
 * @param area2
 * @return
 */
public static BigDecimal getQty(MProduct productToGet, BigDecimal area2, I_C_OrderLine line, String trxn) {
	
	BigDecimal qty = Env.ONE;
	//If the product is the parent, then we want the price per item only
	if(productToGet == null || line == null) return qty;
	//if(area2 != null)
	//{
		if(productToGet.getUOMSymbol().equalsIgnoreCase("sqm") && area2 != null)//it's sqm item, change qty
		{
			return qty = area2;
		}
		else if(productToGet.getUOMSymbol().equalsIgnoreCase("m") || productToGet.getUOMSymbol().equalsIgnoreCase("ml"))//it metres, change qty
		{
			//qty = line.getQtyEntered().divide(BigDecimal.valueOf(1000));
			return qty = MtmUtils.hasLength(line.getM_AttributeSetInstance_ID());/*.divide(BigDecimal.valueOf(1000));*/
		}
		if(productToGet.get_ID() == line.getM_Product_ID())
		{
			if(!productToGet.get_ValueAsBoolean("isgridprice"))
			{
				/*If it's not grid price and it is the orderline product, then we
				 *want the qty as one so the prices are simply read from the price list
				 *without multiplication.
				 */
				return qty;
			}
		}
		/*
		else if(productToGet.getUOMSymbol().equalsIgnoreCase("ml")) //it's millimetres, change qty
		{
			qty = line.getQtyEntered();
			//qty = new BigDecimal((int)MtmUtils.getMattributeInstanceValue(productToGet.get_ID(), "Width", trxn));
		}
		*/
		return qty;
	//}
	//return qty;//TODO: UOM 'Each' is not tested. Ensure orderlines with 'Each' aren't affected.
}
/**
 * Gets the break value for grid priced items.
 * @param params
 * @param mProduct
 * @param M_PriceList_ID
 * @param gTab
 * @param pCtx
 * @return
 */
public static BigDecimal getBreakValue(Object params, MProduct mProduct, int M_PriceList_ID, GridTab gTab, Properties pCtx) {
	//isGridPrice = true;
	//setQtyReadOnly(mTab);//We set the qty to read only so user can't adjust grid price.
	//int mPriceListVersionID = Env.getContextAsInt(ctx, WindowNo, "M_PriceList_Version_ID", true);
	//int M_PriceList_ID = Env.getContextAsInt(pCtx, windowNum, "M_PriceList_ID", true);
	
	log.warning("------MTMUtils getBreakValue(Object params, MProduct mProduct, int M_PriceList_ID, GridTab gTab, Properties pCtx");
	
	if(params != null)
	{
		log.warning("params: " + params.toString());// + ", mProduct: " + mProduct.toString() + ", M_PriceList_ID: " + M_PriceList_ID);
	}
	else
	{
		log.warning("params are null." );
	}
	
	MPriceList pl = MPriceList.get(pCtx, M_PriceList_ID, null);
	int mPriceListVersionID = 0;
	Timestamp date = null;
	if(gTab != null)
	{
		if (gTab.getAD_Table_ID() == I_C_OrderLine.Table_ID)
		date = Env.getContextAsDate(pCtx, "DateOrdered");
	}
	
	else
	{
		date = new Timestamp(System.currentTimeMillis());
	}
	MPriceListVersion plv = pl.getPriceListVersion(date);
	if (plv != null && plv.getM_PriceList_Version_ID() > 0) 
		{
		 	mPriceListVersionID = plv.getM_PriceList_Version_ID();
		}
	
	log.warning("------MTMUtils M_PriceList_Version_ID: " + mPriceListVersionID);
	int mMproductID = mProduct.get_ID();
	log.warning("------MTMUtils mMproductID: " + mMproductID);
	StringBuilder sql = new StringBuilder("SELECT breakvalue FROM m_productpricevendorbreak mb ");
	sql.append("WHERE mb.value_one >= ? AND mb.value_two >= ?");
	sql.append(" AND mb.m_product_id = ");
	sql.append(mMproductID);
	sql.append(" AND mb.m_pricelist_version_id = ");
	sql.append(mPriceListVersionID);
	sql.append(" ORDER BY mb.value_two ASC, mb.value_one ASC");
	sql.append(" FETCH FIRST 1 ROWS ONLY");
	
	BigDecimal breakval = null;
	String stringBreakVal = DB.getSQLValueString(null, sql.toString(), (Object [])params);
	if(stringBreakVal != null)
	{
		breakval = new BigDecimal(stringBreakVal);
	}
	
	if(breakval == null)//No price found, use highest price available
	{
		StringBuilder sql1 = new StringBuilder("SELECT breakvalue FROM m_productpricevendorbreak mb ");
		sql1.append("WHERE mb.value_one = (SELECT MAX(value_one) from m_productpricevendorbreak) ");
		sql1.append("AND value_two = (SELECT MAX(value_two) from m_productpricevendorbreak) ");
		sql1.append("AND mb.m_product_id = ");
		sql1.append(mMproductID);
		sql1.append(" AND mb.m_pricelist_version_id = ?");
		//sql1.append(mPriceListVersionID);
		sql1.append(" FETCH FIRST 1 ROWS ONLY");
		
		String result = DB.getSQLValueString(null, sql1.toString(), mPriceListVersionID);
		if(result!=null)
		{
			breakval = new BigDecimal(result);
			log.warning("---------No price found, use highest price available");
		}
		else
		{
			breakval = Env.ONE;
		}
	
	}
	log.warning("Returning breakvalue: " + breakval);
	return breakval;
}//getBreakValue

/**
 * 
 * @param boolean iSsalesTrx
 * @param BigDecimal area
 * @param ArrayList<Integer> productIDsCheck
 * @param Properties pCtx
 * @param MOrderlIne mOrderLine
 * @param int windowNum
 * @return
 */
public static BigDecimal getCalculatedLineCosts(boolean iSsalesTrx, BigDecimal area, ArrayList<Integer> productIDsCheck, Properties pCtx, I_C_OrderLine mOrderLine, String trxn, int windowNum) {
	BigDecimal calculatedCost = Env.ZERO;
	if(productIDsCheck.size() > 0)
	{
		ArrayList<String> costsMessage = new ArrayList<String>();
		//BigDecimal orderLineProductQty = orderLine.getQtyEntered();
		
		//Get cost for each item in productsToCheck array
		Integer[] productIDsArray = productIDsCheck.toArray(new Integer[productIDsCheck.size()]);
		for(int p = 0; p < productIDsArray.length; p++)
		{
			MProduct productPriceToGet = new MProduct(pCtx, productIDsArray[p].intValue(), null);
			//MOrderLine mOrderLine = new MOrderLine(pCtx, orderLine.getC_OrderLine_ID(), null);
			BigDecimal costProductQty = MtmUtils.getQty(productPriceToGet, area, mOrderLine, trxn);
			BigDecimal returnedPrice = getCalculatedCosts(productPriceToGet, costProductQty, pCtx, mOrderLine, iSsalesTrx);
			if(returnedPrice.compareTo(Env.ZERO)< 0)
			{
				costsMessage.add("No Cost pricing available for: " + productPriceToGet.getName());
				log.warning("Calculating line cost, no Cost pricing available for: " + productPriceToGet.getName());
			}
			else
			{
				calculatedCost = calculatedCost.add(returnedPrice);
				log.warning("Calculating line cost, adding product: " + productPriceToGet.getName() + ", price $" + returnedPrice.toString());
			}	
		}
		if(costsMessage.size() > 0)
		{
			MProduct orderLineProduct = new MProduct(pCtx, mOrderLine.getM_Product_ID(), null);
			log.warning("Costing for" + orderLineProduct.getName() + " is not accurate. The following costs are missing: " + costsMessage);
			try {
				FDialog.warn(windowNum, "Costing for " + orderLineProduct.getName() + " is not accurate. The following costs are missing: " + costsMessage);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	return calculatedCost;
}//getCalculatedLineCosts

/**
 * 
 * @param product
 * @param qty
 * @param pCtx
 * @param orderLine
 * @return
 */
public static BigDecimal getCalculatedCosts(MProduct product, BigDecimal qty, Properties pCtx, I_C_OrderLine mOrderLine, boolean isSalesTrx) {
	
	BigDecimal foundCost = Env.ZERO;
	
	if(product.get_ValueAsBoolean("isgridprice"))
	{
		return getGridPriceProductCost(product, pCtx, mOrderLine.getM_AttributeSetInstance_ID(), mOrderLine , isSalesTrx);
	}
	else
	{
		MPriceList defaultPurchasePriceList =  MPriceList.getDefault(pCtx, false);
		IProductPricing pp = Core.getProductPricing();
		pp.setM_PriceList_ID(defaultPurchasePriceList.getM_PriceList_ID());
		int M_PriceList_Version_ID = defaultPurchasePriceList.getPriceListVersion(null).getM_PriceList_Version_ID();
		pp.setM_PriceList_Version_ID(M_PriceList_Version_ID);
		
		BigDecimal price = Env.ZERO;
		//BigDecimal qty = getQty(mtmProduct, area);
		pp.setInitialValues(product.getM_Product_ID(), mOrderLine.getC_BPartner_ID(), qty, isSalesTrx, null);
		
		//price = pp.getPriceList();
		price = pp.getPriceStd();
		//price = pp.getPriceList().multiply(qty);
		price = pp.getPriceStd().multiply(qty);
	
		if(!(price == Env.ZERO))
		{
			return price;
		}
		//Keep trying
		MAcctSchema[] mAcctSchema = MAcctSchema.getClientAcctSchema(pCtx, Env.getAD_Client_ID(pCtx));
		int acctSchemaID = mAcctSchema[0].get_ID();
		MAcctSchema mSchema = MAcctSchema.get(pCtx, acctSchemaID);
		String costingMethod = mSchema.getCostingMethod();
		int AD_Org_ID = Env.getAD_Org_ID(pCtx);
		BigDecimal retCost = MCost.getCurrentCost(product, 0, mSchema, AD_Org_ID, costingMethod, qty, mOrderLine.getC_OrderLine_ID(), true, null);
		if(retCost != null && retCost.compareTo(Env.ZERO) > 0)
		{
			foundCost = retCost.multiply(qty);
		}
		else
		{
			BigDecimal lastResort = MCost.getSeedCosts(product, 0, mSchema, AD_Org_ID, costingMethod, mOrderLine.getC_OrderLine_ID());
			if(lastResort != null && lastResort.compareTo(Env.ZERO) > 0)
			{
				return lastResort.multiply(qty);
			}
		}
	return foundCost;
	}
	
}//getCalculatedCosts

/**
 * @param MProduct mtmProduct
 * @param Properties pCtx
 * @param int mAttributeSetInstance_ID
 * @param MOrderLine orderLine
 * @return
 */
public static BigDecimal getGridPriceProductCost(MProduct mtmProduct, Properties pCtx, int mAttributeSetInstance_ID, I_C_OrderLine orderLine, boolean isSalesTrx) {
		//get default purchase price list
		MPriceList defaultPurchasePriceList =  MPriceList.getDefault(pCtx, false);
		int productToUseForPrice = 0;
		MProduct priceProduct = mtmProduct;
		//TODO: test that the MProduct mtmProduct parameter is actually a isgridprice
		
		/*TODO: Shared grid pricing
		 * Create products that will be the copy from like Roller Fabric Group 1 Price Grid, tick 'phantom'
		 * Add column to m_product table bld_costprice_copy_id and bld_sellprice_copy_id with appropriate foreign key constraint to m_product_id
		 *
		 * When this method getGridPriceProductCost() is called, it is called on items that 'isgridprice' == true
		 * In this method, perform the following logic:
		 * 1. If the MProduct mtmProduct parameter has a m_product_id in bld_costprice_copy_id or bld_sellprice_copy_id then use that product's price grid.
		 * 2. If the MProduct mtmProduct parameter does not have a 'a m_product_id in bld_costprice_copy_id or bld_sellprice_copy_id then product doesn't exist or it doesn't have a price grid that can be used, 
		 * 	then use the parameter product price grid as default.
		 * 3. If MProduct mtmProduct parameter has the 'Purchase Grid Group', a product and a price grid exists, then lookup and return the found price.
		 * Ensure detailed logging is performed to enable trouble shooting of any possible issues.
		 */
		
		//TODO: check if mtmProduct parameter has a m_product_id in bld_costprice_copy_id or bld_sellprice_copy_id 
		//If it does then create MProduct object and check the grid price
		//If it does not, use the parameter mtmProduct for grid pricing.
		BigDecimal breakvalue = null;
		BigDecimal[] lbw = MtmUtils.getLengthAndWidth(mAttributeSetInstance_ID);
		int bld_Costprice_Copy_ID = mtmProduct.get_ValueAsInt(COLUMN_BLD_COST_PRICE_COPY_ID);
		if(bld_Costprice_Copy_ID > 0)
		{
			//create MProduct object and check the grid price
			MProduct costPriceGridCopy = new MProduct(pCtx, bld_Costprice_Copy_ID, null);
			breakvalue = MtmUtils.getBreakValue(lbw, costPriceGridCopy, defaultPurchasePriceList.getM_PriceList_ID(), null /*gTab*/, pCtx);
			priceProduct = costPriceGridCopy;
		}
		else
		{
			breakvalue = MtmUtils.getBreakValue(lbw, mtmProduct, defaultPurchasePriceList.getM_PriceList_ID(), null /*gTab*/, pCtx);
		}

		//multiply price list at qty by break val: price = pp.getPriceList().multiply(breakvalue);
		
		//Get the relevant pricelist
		IProductPricing pp = Core.getProductPricing();
		pp.setM_PriceList_ID(defaultPurchasePriceList.getM_PriceList_ID());
		int M_PriceList_Version_ID = defaultPurchasePriceList.getPriceListVersion(null).getM_PriceList_Version_ID();
		pp.setM_PriceList_Version_ID(M_PriceList_Version_ID);
		
		BigDecimal price = Env.ZERO;
		//BigDecimal qty = getQty(mtmProduct, area);
		pp.setInitialValues(priceProduct.getM_Product_ID(), orderLine.getC_BPartner_ID(), breakvalue, isSalesTrx, null);
		
		if(breakvalue != null)
		{
			//price = pp.getPriceList();
			price = pp.getPriceStd();
			//price = pp.getPriceList().multiply(breakvalue);
			price = pp.getPriceStd().multiply(breakvalue);
		}
		
		return price;
		
	}//getGridPriceProductCost

public static ArrayList <Integer> getMTMPriceProductIDs(Properties pCtx, MOrderLine mOrderLine) {
	
	//Note that for a new, unsaved record, 'line' will be empty
	//MOrderLine line = new MOrderLine(pCtx, orderLine.getC_OrderLine_ID(), null);
	//int bldInsID = orderLine.get_ValueAsInt("bld_line_productsetinstance_id");
	
	int mProduct_ID = mOrderLine.getM_Product_ID();
	
	ArrayList <Integer> productIDsCheck = new ArrayList<Integer >();
	if(mOrderLine.getM_Product_ID() < 1)//There's no product, return empty ArrayList
	{
		return productIDsCheck;
	}
	//Add the orderline product to the list so the orderline price gets added.
	productIDsCheck.add(Integer.valueOf(mOrderLine.getM_Product_ID()));
	MBLDLineProductInstance[] instance = MBLDLineProductInstance.getmBLDLineProductInstance(mOrderLine.get_ValueAsInt("bld_line_productsetinstance_id"),null);
	//Breakpoint addes below to check this still works from EventHandler
	return processIDs(pCtx, /*productIDsCheck,*/ instance, mProduct_ID, true);
}

public static ArrayList <Integer> getMTMSelectableSellProductIDs(Properties pCtx, I_C_OrderLine orderLine) {
	return getMTMSelectablePartProductIDs(pCtx, orderLine, true);
	
}//getMTMSelectableSellProductIDs

public static ArrayList <Integer> getMTMSelectableCostProductIDs(Properties pCtx, I_C_OrderLine orderLine) {
	return getMTMSelectablePartProductIDs(pCtx, orderLine, false);
}//getMTMSelectableCostProductIDs



/**
 * 	
 * @param Properties pCtx
 * @param MOrderLIne orderLine
 * @return
 */
private static ArrayList <Integer> getMTMSelectablePartProductIDs(Properties pCtx, I_C_OrderLine orderLine, boolean isSell) {
		
		//Note that for a new, unsaved record, 'line' will be empty
		MOrderLine line = new MOrderLine(pCtx, orderLine.getC_OrderLine_ID(), null);
		//int bldInsID = orderLine.get_ValueAsInt("bld_line_productsetinstance_id");
		
		int mProduct_ID = orderLine.getM_Product_ID();
		
		ArrayList <Integer> productIDsCheck = new ArrayList<Integer >();
		if(orderLine.getM_Product_ID() < 1)
		{
			return productIDsCheck;
		}
		
		//Get All the products in the part selection dialogue into an array
		MBLDLineProductInstance[] mBLDLineProductInstance = MBLDLineProductInstance.getmBLDLineProductInstance(line.get_ValueAsInt("bld_line_productsetinstance_id"),null);
				
		//Add the orderline product to the list so the orderline price gets added.
		
		productIDsCheck.add(Integer.valueOf(orderLine.getM_Product_ID()));
		MProduct orderLineProduct = new MProduct(pCtx, orderLine.getM_Product_ID(), null);
		//Only process for grid price products
		//If it's not grid price, send back with just the orderline product. - no extra calculations to be done.
		if(!orderLineProduct.get_ValueAsBoolean("isgridprice"))
		{
			return productIDsCheck;
		}
		
		//Loop through all the products in the part selection dialogue, add any that are 'Select other parttype'.
		MBLDProductPartType[] mBLDProductPartTypes = MBLDProductPartType.getMBLDProductPartTypes(Env.getCtx(), mProduct_ID, null);
		/*mBLDProductPartTypes contains a list of the dialogue part types EG Curtain track, Fabric, Curtain Lining, Curtain bracket
		 *Some of the part types will be 'select other part types' which is a setting that allows items on a parent's BOM
		 *to have items selected from their BOM
		 */
		//Get all selectable parttypes with a Otherbom_M_Parttype_ID
		ArrayList<Integer> otherbomMpartTypeIDs = getSelectOtherMpartTypeIDs(mBLDProductPartTypes);
		
		for(int x = 0; x < mBLDLineProductInstance.length; x++)
		{
			
			MBLDProductPartType instanceMBLDProductPartType = new MBLDProductPartType(Env.getCtx(), mBLDLineProductInstance[x].getBLD_Product_PartType_ID(), null);
			Integer mPartTypeID = instanceMBLDProductPartType.getOtherbomMParttypeID();
			
			//If the instance BLDpartType has one of the otherBomMPartTypeIDs, then add its product to productIDsCheck
			if(otherbomMpartTypeIDs.contains(mPartTypeID))
			{
				//Add the mProductID of the otherBomMPartType
				/*What it needs to do is find the 'other' product on the BLD Line ProductSetInstance Dialogue and add it once?
				 */
				
				
				int instanceProductID = mBLDLineProductInstance[x].getM_Product_ID();//EG bracket on track BOM
				int parentProductIDtoAdd = getParentMproductIDFromOtherInstance(mPartTypeID, mBLDLineProductInstance, instanceProductID);
				if(parentProductIDtoAdd > 0 && !productIDsCheck.contains(parentProductIDtoAdd))//Add parents only once
				{
					productIDsCheck.add(parentProductIDtoAdd);
				}
			}
		}
		
		//Loop through productIDsCheck and create a list
		ArrayList<Integer> returnIDs = new ArrayList<Integer>();
		for(Integer productID : productIDsCheck)
		{
			returnIDs.addAll(processIDs(pCtx, mBLDLineProductInstance, productID, isSell));
		}
		
		returnIDs.add(mProduct_ID);//Add parents
		return returnIDs;
	
	}//getMTMPriceProductIDs

/**
 * 
 * @param otherbomMpartTypeID
 * @param mBLDLineProductInstance
 * @param otherProductID
 * @return
 * Example: otherbomMpartTypeID is 
 */
private static int getParentMproductIDFromOtherInstance(int otherbomMpartTypeID, MBLDLineProductInstance[] mBLDLineProductInstance, int otherProductID) {
	int found = 0;
	int parentProductID = 0;
	MBLDProductPartType instanceMBLDProductPartType = null;
	for(int u = 0; u < mBLDLineProductInstance.length; u++)
	{
		instanceMBLDProductPartType = new MBLDProductPartType(Env.getCtx(), mBLDLineProductInstance[u].getBLD_Product_PartType_ID(), null);
		int instanceMpartTypeID = instanceMBLDProductPartType.getM_PartTypeID();
		if(instanceMpartTypeID == otherbomMpartTypeID)
			{
				found = instanceMpartTypeID;
				parentProductID = mBLDLineProductInstance[u].getM_Product_ID();
				
				break;
			}
	}
	
	//Check that the product on the found instance has the other product on its BOM
	//Get the parent productID
	//int parentProductID = instanceMBLDProductPartType.getM_Product_ID();
	MPPProductBOM defaultBOM = MPPProductBOM.getDefault(MProduct.get(parentProductID), null);
	MPPProductBOMLine[] mPPProductBOMLines = defaultBOM.getLines();
	for(int line = 0; line < mPPProductBOMLines.length; line++)
	{
		int bomProductID = mPPProductBOMLines[line].getM_Product_ID();
		if(bomProductID == otherProductID) return parentProductID;
	}
	
	
	return 0;
}


/**
 * 	
 * @param pCtx
 * @param instance
 * @param parentMproduct_ID
 * @param isSell
 * @return
 */
private static ArrayList <Integer> processIDs(Properties pCtx, MBLDLineProductInstance[] instance, int parentMproduct_ID, boolean isSell) {
		//Get the pp_product_bom_id from the parentMproduct_ID
		ArrayList <Integer> productIDs = new ArrayList<Integer>();
		/*
		StringBuilder sql1 = new StringBuilder("SELECT pp_product_bom_id FROM ");
		sql1.append("pp_product_bom WHERE ");
		sql1.append("m_product_id = ?");
		Object[] params1 = new Object[1];
		params1[0] = parentMproduct_ID;
		int pp_product_bom_id = DB.getSQLValue(null, sql1.toString(), params1);
		*/
		MPPProductBOM defaultBom = MPPProductBOM.getDefault(MProduct.get(parentMproduct_ID), null);
		if (defaultBom != null)
		{
		int pp_product_bom_id = defaultBom.getPP_Product_BOM_ID();
		
		for (int i = 0; i < instance.length; i++)
		{
			//Iterate through products to find ones that are to be added to line amt
			if (instance != null && instance[i].getM_Product_ID() > 0)
			{
				//Get the pp_product_bomline_id for the product we want to check for price or cost add to order line
				//StringBuilder sql = new StringBuilder("SELECT m_product_bom_id FROM ");
				StringBuilder sql = new StringBuilder("SELECT pp_product_bomline_id FROM ");
				//sql.append("m_product_bom WHERE ");
				sql.append("pp_product_bomline WHERE ");
				sql.append("m_product_id = ? ");
				//sql.append("AND m_productbom_id = ?");
				sql.append("AND pp_product_bom_id = ?");
				Object[] params = new Object[2];
				params[0] = instance[i].getM_Product_ID();//Actual productID of the product being checked
				params[1] = pp_product_bom_id;//The BOM ID from the multiple BOMs available for the parent product
				
				
				//params[0] = parentMproduct_ID;
				//params[1] = instance[i].getM_Product_ID();
				
				int pp_product_bomline_id = DB.getSQLValue(null, sql.toString(), params);
				if(pp_product_bomline_id > 0)
				{
					MPPProductBOMLine productToCheck = new MPPProductBOMLine (pCtx, pp_product_bomline_id, null);
					productToCheck.saveEx();
					//TODO: handle isSell logic
					if(isSell)
					{
							if(productToCheck.get_ValueAsBoolean(COLUMN_ADDPRICE))
						{
							//productsToCheck.add(producToCheck);
							//productIDs.add(Integer.valueOf(producToCheck.getM_ProductBOM_ID()));
							productIDs.add(Integer.valueOf(productToCheck.getM_Product_ID()));
						}
					}
					else if(!isSell)
					{
						if(productToCheck.get_ValueAsBoolean(COLUMN_ADDCOST))
						{
							//productsToCheck.add(producToCheck);
							//productIDs.add(Integer.valueOf(producToCheck.getM_ProductBOM_ID()));
							productIDs.add(Integer.valueOf(productToCheck.getM_Product_ID()));
						}
					}
					
				}
			}
		}
	}
		return productIDs;
		
	}
	
	/**
	 * Returns a list of the 'other part type' that was used to select sub BOm items from
	 * in the BLD Line Product SetInstance (product options) dialogue.
	 * @param defaultMPPProductBOMLines
	 * @param parentProductID 
	 * @param mBLDProductPartTypes 
	 * @return
	 */
	public static ArrayList<Integer> getSelectOtherMpartTypeIDs (MBLDProductPartType[] mBLDProductPartTypes) {
		ArrayList<Integer> otherPartTypeIntegers = new ArrayList<Integer>();
		for(int i = 0; i< mBLDProductPartTypes.length; i++)
		{
			int otherMpartTypeID = mBLDProductPartTypes[i].getOtherbomMParttypeID();
			if(otherMpartTypeID > 0)
			{
				otherPartTypeIntegers.add(otherMpartTypeID);
			}
			
		}
		return otherPartTypeIntegers;
		
	}
	
	
	/**
	 * 
	 * @param mProductID
	 * @param M_PriceList_ID
	 * @param pCtx
	 * @param orderLine
	 * @param windowNum
	 * @param l_by_w
	 * @param isSalesTrx
	 * @return
	 */
	public static BigDecimal getListPrice
	(Integer mProductID, int M_PriceList_ID, Properties pCtx, I_C_OrderLine orderLine, int windowNum, BigDecimal[] l_by_w, boolean isSalesTrx) {
		
		IProductPricing pp = Core.getProductPricing();
		pp.setM_PriceList_ID(M_PriceList_ID);
		int M_PriceList_Version_ID = Env.getContextAsInt(pCtx, windowNum, "M_PriceList_Version_ID");
		pp.setM_PriceList_Version_ID(M_PriceList_Version_ID);//TODO:Handle for purchase price list
		
		BigDecimal price = Env.ZERO;
		
		int m_productbom_id = mProductID.intValue();
		MProduct productToGet = new MProduct(pCtx, m_productbom_id, null);
		System.out.println(productToGet.toString());
		//MOrderLine mOrderLine = new MOrderLine(pCtx, orderLine.getC_OrderLine_ID(), null);
		BigDecimal area = null;
		if(l_by_w != null)
		{
			area = l_by_w[0].multiply(l_by_w[1]).divide(new BigDecimal(1000000));
		}
		BigDecimal qty = MtmUtils.getQty(productToGet, area, orderLine, null);
		
		//BigDecimal[] lbw = MtmUtils.hasLengthAndWidth(mAttributeSetInstance_ID);
		if(productToGet.get_ValueAsBoolean("isgridprice"))//it's a grid price product, need to look up price
		{
			

			BigDecimal breakvalue = null;
			//BigDecimal[] lbw = MtmUtils.hasLengthAndWidth(mAttributeSetInstance_ID);
			int bld_Sellprice_Copy_ID = productToGet.get_ValueAsInt(COLUMN_BLD_SELL_PRICE_COPY_ID);
			if(bld_Sellprice_Copy_ID > 0)
			{
				//create MProduct object and check the grid price
				MProduct sellPriceGridCopy = new MProduct(pCtx, bld_Sellprice_Copy_ID , null);
				breakvalue = MtmUtils.getBreakValue(l_by_w, sellPriceGridCopy, pp.getM_PriceList_ID(), null /*gTab*/, pCtx);
				pp.setInitialValues(sellPriceGridCopy.get_ID(), orderLine.getC_BPartner_ID(), breakvalue, isSalesTrx, null);
				//price = pp.getPriceList().multiply(breakvalue);
				price = pp.getPriceStd().multiply(breakvalue);
			}
			else
			{
				//breakvalue = MtmUtils.getBreakValue(l_by_w, mtmProduct, defaultPurchasePriceList.getM_PriceList_ID(), null /*gTab*/, pCtx);
				breakvalue = MtmUtils.getBreakValue(l_by_w, productToGet,  M_PriceList_ID, null, pCtx);
				if(breakvalue != null)
				{
					pp.setInitialValues(m_productbom_id, orderLine.getC_BPartner_ID(), breakvalue, isSalesTrx, null);
					//price = pp.getPriceList().multiply(breakvalue);
					price = pp.getPriceStd().multiply(breakvalue);
					//TODO: Above code gets the list price 8 break value; grid price items need the grid price x break value.
					
					//price = pp.getPriceList();
					//.multiply(breakvalue);
				}
				if(breakvalue.equals(Env.ONE) && productToGet.get_ID() == orderLine.getM_Product_ID())
				{
					try {
						FDialog.warn(windowNum, "No price found at the dimesions entered. Please check the dimesion limits for this product.");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			
			
			return price;
		}
		else
		{
			pp.setInitialValues(m_productbom_id, orderLine.getC_BPartner_ID(), qty, isSalesTrx, null);
			//price = pp.getPriceList().multiply(qty);
			price = pp.getPriceStd().multiply(qty);
			
			return price;
		}
		
	}//getListPrice
	
	/**
	 * Gets drops per curtain for non continuous fabric
	 * @param fabricID
	 * @param curtainID
	 * @param numOfCurtains
	 * @param headingWidth: Total heading width
	 * @param trxName
	 * @return 
	 */
	public static BigDecimal getDropsPerCurtainStd(int fabricID, int curtainID, /*int numOfCurtains,*/ int headingWidthPerCurtain, String trxName) {
		//Get the fullness range, start with a 1/2 drop then go in halves until the right drops is found.
		BigDecimal fullnessTarget = new BigDecimal((String)getMattributeInstanceValue(curtainID, CurtainConfig.ATRRIBUTE_FULLNESS_TARGET.toString(), trxName));
		BigDecimal rollWidth = new BigDecimal((String)getMattributeInstanceValue(fabricID, ROLL_WIDTH, trxName));
		BigDecimal headWidthPerCurtain = new BigDecimal(headingWidthPerCurtain)/*.divide(BigDecimal.valueOf(numOfCurtains))*/;//Heading width PER curtain.
		BigDecimal fullness = Env.ZERO;
		BigDecimal drops = Env.ZERO;
		
		while(fullness.compareTo(fullnessTarget) < 0)
		{
			drops = drops.add(new BigDecimal(0.5));
			fullness = (drops.multiply(rollWidth)).divide(headWidthPerCurtain,2,RoundingMode.HALF_UP);//drops * roll width / width
			
		}
		
		return drops;
		
	}
	
	public static BigDecimal getDropsPerCurtainSWave(int fabricID, /*int curtainID,int numOfCurtains,*/ int headingWidthSwavePerCurtain, String trxName) {
		//Start with a 1/2 drop then go in halves until the right drops is found.
		//BigDecimal fullnessTarget = new BigDecimal((String)getMattributeInstanceValue(curtainID, MTM_FULLNESS_TARGET, trxName));
		BigDecimal rollWidth = new BigDecimal((String)getMattributeInstanceValue(fabricID, ROLL_WIDTH, trxName));
		BigDecimal headWidthPerCurtain = new BigDecimal(headingWidthSwavePerCurtain)/*.divide(BigDecimal.valueOf(numOfCurtains))*/;//Heading width PER curtain.
		BigDecimal calculatedWidth = Env.ZERO;
		BigDecimal drops = Env.ZERO;
		
		while(calculatedWidth.compareTo(headWidthPerCurtain) < 0)
		{
			drops = drops.add(new BigDecimal(0.5));
			calculatedWidth = drops.multiply(rollWidth);//fabricwidth * number of drops
					
			//(drops.multiply(rollWidth)).divide(headWidthPerCurtain,2,RoundingMode.HALF_UP);//drops * roll width / width
			
		}
		
		return drops;
		
	}
	
	
	
	/**
	 * Gets heading width standard (non Swave)curtains. Note this should be called FOR EACH curtain.
	 * @param trackWidth: Track width.
	 * @param sWaveDepth
	 * @param numberOfSwaveCarriers: Should be PER CURTAIN
	 * @return
	 */
	public static double getHeadingWidthStdCarriers(int trackWidth) {
		Double creepage = getCreepage(trackWidth);
		return ((creepage/100)*trackWidth)+trackWidth;
		
	}
	
	/**Gets heading width for swave curtains. Note this should be called FOR EACH curtain.
	 * 
	 * @param sWaveDepth
	 * @param sWaveRunnerCount
	 * @return
	 */
	public static double getHeadingWidthSwave(int sWaveDepth, int sWaveRunnerCount) {
		if(sWaveDepth > 0 && sWaveRunnerCount > 0)
		{
			return Double.valueOf(sWaveDepth * sWaveRunnerCount);
		}
		return 0;
		
	}
	
	
	/**
	 * Gets runner count for curtain carriers
	 * NOTE: This gets the runner count for each curtain then returns the count per curtain * number of curtains. 
	 * The runner count per curtain is always rounded up to even;
	 * To get the runner count per curtain, dividing back to the number of curtains will always get an even number. 
	 * This method is used for both Swave and standard carriers.
	 * Swave curtains MUST have an even number of carriers.
	 * @param trackWidth
	 * @param numOfCurtains 
	 * @param carrierPitch
	 * @return
	 */
	public static Double getTotalRunnerCount(int trackWidth, int numOfCurtains, Double carrierPitch) {
		//Runner count = EVEN((((Creepage%/100)*trackWidth)+trackWidth)/carrierPitch)
		//Creepage = (10*EXP(trackWidth*-0.0035))/0.7
		Double creepage = getCreepage(trackWidth/numOfCurtains);
		Double runnerCountUnRounded = Double.valueOf((((creepage/100)*trackWidth)+trackWidth)/carrierPitch)/numOfCurtains;
		Double runnerCountRoundedUp = Math.ceil(runnerCountUnRounded);
		if(runnerCountRoundedUp % 2 == 0)
		{
			return runnerCountRoundedUp * numOfCurtains;//It's even
		
		}
		else
		{
			return (runnerCountRoundedUp + 1) * numOfCurtains; //Add 1 to make it even.
		}
	}
	
	/** Calculates creepage
	 * Creepage = (10*EXP(trackWidth in centimeters *-0.0035))/0.7
	 * @param trackWidth
	 * @return
	 */
	public static Double getCreepage(int trackWidth) {
		return Double.valueOf((10*Math.exp((trackWidth/10)*-0.0035))/0.7);
	}
	
	/**
	 * Gets new Bom product ID for the parent product. NOTE this will only work when there is only one
	 * pp_product_bom_id per parent. 
	 * @param parentMproduct_ID
	 * @return
	 */
	public static int getActivePPProductBomID(int parentMproduct_ID) {
		StringBuilder sql1 = new StringBuilder("SELECT pp_product_bom_id FROM ");
		sql1.append("pp_product_bom WHERE ");
		sql1.append("m_product_id = ? ");
		sql1.append("AND bomtype = ?");
		Object[] params1 = new Object[2];
		params1[0] = parentMproduct_ID;
		params1[1] = "A";
		return DB.getSQLValue(null, sql1.toString(), params1);
	}

	public static ArrayList<Integer> getOtherParentProductsFromBom(int mProductID, int otherMPartTypeID) {
		MPPProductBOM mPPProductBOM = MPPProductBOM.getDefault(MProduct.get(mProductID), null);
		MPPProductBOMLine[] mPPProductBOMLines = mPPProductBOM.getLines();
		ArrayList<Integer> matchedProductIDs = new ArrayList<Integer>();
		for(int t = 0; t < mPPProductBOMLines.length; t++)
		{
			MProduct lineProduct = MProduct.get(mPPProductBOMLines[t].getM_Product_ID());
			int lineMpartTypeID = lineProduct.getM_PartType_ID();
			if(lineMpartTypeID == otherMPartTypeID) matchedProductIDs.add(lineProduct.getM_Product_ID());
		}
		return matchedProductIDs;
	}
	
}
