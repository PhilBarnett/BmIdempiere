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

/** Generated Model for BLD_MTM_Product_Bom_Add
 *  @author iDempiere (generated) 
 *  @version Release 6.2 - $Id$ */
public class X_BLD_MTM_Product_Bom_Add extends PO implements I_BLD_MTM_Product_Bom_Add, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190906L;

    /** Standard Constructor */
    public X_BLD_MTM_Product_Bom_Add (Properties ctx, int BLD_MTM_Product_Bom_Add_ID, String trxName)
    {
      super (ctx, BLD_MTM_Product_Bom_Add_ID, trxName);
      /** if (BLD_MTM_Product_Bom_Add_ID == 0)
        {
			setBLD_MTM_Product_Bom_Add_ID (0);
			setBLD_MTM_Product_Bom_Trigger_ID (0);
			setLine (0);
// @SQL=SELECT COALESCE(MAX(Line),0)+10 AS DefaultValue FROM BLD_MTM_Product_Bom_Add WHERE BLD_MTM_Product_Bom_Trigger_ID=@BLD_MTM_Product_Bom_Trigger_ID@
			setM_Product_BOM_ID (0);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_BLD_MTM_Product_Bom_Add (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 7 - System - Client - Org 
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
      StringBuffer sb = new StringBuffer ("X_BLD_MTM_Product_Bom_Add[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Product BomDerived to Add.
		@param BLD_MTM_Product_Bom_Add_ID Product BomDerived to Add	  */
	public void setBLD_MTM_Product_Bom_Add_ID (int BLD_MTM_Product_Bom_Add_ID)
	{
		if (BLD_MTM_Product_Bom_Add_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_MTM_Product_Bom_Add_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_MTM_Product_Bom_Add_ID, Integer.valueOf(BLD_MTM_Product_Bom_Add_ID));
	}

	/** Get Product BomDerived to Add.
		@return Product BomDerived to Add	  */
	public int getBLD_MTM_Product_Bom_Add_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_MTM_Product_Bom_Add_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BLD_MTM_Product_Bom_Add_UU.
		@param BLD_MTM_Product_Bom_Add_UU BLD_MTM_Product_Bom_Add_UU	  */
	public void setBLD_MTM_Product_Bom_Add_UU (String BLD_MTM_Product_Bom_Add_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_MTM_Product_Bom_Add_UU, BLD_MTM_Product_Bom_Add_UU);
	}

	/** Get BLD_MTM_Product_Bom_Add_UU.
		@return BLD_MTM_Product_Bom_Add_UU	  */
	public String getBLD_MTM_Product_Bom_Add_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_MTM_Product_Bom_Add_UU);
	}
	
	/** Get PP_Product_Bomline_ID.
	@return PP_Product_Bomline_ID	  */
public int getPP_Product_Bomline_ID () 
{
	Integer ii = (Integer)get_Value(COLUMNNAME_PP_Product_Bomline_ID);
	if (ii == null)
		 return 0;
	return ii.intValue();
}

/** Set PP_Product_Bomline_ID.
@param PP_Product_Bomline_ID PP_Product_Bomline_ID	  */
public void setPP_Product_Bomline_ID (int PP_Product_Bomline_ID)
{
if (PP_Product_Bomline_ID < 1) 
	set_ValueNoCheck (COLUMNNAME_PP_Product_Bomline_ID, null);
else 
	set_ValueNoCheck (COLUMNNAME_PP_Product_Bomline_ID, Integer.valueOf(PP_Product_Bomline_ID));
}

	public I_BLD_MTM_Product_Bom_Trigger getBLD_MTM_Product_Bom_Trigger() throws RuntimeException
    {
		return (I_BLD_MTM_Product_Bom_Trigger)MTable.get(getCtx(), I_BLD_MTM_Product_Bom_Trigger.Table_Name)
			.getPO(getBLD_MTM_Product_Bom_Trigger_ID(), get_TrxName());	}

	/** Set BLD_MTM_Product_Bom_Trigger_ID.
		@param BLD_MTM_Product_Bom_Trigger_ID BLD_MTM_Product_Bom_Trigger_ID	  */
	public void setBLD_MTM_Product_Bom_Trigger_ID (int BLD_MTM_Product_Bom_Trigger_ID)
	{
		if (BLD_MTM_Product_Bom_Trigger_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_MTM_Product_Bom_Trigger_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_MTM_Product_Bom_Trigger_ID, Integer.valueOf(BLD_MTM_Product_Bom_Trigger_ID));
	}

	/** Get BLD_MTM_Product_Bom_Trigger_ID.
		@return BLD_MTM_Product_Bom_Trigger_ID	  */
	public int getBLD_MTM_Product_Bom_Trigger_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_MTM_Product_Bom_Trigger_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Comment/Help.
		@param Help 
		Comment or Hint
	  */
	public void setHelp (String Help)
	{
		set_Value (COLUMNNAME_Help, Help);
	}

	/** Get Comment/Help.
		@return Comment or Hint
	  */
	public String getHelp () 
	{
		return (String)get_Value(COLUMNNAME_Help);
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

	public org.compiere.model.I_M_Product_BOM getM_Product_BOM() throws RuntimeException
    {
		return (org.compiere.model.I_M_Product_BOM)MTable.get(getCtx(), org.compiere.model.I_M_Product_BOM.Table_Name)
			.getPO(getM_Product_BOM_ID(), get_TrxName());	}

	/** Set BOM Line.
		@param M_Product_BOM_ID BOM Line	  */
	public void setM_Product_BOM_ID (int M_Product_BOM_ID)
	{
		if (M_Product_BOM_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_Product_BOM_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_Product_BOM_ID, Integer.valueOf(M_Product_BOM_ID));
	}

	/** Get BOM Line.
		@return BOM Line	  */
	public int getM_Product_BOM_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_BOM_ID);
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