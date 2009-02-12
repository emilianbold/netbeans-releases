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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 */
public final class TerminalLocalNativeProcess extends AbstractNativeProcess {

    private static final Runtime rt = Runtime.getRuntime();
    private final static String dorunScript;
    private final InputStream processOutput;
    private final InputStream processError;
    private final OutputStream processInput;
    private final File pidFile;
//    private final PipedInputStream pin = new PipedInputStream();
//    private final PipedOutputStream pout = new PipedOutputStream(pin);


    static {
        InstalledFileLocator fl = InstalledFileLocator.getDefault();
        File file = fl.locate("bin/nativeexecution/dorun.sh", null, false); // NOI18N
        dorunScript = file.toString();
        try {
            new ProcessBuilder("/bin/chmod", "+x", dorunScript).start().waitFor(); // NOI18N
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public TerminalLocalNativeProcess(final ExternalTerminal t,
            final NativeProcessInfo info) throws IOException {
        super(info);

        ExternalTerminal terminal = t;

        final String commandLine = info.getCommandLine(true);
        final String workingDirectory = info.getWorkingDirectory(true);
        final File wdir =
                workingDirectory == null ? null : new File(workingDirectory);

        pidFile = File.createTempFile("termexec", "pid"); // NOI18N
        pidFile.deleteOnExit();

        String pidFileName = pidFile.toString();

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
            pb.environment().putAll(info.getEnvVariables(true));
            pb.directory(wdir);

            Process termProcess = pb.start();

            processOutput = new ByteArrayInputStream(new byte[0]);
            processError = new ByteArrayInputStream(new byte[0]);
            processInput = null;

            readPID(new FileInputStream(pidFile));
        }
    }

    @Override
    public void cancel() {
        try {
            ProcessBuilder pb =
                    new ProcessBuilder("/bin/kill", "-9", "" + getPID()); // NOI18N
            pb.start().waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public int waitResult() throws InterruptedException {
        File f = new File("/proc/" + getPID()); // NOI18N

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
}
