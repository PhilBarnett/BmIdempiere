/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package au.blindmot.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for BLD_mtom_bomderived
 *  @author iDempiere (generated) 
 *  @version Release 4.1 - $Id$ */
public class X_BLD_mtom_bomderived extends PO implements I_BLD_mtom_bomderived, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20170827L;

    /** Standard Constructor */
    public X_BLD_mtom_bomderived (Properties ctx, int BLD_mtom_bomderived_ID, String trxName)
    {
      super (ctx, BLD_mtom_bomderived_ID, trxName);
      /** if (BLD_mtom_bomderived_ID == 0)
        {
			setbld_mtom_bomderived_ID (0);
			setbld_mtom_item_line_ID (0);
// @bld_mtom_item_line_ID@
			setM_Product_ID (0);
        } */
    }

    /** Load Constructor */
    public X_BLD_mtom_bomderived (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_BLD_mtom_bomderived[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set attribute_name.
		@param attribute_name attribute_name	  */
	public void setattribute_name (String attribute_name)
	{
		set_Value (COLUMNNAME_attribute_name, attribute_name);
	}

	/** Get attribute_name.
		@return attribute_name	  */
	public String getattribute_name () 
	{
		return (String)get_Value(COLUMNNAME_attribute_name);
	}

	/** Set BLD made to measure BOM derived.
		@param bld_mtom_bomderived_ID BLD made to measure BOM derived	  */
	public void setbld_mtom_bomderived_ID (int bld_mtom_bomderived_ID)
	{
		if (bld_mtom_bomderived_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_bld_mtom_bomderived_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_bld_mtom_bomderived_ID, Integer.valueOf(bld_mtom_bomderived_ID));
	}

	/** Get BLD made to measure BOM derived.
		@return BLD made to measure BOM derived	  */
	public int getbld_mtom_bomderived_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_bld_mtom_bomderived_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set bld_mtom_bomderived_UU.
		@param bld_mtom_bomderived_UU bld_mtom_bomderived_UU	  */
	public void setbld_mtom_bomderived_UU (String bld_mtom_bomderived_UU)
	{
		set_ValueNoCheck (COLUMNNAME_bld_mtom_bomderived_UU, bld_mtom_bomderived_UU);
	}

	/** Get bld_mtom_bomderived_UU.
		@return bld_mtom_bomderived_UU	  */
	public String getbld_mtom_bomderived_UU () 
	{
		return (String)get_Value(COLUMNNAME_bld_mtom_bomderived_UU);
	}

	public I_BLD_Mtom_Item_Line getbld_mtom_item_line() throws RuntimeException
    {
		return (I_BLD_Mtom_Item_Line)MTable.get(getCtx(), I_BLD_Mtom_Item_Line.Table_Name)
			.getPO(getbld_mtom_item_line_ID(), get_TrxName());	}

	/** Set Made to measure items.
		@param bld_mtom_item_line_ID Made to measure items	  */
	public void setbld_mtom_item_line_ID (int bld_mtom_item_line_ID)
	{
		if (bld_mtom_item_line_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_bld_mtom_item_line_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_bld_mtom_item_line_ID, Integer.valueOf(bld_mtom_item_line_ID));
	}

	/** Get Made to measure items.
		@return Made to measure items	  */
	public int getbld_mtom_item_line_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_bld_mtom_item_line_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_Attribute getM_Attribute() throws RuntimeException
    {
		return (org.compiere.model.I_M_Attribute)MTable.get(getCtx(), org.compiere.model.I_M_Attribute.Table_Name)
			.getPO(getM_Attribute_ID(), get_TrxName());	}

	/** Set Attribute.
		@param M_Attribute_ID 
		Product Attribute
	  */
	public void setM_Attribute_ID (int M_Attribute_ID)
	{
		if (M_Attribute_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_Attribute_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_Attribute_ID, Integer.valueOf(M_Attribute_ID));
	}

	/** Get Attribute.
		@return Product Attribute
	  */
	public int getM_Attribute_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Attribute_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_Product getM_Product() throws RuntimeException
    {
		return (org.compiere.model.I_M_Product)MTable.get(getCtx(), org.compiere.model.I_M_Product.Table_Name)
			.getPO(getM_Product_ID(), get_TrxName());	}

	/** Set Product.
		@param M_Product_ID 
		Product, Service, Item
	  */
	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1) 
			set_Value (COLUMNNAME_M_Product_ID, null);
		else 
			set_Value (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
	}

	/** Get Product.
		@return Product, Service, Item
	  */
	public int getM_Product_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Quantity.
		@param Qty 
		Quantity
	  */
	public void setQty (BigDecimal Qty)
	{
		set_Value (COLUMNNAME_Qty, Qty);
	}

	/** Get Quantity.
		@return Quantity
	  */
	public BigDecimal getQty () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Qty);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public String getDescription() {
		return (String)get_Value(COLUMNNAME_Description);
	}

	public void setDescription(String description) {
		set_Value(COLUMNNAME_Description, description);
		
	}
	
	/**
	 * setMBOMProductID.
	 * @param mBOMProductID
	 */
public void setMProductBomID (int mBOMProductID)
{
	set_Value (COLUMNNAME_M_Product_BOM_ID, Integer.valueOf(mBOMProductID));
}

/** Get setMBOMProductID.
	@return attributesetinstance_id	  */
public int getMBOMProductID() 
{
	Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_BOM_ID);
	if (ii == null)
		 return 0;
	return ii.intValue();
}

public I_M_AttributeSetInstance getM_AttributeSetInstance() throws RuntimeException
{
	return (I_M_AttributeSetInstance)MTable.get(getCtx(), I_M_AttributeSetInstance.Table_Name)
		.getPO(getM_AttributeSetInstance_ID(), get_TrxName());	}

/** Set Attribute Set Instance.
	@param M_AttributeSetInstance_ID 
	Product Attribute Set Instance
  */
public void setM_AttributeSetInstance_ID (int M_AttributeSetInstance_ID)
{
	if (M_AttributeSetInstance_ID < 0) 
		set_Value (COLUMNNAME_M_AttributeSetInstance_ID, null);
	else 
		set_Value (COLUMNNAME_M_AttributeSetInstance_ID, Integer.valueOf(M_AttributeSetInstance_ID));
}

/** Get Attribute Set Instance.
	@return Product Attribute Set Instance
  */
public int getM_AttributeSetInstance_ID () 
{
	Integer ii = (Integer)get_Value(COLUMNNAME_M_AttributeSetInstance_ID);
	if (ii == null)
		 return 0;
	return ii.intValue();
}


}