package au.blindmot.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBLDMtmScan extends X_BLD_Mtm_Scan {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8705665536164870396L;

	public MBLDMtmScan(Properties ctx, int BLD_Mtm_Scan_ID, String trxName) {
		super(ctx, BLD_Mtm_Scan_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MBLDMtmScan(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

}
