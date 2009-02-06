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
package org.netbeans.modules.nativeexecution.api.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.nativeexecution.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public final class TerminalLocalNativeProcess extends NativeProcess {

    private final static String dorunScript;
    private final static Integer TIMEOUT_TO_USE =
            Integer.valueOf(System.getProperty(
            "dlight.localnativeexecutor.timeout", "3")); // NOI18N
    private final InputStream processOutput;
    private final InputStream processError;
    private final OutputStream processInput;
    private static final Runtime rt = Runtime.getRuntime();
    private final Integer pid;
    private final File pidFile;
    private final Process process;
//    private final PipedInputStream pin = new PipedInputStream();
//    private final PipedOutputStream pout = new PipedOutputStream(pin);


    static {
        InstalledFileLocator fl = InstalledFileLocator.getDefault();
        File file = fl.locate("bin/dorun.sh", null, false); // NOI18N
        dorunScript = file.toString();
    }

    public TerminalLocalNativeProcess(final ExternalTerminal t,
            final NativeProcessInfo info) throws IOException {
        ExternalTerminal terminal = t;
        Integer ppid = null;

        final NativeProcessAccessor processInfo =
                NativeProcessAccessor.getDefault();

        processInfo.setListeners(this, info.getListeners());

        final String commandLine = info.getCommandLine();

        pidFile = File.createTempFile("termexec", "pid"); // NOI18N
        pidFile.deleteOnExit();

        String pidFileName = pidFile.toString();

        processInfo.setID(this, commandLine);

        final ExternalTerminalAccessor terminalInfo =
                ExternalTerminalAccessor.getDefault();

        if (terminalInfo.getTitle(terminal) == null) {
            terminal = terminal.setTitle(commandLine);
        }

        List<String> command = terminalInfo.wrapCommand(terminal,
                dorunScript,
                "-p", pidFileName, // NOI18N
                "-x", terminalInfo.getPrompt(terminal), // NOI18N
                commandLine);

        synchronized (rt) {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.environment().putAll(info.getEnvVariables());

            final String wdir = info.getWorkingDirectory();

            if (wdir != null) {
                pb.directory(new File(wdir));
            }

            process = pb.start();

            processOutput = new ByteArrayInputStream(new byte[0]);
            processError = new ByteArrayInputStream(new byte[0]);
//            processError = pin;
            processInput = null;

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
//        pout.write(loc("TerminalLocalNativeProcess.JobStarted.text").getBytes()); // NOI18N
        processInfo.setState(this, State.RUNNING);
    }

    @Override
    public int getPID() {
        if (pid == null) {
            throw new IllegalStateException();
        }
        return pid.intValue();
    }

    @Override
    protected void cancel() {
        try {
            rt.exec("/bin/kill -9 " + pid);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected int waitResult() throws InterruptedException {
        File f = new File("/proc/" + pid); // NOI18N

        while (f.exists()) {
            Thread.sleep(300);
        }

        try {
            processError.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        try {
            processOutput.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return 999;
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

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(TerminalLocalNativeProcess.class, key, params);
    }

    private class PIDReader implements Callable<Integer> {

        public Integer call() throws Exception {
            BufferedReader in = new BufferedReader(new FileReader(pidFile));
            String pidLine = "-1";

            while (true) {
                pidLine = in.readLine();
                if (pidLine != null) {
                    break;
                }
            }

            return new Integer(pidLine);
        }
    }
}
