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

import java.util.Enumeration;

import com.netbeans.ide.modules.ModuleInstall;
import com.netbeans.ide.execution.Executor;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.TopManager;
import com.netbeans.ide.NotifyDescriptor;

import com.mortbay.HTTP.HttpServer;

/**
* Module installation class for Http Server
*
* @author Petr Jiricka
*/
public class HttpServerModule implements ModuleInstall {

  
  private static HttpServer server;
  private static NbServer config;
  private static Thread serverThread;

  /** Module installed for the first time. */
  public void installed() {
    restored ();
  }

  /** Module installed again.
  * Add applet executor
  */
  public void restored() {            
/*    System.setProperty("DEBUG","true");
    initHTTPServer();           
    // PENDING 
    System.out.println("HttpServer is " + (server == null ? "dead" : "alive"));
    try {
    Thread.currentThread().sleep(10000);
    } catch (Exception e) {
    System.out.println("Exc:" + e.getMessage());}
    stopHTTPServer();*/ 
    com.netbeans.ide.util.HttpServer.registerDefaultServer(new HttpServerSettings());
  }

  /** Module was uninstalled. */
  public void uninstalled() {
  }

  /** Module is being closed. */
  public boolean closing () {
    // stop the server, don't set the running status
    stopHTTPServer();
    return true; // agree to close
  }

  /** initiates HTTPServer so it runs */
  static synchronized void initHTTPServer() {
    if (serverThread == null) {
      serverThread = new Thread("HTTPServer") {
        public void run() {
          HttpServerSettings set = new HttpServerSettings();
          try {                
            config = new NbServer(set);
            server = new HttpServer(config);
            set.runSuccess();
          } catch (Exception ex) {
            // couldn't start
            set.runFailure();
            TopManager.getDefault().notify(new NotifyDescriptor.Message(
              NbBundle.getBundle(HttpServerModule.class).getString("MSG_HTTP_SERVER_START_FAIL"), 
              NotifyDescriptor.Message.WARNING_MESSAGE));
          }
        }
      };
      serverThread.start();
    }  
  }

  /** stops the HTTP server */
  static synchronized void stopHTTPServer() {
    if ((serverThread != null) && (server != null)) {
      server.close();                                
      try {
        serverThread.join();
      }
      catch (InterruptedException e) {
        // PENDING
      }
      serverThread = null;
    }  
  }


}

/*
 * Log
 *  2    Gandalf   1.1         5/10/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/7/99   Petr Jiricka    
 * $
 */
