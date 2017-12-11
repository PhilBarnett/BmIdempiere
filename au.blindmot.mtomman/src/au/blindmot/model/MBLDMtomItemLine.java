package au.blindmot.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.MOrderLine;

import au.blindmot.factories.BLDMtomMakeFactory;
import au.blindmot.make.MadeToMeasureProduct;
import au.blindmot.utils.MtmUtils;

public class MBLDMtomItemLine extends X_BLD_mtom_item_line {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3382564616366609627L;

	public MBLDMtomItemLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	/**
	 * The standard constructor call
	 */
	
	public MBLDMtomItemLine(MOrderLine orderLine, int mTM_prod_ID) {
		this(orderLine.getCtx(), 0, orderLine.get_TrxName());
		int m_Prod_ID = orderLine.getM_Product_ID();
		setDescription(orderLine.getDescription());
		setM_Product_ID(m_Prod_ID);
		setbld_mtom_production_ID(mTM_prod_ID);
		setName(orderLine.getName());
		setC_OrderLine_ID(orderLine.getC_OrderLine_ID());
		setM_AttributeSetInstance_ID(orderLine.getM_AttributeSetInstance_ID());
		if(orderLine.get_Value("mtm_attribute")!=null)
		{
			setinstance_string(orderLine.get_Value("mtm_attribute").toString());
		}
		else
		{
			setinstance_string("0_0_0");
		}
		
		
		/*
		 * Once the production lines are in, what do we do with them? 
		 * TODO: Create table bld_mtom_cuts, add the table to 'Table and Column', generate I & X classes, no M class.
		 * 
		 * 'Interpret' from AttributeSetInstance the components required, may need some sort of 
		 *  AttributeSetInstance to components/cuts table for settings - eg what components (products) are required 
		 *   for a link blind? a drop down awning with motor?
		 *   Add deductions as a product attribute?
		 *   As attributes are going to be the way to propagate info, there will have to be protocol 
		 *   that determines their use, possibly a verification process?
		 *  Develop business logic around 'how things are made' with deductions, normal roll/reverse roll etc.
		 *  	Think about how blind calculations are made to manually make a blind to get ideas.
		 *  	Consider a 'BLDMtomBLindFactory' class that returns a class that implements I_BLD_mtom_Blinds interface
		 *  	with methods like:
		 *  		getSkinSize(int width, int drop)	
		 *  		getCutLengths(int product_ID)
		 *  	Classes to returTODO Auto-generated constructor stubs example: RollerMaker, CommercialRollerMaker, AcmedaWireAwningMaker etc		
		 *  
		 *  Calculate BOM_derived quantities for invent adjustment, look at 'productionline' for code guidelines.
		 *  
		 */
		
	}

	/**
	 * 
	 */
	public MBLDMtomItemLine(Properties ctx, int BLD_mtom_item_line_ID, String trxName) {
		super(ctx, BLD_mtom_item_line_ID, trxName);
		// TODO Auto-generated constructor stub
	
	}
	
	public void set_Barcode(){
		log.warning("---------------In MBLDMtomItemLine.set_Barcode()");
		setbarcode(MtmUtils.getBarcode(get_Table_ID(), getbld_mtom_item_line_ID()));
	}
	
	public boolean deleteProcessedMtmLineItem() {
		return false;
		/*TODO: implement a reversal of processMtmLineItem()
		 * May need to add abstract methods to MadeToMeasureProduct
		 * deleteBomDerived(), delteCuts(), deleteProductionLine()
		 */
		
	}
	
	public boolean processMtmLineItem() {
		MadeToMeasureProduct mTmProduct = BLDMtomMakeFactory.getMtmProduct(getM_Product_ID(), getbld_mtom_item_line_ID () );
		
		if(this.isComplete())//If it has already been processed, delete children and start again.
		{
			mTmProduct.deleteBomDerived();
			mTmProduct.deleteCuts(); 
			mTmProduct.deleteProductionLine();/*
												/TODO Check that the inventory move is reversed when
												 * the ProductionLines are deleted.
												 */
		}
		/*
		 * TODO: interpret MAinstance, set fields in MadeToMeasureProduct object,
		 * 
		 */
		log.warning("---------------In processMtmLineItem");
		
		if(mTmProduct.createBomDerived())
			/*
			*TODO: Ensure that no orphan records get created
			*Delete/rollback any BOM derived, ProductionLine and Cuts records 
			*with this.getbld_mtom_item_line_ID()
			*deleteCuts(), deleteProductionLine(), deleteBomDerived() is in
			*MadeToMeasureProduct so that if the create/get/create methods fail then 
			*the children can be easily deleted.
			**/
			{
				if(mTmProduct.getCuts())
				{
					if(mTmProduct.createProductionLine())
					{
						return true;
					}
				}
			}
		
		
		return false;
		
	}

}
