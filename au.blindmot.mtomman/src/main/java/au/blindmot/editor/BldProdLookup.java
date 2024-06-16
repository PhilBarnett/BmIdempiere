package au.blindmot.editor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.Lookup;
import org.compiere.model.MProduct;
import org.compiere.util.CLogMgt;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.KeyNamePair;
import org.compiere.util.NamePair;

public class BldProdLookup extends Lookup {
	
	private static KeyNamePair	NO_INSTANCE = new KeyNamePair (0,"");

	/**
	 * 
	 */
	
	/*
	 * /TODO Copy from org.compiere.model.MPAttributeLookup
	 */
	private static final long serialVersionUID = -7557402954571813046L;

	public BldProdLookup(int displayType, int windowNo) {
		super(displayType, windowNo);
		// TODO Auto-generated constructor stub
	}
	public BldProdLookup (Properties ctx, int WindowNo)
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
			int mProduct_ID = 0;
			if (value instanceof Integer)
				mProduct_ID = ((Integer)value).intValue();
			
			else
			{
				try
				{
					mProduct_ID = Integer.parseInt(value.toString());
				}
				catch (Exception e)
				{
					log.log(Level.SEVERE, "Value=" + value, e);
				}
			}
			if (mProduct_ID == 0)
				return NO_INSTANCE;
			/*
			 * TODO: Try getting the product from cache instead of querying the DB every time.
			 * Use public static MProduct get (Properties ctx, int M_Product_ID)
			 * This tries the cache; if the MProduct object isn't in the cache, it gets added so it's there next time.
			 */
			
			String Description = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement("SELECT name "
						+ "FROM m_product "
						+ "WHERE m_product_id = ? ", null);
				pstmt.setInt(1, mProduct_ID);
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					Description = rs.getString(1);			//	Description
					if (Description == null || Description.length() == 0)
					{
						if (CLogMgt.isLevelFine())
							Description = "{" + mProduct_ID + "}";
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
			return new KeyNamePair (mProduct_ID, Description);
		
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
