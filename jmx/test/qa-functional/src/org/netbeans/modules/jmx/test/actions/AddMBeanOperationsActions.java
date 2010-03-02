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
import java.util.Properties;
import javax.swing.JLabel;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.OpenAction;
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
    private static boolean initialized;
    
    /** Need to be defined because of JUnit */
    public AddMBeanOperationsActions(String name) {
        super(name);
        popupPath = ACTION_JMX + "|" + ACTION_ADD_MBEAN_OPERATIONS;
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
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
        System.out.println("====================  setup AddMBeanOperationsActions  ====================");
        
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
        
        String className = "U1" + ADD_OPERATIONS_1;
        String interfaceName = "U1" + ADD_OPERATIONS_1_MBEAN;
        
        System.out.println("====================  test3  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
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
        
        String className = "U2" + ADD_OPERATIONS_1;
        String interfaceName = "U2" + ADD_OPERATIONS_1_MBEAN;
        
        System.out.println("====================  test6  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        // Check menu item
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
        addOperationsTest(jmio, className, interfaceName);
    }
    
    private void addOperationsTest(
            JMenuItemOperator jmio, 
            String className, 
            String interfaceName) {
        
        ArrayList<Operation> opList = null;
        ArrayList<Parameter> parameters = null;
        ArrayList<Exception> exceptions = null;
        Node node = null;
        
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
        exceptions.add(new Exception("java.lang.IllegalStateException", ""));
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
        parameters.add(new Parameter("s", "java.lang.String[]", "s Description"));
        exceptions = new ArrayList<Exception>();
        exceptions.add(new Exception(packageName + "." + USER_EXCEPTION, 
                "UserException Description"));
        opList.add(new Operation("op1", "int", parameters, exceptions, "op1 Description"));
        // Operation 2
        parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("l", "java.util.List", "l Description"));
        parameters.add(new Parameter("d", "java.util.Date", "d Description"));
        opList.add(new Operation("op2", "Boolean", parameters, null, "op2 Description"));
        // Operation 3
        parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter("s", "String", "s Description"));
        parameters.add(new Parameter("f", "float", "f Description"));
        exceptions = new ArrayList<Exception>();
        exceptions.add(new Exception(packageName + "." + USER_EXCEPTION, 
                "UserException Description"));
        exceptions.add(new Exception("java.lang.IllegalStateException", "IllegalStateException Description"));
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
                " - op1(java.lang.String[])\n" +
                " - op2(java.util.List,java.util.Date)\n";
        ndo = new NbDialogOperator(
                ADD_OPERATIONS_DIALOG_TITLE.replaceAll("<INTERFACE>", interfaceName));
        assertEquals(message, getTextAreaOperator(ACTION_INFO_TEXT_AREA, ndo).getText());
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

