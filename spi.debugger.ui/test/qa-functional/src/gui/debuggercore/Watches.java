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
import org.netbeans.jemmy.operators.JTextFieldOperator;

import org.netbeans.junit.NbTestSuite;

public class Watches extends JellyTestCase {
    
    private String  sampleDir = System.getProperty("netbeans.user") + File.separator + "sampledir";
    
    private static String newBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_AddBreakpoint");
    private static String newBreakpointTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Breakpoint_Title");
    private static String debuggerItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Debug");
    
    private static String finishSessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Finish_action_name");
    private static String debuggerFinishedMsg = Bundle.getStringTrimmed("org.netbeans.modules.debugger.multisession.Bundle", "CTL_Debugger_end");
    private static String debuggerFinishedTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.multisession.Bundle", "CTL_Finish_debugging_dialog");
    private static String startSessionItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Debug/StartSession");
    private static String runInDebuggerItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Start_action_name");
    
    private static String newWatchTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Watch_Title");
    private static String newWatchItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "AddWatch");
    
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
        
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock("Edit");
        
        EditorOperator editorOperator = new EditorOperator("DebuggerTestApplication"); // NOI18N
        new EventTool().waitNoEvent(1000);
        editorOperator.setCaretPosition(60,1);
        
        new ActionNoBlock(debuggerItem + "|" + newWatchItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator(newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText("jPanel1"); // NOI18N
        dialog.ok();
        new EventTool().waitNoEvent(500);
        
        new ActionNoBlock(debuggerItem + "|" + newWatchItem, null).perform();
        dialog = new NbDialogOperator(newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText("jButton1"); // NOI18N
        dialog.ok();
        new EventTool().waitNoEvent(500);
        
        new ActionNoBlock(debuggerItem + "|" + newWatchItem, null).perform();
        dialog = new NbDialogOperator(newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText("jButton2"); // NOI18N
        dialog.ok();
        new EventTool().waitNoEvent(500);
        
        new ActionNoBlock(debuggerItem + "|" + newBreakpointItem, null).perform();
        dialog = new NbDialogOperator(newBreakpointTitle);
        new JComboBoxOperator(dialog, 1).selectItem("Line");
        new JTextFieldOperator(dialog, 0).typeText(""); // NOI18N
        new JTextFieldOperator(dialog, 1).typeText("DebuggerTestApplication"); // NOI18N
        new JTextFieldOperator(dialog, 2).typeText("60"); // NOI18N
        dialog.ok();
     
        new EventTool().waitNoEvent(1000);
        
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Line DebuggerTestApplication.java:60".equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Line breakpoint was not created.", found);
        // test if debugger stops at an assumed breakpoint line
        
        javaNode.select();
        new Action(debuggerItem + "|" + startSessionItem + "|" + runInDebuggerItem, null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Breakpoint reached at line 60 in class DebuggerTestApplication by thread main.");
        
        // wait till all views are refreshed, ugly!
        new EventTool().waitNoEvent(1000);
        
        Utilities.showDebuggerView(Utilities.watchesViewTitle);
        TopComponentOperator watchesOper = new TopComponentOperator(Utilities.watchesViewTitle);
        jTableOperator = new JTableOperator(watchesOper);
        
        String [] expectedWatches = {"jPanel1", "jButton1", "jButton2"};
        
        for (int i = 0; i < expectedWatches.length; i++) {            
            rowNumber = 0;
            found = false;
            while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
                if (expectedWatches[i].equals(jTableOperator.getValueAt(rowNumber,0).toString()) ) {
                    found = true;
                };
                rowNumber++;
            }
            assertTrue("Some watch (" + expectedWatches[i] + ") was not created", found); // NOI18N
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
