/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.httpserver;

import java.awt.Dialog;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Properties;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.event.EventListenerList;
import org.openide.DialogDescriptor;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

/** Options for http server
*
* @author Ales Novak, Petr Jiricka
*/
public class HttpServerSettings extends SystemOption {

    private static final int MAX_START_RETRIES = 20;
    private static int currentRetries = 0;

    protected static EventListenerList listenerList = new EventListenerList();

    /** Has this been initialized ?
    *  Becomes true if a "running" getter or setter is called
    */
    static boolean inited = false;

    /** Contains threads which are or will be asking for access for the given IP address. */
    private static Hashtable /* InetAddress -> Thread */ whoAsking = new Hashtable();

    public static final int SERVER_STARTUP_TIMEOUT = 3000;

    /** constant for local host */
    public  static final String LOCALHOST = "local"; // NOI18N
    /** constant for any host */
    public static final String ANYHOST = "any"; // NOI18N

    public static final HostProperty hostProperty = new HostProperty("", LOCALHOST);
    
    public static final String PROP_PORT               = "port"; // NOI18N
    public static final String PROP_HOST_PROPERTY      = "hostProperty"; // NOI18N
    static final String PROP_WRAPPER_BASEURL    = "wrapperBaseURL"; // NOI18N
    public static final String PROP_RUNNING            = "running"; // NOI18N

    private static final String PROP_SHOW_GRANT_ACCESS  = "showGrantAccess"; // NOI18N

    /** port */
     private static final int DEFAULT_PORT = 8082;

    /** mapping of wrapper to URL */
    private static String wrapperBaseURL = "/resource/"; // NOI18N
    
    /** Reflects whether the server is actually running, not the running property */
    static boolean running = false;

    private static boolean startStopMessages = true;

    private static Properties mappedServlets = new Properties();

    /** http settings
     * @deprecated use <CODE>SharedClassObject.findObject()</CODE>
     */
    public static HttpServerSettings OPTIONS = null;

    /** Lock for the httpserver operations */
    private static Object httpLock;
    
    /** Used to remember the state of the running property during the deserialization */
    static final long serialVersionUID =7387407495740535307L;
    
    /**
     * Obtains lock for httpserver synchronization
     */
    static final Object httpLock () {
        if (httpLock == null) {
            httpLock = new Object ();
        }
        return httpLock;
    }

    public HttpServerSettings() {
    }

    protected void initialize () {
        super.initialize ();
    }
    
    /** human presentable name */
    public String displayName() {
        return NbBundle.getMessage(HttpServerSettings.class, "CTL_HTTP_settings");
    }

    /** getter for running status */
    public boolean isRunning() {
        if (isWriteExternal()) {
            if (inited) return running;
            else        return true;
        }
        if (inited) {
            return running;
        }
        else {
            // this used to be true, but it seems more reasonable not to start the server by default
            // Fixes bug 11347
            setRunning(false);
            return running;
        }
    }

    /** Intended to be called by the thread which succeeded to start the server */
    void runSuccess() {
        synchronized (httpLock ()) {
            currentRetries = 0;
            running = true;
            httpLock ().notifyAll();
        }
    }

    /** Intended to be called by the thread which failed to start the server. 
     * It decides whether try to start server on next port or show appropriate
     * error message.
     */
    void runFailure(Throwable t) {
        running = false;
        if (t instanceof IncompatibleClassChangeError) {
            // likely there is a wrong servlet API version on CLASSPATH
            DialogDisplayer.getDefault ().notify(new NotifyDescriptor.Message(
               NbBundle.getMessage (HttpServerSettings.class, "MSG_HTTP_SERVER_incompatbleClasses"),
               NotifyDescriptor.Message.WARNING_MESSAGE));
        }
        else if (t instanceof java.net.BindException) {
            // can't open socket - we can retry
            currentRetries ++;
            if (currentRetries <= MAX_START_RETRIES) {
                setPort(getPort() + 1);
                setRunning(true);
            }
            else {
                currentRetries = 0;
                DialogDisplayer.getDefault ().notify(new NotifyDescriptor.Message(
                                               NbBundle.getMessage (HttpServerSettings.class, "MSG_HTTP_SERVER_START_FAIL"),
                                               NotifyDescriptor.Message.WARNING_MESSAGE));
                int p = getPort ();
                if (p < 1024 && inited && Utilities.isUnix()) {
                    DialogDisplayer.getDefault ().notify(new NotifyDescriptor.Message(
                                               NbBundle.getMessage (HttpServerSettings.class, "MSG_onlyRootOnUnix"),
                                               NotifyDescriptor.WARNING_MESSAGE));
                }

            }
        }
        else {
            // unknown problem
            DialogDisplayer.getDefault ().notify(new NotifyDescriptor.Message(
               NbBundle.getMessage (HttpServerSettings.class, "MSG_HTTP_SERVER_START_FAIL_unknown"),
               NotifyDescriptor.Message.WARNING_MESSAGE));
        }
    }

    /** Restarts the server if it is running - must be called in a synchronized block 
     *  No need to restart if it is called during deserialization.
     */
    private void restartIfNecessary(boolean printMessages) {
        if (isReadExternal ()) {
            return;
        }
        if (running) {
            if (!printMessages)
                setStartStopMessages(false);
            HttpServerModule.stopHTTPServer();
            HttpServerModule.initHTTPServer();
            // messages will be enabled by the server thread
        }
    }

    /** Returns a relative directory URL with a leading and a trailing slash */
    private String getCanonicalRelativeURL(String url) {
        String newURL;
        if (url.length() == 0)
            newURL = "/";   // NOI18N
        else {
            if (url.charAt(0) != '/')
                newURL = "/" + url; // NOI18N
            else
                newURL = url;
            if (newURL.charAt(newURL.length() - 1) != '/')
                newURL = newURL + "/";  // NOI18N
        }
        return newURL;
    }

    /** setter for running status */
    public void setRunning(boolean running) {
        if (isReadExternal()) {
            // just for deserialization, do not start
            return;
        }
        inited = true;
        if (this.running == running)
            return;

        synchronized (httpLock ()) {
            if (running) {
                // running status is set by another thread
                HttpServerModule.initHTTPServer();
            }
            else {
                this.running = false;
                HttpServerModule.stopHTTPServer();
            }
        }
        firePropertyChange(PROP_RUNNING, !running ? Boolean.TRUE : Boolean.FALSE, running ? Boolean.TRUE : Boolean.FALSE);
    }

    // NOT publicly available
    
    /** getter for classpath base */
    String getWrapperBaseURL() {
        return wrapperBaseURL;
    }

    /** setter for classpath base */
    void setWrapperBaseURL(String wrapperBaseURL) {
        // canonical form starts and ends with a /
        String oldURL;
        String newURL = getCanonicalRelativeURL(wrapperBaseURL);

        // check if any change is taking place
        if (this.wrapperBaseURL.equals(newURL))
            return;

        // implement the change
        synchronized (httpLock ()) {
            oldURL = this.wrapperBaseURL;
            this.wrapperBaseURL = newURL;
            restartIfNecessary(false);
        }
        firePropertyChange(PROP_WRAPPER_BASEURL, oldURL, this.wrapperBaseURL);
    }

    /** setter for port */
    public void setPort(int p) {
        if (p <= 0 || p >65535) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                        NbBundle.getMessage(HttpServerSettings.class, "ERR_PortNumberOutOfRange", new Integer(p)), NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        
        Object old = getProperty(PROP_PORT);
        int port = ((old == null) ? DEFAULT_PORT : ((Integer)old).intValue());
        
        synchronized (httpLock ()) {
            old = putProperty(PROP_PORT, new Integer(p), false);
            if (old != null) {
                if (p == ((Integer)old).intValue())
                    return;
            }
            restartIfNecessary(true);
        }
        firePropertyChange(PROP_PORT, old, new Integer(p));
    }

    /** getter for port */
    public int getPort() {
        Object prop = getProperty(PROP_PORT);
        return ((prop == null) ? DEFAULT_PORT : ((Integer)prop).intValue());
    }

    public void setStartStopMessages(boolean ssm) {
        startStopMessages = ssm;
    }

    public boolean isStartStopMessages() {
        return startStopMessages;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HttpServerSettings.class);
    }

    /* Access the firePropertyChange from HTTPServer (which holds the enabled prop). */
    void firePropertyChange0 (String name, Object oldVal, Object newVal) {
        firePropertyChange (name, oldVal, newVal);
    }

    /** Returns string for localhost */
    private String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            return "localhost"; // NOI18N
        }
    }

    public void addGrantAccessListener(GrantAccessListener l) {
        listenerList.add(GrantAccessListener.class, l);
    }

    public void removeGrantAccessListener(GrantAccessListener l) {
        listenerList.remove(GrantAccessListener.class, l);
    }

    /** Returns true if oneof the listeners allowed access */
    protected boolean fireGrantAccessEvent(InetAddress clientAddress, String resource) {
        Object[] listeners = listenerList.getListenerList();
        GrantAccessEvent grantAccessEvent = null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==GrantAccessListener.class) {
                if (grantAccessEvent == null)
                    grantAccessEvent = new GrantAccessEvent(this, clientAddress, resource);
                ((GrantAccessListener)listeners[i+1]).grantAccess(grantAccessEvent);
            }
        }
        return (grantAccessEvent == null) ? false : grantAccessEvent.isGranted();
    }

    /** Requests access for address addr. If necessary asks the user. Returns true it the access
    * has been granted. */  
    boolean allowAccess(InetAddress addr, String requestPath) {
        if (accessAllowedNow(addr, requestPath))
            return true;

        Thread askThread = null;
        synchronized (whoAsking) {
            // one more test in the synchronized block
            if (accessAllowedNow(addr, requestPath))
                return true;

            askThread = (Thread)whoAsking.get(addr);
            if (askThread == null) {
                askThread = Thread.currentThread();
                whoAsking.put(addr, askThread);
            }
        }

        // now ask the user
        synchronized (HttpServerSettings.class) {
            if (askThread != Thread.currentThread()) {
                return accessAllowedNow(addr, requestPath);
            }

            try {
                if (!isShowGrantAccessDialog ())
                    return false;
                
                String msg = NbBundle.getMessage (HttpServerSettings.class, "MSG_AddAddress", addr.getHostAddress ());
                
                final GrantAccessPanel panel = new GrantAccessPanel (msg);
                DialogDescriptor descriptor = new DialogDescriptor (
                    panel,
                    NbBundle.getMessage (HttpServerSettings.class, "CTL_GrantAccessTitle"),
                    true,
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.NO_OPTION,
                    null
                );
                descriptor.setMessageType (NotifyDescriptor.QUESTION_MESSAGE);
                // descriptor.setOptionsAlign (DialogDescriptor.BOTTOM_ALIGN);
                final Dialog d  = DialogDisplayer.getDefault ().createDialog (descriptor);
                d.setSize (580, 180);
                d.setVisible(true);

                setShowGrantAccessDialog (panel.getShowDialog ());
                if (NotifyDescriptor.YES_OPTION.equals(descriptor.getValue ())) {
                    appendAddressToGranted(addr.getHostAddress());
                    return true;
                }
                else
                    return false;
            }
            finally {
                whoAsking.remove(addr);
            }
        } // end synchronized
    }

    /** Checks whether access to the server is now allowed. */
    private boolean accessAllowedNow(InetAddress addr, String resource) {
        if (hostProperty.getHost().equals(HttpServerSettings.ANYHOST))
            return true;

        HashSet hs = getGrantedAddressesSet();
        if (hs.contains(addr.getHostAddress()))
            return true;

        if (fireGrantAccessEvent(addr, resource))
            return true;

        return false;
    }

    /** Appends the address to the list of addresses which have been granted access. */
    private void appendAddressToGranted(String addr) {
        synchronized (httpLock ()) {
            String granted = hostProperty.getGrantedAddresses().trim();
            if ((granted.length() > 0) &&
                    (granted.charAt(granted.length() - 1) != ';') &&
                    (granted.charAt(granted.length() - 1) != ','))
                granted += ',';
            granted += addr;
            hostProperty.setGrantedAddresses(granted);
        }
    }

    /** Returns a list of addresses which have been granted access to the web server,
    * including the localhost. Addresses are represented as strings. */
    HashSet getGrantedAddressesSet() {
        HashSet addr = new HashSet();
        try {
            addr.add(InetAddress.getByName("localhost").getHostAddress()); // NOI18N
            addr.add(InetAddress.getLocalHost().getHostAddress());
        }
        catch (UnknownHostException e) {}
        StringTokenizer st = new StringTokenizer(hostProperty.getGrantedAddresses(), ",;"); // NOI18N
        while (st.hasMoreTokens()) {
            String ipa = st.nextToken();
            ipa = ipa.trim();
            try {
                addr.add(InetAddress.getByName(ipa).getHostAddress());
            }
            catch (UnknownHostException e) {}
        }
        return addr;
    }

    Properties getMappedServlets() {
        return mappedServlets;
    }

    /** Converts string into string that is usable in URL. 
     *  This mangling changes some characters
     */
    static String mangle (String name) {
        StringBuffer sb = new StringBuffer ();
        for (int i = 0; i < name.length (); i++) {
            if (Character.isLetterOrDigit (name.charAt (i)) ||
                name.charAt (i) == '.') {
                sb.append (name.charAt (i));
            }
            else {
                String code = Integer.toHexString ((int)name.charAt (i)).toUpperCase ();
                if (code.length ()<2)
                    code = (code.length () == 0)? "00": "0"+code;   // NOI18N
                sb.append ("%").                                    // NOI18N
                append ((code.length () == 2)? code: code.substring (code.length ()-2));
            }
        }
        return sb.toString ();
    }
    
    /** Unconverts string from URL into old string. 
     *  This mangling decodes '%xy'
     */
    static String demangle (String name) {
        StringBuffer sb = new StringBuffer ();
        try {
            for (int i = 0; i < name.length (); i++) {
                if (name.charAt (i) != '%') {
                    sb.append (name.charAt (i));
                }
                else {
                    sb.append ((char)Integer.parseInt (name.substring (i+1, i+3), 16));
                    i += 2;
                }
            }
        }
        catch (NumberFormatException ex) {
            ex.printStackTrace ();
            return "";  // NOI18N
        }
        return sb.toString ();
    }
    
    /** Getter for property hostProperty.
     * @return Value of property hostProperty.
     */
    public HttpServerSettings.HostProperty getHostProperty () {
        return hostProperty;
    }
    
    /** Setter for property hostProperty.
     * @param hostProperty New value of property hostProperty.
     */
    public void setHostProperty (HttpServerSettings.HostProperty hostProperty) {
        if (ANYHOST.equals(hostProperty.getHost ()) || LOCALHOST.equals(hostProperty.getHost ())) {
            firePropertyChange(PROP_HOST_PROPERTY, null, hostProperty);
        }
    }
    
    public boolean isShowGrantAccessDialog () {
        Boolean b = (Boolean)getProperty (PROP_SHOW_GRANT_ACCESS);
        if (b != null) 
            return b.booleanValue ();
        else
            return true;
    }
    
    public void setShowGrantAccessDialog (boolean show) {
        putProperty (PROP_SHOW_GRANT_ACCESS, show ? Boolean.TRUE : Boolean.FALSE, true);
    }
    
    /** Property value that describes set of host with granted access
     */
    public static class HostProperty implements java.io.Serializable {
        
        private String grantedAddresses;
        
        private String host;
        
        private static final long serialVersionUID = 1927848926692414249L;
        
        HostProperty (String grantedAddresses, String host) {
            this.grantedAddresses = grantedAddresses;
            this.host = host;
        }
        
        /** Getter for property host.
         * @return Value of property host.
         */
        public String getHost () {
            return host;
        }
        
        /** Setter for property host.
         * @param host New value of property host.
         */
        public void setHost (String host) {
            this.host = host;
        }
        
        /** Getter for property grantedAddresses.
         * @return Value of property grantedAddresses.
         */
        public String getGrantedAddresses () {
            return grantedAddresses;
        }
        
        /** Setter for property grantedAddresses.
         * @param grantedAddresses New value of property grantedAddresses.
         */
        public void setGrantedAddresses (String grantedAddresses) {
            this.grantedAddresses = grantedAddresses;
        }
        
    }
    
}
