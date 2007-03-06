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
import junit.textui.TestRunner;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.actions.SaveAction.
 * @author Jiri.Skrivanek@sun.com
 */
public class SaveActionTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public SaveActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        // at the Save action is not used anywhere in IDE
        // suite.addTest(new SaveActionTest("testPerformPopup"));
        suite.addTest(new SaveActionTest("testPerformMenu"));
        suite.addTest(new SaveActionTest("testPerformAPI"));
        suite.addTest(new SaveActionTest("testPerformShortcut"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }

    private static EditorOperator eo;
    
    /** Open a osurce in editor and modify something. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        new OpenAction().perform(node);
        eo = new EditorOperator("SampleClass1.java");   // NOI18N
        eo.setCaretPosition(0);
        eo.insert(" ");
        eo.setCaretPosition(0);
        eo.delete(1);
    }
    
    /** Clean up after each test case. */
    protected void tearDown() {
        eo.closeDiscard();
    }
    
    /** Test of performPopup method. */
    public void testPerformPopup() {
        new SaveAction().performPopup(eo);
        eo.waitModified(false);
    }
    
    /** Test of performMenu method. */
    public void testPerformMenu() {
        new SaveAction().performMenu();
        eo.waitModified(false);
    }
    
    /** Test of performAPI method. */
    public void testPerformAPI() {
        new SaveAction().performAPI();
        eo.waitModified(false);
    }
    
    /** Test of performShortcut method. */
    public void testPerformShortcut() {
        new SaveAction().performShortcut();
        eo.waitModified(false);
    }
    
}
