package au.blindmot.factories;

import org.adempiere.webui.factory.IFormFactory;
import org.adempiere.webui.panel.ADForm;
import org.zkoss.util.logging.Log;

import au.blindmot.forms.BLDBarcodeLookup;

public class BLDBarcodeLookupFactory implements IFormFactory{

	@Override
	public ADForm newFormInstance(String formName) {
		if(formName.contains("BLDBarcodeLookup"))
			return new BLDBarcodeLookup();
		
		return null;
	}

}
