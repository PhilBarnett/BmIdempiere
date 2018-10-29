package au.blindmot.make;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.sql.RowSet;

import org.compiere.model.MAttribute;
import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.MProduct;
import org.compiere.model.MProductBOM;
import org.compiere.model.X_M_PartType;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

import au.blindmot.model.MBLDBomDerived;
import au.blindmot.model.MBLDLineProductInstance;
import au.blindmot.model.MBLDMtomCuts;
import au.blindmot.model.MBLDProductPartType;
import au.blindmot.utils.MtmUtils;


public class RollerBlind extends MadeToMeasureProduct {
	
	private ArrayList<KeyNamePair> controlType = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> bottomBar = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> TubularNCM = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> rollerBracket = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> chainSafe = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> chainAcc = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> rollerTube = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> endCap = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> miscItem = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> liftSpring = new ArrayList<KeyNamePair>();
	private String blindControlIns = null;
	private String tNCMIns = null;
	private String rollerBracketIns = null;
	private String rollerBracketColIns = null;
	private String mechColIns = null;
	private String chainSafeIns = null;
	private String rollTypeIns = null;
	private String controlSide = null;
	boolean isChainControl = false;
	
	//Part types as must be entered in Product window
	private static String CONTROL_COMP = "Tubular blind control";
	private static String BOTTOM_BAR = "Bottom bar";
	private static String NON_CONTROL_MECH = "TNCM";
	private static String ROLLER_BRACKET = "Roller bracket";
	private static String CHAIN_SAFE = "Chain Safe";
	private static String CHAIN_ACC = "Chain accessory";
	private static String ROLLER_TUBE = "Roller tube";
	private static String LIFT_SPRING = "Lift spring";
	private static String END_CAP = "End Cap";
	private static String FABRIC = "Fabric";
	
	//
	private static String FABRIC_LENGTH_ADDITION = "Fabric length addition";
	
	//Various BOM parts to make the blind
	private int controlBracketID = 0;
	private int nonControlBracketID = 0;
	private int controlID = 0;
	private int nonControlID = 0;
	private int chainSafeID = 0;
	private int rollerTubeID = 0;
	private int fabricID = 0;
	private int endCapID = 0;
	private int bottomBarID = 0;
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
		//MBLDMtomItemLine mtmrolleritem = new MBLDMtomItemLine(Env.getCtx(), mtom_item_line_id, trxName);
		
		//Set fields based on the AttributePair[] contents.
		for(int i = 0; i < attributePair.length; i++)
		{
			mInstance = attributePair[i].getInstance();
			mInstanceValue = attributePair[i].getInstanceValue();
			/*
			 * TODO: Decide if the column 'location' in the mtmitem table will ever be used.
			if(mInstance.equalsIgnoreCase("Location"))
			{
				if(mtmrolleritem.getlocation() == null || mtmrolleritem.getlocation().length()<1)
				mtmrolleritem.setlocation(mInstanceValue);
			}
			
			else*/ 
			//TODO: Is if(mInstance.equalsIgnoreCase("Blind control")) required?
			
			/*
			if(mInstance.equalsIgnoreCase("Blind control"))
			{	
				blindControlIns = mInstanceValue;
				if(mInstanceValue.equalsIgnoreCase("Chain control"))
				{
					isChainControl = true;
				}
				
			}	
			*/
			/*else if(mInstance.equalsIgnoreCase("Blind non control mech"))
			{
				tNCMIns = mInstanceValue;
			}*/
			/*else if(mInstance.equalsIgnoreCase("Roller bracket"))
			{
				rollerBracketIns = mInstanceValue;
			}*/
			/*else if(mInstance.equalsIgnoreCase("Mech colour"))
			{
				mechColIns = mInstanceValue;
			}*/
			/*else if(mInstance.equalsIgnoreCase("Bracket colour"))
			{
				rollerBracketColIns = mInstanceValue;
			}*/
			/*else if(mInstance.equalsIgnoreCase("Chain safe"))
			{
				chainSafeIns = mInstanceValue;
			}*/
			if(mInstance.equalsIgnoreCase("Roll type"))
			{
				rollTypeIns = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase("Blind control side"))
			{
				controlSide = mInstanceValue;
			}
		}
		
		//addColoursToBracketsControls();
		//mtmrolleritem.saveEx();
	}
	
	/*TODO: Deprecate this
	 * What a horrible way to accomplish this!
	 * /Handle colours
	 *
	private void addColoursToBracketsControls() {
		
		//Add colour to the non control mech
		if(mechColIns != null || mechColIns != "")
		{
			//Add colour to the non control mech
			StringBuilder newtNCMIns = new StringBuilder(tNCMIns);
			newtNCMIns.append(" " + mechColIns);
			tNCMIns = newtNCMIns.toString();
		}
		
		//Add colour to the brackets
		if(rollerBracketColIns != null || rollerBracketColIns != "")
		{
			StringBuilder newrollerBracketIns = new StringBuilder(rollerBracketIns);
			newrollerBracketIns.append(" " + rollerBracketColIns);
			rollerBracketIns = newrollerBracketIns.toString();
		}
		
		//Add colour to any chain control only if it's chain driven
		if(isChainControl && (mechColIns != null || mechColIns != " "))
		{
			StringBuilder newblindControlIns = new StringBuilder(blindControlIns);
			newblindControlIns.append(" " + mechColIns);
			blindControlIns = newblindControlIns.toString();
		}
	}*/

	@Override
	/**
	 * 
	 * 
	 */
	public boolean getCuts() {
		
		/*
		 * /TODO: get the fabricID, rollerTubeID, bottomBarID from BOM lines in case they've been edited by user.
		 */
		patternString = "[0-9][0-9][0-9][0-9][0-9][0-9][0-9]";
	    pattern = Pattern.compile(patternString);
	    //setInstanceFields();//Sets instance fields from the instance_string column
	    setUserSelectedPartIds();
	    setChainControl(controlID);
		populatePartTypes(m_product_id);//Gets the ArrayLists of parts
		setValueProductID();//Currently Set roller tube ID based on blind width only
		
		//Get a value for field fabricWidth & fabricDrop
		BigDecimal fabricWidth = getFabricWidth();
		BigDecimal fabricDrop = getFabricDrop();
		BigDecimal bottomBarCut = getBottomBarCut();
		int fabricLengthAddition = getFabricLengthAddition(m_product_id);
		
		int fabId = getBomProductID("Fabric");
		int fabIdToUse = 0;
		if(fabId > 0)
			{
				fabIdToUse = fabId;
			}
		else
			{
				fabIdToUse = fabricID;
			}
		if(fabIdToUse !=0 )

			{
				addBldMtomCuts(fabIdToUse, fabricWidth, fabricDrop/*TODO: Remove when tested .add(new BigDecimal(fabricLengthAddition))*/, 0);	
			}
		
		int rollerTID = getBomProductID(ROLLER_TUBE);
		int rollerIDToUse = 0;
		if(rollerTID > 0)
		{
			rollerIDToUse = rollerTID;
		}
		else
		{
			rollerIDToUse = rollerTubeID;
		}
		
		if(rollerIDToUse !=0 )
			{
				addBldMtomCuts(rollerIDToUse,Env.ZERO,getRollerTubeCut(wide),0);			
			}
		
		int bottombarIDToUse = 0;
		int bottombID = getBomProductID(BOTTOM_BAR);
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
				addBldMtomCuts(bottombarIDToUse,Env.ZERO,bottomBarCut ,0);
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

	private int getFabricLengthAddition(int m_product_id) {

		MProduct mProduct = new MProduct(Env.getCtx(), m_product_id, trxName);
		int maid = mProduct.getM_AttributeSetInstance_ID();
		MAttributeSetInstance fromMAttributeSetInstance = MAttributeSetInstance.get(Env.getCtx(), maid, m_product_id);
		//int fablengthAddition = mAttributeInstance.get
		int value = 0;
	
		RowSet fromAttributeInstances = getmAttributeInstances(fromMAttributeSetInstance.getM_AttributeSetInstance_ID());
		
	    try {
			while (fromAttributeInstances.next()) {  
			
				int m_attribute_id = fromAttributeInstances.getInt(1);
				MAttribute mAttribute = new MAttribute(Env.getCtx(), m_attribute_id, trxName);
				String name = mAttribute.getName();
				String attributeType = mAttribute.getAttributeValueType();
				if(attributeType.equalsIgnoreCase("N"))
		    		{
		    			value = fromAttributeInstances.getInt(2);
		    		}
			    
			    if(name.equalsIgnoreCase(FABRIC_LENGTH_ADDITION))return value;
			}
	    }catch (SQLException e) {
				log.severe("Could not get values from attributeinstance RowSet " + e.getMessage());
				e.printStackTrace();
			}   
			   return value;
	}


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
		setChainControl(controlID);
		addMtmInstancePartsToBomDerived();
		populatePartTypes(m_product_id) ;
		setValueProductID();//Chain accessory BOM lines are added here.
		//setInstanceFields();
		/*
		addMtmInstancePartsToBomDerived();//Creates BOM lines from the products in the 'instance_string' column.
		addBomDerivedLines(controlID, null);
		addBomDerivedLines(nonControlID, null);
		addBomDerivedLines(nonControlBracketID, null);
		addBomDerivedLines(controlBracketID, null);
		addBomDerivedLines(chainSafeID, null);
		addBomDerivedLines(endCapID, null);
		*/
		if(isChainControl) 
			{
				int liftID = getLiftSpring();
				if(liftID  > 0)addBomDerivedLines(liftID, null);
				
			}
		
		//BOM derived rollertube
		BigDecimal waste = new BigDecimal(getWaste(rollerTubeID));
		BigDecimal rollerTubeQty = getRollerTubeQty();
		addMBLDBomDerived(rollerTubeID, rollerTubeQty, "Procesed with waste factor of: " + (rollerTubeQty.multiply(waste)));
		
		//TODO: Handle adding tube tape, bubble, packaging tape, masking tape, base bar stickers, tube selection
		//Tube selection: create static utility method in MtmUtils? Need to determine bending moment in tube centre.
		
		return true;
	}

	@Override
	public boolean deleteBomDerived() {
		// TODO Auto-generated method stub
		return true;
	}


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
	}
	
	/**
	 * Searches for all the BOM parts from product with m_product_id
	 * sorts them into ArrayList with a KeyNamePair array for each relevant
	 * m_parttype_id
	 * @param m_product_id
	 */
	private void populatePartTypes(int m_product_id) 
	{
		StringBuilder sql = new StringBuilder("SELECT mp.m_parttype_id, mp.m_product_id, mp.name ");
		sql.append("FROM m_product mp INNER JOIN m_product_bom mpb ");
		sql.append("ON mp.m_product_id = mpb.m_productbom_id ");
		sql.append("AND mpb.m_product_id = ");
		sql.append(m_product_id);
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
				/*else if(partType.equalsIgnoreCase(CONTROL_COMP))
				{
					controlType.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(BOTTOM_BAR))
				{
					bottomBar.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(NON_CONTROL_MECH))
				{
					TubularNCM.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(ROLLER_BRACKET))
				{
					rollerBracket.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(CHAIN_SAFE))
				{
					chainSafe.add(new KeyNamePair(productIDs[i], names[i]));
				}*/
				else if(partType.equalsIgnoreCase(CHAIN_ACC))
				{
					chainAcc.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(ROLLER_TUBE))
				{
					rollerTube.add(new KeyNamePair(productIDs[i], names[i]));
				}
				//TODO: Pick end cap to match colour of base bar.
				else if(partType.equalsIgnoreCase(END_CAP))
				{
					endCap.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(LIFT_SPRING))
				{
					liftSpring.add(new KeyNamePair(productIDs[i], names[i]));
				}
				
			}
			
			
		}
	
	/*
	 * Some business logic to consider:
	 * Tube sizes vary in relation to finished size. This can hard coded then made a setting
	 * Each tube requires different mechs - the tube size for each mech will be in it's description field
	 * so it can easily found.
	 * Each bottom bar requires different end caps.
	 * Rollers with chains get 'chain accessory' part types.
	 */
		
		
		/*Get the type part that the 1st partid is.
		 * 
		 * Iterate through the 3 arrays while where still on the same partID
		 * Create a keynamePair from the name and productid.
		 * Append/make the appropriate ListBox items from the keynamepair
		 * when the partID changes, repeat from beginning.
		 * 
		 * 
		 * DB.getRowSet(sql) or getKeynamePair()?
		*Get BOM: SELECT mp.m_parttype_id, mp.m_product_id FROM m_product mp INNER JOIN m_product_bom mpb ON mp.m_product_id = mpb.m_productbom_id AND mpb.m_product_id = '1000010';
		*or SELECT mp.name, mp.m_product_id, mp.m_parttype_id FROM m_product mp INNER JOIN m_product_bom mpb ON mp.m_product_id = mpb.m_productbom_id AND mpb.m_product_id = '1000010';
		*Figure out which BOM items are: Chain, control, roller bracket, bottom bar, TNCM (tubular non control mech)
		*/
	
	
	private void setValueProductID() {
		
		/*Following need to be determined
		 * 
	private int nonControlBracketID = 0;
	private int controlID = 0;
	private int nonControlID = 0;
	private int chainSafeID = 0;
	private int rollerTubeID = 0;
	private int endCapID = 0;
	
		 */
		//Add the chain accessories
		
		/*
		if(isChainControl)
		{
			for (KeyNamePair ca : chainAcc) {
		        StringBuilder sql = new StringBuilder("SELECT m_product_bom_id ");
		        sql.append("FROM m_product_bom ");
		        sql.append("WHERE m_product_id = ");
		        sql.append(m_product_id);
		        sql.append("AND m_productbom_id = ");
		        sql.append(ca.getID());
		        
		        int m_product_bom_id = DB.getSQLValue(trxName, sql.toString());
				MProductBOM mProductBom = new MProductBOM(Env.getCtx(), m_product_bom_id, trxName);
				BigDecimal qty = mProductBom.getBOMQty();
				
				MBLDBomDerived mBomDerived = new MBLDBomDerived(Env.getCtx(), 0, trxName);
				mBomDerived.setbld_mtom_item_line_ID(mtom_item_line_id);
				mBomDerived.setM_Product_ID(ca.getKey());
				mBomDerived.setQty(qty);
				mBomDerived.saveEx();
				
		        }
		}*/
		
		
		//Resolve controlBracketID
		//TODO: Protocol must be bracket description contains: control(literal), colour(instance), type(instance) - dual, single extension etc.
		/*
			controlID = resolveComponents(controlType, blindControlIns);//Resolve controlID
			nonControlID = resolveComponents(TubularNCM, tNCMIns);//Resolve nonControlID
			//TODO: May need more logic to determine brackets as below 2 statement assign the same value.
			nonControlBracketID = resolveComponents(rollerBracket, rollerBracketColIns, rollerBracketIns);//Resolve nonControlBracketID
			controlBracketID = resolveComponents(rollerBracket, rollerBracketColIns, rollerBracketIns);//
			chainSafeID = resolveComponents(chainSafe, chainSafeIns);//Resolve chainSafeID
		*/
			
			/**
			 * Set roller tube ID based on blind width only
			 * 
			*/
			String tubeDuty = "";
			if(wide > 2500)tubeDuty = "HD";
			else {
				tubeDuty = "MD";
			}
			
			rollerTubeID = resolveComponents(rollerTube, tubeDuty);
			if(rollerTubeID < 1)
			{
				MProduct mProduct = new MProduct(Env.getCtx(), m_product_id , trxName);
				StringBuilder msg = new StringBuilder("A roller tube could not be found in the BOM for MProduct: ");
				msg.append(mProduct.getName());
				msg.append(" With MProductID: " + m_product_id);
				msg.append(". Check the BOM for a 'Roller tube' Part Type.");
				msg.append(" Also ensure that the Description field in the Product window of the tube contains 'MD' or 'HD' to denote that");
				msg.append(" the tube is medium duty or heavy duty. FATAL ERROR CAN'T CONTINUE!");
				throw new AdempiereUserError(msg.toString());
			}
			 
			 /* TODO: Resolve rollerTubeID via calculation
			 * There's a method MtmUtils.getBendingMoment(int length, int fabricProductId, int basebarProductId)
			 *that returns the bending moment (BM) in kg-metres.
			 *the tube to select will be the smallest tube cross section that has a rated BM that exceeds
			 *the computed BM for the blind being made.
			 *
			 * TODO: Handle blinds to be on the same size tube if they're all in the same room.
			 */
			//TODO: Resolve endCapID
	}
	/**
	 * Note: There's 2 columns in the m_product_bom_id table
	 * that both look like m_product_bom_id
	 * @param mProductBomid
	 * @return
	 */
	private BigDecimal getBomQty(int mProductBomid) { 
		BigDecimal qty = BigDecimal.ZERO;
		if(mProductBomid == rollerTubeID)
			{
				qty = getRollerTubeQty();
				if(qty != BigDecimal.ZERO)
				{
					return qty;
				}
				
			} else if(mProductBomid == fabricID)
			{
				qty = getFabricQty();//TODO: Change fabricQty to getFabricQty() as fabricQty is set in getCuts() which is poor coding.
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
			}
		
		
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
		
		return bigQty;
		
	}
	
	private void addMBLDBomDerived(int mProductId, BigDecimal qty, String description) {
		log.warning("---------In addMBLDBomDerived(int mProductId, BigDecimal qty, String description) with mProductId: " + mProductId + " qty:" + qty + " description: " + description);
		if(mProductId > 0 && qty.compareTo(BigDecimal.ZERO) > 0)
		{
			MBLDBomDerived mBomDerived = new MBLDBomDerived(Env.getCtx(), 0, trxName);
			mBomDerived.setbld_mtom_item_line_ID(mtom_item_line_id);
			mBomDerived.setM_Product_ID(mProductId);
			qty.setScale(2, BigDecimal.ROUND_CEILING);
			mBomDerived.setQty(qty);
			if(description != null)mBomDerived.setDescription(description);
			mBomDerived.saveEx();
		}
	}
	
	/**
	 * 
	 * @param parts
	 * @param instanceParse
	 * @return
	 */
	private int resolveComponents(ArrayList<KeyNamePair> parts, String instanceParse) {
	
		int part_ID = 0;
		String partDescription = "";
		for(KeyNamePair partPair : parts) 
		{
			part_ID = partPair.getKey();
			MProduct productToExamine = new MProduct(Env.getCtx(), part_ID, trxName);
			partDescription = productToExamine.getDescription().toLowerCase();
			//TODO: Handle different colours in brackets and mechs.
			if(instanceParse!=null)
			{
			System.out.println("partDescription.contains: " + partDescription + " contains " + instanceParse.toLowerCase() + " is " + partDescription.contains(instanceParse.toLowerCase()));
				if(partDescription.contains(instanceParse.toLowerCase()))
				{
					System.out.println("----------------Part found description: " + partDescription + " Part_id: " + part_ID + " From the instance parse of: " + instanceParse.toLowerCase());
					break;//part_ID should now contain the right MProductID
				}
			}
		}
		if(part_ID == 0)//The part couldn't be found.
	{
		log.warning(instanceParse + partDescription + "---------------Could not resolve component from Attribute Instance.. Please ensure that products are set up with descriptions that match Attributes.");
	}
		return part_ID;
	}
	
	/*
	private int resolveComponents(ArrayList<KeyNamePair> parts, String instanceParse, String instanceParse2) {
		
		int part_ID = 0;
		
		for(KeyNamePair partPair : parts) 
		{
			part_ID = partPair.getKey();
			MProduct productToExamine = new MProduct(Env.getCtx(), part_ID, trxName);
			String partDescription = productToExamine.getDescription().toLowerCase();
			
		if(instanceParse != null && instanceParse2 != null)
		  {
			if(partDescription.contains(instanceParse.toLowerCase()) && partDescription.contains(instanceParse2.toLowerCase()))
			{
				break;//part_ID should now contain the right MProductID
			}
		  }
		}
		if(part_ID == 0)//The bracket couldn't be found.
	{
		log.warning(instanceParse + "---------------Could not resolve control from Attribute Instance.. Please ensure that products are set up with descriptions that match Attributes.");
	}
		return part_ID;
	}*/
	
	/**
	 * Sets fields from parts that users can select from the part dialog selection.
	 */
	//TODO: Modify so all parts are added to BOMDerived
	private void setUserSelectedPartIds(){
		
		MBLDLineProductInstance[] mBLDLineProductInstance = getMBLDLineProductInstance(); 
		for(int i = 0; i < mBLDLineProductInstance.length; i++)
		{
				int mProductId = mBLDLineProductInstance[i].getM_Product_ID();
				int mPartTypeID = mBLDLineProductInstance[i].getBLD_Product_PartType_ID();
				MBLDProductPartType mBLDProductPartType  = new MBLDProductPartType(Env.getCtx(), mPartTypeID, null);
				int xMPartTypeID = mBLDProductPartType.getM_PartTypeID();
				X_M_PartType mPartType  = new X_M_PartType (Env.getCtx(), xMPartTypeID , null);
				if(mPartType.getName().equals(CONTROL_COMP))
				{
					controlID = mProductId;
				}
				if(mPartType.getName().equals(NON_CONTROL_MECH))
				{
					nonControlID = mProductId;
				}
				if(mPartType.getName().equals(ROLLER_BRACKET))
				{
					nonControlBracketID = mProductId;
				}
				if(mPartType.getName().equals(ROLLER_BRACKET))
				{
					controlBracketID = mProductId;
				}
				if(mPartType.getName().equals(FABRIC))
				{
					fabricID = mProductId;
				}
		}
		
		/*
		patternString = "[0-9][0-9][0-9][0-9][0-9][0-9][0-9]";
	    pattern = Pattern.compile(patternString);
		if(mtmInstanceParts != null)//Add the parts stored in the bldmtomlineiem instance_string column
	{
		String[] products = getInstanceArray();
		 if(products != null)
		
		for(int i = 0; i < products.length; i++)
		{
			Matcher matcher = pattern.matcher(products[i]);
			boolean matches = matcher.matches();
			if(matches)
			{
				int mProductId = Integer.parseInt(products[i]);
				BigDecimal qty = getBomQty(mProductId);
				addMBLDBomDerived(mProductId, qty, trxName);
			}
		}
	 }*/
    }
	
	private void addBomDerivedLines(int partProduct_id, String description){
		if(partProduct_id != 0)
		{
			addMBLDBomDerived(partProduct_id, getBomQty(partProduct_id), description);
		}
		
	}
	
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
		
	}

	
	/* private String[] getInstanceArray() {
		 
		 if(mtmInstanceParts!=null && mtmInstanceParts.toString().contains("_")) 
		 {
			//Split the value of the mtm_attribute column, first value id fabric, second is chain, third is bottom bar
			String[] products = mtmInstanceParts.toString().split("_");
			return products;
		 } 
		 else return null;

	 } */
	 
	/**Deprecate / delete 
	 
	 private void setInstanceFields() {	
	 
	 String[] products = getInstanceArray();
	 if(products != null)
	 {
		for(int s = 0; s < products.length; s++)//Set the Productids from the bldmtomlineiem instance_string column
		{
			int mProductId = Integer.parseInt(products[s]);
			if(s == 0)fabricID = mProductId;
			//if(s == 1)
			if(s == 2)bottomBarID = mProductId;
		}
	}
	 }*/
	 
	 public ArrayList <Integer> getHeadRailComps() {
	 ArrayList <Integer> headRailComps = new ArrayList <Integer>();
		headRailComps.add(nonControlBracketID);
		headRailComps.add(controlBracketID);
		headRailComps.add(controlID);
		headRailComps.add(nonControlID);
		log.warning("--------In RollerBlind.getHeadRailComps.");
		log.warning("--------Headrail comps are: " + headRailComps.toString());
		return headRailComps;
	 }
	 
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
	 }
	 
	 public BigDecimal getBottomBarCut() {
		 BigDecimal fabricWidth = getFabricWidth();
		 log.warning("-------About to go intoMtmUtils.attributePreCheck()");
		 MtmUtils.attributePreCheck(MtmUtils.MTM_BOTTOM_BAR_DEDUCTION);
		//Note: MTM_BOTTOM_BAR_DEDUCTION is part of parent product non instance attribute.
		 log.warning("About to call static method MBLDMtomCuts.getDeduction with deduction type: " + MtmUtils.MTM_BOTTOM_BAR_DEDUCTION);
		 fabricWidth = fabricWidth.subtract(MBLDMtomCuts.getDeduction(m_product_id, MtmUtils.MTM_BOTTOM_BAR_DEDUCTION, trxName));
		 return fabricWidth;
	 }
	 
	 public BigDecimal getFabricWidth() {
		 BigDecimal fabWidth = getRollerTubeCut(wide);
		 log.warning("-------About to go intoMtmUtils.attributePreCheck()");
		 MtmUtils.attributePreCheck(MtmUtils.MTM_FABRIC_DEDUCTION);
		 log.warning("About to call static method MBLDMtomCuts.getDeduction with deduction type: " + MtmUtils.MTM_FABRIC_DEDUCTION);
		 /* 18/8/18 changed location of MTM_FABRIC_DEDUCTION from fabric to parent product (the roller blind itself)
		  * as a non instance attribute.*/
		 fabWidth = fabWidth.subtract(MBLDMtomCuts.getDeduction(m_product_id, MtmUtils.MTM_FABRIC_DEDUCTION,trxName));
		 return fabWidth;
	 }	

	 public BigDecimal getFabricDrop() {
		 BigDecimal bigHigh = new BigDecimal(high);
		 log.warning("-------About to go intoMtmUtils.attributePreCheck()");
		 MtmUtils.attributePreCheck(MtmUtils.MTM_FABRIC_ADDITION);
		 log.warning("About to call static method MBLDMtomCuts.getDeduction with deduction type: " + MtmUtils.MTM_FABRIC_ADDITION);
		 //Note: MTM_FABRIC_ADDITION is part of parent product non instance attribute.
		 bigHigh = bigHigh.add(MBLDMtomCuts.getDeduction(m_product_id, MtmUtils.MTM_FABRIC_ADDITION, trxName));
		 return  bigHigh;
	 }
	 
	 public BigDecimal getFabricQty() {
		 	BigDecimal fwidth = getFabricWidth().divide(oneThousand);
			BigDecimal fdrop = getFabricDrop().divide(oneThousand);
			BigDecimal qty = fwidth.multiply(fdrop);
			BigDecimal waste = new BigDecimal(getWaste(fabricID));
			qty = ((qty.divide(oneHundred).multiply(waste).add(qty)));
			qty.setScale(4, BigDecimal.ROUND_CEILING);
			return qty;
	 }
	 
	 public BigDecimal getRollerTubeQty() {
		 	BigDecimal qty = getRollerTubeCut(wide);
			BigDecimal waste = new BigDecimal(getWaste(rollerTubeID));
			qty = ((qty.divide(oneHundred).multiply(waste).add(qty)));
			return qty;
	 }
	 
	 public BigDecimal getBottomBarQty() {
		 	BigDecimal qty = getBottomBarCut();
			BigDecimal waste = new BigDecimal(getWaste(bottomBarID));
			qty = ((qty.divide(oneHundred).multiply(waste).add(qty)));
			return qty;
	 }
	 
	 /**
	  * This method gets the BOM line product IDs and was part of a refactor 
	  * to allow the user to change BOM line product types.
	  * @param partType
	  * @return
	  */
	 public int getBomProductID(String partType) {
		 MBLDBomDerived[] bomDerived = mBLDMtomItemLine.getBomDerivedLines(Env.getCtx(), mBLDMtomItemLine.getbld_mtom_item_line_ID());
		
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
				if(partName.equalsIgnoreCase(partType)) return productID; 
			 }
			 else
			 {
				 log.warning("Could not get a part name for partType: " + partType);
			 }
			 
		 }
		 return 0;
	 }
	 
	 private RowSet getmAttributeInstances(int mAttributeSetinstanceID)
		{
			StringBuilder sql = new StringBuilder("SELECT m_attribute_id, value, m_attributevalue_id ");
			sql.append("FROM m_attributeinstance mai ");
			sql.append(" WHERE mai.m_attributesetinstance_id = ");
			sql.append(mAttributeSetinstanceID);
			
			RowSet rowset = DB.getRowSet(sql.toString());
			return rowset;
		}
	 
	 private int getLiftSpring() {
		/*
		 * How heavy is the blind?
		 * What is its rotation?
		 * Check liftSpring array - is it empty? If not, then loop through array and find the lightest spring that
		 * will do the job.
		 * Only specify a spring if the BOM has one that has a range that the blind weight falls into,
		 * IE no spring unless blind is heavy enough for the available springs
		 */
		 BigDecimal weight = MtmUtils.getHangingMass(wide, high, fabricID, bottomBarID, trxName);
		 String rotation = MtmUtils.getRotation(rollTypeIns, controlSide);
		 if(rotation == null || rotation == "")
			 {
			 	log.warning("Could not determine rotation of ID: " + mtom_item_line_id + ", check control side.");
			 	return 0;
			 }
		 
		 if(liftSpring.isEmpty())return 0;
		 for(KeyNamePair spring : liftSpring) 
		 {
			 int springID = spring.getKey();
			 String lowerLimit = (String) MtmUtils.getMattributeInstanceValue(springID, "Lift Spring capacity lower", trxName);
			 String upperLimit = (String) MtmUtils.getMattributeInstanceValue(springID, "Lift Spring capacity upper", trxName);
			 String springRotation = (String) MtmUtils.getMattributeInstanceValue(springID, "Lift Spring rotation", trxName);
		     BigDecimal lower = new  BigDecimal(lowerLimit); 
		     BigDecimal upper = new  BigDecimal(upperLimit);
		     int retval = weight.compareTo(lower);
		     int retval2 = weight.compareTo(upper);
		     if(retval > 0 && retval2 <0)//The blind weight is in the correct range of the spring
		     {
		    	 if(springRotation.equalsIgnoreCase(rotation))
		    	 {
		    		return springID; 
		    	 }
		    	 
		     }
		 }
		 
		 return 0;
		 
	 }
	 private void deductionPreCheck(String deductionType) {
		 StringBuilder sql = new StringBuilder();
		 sql.append("SELECT m_attribute_id FROM m_attribute WHERE m_attribute.name = '");
		 sql.append(deductionType);
		 sql.append("'");
		 
		 if(getRowCount(sql.toString()) > 1)
			 {
			 StringBuilder msg = new StringBuilder("There is more than one Attribute for the deduction type: ");
			 msg.append(deductionType);
			 msg.append(". Check the Attributes that match or are close to ");
			 msg.append(deductionType);
			 msg.append(" and reconfigure.");
			 throw new AdempiereUserError(msg.toString());
			 }
		 
		
	 }
	 
	 private int getRowCount(String sql) {
		 RowSet rowset = DB.getRowSet(sql.toString());
		 int rowCount = 0;
		 try
		 {
			 while(rowset.next() )
				{
				 rowCount++;
				}
		 }
		 catch (SQLException e)
		 {
			 log.severe("Could not get values for query" + e.getMessage());
				e.printStackTrace();
		 } 
		 return rowCount;
	 }
	 
	 /**
		 * Searches for all the BOM parts from product with m_product_id
		 * sorts them into ArrayList with a KeyNamePair array for each relevant
		 * m_parttype_id
		 * @param m_product_id
		 */
		private void setPartIDs(int m_product_id) 
		{
			StringBuilder sql = new StringBuilder("SELECT mp.m_parttype_id, mp.m_product_id, mp.name ");
			sql.append("FROM m_product mp INNER JOIN m_product_bom mpb ");
			sql.append("ON mp.m_product_id = mpb.m_productbom_id ");
			sql.append("AND mpb.m_product_id = ");
			sql.append(m_product_id);
			sql.append(" ORDER BY mp.m_parttype_id");
			
			/*Get the Parttypes for this product
			 * get the instances for this lineID
			 * Iterate through and set the fields for the part IDS
			 * 	private int nonControlBracketID = 0;
			 *	private int controlID = 0;
				private int nonControlID = 0;
				private int chainSafeID = 0;
				private int rollerTubeID = 0;
				private int endCapID = 0;
			 * 
			 * 
			 */
			
		
			
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
			
		
		
			
				for(int i = 0; i < parts.length; i++) {
		
					X_M_PartType mPartType = new X_M_PartType(null, parts[i], null);
					String partType = (String) mPartType.get_Value("name");
					
					if(partType == null)
					{
						miscItem.add(new KeyNamePair(productIDs[i], names[i]));;
					}
					else if(partType.equalsIgnoreCase(CONTROL_COMP))
					{
						controlType.add(new KeyNamePair(productIDs[i], names[i]));
					}
					else if(partType.equalsIgnoreCase(BOTTOM_BAR))
					{
						bottomBar.add(new KeyNamePair(productIDs[i], names[i]));
					}
					else if(partType.equalsIgnoreCase(NON_CONTROL_MECH))
					{
						TubularNCM.add(new KeyNamePair(productIDs[i], names[i]));
					}
					else if(partType.equalsIgnoreCase(ROLLER_BRACKET))
					{
						rollerBracket.add(new KeyNamePair(productIDs[i], names[i]));
					}
					else if(partType.equalsIgnoreCase(CHAIN_SAFE))
					{
						chainSafe.add(new KeyNamePair(productIDs[i], names[i]));
					}
					else if(partType.equalsIgnoreCase(CHAIN_ACC))
					{
						chainAcc.add(new KeyNamePair(productIDs[i], names[i]));
					}
					else if(partType.equalsIgnoreCase(ROLLER_TUBE))
					{
						rollerTube.add(new KeyNamePair(productIDs[i], names[i]));
					}
					else if(partType.equalsIgnoreCase(END_CAP))
					{
						endCap.add(new KeyNamePair(productIDs[i], names[i]));
					}
					else if(partType.equalsIgnoreCase(LIFT_SPRING))
					{
						liftSpring.add(new KeyNamePair(productIDs[i], names[i]));
					}
					
				}
				
				
	}
		
private MBLDLineProductInstance[] getMBLDLineProductInstance() {
	int bld_Line_ProductSetInstance_ID = mBLDMtomItemLine.getBld_Line_ProductSetInstance_ID();
	MBLDLineProductInstance[] mBLDLineProductInstance = MBLDProductPartType.getmBLDLineProductInstance(bld_Line_ProductSetInstance_ID, trxName); 
	return mBLDLineProductInstance;
}

private void addMtmInstancePartsToBomDerived() {
	MBLDLineProductInstance[] mBLDLineProductInstance = getMBLDLineProductInstance();
	
	for(int i = 0; i < mBLDLineProductInstance.length; i++)
	{
		int mProductId = mBLDLineProductInstance[i].getM_Product_ID();
		BigDecimal qty = getBomQty(mProductId);
		addMBLDBomDerived(mProductId, qty, trxName);
	}
}
/**
 * //TODO: Possibly should be a boolean field in the m_product table ischaincontrol
 * @param controlProductID
 */

public void setChainControl(int controlProductID) {
	if(controlProductID > 0)
	{
		MProduct controlProduct = new MProduct(Env.getCtx(), controlProductID, null);
		String controlName = controlProduct.getName();
		if(controlName.contains("chain"))
		{
			isChainControl = true;
		}
		else
		{
			isChainControl = false;
		}
	}
}

}
