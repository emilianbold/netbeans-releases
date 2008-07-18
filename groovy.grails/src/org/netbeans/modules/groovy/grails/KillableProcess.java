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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public class KillableProcess extends Process {

    private static final Logger LOGGER = Logger.getLogger(KillableProcess.class.getName());

    private static final long TIMEOUT = 5000;

    private final Process nativeProcess;

    private final File directory;

    private final String command;

    public KillableProcess(Process nativeProcess, File directory, String command) {
        this.nativeProcess = nativeProcess;
        this.directory = directory;
        this.command = command;
    }

    @Override
    public void destroy() {
        if (!Utilities.isWindows()) {
            nativeProcess.destroy();
            return;
        }

        // wmic process where name="cmd.exe" get processid, commandline
        String params[] = {"process", "where", "name=\"cmd.exe\"", // NOI18N
                            "get", "processid,commandline" }; // NOI18N

        WindowsExecutor executor = new WindowsExecutor("wmic.exe",
                Utilities.escapeParameters(params), directory.getAbsolutePath(), command);

        Thread t = new Thread(executor);
        t.start();

        boolean interrupted = Thread.interrupted();
        try {
            try {
                t.join(TIMEOUT);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.FINEST, null, ex);
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        // kill running server using taskkill
        String pidToKill =  executor.getPid();

        if (pidToKill != null) {
            WindowsExecutor killer = new WindowsExecutor(
                    "taskkill.exe", "/F /PID " + pidToKill + " /T", null, null);

            Thread tk = new Thread(killer);
            tk.start();
        } else {
            nativeProcess.destroy();
        }
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

    private static class WindowsExecutor implements Runnable {

        private final String cmd;

        private final String args;

        private final String nameToFilter;

        private final String commandToFilter;

        private String pid;

        public WindowsExecutor(String cmd, String args, String nameToFilter, String commandToFilter) {
            this.cmd = cmd;
            this.args = args;
            this.nameToFilter = nameToFilter;
            this.commandToFilter = commandToFilter;
        }

        public String getPid() {
            return pid;
        }

        public void run() {
            NbProcessDescriptor cmdProcessDesc = new NbProcessDescriptor(cmd, args);

            try {
                Process utilityProcess = cmdProcessDesc.exec(null, null, true, null);

                if (utilityProcess == null) {
                    return;
                }

                utilityProcess.getOutputStream().close();

                // we wait till the process finishes. De-coupling is done a layer above.
                try {
                    utilityProcess.waitFor();
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.FINEST, null, ex);
                }

                if (nameToFilter == null) {
                    return;
                }

                Pattern pattern = Pattern.compile(".*grails.bat(\\s+-D\\S*=\\S*)*\\s+"
                        + Pattern.quote(commandToFilter) + "\\s+REM NB:" + Pattern.quote(nameToFilter)
                        + ".*");

                BufferedReader procOutput = new BufferedReader(
                        new InputStreamReader(utilityProcess.getInputStream()));
                try {
                    String errString;
                    while ((errString = procOutput.readLine()) != null) {
                        if (pattern.matcher(errString).matches()) {
                            String nbTag = "REM NB:" + nameToFilter; // NOI18N
                            int idx = errString.indexOf(nbTag);
                            idx = idx + nbTag.length();
                            pid = errString.substring(idx).trim();
                            LOGGER.log(Level.FINEST, "Found: " + pid);
                        }
                    }
                } finally {
                    procOutput.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Project exec() problem", ex);
            }
        }
    }
}
