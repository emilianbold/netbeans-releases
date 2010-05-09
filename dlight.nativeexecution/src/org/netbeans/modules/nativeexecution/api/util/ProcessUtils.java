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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.openide.util.Exceptions;

public final class ProcessUtils {

    private ProcessUtils() {
    }
    
    private final static String remoteCharSet = System.getProperty("cnd.remote.charset", "UTF-8"); // NOI18N
    public static String getRemoteCharSet() {
        return remoteCharSet;
    }

    public static BufferedReader getReader(final InputStream is, boolean remote) {
        if (remote) {
            // set charset
            try {
                return new BufferedReader(new InputStreamReader(is, getRemoteCharSet()));
            } catch (UnsupportedEncodingException ex) {
                String msg = getRemoteCharSet() + " encoding is not supported, try to override it with cnd.remote.charset"; //NOI18N
                Exceptions.printStackTrace(new IllegalStateException(msg, ex));
            }
        }
        return new BufferedReader(new InputStreamReader(is));
    }

    public static PrintWriter getWriter(final OutputStream os, boolean remote) {
        if (remote) {
            // set charset
            try {
                return new PrintWriter(new OutputStreamWriter(os, getRemoteCharSet()));
            } catch (UnsupportedEncodingException ex) {
                String msg = getRemoteCharSet() + " encoding is not supported, try to override it with cnd.remote.charset"; //NOI18N
                Exceptions.printStackTrace(new IllegalStateException(msg, ex));
            }
        }
        return new PrintWriter(os);
    }

    public static List<String> readProcessError(final Process p) throws IOException {
        if (p == null) {
            return Collections.<String>emptyList();
        }

        return readProcessStream(p.getErrorStream(), isRemote(p));
    }

    public static String readProcessErrorLine(final Process p) throws IOException {
        if (p == null) {
            return ""; // NOI18N
        }

        return readProcessStreamLine(p.getErrorStream(), isRemote(p));
    }

    public static List<String> readProcessOutput(final Process p) throws IOException {
        if (p == null) {
            return Collections.<String>emptyList();
        }

        return readProcessStream(p.getInputStream(), isRemote(p));
    }

    public static String readProcessOutputLine(final Process p) throws IOException {
        if (p == null) {
            return ""; // NOI18N
        }

        return readProcessStreamLine(p.getInputStream(), isRemote(p));
    }

    private static boolean isRemote(Process process) {
        if (process instanceof NativeProcess) {
            return ((NativeProcess)process).getExecutionEnvironment().isRemote();
        }
        return false;
    }

    public static void logError(final Level logLevel, final Logger log, final Process p) throws IOException {
        if (log == null || !log.isLoggable(logLevel)) {
            return;
        }
        List<String> err = readProcessError(p);
        for (String line : err) {
            log.log(logLevel, "ERROR: " + line); // NOI18N
        }
    }

    private static List<String> readProcessStream(final InputStream stream, boolean remoteStream) throws IOException {
        if (stream == null) {
            return Collections.<String>emptyList();
        }

        final List<String> result = new LinkedList<String>();
        final BufferedReader br = getReader(stream, remoteStream);

        try {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return result;
    }

    private static String readProcessStreamLine(final InputStream stream, boolean remoteStream) throws IOException {
        if (stream == null) {
            return ""; // NOI18N
        }

        final StringBuilder result = new StringBuilder();
        final BufferedReader br = getReader(stream, remoteStream);

        try {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (!first) {
                    result.append('\n');
                }
                result.append(line);
                first = false;
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return result.toString();
    }

    public static void writeError(Writer error, Process p) throws IOException {
        List<String> err = readProcessError(p);
        for (String line : err) {
            error.write(line);
        }
    }

    /**
     * This method tries to destroy the process in two attempts. First attempt
     * is simply calling process' destroy() method. But in some cases this could
     * fail to terminate the process - so in case first attempt fails, send
     * SIGKILL to the process.
     *
     * @param process - process to terminate (not necessarily NativeProcess)
     */
    public static void destroy(Process process) {
        // First attempt is just call destroy() on the process
        process.destroy();

        // But in case the process is in system call (sleep, read, for example)
        // this will not have a desired effect - in this case
        // will send SIGTERM signal..

        try {
            process.exitValue();
            // No exception means successful termination
            return;
        } catch (java.lang.IllegalThreadStateException ex) {
        }

        ExecutionEnvironment execEnv;

        if (process instanceof NativeProcess) {
            execEnv = ((NativeProcess) process).getExecutionEnvironment();
        } else {
            execEnv = ExecutionEnvironmentFactory.getLocal();
        }

        int pid = getPID(process);

        if (pid > 0) {
            try {
                CommonTasksSupport.sendSignal(execEnv, pid, Signal.SIGKILL, null).get();
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
            }
        }
    }

    private static int getPID(Process process) {
        int pid = -1;

        try {
            if (process instanceof NativeProcess) {
                pid = ((NativeProcess) process).getPID();
            } else {
                String className = process.getClass().getName();
                // TODO: windows?...
                if ("java.lang.UNIXProcess".equals(className)) { // NOI18N
                    Field f = process.getClass().getDeclaredField("pid"); // NOI18N
                    f.setAccessible(true);
                    pid = f.getInt(process);
                }
            }
        } catch (Throwable e) {
        }

        return pid;
    }

    public static ExitStatus execute(final ExecutionEnvironment execEnv, final String executable, final String... args) {
        return execute(NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable(executable).setArguments(args));
    }

    public static ExitStatus executeInDir(final String workingDir, final ExecutionEnvironment execEnv,  final String executable, final String... args) {
        return execute(NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable(executable).setArguments(args).setWorkingDirectory(workingDir));
    }

    public static ExitStatus executeWithoutMacroExpansion(final String workingDir, final ExecutionEnvironment execEnv, final String executable, final String... args) {
        if (workingDir != null) {
            return execute(NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable(executable).setArguments(args).setMacroExpansion(false));
        } else {
            return execute(NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable(executable).setArguments(args).setWorkingDirectory(workingDir).setMacroExpansion(false));
        }
    }

    /**
     * This method can be used to start a process without additional handling
     * of exceptions/streams reading, etc.
     *
     * Usage pattern:
     *        ExitStatus status = ProcessUtils.execute(
     *            NativeProcessBuilder.newProcessBuilder(execEnv).
     *            setExecutable("/bin/ls").setArguments("/home"));
     * 
     *        if (status.isOK()) {
     *            do something...
     *        } else {
     *            System.out.println("Error! " + status.error);
     *        }
     *
     * This method WILL modify passed ProcessBuilder:
     *   - X11 forwarding will be switched off
     *   - initial suspend will be switched off
     *   - unbuffering will be switched off
     *   - usage of external terminal will be switched off
     * 
     * @param processBuilder
     * @return
     */
    private static ExitStatus execute(final NativeProcessBuilder processBuilder) {
        ExitStatus result;
        Future<String> error;
        Future<String> output;

        if (processBuilder == null) {
            throw new NullPointerException("NULL process builder!"); // NOI18N
        }

        processBuilder.setX11Forwarding(false);
        processBuilder.setInitialSuspend(false);
        processBuilder.unbufferOutput(false);
        processBuilder.useExternalTerminal(null);

        try {
            final Process process = processBuilder.call();
            error = NativeTaskExecutorService.submit(new Callable<String>() {

                public String call() throws Exception {
                    return readProcessErrorLine(process);
                }
            }, "e"); // NOI18N
            output = NativeTaskExecutorService.submit(new Callable<String>() {

                public String call() throws Exception {
                    return readProcessOutputLine(process);
                }
            }, "o"); // NOI18N

            result = new ExitStatus(process.waitFor(), output.get(), error.get());
        } catch (Throwable th) {
            result = new ExitStatus(-100, "", th.getMessage());
        }

        return result;
    }

    public static final class ExitStatus {

        public final int exitCode;
        public final String error;
        public final String output;

        private ExitStatus(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.error = error;
            this.output = output;
        }

        public boolean isOK() {
            return exitCode == 0;
        }
    }
}
