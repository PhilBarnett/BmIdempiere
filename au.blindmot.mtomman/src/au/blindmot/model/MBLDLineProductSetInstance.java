package au.blindmot.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MProduct;
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


	public static MBLDLineProductSetInstance get(Properties ctx, int m_MbldLineProductsetInstanceID,
			int m_M_Product_ID) {
		
		return new MBLDLineProductSetInstance(ctx, m_MbldLineProductsetInstanceID, null);
	}
	
	public void setDescription(int mProductID)
	{
		//	Make sure we have a Product Set
		MBLDProductPartType[] pps = getProductPartSet(mProductID, get_TrxName());
		if (pps == null)
		{
			setDescription ("");
			return;
		}
		/*
		 * TODO: This method should iterate through pps, get the instance value and write it to the string.
		 * Currently, instance is null because partType.getmBLDLineProductInstance diesn't work and should be deleted.
		 */
		StringBuilder sb = new StringBuilder();
		
		//	Instance Values
		MBLDProductPartType partType = new MBLDProductPartType(p_ctx, 0, get_TrxName());
		MBLDLineProductInstance[] instance = partType.getmBLDLineProductInstance(getBLD_Line_ProductSetInstance_ID());
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
public  MBLDProductPartType[] getProductPartSet(int mProductID, String trxName) {
	String tranName;
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
			System.out.println("partTypeToAdd: " + partTypeToAdd);
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
				Integer partTypeToAdd = new Integer(bldproductparttypeid);
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
	public MBLDLineProductInstance getMBLDLineProductInstance (int BldLineProductSetInstanceID, int mBldPartTypeID, int mProductID)
	{
		MBLDLineProductInstance retInstance = null;
		if(mProductID > 0)
		{
			retInstance = new MBLDLineProductInstance(p_ctx, mBldPartTypeID, 
					BldLineProductSetInstanceID, mProductID, get_TrxName());
			return retInstance;
		}
		else
		{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT m_product_id "); 
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
				mProductID = rs.getInt(1);
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
		}
		
		retInstance = new MBLDLineProductInstance(p_ctx, null, get_TrxName(), mBldPartTypeID, BldLineProductSetInstanceID, mProductID);
		return retInstance;
		
	}
}	//	getAttributeInstance
	
	

