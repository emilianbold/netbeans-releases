/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.support;

import org.netbeans.modules.nativeexecution.util.HostNotConnectedException;
import org.netbeans.modules.nativeexecution.api.NativeTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.nativeexecution.util.HostInfo;
import org.openide.util.Exceptions;

public final class LocalNativeExecutor extends NativeExecutor {

    private final Integer TIMEOUT_TO_USE = Integer.valueOf(System.getProperty(
            "dlight.localnativeexecutor.timeout", "3")); // NOI18N
    private static final Runtime rt = Runtime.getRuntime();
    private Process process;
    private InputStream processOutput;
    private InputStream processError;
    private OutputStream processInput;

    // A temporary hack to set environment.
    // TODO: remove as soon as setting environment becomes possible
    private static final String[] ENV;
    static {
        String env = System.getProperty("dlight.env");
        if (env != null) {
            ENV = new String[] { env };
        } else {
            ENV = new String[] {};
        }
    }

    public LocalNativeExecutor(NativeTask task) {
        super(task);
    }

    public boolean cancel() {
        if (process == null) {
            /*
             * This should not normally happen...
             * this means that user initiated cancel BEFORE process started.
             * Mainly UI should not allow to initiate Cancel before
             * process start...
             * Still, this indicates another problem - that, for some reason, we
             * cannot get PID for a long period of time....
             * See commants below...
             */

            return true;
        }

        /*
         * Close all streams...
         */

        try {
            if (processOutput != null) {
                processOutput.close();
            }
        } catch (IOException ex) {
        }
        try {
            if (processInput != null) {
                processInput.close();
            }
        } catch (IOException ex) {
        }
        try {
            if (processError != null) {
                processError.close();
            }
        } catch (IOException ex) {
        }

        /*
         * ... and try to destroy the process.
         */

        process.destroy();

        /*
         * PROBLEM:
         * I have faced with the situation that sometimes I cannot terminate the
         * process (process.terminate() has no effect)...
         *
         * In this situation command 'kill $pid' issues from a terminal has no
         * effect as well and an attempt to, say, run pstack on the pid failes
         * with message like
         *
         * pstack: cannot examine XXXXX: process is traced
         *
         * kill -9 helps.
         *
         * So, on Linux/Solaris I'm trying to send SIGKILL to the process.
         */

        boolean isUnix = false;

        try {
            isUnix = HostInfo.isUnix(task.getExecutionEnvironment());
        } catch (HostNotConnectedException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (isUnix) {
            Callable<Boolean> processWaiter = new Callable<Boolean>() {

                public Boolean call() {
                    Boolean ok = true;

                    try {
                        /*
                         * We are also waiting on another thread...
                         * But this is not a problem ...
                         */
                        process.waitFor();
                    } catch (InterruptedException ex) {
                        ok = false;
                    }

                    return ok;
                }
            };

            Future<Boolean> waitTask =
                    NativeTaskExecutorService.submit(processWaiter);

            Boolean processTerminated = true;

            try {
                waitTask.get(TIMEOUT_TO_USE, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            } catch (TimeoutException ex) {
                // expected ...
                // it is an indication that process.terminate() didn't succeed
                processTerminated = false;
                // terminate the task ...
                waitTask.cancel(true);
            }

            if (!processTerminated) {
                log.fine("Unable to terminate NativeTask (" + task + // NOI18N
                        ")- have to kill it"); // NOI18N

                // So... Do one more attempt using 'kill -9'...
                NativeTaskSupport.kill(
                        task.getExecutionEnvironment(), 9, task.getPID());
            }
        }
        return true;
    }

    protected int doInvoke() throws Exception {
        int pid = -1;

        synchronized (rt) {
            process = rt.exec(new String[]{
                        "/bin/sh", "-c", // NOI18N
                        "/bin/echo $$; exec " + task.getCommand()}); // NOI18N
            processOutput = process.getInputStream();
            processError = process.getErrorStream();
            processInput = process.getOutputStream();

            /*
             * For some reason sometimes I hang here... (on reading first
             * output line...)
             * That is why I'm doing this in a separate thread and wait for some
             * timeout
             */

            Callable<Integer> pidReader = new Callable<Integer>() {

                public Integer call() throws Exception {
                    String pidLine = "-1"; // NOI18N
                    try {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(processOutput));
                        pidLine = br.readLine().trim();
                    } catch (Exception e) {
                        // Not interested in these exceptions....
                    }

                    return Integer.valueOf(pidLine);
                }
            };

            Future<Integer> pidReaderTask =
                    NativeTaskExecutorService.submit(pidReader);

            try {
                pid = pidReaderTask.get(TIMEOUT_TO_USE, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            } catch (TimeoutException ex) {
                // was not able to get PID in time...
                // Terminate pid reader ...
                pidReaderTask.cancel(true);
            }
        }

        return pid;
    }

    public final InputStream getTaskInputStream() throws IOException {
        return processOutput;
    }

    public final InputStream getTaskErrorStream() throws IOException {
        return processError;
    }

    public final OutputStream getTaskOutputStream() throws IOException {
        return processInput;
    }

    @Override
    protected final Integer doGet() {
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        return process.exitValue();
    }
}
