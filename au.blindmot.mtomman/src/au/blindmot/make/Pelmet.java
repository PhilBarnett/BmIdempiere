package au.blindmot.make;

import java.math.BigDecimal;
import java.util.List;

import org.compiere.model.MProduct;
import org.compiere.model.MProductBOM;
import org.compiere.model.X_M_PartType;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.model.MBLDBomDerived;
import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDProductNonSelect;
import au.blindmot.model.MBLDProductPartType;

public class Pelmet extends MadeToMeasureProduct {
	
	private int productToCut = 0;
	private int pelmetAngleID = 0;
	private static String CUT_TO_LENGTH = "Cut to length item";
	private static String PELMET_RETURN_LEFT = "Pelmet return L";
	private static String PELMET_RETURN_RIGHT = "Pelmet return R";
	private static String PELMET_BRACKET = "Pelmet bracket";
	
	
	
	public Pelmet(int product_id, int bld_mtom_item_line_id, String trxn) {
		super(product_id, bld_mtom_item_line_id, trxn);
		interpretMattributeSetInstance();
		//setPelmetSize();
	}

	@Override
	/**
	 * Must be first method called in object.
	 * Should be called init()?
	 * 
	 */
	public boolean getCuts() {
		setUserSelectedPartIds();
		if(pelmetAngleID != 0)
		{
			addBldMtomCuts(pelmetAngleID, 0, wide, 0);
			
		}
		//Pelmet has only one bom line plus returns?
		return true;
	}

	@Override
	public boolean createBomDerived() {
		
		setUserSelectedPartIds();
		addMtmInstancePartsToBomDerived();
		
		BigDecimal pelwidth = new BigDecimal(wide);
		System.out.println("wide: " + wide + " deep: " + deep + " high: " + high );
		BigDecimal waste = new BigDecimal(getWaste(productToCut));
		if(waste.compareTo(Env.ZERO) < 0)
		{
			log.severe("----------No waste for m_product_id: " + productToCut + " this will cause unexpected inventory movements");
		}
		BigDecimal wasteMult = waste.divide(Env.ONEHUNDRED).add(Env.ONE);
		BigDecimal qty = (pelwidth.multiply(wasteMult));
		qty.setScale(4, BigDecimal.ROUND_CEILING);
		
		addMBLDBomDerived(productToCut, qty , "Procesed with waste factor of: " + waste + "%");
		return true;
	}

	@Override
	public boolean deleteBomDerived() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCuts() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override//Necessary?
	public void interpretMattributeSetInstance() {
			AttributePair[] attributePair = getMAttributeSetInstance();
			String mInstance;
			//Set fields based on the AttributePair[] contents.
			for(int i = 0; i < attributePair.length; i++)
			{
				mInstance = attributePair[i].getInstance();
				if(mInstance.equalsIgnoreCase("Pelmet brackets"))
				{
				}
				else if(mInstance.equalsIgnoreCase("Return left"))
				{
				}
				else if(mInstance.equalsIgnoreCase("Return right"))
				{
				}
				else if(mInstance.equalsIgnoreCase("Pelmet size"))
				{
				}
				else if(mInstance.equalsIgnoreCase("Colour"))
				{
				}
				
			}
		
		}

	@Override
	public List<String> getConfig() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @return
	 */
	private BigDecimal getPelmetCut() {
		BigDecimal pelmetCut = new BigDecimal(getWide());
		if(pelmetCut.compareTo(Env.ZERO) > 0 ) return pelmetCut;
		return Env.ZERO;
	}//getPelmetCut

	/**
	 * 	
	 */
	private void setUserSelectedPartIds(){
			
			MBLDLineProductInstance[] mBLDLineProductInstance = getMBLDLineProductInstance(); 
			for(int i = 0; i < mBLDLineProductInstance.length; i++)
			{
					int mProductId = mBLDLineProductInstance[i].getM_Product_ID();
					int mPartTypeID = mBLDLineProductInstance[i].getBLD_Product_PartType_ID();
					MBLDProductPartType mBLDProductPartType  = new MBLDProductPartType(Env.getCtx(), mPartTypeID, null);
					int xMPartTypeID = mBLDProductPartType.getM_PartTypeID();
					X_M_PartType mPartType  = new X_M_PartType (Env.getCtx(), xMPartTypeID , null);
					if(mPartType.getName().equals(CUT_TO_LENGTH))
					{
						pelmetAngleID = mProductId;
					}
					if(mPartType.getName().equals(PELMET_RETURN_LEFT))
					{
					}
					if(mPartType.getName().equals(PELMET_RETURN_RIGHT))
					{
					}
					if(mPartType.getName().equals(PELMET_BRACKET))
					{
					}
				
			}
	    }//setUserSelectedPartIds
		
	public boolean addMtmInstancePartsToBomDerived() {
		MBLDLineProductInstance[] mBLDLineProductInstance = getMBLDLineProductInstance();
															
		for(int i = 0; i < mBLDLineProductInstance.length; i++)
		{
			int mProductId = mBLDLineProductInstance[i].getM_Product_ID();
			BigDecimal qty = getBomQty(mProductId);
			
			if(mProductId == pelmetAngleID)//Add with waste
			{
				BigDecimal pelwidth = new BigDecimal(wide);
				System.out.println("wide: " + wide + " deep: " + deep + " high: " + high );
				BigDecimal waste = new BigDecimal(getWaste(m_product_id));
				if(waste.compareTo(Env.ZERO) < 0)
				{
					log.severe("----------No waste for m_product_id: " + productToCut + " this will cause unexpected inventory movements");
				}
				BigDecimal wasteMult = waste.divide(Env.ONEHUNDRED).add(Env.ONE);
				BigDecimal qty1 = (pelwidth.multiply(wasteMult));
				qty1.setScale(4, BigDecimal.ROUND_CEILING);
				
				addMBLDBomDerived(mProductId, qty1 , "Procesed with waste factor of: " + waste + "%");
			} else 
			{
				addMBLDBomDerived(mProductId, qty, trxName);
			}
		}
		return true;
	}//addMtmInstancePartsToBomDerived		

	/**
	 * Note: There's 2 columns in the m_product_bom_id table
	 * that both look like m_product_bom_id
	 * @param mProductBomid
	 * @return
	 */
	private BigDecimal getBomQty(int mProductBomid) { 
		BigDecimal qty = BigDecimal.ZERO;
		if(mProductBomid == pelmetAngleID)
			{
				qty = new BigDecimal(wide);
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			}
		
		
		StringBuilder sql = new StringBuilder("SELECT m_product_bom_id ");
	        sql.append("FROM m_product_bom ");
	        sql.append("WHERE m_product_id = ");
	        sql.append(m_product_id);
	        sql.append(" AND m_productbom_id = ");
	        sql.append(mProductBomid);
	        
	        int m_product_bom_id = DB.getSQLValue(trxName, sql.toString());
			MProductBOM mProductBom = new MProductBOM(Env.getCtx(), m_product_bom_id, trxName);
			BigDecimal bigQty = mProductBom.getBOMQty();
		
		return bigQty;
		
	}//getBomQty	
	
	@Override
	public boolean performOperationAddition(MBLDProductNonSelect mBLDPNonSelect, MBLDProductPartType mBLDProductPartType) {
		//perform addition
		 int addID = Integer.parseInt(mBLDPNonSelect.getaddtionalproduct().toString());
		 X_M_PartType addPartType = new X_M_PartType(Env.getCtx(), mBLDProductPartType.getM_PartTypeID(), null);
		 
		 if(addPartType != null)
		 {
			if(addPartType.getName().equalsIgnoreCase("Cut to length item"))
			 {
				 addMBLDBomDerived(addID, getPelmetCut(), trxName);
			 }
			 else
			 {
				 addMBLDBomDerived(addID, getBomQty(addID), trxName);
			 }
			 	
		 }
		
		return true;
	}

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#addTriggeredBom(int)
	 */
	@Override
	public boolean addTriggeredBom(int parentBomID) {
		// TODO Auto-generated method stub
		return false;
	}
	
}//Pelmet
