package au.blindmot.factories;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

import au.blindmot.processes.mtmcreate.MtmCreate;

public class MtmCreateFactory implements IProcessFactory {

	public MtmCreateFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessCall newProcessInstance(String className) {
		if(className.equals("au.blindmot.process.MtmCreate"))return new MtmCreate();
		return null;
	}

}
