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
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 * Test of org.netbeans.jellytools.NewFileWizardOperator.
 * @author tb115823
 */
public class NewFileWizardOperatorTest extends JellyTestCase {

    public static NewFileWizardOperator op;
    
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
        suite.addTest(new NewFileWizardOperatorTest("testInvokeTitle"));
        suite.addTest(new NewFileWizardOperatorTest("testInvoke"));
        suite.addTest(new NewFileWizardOperatorTest("testSelectProjectAndCategoryAndFileType"));
        suite.addTest(new NewFileWizardOperatorTest("testGetDescription"));
        suite.addTest(new NewFileWizardOperatorTest("testCreate"));
        return suite;
    }
    
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewFileWizardOperatorTest(String testName) {
        super(testName);
    }
    
    /** Test of invoke method with title parameter. Opens New File wizard, waits for the dialog and closes it. */
    public void testInvokeTitle() {
        // "New File"
        String title = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewFileWizard_Title");
        op = NewFileWizardOperator.invoke(title);
        op.close();
    }
    
    /** Test of invoke method. Opens New File wizard and waits for the dialog. */
    public void testInvoke() {
        op = NewFileWizardOperator.invoke();
    }
    
    /** Test components on New File Wizard panel 
     *  sets category and filetype
     */
    public void testSelectProjectAndCategoryAndFileType() {
        JComboBoxOperator cbo = op.cboProject();
        cbo.selectItem(0);
        // Java Classes
        op.selectCategory(Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes"));
        // Java Class
        op.selectFileType(Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Class.java"));
    }

    /** Test description component on New File Wizard panel
     *  gets description of selected filetype
     */
    public void testGetDescription() {
        assertTrue("Description should contain Java class sub string.", op.getDescription().indexOf("Java class") > 0);
        op.cancel();
    }

    public void testCreate() {
        // Java Classes
        String javaClassesLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes");
        // Java Class
        String javaClassLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Class.java");
        NewFileWizardOperator.create("SampleProject", javaClassesLabel, javaClassLabel, "sample1", "TempClass");  // NOI18N
        Node classNode = new Node(new SourcePackagesNode("SampleProject"), "sample1|TempClass");  // NOI18N
        DeleteAction deleteAction = new DeleteAction();
        deleteAction.perform(classNode);
        // "Confirm Object Deletion"
        String confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle"); // NOI18N
        new NbDialogOperator(confirmTitle).yes();
    }
}
