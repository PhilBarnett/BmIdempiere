/**
 * 
 */
package au.blindmot.processes.bmleadconvert;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;

import org.adempiere.webui.util.IServerPushCallback;
import org.adempiere.webui.window.FDialog;
import org.zkoss.zk.ui.Executions;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;

import au.blindmot.BMGoogleOauth.GoogleOauthServer;
/**
 * @author phil
 *
 */
public class BmServerPushCallback implements IServerPushCallback {
private static URL REDIRECT_URL;
private static GoogleAuthorizationCodeFlow flow;
private static String USER_ID;
	/**
	 * @param userId 
	 * 
	 */
	public BmServerPushCallback(GoogleAuthorizationCodeFlow authFlow, String userId) {
		flow = authFlow;
		USER_ID = userId;
	}
	

	@SuppressWarnings("restriction")
	public void updateUI() {
		GoogleOauthServer oAuthServer = new GoogleOauthServer(flow, USER_ID);
		REDIRECT_URL = oAuthServer.getSignInUri();
		
		if(isLocalPortInUse(oAuthServer.getPortFromURI()))
		{
			//Assume we have an oAuthServer already running in an existing thread, skip server setup
			Executions.getCurrent().sendRedirect(REDIRECT_URL.toString(), "_blank");//try a redirect
		}
		else
		{
			try
			{
				
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
	private boolean isLocalPortInUse(int port) {
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