/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.util;

import junit.textui.TestRunner;


import org.netbeans.junit.*;

/**
 *
 * @author Jaroslav Tulach
 */
public class TaskTest extends NbTestCase {
    /** Creates a new instance of UtilProgressCursorTest */
    public TaskTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(new NbTestSuite(TaskTest.class));
    }
    
    
    //
    // tests
    //
    public void testPlainTaskWaitsForBeingExecuted () throws Exception {
        R run = new R ();
        Task t = new Task (run);
        
        Thread thread = new Thread (t);
        synchronized (run) {
            thread.start ();
            run.wait ();
        }
        
        assertFalse ("Not finished", t.isFinished ());
        synchronized (run) {
            run.notify ();
        }
        
        t.waitFinished ();
        assertTrue ("Finished", t.isFinished ());
    }
    
    public void testTaskEMPTYIsFinished () throws Exception {
        assertTrue (Task.EMPTY.isFinished ());
    }
    
    public void testWaitFinishedOnEMPTYTaskReturnsImmediatelly () throws Exception {
        Task.EMPTY.waitFinished ();
    }

    public void testWaitWithTimeOutReturnsImmediatellyOnFinishedTasks () throws Exception {
        assertTrue ("Was successfully finished", Task.EMPTY.waitFinished (0));
    }
    
    public void testWaitWithTimeOutReturnsAfterTimeOutWhenTheTaskIsNotComputedAtAll () throws Exception {
        long time = System.currentTimeMillis ();
        Task t = new Task (new R ());
        t.waitFinished (1000);
        time = System.currentTimeMillis () - time;
        
        assertFalse ("Still not finished", t.isFinished ());
        
        if (time < 900 || time > 1100) {
            fail ("Something wrong happened the task should wait for 1000ms but it took: " + time);
        }
    }
    
    public void testWaitOnStrangeTaskThatStartsItsExecutionInOverridenWaitFinishedMethodLikeFolderInstancesDo () throws Exception {
        class MyTask extends Task {
            private int values;
            
            public MyTask () {
                notifyFinished ();
            }
            
            public void waitFinished () {
                notifyRunning ();
                values++;
                notifyFinished ();
            }
        }
        
        MyTask my = new MyTask ();
        assertTrue ("The task thinks that he is finished", my.isFinished ());
        assertTrue ("Ok, even with timeout we got the result", my.waitFinished (1000));
        assertEquals ("But the old waitFinished is called", 1, my.values);
    }
    
    public void testWaitOnStrangeTaskThatTakesReallyLongTime () throws Exception {
        class MyTask extends Task {
            public MyTask () {
                notifyFinished ();
            }
            
            public void waitFinished () {
                try {
                    Thread.sleep (5000);
                } catch (InterruptedException ex) {
                    fail ("Should not happen");
                }
            }
        }
        
        MyTask my = new MyTask ();
        assertTrue ("The task thinks that he is finished", my.isFinished ());
        assertFalse ("but still it get's called, but timeouts", my.waitFinished (1000));
    }
    
    final class R implements Runnable {
        public synchronized void run () {
            notify ();
            try {
                wait ();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

}
