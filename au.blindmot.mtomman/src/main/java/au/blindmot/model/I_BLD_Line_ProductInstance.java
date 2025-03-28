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

/** Generated Interface for BLD_Line_ProductInstance
 *  @author iDempiere (generated) 
 *  @version Release 5.1
 */
@SuppressWarnings("all")
public interface I_BLD_Line_ProductInstance 
{

    /** TableName=BLD_Line_ProductInstance */
    public static final String Table_Name = "BLD_Line_ProductInstance";

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

    /** Column name BLD_Line_ProductInstance_UU */
    public static final String COLUMNNAME_BLD_Line_ProductInstance_UU = "BLD_Line_ProductInstance_UU";

	/** Set BLD_Line_ProductInstance_UU	  */
	public void setBLD_Line_ProductInstance_UU (String BLD_Line_ProductInstance_UU);

	/** Get BLD_Line_ProductInstance_UU	  */
	public String getBLD_Line_ProductInstance_UU();

    /** Column name BLD_Line_ProductSetInstance_ID */
    public static final String COLUMNNAME_BLD_Line_ProductSetInstance_ID = "BLD_Line_ProductSetInstance_ID";

	/** Set BLD Line ProductSetInstance	  */
	public void setBLD_Line_ProductSetInstance_ID (int BLD_Line_ProductSetInstance_ID);

	/** Get BLD Line ProductSetInstance	  */
	public int getBLD_Line_ProductSetInstance_ID();

	public I_BLD_Line_ProductSetInstance getBLD_Line_ProductSetInstance() throws RuntimeException;

    /** Column name BLD_Product_PartType_ID */
    public static final String COLUMNNAME_BLD_Product_PartType_ID = "BLD_Product_PartType_ID";

	/** Set BLD Product PartType	  */
	public void setBLD_Product_PartType_ID (int BLD_Product_PartType_ID);

	/** Get BLD Product PartType	  */
	public int getBLD_Product_PartType_ID();

	public I_BLD_Product_PartType getBLD_Product_PartType() throws RuntimeException;

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
}
