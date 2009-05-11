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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.netbeans.modules.glassfish.spi.RegisteredDerbyServer;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.OperationStateListener;
import org.netbeans.modules.glassfish.spi.Recognizer;
import org.netbeans.modules.glassfish.spi.ServerCommand.GetPropertyCommand;
import org.netbeans.modules.glassfish.spi.ServerCommand.SetPropertyCommand;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.glassfish.spi.TreeParser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * @author Ludovic Chamenois
 * @author Peter Williams
 */
public class StartTask extends BasicTask<OperationState> {

    private final CommonServerSupport support;
    private List<Recognizer> recognizers;
    private FileObject jdkHome = null;
    private List<String> jvmArgs = null;

    /**
     * 
     * @param support common support object for the server instance being started
     * @param recognizers output recognizers to pass to log processors, if any
     * @param stateListener state monitor to track start progress
     */
    public StartTask(CommonServerSupport support, List<Recognizer> recognizers,
            OperationStateListener... stateListener) {
        this(support, recognizers, null, null, stateListener);
    }
    
    /**
     *
     * @param support common support object for the server instance being started
     * @param recognizers output recognizers to pass to log processors, if any
     * @param jdkRoot used for starting in profile mode
     * @param jvmArgs used for starting in profile mode
     * @param stateListener state monitor to track start progress
     */
    public StartTask(final CommonServerSupport support, List<Recognizer> recognizers,
            FileObject jdkRoot, String[] jvmArgs, OperationStateListener... stateListener) {
        super(support.getInstanceProperties(), stateListener);
        List<OperationStateListener> listeners = new ArrayList<OperationStateListener>();
        listeners.addAll(Arrays.asList(stateListener));
        listeners.add(new OperationStateListener() {
            public void operationStateChanged(OperationState newState, String message) {
                if (OperationState.COMPLETED.equals(newState)) {
                RequestProcessor.getDefault().post(new Runnable() {

                    public void run() {
                        GetPropertyCommand gpc = new GetPropertyCommand("*.enable-comet-support");
                        Future<OperationState> result = support.execute(gpc);
                                //((GlassfishModule) si.getBasicNode().getLookup().lookup(GlassfishModule.class)).execute(gpc);
                        try {
                            if (result.get(10, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                                Map<String, String> retVal = gpc.getData();
                                String newValue = support.getInstanceProperties().get(GlassfishModule.COMET_FLAG);
                                if (null == newValue || newValue.trim().length() < 1) {
                                    newValue = "false";
                                }
                                for (Entry<String,String> entry : retVal.entrySet()) {
                                    String key = entry.getKey();
                                    // do not update the admin listener....
                                    if (null != key && !key.contains("admin-listener")) {
                                        SetPropertyCommand spc = new SetPropertyCommand(key,newValue);
                                        Future<OperationState> results = support.execute(spc);
                                            //((GlassfishModule) si.getBasicNode().getLookup().lookup(GlassfishModule.class)).execute(gpc);
                                        results.get(10, TimeUnit.SECONDS);
                                    }
                                }
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
                        } catch (ExecutionException ex) {
                            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
                        } catch (TimeoutException ex) {
                            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
                        }
                    }
                });
                    // attempt to sync the comet support
                }
            }

        });
        this.stateListener = listeners.toArray(new OperationStateListener[listeners.size()]);
        this.support = support;
        this.recognizers = recognizers;
        this.jdkHome = jdkRoot;
        this.jvmArgs = (jvmArgs != null) ? Arrays.asList(removeEscapes(jvmArgs)) : null;
    }
    
    private static String [] removeEscapes(String [] args) {
        for(int i = 0; i < args.length; i++) {
            args[i] = args[i].replace("\\\"", ""); // NOI18N
        }
        return args;
    }

    /**
     * 
     */
    public OperationState call() {
        // Save the current time so that we can deduct that the startup
        // Failed due to timeout
        Logger.getLogger("glassfish").log(Level.FINEST, "StartTask.call() called on thread \"" + Thread.currentThread().getName() + "\""); // NOI18N
        long start = System.currentTimeMillis();

        String host;
        int port = 0;
        
        host = ip.get(GlassfishModule.HOSTNAME_ATTR);
        if(host == null || host.length() == 0) {
            return fireOperationStateChanged(OperationState.FAILED, 
                    "MSG_START_SERVER_FAILED_NOHOST", instanceName); //NOI18N
        }
               
        Process serverProcess;
        try {
            port = Integer.valueOf(ip.get(GlassfishModule.HTTPPORT_ATTR));
            if(port < 0 || port > 65535) {
                return fireOperationStateChanged(OperationState.FAILED, 
                        "MSG_START_SERVER_FAILED_BADPORT", instanceName); //NOI18N
            }

            jdkHome = getJavaPlatformRoot(support);
            // lookup the javadb start service and use it here.
            RegisteredDerbyServer db = Lookup.getDefault().lookup(RegisteredDerbyServer.class);
            if (null != db && "true".equals(ip.get(GlassfishModule.START_DERBY_FLAG))) { // NOI18N
                db.start();
            }
            serverProcess = createProcess();
        } catch (NumberFormatException nfe) {
            Logger.getLogger("glassfish").log(Level.INFO, ip.get(GlassfishModule.HTTPPORT_ATTR), nfe); // NOI18N
            return fireOperationStateChanged(OperationState.FAILED,
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName); //NOI18N
        } catch (IOException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
            return fireOperationStateChanged(OperationState.FAILED, "MSG_PASS_THROUGH",
                    ex.getLocalizedMessage());
        } catch (ProcessCreationException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, null, ex); // NOI18N
            return fireOperationStateChanged(OperationState.FAILED, "MSG_PASS_THROUGH",
                    ex.getLocalizedMessage());
        }

        fireOperationStateChanged(OperationState.RUNNING, 
                "MSG_START_SERVER_IN_PROGRESS", instanceName); // NOI18N
        
        // create a logger to the server's output stream so that a user
        // can observe the progress
        LogViewMgr logger = LogViewMgr.getInstance(ip.get(GlassfishModule.URL_ATTR));
        logger.readInputStreams(recognizers, serverProcess.getInputStream(), serverProcess.getErrorStream());

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
                Logger.getLogger("glassfish").log(Level.FINE, "Server HTTP is live."); // NOI18N
                OperationState state = OperationState.COMPLETED;
                String messageKey = "MSG_SERVER_STARTED"; // NOI18N
                if (!support.isReady(true)) {
                    state = OperationState.FAILED;
                    messageKey = "MSG_START_SERVER_FAILED"; // NOI18N
                }
                return fireOperationStateChanged(state, messageKey, instanceName);
            }
            
            // if we are profiling, we need to lie about the status?
            if (null != jvmArgs) {
                // try to sync the states after the profiler attaches
                RequestProcessor.getDefault().post(new Runnable () {

                    public void run() {
                        while (!CommonServerSupport.isRunning(support.getHostName(), support.getHttpPortNumber())) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException ex) {
                                //Exceptions.printStackTrace(ex);
                            }
                        }
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                    support.refresh();
                                                    
                            }

                        });
                    }
                });
                return fireOperationStateChanged(OperationState.COMPLETED,
                        "MSG_SERVER_STARTED", instanceName); // NOI18N
            }
        }
        
        // If the server did not start in the designated time limits
        // We consider the startup as failed and warn the user
        Logger.getLogger("glassfish").log(Level.FINE, "V3 Failed to start, killing process: " + serverProcess); // NOI18N
        serverProcess.destroy();
        return fireOperationStateChanged(OperationState.FAILED,
                "MSG_START_SERVER_FAILED", instanceName); // NOI18N
    }

    private String[] createEnvironment() {
        List<String> envp = new ArrayList<String>();
        String localJdkHome = getJdkHome();
        if(localJdkHome != null) {
            String javaEnv = "JAVA_HOME=" + localJdkHome; // NOI18N
            envp.add(javaEnv); // NOI18N
            Logger.getLogger("glassfish").log(Level.FINE, "V3 Environment: " + javaEnv); // NOI18N
        } else {
            Logger.getLogger("glassfish").log(Level.WARNING, "Unable to set JAVA_HOME for GlassFish V3 enviroment."); // NOI18N
        }
        Locale currentLocale = Locale.getDefault();
        if (currentLocale.equals(new Locale("tr","TR"))) { // NOI18N
            // the server is just plain broken when run in a Turkish locale, so
            // we need to start it in en_US
            envp.add("LANG=en_US");  // NOI18N
            envp.add("LC_ALL=en_US");  // NOI18N
            String message = NbBundle.getMessage(StartTask.class, "MSG_LocaleSwitched");  // NOI18N
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notifyLater(nd);
        }
        appendSystemEnvVar(envp, GlassfishModule.GEM_HOME);
        appendSystemEnvVar(envp, GlassfishModule.GEM_PATH);
        return envp.toArray(new String[envp.size()]);
    }
    
    private void appendSystemEnvVar(List<String> envp, String key) {
        String value = ip.get(key);
        if(value != null && value.length() > 0) {
            envp.add(key + "=" + value);
        }
    }

    private FileObject getJavaPlatformRoot(CommonServerSupport support) throws IOException {
        FileObject retVal;
        String javaInstall = support.getInstanceProperties().get(GlassfishModule.JAVA_PLATFORM_ATTR);
        if (null == javaInstall || javaInstall.trim().length() < 1) {
            File dir = new File(getJdkHome());
            retVal = FileUtil.createFolder(FileUtil.normalizeFile(dir));
        } else {
            File f = new File(javaInstall);
            if (f.exists()) {
                //              bin             home
                File dir = f.getParentFile().getParentFile();
                retVal = FileUtil.createFolder(FileUtil.normalizeFile(dir));
            } else {
                throw new FileNotFoundException(NbBundle.getMessage(StartTask.class, "MSG_INVALID_JAVA", instanceName, javaInstall)); // NOI18N
            }
        }
        return retVal;
    }
    
    private String getJdkHome() {
        String result;
        if (null != jdkHome) {
            result = FileUtil.toFile(jdkHome).getAbsolutePath();
        } else {
            result = System.getProperty("java.home");      // NOI18N
            if (result.endsWith(File.separatorChar + "jre")) {    // NOI18N
                result = result.substring(0,result.length() - 4);
            }
        }
        return result;
    }
    
    private NbProcessDescriptor createProcessDescriptor() throws ProcessCreationException { 
        String startScript = FileUtil.toFile(jdkHome).getAbsolutePath() +
                File.separatorChar + "bin" + File.separatorChar + "java"; // NOI18N
        if (!File.separator.equals("/")) {
            startScript += ".exe"; // NOI18N
        }
        File ss = new File(startScript);
        if (!ss.exists()) {
            throw new ProcessCreationException(null,"MSG_INVALID_JAVA", instanceName, startScript);
        }
        if (support.getInstanceProvider().requiresJdk6OrHigher() && !Util.appearsToBeJdk6OrBetter(ss)) {
            throw new ProcessCreationException(null,"MSG_START_SERVER_FAILED_JDK_ERROR", instanceName);
        }
        String serverHome = ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
        File jar = ServerUtilities.getJarName(serverHome, ServerUtilities.GFV3_JAR_MATCHER);
        if(jar == null) {
            throw new ProcessCreationException(null,"MSG_START_SERVER_FAILED_FNF");
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

        argumentBuf.append(" -client -jar "); // NOI18N
        argumentBuf.append(quote(jarLocation));
        argumentBuf.append(" --domain " + getDomainName()); // NOI18N
        argumentBuf.append(" --domaindir " + quote(getDomainFolder().getAbsolutePath())); // NOI18N
        
        String arguments = argumentBuf.toString();
        Logger.getLogger("glassfish").log(Level.FINE, "V3 JVM Command: " + startScript + arguments); // NOI18N
        return new NbProcessDescriptor(startScript, arguments);
    }
    
    // quote the string if it contains spaces.  Might want to expand to all
    // white space (tabs, localized white space, etc.)
    private static final String quote(String path) {
        return path.indexOf(' ') == -1 ? path : "\"" + path + "\""; // NOI18N
    }
    
    private StringBuilder appendJavaOpts(List<String> optList, StringBuilder argumentBuf) throws ProcessCreationException {
        String debugPortString = "";
        try {
            for (String option : optList) {
                argumentBuf.append(' ');
                argumentBuf.append(option);
            }

            if (GlassfishModule.DEBUG_MODE.equals(ip.get(GlassfishModule.JVM_MODE))) {
//            javaOpts.append(" -classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address="). // NOI18N
                debugPortString = ip.get(GlassfishModule.DEBUG_PORT);
                String debugTransport = "dt_socket"; // NOI18N
                if ("true".equals(ip.get(GlassfishModule.USE_SHARED_MEM_ATTR))) { // NOI18N
                    debugTransport = "dt_shmem";  // NOI18N
                } else {
                    int t = 0;
                    if (null != debugPortString && debugPortString.trim().length() > 0) {
                        t = Integer.parseInt(debugPortString);
                        if(t < 0 || t > 65535) {
                            throw new NumberFormatException();
                        }
                    }
                }
                //try {
                if (null == debugPortString || "".equals(debugPortString)) {
                    if ("true".equals(ip.get(GlassfishModule.USE_SHARED_MEM_ATTR))) { // NOI18N
                        debugPortString = Integer.toString(Math.abs((ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR) +
                                ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR) +
                                ip.get(GlassfishModule.DOMAIN_NAME_ATTR)).hashCode() + 1));
                    } else {
                        int debugPort = 8787;
                        // calculate the port and save it.
                        ServerSocket t = null;
                        try {
                            t = new ServerSocket(0);
                            debugPort = t.getLocalPort();
                            debugPortString = Integer.toString(debugPort);
                        } finally {
                            if (null != t) {
                                t.close();
                            }
                        }
                    }
                } 
                support.setEnvironmentProperty(GlassfishModule.DEBUG_PORT, debugPortString, true);
                argumentBuf.append(" -Xdebug -Xrunjdwp:transport="); // NOI18N
                argumentBuf.append(debugTransport);
                argumentBuf.append(",address="); // NOI18N
                argumentBuf.append(debugPortString);
                argumentBuf.append(",server=y,suspend=n"); // NOI18N
            }
        } catch (NumberFormatException nfe) {
            throw new ProcessCreationException(nfe, "MSG_START_SERVER_FAILED_INVALIDPORT", instanceName, debugPortString); //NOI18N
        } catch (IOException ioe) {
            throw new ProcessCreationException(ioe, "MSG_START_SERVER_FAILED_INVALIDPORT", instanceName, debugPortString); //NOI18N
        }
        return argumentBuf;
    }
    
    private StringBuilder appendSystemVars(Map<String, String> argMap, StringBuilder argumentBuf) {
        appendSystemVar(argumentBuf, GlassfishModule.JRUBY_HOME, ip.get(GlassfishModule.JRUBY_HOME));
        appendSystemVar(argumentBuf, GlassfishModule.COMET_FLAG, ip.get(GlassfishModule.COMET_FLAG));

        // override the values that are found in the domain.xml file.
        // this is totally a copy/paste from StartTomcat...
        if ("true".equals(ip.get(GlassfishModule.USE_IDE_PROXY_FLAG))) { // NOI18N
            final String[] PROXY_PROPS = {
                "http.proxyHost",       // NOI18N
                "http.proxyPort",       // NOI18N
                "http.nonProxyHosts",   // NOI18N
                "https.proxyHost",      // NOI18N
                "https.proxyPort",      // NOI18N
            };
            boolean isWindows = Utilities.isWindows();
            for (String prop : PROXY_PROPS) {
                String value = System.getProperty(prop);
                if (value != null && value.trim().length() > 0) {
                    if (isWindows && "http.nonProxyHosts".equals(prop)) { // NOI18N
                        // enclose in double quotes to escape the pipes separating the hosts on windows
                        value = "\"" + value + "\""; // NOI18N
                    }
                    argMap.put(prop, value);
                }
            }
        }

        argMap.remove(GlassfishModule.JRUBY_HOME);
        argMap.remove(GlassfishModule.COMET_FLAG);
        
        if(!"false".equals(System.getProperty("glassfish.use.jvm.config"))) { // NOI18N
            for(Map.Entry<String, String> entry: argMap.entrySet()) {
                appendSystemVar(argumentBuf, entry.getKey(), entry.getValue());
            }
        }
        
        return argumentBuf;
    }    
    
    private StringBuilder appendSystemVar(StringBuilder argumentBuf, String key, String value) {
        if(value != null && value.length() > 0) {
            argumentBuf.append(" -D"); // NOI18N
            argumentBuf.append(key);
            argumentBuf.append("="); // NOI18N
            argumentBuf.append(quote(value));
        }
        return argumentBuf;
    }
    
    private Process createProcess() throws ProcessCreationException {
        Process process = null;
        NbProcessDescriptor pd = createProcessDescriptor();
        if(pd != null) {
            try {
                process = pd.exec(null, createEnvironment(), true, new File(
                        ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR)));
            } catch (java.io.IOException ex) {
                throw new ProcessCreationException(ex, "MSG_START_SERVER_FAILED_PD", instanceName);
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

        varMap.put("com.sun.aas.installRoot", fixPath(ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR))); // NOI18N
        varMap.put("com.sun.aas.instanceRoot", fixPath(domainRoot.getAbsolutePath())); // NOI18N
        varMap.put("com.sun.aas.javaRoot", fixPath(jdkHome.getPath())); // NOI18N
        // account for changes of "source" for java db.
        File javadb = new File(ip.get(GlassfishModule.INSTALL_FOLDER_ATTR) + File.separatorChar + "javadb"); // NOI18N
        if (javadb.exists()) {
            // a v3 Prelude install
            varMap.put("com.sun.aas.derbyRoot", fixPath(ip.get(GlassfishModule.INSTALL_FOLDER_ATTR) + File.separatorChar + "javadb")); // NOI18N
        } else {
            // a v3 install
            varMap.put("com.sun.aas.derbyRoot", fixPath(jdkHome.getPath() + File.separatorChar + "javadb")); // NOI18N
        }
        
        File domainXml = new File(domainRoot, "config/domain.xml"); // NOI18N

        JvmConfigReader reader = new JvmConfigReader(optList, argMap, varMap);
        List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
        pathList.add(new TreeParser.Path("/domain/servers/server", reader.getServerFinder())); // NOI18N
        pathList.add(new TreeParser.Path("/domain/configs/config", reader.getConfigFinder())); // NOI18N
        pathList.add(new TreeParser.Path("/domain/configs/config/java-config", reader));  // NOI18N
        try {
            TreeParser.readXml(domainXml, pathList);
        } catch(IllegalStateException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex); // NOI18N
        }
    }

    private static final String fixPath(String path) {
        return path.replace("\\", "\\\\").replace("$", "\\$"); // NOI18N
    }

    private static class ProcessCreationException extends Exception {
        private final String messageName;
        private final String[] args;
        ProcessCreationException(Exception cause, String messageName, String... args) {
            super();
            if (null != cause) {
                initCause(cause);
            }
            this.messageName = messageName;
            this.args = args;
        }

        @Override
        public String getLocalizedMessage() {
            return NbBundle.getMessage(StartTask.class, messageName, args);
        }
    }
    
    private static class JvmConfigReader extends TreeParser.NodeReader {

        private final Map<String, String> argMap;
        private final Map<String, String> varMap;
        private final List<String> optList;
        static private final String SERVER_NAME = "server"; // NOI18N
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
                        if(SERVER_NAME.equals(attributes.getValue("name"))) {        // NOI18N
                            serverConfigName = attributes.getValue("config-ref");   // NOI18N
                            Logger.getLogger("glassfish").finer("DOMAIN.XML: Server profile defined by " + serverConfigName); // NOI18N
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
                    if(serverConfigName != null && serverConfigName.equals(attributes.getValue("name"))) { // NOI18N
                        readJvmConfig = true;
                        Logger.getLogger("glassfish").finer("DOMAIN.XML: Reading JVM options from server profile " + serverConfigName); // NOI18N
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
                if(option.startsWith("-D")) { // NOI18N
                    int splitIndex = option.indexOf('=');
                    if(splitIndex != -1) {
                        String name = option.substring(2, splitIndex);
                        String value = doSub(option.substring(splitIndex+1));
                        if(name.length() > 0) {
                            Logger.getLogger("glassfish").finer("DOMAIN.XML: argument name = " + name + ", value = " + value); // NOI18N
                            argMap.put(name, value);
                        }
                    }
                } else if(option.startsWith("-X")) { // NOI18N
                    option = doSub(option);
                    int splitIndex = option.indexOf('=');
                    if(splitIndex != -1) {
                        String name = option.substring(0, splitIndex);
                        String value = option.substring(splitIndex+1);
                        Logger.getLogger("glassfish").finer("DOMAIN.XML: jvm option: " + name + " = " + value); // NOI18N
                        optList.add(name + '=' + quote(value));
                    } else {
                        Logger.getLogger("glassfish").finer("DOMAIN.XML: jvm option: " + option); // NOI18N
                        optList.add(option);
                    }
                }
            }
        }
        
        private Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}"); // NOI18N
        
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
                                replacement = "\\$\\{" + key + "\\}"; // NOI18N
                            }
                        }
                        matcher.appendReplacement(sb, replacement);
                        result = matcher.find();
                    } while(result);
                    matcher.appendTail(sb);
                    value = sb.toString();
                }
            } catch(Exception ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
            }
            return value;
        }

    }
}