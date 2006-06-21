/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.run;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.io.ReaderInputStream;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.w3c.dom.Element;

/** Executes an Ant Target asynchronously in the IDE.
 */
public final class TargetExecutor implements Runnable {
    
    private static final boolean USE_PROGRESS = Boolean.getBoolean("org.apache.tools.ant.module.run.TargetExecutor.USE_PROGRESS");
    
    /**
     * All tabs which were used for some process which has now ended.
     * These are closed when you start a fresh process.
     * Map from tab to tab display name.
     * @see "#43001"
     */
    private static final Map<InputOutput,String> freeTabs = new WeakHashMap<InputOutput,String>();
    
    private AntProjectCookie pcookie;
    private InputOutput io;
    private OutputStream outputStream;
    private boolean ok = false;
    private int verbosity = AntSettings.getDefault ().getVerbosity ();
    private Map<String,String> properties = NbCollections.checkedMapByCopy(AntSettings.getDefault().getProperties(), String.class, String.class, true);
    private List<String> targetNames;
    /** used for the tab etc. */
    private String displayName;

    /** targets may be null to indicate default target */
    public TargetExecutor (AntProjectCookie pcookie, String[] targets) {
        this.pcookie = pcookie;
        targetNames = ((targets == null) ? null : Arrays.asList(targets));
    }
  
    public void setVerbosity (int v) {
        verbosity = v;
    }
    
    public synchronized void setProperties(Map<String,String> p) {
        properties = new HashMap<String,String>(p);
    }
    
    static String getProcessDisplayName(AntProjectCookie pcookie, List<String> targetNames) {
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
            Iterator<String> it = targetNames.iterator();
            if (it.hasNext()) {
                targetList.append(it.next());
            }
            while (it.hasNext()) {
                targetList.append(NbBundle.getMessage(TargetExecutor.class, "SEP_output_target"));
                targetList.append(it.next());
            }
            return NbBundle.getMessage(TargetExecutor.class, "TITLE_output_target", projectName, fileName, targetList);
        } else {
            return NbBundle.getMessage(TargetExecutor.class, "TITLE_output_notarget", projectName, fileName);
        }
    }
    
    /**
     * Actually start the process.
     */
    public ExecutorTask execute () throws IOException {
        displayName = getProcessDisplayName(pcookie, targetNames);
        
        final ExecutorTask task;
        synchronized (this) {
            // OutputWindow
            if (AntSettings.getDefault().getAutoCloseTabs()) { // #47753
            synchronized (freeTabs) {
                for (Map.Entry<InputOutput,String> entry : freeTabs.entrySet()) {
                    InputOutput free = entry.getKey();
                    String freeName = entry.getValue();
                    if (io == null && freeName.equals(displayName)) {
                        // Reuse it.
                        io = free;
                        io.getOut().reset();
                        // Apparently useless and just prints warning: io.getErr().reset();
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
        @Override
        public void stop () {
            // XXX this should call stopProcess instead (if it can find the Thread somewhere)
            task.stop ();
        }
        @Override
        public int result () {
            return task.result () + (ok ? 0 : 1);
        }
        @Override
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
        
        LastTargetExecuted.record(buildFile, verbosity, targetNames != null ? targetNames.toArray(new String[targetNames.size()]) : null, properties);
        
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
                    @Override
                    public int read() throws IOException {
                        interestingOutputCallback.run();
                        return super.read();
                    }
                    @Override
                    public int read(byte[] b) throws IOException {
                        interestingOutputCallback.run();
                        return super.read(b);
                    }
                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
                        interestingOutputCallback.run();
                        return super.read(b, off, len);
                    }
                    @Override
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
	if (USE_PROGRESS) { // XXX #63332: off by default
	    // #58513: register a progress handle for the task too.
	    handle[0] = ProgressHandleFactory.createHandle(displayName, new Cancellable() {
		public boolean cancel() {
		    stopProcess(thisProcess[0]);
		    return true;
		}
	    }, new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    io.select();
		}
	    });
	    handle[0].start();
	}
        StopBuildingAction.registerProcess(thisProcess[0], displayName);
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
