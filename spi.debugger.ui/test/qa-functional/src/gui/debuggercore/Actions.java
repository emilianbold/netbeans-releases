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

import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
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
        suite.addTest(new Actions("testRemoveBreakpoint"));
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
    
    /** setUp method  */
    public void setUp() {
        Utilities.sleep(1000);
        System.out.println("########  " + getName() + "  #######");
    }
    
    public void setupActionsTests() {
        Node projectNode = new Node(new JTreeOperator(new ProjectsTabOperator()), Utilities.testProjectName);
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
    
    public void testRemoveBreakpoint() {
        Utilities.toggleBreakpoint(110);
        EditorOperator ed = new EditorOperator("MemoryView.java");
        Object[] annotations = ed.getAnnotations(110);
        if (annotations.length > 1)
            fail("Breakpoint annotation was not removed");
        assertTrue("Program counter annotation is not displayed", "Current Program Counter".equals(ed.getAnnotationShortDescription(annotations[0])));
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
        Object[] annotations = ed.getAnnotations(74);
        assertTrue("Call stack line annotation was not removed", annotations.length == 0);
        annotations = ed.getAnnotations(76);
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
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped");
    }
   
    public void testFinishSession() {
        Utilities.endSession();
    }
}
