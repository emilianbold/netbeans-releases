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
        suite.addTest(new Views("setupViewsTests"));
        suite.addTest(new Views("testViewsDefaultOpen"));
        suite.addTest(new Views("testViewsCallStack"));
        suite.addTest(new Views("testViewsClasses"));
        suite.addTest(new Views("testViewsThreads"));
        suite.addTest(new Views("testViewsSessions"));
        suite.addTest(new Views("testViewsSources"));
        suite.addTest(new Views("testViewsClose"));
        suite.addTest(new Views("finishViewsTests"));
        return suite;
    }
    
    /** setUp method  */
    public void setUp() {
        Utilities.sleep(1000);
        System.out.println("########  " + getName() + "  #######");
    }
    
    public void setupViewsTests() {
        Node projectNode = new Node(new JTreeOperator(new ProjectsTabOperator()), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
    }
    
    public void testViewsDefaultOpen() {
        //Utilities.toggleBreakpoint(86);
        //Utilities.startDebugger("Breakpoint hit at line 86 in class examples.advanced.MemoryView by thread main.");
        Utilities.sleep(1000);
        assertNotNull("Local variables view was not opened after debugger start", TopComponentOperator.findTopComponent(Utilities.localVarsViewTitle, 0));
        assertNotNull("Call stack view was not opened after debugger start", TopComponentOperator.findTopComponent(Utilities.callStackViewTitle, 0));
        assertNotNull("Watches view was not opened after debugger start", TopComponentOperator.findTopComponent(Utilities.watchesViewTitle, 0));
    }
    
    public void testViewsCallStack() {
        Utilities.showCallStackView();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.callStackViewTitle));
        String value = Utilities.removeTags(jTableOperator.getValueAt(0,0).toString());
        if (!("MemoryView.updateStatus:86".equals(value)))
            assertTrue("Top level call stack is not MemoryView.updateStatus:86", false);
        value = Utilities.removeTags(jTableOperator.getValueAt(1,0).toString());
        if (!("MemoryView.updateConsumption:74".equals(value)))
            assertTrue("Second level call stack is not MemoryView.updateConsumption:74", false);
        value = Utilities.removeTags(jTableOperator.getValueAt(2,0).toString());
        if (!("MemoryView.main:110".equals(value)))
            assertTrue("Third level call stack is not MemoryView.main:110", false);
    }
    
    public void testViewsClasses() {
        Utilities.showClassesView();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.classesViewTitle));        
        TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "System Class Loader").expand();
        Utilities.sleep(100);
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "Application Class Loader").expand();
        Utilities.sleep(100);
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "Application Class Loader|examples").expand();
        Utilities.sleep(100);
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "Application Class Loader|examples|advanced").expand();
        Utilities.sleep(100);
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "Application Class Loader|examples|advanced|MemoryView").expand();
        Utilities.sleep(100);
        String[] entries = {"System Class Loader", "java", "sun", "Application Class Loader", "examples", "advanced", "Helper", "MemoryView", "1"};
        for (int i = 0; i < entries.length; i++) {
            assertTrue("Node " + entries[i] + " not displayed in Classes view", entries[i].equals(Utilities.removeTags(treeTableOperator.getValueAt(i, 0).toString())));
        }
    }

    public void testViewsThreads() {
        Utilities.showThreadsView();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.threadsViewTitle));        
        TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "system").expand();
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "system|main").expand();
        assertTrue("Thread group system is not shown in threads view", "system".equals(Utilities.removeTags(jTableOperator.getValueAt(0,0).toString())));
        assertTrue("Thread group main is not shown in threads view", "main".equals(Utilities.removeTags(jTableOperator.getValueAt(1,0).toString())));
        assertTrue("Thread main is not shown in threads view", "main".equals(Utilities.removeTags(jTableOperator.getValueAt(2,0).toString())));
        assertTrue("Thread Reference Handler is not shown in threads view", "Reference Handler".equals(Utilities.removeTags(jTableOperator.getValueAt(3,0).toString())));
        assertTrue("Thread Finalizer is not shown in threads view", "Finalizer".equals(Utilities.removeTags(jTableOperator.getValueAt(4,0).toString())));
        assertTrue("Thread Signal Dispatcher is not shown in threads view", "Signal Dispatcher".equals(Utilities.removeTags(jTableOperator.getValueAt(5,0).toString())));
    }

    public void testViewsSessions() {
        Utilities.showSessionsView();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.sessionsViewTitle));
        assertTrue("Session is not shown in threads view", "examples.advanced.MemoryView".equals(Utilities.removeTags(jTableOperator.getValueAt(0,0).toString())));
    }

    public void testViewsSources() {
        Utilities.showSourcesView();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.sourcesViewTitle));
        if (jTableOperator.getRowCount() > 2)
            assertTrue("Too many sourceroots displayed.", false);
        else {
            String debugAppSource = "debugTestProject" + java.io.File.separator + "src (Project debugTestProject)";
            if (jTableOperator.getRowCount() == 1)
                assertTrue("MemoryView source root is not shown in threads view", Utilities.removeTags(jTableOperator.getValueAt(0,0).toString()).endsWith(debugAppSource));
            else {
                assertTrue("JDK source root is not shown in threads view", Utilities.removeTags(jTableOperator.getValueAt(0,0).toString()).endsWith("src.zip"));
                assertTrue("MemoryView source root is not shown in threads view", Utilities.removeTags(jTableOperator.getValueAt(1,0).toString()).endsWith(debugAppSource));
            }
        }
    }
    
    public void testViewsClose() {
        new TopComponentOperator(Utilities.localVarsViewTitle).close();
        new TopComponentOperator(Utilities.watchesViewTitle).close();
        new TopComponentOperator(Utilities.callStackViewTitle).close();
        new TopComponentOperator(Utilities.classesViewTitle).close();
        new TopComponentOperator(Utilities.sessionsViewTitle).close();
        new TopComponentOperator(Utilities.threadsViewTitle).close();
        new TopComponentOperator(Utilities.sourcesViewTitle).close();
    }
    
    public void finishViewsTests() {
        Utilities.endSession();
    }
}
