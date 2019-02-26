package au.blindmot.editor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.Lookup;
import org.compiere.util.CLogMgt;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.KeyNamePair;
import org.compiere.util.NamePair;

public class BldLookup extends Lookup {
	
	private static KeyNamePair	NO_INSTANCE = new KeyNamePair (0,"");

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
	public String getDisplay(Object value) {
		if (value == null)
			return "";
		NamePair pp = get (value);
		if (pp == null)
			return "<" + value.toString() + ">";
		return pp.getName();
	}	//	getDisplay

	@Override
	public NamePair get(Object value) {
		{
			if (value == null)
				return null;
			int bLDProductSetInstance_ID = 0;
			if (value instanceof Integer)
				bLDProductSetInstance_ID = ((Integer)value).intValue();
			else
			{
				try
				{
					bLDProductSetInstance_ID = Integer.parseInt(value.toString());
				}
				catch (Exception e)
				{
					log.log(Level.SEVERE, "Value=" + value, e);
				}
			}
			if (bLDProductSetInstance_ID == 0)
				return NO_INSTANCE;
			//
			String Description = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement("SELECT Description "
						+ "FROM bld_line_productsetinstance "
						+ "WHERE bld_line_productsetinstance_id=?", null);
				pstmt.setInt(1, bLDProductSetInstance_ID);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					Description = rs.getString(1);			//	Description
					if (Description == null || Description.length() == 0)
					{
						if (CLogMgt.isLevelFine())
							Description = "{" + bLDProductSetInstance_ID + "}";
						else
							Description = "";
					}
				}
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, "get", e);
			}
			finally
			{
				DB.close(rs, pstmt);
				rs = null; pstmt = null;
			}
			if (Description == null)
				return null;
			return new KeyNamePair (bLDProductSetInstance_ID, Description);
		}	//	get
		
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
		return get(key) != null;
	}

	@Override
	public boolean containsKeyNoDirect(Object key) {
		return containsKey(key);
	}
	

}
