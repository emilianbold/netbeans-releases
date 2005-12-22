/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit;

import java.util.Enumeration;
import javax.swing.SwingUtilities;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/** Check that the test can timeout.
 *
 * @author Jaroslav Tulach
 */
public class TimeOutTest extends NbTestCase {
    private Thread main;
    private boolean expectedResult;
    private boolean ok;
    
    public TimeOutTest (String testName) {
        super (testName);
    }
    
    protected void setUp () throws Exception {
    }

    protected void tearDown () throws Exception {
    }
    
    protected boolean runInEQ () {
        return getName().indexOf("AWT") >= 0;
    }
    
    protected int timeOut() {
        return 2500;
    }

    public void run(TestResult result) {
        this.main = Thread.currentThread();
        
        TestResult mine = new TestResult();
        result.startTest(this);
        super.run(mine);
        
        if (mine.errorCount() != 0) {
            Enumeration en = mine.errors();
            while(en.hasMoreElements()) {
                TestFailure f = (TestFailure)en.nextElement();
                result.addError(this, f.thrownException());
            }
            return;
        }
        
        if (ok != (mine.failureCount() == 0)) {
            result.addFailure(this, 
                new AssertionFailedError(
                    "ok: " + ok + " count: " + mine.failureCount() + " for " + getName()
                )
            );
            return;
        }
        
        result.endTest(this);
    }
    
    public void testRunsInAWTThreadAndShallSucceed () {
        assertTrue(SwingUtilities.isEventDispatchThread());
        expectedResult = true;
        ok = SwingUtilities.isEventDispatchThread();
    }
    
    public void testRunsInAWTThreadAndShallSucceedWith1sDelay () throws Exception {
        assertTrue(SwingUtilities.isEventDispatchThread());
        expectedResult = true;
        Thread.sleep(1000);
        ok = SwingUtilities.isEventDispatchThread();
    }

    public void testRunsInAWTThreadAndShallFailWith5sDelay () throws Exception {
        assertTrue(SwingUtilities.isEventDispatchThread());
        expectedResult = false;
        Thread.sleep(5000);
        ok = true;
    }

    public void testRunsShallSucceedWithNoDelay () {
        assertFalse(SwingUtilities.isEventDispatchThread());
        if (Thread.currentThread() == main) {
            fail("We should run in dedicated thread");
        }
        expectedResult = true;
        ok = true;
    }
    public void testRunsShallSucceedWith1sDelay () throws InterruptedException {
        assertFalse(SwingUtilities.isEventDispatchThread());
        if (Thread.currentThread() == main) {
            fail("We should run in dedicated thread");
        }
        expectedResult = true;
        Thread.sleep(1000);
        ok = true;
    }
    public void testRunsShallFailWith5sDelay () throws InterruptedException {
        assertFalse(SwingUtilities.isEventDispatchThread());
        if (Thread.currentThread() == main) {
            fail("We should run in dedicated thread");
        }
        expectedResult = false;
        Thread.sleep(5000);
        ok = true;
    }
    
}
