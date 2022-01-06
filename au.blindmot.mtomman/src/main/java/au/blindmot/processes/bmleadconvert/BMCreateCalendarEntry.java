/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/

package au.blindmot.processes.bmleadconvert;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.util.ServerPushTemplate;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCampaign;
import org.compiere.model.MLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MRequest;
import org.compiere.model.MUser;
import org.compiere.model.PO;
import org.compiere.process.AddAuthorizationProcess;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.zkoss.zk.ui.Desktop;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;





public class  BMCreateCalendarEntry  extends SvrProcess {


	private boolean boolParam;
	private Date dateParam;
	private String rangeFrom;
	private String rangeTo;
	private int intParam;
	private BigDecimal bigDecParam;
	private PO record;
	private MRequest meeting; //new MRequest
	private int recordId;
	private MOrder cOrder;
	private String type;
	
	private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final CLogger 	slog = CLogger.getCLogger (BMCreateCalendarEntry.class);
    //private static final int WEB_PORT = 8180;//100 + CConnection.get().getWebPort();
    
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String CREDENTIALS_FILE_PATH_2 = "./credentials.json";

	
	/**
	 * The prepare function is called first and is used to load parameters
	 * which are passed to the process by the framework. Parameters to be
	 * passed are configured in Report & Process -> Parameter.
	 * 
	 */
	@Override
	protected void prepare() {

		// Each Report & Process parameter name is set by the field DB Column Name
		for ( ProcessInfoParameter para : getParameter())
		{
			if ( para.getParameterName().equals("isBooleanParam") )
				boolParam = "Y".equals((String) para.getParameter());			// later versions can use getParameterAsString
			else if ( para.getParameterName().equals("dateParam") )
				dateParam = (Date) para.getParameter();
			// parameters may also specify the start and end value of a range
			else if ( para.getParameterName().equals("rangeParam") )
			{
				rangeFrom = (String) para.getParameter();
				rangeTo = (String) para.getParameter_To();
			}
			else if ( para.getParameterName().equals("intParam") )
				intParam = para.getParameterAsInt();
			else if ( para.getParameterName().equals("bigDecParam") )
				bigDecParam = (BigDecimal) para.getParameter();
			else 
				log.info("Parameter not found " + para.getParameterName());
		}

		// you can also retrieve the id of the current record for processes called from a window
		recordId = getRecord_ID();
	}
	
	/**
	 * The doIt method is where your process does its work
	 */
	@Override
	protected String doIt() throws Exception {

		/* Commonly the doIt method firstly do some validations on the parameters
		   and throws AdempiereUserException or AdempiereSystemException if errors found
		
		   After this the process code is written and on any error an Exception must be thrown
		   Use the addLog method to register important information about the running of your process
		   This information is preserved in a log and shown to the user at the end.
		*/
		
		meeting = new MRequest(getCtx(), recordId,null);//Seems to fail here silently? Maybe try null as trxn?
		type = meeting.getR_RequestType().getName();
		
		if (!type.equalsIgnoreCase("Sales Lead")&&
				(!type.equals("Warranty")&&
				(!type.equals("Service Call")&&
			(!type.equals("Installation")))))
				{
					throw new AdempiereUserError("The Request is not a Sales Lead, Installation, Service Call or Warranty.");
				}
		int cOrderID = meeting.getC_Order_ID();
		int calendarUserID = 0;
		if(type.equalsIgnoreCase("Sales Lead"))
		{
			calendarUserID = meeting.getSalesRep_ID();
		}
		
		if (cOrderID > 0)
		{
			cOrder = new MOrder(getCtx(), meeting.getC_Order_ID(), get_TrxName());
		}
		
		//MOpportunity op = new MOpportunity(getCtx(), 0, get_TrxName());
		MBPartner bp = new MBPartner(getCtx(), meeting.getC_BPartner_ID(), get_TrxName());
		if(bp.get_ID() == 0) throw new AdempiereUserError("Request must have a Business Partner.");
		MUser customerUser = new MUser(getCtx(), bp.getPrimaryAD_User_ID(), get_TrxName());
		Timestamp p_meetingDate = meeting.getStartTime();
		if(p_meetingDate == null) throw new AdempiereUserError("Request must have a start time.");
		MBPartnerLocation[] locs = bp.getLocations(true);
		MBPartnerLocation loc = locs[0];//Just use the first location.
		
		if(p_meetingDate != null)
		{
		//Create Google Calendar entry from Request
		   // Refer to the Java quickstart on how to setup the environment:
        // https://developers.google.com/calendar/quickstart/java
        // Change the scope to CalendarScopes.CALENDAR and delete any stored
        // credentials.
		// Build a new authorized API client service.
			
		/*
		 * TODO: Add installer user ids (make Installer the BP for record?)
		 * TODO: Add FK bld_mtm_install_id to r_request table
		 */
			
		MUser calendarUser = new MUser(getCtx(), calendarUserID, null);
		log.warning("-------- In BM LeadConvert creating Google Calendar entry.");
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        log.warning("-------- In BM LeadConvert creating Google Calendar entry. Created NetHttpTransport: " + HTTP_TRANSPORT .toString());
  
        //Do we already have credentials?
        //MUser salesRep = new MUser(getCtx(), p_SalesRep_ID, null);
        //String userId = salesRep.getEMailUser();
        
        /* 1/1/22
        Credential storedCredential = flow.loadCredential(userId);
        if(storedCredential == null)
        {
        	GoogleAuthorizationCodeRequestUrl requestURL = flow.newAuthorizationUrl();
        	// response = null;
        	//HttpServletResponse.sendRedirect(requestURL.getRedirectUri());
        }
 */ //1/1/22
        //TODO:Currently doing salesreps only, modify based on type var to cater for other users.
        //TODO: Test for users that have their own BP (not 'clientUser' etc).
        
        
        
        
        String emailUser = calendarUser.getEMailUser();
        if(emailUser == null) throw new AdempiereUserError("The person you are trying to create the calendar entry doesn't have an email address.");
        
        Credential calCredential = getCredentials(HTTP_TRANSPORT, emailUser);
        log.warning("-------- In BM LeadConvert creating Google Calendar entry. getCredentials(HTTP_TRANSPORT) returned: " + calCredential.toString());
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, calCredential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        log.warning("-------- In BM LeadConvert creating Google Calendar entry. Created Calendar object: " +service.toString()); 
       
        
      
        
        String meetingID = null;
        if(meeting != null)
        {
        	log.warning("-------- In BM LeadConvert creating Google Calendar from meeting: " + meeting.get_ID());
        	meetingID = meeting.getR_Request_UU();
        	meetingID = meetingID.replace("-", "");
        	System.out.print(meetingID);
        }
        log.warning("-------- In BM LeadConvert creating Google Calendar entry with MRequestID: " + meetingID);
        
        //Setup private event extended properties
        //Commented out - can't be seen in actual calendar
        /*
        Map <String, String> extPropertiesPrivate = new HashMap<>();
        
        if(op !=null && lead != null)
        {
        	extPropertiesPrivate.put("Lead phone: ", lead.getPhone());
        	extPropertiesPrivate.put("Lead email: ", lead.getPhone());
        	MCampaign campaign = new MCampaign(getCtx(), lead.getC_Campaign_ID(), null);
		    String campName = campaign.getName();
		    if(campName != null)
		    {
		    	extPropertiesPrivate.put("Lead source: ", campaign.getName());
		    }
		     MUser creator = new MUser(getCtx(), op.getCreatedBy(), null);
		     extPropertiesPrivate.put("Created by: ", creator.getName());
        }
        
        if(cOrder != null)
        {
        	String orderLink = "Order Link not Available";
        	try
        	{
        		orderLink = AEnv.getZoomUrlTableID(cOrder);
        	}
        	catch(Exception e)
        	{
        		System.out.println("SalesOrder link not added. \n" + e.toString());
        		addLog("SalesOrder link not added.Sales Order number: " + cOrder.getDocumentNo());
        	}
        	extPropertiesPrivate.put("Link to Sales Order: ", cOrder.getDocumentNo()+" "+orderLink);
        }
        */
        StringBuilder description = new StringBuilder();
        	description.append("Regarding: ");
        	description.append(meeting.getSummary());//This should be all the customer can see.
       
        description.append("\n");
        description.append("Client email: ");
        description.append(customerUser.getEMail());
        description.append("\n");
        description.append("Client phone: ");
        description.append(customerUser.getPhone());
        description.append("\n");
        MCampaign campaign = new MCampaign(getCtx(), customerUser.getC_Campaign_ID(), null);
        String campName = campaign.getName();
        if(campName != null)
        {
        	description.append("Lead source: ");
        	description.append(campaign.getName());
        	description.append("\n");
        }
       
        	 description.append("Created by: ");
        	 MUser creator = new MUser(getCtx(), meeting.getCreatedBy(), null);
		     description.append(creator.getName());
		     description.append("\n");
        
        if(cOrder != null)
        {
        	String orderLink = "Order Link not Available.";
        	try
        	{
        		orderLink = AEnv.getZoomUrlTableID(cOrder);
        	}
        	catch(Exception e)
        	{
        		System.out.println("SalesOrder link not added. \n" + e.toString());
        		addLog("SalesOrder link not added. Sales Order number: " + cOrder.getDocumentNo());
        	}
        	description.append("Link to Sales Order number ");
        	description.append(cOrder.getDocumentNo());
        	description.append(":\n");
        	description.append(orderLink);
        }
        log.warning("-------- In BM LeadConvert Creating Calendar entry with description: " + description.toString());
       
        MLocation mLoc = new MLocation(getCtx(), loc.getC_Location_ID(), get_TrxName());
        StringBuilder location = new StringBuilder();
        StringBuilder summary = new StringBuilder();
        summary.append(bp.getName());
       if(mLoc != null)
       {
	    	
	    	String add1 = mLoc.getAddress1();
	    	if(add1 != null)
	    	{
	    		location.append(add1);
	    	}
	        location.append(" ");
	        String add2 = mLoc.getAddress2();//This is sometimes 'City'
	        if(add2 != null)
	        {
	        	location.append(" ");
	        	location.append(add2);
	        	summary.append(" ");
	        	summary.append(add2);
	        }
	        String city = mLoc.getCity();
	        if(city != null)
	        {
	        	location.append(" ");
	        	location.append(city);
	        	summary.append(" ");
	        	summary.append(city);
	        }
	        
       }
       log.warning("-------- In BM LeadConvert Creating Calendar entry with location: " + location.toString());
        
        Event event = new Event()
            .setSummary(summary.toString())
            .setLocation(location.toString())
            .setDescription(description.toString());
        	if(meetingID != null)
        		{
        			event.setId(meetingID);
        		}
        	
        	/*	Google example
        	 * Event.ExtendedProperties extendedProperties = new Event.ExtendedProperties();
    			Map<String, String> privateExtendedProperties = new HashMap<String, String>();
    			privateExtendedProperties.put("domodentEventId", ddEvent._id);
    			extendedProperties.setPrivate(privateExtendedProperties);
    			event.setExtendedProperties(extendedProperties);
        	
        	//Commented out - can't be seen in actual calendar
        	Event.ExtendedProperties extendedProperties = new Event.ExtendedProperties();
        	extendedProperties.setShared(extPropertiesPrivate);
        	event.setExtendedProperties(extendedProperties);
        	 */
      //  event.get
        	
        
        //final long duration = (p_meetingDuration.multiply(new BigDecimal(60000))).longValue();
		
		//endDate.setTime(meeting.getEndTime());
		
        DateTime startDateTime = new DateTime(p_meetingDate);
        EventDateTime start = new EventDateTime()
            .setDateTime(startDateTime)
            .setTimeZone("Australia/Sydney");
        event.setStart(start);
        
        BigDecimal bigDuration = new BigDecimal(120);
        final long defaultDuration = (bigDuration.multiply(new BigDecimal(60000))).longValue();
        Timestamp endDate = new Timestamp(0);
		if(meeting.getEndTime() == null)
		{
			endDate.setTime(p_meetingDate.getTime() + defaultDuration);
		}
		else
		{
			endDate = meeting.getEndTime();
		}
        DateTime endDateTime = new DateTime(endDate);
        EventDateTime end = new EventDateTime()
            .setDateTime(endDateTime)
            .setTimeZone("Australia/Sydney");
        event.setEnd(end);

       // String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
       // event.setRecurrence(Arrays.asList(recurrence));

        /*//Attendees can be added as per below
        EventAttendee[] attendees = new EventAttendee[] {
           new EventAttendee().setEmail("phil@blindmotion.com.au"),
           new EventAttendee().setEmail("sbrin@example.com"),
        };
        event.setAttendees(Arrays.asList(attendees));
		*/
        
        EventReminder[] reminderOverrides = new EventReminder[] {
            new EventReminder().setMethod("email").setMinutes(24 * 60),
            new EventReminder().setMethod("popup").setMinutes(10),
        };
        Event.Reminders reminders = new Event.Reminders()
            .setUseDefault(false)
            .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);
        String calendarId = "";
        try {
        calendarId = calendarUser.getEMailUser(); //"philbarnett72@gmail.com";//Hard coded for testing.
        event = service.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
        log.warning("-------- In BM LeadConvert Creating Calendar entry with MUser: " + calendarUser.getName());
        log.warning("-------- In BM LeadConvert Creating Calendar" + "Event created: %s\n" + event.getHtmlLink());
        /*
        StringBuilder link = new StringBuilder("<a href=");
        link.append(event.getHtmlLink());
        link.append(">");
        link.append("Calendar entry</a>");
        */
        addLog("Event created: " + event.getHtmlLink());
        }
         catch (/*com.google.api.client.googleapis.json.*/GoogleJsonResponseException e)
        {
        	log.warning("BMCreateCalendarEntry failed with error: " + e.toString());
        	int statusCode = e.getStatusCode();
        	if(statusCode == 404)
        	{
        		throw new AdempiereUserError("404 error. Check that the " + calendarId + " is actually a Google calendar.");
        	}
        	else if(statusCode == 401)
        	{
        		throw new AdempiereUserError("401 error. Check you have 'Make changes and manage sharing' on the " + calendarId + " calendar.");
        	}
        	else if(statusCode == 409)
        	{
        		throw new AdempiereUserError("409 error. The calendar event already exists. Dirty fix if you deleted it: copy the request and try again.");
        	}
        	else
        	{
        		throw new AdempiereUserError(e.toString());
        	}
        }
	}
		return "@OK@";
}

		
/**
* Creates an authorized Credential object.
* @param HTTP_TRANSPORT The network HTTP Transport.
* @return An authorized Credential object.
* @throws IOException If the credentials.json file cannot be found.
*/
@SuppressWarnings("restriction")
/*
 * TODO: Remove String userId from this method and all affected code/assignments
 * It is creating credentials for the calendar use and it should be for the logged in user.
 */
private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, String userId) throws IOException {
       // Load client secrets.
	
	//Try new 8.2 feature to handle OAuth
	SvrProcess addAuthorizationProcess = new AddAuthorizationProcess();
	ArrayList<ProcessInfoParameter> paras = new ArrayList<ProcessInfoParameter>();
	//(String parameterName, Object parameter, Object parameter_To, String info, String info_To)
	ProcessInfoParameter aD_AuthorizationScope = new ProcessInfoParameter("AD_AuthorizationScopes", "Calendar", "", "", "");
	paras.add(aD_AuthorizationScope);
	//Note hardcoded, TODO: get the credential ID
	ProcessInfoParameter aD_AuthorizationCredential_ID = new ProcessInfoParameter("AD_AuthorizationCredential_ID", "1000001", "", "", "");
	paras.add(aD_AuthorizationCredential_ID);
	ProcessInfoParameter auth_OpenPopup = new ProcessInfoParameter("Auth_OpenPopup", "N", "", "", "");
	paras.add(auth_OpenPopup);
	ProcessInfoParameter aD_Language = new ProcessInfoParameter("AD_Language", "English", "", "", "");
	paras.add(aD_Language);
	
	StringBuilder sql = new StringBuilder();
	sql.append("SELECT ad_process_id ");
	sql.append("FROM ad_process ");
	sql.append("WHERE ad_process.value = 'AddAuthorizationProcess'");
	int processID = DB.getSQLValue(null, sql.toString());
	
	ProcessInfo pI = new ProcessInfo("AddAuthorizationProcess", processID);
	ProcessInfoParameter[] paraArray = new ProcessInfoParameter[paras.size()];
	pI.setParameter(paras.toArray(paraArray));
	//static String trxName = get_TrxName();
	addAuthorizationProcess.startProcess(Env.getCtx(), pI, null);
	
	//ProcessInfo pI = addAuthorizationProcess.getProcessInfo();
	
	//String msg = (String) addAuthorizationProcess.doIt("org.compiere.process.AddAuthorizationProcess", "doIt", null);
	//System.out.println(msg);
	
	
	
	
		slog.warning("------------In BMCreateCalendarEntry getCredentials()");
       InputStream in = BMConvertLead.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH);
       if (in == null) 
       {
       	{
       		throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH/* + " or " + CREDENTIALS_FILE_PATH_2*/);
       	}
       	/*
       	InputStream in2 = BMConvertLead.class.getClassLoader().getResourceAsStream(CREDENTIALS_FILE_PATH_2);
       	if(in2 == null)
       	{
       		throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH + " or " + CREDENTIALS_FILE_PATH_2);
       	}
       	in2.close();
           */
       }
       
       
       
       
       
       
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
     //  GoogleClientSecrets clientSecrets = null;

       // Build flow and trigger user authorization request.
       //URL tokenPath = BMConvertLead.class.getClassLoader().get(TOKENS_DIRECTORY_PATH);
       
       
       GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
               HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
               .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
               .setAccessType("offline")
               .build();
       
      
       //Do we already have credentials?
       //MUser salesRep = new MUser(getCtx(), p_SalesRep_ID, null);
       //String userId = salesRep.getEMailUser();
       Credential storedCredential = flow.loadCredential(userId);
   /*   if(storedCredential == null)//test for validity
       	
       {
       	 try {
       		    // refresh the credential to see if the refresh token is still valid
       		    storedCredential.refreshToken();
       		    System.out.println("Refreshed: expires in: " + storedCredential.getExpiresInSeconds());
       		  } catch (TokenResponseException e) 
       	 		{
       			  System.out.println("Credentials invalid - " + e.toString());
       			  storedCredential = null;//Set to null so they get renewed.
       		    // This Exception contains the HTTP status and reason etc.
       		    // In case of a revoke, this will throw something like a 401 - "invalid_grant"
       	 		}
       }
       */
       if(storedCredential == null||storedCredential != null)//we don't have credentials.
       	/*THE ABOVE IF STATEMENT IS BY DESIGNED TO BE TRUE
       	 * Sometimes the credential returns 401 not authourised even when the validity test is successful.
       	 * So the dirty workaround is to just open a browser to ensure authentication does not fail.
       	 */
       {
       	Desktop desktop = AEnv.getDesktop();
       	ServerPushTemplate pushUpdateUi = new ServerPushTemplate(desktop);
       	BmServerPushCallback callback = new BmServerPushCallback(flow, userId);
       	
       	slog.warning("------------In BMCreateCalendarEntry getCredentials(), about to execute pushUpdateUi.execute(callback); Will log if succesful");
       	pushUpdateUi.executeAsync(callback); //Async or sync?
       	//pushUpdateUi.execute(callback);
       	slog.warning("------------In BMCreateCalendarEntry getCredentials(), execute pushUpdateUi.execute(callback); succesful");
       	
       	long startTime = System.currentTimeMillis();
   		long waitTime = 10000; //10 second timeout
   		long end_time = startTime + waitTime;

   		while (System.currentTimeMillis() < end_time || flow.loadCredential(userId) == null) {
   		   try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
   		}
   		
       	storedCredential = flow.loadCredential(userId);
       }
       //We do have credentials
       in.close();
		return storedCredential;
      
       //LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(WEB_PORT).build();
       //return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
   
	}	

	/**
	 * Post process actions (outside trx).
	 * Please note that at this point the transaction is committed so
	 * you can't rollback.
	 * This method is useful if you need to do some custom work when 
	 * the process complete the work (e.g. open some windows).
	 *  
	 * @param success true if the process was success
	 * @since 3.1.4
	 */
	@Override
	protected void postProcess(boolean success) {
		if (success) {
			
		} else {
              
		}
	}

}
