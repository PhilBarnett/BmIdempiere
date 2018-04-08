package au.blindmot.remittance;

import java.math.BigDecimal;
import java.util.logging.Level;

import javax.sql.RowSet;

import org.compiere.model.MInvoice;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

public class Remittance extends SvrProcess {
	private int c_PaySelection_ID = 0;
	private boolean includePaid = false;
	private int check_ID = 0;
	private StringBuilder mailText;

	@Override
	protected void prepare() {

		ProcessInfoParameter[] paras = getParameter();
		for(ProcessInfoParameter para : paras)
		{
			String paraName = para.getParameterName();
			if(paraName.equalsIgnoreCase("C_PaySelection_ID"))
				c_PaySelection_ID = para.getParameterAsInt();
			else if(paraName.equalsIgnoreCase("check_box"))
				includePaid = para.getParameterAsBoolean(); 
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + paraName);
		}
		
	}

	@Override
	protected String doIt() throws Exception {
		/*
		 }*TODO Auto-generated method stub
		 *SELECT * FROM c_payselectionline cp WHERE cp.c_payselection_id = '1000009' ORDER BY cp.c_payselectioncheck_id
		 *Iterate through result set, build a string of pay advice for each invoice on the same cp.c_payselectioncheck_id
		 *Send email as each cp.c_payselectioncheck_id is completed.
		 *Use a record attached to BP called 'remittance'; if doesn't exist send to first contact with an email address.
		 *Use a mail template called 'Remittacnce', if doesn't exist just send as is.
		 *Check that email is sent; if not, resend (how many times?)
		 *Alert user as to success or failure - message.
		 *
		 */
		StringBuilder sql = new StringBuilder("SELECT c_payselectioncheck_id, c_invoice_id, processed, payamt ");
		sql.append("FROM c_payselectionline cp ");
		sql.append("WHERE cp.c_payselection_id = ");
		sql.append(c_PaySelection_ID);
		sql.append(" ORDER BY cp.c_payselectioncheck_id");
		
		RowSet rowSet = DB.getRowSet(sql.toString());
	
		while (rowSet.next()) {  
            int checkID = rowSet.getInt(1);
			int invoiceID =  rowSet.getInt(2);
			boolean processed = rowSet.getBoolean(3);
			BigDecimal payamt = rowSet.getBigDecimal(4);
			prepareEmail(checkID, invoiceID, processed, payamt);
		}
		
		
		//String msg = ("Remittance advice sent: " + mBLDMtomProduction.getDocumentNo());
		//addLog(mBLDMtomProduction.getbld_mtom_production_ID(), null, null, msg, MBLDMtomProduction.Table_ID, mBLDMtomProduction.getbld_mtom_production_ID());
		return "@OK@";
		
		//return null;
	}
	
	private void prepareEmail(int checkID, int invoiceID, boolean processed, BigDecimal payamt) {
		if(checkID != check_ID)
		{
			//send email with mailText
			//reset check_ID
		}
		
		
	}
	private String getInvPO(int invoiceID) {
		MInvoice invoice = new MInvoice(getCtx(), invoiceID, null);
		return invoice.getPOReference();
		
	}

}

