package au.blindmot.forms;

import org.adempiere.webui.adwindow.ADWindow;
import org.adempiere.webui.adwindow.ADWindowContent;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.GridTab;
import org.compiere.model.MQuery;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

import au.blindmot.model.I_BLD_mtom_cuts;
import au.blindmot.model.I_BLD_mtom_item_line;
import au.blindmot.model.I_BLD_mtom_production;
import au.blindmot.utils.MtmUtils;

public class BLDBarcodeLookup extends ADForm {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2230808158306993766L;
	Textbox barcode = new Textbox();
	Label label = new Label("Barcode: ");
	//021001151

	@Override
	protected void initForm() {
		this.appendChild(label);	
		this.appendChild(barcode);
		barcode.addEventListener(Events.ON_OK, this);
	}

	public void onEvent(Event event) throws Exception {
		if(event.getTarget().equals(barcode) && event.getName().equals(Events.ON_OK))
		{
			proccessBarcode(barcode.getValue());
		}
	}
	
	public void proccessBarcode(String barcodeText) {
		String prefix = barcodeText.substring(0, 2);
		String id = barcodeText.substring(2);
		if(id.length() != 7)
		{
			throw new AdempiereUserError("Barcode length incorrect.", "Is this is a Made to measure barcode?");
		}
		if(prefix.equalsIgnoreCase(MtmUtils.MTM_PRODUCTION_PREFIX))
		{
			windowFromBarcode(I_BLD_mtom_production.Table_ID,id);
		}
		else if(prefix.equalsIgnoreCase(MtmUtils.MTM_PRODUCTION_ITEM_PREFIX))
		{
			windowFromBarcode(I_BLD_mtom_item_line.Table_ID, id);
		}
		
		else if(prefix.equalsIgnoreCase(MtmUtils.MTM_PRODUCTION_ASSEMBLEY_ITEM))
		{
			windowFromBarcode(I_BLD_mtom_cuts.Table_ID, id);
		}
		
		else FDialog.warn(getWindowNo(), "Barcode not recognised.", "Warning");
		//TODO:More error handling- check length etc.
	}
		
	public void windowFromBarcode(int tableID, String id) {
		 
		int winID = getWindowID(tableID);
		String table = getTableName(tableID);
		StringBuilder whereString = new StringBuilder(table);
		whereString.append("_ID IN (");
		whereString.append(id);
		whereString.append(")");
		final MQuery query = new MQuery(table); 
		 query.addRestriction(whereString.toString());
		 query.setZoomTableName(table);
		 query.setZoomColumnName(table + "_ID");
		 query.setZoomValue(Integer.parseInt(id));
		 AEnv.showZoomWindow(winID, query);
		
	}
		
	public int getWindowID(int tableID) {
		StringBuilder sql = new StringBuilder("SELECT at.ad_window_id ");
		sql.append("FROM ad_tab at ");
		sql.append("JOIN ad_window aw ON aw.ad_window_id = at.ad_window_id ");
		sql.append("WHERE at.ad_table_id = " + tableID);
		int WinID = 0;
		WinID = DB.getSQLValue(null, sql.toString());
		return WinID;
		
	}
	
	public String getTableName(int tableID) {
		StringBuilder sql = new StringBuilder("SELECT at.tablename ");
		sql.append("FROM ad_table at ");
		sql.append("WHERE at.ad_table_id = ?");
		String tableName = DB.getSQLValueString(null, sql.toString(), tableID);
		return tableName;
	}
		


}
