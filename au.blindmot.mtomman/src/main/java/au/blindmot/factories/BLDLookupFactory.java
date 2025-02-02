package au.blindmot.factories;

import org.adempiere.base.ILookupFactory;
import org.compiere.model.GridFieldVO;
import org.compiere.model.InfoColumnVO;
import org.compiere.model.Lookup;
import org.compiere.util.CLogger;

import au.blindmot.editor.BldLookup;
import au.blindmot.editor.BldProdLookup;

public class BLDLookupFactory implements ILookupFactory {
	
	CLogger log = CLogger.getCLogger(BLDLookupFactory.class);
	public Lookup getLookup(GridFieldVO gridFieldVO) {
		if(gridFieldVO.displayType == BLDDisplayTypeFactory.BldMtmParts) {
			log.warning("--------BLD lookup loaded");
			return new BldLookup(gridFieldVO.ctx, gridFieldVO.WindowNo);
		}
		
		if(gridFieldVO.displayType == BLDDisplayTypeFactory.BldMtmProduct) {
			log.warning("--------BLD lookup loaded");
			return new BldProdLookup(gridFieldVO.ctx, gridFieldVO.WindowNo);
		}
				
		return null;
	}

	public boolean isLookup(GridFieldVO gridFieldVO) {
		if (gridFieldVO.displayType == BLDDisplayTypeFactory.BldMtmParts)
		{
			return true;
		}
		if (gridFieldVO.displayType == BLDDisplayTypeFactory.BldMtmProduct)
		{
			return true;
		}
			
		return false;
	}

	@Override
	public boolean isLookup(InfoColumnVO infoColumnVO) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	

}

