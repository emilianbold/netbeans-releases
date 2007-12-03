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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.model.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *  CsmFile analogue of CsmFileTaskFactory
 * 
 * This factory should be registered in the global lookup by listing its fully qualified
 * name in file <code>META-INF/services/org.netbeans.modules.cnd.modelimpl.csm.scheduling.CsmFileTaskFactory</code>.
 * 
 * @author Sergey Grinev
 */
public abstract class CsmFileTaskFactory {
    private final Map<FileObject, Runnable> file2Task = new HashMap<FileObject, Runnable>();
    private final Map<FileObject, CsmFile> file2csm = new HashMap<FileObject, CsmFile>();
    private final ProgressListener progressListener;

    protected CsmFileTaskFactory() {
        progressListener = new ProgressListener();
        CsmModelAccessor.getModel().addProgressListener(progressListener);
    }

    protected abstract Runnable createTask(FileObject file);

    protected abstract Collection<FileObject> getFileObjects();

    protected final void fileObjectsChanged() {
        final List<FileObject> currentFiles = new ArrayList(getFileObjects());

        WORKER.post(new Runnable() {

            public void run() {
                stateChangedImpl(currentFiles);
            }
        });
    }

    private void stateChangedImpl(List<FileObject> currentFiles) {
        Map<CsmFile, Runnable> toRemove = new HashMap<CsmFile, Runnable>();
        Map<CsmFile, Runnable> toAdd = new HashMap<CsmFile, Runnable>();

        synchronized (this) {
            List<FileObject> addedFiles = new ArrayList(currentFiles);
            List<FileObject> removedFiles = new ArrayList(file2Task.keySet());

            addedFiles.removeAll(file2Task.keySet());
            removedFiles.removeAll(currentFiles);

            //remove old tasks:
            for (FileObject r : removedFiles) {
                CsmFile source = file2csm.remove(r);

                if (source == null) {
                    //TODO: log
                    continue;
                }

                toRemove.put(source, file2Task.remove(r));
            }

            //add new tasks:
            for (FileObject fileObject : addedFiles) {
                if (fileObject == null) {
                    continue;
                }
                if (!fileObject.isValid()) {
                    continue;
                }
                CsmFile csmFile = CsmUtilities.getCsmFile(fileObject, false);

                if (csmFile != null) {
                    Runnable task = createTask(fileObject);

                    toAdd.put(csmFile, task);

                    file2Task.put(fileObject, task);
                    file2csm.put(fileObject, csmFile);
                }
            }
        }


        for (Entry<CsmFile, Runnable> e : toRemove.entrySet()) {
            scheduler.removeParseCompletionTask(e.getKey(), e.getValue());
        }

        for (Entry<CsmFile, Runnable> e : toAdd.entrySet()) {
            try {
                scheduler.addParseCompletionTask(e.getKey(), e.getValue());
            } catch (IOException ex) {

            }
        }
    }

    protected final synchronized void reschedule(FileObject file) throws IllegalArgumentException {
        CsmFile source = file2csm.get(file);

        if (source == null) {
            return;
        }

        Runnable task = file2Task.get(file);

        if (task == null) {
            return;
        }

        scheduler.rescheduleTask(source, task);
    }
    
    private static RequestProcessor WORKER = new RequestProcessor("CsmFileTaskFactory", 1);

    static {
        CsmFileTaskFactoryManager.ACCESSOR = new CsmFileTaskFactoryManager.Accessor() {

            public void fireChangeEvent(CsmFileTaskFactory f) {
                f.fileObjectsChanged();
            }
        };

        scheduler = new Scheduler();

    }

    private static class Scheduler {

        public void addParseCompletionTask(CsmFile js, Runnable task) throws IOException {
            System.err.println("addParseCompletionTask for " + js.getAbsolutePath());
            List<TaskPair> taskPairs = csmFile2task.get(js);
            if (taskPairs == null) {
                taskPairs = new ArrayList<TaskPair>();
            }
            RequestProcessor.Task rpTask = RequestProcessor.getDefault().create(task, true);
            taskPairs.add(new TaskPair(task, rpTask));
            csmFile2task.put(js, taskPairs); // do we need this?
        }

        public void removeParseCompletionTask(CsmFile js, Runnable task) {
            System.err.println("removeParseCompletionTask for " + js.getAbsolutePath());
            List<TaskPair> taskPairs = csmFile2task.get(js);
            assert taskPairs != null;
            for (TaskPair taskPair : taskPairs) {
                if (taskPair.task == task) {
                    taskPairs.remove(taskPair);
                    break;
                }
            }
            csmFile2task.put(js, taskPairs); // do we need this?*/
        }
        
        public void rescheduleTask(CsmFile js, Runnable task) {
            List<TaskPair> taskPairs = csmFile2task.get(js);
            for (TaskPair taskPair : taskPairs) {
                if (taskPair.task == task) {
                    reschedule(js, taskPair.rpTask);
                    break;
                }
            }
        }
        
        private void reschedule(CsmFile js, RequestProcessor.Task rpTask) {
            System.err.println("rescheduleTask for " + js.getAbsolutePath());
            rpTask.cancel();
            rpTask.run();
        }

        public void reschedule(CsmFile file) {
            List<TaskPair> tasks = csmFile2task.get(file);
            if (tasks != null) {
                for (TaskPair taskPair : tasks) {
                    reschedule(file, taskPair.rpTask);
                }

            }
        }
        //private final Map<CsmFile, List<Runnable>> csmFile2task = new HashMap<CsmFile, List<Runnable>>();
        private final Map<CsmFile, List<TaskPair>> csmFile2task = new HashMap<CsmFile, List<TaskPair>>();
        // temp map before task and rptask would be merged
        //private final Map<Runnable, RequestProcessor.Task> task2rpTask = new HashMap<Runnable, RequestProcessor.Task>();
        //private final Map<RequestProcessor.Task, Runnable> rpTask2task = new HashMap<RequestProcessor.Task, Runnable>();
        
        private final class TaskPair {

            TaskPair(Runnable task, RequestProcessor.Task rpTask) {
                this.task = task;
                this.rpTask = rpTask;
            }
            
            Runnable task;
            RequestProcessor.Task rpTask;
        }
    }
    static Scheduler scheduler;

    private static class ProgressListener extends CsmProgressAdapter {

        @Override
        public void fileParsingFinished(CsmFile file) {
            scheduler.reschedule(file);
        }
    }
}
