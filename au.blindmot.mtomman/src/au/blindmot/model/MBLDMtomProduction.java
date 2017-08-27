package au.blindmot.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBLDMtomProduction extends X_BLD_mtom_production {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2339407400844279640L;

	public MBLDMtomProduction(Properties ctx, int BLD_mtom_production_ID, String trxName) {
		super(ctx, BLD_mtom_production_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MBLDMtomProduction(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

}
