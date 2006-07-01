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
