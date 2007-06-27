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
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.JLabel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.modules.jmx.test.helpers.Parameter;
import org.netbeans.modules.jmx.test.helpers.Exception;
import org.netbeans.modules.jmx.test.helpers.Operation;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Call menu actions "Add MBean Operations".
 * This action is activable either when selecting the class node or
 * when clicking directly in the java file editor.
 * Check components and created files.
 */
public class AddMBeanOperationsActions extends ActionsTestCase {
    
    /** Need to be defined because of JUnit */
    public AddMBeanOperationsActions(String name) {
        super(name);
        popupPath = ACTION_JMX + "|" + ACTION_ADD_MBEAN_OPERATIONS;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new AddMBeanOperationsActions("init"));
        suite.addTest(new AddMBeanOperationsActions("test1"));
        suite.addTest(new AddMBeanOperationsActions("test2"));
        suite.addTest(new AddMBeanOperationsActions("test3"));
        suite.addTest(new AddMBeanOperationsActions("test4"));
        suite.addTest(new AddMBeanOperationsActions("test5"));
        suite.addTest(new AddMBeanOperationsActions("test6"));
        return suite;
    }
    
    
    /**
     * Create all needed files for running next tests
     */
    public void init() {
        
        System.out.println("====================  init  ====================");
        
        System.out.println("Create new java class " + SIMPLE_1);
        createJavaFile(SIMPLE_1);
        
        System.out.println("Create new java class " + DYNAMIC_1);
        createJavaFile(DYNAMIC_1);
        
        System.out.println("Create new java class " + USER_EXCEPTION);
        createJavaFile(USER_EXCEPTION);
        
        String name = null;
        
        // Create a java file that is modified by the test
        // Add a prefix to this file in order to compare it with
        // the expected modified golden file instead of the original one
        // Update 1
        name = "U1" + ADD_OPERATIONS_1_SUPER;
        System.out.println("Create new java class " + name);
        createJavaFile(name, ADD_OPERATIONS_1_SUPER);
        
        name = "U1" + ADD_OPERATIONS_1_SUPER_MBEAN;
        System.out.println("Create new java interface " + name);
        createJavaFile(name, ADD_OPERATIONS_1_SUPER_MBEAN);
        
        name = "U1" + ADD_OPERATIONS_1_MBEAN;
        System.out.println("Create new java interface " + name);
        Properties properties = new Properties();
        properties.put(ADD_OPERATIONS_1_SUPER_MBEAN,
                "U1" + ADD_OPERATIONS_1_SUPER_MBEAN);
        createJavaFile(name, ADD_OPERATIONS_1_MBEAN, properties);
        
        name = "U1" + ADD_OPERATIONS_1;
        System.out.println("Create new java class " + name);
        createJavaFile(name, ADD_OPERATIONS_1);
        
        // Update 2
        name = "U2" + ADD_OPERATIONS_1_SUPER;
        System.out.println("Create new java class " + name);
        createJavaFile(name, ADD_OPERATIONS_1_SUPER);
        
        name = "U2" + ADD_OPERATIONS_1_SUPER_MBEAN;
        System.out.println("Create new java interface " + name);
        createJavaFile(name, ADD_OPERATIONS_1_SUPER_MBEAN);
        
        name = "U2" + ADD_OPERATIONS_1_MBEAN;
        System.out.println("Create new java interface " + name);
        properties = new Properties();
        properties.put(ADD_OPERATIONS_1_SUPER_MBEAN,
                "U2" + ADD_OPERATIONS_1_SUPER_MBEAN);
        createJavaFile(name, ADD_OPERATIONS_1_MBEAN, properties);
        
        name = "U2" + ADD_OPERATIONS_1;
        System.out.println("Create new java class " + name);
        createJavaFile(name, ADD_OPERATIONS_1);
    }
    
    //=========================================================================
    // CALL ADD ATTRIBUTES ACTION FROM NODE
    //=========================================================================
    
    public void test1() {
        
        System.out.println("====================  test1  ====================");
        
        System.out.println("Check action menu components for " + SIMPLE_1);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + SIMPLE_1);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertFalse(jmio.isEnabled());
    }
    
    public void test2() {
        
        System.out.println("====================  test2  ====================");
        
        System.out.println("Check action menu components for " + DYNAMIC_1);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + DYNAMIC_1);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertFalse(jmio.isEnabled());
    }
    
    public void test3() {
        
        String className = "U1" + ADD_OPERATIONS_1;
        String interfaceName = "U1" + ADD_OPERATIONS_1_MBEAN;
        
        System.out.println("====================  test3  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertTrue(jmio.isEnabled());
        
        addOperationsTest(jmio, className, interfaceName);
    }
    
    //=========================================================================
    // CALL ADD ATTRIBUTES ACTION FROM NODE
    //=========================================================================
    
    public void test4() {
        
        System.out.println("====================  test4  ====================");
        
        System.out.println("Check action menu components for " + SIMPLE_1);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + SIMPLE_1);
        System.out.println("Open java file " + SIMPLE_1);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(SIMPLE_1);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertFalse(jmio.isEnabled());
    }
    
    public void test5() {
        
        System.out.println("====================  test5  ====================");
        
        System.out.println("Check action menu components for " + DYNAMIC_1);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + DYNAMIC_1);
        System.out.println("Open java file " + DYNAMIC_1);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(DYNAMIC_1);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertFalse(jmio.isEnabled());
    }
    
    public void test6() {
        
        String className = "U2" + ADD_OPERATIONS_1;
        String interfaceName = "U2" + ADD_OPERATIONS_1_MBEAN;
        
        System.out.println("====================  test6  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        // Check menu item
        System.out.println("Open java file " + className);
        new Action(null, "Open").perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(jmio.isEnabled());
        
        addOperationsTest(jmio, className, interfaceName);
    }
    
    private void addOperationsTest(
            JMenuItemOperator jmio, 
            String className, 
            String interfaceName) {
        
        ArrayList<Operation> opList = null;
        ArrayList<Parameter> parameters = null;
        ArrayList<Exception> exceptions = null;
        
        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(
                ADD_OPERATIONS_DIALOG_TITLE.replaceAll("<INTERFACE>", interfaceName));
        assertFalse(ndo.btOK().isEnabled());
        
        // Check warning message is displayed
        System.out.println("Check warning message = " +
                SPECIFY_AT_LEAST_ONE_OPERATION_WARNING);
        JLabel jl = getLabel(SPECIFY_AT_LEAST_ONE_OPERATION_WARNING,
                ndo.getContentPane());
        assertNotNull(jl);
        
        JTableOperator jto = getTableOperator(OPERATION_TABLE_FROM_ACTION, ndo);
        
        // Check existing operations
        System.out.println("Check existing operations");
        opList = new ArrayList<Operation>();
        opList.add(new Operation("op4", "void", "Operation exposed for management"));
        parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("param0", "java.lang.String[]", ""));
        exceptions = new ArrayList<Exception>();
        exceptions.add(new Exception("IllegalStateException", ""));
        opList.add(new Operation("op5", "double", parameters, exceptions,
                "Operation exposed for management"));
        checkMBeanOperations(jto, false, opList);
        
        // Add new operations
        System.out.println("Add operations");
        opList = new ArrayList<Operation>();
        // Operation 0
        opList.add(new Operation("op0", "void", "op0 Description"));
        // Operation 1
        parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("s", "String[]", "s Description"));
        exceptions = new ArrayList<Exception>();
        exceptions.add(new Exception(USER_EXCEPTION, "UserException Description"));
        opList.add(new Operation("op1", "int", parameters, exceptions, "op1 Description"));
        // Operation 2
        parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("l", "List", "l Description"));
        parameters.add(new Parameter("d", "Date", "d Description"));
        opList.add(new Operation("op2", "Boolean", parameters, null, "op2 Description"));
        // Operation 3
        parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("s", "String", "s Description"));
        parameters.add(new Parameter("f", "float", "f Description"));
        exceptions = new ArrayList<Exception>();
        exceptions.add(new Exception(USER_EXCEPTION, "UserException Description"));
        exceptions.add(new Exception("IllegalStateException", "IllegalStateException Description"));
        opList.add(new Operation("op3", "long", parameters, exceptions, "op3 Description"));
        addMBeanOperations(ndo, jto, opList);
        ndo.ok();
        
        // Check warning message is displayed
        System.out.println("Check warning message");
        String message = "One or more of the added operation implementations already \n" +
                "exists in the " + className + " class.\n" +
                interfaceName  + " will be updated with the new operations.\n" +
                className + " will not be updated with the following methods,\n" +
                "because they are already present in the class:\n" +
                " - op0()\n" +
                " - op1(String[])\n" +
                " - op2(List,Date)\n";
        ndo = new NbDialogOperator(
                ADD_OPERATIONS_DIALOG_TITLE.replaceAll("<INTERFACE>", interfaceName));
        assertEquals(message, getTextAreaOperator(ACTION_INFO_TEXT_AREA, ndo).getText());
        ndo.ok();
        
        // Save updated java class file
        selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        EditorOperator eo = new EditorOperator(className);
        eo.save();
        System.out.println("Check updated java class file");
        String content = getFileContent(getGoldenFile(className));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
        
        // Save updated java interface file
        selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + interfaceName);
        eo = new EditorOperator(interfaceName);
        eo.save();
        System.out.println("Check updated java interface file");
        content = getFileContent(getGoldenFile(interfaceName));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
    }
}

