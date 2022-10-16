/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package au.blindmot.editor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.Level;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.Column;
import org.adempiere.webui.component.Columns;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.NumberBox;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MColumn;
import org.compiere.model.MField;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.lang.SystemException;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.South;
import org.zkoss.zul.impl.InputElement;

import au.blindmot.model.MBLDProductNonSelect;


/**
 *  Product Attribute Set Product/Instance Dialog Editor.
 * 	Called from VPAttribute.actionPerformed
 *
 *  @author Jorg Janke
 *  
 *  ZK Port
 *  @author Low Heng Sin
 */
public class WNonSelectDialog extends Window implements EventListener<Event>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7810825026970615029L;
	private int m_bLD_Product_Non_Select_ID;
	private GridField m_Grid_Field;

	/**
	 *	Product Attribute Instance Dialog
	 *	@param mBLDLineProductsetInstanceID Product Attribute Set Instance id
	 * 	@param M_Product_ID Product id
	 * 	@param C_BPartner_ID b partner
	 * 	@param productWindow this is the product window (define Product Instance)
	 * 	@param AD_Column_ID column
	 * @param productWindow 
	 * 	@param WindowNo window
	 * @param m_WindowNo2 
	 * @throws SystemException 
	 * @throws Exception 
	 */
	
	public WNonSelectDialog(int bldProductsetinstance_ID, int M_Product_ID, int bLD_Product_Non_Select_ID, boolean productWindow,
			int AD_Column_ID, int WindowNo, GridField gridField) throws SystemException, Exception {
		super ();
		
		//if(gridField.getGridTab() != null){
			//gridField.getGridTab().getField("substituteproduct").addPropertyChangeListener(this);
			//gridField.getGridTab().getField("addtionalproduct").addPropertyChangeListener(this);
			
		//TODO Change below
		this.setTitle(Msg.translate(Env.getCtx(), "BLD_Line_ProductSetInstance_ID"));
		if (!ThemeManager.isUseCSSForWindowSize())
			ZKUpdateUtil.setWindowWidthX(this, 500);
		this.setSclass("popup-dialog pattribute-dialog");
		this.setBorder("normal");
		this.setShadow(true);
		this.setSizable(true);
		
		if (log.isLoggable(Level.CONFIG)) log.config("Mbld_line_productsetinstance_id " + bldProductsetinstance_ID
			+ ", M_Product_ID=" + M_Product_ID
			+ ", Column=" + AD_Column_ID);
		m_WindowNo = SessionManager.getAppDesktop().registerWindow(this);
		m_M_Product_ID = M_Product_ID;
		m_AD_Column_ID = AD_Column_ID;
		m_WindowNoParent = WindowNo;
		m_bLD_Product_Non_Select_ID = bLD_Product_Non_Select_ID;
		m_Grid_Field = gridField;

		//get columnName from ad_column
	
		
		try
		{
			init();
		}
		catch(Exception ex)
		{
			log.log(Level.SEVERE, "VPAttributeDialog" + ex);
		}
		//	Dynamic Init
		
		if (!initAttributes (true))
		{
			dispose();
			return;
		}
		
		
		AEnv.showCenterScreen(this);
	}	//	VPAttributeDialog

	private int						m_WindowNo;
	private Listbox		existingCombo = new Listbox();
	private int 					m_M_Product_ID;
	private int						m_C_BPartner_ID;
	private int						m_AD_Column_ID;
	private int						m_WindowNoParent;
	/**	Enter Product Attributes		*/
	private boolean					m_productWindow = false;
	/**	Change							*/
	private boolean					m_changed = false;
	
	private CLogger					log = CLogger.getCLogger(getClass());
	/** Row Counter					*/
	private int						m_row = 0;
	/** List of Editors				*/
	private ArrayList<HtmlBasedComponent>		m_editors = new ArrayList<HtmlBasedComponent>();
	/** Length of Instance value (40)	*/
	//private static final int		INSTANCE_VALUE_LENGTH = 40;

	
	private Button		bNewRecord = new Button(Msg.getMsg(Env.getCtx(), "NewRecord"));
	private Button		bSelect = new Button(); 
	
	//	Lot Popup
	Menupopup 					popupMenu = new Menupopup();
	private Menuitem 			mZoom;
	//
	private Textbox fieldDescription = new Textbox(); //TODO: set length to 20
	//
	private Borderlayout mainLayout = new Borderlayout();
	private Panel centerPanel = new Panel();
	private Grid centerLayout = new Grid();
	private ConfirmPanel confirmPanel = new ConfirmPanel (true);
	private Listbox box;
	private Checkbox cbNewEdit = new Checkbox();
	


	

	/**
	 *	Layout
	 * 	@throws Exception
	 */
	private void init () throws Exception
	{
		mainLayout.setParent(this);
		ZKUpdateUtil.setHflex(mainLayout, "1");
		ZKUpdateUtil.setVflex(mainLayout, "min");
		
		Center center = new Center();
		center.setSclass("dialog-content");
		center.setParent(mainLayout);
		ZKUpdateUtil.setVflex(centerPanel, "min");
		ZKUpdateUtil.setHflex(centerPanel, "min");
		center.appendChild(centerPanel);

		South south = new South();
		south.setSclass("dialog-footer");
		south.setParent(mainLayout);
		south.appendChild(confirmPanel);
		
		centerPanel.appendChild(centerLayout);
		centerLayout.setOddRowSclass("even");
		//
		confirmPanel.addActionListener(Events.ON_CLICK, this);
		
		
		
		/*
		
		Object id = gridTab.getValue(gridField.getColumnName().toString());
		if(id != null)
		{
		int prodID = Integer.parseInt(id.toString());
			if(prodID > 0)
			{
				initEditor(prodID);
			}
		}
		*/
		
	} //	init
	
	/**
	 *	Dyanmic Init.
	 *  @return true if initialized
	 * @throws WrongValueException 
	 */
	private boolean initAttributes (boolean readOnly) throws Exception, SystemException
	{
		Columns columns = new Columns();
		columns.setParent(centerLayout);
		
		Column column = new Column();
		column.setParent(columns);
		ZKUpdateUtil.setWidth(column, "30%");
		
		column = new Column();
		column.setParent(columns);
		ZKUpdateUtil.setWidth(column, "70%");
		
		Rows rows = new Rows();
		rows.setParent(centerLayout);
		
		//	Set Instance Attributes
		
		Row row = new Row();
		
		MBLDProductNonSelect current = new MBLDProductNonSelect(Env.getCtx(), m_bLD_Product_Non_Select_ID, null);
		BigDecimal currVal = Env.ZERO;
		if(m_bLD_Product_Non_Select_ID > 0)
		if((BigDecimal) current.get_ValueOfColumn(m_AD_Column_ID) != null)
			{
				currVal = (BigDecimal) current.get_ValueOfColumn(m_AD_Column_ID);
			}
		if (currVal.compareTo(Env.ZERO) == 0)		//	new
			cbNewEdit.setLabel(Msg.getMsg(Env.getCtx(), "NewRecord"));
		//else
			cbNewEdit.setLabel(Msg.getMsg(Env.getCtx(), "EditRecord"));
		cbNewEdit.addEventListener(Events.ON_CHECK, this);
		row.appendChild(cbNewEdit);
		bSelect.setLabel(Msg.getMsg(Env.getCtx(), "SelectExisting"));
		bSelect.setImage(ThemeManager.getThemeResource("images/PAttribute16.png"));
		bSelect.addEventListener(Events.ON_CLICK, this);
		bSelect.setEnabled(false);
		row.appendChild(bSelect);
		ZKUpdateUtil.setHflex(bSelect, "1");
		rows.appendChild(row);
		
		Listbox editor = new Listbox();
		
		editor.setMold("select");
		int bLDProductNonSelectID = Env.getContextAsInt(Env.getCtx(), m_WindowNoParent, "BLD_Product_Non_Select_ID");
		int mProductID = Env.getContextAsInt(Env.getCtx(), m_WindowNoParent, "M_Product_ID");
		if(bLDProductNonSelectID > 0 && mProductID >= 0)
		{
			MBLDProductNonSelect mBLDProductNonSelect = new MBLDProductNonSelect(Env.getCtx(), bLDProductNonSelectID, null);
			MProduct[] mProducts = mBLDProductNonSelect.getNonSelectableProducts();
			
			addAttributeLine (rows, mBLDProductNonSelect, false, false);
			for(int i=0; i < mProducts.length; i++) 
			{
				ListItem item = new ListItem(mProducts[i] != null ? mProducts[i].getName() : "", mProducts[i].getValue());
				editor.appendChild(item);
			}
		}
		int intCurrValue = currVal.intValueExact();
//		New/Edit Window
			if (!m_productWindow)
			{
				cbNewEdit.setChecked(!(intCurrValue > 0));
				cmd_newEdit();
			}
			else
			{
				cbNewEdit.setSelected(false);
				cbNewEdit.setEnabled(intCurrValue > 0);
				bNewRecord.setEnabled(intCurrValue > 0);
				boolean rw = intCurrValue  == 0;
				for (int i = 0; i < m_editors.size(); i++)
				{
					HtmlBasedComponent editor1 = m_editors.get(i);
					if (editor1 instanceof InputElement)
						((InputElement)editor1).setReadonly(!rw);
					else if (editor1 instanceof Listbox)
						((Listbox)editor1).setEnabled(rw);
					else if (editor1 instanceof NumberBox)
						((NumberBox)editor1).setEnabled(rw);
				}
			}
		
		
		
		if (readOnly)
			editor.setEnabled(false);
		else
			m_editors.add (editor);
		row.appendChild(editor);
		ZKUpdateUtil.setHflex(editor, "1");
		
		
		//}
return true;
}	//	initAttribute

	/**
	 *	dispose
	 */
	public void dispose()
	{
		Env.clearWinContext(m_WindowNo);
		//
		//Env.setContext(Env.getCtx(), m_WindowNoParent, Env.TAB_INFO, m_columnName, 
		//	String.valueOf(m_MbldLineProductsetInstanceID));
		//Env.setContext(Env.getCtx(), m_WindowNoParent, Env.TAB_INFO, "M_Locator_ID", 
		//	String.valueOf(m_M_Locator_ID));
		//
		this.detach();
	}	//	dispose

	public void onEvent(Event e) throws Exception 
	{
		
		
		if (e.getTarget() == bNewRecord)
		{
			cmd_newRecord();
		}
		else if (e.getTarget() == cbNewEdit)
		{
			if (m_productWindow)
			{
				cmd_edit();
			}
			else
			{
				cmd_newEdit();
			}
		}
		else if (e.getTarget() == existingCombo)
		{
			cmd_existingCombo();
		}
		
		
		
		//	OK
		if (e.getTarget().getId().equals("Ok"))
		{
			if (saveSelection())
				dispose();
		}
		
		//	Cancel
		else if (e.getTarget().getId().equals("Cancel"))
		{
			m_changed = false;
			
			dispose();
		}
		
		else
			log.log(Level.SEVERE, "not found - " + e);
	}	//	actionPerformed

	private void cmd_newRecord() {
		
		bNewRecord.setEnabled(false);
				
		for (int i = 0; i < m_editors.size(); i++)
		{
			HtmlBasedComponent editor = m_editors.get(i);
			if (editor instanceof InputElement)
			{
				((InputElement)editor).setReadonly(false);
				((InputElement)editor).setText(null);
			}
			else if (editor instanceof Listbox)
			{
				((Listbox)editor).setEnabled(true);
				((Listbox)editor).setSelectedItem(null);
			}
			else if (editor instanceof NumberBox)
			{
				((NumberBox)editor).setEnabled(true);
				((NumberBox)editor).setValue(null);
			}
		}
		fieldDescription.setText("");
	}


	/**
	 *	Save Selection
	 *	@return true if saved
	 */
	private boolean saveSelection()
	{
		
		log.warning("-------editors.size:  " + m_editors.size());
		for (int i = 0; i < m_editors.size(); i++)
		{
		
		Listbox editor = (Listbox)m_editors.get(i);
		ListItem item = editor.getSelectedItem();
		MProduct value = item != null ? (MProduct)item.getValue() : null;
		//if (log.isLoggable(Level.FINE)) log.fine(productPartSet[i].getName() + "=" + value);
		//if (productPartSet[i].isMandatory() && value == null)
			//mandatory += " - " + productPartSet[i].getName();
		
		//Code below not getting called.
		if(value != null)
		{
			BigDecimal setValue = new BigDecimal(value.get_ID());
			//MProduct mProduct = (MProduct)value;
			//int bLDProductNonSelectID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "BLD_Product_Non_Select_ID");
			
			if(m_bLD_Product_Non_Select_ID > 0 && value != null)
			{
				MBLDProductNonSelect mBLDProductNonSelect = new MBLDProductNonSelect(Env.getCtx(), m_bLD_Product_Non_Select_ID, null);
				//String column = gridField.getColumnName();
				log.warning("---------Setting column: " + m_AD_Column_ID + " with: " + value);
				mBLDProductNonSelect.set_ValueOfColumn(m_AD_Column_ID, setValue);
				
				GridTab gridTab = m_Grid_Field.getGridTab();
				gridTab.getTableModel().setCompareDB(false);
				gridTab.setValue(m_Grid_Field.getColumnName(), setValue);
				
				//mBLDProductNonSelect.saveEx();
			}
		
			
			
			
		}
		
	}
		return true;
}	//	saveSelection

	private void cmd_edit() {
		boolean check = cbNewEdit.isSelected();
		for (int i = 0; i < m_editors.size(); i++)
		{
			HtmlBasedComponent editor = m_editors.get(i);
			if (editor instanceof InputElement)
				((InputElement)editor).setReadonly(!check);
			else if (editor instanceof Listbox)
				((Listbox)editor).setEnabled(check);
			else if (editor instanceof NumberBox)
				((NumberBox)editor).setEnabled(check);
		}
		
		
	}
	
	/**
	 * 	Instance New/Edit
	 */
	private void cmd_newEdit()
	{
		boolean rw = cbNewEdit.isChecked();
		if (log.isLoggable(Level.CONFIG)) log.config("R/W=" + rw + " ");
		
		for (int i = 0; i < m_editors.size(); i++)
		{
			HtmlBasedComponent editor = m_editors.get(i);
			if (editor instanceof InputElement)
				((InputElement)editor).setReadonly(!rw);
			else if (editor instanceof Listbox)
				((Listbox)editor).setEnabled(rw);
			else if (editor instanceof NumberBox)
				((NumberBox)editor).setEnabled(rw);
		}
		
	}	//	cmd_newEdit

	
	
	//	getM_AttributeSetInstanceName
	
	/**
	 * 	Value Changed
	 *	@return true if changed
	 */
	public boolean isChanged()
	{
		return m_changed;
	}	//	isChanged
	
	private void addAttributeLine (Rows rows, MBLDProductNonSelect mBLDProductNonSelect, boolean product, boolean readOnly)
	{
		log.warning("--------In WBldPartsDialog.addAttributeLine()) start --- partType: " + mBLDProductNonSelect.toString());
		log.warning("--------partType.getName(): " + mBLDProductNonSelect.toString() + " partType.getDescription(): " + mBLDProductNonSelect.getDescription());
		
		//TODO: Change Attribute object to MBLDProductPartType
		if (log.isLoggable(Level.FINE)) log.fine(mBLDProductNonSelect + ", Product=" + product + ", R/O=" + readOnly);
		
		m_row++;
		MColumn aDColumn = new MColumn(Env.getCtx(), m_AD_Column_ID, null);
		//m_Grid_Field.get
		GridTab gtab = m_Grid_Field.getGridTab();
		GridField field = gtab.getField(aDColumn.getName());
		int fieldID = field.getAD_Field_ID();
		MField mField = new MField(Env.getCtx(), fieldID, null);


		Label label = new Label (mField.getName());
		if (product)
			label.setStyle("font-weight: bold");
	
		Row row = rows.newRow();
		row.appendChild(label.rightAlign());

		
			MProduct[] values = mBLDProductNonSelect.getNonSelectableProducts();	//	optional = null
			Listbox editor = new Listbox();
			
			editor.setMold("select");
			for (MProduct value : values) 
			{
				ListItem item = new ListItem(value != null ? value.getName() : "", value);
				editor.appendChild(item);
			}
			if (readOnly)
				editor.setEnabled(false);
			else
				m_editors.add (editor);
			row.appendChild(editor);
			ZKUpdateUtil.setHflex(editor, "1");
			setListAttribute(mBLDProductNonSelect, editor);
			
		
	}	//	addAttributeLine
	private void setListAttribute(MBLDProductNonSelect mPNS, Listbox editor) {
		boolean found = false;

			if (mPNS != null)
			{
				MProduct[] values = mPNS.getNonSelectableProducts();
				
				log.warning("--------In setListAttribute, MBLDLineProductInstance instance: " + mPNS.toString());
				BigDecimal bigProdID = (BigDecimal) mPNS.get_ValueOfColumn(m_AD_Column_ID);
				if(bigProdID != null)
				{
					int prodID = bigProdID.intValue();
					
					for (int i = 0; i < values.length; i++)
					{
						if (values[i] != null && values[i].get_ID() == prodID)
						{
							for (int i1 = 0; i1 < editor.getItemCount(); i1++)
							{
								editor.setSelectedIndex (i1);
								if(editor.getValue() != null)
								{
									Object setValue = editor.getValue();
									MProduct mProduct = (MProduct)setValue;
									if(mProduct.get_ID() == prodID)
									{		
										break;
									}
								}					
							}
							MProduct mProduct = (MProduct)editor.getValue();
							log.warning("MProduct selected from list: " + mProduct.getName());
							found = true;
							break;
						}
					}
				
				if (found )	
					{
						if (log.isLoggable(Level.FINE)) log.fine("Attribute=" + mPNS.get_TableName() + " #" + values.length + " - found: " + prodID);
					}
				}
			}
} //	WPAttributeDialog
	
	private void cmd_existingCombo() {
		ListItem pp = existingCombo.getSelectedItem();
		if (pp != null && (Integer)pp.getValue() != -1)
		{
			int colValue = (Integer) pp.getValue();
			//m_masi = MBLDLineProductSetInstance.get(Env.getCtx(), m_MbldLineProductsetInstanceID, m_M_Product_ID);
			// Get Part Set
			
			//MBLDLineProductSetInstance mbps = new MBLDLineProductSetInstance(Env.getCtx(), m_MbldLineProductsetInstanceID, null);
			//MBLDProductPartType[] attributes = mbps.getProductPartSet(m_M_Product_ID, null, true);
			if (log.isLoggable(Level.FINE)) log.fine ("Column value=" + colValue);
			//for (int i = 0; i < attributes.length; i++)
				
				//updateAttributeEditor(attributes[i], i);
			
			cbNewEdit.setEnabled(true);
			cbNewEdit.setSelected(false);
			bNewRecord.setEnabled(true);
			cmd_edit();
			
		}
	}
	
	private void updateAttributeEditor(MBLDProductNonSelect nPNS, int index) {
		//if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attribute.getAttributeValueType()))
		//{
			Listbox editor = (Listbox) m_editors.get(index);
			setListAttribute(nPNS, editor);
		
	}
}


