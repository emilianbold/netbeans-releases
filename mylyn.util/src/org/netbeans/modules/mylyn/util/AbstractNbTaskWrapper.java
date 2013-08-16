/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mylyn.util;

import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Ondrej Vrabec
 */
public abstract class AbstractNbTaskWrapper {
    
    static final String ATTR_NEW_UNREAD = "NetBeans.task.markedNewUnread"; //NOI18N
    private static final Object MODEL_LOCK = new Object();
    private static final Logger LOG = Logger.getLogger(AbstractNbTaskWrapper.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("NBTasks"); //NOI18N

    private NbTask task;
    private NbTaskDataModel model;
    private NbTaskDataModel.NbTaskDataModelListener list;
    private boolean readPending;
    private final TaskDataListenerImpl taskDataListener;
    private final TaskListenerImpl taskListener;
    private Reference<TaskData> repositoryDataRef;
    private final RequestProcessor.Task repositoryTaskDataLoaderTask;

    public AbstractNbTaskWrapper (NbTask task) {
        this.task = task;
        this.repositoryDataRef = new SoftReference<TaskData>(null);
        repositoryTaskDataLoaderTask = RP.create(new Runnable() {
            @Override
            public void run () {
                loadRepositoryTaskData();
            }
        });
        MylynSupport mylynSupp = MylynSupport.getInstance();
        taskDataListener = new TaskDataListenerImpl();
        mylynSupp.addTaskDataListener(WeakListeners.create(TaskDataListener.class, taskDataListener, mylynSupp));
        taskListener = new TaskListenerImpl();
        task.addNbTaskListener(WeakListeners.create(NbTaskListener.class, taskListener, mylynSupp));
    }

    /**
     * Returns the id of the given task or null if task is new
     * @param task
     * @return id or null
     */
    public static String getID (NbTask task) {
        if (task.getSynchronizationState() == NbTask.SynchronizationState.OUTGOING_NEW) {
            return "-" + task.getTaskId();
        }
        return task.getTaskId();
    }

    protected final TaskData getRepositoryTaskData () {
        TaskData taskData = repositoryDataRef.get();
        if (taskData == null) {
            if (EventQueue.isDispatchThread()) {
                repositoryTaskDataLoaderTask.schedule(100);
            } else {
                return loadRepositoryTaskData();
            }
        }
        return taskData;
    }

    private TaskData loadRepositoryTaskData () {
        // this method is time consuming
        assert !EventQueue.isDispatchThread();
        TaskData td = repositoryDataRef.get();
        if (td != null) {
            return td;
        }
        try {
            NbTaskDataState taskDataState = task.getTaskDataState();
            if (taskDataState != null) {
                td = taskDataState.getRepositoryData();
                repositoryDataRef = new SoftReference<TaskData>(td);
                repositoryTaskDataLoaded(td);
            }
        } catch (CoreException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return td;
    }

    protected final void deleteTask () {
        assert task.getSynchronizationState() == NbTask.SynchronizationState.OUTGOING_NEW : "Only new local tasks can be deleted: " + task.getSynchronizationState();
        synchronized (MODEL_LOCK) {
            if (list != null) {
                model.removeNbTaskDataModelListener(list);
                list = null;
            }
            model = null;
        }
        MylynSupport mylynSupp = MylynSupport.getInstance();
        mylynSupp.removeTaskDataListener(taskDataListener);
        task.removeNbTaskListener(taskListener);
        if (task.getSynchronizationState() == NbTask.SynchronizationState.OUTGOING_NEW) {
            task.delete();
            taskDeleted(task);
        }
    }

    protected abstract void taskDeleted (NbTask task);

    public final boolean isMarkedNewUnread () {
        return isNew() && Boolean.TRUE.toString().equals(task.getAttribute(ATTR_NEW_UNREAD));
    }

    public final boolean isNew () {
        return task.isNew();
    }

    protected final void markNewRead () {
        task.setAttribute(ATTR_NEW_UNREAD, null);
    }

    public final void setUpToDate (boolean seen, boolean markReadPending) {
        synchronized (MODEL_LOCK) {
            if (markReadPending) {
                // this is a workaround to keep incoming changes visible in editor
                NbTask.SynchronizationState syncState = task.getSynchronizationState();
                readPending |= syncState == NbTask.SynchronizationState.INCOMING
                        || syncState == NbTask.SynchronizationState.CONFLICT;
            } else {
                readPending = false;
            }
            task.markSeen(seen);
        }
    }

    protected final void editorOpened () {
        list = new NbTaskDataModel.NbTaskDataModelListener() {
            @Override
            public void attributeChanged (NbTaskDataModel.NbTaskDataModelEvent event) {
                NbTaskDataModel m = model;
                if (event.getModel() == m) {
                    AbstractNbTaskWrapper.this.attributeChanged(event, m);
                }
            }
        };
        if (task.getSynchronizationState() == NbTask.SynchronizationState.INCOMING_NEW) {
            // mark as seen so no fields are highlighted
            setUpToDate(true, false);
        }
        // clear upon close
        synchronized (MODEL_LOCK) {
            if (readPending) {
                // make sure remote changes are not lost and still highlighted in the editor
                setUpToDate(false, false);
            }
            model = task.getTaskDataModel();
            model.addNbTaskDataModelListener(list);
        }
    }

    protected final void editorClosed () {
        final NbTaskDataModel m = model;
        final boolean markedAsNewUnread = isMarkedNewUnread();
        if (m != null) {
            if (list != null) {
                m.removeNbTaskDataModelListener(list);
                list = null;
            }
            readPending = false;
            if (markedAsNewUnread) {
                // was not modified by user and not yet saved
                deleteTask();
            } else {
                synchronized (MODEL_LOCK) {
                    if (model == m) {
                        model = null;
                    }
                }
                if (m.isDirty()) {
                    try {
                        save();
                    } catch (CoreException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                }
            }
        }
    }

    protected final void runWithModelLoaded (Runnable runnable) {
        synchronized (MODEL_LOCK) {
            boolean closeModel = false;
            try {
                if (model == null) {
                    closeModel = true;
                    model = task.getTaskDataModel();
                }
                runnable.run();
            } finally {
                if (closeModel) {
                    if (model != null && model.isDirty()) {
                        try {
                            // let's not loose edits
                            model.save();
                        } catch (CoreException ex) {
                            LOG.log(Level.INFO, null, ex);
                        }
                    }
                    model = null;
                }
            }
        }
    }

    protected abstract void attributeChanged (NbTaskDataModel.NbTaskDataModelEvent event, NbTaskDataModel model);

    private void save () throws CoreException {
        NbTaskDataModel m = this.model;
        markNewRead();
        if (m.isDirty()) {
            if (isNew()) {
                String summary = task.getSummary();
                String newSummary = getSummary(m.getLocalTaskData());
                if (newSummary != null && !(newSummary.isEmpty() || newSummary.equals(summary))) {
                    task.setSummary(newSummary);
                    taskModified(false);
                }
            }
            m.save();
            modelSaved(m);
        }
    }
    
    protected final void taskSubmitted (NbTask task) {
        if (task != null && task != this.task) {
            this.task.removeNbTaskListener(taskListener);
            this.task = task;
            task.addNbTaskListener(taskListener);
            synchronized (MODEL_LOCK) {
                if (list != null) {
                    model.removeNbTaskDataModelListener(list);
                }
                model = task.getTaskDataModel();
                repositoryDataRef.clear();
                if (list != null) {
                    model.addNbTaskDataModelListener(list);
                }
            }
        }
    }

    protected final boolean saveChanges () {
        try {
            save();
            return true;
        } catch (CoreException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return false;
    }

    public final boolean cancelChanges () {
        try {
            if (saveChanges()) {
                task.discardLocalEdits();
                model.refresh();
                return true;
            }
        } catch (CoreException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return false;
    }

    public final boolean hasLocalEdits () {
        NbTaskDataModel m = model;
        return !(m == null || m.getChangedAttributes().isEmpty());
    }

    protected final boolean updateModel () {
        try {
            model.refresh();
            return true;
        } catch (CoreException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        return false;
    }

    protected abstract void modelSaved (NbTaskDataModel model);

    protected abstract String getSummary (TaskData taskData);

    protected abstract void taskDataUpdated ();

    protected final boolean isSeen () {
        NbTask.SynchronizationState syncState = task.getSynchronizationState();
        return syncState == NbTask.SynchronizationState.OUTGOING
                || syncState == NbTask.SynchronizationState.OUTGOING_NEW
                || syncState == NbTask.SynchronizationState.SYNCHRONIZED;
    }

    protected abstract void taskModified (boolean syncStateChanged);

    protected final NbTaskDataModel getModel () {
        return model;
    }

    protected abstract void repositoryTaskDataLoaded (TaskData repositoryTaskData);

    protected final NbTask getNbTask () {
        return task;
    }

    public final long getCreated () {
        Date createdDate = getCreatedDate();
        if (createdDate != null) {
            return createdDate.getTime();
        } else {
            return -1;
        }
    }

    public final Date getCreatedDate () {
        return task.getCreationDate();
    }

    public final long getLastModify () {
        Date lastModifyDate = getLastModifyDate();
        if (lastModifyDate != null) {
            return lastModifyDate.getTime();
        } else {
            return -1;
        }
    }

    public final Date getLastModifyDate () {
        return task.getModificationDate();
    }

    public final String getSummary () {
        return task.getSummary();
    }

    public final String getID () {
        return getID(task);
    }

    public final boolean isFinished () {
        return task.isCompleted();
    }

    public final IssueStatusProvider.Status getStatus () {
        return getNbStatus();
    }

    protected final NbTask.SynchronizationState getSynchronizationState () {
        return task.getSynchronizationState();
    }

    public final IssueStatusProvider.Status getNbStatus () {
        switch (getSynchronizationState()) {
            case CONFLICT:
            case INCOMING:
                return IssueStatusProvider.Status.MODIFIED;
            case INCOMING_NEW:
                return IssueStatusProvider.Status.NEW;
            case OUTGOING:
            case OUTGOING_NEW:
            case SYNCHRONIZED:
                return IssueStatusProvider.Status.SEEN;
        }
        return null;
    }

    private class TaskDataListenerImpl implements TaskDataListener {

        @Override
        public void taskDataUpdated (TaskDataListener.TaskDataEvent event) {
            if (event.getTask() == task) {
                if (event.getTaskData() != null && !event.getTaskData().isPartial()) {
                    repositoryDataRef = new SoftReference<TaskData>(event.getTaskData());
                }
                if (event.getTaskDataUpdated()) {
                    NbTaskDataModel m = model;
                    if (m != null) {
                        try {
                            m.refresh();
                        } catch (CoreException ex) {
                            LOG.log(Level.INFO, null, ex);
                        }
                    }
                    AbstractNbTaskWrapper.this.taskDataUpdated();
                }
            }
        }
    }

    private class TaskListenerImpl implements NbTaskListener {

        @Override
        public void taskModified (NbTaskListener.TaskEvent event) {
            if (event.getTask() == task && event.getKind() == NbTaskListener.TaskEvent.Kind.MODIFIED) {
                boolean syncStateChanged = event.taskStateChanged();
                AbstractNbTaskWrapper.this.taskModified(syncStateChanged);
            }
        }

    }
}
