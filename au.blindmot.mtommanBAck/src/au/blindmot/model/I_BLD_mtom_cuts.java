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
package au.blindmot.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.compiere.model.MTable;
import org.compiere.util.KeyNamePair;

/** Generated Interface for BLD_mtom_cuts
 *  @author iDempiere (generated) 
 *  @version Release 4.1
 */
@SuppressWarnings("all")
public interface I_BLD_mtom_cuts 
{

    /** TableName=BLD_mtom_cuts */
    public static final String Table_Name = "BLD_mtom_cuts";

    /** AD_Table_ID=1000013 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name BLD_mtom_cuts_ID */
    public static final String COLUMNNAME_BLD_mtom_cuts_ID = "BLD_mtom_cuts_ID";

	/** Set Made to measure cuts	  */
	public void setBLD_mtom_cuts_ID (int BLD_mtom_cuts_ID);

	/** Get Made to measure cuts	  */
	public int getBLD_mtom_cuts_ID();

    /** Column name BLD_mtom_cuts_UU */
    public static final String COLUMNNAME_BLD_mtom_cuts_UU = "BLD_mtom_cuts_UU";

	/** Set BLD_mtom_cuts_UU	  */
	public void setBLD_mtom_cuts_UU (String BLD_mtom_cuts_UU);

	/** Get BLD_mtom_cuts_UU	  */
	public String getBLD_mtom_cuts_UU();

    /** Column name bld_mtom_item_line_ID */
    public static final String COLUMNNAME_bld_mtom_item_line_ID = "bld_mtom_item_line_ID";

	/** Set Made to measure items	  */
	public void setbld_mtom_item_line_ID (int bld_mtom_item_line_ID);

	/** Get Made to measure items	  */
	public int getbld_mtom_item_line_ID();

	public I_BLD_mtom_item_line getbld_mtom_item_line() throws RuntimeException;

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name Height */
    public static final String COLUMNNAME_Height = "Height";

	/** Set Height	  */
	public void setHeight (BigDecimal Height);

	/** Get Height	  */
	public BigDecimal getHeight();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name Length */
    public static final String COLUMNNAME_Length = "Length";

	/** Set Length	  */
	public void setLength (BigDecimal Length);

	/** Get Length	  */
	public BigDecimal getLength();

    /** Column name M_Product_ID */
    public static final String COLUMNNAME_M_Product_ID = "M_Product_ID";

	/** Set Product.
	  * Product, Service, Item
	  */
	public void setM_Product_ID (int M_Product_ID);

	/** Get Product.
	  * Product, Service, Item
	  */
	public int getM_Product_ID();

	public org.compiere.model.I_M_Product getM_Product() throws RuntimeException;

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();

    /** Column name Width */
    public static final String COLUMNNAME_Width = "Width";

	/** Set Width	  */
	public void setWidth (BigDecimal Width);

	/** Get Width	  */
	public BigDecimal getWidth();
}
