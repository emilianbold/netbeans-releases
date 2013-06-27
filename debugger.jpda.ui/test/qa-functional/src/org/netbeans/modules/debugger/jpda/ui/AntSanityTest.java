/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.ui;

import java.io.IOException;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.BuildJavaProjectAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.modules.debugger.actions.RunToCursorAction;
import org.netbeans.jellytools.modules.debugger.actions.PauseAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.openide.util.Exceptions;

/**
 * Set of basic tests to make sure that debugging of Ant based Java projects is
 * not seriously broken.
 *
 * @author Jiri Kovalsky
 */
public class AntSanityTest extends JellyTestCase {

    /**
     * Array of test names to be executed in this test suite.
     */
    public static String[] testNames = new String[]{
        "openDebugProject",
        "startDebuggingF7",
        "pause",
        "testLineBreakpoint",
        "runToCursor"
    };
    
    /**
     * Name of tested project root node.
     */
    private static final String DEBUG_TEST_PROJECT_ANT = "debugTestProjectAnt";

    /**
     * Constructor required by JUnit.
     *
     * @param testName Method name to be used as test case.
     */
    public AntSanityTest(String testName) {
        super(testName);
    }

    /**
     * Returns suite of tests to be executed.
     *
     * @return Suite with test cases to be executed by JUnit.
     */
    public static Test suite() {
        return createModuleTest(AntSanityTest.class, testNames);
    }

    /**
     * Sets up IDE before each test case.
     */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    /**
     * Opens and builds Ant based test project for the next tests.
     *
     * @throws IOException Exception thrown if opening project failed.
     */
    public void openDebugProject() throws IOException {
        openDataProjects(DEBUG_TEST_PROJECT_ANT);
        new EventTool().waitNoEvent(1000);
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(DEBUG_TEST_PROJECT_ANT);
        new BuildJavaProjectAction().perform(projectNode);
        stt.waitText("Finished building " + DEBUG_TEST_PROJECT_ANT + " (clean,jar).");
        stt.stop();
    }

    /**
     * Starts debugging session by stepping into main method.
     */
    public void startDebuggingF7() {
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(DEBUG_TEST_PROJECT_ANT);
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        new StepIntoAction().perform(projectNode);
        stt.waitText("Thread main stopped at MemoryView.java:276.");
        stt.stop();
        assertEquals(new EditorOperator("MemoryView.java").getLineNumber(), 276);
    }
    
    /**
     * Only pauses debugging session.
     */
    public void pause() {
        new PauseAction().perform();
        TopComponentOperator debuggingView = new TopComponentOperator("Debugging");
        JTreeOperator threads = new JTreeOperator(debuggingView);
        TreePath firstThread = threads.getPathForRow(0);
        assertTrue(firstThread.toString().equals("[, 'Finalizer' suspended at 'Object.wait']"));
    }

    /**
     * Sets line 278 breakpoint and continues with debugging.
     */
    public void testLineBreakpoint() {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        eo.setCaretPositionToLine(278);
        new ToggleBreakpointAction().perform();
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        new ContinueAction().perform();
        stt.waitText("Thread main stopped at MemoryView.java:278.");
        stt.stop();
        assertEquals(new EditorOperator("MemoryView.java").getLineNumber(), 278);
    }
    
    /**
     * Sets line 280 breakpoint, disables it and then continues to run to cursor at line 282.
     */
    public void runToCursor() {
        EditorOperator eo = new EditorOperator("MemoryView.java");
        eo.setCaretPositionToLine(280);
        new ToggleBreakpointAction().perform();
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        jTableOperator.waitCell("Line MemoryView.java:280", 1, 0);
        new JPopupMenuOperator(jTableOperator.callPopupOnCell(1, 0)).pushMenu("Disable");
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        eo.makeComponentVisible();
        eo.setCaretPositionToLine(282);
        new RunToCursorAction().perform();
        stt.waitText("Thread main stopped at MemoryView.java:282.");
        stt.stop();
        assertEquals(new EditorOperator("MemoryView.java").getLineNumber(), 282);
    }    
}