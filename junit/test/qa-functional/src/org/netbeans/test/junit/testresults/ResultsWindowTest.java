/*
 * ResultsWindowTest.java
 *
 * Created on September 12, 2006, 11:51 AM
 *
 */

package org.netbeans.test.junit.testresults;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.junit.utils.ResultWindowOperator;
import org.netbeans.test.junit.utils.Utilities;

/**
 *
 * @author max.sauer@sun.com
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
    
    /** Tests visiblility of results window */
    public void testResultWindowOpened() {
        //open Test package
        Node n = Utilities.openFile(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
        
        JPopupMenuOperator jpmo = n.callPopup();
        jpmo.pushMenu(Utilities.RUN_FILE);
        Utilities.takeANap(7000);
        ResultWindowOperator rwo = new ResultWindowOperator();
        assertTrue("Junit Output window should be visible", rwo.isVisible());
        rwo.close(); //close it
        assertFalse("Junit Output window is visible," +
                "should be closed", rwo.isShowing());
    }
    
    /**
     * Test whether filter button inside results window is enabled
     */
    public void testFilterButtonEnabled() {
        Node n = Utilities.openFile(Utilities.TEST_PACKAGES_PATH + "|"
                + TEST_PACKAGE_NAME + "|EmptyJUnitTest");
        JPopupMenuOperator jpmo = n.callPopup();
        jpmo.pushMenu(Utilities.RUN_FILE);
        Utilities.takeANap(4000);
        ResultWindowOperator rwo = new ResultWindowOperator();
        assertTrue("Filter button should eb enabled",
                rwo.isFilterButtonEnabled());
        
    }
    
    
    /**
     * Test functionality of filter button
     * Runs suite with three tests:
     * one with both failing and succeeding tests
     * one with only failing
     * one with only succeeding
     * (testresults.test.TestResultsSuite from JunitTestProject)
     */
    public void testPressFilterButton() {
        Utilities.testWholeProject();
        Utilities.takeANap(4000);
        ResultWindowOperator rwo = new ResultWindowOperator();
        rwo.pushFilterButton();

        //TODO: Finish this test        
    }
    
    
}
