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
import org.openide.filesystems.FileObject;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

/** Options for http server
*
* @author Ales Novak, Petr Jiricka
* @version 0.12, May 5, 1999
*/
public class HttpServerSettings extends SystemOption implements HttpServer.Impl {

    private static final int MAX_START_RETRIES = 5;
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
    public static final String PROP_HOST               = "host"; // NOI18N
    public static final String PROP_REPOSITORY_BASEURL = "repositoryBaseURL"; // NOI18N
    public static final String PROP_CLASSPATH_BASEURL  = "classpathBaseURL"; // NOI18N
    public static final String PROP_RUNNING            = "running"; // NOI18N
    public static final String PROP_GRANTED_ADDRESSES  = "grantedAddresses"; // NOI18N

    /** bundle to obtain text information from */
    private static ResourceBundle bundle = NbBundle.getBundle(HttpServerSettings.class);

    /** port */
    //  private static int port = 8082; //8080
    private static final int DEFAULT_PORT = 8082;

    /** allowed connections hosts - local/any */
    private static String host = LOCALHOST;

    /** mapping of repository to URL */
    private static String repositoryBaseURL = "/repository/"; // NOI18N

    /** mapping of classpath to URL */
    private static String classpathBaseURL = "/classpath/"; // NOI18N

    /** addresses which have been granted access to the web server */
    private static String grantedAddresses = ""; // NOI18N

    /** Reflects whether the server is actually running, not the running property */
    static boolean running = false;

    private static boolean startStopMessages = true;

    private static Properties mappedServlets = new Properties();

    /** http settings */
    public static HttpServerSettings OPTIONS = new HttpServerSettings();

    /** last used servlet name */
    private static int lastUsedName = 0;
    /** map names of servlets to paths */
    private static HashMap nameMap = new HashMap();

    /** Used to remember the state of the running property during the deserialization */
    private boolean pendingRunning = true;

    static final long serialVersionUID =7387407495740535307L;

    public HttpServerSettings() {
    }

    /** This is a project option. */
    private boolean isGlobal() {
        return false;
    }

    /** human presentable name */
    public String displayName() {
        return bundle.getString("CTL_HTTP_settings");
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
            // default value, which is true -> start it
            setRunning(true);
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
                                               NbBundle.getBundle(HttpServerSettings.class).getString("MSG_HTTP_SERVER_START_FAIL"),
                                               NotifyDescriptor.Message.WARNING_MESSAGE));
        }
    }

    /** Restarts the server if it is running - must be called in a synchronized block */
    private void restartIfNecessary(boolean printMessages) {
        if (running) {
            if (!printMessages)
                setStartStopMessages(false);
            HttpServerModule.stopHTTPServer();
            HttpServerModule.initHTTPServer();
            // messages will be enabled by the server thread
        }
    }

    /** Reads from the serialized state */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
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

    /** Getter for grantedAddresses property */
    public String getGrantedAddresses() {
        return grantedAddresses;
    }

    /** Setter for grantedAccesses property */
    public void setGrantedAddresses(String grantedAddresses) {
        this.grantedAddresses = grantedAddresses;
        firePropertyChange(PROP_GRANTED_ADDRESSES, null, this.grantedAddresses);
    }

    /** setter for port */
    public void setPort(int p) {
        Object old = null;
        synchronized (HttpServerSettings.OPTIONS) {
            old = putProperty(PROP_PORT, new Integer(p), false);
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
        firePropertyChange(PROP_HOST, null, this.host);
    }

    /** getter for host */
    public String getHost() {
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

}

/*
 * Log
 *  31   Jaga      1.29.1.0    3/24/00  Petr Jiricka    Fixing main servlets, 
 *       grant access listeners
 *  30   Gandalf   1.29        1/12/00  Petr Jiricka    i18n
 *  29   Gandalf   1.28        1/11/00  Petr Jiricka    Fixed 5133
 *  28   Gandalf   1.27        1/9/00   Petr Jiricka    Cleanup
 *  27   Gandalf   1.26        1/4/00   Petr Jiricka    Added to project options
 *  26   Gandalf   1.25        1/3/00   Petr Jiricka    Bugfix 5133
 *  25   Gandalf   1.24        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  24   Gandalf   1.23        10/9/99  Petr Jiricka    Fixed serialization of 
 *       running property in the first startup if the server hasn't been 
 *       launched.
 *  23   Gandalf   1.22        10/7/99  Petr Jiricka    Fixed multiple startup 
 *       and shutdown at deserialization in some cases
 *  22   Gandalf   1.21        10/6/99  Petr Jiricka    Changes caused by module
 *       (de)serialization
 *  21   Gandalf   1.20        10/6/99  Petr Jiricka    Fixed bug causing the 
 *       server to start at IDE shutdown (after the first start of the IDE)
 *  20   Gandalf   1.19        9/30/99  Petr Jiricka    Jetty -> JSWDK
 *  19   Gandalf   1.18        9/13/99  Petr Jiricka    Default port moved to 
 *       8082
 *  18   Gandalf   1.17        9/8/99   Petr Jiricka    Fixed 
 *       NullPointerException at startup
 *  17   Gandalf   1.16        8/17/99  Petr Jiricka    Fixed startup of the 
 *       server during the first IDE start
 *  16   Gandalf   1.15        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  15   Gandalf   1.14        8/9/99   Petr Jiricka    Fixed bug with multiple 
 *       restarts of the server on IDE startup
 *  14   Gandalf   1.13        7/3/99   Petr Jiricka    
 *  13   Gandalf   1.12        7/3/99   Petr Jiricka    
 *  12   Gandalf   1.11        6/25/99  Petr Jiricka    Removed debug prints
 *  11   Gandalf   1.10        6/24/99  Petr Jiricka    Implements recent 
 *       changes in org.openide.util.HttpServer - allowAccess(...)
 *  10   Gandalf   1.9         6/23/99  Petr Jiricka    
 *  9    Gandalf   1.8         6/22/99  Petr Jiricka    
 *  8    Gandalf   1.7         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         6/8/99   Petr Jiricka    
 *  6    Gandalf   1.5         5/31/99  Petr Jiricka    
 *  5    Gandalf   1.4         5/28/99  Petr Jiricka    
 *  4    Gandalf   1.3         5/11/99  Petr Jiricka    
 *  3    Gandalf   1.2         5/11/99  Petr Jiricka    
 *  2    Gandalf   1.1         5/10/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/7/99   Petr Jiricka    
 * $
 */
