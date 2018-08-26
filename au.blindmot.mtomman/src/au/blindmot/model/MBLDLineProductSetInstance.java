package au.blindmot.model;

import java.util.Properties;

import org.compiere.model.MAttributeSet;

public class MBLDLineProductSetInstance extends X_BLD_Line_ProductSetInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3522187502958971349L;

	public MBLDLineProductSetInstance(Properties ctx, int BLD_Line_ProductSetInstance_ID, String trxName) {
		super(ctx, BLD_Line_ProductSetInstance_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MAttributeSet getMAttributeSet()
	{
		if (m_mas == null && getM_AttributeSet_ID() != 0)
			m_mas = new MAttributeSet (getCtx(), getM_AttributeSet_ID(), get_TrxName());
		return m_mas;
	}	//	getMAttributeSet

}
