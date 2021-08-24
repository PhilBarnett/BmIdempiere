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

/** Generated Model for BLD_Line_ProductInstance
 *  @author iDempiere (generated) 
 *  @version Release 5.1 - $Id$ */
public class X_BLD_Line_ProductInstance extends PO implements I_BLD_Line_ProductInstance, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20180823L;
	
	 /** Column name M_AttributeSetInstance_ID */
    public static final String COLUMNNAME_M_AttributeSetInstance_ID = "M_AttributeSetInstance_ID";

    /** Standard Constructor */
    public X_BLD_Line_ProductInstance (Properties ctx, int BLD_Line_ProductInstance_ID, String trxName)
    {
      super (ctx, BLD_Line_ProductInstance_ID, trxName);
      /** if (BLD_Line_ProductInstance_ID == 0)
        {
			setBLD_Line_ProductSetInstance_ID (0);
			setBLD_Product_PartType_ID (0);
        } */
    }

    /** Load Constructor */
    public X_BLD_Line_ProductInstance (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    public X_BLD_Line_ProductInstance(Properties p_ctx) {
		super(p_ctx);
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
      StringBuffer sb = new StringBuffer ("X_BLD_Line_ProductInstance[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set BLD_Line_ProductInstance_UU.
		@param BLD_Line_ProductInstance_UU BLD_Line_ProductInstance_UU	  */
	public void setBLD_Line_ProductInstance_UU (String BLD_Line_ProductInstance_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_Line_ProductInstance_UU, BLD_Line_ProductInstance_UU);
	}

	/** Get BLD_Line_ProductInstance_UU.
		@return BLD_Line_ProductInstance_UU	  */
	public String getBLD_Line_ProductInstance_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_Line_ProductInstance_UU);
	}

	public I_BLD_Line_ProductSetInstance getBLD_Line_ProductSetInstance() throws RuntimeException
    {
		return (I_BLD_Line_ProductSetInstance)MTable.get(getCtx(), I_BLD_Line_ProductSetInstance.Table_Name)
			.getPO(getBLD_Line_ProductSetInstance_ID(), get_TrxName());	}

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

	public I_BLD_Product_PartType getBLD_Product_PartType() throws RuntimeException
    {
		return (I_BLD_Product_PartType)MTable.get(getCtx(), I_BLD_Product_PartType.Table_Name)
			.getPO(getBLD_Product_PartType_ID(), get_TrxName());	}

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
	
	public I_M_AttributeSetInstance getM_AttributeSetInstance() throws RuntimeException
    {
		return (I_M_AttributeSetInstance)MTable.get(getCtx(), I_M_AttributeSetInstance.Table_Name)
			.getPO(getM_AttributeSetInstance_ID(), get_TrxName());	}

	/** Set Attribute Set Instance.
		@param M_AttributeSetInstance_ID 
		Product Attribute Set Instance
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
	public int getM_AttributeSetInstance_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_AttributeSetInstance_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}