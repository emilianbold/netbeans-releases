/*
 * FreeFormMainTest.java
 * JUnit based test
 *
 * Created on June 14, 2004, 3:58 PM
 */

package org.netbeans.test;
import junit.framework.*;


import junit.framework.*;

/**
 *
 * @author mkubec
 */
public class FreeFormMainTest extends TestCase {
    
    public FreeFormMainTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FreeFormMainTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    /**
     * Test of getString method, of class org.netbeans.test.FreeFormMain.
     */
    public void testGetString() {
        System.out.println("testGetString");
        assertEquals("Ahoj", FreeFormMain.getString());
    }

}
