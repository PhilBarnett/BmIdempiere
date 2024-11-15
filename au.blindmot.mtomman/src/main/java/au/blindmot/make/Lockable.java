/**
 * 
 */
package au.blindmot.make;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author phil
 *
 */
public interface Lockable {

	public ArrayList<Integer> getLockBarCuts(BigDecimal bottomRailWidth, int lockBarProductID);
	
}
