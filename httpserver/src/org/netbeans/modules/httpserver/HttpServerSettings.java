/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.httpserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInput;
import java.util.ResourceBundle;
import java.util.HashSet;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Properties;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;

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
                 
  /** Has this been initialized ? 
  *  Becomes true if a "running" getter or setter is called
  */
  static boolean inited = false;
  
  public static final int SERVER_STARTUP_TIMEOUT = 3000;

  /** constant for local host */
  public  static final String LOCALHOST = "local";

  /** constant for any host */
  public static final String ANYHOST = "any";

  /** bundle to obtain text information from */
  private static ResourceBundle bundle = NbBundle.getBundle(HttpServerSettings.class);

  /** port */
  private static int port = 8082; //8080

  /** allowed connections hosts - local/any */
  private static String host = LOCALHOST;

  /** mapping of repository to URL */
  private static String repositoryBaseURL = "/repository/";
  
  /** mapping of classpath to URL */
  private static String classpathBaseURL = "/classpath/";
                                        
  /** addresses which have been granted access to the web server */
  private static String grantedAddresses = "";
                                        
  /** Reflects whether the server is actually running, not the running property */
  static boolean running = false;
  
  private static boolean startStopMessages = true;
  
  private static Properties mappedServlets = new Properties();

  /** http settings */
  public static HttpServerSettings OPTIONS;
                                      
  /** last used servlet name */                                    
  private static int lastUsedName = 0;
  /** map names of servlets to paths */
  private static HashMap nameMap = new HashMap();
                                        
  /** Used to remember the state of the running property during the deserialization */                                      
  private boolean pendingRunning = true;

  static final long serialVersionUID =7387407495740535307L;
  
  public HttpServerSettings() {
    // set the writer
    com.mortbay.Base.Log.instance()._out = new NullWriter();

    if (OPTIONS == null) {
      OPTIONS = this;  
      // register the server
      try {
        org.openide.util.HttpServer.registerServer(OPTIONS);
      }
      catch (SecurityException e) {}
    }  
    
  }

  /** human presentable name */
  public String displayName() {
    return bundle.getString("CTL_HTTP_settings");
  }
                                      
  /** getter for running status */
  public boolean isRunning() {
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
    else 
      currentRetries = 0;
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
    if (getProperty("loadingExternal") != null) {
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
    firePropertyChange("running", new Boolean(!running), new Boolean(running));
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
    firePropertyChange("repositoryBaseURL", null, this.repositoryBaseURL);
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
    firePropertyChange("classpathBaseURL", null, this.classpathBaseURL);
  }
                           
  /** Getter for grantedAddresses property */                                            
  public String getGrantedAddresses() {
    return grantedAddresses;
  }
                                              
  /** Setter for grantedAccesses property */                                            
  public void setGrantedAddresses(String grantedAddresses) {
    this.grantedAddresses = grantedAddresses;
    firePropertyChange("grantedAddresses", null, this.grantedAddresses);
  }
                                              
  /** setter for port */
  public void setPort(int p) {
    synchronized (HttpServerSettings.OPTIONS) {
      port = p;
      restartIfNecessary(true);
    }                
    firePropertyChange("port", null, new Integer(port));
  }

  /** getter for port */
  public int getPort() {
    return port;
  }

  /** setter for host */
  public void setHost(String h) {
    if (h.equals(ANYHOST) || h.equals(LOCALHOST))
      host = h;
    firePropertyChange("host", null, this.host);
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
                                       
  /* Implementation of HttpServer interface */
  
  /** Returns string for localhost */
  private String getLocalHost() {                                  
    try {
      return InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e) {
      return "localhost";
    }
  }              
                                       
  /** Maps a file object to a URL. Should ensure that the file object is accessible on the given URL. */
  public URL getRepositoryURL(FileObject fo) throws MalformedURLException, UnknownHostException {
    setRunning(true);                                                           
    return new URL("http", getLocalHost(), getPort(), 
      getRepositoryBaseURL() + fo.getPackageNameExt('/','.'));
  }
                             
  /** Maps the repository root to a URL. This URL should serve a page from which repository objects are accessible. */
  public URL getRepositoryRoot() throws MalformedURLException, UnknownHostException {
    setRunning(true);                                                           
    return new URL("http", getLocalHost(), getPort(), getRepositoryBaseURL());
  }
                                                                                                                     
  /** Maps a resource path to a URL. Should ensure that the resource is accessible on the given URL.
  * @param resourcePath path of the resource in the classloader format
  * @see ClassLoader#getResource(java.lang.String)
  * @see TopManager#systemClassLoader()
  */
  public URL getResourceURL(String resourcePath) throws MalformedURLException, UnknownHostException {
    setRunning(true);                                                           
    return new URL("http", getLocalHost(), getPort(), getClasspathBaseURL() + 
      (resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath));
  }
    
  /** Maps a resource root to a URL. Should ensure that all resources under the root are accessible under an URL
  * consisting of the returned URL and fully qualified resource name.
  * @param resourcePath path of the resource in the classloader format
  * @see ClassLoader#getResource(java.lang.String)
  * @see TopManager#systemClassLoader()
  */
  public URL getResourceRoot() throws MalformedURLException, UnknownHostException {
    setRunning(true);                                                           
    return new URL("http", getLocalHost(), getPort(), getClasspathBaseURL());
  }                                   
  
  public void mapServlet(String urlPath, String className) {
    lastUsedName++;
    String name = "NONAME" + lastUsedName;
    nameMap.put(urlPath, name);
    mapServlet(className, name, urlPath);
  }                                       
  
  public void unmapServlet(String urlPath) {
    unmapServlet0((String)nameMap.get(urlPath));
    nameMap.remove(urlPath);
  }
  
  private void mapServlet(String className, String name, String urlPath) {
    if (name.indexOf('.') != -1)
      throw new IllegalArgumentException("Servlet name may not contain a dot");
    synchronized (HttpServerSettings.OPTIONS) {
      mappedServlets.put("SERVLET." + name + ".CLASS", className); 
      mappedServlets.put("SERVLET." + name + ".PATHS", urlPath); 
      mappedServlets.put("SERVLET." + name + ".Loader", NbLoader.class.getName()); 
      restartIfNecessary(false);
    }  
  }
    
  private void unmapServlet0(String name) {
    if (name.indexOf('.') != -1)
      throw new IllegalArgumentException("Servlet name may not contain a dot");
    synchronized (HttpServerSettings.OPTIONS) {
      mappedServlets.remove("SERVLET." + name + ".CLASS"); 
      mappedServlets.remove("SERVLET." + name + ".PATHS"); 
      mappedServlets.remove("SERVLET." + name + ".Loader"); 
      restartIfNecessary(false);
    }  
  }

  /** Requests access for address addr. If necessary asks the user. Returns true it the access 
  * has been granted. */  
  public boolean allowAccess(InetAddress addr) {
    if (getHost().equals(HttpServerSettings.ANYHOST))
      return true;
      
    HashSet hs = getGrantedAddressesSet();
    if (hs.contains(addr.getHostAddress()))
      return true;
    
    // now ask the user
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
      addr.add(InetAddress.getByName("localhost").getHostAddress());
      addr.add(InetAddress.getLocalHost().getHostAddress());
    }
    catch (UnknownHostException e) {}
    StringTokenizer st = new StringTokenizer(getGrantedAddresses(), ",;");
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
