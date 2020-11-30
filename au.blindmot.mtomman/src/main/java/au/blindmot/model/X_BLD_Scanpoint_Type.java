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

/** Generated Model for BLD_Scanpoint_Type
 *  @author iDempiere (generated) 
 *  @version Release 6.1 - $Id$ */
public class X_BLD_Scanpoint_Type extends PO implements I_BLD_Scanpoint_Type, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190131L;

    /** Standard Constructor */
    public X_BLD_Scanpoint_Type (Properties ctx, int BLD_Scanpoint_Type_ID, String trxName)
    {
      super (ctx, BLD_Scanpoint_Type_ID, trxName);
      /** if (BLD_Scanpoint_Type_ID == 0)
        {
			setBLD_Scanpoint_Type_ID (0);
			setLine (0);
        } */
    }

    /** Load Constructor */
    public X_BLD_Scanpoint_Type (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_BLD_Scanpoint_Type[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

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

	/** Set BLD_Scanpoint_Type_UU.
		@param BLD_Scanpoint_Type_UU BLD_Scanpoint_Type_UU	  */
	public void setBLD_Scanpoint_Type_UU (String BLD_Scanpoint_Type_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_Scanpoint_Type_UU, BLD_Scanpoint_Type_UU);
	}

	/** Get BLD_Scanpoint_Type_UU.
		@return BLD_Scanpoint_Type_UU	  */
	public String getBLD_Scanpoint_Type_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_Scanpoint_Type_UU);
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
}