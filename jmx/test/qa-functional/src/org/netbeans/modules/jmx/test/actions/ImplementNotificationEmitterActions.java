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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.modules.jmx.test.helpers.Notification;
import org.netbeans.modules.jmx.test.helpers.NotificationType;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Call menu actions "Implement NotificationEmitter interface".
 * Check components and created files.
 */
public class ImplementNotificationEmitterActions extends ActionsTestCase {
    
    /** Need to be defined because of JUnit */
    public ImplementNotificationEmitterActions(String name) {
        super(name);
        popupPath = ACTION_JMX + "|" + ACTION_IMPLEMENT_NOTIFICATION_EMITTER;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        //        suite.addTest(new ImplementNotificationEmitterActions("init"));
        //        suite.addTest(new ImplementNotificationEmitterActions("test1"));
        //        suite.addTest(new ImplementNotificationEmitterActions("test2"));
        //        suite.addTest(new ImplementNotificationEmitterActions("test3"));
        //        suite.addTest(new ImplementNotificationEmitterActions("test4"));
        suite.addTest(new ImplementNotificationEmitterActions("test5"));
        return suite;
    }
    
    
    /**
     * Create all needed files for running next tests
     */
    public void init() {
        
        System.out.println("====================  init  ====================");
        
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
        
        System.out.println("Check action menu components for " + DYNAMIC_3);
        Node node = selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + DYNAMIC_3);
        // Check menu item
        JMenuItemOperator jmio = showMenuItem(node, popupPath);
        assertFalse(jmio.isEnabled());
    }
    
    public void test3() {
        
        String className = "U1" + SIMPLE_2;
        
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
                IMPLEMENT_NOTIFICATION_EMITTER_DIALOG_TITLE);
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
        
        String className = "U1" + STANDARD_1;
        ArrayList<Notification> notifList = null;
        
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
                IMPLEMENT_NOTIFICATION_EMITTER_DIALOG_TITLE);
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
        selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" +
                SOURCE_PACKAGES + "|" + packageName + "|" + className);
        EditorOperator eo = new EditorOperator(className);
        eo.save();
        System.out.println("Check updated java class file");
        String content = getFileContent(getGoldenFile(className));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        assertTrue(compareFileContents(eo.getText(), content));
    }
    
    public void test5() {
        
        String className = "U1" + DYNAMIC_4;
        ArrayList<Notification> notifList = null;
        ArrayList<NotificationType> types = null;
        
        System.out.println("====================  test5  ====================");
        
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
                IMPLEMENT_NOTIFICATION_EMITTER_DIALOG_TITLE);
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
        notifList.add(new Notification("javax.management.AttributeChangeNotification",
                "notif0 Description", null));
        // Notification 1
        types = new ArrayList<NotificationType>();
        types.add(new NotificationType("com.foo.bar.notif1"));
        notifList.add(new Notification("javax.management.Notification",
                "notif1 Description", types));
        // Notification 2
        types = new ArrayList<NotificationType>();
        types.add(new NotificationType("com.foo.bar.notif2.type1"));
        types.add(new NotificationType("com.foo.bar.notif2.type2"));
        notifList.add(new Notification("javax.management.Notification",
                "notif2 Description", types));
        // Notification 3
        notifList.add(new Notification(packageName + "." + USER_NOTIFICATION,
                "notif3 Description", null));
        addMBeanNotifications(ndo, jto, notifList);
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
    }
}

