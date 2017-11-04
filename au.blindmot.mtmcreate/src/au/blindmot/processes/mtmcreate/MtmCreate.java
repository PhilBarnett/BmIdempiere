/**
 * 
 */
package au.blindmot.processes.mtmcreate;

import java.math.BigDecimal;
import java.util.logging.Level;

import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.Env;

/**
 * @author phil
 * A process to create MBLDMtomProduction form orders
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
			if(paraName.equalsIgnoreCase("Order_ID"))
				C_Order_ID = para.getParameterAsInt();
			else if(paraName.equalsIgnoreCase("Overwrite_existing"))
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
		
		if(checkOrderStatus() && checkMtmStatus())
		{
			createNewMtmProduction();
		}
		
	}

	@Override
	protected String doIt() throws Exception {
		//TODO: Show dialog with number of lines created and hyperlink to mtmproduction created.
		
		return null;
	}
	
	private boolean checkOrderStatus()
	{
		MOrder thisOrder = new MOrder(Env.getCtx(), C_Order_ID, null);
		if(!thisOrder.getDocStatus().equals("CL")) 
			{
				throw new AdempiereUserError ("Order " + C_Order_ID + " status not closed");
			}
		MOrderLine[] lines = thisOrder.getLines();
		boolean pass = true;
		for(MOrderLine line : lines)
		{
			int mProduct = line.get_ValueAsInt("m_product_id");
			MProduct productToCheck = new MProduct(getCtx(), mProduct, get_TrxName());
			String isMtm = productToCheck.get_ValueAsString("ismadetomeasure");
			boolean isManufactured = productToCheck.isManufactured();
			if(!isManufactured && isMtm != "Y")
			{
				pass = false;
				break;
			}
		}
		return pass;
	}
	
	private boolean checkMtmStatus()
	{
		if(ignoreExistingmtmProd)return true;
		//TODO: Check if there's an existing MtmProduction with C_Order_ID in it.
		return false;
	}

	private boolean createNewMtmProduction()
	{
		return false;
	}
}
