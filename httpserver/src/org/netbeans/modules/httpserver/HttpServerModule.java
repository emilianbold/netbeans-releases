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
import java.net.URL;
import java.net.MalformedURLException;

import org.openide.modules.ModuleInstall;
import org.openide.execution.Executor;
import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

import com.sun.web.server.HttpServer;
import com.sun.web.server.HttpServerException;
import com.sun.web.core.Container;
import com.sun.web.core.Context;

/**
* Module installation class for Http Server
*
* @author Petr Jiricka
*/
public class HttpServerModule extends ModuleInstall implements Externalizable {

  
  private static HttpServer server;
//  private static NbServer config;
  private static Thread serverThread;
  private static boolean inSetRunning = false;
  
  static boolean optionsSerialized = false;

  /** Module installed again.
  * Add applet executor
  */
  public void restored() {            
System.out.println("restored - serialized : " + optionsSerialized);
    if (!optionsSerialized) {
      // set the default value of the running property
      new HttpServerSettings();  // this fills the options variable,  bugfix #3595
      HttpServerSettings.OPTIONS.isRunning();                                    
    }
  }


  /** Module is being closed. */
  public boolean closing () {
    // stop the server, don't set the running status
    try {
      org.openide.util.HttpServer.deregisterServer(HttpServerSettings.OPTIONS);
    }
    catch (SecurityException e) {
      // pending - why do I get SecurityException ?
    } 
    synchronized (HttpServerSettings.OPTIONS) {
      stopHTTPServer();
    }  
    return true; // agree to close
  }

	/** Writes data
	* @param out ObjectOutputStream
	*/
 	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(new Boolean(true));
	}
	
	/** Reads data
	* @param in ObjectInputStream
	*/
 	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException	{
System.out.println("readExternal");
 	  Object obj = in.readObject();
 	  if (obj instanceof Boolean) {
 	    optionsSerialized = ((Boolean)obj).booleanValue();
System.out.println("boolean : " + optionsSerialized);
 	  }
System.out.println("class : " + ((obj == null) ? "null" : obj.getClass().getName()));
	}	

  /** initiates HTTPServer so it runs */
  static void initHTTPServer() {
    if (inSetRunning)
      return;
    synchronized (HttpServerSettings.OPTIONS) {  
      if (inSetRunning)
        return; 
      inSetRunning = true;
      try {
        if ((serverThread != null) && (!HttpServerSettings.OPTIONS.running)) {
          // another thread is trying to start the server, wait for a while and then stop it if it's still bad
          try {
            Thread.currentThread().sleep(2000);
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
                server = buildServer();
                server.start();
                HttpServerSettings.OPTIONS.runSuccess();
                // this is not a debug message, this is a server startup message
                if (HttpServerSettings.OPTIONS.isStartStopMessages())
                  System.out.println(java.text.MessageFormat.format(NbBundle.getBundle(HttpServerModule.class).
                    getString("CTL_ServerStarted"), new Object[] {new Integer(HttpServerSettings.OPTIONS.getPort())}));
              } 
              catch (Throwable ex) {
                // couldn't start
ex.printStackTrace();                
                serverThread = null;
                inSetRunning = false;
                HttpServerSettings.OPTIONS.runFailure();
                if (!HttpServerSettings.OPTIONS.running)
                  TopManager.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getBundle(HttpServerModule.class).getString("MSG_HTTP_SERVER_START_FAIL"), 
                    NotifyDescriptor.Message.WARNING_MESSAGE));
              }
              finally {
                HttpServerSettings.OPTIONS.setStartStopMessages(true);
              }
            }
          };
          serverThread.start();
        }  
        // wait for the other thread to start the server
        try {
          HttpServerSettings.OPTIONS.wait(HttpServerSettings.SERVER_STARTUP_TIMEOUT);
        }
        catch (Exception e) {
        }
      }  
      finally {  
        inSetRunning = false;
      }  
    }
  }

  /** stops the HTTP server */
  static void stopHTTPServer() {
    if (inSetRunning)
      return;
    synchronized (HttpServerSettings.OPTIONS) {  
      if (inSetRunning)
        return;
      inSetRunning = true;
      try {
        if ((serverThread != null) && (server != null)) {
          try {
            server.stop();
            serverThread.join();
          }
          catch (InterruptedException e) {
            serverThread.stop(); 
            /* deprecated, but this really is the last resort,
               only if everything else failed */
          } 
          catch (HttpServerException e) {
e.printStackTrace();
            serverThread.stop(); 
            /* deprecated, but this really is the last resort,
               only if everything else failed */
          }
          serverThread = null;
          // this is not a debug message, this is a server shutdown message
          if (HttpServerSettings.OPTIONS.isStartStopMessages())
            System.out.println(NbBundle.getBundle(HttpServerModule.class).
              getString("CTL_ServerStopped"));
        }  
      }
      finally {  
        inSetRunning = false;
      }  
    }
  }
  
  
  private static HttpServer buildServer() {
    HttpServerSettings op = HttpServerSettings.OPTIONS;
    HttpServer server = new HttpServer(op.getPort(), null, null);

    try {
      server.setDocumentBase(new URL("file:///nonexistingdirectory"));
    }
    catch (MalformedURLException e) {
      throw new InternalError();
    }  
    Context context = server.getDefaultContext();
    context.setClassLoader(TopManager.getDefault().systemClassLoader());
    
    Container container = context.getContainer();

    container.addServlet("repositoryHandler", "com.netbeans.developer.modules.httpserver.RepositoryServlet");
    container.addMapping("repositoryHandler", op.getRepositoryBaseURL());
    
    container.addServlet("classpathHandler", "com.netbeans.developer.modules.httpserver.ClasspathServlet");
    container.addMapping("classpathHandler", op.getClasspathBaseURL());
    
    return server;
  }


}

/*
 * Log
 *  26   Gandalf   1.25        10/6/99  Petr Jiricka    Removed debug DumpStack
 *  25   Gandalf   1.24        10/5/99  Petr Jiricka    
 *  24   Gandalf   1.23        10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  23   Gandalf   1.22        9/30/99  Petr Jiricka    Jetty -> JSWDK (TomCat)
 *  22   Gandalf   1.21        9/8/99   Petr Jiricka    SecurityException fix
 *  21   Gandalf   1.20        9/8/99   Petr Jiricka    Fixed 
 *       NullPointerException at startup
 *  20   Gandalf   1.19        8/17/99  Petr Jiricka    Externalization - server
 *       startup during the first IDE start
 *  19   Gandalf   1.18        8/9/99   Petr Jiricka    Fixed bug with multiple 
 *       restarts of the server on IDE startup
 *  18   Gandalf   1.17        7/24/99  Petr Jiricka    
 *  17   Gandalf   1.16        7/3/99   Petr Jiricka    
 *  16   Gandalf   1.15        7/3/99   Petr Jiricka    
 *  15   Gandalf   1.14        6/23/99  Petr Jiricka    
 *  14   Gandalf   1.13        6/22/99  Petr Jiricka    
 *  13   Gandalf   1.12        6/11/99  Jaroslav Tulach System.out commented
 *  12   Gandalf   1.11        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   Gandalf   1.10        6/8/99   Petr Jiricka    
 *  10   Gandalf   1.9         6/1/99   Petr Jiricka    
 *  9    Gandalf   1.8         6/1/99   Petr Jiricka    
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
