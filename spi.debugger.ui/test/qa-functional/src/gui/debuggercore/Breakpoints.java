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

import java.awt.event.KeyEvent;

import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;

public class Breakpoints extends JellyTestCase {
    
    public Breakpoints(String name) {
        super(name);
    }
    
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Breakpoints("testLineBreakpoint"));
        suite.addTest(new Breakpoints("testConditionalLineBreakpoint"));
        suite.addTest(new Breakpoints("testMethodBreakpoint"));
        suite.addTest(new Breakpoints("testClassBreakpoint"));
        suite.addTest(new Breakpoints("testVariableAccessBreakpoint"));
        suite.addTest(new Breakpoints("testVariableModificationBreakpoint"));
//        suite.addTest(new Breakpoints("testThreadBreakpoint"));
        suite.addTest(new Breakpoints("testExceptionBreakpoint"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** setUp method  */
    public void setUp() {
        Utilities.sleep(1000);
        System.out.println("########  " + getName() + "  #######");
    }
    
    /** tearDown method */
    public void tearDown() {
        Utilities.deleteAllBreakpoints();
        Utilities.closeZombieSessions();
    }

    public void testLineBreakpoint() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        Node projectNode = new Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);

        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        editorOperator.setCaretPosition(52, 1);
        Utilities.sleep(2000);
        
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.toggleBreakpointItem).toString(), null).perform();
        new Action(null, null, Utilities.toggleBreakpointShortcut).performShortcut();
        
        // test if breakpoint exists in breakpoints view
        Utilities.sleep(1000);
        Utilities.showBreakpointsView();
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Line MemoryView.java:52".equals(jTableOperator.getValueAt(rowNumber, 0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Line breakpoint was not created.", found);

        // test if debugger stops at an assumed breakpoint line
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        new Action(null, null, Utilities.debugProjectShortcut).performShortcut();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        Utilities.sleep(1000);
        mwo.waitStatusText("Thread main stopped at MemoryView.java:52.");

        // test if debugger stops at an assumed breakpoint line after continue
        editorOperator.setCaretPosition(103, 1);
        Utilities.sleep(2000);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.toggleBreakpointItem).toString(), null).perform();
        new Action(null, null, Utilities.toggleBreakpointShortcut).performShortcut();
        Utilities.sleep(1000);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        Utilities.sleep(1000);
        mwo.waitStatusText("Thread main stopped at MemoryView.java:103.");
        
        // finnish bedugging session
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText(Utilities.finishedStatusBarText);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session was not killed.");
            throw(tee);
        }
    }
    
    public void testConditionalLineBreakpoint() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        Node projectNode = new Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);

        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        editorOperator.setCaretPosition(103, 40);
        Utilities.sleep(2000);
        
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.toggleBreakpointItem).toString(), null).perform();
        new Action(null, null, Utilities.toggleBreakpointShortcut).performShortcut();
        
        // test if breakpoint exists in breakpoints view
        Utilities.sleep(1000);
        Utilities.showBreakpointsView();
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Line MemoryView.java:103".equals(jTableOperator.getValueAt(rowNumber, 0).toString()) ) {
                found = true;
                new JPopupMenuOperator(jTableOperator.callPopupOnCell(rowNumber, 1)).pushMenuNoBlock("Customize");
                Utilities.sleep(1000);
                NbDialogOperator dialog = new NbDialogOperator("Customize Breakpoint");
                new JTextFieldOperator(dialog, 0).setText("taken > 5000");
                dialog.ok();
                Utilities.sleep(1000);
            };
            rowNumber++;
        }
        assertTrue("Line breakpoint was not created.", found);

        // test if debugger stops at an assumed breakpoint line
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        new Action(null, null, Utilities.debugProjectShortcut).performShortcut();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        Utilities.sleep(1000);
        mwo.waitStatusText("Thread main stopped at MemoryView.java:103.");
        
        // finish bedugging session
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText(Utilities.finishedStatusBarText);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session was not killed.");
            throw(tee);
        }
    }

    public void testMethodBreakpoint() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        Node projectNode = new Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);

        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        
        // create new breakpoint and check pre-filled values
        editorOperator.setCaretPosition(90, 5);
        Utilities.sleep(2000);
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        Utilities.sleep(1000);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Method");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 1).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 2).getText()));
        assertTrue("Method Name was not set to correct value.", "updateStatus".equals(new JTextFieldOperator(dialog, 3).getText()));
        dialog.cancel();

        editorOperator.setCaretPosition(107, 1);
        Utilities.sleep(2000);
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        Utilities.sleep(1000);
        dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Method");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 1).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 2).getText()));
        assertTrue("Method Name was not set to correct value.", "updateStatus".equals(new JTextFieldOperator(dialog, 3).getText()));
        dialog.ok();
        
        // test if breakpoint exists in breakpoints view
        Utilities.sleep(1000);
        Utilities.showBreakpointsView();
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Method MemoryView.updateStatus".equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Method breakpoint was not created.", found);

        // test if debugger stops at an assumed breakpoint line
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        new Action(null, null, Utilities.debugProjectShortcut).performShortcut();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        Utilities.sleep(1000);
        mwo.waitStatusText("Thread main stopped at MemoryView.java:91.");

        // finnish bedugging session
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText("User program finished");
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session was not killed.");
            throw(tee);
        }
    }
    
    public void testClassBreakpoint() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        Node projectNode = new Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);

        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        
        // create new breakpoint and check pre-filled values
        editorOperator.setCaretPosition(32, 1);
        Utilities.sleep(2000);
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        Utilities.sleep(1000);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 0).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 1).getText()));
        dialog.cancel();
        
        editorOperator.setCaretPosition(42, 1);
        Utilities.sleep(2000);
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        Utilities.sleep(1000);
        dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 0).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 1).getText()));
        dialog.cancel();

        editorOperator.setCaretPosition(50, 1);
        Utilities.sleep(2000);
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        Utilities.sleep(1000);
        dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 0).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 1).getText()));
        dialog.cancel();

        editorOperator.setCaretPosition(107, 1);
        Utilities.sleep(2000);
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        Utilities.sleep(1000);
        dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 0).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 1).getText()));
        new JTextFieldOperator(dialog, 1).setText("MemoryView*");
        new JComboBoxOperator(dialog, 1).selectItem("Class Prepare or Unload");
        dialog.ok();

        // test if breakpoint exists in breakpoints view
        Utilities.sleep(1000);
        Utilities.showBreakpointsView();
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Class examples.advanced.MemoryView* prepare / unload".equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Class breakpoint was not created.", found);

        // test if debugger stops at an assumed breakpoint line
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        new Action(null, null, Utilities.debugProjectShortcut).performShortcut();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        Utilities.sleep(1000);
        for (int i = 0; i < 6; i++) {
            mwo.waitStatusText("Class breakpoint hit for class examples.advanced.MemoryView");
            //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
            Utilities.sleep(1000);
            new Action(null, null, Utilities.continueShortcut).performShortcut();
        }
        mwo.waitStatusText(Utilities.runningStatusBarText);
        
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText(Utilities.finishedStatusBarText);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session was not killed.");
            throw(tee);
        }
    }
    
    public void testVariableAccessBreakpoint() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        Node projectNode = new Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);

        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        editorOperator.setCaretPosition(40, 19);
        Utilities.sleep(2000);
        
        // create new breakpoint and check pre-filled values
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        Utilities.sleep(1000);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Variable");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 1).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 2).getText()));
        assertTrue("Variable Name was not set to correct value.", "timer".equals(new JTextFieldOperator(dialog, 3).getText()));
        new JComboBoxOperator(dialog, 1).selectItem("Variable Access");
        dialog.ok();

        // test if breakpoint exists in breakpoints view
        Utilities.sleep(1000);
        Utilities.showBreakpointsView();
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Variable MemoryView.timer access".equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Variable breakpoint was not created.", found);

        // test if debugger stops at an assumed breakpoint line
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        new Action(null, null, Utilities.debugProjectShortcut).performShortcut();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        Utilities.sleep(1000);
        mwo.waitStatusText("Thread main stopped at MemoryView.java:70.");

        // finnish bedugging session
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText(Utilities.finishedStatusBarText);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session was not killed.");
            throw(tee);
        }
    }
    
    public void testVariableModificationBreakpoint() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        Node projectNode = new Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);

        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        editorOperator.setCaretPosition(45, 1);
        Utilities.sleep(2000);
        
        // create new breakpoint and check pre-filled values
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        Utilities.sleep(1000);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Variable");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 1).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 2).getText()));
        new JTextFieldOperator(dialog, 3).setText("timer");
        new JComboBoxOperator(dialog, 1).selectItem("Variable Modification");
        dialog.ok();
        
        // test if breakpoint exists in breakpoints view
        Utilities.sleep(1000);
        Utilities.showBreakpointsView();
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Variable MemoryView.timer modification".equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Variable breakpoint was not created.", found);

        // test if debugger stops at an assumed breakpoint line
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        new Action(null, null, Utilities.debugProjectShortcut).performShortcut();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        Utilities.sleep(1000);
        mwo.waitStatusText("Thread main stopped at MemoryView.java:45.");

        // finnish bedugging session
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText(Utilities.finishedStatusBarText);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session was not killed.");
            throw(tee);
        }
    }
    
    /**
     *
     */
    public void testThreadBreakpoint() {
        /*Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
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
        mwo.waitStatusText(debuggerFinishedMsg);*/
        
    }
    
    /**
     *
     */
    public void testExceptionBreakpoint() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        Node projectNode = new Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);

        // create new breakpoint and check pre-filled values
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        Utilities.sleep(1000);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Exception");
        Utilities.sleep(1000);
        new JTextFieldOperator(dialog, 2).setText("java.lang");
        new JComboBoxOperator(dialog, 2).typeText("Exception");
        new JComboBoxOperator(dialog, 1).selectItem("Catched or Uncatched");
        dialog.ok();
        
        // test if breakpoint exists in breakpoints view
        Utilities.sleep(1000);
        Utilities.showBreakpointsView();
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Exception Exception".equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Exception breakpoint was not created.", found);
        
        // test if debugger hits the breakpoint
        new Action(null, null, Utilities.debugProjectShortcut).performShortcut();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        Utilities.sleep(1000);
        mwo.waitStatusText("Exception breakpoint hit in java.lang.ClassLoader");
        
        // delete the breakpoint and continue, check if application gets up
        new JPopupMenuOperator(jTableOperator.callPopupOnCell(rowNumber, 0)).pushMenu("Delete");
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        mwo.waitStatusText(Utilities.runningStatusBarText);

        // finnish bedugging session
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText(Utilities.finishedStatusBarText);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session was not killed.");
            throw(tee);
        }
    }
}
