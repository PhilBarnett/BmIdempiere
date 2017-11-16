package au.blindmot.make;

import org.compiere.model.MAttributeSetInstance;

public abstract class MadeToMeasureProduct {

	public MadeToMeasureProduct() {
		// TODO Auto-generated constructor stub
	}
	
	public MadeToMeasureProduct(int width, int height, int depth, int product_id) {
		wide = width;
		high = height;
		deep = depth;
		// TODO Auto-generated constructor stub
	}
	
	protected int wide;
	protected int high;
	protected int deep;
	
	public abstract boolean getCuts();
	public abstract boolean createBomDerived();
	public abstract boolean createProductionLine();

	public abstract void interpretMattributeSetInstance(MAttributeSetInstance mAttribute);
	/**
	 * Set object dimension fields so that other methods can use them.
	 * e.g. int theHeight = //some code;
	 * this.height = theHeight;
	 * Set other fields unique to concrete class
	 * e.g. int theLeftMech = //some code;
	 * this.leftMech = theLeftMech;//set the product_iD of the left mech
	 */ 

}
