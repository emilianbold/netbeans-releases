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
import java.util.StringTokenizer;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.progress.ProgressEventSupport;
import org.netbeans.modules.tomcat5.progress.Status;
import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.ProcessExecutor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** Extension to JSR88 that enables starting of Tomcat.
 *
 * <p>
 * TODO: create lib and classes dirs in base dir and modify catalina.policy to load from them
 * TODO: progress when starting
 *
 * @author Radim Kubacki
 */
public final class StartTomcat implements StartServer, Runnable, ProgressObject
{
    public static final String TAG_CATALINA_HOME = "catalina_home"; // NOI18N
    public static final String TAG_CATALINA_BASE = "catalina_base"; // NOI18N
    
    /** Startup command tag. */
    public static final String TAG_STARTUP_CMD   = "startup"; // NOI18N
    
    private static NbProcessDescriptor defaultExecDesc() {
        return new NbProcessDescriptor (
                "{" + StartTomcat.TAG_CATALINA_HOME + "}{" +     // NOI18N
                ProcessExecutor.Format.TAG_SEPARATOR + "}bin{" + // NOI18N
                ProcessExecutor.Format.TAG_SEPARATOR + "}{" +     // NOI18N
                StartTomcat.TAG_STARTUP_CMD + "}",  // NOI18N
                "", 
                org.openide.util.NbBundle.getMessage (StartTomcat.class, "MSG_TomcatExecutionCommand")
            );     
    }

    private TomcatManager tm;
    
    private ProgressEventSupport pes;
    
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
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, CommandType.START, "", StateType.RUNNING));
        RequestProcessor.getDefault ().post (this, 0, Thread.NORM_PRIORITY);
        return this;
    }
    
    public synchronized void run () {
        // PENDING check whether is runs or not
        String home = tm.getCatalinaHome ();
        String base = tm.getCatalinaBase ();
        if (home == null) {
            // no home - start not supported
            pes.fireHandleProgressEvent (
                null, new Status (ActionType.EXECUTE, CommandType.START, NbBundle.getMessage (StartTomcat.class, "MSG_notStarting"), StateType.COMPLETED));
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
        }
        // XXX check for null's
        
        NbProcessDescriptor pd  = defaultExecDesc ();
        try { 
            pes.fireHandleProgressEvent (
                null, 
                new Status (
                    ActionType.EXECUTE, 
                    CommandType.START, 
                    NbBundle.getMessage (StartTomcat.class, "MSG_startProcess"), 
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
                new Status (ActionType.EXECUTE, CommandType.START, ioe.getLocalizedMessage (), StateType.FAILED)
            );
        }
        
        // XXX probably better is to check exit status before reporting COMPLETED
        pes.fireHandleProgressEvent (null, new Status (ActionType.EXECUTE, CommandType.START, "", StateType.COMPLETED));
    }
    
    /** This implementation does nothing.
     * Target is already started when Tomcat starts.
     */
    public ProgressObject startServer (Target target) {
        
        return null; // PENDING
    }
    
    public boolean supportsDebugging (Target target) {
        return false;   // PENDING
    }
    
    public void stopDebugging (Target target) {
        throw new RuntimeException ("Tomcat debugging not supported yet");    // NOI18N
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
        FileObject targetFolder;
        if (!baseDir.isAbsolute ()) {
            baseDir = new File(System.getProperty("netbeans.user")+System.getProperty("file.separator")+baseDir);
            FileObject [] targetFO = FileUtil.fromFile (new File(System.getProperty("netbeans.user")));
            targetFolder = targetFO.length > 0? targetFO[0]: null;
        }
        else {
            FileObject [] targetFO = FileUtil.fromFile (baseDir.getParentFile ());
            targetFolder = targetFO.length > 0? targetFO[0]: null;
        }
        try {
            
            if (targetFolder == null) {
                TomcatFactory.getEM ().log (ErrorManager.INFORMATIONAL, "Cannot find parent folder for base dir "+baseDir.getPath ());
                return null;
            }
            FileObject baseDirFO = targetFolder.createFolder (baseDir.getName ());
            // create directories
            String [] subdirs = new String [] { 
                "conf",   // NOI18N
                "conf/Catalina",   // NOI18N
                "conf/Catalina/localhost",   // NOI18N
                "logs",   // NOI18N
                "work",   // NOI18N
                "temp"    // NOI18N
                /*, "webapps"*/ 
            };
            for (int i = 0; i<subdirs.length; i++) {
                StringTokenizer stok = new StringTokenizer (subdirs[i], "/"); // NOI18N
                FileObject dest = baseDirFO;
                String folder = null;
                while (stok.hasMoreTokens ()) {
                    folder = stok.nextToken ();
                    if (stok.hasMoreTokens ()) {
                        dest = dest.getFileObject (folder);
                    }
                }
                dest.createFolder (folder);
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
            };
            String [] patternFrom = new String [] { 
                null, 
                null, 
                "appBase=\"webapps\"",   // NOI18N
                "</tomcat-users>",   // NOI18N
                null, 
                null, 
                null 
            };
            String [] patternTo = new String [] { 
                null, 
                null, 
                "appBase=\""+new File (homeDir, "webapps").getAbsolutePath ()+"\"",   // NOI18N
                "<user username=\"ide\" password=\"ide_manager\" roles=\"admin,manager\"/>\n</tomcat-users>",   // NOI18N
                null, 
                null, 
                null 
            };
            for (int i = 0; i<files.length; i++) {
                // get folder from, to, name and ext
                int slash = files[i].lastIndexOf ('/');
                int dot = files[i].lastIndexOf ('.');
                String sfolder = files[i].substring (0, slash);
                String sname = files[i].substring (slash+1, dot);
                String sext = files[i].substring (dot+1);
                File fromDir = new File (homeDir, sfolder); // NOI18N
                File toDir = new File (baseDir, sfolder); // NOI18N
                FileObject [] fromFO = FileUtil.fromFile (fromDir);
                FileObject toFO = baseDirFO.getFileObject (sfolder);
                if (fromFO.length == 0 || toFO == null) {
                    TomcatFactory.getEM ().log (ErrorManager.INFORMATIONAL, "Cannot find FileObject for home dir or base dir");
                    return null;
                }       
                
                FileObject source = fromFO[0].getFileObject (sname, sext);
                if (source == null) {
                    TomcatFactory.getEM ().log (ErrorManager.INFORMATIONAL, "Cannot find config file "+sname+"."+sext);
                    return null;
                }
                if (patternTo[i] == null) {
                    FileUtil.copyFile (source, toFO, sname, sext);
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
    
    /** Format that provides value usefull for Tomcat execution. 
     * Currently this is only the name of startup wrapper.
    */
    private static class TomcatFormat extends org.openide.util.MapFormat {
        
        private static final long serialVersionUID = 992972967554321415L;

        public TomcatFormat (String home) {
            super(new java.util.HashMap ());
            java.util.Map map = getMap ();
            map.put (TAG_STARTUP_CMD, org.openide.util.Utilities.isWindows ()? "startup.bat": "startup.sh"); // NOI18N
            map.put (StartTomcat.TAG_CATALINA_HOME, home); // NOI18N
            map.put (ProcessExecutor.Format.TAG_SEPARATOR, File.separator);
        }
    }
    
}
