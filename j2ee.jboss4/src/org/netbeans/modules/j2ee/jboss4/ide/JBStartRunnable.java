/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.jboss4.ide;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.netbeans.modules.j2ee.jboss4.util.JBProperties;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 *
 * @author Kirill Sorokin
 * @author Libor Kotouc
 */
class JBStartRunnable implements Runnable {

    private final static String STARTUP_SH = "/bin/run.sh";
    private final static String STARTUP_BAT = "/bin/run.bat";

    private JBDeploymentManager dm;
    private String instanceName;
    private JBStartServer startServer;
    private ProfilerServerSettings profilerSettings;

    JBStartRunnable(ProfilerServerSettings profilerSettings, JBDeploymentManager dm, JBStartServer startServer) {
        this.dm = dm;
        this.instanceName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
        this.startServer = startServer;
        this.profilerSettings = profilerSettings;
    }

    public void run() {

        InstanceProperties ip = dm.getInstanceProperties();

        boolean free = checkPorts(ip);
        if (!free) {
            return;
        }

        Process serverProcess = createProcess(ip);
        if (serverProcess == null) {
            return;
        }

        JBLogWriter logWriter = createLogWriter();
        
        waitForServerToStart(logWriter, serverProcess);
    }

    private String[] createEnvironment(final InstanceProperties ip) {
        
        JBProperties properties = dm.getProperties();

        // set the JAVA_OPTS value
        String javaOpts = properties.getJavaOpts();
        StringBuilder javaOptsBuilder = new StringBuilder(javaOpts);
        // use the IDE proxy settings if the 'use proxy' checkbox is selected
        // do not override a property if it was set manually by the user
        if (properties.getProxyEnabled()) {
            final String[] PROXY_PROPS = {
                "http.proxyHost",       // NOI18N
                "http.proxyPort",       // NOI18N
                "http.nonProxyHosts",   // NOI18N
                "https.proxyHost",      // NOI18N
                "https.proxyPort",      // NOI18N
            };
            for (String prop : PROXY_PROPS) {
                if (javaOpts.indexOf(prop) == -1) {
                    String value = System.getProperty(prop);
                    if (value != null) {
                        if ("http.nonProxyHosts".equals(prop)) { // NOI18N
                            try {
                                // remove newline characters, as the value may contain them, see issue #81174
                                BufferedReader br = new BufferedReader(new StringReader(value));
                                String line = null;
                                StringBuilder noNL = new StringBuilder();
                                while ((line = br.readLine()) != null) {
                                    noNL.append(line);
                                }
                                value = noNL.toString();

                                // enclose the host list in double quotes because it may contain spaces
                                value = "\"" + value + "\""; // NOI18N
                            }
                            catch (IOException ioe) {
                                Exceptions.attachLocalizedMessage(ioe, NbBundle.getMessage(JBStartRunnable.class, "ERR_NonProxyHostParsingError"));
                                Logger.getLogger("global").log(Level.WARNING, null, ioe);
                                value = null;
                            }
                        }
                        if (value != null) {
                            javaOptsBuilder.append(" -D").append(prop).append("=").append(value); // NOI18N
                        }
                    }
                }
            }
        }

        if (startServer.getMode() == JBStartServer.MODE.DEBUG && javaOptsBuilder.toString().indexOf("-Xdebug") == -1) { // NOI18N
            // if in debug mode and the debug options not specified manually
            javaOptsBuilder.append(" -classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address="). // NOI18N
                            append(dm.getDebuggingPort()).
                            append(",server=y,suspend=n"); // NOI18N
        } 
        else
        if (startServer.getMode() == JBStartServer.MODE.PROFILE) {

            // get JVM arguments used for starting the server
            String[] profJvmArgs = profilerSettings.getJvmArgs();
            for (int i = 0; i < profJvmArgs.length; i++) {
                javaOptsBuilder.append(" ").append(profJvmArgs[i]); // NOI18N
            }
        }

        // create new environment for server
        javaOpts = javaOptsBuilder.toString();

        // get Java platform that will run the server
        JavaPlatform platform = (startServer.getMode() != JBStartServer.MODE.PROFILE ? properties.getJavaPlatform() : profilerSettings.getJavaPlatform());
        String javaHome = getJavaHome(platform);

        String envp[] = new String[] {
            "JAVA=" + javaHome + "/bin/java",   // NOI18N
            "JAVA_HOME=" + javaHome,            // NOI18N
            "JAVA_OPTS=" + javaOpts,            // NOI18N
        };
        return envp;
    }
    
    private boolean checkPorts(final InstanceProperties ip) {

        try {
            String serverName = ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);

            String strHTTPConnectorPort = ip.getProperty(JBPluginProperties.PROPERTY_PORT);
            int HTTPConnectorPort = new Integer(strHTTPConnectorPort).intValue();
            if (!JBPluginUtils.isPortFree(HTTPConnectorPort)) {
                fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_HTTP_PORT_IN_USE", strHTTPConnectorPort));
                return false;
            }

            String serverDir = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);

            String strJNPServicePort = JBPluginUtils.getJnpPort(serverDir);
            int JNPServicePort = new Integer(strJNPServicePort).intValue();
            if (!JBPluginUtils.isPortFree(JNPServicePort)) {
                fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_JNP_PORT_IN_USE", strJNPServicePort));//NOI18N
                return false;
            }

            String strRMINamingServicePort = JBPluginUtils.getRMINamingServicePort(serverDir);
            int RMINamingServicePort = new Integer(strRMINamingServicePort).intValue();
            if (!JBPluginUtils.isPortFree(RMINamingServicePort)) {
                fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_RMI_PORT_IN_USE", strRMINamingServicePort));//NOI18N
                return false;
            }

            String server = ip.getProperty(JBPluginProperties.PROPERTY_SERVER);
            if (!"minimal".equals(server)) {
                String strRMIInvokerPort = JBPluginUtils.getRMIInvokerPort(serverDir);
                int RMIInvokerPort = new Integer(strRMIInvokerPort).intValue();
                if (!JBPluginUtils.isPortFree(RMIInvokerPort)) {
                    fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_INVOKER_PORT_IN_USE", strRMIInvokerPort));//NOI18N
                    return false;
                }
            }

        } catch (NumberFormatException nfe) {
            // continue and let server to report the problem
        }
        
        return true;
    }

    private NbProcessDescriptor createProcessDescriptor(InstanceProperties ip) {
        
        final String serverLocation = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
        final String serverRunFileName = serverLocation + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH);
        if (!new File(serverRunFileName).exists()){
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_FNF"));//NOI18N
            return null;
        }

        final String instanceName = ip.getProperty(JBPluginProperties.PROPERTY_SERVER);
        String args = ("all".equals(instanceName) ? "-b 127.0.0.1 " : "") + "-c " + instanceName; // NOI18N
        return new NbProcessDescriptor(serverRunFileName, args);
    }

    
    private static String getJavaHome(JavaPlatform platform) {
        FileObject fo = (FileObject)platform.getInstallFolders().iterator().next();
        return FileUtil.toFile(fo).getAbsolutePath();
    }
 
    private String createProgressMessage(final String resName) {
        return createProgressMessage(resName, null);
    }
    
    private String createProgressMessage(final String resName, final String param) {
        return NbBundle.getMessage(JBStartRunnable.class, resName, instanceName, param);
    }

    private Process createProcess(InstanceProperties ip) {
        
        //TODO do we really have to stop the log writer?
        if (startServer.getMode() == JBStartServer.MODE.PROFILE) {

            // stop logger if running
            JBLogWriter logWriter = JBLogWriter.getInstance(instanceName);
            if (logWriter != null && logWriter.isRunning()) logWriter.stop();
        }
        
        NbProcessDescriptor pd = createProcessDescriptor(ip);
        if (pd == null) {
            return null;
        }
        
        String envp[] = createEnvironment(ip);

        try {
            return pd.exec(null, envp, true, null );
        } catch (java.io.IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);

            final String serverLocation = ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
            final String serverRunFileName = serverLocation + (Utilities.isWindows() ? STARTUP_BAT : STARTUP_SH);
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED_PD", serverRunFileName));

            return null;
        }
    }
    
    private InputOutput openConsole() {
        InputOutput io = UISupport.getServerIO(dm.getUrl());
        if (io == null) {
            return null; // finish, it looks like this server instance has been unregistered
        }

        // clear the old output
        try {
            io.getOut().reset();
        } catch (IOException ioe) {
            // no op
        }
        io.select();
        
        return io;
    }            

    private void fireStartProgressEvent(StateType stateType, String msg) {
        startServer.fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.START, stateType, msg));
    }

    private JBLogWriter createLogWriter() {
        InputOutput io = openConsole();
        return JBLogWriter.createInstance(io, instanceName);
    }
    
    private void waitForServerToStart(JBLogWriter logWriter, Process serverProcess) {
        
        fireStartProgressEvent(StateType.RUNNING, createProgressMessage("MSG_START_SERVER_IN_PROGRESS"));

        JBStartServer.ACTION_STATUS status = logWriter.start(serverProcess, startServer);
        
        // reset the need restart flag
        dm.setNeedsRestart(false);
        
        if (status == JBStartServer.ACTION_STATUS.SUCCESS) {
            fireStartProgressEvent(StateType.COMPLETED, createProgressMessage("MSG_SERVER_STARTED"));
        }
        else
        if (status == JBStartServer.ACTION_STATUS.FAILURE) {
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_START_SERVER_FAILED"));
        }
        else 
        if (status == JBStartServer.ACTION_STATUS.UNKNOWN) {
            fireStartProgressEvent(StateType.FAILED, createProgressMessage("MSG_StartServerTimeout"));
        }
        
    }
    
}
    
