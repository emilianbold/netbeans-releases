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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.modules.jmx.test.helpers.Notification;
import org.netbeans.modules.jmx.test.helpers.NotificationType;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Call menu actions "Implement NotificationEmitter interface".
 * This action is activable either when selecting the class node or
 * when clicking directly in the java file editor.
 * Check components and created files.
 */
public class ImplementNotificationEmitterActions extends ActionsTestCase {
    private static boolean initialized;

    /** Need to be defined because of JUnit */
    public ImplementNotificationEmitterActions(String name) {
        super(name);
        popupPath = ACTION_JMX + "|" + ACTION_IMPLEMENT_NOTIFICATION_EMITTER;
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

        System.out.println("====================  setup ImplementNotificationEmitterActions  ====================");

        System.out.println("Create new java class " + SIMPLE_1);
        createJavaFile(SIMPLE_1);

        System.out.println("Create new java class " + DYNAMIC_3);
        createJavaFile(DYNAMIC_3);

        System.out.println("Create new java class " + USER_NOTIFICATION);
        createJavaFile(USER_NOTIFICATION);

        String name = null;

        // Create a java file that is modified by the test
        // Add a prefix to this file in order to compare it with
        // the expected modified golden file instead of the original one
        // Update 1
        name = "U1" + SIMPLE_2_MBEAN;
        System.out.println("Create new java interface " + name);
        createJavaFile(name, SIMPLE_2_MBEAN);

        name = "U1" + SIMPLE_2;
        System.out.println("Create new java class " + name);
        createJavaFile(name, SIMPLE_2);

        // Create a java file that is modified by the test
        // Add a prefix to this file in order to compare it with
        // the expected modified golden file instead of the original one
        // Update 1
        name = "U1" + STANDARD_1_MBEAN;
        System.out.println("Create new java interface " + name);
        createJavaFile(name, STANDARD_1_MBEAN);

        name = "U1" + STANDARD_1;
        System.out.println("Create new java class " + name);
        createJavaFile(name, STANDARD_1);

        // Create a java file that is modified by the test
        // Add a prefix to this file in order to compare it with
        // the expected modified golden file instead of the original one
        // Update 1
        name = "U1" + DYNAMIC_4_SUPPORT;
        System.out.println("Create new java class " + name);
        createJavaFile(name, DYNAMIC_4_SUPPORT);

        name = "U1" + DYNAMIC_4;
        System.out.println("Create new java class " + name);
        createJavaFile(name, DYNAMIC_4);
    }

    //=========================================================================
    // CALL IMPLEMENT NOTIFICATION EMITTER ACTION FROM NODE
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

        System.out.println("Check action menu components for " + DYNAMIC_3);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" + 
                SOURCE_PACKAGES + "|" + packageName + "|" + DYNAMIC_3);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertFalse(isMenuItemEnabled(jmio));
    }

    public void test3() {

        String className = "U1" + SIMPLE_2;

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
        NbDialogOperator ndo = new NbDialogOperator(IMPLEMENT_NOTIFICATION_EMITTER_DIALOG_TITLE);
        ndo.ok();

        // Save updated java file
        node.select();
        EditorOperator eo = new EditorOperator(className);
        eo.save();
        sleep(2000);
        System.out.println("Check update java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }

    public void test4() {

        String className = "U1" + STANDARD_1;
        ArrayList<Notification> notifList = null;

        System.out.println("====================  test4  ====================");

        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" + 
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));

        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(IMPLEMENT_NOTIFICATION_EMITTER_DIALOG_TITLE);
        assertTrue(ndo.btOK().isEnabled());

        System.out.println("Enable delegation to broadcaster");
        setCheckBoxSelection(GENERATE_DELEGATION_CHECK_BOX, ndo, true);

        JTableOperator jto = getTableOperator(NOTIFICATION_TABLE, ndo);

        // Add new notifications
        System.out.println("Add notifications");
        notifList = new ArrayList<Notification>();
        notifList.add(new Notification("javax.management.AttributeChangeNotification", 
                "notif0 Description", null));
        addMBeanNotifications(ndo, jto, notifList);
        ndo.ok();

        // Save updated java class file
        node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" + 
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        EditorOperator eo = new EditorOperator(className);
        eo.save();
        sleep(2000);
        System.out.println("Check updated java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }


    //=========================================================================
    // CALL ADD ATTRIBUTES ACTION FROM EDITOR
    //=========================================================================
    public void test5() {

        System.out.println("====================  test5  ====================");

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

    public void test6() {

        System.out.println("====================  test6  ====================");

        System.out.println("Check action menu components for " + DYNAMIC_3);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" + 
                SOURCE_PACKAGES + "|" + packageName + "|" + DYNAMIC_3);
        System.out.println("Open java file " + DYNAMIC_3);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(DYNAMIC_3);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertFalse(isMenuItemEnabled(jmio));
    }

    public void test7() {

        String className = "U1" + DYNAMIC_4;
        ArrayList<Notification> notifList = null;
        ArrayList<NotificationType> types = null;

        System.out.println("====================  test7  ====================");

        System.out.println("Check action menu components for " + className);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" + 
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        System.out.println("Open java file " + className);
        new OpenAction().perform(node);
        // Check menu item
        EditorOperator eo = new EditorOperator(className);
        JMenuItemOperator jmio = showMenuItem(eo, popupPath);
        assertTrue(isMenuItemEnabled2(jmio));

        // Call menu item
        System.out.println("Call action menu " + popupPath);
        jmio.push();
        NbDialogOperator ndo = new NbDialogOperator(IMPLEMENT_NOTIFICATION_EMITTER_DIALOG_TITLE);
        assertTrue(ndo.btOK().isEnabled());

        System.out.println("Enable delegation to broadcaster");
        setCheckBoxSelection(GENERATE_DELEGATION_CHECK_BOX, ndo, true);
        System.out.println("Enable private sequence number and accessor generation");
        setCheckBoxSelection(GENERATE_SEQUENCE_NUM_CHECK_BOX, ndo, true);

        JTableOperator jto = getTableOperator(NOTIFICATION_TABLE, ndo);

        // Add new notifications
        System.out.println("Add notifications");
        notifList = new ArrayList<Notification>();
        // Notification 0
        notifList.add(new Notification("javax.management.AttributeChangeNotification", "notif0 Description", null));
        // Notification 1
        types = new ArrayList<NotificationType>();
        types.add(new NotificationType(packageName + ".notif1"));
        notifList.add(new Notification("javax.management.Notification", "notif1 Description", types));
        // Notification 2
        types = new ArrayList<NotificationType>();
        types.add(new NotificationType(packageName + ".notif2.type1"));
        types.add(new NotificationType(packageName + ".notif2.type2"));
        notifList.add(new Notification("javax.management.Notification", "notif2 Description", types));
        // Notification 3
        notifList.add(new Notification(packageName + "." + USER_NOTIFICATION, "notif3 Description", null));
        addMBeanNotifications(ndo, jto, notifList);
        ndo.ok();

        // Save updated java class file
        node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" + 
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        eo = new EditorOperator(className);
        eo.save();
        sleep(2000);
        System.out.println("Check updated java class file");
        checkUpdatedFiles(eo, node.getPath(), getGoldenFile(className));
    }
}
