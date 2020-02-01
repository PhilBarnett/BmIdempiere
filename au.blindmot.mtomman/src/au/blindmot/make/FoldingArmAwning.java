/**
 * 
 */
package au.blindmot.make;

import java.util.List;

/**
 * @author phil
 *
 */
public class FoldingArmAwning extends MadeToMeasureProduct {

	/**
	 * @param mProduct_ID
	 * @param mtom_item_line_id
	 * @param trxnName
	 */
	public FoldingArmAwning(int mProduct_ID, int mtom_item_line_id, String trxnName) {
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

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#getCuts()
	 */
	@Override
	public boolean getCuts() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#createBomDerived()
	 */
	@Override
	public boolean createBomDerived() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#deleteBomDerived()
	 */
	@Override
	public boolean deleteBomDerived() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#deleteCuts()
	 */
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
	 * @see au.blindmot.make.MadeToMeasureProduct#interpretMattributeSetInstance()
	 */
	@Override
	public void interpretMattributeSetInstance() {
		// TODO Auto-generated method stub

	}

}
