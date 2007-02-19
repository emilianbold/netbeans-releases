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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.progress;

import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.progress.spi.InternalHandle;

/**
 * Instances provided by the ProgressHandleFactory allow the users of the API to
 * notify the progress bar UI about changes in the state of the running task.
 * Progress component will be visualized only after one of the start() methods.
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public final class ProgressHandle {
    private InternalHandle internal;
    /** Creates a new instance of ProgressHandle */
    ProgressHandle(InternalHandle internal) {
        this.internal = internal;
    }

    
    /**
     * start the progress indication for indeterminate task. 
     * it will be visualized by a progress bar in indeterminate mode.
     * 
     */
    public void start() {
        start(0, -1);
    }
    
    /**
     * start the progress indication for a task with known number of steps.
     * @param workunits total number of workunits that will be processed
     */
    public void start(int workunits) {
       start(workunits, -1); 
    }
    
    
    /**
     * start the progress indication for a task with known number of steps and known
     * time estimate for completing the task.
     * @param workunits total number of workunits that will be processed
     * @param estimate estimated time to process the task in seconds
     */
    
    public void start(int workunits, long estimate) {
       internal.start("", workunits, estimate);
    }


    /**
     * Currently determinate task (with percentage or time estimate) can be 
     * switched to indeterminate mode.
     */
    public void switchToIndeterminate() {
        internal.toIndeterminate();
    }
    
    /**
     * Currently running task can switch to silent suspend mode where the progress bar 
     * stops moving, hides completely or partially. Useful to make progress in status bar less intrusive 
     * for very long running tasks, eg. running an ant script that executes user application, debugs user application etc.
     * Any incoming progress wakes up the progress bar to previous state.
     * @param message a message to display in the silent mode
     * @since org.netbeans.api.progress/1 1.9
     */
    public void suspend(String message) {
        internal.toSilent(message);
    }
    
    /**
     * Currently indeterminate task can be switched to show percentage completed.
     * A common usecase is to calculate the amount of work in the beginning showing 
     * in indeterminate mode and later switch to the progress with known steps
     * @param workunits a definite number of complete units of work out of the total
     */
    public void switchToDeterminate(int workunits) {
        internal.toDeterminate(workunits, -1);
    }
    
    /**
     * Currently indeterminate task can be switched to show the time estimate til completion.
     * A common usecase is to calculate the amount of work in the beginning 
     * in indeterminate mode and later switch to the progress with the calculated estimate.
     * @param workunits a definite number of complete units of work out of the total
     * @param estimate estimated time to process the task, in seconds
     */
    public void switchToDeterminate(int workunits, long estimate) {
        internal.toDeterminate(workunits, estimate);
    }
    
    /**
     * finish the task, remove the task's component from the progress bar UI.
     */
    public void finish() {
        internal.finish();
    }
    
    
    /**
     * Notify the user about completed workunits.
     * @param workunit a cumulative number of workunits completed so far
     */
    public void progress(int workunit) {
        progress(null, workunit);
    }
    
    /**
     * Notify the user about progress by showing message with details.
     * @param message details about the status of the task
     */
    public void progress(String message) {
        progress(message, InternalHandle.NO_INCREASE);
    }
    
    /**
     * Notify the user about completed workunits and show additional detailed message.
     * @param message details about the status of the task
     * @param workunit a cumulative number of workunits completed so far
     */
    public void progress(String message, int workunit) {
        internal.progress(message, workunit);
    }
    

    /**
     * Set a custom initial delay for the progress task to appear in the status bar.
     * This delay marks the time between starting of the progress handle
     * and its appearance in the status bar. If it finishes earlier, it's not shown at all.
     * There is a default &lt; 1s value for this. If you want it to appear earlier or later,
     * call this method with the value you prefer <strong>before {@linkplain #start() starting}</strong> the handle.
     * (Has no effect if called after the handle is started.)
     * <p> Progress bars that are placed in custom dialogs do always appear right away without a delay.
     * @param millis number of miliseconds to wait before the progress appears in status bar.
     * @since org.netbeans.api.progress/1 1.2
     */
    public void setInitialDelay(int millis) {
       internal.setInitialDelay(millis); 
    }
    
    /**
     * change the display name of the progress task. Use with care, please make sure the changed name is not completely different,
     * or otherwise it might appear to the user as a different task.
     * @param newDisplayName a new name to set for the task
     * @since org.netbeans.api.progress 1.5
     */
    public void setDisplayName(String newDisplayName) {
        internal.requestDisplayNameChange(newDisplayName);
    }
    
    /**
     * have the component in custom location, don't include in the status bar.
     */
    JComponent extractComponent() {
        return internal.extractComponent();
    }

    /**
     * for unit testing only..
     * @deprecated for unit testing only.
     */
    @Deprecated
    InternalHandle getInternalHandle() {
        return internal;
    }

    JLabel extractDetailLabel() {
        return internal.extractDetailLabel();
    }

    JLabel extractMainLabel() {
        return internal.extractMainLabel();
    }
    

}
