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
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.execution.NativeExecution;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *  A support class for helping execution of an executable, a makefile, or a script.
 */
/* package-local */
class LocalNativeExecution extends NativeExecution {
    /** Script file that merges stdout and stderr on Unix */
    //private static File stdOutErrFile = null;
    //private static boolean hasWarned = false;

    //private File runDirFile;
    private static ResourceBundle bundle = NbBundle.getBundle(LocalNativeExecution.class);
    private OutputReaderThread outputReaderThread = null; // Thread for running process
    private InputReaderThread inputReaderThread = null; // Thread for running process
    //private Process executionProcess = null;
    //private PrintWriter out;

    private static Logger execLog;

    /* package-local */
    LocalNativeExecution(ExecutionEnvironment execEnv) {
        assert execEnv.isLocal();
    }

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
        int rc = -1;

        //this.runDirFile = runDirFile;
        //this.out = out;

        if (!runDirFile.exists() || !runDirFile.isDirectory()) {
            String msg = MessageFormat.format(getString("NOT_A_VALID_BUILD_DIRECTORY"), new Object[] {runDirFile.getPath()}); // NOI18N
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            return -1;
        }

        Process executionProcess = exec(executable, arguments, envp, runDirFile);
        outputReaderThread = new OutputReaderThread(executionProcess.getInputStream(), out);
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

    private static Logger getExecLog() {
        if (execLog == null) {
            execLog = Logger.getLogger(LocalNativeExecution.class.getName());
        }
        return execLog;
    }

    private static void logArgs(String[] args) {
        getExecLog().fine("Running: " + Arrays.asList(args)); // NOI18N
    }

    // Copied from NbProcessDescriptor but with error stream redirection
    private Process exec(String processName, String arguments, String[] envp, File cwd) throws IOException {
        String[] args = Utilities.parseParameters(arguments);

        // copy the call string
        String[] call = new String[args.length + 1];
        call[0] = processName;
        System.arraycopy(args, 0, call, 1, args.length);

        logArgs(call);

        ProcessBuilder pb = new ProcessBuilder(call);
        pb.redirectErrorStream(true);

        if (envp != null) {
            Map<String,String> e = pb.environment();
            for (int i = 0; i < envp.length; i++) {
                String nameval = envp[i];
                int idx = nameval.indexOf('='); // NOI18N
                // [PENDING] add localized annotation...
                if (idx == -1) {
                    throw new IOException ("No equal sign in name=value: " + nameval); // NOI18N
                }
                e.put(nameval.substring(0, idx), nameval.substring(idx + 1));
                }
            }

        if (cwd != null) {
            pb.directory(cwd);
        }
        
        return pb.start();
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

    private static String getString(String prop) {
        return bundle.getString(prop);
    }
}
