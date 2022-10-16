/**
 * 
 */
package au.blindmot.make;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.compiere.model.MProduct;
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
public class CurtainTrack extends RollerBlind {

	//private int trackProfileID;
	private int carrierID;
	private int curtainTapeID;
	private int curtainBracketID;
	private String heading;
	private boolean isSwave;
	private String curtainOpening;
	private String position;
	private int driveBeltID;
	//private static String PART_TYPE_CURTAIN_MOTOR
	//private static final String ROLLER_TUBE = "Roller tube";



	public CurtainTrack(int product_id, int bld_mtom_item_line_id, String trxn) {
		super(product_id, bld_mtom_item_line_id, trxn);
		// TODO Auto-generated constructor stub
		interpretMattributeSetInstance();
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
				
				if(parTypeName.equals(PART_TYPE_ROLLER_TUBE))
				{
					rollerTubeID = mProductId;
				}
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString()))
				{
					carrierID = mProductId;
				}
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_CURTAIN_TAPE.toString()))
				{
					curtainTapeID = mProductId;//Needed for track?
				}
				
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_CURTAIN_BRACKET.toString()))
				{
					curtainBracketID = mProductId;
				}
				/*
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_FABRIC.toString()))
				{
					fabricID = mProductId;
				}
				else if(parTypeName.equals(CurtainConfig.PART_TYPE_CURTAIN_LINING.toString()))
				{
					liningID = mProductId;
				}*/
		}
		return true;
    }//setUserSelectedPartIds
	
	
	
	/**
	 * Gets the BOM qty for production items that have made to measure special amounts based on size
	 * Note: There's 2 columns in the m_product_bom_id table
	 * that both look like m_product_bom_id
	 * @param mProductBomid
	 * @return
	 *///TODO:Move track related calls to CurtainTrack.java
	@Override
	public BigDecimal getBomQty(int mProductBomid) { 
		//Track BOM qtys needed: track, carriers, drive belt, flick sticks?, stop carriers?, 
		int carrierFoundID = 0;
		if(carrierID > 0)
		{
			carrierFoundID = carrierID;
		}
		else
		{
			carrierID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_CARRIER.toString());
		}
		
		int headingTapeID = 0;
		if(curtainTapeID > 0)
		{
			headingTapeID = curtainTapeID;
		}
		else
		{
			curtainTapeID = getBomDerivedProductID(CurtainConfig.PART_TYPE_CURTAIN_TAPE.toString());
		}
		
		if(!(driveBeltID > 0))
		{
			driveBeltID = getBomDerivedProductID(CurtainConfig.PART_TYPE_DRIVE_BELT.toString());
		}
		
		
		
		BigDecimal qty = BigDecimal.ZERO;
		if(mProductBomid == rollerTubeID)
			{
				qty = getRollerTubeQty(rollerTubeID);//check to see if this works - if it does delete this comment.
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
				
			} 
			else if(mProductBomid == carrierFoundID)
			{
				String carrierPitchS = (String) MtmUtils.getMattributeInstanceValue(mProductBomid, CurtainConfig.ATTRIBUTE_CARRIER_PITCH.toString(), trxName);
				if(carrierPitchS == null && mProductBomid == 0)
				{
					return BigDecimal.ZERO;
					//throwNoCarrierError(carrierFoundID);
				}
				Double carrierPitch = Double.valueOf((String) MtmUtils.getMattributeInstanceValue(mProductBomid, CurtainConfig.ATTRIBUTE_CARRIER_PITCH.toString(), trxName));
				qty = BigDecimal.valueOf(MtmUtils.getTotalRunnerCount(wide, Curtain.getNumberOfCurtains(curtainOpening), carrierPitch));
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			}
			else if(mProductBomid == headingTapeID)
			{
				Double carrierPitch = Double.valueOf((String) MtmUtils.getMattributeInstanceValue(carrierFoundID, CurtainConfig.ATTRIBUTE_CARRIER_PITCH.toString(), trxName));
				BigDecimal totalRunnerCount = BigDecimal.valueOf(MtmUtils.getTotalRunnerCount(wide, Curtain.getNumberOfCurtains(curtainOpening), carrierPitch));
				BigDecimal sWaveDepth = new BigDecimal ((String)MtmUtils.getMattributeInstanceValue(mProductBomid, CurtainConfig.ATTRIBUTE_SWAVE_CURTAIN_TAPE_DEPTH.toString(), trxName));
				qty = totalRunnerCount.multiply(sWaveDepth);
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			}
			else if(mProductBomid == curtainBracketID)
			{
				//setIsFaceFix(mProductBomid);
				String bracketsPerMetre = (String) MtmUtils.getMattributeInstanceValue(mProductBomid, CurtainConfig.ATTRIBUTE_BRACKETS_PER_METRE.toString(), trxName);
				BigDecimal bigBracketsPerMetre = new BigDecimal(bracketsPerMetre);
				BigDecimal bigTrackWidth = BigDecimal.valueOf(wide);
				
				BigDecimal bracketsNeeded = bigTrackWidth.divide(new BigDecimal(1000)).multiply(bigBracketsPerMetre);
				BigDecimal bracketsRounded = bracketsNeeded.setScale(0, RoundingMode.HALF_UP);
				if(bracketsRounded.compareTo(Env.ZERO) > 0)
				{
					return bracketsRounded;
				}
				else
				{
					throw new AdempiereUserError(CurtainConfig.ERROR_NO_BRACKETS.toString());
				} 
			}
			else if(mProductBomid == driveBeltID)
			{
				return getBeltCut(driveBeltID);
			}
		return qty;//Default
		//return super.getBomQty(mProductBomid);//Default
	}//getBomQty
	
	public void throwNoCarrierError(int mProductID) {
		MProduct noPitchCarrier = MProduct.get(mProductID);
		throw new AdempiereUserError("Product: " + noPitchCarrier.toString() + " has no carrier pitch specifid, check product setup.");
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
			else if(mInstance.equalsIgnoreCase(CurtainConfig.ATTRIBUTE_CURTAIN_OPENING.toString()))
			{
				curtainOpening = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase(CurtainConfig.ATRRIBUTE_CURTAIN_POSITION.toString())) 
			{
				position = mInstanceValue;
			}
		}
		
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
	}
	
	/**
	 * Gets the belt cut for motorised tracks.
	 * @param beltProductID
	 * @return
	 */
	private BigDecimal getBeltCut(int beltProductID) {
		//Get head rail cut
		BigDecimal headRailCut = getRollerTubeCut(wide);
		BigDecimal widthAddition = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(beltProductID, MtmUtils.MTM_WIDTH_ADDITION.toString(), trxName));
		BigDecimal widthMultiplier = new BigDecimal((String)MtmUtils.getMattributeInstanceValue(beltProductID, MtmUtils.MTM_WIDTH_MULTIPLIER.toString(), trxName));
		BigDecimal beltCut = headRailCut.multiply(widthMultiplier).add(widthAddition);
		return beltCut;
	}
	
	@Override
	/**
	 * 
	 */
	public boolean getCuts() {
		
		if(driveBeltID > 0)
		{
			addBldMtomCuts(driveBeltID, Env.ZERO, getBeltCut(driveBeltID), Env.ZERO);
		}
		
		/*Uncomment to add carriers to cut list.
		 * 
		if(carrierID > 0)
		{
			int numOfCurtains = Curtain.getNumberOfCurtains(curtainOpening);
			BigDecimal totalCarriers = getBomQty(carrierID);
			BigDecimal carriersPerCurtain = totalCarriers.divide(new BigDecimal(numOfCurtains));
			
			for(int x = 0; x < numOfCurtains; x++)
			{
				addBldMtomCuts(carrierID, Env.ZERO, carriersPerCurtain, Env.ZERO);
			}
			
		} */
		
		super.getCuts();
		return true;
	}
}
