/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version 1.0
 * (the "License"). You may not use this file except in compliance with the
 * License. A copy of the License is available at http://www.sun.com/.
 *
 * The Original Code is the Jemmy library. The Initial Developer of the
 * Original Code is Alexandre Iline. All Rights Reserved.
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
     * Test setTimeouts method.
     */
    public void testSetTimeouts() {
    }
    
    /**
     * Test getTimeouts method.
     */
    public void testGetTimeouts() {
    }
    
    /**
     * Test setOutput method.
     */
    public void testSetOutput() {
    }
    
    /**
     * Test getOutput method.
     */
    public void testGetOutput() {
    }
    
    /**
     * Test waitAction method.
     */
    public void testWaitAction() throws Exception {
    }
    
    /**
     * Test actionProduced method.
     */
    public void testActionProduced() {
    }
    
    /**
     * Test getDescription method.
     */
    public void testGetDescription() {
    }
    
    /**
     * Test getWaitingStartedMessage method.
     */
    public void testGetWaitingStartedMessage() {
    }
    
    /**
     * Test getTimeoutExpiredMessage method.
     */
    public void testGetTimeoutExpiredMessage() {
    }
    
    /**
     * Test getActionProducedMessage method.
     */
    public void testGetActionProducedMessage() {
    }
    
    /**
     * Test getGoldenWaitingStartedMessage method.
     */
    public void testGetGoldenWaitingStartedMessage() {
    }
    
    /**
     * Test getGoldenTimeoutExpiredMessage method.
     */
    public void testGetGoldenTimeoutExpiredMessage() {
    }
    
    /**
     * Test getGoldenActionProducedMessage method.
     */
    public void testGetGoldenActionProducedMessage() {
    }
    
    /**
     * Test timeFromStart method.
     */
    public void testTimeFromStart() {
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
