

/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package au.blindmot.gridprice.processes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.compiere.model.MProduct;
import org.compiere.model.PO;
import org.compiere.model.X_M_ProductPriceVendorBreak;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class GridPrice extends SvrProcess {
	
	/*
	 * Creates an MTM Grid Prices for a specific product. File must be in the following format: first row -> <blank>,width1,width2,width3... 
	 * Second and subsequent rows: -> drop,price@width1,price@width2,price@width3... If the form is not followed exactly, this process will fail. If the Price List type is set to 'List Price', the process will create List prices and will also create Limit Price and Standard Prices as a percentage of the List Price based on your entries. If you create a Limit Price or 
	 * Standard Price, any settings in the 'Set Price...' fields are ignored.
	 */


	private boolean boolParam;
	private Date dateParam;
	private String rangeFrom;
	private String rangeTo;
	private int mPriceListVersionID;
	private BigDecimal bigDecParam;
	private PO record;
	private String fileName;
	private BigDecimal priceLmtPercent;
	private BigDecimal priceListPercent;
	private static String PRICE_LIST = "pricelist";
	private static String PRICE_LIMIT = "pricelimit";
	private static String PRICE_STANDARD = "pricestd";
	private String priceListType = PRICE_LIST;
	private int mProductID;
	private BigDecimal breakValue = new BigDecimal("0.001");
	private BigDecimal breakIncrement = new BigDecimal("0.001");
	
	
	/**
	 * The prepare function is called first and is used to load parameters
	 * which are passed to the process by the framework. Parameters to be
	 * passed are configured in Report & Process -> Parameter.
	 * 
	 */
	@Override
	protected void prepare() {

		// Each Report & Process parameter name is set by the field DB Column Name
		for ( ProcessInfoParameter para : getParameter())
		{
			String name = para.getParameterName();
			if ( para.getParameterName().equals("isBooleanParam") )
				boolParam = "Y".equals((String) para.getParameter());			// later versions can use getParameterAsString
			else if ( para.getParameterName().equals("dateParam") )
				dateParam = (Date) para.getParameter();
			// parameters may also specify the start and end value of a range
			else if ( para.getParameterName().equals("rangeParam") )
			{
				rangeFrom = (String) para.getParameter();
				rangeTo = (String) para.getParameter_To();
			}
			else if (para.getParameterName().equals("filename"))
				fileName = para.getParameterAsString();
			else if ( para.getParameterName().equals("MPriceListVersionID") )
				mPriceListVersionID = para.getParameterAsInt();
			else if ( para.getParameterName().equals("bigDecParam") )
				bigDecParam = (BigDecimal) para.getParameter();
			else if (name.equalsIgnoreCase("Set_Price_Limit"))
				priceLmtPercent = (BigDecimal) para.getParameter();
			else if (name.equalsIgnoreCase("Set_Price_List"))
				priceListPercent = (BigDecimal) para.getParameter();
			else if (name.equalsIgnoreCase("Price_List"))
			{
				String listPara = para.getParameterAsString();
				{
					if(listPara.equalsIgnoreCase("apricestd")) priceListType = PRICE_STANDARD;
					else if(listPara.equalsIgnoreCase("pricelimit")) priceListType = PRICE_LIMIT;
					else if(listPara.equalsIgnoreCase("pricelist")) priceListType = PRICE_LIST;
				}
			}
			else if(name.equalsIgnoreCase("M_Product_ID"))
			{
				BigDecimal bigMproductID = (BigDecimal)para.getParameter();
				mProductID = bigMproductID.intValue(); 
			}
				
			
			else 
				log.info("Parameter not found " + para.getParameterName());
		}

		// you can also retrieve the id of the current record for processes called from a window
		int recordId = getRecord_ID();
	}
	
	/**
	 * The doIt method is where your process does its work
	 */
	@Override
	protected String doIt() throws Exception {

		/* Commonly the doIt method firstly do some validations on the parameters
		   and throws AdempiereUserException or AdempiereSystemException if errors found
		
		   After this the process code is written and on any error an Exception must be thrown
		   Use the addLog method to register important information about the running of your process
		   This information is preserved in a log and shown to the user at the end.
		   
		   TODO:
		*/
		List<String> firstLine = new ArrayList<String>();//This will represent the width of an MTM product.
		List<String> dropAndPrice = null;
		
		try {

            if(fileName == null)
            {
            	throw new AdempiereUserError("File name not found.", "Does the file exist?");
            }
			File inputFile = new File(fileName);

            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));

            String readLine = "";

            System.out.println("Reading file using Buffered Reader");
            int incrementor = 0;
            while ((readLine = bufferedReader.readLine()) != null) {
            	if(incrementor == 0)//It's the first line
            	{
            		firstLine = CSVUtils.parseLine(readLine);
            		//TODO: What's the content of this look like? Is the first item blank or is a number?
            	}
            	if(incrementor > 0)//It's 2nd line or greater.
            	{
            		dropAndPrice = CSVUtils.parseLine(readLine);
            		createGridPrice(firstLine , dropAndPrice);
            	}
            	incrementor++;
            	/*TODO:Read other lines into an ArrayList
            	 * 1st item in list is drop; the rest are the price of the width x drop.
            	 * Create method to iterate through firstLine and current line
            	 * 
            	 *
            	 */
                System.out.println(readLine);
               
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		MProduct mp = new MProduct(getCtx(), mProductID, get_TrxName());
		return "Succesfully executed price break changes for Product: " + mp.getName();
	}

	/**
	 * Post process actions (outside trx).
	 * Please note that at this point the transaction is committed so
	 * you can't rollback.
	 * This method is useful if you need to do some custom work when 
	 * the process complete the work (e.g. open some windows).
	 *  
	 * @param success true if the process was success
	 * @since 3.1.4
	 */
	@Override
	protected void postProcess(boolean success) {
		if (success) {
			
		} else {
              
		}
	}
	
	private void createGridPrice(List<String> width, List<String> dropAndPrice) {
	//Iterate through width
		BigDecimal drop = new BigDecimal(dropAndPrice.get(0));
		for(String wide : width)
		{
			breakValue = breakValue.add(breakIncrement);
			//create a gridprice at this position in width
			/*
			 * If the Price List type is set to 'List Price', the process will create List prices and will also create 
			 * Limit Price and Standard Prices as a percentage of the List Price based on your entries. 
			 * If you create a Limit Price or Standard Price, any settings in the 'Set Price...' fields are ignored.
			 */
			System.out.println("width.indexOf(wide): " + width.indexOf(wide));
			int mproductvrbkid = 0;
			if(width.indexOf(wide) > 0)
			{
				mproductvrbkid = getExistingmProductPriceVendorBreakID(new BigDecimal(wide), drop);
			}
			
			if(priceListType == PRICE_STANDARD && mproductvrbkid == -1 && width.indexOf(wide) > 0)
			{
				//Create List Price
				//TODO:Handle 0 in the Set price list & Set price limit parameters
				BigDecimal priceStd = new BigDecimal(dropAndPrice.get(width.indexOf(wide))).divide(breakValue, 5, RoundingMode.HALF_EVEN);
				BigDecimal priceList = priceStd.multiply(priceListPercent.divide(Env.ONEHUNDRED, 1, RoundingMode.HALF_EVEN));
				BigDecimal priceLmt = priceStd.multiply(priceLmtPercent.divide(Env.ONEHUNDRED, 1, RoundingMode.HALF_EVEN));
				setGridPrice(wide, drop, breakValue, priceList, priceStd, priceLmt);
				//Create Standard price based on priceStandard parameter
				//Create Limit price based on priceLimit parameter
			}
			else if(priceListType == PRICE_LIMIT && width.indexOf(wide) > 0)
			{
				//Update LimitPrice in existing records based on file price list entries.
				if(mproductvrbkid == -1)
				{
					throw new AdempiereUserError
					("No Existing records found that match: " + wide + " " + drop, "Check the price records for this product and create new ones if required.");
				}
				BigDecimal limitToSet = new BigDecimal(dropAndPrice.get(width.indexOf(wide))).divide(breakValue, 2, RoundingMode.HALF_EVEN);
				updateExistingPriceBreak(mproductvrbkid, PRICE_LIMIT, limitToSet);
				
			}
			else if(priceListType == PRICE_LIST && width.indexOf(wide) > 0)
			{
				//Create Standard price based on file price list entries.
				if(mproductvrbkid == -1)
				{
					throw new AdempiereUserError
					("No Existing records found that match: " + wide + " " + drop + " Check the price records for this product and create new ones if required.");
				}
				BigDecimal stdToSet = new BigDecimal(dropAndPrice.get(width.indexOf(wide))).divide(breakValue, 2, RoundingMode.HALF_EVEN);
				updateExistingPriceBreak(mproductvrbkid, PRICE_LIST, stdToSet);
			}
			/*
			else if(//IF what?)
			{
				throw new AdempiereUserError("Create or Update failed.", "Are there existing records you're trying to overwrite?");
			}
			
			*/
			
			
		}
	}
	
	private void setGridPrice(String wide, BigDecimal dropToSet, BigDecimal breakValueToSet, BigDecimal priceList, BigDecimal priceLiStd, BigDecimal priceLimit) {
		String trxName = get_TrxName();
		BigDecimal bigWide = new BigDecimal(wide);
		X_M_ProductPriceVendorBreak MProductPriceVendorBreak = new X_M_ProductPriceVendorBreak(Env.getCtx(), 0, trxName);
		MProductPriceVendorBreak.setBreakValue(breakValueToSet);
		MProductPriceVendorBreak.set_ValueOfColumn("value_one", bigWide);
		MProductPriceVendorBreak.set_ValueOfColumn("value_two", dropToSet);//TODO:Check that the index is correct
		MProductPriceVendorBreak.setM_PriceList_Version_ID(mPriceListVersionID);
		MProductPriceVendorBreak.setM_Product_ID(mProductID);
		MProductPriceVendorBreak.setPriceList(priceList);
		MProductPriceVendorBreak.setPriceStd(priceLiStd);
		MProductPriceVendorBreak.setPriceLimit(priceLimit);
		System.out.println("wide: " + wide + " dropToSet: " + dropToSet + " Breakvalue: " + breakValueToSet);
		MProductPriceVendorBreak.saveEx(trxName);
	}
	
	private int getExistingmProductPriceVendorBreakID(BigDecimal width, BigDecimal drop) {
		
		int  mproductpricevendorbreakid = -1;
		StringBuilder sql = new StringBuilder("SELECT m_productpricevendorbreak_id ");
		sql.append("FROM m_productpricevendorbreak mb ");
		sql.append("WHERE mb.value_one = ");
		sql.append(width.longValue());
		sql.append(" AND mb.value_two = ");
		sql.append(drop.longValue());
		sql.append(" AND mb.m_product_id = ");
		sql.append(mProductID);
		sql.append(" FETCH FIRST 1 ROWS ONLY");
		mproductpricevendorbreakid = DB.getSQLValue(get_TrxName(), sql.toString());//returns -1 if not found
		
		return mproductpricevendorbreakid;
	}

	private void updateExistingPriceBreak(int mvpbid, String priceType, BigDecimal price) {
		X_M_ProductPriceVendorBreak MProductPriceVendorBreak = new X_M_ProductPriceVendorBreak(Env.getCtx(), mvpbid, get_TrxName());
		if(priceType == PRICE_LIMIT)
		{
			MProductPriceVendorBreak.setPriceLimit(price);
			MProductPriceVendorBreak.saveEx();
		}
		else if(priceType == PRICE_LIST)
		{
			MProductPriceVendorBreak.setPriceList(price);
			MProductPriceVendorBreak.saveEx();
		}
		
	}
}