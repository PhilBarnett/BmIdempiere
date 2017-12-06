package au.blindmot.make;

import org.compiere.model.MAttributeSetInstance;

public class RollerBlind extends MadeToMeasureProduct {

	public RollerBlind (int product_id, int bld_mtom_item_line_id) {
		super(product_id, bld_mtom_item_line_id);
	}
	
	
	@Override
	public void interpretMattributeSetInstance(MAttributeSetInstance mAttribute) {
		// TODO Can this method be removed from Super class and here?

	}
	

	@Override
	public boolean getCuts() {
		return false;
		/*
		 * TODO: Check that KeyNamePair is the right thing to return?
		 * Perhaps a 2 dimensional array would be better? Would need to change here and in WindowFurnishing.java
		 */
	
	
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean createBomDerived() {//This is the first method called when processing
		/*TODO: Get the AttributePair[] containing the control, non control
		 * 
		 */
		AttributePair[] attributePair = getMAttributeSetInstance();
		if(mBLDMtomItemLine != null);//There's part numbers ready to add if this isn't null.
		
		return false;
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean createProductionLine() {
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
	public boolean deleteProductionLine() {
		// TODO Auto-generated method stub
		return false;
	}

}
