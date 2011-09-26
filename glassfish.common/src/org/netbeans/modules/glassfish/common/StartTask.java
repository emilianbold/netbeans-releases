/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.Dialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.glassfish.spi.RegisteredDerbyServer;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.netbeans.modules.glassfish.spi.OperationStateListener;
import org.netbeans.modules.glassfish.spi.Recognizer;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.glassfish.spi.TreeParser;
import org.netbeans.modules.glassfish.spi.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 * @author Ludovic Chamenois
 * @author Peter Williams
 */
public class StartTask extends BasicTask<OperationState> {

    private static final String MAIN_CLASS = "com.sun.enterprise.glassfish.bootstrap.ASMain"; // NOI18N

    private final CommonServerSupport support;
    private List<Recognizer> recognizers;
    private FileObject jdkHome = null;
    private List<String> jvmArgs = null;
    static final private int LOWEST_USER_PORT = org.openide.util.Utilities.isWindows() ? 1 : 1025;

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
            @Override
            public void operationStateChanged(OperationState newState, String message) {
                if (OperationState.COMPLETED.equals(newState)) {
                    // attempt to sync the comet support
                    RequestProcessor.getDefault().post(new EnableComet(support));
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
    @Override
    public OperationState call() {
        // Save the current time so that we can deduct that the startup
        // Failed due to timeout
        Logger.getLogger("glassfish").log(Level.FINEST, "StartTask.call() called on thread \"{0}\"", Thread.currentThread().getName()); // NOI18N
        final long start = System.currentTimeMillis();

        final String adminHost;
        final int adminPort;

        adminHost = ip.get(GlassfishModule.HOSTNAME_ATTR);
        if(adminHost == null || adminHost.length() == 0) {
            return fireOperationStateChanged(OperationState.FAILED,
                    "MSG_START_SERVER_FAILED_NOHOST", instanceName); //NOI18N
        }

        adminPort = Integer.valueOf(ip.get(GlassfishModule.ADMINPORT_ATTR));
        if(adminPort < 0 || adminPort > 65535) {
            return fireOperationStateChanged(OperationState.FAILED,
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName); //NOI18N
        }

        if (support.isRemote()) {
            if (support.isReallyRunning()) {
                if (Util.isDefaultOrServerTarget(ip)) {
                    return restartDAS(adminHost,adminPort,start);
                } else {
                    return startClusterOrInstance(adminHost,adminPort);
                }
            } else {
                return fireOperationStateChanged(OperationState.FAILED,
                        "MSG_START_SERVER_FAILED_DASDOWN", instanceName); //NOI18N
            }
        } else if (!support.isReallyRunning()) {
            return startDASAndClusterOrInstance(adminHost,adminPort);
        } else {
            return startClusterOrInstance(adminHost,adminPort);
        }
    }

    private OperationState restartDAS(String adminHost, int adminPort, final long start) {
                // deal with the remote case here...
                CommandRunner mgr = new CommandRunner(true, support.getCommandFactory(), ip, new OperationStateListener() {
                    // if the http command is successful, we are not done yet...
                    // The server still has to stop. If we signal success to the 'stateListener'
                    // for the task, it may be premature.

                    @Override
                    public void operationStateChanged(OperationState newState, String message) {
                        if (newState == OperationState.RUNNING) {
                            support.setServerState(ServerState.STARTING);
                        }
                        if (newState == OperationState.FAILED) {
                            fireOperationStateChanged(newState, message, instanceName);
                            support.setServerState(ServerState.STOPPED);
                            //support.refresh();
                        } else if (newState == OperationState.COMPLETED) {
                            if (message.matches("[sg]et\\?.*\\=configs\\..*")) {
                                return;
                            }
                            long startTime = System.currentTimeMillis();
                            OperationState state = OperationState.RUNNING;
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                // no op
                            }
                            while (OperationState.RUNNING == state && System.currentTimeMillis() - start < START_TIMEOUT) {
                                // Send the 'completed' event and return when the server is running
                                boolean httpLive = support.isReady(false, 2000, TIMEUNIT); //CommonServerSupport.isRunning(host, port,ip.get(GlassfishModule.DISPLAY_NAME_ATTR));

                                // Sleep for a little so that we do not make our checks too often
                                //
                                // Doing this before we check httpAlive also prevents us from
                                // pinging the server too quickly after the ports go live.
                                //
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    // no op
                                }

                                if (httpLive) {
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        // no op
                                    }
                                    state = OperationState.COMPLETED;
                                }
                            }
                            if (state == OperationState.COMPLETED) { //support.isReady(false, 120, TimeUnit.SECONDS)) {
                                support.setServerState(ServerState.RUNNING);
                            } else {
                                support.setServerState(ServerState.STOPPED);
                            }
                        }
                    }
                });
                int debugPort = -1;
                if (GlassfishModule.DEBUG_MODE.equals(ip.get(GlassfishModule.JVM_MODE))) {
                    try {
                        debugPort = Integer.parseInt(ip.get(GlassfishModule.DEBUG_PORT));
                        if (debugPort < LOWEST_USER_PORT || debugPort > 65535) {
                            support.setEnvironmentProperty(GlassfishModule.DEBUG_PORT, "9009", true);
                            debugPort = 9009;
                            Logger.getLogger("glassfish").log(Level.INFO, "converted debug port to 9009 for {0}", instanceName);
                        }
                    } catch (NumberFormatException nfe) {
                        support.setEnvironmentProperty(GlassfishModule.DEBUG_PORT, "9009", true);
                        support.setEnvironmentProperty(GlassfishModule.USE_SHARED_MEM_ATTR, "false", true);
                        debugPort = 9009;
                        Logger.getLogger("glassfish").log(Level.INFO, "converted debug type to socket and port to 9009 for {0}", instanceName);
                    }
                }
                String restartQ = "";
                if (support.supportsRestartInDebug()) {
                    restartQ = -1 == debugPort ? "debug=false" : "debug=true";
                }
                mgr.restartServer(debugPort,restartQ);
                return fireOperationStateChanged(OperationState.RUNNING,
                        "MSG_START_SERVER_IN_PROGRESS", instanceName); // NOI18N

            }

    private OperationState startDASAndClusterOrInstance(String adminHost, int adminPort) {
        long start = System.currentTimeMillis();
        Process serverProcess;
        try {
            if (null == jdkHome) {
                jdkHome = getJavaPlatformRoot(support);
            }
            // lookup the javadb start service and use it here.
            RegisteredDerbyServer db = Lookup.getDefault().lookup(RegisteredDerbyServer.class);
            if (null != db && "true".equals(ip.get(GlassfishModule.START_DERBY_FLAG))) { // NOI18N
                db.start();
            }
            // this may be an autheticated server... so we will say it is started.
            // other operations will fail if the process on the port is not a
            // GF v3 server.
            if (support.isReady(false, 20000, TIMEUNIT)) {
                OperationState result = OperationState.COMPLETED;
                if (GlassfishModule.PROFILE_MODE.equals(ip.get(GlassfishModule.JVM_MODE))) {
                    result = OperationState.FAILED;
                }
                return fireOperationStateChanged(result,
                        "MSG_START_SERVER_OCCUPIED_PORT", instanceName); //NOI18N
            }
            if (upgradeFailed()) {
                return fireOperationStateChanged(OperationState.FAILED,
                        "MSG_DOMAIN_UPGRADE_FAILED", instanceName); //NOI18N
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
        logger.readInputStreams(recognizers, false, null, serverProcess.getInputStream(), serverProcess.getErrorStream());

        // Waiting for server to start
        while(System.currentTimeMillis() - start < START_TIMEOUT) {
            // Send the 'completed' event and return when the server is running
            boolean httpLive = Utils.isLocalPortOccupied(adminPort);

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
                if (!support.isReady(true,30,TimeUnit.SECONDS)) {
                    state = OperationState.FAILED;
                    messageKey = "MSG_START_SERVER_FAILED"; // NOI18N
                    serverProcess.destroy();
                    logger.stopReaders();
                    return fireOperationStateChanged(state, messageKey, instanceName);
                }
                return startClusterOrInstance(adminHost,adminPort);
            }

            // if we are profiling, we need to lie about the status?
            if (null != jvmArgs) {
                // try to sync the states after the profiler attaches
                RequestProcessor.getDefault().post(new Runnable () {

                    @Override
                    public void run() {
                        while (!support.isReady(false, 2000, TIMEUNIT)) { // !CommonServerSupport.isRunning(support.getHostName(), support.getAdminPortNumber(),                                ip.get(GlassfishModule.DISPLAY_NAME_ATTR))) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException ex) {
                            }
                        }
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
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
        Logger.getLogger("glassfish").log(Level.INFO, "V3 Failed to start, killing process: {0} after {1}", new Object[]{serverProcess, System.currentTimeMillis() - start});
        serverProcess.destroy();
        logger.stopReaders();
        return fireOperationStateChanged(OperationState.FAILED,
                "MSG_START_SERVER_FAILED2", instanceName,adminHost,adminPort+""); // NOI18N
    }

        private OperationState startClusterOrInstance(String adminHost, int adminPort) {
            String target = Util.computeTarget(ip);
            if (Util.isDefaultOrServerTarget(ip)) {
                return fireOperationStateChanged(OperationState.COMPLETED,
                        "MSG_SERVER_STARTED", instanceName); // NOI18N
            } else {
                OperationState retVal = null;
                // try start-cluster
                CommandRunner inner = new CommandRunner(true, 
                        support.getCommandFactory(), ip, new OperationStateListener() {
                    @Override
                    public void operationStateChanged(OperationState newState, String message) {

                    }
                }
                );
                Future<OperationState> result = inner.execute(new Commands.StartCluster(target));
                OperationState state = null;
                try {
                    state = result.get();
                } catch (InterruptedException ie) {
                    Logger.getLogger("glassfish").log(Level.INFO, "start-cluster",ie); // NOI18N
                } catch (ExecutionException ie) {
                    Logger.getLogger("glassfish").log(Level.INFO, "start-cluster",ie); // NOI18N
                }
                if (state == OperationState.FAILED) {
                    // if start-cluster not successful, try start-instance
                    inner =  new CommandRunner(true, support.getCommandFactory(), ip, new OperationStateListener() {
                        @Override
                        public void operationStateChanged(OperationState newState, String message) {

                        }
                    });
                    result = inner.execute(new Commands.StartInstance(target));
                    try {
                        state = result.get();
                    } catch (InterruptedException ie) {
                        Logger.getLogger("glassfish").log(Level.INFO, "start-instance",ie);  // NOI18N
                    } catch (ExecutionException ie) {
                        Logger.getLogger("glassfish").log(Level.INFO, "start-instance",ie);  // NOI18N
                    }
                    if (state == OperationState.FAILED) {
                        // if start instance not suscessful fail
                        return fireOperationStateChanged(OperationState.FAILED,
                                "MSG_START_TARGET_FAILED", instanceName,target); // NOI18N
                    }
                }

                // update http port
                support.updateHttpPort();

                // ping the http port

                return fireOperationStateChanged(OperationState.COMPLETED,
                        "MSG_SERVER_STARTED", instanceName); // NOI18N
            }

        }
    private String[] createEnvironment() {
        List<String> envp = new ArrayList<String>();
        String localJdkHome = getJdkHome();
        if(localJdkHome != null) {
            String javaEnv = "JAVA_HOME=" + localJdkHome; // NOI18N
            envp.add(javaEnv); // NOI18N
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
        Logger logger = Logger.getLogger("glassfish"); // NOI18N
        if(logger.isLoggable(Level.FINE)) {
            String logmsg = "V3 Environment: "; // NOI18N
            for(String var: envp) {
                logmsg += var;
                logmsg += " "; // NOI18N
            }
            logger.log(Level.FINE, logmsg);
        }
        return envp.toArray(new String[envp.size()]);
    }

    private void appendSystemEnvVar(List<String> envp, String key) {
        String value = ip.get(key);
        if(value != null && value.length() > 0) {
            envp.add(key + "=" + value); // NOI18N
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
        if (Utilities.isWindows()) {
            startScript += ".exe"; // NOI18N
        }
        File ss = new File(startScript);
        if (!ss.exists()) {
            throw new ProcessCreationException(null, "MSG_INVALID_JAVA", instanceName, startScript); // NOI18N
        }
        if (support.getInstanceProvider().requiresJdk6OrHigher() && !Util.appearsToBeJdk6OrBetter(ss)) {
            throw new ProcessCreationException(null, "MSG_START_SERVER_FAILED_JDK_ERROR", instanceName); // NOI18N
        }
        String serverHome = ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
        File bootstrapJar = ServerUtilities.getJarName(serverHome, ServerUtilities.GFV3_JAR_MATCHER);
        if(bootstrapJar == null) {
            throw new ProcessCreationException(null, "MSG_START_SERVER_FAILED_FNF"); // NOI18N
        }

        File domainDir = getDomainFolder();
        List<String> optList = new ArrayList<String>(10);
        Map<String, String> argMap = new HashMap<String, String>();
        Map<String, String> propMap = new HashMap<String, String>();
        if (!readJvmArgs(domainDir, optList, argMap, propMap)) {
            throw new ProcessCreationException(null, "MSG_START_SERVER_FAILED_DOMAIN_FNF"); // NOI18N
        }

        if (null != jvmArgs) {
            optList.addAll(jvmArgs);
        }

        StringBuilder argumentBuf = new StringBuilder(1024);
        String classpath = computeClassPath(propMap, domainDir, bootstrapJar);
        if(classpath != null) {
            argumentBuf.append("-cp "); // NOI18N
            argumentBuf.append(classpath);
        }
        appendSystemVars(argMap, argumentBuf);
        appendJavaOpts(optList, argumentBuf);

        if(classpath != null) {
            argumentBuf.append(" ");
            argumentBuf.append(MAIN_CLASS);
        } else {
            argumentBuf.append(" -jar "); // NOI18N
            argumentBuf.append(Util.quote(bootstrapJar.getAbsolutePath()));
        }
        argumentBuf.append(" --domain ").append(getDomainName()); // NOI18N
        argumentBuf.append(" --domaindir ").append(Util.quote(domainDir.getAbsolutePath())); // NOI18N

        String arguments = argumentBuf.toString();
        Logger.getLogger("glassfish").log(Level.FINE, "V3 JVM Command: {0} {1}", new Object[]{startScript, arguments}); // NOI18N
        return new NbProcessDescriptor(startScript, arguments);
    }

    private String computeClassPath(Map<String, String> propMap, File domainDir, File bootstrapJar) {
        String result = null;
        List<File> prefixCP = Util.classPathToFileList(propMap.get("classpath-prefix"), domainDir); // NOI18N
        List<File> suffixCP = Util.classPathToFileList(propMap.get("classpath-suffix"), domainDir); // NOI18N
        boolean useEnvCP = "false".equals(propMap.get("env-classpath-ignored")); // NOI18N
        List<File> envCP = Util.classPathToFileList(useEnvCP ? System.getenv("CLASSPATH") : null, domainDir); // NOI18N
        List<File> systemCP = Util.classPathToFileList(propMap.get("system-classpath"), domainDir); // NOI18N

        if(prefixCP.size() > 0 || suffixCP.size() > 0 || envCP.size() > 0 || systemCP.size() > 0) {
            List<File> mainCP = Util.classPathToFileList(bootstrapJar.getAbsolutePath(), null);

            if(mainCP.size() > 0) {
                List<File> completeCP = new ArrayList<File>(32);
                completeCP.addAll(prefixCP);
                completeCP.addAll(mainCP);
                completeCP.addAll(systemCP);
                completeCP.addAll(envCP);
                completeCP.addAll(suffixCP);

                // Build classpath in proper order - prefix / main / system / environment / suffix
                // Note that completeCP should always have at least 2 elements at
                // this point (1 from mainCP and 1 from some other CP modifier)
                StringBuilder classPath = new StringBuilder(1024);
                Iterator<File> iter = completeCP.iterator();
                classPath.append(Util.quote(iter.next().getPath()));
                while(iter.hasNext()) {
                    classPath.append(File.pathSeparatorChar);
                    classPath.append(Util.quote(iter.next().getPath()));
                }
                result = classPath.toString();
            } else {
                Logger.getLogger("glassfish").log(Level.WARNING, // NOI18N
                        "Unable to read main classpath from glassfish main jar when building launch classpath."); // NOI18N
            }
        }
        return result;
    }

    private StringBuilder appendJavaOpts(List<String> optList, StringBuilder argumentBuf)
            throws ProcessCreationException {
        String debugPortString = ""; // NOI18N
        try {
            for (String option : optList) {
                argumentBuf.append(' ');
                argumentBuf.append(option);
            }

            if (GlassfishModule.DEBUG_MODE.equals(ip.get(GlassfishModule.JVM_MODE))) {
//            javaOpts.append(" -classic -Xdebug -Xnoagent -Djava.compiler=NONE
//                -Xrunjdwp:transport=dt_socket,address="). // NOI18N
                debugPortString = ip.get(GlassfishModule.DEBUG_PORT);
                String debugTransport = "dt_socket"; // NOI18N
                if ("true".equals(ip.get(GlassfishModule.USE_SHARED_MEM_ATTR))) { // NOI18N
                    debugTransport = "dt_shmem";  // NOI18N
                } else {
                    int t = 0;
                    if (null != debugPortString && debugPortString.trim().length() > 0) {
                        t = Integer.parseInt(debugPortString);
                        if(t < LOWEST_USER_PORT || t > 65535) {
                            throw new NumberFormatException();
                        }
                    }
                }
                //try {
                if (null == debugPortString || "".equals(debugPortString)) {
                    if ("true".equals(ip.get(GlassfishModule.USE_SHARED_MEM_ATTR))) { // NOI18N
                        debugPortString = Integer.toString(
                                Math.abs((ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR) +
                                support.getDomainsRoot() +
                                ip.get(GlassfishModule.DOMAIN_NAME_ATTR)).hashCode() + 1));
                    } else {
                        debugPortString = selectDebugPort();
                    }
                    DialogDescriptor note =
                            new DialogDescriptor(
                            NbBundle.getMessage(StartTask.class,"MSG_SELECTED_PORT", debugPortString),
                            NbBundle.getMessage(StartTask.class,"TITLE_MESSAGE"),
                            false,
                            new Object[] { DialogDescriptor.OK_OPTION },
                            DialogDescriptor.OK_OPTION,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            null);
                    Dialog d = DialogDisplayer.getDefault().createDialog(note);
                    d.setVisible(true);
                }
                support.setEnvironmentProperty(GlassfishModule.DEBUG_PORT, debugPortString, true);
                argumentBuf.append(" -Xdebug -Xrunjdwp:transport="); // NOI18N
                argumentBuf.append(debugTransport);
                argumentBuf.append(",address="); // NOI18N
                argumentBuf.append(debugPortString);
                argumentBuf.append(",server=y,suspend=n"); // NOI18N
            }
        } catch (IOException ioe) {
            throw new ProcessCreationException(ioe,
                    "MSG_START_SERVER_FAILED_INVALIDPORT", instanceName, debugPortString); //NOI18N
        }
        return argumentBuf;
    }

    private String selectDebugPort() throws IOException {
        int debugPort = 9009;
        ServerSocket t = null;
        try {
            // try to use the 'standard port'
            t = new ServerSocket(debugPort);
            return Integer.toString(debugPort);
        } catch (IOException ex) {
            // log this... but don't panic
            Logger.getLogger("glassfish").fine("9009 is in use... going random");
        } finally {
            if (null != t) try { t.close(); } catch (IOException ioe) {}
        }
        try {
            // try to find a different port... if this fails,
            //    it is a great time to panic.
            t = new ServerSocket(0);
            debugPort = t.getLocalPort();
            return Integer.toString(debugPort);
        } finally {
            if (null != t) try { t.close(); } catch (IOException ioe) {}
        }
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

        for(Map.Entry<String, String> entry: argMap.entrySet()) {
            appendSystemVar(argumentBuf, entry.getKey(), entry.getValue());
        }

        return argumentBuf;
    }

    private StringBuilder appendSystemVar(StringBuilder argumentBuf, String key, String value) {
        if(value != null && value.length() > 0) {
            argumentBuf.append(" -D"); // NOI18N
            argumentBuf.append(key);
            argumentBuf.append("="); // NOI18N
            argumentBuf.append(Util.quote(value));
        }
        return argumentBuf;
    }

    private Process createProcess() throws ProcessCreationException {
        Process process = null;
        NbProcessDescriptor pd = createProcessDescriptor();
        if(pd != null) {
            try {
                process = pd.exec(null, createEnvironment(), true, getDomainFolder());
            } catch (java.io.IOException ex) {
                throw new ProcessCreationException(ex, "MSG_START_SERVER_FAILED_PD", instanceName); // NOI18N
            }
        }
        return process;
    }

    private File getDomainFolder() {
        return new File(support.getDomainsRoot()+ File.separatorChar + getDomainName());
    }

    private String getDomainName() {
        return ip.get(GlassfishModule.DOMAIN_NAME_ATTR);
    }

    private boolean readJvmArgs(File domainRoot, List<String> optList,
            Map<String, String> argMap, Map<String, String> propMap) {
        Map<String, String> varMap = new HashMap<String, String>();

        varMap.put("com.sun.aas.installRoot", Utils.escapePath(ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR))); // NOI18N
        varMap.put("com.sun.aas.instanceRoot", Utils.escapePath(domainRoot.getAbsolutePath())); // NOI18N
        varMap.put("com.sun.aas.javaRoot", Utils.escapePath(jdkHome.getPath())); // NOI18N
        varMap.put("com.sun.aas.derbyRoot", getJavaDBLocation()); // NOI18N

        File domainXml = new File(domainRoot, "config/domain.xml"); // NOI18N
        if (domainXml.exists()) {
            JvmConfigReader reader = new JvmConfigReader(optList, argMap, varMap, propMap);
            List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
            pathList.add(new TreeParser.Path("/domain/servers/server", reader.getServerFinder())); // NOI18N
            pathList.add(new TreeParser.Path("/domain/configs/config", reader.getConfigFinder())); // NOI18N
            pathList.add(new TreeParser.Path("/domain/configs/config/java-config", reader));  // NOI18N

            // this option does not apply to installs that do not have the btrace-agent.jar
            // so check for that first.
            File irf = new File(ip.get(GlassfishModule.GLASSFISH_FOLDER_ATTR));
            File btrace = new File(irf, "lib/monitor/btrace-agent.jar"); // NOI18N
            if (btrace.exists()) {
                pathList.add(new TreeParser.Path("/domain/configs/config/monitoring-service", reader.getMonitoringFinder(btrace)));  // NOI18N
            }
            try {
                TreeParser.readXml(domainXml, pathList);
                if (Utilities.isMac() &&
                    System.getProperty("java.vm.vendor").equals("Apple Inc.")) {
                    // on Mac OS, unless the property is specified in the domain.xml, we add
                    // the -d32 flag to start the JVM in 32 bits mode
                    if (!optList.contains("-d64") && !optList.contains("-d32")) {
                        optList.add("-d32");
                    }
                }
                return true;
            } catch(IllegalStateException ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
            }
        }
        return false;
    }

    private String getJavaDBLocation() {
        String javadb = ip.get(GlassfishModule.INSTALL_FOLDER_ATTR) + File.separatorChar + "javadb"; // NOI18N
        if (new File(javadb).exists()) {
            // V3 Prelude includes javadb as it can run on JDK 5
            javadb = Utils.escapePath(javadb);
        } else {
            // V3 uses javadb from JDK 6
            javadb = Utils.escapePath(jdkHome.getPath() + File.separatorChar + "javadb"); // NOI18N
        }
        return javadb;
    }

    private boolean upgradeFailed() {
        // get server install version
        File glassfishDir = new File(support.getGlassfishRoot());
        int installVersion = ServerDetails.getVersionFromInstallDirectory(glassfishDir);

        if (installVersion < 0) return false;  // no upgrade attempted, so it DID NOT fail.

        // get domain.xml 'version'
        File domainXmlFile = new File(getDomainFolder(), "config" + File.separator + "domain.xml"); // NOI18N
        int domainVersion = ServerDetails.getVersionFromDomainXml(domainXmlFile);

        if (domainVersion < 0) return false;  // no upgrade attempted, so it DID NOT fail.

        if (domainVersion/10 < installVersion/10) {
            return executeUpgradeProcess() != 0;
        }
        return false;
    }

    private int executeUpgradeProcess() {
        int retVal = -1;
        File asadmin = findFirstExecutableFile(new File(support.getGlassfishRoot()), "asadmin", "bin");
        if (null == asadmin)
            return retVal;
        NbProcessDescriptor upgrader = new NbProcessDescriptor(asadmin.getAbsolutePath(),
                    "start-domain --upgrade --domaindir "+Util.quote(support.getDomainsRoot())+" "+  // NOI18N
                    support.getDomainName());
        try {
            Process p = upgrader.exec();
            p.waitFor();
            retVal = p.exitValue();
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, upgrader.toString(), ex); // NOI18N
        } catch (IOException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, upgrader.toString(), ex); // NOI18N
        }
        return retVal;
    }

    // TODO : refactor and remove 'similar' methods post 7.0
    private File findFirstExecutableFile(File installRoot, String executableName, String... directories) {
        File result = null;
        if (installRoot != null && installRoot.exists()) {
            for (String dir : directories) {
                File updateCenterBin = new File(installRoot, dir); // NOI18N
                if (updateCenterBin.exists()) {
                    if (Utilities.isWindows()) {
                        File launcherPath = new File(updateCenterBin, executableName + ".exe"); // NOI18N
                        if (launcherPath.exists()) {
                            result = launcherPath;
                        } else {
                            launcherPath = new File(updateCenterBin, executableName + ".bat"); // NOI18N
                            result = (launcherPath.exists()) ? launcherPath : null;
                        }
                    } else {
                        File launcherPath = new File(updateCenterBin, executableName); // NOI18N
                        result = (launcherPath.exists()) ? launcherPath : null;
                    }
                    if (null != result) {
                        break;
                    }
                }
            }
        }
        return result;
    }
}
