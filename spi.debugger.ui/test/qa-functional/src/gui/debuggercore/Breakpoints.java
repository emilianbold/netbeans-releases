/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * The Original Code is NetBeans.
 * The Initial Developer of the Original Code is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

package gui.debuggercore;

import java.io.File;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JTable;

import junit.textui.TestRunner;

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import org.netbeans.junit.NbTestSuite;

import org.openide.nodes.PropertySupport.Reflection;

public class Breakpoints extends JellyTestCase {
    
    private String  sampleDir = System.getProperty("netbeans.user") + File.separator + "sampledir";
    
    private static String newBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_AddBreakpoint");
    private static String startSessionItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Debug/StartSession");
    private static String runInDebuggerItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Start_action_name");
    private static String finishSessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Finish_action_name");
    private static String newBreakpointTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Breakpoint_Title");
    
    private static String debuggerItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Debug");
    private static String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window");
    
    private static String editAction = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");
    
    private static String debuggerFinishedMsg = Bundle.getStringTrimmed("org.netbeans.modules.debugger.multisession.Bundle", "CTL_Debugger_end");
    private static String debuggerFinishedTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.multisession.Bundle", "CTL_Finish_debugging_dialog");
    
    public Breakpoints(String name) {
        super(name);
    }
    
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Breakpoints("testLineBreakpoint"));
        suite.addTest(new Breakpoints("testMethodBreakpoint"));
        suite.addTest(new Breakpoints("testClassBreakpoint"));
        suite.addTest(new Breakpoints("testVariableBreakpoint"));
        suite.addTest(new Breakpoints("testThreadBreakpoint"));
        suite.addTest(new Breakpoints("testExceptionBreakpoint"));
        suite.addTest(new Breakpoints("testConditionalLineBreakpoint"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** setUp method  */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }
    
    /** tearDown method */
    public void tearDown() {
        Utilities.deleteAllBreakpoints();
        Utilities.closeZombieSessions();
        Utilities.closeTerms();
        new EventTool().waitNoEvent(1000);
    }
    
    /**
     *
     */
    public void testLineBreakpoint() {
        
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock(editAction);
        
        EditorOperator editorOperator = new EditorOperator("DebuggerTestApplication"); // NOI18N
        new EventTool().waitNoEvent(1000); // XXX
        editorOperator.setCaretPosition(60, 1);
        
        new ActionNoBlock(debuggerItem + "|" + newBreakpointItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator(newBreakpointTitle);
        new JComboBoxOperator(dialog, 1).selectItem("Line");
        new JTextFieldOperator(dialog, 0).typeText(""); // NOI18N
        new JTextFieldOperator(dialog, 1).typeText("DebuggerTestApplication"); // NOI18N
        new JTextFieldOperator(dialog, 2).typeText("60"); // NOI18N
        dialog.ok();
        
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Line DebuggerTestApplication.java:60".equals(jTableOperator.getValueAt(rowNumber, 0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Line breakpoint was not created", found); // NOI18N
        breakpointsOper.close();

        // test if debugger stops at an assumed breakpoint line
        
        new Action(windowItem + "|Filesystems", null).perform();
        javaNode.select();
        new Action(debuggerItem + "|" + startSessionItem + "|" + runInDebuggerItem, null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Breakpoint reached at line 60 in class DebuggerTestApplication by thread main.");
        new ActionNoBlock(debuggerItem + "|" + finishSessionsItem, null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            dialog = new NbDialogOperator(debuggerFinishedTitle);
            dialog.ok();
        } catch (TimeoutExpiredException tee) {
            System.out.println("Dialog - Finish Debugging Session was not displayed.");
            throw(tee);
        }
        mwo.waitStatusText(debuggerFinishedMsg);
        
    }
    
    /**
     *
     */
    public void testMethodBreakpoint() {
        
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock(editAction);
        
        EditorOperator editorOperator = new EditorOperator("DebuggerTestApplication"); // NOI18N
        new EventTool().waitNoEvent(1000); // XXX
        editorOperator.setCaretPosition(60,1);
        
        new ActionNoBlock(debuggerItem + "|" + newBreakpointItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator(newBreakpointTitle);
        new JComboBoxOperator(dialog, 1).selectItem("Method");
        new JTextFieldOperator(dialog, 0).typeText(""); // NOI18N
        new JTextFieldOperator(dialog, 1).typeText("DebuggerTestApplication"); // NOI18N
        new JTextFieldOperator(dialog, 2).typeText("updateCounter"); // NOI18N
        dialog.ok();
        
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Method DebuggerTestApplication.updateCounter".equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Method breakpoint was not created.", found); // NOI18N
        breakpointsOper.close();
        
        // test if debugger stops at an assumed breakpoint method
        
        new Action(windowItem + "|Filesystems", null).perform();
        javaNode.select();
        new Action(debuggerItem + "|" + startSessionItem + "|" + runInDebuggerItem, null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Method updateCounter reached at line 118 in class DebuggerTestApplication by thread counterThread.");
        new ActionNoBlock(debuggerItem + "|" + finishSessionsItem, null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            dialog = new NbDialogOperator(debuggerFinishedTitle);
            dialog.ok();
        } catch (TimeoutExpiredException tee) {
            System.out.println("Dialog - Finish Debugging Session was not displayed."); // NOI18N
            throw(tee);
        }
        mwo.waitStatusText(debuggerFinishedMsg);
        
    }
    
    /**
     *
     */
    public void testClassBreakpoint() {
    
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock(editAction);
        
        EditorOperator editorOperator = new EditorOperator("DebuggerTestApplication"); // NOI18N
        new EventTool().waitNoEvent(1000); // XXX
        editorOperator.setCaretPosition(60,1);
        
        new ActionNoBlock(debuggerItem + "|" + newBreakpointItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator(newBreakpointTitle);
        new JComboBoxOperator(dialog, 1).selectItem("Class");
        new JTextFieldOperator(dialog, 0).typeText(""); // NOI18N
        new JTextFieldOperator(dialog, 1).typeText("DebuggerTestApplication"); // NOI18N
        new JComboBoxOperator(dialog, 4).selectItem("Class Prepare or Unload");
        dialog.ok();
        
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Class DebuggerTestApplication prepare / unload".equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Class breakpoint was not created.", found);
        breakpointsOper.close();
        
        // test if debugger stops at an assumed breakpoint class prepared
        // it's not possible to test it, because the notification "Class examples.colorpicker.ColorPicker prepared"
        // is not displayed at a status bar
        // test if debugger stops at an assumed breakpoint method
        
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        new Action(windowItem + "|Filesystems", null).perform();
        javaNode.select();
        new Action(debuggerItem + "|" + startSessionItem + "|" + runInDebuggerItem, null).perform();
        mwo.waitStatusText("Class DebuggerTestApplication prepared");
        new ActionNoBlock(debuggerItem + "|" + finishSessionsItem, null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            dialog = new NbDialogOperator(debuggerFinishedTitle);
            dialog.ok();
        } catch (TimeoutExpiredException tee) {
            System.out.println("Dialog - Finish Debugging Session was not displayed.");
            throw(tee);
        }
        mwo.waitStatusText(debuggerFinishedMsg);
        
    }
    
    /**
     *
     */
    public void testVariableBreakpoint() {
        
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock(editAction);

        EditorOperator editorOperator = new EditorOperator("DebuggerTestApplication"); // NOI18N
        new EventTool().waitNoEvent(1000); // XXX
        editorOperator.setCaretPosition(35,1);

        new ActionNoBlock(debuggerItem + "|" + newBreakpointItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator("New Breakpoint");
        new JComboBoxOperator(dialog, 1).selectItem("Variable");
        new JTextFieldOperator(dialog, 0).typeText("");
        new JTextFieldOperator(dialog, 1).typeText("DebuggerTestApplication");
        new JTextFieldOperator(dialog, 2).typeText("counter");
        new JComboBoxOperator(dialog, 5).selectItem("Variable Access");
        dialog.ok();

        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Variable DebuggerTestApplication.counter access".equals(jTableOperator.getValueAt(rowNumber, 0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Variable breakpoint was not created.",found);
        breakpointsOper.close();
        
        // test if debugger stops at an assumed breakpoint variable
        
        new Action(windowItem + "|Filesystems", null).perform();
        javaNode.select();
        new Action(debuggerItem + "|" + startSessionItem + "|" + runInDebuggerItem, null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Variable counter access (0).");
        new ActionNoBlock(debuggerItem + "|" + finishSessionsItem, null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout",5000);
            dialog = new NbDialogOperator(debuggerFinishedTitle);
            dialog.ok();
        } catch (TimeoutExpiredException tee) {
            System.out.println("Dialog - Finish Debugging Session was not displayed.");
            throw(tee);
        }
        mwo.waitStatusText(debuggerFinishedMsg);
        
    }
    
    /**
     *
     */
    public void testThreadBreakpoint() {
   
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock(editAction);
        
        EditorOperator editorOperator = new EditorOperator("DebuggerTestApplication"); // NOI18N
        new EventTool().waitNoEvent(1000); // XXX
        editorOperator.setCaretPosition(60,1);

        new ActionNoBlock(debuggerItem + "|" + newBreakpointItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator("New Breakpoint");
        new JComboBoxOperator(dialog, 1).selectItem("Thread");
        new JComboBoxOperator(dialog, 2).selectItem("Thread Start or Death");
        dialog.ok();
                
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        String value;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Thread start / death".equals(jTableOperator.getValueAt(rowNumber, 0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Thread breakpoint was not created.", found);
        breakpointsOper.close();
        
        // test if debugger stops at an assumed breakpoint variable
        
        new Action(windowItem + "|Filesystems", null).perform();
        javaNode.select();
        new Action(debuggerItem + "|" + startSessionItem + "|" + runInDebuggerItem, null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        //mwo.waitStatusText("Debugger running"); ???
        
        Utilities.showDebuggerView(Utilities.sessionsViewTitle);
        TopComponentOperator sessionsOper = new TopComponentOperator(Utilities.sessionsViewTitle);
        jTableOperator = new JTableOperator(sessionsOper);
        assertTrue("There isn't an expected text in the Session view!",
            "DebuggerTestApplication".equals(jTableOperator.getValueAt(0, 0).toString()) );
        
        new EventTool().waitNoEvent(2000); // probably need more time to finish task
        mwo.waitStatusText("Thread Signal Dispatcher started.");

        String contentOfDebuggerConsole = new TermOperator("Debugger Console").getText();                
        
        assertTrue("The text \"Thread Signal Dispatcher started.\" not in the Debugger Console!", 
            (contentOfDebuggerConsole.indexOf("Thread Signal Dispatcher started.") != -1) );
        new ActionNoBlock(debuggerItem + "|" + finishSessionsItem, null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            dialog = new NbDialogOperator(debuggerFinishedTitle);
            dialog.ok();
        } catch (TimeoutExpiredException tee) {
            System.out.println("Dialog - Finish Debugging Session was not displayed.");
            throw(tee);
        }
        mwo.waitStatusText(debuggerFinishedMsg);
        
    }
    
    /**
     *
     */
    public void testExceptionBreakpoint() {
        
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock(editAction);
                
        EditorOperator editorOperator = new EditorOperator("DebuggerTestApplication"); // NOI18N
        editorOperator.setCaretPosition(35,1);

        new ActionNoBlock(debuggerItem + "|" + newBreakpointItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator(newBreakpointTitle);
        new JComboBoxOperator(dialog, 1).selectItem("Exception");
        new JTextFieldOperator(dialog, 0).typeText("java.lang"); // NOI18N
        new JTextFieldOperator(dialog, 1).typeText("Exception"); // NOI18N
        new JComboBoxOperator(dialog, 4).selectItem("Exception Caught or Uncaught"); 
        dialog.ok();
                
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Exception java.lang.Exception".equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Exception breakpoint was not created", found);
        breakpointsOper.close();
        
    }
    
    /**
     *
     */
    public void testConditionalLineBreakpoint() {
        
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock(editAction);
        
        EditorOperator editorOperator = new EditorOperator("DebuggerTestApplication"); // NOI18N
        new EventTool().waitNoEvent(1000); // XXX
        editorOperator.setCaretPosition(120,1);

        new ActionNoBlock(debuggerItem + "|" + newBreakpointItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator(newBreakpointTitle);
        new JComboBoxOperator(dialog, 1).selectItem("Line");
        new JTextFieldOperator(dialog, 0).typeText(""); // NOI18N
        new JTextFieldOperator(dialog, 1).typeText("DebuggerTestApplication"); // NOI18N
        new JTextFieldOperator(dialog, 2).typeText("120"); // NOI18N
        new JTextFieldOperator(dialog, 3).typeText("counter==5"); // NOI18N
        dialog.ok();
        
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Line DebuggerTestApplication.java:120".equals(jTableOperator.getValueAt(rowNumber, 0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Line breakpoint was not created.", found);
        breakpointsOper.close();
        
        // test if debugger stops at an assumed breakpoint line
        
        new Action(windowItem + "|Filesystems", null).perform();
        javaNode.select();
        new Action(debuggerItem + "|" + startSessionItem + "|" + runInDebuggerItem, null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Breakpoint reached at line 120 in class DebuggerTestApplication by thread counterThread.");
        
        new EventTool().waitNoEvent(2000); // XXX
        
        Utilities.showDebuggerView(Utilities.localVarsViewTitle);
        TopComponentOperator localVarsOper = new TopComponentOperator(Utilities.localVarsViewTitle);
        jTableOperator = new JTableOperator(localVarsOper);
        TreeTableOperator treeTableOperator = new TreeTableOperator((JTable) jTableOperator.getSource());
        
        new Node(treeTableOperator.tree(), "this").expand();
        String [] expectedLocalVariablesColumn_0 = {"counter"};
        String [] expectedLocalVariablesColumn_1 = {"5"};
        rowNumber = 0;
        
        try {
            for (int i = 0; i < expectedLocalVariablesColumn_0.length; i++) {
                rowNumber = -1;
                found = false;
                while ((++rowNumber < jTableOperator.getRowCount()) && (!found)) {
//                    System.out.println(rowNumber + " XXX" + jTableOperator.getValueAt(rowNumber,0).toString()+"\t"+((org.openide.nodes.PropertySupport.Reflection)jTableOperator.getValueAt(rowNumber,1)).getValue().toString());
                    if ((expectedLocalVariablesColumn_0[i].equals(jTableOperator.getValueAt(rowNumber,0).toString())))  {
                        if (expectedLocalVariablesColumn_1[i].equals(((Reflection)jTableOperator.getValueAt(rowNumber,1)).getValue().toString())) {
                            found = true;                     
                        } else {
                            assertTrue("A - Expected Variable " + expectedLocalVariablesColumn_0[i] + " (" + 
                                expectedLocalVariablesColumn_1[i]+") not found - < " + 
                                ((Reflection)jTableOperator.getValueAt(rowNumber,1)).getValue().toString() + " >", found);                        
                        }
                    };
                }
                assertTrue("B - Expected Variable "+expectedLocalVariablesColumn_0[i] + " (" + 
                    expectedLocalVariablesColumn_1[i]+") not found\t", found);
            }
        } catch (java.lang.IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (java.lang.IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
        }
        
        new ActionNoBlock(debuggerItem + "|" + finishSessionsItem, null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            dialog = new NbDialogOperator(debuggerFinishedTitle);
            dialog.ok();
        } catch (TimeoutExpiredException tee) {
            System.out.println("Dialog - Finish Debugging Session was not displayed.");
            throw(tee);
        }
        mwo.waitStatusText(debuggerFinishedMsg);
        
    }
        
}
