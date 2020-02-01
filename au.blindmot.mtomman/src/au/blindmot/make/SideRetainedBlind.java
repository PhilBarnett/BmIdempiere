/**
 * 
 */
package au.blindmot.make;

import java.math.BigDecimal;
import java.util.List;

import org.compiere.model.MProduct;
import org.compiere.model.MProductBOM;
import org.compiere.model.X_M_PartType;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.Env;

import au.blindmot.model.MBLDBomDerived;
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
	

	/**
	 * @param mProduct_ID
	 * @param mtom_item_line_id
	 * @param trxnName
	 */
	public SideRetainedBlind(int mProduct_ID, int mtom_item_line_id, String trxnName) {
		super(mProduct_ID, mtom_item_line_id, trxnName);
	}

	/* (non-Javadoc)
	 * @see au.blindmot.make.MtmInfo#getConfig()
	 */
	@Override
	public List<String> getConfig() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void interpretMattributeSetInstance() {
	super.interpretMattributeSetInstance();

	}

	@Override
	public boolean getCuts() {
		super.getCuts();
		MBLDBomDerived[] bomDerivedLines = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), mtom_item_line_id);
		
		//Get the overall drop deduction from parent 
		int parentDropDeduction = MtmUtils.getDeduction(m_product_id, MtmUtils.MTM_DROP_DEDUCTION);
		
		//Loop through BOMderived.
		for (MBLDBomDerived lines : bomDerivedLines)
		{
			int bomLineProductID = lines.getM_Product_ID();
			String mPartType = MtmUtils.getMPartype(bomLineProductID);
			if(mPartType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL) 
					|| mPartType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL_COVER))//Find 'Awning channel' part types.
				{
					//Get MTM_DROP_DEDUCTION_ADJUST
					int dropAdjust = MtmUtils.getDeduction(bomLineProductID, MtmUtils.MTM_DROP_DEDUCTION_ADJUST);
					int totalDeduction = parentDropDeduction + dropAdjust;
					int cut = high - totalDeduction;
					addBldMtomCuts(bomLineProductID, 0, cut, 0);
				}
			
			if(mPartType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX_BACK)
					|| mPartType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX))
				{
					//Get MTM headbox overall deduction
					int overallDeduction = MtmUtils.getDeduction(m_product_id, MtmUtils.MTM_OVERALL_DEDUCTION);
					int cut = wide - overallDeduction;
					addBldMtomCuts(bomLineProductID, 0, cut, 0);
				}
		}
		return true;
	}

	

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
	public boolean addTriggeredBom(int parentBomID) {
		MProductBOM mBomItem = new MProductBOM(Env.getCtx(), parentBomID, null);
		int mProductID = mBomItem.getM_ProductBOM_ID();
		//MProduct bomProduct = MProduct.get(Env.getCtx(),mBomItem .getM_ProductBOM_ID());
		X_M_PartType mPartType = new X_M_PartType(Env.getCtx(), mBomItem.getM_PartType_ID(), null);
		String partType = mPartType.getName();
		BigDecimal qty;

		//if it's a side channel, headbox or channel part, get the drop less drop deduction plus waste and set as BOM derived qty.
		if(partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL) 
				|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL_COVER)
				|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX_BACK)
				|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_HEADBOX))
		{
			BigDecimal bomDerivedQty = getBomQty(mProductID, partType);
			addMBLDBomDerived(mProductID, bomDerivedQty, "Added by BOM trigger");
		}
		else //If it's something we don't specifically care about then add using generic Superclass method
		{
			qty = mBomItem.getBOMQty();
			addMBLDBomDerived(mProductID, qty, "Added by BOM trigger");
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
	 */
	@Override
	public boolean addMtmInstancePartsToBomDerived() {
		super.addMtmInstancePartsToBomDerived();
		return false;
	}
	
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
			int parentBomID = getParentBOMLineID(bomItemProductID);
			MProductBOM mBomItem = new MProductBOM(Env.getCtx(), parentBomID, null);
			MProduct bomProduct = MProduct.get(Env.getCtx(),mBomItem.getM_ProductBOM_ID());
			
			MtmUtils.attributePreCheck("Waste");
			MtmUtils.attributePreCheck(MtmUtils.MTM_OVERALL_DEDUCTION);
			Object wasteObject = MtmUtils.getMattributeInstanceValue(bomItemProductID, "Waste", null);
			if(wasteObject == null) throw new AdempiereUserError("No waste attribute or value for: " + bomProduct.getName());
			
			
			if(partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL) 
					|| partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE_AWNING_CHANNEL_COVER))
			{
				
				int parentDropDeduction = MtmUtils.getDeduction(m_product_id, MtmUtils.MTM_DROP_DEDUCTION);
				int dropAdjust = MtmUtils.getDeduction(bomItemProductID, MtmUtils.MTM_DROP_DEDUCTION_ADJUST);
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
	}
	

}
