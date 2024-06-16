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

/** Generated Model for BLD_Mtm_Scanpoint
 *  @author iDempiere (generated) 
 *  @version Release 6.1 - $Id$ */
public class X_BLD_Mtm_Scanpoint extends PO implements I_BLD_Mtm_Scanpoint, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190131L;

    /** Standard Constructor */
    public X_BLD_Mtm_Scanpoint (Properties ctx, int BLD_Mtm_Scanpoint_ID, String trxName)
    {
      super (ctx, BLD_Mtm_Scanpoint_ID, trxName);
      /** if (BLD_Mtm_Scanpoint_ID == 0)
        {
			setBLD_Mtm_Scanpoint_ID (0);
			setisendpoint (false);
			setLine (0);
        } */
    }

    /** Load Constructor */
    public X_BLD_Mtm_Scanpoint (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 4 - System 
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
      StringBuffer sb = new StringBuffer ("X_BLD_Mtm_Scanpoint[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set BLD_Mtm_Scanpoint.
		@param BLD_Mtm_Scanpoint_ID BLD_Mtm_Scanpoint	  */
	public void setBLD_Mtm_Scanpoint_ID (int BLD_Mtm_Scanpoint_ID)
	{
		if (BLD_Mtm_Scanpoint_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_Mtm_Scanpoint_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_Mtm_Scanpoint_ID, Integer.valueOf(BLD_Mtm_Scanpoint_ID));
	}

	/** Get BLD_Mtm_Scanpoint.
		@return BLD_Mtm_Scanpoint	  */
	public int getBLD_Mtm_Scanpoint_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_Mtm_Scanpoint_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BLD_Mtm_Scanpoint_UU.
		@param BLD_Mtm_Scanpoint_UU BLD_Mtm_Scanpoint_UU	  */
	public void setBLD_Mtm_Scanpoint_UU (String BLD_Mtm_Scanpoint_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_Mtm_Scanpoint_UU, BLD_Mtm_Scanpoint_UU);
	}

	/** Get BLD_Mtm_Scanpoint_UU.
		@return BLD_Mtm_Scanpoint_UU	  */
	public String getBLD_Mtm_Scanpoint_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_Mtm_Scanpoint_UU);
	}

	public I_BLD_Scanpoint_Type getBLD_Scanpoint_Type() throws RuntimeException
    {
		return (I_BLD_Scanpoint_Type)MTable.get(getCtx(), I_BLD_Scanpoint_Type.Table_Name)
			.getPO(getBLD_Scanpoint_Type_ID(), get_TrxName());	}

	/** Set BLD_Scanpoint_Type.
		@param BLD_Scanpoint_Type_ID BLD_Scanpoint_Type	  */
	public void setBLD_Scanpoint_Type_ID (int BLD_Scanpoint_Type_ID)
	{
		if (BLD_Scanpoint_Type_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_Scanpoint_Type_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_Scanpoint_Type_ID, Integer.valueOf(BLD_Scanpoint_Type_ID));
	}

	/** Get BLD_Scanpoint_Type.
		@return BLD_Scanpoint_Type	  */
	public int getBLD_Scanpoint_Type_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_Scanpoint_Type_ID);
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

	/** Set isendpoint.
		@param isendpoint isendpoint	  */
	public void setisendpoint (boolean isendpoint)
	{
		set_Value (COLUMNNAME_isendpoint, Boolean.valueOf(isendpoint));
	}

	/** Get isendpoint.
		@return isendpoint	  */
	public boolean isendpoint () 
	{
		Object oo = get_Value(COLUMNNAME_isendpoint);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
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
}