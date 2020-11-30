package au.blindmot.mtmproductinfo.processes;


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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import org.compiere.model.MProduct;
import org.compiere.model.PO;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

import au.blindmot.factories.BLDMtomMakeFactory;
import au.blindmot.make.MadeToMeasureProduct;

public class MtmProductGetConfig  extends SvrProcess {


	private boolean boolParam;
	private Date dateParam;
	private String rangeFrom;
	private String rangeTo;
	private int mProduct;
	private BigDecimal bigDecParam;
	private PO record;
	
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
			else if ( para.getParameterName().equals("M_Product_ID") )
				mProduct = para.getParameterAsInt();
			else if ( para.getParameterName().equals("bigDecParam") )
				bigDecParam = (BigDecimal) para.getParameter();
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

		StringBuilder msg = new StringBuilder();
		MadeToMeasureProduct makeInstance = BLDMtomMakeFactory.getMtmProduct(mProduct, 0, get_TrxName());
		MProduct mtmProd = new MProduct(getCtx(), mProduct, get_TrxName());
		msg.append("Config required for: " + mtmProd.getName() + "\n");
		ArrayList<String> configList = (ArrayList<String>) makeInstance.getConfig();
		if(configList != null)
		{
			for(String listItem : configList)
		{
			msg.append(listItem + "\n");
		}
		addLog(msg.toString());
		
		return "Config returned successfully.";
		}
		return "No config available";
		
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

