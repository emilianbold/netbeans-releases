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

package org.openide.util;

import java.lang.ref.*;
import java.util.*;
import org.openide.ErrorManager;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.util.Task;

public class RequestProcessorTest extends NbTestCase {
    
    public RequestProcessorTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(RequestProcessorTest.class);
        
        return suite;
    }
    
    
    /** A test to check that objects are executed in the right order.
     */
    public void testOrder () throws Exception {
        final int[] count = new int[1];
        final String[] fail = new String[1];
        
        class X extends Object 
        implements Runnable, Comparable {
            public int order;
            
            public void run () {
                if (order != count[0]++) {
                    if (fail[0] == null) {
                        fail[0] = "Executing task " + order + " instead of " + count[0];
                    }
                }
            }
            
            public int compareTo (Object o) {
                X x = (X)o;
                
                return System.identityHashCode (x) - System.identityHashCode (this);
            }
            
            public String toString () {
                return "O: " + order;
            }
        };
        
        // prepare the tasks 
        X[] arr = new X[10];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new X ();
        }
        
        // sort it
//        Arrays.sort (arr);
        
        for (int i = 0; i < arr.length; i++) {
            arr[i].order = i;
        }
        
        // execute the task as quickly as possible (only those with the same time
        // can have wrong order
        RequestProcessor.Task[] wait = new RequestProcessor.Task[arr.length];
        for (int i = 0; i < arr.length; i++) {
            wait[i] = RequestProcessor.postRequest (arr[i]);
        }
        
        // wait to all tasks to finish
        for (int i = 0; i < arr.length; i++) {
            wait[i].waitFinished ();
        }
        
        if (fail[0] != null) {
            fail (fail[0]);
        }
            
    }

    /**
     * A test that check that priorities are handled well.
     */
    public void testPriorityQueue() throws Exception {
        
        final Runnable[] arr = new Runnable[5];
        
        class R implements Runnable {
            
            public int index;
            
            public R (int i) {
                index = i;
            }
            
            public synchronized void run () {
                for (int i = 0; /*i < arr.length*/; i++) {
                    if (arr[i] == null) {
                        arr[i] = this;
                        break;
                    }
                }
                
            }
            
            public String toString () {
                return " R index " + index;
            }
        }       

        Runnable r[] = new Runnable[5];
        // expected order of execution
        for (int i = 0; i<5; i++) {
            r[i] = new R(i);
        }
        
        RequestProcessor rp = new RequestProcessor("PrioriyTest");
        
        RequestProcessor.Task t[] = new RequestProcessor.Task[5];
        synchronized (r[0]) {
            t[4] = rp.post(r[0], 0, 3);
            t[0] = rp.post(r[4], 0, 1);
            t[1] = rp.post(r[2], 0, 2);
            t[2] = rp.post(r[1], 0, 2);
            t[3] = rp.post(r[3], 0, 2);            
            t[2].setPriority(3);
        }
        
        for (int i = 0; i<5; i++) {
            t[i].waitFinished();
        }
        
        for (int i = 0; i<5; i++) {
            R next = (R) arr[i];
            if (next.index != i) fail("Expected at " + i + " but was " +  next.index);
        }
    }
    
    /** Test bug http://www.netbeans.org/issues/show_bug.cgi?id=31906
     */
    public void testBug31906_SimulateDataFolderTest () {
        RequestProcessor rp = new RequestProcessor ("dataFolderTest");
        
        class X implements Runnable {
            private RequestProcessor.Task wait;
            private int cnt;
            
            public synchronized void run () {
                if (wait != null) {
                    wait.waitFinished ();
                    cnt++;
                } else {
                    cnt++;
                }
            }
            
            public synchronized void assertCnt (String msg, int cnt) {
                assertEquals (msg, cnt, this.cnt);
                this.cnt = 0;
            }
            
            public synchronized void waitFor (RequestProcessor.Task t) {
                wait = t;
            }
            
        }
        X[] arr = { new X(), new X() };
        RequestProcessor.Task[] tasks = { 
            rp.create (arr[0]), 
            rp.create (arr[1])
        };
        tasks[0].setPriority(Thread.NORM_PRIORITY - 1);
        tasks[1].setPriority(Thread.NORM_PRIORITY + 1);
        
        tasks[0].schedule(0);
        tasks[1].schedule(0);

        tasks[0].waitFinished();
        arr[0].assertCnt (" Once", 1);
        tasks[1].waitFinished ();
        arr[1].assertCnt (" Once as well", 1);

        tasks[0].schedule(100);
        tasks[1].schedule(100);
        tasks[0].schedule(10);
        tasks[1].schedule(10);

        tasks[0].waitFinished();
        tasks[1].waitFinished();

        arr[0].assertCnt (" 1a", 1);
        arr[1].assertCnt (" 1b", 1);

        arr[0].waitFor (tasks[1]);
        tasks[1].schedule(100);
        tasks[0].schedule(10);
        tasks[0].waitFinished ();
        arr[0].assertCnt (" task 0 is executed", 1);
        arr[1].assertCnt (" but it also executes task 1", 1);

        tasks[0].schedule(10);
        tasks[0].waitFinished ();
        arr[0].assertCnt (" task O is executed", 1);
        arr[1].assertCnt (" but it does not execute 1", 0);
    }
    
    
    /** Test priority inversion and whether it is properly notified
     */
    public void testPriorityInversionProblemAndItsDiagnosis () throws Exception {
        RequestProcessor rp = new RequestProcessor ("testPriorityInversionProblemAndItsDiagnosis");
        
        final Runnable[] arr = new Runnable[3];
        
        class R implements Runnable {
            
            public int index;
            public Task t;
            
            public R (int i) {
                index = i;
            }
            
            public synchronized void run () {
                for (int i = 0; /*i < arr.length*/; i++) {
                    if (arr[i] == null) {
                        arr[i] = this;
                        break;
                    }
                }
                
                if (t != null) {
                    t.waitFinished ();
                }
            }
            
            public String toString () {
                return " R index " + index;
            }
        }
         
        R r1 = new R (1);
        R r2 = new R (2);
        R r3 = new R (3);
        
        Task t1;
        Task t2;
        Task t3;
        
        synchronized (r1) {
            t1 = rp.post (r1);
            t2 = rp.post (r2);
            
            // r1 will call the waitFinished of r3
            r1.t = t3 = rp.post (r3);
        }
        
        t1.waitFinished ();
        t2.waitFinished ();
        t3.waitFinished ();
        
        assertEquals ("First started is t1", r1, arr[0]);
        assertEquals ("Second started is t3", r3, arr[1]);
        assertEquals ("Last started is t2", r2, arr[2]);
        
        // now we should ensure that the RP warned everyone about the 
        // priority inheritance and all its possible complications (t2 running
        // later than t3)
    }
    
    /** Test of finalize method, of class org.openide.util.RequestProcessor. */
    public void testFinalize() throws Exception {
        RequestProcessor rp = new RequestProcessor ("toGarbageCollect");
        Reference ref = new WeakReference (rp);
        Reference task;
        
        final Object lock = new Object ();
        
        
        synchronized (lock) {
            task = new WeakReference (rp.post (new Runnable () {
                public void run () {
                    synchronized (lock) {
                        lock.notify ();
                    }
                }
            }));
            
            
            rp = null;

            doGc (10, null);
            
            if (ref.get () == null) {
                fail ("garbage collected even a task is planed."); // NOI18N
            }
            
            // run the task
            lock.wait ();
            
        }
        
        doGc (1000, task);
        
        if (task.get () != null) {
            fail ("task is not garbage collected.");
        }
        
        doGc (1000, ref);
        if (ref.get () != null) {
            fail ("not garbage collected at all."); // NOI18N
        }
        
    }
    
    /** Check whether task is finished when it should be.
     */
    public void testCheckFinished () {
        RequestProcessor rp = new RequestProcessor ("Finish");
        
        class R extends Object implements Runnable {
            RequestProcessor.Task t;
            
            public void run () {
                if (t.isFinished ()) {
                    fail ("Finished when running");
                }
            }
        }
        
        R r = new R ();
        RequestProcessor.Task task = rp.create (r);
        r.t = task;

        if (task.isFinished ()) {
            fail ("Finished after creation");
        }
     
        task.schedule (200);
        
        if (task.isFinished ()) {
            fail ("Finished when planed");
        }
        
        task.waitFinished ();
        
        if (!task.isFinished ()) {
            fail ("Not finished after waitFinished");
        }
        
        task.schedule (200);
        
        if (task.isFinished ()) {
            fail ("Finished when planed");
        }
    }
        

    /** Test to check the waiting in request processor.
    */
    public void testWaitFinishedOnNotStartedTask () throws Exception {
        Counter x = new Counter ();
        final RequestProcessor.Task task = RequestProcessor.getDefault().create (x);
        
        //
        // Following code tests whether the RP.create().waitFinished really blocks
        // until somebody schedules the task.
        //
        class WaitThread extends Thread {
            public boolean finished;
            
            public void run () {
                task.waitFinished ();
                synchronized (this) {
                    finished = true;
                    notifyAll ();
                }
            }
            
            public synchronized void w (int timeOut) throws Exception {
                if (!finished) {
                    wait (timeOut);
                }
            }
        }
        WaitThread wt = new WaitThread ();
        wt.start ();
        wt.w (100);
        assertTrue ("The waitFinished has not ended, because the task has not been planned", !wt.finished);
        task.schedule (0);
        wt.w (0);
        assertTrue ("The waitFinished finished, as the task is now planned", wt.finished);
        x.assertCnt ("The task has been executed", 1);
    }
    
    /** Test to check the waiting in request processor.
    */
    public void testWaitFinishedOnNotStartedTaskFromRPThread () throws Exception {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testWaitFinishedOnNotStartedTaskFromRPThread");
        final RequestProcessor.Task task = rp.create (x);
        
        //
        // Following code tests whether the RP.create().waitFinished really blocks
        // until somebody schedules the task.
        //
        class WaitTask implements Runnable {
            public boolean finished;
            
            public synchronized void run () {
                task.waitFinished ();
                finished = true;
                notifyAll ();
            }
            
            public synchronized void w (int timeOut) throws Exception {
                if (!finished) {
                    wait (timeOut);
                }
            }
        }
        WaitTask wt = new WaitTask ();
        rp.post (wt);
        wt.w (0);
        assertTrue ("The task.waitFinished has to finish, otherwise the RequestProcessor thread will stay occupied forever", wt.finished);
        x.assertCnt ("The task has been executed - wait from RP made it start", 1);
    }
        
    public void testWaitFinished2 () {        
        Counter x = new Counter ();
        final RequestProcessor.Task task = RequestProcessor.getDefault().create (x);
        task.schedule (500);
        if (task.cancel ()) {
            // ok, task is canceled
            task.waitFinished ();
        }

        // does a task that is scheduled means that it is not finished?
        task.schedule (200);
        task.waitFinished();
        x.assertCnt ("Wait does not wait for finish of scheduled tasks, that already has been posted", 1);
    }
    
    /** Ensure that it is safe to call schedule() while the task is running
     * (should finish the task and run it again).
     */
    public void testScheduleWhileRunning() throws Exception {
        class X implements Runnable {
            public synchronized void run() {
                try {
                    if (cnt == 0) {
                        this.notify(); // #1
                        this.wait(9999); // #2
                        cnt++;
                    } else {
                        cnt++;
                        this.notify(); // #3
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            public int cnt = 0;
        }
        X x = new X();
        synchronized (x) {
            RequestProcessor.Task task = RequestProcessor.postRequest(x);
            x.wait(9999); // #1
            assertEquals(0, x.cnt);
            task.schedule(0);
            x.notify(); // #2
            x.wait(9999); // #3
            assertEquals(2, x.cnt);
        }
    }
    
    /** Make sure it is safe to call waitFinished() on a task from within
     * a task listener.
     */
    public void testWaitFinishedFromNotification() throws Exception {
        class X implements Runnable {
            private Task task;
            private int cnt;
            public synchronized Task start() {
                if (task == null) {
                    task = RequestProcessor.postRequest(this);
                }
                return task;
            }
            public void run() {
                cnt++;
            }
            public int getCount() {
                return cnt;
            }
            public void block() {
                start().waitFinished();
            }
        }
        final X x = new X();
        final Object lock = "wait for task to finish";
        final boolean[] finished = new boolean[1];
        x.start().addTaskListener(new TaskListener() {
            public void taskFinished(Task t) {
                x.block();
                finished[0] = true;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            lock.wait(5000);
        }
        assertTrue(finished[0]);
        assertEquals(1, x.getCount());
    }

    /** Make sure that successfully canceled task is not performed.
     */
    public void testCancel() throws Exception {
        class X implements Runnable {
            public boolean performed = false;
            public void run() {
                performed = true;
            }
        }
        
        X x = new X();
        final boolean[] finished = new boolean[1];
        finished[0] = false;
        
        // post task with some delay
        RequestProcessor.Task task = RequestProcessor.postRequest(x, 1000);
        task.addTaskListener(new TaskListener() {
            public void taskFinished(Task t) {
                finished[0] = true;
            }
        });

        boolean canceled = task.cancel();
        assertTrue("Task was not canceled", canceled);
        assertTrue("The taskFinished was not called for canceled task", finished[0]);
        Thread.sleep(1500); // wait longer than task delay
        assertTrue("Task was performed even if it is canceled", !x.performed);
    }
    
    public void testWaitWithTimeOutCanFinishEvenTheTaskHasNotRun () throws Exception {
        class Run implements Runnable {
            public boolean runned;
            public synchronized void run () {
                runned = true;
            }
        }
        
        Run run = new Run ();
        
        synchronized (run) {
            RequestProcessor.Task task = RequestProcessor.getDefault ().post (run);
            task.waitFinished (100);
            assertFalse ("We are here and the task has not finished", run.runned);
            assertFalse ("Not finished", task.isFinished ());
        }
    }
    
    public void testWhenWaitingForALimitedTimeFromTheSameProcessorThenInterruptedExceptionIsThrownImmediatelly () throws Exception {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testWaitFinishedOnNotStartedTaskFromRPThread");
        final RequestProcessor.Task task = rp.create (x);
        
        class WaitTask implements Runnable {
            public boolean finished;
            
            public synchronized void run () {
                long time = System.currentTimeMillis ();
                try {
                    task.waitFinished (1000);
                    fail ("This should throw an exception. Btw time was: " + (System.currentTimeMillis () - time));
                } catch (InterruptedException ex) {
                    // ok, this is expected
                } finally {
                    time = System.currentTimeMillis () - time;
                    notifyAll ();
                }
                if (time > 100) {
                    fail ("Exception should be thrown quickly. Was: " + time);
                }
                finished = true;
            }
            
        }
        WaitTask wt = new WaitTask ();
        synchronized (wt) {
            rp.post (wt);
            wt.wait ();
        }
        assertTrue ("The task.waitFinished has to finish", wt.finished);
        x.assertCnt ("The task has NOT been executed", 0);
    }
    
    public void testWhenWaitingForAlreadyFinishedTaskWithTimeOutTheResultIsGood () throws Exception {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testWaitFinishedOnStartedTaskFromRPThread");
        final RequestProcessor.Task task = rp.post (x);
        task.waitFinished ();
        x.assertCnt ("The task has been executed before", 1);
        
        class WaitTask implements Runnable {
            public boolean finished;
            
            public synchronized void run () {
                notifyAll ();
                try {
                    assertTrue ("The task has been already finished", task.waitFinished (1000));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail ("Should not happen");
                }
                finished = true;
            }
            
        }
        WaitTask wt = new WaitTask ();
        synchronized (wt) {
            rp.post (wt);
            wt.wait ();
        }
        assertTrue ("The task.waitFinished has to finish", wt.finished);
    }
    
    /**
     * A processing thread must survive throwable thrown during
     * execution of given taks. RuntimeException
     */
    public void testSurvivesException() throws Exception {
        doSurviveTest(false); // NPE
        doSurviveTest(true);  // AssertionError
    }


    private void doSurviveTest(final boolean error) throws Exception {
        RequestProcessor rp = new RequestProcessor("SurvivesTest");
        Counter x = new Counter ();
        
        final Locker lock = new Locker();
        
        rp.post (new Runnable() {
            public void run() {
                lock.waitOn();
                
                if (error) {
                    throw new AssertionError();
                } else {
                    throw new NullPointerException();
                }
            }
        });
        
        rp.post(x);
        lock.notifyOn();
        
        x.assertCntWaiting("Second task not performed after " +
                     (error ? "error" : "exception"), 1);
    }
    
    private static void doGc (int count, Reference toClear) {
        java.util.ArrayList l = new java.util.ArrayList (count);
        while (count-- > 0) {
            if (toClear != null && toClear.get () == null) break;
            
            l.add (new byte[1000]);
            System.gc ();
            System.runFinalization();
	    try {
	        Thread.sleep(10);
	    } catch (InterruptedException e) {}
        }
    }
    
    private static class Counter extends Object implements Runnable {
        private int count = 0;

        public synchronized void run () {
            count++;
        }
        
        public synchronized void assertCnt (String msg, int cnt) {
            assertEquals (msg, cnt, this.count);
            this.count = 0;
        }

        public synchronized void assertCntWaiting(String msg, int cnt) {
            // have to wait actively to recognize starvation :-(
            for (int i=1; i<10; i++) {
                try { wait(20*i*i); } catch (InterruptedException e) {}
                if (count == cnt) { // passed
                    count = 0;
                    return;
                }
            }
            assertEquals (msg, cnt, count); // let it fail
        }
    }

    private static class Locker {
        boolean ready = false;
        
        public synchronized void waitOn() {
            while (ready == false) {
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
        }
        
        public synchronized void notifyOn() {
            ready = true;
            notifyAll();
        }
    }
}
