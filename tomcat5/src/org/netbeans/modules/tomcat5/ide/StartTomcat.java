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

import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentPlanSplitter;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.netbeans.modules.tomcat5.*;
import org.netbeans.modules.tomcat5.config.*;
import org.netbeans.modules.tomcat5.util.TomcatInstallUtil;
import org.netbeans.modules.tomcat5.progress.ProgressEventSupport;
import org.netbeans.modules.tomcat5.progress.Status;

import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.openide.filesystems.*;

import org.w3c.dom.Document;
import org.xml.sax.*;
import org.xml.sax.SAXException;
import org.openide.xml.XMLUtil;

/** Extension to Deployment API that enables starting of Tomcat.
 *
 * @author Radim Kubacki, Pavel Buzek
 */
public final class StartTomcat extends StartServer implements ProgressObject
{
    public static final String TAG_CATALINA_HOME = "catalina_home"; // NOI18N
    public static final String TAG_CATALINA_BASE = "catalina_base"; // NOI18N
    /** Tag replaced with separator between filename components */
    public static final String TAG_SEPARATOR = "/"; // NOI18N
    
    public static final String TAG_JPDA = "jpda"; // NOI18N
    public static final String TAG_JPDA_STARTUP = "jpda_startup"; // NOI18N

    /** Startup command tag. */
    public static final String TAG_EXEC_CMD      = "catalina"; // NOI18N
    public static final String TAG_EXEC_STARTUP  = "exec_startup"; // NOI18N
    public static final String TAG_EXEC_SHUTDOWN = "exec_shutdown"; // NOI18N
    /** Shutdown command tag. */
    //public static final String TAG_SHUTDOWN_CMD   = "shutdown"; // NOI18N
    /** Debug startup/shutdown tag */
    public static final String TAG_DEBUG_CMD   = "catalina"; // NOI18N
    
    private static final Integer DEFAULT_ADMIN_PORT = new Integer (8025);
    private static final Integer DEFAULT_SERVER_PORT = new Integer (8084);
    
    private static NbProcessDescriptor defaultExecDesc(String command, String argCommand) {
        return new NbProcessDescriptor (
                "{" + StartTomcat.TAG_CATALINA_HOME + "}{" +     // NOI18N
                StartTomcat.TAG_SEPARATOR + "}bin{" + // NOI18N
                StartTomcat.TAG_SEPARATOR + "}{" +     // NOI18N
                command + "}",  // NOI18N
                "{" + argCommand + "}" ,  // NOI18N
                org.openide.util.NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
            );     
    }

    private static NbProcessDescriptor defaultDebugStartDesc(String command, String jpdaCommand) {
        return new NbProcessDescriptor (
                "{" + StartTomcat.TAG_CATALINA_HOME + "}{" +     // NOI18N
                StartTomcat.TAG_SEPARATOR + "}bin{" + // NOI18N
                StartTomcat.TAG_SEPARATOR + "}{" +     // NOI18N
                command + "}",  // NOI18N
                "{" + StartTomcat.TAG_JPDA + "}" + " {" + jpdaCommand + "}",  // NOI18N
                org.openide.util.NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
            );     
    }

    private TomcatManager tm;
    
    private ProgressEventSupport pes;
    
    private static Map isDebugModeUri = Collections.synchronizedMap((Map)new HashMap(2,1));
    
    public StartTomcat (DeploymentManager manager) {
        pes = new ProgressEventSupport (this);
        tm = (TomcatManager)manager;
        tm.setStartTomcat (this);
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
        return tm.isRunning ();
    }

    /**
     * Returns true if this target is in debug mode.
     */
    public boolean isDebuggable(Target target) {
        if (!isDebugModeUri.containsKey(tm.getUri())) {
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
        String dbgType = tm.getDebugType();
        if ((dbgType == null) || (dbgType.toLowerCase().indexOf("socket") > -1)) {
            Integer dbgPort = tm.getDebugPort();
            if (dbgPort != null) {
                sdi = new ServerDebugInfo("localhost", dbgPort.intValue());  // NOI18N
            } else {
                sdi = new ServerDebugInfo("localhost", TomcatManager.DEFAULT_DEBUG_PORT.intValue());  // NOI18N
            }
        } else {
            String shmem = tm.getSharedMemory();
            if (shmem != null) {
                sdi = new ServerDebugInfo("localhost", shmem);
            } else {
                sdi = new ServerDebugInfo("localhost", TomcatManager.DEFAULT_SHARED_MEMORY);
            }
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
            String home = tm.getCatalinaHome ();
            String base = tm.getCatalinaBase ();
            if (home == null) {
                // no home - start not supported
                pes.fireHandleProgressEvent (
                    null, new Status (ActionType.EXECUTE, command, 
                        NbBundle.getMessage (StartTomcat.class, command == CommandType.START ? "MSG_notStarting" : "MSG_notStopping"),
                        StateType.COMPLETED));
                return;
            }
            if (base == null) {
                base = home;
            }

            InstalledFileLocator ifl = InstalledFileLocator.getDefault ();
            File homeDir = new File (home);
            if (!homeDir.isAbsolute ()) {
                homeDir = ifl.locate (home, null, false);
            }

            File baseDir = new File (base);
            if (!baseDir.isAbsolute ()) {
                File baseDir2 = ifl.locate (base, null, false);
                if (baseDir2 == null) {
                    baseDir = createBaseDir (baseDir, homeDir);
                } else {
                    baseDir = baseDir2;
                }
            } else {
                if ((baseDir != null)) {
                    if ((!baseDir.exists()) || 
                        (baseDir.isDirectory() && baseDir.listFiles().length <= 0)) {
                            baseDir = tm.createBaseDir(baseDir, homeDir);
                    }
                }
            }
            
            // XXX check for null's

            // install the monitor
            if (command == CommandType.START) {
                try {
                    MonitorSupport.synchronizeMonitorWithFlag(tm, true, true);
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

            if ((debug) && (command == CommandType.START)) {

                NbProcessDescriptor pd  = defaultDebugStartDesc (StartTomcat.TAG_DEBUG_CMD, StartTomcat.TAG_JPDA_STARTUP);
                try { 
                    pes.fireHandleProgressEvent (
                        null, 
                        new Status (
                            ActionType.EXECUTE, 
                            command,
                            NbBundle.getMessage (StartTomcat.class, "MSG_startProcess"), 
                            StateType.RUNNING
                        )
                    );
                    Process p;
                    String transportStr = "JPDA_TRANSPORT=dt_socket";         // NOI18N
                    String addressStr = "JPDA_ADDRESS=11555";                 // NOI18N

                    if (org.openide.util.Utilities.isWindows()) {
                        String dbgType = tm.getDebugType();
                        if ((dbgType.toLowerCase().indexOf("socket") > -1) || (dbgType == null)) {         // NOI18N
                            addressStr = "JPDA_ADDRESS=" + tm.getDebugPort().toString(); // NOI18N
                        } else {
                            transportStr = "JPDA_TRANSPORT=dt_shmem";                // NOI18N
                            addressStr = "JPDA_ADDRESS=" + tm.getSharedMemory();     // NOI18N
                        }
                    } else {
                        addressStr = "JPDA_ADDRESS=" + tm.getDebugPort().toString();         // NOI18N
                    }
                    if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                        TomcatFactory.getEM ().log ("transport: " + transportStr);    // NOI18N
                        TomcatFactory.getEM ().log ("address: " + addressStr);    // NOI18N
                    }
                    p = pd.exec (
                        new TomcatFormat (homeDir.getAbsolutePath ()),
                        new String[] {
                            "JAVA_HOME="+System.getProperty ("jdk.home"),   // NOI18N
                            transportStr,
                            addressStr,
                            "CATALINA_HOME="+homeDir.getAbsolutePath (),    // NOI18N
                            "CATALINA_BASE="+baseDir.getAbsolutePath ()     // NOI18N
                        },
                        true,
                        new File (homeDir, "bin")
                    );        
                    ProcessSupport.connectProcessToOutputWindow(p, tm.getUri());
                } catch (java.io.IOException ioe) {
                    if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                        TomcatFactory.getEM ().notify (ErrorManager.INFORMATIONAL, ioe);    // NOI18N
                    }
                    pes.fireHandleProgressEvent (
                        null, 
                        new Status (ActionType.EXECUTE, command, ioe.getLocalizedMessage (), StateType.FAILED)
                    );
                }        
            } else {
                NbProcessDescriptor pd  = defaultExecDesc (StartTomcat.TAG_EXEC_CMD, 
                                                           command == CommandType.START ? StartTomcat.TAG_EXEC_STARTUP : StartTomcat.TAG_EXEC_SHUTDOWN);
                try { 
                    pes.fireHandleProgressEvent (
                        null, 
                        new Status (
                            ActionType.EXECUTE, 
                            command,
                            NbBundle.getMessage (StartTomcat.class, command == CommandType.START ? "MSG_startProcess" : "MSG_stopProcess"), 
                            StateType.RUNNING
                        )
                    );
                    Process p = pd.exec (
                        new TomcatFormat (homeDir.getAbsolutePath ()), 
                        new String[] { 
                            "JAVA_HOME="+System.getProperty ("jdk.home"),  // NOI18N 
                            "CATALINA_HOME="+homeDir.getAbsolutePath (), 
                            "CATALINA_BASE="+baseDir.getAbsolutePath ()
                        },
                        true,
                        new File (homeDir, "bin")
                    );
                    if (command == CommandType.START) {
                        ProcessSupport.connectProcessToOutputWindow(p, tm.getUri());
                    }
                } catch (java.io.IOException ioe) {
                    if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
                        TomcatFactory.getEM ().notify (ErrorManager.INFORMATIONAL, ioe);    // NOI18N
                    }
                    pes.fireHandleProgressEvent (
                        null, 
                        new Status (ActionType.EXECUTE, command, ioe.getLocalizedMessage (), StateType.FAILED)
                    );
                }        
            }
            boolean isRunning = isRunning();
            while ((command == CommandType.START && !isRunning) ||  //still no feedback when starting
                   (command == CommandType.STOP && isRunning)) {    //still getting feedback when stopping
                pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, command, NbBundle.getMessage (StartTomcat.class, "MSG_waiting"), StateType.RUNNING));
                try {
                    Thread.sleep(500); // take a nap before next retry
                } catch(InterruptedException ie) {}
                isRunning = isRunning();
            }
/*            running = command.equals (CommandType.START);
            if (debug) {
                if (running) {
                    isDebugModeUri.put(tm.getUri(), new Object());
                } else {
                    isDebugModeUri.remove(tm.getUri());
                }
            } else {
                isDebugModeUri.remove(tm.getUri());
            }
*/
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, command, "", StateType.COMPLETED));
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
        
    /** Initializes base dir for use with Tomcat 5.0.x. 
     *  @param baseDir directory for base dir.
     *  @param homeDir directory to copy config files from.
     *  @return File with absolute path for created dir or <CODE>null</CODE> when ther is an error.
     */
    private File createBaseDir (File baseDir, File homeDir) {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("creating base dir for "+tm);    // NOI18N
        }
        pes.fireHandleProgressEvent (
            null, 
            new Status (
                ActionType.EXECUTE, 
                CommandType.START, 
                NbBundle.getMessage (StartTomcat.class, "MSG_createBaseDir"), 
                StateType.RUNNING
            )
        );
        File baseD=tm.createBaseDir(baseDir, homeDir);
        if (baseD!=null) {
            try {
                //patch server.xml using S2B
                tm.getCatalinaBaseFileObject ();
    //            try {
    //                lfs = new LocalFileSystem ();
    //                lfs.setRootDirectory (baseDir);
    //                lfs.setHidden (true);
    //                Repository.getDefault ().addFileSystem (lfs);
                    File serverFile = new File (baseD, "conf/server.xml");   // NOI18N
                    FileInputStream is = new FileInputStream (serverFile);
                    Document doc = XMLUtil.parse(new InputSource(is), false, false, null,org.openide.xml.EntityCatalog.getDefault());
                    TomcatInstallUtil.setAdminPort (DEFAULT_ADMIN_PORT, FileUtil.fromFile (serverFile)[0]);
                    TomcatInstallUtil.setServerPort (DEFAULT_SERVER_PORT, FileUtil.fromFile (serverFile)[0]);
                    is.close();
    //            } catch (java.beans.PropertyVetoException pex) {
    //                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, pex);
    //                return null;
    //            } finally {
    //                if (lfs != null) {
    //                    Repository.getDefault ().removeFileSystem (lfs);
    //                }
    //            }
            } catch (java.io.IOException ioe) {
                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
                return null;
            } catch (org.xml.sax.SAXException spe) {
                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, spe);
                return null;
            }
        }
        return baseD;
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
    
    /** Format that provides value usefull for Tomcat execution. 
     * Currently this is only the name of startup wrapper.
    */
    private static class TomcatFormat extends org.openide.util.MapFormat {
        
        private static final long serialVersionUID = 992972967554321415L;

        public TomcatFormat (String home) {
            super(new java.util.HashMap ());
            java.util.Map map = getMap ();
            map.put (TAG_EXEC_CMD, org.openide.util.Utilities.isWindows ()? "catalina.bat": "catalina.sh"); // NOI18N
            map.put (TAG_EXEC_STARTUP, "run"); // NOI18N
            map.put (TAG_EXEC_SHUTDOWN, "stop"); // NOI18N
            //map.put (TAG_SHUTDOWN_CMD, org.openide.util.Utilities.isWindows ()? "shutdown.bat": "shutdown.sh"); // NOI18N
            map.put (TAG_DEBUG_CMD, org.openide.util.Utilities.isWindows ()? "catalina.bat": "catalina.sh"); // NOI18N
            map.put (TAG_JPDA, "jpda"); // NOI18N
            map.put (TAG_JPDA_STARTUP, "run"); // NOI18N
            map.put (StartTomcat.TAG_CATALINA_HOME, home); // NOI18N
            map.put (TAG_SEPARATOR, File.separator);
        }
    }
    
    public String toString () {
        return "StartTomcat [" + tm + "]";
    }
}
