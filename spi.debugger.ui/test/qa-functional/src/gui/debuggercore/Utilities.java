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

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

public class Utilities {
    
    private String  sampleDir = System.getProperty("netbeans.user") + File.separator + "sampledir";
    
    private static String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window");
    private static String debuggingItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");
    
    private static String breakpointsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.nodes.Bundle", "CTL_Breakpoints_view");
    private static String sessionsViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.nodes.Bundle", "CTL_Sessions_view");
    private static String watchesViewTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.nodes.Bundle", "CTL_Watches_view");
    
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
            
            String confirmTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", 
                "MSG_ConfirmDeleteAllBreakpointsTitle");
            new NbDialogOperator(confirmTitle).yes();            
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
            
            String confirmDeleteAll = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", 
                "MSG_ConfirmDeleteAllWatchesTitle");
            new NbDialogOperator(confirmDeleteAll).yes();            
        }
        
        watchesOper.close();
        
    }
    
    public static boolean mountSampledir() {
        
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
        
    }
    
    public static String getApplicationFileSystem() {
        
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
        
    }

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
                mwo.waitStatusText("Debugger finished");
                
            }
        }
        
        sessionsOper.close();

    }
    
    public static void showDebuggerView(String viewName) {
        new ActionNoBlock(windowItem + "|" + debuggingItem + "|" + viewName, null).perform();
    }
    
    /* Close terms only if they are opened */
    public static void closeTerms() {
        try {
            TermOperator debuggerConsole = new TermOperator("Debugger Console");
            debuggerConsole.close();
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            //
        }
        try {
            TermOperator processOutput = new TermOperator("Process Output");
            processOutput.close();
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            //
        }
    }
    
}
