/*
 * JUnitTest.java
 *
 * Created on February 3, 2005, 2:37 PM
 */

package org.netbeans.jemmy.testing.junit;

import org.netbeans.jemmy.Test;

/**
 *
 * @author shura
 */
public class JUnitTest extends junit.framework.TestCase {
    private static final String[] ALL_TESTS = {"001", "002", "003", "004", "005", "006", "007", "008", "009", "010", "011", "012", "013", "014", "015", "016", "017", "018", "019", "020", "021", "022", "023", "024", "025", "026", "027", "028", "029", "030", "031", "032", "033", "034", "035", "036", "037", "038", "039", "040", "041", "042", "043", "044", "045", "046", "047", "048"};
    private static final String[] MAC_TESTS = {"001", "002", "003", "004", "005", "006", "007",                      "011", "012", "013", "014",        "016", "017", "018", "019", "020", "021", "022", "023", "024", "025",        "027", "028", "029",        "031", "032", "033", "034",               "037", "038", "039", "040", "041", "042", "043", "044", "045", "046", "047", "048"};
    private static final String[] SOL_TESTS = {"001", "002", "003", "004", "005", "006", "007",                      "011", "012", "013", "014",        "016", "017", "018", "019", "020", "021", "022", "023", "024", "025",        "027", "028", "029",        "031", "032", "033", "034",               "037", "038", "039", "040", "041", "042", "043", "044", "045", "046", "047", "048"};
    private static final String[] DBG_TESTS = {"001", "002", "004"};
    
    String jemmyTestIndex;
    /** Creates a new instance of JUnitTest */
    JUnitTest(String jemmyTestIndex) {
        this.jemmyTestIndex = jemmyTestIndex;
    }
    public void run(junit.framework.TestResult result) {
        result.startTest(this);
        String[] args = {"org.netbeans.jemmy.testing.jemmy_" + jemmyTestIndex};
        Test.run(args);
        result.endTest(this);
    }
    public static junit.framework.TestSuite suite(String index) {
        junit.framework.TestSuite suite = new junit.framework.TestSuite();
        suite.addTest(new JUnitTest(index));
        return(suite);
    }
    public static junit.framework.TestSuite suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite();
        String[] tests = DBG_TESTS;
        if(System.getProperty("os.name").startsWith("Mac OS")) {
            tests = MAC_TESTS;
        } else if(System.getProperty("os.name").startsWith("SunOS")) {
            tests = SOL_TESTS;
        }
        for(int i = 0; i < tests.length; i++) {
            suite.addTest(new JUnitTest(tests[i]));
        }
        return(suite);
    }
}
