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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for Bld_Mtm_Install
 *  @author iDempiere (generated) 
 *  @version Release 9 - $Id$ */
@org.adempiere.base.Model(table="Bld_Mtm_Install")
public class X_Bld_Mtm_Install extends PO implements I_Bld_Mtm_Install, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20241106L;

    /** Standard Constructor */
    public X_Bld_Mtm_Install (Properties ctx, int Bld_Mtm_Install_ID, String trxName)
    {
      super (ctx, Bld_Mtm_Install_ID, trxName);
      /** if (Bld_Mtm_Install_ID == 0)
        {
			setBld_Mtm_Install_ID (0);
			setName (null);
			setStatus (null);
			setcheckmeasure_booked (false);
			setcontract_received (false);
			setdate_promised (new Timestamp( System.currentTimeMillis() ));
			setis_checkmeasured (false);
        } */
    }

    /** Load Constructor */
    public X_Bld_Mtm_Install (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
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
      StringBuilder sb = new StringBuilder ("X_Bld_Mtm_Install[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
    }

	public I_C_Location getBP_Location() throws RuntimeException
	{
		return (I_C_Location)MTable.get(getCtx(), I_C_Location.Table_ID)
			.getPO(getBP_Location_ID(), get_TrxName());
	}

	/** Set BP Address.
		@param BP_Location_ID Address of the Business Partner
	*/
	public void setBP_Location_ID (int BP_Location_ID)
	{
		if (BP_Location_ID < 1)
			set_Value (COLUMNNAME_BP_Location_ID, null);
		else
			set_Value (COLUMNNAME_BP_Location_ID, Integer.valueOf(BP_Location_ID));
	}

	/** Get BP Address.
		@return Address of the Business Partner
	  */
	public int getBP_Location_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_BP_Location_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Bld Mtm Install.
		@param Bld_Mtm_Install_ID Bld Mtm Install
	*/
	public void setBld_Mtm_Install_ID (int Bld_Mtm_Install_ID)
	{
		if (Bld_Mtm_Install_ID < 1)
			set_ValueNoCheck (COLUMNNAME_Bld_Mtm_Install_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_Bld_Mtm_Install_ID, Integer.valueOf(Bld_Mtm_Install_ID));
	}

	/** Get Bld Mtm Install.
		@return Bld Mtm Install	  */
	public int getBld_Mtm_Install_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Bld_Mtm_Install_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Bld_Mtm_Install_UU.
		@param Bld_Mtm_Install_UU Bld_Mtm_Install_UU
	*/
	public void setBld_Mtm_Install_UU (String Bld_Mtm_Install_UU)
	{
		set_ValueNoCheck (COLUMNNAME_Bld_Mtm_Install_UU, Bld_Mtm_Install_UU);
	}

	/** Get Bld_Mtm_Install_UU.
		@return Bld_Mtm_Install_UU	  */
	public String getBld_Mtm_Install_UU()
	{
		return (String)get_Value(COLUMNNAME_Bld_Mtm_Install_UU);
	}

	public org.compiere.model.I_C_Activity getC_Activity() throws RuntimeException
	{
		return (org.compiere.model.I_C_Activity)MTable.get(getCtx(), org.compiere.model.I_C_Activity.Table_ID)
			.getPO(getC_Activity_ID(), get_TrxName());
	}

	/** Set Activity.
		@param C_Activity_ID Business Activity
	*/
	public void setC_Activity_ID (int C_Activity_ID)
	{
		if (C_Activity_ID < 1)
			set_Value (COLUMNNAME_C_Activity_ID, null);
		else
			set_Value (COLUMNNAME_C_Activity_ID, Integer.valueOf(C_Activity_ID));
	}

	/** Get Activity.
		@return Business Activity
	  */
	public int getC_Activity_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Activity_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException
	{
		return (org.compiere.model.I_C_BPartner)MTable.get(getCtx(), org.compiere.model.I_C_BPartner.Table_ID)
			.getPO(getC_BPartner_ID(), get_TrxName());
	}

	/** Set Business Partner.
		@param C_BPartner_ID Identifies a Business Partner
	*/
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		if (C_BPartner_ID < 1)
			set_ValueNoCheck (COLUMNNAME_C_BPartner_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_BPartner_ID, Integer.valueOf(C_BPartner_ID));
	}

	/** Get Business Partner.
		@return Identifies a Business Partner
	  */
	public int getC_BPartner_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_BPartner_Location getC_BPartner_Location() throws RuntimeException
	{
		return (org.compiere.model.I_C_BPartner_Location)MTable.get(getCtx(), org.compiere.model.I_C_BPartner_Location.Table_ID)
			.getPO(getC_BPartner_Location_ID(), get_TrxName());
	}

	/** Set Partner Location.
		@param C_BPartner_Location_ID Identifies the (ship to) address for this Business Partner
	*/
	public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
	{
		if (C_BPartner_Location_ID < 1)
			set_ValueNoCheck (COLUMNNAME_C_BPartner_Location_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_BPartner_Location_ID, Integer.valueOf(C_BPartner_Location_ID));
	}

	/** Get Partner Location.
		@return Identifies the (ship to) address for this Business Partner
	  */
	public int getC_BPartner_Location_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_Location_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Comments.
		@param Comments Comments or additional information
	*/
	public void setComments (String Comments)
	{
		set_Value (COLUMNNAME_Comments, Comments);
	}

	/** Get Comments.
		@return Comments or additional information
	  */
	public String getComments()
	{
		return (String)get_Value(COLUMNNAME_Comments);
	}

	/** Set Description.
		@param Description Optional short description of the record
	*/
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription()
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Name.
		@param Name Alphanumeric identifier of the entity
	*/
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName()
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

	/** Set Process Now.
		@param Processing Process Now
	*/
	public void setProcessing (boolean Processing)
	{
		set_Value (COLUMNNAME_Processing, Boolean.valueOf(Processing));
	}

	/** Get Process Now.
		@return Process Now	  */
	public boolean isProcessing()
	{
		Object oo = get_Value(COLUMNNAME_Processing);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	public org.compiere.model.I_AD_User getSalesRep() throws RuntimeException
	{
		return (org.compiere.model.I_AD_User)MTable.get(getCtx(), org.compiere.model.I_AD_User.Table_ID)
			.getPO(getSalesRep_ID(), get_TrxName());
	}

	/** Set Sales Representative.
		@param SalesRep_ID Sales Representative or Company Agent
	*/
	public void setSalesRep_ID (int SalesRep_ID)
	{
		if (SalesRep_ID < 1)
			set_ValueNoCheck (COLUMNNAME_SalesRep_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_SalesRep_ID, Integer.valueOf(SalesRep_ID));
	}

	/** Get Sales Representative.
		@return Sales Representative or Company Agent
	  */
	public int getSalesRep_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_SalesRep_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Accepted = Acc */
	public static final String STATUS_Accepted = "Acc";
	/** Awaiting Certificate = ace */
	public static final String STATUS_AwaitingCertificate = "ace";
	/** Complete = com */
	public static final String STATUS_Complete = "com";
	/** Install Booked = in */
	public static final String STATUS_InstallBooked = "in";
	/** Incomplete = incom */
	public static final String STATUS_Incomplete = "incom";
	/** Partial Installation = par */
	public static final String STATUS_PartialInstallation = "par";
	/** Production = pr */
	public static final String STATUS_Production = "pr";
	/** Pre Production = pre */
	public static final String STATUS_PreProduction = "pre";
	/** Production Complete = prodcom */
	public static final String STATUS_ProductionComplete = "prodcom";
	/** Quote = qte */
	public static final String STATUS_Quote = "qte";
	/** Set Status.
		@param Status Status of the currently running check
	*/
	public void setStatus (String Status)
	{

		set_Value (COLUMNNAME_Status, Status);
	}

	/** Get Status.
		@return Status of the currently running check
	  */
	public String getStatus()
	{
		return (String)get_Value(COLUMNNAME_Status);
	}

	public org.compiere.model.I_AD_User getSupervisor() throws RuntimeException
	{
		return (org.compiere.model.I_AD_User)MTable.get(getCtx(), org.compiere.model.I_AD_User.Table_ID)
			.getPO(getSupervisor_ID(), get_TrxName());
	}

	/** Set Supervisor.
		@param Supervisor_ID Supervisor for this user/organization - used for escalation and approval
	*/
	public void setSupervisor_ID (int Supervisor_ID)
	{
		if (Supervisor_ID < 1)
			set_Value (COLUMNNAME_Supervisor_ID, null);
		else
			set_Value (COLUMNNAME_Supervisor_ID, Integer.valueOf(Supervisor_ID));
	}

	/** Get Supervisor.
		@return Supervisor for this user/organization - used for escalation and approval
	  */
	public int getSupervisor_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Supervisor_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Search Key.
		@param Value Search key for the record in the format required - must be unique
	*/
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue()
	{
		return (String)get_Value(COLUMNNAME_Value);
	}

	/** Set checkmeasure_booked.
		@param checkmeasure_booked checkmeasure_booked
	*/
	public void setcheckmeasure_booked (boolean checkmeasure_booked)
	{
		set_Value (COLUMNNAME_checkmeasure_booked, Boolean.valueOf(checkmeasure_booked));
	}

	/** Get checkmeasure_booked.
		@return checkmeasure_booked	  */
	public boolean ischeckmeasure_booked()
	{
		Object oo = get_Value(COLUMNNAME_checkmeasure_booked);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set contract_received.
		@param contract_received contract_received
	*/
	public void setcontract_received (boolean contract_received)
	{
		set_Value (COLUMNNAME_contract_received, Boolean.valueOf(contract_received));
	}

	/** Get contract_received.
		@return contract_received	  */
	public boolean iscontract_received()
	{
		Object oo = get_Value(COLUMNNAME_contract_received);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Date Promised.
		@param date_promised Date Promised
	*/
	public void setdate_promised (Timestamp date_promised)
	{
		set_Value (COLUMNNAME_date_promised, date_promised);
	}

	/** Get Date Promised.
		@return Date Promised	  */
	public Timestamp getdate_promised()
	{
		return (Timestamp)get_Value(COLUMNNAME_date_promised);
	}

	/** Set is_checkmeasured.
		@param is_checkmeasured is_checkmeasured
	*/
	public void setis_checkmeasured (boolean is_checkmeasured)
	{
		set_Value (COLUMNNAME_is_checkmeasured, Boolean.valueOf(is_checkmeasured));
	}

	/** Get is_checkmeasured.
		@return is_checkmeasured	  */
	public boolean is_checkmeasured()
	{
		Object oo = get_Value(COLUMNNAME_is_checkmeasured);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set rectifyamount.
		@param rectifyamount rectifyamount
	*/
	public void setrectifyamount (BigDecimal rectifyamount)
	{
		set_Value (COLUMNNAME_rectifyamount, rectifyamount);
	}

	/** Get rectifyamount.
		@return rectifyamount	  */
	public BigDecimal getrectifyamount()
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_rectifyamount);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set resolution.
		@param resolution resolution
	*/
	public void setresolution (String resolution)
	{
		set_Value (COLUMNNAME_resolution, resolution);
	}

	/** Get resolution.
		@return resolution	  */
	public String getresolution()
	{
		return (String)get_Value(COLUMNNAME_resolution);
	}
}