package au.blindmot.mtmcallouts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import org.compiere.model.MAcctSchema;
import org.compiere.model.MBOMProduct;
import org.compiere.model.MCost;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MProductBOM;
import org.compiere.model.MRole;
import org.compiere.model.MUOM;
import org.compiere.model.MUOMConversion;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDProductPartType;
import au.blindmot.utils.MtmUtils;


public class MtmCallouts implements IColumnCallout {

	CLogger log = CLogger.getCLogger(MtmCallouts.class);
	int windowNum = 0;
	int tabNum = 0;
	GridTab tab = null;
	GridField gridField = null;
	private boolean isGridPrice = false;
	private boolean isSalesTrx;
	BigDecimal breakval = null;
	I_C_OrderLine orderLine;
	GridTab gTab;
	Properties pCtx;
	Object oldVal;
	ArrayList<String> priceMessage;
	int mAttributeSetInstance_ID = 0;
	BigDecimal area = null;
	
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {
	
		log.warning("----------In MtmCallouts.start(): " + mField.getColumnName());
		orderLine = GridTabWrapper.create(mTab, I_C_OrderLine.class);
		oldVal = oldValue;
		//Setup fields
		BigDecimal listPrice, qty, enteredPrice, discount;
		listPrice = orderLine.getPriceList();
		qty = orderLine.getQtyEntered();
		enteredPrice = orderLine.getPriceEntered();
		discount = orderLine.getDiscount();
		mAttributeSetInstance_ID = orderLine.getM_AttributeSetInstance_ID();
		
		BigDecimal sqmMtr = new BigDecimal("1000000");
		BigDecimal[] lbw = MtmUtils.hasLengthAndWidth(mAttributeSetInstance_ID);
		if(lbw != null) area = lbw[0].multiply(lbw[1]).setScale(2).divide(sqmMtr);
		
		
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
		
		windowNum = WindowNo;
		gridField = mField;
		gTab = mTab;
		pCtx = ctx;
		
		if(mTab.getAD_Table_ID() == MOrderLine.Table_ID)
		{
			//If it's an mtm product
			if(mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID) != null)//We have a product
			{
			int mProduct_ID = (Integer) mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID);
			log.warning("----------MProductID: " + mProduct_ID);
			MProduct mProduct = new MProduct(ctx, mProduct_ID, null);
			int MAttribSetIns_ID = orderLine.getM_AttributeSetInstance_ID();
			BigDecimal[] l_by_w = MtmUtils.hasLengthAndWidth(MAttribSetIns_ID);
			if(l_by_w != null)
			{
				setBreakVal(mProduct);
			}
			
			isGridPrice = mProduct.get_ValueAsBoolean("isgridprice");
			if(isMadeToMeasure (mTab,ctx))
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
					if(isPriceLocked(mTab))
						{
							return "";//Don't change qty fields if price is locked - no change to qty, no change to price.
						}
					System.out.println("---------It's MASI column.");
					l_by_w = MtmUtils.hasLengthAndWidth((Integer)(value));//value here is MAttributeSetInstance_ID
			
					if(l_by_w != null)
						log.warning("------MTM Callouts Length: " + l_by_w[0] + " Width: " + l_by_w[1]);	
					{
						if(isGridPrice)//Set qty from pricelist - grid pricing.
						{
							//breakval = getBreakValue((Object)l_by_w, mProduct);
							
							
							/*If it's mtm AND UOM = Each then use the break val to get the actual prices from the pricing table
							 * TODO: change to set the price based on the break valXprice calc, set qty to user entered or 1.
							 */
							
							
							log.warning("-------MtmCallouts setting field " + MOrderLine.COLUMNNAME_QtyEntered +  " with: " + breakval);
							//setField(breakval, mTab, MOrderLine.COLUMNNAME_QtyEntered);
							amt(Env.getCtx(), windowNum, tab, gridField, value, breakval);
							setLocked(true, mTab);
						}
						else //It's a regular non gridprice MTM product
						{ //TODO: check for UOM and if it is a area UOM then continue with this block, otherwise skip.
							//BigDecimal sqmMtr = new BigDecimal("1000000");
							if(l_by_w != null)
							{
								BigDecimal area = l_by_w[0].multiply(l_by_w[1]).setScale(2).divide(sqmMtr);
								System.out.println(area);	
								log.warning("-------MtmCallouts setting field with: " + l_by_w);
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
					if(l_by_w == null)//Check if it has length only
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
					amt(ctx, mProduct_ID, mTab, mField, value, breakval);
					setLocked(true, mTab);
				}
				if(mField.getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_QtyEntered))
				{
					amt(ctx, mProduct_ID, mTab, mField, value, breakval);
					setLocked(true, mTab);
				}
				if(mField.getColumnName().equalsIgnoreCase("bld_line_productsetinstance_id"))
				{
					amt(ctx, mProduct_ID, mTab, mField, value, breakval);
					setLocked(true, mTab);
				}
				
			}
				if(mField.getColumnName().equalsIgnoreCase(MOrderLine.COLUMNNAME_M_Product_ID))
				{
					//I_C_OrderLine orderLine = GridTabWrapper.create(mTab, I_C_OrderLine.class);
					//int lineID = orderLine.getC_OrderLine_ID();
					setBLDLineProductSetInstance(null,mTab);
					
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
		
		int C_UOM_To_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "C_UOM_ID");
		int M_Product_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "M_Product_ID");
		int mProduct_ID = (Integer) mTab.getValue(MOrderLine.COLUMNNAME_M_Product_ID);
		int M_PriceList_ID = Env.getContextAsInt(ctx, WindowNo, mTab.getTabNo(), "M_PriceList_ID");
		int StdPrecision = MPriceList.getStandardPrecision(ctx, M_PriceList_ID);
		BigDecimal totalPriceToAdd = BigDecimal.ZERO;
		MPriceList pl = new MPriceList(ctx, M_PriceList_ID, null);
		boolean isEnforcePriceLimit = pl.isEnforcePriceLimit();
		BigDecimal QtyEntered, QtyOrdered, PriceEntered, PriceActual, PriceLimit, Discount, PriceList, bpDiscount;
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
			|| mField.getColumnName().equals("M_AttributeSetInstance_ID")
			|| mField.getColumnName().equals("M_Product_ID"))
			|| mField.getColumnName().equalsIgnoreCase("bld_line_productsetinstance_id")
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
			PriceEntered = MUOMConversion.convertProductFrom (ctx, M_Product_ID,
				C_UOM_To_ID, pp.getPriceStd());
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
			if(isMadeToMeasure(mTab, ctx) && uom.getName().equalsIgnoreCase("Each") && breakVal != null && isGridPrice)
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
		if(mField.getColumnName().equalsIgnoreCase("bld_line_productsetinstance_id"))
		{
			BigDecimal qty = Env.ONE;
			MOrderLine line = new MOrderLine(ctx, orderLine.getC_OrderLine_ID(), null);
			ArrayList <MProductBOM> productsToCheck = new ArrayList<MProductBOM >();
			//Get the products for this Order line
			MBLDLineProductInstance[] instance = MBLDProductPartType.getmBLDLineProductInstance(line.get_ValueAsInt("bld_line_productsetinstance_id"),null);
			for (int i = 0; i < instance.length; i++)
			{
				//Iterate through products to find ones that are to be added to line amt
				if (instance != null && instance[i].getM_Product_ID() > 0)
				{
					StringBuilder sql = new StringBuilder("SELECT m_product_bom_id FROM ");
					sql.append("m_product_bom WHERE ");
					sql.append("m_product_id = ? ");
					sql.append("AND m_productbom_id = ?");
					Object[] params = new Object[2];
					params[0] = mProduct_ID;
					params[1] = instance[i].getM_Product_ID();
					int m_product_bom_id = DB.getSQLValue(null, sql.toString(), params);
					if(m_product_bom_id > 0)
					{
						MProductBOM producToCheck = new MProductBOM (ctx, m_product_bom_id, null);
						producToCheck.saveEx();
						if(producToCheck.get_ValueAsBoolean("addprice"))
						{
							productsToCheck.add(producToCheck);
						}
					}
				}
			}
			if(productsToCheck.size()>0)
			{
				priceMessage = new ArrayList<String>();
				//
				totalPriceToAdd = getListPrices(productsToCheck.toArray(new MProductBOM[productsToCheck.size()]),qty,M_PriceList_ID);
				FDialog.warn(WindowNo, "Actual price will contain prices for additional products: " + priceMessage.toString() + " ");
				//Set Calculated Cost
				BigDecimal calculatedCost = Env.ZERO;
				MProductBOM[] bomArray = (MProductBOM[]) productsToCheck.toArray();
				for(int p = 0; p < bomArray.length; p++)
				{
					MProduct productPriceToGet = new MProduct(pCtx, bomArray[p].getM_ProductBOM_ID(), null);
					calculatedCost.add(getCalculatedCosts(productPriceToGet, qty));
				}
				
			}
			
			
		}

		//  Discount entered - Calculate Actual/Entered
		if (mField.getColumnName().equals("Discount")/*||mField.getColumnName().equals("M_AttributeSetInstance_ID")*/)		
		{
			/*Note: When the 'Quantity' field is changed by a user, the system changes the discount and triggers this code block.
			 * Also changed by system are 'PriceEntered' and 'PriceList'
			 *This code is to over ride the standard behaviour. Aim is to keep the data as a user would expect.
			 */
			MUOM uom = MUOM.get(ctx, C_UOM_To_ID);
			if(isGridPrice && breakVal != null && isMadeToMeasure(mTab, ctx) && uom.getName().equalsIgnoreCase("Each") && mField.getColumnName().equals("Discount"))
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
			/*
			else
			if(isGridPrice)
				{
					I_C_OrderLine orderLine = GridTabWrapper.create(mTab, I_C_OrderLine.class);
					if(orderLine != null)
					{
						Discount = calculateDiscount(orderLine, M_Product_ID, isSalesTrx);
					}
					
				} */
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
		PriceActual = PriceActual.add(totalPriceToAdd);
		mTab.setValue("PriceActual", PriceActual);
		BigDecimal LineNetAmt = QtyOrdered.multiply(PriceActual);
		if (LineNetAmt.scale() > StdPrecision)
			LineNetAmt = LineNetAmt.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);
		if (log.isLoggable(Level.INFO)) log.info("LineNetAmt=" + LineNetAmt);
		mTab.setValue("LineNetAmt", LineNetAmt);
		//
		return "";
	}	//	amt
	
	private BigDecimal getListPrices(MProductBOM[] mbomProducts, BigDecimal qty, int M_PriceList_ID) {
		//IProductPricing pp = Core.getProductPricing();
		BigDecimal totalToAdd = Env.ZERO;
		MOrder order = new MOrder(Env.getCtx(), orderLine.getC_Order_ID(), null);
		isSalesTrx = order.isSOTrx();
		//pp.setQty(qty);
		BigDecimal price = null;
		
		
		
		//loop through Bom products and get prices
		//TODO: Handle for curtains - fabric will require a separate calc? Allow for fullness in price?
		for(int i = 0; i < mbomProducts.length; i++)
		{
			IProductPricing pp = Core.getProductPricing();
			pp.setM_PriceList_ID(M_PriceList_ID);
			int M_PriceList_Version_ID = Env.getContextAsInt(pCtx, windowNum, "M_PriceList_Version_ID");
			pp.setM_PriceList_Version_ID(M_PriceList_Version_ID);//TODO:Handle for purchase price list
			
			price = Env.ZERO;
			int m_productbom_id = mbomProducts[i].get_ValueAsInt("m_productbom_id");
			MProduct productToGet = new MProduct(pCtx, m_productbom_id, null);
			
			/*
			if(productToGet.getUOMSymbol().equalsIgnoreCase("sqm") && area != null)//it's sqm item, change qty
			{
				qty = area;
			}
			else if(productToGet.getUOMSymbol().equalsIgnoreCase("m"))//it metres, change qty
			{
				qty = new BigDecimal((int)MtmUtils.getMattributeInstanceValue(productToGet.get_ID(), "Width", null)).divide(BigDecimal.valueOf(1000));
			}
			else if(productToGet.getUOMSymbol().equalsIgnoreCase("ml")) //it's millimetres, change qty
			{
				qty = new BigDecimal((int)MtmUtils.getMattributeInstanceValue(productToGet.get_ID(), "Width", null));
			}
			*/
			qty = getQty(productToGet, area);
			pp.setInitialValues(m_productbom_id, orderLine.getC_BPartner_ID(), qty, isSalesTrx, null);
			BigDecimal[] lbw = MtmUtils.hasLengthAndWidth(mAttributeSetInstance_ID);
			if(productToGet.get_ValueAsBoolean("isgridprice"))//it's a grid price product, need to look up price
			{
				BigDecimal breakvalue = getBreakValue(lbw, productToGet);
				if(breakval != null)
				{
					price = pp.getPriceList().multiply(breakvalue);
				}
			}
			else
			{
				price = pp.getPriceList().multiply(qty);
			}
			
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
	}

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
	
	public boolean setBreakVal(MProduct mProduct) {
		
		BigDecimal[] l_by_w = MtmUtils.hasLengthAndWidth(mAttributeSetInstance_ID);
		if(l_by_w != null) log.warning("------MTM Callouts Length: " + l_by_w[0] + " Width: " + l_by_w[1]);
		breakval = getBreakValue(l_by_w, mProduct);
		if(breakval != null) return true;
		return false;
	}
	
	public BigDecimal getBreakValue(Object params, MProduct mProduct) {
		//isGridPrice = true;
		//setQtyReadOnly(mTab);//We set the qty to read only so user can't adjust grid price.
		//int mPriceListVersionID = Env.getContextAsInt(ctx, WindowNo, "M_PriceList_Version_ID", true);
		int M_PriceList_ID = Env.getContextAsInt(pCtx, windowNum, "M_PriceList_ID", true);
		MPriceList pl = MPriceList.get(pCtx, M_PriceList_ID, null);
		int mPriceListVersionID = 0;
		Timestamp date = null;
		if (gTab.getAD_Table_ID() == I_C_OrderLine.Table_ID)
			date = Env.getContextAsDate(pCtx, "DateOrdered");
		MPriceListVersion plv = pl.getPriceListVersion(date);
		if (plv != null && plv.getM_PriceList_Version_ID() > 0) 
			{
			 	mPriceListVersionID = plv.getM_PriceList_Version_ID();
			}
		
		log.warning("------MTM Callouts M_PriceList_Version_ID: " + mPriceListVersionID);
		int mMproductID = mProduct.get_ID();
		log.warning("------MTM Callouts mMproductID: " + mMproductID);
		StringBuilder sql = new StringBuilder("SELECT breakvalue FROM m_productpricevendorbreak mb ");
		sql.append("WHERE mb.value_two >= ? AND mb.value_one >= ? ");
		sql.append("AND mb.m_product_id = ");
		sql.append(mMproductID);
		sql.append(" AND mb.m_pricelist_version_id = ");
		sql.append(mPriceListVersionID);
		sql.append(" ORDER BY mb.value_two ASC, mb.value_one ASC");
		sql.append(" FETCH FIRST 1 ROWS ONLY");
		
		breakval = null;
		String stringBreakVal = DB.getSQLValueString(null, sql.toString(), (Object [])params);
		if(stringBreakVal != null)
		{
			breakval = new BigDecimal(stringBreakVal);
		}
		
		if(breakval == null)//No price found, use highest price available
		{
			StringBuilder sql1 = new StringBuilder("SELECT breakvalue FROM m_productpricevendorbreak mb ");
			sql1.append("WHERE mb.value_one = (SELECT MAX(value_one) from m_productpricevendorbreak) ");
			sql1.append("AND value_two = (SELECT MAX(value_two) from m_productpricevendorbreak) ");
			sql1.append("AND mb.m_product_id = ");
			sql1.append(mMproductID);
			sql1.append(" AND mb.m_pricelist_version_id = ?");
			//sql1.append(mPriceListVersionID);
			sql1.append(" FETCH FIRST 1 ROWS ONLY");
			
			String result = DB.getSQLValueString(null, sql1.toString(), mPriceListVersionID);
			if(result!=null)
			{
				breakval = new BigDecimal(result);
				log.warning("---------No price found, use highest price available");
			}
			else
			{
				FDialog.warn(windowNum, "No price found at the dimesions entered. Please check the dimesion limits for this product. Setting price to: " + breakval);
				breakval = Env.ONE;
			}
		
		}
		return breakval;
	}
	
	/**
	 * 
	 */
	public BigDecimal getCalculatedCosts(MProduct product, BigDecimal qty) {
		
		if(product.get_ValueAsBoolean("isgridprice"))
		{
			return getMTMProductCost(product);
		}
		else
		{
			MAcctSchema[] mAcctSchema = MAcctSchema.getClientAcctSchema(pCtx, Env.getAD_Client_ID(pCtx));
			int acctSchemaID = mAcctSchema[0].get_ID();
			MAcctSchema mSchema = MAcctSchema.get(pCtx, acctSchemaID);
			String costingMethod = mSchema.getCostingMethod();
			log.warning(mAcctSchema.toString());
			int AD_Org_ID = Env.getAD_Org_ID(pCtx);
			return  MCost.getCurrentCost(product, 0, mSchema, AD_Org_ID, costingMethod, qty, orderLine.getC_OrderLine_ID(), true, null);
		}
		
		
	}
	/**
	 * Gets a cost for an mtm product.
	 * Looks up the grid price if it's a grid priced item
	 * The default purchase price is used and must have a grid loaded with costs as appropriate.
	 * @param mtmProduct
	 * @return
	 */
	public BigDecimal getMTMProductCost(MProduct mtmProduct) {
		
		return null;
		
	}
	public BigDecimal getQty(MProduct productToGet, BigDecimal area) {
		
		BigDecimal qty = Env.ONE;
		if(area != null)
		{
			if(productToGet.getUOMSymbol().equalsIgnoreCase("sqm") && area != null)//it's sqm item, change qty
			{
				qty = area;
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
		return qty;
	}
}
