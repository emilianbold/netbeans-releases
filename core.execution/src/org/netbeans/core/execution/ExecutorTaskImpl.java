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

package org.netbeans.core.execution;

import org.openide.execution.ExecutorTask;
import org.openide.windows.InputOutput;

/** Purpose ???
*
* @author Ales Novak
* @version 1.0, November 18, 1998
*/
final class ExecutorTaskImpl extends ExecutorTask {
    /** result */
    int result = -1;
    /** SysProcess ref */
    DefaultSysProcess proc;
    /** lock */
    Object lock = this;

    /** constructor */
    ExecutorTaskImpl() {
        super(new Runnable() {
                  public void run() {}
              }
             );
    }

    /** Stops the task. */
    public void stop() {
        try {
            synchronized (lock) {
                while (proc == null) lock.wait();
                proc.stop();
            }
        } catch (InterruptedException e) {
        }
    }
    /** @return result 0 means success. Blocking operation. */
    public int result() {
        waitFinished();
        return result;
    }
    // hack off
    final void finished() {
        notifyFinished();
    }
    public void run() {
        waitFinished();
    }

    public InputOutput getInputOutput() {
        return proc.getInputOutput();
    }
}
