/**
 * 
 */
package au.blindmot.copybomtrigger.process;

import java.math.BigDecimal;
import java.util.Date;

import org.compiere.model.MProduct;
import org.compiere.model.MProductBOM;
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
			
			//MBLDProductPartType bdlPartTypeFrom = new MBLDProductPartType(getCtx(), recordId, get_TrxName());
			MProduct mProductFrom = new MProduct(getCtx(), recordId, get_TrxName());
			MProduct mProductTo = new MProduct(getCtx(), toProductID, get_TrxName());
			MBLDMtmProductBomTrigger[] fromBomTriggers = MtmUtils.getMBLDMtmProductBomTrigger(recordId);
			
			log.warning("mProductTo.getM_Product_Category_ID() = " + mProductTo.getM_Product_Category_ID());
			log.warning("mProductFrom.getM_Product_Category_ID() = " +mProductFrom.getM_Product_Category_ID());
			log.warning("mProductTo.getClassification() = " + mProductTo.getClassification());
			log.warning("mProductFrom.getClassification() = " + mProductFrom.getClassification());
			MProductBOM[] fromBomLines = MProductBOM.getBOMLines(mProductFrom);
			if((mProductTo.getM_Product_Category_ID() != mProductFrom.getM_Product_Category_ID())/*|| !mProductTo.getClassification().equalsIgnoreCase(mProductFrom.getClassification())*/)
			{
				throw new AdempiereUserError("Destination product is a different Product Category or classification than parent product.");
			}
			if(mProductTo.getM_Product_ID() == mProductFrom.getM_Product_ID())
			{
				throw new AdempiereUserError("Can't copy to the same product");
			}
			
			
			//Check if each product trigger is on the destination product BOM. If it's not, add it.
			for(int j=0; j < fromBomTriggers.length; j++)
			{
				MProductBOM bomLineFrom = new MProductBOM(Env.getCtx(), fromBomTriggers[j].getM_Product_BOM_ID(), trx);
				BigDecimal newProductToCheck = new BigDecimal(bomLineFrom.getM_ProductBOM_ID());
				BigDecimal newProductBOMQty = getBOMQty(fromBomLines, newProductToCheck);
				
				
				if(newProductToCheck.compareTo(Env.ZERO) > 0 && !isOnDestinationBOM(newProductToCheck.intValue()))
				{
					addToDestinationBOM(newProductToCheck.intValue(), newProductBOMQty);
				}
				
			}
			
			
			
			for(int i = 0; i < fromBomTriggers.length; i++)
			{
				//Add header record
				MBLDMtmProductBomTrigger toMBLDMtmProductBomTrigger = new MBLDMtmProductBomTrigger(Env.getCtx(), 0, trx);
				toMBLDMtmProductBomTrigger.setDescription(fromBomTriggers[i].getDescription());
				toMBLDMtmProductBomTrigger.setHelp(fromBomTriggers[i].getHelp());
				toMBLDMtmProductBomTrigger.setM_Product_BOM_ID(fromBomTriggers[i].getM_Product_BOM_ID());
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
						MProductBOM bomLineFrom = new MProductBOM(Env.getCtx(), fromAddLines[g].getM_Product_BOM_ID(), trx);
						BigDecimal newProductToCheck = new BigDecimal(bomLineFrom.getM_ProductBOM_ID());
						BigDecimal newProductBOMQty = getBOMQty(fromBomLines, newProductToCheck);
						
						if(newProductToCheck.compareTo(Env.ZERO) > 0 && !isOnDestinationBOM(newProductToCheck.intValue()))
						{
							addToDestinationBOM(newProductToCheck.intValue(), newProductBOMQty);
						}
					//Add the lines
					MBLDMtmProductBomAdd toMBLDMtmProductBomAddLine = new MBLDMtmProductBomAdd(Env.getCtx(), 0, trx);
					toMBLDMtmProductBomAddLine.setBLD_MTM_Product_Bom_Trigger_ID(toMBLDMtmProductBomTrigger.get_ID());
					toMBLDMtmProductBomAddLine.setM_Product_BOM_ID(fromAddLines[g].getM_Product_BOM_ID());
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
		
		/**
		 * @param fromBomLines
		 * @param subProductToCheck
		 * @return
		 */
		private BigDecimal getBOMQty(MProductBOM[] fromBomLines, BigDecimal subProductToCheck) {
			for(int z = 0; z < fromBomLines.length; z++)
			{
				if(fromBomLines[z].getM_ProductBOM_ID() == subProductToCheck.intValue())
				{
					return fromBomLines[z].getBOMQty();
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
			MProductBOM[] destBomProducts = MProductBOM.getBOMLines(Env.getCtx(), toProductID, get_TrxName());
			for(int i = 0; i < destBomProducts.length; i++)
			{
				if(destBomProducts[i].getM_ProductBOM_ID() == destBOMProductID) return true;
			}
			return false;
		}
		
		/**
		 * @param ProductBOMQty 
		 * 
		 */
		private void addToDestinationBOM(int destBOMProductID, BigDecimal ProductBOMQty) {
			String trxName = get_TrxName();
			MProductBOM toBomLine = new MProductBOM(getCtx(), 0, trxName);
			toBomLine.setM_ProductBOM_ID(destBOMProductID);
			toBomLine.setM_Product_ID(toProductID);
			toBomLine.setBOMQty(ProductBOMQty);
			String sql = "SELECT NVL(MAX(Line),0)+10 FROM M_Product_BOM WHERE M_Product_BOM_ID=?";
			int ii = DB.getSQLValue (get_TrxName(), sql, toBomLine.getM_Product_BOM_ID());
			toBomLine.setLine (ii);	
			toBomLine.saveEx(trxName);
		}
		
		private int getToMProductBOMID(Object object) {
			MProductBOM[] destBomProducts = MProductBOM.getBOMLines(Env.getCtx(), toProductID, get_TrxName());
			BigDecimal bigObject = (BigDecimal)object;
			int iD = bigObject.intValue();
			for(int i = 0; i < destBomProducts.length; i++)
			{
				if(destBomProducts[i].getM_ProductBOM_ID() == iD)
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
