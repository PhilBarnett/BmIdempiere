package au.blindmot.mtmlabels;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;

import javax.sql.RowSet;

import org.compiere.model.MBPartner;
import org.compiere.model.MProduct;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import au.blindmot.model.MBLDMtomItemLine;
import au.blindmot.model.MBLDMtomProduction;

public class MtmLabels extends SvrProcess{
	
	public static final String START_FORMAT = "^XA";
	/*^PRp,s,b 
	 * p=print speed, s = slew speed, b=backfeedspeed
	 */
	public static final String PRINT_RATE = "^PRB,D";
	//^A0r,h,w	scalable alphanumeric font rotation,height,width
	public static final String SCALABLE_FONT_ROTATION = "^A0N,";
	public static final String MEDIA_DARKNESS = "^MD20";
	public static final String LABEL_HOME = "^LH0,0";
	public static final String FIELD_ORIGIN = "^FO";
	public static final String CHANGE_INTERNAT_FONT = "^CI";
	public static final String FIELD_SEPARATOR = "^FS";
	public static final String BARCODE_DEFAULTS = "^BY2,2.7,13";
	public static final String BARCODE_I20F5 = "^B2N,132Y,N,N";
	public static final String FORMAT_DATA = "^FD";
	public static final String DOTS_PER_MM = "^JMA"; 
	public static final String SERIAL_START = "^SN";
	public static final String SERIAL_END = ",N,Y";
	public static final String PRINT_QUALITY = "^PQ1,0,1,Y";
	public static final String END_FORMAT = "^XZ";
	private int bLDMtomProduction_ID = 0;
	
	

	@Override
	protected void prepare() {
		
		ProcessInfoParameter[] paras = getParameter();
		for(ProcessInfoParameter para : paras)
		{
			
			String paraName = para.getParameterName();
			if(paraName.equalsIgnoreCase("BLDMtomProduction_ID"))
				bLDMtomProduction_ID = para.getParameterAsInt();
			//else if(paraName.equalsIgnoreCase("check_box"))
				//ignoreExistingmtmProd = para.getParameterAsBoolean(); 
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + paraName);
				
		}
		
	}


	@Override
	protected String doIt() throws Exception {
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
		MBLDMtomProduction mBLDMtomProduction = new MBLDMtomProduction(getCtx(), bLDMtomProduction_ID, get_TrxName());
		MBLDMtomItemLine[] lines = mBLDMtomProduction.getLines();
		StringBuilder outputFile = new StringBuilder();
		StringBuilder label = new StringBuilder();
		for(int i = 0; i < lines.length; i++)
		{
			label.append(START_FORMAT);
			label.append(LABEL_HOME);
			label.append(PRINT_RATE);
			label.append(MEDIA_DARKNESS);
			label.append(DOTS_PER_MM);
			label.append(FIELD_SEPARATOR);
			label.append(BARCODE_DEFAULTS);
			
			label.append(addBarcode(lines[i].getbarcode()));
			label.append(addProductionDate(lines[i]));
			label.append(addOrderInfo(lines[i].getLine(),mBLDMtomProduction.getDocumentNo()));
			label.append(addClientName(mBLDMtomProduction));
			label.append(addOrderDescription(mBLDMtomProduction.getDescription()));
			label.append(addFabric(lines[i].getinstance_string()));
			label.append(addLocation(lines[i]));
			label.append(addFinshedSize(lines[i]));
			label.append(PRINT_QUALITY);
			label.append(END_FORMAT);
			label.append("\n");
			outputFile.append(label);
		}
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(mBLDMtomProduction.getDocumentNo()+".buz"));
			out.write(outputFile.toString());
		}
		catch (IOException e)
		{
			System.out.println("Exception: " + e.getMessage());
		}
		finally
		{
			out.close();
		}
		
		
		
		return null;
	}
	
	private String addBarcode(String barcode) {
		StringBuilder bc = new StringBuilder();
		bc.append(FIELD_ORIGIN + "506,20");
		bc.append(BARCODE_I20F5);
		bc.append(SERIAL_START);
		bc.append(barcode);
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
		date.append(cal.DATE + "/");
		date.append(cal.MONTH + "/");
		date.append(cal.YEAR);
		date.append(FIELD_SEPARATOR);
		return date.toString();
		
	}
	
	private String addOrderInfo(int lineNo, String string) {
		StringBuilder orderInfo = new StringBuilder();
		orderInfo.append(FIELD_ORIGIN + "220,20");
		orderInfo.append(SCALABLE_FONT_ROTATION + "41,48");
		orderInfo.append(CHANGE_INTERNAT_FONT + "13");
		orderInfo.append(FORMAT_DATA);
		orderInfo.append(string + "|" + lineNo);
		orderInfo.append(FIELD_SEPARATOR);
		return orderInfo.toString();
	}
	
	private String addClientName(MBLDMtomProduction prod) {
		int bpID = prod.getC_BPartner_ID();
		MBPartner bPartner = new MBPartner(getCtx(), bpID, get_TrxName());
		String bpName = bPartner.getName();
		String bpName2 = bPartner.getName2();
		StringBuilder name = new StringBuilder();
		name.append(FIELD_ORIGIN + "20,55");
		name.append(SCALABLE_FONT_ROTATION + "30,30");
		name.append(CHANGE_INTERNAT_FONT + "13");
		name.append(FORMAT_DATA);
		name.append(bpName2 + ", " + bpName);
		name.append(FIELD_SEPARATOR);
		return name.toString();
	}
	
	private String addOrderDescription(String description) {
		String smallDesc = description.substring(0, 12);
		StringBuilder desc = new StringBuilder();
		desc.append(FIELD_ORIGIN + "20,95");
		desc.append(SCALABLE_FONT_ROTATION + "30,30");
		desc.append(CHANGE_INTERNAT_FONT + "13");
		desc.append(FORMAT_DATA);
		desc.append(smallDesc);
		desc.append(FIELD_SEPARATOR);
		return desc.toString();
		
	}
	
	private String addFabric(String instanceString) {
		String fabric = instanceString.substring(0, 6);
		Integer fabID = new Integer(fabric);
		MProduct mProduct = new MProduct(getCtx(), fabID, get_TrxName());
		String name = mProduct.getName();
		String desc = mProduct.getDescription();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT mai.value ");
		sql.append("FROM m_product mp ");
		sql.append("JOIN m_attributeinstance mai ON mai.m_attributesetinstance_id = mp.m_attributesetinstance_id ");
		sql.append("JOIN m_attribute ma ON ma.m_attribute_id = mai.m_attribute_id ");
		sql.append("AND mp.name = (SELECT name FROM m_product WHERE m_product_id = ?");
		sql.append(" AND ma.name LIKE 'Fabric desc'");
		String fabType = DB.getSQLValueString(get_TrxName(), sql.toString(), (Object)fabID);
		
		StringBuilder fab = new StringBuilder();
		fab.append(FIELD_ORIGIN + "20,135");
		fab.append(SCALABLE_FONT_ROTATION + "30,30");
		fab.append(CHANGE_INTERNAT_FONT + "13");
		fab.append(FORMAT_DATA);
		fab.append(name + " " + desc + " " + fabType);
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
		locToReturn.append(location);
		locToReturn.append(FIELD_SEPARATOR);
		
		return locToReturn.toString();
	}
	
	private String addFinshedSize(MBLDMtomItemLine line) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ma.name, mai.value ");
		sql.append("FROM m_attributeinstance mai ");
		sql.append("JOIN m_attribute ma ON ma.m_attribute_id = mai.m_attribute_id ");
		sql.append("JOIN bld_mtom_item_line mtm ON mtm.attributesetinstance_id = mai.m_attributesetinstance_id ");
		sql.append("WHERE mtm.bld_mtom_item_line_id = ? ");
		sql.append(line.getbld_mtom_item_line_ID());
		sql.append(" AND (ma.name LIKE 'Drop' OR ma.name LIKE 'Width') ");
		sql.append("ORDER BY ma.name DESC");
		
		StringBuilder finSize = new StringBuilder();
		RowSet rowset = DB.getRowSet(sql.toString());
		try {
			while (rowset.next()) { 
				
				finSize.append(rowset.getString(1) + ": " + rowset.getString(2));
			}
		} catch (SQLException e) {
			log.severe("MtmLables.addFinshedSize() Could not get width or drop.");
			e.printStackTrace();
		}
		
		if(finSize.toString() != "")
		{
			return finSize.toString();
		}
		return "No size available.";
	}

}
