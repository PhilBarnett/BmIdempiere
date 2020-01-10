/**
 * 
 */
package au.blindmot.setinactive.process;

/**
 * @author phil
 *
 */

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
import java.util.Date;

import org.compiere.model.MBankStatement;
import org.compiere.model.MBankStatementLine;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;

public class SetInactive extends SvrProcess {


	private boolean boolParam;
	private Date dateParam;
	private String rangeFrom;
	private String rangeTo;
	private String childTable;
	private int intParam;
	private BigDecimal bigDecParam;
	private int recordId = 0;
	private int tableId = 0;
	
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
			if ( para.getParameterName().equals("setactive") )
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
				intParam = para.getParameterAsInt();
			else if ( para.getParameterName().equals("bigDecParam") )
				bigDecParam = (BigDecimal) para.getParameter();
			else if ( para.getParameterName().equals("isStringParam") )
				childTable = para.getParameterAsString();
			
			else 
				log.info("Parameter not found " + para.getParameterName());
		}

		// you can also retrieve the id of the current record for processes called from a window
		recordId = getRecord_ID();
		tableId = getTable_ID();
	}
	
	/**
	 * The doIt method is where your process does its work
	 */
	@Override
	protected String doIt() throws Exception {

		//validate parameters
		/*if(childTable == null)
		{
			throw new AdempiereUserError("No child table to set inactive records.");
		}*/
		
		if (recordId < 1)
		{
			throw new AdempiereUserError("No parent record.");
		}
		if(tableId < 1)
		{
			throw new AdempiereUserError("No parent table.");
		}
		
		//Get child records using childtable and recordID
		MTable parentTable = new MTable(getCtx(), tableId, null);
		
		MTable chTable = MTable.get(getCtx(), childTable);
		
		//Class tableClass = MTable.getClass(childTable);
		//String className = tableClass.getName();
		//tableClass.
		
		//tableClass.cast(tableClass.getSuperclass());
		//PO persistentObject = (PO)tableClass;
		
		//Iterate through child records and set them inactive
		if(parentTable.getName().equalsIgnoreCase("Bank Statement"))
		{
			//get lines
			String trxName = get_TrxName();
			MBankStatement statement = new MBankStatement(getCtx(), recordId, null);
			MBankStatementLine[] lines = statement.getLines(false);
			if (lines.length < 1) throw new AdempiereUserError("No lines.");
			for(int i = 0; i < lines.length; i++)
			{
				lines[i].setIsActive(boolParam);
				lines[i].save(trxName);
			}
			
			
		}
		
		
		//Optionally set all active
		//How to determine the child record table?
		
		/* Commonly the doIt method firstly do some validations on the parameters
		   and throws AdempiereUserException or AdempiereSystemException if errors found
		
		   After this the process code is written and on any error an Exception must be thrown
		   Use the addLog method to register important information about the running of your process
		   This information is preserved in a log and shown to the user at the end.
		*/
		
		return "OK";
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