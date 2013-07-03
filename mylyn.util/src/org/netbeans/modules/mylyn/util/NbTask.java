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

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.tasks.core.TaskContainerDelta;
import org.eclipse.mylyn.tasks.core.ITask;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.mylyn.util.internal.TaskListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author Ondrej Vrabec
 */
public final class NbTask {
    private final ITask delegate;
    private final TaskListenerImpl list;
    private final List<NbTaskListener> listeners;
    private SynchronizationState syncState;
    
    NbTask (ITask task) {
        this.delegate = task;
        this.listeners = new CopyOnWriteArrayList<NbTaskListener>();
        updateSynchronizationState();
        list = new TaskListenerImpl();
        MylynSupport.getInstance().addTaskListener(task, WeakListeners.create(TaskListener.class,
                list,
                MylynSupport.getInstance()));
    }

    public SynchronizationState getSynchronizationState () {
        return syncState;
    }

    public boolean isOutgoing () {
        SynchronizationState state = getSynchronizationState();
        return state == SynchronizationState.CONFLICT
                || state == SynchronizationState.OUTGOING
                || state == SynchronizationState.OUTGOING_NEW;
    }

    public String getTaskId () {
        return delegate.getTaskId();
    }

    ITask getDelegate () {
        return delegate;
    }

    public void markSeen (boolean seen) {
        MylynSupport.getInstance().markTaskSeen(getDelegate(), seen);
    }

    public void delete () {
        MylynSupport.getInstance().deleteTask(getDelegate());
    }
    
    public void discardLocalEdits () throws CoreException {
        MylynSupport.getInstance().discardLocalEdits(delegate);
    }

    public void setAttribute (String attributeName, String attributeValue) {
        delegate.setAttribute(attributeName, attributeValue);
    }

    public String getAttribute (String attributeName) {
        return delegate.getAttribute(attributeName);
    }

    @Override
    public String toString () {
        return delegate.toString();
    }

    @Override
    public int hashCode () {
        return delegate.hashCode();
    }

    @Override
    public boolean equals (Object obj) {
        if (obj instanceof NbTask) {
            return delegate.equals(((NbTask) obj).delegate);
        } else {
            return false;
        }
    }

    public String getSummary () {
        return delegate.getSummary();
    }

    public Date getModificationDate () {
        return delegate.getModificationDate();
    }

    public Date getCreationDate () {
        return delegate.getCreationDate();
    }

    public boolean isCompleted () {
        return delegate.isCompleted();
    }

    public IssueStatusProvider.Status getNbStatus () {
        switch (delegate.getSynchronizationState()) {
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

    public void setSummary (String summary) {
        delegate.setSummary(summary);
    }

    /**
     * Adds a listener to the tasklist. The listener will be notified on a change
     * in tasks's content (summary, description, etc.).
     *
     * @param listener listener
     */
    public void addNbTaskListener (NbTaskListener listener) {
        listeners.add(listener);
    }

    public void removeNbTaskListener (NbTaskListener listener) {
        listeners.remove(listener);
    }

    public String getRepositoryUrl () {
        return delegate.getRepositoryUrl();
    }

    private void updateSynchronizationState () {
        switch (delegate.getSynchronizationState()) {
            case CONFLICT:
                syncState = SynchronizationState.CONFLICT;
                break;
            case INCOMING:
                syncState = SynchronizationState.INCOMING;
                break;
            case INCOMING_NEW:
                syncState = SynchronizationState.INCOMING_NEW;
                break;
            case OUTGOING:
                syncState = SynchronizationState.OUTGOING;
                break;
            case OUTGOING_NEW:
                syncState = SynchronizationState.OUTGOING_NEW;
                break;
            case SYNCHRONIZED:
            default:
                syncState = SynchronizationState.SYNCHRONIZED;
                break;
        }
    }

    public static enum SynchronizationState {
        INCOMING_NEW,
        INCOMING,
        OUTGOING_NEW,
        OUTGOING,
        SYNCHRONIZED,
        CONFLICT
    }

    private class TaskListenerImpl implements TaskListener {

        public TaskListenerImpl () {
        }

        @Override
        public void taskModified (ITask task, TaskContainerDelta delta) {
            assert task == NbTask.this.delegate;
            SynchronizationState oldState = getSynchronizationState();
            updateSynchronizationState();
            NbTaskListener.TaskEvent ev = new NbTaskListener.TaskEvent(NbTask.this, delta,
                    oldState != getSynchronizationState());
            for (NbTaskListener list : listeners.toArray(new NbTaskListener[0])) {
                list.taskModified(ev);
            }
        }
    }
    
}
