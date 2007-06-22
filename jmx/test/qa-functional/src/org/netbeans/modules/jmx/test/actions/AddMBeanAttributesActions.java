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
import javax.swing.JLabel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.modules.jmx.test.helpers.Attribute;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Call menu actions "Add MBean Attributes".
 * Check components and created files.
 */
public class AddMBeanAttributesActions extends ActionsTestCase {
    
    /** Need to be defined because of JUnit */
    public AddMBeanAttributesActions(String name) {
        super(name);
        popupPath = ACTION_JMX + "|" + ACTION_ADD_MBEAN_ATTRIBUTES;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new AddMBeanAttributesActions("init"));
        suite.addTest(new AddMBeanAttributesActions("test1"));
        suite.addTest(new AddMBeanAttributesActions("test2"));
        suite.addTest(new AddMBeanAttributesActions("test3"));
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
        
        String name = null;
        
        // Create a java file that is modified by the test
        // Add a prefix to this file in order to compare it with
        // the expected modified golden file instead of the original one
        // Update 1
        name = "U1" + ADD_ATTRIBUTES_1_SUPER;
        System.out.println("Create new java class " + name);
        createJavaFile(name, ADD_ATTRIBUTES_1_SUPER);
        
        name = "U1" + ADD_ATTRIBUTES_1_MBEAN;
        System.out.println("Create new java interface " + name);
        createJavaFile(name, ADD_ATTRIBUTES_1_MBEAN);
        
        name = "U1" + ADD_ATTRIBUTES_1;
        System.out.println("Create new java class " + name);
        createJavaFile(name, ADD_ATTRIBUTES_1);
    }
    
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
        
        String className = "U1" + ADD_ATTRIBUTES_1;
        String interfaceName = "U1" + ADD_ATTRIBUTES_1_MBEAN;
        ArrayList<Attribute> attrList = null;
        
        System.out.println("====================  test3  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertTrue(jmio.isEnabled());
        
        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(
                ADD_ATTRIBUTES_DIALOG_TITLE.replaceAll("<INTERFACE>", interfaceName));
        assertFalse(ndo.btOK().isEnabled());
        
        // Check warning message is displayed
        System.out.println("Check warning message = " +
                SPECIFY_AT_LEAST_ONE_ATTRIBUTE_WARNING);
        JLabel jl = getLabel(SPECIFY_AT_LEAST_ONE_ATTRIBUTE_WARNING,
                ndo.getContentPane());
        assertNotNull(jl);
        
        JTableOperator jto = getTableOperator(ATTRIBUTE_TABLE, ndo);
        
        // Check existing attributes
        System.out.println("Check existing attributes");
        attrList = new ArrayList<Attribute>();
        attrList.add(new Attribute("Attr9", "java.lang.String[]",
                READ_WRITE, "Attribute exposed for management"));
        attrList.add(new Attribute("Attr10", "int",
                READ_ONLY, "Attribute exposed for management"));
        attrList.add(new Attribute("Attr11", "java.util.Date",
                WRITE_ONLY, "Attribute exposed for management"));
        checkMBeanAttributes(jto, attrList);
        
        // Add new attributes
        System.out.println("Add attributes");
        attrList = new ArrayList<Attribute>();
        attrList.add(new Attribute("Attr0", "int", READ_ONLY, "Attr0 Description"));
        attrList.add(new Attribute("Attr1", "boolean", READ_ONLY, "Attr1 Description"));
        attrList.add(new Attribute("Attr2", "java.util.Date", READ_ONLY, "Attr2 Description"));
        attrList.add(new Attribute("Attr3", "ObjectName", READ_WRITE, "Attr3 Description"));
        attrList.add(new Attribute("Attr4", "boolean", READ_WRITE, "Attr4 Description"));
        attrList.add(new Attribute("Attr5", "String[]", READ_WRITE, "Attr5 Description"));
        attrList.add(new Attribute("Attr6", "java.util.List", READ_WRITE, "Attr6 Description"));
        attrList.add(new Attribute("Attr7", "double", READ_ONLY, "Attr7 Description"));
        attrList.add(new Attribute("Attr8", "char", READ_WRITE, "Attr8 Description"));
        addMBeanAttributes(ndo, jto, attrList);
        ndo.ok();
        
        // Check warning message is displayed
        System.out.println("Check warning message");
        String message = "One or more of the added attribute implementations \n" +
                "already exists in the " + className + " class.\n" +
                interfaceName  + " will be updated with the new operations.\n" +
                className + " will not be updated with the following methods,\n" +
                "because they are already present in the class:\n" +
                " - getAttr0\n" +
                " - isAttr1\n" +
                " - getAttr3\n" +
                " - isAttr4\n" +
                " - getAttr5\n" +
                " - setAttr5\n" +
                " - setAttr6\n";
        ndo = new NbDialogOperator(
                ADD_ATTRIBUTES_DIALOG_TITLE.replaceAll("<INTERFACE>", interfaceName));
        assertEquals(message, getTextAreaOperator( ACTION_INFO_TEXT_AREA, ndo).getText());
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

