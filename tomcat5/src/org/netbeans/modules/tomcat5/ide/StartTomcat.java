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

import java.io.File;
import java.util.StringTokenizer;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.execution.ProcessExecutor;
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
                ProcessExecutor.Format.TAG_SEPARATOR + "}" +     // NOI18N
                StartTomcat.TAG_STARTUP_CMD,
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
        StringTokenizer stok = new StringTokenizer (uri, "@");
        String home = stok.nextToken ();
        String base = stok.hasMoreTokens ()? stok.nextToken (): home;
        
        InstalledFileLocator ifl = InstalledFileLocator.getDefault ();
        File homeDir = new File (home);
        if (!homeDir.isAbsolute ()) {
            homeDir = ifl.locate (home, null, false);
        }
        
        File baseDir = new File (base);
        if (!baseDir.isAbsolute ()) {
            baseDir = ifl.locate (base, null, false);
            // PENDING if it doesn't exist yet create it and then start
        }
        // XXX check for null's
        
        NbProcessDescriptor pd  = defaultExecDesc ();
        try { 
            pd.exec (
                new TomcatFormat (), 
                new String[] { 
                    "CATALINA_HOME="+homeDir.getAbsolutePath (), 
                    "CATALINA_BASE="+baseDir.getAbsolutePath () 
                }
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
    
    /** Format that provides value usefull for Tomcat execution. 
     * Currently this is only the name of startup wrapper.
    */
    private static class TomcatFormat extends org.openide.util.MapFormat {
        
        private static final long serialVersionUID = 992972967554321415L;

        public TomcatFormat (
        ) {
            super(new java.util.HashMap ());
            java.util.Map map = getMap ();
            map.put (TAG_STARTUP_CMD, org.openide.util.Utilities.isWindows ()? "startup.bat": "startup.sh"); // NOI18N
        }
    }
    
}
