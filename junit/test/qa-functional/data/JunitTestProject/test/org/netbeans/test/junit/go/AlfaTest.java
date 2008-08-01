/*
 * AlfaTest.java
 * JUnit based test
 *
 * Created on February 7, 2007, 11:26 AM
 */

package org.netbeans.test.junit.go;

import org.netbeans.test.junit.go.Alfa;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author ms159439
 */
public class AlfaTest extends TestCase {
    
    public AlfaTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AlfaTest.class);

        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testHello() {
        System.out.println("hello");
        String name = "";
        Alfa instance = new Alfa();
        String expResult = "";
        String result = instance.hello(name);

        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

}
