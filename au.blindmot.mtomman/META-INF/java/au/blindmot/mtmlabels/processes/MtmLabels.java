package au.blindmot.mtmlabels.processes;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import javax.sql.RowSet;

import org.apache.commons.lang.StringUtils;
import org.compiere.model.MBPartner;
import org.compiere.model.MProduct;
import org.compiere.model.MProductCategory;
import org.compiere.model.X_M_PartType;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

import au.blindmot.model.MBLDBomDerived;
import au.blindmot.model.MBLDMtomCuts;
import au.blindmot.model.MBLDMtomItemLine;
import au.blindmot.model.MBLDMtomProduction;
import au.blindmot.utils.MtmUtils;

public class MtmLabels extends SvrProcess{
	
	public static final String START_FORMAT = "^XA";
	/*^PRp,s,b 
	 * p=print speed, s = slew speed, b=backfeedspeed
	 */
	public static final String PRINT_RATE = "^PRB,D";
	//^A0r,h,w	scalable alphanumeric font rotation,height,width
	public static final String SCALABLE_FONT_ROTATION = "^A0N";
	public static final String MEDIA_DARKNESS = "^MD20";
	public static final String LABEL_HOME = "^LH0,0";
	public static final String FIELD_ORIGIN = "^FO";
	public static final String CHANGE_INTERNAT_FONT = "^CI";
	public static final String FIELD_SEPARATOR = "^FS";
	public static final String BARCODE_DEFAULTS = "^BY2,2.7,13";
	public static final String BARCODE_I20F5 = "^B2N,130,Y,N,N";
	public static final String BARCODE_30F9 = "^B3N,N,40,Y,N";
	public static final String FORMAT_DATA = "^FD";
	public static final String DOTS_PER_MM = "^JMA"; 
	public static final String SERIAL_START = "^SN";
	public static final String SERIAL_END = ",N,Y";
	public static final String PRINT_QUALITY = "^PQ1,0,1,Y";
	public static final String END_FORMAT = "^XZ";
	public static final String FABRIC = "Fabric";
	private int bLDMtomProduction_ID = 0;
	private boolean finishedItemOnly = false;
	private int finishedLabelCopies = 2;
	private int labelCopies = 1;
	private StringBuilder productOutput = new StringBuilder();
	private StringBuilder fabricOutput = new StringBuilder();
	private StringBuilder cutItemOutput = new StringBuilder();
	/*
	 * (non-Javadoc)
	 * @see org.compiere.process.SvrProcess#prepare()
	 */
	

	@Override
	protected void prepare() {
		
		log.warning("----------In prepare () au.blindmot.mtmlabels.processes.MtmLabels");
		ProcessInfoParameter[] paras = getParameter();
		for(ProcessInfoParameter para : paras)
		{
			
			String paraName = para.getParameterName();
			if(paraName.equalsIgnoreCase("bld_mtom_production_ID"))
				bLDMtomProduction_ID = para.getParameterAsInt();
			else if(paraName.equalsIgnoreCase("FinishedItemOnly"))
				finishedItemOnly = para.getParameterAsBoolean(); 
			else if(paraName.equalsIgnoreCase("LabelCopies"))
			{
				int copies = para.getParameterAsInt();
				if(copies == 0)
				{
					labelCopies = 1;//Default to at least 1.
				}
				else
				{
					labelCopies = copies;
				}
			}
			else if(paraName.equalsIgnoreCase("FinishedLabelCopies"))
			{
				finishedLabelCopies = para.getParameterAsInt();
			}
				
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + paraName);
				
		}
		
	}
//TODO: create labels that have the output grouped by: End product, fabric, other cuts
	/*Tidy up DoIt() -> create method to print the end product and remove code from doIt()
	 * Create 3 StringBuilder variables EndProduct, fabric, cutItems and append to final label at end
	 * 
	 */
	
	@Override
	protected String doIt() throws Exception {
		
		log.warning("----------In doIt() au.blindmot.mtmlabels.processes.MtmLabels");
		// TODO Auto-generated method stub
		/*What we want to do is:
		 * Get the mtmProductionId
		 * Get the Mtom_item_lines from the productionID
		 * Create label based on the MTMlINE info
		 * FIELDS:
		 * Barcode
		 * Date production created
		 * Client name
		 * Order description
		 * Fabric 
		 * location
		 * finished size
		 * 
		 * ^XA^LH0,0^PRB,D^MD20^JMA^FS^BY2,2.7,13^FO506,20^B2N,132,Y,N,N^SN0016593650060001,N,Y^FS^FO20,20^A0N,28,37^CI13^FD14/9/2017^FS^FO220,20^A0N,41,48^CI13^FD16593A/006^FS^FO20,55^A0N,30,30^CI13^FDShahzad, Anwar^FS^FO20,95^A0N,30,30^CI13^FDExtra rollers^FS^FO20,135^A0N,30,30^CI13^FDROLL LeReve TRANS Marble^FS^FO20,175^A0N,30,30^CI13^FDWIR^FS^FO300,175^A0N,30,30^CI13^FDW:848 H:900^PQ1,0,1,Y^XZ
		 */
		if(bLDMtomProduction_ID == 0)
		{
			throw new AdempiereUserError("No MTM production (bLDMtomProduction_ID ==0) to create labels for. Is this an unsaved or new record?"); 
		}
		
		MBLDMtomProduction mBLDMtomProduction = new MBLDMtomProduction(getCtx(), bLDMtomProduction_ID, get_TrxName());
		MBLDMtomItemLine[] lines = mBLDMtomProduction.getLines();
		StringBuilder outputStringBuilder = new StringBuilder();
		
		for(int numCopies = 0; numCopies < labelCopies; numCopies++)
		{
			for(int i = 0; i < lines.length; i++)
			{
				StringBuilder label = new StringBuilder();
				StringBuilder productToAdd = getFinishedItem(lines[i], mBLDMtomProduction);
				
				//Add the finished item label as many times as required.
				for(int a = 0; a < finishedLabelCopies; a++)
				{
					productOutput.append(productToAdd);
				}
				/*
				 *Do other items only if finishedItemOnly is false
				 */
				
					MBLDMtomCuts[] cuts = lines[i].getCutLines(getCtx(), lines[i].getbld_mtom_item_line_ID());
					//boolean fabricHasBeenCut = false;
					
					for(int x =0; x < cuts.length; x++)
					{
						boolean isItFabric = isFabric(cuts[x].getM_Product_ID());
						
						
						if(!finishedItemOnly && isItFabric)//If it's fabric skip if already cut -> functionality removed, delete after testing.
						{
							StringBuilder fabric = addCuts(cuts[x], lines[i], mBLDMtomProduction, true);
							fabricOutput.append(fabric);
							//fabricHasBeenCut = true;
						}
						
						if(!finishedItemOnly && !isItFabric)
						{
							StringBuilder otherCut = addCuts(cuts[x], lines[i], mBLDMtomProduction, false);
							cutItemOutput.append(otherCut);
						}
						isItFabric = false;
					}
				
			}
		}
		
		String filenameForDownload = mBLDMtomProduction.getDocumentNo() + ".buz";
		File tempFile = new File(filenameForDownload);
		FileWriter fw = new FileWriter(tempFile);
		outputStringBuilder.append(productOutput);
		outputStringBuilder.append(fabricOutput);
		outputStringBuilder.append(cutItemOutput);
		fw.write(outputStringBuilder.toString());
		processUI.download(tempFile);
	
		tempFile.deleteOnExit();
		fw.flush();
		fw.close();
		log.warning("----------Finished au.blindmot.mtmlabels.processes.MtmLabels");
		return null;
	}
	
	private String addBarcode(String barcode) {
		StringBuilder bc = new StringBuilder();
		bc.append(FIELD_ORIGIN + "450,20");
		bc.append(BARCODE_30F9);
		bc.append(SERIAL_START);
		if(bc != null)bc.append(barcode);
		bc.append(SERIAL_END);
		bc.append(FIELD_SEPARATOR);
		return bc.toString();
	}
	
	private String addCutBarcode(String cutBarcode) {
		StringBuilder bc = new StringBuilder();
		bc.append(FIELD_ORIGIN + "250,75");
		bc.append(BARCODE_30F9);
		bc.append(SERIAL_START);
		if(bc != null)bc.append(cutBarcode);
		bc.append(SERIAL_END);
		bc.append(FIELD_SEPARATOR);
		return bc.toString();
	}
	
	private String addProductionDate(MBLDMtomItemLine line) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(line.getCreated());
		StringBuilder date = new StringBuilder();
		date.append(FIELD_ORIGIN + "20,20");
		date.append(SCALABLE_FONT_ROTATION + ",28,37");
		date.append(CHANGE_INTERNAT_FONT + "13");
		date.append(FORMAT_DATA);
		date.append(cal.get(Calendar.DATE) + "/");
		int month = cal.get(Calendar.MONTH) +1;//The first month of the year in the Gregorian and Julian calendars is JANUARY which is 0;
		date.append(month + "/");
		date.append(cal.get(Calendar.YEAR));
		date.append(FIELD_SEPARATOR);
		return date.toString();
		
	}
	
	private String addOrderInfo(int lineNo, String string) {
		StringBuilder orderInfo = new StringBuilder();
		orderInfo.append(FIELD_ORIGIN + "200,20");
		orderInfo.append(SCALABLE_FONT_ROTATION + "35,35");
		orderInfo.append(CHANGE_INTERNAT_FONT + "13");
		orderInfo.append(FORMAT_DATA);
		orderInfo.append(string + "->" + lineNo);
		orderInfo.append(FIELD_SEPARATOR);
		return orderInfo.toString();
	}
	
	private String addClientName(MBLDMtomProduction prod) {
		int bpID = prod.getC_BPartner_ID();
		if(bpID == 0) return null;
		MBPartner bPartner = new MBPartner(getCtx(), bpID, get_TrxName());
		String bpName = bPartner.getName();
		String bpName2 = bPartner.getName2();
		StringBuilder name = new StringBuilder();
		name.append(FIELD_ORIGIN + "20,55");
		name.append(SCALABLE_FONT_ROTATION + "30,30");
		name.append(CHANGE_INTERNAT_FONT + "13");
		name.append(FORMAT_DATA);
		if(bpName2 != null)name.append(bpName2 + ", ");
		if(bpName != null)name.append(bpName);
		name.append(FIELD_SEPARATOR);
		return name.toString();
	}
	
	private String addOrderDescription(String description) {
		String smallDesc = "";
		if(description != null && description.length() > 21) 
			{
				smallDesc = description.substring(0, 21);//Prevent description from overlapping other field,
			}
		else if(description != null)
		{
			smallDesc = description;
		}
		StringBuilder desc = new StringBuilder();
		desc.append(FIELD_ORIGIN + "20,95");
		desc.append(SCALABLE_FONT_ROTATION + "30,30");
		desc.append(CHANGE_INTERNAT_FONT + "13");
		desc.append(FORMAT_DATA);
		desc.append(smallDesc);
		desc.append(FIELD_SEPARATOR);
		return desc.toString();
		
	}
	
	private String addProductname(int mProductID, boolean isEndProd) {
		MProduct cutProduct = new MProduct(getCtx(), mProductID, get_TrxName());
		String cutName =  cutProduct.getName();
		StringBuilder nameToRet = new StringBuilder();
		String fieldOrigin = "20,135";
		if(isEndProd) fieldOrigin ="320,95";
		nameToRet.append(FIELD_ORIGIN + fieldOrigin);
		nameToRet.append(SCALABLE_FONT_ROTATION + "30,30");
		nameToRet.append(CHANGE_INTERNAT_FONT + "13");
		nameToRet.append(FORMAT_DATA);
		if(cutName != null)nameToRet.append(cutName);
		nameToRet.append(FIELD_SEPARATOR);
		return nameToRet.toString();
	} 
	
	private String addCutLength(MBLDMtomCuts cut)
	{
		BigDecimal cutLength =  cut.getLength();
		if(cutLength.compareTo(Env.ZERO) == 0) return "";
		String length = cutLength.toString();
		StringBuilder cutToRet = new StringBuilder();
		cutToRet.append(FIELD_ORIGIN + "300,175");
		cutToRet.append(SCALABLE_FONT_ROTATION + "30,30");
		cutToRet.append(CHANGE_INTERNAT_FONT + "13");
		cutToRet.append(FORMAT_DATA);
		if(length != null) cutToRet.append("Cut length: " + length + "mm");
		cutToRet.append(FIELD_SEPARATOR);
		
		return cutToRet.toString();
	}
	private String addCutWidth(MBLDMtomCuts cut)
	{
		BigDecimal cutWidth =  cut.getWidth();
		if(cutWidth.compareTo(Env.ZERO) == 0) return "";
		String width = cutWidth.toString();
		StringBuilder cutToRet = new StringBuilder();
		cutToRet.append(FIELD_ORIGIN + "20,175");
		cutToRet.append(SCALABLE_FONT_ROTATION + "30,30");
		cutToRet.append(CHANGE_INTERNAT_FONT + "13");
		cutToRet.append(FORMAT_DATA);
		if(width != null) cutToRet.append("Cut width: " + width + "mm");
		cutToRet.append(FIELD_SEPARATOR);
		return cutToRet.toString();
	}
	
	
	private String addFabric(MBLDMtomItemLine line) {
		
		int fabID = 0;
		MProduct bomProduct = null;
		MBLDBomDerived[] bomLines = line.getBomDerivedLines (getCtx(), line.get_ID());
		for(int y = 0; y < bomLines.length; y++)
		{
			int mProductBomID = bomLines[y].getM_Product_ID();
			bomProduct = new MProduct(Env.getCtx(), mProductBomID, null);
			X_M_PartType bomPartType = new X_M_PartType(Env.getCtx(), bomProduct.getM_PartType_ID(), null);
			if(bomPartType != null)
				{
					if(bomPartType.getName().equalsIgnoreCase(FABRIC))
					{
						fabID = mProductBomID;
						break;
					}
				}
				
		}
		if(fabID == 0)
			{
				throw new AdempiereUserError("There is no fabric/bottom bar specified for: " + line.getLine());
			}
		
		String name = bomProduct.getName();
		String desc = bomProduct.getDescription();
		
		List<Object> params = new  ArrayList<Object>();
		params.add(fabID);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT mai.value ");
		sql.append("FROM m_product mp ");
		sql.append("JOIN m_attributeinstance mai ON mai.m_attributesetinstance_id = mp.m_attributesetinstance_id ");
		sql.append("JOIN m_attribute ma ON ma.m_attribute_id = mai.m_attribute_id ");
		sql.append("AND mp.name = (SELECT name FROM m_product WHERE m_product_id = ?)");
		sql.append(" AND ma.name LIKE 'Fabric desc'");
		String fabType = DB.getSQLValueString(get_TrxName(), sql.toString(), params);
		
		StringBuilder fabricDesc = new StringBuilder("");
		if(name != null)fabricDesc.append(name + " ");
		if(desc != null)fabricDesc.append(desc + " ");
		if(fabType != null)fabricDesc.append(fabType);
		
		StringBuilder fab = new StringBuilder();
		fab.append(FIELD_ORIGIN + "20,135");
		fab.append(SCALABLE_FONT_ROTATION + "30,30");
		fab.append(CHANGE_INTERNAT_FONT + "13");
		fab.append(FORMAT_DATA);
		fab.append(fabricDesc);
		fab.append(FIELD_SEPARATOR);
		return fab.toString();
		
	}
	
	private String addLocation(MBLDMtomItemLine line) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT mai.value ");
		sql.append("FROM m_attributeinstance mai ");
		sql.append("JOIN m_attribute ma ON ma.m_attribute_id = mai.m_attribute_id ");
		sql.append("JOIN bld_mtom_item_line mtm ON mtm.attributesetinstance_id = mai.m_attributesetinstance_id ");
		sql.append("WHERE mtm.bld_mtom_item_line_id = ?");
		sql.append(" AND ma.name LIKE 'Location'");
		Integer mtmID = new Integer(line.getbld_mtom_item_line_ID());
		String location = DB.getSQLValueString(get_TrxName(), sql.toString(), (Object)mtmID);
		
		StringBuilder locToReturn = new StringBuilder();
		locToReturn.append(FIELD_ORIGIN + "20,175");
		locToReturn.append(SCALABLE_FONT_ROTATION + "30,30");
		locToReturn.append(CHANGE_INTERNAT_FONT + "13");
		locToReturn.append(FORMAT_DATA);
		if(location != null)locToReturn.append(location);
		locToReturn.append(FIELD_SEPARATOR);
		
		return locToReturn.toString();
	}
	
	private String addFinshedSize(MBLDMtomItemLine line) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ma.name, mai.value ");
		sql.append("FROM m_attributeinstance mai ");
		sql.append("JOIN m_attribute ma ON ma.m_attribute_id = mai.m_attribute_id ");
		sql.append("JOIN bld_mtom_item_line mtm ON mtm.attributesetinstance_id = mai.m_attributesetinstance_id ");
		sql.append("WHERE mtm.bld_mtom_item_line_id = ");
		sql.append(line.getbld_mtom_item_line_ID());
		sql.append(" AND (ma.name LIKE 'Drop' OR ma.name LIKE 'Width') ");
		sql.append("ORDER BY ma.name DESC");
		
		StringBuilder finSize = new StringBuilder();
		RowSet rowset = DB.getRowSet(sql.toString());
		try {
			while (rowset.next()) { 
				
				finSize.append(rowset.getString(1) + ":" + rowset.getString(2) + " ");
			}
		} catch (SQLException e) {
			log.severe("MtmLables.addFinshedSize() Could not get width or drop.");
			e.printStackTrace();
		}
		
		StringBuilder retSize = new StringBuilder();
		retSize.append(FIELD_ORIGIN + "450,175");
		retSize.append(SCALABLE_FONT_ROTATION + "30,30");
		retSize.append(CHANGE_INTERNAT_FONT + "13");
		retSize.append(FORMAT_DATA);
		if(finSize != null) retSize.append(finSize);
		retSize.append(FIELD_SEPARATOR);
		
		return retSize.toString();
		
	}
	
	private boolean isFabric(int mProductID) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT name ");
		sql.append("FROM m_parttype ");
		sql.append("WHERE m_parttype_id =(SELECT m_parttype_id FROM m_product WHERE m_product_id = ?");
		sql.append(")");
		
		String partType = DB.getSQLValueString(get_TrxName(), sql.toString(), mProductID);
		if(partType == null) return false;
		if(partType.equalsIgnoreCase("Fabric"))return true;
		return false;
		
	}
	
	/**
	 * Adds cut items to label output
	 * @param cut
	 * @param line
	 * @param mBLDMtomProduction
	 * @param isFabric
	 * @return
	 */
	private StringBuilder addCuts(MBLDMtomCuts cut, MBLDMtomItemLine line, MBLDMtomProduction mBLDMtomProduction, boolean isFabric) {
	
			StringBuilder cutItems = new StringBuilder();
			cutItems.append(START_FORMAT);
			cutItems.append(LABEL_HOME);
			cutItems.append(PRINT_RATE);
			cutItems.append(MEDIA_DARKNESS);
			cutItems.append(DOTS_PER_MM);
			cutItems.append(FIELD_SEPARATOR);
			cutItems.append(BARCODE_DEFAULTS);
			
			cutItems.append(addBarcode(MtmUtils.getBarcode(cut.get_Table_ID(), cut.getBLD_mtom_cuts_ID())));
			cutItems.append(addProductionDate(line));
			cutItems.append(addOrderInfo(line.getLine(),mBLDMtomProduction.getDocumentNo()));
			cutItems.append(addClientName(mBLDMtomProduction));
			cutItems.append(addOrderDescription(mBLDMtomProduction.getDescription()));
			boolean isEndProduct = false;
			if(cut.getM_Product_ID() == line.getM_Product_ID()) isEndProduct = true;//Redundant, should always be false here
			if(isFabric(cut.getM_Product_ID()))
			{
				cutItems.append(addFabric(line));
				cutItems.append(addCutWidth(cut));
			}
			else
			{
				cutItems.append(addProductname(cut.getM_Product_ID(), isEndProduct));
				String numbers = cut.getLength().toString();
				String[] cutLength  = StringUtils.split(numbers, ".");
				cutItems.append(addCutBarcode(cutLength[0]));
			}
			cutItems.append(addCutLength(cut));
			cutItems.append(addCutWidth(cut));
			cutItems.append(PRINT_QUALITY);
			cutItems.append(END_FORMAT);
			cutItems.append("\n");
			return cutItems; 
		
}
	
	private boolean isRollerBlind(int mProductID) {
		MProduct theProduct = new MProduct(Env.getCtx(), mProductID, get_TrxName());
		int productCategoryID = theProduct.getM_Product_Category_ID();
		MProductCategory prodCategory = new MProductCategory(getCtx(), productCategoryID, null);
		String name = prodCategory.getName();
		if((name.contains("roller") || name.contains("Roller")))return true;
		return false;
	}
	
	private StringBuilder getFinishedItem(MBLDMtomItemLine line, MBLDMtomProduction mBLDMtomPrdctn) {
		StringBuilder productLabel = new StringBuilder();
		
		productLabel.append(START_FORMAT);
		productLabel.append(LABEL_HOME);
		productLabel.append(PRINT_RATE);
		productLabel.append(MEDIA_DARKNESS);
		productLabel.append(DOTS_PER_MM);
		productLabel.append(FIELD_SEPARATOR);
		productLabel.append(BARCODE_DEFAULTS);
		
		productLabel.append(addBarcode(line.getbarcode()));
		productLabel.append(addProductionDate(line));
		productLabel.append(addOrderInfo(line.getLine(),mBLDMtomPrdctn.getDocumentNo()));
		productLabel.append(addClientName(mBLDMtomPrdctn));
		productLabel.append(addOrderDescription(mBLDMtomPrdctn.getDescription()));
		if(isRollerBlind(line.getM_Product_ID()))
		{
			productLabel.append(addFabric(line));
		}
		productLabel.append(addLocation(line));
		productLabel.append(addProductname(line.getM_Product_ID(), true));
		productLabel.append(addFinshedSize(line));
		productLabel.append(PRINT_QUALITY);
		productLabel.append(END_FORMAT);
		productLabel.append("\n");
		return productLabel;
		
		
	}
	

}
