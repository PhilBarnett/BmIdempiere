/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 2018 Phil Barnett                							  *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/

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
