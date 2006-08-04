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

package org.netbeans.api.progress.aggregate;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;

/**
 * Factory for creation of aggregate progress indication handles and individual contributor instances.
 * For a more simple version of progress indication, see {@link org.netbeans.api.progress.ProgressHandleFactory}
 *
 * @author mkleint (mkleint@netbeans.org)
 */
public final class AggregateProgressFactory {

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
    
    /**
     * Get the progress bar component for use in custom dialogs, the task won't 
     * show in the progress bar anymore.
     * @since org.netbeans.api.progress 1.3
     * @return the component to use in custom UI.
     */
    public static JComponent createProgressComponent(AggregateProgressHandle handle) {
        return handle.extractComponent();
    }    
    
    /**
     * Get the task title component for use in custom dialogs, the task won't 
     * show in the progress bar anymore. The text of the label is changed by calls to handle's <code>setDisplayName()</code> method.
     * @return the component to use in custom UI.
     * @since org.netbeans.api.progress 1.8
     */
    public static JLabel createMainLabelComponent(AggregateProgressHandle handle) {
        return handle.extractMainLabel();
    }
    
    /**
     * Get the detail messages component for use in custom dialogs, the task won't 
     * show in the progress bar anymore.The text of the label is changed by calls to contributors' <code>progress(String)</code> methods.
     * @return the component to use in custom UI.
     * @since org.netbeans.api.progress 1.8
     */
    public static JLabel createDetailLabelComponent(AggregateProgressHandle handle) {
        return handle.extractDetailLabel();
    }
    
}
