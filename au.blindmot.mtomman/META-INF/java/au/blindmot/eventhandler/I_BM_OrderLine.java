package au.blindmot.eventhandler;

import java.sql.ResultSet;

public interface I_BM_OrderLine {
	
	 /** Column name BLD_Line_Productsetinstance_ID*/
    public static final String COLUMNNAME_BLD_Line_Productsetinstance_ID = "bld_line_productsetinstance_id";
	
	public int getBLDLineProductSetInstance_ID();

	public int getC_OrderLine_ID();

	public int getM_Product_ID();

}
