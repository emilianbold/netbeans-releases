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

import java.util.Arrays;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.PasteActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

/** Test DocumentsDialogOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class DocumentsDialogOperatorTest extends JellyTestCase {
    
    public DocumentsDialogOperatorTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite();
        // test cases have to be in particular order
        suite.addTest(new DocumentsDialogOperatorTest("testInvoke"));
        suite.addTest(new DocumentsDialogOperatorTest("testVerify"));
        suite.addTest(new DocumentsDialogOperatorTest("testSelectDocument"));
        suite.addTest(new DocumentsDialogOperatorTest("testSelectDocuments"));
        suite.addTest(new DocumentsDialogOperatorTest("testGetDescription"));
        suite.addTest(new DocumentsDialogOperatorTest("testSaveDocuments"));
        suite.addTest(new DocumentsDialogOperatorTest("testCloseDocuments"));
        suite.addTest(new DocumentsDialogOperatorTest("testSwitchToDocument"));
        return suite;
    }
    
    /** Print out test name. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }

    private static DocumentsDialogOperator documentsOper;
    private static Node editableSourceNode;
    
    /**
     * Test of invoke method.
     */
    public void testInvoke() {
        EditorOperator.closeDiscardAll();
        Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
        Node sample2 = new Node(new SourcePackagesNode("SampleProject"), "sample1.sample2");  // NOI18N
        Node node = new Node(sample1, "SampleClass1");// NOI18N
        new OpenAction().performAPI(node);
        node = new Node(sample2, "SampleClass2");// NOI18N
        new OpenAction().performAPI(node);
        // copy node to be able to write in
        new CopyAction().performAPI(node);
        new PasteActionNoBlock().performAPI(sample2);
        String copyClassTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_CopyClass");
        NbDialogOperator copyClassOper = new NbDialogOperator(copyClassTitle);
        // "Refactor"
        String refactorLabel = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_Finish");
        new JButtonOperator(copyClassOper, refactorLabel).push();
        copyClassOper.waitClosed();
        editableSourceNode = new Node(sample2, "SampleClass21");// NOI18N
        new OpenAction().performAPI(editableSourceNode);
        documentsOper = DocumentsDialogOperator.invoke();
    }
    
    /**
     * Test of verify method.
     */
    public void testVerify() {
        documentsOper.verify();
    }

    /**
     * Test of selectDocument method.
     */
    public void testSelectDocument() {
        documentsOper.selectDocument("SampleClass1.java"); // NOI18N
        assertEquals("Wrong document selected.", "SampleClass1.java", 
                     documentsOper.lstDocuments().getSelectedValue().toString());  // NOI18N
        documentsOper.selectDocument(2);
        assertEquals("Wrong document selected.", 2, documentsOper.lstDocuments().getSelectedIndex());  // NOI18N
    }
    
    /**
     * Test of selectDocuments method.
     */
    public void testSelectDocuments() {
        String[] documents = {"SampleClass1.java", "SampleClass2.java"}; // NOI18N
        documentsOper.selectDocuments(documents);
        Object[] selected = documentsOper.lstDocuments().getSelectedValues();
        for(int i = 0;i<selected.length;i++) {
            assertEquals("Wrong document selected by names.", documents[i], selected[i].toString());
        }
        // test one document
        documentsOper.selectDocuments(new String[] {"SampleClass21.java"}); // NOI18N
        assertEquals("Wrong document selected.", "SampleClass21.java", 
                     documentsOper.lstDocuments().getSelectedValue().toString());  // NOI18N
        
        int[] indexes = {0, 1};
        documentsOper.selectDocuments(indexes);
        assertTrue("Wrong documents selected by indexes.", 
                   Arrays.equals(indexes, documentsOper.lstDocuments().getSelectedIndices()));  // NOI18N
        // test one document
        documentsOper.selectDocuments(new int[] {2});
        assertEquals("Wrong document selected.", 2, documentsOper.lstDocuments().getSelectedIndex());  // NOI18N
    }

    /**
     * Test of getDescription method.
     */
    public void testGetDescription() {
        documentsOper.selectDocument("SampleClass1.java"); // NOI18N
        assertTrue("Wrong description obtain.", documentsOper.getDescription().indexOf("SampleClass1.java") > -1); // NOI18N
    }

    /**
     * Test of saveDocuments method.
     */
    public void testSaveDocuments() {
        EditorOperator eo = new EditorOperator("SampleClass21.java"); // NOI18N
        eo.insert("//dummy\n", 1, 1); // NOI18N
        documentsOper.selectDocument("SampleClass21.java");  // NOI18N
        documentsOper.saveDocuments();
        boolean modified = eo.isModified();
        eo.closeDiscard();
        assertFalse("Document is not saved.", modified);//NOI18N
    }
    
    /**
     * Test of closeDocuments method.
     */ 
    public void testCloseDocuments() {
        documentsOper.selectDocument("SampleClass2.java");  // NOI18N
        documentsOper.closeDocuments();
        assertTrue("Document was not closed.", documentsOper.lstDocuments().getModel().getSize() == 1);
    }
    
    /**
     * Test of switchToDocument method.
     */
    public void testSwitchToDocument() {
        documentsOper.selectDocument("SampleClass1.java"); //NOI18N
        documentsOper.switchToDocument();
        // clean up - delete editable source
        new DeleteAction().performAPI(editableSourceNode);
        // "Confirm Object Deletion"
        String confirmTitle = Bundle.getString("org.openide.explorer.Bundle",
                                               "MSG_ConfirmDeleteObjectTitle"); // NOI18N
        new NbDialogOperator(confirmTitle).yes();
    }
}
