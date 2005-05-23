/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.weblogic9.optional;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import javax.enterprise.deploy.spi.status.*;

import org.openide.*;
import org.openide.util.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

import org.netbeans.modules.j2ee.weblogic9.*;
import org.netbeans.modules.j2ee.weblogic9.util.*;

/**
 * This class provides functionality used to start and stop a particular server
 * instance. It also supports starting the server in debug mode, so that 
 * NetBeans can connect to it using its built-in JPDA debugger.
 * 
 * @author Kirill Sorokin
 */
public class WLStartServer extends StartServer {
    
    /**
     * The server's deployment manager, to be exact the plugin's wrapper for it
     */
    private WLDeploymentManager dm;
    
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
        serverProgress.notifyStart(StateType.RUNNING,  ""); // NOI18N
        
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
                WLDeploymentFactory.DEBUGGER_PORT_ATTR)).intValue());
    }
    
    /**
     * Tells whether the normal startup of the server is supported.
     * 
     * @return if the server is local, its true, false otherwise
     */
    public boolean supportsStartDeploymentManager() {
        // if the server is local we can start it
        if (dm.getInstanceProperties().getProperty(
                WLDeploymentFactory.IS_LOCAL_ATTR).equals("true")) {   // NOI18N
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
        serverProgress.notifyStop(StateType.RUNNING, ""); // NOI18N
        
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
        serverProgress.notifyStart(StateType.RUNNING,  ""); // NOI18N
        
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
        // try to get an open socket to the target host/port
        try {
            new Socket(dm.getHost(), new Integer(dm.getPort()).intValue());
            
            // if we are successful, return true
            return true;
        } catch (UnknownHostException e) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
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
            list.add(key + "=" + properties.getProperty(key));
        }
        
        // convert the list into an array and return
        return (String[]) list.toArray(new String[list.size()]);
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
                    WLDeploymentFactory.DOMAIN_ROOT_ATTR);
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
                Process serverProcess = Runtime.getRuntime().exec(
                        domainHome + "/" + (Utilities.isWindows() ?    // NOI18N
                            STARTUP_BAT : STARTUP_SH)); 
                
                // create a tailer to the server's output stream so that a user
                // can observe the progress
                new WLTailer(serverProcess.getInputStream(), 
                        NbBundle.getMessage(WLStartServer.class, 
                        "TXT_ioWindowTitle")).start();                 // NOI18N
                
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
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
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
                    WLDeploymentFactory.DOMAIN_ROOT_ATTR);
            debuggerPort = dm.getInstanceProperties().getProperty(
                    WLDeploymentFactory.DEBUGGER_PORT_ATTR);
        }
        
        /**
         * Implementation of the run() method from the Runnable interface
         */
        public void run() {
            try {
                // save the current time so that we can deduct that the startup
                // failed due to timeout
                long start = System.currentTimeMillis();
                
                // get the current environment
                Properties environment = System.getProperties();
                environment.put("JAVA_OPTIONS", "-Xdebug " +           // NOI18N
                        "-Xnoagent -Xrunjdwp:transport=dt_socket" +    // NOI18N
                        ",address=" + debuggerPort +                   // NOI18N
                        ",server=y,suspend=n");                        // NOI18N
                
                // create the startup process
                Process serverProcess = Runtime.getRuntime().exec(new String[]{domainHome + "/" + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH)}, properties2StringArray(environment));
                
//                ProcessBuilder processBuilder = new ProcessBuilder(new String[]{domainHome + "/" + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH)}); // NOI18N
//                processBuilder.environment().put("JAVA_OPTIONS", "-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=" + debuggerPort + ",server=y,suspend=n"); // NOI18N
//                
//                Process serverProcess = processBuilder.start();
                
                // create a tailer to the server's output stream so that a user
                // can observe the progress
                new WLTailer(serverProcess.getInputStream(), NbBundle.getMessage(WLStartServer.class, "TXT_ioWindowTitle")).start();
                
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
                serverProgress.notifyStart(StateType.FAILED, NbBundle.getMessage(WLStartServer.class, "")); // NOI18N
                serverProcess.destroy();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
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
                    WLDeploymentFactory.DOMAIN_ROOT_ATTR);
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
                        domainHome + "/" + (Utilities.isWindows() ?    // NOI18N
                            SHUTDOWN_BAT : SHUTDOWN_SH)); 
                
                // create a tailer to the server's output stream so that a user
                // can observe the progress
                new WLTailer(serverProcess.getInputStream(), NbBundle.getMessage(WLStartServer.class, "TXT_ioWindowTitle")).start(); // NOI18N
                
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    if (isRunning()) {
                        serverProgress.notifyStop(StateType.RUNNING, 
                                "");                                   // NOI18N
                    } else {
                        serverProgress.notifyStop(StateType.COMPLETED, 
                                "");                                   // NOI18N
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
                serverProgress.notifyStop(StateType.FAILED, "");       // NOI18N
                serverProcess.destroy();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
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