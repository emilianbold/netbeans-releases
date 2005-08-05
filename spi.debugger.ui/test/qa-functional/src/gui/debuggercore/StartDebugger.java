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
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;

public class StartDebugger extends JellyTestCase {
   
    public StartDebugger(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new StartDebugger("setupStartTests"));
        suite.addTest(new StartDebugger("testRunInDebugger"));
        suite.addTest(new StartDebugger("testDebugFile"));
        suite.addTest(new StartDebugger("testRunDebuggerStepInto"));
        suite.addTest(new StartDebugger("testRunDebuggerRunToCursor"));
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
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
    }
    
    public void setupStartTests() {
        Utilities.sleep(1000);
        Node projectNode = new Node(new JTreeOperator(new ProjectsTabOperator()), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        
        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        
        new Action(null, null, Utilities.buildProjectShortcut).performShortcut();
        Utilities.sleep(5000);
        MainWindowOperator.getDefault().waitStatusText(Utilities.buildCompleteStatusBarText);
    }
    
    public void testRunInDebugger() {
        Utilities.startDebugger(Utilities.runningStatusBarText);
    }
    
    public void testDebugFile() {
        new EditorOperator("MemoryView.java").grabFocus();
        new Action(null, null, Utilities.debugFileShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.runningStatusBarText);

        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.finishedStatusBarText);
    }
    
    public void testRunDebuggerStepInto() {
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.stepIntoItem).toString(), null).perform();
        new Action(null, null, Utilities.stepIntoShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:33.");
        
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.finishedStatusBarText);
    }
    
    public void testRunDebuggerRunToCursor() {
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        editorOperator.setCaretPosition(86, 1);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runToCursorItem).toString(), null).perform();
        new Action(null, null, Utilities.runToCursorShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:86.");
        
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.finishedStatusBarText);
    }
}
