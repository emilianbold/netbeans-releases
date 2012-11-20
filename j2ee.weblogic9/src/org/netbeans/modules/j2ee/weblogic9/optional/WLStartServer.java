/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public final class WLStartServer extends StartServer {

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

    private final ExecutorService service = new RequestProcessor("WebLogic Start/Stop"); // NOI18N

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
        Process proc = dm.getServerProcess();

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
    public ProgressObject startProfiling( Target target )
    {
        LOGGER.log(Level.FINER, "Starting server in profiling mode"); // NOI18N

        WLServerProgress serverProgress = new WLServerProgress(this);

        String serverName = dm.getInstanceProperties().getProperty(
                InstanceProperties.DISPLAY_NAME_ATTR);
        serverProgress.notifyStart(StateType.RUNNING,
                NbBundle.getMessage(WLStartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName));

        String uri = dm.getUri();
        service.submit(new WLProfilingStartTask(uri, serverProgress, dm));

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

    @Override
    public boolean needsRestart(Target target) {
        return dm.isRestartNeeded();
    }

    private static boolean ping(String host, int port, int timeout) {
        if (pingPath(host, port, timeout, "/console/login/LoginForm.jsp")) {
            return true;
        }
        // TODO this is somehow broken - we have to access consoledwp first,
        // getting 404 - this cause console is then available under console
        return pingPath(host, port, timeout, "/consoledwp")
                || pingPath(host, port, timeout, "/console");
    }

    private static boolean pingPath(String host, int port, int timeout, String path) {
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
                        out.println("GET " + path + " HTTP/1.1\nHost:\n"); // NOI18N
                        String line = in.readLine();
                        return "HTTP/1.1 200 OK".equals(line)
                                || "HTTP/1.1 302 Moved Temporarily".equals(line);
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
                process.getInputStream(), Charset.defaultCharset()), 
                InputProcessors.printing(io.getOut(), new ErrorLineConvertor(), true)));
        service.submit(InputReaderTask.newTask(InputReaders.forStream(
                process.getErrorStream(), Charset.defaultCharset()), 
                InputProcessors.printing(io.getErr(), new ErrorLineConvertor(), false)));
    }

    private static void stopService(String uri, final ExecutorService service) {
        if (service != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                public Void run() {
                    service.shutdownNow();
                    return null;
                }
            });
            InputOutput io = UISupport.getServerIO(uri);
            if (io != null) {
                io.getOut().close();
                io.getErr().close();
            }
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

    private static StringBuilder appendNonProxyHosts(StringBuilder sb) {
        if (sb.indexOf("http.nonProxyHosts") < 0) { // NOI18N
            String nonProxyHosts = NonProxyHostsHelper.getNonProxyHosts();
            if (!nonProxyHosts.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append("-Dhttp.nonProxyHosts="); // NOI18N
                sb.append('"').append(nonProxyHosts).append('"');
            }
        }
        return sb;
    }

    private class WLProfilingStartTask extends WLStartTask {

        public WLProfilingStartTask(String uri, WLServerProgress serverProgress,
                WLDeploymentManager dm) {

            super( uri , serverProgress, dm );
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
                Process process = dm.getServerProcess();
                if (process != null) {
                    Map<String, String> mark = new HashMap<String, String>();
                    mark.put(WLStartTask.KEY_UUID, dm.getUri());
                    ExternalProcessSupport.destroy(process, mark);
                }
            }
        }

        @Override
        protected ExternalProcessBuilder setJavaOptionsEnv(ExternalProcessBuilder builder) {
            ExternalProcessBuilder result = builder;

            StringBuilder javaOptsBuilder = new StringBuilder();
            String javaOpts = dm.getInstanceProperties().getProperty(
                    WLPluginProperties.JAVA_OPTS);
            if ( javaOpts!= null && javaOpts.trim().length() >0 ){
                javaOptsBuilder.append( " " );                              // NOI18N
                javaOptsBuilder.append( javaOpts.trim() );
            }

            for (StartupExtender args : StartupExtender.getExtenders(
                        Lookups.singleton(CommonServerBridge.getCommonInstance(dm.getUri())), StartupExtender.StartMode.PROFILE)) {
                for (String singleArg : args.getArguments()) {
                    javaOptsBuilder.append(' ').append(singleArg);
                }
            }

            appendNonProxyHosts(javaOptsBuilder);
            String toAdd = javaOptsBuilder.toString().trim();
            if (!toAdd.isEmpty()){
                result = result.addEnvironmentVariable(JAVA_OPTIONS_VARIABLE, 
                    toAdd);
            }
            return result;
        }

        @Override
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
    }

    private class WLDebugStartTask extends WLStartTask {

        public WLDebugStartTask(String uri, WLServerProgress serverProgress,
                WLDeploymentManager dm) {
            super( uri , serverProgress, dm  );
        }

        @Override
        protected ExternalProcessBuilder setJavaOptionsEnv(ExternalProcessBuilder builder) {
            int debugPort = 4000;
            debugPort = Integer.parseInt(dm.getInstanceProperties().getProperty(
                    WLPluginProperties.DEBUGGER_PORT_ATTR));

            StringBuilder javaOptsBuilder = new StringBuilder();
            String javaOpts = dm.getInstanceProperties().getProperty(
                    WLPluginProperties.JAVA_OPTS);
            if ( javaOpts!= null && javaOpts.trim().length() >0 ){
                javaOptsBuilder.append( javaOpts.trim() );
            }
            if ( javaOptsBuilder.length()> 0 ){
                javaOptsBuilder.append(" ");                                    // NOI18N
            }
            javaOptsBuilder.append("-Xdebug -Xnoagent -Djava.compiler=none ");  // NOI18N
            javaOptsBuilder.append("-Xrunjdwp:server=y,suspend=n,transport=dt_socket,address=");// NOI18N
            javaOptsBuilder.append( debugPort );
            for (StartupExtender args : StartupExtender.getExtenders(
                        Lookups.singleton(CommonServerBridge.getCommonInstance(dm.getUri())), StartupExtender.StartMode.DEBUG)) {
                for (String singleArg : args.getArguments()) {
                    javaOptsBuilder.append(' ').append(singleArg);
                }
            }

            appendNonProxyHosts(javaOptsBuilder);
            ExternalProcessBuilder result = builder.addEnvironmentVariable(
                    JAVA_OPTIONS_VARIABLE,
                    javaOptsBuilder.toString());   
            return result;

        }
    }

    private class WLStartTask implements Runnable {

        static final String JAVA_VENDOR_VARIABLE = "JAVA_VENDOR";    // NOI18N

        static final String JAVA_OPTIONS_VARIABLE = "JAVA_OPTIONS";  // NOI18N
        
        static final String MEMORY_OPTIONS_VARIABLE= "USER_MEM_ARGS";// NOI18N

        static final String MEMORY_OPTIONS_VARIABLE_11_WEB = "MEM_ARGS";// NOI18N

        private static final String KEY_UUID = "NB_EXEC_WL_START_PROCESS_UUID"; //NOI18N

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
         * Name of the startup script for Unices
         */
        private static final String STARTUP_SH = "startWebLogic.sh";   // NOI18N

        /**
         * Name of the startup script for windows
         */
        private static final String STARTUP_BAT = "startWebLogic.cmd"; // NOI18N

        /**
         * Name of the startup script for Unices
         */
        private static final String STARTUP_SH_DWP_ALTERNATIVE = "startServer.sh";   // NOI18N

        /**
         * Name of the startup script for windows
         */
        private static final String STARTUP_BAT_DWP_ALTERNATIVE = "startServer.cmd"; // NOI18N

        private final String uri;

        private final WLServerProgress serverProgress;

        private final WLDeploymentManager dm;

        public WLStartTask(String uri, WLServerProgress serverProgress, WLDeploymentManager dm) {
            this.uri = uri;
            this.serverProgress = serverProgress;
            this.dm = dm;
        }

        @Override
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

                File startup = null;
                if (Utilities.isWindows()) {
                    startup = new File(domainHome, STARTUP_BAT);
                    if (!startup.exists()) {
                        startup = new File(new File(domainHome, "bin"), STARTUP_BAT); // NOI18N
                    }
                    if (!startup.exists()) {
                        startup = new File(new File(domainHome, "bin"), STARTUP_BAT_DWP_ALTERNATIVE); // NOI18N
                    }
                } else {
                    startup = new File(domainHome, STARTUP_SH);
                    if (!startup.exists()) {
                        startup = new File(new File(domainHome, "bin"), STARTUP_SH); // NOI18N
                    }                    
                    if (!startup.exists()) {
                        startup = new File(new File(domainHome, "bin"), STARTUP_SH_DWP_ALTERNATIVE); // NOI18N
                    }
                }

                ExternalProcessBuilder builder = new ExternalProcessBuilder(startup.getAbsolutePath());
                builder = builder.workingDirectory(domainHome)
                        .addEnvironmentVariable(KEY_UUID, dm.getUri());
                
                String mwHome = dm.getProductProperties().getMiddlewareHome();
                if (mwHome != null) {
                    builder = builder.addEnvironmentVariable("MW_HOME", mwHome); // NOI18N
                }

                builder = initBuilder(builder);

                Process process = builder.call();
                dm.setServerProcess(process);

                ExecutorService service = Executors.newFixedThreadPool(2);
                startService(uri, process, service);

                String serverName = dm.getInstanceProperties().getProperty(
                        InstanceProperties.DISPLAY_NAME_ATTR);

                // wait till the timeout happens, or if the server starts before
                // send the completed event to j2eeserver
                while ((System.currentTimeMillis() - start) < TIMEOUT) {
                    if (isRunning()) {
                        // reset the restart flag
                        dm.setRestartNeeded(false);

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
                            stopService(uri, service);
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

        private ExternalProcessBuilder initBuilder(ExternalProcessBuilder builder) {
            ExternalProcessBuilder result = builder;
            
            result = setJavaOptionsEnv(result);
            String vendor = dm.getInstanceProperties().getProperty(WLPluginProperties.VENDOR);
            if (vendor != null && vendor.trim().length() > 0) {
                result = result.addEnvironmentVariable(JAVA_VENDOR_VARIABLE,
                        vendor.trim());
            }
            String memoryOptions = dm.getInstanceProperties().getProperty(
                    WLPluginProperties.MEM_OPTS);
            if (memoryOptions != null && memoryOptions.trim().length() > 0) {
                result = result.addEnvironmentVariable(MEMORY_OPTIONS_VARIABLE,
                        memoryOptions.trim());
            }  
            return result;
        }
        
        protected ExternalProcessBuilder setJavaOptionsEnv(ExternalProcessBuilder builder) {
            ExternalProcessBuilder result = builder;
            String javaOpts = dm.getInstanceProperties().getProperty(WLPluginProperties.JAVA_OPTS);
            StringBuilder sb = new StringBuilder((javaOpts!= null && javaOpts.trim().length() > 0)
                    ? javaOpts.trim() : "");
            for (StartupExtender args : StartupExtender.getExtenders(
                        Lookups.singleton(CommonServerBridge.getCommonInstance(dm.getUri())), StartupExtender.StartMode.NORMAL)) {
                for (String singleArg : args.getArguments()) {
                    sb.append(' ').append(singleArg);
                }
            }

            appendNonProxyHosts(sb);
            if (sb.length() > 0) {
                result = builder.addEnvironmentVariable(JAVA_OPTIONS_VARIABLE,
                        sb.toString());
            }
            return result;
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

        @Override
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
            File shutdown = null;
            if (Utilities.isWindows()) {
                shutdown = new File(new File(domainHome, "bin"), SHUTDOWN_BAT); // NOI18N
            } else {
                shutdown = new File(new File(domainHome, "bin"), SHUTDOWN_SH); // NOI18N
            }

            ExecutorService stopService = null;
            Process stopProcess = null;
            String serverName = dm.getInstanceProperties().getProperty(
                    InstanceProperties.DISPLAY_NAME_ATTR);
            String uuid = UUID.randomUUID().toString();

            try {
                long start = System.currentTimeMillis();    

                if (shutdown.exists()) {
                    ExternalProcessBuilder builder = new ExternalProcessBuilder(shutdown.getAbsolutePath());

                    builder = builder.workingDirectory(domainHome)
                            .addEnvironmentVariable(KEY_UUID, uuid)
                            .addArgument(username)
                            .addArgument(password)
                            .addArgument("t3://" + host + ":" + port);

                    String mwHome = dm.getProductProperties().getMiddlewareHome();
                    if (mwHome != null) {
                        builder = builder.addEnvironmentVariable("MW_HOME", mwHome); // NOI18N
                    }

                    stopProcess = builder.call();
                    stopService = Executors.newFixedThreadPool(2);
                    startService(uri, stopProcess, stopService);
                } else {
                        Process process = dm.getServerProcess();
                        if (process == null) {
                            // FIXME what to do here
                            serverProgress.notifyStop(StateType.FAILED,
                                NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_FAILED", serverName));
                            return;
                        }
                        Map<String, String> mark = new HashMap<String, String>();
                        mark.put(WLStartTask.KEY_UUID, dm.getUri());
                        ExternalProcessSupport.destroy(process, mark);
                }

                while ((System.currentTimeMillis() - start) < TIMEOUT) {
                    if (isRunning() && isRunning(stopProcess)) {
                        serverProgress.notifyStop(StateType.RUNNING,
                                NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName));
                        try {
                            Thread.sleep(DELAY);
                        } catch (InterruptedException e) {
                            serverProgress.notifyStart(StateType.FAILED,
                                    NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_INTERRUPTED"));
                            Thread.currentThread().interrupt();
                            return;
                        }
                    } else {
                        if (stopProcess != null) {
                            try {
                                stopProcess.waitFor();
                            } catch (InterruptedException ex) {
                                serverProgress.notifyStart(StateType.FAILED,
                                        NbBundle.getMessage(WLStartServer.class, "MSG_STOP_SERVER_INTERRUPTED"));
                                Thread.currentThread().interrupt();
                                return;
                            }
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
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, null, e);
            } finally {
                // do the cleanup
                if (stopProcess != null) {
                    Map<String, String> mark = new HashMap<String, String>();
                    mark.put(KEY_UUID, uuid);
                    ExternalProcessSupport.destroy(stopProcess, mark);
                    stopService(uri, stopService);
                }
            }
        }
    }
}
