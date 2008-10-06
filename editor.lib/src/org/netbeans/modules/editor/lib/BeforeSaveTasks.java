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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.GapList;

/**
 * Removal of trailing whitespace
 *
 * @author Miloslav Metelka
 * @since 1.9
 */
public final class BeforeSaveTasks {
    
    private static final Logger LOG = Logger.getLogger(BeforeSaveTasks.class.getName());
    
    public static synchronized BeforeSaveTasks get(BaseDocument doc) {
        BeforeSaveTasks beforeSaveTasks = (BeforeSaveTasks) doc.getProperty(BeforeSaveTasks.class);
        if (beforeSaveTasks == null) {
            beforeSaveTasks = new BeforeSaveTasks(doc);
            doc.putProperty(BeforeSaveTasks.class, beforeSaveTasks);
        }
        return beforeSaveTasks;
    }
    
    private final BaseDocument doc;

    private List<Task> tasks = new GapList<Task>(1);
    
    private BeforeSaveTasks(BaseDocument doc) {
        this.doc = doc;
        Runnable beforeSaveRunnable = (Runnable)
                doc.getProperty("beforeSaveRunnable"); // Name of prop in sync with CloneableEditorSupport NOI18N
        if (beforeSaveRunnable != null) {
            throw new IllegalStateException("\"beforeSaveRunnable\" property of document " + doc + // NOI18N
                    " is already occupied by " + beforeSaveRunnable); // NOI18N
        }
        beforeSaveRunnable = new Runnable() {
            public void run() {
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
    public synchronized void addTask(Task task) {
        if (task == null)
            throw new IllegalArgumentException("task must not be null"); // NOI18N
        tasks.add(task);
    }

    /**
     * Remove a task from the list of existing before-save tasks.
     *
     * @param task runnable to be removed.
     * @return true if the tasks was removed successfully or false if the task
     *  was not found (compared by <code>Object.equals()</code>).
     */
    public synchronized boolean removeTask(Runnable task) {
        return tasks.remove(task);
    }
    
    private void runTasks() {
        doc.runAtomicAsUser (new Runnable () {
            public void run () {
                CompoundEdit atomicEdit = EditorPackageAccessor.get().markAtomicEditsNonSignificant(doc);
                // Since these are before-save actions they should generally not prevent
                // the save operation to succeed. Thus the possible exceptions thrown
                // by the tasks will be notified but they will not prevent the save to succeed.
                try {
                    for (Task task : tasks) {
                        task.run(atomicEdit);
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Exception thrown in before-save tasks", e); // NOI18N
                }
            }
        });
    }

    public interface Task {
        
        void run(CompoundEdit edit);

    }

}
