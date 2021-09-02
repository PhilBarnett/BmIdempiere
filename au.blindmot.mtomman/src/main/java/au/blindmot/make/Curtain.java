/**
 * 
 */
package au.blindmot.make;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.compiere.model.X_M_PartType;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.Env;

import au.blindmot.make.Curtain.CurtainConfig;
import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDProductPartType;
import au.blindmot.utils.MtmUtils;

/**
 * @author phil
 *
 */
public class Curtain extends RollerBlind {
	
	private static final String ERROR_NO_HOOK_CLEARANCE = "Hook clearance cannot be determined. Check that the Attributes are correctly setup for the track selected";
	private static final String ERROR_NO_HEADER = "Header cannot be determined. Check that the Attributes are correctly setup for the track selected";
	private static final String ERROR_NO_CARRIER = "Carrier part not in Product options or not setup to be added to BOM. Check the Product Options (BLD Productset Instance) for this product.";
	private static final String ERROR_NO_SWAVE_DEPTH = "No Swave depth detected for curtain tape. Check the curtain tape on the BOM derived for attribute ";
	private static final String ERROR_NO_BRACKETS = "No brackets found. Ensure any brackets on the parent product's BOM have the 'Brackets per metre' attribute set to something meaningful";
	private static final String ERROR_NO_CARRIER_PITCH = "No carrier pitch determined. This is an attribute called 'Carrier pitch' in the product with partType 'Carrier type'.";
	private static final String ERROR_NO_SWAVE_TAPE = "No Swave tape found. Check product setup";
	protected int curtainTrackID = 0;
	protected int carrierID = 0;
	protected int curtainTapeID = 0;
	protected int curtainBracketID = 0;
	protected boolean isSwave = false;
	private boolean isFaceFix = false;
	private int liningID = 0;
	private String heading;
	private BigDecimal floorClearance;
	private String curtainOpening;
	private String position;
	
	protected static final String CURTAIN_POSITION_FRONT = "Front";
	protected static final String CURTAIN_OPENING_CENTRE = "Centre, 2 curtains";
	protected static final String CURTAIN_OPENING_LEFT = "Stack Left, 1 curtain";
	protected static final String CURTAIN_OPENING_RIGHT = "Stack Right, 1 curtain";
	protected static final String CURTAIN_OPENING_1_FREE = "1 curtain free flowing";
	protected static final String CURTAIN_OPENING_2_FREE = "2 curtains free flowing";
	
	protected static final String ATTRIBUTE_CURTAIN_OPENING = "Curtain opening";
	protected static final String ATTRIBUTE_CURTAIN_HEADING = "Heading";//Heading to be either 'Swave' or 'Standard'.
	protected static final String ATTRIBUTE_CARRIER_PITCH = "Carrier pitch";
	protected static final String ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH = "Swave depth";
	private static final String ATTRIBUTE_FLOOR_CLEARANCE = "Floor Clearance (mm)";
	private static final String ATTRIBUTE_IS_FACE_FIT = "Is face fit?";
	private static final String ATTRIBUTE_BRACKETS_PER_METRE = "Brackets per metre";
	
	//public static final String PART_TYPE_CURTAIN_CARRIER = "Curtain carrier";
	public static final String PART_TYPE_CURTAIN_TRACK = "Curtain track";
	public static final String PART_TYPE_CURTAIN_LINING = "Lining";
	public static final String PART_TYPE_CURTAIN_TAPE = "Curtain tape";
	public static final String PART_TYPE_CURTAIN_BRACKET = "Curtain bracket";
	
	
	//private static String PART_TYPE_Is face fit?
	//private static String PART_TYPE_
	
	

	public Curtain(int mProduct_ID, int mtom_item_line_id, String trxnName) {
		super(mProduct_ID, mtom_item_line_id, trxnName);
		interpretMattributeSetInstance();
	}
	
	private void setIsSwave() {
		
		if (heading.contains(CurtainConfig.INSTANCE_ATTRIBUTE_CURTAIN_HEADING_VALUE_SWAVE.toString())
				||heading.contains(CurtainConfig.INSTANCE_ATTRIBUTE_CURTAIN_HEADING_VALUE_SFOLD.toString()))
		{
			isSwave = true;
		}
		else
		{
			isSwave = false;
		}
		/*
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
		*/
	}
	
	/**
	 * 
	 */
	@Override
	public void interpretMattributeSetInstance() {
		AttributePair[] attributePair = getMAttributeSetInstance();
		String mInstance;
		String mInstanceValue;
		//MBLDMtomItemLine mtmrolleritem = new MBLDMtomItemLine(Env.getCtx(), mtom_item_line_id, trxName);getBomQty(int mProductBomid)
		
		//Set fields based on the AttributePair[] contents.
		for(int i = 0; i < attributePair.length; i++)
		{
			mInstance = attributePair[i].getInstance();
			mInstanceValue = attributePair[i].getInstanceValue();
	
			if(mInstance.equalsIgnoreCase(ATTRIBUTE_CURTAIN_HEADING))
			{
				heading = mInstanceValue;
				setIsSwave();
			}
			else if(mInstance.equalsIgnoreCase(ATTRIBUTE_FLOOR_CLEARANCE))
			{
				floorClearance = new BigDecimal(mInstanceValue);
			}
			else if(mInstance.equalsIgnoreCase(ATTRIBUTE_CURTAIN_OPENING))
			{
				curtainOpening = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase(MtmUtils.MTM_CURTAIN_POSITION)) 
			{
				position = mInstanceValue;
			}
			
		}
		
	}

	@Override
	/**
	 * COPIED FROM ROLLER BLIND - MODIFY TO SUIT
	 * 
	 */
	public boolean getCuts() {
		
		//setUserSelectedPartIds();
		//setIsSwave();
		
		log.warning("---------In getCuts()");
		/*
		 * /TODO: get the fabricID, rollerTubeID (curtain track), bottomBarID from BOM lines in case they've been edited by user.
		 * TODO: Modify to iterate through BOM derived and cut as appropriate:
		 * getBomDerived
		 * iterate -> get cut based on parttype
		 */
			/**********************Code from HERE*/
		int trackID = getBomDerivedProductID(PART_TYPE_CURTAIN_TRACK);
		log.warning("--------Curtain track ID found: " + trackID);
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
		
		
		if(fabricID !=0 )

			{
				addFabricCuts();
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
		BigDecimal qty = getCurtainFabricQty(fabricID);
		int numberOfCurtains = getNumberOfCurtains();
		int fabricWidth = Integer.valueOf((String) MtmUtils.getMattributeInstanceValue(fabricID, MtmUtils.ROLL_WIDTH, trxName));
		if(isContinuous())
		{	
			for(int i = 0; i < numberOfCurtains; i++)
			{
				addBldMtomCuts(fabricID, qty.intValue()/numberOfCurtains, fabricWidth, 0);	
			}
		}
		else
		{
			Double runnerCountTotal = Double.valueOf(0);
			Double headingWidthPerCurtain = Double.valueOf(0);
			BigDecimal makeDrop = getMakeDrop(BigDecimal.valueOf(high));
			BigDecimal fabricLengthAdd = (BigDecimal) MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_FABRIC_ADDITION, trxName);
			BigDecimal dropCutLength = makeDrop.add(fabricLengthAdd);
			if(isSwave)
				{
					int tapeID = getBomDerivedProductID(PART_TYPE_CURTAIN_TAPE);
					CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString();
					int carrierID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString());
					Double carrierPitch = (Double) MtmUtils.getMattributeInstanceValue(carrierID, ATTRIBUTE_CARRIER_PITCH, trxName);
					runnerCountTotal = MtmUtils.getTotalRunnerCount(wide, numberOfCurtains, carrierPitch);
					
					int swaveDepth = (int) MtmUtils.getMattributeInstanceValue(tapeID, ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH, trxName);
					if(swaveDepth < 1)throw new AdempiereUserError(ERROR_NO_SWAVE_DEPTH + ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH);
					headingWidthPerCurtain = MtmUtils.getHeadingWidthSwave(swaveDepth,(int)(runnerCountTotal/numberOfCurtains));
				}
			else
				{
					headingWidthPerCurtain = MtmUtils.getHeadingWidthStdCarriers(wide/numberOfCurtains);
				}
			
				BigDecimal dropsPerCurtain = MtmUtils.getDropsPerCurtain(fabricID, m_product_id, numberOfCurtains, headingWidthPerCurtain.intValue(), trxName);
				int dropsPerCurtainInt = dropsPerCurtain.intValue();
				BigDecimal remainder = dropsPerCurtain.remainder(new BigDecimal(1));//Handles drops per curtain such as 3.5
				BigDecimal remainderWidth = remainder.multiply(BigDecimal.valueOf(fabricWidth));
				
				for(int i = 0; i < numberOfCurtains; i++)//Loop through each curtain
				{
					
					for(int j = 0; j < dropsPerCurtainInt; j++) 
					{
						//Add whole drops
						addBldMtomCuts(fabricWidth, dropCutLength.intValue(), fabricWidth, 0);
					}
					//Add the remainder
					addBldMtomCuts(remainderWidth.intValue(), dropCutLength.intValue(), fabricWidth, 0);
				}
		}
			
	}//addFabricCuts()
		
	
	private boolean isContinuous() {
		BigDecimal rollWidth = new BigDecimal ((String)MtmUtils.getMattributeInstanceValue(fabricID, MtmUtils.ROLL_WIDTH, trxName));
		BigDecimal dropAdd = new BigDecimal ((String)MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_FABRIC_ADDITION, trxName));
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
		int carrierFoundID = 0;
		if(carrierID > 0)
		{
			carrierFoundID = carrierID;
		}
		else
		{
			carrierFoundID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString());
		}
		System.out.println("label " +CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString());
		int headingTapeID = 0;
		if(curtainTapeID > 0)
		{
			headingTapeID = curtainTapeID;
		}
		else
		{
			headingTapeID = getBomDerivedProductID(PART_TYPE_CURTAIN_TAPE);
		}
		
		
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
				qty = getCurtainFabricQty(fabricID);
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
			else if(mProductBomid == carrierFoundID)
			{
				Double carrierPitch = Double.valueOf((String) MtmUtils.getMattributeInstanceValue(mProductBomid, ATTRIBUTE_CARRIER_PITCH, trxName));
				qty = BigDecimal.valueOf(MtmUtils.getTotalRunnerCount(wide, getNumberOfCurtains(), carrierPitch));
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			}
			else if(mProductBomid == headingTapeID)
			{
				Double carrierPitch = Double.valueOf((String) MtmUtils.getMattributeInstanceValue(carrierFoundID, ATTRIBUTE_CARRIER_PITCH, trxName));
				BigDecimal totalRunnerCount = BigDecimal.valueOf(MtmUtils.getTotalRunnerCount(wide, getNumberOfCurtains(), carrierPitch));
				BigDecimal sWaveDepth = new BigDecimal ((String)MtmUtils.getMattributeInstanceValue(mProductBomid, ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH, trxName));
				qty = totalRunnerCount.multiply(sWaveDepth);
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			}
			else if(mProductBomid == curtainBracketID)
			{
				setIsFaceFix(mProductBomid);
				int bracketsPerMetre = Integer.valueOf((String) MtmUtils.getMattributeInstanceValue(mProductBomid, ATTRIBUTE_BRACKETS_PER_METRE, trxName));
				BigDecimal bigBracketsPerMetre = BigDecimal.valueOf(bracketsPerMetre);
				BigDecimal bigTrackWidth = BigDecimal.valueOf(wide);
				
				BigDecimal bracketsNeeded = bigTrackWidth.divide(new BigDecimal(1000)).multiply(bigBracketsPerMetre);
				BigDecimal bracketsRounded = bracketsNeeded.setScale(0, RoundingMode.HALF_UP);
				if(bracketsRounded.compareTo(Env.ZERO) > 0)
				{
					return bracketsRounded;
				}
				else
				{
					throw new AdempiereUserError(ERROR_NO_BRACKETS);
				} 
			}
		return qty;//Default
		//return super.getBomQty(mProductBomid);//Default
	}//getBomQty
	
	
	private void setIsFaceFix(int mProductBomid) {
		String isFaceFixed = (String) MtmUtils.getMattributeInstanceValue(mProductBomid, ATTRIBUTE_IS_FACE_FIT, trxName);
		if(isFaceFixed.equalsIgnoreCase("Yes"))
		{
			isFaceFix = true;
		}
		else
		{
			isFaceFix = false;
		}
		
	}

	/**
	 * Returns the length of fabric needed. 
	 * @param fabricID
	 * @return
	 */
	private BigDecimal getCurtainFabricQty(int fabricID) {
		BigDecimal measuredDrop = new BigDecimal(high);
		BigDecimal makeDrop = getMakeDrop(measuredDrop);
		BigDecimal targetFullness = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_FULLNESS_TARGET, trxName));
		int numOfCurtains = getNumberOfCurtains();
		int carrierFoundID = 0;
		if(carrierID > 0)
		{
			carrierFoundID = carrierID;
		}
		else
		{
			carrierFoundID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString());
		}
		//if(carrierFoundID < 1)throw new AdempiereUserError(ERROR_NO_CARRIER);
		
		
		if(carrierFoundID == 0)
		{
			log.warning("------In Curtain.getCurtainFabricQty(int fabricID) --> No carrierID, returning ZERO");
			return BigDecimal.ZERO;
		}
		
		BigDecimal fabricAddition = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_FABRIC_ADDITION, trxName));
		BigDecimal fabricCutDrop = makeDrop.add(fabricAddition);
		
		if(isSwave) 
		{
			int headingTapeID = 0;
			if(curtainTapeID > 0)
			{
				headingTapeID = curtainTapeID;
			}
			else
			{
				headingTapeID = getBomDerivedProductID(PART_TYPE_CURTAIN_TAPE);
			}
			if(headingTapeID < 1)
			{
				throw new AdempiereUserError(ERROR_NO_SWAVE_TAPE);
			}
			
			//Integer[] headingTapeProductIDs = getProductIDFromBldLineSetInstance(PART_TYPE_CURTAIN_TAPE);
			
			//if(headingTapeProductIDs.length > 2) log.warning("-------------More than 1 heading tape found in partset");
			//if(headingTapeProductIDs.length > 0) headingTapeID = headingTapeProductIDs[0].intValue();
			//if(headingTapeID == 0) throw new AdempiereUserError(ERROR_NO_SWAVE_TAPE);
			
			BigDecimal waveDepth = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(headingTapeID, ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH, trxName));
			Double carrierPitch = Double.valueOf((String) MtmUtils.getMattributeInstanceValue(carrierFoundID, ATTRIBUTE_CARRIER_PITCH, trxName));
			if(carrierPitch == null) throw new AdempiereUserError(ERROR_NO_CARRIER_PITCH);
			
			Double runnerCount = MtmUtils.getTotalRunnerCount(wide, numOfCurtains, carrierPitch);//gets runners per curtain
			BigDecimal headingWidth = BigDecimal.valueOf(MtmUtils.getHeadingWidthSwave(waveDepth.intValue(), runnerCount.intValue()));
			if(isContinuous())
			{
				//calculate based on heading tape
				return headingWidth.multiply(BigDecimal.valueOf(numOfCurtains));
			}
			else
			{
				//Get the number of drops and multiply by (measured drop + MTM_FABRIC_ADDITION) * number of curtains
				BigDecimal dropsPerCurtain = MtmUtils.getDropsPerCurtain(fabricID, m_product_id, numOfCurtains, headingWidth.intValue(), trxName);
				return dropsPerCurtain.multiply(fabricCutDrop).multiply(BigDecimal.valueOf(numOfCurtains));
			}
		}
		else
		{
			if(isContinuous())
			{
				return targetFullness.multiply(new BigDecimal(wide));
			}
			else
			{
				//=heading width std carriers * drops/curtain * num of curtains
				Double headingWidthStd = MtmUtils.getHeadingWidthStdCarriers(wide);
				BigDecimal dropsPerCurtain = MtmUtils.getDropsPerCurtain(fabricID, m_product_id, numOfCurtains, headingWidthStd.intValue(), trxName);
				return dropsPerCurtain.multiply(fabricCutDrop).multiply(BigDecimal.valueOf(numOfCurtains));
			}
		}
	}

	private int getNumberOfCurtains() {
		//String curtainOpening = (String) MtmUtils.getMattributeInstanceValue(m_product_id, ATTRIBUTE_CURTAIN_OPENING, trxName);
		int numberOfCurtains = 0;
		switch (curtainOpening) {
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

	/**
	 * Swave curtain have no header: header = Env.ZERO;
	 * Std curtains: Face fit curtains usually have a larger header, top fix or curtains on the rear tracks 
	 * have a smaller header.
	 * The make drop calculation is measuredDrop.subtract(floorClearance).subtract(hookClearance).add(header);
	 * @param measuredDrop
	 * @return
	 */
	private BigDecimal getMakeDrop(BigDecimal measuredDrop) {
		//floorClearance = (BigDecimal) MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_FLOOR_CLEARANCE, trxName);
		// /*String*/ position = (String) MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_CURTAIN_POSITION, trxName);
		//String fit = (String) MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_CURTAIN_POSITION, trxName);
		//TODO: Get the fit from a bracket attribute
		BigDecimal hookClearance = null;
		BigDecimal header = null;
		BigDecimal makeDrop = Env.ZERO;
		if(isSwave)
		{
			header = Env.ZERO;
		}
		
		Integer[] bracketProductIDs = getProductIDFromBldLineSetInstance(PART_TYPE_CURTAIN_BRACKET);
		Integer[] trackProductIDs = getProductIDFromBldLineSetInstance(PART_TYPE_CURTAIN_TRACK);

		if(bracketProductIDs.length > 0) log.warning("-------------More than 1 bracket found in partset");
		if(trackProductIDs.length > 0) log.warning("-------------More than 1 track found in partset");
		int bracketID = bracketProductIDs[0].intValue();
		int trackID = trackProductIDs[0].intValue();
		String facefitYorN = (String) MtmUtils.getMattributeInstanceValue(bracketID, ATTRIBUTE_IS_FACE_FIT, trxName);
		
		
		if(position.equalsIgnoreCase(CURTAIN_POSITION_FRONT) && facefitYorN.equalsIgnoreCase("Y"))//It's face fit on front track
		{
			if(isSwave)//it's FF Swave
			{
				hookClearance = (BigDecimal) MtmUtils.getMattributeInstanceValue(trackID, MtmUtils.MTM_HOOK_CLEARANCE_FF, trxName);
					
			}
			else//It's FF std
			{
				hookClearance = (BigDecimal) MtmUtils.getMattributeInstanceValue(trackID, MtmUtils.MTM_HOOK_CLEARANCE_FF_SW, trxName);
				header = (BigDecimal) MtmUtils.getMattributeInstanceValue(trackID, MtmUtils.MTM_HEADER_FF, trxName);
			}
			
		
		}
		else//It's top fix or track other than front
		{
			if(isSwave)//It's TF Swave
			{
				hookClearance = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(trackID, MtmUtils.MTM_HOOK_CLEARANCE_TF, trxName));
				
			}
			else//It's TF Std
			{
				hookClearance = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(trackID, MtmUtils.MTM_HOOK_CLEARANCE_TF_SW, trxName));
				header = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(trackID, MtmUtils.MTM_HEADER_TF, trxName));
			}
			
		}
		if(hookClearance == null)throw new AdempiereUserError(ERROR_NO_HOOK_CLEARANCE);
		if(header == null)throw new AdempiereUserError(ERROR_NO_HEADER);
		makeDrop = measuredDrop.subtract(floorClearance).subtract(hookClearance).add(header);
		log.warning("-------- Measured Drop: " + measuredDrop.toString() + ", Floor clearance: " + floorClearance.toString() + ", Hook clearance: " + hookClearance.toString() + ", Header: " + header.toString());
		
		return makeDrop;
	}

	/**
	 * @Override 
	 * @see au.blindmot.make.MtmInfo#getConfig()
	 */	
	public List<String> getConfig() {
		
		return CurtainConfig.getConfig();
		/*ArrayList <String> config = new ArrayList<String>();
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
		
		return config; */
	
	}

	/**
	 * Sets fields from parts that users can select from the part dialog selection.
	 * @return 
	 */
	public boolean setUserSelectedPartIds(){
		
		MBLDLineProductInstance[] mBLDLineProductInstance = getMBLDLineProductInstance(); 
		
		for(int i = 0; i < mBLDLineProductInstance.length; i++)
		{
				int mProductId = mBLDLineProductInstance[i].getM_Product_ID();
				int mPartTypeID = mBLDLineProductInstance[i].getBLD_Product_PartType_ID();
				MBLDProductPartType mBLDProductPartType  = new MBLDProductPartType(Env.getCtx(), mPartTypeID, null);
				int xMPartTypeID = mBLDProductPartType.getM_PartTypeID();
				X_M_PartType mPartType  = new X_M_PartType (Env.getCtx(), xMPartTypeID , null);
				String parTypeName = mPartType.getName();
				
				if(parTypeName.equals(PART_TYPE_CURTAIN_TRACK))
				{
					curtainTrackID = mProductId;
				}
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString()))
				{
					carrierID = mProductId;
				}
				else if(parTypeName.equals(PART_TYPE_CURTAIN_TAPE))
				{
					curtainTapeID = mProductId;
				}
				
				else if(parTypeName.equals(PART_TYPE_CURTAIN_BRACKET))
				{
					curtainBracketID = mProductId;
				}
				else if(parTypeName.equals(PART_TYPE_FABRIC))
				{
					fabricID = mProductId;
				}
				else if(parTypeName.equals(PART_TYPE_CURTAIN_LINING))
				{
					liningID = mProductId;
				}
		}
		return true;
    }//setUserSelectedPartIds
	
	
	public enum CurtainConfig {
	    PART_TYPE_CURTAIN_CARRIER("Curtain carrier"),
	    ATTRIBUTE_CURTAIN_LENGTH_ADDITION("Length addition"),
		INSTANCE_ATTRIBUTE_CURTAIN_HEADING_VALUE_SWAVE("SWave"),
		INSTANCE_ATTRIBUTE_CURTAIN_HEADING_VALUE_SFOLD("SFold");
	    // ...
	    //NE("Neon", 10, 20.180f);

	    private static final Map<String, CurtainConfig> BY_LABEL = new HashMap<>();
	    //private static final Map<Integer, Element> BY_ATOMIC_NUMBER = new HashMap<>();
	    //private static final Map<Float, Element> BY_ATOMIC_WEIGHT = new HashMap<>();
	    private static final List<String> CONFIG = new ArrayList<String>();
	    
	 /*   static {
	        for (CurtainConfig e : values()) {
	            BY_LABEL.put(e.label, e);
	            //BY_ATOMIC_NUMBER.put(e.atomicNumber, e);
	            //BY_ATOMIC_WEIGHT.put(e.atomicWeight, e);
	        }
	    }*/

	    public final String label;
	    //public final int atomicNumber;
	    //public final float atomicWeight;

	    private CurtainConfig(String label/*, int atomicNumber, float atomicWeight*/) {
	        this.label = label;
	        //this.atomicNumber = atomicNumber;
	        //this.atomicWeight = atomicWeight;
	    }
	    
	    public static List<String> getConfig() {
	    	CONFIG.clear();
	    	CurtainConfig[] curtainConfig = CurtainConfig.values();
	    	
	    	for(CurtainConfig cConfig : curtainConfig)
	    	{
	    		StringBuilder configToAdd = new StringBuilder(/*"Curtain Config: "*/);
	    		//configToAdd.append("cConfig.name(), ");
	    		configToAdd.append(cConfig.name());
	    		configToAdd.append(", Value: ");
	    		configToAdd.append(cConfig.toString());
	    		configToAdd.append("\n");
	    		CONFIG.add(configToAdd.toString());
	    	}
	    	
	    	return CONFIG;
	    }

	    public static CurtainConfig valueOfLabel(String label) {
	        return BY_LABEL.get(label);
	    }
	    
	    public String toString() { 
	        return this.label; 
	    }
	/*
	    public static Element valueOfAtomicNumber(int number) {
	        return BY_ATOMIC_NUMBER.get(number);
	    }

	    public static Element valueOfAtomicWeight(float weight) {
	        return BY_ATOMIC_WEIGHT.get(weight);
	    } */
	}
}
