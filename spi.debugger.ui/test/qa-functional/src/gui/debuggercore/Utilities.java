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

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;

public class Utilities {
    
    public static String windowMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window");
    public static String runMenu = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    public static String debugMenu = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");

    public static String toggleBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Toggle_breakpoint");
    public static String newBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_AddBreakpoint");
    public static String newWatchItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_New_Watch");
    public static String runInDebuggerItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_DebugMainProjectAction_Name");
    public static String stepIntoItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Step_into_action_name");
    public static String continueItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Continue_action_name");
    public static String killSessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_KillAction_name");
    public static String runToCursorItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Run_to_cursor_action_name");
    
    public static String localVarsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_LocalVariablesAction");
    public static String watchesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchesAction");
    public static String callStackItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_CallStackAction");
    public static String classesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.actions.Bundle", "CTL_ClassesAction");
    public static String sourcesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.actions.Bundle", "CTL_SourcesViewAction");
    public static String breakpointsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_BreakpointsAction");
    public static String sessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_SessionsAction");
    public static String threadsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_ThreadsAction");
    
    public static String localVarsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Variables_view");
    public static String watchesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Watches_view");
    public static String callStackViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Call_stack_view");
    public static String classesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.views.Bundle", "CTL_Classes_view");
    public static String sourcesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.views.Bundle", "CTL_Sourcess_view");
    public static String breakpointsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Breakpoints_view");
    public static String sessionsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Sessions_view");
    public static String threadsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Threads_view");
    
    public static String newBreakpointTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Breakpoint_Title");
    public static String newWatchTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchDialog_Title");
    
    public static String runningStatusBarText = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_running");
    public static String finishedStatusBarText = Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_finished");
    public static String buildCompleteStatusBarText = "Finished building";
    
    public static String openSourceAction = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    public static String setMainProjectAction = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetAsMainProjectAction_Name");
    public static String projectPropertiesAction = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_CustomizeProjectAction_Popup_Name");
    public static String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_Customizer_Title");
    public static String runningProjectTreeItem = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.customizer.Bundle", "LBL_Config_Run");
    
    public static String testProjectName = "debugTestProject";
    
    public static Action.Shortcut toggleBreakpointShortcut = new Action.Shortcut(KeyEvent.VK_F8, KeyEvent.CTRL_MASK);
    public static Action.Shortcut newBreakpointShortcut = new Action.Shortcut(KeyEvent.VK_F8, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);
    public static Action.Shortcut newWatchShortcut = new Action.Shortcut(KeyEvent.VK_F7, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);
    public static Action.Shortcut debugProjectShortcut = new Action.Shortcut(KeyEvent.VK_F5);
    public static Action.Shortcut runToCursorShortcut = new Action.Shortcut(KeyEvent.VK_F4);
    public static Action.Shortcut stepIntoShortcut = new Action.Shortcut(KeyEvent.VK_F7);
    public static Action.Shortcut continueShortcut = new Action.Shortcut(KeyEvent.VK_F5, KeyEvent.CTRL_MASK);
    public static Action.Shortcut killSessionShortcut = new Action.Shortcut(KeyEvent.VK_F5, KeyEvent.SHIFT_MASK);
    public static Action.Shortcut buildProjectShortcut = new Action.Shortcut(KeyEvent.VK_F11);
    
    public Utilities() {}
    
    public static void deleteAllBreakpoints() {
        showBreakpointsView();
        sleep(2000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(breakpointsViewTitle));
        if (jTableOperator.getRowCount() > 0)
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenu("Delete All");
    }
    
    public static void deleteAllWatches() {
        showWatchesView();
        sleep(500);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(watchesViewTitle));
        if (jTableOperator.getRowCount() > 0)
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenu("Delete All");
    }
    
    public static void closeZombieSessions() {
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        showSessionsView();
        sleep(500);
        TopComponentOperator sessionsOper = new TopComponentOperator(sessionsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(sessionsOper);
        
        for (int i = 0; i < jTableOperator.getRowCount(); i++) {
            //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
            new Action(null, null, Utilities.killSessionShortcut).performShortcut();
            Utilities.sleep(1000);
        }
        
        jTableOperator = new JTableOperator(sessionsOper);            
        if (jTableOperator.getRowCount() > 0) {
            for (int i = 0; i < jTableOperator.getRowCount(); i++) {
                jTableOperator.selectCell(i,0);
                javax.swing.JPopupMenu jPopupMenu = jTableOperator.callPopupOnCell(jTableOperator.getRowCount() - 1,0);
                new JPopupMenuOperator(jPopupMenu).pushMenu("Finish");
                mwo.waitStatusText("User program finished");
            }
        }
        sessionsOper.close();
    }
    
    public static void showDebuggerView(String viewName) {
        new Action(windowMenu + "|" + debugMenu + "|" + viewName, null).perform();
    }
    
    public static void showLocalVariablesView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + localVarsItem, null).perform();
        new Action(null, null, new Action.Shortcut(KeyEvent.VK_1, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showWatchesView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + watchesItem, null).perform();
        new Action(null, null, new Action.Shortcut(KeyEvent.VK_2, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showCallStackView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + callStackItem, null).perform();
        new Action(null, null, new Action.Shortcut(KeyEvent.VK_3, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showClassesView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + classesItem, null).perform();
        new Action(null, null, new Action.Shortcut(KeyEvent.VK_4, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showBreakpointsView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + breakpointsItem, null).perform();
        new Action(null, null, new Action.Shortcut(KeyEvent.VK_5, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showSessionsView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + sessionsItem, null).perform();
        new Action(null, null, new Action.Shortcut(KeyEvent.VK_6, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showThreadsView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + threadsItem, null).perform();
        new Action(null, null, new Action.Shortcut(KeyEvent.VK_7, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void showSourcesView() {
        //new Action(windowMenu + "|" + debugMenu + "|" + sourcesItem, null).perform();
        new Action(null, null, new Action.Shortcut(KeyEvent.VK_8, KeyEvent.ALT_MASK|KeyEvent.SHIFT_MASK)).performShortcut();
    }
    
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (Exception ex) {};
    }
    
    public static String removeTags(String in) {
        String out = "";
        in = in.trim();
        if (in.indexOf('<') == -1) {
            out = in;
        } else {
            while (in.indexOf('<') >= 0) {
                if (in.indexOf('<') == 0) {
                    in = in.substring(in.indexOf('>')+1, in.length());
                } else {
                    out += in.substring(0, in.indexOf('<'));
                    in = in.substring(in.indexOf('<'), in.length());
                }
            }
        }
        return out;
    }
    
    public static void setCaret(int row, int column ) {
        new EditorOperator("MemoryView.java").setCaretPosition(row, column);
        Utilities.sleep(2000);
    }
    
    public static void toggleBreakpoint(int row) {
        setCaret(row, 1);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.toggleBreakpointItem).toString(), null).perform();
        new Action(null, null, Utilities.toggleBreakpointShortcut).performShortcut();
        Utilities.sleep(200);
    }
    
    public static NbDialogOperator newBreakpoint(int row, int column) {
        setCaret(row, column);
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, newBreakpointShortcut).performShortcut();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        Utilities.sleep(1000);
        return dialog;
    }
    
    public static void endSession() {
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.finishedStatusBarText);
    }
    
    public static void startDebugger(String statusText) {
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        new Action(null, null, Utilities.debugProjectShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(statusText);
    }
}

