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
import java.util.ResourceBundle;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.netbeans.ide.options.SystemOption;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.HttpServer;
import com.netbeans.ide.filesystems.FileObject;

/** Options for http server
*
* @author Ales Novak, Petr Jiricka
* @version 0.12, May 5, 1999
*/
public class HttpServerSettings extends SystemOption implements HttpServer.Impl {
  
  /** generated Serialized Version UID */
  //static final long serialVersionUID = -2930037136839837001L;
                 
  /** Object to synchronize on */               
  static Object lock = new Object();
  
  /** Has this been initialized ? 
  *  Becomes true if a "running" getter or setter is called
  */
  static boolean inited = false;

  /** constant for local host */
  public  static final String LOCALHOST = "local";

  /** constant for any host */
  public static final String ANYHOST = "any";

  /** bundle to obtain text information from */
  private static ResourceBundle bundle = NbBundle.getBundle(HttpServerSettings.class);

  /** port */
  private static int port = 81; //8080

  /** allowed connections hosts - local/any */
  private static String host = LOCALHOST;

  /** mapping of repository to URL */
  private static String repositoryBaseURL = "/repository/";
  
  /** mapping of classpath to URL */
  private static String classpathBaseURL = "/classpath/";
                                        
  /** Reflects whether the server is actually running, not the running property */
  private static boolean running = false;

  /** http settings */
  final static HttpServerSettings OPTIONS = new HttpServerSettings();


  public HttpServerSettings() {
  }

  /** human presentable name */
  public String displayName() {
    return bundle.getString("CTL_HTTP_settings");
  }
                                      
  /** getter for running status */
  public boolean isRunning() {
    if (inited)
      return running;
    else {
      // default value, which is true -> start it
      setRunning(true);
      return running;
    } 
  }                                         
  
  /** Intended to be called by the thread which succeeded to start the server */
  void runSuccess() {
    running = true;
  }

  /** Intended to be called by the thread which failed to start the server */
  void runFailure() {
    running = false;
  }

  /** Restarts the server if it is running */
  private void restartIfNecessary() {
    if (isRunning()) {
      HttpServerModule.stopHTTPServer();
      HttpServerModule.initHTTPServer();
    }
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
    inited = true;
    if (this.running == running)
      return;
    
    synchronized (this) {
      if (running)  
        // running status is set by another thread
        HttpServerModule.initHTTPServer();
      else {
        running = false;
        HttpServerModule.stopHTTPServer();
      }  
    }  
    // PENDING  
    //firePropertyChange(
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
    synchronized (lock) {
      this.repositoryBaseURL = newURL;
      restartIfNecessary();
    }
    // PENDING
    //firePropertyChange(
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
    synchronized (lock) {
      this.classpathBaseURL = newURL;
      restartIfNecessary();
    }
    // PENDING
    //firePropertyChange(
  }

  /** setter for port */
  public void setPort(int p) {
    synchronized (lock) {
      port = p;
      restartIfNecessary();
    }  
  }

  /** getter for port */
  public int getPort() {
    return port;
  }

  /** setter for host */
  public void setHost(String h) {
    if (h == ANYHOST || h == LOCALHOST)
      host = h;
  }

  /** getter for host */
  public String getHost() {
    return host;
  }


  /* Access the firePropertyChange from HTTPServer (which holds the enabled prop). */
  void firePropertyChange0 (String name, Object oldVal, Object newVal) {
    firePropertyChange (name, oldVal, newVal);
  }
                                       
  /* Implementation of HttpServer interface */
                                       
  /** Maps a file object to a URL. Should ensure that the file object is accessible on the given URL. */
  public URL getRepositoryURL(FileObject fo) throws MalformedURLException, UnknownHostException {
    setRunning(true);                                                           
    return new URL("http", InetAddress.getLocalHost().getHostName(), getPort(), 
      getRepositoryBaseURL() + fo.getPackageNameExt('/','.'));
  }
                             
  /** Maps the repository root to a URL. This URL should serve a page from which repository objects are accessible. */
  public URL getRepositoryRoot() throws MalformedURLException, UnknownHostException {
    setRunning(true);                                                           
    return new URL("http", InetAddress.getLocalHost().getHostName(), getPort(), getRepositoryBaseURL());
  }
                                                                                                                     
  /** Maps a resource path to a URL. Should ensure that the resource is accessible on the given URL.
  * @param resourcePath path of the resource in the classloader format
  * @see ClassLoader#getResource(java.lang.String)
  * @see TopManager#systemClassLoader()
  */
  public URL getResourceURL(String resourcePath) throws MalformedURLException, UnknownHostException {
    setRunning(true);                                                           
    return new URL("http", InetAddress.getLocalHost().getHostName(), getPort(), 
      getClasspathBaseURL() + resourcePath);
  }
    
                                       
}

/*
 * Log
 *  2    Gandalf   1.1         5/10/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/7/99   Petr Jiricka    
 * $
 */
