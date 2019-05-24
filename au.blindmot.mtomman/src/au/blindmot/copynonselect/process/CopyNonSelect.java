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
import org.compiere.model.PO;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;

import au.blindmot.model.MBLDProductPartType;

public class CopyNonSelect extends SvrProcess {


	private boolean boolParam;
	private Date dateParam;
	private String rangeFrom;
	private String rangeTo;
	private int toProductID;
	int toMProductID = 0;
	private BigDecimal bigDecParam;
	private PO record;
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
				boolParam = "Y".equals((String) para.getParameter());			// later versions can use getParameterAsString
			else if ( para.getParameterName().equals("dateParam") )
				dateParam = (Date) para.getParameter();
			// parameters may also specify the start and end value of a range
			else if ( para.getParameterName().equals("rangeParam") )
			{
				rangeFrom = (String) para.getParameter();
				rangeTo = (String) para.getParameter_To();
			}
			else if ( para.getParameterName().equals("intParam") )
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
		if(recordId == 0 || toProductID==0)
		{
			throw new AdempiereUserError("There is no parent record to copy from.");
		}
		MBLDProductPartType bdlPartTypeFrom = new MBLDProductPartType(getCtx(), recordId, get_TrxName());
		//MBLDProductPartType bdlPartTypeTo = new MBLDProductPartType(getCtx(), bLDProductPartTypeID, get_TrxName());
		MProduct mProducrFrom = new MProduct(getCtx(), bdlPartTypeFrom.getM_Product_ID(), get_TrxName());
		MProduct mProducrTo = new MProduct(getCtx(), toMProductID, get_TrxName());
		if((mProducrTo.getM_Product_Category_ID() != mProducrFrom.getM_Product_Category_ID())|| mProducrTo.getClassification() != mProducrFrom.getClassification())
		{
			throw new AdempiereUserError("Destination product is a different Product Category or classification than parent product.");
		}
		
		/*
		 * Check if additional and substitute products exist on parent M_Product_BOM; add them if they don't; once this is done, 
		 * add a copy of the BLD_Product_PartType record : value IN (SELECT value FROM tbl_name);
		 */
		
		//Add copies of the BLD_Product_Non_Select as child records to the newly created BLD_Product_PartType record.
		//Present user with link to records once done.
		
		
		return "A message to the user (indicating success - failures must throw Exceptions)";
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