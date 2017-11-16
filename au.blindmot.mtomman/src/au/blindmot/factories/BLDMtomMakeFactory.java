/**
 * 
 */
package au.blindmot.factories;

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
	 * -Extend WindowFurnishing and implement the appropriate interface.
	 * 
	 */
	  public static MadeToMeasureProduct getMtmProduct(int m_Product_ID)
	  {
		 String sql = new String("SELECT classification from m_product WHERE m_product_id = ?");
		 String classification = DB.getSQLValueString(null, sql, m_Product_ID);
		 
	    if ( classification.equals("roller") )
	      return new RollerBlind();
	    else if ( classification.equals("awning") )
	      return new AwningBlind();
	    else if ( classification.equals("panel") )
	      return new PanelGlide();

	    return null;
	  }
	}
	

