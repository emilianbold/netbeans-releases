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

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINER);
        Logger logger = Logger.getLogger(Waiter.class.getName());
        logger.addHandler(handler);
        logger.setLevel(Level.FINER);
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
            
            waiter.waitAction("testIssue30537");
        }
        catch(InterruptedException exception) {
            fail();
        }
    }
}
