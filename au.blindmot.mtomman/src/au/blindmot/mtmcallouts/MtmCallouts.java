package au.blindmot.mtmcallouts;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.base.Core;
import org.adempiere.base.IColumnCallout;
import org.adempiere.base.IProductPricing;
import org.adempiere.model.GridTabWrapper;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.MRole;
import org.compiere.model.MUOMConversion;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.utils.MtmUtils;

public class MtmCallouts implements IColumnCallout {

	CLogger log = CLogger.getCLogger(MtmCallouts.class);
	int windowNum = 0;
	GridTab tab = null;
	GridField gridField = null;
	
	
	
	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {
	
		log.warning("----------In MtmCallouts.start(): " + mField.getColumnName());
		if(value == null && oldValue != null)
			{
				value = oldValue;
			}
			if(value == null && oldValue == null)
		{
			return"";
		}
		if(mTab == null) 
		{
			return "";
		}
		else
		{
			tab = mTab;
		}
		
		windowNum = WindowNo;
		gridField = mField;
		
		if(mTab.getAD_Table_ID() == MOrderLine.Table_ID)
		{
			//If it's an mtm product
			int mProduct_ID = (int) mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID);
			log.warning("----------MProductID: " + mProduct_ID);
			MProduct mProduct = new MProduct(ctx, mProduct_ID, null);
			if(mProduct.get_ValueAsBoolean("ismadetomeasure"))
			{
				if(mField.getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_M_AttributeSetInstance_ID))
				{
					/*
					 * How to know if there's grid pricing? Check if value_one, value_two are not null? Have an explicitly set column 'isgridprice' in m_product table? Make protocol that all
					 * MTM products have grid pricing - would this work for pelmets?
					*TODO:Add 2 new columns to M_ProductPriceVendorBreak table -> value_one, value_two
					Make the breakvalue column a linear list - 1,2,3,4,5 etc
					Consider value_one the width value_two the drop.
					SELECT the record from the M_ProductPriceVendorBreak table that matches the width and drop entered in the Mattributesetinstance record for the line item.
					Programmatically set the qty as the found price break. The actual price is qty x price so the price is actual divided by the arbitrary qty value.
					The price should auto calculate.
					*
					*TODO:Add virtual column to M_ProductPriceVendorBreak table and field to window to show effective price.
					*TODO: Add column to m_product table 'isgridprice'
					*TODO:Add 2 new columns to M_ProductPriceVendorBreak table -> value_one, value_two
					*TODO: Write a method that returns the qty from the M_ProductPriceVendorBreak based on value_one, value_two. Look through amt() to see how the 
					*price list selection works.
					*Research:
					*int M_PriceList_Version_ID = Env.getContextAsInt(ctx, WindowNo, "M_PriceList_Version_ID");
					*In this method, if the product is a grid price, pick the break value and set the qty field with it.
					*The actual price desired will have to be the result of the qty x price
					*TODO: The value to sent to amt(Env.getCtx(), windowNum, tab, gridField, value) is
					*SELECT breakvalue FROM m_productpricevendorbreak WHERE (value_one >= width AND <= width +1) AND (value_two <= drop <= drop +1)
					*
					* Need to create a way to import the price grids and write the breaks taking into consideration the price x break.
					* ->Check for duplicates for the product.
					*
					*/
					if(isPriceLocked(mTab)) return "";//Don't change qty fields if price is locked - no change to qty, no change to price.
					System.out.println("---------It's MASI column.");
					BigDecimal[] l_by_w = MtmUtils.hasLengthAndWidth((int)value);
					if(l_by_w != null)
					{
						if(mProduct.get_ValueAsBoolean("isgridprice"))//Set qty from pricelist - grid pricing.
						{
							int mPriceListVersionID = Env.getContextAsInt(ctx, WindowNo, "M_PriceList_Version_ID");
							int mMproductID = mProduct.get_ID();
							StringBuilder sql = new StringBuilder("SELECT breakvalue FROM m_productpricevendorbreak mb ");
							sql.append("WHERE mb.value_one >= ? AND mb.value_two >= ? ");
							sql.append("AND mb.m_product_id = ");
							sql.append(mMproductID);
							sql.append(" AND mb.m_pricelist_version_id = ");
							sql.append(mPriceListVersionID);
							sql.append(" FETCH FIRST 1 ROWS ONLY");
							
							BigDecimal breakval = new BigDecimal(DB.getSQLValue(null, sql.toString(), l_by_w[0], l_by_w[1]));
							log.warning("-------MtmCallouts setting field with: " + breakval);
							setField(breakval, mTab);
							amt(Env.getCtx(), windowNum, tab, gridField, breakval);
							setLocked(true, mTab);
						}
						else
						{
							BigDecimal area = l_by_w[0].multiply(l_by_w[1]).setScale(2);
							System.out.println(area);	
							log.warning("-------MtmCallouts setting field with: " + l_by_w);
							setField(area, mTab);
							amt(Env.getCtx(), windowNum, tab, gridField, area);
							setLocked(true, mTab);
						}
						
						/*
						 * int M_Product_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "M_Product_ID");
						 * int M_PriceList_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "M_PriceList_ID");
						 */
					}
					if(l_by_w == null)//Check if it has length only
					{
						BigDecimal length = MtmUtils.hasLength((int)value).setScale(2, BigDecimal.ROUND_HALF_EVEN);
						if(length != Env.ZERO.setScale(2))
						{
							log.warning("--------MtmCallouts setting field with: " + length);
							setField(length, mTab);
							//GridFieldVO vo = new GridFieldVO(ctx, mProduct_ID, mProduct_ID, mProduct_ID, mProduct_ID, false);
							//GridField qtyField = new GridField(null);
							amt(Env.getCtx(), windowNum, tab, gridField, length);
							setLocked(true, mTab);
						}
					}//public String org.compiere.model.CalloutOrder.amt (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
					
				}
			}
		}
		
		//If it has length and width
		//Then calculate the m^2 and 
		//Check if the field exists then set the quantity field with the result.'Made to measure' check box in the 
		if(mTab.getAD_Table_ID() == MInvoice.Table_ID)
		{
			Timestamp dateEntered = (Timestamp) mTab.getValue(MInvoice.COLUMNNAME_DateInvoiced);
			Timestamp now = new Timestamp(System.currentTimeMillis());
			if(dateEntered.after(now))
			{
				FDialog.warn(WindowNo, "Date invoiced is after current date and may be in error.");
			}
			
		}
		return "";
	}
	
	private void setField(BigDecimal amount, GridTab mTab) {
		GridField[] fields = mTab.getFields();
		for(int i=0; i<fields.length; i++)
		{
			if(fields[i].getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_QtyEntered)||fields[i].getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_QtyOrdered)) 
				{
					mTab.setValue(fields[i], amount);
				}
		}
	}
	
	private void setLocked(boolean lockIt, GridTab mTab) {
		GridField[] fields = mTab.getFields();
		for(int i=0; i<fields.length; i++)
		{
			if(fields[i].getColumnName().equalsIgnoreCase("lockprice"))  
				{
					mTab.setValue(fields[i], lockIt);
					break;
				}
		}
	}
	
	private boolean isPriceLocked(GridTab mTab) {
		GridField[] fields = mTab.getFields();
		boolean isLocked = false;
		for(int i=0; i<fields.length; i++)
		{
			if(fields[i].getColumnName().equalsIgnoreCase("lockprice")) 
				{
					isLocked = (boolean) fields[i].getValue();
					break;
				}
		}
		return isLocked;
	}
	/**
	 * Copied from org.compiere.model.CalloutOrder because org.compiere.model.CalloutOrder
	 * can't be called from another callout.
	 * @param ctx
	 * @param WindowNo
	 * @param mTab
	 * @param mField
	 * @param value
	 * @return
	 */
	public String amt (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		
		int C_UOM_To_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "C_UOM_ID");
		int M_Product_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "M_Product_ID");
		int M_PriceList_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "M_PriceList_ID");
		int StdPrecision = MPriceList.getStandardPrecision(ctx, M_PriceList_ID);
		MPriceList pl = new MPriceList(ctx, M_PriceList_ID, null);
		boolean isEnforcePriceLimit = pl.isEnforcePriceLimit();
		BigDecimal QtyEntered, QtyOrdered, PriceEntered, PriceActual, PriceLimit, Discount, PriceList;
		//	get values
			QtyEntered = (BigDecimal)mTab.getValue("QtyEntered");
		if (QtyEntered == null)
			QtyEntered = Env.ZERO;
		
		QtyOrdered = (BigDecimal)mTab.getValue("QtyOrdered");
		if (QtyOrdered == null)
			QtyOrdered = Env.ZERO;
		if (log.isLoggable(Level.FINE)) log.fine("QtyEntered=" + QtyEntered + ", Ordered=" + QtyOrdered + ", UOM=" + C_UOM_To_ID);
		//
		PriceEntered = (BigDecimal)mTab.getValue("PriceEntered");
		PriceActual = (BigDecimal)mTab.getValue("PriceActual");
		Discount = (BigDecimal)mTab.getValue("Discount");
		PriceLimit = (BigDecimal)mTab.getValue("PriceLimit");
		PriceList = (BigDecimal)mTab.getValue("PriceList");
		if (log.isLoggable(Level.FINE)){
			log.fine("PriceList=" + PriceList + ", Limit=" + PriceLimit + ", Precision=" + StdPrecision);
			log.fine("PriceEntered=" + PriceEntered + ", Actual=" + PriceActual + ", Discount=" + Discount);
		}

		//		No Product
		if (M_Product_ID == 0)
		{
			// if price change sync price actual and entered
			// else ignore
			if (mField.getColumnName().equals("PriceActual"))
			{
				PriceEntered = (BigDecimal) value;
				mTab.setValue("PriceEntered", value);
			}
			else if (mField.getColumnName().equals("PriceEntered"))
			{
				PriceActual = (BigDecimal) value;
				mTab.setValue("PriceActual", value);
			}
		}
		//	Product Qty changed - recalc price
		else if ((mField.getColumnName().equals("QtyOrdered")
			|| mField.getColumnName().equals("QtyEntered")
			|| mField.getColumnName().equals("C_UOM_ID")
			|| mField.getColumnName().equals("M_AttributeSetInstance_ID")
			|| mField.getColumnName().equals("M_Product_ID"))
			&& !"N".equals(Env.getContext(ctx, WindowNo, "DiscountSchema")))
		{
			if (mField.getColumnName().equals("QtyEntered"))
				QtyOrdered = MUOMConversion.convertProductFrom (ctx, M_Product_ID,
					C_UOM_To_ID, QtyEntered);
			if (QtyOrdered == null)
				QtyOrdered = QtyEntered;
			I_C_OrderLine orderLine = GridTabWrapper.create(mTab, I_C_OrderLine.class);
			IProductPricing pp = Core.getProductPricing();
			pp.setOrderLine(orderLine, null);
			pp.setQty(QtyOrdered);
			pp.setM_PriceList_ID(M_PriceList_ID);
			int M_PriceList_Version_ID = Env.getContextAsInt(ctx, WindowNo, "M_PriceList_Version_ID");
			pp.setM_PriceList_Version_ID(M_PriceList_Version_ID);
			//
			PriceEntered = MUOMConversion.convertProductFrom (ctx, M_Product_ID,
				C_UOM_To_ID, pp.getPriceStd());
			if (PriceEntered == null)
				PriceEntered = pp.getPriceStd();
			//
			if (log.isLoggable(Level.FINE)) log.fine("QtyChanged -> PriceActual=" + pp.getPriceStd()
				+ ", PriceEntered=" + PriceEntered + ", Discount=" + pp.getDiscount());
			PriceActual = pp.getPriceStd();
			PriceEntered = pp.getPriceStd();
			Discount = pp.getDiscount();
			PriceLimit = pp.getPriceLimit();
			PriceList = pp.getPriceList();
			mTab.setValue("PriceList", pp.getPriceList());
			mTab.setValue("PriceLimit", pp.getPriceLimit());
			mTab.setValue("PriceActual", pp.getPriceStd());
			mTab.setValue("PriceEntered", pp.getPriceStd());
			mTab.setValue("Discount", pp.getDiscount());
			mTab.setValue("PriceEntered", PriceEntered);
			Env.setContext(ctx, WindowNo, "DiscountSchema", pp.isDiscountSchema() ? "Y" : "N");
		}
		else if (mField.getColumnName().equals("PriceActual"))
		{
			PriceActual = (BigDecimal)value;
			PriceEntered = MUOMConversion.convertProductFrom (ctx, M_Product_ID,
				C_UOM_To_ID, PriceActual);
			if (PriceEntered == null)
				PriceEntered = PriceActual;
			//
			if (log.isLoggable(Level.FINE)) log.fine("PriceActual=" + PriceActual
				+ " -> PriceEntered=" + PriceEntered);
			mTab.setValue("PriceEntered", PriceEntered);
		}
		else if (mField.getColumnName().equals("PriceEntered"))
		{
			PriceEntered = (BigDecimal)value;
			PriceActual = MUOMConversion.convertProductTo (ctx, M_Product_ID,
				C_UOM_To_ID, PriceEntered);
			if (PriceActual == null)
				PriceActual = PriceEntered;
			//
			if (log.isLoggable(Level.FINE)) log.fine("PriceEntered=" + PriceEntered
				+ " -> PriceActual=" + PriceActual);
			mTab.setValue("PriceActual", PriceActual);
		}

		//  Discount entered - Calculate Actual/Entered
		if (mField.getColumnName().equals("Discount"))
		{
			if ( PriceList.doubleValue() != 0 )
				PriceActual = BigDecimal.valueOf((100.0 - Discount.doubleValue()) / 100.0 * PriceList.doubleValue());
			if (PriceActual.scale() > StdPrecision)
				PriceActual = PriceActual.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
			PriceEntered = MUOMConversion.convertProductFrom (ctx, M_Product_ID,
				C_UOM_To_ID, PriceActual);
			if (PriceEntered == null)
				PriceEntered = PriceActual;
			mTab.setValue("PriceActual", PriceActual);
			mTab.setValue("PriceEntered", PriceEntered);
		}
		//	calculate Discount
		else
		{
			if (PriceList.compareTo(Env.ZERO) == 0)
				Discount = Env.ZERO;
			else
				Discount = BigDecimal.valueOf((PriceList.doubleValue() - PriceActual.doubleValue()) / PriceList.doubleValue() * 100.0);
			if (Discount.scale() > 2)
				Discount = Discount.setScale(2, BigDecimal.ROUND_HALF_UP);
			mTab.setValue("Discount", Discount);
		}
		if (log.isLoggable(Level.FINE)) log.fine("PriceEntered=" + PriceEntered + ", Actual=" + PriceActual + ", Discount=" + Discount);

		//	Check PriceLimit
		String epl = Env.getContext(ctx, WindowNo, "EnforcePriceLimit");
		boolean enforce = Env.isSOTrx(ctx, WindowNo) && epl != null && !epl.equals("") ? epl.equals("Y") : isEnforcePriceLimit;
		if (enforce && MRole.getDefault().isOverwritePriceLimit())
			enforce = false;
		//	Check Price Limit?
		if (enforce && PriceLimit.doubleValue() != 0.0
		  && PriceActual.compareTo(PriceLimit) < 0)
		{
			PriceActual = PriceLimit;
			PriceEntered = MUOMConversion.convertProductFrom (ctx, M_Product_ID,
				C_UOM_To_ID, PriceLimit);
			if (PriceEntered == null)
				PriceEntered = PriceLimit;
			if (log.isLoggable(Level.FINE)) log.fine("(under) PriceEntered=" + PriceEntered + ", Actual" + PriceLimit);
			mTab.setValue ("PriceActual", PriceLimit);
			mTab.setValue ("PriceEntered", PriceEntered);
			mTab.fireDataStatusEEvent ("UnderLimitPrice", "", false);
			//	Repeat Discount calc
			if (PriceList.compareTo(Env.ZERO) != 0)
			{
				Discount = BigDecimal.valueOf((PriceList.doubleValue () - PriceActual.doubleValue ()) / PriceList.doubleValue () * 100.0);
				if (Discount.scale () > 2)
					Discount = Discount.setScale (2, BigDecimal.ROUND_HALF_UP);
				mTab.setValue ("Discount", Discount);
			}
		}

		//	Line Net Amt
		BigDecimal LineNetAmt = QtyOrdered.multiply(PriceActual);
		if (LineNetAmt.scale() > StdPrecision)
			LineNetAmt = LineNetAmt.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
		if (log.isLoggable(Level.INFO)) log.info("LineNetAmt=" + LineNetAmt);
		mTab.setValue("LineNetAmt", LineNetAmt);
		//
		return "";
	}	//	amt

}
