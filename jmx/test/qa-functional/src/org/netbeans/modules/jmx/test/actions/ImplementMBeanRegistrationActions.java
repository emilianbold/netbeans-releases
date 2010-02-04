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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Call menu actions "Implement MBeanRegistration interface".
 * This action is activable either when selecting the class node or
 * when clicking directly in the java file editor.
 * Check components and created files.
 */
public class ImplementMBeanRegistrationActions extends ActionsTestCase {
    private static boolean initialized;
    
    /** Need to be defined because of JUnit */
    public ImplementMBeanRegistrationActions(String name) {
        super(name);
        popupPath = ACTION_JMX + "|" + ACTION_IMPLEMENT_MBEAN_REGISTRATION;
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

        System.out.println("====================  setup ImplementMBeanRegistrationActions  ====================");
        
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
    
    //=========================================================================
    // CALL IMPLEMENT MBEAN REGISTRATION ACTION FROM NODE
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
        
        System.out.println("Check action menu components for " + DYNAMIC_2);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + DYNAMIC_2);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertFalse(isMenuItemEnabled(jmio));
    }
    
    public void test3() throws Exception {
        
        String className = "U1" + DYNAMIC_1;
        
        System.out.println("====================  test3  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(
                IMPLEMENT_MBEAN_REGISTRATION_DIALOG_TITLE);
        System.out.println("Do not generate private fiels");
        ndo.ok();
        
        // Save updated java file
        node.select();
        EditorOperator eo = new EditorOperator(className);
        eo.save();
        sleep(2000);
        System.out.println("Check update java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }
    
    //=========================================================================
    // CALL IMPLEMENT MBEAN REGISTRATION ACTION FROM EDITOR
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
        
        System.out.println("Check action menu components for " + DYNAMIC_2);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + DYNAMIC_2);
        System.out.println("Open java file " + DYNAMIC_2);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(DYNAMIC_2);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertFalse(isMenuItemEnabled(jmio));
    }
    
    public void test6() {
        
        String className = "U2" + DYNAMIC_1;
        
        System.out.println("====================  test6  ====================");
        
        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));
        
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
        sleep(2000);
        System.out.println("Check update java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }
}

