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
 * Test Locks.eventHybrid.
 * @author Jesse Glick
 */
public class EventHybridLockTest extends TestCase {
    
    public EventHybridLockTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new TestSuite(EventHybridLockTest.class));
    }
    
    /*
    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
    }
     */
    
    public void testEventHybrid() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        // test canRead, canWrite, correct thread used by synch write methods
        assertTrue(!Locks.eventHybrid().canRead());
        assertTrue(!Locks.eventHybrid().canWrite());
        assertEquals(Boolean.TRUE, Locks.eventHybrid().read(new LockAction() {
            public Object run() {
                return new Boolean(!EventQueue.isDispatchThread() &&
                                   Locks.eventHybrid().canRead() &&
                                   !Locks.eventHybrid().canWrite());
            }
        }));
        assertEquals(Boolean.TRUE, Locks.eventHybrid().read(new LockExceptionAction() {
            public Object run() throws Exception {
                return new Boolean(!EventQueue.isDispatchThread() &&
                                   Locks.eventHybrid().canRead() &&
                                   !Locks.eventHybrid().canWrite());
            }
        }));
        assertEquals(Boolean.TRUE, Locks.eventHybrid().write(new LockAction() {
            public Object run() {
                return new Boolean(EventQueue.isDispatchThread() &&
                                   Locks.eventHybrid().canRead() &&
                                   Locks.eventHybrid().canWrite());
            }
        }));
        assertEquals(Boolean.TRUE, Locks.eventHybrid().write(new LockExceptionAction() {
            public Object run() throws Exception {
                return new Boolean(EventQueue.isDispatchThread() &&
                                   Locks.eventHybrid().canRead() &&
                                   Locks.eventHybrid().canWrite());
            }
        }));
        final boolean[] b = new boolean[1];
        // Test that while in AWT randomly, can read but not write.
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                b[0] = EventQueue.isDispatchThread() &&
                       Locks.eventHybrid().canRead() &&
                       !Locks.eventHybrid().canWrite();
            }
        });
        assertTrue(b[0]);
        // While in AWT, can enter read or write directly.
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                b[0] = ((Boolean)Locks.eventHybrid().read(new LockAction() {
                    public Object run() {
                        return new Boolean(EventQueue.isDispatchThread() &&
                                           Locks.eventHybrid().canRead() &&
                                           !Locks.eventHybrid().canWrite());
                    }
                })).booleanValue();
            }
        });
        assertTrue(b[0]);
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                b[0] = ((Boolean)Locks.eventHybrid().write(new LockAction() {
                    public Object run() {
                        return new Boolean(EventQueue.isDispatchThread() &&
                                           Locks.eventHybrid().canRead() &&
                                           Locks.eventHybrid().canWrite());
                    }
                })).booleanValue();
            }
        });
        assertTrue(b[0]);
    }
    
    public void testEventHybridLocking() throws Exception {
        // XXX test locking out readers by writer or vice-versa
    }
    
    public void testEventHybridReadToWriteForbidden() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        Locks.eventHybrid().read(new LockAction() {
            public Object run() {
                assertTrue(!EventQueue.isDispatchThread());
                assertTrue(Locks.eventHybrid().canRead());
                assertTrue(!Locks.eventHybrid().canWrite());
                try {
                    Locks.eventHybrid().write(new LockAction() {
                        public Object run() {
                            fail();
                            return null;
                        }
                    });
                } catch (IllegalStateException e) {
                    // OK.
                }
                return null;
            }
        });
        final Error[] err = new Error[1];
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    Locks.eventHybrid().read(new LockAction() {
                        public Object run() {
                            assertTrue(EventQueue.isDispatchThread());
                            assertTrue(Locks.eventHybrid().canRead());
                            assertTrue(!Locks.eventHybrid().canWrite());
                            try {
                                Locks.eventHybrid().write(new LockAction() {
                                    public Object run() {
                                        fail();
                                        return null;
                                    }
                                });
                            } catch (IllegalStateException e) {
                                // OK.
                            }
                            return null;
                        }
                    });
                } catch (Error e) {
                    err[0] = e;
                }
            }
        });
        if (err[0] != null) throw err[0];
        Locks.eventHybrid().write(new LockAction() {
            public Object run() {
                assertTrue(Locks.eventHybrid().canWrite());
                Locks.eventHybrid().read(new LockAction() {
                    public Object run() {
                        assertTrue(Locks.eventHybrid().canRead());
                        assertTrue(!Locks.eventHybrid().canWrite());
                        try {
                            Locks.eventHybrid().write(new LockAction() {
                                public Object run() {
                                    fail();
                                    return null;
                                }
                            });
                        } catch (IllegalStateException e) {
                            // OK.
                        }
                        return null;
                    }
                });
                return null;
            }
        });
    }
    
    public void testEventHybridWriteToReadPermitted() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        Locks.eventHybrid().write(new LockAction() {
            public Object run() {
                assertTrue(EventQueue.isDispatchThread());
                try {
                    Locks.eventHybrid().read(new LockAction() {
                        public Object run() {
                            // OK.
                            return null;
                        }
                    });
                } catch (IllegalStateException e) {
                    fail(e.toString());
                }
                return null;
            }
        });
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                Locks.eventHybrid().write(new LockAction() {
                    public Object run() {
                        assertTrue(EventQueue.isDispatchThread());
                        try {
                            Locks.eventHybrid().read(new LockAction() {
                                public Object run() {
                                    // OK.
                                    return null;
                                }
                            });
                        } catch (IllegalStateException e) {
                            fail(e.toString());
                        }
                        return null;
                    }
                });
            }
        });
    }
    
    public void testEventHybridPostedRequests() throws Exception {
        // XXX postRR, postWR
    }
    
    public void testEventHybridExceptions() throws Exception {
        assertTrue("Not starting in AWT", !EventQueue.isDispatchThread());
        // test that checked excs from M.EA throw correct ME
        try {
            Locks.eventHybrid().read(new LockExceptionAction() {
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
            Locks.eventHybrid().read(new LockExceptionAction() {
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
            Locks.eventHybrid().read(new LockAction() {
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
            Locks.eventHybrid().read(new Runnable() {
                public void run() {
                    throw new IllegalArgumentException();
                }
            });
            fail();
        } catch (IllegalArgumentException e) {
            // OK.
        }
        try {
            Locks.eventHybrid().write(new Runnable() {
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
