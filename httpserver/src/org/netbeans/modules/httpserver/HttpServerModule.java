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
import java.io.*;

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
    System.out.println("Registering HttpServerModule as server");
    com.netbeans.ide.util.HttpServer.registerServer(HttpServerSettings.OPTIONS);
//    com.mortbay.Base.Log.instance()._out = new NullWriter();
  }

  /** Module was uninstalled. */
  public void uninstalled() {
  }

  /** Module is being closed. */
  public boolean closing () {
    // stop the server, don't set the running status
    com.netbeans.ide.util.HttpServer.deregisterServer(HttpServerSettings.OPTIONS);
    stopHTTPServer();
    return true; // agree to close
  }

  /** initiates HTTPServer so it runs */
  static synchronized void initHTTPServer() {
    if ((serverThread != null) && (!HttpServerSettings.OPTIONS.running)) {
      // another thread is trying to start the server, wait for a while and then stop it if it's still bad
      try {
        Thread.currentThread().sleep(3000);
      }
      catch (InterruptedException e) {}
      if ((serverThread != null) && (!HttpServerSettings.OPTIONS.running)) {
        serverThread.stop();
        serverThread = null;
      }
    }
    if (serverThread == null) {
      serverThread = new Thread("HTTPServer") {
        public void run() {
          try {                
            config = new NbServer(HttpServerSettings.OPTIONS);
            server = new NbHttpServer(config);
            HttpServerSettings.OPTIONS.runSuccess();
          } catch (Exception ex) {
            // couldn't start
            HttpServerSettings.OPTIONS.runFailure();
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
        serverThread.stop();
        // PENDING
      }
      serverThread = null;
    }  
  }


}

/*
 * Log
 *  8    Gandalf   1.7         5/31/99  Petr Jiricka    
 *  7    Gandalf   1.6         5/28/99  Petr Jiricka    
 *  6    Gandalf   1.5         5/25/99  Petr Jiricka    
 *  5    Gandalf   1.4         5/17/99  Petr Jiricka    
 *  4    Gandalf   1.3         5/17/99  Petr Jiricka    
 *  3    Gandalf   1.2         5/11/99  Petr Jiricka    
 *  2    Gandalf   1.1         5/10/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/7/99   Petr Jiricka    
 * $
 */
