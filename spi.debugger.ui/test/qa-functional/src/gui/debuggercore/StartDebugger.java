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
import org.netbeans.jellytools.modules.debuggercore.operators.AttachJDialogOperator;
import org.netbeans.jellytools.nodes.JavaNode;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;

public class StartDebugger extends JellyTestCase {
    
    private String  sampleDir = System.getProperty("netbeans.user") + File.separator + "sampledir";
    
    private static String startSessionItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Debug/StartSession");
    private static String runInDebuggerItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Start_action_name");
    private static String attachItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Connect");
    private static String finishSessionsItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Finish_action_name");
    private static String stepIntoItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Step_into_action_name");
    private static String runToCursorItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.support.actions.Bundle", "CTL_Run_to_cursor_action_name");
    
    private static String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window");
    private static String debuggerItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Debug");
    
    private static String editAction = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");
    
    private static String debuggerFinishedMsg = Bundle.getStringTrimmed("org.netbeans.modules.debugger.multisession.Bundle", "CTL_Debugger_end");
    private static String debuggerFinishedTitle = Bundle.getStringTrimmed("org.netbeans.modules.debugger.multisession.Bundle", "CTL_Finish_debugging_dialog");
    
    public StartDebugger(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new StartDebugger("testRunDebugger"));
        suite.addTest(new StartDebugger("testRunDebuggerStepInto"));
        suite.addTest(new StartDebugger("testRunDebuggerRunToCursor"));
        suite.addTest(new StartDebugger("testRunDebuggerAttach"));
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
        Utilities.closeZombieSessions();
        new EventTool().waitNoEvent(1000);
    }
    
    /**
     *
     */
    public void testRunDebugger() {
        
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        
        new Action(debuggerItem + "|" + startSessionItem + "|" + runInDebuggerItem, null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Debugger running");
        
        new ActionNoBlock(debuggerItem + "|" + finishSessionsItem, null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            NbDialogOperator dialog = new NbDialogOperator(debuggerFinishedTitle);
            dialog.ok();
        } catch (TimeoutExpiredException tee) {
            System.out.println("Dialog - Finish Debugging Session was not displayed.");
            throw(tee);
        }
        mwo.waitStatusText(debuggerFinishedMsg);
        Utilities.closeTerms();
        
    }
    
    /**
     *
     */
    public void testRunDebuggerStepInto() {

        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock(editAction);
        
        new Action(debuggerItem + "|" + stepIntoItem, null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Thread main stopped at DebuggerTestApplication.main line 133.");
        
        new ActionNoBlock(debuggerItem + "|" + finishSessionsItem, null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            NbDialogOperator dialog = new NbDialogOperator(debuggerFinishedTitle);
            dialog.ok();
        } catch (TimeoutExpiredException tee) {
            System.out.println("Dialog - Finish Debugging Session was not displayed.");
            throw(tee);
        }
        mwo.waitStatusText(debuggerFinishedMsg);
        Utilities.closeTerms();
        
    }
    
    /**
     *
     */
    public void testRunDebuggerRunToCursor() {
        
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock(editAction);
        
        EditorOperator editorOperator = new EditorOperator("DebuggerTestApplication"); // NOI18N
        new EventTool().waitNoEvent(2000);
        editorOperator.setCaretPosition(60,1);
        
        new Action(debuggerItem + "|" + runToCursorItem, null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Thread main stopped at DebuggerTestApplication.initComponents line 60.");
        new ActionNoBlock(debuggerItem + "|" + finishSessionsItem, null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            NbDialogOperator dialog = new NbDialogOperator(debuggerFinishedTitle);
            dialog.ok();
        } catch (TimeoutExpiredException tee) {
            System.out.println("Dialog - Finish Debugging Session was not displayed.");
            throw(tee);
        }
        mwo.waitStatusText(debuggerFinishedMsg);
        Utilities.closeTerms();
        
    }
    
    /**
     *
     */
    public void testRunDebuggerAttach() {
        
        int attachAddress = 0;

        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode.tree(), 
            Utilities.getApplicationFileSystem() + "|" + "DebuggerTestApplication"); // NOI18N
        javaNode.select();
        javaNode.performPopupActionNoBlock("Properties");
        new EventTool().waitNoEvent(2000);
        
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator();
        PropertySheetTabOperator propertySheetTabOperator = propertySheetOperator.getPropertySheetTabOperator("Execution");
        new ComboBoxProperty(propertySheetTabOperator, "Executor").setValue("Debugger Execution");
        propertySheetOperator.close();
        javaNode.performPopupActionNoBlock("Execute");
        new EventTool().waitNoEvent(5000);
        
        OutputWindowOperator outputWindowOperator = new OutputWindowOperator();
        outputWindowOperator.selectPage("DebuggerTestApplication");
        String stringAddress = outputWindowOperator.getText().substring(outputWindowOperator.getText().indexOf("Listening"), 
            outputWindowOperator.getText().indexOf("\n"));
        stringAddress = stringAddress.substring(stringAddress.indexOf(":") + 1).trim();
        try {
            attachAddress = Integer.parseInt(stringAddress);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            throw(nfe);
        }
        javaNode.select();
        
        new ActionNoBlock(debuggerItem + "|" + startSessionItem + "|" + attachItem, null).perform();
        AttachJDialogOperator attachJDialogOperator = new AttachJDialogOperator();
        if (attachJDialogOperator.getTransport().equals("dt_shmem")) {
            attachJDialogOperator.selectConnector(1);        
        }  else {
            System.out.println("< "+"dt_socket"+" >");
        }
        new EventTool().waitNoEvent(2000);
        attachJDialogOperator.setPort(""); // NOI18N
        attachJDialogOperator.typePort(stringAddress);
        attachJDialogOperator.oK();
        
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Debugger running");
        new EventTool().waitNoEvent(500);
        new ActionNoBlock(debuggerItem + "|" + finishSessionsItem, null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            NbDialogOperator dialog = new NbDialogOperator(debuggerFinishedTitle);
            dialog.ok();
        } catch (TimeoutExpiredException tee) {
            System.out.println("Dialog - Finish Debugging Session was not displayed.");
            throw(tee);
        }
        mwo.waitStatusText(debuggerFinishedMsg);
        
        new Action(windowItem + "|Runtime", null).perform();
        TopComponentOperator runtimeOper = new TopComponentOperator("Runtime");
        JTreeOperator jTreeOperator = new JTreeOperator(runtimeOper);
        Node node = new Node(jTreeOperator, "Processes|DebuggerTestApplication");
        node.select();
        new EventTool().waitNoEvent(500);
        node.performPopupActionNoBlock("Terminate Process");
        new TermOperator("DebuggerTestApplication").close();
        new TermOperator("Debugger Console").close();
        
    }
    
}
