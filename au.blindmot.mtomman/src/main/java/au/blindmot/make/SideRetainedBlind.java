/**
 * 
 */
package au.blindmot.make;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.compiere.model.MProduct;
import org.compiere.model.MProductBOM;
import org.compiere.model.X_M_PartType;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.model.MBLDBomDerived;
import au.blindmot.model.MBLDMtomCuts;
import au.blindmot.utils.MtmUtils;

/**
 * @author phil
 *
 */
public class SideRetainedBlind extends RollerBlind  {
	
	public static String MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL = "Awning Channel";
	public static String MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL_COVER = "Awning channel cover";
	public static String MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX_BACK = "Head Box back";
	public static String MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX = "Head Box";
	public static String SIDE_RETAINED_BLIND = "Side Retained Blind";
	
	
	/**
	 * @param mProduct_ID
	 * @param mtom_item_line_id
	 * @param trxnName
	 */
	public SideRetainedBlind(int mProduct_ID, int mtom_item_line_id, String trxnName) {
		super(mProduct_ID, mtom_item_line_id, trxnName);
	}
	
	@Override
	public void interpretMattributeSetInstance() {
	super.interpretMattributeSetInstance();

	}
	
	/**
	 * @Override
	 * @see au.blindmot.make.MtmInfo#getConfig()
	 */
			
	public List<String> getConfig() {
			ArrayList <String> config = new ArrayList<String>();
			config.add("Attribute: Bottom bar addition - The number to DEDUCT from overall width (not related to tube) to get the bottom bar width."+System.lineSeparator());
			config.add("Attribute: SRS ONLY - Fabric deduction The number to DEDUCT from overall width (not related to tube), same for all control types"+System.lineSeparator());
			config.add("Attribute: NON SRS - Fabric deduction The number to DEDUCT from tube to get fabric width, same concept as ordinary roller blind"+System.lineSeparator());
			config.add("Attribute: Fabric length addition - How much extra to add to fabric cut relative to the length of a blind."+System.lineSeparator());
			config.add("Attribute: Drop deduction - The number to deduct from the drop to get dimensions for side rails etc."+System.lineSeparator());
			config.add("Attribute: Overall deduction - The number to deduct from the width to get dimensions for the front cover and back plate."+System.lineSeparator());
			config.add("Instance Attribute: Width"+System.lineSeparator());
			config.add("Instance Attribute: Drop"+System.lineSeparator());
			config.add("Instance Attribute: Blind control side - List: Left, Right"+System.lineSeparator());
			config.add("Instance Attribute: Roll Type - List: NR, RR"+System.lineSeparator());
			config.add("NOTE: Side rail parts have an attribute 'Drop deduction Adjust' that takes another deduction on top of the drop deduction. This varies per part type");
			
			return config;
		}//getConfig()

	
	/**
	 * @Override
	 */
	public boolean getCuts() {
		
		/*
		 *Get head rail deductions - head rail is the top part of roller blind from bracket to bracket.
		 *2. tube cut length = width -  sum(rail head deductions)
		 *3. fabric cut width =  width - fabric deduction
		 *4. fabric cut length = length + Fabric length addition - which is a non instance attribute of the
		 *the actual finished product, eg 'roller blind'.
		 *5. bottom bar cut length = width - bottom bar deduction.
		 *6. As each cut is determined, the qty in the BOM derived items for fabric, tube and bottom bar 
		 *will need to be set.
		 */
		
		log.warning("---------In getCuts()");
		
	    setUserSelectedPartIds();
	    setChainControl(controlID);
		populatePartTypes(m_product_id);//Gets the ArrayLists of partsget
		setupTubeFabric();
		
		//Get a value for variable fabricWidth & fabricDrop
		BigDecimal fabricWidth = getFabricWidth();
		BigDecimal fabricDrop = getFabricDrop();
		BigDecimal bottomBarCut = getBottomBarCut();
		
		if(fabricID !=0 )

			{
				addBldMtomCuts(fabricID, fabricWidth, fabricDrop, 0);	
			}
	
		
		if(rollerTubeID !=0 )
			{
				addBldMtomCuts(rollerTubeID,Env.ZERO,getRollerTubeCut(wide),0);
				log.warning("--------addBldMtomCuts Adding roller tube to cuts: " + rollerTubeID);
			}
		
		int bottombarIDToUse = 0;
		int bottombID = getBomProductID(PART_TYPE_BOTTOM_BAR);
		if(bottombID > 0)
		{
			bottombarIDToUse = bottombID;
		}
		else
		{
			bottombarIDToUse = bottomBarID;
		}
		
		if(bottombarIDToUse !=0 )

			{
				addBldMtomCuts(bottombarIDToUse,Env.ZERO,bottomBarCut,0);
			}
		
		MBLDBomDerived[] bomDerivedLines = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), mtom_item_line_id);
		
		//Get the overall drop deduction from parent 
		int parentDropDeduction = MtmUtils.getDeduction(m_product_id, MtmUtils.MTM_DROP_DEDUCTION);
		
		//Loop through BOMderived.
		for (MBLDBomDerived lines : bomDerivedLines)
		{
			int bomLineProductID = lines.getM_Product_ID();
			String mPartType = MtmUtils.getMPartype(bomLineProductID);
			if(mPartType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL_COVER)
					|| mPartType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL))//Find 'Awning channel' part types.
				{
					//Get MTM_DROP_DEDUCTION_ADJUST
					int dropAdjust = MtmUtils.getDeduction(bomLineProductID, MtmUtils.MTM_DROP_DEDUCTION_ADJUST);
					log.warning("-------GetCuts() dropAdjust : " + dropAdjust);
					int totalDeduction = parentDropDeduction + dropAdjust;
					int cut = high - totalDeduction;
					addBldMtomCuts(bomLineProductID, 0, cut, 0);
				}
			
			if(mPartType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX_BACK)
					|| mPartType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX))
				{
					//Get MTM headbox overall deduction
					int overallDeduction = MtmUtils.getDeduction(m_product_id, MtmUtils.MTM_OVERALL_DEDUCTION);
					log.warning("-------GetCuts() Overall deduction is: " + overallDeduction);
					int cut = wide - overallDeduction;
					addBldMtomCuts(bomLineProductID, 0, cut, 0);
				}
		}
		return true;
	}//getCuts()

	

	@Override
	public boolean deleteBomDerived() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCuts() {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#addTriggeredBom(int)
	 */
	@Override
	public boolean addTriggeredBom(int parentBomID, int triggeredQty) {
		MProductBOM mBomItem = new MProductBOM(Env.getCtx(), parentBomID, null);
		int mProductID = mBomItem.getM_ProductBOM_ID();
		//MProduct bomProduct = MProduct.get(Env.getCtx(),mBomItem .getM_ProductBOM_ID());
		X_M_PartType mPartType = new X_M_PartType(Env.getCtx(), mBomItem.getM_PartType_ID(), null);
		String partType = mPartType.getName();
		BigDecimal bigTriggeredQty = new BigDecimal(triggeredQty);
		
		/*BOM derived & BOM Derived Triggers
		 *In the BOM Derived, there is only 1 'Qty' field. There are also items with different UOMs.
		 *The triggeredQty is the number of items that the user wants to add to the BOM derived.
		 *For UOM of each, the 'quantity' field of BOM derived is set to the triggeredQty
		 *For UOM of length, the 'quantity' field of BOM derived is set by calling the this.getBomQty method 
		 *and the line is duplicated 'triggeredQty' times.
		 *If the triggeredQty == 0 then the qty from the parent (manufactured) product is used.
		 */
		MProduct bomProduct = MProduct.get(Env.getCtx(), mProductID);
		String uom = bomProduct.getUOMSymbol();
		BigDecimal bomDerivedQty;
		//if it's a side channel, headbox or channel part, get the drop less drop deduction plus waste and set as BOM derived qty.
		if(partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL) 
				|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL_COVER)
				|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX_BACK)
				|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX))
		{
			bomDerivedQty = getBomQty(mProductID, partType);
			addTriggeredLine(mProductID, uom, bigTriggeredQty, bomDerivedQty);
		}
		else //If it's something we don't specifically care about then add using generic Superclass method
		{
			bomDerivedQty = mBomItem.getBOMQty();
			addTriggeredLine(mProductID, uom, bigTriggeredQty, bomDerivedQty);
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#createBomDerived()
	 */
	@Override
	public boolean createBomDerived() {
		super.createBomDerived();
		return true;
	}

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#addMtmInstancePartsToBomDerived()
	 * Commented out 29/8/2021 because the method was moved form RollerBlind.java to MadeToMeasureProduct.java (superclass)
	 *//*
	@Override
	public boolean addMtmInstancePartsToBomDerived() {
		super.addMtmInstancePartsToBomDerived();
		return false;
	}*/
	
	private void adjustBomQty() {
		MBLDBomDerived[] bomDerivedLines = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), mtom_item_line_id);
		for (MBLDBomDerived lines : bomDerivedLines)
		{
			int parentBomID = lines.getMBOMProductID();
			
			MProductBOM mBomItem = new MProductBOM(Env.getCtx(), parentBomID, null);
			int mProductID = mBomItem.getM_ProductBOM_ID();
			X_M_PartType mPartType = new X_M_PartType(Env.getCtx(), mBomItem.getM_PartType_ID(), null);
			String partType = mPartType.getName();
	
			log.warning("partType: " + partType.toString());
			//if it's a side channel or channel part, get the drop less drop deduction plus waste and set as BOM derived qty.
			if(!(partType == null) && partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL) 
					|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL_COVER)
					|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX_BACK) 
					|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX))
			{
				lines.setQty(getBomQty(mProductID,partType));
				lines.saveEx();
			}
		
		}
	}
	
	/**
	 * @override
	 */
	public boolean setAutoSelectedPartIds() {
		super.setAutoSelectedPartIds();
		adjustBomQty();
		return true;
	}
	
	private BigDecimal getBomQty(int bomItemProductID, String partType) {
		{
			//get qty and adjust bomDerived line
			BigDecimal qty = Env.ZERO;
			BigDecimal waste = Env.ZERO;
			MProduct parentProduct = MProduct.get(Env.getCtx(), m_product_id);
			int parentBomID = getParentBOMLineID(bomItemProductID);
			MProductBOM mBomItem = new MProductBOM(Env.getCtx(), parentBomID, null);
			MProduct bomProduct = MProduct.get(Env.getCtx(),mBomItem.getM_ProductBOM_ID());
			
			MtmUtils.attributePreCheck("Waste");
			MtmUtils.attributePreCheck(MtmUtils.MTM_OVERALL_DEDUCTION);
			MtmUtils.attributePreCheck(MtmUtils.MTM_DROP_DEDUCTION);
			Object wasteObject = MtmUtils.getMattributeInstanceValue(bomItemProductID, "Waste", null);
			if(wasteObject == null) throw new AdempiereUserError("No waste attribute or value for: " + bomProduct.getName());
			
			int dropDeductionObject = MtmUtils.getDeduction(m_product_id, MtmUtils.MTM_DROP_DEDUCTION);//Parent Drop
			//Make sure there's a parent drop
			if(dropDeductionObject < 0) throw new AdempiereUserError("No waste dropDeduction or value for: " + parentProduct.getName());
			
			if(partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL) 
					|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL_COVER))
			{
				int parentDropDeduction = MtmUtils.getDeduction(m_product_id, MtmUtils.MTM_DROP_DEDUCTION);//Parent Drop
				if(partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL_COVER))
					{
						Object dropDeductionAdjustObject = MtmUtils.getMattributeInstanceValue(bomItemProductID, MtmUtils.MTM_DROP_DEDUCTION_ADJUST, null);
						if(dropDeductionAdjustObject == null) throw new AdempiereUserError("No waste dropDeductionAdjust or value for: " + bomProduct.getName());
					}
				//Drop adjust is -ve
				int dropAdjust = MtmUtils.getDeduction(bomItemProductID, MtmUtils.MTM_DROP_DEDUCTION_ADJUST);
				log.warning("-------getBomQty drop Deduction Adjust: " + dropAdjust);
				int totalDed = parentDropDeduction + dropAdjust;
				qty = new BigDecimal(high - totalDed);
			}
			
			if(partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX_BACK)
			|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX))
			{
				int overallDeduction = MtmUtils.getDeduction(m_product_id, MtmUtils.MTM_OVERALL_DEDUCTION);
				if(overallDeduction == -1) log.warning("Overall deduction is -1, check the Overall deduction attribute in the parent product");
				qty = new BigDecimal(wide - overallDeduction);
			}
			return qty.add(qty.multiply(waste.divide(oneHundred)));	
		}
	}//getBomQty
	
	/**
	 * @override
	 */
	public BigDecimal getFabricWidth() {
		StringBuilder sql1 = new StringBuilder("SELECT mpc.name FROM m_product_category mpc ");
		sql1.append("JOIN m_product mp ON mp.m_product_category_id = mpc.m_product_category_id ");
		sql1.append("WHERE mp.m_product_id = ?");
		String productCategory = DB.getSQLValueString(null, sql1.toString(), m_product_id);
		if (productCategory.equals(SIDE_RETAINED_BLIND))
		{
			//For Alpha SRS, the fabric is a width deduction and doesn't vary with different controls etc.
			BigDecimal fabWidth;// = getRollerTubeCut(wide);
			log.warning("-------About to go intoMtmUtils.attributePreCheck()");
			MtmUtils.attributePreCheck(MtmUtils.MTM_FABRIC_DEDUCTION);
			log.warning("About to call static method MBLDMtomCuts.getDeduction with deduction type: " + MtmUtils.MTM_FABRIC_DEDUCTION);
			fabWidth = new BigDecimal(wide).subtract(MBLDMtomCuts.getDeduction(m_product_id, MtmUtils.MTM_FABRIC_DEDUCTION,trxName));
			return fabWidth;
		}
		else
		{
			return super.getFabricWidth();//just treat as normal roller blind for everything else.
		}
		
	 }//getFabricWidth
	
	/**
	 * @override
	 */ 
	public BigDecimal getBottomBarCut() {
		return super.getBottomBarCut();
		
		/* BigDecimal bbWidth = new BigDecimal(wide);
		 log.warning("-------About to go intoMtmUtils.attributePreCheck()");
		 MtmUtils.attributePreCheck(MtmUtils.MTM_BOTTOM_BAR_DEDUCTION);
		 //For Alpha SRS, the bottom doesn't vary with different controls etc.
		 log.warning("About to call static method MBLDMtomCuts.getDeduction with deduction type: " + MtmUtils.MTM_BOTTOM_BAR_DEDUCTION);
		 bbWidth = bbWidth.subtract(MBLDMtomCuts.getDeduction(m_product_id, MtmUtils.MTM_BOTTOM_BAR_DEDUCTION, trxName));
		 return bbWidth;
		 */
	 }//getBottomBarCut

}
