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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.execution.NativeExecution;
import org.netbeans.modules.cnd.execution.OutputWindowWriter;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public class NativeExecutor implements Runnable {
    private ArrayList listeners = new ArrayList();
    
    private String runDir;
    private String executable;
    private String arguments;
    private String[] envp;
    private String tabName;
    private String actionName;
    private String rcfile;
    private boolean parseOutputForErrors;
    private boolean showInput;
    
    private boolean showHeader = true;
    private boolean showFooter = true;
    
    /** I/O class for writing output to a build tab */
    private InputOutput io;
    private PrintWriter out;
    
    /** targets may be null to indicate default target */
    public NativeExecutor(
            String runDir,
            String executable,
            String arguments,
            String[] envp,
            String tabName,
            String actionName,
            boolean parseOutputForErrors) {
        this(runDir, executable, arguments, envp, tabName, actionName, parseOutputForErrors, false);
    }
    
    /** targets may be null to indicate default target */
    public NativeExecutor(
            String runDir,
            String executable,
            String arguments,
            String[] envp,
            String tabName,
            String actionName,
            boolean parseOutputForErrors,
            boolean showInput) {
        //this.pae = pae;
        this.runDir = runDir;
        this.executable = executable;
        this.arguments = arguments;
        this.envp = envp;
        this.tabName = tabName;
        this.actionName = actionName;
        this.parseOutputForErrors = parseOutputForErrors;
        this.showInput = showInput;
    }
    
    public NativeExecutor(
            String runDir,
            String executable,
            String arguments,
            String[] envp,
            String tabName,
            String actionName,
            boolean parseOutputForErrors,
            boolean showInput,
            boolean showHeader,
            boolean showFooter ) {
        this( runDir, executable, arguments, envp, tabName, actionName, parseOutputForErrors, showInput );
        
        this.showHeader = showHeader;
        this.showFooter = showFooter;
    }
    
    /** Start it going. */
    public ExecutorTask execute() throws IOException {
        return execute(getTab(tabName));
    }
    
    /** Start it going. */
    public ExecutorTask execute(InputOutput io) throws IOException {
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
    
    public void setExitValueOverride(String rcfile) {
        this.rcfile = rcfile;
    }
    
    /**
     *  Call execute(), not this method directly!
     */
    synchronized public void run() {
        io.setFocusTaken(true);
        io.setErrVisible(false);
        io.setErrSeparated(false);
        if (showInput) {
            io.setInputVisible(true);
        }
        
        File runDirFile = new File(runDir);
        if (parseOutputForErrors)
            out = new PrintWriter(new OutputWindowWriter(io.getOut(), FileUtil.toFileObject(runDirFile), parseOutputForErrors));
        else
            out = io.getOut();
        executionStarted();
        int rc = 0;
        
        try {
            // Execute the selected command
            rc = new NativeExecution().executeCommand(
                    runDirFile,
                    executable,
                    arguments,
                    envp,
                    out,
		    showInput ? io.getIn() : null);
        } catch (ThreadDeath td) {
            StatusDisplayer.getDefault().setStatusText(getString("MSG_FailedStatus"));
            executionFinished(-1);
            out.close();
            throw td;
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
        if (rcfile != null) {
            File file = null;
            
            try {
                file = new File(rcfile);
                
                if (file.exists()) {
                    FileReader fr = new FileReader(file);

                    if (fr.ready()) {
                        char[] cbuf = new char[256];
                        int i = fr.read(cbuf);
                        if (i > 0) {
                            rc = Integer.valueOf(String.valueOf(cbuf, 0, i - 1)).intValue();
                        }

                    }
                    fr.close();
                    file.delete();
                }
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            } finally {
                if (file != null && file.exists()) {
                   file.delete(); 
                }
            }     
        }
        executionFinished(rc);
        out.close();
    }
    
    private void executionStarted() {
        if( showHeader ) {
            String preText = MessageFormat.format(getString("PRETEXT"),
		    new Object[] {exePlusArgsQuoted(executable, arguments), runDir});
            out.println(preText);
            out.println("");
        }
        fireExecutionStarted();
    }
    
    private void executionFinished(int exitValue) {
        if( showFooter ) {
            String failedOrSucceded = MessageFormat.format(getString(exitValue == 0 ? "SUCCESSFUL" : "FAILED"), new Object[] {actionName});
            String postText = MessageFormat.format(getString("POSTTEXT"), new Object[] {failedOrSucceded, "" + exitValue}); // NOI18N
            out.println(""); // NOI18N
            out.println(postText);
            out.println(""); // NOI18N
            StatusDisplayer.getDefault().setStatusText(failedOrSucceded);
        }
        fireExecutionFinished(exitValue);
    }
    
    public void addExecutionListener(ExecutionListener l) {
        listeners.add(l);
    }
    
    public void removeExecutionListener(ExecutionListener l) {
        listeners.remove(listeners.indexOf(l));
    }
    
    /**
     * Send a ExecutionEvent to each executionStarted method
     */
    private void fireExecutionStarted() {
        for (int i = 0; i < listeners.size(); i++) {
            ExecutionListener listener = (ExecutionListener) listeners.get(i);
            listener.executionStarted();
        }
    }
    
    /**
     * Send a ExecutionEvent to each executionFinished method
     */
    private void fireExecutionFinished(int rc) {
        for (int i = 0; i < listeners.size(); i++) {
            ExecutionListener listener = (ExecutionListener) listeners.get(i);
            listener.executionFinished(rc);
        }
    }
    
    private String exePlusArgsQuoted(String exe, String args) {
        String ret = exe;
        // add quoted arguments
        if (args == null || args.length() == 0)
            ret =  "\"" + ret + "\""; // NOI18N
        else
            ret =  "\"" + ret + " " + args + "\""; // NOI18N
        
        return ret;
    }
    
    private static ResourceBundle bundle = null;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(NativeExecutor.class);
        }
        return bundle.getString(s);
    }
}
