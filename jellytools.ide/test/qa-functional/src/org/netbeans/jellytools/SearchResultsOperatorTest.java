/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of SearchResultsOperator.
 * @author Jiri.Skrivanek@sun.com
 */
public class SearchResultsOperatorTest extends JellyTestCase {

    public static final String[] tests = new String[] {
        "testBtStopSearch",
        "testBtShowDetails",
        "testBtModifySearch",
        "testTreeResult",
        "testSelectResult",
        "testOpenResult",
        "testModifySearch",
        "testShowDetails",
        "testWaitEndOfSearch",
        "testVerify"
    };
    
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
        /*
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
         */
        return createModuleTest(SearchResultsOperatorTest.class, 
        tests);
    }
    
    /** Creates new SearchResultsOperatorTest */
    public SearchResultsOperatorTest(String testName) {
        super(testName);
    }

    private static SearchResultsOperator searchResultsOper = null;
    
    /** Open find dialog on sample project and find sample substring. */
    public void setUp() throws IOException {
        System.out.println("### "+getName()+" ###");
        openDataProjects("SampleProject");
        if(searchResultsOper == null) {
            FindInFilesOperator fifo = FindInFilesOperator.invoke(new ProjectsTabOperator().getProjectRootNode("SampleProject"));
            fifo.txtText().setText("sample");
            searchResultsOper = fifo.find();
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
        searchResultsOper.makeComponentVisible();
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
