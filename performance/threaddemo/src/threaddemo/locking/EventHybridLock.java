/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.locking;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

// XXX give preference to waiting writers: first to enter after last reader leaves; no new readers
// XXX handle interactions with other locks
// XXX forbid R -> W in AWT if have explicitly entered R

/**
 * Special "event hybrid" lock.
 * If in AWT, automatically canRead, and read access is a no-op.
 * Write access from AWT is OK.
 * Write access otherwise calls invokeAndWait.
 * @author Jesse Glick
 */
final class EventHybridLock implements Lock {
    
    public static final Lock DEFAULT = new EventHybridLock();
    
    private EventHybridLock() {}
    
    public Object read(LockAction action) {
        if (EventLock.isDispatchThread()) {
            // Fine, go ahead.
            if (semaphore == -1) {
                semaphore = -2;
                try {
                    return action.run();
                } finally {
                    semaphore = -1;
                }
            } else {
                boolean oldReadingInAwt = readingInAwt;
                readingInAwt = true;
                try {
                    return action.run();
                } finally {
                    readingInAwt = oldReadingInAwt;
                }
            }
        } else {
            // Need to make sure no writers are running.
            Thread reader;
            synchronized (this) {
                while (semaphore < 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e.toString());
                    }
                }
                semaphore++;
                Thread curr = Thread.currentThread();
                if (readers.add(curr)) {
                    reader = curr;
                } else {
                    reader = null;
                }
            }
            try {
                return action.run();
            } finally {
                synchronized (this) {
                    semaphore--;
                    if (reader != null) {
                        readers.remove(reader);
                    }
                    notifyAll();
                }
            }
        }
    }
    
    private static LockAction convertExceptionAction(final LockExceptionAction action) {
        return new LockAction() {
            public Object run() {
                try {
                    return new Object[] {action.run()};
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    return e;
                }
            }
        };
    }
    
    private static Object finishExceptionAction(Object result) throws InvocationTargetException {
        if (result instanceof Object[]) {
            return ((Object[])result)[0];
        } else {
            throw new InvocationTargetException((Exception)result);
        }
    }
    
    public Object read(LockExceptionAction action) throws InvocationTargetException {
        return finishExceptionAction(read(convertExceptionAction(action)));
    }
    
    public void read(final Runnable action) {
        read(convertRunnable(action));
    }
    
    public Object write(final LockAction action) {
        if (!EventLock.isDispatchThread()) {
            // Try again in AWT.
            if (canRead()) {
                throw new IllegalStateException("Cannot go R -> W"); // NOI18N
            }
            try {
                final Object[] o = new Object[1];
                final Error[] err = new Error[1];
                EventLock.invokeAndWaitLowPriority(this, new Runnable() {
                    public void run() {
                        try {
                            o[0] = write(action);
                        } catch (Error e) {
                            err[0] = e;
                        }
                    }
                });
                if (err[0] != null) {
                    throw err[0];
                }
                return o[0];
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.toString());
            } catch (InvocationTargetException e) {
                Throwable x = e.getTargetException();
                if (x instanceof RuntimeException) {
                    throw (RuntimeException)x;
                } else if (x instanceof Error) {
                    throw (Error)x;
                } else {
                    throw new IllegalStateException(x.toString());
                }
            }
        }
        // We are in AWT.
        int oldSemaphore;
        synchronized (this) {
            while (semaphore > 0) {
                // Wait for readers to finish first.
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e.toString());
                }
            }
            oldSemaphore = semaphore;
            if (semaphore == 0) {
                if (readingInAwt) {
                    throw new IllegalStateException("Cannot go R -> W"); // NOI18N
                } else {
                    semaphore = -1;
                }
            } else if (semaphore == -1) {
                // OK.
            } else if (semaphore == -2) {
                throw new IllegalStateException("Cannot go R -> W"); // NOI18N
            }
        }
        try {
            return action.run();
        } finally {
            if (oldSemaphore == 0) {
                // Exiting outermost write; permit readers to enter.
                synchronized (this) {
                    semaphore = 0;
                    notifyAll();
                }
            }
        }
    }
    
    public Object write(LockExceptionAction action) throws InvocationTargetException {
        return finishExceptionAction(write(convertExceptionAction(action)));
    }
    
    public void write(final Runnable action) {
        write(convertRunnable(action));
    }
    
    private static LockAction convertRunnable(final Runnable action) {
        return new LockAction() {
            public Object run() {
                action.run();
                return null;
            }
        };
    }
    
    public void readLater(final Runnable action) {
        EventLock.invokeLaterLowPriority(this, new Runnable() {
            public void run() {
                read(action);
            }
        });
    }
    
    public void writeLater(final Runnable action) {
        EventLock.invokeLaterLowPriority(this, new Runnable() {
            public void run() {
                write(action);
            }
        });
    }
    
    public boolean canRead() {
        if (EventLock.isDispatchThread()) {
            return true;
        } else {
            return readers.contains(Thread.currentThread());
        }
    }
    
    public boolean canWrite() {
        if (EventLock.isDispatchThread()) {
            return semaphore == -1;
        } else {
            return false;
        }
    }
    
    public String toString() {
        // XXX include state info here
        return "Locks.eventHybridLock"; // NOI18N
    }
    
    /**
     * Count of active outside readers, or write lock state.
     * When positive, one or more readers outside AWT are holding the read lock.
     * When zero, the lock is uncontended (available in AWT).
     * When -1, the write lock is held.
     * When -2, the read lock is held inside the write lock.
     * In AWT, you can recursively enter either the read or write lock as much
     * as desired; semaphore tracks the current status only.
     */
    private int semaphore = 0;
    
    /**
     * If true, we are explicitly reading in AWT at the moment.
     */
    private boolean readingInAwt = false;
    
    /**
     * Set of readers which are running.
     */
    private final Set readers = new HashSet(); // Set<Thread>
    
}
