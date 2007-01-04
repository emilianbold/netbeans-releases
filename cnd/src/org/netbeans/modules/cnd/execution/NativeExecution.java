/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.modules.cnd.execution41.org.openide.loaders.ExecutionSupport;
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

/**
 *  A support class for helping execution of an executable, a makefile, or a script.
 */
public class NativeExecution extends ExecutionSupport {
    /** Script file that merges stdout and stderr on Unix */
    private static File stdOutErrFile = null;
    private static boolean hasWarned = false;
    
    private File runDirFile;
    private static ResourceBundle bundle = NbBundle.getBundle(NativeExecution.class);
    private OutputReaderThread outputReaderThread = null; // Thread for running process
    private InputReaderThread inputReaderThread = null; // Thread for running process
    private Process executionProcess = null;
    private PrintWriter out;
    private Reader tmp_in;
    
    /** Constructor */
    public NativeExecution() {
        super(null);
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
            Reader in) throws IOException, InterruptedException {
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
        
        List nueEnvp;
        if (envp != null) {
            nueEnvp = new ArrayList(Arrays.asList(envp));
        } else {
            nueEnvp = new ArrayList();
        }
        nueEnvp.add("SPRO_EXPAND_ERRORS="); // NOI18N
        
        envp = (String[] ) nueEnvp.toArray(new String[0]);
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
    
    public void start() {
        super.start();
    }
    
    public void destroy() {
        /*
        if (executionThread != null) {
            executionThread.interrupt();
        }
         */
        if (executionProcess != null) {
            executionProcess.destroy();
        }
    }
    
    
    /** Helper class to read the input from the build */
    private static final class OutputReaderThread  extends Thread {
        
        /** This is all output, not just stderr */
        private Reader err;
        private Writer output;
        private Reader tmp_in;
        
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
        public void run() {
            try {
                int read;
                
                while ((read = err.read()) != (-1)) {
                    output.write((char) read);
                    output.flush();
                }
                output.flush();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
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
