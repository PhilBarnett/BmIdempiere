package au.blindmot.ABAPaymentExFactory;

import org.adempiere.base.IPaymentExporterFactory;
import org.compiere.util.CLogger;
import org.compiere.util.PaymentExport;

import au.blindmot.ABAPaymentExport.ABAPaymentExporter;

public class ABAPaymentExportFactory implements IPaymentExporterFactory {
	
	private final static CLogger log = CLogger.getCLogger(ABAPaymentExportFactory.class);

	@Override
	public PaymentExport newPaymentExporterInstance(String className) {
		if (ABAPaymentExporter.class.getName().equalsIgnoreCase(className))
		{
			log.warning("----------In ABAPaymentExportFactory newPaymentExporterInstance()");
			return new ABAPaymentExporter();
			
		}
		System.out.println(ABAPaymentExporter.class.getName());
		System.out.println(className);
		
			return null;
	}

}
