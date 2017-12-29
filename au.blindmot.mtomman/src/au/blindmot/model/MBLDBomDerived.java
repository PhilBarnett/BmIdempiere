package au.blindmot.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBLDBomDerived extends X_BLD_mtom_bomderived {

	/**
	 * This MCkass may not be necessary
	 */
	private static final long serialVersionUID = -6034231174542629519L;

	public MBLDBomDerived(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public MBLDBomDerived(Properties ctx, int BLD_mtom_bomderived_ID, String trxName) {
		super(ctx, BLD_mtom_bomderived_ID, trxName);
		// TODO Auto-generated constructor stub
	}

}
