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

package org.netbeans.api.progress.aggregate;

import javax.swing.Action;
import org.netbeans.progress.module.InternalHandle;
import org.openide.util.Cancellable;

/**
 * Factory for creation of aggregate progress indication handles and individual contributor instances.
 * For a more simple version of progress indication, see {@link org.netbeans.api.progress.ProgressHandleFactory}
 *
 * @author mkleint (mkleint@netbeans.org)
 */
public class AggregateProgressFactory {
    
    /** Creates a new instance of AggregateProgressFactory */
    private AggregateProgressFactory() {
    }
    
    /**
     * Create an aggregating progress ui handle for a long lasting task.
     * @param contributors the initial set of progress indication contributors that are aggregated in the UI.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of <code>ProgressHandle</code>, initialized but not started.
     *
     */
    public static AggregateProgressHandle createHandle(String displayName, ProgressContributor[] contributors, 
                                                       Cancellable allowToCancel, Action linkOutput) {
        return new AggregateProgressHandle(displayName, contributors, allowToCancel, linkOutput, false);
    }
    
    public static ProgressContributor createProgressContributor(String trackingId) {
        return new ProgressContributor(trackingId);
    }
    
    /**
     * Create an aggregating progress ui handle for a long lasting task.
     * @param contributors the initial set of progress indication contributors that are aggregated in the UI.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of <code>ProgressHandle</code>, initialized but not started.
     *
     */
    public static AggregateProgressHandle createSystemHandle(String displayName, ProgressContributor[] contributors, 
                                                       Cancellable allowToCancel, Action linkOutput) {
        return new AggregateProgressHandle(displayName, contributors, allowToCancel, linkOutput, true);
    }    
    
}
