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
import org.compiere.model.MClient;
import org.compiere.model.MCurrency;
import org.compiere.model.MOrg;
import org.compiere.model.MPaySelectionCheck;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.PaymentExport;

public class ABAPaymentExport implements PaymentExport {
	
	/** Logger								*/
	static private CLogger	s_log = CLogger.getCLogger (ABAPaymentExport.class);
	
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
55	Allotment
56	Dividend
57	Debenture/Note Intereststring char
	 * 
	 * 
	 */
	
	public int exportToFile (MPaySelectionCheck[] checks, File file, StringBuffer err) {
		return exportToFile (checks, false, (String)null, file, err);
		
		//Get client name, bank code, transaction date.
		
		
			int xx=0;
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
				Split bsb for org
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
					String PaymentInfoId="Payments";
				BigDecimal CtrlSum = BigDecimal.valueOf(6554.23);
				String ExecutionDate = "2011-12-31";
				String Dbtr_Name = mClient.getName();
				String DbtrAcct_IBAN = "IBAN_IBAN";
				String DbtrAcct_BIC = "BICBIC";
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
				
				I_C_BankAccount cBankAccount =paySelection.getC_BankAccount();
				DbtrAcct_IBAN = cBankAccount.getIBAN();
				DbtrAcct_BIC = cBankAccount.getC_Bank().getRoutingNo();
				
				msg.append(getHeader(ExecutionDate));
				
				msg.append(iSEPA_Document_open());
				msg.append(iSEPA_CstmrCdtTrfInitn_open());
					msg.append(iSEPA_GrpHdr_open());
						msg.append(iSEPA_MsgId(iSEPA_ConvertSign(MsgId)));
						msg.append(iSEPA_CreDtTm(iSEPA_ConvertSign(CreationDate)));
						msg.append(iSEPA_NbOfTxs(NumberOfTransactions));
						msg.append(iSEPA_InitgPty(iSEPA_ConvertSign(InitiatorName)));
					msg.append(iSEPA_GrpHdr_close());
					msg.append(iSEPA_PmtInf_open());
						msg.append(iSEPA_PmtInfId(iSEPA_ConvertSign(PaymentInfoId)));
						msg.append(iSEPA_PmtMtd());
						msg.append(iSEPA_BtchBookg());
						msg.append(iSEPA_NbOfTxs(NumberOfTransactions));
						msg.append(iSEPA_CtrlSum(CtrlSum));
						msg.append(iSEPA_PmtTpInf());
						msg.append(iSEPA_ReqdExctnDt(iSEPA_ConvertSign(ExecutionDate)));
						msg.append(iSEPA_DbtrNm(iSEPA_ConvertSign(Dbtr_Name)));
						msg.append(iSEPA_DbtrAcctIBAN(iSEPA_ConvertSign(DbtrAcct_IBAN)));
						msg.append(iSEPA_DbtrAgtFinInstnIdBIC(iSEPA_ConvertSign(DbtrAcct_BIC)));
						msg.append(iSEPA_ChrgBr());
						
						for (int i = 0; i < checks.length; i++)
						{
							MPaySelectionCheck mPaySelectionCheck = checks[i];

							if (mPaySelectionCheck == null)
								continue;
							mPaySelectionCheck.getC_BPartner_ID();
							mPaySelectionCheck.getDocumentNo();
							mPaySelectionCheck.isProcessed();
							String PmtId=String.valueOf(mClient.getAD_Client_ID())+"-"+String.valueOf(mPaySelectionCheck.get_ID());
							//"OriginatorID1235";
							String Ccy="EUR";
							BigDecimal Amount=BigDecimal.ZERO;
							String AmountAsString ="";
							AmountAsString = String.valueOf(Amount);
							String CreditorName="aaaaaaaaa";
							String CdtrAcct_BIC="cdtr bic";
							String CdtrAcct_IBAN="cdtr iban";
							
							String bp[] = getBPartnerInfo(mPaySelectionCheck.getC_BPartner_ID());
							CreditorName = bp[BP_NAME];
							String ba[] = getBPartnerAccountInfo(mPaySelectionCheck.getC_BPartner_ID());
							CdtrAcct_IBAN = ba[0];
							CdtrAcct_BIC = ba[1];
							
							//trx iteration
							msg=msg.append(iSEPA_CdtTrfTxInf_open());
								msg=msg.append(iSEPA_PmtId(iSEPA_ConvertSign(PmtId)));
								msg=msg.append(iSEPA_AmtInstdAmt(MCurrency.getISO_Code(Env.getCtx(), mPaySelectionCheck.getParent().getC_Currency_ID()),String.valueOf(mPaySelectionCheck.getPayAmt())));
								msg=msg.append(iSEPA_CdtrAgtFinInstnIdBIC(iSEPA_ConvertSign(CdtrAcct_BIC)));
								msg=msg.append(iSEPA_CdtrNm(iSEPA_ConvertSign(CreditorName)));
								msg=msg.append(iSEPA_CdtrAcctIBAN(iSEPA_ConvertSign(CdtrAcct_IBAN)));
							msg=msg.append(iSEPA_CdtTrfTxInf_close());
						}
						
						
						msg=msg.append(iSEPA_PmtInf_close());
					msg=msg.append(iSEPA_CstmrCdtTrfInitn_close());
				msg=msg.append(iSEPA_Document_close());
				
				// convert everthing to utf-8
				//String msg_utf8 = new String(msg.toString().getBytes("UTF-8"), "ISO-8859-1");
				//fw.write(iSEPA_ConvertSign(msg.toString()));
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

	public String getHeader(String bankAccName, String exDate) {
		StringBuilder header = new StringBuilder();
		header.setLength(120);
		header.insert(0, "0");
		
		//blank fill 2-18
		for(int i = 1; i < 18; i++)
		{
			header.insert(i, " ");
		}
		
		//set reel sequence no.
		header.insert(18, "01");
		
		//TODO: create DB column for bank code
		header.insert(18, "01");
		
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
		for(int i = 80; i < 120; i++)
		{
			header.insert(i, " ");
		}
		header.insert(120, "\n");
		
		return header.toString();
		
		
	}


	public String getFilenameSuffix() {
		return ".aba";
	}
	
	/**
	 *  Get Vendor/Customer Bank Account Information
	 *  Based on BP_ static variables
	 *  @param C_BPartner_ID BPartner
	 *  @return info array
	 */
	private static String[] getBPartnerAccountInfo (int C_BPartner_ID)
	{
		String[] bankAccount = new String[2];
/*** as1 changed to simplify account management
		String sql = "select ba.accountno,b.routingno "
					+"from c_bp_bankaccount ba,c_bank b "
					+"where ba.c_bpartner_id=? "
					+"and ba.c_bank_id = b.c_bank_id " 
					;
****/	
		String sql = "select ba.accountno,ba.routingno "
				+"from c_bp_bankaccount ba "
				+"where ba.c_bpartner_id=? "
				;
		
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
				bankAccount[0] = rs.getString(1);
				if (bankAccount[0] == null)
					bankAccount[0] = "";
				bankAccount[1] = rs.getString(2);
				if (bankAccount[1] == null)
					bankAccount[1] = "";
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
}
