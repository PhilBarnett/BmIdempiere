package au.blindmot.factories;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

import au.blindmot.mtmlabels.processes.MtmLabels;
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
		return null;
	}
	
}//
