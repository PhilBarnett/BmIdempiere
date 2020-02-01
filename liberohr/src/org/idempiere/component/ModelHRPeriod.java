package org.idempiere.component;

import static org.eevolution.model.I_HR_Period.Table_Name;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.eevolution.model.MHRPeriod;

public class ModelHRPeriod implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		 if (Table_Name.equals(tableName)) {
		     return MHRPeriod.class;
		   } else  
			   return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		if (Table_Name.equals(tableName)) {
		     return new MHRPeriod(Env.getCtx(), Record_ID, trxName);
		   } else  
			   return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		if (Table_Name.equals(tableName)) {
		     return new MHRPeriod(Env.getCtx(), rs, trxName);
		   } else 
			   return null;
	}
}
