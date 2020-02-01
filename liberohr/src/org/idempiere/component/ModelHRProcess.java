package org.idempiere.component;

import static org.eevolution.model.I_HR_Process.Table_Name;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.eevolution.model.MHRProcess;

public class ModelHRProcess implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		 if (Table_Name.equals(tableName)) {
		     return MHRProcess.class;
		   } else  
			   return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		if (Table_Name.equals(tableName)) {
		     return new MHRProcess(Env.getCtx(), Record_ID, trxName);
		   } else  
			   return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		if (Table_Name.equals(tableName)) {
		     return new MHRProcess(Env.getCtx(), rs, trxName);
		   } else 
			   return null;
	}
}
