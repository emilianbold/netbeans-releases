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
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.NbTestSuite;

public class StartDebugger extends JellyTestCase {
    
    public StartDebugger(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new StartDebugger("testDebugMainProject"));
        suite.addTest(new StartDebugger("testDebugProject"));
        suite.addTest(new StartDebugger("testDebugFile"));
        suite.addTest(new StartDebugger("testRunDebuggerStepInto"));
        suite.addTest(new StartDebugger("testRunDebuggerRunToCursor"));
        return suite;
    }
    
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }
    
    public void tearDown() {
        JemmyProperties.getCurrentOutput().printTrace("\nteardown\n");
        Utilities.endAllSessions();
    }
    
    public void testDebugMainProject() throws Throwable {
        try {
            new Action(Utilities.runMenu+"|"+Utilities.debugMainProjectItem, null).perform();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, 0);
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
    
    public void testDebugProject() throws Throwable {
        try {
            Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            new DebugProjectAction().perform(projectNode);
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, 0);
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
    
    public void testDebugFile() throws Throwable {
        try {
            //open source
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N
            EditorOperator eo = new EditorOperator("MemoryView.java");
            new Action(null, null, Utilities.debugFileShortcut).performShortcut();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, 0);
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
    
    public void testRunDebuggerStepInto() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            new Action(null, null, Utilities.stepIntoShortcut).performShortcut();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:", 0);
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
    
    public void testRunDebuggerRunToCursor() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.setCaret(eo, 75);
            new Action(null, null, Utilities.runToCursorShortcut).performShortcut();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:75", 0);
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
