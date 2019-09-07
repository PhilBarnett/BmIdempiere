package au.blindmot.make;

import java.math.BigDecimal;
import java.util.List;

import org.compiere.model.MBOMProduct;
import org.compiere.model.MProduct;
import org.compiere.model.MProductBOM;
import org.compiere.model.X_M_PartType;
import org.compiere.util.Env;

import au.blindmot.utils.MtmUtils;

public class AwningBlind extends RollerBlind {

	/**
	 * 
	 * @param product_id
	 * @param bld_mtom_item_line_id
	 */
	public AwningBlind (int product_id, int bld_mtom_item_line_id, String trxnName) {
		super(product_id, bld_mtom_item_line_id, trxnName);
	}
	
	public static String MTM_SIDE_CHANNEL_PARTTYPE = "Side Channel";
	
	@Override
	public void interpretMattributeSetInstance() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getCuts() {
		return false;
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean createBomDerived() {
		return false;
		// TODO Auto-generated method stub
		
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

	@Override
	public List<String> getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#setAutoSelectedPartIds()
	 */
	@Override
	public boolean setAutoSelectedPartIds() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#addMtmInstancePartsToBomDerived()
	 */
	@Override
	public boolean addMtmInstancePartsToBomDerived() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#addTriggeredBom(int)
	 */
	@Override
	public boolean addTriggeredBom(int parentBomID) {
		MProductBOM mBomItem = new MProductBOM(Env.getCtx(), parentBomID, null);
		int mProductID = mBomItem.getM_Product_ID();
		MProduct bomProduct = MProduct.get(Env.getCtx(), mProductID);
		X_M_PartType mPartType = new X_M_PartType(Env.getCtx(), bomProduct.getM_PartType_ID(), null);
		String partType = mPartType.getName();
		BigDecimal qty;

		//if it's a side channel or channel part, get the drop less drop deduction plus waste and set as BOM derived qty.
		if(partType.equalsIgnoreCase(MTM_SIDE_CHANNEL_PARTTYPE))
		{
			int dropDeduction = MtmUtils.getDeduction(mProductID, MtmUtils.MTM_DROP_DEDUCTION);
			BigDecimal waste = (BigDecimal) MtmUtils.getMattributeInstanceValue(mProductID, "Waste", null);
			qty = new BigDecimal(high - dropDeduction).multiply(waste.multiply(oneHundred)); 
			addMBLDBomDerived(mProductID, qty, "Added by BOM trigger");
			//Do stuff
		}
		else //If it's something we don't specifically care about then add using generic Superclass method
		{
			qty = mBomItem.getBOMQty();
			addMBLDBomDerived(mProductID, qty, "Added by BOM trigger");
		}
		
		return true;
	}

}
