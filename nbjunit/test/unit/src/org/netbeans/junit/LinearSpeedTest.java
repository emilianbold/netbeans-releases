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
