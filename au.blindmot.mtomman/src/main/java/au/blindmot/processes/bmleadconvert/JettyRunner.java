package au.blindmot.processes.bmleadconvert;

import java.io.IOException;

import org.adempiere.util.LogAuthFailure;
import org.compiere.util.CLogger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;

public class JettyRunner implements Runnable{
	
private GoogleAuthorizationCodeFlow currentFlow = null;
private GoogleOauthServer authServer = null;
private static String USER_ID;
private long elapsedTime = 0;
protected static CLogger log = CLogger.get();

	public JettyRunner(GoogleAuthorizationCodeFlow flow, GoogleOauthServer oAuthServer, String uSER_ID) {
		currentFlow = flow;
		authServer = oAuthServer;
		USER_ID = uSER_ID;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			startOauthServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void startOauthServer() throws IOException {
		
		
		long startTime = System.currentTimeMillis();
		long waitTime = 100000; //100 second timeout
		long end_time = startTime + waitTime;
		int serverPort = GoogleOauthServer.getRedirectPortHTTPS();

		while (System.currentTimeMillis() < end_time || currentFlow.loadCredential(USER_ID) == null) 
		{
			try 
			{
				if(!BmServerPushCallback.isLocalPortInUse(serverPort))
				authServer.startJetty();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
				updateElapsed(startTime);
		}
		log.warning("-------JettyRunner.startOauthServer() completed after:" + elapsedTime + "ms");
		
	}

	private void updateElapsed(long startT) {
		elapsedTime = System.currentTimeMillis() - startT;
		
	}

}
