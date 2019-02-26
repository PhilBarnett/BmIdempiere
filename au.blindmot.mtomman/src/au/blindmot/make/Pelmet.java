package au.blindmot.make;

import java.math.BigDecimal;
import java.util.List;

import org.compiere.model.MProduct;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.model.MBLDBomDerived;

public class Pelmet extends MadeToMeasureProduct {
	
	private String pelmetColIns = null;
	private String pelmetSize = null;
	private String pelmetReturnSize = null;
	private String pelmetBracketSize = null;
	private String pelmetReturnSizeL =null;						
	private String pelmetReturnSizeR =null;
	private boolean hasReturns = false;
	private int productToCut = 0;
	
	
	
	public Pelmet(int product_id, int bld_mtom_item_line_id, String trxn) {
		super(product_id, bld_mtom_item_line_id, trxn);
		interpretMattributeSetInstance();
		setPelmetSize();
		
	}

	@Override
	/**
	 * Must be first method called in object.
	 * Should be called init()?
	 * 
	 */
	public boolean getCuts() {
		//TODO:Uncomment below if required, otherwise delete.
		//interpretMattributeSetInstance();
		
		if(productToCut != 0)
		{
			addBldMtomCuts(productToCut, wide, 0, 0);
			
		}
		
		
		/*
		 * get width from attributes.
		 * there's only one cut.
		 * Choose product to cut from BOM based on colour & size
		 * put 100 & 125mm pelmet into same product?
		 * 
		 * 
		addBldMtomCuts(fabricID, fabricWidth, fabricDrop, 0);
		BigDecimal fwidth = new BigDecimal(fabricWidth).divide(oneThousand);
		BigDecimal fdrop = new BigDecimal(fabricDrop).divide(oneThousand);
		BigDecimal qty = fwidth.multiply(fdrop);
		BigDecimal waste = new BigDecimal(getWaste(fabricID));
		qty = ((qty.divide(oneHundred).multiply(waste).add(qty)));
		qty.setScale(4, BigDecimal.ROUND_CEILING);
		fabricQty = qty;
		
		*/
		//Pelmet has only one bom line plus returns?
		
		
		return true;
	}

	@Override
	public boolean createBomDerived() {
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

	@Override
	public void interpretMattributeSetInstance() {
			AttributePair[] attributePair = getMAttributeSetInstance();
			String mInstance;
			String mInstanceValue;
			
			//Set fields based on the AttributePair[] contents.
			for(int i = 0; i < attributePair.length; i++)
			{
				mInstance = attributePair[i].getInstance();
				mInstanceValue = attributePair[i].getInstanceValue();
				
				if(mInstance.equalsIgnoreCase("Pelmet brackets"))
				{
					pelmetBracketSize = mInstanceValue;
				}
				else if(mInstance.equalsIgnoreCase("Return left"))
				{
					pelmetReturnSizeL = mInstanceValue;
				}
				else if(mInstance.equalsIgnoreCase("Return right"))
				{
					pelmetReturnSizeR = mInstanceValue;
				}
				else if(mInstance.equalsIgnoreCase("Pelmet size"))
				{
					pelmetSize = mInstanceValue;
				}
				else if(mInstance.equalsIgnoreCase("Colour"))
				{
					pelmetColIns = mInstanceValue;
				}
				
			}
		
		}
	private int getcutItemID(String colour, String size) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT mpb.m_productbom_id ");
		sql.append("FROM m_product_bom mpb ");
		sql.append("JOIN m_product mp ON mp.m_product_id = mpb.m_productbom_id ");
		sql.append("WHERE ");
		sql.append("mpb.m_product_id = ");
		sql.append(m_product_id);
		sql.append(" AND mp.name LIKE '");
		sql.append(size + "%'");
		sql.append(" AND mp.description LIKE '");
		sql.append(colour + "%'");
	
		int itemID = DB.getSQLValue(trxName, sql.toString());
		if(itemID < 0)
		{
			MProduct parent = new MProduct(Env.getCtx(), m_product_id, null);
			String prodName = parent.getName();
			throw new AdempiereUserError("There was no product to cut in the BOM for " + prodName + " with size: " + size + " and colour: " + colour + ". Check BOM and try again.");
		}
		return itemID;
	}

	/*DELETE AFTER TESTING
	private void addMBLDBomDerived(int mProductId, BigDecimal qty, String description) {
		MBLDBomDerived mBomDerived = new MBLDBomDerived(Env.getCtx(), 0, trxName);
		mBomDerived.setbld_mtom_item_line_ID(mtom_item_line_id);
		mBomDerived.setM_Product_ID(mProductId);
		qty.setScale(2, BigDecimal.ROUND_CEILING);
		mBomDerived.setQty(qty);
		if(description != null)mBomDerived.setDescription(description);
		mBomDerived.saveEx();
	}
	*/
	
	/**
	 * set MProductID of angle to make pelmet from.
	 */
	private void setPelmetSize() {
		if(pelmetSize != null && pelmetColIns !=null)
		{
			productToCut = getcutItemID(pelmetColIns, pelmetSize);
		}
	}

	@Override
	public List<String> getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
