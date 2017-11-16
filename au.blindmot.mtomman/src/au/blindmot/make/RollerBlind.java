package au.blindmot.make;

import org.compiere.model.MAttributeSetInstance;
import org.compiere.util.KeyNamePair;

public class RollerBlind extends MadeToMeasureProduct {

	@Override
	public void interpretMattributeSetInstance(MAttributeSetInstance mAttribute) {
		// TODO Auto-generated method stub

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
	public boolean createBomDerived() {
		return false;
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean createProductionLine() {
		return false;
		// TODO Auto-generated method stub
		
	}

}
