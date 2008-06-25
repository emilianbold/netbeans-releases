/*
 * EmptyJUnitTest.java
 * JUnit based test
 *
 * Created on September 20, 2006, 3:57 PM
 */

package org.netbeans.test.junit.testresults.test;

import junit.framework.TestCase;
import junit.framework.*;

/**
 *
 * @author ms159439
 */
public class EmptyJUnitTest extends TestCase {
    
    public EmptyJUnitTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        junit.framework.TestSuite suite =
                new junit.framework.TestSuite(EmptyJUnitTest.class);
        return suite;
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

}
