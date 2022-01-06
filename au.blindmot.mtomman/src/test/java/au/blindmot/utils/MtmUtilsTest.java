/**
 * 
 */
package au.blindmot.utils;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.compiere.util.Env;
import org.junit.Test;

/**
 * @author phil
 *
 */
public class MtmUtilsTest {

	private long numOfCurtains = 2;
	private Double headingWidthSwave;

	/**
	 * Test method for {@link au.blindmot.utils.MtmUtils#getTotalRunnerCount(int, int, java.lang.Double)}.
	 */
	@Test
	public final void testGetTotalRunnerCountOneCurtain() {
		//getTotalRunnerCount(int trackWidth, int numOfCurtains, Double carrierPitch)
		assertEquals(36, MtmUtils.getTotalRunnerCount(2000,1,Double.valueOf(60)),0);
	}
	
	/**
	 * Test method for {@link au.blindmot.utils.MtmUtils#getTotalRunnerCount(int, int, java.lang.Double)}.
	 */
	@Test
	public final void testGetTotalRunnerCountTwoCurtains() {
		//getTotalRunnerCount(int trackWidth, int numOfCurtains, Double carrierPitch)
		assertEquals(38, MtmUtils.getTotalRunnerCount(2000,2,Double.valueOf(60)),2);
	}
	
	/**
	 * Test method for {@link au.blindmot.utils.MtmUtils#getCreepage(int)}.
	 */
	@Test
	public final void testGetCreepage() {
		assertEquals(7.09407576844871, MtmUtils.getCreepage(2000), .01);
				
				//fail("Not yet implemented"); // TODO
			}
	
	@Test
	public final void testGetCreepage2() {
		assertEquals(1.23276552141958, MtmUtils.getCreepage(7000), .01);
				
				//fail("Not yet implemented"); // TODO
			}
	@Test
	public final void testgetHeadingWidthSwave() {
		assertEquals(6000, MtmUtils.getHeadingWidthSwave(100, 60), 0);
	}
	
	@Test
	public final void testgetHeadingWidthSwave0() {
		assertEquals(0, MtmUtils.getHeadingWidthSwave(0, 0), 0);
	}
	
	@Test
	public final void testgetDropsPerCurtain() {
		headingWidthSwave = MtmUtils.getHeadingWidthSwave(100, 38);
		assertEquals(3800,  headingWidthSwave, 0);
		//Get the fullness range, start with a 1/2 drop then go in halves until the right drops is found.
		BigDecimal fullnessTarget = BigDecimal.valueOf(2.5);
		BigDecimal rollWidth = BigDecimal.valueOf(1500);
		BigDecimal headWidth = new BigDecimal(headingWidthSwave).divide(BigDecimal.valueOf(numOfCurtains ));//Heading width PER curtain.
		BigDecimal fullness = Env.ZERO;
		BigDecimal drops = Env.ZERO;
		
		while(fullness.compareTo(fullnessTarget) < 0)
		{
			drops = drops.add(new BigDecimal(0.5));
			fullness = (drops.multiply(rollWidth)).divide(headWidth,2,RoundingMode.HALF_UP);//drops * roll width / width
			//fullness = rollWidth.divide(headWidth,2,RoundingMode.HALF_UP);
		}
		
		assertEquals(5, drops.doubleValue(), 2);
	
	}
}