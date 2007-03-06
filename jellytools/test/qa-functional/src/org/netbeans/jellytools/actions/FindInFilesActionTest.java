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
package org.netbeans.jellytools.actions;

import junit.textui.TestRunner;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.FindInFilesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.actions.FindInFilesAction.
 * @author Jiri.Skrivanek@sun.com
 */
public class FindInFilesActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FindInFilesActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new FindInFilesActionTest("testPerformPopup"));
        suite.addTest(new FindInFilesActionTest("testPerformMenu"));
        suite.addTest(new FindInFilesActionTest("testPerformAPI"));
        suite.addTest(new FindInFilesActionTest("testPerformShortcut"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Redirect output to log files, wait before each test case. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Clean up after each test case. */
    protected void tearDown() {
    }
    
    /** Test of performPopup method. */
    public void testPerformPopup() {
        Node node = new ProjectsTabOperator().getProjectRootNode("SampleProject");  // NOI18N
        new FindInFilesAction().performPopup(node);
        new FindInFilesOperator().close();
    }
    
    /** Test of performMenu method. */
    public void testPerformMenu() {
        Node node = new ProjectsTabOperator().getProjectRootNode("SampleProject");  // NOI18N
        new FindInFilesAction().performMenu(node);
        new FindInFilesOperator().close();
        // need to wait here because next menu can disappear
        new EventTool().waitNoEvent(500);
        new FindInFilesAction().performMenu();
        new FindInFilesOperator().close();
    }
    
    /** Test of performAPI method. */
    public void testPerformAPI() {
        Node node = new ProjectsTabOperator().getProjectRootNode("SampleProject");  // NOI18N
        new FindInFilesAction().performAPI(node);
        new FindInFilesOperator().close();
        new FindInFilesAction().performAPI(new ProjectsTabOperator());
        new FindInFilesOperator().close();
        new FindInFilesAction().performAPI();
        new FindInFilesOperator().close();
    }
    
    /** Test of performShortcut method. */
    public void testPerformShortcut() {
        new FindInFilesAction().performShortcut();
        new FindInFilesOperator().close();
        Node node = new ProjectsTabOperator().getProjectRootNode("SampleProject");  // NOI18N
        new FindInFilesAction().performShortcut(node);
        new FindInFilesOperator().close();
        // On some linux it may happen autorepeat is activated and it 
        // opens dialog multiple times. So, we need to close all modal dialogs.
        // See issue http://www.netbeans.org/issues/show_bug.cgi?id=56672.
        closeAllModal();
        try {
            new FindInFilesAction().performShortcut(new ProjectsTabOperator());
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {}
    }
    
}
