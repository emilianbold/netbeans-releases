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

import com.netbeans.ide.options.SystemOption;
import com.netbeans.ide.util.NbBundle;

/** Options for http server
*
* @author Ales Novak, Petr Jiricka
* @version 0.12, May 5, 1999
*/
public class HttpServerSettings extends SystemOption {
  
  /** generated Serialized Version UID */
  //static final long serialVersionUID = -2930037136839837001L;
  
  static Object lock = new Object();

  /** constant for local host */
  public  static final String LOCALHOST = "local";

  /** constant for any host */
  public static final String ANYHOST = "any";

  /** bundle to obtain text information from */
  private static ResourceBundle bundle = NbBundle.getBundle(HttpServerSettings.class);

  /** port */
  private static int port = 81; //8080

  /** mapping of repository to URL */
  private static String host = LOCALHOST;

  /** allowed connections hosts - local/any */
  private static String repositoryBaseURL = "/repository/";
  
  private static boolean running = false;

  /** http settings */
  final static HttpServerSettings OPTIONS = new HttpServerSettings();


  public HttpServerSettings() {
    System.out.println("Server settings instantiated");
  }

  /** human presentable name */
  public String displayName() {
    return bundle.getString("CTL_HTTP_settings");
  }
                                      
  /** getter for running status */
  public boolean isRunning() {
    return running;
  }

  /** setter for running status */
  public void setRunning(boolean running) {
    if (this.running == running)
      return;
    
    synchronized (this) {
      this.running = running;
      if (running)  
        HttpServerModule.initHTTPServer();
      else   
        HttpServerModule.stopHTTPServer();
    }  
    // PENDING  
    //firePropertyChange(
  }

  /** getter for repository base */
  public String getRepositoryBaseURL() {
    return repositoryBaseURL;
  }
  
  private void restartIfNecessary() {
    if (isRunning()) {
      HttpServerModule.stopHTTPServer();
      HttpServerModule.initHTTPServer();
    }
  }
  
  /** setter for repository base */
  public void setRepositoryBaseURL(String repositoryBaseURL) {
    // canonical form starts and ends with a /
    String newURL;
    if (repositoryBaseURL.length() == 0)
      newURL = "/";
    else {
      if (repositoryBaseURL.charAt(0) != '/')
        newURL = "/" + repositoryBaseURL;
      else
        newURL = repositoryBaseURL;
      if (newURL.charAt(newURL.length() - 1) != '/')
        newURL = newURL + "/";
    }                                     
    
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

}

/*
 * Log
 *  1    Gandalf   1.0         5/7/99   Petr Jiricka    
 * $
 */
