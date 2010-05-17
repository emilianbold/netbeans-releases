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
package org.netbeans.modules.collab.provider.im;

import com.sun.collablet.Account;
import com.sun.collablet.AccountManager;
import com.sun.collablet.CollabException;
import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;

import org.apache.log4j.*;
import org.openide.*;
import org.openide.awt.*;
import org.openide.util.*;

import java.beans.*;

import java.io.*;

import java.net.*;

import java.util.*;

import org.netbeans.lib.collab.*;

import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author  Todd Fast <todd.fast@sun.com>
 */
public class IMCollabManager extends CollabManager {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    private static final String PROXY_SESSION_FACTORY_CLASS = "org.netbeans.lib.collab.xmpp.ProxySessionProvider"; // NOI18N
    private static final String LIB_KEEPALIVE = "org.netbeans.lib.collab.xmpp.session.keepaliveinterval";
    private static final String HTTPS_KEEPALIVE = "org.netbeans.lib.collab.https.keepalive";
    private static final String SOCKS_KEEPALIVE = "org.netbeans.lib.collab.socks.keepalive";
    private static final String DEFAULT_KEEPALIVE = "30"; // 30 seconds

    private IMReconnect reconnect = new IMReconnect();
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    static {
        if (System.getProperty(LIB_KEEPALIVE) == null)
		System.setProperty(LIB_KEEPALIVE, "30");
             
        // if the log4j is not explicitely configured, redirect it
        // to a (nonexistent) file out of the default package.    
        if (System.getProperty("log4j.configuration") == null) {
	    System.setProperty("log4j.configuration",
                "org/netbeans/modules/collab/provider/im/log4j.properties");
        }
        
        // temp, to enable IM client log
        if (Debug.isEnabled()) {
            try {
                Logger logger = LogManager.getRootLogger();
                PatternLayout layout = new PatternLayout("%d{HH:mm:ss,SSS} %-5p %c [%t] %m%n"); // NOI18N

                //				ConsoleAppender appender = 
                //					new ConsoleAppender(layout, "System.out");
                String userHome = System.getProperties().getProperty("netbeans.user");
                String logs = userHome + File.separator + "collab" + File.separator + "logs";
                String clientLog = logs + File.separator + "client.log";
                File logsDir = new File(logs);

                if (!logsDir.exists()) {
                    logsDir.mkdirs();
                }

                File logFile = new File(clientLog);

                if (!logFile.exists()) {
                    logFile.createNewFile();
                }

                FileAppender appender = new RollingFileAppender(layout, clientLog);
                logger.setLevel(Level.DEBUG);
                logger.addAppender(appender);
            } catch (Exception e) {
                Debug.debugNotify(e);
                e.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private Map sessions = new HashMap();

    //	private CollaborationSessionFactory sessionFactory;
    //	private CollaborationSessionFactory proxySessionFactory;
    private boolean suspendSessionChangeEvents;

    /**
     *
     *
     */
    public IMCollabManager() {
        super();
    }

    /**
     *
     *
     */
    public String getDisplayName() {
        return "Sun Instant Messaging Server Collaboration Manager";
    }

    /**
     *
     *
     */
    protected synchronized void cleanup() {
        try {
            suspendSessionChangeEvents(true);

            // Logout each sessions. This would normally fire an event
            // for each session that was logged out, potentially causing
            // deadlocks with listeners trying to access this class during
            // event handling.
            CollabSession[] sessions = getSessions();

            for (int i = 0; i < sessions.length; i++) {
                // This should result in the session being removed from
                // the internal list of sessions
                sessions[i].logout();
            }

            // Close the session factory
            //			try
            //			{
            //				if (sessionFactory!=null)
            //				{
            //					sessionFactory.close();
            //					sessionFactory=null;
            //				}
            //			}
            //			catch (Exception e)
            //			{
            //				Debug.debugNotify(e);
            //			}
        } finally {
            suspendSessionChangeEvents(false);
            getChangeSupport().firePropertyChange(PROP_SESSIONS, null, null);
        }
    }

    /**
     *
     *
     */
    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();
    }

    ////////////////////////////////////////////////////////////////////////////
    // CollabManager methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void registerUser(Account account) throws IOException, CollabException {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        try {
            getCollaborationSessionFactory(account).getCollaborationSessionProvider().register(
                getServerURL(account), new Registration(account)
            );
        } catch (java.nio.channels.UnresolvedAddressException e) {
            // Could not resolve hostname
            throw new UnknownHostException(account.getServer());
        } catch (CollaborationException e) {
            //			if (e.getCause() instanceof org.jabberstudio.jso.StreamException)
            //			{
            //				Debug.out.println(" stream exception, server not responding");
            //				throw new IOException (e.getMessage());
            //			}
            // TODO: Is the cause the right thing to throw here?
            //			throw new CollabException(e.getCause());
            throw new IOException(e.getMessage());
        }
    }

    /**
     *
     *
     */
    public CollabSession createSession(Account account, String password)
    throws IOException, SecurityException, CollabException {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        // Check for existing session
        if (getSession(account) != null) {
            throw new IllegalStateException("Session already exists for account " + account);
        }

        CollaborationSessionFactory factory = null;
        CollaborationSession session = null;
        IMCollabSession jimSession = null;

        try {
            factory = getCollaborationSessionFactory(account);

            Debug.out.println("Logging in..."); // NOI18N

            // Create a bridge session object
            jimSession = new IMCollabSession(this, account);

            // Allocate a new session
            session = factory.getSession(getServerURL(account), account.getUserName() + "/ide", password, jimSession); // NOI18N

            // Attach the IM session to the bridge
            jimSession.attachSession(session);

            // Track the session
            addSession(jimSession);

            Debug.out.println("Logged in successfully"); // NOI18N

            return jimSession;
        } catch (AuthenticationException e) {
            try {
                if (factory != null) {
                    factory.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();

                // Ignore
                Debug.debugNotify(ex);
            }

            // Throw a nice exception
            SecurityException ex = new SecurityException("Authentication failed"); // NOI18N
            ex.initCause(e);
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (jimSession != null) {
                    jimSession.logout();
                }
            } catch (Exception ex) {
                // Ignore
                Debug.debugNotify(ex);
            }

            try {
                if (session != null) {
                    session.logout();
                }
            } catch (Exception ex) {
                // Ignore
                Debug.debugNotify(ex);
            }

            try {
                if (factory != null) {
                    factory.close();
                }
            } catch (Exception ex) {
                // Ignore
                Debug.debugNotify(ex);
            }

            // Workaround: AuthenticationException is being thrown inside
            // a CollaborationException
            if (e.getCause() instanceof AuthenticationException) {
                SecurityException ex = new SecurityException("Authentication failed"); // NOI18N
                ex.initCause(e.getCause());
                throw ex;
            } else if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else if (e.getCause() instanceof java.nio.channels.UnresolvedAddressException) {
                // Could not resolve hostname
                throw new UnknownHostException(account.getServer());
            } else if (
                (e.getCause() == null) && (e.getMessage() != null) &&
                    (e.getMessage().indexOf("java.net.ConnectException") != -1)
            ) // HACK // NOI18N
             {
                throw new java.net.ConnectException(e.getMessage());
            } else {
                Debug.logDebugException("create session failed", e, true);
                throw new CollabException(e.getCause(), e.getMessage());
            }
        }
    }

    /**
     * Returns a URL formatted according to the account's proxy connection
     * requirements
     *
     */
    protected String getServerURL(Account account) {
        String result = null;
        boolean usingProxy = false;

        switch (account.getProxyType()) {
        case Account.PROXY_HTTPS: {
            usingProxy = true;

            // If HTTPS tunneling is used, the URL should be of this format:
            // https://httphost:httpport?service=xmpphost:xmppport
            String server = account.getServer();

            if ((server == null) || (server.trim().length() == 0)) {
                throw new IllegalArgumentException("No server specified"); // NOI18N
            }

            // Append the default XMPP port.  This is necessary because the 
            // proxy will otherwise assume port 80 or 443.
            if (server.indexOf(":") == -1) { // NOI18N
                server += ":5222"; // NOI18N
            }

            result = "http://" + account.getProxyServer() + // NOI18N
                "?service=" + server; // NOI18N

            // Add keepalive
            String keepalive = System.getProperty(HTTPS_KEEPALIVE, DEFAULT_KEEPALIVE);

            if ((keepalive != null) && (keepalive.trim().length() > 0)) {
                result += ("&keepalive=" + keepalive); // NOI18N
            }

            break;
        }

        case Account.PROXY_SOCKS_5: {
            usingProxy = true;

            // If HTTPS tunneling is used, the URL should be of this format:
            // socks://sockshost:socksport?service=xmpphost:xmppport
            String server = account.getServer();

            if ((server == null) || (server.trim().length() == 0)) {
                throw new IllegalArgumentException("No server specified"); // NOI18N
            }

            // Append the default XMPP port.  This is necessary because the 
            // proxy will otherwise not know the correct port.
            if (server.indexOf(":") == -1) { // NOI18N
                server += ":5222"; // NOI18N
            }

            result = "socks://" + account.getProxyServer() + // NOI18N
                "?service=" + server; // NOI18N

            // Add keepalive; this would be unusual, but I'm adding just
            // in case
            String keepalive = System.getProperty(SOCKS_KEEPALIVE, DEFAULT_KEEPALIVE);

            if ((keepalive != null) && (keepalive.trim().length() > 0)) {
                result += ("&keepalive=" + keepalive); // NOI18N
            }

            break;
        }

        default:
        case Account.PROXY_NONE:
            result = account.getServer();
        }

        // Append proxy info
        if (usingProxy) {
            String proxyUsername = account.getProxyUserName();
            String proxyPassword = account.getProxyPassword();

            if ((proxyUsername != null) && (proxyUsername.trim().length() > 0)) {
                result += ("&authname=" + proxyUsername + // NOI18N
                "&password=" + proxyPassword); // NOI18N
            }
        }

        Debug.out.println("Connecting with URL: " + result); // NOI18N

        return result;
    }

    /**
     *
     *
     */
    public CollabSession getSession(Account account) {
        return (CollabSession) sessions.get(account);
    }

    /**
     *
     *
     */
    public CollabSession[] getSessions() {
        return (CollabSession[]) sessions.values().toArray(new CollabSession[sessions.size()]);
    }

    /**
     *
     *
     */
    protected synchronized void addSession(CollabSession session) {
        if (session == null) {
            return;
        }

        sessions.put(session.getAccount(), session);

        if (!suspendSessionChangeEvents) {
            getChangeSupport().firePropertyChange(PROP_SESSIONS, null, session);
        }
    }

    /**
     *
     *
     */
    protected synchronized void removeSession(CollabSession session) {
        if (session == null) {
            return;
        }

        sessions.remove(session.getAccount());

        if (!suspendSessionChangeEvents) {
            getChangeSupport().firePropertyChange(PROP_SESSIONS, session, null);
        }
    }

    /**
     *
     *
     */
    protected synchronized void suspendSessionChangeEvents(boolean value) {
        suspendSessionChangeEvents = value;
    }

    /**
     *
     *
     */
    protected CollaborationSessionFactory getCollaborationSessionFactory(Account account)
    throws CollabException {
        //		synchronized(this)
        //		{
        //			try
        //			{
        //				// Detect whether or not this is a proxied account; use the
        //				// HTTP-tunneling factory if so
        //				if (account.getServer().startsWith("http")) // NOI18N
        //				{
        //					if (proxySessionFactory==null)
        //					{
        //						Debug.out.println("Creating proxy collaboration "+
        //							"session factory");
        //						proxySessionFactory=new CollaborationSessionFactory(
        //							WEB_PROXY_SESSION_FACTORY_CLASS);
        //					}
        //
        //					return proxySessionFactory;
        //				}
        //				else
        //				{
        //					if (sessionFactory==null)
        //					{
        //						Debug.out.println("Creating collaboration " +
        //							"session factory");
        //						sessionFactory=new CollaborationSessionFactory();
        //					}
        //
        //					return sessionFactory;
        //				}
        //			}
        //			catch (Exception e)
        //			{
        //				CollabException ex=new CollabException(e,
        //					"Could not create CollaborationSessionFactory");
        //				throw ex;
        //			}
        //		}
        // TAF: once again, we must create a new factory each time due to an 
        // IM bug
        synchronized (this) {
            try {
                switch (account.getProxyType()) {
                case Account.PROXY_HTTPS:
                    Debug.out.println("Creating HTTP proxy " + // NOI18N
                        "collaboration session factory"
                    ); // NOI18N

                    return new CollaborationSessionFactory(PROXY_SESSION_FACTORY_CLASS);

                case Account.PROXY_SOCKS_5:
                    Debug.out.println("Creating SOCKS proxy " + // NOI18N
                        "collaboration session factory"
                    ); // NOI18N

                    return new CollaborationSessionFactory(PROXY_SESSION_FACTORY_CLASS);

                default:
                case Account.PROXY_NONE:
                    Debug.out.println("Creating standard " + // NOI18N
                        "collaboration session factory"
                    ); // NOI18N

                    return new CollaborationSessionFactory();
                }
            } catch (Exception e) {
                CollabException ex = new CollabException(e, "Could not create CollaborationSessionFactory"); // NOI18N
                throw ex;
            }
        }
    }

    /**
     *
     *
     */
    public void unregisterUser(Account account) throws IOException, SecurityException, CollabException {
        CollaborationSessionFactory factory = null;
        CollaborationSession session = null;
        IMCollabSession jimSession = null;

        Debug.out.println(" unregister account");

        CollabSession collabSession = getSession(account);

        try {
            if (collabSession != null) {
                jimSession = (IMCollabSession) collabSession;
                session = jimSession.getCollaborationSession();
            } else // attempt to login to get a session
             {
                factory = getCollaborationSessionFactory(account);

                // Create a bridge session object
                jimSession = new IMCollabSession(this, account);

                // Allocate a new session
                session = factory.getSession(
                        getServerURL(account), account.getUserName() + "/ide", account.getPassword(), jimSession
                    ); // NOI18N

                Debug.out.println("Logged in successfully for deleting account"); // NOI18N
            }

            RegistrationListener listener = new Registration(account);

            session.unregister(listener);
        } catch (AuthenticationException e) {
            try {
                if (factory != null) {
                    factory.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();

                // Ignore
                Debug.debugNotify(ex);
            }

            // Throw a nice exception
            SecurityException ex = new SecurityException("Authentication failed"); // NOI18N
            ex.initCause(e);
            throw ex;
        } catch (Exception e) {
            //			e.printStackTrace();
            try {
                if (session != null) {
                    session.logout();
                }
            } catch (Exception ex) {
                // Ignore
                Debug.debugNotify(ex);
            }

            try {
                if (factory != null) {
                    factory.close();
                }
            } catch (Exception ex) {
                // Ignore
                Debug.debugNotify(ex);
            }

            // Workaround: AuthenticationException is being thrown inside
            // a CollaborationException
            if (e.getCause() instanceof AuthenticationException) {
                SecurityException ex = new SecurityException("Authentication failed"); // NOI18N
                ex.initCause(e.getCause());
                throw ex;
            } else if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else if (e.getCause() instanceof java.nio.channels.UnresolvedAddressException) {
                // Could not resolve hostname
                throw new UnknownHostException(account.getServer());
            } else if (
                (e.getCause() == null) && (e.getMessage() != null) &&
                    (e.getMessage().indexOf("java.net.ConnectException") != -1)
            ) // HACK // NOI18N
             {
                throw new java.net.ConnectException(e.getMessage());
            } else {
                Debug.logDebugException("create session failed", e, true);
                throw new CollabException(e.getCause(), e.getMessage());
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected static class Registration extends Object implements RegistrationListener {
        private Account account;
        private String newPassword;

        /**
         *
         *
         */
        public Registration(Account account) {
            super();
            this.account = account;
        }

        /**
         *
         *
         */
        public Registration(Account account, String newPassword) {
            super();
            this.account = account;
            this.newPassword = newPassword;
        }

        /**
         *
         *
         */
        public Account getAccount() {
            return account;
        }

        /**
         *
         *
         */
        public boolean fillRegistrationInformation(Map fields, String server) {
            fields.put(RegistrationListener.FIRST, getAccount().getFirstName());
            fields.put(RegistrationListener.LAST, getAccount().getLastName());
            fields.put(RegistrationListener.EMAIL, getAccount().getEmail());
            fields.put(RegistrationListener.NAME, getAccount().getDisplayName());
            fields.put(RegistrationListener.PASSWORD, getAccount().getPassword());
            fields.put(RegistrationListener.USERNAME, getAccount().getUserName());

            return true;
        }

        /**
         *
         *
         */
        public void redirected(java.net.URL url, String server) {
            // TODO: Notify user that he/she must use the browser to register
            Debug.out.println("Redirecting registration to " + url);

            // Open the browser to the location
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }

        /**
         *
         *
         */
        public void registered(String server) {
            Debug.out.println("User was successfully registered on " + server);

            try {
                // Notify the user
                CollabManager.getDefault().getUserInterface().notifyAccountRegistrationSucceeded(getAccount());

                // Add the account to the list
                AccountManager.getDefault().addAccount(getAccount());

                Account defaultAccount = CollabManager.getDefault().getUserInterface().getDefaultAccount();

                if (defaultAccount == null) {
                    CollabManager.getDefault().getUserInterface().setDefaultAccount(getAccount());
                }
            } catch (IOException ex) {
                Debug.errorManager.notify(ex);
            }
        }

        /**
         *
         *
         */
        public void registrationFailed(String errorCondition, String errorText, String server) {
            // TODO: Proper error notification
            Debug.out.println("Registration failed with error condition " + errorCondition + "; message: " + errorText);

            // Try to find a reason message
            String reason = NbBundle.getMessage(
                    IMCollabManager.class, "MSG_IMCollabManager_RegisrationFailed_" + errorCondition, server,
                    account.getUserName(), errorText
                );

            if (reason == null) {
                reason = errorText;
            }

            // Notify the user
            //			CollabManager.getDefault().getUserInterface()
            //				.notifyAccountRegistrationFailed(getAccount(),reason);
            // Retry creating the account
            CollabManager.getDefault().getUserInterface().createNewAccount(getAccount(), reason);
        }

        /**
         *
         *
         */
        public boolean userAlreadyRegistered(String user, String server) {
            // TODO: Proper error notification
            Debug.out.println("User " + user + "is already registered with " + "the server " + server);

            try {
                AccountManager.getDefault().addAccount(getAccount());
            } catch (IOException ex) {
                // TO DO
                Debug.errorManager.notify(ex);
            }

            return true;
        }

        /**
         *
         *
         */
        public void passwordChanged(String user, String server) {
            // there is a bug in IM API, this callback is never invoked, it uses
            // registrationUpdated instead
            // TODO: Proper notification
            Debug.out.println("Password for " + user + " has been successfully changed by the " + server);

            //		  
            //			// modify the account
            //			getAccount().setPassword(newPassword);
            //			Debug.out.println(" after setting client account");
            //				// Notify the user
            //			CollabManager.getDefault().getUserInterface()
            //				.notifyAccountPasswordChangeSucceeded(getAccount());
        }

        /**
         *
         *
         */
        public void unregistrationFailed(String errorCondition, String errorText, String server) {
            Debug.out.println("UnRegistration failed with error condition " + errorCondition + "message: " + errorText);

            String reason = NbBundle.getMessage(
                    IMCollabManager.class, "MSG_IMCollabManager_unregistrationFailed_" + errorCondition, server,
                    account.getUserName(), errorText
                );

            // Notify the user
            CollabManager.getDefault().getUserInterface().notifyAccountUnRegistrationFailed(getAccount(), reason);
        }

        /**
         *
         *
         */
        public void unregistered(String server) {
            Debug.out.println("User was successfully unregistered on " + server);

            if (CollabManager.getDefault().getSession(getAccount()) != null) {
                CollabManager.getDefault().getSession(getAccount()).logout();
            }

            try {
                // Notify the user
                CollabManager.getDefault().getUserInterface().notifyAccountUnRegistrationSucceeded(getAccount());

                // remove the account
                AccountManager.getDefault().removeAccount(account);
            } catch (IOException ex) {
                Debug.errorManager.notify(ex);
            }
        }

        /**
         *
         *
         */
        public void registrationUpdated(String server) {
            Debug.out.println("Password has been successfully changed by the " + server);

            getAccount().setPassword(newPassword);

            // Notify the user
            CollabManager.getDefault().getUserInterface().notifyAccountPasswordChangeSucceeded(getAccount());
        }

        /**
         *
         *
         */
        public void registrationUpdateFailed(String errorCondition, String errorText, String server) {
            Debug.out.println(
                "Registration update has failed with " + "error condition " + errorCondition + " and error message: " +
                errorText
            );

            String reason = NbBundle.getMessage(
                    IMCollabManager.class, "MSG_IMCollabManager_registrationUpdateFailed_" + errorCondition, server,
                    account.getUserName(), errorText
                );

            CollabManager.getDefault().getUserInterface().notifyAccountPasswordChangeFailed(getAccount(), reason);
        }
    }
    
    protected IMReconnect getReconnect() {
        return reconnect;
    }
    
}
