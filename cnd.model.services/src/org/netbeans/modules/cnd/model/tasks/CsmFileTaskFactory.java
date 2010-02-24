/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.model.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
import org.netbeans.modules.cnd.model.tasks.CsmFileTaskFactory.PhaseRunner.Phase;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *  CsmFile analogue of CsmFileTaskFactory
 *
 * This factory should be registered in the global lookup using {@link org.openide.util.lookup.ServiceProvider}.
 *
 * @author Sergey Grinev
 */
public abstract class CsmFileTaskFactory {

    private final Map<FileObject, TaskData> fobj2task = new ConcurrentHashMap<FileObject, TaskData>();
    private final ProgressListener progressListener = new ProgressListener();
    private final ModelListener modelListener = new ModelListener();
    private static final class FileTaskFactoryLock {}
    private final Object fileTaskFactoryLock = new FileTaskFactoryLock();
    private static final int IMMEDIATELY = 0;

    // processors below should have FileTaskFactory in their names, because name is checked
    // to suppress warnings in org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml
    private static RequestProcessor WORKER = new RequestProcessor("CsmFileTaskFactory", 1); //NOI18N
    private static RequestProcessor HIGH_PRIORITY_WORKER = new RequestProcessor("CsmHighPriorityFileTaskFactory", 1); //NOI18N
    private static RequestProcessor DECISION_WORKER = new RequestProcessor("CsmDecisionFileTaskFactory", 1); //NOI18N

    public final static String USE_OWN_CARET_POSITION = "use-own-caret-position"; // NOI18N

    static {
        CsmFileTaskFactoryManager.ACCESSOR = new CsmFileTaskFactoryManager.Accessor() {
            public void fireChangeEvent(CsmFileTaskFactory f) {
                f.fileObjectsChanged();
            }
        };
    }

    protected CsmFileTaskFactory() {
        CsmListeners.getDefault().addProgressListener(progressListener);
        CsmListeners.getDefault().addModelListener(modelListener);
    }

    protected abstract PhaseRunner createTask(FileObject fo);

    protected abstract Collection<FileObject> getFileObjects();

    protected abstract int taskDelay();

    protected abstract int rescheduleDelay();

    protected final void fileObjectsChanged() {
        final Set<FileObject> currentFiles = new HashSet<FileObject>(getFileObjects());
        final long id = Math.round(100.0*Math.random());
        final String name = this.getClass().getName();
        if (OpenedEditors.SHOW_TIME) {System.err.println("CsmFileTaskFactory: POST worker " + id);}
        DECISION_WORKER.post(new Runnable() {

            public void run() {
                long start = System.currentTimeMillis();
                if (OpenedEditors.SHOW_TIME) {System.err.println("CsmFileTaskFactory: RUN worker " + id + " [" + name + "]" );}
                stateChangedImpl(currentFiles);
                if (OpenedEditors.SHOW_TIME) {System.err.println("CsmFileTaskFactory: DONE worker " + id + " after " + (System.currentTimeMillis() - start) + "ms.");}
            }
        });
    }

    public final void reschedule(final FileObject file) throws IllegalArgumentException {
        postDecision(new Runnable() {
            public void run() {
                runTask(file, PhaseRunner.Phase.PARSED, rescheduleDelay());
            }
        });
    }

    private final void postDecision(Runnable runnable){
        DECISION_WORKER.post(runnable);
    }

    private boolean checkMimeType(FileObject fileObject) {
        if (fileObject == null) {
            return false;
        } else {
            String mimeType = fileObject.getMIMEType();
            return MIMENames.isHeaderOrCppOrC(mimeType);
        }
    }

    private void stateChangedImpl(Collection<FileObject> currentFiles) {
        Map<FileObject, TaskData> toRemove = new HashMap<FileObject, TaskData>();
        Map<FileObject, TaskData> toAdd = new HashMap<FileObject, TaskData>();

        synchronized (fileTaskFactoryLock) {
            List<FileObject> addedFiles = new ArrayList<FileObject>(currentFiles);
            List<FileObject> removedFiles = new ArrayList<FileObject>(fobj2task.keySet());

            addedFiles.removeAll(fobj2task.keySet());
            removedFiles.removeAll(currentFiles);

            //remove old tasks:
            for (FileObject r : removedFiles) {
                toRemove.put(r, fobj2task.remove(r));
            }

            List<FileObject> verifiedFiles = new ArrayList<FileObject>(fobj2task.keySet());
            // Model events should be redesigned. It is still inconvenient for clients. Move to lookup of DataObject direction. Stop hacking!
            // verify rest task
            for (FileObject v : verifiedFiles) {
                if (v == null) {
                    continue;
                }
                if (!v.isValid()) {
                    continue;
                }
                if (!checkMimeType(v)) {
                    continue;
                }

                CsmFile csmFile = getCsmFile(v, true);
                if (csmFile != null) {
                    TaskData oldTaskData = fobj2task.get(v);
                    if (!csmFile.equals(oldTaskData.file)) {
                        toRemove.put(v, fobj2task.remove(v));

                        PhaseRunner task = createTask(v);
                        TaskData data = new TaskData(task, csmFile);
                        toAdd.put(v, data);
                        fobj2task.put(v, data);
                    }
                }
            }

            //add new tasks:
            for (FileObject fileObject : addedFiles) {
                if (fileObject == null) {
                    continue;
                }
                if (!fileObject.isValid()) {
                    continue;
                }
                if (!checkMimeType(fileObject)) {
                    continue;
                }

                CsmFile csmFile = getCsmFile(fileObject, true);
                if (csmFile != null) {
                    PhaseRunner task = createTask(fileObject);
                    TaskData data = new TaskData(task, csmFile);
                    toAdd.put(fileObject, data);
                    fobj2task.put(fileObject, data);
                }
            }
        }

        for (Entry<FileObject, TaskData> e : toRemove.entrySet()) {
            CsmFile csmFile = getCsmFile(e.getKey(), false);
            if (csmFile != null) {
                if (OpenedEditors.SHOW_TIME) {System.err.println("CFTF: removing " + csmFile.getAbsolutePath());}
            }
            if (e!=null && e.getValue()!=null ) {
                PhaseRunner runner = e.getValue().runner;
                Task task = e.getValue().task;
                runner.cancel();
                if (task != null) {
                    task.cancel();
                }
                post(e.getValue(), e.getKey(), PhaseRunner.Phase.CLEANUP, IMMEDIATELY);
            }
            // it isn't necessary to check mime type here -
            // we checked it when adding task
            if (csmFile != null) {
                CsmStandaloneFileProvider.getDefault().notifyClosed(csmFile);
            }
        }

        for (Entry<FileObject, TaskData> e : toAdd.entrySet()) {
            CsmFile csmFile = getCsmFile(e.getKey(), false);
            if (csmFile != null) {
                if (OpenedEditors.SHOW_TIME) {System.err.println("CFTF: adding "+ //NOI18N
                        (csmFile.isParsed() ? PhaseRunner.Phase.PARSED : PhaseRunner.Phase.INIT)+
                        " "+e.getValue().runner.toString()+" " + csmFile.getAbsolutePath());} //NOI18N
                post(e.getValue(), e.getKey(), csmFile.isParsed() ? PhaseRunner.Phase.PARSED : PhaseRunner.Phase.INIT, taskDelay());
            }
        }
    }

    private static CsmFile getCsmFile(FileObject fo, boolean allowStandalone) {
        CsmFile csmFile = null;
        if (fo != null) {
            Document doc = CsmUtilities.getDocument(fo);
            if (doc != null) {
                csmFile = CsmUtilities.getCsmFile(doc, false, false);
            }
            if (csmFile == null) {
                csmFile = CsmUtilities.getCsmFile(fo, false, false);
            }
            if (allowStandalone && csmFile == null) {
                csmFile = CsmStandaloneFileProvider.getDefault().getCsmFile(fo);
            }
        }
        return csmFile;
    }

    private void runAllTasks(PhaseRunner.Phase phase, int delay) {
        for (FileObject fo : fobj2task.keySet()) {
            runTask(fo, phase, delay);
        }
    }

   private final void runTask(CsmFile eventCsm, PhaseRunner.Phase phase, int delay) {
        if (fobj2task.isEmpty()) {
            return;
        }
        FileObject fobj = null;
        TaskData pr = null;
        for (Map.Entry<FileObject, TaskData> entry : fobj2task.entrySet()){
            if (eventCsm.equals(entry.getValue().file)){
                fobj = entry.getKey();
                pr = entry.getValue();
                break;
            }
        }
        if (pr == null) {
            return;
        }
        _runTask(pr, fobj, phase, delay);
   }

    private final void runTask(FileObject eventFobj, PhaseRunner.Phase phase, int delay) {
        TaskData pr = fobj2task.get(eventFobj);
        if (pr == null) {
            return;
        }
        _runTask(pr, eventFobj, phase, delay);
    }

    private final void _runTask(TaskData pr, FileObject fobj, Phase phase, int delay) {
        pr.runner.cancel();
        if (pr.task != null) {
            pr.task.cancel();
        }
        if (!pr.runner.isValid()) {
            PhaseRunner runner = createTask(fobj);
            assert runner.isValid();
            pr = new TaskData(runner, getCsmFile(fobj, false));
            synchronized (fileTaskFactoryLock) {
                fobj2task.put(fobj, pr);
            }
        }
        // Run the same task for related document if it exists
        Document doc = CsmUtilities.getDocument(fobj);
        if (doc != null) {
            Document doc2 = (Document) doc.getProperty(Document.class);
            if (doc2 != null) {
                FileObject fobj2 = CsmUtilities.getFileObject(doc2);
                if (fobj2 != null) {
                    PhaseRunner task = createTask(fobj2);
                    TaskData data = new TaskData(task, getCsmFile(fobj2, false));
                    doc2.putProperty(USE_OWN_CARET_POSITION, false);
                    doc.putProperty(USE_OWN_CARET_POSITION, true);
                    if (data != null) {
                        post(data, fobj2, phase, delay);
                    }
                }
            }
        }
        post(pr, fobj, phase, delay);
    }

    private final void post(TaskData pr, FileObject fo, PhaseRunner.Phase phase, int delay) {
        if (pr.runner.isHighPriority()) {
            pr.task = HIGH_PRIORITY_WORKER.post(new CsmSafeRunnable(getRunnable(pr.runner, phase), fo), delay, Thread.NORM_PRIORITY);
        } else {
            pr.task = WORKER.post(new CsmSafeRunnable(getRunnable(pr.runner, phase), fo), delay);
        }
    }

    private class ProgressListener extends CsmProgressAdapter {

        @Override
        public void fileParsingFinished(final CsmFile file) {
            postDecision(new Runnable() {
                public void run() {
                    runTask(file, PhaseRunner.Phase.PARSED, IMMEDIATELY);
                }
            });
        }

        @Override
        public void fileParsingStarted(final CsmFile file) {
            postDecision(new Runnable() {
                public void run() {
                    runTask(file, PhaseRunner.Phase.PARSING_STARTED, IMMEDIATELY);
                }
            });
        }

        @Override
        public void projectParsingFinished(CsmProject project) {
            postDecision(new Runnable() {
                public void run() {
                    runAllTasks(PhaseRunner.Phase.PROJECT_PARSED, IMMEDIATELY);
                }
            });
        }

    }

    private class ModelListener implements CsmModelListener {

        public void projectOpened(CsmProject project) {
            // do nothing
        }

        public void projectClosed(CsmProject project) {
            // TODO: do something? Cleanup, maybe?
        }

        public void modelChanged(final CsmChangeEvent e) {
            if (!e.getRemovedFiles().isEmpty()){
                postDecision(new Runnable() {
                    public void run() {
                        for (CsmFile f : e.getRemovedFiles()){
                            FileObject fobj = CsmUtilities.getFileObject(f);
                            if (fobj != null) {
                                if (fobj2task.get(fobj) != null) {
                                    Document doc = CsmUtilities.getDocument(fobj);
                                    if (doc != null) {
                                        synchronized (fileTaskFactoryLock) {
                                            runTask(fobj, PhaseRunner.Phase.CLEANUP, IMMEDIATELY);
                                            fobj2task.put(fobj, new TaskData(lazyRunner(), CsmUtilities.getCsmFile(doc, false, false)));
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
            if (!e.getNewFiles().isEmpty()){
                fileObjectsChanged();
            }
        }
    }

    public static interface PhaseRunner {
        public enum Phase {
            INIT,
            PARSING_STARTED,
            PARSED,
            PROJECT_PARSED,
            CLEANUP
        };
        public abstract void run(Phase phase);
        public abstract boolean isValid();
        public abstract void cancel();
        public abstract boolean isHighPriority();
    }

    protected static PhaseRunner lazyRunner() {
        return new PhaseRunner() {
            public void run(Phase phase) {
                // do nothing for all phases
            }

            public boolean isValid() {
                return true;
            }

            public void cancel() {
            }

            public boolean isHighPriority() {
                return false;
            }
        };
    }

    private static final class TaskData {
        private final PhaseRunner runner;
        private final CsmFile file;
        private Task task;
        private TaskData(PhaseRunner runner, CsmFile file) {
            this.runner = runner;
            this.file = file;
        }
    }

    private static final Runnable getRunnable(final PhaseRunner pr, final PhaseRunner.Phase phase) {
        return new Runnable() {
            public void run() {
                pr.run(phase);
            }
        };
    }

    private static final class CsmSafeRunnable implements Runnable {
        private FileObject fileObject;
        private Runnable run;
        public CsmSafeRunnable(Runnable run, FileObject fileObject) {
            this.run = run;
            this.fileObject = fileObject;
        }

        public void run() {
            CsmFile file = getCsmFile(fileObject, false);
            if (file !=  null && file.isValid() /*&& (file.isHeaderFile() || file.isSourceFile())*/) {
                run.run();
            }
        }
    }

}
