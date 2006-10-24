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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;

//because of Javadoc:
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.LookupBasedJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.JavaSourceTaskFactorySupport;

/**
 * A factory for tasks that will be run in the {@link JavaSource} Java parsing harness.
 *
 * Please note that there is usually no need to implement this interface directly,
 * as there are support classes for common {@link JavaSourceTaskFactory} implementations.
 *
 * This factory should be registered in the global lookup by listing its fully qualified
 * name in file <code>META-INF/services/org.netbeans.api.java.source.JavaSourceTaskFactory</code>.
 * 
 * @see EditorAwareJavaSourceTaskFactory
 * @see CaretAwareJavaSourceTaskFactory
 * @see LookupBasedJavaSourceTaskFactory
 * @see JavaSourceTaskFactorySupport
 *
 * @author Jan Lahoda
 */
public interface JavaSourceTaskFactory {

    /**Get a priority at which the tasks created by this factory should operate.
     *
     * @return priority of tasks created by this factory
     * @see #createTask
     * @see #getPhase
     */
    public JavaSource.Priority getPriority();

    /**Get a compilation phase that is required by tasks created by this factory.
     *
     * @return phase required by tasks created by this factory
     * @see #createTask
     * @see #getPriority
     */
    public JavaSource.Phase getPhase();

    /**Create task for a given file. This task will be registered into the {@link JavaSource}
     * parsing harness with a given {@link #getPriority priority} and {@link #getPhase phase}.
     *
     * Please note that this method should run as quickly as possible.
     *
     * @param file for which file the task should be created.
     * @return created {@link CancellableTask}  for a given file.
     */
    public CancellableTask<CompilationInfo> createTask(FileObject file);

    /**Specifies on which files should be registered tasks created by this factory.
     * On {@link JavaSource}'s corresponding to {@link FileObject}s returned from
     * this method will be registered tasks created by the {@link #createTask} method
     * of this factory.
     *
     * If this list changes, a change event should be fired to all registered
     * {@link ChangeListener}s.
     *
     * @return list of {@link FileObject} on which tasks from this factory should be
     * registered.
     * @see #createTask
     * @see #addChangeListener
     * @see EditorAwareJavaSourceTaskFactory
     * @see CaretAwareJavaSourceTaskFactory
     */
    public List<FileObject> getFileObjects();

    /**Add a {@link ChangeListener}. The change event should be fired when the
     * return value of {@link #getFileObjects} changes.
     *
     * @param l listener to register
     * @see #getFileObjects
     * @see #removeChangeListener
     * @see EditorAwareJavaSourceTaskFactory
     * @see CaretAwareJavaSourceTaskFactory
     */
    public void addChangeListener(ChangeListener l);

    /**Removes a {@link ChangeListener}.
     *
     * @param l listener to unregister
     * @see #addChangeListener
     * @see EditorAwareJavaSourceTaskFactory
     * @see CaretAwareJavaSourceTaskFactory
     */
    public void removeChangeListener(ChangeListener l);

    /**Add a {@link RescheduleListener}. The reschedule event should be fired for
     * a {@link FileObject} if the task created by this factory for a given file
     * should be rescheduled.
     *
     * @param l listener to register
     * @see #removeRescheduleListener
     * @see EditorAwareJavaSourceTaskFactory
     * @see CaretAwareJavaSourceTaskFactory
     */
    public void addRescheduleListener(RescheduleListener l);

    /**Remove a {@link RescheduleListener}.
     *
     * @param l listener to unregister
     * @see #addRescheduleListener
     * @see EditorAwareJavaSourceTaskFactory
     * @see CaretAwareJavaSourceTaskFactory
     */
    public void removeRescheduleListener(RescheduleListener l);

}
