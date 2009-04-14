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

package org.netbeans.modules.groovy.grails;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.InputReaderTask;
import org.netbeans.api.extexecution.input.InputReaders;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public class KillableProcess extends Process {

    private static final Logger LOGGER = Logger.getLogger(KillableProcess.class.getName());

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static final long TIMEOUT = 5000;

    private static final long CLEANUP_TIMEOUT = 3000;

    private final Process nativeProcess;

    private final String remark;

    private final String command;

    public KillableProcess(Process nativeProcess, String command, String remark) {
        this.nativeProcess = nativeProcess;
        this.command = command;
        this.remark = remark;
    }

    @Override
    public void destroy() {
        if (!Utilities.isWindows()) {
            LOGGER.log(Level.FINEST, "Not windows - normal exit");
            nativeProcess.destroy();
            return;
        }

        int pid = -1;

        ExternalProcessBuilder builder = new ExternalProcessBuilder("wmic.exe") // NOI18N
                .redirectErrorStream(true).addArgument("process") // NOI18N
                .addArgument("where").addArgument("name=\"cmd.exe\"") // NOI18N
                .addArgument("get").addArgument("processid,commandline"); // NOI18N

        CountDownLatch latch = new CountDownLatch(1);
        PidLineProcessor pidProcessor = new PidLineProcessor(latch, command, remark);

        LOGGER.log(Level.FINEST, "About to run wmic.exe");

        Future<Integer> task = EXECUTOR_SERVICE.submit(new ExecutionCallable(builder, pidProcessor));
        try {
            Integer retValue = task.get(TIMEOUT, TimeUnit.MILLISECONDS);
            if (retValue != null && retValue.intValue() == 0) {
                latch.await(CLEANUP_TIMEOUT, TimeUnit.MILLISECONDS);
                pid = pidProcessor.getPid();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (TimeoutException ex) {
            LOGGER.log(Level.INFO, null, ex);
            task.cancel(true);
        }

        if (pid < 0) {
            LOGGER.log(Level.FINEST, "No pid acquired - normal exit");
            nativeProcess.destroy();
            return;
        }

        builder = new ExternalProcessBuilder("taskkill.exe"); // NOI18N
        builder = builder.redirectErrorStream(true).addArgument("/F") // NOI18N
                .addArgument("/PID").addArgument(Integer.toString(pid)).addArgument("/T"); // NOI18N

        LOGGER.log(Level.FINEST, "About to run taskkill.exe with pid {0}", pid);

        EXECUTOR_SERVICE.submit(new ExecutionCallable(builder, null));
    }

    @Override
    public InputStream getErrorStream() {
        return nativeProcess.getErrorStream();
    }

    @Override
    public InputStream getInputStream() {
        return nativeProcess.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return nativeProcess.getOutputStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return nativeProcess.waitFor();
    }

    @Override
    public int exitValue() {
        return nativeProcess.exitValue();
    }

    // package for tests
    static class PidLineProcessor implements LineProcessor {

        private final CountDownLatch latch;

        private final Pattern pattern;

        private AtomicInteger pid = new AtomicInteger(-1);

        public PidLineProcessor(CountDownLatch latch, String command, String remark) {
            this.latch = latch;
            // grails.bat["] parameters command mark
            pattern = Pattern.compile("^.*grails.bat\"?(\\s+.*)*\\s+" // NOI18N
                    + Pattern.quote(command) + "\\s+REM NB:" + Pattern.quote(remark) // NOI18n
                    + ".*\\s+(\\d+)(\\s+.*)?$"); // NOI18N
        }

        public void processLine(String line) {
            LOGGER.log(Level.FINEST, "WMIC output line {0}", line);

            Matcher matcher = pattern.matcher(line);
            try {
                if (matcher.matches()) {
                    pid.set(Integer.parseInt(matcher.group(2)));
                    latch.countDown();
                }
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        public void close() {
            LOGGER.log(Level.FINEST, "WMIC processor closed");
            latch.countDown();
        }

        public void reset() {
            // noop
        }

        public int getPid() {
            return pid.get();
        }
    }

    private static class ExecutionCallable implements Callable<Integer> {

        private final Callable<Process> builder;

        private final LineProcessor processor;

        public ExecutionCallable(Callable<Process> builder, LineProcessor processor) {
            this.builder = builder;
            this.processor = processor;
        }

        public Integer call() throws Exception {
            boolean interrupted = false;

            Process process = builder.call();
            // troubles on XP if this is omitted
            process.getOutputStream().close();

            ProcessInputStream is = new ProcessInputStream(process, process.getInputStream());
            InputReaderTask task = InputReaderTask.newDrainingTask(
                    InputReaders.forStream(is, Charset.defaultCharset()),
                    processor != null ? InputProcessors.bridge(processor) : null);

            EXECUTOR_SERVICE.submit(task);

            try {
                process.waitFor();
            } catch (InterruptedException ex) {
                interrupted = true;
            } finally {
                try {
                    interrupted = interrupted | Thread.interrupted();

                    if (!interrupted) {
                        if (is != null) {
                            is.close(true);
                        }
                    }

                    if (process != null) {
                        process.destroy();

                        try {
                            return process.exitValue();
                        } catch (IllegalThreadStateException ex) {
                            // noop
                        }
                    }
                } finally {
                    if (task != null) {
                        task.cancel();
                    }
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            return null;
        }
    }
}
