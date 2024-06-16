/**
 * 
 */
package au.blindmot.processes.bmleadconvert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;

import org.adempiere.webui.util.IServerPushCallback;
import org.adempiere.webui.window.FDialog;
import org.compiere.util.CLogger;
import org.zkoss.zk.ui.Executions;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
/**
 * @author phil
 *
 */
public class BmServerPushCallback implements IServerPushCallback {
private static URL REDIRECT_URL;
private static GoogleAuthorizationCodeFlow flow;
private static String USER_ID;
private final CLogger 	log = CLogger.getCLogger (BmServerPushCallback.class);
	/**
	 * @param userId 
	 * 
	 */
	public BmServerPushCallback(GoogleAuthorizationCodeFlow authFlow, String userId) {
		flow = authFlow;
		USER_ID = userId;
	}
	

	public void updateUI() {
		log.warning("---------In BmServerPushCallback.updateUI()");
		GoogleOauthServer oAuthServer = new GoogleOauthServer(flow, USER_ID);
		REDIRECT_URL = oAuthServer.getSignInUri();
		final int portInUse = GoogleOauthServer.getRedirectPortHTTPS();
		log.warning("Checking if port is in use: " + portInUse);
		if(isLocalPortInUse(portInUse))
		{
			//Assume we have an oAuthServer already running in an existing thread, skip server setup
			log.warning("---------In BmServerPushCallback.updateUI(): Local port is in use, server already running, trying a redirect");
			Executions.getCurrent().sendRedirect(REDIRECT_URL.toString(), "_blank");//try a redirect
		}
		else
		{
			try
			{
				/*
				 * Check below code: does starting a server with a USER_ID parameter lock it to the calendar
				 * being created?
				 */
				JettyRunner jettyStarter = new JettyRunner(flow, oAuthServer, USER_ID);
				Thread jettyThread = new Thread(jettyStarter);
				jettyThread.start();//Start the jetty server in a separate thread.
				int port = 0;
				
				long startTime = System.currentTimeMillis();
				long waitTime = 15000; //15 second timeout
				long end_time = startTime + waitTime;
				
				while(oAuthServer.getPort() < 1 || System.currentTimeMillis() < end_time)
				{
					Thread.sleep(500);//Just wait a little bit.
				}
				port = oAuthServer.getPort();
				
				startTime = System.currentTimeMillis();
				waitTime = 15000; //15 second timeout
				end_time = startTime + waitTime;
				
				while (!(new InetSocketAddress(REDIRECT_URL.toString(), port).isUnresolved()) || System.currentTimeMillis() < end_time)
				{
					Thread.sleep(500);//Just wait a little bit.
				}
				
				Executions.getCurrent().sendRedirect(REDIRECT_URL.toString(), "_blank");
			}
			catch (Exception e) {
				String message = e.getMessage();
				FDialog.warn(0,"URLnotValid", message);
			}
		}
	}
	public static boolean isLocalPortInUse(int port) {
	    try {
	        // ServerSocket try to open a LOCAL port
	        new ServerSocket(port).close();
	        // local port can be opened, it's available
	        return false;
	    } catch(IOException e) {
	        // local port cannot be opened, it's in use
	        return true;
	    }
	}
}