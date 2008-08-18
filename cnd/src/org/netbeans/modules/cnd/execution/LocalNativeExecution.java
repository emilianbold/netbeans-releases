/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.execution;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ResourceBundle;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.modules.InstalledFileLocator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.execution.NativeExecution;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;

/**
 *  A support class for helping execution of an executable, a makefile, or a script.
 */
public class LocalNativeExecution extends NativeExecution {
    /** Script file that merges stdout and stderr on Unix */
    private static File stdOutErrFile = null;
    private static boolean hasWarned = false;

    private File runDirFile;
    private static ResourceBundle bundle = NbBundle.getBundle(LocalNativeExecution.class);
    private OutputReaderThread outputReaderThread = null; // Thread for running process
    private InputReaderThread inputReaderThread = null; // Thread for running process
    private Process executionProcess = null;
    private PrintWriter out;

    /**
     * Execute an executable, a makefile, or a script
     * @param runDir absolute path to directory from where the command should be executed
     * @param executable absolute or relative path to executable, makefile, or script
     * @param arguments space separated list of arguments
     * @param envp environment variables (name-value pairs of the form ABC=123)
     * @param out Output
     * @param io Input
     * @param parseOutput true if output should be parsed for compiler errors
     * @return completion code
     */
    public int executeCommand(
            File runDirFile,
            String executable,
            String arguments,
            String[] envp,
            PrintWriter out,
            Reader in,
            boolean unbuffer) throws IOException, InterruptedException {
        String commandInterpreter;
        String commandLine;
        int rc = -1;

        this.runDirFile = runDirFile;
        this.out = out;

        if (!runDirFile.exists() || !runDirFile.isDirectory()) {
            String msg = MessageFormat.format(getString("NOT_A_VALID_BUILD_DIRECTORY"), new Object[] {runDirFile.getPath()}); // NOI18N
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            return -1;
        }

        commandInterpreter = getStdOutErrFile().getPath();
        commandLine = executable + " " + arguments; // NOI18N

        // Start the build process and a build reader.
        NbProcessDescriptor desc = new NbProcessDescriptor(commandInterpreter, commandLine);

        // Updating environment variables
        List<String> envpList = new ArrayList<String>();
        if (envp != null) {
            envpList.addAll(Arrays.asList(envp));
        }
        envpList.add("SPRO_EXPAND_ERRORS="); // NOI18N

        if (unbuffer) {
            String unbufferPath = getUnbufferPath();
            if (unbufferPath != null) {
                if (Utilities.isMac()) {
                    envpList.add("DYLD_INSERT_LIBRARIES=" + unbufferPath); // NOI18N
                    envpList.add("DYLD_FORCE_FLAT_NAMESPACE=yes"); // NOI18N
                } else if (Utilities.isWindows()) {
                    //TODO: issue #144106
                } else {
                    envpList.add("LD_PRELOAD=" + unbufferPath); // NOI18N
                }
            }
        }
        envp = envpList.toArray(new String[envpList.size()]);

        executionProcess = desc.exec(null, envp, true, runDirFile);
        outputReaderThread = new OutputReaderThread(executionProcess.getErrorStream(), out);
        outputReaderThread.start();
        if (in != null) {
            inputReaderThread = new InputReaderThread(executionProcess.getOutputStream(), in);
            inputReaderThread.start();
        } else {
            inputReaderThread = null;
        }
        try {
            rc = executionProcess.waitFor();
        } catch (InterruptedException ex) {
            // We've interupted the process. Kill it and wait for the process to finish.
            executionProcess.destroy();
            while (rc < 0) {
                try {
                    rc = executionProcess.waitFor();
                } catch (InterruptedException ex1) {
                }
            }
        }
        try {
            outputReaderThread.join();	    // wait for the thread to complete
        } catch (InterruptedException ex2) {
            // On Windows join() throws InterruptedException if process was terminated/interrupted
        }

        return rc;
    }

    public void stop() {
        /*
        if (executionThread != null) {
            executionThread.interrupt();
        }
         */
        outputReaderThread.cancel();
//        if (executionProcess != null) {
//            executionProcess.destroy();
//        }
    }

    private String getUnbufferPath() {
        int platformType = PlatformInfo.getDefault(CompilerSetManager.LOCALHOST).getPlatform();
        String unbufferName = getUnbufferName(platformType);
        if (unbufferName == null) {
            return null;
        }
        File file = InstalledFileLocator.getDefault().locate("bin/" + unbufferName, null, false);
        if (file != null && file.exists()) {
            return fixPath(file.getAbsolutePath());
        } else {
            log.warning("unbuffer: " + unbufferName + " not found");
            return null;
        }
    }

    private String fixPath(String path) {
        // TODO: implement
        /*
        if (isCygwin() && path.charAt(1) == ':') {
            return "/cygdrive/" + path.charAt(0) + path.substring(2).replace("\\", "/"); // NOI18N
        } else if (isMinGW() && path.charAt(1) == ':') {
            return "/" + path.charAt(0) + path.substring(2).replace("\\", "/"); // NOI18N
        } else {
            return path;
        }*/
        return path;
    }


    /** Helper class to read the input from the build */
    private static final class OutputReaderThread  extends Thread {

        /** This is all output, not just stderr */
        private Reader err;
        private Writer output;
        private boolean cancel = false;

        public OutputReaderThread(InputStream err, Writer output) {
            this.err = new InputStreamReader(err);
            this.output = output;
            setName("OutputReaderThread"); // NOI18N - Note NetBeans doesn't xlate "IDE Main"
        }

        /**
         *  Reader proc to read the combined stdout and stderr from the build process.
         *  The output comes in on a single descriptor because the build process is started
         *  via a script which diverts stdout to stderr. This is because older versions of
         *  Java don't have a good way of interleaving stdout and stderr while keeping the
         *  exact order of the output.
         */
        @Override
        public void run() {
            try {
                int read;

                while ((read = err.read()) != (-1)) {
                    if (cancel) { // 131739
                        return;
                    }
                    if (read == 10)
                        output.write("\n"); // NOI18N
                    else
                        output.write((char) read);
                    //output.flush(); // 135380
                }
                output.flush();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        public void cancel() {
            cancel = true;
        }
    }

    /** Helper class to read the input from the build */
    private static final class InputReaderThread extends Thread {

        /** This is all output, not just stderr */
        private Reader in;
        private OutputStream pout;

        public InputReaderThread(OutputStream pout, Reader in) {
            this.pout = pout;
            this.in = in;
            setName("inputReaderThread"); // NOI18N - Note NetBeans doesn't xlate "IDE Main"
        }

        /**
         *  Reader proc to read input from Output2's input textfield and send it
         *  to the running process.
         */
        @Override
        public void run() {
            int ch;

            try {
                while ((ch = in.read()) != (-1)) {
                    pout.write((char) ch);
                    pout.flush();
                }
            } catch (IOException e) {
            } finally {
                // Handle EOF and other exits
                try {
                    pout.flush();
                    pout.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    /**
     * Find the script stdouterr.sh somewhere in the installation tree. It is needed to merge stdout and stderr
     * for for instance makefile execution.
     */
    public static File getStdOutErrFile() {
        if (stdOutErrFile == null) {
            String stderrCmd;

            if( Utilities.isUnix()) {
                stderrCmd = "bin/stdouterr.sh"; // NOI18N
            } else {
                stderrCmd = "bin\\stdouterr.bat";   // NOI18N
            }

            stdOutErrFile = InstalledFileLocator.getDefault().locate(stderrCmd, null, false);
            if (stdOutErrFile == null && !hasWarned) {
                String msg = MessageFormat.format(getString("CANNOT_FIND_SCRIPT"), new Object[] {stderrCmd});
                NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(notifyDescriptor);
                hasWarned = true;
            }
        }
        return stdOutErrFile;
    }

    private static String getString(String prop) {
        return bundle.getString(prop);
    }
}
