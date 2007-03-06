/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of SearchResultsOperator.
 * @author Jiri.Skrivanek@sun.com
 */
public class SearchResultsOperatorTest extends JellyTestCase {

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SearchResultsOperatorTest("testBtStopSearch"));
        suite.addTest(new SearchResultsOperatorTest("testBtShowDetails"));
        suite.addTest(new SearchResultsOperatorTest("testBtModifySearch"));
        suite.addTest(new SearchResultsOperatorTest("testTreeResult"));
        suite.addTest(new SearchResultsOperatorTest("testSelectResult"));
        suite.addTest(new SearchResultsOperatorTest("testOpenResult"));
//        suite.addTest(new SearchResultsOperatorTest("testStopSearch"));
        suite.addTest(new SearchResultsOperatorTest("testModifySearch"));
        suite.addTest(new SearchResultsOperatorTest("testShowDetails"));
        suite.addTest(new SearchResultsOperatorTest("testWaitEndOfSearch"));
        suite.addTest(new SearchResultsOperatorTest("testVerify"));
        return suite;
    }
    
    /** Creates new SearchResultsOperatorTest */
    public SearchResultsOperatorTest(String testName) {
        super(testName);
    }

    private static SearchResultsOperator searchResultsOper = null;
    
    /** Open find dialog on sample project and find sample substring. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
        if(searchResultsOper == null) {
            FindInFilesOperator fifo = FindInFilesOperator.invoke(new ProjectsTabOperator().getProjectRootNode("SampleProject"));
            fifo.setFullTextSubstring("sample", false, false);
            searchResultsOper = fifo.search();
        }
    }
    
    /** Test btStopSearch method */
    public void testBtStopSearch() {
        searchResultsOper.btStopSearch();
    }

    /** Test btShowDetails method */
    public void testBtShowDetails() {
        searchResultsOper.btShowDetails();
    }

    /** Test btModifySearch method  */
    public void testBtModifySearch() {
        searchResultsOper.btModifySearch();
    }

    /** Test treeResult method  */
    public void testTreeResult() {
        searchResultsOper.treeResult();
    }

    /** Test selectResult method */
    public void testSelectResult() {
        searchResultsOper.selectResult("SampleClass1.java"); //NOI18N
    }
    
    /** Test openResult method */
    public void testOpenResult() {
        searchResultsOper.openResult("SampleClass1.java|sample");  //NOI18N
        new EditorOperator("SampleClass1").close(); //NOI18N
    }

    /** Test stopSearch method */
    public void testStopSearch() {
        // need to find a test case to test it
    }

    /** Test showDetails method  */
    public void testShowDetails() {
        searchResultsOper.showDetails().close();
    }

    /** Test modifySearch method*/
    public void testModifySearch() {
        searchResultsOper.modifySearch().close();
    }

    /** Test waitEndOfSearch method */
    public void testWaitEndOfSearch() {
        // searching done in setup should be finished
        searchResultsOper.waitEndOfSearch();
    }

    /** Test verify method */
    public void testVerify() {
        searchResultsOper.verify();
        searchResultsOper.close();
    }
}
