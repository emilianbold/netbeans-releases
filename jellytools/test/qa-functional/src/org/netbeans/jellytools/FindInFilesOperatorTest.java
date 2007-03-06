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
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbTestSuite;

/**
 *
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
        suite.addTest(new FindInFilesOperatorTest("testFullText"));
        suite.addTest(new FindInFilesOperatorTest("testObjectName"));
        suite.addTest(new FindInFilesOperatorTest("testType"));
        suite.addTest(new FindInFilesOperatorTest("testDate"));
        suite.addTest(new FindInFilesOperatorTest("testUseAndClear"));
        return suite;
    }
    
    /** Creates a new instance of SearchFilesystemOperatorTest */
    public FindInFilesOperatorTest(String testName) {
        super(testName);
    }
    
    public void testFullText() {
        FindInFilesOperator sw = new FindInFilesOperator();
        sw.setFullTextSubstring("substring", true, true);
        sw.saveSetting("substring");
        sw.setFullTextRegExpr("regexpr");
        sw.restoreSaved("substring");
    }

    public void testObjectName() {
        FindInFilesOperator sw = new FindInFilesOperator();
        sw.setObjectNameSubstring("substring", true, true);
        sw.saveSetting("substring");
        sw.setObjectNameRegExpr("regexpr");
        sw.restoreSaved("substring");
    }

    public void testType() {
        FindInFilesOperator sw = new FindInFilesOperator();
        sw.setType("Class Objects");
        sw.saveSetting("Class Objects");
        sw.setType("Java Source Objects");
        sw.restoreSaved("Class Objects");
    }    

    public void testDate() {
        FindInFilesOperator sw = new FindInFilesOperator();
        sw.setDateBetween("Jun 1, 2001", "Jun 1, 2002");
        sw.saveSetting("Jun");
        sw.setDatePastDays(10);
        sw.restoreSaved("Jun");
    }    

    public void testUseAndClear() {
        FindInFilesOperator sw = new FindInFilesOperator();
        // Sometimes it failes because "Use This Criterion for Search" check box
        // is restored by IDE against jemmy setting
        sw.setVerification(false);
        sw.restoreFullText("substring");
        sw.restoreObjectName("substring");
        sw.restoreType("Class Objects");
        sw.restoreDate("Jun");
        sw.clearCriteria();
    }
    
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        // increase timeout for waiting until combo popup is opened
        JemmyProperties.setCurrentTimeout("JComboBoxOperator.WaitListTimeout", 30000);
        FindInFilesOperator.invoke(new ProjectsTabOperator().getProjectRootNode("SampleProject"));
    }
    
    /** Clean up after each test case. */
    protected void tearDown() {
        new FindInFilesOperator().close();
    }
}
