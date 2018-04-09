package au.blindmot.remittance.factory;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

import au.blindmot.mtmlabels.processes.MtmLabels;
import au.blindmot.remittance.Remittance;

public class RemittanceFactory implements IProcessFactory {

	@Override
	public ProcessCall newProcessInstance(String className) {
		if(className.equals("au.blindmot.remittance.Remittance"))
		{
			return new Remittance();
		}
		return null;
	}

}
