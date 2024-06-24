package za.co.ntier.factories;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;
import za.co.ntier.processes.WooCommerce;
import za.co.ntier.processes.WooCommerceProductSetup;

public class WooCommerceFactory implements IProcessFactory{

	@Override
	public ProcessCall newProcessInstance(String className) {
		if (className.equals("za.co.ntier.processes.WooCommerce")) return  new WooCommerce();
		else if (className.equals("za.co.ntier.processes.WooCommerceProductSetup")) return  new WooCommerceProductSetup();
		
		return null;
	}
	
	

}
