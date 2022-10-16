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

/** Generated Model for BLD_Mtom_Production_Log
 *  @author iDempiere (generated) 
 *  @version Release 5.1 - $Id$ */
public class X_BLD_Mtom_Production_Log extends PO implements I_BLD_Mtom_Production_Log, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20180818L;

    /** Standard Constructor */
    public X_BLD_Mtom_Production_Log (Properties ctx, int BLD_Mtom_Production_Log_ID, String trxName)
    {
      super (ctx, BLD_Mtom_Production_Log_ID, trxName);
      /** if (BLD_Mtom_Production_Log_ID == 0)
        {
			setbld_mtom_item_line_ID (0);
			setbld_mtom_production_ID (0);
			setBLD_Mtom_Production_Log_ID (0);
			setM_Product_ID (0);
        } */
    }

    /** Load Constructor */
    public X_BLD_Mtom_Production_Log (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_BLD_Mtom_Production_Log[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public int getbld_mtom_item_line_id() throws RuntimeException
    {
		return get_ValueAsInt(COLUMNNAME_bld_mtom_item_line_ID);
		}

	/** Set Made to measure items.
		@param bld_mtom_item_line_ID Made to measure items	  */
	public void setbld_mtom_item_line_ID (int bld_mtom_item_line_ID)
	{
		if (bld_mtom_item_line_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_bld_mtom_item_line_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_bld_mtom_item_line_ID, Integer.valueOf(bld_mtom_item_line_ID));
	}

	/** Get Made to measure items.
		@return Made to measure items	  */
	public int getbld_mtom_item_line_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_bld_mtom_item_line_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/*
	public I_bld_mtom_production getbld_mtom_production() throws RuntimeException
    {
		return (I_bld_mtom_production)MTable.get(getCtx(), I_bld_mtom_production.Table_Name)
			.getPO(getbld_mtom_production_ID(), get_TrxName());	}
	 */
	
	
	/** Set Made to measure production.
		@param bld_mtom_production_ID Made to measure production	  */
	public void setbld_mtom_production_ID (int bld_mtom_production_ID)
	{
		if (bld_mtom_production_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_bld_mtom_production_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_bld_mtom_production_ID, Integer.valueOf(bld_mtom_production_ID));
	}

	/** Get Made to measure production.
		@return Made to measure production	  */
	public int getbld_mtom_production_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_bld_mtom_production_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set MTM log table.
		@param BLD_Mtom_Production_Log_ID MTM log table	  */
	public void setBLD_Mtom_Production_Log_ID (int BLD_Mtom_Production_Log_ID)
	{
		if (BLD_Mtom_Production_Log_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_BLD_Mtom_Production_Log_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_BLD_Mtom_Production_Log_ID, Integer.valueOf(BLD_Mtom_Production_Log_ID));
	}

	/** Get MTM log table.
		@return MTM log table	  */
	public int getBLD_Mtom_Production_Log_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BLD_Mtom_Production_Log_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set BLD_Mtom_Production_Log_UU.
		@param BLD_Mtom_Production_Log_UU BLD_Mtom_Production_Log_UU	  */
	public void setBLD_Mtom_Production_Log_UU (String BLD_Mtom_Production_Log_UU)
	{
		set_ValueNoCheck (COLUMNNAME_BLD_Mtom_Production_Log_UU, BLD_Mtom_Production_Log_UU);
	}

	/** Get BLD_Mtom_Production_Log_UU.
		@return BLD_Mtom_Production_Log_UU	  */
	public String getBLD_Mtom_Production_Log_UU () 
	{
		return (String)get_Value(COLUMNNAME_BLD_Mtom_Production_Log_UU);
	}

	/** Set log_text.
		@param log_text log_text	  */
	public void setlog_text (String log_text)
	{
		set_Value (COLUMNNAME_log_text, log_text);
	}

	/** Get log_text.
		@return log_text	  */
	public String getlog_text () 
	{
		return (String)get_Value(COLUMNNAME_log_text);
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

	
}