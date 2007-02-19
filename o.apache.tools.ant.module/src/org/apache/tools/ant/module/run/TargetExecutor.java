/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
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
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
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
    private static final Map<InputOutput,String> freeTabs = new WeakHashMap<InputOutput,String>();
    
    /**
     * Display names of currently active processes.
     */
    private static final Set<String> activeDisplayNames = new HashSet<String>();
    
    private AntProjectCookie pcookie;
    private InputOutput io;
    private OutputStream outputStream;
    private boolean ok = false;
    private int verbosity = AntSettings.getVerbosity ();
    private Map<String,String> properties = AntSettings.getProperties();
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
        } else if (pcookie.getFile() != null) {
            fileName = pcookie.getFile().getName();
        } else {
            fileName = ""; // last resort for #84874
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
    
    private static final Map<InputOutput,StopAction> stopActions = new HashMap<InputOutput,StopAction>();
    private static final Map<InputOutput,RerunAction> rerunActions = new HashMap<InputOutput,RerunAction>();

    private static final class StopAction extends AbstractAction {

        public Thread t;

        public StopAction() {
            setEnabled(false); // initially, until ready
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(TargetExecutor.class.getResource("/org/apache/tools/ant/module/resources/stop.gif"));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return NbBundle.getMessage(TargetExecutor.class, "TargetExecutor.StopAction.stop");
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            setEnabled(false); // discourage repeated clicking
            if (t != null) { // #84688
                stopProcess(t);
            }
        }

    }

    private static final class RerunAction extends AbstractAction implements FileChangeListener {

        private final AntProjectCookie pcookie;
        private final List<String> targetNames;
        private final int verbosity;
        private final Map<String,String> properties;

        public RerunAction(TargetExecutor prototype) {
            pcookie = prototype.pcookie;
            targetNames = prototype.targetNames;
            verbosity = prototype.verbosity;
            properties = prototype.properties;
            setEnabled(false); // initially, until ready
            FileObject script = pcookie.getFileObject();
            if (script != null) {
                script.addFileChangeListener(FileUtil.weakFileChangeListener(this, script));
            }
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(TargetExecutor.class.getResource("/org/apache/tools/ant/module/resources/rerun.png"));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return NbBundle.getMessage(TargetExecutor.class, "TargetExecutor.RerunAction.rerun");
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            try {
                TargetExecutor exec = new TargetExecutor(pcookie,
                        targetNames != null ? targetNames.toArray(new String[targetNames.size()]) : null);
                exec.setVerbosity(verbosity);
                exec.setProperties(properties);
                exec.execute();
            } catch (IOException x) {
                Logger.getLogger(TargetExecutor.class.getName()).log(Level.INFO, null, x);
            }
        }

        public void fileDeleted(FileEvent fe) {
            firePropertyChange("enabled", null, false); // NOI18N
        }

        public void fileFolderCreated(FileEvent fe) {}

        public void fileDataCreated(FileEvent fe) {}

        public void fileChanged(FileEvent fe) {}

        public void fileRenamed(FileRenameEvent fe) {}

        public void fileAttributeChanged(FileAttributeEvent fe) {}

        public boolean isEnabled() {
            // #84874: should be disabled in case the original Ant script is now gone.
            return super.isEnabled() && pcookie.getFileObject() != null && pcookie.getFileObject().isValid();
        }

    }

    /**
     * Actually start the process.
     */
    public ExecutorTask execute () throws IOException {
        String dn = getProcessDisplayName(pcookie, targetNames);
        if (activeDisplayNames.contains(dn)) {
            // Uniquify: "prj (targ) #2", "prj (targ) #3", etc.
            int i = 2;
            String testdn;
            do {
                testdn = NbBundle.getMessage(TargetExecutor.class, "TargetExecutor.uniquified", dn, i++);
            } while (activeDisplayNames.contains(testdn));
            dn = testdn;
        }
        assert !activeDisplayNames.contains(dn);
        displayName = dn;
        activeDisplayNames.add(displayName);
        
        final ExecutorTask task;
        synchronized (this) {
            // OutputWindow
            if (AntSettings.getAutoCloseTabs()) { // #47753
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
                        stopActions.remove(free);
                        rerunActions.remove(free);
                    }
                }
                freeTabs.clear();
            }
            }
            if (io == null) {
                StopAction sa = new StopAction();
                RerunAction ra = new RerunAction(this);
                io = IOProvider.getDefault().getIO(displayName, new Action[] {ra, sa});
                stopActions.put(io, sa);
                rerunActions.put(io, ra);
            }
            task = ExecutionEngine.getDefault().execute(null, this, InputOutput.NULL);
        }
        WrapperExecutorTask wrapper = new WrapperExecutorTask(task, io);
        RequestProcessor.getDefault().post(wrapper);
        return wrapper;
    }
    
    public ExecutorTask execute(OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        ExecutorTask task = ExecutionEngine.getDefault().execute(null, this, InputOutput.NULL);
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
            StopAction sa = stopActions.get(io);
            if (sa != null) {
                sa.actionPerformed(null);
            } else { // just in case
                task.stop();
            }
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
        final StopAction sa = stopActions.get(io);
        assert sa != null;
        RerunAction ra = rerunActions.get(io);
        assert ra != null;
        try {
            
        final boolean[] displayed = new boolean[] {AntSettings.getAlwaysShowOutput()};
        
        if (outputStream == null) {
            if (displayed[0]) {
                io.select();
            }
        }
        
        if (AntSettings.getSaveAll()) {
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
        StopBuildingAction.registerProcess(thisProcess[0], displayName);
        sa.t = thisProcess[0];
	    // #58513, #87801: register a progress handle for the task too.
        ProgressHandle handle = ProgressHandleFactory.createHandle(displayName, new Cancellable() {
            public boolean cancel() {
                sa.actionPerformed(null);
                return true;
            }
        }, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                io.select();
            }
        });
        handle.setInitialDelay(0); // #92436
        handle.start();
        sa.setEnabled(true);
        ra.setEnabled(false);
        ok = AntBridge.getInterface().run(buildFile, targetNames, in, out, err, properties, verbosity, displayName, interestingOutputCallback, handle);
        
        } finally {
            if (io != null) {
                synchronized (freeTabs) {
                    freeTabs.put(io, displayName);
                }
            }
            if (thisProcess[0] != null) {
                StopBuildingAction.unregisterProcess(thisProcess[0]);
            }
            sa.t = null;
            sa.setEnabled(false);
            ra.setEnabled(true);
            activeDisplayNames.remove(displayName);
        }
    }
    
    /** Try to stop a build. */
    static void stopProcess(Thread t) {
        AntBridge.getInterface().stop(t);
    }

}
