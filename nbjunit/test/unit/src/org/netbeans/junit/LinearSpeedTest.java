/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;

/** Checks behaviour of linear speed suite in case the
 * average time per one unit it lower than 1ms.
 *
 * @author Jaroslav Tulach
 */
public class LinearSpeedTest extends NbTestCase {
    public LinearSpeedTest(String s) {
        super(s);
    }
    
    public static Test suite() {
        final Test t = NbTestSuite.linearSpeedSuite(LinearSpeedTest.class, 2,2);
        
        class ThisHasToFail implements Test {
            public int countTestCases() {
                return 1;
            }

            public void run(TestResult testResult) {
                TestResult r = new TestResult();
                t.run(r);
                
                int count = r.errorCount() + r.failureCount();
                if (count == 0) {
                    testResult.startTest(this);
                    testResult.addFailure(this, new AssertionFailedError("LinearSpeedTest must fail: " + count));
                    testResult.endTest(this);
                }
            }
        }
        return new ThisHasToFail();
    }
    
    public void testBasicSizeOf1000() throws Exception {
        Thread.sleep(100);
    }
    public void testShouldBeTenTimesSloweverButIsJustTwice10000() throws Exception {
        Thread.sleep(200);
    }
}
