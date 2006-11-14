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

package org.netbeans.modules.j2ee.jboss4.ide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.util.JBProperties;
import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Kirill Sorokin
 * @author Libor Kotouc
 */
class JBStopRunnable implements Runnable {

    public static final boolean VERBOSE = 
        System.getProperty ("netbeans.serverplugins.jboss4.logging") != null;
    
    private final static String SHUTDOWN_SH = "/bin/shutdown.sh";//NOI18N
    private final static String SHUTDOWN_BAT = "/bin/shutdown.bat";//NOI18N

    private static int TIMEOUT = 300000;
    
    private JBDeploymentManager dm;
    private JBStartServer startServer;

    JBStopRunnable(JBDeploymentManager dm, JBStartServer startServer) {
        this.dm = dm;
        this.startServer = startServer;
    }
    
    private String[] createEnvironment() {
        
        JBProperties properties = dm.getProperties();

        JavaPlatform platform = properties.getJavaPlatform();
        FileObject fo = (FileObject) platform.getInstallFolders().iterator().next();
        String javaHome = FileUtil.toFile(fo).getAbsolutePath();
        List<String> envp = new ArrayList<String>(3);
        envp.add("JAVA=" + javaHome + "/bin/java"); // NOI18N
        envp.add("JAVA_HOME=" + javaHome); // NOI18N
        if (Utilities.isWindows()) {
            // the shutdown script should not wait for a key press
            envp.add("NOPAUSE=true"); // NOI18N
        }
        return (String[]) envp.toArray(new String[envp.size()]);
    }
    
    public void run() {

        InstanceProperties ip = dm.getInstanceProperties();

        String configName = ip.getProperty("server"); // NOI18N
        if ("minimal".equals(configName)) { // NOI18N
            startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED, NbBundle.getMessage(JBStopRunnable.class, "MSG_STOP_SERVER_FAILED_MINIMAL")));//NOI18N
            return;
        }

        String serverName = ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);

        String serverLocation = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
        String serverStopFileName = serverLocation + (Utilities.isWindows() ? SHUTDOWN_BAT : SHUTDOWN_SH);

        File serverStopFile = new File(serverStopFileName);
        if (!serverStopFile.exists()){
            startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED, NbBundle.getMessage(JBStopRunnable.class, "MSG_STOP_SERVER_FAILED_FNF", serverName)));//NOI18N
            return;
        }
        
        JBProperties properties = dm.getProperties();
        StringBuilder credentialsParams = new StringBuilder(32);
        credentialsParams.append(" -u ").append(properties.getUsername()).append(" -p ").append(properties.getPassword()); // NOI18N
        // Currently there is a problem stopping JBoss when Profiler agent is loaded.
        // As a workaround for now, --halt parameter has to be used for stopping the server.
        NbProcessDescriptor pd = (startServer.getMode() == JBStartServer.MODE.PROFILE ? 
                                  new NbProcessDescriptor(serverStopFileName, "--halt=0 " + credentialsParams) :   // NOI18N
                                  new NbProcessDescriptor(serverStopFileName, "--shutdown " + credentialsParams)); // NOI18N

        Process stoppingProcess = null;
        try {
            String envp[] = createEnvironment();
            stoppingProcess = pd.exec(null, envp, true, null);
        } catch (java.io.IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);

            startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                    NbBundle.getMessage(JBStopRunnable.class, "MSG_STOP_SERVER_FAILED_PD", serverName, serverStopFileName)));//NOI18N

            return;
        }

        startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                NbBundle.getMessage(JBStopRunnable.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)));

        if (VERBOSE) {
            System.out.println("JBStopRunnable: Entering the loop"); // NOI18N
        }
        
        int elapsed = 0;
        while (elapsed < TIMEOUT) {
            // check whether the stopping process did not fail
            try {
                int processExitValue = stoppingProcess.exitValue();
                if (VERBOSE) {
                    System.out.println("JBStopRunnable: the stopping process has terminated with the exit value " + processExitValue); // NOI18N
                }
                if (processExitValue != 0) {
                    // stopping process failed
                    String msg = NbBundle.getMessage(JBStopRunnable.class, "MSG_STOP_SERVER_FAILED", serverName);
                    startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED, msg));
                    return;
                }
            } catch (IllegalThreadStateException e) {
                // process is still running
            }
            if (startServer.isRunning()) {
                startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                        NbBundle.getMessage(JBStopRunnable.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)));//NOI18N
                if (VERBOSE) {
                    System.out.println("JBStopRunnable: STOPPING message fired"); // NOI18N
                }
                try {
                    elapsed += 500;
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
            } else {
                if (VERBOSE) {
                    System.out.println("JBStopRunnable: JBoss has been stopped, going to stop the Log Writer thread");
                }
                final JBLogWriter logWriter = JBLogWriter.getInstance(ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR));
                if (logWriter != null && logWriter.isRunning()) {
                    logWriter.waitForServerProcessFinished(10000);
                    logWriter.stop();
                }

                startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.COMPLETED,
                        NbBundle.getMessage(JBStopRunnable.class, "MSG_SERVER_STOPPED", serverName)));//NOI18N
                if (VERBOSE) {
                    System.out.println("JBStopRunnable: STOPPED message fired"); // NOI18N
                }

                return;
            }
        }
        
        startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                NbBundle.getMessage(JBStopRunnable.class, "MSG_StopServerTimeout")));
        if (stoppingProcess != null) {
            stoppingProcess.destroy();
        }
        
        if (VERBOSE) {
            System.out.println("JBStopRunnable: TIMEOUT EXPIRED"); // NOI18N
        }
    }
}
    
