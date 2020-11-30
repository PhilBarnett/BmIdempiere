package au.blindmot.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBLDProductionLog extends X_BLD_Mtom_Production_Log {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MBLDProductionLog(Properties ctx, int BLD_Mtom_Production_Log_ID, String trxName) {
		super(ctx, BLD_Mtom_Production_Log_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MBLDProductionLog(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

}
