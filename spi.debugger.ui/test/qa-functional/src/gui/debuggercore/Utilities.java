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
import java.util.Enumeration;

import javax.swing.JPopupMenu;

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.MountAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

//import org.openide.filesystems.FileSystem;
//import org.openide.filesystems.Repository;

public class Utilities {
    
    public static String windowMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window");
    //public static String runMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Run");
    public static String runMenu = "Run";
    //public static String debugMenu = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");
    public static String debugMenu = "Debug";

    public static String toggleBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Toggle_breakpoint");
    public static String newBreakpointItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_AddBreakpoint");
    public static String newWatchItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_New_Watch");
    //public static String runInDebuggerItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Start_action_name");
    public static String runInDebuggerItem = "Run Main Project in Debugger";
    //public static String stepIntoItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "???");
    public static String stepIntoItem = "Step Into";
    public static String killSessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_KillAction_name");
    public static String runToCursorItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Run_to_cursor_action_name");
    
    public static String localVarsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_LocalVariablesAction");
    public static String watchesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchesAction");
    public static String callStackItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_CallStackAction");
    //public static String classesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_ClassesAction");
    public static String classesItem = "Classes";
    public static String breakpointsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_BreakpointsAction");
    public static String sessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_SessionsAction");
    public static String threadsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_ThreadsAction");
    
    public static String localVarsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Variables_view");
    public static String watchesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Watches_view");
    public static String callStackViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Call_stack_view");
    public static String classesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Classes_view");
    public static String breakpointsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Breakpoints_view");
    public static String sessionsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Sessions_view");
    public static String threadsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Threads_view");
    
    public static String newBreakpointTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Breakpoint_Title");
    public static String newWatchTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchDialog_Title");

    public static String projectsTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_ProjectsLogicalTabAction_Name");
    public static String testProjectName = "debugTestProject";
    
    
    public Utilities() {}
    
    public static void deleteAllBreakpoints() {
        showDebuggerView(breakpointsViewTitle);
        TopComponentOperator breakpointsOper = new TopComponentOperator(breakpointsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(breakpointsOper);
        if (jTableOperator.getRowCount() != 0) {
            jTableOperator.selectAll();
            new EventTool().waitNoEvent(750); // XXX
            JPopupMenu jPopupMenu = jTableOperator.callPopupOnCell(jTableOperator.getRowCount() - 1, 0);
            new EventTool().waitNoEvent(750); // XXX
            new JPopupMenuOperator(jPopupMenu).pushMenuNoBlock(new String [] {"Delete All"});
        }
        new EventTool().waitNoEvent(750);
        breakpointsOper.close();
    }
    
    public static void deleteAllWatches() {
        showDebuggerView(watchesViewTitle);
        TopComponentOperator watchesOper = new TopComponentOperator(watchesViewTitle);
        JTableOperator jTableOperator = new JTableOperator(watchesOper);
        if (jTableOperator.getRowCount() != 0) {
            jTableOperator.selectAll();
            new EventTool().waitNoEvent(750); // XXX
            JPopupMenu jPopupMenu = jTableOperator.callPopupOnCell(jTableOperator.getRowCount() - 1,0);
            new EventTool().waitNoEvent(750); // XXX
            new JPopupMenuOperator(jPopupMenu).pushMenuNoBlock(new String [] {"Delete All"});
        }
        watchesOper.close();
    }
    
/*    public static boolean mountSampledir() {
        
        new EventTool().waitNoEvent(1000); // XXX
        String userdir = System.getProperty("netbeans.user"); // NOI18N
        String mountPoint = userdir+File.separator+"sampledir"; // NOI18N
        mountPoint = mountPoint.replace('\\', '/');
        FileSystem fs = Repository.getDefault().findFileSystem(mountPoint);
        
        if (fs == null) {
            new MountAction().performMenu();
            NewWizardOperator newWizardOper = new NewWizardOperator();
            JTreeOperator tree = new JTreeOperator(newWizardOper);
            String localDirLabel = Bundle.getString("org.netbeans.core.Bundle", 
                "Templates/Mount/org-netbeans-core-ExLocalFileSystem.settings"); // NOI18N
            new Node(tree, localDirLabel).select();
            newWizardOper.next();
            File file = new File(mountPoint);
            new JFileChooserOperator().setSelectedFile(file);
            newWizardOper.finish();
        }
        return true;
        
    }*/
    
/*    public static String getApplicationFileSystem() {
        
        Enumeration enumerationFileSystems = Repository.getDefault().fileSystems();
        boolean founded = false;
        FileSystem fileSystem = null;
        
        while (enumerationFileSystems.hasMoreElements() && !founded) {
            fileSystem = (FileSystem) enumerationFileSystems.nextElement();
            if (fileSystem.toString().indexOf("debuggercore/test/apps") != -1) {
                founded = !founded;
            }
        }
        return fileSystem.getDisplayName();
        
    }*/

    public static void closeZombieSessions() {
        
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        
        showDebuggerView(sessionsViewTitle);
        TopComponentOperator sessionsOper = new TopComponentOperator(sessionsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(sessionsOper);
        
        if (jTableOperator.getRowCount() > 0) {
            for (int i = 0; i < jTableOperator.getRowCount(); i++) {
                jTableOperator.selectCell(i,0);
                new EventTool().waitNoEvent(750); // XXX
                JPopupMenu jPopupMenu = jTableOperator.callPopupOnCell(jTableOperator.getRowCount() - 1,0);
                new EventTool().waitNoEvent(750); // XXX
                new JPopupMenuOperator(jPopupMenu).pushMenuNoBlock(new String [] {"Finish"});
//                try {
//                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout",5000);
//                    NbDialogOperator dialog = new NbDialogOperator("Finish Debugging Session");
//                    dialog.ok();
//                } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
//                    System.out.println("Dialog - Finish Debugging Session was not displayed.");
//                    throw(tee);
//                }
                mwo.waitStatusText("User program finished");
                
            }
        }
        
        sessionsOper.close();

    }
    
    public static void showDebuggerView(String viewName) {
        new ActionNoBlock(windowMenu + "|" + debugMenu + "|" + viewName, null).perform();
    }
    
    public static void showLocalVariablesView() {
        new ActionNoBlock(windowMenu + "|" + debugMenu + "|" + localVarsItem, null).perform();
    }
    
    public static void showWatchesView() {
        new ActionNoBlock(windowMenu + "|" + debugMenu + "|" + watchesItem, null).perform();
    }
    
    public static void showCallStackView() {
        new ActionNoBlock(windowMenu + "|" + debugMenu + "|" + callStackItem, null).perform();
    }
    
    public static void showClassesView() {
        new ActionNoBlock(windowMenu + "|" + debugMenu + "|" + classesItem, null).perform();
    }
    
    public static void showBreakpointsView() {
        new ActionNoBlock(windowMenu + "|" + debugMenu + "|" + breakpointsItem, null).perform();
    }
    
    public static void showSessionsView() {
        new ActionNoBlock(windowMenu + "|" + debugMenu + "|" + sessionsItem, null).perform();
    }
    
    public static void showThreadsView() {
        new ActionNoBlock(windowMenu + "|" + debugMenu + "|" + threadsItem, null).perform();
    }
    
    /* Close terms only if they are opened */
    public static void closeTerms() {
        /*try {
            OutputTabOperator debuggerConsole = new OutputTabOperator("debugTestProject (debug)");
            debuggerConsole.close();
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            // do nothing
        }
        try {
            OutputTabOperator processOutput = new OutputTabOperator("examples.advanced.MemoryView");
            processOutput.close();
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            // do nothing
        }*/
    }
    
}
