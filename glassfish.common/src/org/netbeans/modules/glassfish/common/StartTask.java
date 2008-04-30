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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.GlassfishModule.OperationState;
import org.netbeans.spi.glassfish.OperationStateListener;
import org.netbeans.spi.glassfish.ServerUtilities;
import org.netbeans.spi.glassfish.TreeParser;
import org.openide.ErrorManager;
import org.openide.execution.NbProcessDescriptor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


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
        Logger.getLogger("glassfish").log(Level.FINE, 
                "V3 Failed to start, killing process: " + serverProcess);
        serverProcess.destroy();
        fireOperationStateChanged(OperationState.FAILED, 
                "MSG_START_SERVER_FAILED", instanceName);
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
                result = javaHome.substring(javaHome.length() - 4);
            }
        } else {
            result = jdkHome;
        }
        return result;
    }
    
    private NbProcessDescriptor createProcessDescriptor() {
        String startScript = System.getProperty("java.home") + 
                File.separatorChar + "bin" + File.separatorChar + "java";
        String serverHome = ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
        File jar = ServerUtilities.getJarName(serverHome, ServerUtilities.GFV3_PREFIX_JAR_NAME);
        if(jar == null) {
            fireOperationStateChanged(OperationState.FAILED, "MSG_START_SERVER_FAILED_FNF"); // NOI18N
            return null;
        }
        String jarLocation = jar.getAbsolutePath();
        
        Map<String, String> argMap = readJvmArgs(getDomain(serverHome));
        
        StringBuilder argumentBuf = new StringBuilder(1024);
        appendSystemVars(argMap, argumentBuf);
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
    
    private StringBuilder appendSystemVars(Map<String, String> argMap, StringBuilder argumentBuf) {
        appendSystemVar(argumentBuf, GlassfishModule.JRUBY_HOME, ip.get(GlassfishModule.JRUBY_HOME));
        appendSystemVar(argumentBuf, GlassfishModule.COMET_FLAG, ip.get(GlassfishModule.COMET_FLAG));

        argMap.remove(GlassfishModule.JRUBY_HOME);
        argMap.remove(GlassfishModule.COMET_FLAG);
        
        if(!"false".equals(System.getProperty("glassfish.use.jvm.config"))) {
            for(Map.Entry<String, String> entry: argMap.entrySet()) {
                appendSystemVar(argumentBuf, entry.getKey(), entry.getValue());
            }
        }
        
        return argumentBuf;
    }    
    
    private StringBuilder appendSystemVar(StringBuilder argumentBuf, String key, String value) {
        if(value != null && value.length() > 0) {
            argumentBuf.append(" -D");
            argumentBuf.append(key);
            argumentBuf.append("=");
            argumentBuf.append(quote(value));
        }
        return argumentBuf;
    }
    
    private Process createProcess() {
        Process process = null;
        NbProcessDescriptor pd = createProcessDescriptor();
        if(pd != null) {
            try {
                process = pd.exec(null, createEnvironment(), true, new File(
                        ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR)));
            } catch (java.io.IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                fireOperationStateChanged(OperationState.FAILED, 
                        "MSG_START_SERVER_FAILED_PD"); // NOI18N
            }
        }
        return process;
    }
    
    private File getDomain(String serverHome) {
        // !PW FIXME default domain hardcoded.
        return new File(serverHome, "domains" + File.separatorChar + "domain1");
    }
    
    private Map<String, String> readJvmArgs(File domainRoot) {
        Map<String, String> argMap = new LinkedHashMap<String, String>();
        Map<String, String> varMap = new HashMap<String, String>();
        
        varMap.put("com.sun.aas.installRoot", ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR));
        varMap.put("com.sun.aas.instanceRoot", domainRoot.getAbsolutePath());
        varMap.put("com.sun.aas.javaRoot", System.getProperty("java.home"));
        varMap.put("com.sun.aas.derbyRoot", 
                ip.get(GlassfishModule.INSTALL_FOLDER_ATTR) + File.separatorChar + "javadb");
        
        File domainXml = new File(domainRoot, "config/domain.xml");

        JvmConfigReader reader = new JvmConfigReader(argMap, varMap);
        List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
        pathList.add(new TreeParser.Path("/domain/servers/server", reader.getServerFinder()));
        pathList.add(new TreeParser.Path("/domain/configs/config", reader.getConfigFinder()));
        pathList.add(new TreeParser.Path("/domain/configs/config/java-config", reader));
        try {
            TreeParser.readXml(domainXml, pathList);
        } catch(IllegalStateException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return argMap;
    }
    
    private static class JvmConfigReader extends TreeParser.NodeReader {

        private final Map<String, String> argMap;
        private final Map<String, String> varMap;
        private final String serverName = "server";
        private String serverConfigName;
        private boolean readJvmConfig = false;
        
        public JvmConfigReader(Map<String, String> argMap, Map<String, String> varMap) {
            this.argMap = argMap;
            this.varMap = varMap;
        }
        
        public TreeParser.NodeReader getServerFinder() {
            return new TreeParser.NodeReader() {
                @Override
                public void readAttributes(String qname, Attributes attributes) throws SAXException {
//                    <server lb-weight="100" name="server" config-ref="server-config">
                    if(serverConfigName == null || serverConfigName.length() == 0) {
                        if(serverName.equals(attributes.getValue("name"))) {
                            serverConfigName = attributes.getValue("config-ref");
                            Logger.getLogger("glassfish").finer("DOMAIN.XML: Server profile defined by " + serverConfigName);
                        }
                    }
                }
            };
        }
        
        public TreeParser.NodeReader getConfigFinder() {
            return new TreeParser.NodeReader() {
                @Override
                public void readAttributes(String qname, Attributes attributes) throws SAXException {
//                    <config name="server-config" dynamic-reconfiguration-enabled="true">
                    if(serverConfigName != null && serverConfigName.equals(attributes.getValue("name"))) {
                        readJvmConfig = true;
                        Logger.getLogger("glassfish").finer("DOMAIN.XML: Reading JVM options from server profile " + serverConfigName);
                    }
                }
                @Override
                public void endNode(String qname) throws SAXException {
                    readJvmConfig = false;
                }
            };
        }
        
        @Override
        public void readCData(String qname, char [] ch, int start, int length) throws SAXException {
//            <jvm-options>-client</jvm-options>
//            <jvm-options>-Djava.endorsed.dirs=${com.sun.aas.installRoot}/lib/endorsed</jvm-options>
            if(readJvmConfig) {
                String option = new String(ch, start, length);
                if(option.startsWith("-D")) {
                    int splitIndex = option.indexOf('=');
                    if(splitIndex != -1) {
                        String name = option.substring(2, splitIndex);
                        String value = doSub(option.substring(splitIndex+1));
                        if(name.length() > 0) {
                            Logger.getLogger("glassfish").finer("DOMAIN.XML: argument name = " + name + ", value = " + value);
                            argMap.put(name, value);
                        }
                    }
                }
            }
        }
        
        private Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        
        private String doSub(String value) {
            try {
                Matcher matcher = pattern.matcher(value);
                boolean result = matcher.find();
                if(result) {
                    StringBuffer sb = new StringBuffer();
                    do {
                        String key = matcher.group(1);
                        String replacement = varMap.get(key);
                        if(replacement == null) {
                            replacement = System.getProperty(key);
                            if(replacement == null) {
                                replacement = "\\$\\{" + key + "\\}";
                            }
                        }
                        matcher.appendReplacement(sb, replacement);
                        result = matcher.find();
                    } while(result);
                    matcher.appendTail(sb);
                    value = sb.toString();
                }
            } catch(Exception ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
            return value;
        }

    }
}