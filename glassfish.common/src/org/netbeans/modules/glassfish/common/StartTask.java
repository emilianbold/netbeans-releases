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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.GlassfishModule.OperationState;
import org.netbeans.spi.glassfish.OperationStateListener;
import org.netbeans.spi.glassfish.ServerUtilities;
import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;


/**
 * @author Ludovic Chamenois
 * @author Peter Williams
 */
public class StartTask extends BasicTask<OperationState> {
    

    /**
     * 
     * @param dm 
     * @param startServer 
     */
    public StartTask(Map<String, String> properties, OperationStateListener... stateListener) {
        super(properties, stateListener);
    }
    
    /**
     * 
     */
    public OperationState call() {
        // Save the current time so that we can deduct that the startup
        // Failed due to timeout
        Logger.getLogger("glassfish").log(Level.FINEST, 
                "StartTask.call() called on thread \"" + Thread.currentThread().getName() + "\"");
        long start = System.currentTimeMillis();

        String host = null;
        int port = 0;
        
        host = ip.get(GlassfishModule.HOSTNAME_ATTR);
        if(host == null || host.length() == 0) {
            return fireOperationStateChanged(OperationState.FAILED, 
                    "MSG_START_SERVER_FAILED_NOHOST", instanceName); //NOI18N
        }
               
        try {
            port = Integer.valueOf(ip.get(GlassfishModule.HTTPPORT_ATTR));
            if(port < 0 || port > 65535) {
                return fireOperationStateChanged(OperationState.FAILED, 
                        "MSG_START_SERVER_FAILED_BADPORT", instanceName); //NOI18N
            }
        } catch(NumberFormatException ex) {
            return fireOperationStateChanged(OperationState.FAILED, 
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName); //NOI18N
        }
        
        Process serverProcess = createProcess();
        if (serverProcess == null) {
            // failed event already sent...
            return OperationState.FAILED;
        }

        fireOperationStateChanged(OperationState.RUNNING, 
                "MSG_START_SERVER_IN_PROGRESS", instanceName);
        
        // create a logger to the server's output stream so that a user
        // can observe the progress
        LogViewMgr logger = LogViewMgr.getInstance(ip.get(GlassfishModule.URL_ATTR));
        logger.readInputStreams(serverProcess.getInputStream(), serverProcess.getErrorStream());

        // Waiting for server to start
        while(System.currentTimeMillis() - start < TIMEOUT) {
            // Send the 'completed' event and return when the server is running
            if(CommonServerSupport.isRunning(host, port)) {
                // !PW FIXME V3 as of March 12 is starting Grizzly & listening
                // for connections before the server is ready to take asadmin
                // commands.  Until this is fixed, wait 1 second before assuming
                // it's really ok.  Otherwise, domain.xml can get corrupted.
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                }
                return fireOperationStateChanged(OperationState.COMPLETED, 
                        "MSG_SERVER_STARTED", instanceName); // NOI18N
            }
            
            // Sleep for a little so that we do not make our checks too often
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                // no op
            }
        }
        
        // If the server did not start in the designated time limits
        // We consider the startup as failed and warn the user
        serverProcess.destroy();
        return OperationState.FAILED;
    }
    
    private String[] createEnvironment() {
        List<String> envp = new ArrayList<String>();
        String jdkHome = getJdkHome();
        if(jdkHome != null) {
            String javaEnv = "JAVA_HOME=" + jdkHome;
            envp.add(javaEnv); // NOI18N
            Logger.getLogger("glassfish").log(Level.FINE, "V3 Environment: " + javaEnv);
        } else {
            Logger.getLogger("glassfish").log(Level.WARNING, "Unable to set JAVA_HOME for GlassFish V3 enviroment.");
        }
        return (String[]) envp.toArray(new String[envp.size()]);
    }
    
    private String getJdkHome() {
        String result = null;
        String jdkHome = System.getProperty("jdk.home");
        if(jdkHome == null || jdkHome.length() == 0) {
            String javaHome = System.getProperty("java.home");
            if(javaHome.endsWith(File.separatorChar + "jre")) {
                result = javaHome.substring(javaHome.length()-4);
            }
        } else {
            result = jdkHome;
        }
        return result;
    }
    
    private NbProcessDescriptor createProcessDescriptor() {
        String startScript = System.getProperty("java.home") + "/bin/java" ; 
        String serverHome = ip.get(GlassfishModule.HOME_FOLDER_ATTR);
        String jarLocation = serverHome + "/" + ServerUtilities.GFV3_MODULES_DIR_NAME + "/" + ServerUtilities.GFV3_SNAPSHOT_JAR_NAME;
        if(!new File(jarLocation).exists()) {
            // try TP2 jar names
            jarLocation = serverHome + "/" + ServerUtilities.GFV3_MODULES_DIR_NAME + "/" + ServerUtilities.GFV3_TP2_JAR_NAME;
            if(!new File(jarLocation).exists()) {
                fireOperationStateChanged(OperationState.FAILED, "MSG_START_SERVER_FAILED_FNF"); // NOI18N
                return null;
            }
        }
        
        StringBuilder argumentBuf = new StringBuilder(1024);
        appendSystemVars(argumentBuf);
        appendJavaOpts(argumentBuf);
        argumentBuf.append(" -client -jar ");
        argumentBuf.append(quote(jarLocation));
        
        String arguments = argumentBuf.toString();
        Logger.getLogger("glassfish").log(Level.FINE, "V3 JVM Command: " + startScript + arguments);
        return new NbProcessDescriptor(startScript, arguments); // NOI18N
    }
    
    // quote the string if it contains spaces.  Might want to expand to all
    // white space (tabs, localized white space, etc.)
    private String quote(String path) {
        return path.indexOf(' ') == -1 ? path : "\"" + path + "\"";
    }
    
    private StringBuilder appendJavaOpts(StringBuilder argumentBuf) {
        if(GlassfishModule.DEBUG_MODE.equals(ip.get(GlassfishModule.JVM_MODE))) {
//            javaOpts.append(" -classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address="). // NOI18N
            argumentBuf.append(" -Xdebug -Xrunjdwp:transport=dt_socket,address=");
            argumentBuf.append(ip.get(GlassfishModule.DEBUG_PORT));
            argumentBuf.append(",server=y,suspend=n"); // NOI18N
        }
        return argumentBuf;
    }
    
    private StringBuilder appendSystemVars(StringBuilder argumentBuf) {
        String jrubyHome = ip.get(GlassfishModule.JRUBY_HOME);
        if(jrubyHome != null) {
            argumentBuf.append(" -D");
            argumentBuf.append(GlassfishModule.JRUBY_HOME);
            argumentBuf.append("=\"");
            argumentBuf.append(jrubyHome);
            argumentBuf.append("\"");
        }

        String cometEnabled = ip.get(GlassfishModule.COMET_FLAG);
        // !PW FIXME remove when persistence for flags is enabled.
        if(cometEnabled == null) {
            cometEnabled = System.getProperty(GlassfishModule.COMET_FLAG);
        }
        if(cometEnabled != null && cometEnabled.length() > 0) {
            argumentBuf.append(" -D");
            argumentBuf.append(GlassfishModule.COMET_FLAG);
            argumentBuf.append("=");
            argumentBuf.append(cometEnabled);
        }
         
        return argumentBuf;
    }    
    
    private Process createProcess() {
        Process process = null;
        NbProcessDescriptor pd = createProcessDescriptor();
        if(pd != null) {
            try {
                process = pd.exec(null, createEnvironment(), true, new File(
                        ip.get(GlassfishModule.HOME_FOLDER_ATTR)));
            } catch (java.io.IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                fireOperationStateChanged(OperationState.FAILED, 
                        "MSG_START_SERVER_FAILED_PD"); // NOI18N
            }
        }
        return process;
    }
}