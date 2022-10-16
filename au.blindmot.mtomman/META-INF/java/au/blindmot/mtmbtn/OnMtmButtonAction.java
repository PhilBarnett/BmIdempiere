/**
 * 
 */
package au.blindmot.mtmbtn;

import org.adempiere.webui.action.IAction;
import org.adempiere.webui.adwindow.ADWindow;
import org.adempiere.webui.adwindow.ADWindowContent;
import org.adempiere.webui.adwindow.AbstractADWindowContent;
import org.compiere.model.GridTab;
import org.compiere.util.CLogger;


/**
 * @author phil
 *
 */
public class OnMtmButtonAction implements IAction {

	private static CLogger log = CLogger.getCLogger(OnMtmButtonAction.class);
	private AbstractADWindowContent panel;
	private GridTab 		tab = null;
	
	
	/**
	 * <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
	 */
	public OnMtmButtonAction() {
		
	}


	public void execute(Object target) {
		log.warning("----------In OnMtmButtonAction.execute()");
		ADWindow window = (ADWindow)target;
		ADWindowContent content = window.getADWindowContent();
		tab = content.getActiveGridTab();
		tab.getAD_Window_ID();
		panel = content;
		MtmButtonActionWindow mtmButtonActionWindow = new MtmButtonActionWindow(panel, window);
		
		mtmButtonActionWindow.show(); 
	}
}
		
		


	