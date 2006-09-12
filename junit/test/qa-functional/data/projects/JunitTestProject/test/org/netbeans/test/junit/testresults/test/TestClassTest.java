/*
 * TestClassTest.java
 * JUnit based test
 *
 * Created on September 12, 2006, 12:05 PM
 */

package org.netbeans.test.junit.testresults.test;

import junit.framework.TestCase;
import junit.framework.*;

/**
 *
 * @author ms159439
 */
public class TestClassTest extends TestCase {
    
    public TestClassTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of add0 method, of class org.netbeans.test.junit.testresults.test.TestClass.
     */
    public void testAdd0() {
        System.out.println("add0");
        
        int a = 1;
        int b = 1;
        TestClass instance = new TestClass();
        
        int expResult = 2;
        int result = instance.add0(a, b);
        assertEquals(expResult, result);
    }

    /**
     * Test of add1 method, of class org.netbeans.test.junit.testresults.test.TestClass.
     */
    public void testAdd1() {
        System.out.println("add1");
        
        int a = 1;
        int b = 1;
        TestClass instance = new TestClass();
        
        int expResult = 0; //wrong -- should fail
        int result = instance.add1(a, b);
        assertEquals(expResult, result);
    }
    
}
