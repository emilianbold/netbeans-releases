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

/*
 * CreateTestTest.java
 *
 * Created on August 2, 2006, 1:39 PM
 */

package org.netbeans.test.junit.testcreation;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.junit.testcase.JunitTestCase;
import org.netbeans.test.junit.utils.Utilities;

/**
 *
 * @author ms159439
 */
public class CreateTestTest extends JunitTestCase {
    
    /** path to sample files */
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.junit.testcreation";
    
    /** name of sample package */
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH+".test";
    
    /**
     * Creates a new instance of CreateTestTest
     */
    public CreateTestTest(String testName) {
        super(testName);
    }
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(CreateTestTest.class);
        return suite;
    }
    
    /**
     * Test creation accessed from popup menu
     * With default options (checkboxes)
     */
    public void testCreateTestByPopup() {
        //open sample class
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        
        Utilities.pushCreateTestsPopup(n);
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        ndo.btOK().push(); //defaults checked
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation accessed from popup menu
     * Without public methods
     */
    public void testCreateTestByPopup2() {
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        
        Utilities.pushCreateTestsPopup(n);
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 0);//public methods
        jbo.clickMouse();
        ndo.btOK().push();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME +
                "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation accessed from popup menu
     * Without public methods and w/o protected methods
     */
    public void testCreateTestByPopup3() {
        //necessary to delete created tests from testCreateTestByPopup2
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|"  + Utilities.TEST_CLASS_NAME);
        
        Utilities.pushCreateTestsPopup(n);
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 1);//protected methods
        jbo.clickMouse();
        ndo.btOK().push();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation accessed from popup menu
     * Without public methods and w/o protected methods and w/o friendly
     * should allow to create anything -- OK should be disabled
     */
    public void testCreateTestByPopup4() {
        //necessary to delete created tests from testCreateTestByPopup3
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
        Utilities.pushCreateTestsPopup(n);
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 2);//friendly methods
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        assertFalse(ndo.btOK().isEnabled()); // OK button should be disabled
        ndo.btCancel().push(); //cancel the dialog
    }
    
    /**
     * Test creation w/o setUp() 
     */
    public void testCreateWOsetUp() {
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
        Utilities.pushCreateTestsPopup(n);
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 3);//setUp methods
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        ndo.btOK().push();
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation w/o tearDown() 
     */
    public void testCreateWOtearDown() {
        //necessary to delete created tests from testCreateTestWOsetUp
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
        Utilities.pushCreateTestsPopup(n);
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 4);//tearDown methods
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        ndo.btOK().push();
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation w/o default method bodies 
     */
    public void testCreateWODefMethodBodies() {
        //necessary to delete created tests from testCreateTestWOsetUp
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
        Utilities.pushCreateTestsPopup(n);
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 5);//methods bodies
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        ndo.btOK().push();
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation w/o javdoc comments 
     */
    public void testCreateWOJavadoc() {
        //necessary to delete created tests from testCreateTestWOsetUp
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
        Utilities.pushCreateTestsPopup(n);
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 6);//javadoc
        
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        ndo.btOK().push();
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation w/o source code hints() 
     */
    public void testCreateWOHints() {
        //necessary to delete created tests from testCreateTestWOsetUp
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
        Utilities.pushCreateTestsPopup(n);
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 7);//hints
        
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        ndo.btOK().push();
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation accessed from wizard
     */
    public void testCreateTestByWizard() {
        //necessary to delete created tests previous
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        NewFileWizardOperator op = NewFileWizardOperator.invoke();
        op.selectCategory(Bundle.getString(Utilities.JUNIT_BUNDLE,
                "Templates/JUnit"));
        op.selectFileType(Bundle.getString(Utilities.JUNIT_BUNDLE,
                "Templates/JUnit/SimpleJUnitTest.java"));
        op.next();
        new JTextFieldOperator(op,0).
                setText("org.netbeans.test.junit.testcreation.test.TestClass");
        op.finish();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
        
        //For some resaon, this couses test to fail
        //        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
        //                "|" + TEST_PACKAGE_NAME);
    }
    
}
