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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.actions;

import javax.swing.JLabel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.Action;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Call menu actions "Generate MBean Registration".
 * This action is activable only when clicking directly in the java file editor.
 * Check components and created files.
 */
public class GenerateMBeanRegistrationActions extends ActionsTestCase {
    
    /** Need to be defined because of JUnit */
    public GenerateMBeanRegistrationActions(String name) {
        super(name);
        popupPath = ACTION_JMX + "|" + ACTION_GENERATE_MBEAN_REGISTRATION;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new GenerateMBeanRegistrationActions("init"));
        suite.addTest(new GenerateMBeanRegistrationActions("test1"));
        suite.addTest(new GenerateMBeanRegistrationActions("test2"));
        suite.addTest(new GenerateMBeanRegistrationActions("test3"));
        suite.addTest(new GenerateMBeanRegistrationActions("test4"));
        suite.addTest(new GenerateMBeanRegistrationActions("test5"));
        suite.addTest(new GenerateMBeanRegistrationActions("test6"));
        suite.addTest(new GenerateMBeanRegistrationActions("test7"));
        suite.addTest(new GenerateMBeanRegistrationActions("test8"));
        return suite;
    }
    
    /**
     * Create all needed files for running next tests
     */
    public void init() {
        
        System.out.println("====================  init  ====================");
        
        System.out.println("Create new java class " + SIMPLE_1);
        createJavaFile(SIMPLE_1);
        
        System.out.println("Create new java class " + SIMPLE_3);
        createJavaFile(SIMPLE_3);
        
        System.out.println("Create new java class " + SIMPLE_4);
        createJavaFile(SIMPLE_4);
        
        String name = null;
        
        // Create a java file that is modified by the test
        // Add a prefix to this file in order to compare it with
        // the expected modified golden file instead of the original one
        // Update 1
        name = "U1" + SIMPLE_4;
        System.out.println("Create new java class " + name);
        createJavaFile(name, SIMPLE_4);
        // Update 2
        name = "U2" + SIMPLE_4;
        System.out.println("Create new java class " + name);
        createJavaFile(name, SIMPLE_4);
        // Update 3
        name = "U3" + SIMPLE_4;
        System.out.println("Create new java class " + name);
        createJavaFile(name, SIMPLE_4);
        // Update 4
        name = "U4" + SIMPLE_4;
        System.out.println("Create new java class " + name);
        createJavaFile(name, SIMPLE_4);
        // Update 5
        name = "U5" + SIMPLE_4;
        System.out.println("Create new java class " + name);
        createJavaFile(name, SIMPLE_4);
        // Update 6
        name = "U6" + SIMPLE_4;
        System.out.println("Create new java class " + name);
        createJavaFile(name, SIMPLE_4);
        
        System.out.println("Create new java class " + SIMPLE_5);
        createJavaFile(SIMPLE_5);
        
        System.out.println("Create new java class " + SIMPLE_5_INTF);
        createJavaFile(SIMPLE_5_INTF);
        
        System.out.println("Create new java class " + DYNAMIC_1);
        createJavaFile(DYNAMIC_1);
    }

    public void test1() {
        
        System.out.println("====================  test1  ====================");
        
        System.out.println("Check action menu components for " + SIMPLE_3);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + SIMPLE_3);
        System.out.println("Open java file " + SIMPLE_3);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(SIMPLE_3);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertFalse(jmio.isEnabled());
    }
    
    public void test2() {
        
        String className = "U1" + SIMPLE_4;
        
        System.out.println("====================  test2  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.setCaretPosition("//TODO Add your MBean registration code here", false);
        eo.insert("\n");
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(jmio.isEnabled());
        
        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(
                GENERATE_MBEAN_REGISTRATION_DIALOG_TITLE);
        System.out.println("Set MBean Class to " + packageName + ".NotAnMBean");
        setTextFieldContent(MBEAN_CLASS_TEXT_FIELD, ndo, 
                packageName + ".NotAnMBean" + className);
        assertFalse(ndo.btOK().isEnabled());
        
        // Check warning message is displayed
        System.out.println("Check warning message = " + NOT_A_MBEAN_CLASS_WARNING);
        JLabel jl = getLabel(NOT_A_MBEAN_CLASS_WARNING, ndo.getContentPane());
        assertNotNull(jl);
        
        System.out.println("Set MBean class to " + 
                packageName + "." + DYNAMIC_1);
        setTextFieldContent(MBEAN_CLASS_TEXT_FIELD, ndo, 
                packageName + "." + DYNAMIC_1);
        System.out.println("Set MBean object name to " +
                packageName + ":type=" + DYNAMIC_1);
        setTextFieldContent(OBJECT_NAME_TEXT_FIELD, ndo, 
                packageName + ":type=" + DYNAMIC_1);
        System.out.println("Set MBean constructor to " + DYNAMIC_1 + "()");
        selectComboBoxItem(CONSTRUCTOR_COMBO_BOX, ndo, DYNAMIC_1 + "()");
        assertTrue(ndo.btOK().isEnabled());
        ndo.ok();
        
        // Save updated java file
        node.select();
        eo.save();
        System.out.println("Check update java class file");
        String content = getFileContent(getGoldenFile(className));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
    }
    
    public void test3() {
        
        String className = "U2" + SIMPLE_4;
        
        System.out.println("====================  test3  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.setCaretPosition("//TODO Add your MBean registration code here", false);
        eo.insert("\n");
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(jmio.isEnabled());
        
        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(
                GENERATE_MBEAN_REGISTRATION_DIALOG_TITLE);
        System.out.println("Set MBean class to " + 
                packageName + "." + DYNAMIC_1);
        setTextFieldContent(MBEAN_CLASS_TEXT_FIELD, ndo, 
                packageName + "." + DYNAMIC_1);
        System.out.println("Set MBean object name to " +
                packageName + ":type=" + DYNAMIC_1);
        setTextFieldContent(OBJECT_NAME_TEXT_FIELD, ndo, 
                packageName + ":type=" + DYNAMIC_1);
        System.out.println("Set MBean constructor to " + DYNAMIC_1 + "(int[])");
        selectComboBoxItem(CONSTRUCTOR_COMBO_BOX, ndo, DYNAMIC_1 + "(int[])");
        ndo.ok();
        
        // Save updated java file
        node.select();
        eo = new EditorOperator(className);
        eo.save();
        System.out.println("Check update java class file");
        String content = getFileContent(getGoldenFile(className));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
    }
    
    public void test4() {
        
        String className = "U3" + SIMPLE_4;
        
        System.out.println("====================  test4  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.setCaretPosition("//TODO Add your MBean registration code here", false);
        eo.insert("\n");
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(jmio.isEnabled());
        
        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(
                GENERATE_MBEAN_REGISTRATION_DIALOG_TITLE);
        System.out.println("Register java object wrapped in a standard MBean");
        setRadioButtonSelection(STANDARD_MBEAN_RADIO_BUTTON, ndo, true);
        System.out.println("Set java class to " + 
                packageName + ".NonExistingJavaClass");
        setTextFieldContent(JAVA_CLASS_TEXT_FIELD, ndo, 
                packageName + ".NonExistingJavaClass");
        assertFalse(ndo.btOK().isEnabled());
        
        // Check warning message is displayed
        System.out.println("Check warning message = " +
                SPECIFIED_JAVA_CLASS_DOES_NOT_EXIST_WARNING);
        JLabel jl = getLabel(SPECIFIED_JAVA_CLASS_DOES_NOT_EXIST_WARNING,
                ndo.getContentPane());
        assertNotNull(jl);
        
        System.out.println("Set java class to " + packageName + "." + SIMPLE_5);
        setTextFieldContent(JAVA_CLASS_TEXT_FIELD, ndo, 
                packageName + "." + SIMPLE_5);
        System.out.println("Set MBean object name to " +
                packageName + ":type=" + SIMPLE_5);
        setTextFieldContent(OBJECT_NAME_TEXT_FIELD, ndo, 
                packageName + ":type=" + SIMPLE_5);
        System.out.println("Set management interface applying " +
                "JMX Design Pattern Management Interface");
        try {
            selectComboBoxItem(MANAGEMENT_INTERFACE_COMBO_BOX, ndo,
                    "Apply JMX Design Pattern Management Interface");
            fail("The constructor list musn't have the item 'Apply JMX Design " +
                    "Pattern Management Interface'.");
        } catch (org.netbeans.jemmy.TimeoutExpiredException e) {}
        System.out.println("Set management interface to " +
                packageName + "." + SIMPLE_5_INTF);
        selectComboBoxItem(MANAGEMENT_INTERFACE_COMBO_BOX, ndo, 
                packageName + "." + SIMPLE_5_INTF);
        System.out.println("Set MBean constructor to object instantiation not generated");
        selectComboBoxItem(CONSTRUCTOR_COMBO_BOX, ndo, 
                "Object Instantiation not generated");
        assertTrue(ndo.btOK().isEnabled());
        ndo.ok();
        
        // Save updated java file
        node.select();
        eo = new EditorOperator(className);
        eo.save();
        System.out.println("Check update java class file");
        String content = getFileContent(getGoldenFile(className));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
    }
    
    public void test5() {
        
        String className = "U4" + SIMPLE_4;
        
        System.out.println("====================  test5  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.setCaretPosition("//TODO Add your MBean registration code here", false);
        eo.insert("\n");
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(jmio.isEnabled());
        
        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(
                GENERATE_MBEAN_REGISTRATION_DIALOG_TITLE);
        System.out.println("Register java object wrapped in a standard MBean");
        setRadioButtonSelection(STANDARD_MBEAN_RADIO_BUTTON, ndo, true);
        System.out.println("Set java class to " + packageName + "." + SIMPLE_5);
        setTextFieldContent(JAVA_CLASS_TEXT_FIELD, ndo, 
                packageName + "." + SIMPLE_5);
        System.out.println("Set MBean object name to " +
                packageName + ":type=" + SIMPLE_5);
        setTextFieldContent(OBJECT_NAME_TEXT_FIELD, ndo, 
                packageName + ":type=" + SIMPLE_5);
        System.out.println("Set management interface to " +
                packageName + "." + SIMPLE_5_INTF);
        selectComboBoxItem(MANAGEMENT_INTERFACE_COMBO_BOX, ndo, 
                packageName + "." + SIMPLE_5_INTF);
        System.out.println("Set MBean constructor to " + SIMPLE_5 + "()");
        selectComboBoxItem(CONSTRUCTOR_COMBO_BOX, ndo, SIMPLE_5 + "()");
        assertTrue(ndo.btOK().isEnabled());
        ndo.ok();
        
        // Save updated java file
        node.select();
        eo = new EditorOperator(className);
        eo.save();
        System.out.println("Check update java class file");
        String content = getFileContent(getGoldenFile(className));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
    }
    
    public void test6() {
        
        String className = "U5" + SIMPLE_4;
        
        System.out.println("====================  test6  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.setCaretPosition("//TODO Add your MBean registration code here", false);
        eo.insert("\n");
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(jmio.isEnabled());
        
        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(
                GENERATE_MBEAN_REGISTRATION_DIALOG_TITLE);
        System.out.println("Register java object wrapped in a standard MBean");
        setRadioButtonSelection(STANDARD_MBEAN_RADIO_BUTTON, ndo, true);
        System.out.println("Set java class to " + packageName + "." + SIMPLE_5);
        setTextFieldContent(JAVA_CLASS_TEXT_FIELD, ndo, 
                packageName + "." + SIMPLE_5);
        System.out.println("Set MBean object name to " +
                packageName + ":type=" + SIMPLE_5);
        setTextFieldContent(OBJECT_NAME_TEXT_FIELD, ndo, 
                packageName + ":type=" + SIMPLE_5);
        System.out.println("Set management interface to " +
                packageName + "." + SIMPLE_5_INTF);
        selectComboBoxItem(MANAGEMENT_INTERFACE_COMBO_BOX, ndo, 
                packageName + "." + SIMPLE_5_INTF);
        System.out.println("Set object wrapped as MXBean");
        setCheckBoxSelection(OBJECT_WRAPPED_AS_MXBEAN_CHECK_BOX, ndo, true);
        System.out.println("Set MBean constructor to " + SIMPLE_5 + "(String)");
        selectComboBoxItem(CONSTRUCTOR_COMBO_BOX, ndo, SIMPLE_5 + "(String)");
        assertTrue(ndo.btOK().isEnabled());
        ndo.ok();
        
        // Save updated java file
        node.select();
        eo = new EditorOperator(className);
        eo.save();
        System.out.println("Check update java class file");
        String content = getFileContent(getGoldenFile(className));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
    }
    
    public void test7() {
        
        String className = "U6" + SIMPLE_4;
        
        System.out.println("====================  test7  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.setCaretPosition("//TODO Add your MBean registration code here", false);
        eo.insert("\n");
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(jmio.isEnabled());
        
        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(
                GENERATE_MBEAN_REGISTRATION_DIALOG_TITLE);
        System.out.println("Register java object wrapped in a standard MBean");
        setRadioButtonSelection(STANDARD_MBEAN_RADIO_BUTTON, ndo, true);
        System.out.println("Set java class to " + packageName + "." + DYNAMIC_1);
        setTextFieldContent(JAVA_CLASS_TEXT_FIELD, ndo, 
                packageName + "." + DYNAMIC_1);
        System.out.println("Set MBean object name to " +
                packageName + ":type=" + DYNAMIC_1);
        setTextFieldContent(OBJECT_NAME_TEXT_FIELD, ndo, 
                packageName + ":type=" + DYNAMIC_1);
        System.out.println("Set management interface to " +
                "Apply JMX Design Pattern Management Interface");
        selectComboBoxItem(MANAGEMENT_INTERFACE_COMBO_BOX, ndo, 
                "Apply JMX Design Pattern Management Interface");
        System.out.println("Set MBean constructor to " + DYNAMIC_1 + "(String)");
        selectComboBoxItem(CONSTRUCTOR_COMBO_BOX, ndo, DYNAMIC_1 + "(String)");
        assertTrue(ndo.btOK().isEnabled());
        ndo.ok();
        
        // Save updated java file
        node.select();
        eo = new EditorOperator(className);
        eo.save();
        System.out.println("Check update java class file");
        String content = getFileContent(getGoldenFile(className));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
    }
    
    public void test8() {
        
        String className = SIMPLE_4;
        
        System.out.println("====================  test8  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.setCaretPosition("//TODO Add your MBean registration code here", false);
        eo.insert("\n");
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(jmio.isEnabled());
        
        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(
                GENERATE_MBEAN_REGISTRATION_DIALOG_TITLE);
        System.out.println("Register java object wrapped in a standard MBean");
        setRadioButtonSelection(STANDARD_MBEAN_RADIO_BUTTON, ndo, true);
        System.out.println("Set java class to " + packageName + "." + SIMPLE_1);
        setTextFieldContent(JAVA_CLASS_TEXT_FIELD, ndo, 
                packageName + "." + SIMPLE_1);
        assertFalse(ndo.btOK().isEnabled());
        // Check warning message is displayed
        System.out.println("Check warning message = " +
                SPECIFIED_JAVA_CLASS_CANT_BE_WRAPPED_WARNING);
        JLabel jl = getLabel(SPECIFIED_JAVA_CLASS_CANT_BE_WRAPPED_WARNING,
                ndo.getContentPane());
        assertNotNull(jl);
        ndo.cancel();
    }
}

