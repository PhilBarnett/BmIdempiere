package au.blindmot.mtmlabels;

import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;

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
			else if(paraName.equalsIgnoreCase("check_box"))
				ignoreExistingmtmProd = para.getParameterAsBoolean(); 
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
			label.append(START_FORMAT);//TODO: Add labelhome, printrate, media darkenss, JMA
			label.append(addBarcode(lines[i].getbarcode()));
			label.append(addProductionDate(lines[i]));
			label.append(addClientName(lines[i]));
			label.append(addOrderDescription(mBLDMtomProduction.getDescription()));
			label.append(addFabric(lines[i].getinstance_string()));
			label.append(addLocation(lines[i]));
			label.append(addFinshedSize(lines[i]));
			label.append(PRINT_QUALITY);
			label.append(END_FORMAT);
			outputFile.append(label);
		}
		
		
		
		return null;
	}
	
	private String addBarcode(String barcode) {
		
	}
	
	private String addProductionDate(MBLDMtomItemLine line) {
		
	}
	
	private String addClientName(MBLDMtomItemLine line) {
		//TODO: Get BPname
	}
	
	private String addOrderDescription(String orderDesc) {
		
	}
	
	private String addFabric(String instanceString) {
		//TODO:Parse out instance string, copy/call method from MtmButtonAction
	}
	
	private String addLocation(MBLDMtomItemLine line) {
		
	}
	
	private String addFinshedSize(MBLDMtomItemLine line) {
		//TODO: Get width x drop from attributes
	}

}
