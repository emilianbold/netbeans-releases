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
import org.netbeans.modules.tomcat5.TomcatManager;
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
    
    public void setDeploymentManager (javax.enterprise.deploy.spi.DeploymentManager manager) {
        tm = (TomcatManager)manager;
    }
    
    public boolean supportsStartDeploymentManager () {
        return true;
    }
    
    /** Start Tomcat server if the TomcatManager is not connected.
     */
    public ProgressObject startDeploymentManager () {
        if (tm.isConnected ())
            return null;
        String uri = tm.getUri ();
        String home = tm.getCatalinaHome ();
        String base = tm.getCatalinaBase ();
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
        
        return null; // PENDING
    }
    
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
     *  @return File with absolute path for created dir.
     */
    private File createBaseDir (File baseDir, File homeDir) {
        if (!baseDir.isAbsolute ()) {
            baseDir = new File(System.getProperty("netbeans.user")+System.getProperty("file.separator")+baseDir);
        }
        try {
            baseDir.mkdir ();
            // create directories
            String [] subdirs = new String [] { "conf", "logs", "work", "temp" /*, "webapps"*/ };
            for (int i = 0; i<subdirs.length; i++) {
                new File (baseDir, subdirs[i]).mkdir ();
            }
            // copy config files
            File confDir = new File (baseDir, "conf");
            String [] files = new String [] { 
                "catalina", 
                "catalina", 
                "web", 
            };
            String [] exts = new String [] { 
                "policy", 
                "properties", 
                "xml", 
            };
            File homeConfDir = new File (homeDir, "conf");
            FileObject [] homeFO = FileUtil.fromFile (homeConfDir);
            FileObject [] baseFO = FileUtil.fromFile (confDir);
            if (homeFO.length == 0 || baseFO.length == 0) {
                throw new IllegalStateException ("Cannot create base dir");
            }       
            for (int i = 0; i<files.length; i++) {
                FileUtil.copyFile (homeFO[0].getFileObject (files[i], exts[i]), baseFO[0], files[i], exts[i]);
            }
            // modify server.xml
            copyAndPatch (
                new File (homeConfDir, "server.xml"), 
                new File (confDir, "server.xml"), 
                "appBase=\"webapps\"",
                "appBase=\""+new File (homeDir, "webapps").getAbsolutePath ()+"\""
            );
            // modify tomcat-users.xml
            copyAndPatch (
                new File (homeConfDir, "tomcat-users.xml"), 
                new File (confDir, "tomcat-users.xml"), 
                "</tomcat-users>",
                "<user username=\"ide\" password=\"ide_manager\" roles=\"admin,manager\"/>\n</tomcat-users>"
            );
        }
        catch (java.io.IOException ioe) {
            System.err.println("!!! createBaseDir failed");
            ioe.printStackTrace ();
            throw new IllegalStateException ("Cannot create base dir");
        }
        return baseDir;
    }
    
    /** Copies server.xml file and patches appBase="webapps" to
     * appBase="$CATALINA_HOME/webapps" during the copy.
     */
    private void copyAndPatch (File src, File dst, String from, String to) {
        java.io.Reader r = null;
        java.io.Writer out = null;
        try {
            r = new BufferedReader (new InputStreamReader (new FileInputStream (src), "utf-8"));
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
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream (dst), "utf-8"));
            out.write (sb.toString ());
            
        } catch (java.io.IOException ioe) {
        } finally {
            try { if (out != null) out.close (); } catch (java.io.IOException ioe) {}
            try { if (r != null) r.close (); } catch (java.io.IOException ioe) {}
        }
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
