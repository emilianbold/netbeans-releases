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
import java.net.InetAddress;
import java.lang.reflect.InvocationTargetException;

import org.openide.modules.ModuleInstall;
import org.openide.execution.Executor;
import org.openide.execution.NbClassPath;
import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.ServerConnector;
import org.apache.tomcat.core.ServletWrapper;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.context.*;
import org.apache.tomcat.service.SimpleTcpConnector;

/**
* Module installation class for Http Server
*
* @author Petr Jiricka
*/
public class HttpServerModule extends ModuleInstall implements Externalizable {

  
  private static ContextManager server;
  private static Thread serverThread;
  private static boolean inSetRunning = false;
  
  static final long serialVersionUID =8562026516563511530L;

  /** Module installed again.
  */
  public void restored() {            
    try {
      org.openide.util.HttpServer.registerServer(HttpServerSettings.OPTIONS);
    }
    catch (SecurityException e) {}
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
          serverThread = new Thread("HTTPServer") { // NOI18N
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
              catch (ThreadDeath td) {
                throw td;
              }
              catch (Throwable ex) {
ex.printStackTrace();
                // couldn't start
                serverThread = null;
                inSetRunning = false;
                HttpServerSettings.OPTIONS.runFailure();
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
          catch (Exception e) {
//e.printStackTrace();
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

  private static ContextManager buildServer() throws Exception {
    HttpServerSettings op = HttpServerSettings.OPTIONS;

    ContextManager cm = new ContextManager();
cm.setDebug(10);

    /*ServerConnector sc = new SimpleTcpConnector();
    sc.setProperty(SimpleTcpConnector.HANDLER, "org.apache.tomcat.service.http.HttpConnectionHandler");
    sc.setProperty(SimpleTcpConnector.PORT, "" + op.getPort());
    cm.addServerConnector(sc);*/

    cm.addContextInterceptor(new LogEvents());
    //cm.addContextInterceptor(new AutoSetup());
    cm.addContextInterceptor(new NbCMSetter());
    cm.addContextInterceptor(new WorkDirInterceptor());
    //cm.addContextInterceptor(new WebXmlReader());
    cm.addContextInterceptor(new LoadOnStartupInterceptor());

    cm.setDefaults();
    // set the HTTP connector port
    SimpleTcpConnector con = (SimpleTcpConnector)cm.getConnectors().nextElement();
    con.setPort(op.getPort());

    Context ctxt = new Context();
    ctxt.setContextManager(cm);

    ServletWrapper repo = new ServletWrapper();
    repo.setServletClass("com.netbeans.developer.modules.httpserver.RepositoryServlet");
    repo.setServletName("RepositoryServlet");
    ctxt.addServlet(repo);
    ctxt.addServletMapping(op.getRepositoryBaseURL(), "RepositoryServlet");
    
    ServletWrapper claz = new ServletWrapper();
    claz.setServletClass("com.netbeans.developer.modules.httpserver.ClasspathServlet");
    claz.setServletName("ClasspathServlet");
    ctxt.addServlet(claz);
    ctxt.addServletMapping(op.getClasspathBaseURL(), "ClasspathServlet");

    cm.addContext(ctxt);

System.out.println("workdir " + cm.getWorkDir());
System.out.println("tomcat home " + cm.getTomcatHome());
    cm.init();
    return cm;
  }
  
  
/*  private static HttpServer buildServer() {
    HttpServerSettings op = HttpServerSettings.OPTIONS;

    HttpServer server = new NbHttpServer(op.getPort(), null, null);

    try {
      server.setDocumentBase(new URL("file:///nonexistingdirectory")); // NOI18N
    }
    catch (MalformedURLException e) {
      throw new InternalError();
    }  
    Context context = server.getDefaultContext();
    context.setSecurityModule (new NbSecurityModule (context));
    context.setClassLoader(TopManager.getDefault().systemClassLoader());
    
    Container container = context.getContainer();

    container.addServlet("repositoryHandler", "com.netbeans.developer.modules.httpserver.RepositoryServlet"); // NOI18N
    container.addMapping("repositoryHandler", op.getRepositoryBaseURL()); // NOI18N
    
    container.addServlet("classpathHandler", "com.netbeans.developer.modules.httpserver.ClasspathServlet"); // NOI18N
    container.addMapping("classpathHandler", op.getClasspathBaseURL()); // NOI18N
    
    return server;
  }
  
  static class NbHttpServer extends HttpServer {
    
    public NbHttpServer(int i, InetAddress inetaddress, String s) {
      super(i, inetaddress, s);
    }
    
    public void start() throws HttpServerException {
      File wd = NbClassPath.toFile(
        TopManager.getDefault().getRepository().getDefaultFileSystem().getRoot());
      wd = new File(wd, "httpwork"); // NOI18N
        
      try {
        java.lang.reflect.Field ff = HttpServer.class.getDeclaredField("isWorkDirPersistent"); // NOI18N
        ff.setAccessible(true);
        boolean bb = ff.getBoolean(this);
        
        getDefaultContext().setWorkDir(wd, bb / *isWorkDirPersistent* /);
        getDefaultContext().init();
        EndpointManager endpointmanager = EndpointManager.getManager();

        // endpointmanager.startServer(this);
        java.lang.reflect.Method mm = EndpointManager.class.getDeclaredMethod("startServer", new Class[] {HttpServer.class}); // NOI18N
        mm.setAccessible(true);
        mm.invoke(endpointmanager, new Object[] { this });
      }
      catch (NoSuchMethodException e) {
        if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
          e.printStackTrace();
        throw new IllegalArgumentException();  
      }
      catch (NoSuchFieldException e) {
        if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
          e.printStackTrace();
        throw new IllegalArgumentException();  
      }
      catch (IllegalAccessException e) {
        if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
          e.printStackTrace();
        throw new IllegalArgumentException();  
      }
      catch (InvocationTargetException e) {
        Throwable th = e.getTargetException();
        throw ((HttpServerException)th);
      }
    }
      
  }
    
  static class NbSecurityModule implements SecurityModule {

    public NbSecurityModule (Context context1) {
      context = context1;
    }

    public Context getContext() {
      return context;
    }

    public boolean authenticateRequest(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
      throws IOException {
      return true;
    }

    public boolean authorizeRequest(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
      throws IOException {

      HttpServerSettings op = HttpServerSettings.OPTIONS;

      String requestURI = httpservletrequest.getRequestURI ();
      String contextPath = context.getPath();
      String lookupPath = requestURI.substring(contextPath.length(), requestURI.length());
      int i = lookupPath.indexOf("?"); // NOI18N
      if(i > -1) lookupPath = lookupPath.substring(0, i);
      if(lookupPath.length() < 1) lookupPath = "/"; // NOI18N
      String s = lookupPath.toLowerCase();
      if(s.startsWith("/servlet/")) { // NOI18N
        return true;
      }  
      if(s.startsWith("/web-inf")) { // NOI18N
        httpservletresponse.sendError(403);
        return false;
      } if (s.startsWith(op.getRepositoryBaseURL() + "/") || s.startsWith(op.getClasspathBaseURL() + "/")) { // NOI18N
        return true;
      } else {
        httpservletresponse.sendError(404);
        return false;
      }
                   
    }

    private Context context;
  }*/

}

/*
 * Log
 *  40   Jaga      1.37.1.1    3/24/00  Petr Jiricka    Implemented 
 *       Jakarta-based webserver
 *  39   Jaga      1.37.1.0    3/22/00  Petr Jiricka    Fixed compilation 
 *       errors.
 *  38   Gandalf   1.37        1/13/00  Petr Jiricka    More i18n
 *  37   Gandalf   1.36        1/12/00  Petr Jiricka    i18n
 *  36   Gandalf   1.35        1/11/00  Petr Jiricka    Cleanup
 *  35   Gandalf   1.34        1/9/00   Petr Jiricka    Cleanup
 *  34   Gandalf   1.33        12/1/99  Ales Novak      bugfix - working 
 *       directory is not system - system is deleted no more
 *  33   Gandalf   1.32        11/27/99 Patrik Knakal   
 *  32   Gandalf   1.31        11/25/99 Petr Jiricka    - Another 
 *       security-related fix - for URLs starting with an allowed substring but 
 *       continuing further on.  - Relaxed security to allow executing arbitrary
 *       servlets  - Fixed undesirable creation of "work" directory
 *  31   Gandalf   1.30        11/24/99 Ian Formanek    Fixed bug 4767 - 
 *       Security problem:  Internal HTTP Server allows access to directories 
 *       including and below root directory under NT.  Could default Project 
 *       Settings | HTTP Server | Grant Access To to 127.0.0.0?
 *  30   Gandalf   1.29        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  29   Gandalf   1.28        10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  28   Gandalf   1.27        10/7/99  Petr Jiricka    
 *  27   Gandalf   1.26        10/6/99  Petr Jiricka    Removed module 
 *       (de)serialization
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
