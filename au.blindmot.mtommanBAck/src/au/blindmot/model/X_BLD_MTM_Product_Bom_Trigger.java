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

/** Generated Model for BLD_MTM_Product_Bom_Trigger
 *  @author iDempiere (generated) 
 *  @version Release 6.2 - $Id$ */
public class X_BLD_MTM_Product_Bom_Trigger extends PO implements I_BLD_MTM_Product_Bom_Trigger, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190906L;

    /** Standard Constructor */
    public X_BLD_MTM_Product_Bom_Trigger (Properties ctx, int BLD_MTM_Product_Bom_Trigger_ID, String trxName)
    {
      super (ctx, BLD_MTM_Product_Bom_Trigger_ID, trxName);
      /** if (BLD_MTM_Product_Bom_Trigger_ID == 0)
        {
			setBLD_MTM_Product_Bom_Trigger_ID (0);
			setDescription (null);
			setIsTriggerDelete (false);
			setM_Product_BOM_ID (0);
			setName (null);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_BLD_MTM_Product_Bom_Trigger (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_BLD_MTM_Product_Bom_Trigger[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

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

	/** Set BLD_MTM_Product_Bom_Trigger_UU.
		@param BLD_MTM_Product_Bom_Trigger_UU BLD_MTM_Product_Bom_Trigger_UU	  */
	public void setBLD_MTM_Product_Bom_Trigger_UU (String BLD_MTM_Product_Bom_Trigger_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_MTM_Product_Bom_Trigger_UU, BLD_MTM_Product_Bom_Trigger_UU);
	}

	/** Get BLD_MTM_Product_Bom_Trigger_UU.
		@return BLD_MTM_Product_Bom_Trigger_UU	  */
	public String getBLD_MTM_Product_Bom_Trigger_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_MTM_Product_Bom_Trigger_UU);
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

	/** Set IsTriggerDelete.
		@param IsTriggerDelete IsTriggerDelete	  */
	public void setIsTriggerDelete (boolean IsTriggerDelete)
	{
		set_Value (COLUMNNAME_IsTriggerDelete, Boolean.valueOf(IsTriggerDelete));
	}

	/** Get IsTriggerDelete.
		@return IsTriggerDelete	  */
	public boolean isTriggerDelete () 
	{
		Object oo = get_Value(COLUMNNAME_IsTriggerDelete);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
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
			set_Value (COLUMNNAME_M_Product_BOM_ID, null);
		else 
			set_Value (COLUMNNAME_M_Product_BOM_ID, Integer.valueOf(M_Product_BOM_ID));
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

	/* (non-Javadoc)
	 * @see au.blindmot.model.I_BLD_MTM_Product_Bom_Trigger#setM_Product_ID(int)
	 */
	@Override
	public void setM_Product_ID(int M_Product_ID) {
		if (M_Product_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_Product_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
		
	}

	/* (non-Javadoc)
	 * @see au.blindmot.model.I_BLD_MTM_Product_Bom_Trigger#getM_Product_ID()
	 */
	@Override
	public int getM_Product_ID() {
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}