package au.blindmot.eventhandler;

import java.math.BigDecimal;
import java.util.Properties;

import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.util.Env;

public class BLDMOrderLine extends MOrderLine implements I_BM_OrderLine {

	/**
	 * Created to allow for the setting of discounts in orderlines.
	 */
	private static final long serialVersionUID = 5551569046330630807L;
	public int prevMOrderLineID = 0;


	public BLDMOrderLine(Properties ctx, int C_OrderLine_ID, String trxName) {
		super(ctx, C_OrderLine_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	/**************************************************************************
	 * 	Before Save
	 *	@param newRecord
	 *	@return true if it can be saved
	 */
	
	@Override
	protected boolean beforeSave (boolean newRecord)
	{
		return true;
	}
		
	
	public void setDiscount() {
		boolean isgridprice = false;
		MOrderLine copiedOrderLine = null;
		if(prevMOrderLineID !=0)
		{
			copiedOrderLine = new MOrderLine(Env.getCtx(), prevMOrderLineID, get_TrxName());
			MProduct mProduct = new MProduct(Env.getCtx(), copiedOrderLine.getM_Product_ID(),get_TrxName());
			isgridprice = mProduct.get_ValueAsBoolean("isgridprice");
		}
		if(isgridprice)//Copy from previous orderline
		{
			//set discount as per previous line
			if(copiedOrderLine!=null)
			{
				setDiscount(copiedOrderLine.getDiscount());
				BigDecimal LineNetAmt = copiedOrderLine.getLineNetAmt();
				set_ValueNoCheck (COLUMNNAME_LineNetAmt, LineNetAmt);
			}
			
		}
		else
		{
			setLineNetAmt();	//	extended Amount with or without tax
		}
		
	}
	
	
	/**
	 * 	After Save
	 *	@param newRecord new
	 *	@param success success
	 *	@return saved
	 */
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		return success;
	}	//	afterSave
	
	public void setDiscount (BigDecimal Discount)
	{
		set_Value (COLUMNNAME_Discount, Discount);
	}

	public int getPrevMOrderID() {
		return prevMOrderLineID;
	}

	public void setPrevMLineOrderLineID(int prevMOrderLineID) {
		this.prevMOrderLineID = prevMOrderLineID;
	}

	@Override
	public int getBLDLineProductSetInstance_ID() {
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_Line_Productsetinstance_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}
