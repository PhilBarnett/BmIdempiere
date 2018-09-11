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

/** Generated Model for BLD_Product_PartType
 *  @author iDempiere (generated) 
 *  @version Release 5.1 - $Id$ */
public class X_BLD_Product_PartType extends PO implements I_BLD_Product_PartType, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20180823L;

    /** Standard Constructor */
    public X_BLD_Product_PartType (Properties ctx, int BLD_Product_PartType_ID, String trxName)
    {
      super (ctx, BLD_Product_PartType_ID, trxName);
      /** if (BLD_Product_PartType_ID == 0)
        {
			setBLD_Product_PartType_ID (0);
			setIsMandatory (false);
			setM_Product_ID (0);
        } */
    }

    /** Load Constructor */
    public X_BLD_Product_PartType (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_BLD_Product_PartType[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set BLD Product PartType.
		@param BLD_Product_PartType_ID BLD Product PartType	  */
	public void setBLD_Product_PartType_ID (int BLD_Product_PartType_ID)
	{
		if (BLD_Product_PartType_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_Product_PartType_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_Product_PartType_ID, Integer.valueOf(BLD_Product_PartType_ID));
	}

	/** Get BLD Product PartType.
		@return BLD Product PartType	  */
	public int getBLD_Product_PartType_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_Product_PartType_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BLD_Product_PartType_UU.
		@param BLD_Product_PartType_UU BLD_Product_PartType_UU	  */
	public void setBLD_Product_PartType_UU (String BLD_Product_PartType_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_Product_PartType_UU, BLD_Product_PartType_UU);
	}

	/** Get BLD_Product_PartType_UU.
		@return BLD_Product_PartType_UU	  */
	public String getBLD_Product_PartType_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_Product_PartType_UU);
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

	/** Set Mandatory.
		@param IsMandatory 
		Data entry is required in this column
	  */
	public void setIsMandatory (boolean IsMandatory)
	{
		set_Value (COLUMNNAME_IsMandatory, Boolean.valueOf(IsMandatory));
	}

	/** Get Mandatory.
		@return Data entry is required in this column
	  */
	public boolean isMandatory () 
	{
		Object oo = get_Value(COLUMNNAME_IsMandatory);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
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

	@Override
	public void setBLD_M_PartType_ID(int BLD_M_PartType_ID) {
		if (BLD_M_PartType_ID < 1) 
			set_Value (COLUMNNAME_BLD_M_PartType_ID, null);
		else 
			set_Value (COLUMNNAME_BLD_M_PartType_ID, Integer.valueOf(BLD_M_PartType_ID));
		
	}

	@Override
	public int getBLD_M_PartType_ID() {
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_M_PartType_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public int getM_PartTypeID() {
		Integer ii = (Integer)get_Value(COLUMNNAME_MPartTypeID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	@Override
	public void setM_PartTypeId(int mPartTypeID) {
		if (mPartTypeID < 1) 
			set_Value (COLUMNNAME_MPartTypeID, null);
		else 
			set_Value (COLUMNNAME_MPartTypeID, Integer.valueOf(mPartTypeID));
		
	}
}