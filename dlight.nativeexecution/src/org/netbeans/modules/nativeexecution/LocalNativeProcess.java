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
package org.netbeans.modules.nativeexecution;

import com.sun.jna.Pointer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.UnbufferSupport;
import org.netbeans.modules.nativeexecution.support.Win32APISupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class LocalNativeProcess extends AbstractNativeProcess {

    private Process process = null;
    private final CountDownLatch additionalMsgLatch = new CountDownLatch(1);

    public LocalNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    @Override
    protected void create() throws Throwable {
        boolean isWindows = hostInfo.getOSFamily() == OSFamily.WINDOWS;

        try {
            if (isWindows) {
                createWin();
            } else {
                createNonWin();
            }
        } catch (Throwable ex) {
            if (process != null) {
                process.destroy();
            }
            throw ex;
        }
    }

    private void createNonWin() throws IOException, InterruptedException {
        final MacroMap env = info.getEnvironment().clone();

        if (info.isUnbuffer()) {
            UnbufferSupport.initUnbuffer(info.getExecutionEnvironment(), env);
        }

        final ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-s"); // NOI18N

        // Get working directory ....
        String workingDirectory = info.getWorkingDirectory(true);

        if (workingDirectory != null) {
            File wd = new File(workingDirectory);
            if (!wd.exists()) {
                throw new FileNotFoundException(loc("NativeProcess.noSuchDirectoryError.text", wd.getAbsolutePath())); // NOI18N
            }
            pb.directory(wd);
        }

        if (isInterrupted()) {
            throw new InterruptedException();
        }

        process = pb.start();

        OutputStream toProcessStream = process.getOutputStream();
        InputStream fromProcessStream = process.getInputStream();

        setErrorStream(process.getErrorStream());
        setInputStream(fromProcessStream);
        setOutputStream(toProcessStream);

        toProcessStream.write("echo $$\n".getBytes()); // NOI18N
        toProcessStream.flush();

        EnvWriter ew = new EnvWriter(toProcessStream, false);
        ew.write(env);

        if (info.getInitialSuspend()) {
            toProcessStream.write("ITS_TIME_TO_START=\n".getBytes()); // NOI18N
            toProcessStream.write("trap 'ITS_TIME_TO_START=1' CONT\n".getBytes()); // NOI18N
            toProcessStream.write("while [ -z \"$ITS_TIME_TO_START\" ]; do sleep 1; done\n".getBytes()); // NOI18N
        }

        toProcessStream.write(("exec " + info.getCommandLineForShell() + "\n").getBytes()); // NOI18N
        toProcessStream.flush();

        creation_ts = System.nanoTime();

        readPID(fromProcessStream);
    }

    private void createWin() throws IOException, InterruptedException {
        // Don't use shell wrapping on Windows...
        // Mostly this is because exec works not as expected and we cannot
        // control processes started with exec method....

        // Suspend is not supported on Windows.

        final ProcessBuilder pb = new ProcessBuilder(); // NOI18N

        final MacroMap jointEnv = MacroMap.forExecEnv(ExecutionEnvironmentFactory.getLocal());
        jointEnv.putAll(info.getEnvironment());

        if (isInterrupted()) {
            throw new InterruptedException();
        }

        if (info.isUnbuffer()) {
            UnbufferSupport.initUnbuffer(info.getExecutionEnvironment(), jointEnv);
        }

        pb.environment().clear();

        for (Entry<String, String> envEntry : jointEnv.entrySet()) {
            pb.environment().put(envEntry.getKey(), envEntry.getValue());
        }

        pb.command(info.getCommand());

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest(String.format("Command: %s", info.getCommand())); // NOI18N
        }

        String wdir = info.getWorkingDirectory(true);
        if (wdir != null) {
            File wd = new File(wdir);
            if (!wd.exists()) {
                throw new FileNotFoundException(loc("NativeProcess.noSuchDirectoryError.text", wd.getAbsolutePath())); // NOI18N
            }
            pb.directory(wd);
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest(String.format("Working directory: %s", wdir)); // NOI18N
            }
        }

        process = pb.start();

        creation_ts = System.nanoTime();

        setErrorStream(new ErrorStream(process.getErrorStream(), additionalMsgLatch));
        setInputStream(process.getInputStream());
        setOutputStream(process.getOutputStream());

        int newPid = 12345;

        try {
            String className = process.getClass().getName();
            if ("java.lang.Win32Process".equals(className) || "java.lang.ProcessImpl".equals(className)) { // NOI18N
                Field f = process.getClass().getDeclaredField("handle"); // NOI18N
                f.setAccessible(true);
                long phandle = f.getLong(process);

                Win32APISupport kernel = Win32APISupport.instance;
                Win32APISupport.HANDLE handle = new Win32APISupport.HANDLE();
                handle.setPointer(Pointer.createConstant(phandle));
                newPid = kernel.GetProcessId(handle);
            }
        } catch (Throwable e) {
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(("" + newPid).getBytes()); // NOI18N

        readPID(bis);
    }

    @Override
    public final int waitResult() throws InterruptedException {
        if (process == null) {
            return -1;
        }

        try {
            int exitcode = process.waitFor();

            /*
             * Bug 179555 - Qt application fails to run in case of default qt sdk installation
             */

            if (exitcode == -1073741515 && Utilities.isWindows()) {
                // This means Initialization error. May be the reason is that no required dll found
                StringBuilder cmd = new StringBuilder();
                for (String s : info.getCommand()) {
                    cmd.append(s).append(' ');
                }

                String errorMsg = loc("LocalNativeProcess.windowsProcessStartFailed.1073741515.text", cmd.toString()); // NOI18N
                if (info.isPtyMode()) {
                    errorMsg = errorMsg.replaceAll("\n", "\n\r"); // NOI18N
                }

                ((ErrorStream) getErrorStream()).addErrorMessage(errorMsg);
            }
            return exitcode;
        } finally {
            additionalMsgLatch.countDown();
        }
    }

    @Override
    protected final synchronized void cancel() {
        if (process != null) {
            ProcessUtils.destroy(process);
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(LocalNativeProcess.class, key, params);
    }

    private static class ErrorStream extends InputStream {

        private final InputStream orig;
        private final CountDownLatch additionalMsgLatch;
        private transient ByteArrayInputStream additionalMsg = null;

        public ErrorStream(InputStream orig, CountDownLatch additionalMsgLatch) {
            this.orig = orig;
            this.additionalMsgLatch = additionalMsgLatch;
        }

        @Override
        public int read() throws IOException {
            int c = orig.read();

            if (c < 0) {
                try {
                    additionalMsgLatch.await();
                } catch (InterruptedException ex) {
                }
            }

            if (additionalMsg != null && c < 0) {
                c = additionalMsg.read();
            }

            return c;
        }

        private void addErrorMessage(String additionalMsg) {
            this.additionalMsg = new ByteArrayInputStream(additionalMsg.getBytes());
        }
    }
}
