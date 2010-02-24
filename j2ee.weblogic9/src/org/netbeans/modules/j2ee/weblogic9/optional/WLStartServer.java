/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.optional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.ExternalProcessSupport;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.InputReaderTask;
import org.netbeans.api.extexecution.input.InputReaders;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public final class WLStartServer extends StartServer {
    
    private static final String SUN = "Sun";        // NOI18N

    /**
     * The socket timeout value for server ping. Unfortunately there is no right
     * value and the server state should be checked in different (more reliable)
     * way.
     */
    private static final int SERVER_CHECK_TIMEOUT = 10000;

    private static final Logger LOGGER = Logger.getLogger(WLStartServer.class.getName());

    /* GuardedBy(WLStartServer.class) */
    private static Set<String> SERVERS_IN_DEBUG;

    private final WLDeploymentManager dm;

    private final ExecutorService service = Executors.newCachedThreadPool();

    /* GuardedBy("this") */
    private Process serverProcess;

    public WLStartServer(WLDeploymentManager dm) {
        this.dm = dm;
    }

    @Override
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo(dm.getHost(), new Integer(
                dm.getInstanceProperties().getProperty(
                WLPluginProperties.DEBUGGER_PORT_ATTR)).intValue());
    }

    @Override
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }

    @Override
    public boolean isDebuggable(Target target) {
        if (!isServerInDebug(dm.getUri())) {
            return false;
        }
        if (!isRunning()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isRunning() {
        Process proc = null;
        synchronized (this) {
            proc = serverProcess;
        }

        if (!isRunning(proc)) {
            return false;
        }

        String host = dm.getHost();
        int port = Integer.parseInt(dm.getPort().trim());
        return ping(host, port, SERVER_CHECK_TIMEOUT); // is server responding?
    }

    @Override
    public boolean needsStartForAdminConfig() {
        return true;
    }

    @Override
    public boolean needsStartForConfigure() {
        return false;
    }

    @Override
    public boolean needsStartForTargetList() {
        return true;
    }

    @Override
    public ProgressObject startDebugging(Target target) {
        LOGGER.log(Level.FINER, "Starting server in debug mode"); // NOI18N

        WLServerProgress serverProgress = new WLServerProgress(this);

        String serverName = dm.getInstanceProperties().getProperty(
                InstanceProperties.DISPLAY_NAME_ATTR);
        serverProgress.notifyStart(StateType.RUNNING,
                NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName));

        String uri = dm.getUri();
        service.submit(new WLDebugStartTask(uri, serverProgress, dm));

        addServerInDebug(uri);
        return serverProgress;
    }

    @Override
    public ProgressObject startDeploymentManager() {
        LOGGER.log(Level.FINER, "Starting server"); // NOI18N

        WLServerProgress serverProgress = new WLServerProgress(this);

        String serverName = dm.getInstanceProperties().getProperty(
                InstanceProperties.DISPLAY_NAME_ATTR);
        serverProgress.notifyStart(StateType.RUNNING,
                NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName));

        String uri = dm.getUri();
        service.submit(new WLStartTask(uri, serverProgress, dm));

        removeServerInDebug(uri);
        return serverProgress;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer#startProfiling(javax.enterprise.deploy.spi.Target, org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings)
     */
    @Override
    public ProgressObject startProfiling( Target target,
            ProfilerServerSettings settings )
    {
        LOGGER.log(Level.FINER, "Starting server in profiling mode"); // NOI18N

        WLServerProgress serverProgress = new WLServerProgress(this);

        String serverName = dm.getInstanceProperties().getProperty(
                InstanceProperties.DISPLAY_NAME_ATTR);
        serverProgress.notifyStart(StateType.RUNNING,
                NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName));

        String uri = dm.getUri();
        service.submit(new WLProfilingStartTask(uri, serverProgress, dm, settings));

        removeServerInDebug(uri);
        return serverProgress;
    }

    @Override
    public ProgressObject stopDeploymentManager() {
        LOGGER.log(Level.FINER, "Stopping server"); // NOI18N

        WLServerProgress serverProgress = new WLServerProgress(this);

        String serverName = dm.getInstanceProperties().getProperty(
                InstanceProperties.DISPLAY_NAME_ATTR);
        serverProgress.notifyStart(StateType.RUNNING,
                NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName));

        String uri = dm.getUri();
        service.submit(new WLStopTask(uri, serverProgress, dm));

        removeServerInDebug(uri);
        return serverProgress;
    }

    @Override
    public boolean supportsStartDeploymentManager() {
        return true;
    }
    
    @Override
    public boolean supportsStartProfiling( Target target ) {
        return true;
    }

    @Override
    public boolean supportsStartDebugging(Target target) {
        //if we can start it we can debug it
        return supportsStartDeploymentManager();
    }

    private static boolean ping(String host, int port, int timeout) {
        // checking whether a socket can be created is not reliable enough, see #47048
        Socket socket = new Socket();
        try {
            try {
                socket.connect(new InetSocketAddress(host, port), timeout); // NOI18N
                socket.setSoTimeout(timeout);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    try {
                        // request for the login form - we guess that OK response
                        // means pinging the WL server
                        out.println("GET /console/login/LoginForm.jsp HTTP/1.1\nHost:\n"); // NOI18N

                        // check response
                        return "HTTP/1.1 200 OK".equals(in.readLine()); // NOI18N
                    } finally {
                        in.close();
                    }
                } finally {
                    out.close();
                }
            } finally {
                socket.close();
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.FINE, null, ioe);
            return false;
        }
    }

    private static synchronized void addServerInDebug(String uri) {
        if (SERVERS_IN_DEBUG == null) {
            SERVERS_IN_DEBUG = new HashSet<String>(1);
        }
        SERVERS_IN_DEBUG.add(uri);
    }

    private static synchronized void removeServerInDebug(String uri) {
        if (SERVERS_IN_DEBUG == null) {
            return;
        }
        SERVERS_IN_DEBUG.remove(uri);
    }

    private static synchronized boolean isServerInDebug(String uri) {
        return SERVERS_IN_DEBUG != null && SERVERS_IN_DEBUG.contains(uri);
    }

    private static void startService(String uri, Process process, ExecutorService service) {
        InputOutput io = UISupport.getServerIO(uri);
        if (io == null) {
            return;
        }

        try {
            // as described in the api we reset just ouptut
            io.getOut().reset();
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        io.select();

        service.submit(InputReaderTask.newTask(InputReaders.forStream(
                process.getInputStream(), Charset.defaultCharset()), InputProcessors.printing(io.getOut(), true)));
        service.submit(InputReaderTask.newTask(InputReaders.forStream(
                process.getErrorStream(), Charset.defaultCharset()), InputProcessors.printing(io.getErr(), false)));
    }

    private static void stopService(final ExecutorService service) {
        if (service != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                public Void run() {
                    service.shutdownNow();
                    return null;
                }
            });
        }
    }

    private static boolean isRunning(Process process) {
        if (process != null) {
            try {
                process.exitValue();
                // process is stopped
                return false;
            } catch (IllegalThreadStateException e) {
                // process is running
            }
        }
        return true;
    }
    
    private class WLProfilingStartTask extends WLStartTask {

        WLProfilingStartTask(String uri, WLServerProgress serverProgress,
                WLDeploymentManager dm, ProfilerServerSettings settings ) 
        {
            super( uri , serverProgress, dm );
            mySettings = settings;
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.j2ee.weblogic9.optional.WLStartServer.WLStartTask#run()
         */
        @Override
        public void run() {
            super.run();
            int state = ProfilerSupport.getState();
            if ( state == ProfilerSupport.STATE_INACTIVE){
                getProgress().notifyStart(StateType.FAILED,
                        NbBundle.getMessage(WLStartServer.class, 
                                "MSG_START_PROFILED_SERVER_FAILED",
                                dm.getInstanceProperties().getProperty(
                                        InstanceProperties.DISPLAY_NAME_ATTR)));
                Process process = null;
                synchronized (WLStartServer.this) {
                    process = serverProcess;
                }
                process.destroy();
            }
        }
        
        @Override
        protected ExternalProcessBuilder initBuilder(
                ExternalProcessBuilder builder )
        {
            ExternalProcessBuilder result = builder;
            JavaPlatform javaPlatform = getSettings().getJavaPlatform();
            String vendor = javaPlatform.getVendor();
            
            String javaHome = getJavaHome(javaPlatform);
            result = result.addEnvironmentVariable("JAVA_HOME", javaHome);  // NOI18N
            if ( SUN.equals( vendor )){
                result = result.addEnvironmentVariable("SUN_JAVA_HOME",     // NOI18N
                        javaHome);
            }
            
            StringBuilder javaOptsBuilder = new StringBuilder();
            String[] profJvmArgs = getSettings().getJvmArgs();
            for (int i = 0; i < profJvmArgs.length; i++) {
                javaOptsBuilder.append(" ").append(profJvmArgs[i]);         // NOI18N
            }
            result = result.addEnvironmentVariable("JAVA_OPTIONS",          // NOI18N 
                    javaOptsBuilder.toString());                        
            return result;
        }
        
        protected boolean isRunning(){
            int state = ProfilerSupport.getState();
            if (state == ProfilerSupport.STATE_BLOCKING || 
                    state == ProfilerSupport.STATE_RUNNING  ||
                    state == ProfilerSupport.STATE_PROFILING )
            {
                return true;
            }
            return super.isRunning();
        }
        
        private String getJavaHome(JavaPlatform platform) {
            FileObject fo = (FileObject)platform.getInstallFolders().iterator().next();
            return FileUtil.toFile(fo).getAbsolutePath();
        }
        
        private ProfilerServerSettings getSettings(){
            return mySettings; 
        }
        
        private ProfilerServerSettings mySettings;
    }
    
    private class WLDebugStartTask extends WLStartTask {
        WLDebugStartTask(String uri, WLServerProgress serverProgress,
                WLDeploymentManager dm ) 
        {
            super( uri , serverProgress, dm  );
        }
        
        
        @Override
        protected ExternalProcessBuilder initBuilder(
                ExternalProcessBuilder builder )
        {
            int debugPort = 4000;
            debugPort = Integer.parseInt(dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DEBUGGER_PORT_ATTR));
            
            ExternalProcessBuilder result = builder.addEnvironmentVariable("JAVA_OPTIONS",
                    "-Xdebug -Xnoagent -Djava.compiler=none " +
                    "-Xrunjdwp:server=y,suspend=n,transport=dt_socket,address="
                    + debugPort);   //NOI18N
            return result;
        }
    }

    private class WLStartTask implements Runnable {

        /**
         * The amount of time in milliseconds during which the server should
         * start
         */
        private static final int TIMEOUT = 300000;

        /**
         * The amount of time in milliseconds that we should wait between checks
         */
        private static final int DELAY = 1000;

        /**
         * Name of the startup script for windows
         */
        private static final String STARTUP_SH = "startWebLogic.sh";   // NOI18N

        /**
         * Name of the startup script for Unices
         */
        private static final String STARTUP_BAT = "startWebLogic.cmd"; // NOI18N

        private final String uri;

        private final WLServerProgress serverProgress;

        private final WLDeploymentManager dm;

        public WLStartTask(String uri, WLServerProgress serverProgress,
                WLDeploymentManager dm) 
        {
            this.uri = uri;
            this.serverProgress = serverProgress;
            this.dm = dm;
        }

        public void run() {
            String domainString = dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DOMAIN_ROOT_ATTR);

            File domainHome = new File(domainString);
            if (!domainHome.exists() || !domainHome.isDirectory()) {
                serverProgress.notifyStart(StateType.FAILED,
                        NbBundle.getMessage(WLStartServer.class, "MSG_NO_DOMAIN_HOME"));
                return;
            }

            try {
                long start = System.currentTimeMillis();

                ExternalProcessBuilder builder = new ExternalProcessBuilder(Utilities.isWindows()
                            ? new File(domainHome, STARTUP_BAT).getAbsolutePath() // NOI18N
                            : new File(domainHome, STARTUP_SH).getAbsolutePath()); // NOI18N
                builder = builder.workingDirectory(domainHome);

                builder = initBuilder(builder);

                Process process = null;
                synchronized (WLStartServer.this) {
                    serverProcess = builder.call();
                    process = serverProcess;
                }

                ExecutorService service = Executors.newFixedThreadPool(2);
                startService(uri, process, service);

                String serverName = dm.getInstanceProperties().getProperty(
                        InstanceProperties.DISPLAY_NAME_ATTR);

                // wait till the timeout happens, or if the server starts before
                // send the completed event to j2eeserver
                while ((System.currentTimeMillis() - start) < TIMEOUT) {
                    if (isRunning()) {
                        serverProgress.notifyStart(StateType.COMPLETED,
                                NbBundle.getMessage(WLStartServer.class, "MSG_SERVER_STARTED", serverName));

                        // FIXME we should wait for the process and kill service
                        boolean interrupted = false;
                        try {
                            process.waitFor();
                        } catch (InterruptedException ex) {
                            interrupted = true;
                        }
                        if (interrupted) {
                            Thread.currentThread().interrupt();
                        } else {
                            stopService(service);
                        }
                        return;
                    }
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        serverProgress.notifyStart(StateType.FAILED,
                                NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_INTERRUPTED"));
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                serverProgress.notifyStart(StateType.FAILED,
                        NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_TIMEOUT"));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        }
        
        protected ExternalProcessBuilder initBuilder(ExternalProcessBuilder builder){
            return builder;
        }
        
        protected boolean isRunning(){
            return WLStartServer.this.isRunning();
        }
        
        protected WLServerProgress getProgress(){
            return serverProgress;
        }
    }

    private class WLStopTask implements Runnable {

        /**
         * The amount of time in milliseconds during which the server should
         * stop
         */
        private static final int TIMEOUT = 300000;

        /**
         * The amount of time in milliseconds that we should wait between checks
         */
        private static final int DELAY = 1000;

        /**
         * Name of the shutdown script for windows
         */
        private static final String SHUTDOWN_SH = "stopWebLogic.sh"; // NOI18N

        /**
         * Name of the shutdown script for unices
         */
        private static final String SHUTDOWN_BAT = "stopWebLogic.cmd"; // NOI18N

        private static final String KEY_UUID = "NB_EXEC_WL_STOP_PROCESS_UUID"; //NOI18N

        private final String uri;

        private final WLServerProgress serverProgress;

        private final WLDeploymentManager dm;

        public WLStopTask(String uri, WLServerProgress serverProgress,
                WLDeploymentManager dm) {
            this.uri = uri;
            this.serverProgress = serverProgress;
            this.dm = dm;
        }

        public void run() {
            String username = dm.getInstanceProperties().getProperty(InstanceProperties.USERNAME_ATTR);
            String password = dm.getInstanceProperties().getProperty(InstanceProperties.PASSWORD_ATTR);

            // it is guaranteed it is WL
            String[] parts = uri.substring(WLDeploymentFactory.URI_PREFIX.length()).split(":");

            String host = parts[0];
            String port = parts.length > 1 ? parts[1] : "";

            String domainString = dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DOMAIN_ROOT_ATTR);

            File domainHome = new File(domainString);
            if (!domainHome.exists() || !domainHome.isDirectory()) {
                serverProgress.notifyStop(StateType.FAILED,
                        NbBundle.getMessage(WLStartServer.class, "MSG_NO_DOMAIN_HOME"));
                return;
            }

            ExecutorService stopService = null;
            try {
                long start = System.currentTimeMillis();
                String uuid = UUID.randomUUID().toString();

                ExternalProcessBuilder builder = new ExternalProcessBuilder(Utilities.isWindows()
                            ? new File(new File(domainHome, "bin"), SHUTDOWN_BAT).getAbsolutePath() // NOI18N
                            : new File(new File(domainHome, "bin"), SHUTDOWN_SH).getAbsolutePath()); // NOI18N

                builder = builder.workingDirectory(domainHome)
                        .addEnvironmentVariable(KEY_UUID, uuid)
                        .addArgument(username)
                        .addArgument(password)
                        .addArgument("t3://" + host + ":" + port);

                Process stopProcess = builder.call();
                stopService = Executors.newFixedThreadPool(2);
                startService(uri, stopProcess, stopService);

                String serverName = dm.getInstanceProperties().getProperty(
                        InstanceProperties.DISPLAY_NAME_ATTR);

                while ((System.currentTimeMillis() - start) < TIMEOUT) {
                    if (isRunning() && isRunning(stopProcess)) {
                        serverProgress.notifyStop(StateType.RUNNING,
                                NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName));
                        try {
                            Thread.sleep(DELAY);
                        } catch (InterruptedException e) {
                            serverProgress.notifyStart(StateType.FAILED,
                                    NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_INTERRUPTED"));
                            Thread.currentThread().interrupt();
                            return;
                        }
                    } else {
                        try {
                            stopProcess.waitFor();
                        } catch (InterruptedException ex) {
                            serverProgress.notifyStart(StateType.FAILED,
                                    NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_INTERRUPTED"));
                            Thread.currentThread().interrupt();
                            return;
                        }

                        if (isRunning()) {
                            serverProgress.notifyStop(StateType.FAILED,
                                NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_FAILED", serverName));
                        } else {
                            serverProgress.notifyStop(StateType.COMPLETED,
                                NbBundle.getMessage(WLStartServer.class, "MSG_SERVER_STOPPED", serverName));
                        }
                        return;
                    }
                }

                // if the server did not stop in the designated time limits
                // we consider the stop process as failed and kill the process
                serverProgress.notifyStop(StateType.FAILED,
                        NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_TIMEOUT"));

                // do the cleanup
                Map<String, String> mark = new HashMap<String, String>();
                mark.put(KEY_UUID, uuid);
                ExternalProcessSupport.destroy(stopProcess, mark);
                stopService(stopService);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        }
    }
}
