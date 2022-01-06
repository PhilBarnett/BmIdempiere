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

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for BLD_Mtom_Item_Detail
 *  @author iDempiere (generated) 
 *  @version Release 8.2 - $Id$ */
public class X_BLD_Mtom_Item_Detail extends PO implements I_BLD_Mtom_Item_Detail, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20210905L;

    /** Standard Constructor */
    public X_BLD_Mtom_Item_Detail (Properties ctx, int BLD_Mtom_Item_Detail_ID, String trxName)
    {
      super (ctx, BLD_Mtom_Item_Detail_ID, trxName);
      /** if (BLD_Mtom_Item_Detail_ID == 0)
        {
			setBLD_Mtom_Item_Detail_ID (0);
			setbld_mtom_item_line_ID (0);
			setLine (0);
// @SQL=SELECT COALESCE(MAX(Line),0)+10 AS DefaultValue FROM C_OrderLine WHERE C_Order_ID=@C_Order_ID@
			setName (null);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_BLD_Mtom_Item_Detail (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_BLD_Mtom_Item_Detail[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	/** Set Made to measure item detail.
		@param BLD_Mtom_Item_Detail_ID Made to measure item detail	  */
	public void setBLD_Mtom_Item_Detail_ID (int BLD_Mtom_Item_Detail_ID)
	{
		if (BLD_Mtom_Item_Detail_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_Mtom_Item_Detail_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_Mtom_Item_Detail_ID, Integer.valueOf(BLD_Mtom_Item_Detail_ID));
	}

	/** Get Made to measure item detail.
		@return Made to measure item detail	  */
	public int getBLD_Mtom_Item_Detail_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_Mtom_Item_Detail_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BLD_Mtom_Item_Detail_UU.
		@param BLD_Mtom_Item_Detail_UU BLD_Mtom_Item_Detail_UU	  */
	public void setBLD_Mtom_Item_Detail_UU (String BLD_Mtom_Item_Detail_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_Mtom_Item_Detail_UU, BLD_Mtom_Item_Detail_UU);
	}

	/** Get BLD_Mtom_Item_Detail_UU.
		@return BLD_Mtom_Item_Detail_UU	  */
	public String getBLD_Mtom_Item_Detail_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_Mtom_Item_Detail_UU);
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

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Description_other.
		@param description2 Description2	  */
	public void setdescriptionOther (String description2)
	{
		set_Value (COLUMNNAME_description2, description2);
	}

	/** Get Description2.
		@return Description2	  */
	public String getdescriptionOther () 
	{
		return (String)get_Value(COLUMNNAME_description2);
	}

	/** Set Line No.
		@param Line 
		Unique line for this document
	  */
	public void setLine (int Line)
	{
		set_ValueNoCheck (COLUMNNAME_Line, Integer.valueOf(Line));
	}

	/** Get Line No.
		@return Unique line for this document
	  */
	public int getLine () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Line);
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

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

	
	/** Set Search Key.
		@param Value 
		Search key for the record in the format required - must be unique
	  */
/*	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}
*/
	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
/*	public String getValue () 
	{
		return (String)get_Value(COLUMNNAME_Value);
	}*/
}