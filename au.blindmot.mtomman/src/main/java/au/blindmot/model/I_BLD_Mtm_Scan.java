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

/** Generated Interface for BLD_Mtm_Scan
 *  @author iDempiere (generated) 
 *  @version Release 6.1
 */
@SuppressWarnings("all")
public interface I_BLD_Mtm_Scan 
{

    /** TableName=BLD_Mtm_Scan */
    public static final String Table_Name = "BLD_Mtm_Scan";

    /** AD_Table_ID=1000059 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 4 - System 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(4);

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

    /** Column name AD_User_ID */
    public static final String COLUMNNAME_AD_User_ID = "AD_User_ID";

	/** Set User/Contact.
	  * User within the system - Internal or Business Partner Contact
	  */
	public void setAD_User_ID (int AD_User_ID);

	/** Get User/Contact.
	  * User within the system - Internal or Business Partner Contact
	  */
	public int getAD_User_ID();

	public org.compiere.model.I_AD_User getAD_User() throws RuntimeException;

    /** Column name barcode */
    public static final String COLUMNNAME_barcode = "barcode";

	/** Set barcode	  */
	public void setbarcode (String barcode);

	/** Get barcode	  */
	public String getbarcode();

    /** Column name BLD_Mtm_Scan_ID */
    public static final String COLUMNNAME_BLD_Mtm_Scan_ID = "BLD_Mtm_Scan_ID";

	/** Set BLD_Mtm_Scan	  */
	public void setBLD_Mtm_Scan_ID (int BLD_Mtm_Scan_ID);

	/** Get BLD_Mtm_Scan	  */
	public int getBLD_Mtm_Scan_ID();

    /** Column name BLD_Mtm_Scanpoint_ID */
    public static final String COLUMNNAME_BLD_Mtm_Scanpoint_ID = "BLD_Mtm_Scanpoint_ID";

	/** Set BLD_Mtm_Scanpoint	  */
	public void setBLD_Mtm_Scanpoint_ID (int BLD_Mtm_Scanpoint_ID);

	/** Get BLD_Mtm_Scanpoint	  */
	public int getBLD_Mtm_Scanpoint_ID();

	public I_BLD_Mtm_Scanpoint getBLD_Mtm_Scanpoint() throws RuntimeException;

    /** Column name BLD_Mtm_Scan_UU */
    public static final String COLUMNNAME_BLD_Mtm_Scan_UU = "BLD_Mtm_Scan_UU";

	/** Set BLD_Mtm_Scan_UU	  */
	public void setBLD_Mtm_Scan_UU (String BLD_Mtm_Scan_UU);

	/** Get BLD_Mtm_Scan_UU	  */
	public String getBLD_Mtm_Scan_UU();

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

    /** Column name IsError */
    public static final String COLUMNNAME_IsError = "IsError";

	/** Set Error.
	  * An Error occurred in the execution
	  */
	public void setIsError (boolean IsError);

	/** Get Error.
	  * An Error occurred in the execution
	  */
	public boolean isError();

    /** Column name isprocessed */
    public static final String COLUMNNAME_isprocessed = "isprocessed";

	/** Set isprocessed	  */
	public void setisprocessed (boolean isprocessed);

	/** Get isprocessed	  */
	public boolean isprocessed();

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

    /** Column name M_Locator_ID */
    public static final String COLUMNNAME_M_Locator_ID = "M_Locator_ID";

	/** Set Locator.
	  * Warehouse Locator
	  */
	public void setM_Locator_ID (int M_Locator_ID);

	/** Get Locator.
	  * Warehouse Locator
	  */
	public int getM_Locator_ID();

	public I_M_Locator getM_Locator() throws RuntimeException;

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
