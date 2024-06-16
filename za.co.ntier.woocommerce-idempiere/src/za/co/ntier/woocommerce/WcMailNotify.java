package za.co.ntier.woocommerce;

import java.util.Properties;

import org.compiere.model.MClient;
import org.compiere.model.MUser;
import org.compiere.util.EMail;
import org.compiere.util.Env;

public class WcMailNotify {
	private static StringBuilder mailSubject = new StringBuilder("Woocommerce sync message");
	private static String DEFAULT_FROM = "system@idempiere.com";
	private StringBuilder  mailBody = new StringBuilder();
	private StringBuilder mailHeader = new StringBuilder();
	private static MClient client = null;
	/**
	 * 
	 * @param to
	 * @param body
	 * @param ctx
	 * @param trxnName
	 * @return
	 */
	public static String sendEmail(String to, String body, String subject, Properties ctx, String trxnName) {
		//String to = getBPEmail(bpID);
		StringBuilder mailResult = new StringBuilder();
		client = new MClient(ctx, Env.getAD_Client_ID(ctx), false, trxnName);
		MUser currentUser = new MUser(Env.getCtx(), Env.getAD_User_ID(Env.getCtx()), trxnName);
		String from = currentUser.getEMail();
		if(from == null)
		{
			from = DEFAULT_FROM;
		}
		String emailSubject = subject;
		if (subject == "" || subject == null)
		{
			emailSubject = mailSubject.toString();
		}
	
		EMail email = new EMail(client, from, to, emailSubject, body);
		//if(from==null)throw new AdempiereUserError("Could not find an email address for: " + currentUser.getName() + ". Check your email in your User record.");
		
		String username = currentUser.getEMailUser();//TODO: change to system defaults
		String password = currentUser.getEMailUserPW(); //TODO: change to system defaults
		email.createAuthenticator(username, password);
		
		if(!email.send().equalsIgnoreCase("OK"))//try twice then write error
			{
				if(!email.send().equalsIgnoreCase("OK"))
				{
					mailResult.append("Failed to send email to: ");
					mailResult.append(to);
					mailResult.append("\n");
				}
				else
				{
					successMail(to, mailResult);
				}
			}
			else
			{
				successMail(to, mailResult);
			}
		return mailResult.toString();
	}
	
	
	/*private void setHeaderAndText() {
		if(!(mMailTextID > 0))
		{
			mailHeader.append(REMITTANCE_HEADER);
			mailBody.append(DEFAULT_MAILBODY);
			subject = DEFAULT_SUBJECT;
		}
		else
		{
			MMailText text = new MMailText(Env.getCtx(), mMailTextID, null);
			mailHeader.append(text.getMailText2());
			mailBody.append(text.getMailText());
			subject = text.getMailHeader();
		}
		
		String[] header = mailHeader.toString().split(",");
		StringBuilder tabbedHeader = new StringBuilder();
		for(int x =0; x < header.length; x++)
		{
			tabbedHeader.append(header[x]);
			tabbedHeader.append("\t");
		}
		tabbedHeader.append("\n");
		
		mailHeader=tabbedHeader;
		mailBody.append("\n" + mailHeader);
	} */
	
	/**
	 * 
	 * @param toAddress
	 */
	private static StringBuilder successMail(String toAddress, StringBuilder mailResult) {
		mailResult.append("Successfully sent email to: ");
		mailResult.append(toAddress);
		mailResult.append("\n");
		return mailResult; 
	}
}
