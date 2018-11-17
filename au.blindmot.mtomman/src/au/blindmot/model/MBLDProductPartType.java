package au.blindmot.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MProduct;
import org.compiere.model.PO;
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
	
	public MBLDProductPartType(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
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
 * @param mPartypeID
 * @param trxName
 * @return
 */
public static MProduct[] getPartSetProducts(int mProductID, int mPartypeID, String trxName) {
	if (s_log.isLoggable(Level.FINE)) s_log.fine("From M_Product_ID=" + mProductID);
	if (mProductID == 0) return null;
	
	HashSet<MProduct> products= new HashSet<MProduct>();
	StringBuffer sql = new StringBuffer();
	sql.append("SELECT mpb.m_productbom_id "); 
	sql.append("FROM m_product_bom mpb ");
	sql.append("JOIN m_product mp ON mp.m_product_id = mpb.m_productbom_id ");
	sql.append("JOIN m_parttype mpt ON mpt.m_parttype_id = mp.m_parttype_id ");
	sql.append("JOIN bld_product_parttype bpt ON bpt.m_parttype_id = mpt.m_parttype_id ");
	sql.append("WHERE mpb.m_product_id = ? ");
	sql.append("AND bpt.m_parttype_id = ? ");
	sql.append("ORDER BY bpt.m_parttype_id");

	PreparedStatement pstmt = null;
	ResultSet rs = null;
	try
	{
		pstmt = DB.prepareStatement(sql.toString(), null);
		pstmt.setInt(1, mProductID);
		pstmt.setInt(2, mPartypeID);
		rs = pstmt.executeQuery();
		while(rs.next())
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
public static PO  getMBLDProductPartType  (int mPartTypeID, int mProductID, Properties ctx, String trxName)
{
	final String whereClause = I_BLD_Product_PartType.COLUMNNAME_BLD_Product_PartType_ID +"=? AND " + I_BLD_Product_PartType.COLUMNNAME_M_Product__ID+"=?";
	PO retValue = new Query(ctx,I_BLD_Product_PartType.Table_Name, whereClause,trxName)
	.setParameters(mPartTypeID, mProductID)
	.first();

	return retValue;
}


public MBLDLineProductInstance getMBldLineProductInstance(int m_MbldLineProductsetInstanceID) {
	
	StringBuilder sql = new StringBuilder();
	sql.append("SELECT bld_line_productinstance_id ");
	sql.append("FROM bld_line_productinstance ");
	sql.append("WHERE bld_product_parttype_id = ? ");
	sql.append("AND bld_line_productsetinstance_id = ? ");
	MBLDLineProductInstance retValue = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	int partTypeID = get_ID();
	try
	{
		pstmt = DB.prepareStatement(sql.toString(), null);
		pstmt.setInt(1, partTypeID);
		pstmt.setInt(2, m_MbldLineProductsetInstanceID);
		rs = pstmt.executeQuery();
		if(rs.next())
		{
			int BldLineProductinstanceID  = rs.getInt(1);
			retValue = new MBLDLineProductInstance (Env.getCtx(), BldLineProductinstanceID, get_TrxName());
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
	
	return retValue;
	
} //GetMBldLineProductInstance
	

public void setMBLDLineProductInstance(int m_MbldLineProductsetInstanceID, MProduct value, boolean keep) {
	/*Load the correct MBLDLineProductInstance object
	 * 
	 */
	int mProductID = 0;
	if(value != null)
	{
		mProductID = value.getM_Product_ID();
	}
	
	System.out.println("MBLDProductPartType.get_ID(): " + get_ID());
	System.out.println("MBLDProductPartType.getBLD_M_PartType_ID()(): " + getBLD_M_PartType_ID());
	MBLDLineProductInstance mBLDLineProductInstance = getMBldLineProductInstance(m_MbldLineProductsetInstanceID);

	if (mBLDLineProductInstance == null && keep)//Doesn't exist yet - create new if we want to keep it.
	{
		
		mBLDLineProductInstance = new MBLDLineProductInstance(p_ctx, 0, get_TrxName());
		mBLDLineProductInstance.setBLD_Line_ProductSetInstance_ID(m_MbldLineProductsetInstanceID);
		mBLDLineProductInstance.setM_Product_ID(mProductID );
		mBLDLineProductInstance.setBLD_Product_PartType_ID(get_ID());
		mBLDLineProductInstance.saveEx();
	}
	if(keep)//only save if we want to keep
	{
		mBLDLineProductInstance.setM_Product_ID(mProductID );
		mBLDLineProductInstance.save();
	}
	if(mBLDLineProductInstance != null && !keep)//delete existing if we don't want to keep.
	{
		mBLDLineProductInstance.delete(true, get_TrxName());
	}
	
}

public static MBLDLineProductInstance[] getmBLDLineProductInstance(int bld_Line_ProductSetInstance_ID, String trxn) {
	
	StringBuilder sql = new StringBuilder();
	sql.append("SELECT bld_line_productinstance_id ");
	sql.append("FROM bld_line_productinstance ");
	sql.append("WHERE bld_line_productsetinstance_id = ? ");
	sql.append("ORDER BY bld_product_parttype_id");
	MBLDLineProductInstance[] retValueArray  = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	ArrayList<MBLDLineProductInstance> mBPSI = new ArrayList<MBLDLineProductInstance>();
	
	try
	{
		pstmt = DB.prepareStatement(sql.toString(), null);
		pstmt.setInt(1, bld_Line_ProductSetInstance_ID);
		rs = pstmt.executeQuery();
		while(rs.next())
		{
			int mBLDLineProductInstanceID = rs.getInt(1);
			MBLDLineProductInstance addValue = new MBLDLineProductInstance (Env.getCtx(), mBLDLineProductInstanceID, trxn);
			//Does this need to be saved? addValue.saveEx();
			mBPSI.add(addValue);
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
	retValueArray = new MBLDLineProductInstance[mBPSI.size()];
	return mBPSI.toArray(retValueArray);
	
	}

}
