/**
 * 
 */
package au.blindmot.copybomtrigger.process;

import java.math.BigDecimal;
import java.util.Date;

import org.compiere.model.MProduct;
import org.eevolution.model.MPPProductBOMLine;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.model.MBLDMtmProductBomAdd;
import au.blindmot.model.MBLDMtmProductBomTrigger;
import au.blindmot.utils.MtmUtils;

/**
 * @author phil
 *
 */
public class CopyBomTrigger extends SvrProcess {


		private Date dateParam;
		private String rangeFrom;
		private String rangeTo;
		private int toProductID;
		//int toMProductID = 0;
		private BigDecimal bigDecParam;
		private int recordId = 0;
		private boolean ignoreClassification = false;
		
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
				if ( para.getParameterName().equals("IgnoreClassification") )
					ignoreClassification = "Y".equals((String) para.getParameter());
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
			
			//MBLDProductPartType bdlPartTypeFrom = new MBLDProductPartType(getCtx(), recordId, get_TrxName());
			MProduct mProductFrom = new MProduct(getCtx(), recordId, get_TrxName());
			MProduct mProductTo = new MProduct(getCtx(), toProductID, get_TrxName());
			MBLDMtmProductBomTrigger[] fromBomTriggers = MtmUtils.getMBLDMtmProductBomTrigger(recordId);
			
			log.warning("mProductTo.getM_Product_Category_ID() = " + mProductTo.getM_Product_Category_ID());
			log.warning("mProductFrom.getM_Product_Category_ID() = " +mProductFrom.getM_Product_Category_ID());
			log.warning("mProductTo.getClassification() = " + mProductTo.getClassification());
			log.warning("mProductFrom.getClassification() = " + mProductFrom.getClassification());
			MPPProductBOMLine[] fromBomLines = MPPProductBOMLine.getBOMLines(mProductFrom);
			if((mProductTo.getM_Product_Category_ID() != mProductFrom.getM_Product_Category_ID())/*|| !mProductTo.getClassification().equalsIgnoreCase(mProductFrom.getClassification())*/)
			{
				if(!ignoreClassification)
				{
					throw new AdempiereUserError("Destination product is a different Product Category or classification than parent product.");
				}
				
			}
			if(mProductTo.getM_Product_ID() == mProductFrom.getM_Product_ID())
			{
				throw new AdempiereUserError("Can't copy to the same product");
			}
			
			
			//Check if each product trigger is on the destination product BOM. If it's not, add it.
			for(int j=0; j < fromBomTriggers.length; j++)
			{
				MPPProductBOMLine bomLineFrom = new MPPProductBOMLine(Env.getCtx(), fromBomTriggers[j].getPP_Product_Bomline_ID(), trx);
				BigDecimal newProductToCheck = new BigDecimal(bomLineFrom.getM_Product_ID());
				boolean isPicklist = bomLineFrom.get_ValueAsBoolean("picklist");
				BigDecimal newProductBOMQty = getBOMQty(fromBomLines, newProductToCheck);
				
				
				if(newProductToCheck.compareTo(Env.ZERO) > 0 && !isOnDestinationBOM(newProductToCheck.intValue()))
				{
					addToDestinationBOM(newProductToCheck.intValue(), newProductBOMQty, isPicklist);
				}
				
			}
			
			
			
			for(int i = 0; i < fromBomTriggers.length; i++)
			{
				//Add header record
				MBLDMtmProductBomTrigger toMBLDMtmProductBomTrigger = new MBLDMtmProductBomTrigger(Env.getCtx(), 0, trx);
				toMBLDMtmProductBomTrigger.setDescription(fromBomTriggers[i].getDescription());
				toMBLDMtmProductBomTrigger.setHelp(fromBomTriggers[i].getHelp());
				/*Next line causes products to be copied with parent M_Product_BOM_ID, 
				 * should be destination M_Product_BOM_ID*/
				int destMProductBomId = getDestinationMProductBomId(fromBomTriggers[i]);
				toMBLDMtmProductBomTrigger.setPP_Product_Bomline_ID(destMProductBomId);
				toMBLDMtmProductBomTrigger.setM_Product_ID(toProductID);
				toMBLDMtmProductBomTrigger.setIsActive(fromBomTriggers[i].isActive());
				toMBLDMtmProductBomTrigger.setIsTriggerDelete(fromBomTriggers[i].isTriggerDelete());
				toMBLDMtmProductBomTrigger.setName(fromBomTriggers[i].getName());
				toMBLDMtmProductBomTrigger.saveEx(trx);
				//Add lines
				MBLDMtmProductBomAdd[] fromAddLines = fromBomTriggers[i].getLines(null, null);
				for(int g = 0; g < fromAddLines.length; g++)
				{
					//Check if each BOM Derived Modified is on the destination product BOM. If it's not, add it.
					MPPProductBOMLine bomLineFrom = new MPPProductBOMLine(Env.getCtx(), fromAddLines[g].getPP_Product_Bomline_ID(), trx);
					//System.out.println("From Lines fromAddLines[g].getPP_Product_Bomline_ID(): "+ fromAddLines[g].getPP_Product_Bomline_ID());
						BigDecimal newProductToCheck = new BigDecimal(bomLineFrom.getM_Product_ID());
						boolean isPicklist = bomLineFrom.get_ValueAsBoolean("picklist");
						BigDecimal newProductBOMQty = getBOMQty(fromBomLines, newProductToCheck);
						
						if(newProductToCheck.compareTo(Env.ZERO) > 0 && !isOnDestinationBOM(newProductToCheck.intValue()))
						{
							//Add products to destination BOM if they aren't there already.
							addToDestinationBOM(newProductToCheck.intValue(), newProductBOMQty, isPicklist);
						}
					//Add the lines
					MBLDMtmProductBomAdd toMBLDMtmProductBomAddLine = new MBLDMtmProductBomAdd(Env.getCtx(), 0, trx);
					log.warning("---------Adding BOM trigger lines");
					toMBLDMtmProductBomAddLine.setBLD_MTM_Product_Bom_Trigger_ID(toMBLDMtmProductBomTrigger.get_ID());
					
					//toMBLDMtmProductBomAddLine.setM_Product_BOM_ID(getDestinationMProductBomId(fromAddLines[g]));
					toMBLDMtmProductBomAddLine.setPP_Product_Bomline_ID(getDestinationMProductBomId(fromAddLines[g]));
					toMBLDMtmProductBomAddLine.setHelp(fromAddLines[g].getHelp());
					toMBLDMtmProductBomAddLine.setQty(fromAddLines[g].getQty());
					toMBLDMtmProductBomAddLine.setIsActive(fromAddLines[g].isActive());
					String sql = "SELECT NVL(MAX(Line),0)+10 FROM bld_mtm_product_bom_add WHERE bld_mtm_product_bom_trigger_id=?";
					int ii = DB.getSQLValue (get_TrxName(), sql, toMBLDMtmProductBomTrigger.get_ID());
					toMBLDMtmProductBomAddLine.setLine(ii);
					toMBLDMtmProductBomAddLine.saveEx();
				}
				
			}
			
			//Present user with link to records once done.
			String msg = ("BOM Triggers & child records at Product: " + toProductID);
			addLog(mProductTo.get_ID(), null, null, msg, MProduct.Table_ID, mProductTo.get_ID());
			return "@OK@";
		}
		
		private int getDestinationMProductBomId(MBLDMtmProductBomAdd fromMbldMtmProductBomAdd) {
			StringBuilder sql = new StringBuilder("SELECT m_productbom_id ");
			sql.append("FROM m_product_bom mpb ");
			sql.append("WHERE mpb.m_product_bom_id = ? ");
			int m_Product_Bom_Id = DB.getSQLValue(get_TrxName(), sql.toString(), fromMbldMtmProductBomAdd.getPP_Product_Bomline_ID());
			return getMProductBomId(m_Product_Bom_Id);
		}

		private int getDestinationMProductBomId(MBLDMtmProductBomTrigger fromMbldMtmProductBomTrigger) {
			StringBuilder sql = new StringBuilder("SELECT m_productbom_id ");
			sql.append("FROM m_product_bom mpb ");
			sql.append("WHERE mpb.m_product_bom_id = ? ");
			int m_Product_Bom_Id = DB.getSQLValue(get_TrxName(), sql.toString(), fromMbldMtmProductBomTrigger.getPP_Product_Bomline_ID());
			return getMProductBomId(m_Product_Bom_Id);
		}
		
		private int getMProductBomId(int m_Product_Bom_Id) {
			StringBuilder sql2 = new StringBuilder("SELECT m_product_bom_id ");
			sql2.append("FROM m_product_bom mpb ");
			sql2.append("WHERE mpb.m_productbom_id = ? ");
			sql2.append("AND mpb.m_product_id = ?");
			return m_Product_Bom_Id = DB.getSQLValue(get_TrxName(), sql2.toString(), m_Product_Bom_Id, toProductID);
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
		 * @param isPicklist 
		 * 
		 */
		private void addToDestinationBOM(int destBOMProductID, BigDecimal ProductBOMQty, boolean isPicklist) {
			String trxName = get_TrxName();
			MPPProductBOMLine toBomLine = new MPPProductBOMLine(getCtx(), 0, trxName);
			toBomLine.setM_Product_ID(destBOMProductID);
			toBomLine.setM_Product_ID(toProductID);
			toBomLine.setQtyBOM(ProductBOMQty);
			String sql = "SELECT NVL(MAX(Line),0)+10 FROM M_Product_BOM WHERE M_Product_BOM_ID=?";
			int ii = DB.getSQLValue (get_TrxName(), sql, toBomLine.getPP_Product_BOMLine_ID());
			toBomLine.setLine (ii);	
			toBomLine.set_ValueOfColumn("picklist", isPicklist);
			toBomLine.saveEx(trxName);
		}
		
	/*	private int getToMProductBOMID(Object object) {
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
			
		}*/

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
