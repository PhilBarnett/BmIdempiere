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

/** Generated Model for BLD_Product_Non_Select
 *  @author iDempiere (generated) 
 *  @version Release 6.2 - $Id$ */
public class X_BLD_Product_Non_Select extends PO implements I_BLD_Product_Non_Select, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20190228L;

    /** Standard Constructor */
    public X_BLD_Product_Non_Select (Properties ctx, int BLD_Product_Non_Select_ID, String trxName)
    {
      super (ctx, BLD_Product_Non_Select_ID, trxName);
      /** if (BLD_Product_Non_Select_ID == 0)
        {
			setBLD_Product_Non_Select_ID (0);
			setBLD_Product_PartType_ID (0);
			setLine (0);
// @SQL=SELECT COALESCE(MAX(Line),0)+10 AS DefaultValue FROM BLD_Product_Non_Select WHERE BLD_Product_PartType_ID = @BLD_Product_PartType_ID@
        } */
    }

    /** Load Constructor */
    public X_BLD_Product_Non_Select (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_BLD_Product_Non_Select[")
        .append(get_ID()).append("]");
      return sb.toString();
    }
    

	/** Get PP_Product_Bomline_ID.
	@return PP_Product_Bomline_ID	  */
public int getPP_Product_Bomline_ID () 
{
	Integer ii = (Integer)get_Value(COLUMNNAME_PP_Product_Bomline_ID);
	if (ii == null)
		 return 0;
	return ii.intValue();
}

/** Set PP_Product_Bomline_ID.
@param PP_Product_Bomline_ID PP_Product_Bomline_ID	  */
public void setPP_Product_Bomline_ID (int PP_Product_Bomline_ID)
{
if (PP_Product_Bomline_ID < 1) 
	set_ValueNoCheck (COLUMNNAME_PP_Product_Bomline_ID, null);
else 
	set_ValueNoCheck (COLUMNNAME_PP_Product_Bomline_ID, Integer.valueOf(PP_Product_Bomline_ID));
}

	/** Set addtionalproduct.
		@param addtionalproduct addtionalproduct	  */
	public void setaddtionalproduct (Object addtionalproduct)
	{
		set_Value (COLUMNNAME_addtionalproduct, addtionalproduct);
	}

	/** Get addtionalproduct.
		@return addtionalproduct	  */
	public Object getaddtionalproduct () 
	{
				return get_Value(COLUMNNAME_addtionalproduct);
	}

	/** Set BLD_Product_Non_Select.
		@param BLD_Product_Non_Select_ID BLD_Product_Non_Select	  */
	public void setBLD_Product_Non_Select_ID (int BLD_Product_Non_Select_ID)
	{
		if (BLD_Product_Non_Select_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_Product_Non_Select_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_Product_Non_Select_ID, Integer.valueOf(BLD_Product_Non_Select_ID));
	}

	/** Get BLD_Product_Non_Select.
		@return BLD_Product_Non_Select	  */
	public int getBLD_Product_Non_Select_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_Product_Non_Select_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BLD_Product_Non_Select_UU.
		@param BLD_Product_Non_Select_UU BLD_Product_Non_Select_UU	  */
	public void setBLD_Product_Non_Select_UU (String BLD_Product_Non_Select_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_Product_Non_Select_UU, BLD_Product_Non_Select_UU);
	}

	/** Get BLD_Product_Non_Select_UU.
		@return BLD_Product_Non_Select_UU	  */
	public String getBLD_Product_Non_Select_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_Product_Non_Select_UU);
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
	
	public void setM_Product_Bom_ID (int M_Product_Bom_ID)
	{
		if (M_Product_Bom_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_M_Product_BOM_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_M_Product_BOM_ID, Integer.valueOf(M_Product_Bom_ID));
	}
	
	public int getM_Product_Bom_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_BOM_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Has Lift Spring = HasLiftSpring */
	public static final String CONDITION_SET_HasLiftSpring = "HasLiftSpring";
	/** Requires Tuning = requirestuning */
	public static final String CONDITION_SET_RequiresTuning = "requirestuning";
	/** Set condition_set.
		@param condition_set condition_set	  */
	public void setcondition_set (String condition_set)
	{

		set_Value (COLUMNNAME_condition_set, condition_set);
	}

	/** Get condition_set.
		@return condition_set	  */
	public String getcondition_set () 
	{
		return (String)get_Value(COLUMNNAME_condition_set);
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

	/** Set drop1.
		@param drop1 drop1	  */
	public void setdrop1 (int drop1)
	{
		set_Value (COLUMNNAME_drop1, Integer.valueOf(drop1));
	}

	/** Get drop1.
		@return drop1	  */
	public int getdrop1 () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_drop1);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set drop2.
		@param drop2 drop2	  */
	public void setdrop2 (int drop2)
	{
		set_Value (COLUMNNAME_drop2, Integer.valueOf(drop2));
	}

	/** Get drop2.
		@return drop2	  */
	public int getdrop2 () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_drop2);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	public org.compiere.model.I_M_PartType getM_PartType() throws RuntimeException
    {
		return (org.compiere.model.I_M_PartType)MTable.get(getCtx(), org.compiere.model.I_M_PartType.Table_Name)
			.getPO(getM_PartType_ID(), get_TrxName());	}

	/** Set Part Type.
		@param M_PartType_ID Part Type	  */
	public void setM_PartType_ID (int M_PartType_ID)
	{
		throw new IllegalArgumentException ("M_PartType_ID is virtual column");	}

	/** Get Part Type.
		@return Part Type	  */
	public int getM_PartType_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_PartType_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Addition = Addition */
	public static final String OPERATION_TYPE_Addition = "Addition";
	/** Substitution = Substitution */
	public static final String OPERATION_TYPE_Substitution = "Substitution";
	/** Condition Set = Condition Set */
	public static final String OPERATION_TYPE_ConditionSet = "Condition Set";
	/** Set operation_type.
		@param operation_type operation_type	  */
	public void setoperation_type (String operation_type)
	{

		set_Value (COLUMNNAME_operation_type, operation_type);
	}

	/** Get operation_type.
		@return operation_type	  */
	public String getoperation_type () 
	{
		return (String)get_Value(COLUMNNAME_operation_type);
	}

	/** Set substituteproduct.
		@param substituteproduct substituteproduct	  */
	public void setsubstituteproduct (Object substituteproduct)
	{
		set_Value (COLUMNNAME_substituteproduct, substituteproduct);
	}

	/** Get substituteproduct.
		@return substituteproduct	  */
	public Object getsubstituteproduct () 
	{
				return get_Value(COLUMNNAME_substituteproduct);
	}

	/** Set width1.
		@param width1 width1	  */
	public void setwidth1 (int width1)
	{
		set_Value (COLUMNNAME_width1, Integer.valueOf(width1));
	}

	/** Get width1.
		@return width1	  */
	public int getwidth1 () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_width1);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set width2.
		@param width2 width2	  */
	public void setwidth2 (int width2)
	{
		set_Value (COLUMNNAME_width2, Integer.valueOf(width2));
	}

	/** Get width2.
		@return width2	  */
	public int getwidth2 () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_width2);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public void setM_Product_ID(int M_Product_ID) {
		// TODO Auto-generated method stub
		
	}

	
	public int getM_Product_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public I_M_Product getM_Product() throws RuntimeException {
		// TODO Auto-generated method stub
		return null;
	}
}