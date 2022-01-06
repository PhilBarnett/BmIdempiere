package au.blindmot.processes.bmleadconvert;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.compiere.model.MSysConfig;
import org.compiere.util.AdempiereUserError;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.zkoss.json.JSONObject;
import org.zkoss.json.parser.JSONParser;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.common.collect.ImmutableMap;


	public class GoogleOauthServer {
		
		/*See resources here
		 * http://highaltitudedev.blogspot.com/2013/10/google-oauth2-with-jettyservlets.html
		 * 
		 */

	 private final String clientId = "988374284472-c3vu92mel4n5hjlkmpo0j04d1h924hga.apps.googleusercontent.com";
	 private final String clientSecret = "4E4P9XpHKW64HIWO4roIBH0f";
	 /**
	     * Global instance of the scopes required by this quickstart.
	     * If modifying these scopes, delete your previously saved tokens/ folder.
	     */
	    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
	    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
	    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	    private static final String TOKENS_DIRECTORY_PATH = "tokens";
	    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
	    //private static final String REDIRECT_URI ="http://localhost:"+PORT+"/callback";
	    //private static final String SIGNIN_URI ="http://localhost:"+PORT+"/signin";
	    private static final String BLD_OAUTH_REDIRECT_URL = "BLD_OAUTH_REDIRECT_URL";
	    private static final String BLD_OAUTH_SIGNIN_URL = "BLD_OAUTH_SIGNIN_URL";

		private static final String BLD_OAUTH_REDIRECT_PORT_HTTP = "BLD_OAUTH_REDIRECT_PORT_HTTP";
		private static final String BLD_OAUTH_REDIRECT_PORT_HTTPS = "BLD_OAUTH_REDIRECT_PORT_HTTPS";
		private static final String BLD_OAUTH_REDIRECT_USE_HTTPS = "BLD_OAUTH_REDIRECT_USE_HTTPS";
		
	    private String USER_ID;
	    private static GoogleAuthorizationCodeFlow flow = null;
	    
	public GoogleOauthServer() {
		// TODO Auto-generated constructor stub
	}

 
//public static final int PORT = 8180;
//private Server server = new Server(getRedirectPort());
Server server = new Server();
	 
	public GoogleOauthServer(GoogleAuthorizationCodeFlow AuthFlow, String uSER_ID) {
		//TODO: remove USER_ID because it is the calendar user and should be for the logged in user.
		flow = AuthFlow;
		USER_ID = uSER_ID;//
		
	}
	
	/**
	 * Sets up the server to listen on both the config settings defined HTTP and HTTPS ports.
	 * The BLD_OAUTH_REDIRECT_URL and BLD_OAUTH_SIGNIN_URL will be what determines if SSL is used or not.
	 * @param server
	 */
	
	public void setUpServer(Server server) {
		
			// ServerConnector connector = new ServerConnector(server);
			//  connector.setPort(getRedirectPortHTTP());
	
		 	  // HTTP Configuration
			  HttpConfiguration http = new HttpConfiguration();
			  http.addCustomizer(new SecureRequestCustomizer());
			  
			  //Configuration for HTTPS redirect
		      http.setSecurePort(getRedirectPortHTTPS());
		      http.setSecureScheme("https");
		      ServerConnector connector = new ServerConnector(server);
		      connector.addConnectionFactory(new HttpConnectionFactory(http));
		      
		      // Setting HTTP port
		      connector.setPort(getRedirectPortHTTP());
		    
			  //http.setSecureScheme("https");
			  //http.setSecurePort(getRedirectPortHTTPS());
			  
			  /*
			  ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(http));
			  connector.setPort(8180);
			  server.addConnector(connector);
			  */
			  
			  // HTTPS configuration
		      HttpConfiguration https = new HttpConfiguration();
		      https.addCustomizer(new SecureRequestCustomizer());
	
			  
			  
			 ClassLoader classLoader = GoogleOauthServer.class.getClassLoader();
			 System.out.println("  Path: \""+classLoader.getResource(".")+"\"");
			 System.out.println("  Path: \""+GoogleOauthServer.class.getResource(".")+"\"");
			 
			 //Note: the directory of the location of the mykey.jks must be added to the runtime classpath.
			 //URL keyStorePath = GoogleOauthServer.class.getResource(".");
			 URL keyStorePath = classLoader.getResource("/mykey.jks");
			 //URL keyStorePath2 = classLoader.getResource("/mykey.jks");
			 //URL keyStorePath3 = GoogleOauthServer.class.getResource("/mykey.jks");
			 //URL keyStorePath4 = GoogleOauthServer.class.getResource("mykey.jks");
			 
			// Configuring SSL
			 SslContextFactory sslContextFactory = new SslContextFactory.Server();
			 
			// Defining keystore path and passwords
			 //sslContextFactory.setKeyStorePath(GoogleOauthServer.class.getResource("/keystore.jks").toExternalForm());
			 sslContextFactory.setKeyStorePath(keyStorePath.toExternalForm());
			 sslContextFactory.setKeyStorePassword("123456");//hardcoded self signed certificate passwords
			 sslContextFactory.setKeyManagerPassword("123456");//hardcoded self signed certificate passwords
			 //sslContextFactory.setTrustStorePath(keyStorePath.toString());
			 //sslContextFactory.setTrustStorePassword("123456");
			 
			 //Configuring the connector
		      ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
		      sslConnector.setPort(getRedirectPortHTTPS());
			 
			 /*
			 ServerConnector sslConnector = new ServerConnector(server,
			         new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.toString()),
			         new HttpConnectionFactory(https));
			 sslConnector.setPort(getRedirectPortHTTPS());
			 */
	
			 //server.setConnectors(new Connector[] { connector, sslConnector });
			 server.setConnectors(new Connector[]{sslConnector, connector});
		
	}//SetUpServer

	/*public static void main(String[] args) throws Exception {
	  new GoogleOauthServer().startJetty();
	 } */
	 
	  private static URI getOauthUrl(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
	        // Load client secrets.
	        InputStream in = GoogleOauthServer.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
	        if (in == null) {
	            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
	        }
	        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
	       
	        // Build flow and trigger user authorization request.
	    /*   flow = new GoogleAuthorizationCodeFlow.Builder(
	                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
	                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
	                .setAccessType("offline")
	                .build();
	    */    
	        in.close();        
	        return flow.newAuthorizationUrl().setRedirectUri(getRedirectUri().toString()).toURI();
	        
	  }
	 
	 public void startJetty() throws Exception {
	
		 	setUpServer(server);
		 	// Figure out what path to serve content from
	        ClassLoader classLoader = GoogleOauthServer.class.getClassLoader();
	        // We look for a file, as ClassLoader.getResource() is not
	        // designed to look for directories (we resolve the directory later)
	        URL f = classLoader.getResource("/Authenticated.html");
	        if (f == null)
	        {
	            throw new RuntimeException("Unable to find resource directory");
	        }

	        // Resolve file to directory
	     
	        URI webRootUri = f.toURI().resolve("./").normalize();
	        System.err.println("WebRoot is " + webRootUri);
		 
		 	ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	        context.setContextPath("/");
	        context.setBaseResource(Resource.newResource(webRootUri));
	        server.setHandler(context);
	 
	        // map servlets to endpoints
	        context.addServlet(new ServletHolder(new SigninServlet()),getSignInUri().getFile());        
	        context.addServlet(new ServletHolder(new CallbackServlet()),getRedirectUri().getFile());   

	        ServletHolder holderPwd = new ServletHolder("default",DefaultServlet.class);
	        holderPwd.setInitParameter("dirAllowed","true");
	        context.addServlet(holderPwd,"/");
	        
	        server.start();
	        server.join();
	 }

	 class SigninServlet extends HttpServlet {
		 /**
		 * 
		 */
		private static final long serialVersionUID = 3757548502051622543L;
		@Override
	  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
		  
		  // redirect to google for authorization
	  /* StringBuilder oauthUrl2 = new StringBuilder().append("https://accounts.google.com/o/oauth2/auth")
	   .append("?client_id=").append(clientId) // the client id from the api console registration
	   .append("&response_type=code")
	   .append("&scope=https://www.googleapis.com/auth/calendar") // scope is the api permissions we are requesting
	   .append("&redirect_uri=http://localhost:8180/callback") // the servlet that google redirects to after authorization
	   .append("&state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id")
	   .append("&access_type=offline") // here we are asking to access to user's data while they are not signed in
	   .append("&approval_prompt=force"); // this requires them to verify which account to use, if they are already signed in
      */
	   /*Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
               .setApplicationName(APPLICATION_NAME)
               .build();*/
       //comment out from here
	   String oauthUrl = null;
	try {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		oauthUrl = getOauthUrl(HTTP_TRANSPORT).toString();
		//Comment out the rest of this and use below
		//oauthUrl = flow.newAuthorizationUrl().toString();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (GeneralSecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();//To here?
	}
	   if(oauthUrl != null)
	   {
		   resp.sendRedirect(oauthUrl.toString());
	   }
	   else
	   {
		   throw new Error();
	   }
		
	  }

	 }
	 
	 class CallbackServlet extends HttpServlet {
	  /**
		 * 
		 */
		private static final long serialVersionUID = 8722080804803681155L;

	@Override
	  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
	   // google redirects with
	   //http://localhost:8180/callback?state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id&code=4/ygE-kCdJ_pgwb1mKZq3uaTEWLUBd.slJWq1jM9mcUEnp6UAPFm0F2NQjrgwI&authuser=0&prompt=consent&session_state=a3d1eb134189705e9acf2f573325e6f30dd30ee4..d62c
	   
	   // if the user denied access, we get back an error, ex
	   // error=access_denied&state=session%3Dpotatoes
	   
	   if (req.getParameter("error") != null) {
	    resp.getWriter().println(req.getParameter("error"));
	    return;
	   }
	   
	   //Below code gets the base URL from request
	   //String URL = AEnv.getApplicationUrl();
	   /*
	   StringBuffer url = req.getRequestURL();
       String uri = req.getRequestURI();
       String ctx = req.getContextPath();
       String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
       */
	   // google returns a code that can be exchanged for a access token
	   String code = req.getParameter("code");
	   
	   // get the access token by post to Google
	   //Could use GoogleAuthorizationCodeRequestUrl requestURL = flow.newAuthorizationUrl();?
	   //String body = post("https://accounts.google.com/o/oauth2/token", ImmutableMap.<String,String>builder()
	   String body = post("https://oauth2.googleapis.com/token", ImmutableMap.<String,String>builder()
	     .put("code", code)
	     .put("client_id", clientId)
	     .put("client_secret", clientSecret)
	     .put("redirect_uri", getRedirectUri().toString())
	     .put("grant_type", "authorization_code").build());

	   // ex. returns
	//   {
//	       "access_token": "ya29.AHES6ZQS-BsKiPxdU_iKChTsaGCYZGcuqhm_A5bef8ksNoU",
//	       "token_type": "Bearer",
//	       "expires_in": 3600,
//	       "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjA5ZmE5NmFjZWNkOGQyZWRjZmFiMjk0NDRhOTgyN2UwZmFiODlhYTYifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiZW1haWwiOiJhbmRyZXcucmFwcEBnbWFpbC5jb20iLCJhdWQiOiI1MDgxNzA4MjE1MDIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdF9oYXNoIjoieUpVTFp3UjVDX2ZmWmozWkNublJvZyIsInN1YiI6IjExODM4NTYyMDEzNDczMjQzMTYzOSIsImF6cCI6IjUwODE3MDgyMTUwMi5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImlhdCI6MTM4Mjc0MjAzNSwiZXhwIjoxMzgyNzQ1OTM1fQ.Va3kePMh1FlhT1QBdLGgjuaiI3pM9xv9zWGMA9cbbzdr6Tkdy9E-8kHqrFg7cRiQkKt4OKp3M9H60Acw_H15sV6MiOah4vhJcxt0l4-08-A84inI4rsnFn5hp8b-dJKVyxw1Dj1tocgwnYI03czUV3cVqt9wptG34vTEcV3dsU8",
//	       "refresh_token": "1/Hc1oTSLuw7NMc3qSQMTNqN6MlmgVafc78IZaGhwYS-o"
	//   }
	   
	   JSONObject jsonObject = null;
	   jsonObject = getJsonObject(body);
	   // get the access token from json and request info from Google
	  
	   // google tokens expire after an hour, but since we requested offline access we can get a new token without user involvement via the refresh token
	   String accessToken = (String) jsonObject.get("access_token");
	   TokenResponse response = new TokenResponse().setAccessToken(accessToken);
	   
	   //if(USER_ID == null) USER_ID = "philbarnett72@gmail.com";
	   //TODO: USER_ID needs to be logged in user, not calendar user as it currently is.
	   flow.createAndStoreCredential(response, USER_ID);
	   System.err.println(Paths.get("").toAbsolutePath().toString());
	  // File myObj = new File("filename.txt");
	  // myObj.createNewFile();
	   resp.sendRedirect("Authenticated.html");
	     
	   // you may want to store the access token in session
	   req.getSession().setAttribute("access_token", accessToken);
	   req.getSession().setAttribute("user_id", USER_ID);
	   // get some info about the user with the access token
	   //This code fails - it's incorrectly formatted
	   //String json = get(new StringBuilder("https://www.googleapis.com/oauth2/v1/userinfo?access_token=").append(accessToken).toString());
	   
	 
	   // now we could store the email address in session
	   
	   // return the json of the user's basic info
	   //resp.getWriter().println(json);
	  } 
	 }
	 
	 // makes a GET request to url and returns body as a string
	 public String get(String url) throws ClientProtocolException, IOException {
	  return execute(new HttpGet(url));
	 }
	 
	 // makes a POST request to url with form parameters and returns body as a string
	 public String post(String url, Map<String,String> formParameters) throws ClientProtocolException, IOException { 
	  HttpPost request = new HttpPost(url);
	   
	  List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	  
	  for (String key : formParameters.keySet()) {
	   nvps.add(new BasicNameValuePair(key, formParameters.get(key))); 
	  }

	  request.setEntity(new UrlEncodedFormEntity(nvps));
	  
	  return execute(request);
	  
	 }
	 
	 // makes request and checks response code for 200
	 private String execute(HttpRequestBase request) throws ClientProtocolException, IOException {
	  HttpClient httpClient = new DefaultHttpClient();
	  HttpResponse response = httpClient.execute(request);
	     
	  HttpEntity entity = response.getEntity();
	     String body = EntityUtils.toString(entity);

	  if (response.getStatusLine().getStatusCode() != 200) {
	   throw new RuntimeException("Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
	  }

	     return body;
	 }

	public static URL getRedirectUri() {
		URL redirectURI = null;
		try {
			redirectURI = new URL (MSysConfig.getValue(BLD_OAUTH_REDIRECT_URL));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(redirectURI == null)
		{
			throw new AdempiereUserError("Ensure a System Configurator setting 'BLD_OAUTH_CALLBACK_URL' exists and has a setting matching one at https://console.developers.google.com");
		}
		else return redirectURI;
	}
	/**A firewall rule must exist to pass the URL to the server. 
	 * EG HAProxy front end listens on port 80, backend ACL: backend path contains oauth2callback then use Idempiere server on <BLD_OAUTH_REDIRECT_PORT>
	 * 
	 * @return
	 */
	public static int getRedirectPortHTTP() {
		int redirectPort = 0;
		try {
				redirectPort = MSysConfig.getIntValue(BLD_OAUTH_REDIRECT_PORT_HTTP,0);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			if(redirectPort < 1)
			{
				throw new AdempiereUserError("Ensure a System Configurator setting 'BLD_OAUTH_REDIRECT_PORT_HTTP' exists and that a firewall setting to use the server on this port when the callback and signin URLs hit the firewall.");
			}
		else 
			{
				return redirectPort;
			}
	}
	
	public static int getRedirectPortHTTPS() {
		int redirectPort = 0;
		try {
				redirectPort = MSysConfig.getIntValue(BLD_OAUTH_REDIRECT_PORT_HTTPS,0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			if(redirectPort < 1)
			{
				throw new AdempiereUserError("Ensure a System Configurator setting 'BLD_OAUTH_REDIRECT_PORT_HTTPS' exists and that a firewall setting to use the server on this port when the callback and signin URLs hit the firewall.");
			}
		else 
			{
				return redirectPort;
			}
	}
	
	
	
	public static boolean useHttps() {
		boolean useHttps = false;
		try {
			useHttps = MSysConfig.getBooleanValue(BLD_OAUTH_REDIRECT_USE_HTTPS,false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return useHttps;
	}
	
	public URL getSignInUri() {
		URL signInURI = null;
		try {
			signInURI = new URL(MSysConfig.getValue(BLD_OAUTH_SIGNIN_URL));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(signInURI  == null)
		{
			throw new AdempiereUserError("Ensure a System Configurator setting 'BLD_OAUTH_SIGNIN_URL' exists and has a setting value like 'https://<applicationroot>:<oauthserver_port>/signin");
		}
		else return signInURI ;
	}
	  
	  protected JSONObject getJsonObject(String body) {
		  try {
			    Object jsonObject = (JSONObject) new JSONParser().parse(body);
			    return (JSONObject) jsonObject;
			   } catch (ParseException e) {
			    throw new RuntimeException("Unable to parse json " + body);
			   }
	  }

	public int getPort() {
		try {
		return (((ServerConnector)server.getConnectors()[0]).getLocalPort());
	}
		catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	public int getPortFromURI() {
		URL redirectURI = getRedirectUri();
		if(redirectURI != null)
		{
			int port = redirectURI.getPort();
			return port;
		}
		else 
		{
			throw new AdempiereUserError("Can't determine Oauth server port");
		}
	}
}