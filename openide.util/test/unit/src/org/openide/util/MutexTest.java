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

package org.openide.util;

import java.io.IOException;
import java.lang.ref.*;
import java.util.*;
import org.openide.ErrorManager;
import junit.framework.*;
import org.netbeans.junit.*;

public class MutexTest extends NbTestCase {
    Mutex.Privileged p;
    Mutex m;
    
    
    
    public MutexTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(MutexTest.class);
        
        return suite;
    }
    
    /** Sets up the test.
     */
    protected void setUp () {
        p = new Mutex.Privileged ();
        m = new Mutex (p);
        Mutex.beStrict = true;
    }
    
    public void testReadWriteRead() throws Exception {
        
        final Object lock = new Object();
        final Mutex.Privileged mPriv =  new Mutex.Privileged();
        final Mutex m = new Mutex( mPriv );
        
        synchronized ( lock ) {
            mPriv.enterReadAccess();
            
            new Thread() {
                public void run () {
                    synchronized( lock ) {
                        lock.notifyAll();
                    }
                    mPriv.enterWriteAccess();
                    synchronized ( lock ) {
                        lock.notifyAll();
                        mPriv.exitWriteAccess();
                    }
                }
            }.start();
            
            lock.wait();
                       
        }
        Thread.sleep (100);
        
        mPriv.enterReadAccess();
        
        mPriv.exitReadAccess();
        
        synchronized ( lock ) {
            mPriv.exitReadAccess();
            lock.wait();
        }
    }
    
    /** Simple test to execute read access and write access imediatelly.
     */
    public void testPostImmediatelly () {
        State s = new State ();
        
        m.postReadRequest(s);
        
        if (s.state != 1) {
            fail ("Read request not started immediatelly");
        }
        
        m.postWriteRequest (s);
        
        if (s.state != 2) {
            fail ("Write request not started immediately");
        }
    }

    /** Behaviour of postWriteRequest is defined by this test.
     */
    public void testPostWriteRequest () {
        
        State s = new State ();
        
        // first enter
        p.enterWriteAccess ();
            p.enterReadAccess ();
        
            m.postWriteRequest(s);
            
            if (s.state != 0) {
                fail ("Write request started when we are in read access");
            }
                
            p.exitReadAccess ();
            
        if (s.state != 1) {
            fail ("Write request not run when leaving read access: " + s.state);
        }
                
        // exiting
        p.exitWriteAccess ();
        
        if (s.state != 1) {
            fail ("Run more times?: " + s.state);
        }
    }    
    
    /** Behaviour of postReadRequest is defined by this test.
     */
    public void testPostReadRequest () {
        
        State s = new State ();
        
        // first enter
            p.enterWriteAccess ();
        
            m.postReadRequest(s);
            
            if (s.state != 0) {
                fail ("Read request started when we are in write access");
            }
                
            p.exitWriteAccess ();
            
            if (s.state != 1) {
                fail ("Read request not run when leaving write access: " + s.state);
            }
                
        
        if (s.state != 1) {
            fail ("Run more times?: " + s.state);
        }
    }
    
    /** Test enter from S mode to X mode *
    public void testXtoS() {
        State s = new State ();
        
        p.enterReadAccess ();
        p.enterWriteAccess ();
        s.run();
        p.exitWriteAccess();
        p.exitReadAccess();
        if (s.state != 1) {
            fail ("Run more times?: " + s.state);
        }
    }
*/

    /** Tests posting write and read requests while the Mutex is held
     * in X mode and was entered in S mode as well
     */
    public void testPostWriteReadRequests() {
        State s = new State ();
        
        // first enter
        p.enterWriteAccess ();
            p.enterReadAccess ();
        
            m.postWriteRequest(s);
            
            if (s.state != 0) {
                fail ("Write request started when we are in read access");
            }
                
            m.postReadRequest(s);
            
            if (s.state != 0) {
                fail ("Read request started when we are in write access");
            }
            
            p.exitReadAccess ();
            
            if (s.state != 1) {
                fail ("Write request not run when leaving read access: " + s.state);
            }
            
        // exiting
        p.exitWriteAccess ();
        
        if (s.state != 2) {
            fail ("Read request not run when leaving write access: " + s.state);
        }
        
        consistencyCheck();
    }
       
    /** Tests simple postWriteRequest */
    public void testSimplePostWriteRequest() {
        State s = new State ();
        
        m.postWriteRequest(s);
        
        if (s.state != 1) {
            fail ("Write request not run: " + s.state);
        }
        
        consistencyCheck();
    }
    
    /** Tests simple postReadRequest */
    public void testSimplePostReadRequest() {
        State s = new State ();
        
        m.postReadRequest(s);
        
        if (s.state != 1) {
            fail ("Read request not run: " + s.state);
        }
        
        consistencyCheck();
    }
    
    // starts a new thread, after return the thread will hold lock "p" in
    // mode X for timeout milliseconds
    private static void asyncEnter(final Mutex.Privileged p, final boolean X, final long timeout) throws InterruptedException {
        asyncEnter(p, X, timeout, null);
    }

     // starts a new thread, after return the thread will hold lock "p" in
    // mode X for timeout milliseconds, the new thread execs "run" first
    private static void asyncEnter(final Mutex.Privileged p, final boolean X, final long timeout, final Runnable run) throws InterruptedException {
        final Object lock = new Object();
        
        synchronized (lock) {
            new Thread(new Runnable() {
                public void run() {
                    if (X) {
                        p.enterWriteAccess();
                    } else {
                        p.enterReadAccess();
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
                        p.exitWriteAccess();
                    } else {
                        p.exitReadAccess();
                    }
                    
                }
            }).start();
            
            lock.wait();
        }
    }
    
    /** Tests enterWriteAccess while the Mutex is contended in X mode by
     * another thread
     */
    public void testXContendedX() throws InterruptedException {
        asyncEnter(p, true, 2000);
        
        // first enter
        p.enterWriteAccess();
        p.exitWriteAccess();
        
        consistencyCheck();
    }
    
    /** Tests enterReadAccess while the Mutex is contended in X mode by
     * another thread
     */
    public void testXContendedS() throws InterruptedException {
        asyncEnter(p, true, 2000);
        
        // first enter
        p.enterReadAccess();
        p.exitReadAccess();
        
        consistencyCheck();
    }
    
    /** Tests enterWriteAccess while the Mutex is contended in S mode by
     * another thread
     */
    public void testSContendedX() throws InterruptedException {
        asyncEnter(p, false, 2000);
        
        // first enter
        p.enterWriteAccess();
        p.exitWriteAccess();
        
        consistencyCheck();
    }
    
    /** Tests enterReadAccess while the Mutex is contended in S mode by
     * another thread
     */
    public void testSContendedS() throws InterruptedException {
        asyncEnter(p, false, 2000);
        
        // first enter
        p.enterReadAccess();
        p.exitReadAccess();
        
        consistencyCheck();
    }
    
    /** Tests postWriteRequest while the Mutex is contended in X mode by
     * another thread
     */
    public void testXContendedPx() throws InterruptedException {
        asyncEnter(p, true, 2000);
        
        State s = new State ();
        
        m.postWriteRequest(s);
        
        if (s.state != 1) {
            fail ("Write request not run: " + s.state);
        }
        
        consistencyCheck();
    }
    
    /** Tests postReadRequest while the Mutex is contended in X mode by
     * another thread
     */
    public void testXContendedPs() throws InterruptedException {
        asyncEnter(p, true, 2000);
        
        State s = new State ();
        
        m.postReadRequest(s);
        
        if (s.state != 1) {
            fail ("Read request not run: " + s.state);
        }
        
        consistencyCheck();
    }
    
    /** Tests postWriteRequest while the Mutex is contended in S mode by
     * another thread
     */
    public void testSContendedPx() throws InterruptedException {
        asyncEnter(p, false, 2000);
        
        State s = new State ();
        
        m.postWriteRequest(s);
        
        if (s.state != 1) {
            fail ("Write request not run: " + s.state);
        }
        
        consistencyCheck();
    }
    
    /** Tests postReadRequest while the Mutex is contended in S mode by
     * another thread
     */
    public void testSContendedPs() throws InterruptedException {
        asyncEnter(p, false, 2000);
        
        State s = new State ();
        
        m.postReadRequest(s);
        
        if (s.state != 1) {
            fail ("Write request not run: " + s.state);
        }
        
        consistencyCheck();
    }
    
    /** Tests postWriteRequest and postReadRequest while the Mutex is contended in S mode by
     * another thread as well as this thread.
     */
    public void testSContendedSPsPx() throws InterruptedException {
        asyncEnter(p, false, 2000);
        
        State s = new State ();
        
        p.enterReadAccess();
        m.postReadRequest(s);
        
        if (s.state != 1) {
            fail ("Read request not run: " + s.state);
        }
        
        m.postWriteRequest(s);
        
        if (s.state != 1) {
            fail ("Write request run: " + s.state);
        }
        
        p.exitReadAccess();
        
        if (s.state != 2) {
            fail ("Write request not run: " + s.state);
        }
        
        consistencyCheck();
    }
    
    /** The Mutex is held in S mode by a thread which also posted a
     * write request. Another thread tries enterWriteAccess.
     */
    public void testSPxContendedX() throws Exception {
        final State s = new State ();

        asyncEnter(p, false, 2000, new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                m.postWriteRequest(s);
                if (s.state == 1) {
                    fail ("Write request run: " + s.state);
                }
            }
        });
        
        p.enterWriteAccess();
        if (s.state != 1) {
            fail ("Write request not run: " + s.state);
        }
        p.exitWriteAccess();
        
        consistencyCheck();
    }
    
    /**
     * Test case for #16577. Grab X,S and post X request,
     * the second thread waits for X, causing the mutex to be 
     * in CHAINED.
     */
    public void testXSPxContendedX() throws Exception {
        final State s = new State ();

        asyncEnter(p, true, 2000, new Runnable() {
            public void run() {
                p.enterReadAccess();
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                m.postWriteRequest(s);
                p.exitReadAccess();
                
                if (s.state != 1) {
                    fail ("Write request not run: " + s.state);
                }
            }
        });
        
        p.enterWriteAccess();
        p.exitWriteAccess();
        
        consistencyCheck();
    }
    
    /**
     * The scenario:
     * - Have 3 threads, A, B and C
     * - writeLock mutex1 in A
     * - writeLock mutex2 in B
     *   - postReadLock mutex2 in B
     *     - writeLock mutex1 in B
     * - writeLock mutex2 in C
     *   - readLock mutex2 in A
     * - leaveWriteLock mutex2 in B
     *
     */
    public void testStarvation49466() throws Exception {
        final Mutex.Privileged pr1 = new Mutex.Privileged();
        final Mutex mutex1 = new Mutex(pr1);
        
        final Mutex.Privileged pr2 = new Mutex.Privileged();
        final Mutex mutex2 = new Mutex(pr2);
        
        final boolean[] done = new boolean[3];

        final Ticker tick0 = new Ticker();
        final Ticker tick1 = new Ticker();
        final Ticker tick2 = new Ticker();
        final Ticker tick3 = new Ticker();
        
        Thread A = new Thread() {
            public void run() {
                pr1.enterWriteAccess();
                tick0.tick();
                
                tick1.waitOn();
                
                pr2.enterReadAccess();
                done[0] = true;
                pr2.exitReadAccess();
                    
                pr1.exitWriteAccess();
            }        
        };

        // writeLock mutex1 in A
        A.start();
        tick0.waitOn();

        
        Thread B = new Thread() {
            public void run() {
                pr2.enterWriteAccess();
                
                mutex2.postReadRequest(new Runnable() {
                   public void run() {
                       tick0.tick();
                       pr1.enterWriteAccess();
                       done[1] = true;
                       pr1.exitWriteAccess();
                   } 
                });

                tick0.tick();
                
                tick2.waitOn();
                
                pr2.exitWriteAccess();
            }        
        };
        
        // writeLock mutex2 in B
        B.start();
        tick0.waitOn();

/*
 * The test fails even when using only first two threads.
 *
        Thread C = new Thread() {
            public void run() {
                tick0.tick(); // have to tick in advance and wait :-(
                pr2.enterWriteAccess();
                done[2] = true;
                pr2.exitWriteAccess();
            }        
        };
        
        // writeLock mutex2 in C
        C.start();
        tick0.waitOn();
        Thread.sleep(1000); // between tick and C enqueued ...
*/
        
        // readLock mutex2 in A
        tick1.tick(); // enqueues A in mutex2's queue
        Thread.sleep(1000); // between tick and A enqueued ...
        
        // leaveWriteLock mutex2 in B
        tick2.tick();
        
        //  postReadLock mutex2 in B
        tick0.waitOn();

        // System.err.println("Do a thread dump now!");
        Thread.sleep(2000); // give them some time ...
        
        assertTrue("Thread A finished", done[0]);
        assertTrue("Thread B finished", done[1]);
        
//        assertTrue("Thread C succeed", done[2]);
    }
    
    private static class Ticker {
        boolean state;
        
        public void waitOn() {
            synchronized(this) {
                while (!state) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new InternalError();
                    }
                }
                state = false; // reusable
            }
        }
        
        public void tick() {
            synchronized(this) {
                state = true;
                notifyAll();
            }
        }
    }
    
    
    
    /**
     * Grab X and post S request,
     * the second thread waits for X, causing the mutex to be 
     * in CHAINED.
     */
    public void testXPsContendedX() throws Exception {
        final State s = new State ();

        asyncEnter(p, true, 2000, new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                m.postReadRequest(s);
                
                if (s.state == 1) {
                    fail ("Read request run: " + s.state);
                }
            }
        });
        
        p.enterWriteAccess();
        Thread.sleep(4000);
        p.exitWriteAccess();
        
        consistencyCheck();
    }
    
    /** Checks the Mutex is in the consistent state, i.e. enterWriteAccess must pass */
    private void consistencyCheck() {
        p.enterWriteAccess();
        p.exitWriteAccess();
    }
    
    public void testNoWayToDoReadAndThenWrite () {
        class R implements Runnable {
            public void run () {
                m.writeAccess (this);
            }
        }
        
        try {
            m.readAccess (new R ());
            fail ("This is supposed to throw an IllegalStateException");
        } catch (IllegalStateException ex) {
            // ok, this is expected
        }
    }

    public void testNoWayToDoWriteThenReadAndThenWrite () {
        class R implements Runnable {
            public boolean second;
            public boolean end;
            public boolean ending;
            
            public void run () {
                if (end) {
                    ending = true;
                    return;
                }
                
                if (second) {
                    end = true;
                    m.writeAccess (this);
                } else {
                    second = true;
                    m.readAccess (this);
                }
            }
        }
        R r = new R ();
        try {
            m.writeAccess (r);
            fail ("This is supposed to throw an IllegalStateException");
        } catch (IllegalStateException ex) {
            // ok, this is expected
            assertTrue ("We were in the write access section", r.second);
            assertTrue ("We were before the writeAcess(this)", r.end);
            assertFalse ("We never reached ending", r.ending);
        }
    }
    
    public void testIsOrIsNotInReadOrWriteAccess () {
        new ReadWriteChecking ("No r/w", Boolean.FALSE, Boolean.FALSE).run ();
        m.readAccess (new ReadWriteChecking ("r but no w", Boolean.TRUE, Boolean.FALSE));
        m.writeAccess (new ReadWriteChecking ("w but no r", Boolean.FALSE, Boolean.TRUE));
        m.readAccess (new Runnable () {
            public void run () {
                m.postReadRequest (new ReadWriteChecking ("+r -w", Boolean.TRUE, Boolean.FALSE));
            }
        });
        m.readAccess (new Runnable () {
            public void run () {
                m.postWriteRequest (new ReadWriteChecking ("-r +w", Boolean.FALSE, Boolean.TRUE));
            }
        });
        m.writeAccess (new Runnable () {
            public void run () {
                m.postReadRequest (new ReadWriteChecking ("+r -w", Boolean.TRUE, Boolean.FALSE));
            }
        });
        m.writeAccess (new Runnable () {
            public void run () {
                m.postWriteRequest (new ReadWriteChecking ("-r +w", Boolean.FALSE, Boolean.TRUE));
            }
        });
        
        // write->read->test (downgrade from write to read)
        m.writeAccess (new Runnable () {
            public boolean second;
            
            public void run () {
                if (!second) {
                    second = true;
                    m.readAccess (this);
                    return;
                }
                
                class P implements Runnable {
                    public boolean exec;
                    public void run () {
                        exec = true;
                    }
                }
                P r = new P ();
                P w = new P ();
                m.postWriteRequest (w);
                m.postReadRequest (r);
                assertFalse ("Writer not executed", w.exec);
                assertFalse ("Reader not executed", r.exec);
                
                m.readAccess (new ReadWriteChecking ("+r +w", Boolean.TRUE, Boolean.TRUE));
            }
        });
        
        new ReadWriteChecking ("None at the end", Boolean.FALSE, Boolean.FALSE).run ();
    }
    
    public void testIsReadOrWriteForEventMutex () throws Exception {
        class Test implements Runnable {
            public boolean isRead;
            public boolean isWrite;
            
            public void run () {
                isRead = Mutex.EVENT.isReadAccess ();
                isWrite = Mutex.EVENT.isWriteAccess ();
            }
        }
        
        Test rp = new Test ();
        org.openide.util.RequestProcessor.getDefault ().post(rp).waitFinished ();
        
        Test awt = new Test ();
        javax.swing.SwingUtilities.invokeAndWait (awt);
        
        assertFalse ("Nothing in RP", rp.isRead);
        assertFalse ("No in RP", rp.isWrite);
        
        assertTrue ("Is read in ", awt.isRead);
        assertTrue ("is also write", awt.isWrite);        
    }

    private void exceptionsReporting(final Throwable t) throws Exception {
        final IOException e1 = new IOException();
        final Mutex mm = m;
        final Runnable secondRequest = new Runnable() {
            public void run() {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                } else {
                    throw (Error)t;
                }
            }
        };
        Mutex.ExceptionAction firstRequest = new Mutex.ExceptionAction() {
            public Object run () throws Exception {
                mm.postWriteRequest(secondRequest);
                throw e1;
            }
        };
        try {
            m.readAccess(firstRequest);
        } catch (MutexException mu) {
            Exception e = mu.getException();
            assertEquals("IOException correctly reported", e, e1);
            return;
        } catch (Throwable e) {
            fail("a problem in postWriteRequest() should not swallow any " +
                    "exception thrown in readAccess() because that might be " +
                    "the cause of the problem. "+e.toString());
        }
        fail("should never get here");
    }

    /* Test case for #51911. Failing at the moment */
    public void testThrowingAssertionErrorInSpecialCase() throws Exception {
        exceptionsReporting(new AssertionError());
    }
    
    public void testThrowingRuntimeExceptionInSpecialCase() throws Exception {
        exceptionsReporting(new RuntimeException());
    } /**/
    
    private class ReadWriteChecking implements Runnable {
        public Boolean read;
        public Boolean write;
        public String msg;

        public ReadWriteChecking (String msg, Boolean read, Boolean write) {
            assertNotNull ("Msg cannot be null", msg);
            this.msg = msg;
            this.read = read;
            this.write = write;
        }

        protected void finalize () {
            assertNull ("Run method was not called!", msg);
        }

        public void run () {
            if (write != null) assertEquals (msg, write.booleanValue (), m.isWriteAccess ());
            if (read != null) assertEquals (msg, read.booleanValue (), m.isReadAccess ());
            msg = null;
        }
    }
    
    
    private static class State implements Runnable {
        public int state;

        public void run () {
            state++;
        }
        
    } // end of State            
}
