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

public class Views extends JellyTestCase {
    
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
    
    private static String breakpointsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.nodes.Bundle", "CTL_Breakpoints_view");
    private static String watchesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.nodes.Bundle", "CTL_Watches_view");    
    
    public Views(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Views("testViews"));
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
        //Utilities.closeTerms();
    }
    
    /**
     *
     */
    public void testViews() {
        
        String [] actionItems = new String [] { Utilities.localVarsItem, Utilities.watchesItem, 
            Utilities.callStackItem, Utilities.classesItem, Utilities.breakpointsItem, 
            Utilities.sessionsItem, Utilities.threadsItem, Utilities.allInOneItem };
            
        String [] viewTitles = new String [] { Utilities.localVarsViewTitle, Utilities.watchesViewTitle, 
            Utilities.callStackViewTitle, Utilities.classesViewTitle, Utilities.breakpointsViewTitle,
            Utilities.sessionsViewTitle, Utilities.threadsViewTitle, Utilities.allInOneViewTitle};
         
        TopComponentOperator [] viewsOpers = new TopComponentOperator[actionItems.length];
            
            for (int i = 0; i < actionItems.length; i++) {
                Utilities.showDebuggerView(actionItems[i]);
                TopComponentOperator top = new TopComponentOperator(viewTitles[i]);
                viewsOpers[i] = top;
            }
            
            for (int j = 0; j < viewsOpers.length; j++) {
                viewsOpers[j].close();
            }
        
    }
    
}
