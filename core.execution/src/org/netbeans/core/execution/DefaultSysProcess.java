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

import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.windows.InputOutput;

/** Support for Executor beans and for their SysProcess subclasses.
*
* @author Ales Novak
*/
final class DefaultSysProcess extends ExecutorTask {

    /** reference count of instances */
    static int processCount;
    /** reference to SysProcess ThreadGroup */
    private final TaskThreadGroup group;
    /** flag deciding whether is the process destroyed or not */
    private boolean destroyed = false;
    /** InputOutput for this Context */
    private final InputOutput io;
    /** Name */
    private final String name;

    /**
    * @param grp is a ThreadGroup of this process
    */
    public DefaultSysProcess(Runnable run, TaskThreadGroup grp, InputOutput io, String name) {
        super(run);
        group = grp;
        this.io = io;
        this.name = name;
    }

    /** terminates the process by killing all its thread (ThreadGroup) */
    public synchronized void stop() {

        if (destroyed) return;
        destroyed = true;
        try {
            group.interrupt();
            group.stop();
            group.getRunClassThread().waitForEnd();
        } catch (InterruptedException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
        ExecutionEngine.closeGroup(group);
        group.kill();  // force RunClass thread get out - end of exec is fired
        notifyFinished();
    }

    /** waits for this process is done
    * @return 0
    */
    public int result() {
        // called by an instance of RunClass thread - kill() in previous stop() forces calling thread
        // return from waitFor()
        try {
            group.waitFor();
        } catch (InterruptedException e) {
            return 4; // EINTR
        }
        notifyFinished();
        return 0;
    }

    /** @return an InputOutput */
    public InputOutput getInputOutput() {
        return io;
    }

    public void run() {
    }

    public String getName() {
        return name;
    }
    
    /** destroy the thread group this process was handled from. Not that simple
     * as it seems, since the ThreadGroup can't be destroyed from inside.
     */
    void destroyThreadGroup(ThreadGroup base) {
        new Thread(base, new Runnable() {
            public void run() {
                try {
                    while (group.activeCount() > 0) {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    ErrorManager.getDefault().notify(e);
                }
                if (!group.isDestroyed()) group.destroy();
            }
        }).start();
    }
}
