// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
// </editor-fold>

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.OperationStateListener;
import org.netbeans.modules.glassfish.spi.Recognizer;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.glassfish.spi.TreeParser;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * @author Ludovic Chamenois
 * @author Peter Williams
 */
public class StartTask extends BasicTask<OperationState> {

    private List<Recognizer> recognizers;
    private FileObject jdkHome = null;
    private List<String> jvmArgs = null;

    /**
     * 
     * @param dm 
     * @param startServer 
     */
    public StartTask(Map<String, String> properties, List<Recognizer> recognizers,
            OperationStateListener... stateListener) {
        this(properties, recognizers, null, null, stateListener);
    }
    
    /**
     * 
     */
    public StartTask(Map<String, String> properties, List<Recognizer> recognizers,
            FileObject jdkRoot, String[] jvmArgs, OperationStateListener... stateListener) {
        super(properties, stateListener);
        this.recognizers = recognizers;
        this.jdkHome = jdkRoot;
        this.jvmArgs = (jvmArgs != null) ? Arrays.asList(jvmArgs) : null;
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
        
        Process serverProcess = null;
        try {
            serverProcess = createProcess();
        } catch (IOException ex) {
            fireOperationStateChanged(OperationState.FAILED, 
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName); //NOI18N
        }
        if (serverProcess == null) {
            // failed event already sent...
            return OperationState.FAILED;
        }

        fireOperationStateChanged(OperationState.RUNNING, 
                "MSG_START_SERVER_IN_PROGRESS", instanceName);
        
        // create a logger to the server's output stream so that a user
        // can observe the progress
        LogViewMgr logger = LogViewMgr.getInstance(ip.get(GlassfishModule.URL_ATTR));
        logger.readInputStreams(recognizers, serverProcess.getInputStream(), serverProcess.getErrorStream());

        GlassfishInstance gi = new GlassfishInstance(ip);

        // Waiting for server to start
        while(System.currentTimeMillis() - start < START_TIMEOUT) {
            // Send the 'completed' event and return when the server is running
            boolean httpLive = CommonServerSupport.isRunning(host, port);

            // Sleep for a little so that we do not make our checks too often
            //
            // Doing this before we check httpAlive also prevents us from
            // pinging the server too quickly after the ports go live.
            //
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                // no op
            }

            if(httpLive) {
                Logger.getLogger("glassfish").log(Level.FINE, "Server HTTP is live.");
                OperationState state = OperationState.COMPLETED;
                String messageKey = "MSG_SERVER_STARTED";
                if (!gi.getCommonSupport().isReady(true)) {
                    state = OperationState.FAILED;
                    messageKey = "MSG_START_SERVER_FAILED";
                }
                return fireOperationStateChanged(state,
                        messageKey, instanceName); // NOI18N
            }
            
            // if we are profiling, we need to lie about the status?
            if (null != jvmArgs) {
                return fireOperationStateChanged(OperationState.COMPLETED, 
                        "MSG_SERVER_STARTED", instanceName); // NOI18N
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
        String localJdkHome = getJdkHome();
        if(localJdkHome != null) {
            String javaEnv = "JAVA_HOME=" + localJdkHome;
            envp.add(javaEnv); // NOI18N
            Logger.getLogger("glassfish").log(Level.FINE, "V3 Environment: " + javaEnv);
        } else {
            Logger.getLogger("glassfish").log(Level.WARNING, "Unable to set JAVA_HOME for GlassFish V3 enviroment.");
        }
        Locale currentLocale = Locale.getDefault();
        if (currentLocale.equals(new Locale("tr","TR"))) {
            // the server is just plain broken when run in a Turkish locale, so
            // we need to start it in en_US
            envp.add("LANG=en_US");  // NOI18N
            envp.add("LC_ALL=en_US");  // NOI18N
            String message = NbBundle.getMessage(StartTask.class, "MSG_LocaleSwitched");  // NOI18N
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notifyLater(nd);
        }
        return (String[]) envp.toArray(new String[envp.size()]);
    }
    
    private String getJdkHome() {
        String result = null;
        if (null != jdkHome) {
            result = FileUtil.toFile(jdkHome).getAbsolutePath();
        } else {
            String localJdkHome = System.getProperty("jdk.home");       // NOI18N
            if (localJdkHome == null || localJdkHome.length() == 0) {
                String javaHome = System.getProperty("java.home");      // NOI18N
                if (javaHome.endsWith(File.separatorChar + "jre")) {    // NOI18N
                    result = javaHome.substring(javaHome.length() - 4);
                }
            } else {
                result = localJdkHome;
            }
        }
        return result;
    }
    
    private NbProcessDescriptor createProcessDescriptor() throws IOException {
        String startScript;
        if (null == jdkHome) {
           startScript = System.getProperty("java.home") +        
                File.separatorChar + "bin" + File.separatorChar + "java";
        } else {
            startScript = FileUtil.toFile(jdkHome).getAbsolutePath() +
                File.separatorChar + "bin" + File.separatorChar + "java";
        }
        String serverHome = ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
        File jar = ServerUtilities.getJarName(serverHome, ServerUtilities.GFV3_JAR_MATCHER);
        if(jar == null) {
            fireOperationStateChanged(OperationState.FAILED, "MSG_START_SERVER_FAILED_FNF"); // NOI18N
            return null;
        }
        String jarLocation = jar.getAbsolutePath();
        
        List<String> optList = new ArrayList<String>(10);
        Map<String, String> argMap = new HashMap<String, String>();
        readJvmArgs(getDomainFolder(), optList, argMap);
        
        if (null != jvmArgs) {
            optList.addAll(jvmArgs);
        }
        
        StringBuilder argumentBuf = new StringBuilder(1024);
        appendSystemVars(argMap, argumentBuf);
        appendJavaOpts(optList, argumentBuf);

        argumentBuf.append(" -client -jar ");
        argumentBuf.append(quote(jarLocation));
        argumentBuf.append(" --domain " + getDomainName());
        argumentBuf.append(" --domaindir " + quote(getDomainFolder().getAbsolutePath()));
        
        String arguments = argumentBuf.toString();
        Logger.getLogger("glassfish").log(Level.FINE, "V3 JVM Command: " + startScript + arguments);
        return new NbProcessDescriptor(startScript, arguments); // NOI18N
    }
    
    // quote the string if it contains spaces.  Might want to expand to all
    // white space (tabs, localized white space, etc.)
    private static final String quote(String path) {
        return path.indexOf(' ') == -1 ? path : "\"" + path + "\"";
    }
    
    private StringBuilder appendJavaOpts(List<String> optList, StringBuilder argumentBuf) throws IOException {
        for(String option: optList) {
            argumentBuf.append(' ');
            argumentBuf.append(option);
        }

        if(GlassfishModule.DEBUG_MODE.equals(ip.get(GlassfishModule.JVM_MODE))) {
//            javaOpts.append(" -classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address="). // NOI18N
            try {
                int debugPort = 8787;
                // calculate the port and save it.
                ServerSocket t = new ServerSocket(0);
                debugPort = t.getLocalPort();
                String debugPortString = Integer.toString(debugPort);
                ip.put(GlassfishModule.DEBUG_PORT, debugPortString);
                argumentBuf.append(" -Xdebug -Xrunjdwp:transport=dt_socket,address="); // NOI18N
                argumentBuf.append(debugPortString);
                argumentBuf.append(",server=y,suspend=n"); // NOI18N
                t.close();
            } catch (IOException ioe) {
                Logger.getLogger("glassfish").log(Level.FINE, "Could not get a socket for debugging",ioe);
                fireOperationStateChanged(OperationState.FAILED,
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName); //NOI18N                throw ioe;
            }
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
    
    private Process createProcess() throws IOException {
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
    
    private File getDomainFolder() {
        return new File(ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR) + 
                File.separatorChar + getDomainName());
    }
    
    private final String getDomainName() {
        return ip.get(GlassfishModule.DOMAIN_NAME_ATTR);
    }
    
    private void readJvmArgs(File domainRoot, List<String> optList, Map<String, String> argMap) {
        Map<String, String> varMap = new HashMap<String, String>();

        varMap.put("com.sun.aas.installRoot", fixPath(ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR)));
        varMap.put("com.sun.aas.instanceRoot", fixPath(domainRoot.getAbsolutePath()));
        varMap.put("com.sun.aas.javaRoot", fixPath(System.getProperty("java.home")));
        varMap.put("com.sun.aas.derbyRoot", 
                fixPath(ip.get(GlassfishModule.INSTALL_FOLDER_ATTR) + File.separatorChar + "javadb"));
        
        File domainXml = new File(domainRoot, "config/domain.xml");

        JvmConfigReader reader = new JvmConfigReader(optList, argMap, varMap);
        List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
        pathList.add(new TreeParser.Path("/domain/servers/server", reader.getServerFinder()));
        pathList.add(new TreeParser.Path("/domain/configs/config", reader.getConfigFinder()));
        pathList.add(new TreeParser.Path("/domain/configs/config/java-config", reader));
        try {
            TreeParser.readXml(domainXml, pathList);
        } catch(IllegalStateException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
    }

    private static final String fixPath(String path) {
        return path.replace("\\", "\\\\").replace("$", "\\$");
    }
    
    private static class JvmConfigReader extends TreeParser.NodeReader {

        private final Map<String, String> argMap;
        private final Map<String, String> varMap;
        private final List<String> optList;
        private final String serverName = "server";
        private String serverConfigName;
        private boolean readJvmConfig = false;
        
        public JvmConfigReader(List<String> optList, Map<String, String> argMap, Map<String, String> varMap) {
            this.optList = optList;
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
                } else if(option.startsWith("-X")) {
                    option = doSub(option);
                    int splitIndex = option.indexOf('=');
                    if(splitIndex != -1) {
                        String name = option.substring(0, splitIndex);
                        String value = option.substring(splitIndex+1);
                        Logger.getLogger("glassfish").finer("DOMAIN.XML: jvm option: " + name + " = " + value);
                        optList.add(name + '=' + quote(value));
                    } else {
                        Logger.getLogger("glassfish").finer("DOMAIN.XML: jvm option: " + option);
                        optList.add(option);
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
                    StringBuffer sb = new StringBuffer(value.length()*2);
                    do {
                        String key = matcher.group(1);
                        String replacement = varMap.get(key);
                        if(replacement == null) {
                            replacement = System.getProperty(key);
                            if(replacement != null) {
                                replacement = fixPath(replacement);
                            } else {
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