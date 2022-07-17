package au.blindmot.make;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.compiere.model.MProduct;
import org.compiere.model.MProductBOM;
import org.compiere.model.X_M_PartType;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.eevolution.model.MPPProductBOM;

import au.blindmot.model.MBLDBomDerived;
import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDMtomCuts;
import au.blindmot.model.MBLDMtomItemLine;
import au.blindmot.model.MBLDProductNonSelect;
import au.blindmot.model.MBLDProductPartType;
import au.blindmot.utils.MtmUtils;


public class RollerBlind extends MadeToMeasureProduct{

	private ArrayList<KeyNamePair> chainAcc = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> rollerTube = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> endCap = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> miscItem = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> liftSpring = new ArrayList<KeyNamePair>();
	private String rollTypeIns = null;
	private String controlSide = null;
	boolean isChainControl = false;
	
	//Part types as must be entered in Product window
	private static String PART_TYPE_CONTROL_COMP = "Tubular blind control";
	protected static String PART_TYPE_BOTTOM_BAR = "Bottom bar";
	private static String PART_TYPE_NON_CONTROL_MECH = "TNCM";
	private static String PART_TYPE_ROLLER_BRACKET = "Roller bracket";
	private static String PART_TYPE_CHAIN_ACC = "Chain accessory";
	protected static String PART_TYPE_ROLLER_TUBE = "Roller tube";
	private static String PART_TYPE_LIFT_SPRING = "Lift spring";
	private static String PART_TYPE_END_CAP = "End Cap";
	public static String PART_TYPE_FABRIC = "Fabric";
	private static String IS_CHAIN_CONTROL = "Is chain control";
	private static String LIFT_SPRING_CAPACITY_UPPER = "Lift Spring capacity upper";
	private static String LIFT_SPRING_ROTATION = "Lift Spring rotation";
	
	
	protected int controlID = 0;
	protected int rollerTubeID = 0;
	protected int fabricID = 0;
	protected int bottomBarID = 0;
	protected int liftSpringID = 0;
	String patternString = null;
    Pattern pattern = null;
    BigDecimal oneHundred = new BigDecimal("100");
	BigDecimal oneThousand = new BigDecimal("1000");

	public RollerBlind (int product_id, int bld_mtom_item_line_id, String trxn) {
		super(product_id, bld_mtom_item_line_id, trxn);
		interpretMattributeSetInstance();
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
	
			if(mInstance.equalsIgnoreCase("Roll type"))
			{
				rollTypeIns = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase(CONTROL_SIDE))
			{
				controlSide = mInstanceValue;
			}
		}
		
	}
	

	@Override
	/**
	 * 
	 * 
	 */
	public boolean getCuts() {
		log.warning("---------In getCuts()");
		/*
		 * /TODO: get the fabricID, rollerTubeID, bottomBarID from BOM lines in case they've been edited by user.
		 * TODO: Modify to iterate through BOM derived and cut as appropriate:
		 * getBomDerived
		 * iterate -> get cut based on parttype
		 */
		
	    //setUserSelectedPartIds();
	    setChainControl(controlID);
		populatePartTypes(m_product_id);//Gets the ArrayLists of partsget
		setupTubeFabric();
		//setValueProductID();//Currently Set roller tube ID based on blind width only
		
		//Get a value for field fabricWidth & fabricDrop
		BigDecimal fabricWidth = getFabricWidth();
		BigDecimal fabricDrop = getFabricDrop();
		BigDecimal bottomBarCut = getBottomBarCut();
		
		if(fabricID !=0 )

			{
				addBldMtomCuts(fabricID, fabricWidth, fabricDrop, Env.ZERO);	
			}
		
		int rollerTID = getBomDerivedProductID(PART_TYPE_ROLLER_TUBE);
		log.warning("--------Roller tube ID found: " + rollerTID);
		int rollerIDToUse = 0;
		if(rollerTID > 0)
		{
			rollerIDToUse = rollerTID;
		}
		else
		{
			rollerIDToUse = rollerTubeID;
		}
		
		if(rollerTubeID !=0 )
			{
				addBldMtomCuts(rollerTubeID,Env.ZERO,getRollerTubeCut(wide),Env.ZERO);
				log.warning("--------addBldMtomCuts Adding roller tube to cuts: " + rollerIDToUse);
			}
		
		int bottombarIDToUse = 0;
		int bottombID = getBomDerivedProductID(PART_TYPE_BOTTOM_BAR);
		if(bottombID > 0)
		{
			bottombarIDToUse = bottombID;
		}
		else
		{
			bottombarIDToUse = bottomBarID;
		}
		
		if(bottombarIDToUse !=0 )

			{
				addBldMtomCuts(bottombarIDToUse,Env.ZERO,bottomBarCut,Env.ZERO);
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
		
	}//getCuts

	

	@Override
	public boolean createBomDerived() {
		/*
		 * What we want to do here is add all the parts required to make the blind.
		 * The info we have is: 
		 * 1. the list of actual ProductIDs from the mtmInstanceParts field which is set when getMAttributeSetInstance() is called.
		 * 2. The attribute pairs from the getMAttributeSetInstance() method which returns an array of MAttributePairs which will need to matched to parts
		 * The following partypes need to be determined then records added to the bld_mtom_bomderived table:
		 * Chain (direct from mtmInstanceParts)
		 * Bottom bar (direct from mtmInstanceParts)
		 * Fabric(direct from mtmInstanceParts)
		 * Tubular blind control
		 * Roller bracket
		 * TNCN - tubular non control mech
		 * etc
		 * From the AttributePair[]
		 */
		//Get fields setup
		setUserSelectedPartIds();
		addMtmInstancePartsToBomDerived();
		populatePartTypes(m_product_id) ;
		setChainControl(controlID);//Must be called before setAutoSelectedPartIds()
		
		//TODO: Handle adding tube tape, bubble, packaging tape, masking tape, base bar stickers, tube selection
		
		return true;
	}//createBomDerived()

	@Override
	public boolean deleteBomDerived() {
		// TODO Auto-generated method stub
		return true;
	}//deleteBomDerived


	@Override
	public boolean deleteCuts() {
		MBLDMtomCuts[] cuts = null;
	if(mBLDMtomItemLine!=null)
	{
		cuts = mBLDMtomItemLine.getCutLines(Env.getCtx(), mtom_item_line_id);
	}
	for(int i =0; i<cuts.length; i++)
	{
		cuts[i].deleteEx(true);
	}

		return true;
	}//deleteCuts
	
	/**
	 * Searches for all the BOM parts from product with m_product_id
	 * sorts them into ArrayList with a KeyNamePair array for each relevant
	 * m_parttype_id
	 * @param m_product_id
	 */
	protected void populatePartTypes(int m_product_id) {
		MPPProductBOM mPPProductBOM = MPPProductBOM.getDefault(MProduct.get(m_product_id), trxName);
		int pPproductBomID = mPPProductBOM.getPP_Product_BOM_ID();
		
		StringBuilder sql = new StringBuilder("SELECT mp.m_parttype_id, mp.m_product_id, mp.name ");
		sql.append("FROM m_product mp ");
		sql.append("JOIN pp_product_bomline ppb ");
		sql.append("ON mp.m_product_id = ppb.m_product_id ");
		sql.append("AND ppb.pp_product_bom_id = ");
		sql.append(pPproductBomID);
		sql.append(" ORDER BY mp.m_parttype_id");
		
		List<String> mpName = new ArrayList<String>();
		List<Integer> partIDs = new ArrayList<Integer>();
		List<Integer> prodIDs = new ArrayList<Integer>();
		
		ResultSet rs = DB.getRowSet(sql.toString());
		System.out.println(rs.toString());
		
		    try {
				while (rs.next()) {
					partIDs.add(rs.getInt(1));
					prodIDs.add(rs.getInt(2));
					mpName.add(rs.getString(3));
				}
			} catch (SQLException e) 
		    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    finally
		    {
		    	DB.close(rs);
		    	rs = null;
		    }
		
		Integer parts[] = partIDs.toArray(new Integer[(partIDs.size())]);
		String names[] = mpName.toArray(new String[mpName.size()]);
		Integer productIDs[] = prodIDs.toArray(new Integer[(prodIDs.size())]);
		
	
	
		/*TODO: The parts added by the editor dialogue can be removed from the code below
		 * 
		 */
			for(int i = 0; i < parts.length; i++) {
	
				X_M_PartType mPartType = new X_M_PartType(null, parts[i], null);
				String partType = (String) mPartType.get_Value("name");
				
				if(partType == null)
				{
					miscItem.add(new KeyNamePair(productIDs[i], names[i]));;
				}
				else if(partType.equalsIgnoreCase(PART_TYPE_CHAIN_ACC))
				{
					chainAcc.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(PART_TYPE_ROLLER_TUBE))
				{
					rollerTube.add(new KeyNamePair(productIDs[i], names[i]));
				}
				//TODO: Pick end cap to match colour of base bar.
				else if(partType.equalsIgnoreCase(PART_TYPE_END_CAP))
				{
					endCap.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(PART_TYPE_LIFT_SPRING))
				{
					liftSpring.add(new KeyNamePair(productIDs[i], names[i]));
				}
				
			}
			
			
		}//populatePartTypes

	
	/**
	 * Gets the BOM qty for production items that have made to measure special amounts based on size
	 * Note: There's 2 columns in the m_product_bom_id table
	 * that both look like m_product_bom_id
	 * @param mProductBomid
	 * @return
	 */
	@Override
	public BigDecimal getBomQty(int mProductBomid) { 
		BigDecimal qty = BigDecimal.ZERO;
		if(mProductBomid == rollerTubeID)
			{
				qty = getRollerTubeQty(rollerTubeID);
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
				
			} else if(mProductBomid == fabricID)
			{
				qty = getFabricQty();
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			} else if(mProductBomid == bottomBarID)
			{
				qty = getBottomBarQty();
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
			} else if(mProductBomid == liftSpringID)
			{
				qty = Env.ONE;
				return qty;
			}
		return qty;//Default
		//return super.getBomQty(mProductBomid);
	}//getBomQty
		
		/*
		 * TESTME: Below code moved to superclass 5/12/2020, test & remove.
		 * 
		StringBuilder sql = new StringBuilder("SELECT m_product_bom_id ");
	        sql.append("FROM m_product_bom ");
	        sql.append("WHERE m_product_id = ");
	        sql.append(m_product_id);
	        sql.append(" AND m_productbom_id = ");
	        sql.append(mProductBomid);
	        
	        int m_product_bom_id = DB.getSQLValue(trxName, sql.toString());
			MProductBOM mProductBom = new MProductBOM(Env.getCtx(), m_product_bom_id, trxName);
			//if(mProductBom.get_ValueAsBoolean(columnName))
			
			
			
			BigDecimal bigQty = mProductBom.getBOMQty();
		
		return bigQty; */
		
	

	/**
	 * Sets fields from parts that users can select from the part dialog selection.
	 * @return 
	 */
	//TODO: Modify so all parts are added to BOMDerived
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
				if(parTypeName.equals(PART_TYPE_CONTROL_COMP))
				{
					controlID = mProductId;
				}
				if(parTypeName.equals(PART_TYPE_NON_CONTROL_MECH))
				{
				}
				if(parTypeName.equals(PART_TYPE_ROLLER_BRACKET))
				{
				}
				if(parTypeName.equals(PART_TYPE_ROLLER_BRACKET))
				{
				}
				if(parTypeName.equals(PART_TYPE_FABRIC))
				{
					fabricID = mProductId;
				}
				if(parTypeName.equals(PART_TYPE_BOTTOM_BAR))
				{
					bottomBarID = mProductId;
				}
		}
		return true;
    }//setUserSelectedPartIds
	
	/**
	 * 
	 * @param partProduct_id
	 * @param description
	 */
	private void addBomDerivedLines(int partProduct_id, String description){
		if(partProduct_id != 0)
		{
			addMBLDBomDerived(partProduct_id, getBomQty(partProduct_id), 0, description);
		}
		
	}//addBomDerivedLines
	
	/**
	 * MOVE TO SUPERCLASS?
	 * @param mProductID
	 * @param width
	 * @param length
	 * @param height
	 */
	/*
	protected void addBldMtomCuts(int mProductID, BigDecimal width, BigDecimal length, int height){
		BigDecimal bigWidth = width;
		BigDecimal bigLength = length;
		BigDecimal bigHeight = new BigDecimal(height);
		if(mProductID != 0)
		{
			MBLDMtomCuts cut = new MBLDMtomCuts(Env.getCtx(), 0, trxName);
			cut.setWidth(bigWidth);
			cut.setLength(bigLength);
			cut.setHeight(bigHeight);
			cut.setM_Product_ID(mProductID);
			cut.setbld_mtom_item_line_ID(mtom_item_line_id);
			cut.saveEx();
		}
		
	}//addBldMtomCuts */

	 
	 public ArrayList <KeyNamePair> getHeadRailComps() {
	 ArrayList <KeyNamePair> headRailComps = new ArrayList <KeyNamePair>();
	 
	 //Allow blinds to be built without brackets but not without control and non control mechs.
	 
	 //TODO: Add headrail comps from bomlines
	 
	 	MBLDMtomItemLine item = new MBLDMtomItemLine(Env.getCtx(), mtom_item_line_id, trxName);
	 	MBLDBomDerived[] bomDLines = item.getBomDerivedLines(Env.getCtx(), mtom_item_line_id);
	 	for(int i = 0; i < bomDLines.length; i++)
	 	{
	 		if(bomDLines[i].hasDeduction(MtmUtils.MTM_HEAD_RAIL_DEDUCTION))
	 		{
	 			KeyNamePair qtyID = new KeyNamePair(bomDLines[i].getQty().intValue(),Integer.toString(bomDLines[i].getM_Product_ID()));
	 			headRailComps.add(qtyID);
	 			log.warning("--------Adding product to HeadrailComps: " + new MProduct(Env.getCtx(), bomDLines[i].getM_Product_ID(), trxName).getName());
	 		}
	 	}
		return headRailComps;
	 }//getHeadRailComps
	 
	 public BigDecimal getRollerTubeCut(int width) {
		 BigDecimal bigWidth = new BigDecimal(width);
		 log.warning("---------In RollerBlind.getRollerTubeCut");
		 log.warning("-------About to go intoMtmUtils.attributePreCheck()");
		 MtmUtils.attributePreCheck(MtmUtils.MTM_HEAD_RAIL_DEDUCTION);
		 log.warning("About to call static method MBLDMtomCuts.getDeductions with deduction type: " + MtmUtils.MTM_HEAD_RAIL_DEDUCTION +" and Headrail comps");
		 BigDecimal deductions = MBLDMtomCuts.getDeductions(getHeadRailComps(), MtmUtils.MTM_HEAD_RAIL_DEDUCTION, trxName);
		 log.warning("---------In getRollerTubeCut() Deductions from MBLDMtomCuts.getDeductions(getHeadRailComps(), MtmUtils.MTM_HEAD_RAIL_DEDUCTION, trxName: " + deductions.toString());
		 bigWidth = bigWidth.subtract(deductions);
		 return bigWidth;
	 }//getRollerTubeCut
	 
	 public BigDecimal getBottomBarCut() {
		 BigDecimal fabricWidth = getFabricWidth();
		 log.warning("-------About to go intoMtmUtils.attributePreCheck()");
		 MtmUtils.attributePreCheck(MtmUtils.MTM_BOTTOM_BAR_DEDUCTION);
		//Note: MTM_BOTTOM_BAR_DEDUCTION is part of parent product non instance attribute.
		 log.warning("About to call static method MBLDMtomCuts.getDeduction with deduction type: " + MtmUtils.MTM_BOTTOM_BAR_DEDUCTION);
		 fabricWidth = fabricWidth.add(MBLDMtomCuts.getDeduction(m_product_id, MtmUtils.MTM_BOTTOM_BAR_DEDUCTION, trxName));
		 return fabricWidth;
	 }//getBottomBarCut
	 
	 public BigDecimal getFabricWidth() {
		 BigDecimal fabWidth = getRollerTubeCut(wide);
		 log.warning("-------About to go intoMtmUtils.attributePreCheck()");
		 MtmUtils.attributePreCheck(MtmUtils.MTM_FABRIC_DEDUCTION);
		 log.warning("About to call static method MBLDMtomCuts.getDeduction with deduction type: " + MtmUtils.MTM_FABRIC_DEDUCTION);
		 /* 18/8/18 changed location of MTM_FABRIC_DEDUCTION from fabric to parent product (the roller blind itself)
		  * as a non instance attribute.*/
		 fabWidth = fabWidth.subtract(MBLDMtomCuts.getDeduction(m_product_id, MtmUtils.MTM_FABRIC_DEDUCTION,trxName));
		 return fabWidth;
	 }//getFabricWidth

	 public BigDecimal getFabricDrop() {
		 BigDecimal bigHigh = new BigDecimal(high);
		 log.warning("-------About to go intoMtmUtils.attributePreCheck()");
		 MtmUtils.attributePreCheck(MtmUtils.MTM_FABRIC_ADDITION);
		 log.warning("About to call static method MBLDMtomCuts.getDeduction with deduction type: " + MtmUtils.MTM_FABRIC_ADDITION);
		 //Note: MTM_FABRIC_ADDITION is part of parent product non instance attribute.
		 bigHigh = bigHigh.add(MBLDMtomCuts.getDeduction(m_product_id, MtmUtils.MTM_FABRIC_ADDITION, trxName));
		 return  bigHigh;
	 }//getFabricDrop
	 
	 public BigDecimal getFabricQty() {
		 	BigDecimal fwidth = getFabricWidth().divide(oneThousand);
			BigDecimal fdrop = getFabricDrop().divide(oneThousand);
			BigDecimal qty = fwidth.multiply(fdrop);
			BigDecimal waste = new BigDecimal(getWaste(m_product_id ));//fabric waste is in finished product attribute.
			qty = ((qty.divide(oneHundred).multiply(waste).add(qty)));
			qty.setScale(4, BigDecimal.ROUND_CEILING);
			return qty;
	 }//getFabricQty
	 
	 public BigDecimal getRollerTubeQty(int rtubeID) {
		 	BigDecimal qty = getRollerTubeCut(wide);
			BigDecimal waste = new BigDecimal(getWaste(rtubeID));
			qty = ((qty.divide(oneHundred).multiply(waste).add(qty)));
			return qty;
	 }
	 
	 public BigDecimal getBottomBarQty() {
		 	BigDecimal qty = getBottomBarCut();
			BigDecimal waste = new BigDecimal(getWaste(bottomBarID));
			qty = ((qty.divide(oneHundred).multiply(waste).add(qty)));
			return qty;
	 }//getRollerTubeQty
	 
	 /**
	  * This method gets the BOM line product IDs and was part of a refactor 
	  * to allow the user to change BOM line product types. Must be called after the BOM has been populated, ie after createBomDerived()
	  * MOVED TO SUPERCLASS 31/8/2021
	  * @param partType
	  * @return
	  
	 public int getBomDerivedProductID(String partType) {
		 log.warning("---------In getBomProductID(String partType)---> String partType = " + partType);
		 MBLDBomDerived[] bomDerived = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), mBLDMtomItemLine.getbld_mtom_item_line_ID());
		 log.warning("--------In getBomProductID() bomDerived: " + bomDerived.toString());
		 
		 for(int p = 0; p < bomDerived.length; p++)
		 {
			 int productID = bomDerived[p].getM_Product_ID();
			 MProduct mProduct = new MProduct(Env.getCtx(), productID, trxName);
			 int mPartTypeID = mProduct.getM_PartType_ID();
			 StringBuilder sql = new StringBuilder("SELECT name FROM m_parttype ");
			 sql.append(" WHERE m_parttype_id = ?");
			 String partName = DB.getSQLValueString(trxName, sql.toString(), mPartTypeID);
		
			 if(partName != null)
			 {
				 if(partName.equalsIgnoreCase(partType)) 
				 {
					 log.warning("---------returning mProduct: " + mProduct.getName() + "For parttype: " + partType);
					 return productID; 
				 }
			 }
			 else
			 {
				 log.warning("Could not get a part name for partType: " + partType);
			 }
			 
		 }
		 return 0;
	 }*///getBomProductID
	 
	 private int getLiftSpring(boolean useWeight) {
		/*
		 * How heavy is the blind?
		 * What is its rotation?
		 * Check liftSpring array - is it empty? If not, then loop through array and find the lightest spring that
		 * will do the job.
		 * Only specify a spring if the BOM has one that has a range that the blind weight falls into,
		 * IE no spring unless blind is heavy enough for the available springs
		 */
		 BigDecimal weight = Env.ZERO;
		 if(useWeight)
		 {
			 weight = MtmUtils.getHangingMass(wide, high, fabricID, bottomBarID, trxName);
		 }
		 
		 String rotation = MtmUtils.getRotation(rollTypeIns, controlSide);
		 if(rotation == null || rotation == "")
			 {
			 	log.warning("Could not determine rotation of ID: " + mtom_item_line_id + ", check control side.");
			 	return 0;
			 }
		 MtmUtils.attributePreCheck(LIFT_SPRING_CAPACITY_UPPER);
		 MtmUtils.attributePreCheck(LIFT_SPRING_CAPACITY_UPPER);
		 MtmUtils.attributePreCheck(LIFT_SPRING_ROTATION);
		 if(liftSpring.isEmpty())return 0;
		 for(KeyNamePair spring : liftSpring) 
		 {
			 int springID = spring.getKey();
			 String lowerLimit = (String) MtmUtils.getMattributeInstanceValue(springID, "Lift Spring capacity lower", trxName);
			 String upperLimit = (String) MtmUtils.getMattributeInstanceValue(springID, "Lift Spring capacity upper", trxName);
			 String springRotation = (String) MtmUtils.getMattributeInstanceValue(springID, "Lift Spring rotation", trxName);
		     if(springRotation != null && upperLimit != null && lowerLimit != null)
		     {
		     BigDecimal lower = new  BigDecimal(lowerLimit); 
		     BigDecimal upper = new  BigDecimal(upperLimit);
		     int retval = weight.compareTo(lower);
		     int retval2 = weight.compareTo(upper);
		     if(!useWeight || retval > 0 && retval2 <0)//The blind weight is in the correct range of the spring or we're not using weight
			     { 
			    	 if(rotation != null || rotation != "")
			    	 {
				    	 if(springRotation.equalsIgnoreCase(rotation))
					    	 {
				    		 liftSpringID = springID;
				    		 return springID; 
					    	 }
				     }
			     }
		     }
			 
		 }
		 log.warning("Could not resolve lift spring for product: " +  m_product_id + ". Check the attributes for BOM lift springs for this product");
		 return 0;
		 
	 }//getLiftSpring
	 
	 
	/**
	 * Moved to Superclass 29/8/2021
	 */
	/**public boolean addMtmInstancePartsToBomDerived() {
		MBLDLineProductInstance[] mBLDLineProductInstance = getMBLDLineProductInstance();
															
		for(int i = 0; i < mBLDLineProductInstance.length; i++)
		{
			int mProductId = mBLDLineProductInstance[i].getM_Product_ID();
			BigDecimal qty = getBomQty(mProductId);
			addMBLDBomDerived(mProductId, qty, trxName);
		}
		return true;
	}*/ //addMtmInstancePartsToBomDerived 
	
/**
 *
 * @param controlProductID
 */
public void setChainControl(int controlProductID) {
	if(controlProductID > 0)
	{
		String chainControl = (String) MtmUtils.getMattributeInstanceValue(controlProductID, IS_CHAIN_CONTROL, null);
		if(chainControl.equalsIgnoreCase("Yes"))
		{
			isChainControl = true;
		}
		else
		{
			isChainControl = false;
		}
	}
}//setChainControl

@Override
public List<String> getConfig() {
		ArrayList <String> config = new ArrayList<String>();
		config.add("Attribute: Bottom bar addition - The number to add to fabric to get the bottom bar length.");
		config.add("Attribute: Fabric deduction - Deduction relative to tube, usually -ve");
		config.add("Attribute: Fabric length addition - How much extra to add to fabric cut relative to the length of a blind.");
		config.add("Instance Attribute: Width");
		config.add("Instance Attribute: Drop");
		config.add("Instance Attribute: Blind control side - List: Left, Right");
		config.add("Instance Attribute: Roll Type - List: NR, RR");
		return config;
	}//getConfig()

/* (non-Javadoc)
 * @see au.blindmot.make.MadeToMeasureProduct#performOperationAddition(au.blindmot.model.MBLDProductNonSelect, au.blindmot.model.MBLDProductPartType)
 */
@Override
public boolean performOperationAddition(MBLDProductNonSelect mBLDPNonSelect, MBLDProductPartType mBLDProductPartType) {
	 
		 //perform addition
		 int addID = Integer.parseInt(mBLDPNonSelect.getaddtionalproduct().toString());
		 X_M_PartType addPartType = new X_M_PartType(Env.getCtx(), mBLDProductPartType.getM_PartTypeID(), null);
		 
		 if(addPartType != null)
		 {
			 if(addPartType.getName().equalsIgnoreCase("Roller tube"))
			 {
				log.warning("Roller tube cut = " + getRollerTubeCut(wide));
				BigDecimal waste = new BigDecimal(getWaste(addID));
				BigDecimal rollerTubeQty = getRollerTubeQty(addID);
				addMBLDBomDerived(addID, rollerTubeQty, 0, "Procesed with waste factor of: " + (rollerTubeQty.multiply(waste)));
			 }
			 else if(addPartType.getName().equalsIgnoreCase("Bottom bar"))
			 {
				 addMBLDBomDerived(addID, getBottomBarCut(), 0, trxName);
			 }
			 else
			 {
				 BigDecimal qty = getBomQty(addID);
				 if(qty.compareTo(BigDecimal.ZERO) == 0)
				 {
					 qty = BigDecimal.ONE;//Make Sure it gets added as zeros get skipped in addMBLDBomDerived(addID, qty, trxName);
				 }
				 addMBLDBomDerived(addID, qty, 0, "Added by performOperationAddition() ");
			 }
			 	
		 } return true;

}//performOperationAddition

/**
 * 
 */
@Override
public boolean performOperationConditionSet(MBLDProductNonSelect mBLDPNonSelect) {
		 //perform conditon set
	log.warning("--------In performOperationConditionSet(MBLDProductNonSelect mBLDPNonSelect)");
	log.warning("-----MBLDProductNonSelect.MTM_NON_SELECT_CONDITION_HAS_LIFT_SPRING: " + mBLDPNonSelect.getcondition_set().equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_CONDITION_HAS_LIFT_SPRING));
	log.warning("isChainControl: " + isChainControl);
		 if(mBLDPNonSelect.getcondition_set().equalsIgnoreCase(MBLDProductNonSelect.MTM_NON_SELECT_CONDITION_HAS_LIFT_SPRING))
		 {
			 if(isChainControl)//Add a lift spring if it's chain controlled
				{
					int liftID = getLiftSpring(false);
					log.warning("liftID: " + liftID );
					if(liftID  > 0)addBomDerivedLines(liftID, null);	
				}
		 }
	 
	return true;
}//performOperationConditionSet

	/* (non-Javadoc)
	 * @see au.blindmot.make.MadeToMeasureProduct#addTriggeredBom(int)
	 */
	@Override
	public boolean addTriggeredBom(int parentBomID, int qty) {
		super.addTriggeredBom(parentBomID, qty);
		return false;
	}

	public void setupTubeFabric() {
	
		int fabId = getBomDerivedProductID("Fabric");
		if(fabId > 0)
			{
				fabricID = fabId;
			}
		
		int rollerTID = getBomDerivedProductID(PART_TYPE_ROLLER_TUBE);
		log.warning("--------Roller tube ID found: " + rollerTID);
		if(rollerTID > 0)
		{
			rollerTubeID = rollerTID;
		}
		
	}//setupTubeFabric()

}//RollerBlind
