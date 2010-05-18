/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab.tools;

import org.netbeans.lib.collab.*;
import org.netbeans.lib.collab.util.*;
import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.log4j.*;

/**
 *

 *
 * @author Jacques Belissent
 * @author Rahul Shah
 * @author Vijayakumar Palaniappan
 * @author Rebecca Ramer
 *
 */
class WorkerTimerTask extends TimerTask {
    
    Runnable runnable;
    WorkerTimerTask(Runnable r) {
        runnable = r;
    }
    
    public void run() {
        Generate.worker.addRunnable(runnable);
    }
}




public class Generate implements CollaborationSessionListener, ConferenceServiceListener, PresenceServiceListener, PersonalStoreServiceListener, NotificationServiceListener, SecureSessionListener
{

    static final int SAMPLING_RATE = 10000;
    static final int MAX_WORKER_THREADS = 20;
    static boolean DEBUG_ON = false;
    static boolean APILOG = false;
    static boolean QUIET = false; // turn off most voluminous messages

    static boolean VARIABLE_CONF = false; //vary participants and duration
    static boolean BIG_CONF = false; //use nri-style conferences only
    static boolean NO_CONF = false; //no conference rooms
    static boolean NO_UPDATES = false; //no presence updates
    static boolean NO_INVITES = false;


    int maxNumPrivateParticipants = -1;
    int maxPrivateRoomDuration = 10;

    int maxNumPublicParticipants = -1;
    int maxPublicRoomDuration = 30;

    //Thread sleep time for each user, in between each msg send
    static int minWaitTime = 1000;
    static int maxWaitTime = 10000;

    //int TIMETOPAUSE = 1000;*/
    static String[] shortMonth = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec" };

    static double MSG_RATE = 0;
    static int[] START_USER; 
    static int[] END_USER;
    int[] OFFSET;
    static int numServers = 0;
    static int numUsers = 0;
    static String reloginUid = null;
    static String bigConfOwner = null;

    //number of hours in a day.
    int hours = 8;
    static String SERVER[];
    static String DOMAIN[];
    static String LOG_FILE = "STD_OUT";
    static String APILOG_FILE = "";
    static PrintStream ps = new PrintStream(System.out);

    public static boolean[] WATCH;
    public static boolean LOGIN = false;
    public static boolean MSG_LISTEN = false;
    
    //Duration for which the conference room will last (in ms)
    static long conferenceDuration = 600000; // 10 minutes
    static long newPrivateConferenceDelay = 600000; // 10 minutes
    static long newPublicConferenceDelay = 600000; // 10 minutes
    static long presenceUpdateDelay = 600000; // 10 minutes
    static long sendNotificationDelay = 600000; // 10 minutes

    //number of updates done per user per day
    static int updates = 20;
    //number of conference rooms created by a user per day.
    static int privateRooms = 5;
    static int publicRooms = 0;
    //number of relogins per user per day
    static int relogin = 0;
    //number of notifications per per user day
    static int notifications = 0;
    //number of recipients per notification
    static int numRecipients = 1;
    //percentage of users logged in at anytime.
    static int pul = 100;
    //password to use
    static String password = "iplanet";
    //fire messages when no of occupants in the room are
    static int occThrForMessFiring = 0;
    
    
    Hashtable _sessionMap = new Hashtable();

    static CollaborationSession _session[][];

    CollaborationSessionFactory fac;

    String sso_server = null;
    String org_name = null;
    String root_suffix = null;

    static Worker worker = new Worker(0, MAX_WORKER_THREADS,
				      MAX_WORKER_THREADS*10);

    Timer timer = new Timer(false);

    HashSet conferenceTypes = new HashSet();

    // stats info
    final private static String AUTH_SERVICE_NAME = "auth";
    final private static String SESSION_RESOURCE_NAME = "session";
    final private static String PRESENCE_SERVICE_NAME = "presence";
    final protected static String MESSAGE_SERVICE_NAME = "message";
    static {
	LoadStatistics.createService(AUTH_SERVICE_NAME);
	LoadStatistics.createResource(SESSION_RESOURCE_NAME);
	LoadStatistics.createService(PRESENCE_SERVICE_NAME);
	LoadStatistics.createService(MESSAGE_SERVICE_NAME);
    }

    private static Random rand;

    /** Creates new Generate */
    public Generate() {
        Random r1 = new Random(System.currentTimeMillis());
        rand = new Random(r1.nextLong());
    }

    private String getCookie(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            String cookie = connection.getHeaderField("Set-cookie");
            if (cookie == null) {
                System.out.println("No cookies in HTTP request, server down?");
                return null;
            }
            return cookie;
        } catch (Exception e) {
            System.out.println("getCookie Exception : " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
        }
    }

    private String getSessionId(URL url, String cookie,
                                String user, String password) {
        HttpURLConnection connection = null;
        BufferedReader in = null;
        PrintStream out = null;
        
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cookie", cookie);
            connection.setFollowRedirects(false);

            out = new PrintStream(connection.getOutputStream());
            String params = "TOKEN0=" + user + "&TOKEN1=" + password;
            out.print(params);
            out.flush();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer in_buf = new StringBuffer();
            int len;
            char[] buf = new char[1024];
            while ((len = in.read(buf, 0, buf.length)) != -1) {
                in_buf.append(buf, 0, len);
            }

            // Get the cookie and remove extra characters.  The thing in interest
            // is the session id
            String cookieValue = null;
            int index = cookie.indexOf("=");
            int index1 = cookie.indexOf(";");
            cookieValue = cookie.substring(index + 1, index1);
            // Get the session id
            cookieValue = convertCookie(cookieValue);

            return cookieValue;
        } catch (Exception e) {
            System.out.println("Authentication failed : " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {}
                in = null;
            }
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
            if (out != null) {
                out.flush();
                out.close();
                out=null;
            }
        }
    }

    private String convertCookie(String cookie) {
        while (true) {
            int temp = cookie.indexOf("%25");
            if (temp == -1) {
                break;
            } else {
                String newCookie = cookie.substring(0, temp) + "%" +
                    cookie.substring(temp+3);
                cookie = newCookie;
            }
        }

        while (true) {
            int temp = cookie.indexOf("%2b");
            if (temp == -1) {
                return cookie;
            } else {
                String newCookie = cookie.substring(0, temp) + "+" +
                    cookie.substring(temp+3);
                cookie = newCookie;
            }
        }
    }

    public void login() {
        try {
            fac = new CollaborationSessionFactory();
        } catch(Exception e) {
            e.printStackTrace();
        }

        long start = System.currentTimeMillis();
        _session = new CollaborationSession[numServers][];
        
        for(int sIndex = 0; sIndex < numServers; sIndex++) {
            printlog("Login users from " + START_USER[sIndex] + "to " + END_USER[sIndex] + " on " + DOMAIN[sIndex]);
            _session[sIndex] = new CollaborationSession[OFFSET[sIndex]];
          
            for (int uIndex = 0 ; uIndex < OFFSET[sIndex] ; uIndex++) {
                try {
                    String uid = Integer.toString(START_USER[sIndex] + uIndex);
                    loginUser(uid, uIndex, sIndex);
                    numUsers++;
                } catch(CollaborationException e) {
                    e.printStackTrace();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        long end = System.currentTimeMillis();

        printlog(numUsers + " Users logged in, Time taken = " + round((end -start)/1000) + " Sec");
    }

    public void joinBigConference() {

        printlog("Start joining big conference");
        long start = System.currentTimeMillis();
        for(int sIndex = 0; sIndex < numServers; sIndex++) {

            for (int uIndex = 0 ; uIndex < OFFSET[sIndex] ; uIndex++) {

               try {

                    CollaborationSession session = _session[sIndex][uIndex];

                    ConferenceService service = session.getConferenceService();

                    String bigConferenceName = "loadbigconf" + "@muc." + session.getPrincipal().getDomainName();
                    
                    Conference conf = service.getPublicConference(bigConferenceName);
                    if (conf != null) {
                   
                        String uid = session.getPrincipal().getUID();

                        if (LoadConference.isInitiator(uid, conf) == false) {

                            LoadConference c = new LoadConference(session, conf, timer); 

                        }
                    } else {
                        printlog("Big conference does not exist");

                    }
                } catch(CollaborationException e) {
                    e.printStackTrace();
                } catch(Exception e) {
                    e.printStackTrace();
               }

           } 
        }    
        long end = System.currentTimeMillis();

        printlog(numUsers + " Users joined big conference, Time taken = " + round((end -start)/1000) + " Sec");
    }
    
            

    public void loginUser(String uid, int uIndex, int sIndex) throws CollaborationException
    {
        printlog("Log in user " + uid);

	CollaborationSession session;

	Object trans = LoadStatistics.startTransaction(AUTH_SERVICE_NAME);

        if (sso_server == null) {
            session = fac.getSession(SERVER[sIndex], uid,
				     password, this);

	} else {
            URL initurl;
            URL loginurl;
            try {
                // this url will be used to get the cookie
                initurl = new URL("http://" + sso_server + 
                                  "/amserver/login?module=LDAP" + "&org=o%3d" + 
                                  org_name + "," + "o%3d" + root_suffix);
                // this is the login url to authenticate and get a session
                loginurl = new URL("http://" + sso_server + 
                                   "/amserver/login?module=LDAP");
            } catch(java.net.MalformedURLException e) {
                e.printStackTrace();
                throw new CollaborationException("Malformed URL");
            }
            // first get the iPlanetDirectoryPro cookie
            String cook = getCookie(initurl);
            String sid = getSessionId(loginurl, cook, uid, "iplanet");
            session = fac.getSession(SERVER[sIndex], 
				     uid, sid, this);
        }

	ConferenceService cService = session.getConferenceService();
        cService.initialize(this);

	PresenceService pService = session.getPresenceService();
        pService.initialize(this);

	NotificationService nService = session.getNotificationService();
        nService.initialize(this);

	PersonalStoreService psService = session.getPersonalStoreService();
        psService.initialize(this);

	_session[sIndex][uIndex] = session;
	_sessionMap.put(session.getPrincipal().getUID(), session);

	LoadStatistics.incrementResourceOrder(SESSION_RESOURCE_NAME);
	LoadStatistics.endTransaction(trans);

	// publish initial presence
        PresenceTuple pt = new PresenceTuple("im", uid, 
                                             PresenceService.STATUS_OPEN);
        Presence p = new Presence(uid);
        p.addTuple(pt);
        pService.publish(p);

    }


    class Relogin extends Thread {
	long time, down_time;
   
	Relogin(long time, long down_time) {
	    this.time = time;
	    this.down_time = down_time;
	}
 
	public void run() {    

	    Generate.debuglog("Relogin starting");
            int sIndex;
            int uIndex;

	    while(true) {
		try {

                    CollaborationSession session = null;
                    do {
                        sIndex = getRand(0,numServers);
                        uIndex =
                            getRand(0, _session[sIndex].length);
                        session = _session[sIndex][uIndex];
                        if (( session != null) && 
                            (LoadConference.isInitiator(session.getPrincipal().getUID()))) {
                           session = null;
                         
                        }
                    } while(session == null);

                    String uid = session.getPrincipal().getUID();
                    updateReloginUid(uid);
                    _sessionMap.remove(uid);
                    session.logout();
                    printlog("Logout user " + uid);

		    LoadStatistics.decrementResourceOrder(SESSION_RESOURCE_NAME);
		    // schedule relogin
		    TimerTask task = new WorkerTimerTask(new ReloginRunnable(uid, uIndex, sIndex));
		    timer.schedule(task, down_time);

		    // take a break
		    Thread.sleep(time);

		} catch(CollaborationException e) {
		    e.printStackTrace();
		} catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}
    }
   
    class ReloginRunnable implements Runnable {
        String uid;
        int uIndex, sIndex;

        ReloginRunnable(String uid, int uIndex, int sIndex) {
            this.uid = uid;
            this.uIndex = uIndex;
            this.sIndex = sIndex;
        }

	public void run() {
	    try {
		loginUser(uid, uIndex, sIndex);
                updateReloginUid(null);
	    } catch(Exception e) {
                e.printStackTrace();
            }
	}
    }

    class ConferenceStartRunnable implements Runnable {
        String type;
        int numParticipants;
        long duration;
        long delay;
	
        ConferenceStartRunnable(String cType) {
            this.type = cType;

            if (type.equals("private")) {
                if (VARIABLE_CONF) {
                    this.numParticipants = getRand(2, maxNumPrivateParticipants);
                    this.duration = getRand(Math.min(maxPrivateRoomDuration, maxWaitTime), maxPrivateRoomDuration);
                } else {
                    this.numParticipants = maxNumPrivateParticipants;
                    this.duration = maxPrivateRoomDuration;
                }

                this.delay = newPrivateConferenceDelay;

            } else if (type.equals("public")) {
                if (VARIABLE_CONF) {
                    this.numParticipants = getRand(2, maxNumPublicParticipants);
                    this.duration = getRand(Math.min(maxNumPublicParticipants, maxWaitTime), maxPublicRoomDuration);
                } else {
                    this.numParticipants = maxNumPublicParticipants;
                    this.duration = maxPublicRoomDuration;
                }

                this.delay = newPublicConferenceDelay;

            } else if (type.equals("big")) {

                this.numParticipants = numUsers;
                //this.duration = hours * 3600000;
                //this.delay = 300000; // 5 minutes

            } else { debuglog("no conference type specified"); }

            debuglog(" duration: " + duration + " delay: " + delay + " participants: " + numParticipants);
            
        }

	public void run() {
        long minWait;
        long maxWait;
        long numMessagesToSend;

        if (!BIG_CONF) {

	// schedule the next conference start
	TimerTask task = new WorkerTimerTask(this);
	timer.schedule(task, delay);
        }

	try {

	    Set uidSet = getRandomUIDSet(numParticipants - 1);
	    CollaborationSession session = getRandomSession();

            if (!BIG_CONF) {
	        // reset wait times based on conference participation
                minWait = minWaitTime * numParticipants;
                maxWait = maxWaitTime * numParticipants;
    
	        // set number of messages to send to a conference 
	        // based on duration in ms divided by average time between
	        // 2 postings
                numMessagesToSend = (2 * duration) / (minWait + maxWait);


                // verify that the initiator is not also an invitee or is also
                // an initiator of a conference in progress
                int count = 0;
                while ((uidSet.contains(session.getPrincipal().getUID())) ||
                    (LoadConference.isInitiator(session.getPrincipal().getUID()))) {
                    if (count > numUsers) {
                        debuglog("Too many conferences in progress");
                        return;
                    }
                    count++;
                    debuglog("Get another session: " + session.getPrincipal().getUID());
	            session = getRandomSession();
	        }

                if (type.equals("private")) {

                    debuglog("Creating private conference: initiated by: " + session.getPrincipal().getUID() + "numMessagesToSend: " + numMessagesToSend + "numParticipants: " + numParticipants);

		    LoadConference c = new LoadConference(type, session, null, numMessagesToSend, minWait, maxWait, uidSet, timer);

                } else if (type.equals("public")) {
                    debuglog("Creating public conference: initiated by: " + session.getPrincipal().getUID() + "numMessagesToSend: " + numMessagesToSend + "numParticipants: " + numParticipants);

                    String conferenceName = "loadPublicConf-" + StringUtility.getLocalPartFromAddress(session.getPrincipal().getUID()) + "@muc." + session.getPrincipal().getDomainName();

		    LoadConference c = new LoadConference(type, session, conferenceName, numMessagesToSend, minWait, maxWait, uidSet, timer);

               }

            } else { // big conference 
                minWait = minWaitTime;// interval between message sets
                maxWait = maxWaitTime;  // when to leave conference
                numMessagesToSend = 1; // need to make this a option

                if (NO_INVITES) {
                    session = findSession(bigConfOwner);
                }

                if (session != null) {
                
                    debuglog("Big conference: initiated by: " + session.getPrincipal().getUID() + "numMessagesToSend: " + numMessagesToSend + "numParticipants: " + numParticipants);
		
                    String conferenceName = "loadbigconf" + "@muc." + session.getPrincipal().getDomainName();

                    LoadConference c = new LoadConference(type, session, conferenceName, numMessagesToSend, minWait, maxWait, uidSet, timer);

                 } else {
                     printlog("bigconference creator not member of this instance");
                 }
            }

	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

    }


    class PresenceUpdateRunnable implements Runnable 
    {

	public void run() {	    
	    Generate.debuglog("PresenceUpdates starting");
	    Presence presence;

	    // schedule the next presence update
	    TimerTask task = new WorkerTimerTask(this);
	    timer.schedule(task, presenceUpdateDelay);

	    Object trans = 
		LoadStatistics.startTransaction(PRESENCE_SERVICE_NAME);
	    
            try {
		CollaborationSession session = Generate.getRandomSession();
		PresenceService pservice = session.getPresenceService();
                String uid = session.getPrincipal().getUID();
		presence = pservice.fetchPresence(uid);
		
                PresenceTuple pt = (PresenceTuple)presence.getTuples().iterator().next();
                String status = pt.getStatus();
                if (status.equals(PresenceService.STATUS_AWAY)) status = PresenceService.STATUS_BUSY;
                else if (status.equals(PresenceService.STATUS_BUSY)) status = PresenceService.STATUS_IDLE;
                else if (status.equals(PresenceService.STATUS_IDLE)) status = PresenceService.STATUS_OPEN;
                else if (status.equals(PresenceService.STATUS_OPEN)) status = PresenceService.STATUS_AWAY;
                pt = new PresenceTuple("im", uid, status);
                Presence p = new Presence(uid);
                p.addTuple(pt);
                pservice.publish(p);
                Generate.printlog("Status for user " + uid + " changed");
		
            } catch(TimeoutException e) {
                // server is slow - continue
                e.printStackTrace();
            } catch(CollaborationException e) {
                // should not get this
                e.printStackTrace();
	    }

	    LoadStatistics.endTransaction(trans);
	    
	}
    }


    class SendNotificationRunnable implements MessageStatusListener, Runnable
    {
	public void run() {
	    //Generate.debuglog("Notification starting");

	    // schedule the next presence update
	    TimerTask task = new WorkerTimerTask(this);
	    timer.schedule(task, sendNotificationDelay);

	    Object trans = 
		LoadStatistics.startTransaction(MESSAGE_SERVICE_NAME);
	    
	    try {
		CollaborationSession sender = Generate.getRandomSession();
		NotificationService nService =
		    sender.getNotificationService();
		String senderUID = sender.getPrincipal().getUID();
		
		Message msg = nService.createMessage();
		MessagePart part = msg.newPart();
		part.setContent("This is the test message");
		msg.addPart(part);
		for(int i = 0; i < numRecipients; i++) {
		    CollaborationSession rcpt = Generate.getRandomSession();
		    String rcptUID = rcpt.getPrincipal().getUID();
		    msg.addRecipient(rcptUID);
		}
		
		nService.sendMessage(msg, this);
		
	    } catch(Exception e) {
		e.printStackTrace();
	    }

	    LoadStatistics.endTransaction(trans);

	}

	public void onReply(Message message) {
	}
	
	public boolean onReceipt(String destination, int deliveryStatus) {
	    return false;
	}
  
    }
   

    
    public void startThreads() {
	
        if (!NO_UPDATES) {
	    presenceUpdateDelay = (hours * 3600000)/ (numUsers * (updates - relogin));

	    // schedule the first presence update
	    TimerTask task = 
		new WorkerTimerTask(new PresenceUpdateRunnable());
	    timer.schedule(task, 0);
        }

        if (relogin > 0) {
            long relogin_time = (hours * 60 * 60 * 1000)/(numUsers * relogin);
            long down_time = (hours * 60 * 60 * 1000)* (100 - pul) / (100 * relogin ); 
            Relogin r = new Relogin(relogin_time, down_time);
            r.start();
        }

        if ((notifications > 0) && (numRecipients > 0)) {
	    sendNotificationDelay = (hours * 3600000) / (numUsers * notifications);
	    TimerTask task = 
		new WorkerTimerTask(new SendNotificationRunnable());
	    timer.schedule(task, 0);
        }

        if (!NO_CONF) {

            if (!BIG_CONF) {

                if (privateRooms > 0) {
                    newPrivateConferenceDelay = (hours * 3600000) / (numUsers * privateRooms);
                }
                
                if (publicRooms > 0) { 
                    newPublicConferenceDelay = (hours * 3600000) / (numUsers * publicRooms);
                }
            }

            for (Iterator i = conferenceTypes.iterator(); i.hasNext(); ) {

	        // schedule the first conference start
	        TimerTask task = 
		    new WorkerTimerTask(new ConferenceStartRunnable((String)i.next()));
	        timer.schedule(task, 0);
                if (!BIG_CONF) {
                    try {
                        // pausing between conference starts
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }

    static CollaborationSession getRandomSession() 
    {
	for (;;) {
            try {
	        int iServer = Generate.getRand(0,numServers);
	        int iUser = Generate.getRand(0, _session[iServer].length);
	        CollaborationSession s =_session[iServer][iUser];
	        if (s != null) {
		    String uid = s.getPrincipal().getUID();
                    if (! checkReloginUid(uid)) {
                        return s; 
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    static Set getRandomUIDSet(int size)  throws Exception 
    {
	HashSet uidSet = new HashSet();
	while (uidSet.size() < size) {
	    int iServer = Generate.getRand(0,numServers);
	    int iUser = Generate.getRand(0, _session[iServer].length);
	    CollaborationSession s = _session[iServer][iUser];
	    if (s != null) {
		String uid = s.getPrincipal().getUID();
                if (! checkReloginUid(uid)) {
		    uidSet.add(uid);
                }
	    }
	}
	return uidSet;
    }

    protected static synchronized void printMessage(Message message) {
        // display the message
        printlog("Subject: " + message.getHeader("subject"));
        printlog("Content-type: " + message.getContentType());
        MessagePart[] parts = message.getParts();
        for (int i = 0 ; i < parts.length ; i++) {
            printlog("Part " + i);
            printlog("    Content-type: " + parts[i].getContentType());
            try {
                BufferedReader br=new BufferedReader(new InputStreamReader(parts[i].getInputStream()));
                String str;
                while((str=br.readLine()) != null) printlog(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void printlog(String message) {
        Calendar c = Calendar.getInstance();
        StringBuffer date = new StringBuffer();
        date.append( "[");
        int day = c.get(Calendar.DATE);
        if(day < 10) date.append('0');
        date.append(day);

        date .append("/");
        int month = c.get(Calendar.MONTH);
        date.append(shortMonth[month]);

        date.append("/");
        int year = c.get(Calendar.YEAR);
        date.append(year);

        date.append(":"); 
        int hour = c.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) date.append('0');
        date.append(hour);

        date.append(":");
        int min = c.get(Calendar.MINUTE);
        if (min < 10) date.append('0');
        date.append(min);

        date.append(":");
        int sec =  c.get(Calendar.SECOND);
        if (sec < 10) date.append('0');
        date.append(sec);
        date.append("]");

        ps.println(date + " " +  message);
    }

    public static synchronized void debuglog(String message) {
        if (DEBUG_ON) {
            System.out.println("[Load] " + message);
        }
    }

    public static String round(double dl) {
        String f = (new Double(dl)).toString();
        if ((f.indexOf('.') == -1) || (f.indexOf('.') + 3) >= f.length()) return f;
        return f.substring(0,f.indexOf('.') + 3);
    }
    
    public static int getRand(int min, int max) {
        int num = rand.nextInt(max); 
        if(num <= -1) num *= -1;
        num %= (max - min);
        num += min;
        return num;
    }
    
    public static long getRand(long min, long max) {
        long num = rand.nextLong(); 
        if(num <= -1) num *= -1;
        num %= (max - min);
        num += min;
        return num;
    }
    
    public void readArgs(String arg[]) {
        String contractFile = arg[0];
        String factory = "default";
        int capacity = -1;
        int concurrency = -1;
	long statsPeriod = 0;

        try {

            // read contract file
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(contractFile)));
            ArrayList array = new ArrayList();
            for(;;) {
                String sContract = reader.readLine();
                if (sContract == null) break;
                if (sContract.startsWith("#")) continue;
                numServers++;
                array.add((Object)sContract);
            }
            SERVER = new String[numServers];
            DOMAIN = new String[numServers];
            START_USER = new int[numServers];
            END_USER = new int[numServers];
            OFFSET = new int[numServers];

            for(int i = 0;i < numServers; i++) {
                String sContract = (String)array.get(i);
                StringTokenizer st = new StringTokenizer(sContract, " ");
                SERVER[i] = st.nextToken();
                DOMAIN[i] = (new HostPort(SERVER[i], 5222)).getHostName();
                START_USER[i] = Integer.parseInt(st.nextToken());
                OFFSET[i] = Integer.parseInt(st.nextToken());
                END_USER[i] = START_USER[i] + OFFSET[i] - 1;
                for(;;) {
                    if (!st.hasMoreElements()) break;
                    String token = st.nextToken();
                    if(token.equals("-wait")) {
                        minWaitTime = Integer.parseInt(st.nextToken()) * 1000;
                        maxWaitTime = Integer.parseInt(st.nextToken()) * 1000;
                        if (maxWaitTime < minWaitTime) {
				printlog("min wait should be less than or equal to max wait");
				minWaitTime = 1000;
				maxWaitTime = 10000;
			}
                    } else if(token.equals("-aml")) {
                        MSG_LISTEN = true;
                    } else if(token.equals("-log")) {
                        LOG_FILE = st.nextToken();
                    } else if (token.equals("-stats:period") ||
                         token.equals("--stats:period")) {
                            statsPeriod = Long.parseLong(st.nextToken()) * 1000;
                    } else if(token.equals("-apilog")) {
                         APILOG = true;
                         APILOG_FILE = st.nextToken();
                    } else if (token.equals("-factory")) {
                         factory = st.nextToken();
                    } else if(token.equals("-capacity")) {
                         capacity = Integer.parseInt(st.nextToken());
                    } else if(token.equals("-concurrency")) {
                        concurrency = Integer.parseInt(st.nextToken());
                    } else if(token.equals("-updates")) {
                        updates = Integer.parseInt(st.nextToken());

                    // vary conference participants and duration
                    } else if(token.equals("-variable")) {
                        VARIABLE_CONF = true;

                    // number of private conference rooms
                    } else if(token.equals("-rooms")) {
                        privateRooms = Integer.parseInt(st.nextToken());

                    // private max duration
                    } else if(token.equals("-duration")) {
                        maxPrivateRoomDuration = Integer.parseInt(st.nextToken()) * 60000;  // in minutes

                    // private max participants
                    } else if(token.equals("-people")) {
                        maxNumPrivateParticipants = Integer.parseInt(st.nextToken());
                            
                    // public rooms
                    } else if(token.equals("-pub-rooms")) {
                        publicRooms = Integer.parseInt(st.nextToken());

                    // public max duration
                    } else if(token.equals("-pub-duration")) {
                        maxPublicRoomDuration = Integer.parseInt(st.nextToken()) * 60000;  // in minutes
                    // public max participants
                    } else if(token.equals("-pub-people")) {
                        maxNumPublicParticipants = Integer.parseInt(st.nextToken());
                            
                    } else if (token.equals("-relogin")) {
                        relogin = Integer.parseInt(st.nextToken());
                    } else if(token.equals("-notification")) {
                        notifications = Integer.parseInt(st.nextToken());
                    } else if(token.equals("-recipient")) {
                        numRecipients = Integer.parseInt(st.nextToken());
                    } else if(token.equals("-pul")) {
                        pul = Integer.parseInt(st.nextToken());
                    } else if(token.equals("-day")) {
                        hours = Integer.parseInt(st.nextToken());
                    } else if(token.equals("-debug")) {
                        DEBUG_ON = true;
                    } else if(token.equals("-quiet")) {
                        QUIET = true;
                    } else if(token.equals("-ssoserver")) {
                        sso_server = st.nextToken();
                    } else if ((token.equals("-org")) || 
                               (token.equals("-orgname"))) {
                        org_name = st.nextToken();
                    } else if (token.equals("-suffix")) {
                        root_suffix = st.nextToken();
                    } else if (token.equals("-noconferences")) {
                        NO_CONF = true;
                    } else if (token.equals("-noupdates")) {
                        NO_UPDATES = true;
                    } else if (token.equals("-bigconferences")) {
                        BIG_CONF = true;
                        privateRooms = 0;
                        publicRooms = 0;
                    } else if (token.equals("-noinvite")) {
                        // join public conf wo in
                        NO_INVITES = true;
                        bigConfOwner = st.nextToken();
                    } else if (token.equals("-passwd")) {
                        password = st.nextToken();
                    } else if(token.equals("-occ_thr_mess_fire")) {
                        occThrForMessFiring = Integer.parseInt(st.nextToken());
                    }
                }
            }
            if (pul > 100) System.out.println("Percentage of users logged in cannot be greater than 100");
            if (relogin > updates) System.out.println("Relogins cannot be greater than presence updates");
            if (!LOG_FILE.equals("STD_OUT")) ps = new PrintStream(new FileOutputStream(LOG_FILE,true));

            if ((APILOG) && (!APILOG_FILE.equals(""))) {
                System.setProperty("com.iplanet.im.client.api.debug", "true");
                System.setProperty("com.sun.im.xmpp.log", "debug");
                System.setProperty("org.netbeans.lib.collab.xmpp.log", "debug");
                Logger logger = LogManager.getLogger("org.netbeans.lib.collab");
                new PropertyConfigurator().configure(APILOG_FILE);
            }

            if (!factory.equals("default")) System.setProperty(CollaborationSessionFactory.systemProperty, factory);
            if (concurrency !=-1) System.setProperty("com.iplanet.im.client.api.concurrency", Integer.toString(concurrency));
            if (capacity != -1) System.setProperty("com.iplanet.im.mux.capacity", Integer.toString(capacity));
    
            if (privateRooms > 0) {
                conferenceTypes.add("private");
                if (maxNumPrivateParticipants == -1) {
                    maxNumPrivateParticipants = 2;
                }
            }

            if (publicRooms > 0 ) {
                conferenceTypes.add("public");
                if (maxNumPublicParticipants == -1) {
                    maxNumPublicParticipants = 3;
                }
            } 

            if (BIG_CONF) {
                conferenceTypes.add("big");
                if (maxNumPublicParticipants == -1) {
                   maxNumPublicParticipants = 1000;
                }
            }

            if (statsPeriod > 0) {
                LoadStatistics.startPrintLoop(ps, statsPeriod);
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main (String args[]) {
        Generate i = new Generate();
        i.readArgs(args);
        i.login();
        
        if (occThrForMessFiring == 0) {
            occThrForMessFiring = numUsers;
        }
        
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        i.startThreads();
        if ((i.BIG_CONF) && (i.NO_INVITES)) {
            try {
                Thread.sleep(2000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            i.joinBigConference();
        }
    }

    private synchronized void updateReloginUid(String uid) 
    {

        reloginUid = uid;
    }

    private static synchronized boolean checkReloginUid(String uid)
    {

        if (reloginUid == null) return false;
        if (reloginUid.equals(uid)) return true;
        return false;
    }

    /**
     * invoked when an unexpected failure has happened.
     * The listener is no longer
     * valid after this method has been called.
     * @param e describes the nature of the problem
     */
    public void onError(CollaborationException e) {
    }

    public void onError(String pURL, String reason)
    {
    }
    
    /** 
     * invoked when a presence received
     */    
    public void onPresence(Presence p)
    {
        try {
            for (Iterator i = p.getTuples().iterator(); i.hasNext() ; ) {
                PresenceTuple t = (PresenceTuple)i.next();
                printlog("Status change(" + t.getStatus() + ") for "+ t.getPresenceURL());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * invoked when a subscribe request is received
     */    
    public void onSubscribeRequest(Presence p)
    {
    }
    
    /**
     * invoked when a subscribe request is approved
     */    
    public void onSubscribed(Presence p)
    {
    }
    
    /**
     * invoked when a unsubsribe request is received
     */
    public void onUnsubscribe(Presence p)
    {
    }
    
    /**
     * invoked when a unsubscribe request is approved
     */    
    public void onUnsubscribed(Presence p)
    {
    }


    /**
     * invoked by the provider when requested presence information is
     * received
     *
     * @param presenceInfo presence info (XML)
     */
    public void onPresenceInfo(String presence) {
        try {
            PresenceHelper ph = new PresenceHelper(presence);
            for (Iterator i = ph.getTuples().iterator(); i.hasNext() ; ) {
                PresenceTuple t = (PresenceTuple)i.next();
                printlog("Status change(" + t.getStatus() + ") for "+ t.getPresenceURL());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onEvent(PersonalStoreEvent event) { }
    
    /**
     * invoked when an alert is received
     *
     * Application should use the mehtods of received message to convey
     * reception statuses and responses to the sender.
     * @param message received message
     */
    public void onMessage(Message message) {}


    /**
     * invoked by the provider when no more data will be sent through this
     * listener
     */
    public void onCompletion() {
    }
    

    private CollaborationSession findSession(String uid)
    {

        return (CollaborationSession)_sessionMap.get(uid);
    }

    /**
     * invoked when another user asks this user to join a conference.
     * @param conference conference to which the current user is being invited
     * @param accompanying message.  It typically contains a reason for the invitation.
     */
    public void onInvite(Conference conf,
			 InviteMessage message) 
    {
        try {
	    debuglog("Received invitation from=" + message.getOriginator() + " to=" + (message.getRecipients())[0] + " conf=" + conf.getDestination());
            if (MSG_LISTEN) Generate.printMessage(message);

	    CollaborationSession session = findSession((message.getRecipients())[0]);
	    if (session != null) {

                LoadConferenceParticipant initiator = LoadConference.findInitiator(message.getOriginator());

                if (initiator == null) {
                    debuglog ("Too late: " + message.getOriginator() + " has already left: " + conf.getDestination() + " sending rsvp to decline invitation");
                 message.rsvp(false);
                }
		else {
                    debuglog ("Sending rsvp to accept invitation from: " + message.getOriginator() + " for conference: " + conf.getDestination());
                    LoadConference c = new LoadConference(initiator.getConferenceType(), session, initiator, conf, timer);
                    message.rsvp(true);
                }

	    } else {
		debuglog("Received invitation from=" + message.getOriginator() + " to=" + (message.getRecipients())[0] + " conf=" + conf.getDestination() + " IS NOT ONLINE!!!");
	    }

        } catch(CollaborationException e) {
            e.printStackTrace();
        }
    }

    public boolean onX509Certificate(java.security.cert.X509Certificate[] cert) {
        return true;
    }
    
    
    
}
