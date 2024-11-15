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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import org.adempiere.webui.event.DialogEvents;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.adempiere.webui.window.FDialog;
import org.adempiere.webui.window.WPAttributeInstance;
import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeInstance;
import org.compiere.model.MDocType;
import org.compiere.model.MProduct;
import org.compiere.model.MProductCategory;
import org.compiere.model.MQuery;
import org.compiere.model.SystemIDs;
import org.compiere.model.X_M_MovementLine;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
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

import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDLineProductSetInstance;
import au.blindmot.model.MBLDProductPartType;
import au.blindmot.utils.MtmUtils;


/**
 *  Product Attribute Set Product/Instance Dialog Editor.
 * 	Called from VPAttribute.actionPerformed
 *
 *  @author Jorg Janke
 *  
 *  ZK Port
 *  @author Low Heng Sin
 */
public class WBldPartsDialog extends Window implements EventListener<Event>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7810825026970615029L;

	/**
	 *	Product Attribute Instance Dialog
	 *	@param mBLDLineProductsetInstanceID Product Attribute Set Instance id
	 * 	@param M_Product_ID Product id
	 * 	@param C_BPartner_ID b partner
	 * 	@param productWindow this is the product window (define Product Instance)
	 * 	@param AD_Column_ID column
	 * 	@param WindowNo window
	 * @throws Exception 
	 */
	public WBldPartsDialog (int mBLDLineProductsetInstanceID, 
		int M_Product_ID, int C_BPartner_ID, 
		boolean productWindow, int AD_Column_ID, int WindowNo) throws Exception
	{
		super ();
		this.setTitle(Msg.translate(Env.getCtx(), "BLD_Line_ProductSetInstance_ID"));
		if (!ThemeManager.isUseCSSForWindowSize())
			ZKUpdateUtil.setWindowWidthX(this, 600);
		this.setSclass("popup-dialog pattribute-dialog");
		this.setBorder("normal");
		this.setShadow(true);
		this.setSizable(true);
		
		if (log.isLoggable(Level.CONFIG)) log.config("Mbld_line_productsetinstance_id " + mBLDLineProductsetInstanceID 
			+ ", M_Product_ID=" + M_Product_ID
			+ ", C_BPartner_ID=" + C_BPartner_ID
			+ ", ProductW=" + productWindow + ", Column=" + AD_Column_ID);
		m_WindowNo = SessionManager.getAppDesktop().registerWindow(this);
		m_MbldLineProductsetInstanceID = mBLDLineProductsetInstanceID;
		m_M_Product_ID = M_Product_ID;
		m_C_BPartner_ID = C_BPartner_ID;
		m_productWindow = productWindow;
		m_AD_Column_ID = AD_Column_ID;
		m_WindowNoParent = WindowNo;

		//get columnName from ad_column
		m_columnName = DB.getSQLValueString(null, "SELECT ColumnName FROM AD_Column WHERE AD_Column_ID = ?", m_AD_Column_ID);
		if (m_columnName == null || m_columnName.trim().length() == 0) 
		{
			//fallback
			m_columnName = "BLD_Line_ProductSetInstance_ID";
		}
		
		try
		{
			init();
		}
		catch(Exception ex)
		{
			log.log(Level.SEVERE, "VPAttributeDialog" + ex);
		}
		//	Dynamic Init
		if (!initAttributes ())
		{
			dispose();
			return;
		}
		AEnv.showCenterScreen(this);
	}	//	VPAttributeDialog

	private int						m_WindowNo;
	//private MAttributeSetInstance	m_masi;//TODO: change type to MBLDLineProductInstance and see what happens
	private MBLDLineProductSetInstance	m_masi;//TODO: change type to MBLDLineProductSetInstance and see what happens
	private int 					m_MbldLineProductsetInstanceID;
	private int 					m_M_Locator_ID;
	private String					m_M_AttributeSetInstanceName;
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

	private Checkbox	cbNewEdit = new Checkbox();
	private Checkbox	cbDualRoller = new Checkbox();
	private Button		bNewRecord = new Button(Msg.getMsg(Env.getCtx(), "NewRecord"));
	private Listbox		existingCombo = new Listbox();
	private Button		bSelect = new Button(); 
	private Listbox		ctrlBox = null;
	private Listbox		nCtrlBox = null;
	private Listbox 	linkBox = null;
	private Listbox		bracketBox = null;
	private Listbox fieldLot = new Listbox();
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
	
	private String m_columnName = null;
	private boolean isChainControl = false;
	private boolean isDualRoller = false;
	private boolean isLink = false;
	//private boolean isIdleLink = false;
	private static String IS_CHAIN_CONTROL = "Is chain control";
	private static String TUBULAR_NON_CONTROL = "TNCM";
	private static String TUBULAR_BLIND_CONTROL = "Tubular blind control";
	private static String BRACKET = "Roller bracket";
	private static String LINK_BRACKET = "Roller link bracket";
	private static String IS_LINK = "Is link";
	private ArrayList<Listbox> chainArray = new ArrayList<Listbox>();

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
	}	//	init

	/**
	 *	Dyanmic Init.
	 *  @return true if initialized
	 * @throws WrongValueException 
	 */
	private boolean initAttributes () throws Exception, SystemException
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
		
		if (m_M_Product_ID == 0 && !m_productWindow)
			return false;
		
		MBLDLineProductSetInstance psa = new MBLDLineProductSetInstance(Env.getCtx(), m_MbldLineProductsetInstanceID, null);
		Integer[] partTypes = psa.getPartTypes(m_M_Product_ID);
		//MBLDProductPartType[] partTypes = psa.getProductPartSet(m_M_Product_ID, null);
		MProduct mProduct = new MProduct(Env.getCtx(), m_M_Product_ID, null);
		//TODO: Check if MTM product if not return false
		//TODO: Get getProductPartSet and modify code below to suit.
		
		
		
		int M_AttributeSet_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNoParent, "M_AttributeSet_ID");
		if (m_M_Product_ID != 0 && M_AttributeSet_ID == 0)
		{
			//	Get Model
			m_masi = MBLDLineProductSetInstance.get(Env.getCtx(), m_MbldLineProductsetInstanceID, m_M_Product_ID);
			if (m_masi == null)
			{
				log.severe ("No Model for M_AttributeSetInstance_ID=" + m_MbldLineProductsetInstanceID + ", M_Product_ID=" + m_M_Product_ID);
				return false;
			}
			//Env.setContext(Env.getCtx(), m_WindowNo, "M_AttributeSet_ID", m_masi.getM_AttributeSet_ID());
	
			//	Get Attribute Set
			//TODO: Change to something meaningful for this situation
			//as = m_masi.getMAttributeSet();
		}
		/*
		else 
		{
			m_masi = new MBLDLineProductSetInstance (Env.getCtx(), m_MbldLineProductsetInstanceID, M_AttributeSet_ID, null);
			as = m_masi.getMAttributeSet();
		} */
		
		//	Product has no PartTypesSet
		if (partTypes == null)		
		{
			FDialog.error(m_WindowNo, this, "PartTypesNoPartTypesSet");
			return false;
		}
		//Product is not a made to measure product
		if (!mProduct.get_ValueAsBoolean("ismadetomeasure"))
		{
			FDialog.error(m_WindowNo, this, "MProductNotMadeTo Measure");
			return false;
		}
		
		/*
		 * TODO Get the controlID here
		 * Check if it's dual or single and set flag
		 */
		
		else	//	Set Instance Attributes
		{
			Row row = new Row();
			
			//	New/Edit - Selection
			if (m_MbldLineProductsetInstanceID == 0)		//	new
				cbNewEdit.setLabel(Msg.getMsg(Env.getCtx(), "NewRecord"));
			else
				cbNewEdit.setLabel(Msg.getMsg(Env.getCtx(), "EditRecord"));
			cbNewEdit.addEventListener(Events.ON_CHECK, this);
			row.appendChild(cbNewEdit);
			bSelect.setLabel(Msg.getMsg(Env.getCtx(), "SelectExisting"));
			bSelect.setImage(ThemeManager.getThemeResource("images/PAttribute16.png"));
			bSelect.addEventListener(Events.ON_CLICK, this);
			row.appendChild(bSelect);
			ZKUpdateUtil.setHflex(bSelect, "1");
			rows.appendChild(row);
			
			MProductCategory  mProductCategory = new MProductCategory(Env.getCtx(),mProduct.getM_Product_Category_ID(), null);
			
			if(mProductCategory.getName() != null && mProduct.getClassification() != null)
			{
				if(mProduct.getClassification().equalsIgnoreCase("roller")||mProductCategory.getName().equalsIgnoreCase("Roller Blind"))
				{//Add dual roller option if it's a roller blind.
					Row row1 = new Row();
					row1.appendChild(cbDualRoller);
					cbDualRoller.setLabel("Dual Roller");
					cbDualRoller.addEventListener(Events.ON_CHECK, this);
					rows.appendChild(row1);
				}
			}
			
			//	All Attributes
			
			/*XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
			 * This where the magic starts
			 * TODO: 
			 */
			
			MBLDLineProductSetInstance mbps = new MBLDLineProductSetInstance(Env.getCtx(), m_MbldLineProductsetInstanceID, null);
			log.warning("mbs == :" + mbps.toString());
			MBLDProductPartType[] partTypes1 =  mbps.getProductPartSet(m_M_Product_ID , null, true);
			log.warning ("Part Types= " + partTypes1.length);
			for (int i = 0; i < partTypes1.length; i++)
				addAttributeLine (rows, partTypes1[i], false, false);
			
			/*/Delete once bug checked
			MAttribute[] attributes = as.getMAttributes (true);
			if (log.isLoggable(Level.FINE)) log.fine ("Instance Attributes=" + attributes.length);
			for (int i = 0; i < attributes.length; i++)
				addAttributeLine (rows, attributes[i], false, false);
			*/
		}
			
		if (m_row == 0)
		{
			FDialog.error(m_WindowNo, this, "PAttributeNoInfo");
			return false;
		}

		//	New/Edit Window
		if (!m_productWindow)
		{
			cbNewEdit.setChecked(m_MbldLineProductsetInstanceID == 0);
			cmd_newEdit();
		}
		else
		{
			cbNewEdit.setSelected(false);
			cbNewEdit.setEnabled(m_MbldLineProductsetInstanceID > 0);
			bNewRecord.setEnabled(m_MbldLineProductsetInstanceID > 0);
			boolean rw = m_MbldLineProductsetInstanceID == 0;
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
		}

		//	Attrribute Set Instance Description
		Label label = new Label (Msg.translate(Env.getCtx(), "Description"));
//		label.setLabelFor(fieldDescription);
		fieldDescription.setText(m_masi.getDescription());
		System.out.println(m_masi.getDescription());
		fieldDescription.setReadonly(true);
		Row row = new Row();
		row.setParent(rows);
		row.appendChild(label);
		row.appendChild(fieldDescription);
		ZKUpdateUtil.setHflex(fieldDescription, "1");
		
		return true;
	}	//	initAttribute

	/**
	 * 	Add Attribute Line
	 *	@param mBLDpartType attribute
	 * 	@param product product level attribute
	 * 	@param readOnly value is read only
	 */
	private void addAttributeLine (Rows rows, MBLDProductPartType mBLDpartType, boolean product, boolean readOnly)
	{
		log.warning("--------In WBldPartsDialog.addAttributeLine()) start --- partType: " + mBLDpartType.toString());
		log.warning("--------partType.getName(): " + mBLDpartType.getName() + " partType.getDescription(): " + mBLDpartType.getDescription());
		boolean tubularBlindControl = false;
		boolean tNCM = false;
		boolean isLinkControl = false;
		boolean bracket = false;
		//TODO: Change Attribute object to MBLDProductPartType
		if (log.isLoggable(Level.FINE)) log.fine(mBLDpartType + ", Product=" + product + ", R/O=" + readOnly);
		
		m_row++;
		MProduct[] values = MBLDProductPartType.getPartSetProducts(m_M_Product_ID, mBLDpartType.getBLD_M_PartType_ID(), null);	//	optional = null
		Label label = new Label (mBLDpartType.getName());
		if (product)
			label.setStyle("font-weight: bold");
		log.warning("--------In WBldPartsDialog.addAttributeLine() Before if (partType.getDescription() != null) String desc = partType.getDescription() == " + mBLDpartType.getDescription());	
		if (mBLDpartType.getName() != null)
		{
			String desc = mBLDpartType.getName();
			if(desc == null)
			label.setTooltiptext(desc);
			log.warning("--------In WBldPartsDialog.addAttributeLine() String desc = partType.getDescription() == " + desc);
			if(desc.equalsIgnoreCase(TUBULAR_BLIND_CONTROL) || mBLDpartType.getName().equalsIgnoreCase(TUBULAR_BLIND_CONTROL))
			{
				log.warning("--------In WBldPartsDialog.addAttributeLine() tubularBlindControl = true");
				tubularBlindControl = true;
				//setIsDualFlag(mBLDpartType.get_ID());
				//values = modifyDualTypes(values, isDualRoller);
			}
			if(desc.equalsIgnoreCase(TUBULAR_NON_CONTROL) || mBLDpartType.getName().equalsIgnoreCase(TUBULAR_NON_CONTROL))
			{
				log.warning("--------In WBldPartsDialog.addAttributeLine() TNCM = true");
				//setIsDualFlag(mBLDpartType.get_ID());
				tNCM = true;
				//setIsLinkControlFlag();
				//values = modifyDualTypes(values, isDualRoller);
			}
			if(desc.equalsIgnoreCase(LINK_BRACKET) || mBLDpartType.getName().equalsIgnoreCase(LINK_BRACKET))
			{
				log.warning("--------In WBldPartsDialog.addAttributeLine() isLinkControl = true");
				setIsDualFlag(mBLDpartType.get_ID());
				isLink = true;
				values = modifyDualTypes(values, isDualRoller);
			}
			
			if(desc.equalsIgnoreCase(IS_LINK) || mBLDpartType.getName().equalsIgnoreCase(IS_LINK))//Probably wont work
			{
				log.warning("--------In WBldPartsDialog.addAttributeLine() IS_LINK = true");
				setIsDualFlag(mBLDpartType.get_ID());
				isLink = true;
				values = modifyDualTypes(values, isDualRoller);
			}
			if(desc.equalsIgnoreCase(BRACKET) || mBLDpartType.getName().equalsIgnoreCase(BRACKET))
			{
				log.warning("--------In WBldPartsDialog.addAttributeLine() IS_LINK = true");
				setIsDualFlag(mBLDpartType.get_ID());
				bracket = true;
				values = modifyDualTypes(values, isDualRoller);
			}
			
			
		}
			
		
		
		/*
		 * If the partType.getDescription() is tubular blind control then add a listener
		 * If partType.getDescription() == 'Chain accessory' or 'Chain' then check to see if tubular blind control is a chain drive - if it is then set field active
		 */
		
		Row row = rows.newRow();
		row.appendChild(label.rightAlign());
		//
		//BLD will all be LIST
		//if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attribute.getAttributeValueType()))
		/*{*/
			
		//TODO: Change to MProduct[] partType.getPartSetProducts(int mProductID, int mPartypeID, String trxName) 
		
			Listbox editor = new Listbox();
			
			if(tubularBlindControl == true)
			{
				log.warning("---------In WBldPartsDialog.addAttributeLine tubularBlindControl == true");
				if(editor != null)
				{
					log.warning("---------In WBldPartsDialog.addAttributeLine tubularBlindControl == true, editor != null");
				}
				editor.setId("controlBox");
				editor.addEventListener(Events.ON_SELECT, this); 
				log.warning("---------line 458 editor");
				ctrlBox = editor;
				tubularBlindControl = false;
				
				//modify/remove bracket parts from MProduct[] values
				//values = modifyDualTypes(values, isDualRoller);
				
			}
			
			/**
			 * Delete or deprecate
			 */
			if(tNCM == true)
			{
				log.warning("---------In WBldPartsDialog.addAttributeLine TNCM == true");
				if(editor != null)
				{
					log.warning("---------In WBldPartsDialog.addAttributeLine TNCM == true, editor != null");
				}
				editor.setId("TNCMBox");
				editor.addEventListener(Events.ON_SELECT, this); 	
				nCtrlBox  = editor;
				tNCM = false;
			
				//modify/remove bracket parts from MProduct[] values
				//values = modifyDualTypes(values, isDualRoller);
				
			}
			
			if(isLink == true)
			{
				log.warning("---------In WBldPartsDialog.addAttributeLine IS_LINK == true");
				if(editor != null)
				{
					log.warning("---------In WBldPartsDialog.addAttributeLine IS_LINK  == true, editor != null");
				}
				editor.setId("LinkBox");
				editor.addEventListener(Events.ON_SELECT, this); 	
				linkBox  = editor;
				isLink = false;
			
				//modify/remove bracket parts from MProduct[] values
				//values = modifyDualTypes(values, isDualRoller);
				
			}
			
			if(bracket == true)
			{
				log.warning("---------In WBldPartsDialog.addAttributeLine bracket == true");
				if(editor != null)
				{
					log.warning("---------In WBldPartsDialog.addAttributeLine bracket  == true, editor != null");
				}
				editor.setId("bracketBox");
				editor.addEventListener(Events.ON_SELECT, this); 	
				bracketBox  = editor;
				bracket = false;
			}
			
			
			
			
			//Add any chain related rows to chainArray
			if(editor != null && mBLDpartType.getName().contains("Chain"))
			{
				chainArray.add(editor);
			}
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
			setListAttribute(mBLDpartType, editor);
			
			
			
		
	}	//	addAttributeLine
	
	private ListItem getControlId(Listbox ctrlOrTNCM)
	{
		if(ctrlOrTNCM == null)
		{
			log.warning("---------In WBldPartsDialog.getControlID(), ctrlBox == null");
		}
		
		if(ctrlOrTNCM != null)
		{
			return ctrlOrTNCM.getSelectedItem();
		}
		log.warning("---------In WBldPartsDialog.getControlID() returning null");
		return null;
	}
	
	private void updateAttributeEditor(MBLDProductPartType partType, int index) {
		//if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attribute.getAttributeValueType()))
		//{
			Listbox editor = (Listbox) m_editors.get(index);
			setListAttribute(partType, editor);
			
		//}
		/*	
		else if (MAttribute.ATTRIBUTEVALUETYPE_Number.equals(attribute.getAttributeValueType()))
		{
			NumberBox editor = (NumberBox) m_editors.get(index);
			setNumberAttribute(attribute, editor);
		}
		else	//	Text Field
		{
			Textbox editor = (Textbox) m_editors.get(index);
			setStringAttribute(attribute, editor);
		}
		*/
	}
	
	private void setStringAttribute(MAttribute attribute, Textbox editor)  throws WrongValueException {
		MAttributeInstance instance = attribute.getMAttributeInstance (m_MbldLineProductsetInstanceID);
		if (instance != null)
			try {
				editor.setText(instance.getValue());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void setNumberAttribute(MAttribute attribute, NumberBox editor) {
		MAttributeInstance instance = attribute.getMAttributeInstance (m_MbldLineProductsetInstanceID);
		if (instance != null)
			editor.setValue(instance.getValueNumber());
		else
			editor.setValue(Env.ZERO);		
	}

	/*
	 * BLD will always be list.
	 * Get the parttype.
	 * Get the instance of the partype based on m_MbldLineProductsetInstanceID -> bld_line_productinstanceproductID
	 * -> bld_product_parttype_id gets  m_product_id  which is list value.
	 * 
	 */
	private void setListAttribute(MBLDProductPartType mBLDpartType, Listbox editor) {
		boolean found = false;
		
		MProduct[] values = MBLDProductPartType.getPartSetProducts(m_M_Product_ID, mBLDpartType.getBLD_M_PartType_ID(), null);	//	optional = null
			
		if(mBLDpartType.getName().equalsIgnoreCase(TUBULAR_BLIND_CONTROL) || mBLDpartType.getName().equalsIgnoreCase(TUBULAR_NON_CONTROL))
			{
				values = modifyDualTypes(values, isDualRoller);
			}
		
		if(m_MbldLineProductsetInstanceID > 1)
		{	
			MBLDLineProductSetInstance mBLDlPSI  = new MBLDLineProductSetInstance(Env.getCtx(), m_MbldLineProductsetInstanceID, null);
			MBLDLineProductInstance instance = 
					mBLDlPSI.getMBLDLineProductInstance(m_MbldLineProductsetInstanceID, mBLDpartType.get_ID());
		/*instance var below should be the instance for this parttype and m_MbldLineProductsetInstanceID
		 * 
		 */	
			
		/*	
			
		if (instance != null)
		{
			log.warning("--------In setListAttribute, MBLDLineProductInstance instance: " + instance.toString());
			for (int i = 0; i < values.length; i++)
			{
				if (values[i] != null && values[i].get_ID() == instance.getM_Product_ID())
				{
					editor.setSelectedIndex (i);
					found = true;
					break;
				}
			}
			if (found ){
				if (log.isLoggable(Level.FINE)) log.fine("Attribute=" + partType.getName() + " #" + values.length + " - found: " + instance);
			} else {
				log.warning("Attribute=" + partType.getName() + " #" + values.length + " - NOT found: " + instance);
			}
		}	//	setComboBox
		
		*/
			
			if (instance != null)
			{
				log.warning("--------In setListAttribute, MBLDLineProductInstance instance: " + instance.toString());
				int prodID = instance.getM_Product_ID();
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
									found = true;
									break;
								}
							}					
						}
						
						if(editor != null)
						{
							if(editor.getItemCount() > 0)
							{
								MProduct mProduct = (MProduct)editor.getValue();
								log.warning("MProduct selected from list: " + mProduct.getName());
							}
							
						}
						
						break;
					}
				}
				if (found ){
					
					if(editor.getId().equalsIgnoreCase("bracketBox") || editor.getId().equalsIgnoreCase("linkBox"))
					{
						//Set IsDual flag
						String isProductDual = (String) MtmUtils.getMattributeInstanceValue(prodID, MtmUtils.MTM_IS_DUAL, null);
						if(isProductDual.equalsIgnoreCase("Yes"))
						{
							isDualRoller = true;
						}
						else 
						{
							isDualRoller = false;
						}
						cbDualRoller.setSelected(isDualRoller);
					}
					if (log.isLoggable(Level.FINE)) log.fine("Attribute=" + mBLDpartType.getName() + " #" + values.length + " - found: " + instance);
				} else {
					log.warning("Attribute=" + mBLDpartType.getName() + " #" + values.length + " - NOT found: " + instance);
				}
			}	//	setComboBox
			else
				if (log.isLoggable(Level.FINE)) log.fine("Attribute=" + mBLDpartType.getName() + " #" + values.length + " no instance");
		} else {
			if (log.isLoggable(Level.FINE)) log.fine("Attribute=" + mBLDpartType.getName() + " #" + values.length + " no instance");
	}
}

	/**
	 *	dispose
	 */
	public void dispose()
	{
		Env.clearWinContext(m_WindowNo);
		//
		Env.setContext(Env.getCtx(), m_WindowNoParent, Env.TAB_INFO, m_columnName, 
			String.valueOf(m_MbldLineProductsetInstanceID));
		Env.setContext(Env.getCtx(), m_WindowNoParent, Env.TAB_INFO, "M_Locator_ID", 
			String.valueOf(m_M_Locator_ID));
		//
		this.detach();
	}	//	dispose

	public void onEvent(Event e) throws Exception 
	{
		//	Select Instance
		if (e.getTarget() == bSelect)
		{
			cmd_select();//May be redundant - remove method and calls to it if possible.				
		}
		//	New/Edit
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
		else if (e.getTarget() == cbDualRoller)
		{
			cmd_dualRoller();
		}
		else if (e.getTarget() == bNewRecord)
		{
			cmd_newRecord();
		}
		else if (e.getTarget() == existingCombo)
		{
			cmd_existingCombo();
		}
		else if (e.getTarget().getId().equalsIgnoreCase("controlBox")/* || e.getTarget().getId().equalsIgnoreCase("TNCMBox")*/)
		{
			log.warning("---------In WBldPartsDialog.onEvent()---e.getTarget().getId().equalsIgnoreCase(\"controlBox\")");
			setChainControlFlag();
			setIsLinkControlFlag();
			setChainEditorsActive();//Set the chain related Listboxes inactive	
			setBracketEditorActive();
			
		}
		else if (e.getTarget().getId().equalsIgnoreCase("LinkBox"))
		{
			//Set 
		}
		
		//	OK
		else if (e.getTarget().getId().equals("Ok"))
		{
			if (saveSelection())
				dispose();
		}
		
		//	Cancel
		else if (e.getTarget().getId().equals("Cancel"))
		{
			m_changed = false;
			m_MbldLineProductsetInstanceID = 0;
			m_M_Locator_ID = 0;
			dispose();
		}
		//	Zoom M_Lot
		else if (e.getTarget() == mZoom)
		{
			cmd_zoom();
		}
		else
			log.log(Level.SEVERE, "not found - " + e);
	}	//	actionPerformed

	/**
	 * 
	 */
	private void cmd_dualRoller() {
		isDualRoller = cbDualRoller.isChecked();
		MBLDLineProductSetInstance mbps = new MBLDLineProductSetInstance(Env.getCtx(), m_MbldLineProductsetInstanceID, null);
		MBLDProductPartType[] partTypes1 =  mbps.getProductPartSet(m_M_Product_ID , null, true);
		for(int q = 0; q < partTypes1.length; q++)
		{
			/*
			if(partTypes1[q].getName().equalsIgnoreCase("TNCM"))
			{
				resetControlEditor(nCtrlBox, partTypes1[q].getBLD_M_PartType_ID());
				setListAttribute(partTypes1[q], nCtrlBox);
			}
			*/
			/*
			else if(partTypes1[q].getName().equalsIgnoreCase(TUBULAR_BLIND_CONTROL))
			{
				resetControlEditor(ctrlBox, partTypes1[q].getBLD_M_PartType_ID());
				setListAttribute(partTypes1[q], ctrlBox);
			}
			*/
			/*else*/ if(partTypes1[q].getName().equalsIgnoreCase(BRACKET))
			{
				resetEditor(bracketBox, partTypes1[q].getBLD_M_PartType_ID());
				setListAttribute(partTypes1[q], bracketBox);
			}
			else if(partTypes1[q].getName().equalsIgnoreCase(LINK_BRACKET))
			{
				resetEditor(linkBox, partTypes1[q].getBLD_M_PartType_ID());
				setListAttribute(partTypes1[q], linkBox);
			}	
			
		}
		setChainEditorsActive();
		setBracketEditorActive();
		
	}

	private void cmd_existingCombo() {
		ListItem pp = existingCombo.getSelectedItem();
		if (pp != null && (Integer)pp.getValue() != -1)
		{
			m_MbldLineProductsetInstanceID = (Integer) pp.getValue();
			m_masi = MBLDLineProductSetInstance.get(Env.getCtx(), m_MbldLineProductsetInstanceID, m_M_Product_ID);
			// Get Part Set
			
			MBLDLineProductSetInstance mbps = new MBLDLineProductSetInstance(Env.getCtx(), m_MbldLineProductsetInstanceID, null);
			MBLDProductPartType[] attributes = mbps.getProductPartSet(m_M_Product_ID, null, true);
			if (log.isLoggable(Level.FINE)) log.fine ("Product Attributes=" + attributes.length);
			for (int i = 0; i < attributes.length; i++)
				
				updateAttributeEditor(attributes[i], i);
			
			cbNewEdit.setEnabled(true);
			cbNewEdit.setSelected(false);
			bNewRecord.setEnabled(true);
			cmd_edit();
			
		}
	}

	private void cmd_newRecord() {
		cbNewEdit.setSelected(false);
		cbNewEdit.setEnabled(false);
		bNewRecord.setEnabled(false);
		existingCombo.setSelectedItem(null);
		
		m_MbldLineProductsetInstanceID = 0;
		//int M_AttributeSet_ID = m_masi.getM_AttributeSet_ID();
		m_masi = new MBLDLineProductSetInstance (Env.getCtx(), m_MbldLineProductsetInstanceID, null);		
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
		setChainEditorsActive();
	}

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
		if(check)
		{
			setChainEditorsActive();
			setBracketEditorActive();
		}
		
		
	}

	/**
	 * 	Instance Selection Button
	 * 	@return true if selected
	 */
	private void cmd_select()
	{
		log.config("");
		
		int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNoParent, "M_Warehouse_ID");
		
		int C_DocType_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNoParent, "C_DocType_ID");
		if (C_DocType_ID > 0) {
			MDocType doctype = new MDocType (Env.getCtx(), C_DocType_ID, null);
			String docbase = doctype.getDocBaseType();
			if (docbase.equals(MDocType.DOCBASETYPE_MaterialReceipt))
				M_Warehouse_ID = 0;
		}
		
		// teo_sarca [ 1564520 ] Inventory Move: can't select existing attributes
		int M_Locator_ID = 0;
		if (m_AD_Column_ID == SystemIDs.COLUMN_M_MOVEMENTLINE_M_ATTRIBUTESETINSTANCE_ID) { // TODO: hardcoded: M_MovementLine[324].M_AttributeSetInstance_ID[8551]
			M_Locator_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNoParent, X_M_MovementLine.COLUMNNAME_M_Locator_ID, true); // only window
		}
		
		String title = "";
		//	Get Text
		String sql = "SELECT p.Name, w.Name, w.M_Warehouse_ID FROM M_Product p, M_Warehouse w "
			+ "WHERE p.M_Product_ID=? AND w.M_Warehouse_ID"
				+ (M_Locator_ID <= 0 ? "=?" : " IN (SELECT M_Warehouse_ID FROM M_Locator where M_Locator_ID=?)"); // teo_sarca [ 1564520 ]
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, m_M_Product_ID);
			pstmt.setInt(2, M_Locator_ID <= 0 ? M_Warehouse_ID : M_Locator_ID);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				title = rs.getString(1) + " - " + rs.getString(2);
				M_Warehouse_ID = rs.getInt(3); // fetch the actual warehouse - teo_sarca [ 1564520 ]
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		//		
		final WPAttributeInstance pai = new WPAttributeInstance(title, 
			M_Warehouse_ID, M_Locator_ID, m_M_Product_ID, m_C_BPartner_ID);
		pai.addEventListener(DialogEvents.ON_WINDOW_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				if (pai.getM_AttributeSetInstance_ID() != -1)
				{
					m_MbldLineProductsetInstanceID = pai.getM_AttributeSetInstance_ID();
					m_M_AttributeSetInstanceName = pai.getM_AttributeSetInstanceName();
					m_M_Locator_ID = pai.getM_Locator_ID();
					m_changed = true;
					dispose();
				}				
			}
		});		
	}	//	cmd_select

	/**
	 * 	Instance New/Edit
	 */
	private void cmd_newEdit()
	{
		boolean rw = cbNewEdit.isChecked();
		if (log.isLoggable(Level.CONFIG)) log.config("R/W=" + rw + " " + m_masi);
		
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
		cbDualRoller.setEnabled(rw);
		if(rw)
		{
			setChainEditorsActive();
			setBracketEditorActive();
		}
		
	}	//	cmd_newEdit

	/**
	 * 	Zoom M_Lot
	 */
	private void cmd_zoom()
	{
		int M_Lot_ID = 0;
		ListItem pp = fieldLot.getSelectedItem();
		if (pp != null)
			M_Lot_ID = (Integer) pp.getValue();
		MQuery zoomQuery = new MQuery("M_Lot");
		zoomQuery.addRestriction("M_Lot_ID", MQuery.EQUAL, M_Lot_ID);
		log.info(zoomQuery.toString());
		//
		//TODO: to port
		/*
		int AD_Window_ID = 257;		//	Lot
		AWindow frame = new AWindow();
		if (frame.initWindow(AD_Window_ID, zoomQuery))
		{
			this.setVisible(false);
			this.setModal (false);	//	otherwise blocked
			this.setVisible(true);
			AEnv.addToWindowManager(frame);
			AEnv.showScreen(frame, SwingConstants.EAST);
		}*/
	}	//	cmd_zoom

	/**
	 *	Save Selection
	 *	@return true if saved
	 */
	private boolean saveSelection()
	{
		log.warning("--------In WBldPartsDialog.saveSelection() m_MbldLineProductsetInstanceID == " + m_MbldLineProductsetInstanceID);
		MBLDLineProductSetInstance as = new MBLDLineProductSetInstance(Env.getCtx(), m_MbldLineProductsetInstanceID, null);
		log.warning("---------MBLDLineProductSetInstance as == " + as.toString());
		
		if (as == null)
			{
				//log.severe ("No Model for M_AttributeSetInstance_ID=" + m_MbldLineProductsetInstanceID + ", M_Product_ID=" + m_M_Product_ID);	
				return true;
			}
		//
		m_changed = false;
		String mandatory = "";

		//	***	Save Attributes ***
		//	New Instance
		if (mandatory.isEmpty() && (m_changed || m_masi.getBLD_Line_ProductSetInstance_ID() == 0))
		{
			m_masi.saveEx();//TODO: This is critical and should work flawlessly
			m_MbldLineProductsetInstanceID = m_masi.getBLD_Line_ProductSetInstance_ID();
			m_M_AttributeSetInstanceName = m_masi.getDescription();
		}

		//	Save Instance Attributes
		//MAttribute[] attributes = as.getMAttributes(!m_productWindow);
		MBLDLineProductSetInstance mbps = new MBLDLineProductSetInstance(Env.getCtx(), m_MbldLineProductsetInstanceID, null);
		MBLDProductPartType[] productPartSet = mbps.getProductPartSet(m_M_Product_ID, null, true);
		
		for (int i = 0; i < productPartSet.length; i++)
		{
			//if (MAttribute.ATTRIBUTEVALUETYPE_List.equals(attributes[i].getAttributeValueType()))
			//{
				Listbox editor = (Listbox)m_editors.get(i);
				ListItem item = editor.getSelectedItem();
				MProduct value = item != null ? (MProduct)item.getValue() : null;
				//if (log.isLoggable(Level.FINE)) log.fine(productPartSet[i].getName() + "=" + value);
				if (productPartSet[i].isMandatory() && value == null)
					mandatory += " - " + productPartSet[i].getName();
				
				log.warning("BEFORE if((!isChainPartType(editor)) || ((isChainPartType(editor) && isChainControl)))");
				log.warning("isChainPartType(editor)) : " + isChainPartType(editor) + " isChainControl: " + isChainControl);
				log.warning("editor: " + editor.toString());
				if((!isChainPartType(editor)) || ((isChainPartType(editor) && isChainControl)))
				{
					productPartSet[i].setMBLDLineProductInstance(m_MbldLineProductsetInstanceID, value, true);
				}
				else if(isChainPartType(editor) && !isChainControl)
				{
					//Don't save or create new and delete existing if it's chain control and a chain part type
					productPartSet[i].setMBLDLineProductInstance(m_MbldLineProductsetInstanceID, value, false);
				}
				if(editor.getId().equalsIgnoreCase("linkBox") && !isLink)
				{
					//Don't save or create new and delete existing if it's the link bracker listbox and it's not a link
					productPartSet[i].setMBLDLineProductInstance(m_MbldLineProductsetInstanceID, value, false);
				}
				if(editor.getId().equalsIgnoreCase("bracketBox") && isLink)
				{
					//Don't save or create new and delete existing if it's bracket and is a link
					productPartSet[i].setMBLDLineProductInstance(m_MbldLineProductsetInstanceID, value, false);
				}
				
			//}
				
			m_changed = true;
		}	//	for all attributes
		m_MbldLineProductsetInstanceID = m_masi.getBLD_Line_ProductSetInstance_ID();
		m_M_AttributeSetInstanceName = m_masi.getDescription();
		//
		if (mandatory.length() > 0)
		{
			FDialog.error(m_WindowNo, this, "FillMandatory", mandatory);
			return false;
		}
		//	Save Model
		else if (m_changed)
		{
			log.warning("--------WBldPartsDialog.saveSelection() --- m_changed == true");
			m_masi.setDescription (m_M_Product_ID);
			m_masi.saveEx();
		}
		return true;
	}	//	saveSelection

	
	private boolean isChainPartType(Listbox editor) {
		log.warning("---------In WBldPartsDialog.isChainPartType() chainArray: " + chainArray.toString());
		for(Listbox lBox : chainArray)
		{
			if(lBox == editor)
			{
				log.warning("---------WBldPartsDialog.isChainPartType() - Listbox: " + lBox.toString() + " is a chain parttype");
				return true;
			}
				
		}
		return false;
	}	//isChainPartType

	/**************************************************************************
	 * 	Get Instance ID
	 * 	@return Instance ID
	 */
	public int getM_AttributeSetInstance_ID()
	{
		return m_MbldLineProductsetInstanceID;
	}	//	getM_AttributeSetInstance_ID

	/**
	 * 	Get Instance Name
	 * 	@return Instance Name
	 */
	public String getM_AttributeSetInstanceName()
	{
		return m_M_AttributeSetInstanceName;
	}	//	getM_AttributeSetInstanceName
	
	/**
	 * Get Locator ID
	 * @return M_Locator_ID
	 */
	public int getM_Locator_ID()
	{
		return m_M_Locator_ID; 
	}

	/**
	 * 	Value Changed
	 *	@return true if changed
	 */
	public boolean isChanged()
	{
		return m_changed;
	}	//	isChanged

	/**
	 * Sets chain related editors to not enabled if the control mech is not a chain drive
	 */
	private void setChainControlFlag() 
	{
		log.warning("--------In WBldPartsDialog.setChainControlFlag()");
		ListItem item = getControlId(ctrlBox);
		if(item == null)
		{
			log.warning("--------In WBldPartsDialog.setChainControlFlag() isChainControl = false");
			isChainControl = false;
			return;
		}
		
		log.warning("---------setChainControlFlag(): " + item.toString());
		MProduct value = item != null ? (MProduct)item.getValue() : null;
		int controlID = value.get_ID();
		if(controlID > 0)
		{
			MProduct product = new MProduct(Env.getCtx(), controlID, null);
			int prodID = product.get_ID();
			String attribute = (String) MtmUtils.getMattributeInstanceValue(prodID, IS_CHAIN_CONTROL, null);
			if(attribute.equalsIgnoreCase("Yes")) 
			{
				log.warning("--------In WBldPartsDialog.setChainControlFlag()--- isChainControl = true;");
				isChainControl = true;
			}
			else
			{
				log.warning("--------In WBldPartsDialog.setChainControlFlag()--- isChainControl = false;");
				isChainControl = false;
			}
		}
		
		}
	
	private void setIsLinkControlFlag() {
		
		log.warning("--------In WBldPartsDialog.setIsLinkControlFlag()");
		ListItem ctrlItem = getControlId(ctrlBox);
		ListItem nonCtrlItem = getControlId(nCtrlBox);
		if(ctrlItem != null && nonCtrlItem != null)
		{
			log.warning("--------In WBldPartsDialog.setIsLinkControlFlag()");
		}
		else
		{
			log.warning("--------In WBldPartsDialog.setIsLinkControlFlag() setIsLinkControl = false");
			isLink = false;
			return;
		}
		
		log.warning("---------setIsLinkControlFlag(): " + ctrlItem.toString());
		MProduct value = ctrlItem != null ? (MProduct)ctrlItem.getValue() : null;
		MProduct ncValue = nonCtrlItem != null ? (MProduct)nonCtrlItem.getValue() : null;
		int controlID = value.get_ID();
		int nonControlID = ncValue.get_ID();
		if(controlID > 0 && nonControlID > 0)
		{
			//MProduct product = new MProduct(Env.getCtx(), controlID, null);
			int prodID = value.get_ID();
			int ncProdID = ncValue.get_ID();
			String attribute = (String) MtmUtils.getMattributeInstanceValue(prodID, IS_LINK, null);
			String ncAttribute = (String) MtmUtils.getMattributeInstanceValue(ncProdID, IS_LINK, null);
			if(attribute != null && attribute.equalsIgnoreCase("Yes")) 
			{
				log.warning("--------In WBldPartsDialog.setIsLinkControlFlag() setIsLinkControl = true;");
				isLink = true;
			}
			else
			{
				log.warning("--------In WBldPartsDialog.setIsLinkControlFlag() setIsLinkControl = false;");
				isLink = false;
			}
			/*
			if(ncAttribute != null && ncAttribute.equalsIgnoreCase("Yes"))
			{
				log.warning("--------In WBldPartsDialog.setIsLinkControlFlag() setIsIdleLink = true;");
				isIdleLink = true;
			}
			else
			{
				log.warning("--------In WBldPartsDialog.setIsLinkControlFlag() setIsIdleLink = false;");
				isIdleLink = false;
			}
			*/
		}
	}

	private void setChainEditorsActive() {
		log.warning("--------In WBldPartsDialog.setActive()");
		setChainControlFlag();
		
		for(Listbox box : chainArray)
		{
			box.setEnabled(isChainControl);
		}
	}
	
	private void setBracketEditorActive() {
		log.warning("--------In WBldPartsDialog.setBracketEditorActive()");
		setIsLinkControlFlag();
		
		log.warning("--------In WBldPartsDialog.setBracketEditorActive()... isLink = " + isLink);
		
		if(bracketBox != null) 
		{
			bracketBox.setEnabled(!isLink);
			log.warning("--------In WBldPartsDialog.setBracketEditorActive()... bracketBox = " + bracketBox.toString());
		}
		else
		{
			log.warning("--------In WBldPartsDialog.setBracketEditorActive()... bracketBox = NULL - check partype Roller bracket = 'Roller bracket' exactly");
		}
		if(linkBox != null)
		{
			linkBox.setEnabled(isLink);
			log.warning("--------In WBldPartsDialog.setBracketEditorActive()... linkBox = " + linkBox.toString());
		}
		else
		{
			log.warning("--------In WBldPartsDialog.setBracketEditorActive()... linkBox = NULL - check partype Roller link bracket = 'Roller link bracket' exactly");
		}
		
	}
	
	private MProduct[] modifyDualTypes(MProduct[] values, boolean isDual)
	{
		ArrayList<MProduct> modifiedList = new ArrayList<MProduct>();
		for(int j = 0; j< values.length; j++)
		{
			String isProductDual = (String) MtmUtils.getMattributeInstanceValue(values[j].getM_Product_ID(), MtmUtils.MTM_IS_DUAL, null);
			if(isProductDual != null)
			{
				if(isDual && (isProductDual.equalsIgnoreCase("Yes")||isProductDual.equalsIgnoreCase("Both")))
				{
					modifiedList.add(values[j]);
				}
				else if(!isDual && (isProductDual.equalsIgnoreCase("No")||isProductDual.equalsIgnoreCase("Both")))
				{
					modifiedList.add(values[j]);
				}
			}
			else
			{
				MProduct noIsDualAttribute = new MProduct(Env.getCtx(), values[j].getM_Product_ID(), null);
				throw new AdempiereUserError("No Is Dual Roller attribute found for BOM Product: " + noIsDualAttribute.getName() 
				+ ". Check this product's attribute setup before continuing.");
			}
		}
		
		MProduct[] retValues = new MProduct[modifiedList.size()];
		return modifiedList.toArray(retValues);
	}
	
	
	/**
	 * Deprecate/delete
	 * @param editor
	 * @param partTypeID
	 */
	private void resetEditor(Listbox editor, int partTypeID) {
		
		MProduct[] values = MBLDProductPartType.getPartSetProducts(m_M_Product_ID, partTypeID, null);
		values = modifyDualTypes(values, isDualRoller);
		editor.removeAllItems();
		for (MProduct value : values) 
		{
			ListItem item = new ListItem(value != null ? value.getName() : "", value);
			editor.appendChild(item);
		}
		
	}
	 private void setIsDualFlag(int mbldpartTypeID) {
		 MBLDProductPartType controlPart = new MBLDProductPartType(Env.getCtx(), mbldpartTypeID, null);
		 MBLDLineProductInstance controlInstance = controlPart.getMBldLineProductInstance(m_MbldLineProductsetInstanceID);
		 if(controlInstance != null)//will default to false if null
		 {
			 
			 int intialControlID = controlInstance.getM_Product_ID();
			 String isDualCtrl = (String) MtmUtils.getMattributeInstanceValue(intialControlID, MtmUtils.MTM_IS_DUAL, null);
			 if(isDualCtrl != null)
			 {
				 if(isDualCtrl.equalsIgnoreCase("Yes"))
				 {
					 isDualRoller = true;
				 }
				 else
				 {
					 isDualRoller = false;
				 }
			 }
		 }
	 }
	
} //	WPAttributeDialog

