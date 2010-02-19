/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.jmx.test.actions;

import javax.swing.JLabel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.OpenAction;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Call menu actions "Generate MBean Registration".
 * This action is activable only when clicking directly in the java file editor.
 * Check components and created files.
 */
public class GenerateMBeanRegistrationActions extends ActionsTestCase {
    private static boolean initialized;
    
    /** Need to be defined because of JUnit */
    public GenerateMBeanRegistrationActions(String name) {
        super(name);
        popupPath = ACTION_JMX + "|" + ACTION_GENERATE_MBEAN_REGISTRATION;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (!initialized) {
            init();
            initialized = true;
        }
    }
    
    /**
     * Create all needed files for running next tests
     */
    public void init() {
        
        System.out.println("====================  setup GenerateMBeanRegistrationActions  ====================");
        
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
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(SIMPLE_3);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled(jmio));
    }
    
    public void test2() {
        
        String className = "U1" + SIMPLE_4;
        
        System.out.println("====================  test2  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.select("//TODO Add your MBean registration code here\n");
//        eo.setCaretPosition("//TODO Add your MBean registration code here", true);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
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
        sleep(2000);
        System.out.println("Check update java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }
    
    public void test3() {
        
        String className = "U2" + SIMPLE_4;
        
        System.out.println("====================  test3  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.select("//TODO Add your MBean registration code here\n");
//        eo.setCaretPosition("//TODO Add your MBean registration code here", true);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
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
        sleep(2000);
        System.out.println("Check update java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }
    
    public void test4() {
        
        String className = "U3" + SIMPLE_4;
        
        System.out.println("====================  test4  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.select("//TODO Add your MBean registration code here\n");
//        eo.setCaretPosition("//TODO Add your MBean registration code here", true);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
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
        sleep(2000);
        System.out.println("Check update java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }
    
    public void test5() {
        
        String className = "U4" + SIMPLE_4;
        
        System.out.println("====================  test5  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.select("//TODO Add your MBean registration code here\n");
//        eo.setCaretPosition("//TODO Add your MBean registration code here", true);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
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
        sleep(2000);
        System.out.println("Check update java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }
    
    public void test6() {
        
        String className = "U5" + SIMPLE_4;
        
        System.out.println("====================  test6  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.select("//TODO Add your MBean registration code here\n");
//        eo.setCaretPosition("//TODO Add your MBean registration code here", true);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
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
        System.out.println("Set MBean constructor to " + SIMPLE_5 + "(java.lang.String)");
        selectComboBoxItem(CONSTRUCTOR_COMBO_BOX, ndo, SIMPLE_5 + "(java.lang.String)");
        assertTrue(ndo.btOK().isEnabled());
        ndo.ok();
        
        // Save updated java file
        node.select();
        eo = new EditorOperator(className);
        eo.save();
        sleep(2000);
        System.out.println("Check update java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }
    
    public void test7() {
        
        String className = "U6" + SIMPLE_4;
        
        System.out.println("====================  test7  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.select("//TODO Add your MBean registration code here\n");
//        eo.setCaretPosition("//TODO Add your MBean registration code here", true);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
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
        System.out.println("Set MBean constructor to " + DYNAMIC_1 + "(java.lang.String)");
        selectComboBoxItem(CONSTRUCTOR_COMBO_BOX, ndo, DYNAMIC_1 + "(java.lang.String)");
        assertTrue(ndo.btOK().isEnabled());
        ndo.ok();
        
        // Save updated java file
        node.select();
        eo = new EditorOperator(className);
        eo.save();
        sleep(2000);
        System.out.println("Check update java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }
    
    public void test8() {
        
        String className = SIMPLE_4;
        
        System.out.println("====================  test8  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        // The generated code is inserted at the cursor position
        eo.select("//TODO Add your MBean registration code here\n");
//        eo.setCaretPosition("//TODO Add your MBean registration code here", true);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
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

