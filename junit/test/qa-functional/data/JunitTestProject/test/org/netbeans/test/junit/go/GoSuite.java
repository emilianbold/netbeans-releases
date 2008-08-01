/*
 * TestSuite.java
 * JUnit based test
 *
 * Created on February 7, 2007, 11:26 AM
 */

package org.netbeans.test.junit.go;

import org.netbeans.test.junit.go.AlfaTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author ms159439
 */
public class GoSuite extends TestCase {
    
    public GoSuite(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("GoSuite");

        suite.addTest(AlfaTest.suite());
        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
