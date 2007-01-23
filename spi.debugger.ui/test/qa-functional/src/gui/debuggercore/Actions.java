/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * The Original Software is NetBeans.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2005
 * All Rights Reserved.
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

package gui.debuggercore;

import java.awt.event.KeyEvent;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.FinishDebuggerAction;
import org.netbeans.jellytools.modules.debugger.actions.RunToCursorAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOutAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author cincura, ehucka
 */
public class Actions extends JellyTestCase {
    
    private MainWindowOperator.StatusTextTracer stt;
    
    public Actions(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Actions("testCheckEnabledActions"));
        suite.addTest(new Actions("testCheckEnabledActionsDebugging"));
        suite.addTest(new Actions("testToggleBreakpoints"));
        suite.addTest(new Actions("testStartDebugging"));
        suite.addTest(new Actions("testStepInto"));
        suite.addTest(new Actions("testStepOut"));
        suite.addTest(new Actions("testContinue"));
        suite.addTest(new Actions("testStepOver"));
        suite.addTest(new Actions("testRunToCursor"));
        suite.addTest(new Actions("removeBreakpoint"));
        suite.addTest(new Actions("testPause"));
        suite.addTest(new Actions("testFinishSession"));
        return suite;
    }
    
    /** setUp method  */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        stt = MainWindowOperator.getDefault().getStatusTextTracer();
        // start to track Main Window status bar
        stt.start();
        // increase timeout to 60 seconds when waiting for status bar text
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 30000);
    }
    
    public void testCheckEnabledActions() {
        new Action(null, Utilities.setMainProjectAction).perform(new ProjectsTabOperator().getProjectRootNode(Utilities.testProjectName));
        
        Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
        Utilities.verifyPopup(projectNode, new String[] {
            //check build project action
            Bundle.getString("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_BuildAction_Name"),
            //check run project action
            Bundle.getString("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_RunAction_Name"),
            //check debug project action
            Bundle.getString("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_DebugAction_Name")
        });
        
        //main menu actions
        //check main menu debug main project action
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.debugMainProjectItem, null).isEnabled());
        //Step into
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.stepIntoItem, null).isEnabled());
        //new breakpoint
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.newBreakpointItem, null).isEnabled());
        //new watch
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.newWatchItem, null).isEnabled());
        //main menu actions disabled
        //check finish debugger
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.finishSessionsItem, null).isEnabled());
        //pause
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.pauseItem, null).isEnabled());
        //continue
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.continueItem, null).isEnabled());
        //step over
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.stepOverItem, null).isEnabled());
        //step out
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.stepOutItem, null).isEnabled());
        //run to cursor
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.runToCursorItem, null).isEnabled());
        //run into method
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.runIntoMethodItem, null).isEnabled());
        //apply code changes
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.applyCodeChangesItem, null).isEnabled());
        //toggle breakpoint
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.toggleBreakpointItem, null).isEnabled());
        //evaluate expression
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.evaluateExpressionItem, null).isEnabled());
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        
        //open source file
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode); // NOI18N
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.setCaret(eo, 80);
        //main menu file actions
        //check run file action
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_RunSingleAction_Name", new Object[] {new Integer(1), "MemoryView.java"});
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.runFileMenu+"|"+actionName, null).isEnabled());
        //check debug file action
        actionName = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_DebugSingleAction_Name", new Object[] {new Integer(1), "MemoryView.java"});
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.runFileMenu+"|"+actionName, null).isEnabled());
        //run to cursor
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.runToCursorItem, null).isEnabled());
        //toggle breakpoint
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.toggleBreakpointItem, null).isEnabled());
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        
        //source popup menu actions
        JPopupMenuOperator operator = new JPopupMenuOperator(JPopupMenuOperator.callPopup(eo, 50, 50));
        Utilities.verifyPopup(operator, new String[] {
            //check debug file
            Bundle.getString("org.netbeans.modules.java.project.Bundle", "LBL_DebugFile_Action"),
            //new watch
            Bundle.getString("org.netbeans.editor.Bundle", "add-watch"),
            //check toggle breakpoint
            Bundle.getString("org.netbeans.editor.Bundle", "toggle-breakpoint")
        });
        
        //tools menu
        //debug is not visible
        for (int i=0;i < MainWindowOperator.getDefault().getToolbarCount();i++) {
            assertFalse(MainWindowOperator.getDefault().getToolbarName(i).equals(Utilities.debugToolbarLabel));
        }
        //run
        ContainerOperator tbrop = MainWindowOperator.getDefault().getToolbar(Bundle.getString("org.netbeans.modules.project.ui.Bundle", "Toolbars/Build"));
        assertTrue(MainWindowOperator.getDefault().getToolbarButton(tbrop, Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_DebugMainProjectAction_Name")).isEnabled());
        assertTrue(MainWindowOperator.getDefault().getToolbarButton(tbrop, Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Connect")).isEnabled());
        
        eo.close();
    }
    
    public void testCheckEnabledActionsDebugging() {
        //open source
        Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode); // NOI18N
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //place breakpoint
        Utilities.toggleBreakpoint(eo, 104);
        //start debugging
        new DebugProjectAction().perform(projectNode);
        //wait for breakpoint
        Utilities.waitStatusText("Thread main stopped at MemoryView.java:104");
        //check actions
        
        //main menu actions
        //check main menu debug main project action
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.debugMainProjectItem, null).isEnabled());
        //Step into
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.stepIntoItem, null).isEnabled());
        //new breakpoint
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.newBreakpointItem, null).isEnabled());
        //new watch
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.newWatchItem, null).isEnabled());
        //check finish debugger
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.finishSessionsItem, null).isEnabled());
        //pause
        assertFalse(new Action(Utilities.runMenu+"|"+Utilities.pauseItem, null).isEnabled());
        //continue
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.continueItem, null).isEnabled());
        //step over
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.stepOverItem, null).isEnabled());
        //step out
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.stepOutItem, null).isEnabled());
        //run to cursor
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.runToCursorItem, null).isEnabled());
        //run into method
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.runIntoMethodItem, null).isEnabled());
        //apply code changes
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.applyCodeChangesItem, null).isEnabled());
        //toggle breakpoint
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.toggleBreakpointItem, null).isEnabled());
        //evaluate expression
        assertTrue(new Action(Utilities.runMenu+"|"+Utilities.evaluateExpressionItem, null).isEnabled());
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        
        //debug toolbar
        ContainerOperator debugToolbarOper = Utilities.getDebugToolbar();
        assertTrue(MainWindowOperator.getDefault().getToolbarButton(debugToolbarOper,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_KillAction_name")).isEnabled());
        assertFalse(MainWindowOperator.getDefault().getToolbarButton(debugToolbarOper,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Pause_action_name")).isEnabled());
        assertTrue(MainWindowOperator.getDefault().getToolbarButton(debugToolbarOper,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Continue_action_name")).isEnabled());
        //step
        assertTrue(MainWindowOperator.getDefault().getToolbarButton(debugToolbarOper,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Step_over_action_name")).isEnabled());
        assertTrue(MainWindowOperator.getDefault().getToolbarButton(debugToolbarOper,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Step_into_action_name")).isEnabled());
        assertTrue(MainWindowOperator.getDefault().getToolbarButton(debugToolbarOper,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Step_out_action_name")).isEnabled());
        //run to cursor
        assertTrue(MainWindowOperator.getDefault().getToolbarButton(debugToolbarOper,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Run_to_cursor_action_name")).isEnabled());
        assertTrue(MainWindowOperator.getDefault().getToolbarButton(debugToolbarOper,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_Fix_action_name")).isEnabled());
        assertTrue(MainWindowOperator.getDefault().getToolbarButton(debugToolbarOper,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_New_Watch")).isEnabled());
        
        //remove breakpoint
        Utilities.toggleBreakpoint(eo, 104, false);
        //finish debugging
        Utilities.endSession();
        //close sources
        eo.close();
    }
    
    public void testToggleBreakpoints() {
        //open source
        Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode); // NOI18N
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //place breakpoint
        Utilities.toggleBreakpoint(eo, 80);
        assertTrue("Breakpoint annotation is not displayed", Utilities.checkAnnotation(eo, 80, "Breakpoint"));
    }
    
    public void testStartDebugging() {
        //start debugging
        Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
        new DebugProjectAction().perform(projectNode);
        //wait for breakpoint
        Utilities.waitStatusText("Thread main stopped at MemoryView.java:80");
        EditorOperator eo = new EditorOperator("MemoryView.java");
        assertTrue(Utilities.checkAnnotation(eo, 80, "Breakpoint"));
        assertTrue(Utilities.checkAnnotation(eo, 80, "CurrentPC"));
    }
    
    public void testStepInto() {
        new StepIntoAction().performShortcut();
        stt.waitText("Thread main stopped at MemoryView.java:92");
        //check 80, 92
        EditorOperator eo = new EditorOperator("MemoryView.java");
        assertTrue(Utilities.checkAnnotation(eo, 92, "CurrentPC"));
        assertTrue(Utilities.checkAnnotation(eo, 80, "CallSite"));
        /*
        Object[] annotations = ed.getAnnotations(80);
        for (int i = 0; i < annotations.length; i++) {
            Object object = annotations[i];
            System.err.println("Annotation on line 80: "+ed.getAnnotationType(annotations[i]));
        }*/
    }
    
    public void testStepOut() {
        new StepOutAction().performShortcut();
        stt.waitText("Thread main stopped at MemoryView.java:82");
        //check 82, 92
        EditorOperator eo = new EditorOperator("MemoryView.java");
        assertFalse(Utilities.checkAnnotation(eo, 92, "CurrentPC"));
        assertTrue(Utilities.checkAnnotation(eo, 82, "CurrentPC"));
    }
    
    public void testContinue() {
        new ContinueAction().performShortcut();
        stt.waitText("Thread main stopped at MemoryView.java:80");
        //check 80, 82
        EditorOperator eo = new EditorOperator("MemoryView.java");
        assertFalse(Utilities.checkAnnotation(eo, 82, "CurrentPC"));
        assertTrue(Utilities.checkAnnotation(eo, 80, "CurrentPC"));
    }
    
    public void testStepOver() {
        new StepOverAction().performShortcut();
        stt.waitText("Thread main stopped at MemoryView.java:82");
        //check 80, 82
        EditorOperator eo = new EditorOperator("MemoryView.java");
        assertFalse(Utilities.checkAnnotation(eo, 80, "CurrentPC"));
        assertTrue(Utilities.checkAnnotation(eo, 82, "CurrentPC"));
    }
    
    public void testRunToCursor() {
        //continue
        new ContinueAction().performShortcut();
        stt.waitText("Thread main stopped at MemoryView.java:80");
        //get line number
        EditorOperator eo = new EditorOperator("MemoryView.java");
        eo.select("r.totalMemory");
        int line = eo.getLineNumber();
        //run to cursor
        new RunToCursorAction().performShortcut();
        stt.waitText("Thread main stopped at MemoryView.java:"+line);
        //check line
        assertFalse(Utilities.checkAnnotation(eo, 80, "CurrentPC"));
        assertTrue(Utilities.checkAnnotation(eo, line, "CurrentPC"));
    }
    
    public void removeBreakpoint() {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //place breakpoint
        Utilities.toggleBreakpoint(eo, 80, false);
        assertFalse("Breakpoint annotation is not removed", Utilities.checkAnnotation(eo, 80, "Breakpoint"));
        
    }
    
    public void testPause() {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        //place breakpoint
        Utilities.toggleBreakpoint(eo, 80);
        //continue
        new ContinueAction().performShortcut();
        stt.waitText("Thread main stopped at MemoryView.java:80");
        //remove breakpoint
        Utilities.toggleBreakpoint(eo, 80, false);
        //continue
        new ContinueAction().performShortcut();
        //pause after 300 ms - should be on sleep
        Utilities.sleep(400);
        new Action(Utilities.runMenu+"|"+Utilities.pauseItem, null).perform();
        Utilities.waitStatusTextPrefix("Thread main stopped at ");
        new EditorOperator("Thread.java").close();
        eo = new EditorOperator("MemoryView.java");
        assertTrue(Utilities.checkAnnotation(eo, 82, "CallSite"));
    }
    
    public void testFinishSession() {
        new FinishDebuggerAction().performShortcut();
        Utilities.waitStatusTextPrefix("Finished building ");
    }
}
