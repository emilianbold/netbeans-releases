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

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.awt.event.PaintEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.openide.util.Utilities;

// XXX could track read vs. write state

/**
 * Lock impl that works in the event thread.
 * @author Jesse Glick
 */
final class EventLock implements Lock {
    
    public final static Lock DEFAULT = new EventLock();
    
    private EventLock() {}
    
    public Object read(final LockAction action) {
        if (isDispatchThread()) {
            return action.run();
        } else {
            ReadWriteLock.enteringOther(this);
            final Object[] result = new Object[1];
            try {
                invokeAndWaitLowPriority(this, new Runnable() {
                    public void run() {
                        result[0] = action.run();
                    }
                });
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.toString());
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                } else if (t instanceof Error) {
                    throw (Error)t;
                } else {
                    throw new IllegalStateException(t.toString());
                }
            }
            return result[0];
        }
    }
    
    public Object read(final LockExceptionAction action) throws InvocationTargetException {
        if (isDispatchThread()) {
            try {
                return action.run();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        } else {
            ReadWriteLock.enteringOther(this);
            final Throwable[] exc = new Throwable[1];
            final Object[] result = new Object[1];
            try {
                invokeAndWaitLowPriority(this, new Runnable() {
                    public void run() {
                        try {
                            result[0] = action.run();
                        } catch (Throwable t) {
                            exc[0] = t;
                        }
                    }
                });
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.toString());
            } catch (InvocationTargetException e) {
                // Should not happen since we caught Exception above already:
                throw new IllegalStateException(e.getTargetException().toString());
            }
            if (exc[0] instanceof RuntimeException) {
                throw (RuntimeException)exc[0];
            } else if (exc[0] instanceof Error) {
                throw (Error)exc[0];
            } else if (exc[0] != null) {
                throw new InvocationTargetException((Exception)exc[0]);
            } else {
                return result[0];
            }
        }
    }
    
    public void readLater(final Runnable action) {
        final Object held = ReadWriteLock.findHeldLocksOpaque();
        invokeLaterLowPriority(this, new Runnable() {
            public void run() {
                ReadWriteLock.runWhileHoldingOpaque(held, action);
            }
        });
    }
    
    public Object write(LockAction action) {
        return read(action);
    }
    
    public Object write(LockExceptionAction action) throws InvocationTargetException {
        return read(action);
    }
    
    public void writeLater(Runnable action) {
        readLater(action);
    }
    
    public void read(Runnable action) {
        if (isDispatchThread()) {
            action.run();
        } else {
            ReadWriteLock.enteringOther(this);
            try {
                invokeAndWaitLowPriority(this, action);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.toString());
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                } else if (t instanceof Error) {
                    throw (Error)t;
                } else {
                    throw new IllegalStateException(t.toString());
                }
            }
        }
    }
    
    public void write(Runnable action) {
        read(action);
    }
    
    public boolean canRead() {
        return isDispatchThread();
    }
    
    public boolean canWrite() {
        return isDispatchThread();
    }
    
    public String toString() {
        return "Locks.eventLock"; // NOI18N
    }
    
    /** @return true iff current thread is EventDispatchThread */
    static boolean isDispatchThread() {
        boolean dispatch = EventQueue.isDispatchThread ();
        if (!dispatch && Utilities.getOperatingSystem () == Utilities.OS_SOLARIS) {
            // on solaris the event queue is not always recognized correctly
            // => try to guess by name
            dispatch = (Thread.currentThread().getClass().getName().indexOf("EventDispatchThread") >= 0); // NOI18N
        }
        return dispatch;
    }

    /**
     * Similar to {@link EventQueue#invokeLater} but posts the event at the same
     * priority as paint requests, to avoid bad visual artifacts.
     */
    static void invokeLaterLowPriority(Lock m, Runnable r) {
        Toolkit t = Toolkit.getDefaultToolkit();
        EventQueue q = t.getSystemEventQueue();
        q.postEvent(new PaintPriorityEvent(m, t, r, null, false));
    }
    
    /**
     * Similar to {@link EventQueue#invokeAndWait} but posts the event at the same
     * priority as paint requests, to avoid bad visual artifacts.
     */
    static void invokeAndWaitLowPriority(Lock m, Runnable r)
            throws InterruptedException, InvocationTargetException {
        Toolkit t = Toolkit.getDefaultToolkit();
        EventQueue q = t.getSystemEventQueue();
        Object lock = new PaintPriorityEventLock();
        InvocationEvent ev = new PaintPriorityEvent(m, t, r, lock, true);
        synchronized (lock) {
            q.postEvent(ev);
            lock.wait();
        }
        Exception e = ev.getException();
        if (e != null) {
            throw new InvocationTargetException(e);
        }
    }
    
    private static final class PaintPriorityEvent extends InvocationEvent {
        private final Lock m;
        public PaintPriorityEvent(Lock m, Toolkit source, Runnable runnable, Object notifier, boolean catchExceptions) {
            super(source, PaintEvent.PAINT, runnable, notifier, catchExceptions);
            this.m = m;
        }
        public String paramString() {
            return super.paramString() + ",lock=" + m; // NOI18N
        }
    }
    private static final class PaintPriorityEventLock {}
    
}
