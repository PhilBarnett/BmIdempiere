package au.blindmot.model;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.process.DocAction;
import org.compiere.process.DocOptions;
import org.compiere.process.DocumentEngine;

public class MBLDMtomProduction extends X_BLD_mtom_production implements DocAction, DocOptions {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2339407400844279640L;
	private int Morder_id = 0;
	private MOrder mOrder = null;

	
	
	/**************************************************************************
	 *  Default Constructor
	 *  @param ctx context
	 *  @param  BLD_mtom_production_ID    order to load, (0 create new order)
	 *  @param trxName trx name
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
	public MBLDMtomProduction(Properties ctx, ResultSet rs, String trxName, int morderid) {
		super(ctx, rs, trxName);
		//Get info from parent MOrder
		mOrder = new MOrder(ctx, morderid, get_TrxName());
		setC_BPartner_ID(mOrder.getC_BPartner_ID());
		setC_Campaign_ID(mOrder.getC_Campaign_ID());
		setC_Project_ID(mOrder.getC_Project_ID());
		setC_Order_ID(morderid);
		setDescription(mOrder.getDescription());
		
		//Add shell of finished items based on Order line from MOrder
		addProductionItem();
		
	}

	private void addProductionItem() {
		MOrderLine[] orderLines = mOrder.getLines();
		for (int i=0; i < orderLines.length; i++)
		{
			MOrderLine line = orderLines[i];
			//TODO Check if line is a MTM product, if not, skip and log, add to string for display?
			//If it is MTM product, add to bld_mtom_item_line in 'unprocessed' form
			//Iterate through until done
		}
		
		
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
		setC_DocType_ID(getC_DocTypeTarget_ID());
		
		
		return DocAction.STATUS_InProgress;
	}

	@Override
	public boolean approveIt() {

		return true;
	}

	@Override
	public boolean rejectIt() {

		return true;
	}

	@Override
	public String completeIt() {
		setProcessed(true);
		return DocAction.STATUS_Completed;
	}

	@Override
	public boolean voidIt() {

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


}
