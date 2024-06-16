package au.blindmot.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MProduct;
import org.compiere.model.Query;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MPPProductBOM;
import org.eevolution.model.MPPProductBOMLine;

import au.blindmot.utils.MtmUtils;

public class MBLDProductNonSelect extends X_BLD_Product_Non_Select {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String MTM_NON_SELECT_CONDITION_HAS_LIFT_SPRING = "HasLiftSpring";
	public static final String MTM_NON_SELECT_CONDITION_TUNE = "requirestuning";
	public static final String MTM_NON_SELECT_OPERATION_ADDITION = "Addition";
	public static final String MTM_NON_SELECT_OPERATION_CONDITION_SET = "Condition Set";
	public static final String MTM_NON_SELECT_OPERATION_SUBSTITUTION = "Substitution";
	public static final String MTM_NON_SELECT_OPERATION_DELETE = "Deletion";
	public static final String MTM_NON_SELECT_OPERATION_ATTRIBUTE_BASED_ADD = "attribute";
	public static final String MTM_NON_SELECT_OPERATION_ATTRIBUTE_BASED_DELETE = "attribute_del";
	
	public MBLDProductNonSelect(Properties ctx, int BLD_Product_Non_Select_ID, String trxName) {
		super(ctx, BLD_Product_Non_Select_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MBLDProductNonSelect(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @override
	 */
	protected boolean beforeSave(boolean newRecord){
		if(newRecord) return true;
		if(getdrop2() <= getdrop1())
		{
			throw new AdempiereUserError("Drop 2 must be greater than Drop 1.");
		}
		if(getwidth2() <= getwidth1())
		{
			throw new AdempiereUserError("Width 2 must be greater than Width 1.");
		}
		if(getoperation_type().equalsIgnoreCase(MTM_NON_SELECT_OPERATION_SUBSTITUTION))
		{
			 if(getsubstituteproduct() != null && getaddtionalproduct() != null)
			 {
				 if(getsubstituteproduct().equals(getaddtionalproduct()))
						 {
					 		throw new AdempiereUserError("Substitute product must be different to Additional product and neither can be blank.");
						 }
			 }
		}
		
		if(getoperation_type().equalsIgnoreCase(MTM_NON_SELECT_OPERATION_DELETE) && (getsubstituteproduct().toString().equalsIgnoreCase("0")))
		{
			throw new AdempiereUserError("Substitute product must not be blank.");
		}
		log.warning((getaddtionalproduct().toString()));
		if(getoperation_type().equalsIgnoreCase(MTM_NON_SELECT_OPERATION_ADDITION) && (getaddtionalproduct().toString().equalsIgnoreCase("0")))
		{
			throw new AdempiereUserError("Additional product must not be blank.");
		}
		
		//set M_Product_BOM_ID
		BigDecimal additionalProductID = (BigDecimal) getaddtionalproduct();
		BigDecimal substituteProductID = (BigDecimal) getsubstituteproduct();
		BigDecimal xmProductID = (BigDecimal) get_Value(COLUMNNAME_XM_Product_ID);
		
		log.warning("Additional product_id: " + additionalProductID);
		log.warning("substituteProductID: " + substituteProductID);
		log.warning("xmProductID: " + xmProductID);
		
		if(additionalProductID != null && additionalProductID.compareTo(Env.ZERO) > 0)
		{
			setMPProductBOMLine_ID(additionalProductID.intValue());
		}
		if(substituteProductID != null && substituteProductID.compareTo(Env.ZERO) > 0)
		{
			setMPProductBOMLine_ID(substituteProductID.intValue());
		}
		if(xmProductID != null && xmProductID.compareTo(Env.ZERO) > 0)
		{
			setMPProductBOMLine_ID(xmProductID.intValue());
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param nonSelectProductID
	 */
	public void setMPProductBOMLine_ID(int nonSelectProductID) {
	
	MPPProductBOMLine[] bomProducts = getMPProductBOMLines(nonSelectProductID);
	if(bomProducts != null && bomProducts.length > 0)
		{
			int mPProductBOMLineID = bomProducts[0].getPP_Product_BOM_ID();
			set_Value(COLUMNNAME_PP_Product_Bomline_ID , mPProductBOMLineID);
		}
	}
	
	/**
	 * Returns found M_Product_BOM_IDs
	 * @param mProductID
	 * @return
	 */
	public MPPProductBOMLine[] getMPProductBOMLines(int bomMProductID) {
		
		//MtmUtils.getActivePPProductBomID(bomMProductID)
		//MPPProductBOM mPPProductBOM = MPPProductBOM.getDefault(MProduct.get(getM_Product_ID()), get_TrxName());
		
		List<MPPProductBOMLine> mps = null;
		StringBuilder whereClause = new StringBuilder();
		//whereClause.append("m_productbom_id = ");
		whereClause.append("m_product_id = ");//Bom line product_id
		whereClause.append(bomMProductID);
		whereClause.append(" AND m_product_id = ");//parent product_id
	
		log.warning("getM_Product_ID() =" + getM_Product_ID());
		whereClause.append(getM_Product_ID());
		
		
		mps = new Query(Env.getCtx(), MPPProductBOMLine.Table_Name, whereClause.toString(), null).list();
	
		MPPProductBOMLine[] mpsArray = new MPPProductBOMLine[mps.size()];
		return mps.toArray(mpsArray);
	}
	
	/**
	 * 
	 * @return
	 */
	public MProduct[] getNonSelectableProducts() {
		ArrayList<MProduct> mps = new ArrayList<MProduct>();
		String trxName  = get_TrxName();
		
		
		MBLDProductPartType mBLDProductPartType = new MBLDProductPartType(Env.getCtx(), getBLD_Product_PartType_ID(), trxName);
		//int bLDProductPTID = Env.getContextAsInt(Env.getCtx(), m_WindowNoParent, "BLD_Product_PartType_ID");
		int mProductID = mBLDProductPartType.getM_Product_ID();
		int mPartTypeID = mBLDProductPartType.getM_PartTypeID();
		
		ArrayList<Integer> parentProducts = null;
		int otherMPartTypeID = mBLDProductPartType.getOtherbomMParttypeID();
		if(otherMPartTypeID > 0)//Get the other parents to get BOM lines from
		{
			parentProducts = MtmUtils.getOtherParentProductsFromBom(mProductID, otherMPartTypeID);
		}
		else//Just add the parent product.
		{
			parentProducts = new ArrayList<Integer>();
			parentProducts.add(mProductID);
		}
		
//Loop through parent products, get bomline products.
		//ArrayList<Integer> matchedProductIDs = new ArrayList<Integer>();
		for(Integer mpProductID : parentProducts)
		{
			//Get Bom
			MPPProductBOM mPPProductBOM = MPPProductBOM.getDefault(MProduct.get(mpProductID), null);
			MPPProductBOMLine[] mPPProductBOMLines = mPPProductBOM.getLines();
			
			for(int y = 0; y < mPPProductBOMLines.length; y++)
			{
				MProduct lineProduct = MProduct.get(mPPProductBOMLines[y].getM_Product_ID());
				int linePartTypeID = lineProduct.getM_PartType_ID();
				if(linePartTypeID == mPartTypeID)
				{
					mps.add(lineProduct);
				}
			}
		}
		MProduct[] mpsArray = new MProduct[mps.size()];
		return mps.toArray(mpsArray);
	
	}
	
	public boolean isWidthDropMatch(int width, int drop) {
		
		log.warning("getwidth1() & getwidth2()" + getwidth1() + " & " + getwidth2());
		if(width > getwidth1() && width < getwidth2())
		{
			log.warning("width > getwidth1() && width < getwidth2() : true");
			if(drop > getdrop1() && drop < getdrop2())
				{
					log.warning("getdrop1() & getdrop2()" + getdrop1() + " & " + getdrop2());
					log.warning("if(drop > getdrop1() && drop < getdrop2() : true");
					return true;
				}
		}
		return false;
	}
	
	/**
	 * @override
	 */
	public int getM_Product_ID() {
		int mProduct = 0;
		int bldProductNonSelectID  = get_ID();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT m_product_id ");
		sql.append("FROM bld_product_parttype bpt ");
		sql.append(" JOIN bld_product_non_select bpns ON bpns.bld_product_parttype_id ");
		sql.append("= bpt.bld_product_parttype_id ");
		sql.append("WHERE bpns.bld_product_non_select_id = ?");
		
		
		mProduct = DB.getSQLValue(get_TrxName(), sql.toString(), bldProductNonSelectID);
		
		return mProduct;
		
	}
}
