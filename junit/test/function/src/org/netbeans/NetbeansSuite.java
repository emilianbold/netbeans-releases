
package org.netbeans;
import junit.framework.*;

public class NetbeansSuite extends TestCase {

    public NetbeansSuite(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        //--JUNIT:
        //This block was automatically generated and can be regenerated again.
        //Do NOT change lines enclosed by the --JUNIT: and :JUNIT-- tags.
        TestSuite suite = new TestSuite("NetbeansSuite");
        suite.addTest(org.netbeans.modules.ModulesSuite.suite());
        //:JUNIT--
        //This value MUST ALWAYS be returned from this function.
        return suite;
        
}
    
}
