package au.blindmot.make;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MAttributeSet;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MAttributeValue;
import org.compiere.model.MProduct;
import org.compiere.model.MProductionLine;
import org.compiere.model.Query;
import org.compiere.model.X_M_PartType;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MPPProductBOM;
import org.eevolution.model.MPPProductBOMLine;

import au.blindmot.model.I_BLD_MTM_Product_Bom_Trigger;
import au.blindmot.model.MBLDBomDerived;
import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDMtmProductBomAdd;
import au.blindmot.model.MBLDMtmProductBomTrigger;
import au.blindmot.model.MBLDMtomCuts;
import au.blindmot.model.MBLDMtomItemLine;
import au.blindmot.model.MBLDProductNonSelect;
import au.blindmot.model.MBLDProductPartType;
import au.blindmot.utils.MtmUtils;

/**
 * @author Phil Barnett
 *
 */
public abstract class MadeToMeasureProduct implements MtmInfo {

protected int wide = 0;
protected int high = 0;
protected int deep = 0;
protected int m_product_id = 0;
protected int mtom_item_line_id = 0;
protected String mtmInstanceParts = null;
protected CLogger log;
protected MBLDMtomItemLine mBLDMtomItemLine = null;
protected String trxName;
public static String CONTROL_SIDE = "Control Side";
public static String EACH = "Ea";
public static String EACH_1 = "Ea ";//Each with space



/**
 * 
 */
	public MadeToMeasureProduct() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor
	 * @param product_id
	 * @param bld_mtom_item_line_id
	 * @param transactionName
	 */
	public MadeToMeasureProduct(int product_id, int bld_mtom_item_line_id, String transactionName) {
		
		m_product_id = product_id;
		mtom_item_line_id = bld_mtom_item_line_id;
		mBLDMtomItemLine = new MBLDMtomItemLine(Env.getCtx(), bld_mtom_item_line_id, transactionName);
		log = CLogger.getCLogger (getClass());
		trxName = transactionName;
	}
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param depth
	 * @param product_id
	 * @param bld_mtom_item_line_id
	 */
	public MadeToMeasureProduct(int width, int height, int depth, int product_id, int bld_mtom_item_line_id) {
		wide = width;
		high = height;
		deep = depth;
		m_product_id = product_id;
		mtom_item_line_id = bld_mtom_item_line_id;
		
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean getCuts();//Return true if successful, delete created records if fail.
	public abstract boolean createBomDerived();//Return true if successful, delete created records if fail.
	public abstract boolean deleteBomDerived();
	public abstract boolean deleteCuts(); 
	public abstract boolean setUserSelectedPartIds();
	
	/**
	 * 
	 * @param parentBomID
	 * @param qty
	 * @return
	 */
	public boolean addTriggeredBom(int parentBomID, int triggeredQty) {
		
		/*BOM derived & BOM Derived Triggers
		 *In the BOM Derived, there is only 1 'Qty' field. There are also items with different UOMs.
		 *The triggeredQty is the number of items that the user wants to add to the BOM derived.
		 *For UOM of each, the 'quantity' field of BOM derived is set to the triggeredQty
		 *For UOM of length, the 'quantity' field of BOM derived is set by calling the this.getBomQty method 
		 *and the line is duplicated 'triggeredQty' times.
		 *If the triggeredQty == 0 then the qty from the parent (manufactured) product is used.
		 *
		 *Override this method to add MTM product BOM derived lines that require deductions etc.
		 */
		
		MPPProductBOMLine mBomItem = new MPPProductBOMLine(Env.getCtx(), parentBomID, null);//New MPPProductBOMLine object, no
	//	MPPProductBOMLine mBomItem = new MPPProductBOMLine(Env.getCtx(), parentBomID, null);
		int mProductID = mBomItem.getM_Product_ID();//Get the product_id of the bom product
		MProduct bomProduct = MProduct.get(Env.getCtx(), mProductID);
		//X_M_PartType mPartType = new X_M_PartType(Env.getCtx(), bomProduct.getM_PartType_ID(), null);
		BigDecimal qty = mBomItem.getQty();//getBOMQty() returns the qty from the parent product's BOM, excluding scrap
		BigDecimal bigTriggeredQty = new BigDecimal(triggeredQty);
		String uom = bomProduct.getUOMSymbol();
		
		addTriggeredLine(mProductID, uom, bigTriggeredQty, qty);
		
		return false;
		
	}
	
	public void addTriggeredLine(int mProductID, String uom, BigDecimal bigTriggeredQty, BigDecimal parentBOMqty) {
		if(bigTriggeredQty.compareTo(Env.ZERO) == 0 || bigTriggeredQty.compareTo(Env.ZERO) < 0 ) bigTriggeredQty = BigDecimal.ONE;
		if(uom.equalsIgnoreCase(EACH) || uom.equalsIgnoreCase(EACH_1))//if it's 'each' just add the required qty to BOM derived
		{
			addMBLDBomDerived(mProductID, bigTriggeredQty, 0, "Added by BOM trigger");
		}
		else//if it's not 'each', add a line for each
		{
			for(int i=0; i<bigTriggeredQty.intValue(); i++)
			{
				addMBLDBomDerived(mProductID, parentBOMqty, 0, "Added by BOM trigger");
			}
		}
	}
	
	/**
	 * Called in MBLDMtomItemLine class
	 * @return
	 */
	public boolean setAutoSelectedPartIds() {
		/*Get a list of auto selected parttypes
		 * iterate through list - for each item, 
		 * get the MBLDProductNonSelect[] MBLDProductPartType.getMBLDProductNonSelectLines(int mBLDProductPartTypeID, String trxn))
		 * Determine if the MBLDProductNonSelect matches the size of current item. 
		 * If it does, call a method based on each MBLDProductNonSelect operation type to modify BOM lines
		 * EG performSubstitution(subProduct, addProduct) performAdd(addProduct) performConditionSet(conditionSet)
		 * Override methods as required.
		 * 
		 */
		MBLDProductPartType[] mBLDProductPartTypeArray = getMBLDProductPartTypeLines();
		for(int j = 0; j < mBLDProductPartTypeArray.length; j++)
		{
			 MBLDProductNonSelect[] mBLDPNonSelectArray = mBLDProductPartTypeArray[j].getMBLDProductNonSelectLines(mBLDProductPartTypeArray[j].get_ID(), trxName);
			 for(int x = 0; x < mBLDPNonSelectArray.length; x++)
			 {
				 //Looping through Non Selectable Part Types -> auto set BOMderived parts based on widths and drops.
				 //Determine if this item matches width and drop criteria
				 if(mBLDPNonSelectArray[x].isWidthDropMatch(wide, high))
				 {
					 String operation = mBLDPNonSelectArray[x].getoperation_type();
					 if(operation.equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_OPERATION_ADDITION )) 
					 {
						 //Override method as required in concrete classes.
						 performOperationAddition(mBLDPNonSelectArray[x], mBLDProductPartTypeArray[j]);
					 }
					 else if(operation.equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_OPERATION_SUBSTITUTION))
					 {
						//Override method as required in concrete classes.
						 performOperationSubstitution(mBLDPNonSelectArray[x]);
					 }
					 else if(operation.equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_OPERATION_CONDITION_SET))
						 
					 {
						//Override method as required in concrete classes.
						 performOperationConditionSet(mBLDPNonSelectArray[x]);
					 }
					 
					 else if(operation.equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_OPERATION_DELETE))
					 {
						//Override method as required in concrete classes.
						 performOperationDelete(mBLDPNonSelectArray[x]);
					 }
					 else if(operation.equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_OPERATION_ATTRIBUTE_BASED_ADD) || 
							 operation.equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_OPERATION_ATTRIBUTE_BASED_DELETE))
					 {
						//Override method as required in concrete classes.
						 performOperationAttributeBased(mBLDPNonSelectArray[x], mBLDProductPartTypeArray[j], operation);
					 }
				 }
			 }
		}
		return true;
	}//setAutoSelectedPartIds()

	/**
	 * 
	 * @param mbldProductNonSelect
	 * @param mBLDProductPartTypeArray
	 * @param operation
	 * @return
	 */
	private boolean performOperationAttributeBased(MBLDProductNonSelect mbldProductNonSelect, MBLDProductPartType mBLDProductPartTypeArray, String operation) {
		
		AttributePair[] attributePairs = getMAttributeSetInstance();
		AttributePair nonSelectAttributes = getAttributeFromMBLDProductNonSelect(mbldProductNonSelect);
		for(int i = 0; i < attributePairs.length; i++)
		{
			if(attributePairs[i].equals(nonSelectAttributes))
			{
				if(operation.equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_OPERATION_ATTRIBUTE_BASED_ADD))
				{
					performOperationAddition(mbldProductNonSelect, mBLDProductPartTypeArray);
				}
				else if(operation.equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_OPERATION_ATTRIBUTE_BASED_DELETE))
				{
					performOperationDelete(mbldProductNonSelect);
				}
				
			}
		}
		//Get instance attributes
		//Check if attributes from product match mbldProductNonSelect variable
		//Add if they do
		return true;
	}

	//public abstract boolean addMtmInstancePartsToBomDerived();
	public boolean addMtmInstancePartsToBomDerived() {
		MBLDLineProductInstance[] mBLDLineProductInstance = getMBLDLineProductInstance();
															
		for(int i = 0; i < mBLDLineProductInstance.length; i++)
		{
			int mProductId = mBLDLineProductInstance[i].getM_Product_ID();
			BigDecimal qty = getBomQty(mProductId);
			
			int mAttributeSetInstanceId = mBLDLineProductInstance[i].getM_AttributeSetInstance_ID();
			
			if(qty.compareTo(Env.ZERO)<1)
			{
				if(isProductPartTypeSelectable(mProductId, m_product_id))//If it's a selectable parttype, then qty = 1
					{
						qty = Env.ONE;//Ensure that anything user selectable makes it to BOM.
					}
					else qty = getMProductBomQty(mProductId);//Get a default from parent BOM
			}
			addMBLDBomDerived(mProductId, qty, mAttributeSetInstanceId, "Added by addMtmInstancePartsToBomDerived()");
		}
		return true;
	}//addMtmInstancePartsToBomDerived
	
	public AttributePair getAttributeFromMBLDProductNonSelect(MBLDProductNonSelect mbldProductNonSelect) {
		AttributePair nonSelectAttributes = new AttributePair();
		int mAttributeID = (int) mbldProductNonSelect.get_Value("m_attribute_id");
		int mAttributeValueID = (int) mbldProductNonSelect.get_Value("m_attributevalue_id");
		nonSelectAttributes.setInstance((new MAttribute(Env.getCtx(), mAttributeID, null).getName()));
		nonSelectAttributes.setInstanceValue((new MAttributeValue(Env.getCtx(), mAttributeValueID, null).getName()));
		return nonSelectAttributes;
		
	}
	
	public int getWide() {
		return wide;
	}

	public void setWide(int wide) {
		this.wide = wide;
	}

	public int getHigh() {
		return high;
	}

	public void setHigh(int high) {
		this.high = high;
	}

	public int getDeep() {
		return deep;
	}

	public void setDeep(int deep) {
		this.deep = deep;
	}

	/**
	 * 
	 * @param mAttribute
	 */
	public abstract void interpretMattributeSetInstance();
	
	protected void addBldMtomCuts(int mProductID, int width, int length, int height){
		BigDecimal bigWidth = new BigDecimal(width);
		BigDecimal bigLength = new BigDecimal(length);
		BigDecimal bigHeight = new BigDecimal(height);
		if(mProductID != 0)
		{
			addBldMtomCuts(mProductID, bigWidth, bigLength, bigHeight);
		}
		
	}
	
	protected void addBldMtomCuts(int mProductID, BigDecimal width, BigDecimal length, BigDecimal height) {
		if(mProductID != 0)
		{
			MBLDMtomCuts cut = new MBLDMtomCuts(Env.getCtx(), 0, trxName);
			cut.setWidth(width);
			cut.setLength(length);
			cut.setHeight(height);
			cut.setM_Product_ID(mProductID);
			cut.setbld_mtom_item_line_ID(mtom_item_line_id);
			cut.saveEx();
		}
	}
	
	public AttributePair[] getMAttributeSetInstance() {
		int mAttributeSetInstance_ID = mBLDMtomItemLine.getattributesetinstance_id();
		MAttributeSetInstance mAttributeSetInstance = new MAttributeSetInstance(null, mAttributeSetInstance_ID, null);
		
		MAttributeSet mAttributeSet = new MAttributeSet(null, mAttributeSetInstance.getM_AttributeSet_ID(), null);
		
		/*Notes: mai contains the actual instance value, like '1600' or 'Right hand chain'
		 * attributes[] holds the names of the attributes in order that they appear in the Attribute Set Instance dialog box.
		 * Original plan was to 'interpret' parts required from the MAI then match them to BOM items. This is a bad idea.
		 * BETTER would be to select the parts directly from BOM via the MtmButton.
		 * HACK: Add to MProducts something like 'blind control', 'blind bracket', 'blind tube', 'blind bottom bar' etc.
		 * 
		 * So what do we set with MAIs? Things that aren't directly related to parts!
		 * Width, drop, control side, location - what else?
		 * MbldLineProduct instance (Product options) in conjunction with BOM triggers handle changes in parts related to size and Mattribute instance triggered changes.
		 */
		
		MAttribute[] attributes = mAttributeSet.getMAttributes(true);
		List<AttributePair> list = new ArrayList<AttributePair>();
		for (int i = 0; i < attributes.length; i++) {
			MAttributeInstance mai = attributes[i].getMAttributeInstance(mAttributeSetInstance.getM_AttributeSetInstance_ID());
			//Below commented out 26/7/22; was causing termination of loop when a null attribute value was found.
			/*if (mai == null || mai.getValue() == null || attributes[i] == null)
			{
				break;
			}
			else*/
			{
				
				if(attributes[i].getName() == null)
				{
					log.log(Level.SEVERE, "Atrributes missing from mtom_item_line_id: " + mtom_item_line_id);
				}
				
				else if(attributes[i].getName().equalsIgnoreCase("Width"))
				{
					wide = Integer.parseInt(mai.getValue());
				}
				else if(attributes[i].getName().equalsIgnoreCase("Drop"))
				{
					high = Integer.parseInt(mai.getValue());
				}
				else if(attributes[i].getName().equalsIgnoreCase("Height"))
				{
					high = Integer.parseInt(mai.getValue());
				}
				else if(attributes[i].getName().equalsIgnoreCase("Depth"))
				{
					deep = Integer.parseInt(mai.getValue());
				}
				
				else 
					{
					if (mai.getValue() != null || mai != null || attributes[i] != null)
						{
							list.add(new AttributePair(attributes[i].getName().toString(), mai.getValue()));//Add the remaining attributes to an AtributePair
						}
					

					}
				
				/*
				 * Notes: mai contains the actual instance value, like '1600' or 'Right hand chain'
				 * attributes[] holds the names of the attributes in order that appear in the Attribute Set Instance dialog box.
				 * 
				 */
			}
				
		}
		if(mBLDMtomItemLine.getinstance_string() != null)mtmInstanceParts = mBLDMtomItemLine.getinstance_string();
		return list.toArray(new AttributePair[list.size()]);
		/*mtmInstanceParts
		 * TODO: Add fields for fabric and chain get/set/handle MBLDMtomItemLine.instance_string
		 * Note MBLDMtomItemLine.instance_string holds the values for chain and fabric product_ids
		 */
		
		
	}
	
	public int getWaste(int mprodID) {
		StringBuilder sql = new StringBuilder("SELECT ma.value FROM m_attributeinstance ma");
		sql.append(" WHERE ma.m_attributesetinstance_id =");
		sql.append(" (SELECT mp.m_attributesetinstance_id FROM m_product mp WHERE mp.m_product_id = ");
		sql.append(mprodID + ")");
		sql.append(" AND ma.m_attribute_id =");
		sql.append(" (SELECT mat.m_attribute_id FROM m_attribute mat WHERE mat.name = 'Waste' AND mat.isactive = 'Y')");
		int waste = 0;
		//Note attribute must be called "Waste%" and nothing else.
		
		log.warning("--------Calling MtmUtils.attributePreCheck(\"Waste\")");
		MtmUtils.attributePreCheck("Waste");
		//log.warning("--------Calling MtmUtils.attributePreCheck(\"waste%\")");
		//MtmUtils.attributePreCheck("waste%");
		//log.warning("--------Calling MtmUtils.attributePreCheck(\"waste\")");
		//MtmUtils.attributePreCheck("waste");
		int checkWaste = DB.getSQLValue(trxName, sql.toString());
		if(checkWaste > 0) return checkWaste;
		log.warning("---------No waste attribute found for MProuctID: " + mprodID + " Returning waste = " + waste);
		return waste;
	}
	
	/**
	 * Possibly change to non abstract and write generic method body.
	 * Or create another method to provide the MAttributeSetInstance interpretation and return it in an array,
	 * then have an abstract method that requires subclasses to do something with it?
	 * Set object dimension fields so that other methods can use them.
	 * e.g. int theHeight = //some code;
	 * this.height = theHeight;
	 * Set other fields unique to concrete class
	 * e.g. int theLeftMech = //some code;
	 * this.leftMech = theLeftMech;//set the product_iD of the left mech
	 * @param mAttributeSetInstanceId 
	 */ 

	public void addMBLDBomDerived(int mProductId, BigDecimal qty, int mAttributeSetInstanceId, String description) {
		log.warning("---------In addMBLDBomDerived(int mProductId, BigDecimal qty, String description) with mProductId: " + mProductId + " qty:" + qty + " description: " + description);
		log.warning("---------mProductId: " + mProductId + " qty: " + qty);
		if(mProductId > 0 && qty.compareTo(BigDecimal.ZERO) > 0)
		{
			MBLDBomDerived mBomDerived = new MBLDBomDerived(Env.getCtx(), 0, trxName);
			mBomDerived.setbld_mtom_item_line_ID(mtom_item_line_id);
			mBomDerived.setM_Product_ID(mProductId);
			qty.setScale(2, RoundingMode.HALF_EVEN);
			mBomDerived.setQty(qty);
			mBomDerived.setM_AttributeSetInstance_ID(mAttributeSetInstanceId);
			int ppProductBomLineID = getParentBOMLineID(mProductId);
			mBomDerived.setPP_Product_Bomline_ID(ppProductBomLineID);
			if(description != null)mBomDerived.setDescription(description);
			mBomDerived.saveEx();
		}
	}
	
	public void addMBLDBomDerived(int mProductId, BigDecimal qty, String description) {
		addMBLDBomDerived(mProductId, qty, 0, description);
	}
	
	public MBLDProductPartType[] getMBLDProductPartTypeLines() {
		 {
			 	final String whereClause = "bld_product_parttype.m_product_id"+"=?"; 
			 	List<MProductionLine> list = new Query(Env.getCtx(), MBLDProductPartType.Table_Name, whereClause, trxName)
			 		.setParameters(new Object[]{m_product_id})
			 		.setOrderBy(MBLDProductPartType.COLUMNNAME_M_Product__ID)
			 		.list();
			 	return list.toArray(new MBLDProductPartType[list.size()]);
			 }
	}
	
	/**
	 * 
	 * @return
	 */
	public MBLDLineProductInstance[] getMBLDLineProductInstance() {
		int bld_Line_ProductSetInstance_ID = mBLDMtomItemLine.getBld_Line_ProductSetInstance_ID();
		MBLDLineProductInstance[] mBLDLineProductInstance = MBLDLineProductInstance.getmBLDLineProductInstance(bld_Line_ProductSetInstance_ID, trxName); 
		return mBLDLineProductInstance;
	}//getMBLDLineProductInstance
	
	public  boolean  performOperationDelete(MBLDProductNonSelect mBLDPNonSelect) {
		 //perform delete. Move to abstract class as separate method?
		 MBLDBomDerived[] bomDerived = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), mBLDMtomItemLine.getbld_mtom_item_line_ID());
		 int subID = Integer.parseInt(mBLDPNonSelect.getsubstituteproduct().toString());
		 for(int z = 0; z < bomDerived.length; z++)
		 {
			 if(bomDerived[z].getM_Product_ID() == subID)
			 {
				 bomDerived[z].delete(true, trxName);
			 }
		 } 
		 return true;
	}//performOperationDelete
	
	public boolean performOperationSubstitution(MBLDProductNonSelect mBLDPNonSelect) {
		 //perform substitution
		 //get BOM line with substitute product & swap productID with Additional product
		 int addID = Integer.parseInt(mBLDPNonSelect.getaddtionalproduct().toString());
		 int subID = Integer.parseInt(mBLDPNonSelect.getsubstituteproduct().toString());
		 MBLDBomDerived[] bomDerived = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), mBLDMtomItemLine.getbld_mtom_item_line_ID());
		 for(int z = 0; z < bomDerived.length; z++)
		 {
			 if(bomDerived[z].getM_Product_ID() == subID)
			 {
				 bomDerived[z].setM_Product_ID(addID);
				 bomDerived[z].saveEx();
			 }
		 }
		return true;
		 
	 
	}//performOperationSubstitution
	
	public boolean performOperationAddition(MBLDProductNonSelect mBLDPNonSelect, MBLDProductPartType mBLDProductPartType) {
		
		//perform addition
		 int addID = Integer.parseInt(mBLDPNonSelect.getaddtionalproduct().toString());
		 X_M_PartType addPartType = new X_M_PartType(Env.getCtx(), mBLDProductPartType.getM_PartTypeID(), null);
		 
		 if(addPartType != null)
		 {
			 BigDecimal qty = getBomQty(addID);
			 if(qty.compareTo(BigDecimal.ZERO) == 0)
			 {
				 qty = BigDecimal.ONE;//Makes Sure it gets added as zeros get skipped in addMBLDBomDerived(addID, qty, trxName);
			 }
			 addMBLDBomDerived(addID, qty, 0, trxName); 	
		 }
		
		return true;
		
		
		//return true;
	}//performOperationAddition
	
	/**
	 * Gets the Bom qty required for product to go onto the concrete class' BOM.
	 * Return BigDecimal.ZERO if the mProductBomid is not found in the if() statements.
	 * @Override
	public BigDecimal getBomQty(int mProductBomid) { 
		BigDecimal qty = BigDecimal.ZERO;
		if(mProductBomid == rollerTubeID)
			{
				qty = getRollerTubeQty(rollerTubeID);
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
				
			} else if(mProductBomid == fabricID)
			{
				qty = getFabricQty();
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			} else if(mProductBomid == bottomBarID)
			{
				qty = getBottomBarQty();
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			}
		return qty;//Default
	 * @param addID
	 * @return
	 */
	public abstract BigDecimal getBomQty(int mProductBomid); 
	
	/**
	 * Gets the BOM qty from the Parent BOM product.
	 * @param mProductBomID
	 * @return
	 */
	public BigDecimal getMProductBomQty(int mProductBomID) {
		
		int parentBomID = MtmUtils.getActivePPProductBomID(m_product_id);
		
		StringBuilder sql = new StringBuilder("SELECT pp_product_bomline_id ");
        sql.append("FROM pp_product_bomline ");
        sql.append("WHERE m_product_id = ");
        sql.append(mProductBomID);
        sql.append(" AND pp_product_bom_id = ");
        sql.append(parentBomID);
        
        int pp_product_bomline_id = DB.getSQLValue(trxName, sql.toString());
        
        MPPProductBOMLine mProductBom = new MPPProductBOMLine(Env.getCtx(), pp_product_bomline_id, trxName);
		//if(mProductBom.get_ValueAsBoolean(columnName))
		
		
		
		BigDecimal bigQty = mProductBom.getQty();
	
	return bigQty;
	
	}

	public boolean performOperationConditionSet(MBLDProductNonSelect mBLDPNonSelect) {
		/*
		 *  //perform conditon set
		 if(mBLDPNonSelect.getcondition_set().equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_CONDITION_HAS_LIFT_SPRING))
		 {
			 if(isChainControl)//Add a lift spring if it's chain controlled
				{
					int liftID = getLiftSpring(false);
					if(liftID  > 0)addBomDerivedLines(liftID, null);	
				}
		 }
	 
	return true;
		 */
		return true;
	}//performOperationConditionSet
	
	public boolean processTriggers(MBLDMtomItemLine itemLine) {
		MBLDMtmProductBomTrigger[] triggers = getTriggers(itemLine.getM_Product_ID());
		MBLDBomDerived[] bomDerivedLines = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), itemLine.get_ID());
		//Loop through bomDerivedLines, check if trigger is in bomDerivedLines
		for(int g = 0; g < bomDerivedLines.length; g++)
		{
			for(int q= 0; q < triggers.length; q++)
			{
				boolean isTriggerDelete = triggers[q].isTriggerDelete();
				if(bomDerivedLines[g].getPP_Product_Bomline_ID() == triggers[q].getPP_Product_Bomline_ID())
				{
					//If the trigger matches the BOMderived line then add the trigger products
					MBLDMtmProductBomAdd[] bomsToChange = triggers[q].getLines(null, null);
					for(int yy = 0; yy < bomsToChange.length; yy++)
					{
						int parentBomID = bomsToChange[yy].getPP_Product_Bomline_ID();
						if(isTriggerDelete)
						{
							deleteBOMLine(parentBomID, itemLine);
						}
						else
						{
							int triggeredQty = bomsToChange[yy].getQty().intValue();
							addTriggeredBom(parentBomID, triggeredQty);
						}
					}
				}
				
			}
		}
		return false;
		
	}

	/**
	 * @return
	 */
	private MBLDMtmProductBomTrigger[] getTriggers(int mProductID) {
		
		//int mProductID = itemLine.getM_Product_ID();
		StringBuilder whereClauseFinal = new StringBuilder(MBLDMtmProductBomTrigger.COLUMNNAME_M_Product_ID+"=? ");
	
		List<MBLDMtmProductBomTrigger> list = new Query(Env.getCtx(), I_BLD_MTM_Product_Bom_Trigger.Table_Name, whereClauseFinal.toString(), null)
										.setParameters(mProductID)
										.setOrderBy(null)
										.list();
		
		return list.toArray(new MBLDMtmProductBomTrigger[list.size()]);	
		
		//	getTriggers
		 
	}
	
	/**
	 * Deprecate
	 * @param bomProductID
	 * @return
	 */
	public int getParentBOMLineID(int bomProductID) {
		int activePPProductBomID = MtmUtils.getActivePPProductBomID(m_product_id);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT pp_product_bomline_id ");
		sql.append("FROM pp_product_bomline mpb ");
		sql.append("WHERE mpb.pp_product_bom_id = ? ");
		sql.append("AND mpb.m_product_id = ? ");
		sql.append("FETCH FIRST 1 ROWS ONLY");
		return DB.getSQLValue(null, sql.toString(), activePPProductBomID, bomProductID/*needs to be m_product_id*/);
		
	}
	
	public boolean deleteBOMLine(int mParentBOMLineID, MBLDMtomItemLine itemLine) {
		MBLDBomDerived[] bomLines = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), itemLine.get_ID());
		for(int l = 0; l < bomLines.length; l++)
		{
			if(bomLines[l].getPP_Product_Bomline_ID() == mParentBOMLineID)
			{
				bomLines[l].delete(true, itemLine.get_TrxName());
			}
		}
		return true;
	}

	public List<String> getConfig() {
		// TODO Auto-generated method stub
		return null;
	}
	
	 /**
	  * This method gets the BOM line product IDs and was part of a refactor 
	  * to allow the user to change BOM line product types. Must be called after the BOM has been populated, ie after createBomDerived()
	  * MOVED TO SUPERCLASS 31/8/2021
	  * @param partType
	  * @return
	  */
	 public int getBomDerivedProductID(String partType) {
		 log.warning("---------In getBomProductID(String partType)---> String partType = " + partType);
		 MBLDBomDerived[] bomDerived = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), mBLDMtomItemLine.getbld_mtom_item_line_ID());
		 log.warning("--------In getBomProductID() bomDerived: " + bomDerived.toString());
		 
		 for(int p = 0; p < bomDerived.length; p++)
		 {
			 int productID = bomDerived[p].getM_Product_ID();
			 MProduct mProduct = new MProduct(Env.getCtx(), productID, trxName);
			 int mPartTypeID = mProduct.getM_PartType_ID();
			 StringBuilder sql = new StringBuilder("SELECT name FROM m_parttype ");
			 sql.append(" WHERE m_parttype_id = ?");
			 String partName = DB.getSQLValueString(trxName, sql.toString(), mPartTypeID);
		
			 if(partName != null)
			 {
				 if(partName.equalsIgnoreCase(partType)) 
				 {
					 log.warning("---------returning mProduct: " + mProduct.getName() + "For parttype: " + partType);
					 return productID; 
				 }
			 }
			 else
			 {
				 log.warning("Could not get a part name for partType: " + partType);
			 }
			 
		 }
		 return 0;
	 }//getBomProductID
	 
	 /**
	  * 
	  * @param partType
	  * @return
	  */
	 public Integer[] getProductIDFromBldLineSetInstance(String partType) {
		 log.warning("---------In getProductIDFromBldLineSetInstance(String partType)---> String partType = " + partType);
		 MBLDLineProductInstance[] mBLDLineProductInstance = getMBLDLineProductInstance();
		 ArrayList<Integer> foundIDs = new ArrayList<Integer>();
		 
		 /*
		 for (int i = 0; i < mBLDLineProductInstance.length; i++)
			{
				
				if (mBLDLineProductInstance != null && mBLDLineProductInstance[i].getM_Product_ID() > 0)
				{
					MProduct bldPartSetProduct = new MProduct(Env.getCtx(), mBLDLineProductInstance[i].getM_Product_ID(), trxName);
					//if (sb.length() > 0)
						//sb.append("_");
					//sb.append(nameAdd.getName());
				}
			}
		 */
		 
		 for(int i = 0; i < mBLDLineProductInstance.length; i++)
		 {
			System.out.println(mBLDLineProductInstance[i].getBLD_Product_PartType());
			MProduct bldPartSetProduct = new MProduct(Env.getCtx(), mBLDLineProductInstance[i].getM_Product_ID(), trxName);
			X_M_PartType xMPartType = new X_M_PartType(Env.getCtx(), bldPartSetProduct.getM_PartType_ID(), trxName);
			
			if(xMPartType != null && xMPartType.getName() != null && xMPartType.getName().equalsIgnoreCase(partType))
			 {
				int partTypeID = mBLDLineProductInstance[i].getM_Product_ID();
				foundIDs.add(Integer.valueOf(partTypeID));
			 }
		 }
		 
		return foundIDs.toArray(new Integer[foundIDs.size()]);

	 }
	 
	 public boolean updateBomQty() {
		 //Get Bom derived
		 MBLDBomDerived[] bomDerived = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), mBLDMtomItemLine.getbld_mtom_item_line_ID());
		 for(int x = 0; x < bomDerived.length; x++ )
		 {
			 //Loop through Bom derived, call getBomQty(productID) for each line
			 int mProductBomID = bomDerived[x].getPP_Product_Bomline_ID();
			 int mProductID = bomDerived[x].getM_Product_ID();
			 MProduct productToUpdate = MProduct.get(mProductID);
			 log.warning("-----Product checking qty for is: " + productToUpdate.getName());
			 BigDecimal qty = getBomQty(mProductID);//returns 0 if a qty is not set.
			 if(qty.compareTo(Env.ZERO) > 0)
			 {
				 bomDerived[x].setQty(qty);
				 bomDerived[x].saveEx(trxName);
			 }
		 }
		 return true;
	 }
	 
	 public boolean addMtomItemDetail() {
		 return true;
	 }
	 
	 /**
	  * 
	  * @param bomProductID
	  * @param m_product_id
	  * @param mPartTypeID
	  * @return
	  */
	 public static boolean isProductPartTypeSelectable(int bomProductID, int m_product_id) {
		 
		 MBLDProductPartType[] mBLDProductPartTypes = MBLDProductPartType.getMBLDProductPartTypes(Env.getCtx(), m_product_id, null);
		 boolean found = false;
		if(bomProductID > 0) 
		{
			int partTypeID = MProduct.get(bomProductID).getM_PartType_ID();
		
		 
		 for(int q = 0; q < mBLDProductPartTypes.length; q++)
		 {
			 if(mBLDProductPartTypes[q].getM_PartTypeID() == partTypeID)//It's a selectable parttype
			 {
				 found = true;
				 break;
			 }
		 }
	 }
		return found;
	 }
	
}//MadeToMeasureProduct
