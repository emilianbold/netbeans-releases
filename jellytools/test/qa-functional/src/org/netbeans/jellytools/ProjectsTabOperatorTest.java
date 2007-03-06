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

import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

/** Test ProjectsTabOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ProjectsTabOperatorTest extends JellyTestCase {

    public ProjectsTabOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite();
        // suites have to be in particular order
        suite.addTest(new ProjectsTabOperatorTest("testInvoke"));
        suite.addTest(new ProjectsTabOperatorTest("testTree"));
        suite.addTest(new ProjectsTabOperatorTest("testGetProjectRootNode"));
        suite.addTest(new ProjectsTabOperatorTest("testVerify"));
        return suite;
    }
    
    /** Print out test name. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    private static ProjectsTabOperator projectsOper;
    
    /**
     * Test of invoke method.
     */
    public void testInvoke() {
        ProjectsTabOperator.invoke().close();
        projectsOper = ProjectsTabOperator.invoke();
    }
    
    /**
     * Test of tree method.
     */
    public void testTree() {
        RuntimeTabOperator.invoke();
        // has to make tab visible
        projectsOper.tree();
    }
    
    /**
     * Test of getRootNode method.
     */
    public void testGetProjectRootNode() {
        projectsOper.getProjectRootNode("SampleProject");   // NOI18N
    }
    
    /**
     * Test of verify method.
     */
    public void testVerify() {
        projectsOper.verify();
    }
}
