package au.blindmot.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.compiere.acct.Doc;
import org.compiere.acct.Fact;
import org.compiere.model.MAcctSchema;

public class Doc_BLDMtomProduction extends Doc {

	public Doc_BLDMtomProduction(MAcctSchema as, ResultSet rs,
			String trxName) {
		super(as, MBLDMtomProduction.class, rs, null, trxName);
	
	}


	@Override
	protected String loadDocumentDetails() {
		
		return null;
	}

	@Override
	public BigDecimal getBalance() {
		
		return BigDecimal.ZERO;
	}

	@Override
	public ArrayList<Fact> createFacts(MAcctSchema as) {
		ArrayList<Fact> facts = new ArrayList<Fact>();
		return facts;
	}

}
