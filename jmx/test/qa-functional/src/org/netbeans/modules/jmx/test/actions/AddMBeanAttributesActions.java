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

import java.util.ArrayList;
import javax.swing.JLabel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.modules.jmx.test.helpers.Attribute;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Call menu actions "Add MBean Attributes".
 * This action is activable either when selecting the class node or
 * when clicking directly in the java file editor.
 * Check components and created files.
 */
public class AddMBeanAttributesActions extends ActionsTestCase {
    private static boolean initialized;
    
    /** Need to be defined because of JUnit */
    public AddMBeanAttributesActions(String name) {
        super(name);
        popupPath = ACTION_JMX + "|" + ACTION_ADD_MBEAN_ATTRIBUTES;
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
        
        System.out.println("====================  setup AddMBeanAttributesActions ====================");
        
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
        
        // Update 2
        name = "U2" + ADD_ATTRIBUTES_1_SUPER;
        System.out.println("Create new java class " + name);
        createJavaFile(name, ADD_ATTRIBUTES_1_SUPER);
        
        name = "U2" + ADD_ATTRIBUTES_1_MBEAN;
        System.out.println("Create new java interface " + name);
        createJavaFile(name, ADD_ATTRIBUTES_1_MBEAN);
        
        name = "U2" + ADD_ATTRIBUTES_1;
        System.out.println("Create new java class " + name);
        createJavaFile(name, ADD_ATTRIBUTES_1);
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
        assertFalse(isMenuItemEnabled(jmio));
    }
    
    public void test2() {
        
        System.out.println("====================  test2  ====================");
        
        System.out.println("Check action menu components for " + DYNAMIC_1);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + DYNAMIC_1);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertFalse(isMenuItemEnabled(jmio));
    }
    
    public void test3() {
        
        String className = "U1" + ADD_ATTRIBUTES_1;
        String interfaceName = "U1" + ADD_ATTRIBUTES_1_MBEAN;
        
        System.out.println("====================  test3  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
        addAttributesTest(jmio, className, interfaceName);
    }

    //=========================================================================
    // CALL ADD ATTRIBUTES ACTION FROM EDITOR
    //=========================================================================
    
    public void test4() {
        
        System.out.println("====================  test4  ====================");
        
        System.out.println("Check action menu components for " + SIMPLE_1);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + SIMPLE_1);
        System.out.println("Open java file " + SIMPLE_1);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(SIMPLE_1);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertFalse(isMenuItemEnabled(jmio));
    }
    
    public void test5() {
        
        System.out.println("====================  test5  ====================");
        
        System.out.println("Check action menu components for " + DYNAMIC_1);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + DYNAMIC_1);
        System.out.println("Open java file " + DYNAMIC_1);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(DYNAMIC_1);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertFalse(isMenuItemEnabled(jmio));
    }
    
    public void test6() {
        
        String className = "U2" + ADD_ATTRIBUTES_1;
        String interfaceName = "U2" + ADD_ATTRIBUTES_1_MBEAN;
        
        System.out.println("====================  test6  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
        addAttributesTest(jmio, className, interfaceName);
    }
    
    private void addAttributesTest(
            JMenuItemOperator jmio, 
            String className, 
            String interfaceName) {
        
        ArrayList<Attribute> attrList = null;
        Node node = null;
        
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
        attrList.add(new Attribute("Attr3", "javax.management.ObjectName", READ_WRITE, "Attr3 Description"));
        attrList.add(new Attribute("Attr4", "boolean", READ_WRITE, "Attr4 Description"));
        attrList.add(new Attribute("Attr5", "java.lang.String[]", READ_WRITE, "Attr5 Description"));
        attrList.add(new Attribute("Attr6", "java.util.List", READ_WRITE, "Attr6 Description"));
        attrList.add(new Attribute("Attr7", "double", READ_ONLY, "Attr7 Description"));
        attrList.add(new Attribute("Attr8", "char", READ_WRITE, "Attr8 Description"));
        addMBeanAttributes(ndo, jto, attrList);
        ndo.ok();
        
        // Check warning message is displayed
        System.out.println("Check warning message");
        String message = "One or more of the added attribute implementations already \n" +
                "exists in the " + className + " class.\n" +
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
        node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        EditorOperator eo = new EditorOperator(className);
        eo.save();
        sleep(2000);
        System.out.println("Check updated java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
        
        // Save updated java interface file
        node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + interfaceName);
        eo = new EditorOperator(interfaceName);
        eo.save();
        sleep(2000);
        System.out.println("Check updated java interface file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(interfaceName));
    }
}

