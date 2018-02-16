package au.blindmot.mtmlabels.labelfactory;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

import au.blindmot.mtmlabels.processes.MtmLabels;


public class BLDMtomLabelFactory implements IProcessFactory {

	@Override
	public ProcessCall newProcessInstance(String className) {
		if(className.equals("au.blindmot.mtmlabels.processes.MtmLabels"))return new MtmLabels();
		return null;
	}

}
