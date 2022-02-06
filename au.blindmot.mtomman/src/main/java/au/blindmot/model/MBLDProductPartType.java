package au.blindmot.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.webui.window.FDialog;
import org.compiere.model.MProduct;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_M_PartType;
import org.compiere.model.X_M_Product;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.utils.MtmUtils;

public class MBLDProductPartType extends X_BLD_Product_PartType {

	/**
	 * 
	 */
	private static final long serialVersionUID = -978856611260313111L;
	static CLogger log = CLogger.getCLogger(MBLDProductPartType.class);

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
	if (log.isLoggable(Level.FINE)) log.fine("From M_Product_ID=" + mProductID);
	if (mProductID == 0) return null;
	int activePPBomID = MtmUtils.getActivePPProductBomID(mProductID);
	
	List<MProduct> products= new ArrayList<MProduct>();
	StringBuffer sql = new StringBuffer();
	sql.append("SELECT mpb.m_product_id "); 
	sql.append("FROM pp_product_bomline mpb ");
	sql.append("JOIN m_product mp ON mp.m_product_id = mpb.m_product_id ");
	sql.append("JOIN m_parttype mpt ON mpt.m_parttype_id = mp.m_parttype_id ");
	sql.append("JOIN bld_product_parttype bpt ON bpt.m_parttype_id = mpt.m_parttype_id ");
	sql.append("JOIN pp_product_bom ppb ON ppb.pp_product_bom_id = mpb.pp_product_bom_id ");
	sql.append("WHERE bpt.m_parttype_id = ? ");
	sql.append("AND ppb.pp_product_bom_id = ? ");
	sql.append("AND mpb.isactive = 'Y'");
	sql.append("ORDER BY mp.name");

	PreparedStatement pstmt = null;
	ResultSet rs = null;
	try
	{
		pstmt = DB.prepareStatement(sql.toString(), null);
		pstmt.setInt(1, mPartypeID);
		pstmt.setInt(2, activePPBomID);
		rs = pstmt.executeQuery();
		while(rs.next())
		{
			int mProductBomID  = rs.getInt(1);
			MProduct partToAdd = new MProduct(Env.getCtx(), mProductBomID, null);
			//Don't add duplicate product names. Check if product already exists by name in products.
			if(!hasProduct(products, partToAdd))
			{
				products.add(partToAdd);
			}
			
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
	MProduct[] partsArray = new MProduct[products.size()];
	
	/*
	 //Testing output
	for(MProduct product : products)
	 {
		 log.warning(product.getName());
	 }
	 */
	return products.toArray(partsArray);
	
	}

/**
 * Checks to see if product name is already in the HashSet
 * @param products
 * @param toAdd
 * @return
 */
private static boolean hasProduct(List<MProduct> products, MProduct toAdd) {
	for(Object toCheck : products)
	{
		String addName = toAdd.getName();
		String checkName = ((X_M_Product) toCheck).getName();
		//toCheck = (MProduct) toCheck;
		if (addName.equalsIgnoreCase(checkName))
		{
			return true;
		}
	}
	
	return false;
	
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
	final String whereClause = I_BLD_Product_PartType.COLUMNNAME_BLD_Product_PartType_ID +"=? AND " + I_BLD_Product_PartType.COLUMNNAME_M_Product_ID+"=?";
	PO retValue = new Query(ctx,I_BLD_Product_PartType.Table_Name, whereClause,trxName)
	.setParameters(mPartTypeID, mProductID)
	.first();

	return retValue;
}

/**
 * 
 * @param ctx
 * @param mProductID
 * @param trxName
 * @return
 */
public static MBLDProductPartType [] getMBLDProductPartTypes(Properties ctx, int mProductID, String trxName) {
	final String whereClause = I_BLD_Product_PartType.COLUMNNAME_M_Product_ID +"=?";
	List<MBLDProductPartType> retValue = new Query(ctx,I_BLD_Product_PartType.Table_Name, whereClause,trxName)
	.setParameters(mProductID)
	.list();
	 
	return retValue.toArray(new MBLDProductPartType[retValue.size()]);
}


/**
 * 
 * @param m_MbldLineProductsetInstanceID
 * @return
 */
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
		log.log(Level.SEVERE, sql.toString(), ex);
	}
	finally
	{
		DB.close(rs, pstmt);
		rs = null; pstmt = null;
	}
	
	return retValue;
	
} //GetMBldLineProductInstance
	

/**
 * 
 * @param m_MbldLineProductsetInstanceID
 * @param value
 * @param keep
 */
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
	System.out.println("MBLDProductPartType.getBLD_M_PartType_ID()(): " + getBLD_Product_PartType_ID());
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

/**
 * 
 * @param bld_Line_ProductSetInstance_ID
 * @param trxn
 * @return
 */
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
		log.log(Level.SEVERE, sql.toString(), ex);
	}
	finally
	{
		DB.close(rs, pstmt);
		rs = null; pstmt = null;
	}
	retValueArray = new MBLDLineProductInstance[mBPSI.size()];
	return mBPSI.toArray(retValueArray);
	
	}

/**
 * 
 * @param mBLDProductPartTypeID
 * @param trxn
 * @return
 */
public MBLDProductNonSelect[] getMBLDProductNonSelectLines(int mBLDProductPartTypeID, String trxn) {
	log.warning("getName(): " + getName() + " is_user_select = " + get_ValueAsString("is_user_select") + " is_user_select = " + get_ValueAsBoolean("is_user_select"));
	
	if(get_ValueAsBoolean("is_user_select")) return new MBLDProductNonSelect[0];
	
	StringBuilder sql = new StringBuilder();
	sql.append("SELECT bld_product_non_select_id ");
	sql.append("FROM bld_product_non_select ");
	sql.append("WHERE bld_product_parttype_id = ? ");
	sql.append("ORDER BY bld_product_parttype_id");
	MBLDProductNonSelect[] retValueArray  = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	ArrayList<MBLDProductNonSelect> mPNS = new ArrayList<MBLDProductNonSelect>();
	
	try
	{
		pstmt = DB.prepareStatement(sql.toString(), null);
		pstmt.setInt(1, mBLDProductPartTypeID);
		rs = pstmt.executeQuery();
		while(rs.next())
		{
			int mBLDProductNonSelectID = rs.getInt(1);
			MBLDProductNonSelect addValue = new MBLDProductNonSelect (Env.getCtx(), mBLDProductNonSelectID, trxn);
			mPNS.add(addValue);
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
	retValueArray = new MBLDProductNonSelect[mPNS.size()];
	return mPNS.toArray(retValueArray);
	
}



/**
 * @override
 */
protected boolean beforeSave(boolean newRecord){
	MBLDProductNonSelect[] mBLDProductNonSelectArray = getMBLDProductNonSelectLines(get_ID(), get_TrxName());
	int currentID = get_ID();
	for(int i = 0; i < mBLDProductNonSelectArray.length; i++)
	{
		if(currentID != mBLDProductNonSelectArray[i].getM_PartType_ID())
		{
			X_M_PartType partType = new X_M_PartType(getCtx(), getM_PartTypeID(), null);
			FDialog.warn(0, "There are Non Selectable Part Types attached to this record that do not have the part Type: " + partType.getName() + ". This may cause unexpected results");
		}
	}
	
	return true;

	}
}
