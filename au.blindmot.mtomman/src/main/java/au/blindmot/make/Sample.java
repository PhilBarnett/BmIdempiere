/**
 * 
 */
package au.blindmot.make;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import au.blindmot.model.MBLDMtomCuts;
import au.blindmot.utils.MtmUtils;

/**
 * @author phil
 *
 */
public class Sample extends RollerBlind {
	
	private static String DEFAULT_WIDTH ="Default Width";
	private static String DEFAULT_DROP ="Default Drop";

	public Sample(int product_id, int bld_mtom_item_line_id, String trxn) {
		super(product_id, bld_mtom_item_line_id, trxn);
		// TODO Auto-generated constructor stub
	}
	@Override
	public BigDecimal getFabricDrop() {
		MtmUtils.attributePreCheck(DEFAULT_DROP);
		Object drop = MtmUtils.getMattributeInstanceValue(m_product_id, DEFAULT_DROP, null);
		return new BigDecimal ((String) drop);
	
	}
	@Override
	 public BigDecimal getFabricWidth() {
		 MtmUtils.attributePreCheck(DEFAULT_WIDTH);
		 Object width = MtmUtils.getMattributeInstanceValue(m_product_id, DEFAULT_WIDTH, null);
			return new BigDecimal((String)width);
		 
	 }
	
	@Override
	public List<String> getConfig() {
			ArrayList <String> config = new ArrayList<String>();
			config.add("Attribute: Default Width: The width to make the sample. ");
			config.add("Attribute: Default Drop: The drop to make the sample. ");
			config.add("Instance Attribute: THERE ARE NONE REQUIRED, default width and drop are used for production width and drop");
			
			return config;
		}//getConfig()

}
