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

import junit.textui.TestRunner;

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import org.netbeans.junit.NbTestSuite;

public class Watches extends JellyTestCase {
    
    public Watches(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Watches("testAddWatch"));
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
        Utilities.deleteAllWatches();
        Utilities.closeZombieSessions();
        Utilities.closeTerms();
        new EventTool().waitNoEvent(750);
    }
    
    /**
     *
     */
    public void testAddWatch() {
        TopComponentOperator projectsTabOper = new TopComponentOperator(Utilities.projectsTitle);
        Node projectNode = new Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction("Set as Main Project");

        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction("Open");
        
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(1000);
        editorOperator.setCaretPosition(103, 1);
        
        new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.toggleBreakpointItem).toString(), null).perform();
        
        // create new watches
        new ActionNoBlock(Utilities.runMenu + "|" + Utilities.newWatchItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText("free");
        dialog.ok();
        new EventTool().waitNoEvent(500);
        
        new ActionNoBlock(Utilities.runMenu + "|" + Utilities.newWatchItem, null).perform();
        dialog = new NbDialogOperator(Utilities.newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText("taken");
        dialog.ok();
        new EventTool().waitNoEvent(500);
        
        new ActionNoBlock(Utilities.runMenu + "|" + Utilities.newWatchItem, null).perform();
        dialog = new NbDialogOperator(Utilities.newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText("total");
        dialog.ok();
        new EventTool().waitNoEvent(500);

        // test if breakpoint exists in breakpoints view
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Line MemoryView.java:103".equals(jTableOperator.getValueAt(rowNumber, 0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Line breakpoint was not created.", found);
        breakpointsOper.close();
        
        // test if debugger stops at an assumed breakpoint line
        new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Thread main stopped at MemoryView.java:103.");
        
        // wait till all views are refreshed, ugly!
        new EventTool().waitNoEvent(1000);
        
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        TopComponentOperator watchesOper = new TopComponentOperator(Utilities.watchesViewTitle);
        jTableOperator = new JTableOperator(watchesOper);
        
        String [] expectedWatches = {"free", "taken", "total"};
        
        for (int i = 0; i < expectedWatches.length; i++) {            
            rowNumber = 0;
            found = false;
            while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
                if (expectedWatches[i].equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                    found = true;
                };
                rowNumber++;
            }
            assertTrue("Watch \"" + expectedWatches[i] + "\" was not created", found);
        }                

        // finnish bedugging session
        new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText("User program finished");
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session was not killed.");
            throw(tee);
        }
    }
}
