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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.MaximizeWindowAction and 
 * org.netbeans.jellytools.actions.RestoreWindowAction.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class MaximizeWindowActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name
     */
    public MaximizeWindowActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new MaximizeWindowActionTest("testPerformPopup"));
        suite.addTest(new MaximizeWindowActionTest("testPerformAPI"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Test performPopup */
    public void testPerformPopup() {
        // test editor TopComponent
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        new OpenAction().performAPI(node);
        EditorOperator eo = new EditorOperator("SampleClass1.java"); // NOI18N
        new MaximizeWindowAction().performPopup(eo);
        new RestoreWindowAction().performPopup(eo);
        eo.closeDiscard();
        // test editor TopComponent
        ProjectsTabOperator pto = new ProjectsTabOperator();
        new MaximizeWindowAction().performPopup(pto);
        new RestoreWindowAction().performPopup(pto);
    }
    
    /** Test performAPI  */
    public void testPerformAPI() {
        // test editor TopComponent
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        new OpenAction().performAPI(node);
        EditorOperator eo = new EditorOperator("SampleClass1.java"); // NOI18N
        new MaximizeWindowAction().performAPI(eo);
        new RestoreWindowAction().performAPI(eo);
        eo.closeDiscard();
        // test non editor TopComponent
        ProjectsTabOperator pto = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(pto);
        new RestoreWindowAction().performAPI();
    }
    
}
