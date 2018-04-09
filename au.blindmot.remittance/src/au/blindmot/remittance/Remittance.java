package au.blindmot.remittance;

import java.math.BigDecimal;
import java.util.logging.Level;

import javax.sql.RowSet;

import org.compiere.model.MClient;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrg;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MPaySelection;
import org.compiere.model.X_I_BPartner;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;

public class Remittance extends SvrProcess {
	private int c_PaySelection_ID = 0;
	private boolean includePaid = false;
	private int check_ID = 0;
	private StringBuilder mailText;
	private StringBuilder mailSubject;
	private StringBuilder mailResult = new StringBuilder();
	private static String REMITTANCE_HEADER = "Our ref" + "\t" + "Your ref" + "\t" + "Amount";
	private int count = 0;
	MClient client = null;

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
		client = new MClient(getCtx(), Env.getAD_Client_ID(getCtx()), false, get_TrxName());
		mailSubject.append("Remittance advice from ");
		mailSubject.append(client.getName());
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
			
		
		String msg = mailResult.toString();
		addLog(c_PaySelection_ID, null, null, msg, MPaySelection.Table_ID, c_PaySelection_ID);
		return "@OK@";
		
	}
	
	private void prepareEmail(int checkID, int invoiceID, boolean processed, BigDecimal payamt) {
		if(count == 0)checkID = check_ID;
		if((checkID != check_ID))//It's a new cheque, start again.
		{
			count = 0;
			MOrg mOrg = new MOrg(getCtx(), Env.getAD_Org_ID(getCtx()), get_TrxName());
			MOrgInfo orgInfo = mOrg.getInfo();
			String to = getInvBPEmail(invoiceID);
			EMail email = new EMail(client, orgInfo.getEMail(), to, mailSubject.toString(), mailText.toString());
			if(!email.send().equalsIgnoreCase("OK)"))//try twice then write error
				{
					if(!email.send().equalsIgnoreCase("OK)"))
					{
						mailResult.append("Failed to send email to: ");
						mailResult.append(to);
						mailResult.append("\n");
					}
					else
					{
						successMail(to);
					}
				}
				else
				{
					successMail(to);
				}
					
			mailText = new StringBuilder();
			mailText.append(REMITTANCE_HEADER);//Clean mailtText ready for next cheque.
		}
		{
			if(isInvoicePaid(invoiceID))
			{
				mailText.append(invoiceID);
				mailText.append("\t");
				mailText.append(getInvPO(invoiceID));
				mailText.append("\t");
				mailText.append("$" + payamt);
				mailText.append("\n");
				check_ID = checkID;
				count ++;
			}
		
		
	}
}
	private String getInvPO(int invoiceID) {
		MInvoice invoice = new MInvoice(getCtx(), invoiceID, get_TrxName());
		return invoice.getPOReference();
		
	}
	private boolean isInvoicePaid(int invoiceID) {
		MInvoice invoice = new MInvoice(getCtx(), invoiceID, get_TrxName());
		return invoice.isPaid();
		
	}
	
	private String getInvBPEmail(int invoiceID) {
		MInvoice invoice = new MInvoice(getCtx(), invoiceID, get_TrxName());
		int bP_ID = invoice.getC_BPartner_ID();
		X_I_BPartner bp = new X_I_BPartner(getCtx(),bP_ID, get_TrxName());
		return bp.getEMail();
		
	}
	
	private void successMail(String toAddress) {
			mailResult.append("Successfully sent email to: ");
			mailResult.append(toAddress);
			mailResult.append("\n");
	}

}

