package golden;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbTestCase;

import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.io.FileOutputStream;

/** Example of golden file approach.
 * Next to this GoldenTest1.java file there should exist GoldenTest1.pass file
 * containing expected output (AKA golden file)
 *
 */
public class GoldenTest1 extends NbTestCase {
    
    
    public GoldenTest1(String testName) {
        super(testName);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(GoldenTest1.class));
    }
    
    /** Uses golden files approach. Messages are written to the reference file
     * by the ref() method. At the end tearDown() method satisfy comparing with
     * golden file.<br>
     * Of course, you can also use fail() and assert() methods within the test to indicate
     * a test failure.
     */
    public void testPart1() throws Exception {
        // print path where log and ref files will be stored
        System.out.println("WORKDIR="+getWorkDirPath());
        ref("Output to ref file called testPart1.ref");
        log("message to log file testPart1.log");
        log("mySpecialLog.log", "message to log file mySpecialLog.log");
        // here write body of the test
    }
    
    
    /** This method should fail because contents of reference file and
     * golden file will differ.
     */
    public void testPart2() throws Exception {
        ref("Output to ref file called testPart12.ref");
        log("message to log file testPart2.log");
        // here write body of the test
        log("Finished.");
    }
    
    /* Use tearDown to compare reference files after each test method is finished.
     *  assertFile() method compares <testname>.ref file againts <testname>.pass
     */
    protected void tearDown() {
        compareReferenceFiles();
    }
    
}
