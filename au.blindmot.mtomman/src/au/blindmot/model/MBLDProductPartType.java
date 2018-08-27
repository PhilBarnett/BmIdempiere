package au.blindmot.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.model.X_M_PartType;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MBLDProductPartType extends X_BLD_Product_PartType {

	/**
	 * 
	 */
	private static final long serialVersionUID = -978856611260313111L;
	static CLogger s_log = CLogger.getCLogger(MBLDProductPartType.class);

	public MBLDProductPartType(Properties ctx, int BLD_Product_PartType_ID, String trxName) {
		super(ctx, BLD_Product_PartType_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName() {
		
		X_M_PartType parentType = new X_M_PartType(p_ctx, this.getBLD_M_PartType_ID() , get_TrxName());
		if(parentType != null)
			{
				return parentType.getName(); 
			}
		return "";
		
	}
	
	
	/**
	 * 
	 * @param mProductID
	 * @param trxName
	 * @return
	 */
public static X_M_PartType[] getProductPartSet(int mProductID, String trxName) {
	if (s_log.isLoggable(Level.FINE)) s_log.fine("From M_Product_ID=" + mProductID);
	if (mProductID == 0)
		return null;
	ArrayList<X_M_PartType> partTypes= new ArrayList<X_M_PartType>();
	
	StringBuffer sql = new StringBuffer("SELECT m_parttype_id ");
	sql.append("FROM bld_product_parttype ");
		sql.append("WHERE M_Product_ID=?");
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	try
	{
		pstmt = DB.prepareStatement(sql.toString(), null);
		pstmt.setInt(1, mProductID);
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			int mParttypeid  = rs.getInt(1);
			X_M_PartType partTypeToAdd = new X_M_PartType(Env.getCtx(), mParttypeid, null);
			partTypes.add(partTypeToAdd);
		}
	}
	catch (SQLException ex)
	{
		s_log.log(Level.SEVERE, sql.toString(), ex);
	}
	finally
	{
		DB.close(rs, pstmt);
		rs = null; pstmt = null;
	}
	X_M_PartType[] partsArray = new X_M_PartType[partTypes.size()];
	return partTypes.toArray(partsArray);
	
}

/**
 * 
 * @param mProductID
 * @param mPartypeID
 * @param trxName
 * @return
 */
public static MProduct[] getPartSetProducts(int mProductID, int mPartypeID, String trxName) {
	if (s_log.isLoggable(Level.FINE)) s_log.fine("From M_Product_ID=" + mProductID);
	if (mProductID == 0)
		return null;
	ArrayList<MProduct> products= new ArrayList<MProduct>();
	StringBuffer sql = new StringBuffer();
	sql.append("SELECT mpb.m_product_bom_id, "); 
	sql.append("mp.name ");
	sql.append("FROM m_product_bom mpb ");
	sql.append("JOIN m_product mp ON mp.m_product_id = mpb.m_product_bom_id ");
	sql.append("JOIN m_parttype mpt ON mpt.m_parttype_id = mp.m_parttype_id ");
	sql.append("WHERE mpb.m_product_id = ?");
	sql.append("AND mpt.m_parttype_id = ?");

	PreparedStatement pstmt = null;
	ResultSet rs = null;
	try
	{
		pstmt = DB.prepareStatement(sql.toString(), null);
		pstmt.setInt(1, mProductID);
		pstmt.setInt(2, mPartypeID);
		rs = pstmt.executeQuery();
		if (rs.next())
		{
			int mProductBomID  = rs.getInt(1);
			MProduct partToAdd = new MProduct(Env.getCtx(), mProductBomID, null);
			products.add(partToAdd);
		}
	}
	catch (SQLException ex)
	{
		s_log.log(Level.SEVERE, sql.toString(), ex);
	}
	finally
	{
		DB.close(rs, pstmt);
		rs = null; pstmt = null;
	}
	MProduct[] partsArray = new MProduct[products.size()];
	return products.toArray(partsArray);
	
	}

/**
 * 
 * @param mPartTypeID
 * @param mProductID
 * @param ctx
 * @param trxName
 * @return
 */
public static MBLDProductPartType  getMBLDProductPartType  (int mPartTypeID, int mProductID, Properties ctx, String trxName)
{
	final String whereClause = I_BLD_Product_PartType.COLUMNNAME_BLD_Product_PartType_ID +"=? AND "+I_BLD_Line_ProductInstance.COLUMNNAME_BLD_Line_ProductSetInstance_ID+"=?";
	MBLDProductPartType retValue = new Query(ctx,I_BLD_Product_PartType.Table_Name, whereClause,trxName)
	.setParameters(mPartTypeID, mProductID)
	.first();

	return retValue;
}

/**
 * 
 * @param BldLineProductSetInstanceID
 * @return
 */
public MBLDLineProductInstance getMBLDLineProductInstance (int BldLineProductSetInstanceID)
{
	final String whereClause = I_BLD_Line_ProductInstance.COLUMNNAME_BLD_Product_PartType_ID +"=? AND "+I_BLD_Line_ProductInstance.COLUMNNAME_BLD_Line_ProductSetInstance_ID+"=?";
	MBLDLineProductInstance retValue = new Query(getCtx(),I_BLD_Line_ProductInstance.Table_Name,whereClause,get_TrxName())
	.setParameters(getBLD_Product_PartType_ID(),BldLineProductSetInstanceID)
	.first();

	return retValue;
}	//	getAttributeInstance


}
