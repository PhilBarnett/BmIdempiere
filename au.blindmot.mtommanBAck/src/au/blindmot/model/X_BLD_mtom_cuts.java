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

/** Generated Model for BLD_mtom_cuts
 *  @author iDempiere (generated) 
 *  @version Release 4.1 - $Id$ */
public class X_BLD_mtom_cuts extends PO implements I_BLD_mtom_cuts, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20170928L;

    /** Standard Constructor */
    public X_BLD_mtom_cuts (Properties ctx, int BLD_mtom_cuts_ID, String trxName)
    {
      super (ctx, BLD_mtom_cuts_ID, trxName);
      /** if (BLD_mtom_cuts_ID == 0)
        {
			setBLD_mtom_cuts_ID (0);
			setbld_mtom_item_line_ID (0);
			setM_Product_ID (0);
        } */
    }

    /** Load Constructor */
    public X_BLD_mtom_cuts (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_BLD_mtom_cuts[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Made to measure cuts.
		@param BLD_mtom_cuts_ID Made to measure cuts	  */
	public void setBLD_mtom_cuts_ID (int BLD_mtom_cuts_ID)
	{
		if (BLD_mtom_cuts_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_mtom_cuts_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_mtom_cuts_ID, Integer.valueOf(BLD_mtom_cuts_ID));
	}

	/** Get Made to measure cuts.
		@return Made to measure cuts	  */
	public int getBLD_mtom_cuts_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_mtom_cuts_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BLD_mtom_cuts_UU.
		@param BLD_mtom_cuts_UU BLD_mtom_cuts_UU	  */
	public void setBLD_mtom_cuts_UU (String BLD_mtom_cuts_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_mtom_cuts_UU, BLD_mtom_cuts_UU);
	}

	/** Get BLD_mtom_cuts_UU.
		@return BLD_mtom_cuts_UU	  */
	public String getBLD_mtom_cuts_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_mtom_cuts_UU);
	}

	public I_BLD_mtom_item_line getbld_mtom_item_line() throws RuntimeException
    {
		return (I_BLD_mtom_item_line)MTable.get(getCtx(), I_BLD_mtom_item_line.Table_Name)
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

	/** Set Height.
		@param Height Height	  */
	public void setHeight (BigDecimal Height)
	{
		set_Value (COLUMNNAME_Height, Height);
	}

	/** Get Height.
		@return Height	  */
	public BigDecimal getHeight () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Height);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Length.
		@param Length Length	  */
	public void setLength (BigDecimal Length)
	{
		set_Value (COLUMNNAME_Length, Length);
	}

	/** Get Length.
		@return Length	  */
	public BigDecimal getLength () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Length);
		if (bd == null)
			 return Env.ZERO;
		return bd;
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

	/** Set Width.
		@param Width Width	  */
	public void setWidth (BigDecimal Width)
	{
		set_Value (COLUMNNAME_Width, Width);
	}

	/** Get Width.
		@return Width	  */
	public BigDecimal getWidth () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Width);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}