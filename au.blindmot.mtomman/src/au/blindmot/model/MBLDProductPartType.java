package au.blindmot.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MProduct;
import org.compiere.model.X_M_PartType;
import org.compiere.model.X_M_Product_BOM;
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
	
public static X_M_PartType[] getProductPartSet(int mProductID, String trxName) {
	if (s_log.isLoggable(Level.FINE)) s_log.fine("From M_Product_ID=" + mProductID);
	if (mProductID == 0)
		return null;
	ArrayList<X_M_PartType> partTypes= new ArrayList<X_M_PartType>();
	
	String sql = "SELECT m_parttype_id "
		+ "FROM bld_product_parttype "
		+ "WHERE M_Product_ID=?";
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	try
	{
		pstmt = DB.prepareStatement(sql, null);
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
		s_log.log(Level.SEVERE, sql, ex);
	}
	finally
	{
		DB.close(rs, pstmt);
		rs = null; pstmt = null;
	}
	X_M_PartType[] partsArray = new X_M_PartType[partTypes.size()];
	return partTypes.toArray(partsArray);
	
}

public static X_M_Product_BOM[] getPartSetProducts(int mProductID, int mPartypeID, String trxName) {
	if (s_log.isLoggable(Level.FINE)) s_log.fine("From M_Product_ID=" + mProductID);
	if (mProductID == 0)
		return null;
	ArrayList<X_M_Product_BOM> products= new ArrayList<X_M_Product_BOM>();
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
			X_M_Product_BOM partTypeToAdd = new X_M_Product_BOM(Env.getCtx(), mProductBomID, null);
			products.add(partTypeToAdd);
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
	X_M_Product_BOM[] partsArray = new X_M_Product_BOM[products.size()];
	return products.toArray(partsArray);
	
}

}
