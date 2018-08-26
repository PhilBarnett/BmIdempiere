package au.blindmot.editor;

import java.util.ArrayList;
import java.util.Properties;

import org.compiere.model.Lookup;
import org.compiere.util.DisplayType;
import org.compiere.util.NamePair;

public class BldLookup extends Lookup {

	/**
	 * 
	 */
	
	/*
	 * /TODO Copy from org.compiere.model.MPAttributeLookup
	 */
	private static final long serialVersionUID = -7557402954571813046L;

	public BldLookup(int displayType, int windowNo) {
		super(displayType, windowNo);
		// TODO Auto-generated constructor stub
	}
	public BldLookup (Properties ctx, int WindowNo)
	{
		super(DisplayType.Text, WindowNo);
//		m_ctx = ctx;
	}

	@Override
	public String getDisplay(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NamePair get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Object> getData(boolean mandatory, boolean onlyValidated, boolean onlyActive, boolean temporary,
			boolean shortlist) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKeyNoDirect(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

}
