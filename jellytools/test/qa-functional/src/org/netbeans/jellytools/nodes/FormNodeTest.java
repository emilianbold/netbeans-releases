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
package org.netbeans.jellytools.nodes;

import java.awt.Toolkit;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.junit.NbTestSuite;

/** Test of org.netbeans.jellytools.nodes.FormNode
 */
public class FormNodeTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FormNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new FormNodeTest("testVerifyPopup"));
        suite.addTest(new FormNodeTest("testOpen"));
        suite.addTest(new FormNodeTest("testEdit"));
        suite.addTest(new FormNodeTest("testCompile"));
        suite.addTest(new FormNodeTest("testCut"));
        suite.addTest(new FormNodeTest("testCopy"));
        suite.addTest(new FormNodeTest("testDelete"));
        suite.addTest(new FormNodeTest("testSaveAsTemplate"));
        suite.addTest(new FormNodeTest("testProperties"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    private static FormNode formNode;
    
    /** Find node. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        if(formNode == null) {
            formNode = new FormNode(new FilesTabOperator().getProjectNode("SampleProject"),
                                    "src|sample1|JFrameSample.java"); // NOI18N
        }
    }
    
    /** Test verifyPopup */
    public void testVerifyPopup() {
        formNode.verifyPopup(); // NOI18N
    }
    
    /** Test open */
    public void testOpen() {
        formNode.open();
        FormDesignerOperator formDesigner = new FormDesignerOperator("JFrameSample");  // NOI18N
        // for an unknown reason IDE thinks that opened form is modified and we need to save it
        new SaveAllAction().performAPI();
        formDesigner.closeDiscard();
    }
    
    /** Test edit  */
    public void testEdit() {
        formNode.edit();
        new EditorOperator("JFrameSample").closeDiscard();  //NOI18N
    }
    
    /** Test compile  */
    public void testCompile() {
        MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        formNode.compile();
        // wait status text "Building SampleProject (compile-single)"
        statusTextTracer.waitText("compile-single", true); // NOI18N
        // wait status text "Finished building SampleProject (compile-single).
        statusTextTracer.waitText("compile-single", true); // NOI18N
        statusTextTracer.stop();
    }
    
    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        formNode.cut();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test copy  */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        formNode.copy();
        Utils.testClipboard(clipboard1);
    }

    /** Test delete */
    public void testDelete() {
        formNode.delete();
        Utils.closeConfirmDialog();
    }
    
    /** Test saveAsTemplate. */
    public void testSaveAsTemplate() {
        formNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }

    /** Test properties */
    public void testProperties() {
        formNode.properties();
        Utils.closeProperties("JFrameSample");  // NOI18N
    }
}
