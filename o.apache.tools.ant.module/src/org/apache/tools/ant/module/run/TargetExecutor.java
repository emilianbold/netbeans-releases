/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2001.
 * All Rights Reserved.
 *
 * Contributor(s): Jayme C. Edwards, Jesse Glick
 */
 
package org.apache.tools.ant.module.run;

import java.awt.EventQueue;
import java.io.*;
import java.util.*;

import org.openide.*;
import org.openide.awt.Actions;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.*;
import org.openide.execution.ExecutionEngine;
import org.openide.loaders.DataObject;
import org.openide.util.*;
import org.openide.windows.*;

import org.w3c.dom.Element;

import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.openide.util.io.ReaderInputStream;

/** Executes an Ant Target asynchronously in the IDE.
 */
public final class TargetExecutor implements Runnable {
    
    /**
     * All tabs which were used for some process which has now ended.
     * These are closed when you start a fresh process.
     * Map from tab to tab display name.
     * @see "#43001"
     */
    private static final Map/*<InputOutput,String>*/ freeTabs = new WeakHashMap();
    
    private AntProjectCookie pcookie;
    private InputOutput io;
    private OutputStream outputStream;
    private boolean ok = false;
    private int verbosity = AntSettings.getDefault ().getVerbosity ();
    private Properties properties = (Properties) AntSettings.getDefault ().getProperties ().clone ();
    private List targetNames;
    /** used for the tab etc. */
    private String displayName;

    /** targets may be null to indicate default target */
    public TargetExecutor (AntProjectCookie pcookie, String[] targets) {
        this.pcookie = pcookie;
        targetNames = ((targets == null) ? null : Arrays.asList((Object[]) targets));
    }
  
    public void setVerbosity (int v) {
        verbosity = v;
    }
    
    public synchronized void setProperties (Properties p) {
        properties = (Properties) p.clone ();
    }
    
    public synchronized void addProperties (Properties p) {
        if (p.isEmpty ()) return;
        Properties old = properties;
        properties = new Properties ();
        properties.putAll (old);
        properties.putAll (p);
    }
    
    public ExecutorTask execute () throws IOException {
        Element projel = pcookie.getProjectElement();
        String projectName;
        if (projel != null) {
            // remove & if available.
            projectName = Actions.cutAmpersand(projel.getAttribute("name")); // NOI18N
        } else {
            projectName = NbBundle.getMessage(TargetExecutor.class, "LBL_unparseable_proj_name");
        }
        String fileName;
        if (pcookie.getFileObject() != null) {
            fileName = pcookie.getFileObject().getNameExt();
        } else {
            fileName = pcookie.getFile().getName();
        }
        if (projectName.equals("")) { // NOI18N
            // No name="..." given, so try the file name instead.
            projectName = fileName;
        }
        if (targetNames != null) {
            StringBuffer targetList = new StringBuffer();
            Iterator it = targetNames.iterator();
            if (it.hasNext()) {
                targetList.append((String) it.next());
            }
            while (it.hasNext()) {
                targetList.append(NbBundle.getMessage(TargetExecutor.class, "SEP_output_target"));
                targetList.append((String) it.next());
            }
            displayName = NbBundle.getMessage(TargetExecutor.class, "TITLE_output_target", projectName, fileName, targetList);
        } else {
            displayName = NbBundle.getMessage(TargetExecutor.class, "TITLE_output_notarget", projectName, fileName);
        }
        
        final ExecutorTask task;
        synchronized (this) {
            // OutputWindow
            synchronized (freeTabs) {
                Iterator it = freeTabs.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    InputOutput free = (InputOutput)entry.getKey();
                    String freeName = (String)entry.getValue();
                    if (io == null && freeName.equals(displayName)) {
                        // Reuse it.
                        io = free;
                        io.getOut().reset();
                        io.getErr().reset();
                        io.flushReader();
                    } else {
                        // Discard it.
                        // XXX if you run e.g. F9 many times very fast, sometimes
                        // it can happen that some tabs stay open even though they
                        // should be closed. Why? Happens with both core/output and
                        // core/output2 so may be a bug in the execution engine...
                        // or in code shared between these two modules.
                        // Replanning to EQ does not help, either.
                        free.closeInputOutput();
                    }
                }
                freeTabs.clear();
            }
            if (io == null) {
                io = IOProvider.getDefault().getIO(displayName, true);
            }
            // Disabled since for Ant-based compilation it is usually annoying:
            // #16720:
            //io.select();
            
            task = ExecutionEngine.getDefault().execute(displayName, this, InputOutput.NULL);
        }
        WrapperExecutorTask wrapper = new WrapperExecutorTask(task, io);
        RequestProcessor.getDefault().post(wrapper);
        return wrapper;
    }
    
    public ExecutorTask execute(OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        ExecutorTask task = ExecutionEngine.getDefault().execute(
            NbBundle.getMessage(TargetExecutor.class, "LABEL_execution_name"), this, InputOutput.NULL);
        return new WrapperExecutorTask(task, null);
    }
    
    private class WrapperExecutorTask extends ExecutorTask {
        private ExecutorTask task;
        private InputOutput io;
        public WrapperExecutorTask(ExecutorTask task, InputOutput io) {
            super(new WrapperRunnable(task));
            this.task = task;
            this.io = io;
        }
        public void stop () {
            task.stop ();
        }
        public int result () {
            return task.result () + (ok ? 0 : 1);
        }
        public InputOutput getInputOutput () {
            return io;
        }
    }
    private static class WrapperRunnable implements Runnable {
        private final ExecutorTask task;
        public WrapperRunnable(ExecutorTask task) {
            this.task = task;
        }
        public void run () {
            task.waitFinished ();
        }
    }
  
    /** Call execute(), not this method directly!
     */
    synchronized public void run () {
        try {
        
        if (outputStream == null) {
            // Just annoying during normal compilation:
            //io.setFocusTaken (true);
            io.setErrVisible (false);
            // Generally more annoying than helpful:
            io.setErrSeparated (false);
            // But want to bring I/O window to front without selecting, if possible:
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    // XXX would be cleaner to call ioTC.requestVisible but we have
                    // no way of getting the TC corresponding to io...
                    TopComponent orig = TopComponent.getRegistry().getActivated();
                    io.select();
                    if (orig != null) {
                        orig.requestActive();
                    }
                }
            });
        }
        
        if (AntSettings.getDefault ().getSaveAll ()) {
            LifecycleManager.getDefault ().saveAll ();
        }
        
        PrintStream out;
        PrintStream err;
        if (outputStream == null) {
            out = new PrintStream(new OutputWriterOutputStream(io.getOut()));
            err = new PrintStream(new OutputWriterOutputStream(io.getErr()));
        } else {
            out = err = new PrintStream(outputStream);
        }
        
        File buildFile = pcookie.getFile ();
        if (buildFile == null) {
            err.println(NbBundle.getMessage(TargetExecutor.class, "EXC_non_local_proj_file"));
            return;
        }
        
        // Don't hog the CPU, the build might take a while:
        Thread.currentThread().setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
        
        InputStream in = null;
        if (outputStream == null) { // #43043
            try {
                in = new ReaderInputStream(io.getIn());
            } catch (IOException e) {
                AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        ok = AntBridge.getInterface().run(buildFile, pcookie.getFileObject(), targetNames,
                                          in, out, err, properties, verbosity, outputStream == null, displayName);
        
        } finally {
            if (io != null) {
                synchronized (freeTabs) {
                    freeTabs.put(io, displayName);
                }
            }
        }
    }

}
