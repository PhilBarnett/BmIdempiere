package au.blindmot.editor;

import static org.compiere.model.SystemIDs.COLUMN_M_PRODUCT_M_ATTRIBUTESETINSTANCE_ID;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.adempiere.webui.component.EditorBox;
import org.adempiere.webui.component.PAttributebox;
import org.adempiere.webui.editor.IEditorConfiguration;
import org.adempiere.webui.editor.WEditor;
import org.adempiere.webui.editor.WEditorPopupMenu;
import org.adempiere.webui.event.ContextMenuEvent;
import org.adempiere.webui.event.ContextMenuListener;
import org.adempiere.webui.event.DialogEvents;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.window.WFieldRecordInfo;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.Lookup;
import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.model.StateChangeEvent;
import org.compiere.model.StateChangeListener;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import au.blindmot.model.MBLDProductNonSelect;
import au.blindmot.model.MBLDProductPartType;

public class WNonSelectProductEditor extends WEditor implements ContextMenuListener {
	
	private static final CLogger log = CLogger.getCLogger(WNonSelectProductEditor.class);
	private Listbox box;
	List<MProduct> mps;
	Set<Object> items = null;
	private static final String[] LISTENER_EVENTS = {Events.ON_CLICK, Events.ON_CHANGE, Events.ON_OK};

	private int m_WindowNo;

	private Lookup bldProdLookup;

	private int m_C_BPartner_ID;

	private Object m_value;

	private GridTab m_GridTab;
	private GridField m_GridField;

	/**	No Instance Key					*/
	private static Integer		NO_INSTANCE = new Integer(0);

	public WNonSelectProductEditor(GridField gridField, GridTab gridTab) {
		super(new WBldProdEditBox(), gridField);
		m_GridTab = gridTab;
		m_GridField = gridField;
		initComponents();
		
		}
	
	/**
	 * 
	 * @param gridTab
	 * @param gridField
	 * @param tableEditor
	 * @param editorConfiguration
	 */
	public WNonSelectProductEditor(GridTab gridTab, GridField gridField, boolean tableEditor, IEditorConfiguration editorConfiguration)
	{
		super(new WBldProdEditBox(), gridField, tableEditor, editorConfiguration);
		m_GridTab = gridTab;
		initComponents();
	}

		//EditorBox layout = (EditorBox) this.getComponent();
		//layout.setHeight(null);
		//layout.setWidth("100%");
		
		
	
	private void initComponents() {
		if (ThemeManager.isUseFontIconForImage())
			getComponent().getButton().setIconSclass("z-icon-PAttribute");
		else
			getComponent().setButtonImage(ThemeManager.getThemeResource("images/PAttribute16.png"));
		//TODO: Cull non required assets from /theme
		// getComponent().addEventListener(Events.ON_CLICK, this); // IDEMPIERE-426 - dup listener, already set at WEditor

		m_WindowNo = gridField.getWindowNo();
		bldProdLookup = gridField.getLookup();
		m_C_BPartner_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "C_BPartner_ID");

		//	Popup
		popupMenu = new WEditorPopupMenu(true, false, false);
		addChangeLogMenu(popupMenu);
		
		getComponent().getTextbox().setReadonly(true);

		if (gridField != null)
			getComponent().getTextbox().setPlaceholder(gridField.getPlaceholder());
	}
	
	
		
	
public void initEditor(int prodID) {
	boolean found = false;
	for (int i = 0; i < box.getItemCount(); i++)
	{
				box.setSelectedIndex (i);
				Listitem item = box.getSelectedItem();
				if(item.getValue() != null)
				{
					Object setValue = item.getValue();
					//MProduct mProduct = (MProduct)setValue;
					if(Integer.parseInt(setValue.toString()) == prodID)
					{		
						found = true;
						break;
					}
				}
	}
	
}	//	setComboBox

	
	public void onEvent(Event event) throws Exception
	{
		if (Events.ON_CHANGE.equals(event.getName()) || Events.ON_OK.equals(event.getName()))
		{
			String newText = getComponent().getText();
			String oldText = null;
			if (m_value != null)
			{
				oldText = bldProdLookup.getDisplay(m_value);
			}
			if (oldText != null && newText != null && oldText.equals(newText))
			{
	    	    return;
	    	}
	        if (oldText == null && newText == null)
	        {
	        	return;
	        }
			ValueChangeEvent changeEvent = new ValueChangeEvent(this, this.getColumnName(), oldText, newText);
			fireValueChange(changeEvent);
		}
		else if (Events.ON_CLICK.equals(event.getName()))
		{
			cmd_dialog();
		}
	}
	
	private void cmd_dialog() throws Exception
	{
		//TODO: getValue() returns a string throws class cast exception 
		Integer oldValue = 0;
		if(getValue() != null)
		{
			oldValue = new Integer(getValue().toString());
		}
		
		final int oldValueInt = oldValue == null ? 0 : oldValue.intValue ();
		int bldProductsetinstance_ID = oldValueInt;
		int M_Product_ID = 0;
		int M_ProductBOM_ID = 0;
		if (m_GridTab != null) 
		{
			M_Product_ID = Env.getContextAsInt (Env.getCtx (), m_WindowNo, m_GridTab.getTabNo(), "M_Product_ID");
			M_ProductBOM_ID = Env.getContextAsInt (Env.getCtx (), m_WindowNo, m_GridTab.getTabNo(), "M_ProductBOM_ID");
			//For third level tab (e.g, LineMA), should take M_Product_ID from Line instead of from Header
			if (m_GridTab.getTabLevel() > 1 && m_GridTab.getParentTab() != null && m_GridTab.getField("M_Product_ID")==null) 
			{
				int tmp = Env.getContextAsInt (Env.getCtx (), m_WindowNo, m_GridTab.getParentTab().getTabNo(), "M_Product_ID");
				if (tmp > 0)
					M_Product_ID = tmp;
			}
		} else {
			M_Product_ID = Env.getContextAsInt (Env.getCtx (), m_WindowNo, "M_Product_ID");
			M_ProductBOM_ID = Env.getContextAsInt (Env.getCtx (), m_WindowNo, "M_ProductBOM_ID");
		}

		if (log.isLoggable(Level.CONFIG)) 
			{
				log.config("M_Product_ID=" + M_Product_ID + "/" + M_ProductBOM_ID
				
				+ ",bldProductsetinstance_ID= " + bldProductsetinstance_ID
				+ ", AD_Column_ID=" + gridField.getAD_Column_ID());
			}
		//	M_Product.M_AttributeSetInstance_ID = 8418
		final boolean productWindow = (gridField.getAD_Column_ID() == COLUMN_M_PRODUCT_M_ATTRIBUTESETINSTANCE_ID);		//	HARDCODED

		if (M_ProductBOM_ID != 0)	//	Use BOM Component
			M_Product_ID = M_ProductBOM_ID;
		//
		if (!productWindow && (M_Product_ID == 0))
		{
			getComponent().setText(null);
			bldProductsetinstance_ID = 0;
			
			processChanges(oldValueInt, bldProductsetinstance_ID);
		}
		else
		{
			int bLD_Product_Non_Select_ID =0;
			if(m_GridTab.getValue("BLD_Product_Non_Select_ID") != null)
			{
				bLD_Product_Non_Select_ID = (Integer)m_GridTab.getValue("BLD_Product_Non_Select_ID");
			}
			final WNonSelectDialog vad = new WNonSelectDialog (
				bldProductsetinstance_ID, M_Product_ID, bLD_Product_Non_Select_ID,
				productWindow, gridField.getAD_Column_ID(), m_WindowNo, gridField);
			vad.addEventListener(DialogEvents.ON_WINDOW_CLOSE, new EventListener<Event>() {
			
				
				public void onEvent(Event event) throws Exception {
					boolean changed = false;
					int M_AttributeSetInstance_ID = 0;
					
					/*
					if (vad.isChanged())
					{
						getComponent().setText(vad.getM_AttributeSetInstanceName());
						M_AttributeSetInstance_ID = vad.getM_AttributeSetInstance_ID();
						if (m_GridTab != null && !productWindow && vad.getM_Locator_ID() > 0)
						{
							if (gridField.getColumnName().equals("M_AttributeSetInstanceTo_ID"))
								m_GridTab.setValue("M_LocatorTo_ID", vad.getM_Locator_ID());
							else
								m_GridTab.setValue("M_Locator_ID", vad.getM_Locator_ID());
							
						}
						changed = true;
					}
					*/
					
					//	Set Value
					if (changed)
					{
						processChanges(oldValueInt, M_AttributeSetInstance_ID);
					}	//	change
				}
			});
		}
	}
	
	
		
	
	private void processChanges(int oldValueInt, int mProductNSID) {
		if (log.isLoggable(Level.FINEST)) log.finest("Changed M_AttributeSetInstance_ID=" + mProductNSID);
		m_value = new Object();				//	force re-query display
		if (mProductNSID == 0)
			setValue(null);
		else
			setValue(new Integer(mProductNSID));

		ValueChangeEvent vce = new ValueChangeEvent(this, gridField.getColumnName(), new Object(), getValue());
		fireValueChange(vce);
		if (mProductNSID == oldValueInt && m_GridTab != null && gridField != null)
		{
			//  force Change - user does not realize that embedded object is already saved.
			m_GridTab.processFieldChange(gridField);
		}
	}
	
	public void onMenu(ContextMenuEvent evt)
	{
		
		if (WEditorPopupMenu.CHANGE_LOG_EVENT.equals(evt.getContextEvent()))
		{
			WFieldRecordInfo.start(gridField);
		}
	}
	
	public String[] getEvents()
    {
        return LISTENER_EVENTS;
    }

	@Override
	public void setReadWrite(boolean readWrite) {
		// TODO Auto-generated method stub

	}
	
	public void setValue(Object value)
	{
		if (value == null || NO_INSTANCE.equals(value))
		{
			getComponent().setText("");
			m_value = value;
			return;
		}

		//	The same
		if (value.equals(m_value))
			return;
		//	new value
		if (log.isLoggable(Level.FINE)) log.fine("Value=" + value);
		m_value = value;
		getComponent().setText(bldProdLookup.getDisplay(value));	//	loads value
	}
	
	@Override
	public WBldProdEditBox  getComponent()
	{
		return (WBldProdEditBox) component;
	}

	@Override
	public boolean isReadWrite() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplay() {
		return getComponent().getText();
	}
	
}
