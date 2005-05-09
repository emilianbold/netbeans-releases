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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import org.netbeans.progress.module.InternalHandle;

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
     * Currently indeterminate task can be switched to show percentage completed.
     * A common usecase is to calculate the amount of work in the beginning showing 
     * in indeterminate mode and later switch to the progress with known steps
     */
    public void switchToDeterminate(int workunits) {
        internal.toDeterminate(workunits, -1);
    }
    
    /**
     * Currently indeterminate task can be switched to show the time estimate til completion.
     * A common usecase is to calculate the amount of work in the beginning 
     * in indeterminate mode and later switch to the progress with the calculated estimate.
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
     * have the component in custom location, don't include in the status bar.
     */
    JComponent extractComponent() {
        return internal.extractComponent();
    }

    /**
     * for unit testing only..
     * @deprecated for unit testing only.
     */
    InternalHandle getInternalHandle() {
        return internal;
    }
    

}
