/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
