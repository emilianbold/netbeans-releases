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
import com.netbeans.ide.TopManager;
import com.netbeans.ide.execution.Executor;

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
  }

  /** Module was uninstalled. */
  public void uninstalled() {
  }

  /** Module is being closed. */
  public boolean closing () {
    // stop the server
/*    stopHTTPServer();*/
    return true; // agree to close
  }

  /** initiates HTTPServer so it runs */
  static synchronized void initHTTPServer() {
    if (serverThread == null) {
      System.out.println("Starting the server");
      serverThread = new Thread("HTTPServer") {
        public void run() {
          try {                
            HttpServerSettings set = new HttpServerSettings();
            config = new NbServer(set);
            server = new HttpServer(config);
          } catch (Exception ex) {
            com.netbeans.ide.TopManager.getDefault().notifyException(ex);
          }
        }
      };
      serverThread.start();
    }  
  }

  /** stops the HTTP server */
  static synchronized void stopHTTPServer() {
    if ((serverThread != null) && (server != null)) {
      System.out.println("Stopping the server");
      server.close();                                
      try {
        serverThread.join();
      }
      catch (InterruptedException e) {
        // PENDING
      }
      serverThread = null;
      System.out.println("Server stopped");
    }  
  }


}

/*
 * Log
 *  1    Gandalf   1.0         5/7/99   Petr Jiricka    
 * $
 */
