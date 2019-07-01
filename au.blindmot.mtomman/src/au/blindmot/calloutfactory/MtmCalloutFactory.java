package au.blindmot.calloutfactory;

import java.util.ArrayList;
import java.util.List;

import org.adempiere.base.IColumnCallout;
import org.adempiere.base.IColumnCalloutFactory;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrderLine;

import au.blindmot.mtmcallouts.MtmCallouts;

public class MtmCalloutFactory implements IColumnCalloutFactory {

	@Override
	public IColumnCallout[] getColumnCallouts(String tableName, String columnName) {
		
		List<IColumnCallout> list = new ArrayList<IColumnCallout>();
		if(tableName.equalsIgnoreCase(MOrderLine.Table_Name) && columnName.equalsIgnoreCase(MOrderLine.COLUMNNAME_M_AttributeSetInstance_ID))
		{
			list.add(new MtmCallouts());
		}
		if(tableName.equalsIgnoreCase(MInvoice.Table_Name) && columnName.equalsIgnoreCase(MInvoice.COLUMNNAME_DateInvoiced))
		{
			list.add(new MtmCallouts());
		}
		if(tableName.equalsIgnoreCase(MInvoice.Table_Name) && columnName.equalsIgnoreCase("lve_poinvoiceno"))
		{
			list.add(new MtmCallouts());
		}
		if(tableName.equalsIgnoreCase(MOrderLine.Table_Name) && columnName.equalsIgnoreCase(MOrderLine.COLUMNNAME_M_Product_ID))
		{
			list.add(new MtmCallouts());
		}
		//
		
		return list != null ? list.toArray(new IColumnCallout[0]) : new IColumnCallout[0];
	}

	
}
