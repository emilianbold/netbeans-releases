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
import javax.swing.JTable;
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

public class LocalVariables extends JellyTestCase {
    
//    private String  sampleDir = System.getProperty("netbeans.user") + File.separator + "sampledir";
//    
//    private static String newBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_AddBreakpoint");
//    private static String newBreakpointTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Breakpoint_Title");
//    private static String finishSessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Finish_action_name");
//    private static String startSessionItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Debug/StartSession");
//    private static String runInDebuggerItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Start_action_name");
//    
//    private static String debuggerItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Debug");
//    
//    private static String debuggerFinishedMsg = Bundle.getStringTrimmed("org.netbeans.modules.debugger.multisession.Bundle", "CTL_Debugger_end");
//    private static String debuggerFinishedTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.multisession.Bundle", "CTL_Finish_debugging_dialog");
    
    public LocalVariables(String name) {
        super(name);
    }
    
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new LocalVariables("testLocalVariablesPresence"));
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
    
    public void testLocalVariablesPresence() {
        
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        Node projectNode = new Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);

        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupActionNoBlock("Open");
        
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        new EventTool().waitNoEvent(1000);
        
        // create new line breakpoint
        editorOperator.setCaretPosition(111, 1);
        new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.toggleBreakpointItem).toString(), null).perform();
        
        // test if breakpoint exists in breakpoints view
        Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(Utilities.breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        
        int rowNumber = 0;
        boolean found = false;
        
        while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
            if ("Line MemoryView.java:111".equals(jTableOperator.getValueAt(rowNumber, 0).toString()) ) {
                found = true;
            };
            rowNumber++;
        }
        assertTrue("Line breakpoint was not created.", found);
        breakpointsOper.close();

        // test if debugger stops at an assumed breakpoint line
        new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Thread main stopped at MemoryView.java:111.");

        new EventTool().waitNoEvent(1000);
        
        // show local variables view and check values
        Utilities.showDebuggerView(Utilities.localVarsViewTitle);
        TopComponentOperator localVarsOper = new TopComponentOperator(Utilities.localVarsViewTitle);
        jTableOperator = new JTableOperator(localVarsOper);
        
        TreeTableOperator treeTableOperator = new TreeTableOperator((JTable) jTableOperator.getSource());
        new Node(treeTableOperator.tree(), "this").expand();
	 	
        String [] expectedLocalVariablesColumn_0 = {"this", "r", "free","total","taken" };
        String [] expectedLocalVariablesColumn_1 = {"MemoryView", "Runtime", "long", "long", "int"};
        for (int i = 0; i < expectedLocalVariablesColumn_0.length; i++) {
            rowNumber = 0;
            found = false;
            while ((rowNumber < jTableOperator.getRowCount()) && (!found)) {
                if ( (expectedLocalVariablesColumn_0[i].equals(jTableOperator.getValueAt(rowNumber,0).toString())) )  {
                    found = true;
                };
                rowNumber++;
            }
           assertTrue("Local Variable \"" + expectedLocalVariablesColumn_0[i] + "\" was not displayed", found);
        }                        

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
