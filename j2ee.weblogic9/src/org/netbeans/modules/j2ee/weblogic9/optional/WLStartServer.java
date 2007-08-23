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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9.optional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.util.WLDebug;
import org.netbeans.modules.j2ee.weblogic9.util.WLTailer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * This class provides functionality used to start and stop a particular server
 * instance. It also supports starting the server in debug mode, so that 
 * NetBeans can connect to it using its built-in JPDA debugger.
 * 
 * @author Kirill Sorokin
 */
public class WLStartServer extends StartServer {
    
    private static final int SERVER_CHECK_TIMEOUT = 2000;
    
    /**
     * The server's deployment manager, to be exact the plugin's wrapper for it
     */
    private WLDeploymentManager dm;
    
    /**
     * WL server process instance
     */
    private Process serverProcess;
    
    /**
     * Creates a new instance of WLStartServer
     * 
     * @param the server's deployment manager
     */
    public WLStartServer(DeploymentManager dm) {
        // cast the deployment manager to the plugin's wrapper class and save
        this.dm = (WLDeploymentManager) dm;
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
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify(getClass(), "starting server in " +         // NOI18N
                    "debug mode");                                     // NOI18N
        
        // create a new progress object
        WLServerProgress serverProgress = new WLServerProgress(this);
        
        // send a message denoting that the startup process has begun
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        serverProgress.notifyStart(StateType.RUNNING,  NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName)); // NOI18N
        
        // run the startup process in a separate thread
        RequestProcessor.getDefault().post(new WLStartDebugRunnable(
                serverProgress), 0, Thread.NORM_PRIORITY);
        
        // set the debuggable marker
        isDebuggable = true;
        
        // return the progress object
        return serverProgress;
    }
    
    /**
     * Marker describing whether the server is started in debug mode
     */
    private boolean isDebuggable;
    
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
        return isDebuggable;
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
                WLPluginProperties.DEBUGGER_PORT_ATTR)).intValue());
    }
    
    /**
     * Tells whether the normal startup of the server is supported.
     * 
     * @return if the server is local, its true, false otherwise
     */
    public boolean supportsStartDeploymentManager() {
        // if the server is local we can start it
        if (dm.getInstanceProperties().getProperty(
                WLPluginProperties.IS_LOCAL_ATTR).equals("true")) {   // NOI18N
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
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify(getClass(), "stopping server!");            // NOI18N
        
        // create a new progress object
        WLServerProgress serverProgress = new WLServerProgress(this);
        
        // send a message denoting that the shutdown process has begun
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        serverProgress.notifyStart(StateType.RUNNING,  NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)); // NOI18N
        
        // run the shutdown process in a separate thread
        RequestProcessor.getDefault().post(new WLStopRunnable(serverProgress), 0, Thread.NORM_PRIORITY);
        
        // set the debugguable marker to false as the server is stopped
        isDebuggable = false;
        
        // return the progress object
        return serverProgress;
    }
    
    /**
     * Starts a server instance identified by the deployment manager.
     * 
     * @return a progress object describing the server startup process
     */
    public ProgressObject startDeploymentManager() {
        if (WLDebug.isEnabled()) // debug output
            WLDebug.notify(getClass(), "starting server!");            // NOI18N
        
        // create a new progress object
        WLServerProgress serverProgress = new WLServerProgress(this);
        
        // send a message denoting that the startup process has begun
        String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        serverProgress.notifyStart(StateType.RUNNING,  NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName)); // NOI18N
        
        // run the startup process in a separate thread
        RequestProcessor.getDefault().post(new WLStartRunnable(
                serverProgress), 0, Thread.NORM_PRIORITY);
        
        // set the debuggble marker to false as we do not start the server in 
        // debug mode
        isDebuggable = false;
        
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
        return false;
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
        return isRunning(true);
    }
    
    /**
     * Returns true if the server is running.
     *
     * @param checkResponse should be checked whether is the server responding - is really up?
     * @return <code>true</code> if the server is running.
     */
    public boolean isRunning(boolean checkResponse) {
        Process proc = dm.getServerProcess();
        if (proc != null) {
            try {
                proc.exitValue();
                // process is stopped
                return false;
            } catch (IllegalThreadStateException e) {
                // process is running
                if (!checkResponse) {
                    return true;
                }
            }
        }
        if (checkResponse) {
            String host = dm.getHost();
            int port = new Integer(dm.getPort()).intValue();
            return ping(host, port, SERVER_CHECK_TIMEOUT); // is server responding?
        } else {
            return false; // cannot resolve the state
        }
    }

    /** Return true if the server is stopped. If the server was started from within
     * the IDE, determin the server state from the process exit code, otherwise try
     * to ping it. 
     */
    private boolean isStopped() {
        Process proc = dm.getServerProcess();
        if (proc != null) {
            try {
                proc.exitValue();
                // process is stopped
                return true;
            } catch (IllegalThreadStateException e) { 
                // process is still running
                return false;
            }
        } else {
            String host = dm.getHost();
            int port = new Integer(dm.getPort()).intValue();
            return !ping(host, port, SERVER_CHECK_TIMEOUT); // is server responding?
        }
    }
    
    /** Return true if a WL server is running on the specifed host:port */
    public static boolean ping(String host, int port, int timeout) {
        // checking whether a socket can be created is not reliable enough, see #47048
        Socket socket = new Socket();
        try {
            try {
                socket.connect(new InetSocketAddress(host, port), timeout); // NOI18N
                socket.setSoTimeout(timeout);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    try {
                        // request for the login form - we guess that OK response means pinging the WL server
                        out.println("GET /console/login/LoginForm.jsp HTTP/1.1\nHost:\n"); // NOI18N

                        // check response
                        return "HTTP/1.1 200 OK".equals(in.readLine()); // NOI18N
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
            list.add(key + "=" + properties.getProperty(key));
        }
        
        // convert the list into an array and return
        return (String[]) list.toArray(new String[list.size()]);
    }

    public boolean supportsStartDebugging(Target target) {
        //if we can start it we can debug it
        return supportsStartDeploymentManager();
    }
    
    /**
     * Runnable that starts the server in normal mode. It is used to start the 
     * server in a separate thread, so that the IDE does not hang up during the
     * startup process.
     * 
     * @author Kirill Sorokin
     */
    private class WLStartRunnable implements Runnable {
        
        /**
         * Root directory for the selected profile
         */ 
        private String domainHome;
        
        /**
         * Progress object that describes the startup process. It will be 
         * notified of the progress and the success/failure of the process
         */
        private WLServerProgress serverProgress;
        
        /** 
         * Creates a new instance of WSStartRunnable.
         * 
         * @param serverProgress the prgress object that the thread should 
         *      notify of anything that happens with the startup process
         */
        public WLStartRunnable(WLServerProgress serverProgress) {
            // save the progress object
            this.serverProgress = serverProgress;
            
            // get the profile root directory and the instance name from the
            // deployment manager
            domainHome = dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DOMAIN_ROOT_ATTR);
        }
        
        /**
         * Implementation of the run() method from the Runnable interface
         */
        public void run() {
            try {
                // save the current time so that we can deduct that the startup
                // failed due to timeout
                long start = System.currentTimeMillis();
                
                // create the startup process
                serverProcess = Runtime.getRuntime().exec(
                        domainHome + "/" + (Utilities.isWindows() ?    // NOI18N
                            STARTUP_BAT : STARTUP_SH)); 
                
                dm.setServerProcess(serverProcess);
                
                // create a tailer to the server's output stream so that a user
                // can observe the progress
                new WLTailer(serverProcess.getInputStream(), dm.getURI());
                
                String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);

                // wait till the timeout happens, or if the server starts before 
                // send the completed event to j2eeserver
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    // send the 'completed' event and return when the server is running
                    if (isRunning()) {
                        serverProgress.notifyStart(StateType.COMPLETED, NbBundle.getMessage(WLStartServer.class, "MSG_SERVER_STARTED", serverName)); // NOI18N
                        return;
                    }

                    // sleep for a little so that we do not make our checks too
                    // often
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {}
                }

                // if the server did not start in the designated time limits
                // we consider the startup as failed and warn the user
                serverProgress.notifyStart(StateType.FAILED, NbBundle.getMessage(WLStartServer.class, "MSG_StartServerTimeout"));
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
        }
        
        /**
         * The amount of time in milliseconds during which the server should
         * start
         */
        private static final int TIMEOUT = 900000;
        
        /**
         * The amount of time in milliseconds that we should wait between checks
         */
        private static final int DELAY = 5000;
        
        /** 
         * Name of the startup script for windows
         */
        private static final String STARTUP_SH = "startWebLogic.sh";   // NOI18N
        
        /**
         * Name of the startup script for Unices
         */
        private static final String STARTUP_BAT = "startWebLogic.cmd"; // NOI18N
    }
    
    /**
     * Runnable that starts the server in debug mode. It is used to start the 
     * server in a separate thread, so that the IDE does not hang up during the
     * startup process.
     * 
     * @author Kirill Sorokin
     */
    private class WLStartDebugRunnable implements Runnable {
        
        /**
         * Root directory for the selected profile
         */ 
        private String domainHome;
        
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
        private WLServerProgress serverProgress;
        
        /** 
         * Creates a new instance of WSStartDebugRunnable.
         * 
         * @param serverProgress the prgress object that the thread should 
         *      notify of anything that happens with the startup process
         */
        public WLStartDebugRunnable(WLServerProgress serverProgress) {
            // save the progress object
            this.serverProgress = serverProgress;
            
            // get the profile root directory, the debugger port and the 
            // instance name from the deployment manager
            domainHome = dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DOMAIN_ROOT_ATTR);
            debuggerPort = dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DEBUGGER_PORT_ATTR);
        }
        
        /**
         * Implementation of the run() method from the Runnable interface
         */
        public void run() {
            try {
                // save the current time so that we can deduct that the startup
                // failed due to timeout
                long start = System.currentTimeMillis();
                
                
                // create the startup process
                File cwd = new File (domainHome);
                assert cwd.isDirectory() : "Working directory for weblogic does not exist:" + domainHome; //NOI18N
                org.openide.execution.NbProcessDescriptor pd = new org.openide.execution.NbProcessDescriptor(domainHome + "/" + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH), "");
                String envp[];
                envp = new String[] {"JAVA_OPTIONS=-Xdebug -Xnoagent -Djava.compiler=none -Xrunjdwp:server=y,suspend=n,transport=dt_socket,address=" + debuggerPort};    // NOI18N
                        
                serverProcess = pd.exec(null, envp, true, cwd);
                
                dm.setServerProcess(serverProcess);
                
                // create a tailer to the server's output stream so that a user
                // can observe the progress
                new WLTailer(serverProcess.getInputStream(), dm.getURI());
                
                String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
                
                // wait till the timeout happens, or if the server starts before 
                // send the completed event to j2eeserver
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    // send the 'completed' event and return when the server is running
                    if (isRunning()) {
                        serverProgress.notifyStart(StateType.COMPLETED, NbBundle.getMessage(WLStartServer.class, "MSG_SERVER_STARTED", serverName)); // NOI18N
                        return;
                    }

                    // sleep for a little so that we do not make our checks too
                    // often
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {}
                }

                // if the server did not start in the designated time limits
                // we consider the startup as failed and warn the user
                serverProgress.notifyStart(StateType.FAILED, NbBundle.getMessage(WLStartServer.class, "MSG_StartServerTimeout"));
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
        }
        
        /**
         * The amount of time in milliseconds during which the server should
         * start
         */
        private static final int TIMEOUT = 900000;
        
        /**
         * The amount of time in milliseconds that we should wait between checks
         */
        private static final int DELAY = 1000;
        
        /** 
         * Name of the startup script for windows
         */
        private static final String STARTUP_SH = "startWebLogic.sh"; // NOI18N
        
        /**
         * Name of the startup script for Unices
         */
        private static final String STARTUP_BAT = "startWebLogic.cmd"; // NOI18N
    }
    
    /**
     * Runnable that stops the server. It is used to stop the server in a 
     * separate thread, so that the IDE does not hang up during the stop 
     * process.
     * 
     * @author Kirill Sorokin
     */
    private class WLStopRunnable implements Runnable {
        
        /**
         * Root directory for the selected profile
         */ 
        private String domainHome;
        
        /**
         * Progress object that describes the stop process. It will be 
         * notified of the progress and the success/failure of the process
         */
        private WLServerProgress serverProgress;
        
        /** 
         * Creates a new instance of WSStopRunnable.
         * 
         * @param serverProgress the prgress object that the thread should 
         *      notify of anything that happens with the stop process
         */
        public WLStopRunnable(WLServerProgress serverProgress) {
            // save the progress pbject
            this.serverProgress = serverProgress;
            
            // get the profile home directory and the instance name from the
            // deployment manager
            domainHome = dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DOMAIN_ROOT_ATTR);
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
                        domainHome + "/bin/" + (Utilities.isWindows() ?    // NOI18N
                            SHUTDOWN_BAT : SHUTDOWN_SH)); 
                
                // create a tailer to the server's output stream so that a user
                // can observe the progress
                new WLTailer(serverProcess.getInputStream(), dm.getURI());

                String serverName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
                
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    if (!isStopped()) {
                        serverProgress.notifyStop(StateType.RUNNING, NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)); // NOI18N
                    } else {
                        try {
                            serverProcess.waitFor();
                        } catch (InterruptedException ex) {
                        }
                        long pbLagTime  = (System.currentTimeMillis() - start) / 4;
                        try {
                            Thread.sleep(pbLagTime);
                        } catch (InterruptedException e) {}
                        serverProgress.notifyStop(StateType.COMPLETED, NbBundle.getMessage(WLStartServer.class, "MSG_SERVER_STOPPED", serverName)); // NOI18N
                        return;
                    }

                    // sleep for a while so that we do not make our checks too often
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {}
                }
                    
                // if the server did not stop in the designated time limits
                // we consider the stop process as failed and kill the process
                serverProgress.notifyStop(StateType.FAILED, NbBundle.getMessage(WLStartServer.class, "MSG_StopServerTimeout"));
                serverProcess.destroy();
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.WARNING, null, e);
            }
        }
        
        /**
         * The amount of time in milliseconds during which the server should
         * stop
         */
        private static final int TIMEOUT = 900000;
        
        /**
         * The amount of time in milliseconds that we should wait between checks
         */
        private static final int DELAY = 1000;
        
        /** 
         * Name of the shutdown script for windows
         */
        private static final String SHUTDOWN_SH = "stopWebLogic.sh"; // NOI18N
        
        /** 
         * Name of the shutdown script for unices
         */
        private static final String SHUTDOWN_BAT = "stopWebLogic.cmd"; // NOI18N
    }
    
    /**
     * An implementation of the ProgressObject interface targeted at tracking 
     * the server instance's startup/shutdown progress
     * 
     * @author Kirill Sorokin
     */
    private static class WLServerProgress implements ProgressObject {
        
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
        public WLServerProgress(Object source) {
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
            notify(new WLDeploymentStatus(ActionType.EXECUTE, CommandType.START, state, message));
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
            notify(new WLDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, state, message));
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
    private static class WLDeploymentStatus implements DeploymentStatus {
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
        public WLDeploymentStatus(ActionType action, CommandType command, StateType state, String message) {
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
    // These introduce two additional states to the instance - starting and 
    // stopping son that we can filter sunsequent requests. They are currently 
    // not used, but may be if problems wth multiple startup arise again
    // private static final int STATE_STOPPED  = 0;
    // private static final int STATE_STARTING = 1;
    // private static final int STATE_STARTED  = 2;
    // private static final int STATE_STOPPING = 3;
}