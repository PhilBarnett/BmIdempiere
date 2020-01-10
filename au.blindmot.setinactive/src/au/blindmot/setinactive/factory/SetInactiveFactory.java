/**
 * 
 */
package au.blindmot.setinactive.factory;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;
import org.compiere.util.CLogger;
import au.blindmot.setinactive.process.SetInactive;

/**
 * @author phil
 *
 */
public class SetInactiveFactory implements IProcessFactory {

	CLogger log = CLogger.getCLogger(SetInactive.class);
	/* (non-Javadoc)
	 * @see org.adempiere.base.IProcessFactory#newProcessInstance(java.lang.String)
	 */
	@Override
	public ProcessCall newProcessInstance(String className) {
			if(className.equals("au.blindmot.setinactive.process.SetInactive"))
				{
					log.warning("---------> au.blindmot.setinactive  is loaded");
					return new SetInactive();
				}
			return null;
	}
}

