package za.co.ntier.factories;
import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;

import za.co.ntier.model.MzzWoocommerce;
import za.co.ntier.model.MzzWoocommerceMap;
import za.co.ntier.model.MzzWoocommerceMapLine;

public class WooCommerceModelFactory implements IModelFactory {
	
	//MzzWoocommerceMapLine

	public Class<?> getClass(String tableName) {
		

		if(tableName.equalsIgnoreCase(MzzWoocommerceMap.Table_Name))
			return MzzWoocommerceMap.class;
		
		if(tableName.equalsIgnoreCase(MzzWoocommerceMapLine.Table_Name))
			return MzzWoocommerceMapLine.class;
		
		if(tableName.equalsIgnoreCase(MzzWoocommerce.Table_Name))
			return MzzWoocommerce.class;
			
		return null;
	}

	public PO getPO(String tableName, int Record_ID, String trxName) {
		
		if(tableName.equalsIgnoreCase(MzzWoocommerceMap.Table_Name))
		return new MzzWoocommerceMap(Env.getCtx(), Record_ID, trxName);
		
		if(tableName.equalsIgnoreCase(MzzWoocommerceMapLine.Table_Name))
			return new MzzWoocommerceMapLine(Env.getCtx(), Record_ID, trxName);
		
		if(tableName.equalsIgnoreCase(MzzWoocommerce.Table_Name))
			return new MzzWoocommerce(Env.getCtx(), Record_ID, trxName);
		
		return null;
	}

	public PO getPO(String tableName, ResultSet rs, String trxName) {
		
		if(tableName.equalsIgnoreCase(MzzWoocommerceMap.Table_Name))
			return new MzzWoocommerceMap(Env.getCtx(), rs, trxName);
		
		if(tableName.equalsIgnoreCase(MzzWoocommerceMapLine.Table_Name))
			return new MzzWoocommerceMapLine(Env.getCtx(), rs, trxName);
		
		if(tableName.equalsIgnoreCase(MzzWoocommerce.Table_Name))
			return new MzzWoocommerce(Env.getCtx(), rs, trxName);
		
		return null;
	}

}
