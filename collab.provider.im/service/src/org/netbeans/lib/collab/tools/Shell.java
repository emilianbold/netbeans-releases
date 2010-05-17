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
import org.netbeans.lib.collab.util.HTMLConverter;
import org.netbeans.lib.collab.util.StringUtility;
import java.security.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import java.security.Principal;

import org.apache.log4j.*;

/**
 *
 * @author Jacques Belissent
 * @author Rahul Shah
 * @author Vijayakumar Palaniappan
 *
 */
class ShellRegistrationListener implements RegistrationListener {

    /** Creates a new instance of ShellRegistrationListener */
    private Shell _shell;
    private String[] registrationFields = new String[] { RegistrationListener.USERNAME, RegistrationListener.PASSWORD,
                                                        RegistrationListener.FIRST, RegistrationListener.LAST,
                                                        RegistrationListener.NAME, RegistrationListener.EMAIL 
                                                    };
    // private boolean _gatewayRegistration;
    
    public ShellRegistrationListener(Shell shell) {
        _shell = shell;
        // _gatewayRegistration = gatewayRegistration;
    }
    
    public boolean fillRegistrationInformation(java.util.Map fieldValuePairs, String server) {
        System.out.println();
        System.out.println("Please fill the registration fields");
        String fieldName = null;
        String value = null;        
        // Map fieldValuePairs = new HashMap();       
        Set fields = fieldValuePairs.keySet();
        List fieldList = new ArrayList();
       
        for (int i = 0; i < registrationFields.length; i++) {
            if (fields.contains(registrationFields[i])) {
                fieldList.add(registrationFields[i]);
            } 
        } 
         
        for (Iterator iter = fields.iterator(); iter.hasNext();) {
            String field  = (String)iter.next();
            if (!fieldList.contains(field)) {
                fieldList.add(field);
            }
        }                    
        
        for (Iterator iter = fieldList.iterator(); iter.hasNext();) {
            fieldName = (String)iter.next();
            value = _shell.prompt(fieldName + " : ",
            false, (String)fieldValuePairs.get(fieldName), true);
            fieldValuePairs.put(fieldName, value);
        }
         
        synchronized(_shell) {
            _shell.notifyAll();
        }
        // return fieldValuePairs;
        return true;
    }
    
    public void redirected(java.net.URL url, String server) {
        System.out.println("Please follow " + url + " for registration");        
        /*
        synchronized(_shell) {
            _shell.notifyAll();
        }
         */
    }
    
    public void registered(String server) {
        System.out.println("User was successfully registered on " + server); 
        /*
        synchronized(_shell) {
            _shell.notifyAll();
        } 
         */       
    }
    
    public void unregistered(String server) {
        System.out.println("User was successfully unregistered on " + server);
        /*
        synchronized(_shell) {
            _shell.notifyAll();
        }
         */
    }
    
    public void registrationFailed(String errorCondition, String errorText, String server) {
        System.out.println("Registration failed with error condition " + errorCondition + "message: " + errorText);  
        /*
        synchronized(_shell) {
            _shell.notifyAll();
        }
         */
    }
    
    public void unregistrationFailed(String errorCondition, String errorText, String server) {
        System.out.println("UnRegistration failed with error condition " + errorCondition + "message: " + errorText);
        /*
        synchronized(_shell) {
            _shell.notifyAll();
        }
         */
    }
   
    /*
    public boolean userAlreadyRegistered(String user, String server) {
        System.out.println("User " + user + "is already registered with the " + server);
        
        synchronized(_shell) {
            _shell.notifyAll();
        }
        return true;
    }
     */
    
    public void registrationUpdated(String server) {
       System.out.println("Password for  has been successfully changed by the " + server);
       /*
       synchronized(_shell) {
           _shell.notifyAll();
       } 
        */   
    }
    
    public void registrationUpdateFailed(String errorCondition, String errorText, String server) {        
        System.out.println("Registration update has failed with error condition " + errorCondition + " and error message: " + errorText);
        /*
        synchronized(_shell) {
            _shell.notifyAll();
        }
         */
    }
     
}


class ContentStreamListenerImpl implements ContentStreamListener {
    ContentStream _cs;
    ReceiverStreamingProfile _profile;
    public ContentStreamListenerImpl() {
    }

    public ContentStreamListenerImpl(ReceiverStreamingProfile profile) {
        _profile = profile;
    }

    public void setContentStream(ContentStream cs) {
        _cs = cs;
    }
    
    public void closed(int status, String reason) {
        System.out.println("Stream closed. Status -> " + status + " reason -> " + reason);
        System.out.println("Transferred bytes -> " + _cs.getTransferredBytes());
        if (_profile != null)
            System.out.println("Fileintegrity -> " + ((ReceiverFileStreamingProfile)_profile).checkIntegrity());
    }
    
    public void started() {
        System.out.println("Stream started");
    }
}

/** 
 * Command line interface to test the client API.
 */
public class Shell implements MessageStatusListener, InviteMessageStatusListener, 
        SecureSessionListener, PresenceServiceListener , NotificationServiceListener, 
        ConferenceServiceListener, StreamingServiceListener , SecurityListener , 
        AuthenticationListener, PersonalStoreServiceListener
{
            
    //////////////////////////////////////////////////////////////////
    // INSTANCE VARIABLES : BEGIN
    //////////////////////////////////////////////////////////////////

    // sessions
    CollaborationSession _session;
    PresenceService _presenceService;
    NewsService _newsService;
    ConferenceService _conferenceService;
    NotificationService _messageService;
    PersonalStoreService _personalStoreService;
    StreamingService _streamingService;

    static String _defaultDomain ;

    // to keep track of received messages so responses
    // and message statusses can be sent
    Vector _receivedMessages;
    CollaborationSessionFactory _factory;
    CollaborationSessionFactory _compFactory;
    // source to read commands from
    BufferedReader _reader;

    // whether impsh reads from stdin
    boolean interactive;
    
    // contains all open conferences and news channels
    Hashtable _conferences = new Hashtable();
    Hashtable _confnames = new Hashtable();
    Hashtable _polls = new Hashtable();

    CollaborationPrincipal _principal;

    //tells if waiting to read or running a command
    boolean running, waiting;

    // When used with multi user mode the output from multiple im shells are sorted to
    // remove the effect of asynchronously occurring events in the files to be diff'd
    // (.out versus .tso, the diff'd result being in .tso.dif)
    //  However for purposes debugging when tests fail it is
    // helpful to reduce the amount 
    boolean inTestingMode = false;
    final int testModeWaitMsec = 500;
    
    //contains the Identity session ID.
    String sessionId;

    public static boolean _usesso = false;
    private static String defaultServer;
   
    //static Logger logger = LogManager.getRootLogger();

    static {
	/*
        try {
            //PatternLayout layout = new PatternLayout("%d{HH:mm:ss,SSS} %-5p %c [%t] %m%n");
            ConsoleAppender appender = new ConsoleAppender(layout, "System.out");
            NullAppender appender = new NullAppender();
            //logger.setLevel(Level.ERROR);
            logger.addAppender(appender);
        } catch (Exception e) {
            e.printStackTrace();
        }
	*/
        try {
            defaultServer = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            defaultServer = "localhost";
        }
    }

    String currentServer      = null;
    String user        = System.getProperty("user.name");
    String password    = "iplanet";
        

    private final static HTMLConverter htmlConverter = new HTMLConverter();

    //////////////////////////////////////////////////////////////////
    // INSTANCE VARIABLES : END
    //////////////////////////////////////////////////////////////////

    //create a connection using the command line
    public Shell() throws Exception {
        init(true, null);
    }
    
    //create a command line interface to the server
    public Shell(BufferedReader br, boolean multiShells) throws Exception {
        inTestingMode = multiShells;
        init(false, br);
    }

    private void init(boolean interactive, BufferedReader br) throws Exception
    {
        
        // initialize
        this.interactive = interactive;
        if (br != null) {
            _reader = br;
        } else {
            _reader = new BufferedReader(new InputStreamReader(System.in));
        }
        _factory = new CollaborationSessionFactory();
        _receivedMessages = new Vector();

    }
    
    // This is extended in the test program Multiiimtalk. For testing purposes
    // randomly generated conference names means test output cannot
    // be reproduced consitently

    protected String getCanonicalConferenceName(Conference conf) {
        return conf.getDestination();
    }

    // The user creating a conference does not have to always refer to it
    // as a fully qualified domain name. e.g. conf instead of conf@a.b.com

    protected void assignLocalName(String name, String confDest) {
        _confnames.put(name, confDest);
    }

    // If invited to a conference (e.g. conf@a.b.com) whose domain is the same as the principal
    // allow a shortened name (conf) for subsequent reference. If the principal is in another
    //domain (e.g. d.e.com)
    // Then reference must instead be by fully qualified domain name.

    protected void assignLocalNameIfSameDomain(String confDest) {
        if (confDest.endsWith("@" + _principal.getDomainName())) {
            _confnames.put(getLocalPartFromAddress(confDest), confDest);
        }
    }

    /**
     * main command loop
     */
    public void start() {

        while (true) {
            try {
                if (interactive) {
                    System.out.print("\nimp> ");
                }
                
                String s = _reader.readLine();
               
                // prompt has set the waiting flag
                // this is not a new command
                if (_asyncReader.isActive()) {
                    _asyncReader.write(s);
                    continue;
                }
                
                if (s == null) break;
                s = s.trim();
                if (s.equals("")) continue;


                running=true;
                StringTokenizer st = new StringTokenizer(s);
                String cmd = st.nextToken();
                String args[] = new String [ st.countTokens() ];


                int i = 0;
                while (st.hasMoreTokens()) {
                    args[i++] = st.nextToken();
                }
                if (cmd.equals("amc") ||
                    cmd.equalsIgnoreCase("AddMessageConference")) {
                    addConferenceMessage(args);
                } else if (cmd.equals("amnc") ||
                    cmd.equalsIgnoreCase("AddMessageNewsChannel")) {
                    addNewsMessage(args);
                } else if (cmd.equals("send")) {
                    sendMessage(args);
                } else if (cmd.equals("pause")) {
                    Thread.sleep(1000);
                } else if (cmd.equals("stop")) {
                    // just continue keeping the program alive, but just stop 
                    // processing anything more from the input.
                    while (true){
                        Thread.sleep(1000);
                    }
                } else if (cmd.equals("cnc")) {
                    addNewsChannel(args);
                } else if (cmd.equals("snc")) {
                    subscribeNewsChannel(args);
                } else if (cmd.equals("unc")) {
                    unSubscribeNewsChannel(args);
                } else if (cmd.equals("rnc")) {
                    delNewsChannel(args);
                } else if (cmd.equals("ac")) {
                    addConference(args);
                } else if (cmd.equals("apc")) {
                    addPublicConference(args);
                } else if (cmd.equals("jpc")) {
                    joinPublicConference(args);
                } else if (cmd.equals("spc")) {
                    subscribePublicConference(args);
                } else if (cmd.equals("lpc")) {
                    listSubscribedPublicConference(args);
                } else if (cmd.equals("upc")) {
                    unsubscribePublicConference(args);
                } else if (cmd.equals("rc")) {
                    delRoom(args);
                } else if (cmd.equals("mc")) {
                    moderateRoom(args);
                } else if (cmd.equals("lmc")) {
                    listMembersInRoom(args);
                } else if (cmd.equals("ammc")) {
                    addModeratedMessage(args);
                } else if (cmd.equals("login")){
                    login(args);
                } else if (cmd.equals("loginc")) {
                    loginComponent(args);
				} else if (cmd.equals("caps")) {
                    setCapabilities(args);
                } else if (cmd.equals("ru")) {
                    registerUser(args);
                } else if (cmd.equals("uru")) {
                    unregisterUser(args);
                } else if(cmd.equals("cup")) {
                    changeUserPassword(args);
                } else if (cmd.equals("rgw")) {
                    registerGateway(args);     
                } else if (cmd.equals("urgw")) {
                    System.out.println("It is unregister gateway");
                    unregisterGateway(args);
                } else if (cmd.equals("lsgw")) {
                    listGateways(args);
                } else if (cmd.equals("lsrgw")) {
                    listRegisteredGateways(args);
                } else if (cmd.equals("dmnc")){
                    delNewsMessage(args);
                } else if (cmd.equals("lmnc")) {
                    listMessages(args);
                } else if (cmd.equals("lsnc")) {
                    listNewsChannels(args);
                } else if (cmd.equals("lsmnc")) {
                    listManagedNewsChannels(args);   
                } else if (cmd.equals("gsnc")) {
                    getSubscribedNewsChannels(args);                
                } else if (cmd.equals("lsnp")) {
                    listNewsPrivileges(args);
                } else if (cmd.equals("snp")) {
                    setNewsPrivileges(args);
                } else if(cmd.equals("gnc")) {
                    getNewsConfiguration(args);
                } else if (cmd.equals("sunp")) {
                    setNewsPrivilege(args);
                } else if (cmd.equals("gunp")) {
                    getNewsPrivilege(args);                
                } else if (cmd.equals("gndp")) {
                    getDefaultNewsPrivilege(args);
                } else if (cmd.equals("sndp")) {
                    setDefaultNewsPrivilege(args);
                } else if (cmd.equals("lq")) {
                    //queues(args);
                } else if (cmd.equals("lsc")) {
                    listPublicConferences(args);
                } else if (cmd.equals("lu")) {
                    //listUsers(args);
                } else if (cmd.equals("lg")) {
                    //listGroups(args);
                } else if (cmd.equals("lc")) {
                    quitRoom(args);
                } else if (cmd.equals("cacl")) {
                    editConferenceAcl(args);
                } else if (cmd.equals("dcacl")) {
                    displayConferenceAcl(args);
                } else if (cmd.equals("status")) {
                    sendMessageStatus(args);
                } else if (cmd.equals("reply")) {
                    sendMessageReply(args);
                } else if (cmd.equals("getdp")) {
                    getDestinationProperty(args);
                } else if (cmd.equals("setdp")) {
                    setDestinationProperty(args);
                } else if (cmd.equals("rmdp")) {
                    removeDestinationProperty(args);
                } else if (cmd.equals("s")) {
                    //saveSettings(args);
                } else if (cmd.equals("ss")) {
                    startStream(args);
                } else if (cmd.equals("h") || cmd.equals("help")) {
                    help();
                } else if (cmd.equals("logout")) {
                    logout();     
                } else if (cmd.equals("e") ||
                           cmd.equals("exit") ||
                           cmd.equals("quit")) {
                    logout();
                    running=false;
                    break;
                } else if (cmd.equals("iuc")) {
                    invite(args);
                } else if (cmd.equals("checkacl")) {
                    //checkacl(args);
                } else if (cmd.equals("ls")) {
                    //listServers(args);
                } else if (cmd.equals("ss")) {
                    //setServer(args);
                } else if (cmd.equals("ppi")) {
                    publishPresenceInfo(args);
                } else if (cmd.equals("ppe")) {
                    publishPersonalEvent(args);
                } else if (cmd.equals("fpi")) {
                    fetchPresenceInfo(args);
                } else if (cmd.equals("fp")) {
                    fetchPresence(args);
                } else if (cmd.equals("spi")) {
                    subscribePresenceInfo(args);
                } else if (cmd.equals("uspi")) {
                    unsubscribePresenceInfo(args);
                } else if (cmd.equals("poll")) {
                    sendPollMessage(args);
                } else if (cmd.equals("pollr")) {
                    sendPollResponse(args);
                } else if (cmd.equals("acon")) {
                    addContact(args);
                } else if (cmd.equals("alcon")) {
                    addLegacyContact(args);
                } else if (cmd.equals("rcon")) {
                    removeContact(args);
                } else if (cmd.equals("rlcon")) {
                    removeLegacyContact(args);
                } else if (cmd.equals("search")) {
                    searchEntries(args);
                } else if (cmd.equals("agrp")) {
                    addContactGroup(args);
                } else if (cmd.equals("rgrp")) {
                    removeContactGroup(args);
                } else if (cmd.equals("afol")) {
                    addContactFolder(args);
                } else if (cmd.equals("rfol")) {
                    removeContactFolder(args);
                } else if (cmd.equals("nfol")) {
                    renameContactFolder(args);
                } else if (cmd.equals("lcon")) {
                    listContacts(args);
                } else if (cmd.equals("egrp")) {
                    expandContactGroup(args);
                } else if (cmd.equals("lpro")) {
                    listProfile(args);
                } else if (cmd.equals("gpro")) {
                    getProfile(args);
                } else if (cmd.equals("cpro")) {
                    changeProfile(args);
                } else if (cmd.equals("rpro")) {
                    removeProfile(args);
                } else if (cmd.equals("asub")) {
                    addSubscription(args);
                } else if (cmd.equals("rsub")) {
                    removeSubscription(args);
                } else if (cmd.equals("lsub")) {
                    listSubscriptions(args);
                } else if (cmd.equals("lpl")) {
                    listPrivacyList(args);
                } else if (cmd.equals("gpl")) {
                    getPrivacyList(args);
                } else if (cmd.equals("spl")) {
                    setPrivacyList(args);
                } else if (cmd.equals("rpl")) {
                    removePrivacyList(args);
                } else if (cmd.equals("?")) {
                    help();
                } else if (cmd.equalsIgnoreCase("y") ||
                           cmd.equalsIgnoreCase("n") ||
                           cmd.equalsIgnoreCase("no") ||
                           cmd.equalsIgnoreCase("yes")) {
                    _asyncReader.write(cmd);
                } else {
                    System.out.println("error: unknown command : " + cmd);
                    if (interactive == true) help();
                }
                running=false;
                
            } catch (IOException ioe) {
                ioe.printStackTrace();
                break;

            } catch (Exception e) {
                running=false;
                System.out.println("Error " + e.toString());
                e.printStackTrace();
            }
            
        }
        _factory.close();
        if (_compFactory != null) {
            _compFactory.close();
        }
    }

    void help() {
        System.out.println("");
        System.out.println("login = start a session with presence, conference,");
        System.out.println("        message, and news channel services");
        System.out.println("");
        System.out.println("logout = logout of the current session");
        System.out.println("caps   = set capabilities and features");
        System.out.println();
        System.out.println(" --- PRESENCE --- ");
        System.out.println("ppi  = publish availability      fpi  = fetch availability");
        System.out.println("spi  = subcribe to availability  uspi = unsubcribe to availability");
        System.out.println("sacl = show presence rule acl    pacl = edit presence rule acl");
        System.out.println("ppe  = publish personal event");
        System.out.println("");
        System.out.println(" --- CONFERENCE ---");
        System.out.println("amc  = add or send a message     iuc = invite user");
        System.out.println("ac   = setup conference          rc  = remove conference");
        System.out.println("jpc  = join public conference    lc  = leave conference");
        System.out.println("lsc  = list public conferences   apc = add public conference");
        System.out.println("spc  = subs public conferences   upc = unsu public conference");
        System.out.println("lpc  = list subscribed public conferences");
        System.out.println("cacl = edit conference access rules dcacl = display conference acls");
        System.out.println("mc   = moderate conference room  ammc = add moderated message to room");
        System.out.println("lmc  = list members in a public conference room");
        System.out.println("");
        System.out.println(" --- NEWS --- ");
        System.out.println("cnc  = create news channel       rnc = remove news channel");
        System.out.println("snc  = subscribe news channel    unc = unsubscribe news channel");
        System.out.println("lsnc = list news channels        amnc = post a message");
        System.out.println("lmnc = list messages             dmnc = delete message");
        System.out.println("lsmnc = list managed News channels      gsnc = get subscribed news channels");       
        System.out.println("snp = set News access control list      lsnp = get news access control list"); 
        System.out.println("sndp = set default access level of newschannel gndp = get default acess level of newschannel");
         System.out.println("sunp = set Privilege of current user   gunp = get privilege of current user"); 
        System.out.println("");
        System.out.println(" --- NOTIFICATION --- ");
        System.out.println("send = send a message to users");
        System.out.println("status = send message status     reply reply last message");
        System.out.println("poll = send poll message         pollr send poll response");
        System.out.println("");
        System.out.println(" --- PERSONAL STORE --- ");
        System.out.println("lcon = list contacts\t\t");
        System.out.println("rcon = remove contact            acon = add contact");
        System.out.println("rlcon = remove legacy contact    alcon = add legacy contact");
        System.out.println("rgrp = remove contact group      agrp = add contact group");
        System.out.println("rfol = remove contact folder     afol = add contact folder");
        System.out.println("nfol = rename contact folder");
        System.out.println("search = search entries");
        System.out.println("lpro = list profile info         rpro = remove profile info");
        System.out.println("gpro = get profile info          cpro = change profile info");
        System.out.println("asub = subscribe to destination  rsub = unsubscribe");
        System.out.println("lsub = list subscriptions");
        System.out.println();
        System.out.println(" --- ARCHIVE TEST --- ");
        System.out.println("rat = run archive tests\t\t");
        System.out.println();
        System.out.println(" --- Privacy List --- ");
        System.out.println("lpl = List Privacy Lists\t\t");
        System.out.println("gpl = Get Privacy List\t\t");
        System.out.println("spl = Set Privacy List\t\t");
        System.out.println("rpl = Remove Privacy List\t\t");
        System.out.println();
        System.out.println("---- Gateway--------");
        System.out.println("rgw = register with gateway      urgw = unregister gateway");
        System.out.println("lsgw = list all available gateways");
        System.out.println();
        System.out.println("---- Streaming Service--------");
        System.out.println("ss = start the stream");
        System.out.println();
        System.out.println("e = exit");
        System.out.println("h = print this help");
        System.out.println("for help on a command type \"command ?\"");
    }

    ///////////////////////////////////////////////////////////////
    // PROMPT UTILS : BEGIN
    ///////////////////////////////////////////////////////////////
              
    // synchronous vs. asynchronous prompting
    //
    // * synchronous: information is asked
    // preparation of making a request to the server.  At this point the 
    // reader is not reading (blocked on a read).  So it is possible
    // for the prompting method to add
    //
    // * asynchronous: information is asked in response of a request
    // sent by the server.  At this point the reader is likely being used.
    // In this case the prompt method waits for the next line to be
    // read.  The main redeloop will then detect the fact that an
    // asynchronous prompt is ongoing and treat the next line as a 
    // response to this prompt and not as a new command.
    //
    // Note: we can get into some issues when a server request is
    // at the same time a the shell is prompting the user for additional
    // request information.  The latter in this case takes precedence
    // The assumption is the user will complete the request before 
    // answering the asynchronous server request.

    class AsyncReader
    {
        private boolean _isActive = false;
        private LinkedList _values = new LinkedList();
        public synchronized void write(String val)
        {
            // System.out.println("DEBUG AsyncReader updated: " + val);            
            _values.add(val);
            notify();
        }
        public boolean isActive() { return _isActive; }
        public synchronized String read(String ask)
        {
            String result = null;
            _isActive = true;
            
            System.out.print(ask);

            while (_values.size() == 0) {
                try {
                    wait();
                } catch (Exception e) {}
                
            }
            result = (String)_values.removeFirst();
            _isActive = false;
            //System.out.println("DEBUG AsyncReader returns: " + result);
            return result;
        }
    }
    AsyncReader _asyncReader = new AsyncReader();

    boolean getYesOrNo(String ask, boolean async) throws Exception 
    {
        boolean done = false;
        
        while (!done){
            String s = prompt(ask, false, async);
            if (s.equals("y") || s.equalsIgnoreCase("yes")) {
                return true;
            } else if (s.equals("n") || s.equalsIgnoreCase("no")) {
                return false;
            }
        }
        return false;
    }
    
    public boolean isRunning() { return running; }
    public boolean isWaiting() { return waiting; }

    protected String prompt(String ask, boolean newline) {
        return prompt(ask, newline, "", false);
    }

    protected String prompt(String ask, boolean newline, boolean async) {
        return prompt(ask, newline, "", async);
    }
          
    protected String prompt(String ask, boolean newline, String defaultValue) {
        return prompt(ask, newline, defaultValue, false);
    }

    protected String prompt(String ask, boolean newline, String defaultValue,
                            boolean async)
    {
        String ret = defaultValue;
        waiting = true;
        if (async) {
            ret = _asyncReader.read(ask);
        } else {
            if (newline) {
                System.out.println(ask);
            } else {
                System.out.print(ask);
            }
            try {              
                ret = _reader.readLine();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception is thrown");
            }
        }
        waiting = false;

        if (ret.length() > 0) return ret;
        else return defaultValue;
    }

    protected String[] promptValues(String ask, boolean newline, String defaultValue) {
        String s = prompt(ask, false, defaultValue, false);
        if (s != null && s.length() > 0) {
            ArrayList list = new ArrayList();
            for (StringTokenizer st = new StringTokenizer(s);
                 st.hasMoreTokens(); ) {
                list.add(st.nextToken());
            }        
            String[] array = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = (String)list.get(i);
            }
            return array;
        } else {
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////
    // PROMPT UTILS : END
    ///////////////////////////////////////////////////////////////




    
    ///////////////////////////////////////////////////////////////
    // SESSION MANAGEMENT : BEGIN
    ///////////////////////////////////////////////////////////////
              
    // The number in the test script that identifies this instance
    private String _instanceID = null;
    public void setInstanceID(String s) { _instanceID = s; }
    public String getInstanceID() { return _instanceID; }


    private void logout() throws Exception {        
        if(_session != null)_session.logout();
    }


    void readLoginInfo(String args[], boolean includeResource)
    {
         if (args.length > 0 && args[0].equals("?")) {
            System.out.println("login host username password");
            return;
        }

        // prompt server
        if (args.length >= 1) {
            currentServer = args[0];
        } else {
            currentServer = prompt("IM Service [" + defaultServer + "]: ",
                            false, defaultServer);
            defaultServer = currentServer; // use last entered value as default
        }
        
        if (args.length >= 2) {
            user = args[1];
        } else {
            
            user = prompt("User Name [" + user + "]: ",
                          false, user);                                         
        }
         
         if (includeResource) {
             if (user.indexOf('/') < 0) user = user + "/shell";
         }
        
        if (args.length >= 3) {
            password = args[2];
        } else { 
            if (includeResource) {
            password = prompt("Password [" + password + "]: ",
                              false, password);
            } else {
                String dpassword = "";
                password = prompt("Password [" + dpassword + "]: ",
                              false, dpassword);
                if (password == null) {
                    password = dpassword;
                }
            }
        }
    }

    void initServices() throws Exception
    {
                        
	_presenceService = _session.getPresenceService();
	_presenceService.initialize(this);
	_newsService = _session.getNewsService();
	_conferenceService = _session.getConferenceService();
	_conferenceService.initialize(this);
	_messageService = _session.getNotificationService();
	_messageService.initialize(this);
	_personalStoreService = _session.getPersonalStoreService();
	_personalStoreService.initialize(this);
	_streamingService = _session.getStreamingService();
	_streamingService.initialize(this);
        
        _principal = _session.getPrincipal();
    }

    //login to the server    
    public void login(String args[]) throws Exception 
    {    
        readLoginInfo(args, true);

        _session = _factory.getSession(currentServer, user, password, this);
        
        initServices();
    }

    
    public void loginComponent(String args[]) throws Exception {
        readLoginInfo(args, false);
        
        /*
        user = StringUtility.appendResourceToAddress(user, "calimbot");
         */
        _compFactory = new CollaborationSessionFactory("org.netbeans.lib.collab.xmpp.XMPPComponentSessionProvider");
        CollaborationSessionProvider sprovider = _compFactory.getCollaborationSessionProvider();
        ApplicationInfo appinfo = sprovider.getApplicationInfo();

        appinfo.setCategory("component");
        appinfo.setType("generic");
        _session = _compFactory.getSession(currentServer, user, password, this); 
	_messageService = _session.getNotificationService();
	_messageService.initialize(this);
        _principal = _session.getPrincipal();
        //initServices();
    }


    
    public void registerGateway(String [] args) throws Exception
    {     
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("rgw username password");
            return;
        }
        
        Map m = listGateways(args);       
        String gatewayChoice = prompt("Enter the gateway of your choice: ", false);  
        /*
        PersonalGateway gateway = 
                (PersonalGateway)_personalStoreSession.getEntry(PersonalStoreEntry.GATEWAY,
                                                                gatewayName);
         */
        PersonalGateway gateway = (PersonalGateway)m.get(gatewayChoice);
        String user = null;
        String password = null;
        if (gateway != null) {
            /*
            if (args.length >= 1) {
                user = args[0];
            } else {
                user = prompt("User Name [" + user + "]: ",
                false, user);
            }            
            if (args.length >= 2) {
                password = args[1];
            } else {
                password = prompt("Password [" + password + "]: ",
                false, password);
            }
            */
            /*
            Set fields = gateway.getRegistrationFields();
            Map fieldValuePairs = new HashMap();
            for (Iterator i = fields.iterator(); i.hasNext();) {
                String fieldName = (String)i.next();
                if (fieldName.equals("username")) {
                    fieldValuePairs.put(fieldName, user);
                }
                if (fieldName.equals("password")) {
                    fieldValuePairs.put(fieldName, password);
                }                                               
            }
            gateway.register(fieldValuePairs); */
            // gateway.register(user, password);
            gateway.register(new ShellRegistrationListener(this));
            /*
            synchronized(this) {
            wait();
            }
             */
        } else {
            System.out.println("Gateway was not found");
            return;
        }                       
    }
    
    public void unregisterGateway(String [] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("rgw username password");
            return;
        }
        System.out.println("List of Registered gateways");
        for (Iterator i = _personalStoreService.getEntries(PersonalStoreEntry.GATEWAY).iterator(); i.hasNext();) {
            PersonalGateway gateWayEntry = (PersonalGateway)i.next();
            System.out.println( " -" + gateWayEntry.getHostName());
        }
        
        String gatewayName = prompt("Gateway to un-register : ", false);
        PersonalGateway gateway = (PersonalGateway)_personalStoreService.getEntry(PersonalStoreEntry.GATEWAY,gatewayName);
        
        if (gateway != null) {
            gateway.unregister(new ShellRegistrationListener(this));
        } else {
            System.out.println("Gateway " + gatewayName + " was not found");
            return;
        }
    }
    
    public void registerUser(String [] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("ru server");
            return;
        }
        String server = null;
        if (args.length >= 1) {
            currentServer = args[0];
        } else {
            currentServer = prompt("IM Service : ", false);
        }
        
        CollaborationSessionProvider sessionProvider =
                _factory.getCollaborationSessionProvider();
        
        sessionProvider.register(currentServer, new ShellRegistrationListener(this));
        /*
        synchronized(this) {
            wait();
        }
         */
        // _session.register(server, new ShellRegistrationListener(this, false));
    }
    
    public void unregisterUser(String [] args) throws Exception {
        _session.unregister(new ShellRegistrationListener(this));
    }
    
    public void changeUserPassword(String [] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("cup server");
            return;
        }
        String password = null;
        if (args.length >= 1) {
            password = args[0];
        } else {
            password = prompt("New Password : ",
            false);
        }
        
        _session.changePassword(password, new ShellRegistrationListener(this));
        /*
        synchronized(this) {
            wait();
        }
         */
        // _session.register(server, new ShellRegistrationListener(this, false));
    }
    
    public Map listGateways(String [] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("lgw");
            return null;
        }
        System.out.println("List of available gateways");
        PersonalStoreEntry[] pgw =  (PersonalStoreEntry[])_personalStoreService.search(
                                                PersonalStoreService.SEARCHTYPE_CONTAINS, 
                                                "*", 
                                                PersonalStoreEntry.GATEWAY);
        Map m = new HashMap();
        for (int i = 0; i < pgw.length; i++) {
            System.out.println(i + 1 + ") " + pgw[i].getEntryId() + 
                            " <" + ((PersonalGateway)pgw[i]).getService() + ">");            
            m.put(new Integer(i+1).toString(), pgw[i]);            
        }
        return m;
    }
    
    public Map listRegisteredGateways(String [] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("lgw");
            return null;
        }
        System.out.println("List of Registered gateways");
        int j = 0;
        Map m = new HashMap();
        for (Iterator i = _personalStoreService.getEntries(PersonalStoreEntry.GATEWAY).iterator(); i.hasNext();) {
            PersonalGateway gateWayEntry = (PersonalGateway)i.next();
            System.out.println(j + 1 + ") " + gateWayEntry.getHostName() +
                                " <" + gateWayEntry.getService() + ">");
            m.put(new Integer(j + 1).toString(), gateWayEntry);
        }
        return m;
    }
       
    private void setCapabilities(String[] arg) throws Exception {
        CollaborationSessionProvider sessionProvider =
                _factory.getCollaborationSessionProvider();
        
        ApplicationInfo appinfo = sessionProvider.getApplicationInfo();

	appinfo.setCategory("client");
	appinfo.setType("generic");
	appinfo.setVersion("1.0");
	appinfo.setName(getClass().toString());
        
        String feature;
        for (Iterator i = appinfo.getFeatures().iterator(); i.hasNext(); ) {
 feature = (String)i.next();
            if (!getYesOrNo("Keep " + feature + "?  ", false)) {
                i.remove();
            }
        }
        
        for (;;) {
            feature = prompt("Add feature: ", false);
            if (feature != null && feature.trim().length() > 0) {
				if (feature.indexOf(":") > 0) {
                    appinfo.addFeature(feature);
				} else {
					appinfo.addFeature("http://jabber.org/protocol/" + feature);
				}
            } else {
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////
    // SESSION MANAGEMENT : END
    ///////////////////////////////////////////////////////////////



       

    ///////////////////////////////////////////////////////////////
    // PRESENCE SERVICE : BEGIN
    ///////////////////////////////////////////////////////////////
        
    /**
     * presence status change callback
     */
    public void onPresenceInfo(String presenceInfo) 
    {
        PresenceHelper ph = null;
        try {
            ph = new PresenceHelper(presenceInfo);
        } catch(Exception e) {
            e.printStackTrace();
        }
        ArrayList tuples =  (ArrayList)ph.getTuples();

        for(int i = 0; i < tuples.size(); i++ ) {
              PresenceTuple t = (PresenceTuple)tuples.get(i);
              if(t.getNote() != null && t.getNote().length() > 0 ) {
                  System.out.println("\n[Presence Info Received] Recipient=<" + _principal.getUID() + "> Destination=<" + t.getContact() + "> Status=<" + t.getStatus() + "> Note=<" + t.getNote() + ">");
              } else {
                  System.out.println("\n[Presence Info Received] Recipient=<" + _principal.getUID() + "> Destination=<" + t.getContact() + "> Status=<" + t.getStatus() + ">");
              }
            
        }
    }

    //set the users status as away
    private void publishPresenceInfo(String[] args) throws Exception {        
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("ppi status  - publish presence status");
            return;
        }
        String status;
        if (args.length >= 1) {
            status = args[0];
        } else {
            status = prompt("New presence status (OPEN, CLOSED, AWAY, IDLE, FORWARDED): ", false);
        }
        
        String address;
        if (args.length >= 2) {
            address = args[1];
        } else {
            address = prompt("Entity (Enter if this user): ", false);
        }
	if (address.trim().length() == 0) address = _principal.getUID();

        String note = prompt("Optional note: ", false);
        
        //PresenceTuple pt = new PresenceTuple("im", address, status);
        PresenceTuple pt = new PresenceTuple();
        pt.setContact(address);
        pt.setStatus(status);
        if (note != null && note.length() > 0) {
            pt.addNote(note);
        }
        //Presence p = new Presence(address);
        //p.addTuple(pt);
        Presence p = new Presence(pt);
        //_presenceService.publish(p.toString());
        _presenceService.publish(p);
    }
           
    private void publishPersonalEvent(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("ppe status  - publish personal event");
            return;
        }
 
        String node = null;
        if (args.length >= 1) {
            node = args[0];
        } else {
            node = prompt("PEP Node: ", false);
        }
        if (node.indexOf(":") < 0) {
            node = "http://jabber.org/protocol/caps#" + node;
        }

        String content = prompt("enter the event data", true);
  
        NewsChannel pepChannel = 
                _newsService.getNewsChannel(node, null);
        Message m = pepChannel.createMessage();
        m.setContent(content, "text/xml");
        pepChannel.addMessage(m);
    }
    

    //check on other users status
    public void fetchPresenceInfo(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("fpi [ entity ]");
            return;
        }
        String[] entities;
        if (args.length >= 1) {
            entities = args;
        } else {
            entities = promptValues("Entities (space-sparated): ", false, null);
        }

        try {
            /*if (entities.length > 1) {
                _presenceService.fetch(entities, this);
            } else {
                _presenceService.fetch(entities[0], this);
            }*/
        } catch  (Exception e) {
            System.out.println("Cannot fetch presence information: " + e);
        }
    }
        
    //get other users status 
    public void fetchPresence(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("fp [ entity ]");
            return;
        }
        String[] entities;
        if (args.length >= 1) {
            entities = args;
        } else {
            entities = promptValues("Entities (space-sparated): ", false, null);
        }

        Presence p = null;
        try {
            if (entities.length > 1) {
                Presence[] ps = _presenceService.fetchPresence(entities);
            } else {
                p = _presenceService.fetchPresence(entities[0]);
            }
            printPresence(p);
        } catch  (Exception e) {
            System.out.println("Cannot fetch presence information");
        }
    }


    //check on other users status
    public void subscribePresenceInfo(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("spi [ entity ]");
            return;
        }
        String[] entities;
        if (args.length >= 1) {
            entities = args;
        } else {
            entities = promptValues("Entities (space-sparated): ", false, null);
        }

        if (entities.length > 1) {
            _presenceService.subscribe(entities);//, this);
        } else {
            _presenceService.subscribe(entities[0]);//, this);
        }
    }

    // Cancel updates of other users status
    public void unsubscribePresenceInfo(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("uspi [ entity ]");
            return;
        }
        String[] entities;
        if (args.length >= 1) {
            entities = args;
        } else {
            entities = promptValues("Entities (space-sparated): ", false, null);
        }

        if (entities.length > 1) {
            _presenceService.unsubscribe(entities);
        } else {
            _presenceService.unsubscribe(entities[0]);
        }
    }

    /**
     * required by PresenceInfoListener
     */
    public void onCompletion() {
        // todo
    }

    private void listPrivacyList(String[] args) throws Exception
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("lpl");
            return;
        }
        System.out.println("Current privacy list");
        List l = _session.listPrivacyLists();
        for(Iterator i = l.iterator(); i.hasNext();) {
            System.out.println("- " + i.next());
        }
    }
    
    private void getPrivacyList(String[] args) throws Exception
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("gpl [ name ]");
            return;
        }
        String name = "test";
        if (args.length > 0) {
            name = args[0];
        } else {
            name = prompt("Enter the name of the Privacy List: ",false);
        }
        PrivacyList list = _session.getPrivacyList(name);
        
        if (list == null) {
           System.out.println("No such privacy list could be found");
           return;
        }
        System.out.println("Current privacy");
        Collection c = list.getPrivacyItems();
        for(Iterator itr = c.iterator(); itr.hasNext();) {
            PrivacyItem item = (PrivacyItem)itr.next();
            System.out.println("Type[ " + item.getType() + " ] Access [ " + 
                        item.getAccess() + " ] Resource [ " + item.getResource() + " ]");
            //String[] subjects = item[i].getSubjects();
            //for(int j = 0; j < subjects.length; j++) {
                //System.out.println(" - " + subjects[j]);
                System.out.println(" - " + item.getSubject());
            //}
        }
    }

    private void removePrivacyList(String[] args) throws Exception
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("rpl [ name ]");
            return;
        }
        String name = "test";
        if (args.length > 0) {
            name = args[0];
        } else {
            name = prompt("Enter the name of the Privacy List: ",false);
        }
        _session.removePrivacyList(name);
    }
    
    private void setPrivacyList(String[] args) throws Exception
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("spl [ name ]");
            return;
        }
        String name = "test";
        if (args.length > 0) {
            name = args[0];
        } else {
            name = prompt("Enter the name of the Privacy List: ", false);
        }

        // display the rules
        PrivacyList list = null;
        try {
            list = _session.getPrivacyList(name);
        } catch (Exception e) {
        }
        if (list != null) {
            System.out.println("Current privacy list " + name);
            Collection c = list.getPrivacyItems();
            PrivacyItem[] items = new PrivacyItem[c.size()];
            int i = 0;
            for(Iterator itr = c.iterator(); itr.hasNext();) {
                PrivacyItem item = (PrivacyItem)itr.next();
                System.out.println("[" + i++ + "]\tType[ " + item.getType() + " ] Access [ " + 
                                   item.getAccess() + " ] Resource [ " + item.getResource() + " ]");
                //String[] subjects = item[i].getSubjects();
                //for(int j = 0; j < subjects.length; j++) {
                    //System.out.println(" - " + subjects[j]);
                System.out.println(" - " + item.getSubject());
                //}
            }

            // remove existing
            for (;;) {
                String sIndex = prompt("Remove item index: ", false);
                if (sIndex.length() <= 1) break;
                int index = Integer.parseInt(sIndex);
                if (index >= 0 && index < items.length) {
                    list.removePrivacyItem(items[index]);
                } else {
                    System.out.println("index out of bounds");
                }
            }
        } else {
            list  = _session.createPrivacyList(name);
            System.out.println("creating new list");
        }

        // add new ones
        String yesOrNo = "no";
        yesOrNo = prompt("Add a privacy item? ", true, "no");
        while (yesOrNo.equalsIgnoreCase("yes")) {
            
            String sType   = prompt("    type (jid|subscription|group): ", false);
            String sAction = prompt("    action (allow|deny):           ", false);
            String sValue  = prompt("    value:                         ", false);
            String sRes    = prompt("    resources (message,iq,p-out):   ", false);

            int action = PrivacyItem.ALLOW;
            if (sAction.equalsIgnoreCase("deny")) action = PrivacyItem.DENY;

            PrivacyItem item = list.createPrivacyItem(sType, action);
            item.setSubject(sValue);

            int resource = 0;
            if (sRes == null || sRes.trim().length() == 0) {
                resource = -1;
            } else {
                if (sRes.indexOf("message") >= 0) resource |= PrivacyItem.MESSAGE;
                if (sRes.indexOf("iq") >= 0) resource |= PrivacyItem.IQ;
                if (sRes.indexOf("presence-in") >= 0) resource |= PrivacyItem.PRESENCE_IN;
                if (sRes.indexOf("presence-out") >= 0) resource |= PrivacyItem.PRESENCE_OUT;
            }
            item.setResource(resource);

            list.addPrivacyItem(item);

            yesOrNo = prompt("Add a privacy item? ", true, "no");
        }

        _session.addPrivacyList(list);        
        
    }
    
    ///////////////////////////////////////////////////////////////
    // PRESENCE SERVICE : END
    ///////////////////////////////////////////////////////////////
        



    ///////////////////////////////////////////////////////////////
    // CONFERENCE SERVICE : BEGIN
    ///////////////////////////////////////////////////////////////
                
    public void setConfName(Hashtable h){
        _confnames=h;
    }

    private ShellConference findConference(String name) throws Exception 
    {
        ShellConference c;
        String confname = (String)_confnames.get(name);
        if (confname != null) {
            return (ShellConference)_conferences.get(confname);
        } else {
            return (ShellConference)_conferences.get(name);
        }
    }

    private ShellNewsChannel findNewsChannel(String theName, boolean subscribe) throws Exception {
        ShellNewsChannel nc = null;
        String fqName = appendDomainToAddress(theName, "pubsub." + _session.getPrincipal().getDomainName());
        String channelName = (String)_confnames.get(fqName);
        if (channelName != null) {
            nc = (ShellNewsChannel)_conferences.get(channelName);
        } else {
            nc = (ShellNewsChannel)_conferences.get(fqName);
        }
        if (nc == null) {
            // Then go ask the server
            // Added the try catch statement to avoid the recursive flow of the exception. - Rahul
            //String name = getLocalPartFromAddress(fqName);
            try {
                //System.out.println("Creating new newschannel object");
                //nc = new ShellNewsChannel(_principal.getUID(), _newsService, name, subscribe);
                //System.out.println("nc is still null ");
                //theName = getLocalPartFromAddress(theName);
                nc = new ShellNewsChannel(_principal.getUID(), _newsService, fqName, subscribe);
                assignLocalName(fqName, nc.getConference().getDestination());
            } catch(NoSuchElementException e) {
                //System.out.println("Cannot create newsChannel: " + e.getMessage());
            }
            if (nc != null) {
                // _conferences.put(fqName, nc);
                 _conferences.put(nc.getConference().getDestination(),nc);
            }
        }
        return nc;
    }
    
    //invite a user to a room
    private void invite(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("iuc [ conf [ users ] ]");
            System.out.println("invite message");
            return;
        }

        String roomname;
        if (args.length > 0) {
            roomname = args[0];
        } else {
            roomname = prompt("Conference: ", false);
        }
        
        String names = "";
        if (args.length >= 2) {
            names = args[1];
        } else {
            names = prompt("User(s) to invite (space-separated): ",false);
        }

        String ret = prompt("Invite message: ", true);
        if (ret.equals("")) ret = "<empty>";
        ret = "     " + ret;   //TODO Works but I need to look at more

        ShellConference c = findConference(roomname);
        if (c == null) {
            System.out.println("[" + _principal.getUID() + "] Conference " + roomname + " not found");
            return;
        }
        Conference conf = c.getConference();
        _conferences.put(conf.getDestination(), c);
        
        Message newMsg = conf.createInviteMessage();
        MessagePart part = newMsg.newPart();
        part.setContent(ret);
        newMsg.addPart(part);

        StringTokenizer st = new StringTokenizer(names);
        while (st.hasMoreTokens()) {
            newMsg.addRecipient(st.nextToken());
        }
        conf.invite(Conference.MANAGE, newMsg, this);
        
    }
        
    //add a message to a room, user or topic
    private void addConferenceMessage(String args[]) throws Exception {

        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("amc [ conference ]");
            System.out.println("msg line");
            return;
        }

        Message newMsg = null;
        MessagePart part;
        
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        //name = appendDomainToAddress(name,_principal.getDomainName());
        ShellConference c = findConference(name);
        if (c == null) {
            System.out.println("Conference " + name + " not found");
            return;
        }
        String ret = prompt("enter the message", true);
        Conference conf = c.getConference();
        newMsg = conf.createMessage();
        part = newMsg.newPart();
        part.setContent(ret);
        part.setContent(ret,"text/html");
        newMsg.addPart(part);
        
        conf.addMessage(newMsg);

    }

    //add a public conference
    private void addPublicConference(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("apc [ conf ] ");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }
  

        try {        
            ShellConference c = new ShellConference(_principal.getUID(), _conferenceService, name, true, true);
            c.setShell(this);
            _conferences.put(c.getConference().getDestination(), c);
            //name = appendDomainToAddress(name,_principal.getDomainName());
            assignLocalName(name, c.getConference().getDestination());
            System.out.println("Created public conference " + name + "=" + c.getConference().getDestination());
            _conferences.put(name, c);
        } catch (Exception e) {
            System.out.println("Cannot create conference: " + e.getMessage());
        }
    }
    
    //join a public conference
    private void joinPublicConference(String args[]) throws Exception 
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("jpc [ conf ] ");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }
        //name = appendDomainToAddress(name,_principal.getDomainName());

        ShellConference c = findConference(name);
        if (c == null) {
            c = new ShellConference(_principal.getUID(), _conferenceService, name, false, true);
            c.setShell(this);
            _conferences.put(name, c);
        } else {
            c.getConference().join(c);
        }
        if (c != null) {
            System.out.println("[" + _principal.getUID() + "] Joined conference " + c.getConference().getDestination());
        } else {
            System.out.println("Failed to join conference " + name);
        }
    }
    
    private void listSubscribedPublicConference(String args[]) throws Exception
    {
         Collection coll = _personalStoreService.getEntries(PersonalStoreEntry.CONFERENCE);
         System.out.println("Listing subscribed conferences - START");
         if (coll != null) {
             for (Iterator i = coll.iterator(); i.hasNext(); ) {
                 PersonalConference pc = (PersonalConference)i.next();
                 System.out.println("    " + pc.getDisplayName() + " <" + pc.getAddress() + ">");
             }
         }
         System.out.println("Listing subscribed conferences - END");
     }


    //join a public conference
    private void subscribePublicConference(String args[]) throws Exception 
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("spc [ conf ] ");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        ShellConference c = findConference(name);
        if (c == null) {
            try {
            c = new ShellConference(_principal.getUID(),
                                     _conferenceService, name,
                                     false, false);
            c.setShell(this);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return;
            }
            _conferences.put(name, c);
        }

        if (c != null) {
            Conference conf = c.getConference();
            PersonalConference pc = (PersonalConference)_personalStoreService.createEntry(PersonalStoreEntry.CONFERENCE, conf.getDestination());
            pc.setAddress(conf.getDestination());
            pc.save();
        } else {
            System.out.println("Failed to locate conference " + name);
        }
    }
    
    //join a public conference
    private void unsubscribePublicConference(String args[]) throws Exception 
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("upc [ conf ] ");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        ShellConference c = findConference(name);
        if (c == null) {
            c = new ShellConference(_principal.getUID(),
                                     _conferenceService, name,
                                     false, false);
            c.setShell(this);
            _conferences.put(name, c);
        }
        if (c != null) {
            Conference conf = c.getConference();
            PersonalStoreEntry pc = _personalStoreService.getEntry(PersonalStoreEntry.CONFERENCE,
                                                                   conf.getDestination());
            if (pc != null) pc.remove();
        } else {
            System.out.println("Failed to locate conference " + name);
        }

    }
    

    //add a conference
    private void addConference(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("ac");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        ShellConference c = new ShellConference(_principal.getUID(), _conferenceService, name);
        c.setShell(this);
        String fqName = appendDomainToAddress(name, "muc." + _session.getPrincipal().getDomainName());
        _conferences.put(c.getConference().getDestination(), c);                     
        _conferences.put(name, c);        
        _conferences.put(fqName, c);
        assignLocalName(name, c.getConference().getDestination());       
        
        System.out.println("[" + _principal.getUID() + "] Created private conference " + name);
    }
    
    //delete a room
    private void delRoom(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("rc [ conf ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        ShellConference c = findConference(name);
        if (c == null) {
            c = new ShellConference(_principal.getUID(), _conferenceService,
                                    name, false, false);
            c.setShell(this);
            if (c.getConference() == null) {
                System.out.println("Conference " + name + " not found.");
                return;
            }
            //_conferences.put(name, c);
        }
        _conferences.remove(c.getConference().getDestination());
        _conferences.remove(name);

        c.getConference().close();
    }
    
    //leave a room
    public void quitRoom(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("lc [ conference ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }
        //name = appendDomainToAddress(name,_principal.getDomainName());
        ShellConference conf = findConference(name);
        if (conf != null) {
            conf.getConference().leave();
            _conferences.remove(conf.getConference().getDestination());
            _conferences.remove(name);
        } else {
            System.out.println("Conference not found");
        }
    }
    
    public void onInvite(Conference conference, InviteMessage message) {

        // When run standalone, imp shell can only use the locally generated conference name
        // but with MultiimTalk, it can use other's defined names, so
        // should include invitor, because many of the conference names
        // will be simple and repeated...
        if (conference.isPublic()) {
            System.out.println("\nYou have been invited to conference <" + conference.getDestination() + "> by <" + message.getOriginator() + ">");
        } else {
            System.out.println("\nYou have been invited to a private conference by <" + message.getOriginator() + ">");
        }
        try {
            if (getYesOrNo("Do you want to join?\n", true)) {
                String name;
                if (!conference.isPublic()) {
                    name = prompt("Enter a local name for this conference: ",
                                  false, true);
                    String confAddress = conference.getDestination();
                    assignLocalName(name, confAddress);
                    String fqName = appendDomainToAddress(name, getDomainFromAddress(confAddress, null));
                    assignLocalName(fqName, confAddress);
                    
                } else {
                    name = conference.getDestination();
                    assignLocalNameIfSameDomain(conference.getDestination());
                }

                ShellConference c = new ShellConference(_principal.getUID(), _conferenceService, conference, name);
                c.setShell(this);
                _conferences.put(conference.getDestination(), c);
                _conferences.put(name, c);
                message.rsvp(true);
                System.out.println("[" + _principal.getUID() + "] Joined conference " + name);                
            } else {
                message.rsvp(false);
                System.out.println("\nDeclining invitation");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\n Exception thrown: Failed to join a Conference");
        }
    }
 
    public void onRsvp(String destination, Message message, boolean accepted) {
        onRsvp(destination, accepted);
        printMessage(message);
    }
    
    public void onRsvp(String destination, boolean accepted) {
        if (accepted) {
            System.out.println(destination + " accepted your invitation.");
        } else {
            System.out.println(destination + " declined your invitation.");
        }
    }
    
    private String accessToString(int access)
    {
        if (Conference.MANAGE == (Conference.MANAGE & access)){
            return "MANAGE";
        }
        if (Conference.PUBLISH == (Conference.PUBLISH & access)){
            return "PUBLISH";
        }
        if (Conference.LISTEN == (Conference.LISTEN & access)){
            return "LISTEN";
        }
        if (Conference.NONE == (Conference.NONE & access)){
            return "NONE";
        }
        System.out.println("Invalid access level: " + access);
        return "UNKNOWN";
        /*
        switch (access) {
        case Conference.NONE :
            return "NONE";
        case Conference.LISTEN :
            return "LISTEN";
        case Conference.PUBLISH :
            return "PUBLISH";
        case Conference.MANAGE :
            return "MANAGE";
        default :
            System.out.println("Invalid access level: " + access);
            return "UNKNOWN";
        }
         */
    }

    private int accessToVal(String access) throws Exception
    {
        if (access.equalsIgnoreCase("NONE")) {
            return Conference.NONE;
        } else if (access.equalsIgnoreCase("LISTEN")) {
            return Conference.LISTEN;
        } else if (access.equalsIgnoreCase("PUBLISH")) {
            return Conference.PUBLISH;
        } else if (access.equalsIgnoreCase("MANAGE")) {
            return Conference.MANAGE;
        } else {
            throw new Exception("invalid access level: " + access);
        }
    }

    private void displayConferenceAcl(String[] args)  throws Exception
    {
        // display the current acl - there is only one rule today
        // use _username as presentity identifier
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("dcacl [ public-conference ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        ShellConference c = findConference(name);
        if (c == null) {
            c = new ShellConference(_principal.getUID(), _conferenceService, name, false, false);
            c.setShell(this);
            _conferences.put(name, c);
        }
        Conference conf = c.getConference();
        Map accessList = conf.listPrivileges();
        for(Iterator i = accessList.keySet().iterator(); i.hasNext();) {
            String uid = (String)i.next();
            System.out.println("User [" + uid + "] has " +
                        accessToString(((Integer)accessList.get(uid)).intValue()) +
                        " access");
        }
    }

    private void editConferenceAcl(String[] args) throws Exception
    { 
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("cacl [ public-conference ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        ShellConference c = findConference(name);
        if (c == null) {
            c = new ShellConference(_principal.getUID(), _conferenceService, name, false, false);
            c.setShell(this);
            _conferences.put(name, c);
        }
        Conference conf = c.getConference();

        // modify default access ?
        String sDefaultAccess = accessToString(conf.getDefaultPrivilege());
        String sNewDefaultAccess = prompt("Default access [" + sDefaultAccess + "] : ", false, sDefaultAccess);
        if (!sNewDefaultAccess.equalsIgnoreCase("remove") && !sDefaultAccess.equals(sNewDefaultAccess)) {
            conf.setDefaultPrivilege(accessToVal(sNewDefaultAccess));
        }

        for (;;) {
            String member = prompt("Modify Access for: ", false);
            if (member.equals("")) break;
            String sAccess = accessToString(conf.getPrivilege(member));
            String sNewAccess = prompt("New access level [" + sAccess + "]: ",
                                       false, sAccess);

            if (sNewAccess.equalsIgnoreCase("remove")){
                // get to refresh
                conf = c.getConference();
                Map accessList = conf.listPrivileges();
                if (null == accessList.remove(member)){
                    System.out.println("No access present for: " + member);
                    continue;
                }

                try {
                    conf.setPrivileges(accessList);
                } catch (Exception e) {
                    System.out.println("Cannot remove privilege for: " + member);
                }
            }
            else {
                try {
                    conf.setPrivilege(member, accessToVal(sNewAccess));
                } catch (Exception e) {
                    System.out.println("Cannot set privilege for: " + name);
                }
            }
        }
    }


    private void getDestinationProperty(String args[]) throws Exception
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("rmdp [ conference ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        ShellConference c = findConference(name);
        if (c == null) {
            c = new ShellConference(_principal.getUID(), _conferenceService, name, false, false);
            c.setShell(this);
            _conferences.put(name, c);
        }
        Conference conf = c.getConference();

        for (;;) {
            String att = prompt("Property : ", false);
            if (att.equals("")) break;
            System.out.println("    " + att + " = " + conf.getProperty(att));
        }

    }
    
    private void removeDestinationProperty(String args[]) throws Exception
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("rmcp [ conference ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        ShellConference c = findConference(name);
        if (c == null) {
            c = new ShellConference(_principal.getUID(), _conferenceService, name, false, false);
            c.setShell(this);
            _conferences.put(name, c);
        }
        Conference conf = c.getConference();

        for (;;) {
            String att = prompt("Property : ", false);
            if (att.equals("")) break;
            conf.setProperty(att, null);
        }

    }
    
    private void setDestinationProperty(String args[]) throws Exception
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("setdp [ conference ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        ShellConference c = findConference(name);
        if (c == null) {
            c = new ShellConference(_principal.getUID(), _conferenceService, name, false, false);
            c.setShell(this);
            _conferences.put(name, c);
        }
        Conference conf = c.getConference();
        
        for (;;) {
            String att = prompt("Property : ", false);
            if (att.equals("")) break;
            String val = prompt("Value    : ", false);
            conf.setProperty(att, val);
        }

    }
    
    public void listPublicConferences(String args[]) throws Exception
    {
        PersonalStoreEntry[] pc = _personalStoreService.search(PersonalStoreService.SEARCHTYPE_EQUALS, "*", PersonalStoreEntry.CONFERENCE);
        System.out.println("Listing public conferences - START");
        if (pc != null) {
            for (int i = 0; i < pc.length; i++ ) {
                PersonalConference c = (PersonalConference)pc[i];
                System.out.println("    " + c.getDisplayName() + " <" + c.getAddress() + ">");
            }
        }
        System.out.println("Listing public conferences - END");
    }
        
    public void moderateRoom(String args[]) throws Exception
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("mc [ conference ] [start | stop]");
            return;
        }
        String name;
        String state;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }
        if (args.length >= 2) {
            state = args[1];
        } else {
            state = prompt("[start|stop]: ", false);
        }
        ShellConference c = findConference(name);
        if (c == null) {
            System.out.println("Conference " + name + " not found");
            return;
        }
        Conference conf = c.getConference();
        if (state.equalsIgnoreCase("start")) {
            conf.moderate(true);
        } else conf.moderate(false);
    }
    
    public void addModeratedMessage(String args[]) throws Exception{
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("ammc [ conference ]");
            System.out.println("msg line");
            return;
        }

        Message newMsg = null;
        MessagePart part;
        
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        //name = appendDomainToAddress(name,_principal.getDomainName());
        ShellConference c = findConference(name);
        if (c == null) {
            System.out.println("Conference " + name + " not found");
            return;
        }
        String ret = prompt("enter the message", true);
        Conference conf = c.getConference();
        newMsg = conf.createMessage();
        part = newMsg.newPart();
        part.setContent(ret);
        part.setContent(ret,"text/html");
        newMsg.addPart(part);
        conf.addModeratedMessage(newMsg, Conference.STATUS_SUBMIT, null);
    }
    
    public void handleModeratedMessage(Conference conf, Message message, int status) 
                throws Exception
    {
        if (status == Conference.STATUS_SUBMIT) {
            conf.addModeratedMessage(message,Conference.STATUS_PENDING,"waiting for approval");
            String nextStatus = prompt("Option (approve, reject, modify)[approve]: ", false, true);
            int s = Conference.STATUS_APPROVED;
            if ("REJECT".equalsIgnoreCase(nextStatus)) {
                s = Conference.STATUS_REJECTED;
            } else if ("MODIFY".equalsIgnoreCase(nextStatus)) {
                s = Conference.STATUS_MODIFIED;
                MessagePart parts[] = message.getParts();
                String newMsg = prompt("enter modified message", true, parts[0].getContent(), true);
                parts[0].setContent(newMsg);
            }
            String reason = prompt("Reason for your decision: ", false, true);
            conf.addModeratedMessage(message,s,reason);
        }
    }
    
    public void listMembersInRoom(String args[]) throws Exception{
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Conference: ", false);
        }

        //name = appendDomainToAddress(name,_principal.getDomainName());
        ShellConference c = findConference(name);
        if (c == null) {
            c = new ShellConference(_principal.getUID(), _conferenceService, name, false, false);
            c.setShell(this);
            _conferences.put(name, c);
        }
        Conference conf = c.getConference();
        Collection participants = conf.getParticipants();
        System.out.println("Number of participants - " + participants.size());
        for(Iterator i = participants.iterator(); i.hasNext();) {
            System.out.println((String)i.next());
        }
    }
    ///////////////////////////////////////////////////////////////
    // CONFERENCE SERVICE : END
    ///////////////////////////////////////////////////////////////

        
    
    
    ///////////////////////////////////////////////////////////////
    // NEWS SERVICE : BEGIN
    ///////////////////////////////////////////////////////////////    
    
    //add a message to a room, user or topic
    private void addNewsMessage(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("amnc [ channel ]");
            System.out.println("subj line");
            System.out.println("msg line");
            return;
        }

        Message newMsg = null;
        MessagePart part;
    
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = (ShellNewsChannel)findNewsChannel(name, true);
        if (bb == null) {
            System.out.println("News Channel " + name + " not found");
            return;
        }

        String topicSub = prompt("Subject: ", false);
        String ret = prompt("Message: ", true);
        if (ret.equals("")) ret = "<empty>";
        
        newMsg = bb.getConference().createMessage();
        part = newMsg.newPart();
        newMsg.setHeader("subject", topicSub);        
        part.setContent(ret);
        newMsg.addPart(part);
        try {
            bb.getConference().addMessage(newMsg);
        }
        catch (Exception e) {
            System.out.println("Exception thrown in addMessage");            
        }
        /*
        bb._messageIdToNumber.put(newMsg.getMessageId(), Integer.toString(bb._messageIdCount));
        bb._numberToMessageId.put(Integer.toString(bb._messageIdCount), newMsg.getMessageId());
        bb._messageIdCount++;
         */
    }

    //delete a message from a topic
    public void delNewsMessage(String args[]) throws Exception {
        String name;
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("dmnc [ channel ]");
            System.out.println("[id]");
            return;
        }
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = (ShellNewsChannel)findNewsChannel(name, true);

        if (bb == null) {
            System.out.println("News channel " + name + " not found.");
            return;
        }
        
        String simpleId = "<unknown>";
        if (args.length >= 2) {
            simpleId = args[1];
        } else {
            bb.listMessages();
            simpleId = prompt("Id of the message delete: ", false);
        }
        String origId = (String) bb._numberToMessageId.get(simpleId);
        if (origId != null) {
            ((NewsChannel)bb.getConference()).removeMessage(origId);
            bb._messages.remove(origId);
            bb._numberToMessageId.remove(simpleId);
            bb._messageIdToNumber.remove(origId);
        } else {
            System.out.println("No message with this id: " + simpleId);
        }
    }
    
    //create a new topic
    public void addNewsChannel(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("cnc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        
        //Added the following block to check whether the news channel
        // already exists. -Rahul

        ShellNewsChannel nc = (ShellNewsChannel)findNewsChannel(name, true);
		/*
        if (nc != null) {
            System.out.println("News channel " + name + " already present.");
            return;
        }
		*/
        try {
            ShellNewsChannel bb = new ShellNewsChannel(_principal.getUID(), _newsService, name, Conference.PUBLISH);                        
            _conferences.put(bb.getConference().getDestination(), bb);
        } catch (Exception e) {
            // System.out.println("Cannot create news channel: " + e.getMessage());
        }
 
    }
    
    //create a new topic
    public void subscribeNewsChannel(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("snc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = (ShellNewsChannel)findNewsChannel(name, true);
        if (bb == null) {
            System.out.println("News channel " + name + " not found.");
            return;
        }
        System.out.println("subscribed to " + name);
    }
    
    // Stop receiving messages from a news channel
    // Note: leave() is performed and the object is no longer useable,
    // so it would be wrong to try and re-join() it. Removing the channel
    // is the only valid operation after this.

    public void unSubscribeNewsChannel(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("unc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = (ShellNewsChannel)findNewsChannel(name, false);
        String fqName = appendDomainToAddress(name,  "pubsub." + _session.getPrincipal().getDomainName());
        if (bb == null) {
            System.out.println("News channel " + name + " not found.");
            return;
        }
       bb.getConference().leave();        
        _conferences.remove(fqName);
        // remove all cached messages
        bb._messages.clear();
        bb._messageIdToNumber.clear();
        bb._numberToMessageId.clear();
        // Make sure the channel list is updated after unsubscribe
        // That way if it was deleted elsewhere the fact will be
        // correctly reflected.
        // Thus unsubscribe is a way of updating the channel prior to
        // recreating it or resubscribing to it.
        //System.out.println("News Channel " + fqName + " deleted");
        System.out.println("unsubscribed from " + fqName);
    }

    public void listNewsChannels(String args[]) throws Exception {
        java.util.Collection bbList = _newsService.listNewsChannels();
        System.out.println("Listing news channels - START");
        if (bbList != null) {
            java.util.Iterator bbIter = bbList.iterator();            
            while (bbIter.hasNext()) {
                NewsChannel bb = (NewsChannel)bbIter.next();
                System.out.println("    " + bb.getDestination());
            }
        }
        System.out.println("Listing news channels - END");
    }
    
    public void listManagedNewsChannels(String args[]) throws Exception {
        java.util.Collection bbList = _newsService.listNewsChannels(Conference.MANAGE);
        System.out.println("Listing news channels - START");
        if (bbList != null) {
            java.util.Iterator bbIter = bbList.iterator();
            while (bbIter.hasNext()) {
                NewsChannel bb = (NewsChannel)bbIter.next();
                System.out.println("    " + bb.getDestination());
            }
        }
        System.out.println("Listing news channels - END");
    }
    
    public void getSubscribedNewsChannels(String args[]) throws Exception {
        java.util.Collection bbList = _newsService.getSubscribedNewsChannels();
        System.out.println("Listing news channels - START");
        if (bbList != null) {
            java.util.Iterator bbIter = bbList.iterator();
            while (bbIter.hasNext()) {
                NewsChannel bb = (NewsChannel)bbIter.next();
                System.out.println("    " + bb.getDestination());
            }
        }
        System.out.println("Listing news channels - END");
    }
    
    public void listNewsPrivileges(String args[]) throws Exception {
        
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("unc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = (ShellNewsChannel)findNewsChannel(name, false);
        
        if (bb == null) {
            System.out.println("News channel " + name + " not found.");
            return;
        }
        java.util.Map privMap = bb.getConference().listPrivileges();
        System.out.println("Listing privilges of news channel - START");
        if (privMap != null) {
            for (Iterator i = privMap.keySet().iterator();i.hasNext();) {
                String userID = (String)i.next();
                Integer accesslevel = (Integer)privMap.get(userID);
                System.out.println("User " + userID + " has access " + accessInt2accessS(accesslevel.intValue()));
            }
        }
        System.out.println("Listing privilges of news channel - END");
    }
    
    public void setNewsPrivileges(String args[]) throws Exception {
        
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("unc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = (ShellNewsChannel)findNewsChannel(name, false);
        
        if (bb == null) {
            System.out.println("News channel " + name + " not found.");
            return;
        }
        Map user2Access = new HashMap();
        String userID = null;
        String access = null;
        int accesslevel = -1;
        for (;;) {
            userID = prompt("User (return if no more):", false);
            if (userID.equals("")) break;
            access = prompt("Access Level[NONE,LISTEN,PUBLISH,MANAGE]:", false);
            if (access.equals("")) {
                break;
            }
            accesslevel = accessS2accessInt(access);
            /*
            if (access.equals("")) {
                break;
            } else if (access.equalsIgnoreCase("NONE")) {
                accesslevel = Conference.NONE;
            } else if (access.equalsIgnoreCase("LISTEN")) {
                accesslevel = Conference.LISTEN;
            } else if (access.equalsIgnoreCase("PUBLISH")) {
                accesslevel = Conference.PUBLISH;
            } else if (access.equalsIgnoreCase("MANAGE")) {
                accesslevel = Conference.MANAGE;
            }
             */

            user2Access.put(userID,new Integer(accesslevel));
        }               
        ((NewsChannel)bb.getConference()).setPrivileges(user2Access);
    }
    
    public void getNewsConfiguration(String [] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("gnc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = findNewsChannel(name, false);
        if (bb == null) {
            System.out.println("news channel " + name + " not found");
            return;
        }
        //System.out.println(((XMPPNewsChannel)bb.getConference()).getConfiguration().toString());
    }
    
    public void getNewsPrivilege(String [] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("gunc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = findNewsChannel(name, false);
        if (bb == null) {
            System.out.println("news channel " + name + " not found");
            return;
        }
        System.out.println(accessInt2accessS(bb.getConference().getPrivilege()));
    }
    
    public void setNewsPrivilege(String [] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("sunc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = findNewsChannel(name, false);
        if (bb == null) {
            System.out.println("news channel " + name + " not found");
            return;
        }
        String access;
        int accesslevel = -1;
        if (args.length >= 2) {
            access = args[1];
        } else {
            access = prompt("Access Level[NONE,LISTEN,PUBLISH,MANAGE]:", false);
        }
        accesslevel = accessS2accessInt(access);
        bb.getConference().setPrivilege(_session.getPrincipal().getUID(),accesslevel);
    }
    
    public void getDefaultNewsPrivilege(String [] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("gnc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = findNewsChannel(name, false);
        if (bb == null) {
            System.out.println("news channel " + name + " not found");
            return;
        }
        int defaultAccess = bb.getConference().getDefaultPrivilege();
        System.out.println("DEFAULT ACCESS ====" + accessInt2accessS(defaultAccess));
    }
    
    public void setDefaultNewsPrivilege(String [] args) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("gnc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        
        String access;
        int accesslevel = -1;
        if (args.length >= 2) {
            access = args[1];
        } else {
            access = prompt("Access Level[NONE,LISTEN,PUBLISH,MANAGE]:", false);
        }
        accesslevel = accessS2accessInt(access);
        /*
            if (access.equalsIgnoreCase("NONE")) {
                accesslevel = Conference.NONE;
            } else if (access.equalsIgnoreCase("LISTEN")) {
                accesslevel = Conference.LISTEN;
            } else if (access.equalsIgnoreCase("PUBLISH")) {
                accesslevel = Conference.PUBLISH;
            } else if (access.equalsIgnoreCase("MANAGE")) {
                accesslevel = Conference.MANAGE;
            }
         */
                
        ShellNewsChannel bb = findNewsChannel(name, false);
        if (bb == null) {
            System.out.println("news channel " + name + " not found");
            return;
        }
        bb.getConference().setDefaultPrivilege(accesslevel);        
    }
        
    //delete a topic
    public void delNewsChannel(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("rnc [ channel ]");
            return;
        }
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = findNewsChannel(name, false);
        if (bb == null) {
            System.out.println("news channel " + name + " not found");
            return;
        }
        bb.getConference().close();

        // remove all cached messages
        bb._messages.clear();
        bb._messageIdToNumber.clear();
        bb._numberToMessageId.clear();

        // update local cache
        String fqName = appendDomainToAddress(name,"pubsub." + _principal.getDomainName());
        _conferences.remove(fqName);

        System.out.println("News Channel " + fqName + " deleted");
    }

    //list all messages in a room or news channel
    private void listMessages(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("lmnc [ channel ]");
            return;
        }

        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("News channel: ", false);
        }
        ShellNewsChannel bb = (ShellNewsChannel)findNewsChannel(name, true);
//Added the if statement to check whether the news channel was found- Rahul
        if (bb == null) {
            System.out.println("News channel " + name + " not found.");
            return;
        }
        bb.listMessages();
    }    

    ///////////////////////////////////////////////////////////////
    // NEWS SERVICE : END
    ///////////////////////////////////////////////////////////////





    
    ///////////////////////////////////////////////////////////////
    // NOTIFICATION SERVICE : BEGIN
    ///////////////////////////////////////////////////////////////

    //add a message to a room, user or topic
    private void sendMessage(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("send [ users ]");
            System.out.println("subj line");
            System.out.println("msg line");
            return;
        }

        Message newMsg = null;
        MessagePart part;
        
        String names = "";
        if (args.length >= 1) {
            names += args[0];
            for (int i = 1 ; i < args.length ; i++) {
                names += " " + args[i];
            }
        } else {
            names = prompt("recipient list (space-separated): ", false);
        }
            
        String sub = prompt("Subject: ", false);
        String ct  = prompt("Content-Type [text/plain]: ", false, "text/plain");
        String ret = prompt("Message: ", true);
        
        newMsg = _messageService.createMessage();
        newMsg.setHeader("subject", sub);
        newMsg.setContent(ret, ct);
        if (("text/html").equalsIgnoreCase(ct)) {
            newMsg.setContent(htmlConverter.convertToText(ret));
        }
        
        StringTokenizer st = new StringTokenizer(names);
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            newMsg.addRecipient(name);
        }

	newMsg.addPart(newMsg);

        // attachments
        for (;;) {
            String filename = prompt("Attach File: ", false);
            if (filename.equals("")) break;
	    File f = new File(filename);
	    if (f.exists()) {
		MessagePart mp = newMsg.newPart();
		String enc = prompt("Encoding [UTF-8]: ", false, "UTF-8");
		mp.setContent(new FileInputStream(f), enc);
		mp.setContentName(f.getName());
		newMsg.addPart(mp);
	    } else {
		System.out.println("File not found: " + f.getAbsolutePath());
	    }
        }

        _messageService.sendMessage(newMsg, this);        
    }

    public void onMessage(Message message) {
        System.out.println("[Message Received] Recipient=<" + _principal.getUID() + "> From=<" + message.getOriginator() + ">");
        try {
            if ((message.getContentType() != null) &&
                (message.getContentType().equalsIgnoreCase("application/x-iim-poll") ||
                message.getContentType().equalsIgnoreCase("application/x-iim-poll-reply"))) {
                String content = message.getContent();
                PollHelper ph = new PollHelper(content);
                int noOfAnswers = ph.countAnswers();
                System.out.println("Poll Question: " + ph.getQuestion());
                System.out.println("Answers: ");
                for(int i = 0; i < noOfAnswers; i++ ) {
                    System.out.println(i + " " + ph.getAnswer(i));
                }
            } else {
                printMessage(message);
                message.sendStatus(MessageStatus.RECEIVED);
            }
            _receivedMessages.addElement(message);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    private void sendMessageReply(String[] args) throws Exception {         
//Added if statement to check whether _receivedMessages is empty.-Rahul
        if (_receivedMessages.isEmpty()) return;
        Message message = (Message)_receivedMessages.firstElement();
        System.out.println("Replying to " + message.getOriginator());
        Message newMsg = _messageService.createMessage(message.getOriginator());
        String sub = prompt("Subject: ", true);
        String ret = prompt("Message: ", true);

        newMsg.setHeader("subject", sub);
        MessagePart part = newMsg.newPart();
        part.setContent(ret);
        newMsg.addPart(part);
                        
        message.sendReply(newMsg);
        
        _receivedMessages.remove(message);
    }
    
    private void sendMessageStatus(String[] args) throws Exception {           
        //Added if statement to check whether _receivedMessages is empty.-Rahul
        if (_receivedMessages.isEmpty()) return;
        Message message = (Message)_receivedMessages.firstElement();
        _receivedMessages.remove(message);
        try {
            message.sendStatus(MessageStatus.READ);
        }
        catch (Exception e) {
            System.out.println("Exception thrown in sendMessageStatus");            
        }
    }
        
    public boolean onX509Certificate(java.security.cert.X509Certificate[] chain)
    {
        return true;
    }
    
    public boolean continueInClear(){
        return true;
    }
    
    public void securityHandshakeComplete(){
    }
    
    public boolean useTLS(){
        return true;
    }
    
    public int useAuthenticationMechanism(String mechanisms[]){
        return 0;
    }
    
    public void authenticationComplete(){
    }
    

    public boolean Ask_onX509Certificate(java.security.cert.X509Certificate[] chain)
    {
        StringBuffer chainS = new StringBuffer();

        chainS.append("Untrusted certificate received from server:\n");
        chainS.append("---------------------\n");
        for (int i=0; i<chain.length; i++) {
            chainS.append("\nIssuer DN: " + chain[i].getIssuerDN());
            chainS.append("\nSubject DN: " + chain[i].getSubjectDN());
            chainS.append("\nValidity: from " + chain[i].getNotBefore() + " to " + chain[i].getNotAfter());
            chainS.append("\n---------------------\n");
        }

        System.out.println(chainS.toString());
        try {
            return getYesOrNo("Do you trust this certificate? ", true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // presence error
    public void onError(String pURL, String errorText) 
    {
        System.out.println("[Presence Service Error] Destination=" + pURL + " cause=" + errorText);
    }
    
    public void onError(CollaborationException e) 
    {
        e.printStackTrace();
        _session = null;
        _presenceService = null;
        _newsService = null;
        _conferenceService = null;
        _messageService = null;
        _personalStoreService = null;
        _principal = null;

    }
    
    /**
     * invoked when the message has been disposed of by one of the
     * recipients.
     * @param destination message recipient address
     * @param deliveryStatus status code as defined in MessageStatus
     * @see MessageStatus
     */
    public boolean onReceipt(String destination,int deliveryStatus) 
    {
        /* screws up poll replies, they print after a pollr
         * and on the same line, just after prompt, e.g:
         *  Index of the answer of your choice: [Message Status Received] Recipient=<1500003@red1> From=<1500001@red1> Status=<1>
        if (inTestingMode) {
            try {
                Thread.sleep(testModeWaitMsec);
            } catch  (Exception e) {
                System.out.println("Exception " + e);
            }
        }
        */
        System.out.println("[Message Status Received] Recipient=<" + _principal.getUID() + "> From=<" + destination + "> Status=<" + deliveryStatus + ">");
        return true;  // may cause memory leak
    }
    
    /**
     * invoked when a reply to a message is received
     * @param message reply message
     */
    public void onReply(Message message) {
        try {
            if(message.getContentType() != null && 
               (message.getContentType().equalsIgnoreCase("application/x-iim-poll-reply") ||
                message.getContentType().equalsIgnoreCase("application/x-iim-poll"))) {
                PollResponse pr = new PollResponse(message.getContent());
                Poll ph = (Poll)_polls.get(pr.getPollID());
                System.out.println("[Poll Reply Received] Recipient=<" + _principal.getUID() + "> Question=<" + ph.getQuestion() + "> Answer=<" + ph.parseAnswer(message.getContent()) + ">");
            } else {
                printMessage(message);
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    
    public void onEvent(PersonalStoreEvent event) {
        switch(event.getType()) {
            case PersonalStoreEvent.TYPE_ADDED : 
                System.out.println("Contact added: " + event.getEntry().getDisplayName());
                break;

            case PersonalStoreEvent.TYPE_REMOVED :  
                System.out.println("Contact removed: " + event.getEntry().getDisplayName());
                break;

            case PersonalStoreEvent.TYPE_MODIFIED :  
                System.out.println("Contact modified: " + event.getEntry().getDisplayName());
                break;

            case PersonalStoreEvent.TYPE_DATA :  
                System.out.println("Contact Event Received: " + event.getEntry().getDisplayName() +"\n    " + event.getPayload().getName() + " = " + event.getPayload().getData());
                break;
        }
    }
    
    // send a poll message
    private void sendPollMessage(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("poll [ users ]");
            return;
        }

        String names;
        if (args.length >= 2) {
            names = args[1];
            for (int i = 2 ; i < args.length ; i++) {
                names += " " + args[i];
            }
        } else {
            names = prompt("To: ", false);
        }

        String question = prompt("Question: ", false);
        
        ArrayList answers = new ArrayList();
        for (;;) {
            String answer = prompt("Answer (return if no more):", false);
            if (answer.equals("")) break;
            answers.add(answer);
        }
        
        boolean allowCustom = false;
        String sCustom = prompt("Allow custom answers? (y/n): ", false);
        if (sCustom.equalsIgnoreCase("y")) allowCustom = true;
        Poll helper = new Poll(question, answers, allowCustom);
        
        Message message = _messageService.createMessage();
        message.setContentType("application/x-iim-poll");
        message.setContent(helper.toString());
            
        StringTokenizer st = new StringTokenizer(names);
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            message.addRecipient(name);
        }
        _polls.put(helper.getPollID(), helper);    
        _messageService.sendMessage(message, this);
    }

    
        
    // send a poll response
    private void sendPollResponse(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("pollr");
            return;
        }

//Added if statement to check whether _receivedMessages is empty.-Rahul
        if (_receivedMessages.isEmpty()) return;
        Message message = (Message)_receivedMessages.firstElement();
        PollHelper helper = new PollHelper(message.getContent());

        System.out.println("Poll Question: " + helper.getQuestion());
        int noOfAnswers = helper.countAnswers();
        System.out.println("Answers: " );
        for(int i = 0; i < noOfAnswers; i++ ) {
             System.out.println(i  + " " + helper.getAnswer(i) );
        }
        
        int iAnswer = -1;
        String answer = prompt("Index of the answer of your choice: ", false);
        try {
            iAnswer = (new Integer(answer)).intValue();
        } catch (Exception e) {
            answer = prompt("Custom answer: ", false);            
        }
        
        Message reply = _messageService.createMessage(message.getOriginator());
        reply.setContentType("application/x-iim-poll-reply");
        message.setContentType("application/x-iim-poll-reply");
        if (iAnswer >= 0) {
            reply.setContent(helper.createResponse(iAnswer));
        } else {
            reply.setContent(helper.createResponse(answer));
        }
        
        message.sendReply(reply);
        _receivedMessages.remove(message);
    }
 
    ///////////////////////////////////////////////////////////////
    // NOTIFICATION SERVICE : END
    ///////////////////////////////////////////////////////////////



    



    ///////////////////////////////////////////////////////////////
    // PERSONAL STORE SERVICE : BEGIN
    ///////////////////////////////////////////////////////////////

    private Collection getFolders(String args[])  throws Exception
    {
        Collection folders;
        if (args.length > 0) {
            CollaborationPrincipal p = _session.createPrincipal(args[0]);
            folders = _personalStoreService.getFolders(p, PersonalStoreFolder.CONTACT_FOLDER);
        } else {
            folders = _personalStoreService.getFolders(PersonalStoreFolder.CONTACT_FOLDER);
        }
        return folders;
    }


    private PersonalStoreFolder findFolder(Collection folders, String name)
    {
        for (Iterator i = folders.iterator() ; i.hasNext() ; ) {
            PersonalStoreFolder f = (PersonalStoreFolder)i.next();
            if (f.getDisplayName().equals(name)) return f;
        }
        return null;
    }


    private void listContacts(String args[]) throws Exception 
    {
        Collection folders = getFolders(args);
        for (Iterator i = folders.iterator() ; i.hasNext() ; ) {
            PersonalStoreFolder f = (PersonalStoreFolder)i.next();
            Collection entries = f.getEntries();
            System.out.println(" - " + f.getDisplayName());
            for (Iterator j = entries.iterator() ; j.hasNext() ;) {
                PersonalStoreEntry e = (PersonalStoreEntry)j.next();
                if (e.getType() == PersonalStoreEntry.CONTACT) {
                    PersonalContact c = (PersonalContact)e;
                    System.out.println("     - " + c.getDisplayName() + " <" + c.getAddress(PersonalContact.IM) + ">");
                } else if (e.getType() == PersonalStoreEntry.GROUP) {
                    PersonalGroup c = (PersonalGroup)e;
                    System.out.println("     - " + c.getDisplayName() + " <" + c.getDistinguishedName() + ">");
                } else {
                    System.out.println("     - " + e.getDisplayName() + " <" + e.getEntryId() + ">");
                }
            }
        }
    }


    private void expandContactGroup(String args[]) throws Exception {
        Collection folders = getFolders(args);
        String folder = prompt("Folder name          :", false);
        String id     = prompt("Group id             :", false);

        PersonalStoreFolder f = findFolder(folders, folder);
        if (f == null) {
            System.out.println("No Such Folder: " + folder);
            return;
        }
            
        PersonalStoreEntry e;
        if (args.length > 0) {
            CollaborationPrincipal p = _session.createPrincipal(args[0]);
            e = (PersonalGroup)_personalStoreService.getEntry(p, PersonalStoreEntry.GROUP, id);
        } else {
            e = (PersonalGroup)_personalStoreService.getEntry(PersonalStoreEntry.GROUP, id);
        }
        if (e == null) {
            System.out.println("No Such Entry: " + id);
            return;
        }
            
        Collection c = ((PersonalGroup)e).expand();

        System.out.println("Listing group members - START");
        for (Iterator i = c.iterator() ; i.hasNext() ; ) {
            CollaborationPrincipal principal = (CollaborationPrincipal)i.next();
            System.out.println("    " + principal.getDisplayName() + " <" +
                               principal.getUID() + ">");
        }
        System.out.println("Listing group members - END");
    }

    private void searchEntries(String args[]) throws Exception {
        String type    = prompt("Option (StartWith, EndsWith, Contains, Equals): ", false);
	String attribute    = prompt("Option (mail, uid, name): ", false);
        String pattern = prompt("Search pattern: ", false);
        int searchType = PersonalStoreService.SEARCHTYPE_CONTAINS;

        if (type.equalsIgnoreCase("startwith")) {
            searchType = PersonalStoreService.SEARCHTYPE_STARTSWITH;
        } else if (type.equalsIgnoreCase("endswith")) {
            searchType = PersonalStoreService.SEARCHTYPE_ENDSWITH;
        } else if (type.equalsIgnoreCase("contains")) {
            searchType = PersonalStoreService.SEARCHTYPE_CONTAINS;
        } else if (type.equalsIgnoreCase("equals")) {
            searchType = PersonalStoreService.SEARCHTYPE_EQUALS;
        }

        int searchAttribute = PersonalStoreService.NAME_ATTRIBUTE;

        if (attribute.equalsIgnoreCase("mail")) {
            searchAttribute = PersonalStoreService.MAIL_ATTRIBUTE;
        } else if (attribute.equalsIgnoreCase("uid")) {
            searchAttribute = PersonalStoreService.UID_ATTRIBUTE;
        } else {
            searchAttribute = PersonalStoreService.NAME_ATTRIBUTE;
        }

        PersonalStoreEntry[] entries = (PersonalStoreEntry[])_personalStoreService.search(searchType, pattern, PersonalStoreEntry.CONTACT, searchAttribute);
        if (entries == null) {
            System.out.println("No entries found");
        } else {
            System.out.println("Start entries listing");
            for (int i = 0; i < entries.length; i++) {
                PersonalStoreEntry entry = entries[i];
                if (entry.getType() == PersonalStoreEntry.CONTACT) {
                    System.out.println("  " + entry.getDisplayName() + " (" + ((PersonalContact)entry).getAddress(PersonalContact.IM) + ")");
                } else if (entry.getType() == PersonalStoreEntry.GROUP) {
                    System.out.println("  " + entry.getDisplayName() + " (" + ((PersonalGroup)entry).getDistinguishedName() + ")");
                } else {
                    System.out.println("  " + entry.getDisplayName() + " (" +
                                   entry.getEntryId() + ")");
                }
            }
            System.out.println("End entries listing");
        }
    }

    private void addContact(String args[]) throws Exception {
        Collection folders = getFolders(args);
        String folder  = prompt("Folder name: ", false);
        String name    = prompt("Contact id: ", false);

        PersonalStoreFolder f = findFolder(folders, folder);
        if (f == null) {
            //throw new Exception("No such Folder: " + folder);
            System.out.println("No such Folder: " + folder);
        }

        String localUID = getLocalPartFromAddress(name); 
                      
        CollaborationPrincipal[] searchResults =
                                    _personalStoreService.searchPrincipals(
                                                PersonalStoreService.SEARCHTYPE_EQUALS,
                                                localUID);
        boolean found = false;
        CollaborationPrincipal cp = null;
	if (searchResults != null) {
	    for (int i = 0; i < searchResults.length; i++) {
		cp = searchResults[i];
		if(cp.getUID().equalsIgnoreCase(name)) {
		    found = true;
		    break;
		}
	    }
	}
              
        if (found) {
                // check if the contact exist already or not
                PersonalContact entry = (PersonalContact)_personalStoreService.getEntry(PersonalStoreEntry.CONTACT, name);
                if (entry == null) {
                    entry = (PersonalContact)_personalStoreService.createEntry(PersonalStoreEntry.CONTACT, cp.getDisplayName());
                }
                entry.addAddress(PersonalContact.IM, name, 0);
                entry.addToFolder(f);
                entry.save();
        } else {
            //throw new CollaborationException("No entry found in the directory with the name " + name);
            System.out.println("No entry found in the directory with the name " + name);
        }
        
    }
    
    public void addLegacyContact(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("alcon [ entity ]");
            return;
        }
        // For the time being just subscribe, according to the jep
        subscribePresenceInfo(args);             
    }

    private void removeContact(String args[]) throws Exception {
        Collection folders = getFolders(args);
        String folder = prompt("Folder name: ", false);
        String id = prompt("Contact id: ", false);

        PersonalStoreFolder f = findFolder(folders, folder);
        if (f == null) {
//Commented the throw exception and instead printed the error string. -Rahul
//            throw new Exception("No such Folder: " + folder);
            System.out.println("No Such Folder: " + folder);
            return;
        }
            
        PersonalStoreEntry e;
        if (args.length > 0) {
            CollaborationPrincipal p = _session.createPrincipal(args[0]);
            e = (PersonalContact)_personalStoreService.getEntry(p, PersonalStoreEntry.CONTACT, id);
        } else {
            e = (PersonalContact)_personalStoreService.getEntry(PersonalStoreEntry.CONTACT, id);
        }
        if (e == null) {
//Commented the throw exception and instead printed the error string. -Rahul
//            throw new Exception("No such Entry: " + id);
            System.out.println("No Such Entry: " + id);
            return;
        }
            
        e.removeFromFolder(f);
        e.save();
    }
    
    public void removeLegacyContact(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("rlcon [ entity ]");
            return;            
        }
        // for the time being just unsubscribe
        unsubscribePresenceInfo(args);
    }
    
    private void addContactGroup(String args[]) throws Exception {
        Collection folders = getFolders(args);
        String folder  = prompt("Folder name  :", false);
        String name    = prompt("Group id     :", false);

        PersonalStoreFolder f = findFolder(folders, folder);
        if (f == null) {
            throw new Exception("No such Folder: " + folder);
        }

        boolean found = false;
        PersonalStoreEntry[] searchResults = _personalStoreService.search(PersonalStoreService.SEARCHTYPE_CONTAINS, name, PersonalStoreEntry.GROUP);
        if (searchResults == null) {
            System.out.println("No entry found in the directory with the name " + name);
            return;
        }
        PersonalStoreEntry e = null;
        for (int i = 0; i < searchResults.length; i++) {
            e = searchResults[i];
            if (e.getDisplayName().equalsIgnoreCase(name)) {
                found = true;
                break;
            }
        }

        if (found) {
            // check if the contact exist already or not
            PersonalGroup entry = (PersonalGroup)_personalStoreService.getEntry(PersonalStoreEntry.GROUP, e.getEntryId());
            if (entry == null) {
                entry = (PersonalGroup)_personalStoreService.createEntry(PersonalStoreEntry.GROUP, e.getDisplayName());
            }
            entry.setDistinguishedName(e.getEntryId());
            entry.addToFolder(f);
            entry.save();
        } else {
            System.out.println("No entry found in the directory with the name " + name);
        }
    }

    private void removeContactGroup(String args[]) throws Exception {
        Collection folders = getFolders(args);
        String folder = prompt("Folder name          :", false);
        String id     = prompt("Group id             :", false);

        PersonalStoreFolder f = findFolder(folders, folder);
        if (f == null) {
            System.out.println("No Such Folder: " + folder);
            return;
        }
            
        PersonalStoreEntry e;
        if (args.length > 0) {
            CollaborationPrincipal p = _session.createPrincipal(args[0]);
            e = (PersonalGroup)_personalStoreService.getEntry(p, PersonalStoreEntry.GROUP, id);
        } else {
            e = (PersonalGroup)_personalStoreService.getEntry(PersonalStoreEntry.GROUP, id);
        }
        if (e == null) {
            System.out.println("No Such Entry: " + id);
            return;
        }
            
        e.removeFromFolder(f);
        e.save();
    }
    
    private void removeContactFolder(String args[]) throws Exception {
        Collection folders = getFolders(args);
        String folder = prompt("Folder name:", false);

        PersonalStoreFolder f = findFolder(folders, folder);
        if (f == null) {
            throw new Exception("No such Folder: " + folder);
        }
        f.remove();
    }
    
    private void renameContactFolder(String args[]) throws Exception {
        Collection folders = getFolders(args);
        String oldFolder = prompt("Old Folder name:", false);
        String newFolder = prompt("New Folder name:", false);

        PersonalStoreFolder f = findFolder(folders, oldFolder);
        if (f == null) {
            throw new Exception("No such Folder: " + oldFolder);
        }
        f.rename(newFolder);
        f.save();
    }
    
    private void addContactFolder(String args[]) throws Exception {

        /* ==== Add in line options principal/ folder? sometime
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("afol [ folder ] ");
            return;
        }

        String name;
        if (args.length >= 1) {
            name = args[0];
        } else {
            name = prompt("Folder name: ", false);
        }
        === */
        PersonalStoreFolder f;
        String folder = prompt("Folder name: ", false);
        if (args.length > 0) {
            CollaborationPrincipal p = _session.createPrincipal(args[0]);
            f = (PersonalStoreFolder)_personalStoreService.createEntry(p, PersonalStoreEntry.CONTACT_FOLDER, folder);
         } else {
            f = (PersonalStoreFolder)_personalStoreService.createEntry(PersonalStoreEntry.CONTACT_FOLDER, folder);
         }
        // f = (PersonalStoreFolder)_personalStoreService.createEntry(PersonalStoreEntry.CONTACT_FOLDER, name);
        f.save();
    }

    private void listProfile(String args[]) throws Exception {
        PersonalProfile profile = (PersonalProfile)_personalStoreService.getProfile();

        Map m = profile.getProperties();
        Object [] keys = m.keySet().toArray();
        Arrays.sort(keys, 0, keys.length, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((String) o1).compareTo((String)o2);
            }
        });
        //for (Iterator i = m.keySet().iterator(); i.hasNext();) {
        //String p = (String)i.next();
        for (int i = 0; i < keys.length; i++) {
            String p = (String)keys[i];
            Object propValue = m.get(p);

            if (propValue instanceof Set) {
                Object[] propValuesArray = ((Set)propValue).toArray();
                //propValuesArray = sort(propValuesArray);
                Arrays.sort(propValuesArray, 0, propValuesArray.length, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return ((String) o1).compareTo((String)o2);
                    }
                });
                System.out.print(p + " = [ ");
                for( int j = 0; j < propValuesArray.length; j++ ){
                      System.out.print(propValuesArray[j] + "  ");
                }
                System.out.println(" ]");
            } else {
                System.out.println(p + " = " + propValue);
            }
        }
    }

    private void getProfile(String args[]) throws Exception {
        String propName = prompt("Property Name :", false);
        PersonalProfile profile = (PersonalProfile)_personalStoreService.getProfile();
        String v = profile.getProperty(propName, "");
        System.out.println(propName + " = " + v);
    }

    private void changeProfile(String args[]) throws Exception {
        String propName = prompt("Property Name  :", false);
        String propVal  = prompt("Property Value :", false);        
        PersonalProfile p = (PersonalProfile)_personalStoreService.getProfile();        
        p.setProperty(propName, propVal);        
        p.save();
    }

    private void removeProfile(String args[]) throws Exception {
        PersonalProfile profile = (PersonalProfile)_personalStoreService.getProfile();
        profile.remove();
    }

    private void removeSubscription(String args[]) throws Exception 
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("rsub [ conference|news id ]");
            return;
        }

        boolean isNews = false;
        String type = "conference";
        if (args.length > 0) {
            type = args[0];
        } else {
            type = prompt("Subscription type:", false);
        }

        String id = null;
        if (args.length > 1) {
            id = args[1];
        } else {
            id = prompt("Subscription target:", false);            
        }

        PersonalStoreEntry entry = _personalStoreService.getEntry(type, id);
        if (entry != null) entry.remove();
        else System.out.println("No such Subscription: " + type + " " + id);
    }

    private void addSubscription(String args[]) throws Exception 
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("asub [ conference|news id ]");
            return;
        }

        boolean isNews = false;
        String type = "conference";
        if (args.length > 0) {
            type = args[0];
        } else {
            type = prompt("Subscription type:", false);
        }

        String id = null;
        if (args.length > 1) {
            id = args[1];
        } else {
            id = prompt("Subscription target:", false);            
        }

        PersonalStoreEntry[] entry = _personalStoreService.search(PersonalStoreService.SEARCHTYPE_EQUALS, id, type);
        if (entry != null) entry[0].save();
        else System.out.println("No such Subscription target: " + type + " " + id);
    }

    private void listSubscriptions(String args[]) throws Exception 
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("asub [ conference|news ]");
            return;
        }

        boolean isNews = false;
        String type = "conference";
        if (args.length > 0) {
            type = args[0];
        } else {
            type = prompt("Subscription type:", false);
        }

        Collection c = _personalStoreService.getEntries(type);
        for (Iterator i = c.iterator(); i.hasNext() ; ) {
            PersonalConference dest = (PersonalConference)i.next();
            System.out.println(dest.getAddress());
        }
    }


    
    ///////////////////////////////////////////////////////////////
    // PERSONAL STORE SERVICE : END
    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    // STREAMING SERVICE : START
    ///////////////////////////////////////////////////////////////

    private void startStream(String args[]) throws Exception 
    {
        String rcpt = prompt("Enter the recipient:", false);
        String fileName = prompt("Enter the file name:", false);
        boolean computeHash = getYesOrNo("Compute Hash?", false);
        String desc = prompt("Enter the description:", false);
        String methods[] = {StreamingService.INBAND_STREAM_METHOD, StreamingService.OUTBAND_STREAM_METHOD};
        File f = new File(fileName);
        try {
            SenderFileStreamingProfile profile = new SenderFileStreamingProfile(f, computeHash, desc);
            ContentStreamListenerImpl listener = new ContentStreamListenerImpl();
            ContentStream cs = _streamingService.open(rcpt,methods,profile,listener);
            listener.setContentStream(cs);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void onContentStream(String sender, ReceiverStreamingProfile profile, ContentStream stream) {
        ReceiverFileStreamingProfile fileProfile = (ReceiverFileStreamingProfile)profile;
        System.out.println("Received a request for content streaming from " + sender);
        System.out.println("\tName - " + fileProfile.getName());
        System.out.println("\tDescription - " + fileProfile.getDescription());
        System.out.println("\tSize - " + fileProfile.size());
        String methods[] = stream.getSupportedMethods();
        for (int i = 0; i < methods.length; i++) {
            System.out.println("\t\tMethod[" + i + "] " + methods[i]);
        }
        try {
            boolean ret =  getYesOrNo("Do you accept the stream?", true);
            if(ret) {
                String dir = prompt("Enter the dir to save the file:", false, true);
                File f = new File(dir);
                fileProfile.addOutput(f);
                int i = -1;
                while(true) {
                    try {
                        String index = prompt("Enter the preferred method[0]:", false, "0", true);
                        i = Integer.parseInt(index);
                        if (i >= 0 && i < methods.length) break;
                    } catch(NumberFormatException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Enter correct values");
                }
                ContentStreamListenerImpl listener = new ContentStreamListenerImpl(profile);
                listener.setContentStream(stream);
                stream.accept(methods[i], profile, listener);
            } else {
                String reason = prompt("What is the reason for rejection?\n Enter 1. if Method is not supported or \n2. if The file is not acceptable or the custom reason:", false, true);
                if (reason.equals("1")) {
                    stream.reject(ContentStream.METHOD_NOT_SUPPORTED);
                } else if (reason.equals("2")) {
                    stream.reject(ContentStream.BAD_REQUEST);
                } else {
                    stream.reject(reason);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    ///////////////////////////////////////////////////////////////
    // STREAMING SERVICE : END
    ///////////////////////////////////////////////////////////////

    
    ///////////////////////////////////////////////////////////////
    // COMMON UTILITIES : BEGIN
    ///////////////////////////////////////////////////////////////
    
    protected static void printMessage(Message message) {
        //display the message
        System.out.println("");
        //System.out.println("Message received by " + _principal.getUID());
                
        try {
            System.out.println("    Subject: " + message.getHeader("subject"));
            System.out.println("    Content-type: " + message.getContentType());
            MessagePart[] parts = message.getParts();
            for (int i = 0 ; i < parts.length ; i++) {
                System.out.println("    Part " + i);
                System.out.println("        Content-type: " + parts[i].getContentType());
                try {
                    BufferedReader br=new BufferedReader(new InputStreamReader(parts[i].getInputStream()));
                    String str;
                    while((str=br.readLine()) != null) System.out.println(str);
    
                } catch (Exception e) { 
                    e.printStackTrace();
                }
            }
        } catch(Exception ce) {
            ce.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////
    // COMMON UTILITIES : END
    ///////////////////////////////////////////////////////////////



    //////////////////////////////////////////////////////////////////
    // MAIN : BEGIN
    //////////////////////////////////////////////////////////////////

    private static void exitUsage(String errorText) 
    {
        System.out.println("usage error: " + errorText);
        System.out.println("usage: \n\tjava org.netbeans.lib.collab.sample.Shell [-i <input-file> -factory <session-factory-class-name>] [-debug] [-sso] [-org <orgname>]");
        System.exit(0);
    }
    
    static String _inputFileName = null;
    public static void readArgs(String[] argv)
    {
        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-i")) {
                if (i + 1 < argv.length) {
                    i++;
                    _inputFileName = argv[i];
                } else  {
                    exitUsage("Missing input file");
                }
                
            } else if (argv[i].equals("-factory")) {
                if (i + 1 < argv.length) {
                    i++;
                    // set the Collaboration Session Factory to use
                    System.setProperty(CollaborationSessionFactory.systemProperty, argv[i]);
                } else  {
                    exitUsage("Missing factory");
                }
                
            } else if (argv[i].equals("-debug")) {
                System.setProperty("com.iplanet.im.client.api.debug", "true");
                System.setProperty("com.sun.im.xmpp.log", "debug");
                System.setProperty("org.netbeans.lib.collab.xmpp.log", "debug");
		try {
		    Logger logger = LogManager.getLogger("org.netbeans.lib.collab");
		    PatternLayout layout = new PatternLayout("%d{HH:mm:ss,SSS} %-5p %c [%t] %m%n");
		    ConsoleAppender appender = new ConsoleAppender(layout, "System.out");
		    logger.setLevel(Level.DEBUG);
		    logger.addAppender(appender);
		} catch (Exception e) {
		    e.printStackTrace();
		}
                
            } else if (argv[i].equals("-org") ||
                       argv[i].equals("-orgname")) {
                if (i + 1 < argv.length) {
                    i++;
                    _defaultDomain = argv[i];
                } else  {
                    exitUsage("Missing sso org");
                }
            } else if (argv[i].equals("-service") ||
                       argv[i].equals("-host")) {
                if (i + 1 < argv.length) {
                    i++;
                    defaultServer = argv[i]; 
                } else  {
                    exitUsage("Missing host name");
                }
                
            } else if (argv[i].equals("-sso")) {
                _usesso = true;
            } else {
                exitUsage("Illegal option: " + argv[i]);
            }
        }
    }
    

    public static void main(String[] argv) {

        try {
            BufferedReader br = null;
            readArgs(argv);
            
            if (_inputFileName != null) {
                br = new BufferedReader(new FileReader(_inputFileName));
            }
            Shell interp;
            if (br != null) {
                interp = new Shell(br, false);
            } else {
                interp = new Shell();
            }
            interp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
    
    /** invoked when a subscribe request is received
     */
    public void onSubscribeRequest(Presence p) {
        Object o[] = p.getTuples().toArray();
        PresenceTuple pt = (PresenceTuple)o[0];
        String contact = pt.getContact();
        System.out.println("User <" + contact + "> is requesting for subscription.");
        try {
            boolean ret =  getYesOrNo("Do you approve subscription?\n", true);
            if(ret) {
                _presenceService.authorize(contact);
            } else {
                _presenceService.cancel(contact);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //printPresence(p);
    }
    
    /**
     * invoked when a presence notification is received
     *
     * @param presenceInfo XML presence document.
     * @param presentityURL subscribed presentity
     * @param expires expiration date of the subscription
     */
    public void onPresenceNotify(String presentityURL, String presenceInfo, java.util.Date expires) {
    }
    
    /** invoked when a subscribe request is approved
     */
    public void onSubscribed(Presence p) {
        Object o[] = p.getTuples().toArray();
        PresenceTuple pt = (PresenceTuple)o[0];
        System.out.println("\nUser <" + pt.getContact() + "> is subscribed");
        //printPresence(p);
    }
    
    /** invoked when a unsubscribe request is approved
     */
    public void onUnsubscribed(Presence p) {
        Object o[] = p.getTuples().toArray();
        PresenceTuple pt = (PresenceTuple)o[0];
        System.out.println("\nUser <" + pt.getContact() + "> is unsubscribed");
        //printPresence(p);
    }
    
    /** invoked when a presence received
     */
    public void onPresence(Presence p) {
        printPresence(p);
    }
    
    /** invoked when a unsubscribe request is received
     */
    public boolean onUnsubscribeRequest(Presence p) {
        Object o[] = p.getTuples().toArray();
        PresenceTuple pt = (PresenceTuple)o[0];
        System.out.println("User <" + pt.getContact() + "> is requesting for unsubscription.");
        try {
            return getYesOrNo("Do you approve unsubscription? ", true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //printPresence(p);
    }
    
    public void printPresence(Presence p) {
        Object o[] = p.getTuples().toArray();
        PresenceTuple pt = (PresenceTuple)o[0];
        if(pt.getNote() != null && pt.getNote().length() > 0 ) {
             System.out.println("[Presence Info Received] Recipient=<" + _principal.getUID() + "> Destination=<" + pt.getContact() + "> Status=<" + pt.getStatus() + "> Note=<" + pt.getNote() + ">");
        } else {
             System.out.println("[Presence Info Received] Recipient=<" + _principal.getUID() + "> Destination=<" + pt.getContact() + "> Status=<" + pt.getStatus() + ">");
         }
          /*if(pt.getNote() != null && pt.getNote().length() > 0 ) {
              System.out.println("[Presence Info Received] Recipient=<" + pt.getContact() + "> Status=<" + pt.getStatus() + "> Note=<" + pt.getNote() + ">");
          } else {
              System.out.println("[Presence Info Received] Recipient=<" + pt.getContact() + "> Status=<" + pt.getStatus() + ">");
          }*/
    }
    
    private int accessS2accessInt(String access) {
        int accesslevel = -1;
        if (access.equalsIgnoreCase("NONE")) {
            accesslevel = Conference.NONE;
        } else if (access.equalsIgnoreCase("LISTEN")) {
            accesslevel = Conference.LISTEN;
        } else if (access.equalsIgnoreCase("PUBLISH")) {
            accesslevel = Conference.PUBLISH;
        } else if (access.equalsIgnoreCase("MANAGE")) {
            accesslevel = Conference.MANAGE;
        }
        return accesslevel;
    }
    
    private String accessInt2accessS(int accesslevel) {
        String access = null;
        if (accesslevel == Conference.NONE) {
            access = "NONE";
        } else if (accesslevel == Conference.PUBLISH) {
            access = "PUBLISH";
        } else if (accesslevel == Conference.LISTEN) {
            access = "LISTEN";
        } else if (accesslevel == Conference.MANAGE) {
            access = "MANAGE";
        }  
        return access;
    }
    
    /**
     * extract the domain component of an address.
     * If no domain component is found, the specified
     * default domain is returned
     */
    static String getDomainFromAddress(String in, String defaultDomain) {
        int i = in.lastIndexOf('@');
        if (i > 0) {
            if (in.charAt(i-1) != '\\') {
                return in.substring(i+1);
            }
        }
        return defaultDomain;
    }
    
    static String getLocalPartFromAddress(String in) {
        int i = in.lastIndexOf('@');
        if (i > 0) {
            if (in.charAt(i-1) != '\\') return in.substring(0, i);
        }
        return in;
    }
    
    static String getResourceFromAddress(String in) {
        int i = in.lastIndexOf('/');
        if (i > 0) {
            if (in.charAt(i-1) != '\\') return in.substring(i+1);
        }
        return null;
    }
    
    /**
     * append specified domain component if no domain is present.
     */
    static String appendDomainToAddress(String in, String defaultDomain) {
        int i = in.lastIndexOf('@');
        if (i > 0) {
            if (in.charAt(i-1) != '\\') return in;
        }
        return in + "@" + defaultDomain;
    }
    
    /** invoked when a unsubsribe request is received
     */
    public void onUnsubscribe(Presence p) {
    }    
    
    //////////////////////////////////////////////////////////////////
    // MAIN : END
    //////////////////////////////////////////////////////////////////
    
}
