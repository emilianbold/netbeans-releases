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

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.NbTestSuite;


public class Views extends JellyTestCase {
    
    public Views(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Views("testViewsDefaultOpen"));
        suite.addTest(new Views("testViewsCallStack"));
        suite.addTest(new Views("testViewsClasses"));
        suite.addTest(new Views("testViewsThreads"));
        suite.addTest(new Views("testViewsSessions"));
        suite.addTest(new Views("testViewsSources"));
        suite.addTest(new Views("testViewsClose"));
        return suite;
    }
    
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
        if ("testViewsDefaultOpen".equals(getName())) {
            //open source
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N
            EditorOperator op = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(op, 92);
            Utilities.startDebugger();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:92", 0);
        }
    }
    
    public void tearDown() {
        JemmyProperties.getCurrentOutput().printTrace("\nteardown\n");
        if ("testViewsClose".equals(getName())) {
            Utilities.endAllSessions();
            Utilities.deleteAllBreakpoints();
        }
    }
    
    public void testViewsDefaultOpen() throws Throwable {
        try {
            assertNotNull("Local variables view was not opened after debugger start", TopComponentOperator.findTopComponent(Utilities.localVarsViewTitle, 0));
            assertNotNull("Call stack view was not opened after debugger start", TopComponentOperator.findTopComponent(Utilities.callStackViewTitle, 0));
            assertNotNull("Watches view was not opened after debugger start", TopComponentOperator.findTopComponent(Utilities.watchesViewTitle, 0));
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    public void testViewsCallStack() throws Throwable {
        try {
            Utilities.showDebuggerView(Utilities.callStackViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.callStackViewTitle));
            assertEquals("MemoryView.updateStatus:92", Utilities.removeTags(jTableOperator.getValueAt(0,0).toString()));
            assertEquals("MemoryView.updateConsumption:80", Utilities.removeTags(jTableOperator.getValueAt(1,0).toString()));
            assertEquals("MemoryView.main:117", Utilities.removeTags(jTableOperator.getValueAt(2,0).toString()));
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    public void testViewsClasses() throws Throwable {
        try {
            Utilities.showDebuggerView(Utilities.classesViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.classesViewTitle));
            TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
            new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "Application Class Loader|examples.advanced|MemoryView|1").expand();
            String[] entries = {"System Class Loader", "Application Class Loader", "examples.advanced", "Helper", "MemoryView", "1"};
            for (int i = 0; i < entries.length; i++) {
                assertTrue("Node " + entries[i] + " not displayed in Classes view", entries[i].equals(Utilities.removeTags(treeTableOperator.getValueAt(i, 0).toString())));
            }
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    public void testViewsThreads() throws Throwable {
        try {
            Utilities.showDebuggerView(Utilities.threadsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.threadsViewTitle));
            assertTrue("Thread group system is not shown in threads view", "system".equals(Utilities.removeTags(jTableOperator.getValueAt(0,0).toString())));
            assertTrue("Thread group main is not shown in threads view", "main".equals(Utilities.removeTags(jTableOperator.getValueAt(1,0).toString())));
            assertTrue("Thread main is not shown in threads view", "main".equals(Utilities.removeTags(jTableOperator.getValueAt(2,0).toString())));
            assertTrue("Thread Reference Handler is not shown in threads view", "Reference Handler".equals(Utilities.removeTags(jTableOperator.getValueAt(3,0).toString())));
            assertTrue("Thread Finalizer is not shown in threads view", "Finalizer".equals(Utilities.removeTags(jTableOperator.getValueAt(4,0).toString())));
            assertTrue("Thread Signal Dispatcher is not shown in threads view", "Signal Dispatcher".equals(Utilities.removeTags(jTableOperator.getValueAt(5,0).toString())));
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    public void testViewsSessions() throws Throwable {
        try {
            Utilities.showDebuggerView(Utilities.sessionsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.sessionsViewTitle));
            assertEquals("examples.advanced.MemoryView", Utilities.removeTags(jTableOperator.getValueAt(0,0).toString()));
            try {
                org.openide.nodes.Node.Property property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(0,1);
                assertEquals("Stopped", Utilities.removeTags(property.getValue().toString()));
                property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(0,2);
                assertEquals("org.netbeans.api.debugger.Session localhost:examples.advanced.MemoryView", Utilities.removeTags(property.getValue().toString()));
            } catch (Exception ex) {
                ex.printStackTrace();
                assertTrue(ex.getClass()+": "+ex.getMessage(), false);
            }
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    public void testViewsSources() throws Throwable {
        try {
            Utilities.showDebuggerView(Utilities.sourcesViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.sourcesViewTitle));
            String debugAppSource = "debugTestProject" + java.io.File.separator + "src (Project debugTestProject)";
            boolean jdk = false, project = false;
            for (int i=0;i < jTableOperator.getRowCount();i++) {
                String src = Utilities.removeTags(jTableOperator.getValueAt(i,0).toString());
                if (src.endsWith("src.zip")) {
                    jdk=true;
                } else if (src.endsWith(debugAppSource)) {
                    project = true;
                }
            }
            assertTrue("JDK source root is not shown in threads view", jdk);
            assertTrue("MemoryView source root is not shown in threads view", project);
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    public void testViewsClose() throws Throwable {
        try {
            //new TopComponentOperator(Utilities.localVarsViewTitle).close();
            //new TopComponentOperator(Utilities.watchesViewTitle).close();
            //new TopComponentOperator(Utilities.callStackViewTitle).close();
            new TopComponentOperator(Utilities.classesViewTitle).close();
            new TopComponentOperator(Utilities.sessionsViewTitle).close();
            new TopComponentOperator(Utilities.threadsViewTitle).close();
            new TopComponentOperator(Utilities.sourcesViewTitle).close();
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
}
