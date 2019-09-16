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
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for BLD_mtom_bomderived
 *  @author iDempiere (generated) 
 *  @version Release 4.1
 */
@SuppressWarnings("all")
public interface I_BLD_mtom_bomderived 
{

    /** TableName=BLD_mtom_bomderived */
    public static final String Table_Name = "BLD_mtom_bomderived";

    /** AD_Table_ID=1000012 */
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
    
    /** Column name M_BOMProduct_ID */
    public static final String COLUMNNAME_M_Product_BOM_ID = "M_Product_Bom_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name attribute_name */
    public static final String COLUMNNAME_attribute_name = "attribute_name";

	/** Set attribute_name	  */
	public void setattribute_name (String attribute_name);

	/** Get attribute_name	  */
	public String getattribute_name();

    /** Column name bld_mtom_bomderived_ID */
    public static final String COLUMNNAME_bld_mtom_bomderived_ID = "bld_mtom_bomderived_ID";

	/** Set BLD made to measure BOM derived	  */
	public void setbld_mtom_bomderived_ID (int bld_mtom_bomderived_ID);

	/** Get BLD made to measure BOM derived	  */
	public int getbld_mtom_bomderived_ID();

    /** Column name bld_mtom_bomderived_UU */
    public static final String COLUMNNAME_bld_mtom_bomderived_UU = "bld_mtom_bomderived_UU";

	/** Set bld_mtom_bomderived_UU	  */
	public void setbld_mtom_bomderived_UU (String bld_mtom_bomderived_UU);

	/** Get bld_mtom_bomderived_UU	  */
	public String getbld_mtom_bomderived_UU();

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

    /** Column name M_Attribute_ID */
    public static final String COLUMNNAME_M_Attribute_ID = "M_Attribute_ID";

	/** Set Attribute.
	  * Product Attribute
	  */
	public void setM_Attribute_ID (int M_Attribute_ID);

	/** Get Attribute.
	  * Product Attribute
	  */
	public int getM_Attribute_ID();

	public org.compiere.model.I_M_Attribute getM_Attribute() throws RuntimeException;

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

    /** Column name Qty */
    public static final String COLUMNNAME_Qty = "Qty";

	/** Set Quantity.
	  * Quantity
	  */
	public void setQty (BigDecimal Qty);

	/** Get Quantity.
	  * Quantity
	  */
	public BigDecimal getQty();

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
	
	/** Column name description */
    public static final String COLUMNNAME_Description = "description";
    
    /** Get description */
    public String getDescription();
    
    /** Set description */
    public void setDescription(String description);
}
