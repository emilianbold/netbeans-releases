/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.railsprojects.server;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import javax.swing.AbstractAction;
import org.netbeans.modules.ruby.rubyproject.api.RubyExecution;
import org.netbeans.modules.ruby.rubyproject.execution.DirectoryFileLocator;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer.RecognizedOutput;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;

/**
 * Support for the builtin Ruby on Rails web server: WEBrick, Mongrel, Lighttpd
 *
 * This is really primitive at this point; I should talk to the people who
 * write Java web server plugins and take some pointers. Perhaps it can
 * even implement some of their APIs such that logging, runtime nodes etc.
 * all begin to work.
 * 
 * @todo When launching under JRuby, also pass in -Djruby.thread.pooling=true to the VM
 * 
 * @author Tor Norbye, Pavel Buzek
 */
public class RailsServer {
    
    private static final int MONGREL = 1;
    private static final int LIGHTTPD = 2;
    private static final int WEBRICK = 3;
    
    enum ServerStatus { NOT_STARTED, STARTING, RUNNING; }
    
    private ServerStatus status = ServerStatus.NOT_STARTED;
    private boolean cancelled;
    private int serverId;
    private boolean portConflict;
    /** User chosen port */
    private int originalPort;
    /** Actual port in use (trying other ports for ones not in use) */
    private int port = -1;
    private RailsProject project;
    private File dir;
    private boolean debug;

    public RailsServer(RailsProject project) {
        this.project = project;
        dir = FileUtil.toFile(project.getProjectDirectory());
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    private void ensureRunning() {
        synchronized (RailsServer.this) {
            if (status == ServerStatus.STARTING) {
                return;
            } else if (status == ServerStatus.RUNNING) {
                if (serverId == MONGREL) {
                    // isPortInUse doesn't work for Mongrel
                    return;
                }
                if (isPortInUse(port)) {
                    // Simply assume it is still the same server running
                    return;
                }
            }
        }

        // Server was not started or was killed externally
        Runnable finishedAction =
            new Runnable() {
                public void run() {
                    synchronized (RailsServer.this) {
                        status = ServerStatus.NOT_STARTED;
                        if (portConflict) {
                            // Port conflict - notify user.
                            updatePort();
                        }
                    }
                }
            };

        // Start the server
        synchronized (RailsServer.this) {
            status = ServerStatus.STARTING;
        }

        portConflict = false;
        String portString = project.evaluator().getProperty(RailsProjectProperties.RAILS_PORT);
        port = 0;
        if (portString != null) {
            port = Integer.parseInt(portString);
        }
        if (port == 0) {
            port = 3000;
        }
        originalPort = port;

        while(isPortInUse(port)) {
            port++;
        }
        String projectName = project.getLookup().lookup(ProjectInformation.class).getDisplayName();
        String classPath = project.evaluator().getProperty(RailsProjectProperties.JAVAC_CLASSPATH);
        serverId = getServer();
        String displayName = getServerTabName(serverId, projectName, port);
        String serverPath = "script" + File.separator + "server"; // NOI18N
        ExecutionDescriptor desc = new ExecutionDescriptor(displayName, dir, serverPath);
        desc.additionalArgs("--port", Integer.toString(port)); // NOI18N
        desc.postBuild(finishedAction);
        desc.classPath(classPath);
        desc.addStandardRecognizers();
        desc.addOutputRecognizer(new WebrickMessageListener(getStartedMessage(serverId)));
        desc.frontWindow(false);
        desc.debug(debug);
        desc.fastDebugRequired(debug);
        desc.fileLocator(new DirectoryFileLocator(FileUtil.toFileObject(dir)));
        //desc.showProgress(false); // http://ruby.netbeans.org/issues/show_bug.cgi?id=109261
        desc.showSuspended(true);
        String charsetName = project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING);
        new RubyExecution(desc, charsetName).run();
    }
    
    private static String getServerTabName(int serverId, String projectName, int port) {
        switch (serverId) {
        case MONGREL: return NbBundle.getMessage(RailsServer.class, "MongrelTab", projectName, Integer.toString(port));
        case LIGHTTPD: return NbBundle.getMessage(RailsServer.class, "LighttpdTab", projectName, Integer.toString(port));
        case WEBRICK: 
        default:
            return NbBundle.getMessage(RailsServer.class, "WEBrickTab", projectName, Integer.toString(port));
        }
    }
    
    private static String getStartedMessage(int serverId) {
        switch (serverId) {
        case MONGREL: return "** Mongrel available at "; // NOI18N
        //case LIGHTTPD: return "=> Rails application starting on ";
        case WEBRICK: 
        default:
            return "=> Rails application started on "; // NOI18N
        }
        
    }

    static String getServerName() {
        int id = getServer();
        switch (id) {
        case MONGREL: return NbBundle.getMessage(RailsServer.class, "Mongrel");
        case LIGHTTPD: return NbBundle.getMessage(RailsServer.class, "Lighttpd");
        case WEBRICK: 
        default:
            return NbBundle.getMessage(RailsServer.class, "WEBrick");
        }
    }
    
    /** Figure out which server we're using */
    private static int getServer() {
        RubyInstallation install = RubyInstallation.getInstance();
        
        if (install.getVersion("mongrel") != null) { // NOI18N
            return MONGREL;
        } else if (install.getVersion("lighttpd") != null) { // NOI18N
            return LIGHTTPD;
        } else {
            return WEBRICK;
        }
    }
    
    private void updatePort() {
        String message = NbBundle.getMessage(RailsServer.class, "Conflict", Integer.toString(originalPort));
        NotifyDescriptor nd =
            new NotifyDescriptor.Message(message, 
            NotifyDescriptor.Message.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }

    /** Starts the server if not running and shows url.
     * @param relativeUrl the resulting url will be for example: http://localhost:3001/{relativeUrl}
     */
    public synchronized void showUrl(final String relativeUrl) {
        synchronized (RailsServer.this) {
            if (status == ServerStatus.RUNNING && isPortInUse(port)) {
                try {
                    URL url = new URL("http://localhost:" + port + "/" + relativeUrl); // NOI18N
                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                } catch (MalformedURLException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                return;
            }

            ensureRunning();
        }

        cancelled = false;

        String displayName = NbBundle.getMessage(RailsServer.class, "ServerStartup");
        final ProgressHandle handle =
            ProgressHandleFactory.createHandle(displayName,new Cancellable() {
                    public boolean cancel() {
                        synchronized (RailsServer.this) {
                            cancelled = true;
                        }

                        return true;
                    }
                },
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        // XXX ?
                    }
                });

        handle.start();
        handle.switchToIndeterminate();

        RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        // Try connecting repeatedly, up to 20 seconds, then bail
                        for (int i = 0; i < 20; i++) {
                            try {
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException ie) {
                                ; // Don't worry about it
                            }

                            synchronized (RailsServer.this) {
                                if (status == ServerStatus.RUNNING) {
                                    try {
                                        URL url = new URL("http://localhost:" + port + "/" + relativeUrl); // NOI18N
                                        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                                    } catch (MalformedURLException ex) {
                                        ErrorManager.getDefault().notify(ex);
                                    }

                                    return;
                                }

                                if (status == ServerStatus.NOT_STARTED) {
                                    // Server startup somehow failed...
                                    break;
                                }
                            }

                            /* My attempts to do URLConnections didn't pan out.... so just do a simple
                             * listener based scheme instead based on parsing build output with the
                             * OutputRecognizer
                                URLConnection connection = url.openConnection();
                                connection.setConnectTimeout(1000); // 1 second

                                if (connection instanceof HttpURLConnection) {
                                    HttpURLConnection c = (HttpURLConnection)connection;
                                    c.setRequestMethod("POST");
                                    c.setFollowRedirects(true);

                                    // Try connecting repeatedly, up to 20 seconds, then bail
                                    synchronized (WebrickServer.this) {
                                        if (status == ServerStatus.NOT_STARTED) {
                                            // Server startup somehow failed...
                                            break;
                                        }
                                    }

                                    try {
                                        c.connect();
                                        StatusDisplayer.getDefault()
                                                       .setStatusText("Connect attempt #" + i +
                                            " status was " + c.getResponseCode() + " : " +
                                            c.getResponseMessage() + " : " +
                                            c.getHeaderFields().toString());

                                        if (c.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                            synchronized (WebrickServer.this) {
                                                status = ServerStatus.RUNNING;
                                            }

                                            HtmlBrowser.URLDisplayer.getDefault().showURL(url);

                                            return;
                                        }

                                        // Disconnect?
                                        //c.disconnect();
                                        try {
                                            Thread.currentThread().sleep(1000);
                                        } catch (InterruptedException ie) {
                                            ; // Don't worry about it
                                        }
                                    } catch (ConnectException ce) {
                                        // wait 1 second and try again
                                        try {
                                            Thread.currentThread().sleep(1000);
                                        } catch (InterruptedException ie) {
                                            ; // Don't worry about it
                                        }
                                    }
                                }
                            */
                        }

                        StatusDisplayer.getDefault()
                                       .setStatusText(NbBundle.getMessage(RailsServer.class,
                                "NoServerFound", "http://localhost:" + port + "/" + relativeUrl));

                        //} catch (IOException ioe) {
                        //    ErrorManager.getDefault().notify(ioe);
                    } finally {
                        handle.finish();
                    }
                }
            });
    }

    /** Return true if there is an HTTP response from the port on localhost.
     * Based on tomcatint\tomcat5\src\org.netbeans.modules.tomcat5.util.Utils.java.
     */
    public static boolean isPortInUse(int port) {
        int timeout = 3000;
        Socket socket = new Socket();
        try {
            try {
                socket.connect(new InetSocketAddress("localhost", port), timeout); // NOI18N
                socket.setSoTimeout(timeout);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    try {
                        // request
                        out.println("GET /\n"); // NOI18N

                        // response
                        String text = in.readLine();
                        if (text == null || !text.startsWith("<!DOCTYPE")) { // NOI18N
                            return false; // not an http response
                        }
                        return true;
                    } finally {
                        in.close();
                    }
                } finally {
                    out.close();
                }
            } finally {
                socket.close();
            }
        } catch (IOException ioe) {
            return false;
        }
    }
    
    private class WebrickMessageListener extends OutputRecognizer {
        private String startedMessage;
        
        WebrickMessageListener(String startedMessage) {
            this.startedMessage = startedMessage;
        }

        @Override
        public RecognizedOutput processLine(String line) {
            // This is ugly, but my attempts to use URLConnection on the URL repeatedly
            // and check for connection.getResponseCode()==HttpURLConnection.HTTP_OK didn't
            // work - try that again later
            if (line.startsWith(startedMessage)) { // NOI18N

                synchronized (RailsServer.this) {
                    status = ServerStatus.RUNNING;
                }
            } else if (line.contains("in `new': Address in use (Errno::EADDRINUSE)")) { // NOI18N
                portConflict = true;
            }

            return null;
        }
    }

}
