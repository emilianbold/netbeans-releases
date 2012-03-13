/*
 * SimpleTest.java
 * JUnit based test
 *
 * Created on August 13, 2004, 1:07 PM
 */

package abc.xyz;

import junit.framework.*;

/**
 *
 * @author mp
 */
public class SimpleTest extends TestCase {
    
    public SimpleTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SimpleTest.class);
        return suite;
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
