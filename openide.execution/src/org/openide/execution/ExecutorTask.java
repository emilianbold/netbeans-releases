/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.execution;

import org.openide.util.Task;
import org.openide.windows.InputOutput;

/** A task object that represents an asynchronously
* running execution task.
* Module authors do not typically need to subclass this.
* @author Jaroslav Tulach
*/
public abstract class ExecutorTask extends Task {
    /** Create the task.
    * @param run runnable to run that computes the task
    */
    protected ExecutorTask(Runnable run) {
        super (run);
    }

    /** Stop the computation.
    */
    public abstract void stop ();

    /** Check the result of execution. If the execution
    * is not finished, the calling thread is blocked until it is.
    *
    * @return the result of execution. Zero means successful execution; other numbers may indicate various error conditions.
    */
    public abstract int result ();

    /**
    * @return InputOutput assigned to this process
    */
    public abstract InputOutput getInputOutput();
}
