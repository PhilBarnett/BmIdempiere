/**
 * 
 */
package au.blindmot.mtmbtn;

import java.util.HashSet;
import java.util.Set;

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
import org.zkoss.zul.Space;
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
		
		System.out.println("In execute of MtmButtonAction" + "tab ID: " + m_Tab_id + "record ID: " + tab.getRecord_ID());
		
		/*Dialog to display: 3 Fields - Fabric family, colour, type, chain material, chain length.
		 * TODO: check if the being opened from the current window makes sense.
		 * TODO: Get all the blind fabric types ie Sincerity, Integrity, LeReve etc, SELECT DISTINCT 
		 * so there's only 1 result per type. Then when the user exits the family field, initialise the colour field, then the type field.
		 * If the fabric is a screen, then 'type' field should be greyed.
		 * If the blind is chain driven, then the chain products should be available in the chain material. Once the material is selected, the length can be selected.
		 * 
		 */
		show();
		
	}
	
	public void show(){
		
		fabColour.getItems().clear();
		fabColour.setEnabled(false);
		fabType.getItems().clear();
		fabType.setEnabled(false);
		chainMaterial.getItems().clear();
		chainMaterial.setEnabled(false);//TODO: Set enabled(true) if the item is chain controlled.
		chainLength.getItems().clear();
		fabFamily.setMold("select");blindConfig
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
		row.appendChild(new Label("Fabric Family")); //This translate() method is pretty cool. Such a big codebase to work with.
		row.appendChild(fabFamily);
		ZKUpdateUtil.setHflex(fabFamily, "1");
		fabFamily.addEventListener(Events.ON_SELECT, this);
		
		//Add confirm panel
		
		row = new Row();
		rows.appendChild(row);
		row.appendChild(new Space());

		vb.appendChild(confirmPanel);
		LayoutUtils.addSclass("dialog-footer", confirmPanel);
		confirmPanel.addActionListener(this);
		
		
		}
	}
	
	private void initFabFam() {
		
		   //Get the values for the fabric list box.
		
		String sql = "SELECT DISTINCT ON (name) name"
				+ " FROM m_product as mp"
				+ " WHERE mp.m_parttype_id ="
				+ " (SELECT m_parttype_id FROM m_parttype WHERE m_parttype.name = 'Fabric')";
				
		fabFamily.setMold("select");
			KeyNamePair[] keyNamePairs = DB.getKeyNamePairs(sql, true);
			Set<String> set = new HashSet<String>();
			int i = 0;
			for (KeyNamePair pair : keyNamePairs) {
				
				
				
				set.add(pair.getName());

				}
			//TODO: Does this hack work?
			for (String name : set) {
				i++;
				fabFamily.appendItem(name, i);
				
			}
		
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
			fabFamilySelected = li.getValue().toString();
			//TODO: Validate then set values for fabType
		}
		
		else if(event.getTarget() == fabColour)
		{
			//TODO: Validate then set values for fabType
		}
		
		else if (event.getName().equals("onValidate")) 
		{
			try {
				validate();
			} finally {
				Clients.clearBusy();
				panel.getComponent().invalidate();
			}
		
		
	}

	}
}

