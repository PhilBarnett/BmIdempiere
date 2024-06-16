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

/** Generated Model for BLD_Line_ProductSetInstance
 *  @author iDempiere (generated) 
 *  @version Release 5.1 - $Id$ */
public class X_BLD_Line_ProductSetInstance extends PO implements I_BLD_Line_ProductSetInstance, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20180823L;

    /** Standard Constructor */
    public X_BLD_Line_ProductSetInstance (Properties ctx, int BLD_Line_ProductSetInstance_ID, String trxName)
    {
      super (ctx, BLD_Line_ProductSetInstance_ID, trxName);
      /** if (BLD_Line_ProductSetInstance_ID == 0)
        {
			setBLD_Line_ProductSetInstance_ID (0);
        } */
    }

    /** Load Constructor */
    public X_BLD_Line_ProductSetInstance (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_BLD_Line_ProductSetInstance[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set BLD Line ProductSetInstance.
		@param BLD_Line_ProductSetInstance_ID BLD Line ProductSetInstance	  */
	public void setBLD_Line_ProductSetInstance_ID (int BLD_Line_ProductSetInstance_ID)
	{
		if (BLD_Line_ProductSetInstance_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_Line_ProductSetInstance_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_Line_ProductSetInstance_ID, Integer.valueOf(BLD_Line_ProductSetInstance_ID));
	}

	/** Get BLD Line ProductSetInstance.
		@return BLD Line ProductSetInstance	  */
	public int getBLD_Line_ProductSetInstance_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_Line_ProductSetInstance_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BLD_Line_ProductSetInstance_UU.
		@param BLD_Line_ProductSetInstance_UU BLD_Line_ProductSetInstance_UU	  */
	public void setBLD_Line_ProductSetInstance_UU (String BLD_Line_ProductSetInstance_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_Line_ProductSetInstance_UU, BLD_Line_ProductSetInstance_UU);
	}

	/** Get BLD_Line_ProductSetInstance_UU.
		@return BLD_Line_ProductSetInstance_UU	  */
	public String getBLD_Line_ProductSetInstance_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_Line_ProductSetInstance_UU);
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
}