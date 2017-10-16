/**
 * 
 */
package au.blindmot.mtmbtn;

import java.util.ArrayList;
import java.util.Properties;

import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.action.IAction;
import org.adempiere.webui.adwindow.ADWindow;
import org.adempiere.webui.adwindow.ADWindowContent;
import org.adempiere.webui.adwindow.AbstractADWindowContent;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.GridTab;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Vbox;


/**
 * @author phil
 *
 */
public class MtmButtonAction implements IAction, EventListener<Event> {

	private static CLogger log = CLogger.getCLogger(MtmButtonAction.class);
	private AbstractADWindowContent panel;
	private ConfirmPanel 	confirmPanel = new ConfirmPanel(true);
	private GridTab 		tab = null;
	private int m_AD_Window_ID;
	private int m_Tab_id;
	private Listbox fabFamily = new Listbox();
	private Listbox fabColour = new Listbox();
	private Listbox fabType = new Listbox();
	private Listbox chainMaterial = new Listbox();
	private Listbox chainLength = new Listbox();
	private Window 	blindConfig = null;
	private String fabFamilySelected = null;
	private String fabColourSelected = null;
	private String fabTypeSelected = null;
	private int currentSelection = 0;
	
	
	/**
	 * <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
	 */
	public MtmButtonAction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(Object target) {
		// TODO Auto-generated method stub
		
		ADWindow window = (ADWindow)target;
		ADWindowContent content = window.getADWindowContent();
		tab = content.getActiveGridTab();
		m_AD_Window_ID = tab.getAD_Window_ID();
		panel = content;
		
		log.info("MtmButtonAction window title: " + window.getTitle());
		m_AD_Window_ID = tab.getAD_Window_ID();
		m_Tab_id = tab.getAD_Tab_ID();
		
		//Test to see if the current record is a mtm product.
		//Get the c_orderline_id, find the product_id, check if the product_id is a mtm product
		
		String c_Order_line = Env.parseContext(Env.getCtx(), tab.getWindowNo(), "@C_OrderLine_ID@", true);
		if (c_Order_line ==""){
			FDialog.warn(tab.getWindowNo(), "There's no order line to specify options for.", "Warning");
		}
			else if(c_Order_line != "")
			{
				StringBuilder sql = new StringBuilder("SELECT m_product_id FROM c_orderline WHERE c_orderline_id = ");
				sql.append(c_Order_line);
				int m_Product = DB.getSQLValue(null, sql.toString());
				
				StringBuilder sql_mtm = new StringBuilder("SELECT ismadetomeasure FROM m_product WHERE m_product_id = ?");
				String isMtm = DB.getSQLValueString(null, sql_mtm.toString(), m_Product);
				if (!isMtm.equals("Y"))
				{
					FDialog.warn(tab.getWindowNo(), "There's no made to measure product to specify options for.", "Warning");
				}
				else show();
			}
		
		
		System.out.println("In execute of MtmButtonAction" + " tab ID: " + m_Tab_id + " record ID: " + tab.getRecord_ID());
		System.out.println(Env.getCtx().toString());
		System.out.println("Attempting to parse c_order_line: " + Env.parseContext(Env.getCtx(), tab.getWindowNo(), "@C_OrderLine_ID@", true));
		/*If this returns "" then it fails the check. Should return 'M_Product_ID =1000010' or similar.
		 * Possibly should check for just roller blind?
		 * Maybe create a new category?
		 *
		 */
		
		
	}
	
	public void show(){
		
		fabColour.getItems().clear();
		fabColour.setEnabled(false);
		fabType.getItems().clear();
		fabType.setEnabled(false);
		chainMaterial.getItems().clear();
		chainMaterial.setEnabled(false);//TODO: Set enabled(true) if the item is chain controlled.
		chainLength.getItems().clear();
		fabFamily.setMold("select");
		fabFamily.getItems().clear();
		initFabFam();
		
		if (blindConfig != null)blindConfig=null;
		{	
		blindConfig = new Window();
		ZKUpdateUtil.setWidth(blindConfig, "650px");
		blindConfig.setClosable(true);
		blindConfig.setBorder("normal");
		blindConfig.setStyle("position:absolute");
		blindConfig.addEventListener("onValidate", this);

		Vbox vb = new Vbox();
		ZKUpdateUtil.setWidth(vb, "100%");
		blindConfig.appendChild(vb);
		blindConfig.setSclass("toolbar-popup-window");
		vb.setSclass("toolbar-popup-window-cnt");
		vb.setAlign("stretch");
		
		Grid grid = GridFactory.newGridLayout();
		vb.appendChild(grid);
        
        Columns columns = new Columns();
        Column column = new Column();
        ZKUpdateUtil.setHflex(column, "min");
        columns.appendChild(column);
        column = new Column();
        ZKUpdateUtil.setHflex(column, "1");
        columns.appendChild(column);
        grid.appendChild(columns);
        
        Rows rows = new Rows();
		grid.appendChild(rows);
			
		
		//Add the fabFam list
		Row row = new Row();
		rows.appendChild(row);
		row.appendChild(new Label("Fabric Family:")); //This translate() method is pretty cool. Such a big codebase to work with.
		row.appendChild(fabFamily);
		ZKUpdateUtil.setHflex(fabFamily, "1");
		fabFamily.addEventListener(Events.ON_SELECT, this);
		
		//Add the fabColour Listbox
		Row row1 = new Row();
		rows.appendChild(row1);
		row1.appendChild(new Label("Fabric Colour:")); 
		row1.appendChild(fabColour);
		ZKUpdateUtil.setHflex(fabColour, "1");
		if(fabFamilySelected == null)fabColour.setVisible(false);//Don't show this box until a fabric family is selected.
		fabColour.addEventListener(Events.ON_SELECT, this);
		
		//Add the fabType Listbox
		Row row2 = new Row();
		rows.appendChild(row2);
		row2.appendChild(new Label("Fabric Type:")); 
		row2.appendChild(fabType);
		ZKUpdateUtil.setHflex(fabType, "1");
		if(fabColourSelected == null)fabType.setVisible(false);//Don't show this Listbox until a fabric colour is selected. 
		
		//Add confirm panel
		vb.appendChild(confirmPanel);
		LayoutUtils.addSclass("dialog-footer", confirmPanel);
		confirmPanel.addActionListener(this);
		
		
		LayoutUtils.openPopupWindow(panel.getToolbar().getButton("mtmb"), blindConfig, "after_start");
		
		}
	}
	
	private void loadFabColour(String m_Product_id)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT m_product_id, description");
		sql.append(" FROM m_product");
		sql.append(" WHERE name = ");
		sql.append("(SELECT name FROM m_product WHERE m_product_id = ");
		sql.append(m_Product_id);
		sql.append(")");
		
		fabColour.setMold("select");
		ArrayList<String> dupCheck = new ArrayList<String>();
		KeyNamePair[] keyNamePairs = DB.getKeyNamePairs(sql.toString(), true);
		for (KeyNamePair pair : keyNamePairs) {
			
					//Remove duplicfabColourSelectedates
					if (!dupCheck.contains(pair.getName())) fabColour.appendItem(pair.getName(), pair.getID());
					dupCheck.add(pair.getName());
			
		}
	}
	
	
	private void initFabFam() {
		
		   //Get the values for the fabric list box.
		
		String sql = "SELECT m_product_id, name"
				+ " FROM m_product as mp"
				+ " WHERE mp.m_parttype_id ="
				+ " (SELECT m_parttype_id FROM m_parttype WHERE m_parttype.name = 'Fabric')";
				
		fabFamily.setMold("select");
			KeyNamePair[] keyNamePairs = DB.getKeyNamePairs(sql, true);
			ArrayList<String> dupCheck = new ArrayList<String>();
			for (KeyNamePair pair : keyNamePairs) {
				
					//Remove duplicates
					if (!dupCheck.contains(pair.getName())) fabFamily.appendItem(pair.getName(), pair.getID());
					dupCheck.add(pair.getName());
				}
		
			}
	
	
	private void loadFabType(String selectedColour)//Is this the correct parameter to pass?
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT mp.m_product_id, ma.value ");
		sql.append("FROM m_product mp ");
		sql.append("INNER JOIN m_attributeinstance ma ");
		sql.append("ON mp.m_attributesetinstance_id = ma.m_attributesetinstance_id ");
		sql.append("AND mp.name = (SELECT name FROM m_product WHERE m_product_id = ");
		sql.append(fabFamilySelected);
		sql.append(") ");
		sql.append("AND mp.description = (SELECT description FROM m_product WHERE m_product_id = ");
		sql.append(selectedColour);
		sql.append(")");
		
		fabType.setMold("select");
		KeyNamePair[] keyNamePairs = DB.getKeyNamePairs(sql.toString(), true);
		for (KeyNamePair pair : keyNamePairs) {
			fabType.appendItem(pair.getName(), pair.getID());
		}
		int fabTypeItemCount = fabType.getItemCount();
		if(fabTypeItemCount <3)fabType.setEnabled(false);//If only one item, select it and set box unable
			if (fabTypeItemCount >1 && fabTypeItemCount <3)fabType.setSelectedIndex(1);
			if (fabTypeItemCount <=1)FDialog.warn(tab.getWindowNo(), "Fabric type not determined, check product setup.", "Warning");
				
			
			
		
		/*TODO: Check current item to see it it's chain controlled. If it is, add a Listbox with chain products.
		 * 
		 *
		 */
	}

	private void validate() throws Exception
	{
		//Validate 
	}
	
	@Override
	public void onEvent(Event event) throws Exception {
		
		if(event.getTarget().getId().equals(ConfirmPanel.A_CANCEL))
			blindConfig.onClose();
		
		else if(event.getTarget().getId().equals(ConfirmPanel.A_OK)) {
			
			blindConfig.setVisible(false);
			Clients.showBusy(Msg.getMsg(Env.getCtx(), "Processing"));
			Events.echoEvent("onValidate", blindConfig, null);
		}
		
		else if (event.getTarget() == fabFamily)
		{
			fabFamilySelected = null;
			ListItem li = fabFamily.getSelectedItem();
			fabFamilySelected = li.getValue();
	
			
			
			//Enable and populate the fabColour Listbox
			fabColour.removeAllItems();
			fabType.removeAllItems();
			fabType.setEnabled(false);
			fabColour.setEnabled(true);
			fabColour.setVisible(true);
			loadFabColour(fabFamilySelected);
			
		}
		
		else if(event.getTarget() == fabColour)
		{
			//Enable and populate the fabColour Listbox
			fabColourSelected = null;
			ListItem li = fabColour.getSelectedItem();
			fabColourSelected = li.getValue();
			
			fabType.removeAllItems();
			fabType.setEnabled(true);
			fabType.setVisible(true);
			loadFabType(fabColourSelected);
		}
		
		else if(event.getTarget() == fabType)
		{
		
			fabTypeSelected = null;
			ListItem li = fabType.getSelectedItem();
			fabTypeSelected = li.getValue();
			
			setCurrentSelection(fabTypeSelected);
		}
		
		else if (event.getName().equals("onValidate")) 
		{
			try {
				validate();
			} finally {
				Clients.clearBusy();
				panel.getComponent().invalidate();
			}
		
		/*TODO: Get the current order line/production item PK. When ONCLose is called:
		 * *Create an instance attribute object - get c_orderline.mattributeinstance_id and create the object.
		 * Try appending on to the 'description' field?
		 */
	}

	}
	
	public int getCurrentselection()
	{
		return 1;//TODO: Change to something that makes sense.
		
	}
	
	private final void setCurrentSelection(String lastFabricType)
	{
		currentSelection = Integer.parseInt(lastFabricType);
	}
	

}

