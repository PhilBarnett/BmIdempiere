package au.blindmot.mtmcallouts;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;
import java.util.ArrayList;

import org.adempiere.base.Core;
import org.adempiere.base.IColumnCallout;
import org.adempiere.base.IProductPricing;
import org.adempiere.model.GridTabWrapper;
import org.adempiere.webui.window.FDialog;
import org.apache.commons.lang.StringUtils;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.I_C_Invoice;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.MRole;
import org.compiere.model.MUOM;
import org.compiere.model.MUOMConversion;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

import au.blindmot.eventhandler.I_BM_OrderLine;
import au.blindmot.utils.MtmUtils;


public class MtmCallouts implements IColumnCallout {

	CLogger log = CLogger.getCLogger(MtmCallouts.class);
	int windowNum = 0;
	int tabNum = 0;
	GridTab tab = null;
	GridField gridField = null;
	private boolean isGridPrice = false;
	private boolean isSalesTrx;
	BigDecimal breakvalOderLineProduct = null;
	I_C_OrderLine orderLine;
	MProduct orderLineProduct;
	GridTab gTab;
	Properties pCtx;
	Object oldVal;
	ArrayList<String> priceMessage;
	int mAttributeSetInstance_ID = 0;
	BigDecimal area = null;
	BigDecimal[] l_by_w;
	private MOrderLine mOrderLine;
	I_BM_OrderLine iBMOrderLine;
	
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {
	
		windowNum = WindowNo;
		gridField = mField;
		gTab = mTab;
		pCtx = ctx;
		
		log.warning("----------In MtmCallouts.start(), column name: " + mField.getColumnName());
		orderLine = GridTabWrapper.create(mTab, I_C_OrderLine.class);
		MOrder parentOrder1 = new MOrder(ctx, orderLine.getC_Order_ID(), null);
		boolean isSOtrx = parentOrder1.isSOTrx();
		
		if(!isSOtrx)
		{
			log.warning("Parent Order is a purchase order; exiting MtmCallouts");
			return "";//We don't want to mess with Purchase orders.
		}
		
		
		oldVal = oldValue;
		//Setup fields
		BigDecimal listPrice, qty, enteredPrice, discount;
		listPrice = orderLine.getPriceList();
		qty = orderLine.getQtyEntered();
		enteredPrice = orderLine.getPriceEntered();
		discount = orderLine.getDiscount();
		mAttributeSetInstance_ID = orderLine.getM_AttributeSetInstance_ID();
		int pi = orderLine.getM_Product_ID();
		orderLineProduct = new MProduct(pCtx, orderLine.getM_Product_ID(), null);
		mOrderLine = new MOrderLine(pCtx, orderLine.getC_OrderLine_ID(), null);
		BigDecimal sqmMtr = new BigDecimal("1000000");
		l_by_w = MtmUtils.getLengthAndWidth(mAttributeSetInstance_ID);
		if(l_by_w != null) area = l_by_w[0].multiply(l_by_w[1]).setScale(2).divide(sqmMtr);
		iBMOrderLine = GridTabWrapper.create(mTab, I_BM_OrderLine.class);
		//int bldinsId = iBMOrderLine.getBLDLineProductSetInstance_ID();
		if(orderLineProduct.getM_Product_ID() > 1 && mAttributeSetInstance_ID > 1)
		{
			setBreakValOrderLineProduct(orderLineProduct);
		}
		
		
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
			tabNum = mTab.getTabNo();
		}
		
		if(mTab.getAD_Table_ID() == MOrderLine.Table_ID)
		{
			//If it's an mtm product
			if(mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID) != null)//We have a product
			{
			int mProduct_ID = (Integer) mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID);
			log.warning("----------MProductID: " + mProduct_ID);
			MProduct mProduct = new MProduct(ctx, mProduct_ID, null);
			
			isGridPrice = mProduct.get_ValueAsBoolean("isgridprice");
			boolean isMadeToMeasure = isMadeToMeasure (mTab,ctx);
			if(!(isMadeToMeasure || isGridPrice))//Ordinary product, just set the cost.
			{
				ArrayList <Integer> costProductIDsCheck = new ArrayList<Integer>();
				costProductIDsCheck.add(Integer.valueOf(orderLine.getM_Product_ID()));//Add this line to the array
				BigDecimal calculatedCost = MtmUtils.getCalculatedLineCosts(isSalesTrx, area, costProductIDsCheck, pCtx, mOrderLine, null, windowNum);
				mTab.setValue("calculated_cost", calculatedCost.multiply(qty));
			}
			
			if(isMadeToMeasure)
			{
				if(isPriceLocked(mTab) && mField.getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_M_AttributeSetInstance_ID))
				{
					return "";//Don't change qty fields if price is locked - no change to qty, no change to price.
				}
				
				if(mField.getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_M_AttributeSetInstance_ID))
				{
					MOrder parentOrder = new MOrder(pCtx, orderLine.getC_Order_ID(), null);
					int M_PriceList_ID = parentOrder.getM_PriceList_ID();
					//breakval = MtmUtils.getBreakValue((Object)l_by_w, mProduct, M_PriceList_ID, mTab, ctx);
					
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
			
					//String lineID = Env.getContext(ctx, "copypk");
					//int lineID = orderLine.getC_OrderLine_ID();
					
					String context = ctx.toString();
					
					int index;
					while(StringUtils.countMatches(context, "copypk") > 1)
					{
						index = context.indexOf("copypk");
						context = context.substring(index+6);
					}
					
					index = context.indexOf("copypk");
					char[] destArray = new char[7];
					context.getChars(index+7, index+14, destArray, 0);
					//String idstring = String.valueOf(destArray);
					//int orderLineID = Integer.parseInt(String.valueOf(destArray));
					
					//TODO figure out if this is a copied record HINT: the above goes crazy if it's a new record
					
					/*
					 * /TODO: Below needs to be if(isPriceLocked(mTab) && !isNewRecord) tricky bit is to determine if it's a new record
					 * Try and just set field with old value?
					 */
					System.out.println("---------It's MASI column.");
					//l_by_w = MtmUtils.hasLengthAndWidth((Integer)(value));//value here is MAttributeSetInstance_ID
			
					if(l_by_w != null)
						log.warning("------MTM Callouts Length: " + l_by_w[0].toString() + " Width: " + l_by_w[1].toString());	
					{
						
						if(isGridPrice)//Set qty from pricelist - grid pricing.
						{
							/*If it's mtm AND UOM = Each then use the break val to get the actual prices from the pricing table
							 * TODO: change to set the price based on the break valXprice calc, set qty to user entered or 1.
							 */
							
							//log.warning("-------MtmCallouts setting field " + MOrderLine.COLUMNNAME_QtyEntered +  " with: " + breakvalOderLineProduct);
							//setField(breakval, mTab, MOrderLine.COLUMNNAME_QtyEntered);
							amt(Env.getCtx(), windowNum, tab, gridField, value, breakvalOderLineProduct);
							setLocked(true, mTab);
						}
						else //It's a regular non gridprice MTM product
						{ //TODO: check for UOM and if it is a area UOM then continue with this block, otherwise skip.
							//BigDecimal sqmMtr = new BigDecimal("1000000");
							if(l_by_w != null && (l_by_w[0].compareTo(Env.ZERO)==1) && l_by_w[1].compareTo(Env.ZERO)==1)
							{
								BigDecimal area = l_by_w[0].multiply(l_by_w[1]).setScale(2).divide(sqmMtr);
								System.out.println(area);	
								log.warning("-------MtmCallouts setting field with: " + l_by_w.toString());
								setField(area, mTab, MOrderLine.COLUMNNAME_QtyEntered);
								amt(Env.getCtx(), windowNum, tab, gridField, area, null);
								setLocked(true, mTab);
							}
						}
						
						/*
						 * int M_Product_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "M_Product_ID");
						 * int M_PriceList_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "M_PriceList_ID");
						 */
				
					}
					if(l_by_w != null && (l_by_w[0].compareTo(Env.ZERO)==1 && l_by_w[1].compareTo(Env.ZERO)==0))//Check if it has length only
						//TODO: check for UOM and if it is a length UOM then continue with this block, otherwise skip.
					{
						BigDecimal length = MtmUtils.hasLength((Integer)value).setScale(2, BigDecimal.ROUND_HALF_EVEN);
						if(length != Env.ZERO.setScale(2))
						{
							log.warning("--------MtmCallouts setting field with: " + length);
							setField(length, mTab, MOrderLine.COLUMNNAME_QtyEntered);
							//GridFieldVO vo = new GridFieldVO(ctx, mProduct_ID, mProduct_ID, mProduct_ID, mProduct_ID, false);
							//GridField qtyField = new GridField(null);
							amt(Env.getCtx(), windowNum, tab, gridField, length, null);
							setLocked(true, mTab);
						}
					}//public String org.compiere.model.CalloutOrder.amt (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
					
				}
				
				if(mField.getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_Discount))
				{
					amt(ctx, mProduct_ID, mTab, mField, value, breakvalOderLineProduct);
					setLocked(true, mTab);
				}
				if(mField.getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_QtyEntered))
				{
					amt(ctx, mProduct_ID, mTab, mField, value, breakvalOderLineProduct);
					setLocked(true, mTab);
				}
				if(mField.getColumnName().equalsIgnoreCase("bld_line_productsetinstance_id"))
				{
					amt(ctx, mProduct_ID, mTab, mField, value, breakvalOderLineProduct);
					setLocked(true, mTab);
				}
				
			}
				if(mField.getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_M_Product_ID))
				{
					//I_C_OrderLine orderLine = GridTabWrapper.create(mTab, I_C_OrderLine.class);
					//int lineID = orderLine.getC_OrderLine_ID();
					setBLDLineProductSetInstance(null,mTab);//reset if changing product
					
					/*
					MOrderLine mOrderLine = new MOrderLine(Env.getCtx(), lineID, Env.g);
					mOrderLine.set_ValueOfColumn("BLD_Line_ProductSetInstance_ID", null);
					mOrderLine.save();// try getting transaction
					*/
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
			boolean isNew = mTab.isNew();
			String pOInvoiceNo = "";
			pOInvoiceNo = (String) mTab.getValue("lve_poinvoiceno");
			
			if(pOInvoiceNo != "")
			{
				Object bpID = mTab.getValue(MInvoice.COLUMNNAME_C_BPartner_ID);
				Object[] parameters = new Object[2];
				parameters[0] = bpID;
				parameters[1] = pOInvoiceNo;
				int count = 0;
				count = new Query(ctx, I_C_Invoice.Table_Name,
						"c_bpartner_id = ? AND lve_poinvoiceno = ?", null).setParameters(parameters)
						.count();
				if((count == 1 && isNew)||(count > 1))
				{
					FDialog.warn(WindowNo, "PO Invoice No: " + pOInvoiceNo + " has been used before for this Business Partner.");
				}

			}
			
		}
		return "";
	}
	
	private void setField(BigDecimal amount, GridTab mTab, String fieldName) {
		GridField[] fields = mTab.getFields();
		for(int i=0; i<fields.length; i++)
		{
			if(fields[i].getColumnName().equalsIgnoreCase(fieldName)||fields[i].getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_QtyOrdered)) 
				{
					mTab.setValue(fields[i], amount);
				}
			else if(fields[i].getColumnName().equalsIgnoreCase(fieldName)) 
				{
					mTab.setValue(fields[i], amount);
				}
		}
	}
	
	private void setBLDLineProductSetInstance(Object value, GridTab mTab) {
		GridField[] fields = mTab.getFields();
		for(int i=0; i<fields.length; i++)
		{
			if(fields[i].getColumnName().equalsIgnoreCase("BLD_Line_ProductSetInstance_ID"))
				{
					mTab.setValue(fields[i], value);
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
	
	/**
	 * Possibly not required
	 * @param mTab
	 */
	private void setQtyReadOnly(GridTab mTab) {
		GridField[] fields = mTab.getFields();
		for(int i=0; i<fields.length; i++)
		{
			if(fields[i].getColumnName().equalsIgnoreCase("qtyentered"))  
				{
					fields[i].setDisplayed(false);
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
					if(fields[i].getValue() == null || fields[i].getValue() == "N") return isLocked;
					isLocked = (Boolean) fields[i].getValue();
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
	public String amt (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, BigDecimal breakVal)
	{
		ArrayList <Integer> sellProductIDsCheck = MtmUtils.getMTMSelectableSellProductIDs(ctx, orderLine);
		ArrayList <Integer> costProductIDsCheck = MtmUtils.getMTMSelectableCostProductIDs(ctx, orderLine);
		int C_UOM_To_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "C_UOM_ID");
		int M_Product_ID = orderLineProduct.get_ID();
		MOrder parentOrder = new MOrder(pCtx, orderLine.getC_Order_ID(), null);
		int mProduct_ID = (Integer) mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID);
		int M_PriceList_ID = parentOrder.getM_PriceList_ID();/*Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "M_PriceList_ID")*/;
		int StdPrecision = MPriceList.getStandardPrecision(ctx, M_PriceList_ID);
		BigDecimal totalPriceToAdd = BigDecimal.ZERO;
		MPriceList pl = new MPriceList(ctx, M_PriceList_ID, null);
		boolean isEnforcePriceLimit = pl.isEnforcePriceLimit();
		BigDecimal QtyEntered, QtyOrdered, PriceEntered, PriceActual, PriceLimit, Discount, PriceList, PriceStd, bpDiscount;
		IProductPricing pp = Core.getProductPricing();
		
		
		pp.setOrderLine(orderLine, null);
		//pp.setQty(QtyOrdered);
		pp.setM_PriceList_ID(M_PriceList_ID);
		int M_PriceList_Version_ID = Env.getContextAsInt(ctx, WindowNo, "M_PriceList_Version_ID");
		pp.setM_PriceList_Version_ID(M_PriceList_Version_ID);
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
		if (M_Product_ID == 0 && mProduct_ID == 0)
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
			//|| mField.getColumnName().equals("M_AttributeSetInstance_ID")
			|| mField.getColumnName().equals("M_Product_ID"))
			//|| mField.getColumnName().equalsIgnoreCase("bld_line_productsetinstance_id")
			&& !"N".equals(Env.getContext(ctx, WindowNo, "DiscountSchema")))
		{
			if (mField.getColumnName().equals("QtyEntered"))
				QtyOrdered = MUOMConversion.convertProductFrom (ctx, M_Product_ID,
					C_UOM_To_ID, QtyEntered);
			if (QtyOrdered == null)
				QtyOrdered = QtyEntered;
			//I_C_OrderLine orderLine = GridTabWrapper.create(mTab, I_C_OrderLine.class);
			MOrder order = new MOrder(Env.getCtx(), orderLine.getC_Order_ID(), null);
			isSalesTrx = order.isSOTrx();
			
			//pp.setOrderLine(orderLine, null);
			pp.setQty(QtyOrdered);
			//pp.setM_PriceList_ID(M_PriceList_ID);
			//int M_PriceList_Version_ID = Env.getContextAsInt(ctx, WindowNo, "M_PriceList_Version_ID");
			//pp.setM_PriceList_Version_ID(M_PriceList_Version_ID);
			
			//
			PriceEntered = MUOMConversion.convertProductFrom (ctx, M_Product_ID, C_UOM_To_ID, pp.getPriceStd());
			if (PriceEntered == null)
				PriceEntered = pp.getPriceStd();
			//
			
			Discount = MtmUtils.calculateDiscount(orderLine, M_Product_ID);
			if(Discount == Env.ZERO)
			{
				Discount = pp.getDiscount();
			}
			if (log.isLoggable(Level.FINE)) log.fine("QtyChanged -> PriceActual=" + pp.getPriceStd()
				+ ", PriceEntered=" + PriceEntered + ", Discount=" + Discount);
			/*TODO: 
			 * If it mtm & UOM ea
			 * Then bypass
			 * PriceActual = pp.getPriceStd();
			PriceEntered = pp.getPriceStd();
			PriceLimit = pp.getPriceLimit();
			PriceList = pp.getPriceList();
			 *with a cal of the break val * actual, limit & list to get real price.
			 */
			MUOM uom = MUOM.get(ctx, C_UOM_To_ID);
			if(isMadeToMeasure(mTab, ctx) && uom.getName().equalsIgnoreCase("Each") && breakvalOderLineProduct != null && isGridPrice)
			{
				priceMessage = new ArrayList<String>();
				//MProduct orderLineProduct = new MProduct(pCtx, orderLine.getM_Product_ID(), null);
				totalPriceToAdd = getListPrices(sellProductIDsCheck.toArray(new Integer[sellProductIDsCheck.size()]),M_PriceList_ID);
				if(priceMessage.size() > 0)
				{
					FDialog.warn(WindowNo, "Actual price will contain prices for additional products: " + priceMessage.toString() + " less any discounts.");
				}
				
				PriceActual = totalPriceToAdd;
				PriceEntered = totalPriceToAdd;
				PriceLimit = totalPriceToAdd.divide(Env.ONEHUNDRED);//Hard coded, fix at some point.
				PriceList = totalPriceToAdd;
				
				mTab.setValue("PriceList", PriceList);
				mTab.setValue("PriceLimit", PriceLimit);
				mTab.setValue("PriceActual", pp.getPriceStd().multiply(breakvalOderLineProduct));
				mTab.setValue("PriceEntered", PriceEntered );
				mTab.setValue("Discount", Discount);
				//mTab.setValue("PriceEntered", PriceEntered);//Necessary?
				Env.setContext(ctx, WindowNo, "DiscountSchema", pp.isDiscountSchema() ? "Y" : "N");
			}
			else
			{
				PriceActual = pp.getPriceStd();
				PriceEntered = pp.getPriceStd();
				PriceLimit = pp.getPriceLimit();
				PriceList = pp.getPriceList();
				mTab.setValue("PriceList", pp.getPriceList());
				mTab.setValue("PriceLimit", pp.getPriceLimit());
				mTab.setValue("PriceActual", pp.getPriceStd());
				mTab.setValue("PriceEntered", pp.getPriceStd());
				mTab.setValue("Discount", Discount);
				mTab.setValue("PriceEntered", PriceEntered);
				Env.setContext(ctx, WindowNo, "DiscountSchema", pp.isDiscountSchema() ? "Y" : "N");
			}
		}
		else if (mField.getColumnName().equals("PriceActual"))//Not currently triggered by MtmCalloutFactory 
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
		else if (mField.getColumnName().equals("PriceEntered"))////Not currently triggered by MtmCalloutFactory
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
		//Add prices of items in bld_line_productsetinstance to line amt
		if(mField.getColumnName().equalsIgnoreCase("bld_line_productsetinstance_id")
				|| (isMadeToMeasure(mTab, ctx) && mField.getColumnName().equals("M_AttributeSetInstance_ID")))
		{
			/*It must be an mtm product
			 *Get 
			 */
			
			PriceActual = Env.ZERO;
			//BigDecimal qty = Env.ONE;
			//MOrderLine line = new MOrderLine(ctx, orderLine.getC_OrderLine_ID(), null);
		
			if(sellProductIDsCheck.size()>0)
			{
				priceMessage = new ArrayList<String>();
				//MProduct orderLineProduct = new MProduct(pCtx, orderLine.getM_Product_ID(), null);
				totalPriceToAdd = getListPrices(sellProductIDsCheck.toArray(new Integer[sellProductIDsCheck.size()]),M_PriceList_ID);
				if(priceMessage.size() > 0)
				{
					FDialog.warn(WindowNo, "Actual price will contain prices for additional products: " + priceMessage.toString() + " ");
				}
				
				
				/* Set Calculated Cost
				 * Calculated cost = price list cost of any mtmparent + product costs in productsToCheck
				 * In the case of something like a remote control, its just the latest cost.
				 * In the context of here (we have a mtm product with bld_line_productsetinstance products to add to the calculated cost
				 * we get the cost of the orderline item and add the product costs in productsToCheck
				 */
			}
			PriceActual = totalPriceToAdd;
			PriceEntered = totalPriceToAdd;
			mTab.setValue("PriceList", totalPriceToAdd);
			mTab.setValue("PriceLimit", totalPriceToAdd.divide(Env.ONEHUNDRED));//TODO: Change to something like limit = (list/limit) * totalPriceToAdd
			mTab.setValue("PriceActual", totalPriceToAdd);
			mTab.setValue("PriceEntered", totalPriceToAdd);
		}

		//  Discount entered - Calculate Actual/Entered
		if (mField.getColumnName().equals("Discount")/*||mField.getColumnName().equals("M_AttributeSetInstance_ID")*/)		
		{
			/*Note: When the 'Quantity' field is changed by a user, the system changes the discount and triggers this code block.
			 * Also changed by system are 'PriceEntered' and 'PriceList'
			 *This code is to over ride the standard behaviour. Aim is to keep the data as a user would expect.
			 */
			MUOM uom = MUOM.get(ctx, C_UOM_To_ID);
			if(isGridPrice && breakVal != null 
					&& isMadeToMeasure(mTab, ctx) 
					&& uom.getName().equalsIgnoreCase("Each") 
					&& mField.getColumnName().equals("Discount"))
			{
				PriceActual = pp.getPriceStd().multiply(breakVal);
				PriceEntered = pp.getPriceStd().multiply(breakVal);
				PriceLimit = pp.getPriceLimit().multiply(breakVal);
				PriceList = pp.getPriceList().multiply(breakVal);
				
				mTab.setValue("PriceList", pp.getPriceList().multiply(breakVal));
				mTab.setValue("PriceLimit", pp.getPriceLimit().multiply(breakVal));
				mTab.setValue("PriceActual", pp.getPriceStd().multiply(breakVal));
				mTab.setValue("PriceEntered", pp.getPriceStd().multiply(breakVal));
				mTab.setValue("Discount", Discount);
				mTab.setValue("PriceEntered", PriceEntered);//Necessary?
				Env.setContext(ctx, WindowNo, "DiscountSchema", pp.isDiscountSchema() ? "Y" : "N");
			} 
			else if ( PriceList.doubleValue() != 0 )
			{
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
		}
		//	calculate Discount
		else
		{
			if (PriceList.compareTo(Env.ZERO) == 0)
			{
				Discount = Env.ZERO;
			}
			
			else
			if(isMadeToMeasure(mTab, ctx))
				{
					I_C_OrderLine orderLine = GridTabWrapper.create(mTab, I_C_OrderLine.class);
					if(orderLine != null)
					{
						Discount = MtmUtils.calculateDiscount(orderLine, M_Product_ID);
						BigDecimal disc = PriceEntered.multiply(Discount).divide(Env.ONEHUNDRED);
						PriceEntered = PriceEntered.subtract(disc);
						PriceActual = PriceEntered;
						mTab.setValue("PriceEntered", PriceEntered);
						mTab.setValue("PriceActual", PriceEntered);
					}
					
				} 
			else
				Discount = BigDecimal.valueOf((PriceList.doubleValue() - PriceActual.doubleValue()) / PriceList.doubleValue() * 100.0);
			if (Discount.scale() > 2)
				Discount = Discount.setScale(2, BigDecimal.ROUND_HALF_UP);
			mTab.setValue("Discount", Discount);
		}
		if (log.isLoggable(Level.FINE)) log.fine("PriceEntered=" + PriceEntered + ", Actual=" + PriceActual + ", Discount=" + Discount);

		//	Check PriceLimit
		if(!isGridPrice)
		{
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
		}

		//	Line Net Amt
		//PriceActual = PriceActual.add(totalPriceToAdd);//HACK: Is this even right?
		mTab.setValue("PriceActual", PriceActual);
		BigDecimal LineNetAmt = QtyOrdered.multiply(PriceActual);
		if (LineNetAmt.scale() > StdPrecision)
			LineNetAmt = LineNetAmt.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
		if (log.isLoggable(Level.INFO)) log.info("LineNetAmt=" + LineNetAmt);
		mTab.setValue("LineNetAmt", LineNetAmt);
		BigDecimal calculatedCost = MtmUtils.getCalculatedLineCosts(isSalesTrx, area, costProductIDsCheck, pCtx, orderLine, null, windowNum);
		mTab.setValue("calculated_cost", calculatedCost.multiply(QtyEntered));
		//
		return "";
	}	//	amt
	
	/**
	 * Retrieves the list prices of products in the MPPProductBOMLines[] mbomProducts parameter.
	 * @param productIDs
	 * @param qty
	 * @param M_PriceList_ID
	 * @return
	 */
	private BigDecimal getListPrices(Integer[] productIDs, int M_PriceList_ID) {
		//IProductPricing pp = Core.getProductPricing();
		BigDecimal totalToAdd = Env.ZERO;
		MOrder order = new MOrder(Env.getCtx(), orderLine.getC_Order_ID(), null);
		isSalesTrx = order.isSOTrx();
		BigDecimal price = null;
		
		//loop through Bom products and get prices
		//TODO: Handle for curtains - fabric will require a separate calc? Allow for fullness in price?
		
		for(int i = 0; i < productIDs.length; i++)
		{
			int m_productbom_id = productIDs[i].intValue();
			MProduct productToGet = new MProduct(pCtx, m_productbom_id, null);
			price = MtmUtils.getListPrice(productIDs[i], M_PriceList_ID, pCtx, orderLine, windowNum, l_by_w, isSalesTrx);
			//(Integer mProductID, int M_PriceList_ID, Properties pCtx, I_C_OrderLine orderLine, int windowNum, BigDecimal[] l_by_w, boolean isSalesTrx)
			/*
			IProductPricing pp = Core.getProductPricing();
			pp.setM_PriceList_ID(M_PriceList_ID);
			int M_PriceList_Version_ID = Env.getContextAsInt(pCtx, windowNum, "M_PriceList_Version_ID");
			pp.setM_PriceList_Version_ID(M_PriceList_Version_ID);//TODO:Handle for purchase price list
			
			price = Env.ZERO;
			int m_productbom_id = productIDs[i].intValue();
			MProduct productToGet = new MProduct(pCtx, m_productbom_id, null);
			System.out.println(productToGet.toString());
			MOrderLine mOrderLine = new MOrderLine(pCtx, orderLine.getC_OrderLine_ID(), null);
			BigDecimal qty = MtmUtils.getQty(productToGet, area, orderLine, null);
			
			
			//BigDecimal[] lbw = MtmUtils.hasLengthAndWidth(mAttributeSetInstance_ID);
			if(productToGet.get_ValueAsBoolean("isgridprice"))//it's a grid price product, need to look up price
			{
				BigDecimal breakvalue = MtmUtils.getBreakValue(l_by_w, productToGet,  M_PriceList_ID, gTab, pCtx);
				if(breakvalue != null)
				{
					pp.setInitialValues(m_productbom_id, orderLine.getC_BPartner_ID(), breakvalue, isSalesTrx, null);
					price = pp.getPriceList().multiply(breakvalue);
					//price = pp.getPriceList();
					//.multiply(breakvalue);
				}
				if(breakvalue.equals(Env.ONE) && productToGet.get_ID() == orderLineProduct.get_ID())
				{
					FDialog.warn(windowNum, "No price found at the dimesions entered. Please check the dimesion limits for this product. Setting price to: " + breakvalue);
				}
			}
			
			else
			{
				pp.setInitialValues(m_productbom_id, orderLine.getC_BPartner_ID(), qty, isSalesTrx, null);
				price = pp.getPriceList().multiply(qty);
			}
			*/
			if(price != null)
			{
				totalToAdd = totalToAdd.add(price);
				String message = productToGet.getName().concat(" $").concat(price.toString().concat(", "));
				priceMessage.add(message);
			}
			else
			{
				String message = productToGet.getName().concat(": No price found ($0.00 price).").concat(", ");
				priceMessage.add(message);
				log.warning("In MTMCallOuts.getListPrices " + message);
			}
		}
		
		return totalToAdd;
	}//getListPrices

	public boolean isMadeToMeasure (GridTab mTab, Properties ctx) {
			//If it's an mtm product
			if(mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID) != null)
			{
			int mProduct_ID = (Integer) mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID);
			MProduct mProduct = new MProduct(ctx, mProduct_ID, null);
			if(mProduct.get_ValueAsBoolean("ismadetomeasure")) return true;
			}
			return false;
	}
	
	public boolean setBreakValOrderLineProduct(MProduct mProduct) {
		
		BigDecimal[] l_by_w = MtmUtils.getLengthAndWidth(mAttributeSetInstance_ID);
		if(l_by_w != null) log.warning("------MTM Callouts Length: " + l_by_w[0] + " Width: " + l_by_w[1]);
		int M_PriceList_ID = Env.getContextAsInt(pCtx, windowNum, "M_PriceList_ID", true);
		breakvalOderLineProduct = MtmUtils.getBreakValue(l_by_w, mProduct, M_PriceList_ID, gTab, pCtx);
		if(breakvalOderLineProduct != null) return true;
		return false;
	}
	
	
	/**
	 * Gets the quantity for various UOM.
	 * Assumes width will be in mm
	 * @param productToGet
	 * @param area2
	 * @return
	 */
	public BigDecimal getQty(MProduct productToGet, BigDecimal area2) {
		
		BigDecimal qty = Env.ONE;
		//If the product is the parent, then we want the price per item only
		if(productToGet.get_ID() == orderLineProduct.get_ID())
		{
			qty = Env.ONE;
		}
		else if(area2 != null)
		{
			if(productToGet.getUOMSymbol().equalsIgnoreCase("sqm") && area2 != null)//it's sqm item, change qty
			{
				qty = area2;
			}
			else if(productToGet.getUOMSymbol().equalsIgnoreCase("m"))//it metres, change qty
			{
				qty = new BigDecimal((int)MtmUtils.getMattributeInstanceValue(productToGet.get_ID(), "Width", null)).divide(BigDecimal.valueOf(1000));
			}
			else if(productToGet.getUOMSymbol().equalsIgnoreCase("ml")) //it's millimetres, change qty
			{
				qty = new BigDecimal((int)MtmUtils.getMattributeInstanceValue(productToGet.get_ID(), "Width", null));
			}
			return qty;
		}
		return qty;//TODO: UOM 'Each' is not tested. Ensure orderlines with 'Each' aren't affected.
	} 
	
}
