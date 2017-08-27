package au.blindmot.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MBLDMtomItemLine extends X_BLD_mtom_item_line {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3382564616366609627L;

	public MBLDMtomItemLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public MBLDMtomItemLine(Properties ctx, int BLD_mtom_item_line_ID, String trxName) {
		super(ctx, BLD_mtom_item_line_ID, trxName);
		// TODO Auto-generated constructor stub
	}

}
