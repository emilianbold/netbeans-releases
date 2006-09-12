/*
 * ResultsWindowTest.java
 *
 * Created on September 12, 2006, 11:51 AM
 *
 */

package org.netbeans.test.junit.testresults;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.junit.testcase.JunitTestCase;
import org.netbeans.test.junit.utils.ResultWindowOperator;
import org.netbeans.test.junit.utils.Utilities;

/**
 *
 * @author ms159439
 */
public class ResultsWindowTest extends NbTestCase {
    /** path to sample files */
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.junit.testresults";
    
    /** name of sample package */
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH+".test";
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ResultsWindowTest.class);
        return suite;
    }
    
    /** Creates a new instance of ResultsWindowTest */
    public ResultsWindowTest(String testName) {
        super(testName);
    }
    
    /** Test if JUnit test result window switch buttonis enabled */
    public void testButtonEnabled() {
        //open Test package
        Node n = Utilities.openFile(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
        
        JPopupMenuOperator jpmo = n.callPopup();
        jpmo.pushMenu(Utilities.RUN_FILE);
        Utilities.takeANap(4000);
        //ResultWindowOperator rwo = new ResultWindowOperator();
        //assertTrue(rwo.isVisible());
        //TODO -- finish this test
        // result window opeerator cannot be constructed
    }
    
}
