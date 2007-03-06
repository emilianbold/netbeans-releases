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
import org.netbeans.jellytools.actions.CloneViewAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.EditorWindowOperator.
 * Order of tests is important.
 * @author Jiri.Skrivanek@sun.com
 */
public class EditorWindowOperatorTest extends JellyTestCase {
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public EditorWindowOperatorTest(java.lang.String testName) {
        super(testName);
    }

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
        // order of tests is important
        suite.addTest(new EditorWindowOperatorTest("testSelectPage"));
        suite.addTest(new EditorWindowOperatorTest("testGetEditor"));
        suite.addTest(new EditorWindowOperatorTest("testSelectDocument"));
        suite.addTest(new EditorWindowOperatorTest("testJumpLeft"));
        suite.addTest(new EditorWindowOperatorTest("testMoveTabsRight"));
        suite.addTest(new EditorWindowOperatorTest("testMoveTabsLeft"));
        suite.addTest(new EditorWindowOperatorTest("testVerify"));
        suite.addTest(new EditorWindowOperatorTest("testCloseDiscard"));
        return suite;
    }
    
    /** Redirect output to log files, wait before each test case. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    private static final String SAMPLE_CLASS_1 = "SampleClass1.java";
    private static final String SAMPLE_CLASS_2 = "SampleClass2.java";
    
    /** Test of selectPage method. */
    public void testSelectPage() {
        // next tests depends on this
        startTest();
        Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
        Node sample2 = new Node(new SourcePackagesNode("SampleProject"), "sample1.sample2");  // NOI18N
        JavaNode sampleClass1 = new JavaNode(sample1, SAMPLE_CLASS_1);
        OpenAction openAction = new OpenAction();
        openAction.performAPI(sampleClass1);
        // close all => it satisfies only sample classes are opened
        new EditorWindowOperator().closeDiscard();
        openAction.performAPI(sampleClass1);
        JavaNode sampleClass2 = new JavaNode(sample2, SAMPLE_CLASS_2);
        openAction.performAPI(sampleClass2);
        EditorWindowOperator ewo = new EditorWindowOperator();
        ewo.selectPage(SAMPLE_CLASS_1);
        ewo.selectPage(SAMPLE_CLASS_2);
        assertTrue("Page "+SAMPLE_CLASS_2+" not selected.", 
                    ewo.getEditor().getName().indexOf(SAMPLE_CLASS_2) != -1);
        ewo.selectPage(SAMPLE_CLASS_1);
        assertTrue("Page "+SAMPLE_CLASS_1+" not selected.", 
                    ewo.getEditor().getName().indexOf(SAMPLE_CLASS_1) != -1);
        // finished succesfully
        endTest();
    }

    /** Test of txtEditorPane method. */
    public void testGetEditor() {
        // next tests depends on this
        startTest();
        EditorWindowOperator ewo = new EditorWindowOperator();
        assertEquals("Wrong editor pane found.", SAMPLE_CLASS_1, ewo.getEditor().getName());
        ewo.selectPage(SAMPLE_CLASS_2);
        assertEquals("Wrong editor pane found.", SAMPLE_CLASS_1, ewo.getEditor(0).getName());
        assertEquals("Wrong editor pane found.", SAMPLE_CLASS_2, ewo.getEditor(SAMPLE_CLASS_2).getName());
        // finished succesfully
        endTest();
    }

    /** Test of selectDocument methods. */
    public void testSelectDocument() {
        // this test depends on previous
        startTest();
        // but not block anything else
        clearTestStatus();
        EditorWindowOperator ewo = new EditorWindowOperator();
        ewo.selectDocument(SAMPLE_CLASS_1);
        assertEquals("Wrong document selected.", SAMPLE_CLASS_1, ewo.getEditor().getName());
        ewo.selectDocument(1);
        assertEquals("Wrong document selected.", SAMPLE_CLASS_2, ewo.getEditor().getName());
    }

    /** Test of jumpLeft method. */
    public void testJumpLeft() {
        // next tests depends on this
        startTest();
        EditorWindowOperator ewo = new EditorWindowOperator();
        // clones selected document several times to test control buttons
        for(int i=0;i<10;i++) {
            new CloneViewAction().performAPI();
        }
        // click on leftmost tab until is not fully visible
        int count = 0;
        while(ewo.jumpLeft() && count++ < 100);
        // if it is still possible to jump left, wait a little and do jumpLeft again
        if(ewo.jumpLeft()) {
            new EventTool().waitNoEvent(3000);
            while(ewo.jumpLeft() && count++ < 100);
        }
        assertFalse("Leftmost tab should not be partially hidden.", ewo.jumpLeft());
        // finished succesfully
        endTest();
    }

    /** Test of moveTabsRight method. */
    public void testMoveTabsRight() {
        // next tests depends on this
        startTest();
        EditorWindowOperator ewo = new EditorWindowOperator();
        ewo.moveTabsRight();
        assertTrue("Tabs were not moved to the right.", ewo.btLeft().isEnabled());
        // finished succesfully
        endTest();
    }

    /** Test of moveTabsLeft method. */
    public void testMoveTabsLeft() {
        // this test depends on previous
        startTest();
        // but not block anything else
        clearTestStatus();
        EditorWindowOperator ewo = new EditorWindowOperator();
        ewo.moveTabsLeft();
        assertFalse("Tabs were not moved to the left.", ewo.btLeft().isEnabled());
    }
    
    /** Test of verify method. */
    public void testVerify() {
        EditorWindowOperator ewo = new EditorWindowOperator();
        ewo.verify();
    }

    /** Test of closeDiscard method. */
    public void testCloseDiscard() {
        new EditorWindowOperator().closeDiscard();
    }
}

