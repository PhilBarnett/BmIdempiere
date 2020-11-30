package au.blindmot.factories;

import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.factory.IEditorFactory;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.util.CLogger;

import au.blindmot.editor.WBldMtmPartsEditor;
import au.blindmot.editor.WNonSelectProductEditor;

public class BLDEditorFactory implements IEditorFactory {
	
	CLogger log = CLogger.getCLogger(BLDEditorFactory.class);

	public WEditor getEditor(GridTab gridTab, GridField gridField, boolean tableEditor) {
		if (gridField == null)
        {
            return null;
        }

        WEditor editor = null;
        int displayType = gridField.getDisplayType();

        if(displayType == BLDDisplayTypeFactory.BldMtmParts){
	        log.warning("-------MY CUSTOM BldMtmParts DISPLAYTYPE");
	        editor = new WBldMtmPartsEditor(gridTab, gridField);
        }
        
        if(displayType == BLDDisplayTypeFactory.BldMtmProduct){
	        log.warning("-------MY CUSTOM BldMtmParts DISPLAYTYPE");
	        editor = new WNonSelectProductEditor(gridField, gridTab);
        }
        if(editor != null)
	        editor.setTableEditor(tableEditor);

        return editor;
	}

}
