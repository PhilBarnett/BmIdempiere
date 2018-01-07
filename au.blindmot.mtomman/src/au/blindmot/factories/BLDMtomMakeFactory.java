/**
 * 
 */
package au.blindmot.factories;

import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;

import au.blindmot.make.AwningBlind;
import au.blindmot.make.MadeToMeasureProduct;
import au.blindmot.make.PanelGlide;
import au.blindmot.make.RollerBlind;

/**
 * @author phil
 *
 */
public class BLDMtomMakeFactory {

	/**For this to work as designed, made to measure products must be given classifications that
	 * match those below; 'roller', 'awning', 'panel' etc.
	 * As a new product is added:
	 * -Decide if it warrants creating an interface to ensure some common functionality is provided.
	 * -Extend MadeToMeasureProduct and implement the appropriate interface if one is available.
	 * 
	 */
	  public static MadeToMeasureProduct getMtmProduct(int m_Product_ID, int bld_mtom_item_line_id, String trxnName)
	  {
		 String sql = new String("SELECT classification from m_product WHERE m_product_id = ?");
		 String classification = DB.getSQLValueString(null, sql, m_Product_ID);
		 
		 if(classification == null)
		 {
			 throw new AdempiereUserError("Classisfication cannot be resolved, check the 'classification' field in the product window.");
		 }
		 int mProduct_ID = m_Product_ID;
		 int mtom_item_line_id = bld_mtom_item_line_id;
		 
	    if ( classification.equals("roller") )
	      return new RollerBlind(mProduct_ID, mtom_item_line_id, trxnName);
	    else if ( classification.equals("awning") )
	      return new AwningBlind(mProduct_ID, mtom_item_line_id, trxnName);
	    else if ( classification.equals("panel") )
	      return new PanelGlide(mProduct_ID, mtom_item_line_id, trxnName);

	    return null;
	  }
	}
	

