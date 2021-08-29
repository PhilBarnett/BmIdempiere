/**
 * 
 */
package au.blindmot.make;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.compiere.util.AdempiereUserError;
import org.compiere.util.Env;

import au.blindmot.utils.MtmUtils;

/**
 * @author phil
 *
 */
public class Curtain extends RollerBlind {
	
	private static final String ERROR_NO_HOOK_CLEARANCE = "Hook clearance cannot be determined. Check that the Attributes are correctly setup for the track selected";
	private static final String ERROR_NO_HEADER = "Header cannot be determined. Check that the Attributes are correctly setup for the track selected";
	private static final String ERROR_NO_CARRIER = "Carrier part not in Product options or not setup to be added to BOM. Check the Product Options (BLD Productset Instance) for this product.";
	protected int curtainTrackID = 0;
	protected boolean isSwave = false;
	private int liningID = 0;
	
	protected static final String CURTAIN_POSITION_FRONT = "Front";
	protected static final String CURTAIN_OPENING_CENTRE = "Centre, 2 curtains";
	protected static final String CURTAIN_OPENING_LEFT = "Stack Left, 1 curtain";
	protected static final String CURTAIN_OPENING_RIGHT = "Stack Right, 1 curtain";
	protected static final String CURTAIN_OPENING_1_FREE = "1 curtain free flowing";
	protected static final String CURTAIN_OPENING_2_FREE = "2 curtains free flowing";
	
	protected static final String ATTRIBUTE_CURTAIN_OPENING = "Curtain opening";
	protected static final String ATTRIBUTE_CURTAIN_HEADING = "Heading";//Heading to be either 'Swave' or 'Standard'.
	protected static final String ATTRIBUTE_CARRIER_PITCH = "Carrier pitch";
	
	private static final String PART_TYPE_CURTAIN_CARRIER = "Curtain carrier";
	private static final String PART_TYPE_CURTAIN_TRACK = "Curtain track";
	private static final String PART_TYPE_CURTAIN_LINING = "Lining";
	//private static String PART_TYPE_
	//private static String PART_TYPE_

	public Curtain(int mProduct_ID, int mtom_item_line_id, String trxnName) {
		super(mProduct_ID, mtom_item_line_id, trxnName);
		setIsSwave();
	}
	
	private void setIsSwave() {
		int carrierID = getBomProductID(PART_TYPE_CURTAIN_CARRIER);
		if(carrierID < 1)
		{
			throw new AdempiereUserError(ERROR_NO_CARRIER);
		}
		String header = (String) MtmUtils.getMattributeInstanceValue(carrierID, PART_TYPE_CURTAIN_CARRIER, trxName);
		if(header.contains(MtmUtils.MTM_CURTAIN_CARRIER_SWAVE) || header.contains(MtmUtils.MTM_CURTAIN_CARRIER_SFOLD))
		{
			isSwave = true;
		}
		else
		{
			isSwave = false;
		}
		
	}

	@Override
	/**
	 * COPIED FROM ROLLER BLIND - MODIFY TO SUIT
	 * 
	 */
	public boolean getCuts() {
		log.warning("---------In getCuts()");
		/*
		 * /TODO: get the fabricID, rollerTubeID (curtain track), bottomBarID from BOM lines in case they've been edited by user.
		 * TODO: Modify to iterate through BOM derived and cut as appropriate:
		 * getBomDerived
		 * iterate -> get cut based on parttype
		 */
			/**********************Code from HERE*/
		int trackID = getBomProductID(PART_TYPE_CURTAIN_TRACK);
		log.warning("--------Roller tube ID found: " + trackID);
		int trackIDToUse = 0;
		if(trackID > 0)
		{
			trackIDToUse = trackID;
		}
		else
		{
			trackIDToUse = curtainTrackID;
		}
		
		
		
		if(curtainTrackID !=0 )
			{
				addBldMtomCuts(curtainTrackID,Env.ZERO,getRollerTubeCut(wide),0);
				log.warning("--------addBldMtomCuts Adding roller tube to cuts: " + trackIDToUse);
			}
		
		/**********************Code TO HERE can possibly be deleted.*/
	    setUserSelectedPartIds();
	    setChainControl(controlID);//
		//populatePartTypes(m_product_id);//Gets the ArrayLists of partsget CONSIDER DELETING - TEST TO SEE IF IT GETS CALLED.
		setupTubeFabric();//Is this necessary?
		
		/*Curtain fabric Logic:
		 *if it's Swave: 
		 *-Get the number of carriers, set BOM qty
		 *-Get the fabric & lining qty from tape wave depth; tape wave depth * carrier qty = tape length, curtain width is tape length plus an allowance 
		 *If it's standard carriers
		 *-Get fabric qty based on 
		 */
		
		//Get a value for field fabricWidth & fabricDrop
		BigDecimal fabricWidth = getFabricWidth();
		BigDecimal fabricDrop = getFabricDrop();
		
		
		if(fabricID !=0 )

			{
				addBldMtomCuts(fabricID, fabricWidth, fabricDrop, 0);	
			}
	
		
		return true;
	
		/*
		 *Get head rail deductions - head rail is the top part of roller blind from bracket to bracket.
		 *2. tube cut length = width -  sum(rail head deductions)
		 *3. fabric cut width =  tube - fabric deduction
		 *4. fabric cut length = length + Fabric length addition - which is a non instance attribute of the
		 *the actual finished product, eg 'roller blind'.
		 *5. bottom bar cut length = fabric cut width - bottom bar deduction.
		 *6. As each cut is determined, the qty in the BOM derived items for fabric, tube and bottom bar 
		 *will need to be set.
		 */
	}
	
	/**
	 * 
	 */
	public void addFabricCuts() {
		
		if(isContinuous())
		{
			if(isSwave)
			{
				//Swave? get the fabric based on tape spacing
			}
			else
			{
				/*
				 * Not Swave? Use target fullness to get drops needed
				 * For cuts, width = roll width, for 1/2 drops 1/2 width.
				 * Waste?
				 */
			}
			
			
		}
		else//it's in drops
		{
			
		}
		
	}
	
	private boolean isContinuous() {
		BigDecimal rollWidth = (BigDecimal) MtmUtils.getMattributeInstanceValue(fabricID, MtmUtils.ROLL_WIDTH, trxName);
		BigDecimal dropAdd = (BigDecimal) MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_FABRIC_ADDITION, trxName);
		BigDecimal measuredDrop = new BigDecimal(high);
		if(rollWidth.compareTo(measuredDrop.add(dropAdd)) == 1) return true;//It's continuous
		return false;
	}

	/**
	 * Gets the BOM qty for production items that have made to measure special amounts based on size
	 * Note: There's 2 columns in the m_product_bom_id table
	 * that both look like m_product_bom_id
	 * @param mProductBomid
	 * @return
	 */
	@Override
	public BigDecimal getBomQty(int mProductBomid) { 
		//Curtain BOM qtys needed: track, curtain fabric, runners, flick sticks?, stop carriers?, 
		
		BigDecimal qty = BigDecimal.ZERO;
		if(mProductBomid == curtainTrackID)
			{
				qty = getRollerTubeQty(curtainTrackID);//check to see if this works - if it does delete this comment.
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
				
			} else if(mProductBomid == fabricID)
			{
				qty = getCurtainFabricQty(fabricID);//create method
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			} 
			
			else if(mProductBomid == liningID)
			{
				qty = getCurtainFabricQty(liningID);
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			}
			else if(mProductBomid == getBomProductID(PART_TYPE_CURTAIN_CARRIER))
			{
				Double carrierPitch = (Double) MtmUtils.getMattributeInstanceValue(mProductBomid, ATTRIBUTE_CARRIER_PITCH, trxName);
				qty = BigDecimal.valueOf(MtmUtils.getRunnerCount(wide, carrierPitch));
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			}
		return super.getBomQty(mProductBomid);
	}//getBomQty
	
	
	
	private BigDecimal getCurtainFabricQty(int fabricID) {
		BigDecimal measuredDrop = new BigDecimal(high);
		BigDecimal makeDrop = getMakeDrop(measuredDrop);
		if(isContinuous()) 
		{
			BigDecimal fullness = (BigDecimal) MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_FULLNESS_TARGET, trxName);
			return fullness.multiply(new BigDecimal(wide));
		}
		else
		{
			//getDropsPerCurtain(int fabricID, int curtainID, int numOfCurtains, int headingWidth, String trxName)
			int numOfCurtains = getNumberOfCurtains();
			Double headingWidth = MtmUtils.getHeadingWidth(wide);
			BigDecimal dropsPerCurtain = MtmUtils.getDropsPerCurtain(fabricID, m_product_id, numOfCurtains, headingWidth.intValue(), trxName);
			return dropsPerCurtain.multiply(makeDrop);
		}
	}

	private int getNumberOfCurtains() {
		String key = (String) MtmUtils.getMattributeInstanceValue(m_product_id, ATTRIBUTE_CURTAIN_OPENING, trxName);
		int numberOfCurtains = 0;
		switch (key) {
		case CURTAIN_OPENING_1_FREE:
			numberOfCurtains = 1;
			break;
		case CURTAIN_OPENING_2_FREE:
			numberOfCurtains = 2;
			break;
		case CURTAIN_OPENING_CENTRE:
			numberOfCurtains = 2;
			break;
		case CURTAIN_OPENING_LEFT:
			numberOfCurtains = 2;
			break;
		case CURTAIN_OPENING_RIGHT:
			numberOfCurtains = 1;
			break;

		default: numberOfCurtains = 0;
			break;
		}
		return numberOfCurtains;
	}

	private BigDecimal getMakeDrop(BigDecimal measuredDrop) {
		BigDecimal floorClearance = (BigDecimal) MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_FLOOR_CLEARANCE, trxName);
		String position = (String) MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_CURTAIN_POSITION, trxName);
		String fit = (String) MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_CURTAIN_POSITION, trxName);
		BigDecimal hookClearance = null;
		BigDecimal header = null;
		BigDecimal makeDrop = Env.ZERO;
		if(isSwave)
		{
			header = Env.ZERO;
		}
		else if(position.equalsIgnoreCase(CURTAIN_POSITION_FRONT) && fit.contains("Face"))//It's face fit on front track
		{
			if(!isSwave)
			{
					hookClearance = (BigDecimal) MtmUtils.getMattributeInstanceValue(curtainTrackID, MtmUtils.MTM_HOOK_CLEARANCE_FF, trxName);
					header = (BigDecimal) MtmUtils.getMattributeInstanceValue(curtainTrackID, MtmUtils.MTM_HEADER_FF, trxName);
			}
			else//it's FF Swave
			{
				hookClearance = (BigDecimal) MtmUtils.getMattributeInstanceValue(curtainTrackID, MtmUtils.MTM_HOOK_CLEARANCE_FF_SW, trxName);
			}
			
		
		}
		else//It's top fix or track other than front
		{
			if(!isSwave)
			{
				hookClearance = (BigDecimal) MtmUtils.getMattributeInstanceValue(curtainTrackID, MtmUtils.MTM_HOOK_CLEARANCE_TF, trxName);
				header = (BigDecimal) MtmUtils.getMattributeInstanceValue(curtainTrackID, MtmUtils.MTM_HEADER_TF, trxName);
			}
			else//It's TF Swave
			{
				hookClearance = (BigDecimal) MtmUtils.getMattributeInstanceValue(curtainTrackID, MtmUtils.MTM_HOOK_CLEARANCE_TF_SW, trxName);
			}
			
		}
		if(hookClearance == null)throw new AdempiereUserError(ERROR_NO_HOOK_CLEARANCE);
		if(header == null)throw new AdempiereUserError(ERROR_NO_HEADER);
		makeDrop = measuredDrop.subtract(floorClearance).subtract(hookClearance).add(header);
		
		return makeDrop;
	}

	/**
	 * @Override 
	 * @see au.blindmot.make.MtmInfo#getConfig()
	 */	
	public List<String> getConfig() {
		ArrayList <String> config = new ArrayList<String>();
		config.add("The main product is Curtain. Any other products mentioned that aren't 'Curtain' below are to be on the BOM of the Curtain");
		config.add("Instance Attribute: Width; Number; for product Curtain");
		config.add("Instance Attribute: Drop; Number; for product Curtain");
		config.add("Instance Attribute: Heading; for product Carrier");
		config.add("Instance Attribute: Floor Clearance; Number; for product Curtain");
		config.add("Instance Attribute: Curtain position; for product Curtain");
		config.add("Instance Attribute: Hem; for product Curtain");
		config.add("Instance Attribute: Fit; for product Curtain");
		config.add("Instance Attribute: Product combination; for product Curtain. Options 'Curtain and track', 'Track only', 'Curtain only'");
		config.add("Instance Attribute: Curtain opening; for product Curtain");
		config.add("Instance Attribute: Fabric length addition; for product Curtain");
		config.add("Instance Attribute: Bend; for product Curtain; No, Angle, Continuous");
		config.add("Instance Attribute: Operated By; for Product track(Parttype: Curtain track");
		config.add("Attribute: Carrier pitch; for Product Carrier (part Type Curtain carrier), the spacing between carriers in mm");
		config.add("Attribute: Carrier type; for Product Carrier (part Type Curtain carrier), the carrier type (Swave or Standard)");
		config.add("Attribute: Fullness low; Number; for Product Curtain");
		config.add("Attribute: Fullness target; Number; for Product Curtain");
		config.add("Attribute: Fullness high; Number; for Product Curtain");
		config.add("Attribute: Hook clearance top fix; Number; for Product track");
		config.add("Attribute: Hook clearance face fix; Number; for Product track");
		config.add("Attribute: Header top fix; Number; for Product track");
		config.add("Attribute: Hook clearance face fix Swave; Number; for Product track");
		config.add("Attribute: Header face fix; Number; for Product track");
		config.add("Attribute: Hook clearance top fix Swave; Number; for Product track");
		config.add("Attribute: Roll width; Number; for Product fabric");
		
		return config;
	
	}
	
}
