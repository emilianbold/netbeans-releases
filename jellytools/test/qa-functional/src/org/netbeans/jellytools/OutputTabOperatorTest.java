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

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.netbeans.jellytools.actions.CompileAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

/** Test of OutputTabOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class OutputTabOperatorTest extends JellyTestCase {
    
    public OutputTabOperatorTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite();
        // suites have to be in particular order
        suite.addTest(new OutputTabOperatorTest("testMakeComponentVisible"));
        suite.addTest(new OutputTabOperatorTest("testFindLine"));
        suite.addTest(new OutputTabOperatorTest("testGetText"));
        suite.addTest(new OutputTabOperatorTest("testWaitText"));
        suite.addTest(new OutputTabOperatorTest("testGetLineCount"));
        suite.addTest(new OutputTabOperatorTest("testGetLine"));
        suite.addTest(new OutputTabOperatorTest("testGetLength"));
        suite.addTest(new OutputTabOperatorTest("testVerify"));
        suite.addTest(new OutputTabOperatorTest("testSelectAll"));
        suite.addTest(new OutputTabOperatorTest("testCopy"));
        suite.addTest(new OutputTabOperatorTest("testFind"));
        suite.addTest(new OutputTabOperatorTest("testFindNext"));
        suite.addTest(new OutputTabOperatorTest("testSaveAs"));
        //suite.addTest(new OutputTabOperatorTest("testNextError"));
        //suite.addTest(new OutputTabOperatorTest("testPreviousError"));
        suite.addTest(new OutputTabOperatorTest("testWrapText"));
        suite.addTest(new OutputTabOperatorTest("testClear"));
        suite.addTest(new OutputTabOperatorTest("testClose"));
        return suite;
    }
    
    /** Print out test name. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    private static final String targetName = "compile-single";
    //private static final String targetName = "compile-test-single";
    private static OutputTabOperator outputTabOperator;

    /** Compiles a source which opens output tab to test. */
    private static void initTab() {
        Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
        Node sample2 = new Node(new SourcePackagesNode("SampleProject"), "sample1.sample2");  // NOI18N
        Node sampleClass1 = new Node(sample1, "SampleClass1.java");  // NOI18N
        CompileAction compileAction = new CompileAction();
        compileAction.perform(sampleClass1);
        outputTabOperator = new OutputTabOperator(targetName);
        // increase time to wait
        outputTabOperator.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        // wait build is finished
        outputTabOperator.waitText("BUILD SUCCESSFUL");
    }

    /**
     * Test of makeComponentVisible method.
     */
    public void testMakeComponentVisible() {
        initTab();
        // should be improved to use 2 terms and activate the hidden one
        outputTabOperator.makeComponentVisible();
        assertTrue(targetName+" output tab should be visible.", outputTabOperator.isShowing());
    }

    /**
     * Test of findLine method.
     */
    public void testFindLine() {
        assertTrue("Wrong row found.", outputTabOperator.findLine(targetName)>0); // NOI18N
    }
    
    /**
     * Test of getText method.
     */
    public void testGetText() {
        String text = outputTabOperator.getText();
        assertTrue("Text is not from "+targetName+" output tab.", text.indexOf(targetName) > -1);
        String twoLines = outputTabOperator.getText(0, 1);
        assertTrue("Text from first and second lines should contain 2 ':'", twoLines.indexOf(':') != twoLines.lastIndexOf(':'));
    }
    
    /**
     * Test of waitText method.
     */
    public void testWaitText() {
        outputTabOperator.waitText(targetName);
    }
    
    /**
     * Test of getLineCount method.
     */
    public void testGetLineCount() {
        assertTrue("Wrong line count.", outputTabOperator.getLineCount()>0);
    }
    
    
    /**
     * Test of getLine() method.
     */
    public void testGetLine() {
        assertTrue("Wrong text found.", outputTabOperator.getLine(outputTabOperator.findLine(targetName)).indexOf(targetName) > -1);
    }
    
    /**
     * Test of getLength() method.
     */
    public void testGetLength() {
        assertEquals("Wrong length returned.", outputTabOperator.getLength(), outputTabOperator.getText().length());
    }

    /**
     * Test of verify method.
     */
    public void testVerify() {
        outputTabOperator.verify();
    }
    
    /**
     * Test of selectAll method.
     */
    public void testSelectAll() {
        clearTestStatus();
        startTest();
        outputTabOperator.selectAll();
        endTest();
    }

    /**
     * Test of copy method.
     */
    public void testCopy() throws Exception {
        startTest();
        clearTestStatus();
        outputTabOperator.copy();
        assertTrue("Copy doesn't work.", getClipboardText().indexOf(targetName) > -1);
    }
    
    /**
     * Test of find method.
     */
    public void testFind() {
        outputTabOperator.find();
        // "Find"
        String findTitle = Bundle.getString("org.netbeans.core.output2.Bundle", "LBL_Find_Title");
        NbDialogOperator findDialog = new NbDialogOperator(findTitle);
        // assuming somthing with 'a' is printed in output
        JTextFieldOperator jtfo = new JTextFieldOperator(findDialog);
        jtfo.enterText("a");   // NOI18N
        try {
            // need to wait find action is finished
            findDialog.waitClosed();
        } catch (JemmyException e) {
            // sometimes it fails on Solaris => try it once more in Robot mode
            log("Dialog not closed first time. Trying once more.");
            // "Find"
            String findButtonLabel = Bundle.getString("org.netbeans.core.output2.Bundle", "BTN_Find");
            JButtonOperator findButtonOper = new JButtonOperator(findDialog, findButtonLabel);
            findButtonOper.getProperties().setDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
            findButtonOper.push();
            findDialog.waitClosed();
        }
        new EventTool().waitNoEvent(1000);
    }
    
    /**
     * Test of findNext method.
     */
    public void testFindNext() {
        outputTabOperator.findNext();
    }
    
    /**
     * Test of saveAs method.
     */
    public void testSaveAs() {
        outputTabOperator.saveAs();
        // "Save As"
        String saveAsTitle = Bundle.getString("org.netbeans.core.output2.Bundle", "TITLE_SAVE_DLG");
        new NbDialogOperator(saveAsTitle).close();
    }

    /** Test of nextError method. */
    public void testNextError() {
        // TODO add test some day
        //outputTabOperator.nextError();
    }
    
    /** Test of previousError method. */
    public void testPreviousError() {
        // TODO add test some day
        //outputTabOperator.previousError();
    }
    
    /** Test of wrapText method. */
    public void testWrapText() {
        // set
        outputTabOperator.wrapText();
        // unset
        outputTabOperator.wrapText();
    }
    
    /** Test of clear method. */
    public void testClear() {
        outputTabOperator.clear();
        assertTrue("Text was not cleared.", outputTabOperator.getText().length() == 0);
    }
    
    /**f
     * Test of close method.
     */
    public void testClose() {
        outputTabOperator.close();
        assertFalse("Output tab should be closed.", outputTabOperator.isShowing());
    }
    
    /** Wait until clipboard contains string data and returns the text. */
    private String getClipboardText() throws Exception {
        Waiter waiter = new Waiter(new Waitable() {
            public Object actionProduced(Object obj) {
                Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                if(contents == null) {
                    return null;
                } else {
                    return contents.isDataFlavorSupported(DataFlavor.stringFlavor) ? Boolean.TRUE : null;
                }
            }
            public String getDescription() {
                return("Wait clipboard contains string data");
            }
        });
        waiter.waitAction(null);
        return Toolkit.getDefaultToolkit().getSystemClipboard().
             getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
    }
}
