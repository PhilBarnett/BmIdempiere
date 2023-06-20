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
package za.co.ntier.model;

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for ZZ_Woocommerce_Map
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="ZZ_Woocommerce_Map")
public class X_ZZ_Woocommerce_Map extends PO implements I_ZZ_Woocommerce_Map, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20230620L;

    /** Standard Constructor */
    public X_ZZ_Woocommerce_Map (Properties ctx, int ZZ_Woocommerce_Map_ID, String trxName)
    {
      super (ctx, ZZ_Woocommerce_Map_ID, trxName);
      /** if (ZZ_Woocommerce_Map_ID == 0)
        {
			setignore_no_child_records (false);
			setM_Product_ID (0);
			setZZ_Woocommerce_Map_ID (0);
        } */
    }

    /** Load Constructor */
    public X_ZZ_Woocommerce_Map (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 6 - System - Client 
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
      StringBuilder sb = new StringBuilder ("X_ZZ_Woocommerce_Map[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Description.
		@param Description Optional short description of the record
	*/
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription()
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Comment/Help.
		@param Help Comment or Hint
	*/
	public void setHelp (String Help)
	{
		set_Value (COLUMNNAME_Help, Help);
	}

	/** Get Comment/Help.
		@return Comment or Hint
	  */
	public String getHelp()
	{
		return (String)get_Value(COLUMNNAME_Help);
	}

	/** Set Ignore No Child Records.
		@param ignore_no_child_records Tells the system that a record without mapping lines (child records) is OK.
	*/
	public void setignore_no_child_records (boolean ignore_no_child_records)
	{
		set_Value (COLUMNNAME_ignore_no_child_records, Boolean.valueOf(ignore_no_child_records));
	}

	/** Get Ignore No Child Records.
		@return Tells the system that a record without mapping lines (child records) is OK.
	  */
	public boolean isignore_no_child_records()
	{
		Object oo = get_Value(COLUMNNAME_ignore_no_child_records);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Line No.
		@param Line Unique line for this document
	*/
	public void setLine (int Line)
	{
		set_Value (COLUMNNAME_Line, Integer.valueOf(Line));
	}

	/** Get Line No.
		@return Unique line for this document
	  */
	public int getLine()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Line);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_Product getM_Product() throws RuntimeException
	{
		return (org.compiere.model.I_M_Product)MTable.get(getCtx(), org.compiere.model.I_M_Product.Table_ID)
			.getPO(getM_Product_ID(), get_TrxName());
	}

	/** Set Product.
		@param M_Product_ID Product, Service, Item
	*/
	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1)
			set_ValueNoCheck (COLUMNNAME_M_Product_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
	}

	/** Get Product.
		@return Product, Service, Item
	  */
	public int getM_Product_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set woocommerce_field_key.
		@param woocommerce_field_key woocommerce_field_key
	*/
	public void setwoocommerce_field_key (String woocommerce_field_key)
	{
		set_Value (COLUMNNAME_woocommerce_field_key, woocommerce_field_key);
	}

	/** Get woocommerce_field_key.
		@return woocommerce_field_key	  */
	public String getwoocommerce_field_key()
	{
		return (String)get_Value(COLUMNNAME_woocommerce_field_key);
	}

	/** Set woocommerce_field_label.
		@param woocommerce_field_label woocommerce_field_label
	*/
	public void setwoocommerce_field_label (String woocommerce_field_label)
	{
		set_Value (COLUMNNAME_woocommerce_field_label, woocommerce_field_label);
	}

	/** Get woocommerce_field_label.
		@return woocommerce_field_label	  */
	public String getwoocommerce_field_label()
	{
		return (String)get_Value(COLUMNNAME_woocommerce_field_label);
	}

	/** Set woocommerce_field_value.
		@param woocommerce_field_value woocommerce_field_value
	*/
	public void setwoocommerce_field_value (String woocommerce_field_value)
	{
		set_Value (COLUMNNAME_woocommerce_field_value, woocommerce_field_value);
	}

	/** Get woocommerce_field_value.
		@return woocommerce_field_value	  */
	public String getwoocommerce_field_value()
	{
		return (String)get_Value(COLUMNNAME_woocommerce_field_value);
	}

	/** Set Woocommerce Map.
		@param ZZ_Woocommerce_Map_ID Woocommerce Map
	*/
	public void setZZ_Woocommerce_Map_ID (int ZZ_Woocommerce_Map_ID)
	{
		if (ZZ_Woocommerce_Map_ID < 1)
			set_ValueNoCheck (COLUMNNAME_ZZ_Woocommerce_Map_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_ZZ_Woocommerce_Map_ID, Integer.valueOf(ZZ_Woocommerce_Map_ID));
	}

	/** Get Woocommerce Map.
		@return Woocommerce Map	  */
	public int getZZ_Woocommerce_Map_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ZZ_Woocommerce_Map_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set ZZ_Woocommerce_Map_UU.
		@param ZZ_Woocommerce_Map_UU ZZ_Woocommerce_Map_UU
	*/
	public void setZZ_Woocommerce_Map_UU (String ZZ_Woocommerce_Map_UU)
	{
		set_ValueNoCheck (COLUMNNAME_ZZ_Woocommerce_Map_UU, ZZ_Woocommerce_Map_UU);
	}

	/** Get ZZ_Woocommerce_Map_UU.
		@return ZZ_Woocommerce_Map_UU	  */
	public String getZZ_Woocommerce_Map_UU()
	{
		return (String)get_Value(COLUMNNAME_ZZ_Woocommerce_Map_UU);
	}
}