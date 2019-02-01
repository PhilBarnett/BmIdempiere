package au.blindmot.forms;

import org.adempiere.webui.desktop.TabbedDesktop;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.session.SessionManager;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

public class BLDBarcodeLookup extends ADForm {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2230808158306993766L;
	Textbox barcode = new Textbox();
	Rows rows = new Rows();
	Row row = new Row();
	Label label = new Label("Barcode: ");
	

	@Override
	protected void initForm() {
		// TODO Auto-generated method stub
		this.appendChild(rows);
		rows.appendChild(row);
		row.appendChild(label);	
		row.appendChild(barcode);
		barcode.addEventListener(Events.ON_CHANGE, this);
	}

	public void onEvent(Event event) throws Exception {
		if(event.getTarget().equals(barcode) && event.getName().equals(Events.ON_CHANGE))
		{
			proccessBarcode(barcode.getValue());
		}
	}
	
	public void proccessBarcode(String barcodeText) {
		String prefix = barcodeText.substring(0, 1);
		
		/*Try someof these
		SessionManager.getAppDesktop().openWindow(windowId, query, callback);
		TabbedDesktop.showZoomWindow(AD_Window_ID, query);
		SessionManager.getAppDesktop().showZoomWindow(AD_Window_ID, query);//This one best option?
		*/
	}
	//https://groups.google.com/forum/#!topic/idempiere/OaU0JpRsbn4
	/*
	 * private boolean openPurchaseOrders(){
  /** AD_Window_ID of purchase order window 
  final int PURCHASE_ORDER_WINDOW_ID = 181;
  /** filter the data - needs to be generated for real use...
  String whereString = " C_Order_ID IN (1000000, 1000001)"; 
  final AWindow poFrame = new AWindow(); 
  final MQuery query = new MQuery("C_Order"); 
  query.addRestriction(whereString); 
  final boolean ok = poFrame.initWindow(AD_Window_ID, query);  
  if (!ok) { 
     return false; 
  } 
  poFrame.pack(); 
  AEnv.showCenterScreen(poFrame);
  return true; 
  
  */


}
