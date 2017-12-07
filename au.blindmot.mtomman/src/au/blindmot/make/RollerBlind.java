package au.blindmot.make;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.compiere.model.MAttributeSetInstance;
import org.compiere.model.X_M_PartType;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

import au.blindmot.model.MBLDMtomItemLine;


public class RollerBlind extends MadeToMeasureProduct {
	
	private ArrayList<KeyNamePair> controlType = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> bottomBar = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> TubularNCM = new ArrayList<KeyNamePair>();
	private ArrayList<KeyNamePair> rollerBracket = new ArrayList<KeyNamePair>();
	private String blindControlIns = null;
	private String TNCMIns = null;
	private String rollerBracketIns = null;
	private String rollerBracketColIns = null;
	private String mechColIns = null;
	private String chainSafeIns = null;
	

	public RollerBlind (int product_id, int bld_mtom_item_line_id) {
		super(product_id, bld_mtom_item_line_id);
	}
	
	
	@Override
	public void interpretMattributeSetInstance(MAttributeSetInstance mAttribute) {
		// TODO Can this method be removed from Super class and here?

	}
	

	@Override
	public boolean getCuts() {
		return false;
		/*
		 * TODO: Check that KeyNamePair is the right thing to return?
		 * Perhaps a 2 dimensional array would be better? Would need to change here and in WindowFurnishing.java
		 */
	
	
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean createBomDerived() {//This is the first method called when processing
		/*
		 * What we wnat ot do here is add all the parts required to make the blind.
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
		 * 
		 * 
		 */
		
		populatePartTypes(m_product_id);
		AttributePair[] attributePair = getMAttributeSetInstance();
		if(mtmInstanceParts != null)
		{
			/*
			 * /TODO:There's part numbers ready to add if this isn't null.
			 */
		}
		
		String mInstance;
		String mInstanceValue;
		MBLDMtomItemLine mtmrolleritem = new MBLDMtomItemLine(Env.getCtx(), mtom_item_line_id, null);
		
		//Set fields based on the AttributePair[] contents.
		for(int i = 0; i < attributePair.length; i++)
		{
			mInstance = attributePair[i].getInstance();
			mInstanceValue = attributePair[i].getInstanceValue();
			
			if(mInstance.equalsIgnoreCase("Blind control side"))
			{
				if(mtmrolleritem.getControlSide().length()<1)
				mtmrolleritem.setControlSide(mInstanceValue);
			}
			else if(mInstance.equalsIgnoreCase("Roll type"))
			{
				if(mtmrolleritem.getRollType().length()<1)
				mtmrolleritem.setRollType(mInstanceValue);
			}
			else if(mInstance.equalsIgnoreCase("Location"))
			{
				if(mtmrolleritem.getLocation().length()<1)
				mtmrolleritem.setLocation(mInstanceValue);
			}
			else if(mInstance.equalsIgnoreCase("Blind control"))
			{
				if(!mInstanceValue.equalsIgnoreCase("Chain control"))
					blindControlIns = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase("Blind non control mech"))
			{
				TNCMIns = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase("Roller bracket"))
			{
				rollerBracketIns = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase("Mech colour"))
			{
				mechColIns = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase("Bracket colour"))
			{
				rollerBracketColIns = mInstanceValue;
			}
			else if(mInstance.equalsIgnoreCase("Chain safe"))
			{
				chainSafeIns = mInstanceValue;
			}
		}
		
		//TODO: match the fields to the results of populatePartTypes(int m_product_id)
		return false;
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean createProductionLine() {
		return false;
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean deleteBomDerived() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean deleteCuts() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean deleteProductionLine() {
		// TODO Auto-generated method stub
		return false;
	}
	
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
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		Integer parts[] = partIDs.toArray(new Integer[(partIDs.size())]);
		String names[] = mpName.toArray(new String[mpName.size()]);
		Integer productIDs[] = prodIDs.toArray(new Integer[(prodIDs.size())]);
		
		String controlComp = "Tubular blind control";
		String bBar = "Bottom bar";
		String TNCM = "TNCM";
		String rBracket = "Roller bracket";
		
	
		{
			for(int i = 0; i < parts.length; i++) {
	
				X_M_PartType mPartType = new X_M_PartType(null, parts[i], null);
				String partType = (String) mPartType.get_Value("name");
				if(partType == null)
				{
					break;
				}
				else if(partType.equalsIgnoreCase(controlComp))
				{
					controlType.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(bBar))
				{
					bottomBar.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(TNCM))
				{
					TubularNCM.add(new KeyNamePair(productIDs[i], names[i]));
				}
				else if(partType.equalsIgnoreCase(rBracket))
				{
					rollerBracket.add(new KeyNamePair(productIDs[i], names[i]));
				}
				}
			
			
		}
		
		
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
	
	}

}
