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

package org.netbeans.modules.cnd.api.execution;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.execution.OutputWindowWriter;
import org.netbeans.modules.cnd.execution.Unbuffer;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @deprecated  Use {@link @org-netbeans-modules-nativexecution@} instead
 */
public class NativeExecutor implements Runnable {
    private final ArrayList<ExecutionListener> listeners = new ArrayList<ExecutionListener>();
    
    private final String runDir;
    private final String executable;
    private final String arguments;
    private final String[] envp;
    private final String tabName;
    private final String actionName;
    private final boolean parseOutputForErrors;
    private final boolean showInput;
    private final ExecutionEnvironment execEnv;
    private final boolean unbuffer;
    
    private String rcfile;
    private NativeExecution nativeExecution;
    
    private static final boolean showHeader = Boolean.getBoolean("cnd.execution.showheader");
    
    /** I/O class for writing output to a build tab */
    private InputOutput io;
    private PrintWriter out;
    private PrintWriter err;
    private Writer outputListener;
    
    /**
     * The real constructor. This class is used to manage native execution, but run and build.
     */
    public NativeExecutor(
	    ExecutionEnvironment execEnv,
            String runDir,
            String executable,
            String arguments,
            String[] envp,
            String tabName,
            String actionName,
            boolean parseOutputForErrors,
            boolean showInput,
            boolean unbuffer) {
        this.execEnv = execEnv;
        this.runDir = runDir;
        executable = LinkSupport.resolveWindowsLink(executable);
        this.executable = executable;
        this.arguments = arguments;
        this.envp = envp;
        this.tabName = tabName;
        this.actionName = actionName;
        this.parseOutputForErrors = parseOutputForErrors;
        this.showInput = showInput;
        this.unbuffer = unbuffer;
    }
    
    /** targets may be null to indicate default target */
    /*@Deprecated*/
    public NativeExecutor(
            String runDir,
            String executable,
            String arguments,
            String[] envp,
            String tabName,
            String actionName,
            boolean parseOutputForErrors,
            boolean showInput) {
        this(ExecutionEnvironmentFactory.getLocal(), runDir, executable,
                arguments, envp, tabName, actionName, parseOutputForErrors, showInput, false);
    }
    
    /** targets may be null to indicate default target */
    /*@Deprecated
    public NativeExecutor(
            String runDir,
            String executable,
            String arguments,
            String[] envp,
            String tabName,
            String actionName,
            boolean parseOutputForErrors) {
        this(runDir, executable, arguments, envp, tabName, actionName, parseOutputForErrors, false);
    }*/
    
    /** Start it going. */
    public ExecutorTask execute() throws IOException {
        return execute(getTab(tabName));
    }
    
    /** Start it going. */
    public ExecutorTask execute(InputOutput io) throws IOException {
        return execute(io, null); 
    }
    
    /** Start it going. */
    public ExecutorTask execute(InputOutput io, String host) throws IOException {
        final ExecutorTask task;
        synchronized (this) {
            // OutputWindow
            this.io = io;
            io.select();
            //io.getOut().reset();
            task = ExecutionEngine.getDefault().execute(tabName, this, InputOutput.NULL);
        }
        
        return task;
    }
    
    private InputOutput getTab(String tabName) {
        return IOProvider.getDefault().getIO(tabName, true);
    }
    
    public InputOutput getTab() {
        return io;
    }
    
    public String getTabeName() {
        return tabName;
    }

    public void setOutputListener(Writer outputListener) {
        this.outputListener = outputListener;
    }
    
    public void setExitValueOverride(String rcfile) {
        this.rcfile = rcfile;
    }

    private final String[] prepareEnvironment() {
        List<String> envpList = new ArrayList<String>();
        if (envp != null) {
            envpList.addAll(Arrays.asList(envp));
        }
        envpList.add("SPRO_EXPAND_ERRORS="); // NOI18N

        if (unbuffer) {
            try {
                // TODO: resolve remote path correctly!
                File exeFile = new File(runDir, executable);
                if (!exeFile.exists()) {
                    //try to resolve from the root
                    exeFile = new File(executable);
                }
                for (String envEntry : Unbuffer.getUnbufferEnvironment(execEnv, exeFile.getAbsolutePath())) {
                    envpList.add(envEntry);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return envpList.toArray(new String[envpList.size()]);
    }
    
    /**
     *  Call execute(), not this method directly!
     */
    synchronized public void run() {
        // IZ162493: no need to switch each time into Ouput window
//        io.setFocusTaken(true);
        io.setErrVisible(false);
        io.setErrSeparated(false);
        if (showInput) {
            io.setInputVisible(true);
        }
        
        File runDirFile = new File(runDir);
        OutputWriter originalWriter = io.getOut();
        if (outputListener != null) {
            originalWriter = new OutputWriterProxy(originalWriter, outputListener);
        }
        if (parseOutputForErrors) {
            out = new PrintWriter(new OutputWindowWriter(execEnv, originalWriter, FileUtil.toFileObject(runDirFile), parseOutputForErrors));
        } else {
            out = originalWriter;
        }
        err = io.getErr();
        
        executionStarted();
        int rc = 0;

        long startTime = System.currentTimeMillis();
        
        try {
            // Execute the selected command
            nativeExecution = NativeExecution.getDefault(execEnv);
            rc = nativeExecution.executeCommand(
                    runDirFile,
                    executable,
                    arguments,
                    prepareEnvironment(),
                    out,
		    showInput ? io.getIn() : null,
                    unbuffer);
        } catch (ThreadDeath td) {
            StatusDisplayer.getDefault().setStatusText(getString("MSG_FailedStatus"));
            executionFinished(-1, System.currentTimeMillis() - startTime);
            throw td;
        } catch (IOException ex) {
            // command not found, normal exit
            StatusDisplayer.getDefault().setStatusText(getString("MSG_FailedStatus"));
            rc = -1;
        } catch (InterruptedException ex) {
            // interrupted, normal exit
            StatusDisplayer.getDefault().setStatusText(getString("MSG_FailedStatus"));
            rc = -1;
        } catch (Throwable t) {
            StatusDisplayer.getDefault().setStatusText(getString("MSG_FailedStatus"));
            ErrorManager.getDefault().notify(t);
            rc = -1;
        } finally {
            if (showInput) {
                io.setInputVisible(false);
                try {
                    io.getIn().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        long time = System.currentTimeMillis() - startTime;
        if (rcfile != null) {
            File file = null;
            FileReader fr = null;
            
            try {
                file = new File(rcfile);
                
                if (file.exists()) {
                    fr = new FileReader(file);

                    if (fr.ready()) {
                        char[] cbuf = new char[256];
                        int i = fr.read(cbuf);
                        if (i > 0) {
                            rc = Integer.parseInt(String.valueOf(cbuf, 0, i - 1));
                        }
                    }
                }
            } catch (Exception ex) {
                // do nothing
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
                if (file != null && file.exists()) {
                   file.delete(); 
                }
            }     
        }
        executionFinished(rc, time);
    }
    
    public void stop() {
        nativeExecution.stop();
    }
    
    private void executionStarted() {
        if(showHeader) {
            String runDirToShow = execEnv.isLocal() ?
                runDir : HostInfoProvider.getMapper(execEnv).getRemotePath(runDir,true);
            
            String preText = MessageFormat.format(getString("PRETEXT"),
		    exePlusArgsQuoted(executable, arguments), runDirToShow);
            err.println(preText);
            err.println();
        }
        fireExecutionStarted();
    }

    private void executionFinished(int exitValue, long millis) {
        StringBuilder res = new StringBuilder();
        res.append(MessageFormat.format(getString(exitValue == 0 ? "SUCCESSFUL" : "FAILED"), actionName.toUpperCase())); // NOI18N
        res.append(" ("); // NOI18N
        if (exitValue != 0) {
            res.append(MessageFormat.format(getString("EXIT_VALUE"), exitValue)); // NOI18N
            res.append(' ');
        }
        res.append(MessageFormat.format(getString("TOTAL_TIME"), formatTime(millis))); // NOI18N
        res.append(')');

        PrintWriter pw = (exitValue == 0) ? out : err;
        pw.println(res.toString());
        pw.println();
        StatusDisplayer.getDefault().setStatusText(
                MessageFormat.format(getString(exitValue == 0 ? "MSG_SUCCESSFUL" : "MSG_FAILED"), actionName));  // NOI18N

        out.close();
        err.close();

        fireExecutionFinished(exitValue);
    }

    private static String formatTime(long millis) {
        StringBuilder res = new StringBuilder();
        long seconds = millis/1000;
        long minutes = seconds/60;
        long hours = minutes/60;
        if (hours > 0) {
            res.append(" " + hours + getString("HOUR")); // NOI18N
        }
        if (minutes > 0) {
            res.append(" " + (minutes-hours*60) + getString("MINUTE")); // NOI18N
        }
        if (seconds > 0) {
            res.append(" " + (seconds-minutes*60) + getString("SECOND")); // NOI18N
        } else {
            res.append(" " + (millis-seconds*1000) + getString("MILLISECOND")); // NOI18N
        }
        return res.toString();
    }
    
    public void addExecutionListener(ExecutionListener l) {
        listeners.add(l);
    }
    
    public void removeExecutionListener(ExecutionListener l) {
        listeners.remove(l);
    }
    
    /**
     * Send a ExecutionEvent to each executionStarted method
     */
    private void fireExecutionStarted() {
        for (ExecutionListener listener : listeners) {
            listener.executionStarted(ExecutionListener.UNKNOWN_PID);
        }
    }
    
    /**
     * Send a ExecutionEvent to each executionFinished method
     */
    private void fireExecutionFinished(int rc) {
        for (ExecutionListener listener : listeners) {
            listener.executionFinished(rc);
        }
    }
    
    private String exePlusArgsQuoted(String exe, String args) {
        String ret = exe;
        // add quoted arguments
        if (args == null || args.length() == 0) {
            ret = "\"" + ret + "\""; // NOI18N
        } else {
            ret = "\"" + ret + " " + args + "\""; // NOI18N
        }
        
        return ret;
    }
    
    private static ResourceBundle bundle = null;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(NativeExecutor.class);
        }
        return bundle.getString(s);
    }

    private static class OutputWriterProxy extends OutputWriter {
        private final OutputWriter original;
        private final Writer duplicate;

        private OutputWriterProxy(OutputWriter original, Writer duplicate) {
            super(original);
            this.original = original;
            this.duplicate = duplicate;
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            original.write(cbuf, off, len);
            doWrite(cbuf, off, len);
        }

        @Override
        public void println(String s) {
            original.println(s);
            doWrite(s.toCharArray(), 0, s.length());
            doWrite(new char[]{'\n'}, 0, 1);
        }

        @Override
        public void write(String s, int off, int len) {
            original.write(s, off, len);
            doWrite(s.toCharArray(), off, len);
        }

        @Override
        public void write(int c) {
            original.write(c);
            doWrite(new char[]{(char)c}, 0, 1);
        }

        @Override
        public void write(char data[]) {
            original.write(data);
            doWrite(data, 0, data.length);
        }

        @Override
        public void flush() {
            original.flush();
            try {
                duplicate.flush();
            } catch (IOException ex) {
            }
        }

        @Override
        public void close() {
            original.close();
            try {
                duplicate.close();
            } catch (IOException ex) {
            }
        }

        @Override
        public void reset() throws IOException {
            original.reset();
        }

        @Override
        public void println(String s, OutputListener l) throws IOException {
            original.println(s,l);
            doWrite(s.toCharArray(), 0, s.length());
            doWrite(new char[]{'\n'}, 0, 1);
        }

        private void doWrite(char[] cbuf, int off, int len){
            try {
                duplicate.write(cbuf, off, len);
            } catch (IOException ex) {
            }
        }
    }
}
