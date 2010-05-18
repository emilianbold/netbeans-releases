/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * File         : EventHandler.java
 * Version      : 1.0
 * Description  : Queues runnable objects and calls their run() methods in
 *                sequence.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide.events;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
import org.netbeans.modules.uml.integration.ide.dialogs.IProgressIndicator;
import org.netbeans.modules.uml.integration.ide.dialogs.IProgressIndicatorFactory;
import java.util.Vector;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.ui.support.ProductHelper;

/**
 *  Queues runnable objects and calls their run() methods in sequence from a
 * separate worker thread.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-28  Darshan     Synchronized on EventHandler instance to allow
 *                              synch between source-model and model-source
 *                              threads.
 *
 * @author Darshan
 */
public class EventHandler {
    /**
     *  The number of runnable objects that must be queued before we show a
     * progress indicator to the user.
     */
    private static final short PROGRESS_THRESHOLD = 3;

    private static final short PRESET_MAXIMUM     = 200;

    /**
     *  Queue of runnable objects to be executed in sequence.
     */
    private Vector          runnableQueue = new Vector();

    /**
     *  The worker thread that runs the runnable objects in sequence.
     */
    private WorkerThread    worker    = new WorkerThread();

    /**
     *  The thread that displays and manages the progress indicator.
     */
    private ProgressThread  progress  = null;

    /**
     *  An object that knows how to execute a queued runnable object. Note that
     * the queued runnable objects need not be java.lang.Runnable objects if you
     * provide your own executor.
     */
    private ITaskExecutor   executor  = defaultExecutor;

    /**
     *  The default executor; executes Runnable objects' run() methods.
     */
    public static final ITaskExecutor defaultExecutor = new ITaskExecutor() 
    {
        public void executeTask(Object task) 
        {
            // conover - only execute events if RT is enabled
            if (ProductHelper.getCoreProduct()
                .getRoundTripController().getMode() == RTMode.RTM_LIVE)
            {
                ((Runnable) task).run();
            }
        }
    };

    /**
     * A factory that knows how to create progress indicators. If left null,
     * progress indicators won't be shown.
     */
    private IProgressIndicatorFactory progressFactory = null;

    /**
     *  <code>true</code> if the worker thread is processing a job on the queue.
     */
    private boolean working = false;

    /**
     *  Creates an EventHandler, but does not start the worker thread.
     *
     * @param queueName The thread name of the worker.
     */
    public EventHandler(String queueName) {
        if (queueName != null)
            worker.setName(queueName);
    }

    /**
     *  Sets the executor used to run the objects in the queue.
     *
     * @param executor An <code>EventHandler.ITaskExecutor</code> replacement
     *                 for the default executor. Note that nulling the executor
     *                 will create a do-nothing queue that simply discards the
     *                 jobs on the queue.
     */
    public void setTaskExecutor(EventHandler.ITaskExecutor executor) {
        this.executor = executor;
    }

    /**
     *  Retrieves the executor currently in use.
     * @return The <code>EventHandler.ITaskExecutor</code> that's in use; if
     *         null, this queue is a do-nothing queue.
     */
    public EventHandler.ITaskExecutor getTaskExecutor() {
        return executor;
    }

    /**
     *  Retrieves the progress indicator currently in use.
     * @return An <code>IProgressIndicatorFactory</code>.
     */
    public IProgressIndicatorFactory getProgressFactory() {
        return progressFactory;
    }

    /**
     *  Sets the progress indicator factory to be used.
     * @param factory An <code>IProgressIndicatorFactory</code>; can be null to
     *                suppress progress monitoring.
     */
    public void setProgressFactory(IProgressIndicatorFactory factory) {
        if ((progressFactory = factory) != null) {
            if (progress == null)
                progress = new ProgressThread();
            if (!progress.isAlive())
                progress.start();
        }
    }

    /**
     *  Starts the worker thread to begin processing events from the queue.
     * Existing queued events will be processed.
     */
    public void startWorker() {
        worker.start();
    }

    /**
     *  Stops the worker thread as soon as the currently running Runnable
     * is finished. Pending queued events will be discarded and any open
     * progress indicator will be closed.
     */
    public void stopWorker() {
        worker.interrupt();
        clear();
    }

    /**
     *  Empties the queue of pending events. Any event that is currently being
     * processed will not be affected.
     */
    public void clear() {
        synchronized (runnableQueue) {
            runnableQueue.clear();
            if (progress != null && progress.isAlive())
                progress.killProgress();
        }
    }

    /**
     *  Adds a job to the queue, usually a Runnable job (if the default
     * executor is in use).
     *
     * @param r The job, usually a Runnable.
     */
    public void queueRunnable(Object r) {
        queueRunnable(r, -1);
    }

    /**
     *  Adds a job to the queue at the specified position. The job is usually a
     * Runnable job (if the default executor is in use).
     *
     * @param r     The job, usually a Runnable.
     * @param index The index on the queue at which to insert the Runnable.
     */
    public void queueRunnable(Object r, int index) {
        synchronized (runnableQueue) {
            if (index <= -1 || index > runnableQueue.size())
                runnableQueue.add(r);
            else
                runnableQueue.add(index, r);

            if (progress != null && progressFactory != null
                    && !progress.isWorking()) {
                if (runnableQueue.size() > PROGRESS_THRESHOLD)
                    showProgressIndicator(runnableQueue.size() + 1);
            } else if (worker.isAlive() && progress != null
                       && progress.isWorking()) {
                progress.addTasks(1);
            }
            runnableQueue.notify();
        }
    }

    public void showProgressIndicator(int size) {
        if (progressFactory == null || progress == null)
            throw new IllegalStateException("Cannot show progress indicator");
        progress.startProgress(size);
    }

    /**
     *  Returns the first job in the queue; blocks if the queue is empty.
     * @throws InterruptedException If the thread is interrupted
     */
    private Object getNextRunnable() throws InterruptedException {
        synchronized (runnableQueue) {
            while (runnableQueue.size() == 0) {
                /*if(enableWSSaves && !wasWSSavedLast) {
                    wasWSSavedLast = !wasWSSavedLast;
                    queueWSSave();
               } else {
                    wasWSSavedLast = !wasWSSavedLast;
                    runnableQueue.wait();
                }*/
                runnableQueue.wait();
            }
            Object r = runnableQueue.elementAt(0);
            runnableQueue.remove(0);
            return r;
        }
    }

    /**
     *  Determines if the queue contains items for processing or is currently
     * processing an item.
     *
     * @return <code>true</code> If the queue is busy.
     */
    public boolean isBusy() {
        synchronized (runnableQueue) {
            return runnableQueue.size() > 0 || working;
        }
    }

    /**
     *  An interface for classes that know how to execute an arbitrary task.
     */
    public static interface ITaskExecutor {
        /**
         *  Runs the given task. It is assumed that the task executor knows
         * (or can find out) what kind of task the given object is.
         */
        public void executeTask(Object taskObj) throws Exception;
    }

    /**
     *  Runs queued Runnables.
     */
    private class WorkerThread extends Thread {
        /**
         *  True if this thread has been interrupted.
         */
        private boolean interrupted = false;

        public void run() {
            interrupted = false;

            try {
                while (!interrupted) {
                    Object runnable = getNextRunnable();

                    try {
                        working = true;
                        if (executor != null) executor.executeTask(runnable);
                    } catch (Throwable t) {
                        Log.stackTrace(t);
                    } finally {
                        try {
                            if (progress != null && progress.isWorking()) {
                                progress.incrementProgress();
                                if (runnableQueue.size() == 0)
                                    progress.endProgress();
                            }
                        } catch (Exception ignored) { }
                        working = false;
                    }
                }
            } catch (InterruptedException e) {
            }
        }

        /**
         *  Interrupts the worker thread - the interrupt will be immediate
         * if the worker was waiting on an empty queue, otherwise the
         * currently running Runnable will be allowed to finish first.
         */
        public void interrupt() {
            interrupted = true;
            super.interrupt();
        }
    }

    private class ProgressThread extends Thread {
        private IProgressIndicator indicator = null;
        private boolean            needed = true;
        private int                taskCount = 0;
        private int                position  = 0, completedTasks = 0;

        public void incrementProgress() {
            if (indicator == null) return ;

            int newPos = ++completedTasks * PRESET_MAXIMUM / taskCount;
            if (newPos > position)
                indicator.setProgress(null, position = newPos);
        }

        public void setTaskCount(int tasks) {
            taskCount = tasks;
        }

        public void addTasks(int taskInc) {
            taskCount += taskInc;
        }

        public void setNeeded(boolean needed) {
            this.needed = needed;
        }

        public IProgressIndicator getIndicator() {
            return indicator;
        }

        /**
         *  Starts a new progress indicator, if no indicator is already open.
         * If an indicator is already open, returns silently.
         */
        public void startProgress(int initialTaskCount) {
            if (progressFactory != null && indicator == null) {
                synchronized (this) {
                    // Double-checked locking style synchronization problems
                    // here, perhaps? Happily, this is not mission-critical
                    // stuff.
                    if (indicator != null) return ;

                    indicator = progressFactory.getProgressIndicator();
                    if (indicator != null) {
                        indicator.setMaxRange(PRESET_MAXIMUM);
                        taskCount = initialTaskCount;
                        completedTasks = 0;
                        indicator.setProgress(UMLSupport
                                .getString("Dialog.RoundtripProgress.Text"),
                                           position = 3);
                        Log.out("Done starting progress indicator");

                        if (isAlive())
                            notify();
                        else
                            start();
                    }
                }
            }
        }

        public boolean isWorking() {
            return indicator != null && isAlive();
        }

        public void endProgress() {
            if (indicator != null) indicator.done();
        }

        public void killProgress() {
            setNeeded(false);
            if (indicator != null)
                indicator.done();
            else if (isAlive())
                interrupt();
        }

        synchronized public void run() {
            while (needed) {
                try {
                    while (indicator == null) wait();
                    Log.out("Showing progress indicator");
                    indicator.show();
                    indicator = null;
                } catch (InterruptedException e) {
                    // Precaution
                    indicator = null;
                    break;
                } catch (Exception e) {
                    Log.stackTrace(e);
                }
            }
        }
    }
}
