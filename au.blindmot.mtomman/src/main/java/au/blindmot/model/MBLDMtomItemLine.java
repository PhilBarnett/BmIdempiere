package au.blindmot.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MClient;
import org.compiere.model.MLocator;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductCategory;
import org.compiere.model.MProductionLine;
import org.compiere.model.MQualityTest;
import org.compiere.model.MStorageOnHand;
import org.compiere.model.Query;
import org.compiere.process.DocumentEngine;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;

import au.blindmot.factories.BLDMtomMakeFactory;
import au.blindmot.make.MadeToMeasureProduct;

public class MBLDMtomItemLine extends X_BLD_mtom_item_line {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6986611760223696973L;
	private int lineno;
	private int count;
	@SuppressWarnings("unused")
	private int mProductId = 0;
	private int m_Locator_ID = 0;
	
	
	// Prefixes for bar codes based on table_id - to shorten barcodes
		public static final String MTM_PRODUCTION_PREFIX = "01";
		public static final String MTM_PRODUCTION_ITEM_PREFIX = "02";
		public static final String MTM_PRODUCTION_ASSEMBLEY_ITEM = "03";
		
		
	public MBLDMtomItemLine(Properties ctx, MOrderLine orderLine, int mTM_prod_ID, String trxName) {
			this(ctx, 0, trxName);
			setFromOrderLine(orderLine);		
	}
	
	/**
	 * 
	 */

	public MBLDMtomItemLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	/**
	 * The standard constructor call
	 */
	//MBLDMtomItemLine(getCtx(), 0, mtmProdID, trxn)
	public MBLDMtomItemLine(Properties ctx, int mblditomLineID, int mTM_prod_ID, String trxName) {
		super(ctx, mblditomLineID, trxName);
		setbld_mtom_production_ID(mTM_prod_ID);
		/*
		 * Once the production lines are in, what do we do with them? 
		 * TODO: Create table bld_mtom_cuts, add the table to 'Table and Column', generate I & X classes, no M class.
		 * 
		 * 'Interpret' from AttributeSetInstance the components required, may need some sort of 
		 *  AttributeSetInstance to components/cuts table for settings - eg what components (products) are required 
		 *   for a link blind? a drop down awning with motor?
		 *   Deductions are a product attribute.
		 *   As attributes are going to be the way to propagate info, there will have to be protocol 
		 *   that determines their use, possibly a verification process?
		 *  Develop business logic around 'how things are made' with deductions, normal roll/reverse roll etc.
		 *  	Think about how blind calculations are made to manually make a blind to get ideas.
		 *  	Consider a 'BLDMtomBLindFactory' class that returns a class that implements I_BLD_mtom_Blinds interface
		 *  	with methods like:
		 *  		getSkinSize(int width, int drop)	
		 *  		getCutLengths(int product_ID)
		 *  	Classes to returTODO Auto-generated constructor stubs example: RollerMaker, CommercialRollerMaker, AcmedaWireAwningMaker etc		
		 *  
		 *  Calculate BOM_derived quantities for invent adjustment, look at 'productionline' for code guidelines.
		 *  /private static final long serialVersionUID = -3382564616366609627L;
		 */
		
	}
	
	public void setFromOrderLine(MOrderLine orderLine) {
		int m_Prod_ID = orderLine.getM_Product_ID();
		setDescription(orderLine.getDescription());
		setM_Product_ID(m_Prod_ID);
		mProductId = m_Prod_ID;
		setAD_Org_ID(orderLine.getAD_Org_ID());
		setName(orderLine.getName());
		setC_OrderLine_ID(orderLine.getC_OrderLine_ID());
		setattributesetinstance_id(orderLine.getM_AttributeSetInstance_ID());
		setLine(orderLine.getLine());
		setBld_Line_ProductSetInstance_ID(orderLine.get_ValueAsInt("bld_line_productsetinstance_id"));
		if(orderLine.get_Value("mtm_attribute")!=null)
		{
			setinstance_string(orderLine.get_Value("mtm_attribute").toString());
		}
		else
		{
			setinstance_string("0_0_0");
		}
	}

	/**
	 * 
	 */
	public MBLDMtomItemLine(Properties ctx, int BLD_mtom_item_line_ID, String trxName) {
		super(ctx, BLD_mtom_item_line_ID, trxName);
		// TODO Auto-generated constructor stub
	
	}
	
	public void set_Barcode(){
		log.warning("---------------In MBLDMtomItemLine.set_Barcode()");
		//MtmUtils utils = new MtmUtils(); //Causes massive slowdown
		setbarcode(getBarcode(get_Table_ID(), getbld_mtom_item_line_ID()));
	}
	
	public boolean deleteProcessedMtmLineItem() {
		MadeToMeasureProduct mTmProduct = BLDMtomMakeFactory.getMtmProduct(getM_Product_ID(), getbld_mtom_item_line_ID (), get_TrxName() );
		mTmProduct.deleteBomDerived();
		mTmProduct.deleteCuts(); 
		
		log.warning("---------------In deleteProcessedMtmLineItem()");
		
		return false;
		/*TODO: implement a reversal of processMtmLineItem()
		 * May need to add abstract methods to MadeToMeasureProduct
		 * deleteBomDerived(), delteCuts(), deleteProductionLine()
		 */
		
	}
	
	public boolean addBomDerived() {
		MadeToMeasureProduct mTmProduct = BLDMtomMakeFactory.getMtmProduct(getM_Product_ID(), getbld_mtom_item_line_ID(), get_TrxName());
		if(mTmProduct.createBomDerived() && mTmProduct.setAutoSelectedPartIds() && mTmProduct.processTriggers(this)) return true;
		//TODO: Call addToBomDerived() here.
		return false;
	}
	
	public boolean processMtmLineItem(String docAction) {
		String action = docAction;
		log.warning("---------------In processMtmLineItem");
		MadeToMeasureProduct mTmProduct = BLDMtomMakeFactory.getMtmProduct(getM_Product_ID(), getbld_mtom_item_line_ID(), get_TrxName());
		
			/*
			 * If it has already been processed, delete children and start again.
			 * This if statement may be able to be deleted.
			 */
		if(isComplete() && isprocessed())
			{
				mTmProduct.deleteBomDerived();
				mTmProduct.deleteCuts(); 
				deleteProductionLines();
				setIsComplete(false);
				setisprocessed(false);
			}
	System.out.println("Complete: "+this.isComplete() + " Processed: "+ this.isprocessed() + "Action passed: " + action + " is it: " + DocumentEngine.ACTION_Void);
		if(!isprocessed())//If it is already processed or it's being voided, skip processing
		{
			if((action.equalsIgnoreCase(DocumentEngine.ACTION_Void)))return false;
			
		/*mTmProduct.updateBomQty() added 1/9/2021.
		 * Done because some BOM items rely on the existence of other BOM items to get their quantities; previous
		 * logic path attempted to find BOM items that weren't added at the time of execution.
		 */
		if(mTmProduct.updateBomQty())
		{
			if(mTmProduct.getCuts())
			{
				/*{*/
					if(createLines(false)>0)
					/*{
						MProduct mProduct = new MProduct(p_ctx, getM_Product_ID(), get_TrxName());
						if(createLines(false, mProduct, getProductionQty()) > 0)
						setisprocessed(true);	
					}*/
				/*}*/
return true;
			}
		}
	}
return false;
	}
	/**
	 * 
	 * @param ctx
	 * @param mtmLineItemId
	 * @return
	 */
	public MBLDMtomCuts[] getCutLines (Properties ctx, int mtmLineItemId)
	{
		 {
			 	final String whereClause = MBLDMtomCuts.COLUMNNAME_bld_mtom_item_line_ID+"=?"; 
			 	List<MBLDMtomCuts> list = new Query(ctx, MBLDMtomCuts.Table_Name, whereClause, get_TrxName())
			 		.setParameters(new Object[]{mtmLineItemId})
			 		.setOrderBy(MBLDMtomCuts.COLUMNNAME_BLD_mtom_cuts_ID)
			 		.list();
			 	return list.toArray(new MBLDMtomCuts[list.size()]);
			 }
	}
	
	/**
	 * 
	 * @param ctx
	 * @param mtmLineItemId
	 * @return
	 */
	
	public MBLDBomDerived[] getBomDerivedLines (Properties ctx, int mtmLineItemId)
	{
		 {
			 	final String whereClause = MBLDBomDerived.COLUMNNAME_bld_mtom_item_line_ID+"=?"; 
			 	List<MBLDBomDerived> list = new Query(ctx, MBLDBomDerived.Table_Name, whereClause, get_TrxName())
			 		.setParameters(new Object[]{mtmLineItemId})
			 		.setOrderBy(MBLDBomDerived.COLUMNNAME_bld_mtom_bomderived_ID)
			 		.list();
			 	return list.toArray(new MBLDBomDerived[list.size()]);
			 }
	}

	/*
	 * createLines(boolean mustBeStocked)  and createLines(boolean mustBeStocked, MProduct finishedProduct, BigDecimal requiredQty)
	 * Copied from MProduction
	 * Used to create ProductionLines which are used to create stock movement and other stuff?
	 * In MTMProduction, every manufactured item will have different production lines
	 * the 'BOM' in MProduction is fixed, in MTMProduction the BOM is calculated dynamically
	 */
	
	/**
	 * Creates MProductionLine for the finished items
	 * @param mustBeStocked
	 * @return
	 */
	public int createLines(boolean mustBeStocked) {
		log.warning("--------In createLines(boolean mustBeStocked)");
		
		lineno = 100;
		
		count = 0;
		
		if((getM_Locator_ID()==0))
		{
			StringBuilder sql = new StringBuilder("SELECT m_locator_id ");
			sql.append("FROM m_locator WHERE ad_client_id = ");
			sql.append(Env.getAD_Client_ID(getCtx()));
			sql.append(" AND value = ");
			sql.append("'Standard'");
			m_Locator_ID = DB.getSQLValue(get_TrxName(), sql.toString());
		}

		
		/*
		 * product to be produced 
		 * In the context of MProduction, this would be one product only.
		 */
		MProduct finishedProduct = new MProduct(getCtx(), getM_Product_ID(), get_TrxName());

		MProductionLine line = new MProductionLine(getCtx(), 0, get_TrxName());
		log.warning("Line 276 About to create new MProductionLine with finished product" + finishedProduct.toString());
		line.setLine( lineno );
		line.setM_Product_ID( finishedProduct.get_ID() );
		line.setM_Locator_ID( m_Locator_ID);
		line.setMovementQty(BigDecimal.ONE);//always 1 for MTM manufacturing. Gets overwritten in MProductionLine.beforeSave.
		line.setPlannedQty(BigDecimal.ONE);//always 1 for MTM manufacturing
		//line.setIsEndProduct(true);
		line.setAD_Org_ID(determineAdOrgId());
		line.set_ValueOfColumn("bld_mtom_item_line_id", getbld_mtom_item_line_ID());
		log.warning("---------In MBLDMtomItemLine.createLines()..line.toString(): " + line.toString());
		line.saveEx();
		
		
		log.warning("----------------line.isEndProduct(): "+ line.isEndProduct());
		count++;
		
		createLines(mustBeStocked, finishedProduct, getProductionQty());
		
		return count;
	}
	
	/**
	 * Creates MProductionLine(s) from BOMderived for every MBLDMtomItemLine
	 * @param mustBeStocked
	 * @param finishedProduct
	 * @param requiredQty
	 * @return
	 */

	private int createLines(boolean mustBeStocked, MProduct finishedProduct, BigDecimal requiredQty) {
		
		log.warning("--------In createLines(boolean mustBeStocked, MProduct finishedProduct, BigDecimal requiredQty)");
		int defaultLocator = 0;
		
		MLocator finishedLocator = MLocator.get(getCtx(), getM_Locator_ID());
		
		int M_Warehouse_ID = finishedLocator.getM_Warehouse_ID();
		
		int asi = 0;

		/*
		 * products used in production
		 */
		
		MBLDBomDerived[] bomDerived = getBomDerivedLines(getCtx(), getbld_mtom_item_line_ID());
		log.warning("bomDerived [].toString() = " + bomDerived.toString() );
		try {
			int BOMProduct_ID = 0;
			BigDecimal BOMMovementQty;
			BigDecimal BOMQty;
		
		for(int i = 0; i< bomDerived.length; i++)
		{
			lineno = lineno + 10;
			BOMProduct_ID = bomDerived[i].getM_Product_ID();
			BOMQty = bomDerived[i].getQty();
			BOMMovementQty = BOMQty;
			MProduct bomProd = new MProduct(p_ctx, BOMProduct_ID, get_TrxName());
			log.warning("In for loop. i = " + i + "bomProduct: " + bomProd.getName());
			
			MProduct bomproduct = new MProduct(Env.getCtx(), BOMProduct_ID, get_TrxName());
			log.warning("bomproduct: " + bomproduct.getName());
				
				if ( bomproduct.isBOM() && bomproduct.isPhantom() )
				{
					log.warning("---------bomproduct.isBOM() && bomproduct.isPhantom() is TRUE");
					createLines(mustBeStocked, bomproduct, BOMMovementQty);//Is this recursive?
				}
				else
				{
					defaultLocator = bomproduct.getM_Locator_ID();
					if ( defaultLocator == 0 )
						defaultLocator = getM_Locator_ID();
					if ( defaultLocator == 0 )
						defaultLocator = m_Locator_ID;
					if (!bomproduct.isStocked())
					{					
						MProductionLine BOMLine = null;
						log.warning("Line 350 About to create new MProductionLine with BOMProduct_ID: " + BOMProduct_ID);
						BOMLine = new MProductionLine(getCtx(), 0, get_TrxName());
						BOMLine.setLine( lineno );
						BOMLine.setM_Product_ID( BOMProduct_ID );
						BOMLine.setM_Locator_ID( defaultLocator );  
						BOMLine.setQtyUsed(BOMMovementQty );
						BOMLine.setPlannedQty( BOMMovementQty );
						BOMLine.setAD_Org_ID(determineAdOrgId());
						BOMLine.set_ValueOfColumn("bld_mtom_item_line_id", getbld_mtom_item_line_ID());
						log.warning("Line 359 About to saveEx() new MProductionLine with BOMProduct_ID: " + BOMProduct_ID);
						BOMLine.saveEx(get_TrxName());
						
						lineno = lineno + 10;
						count++;					
					}
					else if (BOMMovementQty.signum() == 0) 
					{
						MProductionLine BOMLine = null;
						log.warning("Line 370 About to saveEx() new MProductionLine with BOMProduct_ID: " + BOMProduct_ID);
						BOMLine = new MProductionLine(getCtx(), 0, get_TrxName());
						BOMLine.setLine( lineno );
						BOMLine.setM_Product_ID( BOMProduct_ID );
						BOMLine.setM_Locator_ID( defaultLocator );  
						BOMLine.setQtyUsed( BOMMovementQty );
						BOMLine.setPlannedQty( BOMMovementQty );
						BOMLine.setAD_Org_ID(determineAdOrgId());
						BOMLine.set_ValueOfColumn("bld_mtom_item_line_id", getbld_mtom_item_line_ID());
						
						BOMLine.saveEx(get_TrxName());

						lineno = lineno + 10;
						count++;
					}
					else
					{

						// BOM stock info
						MStorageOnHand[] storages = null;
						MProduct usedProduct = MProduct.get(getCtx(), BOMProduct_ID);
						defaultLocator = usedProduct.getM_Locator_ID();
						if ( defaultLocator == 0 )
							defaultLocator = m_Locator_ID;
						
						if (usedProduct == null || usedProduct.get_ID() == 0)
							return 0;

						MClient client = MClient.get(getCtx());
						MProductCategory pc = MProductCategory.get(getCtx(),
								usedProduct.getM_Product_Category_ID());
						String MMPolicy = pc.getMMPolicy();
						if (MMPolicy == null || MMPolicy.length() == 0) 
						{ 
							MMPolicy = client.getMMPolicy();
						}

						storages = MStorageOnHand.getWarehouse(getCtx(), M_Warehouse_ID, BOMProduct_ID, 0, null,
								MProductCategory.MMPOLICY_FiFo.equals(MMPolicy), true, 0, get_TrxName());

						MProductionLine BOMLine = null;
						int prevLoc = -1;
						int previousAttribSet = -1;
						// Create lines from storage until qty is reached
						for (int sl = 0; sl < storages.length; sl++) {

							BigDecimal lineQty = storages[sl].getQtyOnHand();
							if (lineQty.signum() != 0) {
								if (lineQty.compareTo(BOMMovementQty) > 0)
									lineQty = BOMMovementQty;


								int loc = storages[sl].getM_Locator_ID();
								int slASI = storages[sl].getM_AttributeSetInstance_ID();
								int locAttribSet = new MAttributeSetInstance(getCtx(), asi,
										get_TrxName()).getM_AttributeSet_ID();

								// roll up costing attributes if in the same locator
								if (locAttribSet == 0 && previousAttribSet == 0
										&& prevLoc == loc) {
									BOMLine.setQtyUsed(BOMLine.getQtyUsed()
											.add(lineQty));
									BOMLine.setPlannedQty(BOMLine.getQtyUsed());
									BOMLine.saveEx(get_TrxName());

								}
								// otherwise create new line
								else {
									log.warning("Line 435 About to create new MProductionLine with BOMProduct_ID: " + BOMProduct_ID);
									BOMLine = new MProductionLine(getCtx(), 0, get_TrxName());
									BOMLine.setLine( lineno );
									BOMLine.setM_Product_ID( BOMProduct_ID );
									BOMLine.setM_Locator_ID( loc );
									BOMLine.setQtyUsed(lineQty);
									BOMLine.setPlannedQty( lineQty);
									BOMLine.setAD_Org_ID(determineAdOrgId());
									if ( slASI != 0 && locAttribSet != 0 )  // ie non costing attribute
										BOMLine.setM_AttributeSetInstance_ID(slASI);
									BOMLine.set_ValueOfColumn("bld_mtom_item_line_id", getbld_mtom_item_line_ID());
									log.warning("Line 446 About to save new MProductionLine with BOMProduct_ID: " + BOMProduct_ID);
									BOMLine.saveEx(get_TrxName());

									lineno = lineno + 10;
									count++;
								}
								prevLoc = loc;
								previousAttribSet = locAttribSet;
								// enough ?
								BOMMovementQty = BOMMovementQty.subtract(lineQty);
								if (BOMMovementQty.signum() == 0)
									break;
							}
						} // for available storages

						// fallback
						if (BOMMovementQty.signum() != 0 ) {
							if (!mustBeStocked)
							{

								// roll up costing attributes if in the same locator
								if ( previousAttribSet == 0
										&& prevLoc == defaultLocator) {
									BOMLine.setQtyUsed(BOMLine.getQtyUsed()
											.add(BOMMovementQty));
									BOMLine.setPlannedQty(BOMLine.getQtyUsed());
									BOMLine.saveEx(get_TrxName());

								}
								// otherwise create new line
								else {
									log.warning("Line 480 About to create new MProductionLine with BOMProduct_ID: " + BOMProduct_ID);
									BOMLine = new MProductionLine(getCtx(), 0, get_TrxName());
									BOMLine.setLine( lineno );
									BOMLine.setM_Product_ID( BOMProduct_ID );
									BOMLine.setM_Locator_ID( defaultLocator );  
									BOMLine.setQtyUsed( BOMMovementQty);
									BOMLine.setPlannedQty( BOMMovementQty);
									BOMLine.setAD_Org_ID(determineAdOrgId());
									BOMLine.set_ValueOfColumn("bld_mtom_item_line_id", getbld_mtom_item_line_ID());
									log.warning("Line 489 About to save new MProductionLine with BOMProduct_ID: " + BOMProduct_ID);
									BOMLine.saveEx(get_TrxName());
									log.warning("Line 491 Just saved new MProductionLine with BOMProduct_ID: " + BOMProduct_ID);
									lineno = lineno + 10;
									count++;
								}

							}
							else
							{
								throw new AdempiereUserError("Not enough stock of " + BOMProduct_ID);
							}
						}
					}
				}
			} // for all bom products
		} catch (Exception e) {
			throw new AdempiereException("Failed to create production lines " + e.getMessage(), e);
		}
		finally {
			if (log.isLoggable(Level.INFO)) log.info("Added ProductionLines---"  + toString());
		}

		return count;
	}
	
	private Object processLines(MProductionLine[] lines) {
		StringBuilder errors = new StringBuilder();
		for ( int i = 0; i<lines.length; i++) {
			String error = lines[i].createTransactions(getMovementDate(), false);
			if (!Util.isEmpty(error)) {
				errors.append(error);
			} else { 
				lines[i].setProcessed( true );
				lines[i].saveEx(get_TrxName());
			}
		}

		return errors.toString();
	}
	private Timestamp getMovementDate() {
		return getCreated();
	}
	
	public MProductionLine[] getLines(Properties ctx, int mtmlineid)
	 {
	 	final String whereClause = "m_productionline.bld_mtom_item_line_id"+"=?"; 
	 	List<MProductionLine> list = new Query(ctx, MProductionLine.Table_Name, whereClause, get_TrxName())
	 		.setParameters(new Object[]{mtmlineid})
	 		.setOrderBy(MProductionLine.COLUMNNAME_M_ProductionLine_ID)
	 		.list();
	 	return list.toArray(new MProductionLine[list.size()]);
	 }
	
	public String processProductionLines(int mtmLineID)
	{
		MProductionLine[] lines = getLines(p_ctx, mtmLineID);
		Object errors = processLines(lines);
		return errors.toString();
	}
	
	public void deleteProductionLines() {

		for (MProductionLine line : getLines(p_ctx, getbld_mtom_item_line_ID()))
		{
			line.deleteEx(true);
		}

	}// deleteLines
	
	
	public String getBarcode(int table_id, int record_id) {

		String prefix = getBarcodePrefix(table_id);
		if (prefix != null) {
			StringBuffer barCode = new StringBuffer(prefix);
			return barCode.append(record_id).toString();
		} else
			return null;

	}

	private String getBarcodePrefix(int table_id) {

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT name  ");
		sql.append("FROM ad_table ");
		sql.append("WHERE ad_table_id = ?");

		String tableName = DB.getSQLValueStringEx(get_TrxName(), sql.toString(), table_id);
		String prefix = null;

		if (tableName.equalsIgnoreCase("Made to measure production")) {
			prefix = MTM_PRODUCTION_PREFIX;
		} else if (tableName.equalsIgnoreCase("Made to measure items")) {
			prefix = MTM_PRODUCTION_ITEM_PREFIX;
		} else if (tableName.equalsIgnoreCase("Made to measure cuts")) {
			prefix = MTM_PRODUCTION_ASSEMBLEY_ITEM;
		}

		return prefix;
	}
	
	private int determineAdOrgId() {//If user neglects to set OrgID in production item, Header OrgId is returned.
		int adOrdId = getAD_Org_ID();
		if(adOrdId != 0) return adOrdId;
		else 
		{
			MBLDMtomProduction mBLDMtomProduction = new MBLDMtomProduction(p_ctx, getbld_mtom_production_ID(),get_TrxName());
			adOrdId = mBLDMtomProduction.getAD_Org_ID();
			return adOrdId;
		}
	}
	@Override
	protected boolean beforeSave(boolean newRecord) {
		if (getLine() == 0)
		{
			String sql = "SELECT COALESCE(MAX(Line),0)+10 FROM bld_mtom_item_line WHERE bld_mtom_item_line_id=?";
			int ii = DB.getSQLValue (get_TrxName(), sql, getbld_mtom_item_line_ID());
			setLine (ii);
		}
		
		MProduct mProduct = new MProduct(p_ctx, getM_Product_ID(), get_TrxName());
		if(mProduct.get_ValueAsBoolean("ismadetomeasure") && mProduct.isManufactured()) return true;
		else return false;
		
	}
	
	public BigDecimal getProductionQty() {
		return BigDecimal.ONE;
	}
	
/**
 * 
 * @param pobj
 */
	public void cleanupProductionLines(boolean isReversal, boolean isfirst) {
		
		log.warning("---------- In MBLDMtomItemLine.cleanupProductionLines()");
		
		int mBLDMtomItemLineID = get_ID();
		
		if(!isReversal)
		{
			if (getattributesetinstance_id() != 0 )
			{
				String where = "M_QualityTest_ID IN (SELECT M_QualityTest_ID " +
				"FROM M_Product_QualityTest WHERE M_Product_ID=?) " +
				"AND M_QualityTest_ID NOT IN (SELECT M_QualityTest_ID " +
				"FROM M_QualityTestResult WHERE M_AttributeSetInstance_ID=?)";
	
				List<MQualityTest> tests = new Query(Env.getCtx(), MQualityTest.Table_Name, where, get_TrxName())
				.setOnlyActiveRecords(true).setParameters(getM_Product_ID(), getattributesetinstance_id()).list();
				// create quality control results
				for (MQualityTest test : tests)
				{
					test.createResult(getattributesetinstance_id());
				}
			}
		}
		BigDecimal productionQty = getProductionQty();
		if(isReversal && !isfirst)
		{
			productionQty = getProductionQty().negate();
		}
			//mToMProductionParent = new MBLDMtomItemLine(pobj.getCtx(), getmBLDMtomItemLineID(), pobj.get_TrxName());
			StringBuilder sql = new StringBuilder("UPDATE ");
			sql.append("m_productionline SET ");
			sql.append("isendproduct = 'Y'");
			sql.append(", movementqty = " + productionQty);
			sql.append(" WHERE m_productionline.bld_mtom_item_line_id = ");
			sql.append(mBLDMtomItemLineID);
			sql.append(" AND m_product_id = ");
			sql.append(getM_Product_ID());
			log.warning("Attempting to DB.executeUpdate. Success is greater than 0 or Yes: " + (DB.executeUpdate(sql.toString(), get_TrxName())>0));
			
		log.warning("At end of MBLDMtomItemLine.cleanupProductionLines()");
	}

	
}
