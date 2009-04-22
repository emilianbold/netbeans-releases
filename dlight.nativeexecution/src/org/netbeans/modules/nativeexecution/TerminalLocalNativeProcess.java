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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.MacroMap;
import org.netbeans.modules.nativeexecution.support.WindowsSupport;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public final class TerminalLocalNativeProcess extends AbstractNativeProcess {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final static File dorunScript;
    private ExternalTerminal terminal;
    private InputStream processOutput;
    private InputStream processError;
    private File resultFile;
    private final boolean isWindows;
    private final boolean isMacOS;

    static {
        InstalledFileLocator fl = InstalledFileLocator.getDefault();
        dorunScript = fl.locate("bin/nativeexecution/dorun.sh", null, false); // NOI18N

        if (dorunScript != null) {
            CommonTasksSupport.chmod(ExecutionEnvironmentFactory.getLocal(),
                    dorunScript.getAbsolutePath(), 0755, null);
        } else {
            log.severe("Unable to locate bin/nativeexecution/dorun.sh file!"); // NOI18N
        }
    }

    public TerminalLocalNativeProcess(
            final NativeProcessInfo info, final ExternalTerminal terminal) {
        super(info);
        this.terminal = terminal;
        this.processOutput = new ByteArrayInputStream(
                (loc("TerminalLocalNativeProcess.ProcessStarted.text") + '\n').getBytes()); // NOI18N
        
        isWindows = hostInfo.getOSFamily() == OSFamily.WINDOWS;
        isMacOS = hostInfo.getOSFamily() == OSFamily.MACOSX;

    }

    protected void create() throws Throwable {
        try {
            if (dorunScript == null) {
                throw new IOException(loc("TerminalLocalNativeProcess.dorunNotFound.text")); // NOI18N
            }

            if (isWindows && hostInfo.getShell() == null) {
                throw new IOException(loc("NativeProcess.shellNotFound.text")); // NOI18N
            }

            final String commandLine = info.getCommandLine();
            String wDir = info.getWorkingDirectory(true);

            String workingDirectory = wDir;

            if (isWindows || workingDirectory == null) {
                workingDirectory = "."; // NOI18N
            }

            File pidFile = File.createTempFile("dlight", "termexec"); // NOI18N
            pidFile.deleteOnExit();

            resultFile = new File(pidFile.getAbsolutePath() + ".res"); // NOI18N

            String pidFileName = pidFile.toString();
            String envFileName = pidFileName + ".env"; // NOI18N

            final ExternalTerminalAccessor terminalInfo =
                    ExternalTerminalAccessor.getDefault();

            if (terminalInfo.getTitle(terminal) == null) {
                terminal = terminal.setTitle(commandLine);
            }

            String cmd = commandLine;

            if (isWindows) {
                pidFileName = pidFileName.replaceAll("\\\\", "/"); // NOI18N
                envFileName = envFileName.replaceAll("\\\\", "/"); // NOI18N
                cmd = cmd.replaceAll("\\\\", "/"); // NOI18N
            }

            List<String> command = terminalInfo.wrapCommand(
                    info.getExecutionEnvironment(),
                    terminal,
                    dorunScript.getAbsolutePath(),
                    "-w", workingDirectory, // NOI18N
                    "-e", envFileName, // NOI18N
                    "-p", pidFileName, // NOI18N
                    "-x", terminalInfo.getPrompt(terminal), // NOI18N
                    cmd);

            ProcessBuilder pb = new ProcessBuilder(command);

            if ((isWindows || isMacOS) && wDir != null) {
                pb.directory(new File(wDir));
            }

            final MacroMap env = info.getEnvVariables();

            // setup DISPLAY variable for MacOS...
            if (isMacOS) {
                ProcessBuilder pb1 = new ProcessBuilder("/bin/sh", "-c", "/bin/echo $DISPLAY"); // NOI18N
                Process p1 = pb1.start();
                int status = p1.waitFor();
                String display = null;

                if (status == 0) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(p1.getInputStream()));
                    display = br.readLine();

                }

                if (display == null || "".equals(display)) { // NOI18N
                    display = ":0.0"; // NOI18N
                }

                pb.environment().put("DISPLAY", display); // NOI18N
            }

            if (!env.isEmpty()) {
                // TODO: FIXME (?)
                // Do PATH normalization on Windows....
                // Problem here is that this is done for PATH env. variable only!

                if (isWindows) {
                    String path = env.get("PATH"); // NOI18N
                    env.put("PATH", WindowsSupport.getInstance().normalizeAllPaths(path)); // NOI18N
                }

                // Always prepend /bin and /usr/bin to PATH
                env.put("PATH", "/bin:/usr/bin:$PATH"); // NOI18N

                File envFile = new File(envFileName);
                OutputStream fos = new FileOutputStream(envFile);
                EnvWriter ew = new EnvWriter(fos);
                ew.write(env);
                fos.close();
            }

            processError = new ByteArrayInputStream(new byte[0]);

            waitPID(pb.start(), pidFile);
        } catch (Throwable ex) {
            String msg = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            processError = new ByteArrayInputStream(msg.getBytes());
            resultFile = null;
            throw ex;
        }
    }

    @Override
    public void cancel() {
        sendSignal(9);
    }

    private synchronized int sendSignal(int signal) {
        int result = 1;
        int pid = -1;

        try {
            pid = getPID();
        } catch (IOException ex) {
        }

        if (pid < 0) {
            return -1;
        }

        try {
            ProcessBuilder pb;
            List<String> command = new ArrayList<String>();

            if (isWindows) {
                command.add(hostInfo.getShell());
                command.add("-c"); // NOI18N
                command.add("/bin/kill -" + signal + " " + getPID()); // NOI18N
            } else {
                command.add("/bin/kill"); // NOI18N
                command.add("-" + signal); // NOI18N
                command.add("" + getPID()); // NOI18N
            }

            pb = new ProcessBuilder(command);
            Process killProcess = pb.start();
            result = killProcess.waitFor();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (InterruptedIOException ex) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    @Override
    public int waitResult() throws InterruptedException {
        int pid = -1;

        try {
            pid = getPID();
        } catch (IOException ex) {
        }

        if (pid < 0) {
            return -1;
        }

        if (isWindows || isMacOS) {
            while (sendSignal(0) == 0) {
                Thread.sleep(300);
            }
        } else {
            File f = new File("/proc/" + pid); // NOI18N

            while (f.exists()) {
                Thread.sleep(300);
            }
        }

        if (resultFile == null) {
            return -1;
        }

        int exitCode = -1;

        try {
            resultFile.deleteOnExit();
            int attempts = 10;

            while (attempts-- > 0) {
                if (resultFile.exists() && resultFile.length() > 0) {
                    BufferedReader statusReader = new BufferedReader(new FileReader(resultFile));
                    String exitCodeString = statusReader.readLine();
                    if (exitCodeString != null) {
                        exitCode = Integer.parseInt(exitCodeString.trim());
                    }
                    break;
                }

                Thread.sleep(500);
            }
        } catch (InterruptedIOException ex) {
            throw new InterruptedException();
        } catch (IOException ex) {
        } catch (NumberFormatException ex) {
        }

        return exitCode;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return processOutput;
    }

    @Override
    public InputStream getErrorStream() {
        return processError;
    }

    private void waitPID(Process termProcess, File pidFile) throws IOException {
        while (!isInterrupted()) {
            /**
             * The following sleep appears after an attempt to support konsole
             * KDE4. This was done to give some time for external process to
             * write information about process' PID to the pidfile and not to
             * get to termProcess.exitValue() too eraly...
             * Currently there are no means on KDE4 to start konsole in
             * 'not-background' mode.
             * An attempt to use --nofork fails when start konsole from jvm
             * (see http://www.nabble.com/Can%27t-use---nofork-for-KUniqueApplications-from-another-kde-process-td21047022.html)
             * So termProcess exits immediately...
             *
             * Also this sleep is justifable because this doesn't make any sense
             * to check for a pid file too often.
             *
             */
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                continue;
            }

            if (pidFile.exists() && pidFile.length() > 0) {
                InputStream pidIS = new FileInputStream(pidFile);
                readPID(pidIS);
                pidIS.close();
                break;
            }

            try {
                int result = termProcess.exitValue();

                if (result != 0) {
                    log.info(loc("TerminalLocalNativeProcess.terminalFailed.text")); // NOI18N
                    BufferedReader br = new BufferedReader(new InputStreamReader(termProcess.getErrorStream()));
                    String s = br.readLine();
                    while (s != null) {
                        log.info("- " + s); // NOI18N
                        s = br.readLine();
                    }

                }

                // No exception - means process is finished..
                interrupt();
            } catch (IllegalThreadStateException ex) {
                // expected ... means that terminal process exists
            }
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(TerminalLocalNativeProcess.class, key, params);
    }
}
