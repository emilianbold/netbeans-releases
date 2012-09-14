/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;
import org.netbeans.editor.BaseDocument;

/**
 * Registration of tasks performed right before document save.
 *
 * @author Miloslav Metelka
 * @since 1.9
 */
public final class BeforeSaveTasks {
    
    private static final Logger LOG = Logger.getLogger(BeforeSaveTasks.class.getName());

    private static final List<Task> tasks = new ArrayList<Task>(5);
    
    public static synchronized BeforeSaveTasks get(BaseDocument doc) {
        BeforeSaveTasks beforeSaveTasks = (BeforeSaveTasks) doc.getProperty(BeforeSaveTasks.class);
        if (beforeSaveTasks == null) {
            beforeSaveTasks = new BeforeSaveTasks(doc);
            doc.putProperty(BeforeSaveTasks.class, beforeSaveTasks);
        }
        return beforeSaveTasks;
    }
    
    private final BaseDocument doc;

    private BeforeSaveTasks(BaseDocument doc) {
        this.doc = doc;
        Runnable beforeSaveRunnable = (Runnable)
                doc.getProperty("beforeSaveRunnable"); // Name of prop in sync with CloneableEditorSupport NOI18N
        if (beforeSaveRunnable != null) {
            throw new IllegalStateException("\"beforeSaveRunnable\" property of document " + doc + // NOI18N
                    " is already occupied by " + beforeSaveRunnable); // NOI18N
        }
        beforeSaveRunnable = new Runnable() {
            public @Override void run() {
                runTasks();
            }
        };
        doc.putProperty("beforeSaveRunnable", beforeSaveRunnable); // NOI18N
    }

    /**
     * Add a new task to be executed before save of the document.
     *
     * @param task non-null task.
     */
    public static void addTask(Task task) {
        if (task == null)
            throw new IllegalArgumentException("task must not be null"); // NOI18N
        synchronized (tasks) {
            tasks.add(task);
        }
    }

    /**
     * Remove a task from the list of existing before-save tasks.
     *
     * @param task runnable to be removed.
     * @return true if the tasks was removed successfully or false if the task
     *  was not found (compared by <code>Object.equals()</code>).
     */
    public static boolean removeTask(Task task) {
        synchronized (tasks) {
            return tasks.remove(task);
        }
    }
    
    public static boolean removeTask(Class taskClass) {
        synchronized (tasks) {
            int i = taskIndex(taskClass);
            if (i >= 0) {
                tasks.remove(i);
                return true;
            }
        }
        return false;
    }

    public static Task getTask(Class taskClass) {
        synchronized (tasks) {
            int i = taskIndex(taskClass);
            return (i >= 0) ? tasks.get(i) : null;
        }
    }

    private static int taskIndex(Class taskClass) {
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.get(i);
            if (taskClass == task.getClass()) {
                return i;
            }
        }
        return -1;
    }

    void runTasks() {
        final List<Task> tasksCopy = new ArrayList<Task>(tasks);
        final List<Object> locks = new ArrayList<Object>(tasksCopy.size());
        int taskCount = tasksCopy.size();
        int lockedTaskEndIndex = 0;
        for (;lockedTaskEndIndex < taskCount; lockedTaskEndIndex++) {
            Task task = tasksCopy.get(lockedTaskEndIndex);
            locks.add(task.lock(doc));
        }
        try {
            doc.runAtomicAsUser (new Runnable () {
                public @Override void run () {
                    UndoableEdit atomicEdit = EditorPackageAccessor.get().BaseDocument_markAtomicEditsNonSignificant(doc);
                    // Since these are before-save actions they should generally not prevent
                    // the save operation to succeed. Thus the possible exceptions thrown
                    // by the tasks will be notified but they will not prevent the save to succeed.
                    try {
                        for (int i = 0; i < tasksCopy.size(); i++) {
                            Task task = tasksCopy.get(i);
                            task.run(locks.get(i), doc, atomicEdit);
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Exception thrown in before-save tasks", e); // NOI18N
                    }

                }
            });
        } finally {
            while (lockedTaskEndIndex > 0) {
                Task task = tasksCopy.get(--lockedTaskEndIndex);
                task.unlock(locks.get(lockedTaskEndIndex), doc);
            }

            EditorPackageAccessor.get().BaseDocument_clearAtomicEdits(doc);
        }
    }

    /**
     * Task run right before document saving such as reformatting or trailing whitespace removal.
     */
    public interface Task {

        /**
         * Perform an extra lock for task if necessary.
         * Locks for all tasks will be obtained first then atomic document lock will be obtained
         * and then all the tasks will be run. Then all the locks will be released by running unlock()
         * in all tasks in a reverse order of locking.
         * @param doc non-null document.
         */
        Object lock(Document doc);

        /**
         * Run the before-save task.
         *
         * @param lockInfo lock object produced by {@link #lock(javax.swing.text.Document) }
         *  that may contain an arbitrary info.
         * @param doc non-null document.
         * @param edit a non-ended edit to which undoable edits of the task should be added.
         *  It may be null in which case the produced tasks should not be added to anything.
         */
        void run(Object lockInfo, Document doc, UndoableEdit edit);

        /**
         * Perform an extra unlock for task if necessary.
         *
         * @param lockInfo lock object produced by {@link #lock(javax.swing.text.Document) }
         *  that may contain an arbitrary info.
         * @param doc non-null document.
         */
        void unlock(Object lockInfo, Document doc);

    }

}
