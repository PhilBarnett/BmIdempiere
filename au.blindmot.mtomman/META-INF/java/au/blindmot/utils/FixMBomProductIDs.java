package au.blindmot.utils;

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
import java.util.List;

import org.compiere.model.MProductBOM;
import org.compiere.model.MQualityTest;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MPPProductBOMLine;

import au.blindmot.model.MBLDBomDerived;
import au.blindmot.model.MBLDMtmProductBomAdd;
import au.blindmot.model.MBLDMtmProductBomTrigger;
import au.blindmot.model.MBLDProductNonSelect;

public class FixMBomProductIDs extends SvrProcess {


	private boolean boolParam;
	private Date dateParam;
	private String rangeFrom;
	private String rangeTo;
	private int intParam;
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
			else if ( para.getParameterName().equals("intParam") )
				intParam = para.getParameterAsInt();
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

		/* Commonly the doIt method firstly do some validations on the parameters
		   and throws AdempiereUserException or AdempiereSystemException if errors found
		
		   After this the process code is written and on any error an Exception must be thrown
		   Use the addLog method to register important information about the running of your process
		   This information is preserved in a log and shown to the user at the end.
		*/
		
		//Add new columns, drop constraints
		/*
		 * Do manually, then add db columns in each instnce
		StringBuilder sql = new StringBuilder();
		sql.append("ALTER TABLE bld_mtm_product_bom_add ADD COLUMN pp_product_bomline_id numeric(10,0) NOT NULL DEFAULT (0);");
		sql.append("ALTER TABLE bld_mtm_product_bom_trigger ADD COLUMN pp_product_bomline_id numeric(10,0) NOT NULL DEFAULT (0);");
		sql.append("ALTER TABLE bld_product_non_select ADD COLUMN pp_product_bomline_id numeric(10,0) NOT NULL DEFAULT (0);");
		sql.append("ALTER TABLE bld_mtm_product_bom_trigger DROP CONSTRAINT bldmtmproductbomtrigger_bommpr;");
		sql.append("ALTER TABLE bld_mtm_product_bom_add DROP CONSTRAINT bld_mtm_product_bom_add_mprodu;");
		sql.append("ALTER TABLE bld_product_non_select DROP CONSTRAINT mproductbom_bldproductnonselec;");
		DB.executeUpdate(sql.toString(), true);
		*/
		
		//Now we have new columns, lets add the right pp_product_bomline_id as foreign keys
		//add to TABLE bld_mtm_product_bom_trigger
		//Get all bld_mtm_product_bom_trigger records as objects.
		
		String where = "bld_mtm_product_bom_trigger_id IS NOT NULL";
		List<MBLDMtmProductBomTrigger> mBLDMtmProductBomTriggers = new Query(Env.getCtx(),MBLDMtmProductBomTrigger.Table_Name,where,get_TrxName()).list();
		for (MBLDMtmProductBomTrigger mBLDMtmProductBomTrigger : mBLDMtmProductBomTriggers)
		{
			int m_product_bom_id = mBLDMtmProductBomTrigger.getM_Product_BOM_ID();
			//MProductBOM olDMproductBom = new MProductBOM(getCtx(), m_product_bom_id, get_TrxName());
			int parentProductID = getOldBomParentProductID(m_product_bom_id);
			
			//Get the new 'pp_product_bom' ID
			int pp_product_bomID = getPPProductBomID(parentProductID);
			int triggerProductID = mBLDMtmProductBomTrigger.getM_Product_ID();
			
			int bomAddProductID = getOldBomProductID(m_product_bom_id);//The product on the old BOM
			
			int pPProductBomLineID = getPPProductBomLineID(pp_product_bomID, bomAddProductID);
			//MPPProductBOMLine mPPProductBOMLine = new MPPProductBOMLine(getCtx(),pPProductBomLineID, get_TrxName());
			log.warning("Setting mBLDMtmProductBomTrigger.setPP_Product_Bomline_ID with: " + pPProductBomLineID);
			mBLDMtmProductBomTrigger.setPP_Product_Bomline_ID(pPProductBomLineID);
			mBLDMtmProductBomTrigger.save(get_TrxName());
			
		}
		
		where = "bld_mtm_product_bom_add_id IS NOT NULL";
		List<MBLDMtmProductBomAdd> mBLDMtmProductBomAdds = new Query(Env.getCtx(),MBLDMtmProductBomAdd.Table_Name,where,get_TrxName()).list();
		for (MBLDMtmProductBomAdd mBLDMtmProductBomAdd : mBLDMtmProductBomAdds)
		{
			int m_product_bom_id = mBLDMtmProductBomAdd.getM_Product_BOM_ID();
			//MProductBOM olDMproductBom = new MProductBOM(getCtx(), m_product_bom_id, get_TrxName());
			int parentProductID = getOldBomParentProductID(m_product_bom_id);
			
			//Get the new 'pp_product_bom' ID
			int pp_product_bomID = getPPProductBomID(parentProductID);
			int bomAddProductID = getOldBomProductID(m_product_bom_id);//The product on the old BOM
			
			int pPProductBomLineID = getPPProductBomLineID(pp_product_bomID, bomAddProductID);
			//MPPProductBOMLine mPPProductBOMLine = new MPPProductBOMLine(getCtx(),pPProductBomLineID, get_TrxName());
			log.warning("Setting mBLDMtmProductBomAdd.setPP_Product_Bomline_ID with: " + pPProductBomLineID);
			mBLDMtmProductBomAdd.setPP_Product_Bomline_ID(pPProductBomLineID);
			mBLDMtmProductBomAdd.save(get_TrxName());
			
		}
		
		where = "bld_product_non_select_id IS NOT NULL";
		List<MBLDProductNonSelect> mBLDProductNonSelects = new Query(Env.getCtx(),MBLDProductNonSelect.Table_Name,where,get_TrxName()).list();
		for (MBLDProductNonSelect mBLDProductNonSelect : mBLDProductNonSelects)
		{
			int m_product_bom_id = mBLDProductNonSelect.getM_Product_Bom_ID();
			//MProductBOM olDMproductBom = new MProductBOM(getCtx(), m_product_bom_id, get_TrxName());
			int parentProductID = getOldBomParentProductID(m_product_bom_id);
			
			//Get the new 'pp_product_bom' ID
			int pp_product_bomID = getPPProductBomID(parentProductID);
			int bomAddProductID = getOldBomProductID(m_product_bom_id);//The product on the old BOM
			
			int pPProductBomLineID = getPPProductBomLineID(pp_product_bomID, bomAddProductID);
			//MPPProductBOMLine mPPProductBOMLine = new MPPProductBOMLine(getCtx(),pPProductBomLineID, get_TrxName());
			log.warning("Setting mBLDProductNonSelect.setPP_Product_Bomline_ID with: " + pPProductBomLineID);
			mBLDProductNonSelect.setPP_Product_Bomline_ID(pPProductBomLineID);
			mBLDProductNonSelect.save(get_TrxName());
			
		}
		
		where = "bld_mtom_bomderived_id IS NOT NULL";
		List<MBLDBomDerived> mBLDBomDeriveds = new Query(Env.getCtx(),MBLDBomDerived.Table_Name,where,get_TrxName()).list();
		for (MBLDBomDerived mBLDBomDerived : mBLDBomDeriveds)
		{
			int m_product_bom_id = mBLDBomDerived.getMBOMProductID();
			//MProductBOM olDMproductBom = new MProductBOM(getCtx(), m_product_bom_id, get_TrxName());
			int parentProductID = getOldBomParentProductID(m_product_bom_id);
			
			//Get the new 'pp_product_bom' ID
			int pp_product_bomID = getPPProductBomID(parentProductID);
			int bomAddProductID = getOldBomProductID(m_product_bom_id);//The product on the old BOM
			
			int pPProductBomLineID = getPPProductBomLineID(pp_product_bomID, bomAddProductID);
			//MPPProductBOMLine mPPProductBOMLine = new MPPProductBOMLine(getCtx(),pPProductBomLineID, get_TrxName());
			log.warning("Setting mBLDBomDerived.setPP_Product_Bomline_ID with: " + pPProductBomLineID);
			mBLDBomDerived.setPP_Product_Bomline_ID(pPProductBomLineID);
			mBLDBomDerived.save(get_TrxName());
			
		}
		/*
		//Commented out until we know it works
		//Add primary key constraints
		StringBuilder sql1 = new StringBuilder();
		sql1.append("ALTER TABLE bld_mtm_product_bom_add ADD CONSTRAINT ");
		sql1.append("bldmtmproductbomadd_pp_product_bom FOREIGN KEY (pp_product_bomline_id) ");
		sql1.append("REFERENCES adempiere.pp_product_bomline (pp_product_bomline_id) MATCH SIMPLE ");		
		sql1.append("ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED");
				
		StringBuilder sql11 = new StringBuilder();
		sql11.append("ALTER TABLE bld_mtm_product_bom_trigger ADD CONSTRAINT ");
		sql11.append("bldmtmproductbomtrigger_pp_product_bom FOREIGN KEY (pp_product_bomline_id) ");
		sql11.append("REFERENCES adempiere.pp_product_bomline (pp_product_bomline_id) MATCH SIMPLE ");		
		sql11.append("ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED");	
		
		StringBuilder sql111 = new StringBuilder();
		sql111.append("ALTER TABLE bld_product_non_select ADD CONSTRAINT ");
		sql111.append("bbldproductnonselect_pp_product_bom FOREIGN KEY (pp_product_bomline_id) ");
		sql111.append("REFERENCES adempiere.pp_product_bomline (pp_product_bomline_id) MATCH SIMPLE ");		
		sql111.append("ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED");
		
		DB.executeUpdate(sql1.toString(), true);
		DB.executeUpdate(sql11.toString(), true);
		DB.executeUpdate(sql111.toString(), true);
		
		*/
		
		return "Process completed successfully, check log for details.";
	}
	
	
	/**
	 * Gets new Bom product ID for the parent product. NOTE this will only work when there is only one
	 * pp_product_bom_id per parent. 
	 * @param parentMproduct_ID
	 * @return
	 */
	private int getPPProductBomID(int parentMproduct_ID) {
		StringBuilder sql1 = new StringBuilder("SELECT pp_product_bom_id FROM ");
		sql1.append("pp_product_bom WHERE ");
		sql1.append("m_product_id = ?");
		Object[] params1 = new Object[1];
		params1[0] = parentMproduct_ID;
		return DB.getSQLValue(null, sql1.toString(), params1);
	}
	
	private int getPPProductBomLineID(int PPProductBomID, int mProductID) {
		StringBuilder sql1 = new StringBuilder("SELECT pp_product_bomline_id FROM ");
		sql1.append("pp_product_bomline WHERE ");
		sql1.append("pp_product_bom_id = ? ");
		sql1.append("AND m_product_id = ?");
		Object[] params1 = new Object[2];
		params1[0] = PPProductBomID;
		params1[1] = mProductID;
		return DB.getSQLValue(null, sql1.toString(), params1);
	}
	
	private int getOldBomParentProductID(int m_product_bom_id) {
		StringBuilder sql1 = new StringBuilder("SELECT m_product_id FROM ");
		sql1.append("m_product_bom_old WHERE ");
		sql1.append("m_product_bom_id = ? ");
		Object[] params1 = new Object[1];
		params1[0] = m_product_bom_id;
		//params1[1] = mProductID;
		return DB.getSQLValue(null, sql1.toString(), params1);
	}
	
	private int getOldBomProductID(int m_product_bom_id) {
		StringBuilder sql1 = new StringBuilder("SELECT m_productbom_id FROM ");
		sql1.append("m_product_bom_old WHERE ");
		sql1.append("m_product_bom_id = ? ");
		Object[] params1 = new Object[1];
		params1[0] = m_product_bom_id;
		//params1[1] = mProductID;
		return DB.getSQLValue(null, sql1.toString(), params1);
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