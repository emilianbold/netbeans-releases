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

package org.netbeans.lib.collab.xmpp;

import java.util.*;
import java.io.*;
import java.net.*;

import java.nio.channels.*;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.jabberstudio.jso.sasl.SASLClientInfo;
import org.jabberstudio.jso.sasl.SASLFeatureConsumer;
import org.jabberstudio.jso.sasl.SASLMechanism;
import org.jabberstudio.jso.sasl.SASLMechanismManager;
import org.jabberstudio.jso.sasl.SASLMechanismsFeature;
import org.jabberstudio.jso.tls.StartTLSFeature;
import org.jabberstudio.jso.tls.StartTLSSocketFeatureConsumer;
import org.jabberstudio.jso.tls.StartTLSSocketStreamSource;
import org.jabberstudio.jso.util.XPathListener;
import org.jabberstudio.jso.x.core.BindQuery;
import org.netbeans.lib.collab.MediaService;
import org.saxpath.SAXPathException;
import org.jabberstudio.jso.xpath.XPathSupport;
import org.jabberstudio.jso.tls.StartTLSPacket;

import org.netbeans.lib.collab.*;
import org.netbeans.lib.collab.util.*;
import org.netbeans.lib.collab.xmpp.jingle.*;

import org.jabberstudio.jso.*;
import org.jabberstudio.jso.event.*;
import org.jabberstudio.jso.io.*;
import org.jabberstudio.jso.io.src.ChannelStreamSource;
import org.jabberstudio.jso.x.core.*;
import org.jabberstudio.jso.x.core.AuthQuery.Method;
import org.jabberstudio.jso.x.si.SIQuery;
import org.jabberstudio.jso.x.sift.FileTransferProfile;
import org.jabberstudio.jso.x.disco.*;
import org.jabberstudio.jso.x.xdata.*;
import org.jabberstudio.jso.util.*;
import org.jabberstudio.jso.util.ByteCodec;
import org.jabberstudio.jso.x.info.OutOfBandExtension;

import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.*;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.*;
import org.netbeans.lib.collab.xmpp.jso.iface.x.event.*;
import org.netbeans.lib.collab.xmpp.jso.iface.x.amp.*;

import org.apache.log4j.*;


/**
 *
 * @author Vijayakumar Palaniappan
 * @author Rahul Shah
 * @author Jacques Belissent
 * @author Mridul Muralidharan
 */
public class XMPPSession implements CollaborationSession
{

    /**
     * Creates a new instance of XMPPSession
     *
     * 1. open XML Ouptut stream to server and write:
     * <pre>
     * C: <?xml version='1.0'?>
     * <stream:stream
     * to='server'
     * xmlns='jabber:client'
     * xmlns:stream='http://etherx.jabber.org/streams'
     * version='1.0'>
     * </pre>
     * 2. read response from server
     * <pre>
     * S: <?xml version='1.0'?>
     * <stream:stream
     * from='shakespeare.lit'
     * id='id_123456789'
     * xmlns='jabber:client'
     * xmlns:stream='http://etherx.jabber.org/streams'
     * version='1.0'>
     * </pre>
     *
     * 3. server sends features (starttls + SASL mechanisms)
     * <pre>
     * <stream:features>
     * <starttls xmlns='urn:ietf:params:xml:ns:xmpp-tls'>
     * <required/>
     * </starttls>
     * <mechanisms xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>
     * <mechanism>DIGEST-MD5</mechanism>
     * <mechanism>PLAIN</mechanism>
     * </mechanisms>
     * </stream:features>
     * </pre>
     * 4. start tls if requested by either client or server
     * 5. authenticate
     *
     */
    static Logger xmpplogger = LogManager.getLogger("org.netbeans.lib.collab.xmpp.traffic");

    public long getShortRequestTimeout() {
        return shortRequestTimeout;
    }

    protected void setShortRequestTimeout(long timeout) {
        this.shortRequestTimeout = timeout;
    }

    class DebugPacketListenerClass implements org.jabberstudio.jso.event.PacketListener {

        public void packetTransferred(PacketEvent evt) {
            try {
                org.jabberstudio.jso.Packet packet = evt.getData();
                if(packet instanceof org.jabberstudio.jso.Message) {
                    Extension ext = null;
                    if((ext = packet.getExtension(IBB_NAMESPACE)) != null) {
                        Packet temp = (Packet)packet.copy();
                        temp.getExtension(IBB_NAMESPACE).clearText();
                        packet = temp;
                    }
                }


                if (evt.getType() == PacketEvent.RECEIVED) {
                    xmpplogger.debug("[" + getCurrentUserJID() +  "]" + " Received a packet: " + packet.toString());
                } else if (evt.getType() == PacketEvent.SENT) {
                    if (!((packet instanceof InfoQuery) &&
                           packet.getType().equals(InfoQuery.SET) &&
                         ((packet.getExtension(AuthQuery.NAMESPACE) != null) ||
                          (packet.getExtension(RegisterQuery.NAMESPACE) != null))))
                    {
                        xmpplogger.debug( "[" + getCurrentUserJID() +  "]" + " Sent a packet: " + packet.toString());
                    }
                } else {
                    xmpplogger.debug("packet: " + packet.toString());
                }
            } catch(Exception e) {
                XMPPSessionProvider.error(e.toString(), e);
            }
        }
    }


    class ServerRedirectionListenerClass implements org.jabberstudio.jso.event.PacketListener {

        public void packetTransferred(PacketEvent evt) {
            final org.jabberstudio.jso.Packet packet = evt.getData();

            if ((packet instanceof StreamError) && (!_logout)) {
                if(StreamError.SEE_OTHER_HOST_CONDITION.equals(
                            ((StreamError)packet).getDefinedCondition())) {
                                    _see_other_host = (StreamError)packet;
                                    XMPPSessionProvider.debug(
                                            "Connection Redirect received");
                }
                // When we get a stream error from the server , notify only if not in legacy mode.
                else if (!isAuthLegacyModeEnabled()){

                    notifyStreamError((StreamError)packet);
                }
            }
        }
    }

    class PacketListenerClass implements org.jabberstudio.jso.event.PacketListener {

        public void packetTransferred(PacketEvent evt) {
            final org.jabberstudio.jso.Packet packet = evt.getData();

            // if sendAndWatch was used then let this packet be
            // handled by other listener registered by sendAndWatch.
            if (removeSendAndWatchID(packet)) return;

            if (packet instanceof org.jabberstudio.jso.Message) {
                //(new Thread(new Runnable() {
                //    public void run() {
                //        processMessage(packet);
                //    }
                //})).start();
                processMessage(packet);

            } else if (packet instanceof PrivacyQuery) {
                //_presenceService.processPrivacyQuery((PrivacyQuery)packet);
            } else if (packet instanceof InfoQuery) {
                //(new Thread(new Runnable() {
                //    public void run() {
                //        processInfoQuery(packet);
                //    }
                //})).start();
                processInfoQuery(packet);
            } else if (packet instanceof org.jabberstudio.jso.Presence) {
                try {
                    processPresence((org.jabberstudio.jso.Presence)packet);
                } catch(CollaborationException e) {
                    XMPPSessionProvider.error(e.toString(),e);
                } catch(Exception e) {
                    XMPPSessionProvider.error(e.toString(),e);
                }
            } else if ((packet instanceof StreamError) && (!_logout)) {
                if(StreamError.SEE_OTHER_HOST_CONDITION.equals(
                            ((StreamError)packet).getDefinedCondition())) {
                                    _see_other_host = (StreamError)packet;
                                    XMPPSessionProvider.debug(
                                            "Connection Redirect received");
                } else {
                    notifyStreamError((StreamError)packet);
                }
            } else {
                XMPPSessionProvider.debug("UnSupported XMPP XML Stanza : " + packet.toString());
                // throw new CollaborationException("UnSupported XMPP XML Stanza: " + packet.toString());
            }
        }
    }

    class StatusListenerClass implements org.jabberstudio.jso.event.StreamStatusListener {
        public void statusChanged(StreamStatusEvent evt) {
            if (evt.getContext().isInbound()){
                if (evt.getNextStatus() == Stream.DISCONNECTED) {
                    xmpplogger.info("disconnected");
                    if ((!_logout) && (!evt.isExceptional())){
                        notifyStreamError(null);
                    }
                }
                if (evt.getNextStatus() == Stream.CLOSED) {
                    xmpplogger.info("closed");
                }
                if (evt.getNextStatus() == Stream.CONNECTED) {
                    xmpplogger.info("connected");
                }
                if (evt.getNextStatus() == Stream.OPENED) {
                    xmpplogger.info("opened ");
                }
            }
        }
    }

    /*
     * The packetListner thread locks the stream, so the operations which could take
     * more time should be launched in a separate thread
     * Launch the registration notification in a separate thread
     *
     */
    class RegisterNotifier implements Runnable {

        private InfoQuery _iqPacket;
        public RegisterNotifier(InfoQuery iq) {
            _iqPacket = iq;
        }

        public void run() {
            try {
            processRegisterQuery(_iqPacket);
            } catch (CollaborationException ce) {
                ce.printStackTrace();
            }
        }

    }

    class ServiceDiscovery implements MonitorListener {
        DiscoItem item;
        XMPPSession session;
        public ServiceDiscovery(XMPPSession s, DiscoItem di) {
            item = di;
            session = s;
        }

        public void monitorFailed(MonitorEvent evt) {
        }

        public void monitorFound(MonitorEvent evt) {
            try {
                Packet packet = evt.getActualPacket();
                if (packet.getType() == Packet.ERROR) {
                    return;
                }
                DiscoInfoQuery diq = (DiscoInfoQuery)packet.listExtensions(DiscoInfoQuery.NAMESPACE).get(0);
                for (Iterator iter = diq.listIdentities().iterator();iter.hasNext(); ) {
                    DiscoIdentity discoIdentity = (DiscoIdentity)iter.next();
                    String category = discoIdentity.getCategory();
                    String type = discoIdentity.getType();
                    if (PersonalStoreEntry.GATEWAY.equals(category)) {
                        if("voip".equals(type)){
                            // if the server has a voip gateway, we need to hold on to its JID
                            // This is needed by the client, so that it can redirect request to
                            // this gateway
                            _voipComponent = item.getJID();
                        }
                        else{
                            PersonalGateway pse = new XMPPPersonalGateway(session,
                            discoIdentity.getName(),
                            item.getJID().toString(),
                            discoIdentity.getType());
                            try {
                                for (Iterator i = diq.getFeatures().iterator();i.hasNext();) {
                                    ((XMPPPersonalGateway)pse).addSupportedFeature((String)i.next());
                                }
                                synchronized(_jabberServiceLock) {
                                    _gateways.put(item.getJID().toString(), pse);
                                }
                            } catch(CollaborationException e) {
                                XMPPSessionProvider.error(e.toString(), e);
                                continue;
                            }
                        }
                    } else if (PersonalStoreEntry.CONFERENCE.equals(category)) {
                        synchronized(_jabberServiceLock) {
                            _mucService = item.getJID();
                        }
                        
                        if (_conferenceService != null) {
                            synchronized(_conferenceService) {
				 _conferenceService.updateExtendedSearchSupport(item.getJID(), diq);
                                _conferenceService.notifyAll();
                            }
                        }
                    } else if ("pubsub".equals(category)) {
                        synchronized(_jabberServiceLock) {
                            _pubsubService = item.getJID();
                        }
                        if (_newsService != null) {
                            synchronized(_newsService) {
                                _newsService.notifyAll();
                            }
                        }
                    } else if ("directory".equals(category)) {
                        synchronized(_jabberServiceLock) {
                            _judService = item.getJID();
                        }
                        if (_personalStoreService != null) {
                            synchronized(_personalStoreService) {
                                _personalStoreService.notifyAll();
                            }
                        }
                    } else if ("server".equals(category) || "service".equals(category)) {
                        // remote jabber server.  Find out more in the background
                        // in the background because the remote server may be
                        // unreachable.  Choosing not to hold response waiting
                        // for another server.
                        if (!_remoteServices.contains(item.getJID())) {
                            new Thread(new DiscoRunnable(item.getJID())).start();
                        }
                    }   else {
                        XMPPSessionProvider.debug("Category " + category + " is not known");
                    }
                    _discoveredServices.add(item.getJID());
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        public void monitorTimeout(MonitorEvent evt) {
        }

    }

    private List _sessionListeners = Collections.synchronizedList(new ArrayList());
    private Map _regisListeners = new HashMap();
    Stream  _connection;
    private boolean channelRegistered = false;

    XMPPPrincipal _client;

    JID _server;
    private String _sessionID, _loginName;
    protected boolean uidHasDomain = false;

    private TreeMap _namespaces = new java.util.TreeMap();

    private XMPPSessionProvider _provider;
    private XMPPPersonalStoreService _personalStoreService;
    private XMPPPresenceService _presenceService;
    private XMPPNewsService _newsService;
    private XMPPConferenceService _conferenceService;
    private XMPPNotificationService _notificationService;
    private XMPPStreamingService _streamingService;

    private StreamSource _css;
    private Object _selectionKey;

    private Set _remoteServices = Collections.synchronizedSet(new HashSet());


    String keyValue;
    boolean ampSupported = false;
    boolean ampCondDeliverSupported = false;
    boolean ampCondExpireAtSupported = false;
    boolean ampCondMatchResourceSupported = false;
    boolean ampActionDropSupported = false;
    boolean ampActionErrorSupported = false;
    boolean ampActionNotifySupported = false;
    boolean ampActionAlertSupported = false;

    private static final long DEFAULT_REQUEST_TIMEOUT = 50000;
    private static final long DEFAULT_SHORT_REQUEST_TIMEOUT = 5000;
    private static final long DEFAULT_FEATURE_TIMEOUT = 1500;


    private long requestTimeout = getLongProperty(
            "org.netbeans.lib.collab.xmpp.XMPPSession.REQUEST_TIMEOUT" , 
            DEFAULT_REQUEST_TIMEOUT);
    private long shortRequestTimeout = getLongProperty(
            "org.netbeans.lib.collab.xmpp.XMPPSession.SHORT_REQUEST_TIMEOUT" , 
            DEFAULT_SHORT_REQUEST_TIMEOUT);
    private long featureTimeout = getLongProperty(
            "org.netbeans.lib.collab.xmpp.XMPPSession.FEATURE_TIMEOUT" ,
            DEFAULT_FEATURE_TIMEOUT);
    
    public static int IBB_MESSAGE_SIZE = 4096;
    //public static int NUM_WORKER_THREAD = 2;

    private volatile boolean _logout = false;


    static JSOImplementation _jso = JSOImplementation.getInstance();
    static StreamDataFactory _sdf = _jso.getDataFactory();

    // implied namespace
    public static final NSI IQ_NAME = new NSI("iq", null);
    public static final NSI MESSAGE_NAME = new NSI("message", null);
    public static final NSI PRESENCE_NAME = new NSI("presence", null);
    public static final NSI STORAGE_NAME = new NSI("storage", null);
    public static final NSI STORAGE_BOOKMARK_NAME = new NSI("storage", "storage:bookmarks");
    public static final String PRIVATE_NAMESPACE = "jabber:iq:private";
    public static final String IBB_NAMESPACE = "http://jabber.org/protocol/ibb";
    public static final NSI IBB_OPEN = _sdf.createNSI("open", IBB_NAMESPACE);
    public static final NSI IBB_CLOSE = _sdf.createNSI("close", IBB_NAMESPACE);
    public static final NSI IBB_DATA = _sdf.createNSI("data", IBB_NAMESPACE);
    public static final XMPPMessagePart.Base64Cod BASE64 = new XMPPMessagePart.Base64Cod();
    // new ByteCodec.Base64Codec();

    public static final String SUN_PRIVATE_NAMESPACE = "sun:xmpp:properties";
    public static final NSI SUN_PRIVATE_NAME = new NSI("x", SUN_PRIVATE_NAMESPACE);
    public static final NSI SUN_PRIVATE_SUNMSGR_NAME = new NSI("sunmsgr", SUN_PRIVATE_NAMESPACE);
    public static final NSI SUN_PRIVATE_POLICY_NAME = new NSI("sunmsgrpolicy", SUN_PRIVATE_NAMESPACE);
    public static final NSI SEARCH_QUERY_NAME = new NSI("query", "jabber:iq:search");
    public static final NSI SUN_ATTACH_NAME = new NSI("attach", "sun:xmpp:attach");

    public static final String DEFAULT_CLIENT_CATEGORY= "client";
    public static final String DEFAULT_CLIENT_TYPE = "pc";
    public static final String DEFAULT_CLIENT_NAME = "SJSClient";
    public static final NSI SUN_PRIVATE_LDAPGROUP_NAME = new NSI("sunldapgrp", "sun:xmpp:ldapgroups");

    //public static final String AMP_NAMESPACE = "http://jabber.org/protocol/amp";
    public static final String AMP_ACTION_DROP_NAMESPACE = AMPExtension.NAMESPACE + "?action=drop";
    public static final String AMP_ACTION_ERROR_NAMESPACE = AMPExtension.NAMESPACE + "?action=error";
    public static final String AMP_ACTION_NOTIFY_NAMESPACE = AMPExtension.NAMESPACE + "?action=notify";
    public static final String AMP_ACTION_ALERT_NAMESPACE = AMPExtension.NAMESPACE + "?action=drop";
    public static final String AMP_COND_EXPIREAT_NAMESPACE = AMPExtension.NAMESPACE + "?condition=expire-at";
    public static final String AMP_COND_DELIVER_NAMESPACE = AMPExtension.NAMESPACE + "?condition=deliver";
    public static final String AMP_COND_MATCHRES_NAMESPACE = AMPExtension.NAMESPACE + "?condition=match-resource";




    // boolean variable set to true once the listPrivacyLists is invoked
    private boolean privacyListsListed = false;

    private Hashtable _ibbMessages = new Hashtable();
    private StreamSourceCreator _streamSrcCreator;

    //A list of id's for which there are already some send and watch call's
    private Map _sendAndWatchIDs = new HashMap();
    private Map inboundSendAndWatch = new HashMap();

    //list of the services which are already discovered
    private List _discoveredServices = new LinkedList();

    protected String _activePrivacyList = null;
    protected String _defaultPrivacyList = null;

    JID _mucService = null;
    JID _pubsubService = null;
    JID _judService = null;
    JID _voipComponent = null;
    Map _gateways = new Hashtable();

    private boolean _loadingServices = false;

    //The lock use to synchronize the access the jabber services
    private Object _jabberServiceLock = new Object();
    //The lock use to synchronize calls to  loadJabberServices method
    private Object _loadingServiceLock = new Object();


    private ServerRedirectionListenerClass redirectionListener = null;
    private volatile StreamError _see_other_host;
    //    private Worker _worker = new Worker(NUM_WORKER_THREAD);

    private boolean _serverFeaturesDiscovered = false;  

    private static String _nonce = "";
    private boolean _authenticated = false;

    static {
        // get a unique id root
        try {
            _nonce = Integer.toString(InetAddress.getLocalHost().hashCode());
        } catch (Exception e) {
        }
    }

    protected XMPPSession() {

    }

    /*public XMPPSession(XMPPSessionProvider fac, String serviceUrl, StreamSourceCreator streamSrcCreator) throws CollaborationException {
        init(fac, serviceUrl,null, null, -1, null, streamSrcCreator);
        connect(serviceUrl);
        try {
            registerChannelWithProvider();
        } catch(IOException ioe) {
            throw new CollaborationException(ioe);
        }
    }*/

    /** Creates new XMPPSession */
    public XMPPSession(XMPPSessionProvider fac,
                       String serviceUrl,
                       String destination,
                       String loginName,
                       String password,
                       int loginType,
                       CollaborationSessionListener listener,
                       StreamSourceCreator streamSrcCreator)
                       throws CollaborationException
    {

        init(fac, serviceUrl, destination, loginName, password,
             loginType, listener, streamSrcCreator);
         _connectAndAuthenticate(serviceUrl,password);
	 // Dont initialise features for new user registeration
	 if (null != _loginName){
         	_initFeaturesSupported();
	 }
    }

    private void _connectAndAuthenticate(String serviceUrl, String password)
                                            throws CollaborationException {

        try {
            // Connect and open stream
            connect(serviceUrl);

            // For new user registeration , the login name will be null.
            // for all other cases , it should be non-null.
            // In case the custom provider does not require a login name ,
            // then pass on "" or some custom empty string which indicates to the
            // provider to ignore the value.
            if (_loginName != null) {
                // Disable legacy mode by default
                disableAuthLegacyMode();
                authenticate(password);
            }
            // Get some stream stats
            registerChannelWithProvider();
            start();
        } catch(CollaborationException ce) {
                if (null != _fi){
                    try{
                        _fi.release();
                    }catch(StreamException stEx){}
                    _fi = null;
                }
                logout();
                if (null != _see_other_host){
                        disableAuthLegacyMode();
                        setAuthLegacyModeActive(false);
                }
                if(_see_other_host != null ||
                        // We can come here also 'cos legacy mode was enabled .. in which case
                        // retry auth with older server's semantics which are :
                        // 1) Always ignore SASL.
                        // 2) Always try jabber:iq auth - irrespective of whether it is advertised or not.
                        isAuthLegacyModeEnabled()) {
                    //because connection is closed, collaborationException
                    //is thrown for sure
                    _logout = false;

                    StreamElement ele = null;

                    if (_see_other_host != null){
                        ele = _see_other_host.getFirstElement(
                                StreamError.SEE_OTHER_HOST_CONDITION,
                                StreamError.CONDITION_NAMESPACE);
                    }
                    _connection = _jso.createStream(getStreamNameSpace());
                    if (!isAuthLegacyModeEnabled()){
                        _connection.getOutboundContext().setVersion("1.0");
                    }
                    else{
                        assert (!isAuthLegacyModeActive());
                         _see_other_host = null;
                        // Activate legacy mode.
                        setAuthLegacyModeActive(true);
                        _connectAndAuthenticate(serviceUrl,password);
                        return ;
                    }
                     if(ele == null && null != _see_other_host) {
                         ele = _see_other_host.getFirstElement("text");
                     }
                     _see_other_host = null;

                     if (ele != null) {
                         String redirectedHost = ele.normalizeText();
                         XMPPSessionProvider.debug(
                                    "Connection Redirected :  " + redirectedHost);

                         setRedirectedHost(redirectedHost);
                         _connectAndAuthenticate(redirectedHost,password);
                     } else {
                         XMPPSessionProvider.error("Unable to get the Redirected Host");
                         throw new CollaborationException("Unable to get the Redirected Host");
                     }
                } else {
                    throw ce;
                }
        } catch(Exception e) {
            logout();
            throw new CollaborationException(e);
        } finally {
            if (null != _fi){
                try{
                    _fi.release();
                }catch(StreamException stEx){}
            }
            // Disable legacy mode.
            disableAuthLegacyMode();
            setAuthLegacyModeActive(false);
        }
    }

    protected void setRedirectedHost(String host){
        /*
        CollaborationSessionListener listener = getSessionListener();

        if (listener instanceof ServerNotificationsListener){
            ((ServerNotificationsListener) listener).serverRedirected(host);
        }
         */
    }

    public CollaborationSessionListener getSessionListener() {
        return (CollaborationSessionListener)_sessionListeners.get(0);
    }

    private void init(XMPPSessionProvider fac, String serviceUrl,
                      String destination, String loginName, String password,
                      int loginType, CollaborationSessionListener listener,
                      StreamSourceCreator streamSrcCreator)
                      throws CollaborationException
     {
         _provider = fac;
         _sessionListeners.add(0,listener);
         _connection = _jso.createStream(getStreamNameSpace());
         _connection.getOutboundContext().setVersion("1.0");
         _streamSrcCreator = streamSrcCreator;
         if (loginName != null) {
             _loginName = loginName;
             uidHasDomain =
                 (StringUtility.getDomainFromAddress(loginName, null) != null);
         }


         XMPPSessionProvider.debug("destination : " + destination);

         if (destination != null) setClientJID(new JID(destination));
        /*
          if (!Utilities.isValidString(_client.getNode()) ||
          !Utilities.isValidString(_client.getResource())) {
          throw new IllegalArgumentException("Client JID does not include a node and/or resource");
          }
         */

    }

    protected int getConnectParameters(String serviceUrl , StringBuffer hostSb ,
            StringBuffer domainSb) throws CollaborationException{

        String domain = "";
        String host = "";
        int port = 5222;

        int startIndex = serviceUrl.indexOf(":");
        if (startIndex > 0) {
            host= serviceUrl.substring(0,startIndex);
            domain = host;
            port = Integer.parseInt(serviceUrl.substring(startIndex+1));
        } else {
            InetSocketAddress isa = SRVLookup.XMPPClient(serviceUrl);
            domain = serviceUrl;
            host = isa.getHostName();
            port = isa.getPort();
        }

        domain = validateDomain(domain);

        hostSb.setLength(0);
        hostSb.append(host);
        domainSb.setLength(0);
        domainSb.append(domain);

        return port;
    }

    protected String validateDomain(String domain){
        String retval = null;
        if (null != _client){
            retval = _client.getDomainName();
        }
        if (!Utilities.isValidString(retval)) {
            retval = domain;
        }
        return retval;
    }

    /**
     * This methods connects to the server and
     * and exchanges the initial stream headers
     */

    private void connect(String serviceUrl) throws CollaborationException {

        // Validate server host info
        String domain, host;
        int port;

        StringBuffer domainSb = new StringBuffer();
        StringBuffer hostSb = new StringBuffer();
        port = getConnectParameters(serviceUrl , hostSb , domainSb);

        domain = domainSb.toString();
        host = hostSb.toString();

        _server = _sdf.createJID(domain);

        if (Utilities.isValidString(_server.getNode()) ||
            Utilities.isValidString(_server.getResource())) {

                throw new IllegalArgumentException("Host JID cannot include a node or resource");
        }

        try {
            channelRegistered = false;
            _css = _streamSrcCreator.createStreamSource(host, port);
            /*
            Socket soc = _streamSrcCreator.getSocket();
            if(soc != null) {
                XMPPSessionProvider.debug(
                                "SO_SNDBUF " + soc.getSendBufferSize());
                XMPPSessionProvider.debug(
                                "SO_RCVBUF " + soc.getReceiveBufferSize());
            }
             */
            _connection.connect(_css);
            redirectionListener = new ServerRedirectionListenerClass();
            _connection.addPacketListener(PacketEvent.RECEIVED, redirectionListener);

        } catch(CollaborationException e) {
            //if it is collaborationException or subclasses of it
            //then propagate as it is
            throw e;
        } catch(Exception e) {
            //se.printStackTrace();
            throw new CollaborationException(e);
        }

        // do the initial stream header exchange
        open();
    }

    protected void configureTimeouts(){
        //setRequestTimeout(DEFAULT_REQUEST_TIMEOUT);
        //setShortRequestTimeout(DEFAULT_SHORT_REQUEST_TIMEOUT);
        setRequestTimeout(requestTimeout);
        setShortRequestTimeout(shortRequestTimeout);
        setFeatureTimeout(featureTimeout);
    }

    /**
     * This method does the initial stream header exchange.
     * This method can be overridden by the subclasses of this class.
     * The parameters of this method are not used by this method, but could be used
     * by the subclass which overrides this method to modify stream context objects.
     */
    protected void open() throws CollaborationException {
        findFeaturesInit();
        openImpl(15000);
        configureTimeouts();
        XMPPSessionProvider.debug("Opened");
        // Get some stream stats
        _server = _connection.getInboundContext().getFrom();

        setClientJID();

        /*
        if ((!uidHasDomain) && (_client != null)) {
            JID jid = new JID(_client.getJID().getNode(),
                              _server.getDomain(),
                              _client.getJID().getResource());
            _client.setJID(jid);
        }
         */
        XMPPSessionProvider.debug("FROM == " + _server);
        _sessionID = _connection.getInboundContext().getID();
        XMPPSessionProvider.debug("ID ==   " + _sessionID);

        findFeatures(getFeatureTimeout());
        processTLS();
    }

    protected void configureOutboundContext(){
        if (_server != null) {
            _connection.getOutboundContext().setTo(_server);
        } else {
            _connection.getOutboundContext().setTo(
                    new JID(_client.getJID().getDomain()));
        }
    }

    protected void openImpl(long timeout) throws CollaborationException{

        configureOutboundContext();

        try {
            XMPPSessionProvider.debug("openImpl() being invoked : " + timeout);
            _connection.open(timeout);
        } catch(StreamException se) {
            //se.printStackTrace();
            throw new CollaborationException(se);
        }
    }

    protected void setFeatureTimeout(long timeout) {
        this.featureTimeout = timeout;
    }
    
    public long getFeatureTimeout() {
        return featureTimeout;
    }
    
    private FeatureIdentifier _fi = null;
    protected void findFeaturesInit() throws CollaborationException {
        try{
            _fi = new FeatureIdentifier(getConnection());
            _fi.initialise();
        } catch(StreamException stEx){
            throw new CollaborationException(stEx);
        }
    }

    // A simple , "find if feature is present in stream" identifier.
    // We will wait until all of the 'reqd nsi list' is satisfied or
    // timeout happens.
    protected void findFeatures(long timeout) throws CollaborationException{

        try{
            _fi.process(timeout);
        }catch(StreamException stEx){
            throw new CollaborationException(stEx);
        }
        return ;
    }

    protected void processTLS() throws CollaborationException {
        
        // Dont do tls if in legacy mode.
        if (isAuthLegacyModeActive()){
            return ;
        }

        boolean tlsPresent = false;
        StartTLSFeature tlsfeature = null;

        Iterator iter = _fi.getFeatureList().iterator();

        while (iter.hasNext()){
            Object obj = iter.next();

            if (obj instanceof StartTLSFeature){
                // remove it from the feature list.
                iter.remove();
                tlsPresent = true;
                tlsfeature = (StartTLSFeature)obj;
                break;
            }
        }

        CollaborationSessionListener listener = getSessionListener();

        // If the listener does not implement SecurityListener or the
        // StreamSource is not an instance of StartTLSSocketStreamSource
        // we will not handle tls.
        // If the server specified tls as mandatory behaviour , then
        // we throw appropriate exception.
        boolean useTLS = (null != listener) &&
                (listener instanceof SecurityListener) &&
                _streamSrcCreator.isTLSSupported() &&
                ((SecurityListener)listener).useTLS();

        XMPPSessionProvider.debug("tlsPresent : " + tlsPresent + " , useTLS : " + useTLS);

        if (!useTLS){
            if (tlsPresent && tlsfeature.isRequired()){
                throw new CollaborationException("TLS support required by server");
            }

            if (listener instanceof SecurityListener){
                if (!((SecurityListener)listener).continueInClear()){
                    throw new CollaborationException("continueInClear == false and tls not supported");
                }
            }

            // Fine, we dont want to support tls and tls is not mandatory for server
            // so proceed in clear text.
            // We continue using the same set of features !
            return ;
        }

        assert (listener instanceof SecurityListener);

        if (!tlsPresent){
            if (!((SecurityListener)listener).continueInClear()){
                throw new CollaborationException("tls not supported by server");
            }
        }
        else{
            startTLSImpl();
        }
    }

    protected void resetConnection() throws CollaborationException{
        // our server throws up on this :(
        try{
            List bak = _fi.getFeatureList();
            _fi.resetFeatureList();
            _connection.drop();
            openImpl(0);
            findFeatures(getFeatureTimeout());
            List n = _fi.getFeatureList();
            if (null == n || 0 == n.size()){
                _fi.setFeatureList(bak);
            }
        }catch(StreamException stEx){
            throw new CollaborationException("tls error");
        }
    }

    protected void startTLSImpl() throws CollaborationException {

        StartTLSHandler handler = new StartTLSHandler(_sdf , _connection ,
                _streamSrcCreator , _css);
        handler.process();
        // reopen stream.
        resetConnection();
        CollaborationSessionListener listener = getSessionListener();
        if (listener instanceof SecurityListener){
            ((SecurityListener)listener).securityHandshakeComplete();
        }

    }

    protected String getLoginName() {
        return _loginName;
    }

    protected String getStreamNameSpace() {
        return Utilities.CLIENT_NAMESPACE;
    }

    protected void setClientJID() throws AuthenticationException {
        if ((!uidHasDomain) && (_client != null)) {
            try {
            JID jid = new JID(_client.getJID().getNode(),
                        _server.getDomain(),
                        _client.getJID().getResource());
             _client.setJID(jid);
            } catch (JIDFormatException jfe) {
                throw new AuthenticationException(jfe);
            }
        }
    }

    protected void setClientJID(JID jid) throws AuthenticationException {
        try {
            if (_client == null) {
                _client = new XMPPPrincipal(jid);
            } else {
                _client.setJID(jid);
            }
        } catch (JIDFormatException jfe) {
            throw new AuthenticationException(jfe);
        }
    }

    protected void fireAuthSuccessEvent(){
        _authenticated = true;
        CollaborationSessionListener listener = getSessionListener();
        if (null != listener &&
                listener instanceof AuthenticationListener){
            ((AuthenticationListener)listener).authenticationComplete();
        }
    }

    /**
     * Jabber authentication
     */
    protected void authenticate(String password) throws CollaborationException {

        List activeFeatureList = _fi.getFeatureList();
        if (null == activeFeatureList || activeFeatureList.isEmpty() ||
                // force fallback to jabber:iq auth
                isAuthLegacyModeActive()){
            // fall back on old method of doing jabber query auth.
            doAuthQueryLogin(password);
            fireAuthSuccessEvent();
            return ;
        }

        // Get list of the supported mechanism's , query user which one to use.
        Iterator iter = activeFeatureList.iterator();
        int valid = 0;
        List mechList = new ArrayList();
        Map authProviders = _provider.getAuthProviderMap();

        while (iter.hasNext()){
            Object obj = iter.next();

            if (obj instanceof SASLMechanismsFeature){
                valid ++;
                SASLMechanismsFeature feature = (SASLMechanismsFeature)obj;
                Iterator iter1 = feature.getMechanismNames().iterator();
                while (iter1.hasNext()){
                    String mechName = (String)iter1.next();
                    if (null != authProviders.get(mechName)){
                        // supported.
                        mechList.add(mechName);
                    }
                }
            }
            else if (obj instanceof AuthStreamFeature){
                valid ++;
                // always supported :-)
                mechList.add(AuthenticationListener.JABBER_IQ_AUTH_MECHANISM);
            }
        }

        if (mechList.size() > 0){
            CollaborationSessionListener listener = getSessionListener();
            // Default to using the most preffered mechanism as sent by server.
            int mechanismToUse = 0;

            if (null != listener &&
                    listener instanceof AuthenticationListener){
                mechanismToUse = ((AuthenticationListener)listener).
                        useAuthenticationMechanism((String [])
                            mechList.toArray(new String[0]));
            }

            if (mechanismToUse < 0 || mechanismToUse >= mechList.size()){
                throw new AuthenticationException("All handlers rejected");
            }

            String mechName = (String)mechList.get(mechanismToUse);
            if (!mechName.equals(AuthenticationListener.JABBER_IQ_AUTH_MECHANISM)){

                XMPPSessionProvider.debug("\nUsing SASL mechanism : " + mechName);
                assert (authProviders.get(mechName) instanceof SASLClientProviderFactory);

                SASLClientProviderFactory providerFactory =
                        (SASLClientProviderFactory)authProviders.get(mechName);
                assert (null != providerFactory);

                SASLClientProvider provider = providerFactory.createInstance(mechName);
                try{
                    provider.init();
                }catch(SASLProviderException spEx){
                    throw new CollaborationException("sasl provider exception in init" , spEx);
                }
                provider.setLoginName(_loginName);
                provider.setPassword(password);

                String toServer = null;
                if (null != _server) {
                    toServer = _server.getDomain();
                } else if (null != _client){
                    toServer = _client.getJID().getDomain();
                }
                provider.setServer(toServer);

                AuthProcessor authproc = new AuthProcessor();
                authproc.setStreamDataFactory(_sdf);
                authproc.setStream(_connection);
                authproc.saslAuthenticate(provider , mechName);
                resetConnection();
                bindResource(_client);
                // Always starting a session !
                startSession();
                fireAuthSuccessEvent();
                XMPPSessionProvider.debug("\nSUCESS Using SASL mechanism : " +
                        mechName + " !!\n");
                return ;
            }

            doAuthQueryLogin(password);
            fireAuthSuccessEvent();
        }
        else if (valid > 0){
            throw new AuthenticationException("No valid auth handler found");
        }
        else{
            // fallback for old behaviour
            doAuthQueryLogin(password);
            fireAuthSuccessEvent();
        }
        return ;
    }


    protected boolean isBindFeatureSupported() throws CollaborationException{

        List activeFeatureList = _fi.getFeatureList();
        XMPPSessionProvider.debug("isBindFeatureSupported : activeFeatureList  == " + activeFeatureList);
        if (null == activeFeatureList || activeFeatureList.isEmpty()){
            return false;
        }

        Iterator iter = activeFeatureList.iterator();
        while (iter.hasNext()){
            Object obj = iter.next();
            if (obj instanceof BindQuery){
                return true;
            }
        }
        return false;
    }

    protected boolean isSessionFeatureSupported() throws CollaborationException{

        List activeFeatureList = _fi.getFeatureList();
        XMPPSessionProvider.debug("isBindFeatureSupported : activeFeatureList  == " + activeFeatureList);
        if (null == activeFeatureList || activeFeatureList.isEmpty()){
            return false;
        }

        Iterator iter = activeFeatureList.iterator();
        while (iter.hasNext()){
            Object obj = iter.next();
            if (obj instanceof SessionQuery){
                return true;
            }
        }
        return false;
    }

    protected void startSession() throws CollaborationException{

        if (!isSessionFeatureSupported()){
            // Silently ignore ? This should be discussed more ...
            // It is not mandatory to support 'session' feature.
            //throw new CollaborationException("Session not supported");
            return ;
        }

        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        SessionQuery sq = (SessionQuery)
            _sdf.createExtensionNode(SessionQuery.NAME, SessionQuery.class);

        if (null == iq){
            // AuthenticationException ?
            throw new CollaborationException("Unable to create iq packet");
        }
        if (null == sq){
            // AuthenticationException ?
            throw new CollaborationException("Unable to create sessionquery packet");
        }

        iq.setTo(_server);
        iq.setType(InfoQuery.SET);
        iq.setID(nextID("session"));

        iq.add(sq);

        try {
            iq = (InfoQuery)sendAndWatch(iq , getRequestTimeout());
        } catch(StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
            // AuthenticationException ?
            throw new CollaborationException(se);
        }

        if (null == iq) {
            throw new CollaborationException("No reply from server");
        }
        if (InfoQuery.ERROR == iq.getType())
        {
            // AuthenticationException ?
            throw new CollaborationException(iq.getError().getText());
        }
    }

    // An extremely dirty set of fallback code in place since we had a nasty
    // bug in JES4 server and need to support it in current release for backward
    // compatibility reasons :-(
    // Essentially , even though the server would advertise bind as a feature, it would
    // be unable to actually perform a bind.
    private static final boolean supportAuthLegacyMode = System.getProperty(
            "org.netbeans.lib.collab.xmpp.XMPPSession.legacymode" , "true").
            equalsIgnoreCase("true");

    private boolean legacyModeEnabled = false;
    private boolean legacyModeActivate = false;

    protected void enableAuthLegacyMode(){
        if (supportAuthLegacyMode){
            legacyModeEnabled = true;
        }
    }
    protected boolean isAuthLegacyModeEnabled(){
        return legacyModeEnabled;
    }
    protected void disableAuthLegacyMode(){
        legacyModeEnabled = false;
    }

    private boolean isAuthLegacyModeActive(){
        return legacyModeActivate;
    }
    private void setAuthLegacyModeActive(boolean actvate){
        this.legacyModeActivate = actvate;
    }

    protected void bindResource(XMPPPrincipal client) throws CollaborationException{

        JID jid = client.getJID();
        // initFeatures would be already called - and so the feature will be
        // set appropriately.
        // initFeatures(true);

        if (!isBindFeatureSupported()){
            throw new CollaborationException("Bind not supported");
        }

        InfoQuery iqBind = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        BindQuery bq = (BindQuery)
            _sdf.createExtensionNode(BindQuery.NAME, BindQuery.class);

        if (null == iqBind){
            throw new CollaborationException("Unable to create iq packet");
        }
        if (null == bq){
            throw new CollaborationException("Unable to create bindquery packet");
        }

        iqBind.setTo(_server);
        iqBind.setType(InfoQuery.SET);
        iqBind.setID(nextID("bind"));

        bq.setJID(jid);

        bq.setResource(jid.getResource());
        iqBind.add(bq);

        // This is where the legacy mode actually gets activated.
        // In JES4 server , this is the request which could fail.
        enableAuthLegacyMode();
        try {
            iqBind = (InfoQuery)sendAndWatch(iqBind, getRequestTimeout());
        } catch(StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
            throw new AuthenticationException(se);
        }

        // We passed it , disable legacy mode.
        disableAuthLegacyMode();

        if (null == iqBind) {
            throw new AuthenticationException("No reply from server");
        }
        if (InfoQuery.ERROR == iqBind.getType())
        {
            throw new AuthenticationException(iqBind.getError().getText());
        }

        // Set the jid to what is specified by the server !
        List bqlist = iqBind.listExtensions(BindQuery.NAMESPACE);
        if (null == bqlist || 1 != bqlist.size())
        {
            throw new AuthenticationException(
                    "BindQuery namespace listextentions returned error : " +
                    bqlist);
        }
        bq = (BindQuery)bqlist.get(0);
        // Should we set only the resource ? Or the whole jid itself ?
        client.setJID(bq.getJID());
        //assert (client.getJID().getDomain().equals(_server.getDomain()));
    }

    protected void doAuthQueryLogin(String password) throws CollaborationException {
        
    
        InfoQuery iqAuth;
        AuthQuery auth;

        // Get Auth Details
        try{
            registerChannelWithProvider();
        }catch(IOException ioEx){
            throw new CollaborationException(ioEx);
        }
        
        iqAuth = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);

        iqAuth.setTo(_server);
        iqAuth.setType(InfoQuery.GET);
        iqAuth.setID(nextID("auth"));

        auth = (AuthQuery)_sdf.createExtensionNode(AuthQuery.NAME, AuthQuery.class);

        if (auth == null) {
            XMPPSessionProvider.debug("Auth is null");
            throw new AuthenticationException("Unable to create auth packet");
        }
        if (_client == null) {
            XMPPSessionProvider.debug("_client is null");
            throw new AuthenticationException("User info not present");
        }

        auth.setUsername(_loginName);

        iqAuth.add(auth);
        try {
            iqAuth = (InfoQuery)sendAndWatch(iqAuth, getRequestTimeout());
        } catch(StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
            throw new AuthenticationException(se);
        }
        if (iqAuth == null) {
            throw new AuthenticationException("No reply from server");
        }
        if ((iqAuth.getType() == InfoQuery.ERROR) &&
            (iqAuth.getError().getType() == PacketError.AUTH))
        {
            throw new AuthenticationException(iqAuth.getError().getText());
        }
        /*if ((iqAuth == null) || (iqAuth.getType() != InfoQuery.RESULT)) {
            throw new IllegalStateException("Could not authenticate to server iqAuth=" + iqAuth);
        }*/

        //  Setup authentication
        auth = (AuthQuery)iqAuth.listExtensions(AuthQuery.NAMESPACE).get(0);
        auth.setUsername(_loginName);
        auth.setResource(_client.getJID().getResource());
        /*
        if (AuthQuery.ZERO_KNOWLEDGE.isSupported(auth)) {
            Method  method = AuthQuery.ZERO_KNOWLEDGE;
            Map params = method.setupAuthParams(auth);
            params.put("password", password);
            auth.setPassword(password);
            method.setupAuth(auth, params);
            XMPPSessionProvider.info("Authenticating using " + method);
            */
        if (AuthQuery.DIGEST.isSupported(auth)) {
            Method    method = AuthQuery.DIGEST;
            Map  params = method.setupAuthParams(auth);
            params.put("sessionid", _sessionID);
            params.put("password", password);
            method.setupAuth(auth, params);
            XMPPSessionProvider.info("Authenticating using " + method);
        } else if (AuthQuery.PLAIN.isSupported(auth)) {
            Method    method = AuthQuery.PLAIN;
            Map       params = method.setupAuthParams(auth);
            params.put("password", password);
            method.setupAuth(auth, params);
            XMPPSessionProvider.info("Authenticating using " + method);
        }

        iqAuth.setTo(_server);
        iqAuth.setType(InfoQuery.SET);
        iqAuth.setID(nextID("auth"));
        try {
            iqAuth = (InfoQuery)sendAndWatch(iqAuth, getRequestTimeout());
        } catch(StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
            throw new AuthenticationException(se);
        }
        if (iqAuth == null) {
            throw new AuthenticationException("No reply from server");
        }
        if ((iqAuth.getType() == InfoQuery.ERROR) &&
            (iqAuth.getError().getType() == PacketError.AUTH))
        {
            throw new AuthenticationException(iqAuth.getError().getText());
        }
        /*if ((iqAuth == null) || (iqAuth.getType() != InfoQuery.RESULT)) {
            throw new IllegalStateException("Could not authenticate to server iqAuth=" + iqAuth);
        }*/

        // Is this required ? - Mridul

        // re-intialize the server to the domain-name of the authenticated user
        _server = new JID(_client.getDomainName());
    }

    void start() throws CollaborationException, IOException {

        XMPPSessionProvider.debug("XMPPSession: start()");
        setKeepAliveEnabled(true);
        //_personalStoreSession = new XMPPPersonalStoreSession(this);
        //_personalStoreSession.getProfile();
        // load the contact list i.e get the roster
        //_personalStoreSession.sendRosterRequest();

    }

    protected void registerChannelWithProvider() throws IOException {
        // This will be called after tls negotiation has completed - so no worries.
        // start the reader
        Runnable packetReader = null;
        synchronized(this){
            if (!channelRegistered){
                packetReader = new Runnable() {
                    public void run() {
                        try {
                            _connection.process();
                        } catch (StreamException se) {
                            //se.printStackTrace();
                        }
                    }
                };
                channelRegistered = true;
            }
        }
        if (null != packetReader){
            setupListeners();
            registerImpl(packetReader);
        }
    }
    
    protected void registerImpl(Runnable packetReader) throws IOException{
        if (_streamSrcCreator instanceof SocketStreamSourceCreator){
            SocketStreamSourceCreator creator = (SocketStreamSourceCreator)_streamSrcCreator;
            ByteChannel channel = creator.getBufferedChannel();
            SocketChannel socketChannel = creator.getSocketChannel();
            if (channel instanceof BufferedByteChannel) {
                _selectionKey = _provider.register(socketChannel, packetReader, 
                        (BufferedByteChannel)channel);
            } else {
                _selectionKey = _provider.register(socketChannel, packetReader);
            }
        }
        else{
            // Unhandled case ! subclass should be handling this - so error.
            XMPPSessionProvider.error("Unsupported _streamSrcCreator : " + 
                    _streamSrcCreator);
        }
    }

    public Map getNameSpaces() {
        return _namespaces;
    }

    private void setupListeners() {
        org.jabberstudio.jso.event.PacketListener  watcher;
        org.jabberstudio.jso.event.StreamStatusListener  statusListener;

        /*Map ns = getNameSpaces();

        //Define namespace mappings
        ns.put("app", _connection.getDefaultNamespace());
        ns.put("time", "jabber:iq:time");
        ns.put("ver", "jabber:iq:version");
        ns.put("last", "jabber:iq:last");*/
        //watcher.setupNamespaces(ns);

        if (null != redirectionListener){
            _connection.removePacketListener(PacketEvent.RECEIVED , redirectionListener);
            redirectionListener = null;
        }

        statusListener = new StatusListenerClass();
        watcher = new PacketListenerClass();
        _connection.addPacketListener(PacketEvent.RECEIVED, watcher);
        _connection.addPacketListener(new DebugPacketListenerClass());
        _connection.addStreamStatusListener(statusListener);

}

    public String nextID(String key) {
        String u = (new java.rmi.server.UID()).toString() + _nonce;
        u = StringUtility.substitute(u, ":", ".");
        u = StringUtility.substitute(u, "-", "_");
        return u;
    }

    public InfoQuery createIBBInfoQuery(JID to, String sid, boolean open) {
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iq.setType(InfoQuery.SET);
        iq.setID(nextID("ibb"));
        iq.setTo(to);
        iq.setFrom(_client.getJID());
        Extension extn;
        if (open) {
            extn = _sdf.createExtensionNode(IBB_OPEN);
                    extn.setAttributeValue("block-size",Integer.toString(IBB_MESSAGE_SIZE));
        } else {
            extn = _sdf.createExtensionNode(IBB_CLOSE);
        }
        extn.setAttributeValue("sid",sid);
        iq.add(extn);
        return iq;
    }

    public void sendAllMessageParts(XMPPMessage msg)
                throws CollaborationException
    {
        org.jabberstudio.jso.Message xmppMessage =
            (org.jabberstudio.jso.Message) msg.getXMPPMessage();
        MessagePart parts[] = msg.getParts();
        if (parts.length == 1) {
            try {
                xmppMessage.add(getPropertiesExtension(msg.getHeaders()));
                //Do we need to update the AMPExtensions for all the parts?
                msg.updateAMPExtension();
                _connection.send(xmppMessage);
            } catch(StreamException se) {
                XMPPSessionProvider.error(se.toString(),se);
                throw new CollaborationException(se.toString());
            }
            return;
        }
        //Accumulate all the sid's for the message parts
        String[] sid_list = new String[parts.length -1];
        for(int i = 1; i < parts.length; i++) {
            String sid = nextID("sid");
            InfoQuery iq = createIBBInfoQuery(xmppMessage.getTo(), sid, true);
            try {
                iq = (InfoQuery)sendAndWatch(iq, getRequestTimeout());
            } catch (StreamException se) {
                XMPPSessionProvider.error(se.toString(),se);
            }

            if ((iq == null) || (iq.getType() != InfoQuery.RESULT)) {
                throw new CollaborationException("Failed to open IBB Stream");
            }
            //if there was a success then send all the message parts.
            //first send the actual message(part[0])
            try {
                Extension data = _sdf.createExtensionNode(IBB_DATA);
                data.setAttributeValue("sid",sid);
                org.jabberstudio.jso.Message newMsg;
                int seqId = 0;
                newMsg = (org.jabberstudio.jso.Message)_sdf.createPacketNode(
                                                MESSAGE_NAME, org.jabberstudio.jso.Message.class);
                newMsg.setFrom(xmppMessage.getFrom());
                newMsg.setTo(xmppMessage.getTo());
                newMsg.setID(nextID("message"));
                String contents = parts[i].getContent();
                //add the meta data to the packet
                newMsg.addExtension(getPropertiesExtension(((XMPPMessagePart)parts[i]).getHeaders()));

                int packets = ((int)contents.length()/IBB_MESSAGE_SIZE) + 1;
                //data = _sdf.createExtensionNode(IBB_DATA);
                //data.setAttributeValue("sid",sid);
                newMsg.add(data);
                int end = 0;
                for(int k = 0; k < packets; k++) {
                    data.setAttributeValue("seq",Integer.toString(seqId++));
                    end = ((end + IBB_MESSAGE_SIZE) <= contents.length()) ?
                                        (end + IBB_MESSAGE_SIZE): contents.length();
                    data.clearText();
                    data.addText(contents.substring(k * IBB_MESSAGE_SIZE , end));
                    _connection.send(newMsg);
                }
                sid_list[i - 1] = sid;
                iq = createIBBInfoQuery(xmppMessage.getTo(), sid, false);
                //use send and watch to avoid any threading issues in the server
                iq = (InfoQuery)sendAndWatch(iq, getRequestTimeout());
                //_connection.send(iq);
            } catch (StreamException se) {
                XMPPSessionProvider.error(se.toString(),se);
            }
        }
        StringBuffer buf = new StringBuffer(sid_list[0]);
        for(int n = 1; n < sid_list.length; n++) {
            buf.append("," + sid_list[n]);
        }
        //finally add the sid list and the headers and send the message
        StreamElement attachElem = _sdf.createElementNode(SUN_ATTACH_NAME);
        attachElem.addText(buf.toString());
        xmppMessage.add(attachElem);
        /*Hashtable ht = msg.getHeaders();
        ht.put(XMPPMessage.SID_LIST, buf.toString());
        xmppMessage.add(getPropertiesExtension(ht));*/
        xmppMessage.add(getPropertiesExtension(msg.getHeaders()));
        //send the actual message after all the attachments are sent.
        try {
            _connection.send(xmppMessage);
        } catch (StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
        }
    }

    private Extension getPropertiesExtension(Hashtable contents) {
        if ((contents == null) || (contents.size() == 0)) return null;
        Extension x = _sdf.createExtensionNode(SUN_PRIVATE_NAME);
        for(Enumeration e1 = contents.keys(); e1.hasMoreElements();) {
            String header = (String)e1.nextElement();
            String value = (String)contents.get(header);
            StreamElement propElem = _sdf.createElementNode(_sdf.createNSI("property", null));
            propElem.setAttributeValue("name",header);
            propElem.addElement("value").addText(value);
            x.add(propElem);
        }
        return x;
    }

    public CollaborationPrincipal createPrincipal(String uid)
                                  throws CollaborationException
    {
        String fquid = StringUtility.appendDomainToAddress(uid,_server.getDomain());
        return new XMPPPrincipal(new JID(fquid));
    }


    /** create a principal object based on a fully-qualified user id
     * @param uid FQ user id.
     * @param displayName
     * @return a new Principal object.
     */
    public CollaborationPrincipal createPrincipal(String uid, String displayName)
                                            throws CollaborationException {
        String fquid = StringUtility.appendDomainToAddress(uid,_server.getDomain());
        return new XMPPPrincipal(new JID(fquid),displayName);
    }


    public static String getItemValue(StreamElement node, String name) {
        String value = null;
        Iterator i = node.listElements(name).iterator();
        if (i.hasNext()) {
            value = ((StreamElement)i.next()).normalizeTrimText();
        }
        return value;
    }

    /*private JID getConferenceServer() throws CollaborationException {
        InfoQuery iq;
        DiscoItemsQuery dq;

        dq = sendItemsQuery(_server,null);
        Iterator itr = dq.listItems().iterator();
        while (itr.hasNext()) {
            DiscoItem item = (DiscoItem)itr.next();
            if ((item.getName()).equals("Multi User Conference Service"))
                return item.getJID();
        }
        return null;
    }*/

    private class DiscoRunnable implements Runnable {
        JID _to;
        DiscoRunnable(JID to) { _to = to; }

        public void run() {
            // query items, and then info for each item
            try {
                DiscoItemsQuery dq = sendItemsQuery(_to, null);
                Iterator itr = dq.listItems().iterator();
                XMPPSessionProvider.debug("Loading Jabber services for " + _to);
                while (itr.hasNext()) {
                    DiscoItem item = (DiscoItem)itr.next();
                    DiscoInfoQuery diq = null;
                    try {
                     diq = sendInfoQuery(item.getJID());
                    } catch (CollaborationException ce) {
                       XMPPSessionProvider.debug(null, ce);
                       continue;
                    }

                    for (Iterator iter = diq.listIdentities().iterator();
                         iter.hasNext(); ) {
                        DiscoIdentity discoIdentity = (DiscoIdentity)iter.next();
                        String category = discoIdentity.getCategory();
                        if (PersonalStoreEntry.CONFERENCE.equals(category) &&
                            _conferenceService != null) {
                            XMPPSessionProvider.debug("adding remote conference service " + item.getJID());
                            _conferenceService.addRemoteService(item.getJID());

                        } else if ("pubsub".equals(category) &&
                                   _newsService != null) {
                            _newsService.addRemoteService(item.getJID());

                        } else if ("directory".equals(category) &&
                                   _personalStoreService != null) {
                            _personalStoreService.addRemoteService(item.getJID());

                        }
                    }
                }

                // keep track so we don't query twice
                _remoteServices.add(_to);

            } catch (Exception e) {
            }
        }
    }

    protected void loadJabberServices() throws CollaborationException {
        DiscoItemsQuery dq = sendItemsQuery(_server,null);
        Iterator itr = dq.listItems().iterator();
        PacketMonitor monitor = new PacketMonitor();
        monitor.setRouter(_connection);
        monitor.setDispatcher(_connection);
        monitor.setTimeout(getRequestTimeout());
        while (itr.hasNext()) {
            DiscoInfoQuery diq = null;
            DiscoItem item = (DiscoItem)itr.next();
            if (item.getJID().equals(getCurrentUserJID()))
                continue;  // no need to discover oneself
            if (!_discoveredServices.contains(item.getJID())) {
                InfoQuery iq = createDiscoInfoQuery(item.getJID(), null);
                try {
                    monitor.send(iq, new ServiceDiscovery(this, item));
                } catch(IllegalStateException e) {
                }
            }
        }
    }

    protected DiscoItemsQuery sendItemsQuery(JID server, String node)
            throws CollaborationException
    {
        DiscoItemsQuery dq;
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iq.setType(InfoQuery.GET);
        iq.setFrom(_client.getJID());
        iq.setTo(server);
        iq.setID(nextID("item"));

        dq = (DiscoItemsQuery)_sdf.createElementNode(DiscoItemsQuery.NAME);
        //dq.addIdentity("conference", "text");
        if (node != null) {
            dq.setNode(node);
        }
        iq.add(dq);

        try {
            iq = (InfoQuery)sendAndWatch(iq, getShortRequestTimeout());
        } catch (StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
            throw new CollaborationException("Connection Error");
        }

        if (iq == null) {
            throw new TimeoutException("Request timed out");
        } else if (iq.getType() == InfoQuery.RESULT) {
            return (DiscoItemsQuery)iq.listExtensions(DiscoItemsQuery.NAMESPACE).get(0);
        } else if (iq.getType() == InfoQuery.ERROR) {
            PacketError error = iq.getError();
            if (error != null) {
                String errorCond = error.getDefinedCondition();
                if (errorCond != null && ((errorCond.equals(PacketError.FEATURE_NOT_IMPLEMENTED_CONDITION))
                || (errorCond.equals(PacketError.SERVICE_UNAVAILABLE_CONDITION)))) {
                    throw new ServiceUnavailableException(error.getText());
                }
            }
        }
        throw new CollaborationException("Some error has occurred in getting the discovery information : " + iq.toString());
    }

    private InfoQuery createDiscoInfoQuery(JID server, String node) {
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iq.setType(InfoQuery.GET);
        iq.setFrom(_client.getJID());
        iq.setTo(server);
        iq.setID(nextID("info"));
        DiscoInfoQuery dq = (DiscoInfoQuery)_sdf.createElementNode(DiscoInfoQuery.NAME);
        if (node != null) {
            dq.setNode(node);
        }
        iq.add(dq);
        return iq;
    }

    protected DiscoInfoQuery sendInfoQuery(JID server, String node) throws CollaborationException {
        InfoQuery inputIq = createDiscoInfoQuery(server, node);
        InfoQuery iq = null;
        try {
            iq = (InfoQuery)sendAndWatch(inputIq, getShortRequestTimeout());
        } catch (StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
            throw new IllegalStateException("Could not authenticate to server!");
        }

        if (iq == null) {
             throw new TimeoutException("Request timed out");
        } else if (iq.getType() == InfoQuery.RESULT) {
             return (DiscoInfoQuery)iq.listExtensions(DiscoInfoQuery.NAMESPACE).get(0);
        } else if (iq.getType() == InfoQuery.ERROR) {
             PacketError error = iq.getError();
             if (error != null) {
                 String errorCond = error.getDefinedCondition();
                 if (errorCond != null && ((errorCond.equals(PacketError.FEATURE_NOT_IMPLEMENTED_CONDITION))
                 || (errorCond.equals(PacketError.SERVICE_UNAVAILABLE_CONDITION)))) {
                     throw new ServiceUnavailableException(error.getText());
                 }
             }
        }
        throw new CollaborationException("Some error has occurred in getting the discovery information : " + iq.toString());
    }

    private DiscoInfoQuery sendInfoQuery(JID server) throws CollaborationException {
        return sendInfoQuery(server, null);
    }

    public CollaborationPrincipal getPrincipal() throws CollaborationException {
        return _client;
    }

    protected long getLogoutTimeout(){
        // Hardcoded for now to point to short req timeout.
        return _authenticated ? 10000 : getShortRequestTimeout();
    }
    
    public void logout()  {
        try {
            _provider.cleanupSession(this);
            _logout = true;
            _connection.close(getLogoutTimeout());
        } catch(Exception e) {
            XMPPSessionProvider.error(e.toString(), e);
        } finally {
            _authenticated = false;
            _provider.cancel(_selectionKey);
            try{
                _connection.disconnect();
            } catch(Exception e) {
                XMPPSessionProvider.error(e.toString(), e);
            }
        }
    }

    public void setSessionListener(CollaborationSessionListener listener) {
        _sessionListeners.remove(0);
        _sessionListeners.add(0, listener);
    }

    private org.netbeans.lib.collab.Presence processPresence(org.jabberstudio.jso.Presence p)
                                        throws CollaborationException
    {
        if ((p.getType() != null) && (p.getType().equals(org.jabberstudio.jso.Presence.ERROR)))
        {
            //throw new CollaborationException("Error while receiving presence");
            XMPPSessionProvider.error("Recieved a presence packet of type error");
            return null;
        }
        Iterator itr = p.listElements().iterator();
        while (itr.hasNext()) {
            Object obj = itr.next();
            if (obj instanceof MUCUserQuery) {
                //It is a MUC User query
                //XMPPConference c =
                 //   (XMPPConference)_conferences.get(p.getFrom().toBareJID().toString());
                if (_conferenceService != null) {
                    XMPPConference c = _conferenceService.getConference(p.getFrom().toBareJID().toString());
                    if (c != null) c.userStatusChange(p);
                } else {
                    XMPPSessionProvider.debug("MUC packet received - conference service not initialized");
                }
                return null;
            }
        }
        XMPPSessionProvider.debug("Calling process presence in presence session");
        if (_presenceService != null) {
            return _presenceService.processPresence(p);
        }
        return null;
    }

    /**
     *  accessor  method
     **/
    public Stream getConnection() {
        return _connection;
    }

    public StreamDataFactory getDataFactory() {
        return _sdf;
    }
    
    private XMPPContentStream getContentStream(String sid)
    {
        XMPPContentStream cs = null;
        if (_streamingService != null) {
            cs = _streamingService.getContentStream(sid);
        } 
        return cs;
    }
    
    private void processInfoQuery(Packet packet) {

        //Should handle error packets: vijay
        try {
            if (_streamingService != null &&
                _streamingService.isStreamingPacket(packet))
            {
                _streamingService.processSIPackets((InfoQuery)packet);
                return;
            }
            //look for IBB and if present then create a hashtable for it
            List elements = packet.listElements("open", IBB_NAMESPACE);
            if (elements.size() > 0) {
                StreamElement ibb = (StreamElement)elements.get(0);
                String sid = ibb.getAttributeValue("sid");
                if (sid == null) return;
                XMPPContentStream cs = getContentStream(sid);
                if (cs != null) {
                    cs.process(packet);
                    return;
                }
                _ibbMessages.put(sid, new ArrayList());
                Packet p = _sdf.createPacketNode(packet.getNSI());
                p.setID(packet.getID());
                p.setType(InfoQuery.RESULT);
                p.setFrom(_client.getJID());
                JID from = packet.getFrom();
                if (_conferenceService != null &&
                    _conferenceService.getConference(from.toBareJID().toString()) != null) {
                    p.setTo(from.toBareJID());
                } else {
                    p.setTo(from);
                }
                //p.setTo(packet.getFrom());
                _connection.send(p);
                return;
            }
            elements = packet.listElements("close", IBB_NAMESPACE);
            if (elements.size() > 0) {
                StreamElement ibb = (StreamElement)elements.get(0);
                String sid = ibb.getAttributeValue("sid");
                if (sid == null) return;
                XMPPContentStream cs = getContentStream(sid);
                if (cs != null) {
                    cs.process(packet);
                    return;
                }
                ArrayList list = (ArrayList)_ibbMessages.get(sid);
                if ((list == null) || (list.size() == 0)) return;
                Packet p = _sdf.createPacketNode(packet.getNSI());
                p.setID(packet.getID());
                p.setType(InfoQuery.RESULT);
                p.setFrom(_client.getJID());
                JID from = packet.getFrom();
                if (_conferenceService != null &&
                    _conferenceService.getConference(from.toBareJID().toString()) != null) {
                    p.setTo(from.toBareJID());
                } else {
                    p.setTo(from);
                }
                //p.setTo(packet.getFrom());
                _connection.send(p);
                //processMessage((Packet)list.get(0), true);
                return;
            }

            List rqList = packet.listExtensions(RosterQuery.NAMESPACE);
            if (rqList.size() > 0){
                 if (_personalStoreService != null) {
                    if(packet.getType() == InfoQuery.RESULT) {
                        _personalStoreService.processAsyncRosterQuery((RosterQuery)rqList.get(0));
                    } else if(packet.getType() == InfoQuery.SET) {
                       _personalStoreService.processRosterQuery((RosterQuery)rqList.get(0));
                    }
                }
                return;
            }

            List pqList = packet.listExtensions(PRIVATE_NAMESPACE);
            if (pqList.size() > 0){
                if (_personalStoreService != null) {
                   List sList = ((StreamElement)pqList.get(0)).listElements(STORAGE_NAME);
                   if (sList.size() > 0) {
                       if (packet.getType() == InfoQuery.RESULT) {
                           _personalStoreService.processAsyncBookmarkQuery((StreamElement)sList.get(0));
                       } else if (packet.getType() == InfoQuery.SET) {
                           _personalStoreService.processBookmarkQuery((StreamElement)sList.get(0));
                       }
                   }

                   List gList = ((StreamElement)pqList.get(0)).listElements(SUN_PRIVATE_LDAPGROUP_NAME);
                   if (gList.size() > 0) {
                       if (packet.getType() == InfoQuery.RESULT) {
                           _personalStoreService.processAsyncLDAPGroupQuery((StreamElement)gList.get(0));
                       } else if (packet.getType() == InfoQuery.SET) {
                           _personalStoreService.processLDAPGroupQuery((StreamElement)gList.get(0));
                       }
                   }
                }
                return;
            }

            if (packet.getExtension(DiscoInfoQuery.NAMESPACE) != null) {
                 processDiscoInfoRequest(packet);
                 return;
            } else if (packet.getExtension(DiscoItemsQuery.NAMESPACE) != null) {
                processDiscoItemsRequest(packet);
                return;
            } else if (packet.getExtension(RegisterQuery.NAMESPACE) != null) {
                new Thread(new RegisterNotifier((InfoQuery)packet)).start();
                return;
            } else if (_regisListeners.containsKey(packet.getID())) {
                new Thread(new RegisterNotifier((InfoQuery)packet)).start();
                return;
            } else if(isJinglePacket(packet)){
                processJinglePacket(packet);
            }
            
            XMPPContentStream cs = getContentStream(packet.getID());
            if (cs != null) {
                cs.process(packet);
                return;
            }

            // NOTE: The check for oob extensions should be done
            // only after check for content stream
            List oobList = packet.listExtensions(OutOfBandExtension.IQ_NAMESPACE);
            if (oobList.size() > 0 && 
                packet.getType() == InfoQuery.SET) {
                if (_streamingService != null){
                    //handle oob packets which were not sent using si extensions.
                    _streamingService.processSIPackets((InfoQuery)packet);
                }
                return;
            }

        } catch (StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
        } catch (CollaborationException ce) {
            XMPPSessionProvider.error(ce.toString(),ce);
        }

    }

    protected void processMessage(Packet packet) {
        AMPExtension amp = (AMPExtension)packet.getExtension(AMPExtension.NAMESPACE);
        if (amp != null) {
            try {
                XMPPMessage msg = new XMPPMessage(this,packet); 
                msg.populateMessageProcessingRules((AMPExtension)amp);
                int status = MessageStatus.FAILED;
                if ((!Packet.ERROR.equals(packet.getType())) && msg.processingRulesIterator().hasNext()) {
                    MessageProcessingRule mpr = (MessageProcessingRule)msg.processingRulesIterator().next();
                    MessageProcessingRule.Condition c[] = mpr.getConditions();
                    if (c != null && c.length > 0 && c[0] instanceof MessageProcessingRule.DispositionCondition) {
                        status = ((MessageProcessingRule.DispositionCondition)c[0]).getMessageStatus();
                    }
                    if (MessageProcessingRule.DEFER.equals(mpr.getAction())) {
                        status = MessageStatus.DELAYED;
                    }
                }
                fireMessageProcessingListener(msg, status, getCollaborationException(packet.getError(), null));
            } catch(CollaborationException ce) {
                XMPPSessionProvider.error(ce.toString(),ce);
            }
            return;
        }
        List elements = packet.listElements("data", IBB_NAMESPACE);
        if (elements.size() > 0) {
            StreamElement ibb = (StreamElement)elements.get(0);
            String sid = ibb.getAttributeValue("sid");
            if (sid != null) {
                XMPPContentStream cs = getContentStream(sid);
                if (cs != null) {
                    cs.process(packet);
                    return;
                }
                ArrayList list = (ArrayList)_ibbMessages.get(sid);
                //iib open request was not received by the client
                if (list == null) return;
                list.add(packet);
                _ibbMessages.put(sid, list);
            }
            return;
        }
        org.netbeans.lib.collab.Message message;
        org.jabberstudio.jso.Message in = (org.jabberstudio.jso.Message)packet;
        try {
            XMPPSessionProvider.debug("Incoming message type: " + in.getType());
            if (in.getType() == Packet.ERROR) {
                XMPPSessionProvider.error("Error message received: " + in.toString());
            } else if (org.jabberstudio.jso.Message.GROUPCHAT.equals(in.getType())) {
                if (_conferenceService != null &&
                       _conferenceService.getConference(in.getFrom().toBareJID().toString()) != null) {
                    
                    XMPPSessionProvider.debug("[PacketTransferred] : processing groupchat message");
                    _conferenceService.processGroupChat(in);
                } else {
                    XMPPSessionProvider.warning("[PacketTransferred] : chat message received as conference service is not initialized - packet is ignored.");
                }
            } else if (org.jabberstudio.jso.Message.CHAT.equals(in.getType())) {
                XMPPSessionProvider.debug("[PacketTransferred] : processing chat message");
                if(_conferenceService != null){
                    if(in.getExtension(XMPPConference.MODERATION_NAMESPACE) != null && 
                            _conferenceService.getConference(in.getFrom().toBareJID().toString()) != null){
                        XMPPSessionProvider.debug("[PacketTransferred] : message for moderation");
                        _conferenceService.processGroupChat(in);
                    }
                    else{
                        
                        _conferenceService.processChat(in);
                    }
                }
                else{
                    XMPPSessionProvider.warning("[PacketTransferred] : chat message received as conference service is not initialized - packet is ignored.");                    
                }
            } else if (in.getExtension(MUCUserQuery.NAMESPACE) != null) {
                XMPPSessionProvider.debug("[PacketTransferred] : processing invite message");
                if (_conferenceService != null) {
                    _conferenceService.processInvite(in);
                } else {
                    XMPPSessionProvider.warning("[PacketTransferred] : chat message received as conference service is not initialized - packet is ignored.");
                }

            } else if (in.getExtension(PubSubEvent.NAMESPACE) != null) {
                if (_newsService != null) {
                    _newsService.processNewsMessage(in);
                } else {
                    XMPPSessionProvider.warning("[PacketTransferred] pubsub packet received as news service is not initialized - packet is ignored.");
                }

            } else {
                if (_notificationService != null) {
                    _notificationService.processNormalMessage(in);
                } else {
                    XMPPSessionProvider.warning("[PacketTransferred] normal messsage packet received as notification service is not initialized - packet is ignored.");
                }

            }
        } catch (CollaborationException ce) {
            XMPPSessionProvider.error(ce.toString(),ce);
        }
    }

    protected XMPPMessage assembleMessages(XMPPMessage m)
        throws CollaborationException
    {
        org.jabberstudio.jso.Message in = (org.jabberstudio.jso.Message)m.getXMPPMessage();
        m.setHeaders(getPropertiesFromPacket(in));
        StreamElement attachElem = in.getFirstElement(SUN_ATTACH_NAME);
        if (attachElem == null) {
            return m;
        }
        String sid_list = attachElem.normalizeTrimText();
        StringTokenizer st = new StringTokenizer(sid_list,",");
        while(st.hasMoreTokens()) {
            String sid = st.nextToken();
            ArrayList list = (ArrayList)_ibbMessages.get(sid);
            if ((list == null) || (list.size() == 0)) {
                continue;//there is no data for this sid.
            }
            MessagePart part = new XMPPMessagePart();
            ((XMPPMessagePart)part).setHeaders(getPropertiesFromPacket((Packet)list.get(0)));
            StringBuffer contents = new StringBuffer();
            for(int i = 0; i < list.size(); i++) {
                in = (org.jabberstudio.jso.Message)list.get(i);
                List elements = in.listElements("data", IBB_NAMESPACE);
                if (elements.size() == 0) continue;
                contents.append(((StreamElement)elements.get(0)).normalizeText());
             }
             part.setContent(contents.toString());
             m.addPart(part);
             _ibbMessages.remove(sid);
        }
        return m;
    }

    private Hashtable getPropertiesFromPacket(Packet in) {
        Extension x = in.getExtension(SUN_PRIVATE_NAMESPACE);
        if (x == null) return null;
        Hashtable contents = new Hashtable();
        for (Iterator itr = x.listElements("property").iterator(); itr.hasNext();) {
            StreamElement propElem = (StreamElement)itr.next();
            String name = propElem.getAttributeValue("name");
            StreamElement valueElem = (StreamElement)propElem.getFirstElement("value");
            if (valueElem != null) {
                contents.put(name,valueElem.normalizeTrimText());
            }
        }
        return contents;
    }

    private void processRegisterQuery(InfoQuery iq) throws CollaborationException {
        XMPPRegistrationListenerWrapper regisListenerWrapper =
                                    (XMPPRegistrationListenerWrapper)_regisListeners.get(iq.getID());
        JID server = iq.getFrom();
        String serverStr = _server.toString();
        RegistrationListener regisListener = null;
        if (regisListenerWrapper != null) {
            regisListener = regisListenerWrapper.getRegisListener();
        } else {
            //return;
            throw new CollaborationException("registration response out of sync");
        }
        XMPPRegistrationListenerWrapper.RequestType reqType = regisListenerWrapper.getRequestType();
        if (server != null) {
            serverStr = server.toString();
        }

        /*

        if (iq.getType() == InfoQuery.ERROR) {
            XMPPSessionProvider.debug("Error in registration");
            PacketError pe = iq.getError();
            if (pe != null) {
                if (XMPPRegistrationListenerWrapper.USER_PASSWD_CHANGE.equals(reqType)) {
                    regisListener.registrationUpdateFailed(pe.getDefinedCondition(), pe.getText(), serverStr);
                } else if (XMPPRegistrationListenerWrapper.GATEWAY_UNREGISTRATION.equals(reqType) ||
                           XMPPRegistrationListenerWrapper.USER_UNREGISTRATION.equals(reqType)){
                               regisListener.unregistrationFailed(getRegistrationErrorCondition(pe.getDefinedCondition()),
                                                                  pe.getText(), serverStr);
                } else if (XMPPRegistrationListenerWrapper.GATEWAY_REGISTRATION.equals(reqType) ||
                            XMPPRegistrationListenerWrapper.USER_REGISTRATION.equals(reqType)) {
                    regisListener.registrationFailed(getRegistrationErrorCondition(pe.getDefinedCondition()),
                                                     pe.getText(), serverStr);
                }
            } else {
                XMPPSessionProvider.debug("Packet error is null");
            }
            _regisListeners.remove(iq.getID());
            return;
        }
         */

        RegisterQuery registerQuery = (RegisterQuery)iq.getExtension(RegisterQuery.NAMESPACE);

        if (registerQuery != null && registerQuery.listElements().size() > 0) {
            processRegisterResponse(iq, registerQuery, regisListenerWrapper,
                                    serverStr);
            /*
            if (iq.getType() == InfoQuery.RESULT && registerQuery != null) {
                if (registerQuery.isRegistered()) {

                    if (XMPPRegistrationListenerWrapper.USER_REGISTRATION.equals(reqType)) {
                        regisListener.registered(serverStr);
                        return;
                    }
                    // it is a gateway re-registration
                }
                keyValue = registerQuery.getKey();
                registerQuery.clearKey();
                Set fields = registerQuery.getFieldNames();

                Map fieldValuePairs = new HashMap();
                for (Iterator iter = fields.iterator(); iter.hasNext();) {
                    String fieldName = (String)iter.next();
                    fieldValuePairs.put(fieldName, registerQuery.getField(fieldName)); // put the value also if any
                }
                boolean filled = regisListener.fillRegistrationInformation(fieldValuePairs,
                serverStr);
                if (!filled) {
                    // do not do anything
                    return;
                } else {
                    String userName = (String)fieldValuePairs.get(RegistrationListener.USERNAME);
                    String password = (String)fieldValuePairs.get(RegistrationListener.PASSWORD);
                    if (userName == null || userName.equals("") || password == null
                    || password.equals("")) {
                        regisListener.registrationFailed(RegistrationListener.MISSING_DATA, "Empty username or password is not allowed",
                        serverStr);
                        logout();
                        return;
                    }
                }

                if (iq.getFrom() != null) {
                    sendRegistration(iq.getFrom(), fieldValuePairs, iq.getID());
                } else {
                    sendRegistration(_server, fieldValuePairs, iq.getID());
                }
            }
             */
        } else {
                    processRegisSuccessFailure(iq, regisListenerWrapper, serverStr);

                    /*
                    if (iq.getType() == InfoQuery.RESULT) {

                     */

                     /*
                        JID sender = packet.getFrom();
                        String senderServer = _server.toString();
                        if (sender != null) {
                            senderServer = sender.toString();
                        }
                        if (!senderServer.equalsIgnoreCase(_server.toString())) { */

                    /*
                        if (XMPPRegistrationListenerWrapper.GATEWAY_REGISTRATION.equals(reqType)) {
                            postGatewayRegistration(serverStr);
                            regisListener.registered(serverStr);
                        } else if (XMPPRegistrationListenerWrapper.USER_REGISTRATION.equals(reqType)) {
                            regisListener.registered(serverStr);
                            logout();
                        } else if (XMPPRegistrationListenerWrapper.USER_PASSWD_CHANGE.equals(reqType)) {
                            regisListener.registrationUpdated(serverStr);
                        } else if (XMPPRegistrationListenerWrapper.GATEWAY_UNREGISTRATION.equals(reqType)) {
                            postGatewayUnregistration(serverStr);
                            regisListener.unregistered(serverStr);
                        } else if (XMPPRegistrationListenerWrapper.USER_UNREGISTRATION.equals(reqType)) {
                            regisListener.unregistered(serverStr);
                        }
                        _regisListeners.remove(packet.getID());

                    } else if (iq.getType() == InfoQuery.ERROR) {
                        XMPPSessionProvider.debug("Error in registration");
                        PacketError pe = packet.getError();

                        if (pe != null) {
                            if (XMPPRegistrationListenerWrapper.USER_PASSWD_CHANGE.equals(reqType)) {
                                regisListener.registrationUpdateFailed(pe.getDefinedCondition(), pe.getText(), serverStr);
                            } else if (XMPPRegistrationListenerWrapper.GATEWAY_UNREGISTRATION.equals(reqType) ||
                                        XMPPRegistrationListenerWrapper.USER_UNREGISTRATION.equals(reqType)){
                                regisListener.unregistrationFailed(getRegistrationErrorCondition(pe.getDefinedCondition()),
                                pe.getText(), serverStr);
                            } else if (XMPPRegistrationListenerWrapper.GATEWAY_REGISTRATION.equals(reqType) ||
                                        XMPPRegistrationListenerWrapper.USER_REGISTRATION.equals(reqType)) {
                                regisListener.registrationFailed(getRegistrationErrorCondition(pe.getDefinedCondition()),
                                pe.getText(), serverStr);
                            }
                        } else {
                            XMPPSessionProvider.debug("Packet error is null");
                        }
                        _regisListeners.remove(iq.getID());
                        if (XMPPRegistrationListenerWrapper.USER_REGISTRATION.equals(reqType)) {
                            logout();
                        }
                        return;
                    }
                     */
                }

    }

    private String getRegistrationErrorCondition(String condition) {
        if (PacketError.CONFLICT_CONDITION.equals(condition)) {
            return RegistrationListener.ALREADY_REGISTERED;
        } else if ((PacketError.NOT_AUTHORIZED_CONDITION.equals(condition)) ||
                   (PacketError.FORBIDDEN_CONDITION.equals(condition)))
        {
            return RegistrationListener.NOT_AUTHORIZED;
        } else if ((PacketError.NOT_ACCEPTABLE_CONDITION.equals(condition)) ||
                   (PacketError.BAD_REQUEST_CONDITION.equals(condition)))
        {
           return RegistrationListener.MISSING_DATA;
        } else if (PacketError.REGISTRATION_REQUIRED_CONDITION.equals(condition)) {
           return RegistrationListener.NOT_REGISTERED;
        } else if ((PacketError.FEATURE_NOT_IMPLEMENTED_CONDITION.equals(condition)) ||
                   (PacketError.UNEXPECTED_REQUEST_CONDITION.equals(condition)) ||
                   (PacketError.NOT_ALLOWED_CONDITION.equals(condition)))
        {
           return RegistrationListener.SERVICE_UNAVAILABLE;
        }
        return RegistrationListener.UNKNOWN_ERROR_CONDITION;
    }

    public static String access2Affiliation(int access) {
        String affiliation = null;
        if (access <= 0) {
            affiliation = "none";
        } else if (access < Conference.LISTEN) {
            affiliation = "outcast";
        } else if (access < Conference.PUBLISH) {
            affiliation = "none";
        } else if (access < Conference.MANAGE) {
            affiliation = "publisher";
        } else {
            affiliation = "owner";
        }
        return affiliation;
    }

    public static int affiliation2Access(String affiliation) {
        int access = 0;
        int defaultAccess = Conference.NONE;

        if (affiliation == null) {
            access = defaultAccess;
        } else if (affiliation.equals("outcast")) {
            access = Conference.NONE;
        } else if (affiliation.equals("publisher")) {
            access = Conference.PUBLISH | Conference.LISTEN | Conference.INVITE;
        } else if (affiliation.equals("owner")) {
            access = Conference.PUBLISH | Conference.LISTEN | Conference.INVITE | Conference.MANAGE;
        } else if (affiliation.equals("none")) {
            // assume read as the access
            access = Conference.LISTEN;
            /*
             if (getPublishModel().equals("open")) {
             access = AccessControlItem.WRITE;
             } else {
             access = AccessControlItem.READ;
             }
             */
        }
        return access;
    }

    public void addConference(XMPPConference c) {
        _conferenceService.addConference(c);
    }

    public void removeConference(String str) {
        _conferenceService.removeConference(str);
    }
    
    public void addRemoteServices(Set set){
        if(!set.isEmpty())
            this._remoteServices.addAll(set);
    }
    
    public void register(String serviceURL, XMPPRegistrationListenerWrapper listener) throws CollaborationException {
        JID dest = null;
        
        if (null != serviceURL){
            int indx = serviceURL.indexOf(':');
            if (-1 != indx){
                serviceURL = serviceURL.substring(0 , indx);
            }
            dest = new JID(serviceURL);
        }
        if (null == dest && null != _client && null != _client.getJID()){
            dest = new JID("" , _client.getJID().getDomain() , "");
        }
        getRegistrationFields(dest , listener);
    }

    public void changePassword(String password, RegistrationListener listener) throws CollaborationException {
        XMPPRegistrationListenerWrapper regisListenerWrapper = new XMPPRegistrationListenerWrapper(listener);
        regisListenerWrapper.setRequestType(XMPPRegistrationListenerWrapper.USER_PASSWD_CHANGE);
        InfoQuery iqQuery = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iqQuery.setTo(_server);
        iqQuery.setType(InfoQuery.SET);
        iqQuery.setID(nextID("register"));
        XMPPRegistrationListenerWrapper regisListener = new XMPPRegistrationListenerWrapper(listener);
        _regisListeners.put(iqQuery.getID(), regisListenerWrapper);
        RegisterQuery rQuery = (RegisterQuery)_sdf.createExtensionNode(RegisterQuery.NAME, RegisterQuery.class);
        rQuery.setUsername(getPrincipal().getName());
        rQuery.setPassword(password);
        iqQuery.addExtension(rQuery);

        try {
            _connection.send(iqQuery);
        } catch(StreamException se) {
            throw new CollaborationException(se.getMessage());
        }
    }


    void getRegistrationFields(JID recipient, XMPPRegistrationListenerWrapper listener) throws CollaborationException
    {
        InfoQuery iqQuery = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);

        iqQuery.setTo(recipient);
        iqQuery.setType(InfoQuery.GET);
        iqQuery.setID(nextID("register"));
        _regisListeners.put(iqQuery.getID(), listener);

        RegisterQuery rQuery = (RegisterQuery)_sdf.createExtensionNode(_sdf.createNSI("query","jabber:iq:register"), RegisterQuery.class);
        if (rQuery == null) {
        }
        iqQuery.addExtension(rQuery);

        try {
        _connection.send(iqQuery);
        } catch(StreamException se) {
            throw new CollaborationException(se.getMessage());
        }

    }


    void sendRegistration(JID jid, Map values, String ID) throws CollaborationException {

        XMPPRegistrationListenerWrapper regisListener = (XMPPRegistrationListenerWrapper)_regisListeners.get(ID);
        InfoQuery iqQuery = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iqQuery.setTo(jid);
        iqQuery.setType(InfoQuery.SET);
        iqQuery.setID(nextID("register"));
        _regisListeners.remove(ID);
        _regisListeners.put(iqQuery.getID(), regisListener);
        RegisterQuery rQuery = (RegisterQuery)_sdf.createExtensionNode(RegisterQuery.NAME, RegisterQuery.class);

        if (keyValue != null) {
            values.put("key",keyValue);
        }

        for (Iterator i = values.keySet().iterator();i.hasNext();) {
            String fieldName = (String)i.next();
            rQuery.setField(fieldName, (String)values.get(fieldName));
        }
        iqQuery.addExtension(rQuery);

        try {
            // _connection.send(iqQuery);
            // sendAndWatch is required as the subscribe after the registration is
            // reaching before registration is completed.
            /*
            InfoQuery iq = (InfoQuery)ReqRespMEP.sendAndWatch(this, iqQuery);
            if ((iq == null) || (iq.getType() != InfoQuery.RESULT)) {
                throw new CollaborationException("Could not register to the server");
            }
             */
            _connection.send(iqQuery);

        } catch(StreamException se) {
            throw new CollaborationException(se.getMessage());
        } finally {
            keyValue = null;
        }

    }

    public void unregister(RegistrationListener listener) throws CollaborationException {
        XMPPRegistrationListenerWrapper regisListenerWrapper = new XMPPRegistrationListenerWrapper(listener);
        regisListenerWrapper.setRequestType(XMPPRegistrationListenerWrapper.USER_UNREGISTRATION);
        unregister(_server, regisListenerWrapper);
    }

    void unregister(JID recipient, XMPPRegistrationListenerWrapper regisListener) throws CollaborationException {
        InfoQuery iqQuery = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);

        iqQuery.setTo(recipient);
        iqQuery.setType(InfoQuery.SET);
        iqQuery.setID(nextID("unregister"));
        _regisListeners.put(iqQuery.getID(), regisListener);

        RegisterQuery rQuery = (RegisterQuery)_sdf.createExtensionNode(RegisterQuery.NAME,
                                                                RegisterQuery.class);
        rQuery.setRemove(true);
        iqQuery.addExtension(rQuery);

        try {
            _connection.send(iqQuery);
        } catch(StreamException se) {
            throw new CollaborationException(se.getMessage());
        }
        // TODO delete all the legacy accounts of the corresponding legacy Service.
    }

    protected XMPPConference getConference(String id) {
        return _conferenceService.getConference(id);
    }

    private void postGatewayRegistration(String senderServer) throws CollaborationException {
        _presenceService.subscribe(senderServer);
        // Not sure how to get rid of this extra search
        // One approach could be to save the PersonalStoreGateway entry in the
        // RegistrationListener
        /*
        PersonalStoreEntry[] gwlist =
        _personalStoreSession.search(PersonalStoreSession.SEARCHTYPE_CONTAINS,
                                      "*", PersonalStoreEntry.GATEWAY);
        PersonalStoreEntry gw = null;
        for (int i = 0; i < gwlist.length; i++) {
            if (gwlist[i].getEntryId().equals(senderServer)) {
                gw = gwlist[i];
                break;
            }
        }
         */

        PersonalGateway gw = _personalStoreService.getGatewayEntry(senderServer);

        PersonalStoreFolder folder =
        (PersonalStoreFolder)_personalStoreService.getEntry(
        PersonalStoreEntry.CONTACT_FOLDER,
        XMPPPersonalGateway.GATEWAY_FOLDER);
        if (folder == null) {
            folder = (PersonalStoreFolder)_personalStoreService.createEntry(
            PersonalStoreEntry.CONTACT_FOLDER,
            XMPPPersonalGateway.GATEWAY_FOLDER);
            folder.save();
        }
        if (gw != null) {
            gw.addToFolder(folder);
            gw.save();
        }

    }

    private void postGatewayUnregistration(String gatewayJID) throws CollaborationException {
        // remove all the legacy users from the roster
        for (Iterator i = _personalStoreService.getEntries(PersonalStoreEntry.CONTACT).iterator();
        i.hasNext();) {
            PersonalContact pc = (PersonalContact)i.next();
            String domain = pc.getPrincipal().getDomainName();
            if (domain != null && domain.equalsIgnoreCase(gatewayJID)) {
                pc.remove();
            }
        }
    }

    /**
     * @return PrivacyList
     *
     */
    public org.netbeans.lib.collab.PrivacyList getPrivacyList(String name)
                                          throws CollaborationException
    {
        PrivacyQuery pq = getPrivacyQueryResult(name);
        if (pq == null) {
            return null;
        }
        org.jabberstudio.jso.x.core.PrivacyList xmppPL = pq.getPrivacyList(name);

        if (xmppPL == null) {
            return null;
        }
        org.netbeans.lib.collab.PrivacyList list = new XMPPPrivacyList(name);
        Iterator itr = xmppPL.listElements().iterator();
        while (itr.hasNext()) {
            addPrivacyItemToList(list, (org.jabberstudio.jso.x.core.PrivacyItem)itr.next());
        }
        return list;
    }

    private void addPrivacyItemToList(org.netbeans.lib.collab.PrivacyList list,
                                    org.jabberstudio.jso.x.core.PrivacyItem xmppItem)
                                    throws CollaborationException
    {
        org.netbeans.lib.collab.PrivacyItem item = new XMPPPrivacyItem();
        if (xmppItem.getAction().equals(org.jabberstudio.jso.x.core.PrivacyItem.ALLOW)) {
            item.setAccess(org.netbeans.lib.collab.PrivacyItem.ALLOW);
        } else {
            item.setAccess(org.netbeans.lib.collab.PrivacyItem.DENY);
        }
        String subject = null;
        if (xmppItem.getType() == org.jabberstudio.jso.x.core.PrivacyItem.SUBSCRIPTION) {
            item.setType(org.netbeans.lib.collab.PrivacyItem.TYPE_SUBSCRIPTION);
            subject = xmppItem.getValue();
        } else if (xmppItem.getType() == org.jabberstudio.jso.x.core.PrivacyItem.GROUP) {
            item.setType(org.netbeans.lib.collab.PrivacyItem.TYPE_GROUP);
            subject = xmppItem.getValue();
        } else if (xmppItem.getType() == org.jabberstudio.jso.x.core.PrivacyItem.JID) {
            item.setType(org.netbeans.lib.collab.PrivacyItem.TYPE_IDENTITIES);
            subject = JIDUtil.decodedJID(xmppItem.getValue());
        }

        item.setSubject(subject);
        boolean resourcePresent = false;
        if (xmppItem.isAppliedToPresenceIn()) {
            resourcePresent = true;
            item.setResource(org.netbeans.lib.collab.PrivacyItem.PRESENCE_IN);
            list.addPrivacyItem(item);
        }
        if (xmppItem.isAppliedToPresenceOut()) {
            resourcePresent = true;
            item.setResource(org.netbeans.lib.collab.PrivacyItem.PRESENCE_OUT);
            list.addPrivacyItem(item);
        }
        if (xmppItem.isAppliedToMessage()) {
            resourcePresent = true;
            item.setResource(org.netbeans.lib.collab.PrivacyItem.MESSAGE);
            list.addPrivacyItem(item);
        }
        if (xmppItem.isAppliedToIQ()) {
            resourcePresent = true;
            item.setResource(org.netbeans.lib.collab.PrivacyItem.IQ);
            list.addPrivacyItem(item);
        }
        if (!resourcePresent) {
            list.addPrivacyItem(item);
        }
    }

    /**
     * @param lst sets this users privacy list
     *
     */
    public void addPrivacyList(org.netbeans.lib.collab.PrivacyList lst) throws CollaborationException {
        //PrivacyListNode list = new PrivacyListNode(_sdf);
        org.jabberstudio.jso.x.core.PrivacyList list =
                    (org.jabberstudio.jso.x.core.PrivacyList)_sdf.createElementNode(
                                               org.jabberstudio.jso.x.core.PrivacyList.NAME,
                                               org.jabberstudio.jso.x.core.PrivacyList.class);
        ((net.outer_planes.jso.x.core.PrivacyListNode)list).setName(lst.getName());

        int order = 0;
        Collection c = lst.getPrivacyItems();
        for(Iterator itr = c.iterator(); itr.hasNext();) {
            org.netbeans.lib.collab.PrivacyItem pi =
                (org.netbeans.lib.collab.PrivacyItem) itr.next();
            org.jabberstudio.jso.x.core.PrivacyItem item = null;
            if (pi.getAccess() == org.netbeans.lib.collab.PrivacyItem.ALLOW) {
                item = list.addItem(org.jabberstudio.jso.x.core.PrivacyItem.ALLOW, order);
            } else if (pi.getAccess() == org.netbeans.lib.collab.PrivacyItem.DENY) {
                item = list.addItem(org.jabberstudio.jso.x.core.PrivacyItem.DENY, order);
            }

            String subject = null;
            if (org.netbeans.lib.collab.PrivacyItem.TYPE_SUBSCRIPTION.equals(pi.getType())) {
                item.setType(org.jabberstudio.jso.x.core.PrivacyItem.SUBSCRIPTION);
                subject = pi.getSubject();
            } else if (org.netbeans.lib.collab.PrivacyItem.TYPE_GROUP.equals(pi.getType())) {
                item.setType(org.jabberstudio.jso.x.core.PrivacyItem.GROUP);
                subject = pi.getSubject();
            } else if (org.netbeans.lib.collab.PrivacyItem.TYPE_IDENTITIES.equals(pi.getType())) {
                item.setType(org.jabberstudio.jso.x.core.PrivacyItem.JID);
                //should encode and send the value
                subject = JIDUtil.encodedJID(pi.getSubject()).toString();
            }
            item.setValue(subject);
            setItemResource(pi,item);
            order++;
        }
        setPrivacyQuery(list,null,null);
    }

    public List listPrivacyLists() throws CollaborationException {

        //XMPPSessionProvider.debug(com.iplanet.im.client.manager.Manager.getStackTrace());

        PrivacyQuery pq = getPrivacyQueryResult(null);

        //mark it as privacy lists have been listed once
        privacyListsListed = true;

        // set the default and active lists
        _activePrivacyList = pq.getActive();
        _defaultPrivacyList = pq.getDefault();
        List l = pq.listPrivacyLists();
        List ret = new ArrayList();
        for(Iterator i = l.iterator(); i.hasNext();) {
            org.jabberstudio.jso.x.core.PrivacyList pl =
                                (org.jabberstudio.jso.x.core.PrivacyList) i.next();
            String name = pl.getName();
            // make the local cache consistent using the list of privacylists
            ret.add(name);
        }
        return ret;
    }

    public void removePrivacyList(String name) throws CollaborationException {
        if (name.equals(_defaultPrivacyList)) {
            _defaultPrivacyList = null;
        }
        if (name.equals(_activePrivacyList)) {
            _activePrivacyList = null;
        }
        net.outer_planes.jso.x.core.PrivacyListNode list =
            new net.outer_planes.jso.x.core.PrivacyListNode(_sdf);
        list.setName(name);
        setPrivacyQuery(list,null,null);
    }

    protected PrivacyQuery getPrivacyQueryResult(String listName)
                         throws CollaborationException
    {
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iq.setType(InfoQuery.GET);
        iq.setID(nextID("privacy"));

        org.jabberstudio.jso.x.core.PrivacyQuery node =
                        (org.jabberstudio.jso.x.core.PrivacyQuery)_sdf.createExtensionNode(
                                    org.jabberstudio.jso.x.core.PrivacyQuery.NAME,
                                    org.jabberstudio.jso.x.core.PrivacyQuery.class);

        if (listName != null) {
            node.addPrivacyList(listName);
        }

        iq.add(node);
        try {
            iq = (InfoQuery)sendAndWatch(iq, getRequestTimeout());
        } catch(StreamException se) {
            //se.printStackTrace();
        }
        if (iq == null) {
            throw new TimeoutException("Timeout while getting the privacy list from server!");
        } else if (iq.getType() == InfoQuery.RESULT) {
            return (PrivacyQuery)iq.listExtensions(PrivacyQuery.NAMESPACE).get(0);
        } else if (iq.getType() == InfoQuery.ERROR) {
            PacketError error = iq.getError();
            if ((error != null) &&
                PacketError.ITEM_NOT_FOUND_CONDITION.equals(error.getDefinedCondition()))
            {
                return null;
            }
        }
        throw new CollaborationException("Could not get the privacy list from server!");
    }


    private void setPrivacyQuery(org.jabberstudio.jso.x.core.PrivacyList list,
                                 String active,
                                 String def)
        throws CollaborationException
    {
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        org.jabberstudio.jso.x.core.PrivacyQuery node =
            (org.jabberstudio.jso.x.core.PrivacyQuery)_sdf.createExtensionNode(
                      org.jabberstudio.jso.x.core.PrivacyQuery.NAME,
                      org.jabberstudio.jso.x.core.PrivacyQuery.class);

        if (list != null) {
            node.add(list);
            node.addPrivacyList(list.getName());
        }
        sendPrivacyQuery(iq,node);
    }

    private void sendPrivacyQuery(InfoQuery iq, PrivacyQuery node)
        throws CollaborationException
    {
        iq.addExtension(node);
        iq.setType(InfoQuery.SET);
        iq.setFrom(_client.getJID());
        iq.setID(nextID("privacy"));
        try {
            InfoQuery response =
                (InfoQuery)sendAndWatch(iq,
                                                      getShortRequestTimeout());
            if (null == response) {
                throw new TimeoutException("No reply from server");
            } else if (InfoQuery.ERROR.equals(response.getType())) {
                throw new CollaborationException(response.getError().getText());
            }
        } catch(StreamException se) {
            throw new CollaborationException(se.getMessage());
        }
    }

    public org.netbeans.lib.collab.PrivacyList createPrivacyList(String name) throws CollaborationException {
        return new XMPPPrivacyList(name);
    }

    /**
     * gets this users default privacy list
     * @return Name of the Privacy List
     *
     */
    public String getDefaultPrivacyListName() throws CollaborationException {
        // return it from the cache if there is one
        // this might cause cache consistency issues
        // in case of multiple resources/sessions
        if (_defaultPrivacyList == null) {
            _defaultPrivacyList = getPrivacyQueryResult(null).getDefault();
        }
        return _defaultPrivacyList;
    }

    /**
     * sets this users default privacy list
     * @param name Name of the Privacy List
     *
     */
    public void setDefaultPrivacyListName(String name) throws CollaborationException {
        _defaultPrivacyList = name;
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        org.jabberstudio.jso.x.core.PrivacyQuery node =
                    (org.jabberstudio.jso.x.core.PrivacyQuery)_sdf.createExtensionNode(
                            org.jabberstudio.jso.x.core.PrivacyQuery.NAME,
                            org.jabberstudio.jso.x.core.PrivacyQuery.class);
        if (name == null) {
            node.addElement("default");
        } else {
            node.setDefault(name);
        }
        //node.setDefault(_defaultPrivacyList);
        sendPrivacyQuery(iq,node);
    }

    /**
     * gets this users active privacy list
     * @return name Name of the PrivacyList
     *
     */

    public String getActivePrivacyListName() throws CollaborationException {
        // return it from the cache if there is one
        // this might cause cache consistency issues
        // in case of multiple resources/sessions
        if (_activePrivacyList == null && !privacyListsListed) {
            _activePrivacyList = getPrivacyQueryResult(null).getActive();
        }
        return _activePrivacyList;
    }

    /**
     * sets this users active privacy list
     * @param name Name of the PrivacyList
     *
     */
    public void setActivePrivacyListName(String name) throws CollaborationException {
        _activePrivacyList = name;
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        org.jabberstudio.jso.x.core.PrivacyQuery node =
                    (org.jabberstudio.jso.x.core.PrivacyQuery)_sdf.createExtensionNode(
                            org.jabberstudio.jso.x.core.PrivacyQuery.NAME,
                            org.jabberstudio.jso.x.core.PrivacyQuery.class);
        //node.setActive(name);
        if (name == null) {
            node.addElement("active");
        } else {
            node.setActive(name);
        }
        sendPrivacyQuery(iq,node);
    }

    /*protected void processPrivacyQuery(PrivacyQuery query) {
        List l = query.listPrivacyLists();
        for(Iterator i = l.iterator(); i.hasNext();) {
            org.jabberstudio.jso.x.core.PrivacyList list =
            (org.jabberstudio.jso.x.core.PrivacyList)i.next();
            List itemList = list.listItems();
            for(Iterator j = itemList.iterator(); j.hasNext();) {
                org.jabberstudio.jso.x.core.PrivacyItem item =
                (org.jabberstudio.jso.x.core.PrivacyItem)j.next();
            }
        }
    }*/

    private void setItemResource(org.netbeans.lib.collab.PrivacyItem pi,
                                 org.jabberstudio.jso.x.core.PrivacyItem item) {
        switch(pi.getResource()) {
            case org.netbeans.lib.collab.PrivacyItem.PRESENCE_IN:
                        /*if (pi[i].getAccess() == org.netbeans.lib.collab.PrivacyItem.ALLOW)
                         */
                item.setAppliedToPresenceIn(true);
                            /*
                        else
                            item.setAppliedToPresenceIn(false);
                             */
                break;
            case org.netbeans.lib.collab.PrivacyItem.PRESENCE_OUT:
                //if (pi[i].getAccess() == org.netbeans.lib.collab.PrivacyItem.ALLOW)
                item.setAppliedToPresenceOut(true);
                            /*
                        else
                            item.setAppliedToPresenceOut(false);
                             */
                break;
            case org.netbeans.lib.collab.PrivacyItem.MESSAGE:
                //if (pi[i].getAccess() == org.netbeans.lib.collab.PrivacyItem.ALLOW)
                item.setAppliedToMessage(true);
                            /*
                        else
                            item.setAppliedToMessage(false);
                             */
                break;
        }
    }

    /*private org.netbeans.lib.collab.PrivacyItem getPrivacyItem(StringBuffer id,
                                                          Hashtable ht,
                                                          org.jabberstudio.jso.x.core.PrivacyItem xmppItem)
    {
        org.netbeans.lib.collab.PrivacyItem item =
                (org.netbeans.lib.collab.PrivacyItem)ht.get(id.toString());
        if (item == null) {
            int access;
            if (xmppItem.getAction().equals(org.jabberstudio.jso.x.core.PrivacyItem.ALLOW)) {
                access = org.netbeans.lib.collab.PrivacyItem.ALLOW;
            } else {
                access = org.netbeans.lib.collab.PrivacyItem.DENY;
            }
            String type = null;
            if (xmppItem.getType() == org.jabberstudio.jso.x.core.PrivacyItem.SUBSCRIPTION) {
                type = org.netbeans.lib.collab.PrivacyItem.TYPE_SUBSCRIPTION;
            } else if (xmppItem.getType() == org.jabberstudio.jso.x.core.PrivacyItem.GROUP) {
                type = org.netbeans.lib.collab.PrivacyItem.TYPE_GROUP;
            } else if (xmppItem.getType() == org.jabberstudio.jso.x.core.PrivacyItem.JID) {
                type = org.netbeans.lib.collab.PrivacyItem.TYPE_IDENTITIES;
            }
            item = new org.netbeans.lib.collab.PrivacyItem(type, access);

            ht.put(id.toString(), item);
        }
        return item;
    }*/

    synchronized void addSendAndWatchID(String id) {
        if (null == id){
            return ;
        }
        _sendAndWatchIDs.put(id , id);
        if (null != inboundSendAndWatch.get(id)){
            // This should not happen - debug statement.
            // Our id generation gaurentee's uniqueness for a box ...
            xmpplogger.debug("addSendAndWatchID for id : " + id +
                    " which is already in c !");
            inboundSendAndWatch.remove(id);
        }
    }
    
    synchronized void forceRemoveSendAndWatch(String id){
       _sendAndWatchIDs.remove(id);
       inboundSendAndWatch.remove(id);
    }
    
    synchronized boolean isSendAndWatchIDRecievedOnce(String id){
        boolean retval = null == id ||
                // not in sendAndWatch list !
                null == _sendAndWatchIDs.get(id) ||
                // Has it been recieved at least once ?
                null != inboundSendAndWatch.get(id)
                ;
        
        return retval;
    }

    synchronized boolean removeSendAndWatchID(final org.jabberstudio.jso.Packet packet) {
        String id = packet.getID();
        
        if (null == id){
            return false;
        }

        //  This is handleed below.
        /*
        if (packet instanceof InfoQuery){
            InfoQuery iq = (InfoQuery)packet;
            if (iq.getType() == InfoQuery.GET ||
                    iq.getType() == InfoQuery.SET){
                // Should be a inbound request ... ignore.
                
                if (null != inboundSendAndWatch.get(id)){
                    // interesting ! bug ?!
                    xmpplogger.debug("removeSendAndWatchID for id : " + id +
                            " coming in multiple times ! packet : " + packet);
                }
                inboundSendAndWatch.put(id , id);
                return false;
            }
        }
         */
        
        if (null == packet.getFrom() ||
                null == _client ||
                null == _client.getJID() ||
                !packet.getFrom().toBareJID().equals(_client.getJID().toBareJID()) ||
                // We have already had this dispatched to us once !
                null != inboundSendAndWatch.get(id)){
            inboundSendAndWatch.remove(id);
            return null != _sendAndWatchIDs.remove(id);
        }
        
        if (null != _sendAndWatchIDs.get(id)){
            // This is a packet which is getting dispatched back to us.
            // We sent this packet , and we are the reciepent of this
            // packet while we are doing a sendAndWatch over it - so expecting a 
            // response back from ourself - ya , I know this sounds backward !
            // So , we just 'mark' this id as already processed.
            inboundSendAndWatch.put(id , id);
        }
        return false;
    }

    void cancelSubscription(String uid) throws CollaborationException {
        if (_presenceService != null) {
            _presenceService.cancel(uid);
        }
    }

  
    
    public void processJinglePacket(Packet packet) throws CollaborationException{
        _p2pservice.processPacket(packet);
    }
    public long getRequestTimeout() {
        return requestTimeout;
    }

    protected void setRequestTimeout(long REQUEST_TIMEOUT) {
        this.requestTimeout = REQUEST_TIMEOUT;
    }


     public NotificationService getNotificationService()
                                                throws CollaborationException {
         if(_notificationService == null) {
             _notificationService = new XMPPNotificationService(this);
         }
         return _notificationService;
     }

      public synchronized ConferenceService getConferenceService()
                                                throws CollaborationException {
          if(_conferenceService == null) {
              _conferenceService = new XMPPConferenceService(this);
          }
          return _conferenceService;
      }
      
      public ExtendedConferenceService getExtendedConferenceService() throws CollaborationException{
             return (ExtendedConferenceService)getConferenceService();
      }
      
      
      public NewsService getNewsService() throws CollaborationException {
          if(_newsService == null) {
              _newsService = new XMPPNewsService(this);
          }

          return _newsService;
      }

      public PersonalStoreService getPersonalStoreService()
                                                throws CollaborationException {
          if(_personalStoreService == null) {
              _personalStoreService = new XMPPPersonalStoreService(this);
          }

          return _personalStoreService;
      }


    public PresenceService getPresenceService() throws CollaborationException {
        if(_presenceService == null) {
            _presenceService = new XMPPPresenceService(this);
        }
        return _presenceService;
    }

    public StreamingService getStreamingService() throws CollaborationException {
        if(_streamingService == null) {
            _streamingService = new XMPPStreamingService(this);
        }
        return _streamingService;
    }

      public Collection listNewsChannels(String domain) throws CollaborationException {
         return _newsService.listNewsChannels();
      }

      protected void addNewsChannel(XMPPNewsChannel nc) {
          if (_newsService != null) {
             _newsService.addNewsChannel(nc);
           }
      }

      protected void removeNewsChannel(XMPPNewsChannel nc) {
          if (_newsService != null) {
             _newsService.removeNewsChannel(nc);
           }
      }

    public boolean isCurrentUser(JID jid) {
        if (jid == null) return false;
        return _client.getJID().toBareJID().equals(jid.toBareJID());
    }

    public JID getCurrentUserJID() {
        return (_client == null) ? null : _client.getJID();
    }

    ApplicationInfo getApplicationInfo() throws CollaborationException {
        return _provider.getApplicationInfo();
    }
    
    private void processDiscoInfoRequest(Packet packet) throws CollaborationException {

        DiscoInfoQuery query = (DiscoInfoQuery)packet.getExtension(DiscoInfoQuery.NAMESPACE);

        if (InfoQuery.RESULT.equals(packet.getType()) ||
            InfoQuery.ERROR.equals(packet.getType()) ||
            InfoQuery.SET.equals(packet.getType())) {
            // for now don't process the disco#result packets
            return;
        }
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iq.setTo(packet.getFrom());
        iq.setType(InfoQuery.RESULT);
        iq.setID(packet.getID());
        DiscoInfoQuery disco = (DiscoInfoQuery)_sdf.createExtensionNode(DiscoInfoQuery.NAME, DiscoInfoQuery.class);

        try {
            if (InfoQuery.GET.equals(packet.getType())) {
                XMPPApplicationInfo appinfo = (XMPPApplicationInfo)_provider.getApplicationInfo();
                appinfo.fillDiscoResponse(disco,
                                          query.getAttributeValue("node"));

            } else {
                // todo decide what to do for a set request
                return;
            }
            iq.add(disco);
            _connection.send(iq);
        } catch (StreamException se) {
            throw new CollaborationException(se.toString());
        }
    }

    private void processDiscoItemsRequest(Packet packet) throws CollaborationException  {
        if (InfoQuery.RESULT.equals(packet.getType()) ||
            InfoQuery.ERROR.equals(packet.getType()) ||
            InfoQuery.SET.equals(packet.getType())) {
            // for now don't process the disco#result packets
            return;
        }
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(IQ_NAME, InfoQuery.class);
        iq.setFrom(packet.getTo());
        iq.setTo(packet.getFrom());
        iq.setType(InfoQuery.RESULT);
        iq.setID(packet.getID());
        try {
        DiscoItemsQuery disco = (DiscoItemsQuery)_sdf.createExtensionNode(DiscoItemsQuery.NAME, DiscoItemsQuery.class);
        iq.add(disco);
        _connection.send(iq);
        } catch (StreamException se) {
            throw new CollaborationException(se.toString());
        }
    }

    public boolean isGatewayEntry(JID jid) {
        try {
            if ((jid != null) && (_personalStoreService != null) &&
                _personalStoreService.isGatewayEntry(jid.toBareJID()))
            {
                    return true;
            }
        } catch(Exception e) {
        }
        return false;
    }

    private void notifyStreamError(StreamError error) {
        if (getSessionListener() != null) {
            _provider.cleanupSession(XMPPSession.this);
            _provider.cancel(_selectionKey);
            CollaborationException e = null;
            if (error != null)  {
                String errorCond = error.getDefinedCondition();
                String errorText = error.getText();
                if (errorText == null) errorText = "Server Disconnected";
                if (StreamError.CONFLICT_CONDITION.equals(errorCond)) {
                    e = new ConflictException("Duplicate Connection detected");
                } else if (StreamError.CONNECTION_TIMEOUT_CONDITION.equals(errorCond)){
                    e = new TimeoutException(errorText);
                } else if (StreamError.HOST_GONE_CONDITION.equals(errorCond) ||
                           StreamError.HOST_UNKNOWN_CONDITION.equals(errorCond) ||
                           StreamError.SYSTEM_SHUTDOWN_CONDITION.equals(errorCond)) 
                {
                    e = new ServiceUnavailableException(errorText);
                } else if (StreamError.IMPROPER_ADDRESSING_CONDITION.equals(errorCond)) {
                    e = new ItemNotFoundException(errorText);
                } else if (StreamError.INVALID_FROM_CONDITION.equals(errorCond) ||
                           StreamError.INVALID_ID_CONDITION.equals(errorCond) ||
                           StreamError.NOT_AUTHORIZED_CONDITION.equals(errorCond)) 
                {
                    e = new AuthorizationException(errorText);
                } else if (StreamError.REMOTE_CONNECTION_FAILED_CONDITION.equals(errorCond)) {
                    e = new RoutingException(errorText);
                } else {
                    e = new CollaborationException(errorText);
                }
            } else {
                e = new CollaborationException("Server Disconnected");
            }
            fireCollaborationSessionListener(e);
        } else {
            //check for the registerListeners
            for (Iterator itr = _regisListeners.entrySet().iterator(); itr.hasNext();) {
                Map.Entry entry = (Map.Entry)itr.next();
                XMPPRegistrationListenerWrapper listener =
                    (XMPPRegistrationListenerWrapper)entry.getValue();
                if (listener != null) {
                    String condition = RegistrationListener.SERVICE_UNAVAILABLE;
                    if ((error != null) &&
                        (error.getFirstElement(null, StreamError.NOT_AUTHORIZED_CONDITION) != null))
                    {
                        condition = RegistrationListener.NOT_AUTHORIZED;
                    }
                    listener.getRegisListener().registrationFailed(condition, "Stream error", null);
                    _regisListeners.remove(entry.getKey());
                }
            }
        }
    }

    private boolean keepAliveEnabled = false;
    
    public void setKeepAliveEnabled(boolean keepAliveEnabled){
        this.keepAliveEnabled = keepAliveEnabled;
    }
    
    public boolean isKeepAliveEnabled(){
        return keepAliveEnabled;
    }
    
    static private byte[] keepAliveBuffer = { ' ', ' ' };
    protected  void sendKeepAlive() {
        if (isKeepAliveEnabled()){
            synchronized(_connection) {
                try {
                    _css.write(keepAliveBuffer, 0, 2);
                } catch (IOException ioe) {
                    // close???
                }
            }
        }
    }

    private boolean _PEPEnabled = true;
    void supportsPersonalEvents(boolean b) { _PEPEnabled = b; }
    boolean supportsPersonalEvents() { return _PEPEnabled; }


    protected boolean _initFeaturesSupported() {
        if (_serverFeaturesDiscovered) return true;
        DiscoInfoQuery diq = null;
        try {
            diq = sendInfoQuery(_server);
        } catch (CollaborationException ce)  {
            XMPPSessionProvider.debug(null, ce);
            _serverFeaturesDiscovered = false;
        }

        if (diq != null) {
            // discover PEP support
            if (diq.getIdentity("pubsub", "pep") == null) {
                _PEPEnabled = false;
            }

	    boolean capsSupported = false;
            for (Iterator i = diq.getFeatures().iterator();i.hasNext(); ) {
                String feature = (String)i.next();
                if (AMPExtension.NAMESPACE.equals(feature)) {
                    ampSupported = true;
                } else if (XMPPSessionProvider.CAPS_NAMESPACE.equals(feature)) {
		    capsSupported = true;
                }

                // todo : there are many other features to assert, like
                // search, version, etc...
		
            }
            _serverFeaturesDiscovered = true;

	    try {
		((XMPPApplicationInfo)_provider.getApplicationInfo()).supportsCaps(capsSupported);
	    } catch(Exception e) {
	    }

        } /* else {
             serviceFailed = true;
         }
         */
        if (ampSupported) {
            try {
                diq = sendInfoQuery(_server, AMPExtension.NAMESPACE);
            } catch (CollaborationException ce)  {
                XMPPSessionProvider.debug(null, ce);
                //_serverFeaturesDiscovered = false;
            }
            for (Iterator i = diq.getFeatures().iterator();i.hasNext(); ) {
                String feature = (String)i.next();
                if (AMP_COND_DELIVER_NAMESPACE.equals(feature)) {
                    ampCondDeliverSupported = true;
                } else if (AMP_COND_EXPIREAT_NAMESPACE.equals(feature)) {
                    ampCondExpireAtSupported = true;
                } else if (AMP_COND_MATCHRES_NAMESPACE.equals(feature)) {
                    ampCondMatchResourceSupported = true;
                } else if (AMP_ACTION_DROP_NAMESPACE.equals(feature)) {
                    ampActionDropSupported = true;
                } else if (AMP_ACTION_ALERT_NAMESPACE.equals(feature)) {
                    ampActionAlertSupported = true;
                } else if (AMP_ACTION_NOTIFY_NAMESPACE.equals(feature)) {
                    ampActionNotifySupported = true;
                } else if (AMP_ACTION_ERROR_NAMESPACE.equals(feature)) {
                    ampActionErrorSupported = true;
                }
            }
        }
        return _serverFeaturesDiscovered;
    }

    protected void waitForServiceInitialization(Object service) {
        try {
            if(!_loadingServices) {
                //  make sure this is it not called multiple times
                // concurrently.
                synchronized(_loadingServiceLock) {
                    if(!_loadingServices) {
                        _loadingServices = true;
                        loadJabberServices();
                    }
                }
            }
            synchronized(service) {
                long waitTime = getShortRequestTimeout();
                long start = System.currentTimeMillis();
                while(!isServiceInitialized(service)) {
                    try {
                        service.wait(waitTime);
                    } catch(InterruptedException ie) {
                        XMPPSessionProvider.debug(ie.toString(),ie);
                    }
                    if(!isServiceInitialized(service)) {
                        long end = System.currentTimeMillis();
                        int slept = (int)(end - start);
                        if(slept >= waitTime) break;
                        else {
                            waitTime -= slept;
                            start = end;
                        }
                    }
                }
            }
        } catch(Exception e) {
            XMPPSessionProvider.debug(e.toString(), e);
        }
        synchronized(_loadingServiceLock) {
            _loadingServices = false;
        }
    }

    private boolean isServiceInitialized(Object service) {
        synchronized(_jabberServiceLock) {
            if(service instanceof ConferenceService) {
                return (_mucService != null);
            }
            if(service instanceof NewsService) {
                return (_pubsubService != null);
            }
            if(service instanceof PersonalStoreService) {
                return (_judService != null);
            }
            return false;
        }
    }


    private void processRegisterResponse(InfoQuery iq, RegisterQuery registerQuery,
                                    XMPPRegistrationListenerWrapper regisListenerWrapper,
                                    String serverStr) throws CollaborationException {
        RegistrationListener regisListener = regisListenerWrapper.getRegisListener();
        XMPPRegistrationListenerWrapper.RequestType reqType = regisListenerWrapper.getRequestType();

        if (iq.getType() == InfoQuery.RESULT && registerQuery != null) {
            if (registerQuery.isRegistered()) {
                if (XMPPRegistrationListenerWrapper.USER_REGISTRATION.equals(reqType)) {
                    regisListener.registered(serverStr);
                    return;
                }
                // it is a gateway re-registration
            }
            keyValue = registerQuery.getKey();
            registerQuery.clearKey();
            Set fields = registerQuery.getFieldNames();

            Map fieldValuePairs = new HashMap();
            for (Iterator iter = fields.iterator(); iter.hasNext();) {
                String fieldName = (String)iter.next();
                fieldValuePairs.put(fieldName, registerQuery.getField(fieldName)); // put the value also if any
            }
            boolean filled = regisListener.fillRegistrationInformation(fieldValuePairs,
            serverStr);
            if (!filled) {
                // do not do anything
                if (XMPPRegistrationListenerWrapper.USER_REGISTRATION.equals(reqType)) {
                    logout();
                }
                return;
            } else {
                String userName = (String)fieldValuePairs.get(RegistrationListener.USERNAME);
                String password = (String)fieldValuePairs.get(RegistrationListener.PASSWORD);
                if (userName == null || userName.equals("") || password == null
                || password.equals("")) {
                    regisListener.registrationFailed(RegistrationListener.MISSING_DATA, "Empty username or password is not allowed",
                    serverStr);
                    if (XMPPRegistrationListenerWrapper.USER_REGISTRATION.equals(reqType)) {
                        logout();
                    }
                    return;
                }
            }

            if (iq.getFrom() != null) {
                sendRegistration(iq.getFrom(), fieldValuePairs, iq.getID());
            } else {
                sendRegistration(_server, fieldValuePairs, iq.getID());
            }
        }
    }

    private void processRegisSuccessFailure(InfoQuery iq,
                            XMPPRegistrationListenerWrapper regisListenerWrapper,
                            String serverStr) throws CollaborationException {

        RegistrationListener regisListener = regisListenerWrapper.getRegisListener();
        XMPPRegistrationListenerWrapper.RequestType reqType = null;
        reqType = regisListenerWrapper.getRequestType();

        if (iq.getType() == InfoQuery.RESULT) {
            if (XMPPRegistrationListenerWrapper.GATEWAY_REGISTRATION.
                                        equals(reqType)) {
                postGatewayRegistration(serverStr);
                regisListener.registered(serverStr);
            } else if (XMPPRegistrationListenerWrapper.USER_REGISTRATION.
                                            equals(reqType)) {
                regisListener.registered(serverStr);
                logout();
            } else if (XMPPRegistrationListenerWrapper.USER_PASSWD_CHANGE.
                                                        equals(reqType)) {
                regisListener.registrationUpdated(serverStr);
            } else if (XMPPRegistrationListenerWrapper.GATEWAY_UNREGISTRATION.
                                            equals(reqType)) {
                postGatewayUnregistration(serverStr);
                regisListener.unregistered(serverStr);
            } else if (XMPPRegistrationListenerWrapper.USER_UNREGISTRATION.
                                            equals(reqType)) {
                regisListener.unregistered(serverStr);
            }
            _regisListeners.remove(iq.getID());

        } else if (iq.getType() == InfoQuery.ERROR) {
            XMPPSessionProvider.debug("Error in registration");
            PacketError pe = iq.getError();
            if (pe != null) {
                if (XMPPRegistrationListenerWrapper.USER_PASSWD_CHANGE.equals(reqType)) {
                    regisListener.registrationUpdateFailed(pe.getDefinedCondition(), pe.getText(), serverStr);
                } else if (XMPPRegistrationListenerWrapper.GATEWAY_UNREGISTRATION.equals(reqType) ||
                    XMPPRegistrationListenerWrapper.USER_UNREGISTRATION.equals(reqType)){
                    regisListener.unregistrationFailed(getRegistrationErrorCondition(
                                                            pe.getDefinedCondition()),
                                                        pe.getText(), serverStr);
                } else if (XMPPRegistrationListenerWrapper.GATEWAY_REGISTRATION.equals(reqType) ||
                    XMPPRegistrationListenerWrapper.USER_REGISTRATION.equals(reqType)) {
                    regisListener.registrationFailed(getRegistrationErrorCondition(
                                                        pe.getDefinedCondition()),
                                                      pe.getText(), serverStr);
                }
            } else {
                XMPPSessionProvider.debug("Packet error is null");
            }
            _regisListeners.remove(iq.getID());
            if (XMPPRegistrationListenerWrapper.USER_REGISTRATION.equals(reqType)) {
                logout();
            }
            return;
        }
    }

    public int addWorkerRunnable(Runnable r) {
        return _provider.addRunnable(r);
    }
    
    private void fireCollaborationSessionListener(CollaborationException e) {
        addWorkerRunnable(new CollaborationSessionNotifier(e));
    }
    
    private void fireMessageProcessingListener(org.netbeans.lib.collab.Message m, int status, CollaborationException e) {
        addWorkerRunnable(new MessageProcessingListenerNotifier(m, status, e));
    }
        
    public  void addSessionListener(CollaborationSessionListener listener) {
        if (!_sessionListeners.contains(listener))
            _sessionListeners.add(listener);
    }

    public void removeSessionListener(CollaborationSessionListener listener) {
        _sessionListeners.remove(listener);
    }
    
    public JID getMUCService() {
        synchronized(_jabberServiceLock) {
            return _mucService;
        }
    }
    
    public void setMUCService(JID service){
        synchronized(_jabberServiceLock){
            this._mucService = service;
        }
    }

    public JID getPubSubService() {
        synchronized(_jabberServiceLock) {
            return _pubsubService;
        }
    }

    public JID getJUDService() {
        synchronized(_jabberServiceLock) {
            return _judService;
        }
    }

    public Map getGateways() {
        synchronized(_jabberServiceLock) {
            return _gateways;
        }
    }
 
    private class CollaborationSessionNotifier implements Runnable {
        CollaborationException ce;
        CollaborationSessionNotifier(CollaborationException ce) {
            this.ce = ce;
        }

        public void run() {
            synchronized(_sessionListeners) {
                for(Iterator itr = _sessionListeners.iterator(); itr.hasNext();) {
                    try {
                        CollaborationSessionListener l = (CollaborationSessionListener)itr.next();
                        if (l == null) continue;
                        l.onError(ce);
                    } catch(Exception e) {
                        XMPPSessionProvider.error(e.toString(),e);
                    }
                }
            }
        }
    }
    
    private class MessageProcessingListenerNotifier implements Runnable {
        private org.netbeans.lib.collab.Message m;
        private int status;
        private CollaborationException ce;
        MessageProcessingListenerNotifier(org.netbeans.lib.collab.Message m, int status, CollaborationException ce) {
            this.ce = ce;
            this.m = m;
            this.status = status;
        }

        public void run() {
            synchronized(_sessionListeners) {
                for(Iterator itr = _sessionListeners.iterator(); itr.hasNext();) {
                    try {
                        CollaborationSessionListener l = (CollaborationSessionListener)itr.next();
                        if (l == null || (!(l instanceof MessageProcessingListener))) continue;
                        ((MessageProcessingListener)l).onMessageStatus(m, status, ce);
                    } catch(Exception e) {
                        XMPPSessionProvider.error(e.toString(),e);
                    }
                }
            }
        }
    }
    
    protected StreamSourceCreator getStreamSourceCreator(){
        return _streamSrcCreator;
    }
    
    public static CollaborationException getCollaborationException(PacketError error, String errorText) {
        if (error == null) return null;
        String errorCond = error.getDefinedCondition();
        if (errorText == null) errorText = error.getText();
        if (errorCond != null) {
            if (PacketError.FEATURE_NOT_IMPLEMENTED_CONDITION.equals(errorCond) ||
                PacketError.SERVICE_UNAVAILABLE_CONDITION.equals(errorCond)) 
            {
                return new ServiceUnavailableException(errorText);
            }
            if (PacketError.CONFLICT_CONDITION.equals(errorCond)) {
                return new ConflictException(errorText);
            }
            if (PacketError.FORBIDDEN_CONDITION.equals(errorCond)) {
                return new AuthorizationException(errorText, AuthorizationException.INSUFFICIENT_PERMISSIONS);
            }
            if (PacketError.ITEM_NOT_FOUND_CONDITION.equals(errorCond)) {
                return new ItemNotFoundException(errorText);
            }
            if (PacketError.NOT_ACCEPTABLE_CONDITION.equals(errorCond)) {
                return new AuthorizationException(errorText, AuthorizationException.NOT_ALLOWED);
            }
            if (PacketError.NOT_ALLOWED_CONDITION.equals(errorCond)) {
                return new AuthorizationException(errorText);
            }
            if (PacketError.NOT_AUTHORIZED_CONDITION.equals(errorCond)) {
                return new AuthorizationException(errorText, AuthorizationException.INVALID_CREDENTIALS);
            }
            if (PacketError.PAYMENT_REQUIRED_CONDITION.equals(errorCond)) {
                return new AuthorizationException(errorText, AuthorizationException.PAYMENT_REQUIRED);
            }
            if (PacketError.RECIPIENT_UNAVAILABLE_CONDITION.equals(errorCond)) {
                return new RecipientUnvailableException(errorText);
            }
            if (PacketError.SUBSCRIPTION_REQUIRED_CONDITION.equals(errorCond)) {
                return new AuthorizationException(errorText, AuthorizationException.SUBSCRIPTION_REQUIRED);
            }
            if (PacketError.REGISTRATION_REQUIRED_CONDITION.equals(errorCond)) {
                return new AuthorizationException(errorText, AuthorizationException.REGISTRATION_REQUIRED);
            }
            if (PacketError.REMOTE_SERVER_NOT_FOUND_CONDITION.equals(errorCond)) {
                return new RoutingException(errorText);
            }
            if (PacketError.REMOTE_SERVER_TIMEOUT_CONDITION.equals(errorCond) ||
                PacketError.RESOURCE_CONSTRAINT_CONDITION.equals(errorCond))
            {
                return new TimeoutException(errorText);
            }
            /*if (PacketError.UNDEFINED_CONDITION.equals(errorCond)) {
                //use collaboration exception
            }
            if (PacketError.UNEXPECTED_REQUEST_CONDITION.equals(errorCond)) {
                //use collaboration exception
            }
            if (PacketError.BAD_REQUEST_CONDITION.equals(errorCond)) {
              //not needed  
            }
            if (PacketError.INTERNAL_SERVER_ERROR_CONDITION.equals(errorCond)) {
                //throw collaboration exception
            }
            if (PacketError.REDIRECT_CONDITION.equals(errorCond)) {
                //handled as part of stream errors
            }*/
        }
        return new CollaborationException(error.getText());
    }
    
    public JID getVoipComponent(){
        return _voipComponent;
    }
    
    private boolean isJinglePacket(Packet packet){
        if(_p2pservice != null &&
            _p2pservice.isJinglePacket(packet)) {
            return true;
        }
        return false;
    }
    


    private static final boolean comparatorMethodAvailable;
    private static java.lang.reflect.Method sendAndWatch1 = null;
    private static java.lang.reflect.Method sendAndWatch2 = null;
    static {
        boolean available = false;
        try{
            Class clazz = Class.forName("org.jabberstudio.jso.util.PacketMonitor");
            Class[] params = new Class[] {Stream.class , Packet.class , Comparator.class};
            sendAndWatch1 = clazz.getMethod("sendAndWatch" , params);
            params = new Class[] {Stream.class , Packet.class , Long.TYPE, Comparator.class};
            sendAndWatch2 = clazz.getMethod("sendAndWatch" , params);
            if (null != sendAndWatch1 && null != sendAndWatch2){
                available = true;
            }
        }catch(Exception ex){
            //available = false;
            //ex.printStackTrace();
        }catch(NoClassDefFoundError ncdfEx){
            //available = false;
            //ncdfEx.printStackTrace();
        }finally{
            comparatorMethodAvailable = available;
        }
    }

    public Packet sendAndWatch(Packet packet) throws IllegalArgumentException,
            StreamException {
        String id = packet.getID();
        addSendAndWatchID(id);
        try{
            if (null != id && comparatorMethodAvailable){
                Comparator comparator = new RedispatchComparator(packet);
                //return PacketMonitor.sendAndWatch(getConnection() , packet , comparator);
                return (Packet)sendAndWatch1.invoke(null , new Object[]{getConnection() , packet , comparator});
            }
            return PacketMonitor.sendAndWatch(getConnection() , packet);
        }
	catch(IllegalAccessException accEx){
            // Can happen ?
            accEx.printStackTrace();
            throw new RuntimeException(accEx);
        }
	catch(java.lang.reflect.InvocationTargetException itEx){
            //itEx.printStackTrace();
	    Throwable th = itEx.getTargetException();

	    if (th instanceof IllegalArgumentException){
		    throw (IllegalArgumentException)th;
	    }
	    if (th instanceof StreamException){
		    throw (StreamException)th;
	    }

            throw new RuntimeException(itEx);
        }finally{
            forceRemoveSendAndWatch(id);
        }
    }
    
    public Packet sendAndWatch(Packet packet , long timeout)
    throws IllegalArgumentException, StreamException {
        String id = packet.getID();
        addSendAndWatchID(id);
        try{
            if (null != id && comparatorMethodAvailable){
                Comparator comparator = new RedispatchComparator(packet);
                //return PacketMonitor.sendAndWatch(getConnection() , packet , timeout , comparator);
                return (Packet)sendAndWatch2.invoke(null , new Object[]{getConnection() , packet , new Long(timeout) , comparator});
            }
            return PacketMonitor.sendAndWatch(getConnection() , packet , timeout);
        }
	catch(IllegalAccessException accEx){
            // Can happen ?
            accEx.printStackTrace();
            throw new RuntimeException(accEx);
        }
	catch(java.lang.reflect.InvocationTargetException itEx){
            //itEx.printStackTrace();
	    Throwable th = itEx.getTargetException();

	    if (th instanceof IllegalArgumentException){
		    throw (IllegalArgumentException)th;
	    }
	    if (th instanceof StreamException){
		    throw (StreamException)th;
	    }

            throw new RuntimeException(itEx);
        }finally{
            forceRemoveSendAndWatch(id);
        }
    }

    private class RedispatchComparator implements Comparator{

        private Packet sentPacket;

        public RedispatchComparator(Packet sentPacket){
            this.sentPacket = sentPacket;

        }

        public int compare(Object o1, Object o2) {

            if (null == o1 && null == o2){
                return 0;
            }

            if (o1 instanceof Packet && o2 instanceof Packet
                    // Paranoia
                    && (o1 == sentPacket || o2 == sentPacket)){
                Packet recievedPacket = (Packet)(o1 == sentPacket ? o2 : o1);

                String id1 = ((Packet)o1).getID();
                String id2 = ((Packet)o2).getID();

                JID inFrom = recievedPacket.getFrom();
                JID outTo = sentPacket.getTo();

                JID ourJID = null != _client ? _client.getJID() : null;

                if (null != id1 && null != id2 &&
                        null != inFrom && null != outTo &&
                        null != ourJID &&
                        id1.equals(id2) &&
                        inFrom.toBareJID().equals(ourJID.toBareJID()) &&
                        inFrom.toBareJID().equals(outTo.toBareJID()))
                {
                    return isSendAndWatchIDRecievedOnce(id1) ? 0 : 1;
                }
            }

            // Rest of the validation will be handled by PacketMonitor
            return 0;
        }
    };
    
    private static long getLongProperty(String key, long def){
        String str = System.getProperty(key);
        long retval = def;
        
        if (null != str){
            try{
                retval = Long.parseLong(str.trim());
            } catch(NumberFormatException nfEx){}
        }
        return retval;
    }

    public Set getFeatures(String jid) {
        try {
            DiscoInfoQuery diq = sendInfoQuery(new JID(jid), null);
            return diq.getFeatures();
        } catch (JIDFormatException ex) {
            xmpplogger.warn("Could not get features, invalid JID : "+ jid);
        } catch (CollaborationException ex) {
            xmpplogger.warn("Could not get features for " + jid + " : " + ex.getMessage());
        }
        return null;
    }
    
    JingleService _p2pservice;
    public P2PService getP2PService(){
        if(_p2pservice == null){
            _p2pservice = new JingleService(this);
        }
        return _p2pservice;
    }
}
