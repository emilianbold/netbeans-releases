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
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;


public class Views extends JellyTestCase {
    
    public Views(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Views("testViewsOpen"));
        suite.addTest(new Views("testViewsCallStack"));
        suite.addTest(new Views("testViewsThreads"));
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
    
    /**
     *
     */
    public void testViewsOpen() {
        Node projectNode = new Node(new JTreeOperator(new ProjectsTabOperator()), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        
        Utilities.showLocalVariablesView();
        new TopComponentOperator(Utilities.localVarsViewTitle).close();
        Utilities.showWatchesView();
        new TopComponentOperator(Utilities.watchesViewTitle).close();
        Utilities.showCallStackView();
        new TopComponentOperator(Utilities.callStackViewTitle).close();
        Utilities.showClassesView();
        new TopComponentOperator(Utilities.classesViewTitle).close();
        Utilities.showBreakpointsView();
        new TopComponentOperator(Utilities.breakpointsViewTitle).close();
        Utilities.showSessionsView();
        new TopComponentOperator(Utilities.sessionsViewTitle).close();
        Utilities.showThreadsView();
        new TopComponentOperator(Utilities.threadsViewTitle).close();
        Utilities.showSourcesView();
        new TopComponentOperator(Utilities.sourcesViewTitle).close();
    }
    
    public void testViewsCallStack() {
        Utilities.setCaret(96, 1);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runToCursorItem).toString(), null).perform();
        new Action(null, null, Utilities.runToCursorShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:96.");
        Utilities.showCallStackView();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.callStackViewTitle));
        String value = Utilities.removeTags(jTableOperator.getValueAt(0,0).toString());
        if (!("MemoryView.updateStatus:96".equals(value)))
            assertTrue("Second level call stack is not MemoryView.updateStatus:96", false);
        value = Utilities.removeTags(jTableOperator.getValueAt(1,0).toString());
        if (!("MemoryView.updateConsumption:74".equals(value)))
            assertTrue("Second level call stack is not MemoryView.updateConsumption:74", false);
        value = Utilities.removeTags(jTableOperator.getValueAt(2,0).toString());
        if (!("MemoryView.main:110".equals(value)))
            assertTrue("Third level call stack is not MemoryView.main:110", false);
    }

    public void testViewsThreads() {
        Utilities.showThreadsView();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.threadsViewTitle));        
        TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "system").expand();
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "system|main").expand();
        if (!("system".equals(Utilities.removeTags(jTableOperator.getValueAt(0,0).toString()))))
            assertTrue("Thread group system is not shown in threads view", false);
        if (!("main".equals(Utilities.removeTags(jTableOperator.getValueAt(1,0).toString()))))
            assertTrue("Thread group main is not shown in threads view", false);
        if (!("main".equals(Utilities.removeTags(jTableOperator.getValueAt(2,0).toString()))))
            assertTrue("Thread main is not shown in threads view", false);
        if (!("Reference Handler".equals(Utilities.removeTags(jTableOperator.getValueAt(3,0).toString()))))
            assertTrue("Thread Reference Handler is not shown in threads view", false);
        if (!("Finalizer".equals(Utilities.removeTags(jTableOperator.getValueAt(4,0).toString()))))
            assertTrue("Thread Finalizer is not shown in threads view", false);
        if (!("Signal Dispatcher".equals(Utilities.removeTags(jTableOperator.getValueAt(5,0).toString()))))
            assertTrue("Thread Signal Dispatcher is not shown in threads view", false);
    }
}
