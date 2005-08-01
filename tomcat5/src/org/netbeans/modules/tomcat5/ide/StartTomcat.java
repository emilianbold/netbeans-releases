/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.ide;

import java.io.*;
import java.util.*;

import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.*;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;

import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;

import org.netbeans.modules.tomcat5.*;
import org.netbeans.modules.tomcat5.progress.ProgressEventSupport;
import org.netbeans.modules.tomcat5.progress.Status;

import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.xml.sax.SAXException;


/** Extension to Deployment API that enables starting of Tomcat.
 *
 * @author Radim Kubacki, Pavel Buzek
 */
public final class StartTomcat extends StartServer implements ProgressObject
{
    public static final String CATALINA_BAT     = "catalina.bat";    // NOI18N
    public static final String CATALINA_SH      = "catalina.sh";     // NOI18N
    public static final String CATALINA_50_BAT  = "catalina.50.bat"; // NOI18N
    public static final String CATALINA_50_SH   = "catalina.50.sh";  // NOI18N
    
    public static final String SETCLASSPATH_BAT = "setclasspath.bat"; // NOI18N
    public static final String SETCLASSPATH_SH  = "setclasspath.sh";  // NOI18N
        
    public static final String TAG_CATALINA_HOME = "catalina_home"; // NOI18N
    public static final String TAG_CATALINA_BASE = "catalina_base"; // NOI18N
    
    public static final String TAG_JPDA = "jpda"; // NOI18N
    public static final String TAG_JPDA_STARTUP = "jpda_startup"; // NOI18N

    /** Startup command tag. */
    public static final String TAG_EXEC_CMD      = "catalina"; // NOI18N
    public static final String TAG_EXEC_STARTUP  = "exec_startup"; // NOI18N
    public static final String TAG_EXEC_SHUTDOWN = "exec_shutdown"; // NOI18N
    public static final String TAG_SECURITY_OPT = "security_option"; //NOI18N
    public static final String TAG_FORCE_OPT = "force_option"; //NOI18N

    /** Debug startup/shutdown tag */
    public static final String TAG_DEBUG_CMD   = "catalina"; // NOI18N


    private TomcatManager tm;
    
    private ProgressEventSupport pes;
    private int currentServerPort; // current server port Tomcat is running on
    
    private static Map isDebugModeUri = Collections.synchronizedMap((Map)new HashMap(2,1));
    
    public StartTomcat (DeploymentManager manager) {
        assert manager instanceof TomcatManager : 
            "Illegal DeploymentManager instance: " + manager.getClass().getName(); // NIO18N
        tm = (TomcatManager)manager;
        tm.setStartTomcat (this);
        pes = new ProgressEventSupport (this);
        currentServerPort = tm.getServerPort();
    }
    
    public boolean supportsStartDeploymentManager () {
        return true;
    }
    
    /** Start Tomcat server if the TomcatManager is not connected.
     */
    public ProgressObject startDeploymentManager () {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("StartTomcat.startDeploymentManager called on "+tm);    // NOI18N
        }
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, CommandType.START, "", StateType.RUNNING));
        RequestProcessor.getDefault ().post (new StartRunnable(false, CommandType.START), 0, Thread.NORM_PRIORITY);
        isDebugModeUri.remove(tm.getUri());
        return this;
    }
    
    /**
     * Returns true if the admin server is also a target server (share the same vm).
     * Start/stopping/debug apply to both servers.
     * @return true when admin is also target server
     */
    public boolean isAlsoTargetServer(Target target) { return true; }

    /**
     * Returns true if the admin server should be started before configure.
     */
    public boolean needsStartForConfigure() { return false; }

    /**
     * Returns true if the admin server should be started before asking for
     * target list.
     */
    public boolean needsStartForTargetList() { return false; }

    /**
     * Returns true if the admin server should be started before admininistrative configuration.
     */
    public boolean needsStartForAdminConfig() { return false; }

    /**
     * Returns true if this admin server is running.
     */
    public boolean isRunning() {
        return tm.isRunning (true);
    }

    /**
     * Returns true if this target is in debug mode.
     */
    public boolean isDebuggable(Target target) {
        if (!isDebugModeUri.containsKey(tm.getUri())) {
            return false;
        }
        if (!isRunning()) {
            isDebugModeUri.remove(tm.getUri());
            return false;
        }
        return true;
    }

    /**
     * Stops the admin server. The DeploymentManager object will be disconnected.
     * All diagnostic should be communicated through ServerProgres with no 
     * exceptions thrown.
     * @return ServerProgress object used to monitor start server progress
     */
    public ProgressObject stopDeploymentManager() { 
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("StartTomcat.stopDeploymentManager called on "+tm);    // NOI18N
        }
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, CommandType.STOP, "", StateType.RUNNING));
        RequestProcessor.getDefault ().post (new StartRunnable(false, CommandType.STOP), 0, Thread.NORM_PRIORITY);
        isDebugModeUri.remove(tm.getUri());
        return this;
    }

    /**
     * Start or restart the target in debug mode.
     * If target is also domain admin, the amdin is restarted in debug mode.
     * All diagnostic should be communicated through ServerProgres with no exceptions thrown.
     * @param target the target server
     * @return ServerProgress object to monitor progress on start operation
     */
    public ProgressObject startDebugging(Target target) {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("StartTomcat.startDebugging called on "+tm);    // NOI18N
        }
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, CommandType.START, "", StateType.RUNNING));
        RequestProcessor.getDefault ().post (new StartRunnable(true, CommandType.START), 0, Thread.NORM_PRIORITY);
        isDebugModeUri.put(tm.getUri(), new Object());
        return this;
    }

    public ServerDebugInfo getDebugInfo(Target target) { 
        ServerDebugInfo sdi;
        TomcatProperties tp = tm.getTomcatProperties();
        if (tp.getDebugType().toLowerCase().indexOf("socket") != -1) {  // NOI18N
            sdi = new ServerDebugInfo("localhost", tp.getDebugPort());  // NOI18N
        } else {
            sdi = new ServerDebugInfo("localhost", tp.getSharedMem());  // NOI18N
        }
        return sdi;
    }
    
    private class StartRunnable implements Runnable {
        
        private boolean debug = false;
        private CommandType command = CommandType.START;
        
        public StartRunnable(boolean debug, CommandType command) {
            this.debug = debug;
            this.command = command;
        }
        
        public synchronized void run () {
            // PENDING check whether is runs or not
            TomcatProperties tp = tm.getTomcatProperties();
            File homeDir = tp.getCatalinaHome();
            if (homeDir == null || !homeDir.exists()) {
                fireCmdExecProgressEvent(
                    command == CommandType.START ? "MSG_NoHomeDirStart" : "MSG_NoHomeDirStop",
                    StateType.FAILED);
                return;
            }
            File baseDir = tp.getCatalinaBase();
            if (baseDir == null) {
                baseDir = homeDir;
            } else {
                if (baseDir != null) {
                    String[] files = baseDir.list();
                    if (files == null || files.length == 0) {
                        baseDir = tm.createBaseDir(baseDir, homeDir);
                    }
                }
                if (baseDir == null) {
                    fireCmdExecProgressEvent(
                        command == CommandType.START ? "MSG_NoBaseDirStart" : "MSG_NoBaseDirStop",
                        StateType.FAILED);
                    return;
                }
            }
            
            // check whether the startup script - catalina.sh/bat exists
            File startupScript = getStartupScript();
            if (!startupScript.exists()) {
                final String MSG = NbBundle.getMessage(
                        StartTomcat.class, 
                        command == CommandType.START ? "MSG_StartFailedNoStartScript" : "MSG_StopFailedNoStartScript",
                        startupScript.getAbsolutePath());
                pes.fireHandleProgressEvent(
                    null,
                    new Status(ActionType.EXECUTE, command, MSG, StateType.FAILED));
                return;
            }
            
            // install the monitor
            if (command == CommandType.START) {
                try {
                    MonitorSupport.synchronizeMonitorWithFlag(tm, true, !tp.isBundledTomcat());
                } catch (IOException e) {
                    if (MonitorSupport.getMonitorFlag(tm)) {
                        // tomcat has been started with monitor enabled
                        MonitorSupport.setMonitorFlag(tm, false);
                        fireCmdExecProgressEvent("MSG_enableMonitorSupportErr", StateType.FAILED);
                    } else {
                        // tomcat has been started with monitor disabled
                        fireCmdExecProgressEvent("MSG_disableMonitorSupportErr", StateType.FAILED);
                    }
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    return;
                } catch (SAXException e) {
                    // fault, but not a critical one
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                try {
                    DebugSupport.allowDebugging(tm);
                }
                catch (IOException e) {
                    // fault, but not a critical one
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                catch (SAXException e) {
                    // fault, but not a critical one
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            currentServerPort = tm.getServerPort(); // remember the server port
            
            if ((debug) && (command == CommandType.START)) {

                NbProcessDescriptor pd  = null;
                if (tp.getSecManager()) {
                    pd = defaultDebugStartDesc (TAG_DEBUG_CMD, TAG_JPDA_STARTUP, TAG_SECURITY_OPT);
                } else {
                    pd = defaultDebugStartDesc (TAG_DEBUG_CMD, TAG_JPDA_STARTUP);
                }
                try {
                    fireCmdExecProgressEvent("MSG_startProcess", StateType.RUNNING);
                    Process p = null;
                    
                    String address;
                    String transport;
                    if (tp.getDebugType().toLowerCase().indexOf("socket") != -1) { // NOI18N
                        transport = "dt_socket"; // NOI18N
                        address = Integer.toString(tp.getDebugPort());
                    } else {
                        transport = "dt_shmem"; // NOI18N
                        address = tp.getSharedMem();
                    }
                    if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                        TomcatFactory.getEM ().log ("transport: " + transport);    // NOI18N
                        TomcatFactory.getEM ().log ("address: " + address);    // NOI18N
                    }
                    p = pd.exec (
                        new TomcatFormat(startupScript, homeDir),
                        new String[] {
                            "JAVA_HOME="        + getJavaHome(),    // NOI18N
                            "JAVA_OPTS="        + tp.getJavaOpts(), // NOI18N
                            "JPDA_TRANSPORT="   + transport,        // NOI18N
                            "JPDA_ADDRESS="     + address,          // NOI18N
                            "CATALINA_HOME="    + homeDir.getAbsolutePath(),    // NOI18N
                            "CATALINA_BASE="    + baseDir.getAbsolutePath()     // NOI18N
                        },
                        true,
                        new File (homeDir, "bin") // NOI18N
                    );
                    tm.setTomcatProcess(p);
                    tm.logManager().closeServerLog();
                    tm.logManager().openServerLog();
                } catch (java.io.IOException ioe) {
                    if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                        TomcatFactory.getEM ().notify (ErrorManager.INFORMATIONAL, ioe);
                    }
                    fireCmdExecProgressEvent(command == CommandType.START ? "MSG_StartFailedIOE" : "MSG_StopFailedIOE",
                            startupScript.getAbsolutePath(), StateType.FAILED);
                    return;
                }
            } else {
                NbProcessDescriptor pd = null;
                if (command == CommandType.START) {
                    if (tp.getSecManager()) {
                        pd = defaultExecDesc(TAG_EXEC_CMD, TAG_EXEC_STARTUP, TAG_SECURITY_OPT);
                    } else {
                        pd = defaultExecDesc(TAG_EXEC_CMD, TAG_EXEC_STARTUP);
                    }
                } else {
                    if (tp.getForceStop() && Utilities.isUnix()) {
                        pd = defaultExecDesc(TAG_EXEC_CMD, TAG_EXEC_SHUTDOWN, TAG_FORCE_OPT);
                    } else {
                        pd = defaultExecDesc(TAG_EXEC_CMD, TAG_EXEC_SHUTDOWN);
                    }
                }
                try {
                    fireCmdExecProgressEvent(command == CommandType.START ? "MSG_startProcess" : "MSG_stopProcess",
                            StateType.RUNNING);
                    Process p = pd.exec (
                        new TomcatFormat (startupScript, homeDir),
                        new String[] { 
                            "JAVA_HOME="        + getJavaHome(),    // NOI18N
                            "JAVA_OPTS="        + tp.getJavaOpts(), // NOI18N
                            "CATALINA_HOME="    + homeDir.getAbsolutePath(),    // NOI18N
                            "CATALINA_BASE="    + baseDir.getAbsolutePath()     // NOI18N
                        },
                        true,
                        new File (homeDir, "bin")
                    );
                    if (command == CommandType.START) {
                        tm.setTomcatProcess(p);
                        tm.logManager().closeServerLog();
                        tm.logManager().openServerLog();
                    } else {
                        // #58554 workaround
                        RequestProcessor.getDefault().post(new StreamConsumer(p.getInputStream()), 0, Thread.MIN_PRIORITY);
                        RequestProcessor.getDefault().post(new StreamConsumer(p.getErrorStream()), 0, Thread.MIN_PRIORITY);
                    }
                } catch (java.io.IOException ioe) {
                    if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                        TomcatFactory.getEM ().notify (ErrorManager.INFORMATIONAL, ioe);    // NOI18N
                    }
                    fireCmdExecProgressEvent(command == CommandType.START ? "MSG_StartFailedIOE" : "MSG_StopFailedIOE",
                            startupScript.getAbsolutePath(), StateType.FAILED);
                    return;
                }
            }
            
            fireCmdExecProgressEvent("MSG_waiting", StateType.RUNNING);
            if (!hasCommandSucceeded()) {
                    fireCmdExecProgressEvent(command == CommandType.START ? "MSG_StartFailed" : "MSG_StopFailed", 
                            StateType.FAILED);
                    return;
            }
            fireCmdExecProgressEvent(command == CommandType.START ? "MSG_Started" : "MSG_Stopped", 
                    StateType.COMPLETED);
        }
        
        /**
         * Fires command progress event of action type <code>ActionType.EXECUTE</code>.
         *
         * @param resName event status message from the bundle, specified by the 
         *        resource name.
         * @param stateType event state type.
         */
        private void fireCmdExecProgressEvent(String resName, StateType stateType) {
            String msg = NbBundle.getMessage(StartTomcat.class, resName);
            pes.fireHandleProgressEvent(
                null,
                new Status(ActionType.EXECUTE, command, msg, stateType));
        }
        
        /**
         * Fires command progress event of action type <code>ActionType.EXECUTE</code>.
         *
         * @param resName event status message from the bundle, specified by the 
         *        resource name.
         * @param arg1 the argument to use when formating the message
         * @param stateType event state type.
         */
        private void fireCmdExecProgressEvent(String resName, Object arg1, StateType stateType) {
            String msg = NbBundle.getMessage(StartTomcat.class, resName, arg1);
            pes.fireHandleProgressEvent(
                null,
                new Status(ActionType.EXECUTE, command, msg, stateType));
        }
        
        
        /** For how long should we keep trying to get response from the server. */
        private static final long TIMEOUT_DELAY = 180000;
        
        /**
         * Try to get response from the server, whether the START/STOP command has 
         * succeeded.
         *
         * @return <code>true</code> if START/STOP command completion was verified,
         *         <code>false</code> if time-out ran out.
         */
        private boolean hasCommandSucceeded() {
            boolean isRunning = isRunning();
            long startTime = System.currentTimeMillis();
            while ((command == CommandType.START && !isRunning) ||  //still no feedback when starting
                   (command == CommandType.STOP && isRunning)) {    //still getting feedback when stopping
                // if time-out ran out, suppose command failed
                if (System.currentTimeMillis() > startTime + TIMEOUT_DELAY) {
                    return false;
                }
                try {
                    Thread.sleep(500); // take a nap before next retry
                } catch(InterruptedException ie) {}
                isRunning = isRunning();
            }
            return true;
        }
    }
    
    /** This implementation does nothing.
     * Target is already started when Tomcat starts.
     */
    public ProgressObject startServer (Target target) {
        return null;
    }
    
    public boolean supportsDebugging (Target target) {
        return true;
    }

    public ClientConfiguration getClientConfiguration (TargetModuleID targetModuleID) {
        return null; // XXX is it OK?
    }
    
    public DeploymentStatus getDeploymentStatus () {
        return pes.getDeploymentStatus ();
    }
    
    public TargetModuleID[] getResultTargetModuleIDs () {
        return new TargetModuleID [] {};
    }
    
    public boolean isCancelSupported () {
        return false;
    }
    
    public void cancel () 
    throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("");
    }
    
    public boolean isStopSupported () {
        return false;
    }
    
    public void stop () 
    throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("");
    }
    
    public void addProgressListener (ProgressListener pl) {
        pes.addProgressListener (pl);
    }
    
    public void removeProgressListener (ProgressListener pl) {
        pes.removeProgressListener (pl);
    }
    
    
    public String toString () {
        return "StartTomcat [" + tm + "]"; // NOI18N
    }
    
    public int getCurrentServerPort() {
        return currentServerPort;
    }
    
    // private helper methods -------------------------------------------------    
    
    private static NbProcessDescriptor defaultExecDesc(String command, String argCommand, String option) {
        return new NbProcessDescriptor (
            "{" + command + "}",  // NOI18N
            "{" + argCommand + "}" + " {" + option + "}",  // NOI18N
            NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
        );
    }
    
    private static NbProcessDescriptor defaultExecDesc(String command, String argCommand) {
        return new NbProcessDescriptor (
            "{" + command + "}",     // NOI18N
            "{" + argCommand + "}",  // NOI18N
            NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
        );
    }

    private static NbProcessDescriptor defaultDebugStartDesc(String command, String jpdaCommand, String option) {
        return new NbProcessDescriptor (
            "{" + command + "}",  // NOI18N
            "{" + TAG_JPDA + "}" + " {" + jpdaCommand + "}" + " {" + option + "}",  // NOI18N
            NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
        );
    }
    
    private static NbProcessDescriptor defaultDebugStartDesc(String command, String jpdaCommand) {
        return new NbProcessDescriptor (
            "{" + command + "}",  // NOI18N
            "{" + TAG_JPDA + "}" + " {" + jpdaCommand + "}",  // NOI18N
            NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
        );
    }
    
    private String getJavaHome() {
        FileObject fo = (FileObject)getJavaPlatform().getInstallFolders().iterator().next();
        return FileUtil.toFile(fo).getAbsolutePath();
    }
    
    /** Return the catalina startup script file. */
    private File getStartupScript() {
        TomcatProperties tp = tm.getTomcatProperties();
        if (tp.getCustomScript()) {
            return new File(tp.getScriptPath());
        }
        String javaVersion = (String)getJavaPlatform().getSystemProperties().get("java.vm.version"); // NOI18N
        // use catalina50.sh/bat for Tomcat 5.0 on jdk1.5
        if (tm.getTomcatVersion() == TomcatManager.TOMCAT_50 
             && javaVersion != null && javaVersion.startsWith("1.5")) {  // NOI18N
            String startupScript = Utilities.isWindows() ? CATALINA_50_BAT : CATALINA_50_SH;
            File scriptFile = new File(tp.getCatalinaHome(), "/bin/" + startupScript); // NOI18N
            if (scriptFile.exists()) {
                return scriptFile;
            }
        }
        String startupScript = Utilities.isWindows() ? CATALINA_BAT : CATALINA_SH;
        return new File(tp.getCatalinaHome(), "/bin/" + startupScript); // NOI18N
    }
    
    private JavaPlatform getJavaPlatform() {
        String currentJvm = tm.getTomcatProperties().getJavaPlatform();
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform[] curJvms = jpm.getPlatforms(currentJvm, new Specification("J2SE", null)); // NOI18N
        assert curJvms.length > 0 : "No Java platform available."; // NOI18N
        return curJvms[0];
    }
    
    /** Utility class that just "consumes" the input stream - #58554 workaround
     */
    private static class StreamConsumer implements Runnable {
        
        private BufferedInputStream in;
        
        public StreamConsumer(InputStream is) {
            in = new BufferedInputStream(is);
        }

        public void run() {
            try {
                byte buffer[] = new byte[1024];
                while (true) {
                    int n = in.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    if (TomcatFactory.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {
                        TomcatFactory.getEM().log(ErrorManager.INFORMATIONAL, new String(buffer, 0, n));
                    }
                }
            } catch (IOException ioe) {
                if (TomcatFactory.getEM().isLoggable(ErrorManager.INFORMATIONAL)) {
                    TomcatFactory.getEM().notify(ErrorManager.INFORMATIONAL, ioe);
                }
            } finally {
                try { in.close(); } catch (IOException ioe) {};
            }
        }
    };
    
    /** Format that provides value usefull for Tomcat execution. 
     * Currently this is only the name of startup wrapper.
    */
    private static class TomcatFormat extends org.openide.util.MapFormat {
        
        private static final long serialVersionUID = 992972967554321415L;
        
        public TomcatFormat(File startupScript, File homeDir) {
            super(new java.util.HashMap ());
            java.util.Map map = getMap ();
            String scriptPath = startupScript.getAbsolutePath();
            map.put(TAG_EXEC_CMD,       scriptPath);
            map.put(TAG_EXEC_STARTUP,   "run");         // NOI18N
            map.put(TAG_EXEC_SHUTDOWN,  "stop");        // NOI18N
            map.put(TAG_DEBUG_CMD,      scriptPath);
            map.put(TAG_JPDA,           "jpda");        // NOI18N
            map.put(TAG_JPDA_STARTUP,   "run");         // NOI18N
            map.put(TAG_SECURITY_OPT,   "-security");   // NOI18N
            map.put(TAG_FORCE_OPT,      "-force");      // NOI18N
            map.put(TAG_CATALINA_HOME,  homeDir.getAbsolutePath());
        }
    }
}
