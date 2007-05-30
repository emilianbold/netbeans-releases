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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;

/** Test of FindInFilesOperator.
 * @author  ai97726
 */
public class FindInFilesOperatorTest extends JellyTestCase {

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
        TestSuite suite = new NbTestSuite();
        suite.addTest(new FindInFilesOperatorTest("testOverall"));
        return suite;
    }
    
    /** Creates a new instance of SearchFilesystemOperatorTest */
    public FindInFilesOperatorTest(String testName) {
        super(testName);
    }
    
    /** Setup before every test case. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Clean up after each test case. */
    protected void tearDown() {
    }
    
    public void testOverall() {
        FindInFilesOperator fifo = FindInFilesOperator.invoke(new ProjectsTabOperator().getProjectRootNode("SampleProject"));
        fifo.txtText().setText("SampleClass1");
        fifo.txtPatterns().setText("*.java");
        fifo.cbWholeWords().changeSelection(true);
        fifo.cbCase().changeSelection(true);
        SearchResultsOperator sro = fifo.find();
        sro.close();
    }
}
