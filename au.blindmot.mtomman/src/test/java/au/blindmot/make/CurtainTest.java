/**
 * 
 */
package au.blindmot.make;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import au.blindmot.utils.MtmUtils;

public class CurtainTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	private int wide = 2000;
	private int numOfCurtains = 2;
	BigDecimal waveDepth = new BigDecimal(100);
	Double carrierPitch = Double.valueOf(60);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}


	/**
	 * Test method for {@link au.blindmot.make.Curtain#addFabricCuts()}.
	 */
	
	@Test
	public final void testTotalRunnerCount() {
		assertEquals(38, MtmUtils.getTotalRunnerCount(wide, numOfCurtains, carrierPitch), 2);
	}
	
	
	@Test
	public final void testGetCurtainFabricQty() {
		//wide = 2000;
		//numOfCurtains = 2;
		//BigDecimal waveDepth = new BigDecimal(100);
		//Double carrierPitch = Double.valueOf(60);
		
		Double totalRunnerCount = MtmUtils.getTotalRunnerCount(wide, numOfCurtains, carrierPitch);//should be 36
		int runnersPerCurtain = (int) (totalRunnerCount/numOfCurtains);//Should 
		BigDecimal headingWidthPerCurtain = BigDecimal.valueOf(MtmUtils.getHeadingWidthSwave(waveDepth.intValue(), runnersPerCurtain));
		//BigDecimal headingWidth = BigDecimal.valueOf(MtmUtils.getHeadingWidthSwave(waveDepth.intValue(), Double.valueOf(totalRunnerCount/numOfCurtains).intValue()));
		assertEquals(3800, headingWidthPerCurtain.multiply(BigDecimal.valueOf(numOfCurtains)).intValue(), 200);

	}
}
