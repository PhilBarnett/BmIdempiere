package au.blindmot.utils;

import java.util.ArrayList;

import org.compiere.util.DB;

public class MtmUtils {
	
//Prefixes for bar codes based on table_id - to shorten barcodes	
public static final String MTM_PRODUCTION_PREFIX = "01";	
public static final String MTM_PRODUCTION_ITEM_PREFIX = "02";
public static final String MTM_PRODUCTION_ASSEMBLEY_ITEM = "03";

	public MtmUtils() {
		// TODO Auto-generated constructor stub
	}
	
public final static String getBarcode(int table_id, int record_id) {
	
	String prefix = getBarcodePrefix(table_id);
	if(prefix!=null) 
		{
			StringBuilder barCode = new StringBuilder(prefix);
			return barCode.append(record_id).toString();
		}
	else return null;
	
}

private static String getBarcodePrefix(int table_id){
	
	StringBuilder sql = new StringBuilder();
	sql.append("SELECT name  ");
	sql.append("FROM ad_table ");
	sql.append("WHERE ad_table_id = ?");
	
	String  tableName = DB.getSQLValueString(null, sql.toString(), table_id);
	String prefix = null;
	
	if(tableName.equalsIgnoreCase("Made to measure production"))
		{
			prefix = MTM_PRODUCTION_PREFIX;
		}
	else if(tableName.equalsIgnoreCase("Made to measure items"))
		{
			prefix = MTM_PRODUCTION_ITEM_PREFIX ;
		}
	else if(tableName.equalsIgnoreCase("Made to measure cuts"))
		{
			prefix = MTM_PRODUCTION_ASSEMBLEY_ITEM; 
		}
	
	
	return prefix;
}

public final static int getBendingMoment(int length, int fabricProductId, int basebarProductId){
	//TODO: get the weight in kg/m^2 of the fabric
	//TODO: get the weight of the base bar 
	/*The bending moment is max in the centre of the beam (blind).
	 * The formula is:
	 * 			wl^2
	 * Mmax = -------
	 * 			 8
	 * Where:
	 * w = kg per lineal metre = total weight of basebar and fabric divided by length
	 * l = length of tube 
	 * Mmax will be in kg-metres
	 * 
	 * See: http://www.totalconstructionhelp.com/deflection.html
	 */
	int moment = 0;
	return moment;

}

public final static int getHeadRailDeduction(ArrayList<Integer> components){
	//SELECT value FROM m_attributeinstance ma WHERE ma.m_attributesetinstance_id = (SELECT m_attributesetinstance_id FROM m_product mp WHERE mp.m_product_id = '1000029') AND ma.m_attribute_id = (SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name = 'Head Rail Deduction');
	 //value above is the deduction.
	
	int deduction = 0;
	for(Integer productId : components)
	{
		
	}
	
	return deduction;
}

}
