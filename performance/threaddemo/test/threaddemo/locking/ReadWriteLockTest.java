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

import java.lang.ref.*;
import java.util.*;
import junit.framework.*;

/**
 * Test behavior of Locks.readWrite and PrivilegedLock.
 * @author Jaroslav Tulach, Ales Novak, Petr Hrebejk, Jesse Glick
 */
public class ReadWriteLockTest extends TestCase {
    
    private PrivilegedLock p;
    private Lock m;
    
    public ReadWriteLockTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(ReadWriteLockTest.class);
    }
    
    protected void setUp() {
        p = new PrivilegedLock();
        m = Locks.readWrite("test", p, 0);
    }
    
    public void testReadWriteRead() throws Exception {
        
        final Object lock = new Object();
        
        synchronized ( lock ) {
            p.enterRead();
            
            new Thread() {
                public void run() {
                    synchronized( lock ) {
                        lock.notifyAll();
                    }
                    p.enterWrite();
                    synchronized ( lock ) {
                        lock.notifyAll();
                        p.exitWrite();
                    }
                }
            }.start();
            
            lock.wait();
            
        }
        Thread.sleep(100);
        
        p.enterRead();
        
        p.exitRead();
        
        synchronized ( lock ) {
            p.exitRead();
            lock.wait();
        }
        
        assertTrue(!m.canRead());
        assertTrue(!m.canWrite());
    }
    
    /** Simple test to execute read access and write access immediately.
     */
    public void testPostImmediately() {
        State s = new State();
        
        m.read(s);
        
        if (s.state != 1) {
            fail("Read request not started immediatelly");
        }
        
        m.write(s);
        
        if (s.state != 2) {
            fail("Write request not started immediately");
        }
    }
    
    // starts a new thread, after return the thread will hold lock "p" in
    // mode X for timeout milliseconds
    private static void asyncEnter(final PrivilegedLock p, final boolean X, final long timeout) throws InterruptedException {
        asyncEnter(p, X, timeout, null);
    }
    
    // starts a new thread, after return the thread will hold lock "p" in
    // mode X for timeout milliseconds, the new thread execs "run" first
    private static void asyncEnter(final PrivilegedLock p, final boolean X, final long timeout, final Runnable run) throws InterruptedException {
        final Object lock = new Object();
        
        synchronized (lock) {
            new Thread(new Runnable() {
                public void run() {
                    if (X) {
                        p.enterWrite();
                    } else {
                        p.enterRead();
                    }
                    
                    synchronized (lock) {
                        lock.notify();
                    }
                    
                    if (run != null) {
                        run.run();
                    }
                    
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    if (X) {
                        p.exitWrite();
                    } else {
                        p.exitRead();
                    }
                    
                }
            }).start();
            
            lock.wait();
        }
    }
    
    /** Tests enterWrite while the Lock is contended in X mode by
     * another thread
     */
    public void testXContendedX() throws InterruptedException {
        asyncEnter(p, true, 2000);
        
        // first enter
        p.enterWrite();
        p.exitWrite();
        
        consistencyCheck();
    }
    
    /** Tests enterRead while the Lock is contended in X mode by
     * another thread
     */
    public void testXContendedS() throws InterruptedException {
        asyncEnter(p, true, 2000);
        
        // first enter
        p.enterRead();
        p.exitRead();
        
        consistencyCheck();
    }
    
    /** Tests enterWrite while the Lock is contended in S mode by
     * another thread
     */
    public void testSContendedX() throws InterruptedException {
        asyncEnter(p, false, 2000);
        
        // first enter
        p.enterWrite();
        p.exitWrite();
        
        consistencyCheck();
    }
    
    /** Tests enterRead while the Lock is contended in S mode by
     * another thread
     */
    public void testSContendedS() throws InterruptedException {
        asyncEnter(p, false, 2000);
        
        // first enter
        p.enterRead();
        p.exitRead();
        
        consistencyCheck();
    }
    
    /** Checks the Lock is in the consistent state, i.e. enterWrite must pass */
    private void consistencyCheck() {
        p.enterWrite();
        p.exitWrite();
    }
    
    private static class State implements Runnable {
        public int state;
        
        public void run() {
            state++;
        }
        
    } // end of State
    
    // --- TESTS ADDED BY JGLICK ---
    
    public void testNestedEntries() throws Exception {
        // can go write -> read
        p.enterWrite();
        try {
            p.enterRead();
            p.exitRead();
        } finally {
            p.exitWrite();
        }
        // and write -> write -> read
        p.enterWrite();
        try {
            p.enterWrite();
            try {
                p.enterRead();
                p.exitRead();
            } finally {
                p.exitWrite();
            }
        } finally {
            p.exitWrite();
        }
        // and write -> read -> read
        p.enterWrite();
        try {
            p.enterRead();
            try {
                p.enterRead();
                p.exitRead();
            } finally {
                p.exitRead();
            }
        } finally {
            p.exitWrite();
        }
        // and even write -> write -> read -> read
        p.enterWrite();
        try {
            p.enterWrite();
            try {
                p.enterRead();
                try {
                    p.enterRead();
                    p.exitRead();
                } finally {
                    p.exitRead();
                }
            } finally {
                p.exitWrite();
            }
        } finally {
            p.exitWrite();
        }
        // but read -> write is forbidden
        p.enterRead();
        try {
            boolean ok = true;
            try {
                p.enterWrite();
                ok = false;
                p.exitWrite();
            } catch (IllegalStateException e) {
                assertTrue(ok);
            }
        } finally {
            p.exitRead();
        }
        // so is write -> read -> write!
        p.enterWrite();
        try {
            p.enterRead();
            try {
                boolean ok = true;
                try {
                    p.enterWrite();
                    ok = false;
                    p.exitWrite();
                } catch (IllegalStateException e) {
                    assertTrue(ok);
                }
            } finally {
                p.exitRead();
            }
        } finally {
            p.exitWrite();
        }
    }
    
    public void testWriteLater() throws Exception {
        assertTrue(!m.canWrite());
        assertTrue(!m.canRead());
        final boolean[] b = new boolean[1];
        synchronized (b) {
            m.writeLater(new Runnable() {
                public void run() {
                    synchronized (b) {
                        b[0] = m.canWrite();
                    }
                }
            });
            b.wait(1000);
            assertTrue(b[0]);
        }
        b[0] = false;
        synchronized (b) {
            p.enterRead();
            try {
                m.writeLater(new Runnable() {
                    public void run() {
                        synchronized (b) {
                            b[0] = m.canWrite();
                        }
                    }
                });
            } finally {
                p.exitRead();
            }
            b.wait(1000);
            assertTrue(b[0]);
        }
    }
    
}
