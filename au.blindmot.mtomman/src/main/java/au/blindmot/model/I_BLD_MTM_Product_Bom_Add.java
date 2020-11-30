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

/** Generated Interface for BLD_MTM_Product_Bom_Add
 *  @author iDempiere (generated) 
 *  @version Release 6.2
 */
@SuppressWarnings("all")
public interface I_BLD_MTM_Product_Bom_Add 
{

    /** TableName=BLD_MTM_Product_Bom_Add */
    public static final String Table_Name = "BLD_MTM_Product_Bom_Add";

    /** AD_Table_ID=1000063 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 7 - System - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(7);

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

    /** Column name BLD_MTM_Product_Bom_Add_ID */
    public static final String COLUMNNAME_BLD_MTM_Product_Bom_Add_ID = "BLD_MTM_Product_Bom_Add_ID";

	/** Set Product BomDerived to Add	  */
	public void setBLD_MTM_Product_Bom_Add_ID (int BLD_MTM_Product_Bom_Add_ID);

	/** Get Product BomDerived to Add	  */
	public int getBLD_MTM_Product_Bom_Add_ID();

    /** Column name BLD_MTM_Product_Bom_Add_UU */
    public static final String COLUMNNAME_BLD_MTM_Product_Bom_Add_UU = "BLD_MTM_Product_Bom_Add_UU";

	/** Set BLD_MTM_Product_Bom_Add_UU	  */
	public void setBLD_MTM_Product_Bom_Add_UU (String BLD_MTM_Product_Bom_Add_UU);

	/** Get BLD_MTM_Product_Bom_Add_UU	  */
	public String getBLD_MTM_Product_Bom_Add_UU();

    /** Column name BLD_MTM_Product_Bom_Trigger_ID */
    public static final String COLUMNNAME_BLD_MTM_Product_Bom_Trigger_ID = "BLD_MTM_Product_Bom_Trigger_ID";

	/** Set BLD_MTM_Product_Bom_Trigger_ID	  */
	public void setBLD_MTM_Product_Bom_Trigger_ID (int BLD_MTM_Product_Bom_Trigger_ID);

	/** Get BLD_MTM_Product_Bom_Trigger_ID	  */
	public int getBLD_MTM_Product_Bom_Trigger_ID();

	public I_BLD_MTM_Product_Bom_Trigger getBLD_MTM_Product_Bom_Trigger() throws RuntimeException;

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

    /** Column name Help */
    public static final String COLUMNNAME_Help = "Help";

	/** Set Comment/Help.
	  * Comment or Hint
	  */
	public void setHelp (String Help);

	/** Get Comment/Help.
	  * Comment or Hint
	  */
	public String getHelp();

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

    /** Column name Line */
    public static final String COLUMNNAME_Line = "Line";

	/** Set Line No.
	  * Unique line for this document
	  */
	public void setLine (int Line);

	/** Get Line No.
	  * Unique line for this document
	  */
	public int getLine();

    /** Column name M_Product_BOM_ID */
    public static final String COLUMNNAME_M_Product_BOM_ID = "M_Product_BOM_ID";

	/** Set BOM Line	  */
	public void setM_Product_BOM_ID (int M_Product_BOM_ID);

	/** Get BOM Line	  */
	public int getM_Product_BOM_ID();

	public org.compiere.model.I_M_Product_BOM getM_Product_BOM() throws RuntimeException;

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

    /** Column name Value */
    public static final String COLUMNNAME_Value = "Value";

	/** Set Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value);

	/** Get Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public String getValue();
}
