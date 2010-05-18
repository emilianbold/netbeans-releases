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
import java.nio.channels.*;
import java.net.*;
import org.jabberstudio.jso.sasl.SASLMechanismManager;
import java.security.cert.X509Certificate;

import org.netbeans.lib.collab.*;

import org.jabberstudio.jso.*;
import org.netbeans.lib.collab.xmpp.jso.impl.x.jingle.JingleFactory;

import org.netbeans.lib.collab.xmpp.jso.impl.x.pubsub.PubSubExtensionFactory;
import org.netbeans.lib.collab.xmpp.jso.impl.x.event.MessageEventFactory;
import org.netbeans.lib.collab.xmpp.jso.impl.x.amp.AMPNodeFactory;


import org.netbeans.lib.collab.util.*;
import org.apache.log4j.*;
import org.apache.log4j.varia.*;
import org.netbeans.lib.collab.xmpp.sasl.IdentitySSOClientProviderFactory;

/**
 *
 * 
 * @author Rahul Shah
 * @author Vijayakumar Palaniappan
 * 
 */
public class XMPPSessionProvider implements org.netbeans.lib.collab.CollaborationSessionProvider
{
    

    private List _sessions = new LinkedList();
    
    private XMPPApplicationInfo _appinfo = null;

    public static final int LOGINTYPE_PRIMARY = 1;
    public static final int LOGINTYPE_SECONDARY = 2;

    public static final String KEEP_ALIVE_INTERVAL = "org.netbeans.lib.collab.xmpp.session.keepaliveinterval";
    public static final String SO_SNDBUF = "org.netbeans.lib.collab.xmpp.so_sndbuf";
    public static final String SO_RCVBUF =  "org.netbeans.lib.collab.xmpp.so_rcvbuf";
    private static int so_sndbuf;
    private static int so_rcvbuf;
    
    boolean keepGoing = true;

    // worker cpacity = 20 times concurrency
    private Worker _worker = new Worker(1, concurrency, concurrency*20,"XMPPSessionProvider");

    //NioSelectWorker channelManager;
    protected SelectWorker channelManager;

    public static final String logPropertyName = "org.netbeans.lib.collab.xmpp.log";
    public static final String concurrencyPropertyName = "org.netbeans.lib.collab.xmpp.concurrency";
    static int concurrency = 10;

    /** process id system property */
    public final static String JVMID_PROP = "com.iplanet.im.jvmid";
    
    // ----------------------------------------------------------
    // log4j debug
    // ----------------------------------------------------------
    private static Logger logger =  LogManager.getLogger("org.netbeans.lib.collab.xmpp");
    //private static boolean debugOn = false;
    protected static String processId;
    
    static final String CAPS_NAMESPACE = "http://jabber.org/protocol/caps";
    static final String CAPS_URI_PREFIX = CAPS_NAMESPACE + "#";
    static final NSI CAPS_NSI = new NSI("c", CAPS_NAMESPACE);
    static final String CAPS_NODE = "http://collab.netbeans.org/xmpp";    

    static {
	try {
	    // this is needed to init log4j incase the app is not doing
            LogManager.getRootLogger().addAppender(new NullAppender());
            
                /** global process identifier */
   
            try {
                processId = InetAddress.getLocalHost().getHostAddress() + "." + System.getProperty(JVMID_PROP, "jvm");
            } catch (Exception e) {
                processId = "ip." + System.getProperty(JVMID_PROP, "pid");
            }
    
	} catch (Exception e) {
	    //e.printStackTrace();
	}
        //just for initing , these are not used in this class
        StreamDataFactory  _sdf = JSOImplementation.getInstance().getDataFactory();
        _sdf.registerElementFactory(new org.netbeans.lib.collab.xmpp.jso.impl.x.muc.MUCFactory());
        _sdf.registerElementFactory(new PubSubExtensionFactory());
        _sdf.registerElementFactory(new MessageEventFactory());
        _sdf.registerElementFactory(new AMPNodeFactory());
        _sdf.registerElementFactory(new JingleFactory());
        Class c = JID.class;
        concurrency = Integer.getInteger(
                            concurrencyPropertyName,concurrency).intValue();
        so_sndbuf  = Integer.getInteger(
                            XMPPSessionProvider.SO_SNDBUF,0).intValue();
        so_rcvbuf   = Integer.getInteger(
                            XMPPSessionProvider.SO_RCVBUF,0).intValue();
    }
    
    /** Creates a new instance of XMPPSessionProvider */
    public XMPPSessionProvider() {
	try {
	    //channelManager = new NioSelectWorker (1, 10);
	    channelManager = new SelectWorker(_worker);
	    new Thread((Runnable) channelManager).start();
    	} catch (Exception e) {
	    //e.printStackTrace();
	}
    startKeepAlive(getKeepAliveInterval());
        registerProvider(new IdentitySSOClientProviderFactory());
    }

    public Object register(SocketChannel sch, Runnable callback) throws IOException{
	return channelManager.register(sch, callback);
    }

    public Object register(SocketChannel sch, Runnable callback,
			   BufferedByteChannel writes)
	throws IOException
    {
	return channelManager.register(sch, callback, writes);
	//return null;
    }

    public void cancel(Object key) {        
	channelManager.cancel(key);
    }

    public int addRunnable(Runnable r) {
	return _worker.addRunnable(r);
    }
    
    public void close() {
	if (_keepAliveThread != null) {
	    _keepAliveThread.close();
	    //_keepAliveThread.join();
	}
	channelManager.close();
	_worker.stop();
    }
    
    public CollaborationSession getSession(String serviceUrl,
                                           String loginName,
                                           String password,
                                           CollaborationSessionListener collaborationSessionListener)
        throws CollaborationException 
    {
        return getSession(serviceUrl, null,
                          loginName, password, LOGINTYPE_PRIMARY,
                          collaborationSessionListener);
    }
    
    public CollaborationSession getSession(String serviceUrl, 
                                           String loginName,
                                           String password,
                                           int loginType,
                                           CollaborationSessionListener collaborationSessionListener)
        throws CollaborationException 
    {
        return getSession(serviceUrl, null, 
                          loginName, password, loginType, 
                          collaborationSessionListener);
    }
        
    public CollaborationSession getSession(String serviceUrl,
                                           String destination,
                                           String loginName,
                                           String password,
                                           CollaborationSessionListener collaborationSessionListener)
        throws CollaborationException 
    {
        return getSession(serviceUrl, destination,
                          loginName, password, LOGINTYPE_PRIMARY,
                          collaborationSessionListener);
    }
    
    public CollaborationSession getSession(String serviceUrl,
                                           String destination,
                                           String loginName,
                                           String password,
                                           int loginType,
                                           CollaborationSessionListener collaborationSessionListener)
        throws CollaborationException 
    {
        if (destination == null) {
            // construct a destination the best we can.
            
            // resource may be provided in login name
            String resource = null;
            //parse uid to get the resource
            int index = loginName.indexOf("/");
            if (index > 0) {
                resource = loginName.substring(index + 1);
                loginName = loginName.substring(0,index);
            } else {
                resource = Long.toString(System.currentTimeMillis());
            }
            
            destination = (new JID(getNode(loginName),
                                   getDomain(loginName, serviceUrl),
                                   resource)).toString();
        }

        try {
        
            XMPPSession s = createSession(serviceUrl, destination,
                              loginName, password, loginType,
                              collaborationSessionListener);
            synchronized (_sessions){
                _sessions.add(s);
            }
            return s;
        } catch (CollaborationException e) {
            // e.printStackTrace();
            throw e;
        }
    }

    String getNode(String loginName) throws CollaborationException {
        // node is local part of login name.  Note that
        // login name may include characters not allowed by 
        // nodeprep.  If so it is url-encoded iun order to avoid
        // a jid format exception
        String node = StringUtility.getLocalPartFromAddress(loginName);
        node = StringUtility.unquoteSpecialCharacters(node);
        String retval = JIDUtil.encodedNode(node);
        return retval;
    }

    protected String getDomain(String loginName, String serviceURL) {
        // domain is domain part of login name.  if not present
        // fall back to domain found in service URL.
        String domain =
            StringUtility.getDomainFromAddress(loginName, null);
        if (domain == null) {
            int startIndex = serviceURL.indexOf(":");
            if (startIndex != -1) {
                domain = serviceURL.substring(0, startIndex);
            } else {
                domain = serviceURL;
            }
        }
        return domain;
    }

    public void register(String serviceURL, RegistrationListener listener) throws CollaborationException {
        
        register(serviceURL, null , listener);
    }
    
    public void register(String serviceURL, String domain , RegistrationListener listener) throws CollaborationException {
        XMPPSession s = createSession(serviceURL, domain, null, null,
                LOGINTYPE_PRIMARY,
                getCollaborationSessionListerForRegistration(listener));
        XMPPRegistrationListenerWrapper regisListener =
                new XMPPRegistrationListenerWrapper(listener);
        regisListener.setRequestType(XMPPRegistrationListenerWrapper.USER_REGISTRATION);
        s.register(null, regisListener);
    }
    
    void cleanupSession(XMPPSession session) {
        //remove it from the cache
        if (null != session) {
            synchronized (_sessions){
                _sessions.remove(session);
            }
        }
    }

    public static void debug(String msg) { 
        logger.debug(msg); 
    }
    
    public static void debug(String msg, Throwable t) { 
        logger.debug(msg,t); 
    }
    
    public static void error(String msg) { 
        logger.error(msg); 
    }
    
    public static void error(String msg, Throwable t) { 
        logger.error(msg,t); 
    }
    
    public static void fatal(String msg) { 
        logger.fatal(msg); 
    }

    public static void fatal(String msg, Throwable t) { 
        logger.fatal(msg,t); 
    }
    
    public static void warning(String msg) { 
        logger.warn(msg); 
    }
    
    public static void warning(String msg, Throwable t) { 
        logger.warn(msg,t); 
    }
    
    public static void info(String msg) {
        logger.info(msg); 
    }
    
    public static void info(String msg, Throwable t) {
        logger.info(msg,t); 
    }
    
    protected XMPPSession createSession(String serviceUrl, 
                String destination, 
                String loginName, 
                String password, 
                int loginType, 
                CollaborationSessionListener collaborationSessionListener) 
                                                throws CollaborationException
    {
        return new XMPPSession(this, serviceUrl, destination,
                               loginName, password, loginType, 
                               collaborationSessionListener, 
                               new SocketStreamSourceCreator(collaborationSessionListener, channelManager));
        // call new StreamSourceCreator(collaborationSessionListener,
        //                              channelManager)
        // to enable non blocking writes
    }
    
    protected CollaborationSessionListener getCollaborationSessionListerForRegistration(
            final RegistrationListener listener)
    {
        if(listener instanceof SecureRegistrationListener){
            return new SecureSessionListener() {

                public boolean onX509Certificate(X509Certificate chain[])
                {
                    return ((SecureRegistrationListener)listener).onX509Certificate(chain);
                }

                public void onError(CollaborationException collaborationexception)
                {
                }
            };
        }
        return null;
    }

    protected long getKeepAliveInterval() {
        String keepAlive = System.getProperty(KEEP_ALIVE_INTERVAL);
        long ret = 0;
        if(keepAlive != null) {
            try {
                ret = Long.parseLong(keepAlive);
            } catch(NumberFormatException e) {
            }
        }
        return ret * 1000;
    }

    public ApplicationInfo getApplicationInfo() throws CollaborationException {
	if (_appinfo == null) _appinfo = new XMPPApplicationInfo();
        return _appinfo;
    }

    public void setApplicationInfo(ApplicationInfo ai) throws CollaborationException {
	if (ai == null) return;
	if (ai instanceof XMPPApplicationInfo) {
	    _appinfo = (XMPPApplicationInfo)ai;
	} else if (_appinfo != null) {
	    _appinfo.update(ai);
	} else {
	    _appinfo = new XMPPApplicationInfo(ai);
	}
    }

    private KeepAliveThread _keepAliveThread = null;
    
    synchronized void startKeepAlive(long period) {
        if(period > 0) {
            if (_keepAliveThread == null) {
                _keepAliveThread = new KeepAliveThread(period);
                _keepAliveThread.start();
            } else if (period < _keepAliveThread.getPeriod()) {
            _keepAliveThread.setPeriod(period);
        }
        }
    }
    
    public static int getSocketSendbufferSize() {
        return so_sndbuf;
    }

    public static int getSocketReceivebufferSize() {
        return so_rcvbuf;
    }
   
    class KeepAliveThread extends Thread
    {
        boolean running = true;
        long period;  // in ms

        KeepAliveThread(long period) { this.period = period; }

        public synchronized void close() {
            synchronized(_sessions) {
                running = false;
                _sessions.notifyAll();
            }
        }

        public void setPeriod(long period) { this.period = period; }
        
        public long getPeriod() { return period; }

        public void run() {
            while (running) {
                synchronized(_sessions) {
                    try { _sessions.wait(period); } catch (Exception e) {}
                    if (!running) return;

                    for (Iterator i = _sessions.iterator();
                         i.hasNext(); ) {
                        XMPPSession s = (XMPPSession)i.next();
                        s.sendKeepAlive();
                    }
                }
            }
        }
    }
    
    private Map authProviderMap = new HashMap();

    {
        List mechanisms = SASLMechanismManager.getInstance().getClientMechanismNames(null);
        Iterator iter = mechanisms.iterator();
        
        while (iter.hasNext()){
            String mechanism = (String)iter.next();
            authProviderMap.put(mechanism , new JSOSASLProviderFactory(mechanism));
        }
    }
    
    public Map getAuthProviderMap(){
        return authProviderMap;
    }
    
    /**
     * Register a SASL client side provider with the provider.
     * If multiple provider's are registered and they support same subset of 
     * mechanism's , then the last provider will override the previous ones.
     *
     * @return The list of mechanism's for which this provider is registered for.
     */
    public void registerProvider(SASLClientProviderFactory provider){
        if (null == provider){
            throw new NullPointerException("provider == null");
        }
        
        String supported[] = provider.getSupportedMechanisms();
        if (null == supported || 0 == supported.length){
            return ;
        }
        
        int count;
        for (count = 0;count < supported.length; count ++){
            String mech = supported[count];
            
            if (null != mech){
                authProviderMap.put(mech , provider);
            }
        }
        return ;
    }
    
    /**
     * Find out if there is a provider registered for the mechanism specified.
     * There will always be a provider for SASL PLAIN , SASL DIGEST-MD5 and 
     * old jabber auth.
     *
     * @return 
     * true if there is a provider registered to handle the specified mechanism.
     */
    public boolean isSASLProviderRegistered(String mechanism){
        if (null == mechanism){
            throw new NullPointerException("provider == null");
        }

        return null != authProviderMap.get(mechanism);
    }
        
}
