/**
 * 
 */
package au.blindmot.processes.mtmcreate;

import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.model.MBLDMtomItemLine;
import au.blindmot.model.MBLDMtomProduction;


/**
 * @author phil
 * A process to create MBLDMtomProduction from orders
 * Ordinarily called from Order window.
 *
 */
public class MtmCreate extends SvrProcess {

	private int C_Order_ID = 0;
	private boolean ignoreExistingmtmProd = true;

	@Override
	protected void prepare() {
		
		
		ProcessInfoParameter[] paras = getParameter();
		for(ProcessInfoParameter para : paras)
		{
			String paraName = para.getParameterName();
			if(paraName.equalsIgnoreCase("c_Order_ID"))
				C_Order_ID = para.getParameterAsInt();
			else if(paraName.equalsIgnoreCase("check_box"))
				ignoreExistingmtmProd = para.getParameterAsBoolean(); 
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + paraName);
		}
		/*Check that there's MTM line items
		 * if no MTM, quit with warn dialog
		 * If there is mtm: Check order status: Only create production run when order complete
		 * 					Check that there has not already been a MBLDMtomProduction created from this order.
		 * 					Perhaps have an overide checkbox for bypassing the above check, so users can
		 * 					create duplicate production runs if desired?
		 * 					Create new MBLDMtomProduction passing COrder_ID to constructor
		 * 					Show dialog with number of lines added, with, maybe summary of 
		 * 					each mtm product that was added.
		 * /
		 */
		
		
		
	}

	@Override
	protected String doIt() throws Exception {
		//TODO: Show dialog with number of lines created and hyperlink to mtmproduction created.
		MBLDMtomProduction mBLDMtomProduction = null;
		if(checkOrderStatus())
				{
					if(checkMtmStatus())
						{
							mBLDMtomProduction = createNewMtmProduction();
						}
					else 
						{
							throw new AdempiereUserError("Production with c_order_id: " + C_Order_ID + " exists already");
						}
						
				}
		
		
		if(mBLDMtomProduction == null)return null;
		
		String msg = ("MTM Production Created " + mBLDMtomProduction.getDocumentNo());
		addLog(mBLDMtomProduction.getbld_mtom_production_ID(), null, null, msg, MBLDMtomProduction.Table_ID, mBLDMtomProduction.getbld_mtom_production_ID());
		return "@OK@";
		
	}
	
	private boolean checkOrderStatus()
	{
		MOrder thisOrder = new MOrder(Env.getCtx(), C_Order_ID, null);
		if(!thisOrder.getDocStatus().equals("CL")&&!thisOrder.getDocStatus().equals("CO")) //Is order complete/closed?
			{
				log.log(Level.INFO, "Order not complete or closed, status was: " + thisOrder.getDocStatus());
				throw new AdempiereUserError ("Order " + C_Order_ID + " status not closed or complete");
				
			}
		MOrderLine[] lines = thisOrder.getLines();
		boolean pass = true;
		for(MOrderLine line : lines)
		{
			int mProduct = line.get_ValueAsInt("m_product_id");
			MProduct productToCheck = new MProduct(getCtx(), mProduct, get_TrxName());
			boolean isMtm = productToCheck.get_ValueAsBoolean("ismadetomeasure");
			boolean isManufactured = productToCheck.isManufactured();
			if(isManufactured && isMtm)
			{
				pass = true;
				break;
			}
		}
		return pass;
	}
	/**
	 * checkMtmStatus()
	 * Check if there's an existing MtmProduction with C_Order_ID in it.
	 * 
	 */
	
	private boolean checkMtmStatus()
	{
		if(ignoreExistingmtmProd)return true;
		
		StringBuilder sql = new StringBuilder("SELECT c_order_id ");
		sql.append("FROM bld_mtom_production WHERE ");
		sql.append("c_order_id = '" + C_Order_ID +"'");
		int existingOrder = DB.getSQLValue(get_TrxName(), sql.toString());//returns -1 if no record is found.
		if(existingOrder != -1)
			{
				log.log(Level.INFO, "Production with c_order_id" + C_Order_ID + " exists already");
				return false;
			}
		else return true;
		
	}

	private MBLDMtomProduction createNewMtmProduction()
	{
		MBLDMtomProduction newMtm = new MBLDMtomProduction(getCtx(), null, get_TrxName(), C_Order_ID);
		newMtm.save();
		newMtm.prepareIt();
		newMtm.setIsCreated("Y");
		newMtm.save();
		int newMtm_ID = newMtm.get_ID();
		

if(newMtm_ID != 0)return newMtm;
		else return null;
	}
}
