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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FileDeploymentLayout;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ModuleUrlResolver;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentPlanSplitter;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.TomcatManagerImpl;
import org.netbeans.modules.tomcat5.TomcatModule;
import org.netbeans.modules.tomcat5.WebappConfiguration;
import org.netbeans.modules.tomcat5.URLWait;
import org.netbeans.modules.tomcat5.progress.ProgressEventSupport;
import org.netbeans.modules.tomcat5.progress.Status;
import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.ProcessExecutor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.debugger.DebuggerInfo;
import org.netbeans.modules.debugger.jpda.RemoteDebuggerInfo;

/** Extension to JSR88 that enables starting of Tomcat.
 *
 * <p>
 * TODO: create lib and classes dirs in base dir and modify catalina.policy to load from them
 * TODO: progress when starting
 *
 * @author Radim Kubacki
 */
public final class StartTomcat implements StartServer, Runnable, ProgressObject, IncrementalDeployment, FileDeploymentLayout, ModuleUrlResolver, DeploymentPlanSplitter
{
    private static StartTomcat instance;
    
    public static synchronized StartTomcat create() {
        if (instance == null) {
            instance = new StartTomcat ();
        }
        return instance;
    }
    
    public static final String TAG_CATALINA_HOME = "catalina_home"; // NOI18N
    public static final String TAG_CATALINA_BASE = "catalina_base"; // NOI18N
    
    public static final String TAG_JPDA = "jpda"; // NOI18N
    public static final String TAG_JPDA_STARTUP = "jpda_startup"; // NOI18N
    public static final String TAG_JPDA_SHUTDOWN = "jpda_shutdown"; // NOI18N

    /** Startup command tag. */
    public static final String TAG_STARTUP_CMD   = "startup"; // NOI18N
    /** Shutdown command tag. */
    public static final String TAG_SHUTDOWN_CMD   = "shutdown"; // NOI18N
    /** Debug startup/shutdown tag */
    public static final String TAG_DEBUG_CMD   = "catalina"; // NOI18N
    
    private static NbProcessDescriptor defaultExecDesc(String command) {
        return new NbProcessDescriptor (
                "{" + StartTomcat.TAG_CATALINA_HOME + "}{" +     // NOI18N
                ProcessExecutor.Format.TAG_SEPARATOR + "}bin{" + // NOI18N
                ProcessExecutor.Format.TAG_SEPARATOR + "}{" +     // NOI18N
                command + "}",  // NOI18N
                "", 
                org.openide.util.NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
            );     
    }

    private static NbProcessDescriptor defaultDebugDesc(String command, String jpdaCommand) {
        return new NbProcessDescriptor (
                "{" + StartTomcat.TAG_CATALINA_HOME + "}{" +     // NOI18N
                ProcessExecutor.Format.TAG_SEPARATOR + "}bin{" + // NOI18N
                ProcessExecutor.Format.TAG_SEPARATOR + "}{" +     // NOI18N
                command + "}",  // NOI18N
                "{" + StartTomcat.TAG_JPDA + "}" + " {" + jpdaCommand + "}",  // NOI18N
                org.openide.util.NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
            );     
    }

    private TomcatManager tm;
    
    private ProgressEventSupport pes;
    
    private CommandType command;

    private boolean isDebugMode = false;
    private boolean startDebugMode = false;
    
    private static final String DEPLOYMENT_PLAN_FNAME = "tomcat_configuration.xml";
    
    /** Default constructor. */
    public StartTomcat () {
        pes = new ProgressEventSupport (this);
    }
    
    /** Associates with @see javax.enterprise.deploy.spi.DeploymentManager */
    public void setDeploymentManager (javax.enterprise.deploy.spi.DeploymentManager manager) {
        tm = (TomcatManager)manager;
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
        command = CommandType.START;
        startDebugMode = false;
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, command, "", StateType.RUNNING));
        RequestProcessor.getDefault ().post (this, 0, Thread.NORM_PRIORITY);
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
    boolean running = false;
    public boolean isRunning() {
        // PENDING should this be cached? see #37937
        return URLWait.waitForStartup (tm, 1000);
//        return running;
    }

    /**
     * Returns true if this target is in debug mode.
     */
    public boolean isDebuggable(Target target) { 
        return isDebugMode; // TODO
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
        command = CommandType.STOP;
        startDebugMode = false;
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, command, "", StateType.RUNNING));
        RequestProcessor.getDefault ().post (this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    /**
     * Start or restart the target in debug mode.
     * If target is also domain admin, the amdin is restarted in debug mode.
     * All diagnostic should be communicated through ServerProgres with no exceptions thrown.
     * @param target the target server
     * @return ServerProgress object to monitor progress on start operation
     */
    public ProgressObject startDebugging(Target target){
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("StartTomcat.startDebugging called on "+tm);    // NOI18N
        }
        startDebugMode = true;
        command = CommandType.START;
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, command, "", StateType.RUNNING));
        RequestProcessor.getDefault ().post (this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public DebuggerInfo getDebugInfo(Target target) { 
        RemoteDebuggerInfo rdi = new RemoteDebuggerInfo("localhost", 8000); // TODO - this must be taken from some sort of customizer of Tomcat node
        return rdi;
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
            }
            else {
                baseDir = baseDir2;
            }
        }
        // XXX check for null's

        if (startDebugMode) {
            NbProcessDescriptor pd  = defaultDebugDesc (StartTomcat.TAG_DEBUG_CMD, 
                                                        command == CommandType.START ? StartTomcat.TAG_JPDA_STARTUP : StartTomcat.TAG_JPDA_SHUTDOWN);
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
//                        "JPDA_TRANSPORT=" + "dt_socket",               // NOI18N  TODO - this must be taken from customizer of Tomcat node
//                        "JPDA_ADDRESS="+"8000",                        // NOI18N  TODO - same as above
                        "CATALINA_HOME="+homeDir.getAbsolutePath (),   // NOI18N
                        "CATALINA_BASE="+baseDir.getAbsolutePath ()    // NOI18N
                    },
                    true,
                    new File (homeDir, "bin")
                );        
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
            NbProcessDescriptor pd  = defaultExecDesc (command == CommandType.START ? StartTomcat.TAG_STARTUP_CMD : StartTomcat.TAG_SHUTDOWN_CMD);
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
        
        while ((command == CommandType.START && !URLWait.waitForStartup (tm, 1000)) ||  //still no feedback when starting
               (command == CommandType.STOP && URLWait.waitForStartup (tm, 1000))) {    //still getting feedback when stopping
            pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, command, NbBundle.getMessage (StartTomcat.class, "MSG_waiting"), StateType.RUNNING));
        }
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, command, "", StateType.COMPLETED));
        running = command.equals (CommandType.START);
        if (startDebugMode) {
            isDebugMode = (command.equals (CommandType.START));
        }
    }
    
    /** This implementation does nothing.
     * Target is already started when Tomcat starts.
     */
    public ProgressObject startServer (Target target) {
        
        return null; // PENDING
    }
    
    public boolean supportsDebugging (Target target) {
        return true;
    }
    
/*    public void stopDebugging (Target target) {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("StartTomcat.stopDebugging called on "+tm);    // NOI18N
        }
        startDebugMode = Boolean.TRUE.booleanValue();
        command = CommandType.STOP;
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, command, "", StateType.RUNNING));
        RequestProcessor.getDefault ().post (this, 0, Thread.NORM_PRIORITY);
    }*/
    
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
        File targetFolder;
        if (!baseDir.isAbsolute ()) {
            baseDir = new File(System.getProperty("netbeans.user")+System.getProperty("file.separator")+baseDir);
            targetFolder = new File(System.getProperty("netbeans.user"));

        } else {
            targetFolder = baseDir.getParentFile ();
        }
        
        try {
            
            if (targetFolder == null) {
                TomcatFactory.getEM ().log (ErrorManager.INFORMATIONAL, "Cannot find parent folder for base dir "+baseDir.getPath ());
                return null;
            }
            File baseDirFO = new File (targetFolder, baseDir.getName ());
            baseDirFO.mkdir ();
                        
            // create directories
            String [] subdirs = new String [] { 
                "conf",   // NOI18N
                "conf/Catalina",   // NOI18N
                "conf/Catalina/localhost",   // NOI18N
                "logs",   // NOI18N
                "work",   // NOI18N
                "temp",   // NOI18N
                "webapps" // NOI18N
            };
            for (int i = 0; i<subdirs.length; i++) {
                File dest = new File (baseDirFO, subdirs [i]);
                dest.mkdirs ();
            }
            // copy config files
            String [] files = new String [] { 
                "conf/catalina.policy",   // NOI18N
                "conf/catalina.properties",   // NOI18N
                "conf/server.xml",   // NOI18N
                "conf/tomcat-users.xml",   // NOI18N
                "conf/web.xml",   // NOI18N
                "conf/Catalina/localhost/admin.xml",   // NOI18N
                "conf/Catalina/localhost/manager.xml",   // NOI18N
                "conf/Catalina/localhost/balancer.xml"   // NOI18N
            };
            String [] patternFrom = new String [] { 
                null, 
                null, 
                "</Host>",   // NOI18N
                "</tomcat-users>",   // NOI18N
                null, 
                "docBase=\"../server/webapps/admin\"", 
                "docBase=\"../server/webapps/manager\"",
                "docBase=\"balancer\""
            };
            String [] patternTo = new String [] { 
                null, 
                null, 
                "<Context path=\"\" docBase=\""+new File (homeDir, "webapps/Root").getAbsolutePath ()+"\" debug=\"0\"/>\n"+
                "<Context path=\"/jsp-examples\" docBase=\""+new File (homeDir, "webapps/jsp-examples").getAbsolutePath ()+"\" debug=\"0\"/>\n"+
                "<Context path=\"/servlets-examples\" docBase=\""+new File (homeDir, "webapps/servlets-examples").getAbsolutePath ()+"\" debug=\"0\"/>\n"+
                "</Host>",   // NOI18N
                "<user username=\"ide\" password=\"ide_manager\" roles=\"manager\"/>\n</tomcat-users>",   // NOI18N
                null, 
                "docBase=\""+new File (homeDir, "server/webapps/admin").getAbsolutePath ()+"\"",   // NOI18N
                "docBase=\""+new File (homeDir, "server/webapps/manager").getAbsolutePath ()+"\"",   // NOI18N
                "docBase=\""+new File (homeDir, "webapps/balancer").getAbsolutePath ()+"\""   // NOI18N
            };
            for (int i = 0; i<files.length; i++) {
                // get folder from, to, name and ext
                int slash = files[i].lastIndexOf ('/');
                String sfolder = files[i].substring (0, slash);
                File fromDir = new File (homeDir, sfolder); // NOI18N
                File toDir = new File (baseDir, sfolder); // NOI18N

                if (patternTo[i] == null) {
                    FileInputStream is = new FileInputStream (new File (fromDir, files[i].substring (slash+1)));
                    FileOutputStream os = new FileOutputStream (new File (toDir, files[i].substring (slash+1)));
                    try {
                        final byte[] BUFFER = new byte[4096];
                        int len;

                        for (;;) {
                            len = is.read (BUFFER);
                            if (len == -1) break;
                            os.write (BUFFER, 0, len);
                        }
                    } catch (java.io.IOException ioe) {
                        ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
                    } finally {
                        try { if (os != null) os.close (); } catch (java.io.IOException ioe) { // ignore this
                        }
                        try { if (is != null) is.close (); } catch (java.io.IOException ioe) { // ignore this 
                        }
                    }
                }
                else {
                    // use patched version
                    if (!copyAndPatch (
                        new File (fromDir, files[i].substring (slash+1)), 
                        new File (toDir, files[i].substring (slash+1)), 
                        patternFrom[i],
                        patternTo[i]
                        )) {
                        ErrorManager.getDefault ().log (ErrorManager.INFORMATIONAL, "Cannot create config file "+files[i]);
                        return null;
                    }
                }
            }
        }
        catch (java.io.IOException ioe) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
            return null;
        }
        return baseDir;
    }
    
    /** Copies server.xml file and patches appBase="webapps" to
     * appBase="$CATALINA_HOME/webapps" during the copy.
     * @return success status.
     */
    private boolean copyAndPatch (File src, File dst, String from, String to) {
        java.io.Reader r = null;
        java.io.Writer out = null;
        try {
            r = new BufferedReader (new InputStreamReader (new FileInputStream (src), "utf-8")); // NOI18N
            StringBuffer sb = new StringBuffer ();
            final char[] BUFFER = new char[4096];
            int len;

            for (;;) {
                len = r.read (BUFFER);
                if (len == -1) break;
                sb.append (BUFFER, 0, len);
            }
            int idx = sb.toString ().indexOf (from);
            if (idx >= 0) {
                sb.replace (idx, idx+from.length (), to);  // NOI18N
            }
            else {
                // Something unexpected
                TomcatFactory.getEM ().log (ErrorManager.WARNING, "Pattern "+from+" not found in "+src.getPath ());
            }
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream (dst), "utf-8")); // NOI18N
            out.write (sb.toString ());
            
        } catch (java.io.IOException ioe) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, ioe);
            return false;
        } finally {
            try { if (out != null) out.close (); } catch (java.io.IOException ioe) { // ignore this
            }
            try { if (r != null) r.close (); } catch (java.io.IOException ioe) { // ignore this 
            }
        }
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
    
    public ProgressObject incrementalDeploy (final TargetModuleID module, org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor changes) {
        if (changes.descriptorChanged () || changes.classesChanged ()) {
            TomcatManagerImpl tmi = new TomcatManagerImpl (tm);
            tmi.reload ((TomcatModule)module);
            return tmi;
        } else {
            final P p = new P (module);
            p.supp.fireHandleProgressEvent (module, new Status (ActionType.EXECUTE, CommandType.DISTRIBUTE, "", StateType.COMPLETED));
            Task t = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        p.supp.fireHandleProgressEvent (module, new Status (ActionType.EXECUTE, CommandType.DISTRIBUTE, "", StateType.COMPLETED));
                        
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            return p;
        }
    }
    
    public String[] getDeploymentPlanFilenames (ModuleType type) {
        if (type.equals (ModuleType.WAR)) {
            return new String [] {DEPLOYMENT_PLAN_FNAME};
        } else {
            return new String [] {};
        }
    }
    
    public File getDirectoryForModule (TargetModuleID module) {
        TomcatModule tModule = (TomcatModule) module;
        String moduleFolder = tm.getCatalinaBaseDir ().getAbsolutePath () 
            + System.getProperty("file.separator") + "webapps"   //NOI18N
            + System.getProperty("file.separator") + tModule.getPath ().substring (1); //NOI18N
        File f = new File (moduleFolder);
        return f;
    }
    
    public String getModuleUrl (TargetModuleID module) {
        if (module instanceof TomcatModule) {
            return ((TomcatModule) module).getWebURL ();
        } else {
            throw new IllegalArgumentException ();
        }
    }
    
    public void writeDeploymentPlanFiles (javax.enterprise.deploy.spi.DeploymentConfiguration configuration, javax.enterprise.deploy.model.DeployableObject module, File[] files) {
    }
    
    public ProgressObject initialDeploy (Target target, javax.enterprise.deploy.model.DeployableObject app, DeploymentConfiguration configuration, File dir) {
        TomcatManagerImpl tmi = new TomcatManagerImpl (tm);
        tmi.initialDeploy (target, ((WebappConfiguration)configuration).getPath (), dir);
        return tmi;
    }
    
    public boolean canFileDeploy (Target target, javax.enterprise.deploy.model.DeployableObject deployable) {
        return deployable.getType ().equals (javax.enterprise.deploy.shared.ModuleType.WAR);
        
    }
    
    public File getDirectoryForNewApplication (Target target, DeployableObject module, DeploymentConfiguration configuration) {
        if (module.getType ().equals (ModuleType.WAR)) {
            if (configuration instanceof WebappConfiguration) {
                String moduleFolder = tm.getCatalinaBaseDir ().getAbsolutePath () 
                    + System.getProperty("file.separator") + "webapps"   //NOI18N
                    + System.getProperty("file.separator") + ((WebappConfiguration)configuration).getPath ().substring (1);  //NOI18N
                File f = new File (moduleFolder);
                return f;
            }
        }
        throw new IllegalArgumentException ("ModuleType:" + module == null ? null : module.getType () + " Configuration:"+configuration); //NOI18N
    }

    public File getDirectoryForNewModule(File appDir, String uri, DeployableObject module, DeploymentConfiguration configuration) {
        throw new UnsupportedOperationException ();
    }

    /** Format that provides value usefull for Tomcat execution. 
     * Currently this is only the name of startup wrapper.
    */
    private static class TomcatFormat extends org.openide.util.MapFormat {
        
        private static final long serialVersionUID = 992972967554321415L;

        public TomcatFormat (String home) {
            super(new java.util.HashMap ());
            java.util.Map map = getMap ();
            map.put (TAG_STARTUP_CMD, org.openide.util.Utilities.isWindows ()? "startup.bat": "startup.sh"); // NOI18N
            map.put (TAG_SHUTDOWN_CMD, org.openide.util.Utilities.isWindows ()? "shutdown.bat": "shutdown.sh"); // NOI18N
            map.put (TAG_DEBUG_CMD, org.openide.util.Utilities.isWindows ()? "catalina.bat": "catalina.sh"); // NOI18N
            map.put (TAG_JPDA, "jpda"); // NOI18N
            map.put (TAG_JPDA_STARTUP, "start"); // NOI18N
            map.put (TAG_JPDA_SHUTDOWN, "stop"); // NOI18N
            map.put (StartTomcat.TAG_CATALINA_HOME, home); // NOI18N
            map.put (ProcessExecutor.Format.TAG_SEPARATOR, File.separator);
        }
    }
    
    public String toString () {
        return "StartTomcat [" + tm + "]";
    }
    
    private static class P implements ProgressObject {
        
        ProgressEventSupport supp = new ProgressEventSupport (this);
        TargetModuleID tmid;
        
        P (TargetModuleID tmid) {
            this.tmid = tmid;
        }
        
        public void addProgressListener (javax.enterprise.deploy.spi.status.ProgressListener progressListener) {
            supp.addProgressListener (progressListener);
        }
        
        public void removeProgressListener (javax.enterprise.deploy.spi.status.ProgressListener progressListener) {
            supp.removeProgressListener (progressListener);
        }
        
        public javax.enterprise.deploy.spi.status.ClientConfiguration getClientConfiguration (javax.enterprise.deploy.spi.TargetModuleID targetModuleID) {
            return null;
        }
        
        public javax.enterprise.deploy.spi.status.DeploymentStatus getDeploymentStatus () {
            return supp.getDeploymentStatus ();
        }
        
        public javax.enterprise.deploy.spi.TargetModuleID[] getResultTargetModuleIDs () {
            return new TargetModuleID [] {tmid};
        }
        
        public boolean isCancelSupported () {
            return false;
        }
        
        public boolean isStopSupported () {
            return false;
        }
        
        public void cancel () throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException {
            throw new OperationUnsupportedException ("");
        }
        
        public void stop () throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException {
            throw new OperationUnsupportedException ("");
        }
        
    }
}
