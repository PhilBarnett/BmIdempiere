package au.blindmot.mtmcallouts;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.RowSet;

import org.adempiere.base.IColumnCallout;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.utils.MtmUtils;

public class MtmCallouts implements IColumnCallout {

	CLogger log = CLogger.getCLogger(MtmCallouts.class);
	
	
	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {
	
		log.warning("----------In MtmCallouts.start(): " + mField.getColumnName());
		if(value == null || oldValue == null) return"";
		if(mTab == null) return "";
		
		int mProductID = (int) mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID);
		log.warning("----------MProductID: " + mProductID);
		
		if(mTab.getAD_Table_ID() == MOrderLine.Table_ID)
		{
			//If it's an mtm product
			int mProduct_ID = (int) mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID);
			MProduct mProduct = new MProduct(ctx, mProduct_ID, null);
			if(mProduct.get_ValueAsBoolean("ismadetomeasure"))
			{
				if(mField.getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_M_AttributeSetInstance_ID))
				{
					System.out.println("---------It's MASI column.");
					BigDecimal l_by_w = MtmUtils.hasLengthAndWidth((int)value).setScale(2, BigDecimal.ROUND_HALF_EVEN);
					if(l_by_w != Env.ZERO.setScale(2))
					{
						setField(l_by_w, mTab);
						return "";
					}
					if(l_by_w == Env.ZERO.setScale(2))//Check if it has length only
					{
						BigDecimal length = MtmUtils.hasLength((int)value).setScale(2, BigDecimal.ROUND_HALF_EVEN);
						if(length != Env.ZERO.setScale(2))
						{
							setField(length, mTab);
							return "";
						}
					}
					
				}
			}
		}
		
		//If it has length and width
		//Then calculate the m^2 and 
		//Check if the field exists then set the quantity field with the result.'Made to measure' check box in the 
		return "";
	}
	
	private void setField(BigDecimal amount, GridTab mTab) {
		GridField[] fields = mTab.getFields();
		for(int i=0; i<fields.length; i++)
		{
			if(fields[i].getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_QtyEntered)) 
				{
					mTab.setValue(fields[i], amount);
				}
		}
	}

}
