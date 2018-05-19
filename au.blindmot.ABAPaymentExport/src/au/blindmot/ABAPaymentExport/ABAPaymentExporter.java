/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2018 Phil Barnett                							  *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/

package au.blindmot.ABAPaymentExport;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.compiere.model.I_C_BankAccount;
import org.compiere.model.I_C_PaySelection;
import org.compiere.model.MBankAccount;
import org.compiere.model.MClient;
import org.compiere.model.MCurrency;
import org.compiere.model.MPaySelectionCheck;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.PaymentExport;

public class ABAPaymentExporter implements PaymentExport {
	
	/** Logger								*/
	static private CLogger	s_log = CLogger.getCLogger (ABAPaymentExporter.class);
	
	/** Client & BP bank columns					 */
	private static final String CLIENT_BANK_BSB_COLUMN_NAME = "account_bsb";
	private static final String BP_BANK_BSB_COLUMN_NAME = "accountbsb";
	private static final String BP_BANK_LODGE_REF = "lodgement_reference";
	private static final String CLIENT_FIN_INS_COLUMN_NAME = "financial_institution";
	private static final String CLIENT_BANK_ACCOUNT_NUMBER = "accountno";
	
	private static final int WIDTH = 120;

	/*Things to export:
	 * BP bank window, client bank window,
	 * (non-Javadoc)
	 * @see org.compiere.util.PaymentExport#exportToFile(org.compiere.model.MPaySelectionCheck[], java.io.File, java.lang.StringBuffer)
	 * A good description of the ABA format is here: http://ddkonline.blogspot.com.au/2009/01/aba-bank-payment-file-format-australian.html
	 * Put info from David ABA web here.
	 */ 

	
	public int exportToFile (MPaySelectionCheck[] checks, File file, StringBuffer err) {
		
		System.out.println("ABAPaymentExporter: au.blindmot.ABAPaymentExport.ABAPaymentExporter has been found.");
		
		//Get client name, bank code, trCLIENT_BANK_BSB_COLUMN_NAMEansaction date.
			if (checks == null || checks.length == 0)
				return 0;
			//  Must be a file
			if (file.isDirectory())
			{
				err.append("Selected file is a directory - can't write" + file.getAbsolutePath());
				s_log.log(Level.SEVERE, err.toString());
				return -1;
			}
			//  delete if exists
			try
			{
				if (file.exists())
					file.delete();
			}
			catch (Exception e)
			{
				s_log.log(Level.WARNING, "Could not delete - " + file.getAbsolutePath(), e);
			}

			int noLines = 0;
			try
			{
				FileWriter fw = new FileWriter(file);
				StringBuilder msg = new StringBuilder();                                   
				new MClient(Env.getCtx(),Env.getAD_Client_ID(Env.getCtx()),null);
			
				int    NumberOfTransactions=0;
				BigDecimal CtrlSum = BigDecimal.valueOf(6554.23);
				String ExecutionDate = "2017-12-31";
			
				CtrlSum=BigDecimal.ZERO;
				for (int i = 0; i < checks.length; i++)
				{
					MPaySelectionCheck mpp = checks[i];
					CtrlSum=CtrlSum.add(mpp.getPayAmt());
					NumberOfTransactions++;
				}
				
				MPaySelectionCheck firstCheque = checks[0];
				I_C_PaySelection paySelection = firstCheque.getC_PaySelection();
			
				ExecutionDate = new SimpleDateFormat("ddMMyy").format(new Date());
				
				I_C_BankAccount cBankAccount = paySelection.getC_BankAccount();
				MBankAccount mBankAccount = (MBankAccount)cBankAccount;
				String clientBSB = mBankAccount.get_ValueAsString(CLIENT_BANK_BSB_COLUMN_NAME);
				String clientAccNum = mBankAccount.get_ValueAsString(CLIENT_BANK_ACCOUNT_NUMBER);
				//Used get_ValueAsString("name") because getName() returns unexpected result.
				String clientBankName = mBankAccount.get_ValueAsString("name");
				if(clientBankName.length()>26)clientBankName = clientBankName.substring(0, 26);
				String clientFinIns = mBankAccount.get_ValueAsString(CLIENT_FIN_INS_COLUMN_NAME);
						
				fw.write(getHeader(clientBankName, clientFinIns, ExecutionDate));
			
					//Add detail records
						for (int i = 0; i < checks.length; i++)
						{
							String bpBSB = null;
							String bpAccountNum = null;
							String bpAccountName = null;
							String lodgementRef = null;
							MPaySelectionCheck mPaySelectionCheck = checks[i];

							if (mPaySelectionCheck == null)
								continue;
							BigDecimal Amount=BigDecimal.ZERO;
							String ba[] = getBPartnerAccountInfo(mPaySelectionCheck.getC_BPartner_ID());
							//[0]=accnum,[1]=bsb,[2]=lodge ref,[3]=accname
							bpAccountNum = ba[0];
							bpBSB = ba[1];
							lodgementRef = ba[2];
							bpAccountName = ba[3];
							
							Amount = mPaySelectionCheck.getPayAmt();
							String currency = MCurrency.getISO_Code(Env.getCtx(), mPaySelectionCheck.getParent().getC_Currency_ID());
							System.out.println("Currency is: " + currency);
							//if(currency != "AUD") throw new AdempiereUserError("ERROR! Currency must be $AU");
							
							//Check and trim strings
							if(bpAccountNum == null || bpAccountNum.length()>9)
							{
								throw new AdempiereUserError("Account number for account: " + bpAccountName + " is NULL or too long.");
							}
							if(bpAccountName.length() > 32) bpAccountName = bpAccountName.substring(0, 31);
							if(lodgementRef.length() > 18) lodgementRef = lodgementRef.substring(0, 17);
							
							//Validate BP bank details
							if(bpAccountName == null || bpAccountName == "")
							{
								throw new AdempiereUserError("Bank account missing name.");
							}
							
							if(bpAccountNum == "" || bpAccountNum.length() < 6)
							{
								throw new AdempiereUserError("Account number for account: " + bpAccountName + " is empty or too short.");							
							}
							if(lodgementRef == null || lodgementRef == "")
							{
								throw new AdempiereUserError("Lodgement reference empty for: " + bpAccountName);
							}
							
							
							fw.write(getdetailRecord
							(		splitBSB(bpBSB),
									bpAccountNum,
									Amount,
									bpAccountName,
									lodgementRef,
									splitBSB(clientBSB),
									clientAccNum,
									clientBankName
							));
						
							System.out.println("After msg.append(getDetailRecord): " + msg);
		
						} 
						
				fw.write((getTrailer(CtrlSum, CtrlSum, BigDecimal.ZERO, NumberOfTransactions)));
				fw.flush();
				fw.close();
				noLines=NumberOfTransactions;
			}
			catch (Exception e)
			{
				err.append(e.toString());
				s_log.log(Level.SEVERE, "", e);
				return -1;
			}

			return noLines;
		}   //  exportToFile

	/**
	 * getHeader - first line of ABA file
	 * @param bankAccName
	 * @param bankCode
	 * @param exDate
	 * @return
	 */
	public String getHeader(String bankAccName, String bankCode, String exDate) {
		StringBuilder header = new StringBuilder();
		header.insert(0, "0");
		
		//blank fill 2-18
		for(int i = 1; i < 18; i++)
		{
			header.insert(i, " ");
		}
		
		//set reel sequence no.
		header.insert(18, "01");
		
		header.insert(20, bankCode);
		
		//blank fill 24-30
		for(int i = 23; i < 30; i++)
		{
			header.insert(i, " ");
		}
		
		//Insert user supplying file. Left justified, blank filled
		header.insert(30, getEntry(bankAccName, false, 26, " "));
		
		//insert user number, must be numeric, right justified, zero filled. == 1
		header.insert(56, getEntry("1", true, 6, "0"));
		//description entry. Left justified, blank filled.
		String desc = "PAYMENT";
		header.insert(62, getEntry(desc, false, 18, " "));
		
		//insert execution date DDMMYY
		header.insert(74, getEntry(exDate, false, 46, " "));
		header.setLength(WIDTH);
		header.insert(WIDTH, "\n");
		return header.toString();
		
		
	}
	
	public String getdetailRecord
	(		String bpBSB,
			String bpAccountNum,
			BigDecimal amount,
			String bpAccountName,
			String lodgementRef,
			String clientBSB,
			String clientAccNum,
			String clientAccName) {
		StringBuilder detail = new StringBuilder();
		detail.setLength(WIDTH);
		detail.insert(0, "1");	
		
		detail.insert(1, bpBSB);//Note: BSB to pre formatted to 999-999
		
		//Insert BPAccNum right justified, blank filled
		detail.insert(8, getEntry(bpAccountNum, true, 9, " "));
		detail.insert(17, " ");
		detail.insert(18, "53");
		
		//Amount. Right justified, zero filled. Unsigned. 
		BigDecimal amountInCents = amount.multiply(Env.ONEHUNDRED).setScale(0, RoundingMode.HALF_UP);
		String newAmount = amountInCents.toString();
		detail.insert(20, getEntry(newAmount, true, 10, "0"));
	
		//Account name. Left justified, blank filled.
		int bpaccNameLen = bpAccountName.length();
		if(bpaccNameLen > 32)//Chop if larger than 32
		{
			bpAccountName = bpAccountName.substring(0, 31);
		}
		detail.insert(30, getEntry(bpAccountName, false, 32, " "));
		
		//Lodgement reference, Left justified, blankfilled.
		detail.insert(62, getEntry(lodgementRef, false, 18, " "));
		
		
		//Note: BSB to preformatted to 999-999
		detail.insert(80, clientBSB);
		
		//Account Number right justified, blank filled.
		detail.insert(87, getEntry(clientAccNum, true, 9, " "));
		
		//Client Account name. Left justified, blank filled.
		detail.insert(96, getEntry(clientAccName, false, 16, " "));
		detail.insert(112, "00000000");
		detail.setLength(WIDTH);
		detail.insert(WIDTH, "\n");
		return detail.toString();
		
	}//getDetail()
	

	/**
	 * getTrailer - the last line of ABA file
	 * @param netTotal
	 * @param crTotal
	 * @param dbTotal
	 * @param count
	 * @return
	 */
	public String getTrailer(BigDecimal netTotal, BigDecimal crTotal, BigDecimal dbTotal, int count) {
		StringBuilder trailer = new StringBuilder();
		trailer.setLength(WIDTH);
		
		trailer.insert(0, "7999-999");
		
		for(int i = 8; i < 20; i++)
		{
			trailer.insert(i, " ");
		}
		
		//Net total. Show in cents without punctuation. Right justified, zero filled. Unsigned.
		//TODO: get netTotal in cents. Is it int? BigDecimal?
		BigDecimal amountInCents = netTotal.multiply(Env.ONEHUNDRED).setScale(0, RoundingMode.HALF_UP);
		String newAmount = amountInCents.toString();
		trailer.insert(20, getEntry(newAmount, true, 10, "0"));
		
		/*
		 *Credit total. Must equal the accumulated total of credit Detail Record amounts. 
		 *Right justified, zero filled. Unsigned.
		 */
		BigDecimal crTotalInCents = netTotal.multiply(Env.ONEHUNDRED).setScale(0, RoundingMode.HALF_UP);
		String newCrAmount = crTotalInCents.toString();
		trailer.insert(30, getEntry(newCrAmount, true, 10, "0"));
	
		
		 /* Debit total. Numeric only valid. Mustequal the accumulated total of debit Detail Record amounts.
		 * Right justified, zero filled. Unsigned
		 */
		BigDecimal dbTotalInCents = dbTotal.multiply(Env.ONEHUNDRED).setScale(0, RoundingMode.HALF_UP);
		String dTotal = dbTotalInCents.toString();
		trailer.insert(40, getEntry(dTotal, true, 10, "0"));
		trailer.insert(50, getEntry("", true, 24, " "));
		
		
		 /*File (User) count of Records Type detail
		 * Numeric only valid. Must equal accumulated number of Record Type 1 items on the file. Right justified, zero filled. 
		 */
		String theCount = new Integer(count).toString();
		trailer.insert(74, getEntry(theCount, true, 6, "0"));
		
		for(int i = 80; i < WIDTH; i++)
		{
			trailer.insert(i, " ");
		}
		trailer.setLength(WIDTH);
		trailer.insert(WIDTH, "\n");
		return trailer.toString();
	}//getTrailer

	public String getFilenameSuffix() {
		return ".aba";
	}
	
	/**
	 *  Get Vendor/Customer Bank Account Information
	 *  Based on BP_ static variables
	 *  @param C_BPartner_ID BPartnersplitBSB(String bsb
	 *  @return info array
	 */
	private static String[] getBPartnerAccountInfo (int C_BPartner_ID)
	{
		String[] bankAccount = new String[4];

		//[0]=accnum,[1]=bsb,[2]=lodge ref,[3]=accname
		StringBuilder sql = new StringBuilder ("select ba.accountno, ");
			sql.append("ba." + BP_BANK_BSB_COLUMN_NAME + ", ");
			sql.append("ba." + BP_BANK_LODGE_REF + ", ");
			sql.append("ba.a_name ");
			sql.append("from c_bp_bankaccount ba ");
			sql.append("where ba.c_bpartner_id=? ");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, C_BPartner_ID);
			rs = pstmt.executeQuery();
			//
			if (rs.next())
			{
				bankAccount[0] = rs.getString(1);
				if (bankAccount[0] == null)
					bankAccount[0] = "";
				bankAccount[1] = rs.getString(2);
				if (bankAccount[1] == null)
					bankAccount[1] = "";
				bankAccount[2] = rs.getString(3);
				if (bankAccount[2] == null)
					bankAccount[2] = "";
				bankAccount[3] = rs.getString(4);
				if (bankAccount[3] == null)
					bankAccount[3] = "";
			}
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		return bankAccount;
	}   //  getBPartnerAccountInfo
	
	public String splitBSB(String bsb) {
		StringBuilder splitBSB = new StringBuilder(bsb);
		splitBSB.setLength(7);
		splitBSB.insert(3, "-");
		return splitBSB.toString();
	}
	
	public String getEntry(String entry, boolean justifiedRight, int fieldLength, String fillChar) {
		//Account name. Left justified, blank filled.
		StringBuilder entryReturn = new StringBuilder();
		int entryLen = entry.length();
		int insertPos = fieldLength - entryLen;
		
		if(justifiedRight)
		{
				
			for(int j = 0; j < insertPos ; j++)
			{
					entryReturn.insert(j, fillChar);//fill from beginning of entryReturn to beginning of entry.
			}
				entryReturn.insert(insertPos, entry);
				return entryReturn.toString();
		}
		else//It's left justified
		{
			entryReturn.insert(0, entry);
			for(int j = entryLen; j < fieldLength; j++)
			{
					entryReturn.insert(j, fillChar);//fill to end of entry Return from end of entry.
			}
			return entryReturn.toString();
		}
	}
}
