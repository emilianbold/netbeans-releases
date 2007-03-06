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
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

/**
 *  Test of OutputOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class OutputOperatorTest extends JellyTestCase {
    
    public OutputOperatorTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite();
        // suites have to be in particular order
        suite.addTest(new OutputOperatorTest("testInvoke"));
        suite.addTest(new OutputOperatorTest("testGetOutputTab"));
        suite.addTest(new OutputOperatorTest("testGetText"));
        suite.addTest(new OutputOperatorTest("testSelectAll"));
        suite.addTest(new OutputOperatorTest("testCopy"));
        suite.addTest(new OutputOperatorTest("testFind"));
        suite.addTest(new OutputOperatorTest("testFindNext"));
        // TODO
        //suite.addTest(new OutputOperatorTest("testNextError"));
        // TODO
        //suite.addTest(new OutputOperatorTest("testPreviousError"));
        suite.addTest(new OutputOperatorTest("testWrapText"));
        suite.addTest(new OutputOperatorTest("testSaveAs"));
        suite.addTest(new OutputOperatorTest("testClear"));
        suite.addTest(new OutputOperatorTest("testVerify"));
        return suite;
    }
    
    /** Print out test name. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    // OutputOperator instance used in tests
    private static OutputOperator outputOperator;
    private static final String OUTPUT_TITLE = "SampleProject (debug)";
    
    /**
     * Test of invoke method
     */
    public void testInvoke() {
        OutputOperator.invoke().close();
        // be sure it is opened
        outputOperator = OutputOperator.invoke();
    }
    
    /**
     * Test of getOutputTab method
     */
    public void testGetOutputTab() {
        // setup - open output tab
        Node sampleProjectNode = ProjectsTabOperator.invoke().getProjectRootNode("SampleProject");
        new DebugProjectAction().perform(sampleProjectNode);
        // increase time to wait
        outputOperator.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        // wait for finish of debugging
        outputOperator.getOutputTab(OUTPUT_TITLE).waitText("total time");
        
        OutputTabOperator oto = outputOperator.getOutputTab(OUTPUT_TITLE);
        assertTrue("Wrong OutputTabOperator found.", oto.getName().indexOf(OUTPUT_TITLE) > -1);
    }
    
    /**
     * Test of getText method
     */
    public void testGetText() {
        String text = outputOperator.getText();
        assertTrue("Text is not from debugger term.", text.indexOf("debug") > -1); //NOI18N
    }
    
    /**
     * Test of selectAll method
     */
    public void testSelectAll() {
        startTest();
        outputOperator.getOutputTab(OUTPUT_TITLE);
        outputOperator.selectAll();
        endTest();
    }

    /**
     * Test of copy method
     */
    public void testCopy() throws Exception {
        startTest();
        clearTestStatus();
        outputOperator.copy();
        assertTrue("Copy doesn't work.", getClipboardText().indexOf("debug") > -1);   // NOI18N
    }
    
    /**
     * Test of find method
     */
    public void testFind() throws Exception {
        outputOperator.find();
        // "Find"
        String findTitle = Bundle.getString("org.netbeans.core.output2.Bundle", "LBL_Find_Title");
        NbDialogOperator findDialog = new NbDialogOperator(findTitle);
        // assuming debug string is printed in output at least twice
        new JTextFieldOperator(findDialog).setText("b");   // NOI18N
        // "Find"
        String findButtonLabel = Bundle.getStringTrimmed("org.netbeans.core.output2.Bundle", "BTN_Find");
        new JButtonOperator(findDialog, findButtonLabel).push();
        // wait a little until "b" is selected
        new EventTool().waitNoEvent(500);
        // verify "b" is selected
        outputOperator.copy();
        if(!getClipboardText().equals("b")) {
            // repeat because find action was not executed
            outputOperator.find();
            findDialog = new NbDialogOperator(findTitle);
            new JTextFieldOperator(findDialog).setText("b");   // NOI18N
            new JButtonOperator(findDialog, findButtonLabel).push();
        }
    }
    
    /**
     * Test of findNext method
     */
    public void testFindNext() {
        outputOperator.findNext();
    }
    
    /** Test of nextError method. */
    public void testNextError() {
        // TODO add test some day
        //outputOperator.nextError();
    }
    
    /** Test of previousError method. */
    public void testPreviousError() {
        // TODO add test some day
        //outputOperator.previousError();
    }

    /** Test of wrapText method. */
    public void testWrapText() {
        // set
        outputOperator.wrapText();
        // unset
        outputOperator.wrapText();
    }

    /**
     * Test of saveAs method.
     */
    public void testSaveAs() {
        outputOperator.saveAs();
        // "Save As"
        String saveAsTitle = Bundle.getString("org.netbeans.core.output2.Bundle", "TITLE_SAVE_DLG");
        new NbDialogOperator(saveAsTitle).close();
    }
    
    /** Test of clear method. */
    public void testClear() {
        outputOperator.clear();
        assertTrue("Text was not cleared.", outputOperator.getText().length() == 0);
    }
    
    /**
     * Test of verify method
     */
    public void testVerify() {
        // currently does nothing
        outputOperator.verify();
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
