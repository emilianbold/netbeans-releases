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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.ProcessExecutor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/** Extension to JSR88 that enables starting of Tomcat.
 *
 * @author Radim Kubacki
 */
public final class StartTomcat implements StartServer
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
    
    /** Default constructor. */
    public StartTomcat () {
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
        // PENDING check whether is runs or not
        System.out.println("begining");
        String uri = tm.getUri ();
        String home = tm.getCatalinaHome ();
        String base = tm.getCatalinaBase ();
        if (base == null) {
            base = home;
        }
        
        System.out.println("get home");
        InstalledFileLocator ifl = InstalledFileLocator.getDefault ();
        File homeDir = new File (home);
        if (!homeDir.isAbsolute ()) {
            homeDir = ifl.locate (home, null, false);
        }
        
        System.out.println("get base");
        File baseDir = new File (base);
        if (!baseDir.isAbsolute ()) {
            File baseDir2 = ifl.locate (base, null, false);
            if (baseDir2 == null) {
                baseDir = createBaseDir (baseDir, homeDir);
            }
        }
        // XXX check for null's
        
        System.out.println("exec");
        NbProcessDescriptor pd  = defaultExecDesc ();
        try { 
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
            org.openide.ErrorManager.getDefault ().notify (ioe);
            return null;
        }
        
        System.out.println("done");
        return null; // PENDING
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
            String [] subdirs = new String [] { "conf", "logs", "work", "temp" /*, "webapps"*/ };
            for (int i = 0; i<subdirs.length; i++) {
                baseDirFO.createFolder (subdirs[i]);
            }
            // copy config files
            File confDir = new File (baseDir, "conf");  // NOI18N
            String [] files = new String [] { 
                "catalina", // NOI18N
                "catalina", // NOI18N 
                "web"       // NOI18N
            };
            String [] exts = new String [] { 
                "policy",     // NOI18N 
                "properties", // NOI18N
                "xml"          // NOI18N
            };
            File homeConfDir = new File (homeDir, "conf"); // NOI18N
            FileObject [] homeFO = FileUtil.fromFile (homeConfDir);
            FileObject baseConfFO = baseDirFO.getFileObject ("conf");
            if (homeFO.length == 0 || baseConfFO == null) {
                TomcatFactory.getEM ().log (ErrorManager.INFORMATIONAL, "Cannot find FileObject for home dir or base dir");
                return null;
            }       
            for (int i = 0; i<files.length; i++) {
                FileObject source = homeFO[0].getFileObject (files[i], exts[i]);
                if (source == null) {
                    TomcatFactory.getEM ().log (ErrorManager.INFORMATIONAL, "Cannot find config file "+files[i]+"."+exts[i]);
                    return null;
                }
                FileUtil.copyFile (homeFO[0].getFileObject (files[i], exts[i]), baseConfFO, files[i], exts[i]);
            }
            // modify server.xml
            if (!copyAndPatch (
                new File (homeConfDir, "server.xml"), 
                new File (confDir, "server.xml"), 
                "appBase=\"webapps\"",
                "appBase=\""+new File (homeDir, "webapps").getAbsolutePath ()+"\""
                )) {
                ErrorManager.getDefault ().log (ErrorManager.INFORMATIONAL, "Cannot create config file server.xml");
                return null;
            }
            // modify tomcat-users.xml
            if (!copyAndPatch (
                new File (homeConfDir, "tomcat-users.xml"), 
                new File (confDir, "tomcat-users.xml"), 
                "</tomcat-users>",
                "<user username=\"ide\" password=\"ide_manager\" roles=\"admin,manager\"/>\n</tomcat-users>"
                )) {
                // might not be a bug
                ErrorManager.getDefault ().log (ErrorManager.INFORMATIONAL, "Cannot create config file tomcat-users.xml");
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
