package au.blindmot.utils;

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

}
