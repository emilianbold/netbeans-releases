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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test Locks.event.
 * @author Jesse Glick
 */
public class EventLockTest extends TestCase {
    
    public EventLockTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new TestSuite(EventLockTest.class));
    }
    
    /*
    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
    }
     */
    
    public void testEventAccess() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        // test canRead, canWrite, correct thread used by synch methods
        assertTrue(!Locks.event().canRead());
        assertTrue(!Locks.event().canWrite());
        assertEquals(Boolean.TRUE, Locks.event().read(new LockAction() {
            public Object run() {
                return new Boolean(EventQueue.isDispatchThread() &&
                                   Locks.event().canRead() &&
                                   Locks.event().canWrite());
            }
        }));
        assertEquals(Boolean.TRUE, Locks.event().read(new LockExceptionAction() {
            public Object run() throws Exception {
                return new Boolean(EventQueue.isDispatchThread() &&
                                   Locks.event().canRead() &&
                                   Locks.event().canWrite());
            }
        }));
        assertEquals(Boolean.TRUE, Locks.event().write(new LockAction() {
            public Object run() {
                return new Boolean(EventQueue.isDispatchThread() &&
                                   Locks.event().canRead() &&
                                   Locks.event().canWrite());
            }
        }));
        assertEquals(Boolean.TRUE, Locks.event().write(new LockExceptionAction() {
            public Object run() throws Exception {
                return new Boolean(EventQueue.isDispatchThread() &&
                                   Locks.event().canRead() &&
                                   Locks.event().canWrite());
            }
        }));
        // test that r/wA(Runnable) runs in AWT eventually
        final boolean[] b = new boolean[1];
        // first, that r/wA will run (even asynch)
        Locks.event().readLater(new Runnable() {
            public void run() {
                synchronized (b) {
                    b[0] = EventQueue.isDispatchThread();
                    b.notify();
                }
            }
        });
        synchronized (b) {
            if (!b[0]) b.wait(9999);
        }
        assertTrue(b[0]);
        Locks.event().writeLater(new Runnable() {
            public void run() {
                synchronized (b) {
                    b[0] = !EventQueue.isDispatchThread();
                    b.notify();
                }
            }
        });
        synchronized (b) {
            if (b[0]) b.wait(9999);
        }
        assertTrue(!b[0]);
        // now that r/wA runs synch in event thread
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                Locks.event().readLater(new Runnable() {
                    public void run() {
                        b[0] = EventQueue.isDispatchThread();
                    }
                });
            }
        });
        assertTrue(b[0]);
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                Locks.event().writeLater(new Runnable() {
                    public void run() {
                        b[0] = !EventQueue.isDispatchThread();
                    }
                });
            }
        });
        assertTrue(!b[0]);
    }
    
    public void testEventExceptions() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        // test that checked excs from M.EA throw correct ME
        try {
            Locks.event().read(new LockExceptionAction() {
                public Object run() throws Exception {
                    throw new IOException();
                }
            });
            fail();
        } catch (InvocationTargetException e) {
            assertEquals(IOException.class, e.getCause().getClass());
        }
        // but that unchecked excs are passed thru
        try {
            Locks.event().read(new LockExceptionAction() {
                public Object run() throws Exception {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        } catch (Exception e) {
            fail(e.toString());
        }
        // similarly for unchecked excs from M.A
        try {
            Locks.event().read(new LockAction() {
                public Object run() {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK
        } catch (RuntimeException e) {
            fail(e.toString());
        }
        // and blocking runnables
        try {
            Locks.event().read(new Runnable() {
                public void run() {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK.
        }
        try {
            Locks.event().write(new Runnable() {
                public void run() {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK.
        }
    }
    
}
