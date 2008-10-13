/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2005
 * All Rights Reserved.
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
 * Contributor(s): Sun Microsystems, Inc.
 */

package org.netbeans.debuggercore;

import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.NewBreakpointAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;



/**
 *
 * @author ehucka, Revision Petr Cyhelsky
 */
public class ThreadBreakpointsTest extends JellyTestCase {

    //MainWindowOperator.StatusTextTracer stt = null;
    /**
     *
     * @param name
     */
    public ThreadBreakpointsTest(String name) {
        super(name);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     *
     * @return
     */
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ThreadBreakpointsTest.class).addTest(
                    "testThreadBreakpointCreation",
                    "testThreadBreakpointFunctionality",
                    "testThreadBreakpointFunctionalityHitCount"
                )
            .enableModules(".*").clusters(".*"));
    }

    /**
     *
     */
    @Override
    public void setUp() throws IOException {
        openDataProjects(Utilities.testProjectName);
        System.out.println("########  " + getName() + "  ####### ");
    }

    /**
     *
     */
    @Override
    public void tearDown() {
        JemmyProperties.getCurrentOutput().printTrace("\nteardown\n");
        Utilities.endAllSessions();
        Utilities.deleteAllBreakpoints();
    }

    /**
     *
     */
    public void testThreadBreakpointCreation() throws Throwable {
        try {
            //open source
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java");
            try {
                eo.clickMouse(50,50,1);
            } catch (Throwable t) {
                System.err.println(t.getMessage());
            }
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            setBreakpointType(dialog, "Thread");
            dialog.ok();
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Thread breakpoint was not created.", "Thread started", jTableOperator.getValueAt(0, 0).toString());
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testThreadBreakpointFunctionality() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            setBreakpointType(dialog, "Thread");
            dialog.ok();

            Utilities.startDebugger();
            try {
                Utilities.waitStatusText("Thread breakpoint hit by thread ");
            } catch (TimeoutExpiredException e) {
                System.err.println(e.getMessage());
                Utilities.checkConsoleForText("Thread breakpoint hit by thread ", 1);
            }
            new ContinueAction().perform();
            try {
                Utilities.waitStatusText("Thread breakpoint hit by thread ");
            } catch (TimeoutExpiredException e) {
                System.err.println(e.getMessage());
                Utilities.checkConsoleForText("Thread breakpoint hit by thread ", 3);
            }
            new ContinueAction().perform();
            Utilities.waitStatusText(Utilities.runningStatusBarText);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testThreadBreakpointFunctionalityHitCount() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            setBreakpointType(dialog, "Thread");
            dialog.ok();

            Utilities.startDebugger();
            Utilities.waitStatusText("Thread breakpoint hit by thread ");
            new ContinueAction().perform();
            Utilities.waitStatusText(Utilities.runningStatusBarText);
            assertEquals("There were more than one hit of the breakpoint", Utilities.checkConsoleForNumberOfOccurrences(Utilities.runningStatusBarText, 0), 2);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    protected void setBreakpointType(NbDialogOperator dialog, String type) {
        new JComboBoxOperator(dialog, 0).selectItem("Java");
        new JComboBoxOperator(dialog, 1).selectItem(type);
    }
}
