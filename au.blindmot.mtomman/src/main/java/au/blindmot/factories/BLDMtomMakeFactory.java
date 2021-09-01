/**
 * 
 */
package au.blindmot.factories;

import org.compiere.model.MProduct;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.make.AwningBlind;
import au.blindmot.make.Curtain;
import au.blindmot.make.FoldingArmAwning;
import au.blindmot.make.MadeToMeasureProduct;
import au.blindmot.make.PanelGlide;
import au.blindmot.make.Pelmet;
import au.blindmot.make.RollerBlind;
import au.blindmot.make.SideRetainedBlind;

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
	
	public static String ROLLER_BLIND = "Roller Blind";
	public static String AWNING_BLIND = "Awning Blind";
	public static String SIDE_RETAINED_BLIND = "Side Retained Blind";
	public static String CABLE_GUIDE_BLIND = "Cable Guide Blind";
	public static String PANEL_GLIDE = "Panel Glide";
	public static String PELMET = "Pelmet";
	public static String FOLDING_ARM_AWNING = "FA Awning";
	public static String CURTAIN = "Curtain";
	
	
	
	  public static MadeToMeasureProduct getMtmProduct(int m_Product_ID, int bld_mtom_item_line_id, String trxnName)
	  {
		 String sql = new String("SELECT classification from m_product WHERE m_product_id = ?");
		 String classification = DB.getSQLValueString(null, sql, m_Product_ID);
		 
		 StringBuilder sql1 = new StringBuilder("SELECT mpc.name FROM m_product_category mpc ");
		 sql1.append("JOIN m_product mp ON mp.m_product_category_id = mpc.m_product_category_id ");
		 sql1.append("WHERE mp.m_product_id = ?");
		 
		 String productCategory = DB.getSQLValueString(trxnName, sql1.toString(), m_Product_ID);
		 
		 if(classification == null && productCategory == null)
		 {
			
			 String name = "";
			 if(m_Product_ID > 0)
			 {
				 name = new MProduct(Env.getCtx(), m_Product_ID, null).getName();
			 }
			 throw new AdempiereUserError("Classisfication cannot be resolved, check the 'classification' field in the product window or the Product Category." + name);
		 }
		 if(classification == null) classification = "blank";//allow for blank or empty classifications
		 int mProduct_ID = m_Product_ID;
		 int mtom_item_line_id = bld_mtom_item_line_id;
		 
	    if ( classification.equals("roller") || productCategory.equals(ROLLER_BLIND))
	      return new RollerBlind(mProduct_ID, mtom_item_line_id, trxnName);
	    else if ( classification.equals("awning") || productCategory.equals(AWNING_BLIND))
	      return new AwningBlind(mProduct_ID, mtom_item_line_id, trxnName);
	    else if ( classification.equals("panel") || productCategory.equals(PANEL_GLIDE))
	      return new PanelGlide(mProduct_ID, mtom_item_line_id, trxnName);
	    else if ( classification.equals("pelmet") || productCategory.equals(PELMET))
		      return new Pelmet(mProduct_ID, mtom_item_line_id, trxnName);
		else if ( classification.equals("srb") || productCategory.equals(SIDE_RETAINED_BLIND) || productCategory.equals(CABLE_GUIDE_BLIND))
			return new SideRetainedBlind(mProduct_ID, mtom_item_line_id, trxnName);
		else if ( classification.equals("faawning") || productCategory.equals(FOLDING_ARM_AWNING))
			return new FoldingArmAwning(mProduct_ID, mtom_item_line_id, trxnName);
		else if ( classification.equals("curtain") || productCategory.equals(CURTAIN))
			return new Curtain(mProduct_ID, mtom_item_line_id, trxnName);
	    

	    return null;
	  }
	}
	

