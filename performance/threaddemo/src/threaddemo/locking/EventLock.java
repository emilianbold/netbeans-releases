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

package threaddemo.locking;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.awt.event.PaintEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Utilities;

// XXX could track read vs. write state

/**
 * Lock impl that works in the event thread.
 * @author Jesse Glick
 */
final class EventLock implements RWLock {

    public final static RWLock DEFAULT = new EventLock();

    private EventLock() {}

    public <T> T read(final LockAction<T> action) {
        if (isDispatchThread()) {
            return action.run();
        } else {
            final List<T> result = new ArrayList<T>(1);
            try {
                invokeAndWaitLowPriority(this, new Runnable() {
                    public void run() {
                        result.add(action.run());
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
            return result.get(0);
        }
    }
    
    public <T, E extends Exception> T read(final LockExceptionAction<T,E> action) throws E {
        if (isDispatchThread()) {
            return action.run();
        } else {
            final Throwable[] exc = new Throwable[1];
            final List<T> result = new ArrayList<T>(1);
            try {
                invokeAndWaitLowPriority(this, new Runnable() {
                    public void run() {
                        try {
                            result.add(action.run());
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
                @SuppressWarnings("unchecked")
                E e = (E) exc[0];
                throw e;
            } else {
                return result.get(0);
            }
        }
    }
    
    public void readLater(final Runnable action) {
        invokeLaterLowPriority(this, action);
    }
    
    public <T> T write(LockAction<T> action) {
        return read(action);
    }
    
    public <T, E extends Exception> T write(LockExceptionAction<T,E> action) throws E {
        return read(action);
    }
    
    public void writeLater(Runnable action) {
        readLater(action);
    }
    
    public void read(Runnable action) {
        if (isDispatchThread()) {
            action.run();
        } else {
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
    static void invokeLaterLowPriority(RWLock m, Runnable r) {
        Toolkit t = Toolkit.getDefaultToolkit();
        EventQueue q = t.getSystemEventQueue();
        q.postEvent(new PaintPriorityEvent(m, t, r, null, false));
    }
    
    /**
     * Similar to {@link EventQueue#invokeAndWait} but posts the event at the same
     * priority as paint requests, to avoid bad visual artifacts.
     */
    static void invokeAndWaitLowPriority(RWLock m, Runnable r)
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
        private final RWLock m;
        public PaintPriorityEvent(RWLock m, Toolkit source, Runnable runnable, Object notifier, boolean catchExceptions) {
            super(source, PaintEvent.PAINT, runnable, notifier, catchExceptions);
            this.m = m;
        }
        public String paramString() {
            return super.paramString() + ",lock=" + m; // NOI18N
        }
    }
    private static final class PaintPriorityEventLock {}
    
}
