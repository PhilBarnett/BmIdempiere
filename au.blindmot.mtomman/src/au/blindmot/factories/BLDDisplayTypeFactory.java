package au.blindmot.factories;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.adempiere.base.IDisplayTypeFactory;
import org.compiere.model.Query;
import org.compiere.model.X_AD_Reference;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Language;


public class BLDDisplayTypeFactory implements IDisplayTypeFactory {
	
	public static int BldMtmParts= ((X_AD_Reference)new Query(Env.getCtx(), X_AD_Reference.Table_Name, "AD_Reference_UU = '616c6609-dbcd-44e2-b50f-88166d7faba0'", null).first()).getAD_Reference_ID();
	public static int BldMtmProduct= ((X_AD_Reference)new Query(Env.getCtx(), X_AD_Reference.Table_Name, "AD_Reference_UU = '7770f878-bac8-4460-a181-89a48fb4aa26'", null).first()).getAD_Reference_ID();
	CLogger log = CLogger.getCLogger(BLDDisplayTypeFactory.class);
	@Override
	public boolean isID(int displayType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNumeric(int displayType) {

		if(displayType == BldMtmProduct)
		{
			log.warning("--------BLDDisplayTypeFactory.isNumeric(int displayType) returned true" + " and int displayType == " + displayType);
			return true;
		}
			
		return false;
	}

	@Override
	public Integer getDefaultPrecision(int displayType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isText(int displayType) {
		if(displayType == BldMtmParts)
		{
			log.warning("--------BLDDisplayTypeFactory.isText(int displayType) returned true" + " and int displayType == " + displayType);
			return true;
		}
			
		return false;
	}

	@Override
	public boolean isDate(int displayType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLookup(int displayType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLOB(int displayType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DecimalFormat getNumberFormat(int displayType, Language language, String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleDateFormat getDateFormat(int displayType, Language language, String pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getClass(int displayType, boolean yesNoAsBoolean) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQLDataType(int displayType, String columnName, int fieldLength) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription(int displayType) {
		// TODO Auto-generated method stub
		return null;
	}

}
