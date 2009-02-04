/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.support;

import org.netbeans.modules.nativeexecution.api.NativeTaskConfig;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.nativeexecution.access.NativeProcessAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionControl;
import org.netbeans.modules.nativeexecution.access.NativeTaskConfigAccessor;
import org.openide.util.Exceptions;

/**
 *
 */
public final class LocalNativeProcess extends NativeProcess {

    private final InputStream processOutput;
    private final InputStream processError;
    private final OutputStream processInput;
    private static final Runtime rt = Runtime.getRuntime();
    private final Integer TIMEOUT_TO_USE = Integer.valueOf(System.getProperty(
            "dlight.localnativeexecutor.timeout", "3")); // NOI18N
    private final Integer pid;
    private final Process process;

    public LocalNativeProcess(NativeTaskConfig cfg,
            ExecutionControl executionControl) throws IOException {

        super(executionControl);

        final NativeProcessAccessor processInfo =
                NativeProcessAccessor.getDefault();

        processInfo.setState(this, State.STARTING);

        Integer ppid = null;

        final NativeTaskConfigAccessor taskInfo =
                NativeTaskConfigAccessor.getDefault();

        final String commandLine = taskInfo.getCommandLine(cfg);

        final String workingDirectory = taskInfo.getWorkingDirectory(cfg);
        final File wdir =
                workingDirectory == null ? null : new File(workingDirectory);

        final Map<String, String> envVars = taskInfo.getEnvVariables(cfg);
        final ArrayList<String> envArr = new ArrayList<String>();

        final ArrayList<String> command = new ArrayList<String>();

        if (!envVars.isEmpty()) {
            for (String var : envVars.keySet()) {
                envArr.add(var + "=" + envVars.get(var)); // NOI18N
            }
        }

        command.add("/bin/sh"); // NOI18N
        command.add("-c"); // NOI18N
        command.add("/bin/echo $$ && exec " + commandLine); // NOI18N

        processInfo.setID(this, commandLine);

        synchronized (rt) {
            process = rt.exec(
                    command.toArray(new String[0]),
                    envArr.toArray(new String[0]),
                    wdir);

            processOutput = process.getInputStream();
            processError = process.getErrorStream();
            processInput = process.getOutputStream();

            /*
             * For some reason sometimes I hang here... (on reading first
             * output line...)
             * That is why I'm doing this in a separate thread and wait for some
             * timeout
             */

            Future<Integer> pidReaderTask =
                    NativeTaskExecutorService.submit(new PIDReader());

            try {
                ppid = pidReaderTask.get(TIMEOUT_TO_USE, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            } catch (TimeoutException ex) {
                // was not able to submit PID in time...
                // Terminate pid reader ...
                pidReaderTask.cancel(true);
            }
        }

        pid = ppid;

        processInfo.setState(this, State.RUNNING);
    }

    @Override
    public int getPID() {
        if (pid == null) {
            throw new IllegalStateException("Process was not started"); // NOI18N
        }

        return pid.intValue();
    }

    @Override
    public OutputStream getOutputStream() {
        return processInput;
    }

    @Override
    public InputStream getInputStream() {
        return processOutput;
    }

    @Override
    public InputStream getErrorStream() {
        return processError;
    }

    @Override
    public final int waitResult() throws InterruptedException {
        int result = process.waitFor();
//        // TODO: How to wait for all buffers?
        Thread.yield();
        return result;
    }

    @Override
    public void cancel() {
        process.destroy();
    }

    private class PIDReader implements Callable<Integer> {

        public Integer call() throws Exception {
            Integer pid = null;

            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(processOutput));
                String pidLine = br.readLine().trim();
                pid = new Integer(pidLine);
            } catch (NumberFormatException e) {
            } catch (Exception e) {
                // Not interested in these exceptions....
            }

            return pid;
        }
    }
}
