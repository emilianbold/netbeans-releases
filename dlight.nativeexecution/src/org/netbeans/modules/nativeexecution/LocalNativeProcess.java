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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.support.MacroMap;
import org.netbeans.modules.nativeexecution.support.UnbufferSupport;
import org.openide.util.NbBundle;

public final class LocalNativeProcess extends AbstractNativeProcess {

    private Process process = null;
    private InputStream processOutput = null;
    private OutputStream processInput = null;
    private InputStream processError = null;

    public LocalNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    protected void create() throws Throwable {
        boolean isWindows = hostInfo.getOSFamily() == OSFamily.WINDOWS;

        try {
            if (isWindows) {
                createWin();
            } else {
                createNonWin();
            }
        } catch (Throwable ex) {
            String msg = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            processOutput = new ByteArrayInputStream(new byte[0]);
            processError = new ByteArrayInputStream(msg.getBytes());
            processInput = new ByteArrayOutputStream();
            throw ex;
        }
    }

    private void createNonWin() throws IOException, InterruptedException {
        // Get working directory ....
        String workingDirectory = info.getWorkingDirectory(true);

        if (workingDirectory != null) {
            workingDirectory = new File(workingDirectory).getAbsolutePath();
        }

        final MacroMap env = info.getEnvVariables();

        UnbufferSupport.initUnbuffer(info, env);

        // Always prepend /bin and /usr/bin to PATH
        env.put("PATH", "/bin:/usr/bin:${PATH}"); // NOI18N

        final ProcessBuilder pb = new ProcessBuilder(hostInfo.getShell(), "-s"); // NOI18N

        if (isInterrupted()) {
            throw new InterruptedException();
        }

        process = pb.start();

        processInput = process.getOutputStream();
        processError = process.getErrorStream();
        processOutput = process.getInputStream();

        processInput.write("echo $$\n".getBytes()); // NOI18N
        processInput.flush();

        EnvWriter ew = new EnvWriter(processInput);
        ew.write(env);

        if (workingDirectory != null) {
            processInput.write(("cd \"" + workingDirectory + "\"\n").getBytes()); // NOI18N
        }

        String cmd = "exec " + info.getCommandLineForShell() + "\n"; // NOI18N

        processInput.write(cmd.getBytes());
        processInput.flush();

        readPID(processOutput);
    }

    private void createWin() throws IOException, InterruptedException {
        // Don't use shell wrapping on Windows...
        // Mostly this is because exec works not as expected and we cannot
        // control processes started with exec method....

        final MacroMap env = info.getEnvVariables();
        final ProcessBuilder pb = new ProcessBuilder(); // NOI18N

        if (isInterrupted()) {
            throw new InterruptedException();
        }

        UnbufferSupport.initUnbuffer(info, env);

        pb.command(info.getCommand());
        LOG.log(Level.FINEST, "Command: {0}", info.getCommand());


        String val = null;

        if (!env.isEmpty()) {
            for (String var : env.keySet()) {
                val = env.get(var);
                if (val != null) {
                    pb.environment().put(var, val);
                    LOG.log(Level.FINEST, "Environment: {0}={1}", new Object[]{var, val});
                }
            }
        }

        String wdir = info.getWorkingDirectory(true);
        if (wdir != null) {
            File wd = new File(wdir);
            if (wd.exists()) {
                pb.directory(wd);
                LOG.log(Level.FINEST, "Working directory: {0}", wdir);
            }
        }

        process = pb.start();

        processInput = process.getOutputStream();
        processError = process.getErrorStream();
        processOutput = process.getInputStream();

        // Fake PID...
        ByteArrayInputStream bis = new ByteArrayInputStream("12345".getBytes()); // NOI18N
        readPID(bis);
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
        if (process == null) {
            throw new InterruptedException();
        }

        /*
         * Why not just process.waitResult()...
         * This is to avoid a problem with short-running tasks, when
         * this Thread (that waits for process' termination) doesn't see
         * that it has been interrupted....
         * TODO: describe situation in details... 
         */

        int result = -1;

//        // Get lock on process not to take it on every itteration
//        // (in process.exitValue())
//
//        synchronized (process) {
        // Why this synchronized is commented-out..
        // This is because ProcessReaper is also synchronized on this...
        // And it should be able to react on process' termination....

        while (true) {
            // This sleep is to avoid lost interrupted exception...
            try {
                Thread.sleep(200);
            // 200 - to make this check not so often...
            // actually, to avoid the problem, 1 is OK.
            } catch (InterruptedException ex) {
                throw ex;
            }

            try {
                result = process.exitValue();
            } catch (IllegalThreadStateException ex) {
                continue;
            }

            break;
        }
//        }

        return result;
    }

    @Override
    protected final synchronized void cancel() {
        if (process != null) {
            process.destroy();
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(LocalNativeProcess.class, key, params);
    }
}
