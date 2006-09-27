/*
 * TestSuccessTest.java
 * JUnit based test
 *
 * Created on September 27, 2006, 2:41 PM
 */

package org.netbeans.test.junit.testresults.test;

import junit.framework.TestCase;
import junit.framework.*;

/**
 *
 * @author ms159439
 */
public class TestSuccessTest extends TestCase {
    
    public TestSuccessTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        junit.framework.TestSuite suite = 
                new junit.framework.TestSuite(TestSuccessTest.class);
        return suite;
    }
    /**
     * Test of True method, of class org.netbeans.test.junit.testresults.test.TestSuccess.
     */
    public void testTrue() {
        System.out.println("True");
        
        TestSuccess instance = new TestSuccess();
        
        boolean expResult = true;
        boolean result = instance.True();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of False method, of class org.netbeans.test.junit.testresults.test.TestSuccess.
     */
    public void testFalse() {
        System.out.println("False");
        
        TestSuccess instance = new TestSuccess();
        
        boolean expResult = false;
        boolean result = instance.False();
        assertEquals(expResult, result);
    }
    
}
