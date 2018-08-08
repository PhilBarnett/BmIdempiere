/**
 * 
 */
package au.blindmot.mtmbtn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import org.compiere.model.X_M_PartType;
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

import au.blindmot.model.MBLDMtomItemLine;


/**
 * @author phil
 * This is a dog's regurgitated breakfast and is full of issues.
 * This could be done by an extra field in the c_orderline table 'mtmoptions' that stores
 * the options and the product_ids like mattributeinstances eg 'chain=1000012_fabric=1000018'
 * and should be entered and validated by an editor.
 * NOTE: If the button is called from a mtmproduction window, any changes to the values are written to the
 * mbldtmtomlineitem. The instance variables are still read from the c_orderline.
 *
 */
public class MtmButtonActionWindow implements EventListener<Event> {

	private static CLogger log = CLogger.getCLogger(MtmButtonActionWindow.class);
	private AbstractADWindowContent panel;
	private ConfirmPanel 	confirmPanel = new ConfirmPanel(true);
	private GridTab 		tab = null;
	private Listbox fabFamily = new Listbox();
	private Listbox fabColour = new Listbox();
	private Listbox fabType = new Listbox();
	private Listbox controlType = new Listbox();
	private Listbox chainList = new Listbox();
	private Listbox bottomBar = new Listbox();
	private Listbox TubularNCM = new Listbox();
	private Listbox rollerBracket = new Listbox();
	private Window 	blindConfig = null;
	private String fabFamilySelected = null;
	private String fabColourSelected = null;
	private String fabTypeSelected = null;
	private String chainSelected = null;
	private String bottomBarSelected = null;
	private int currentFabSelection = 0;
	private int currentChainSelection = 0;
	private int currOrderLine = 0;
	private int currProductId = 0;
	private int currbldmtomitemlineID = 0;
	private int currBottomBar = 0;
	private boolean isChainDriven = false;
	private boolean isMtmProdWindow = false;
	private ADWindow window;

	
	/*
	 * TODO:Add a 'control' ListBox and populate with product from BOM with Part Type = Tubular blind control
	 * TODO: Modify code in Listbox chainList to add chain options if the 'control' is a chain drive
	 * TODO:Add a 'non-control' ListBox and populate with product from BOM with Part Type = Tubular non-control mech
	 * TODO:Add a 'Bracket' ListBox and populate with product from BOM with Part Type = Roller Bracket
	 * TODO:Add a 'Bottom bar' ListBox and populate with product from BOM with Part Type = Bottom bar
	 */
	
	/**
	 * <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
	 */
	public MtmButtonActionWindow(AbstractADWindowContent panel, ADWindow window) {
		this.panel = panel;	
		this.window = window;
		prepare();
	
	}

	
		/*If this returns "" then it fails the check. Should return 'M_Product_ID =1000010' or similar.
		 * Possibly should check for just roller blind?
		 * Maybe create a new category?
		 *
		 */		

	public void show(){
		
		int AD_Table_ID=panel.getActiveGridTab().getAD_Table_ID();
		if (blindConfig == null)
		{	
		blindConfig = new Window();
		
		fabColour.setMold("select");
		fabColour.getItems().clear();
		fabColour.setEnabled(false);
		fabType.getItems().clear();
		fabType.setEnabled(false);
		chainList.getItems().clear();
		bottomBar.getItems().clear();
		try
		{
			chainList.setEnabled(false);
		}
		catch(NullPointerException ex)
		{
			System.out.println("Whoopsie: " + ex.toString());
		}
		//TODO: Set enabled(true) if the item is chain controlled.	
		fabFamily.setMold("select");
		fabFamily.getItems().clear();
		initFabFam();
		setOptions(currProductId);
		
		
		
		
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
		row.appendChild(new Label("Material Family:"));
		row.appendChild(fabFamily);
		ZKUpdateUtil.setHflex(fabFamily, "1");
		fabFamily.addEventListener(Events.ON_SELECT, this);
	
		
		//Add the fabColour Listbox
		Row row1 = new Row();
		rows.appendChild(row1);
		row1.appendChild(new Label("Material Colour:")); 
		row1.appendChild(fabColour);
		ZKUpdateUtil.setHflex(fabColour, "1");
		if(currentFabSelection == 0 && fabFamilySelected == null)
			{
			fabColour.setVisible(false);//Don't show this box until a fabric family is selected.
			}
		fabColour.addEventListener(Events.ON_SELECT, this);
		
		//Add the fabType Listbox
		Row row2 = new Row();
		rows.appendChild(row2);
		row2.appendChild(new Label("Material Type:")); 
		row2.appendChild(fabType);
		ZKUpdateUtil.setHflex(fabType, "1");
		fabType.addEventListener(Events.ON_SELECT, this);
		if((currentFabSelection == 0 && fabColourSelected == null) || fabType.getItemCount() == 0)
			{
				fabType.setVisible(false);//Don't show this Listbox until a fabric colour is selected. 
			}
		
		//Add bottom bar Listbox
		
		if(bottomBar.getItemCount() != 0)
		{
		Row row3 = new Row();
		rows.appendChild(row3);
		row3.appendChild(new Label("Bottom bar:")); 
		row3.appendChild(bottomBar);
		ZKUpdateUtil.setHflex(bottomBar, "1");
		bottomBar.setMold("select");
		bottomBar.addEventListener(Events.ON_SELECT, this);
		initBottomBar();
		bottomBar.setVisible(true);
			
		}
		/*
		 * Add chainFam Listbox
		 * Check if it's chain driven
		 */
		isChainDriven = checkChainDrive();
		if(isChainDriven)
		{
			Row row21 = new Row();
			rows.appendChild(row21);
			row21.appendChild(new Label("Chain:")); 
			row21.appendChild(chainList);
			ZKUpdateUtil.setHflex(chainList, "1");
			chainList.addEventListener(Events.ON_SELECT, this);
			initChains();
		}
		else
		{
			currentChainSelection = 0;
			setCurrentSelection();
		}
		
		
		//Add confirm panel
		vb.appendChild(confirmPanel);
		LayoutUtils.addSclass("dialog-footer", confirmPanel);
		confirmPanel.addActionListener(this);
		
		
		LayoutUtils.openPopupWindow(panel.getToolbar().getButton("mtmb"), blindConfig, "after_start");
		
		}
	}
	
	private void initBottomBar() {
		for(int x=0; x < bottomBar.getItemCount(); x++)
		{
			ListItem item = bottomBar.getItemAtIndex(x);
			if(Integer.parseInt(item.getValue().toString()) == currBottomBar )
			{
				bottomBar.setSelectedItem(item);
				bottomBar.setSelectedIndex(x);
				break;
			}
		}

	
		if(currBottomBar == 0 && bottomBar.getItemCount()>0)//sets a default for bottom bar if no current selection
		{
			bottomBar.setSelectedIndex(0);
			ListItem li = bottomBar.getSelectedItem();
			currBottomBar = Integer.parseInt(li.getValue().toString());
		}
			
		if(bottomBarSelected != null)currBottomBar = Integer.parseInt(bottomBarSelected);	
		
		/*
		if(currBottomBar != 0 && bottomBar.getItemCount() > 0)
		{
			bottomBar.setSelectedIndex(0);//Sets the list with the value from the DB
			bottomBarSelected = Integer.toString(currBottomBar);
		}
		*/
			
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
		
		KeyNamePair checkFab = checkSelection(keyNamePairs, currentFabSelection);
		boolean addCurrent = false;
		if (checkFab != null)
			{
			dupCheck.add(checkFab.getName());
			addCurrent = true;
			}
		
		int count = 0;
		for (KeyNamePair pair : keyNamePairs) 
		{	
		
			if(count  == 1 && addCurrent)fabColour.appendItem(checkFab.getName(), checkFab.getID());//adds the current selection at index 1
		
			//Remove duplicates
				if (!dupCheck.contains(pair.getName())) fabColour.appendItem(pair.getName(), pair.getID());
				dupCheck.add(pair.getName());
				count++;
		}
		
		
		if (currentFabSelection !=0 && fabColour.getItemCount() > 0)
		{
			fabColour.setSelectedIndex(1);//Initializes the list with the value from the DB
			fabColour.setEnabled(true);
			fabColour.setVisible(true);
			loadFabType(Integer.toString(currentFabSelection));
			
		}
		
	   }
	
	
	private KeyNamePair checkSelection(KeyNamePair[] keyNamePairs, int currentSelection)
	{
	if(currentSelection !=0)//There's already a selection, add it to the list at index 1
	{
		for (KeyNamePair pair : keyNamePairs)
		{
			if(pair.getKey() > 1)
			{
				if(pair.getID().equals(Integer.toString(currentSelection)))return pair;
			}
				
			
		}
	}return null;
	}
	
	
	
	
	
	private void initFabFam() {
		
		   //Get the values for the fabric list box.
		//TODO: These should only come from the BOM for the product.
		
		StringBuilder sql = new StringBuilder("SELECT mp.m_product_id, mp.name ");
		sql.append("FROM m_product_bom mpb ");
		sql.append("JOIN m_product mp ON mp.m_product_id = mpb.m_productbom_id ");
		sql.append("WHERE mpb.m_product_id = ");
		sql.append(currProductId);
		sql.append(" AND mp.m_parttype_id = ");
		sql.append("(SELECT m_parttype_id FROM m_parttype WHERE m_parttype.name = 'Fabric')");
				
		fabFamily.setMold("select");
			KeyNamePair[] keyNamePairs = DB.getKeyNamePairs(sql.toString(), true);
			ArrayList<String> dupCheck = new ArrayList<String>();
			boolean addCurrent = false;
			int count = 0;
			
			KeyNamePair checkFab = checkSelection(keyNamePairs, currentFabSelection);
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
			
			if(currentFabSelection != 0 && fabFamily.getItemCount() > 0)
				{
				fabFamily.setSelectedIndex(1);//Sets the list with the value from the DB
				loadFabColour(Integer.toString(currentFabSelection));
				fabFamilySelected = Integer.toString(currentFabSelection);
				}
				
			
			}
	
	
	private void loadFabType(String selectedColour)
	{
		if(currentFabSelection != 0)
		{
			fabFamilySelected = Integer.toString(currentFabSelection);
		}
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT mp.m_product_id, mai.value ");
		sql.append("FROM m_product mp ");
		sql.append("JOIN m_attributeinstance mai ON mai.m_attributesetinstance_id = mp.m_attributesetinstance_id ");
		sql.append("JOIN m_attribute ma ON ma.m_attribute_id = mai.m_attribute_id ");
		sql.append("AND mp.name = (SELECT name FROM m_product WHERE m_product_id = ");
		sql.append(fabFamilySelected);
		sql.append(") ");
		sql.append(" AND mp.description = (SELECT description FROM m_product WHERE m_product_id = ");
		sql.append(selectedColour);
		sql.append(")");
		sql.append("AND ma.name LIKE 'Fabric desc'");
		
		fabType.setMold("select");
		KeyNamePair[] keyNamePairs = DB.getKeyNamePairs(sql.toString(), true);
		ArrayList<String> dupCheck = new ArrayList<String>();
		
		KeyNamePair checkFab = checkSelection(keyNamePairs, currentFabSelection);
		boolean addCurrent = false;
		if (checkFab != null)
			{
			addCurrent = true;
			dupCheck.add(checkFab.getName());
			}
		
		int count = 0;
		for (KeyNamePair pair : keyNamePairs) 
		{	
		
			if(count  == 1 && addCurrent)fabType.appendItem(checkFab.getName(), checkFab.getID());//adds the current selection at index 1
		
			//Remove duplicfabColourSelectedates
			if (!dupCheck.contains(pair.getName())) fabType.appendItem(pair.getName(), pair.getID());//Remove duplicates
			
			dupCheck.add(pair.getName());
			count++;
		}
		System.out.println("fabType.getItemCount(): " + fabType.getItemCount());
		if (currentFabSelection !=0 && fabType.getItemCount() > 1)
		{
			fabType.setSelectedIndex(1);//Initializes the list with the value from the DB
			fabType.setEnabled(true);
			fabType.setVisible(true);
			
		}
		
		int fabTypeItemCount = fabType.getItemCount();
		if(fabTypeItemCount <3)fabType.setEnabled(false);//If only one item, select it and set box unable
			if (fabTypeItemCount >1 && fabTypeItemCount <3)
			{
				fabType.setSelectedIndex(0);
				fabTypeSelected = selectedColour;
			}
			//if (fabTypeItemCount <=1)FDialog.warn(tab.getWindowNo(), "Fabric type not determined, check product setup.", "Warning");
			
	}
	
	private void initChains()//We want the chains to be on the BOM for the product being optioned.
	{
		if(currentChainSelection != 0)
		{
			chainSelected = Integer.toString(currentChainSelection);
		}
		
		StringBuilder sql = new StringBuilder("SELECT mp.m_product_id, mp.name ");
		sql.append("FROM m_product_bom mpb ");
		sql.append("JOIN m_product mp ON mp.m_product_id = mpb.m_productbom_id ");
		sql.append("WHERE mpb.m_product_id = ");
		sql.append(currProductId);
		sql.append("AND mp.m_parttype_id = ");
		sql.append("(SELECT m_parttype_id FROM m_parttype WHERE m_parttype.name = 'Chain');");
		
		chainList.setMold("select");
		KeyNamePair[] keyNamePairs = DB.getKeyNamePairs(sql.toString(), true);
		ArrayList<String> dupCheck = new ArrayList<String>();
		
		KeyNamePair checkChains = checkSelection(keyNamePairs, currentChainSelection);
		boolean addCurrent = false;
		if (checkChains != null)
			{
			addCurrent = true;
			dupCheck.add(checkChains.getName());
			}
		
		int count = 0;
		for (KeyNamePair pair : keyNamePairs) 
		{	
		
			if(count  == 1 && addCurrent)chainList.appendItem(checkChains.getName(), checkChains.getID());//adds the current selection at index 1
		
			//Remove duplicfabColourSelectedates
			if (!dupCheck.contains(pair.getName())) chainList.appendItem(pair.getName(), pair.getID());//Remove duplicates
			
			dupCheck.add(pair.getName());
			count++;
		}
		
		if (currentChainSelection !=0 && chainList.getItemCount() > 0)
		{
			chainList.setSelectedIndex(1);//Initializes the list with the value from the DB
		}
		
		chainList.setEnabled(true);
		chainList.setVisible(true);
		
		
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
			setCurrentSelection(currentFabSelection,currentChainSelection, currBottomBar);
		}
	}
	
	@Override
	public void onEvent(Event event) throws Exception {
		
		if(event.getTarget().getId().equals(ConfirmPanel.A_CANCEL))
			
			blindConfig.onClose();
			
			
		
		else if(event.getTarget().getId().equals(ConfirmPanel.A_OK)) {
			
			if(fabTypeSelected!=null){
				if(Integer.parseInt(fabTypeSelected)!=0)currentFabSelection = Integer.parseInt(fabTypeSelected);
			}
			
			
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
			fabType.setVisible(false);
			fabColour.setEnabled(true);
			fabColour.setVisible(true);
			currentFabSelection = 0;
			loadFabColour(fabFamilySelected);
			setOkButton();
			
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
			currentFabSelection = 0;
			loadFabType(fabColourSelected);
			setOkButton();
		}
		
		else if(event.getTarget() == fabType)
		{
		
			fabTypeSelected = null;
			ListItem li = fabType.getSelectedItem();
			fabTypeSelected = li.getValue();
			
			if(fabTypeSelected == null)
			{
				confirmPanel.setEnabled("OK", false);
			}
			else 
				{
				currentFabSelection = Integer.parseInt(fabTypeSelected);
				confirmPanel.setEnabled("OK", false);
				}
			setOkButton();
		}
		
		else if(event.getTarget() == chainList)
		{
			chainSelected = null;
			ListItem li = chainList.getSelectedItem();
			chainSelected = li.getValue();
			
			if(chainSelected != null)currentChainSelection = Integer.parseInt(chainSelected );
			setOkButton();
		}
		else if(event.getTarget() == bottomBar)
		{
			bottomBarSelected = null;
			ListItem li = bottomBar.getSelectedItem();
			bottomBarSelected = li.getValue().toString();
			if(bottomBarSelected != null)currBottomBar = Integer.parseInt(bottomBarSelected);
			setOkButton();
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
	
	private void setOkButton()
	{
		confirmPanel.setEnabled("Ok", true);
		ListItem li = fabType.getSelectedItem();
		if(li != null)
		{
			if(li.getValue() != null)
			{
				fabTypeSelected = li.getValue();
			}
		}
		
		ListItem li1 = fabColour.getSelectedItem();
		
				if(li1 != null)
				{
					fabTypeSelected = li1.getValue();
				}
		
		
		if(fabTypeSelected == null)
		{
			confirmPanel.setEnabled("Ok", false);
		}
		
		ListItem li2 = chainList.getSelectedItem();
		if(li2 != null)chainSelected = li2.getValue();
		
		if (chainSelected == null & isChainDriven)
			{
			confirmPanel.setEnabled("Ok", false);
			}
	}
	
	public int getCurrentselection()
	{
		return currentFabSelection;//TODO: Change to something that makes sense.
		
	}
	
	private void setCurrentSelection()
	{
		setCurrentSelection(currentFabSelection, currentChainSelection, currBottomBar);
	}
	
	private void setCurrentSelection(final int lastFabric, final int lastChain, final int lastBottomBar)
	{
		//set the mtm_attribute column, but may need to get current contents then make new. Nah, just overwrite it.
		//TODO: Modify to allow use in the context of mtmProduction window.
	        Trx.run(new TrxRunnable() {
	            public void run(String trxName) {
	            	
	        		StringBuilder replaceValue = new StringBuilder(Integer.toString(lastFabric));
	        		replaceValue.append("_");
	        		replaceValue.append(Integer.toString(lastChain));
	        		replaceValue.append("_");
	        		replaceValue.append(currBottomBar);
	        		
	        		if(currbldmtomitemlineID != 0)//It's an mtmproductionline
	        		{
	        			MBLDMtomItemLine thisMtmLine = new MBLDMtomItemLine(Env.getCtx(), currbldmtomitemlineID, null);
						thisMtmLine.setinstance_string(replaceValue.toString());
						thisMtmLine.saveEx();
	        		}
	        		else
	        		{
	        			MOrderLine thisOrderLine = new MOrderLine(Env.getCtx(), currOrderLine, null);
	        			thisOrderLine.set_ValueOfColumn("mtm_attribute", replaceValue.toString());
	        			thisOrderLine.saveEx();
	        		}
	        	
	        		System.out.println(replaceValue.toString());
	        
	            }
	      
	        });
	}
	

	
	private boolean checkChainDrive()
	{
		String description = null;
		StringBuilder sql = new StringBuilder("SELECT description ");
		sql.append("FROM m_attributesetinstance ");
		sql.append("WHERE m_attributesetinstance_id = ?");
		if(currOrderLine!=0)
			{
				MOrderLine thisOrderLine = new MOrderLine(Env.getCtx(), currOrderLine, null);
				int attInsID = thisOrderLine.getM_AttributeSetInstance_ID();
				//Get instance attribute for this lineitem.
				description = DB.getSQLValueString(null, sql.toString(), attInsID);
			}
		else if(currbldmtomitemlineID!=0)
			{
				MBLDMtomItemLine mBLDMtomItemLine = new MBLDMtomItemLine(Env.getCtx(), currbldmtomitemlineID, null);
				int attInsId = mBLDMtomItemLine.getattributesetinstance_id();
				//Get instance attribute for this lineitem.
				description = DB.getSQLValueString(null, sql.toString(), attInsId);
			}
		
		String word = "Chain";
		Boolean found = false;
		if(description != null)found = description.contains(word);
		log.info(description + found.toString());
		
		return found;
	}
	/**
	 * Note that most of this method is not used; only the bottom bar ListBox is set up with this method.
	 * Good code though, could be used in other areas.
	 * @param m_product_id
	 */
	
	private void setOptions(int m_product_id) 
	{
		StringBuilder sql = new StringBuilder("SELECT mp.m_parttype_id, mp.m_product_id, mp.name ");
		sql.append("FROM m_product mp INNER JOIN m_product_bom mpb ");
		sql.append("ON mp.m_product_id = mpb.m_productbom_id ");
		sql.append("AND mpb.isactive  ='Y' ");
		sql.append("AND mpb.m_product_id = ");
		sql.append(m_product_id);
		sql.append(" ORDER BY mp.m_parttype_id");
		
		ResultSet rs = DB.getRowSet(sql.toString());
		System.out.println(rs.toString());
			
		List<String> mpName = new ArrayList<String>();
		List<Integer> partIDs = new ArrayList<Integer>();
		List<Integer> prodIDs = new ArrayList<Integer>();
		
		    try {
				while (rs.next()) {
					partIDs.add(rs.getInt(1));
					prodIDs.add(rs.getInt(2));
					mpName.add(rs.getString(3));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		Integer parts[] = partIDs.toArray(new Integer[(partIDs.size())]);
		String names[] = mpName.toArray(new String[mpName.size()]);
		Integer productIDs[] = prodIDs.toArray(new Integer[(prodIDs.size())]);
		
		String controlComp = "Tubular blind control";
		String bBar = "Bottom bar";
		String TNCM = "TNCM";
		String rBracket = "Roller bracket";
	
		{
			for(int i = 0; i < parts.length; i++) {
	
				X_M_PartType mPartType = new X_M_PartType(null, parts[i], null);
				String partType = (String) mPartType.get_Value("name");
				if(partType == null)
				{
					break;
				}
				else if(partType.equalsIgnoreCase(controlComp))
				{
					controlType.appendItem(names[i], productIDs[i]);
				}
				else if(partType.equalsIgnoreCase(bBar))
				{
					bottomBar.appendItem(names[i], productIDs[i]);
				}
				else if(partType.equalsIgnoreCase(TNCM))
				{
					TubularNCM.appendItem(names[i], productIDs[i]);
				}
				else if(partType.equalsIgnoreCase(rBracket))
				{
					rollerBracket.appendItem(names[i], productIDs[i]);
				}
				}
			
			
		}
	
	}

	
	public void prepare() {
		fabFamilySelected = null;
		fabColourSelected = null;
		fabTypeSelected = null;
		chainSelected = null;
		currentFabSelection = 0;
		currentChainSelection = 0;
		currOrderLine = 0;
		currbldmtomitemlineID = 0;
		currBottomBar = 0;
		isChainDriven = false;
		isMtmProdWindow = false;
		Object mtmAttribute = null;

		/*TODO: Ensure the ADWindow windowobject doesn't cause access issues.
		 * 
		 */
		
		ADWindowContent content = window.getADWindowContent();
		tab = content.getActiveGridTab();
		tab.getAD_Window_ID();
		//panel = content; 
		
		
		log.info("MtmButtonAction window title: " + window.getTitle());
		tab.getAD_Window_ID();
		tab.getAD_Tab_ID();
		if(window.getTitle().toString().equalsIgnoreCase("Production - made to measure"))isMtmProdWindow = true;
		
		log.info("Attempting to parse c_order_line: " + Env.parseContext(Env.getCtx(), tab.getWindowNo(), "@C_OrderLine_ID@", true));
		log.info("Attempting to parse bld_mtom_item_line_ID: " + Env.parseContext(Env.getCtx(), tab.getWindowNo(), "@bld_mtom_item_line_ID@", true));
		if(Env.parseContext(Env.getCtx(), tab.getWindowNo(), "@bld_mtom_item_line_ID@", true).length()!=0)
			{
				currbldmtomitemlineID = Integer.parseInt(Env.parseContext(Env.getCtx(), tab.getWindowNo(), "@bld_mtom_item_line_ID@", true));
			}
		
		//Test to see if the current record is a mtm product.
		//Get the c_orderline_id, find the product_id, check if the product_id is a mtm product
		StringBuilder sql_mtm = new StringBuilder("SELECT ismadetomeasure FROM m_product WHERE m_product_id = ?");
		
		String isMtm = "Y";
		if(currProductId!=0)
		{
			isMtm = DB.getSQLValueString(null, sql_mtm.toString(), currProductId);
		}
		
		String c_Order_line = Env.parseContext(Env.getCtx(), tab.getWindowNo(), "@C_OrderLine_ID@", true);
		String mtm_Order_line = Env.parseContext(Env.getCtx(), tab.getWindowNo(), "@bld_mtom_item_line_ID@", true);
		
		if (c_Order_line =="" && mtm_Order_line=="")
		{
			FDialog.warn(tab.getWindowNo(), "There's no order line to specify options for.", "Warning");
		}
			else if(c_Order_line != "")
			{
				currOrderLine = Integer.parseInt(c_Order_line);
				StringBuilder sql = new StringBuilder("SELECT m_product_id FROM c_orderline WHERE c_orderline_id = ");
				sql.append(c_Order_line);
				int m_Product = DB.getSQLValue(null, sql.toString());
				currProductId = m_Product;
			}
				
			
				if (c_Order_line == "" && isMtm!=null && !isMtm.equals("Y"))
				{
					FDialog.warn(tab.getWindowNo(), "There's no made to measure product to specify options for.", "Warning");
				}
			
				//If not mtmproduction window, do below code - check if attributes already assigned
				
					
					if(isMtmProdWindow&&currbldmtomitemlineID!=0)
					{
						MBLDMtomItemLine thisMtmLine = new MBLDMtomItemLine(Env.getCtx(), currbldmtomitemlineID, null);
						mtmAttribute = thisMtmLine.getinstance_string();
						currProductId = thisMtmLine.getM_Product_ID();
					}
					else
					{
						MOrderLine thisOrderLine = new MOrderLine(Env.getCtx(), currOrderLine, null);
						mtmAttribute = thisOrderLine.get_Value("mtm_attribute");
					}
					  	
					
					//Check if this order line already has attributes assigned.
					
					String patternString = "[0-9][0-9][0-9][0-9][0-9][0-9][0-9]";
				    Pattern pattern = Pattern.compile(patternString);
					
					if(mtmAttribute!=null && mtmAttribute.toString().contains("_"))
					{
						//Split the value of the mtm_attribute column, first value id fabric, second is chain, third is bottom bar
						String[] products = mtmAttribute.toString().split("_");
						
						if(products.length > 0)
						{
							Matcher matcher = pattern.matcher(products[0]);
							boolean matches = matcher.matches();
							if(matches)currentFabSelection = Integer.parseInt(products[0]);
							if(products[0].length() != 7)currentFabSelection = 0;
						}
						
						if(products.length > 1)
						{
							Matcher matcher1 = pattern.matcher(products[1]);
							boolean matches1 = matcher1.matches();
							if(matches1)currentChainSelection = Integer.parseInt(products[1]);
						}
						
						if(products.length > 2)
						{
							Matcher matcher2 = pattern.matcher(products[2]);
							boolean matches2 = matcher2.matches();
							if(matches2)currBottomBar = Integer.parseInt(products[2]);
						}
				}		
	}
}

