package au.blindmot.forms;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.panel.ADForm;
import org.compiere.model.MQuery;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;


import au.blindmot.model.I_BLD_mtom_cuts;
import au.blindmot.model.I_BLD_Mtom_Item_Line;
import au.blindmot.model.I_BLD_mtom_production;
import au.blindmot.utils.MtmBarcode;

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
		final MtmBarcode parsed;
		try {
			parsed = MtmBarcode.parse(barcodeText);
		} catch (IllegalArgumentException ex) {
			throw new AdempiereUserError(ex.getMessage());
		}
		String id = Integer.toString(parsed.getRecordId());
		switch (parsed.getTarget()) {
		case PRODUCTION:
			windowFromBarcode(I_BLD_mtom_production.Table_ID, id);
			break;
		case ITEM:
			windowFromBarcode(I_BLD_Mtom_Item_Line.Table_ID, id);
			break;
		case CUT:
			windowFromBarcode(I_BLD_mtom_cuts.Table_ID, id);
			break;
		}
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
