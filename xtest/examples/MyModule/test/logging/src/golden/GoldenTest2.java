package golden;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbTestCase;


/** Example of golden file approach.
 * Next to this GoldenTest1.java file there should exist GoldenTest1.pass file
 * containing expected output (AKA golden file)
 *
 */
public class GoldenTest2 extends NbTestCase {
    
    
    public GoldenTest2(String testName) {
        super(testName);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(GoldenTest2.class));
    }
    
    /** This method use golden files approach and you need to use compareReferenceFiles()
     * method explicitly to compare generated reference file against golden file.
     */
    public void testPart1() {
        log("message to log file testPart1.log");
        ref("Output to ref file called testPart1.ref");
        // here write body of the test
        boolean somethingWrong = false;
        // You can also use assertTrue() or fail() methods in the body of the test 
        // to indicate a failure
        if(somethingWrong) fail();
        assertTrue(!somethingWrong);
        log("mySpecialLog.log", "message to log file mySpecialLog.log");
        compareReferenceFiles();
    }
    
    
    /** This method doesn't use golden file approach */
    public void testPart2() throws Exception {
        log("message to log file testPart2.log");
        // here write body of the test
    }
    
}
