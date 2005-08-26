/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.run;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.LifecycleManager;
import org.openide.awt.Actions;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.io.ReaderInputStream;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.w3c.dom.Element;

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
    
    private static final Map/*<ThreadGroup,Runnable>*/ interestingOutputCallbacksByThreadGroup = Collections.synchronizedMap(new WeakHashMap());
    
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
            if (AntSettings.getDefault().getAutoCloseTabs()) { // #47753
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
                        // useless: io.flushReader();
                    } else {
                        // Discard it.
                        free.closeInputOutput();
                    }
                }
                freeTabs.clear();
            }
            }
            if (io == null) {
                io = IOProvider.getDefault().getIO(displayName, true);
            }
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
        final Thread[] thisProcess = new Thread[1];
        final ProgressHandle[] handle = new ProgressHandle[1];
        try {
            
        final boolean[] displayed = new boolean[] {AntSettings.getDefault().getAlwaysShowOutput()};
        
        if (outputStream == null) {
            if (displayed[0]) {
                io.select();
            }
        }
        
        if (AntSettings.getDefault ().getSaveAll ()) {
            LifecycleManager.getDefault ().saveAll ();
        }
        
        OutputWriter out;
        OutputWriter err;
        if (outputStream == null) {
            out = io.getOut();
            err = io.getErr();
        } else {
            throw new RuntimeException("XXX No support for outputStream currently!"); // NOI18N
        }
        
        File buildFile = pcookie.getFile ();
        if (buildFile == null) {
            err.println(NbBundle.getMessage(TargetExecutor.class, "EXC_non_local_proj_file"));
            return;
        }
        
        // Don't hog the CPU, the build might take a while:
        Thread.currentThread().setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
        
        final Runnable interestingOutputCallback = new Runnable() {
            public void run() {
                // #58513: display output now.
                if (!displayed[0]) {
                    displayed[0] = true;
                    io.select();
                }
            }
        };
        
        InputStream in = null;
        if (outputStream == null) { // #43043
            try {
                in = new ReaderInputStream(io.getIn()) {
                    // Show the output when an input field is displayed, if it hasn't already.
                    public int read() throws IOException {
                        interestingOutputCallback.run();
                        return super.read();
                    }
                    public int read(byte[] b) throws IOException {
                        interestingOutputCallback.run();
                        return super.read(b);
                    }
                    public int read(byte[] b, int off, int len) throws IOException {
                        interestingOutputCallback.run();
                        return super.read(b, off, len);
                    }
                    public long skip(long n) throws IOException {
                        interestingOutputCallback.run();
                        return super.skip(n);
                    }
                };
            } catch (IOException e) {
                AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        thisProcess[0] = Thread.currentThread();
        // #58513: register a progress handle for the task too.
        handle[0] = ProgressHandleFactory.createHandle(displayName, new Cancellable() {
            public boolean cancel() {
                stopProcess(thisProcess[0]);
                return true;
            }
        }, new AbstractAction("Open Output Tab") { // XXX I18N
            public void actionPerformed(ActionEvent e) {
                io.select();
            }
        });
        handle[0].start();
        StopBuildingAction.registerProcess(thisProcess[0], displayName);
        interestingOutputCallbacksByThreadGroup.put(thisProcess[0].getThreadGroup(), interestingOutputCallback);
        ok = AntBridge.getInterface().run(buildFile, targetNames, in, out, err, properties, verbosity, displayName, interestingOutputCallback);
        
        } finally {
            if (io != null) {
                synchronized (freeTabs) {
                    freeTabs.put(io, displayName);
                }
            }
            if (thisProcess[0] != null) {
                StopBuildingAction.unregisterProcess(thisProcess[0]);
            }
            if (handle[0] != null) {
                handle[0].finish();
            }
        }
    }
    
    /** Try to stop a build. */
    static void stopProcess(Thread t) {
        AntBridge.getInterface().stop(t);
    }

}
