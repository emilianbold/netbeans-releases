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

package org.netbeans.lib.editor.util;

/**
 * Mutex that allows only one thread to proceed
 * other threads must wait until that one finishes.
 * <br>
 * The thread that "holds" the mutex (has the mutex access granted)
 * may reenter the mutex arbitrary number of times
 * (just increasing a "depth" of the locking).
 * <br>
 * If the priority thread enters waiting on the mutex
 * then it will get serviced first once the current thread
 * leaves the mutex.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class PriorityMutex {

    private Thread lockThread;

    private int lockDepth;
    
    private Thread waitingPriorityThread;
    
    /**
     * Acquire the ownership of the mutex.
     *
     * <p>
     * The following pattern should always be used:
     * <pre>
     *   mutex.lock();
     *   try {
     *       ...
     *   } finally {
     *       mutex.unlock();
     *   }
     * </pre>
     */
    public synchronized void lock() {
        Thread thread = Thread.currentThread();
        if (thread != lockThread) { // not nested locking
            // Will wait if either there is another thread already holding the lock
            // or if there is a priority thread waiting but it's not this thread
            while (lockThread != null
                || (waitingPriorityThread != null && waitingPriorityThread != thread)
            ) {
                try {
                    if (waitingPriorityThread == null && isPriorityThread()) {
                        waitingPriorityThread = thread;
                    }

                    wait();

                } catch (InterruptedException e) {
                    waitingPriorityThread = null;
                }
            }

            lockThread = thread;

            if (thread == waitingPriorityThread) {
                waitingPriorityThread = null; // it's now allowed to enter
            }
        }

        lockDepth++;
    }
    
    /**
     * Release the ownership of the mutex.
     *
     * @see lock()
     */
    public synchronized void unlock() {
        if (Thread.currentThread() != lockThread) {
            throw new IllegalStateException("Not locker"); // NOI18N
        }

        if (--lockDepth == 0) {
            lockThread = null;

            notifyAll(); // must all to surely notify waitingPriorityThread too
        }
    }
    
    /**
     * Can be called by the thread
     * that acquired the mutex to check whether there
     * is a priority thread (such as AWT event-notification thread)
     * waiting.
     * <br>
     * If there is a priority thread waiting the non-priority thread
     * should attempt to stop its work as soon and release the ownership
     * of the mutex.
     * <br>
     * The method must *not* be called without first taking the ownership
     * of the mutex (it is intentionally not synchronized).
     */
    public boolean isPriorityThreadWaiting() {
        return (waitingPriorityThread != null);
    }
    
    /**
     * Return a thread that acquired this mutex.
     * <br>
     * This method is intended for diagnostic purposes only.
     *
     * @return thread that currently acquired lock the mutex
        or <code>null</code>
     *  if there is currently no thread holding that acquired this mutex.
     */
    public final synchronized Thread getLockThread() {
        return lockThread;
    }

    /**
     * Return true if the current thread that is entering this method
     * is a priority thread
     * and should be allowed to enter as soon as possible.
     *
     * <p>
     * The default implementation assumes that
     * {@link javax.swing.SwingUtilities#isEventDispatchThread()}
     * is a priority thread.
     *
     * @return true if the entering thread is a priority thread.
     */
    protected boolean isPriorityThread() {
        return javax.swing.SwingUtilities.isEventDispatchThread();
    }

}
