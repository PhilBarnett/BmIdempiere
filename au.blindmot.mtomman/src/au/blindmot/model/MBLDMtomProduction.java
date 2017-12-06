package au.blindmot.model;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.webui.component.Messagebox;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.DocOptions;
import org.compiere.process.DocumentEngine;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.Env;
import org.compiere.util.Util;

public class MBLDMtomProduction extends X_BLD_mtom_production implements DocAction, DocOptions {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2339407400844279640L;
	private int mOrder_id = 0;
	private MOrder mOrder = null;
	private MBLDMtomItemLine[] m_lines = null;

	
	
	/**************************************************************************
	 *  Default Constructor
	 *  @param ctx context
	 *  @param  BLD_mtom_production_ID    order to load, (0 create new order)
	 *  @param trxName trx namenewMtm_ID
	 */
	public MBLDMtomProduction(Properties ctx, int BLD_mtom_production_ID, String trxName) {
		super(ctx, BLD_mtom_production_ID, trxName);
	//  New
			if (BLD_mtom_production_ID == 0)
			{
				setDocStatus(DOCSTATUS_Drafted);
				setDocAction (DOCACTION_Prepare);
		
				super.setProcessed(false);
				setProcessing(false);
				setPosted(false);

				setDateAcct (new Timestamp(System.currentTimeMillis()));
				setDatePromised (new Timestamp(System.currentTimeMillis()));
				setC_BPartner_ID (0);
				setIsApproved(false);
				
			}
		}	//	MBLDMtomProduction
	

	public MBLDMtomProduction(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	
	/**************************************************************************
	 *  Normal constructor MOrder constructor
	 *  @param ctx context
	 *  @param  rs ResultSet
	 *  @param trxName trx name
	 *  @param morderid
	 */
	public MBLDMtomProduction(Properties ctx, ResultSet rs, String trxName, int Corder_id) {
		super(ctx, rs, trxName);
		//Get info from parent MOrder 
		mOrder = new MOrder(ctx, Corder_id, get_TrxName());
		setC_BPartner_ID(mOrder.getC_BPartner_ID());
		setC_Campaign_ID(mOrder.getC_Campaign_ID());
		setC_Project_ID(mOrder.getC_Project_ID());
		setC_Order_ID(Corder_id);
		setDescription(mOrder.getDescription());
		setDatePromised(mOrder.getDatePromised());
		setIsComplete(false);
		mOrder_id = Corder_id;
	}


	@Override
	public int customizeValidActions(String docStatus, Object processing, String orderType, String isSOTrx,
			int AD_Table_ID, String[] docAction, String[] options, int index) {
		if (options == null)
			throw new IllegalArgumentException("Option array parameter is null");
		if (docAction == null)
			throw new IllegalArgumentException("Doc action array parameter is null");

		// If a document is drafted or invalid, the users are able to complete, prepare or void
		if (docStatus.equals(DocumentEngine.STATUS_Drafted) || docStatus.equals(DocumentEngine.STATUS_Invalid)) {
			options[index++] = DocumentEngine.ACTION_Complete;
			options[index++] = DocumentEngine.ACTION_Prepare;
			options[index++] = DocumentEngine.ACTION_Void;

			// If the document is already completed, we also want to be able to reactivate or void it instead of only closing it
		} else if (docStatus.equals(DocumentEngine.STATUS_Completed)) {
			options[index++] = DocumentEngine.ACTION_Void;
			options[index++] = DocumentEngine.ACTION_ReActivate;
		}

		return index;
		
	}
	
	
	@Override
	public boolean processIt(String action) throws Exception {
		
		/*TODO: get mtmlineitems, iterate through calling mbldmlineitem.processMtmLineItem()
		 * TODO: Handle freshly created Productions that aren't based on an Order: I.E. if
		 * there's no Order_ID for the production header, Ask user to manually add items.
		 */
		m_lines = getLines();
		for(int i = 0; i < m_lines.length; i++)
		{
			m_lines[i].processMtmLineItem();
		}
		log.warning("Processing Action=" + action + " - DocStatus=" + getDocStatus() + " - DocAction=" + getDocAction());
		DocumentEngine engine = new DocumentEngine(this, getDocStatus());
		return engine.processIt(action, getDocAction());
	}

	@Override
	public boolean unlockIt() {
		
		return true;
	}

	@Override
	public boolean invalidateIt() {
		if (log.isLoggable(Level.INFO)) log.info(toString());
		setDocAction(DOCACTION_Prepare);
		return true;
	}	//	invalidateIt

	@Override
	public String prepareIt() {
		log.warning("---------------In MBLDMtomProduction.prepareIt()");
		/*
		 * /TODO: Handle an Production created by user from the MTM production window,
		 * That is, one that has no Order number or any line items.
		 */
		//Add shell of finished items based on Order line from MOrder
		addProductionItems();
		
		
		setDocStatus(DOCSTATUS_InProgress);
		return DocAction.STATUS_InProgress;
	}

	@Override
	public boolean approveIt() {
		log.warning("---------------In MBLDMtomProduction.approveIt()");
		return true;
	}

	@Override
	public boolean rejectIt() {

		return true;
	}

	@Override
	public String completeIt() {
		log.warning("---------------In MBLDMtomProduction.completeIt()");
		setProcessed(true);
		return DocAction.STATUS_Completed;
	}

	@Override
	public boolean voidIt() {
		log.warning("---------------In MBLDMtomProduction.voidIt()");
		return true;
	}

	@Override
	public boolean closeIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reverseCorrectIt() {

		return true;
	}

	@Override
	public boolean reverseAccrualIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reActivateIt() {

		return true;
	}

	@Override
	public String getSummary() {
		
		return null;
	}

	@Override
	public String getDocumentInfo() {
	
		return null;
	}

	@Override
	public File createPDF() {
		log.warning("---------------In MBLDMtomProduction.createPDF()");
		return null;
	}

	@Override
	public String getProcessMsg() {
		
		return null;
	}

	@Override
	public int getDoc_User_ID() {
		
		return 0;
	}

	@Override
	public BigDecimal getApprovalAmt() {
		return BigDecimal.ONE;
	}

	@Override
	protected boolean beforeSave(boolean newRecord)
	{
		
		int docTypeTarget_id = getC_DocTypeTarget_ID();
		if(docTypeTarget_id < 1)docTypeTarget_id = 1000000;//TODO: Hard coded may have unexpected results, use sql something like
		//SELECT c_doctyoetarget_id from c_doctype WHERE name = 'Made to Measure';
		setC_DocTypeTarget_ID(docTypeTarget_id);

		int docType_id = getC_DocTypeTarget_ID();
		if(docType_id < 1)docType_id = 1000000;//TODO: Hard coded may have unexpected results, use sql something like
		//SELECT c_doctyoe_id from c_doctype WHERE name = 'Made to Measure';
		setC_DocType_ID(docType_id);
		
		Timestamp timeStamp1 = getMovementDate();
		if(timeStamp1 == null)
		{
			setMovementDate(new Timestamp( System.currentTimeMillis() ));
		}
		setDateAcct (new Timestamp( System.currentTimeMillis() ));
		
		if(getC_Currency_ID() == 0)
		{
			MPriceList plDefault = MPriceList.getDefault(getCtx(), true); 
			setC_Currency_ID(plDefault.getC_Currency().getC_Currency_ID());
		}
		
		if(getAD_User_ID() == 0)
			{
				setAD_User_ID(Env.getAD_User_ID(getCtx()));
			}
		
		/*
		 * //TODO prevent save if the MOrder has already been produced, ie check morder_id is unique in
		 * TABLE bld_mtom_production
		 */
		
		/** Prevents saving
		log.saveError("Error", Msg.parseTranslation(getCtx(), "@C_Currency_ID@ = @C_Currency_ID@"));
		log.saveError("FillMandatory", Msg.getElement(getCtx(), "PriceEntered"));
		/** Issues message
		log.saveWarning(AD_Message, message);
		log.saveInfo (AD_Message, message);
		**/
		return true;
	}
	private void addProductionItems()  {
		
		
		int mtom_production_ID = getbld_mtom_production_ID();
		if ( mtom_production_ID == 0)//The record hasn't been saved yet
		{
			log.severe("Can't add prodcution lines to null record. mtom_production_ID == " + mtom_production_ID);
			throw new AdempiereUserError("Can't add prodcution lines to null record. mtom_production_ID == " + mtom_production_ID, "Have you saved the header record?");
			
		}
		/*Currently able to be added twice.
		 * TODO: Check for duplicates or alert user.
		 */
		if(mOrder == null)
		{
			throw new AdempiereUserError("There's no Order to get production items from.", "You should either manually add production items or delete this Production and create it from an existing order");
		}
		MOrderLine[] orderLines = mOrder.getLines();
		MBLDMtomItemLine mtmLine = null;
		int mtmProdID = this.getbld_mtom_production_ID();
		for (int i=0; i < orderLines.length; i++)
		{
			MOrderLine line = orderLines[i];
			MProduct mProduct = new MProduct(getCtx(), line.getM_Product_ID(),get_TrxName());
			if(mProduct.get_ValueAsBoolean("ismadetomeasure"))
			{
				if (log.isLoggable(Level.FINE)||log.isLoggable(Level.FINER)) log.info("Adding:"+line.toString()+" to production items for production " + mtmProdID);
				mtmLine = new MBLDMtomItemLine(line, mtmProdID);
				if(!mtmLine.save())
					{
						log.severe("Can't add prodcution line:" +  mtmProdID + " to MTM production, mtom_production_ID == " + mtom_production_ID);
						
					}
				else 
					{
						mtmLine.set_Barcode();
						mtmLine.saveEx();
					}
			}
		}

	}
	
	public MBLDMtomItemLine[] getLines (boolean requery, String orderBy)
	{
		if (m_lines != null && !requery) {
			set_TrxName(m_lines, get_TrxName());
			return m_lines;
		}
		//
		String orderClause = "";
		if (orderBy != null && orderBy.length() > 0)orderClause += orderBy;
		
		m_lines = getLines(null, orderClause);
		return m_lines;
	}	//	getLines
	
	public MBLDMtomItemLine[] getLines (String whereClause, String orderClause)
	{
		
		//This method Copied from MOrder
		//Note: The explodeBOM() in MOrder creates a sql string to pass to MOrder.getLines(String, String)
		//red1 - using new Query class from Teo / Victor's MDDOrder.java implementation
		StringBuilder whereClauseFinal = new StringBuilder(MBLDMtomItemLine.COLUMNNAME_C_OrderLine_ID+"=? ");
		if (!Util.isEmpty(whereClause, true))
			whereClauseFinal.append(whereClause);
		if (orderClause.length() == 0)
			orderClause = MBLDMtomItemLine.COLUMNNAME_C_OrderLine_ID;
		//
		List<MBLDMtomItemLine> list = new Query(getCtx(), I_BLD_mtom_item_line.Table_Name, whereClauseFinal.toString(), get_TrxName())
										.setParameters(get_ID())
										.setOrderBy(orderClause)
										.list();
		//
		return list.toArray(new MBLDMtomItemLine[list.size()]);		
	}	//	getLines
	
	public MBLDMtomItemLine[] getLines() {
		return getLines(false, null);
	}
}
