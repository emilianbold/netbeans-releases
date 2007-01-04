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

package org.netbeans.modules.cnd.api.execution;

import java.io.File;
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
        return execute(getTab(actionName, tabName));
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
    
    public InputOutput getTab(String actionName, String tabName) {
        return IOProvider.getDefault().getIO(tabName, false);
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
        
        out = new PrintWriter(new OutputWindowWriter(io.getOut(), FileUtil.toFileObject(runDirFile), parseOutputForErrors));
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
