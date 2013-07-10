/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.glassfish.tools.ide.GlassFishIdeException;
import org.glassfish.tools.ide.admin.*;
import org.glassfish.tools.ide.data.StartupArgs;
import org.glassfish.tools.ide.data.StartupArgsEntity;
import org.glassfish.tools.ide.data.TaskEvent;
import org.glassfish.tools.ide.server.FetchLogSimple;
import org.glassfish.tools.ide.server.ServerTasks;
import org.glassfish.tools.ide.utils.ServerUtils;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.modules.glassfish.common.ui.JavaSEPlatformPanel;
import org.netbeans.modules.glassfish.common.utils.AdminKeyFile;
import org.netbeans.modules.glassfish.common.utils.JavaUtils;
import org.netbeans.modules.glassfish.common.utils.Util;
import org.netbeans.modules.glassfish.spi.*;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
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
public class StartTask extends BasicTask<TaskState> {

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(StartTask.class);

    private static final String MAIN_CLASS = "com.sun.enterprise.glassfish.bootstrap.ASMain"; // NOI18N
    private final CommonServerSupport support;
    private List<Recognizer> recognizers;
    private List<String> jvmArgs = null;
    static final private int LOWEST_USER_PORT = org.openide.util.Utilities.isWindows() ? 1 : 1025;
    private final VMIntrospector vmi;
    private static RequestProcessor NODE_REFRESHER = new RequestProcessor("nodes to refresh");

    /** internal Java SE platform home cache. */
    private FileObject jdkHome;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    private static String[] removeEscapes(String[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].replace("\\\"", ""); // NOI18N
        }
        return args;
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

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param support common support object for the server instance being
     * started
     * @param recognizers output recognizers to pass to log processors, if any
     * @param stateListener state monitor to track start progress
     */
    public StartTask(CommonServerSupport support, List<Recognizer> recognizers,
            VMIntrospector vmi,
            TaskStateListener... stateListener) {
        this(support, recognizers, vmi, null, stateListener);
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
    public StartTask(final CommonServerSupport support,
            List<Recognizer> recognizers, VMIntrospector vmi, String[] jvmArgs,
            TaskStateListener... stateListener) {
        super(support.getInstance(), stateListener);
        List<TaskStateListener> listeners = new ArrayList<TaskStateListener>();
        listeners.addAll(Arrays.asList(stateListener));
        listeners.add(new TaskStateListener() {

            @Override
            public void operationStateChanged(TaskState newState,
                    TaskEvent event, String... args) {
                if (TaskState.COMPLETED.equals(newState)) {
                    // attempt to sync the comet support
                    RequestProcessor.getDefault().post(
                            new EnableComet(support.getInstance()));
                }
            }
        });
        this.stateListener = listeners.toArray(new TaskStateListener[listeners.size()]);
        this.support = support;
        this.recognizers = recognizers;
        this.jvmArgs = (jvmArgs != null) ? Arrays.asList(removeEscapes(jvmArgs)) : null;
        this.vmi = vmi;
        this.jdkHome = null;
        LOGGER.log(Level.FINE, "VMI == {0}", vmi);
    }

    ////////////////////////////////////////////////////////////////////////////
    // ExecutorService call() Method                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     */
    @Override
    public TaskState call() {
        // Save the current time so that we can deduct that the startup
        // Failed due to timeout
        LOGGER.log(Level.FINEST, "StartTask.call() called on thread \"{0}\"", Thread.currentThread().getName()); // NOI18N
        final long start = System.currentTimeMillis();

        final String adminHost;
        final int adminPort;

        adminHost = instance.getProperty(GlassfishModule.HOSTNAME_ATTR);
        if (adminHost == null || adminHost.length() == 0) {
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED,
                    "MSG_START_SERVER_FAILED_NOHOST", instanceName);
        }

        adminPort = Integer.valueOf(instance.getProperty(
                GlassfishModule.ADMINPORT_ATTR));
        if (adminPort < 0 || adminPort > 65535) {
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED,
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName);
        }

        if (support.isRemote()) {
            if (GlassFishState.isReady(instance, false)) {
                if (Util.isDefaultOrServerTarget(instance.getProperties())) {
                    return restartDAS(adminHost, adminPort, start);
                } else {
                    return startClusterOrInstance(adminHost, adminPort);
                }
            } else {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_START_SERVER_FAILED_DASDOWN", instanceName);
            }
        } else if (!GlassFishState.isReady(instance, false)) {
            return startDASAndClusterOrInstance(adminHost, adminPort);
        } else {
            return startClusterOrInstance(adminHost, adminPort);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    private TaskState restartDAS(String adminHost, int adminPort, final long start) {
        // deal with the remote case here...
        TaskStateListener[] listeners = {
                new TaskStateListener() {
                    // if the http command is successful, we are not done yet...
                    // The server still has to stop. If we signal success to the 'stateListener'
                    // for the task, it may be premature.
                    @SuppressWarnings("SleepWhileInLoop")
                    @Override
                    public void operationStateChanged(TaskState newState,
                            TaskEvent event, String... args) {
                        if (newState == TaskState.RUNNING) {
                            support.setServerState(ServerState.STARTING);
                        }
                        if (newState == TaskState.FAILED) {
                            fireOperationStateChanged(newState, event,
                                    instanceName, args);
                            support.setServerState(ServerState.STOPPED);
                            //support.refresh();
                        } else if (args != null && newState == TaskState.COMPLETED) {
                            for (String message : args) {
                                if (message.matches("[sg]et\\?.*\\=configs\\..*")) {
                                    return;
                                }
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
                                boolean httpLive = GlassFishState.isReady(instance, false); //CommonServerSupport.isRunning(host, port,instance.getProperty(GlassfishModule.DISPLAY_NAME_ATTR));

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
                }};
        int debugPort = -1;
        if (GlassfishModule.DEBUG_MODE.equals(instance.getProperty(GlassfishModule.JVM_MODE))) {
            try {
                debugPort = Integer.parseInt(instance.getProperty(GlassfishModule.DEBUG_PORT));
                if (debugPort < LOWEST_USER_PORT || debugPort > 65535) {
                    support.setEnvironmentProperty(GlassfishModule.DEBUG_PORT, "9009", true);
                    debugPort = 9009;
                    LOGGER.log(Level.INFO, "converted debug port to 9009 for {0}", instanceName);
                }
            } catch (NumberFormatException nfe) {
                support.setEnvironmentProperty(GlassfishModule.DEBUG_PORT, "9009", true);
                support.setEnvironmentProperty(GlassfishModule.USE_SHARED_MEM_ATTR, "false", true);
                debugPort = 9009;
                LOGGER.log(Level.INFO, "converted debug type to socket and port to 9009 for {0}", instanceName);
            }
        }
        support.restartServer(debugPort,
                support.supportsRestartInDebug() && debugPort >= 0, listeners);
        return fireOperationStateChanged(TaskState.RUNNING,
                TaskEvent.CMD_FAILED,
                "MSG_START_SERVER_IN_PROGRESS", instanceName); // NOI18N

    }

    @SuppressWarnings("SleepWhileInLoop")
    private TaskState startDASAndClusterOrInstance(String adminHost, int adminPort) {
        Process serverProcess;
        AdminKeyFile keyFile = new AdminKeyFile(instance);
        keyFile.read();
        if (keyFile.isReset()) {
            String password = AdminKeyFile.randomPassword(
                    AdminKeyFile.RANDOM_PASSWORD_LENGTH);            
            instance.setPassword(password);
            keyFile.setPassword(password);
            try {
                GlassfishInstance.writeInstanceToFile(instance);
            } catch(IOException ex) {
                LOGGER.log(Level.INFO,
                        "Could not store GlassFish server attributes", ex);
            }
            keyFile.write();
// Password change dialog disabled.
//            String password = GlassFishPassword.setPassword(instance);
//            if (password != null) {
//                keyFile.setPassword(password);
//                keyFile.write();
//            }
        }
        try {
            if (null == jdkHome) {
                jdkHome = getJavaPlatformRoot();
                File jdkHomeFile = FileUtil.toFile(jdkHome);
                if (!JavaUtils.isJavaPlatformSupported(instance, jdkHomeFile)) {
                    jdkHome = JavaSEPlatformPanel.selectServerSEPlatform(
                            instance, jdkHomeFile);
                }
            }
            if (jdkHome == null) {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED, null , instanceName);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex); // NOI18N
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED, "MSG_PASS_THROUGH",
                    ex.getLocalizedMessage());
        }
        // Time must be measured after Java SE platform selection is done.
        long start = System.currentTimeMillis();
        try {
            // lookup the javadb start service and use it here.
            RegisteredDerbyServer db
                    = Lookup.getDefault().lookup(RegisteredDerbyServer.class);
            if (null != db && "true".equals(
                    instance.getProperty(GlassfishModule.START_DERBY_FLAG))) { // NOI18N
                db.start();
            }
            int testPort = 0;
            String portCandidate = Integer.toString(instance.getAdminPort());
            try {
                testPort = Integer.parseInt(portCandidate);
            } catch (NumberFormatException nfe) {
                LOGGER.log(Level.INFO,
                        "could not parse {0} as an Inetger", portCandidate); // NOI18N
            }
            // this may be an autheticated server... so we will say it is started.
            // other operations will fail if the process on the port is not a
            // GF v3 server.
            LOGGER.log(Level.FINEST,
                    "Checking if GlassFish {0} is running. Timeout set to 20000 ms",
                    instance.getName());
            if (GlassFishState.isReady(instance, false)) {
                TaskState result = TaskState.COMPLETED;
                TaskEvent event = TaskEvent.CMD_COMPLETED;
                if (GlassfishModule.PROFILE_MODE.equals(
                        instance.getProperty(GlassfishModule.JVM_MODE))) {
                    result = TaskState.FAILED;
                    event = TaskEvent.CMD_FAILED;
                }
                return fireOperationStateChanged(result, event,
                        "MSG_START_SERVER_OCCUPIED_PORT", instanceName);
            } else if (testPort != 0 && Utils.isLocalPortOccupied(testPort)) {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_START_SERVER_OCCUPIED_PORT", instanceName);
            }
            if (upgradeFailed()) {
                return fireOperationStateChanged(TaskState.FAILED,
                        TaskEvent.CMD_FAILED,
                        "MSG_DOMAIN_UPGRADE_FAILED", instanceName);
            }
            serverProcess = createProcess();
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.INFO, instance.getProperty(
                    GlassfishModule.HTTPPORT_ATTR), nfe); // NOI18N
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED,
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName);
        } catch (ProcessCreationException ex) {
            Logger.getLogger("glassfish").log(Level.INFO,
                    "Could not start process for " + instanceName, ex);
            return fireOperationStateChanged(TaskState.FAILED,
                    TaskEvent.CMD_FAILED, "MSG_PASS_THROUGH",
                    ex.getLocalizedMessage());
        }

        fireOperationStateChanged(TaskState.RUNNING, TaskEvent.CMD_RUNNING,
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
        LOGGER.log(Level.FINER, "Waiting for server to start for {0} ms",
                new Object[] {Integer.toString(START_TIMEOUT)});
        while (System.currentTimeMillis() - start < START_TIMEOUT) {
            // Send the 'completed' event and return when the server is running
            boolean httpLive = CommonServerSupport.isRunning("localhost", adminPort, "localhost"); // Utils.isLocalPortOccupied(adminPort);
            LOGGER.log(Level.FINEST, "{0} DAS port {1} {2} alive",
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
                if (!GlassFishState.isReady(
                        instance, true, GlassFishState.Mode.STARTUP)) {
//                    TaskState  state = TaskState.FAILED;
                    String messageKey = "MSG_START_SERVER_FAILED"; // NOI18N
                    LOGGER.log(Level.INFO,
                            "{0} is not responding, killing the process.",
                            new Object[] {instance.getName()});
                    LogViewMgr.removeLog(instance);
                    serverProcess.destroy();
                    logger.stopReaders();
                    return fireOperationStateChanged(TaskState.FAILED,
                            TaskEvent.CMD_FAILED, messageKey, instanceName);
                }
                return startClusterOrInstance(adminHost, adminPort);
            }

            // if we are profiling, we need to lie about the status?
            if (null != jvmArgs) {
                LOGGER.log(Level.FINE,
                        "Profiling mode status hack for {0}",
                        new Object[] {instance.getName()});
                // save process to be able to stop process waiting for profiler to attach
                support.setLocalStartProcess(serverProcess);
                // try to sync the states after the profiler attaches
                NODE_REFRESHER.post(new Runnable() {

                    @Override
                    public void run() {
                        while (!GlassFishState.isReady(instance, false)) { // !CommonServerSupport.isRunning(support.getHostName(), support.getAdminPortNumber(),                                instance.getProperty(GlassfishModule.DISPLAY_NAME_ATTR))) {
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
                return fireOperationStateChanged(TaskState.COMPLETED,
                        TaskEvent.CMD_COMPLETED,
                        "MSG_SERVER_STARTED", instanceName);
            }
            // if the user is at a bp somewhere in the startup process we may 
            //   not be finished with the start but 'not dead yet' all the same.
            if (null != vmi && null != debugPort && vmi.isSuspended(adminHost, debugPort)) {
                start = System.currentTimeMillis();
            }
        }

        // If the server did not start in the designated time limits
        // We consider the startup as failed and warn the user
        LOGGER.log(Level.INFO,
                "{0} Failed to start, killing process {1} after {2} ms",
                new Object[]{instance.getName(), serverProcess,
                System.currentTimeMillis() - start});
        LogViewMgr.removeLog(instance);
        serverProcess.destroy();
        logger.stopReaders();
        return fireOperationStateChanged(
                TaskState.FAILED, TaskEvent.CMD_FAILED,
                "MSG_START_SERVER_FAILED2", instanceName,
                adminHost, adminPort + "");
    }

    private TaskState startClusterOrInstance(String adminHost, int adminPort) {
        String target = Util.computeTarget(instance.getProperties());
        if (Util.isDefaultOrServerTarget(instance.getProperties())) {
            return fireOperationStateChanged(TaskState.COMPLETED,
                    TaskEvent.CMD_COMPLETED,
                    "MSG_SERVER_STARTED", instanceName);
        } else {
            TaskState state;
            try {
                ResultString result
                        = CommandStartCluster.startCluster(instance, target);
                state = result.getState();
            } catch (GlassFishIdeException gfie) {
                state = TaskState.FAILED;
                LOGGER.log(Level.INFO, gfie.getMessage(), gfie);
            }
            if (state == TaskState.FAILED) {
                try {
                    ResultString result
                            = CommandStartInstance.startInstance(instance, target);
                    state = result.getState();
                } catch (GlassFishIdeException gfie) {
                    state = TaskState.FAILED;
                    LOGGER.log(Level.INFO, gfie.getMessage(), gfie);
                }
                if (state == TaskState.FAILED) {
                    // if start instance not suscessful fail
                    return fireOperationStateChanged(TaskState.FAILED,
                            TaskEvent.CMD_FAILED,
                            "MSG_START_TARGET_FAILED", instanceName, target);
                }
            }
            support.updateHttpPort();
            return fireOperationStateChanged(TaskState.COMPLETED,
                    TaskEvent.CMD_COMPLETED,
                    "MSG_SERVER_STARTED", instanceName);
        }
    }

    /**
     * Search for Java SE platform to be used for running GlassFish server.
     * <p/>
     * GlassFish instance Java SE platform property is checked first
     * and Java SE platform used to run NetBeans as a fallback option.
     * <p/>
     * @return Java SE platform to be used for running GlassFish server.
     * @throws IOException when GlassFish instance Java SE platform property
     *         does not point to existing directory.
     */
    private FileObject getJavaPlatformRoot() throws IOException {
        FileObject retVal;
        String javaHome = instance.getJavaHome();
        if (null == javaHome || javaHome.trim().length() < 1) {
            File dir = new File(getJdkHome());
            retVal = FileUtil.createFolder(FileUtil.normalizeFile(dir));
        } else {
            File f = new File(javaHome);
            if (f.exists()) {
                retVal = FileUtil.createFolder(FileUtil.normalizeFile(f));
            } else {
                throw new FileNotFoundException(
                        NbBundle.getMessage(StartTask.class,
                        "MSG_INVALID_JAVA", instanceName, javaHome));
            }
        }
        return retVal;
    }

    /**
     * Get Java SE platform used to run NetBeans.
     * <p/>
     * @return Java SE platform used to run NetBeans.
     */
    private String getJdkHome() {
        String result;
        result = System.getProperty("java.home");
        if (result.endsWith(File.separatorChar + "jre")) {
            result = result.substring(0, result.length() - 4);
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
                if (t != 0 && (t < LOWEST_USER_PORT || t > 65535)) {
                    throw new NumberFormatException();
                }
            }
        }
        //try {
        if (null == debugPortString
                || "0".equals(debugPortString) || "".equals(debugPortString)) {
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
        // JDK checks and Java VM process startup were moved to GF Tooling SDK.
        ResultProcess process = ServerTasks.startServer(instance, args);
        if (process.getState() != TaskState.COMPLETED) {
            throw new ProcessCreationException(null, "MSG_START_SERVER_FAILED_PD", instanceName);
        }
        return process.getValue().getProcess();
    }

    private File getDomainFolder() {
        return new File(instance.getDomainsRoot() + File.separatorChar + getDomainName());
    }

    private String getDomainName() {
        return instance.getProperty(GlassfishModule.DOMAIN_NAME_ATTR);
    }

    private boolean upgradeFailed() {
        // get server install version
        File glassfishDir = new File(instance.getGlassfishRoot());
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
        File asadmin = findFirstExecutableFile(new File(instance.getGlassfishRoot()), "asadmin", "bin");
        if (null == asadmin) {
            return retVal;
        }
        NbProcessDescriptor upgrader = new NbProcessDescriptor(asadmin.getAbsolutePath(),
                "start-domain --upgrade --domaindir " + Util.quote(instance.getDomainsRoot()) + " " + // NOI18N
                instance.getDomainName());
        try {
            Process p = upgrader.exec();
            p.waitFor();
            retVal = p.exitValue();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, upgrader.toString(), ex); // NOI18N
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, upgrader.toString(), ex); // NOI18N
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
