/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.httpserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInput;
import java.util.ResourceBundle;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Properties;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import javax.swing.event.EventListenerList;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.HttpServer;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

/** Options for http server
*
* @author Ales Novak, Petr Jiricka
* @version 0.12, May 5, 1999
*/
public class HttpServerSettings extends SystemOption implements HttpServer.Impl {

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

    public static final String PROP_PORT               = "port"; // NOI18N
    public static final String PROP_HOST_PROPERTY      = "hostProperty"; // NOI18N
    public static final String PROP_REPOSITORY_BASEURL = "repositoryBaseURL"; // NOI18N
    public static final String PROP_CLASSPATH_BASEURL  = "classpathBaseURL"; // NOI18N
    public static final String PROP_JAVADOC_BASEURL    = "javadocBaseURL"; // NOI18N
           static final String PROP_WRAPPER_BASEURL    = "wrapperBaseURL"; // NOI18N
    public static final String PROP_RUNNING            = "running"; // NOI18N

    private static final String PROP_HOST               = "host"; // NOI18N
    private static final String PROP_GRANTED_ADDRESSES  = "grantedAddresses"; // NOI18N

    /** port */
    //  private static int port = 8082; //8080
    private static final int DEFAULT_PORT = 8082;

    /** allowed connections hosts - local/any */
    private static String host = LOCALHOST;

    /** mapping of repository to URL */
    private static String repositoryBaseURL = "/repository/"; // NOI18N

    /** mapping of classpath to URL */
    private static String classpathBaseURL = "/classpath/"; // NOI18N

    /** mapping of javadoc to URL */
    private static String javadocBaseURL = "/javadoc/"; // NOI18N
    
    /** mapping of wrapper to URL */
    private static String wrapperBaseURL = "/resource/"; // NOI18N
    
    /** addresses which have been granted access to the web server */
    private static String grantedAddresses = ""; // NOI18N

    /** Reflects whether the server is actually running, not the running property */
    static boolean running = false;

    private static boolean startStopMessages = true;

    private static Properties mappedServlets = new Properties();

    /** http settings */
    // public static HttpServerSettings OPTIONS = (HttpServerSettings)findObject (HttpServerSettings.class, true);
    public static HttpServerSettings OPTIONS = new HttpServerSettings();

    /** last used servlet name */
    private static int lastUsedName = 0;
    /** map names of servlets to paths */
    private static HashMap nameMap = new HashMap();

    /** Used to remember the state of the running property during the deserialization */
    private boolean pendingRunning = true;

    /** Used to remember that server should be restarted after the deserialization */
    private boolean pendingRestart = false;

    static final long serialVersionUID =7387407495740535307L;

    public HttpServerSettings() {
    }

    /** This is a project option. */
    private boolean isGlobal() {
        return false;
    }

    /** human presentable name */
    public String displayName() {
        return NbBundle.getBundle(HttpServerSettings.class).getString("CTL_HTTP_settings");
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
        synchronized (HttpServerSettings.OPTIONS) {
            currentRetries = 0;
            running = true;
            HttpServerSettings.OPTIONS.notifyAll();
        }
    }

    /** Intended to be called by the thread which failed to start the server */
    void runFailure() {
        running = false;
        currentRetries ++;
        if (currentRetries <= MAX_START_RETRIES) {
            setPort(getPort() + 1);
            setRunning(true);
        }
        else {
            currentRetries = 0;
            TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                               NbBundle.getBundle(HttpServerSettings.class).getString("MSG_HTTP_SERVER_START_FAIL"), // NOI18N
                                               NotifyDescriptor.Message.WARNING_MESSAGE));
            int p = getPort ();
            if (p < 1024 && inited && Utilities.isUnix()) {
                TopManager.getDefault().notify(new NotifyDescriptor.Message(
                                               NbBundle.getBundle(HttpServerSettings.class).getString("MSG_onlyRootOnUnix"), // NOI18N
                                               NotifyDescriptor.WARNING_MESSAGE));
            }

        }
    }

    /** Restarts the server if it is running - must be called in a synchronized block */
    private void restartIfNecessary(boolean printMessages) {
        if (isReadExternal () && (running || pendingRunning)) {
            pendingRestart = true;
            return;
        }
        if (running) {
            if (!printMessages)
                setStartStopMessages(false);
            HttpServerModule.stopHTTPServer();
            HttpServerModule.initHTTPServer();
            // messages will be enabled by the server thread
        }
        pendingRestart = false;
    }

    /** Reads from the serialized state */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        setRunning(pendingRunning);
        if (pendingRestart && (pendingRunning == running))
            restartIfNecessary (false);
        else
            setRunning(pendingRunning);
    }

    /** Returns a relative directory URL with a leading and a trailing slash */
    /*  private String getCanonicalRelativeURL(String url) {
        String newURL;
        if (url.length() == 0)
          newURL = ""; // NOI18N
        else {
          if (url.charAt(0) != '/')
            newURL = "/" + url; // NOI18N
          else
            newURL = url;
          if (newURL.charAt(newURL.length() - 1) == '/')
            newURL = newURL.substring(0, newURL.length() - 1);
        }      
        return newURL;                               
      }*/

    /** Returns a relative directory URL with a leading and a trailing slash */
    private String getCanonicalRelativeURL(String url) {
        String newURL;
        if (url.length() == 0)
            newURL = "/";
        else {
            if (url.charAt(0) != '/')
                newURL = "/" + url;
            else
                newURL = url;
            if (newURL.charAt(newURL.length() - 1) != '/')
                newURL = newURL + "/";
        }
        return newURL;
    }

    /** setter for running status */
    public void setRunning(boolean running) {
        if (isReadExternal()) {
            pendingRunning = running;
            return;
        }
        inited = true;
        if (this.running == running)
            return;

        synchronized (HttpServerSettings.OPTIONS) {
            if (running) {
                // running status is set by another thread
                HttpServerModule.initHTTPServer();
            }
            else {
                this.running = false;
                HttpServerModule.stopHTTPServer();
            }
        }
        firePropertyChange(PROP_RUNNING, new Boolean(!running), new Boolean(running));
    }

    /** getter for repository base */
    public String getRepositoryBaseURL() {
        return repositoryBaseURL;
    }

    /** setter for repository base */
    public void setRepositoryBaseURL(String repositoryBaseURL) {
        // canonical form starts and ends with a /
        String newURL = getCanonicalRelativeURL(repositoryBaseURL);

        // check if any change is taking place
        if (this.repositoryBaseURL.equals(newURL))
            return;

        // implement the change
        synchronized (HttpServerSettings.OPTIONS) {
            this.repositoryBaseURL = newURL;
            restartIfNecessary(false);
        }
        firePropertyChange(PROP_REPOSITORY_BASEURL, null, this.repositoryBaseURL);
    }

    /** getter for classpath base */
    public String getClasspathBaseURL() {
        return classpathBaseURL;
    }

    /** setter for classpath base */
    public void setClasspathBaseURL(String classpathBaseURL) {
        // canonical form starts and ends with a /
        String newURL = getCanonicalRelativeURL(classpathBaseURL);

        // check if any change is taking place
        if (this.classpathBaseURL.equals(newURL))
            return;

        // implement the change
        synchronized (HttpServerSettings.OPTIONS) {
            this.classpathBaseURL = newURL;
            restartIfNecessary(false);
        }
        firePropertyChange(PROP_CLASSPATH_BASEURL, null, this.classpathBaseURL);
    }

    /** getter for classpath base */
    public String getJavadocBaseURL() {
        return javadocBaseURL;
    }

    /** setter for classpath base */
    public void setJavadocBaseURL(String javadocBaseURL) {
        // canonical form starts and ends with a /
        String oldURL;
        String newURL = getCanonicalRelativeURL(javadocBaseURL);

        // check if any change is taking place
        if (this.javadocBaseURL.equals(newURL))
            return;

        // implement the change
        synchronized (HttpServerSettings.OPTIONS) {
            oldURL = this.javadocBaseURL;
            this.javadocBaseURL = newURL;
            restartIfNecessary(false);
        }
        firePropertyChange(PROP_JAVADOC_BASEURL, oldURL, this.javadocBaseURL);
    }

    /** Maps a file object to a URL. Should ensure that the file object is accessible on the given URL. */
    public URL getJavadocURL(FileObject fo) throws MalformedURLException, UnknownHostException {
        try {
            setRunning(true);
            return new URL("http", getLocalHost(), getPort(), // NOI18N
                           getJavadocBaseURL() + mangle (fo.getFileSystem ().getDisplayName ()) + "/" + 
                           fo.getPackageNameExt('/','.')); // NOI18N
        }
        catch (org.openide.filesystems.FileStateInvalidException ex) {
            throw new MalformedURLException ();
        }
    }

    // NOT publicly available
    
    /** getter for classpath base */
    String getWrapperBaseURL() {
        setRunning (true);
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
        synchronized (HttpServerSettings.OPTIONS) {
            oldURL = this.wrapperBaseURL;
            this.wrapperBaseURL = newURL;
            restartIfNecessary(false);
        }
        firePropertyChange(PROP_WRAPPER_BASEURL, oldURL, this.wrapperBaseURL);
    }

    /** Getter for grantedAddresses property */
    private String getGrantedAddresses() {
        return grantedAddresses;
    }

    /** Setter for grantedAccesses property */
    public void setGrantedAddresses(String grantedAddresses) {
        this.grantedAddresses = grantedAddresses;
        firePropertyChange(PROP_HOST_PROPERTY, null, this.grantedAddresses);
    }

    /** setter for port */
    public void setPort(int p) {
        Object old = getProperty(PROP_PORT);
        int port = ((old == null) ? DEFAULT_PORT : ((Integer)old).intValue());
        
        synchronized (HttpServerSettings.OPTIONS) {
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

    /** setter for host */
    public void setHost(String h) {
        if (h.equals(ANYHOST) || h.equals(LOCALHOST))
            host = h;
        firePropertyChange(PROP_HOST_PROPERTY, null, this.host);
    }

    /** getter for host */
    private String getHost() {
        return host;
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

    /* Implementation of HttpServer.Impl interface */

    /** Maps a file object to a URL. Should ensure that the file object is accessible on the given URL. */
    public URL getRepositoryURL(FileObject fo) throws MalformedURLException, UnknownHostException {
        setRunning(true);
        return new URL("http", getLocalHost(), getPort(), // NOI18N
                       getRepositoryBaseURL() + fo.getPackageNameExt('/','.')); // NOI18N
    }

    /** Maps the repository root to a URL. This URL should serve a page from which repository objects are accessible. */
    public URL getRepositoryRoot() throws MalformedURLException, UnknownHostException {
        setRunning(true);
        return new URL("http", getLocalHost(), getPort(), getRepositoryBaseURL()); // NOI18N
    }

    /** Maps a resource path to a URL. Should ensure that the resource is accessible on the given URL.
    * @param resourcePath path of the resource in the classloader format
    * @see ClassLoader#getResource(java.lang.String)
    * @see TopManager#systemClassLoader()
    */
    public URL getResourceURL(String resourcePath) throws MalformedURLException, UnknownHostException {
        setRunning(true);
        return new URL("http", getLocalHost(), getPort(), getClasspathBaseURL() + // NOI18N
                       (resourcePath.startsWith("/") ? 
                        resourcePath.substring(1) : 
                        resourcePath)); // NOI18N
    }

    /** Maps a resource root to a URL. Should ensure that all resources under the root are accessible under an URL
    * consisting of the returned URL and fully qualified resource name.
    * @param resourcePath path of the resource in the classloader format
    * @see ClassLoader#getResource(java.lang.String)
    * @see TopManager#systemClassLoader()
    */
    public URL getResourceRoot() throws MalformedURLException, UnknownHostException {
        setRunning(true);
        return new URL("http", getLocalHost(), getPort(), getClasspathBaseURL()); // NOI18N
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

    public boolean allowAccess(InetAddress addr) {
        throw new UnsupportedOperationException();
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
                MessageFormat format = new MessageFormat(NbBundle.getBundle(HttpServerSettings.class).getString("MSG_AddAddress"));
                String msg = format.format(new Object[] { addr.getHostAddress() });
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
                Object ret = TopManager.getDefault().notify(nd);

                if (NotifyDescriptor.YES_OPTION.equals(ret)) {
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
        if (getHost().equals(HttpServerSettings.ANYHOST))
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
        synchronized (HttpServerSettings.OPTIONS) {
            String granted = getGrantedAddresses().trim();
            if ((granted.length() > 0) &&
                    (granted.charAt(granted.length() - 1) != ';') &&
                    (granted.charAt(granted.length() - 1) != ','))
                granted += ',';
            granted += addr;
            setGrantedAddresses(granted);
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
        StringTokenizer st = new StringTokenizer(getGrantedAddresses(), ",;"); // NOI18N
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
                    code = (code.length () == 0)? "00": "0"+code;
                sb.append ("%").
                append ((code.length () == 2)? code: code.substring (code.length ()-2));
            }
        }
// System.out.println("mangling "+name+" to "+sb.toString ()+".");
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
            return "";
        }
// System.out.println("demangling "+name+" to "+sb.toString ()+".");
        return sb.toString ();
    }
    
    /** Getter for property hostProperty.
     * @return Value of property hostProperty.
     */
    public HttpServerSettings.HostProperty getHostProperty () {
        return new HttpServerSettings.HostProperty (grantedAddresses, host);
    }
    
    /** Setter for property hostProperty.
     * @param hostProperty New value of property hostProperty.
     */
    public void setHostProperty (HttpServerSettings.HostProperty hostProperty) {
        if (ANYHOST.equals(hostProperty.getHost ()) || LOCALHOST.equals(hostProperty.getHost ())) {
            grantedAddresses = hostProperty.getGrantedAddresses ();
            host = hostProperty.getHost ();
            firePropertyChange(PROP_HOST_PROPERTY, null, hostProperty);
        }
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
