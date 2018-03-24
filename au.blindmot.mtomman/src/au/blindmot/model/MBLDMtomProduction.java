package au.blindmot.model;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.exceptions.PeriodClosedException;
import org.compiere.acct.Doc;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPeriod;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.MProductionLine;
import org.compiere.model.MProductionLineMA;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.DocOptions;
import org.compiere.process.DocumentEngine;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;
//import org.compiere.util.Trx;
//import org.compiere.util.TrxRunnable;
import org.compiere.util.Util;

public class MBLDMtomProduction extends X_BLD_mtom_production implements DocAction, DocOptions {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2339407400844279640L;
	private int mOrder_id = 0;
	private MOrder mOrder = null;
	private MBLDMtomItemLine[] m_lines = null;
	/**	Process Message 			*/
	private String		m_processMsg = null;
	/**	Just Prepared Flag			*/
	private boolean		m_justPrepared = false;

	
	
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
		setAD_Org_ID(mOrder.getAD_Org_ID());
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

			// If the document is already completed, we also want to be able to void it instead of only closing it
		} else if (docStatus.equals(DocumentEngine.STATUS_Completed)) {
			options[index++] = DocumentEngine.ACTION_Void;
		} else if (docStatus.equals(DocumentEngine.STATUS_InProgress)) {
			options[index++] = DocumentEngine.ACTION_Approve;
		}

		return index;
		
	}
	
	
	@Override
	public boolean processIt(String action) throws Exception {
		
		
		log.warning("---------------Processing Action= " + action + " - DocStatus= " + getDocStatus() + " - DocAction= " + getDocAction());
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
		
		//Add shell of finished items based on Order line from MOrder
		MBLDMtomItemLine[] lines = getLines();
		System.out.println(getDocStatus());
		if(!getDocStatus().equals(DOCSTATUS_InProgress)||getDocStatus().equals(DOCSTATUS_Drafted))
		{
			if(lines.length == 0)//Check to ensure lines don't get added twice.
			{
				addProductionItems();
				m_justPrepared = true;
			}
		}
		
		if(lines.length != 0)//Add BOM derived, Cuts & production lines if not already added.
		{
			
			
		
			for(int i = 0; i < lines.length; i++)
			{
				MBLDMtomCuts[] cuts = lines[i].getCutLines(p_ctx, lines[i].get_ID());
				MProductionLine[] prodLines = lines[i].getLines(p_ctx, lines[i].get_ID());
				MBLDBomDerived[] bomLines = lines[i].getBomDerivedLines(p_ctx, lines[i].get_ID());
				
				//Add new lines only if there are no child records
				if(cuts.length==0 && prodLines.length==0 && bomLines.length==0)
				{
					lines[i].processMtmLineItem(getDocAction());
					lines[i].saveEx();
				}
			}
		}
		
		System.out.println(getDocStatus());
		
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
		
		// Re-Check
				if(DOCSTATUS_Voided.equals(getDocStatus()))voidIt();
						if (!m_justPrepared)
						{
							String status = prepareIt();
							m_justPrepared = false;
							if (!DocAction.STATUS_InProgress.equals(status))
								return status;
						}

						m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
						if (m_processMsg != null)
							return DocAction.STATUS_Invalid;

						StringBuilder errors = new StringBuilder();
						
						
						/*TODO:
						 * deal with errors etc.
						 * This is likely to be time consuming to debug.
						 * Also check that the deletion of BOMDerived works.
						 */
						MBLDMtomItemLine[] mBldItemLines = getLines();
						if(mBldItemLines.length == 0)
						{
							m_processMsg = "@NoLines@";
							return DocAction.STATUS_Invalid;
						}
						for(int i = 0; i < mBldItemLines.length; i++)
						{
							errors.append(mBldItemLines[i].processProductionLines(mBldItemLines[i].getbld_mtom_item_line_ID()));
							if (errors.length() > 0) 
							{
							m_processMsg = errors.toString();
							return DocAction.STATUS_Invalid;
							}
						}
						
						for(int j = 0; j < mBldItemLines.length; j++)
						{
							mBldItemLines[j].setIsComplete(true);
							mBldItemLines[j].saveEx();
						}
		
		setProcessed(true);
		return DocAction.STATUS_Completed;
	}

	@Override
	public boolean voidIt() {
		log.warning("---------------In MBLDMtomProduction.voidIt()");
		if (log.isLoggable(Level.INFO)) log.info(toString());
		// Before Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_VOID);
		if (m_processMsg != null)
			return false;

		if (DOCSTATUS_Closed.equals(getDocStatus())
				|| DOCSTATUS_Reversed.equals(getDocStatus())
				|| DOCSTATUS_Voided.equals(getDocStatus()))
		{
			m_processMsg = "Document Closed: " + getDocStatus();
			setDocAction(DOCACTION_None);
			return false;
		}

		// Not Processed
		if (DOCSTATUS_Drafted.equals(getDocStatus())
				|| DOCSTATUS_Invalid.equals(getDocStatus())
				|| DOCSTATUS_InProgress.equals(getDocStatus())
				|| DOCSTATUS_Approved.equals(getDocStatus())
				|| DOCSTATUS_NotApproved.equals(getDocStatus()) )
		{
			setIsCreated("N");
			
			MBLDMtomItemLine[] lines = getLines();
			for(int f = 0; f<lines.length; f++)
			{
				lines[f].deleteProductionLines();
				lines[f].deleteProcessedMtmLineItem();//calls deleteBomDerived() & deleteCuts()
				lines[f].saveEx();
				
			}
		}
		else
		{
			boolean accrual = false;
			try 
			{
				MPeriod.testPeriodOpen(getCtx(), getMovementDate(), Doc.DOCTYPE_MatProduction, getAD_Org_ID());
			}
			catch (PeriodClosedException e) 
			{
				accrual = true;
			}

			if (accrual)
				return reverseAccrualIt();
			else
				return reverseCorrectIt();
		}

		// After Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_VOID);
		if (m_processMsg != null)
			return false;

		setProcessed(true);
		setDocAction(DOCACTION_None);
		return true; 
	}

	@Override
	public boolean closeIt() {
		log.warning("---------------In MBLDMtomProduction.closeIt()");
		if (log.isLoggable(Level.INFO)) log.info(toString());
		// Before Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_CLOSE);
		if (m_processMsg != null)
			return false;

		setProcessed(true);
		setDocAction(DOCACTION_None);

		// After Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_CLOSE);
		if (m_processMsg != null)
			return false;
		return true;
	}

	@Override
	public boolean reverseCorrectIt() {

		if (log.isLoggable(Level.INFO)) log.info(toString());
		// Before reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		MBLDMtomProduction reversal = null;
		try {
			reversal = reverse(false);
		} catch (Exception e) {
			throw new AdempiereException("Failed to reverseCorectIt()");
		}
		if (reversal == null)
			return false;

		// After reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		m_processMsg = reversal.getDocumentNo();

		return true;
	}

	private MBLDMtomProduction reverse(boolean accrual) throws Exception {
		Timestamp reversalDate = accrual ? Env.getContextAsDate(getCtx(), "#Date") : getMovementDate();
		if (reversalDate == null) {
			reversalDate = new Timestamp(System.currentTimeMillis());
		}

		MPeriod.testPeriodOpen(getCtx(), reversalDate, Doc.DOCTYPE_MatProduction, getAD_Org_ID());
		MBLDMtomProduction reversal = null;
		reversal = copyFrom (reversalDate);

		StringBuilder msgadd = new StringBuilder("{->").append(getDocumentNo()).append(")");
		reversal.addDescription(msgadd.toString());
		reversal.setReversal_ID(getbld_mtom_production_ID ());
		reversal.saveEx(get_TrxName());
		
		// Reverse Line Qty
		MBLDMtomItemLine[] mBLDMtomItemLines = getLines();//TODO: Check if the 2 arrays are the same length
		for(int j = 0; j < mBLDMtomItemLines.length; j++)
		{
			MProductionLine[] sLines = mBLDMtomItemLines[j].getLines(getCtx(), mBLDMtomItemLines[j].getbld_mtom_item_line_ID());
			/*
			 * TODO: Copy the production lines
			 */
			
			MProductionLine[] tLines = getToLines(mBLDMtomItemLines [j], reversal.getbld_mtom_production_ID());
			for (int i = 0; i < sLines.length; i++)
			{		
				//	We need to copy MAcopyFrom 
				if (sLines[i].getM_AttributeSetInstance_ID() == 0)
				{
					MProductionLineMA mas[] = MProductionLineMA.get(getCtx(), sLines[i].get_ID(), get_TrxName());
					for (int x = 0; x < mas.length; x++)
					{
						MProductionLineMA ma = new MProductionLineMA (tLines[i],
							mas[x].getM_AttributeSetInstance_ID(),
							mas[x].getMovementQty().negate(),mas[x].getDateMaterialPolicy());
						ma.saveEx(get_TrxName());					
					}
				}
			}
		}
		
		
		if (!reversal.processIt(DocAction.ACTION_Complete))
		{
			m_processMsg = "Reversal ERROR: " + reversal.getProcessMsg();
			return null;
		}

		reversal.closeIt();
		reversal.setProcessing (false);
		reversal.setDocStatus(DOCSTATUS_Reversed);
		reversal.setDocAction(DOCACTION_None);
		reversal.saveEx(get_TrxName());

		msgadd = new StringBuilder("(").append(reversal.getDocumentNo()).append("<-)");
		addDescription(msgadd.toString());

		setProcessed(true);
		setReversal_ID(reversal.getbld_mtom_production_ID ());
		setDocStatus(DOCSTATUS_Reversed);	//	may come from void
		setDocAction(DOCACTION_None);		

		return reversal;
	}
	private MBLDMtomProduction copyFrom(Timestamp reversalDate) {
		MBLDMtomProduction to = new MBLDMtomProduction(getCtx(), 0, get_TrxName());
		PO.copyValues (this, to, getAD_Client_ID(), getAD_Org_ID());

		to.set_ValueNoCheck ("DocumentNo", null);
		//
		to.setDocStatus (DOCSTATUS_Drafted);		//	Draft
		to.setDocAction(DOCACTION_Complete);
		to.setMovementDate(reversalDate);
		to.setIsComplete(false);
		to.setIsCreated("Y");
		to.setProcessing(false);
		to.setProcessed(false);
		
		/*****************************************
		if (isUseProductionPlan()) {
			to.saveEx();
			Query planQuery = new Query(Env.getCtx(), I_M_ProductionPlan.Table_Name, "M_ProductionPlan.M_Production_ID=?", get_TrxName());
			List<MProductionPlan> fplans = planQuery.setParameters(getM_Production_ID()).list();
			for(MProductionPlan fplan : fplans) {
				MProductionPlan tplan = new MProductionPlan(getCtx(), 0, get_TrxName());
				PO.copyValues (fplan, tplan, getAD_Client_ID(), getAD_Org_ID());
				tplan.setM_Production_ID(to.getM_Production_ID());
				tplan.setProductionQty(fplan.getProductionQty().negate());
				tplan.setProcessed(false);
				tplan.saveEx();

				MProductionLine[] flines = fplan.getLines();
				for(MProductionLine fline : flines) {
					MProductionLine tline = new MProductionLine(tplan);
					PO.copyValues (fline, tline, getAD_Client_ID(), getAD_Org_ID());
					tline.setM_ProductionPlan_ID(tplan.getM_ProductionPlan_ID());
					tline.setMovementQty(fline.getMovementQty().negate());
					tline.setPlannedQty(fline.getPlannedQty().negate());
					tline.setQtyUsed(fline.getQtyUsed().negate());
					tline.saveEx();
				}
			}
			****************************************/
			//to.setProductionQty(getProductionQty().negate());	
			to.saveEx();
		return to;
	}

	@Override
	public boolean reverseAccrualIt() {
		log.warning("---------------In MBLDMtomProduction.reverseAccrualIt()");
		if (log.isLoggable(Level.INFO)) log.info(toString());
		// Before reverseAccrual
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSEACCRUAL);
		if (m_processMsg != null)
			return false;

		MBLDMtomProduction reversal = null;
		try {
			reversal = reverse(true);
		} catch (Exception e) {
			throw new AdempiereException("Failed to reverse - called by reverseAccrualIt()");
		}
		if (reversal == null)
			return false;

		// After reverseAccrual
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSEACCRUAL);
		if (m_processMsg != null)
			return false;

		m_processMsg = reversal.getDocumentNo();

		return true;
	}

	@Override
	public boolean reActivateIt() {
		if (log.isLoggable(Level.INFO)) log.info("reActivateIt - " + toString());
		// Before reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REACTIVATE);
		if (m_processMsg != null)
			return false;

		// After reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REACTIVATE);
		if (m_processMsg != null)
			return false;
		return false;
	}

	@Override
	public String getSummary() {
		return getDocumentNo();
	}

	@Override
	public String getDocumentInfo() {
		return getDocumentNo();
	}

	@Override
	public File createPDF() {
		log.warning("---------------In MBLDMtomProduction.createPDF()");
		return null;
	}

	@Override
	public String getProcessMsg() {
		return m_processMsg;
	}

	@Override
	public int getDoc_User_ID() {
		return getCreatedBy();
	}

	@Override
	public BigDecimal getApprovalAmt() {
		return BigDecimal.ONE;
	}

	@Override
	protected boolean beforeSave(boolean newRecord)
	{
		
		StringBuilder sql = new StringBuilder("SELECT c_doctype_id ");
		sql.append("FROM c_doctype ");
		sql.append("WHERE name = 'Made to Measure'");
		int docId = DB.getSQLValue(get_TrxName(), sql.toString());
		
		int docTypeTarget_id = getC_DocTypeTarget_ID();
		if(docTypeTarget_id < 1)docTypeTarget_id = docId;
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
		
		mOrder = new MOrder(p_ctx, getC_Order_ID(), get_TrxName());
		if(mOrder == null)
		{
			throw new AdempiereUserError("There's no Order to get production items from.", "You should either manually add production items or delete this Production and create it from an existing order");
		}
		final MOrderLine[] orderLines = mOrder.getLines();
		//final MBLDMtomItemLine mtmLine = null;
		final int mtmProdID = this.getbld_mtom_production_ID();
		
		
		for (int i=0; i < orderLines.length; i++)
		{
			final MOrderLine theOrderLine = orderLines[i];
			//MOrderLine line = orderLines[i];
			MProduct mProduct = new MProduct(getCtx(), orderLines[i].getM_Product_ID(),get_TrxName());
			if(mProduct.get_ValueAsBoolean("ismadetomeasure"))
			{
				if (log.isLoggable(Level.FINE)||log.isLoggable(Level.FINER)) log.info("Adding:"+orderLines[i].toString()+" to production items for production " + mtmProdID);
				final String trxn = get_TrxName();
				System.out.println("trxn: " + trxn + " get_TrxName(): " + get_TrxName());
				//MBLDMtomItemLine mtmLine = orderLines[i];
				
			    /*{
			        Trx.run(new TrxRunnable() {
			            public void run(String trxName) {
			            	MBLDMtomItemLine Line = new MBLDMtomItemLine(getCtx(), 0, mtmProdID, trxn);
							Line.setFromOrderLine(theOrderLine);
							Line.saveEx();
			                
			            }
			        });
			    }*/
				
				MBLDMtomItemLine Line = new MBLDMtomItemLine(getCtx(), 0, mtmProdID, trxn);
				Line.setFromOrderLine(theOrderLine);
				Line.saveEx();
				
				Line.set_Barcode();
				Line.saveEx();
	
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
		StringBuilder whereClauseFinal = new StringBuilder(MBLDMtomItemLine.COLUMNNAME_bld_mtom_production_ID+"=? ");
		if (!Util.isEmpty(whereClause, true))
			whereClauseFinal.append(whereClause);
		if (orderClause.length() == 0)
			orderClause = MBLDMtomItemLine.COLUMNNAME_bld_mtom_item_line_ID;
		//
		System.out.print(get_ID());
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
	
	public void addDescription (String description)
	{
		String desc = getDescription();
		if (desc == null)
			setDescription(description);
		else{
			StringBuilder msgd = new StringBuilder(desc).append(" | ").append(description);
			setDescription(msgd.toString());
		}
	}	//	addDescription
	
	private MBLDMtomItemLine copyFrom(MBLDMtomItemLine from) {
		MBLDMtomItemLine to = new MBLDMtomItemLine(getCtx(), 0, get_TrxName());
		PO.copyValues (from, to, getAD_Client_ID(), getAD_Org_ID());
		to.setName(from.getName());
		to.setattributesetinstance_id(from.getattributesetinstance_id());
		to.setDescription(from.getDescription());
		if(from.getM_Locator_ID()!=0)to.setM_Locator_ID(from.getM_Locator_ID());
		to.setM_Product_ID(from.getM_Product_ID());
		to.setbld_mtom_production_ID(from.getbld_mtom_production_ID());
		to.setinstance_string(from.getinstance_string());
		System.out.println("from attrributeSetInstance: " + from.getattributesetinstance());
		to.saveEx();
		System.out.println("to attrributeSetInstance: " + to.getattributesetinstance());
		return to;
	}
	
	private MProductionLine[] getToLines(MBLDMtomItemLine mBLDMtomItemLineFrom, int bld_mtom_production_Id_to) {
	/*
	 * Create new mBLDMtomItemLine
	 * add the negated 'tlines' to copy
	 */
	
	
		MBLDMtomItemLine theLine = copyFrom(mBLDMtomItemLineFrom);
		theLine.setbld_mtom_production_ID(bld_mtom_production_Id_to);
		theLine.setAD_Org_ID(mBLDMtomItemLineFrom.getAD_Org_ID());
		theLine.saveEx();
		
	MProductionLine[] flines = mBLDMtomItemLineFrom.getLines(p_ctx, mBLDMtomItemLineFrom.getbld_mtom_item_line_ID());
	ArrayList<MProductionLine>	retlines = new ArrayList<MProductionLine>();
		for(MProductionLine fline : flines) {
			MProductionLine tline = new MProductionLine(getCtx(), 0, get_TrxName());
			PO.copyValues (fline, tline, getAD_Client_ID(), getAD_Org_ID());
			System.out.println("theLine.getbld_mtom_item_line_ID(): " + theLine.getbld_mtom_item_line_ID());
			System.out.println("mBLDMtomItemLineFrom id: " + mBLDMtomItemLineFrom.getbld_mtom_item_line_ID());
			tline.set_ValueOfColumn("bld_mtom_item_line_id", theLine.getbld_mtom_item_line_ID());
			tline.setMovementQty(fline.getMovementQty().negate());
			tline.setPlannedQty(fline.getPlannedQty().negate());
			tline.setQtyUsed(fline.getQtyUsed().negate());
			if(tline.getAD_Org_ID()==0)tline.setAD_Org_ID(mBLDMtomItemLineFrom.getAD_Org_ID());
			tline.saveEx();
			retlines.add(tline);
			System.out.println(" tline: " + tline.toString() + " tlineID: " + tline.getM_ProductionLine_ID() +
					" mtmitomlineid: " + tline.get_ValueAsInt("bld_mtom_item_line_id"));
			}
		MProductionLine[] linesToRet = new MProductionLine[retlines.size()];
		retlines.toArray(linesToRet);
		return linesToRet;
	}

}
