/**
 * 
 */
package au.blindmot.utils;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author phil
 *
 */
public class MtmUtilsTest {

	/**
	 * Test method for {@link au.blindmot.utils.MtmUtils#getTotalRunnerCount(int, int, java.lang.Double)}.
	 */
	@Test
	public final void testGetTotalRunnerCount() {
		//getTotalRunnerCount(int trackWidth, int numOfCurtains, Double carrierPitch)
		assertEquals(36, MtmUtils.getTotalRunnerCount(2000,1,Double.valueOf(60)),0);
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
	

}
