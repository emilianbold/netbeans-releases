
package org.netbeans.modules.junit;

import org.openide.TopManager;
import junit.framework.*;
import junit.runner.*;
import java.util.*;
import java.io.*;
import junit.framework.*;

public class TestRunnerTest extends TestCase {
    
    public TestRunnerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestRunnerTest.class);
        
        return suite;
    }
    
    /** Test of getLoader method, of class org.netbeans.modules.junit.TestRunner. */
    public void testGetLoader() {
        System.out.println("testGetLoader");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of addError method, of class org.netbeans.modules.junit.TestRunner. */
    public void testAddError() {
        System.out.println("testAddError");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of addFailure method, of class org.netbeans.modules.junit.TestRunner. */
    public void testAddFailure() {
        System.out.println("testAddFailure");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of endTest method, of class org.netbeans.modules.junit.TestRunner. */
    public void testEndTest() {
        System.out.println("testEndTest");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of startTest method, of class org.netbeans.modules.junit.TestRunner. */
    public void testStartTest() {
        System.out.println("testStartTest");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of doRun method, of class org.netbeans.modules.junit.TestRunner. */
    public void testDoRun() {
        System.out.println("testDoRun");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of runFailed method, of class org.netbeans.modules.junit.TestRunner. */
    public void testRunFailed() {
        System.out.println("testRunFailed");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of start method, of class org.netbeans.modules.junit.TestRunner. */
    public void testStart() {
        System.out.println("testStart");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
}
