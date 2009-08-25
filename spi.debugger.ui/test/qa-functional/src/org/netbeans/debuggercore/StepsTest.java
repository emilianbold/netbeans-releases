/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.debuggercore;

import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.RunToCursorAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOutAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
/**
 *
 * @author felipee
 */
public class StepsTest extends DebuggerTestCase {

    private static String[] tests = new String[]{
        "testStepInto",
      /*  "testStepOver",
        "testRunToCursor",
        "testStepOut",
        "testStepOverExpression"*/
    };

    public StepsTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return createModuleTest(StepsTest.class, tests);
    }

    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }

public void testStepInto() throws Throwable {
        try {
            Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N            
            new EventTool().waitNoEvent(1000);
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(eo, 80);
            new DebugProjectAction().perform(projectNode);
            //wait for breakpoint
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:80");
            new StepIntoAction().perform();
            Thread.sleep(2000);
            assertTrue("CurrentPC annotation is not on line 92", Utilities.checkAnnotation(eo, 92, "CurrentPC"));
            assertTrue("Call Site annotation is not on line 80", Utilities.checkAnnotation(eo, 80, "CallSite"));
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

public void testStepOver() throws Throwable {
        try {
            Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N
            new Action(null, Utilities.setMainProjectAction).perform(new ProjectsTabOperator().getProjectRootNode(Utilities.testProjectName));
            new EventTool().waitNoEvent(1000);
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(eo, 80);
            new DebugProjectAction().perform(projectNode);
            //wait for breakpoint
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:80");
            new StepOverAction().performMenu();
            assertFalse("CurrentPC annotation remains on line 80", Utilities.checkAnnotation(eo, 80, "CurrentPC"));
            assertTrue("CurrentPC annotation is not on line 82", Utilities.checkAnnotation(eo, 82, "CurrentPC"));
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

 public void testRunToCursor() throws Throwable {
        try {
            Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N
            new Action(null, Utilities.setMainProjectAction).perform(new ProjectsTabOperator().getProjectRootNode(Utilities.testProjectName));
            new EventTool().waitNoEvent(1000);
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(eo, 80);
            new DebugProjectAction().perform(projectNode);
            //wait for breakpoint
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:80");
            Utilities.deleteAllBreakpoints(); //removes the breakpoint in the way of run to cursor
            Utilities.setCaret(eo, 109);
            //run to cursor
            new RunToCursorAction().performMenu();
            assertFalse("Current PC annotation remains on line 80", Utilities.checkAnnotation(eo, 80, "CurrentPC"));
            assertTrue("Current PC annotation is not on line 109", Utilities.checkAnnotation(eo, 109, "CurrentPC"));
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

  public void testStepOut() throws Throwable {
        try {
            Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N
            new Action(null, Utilities.setMainProjectAction).perform(new ProjectsTabOperator().getProjectRootNode(Utilities.testProjectName));
            new EventTool().waitNoEvent(1000);
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(eo, 94);
            new DebugProjectAction().perform(projectNode);
            //wait for breakpoint
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:94");
            new StepOutAction().performMenu();
            assertFalse("Current PC annotation remains on line 94", Utilities.checkAnnotation(eo, 94, "CurrentPC"));
            assertTrue("Current PC annotation is not on line 80", Utilities.checkAnnotation(eo, 80, "CurrentExpressionLine"));
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

   public void testStepOverExpression() throws Throwable {
        try {
            Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N
            new Action(null, Utilities.setMainProjectAction).perform(new ProjectsTabOperator().getProjectRootNode(Utilities.testProjectName));
            new EventTool().waitNoEvent(1000);
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(eo, 104);
            new DebugProjectAction().perform(projectNode);
            //wait for breakpoint
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:104");
            Utilities.toggleBreakpoint(eo, 104, false);
            new EventTool().waitNoEvent(500);
            String sOE = Utilities.runMenu + "|" + Utilities.stepOverExpresItem;
            new Action(Utilities.runMenu + "|" + "Toggle Line Breakpoint",null).performMenu();
            new Action(sOE, null).performMenu();
            assertTrue("CurrentExpressionLine annotation is not on line 105", Utilities.checkAnnotation(eo, 105, "CurrentExpressionLine"));
            new Action(sOE, null).performMenu();
            assertTrue("CurrentExpressionLine annotation is not on line 106", Utilities.checkAnnotation(eo, 106, "CurrentExpressionLine"));
            new Action(sOE, null).performMenu();
            assertTrue("CurrentExpressionLine annotation is not on line 107", Utilities.checkAnnotation(eo, 107, "CurrentExpressionLine"));
            new Action(sOE, null).performMenu();
            assertTrue("CurrentExpressionLine annotation is not on line 104", Utilities.checkAnnotation(eo, 104, "CurrentExpressionLine"));
            new Action(sOE, null).performMenu();
            assertTrue("CurrentExpressionLine annotation is not on line 104", Utilities.checkAnnotation(eo, 104, "CurrentExpressionLine"));
            new Action(sOE, null).performMenu();
            assertTrue("Current PC annotation is not on line 109", Utilities.checkAnnotation(eo, 109, "CurrentPC"));
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }


}
