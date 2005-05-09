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

package org.netbeans.api.progress;

import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.progress.module.InternalHandle;
import org.openide.util.Cancellable;

/**
 * Factory to create various ProgressHandle instances that allow long lasting 
 * tasks to show their progress using various progress UIs.
 * 
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public final class ProgressHandleFactory {
    
    /** Creates a new instance of ProgressIndicatorFactory */
    private ProgressHandleFactory() {
    }
    
    /**
     * Create a progress ui handle for a long lasting task.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createHandle(String displayName) {
        return createHandle(displayName, null, null);
    }
    
    /**
     * Create a progress ui handle for a long lasting task.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createHandle(String displayName, Cancellable allowToCancel) {
        return createHandle(displayName, allowToCancel, null);
    }

    /**
     * Create a progress ui handle for a long lasting task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     *
     */
    public static ProgressHandle createHandle(String displayName, Action linkOutput) {
        return createHandle(displayName, null, linkOutput);
    }
    
    /**
     * Create a progress ui handle for a long lasting task.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     *
     */
    public static ProgressHandle createHandle(String displayName, Cancellable allowToCancel, Action linkOutput) {
        return new ProgressHandle(new InternalHandle(displayName, allowToCancel, true, linkOutput));
    }
    
    /**
     * Get the progress bar component for use in custom dialogs, the task won't 
     * show in the progress bar anymore.
     * @return the component to use in custom UI.
     */
    public static JComponent createProgressComponent(ProgressHandle handle) {
        return handle.extractComponent();
    }
    
    /**
     * Create a handle for a long lasting task that is not triggered by explicit user action.
     * Such tasks have lower priority in the UI.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createSystemHandle(String displayName) {
        return createSystemHandle(displayName, null);
    }

    /**
     * Create a cancelable handle for a task that is not triggered by explicit user action.
     * Such tasks have lower priority in the UI.
     * @param displayName to be shown in the progress UI
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createSystemHandle(String displayName, Cancellable allowToCancel) {
        return new ProgressHandle(new InternalHandle(displayName, allowToCancel, false, null));
    }
    
    /**
     * Create a progress ui handle for a task that is not triggered by explicit user action.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     *
     */
    public static ProgressHandle createSystemHandle(String displayName, Cancellable allowToCancel, Action linkOutput) {
        return new ProgressHandle(new InternalHandle(displayName, allowToCancel, false, linkOutput));
    }    
    
}
