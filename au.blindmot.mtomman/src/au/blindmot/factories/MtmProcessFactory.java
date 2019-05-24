package au.blindmot.factories;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

import au.blindmot.copynonselect.process.CopyNonSelect;
import au.blindmot.gridprice.processes.GridPrice;
import au.blindmot.mtmlabels.processes.MtmLabels;
import au.blindmot.mtmproductinfo.processes.MtmProductGetConfig;
import au.blindmot.processes.mtmcreate.MtmCreate;

public class MtmProcessFactory implements IProcessFactory {

	public MtmProcessFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessCall newProcessInstance(String className) {
		if(className.equals("au.blindmot.processes.mtmcreate.MtmCreate"))
			{
				return new MtmCreate();
			}
			else if(className.equals("au.blindmot.mtmlabels.processes.MtmLabels"))
			{
				return new MtmLabels();
			}
			else if (className.equals("au.blindmot.gridprice.processes.GridPrice"))
			{
				return new GridPrice();
			}
			else if (className.equals("au.blindmot.mtmproductinfo.processes.MtmProductGetConfig"))
			{	
				return new MtmProductGetConfig();
			}
			else if (className.equals("au.blindmot.copynonselect.process.CopyNonSelect"))
			{	
				return new CopyNonSelect();
			}
		
				
		return null;
	}
	
}//
