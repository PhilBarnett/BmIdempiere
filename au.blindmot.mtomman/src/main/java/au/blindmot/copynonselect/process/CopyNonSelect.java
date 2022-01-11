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
package au.blindmot.copynonselect.process;

import java.math.BigDecimal;
import java.util.Date;

import org.compiere.model.MProduct;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MPPProductBOMLine;

import au.blindmot.model.MBLDProductNonSelect;
import au.blindmot.model.MBLDProductPartType;

public class CopyNonSelect extends SvrProcess {


	private Date dateParam;
	private String rangeFrom;
	private String rangeTo;
	private int toProductID;
	//int toMProductID = 0;
	private BigDecimal bigDecParam;
	private int recordId = 0;
	
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
			if ( para.getParameterName().equals("isBooleanParam") )
				"Y".equals((String) para.getParameter());
			else if ( para.getParameterName().equals("dateParam") )
				dateParam = (Date) para.getParameter();
			// parameters may also specify the start and end value of a range
			else if ( para.getParameterName().equals("rangeParam") )
			{
				rangeFrom = (String) para.getParameter();
				rangeTo = (String) para.getParameter_To();
			}
			else if ( para.getParameterName().equals("M_Product_ID") )
				toProductID = para.getParameterAsInt();
			else if ( para.getParameterName().equals("bigDecParam") )
				bigDecParam = (BigDecimal) para.getParameter();
			else 
				log.info("Parameter not found " + para.getParameterName());
		}

		// you can also retrieve the id of the current record for processes called from a window
		recordId = getRecord_ID();
		log.warning("--------getRecord_ID() = " + recordId);
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
		*/
		
		//Throw error if other parent is a different product category/classification
		if(recordId == 0)
		{
			throw new AdempiereUserError("There is no parent record to copy from.");
		}
		if(toProductID==0)
		{
			throw new AdempiereUserError("There is no record to copy to.");
		}
		
		String trx = get_TrxName();
		MBLDProductPartType bdlPartTypeFrom = new MBLDProductPartType(getCtx(), recordId, get_TrxName());
		MProduct mProductFrom = new MProduct(getCtx(), bdlPartTypeFrom.getM_Product_ID(), get_TrxName());
		MProduct mProductTo = new MProduct(getCtx(), toProductID, get_TrxName());
		MPPProductBOMLine[] fromBomLines = MPPProductBOMLine.getBOMLines(mProductFrom);
		log.warning("mProductTo.getM_Product_Category_ID() = " + mProductTo.getM_Product_Category_ID());
		log.warning("mProductFrom.getM_Product_Category_ID() = " +mProductFrom.getM_Product_Category_ID());
		log.warning("mProductTo.getClassification() = " + mProductTo.getClassification());
		log.warning("mProductFrom.getClassification() = " + mProductFrom.getClassification());
		
		if((mProductTo.getM_Product_Category_ID() != mProductFrom.getM_Product_Category_ID()))
		{
			throw new AdempiereUserError("Destination product is a different Product Category or classification than parent product.");
		}
		if(mProductTo.getM_Product_ID() == mProductFrom.getM_Product_ID())
		{
			throw new AdempiereUserError("Can't copy to the same product");
		}
		
		/*
		 * Check if additional and substitute products exist on parent M_Product_BOM; add them if they don't; once this is done, 
		 * add a copy of the BLD_Product_PartType record : value IN (SELECT value FROM tbl_name);
		 */
		//Get list of products to check from mProducrFrom
		MBLDProductNonSelect[] fromNonSelectLines = bdlPartTypeFrom.getMBLDProductNonSelectLines(recordId, null);
		
		//Check if each product is on the destination product BOM. If it's not, add it.
		for(int j=0; j < fromNonSelectLines.length; j++)
		{
			BigDecimal subProductToCheck = (BigDecimal) fromNonSelectLines[j].getsubstituteproduct();
			BigDecimal addProductToCheck = (BigDecimal) fromNonSelectLines[j].getaddtionalproduct();
			BigDecimal subProductBOMQty = getBOMQty(fromBomLines, subProductToCheck);
			BigDecimal addProductBOMQty = getBOMQty(fromBomLines, addProductToCheck);
			
			if(subProductToCheck.compareTo(Env.ZERO) > 0 && !isOnDestinationBOM(subProductToCheck.intValue()))
			{
				addToDestinationBOM(subProductToCheck.intValue(), subProductBOMQty);
			}
			if(addProductToCheck.compareTo(Env.ZERO) > 0 && !isOnDestinationBOM(addProductToCheck.intValue()))
			{
				addToDestinationBOM(addProductToCheck.intValue(), addProductBOMQty);
			}
			
		}
		
		//Create destination BLD_Product_Non_Select records with appropriate FKs etc
		MBLDProductPartType toMBLDProductPartType = new MBLDProductPartType(getCtx(), 0, trx);
		toMBLDProductPartType.setM_Product_ID(toProductID);
		toMBLDProductPartType.setDescription(bdlPartTypeFrom.getDescription());
		toMBLDProductPartType.setIsMandatory(bdlPartTypeFrom.isMandatory());
		toMBLDProductPartType.setM_PartTypeId(bdlPartTypeFrom.getM_PartTypeID());
		toMBLDProductPartType.setis_user_select(bdlPartTypeFrom.is_user_select());//Should always be 'N'
		String sql = "SELECT NVL(MAX(Line),0)+10 FROM BLD_Product_PartType WHERE M_Product_ID=?";
		int ii = DB.getSQLValue (get_TrxName(), sql, toMBLDProductPartType.getM_Product_ID());
		toMBLDProductPartType.setLine(ii);
		toMBLDProductPartType.saveEx(trx);
		
		//Add copies of the BLD_Product_Non_Select as child records to the newly created BLD_Product_PartType record.
		int toMBLDProductPartTypeID = toMBLDProductPartType.getBLD_Product_PartType_ID();
		MBLDProductNonSelect toMBLDProductNonSelect = null;
		for(int s = 0; s < fromNonSelectLines.length; s++)
		{
			int destMProductBomID1 = getToMProductBOMID(((fromNonSelectLines[s].getsubstituteproduct())));
			int destMProductBomID2 = getToMProductBOMID(((fromNonSelectLines[s].getaddtionalproduct())));
			toMBLDProductNonSelect = new MBLDProductNonSelect(getCtx(), 0, trx);
			toMBLDProductNonSelect.setBLD_Product_PartType_ID(toMBLDProductPartTypeID);
			toMBLDProductNonSelect.setDescription(fromNonSelectLines[s].getDescription());
			toMBLDProductNonSelect.setwidth1(fromNonSelectLines[s].getwidth1());
			toMBLDProductNonSelect.setwidth2(fromNonSelectLines[s].getwidth2());
			toMBLDProductNonSelect.setdrop1(fromNonSelectLines[s].getdrop1());
			toMBLDProductNonSelect.setdrop2(fromNonSelectLines[s].getdrop2());
			toMBLDProductNonSelect.setcondition_set(fromNonSelectLines[s].getcondition_set());
			toMBLDProductNonSelect.setoperation_type(fromNonSelectLines[s].getoperation_type());
			toMBLDProductNonSelect.setsubstituteproduct(fromNonSelectLines[s].getsubstituteproduct());
			toMBLDProductNonSelect.setaddtionalproduct(fromNonSelectLines[s].getaddtionalproduct());
			if(destMProductBomID1 > 0)
			{
				toMBLDProductNonSelect.setM_Product_Bom_ID(destMProductBomID1);
			}
			if(destMProductBomID2 > 0)
			{
				toMBLDProductNonSelect.setM_Product_Bom_ID(destMProductBomID2);
			}
			
			String sql2 = "SELECT NVL(MAX(Line),0)+10 FROM BLD_Product_Non_Select  WHERE BLD_Product_PartType_ID=?";
			int line = DB.getSQLValue (get_TrxName(), sql2, toMBLDProductNonSelect.getBLD_Product_PartType_ID());
			toMBLDProductNonSelect.setLine(line);
			toMBLDProductNonSelect.save(get_TrxName());
			
		}
		//Present user with link to records once done.
		String msg = ("BOM, non user selectable Product PartType & child records at Product: " + toMBLDProductPartType.getM_Product_ID());
		addLog(toMBLDProductPartType.getM_Product_ID(), null, null, msg, MProduct.Table_ID, toMBLDProductPartType.getM_Product_ID());
		return "@OK@";
	}
	
	/**
	 * @param fromBomLines
	 * @param subProductToCheck
	 * @return
	 */
	private BigDecimal getBOMQty(MPPProductBOMLine[] fromBomLines, BigDecimal subProductToCheck) {
		for(int z = 0; z < fromBomLines.length; z++)
		{
			if(fromBomLines[z].getM_Product_ID() == subProductToCheck.intValue())
			{
				return fromBomLines[z].getQty();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param destBOMProductID
	 * @return
	 */
	private boolean isOnDestinationBOM(int destBOMProductID) {
		MPPProductBOMLine[] destBomProducts = MPPProductBOMLine.getBOMLines(MProduct.get(toProductID));
		for(int i = 0; i < destBomProducts.length; i++)
		{
			if(destBomProducts[i].getM_Product_ID() == destBOMProductID) return true;
		}
		return false;
	}
	
	/**
	 * @param ProductBOMQty 
	 * 
	 */
	private void addToDestinationBOM(int destBOMProductID, BigDecimal ProductBOMQty) {
		String trxName = get_TrxName();
		MPPProductBOMLine toBomLine = new MPPProductBOMLine(getCtx(), 0, trxName);
		toBomLine.setM_Product_ID(destBOMProductID);
		toBomLine.setM_Product_ID(toProductID);
		toBomLine.setQtyBOM(ProductBOMQty);
		String sql = "SELECT NVL(MAX(Line),0)+10 FROM M_Product_BOM WHERE M_Product_BOM_ID=?";
		int ii = DB.getSQLValue (get_TrxName(), sql, toBomLine.getPP_Product_BOMLine_ID());
		toBomLine.setLine (ii);	
		toBomLine.saveEx(trxName);
	}
	
	private int getToMProductBOMID(Object object) {
		MPPProductBOMLine[] destBomProducts = MPPProductBOMLine.getBOMLines(MProduct.get(toProductID));
		BigDecimal bigObject = (BigDecimal)object;
		int iD = bigObject.intValue();
		for(int i = 0; i < destBomProducts.length; i++)
		{
			if(destBomProducts[i].getM_Product_ID() == iD)
			{
				return destBomProducts[i].get_ID();
			}
		}
		
		return 0;
		
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

}