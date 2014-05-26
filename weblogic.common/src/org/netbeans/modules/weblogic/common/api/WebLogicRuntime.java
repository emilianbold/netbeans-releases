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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.base.BaseExecutionDescriptor;
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

    private static final String KEY_UUID = "NB_EXEC_WL_START_PROCESS_UUID"; //NOI18N

    private static final int TIMEOUT = 300000;

    private static final int DELAY = 1000;

    private static final int CHECK_TIMEOUT = 10000;

    //@GuardedBy(INSTANCES)
    private static final Map<WebLogicConfiguration, Process> INSTANCES = new HashMap<WebLogicConfiguration, Process>();

    private final WebLogicConfiguration config;

    private WebLogicRuntime(WebLogicConfiguration config) {
        this.config = config;
    }

    @NonNull
    public static WebLogicRuntime getInstance(@NonNull WebLogicConfiguration config) {
        return new WebLogicRuntime(config);
    }

    public boolean start(@NullAllowed final BaseExecutionDescriptor.InputProcessorFactory outFactory,
            @NullAllowed final BaseExecutionDescriptor.InputProcessorFactory errFactory,
            @NullAllowed final RuntimeListener listener) throws InterruptedException {

        if (listener != null) {
            listener.onStart();
        }

        if (config.isRemote() || isRunning()) {
            return true;
        }

        final AtomicBoolean result = new AtomicBoolean();
        final CountDownLatch latch = new CountDownLatch(1);

        RUNTIME_RP.submit(new Runnable() {

            @Override
            public void run() {
                File domainHome = config.getDomainHome();
                if (!domainHome.exists() || !domainHome.isDirectory()) {
                    if (listener != null) {
                        listener.onFail();
                    }
                    latch.countDown();
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

                org.netbeans.api.extexecution.base.ProcessBuilder builder = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
                builder.setExecutable(startup.getAbsolutePath());
                builder.setWorkingDirectory(domainHome.getAbsolutePath());
                builder.getEnvironment().setVariable(KEY_UUID, config.getId());

                File mwHome = config.getLayout().getMiddlewareHome();
                if (mwHome != null) {
                    builder.getEnvironment().setVariable("MW_HOME", mwHome.getAbsolutePath()); // NOI18N
                }

                //builder = initBuilder(builder);

                Process process;
                try {
                    process = builder.call();
                } catch (IOException ex) {
                    if (listener != null) {
                        listener.onException(ex);
                    }
                    latch.countDown();
                    return;
                }
                synchronized (INSTANCES) {
                    INSTANCES.put(config, process);
                }

                if (listener != null) {
                    listener.onProcessStart();
                }

                ExecutorService service = Executors.newFixedThreadPool(2);
                startService(service, process, outFactory, errFactory);

                while ((System.currentTimeMillis() - start) < TIMEOUT) {
                    if (isRunning()) {
                        result.set(true);
                        latch.countDown();

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
                                listener.onFinish();
                            }
                        }
                        // just to be sure
                        latch.countDown();
                        return;
                    }
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        if (listener != null) {
                            listener.onInterrupted();
                        }
                        Thread.currentThread().interrupt();
                        latch.countDown();
                        return;
                    }
                }

                // timeouted
                if (listener != null) {
                    listener.onTimeout();
                }
                latch.countDown();
            }
        });

        latch.await();
        return result.get();
    }

    @NonNull
    public Future<Void> stop() {
        return null;
    }

    public boolean isRunning() {
        Process proc = null;
        synchronized (INSTANCES) {
            proc = INSTANCES.get(config);
        }

        if (!isRunning(proc)) {
            return false;
        }

        String host = config.getHost();
        int port = config.getPort();
        return ping(host, port, CHECK_TIMEOUT); // is server responding?
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

    private static boolean ping(String host, int port, int timeout) {
        if (pingPath(host, port, timeout, "/console/login/LoginForm.jsp")) {
            return true;
        }
        return pingPath(host, port, timeout, "/console");
    }

    private static boolean pingPath(String host, int port, int timeout, String path) {
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
                    return "HTTP/1.1 200 OK".equals(line)
                            || "HTTP/1.1 302 Moved Temporarily".equals(line);
                }
            } finally {
                socket.close();
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.FINE, null, ioe);
            return false;
        }
    }
}
