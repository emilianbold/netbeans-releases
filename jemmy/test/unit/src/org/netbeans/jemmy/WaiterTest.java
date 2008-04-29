/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
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
 * "Portions Copyrighted [year] [name of copyright owner]".
 *
 * The Original Software is the Jemmy library. The Initial Developer of the
 * Original Software is Alexandre Iline. All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A JUnit test for Waiter.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class WaiterTest extends TestCase {
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public WaiterTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup before testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void setUp() throws Exception {
    }
    
    /**
     * Cleanup after testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void tearDown() throws Exception {
    }
    
    /**
     * Suite method.
     *
     * @return a test suite.
     */
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(WaiterTest.class);
        return suite;
    }
    
    /**
     * Test issue #30537.
     */
    public void testIssue30537() {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object object) {
                    return object;
                }
                public String getDescription() {
                    return "Description";
                }
            });
            
            waiter.waitAction(new Object());
        }
        catch(InterruptedException exception) {
            fail();
        }
    }
}
