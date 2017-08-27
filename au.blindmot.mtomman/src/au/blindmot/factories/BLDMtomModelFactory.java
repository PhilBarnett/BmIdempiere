package au.blindmot.factories;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;

import au.blindmot.model.MBLDMtomItemLine;
import au.blindmot.model.MBLDMtomProduction;

public class BLDMtomModelFactory implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		

		if(tableName.equalsIgnoreCase(MBLDMtomItemLine.Table_Name))
			return MBLDMtomItemLine.class;
			
		if(tableName.equalsIgnoreCase(MBLDMtomProduction.Table_Name))
			return MBLDMtomItemLine.class;
		
		
		return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		
		if(tableName.equalsIgnoreCase(MBLDMtomItemLine.Table_Name))
		return new MBLDMtomItemLine(Env.getCtx(), Record_ID, trxName);
		
		if(tableName.equalsIgnoreCase(MBLDMtomProduction.Table_Name))
			return new MBLDMtomItemLine(Env.getCtx(), Record_ID, trxName);
			
			
		return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		
		if(tableName.equalsIgnoreCase(MBLDMtomItemLine.Table_Name))
			return new MBLDMtomItemLine(Env.getCtx(), rs, trxName);
			
		if(tableName.equalsIgnoreCase(MBLDMtomProduction.Table_Name))
			return new MBLDMtomItemLine(Env.getCtx(), rs, trxName);
		
		return null;
	}

}
