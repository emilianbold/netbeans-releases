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
        if (expectedResult != (mine.failureCount() == 0)) {
            result.addFailure(this, 
                new AssertionFailedError(
                    "expectedResult: " + expectedResult + "failureCount: " + mine.failureCount() + " for " + getName()
                )
            );
            return;
        }
        
        result.endTest(this);
    }
    
    public void testRunsInAWTThreadAndShallSucceed () {
        assertTrue(SwingUtilities.isEventDispatchThread());
        expectedResult = true;
    }
    
    public void testRunsInAWTThreadAndShallSucceedWith1sDelay () throws Exception {
        assertTrue(SwingUtilities.isEventDispatchThread());
        expectedResult = true;
        Thread.sleep(1000);
    }

    public void testRunsInAWTThreadAndShallFailWith5sDelay () throws Exception {
        assertTrue(SwingUtilities.isEventDispatchThread());
        expectedResult = false;
        Thread.sleep(5000);
    }

    public void testRunsShallSucceedWithNoDelay () {
        assertFalse(SwingUtilities.isEventDispatchThread());
        if (Thread.currentThread() == main) {
            fail("We should run in dedicated thread");
        }
        expectedResult = true;
    }
    public void testRunsShallSucceedWith1sDelay () throws InterruptedException {
        assertFalse(SwingUtilities.isEventDispatchThread());
        if (Thread.currentThread() == main) {
            fail("We should run in dedicated thread");
        }
        expectedResult = true;
        Thread.sleep(1000);
    }
    
    public void testRunsShallFailWith5sDelay () throws InterruptedException {
        assertFalse(SwingUtilities.isEventDispatchThread());
        if (Thread.currentThread() == main) {
            fail("We should run in dedicated thread");
        }
        expectedResult = false;
        Thread.sleep(5000);
    }
}
