/**
 * 
 */
package au.blindmot.mtmbtn;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.compiere.model.MOrderLine;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.compiere.util.TrxRunnable;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Vbox;


/**
 * @author phil
 * This should really be done by an extra field in the c_orderline table 'mtmoptions' that stores
 * the options and the product_ids like mattributeinstances eg 'chain=1000012_fabric=1000018'
 * and should be entered and validated by an editor.
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
	private int currentFabSelection = 0;
	private int currentChainSelection = 0;
	private int currOrderLine = 0;
	private boolean isChainDriven = false;
	
	
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
				currOrderLine = Integer.parseInt(c_Order_line);
				StringBuilder sql = new StringBuilder("SELECT m_product_id FROM c_orderline WHERE c_orderline_id = ");
				sql.append(c_Order_line);
				int m_Product = DB.getSQLValue(null, sql.toString());
				
				StringBuilder sql_mtm = new StringBuilder("SELECT ismadetomeasure FROM m_product WHERE m_product_id = ?");
				String isMtm = DB.getSQLValueString(null, sql_mtm.toString(), m_Product);
				if (!isMtm.equals("Y"))
				{
					FDialog.warn(tab.getWindowNo(), "There's no made to measure product to specify options for.", "Warning");
				}
				else 
				{
					//Check if this order line already has attributes assigned.
					MOrderLine thisOrderLine = new MOrderLine(Env.getCtx(), currOrderLine, null);
					
					String patternString = "[0-9]+";
				    Pattern pattern = Pattern.compile(patternString);
					
					Object mtmAttribute = thisOrderLine.get_Value("mtm_attribute");
					
					if(mtmAttribute!=null && mtmAttribute.toString().contains("_"))
					{
						//Split the value of the mtm_attribute column, first value id fabric, second is chain
						String[] products = mtmAttribute.toString().split("_");
						
						
						Matcher matcher = pattern.matcher(products[0]);
					    boolean matches = matcher.matches();
						if(matches)currentFabSelection = Integer.parseInt(products[0]);
						
						Matcher matcher1 = pattern.matcher(products[1]);
					    boolean matches1 = matcher1.matches();
						if(matches1)currentChainSelection = Integer.parseInt(products[1]);
					}
					
					show();
				}
			}
		
		
		System.out.println("In execute of MtmButtonAction" + " tab ID: " + m_Tab_id + " record ebayID: " + tab.getRecord_ID());
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
		row.appendChild(new Label("Fabric Family:"));
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
		fabType.addEventListener(Events.ON_SELECT, this);
		if(fabColourSelected == null)fabType.setVisible(false);//Don't show this Listbox until a fabric colour is selected. 
		
		/*
		 * Add chainFam Listbox
		 * Check if it's chain driven
		 */
		isChainDriven = checkChainDrive();
		if(isChainDriven)
		{
			//Add chainFam Listbox and other required stuff for chain selection
		}
		
		
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
		ListItem item = null;
		for (KeyNamePair pair : keyNamePairs) 
		{	
			if(pair!=null && pair.getID() == Integer.toString(currentFabSelection))fabColour.appendItem(pair.getName(), pair.getID());//Prevents currentFabSelection being missed.
			//Remove duplicfabColourSelectedates
					if (!dupCheck.contains(pair.getName())) fabColour.appendItem(pair.getName(), pair.getID());
					dupCheck.add(pair.getName());
					if(pair.getID()==Integer.toString(currentFabSelection)) item = new ListItem(pair.getName(), pair.getID());//Stores the already selected fabric
		}
		
		if (currentFabSelection !=0)
		{
			fabColour.selectItem(item);//Initializes the list with the value from the DB
		}
		
		
	}
	
	
	private KeyNamePair checkFabric(KeyNamePair[] keyNamePairs)
	{
	if(currentFabSelection !=0)//There's already fabric selected, add it to the list at index 1
	{
		for (KeyNamePair pair : keyNamePairs)
		{
			if(pair.getKey() > 1)
			{
				if(pair.getID().equals(Integer.toString(currentFabSelection)))return pair;
			}
				
			
		}
	}return null;
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
			boolean addCurrent = false;
			int count = 0;
			
			KeyNamePair checkFab = checkFabric(keyNamePairs);
			if (checkFab != null)
				{
				dupCheck.add(checkFab.getName());
				addCurrent = true;
				}
			
			for (KeyNamePair pair : keyNamePairs) 
			{	
				System.out.println("Before check "+ pair.getID());
				if(count == 1 && addCurrent)fabFamily.appendItem(checkFab.getName(), checkFab.getID());//adds the current selection at index 1
				
				if (!dupCheck.contains(pair.getName())) fabFamily.appendItem(pair.getName(), pair.getID());//Remove duplicates
					
					dupCheck.add(pair.getName());
					count++;
			}
			
			if(currentFabSelection != 0)fabFamily.setSelectedIndex(1);//Sets the list with the value from the DB
				
				//TODO: Call loadFabColour(String m_Product_id)
			
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
		ListItem item = null;
		for (KeyNamePair pair : keyNamePairs) 
		{
			fabType.appendItem(pair.getName(), pair.getID());
			if(pair.getID()==Integer.toString(currentFabSelection)) item = new ListItem(pair.getName(), pair.getID());//Stores the already selected fabric
		}
		
		if (currentFabSelection !=0)
		{
			fabType.selectItem(item);//Initializes the list with the value from the DB
		}
		int fabTypeItemCount = fabType.getItemCount();
		if(fabTypeItemCount <3)fabType.setEnabled(false);//If only one item, select it and set box unable
			if (fabTypeItemCount >1 && fabTypeItemCount <3)
			{
				fabType.setSelectedIndex(1);
				fabTypeSelected = Integer.toString(fabType.getSelectedIndex());
			}
			if (fabTypeItemCount <=1)FDialog.warn(tab.getWindowNo(), "Fabric type not determined, check product setup.", "Warning");
				
			
			
		
		/*TODO: Check current item to see it it's chain controlled. If it is, add a Listbox with chain products.
		 * 
		 *
		 */
	}

	private void validate() throws Exception
	{
		/*
		 * Validate
		 * Don't update the DB unless there are complete values
		 * For fabric, fabTypeSelected, which is the last fabric config option, MUST have a value
		 * if it is to be written to DB.
		 * For chains, the last field in the dialog must have been given a value. If the blind is not
		 * chain driven, then "" is to be passed
		 */
		
	//If all OK then:
		if(currentFabSelection==0)
		{
			FDialog.warn(tab.getWindowNo(), "Fabric not selected.", "Warning");//Cannot proceed without fabric
		}
		else 
		{
			setCurrentSelection(currentFabSelection,currentChainSelection);
		}
	}
	
	@Override
	public void onEvent(Event event) throws Exception {
		
		if(event.getTarget().getId().equals(ConfirmPanel.A_CANCEL))
			blindConfig.onClose();
		
		else if(event.getTarget().getId().equals(ConfirmPanel.A_OK)) {
			
			if(Integer.parseInt(fabTypeSelected)!=0)currentFabSelection = Integer.parseInt(fabTypeSelected);
			
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
			
			currentFabSelection = Integer.parseInt(fabTypeSelected);
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
	
	private void setCurrentSelection(final int lastFabric, final int lastChain)
	{
		//set the mtm_attribute column, but may need to get current contents then make new. Nah, just overwrite it.
	        Trx.run(new TrxRunnable() {
	            public void run(String trxName) {
	            	MOrderLine thisOrderLine = new MOrderLine(Env.getCtx(), currOrderLine, null);
	        		StringBuilder replaceValue = new StringBuilder(Integer.toString(lastFabric));
	        		replaceValue.append("_");
	        		replaceValue.append(Integer.toString(lastChain));
	        		String look = replaceValue.toString();
	        		thisOrderLine.set_ValueOfColumn("mtm_attribute", replaceValue.toString());
	        		
	        		System.out.println(look);
	              
	            
	                thisOrderLine.saveEx();
	            }
	      
	        });
	}
	

	
	private boolean checkChainDrive()
	{
		MOrderLine thisOrderLine = new MOrderLine(Env.getCtx(), currOrderLine, null);
		int attInsID = thisOrderLine.getM_AttributeSetInstance_ID();
		StringBuilder sql = new StringBuilder("SELECT description ");
		sql.append("FROM m_attributesetinstance ");
		sql.append("WHERE m_attributesetinstance_id = ?");
		
		//Get instance attribute for this lineitem.
		String description = DB.getSQLValueString(null, sql.toString(), attInsID);
		
		String word = "chain";
		Boolean found;
		found = description.contains(word);
		System.out.println(description + found.toString());
		
		return found;
	}
	

}

