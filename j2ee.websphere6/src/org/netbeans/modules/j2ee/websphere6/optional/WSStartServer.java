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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.websphere6.optional;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import javax.enterprise.deploy.spi.status.*;

import org.openide.*;
import org.openide.util.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;

import org.netbeans.modules.j2ee.websphere6.*;
import org.netbeans.modules.j2ee.websphere6.ui.nodes.actions.ShowServerLogAction;
import org.netbeans.modules.j2ee.websphere6.util.*;
// Dileep - Start compile fix
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
// Dileep - Start compile fix

/**
 * This class provides functionality used to start and stop a particular server
 * instance. It also supports starting the server in debug mode, so that
 * NetBeans can connect to it using its built-in JPDA debugger.
 *
 * @author Kirill Sorokin
 */
public class WSStartServer extends StartServer {

    private static final Logger LOGGER = Logger.getLogger(WSStartServer.class.getName());

    /**
     * The server's deployment manager, to be exact the plugin's wrapper for it
     */
    private final WSDeploymentManager dm;
    
    /**
     * Current server's state. Can be either started, starting, stopping or
     * stopped
     */
    private int state;
    
    /**
     * Map containing markers that describe whether the server is started in debug mode
     */
    private static Map isDebugModeMap = Collections.synchronizedMap((Map)new HashMap(2,1));
    
    /**
     * Creates a new instance of WSStartServer
     *
     * @param the server's deployment manager
     */
    public WSStartServer(DeploymentManager dm) {
        // cast the deployment manager to the plugin's wrapper class and save
        this.dm = (WSDeploymentManager) dm;
        
        // get current server state
        this.state = isRunning() ? STATE_STARTED : STATE_STOPPED;
    }
    
    // overrides default StartServer method
    public boolean supportsStartDebugging(Target target) {
        return true;
    }
    
    /**
     * Starts the server (or even a particular target supplied by admin server)
     * in debug mode. We do not support this completely thus always start the
     * server instance defined in the deployment maanger supplied during
     * WSStartServer construction.
     *
     * @param target target thst should be started
     *
     * @return a progress object that describes the startup process
     */
    public ProgressObject startDebugging(Target target) {
        LOGGER.log(Level.FINEST, "Starting server in debug mode"); // NOI18N

        // create a new progress object
        WSServerProgress serverProgress = new WSServerProgress(this);
        
        // send a message denoting that the startup process has begun
        serverProgress.notifyStart(StateType.RUNNING,  "");            // NOI18N
        
        // if the server is stopping
        if (state == STATE_STOPPING) {
            // wait till the startup completes or times out
            for (int i = 0; i < WSStopRunnable.TIMEOUT; i += WSStopRunnable.DELAY) {
                if (state == STATE_STOPPING) {
                    try {
                        Thread.sleep(WSStopRunnable.DELAY);
                    } catch (InterruptedException e) {
                        // do nothing just skip the sleeping phase
                    }
                }
                
                if (state == STATE_STOPPED) {
                    break;
                }
            }
        }
        
        // if the server is started or starting
        if (state == STATE_STARTING || state == STATE_STARTED) {
            // notify the progress object that the process is already
            // completed and return
            serverProgress.notifyStart(StateType.COMPLETED, "");
            return serverProgress;
        }
        
        // run the startup process in a separate thread
        RequestProcessor.getDefault().post(new WSStartDebugRunnable(
                serverProgress), 0, Thread.NORM_PRIORITY);
        
        // set the debuggable marker
        // isDebuggable = true;
        isDebugModeMap.put(dm.getHost() + dm.getPort() + dm.getDomainRoot(), new Object());
        
        // set the state to starting
        state = STATE_STARTING;
        
        // return the progress object
        return serverProgress;
    }
    
    
    
    /**
     * Specifies whether the server instance is started in debug mode. The
     * detalization can go as deep as an individual target, but we do not
     * support this
     *
     * @param target target to be checked
     *
     * @return whether the instance is started in debug mode
     */
    public boolean isDebuggable(Target target) {
        
        if(!isDebugModeMap.containsKey(dm.getHost() + dm.getPort() + dm.getDomainRoot())){
            return false;
        }
        
        if(!isRunning()){
            isDebugModeMap.remove(dm.getHost() + dm.getPort() + dm.getDomainRoot());
            return false;
        }
        return true;
    }
    
    
    
    /**
     * Tells whether the target is also the target server
     *
     * @return true
     */
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    /**
     * Returns the information for attaching the JPDA debugger to the server
     * (host and port). The detalization can be down to a specific target, but
     * we do not support this
     *
     * @param target target for which the information is requested
     *
     * @return the debug information
     */
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo(dm.getHost(), new Integer(
                dm.getInstanceProperties().getProperty(
                WSDeploymentFactory.DEBUGGER_PORT_ATTR)).intValue());
    }
    
    /**
     * Tells whether the normal startup of the server is supported.
     *
     * @return if the server is local, its true, false otherwise
     */
    public boolean supportsStartDeploymentManager() {
        // if the server is local we can start it
        InstanceProperties ip = dm.getInstanceProperties();
        if (ip != null && Boolean.parseBoolean(ip.getProperty(
                WSDeploymentFactory.IS_LOCAL_ATTR))) {   // NOI18N
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Stops the server instance identified by the deployment manager.
     *
     * @return the progress object describing the shutdown process
     */
    public ProgressObject stopDeploymentManager() {
        LOGGER.log(Level.FINEST, "Stopping server"); // NOI18N

        // create a new progress object
        WSServerProgress serverProgress = new WSServerProgress(this);
        
        // send a message denoting that the shutdown process has begun
        serverProgress.notifyStop(StateType.RUNNING, "");              // NOI18N
        
        // if the server is starting
        if (state == STATE_STARTING) {
            // wait till the startup completes or times out
            for (int i = 0; i < WSStartRunnable.TIMEOUT; i += WSStartRunnable.DELAY) {
                if (state == STATE_STARTING) {
                    try {
                        Thread.sleep(WSStartRunnable.DELAY);
                    } catch (InterruptedException e) {
                        // do nothing just skip the sleeping phase
                    }
                }
                
                if (state == STATE_STARTED) {
                    break;
                }
            }
        }
        
        // if the server is stopped or stopping
        if (state == STATE_STOPPING || state == STATE_STOPPED) {
            // notify the progress object that the process is already
            // completed and return
            serverProgress.notifyStop(StateType.COMPLETED, "");
            return serverProgress;
        }
        
        // run the shutdown process in a separate thread
        RequestProcessor.getDefault().post(new WSStopRunnable(serverProgress),
                0, Thread.NORM_PRIORITY);
        
        // set the debugguable marker to false as the server is stopped
        // isDebuggable = false;
        isDebugModeMap.remove(dm.getHost() + dm.getPort() + dm.getDomainRoot());
        
        // set the state to stopping
        state = STATE_STOPPING;
        
        // return the progress object
        return serverProgress;
    }
    
    /**
     * Starts a server instance identified by the deployment manager.
     *
     * @return a progress object describing the server startup process
     */
    public ProgressObject startDeploymentManager() {
        LOGGER.log(Level.FINEST, "Starting server"); // NOI18N

        // create a new progress object
        WSServerProgress serverProgress = new WSServerProgress(this);
        
        // send a message denoting that the startup process has begun
        serverProgress.notifyStart(StateType.RUNNING,  ""); // NOI18N
        
        // if the server is stopping
        if (state == STATE_STOPPING) {
            // wait till the startup completes or times out
            for (int i = 0; i < WSStopRunnable.TIMEOUT; i += WSStopRunnable.DELAY) {
                if (state == STATE_STOPPING) {
                    try {
                        Thread.sleep(WSStopRunnable.DELAY);
                    } catch (InterruptedException e) {
                        // do nothing just skip the sleeping phase
                    }
                }
                
                if (state == STATE_STOPPED) {
                    break;
                }
            }
        }
        
        // if the server is started or starting
        if (state == STATE_STARTING || state == STATE_STARTED) {
            // notify the progress object that the process is already
            // completed and return
            serverProgress.notifyStart(StateType.COMPLETED, "");
            return serverProgress;
        }
        
        // run the startup process in a separate thread
        RequestProcessor.getDefault().post(new WSStartRunnable(serverProgress),
                0, Thread.NORM_PRIORITY);
        
        // set the debuggble marker to false as we do not start the server in
        // debug mode
        // isDebuggable = false;
        isDebugModeMap.remove(dm.getHost() + dm.getPort() + dm.getDomainRoot());
        
        // set the state to starting
        state = STATE_STARTING;
        
        // return the progress object
        return serverProgress;
    }
    
    /**
     * Tells whether we need to start the server instance in order to get a
     * list of deployment targets.
     *
     * @return true
     */
    public boolean needsStartForTargetList() {
        return true;
    }
    
    /**
     * Tells whether we need to start the server instance in order to configure
     * an application
     *
     * @return false
     */
    public boolean needsStartForConfigure() {
        return false;// modified. was true
    }
    
    /**
     * Tells whether we need to start the server instance in order to configure
     * the admin server
     *
     * @return true
     */
    public boolean needsStartForAdminConfig() {
        return true;
    }
    
    /**
     * Tells whether the server instance identified by the deployment manager is
     * currently started
     *
     * @return true is the server is running, false otherwise
     */
    public boolean isRunning() {
        // try to get an open socket to the target host/port
        try {
            new Socket(dm.getHost(), Integer.parseInt(dm.getPort()));
            
            // if we are successful, return true
            return true;
        } catch (UnknownHostException e) {
            Logger.getLogger("global").log(Level.SEVERE, null, e);
        } catch (IOException e) {
            // do nothing this exception means that the server is
            // not started
        }
        
        // we failed to create a socket thus the server is not started
        return false;
    }
    
    /**
     * An utility method that transforms a Properties object to an array of
     * Strings each containing a property in the form of
     * <i>'&lt;name&gt;=&lt;value&gt;'</i>
     *
     * @param properties properties to be converted
     *
     * @return an array of strings with the converted properties
     */
    private static String[] properties2StringArray(Properties properties) {
        // initiate the list that will be converted to array for returning
        List list = new ArrayList();
        
        // get the properties keys
        Enumeration keys = properties.keys();
        
        // iterate over the enumeration
        while (keys.hasMoreElements()) {
            // get the property key
            String key = (String) keys.nextElement();
            
            // get the value associated with this key and store them in the list
            list.add(key + "=" + properties.getProperty(key));         // NOI18N
        }
        
        // convert the list into an array and return
        return (String[]) list.toArray(new String[list.size()]);
    }
    
    /**
     * Turns on/off the debugging mode of the server.
     *
     * @param degubEnabled whether the server should be started in debug mode
     * @param debuggerPort which port the debugger should listen to
     */
    private void setDebugMode(boolean debugEnabled, int debuggerPort) {
        // get the config file path from the instance properties
        File configXmlFile = new File(dm.getInstanceProperties().getProperty(
                WSDeploymentFactory.CONFIG_XML_PATH));
        
        // get the current file's contents
        String contents = WSUtil.readFile(configXmlFile);
        
        // replace the attributes that relate to debugging mode of the server
        if (debugEnabled) {
            contents = contents.replaceAll("debugMode=\"false\"", "debugMode=\"true\"");
            contents = contents.replaceFirst("suspend=n,address=[0-9]+\" genericJvmArguments", "suspend=n,address=" + debuggerPort + "\" genericJvmArguments");
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "Setting the address string to: " + "suspend=n,address=" + debuggerPort);
            }
        } else {
            contents = contents.replaceAll("debugMode=\"true\"", "debugMode=\"false\"");
        }
        
        // write the replaced contents back to the file
        WSUtil.writeFile(configXmlFile, contents);
    }
    
    /**
     * Runnable that starts the server in normal mode. It is used to start the
     * server in a separate thread, so that the IDE does not hang up during the
     * startup process.
     *
     * @author Kirill Sorokin
     */
    private class WSStartRunnable implements Runnable {
        
        /**
         * Root directory for the selected profile
         */
        private String domainHome;
        
        /**
         * Name of the selected server instance, it will be used as the
         * parameter for the startup script
         */
        private String serverName;
        
        /**
         * Progress object that describes the startup process. It will be
         * notified of the progress and the success/failure of the process
         */
        private WSServerProgress serverProgress;
        
        /**
         * Creates a new instance of WSStartRunnable.
         *
         * @param serverProgress the prgress object that the thread should
         *      notify of anything that happens with the startup process
         */
        public WSStartRunnable(WSServerProgress serverProgress) {
            // save the progress object
            this.serverProgress = serverProgress;
            
            // get the profile root directory and the instance name from the
            // deployment manager
            domainHome = dm.getInstanceProperties().getProperty(
                    WSDeploymentFactory.DOMAIN_ROOT_ATTR);
            serverName = dm.getInstanceProperties().getProperty(
                    WSDeploymentFactory.SERVER_NAME_ATTR);
        }
        
        /**
         * Implementation of the run() method from the Runnable interface
         */
        public void run() {
            try {
                // save the current time so that we can deduct that the startup
                // failed due to timeout
                long start = System.currentTimeMillis();
                
                // set the server's debug mode to false
                setDebugMode(false, 0);
                
                // create the startup process
                Process serverProcess = Runtime.getRuntime().
                        exec(new String[]{domainHome + "/bin/" +       // NOI18N
                                (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH),
                        serverName});
                
                // create a tailer to the server's output stream so that a user
                // can observe the progress
                new WSTailer(serverProcess.getInputStream(), NbBundle.
                        getMessage(WSStartServer.class,
                        "TXT_ioWindowTitle",dm.getServerTitleMessage())).start();                 // NOI18N
                
                // show the server's log
                
                new WSTailer(new File(dm.getLogFilePath()),
                        NbBundle.getMessage(ShowServerLogAction.class,
                        "LBL_LogWindowTitle", dm.getServerTitleMessage())).start();
                
                // wait till the timeout happens, or if the server starts before
                // send the completed event to j2eeserver
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    // is the server is not yet running send the 'progressing'
                    // event, else send trhe 'completed' event and return
                    if (!isRunning()) {
                        serverProgress.notifyStart(StateType.RUNNING,
                                "");                                   // NOI18N
                    } else {
                        serverProgress.notifyStart(StateType.COMPLETED,
                                "");                                   // NOI18N
                        
                        // set the state to started
                        state = STATE_STARTED;
                        
                        return;
                    }
                    
                    // sleep for a little so that we do not make our checks too
                    // often
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {}
                }
                
                // if the server did not start in the designated time limits
                // we consider the startup as failed and kill the process
                serverProgress.notifyStart(StateType.FAILED,
                        NbBundle.getMessage(WSStartServer.class, "MSG_StartFailed"));
                serverProcess.destroy();
                
                // set the state to stopped
                state = STATE_STOPPED;
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
        }
        
        /**
         * The amount of time in milliseconds during which the server should
         * start
         */
        private static final int TIMEOUT = 120000;
        
        /**
         * The amount of time in milliseconds that we should wait between checks
         */
        private static final int DELAY = 5000;
        
        /**
         * Name of the startup script for windows
         */
        private static final String STARTUP_SH = "startServer.sh";     // NOI18N
        
        /**
         * Name of the startup script for Unices
         */
        private static final String STARTUP_BAT = "startServer.bat";   // NOI18N
    }
    
    /**
     * Runnable that starts the server in debug mode. It is used to start the
     * server in a separate thread, so that the IDE does not hang up during the
     * startup process.
     *
     * @author Kirill Sorokin
     */
    private class WSStartDebugRunnable implements Runnable {
        
        /**
         * Root directory for the selected profile
         */
        private String domainHome;
        
        /**
         * Name of the selected server instance, it will be used as the
         * parameter for the startup script
         */
        private String serverName;
        
        /**
         * The debugger port that the JPDA debugger should connect to, basically
         * this integer will be added to the server's startup command line, to
         * make the JVM listen for debugger connection on this port
         */
        private String debuggerPort;
        
        /**
         * Progress object that describes the startup process. It will be
         * notified of the progress and the success/failure of the process
         */
        private WSServerProgress serverProgress;
        
        /**
         * Creates a new instance of WSStartDebugRunnable.
         *
         * @param serverProgress the prgress object that the thread should
         *      notify of anything that happens with the startup process
         */
        public WSStartDebugRunnable(WSServerProgress serverProgress) {
            // save the progress object
            this.serverProgress = serverProgress;
            
            // get the profile root directory, the debugger port and the
            // instance name from the deployment manager
            domainHome = dm.getInstanceProperties().getProperty(
                    WSDeploymentFactory.DOMAIN_ROOT_ATTR);
            debuggerPort = dm.getInstanceProperties().getProperty(
                    WSDeploymentFactory.DEBUGGER_PORT_ATTR);
            serverName = dm.getInstanceProperties().getProperty(
                    WSDeploymentFactory.SERVER_NAME_ATTR);
        }
        
        /**
         * Implementation of the run() method from the Runnable interface
         */
        public void run() {
            try {
                // save the current time so that we can deduct that the startup
                // failed due to timeout
                long start = System.currentTimeMillis();
                
                // set the server's debug mode to true
                setDebugMode(true, new Integer(debuggerPort).intValue());
                
                // create the startup process
                Process serverProcess = Runtime.getRuntime().
                        exec(new String[]{domainHome + "/bin/" +       // NOI18N
                                (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH),
                        serverName});
                
                // create a tailer to the server's output stream so that a user
                // can observe the progress
                
                new WSTailer(serverProcess.getInputStream(),
                        NbBundle.getMessage(WSStartServer.class,
                        "TXT_ioWindowTitle",dm.getServerTitleMessage())).start();                 // NOI18N
                
                // show the server's log
                new WSTailer(new File(dm.getLogFilePath()),
                        NbBundle.getMessage(ShowServerLogAction.class,
                        "LBL_LogWindowTitle", dm.getServerTitleMessage())).
                        start();
                
                // wait till the timeout happens, or if the server starts before
                // send the completed event to j2eeserver
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    // is the server is not yet running send the 'progressing'
                    // event, else send trhe 'completed' event and return
                    if (!isRunning()) {
                        serverProgress.notifyStart(StateType.RUNNING,
                                "");                                   // NOI18N
                    } else {
                        serverProgress.notifyStart(StateType.COMPLETED,
                                "");                                   // NOI18N
                        
                        // set the state to started
                        state = STATE_STARTED;
                        
                        return;
                    }
                    
                    // sleep for a little so that we do not make our checks too
                    // often
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {}
                }
                
                // if the server did not start in the designated time limits
                // we consider the startup as failed and kill the process
                serverProgress.notifyStart(StateType.FAILED, "");      // NOI18N
                serverProcess.destroy();
                
                // set the state to stopped
                state = STATE_STOPPED;
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
        }
        
        /**
         * The amount of time in milliseconds during which the server should
         * start
         */
        private static final int TIMEOUT = 120000;
        
        /**
         * The amount of time in milliseconds that we should wait between checks
         */
        private static final int DELAY = 5000;
        
        /**
         * Name of the startup script for windows
         */
        private static final String STARTUP_SH = "startServer.sh";     // NOI18N
        
        /**
         * Name of the startup script for Unices
         */
        private static final String STARTUP_BAT = "startServer.bat";   // NOI18N
    }
    
    /**
     * Runnable that stops the server. It is used to stop the server in a
     * separate thread, so that the IDE does not hang up during the stop
     * process.
     *
     * @author Kirill Sorokin
     */
    private class WSStopRunnable implements Runnable {
        
        /**
         * Root directory for the selected profile
         */
        private String domainHome;
        
        /**
         * Name of the selected server instance, it will be used as the
         * parameter for the stop script
         */
        private String serverName;
        
        /**
         * Progress object that describes the stop process. It will be
         * notified of the progress and the success/failure of the process
         */
        private WSServerProgress serverProgress;
        
        /**
         * Creates a new instance of WSStopRunnable.
         *
         * @param serverProgress the prgress object that the thread should
         *      notify of anything that happens with the stop process
         */
        public WSStopRunnable(WSServerProgress serverProgress) {
            // save the progress pbject
            this.serverProgress = serverProgress;
            
            // get the profile home directory and the instance name from the
            // deployment manager
            domainHome = dm.getInstanceProperties().getProperty(
                    WSDeploymentFactory.DOMAIN_ROOT_ATTR);
            serverName = dm.getInstanceProperties().getProperty(
                    WSDeploymentFactory.SERVER_NAME_ATTR);
        }
        
        /**
         * Implementation of the run() method from the Runnable interface
         */
        public void run() {
            try {
                // save the current time so that we can deduct that the startup
                // failed due to timeout
                long start = System.currentTimeMillis();
                
                // create the stop process
                Process serverProcess = Runtime.getRuntime().exec(
                        new String[]{domainHome + "/bin/" +            // NOI18N
                                (Utilities.isWindows() ? SHUTDOWN_BAT : SHUTDOWN_SH),
                        serverName});
                
                // create a tailer to the server's output stream so that a user
                // can observe the progress
                
                new WSTailer(serverProcess.getInputStream(),
                        NbBundle.getMessage(WSStartServer.class,
                        "TXT_ioWindowTitle", dm.getServerTitleMessage())).start();                 // NOI18N
                
                // show the server's log
                new WSTailer(new File(dm.getLogFilePath()),
                        NbBundle.getMessage(ShowServerLogAction.class,
                        "LBL_LogWindowTitle",  dm.getServerTitleMessage())).start();
                
                // wait till the timeout happens, or if the server starts before
                // send the completed event to j2eeserver
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    if (isRunning()) {
                        serverProgress.notifyStop(StateType.RUNNING, ""); // NOI18N
                    } else {
                        serverProgress.notifyStop(StateType.COMPLETED, ""); // NOI18N
                        
                        // set the state to stopped
                        state = STATE_STOPPED;
                        
                        return;
                    }
                    
                    // sleep for a little so that we do not make our checks too
                    // often
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {}
                }
                
                // if the server did not stop in the designated time limits
                // we consider the stop process as failed and kill the process
                serverProgress.notifyStop(StateType.FAILED,
                        NbBundle.getMessage(WSStartServer.class, "MSG_StartFailed"));
                serverProcess.destroy();
                
                // set the state to started
                state = STATE_STARTED;
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
        }
        
        /**
         * The amount of time in milliseconds during which the server should
         * stop
         */
        private static final int TIMEOUT = 120000;
        
        /**
         * The amount of time in milliseconds that we should wait between checks
         */
        private static final int DELAY = 5000;
        
        /**
         * Name of the shutdown script for windows
         */
        private static final String SHUTDOWN_SH = "stopServer.sh";     // NOI18N
        
        /**
         * Name of the shutdown script for unices
         */
        private static final String SHUTDOWN_BAT = "stopServer.bat";   // NOI18N
    }
    
    /**
     * An implementation of the ProgressObject interface targeted at tracking
     * the server instance's startup/shutdown progress
     *
     * @author Kirill Sorokin
     */
    private static class WSServerProgress implements ProgressObject {
        
        /**
         * Listeners vector
         */
        private Vector listeners = new Vector();
        
        /**
         * Current startus of the startup/shutdown process
         */
        private DeploymentStatus deploymentStatus;
        
        /**
         * Progress events source
         */
        private Object source;
        
        /**
         * Creates a new instance of WSServerProgress. The source supplied will
         * be used as the source for all the events. Ususally it is the parent
         * WSStartServerObject
         *
         * @param source the events' source
         */
        public WSServerProgress(Object source) {
            this.source = source;
        }
        
        /**
         * Sends a startup event to the listeners.
         *
         * @param state the new state of the startup process
         * @param message the attached string message
         */
        public void notifyStart(StateType state, String message) {
            // call the general notify method with the specific startup event
            // parameters set
            notify(new WSDeploymentStatus(ActionType.EXECUTE, CommandType.START, state, message));
        }
        
        /**
         * Sends a shutdown event to the listeners.
         *
         * @param state the new state of the shutdown process
         * @param message the attached string message
         */
        public void notifyStop(StateType state, String message) {
            // call the general notify method with the specific shutdown event
            // parameters set
            notify(new WSDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, state, message));
        }
        
        /**
         * Notifies the listeners of the new process status
         *
         * @param deploymentStatus the new status of the startup/shutdown
         *      process
         */
        public void notify(DeploymentStatus deploymentStatus) {
            // construct a new progress event from the source and the supplied
            // new process status
            ProgressEvent evt = new ProgressEvent(source, null, deploymentStatus);
            
            // update the saved process status
            this.deploymentStatus = deploymentStatus;
            
            // get a copy of the listeners vector so that we do not get any
            // conflicts when multithreading
            java.util.Vector targets = null;
            synchronized (this) {
                if (listeners != null) {
                    targets = (java.util.Vector) listeners.clone();
                }
            }
            
            // traverse the listeners, notifying each with the new event
            if (targets != null) {
                for (int i = 0; i < targets.size(); i++) {
                    ProgressListener target = (ProgressListener) targets.elementAt(i);
                    target.handleProgressEvent(evt);
                }
            }
        }
        
        ////////////////////////////////////////////////////////////////////////////
        // ProgressObject implementation
        ////////////////////////////////////////////////////////////////////////////
        /**
         * A dummy implementation of the ProgressObject method, since this
         * method is not used anywhere, we omit the reasonable implementation
         */
        public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
            return null;
        }
        
        /**
         * Removes the registered listener
         *
         * @param progressListener the listener to be removed
         */
        public void removeProgressListener(ProgressListener progressListener) {
            listeners.remove(progressListener);
        }
        
        /**
         * Adds a new listener
         *
         * @param progressListener the listener to be added
         */
        public void addProgressListener(ProgressListener progressListener) {
            listeners.add(progressListener);
        }
        
        /**
         * Returns the current state of the startup/shutdown process
         *
         * @return current state of the process
         */
        public DeploymentStatus getDeploymentStatus() {
            return deploymentStatus;
        }
        
        /**
         * A dummy implementation of the ProgressObject method, since this
         * method is not used anywhere, we omit the reasonable implementation
         */
        public TargetModuleID[] getResultTargetModuleIDs() {
            return new TargetModuleID[]{};
        }
        
        /**
         * A dummy implementation of the ProgressObject method, since this
         * method is not used anywhere, we omit the reasonable implementation
         */
        public boolean isStopSupported() {
            return false;
        }
        
        /**
         * A dummy implementation of the ProgressObject method, since this
         * method is not used anywhere, we omit the reasonable implementation
         */
        public void stop() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("");               // NOI18N
        }
        
        /**
         * A dummy implementation of the ProgressObject method, since this
         * method is not used anywhere, we omit the reasonable implementation
         */
        public boolean isCancelSupported() {
            return false;
        }
        
        /**
         * A dummy implementation of the ProgressObject method, since this
         * method is not used anywhere, we omit the reasonable implementation
         */
        public void cancel() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("");               // NOI18N
        }
    }
    
    /**
     * A class that describes the startup/shutdown process state. It is an
     * implementation of the DeploymentStatus interface.
     */
    private static class WSDeploymentStatus implements DeploymentStatus {
        /**
         * Current action
         */
        private ActionType action;
        
        /**
         * Current command
         */
        private CommandType command;
        
        /**
         * Current state
         */
        private StateType state;
        
        /**
         * Current message
         */
        private String message;
        
        /**
         * Creates a new WSDeploymentStatus object.
         *
         * @param action current action
         * @param command current command
         * @param state current state
         * @param message current message
         */
        public WSDeploymentStatus(ActionType action, CommandType command, StateType state, String message) {
            // save the supplied parameters
            this.action = action;
            this.command = command;
            this.state = state;
            this.message = message;
        }
        
        /**
         * Returns the current action
         *
         * @return current action
         */
        public ActionType getAction() {
            return action;
        }
        
        /**
         * Returns the current command
         *
         * @return current command
         */
        public CommandType getCommand() {
            return command;
        }
        
        /**
         * Returns the current message
         *
         * @return current message
         */
        public String getMessage() {
            return message;
        }
        
        /**
         * Returns the current state
         *
         * @return current state
         */
        public StateType getState() {
            return state;
        }
        
        /**
         * Tells whether the current action has completed successfully
         *
         * @return true if the action has completed successfully, false
         *      otherwise
         */
        public boolean isCompleted() {
            return StateType.COMPLETED.equals(state);
        }
        
        /**
         * Tells whether the current action has failed
         *
         * @return true if the action has failed, false otherwise
         */
        public boolean isFailed() {
            return StateType.FAILED.equals(state);
        }
        
        /**
         * Tells whether the current action is still running
         *
         * @return true if the action is still running, false otherwise
         */
        public boolean isRunning() {
            return StateType.RUNNING.equals(state);
        }
    };
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants section
    ////////////////////////////////////////////////////////////////////////////
    // These constants introduce two additional states to the instance -
    // starting and stopping son that we can filter sunsequent requests.
    private static final int STATE_STOPPED  = 0;
    private static final int STATE_STARTING = 1;
    private static final int STATE_STARTED  = 2;
    private static final int STATE_STOPPING = 3;
}
