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
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for BLD_mtom_item_line
 *  @author iDempiere (generated) 
 *  @version Release 4.1 - $Id$ */
public class X_BLD_mtom_item_line extends PO implements I_BLD_mtom_item_line, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20170827L;

    /** Standard Constructor */
    public X_BLD_mtom_item_line (Properties ctx, int BLD_mtom_item_line_ID, String trxName)
    {
      super (ctx, BLD_mtom_item_line_ID, trxName);
      /** if (BLD_mtom_item_line_ID == 0)
        {
			setbld_mtom_item_line_ID (0);
			setbld_mtom_production_ID (0);
// @bld_mtom_production_ID@
			setIsCreated (null);
// N
			setM_Product_ID (0);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_BLD_mtom_item_line (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_BLD_mtom_item_line[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set barcode.
		@param barcode barcode	  */
	public void setbarcode (String barcode)
	{
		set_Value (COLUMNNAME_barcode, barcode);
	}

	/** Get barcode.
		@return barcode	  */
	public String getbarcode () 
	{
		return (String)get_Value(COLUMNNAME_barcode);
	}

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

	/** Set bld_mtom_item_line_UU.
		@param bld_mtom_item_line_UU bld_mtom_item_line_UU	  */
	public void setbld_mtom_item_line_UU (String bld_mtom_item_line_UU)
	{
		set_ValueNoCheck (COLUMNNAME_bld_mtom_item_line_UU, bld_mtom_item_line_UU);
	}

	/** Get bld_mtom_item_line_UU.
		@return bld_mtom_item_line_UU	  */
	public String getbld_mtom_item_line_UU () 
	{
		return (String)get_Value(COLUMNNAME_bld_mtom_item_line_UU);
	}

	public I_BLD_mtom_production getbld_mtom_production() throws RuntimeException
    {
		return (I_BLD_mtom_production)MTable.get(getCtx(), I_BLD_mtom_production.Table_Name)
			.getPO(getbld_mtom_production_ID(), get_TrxName());	}

	/** Set Made to measure production.
		@param bld_mtom_production_ID Made to measure production	  */
	public void setbld_mtom_production_ID (int bld_mtom_production_ID)
	{
		if (bld_mtom_production_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_bld_mtom_production_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_bld_mtom_production_ID, Integer.valueOf(bld_mtom_production_ID));
	}

	/** Get Made to measure production.
		@return Made to measure production	  */
	public int getbld_mtom_production_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_bld_mtom_production_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set completed.
		@param completed completed	  */
	public void setcompleted (Timestamp completed)
	{
		set_Value (COLUMNNAME_completed, completed);
	}

	/** Get completed.
		@return completed	  */
	public Timestamp getcompleted () 
	{
		return (Timestamp)get_Value(COLUMNNAME_completed);
	}

	public org.compiere.model.I_C_OrderLine getC_OrderLine() throws RuntimeException
    {
		return (org.compiere.model.I_C_OrderLine)MTable.get(getCtx(), org.compiere.model.I_C_OrderLine.Table_Name)
			.getPO(getC_OrderLine_ID(), get_TrxName());	}

	/** Set Sales Order Line.
		@param C_OrderLine_ID 
		Sales Order Line
	  */
	public void setC_OrderLine_ID (int C_OrderLine_ID)
	{
		if (C_OrderLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_OrderLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_OrderLine_ID, Integer.valueOf(C_OrderLine_ID));
	}

	/** Get Sales Order Line.
		@return Sales Order Line
	  */
	public int getC_OrderLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_OrderLine_ID);
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

	/** Set Complete.
		@param IsComplete 
		It is complete
	  */
	public void setIsComplete (boolean IsComplete)
	{
		set_Value (COLUMNNAME_IsComplete, Boolean.valueOf(IsComplete));
	}

	/** Get Complete.
		@return It is complete
	  */
	public boolean isComplete () 
	{
		Object oo = get_Value(COLUMNNAME_IsComplete);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** IsCreated AD_Reference_ID=319 */
	public static final int ISCREATED_AD_Reference_ID=319;
	/** Yes = Y */
	public static final String ISCREATED_Yes = "Y";
	/** No = N */
	public static final String ISCREATED_No = "N";
	/** Set Records created.
		@param IsCreated Records created	  */
	public void setIsCreated (String IsCreated)
	{

		set_Value (COLUMNNAME_IsCreated, IsCreated);
	}

	/** Get Records created.
		@return Records created	  */
	public String getIsCreated () 
	{
		return (String)get_Value(COLUMNNAME_IsCreated);
	}

	/** Set isrework.
		@param isrework isrework	  */
	public void setisrework (boolean isrework)
	{
		set_Value (COLUMNNAME_isrework, Boolean.valueOf(isrework));
	}

	/** Get isrework.
		@return isrework	  */
	public boolean isrework () 
	{
		Object oo = get_Value(COLUMNNAME_isrework);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	public I_M_Locator getM_Locator() throws RuntimeException
    {
		return (I_M_Locator)MTable.get(getCtx(), I_M_Locator.Table_Name)
			.getPO(getM_Locator_ID(), get_TrxName());	}

	/** Set Locator.
		@param M_Locator_ID 
		Warehouse Locator
	  */
	public void setM_Locator_ID (int M_Locator_ID)
	{
		if (M_Locator_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_Locator_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_Locator_ID, Integer.valueOf(M_Locator_ID));
	}

	/** Get Locator.
		@return Warehouse Locator
	  */
	public int getM_Locator_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Locator_ID);
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
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue () 
	{
		return (String)get_Value(COLUMNNAME_Value);
	}
}