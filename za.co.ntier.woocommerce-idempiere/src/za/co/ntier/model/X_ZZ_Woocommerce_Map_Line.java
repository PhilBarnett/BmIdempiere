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

import au.blindmot.model.I_BLD_Product_PartType;

/** Generated Model for ZZ_Woocommerce_Map_Line
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="ZZ_Woocommerce_Map_Line")
public class X_ZZ_Woocommerce_Map_Line extends PO implements I_ZZ_Woocommerce_Map_Line, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20230604L;

    /** Standard Constructor */
    public X_ZZ_Woocommerce_Map_Line (Properties ctx, int ZZ_Woocommerce_Map_Line_ID, String trxName)
    {
      super (ctx, ZZ_Woocommerce_Map_Line_ID, trxName);
      /** if (ZZ_Woocommerce_Map_Line_ID == 0)
        {
			setAdd_To_Duplicate (false);
			setZZ_Woocommerce_Map_ID (0);
			setZZ_Woocommerce_Map_Line_ID (0);
        } */
    }

    /** Load Constructor */
    public X_ZZ_Woocommerce_Map_Line (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_ZZ_Woocommerce_Map_Line[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Add_To_Duplicate.
		@param Add_To_Duplicate Add_To_Duplicate
	*/
    /*
	public void setAdd_To_Duplicate (boolean Add_To_Duplicate)
	{
		set_Value (COLUMNNAME_Add_To_Duplicate, Boolean.valueOf(Add_To_Duplicate));
	}
*/
   
	/** Get Add_To_Duplicate.
		@return Add_To_Duplicate	  */
    /*public boolean isAdd_To_Duplicate()
	{
		Object oo = get_Value(COLUMNNAME_Add_To_Duplicate);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}
*/
	public I_BLD_Product_PartType getBLD_Product_PartType() throws RuntimeException
	{
		return (I_BLD_Product_PartType)MTable.get(getCtx(), I_BLD_Product_PartType.Table_ID)
			.getPO(getBLD_Product_PartType_ID(), get_TrxName());
	}

	/** Set Product PartType.
		@param BLD_Product_PartType_ID Product PartType
	*/
	public void setBLD_Product_PartType_ID (int BLD_Product_PartType_ID)
	{
		if (BLD_Product_PartType_ID < 1)
			set_Value (COLUMNNAME_BLD_Product_PartType_ID, null);
		else
			set_Value (COLUMNNAME_BLD_Product_PartType_ID, Integer.valueOf(BLD_Product_PartType_ID));
	}

	/** Get Product PartType.
		@return Product PartType	  */
	public int getBLD_Product_PartType_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_Product_PartType_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	public org.compiere.model.I_M_Attribute getM_Attribute() throws RuntimeException
	{
		return (org.compiere.model.I_M_Attribute)MTable.get(getCtx(), org.compiere.model.I_M_Attribute.Table_ID)
			.getPO(getM_Attribute_ID(), get_TrxName());
	}

	/** Set Attribute.
		@param M_Attribute_ID Product Attribute
	*/
	public void setM_Attribute_ID (int M_Attribute_ID)
	{
		if (M_Attribute_ID < 1)
			set_Value (COLUMNNAME_M_Attribute_ID, null);
		else
			set_Value (COLUMNNAME_M_Attribute_ID, Integer.valueOf(M_Attribute_ID));
	}

	/** Get Attribute.
		@return Product Attribute
	  */
	public int getM_Attribute_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Attribute_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_Attribute getM_Attribute_Product() throws RuntimeException
	{
		return (org.compiere.model.I_M_Attribute)MTable.get(getCtx(), org.compiere.model.I_M_Attribute.Table_ID)
			.getPO(getM_Attribute_Product_ID(), get_TrxName());
	}

	/** Set Idempiere product.
		@param M_Attribute_Product_ID Idempiere product
	*/
	public void setM_Attribute_Product_ID (int M_Attribute_Product_ID)
	{
		if (M_Attribute_Product_ID < 1)
			set_Value (COLUMNNAME_M_Attribute_Product_ID, null);
		else
			set_Value (COLUMNNAME_M_Attribute_Product_ID, Integer.valueOf(M_Attribute_Product_ID));
	}

	/** Get Idempiere product.
		@return Idempiere product	  */
	public int getM_Attribute_Product_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Attribute_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_M_AttributeSetInstance getM_AttributeSetInstance() throws RuntimeException
	{
		return (I_M_AttributeSetInstance)MTable.get(getCtx(), I_M_AttributeSetInstance.Table_ID)
			.getPO(getM_AttributeSetInstance_ID(), get_TrxName());
	}

	/** Set Attribute Set Instance.
		@param M_AttributeSetInstance_ID Product Attribute Set Instance
	*/
	public void setM_AttributeSetInstance_ID (int M_AttributeSetInstance_ID)
	{
		if (M_AttributeSetInstance_ID < 0)
			set_Value (COLUMNNAME_M_AttributeSetInstance_ID, null);
		else
			set_Value (COLUMNNAME_M_AttributeSetInstance_ID, Integer.valueOf(M_AttributeSetInstance_ID));
	}

	/** Get Attribute Set Instance.
		@return Product Attribute Set Instance
	  */
	public int getM_AttributeSetInstance_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_AttributeSetInstance_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_AttributeValue getM_AttributeValue() throws RuntimeException
	{
		return (org.compiere.model.I_M_AttributeValue)MTable.get(getCtx(), org.compiere.model.I_M_AttributeValue.Table_ID)
			.getPO(getM_AttributeValue_ID(), get_TrxName());
	}

	/** Set Attribute Value.
		@param M_AttributeValue_ID Product Attribute Value
	*/
	public void setM_AttributeValue_ID (int M_AttributeValue_ID)
	{
		if (M_AttributeValue_ID < 1)
			set_Value (COLUMNNAME_M_AttributeValue_ID, null);
		else
			set_Value (COLUMNNAME_M_AttributeValue_ID, Integer.valueOf(M_AttributeValue_ID));
	}

	/** Get Attribute Value.
		@return Product Attribute Value
	  */
	public int getM_AttributeValue_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_AttributeValue_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_AttributeValue getM_Attributevalue_Product() throws RuntimeException
	{
		return (org.compiere.model.I_M_AttributeValue)MTable.get(getCtx(), org.compiere.model.I_M_AttributeValue.Table_ID)
			.getPO(getM_Attributevalue_Product_ID(), get_TrxName());
	}

	/** Set Idempiere product attribute.
		@param M_Attributevalue_Product_ID Idempiere product attribute
	*/
	public void setM_Attributevalue_Product_ID (int M_Attributevalue_Product_ID)
	{
		if (M_Attributevalue_Product_ID < 1)
			set_Value (COLUMNNAME_M_Attributevalue_Product_ID, null);
		else
			set_Value (COLUMNNAME_M_Attributevalue_Product_ID, Integer.valueOf(M_Attributevalue_Product_ID));
	}

	/** Get Idempiere product attribute.
		@return Idempiere product attribute	  */
	public int getM_Attributevalue_Product_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Attributevalue_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_M_Product getM_Product_Line() throws RuntimeException
	{
		return (org.compiere.model.I_M_Product)MTable.get(getCtx(), org.compiere.model.I_M_Product.Table_ID)
			.getPO(getM_Product_Line_ID(), get_TrxName());
	}

	/** Set M_Product_Line_ID.
		@param M_Product_Line_ID M_Product_Line_ID
	*/
	public void setM_Product_Line_ID (int M_Product_Line_ID)
	{
		if (M_Product_Line_ID < 1)
			set_Value (COLUMNNAME_M_Product_Line_ID, null);
		else
			set_Value (COLUMNNAME_M_Product_Line_ID, Integer.valueOf(M_Product_Line_ID));
	}

	/** Get M_Product_Line_ID.
		@return M_Product_Line_ID	  */
	public int getM_Product_Line_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_Line_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_ZZ_Woocommerce_Map getZZ_Woocommerce_Map() throws RuntimeException
	{
		return (I_ZZ_Woocommerce_Map)MTable.get(getCtx(), I_ZZ_Woocommerce_Map.Table_ID)
			.getPO(getZZ_Woocommerce_Map_ID(), get_TrxName());
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

	/** Set Woocommerce Map Lines.
		@param ZZ_Woocommerce_Map_Line_ID Woocommerce Map Lines
	*/
	public void setZZ_Woocommerce_Map_Line_ID (int ZZ_Woocommerce_Map_Line_ID)
	{
		if (ZZ_Woocommerce_Map_Line_ID < 1)
			set_ValueNoCheck (COLUMNNAME_ZZ_Woocommerce_Map_Line_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_ZZ_Woocommerce_Map_Line_ID, Integer.valueOf(ZZ_Woocommerce_Map_Line_ID));
	}

	/** Get Woocommerce Map Lines.
		@return Woocommerce Map Lines	  */
	public int getZZ_Woocommerce_Map_Line_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ZZ_Woocommerce_Map_Line_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set ZZ_Woocommerce_Map_Line_UU.
		@param ZZ_Woocommerce_Map_Line_UU ZZ_Woocommerce_Map_Line_UU
	*/
	public void setZZ_Woocommerce_Map_Line_UU (String ZZ_Woocommerce_Map_Line_UU)
	{
		set_ValueNoCheck (COLUMNNAME_ZZ_Woocommerce_Map_Line_UU, ZZ_Woocommerce_Map_Line_UU);
	}

	/** Get ZZ_Woocommerce_Map_Line_UU.
		@return ZZ_Woocommerce_Map_Line_UU	  */
	public String getZZ_Woocommerce_Map_Line_UU()
	{
		return (String)get_Value(COLUMNNAME_ZZ_Woocommerce_Map_Line_UU);
	}

	/** Attribute = 10000003 */
	public static final String ZZ_WOOCOMMERCE_MAP_TYPE_Attribute = "10000003";
	/** Product add = 10000004 */
	public static final String ZZ_WOOCOMMERCE_MAP_TYPE_ProductAdd = "10000004";
	/** Product attribute = 10000005 */
	public static final String ZZ_WOOCOMMERCE_MAP_TYPE_ProductAttribute = "10000005";
	/** Set Woocommerce Map Type.
		@param zz_woocommerce_map_type Woocommerce Map Type
	*/
	public void setzz_woocommerce_map_type (String zz_woocommerce_map_type)
	{

		set_Value (COLUMNNAME_zz_woocommerce_map_type, zz_woocommerce_map_type);
	}

	/** Get Woocommerce Map Type.
		@return Woocommerce Map Type	  */
	public String getzz_woocommerce_map_type()
	{
		return (String)get_Value(COLUMNNAME_zz_woocommerce_map_type);
	}
}