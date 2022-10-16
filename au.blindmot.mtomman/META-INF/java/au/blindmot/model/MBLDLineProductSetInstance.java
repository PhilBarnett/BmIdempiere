package au.blindmot.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MProduct;
import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

public class MBLDLineProductSetInstance extends X_BLD_Line_ProductSetInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3522187502958971349L;
	CLogger log = CLogger.getCLogger(MBLDLineProductSetInstance.class);

	public MBLDLineProductSetInstance(Properties ctx, int BLD_Line_ProductSetInstance_ID, String trxName) {
		super(ctx, BLD_Line_ProductSetInstance_ID, trxName);
		// TODO Auto-generated constructor stub
	}


	public MBLDLineProductSetInstance(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}


	public static MBLDLineProductSetInstance get(Properties ctx, int m_MbldLineProductsetInstanceID ,
			int m_M_Product_ID) {
		
		return new MBLDLineProductSetInstance(ctx, m_MbldLineProductsetInstanceID, null);
		
		/*if (m_MbldLineProductsetInstanceID <= 0)
		{
			return new MBLDLineProductSetInstance(ctx, m_MbldLineProductsetInstanceID, null);
		}
		Integer key = Integer.valueOf(m_MbldLineProductsetInstanceID);
		MBLDLineProductSetInstance retValue = (MBLDLineProductSetInstance) s_cache.get (key);
		if (retValue != null)
		{
			return retValue;
		}
		retValue = new MBLDLineProductSetInstance (ctx, m_MbldLineProductsetInstanceID, null);
		if (retValue.get_ID () != 0)
		{
			s_cache.put (key, retValue);
		}
		return retValue;
		*/
		
		//TODO: Delete parameter int m_M_Product_ID - not required, only one call in WBldPartsDialog to break/fix.
		/*
		 * /TODO: Change this method to emulate public static MProduct get (Properties ctx, int M_Product_ID)
		 * This will use caching of the object
		 */
		
		//return new MBLDLineProductSetInstance(ctx, m_MbldLineProductsetInstanceID, null);
		
	}
	
	/**	Cache						
	 * See MProduct on how to use the cache to fetch objects*/
	private static CCache<Integer,MBLDLineProductSetInstance> s_cache	= new CCache<Integer,MBLDLineProductSetInstance>(Table_Name, 40, 60);	//	60 minutes
	
	public void setDescription(int mProductID)
	{
		//	Make sure we have a Product Set
		log.warning("--------In MBLDLineProductSetInstance.setDescription(int mProductID)");
		MBLDProductPartType[] pps = getProductPartSet(mProductID, get_TrxName(), true);
		log.warning("--------In MBLDLineProductSetInstance.setDescription pps == " + pps.toString() + "mProductID == " + mProductID);
		
		if (pps == null)
		{
			setDescription ("");
			return;
		}
		/*
		 * TODO: This method should iterate through pps, get the instance value and write it to the string.
		 * Currently, instance is null because partType.getmBLDLineProductInstance doesn't work and should be deleted.
		 */
		StringBuilder sb = new StringBuilder();
		
		//	Instance Values
		MBLDLineProductInstance[] instance = MBLDLineProductInstance.getmBLDLineProductInstance(getBLD_Line_ProductSetInstance_ID(), get_TrxName());
		for (int i = 0; i < instance.length; i++)
		{
			
			if (instance != null && instance[i].getM_Product_ID() > 0)
			{
				MProduct nameAdd = new MProduct(p_ctx, instance[i].getM_Product_ID(), get_TrxName());
				if (sb.length() > 0)
					sb.append("_");
				sb.append(nameAdd.getName());
			}
		}

		setDescription (sb.toString());
	}	//	setDescription

	/**
	 * 
	 * @param mProductID
	 * @param trxName
	 * @return
	 */
public  MBLDProductPartType[] getProductPartSet(int mProductID, String trxName, boolean isUserSelect) {
	//mProductID is finished product ID.
	String tranName;
	String userSelect;
	if(isUserSelect)
	{
		userSelect = "Y";
	}
	else
	{
		userSelect = "N";
	}
	if(trxName != null)
	{
		tranName = trxName;
	}
	else
	{
		tranName = get_TrxName();
	}
	if (log.isLoggable(Level.FINE)) log.fine("From M_Product_ID=" + mProductID);
	if (mProductID == 0)
		return null;
	ArrayList<MBLDProductPartType> partTypes= new ArrayList<MBLDProductPartType>();
	
	StringBuffer sql = new StringBuffer("SELECT bld_product_parttype_id ");
	sql.append("FROM bld_product_parttype ");
		sql.append("WHERE M_Product_ID=?");
		sql.append(" AND isactive = 'Y' ");
		sql.append(" AND is_user_select = '" + userSelect + "'");
		sql.append(" ORDER BY line");
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	try
	{
		pstmt = DB.prepareStatement(sql.toString(), null);
		pstmt.setInt(1, mProductID);
		rs = pstmt.executeQuery();
		while (rs.next())
		{
			int bldproductparttypeid  = rs.getInt(1);
			MBLDProductPartType partTypeToAdd = new MBLDProductPartType(p_ctx, bldproductparttypeid , tranName);
			System.out.println("partTypeToAdd: " + partTypeToAdd.getName());
			partTypeToAdd.save();
			partTypes.add(partTypeToAdd);
		}
	}
	catch (SQLException ex)
	{
		log.log(Level.SEVERE, sql.toString(), ex);
	}
	finally
	{
		DB.close(rs, pstmt);
		rs = null; pstmt = null;
	}
	
	MBLDProductPartType[] partsArray = new MBLDProductPartType[partTypes.size()];
	return partTypes.toArray(partsArray);
	
}


	public Integer[] getPartTypes(int mProductID) {
		
		if (log.isLoggable(Level.FINE)) log.fine("From M_Product_ID=" + mProductID);
		if (mProductID == 0)
			return null;
		ArrayList<Integer> partTypes= new ArrayList<Integer>();
		
		StringBuffer sql = new StringBuffer("SELECT bld_product_parttype_id ");
		sql.append("FROM bld_product_parttype ");
		sql.append("WHERE M_Product_ID=?");
		sql.append(" AND isactive = 'Y'");
		sql.append(" ORDER BY line");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, mProductID);
			rs = pstmt.executeQuery();
			while (rs.next())
			{
				int bldproductparttypeid  = rs.getInt(1);
				Integer partTypeToAdd = Integer.valueOf(bldproductparttypeid);
				System.out.println("partTypeToAdd: " + partTypeToAdd);
				partTypes.add(partTypeToAdd);
			}
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, sql.toString(), ex);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		
		Integer[] partsArray = new Integer[partTypes.size()];
		return partTypes.toArray(partsArray);
	}
	
	
	/**Gets instance. creates new if mProductID == 0
	 * 
	 * @param BldLineProductSetInstanceID
	 * @param mBldPartTypeID
	 * @param mProductID
	 * @return
	 */
	public MBLDLineProductInstance getMBLDLineProductInstance (int BldLineProductSetInstanceID, int mBldPartTypeID)//, int mProductID)
	{
		MBLDLineProductInstance retInstance = null;
		int bldLineProductInstanceID = 0;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT bld_line_productinstance_id "); 
		sql.append("FROM bld_line_productinstance ");
		sql.append("WHERE bld_line_productsetinstance_id = ? ");
		sql.append("AND bld_product_parttype_id = ? ");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, BldLineProductSetInstanceID);
			pstmt.setInt(2, mBldPartTypeID);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				bldLineProductInstanceID = rs.getInt(1);
			}
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, sql.toString(), ex);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		
		
		if(bldLineProductInstanceID > 0)
		{
			retInstance = new MBLDLineProductInstance(p_ctx, bldLineProductInstanceID, get_TrxName());
			retInstance.save();
			return retInstance;
		}
		
		//
		
		/*
		if(mProductID > 0)
		{
			retInstance = new MBLDLineProductInstance(p_ctx, 0, get_TrxName());
			retInstance.setM_Product_ID(mProductID );
			retInstance.save();
			return retInstance;
		}
		*/
		
		return retInstance;
		
	}
	
	
	/**
	 * Not required yet
	@Override
	
	protected boolean afterSave(boolean newRecord, boolean success) 
	{
		if (super.afterSave(newRecord, success)) 
		{
			if (newRecord && success)
			{
				//use id as description when description is empty
				String desc = this.getDescription();
				if (desc == null || desc.trim().length() == 0)
				{
					this.set_ValueNoCheck("Description", Integer.toString(getBLD_Line_ProductSetInstance_ID()));
					String sql = "UPDATE bld_line_productsetinstance SET Description = ? WHERE M_AttributeSetInstance_ID = ?";
					int no = DB.executeUpdateEx(sql, 
							new Object[]{Integer.toString(getBLD_Line_ProductSetInstance_ID()), getBLD_Line_ProductSetInstance_ID()}, 
							get_TrxName());
					if (no <= 0)
					{
						log.log(Level.SEVERE, "Failed to update description.");
						return false;
					}
				}
			}
			return true;
		}
		
		return false;
	} */
}	//	getAttributeInstance
	
	

