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

/** Generated Model for ZZ_Woocommerce_Match
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="ZZ_Woocommerce_Match")
public class X_ZZ_Woocommerce_Match extends PO implements I_ZZ_Woocommerce_Match, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20230528L;

    /** Standard Constructor */
    public X_ZZ_Woocommerce_Match (Properties ctx, int ZZ_Woocommerce_Match_ID, String trxName)
    {
      super (ctx, ZZ_Woocommerce_Match_ID, trxName);
      /** if (ZZ_Woocommerce_Match_ID == 0)
        {
			setM_Product_ID (0);
			setwoocommerce_key (0);
			setzz_woocommerce_ID (0);
			setZZ_Woocommerce_Match_ID (0);
        } */
    }

    /** Load Constructor */
    public X_ZZ_Woocommerce_Match (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_ZZ_Woocommerce_Match[")
        .append(get_ID()).append("]");
      return sb.toString();
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
			set_Value (COLUMNNAME_M_Product_ID, null);
		else
			set_Value (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
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

	/** Set woocommerce_key.
		@param woocommerce_key woocommerce_key
	*/
	public void setwoocommerce_key (int woocommerce_key)
	{
		set_Value (COLUMNNAME_woocommerce_key, Integer.valueOf(woocommerce_key));
	}

	/** Get woocommerce_key.
		@return woocommerce_key	  */
	public int getwoocommerce_key()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_woocommerce_key);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_zz_woocommerce getzz_woocommerce() throws RuntimeException
	{
		return (I_zz_woocommerce)MTable.get(getCtx(), I_zz_woocommerce.Table_ID)
			.getPO(getzz_woocommerce_ID(), get_TrxName());
	}

	/** Set Woocommerce Default Settings.
		@param zz_woocommerce_ID Woocommerce Default Settings
	*/
	public void setzz_woocommerce_ID (int zz_woocommerce_ID)
	{
		if (zz_woocommerce_ID < 1)
			set_ValueNoCheck (COLUMNNAME_zz_woocommerce_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_zz_woocommerce_ID, Integer.valueOf(zz_woocommerce_ID));
	}

	/** Get Woocommerce Default Settings.
		@return Woocommerce Default Settings	  */
	public int getzz_woocommerce_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_zz_woocommerce_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set ZZ Woocommerce Match.
		@param ZZ_Woocommerce_Match_ID ZZ Woocommerce Match
	*/
	public void setZZ_Woocommerce_Match_ID (int ZZ_Woocommerce_Match_ID)
	{
		if (ZZ_Woocommerce_Match_ID < 1)
			set_ValueNoCheck (COLUMNNAME_ZZ_Woocommerce_Match_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_ZZ_Woocommerce_Match_ID, Integer.valueOf(ZZ_Woocommerce_Match_ID));
	}

	/** Get ZZ Woocommerce Match.
		@return ZZ Woocommerce Match	  */
	public int getZZ_Woocommerce_Match_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ZZ_Woocommerce_Match_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set ZZ_Woocommerce_Match_UU.
		@param ZZ_Woocommerce_Match_UU ZZ_Woocommerce_Match_UU
	*/
	public void setZZ_Woocommerce_Match_UU (String ZZ_Woocommerce_Match_UU)
	{
		set_ValueNoCheck (COLUMNNAME_ZZ_Woocommerce_Match_UU, ZZ_Woocommerce_Match_UU);
	}

	/** Get ZZ_Woocommerce_Match_UU.
		@return ZZ_Woocommerce_Match_UU	  */
	public String getZZ_Woocommerce_Match_UU()
	{
		return (String)get_Value(COLUMNNAME_ZZ_Woocommerce_Match_UU);
	}
}