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

/** Generated Interface for BLD_Product_Non_Select
 *  @author iDempiere (generated) 
 *  @version Release 6.2
 */
@SuppressWarnings("all")
public interface I_BLD_Product_Non_Select 
{

    /** TableName=BLD_Product_Non_Select */
    public static final String Table_Name = "BLD_Product_Non_Select";

    /** AD_Table_ID=1000061 */
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

    /** Column name addtionalproduct */
    public static final String COLUMNNAME_addtionalproduct = "addtionalproduct";

	/** Set addtionalproduct	  */
	public void setaddtionalproduct (Object addtionalproduct);

	/** Get addtionalproduct	  */
	public Object getaddtionalproduct();

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

    /** Column name BLD_Product_Non_Select_ID */
    public static final String COLUMNNAME_BLD_Product_Non_Select_ID = "BLD_Product_Non_Select_ID";

	/** Set BLD_Product_Non_Select	  */
	public void setBLD_Product_Non_Select_ID (int BLD_Product_Non_Select_ID);

	/** Get BLD_Product_Non_Select	  */
	public int getBLD_Product_Non_Select_ID();

    /** Column name BLD_Product_Non_Select_UU */
    public static final String COLUMNNAME_BLD_Product_Non_Select_UU = "BLD_Product_Non_Select_UU";

	/** Set BLD_Product_Non_Select_UU	  */
	public void setBLD_Product_Non_Select_UU (String BLD_Product_Non_Select_UU);

	/** Get BLD_Product_Non_Select_UU	  */
	public String getBLD_Product_Non_Select_UU();

    /** Column name BLD_Product_PartType_ID */
    public static final String COLUMNNAME_BLD_Product_PartType_ID = "BLD_Product_PartType_ID";
    
    /** Column name M_Product_BOM_ID  */
    public static final String COLUMNNAME_M_Product_BOM_ID  = "m_product_bom_id";
    
    /** Column name XM_Product_ID  */
    public static final String COLUMNNAME_XM_Product_ID  = "xm_product_id";

	/** Set BLD Product PartType	  */
	public void setBLD_Product_PartType_ID (int BLD_Product_PartType_ID);

	/** Get BLD Product PartType	  */
	public int getBLD_Product_PartType_ID();

	public I_BLD_Product_PartType getBLD_Product_PartType() throws RuntimeException;

    /** Column name condition_set */
    public static final String COLUMNNAME_condition_set = "condition_set";

	/** Set condition_set	  */
	public void setcondition_set (String condition_set);

	/** Get condition_set	  */
	public String getcondition_set();

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

    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/** Set Description.
	  * Optional short description of the record
	  */
	public void setDescription (String Description);

	/** Get Description.
	  * Optional short description of the record
	  */
	public String getDescription();

    /** Column name drop1 */
    public static final String COLUMNNAME_drop1 = "drop1";

	/** Set drop1	  */
	public void setdrop1 (int drop1);

	/** Get drop1	  */
	public int getdrop1();

    /** Column name drop2 */
    public static final String COLUMNNAME_drop2 = "drop2";

	/** Set drop2	  */
	public void setdrop2 (int drop2);

	/** Get drop2	  */
	public int getdrop2();

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

    /** Column name M_PartType_ID */
    public static final String COLUMNNAME_M_PartType_ID = "M_PartType_ID";

	/** Set Part Type	  */
	public void setM_PartType_ID (int M_PartType_ID);

	/** Get Part Type	  */
	public int getM_PartType_ID();

	public org.compiere.model.I_M_PartType getM_PartType() throws RuntimeException;

    /** Column name operation_type */
    public static final String COLUMNNAME_operation_type = "operation_type";

	/** Set operation_type	  */
	public void setoperation_type (String operation_type);

	/** Get operation_type	  */
	public String getoperation_type();

    /** Column name substituteproduct */
    public static final String COLUMNNAME_substituteproduct = "substituteproduct";

	/** Set substituteproduct	  */
	public void setsubstituteproduct (Object substituteproduct);

	/** Get substituteproduct	  */
	public Object getsubstituteproduct();

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

    /** Column name width1 */
    public static final String COLUMNNAME_width1 = "width1";

	/** Set width1	  */
	public void setwidth1 (int width1);

	/** Get width1	  */
	public int getwidth1();

    /** Column name width2 */
    public static final String COLUMNNAME_width2 = "width2";

	/** Set width2	  */
	public void setwidth2 (int width2);

	/** Get width2	  */
	public int getwidth2();
}
