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

import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author cincura
 */
public class Actions extends JellyTestCase {
    
    public Actions(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Actions("setupActionsTests"));
        suite.addTest(new Actions("testOpenSource"));
        suite.addTest(new Actions("testRunToCursor"));
        suite.addTest(new Actions("testStepInto"));
        suite.addTest(new Actions("testStepOut"));
        suite.addTest(new Actions("testContinue"));
        suite.addTest(new Actions("testBreakpoint"));
        suite.addTest(new Actions("testStepOver"));
        suite.addTest(new Actions("testPause"));
        suite.addTest(new Actions("testFinishSession"));
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
    }
    
    public void setupActionsTests() {
        org.netbeans.jellytools.nodes.Node projectNode = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(new ProjectsTabOperator()), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        
        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        
        new Action(null, null, Utilities.buildProjectShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.buildCompleteStatusBarText);

        Utilities.toggleBreakpoint(110);
        new EditorOperator("MemoryView.java").closeAllDocuments();
    }
    
    public void testOpenSource() {
        Utilities.startDebugger("Thread main stopped at MemoryView.java:110.");
        Utilities.sleep(3000);
        EditorOperator ed = new EditorOperator("MemoryView.java");
        Object[] annotations = ed.getAnnotations(110);
        assertTrue("Breakpoint annotation is not displayed", "Breakpoint".equals(ed.getAnnotationShortDescription(annotations[0])));
        assertTrue("Program counter annotation is not displayed", "Current Program Counter".equals(ed.getAnnotationShortDescription(annotations[1])));
    }
    
    public void testRunToCursor() {
        Utilities.setCaret(74, 1);
        new Action(null, null, Utilities.runToCursorShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:74.");
        Utilities.sleep(1000);
        EditorOperator ed = new EditorOperator("MemoryView.java");
        Object[] annotations = ed.getAnnotations(74);
        assertTrue("Program counter annotation is not displayed", "Current Program Counter".equals(ed.getAnnotationShortDescription(annotations[0])));
    }
    
    public void testStepInto() {
        new Action(null, null, Utilities.stepIntoShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:86.");
        Utilities.sleep(1000);
        EditorOperator ed = new EditorOperator("MemoryView.java");
        Object[] annotations = ed.getAnnotations(74);
        assertTrue("Call stack line annotation is not displayed", "Call Stack Line".equals(ed.getAnnotationShortDescription(annotations[0])));
        annotations = ed.getAnnotations(86);
        assertTrue("Program counter annotation is not displayed", "Current Program Counter".equals(ed.getAnnotationShortDescription(annotations[0])));
    }
    
    public void testStepOut() {
        new Action(null, null, Utilities.stepOutShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:76.");
        Utilities.sleep(1000);
        EditorOperator ed = new EditorOperator("MemoryView.java");
//        Object[] annotations = ed.getAnnotations(74);
//        assertTrue("Call stack line annotation was not removed", annotations.length == 0);
        Object[] annotations = ed.getAnnotations(76);
        assertTrue("Program counter annotation is not displayed", "Current Program Counter".equals(ed.getAnnotationShortDescription(annotations[0])));
    }
    
    public void testContinue() {
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.runningStatusBarText);
        Utilities.sleep(10000);
        assertTrue("Program is not running", Utilities.runningStatusBarText.equalsIgnoreCase(MainWindowOperator.getDefault().getStatusText()));
    }

    public void testBreakpoint() {
        Utilities.toggleBreakpoint(74);
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:74.");
        Utilities.sleep(1000);
        EditorOperator ed = new EditorOperator("MemoryView.java");
        Object[] annotations = ed.getAnnotations(74);
        assertTrue("Breakpoint annotation is not displayed", "Breakpoint".equals(ed.getAnnotationShortDescription(annotations[0])));
        assertTrue("Program counter annotation is not displayed", "Current Program Counter".equals(ed.getAnnotationShortDescription(annotations[1])));
    }
    
    public void testStepOver() {
        new Action(null, null, Utilities.stepOverShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:76.");
        Utilities.sleep(1000);
        EditorOperator ed = new EditorOperator("MemoryView.java");
        Object[] annotations = ed.getAnnotations(76);
        assertTrue("Program counter annotation is not displayed", "Current Program Counter".equals(ed.getAnnotationShortDescription(annotations[0])));
    }
    
    public void testPause() {
        Utilities.toggleBreakpoint(74);
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.runningStatusBarText);
        Utilities.sleep(5000);
        new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.pauseItem).toString(), null).perform();
        MainWindowOperator.getDefault().waitStatusText(Utilities.stoppedStatusBarText);
    }
   
    public void testFinishSession() {
        Utilities.endSession();
    }
}
