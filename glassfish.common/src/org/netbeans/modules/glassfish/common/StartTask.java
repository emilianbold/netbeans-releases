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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.glassfish.tools.ide.admin.ResultProcess;
import org.glassfish.tools.ide.admin.TaskState;
import org.glassfish.tools.ide.data.StartupArgs;
import org.glassfish.tools.ide.data.StartupArgsEntity;
import org.glassfish.tools.ide.server.FetchLogSimple;
import org.glassfish.tools.ide.server.ServerTasks;
import org.glassfish.tools.ide.utils.ServerUtils;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.netbeans.modules.glassfish.spi.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

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
    private final VMIntrospector vmi;
    private static RequestProcessor NODE_REFRESHER = new RequestProcessor("nodes to refresh");

    /**
     *
     * @param support common support object for the server instance being
     * started
     * @param recognizers output recognizers to pass to log processors, if any
     * @param stateListener state monitor to track start progress
     */
    public StartTask(CommonServerSupport support, List<Recognizer> recognizers,
            VMIntrospector vmi,
            OperationStateListener... stateListener) {
        this(support, recognizers, vmi, null, null, stateListener);
    }

    /**
     *
     * @param support common support object for the server instance being
     * started
     * @param recognizers output recognizers to pass to log processors, if any
     * @param jdkRoot used for starting in profile mode
     * @param jvmArgs used for starting in profile mode
     * @param stateListener state monitor to track start progress
     */
    public StartTask(final CommonServerSupport support, List<Recognizer> recognizers,
            VMIntrospector vmi,
            FileObject jdkRoot, String[] jvmArgs, OperationStateListener... stateListener) {
        super(support.getInstance(), stateListener);
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
        this.vmi = vmi;
        Logger.getLogger("glassfish").log(Level.FINE, "VMI == {0}", vmi);
    }

    private static String[] removeEscapes(String[] args) {
        for (int i = 0; i < args.length; i++) {
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

        adminHost = instance.getProperty(GlassfishModule.HOSTNAME_ATTR);
        if (adminHost == null || adminHost.length() == 0) {
            return fireOperationStateChanged(OperationState.FAILED,
                    "MSG_START_SERVER_FAILED_NOHOST", instanceName); //NOI18N
        }

        adminPort = Integer.valueOf(instance.getProperty(
                GlassfishModule.ADMINPORT_ATTR));
        if (adminPort < 0 || adminPort > 65535) {
            return fireOperationStateChanged(OperationState.FAILED,
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName); //NOI18N
        }

        if (support.isRemote()) {
            if (support.isReallyRunning()) {
                if (Util.isDefaultOrServerTarget(instance.getProperties())) {
                    return restartDAS(adminHost, adminPort, start);
                } else {
                    return startClusterOrInstance(adminHost, adminPort);
                }
            } else {
                return fireOperationStateChanged(OperationState.FAILED,
                        "MSG_START_SERVER_FAILED_DASDOWN", instanceName); //NOI18N
            }
        } else if (!support.isReallyRunning()) {
            return startDASAndClusterOrInstance(adminHost, adminPort);
        } else {
            return startClusterOrInstance(adminHost, adminPort);
        }
    }

    private OperationState restartDAS(String adminHost, int adminPort, final long start) {
        // deal with the remote case here...
        CommandRunner mgr = new CommandRunner(true,
                support.getCommandFactory(),
                instance,
                new OperationStateListener() {
                    // if the http command is successful, we are not done yet...
                    // The server still has to stop. If we signal success to the 'stateListener'
                    // for the task, it may be premature.
                    @SuppressWarnings("SleepWhileInLoop")
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
                                boolean httpLive = support.isReady(false, 2000, TIMEUNIT); //CommonServerSupport.isRunning(host, port,instance.getProperty(GlassfishModule.DISPLAY_NAME_ATTR));

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
        if (GlassfishModule.DEBUG_MODE.equals(instance.getProperty(GlassfishModule.JVM_MODE))) {
            try {
                debugPort = Integer.parseInt(instance.getProperty(GlassfishModule.DEBUG_PORT));
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
        mgr.restartServer(debugPort, restartQ);
        return fireOperationStateChanged(OperationState.RUNNING,
                "MSG_START_SERVER_IN_PROGRESS", instanceName); // NOI18N

    }

    @SuppressWarnings("SleepWhileInLoop")
    private OperationState startDASAndClusterOrInstance(String adminHost, int adminPort) {
        long start = System.currentTimeMillis();
        Process serverProcess;
        try {
            if (null == jdkHome) {
                jdkHome = getJavaPlatformRoot(support);
            }
            // lookup the javadb start service and use it here.
            RegisteredDerbyServer db = Lookup.getDefault().lookup(RegisteredDerbyServer.class);
            if (null != db && "true".equals(instance.getProperty(GlassfishModule.START_DERBY_FLAG))) { // NOI18N
                db.start();
            }
            int testPort = 0;
            String portCandidate = support.getAdminPort();
            try {
                testPort = Integer.parseInt(portCandidate);
            } catch (NumberFormatException nfe) {
                Logger.getLogger("glassfish").log(Level.INFO,
                        "could not parse {0} as an Inetger", portCandidate); // NOI18N
            }
            // this may be an autheticated server... so we will say it is started.
            // other operations will fail if the process on the port is not a
            // GF v3 server.
            Logger.getLogger("glassfish").log(Level.FINEST,
                    "Checking if GlassFish {0} is running. Timeout set to 20000 ms",
                    instance.getName());
            if (support.isReady(false, 20000, TIMEUNIT)) {
                OperationState result = OperationState.COMPLETED;
                if (GlassfishModule.PROFILE_MODE.equals(instance.getProperty(GlassfishModule.JVM_MODE))) {
                    result = OperationState.FAILED;
                }
                return fireOperationStateChanged(result,
                        "MSG_START_SERVER_OCCUPIED_PORT", instanceName); //NOI18N
            } else if (testPort != 0 && Utils.isLocalPortOccupied(testPort)) {
                return fireOperationStateChanged(OperationState.FAILED,
                        "MSG_START_SERVER_OCCUPIED_PORT", instanceName); //NOI18N
            }
            if (upgradeFailed()) {
                return fireOperationStateChanged(OperationState.FAILED,
                        "MSG_DOMAIN_UPGRADE_FAILED", instanceName); //NOI18N
            }
            serverProcess = createProcess();
        } catch (NumberFormatException nfe) {
            Logger.getLogger("glassfish").log(Level.INFO, instance.getProperty(GlassfishModule.HTTPPORT_ATTR), nfe); // NOI18N
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
        LogViewMgr logger = LogViewMgr.getInstance(instance.getProperty(
                GlassfishModule.URL_ATTR));
        String debugPort = instance.getProperty(GlassfishModule.DEBUG_PORT);
        logger.readInputStreams(recognizers, false, null,
                new FetchLogSimple(serverProcess.getInputStream()),
                new FetchLogSimple(serverProcess.getErrorStream()));

        // Waiting for server to start
        Logger.getLogger("glassfish").log(Level.FINER, "Waiting for server to start for {0} ms",
                new Object[] {Integer.toString(START_TIMEOUT)});
        while (System.currentTimeMillis() - start < START_TIMEOUT) {
            // Send the 'completed' event and return when the server is running
            boolean httpLive = CommonServerSupport.isRunning("localhost", adminPort, "localhost"); // Utils.isLocalPortOccupied(adminPort);
            Logger.getLogger("glassfish").log(Level.FINEST, "{0} DAS port {1} {2} alive",
                    new Object[] {instance.getName(), Integer.toString(adminPort), httpLive ? "is" : "is not"}); 
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

            if (httpLive) {
                if (!support.isReady(true, 3, TimeUnit.HOURS)) {
                    OperationState  state = OperationState.FAILED;
                    String messageKey = "MSG_START_SERVER_FAILED"; // NOI18N
                    Logger.getLogger("glassfish").log(Level.INFO,
                            "{0} is not responding, killing the process.",
                            new Object[] {instance.getName()});
                    LogViewMgr.removeServerLogStream(instance);
                    serverProcess.destroy();
                    logger.stopReaders();
                    return fireOperationStateChanged(state, messageKey, instanceName);
                }
                return startClusterOrInstance(adminHost, adminPort);
            }

            // if we are profiling, we need to lie about the status?
            if (null != jvmArgs) {
                Logger.getLogger("glassfish").log(Level.FINE,
                        "Profiling mode status hack for {0}",
                        new Object[] {instance.getName()});
                // save process to be able to stop process waiting for profiler to attach
                support.setLocalStartProcess(serverProcess);
                // try to sync the states after the profiler attaches
                NODE_REFRESHER.post(new Runnable() {

                    @Override
                    public void run() {
                        while (!support.isReady(false, 2000, TIMEUNIT)) { // !CommonServerSupport.isRunning(support.getHostName(), support.getAdminPortNumber(),                                instance.getProperty(GlassfishModule.DISPLAY_NAME_ATTR))) {
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
            // if the user is at a bp somewhere in the startup process we may 
            //   not be finished with the start but 'not dead yet' all the same.
            if (null != vmi && null != debugPort && vmi.isSuspended(adminHost, debugPort)) {
                start = System.currentTimeMillis();
            }
        }

        // If the server did not start in the designated time limits
        // We consider the startup as failed and warn the user
        Logger.getLogger("glassfish").log(Level.INFO,
                "{0} Failed to start, killing process {1} after {2} ms",
                new Object[]{instance.getName(), serverProcess,
                System.currentTimeMillis() - start});
        LogViewMgr.removeServerLogStream(instance);
        serverProcess.destroy();
        logger.stopReaders();
        return fireOperationStateChanged(OperationState.FAILED,
                "MSG_START_SERVER_FAILED2", instanceName, adminHost, adminPort + ""); // NOI18N
    }

    private OperationState startClusterOrInstance(String adminHost, int adminPort) {
        String target = Util.computeTarget(instance.getProperties());
        if (Util.isDefaultOrServerTarget(instance.getProperties())) {
            return fireOperationStateChanged(OperationState.COMPLETED,
                    "MSG_SERVER_STARTED", instanceName); // NOI18N
        } else {
            OperationState retVal = null;
            // try start-cluster
            CommandRunner inner = new CommandRunner(true,
                    support.getCommandFactory(), instance,
                    new OperationStateListener() {

                        @Override
                        public void operationStateChanged(OperationState newState, String message) {
                        }
                    });
            Future<OperationState> result = inner.execute(new Commands.StartCluster(target));
            OperationState state = null;
            try {
                state = result.get();
            } catch (InterruptedException ie) {
                Logger.getLogger("glassfish").log(Level.INFO, "start-cluster", ie); // NOI18N
            } catch (ExecutionException ie) {
                Logger.getLogger("glassfish").log(Level.INFO, "start-cluster", ie); // NOI18N
            }
            if (state == OperationState.FAILED) {
                // if start-cluster not successful, try start-instance
                inner = new CommandRunner(true,
                        support.getCommandFactory(), instance,
                        new OperationStateListener() {

                            @Override
                            public void operationStateChanged(OperationState newState, String message) {
                            }
                        });
                result = inner.execute(new Commands.StartInstance(target));
                try {
                    state = result.get();
                } catch (InterruptedException ie) {
                    Logger.getLogger("glassfish").log(Level.INFO, "start-instance", ie);  // NOI18N
                } catch (ExecutionException ie) {
                    Logger.getLogger("glassfish").log(Level.INFO, "start-instance", ie);  // NOI18N
                }
                if (state == OperationState.FAILED) {
                    // if start instance not suscessful fail
                    return fireOperationStateChanged(OperationState.FAILED,
                            "MSG_START_TARGET_FAILED", instanceName, target); // NOI18N
                }
            }

            // update http port
            support.updateHttpPort();

            // ping the http port

            return fireOperationStateChanged(OperationState.COMPLETED,
                    "MSG_SERVER_STARTED", instanceName); // NOI18N
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
                result = result.substring(0, result.length() - 4);
            }
        }
        return result;
    }

    private StartupArgs createProcessDescriptor() throws ProcessCreationException {
        List<String> glassfishArgs = new ArrayList<String>(2);
        String domainDir = Util.quote(getDomainFolder().getAbsolutePath());
        glassfishArgs.add(ServerUtils.cmdLineArgument(
                ServerUtils.GF_DOMAIN_ARG, getDomainName()));
        glassfishArgs.add(ServerUtils.cmdLineArgument(
                ServerUtils.GF_DOMAIN_DIR_ARG, domainDir));

        ArrayList<String> optList = new ArrayList<String>();
        // append debug options
        if (GlassfishModule.DEBUG_MODE.equals(instance.getProperty(GlassfishModule.JVM_MODE))) {
            appendDebugOptions(optList);
        }
        
        // append other options from startup extenders, e.g. for profiling
        appendStartupExtenderParams(optList);

        return new StartupArgsEntity(
                glassfishArgs,
                optList,
                (Map<String, String>) null,
                FileUtil.toFile(jdkHome).getAbsolutePath());
    }

    /**
     * Appends debug options for server start.
     * If the port read from instance properties is not valid (null or out of the range),
     * it offers to the user a different free port.
     * 
     * @param optList
     * @throws ProcessCreationException 
     */
    private void appendDebugOptions(List<String> optList) throws ProcessCreationException {
        String debugPortString = instance.getProperty(GlassfishModule.DEBUG_PORT);
        String debugTransport = "dt_socket"; // NOI18N
        if ("true".equals(instance.getProperty(GlassfishModule.USE_SHARED_MEM_ATTR))) { // NOI18N
            debugTransport = "dt_shmem";  // NOI18N
        } else {
            if (null != debugPortString && debugPortString.trim().length() > 0) {
                int t = Integer.parseInt(debugPortString);
                if (t < LOWEST_USER_PORT || t > 65535) {
                    throw new NumberFormatException();
                }
            }
        }
        //try {
        if (null == debugPortString || "".equals(debugPortString)) {
            if ("true".equals(instance.getProperty(GlassfishModule.USE_SHARED_MEM_ATTR))) { // NOI18N
                debugPortString = Integer.toString(
                        Math.abs((instance.getProperty(GlassfishModule.GLASSFISH_FOLDER_ATTR)
                        + instance.getDomainsRoot()
                        + instance.getProperty(GlassfishModule.DOMAIN_NAME_ATTR)).hashCode() + 1));
            } else {
                try {
                    debugPortString = selectDebugPort();
                } catch (IOException ioe) {
                    throw new ProcessCreationException(ioe,
                            "MSG_START_SERVER_FAILED_INVALIDPORT", instanceName, debugPortString); //NOI18N
                }
            }
            DialogDescriptor note =
                    new DialogDescriptor(
                    NbBundle.getMessage(StartTask.class, "MSG_SELECTED_PORT", debugPortString),
                    NbBundle.getMessage(StartTask.class, "TITLE_MESSAGE"),
                    false,
                    new Object[]{DialogDescriptor.OK_OPTION},
                    DialogDescriptor.OK_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);
            Dialog d = DialogDisplayer.getDefault().createDialog(note);
            d.setVisible(true);
        }
        support.setEnvironmentProperty(GlassfishModule.DEBUG_PORT, debugPortString, true);
        optList.add("-Xdebug");
        StringBuilder opt = new StringBuilder();
        opt.append("-Xrunjdwp:transport="); // NOI18N
        opt.append(debugTransport);
        opt.append(",address="); // NOI18N
        opt.append(debugPortString);
        opt.append(",server=y,suspend=n"); // NOI18N
        optList.add(opt.toString());
    }
    
    private void appendStartupExtenderParams(List<String> optList) {
        for (StartupExtender args : StartupExtender.getExtenders(
                Lookups.singleton(support.getInstanceProvider().getInstance(instance.getProperty("url"))), 
                getMode(instance.getProperty(GlassfishModule.JVM_MODE)))) {
            for (String arg : args.getArguments()) {
                String[] argSplitted = arg.trim().split("\\s+(?=-)");
                optList.addAll(Arrays.asList(argSplitted));
            }
        }
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
            if (null != t) {
                try {
                    t.close();
                } catch (IOException ioe) {
                }
            }
        }
        try {
            // try to find a different port... if this fails,
            //    it is a great time to panic.
            t = new ServerSocket(0);
            debugPort = t.getLocalPort();
            return Integer.toString(debugPort);
        } finally {
            if (null != t) {
                try {
                    t.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    private Process createProcess() throws ProcessCreationException {
        StartupArgs args = createProcessDescriptor();
        ResultProcess process = ServerTasks.startServer(instance, args);
        if (process.getState() != TaskState.COMPLETED) {
            throw new ProcessCreationException(null, "MSG_START_SERVER_FAILED_PD", instanceName);
        }
        return process.getValue().getProcess();
    }

    private File getDomainFolder() {
        return new File(support.getDomainsRoot() + File.separatorChar + getDomainName());
    }

    private String getDomainName() {
        return instance.getProperty(GlassfishModule.DOMAIN_NAME_ATTR);
    }

    private boolean upgradeFailed() {
        // get server install version
        File glassfishDir = new File(support.getGlassfishRoot());
        int installVersion = ServerDetails.getVersionFromInstallDirectory(glassfishDir);

        if (installVersion < 0) {
            return false;  // no upgrade attempted, so it DID NOT fail.
        }
        // get domain.xml 'version'
        File domainXmlFile = new File(getDomainFolder(), "config" + File.separator + "domain.xml"); // NOI18N
        int domainVersion = ServerDetails.getVersionFromDomainXml(domainXmlFile);

        if (domainVersion < 0) {
            return false;  // no upgrade attempted, so it DID NOT fail.
        }
        if (domainVersion / 10 < installVersion / 10 && domainVersion < 310) {
            return executeUpgradeProcess() != 0;
        }
        return false;
    }

    private int executeUpgradeProcess() {
        int retVal = -1;
        File asadmin = findFirstExecutableFile(new File(support.getGlassfishRoot()), "asadmin", "bin");
        if (null == asadmin) {
            return retVal;
        }
        NbProcessDescriptor upgrader = new NbProcessDescriptor(asadmin.getAbsolutePath(),
                "start-domain --upgrade --domaindir " + Util.quote(support.getDomainsRoot()) + " " + // NOI18N
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
    
    private static StartupExtender.StartMode getMode(String gfMode) {
        if (GlassfishModule.PROFILE_MODE.equals(gfMode)) {
            return StartupExtender.StartMode.PROFILE;
        } else if (GlassfishModule.DEBUG_MODE.equals(gfMode)) {
            return StartupExtender.StartMode.DEBUG;
        } else {
            return StartupExtender.StartMode.NORMAL;
        }
    }
}
