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
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDMtomItemDetail;
import au.blindmot.model.MBLDProductPartType;
import au.blindmot.utils.MtmUtils;

/**
 * @author phil
 *
 */
public class Curtain extends RollerBlind {
	
	
	
	protected int curtainTrackID = 0;
	protected int carrierID = 0;
	protected int curtainTapeID = 0;
	protected int curtainBracketID = 0;
	protected boolean isSwave = false;
	private int liningID = 0;
	private String heading;
	private BigDecimal floorClearance = null;
	private String curtainOpening;
	private String position;
	private BigDecimal header;
	private String baseHem;
	private BigDecimal dropsPerCurtainField = Env.ZERO;
	private BigDecimal metresPerCurtain = Env.ZERO;
	private Double headingWidthPerCurtainField;
	public static final  String NUMBER_OF_CURTAINS = "Number of curtains";
	public static final  String HEADING_TYPE = "Heading type";
	public static final  String HEADING_SIZE = "Heading size";
	public static final  String HEADING_WIDTH = "Heading width";
	public static final  String MAKE_DROP = "Finished Drop";
	public static final String BASE_HEM = "Base Hem";
	public static final String DROPS_PER_CURTAIN = "Drops per curtain";
	private static final String METRES_PER_CURTAIN = "Metres per curtain";
	
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
	
			if(mInstance.equalsIgnoreCase(CurtainConfig.ATTRIBUTE_CURTAIN_HEADING.toString()))
			{
				heading = mInstanceValue;
				setIsSwave();
			}
			else if(mInstance.equalsIgnoreCase(CurtainConfig.ATTRIBUTE_FLOOR_CLEARANCE.toString()))
			{
				floorClearance = new BigDecimal(mInstanceValue);
			}
			else if(mInstance.equalsIgnoreCase(CurtainConfig.ATTRIBUTE_CURTAIN_OPENING.toString()))
			{
				curtainOpening = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase(CurtainConfig.ATRRIBUTE_CURTAIN_POSITION.toString())) 
			{
				position = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase(CurtainConfig.ATTRIBUTE_CURTAIN_HEM.toString()))
			{
			baseHem = mInstanceValue;
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
		
		int trackID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_TRACK.toString());
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
		if(curtainTapeID == 0) 
			{
				int tapeID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_TAPE.toString());
				if(tapeID > 0)
				{
					curtainTapeID = tapeID;
				}
				
			}
		{
			if(curtainTapeID > 0)
			{
				//BigDecimal qty = getCurtainFabricQty(mProductID);
				int numberOfCurtains = getNumberOfCurtains(curtainOpening);
				{	
					for(int i = 0; i < numberOfCurtains; i++)
					{
						addBldMtomCuts(curtainTapeID,Env.ZERO,getBomQty(curtainTapeID),Env.ZERO);	
					}
				}
			}
		}
		
		if(fabricID !=0 )

		{
			addFabricCuts(fabricID);
		}
		
		if(liningID !=0 )

		{
			addFabricCuts(liningID);
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
	public void addFabricCuts(int mProductID) {
		BigDecimal qty = getCurtainFabricQty(mProductID);
		int numberOfCurtains = getNumberOfCurtains(curtainOpening);
		int fabricWidth = Integer.valueOf((String) MtmUtils.getMattributeInstanceValue(mProductID, MtmUtils.ROLL_WIDTH, trxName));
		if(isContinuous(mProductID))
		{	
			for(int i = 0; i < numberOfCurtains; i++)
			{
				addBldMtomCuts(mProductID, qty.intValue()/numberOfCurtains, fabricWidth, 0);	
			}
		}
		else//it's not continuous 
		{
			Double runnerCountTotal = Double.valueOf(0);//Initialise with 0
			headingWidthPerCurtainField = Double.valueOf(0);//Initialise with 0
			BigDecimal makeDrop = getMakeDrop(BigDecimal.valueOf(high));
			BigDecimal fabricLengthAdd = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(m_product_id, MtmUtils.MTM_FABRIC_ADDITION, trxName));
			BigDecimal dropCutLength = makeDrop.add(fabricLengthAdd);
			//BigDecimal dropsPerCurtain = Env.ZERO;
			if(isSwave)//it's not continuous and it's Swave
				{
					int tapeID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_TAPE.toString());
					//if(tapeID == 0) tapeID = 1000449;//TODO: Remove hardcode
					int carrierID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString());
					Double carrierPitch = Double.valueOf((String)MtmUtils.getMattributeInstanceValue(carrierID, CurtainConfig.ATTRIBUTE_CARRIER_PITCH.toString(), trxName));
					runnerCountTotal = MtmUtils.getTotalRunnerCount(wide, numberOfCurtains, carrierPitch);
					
					int swaveDepth = Integer.valueOf((String) MtmUtils.getMattributeInstanceValue(tapeID, CurtainConfig.ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH.toString(), trxName));
					if(swaveDepth < 1)throw new AdempiereUserError(CurtainConfig.ERROR_NO_SWAVE_DEPTH.toString() + CurtainConfig.ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH.toString());
					headingWidthPerCurtainField = MtmUtils.getHeadingWidthSwave(swaveDepth,(int)(runnerCountTotal/numberOfCurtains));
					dropsPerCurtainField = MtmUtils.getDropsPerCurtainSWave(mProductID, headingWidthPerCurtainField.intValue(), trxName);
					
				}
			else //it's not continuous and it's not Swave
			{
				headingWidthPerCurtainField = MtmUtils.getHeadingWidthStdCarriers(wide/numberOfCurtains);
				dropsPerCurtainField = MtmUtils.getDropsPerCurtainStd(mProductID, m_product_id, headingWidthPerCurtainField.intValue(), trxName);
			}
				int dropsPerCurtainInt = dropsPerCurtainField.intValue();
				BigDecimal remainder = dropsPerCurtainField.remainder(new BigDecimal(1));//Handles drops per curtain such as 3.5
				BigDecimal remainderWidth = remainder.multiply(BigDecimal.valueOf(fabricWidth));
				
				for(int i = 0; i < numberOfCurtains; i++)//Loop through each curtain
				{
					
					for(int j = 0; j < dropsPerCurtainInt; j++) 
					{
						//Add whole drops
						addBldMtomCuts(mProductID, fabricWidth, dropCutLength.intValue(), 0);
					}
					//Add the remainder
					if(remainderWidth.compareTo(Env.ZERO) > 0) 
					{
						addBldMtomCuts(mProductID, remainderWidth.intValue(), dropCutLength.intValue(), 0);
					}
				}
			}
		
			
	}//addFabricCuts()
		
	
	private boolean isContinuous(int mProductID) {
		BigDecimal rollWidth = new BigDecimal ((String)MtmUtils.getMattributeInstanceValue(mProductID, MtmUtils.ROLL_WIDTH, trxName));
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
	 *///TODO:Move track related calls to CurtainTrack.java
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
		
		int headingTapeID = 0;
		if(curtainTapeID > 0)
		{
			headingTapeID = curtainTapeID;
		}
		else
		{
			headingTapeID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_TAPE.toString());
		}
		
		
		BigDecimal qty = BigDecimal.ZERO;
		if(mProductBomid == 0) return qty;
		if(mProductBomid == curtainTrackID)
			{
				//qty = getRollerTubeQty(curtainTrackID);//check to see if this works - if it does delete this comment.
				//if(qty != BigDecimal.ZERO)
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
				//Double carrierPitch = Double.valueOf((String) MtmUtils.getMattributeInstanceValue(mProductBomid, CurtainConfig.ATTRIBUTE_CARRIER_PITCH.toString(), trxName));
				//qty = BigDecimal.valueOf(MtmUtils.getTotalRunnerCount(wide, getNumberOfCurtains(curtainOpening), carrierPitch));
				//if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			}
			else if(mProductBomid == headingTapeID)
			{
				Double carrierPitch = Double.valueOf((String) MtmUtils.getMattributeInstanceValue(carrierFoundID, CurtainConfig.ATTRIBUTE_CARRIER_PITCH.toString(), trxName));
				BigDecimal totalRunnerCount = BigDecimal.valueOf(MtmUtils.getTotalRunnerCount(wide, getNumberOfCurtains(curtainOpening), carrierPitch));
				BigDecimal sWaveDepth = new BigDecimal ((String)MtmUtils.getMattributeInstanceValue(mProductBomid, CurtainConfig.ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH.toString(), trxName));
				qty = totalRunnerCount.multiply(sWaveDepth);
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			}
		
		
		else if(mProductBomid == curtainBracketID)
			{
				setIsFaceFix(mProductBomid);
				String  bracketsPerMetre = (String) MtmUtils.getMattributeInstanceValue(mProductBomid, CurtainConfig.ATTRIBUTE_BRACKETS_PER_METRE.toString(), trxName);
				BigDecimal bigBracketsPerMetre = new BigDecimal(bracketsPerMetre);
				BigDecimal bigTrackWidth = BigDecimal.valueOf(wide);
				
				BigDecimal bracketsRounded = qty;
				//BigDecimal bracketsNeeded = bigTrackWidth.divide(new BigDecimal(1000)).multiply(bigBracketsPerMetre);
				//bracketsRounded = bracketsNeeded.setScale(0, RoundingMode.HALF_UP);
				//if(bracketsRounded.compareTo(Env.ZERO) > 0)
				if(!(bracketsRounded.compareTo(Env.ZERO) > 0))
				{
					return bracketsRounded;
				}
				else
				{
					throw new AdempiereUserError(CurtainConfig.ERROR_NO_BRACKETS.toString());
				} 
			}
		return qty;//Default
		//return super.getBomQty(mProductBomid);//Default
	}//getBomQty
	
	
	private void setIsFaceFix(int mProductBomid) {
		String isFaceFixed = (String) MtmUtils.getMattributeInstanceValue(mProductBomid, CurtainConfig.ATTRIBUTE_IS_FACE_FIT.toString(), trxName);
		if(isFaceFixed.equalsIgnoreCase("Yes"))
		{
		}
		else
		{//Else what?
		}
		
	}

	/**
	 * Returns the length of fabric needed. 
	 * @param fabricID
	 * @return
	 */
	//TODO the tape is part of Curtain.java, but specific to the track - select from track BOM, leave on curtain BOM, delete from CurtainTrack.java track  BOM derived?
	public BigDecimal getCurtainFabricQty(int fabricID) {
		BigDecimal measuredDrop = new BigDecimal(high);
		BigDecimal makeDrop = getMakeDrop(measuredDrop);
		BigDecimal targetFullness = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(m_product_id, CurtainConfig.ATRRIBUTE_FULLNESS_TARGET.toString(), trxName));
		int numOfCurtains = getNumberOfCurtains(curtainOpening);
		int carrierFoundID = 0;
		
		//Carrier stuff necessary? Handled in curtain track?
		if(carrierID > 0)
		{
			carrierFoundID = carrierID;
		}
		else
		{
			carrierFoundID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString());
		}
		//if(carrierFoundID < 1)throw new AdempiereUserError(CurtainConfig.ERROR_NO_CARRIER.toString());
		
		
		if(carrierFoundID == 0)
		{
			log.warning("------In Curtain.getCurtainFabricQty(int fabricID) --> No carrierID.");
			//return BigDecimal.ZERO;
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
				headingTapeID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_TAPE.toString());
			}
			if(headingTapeID < 1)
			{
				return Env.ZERO;
				//throw new AdempiereUserError(CurtainConfig.ERROR_NO_SWAVE_TAPE.toString());
			}
			
			//Integer[] headingTapeProductIDs = getProductIDFromBldLineSetInstance(PART_TYPE_CURTAIN_TAPE);
			
			//if(headingTapeProductIDs.length > 2) log.warning("-------------More than 1 heading tape found in partset");
			//if(headingTapeProductIDs.length > 0) headingTapeID = headingTapeProductIDs[0].intValue();
			//if(headingTapeID == 0) throw new AdempiereUserError(ERROR_NO_SWAVE_TAPE);
			
			BigDecimal waveDepth = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(headingTapeID, CurtainConfig.ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH.toString(), trxName));
			Double carrierPitch = Double.valueOf((String) MtmUtils.getMattributeInstanceValue(carrierFoundID, CurtainConfig.ATTRIBUTE_CARRIER_PITCH.toString(), trxName));
			if(carrierPitch == null) throw new AdempiereUserError(CurtainConfig.ERROR_NO_CARRIER_PITCH.toString());
			
			Double totalRunnerCount = MtmUtils.getTotalRunnerCount(wide, numOfCurtains, carrierPitch);//gets total number of runners.
			BigDecimal headingWidthPerCurtain = BigDecimal.valueOf(MtmUtils.getHeadingWidthSwave(waveDepth.intValue(), Double.valueOf(totalRunnerCount/numOfCurtains).intValue()));
			headingWidthPerCurtainField = headingWidthPerCurtain.doubleValue();
			if(isContinuous(fabricID))
			{
				//calculate based on heading tape
				metresPerCurtain = headingWidthPerCurtain;
				return headingWidthPerCurtain.multiply(BigDecimal.valueOf(numOfCurtains));
			}
			else
			{
				//Get the number of drops and multiply by (measured drop + MTM_FABRIC_ADDITION) * number of curtains
				
				BigDecimal dropsPerCurtain = MtmUtils.getDropsPerCurtainStd(fabricID, m_product_id, headingWidthPerCurtain.intValue(), trxName);
				return dropsPerCurtain.multiply(fabricCutDrop).multiply(BigDecimal.valueOf(numOfCurtains));
			}
		}
		else
		{
			headingWidthPerCurtainField = MtmUtils.getHeadingWidthStdCarriers(wide/numOfCurtains);
			if(isContinuous(fabricID))
			{
				metresPerCurtain = targetFullness.multiply(new BigDecimal(wide)).divide(BigDecimal.valueOf(numOfCurtains));
				return targetFullness.multiply(new BigDecimal(wide));
			}
			else
			{
				//=heading width std carriers * drops/curtain * num of curtains
				Double headingWidthStd = MtmUtils.getHeadingWidthStdCarriers(wide);
				BigDecimal dropsPerCurtain = MtmUtils.getDropsPerCurtainStd(fabricID, m_product_id, headingWidthStd.intValue()/numOfCurtains, trxName);
				return dropsPerCurtain.multiply(fabricCutDrop).multiply(BigDecimal.valueOf(numOfCurtains));
			}
		}
	}

	public static int getNumberOfCurtains(String opening) {
		//String curtainOpening = (String) MtmUtils.getMattributeInstanceValue(m_product_id, ATTRIBUTE_CURTAIN_OPENING, trxName);
		int numberOfCurtains = 0;
		
		if(CurtainConfig.CURTAIN_OPENING_CENTRE.toString().equalsIgnoreCase(opening)) 
		{
			numberOfCurtains = 2;
		}
		else if(CurtainConfig.CURTAIN_OPENING_1_FREE.toString().equalsIgnoreCase(opening)) 
		{
			numberOfCurtains = 1;
		}
		else if(CurtainConfig.CURTAIN_OPENING_2_FREE.toString().equalsIgnoreCase(opening))
		{
			numberOfCurtains = 2;
		}
		else if(CurtainConfig.CURTAIN_OPENING_LEFT.toString().equalsIgnoreCase(opening))
		{
			numberOfCurtains = 1;
		}
		else if(CurtainConfig.CURTAIN_OPENING_RIGHT.toString().equalsIgnoreCase(opening))
		{
			numberOfCurtains = 1;
		}
		
		if(numberOfCurtains == 0) throw new AdempiereUserError(CurtainConfig.ERROR_NO_CURTAINS.toString());
		
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
		BigDecimal hookClearance = null;
		if(!MtmUtils.attributeExists(CurtainConfig.ATRRIBUTE_HOOK_CLEARANCE_FF.toString()))
		{
			throw new AdempiereUserError(CurtainConfig.ERROR_NO_HOOK_CLEARANCE_FF.toString());
	
		}
		if(!MtmUtils.attributeExists(CurtainConfig.ATRRIBUTE_HOOK_CLEARANCE_TF.toString()))
		{
			throw new AdempiereUserError(CurtainConfig.ERROR_NO_HOOK_CLEARANCE_TF.toString());
	
		}
		
		
		if(floorClearance == null)
		{
			throw new AdempiereUserError(CurtainConfig.ERROR_NO_FLOOR_CLEARANCE.toString());
		}
		//BigDecimal header = null;
		BigDecimal makeDrop = Env.ZERO;
		if(isSwave)
		{
			header = Env.ZERO;
		}
		
		Integer[] bracketProductIDs = getProductIDFromBldLineSetInstance(CurtainConfig.PART_TYPE_CURTAIN_BRACKET.toString());
		Integer[] trackProductIDs = getProductIDFromBldLineSetInstance(CurtainConfig.PART_TYPE_CURTAIN_TRACK.toString());

		if(bracketProductIDs.length > 0) log.warning("-------------More than 1 bracket found in partset");
		if(trackProductIDs.length > 0) log.warning("-------------More than 1 track found in partset");
		if(bracketProductIDs.length < 1) throw new AdempiereUserError(CurtainConfig.ERROR_NO_BRACKETS.toString());
		int bracketID = bracketProductIDs[0].intValue();
		if(trackProductIDs.length < 1) throw new AdempiereUserError(CurtainConfig.ERROR_NO_TRACK.toString());
		int trackID = trackProductIDs[0].intValue(); 
		String facefitYorN = (String) MtmUtils.getMattributeInstanceValue(bracketID, CurtainConfig.ATTRIBUTE_IS_FACE_FIT.toString(), trxName);
		
		
		if(position.equalsIgnoreCase(CurtainConfig.CURTAIN_POSITION_FRONT.toString()) && facefitYorN.equalsIgnoreCase("Y"))//It's face fit on front track
		{
			if(isSwave)//it's FF Swave
			{
				hookClearance = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(trackID, CurtainConfig.ATRRIBUTE_HOOK_CLEARANCE_FF_SW.toString(), trxName));
					
			}
			else//It's FF std
			{
				hookClearance = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(trackID, CurtainConfig.ATRRIBUTE_HOOK_CLEARANCE_FF.toString(), trxName));
				header = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(trackID, CurtainConfig.ATRRIBUTE_HEADER_FF.toString(), trxName));
			}
			
		
		}
		else//It's top fix or track other than front
		{
			if(isSwave)//It's TF Swave
			{
				hookClearance = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(trackID, CurtainConfig.ATRRIBUTE_HOOK_CLEARANCE_TF_SW.toString(), trxName));
				log.warning("---------Swave standard curtain detected, hook clearance:" + hookClearance.toString());
				
			}
			else//It's TF Std
			{
				hookClearance = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(trackID, CurtainConfig.ATRRIBUTE_HOOK_CLEARANCE_TF.toString(), trxName));
				header = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(trackID, CurtainConfig.ATRRIBUTE_HEADER_TF.toString(), trxName));
				log.warning("---------Top fix standard curtain detected, hook clearance:" + hookClearance.toString() + " Header: " + header);
			}
			
		}
		//if(hookClearance == null)throw new AdempiereUserError(CurtainConfig.ERROR_NO_HOOK_CLEARANCE.toString());
		if(header == null)throw new AdempiereUserError(CurtainConfig.ERROR_NO_HEADER.toString());
		makeDrop = measuredDrop.subtract(floorClearance).subtract(hookClearance).add(header);
		log.warning("-------- Measured Drop: " + measuredDrop.toString() + ", Floor clearance: " + floorClearance.toString() + ", Hook clearance: " + hookClearance.toString() + ", Header: " + header.toString());
		
		double roundTo = 0.5;
		double doubleMakeDrop = roundTo * Math.round(((makeDrop.doubleValue()/*/10*/)/roundTo)) ;
		return new BigDecimal(doubleMakeDrop);//returns make drop in mm rounded to 0.5
	}

	/**
	 * @Override 
	 * @see au.blindmot.make.MtmInfo#getConfig()
	 */	
	public List<String> getConfig() {
		
		return CurtainConfig.getConfig();
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
				
				if(parTypeName.equals(CurtainConfig.PART_TYPE_CURTAIN_TRACK.toString()))
				{
					curtainTrackID = mProductId;
				}
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString()))
				{
					carrierID = mProductId;
				}
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_CURTAIN_TAPE.toString()))
				{
					curtainTapeID = mProductId;
				}
				
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_CURTAIN_BRACKET.toString()))
				{
					curtainBracketID = mProductId;
				}
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_FABRIC.toString()))
				{
					fabricID = mProductId;
				}
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_CURTAIN_LINING.toString()))
				{
					liningID = mProductId;
				}
		}
		return true;
    }//setUserSelectedPartIds
	
	
	public enum CurtainConfig {
	   
	
		ERROR_NO_HOOK_CLEARANCE_FF ("Face fit hook clearance cannot be determined. Check that the Attributes are correctly setup for the track selected"),
		ERROR_NO_HOOK_CLEARANCE_TF ("Top fit hook clearance cannot be determined. Check that the Attributes are correctly setup for the track selected"),
		ERROR_NO_HOOK_CLEARANCE ("Hook clearance cannot be determined. Check that the Attributes are correctly setup for the track selected"),
		ERROR_NO_HEADER("Header cannot be determined. Check that the Attributes are correctly setup for the track selected"),
		ERROR_NO_CARRIER("Carrier part not in Product options or not setup to be added to BOM. Check the Product Options (BLD Productset Instance) for this product."),
		ERROR_NO_SWAVE_DEPTH("No Swave depth detected for curtain tape. Check the curtain tape on the BOM derived for attribute "),
		ERROR_NO_BRACKETS("No curtain brackets found. Ensure any brackets on the parent product's BOM have the 'Brackets per metre' attribute set to something meaningful"),
		ERROR_NO_CARRIER_PITCH("No carrier pitch determined. This is an attribute called 'Carrier pitch' in the product with partType 'Carrier type'."),
		ERROR_NO_SWAVE_TAPE("No Swave tape found. Check product setup"),
		ERROR_NO_TRACK("No track found. Check product setup - tracks must present on the product BOM and availabale as 'User Select"),
		ERROR_NO_CURTAINS("No curtains found. Did someone change the 'Curtain opening' attributes? Could also be a programming bug."),
		ERROR_NO_FLOOR_CLEARANCE("No floor clearance found. Check the attribute setup for the parent product and make sure there is an instance attribute: Floor Clearance (mm)"),
		CURTAIN_POSITION_FRONT("Front"),
		CURTAIN_OPENING_CENTRE("Centre, 2 curtains"),
		CURTAIN_OPENING_LEFT("Stack Left, 1 curtain"),
		CURTAIN_OPENING_RIGHT("Stack Right, 1 curtain"),
		CURTAIN_OPENING_1_FREE("1 curtain freeflowing"),
		CURTAIN_OPENING_2_FREE("2 curtains free flowing"),
		
		ATRRIBUTE_HOOK_CLEARANCE_FF ("Hook clearance face fix"),
		ATRRIBUTE_HOOK_CLEARANCE_FF_SW ("Hook clearance face fix Swave"),
		ATRRIBUTE_HOOK_CLEARANCE_TF_SW ("Hook clearance top fix Swave"),
		ATRRIBUTE_HOOK_CLEARANCE_TF ("Hook clearance top fix"),
		ATRRIBUTE_FLOOR_CLEARANCE ("Floor Clearance (mm)"),
		ATRRIBUTE_FULLNESS_LOW ("Fullness low"),
		ATRRIBUTE_FULLNESS_TARGET ("Fullness target"),
		ATRRIBUTE_FULLNESS_HIGH ("Fullness high"),
		ATRRIBUTE_CURTAIN_POSITION ("Curtain position"),
		ATRRIBUTE_CURTAIN_HEADING ("Heading type"),
		ATRRIBUTE_CURTAIN_Fit ("Fit"),
		ATRRIBUTE_CURTAIN_CARRIER_SWAVE ("Swave"),
		ATRRIBUTE_CURTAIN_CARRIER_SFOLD ("Sfold"),
		ATRRIBUTE_CURTAIN_CARRIER_STANDARD ("Standard"),
		ATTRIBUTE_ROLL_WIDTH ("Roll width"),
		ATRRIBUTE_HEADER_FF ("Header face fix"),
		ATRRIBUTE_HEADER_TF ("Header top fix"),

		ATTRIBUTE_SET_CURTAINS("Name: Curtains, for parent curtain product."),
		ATTRIBUTE_SET_CURTAINS_ATTRIBUTES("Location,Width,Drop,Heading,Floor Clearance (mm),Curtain position,Hem,Curtain opening,Bend,Fullness low,Fullness target,Fullness high,Fabric length addition"),
		ATTRIBUTE_SET_XX_TRACK("Name: x track, each PartType 'track' requires its own attribute set."),
		ATTRIBUTE_SET_XX_TRACK_ATTRIBUTES("Hook clearance face fix,Hook clearance top fix,Header face fix,Header top fix,Hook clearance face fix Swave,Hook clearance top fix Swave"),
		ATTRIBUTE_SET_CURTAIN_CARRIERS("Name: Curtain carriers, only one set needed in system, applies to all 'Curtain carrier' parttypes"),
		ATTRIBUTE_SET_CURTAIN_CARRIERS_ATTRIBUTES("Carrier pitch,Carrier type"),
		ATTRIBUTE_SET_CURTAIN_BRACKETS("Name: Curtain brackets, only one set needed in system, applies to all 'Curtain bracket' parttypes"),
		ATTRIBUTE_SET_CURTAIN_BRACKETS_ATTRIBUTES("Is face fit?,Is dual,Brackets per metre"),
		ATTRIBUTE_SET_BLIND_FABRIC("Name: Blind fabric, only one set needed in system, applies to all 'Lining' parttypes"),
		ATTRIBUTE_SET_CURTAIN_TAPE("Name: Curtain tape, only one set needed in system, applies to all 'Curtain tape' parttypes"),
		ATTRIBUTE_SET_CURTAIN_TAPE_ATTRIBUTES("Swave depth"),
		
		//TODO: Handle errors thrown in MtmUtils when a curtain config needed is missing
		
		INSTANCE_ATTRIBUTE_CURTAIN_HEADING_VALUE_SWAVE("SWave"),
		INSTANCE_ATTRIBUTE_CURTAIN_HEADING_VALUE_SFOLD("SFold"),
		ATTRIBUTE_CURTAIN_LENGTH_ADDITION("Length addition"),
		ATTRIBUTE_CURTAIN_OPENING("Curtain opening"),
		ATTRIBUTE_CURTAIN_HEADING("Heading"),
		ATTRIBUTE_CURTAIN_HEM("Hem"),
		ATTRIBUTE_CARRIER_PITCH("Carrier pitch"),
		ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH("Swave depth"),
		ATTRIBUTE_FLOOR_CLEARANCE("Floor Clearance (mm)"),
		ATTRIBUTE_IS_FACE_FIT("Is face fit?"),
		ATTRIBUTE_BRACKETS_PER_METRE("Brackets per metre"),

		PART_TYPE_CURTAIN_CARRIER("Curtain carrier"),
		PART_TYPE_CURTAIN_TRACK("Curtain track"),
		PART_TYPE_CURTAIN_LINING("Curtain lining"),
		PART_TYPE_CURTAIN_TAPE("Curtain tape"),
		PART_TYPE_CURTAIN_BRACKET("Curtain bracket"), 
		PART_TYPE_FABRIC("Fabric"), 
		PART_TYPE_DRIVE_BELT("Curtain drive belt");

	    private static final Map<String, CurtainConfig> BY_LABEL = new HashMap<>();
	   
	    private static final List<String> CONFIG = new ArrayList<String>();
	    
	 /*   static {
	        for (CurtainConfig e : values()) {
	            BY_LABEL.put(e.label, e);
	        }
	    }*/

	    public final String label;
	    private static String value;
	    
	   // CurtainConfig(String value){
	   // 	  this.value = value;
	   // 	 }

	   private CurtainConfig(String label) {
	       this.label = label;
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

	   // public static CurtainConfig valueOfLabel(String label) {
	   //     return BY_LABEL.get(label);
	   // }
	    
	    public static String getValue() {
	    	return value;
	    }
	    
	   public String toString() { 
	        return this.label; 
	    }
	}
	
	public void setCarrierID(int carrierId) {
		carrierID = carrierId;
	}
	
	public void setTapeID(int tapeID) {
		curtainTapeID = tapeID;
	}
	
	public void setHigh(int setHigh) {
		high = setHigh;
	}
	
	public void setCurtainOpening (String opening) {
		curtainOpening = opening;
	}
	
	/**
	 * 
	 */
	@Override
	public boolean addMtomItemDetail() {
		
		//add number of curtains
		addMtomItemDetail(NUMBER_OF_CURTAINS, String.valueOf(getNumberOfCurtains(curtainOpening)));
		//Add heading size
		addMtomItemDetail(HEADING_SIZE, header.divide(new BigDecimal(10)).toPlainString());
		//Add finished drop
		double roundTo = 0.5;
		BigDecimal makeDrop = getMakeDrop(BigDecimal.valueOf(high));
		double doubleMakeDrop = roundTo * Math.round(((makeDrop.doubleValue()/10)/roundTo)) ;
		addMtomItemDetail(MAKE_DROP, String.valueOf(doubleMakeDrop));
		//Add heading type
		addMtomItemDetail(HEADING_TYPE, heading);
		//Add base hem
		addMtomItemDetail(BASE_HEM, baseHem);
		//Add drops per curtain
		addMtomItemDetail(DROPS_PER_CURTAIN, dropsPerCurtainField.toPlainString());
		//Add metres per curtain
		addMtomItemDetail(METRES_PER_CURTAIN, metresPerCurtain.divide(new BigDecimal(1000)).toPlainString());
		//Add heading width
		double roundTo1 = 0.5;
		double headingWidth = roundTo * Math.round(((headingWidthPerCurtainField.doubleValue()/10)/roundTo1)) ;
		addMtomItemDetail(HEADING_WIDTH, String.valueOf(headingWidth));
		
		return true;
	}
	
	public void addMtomItemDetail(String name, String description) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COALESCE(MAX(Line),0)+10 AS DefaultValue ");
		sql.append("FROM BLD_Mtom_Item_Detail WHERE bld_mtom_item_line_id = ");
		sql.append(mtom_item_line_id);
		int line = DB.getSQLValue(trxName, sql.toString());
		MBLDMtomItemDetail mBLDMtomItemDetail = new MBLDMtomItemDetail(Env.getCtx(), 0, trxName);
		mBLDMtomItemDetail.setName(name);
		mBLDMtomItemDetail.setDescription(description);
		mBLDMtomItemDetail.setLine(line);
		mBLDMtomItemDetail.setbld_mtom_item_line_ID(mtom_item_line_id);
		mBLDMtomItemDetail.saveEx(trxName);
	}
}
