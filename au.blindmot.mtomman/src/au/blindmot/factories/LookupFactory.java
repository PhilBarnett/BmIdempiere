package au.blindmot.factories;

import org.adempiere.base.ILookupFactory;
import org.compiere.model.GridFieldVO;
import org.compiere.model.Lookup;
import org.compiere.util.CLogger;

import au.blindmot.editor.BldLookup;

public class LookupFactory implements ILookupFactory {
	
	CLogger log = CLogger.getCLogger(LookupFactory.class);
	@Override
	public Lookup getLookup(GridFieldVO gridFieldVO) {
		if(gridFieldVO.displayType == DisplayTypeFactory.BldMtmParts) {
			log.warning("--------BLD lookup loaded");
			return new BldLookup(gridFieldVO.ctx, gridFieldVO.WindowNo);
		}
			
			
		return null;
	}

	@Override
	public boolean isLookup(GridFieldVO gridFieldVO) {
		if (gridFieldVO.displayType == DisplayTypeFactory.BldMtmParts)return true;
		return false;
	}
	
	
	
	

}

