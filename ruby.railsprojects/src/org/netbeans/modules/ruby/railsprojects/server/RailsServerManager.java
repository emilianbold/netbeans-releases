/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.ruby.railsprojects.server;

import java.awt.Component;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.ruby.platform.execution.DirectoryFileLocator;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
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
 * @todo Rewrite the WEBrick error message which says to press Ctrl-C to cancel the process;
 *   tell the user to use the Stop button in the margin instead (somebody on nbusers asked about this)
 * 
 * @author Tor Norbye, Pavel Buzek, Erno Mononen
 */
public final class RailsServerManager {
    
    enum ServerStatus { NOT_STARTED, STARTING, RUNNING; }

    private static final Logger LOGGER = Logger.getLogger(RailsServerManager.class.getName());
    
    /** Set of currently active - in use; ports. */
    private static final Set<Integer> IN_USE_PORTS = new HashSet<Integer>();;

    /**
     * The timeout in seconds for waiting a server to start.
     */
    private static final int SERVER_STARTUP_TIMEOUT = 120; 
    
    private ServerStatus status = ServerStatus.NOT_STARTED;
    private RubyServer server;
    
    /** True if server failed to start due to port conflict. */
    private boolean portConflict;
    
    /** User chosen port */
    private int originalPort;
    
    /** Actual port in use (trying other ports for ones not in use) */
    private int port = -1;
    
    private RailsProject project;
    private RubyExecution execution;
    private File dir;
    private boolean debug;
    private boolean switchToDebugMode;
    private Semaphore debugSemaphore;
    
    public RailsServerManager(RailsProject project) {
        this.project = project;
        dir = FileUtil.toFile(project.getProjectDirectory());
    }
    
    public synchronized void setDebug(boolean debug) {
        if (status == ServerStatus.RUNNING && !this.debug && debug) {
            switchToDebugMode = true;
        }
        this.debug = debug;
    }
    
    private void ensureRunning() {
        synchronized (RailsServerManager.this) {
            if (status == ServerStatus.STARTING) {
                return;
            } else if (status == ServerStatus.RUNNING) {
                if (switchToDebugMode) {
                    assert debugSemaphore == null : "startSemaphor supposed to be null";
                    debugSemaphore = new Semaphore(0);
                    switchToDebugMode = false;
                } else if (isPortInUse(port)) {
                    // Simply assume it is still the same server running
                    return;
                }
            }
        }
        if (debugSemaphore != null) {
            try {
                execution.kill();
                debugSemaphore.acquire();
                debugSemaphore = null;
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // Server was not started or was killed externally
        Runnable finishedAction =
            new Runnable() {
                public void run() {
                    synchronized (RailsServerManager.this) {
                        status = ServerStatus.NOT_STARTED;
                        if (server != null) {
                            server.removeApplication(port);
                        }
                        IN_USE_PORTS.remove(port);
                        if (portConflict) {
                            // Failed to start due to port conflict - notify user.
                            notifyPortConflict();
                        }
                        if (debugSemaphore != null) {
                            debugSemaphore.release();
                        } else {
                            debug = false;
                        }
                    }
                }
            };

        // Start the server
        synchronized (RailsServerManager.this) {
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
        String serverId = project.evaluator().getProperty(RailsProjectProperties.RAILS_SERVERTYPE);
        RubyPlatform platform = RubyPlatform.platformFor(project);
        RubyInstance instance = ServerRegistry.getDefault().getServer(serverId, platform);
        if (instance == null) {
            // TODO: need to inform the user somehow
            // fall back to the first available server
            List<? extends RubyInstance> availableServers = ServerRegistry.getDefault().getServers();
            for (RubyInstance each : availableServers) {
                if (each.isPlatformSupported(platform)) {
                    instance = each;
                    break;
                }
            }
            assert instance != null : "No servers found for " + platform;
        }
        if (!(instance instanceof RubyServer)){
            final Future<RubyInstance.OperationState> result = 
                    instance.runApplication(platform, projectName, dir);

            final RubyInstance serverInstance = instance;
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        RubyInstance.OperationState state = result.get();
                        if(state == RubyInstance.OperationState.COMPLETED) {
                            synchronized(RailsServerManager.this) {
                                port = serverInstance.getRailsPort();
                                status = ServerStatus.RUNNING;
                            }
                        } else {
                            synchronized(RailsServerManager.this) {
                                status = ServerStatus.NOT_STARTED;
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.INFO, ex.getMessage(), ex);
                        
                        // Ensure status value is reset on exceptions too...
                        synchronized(RailsServerManager.this) {
                            status = ServerStatus.NOT_STARTED;
                        }
                    }
                }
            });
                
            return;
        }
        server = (RubyServer) instance;
        String displayName = getServerTabName(server, projectName, port);
        String serverPath = server.getServerPath();
        ExecutionDescriptor desc = new ExecutionDescriptor(RubyPlatform.platformFor(project), displayName, dir, serverPath);
        desc.additionalArgs(buildStartupArgs());
        desc.postBuild(finishedAction);
        desc.classPath(classPath);
        desc.addStandardRecognizers();
        desc.addOutputRecognizer(new RailsServerRecognizer(server));
        desc.frontWindow(false);
        desc.debug(debug);
        desc.fastDebugRequired(debug);
        desc.fileLocator(new DirectoryFileLocator(FileUtil.toFileObject(dir)));
        //desc.showProgress(false); // http://ruby.netbeans.org/issues/show_bug.cgi?id=109261
        desc.showSuspended(true);
        String charsetName = project.evaluator().getProperty(RailsProjectProperties.SOURCE_ENCODING);
        IN_USE_PORTS.add(port);
        execution = new RubyExecution(desc, charsetName);
        execution.run();
    }
    
    private String[] buildStartupArgs() {
        List<String> result = new  ArrayList<String>();
        if (server.getStartupParam() != null) {
            result.add(server.getStartupParam());
        } 
        String railsEnv = project.evaluator().getProperty(RailsProjectProperties.RAILS_ENV);
        if (railsEnv != null && !"".equals(railsEnv.trim())) {
            result.add("-e");
            result.add(railsEnv);
        }
        result.add("--port");
        result.add(Integer.toString(port));
        return result.toArray(new String[result.size()]);
    }
    
    private static String getServerTabName(RubyServer server, String projectName, int port) {
        return NbBundle.getMessage(RailsServerManager.class, 
                "LBL_ServerTab" , server.getDisplayName(), projectName, String.valueOf(port));
    }
    
    private void notifyPortConflict() {
        String message = NbBundle.getMessage(RailsServerManager.class, "Conflict", Integer.toString(originalPort));
        NotifyDescriptor nd =
            new NotifyDescriptor.Message(message, 
            NotifyDescriptor.Message.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }

    /**
     * Starts the server if not running and shows url.
     * @param relativeUrl the resulting url will be for example: http://localhost:{port}/{relativeUrl}
     */
    public void showUrl(final String relativeUrl) {
        synchronized (RailsServerManager.this) {
            if (!switchToDebugMode && status == ServerStatus.RUNNING && isPortInUse(port)) {
                RailsServerManager.showURL(relativeUrl, port);
                return;
            }
        }
        ensureRunning();

        String displayName = NbBundle.getMessage(RailsServerManager.class, "ServerStartup");
        final ProgressHandle handle =
            ProgressHandleFactory.createHandle(displayName,new Cancellable() {
                    public boolean cancel() {
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
                    // Try connecting repeatedly, up to time specified 
                    // by SERVER_STARTUP_TIMEOUT, then bail
                    int i = 0;
                    for (; i <= SERVER_STARTUP_TIMEOUT; i++) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            // Don't worry about it
                        }

                        synchronized (RailsServerManager.this) {
                            if (status == ServerStatus.RUNNING) {
                                LOGGER.fine("Server " + server + " started in " + i + " seconds.");
                                RailsServerManager.showURL(relativeUrl, port);
                                return;
                            }

                            if (status == ServerStatus.NOT_STARTED) {
                               LOGGER.fine("Server starup failed, server type is: " + server);
                                // Server startup somehow failed...
                                break;
                            }
                        }
                    }

                    LOGGER.fine("Could not start " + server + " in " + i +
                            " seconds, current server status is " + status);
                    
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(RailsServerManager.class,
                                "NoServerFound", "http://localhost:" + port + "/" + relativeUrl));
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
        if (IN_USE_PORTS.contains(port)) {
            return true;
        }
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

    private static void showURL(String relativeUrl, int port) {
        LOGGER.fine("Opening URL: " + "http://localhost:" + port + "/" + relativeUrl);
        try {
            URL url = new URL("http://localhost:" + port + "/" + relativeUrl); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    /**
     * @param outputLine the output line to check.
     * @return true if the given <code>outputLine</code> represented 'address in use'
     * message.
     */
    static boolean isAddressInUseMsg(String outputLine){
        return outputLine.matches(".*in.*: Address.+in use.+(Errno::EADDRINUSE).*"); //NOI18N
    }
    
    private class RailsServerRecognizer extends OutputRecognizer {

        private final RubyServer server;
        
        RailsServerRecognizer(RubyServer server) {
            this.server = server;
        }

        @Override
        public ActionText processLine(String outputLine) {
            
            if (LOGGER.isLoggable(Level.FINEST)){
                LOGGER.log(Level.FINEST, "Processing output line: " + outputLine);
            }

            String line = outputLine;
            
            // This is ugly, but my attempts to use URLConnection on the URL repeatedly
            // and check for connection.getResponseCode()==HttpURLConnection.HTTP_OK didn't
            // work - try that again later
            if (server.isStartupMsg(outputLine)) {
                synchronized (RailsServerManager.this) {
                    LOGGER.fine("Identified " + server + " as running");
                    status = ServerStatus.RUNNING;
                    String projectName = project.getLookup().lookup(ProjectInformation.class).getDisplayName();
                    server.addApplication(new RailsApplication(projectName, port, execution));
                }
            } else if (isAddressInUseMsg(outputLine)) {
                LOGGER.fine("Detected port conflict: " + outputLine);
                portConflict = true;
            }

            if (!line.equals(outputLine)) {
                return new ActionText(new String[] { line }, null, null, null);
            }

            return null;
        }
    }

    public static JComboBox getServerComboBox(RubyPlatform platform) {
        JComboBox result = new JComboBox();
        if (platform != null) {
            result.setModel(new ServerListModel(platform));
        }
        result.setRenderer(new ServerListCellRendered());
        return result;
    }

    public static class ServerListModel extends AbstractListModel implements ComboBoxModel {

        private final List<? extends RubyInstance> servers;
        private Object selected;

        public ServerListModel(RubyPlatform platform) {
            this.servers = ServerRegistry.getDefault().getServers(platform);
            this.selected = servers.get(0);
        }

        public int getSize() {
            return servers.size();
        }

        public Object getElementAt(int index) {
            return servers.get(index);
        }

        public void setSelectedItem(Object server) {
            if (selected != server) {
                this.selected = server;
                fireContentsChanged(this, -1, -1);
            }
        }

        public Object getSelectedItem() {
            return selected;
        }
        
    }

    private static class ServerListCellRendered extends JLabel implements ListCellRenderer {

        public ServerListCellRendered() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            RubyInstance server = (RubyInstance) value;
            if (server != null) {
                setText(server.getDisplayName());
                setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            }
            return this;
        }
    }

    
}
