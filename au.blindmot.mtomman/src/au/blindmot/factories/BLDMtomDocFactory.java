package au.blindmot.factories;

import java.sql.ResultSet;

import org.adempiere.base.IDocFactory;
import org.compiere.acct.Doc;
import org.compiere.model.MAcctSchema;

public class BLDMtomDocFactory implements IDocFactory {


	@Override
	public Doc getDocument(MAcctSchema as, int AD_Table_ID, int Record_ID, String trxName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Doc getDocument(MAcctSchema as, int AD_Table_ID, ResultSet rs, String trxName) {
		// TODO Auto-generated method stub
		return null;
	}

}
