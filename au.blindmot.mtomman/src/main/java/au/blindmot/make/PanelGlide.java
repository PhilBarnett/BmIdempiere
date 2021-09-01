package au.blindmot.make;

import java.math.BigDecimal;
import java.util.List;

public class PanelGlide extends MadeToMeasureProduct {
	

	/**
	 * 
	 * @param product_id
	 * @param bld_mtom_item_line_id
	 */
	public PanelGlide (int product_id, int bld_mtom_item_line_id, String trxnName) {
		super(product_id, bld_mtom_item_line_id, trxnName);
	}

	@Override
	public boolean getCuts() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createBomDerived() {
		return false;
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void interpretMattributeSetInstance() {
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
	public boolean addTriggeredBom(int parentBomID, int qty) {
		super.addTriggeredBom(parentBomID, qty);
		return false;
	}

	@Override
	public BigDecimal getBomQty(int addID) {
		// TODO Auto-generated method stub
		return null;
	}

}
