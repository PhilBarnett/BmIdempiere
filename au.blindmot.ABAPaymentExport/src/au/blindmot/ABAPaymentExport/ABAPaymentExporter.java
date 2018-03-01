package au.blindmot.ABAPaymentExport;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
	
	/** BPartner Info Index for Value       */
	private static final int     BP_VALUE = 0;
	/** BPartner Info Index for Name        */
	private static final int     BP_NAME = 1;
	/** BPartner Info Index for Contact Name    */
	private static final int     BP_CONTACT = 2;
	/** BPartner Info Index for Address 1   */
	private static final int     BP_ADDR1 = 3;
	/** BPartner Info Index for Address 2   */
	private static final int     BP_ADDR2 = 4;
	/** BPartner Info Index for City        */
	private static final int     BP_CITY = 5;
	/** BPartner Info Index for Region      */
	private static final int     BP_REGION = 6;
	/** BPartner Info Index for Postal Code */
	private static final int     BP_POSTAL = 7;
	/** BPartner Info Index for Country     */
	private static final int     BP_COUNTRY = 8;
	/** BPartner Info Index for Reference No    */
	private static final int     BP_REFNO = 9;
	/** Client & BP bank columns					 */
	private static final String CLIENT_BANK_BSB_COLUMN_NAME = "account_bsb";
	private static final String BP_BANK_BSB_COLUMN_NAME = "accountbsb";
	private static final String BP_BANK_LODGE_REF = "lodgement_reference";
	private static final String CLIENT_BANK_ACCOUNT_COLUMN_NAME = "account_name";
	private static final String CLIENT_FIN_INS_COLUMN_NAME = "financial_institution";
	private static final String CLIENT_BANK_ACCOUNT_NUMBER = "acount_number";
	
	private static final int WIDTH = 120;
	

	/*
	 * (non-Javadoc)
	 * @see org.compiere.util.PaymentExport#exportToFile(org.compiere.model.MPaySelectionCheck[], java.io.File, java.lang.StringBuffer)
	 * 
	 * Put info from David ABA web here.
	 * 
1. Definitions
Commonly used terms associated with file formatting and their definitions are as follows:

    Left justified - start input in the first character position of that field
    Right justified - end input in the last character position of that field
    Blank filled - fills the unused portion of that field with blank spaces
    Zero filled - fills the unused portion of that field with zeros
    Unsigned - used in amount fields. Amounts will not be specified as debit or credit.

2. Header Record Definition ('0' record) (just the first line):
Character Position	Field size	Field description	Specification
1 	1 	Record Type 0 	Must be '0'
2-18 	17 	Blank 	Must be blank filled.
19-20 	2 	Reel Sequence Number 	Must be numericcommencing at 01. Right justified. Zero filled
21-23 	3 	Name of User's FinancialInstitution 	Must be approvedFinancial Institution abbreviation. Westpac's abbreviation is "WBC".
24-30 	7 	Blank 	Must be blank filled.
31-56 	26 	Name of User supplying file	Must be User PreferredSpecification as advised in Application. Left justified, blank filled. Allcoded character set valid. Must not be all blanks.
57-62	6	Number of User supplyingfile	Must be UserIdentification Number which is allocated by APCA. Must be numeric, rightjustified, zero filled.
63-74 	12 	Description of entries onfile e.g. "PAYROLL"	All coded character setvalid. Must not be all blanks. Left justified, blank filled.
75-80 	6	Date to be processed(i.e. the date transactions are released to all Financial Institutions)	Must be numeric in theformat of DDMMYY. Must be a valid date. Zero filled.
81-120 	40	Blank	Must be blank filled.

3. Detail Record ('1' record)
Character Position	Field size	Field description 	Specification
1 	1 	Record Type 1 	Must be '1'
2-8 	7 	Bank/State/Branch Number 	Must be numeric with a hyphen in character position 5. Character positions 2 and 3 must equalvalid Financial Institution number. Character position 4 must equal avalid State number (0-9).
9-17 	9 	Account number to be credited/debited 	Numeric, hyphens and blanksonly are valid. Must not contain all blanks or zeros. Leading zeros whichare part of a valid account number must be shown, e.g. 00-1234. Westpacrecommends that (except in the above example), ALL hyphens are edited out.Where account number exceeds nine characters, edit out hyphens. Rightjustified, blank filled.
18 	1 	Indicator 	"N" -for new or variedBank(FI)/State/Branch number or name details, otherwise blank filled.Withholding Tax Indicators: "W" -dividend paid to a resident of a countrywhere a double tax agreement is in force. "X" -dividend paid to a residentof any other country. "Y" -interest paid to all non-residents The amountof withholding tax is to appear in character positions113-120. Note: Where withholding tax has been deducted the appropriateIndicator as shown above is to be used and will override the normalindicator.
19-20 	2 	Transaction Code 	Must only be valid industry standard trancodes (see list). Only numeric valid.
21-30 	10 	Amount 	Only numeric valid. Mustbe greater than zero. Shown in cents without punctuations. Rightjustified, zero filled. Unsigned.
31-62 	32 	Title of Account to be	All coded character setvalid. Must not be all blanks.
		credited/debited 	Left justified, blankfilled. Desirable format: - surname (period) blank
			- given names with blank between each name
63-80 	18 	Lodgement Reference 	All coded character set valid. Reference as submitted by the User indicating details of the originof the entry e.g. Payroll number, invoice, contract number.
			Left justified, blank filled. Must not contain all blanks.
81-96 (81-87) 	16	Trace Record (-BSB Number in format XXX-XXX) 	Bank(FI)/State/Branch andaccount number of User to enable retracing of the entry to its source ifnecessary. Only numeric and hyphens valid. Character positions 81 & 82must equal a valid Financial Institution number. Character position 83must equal a valid State number (0-9). Character position 84 must be ahyphen.
(88-96) 	9 	(-Account Number) 	Right justified, blank filled.
97-112 	16 	Name of Remitter 	Name of originator of the entry. This may vary from Name of the User. All coded character set valid.Must not contain all blanks. Left justified, blank filled.

113-	8 	Amount of 			Numeric only valid. Show in cents without punctuation.
120 		Withholding Tax 	Right justified, zero filled. Unsigned.

4. File Total Record ‘7’ (Trailer)
Character Position	Field size	Field description 	Specification
1 	1 	Record Type 7 	Must be '7'.
2-8 	7 	BSB Format Filler 	Must be '999-999'.
9-20 	12 	Blank 	Must be blank filled.
21-30 	10 	File (User) Net Total Amount	Numeric only valid. Mustequal the difference between File Credit & File Debit Total Amounts.Show in cents without punctuation. Right justified, zero filled. Unsigned.
31-40 	10 	File (User) Credit TotalAmount 	Numeric only valid. Mustequal the accumulated total of credit Detail Record amounts. Show in centswithout punctuation. Right justified, zero filled. Unsigned.
41-50 	10 	File (User) Debit TotalAmount 	Numeric only valid. Mustequal the accumulated total of debit Detail Record amounts. Show in centswithout punctuation. Right justified, zero filled. Unsigned.
51-74 	24 	Blank 	Must be blank filled.
75-80 	6 	File (User) count ofRecords Type 1 	Numeric only valid. Mustequal accumulated number of Record Type 1 items on the file. Rightjustified, zero filled.
81-120 	40 	Blank 	Must be blank filled.

5. Direct Entry Transaction Codes
13	Externally initiateddebit items
50	Externally initiatedcredit items with the exception of those bearing Transaction Codes51-57 inclusive
51	Australian GovernmentSecurity Interest
52	Family Allowance
53	Pay
54	Pension
55	AllotmentCLIENT_BANK_BSB_COLUMN_NAME
56	Dividend
57	Debenture/Note Interest string char
	 * 
	 * 
	 */
	
	public int exportToFile (MPaySelectionCheck[] checks, File file, StringBuffer err) {
		//return exportToFile (checks, false, (String)null, file, err);
		//Delete above line once found to be unnecessary.
		
		
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

			char x = '"';      //  ease
			int noLines = 0;
			StringBuffer line = null;
			try
			{
				FileWriter fw = new FileWriter(file);

				
				StringBuilder msg = new StringBuilder();
				//Split bsb for org;
				//StringBuffer msg=null;
				
				
				MClient mClient = new MClient(Env.getCtx(),Env.getAD_Client_ID(Env.getCtx()),null);
				int adOrgID = mClient.getAD_Org_ID();
				
				/*TODO:
				 * Get Org bank details with adOrgID - sql query
				 * SELECT ... FROM c_bankaccount ... AND paymentexportclass NOT NULL;
				 * Create String (same for all detail lines) with org bank account name acc bsb for detail record
				 * Split bsb for org
				 * iterate through checks , getting BP bank info.
				 * Split bsb for BP with hyphen in middle.
				 * Use trans code 53	Pay
				 * Get value of trans in cents
				 * Get account name
				 * Get lodgement ref from BP
				 * 
				 * Calculate Debit TotalAmount
				 * Calculate Credit TotalAmount 
				 * Calculate net TotalAmount - credit minus debit
				 * 
				 * Calculate number of payment records
				 */
				
				
				//delete from here
				String MsgId="Message-xxxxx";
				String CreationDate="2011-12-32T09:30:47.000Z";
				int    NumberOfTransactions=0;
				String InitiatorName=mClient.getName();
				BigDecimal CtrlSum = BigDecimal.valueOf(6554.23);
				String ExecutionDate = "2011-12-31";
				String Dbtr_Name = mClient.getName();
			
				//To here?
				
				CtrlSum=BigDecimal.ZERO;
				for (int i = 0; i < checks.length; i++)
				{
					MPaySelectionCheck mpp = checks[i];
					CtrlSum=CtrlSum.add(mpp.getPayAmt());
					NumberOfTransactions++;
				}
				
				MPaySelectionCheck firstCheque = checks[0];
				I_C_PaySelection paySelection = firstCheque.getC_PaySelection();
				
				MsgId = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(paySelection.getCreated());
				
				ExecutionDate = new SimpleDateFormat("DDMMYY").format(paySelection.getPayDate());
				CreationDate = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
									+"T"
									+new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())
									+".000Z";
				
				I_C_BankAccount cBankAccount = paySelection.getC_BankAccount();
				MBankAccount mBankAccount = (MBankAccount)cBankAccount;
				String clientBSB = mBankAccount.get_ValueAsString(CLIENT_BANK_BSB_COLUMN_NAME);
				String clientAccNum = mBankAccount.get_ValueAsString(CLIENT_BANK_ACCOUNT_NUMBER);
				String clientBankName = mBankAccount.getName();
				String clientFinIns = mBankAccount.get_ValueAsString(CLIENT_FIN_INS_COLUMN_NAME);
						
				
				msg.append(getHeader(clientBankName, clientFinIns, ExecutionDate));
				
						
						for (int i = 0; i < checks.length; i++)
						{
							String bpBSB = null;
							String bpAccountNum = null;
							BigDecimal amount = null;
							String bpAccountName = null;
							String lodgementRef = null;
							MPaySelectionCheck mPaySelectionCheck = checks[i];

							if (mPaySelectionCheck == null)
								continue;
							String PmtId=String.valueOf(mClient.getAD_Client_ID())+"-"+String.valueOf(mPaySelectionCheck.get_ID());
							//"OriginatorID1235";
							BigDecimal Amount=BigDecimal.ZERO;
							String AmountAsString ="";
					
							
							String bp[] = getBPartnerInfo(mPaySelectionCheck.getC_BPartner_ID());
							String ba[] = getBPartnerAccountInfo(mPaySelectionCheck.getC_BPartner_ID());
							//[0]=accnum,[1]=bsb,[2]=lodge ref,[3]=accname
							bpAccountNum = ba[0];
							bpBSB = ba[1];
							lodgementRef = ba[2];
							bpAccountName = ba[3];
							
							Amount = mPaySelectionCheck.getPayAmt();
							AmountAsString = String.valueOf(Amount);
							
							String currency = MCurrency.getISO_Code(Env.getCtx(), mPaySelectionCheck.getParent().getC_Currency_ID());
							System.out.println("Currency is: " + currency);
							//if(currency != "AUD") throw new AdempiereUserError("ERROR! Currency must be $AU");
							
							
							msg.append(getdetailRecord
							(		splitBSB(bpBSB),
									bpAccountNum,
									Amount,
									bpAccountName,
									lodgementRef,
									splitBSB(clientBSB),
									clientAccNum,
									clientBankName
							));
		
						} 
						
						
				msg.append(getTrailer(CtrlSum, CtrlSum, BigDecimal.ZERO, NumberOfTransactions));
				
				fw.write(msg.toString());

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
		header.setLength(WIDTH);
		header.insert(0, "0");
		
		//blank fill 2-18
		for(int i = 1; i < 18; i++)
		{
			header.insert(i, " ");
		}
		
		//set reel sequence no.
		header.insert(18, "01");
		
		//TODO: create DB column for bank code
		header.insert(18, bankCode);
		
		//blank fill 24-30
		for(int i = 23; i < 29; i++)
		{
			header.insert(i, " ");
		}
		
		//Insert user supplying file
		header.insert(30, bankAccName);
		
		int fill = bankAccName.length();
		//blank fill end user supplying file to 56
		for(int i = 30+fill; i < 56; i++)
		{
			header.insert(i, " ");
		}
		
		//insert user number & description entry
		String descAndEntry = "000001PAYMENT";
		header.insert(56, descAndEntry);
		fill = descAndEntry.length();
		for(int i = 56+fill; i < 74; i++)
		{
			header.insert(i, " ");
		}
		
		//insert execution date DDMMYY
		header.insert(74, exDate);
		for(int i = 80; i < WIDTH; i++)
		{
			header.insert(i, " ");
		}
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
		
		detail.insert(1, bpBSB);//Note: BSB to preformatted to 999-999
		
		StringBuilder accountNum = new StringBuilder(bpAccountNum);
		int length = accountNum.length();
		int insertPos = 17 - length;
		detail.insert(insertPos, accountNum.toString());
		for(int i = 8; i < insertPos; i++)
		{
			detail.insert(i, " ");
		}
		
		detail.insert(17, " ");
		detail.insert(18, "53");
		
		//Amount. Rightjustified, zero filled. Unsigned. 
		BigDecimal amountInCents = amount.divide(Env.ONEHUNDRED);
		StringBuilder newAmount = new StringBuilder(amountInCents.toString());
		int amLength = newAmount.length();
		int insertPos1 = 30 - amLength;
		detail.insert(insertPos1, newAmount.toString());
		for(int i = 20; i < insertPos1; i++)
		{
			detail.insert(i, "0");
		}
		
		//Account name. Left justified, blank filled.
		int bpaccNameLen = bpAccountName.length();
		if(bpaccNameLen > 32)//Chop if larger than 32
		{
			bpAccountName = bpAccountName.substring(0, 31);
		}
		detail.insert(30, bpAccountName);
		for(int j = (30+bpAccountName.length()); j < 62; j++)
		{
			detail.insert(j, " ");
		}
		
		 //Lodgement reference, Left justified, blankfilled.
		int lodgeRefLen = lodgementRef.length();
		if(lodgeRefLen > 18)//Chop if larger than 32
		{
			lodgementRef = lodgementRef.substring(0, 17);
		}
		detail.insert(62, lodgementRef);
		for(int j = (62+lodgementRef.length()); j < 80; j++)
		{
			detail.insert(j, " ");
		}
		
		//Note: BSB to preformatted to 999-999
		detail.insert(80, clientBSB);
		
		int clientAccNumLen = clientAccNum.length();
		int insertpos = 95 - clientAccNumLen;
		detail.insert(insertpos, clientAccNum);
		for(int j = (88 +insertpos); j < 62; j++)
		{
			detail.insert(j, " ");
		}
		
		int clientaccNameLen = clientAccName.length();
		if(clientaccNameLen > 32)//Chop if larger than 32
		{
			clientAccName = clientAccName.substring(0, 31);
		}
		int insertpos1 = 112 - clientAccName.length();
		detail.insert(insertpos1, clientAccName);
		for(int j = 96; j < 112 - insertpos1; j++)
		{
			detail.insert(j, " ");
		}
		
		detail.insert(87, "L00000000");
		return detail.toString();
		
	}
	

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
		StringBuilder nTotal = new StringBuilder (netTotal.toString());
		int length = nTotal.length();
		int insertPos = 30 - length;
		trailer.insert(insertPos, nTotal.toString());
		for(int i = 20; i < insertPos; i++)
		{
			trailer.insert(i, " ");
		}
		
		//Credit total. Mustequal the accumulated total of credit Detail Record amounts.
		StringBuilder cTotal = new StringBuilder (crTotal.toString());
		int length1 = cTotal.length();
		int insertPos1 = 40 - length1;
		trailer.insert(insertPos1, cTotal.toString());
		for(int i = 30; i < insertPos1; i++)
		{
			trailer.insert(i, "0");
		}
		
		//Debit total. Numeric only valid. Mustequal the accumulated total of debit Detail Record amounts.
		StringBuilder dTotal = new StringBuilder (dbTotal.toString());
		int length2 = dTotal.length();
		int insertPos2 = 50 - length2;
		trailer.insert(insertPos2, dTotal.toString());
		for(int i = 40; i < insertPos1; i++)
		{
			trailer.insert(i, "0");
		}
		
		for(int i = 50; i < 74; i++)
		{
			trailer.insert(i, "0");
		}
		
		StringBuilder reCount = new StringBuilder(count);
		int length3 = reCount.length();
		int insertPos3 = 80 - length3;
		trailer.insert(insertPos3, reCount.toString());
		for(int i = 75; i < insertPos3; i++)
		{
			trailer.insert(i, "0");
		}
		
		for(int i = 81; i < WIDTH; i++)
		{
			trailer.insert(i, " ");
		}
		
		return trailer.toString();
	}

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
/*** as1 changed to simplify account management
		String sql = "select ba.accountno,b.routingno "
					+"from c_bp_bankaccount ba,c_bank b "
					+"where ba.c_bpartner_id=? "
					+"and ba.c_bank_id = b.c_bank_id " 
					;
****/	
		//[0]=accnum,[1]=bsb,[2]=lodge ref,[3]=accname
		StringBuilder sql = new StringBuilder ("select ba.accountno, ");
			sql.append("ba." + BP_BANK_BSB_COLUMN_NAME + ", ");
			sql.append("ba." + BP_BANK_LODGE_REF + ", ");
			sql.append("ba.a_name, ");
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
	
	/**
	 *  Get Customer/Vendor Info.
	 *  Based on BP_ static variables
	 *  @param C_BPartner_ID BPartner
	 *  @return info array
	 */
	private static String[] getBPartnerInfo (int C_BPartner_ID)
	{
		String[] bp = new String[10];

		String sql = "SELECT bp.Value, bp.Name, c.Name AS Contact, "
			+ "a.Address1, a.Address2, a.City, r.Name AS Region, a.Postal, "
			+ "cc.Name AS Country, bp.ReferenceNo "
			+ "FROM C_BPartner bp, AD_User c, C_BPartner_Location l, C_Location a, C_Region r, C_Country cc "
			+ "WHERE bp.C_BPartner_ID=?"        // #1
			+ " AND bp.C_BPartner_ID=c.C_BPartner_ID(+)"
			+ " AND bp.C_BPartner_ID=l.C_BPartner_ID"
			+ " AND l.C_Location_ID=a.C_Location_ID"
			+ " AND a.C_Region_ID=r.C_Region_ID(+)"
			+ " AND a.C_Country_ID=cc.C_Country_ID "
			+ "ORDER BY l.IsBillTo DESC";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, C_BPartner_ID);
			rs = pstmt.executeQuery();
			//
			if (rs.next())
			{
				bp[BP_VALUE] = rs.getString(1);
				if (bp[BP_VALUE] == null)
					bp[BP_VALUE] = "";
				bp[BP_NAME] = rs.getString(2);
				if (bp[BP_NAME] == null)
					bp[BP_NAME] = "";
				bp[BP_CONTACT] = rs.getString(3);
				if (bp[BP_CONTACT] == null)
					bp[BP_CONTACT] = "";
				bp[BP_ADDR1] = rs.getString(4);
				if (bp[BP_ADDR1] == null)
					bp[BP_ADDR1] = "";
				bp[BP_ADDR2] = rs.getString(5);
				if (bp[BP_ADDR2] == null)
					bp[BP_ADDR2] = "";
				bp[BP_CITY] = rs.getString(6);
				if (bp[BP_CITY] == null)
					bp[BP_CITY] = "";
				bp[BP_REGION] = rs.getString(7);
				if (bp[BP_REGION] == null)
					bp[BP_REGION] = "";
				bp[BP_POSTAL] = rs.getString(8);
				if (bp[BP_POSTAL] == null)
					bp[BP_POSTAL] = "";
				bp[BP_COUNTRY] = rs.getString(9);
				if (bp[BP_COUNTRY] == null)
					bp[BP_COUNTRY] = "";
				bp[BP_REFNO] = rs.getString(10);
				if (bp[BP_REFNO] == null)
					bp[BP_REFNO] = "";
			}
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		return bp;
	}   //  getBPartnerInfo
	
	public String splitBSB(String bsb) {
		StringBuilder splitBSB = new StringBuilder(bsb);
		splitBSB.insert(2, "-");
		return splitBSB.toString();
	}
}
