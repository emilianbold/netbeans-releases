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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Call menu actions "Implement MBeanRegistration interface".
 * Check components and created files.
 */
public class ImplementMBeanRegistrationActions extends ActionsTestCase {
    
    /** Need to be defined because of JUnit */
    public ImplementMBeanRegistrationActions(String name) {
        super(name);
        popupPath = ACTION_JMX + "|" + ACTION_IMPLEMENT_MBEAN_REGISTRATION;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ImplementMBeanRegistrationActions("init"));
        suite.addTest(new ImplementMBeanRegistrationActions("test1"));
        suite.addTest(new ImplementMBeanRegistrationActions("test2"));
        suite.addTest(new ImplementMBeanRegistrationActions("test3"));
        suite.addTest(new ImplementMBeanRegistrationActions("test4"));
        return suite;
    }
    
    /**
     * Create all needed files for running next tests
     */
    public void init() {
        
        System.out.println("====================  init  ====================");
        
        System.out.println("Create new java class " + SIMPLE_1);
        createJavaFile(SIMPLE_1);
        
        System.out.println("Create new java class " + DYNAMIC_2);
        createJavaFile(DYNAMIC_2);
        
        String name = null;
        
        // Create a java file that is modified by the test
        // Add a prefix to this file in order to compare it with
        // the expected modified golden file instead of the original one
        // Update 1
        name = "U1" + DYNAMIC_1;
        System.out.println("Create new java classes " + name);
        createJavaFile(name, DYNAMIC_1);
        
        // Create a java file that is modified by the test
        // Add a prefix to this file in order to compare it with
        // the expected modified golden file instead of the original one
        // Update 2
        name = "U2" + DYNAMIC_1;
        System.out.println("Create new java classes " + name);
        createJavaFile(name, DYNAMIC_1);
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
        
        System.out.println("Check action menu components for " + DYNAMIC_2);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + DYNAMIC_2);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertFalse(jmio.isEnabled());
    }
    
    public void test3() throws Exception {
        
        String className = "U1" + DYNAMIC_1;
        
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
                IMPLEMENT_MBEAN_REGISTRATION_DIALOG_TITLE);
        ndo.ok();
        
        // Save updated java file
        node.select();
        EditorOperator eo = new EditorOperator(className);
        eo.save();
        System.out.println("Check update java class file");
        String content = getFileContent(getGoldenFile(className));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
    }
    
    public void test4() {
        
        String className = "U2" + DYNAMIC_1;
        
        System.out.println("====================  test4  ====================");
        
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
                IMPLEMENT_MBEAN_REGISTRATION_DIALOG_TITLE);
        System.out.println("Select generate private fiels");
        setCheckBoxSelection(GENERATE_PRIVATE_FIELDS_CHECK_BOX, ndo, true);
        ndo.ok();
        
        // Save updated java file
        node.select();
        EditorOperator eo = new EditorOperator(className);
        eo.save();
        String content = getFileContent(getGoldenFile(className));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
    }
}

