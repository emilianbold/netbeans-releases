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
import org.openide.ErrorManager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.startup.EmbededTomcat;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.ServerConnector;
import org.apache.tomcat.core.ServletWrapper;
import org.apache.tomcat.core.FacadeManager;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.logging.TomcatLogger;
import org.apache.tomcat.context.*;
import org.apache.tomcat.service.PoolTcpConnector;
import org.openide.util.SharedClassObject;

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
            org.openide.util.HttpServer.registerServer(httpserverSettings ());
        }
        catch (SecurityException e) {}
    }


    /** Module is being closed. */
    public void close () {
        // stop the server, don't set the running status
        try {
            org.openide.util.HttpServer.deregisterServer(httpserverSettings ());
        }
        catch (SecurityException e) {
            // pending - why do I get SecurityException ?
        }
        synchronized (HttpServerSettings.httpLock ()) {
            stopHTTPServer();
        }
    }

    /** initiates HTTPServer so it runs */
    static void initHTTPServer() {
        if (inSetRunning)
            return;
        synchronized (HttpServerSettings.httpLock ()) {
            if (inSetRunning)
                return;
            inSetRunning = true;
            try {
                if ((serverThread != null) && (!httpserverSettings ().running)) {
                    // another thread is trying to start the server, wait for a while and then stop it if it's still bad
                    try {
                        Thread.currentThread().sleep(2000);
                    }
                    catch (InterruptedException e) {}
                    if ((serverThread != null) && (!httpserverSettings ().running)) {
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
                                               httpserverSettings ().runSuccess();
                                               // this is not a debug message, this is a server startup message
                                               if (httpserverSettings ().isStartStopMessages())
                                                   System.out.println(java.text.MessageFormat.format(NbBundle.getBundle(HttpServerModule.class).
                                                                      getString("CTL_ServerStarted"), new Object[] {new Integer(httpserverSettings ().getPort())}));
                                           }
                                           catch (ThreadDeath td) {
                                               throw td;
                                           }
                                           catch (java.net.BindException ex) {
                                               // If the socket bind fails, log it. NetBeans will continue to loop 
                                               // to find the first open socket
                                               //
                                               TopManager.getDefault().getErrorManager().notify( ErrorManager.INFORMATIONAL, ex);
                                               // couldn't start
                                               serverThread = null;
                                               inSetRunning = false;
                                               httpserverSettings ().runFailure();
                                           }
                                           catch (Throwable ex) {
                                               ex.printStackTrace();
                                               // couldn't start
                                               serverThread = null;
                                               inSetRunning = false;
                                               httpserverSettings ().runFailure();
                                           }
                                           finally {
                                               httpserverSettings ().setStartStopMessages(true);
                                           }
                                       }
                                   };
                    serverThread.start();
                }
                // wait for the other thread to start the server
                try {
                    HttpServerSettings.httpLock ().wait(HttpServerSettings.SERVER_STARTUP_TIMEOUT);
                }
                catch (Exception e) {
                    TopManager.getDefault().getErrorManager().notify( ErrorManager.INFORMATIONAL, e);
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
        synchronized (HttpServerSettings.httpLock ()) {
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
                    if (httpserverSettings ().isStartStopMessages())
                        System.out.println(NbBundle.getBundle(HttpServerModule.class).
                                           getString("CTL_ServerStopped"));
                }
            }
            finally {
                inSetRunning = false;
            }
        }
    }
    
    
    private static ContextManager getContextManager(EmbededTomcat tc) {
        try {
            java.lang.reflect.Field fm = EmbededTomcat.class.getDeclaredField("contextM");
            fm.setAccessible(true);
            return (ContextManager)fm.get(tc);
        }
        catch (NoSuchFieldException e) {
            return null;
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }
    

    private static ContextManager buildServer() throws Exception {
        HttpServerSettings op = httpserverSettings ();

        NbLogger logger = new NbLogger();
        logger.setName("tc_log");

        EmbededTomcat tc=new EmbededTomcat();
        
        File wd = NbClassPath.toFile(
                      TopManager.getDefault().getRepository().getDefaultFileSystem().getRoot());
        wd = new File(wd, "httpwork"); // NOI18N
        tc.setWorkDir(wd.getAbsolutePath());
        
        // install interceptors which need to be initialized BEFORE the default server interceptors
	NbLoaderInterceptor nbL =new NbLoaderInterceptor();
	tc.addContextInterceptor( nbL );
        
        // hack - force initialization of default interceptors, so our interceptor is after them
        tc.addApplicationAdapter(null);

        // install interceptors which need to be initialized AFTER the default server interceptors
	NbServletsInterceptor nbI =new NbServletsInterceptor();
	tc.addContextInterceptor( nbI );
        
        ServletContext sctx;
        sctx=tc.addContext("", wd.toURL());
        tc.initContext( sctx );
        //ctxt.getServletLoader().setParentLoader(TopManager.getDefault().systemClassLoader());
        
        tc.addEndpoint( op.getPort(), null, null);
        
        ContextManager cm = getContextManager(tc);
        
        // reduce number of threads
        Enumeration e = cm.getConnectors ();
        while (e.hasMoreElements ()) {
            Object o = e.nextElement ();
            if (o instanceof PoolTcpConnector) {
                org.apache.tomcat.core.ServerConnector conn = (PoolTcpConnector)o;
                conn.setAttribute (PoolTcpConnector.MIN_SPARE_THREADS, "0");
                conn.setAttribute (PoolTcpConnector.MAX_SPARE_THREADS, "1");
                conn.setAttribute (PoolTcpConnector.MAX_THREADS, "3");
            }
        }
        
        return cm;
        
    }

    private static class NbLogger extends TomcatLogger {
        public NbLogger() {
            super();
        }

        protected void realLog(String message) {
        }

        protected void realLog(String message, Throwable t) {
        }
    
        public void flush() {
        }
    }

    /** 
     * Obtains settings of this module
     */
    private static HttpServerSettings httpserverSettings () {
        return (HttpServerSettings)SharedClassObject.findObject (HttpServerSettings.class, true);
    }
}

