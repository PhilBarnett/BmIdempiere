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

/** Generated Model for BLD_Mtm_Scan
 *  @author iDempiere (generated) 
 *  @version Release 6.1 - $Id$ */
public class X_BLD_Mtm_Scan extends PO implements I_BLD_Mtm_Scan, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190131L;

    /** Standard Constructor */
    public X_BLD_Mtm_Scan (Properties ctx, int BLD_Mtm_Scan_ID, String trxName)
    {
      super (ctx, BLD_Mtm_Scan_ID, trxName);
      /** if (BLD_Mtm_Scan_ID == 0)
        {
			setAD_User_ID (0);
			setbarcode (null);
			setBLD_Mtm_Scan_ID (0);
			setIsError (false);
			setisprocessed (false);
			setLine (0);
        } */
    }

    /** Load Constructor */
    public X_BLD_Mtm_Scan (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_BLD_Mtm_Scan[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_User getAD_User() throws RuntimeException
    {
		return (org.compiere.model.I_AD_User)MTable.get(getCtx(), org.compiere.model.I_AD_User.Table_Name)
			.getPO(getAD_User_ID(), get_TrxName());	}

	/** Set User/Contact.
		@param AD_User_ID 
		User within the system - Internal or Business Partner Contact
	  */
	public void setAD_User_ID (int AD_User_ID)
	{
		if (AD_User_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_AD_User_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_AD_User_ID, Integer.valueOf(AD_User_ID));
	}

	/** Get User/Contact.
		@return User within the system - Internal or Business Partner Contact
	  */
	public int getAD_User_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_User_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	/** Set BLD_Mtm_Scan.
		@param BLD_Mtm_Scan_ID BLD_Mtm_Scan	  */
	public void setBLD_Mtm_Scan_ID (int BLD_Mtm_Scan_ID)
	{
		if (BLD_Mtm_Scan_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_Mtm_Scan_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_Mtm_Scan_ID, Integer.valueOf(BLD_Mtm_Scan_ID));
	}

	/** Get BLD_Mtm_Scan.
		@return BLD_Mtm_Scan	  */
	public int getBLD_Mtm_Scan_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_Mtm_Scan_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_BLD_Mtm_Scanpoint getBLD_Mtm_Scanpoint() throws RuntimeException
    {
		return (I_BLD_Mtm_Scanpoint)MTable.get(getCtx(), I_BLD_Mtm_Scanpoint.Table_Name)
			.getPO(getBLD_Mtm_Scanpoint_ID(), get_TrxName());	}

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

	/** Set BLD_Mtm_Scan_UU.
		@param BLD_Mtm_Scan_UU BLD_Mtm_Scan_UU	  */
	public void setBLD_Mtm_Scan_UU (String BLD_Mtm_Scan_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_Mtm_Scan_UU, BLD_Mtm_Scan_UU);
	}

	/** Get BLD_Mtm_Scan_UU.
		@return BLD_Mtm_Scan_UU	  */
	public String getBLD_Mtm_Scan_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_Mtm_Scan_UU);
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

	/** Set Error.
		@param IsError 
		An Error occurred in the execution
	  */
	public void setIsError (boolean IsError)
	{
		set_Value (COLUMNNAME_IsError, Boolean.valueOf(IsError));
	}

	/** Get Error.
		@return An Error occurred in the execution
	  */
	public boolean isError () 
	{
		Object oo = get_Value(COLUMNNAME_IsError);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set isprocessed.
		@param isprocessed isprocessed	  */
	public void setisprocessed (boolean isprocessed)
	{
		set_Value (COLUMNNAME_isprocessed, Boolean.valueOf(isprocessed));
	}

	/** Get isprocessed.
		@return isprocessed	  */
	public boolean isprocessed () 
	{
		Object oo = get_Value(COLUMNNAME_isprocessed);
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