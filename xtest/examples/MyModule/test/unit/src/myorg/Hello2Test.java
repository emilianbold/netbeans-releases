
package myorg;

import junit.framework.*;

public class Hello2Test extends TestCase {
    
    public Hello2Test(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(Hello2Test.class);
        
        return suite;
    }
    
    /** Test of hello method, of class myorg.Hello2. */
    public void testHello() {
        System.out.println("testHello");
        
        String greeting = testObject.hello("Joe");
        assertEquals(greeting, "Hello Joe!");
    }
    
    protected Hello2 testObject;
    
    protected void setUp() {
        testObject = new Hello2();
    }
}
