package au.blindmot.make;

import java.util.List;

public class AwningBlind extends MadeToMeasureProduct {

	/**
	 * 
	 * @param product_id
	 * @param bld_mtom_item_line_id
	 */
	public AwningBlind (int product_id, int bld_mtom_item_line_id, String trxnName) {
		super(product_id, bld_mtom_item_line_id, trxnName);
	}
	
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

}
