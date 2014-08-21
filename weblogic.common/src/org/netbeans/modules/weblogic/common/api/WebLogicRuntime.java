/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.weblogic.common.api;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.base.BaseExecutionDescriptor;
import org.netbeans.api.extexecution.base.Environment;
import org.netbeans.api.extexecution.base.Processes;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputReaderTask;
import org.netbeans.api.extexecution.base.input.InputReaders;
import org.openide.util.BaseUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public final class WebLogicRuntime {

    private static final Logger LOGGER = Logger.getLogger(WebLogicRuntime.class.getName());

    private static final RequestProcessor RUNTIME_RP = new RequestProcessor(WebLogicRuntime.class.getName(), 2);

    private static final String STARTUP_SH = "startWebLogic.sh";   // NOI18N

    private static final String STARTUP_BAT = "startWebLogic.cmd"; // NOI18N

    private static final String SHUTDOWN_SH = "stopWebLogic.sh"; // NOI18N

    private static final String SHUTDOWN_BAT = "stopWebLogic.cmd"; // NOI18N

    private static final String START_KEY_UUID = "NB_EXEC_WL_START_PROCESS_UUID"; //NOI18N

    private static final String STOP_KEY_UUID = "NB_EXEC_WL_STOP_PROCESS_UUID"; //NOI18N

    private static final int TIMEOUT = 300000;

    private static final int DELAY = 1000;

    private static final int CHECK_TIMEOUT = 10000;

    //@GuardedBy(PROCESSES)
    private static final WeakHashMap<WebLogicConfiguration, Process> PROCESSES = new WeakHashMap<WebLogicConfiguration, Process>();

    private final WebLogicConfiguration config;

    private WebLogicRuntime(WebLogicConfiguration config) {
        this.config = config;
    }

    @NonNull
    public static WebLogicRuntime getInstance(@NonNull WebLogicConfiguration config) {
        return new WebLogicRuntime(config);
    }

//    public static void clear(WebLogicConfiguration config) {
//        synchronized (PROCESSES) {
//            PROCESSES.remove(config);
//        }
//    }

    public void startAndWait(@NullAllowed final BaseExecutionDescriptor.InputProcessorFactory outFactory,
            @NullAllowed final BaseExecutionDescriptor.InputProcessorFactory errFactory,
            @NullAllowed final Map<String, String> environment) throws InterruptedException, ExecutionException {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> reference = new AtomicReference<>();

        start(outFactory, errFactory, new BlockingListener(latch, reference, false), environment, null);
        latch.await();

        Exception exception = reference.get();
        if (exception != null) {
            throw new ExecutionException(exception);
        }
    }

    public void start(@NullAllowed final BaseExecutionDescriptor.InputProcessorFactory outFactory,
            @NullAllowed final BaseExecutionDescriptor.InputProcessorFactory errFactory,
            @NullAllowed final RuntimeListener listener,
            @NullAllowed final Map<String, String> environment,
            @NullAllowed final RunningCondition condition) {

        if (listener != null) {
            listener.onStart();
        }

        if (config.isRemote()) {
            if (listener != null) {
                listener.onRunning();
                listener.onExit();
            }
            return;
        }

        RUNTIME_RP.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    File domainHome = config.getDomainHome();
                    if (!domainHome.exists() || !domainHome.isDirectory()) {
                        if (listener != null) {
                            listener.onFail();
                        }
                        return;
                    }

                    if (isRunning()) {
                        if (listener != null) {
                            listener.onRunning();
                        }
                        return;
                    }

                    long start = System.currentTimeMillis();

                    File startup;
                    if (BaseUtilities.isWindows()) {
                        startup = new File(domainHome, STARTUP_BAT);
                        if (!startup.exists()) {
                            startup = new File(new File(domainHome, "bin"), STARTUP_BAT); // NOI18N
                        }
                    } else {
                        startup = new File(domainHome, STARTUP_SH);
                        if (!startup.exists()) {
                            startup = new File(new File(domainHome, "bin"), STARTUP_SH); // NOI18N
                        }
                    }

                    org.netbeans.api.extexecution.base.ProcessBuilder builder =
                            org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
                    builder.setExecutable(startup.getAbsolutePath());
                    builder.setWorkingDirectory(domainHome.getAbsolutePath());
                    builder.getEnvironment().setVariable(START_KEY_UUID, config.getId());

                    File mwHome = config.getLayout().getMiddlewareHome();
                    if (mwHome != null) {
                        builder.getEnvironment().setVariable("MW_HOME", mwHome.getAbsolutePath()); // NOI18N
                    }

                    configureEnvironment(builder.getEnvironment(), environment);

                    Process process;
                    try {
                        process = builder.call();
                    } catch (IOException ex) {
                        if (listener != null) {
                            listener.onException(ex);
                        }
                        return;
                    }
                    synchronized (PROCESSES) {
                        PROCESSES.put(config, process);
                    }

                    if (listener != null) {
                        listener.onProcessStart();
                    }

                    ExecutorService service = Executors.newFixedThreadPool(2);
                    startService(service, process, outFactory, errFactory);

                    while ((System.currentTimeMillis() - start) < TIMEOUT) {
                        if ((condition != null && condition.isRunning()) || isRunning()) {
                            if (listener != null) {
                                listener.onRunning();
                            }

                            // FIXME we should wait for the process and kill service
                            boolean interrupted = false;
                            try {
                                process.waitFor();
                            } catch (InterruptedException ex) {
                                interrupted = true;
                            }
                            if (interrupted) {
                                // this is interruption just in wait for process
                                Thread.currentThread().interrupt();
                            } else {
                                stopService(service);
                                if (listener != null) {
                                    listener.onProcessFinish();
                                }
                            }
                            if (listener != null) {
                                listener.onFinish();
                            }
                            return;
                        }
                        try {
                            Thread.sleep(DELAY);
                        } catch (InterruptedException e) {
                            if (listener != null) {
                                listener.onInterrupted();
                            }
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    // timeouted
                    if (listener != null) {
                        listener.onTimeout();
                    }
                } finally {
                    if (listener != null) {
                        listener.onExit();
                    }
                }
            }
        });
    }

    public void stopAndWait(@NullAllowed final BaseExecutionDescriptor.InputProcessorFactory outFactory,
            @NullAllowed final BaseExecutionDescriptor.InputProcessorFactory errFactory) throws InterruptedException, ExecutionException {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> reference = new AtomicReference<>();

        stop(outFactory, errFactory, new BlockingListener(latch, reference, true));
        latch.await();

        Exception exception = reference.get();
        if (exception != null) {
            throw new ExecutionException(exception);
        }
    }

    public void stop(@NullAllowed final BaseExecutionDescriptor.InputProcessorFactory outFactory,
            @NullAllowed final BaseExecutionDescriptor.InputProcessorFactory errFactory,
            @NullAllowed final RuntimeListener listener) {

        if (listener != null) {
            listener.onStart();
        }

        if (config.isRemote()) {
            if (listener != null) {
                listener.onRunning();
                listener.onExit();
            }
            return;
        }

        RUNTIME_RP.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    File domainHome = config.getDomainHome();
                    if (!domainHome.isDirectory()) {
                        if (listener != null) {
                            listener.onFail();
                        }
                        return;
                    }
                    File shutdown;
                    if (BaseUtilities.isWindows()) {
                        shutdown = new File(new File(domainHome, "bin"), SHUTDOWN_BAT); // NOI18N
                    } else {
                        shutdown = new File(new File(domainHome, "bin"), SHUTDOWN_SH); // NOI18N
                    }

                    ExecutorService stopService = null;
                    Process stopProcess = null;
                    String uuid = UUID.randomUUID().toString();

                    try {
                        long start = System.currentTimeMillis();

                        if (shutdown.exists()) {
                            org.netbeans.api.extexecution.base.ProcessBuilder builder
                                    = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
                            builder.setExecutable(shutdown.getAbsolutePath());
                            builder.setWorkingDirectory(domainHome.getAbsolutePath());
                            builder.getEnvironment().setVariable(STOP_KEY_UUID, uuid);

                            List<String> arguments = new ArrayList<String>();
                            arguments.add(config.getUsername());
                            arguments.add(config.getPassword());
                            arguments.add(config.getAdminURL());
                            builder.setArguments(arguments);

                            File mwHome = config.getLayout().getMiddlewareHome();
                            if (mwHome != null) {
                                builder.getEnvironment().setVariable("MW_HOME", mwHome.getAbsolutePath()); // NOI18N
                            }

                            try {
                                stopProcess = builder.call();
                            } catch (IOException ex) {
                                if (listener != null) {
                                    listener.onException(ex);
                                }
                                return;
                            }

                            if (listener != null) {
                                listener.onProcessStart();
                            }

                            stopService = Executors.newFixedThreadPool(2);
                            startService(stopService, stopProcess, outFactory, errFactory);
                        } else {
                            Process process;
                            synchronized (PROCESSES) {
                                process = PROCESSES.get(config);
                            }
                            if (process == null) {
                                // FIXME what to do here
                                if (listener != null) {
                                    listener.onFail();
                                }
                                return;
                            }
                            Map<String, String> mark = new HashMap<String, String>();
                            mark.put(START_KEY_UUID, config.getId());
                            Processes.killTree(process, mark);
                        }

                        while ((System.currentTimeMillis() - start) < TIMEOUT) {
                            if (isRunning() && isRunning(stopProcess)) {
                                if (listener != null) {
                                    listener.onRunning();
                                }

                                try {
                                    Thread.sleep(DELAY);
                                } catch (InterruptedException e) {
                                    if (listener != null) {
                                        listener.onInterrupted();
                                    }
                                    Thread.currentThread().interrupt();
                                    return;
                                }
                            } else {
                                if (stopProcess != null) {
                                    try {
                                        stopProcess.waitFor();
                                    } catch (InterruptedException ex) {
                                        if (listener != null) {
                                            listener.onInterrupted();
                                        }
                                        Thread.currentThread().interrupt();
                                        return;
                                    }
                                }

                                if (isRunning()) {
                                    if (listener != null) {
                                        listener.onFail();
                                    }
                                } else {
                                    if (listener != null) {
                                        listener.onFinish();
                                    }
                                }
                                return;
                            }
                        }

                        // timeouted
                        if (listener != null) {
                            listener.onTimeout();
                        }
                    } finally {
                        // do the cleanup
                        if (stopProcess != null) {
                            Map<String, String> mark = new HashMap<String, String>();
                            mark.put(STOP_KEY_UUID, uuid);
                            Processes.killTree(stopProcess, mark);
                            stopService(stopService);
                            if (listener != null) {
                                listener.onProcessFinish();
                            }
                        }
                    }
                } finally {
                    if (listener != null) {
                        listener.onExit();
                    }
                }
            }
        });
    }

    public void kill() {
        Process process;
        synchronized (PROCESSES) {
            process = PROCESSES.get(config);
        }
        if (process != null) {
            Map<String, String> mark = new HashMap<String, String>();
            mark.put(START_KEY_UUID, config.getId());
            Processes.killTree(process, mark);
        }
    }

    public boolean isRunning() {
//        Process proc;
//        synchronized (PROCESSES) {
//            proc = PROCESSES.get(config);
//        }
//
//        if (!isRunning(proc)) {
//            return false;
//        }

        String host = config.getHost();
        int port = config.getPort();
        return ping(host, port, CHECK_TIMEOUT); // is server responding?
    }

    public boolean isProcessRunning() {
        Process proc;
        synchronized (PROCESSES) {
            proc = PROCESSES.get(config);
        }

        return isRunning(proc);
    }

    private static void configureEnvironment(Environment environment, Map<String, String> variables) {
        if (variables == null) {
            return;
        }

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            environment.setVariable(entry.getKey(), entry.getValue());
        }
    }

    private static void startService(final ExecutorService service, Process process,
            BaseExecutionDescriptor.InputProcessorFactory outFactory,
            BaseExecutionDescriptor.InputProcessorFactory errFactory) {

        InputProcessor output = null;
        if (outFactory != null) {
            output = outFactory.newInputProcessor();
        }
        InputProcessor error = null;
        if (errFactory != null) {
            error = errFactory.newInputProcessor();
        }

        service.submit(InputReaderTask.newTask(InputReaders.forStream(
                process.getInputStream(), Charset.defaultCharset()), output));
        service.submit(InputReaderTask.newTask(InputReaders.forStream(
                process.getErrorStream(), Charset.defaultCharset()), error));
    }

    private static void stopService(final ExecutorService service) {
        if (service != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                @Override
                public Void run() {
                    service.shutdownNow();
                    return null;
                }
            });
        }
    }

    private static boolean isRunning(Process process) {
        if (process == null) {
            return false;
        }
        try {
            process.exitValue();
            // process is stopped
            return false;
        } catch (IllegalThreadStateException e) {
            // process is running
            return true;
        }
    }

    private static boolean ping(String host, int port, int timeout) {
        if (ping(host, port, timeout, "/console/login/LoginForm.jsp")) {
            return true;
        }
        return ping(host, port, timeout, "/console");
    }

    private static boolean ping(String host, int port, int timeout, String path) {
        // checking whether a socket can be created is not reliable enough, see #47048
        Socket socket = new Socket();
        try {
            try {
                socket.connect(new InetSocketAddress(host, port), timeout); // NOI18N
                socket.setSoTimeout(timeout);
                try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    out.println("GET " + path + " HTTP/1.1\nHost:\n"); // NOI18N
                    String line = in.readLine();
                    return "HTTP/1.1 200 OK".equals(line) // NOI18N
                            || "HTTP/1.1 302 Moved Temporarily".equals(line); // NOI18N
                }
            } finally {
                socket.close();
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.FINE, null, ioe);
            return false;
        }
    }

    public static interface RunningCondition {

        boolean isRunning();

    }

    private static class BlockingListener implements RuntimeListener {

        private final CountDownLatch latch;

        private final AtomicReference<Exception> exception;

        private final boolean waitOnRunning;

        public BlockingListener(CountDownLatch latch, AtomicReference<Exception> exception, boolean waitOnRunning) {
            this.latch = latch;
            this.exception = exception;
            this.waitOnRunning = waitOnRunning;
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        public AtomicReference<Exception> getException() {
            return exception;
        }

        @Override
        public void onStart() {
            // noop
        }

        @Override
        public void onFinish() {
            // noop
        }

        @Override
        public void onFail() {
            // noop
        }

        @Override
        public void onProcessStart() {
            // noop
        }

        @Override
        public void onProcessFinish() {
            // noop
        }

        @Override
        public void onRunning() {
            if (!waitOnRunning) {
                latch.countDown();
            }
        }

        @Override
        public void onTimeout() {
            exception.set(new TimeoutException(TIMEOUT + " ms"));
            latch.countDown();
        }

        @Override
        public void onInterrupted() {
            // XXX is this ok ?
            exception.set(new InterruptedException());
            latch.countDown();
        }

        @Override
        public void onException(Exception ex) {
            exception.set(ex);
            latch.countDown();
        }

        @Override
        public void onExit() {
            latch.countDown();
        }
    }
}
