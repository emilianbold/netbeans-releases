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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;



/**
 *
 * @author ehucka, Revision Petr Cyhelsky
 */
public class LineBreakpointsTest extends DebuggerTestCase {

    private static String[] tests = new String[]{
        "testLineBreakpointCreation",
        "testLineBreakpointFunctionality",
        "testLineBreakpointFunctionalityAfterContinue",
        "testLineBreakpointFunctionalityInStaticMethod",
        "testLineBreakpointFunctionalityInInitializer",
        "testLineBreakpointFunctionalityInConstructor",
        "testLineBreakpointFunctionalityInInnerClass",
        "testLineBreakpointFunctionalityInSecondaryClass",
        "testConditionalLineBreakpointFunctionality",
        "testLineBreakpointActions",
        "testLineBreakpointsValidation"
    };

    //MainWindowOperator.StatusTextTracer stt = null;
    /**
     *
     * @param name
     */
    public LineBreakpointsTest(String name) {
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
        return createModuleTest(LineBreakpointsTest.class, tests);
    }

    /**
     *
     */
    @Override
    public void setUp() throws IOException {
        super.setUp();
        System.out.println("########  " + getName() + "  ####### ");
    }

    /**
     *
     */
    public void testLineBreakpointCreation() throws Throwable {
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
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 73);
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Line MemoryView.java:73", jTableOperator.getValueAt(0, 0).toString());
            eo = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(eo, 73, false);
            new EventTool().waitNoEvent(1000);
            jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals(0, jTableOperator.getRowCount());
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testLineBreakpointFunctionality() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 73);
            Utilities.startDebugger();
            try {
                Utilities.waitStatusText("Thread main stopped at MemoryView.java:73");
            } catch (Throwable e) {
                if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:73")) {
                    System.err.println(e.getMessage());
                    throw e;
                }
            }
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityAfterContinue() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 52);
            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:52");
            eo = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(eo, 74);
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:74");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityInStaticMethod() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 114);
            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:114");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityInInitializer() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 45);
            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:45");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityInConstructor() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 54);
            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:54");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityInInnerClass() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 123);
            Utilities.startDebugger();
            Utilities.waitStatusText("Thread Thread-0 stopped at MemoryView.java:123");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testLineBreakpointFunctionalityInSecondaryClass() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 154);
            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:154");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testConditionalLineBreakpointFunctionality() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 63);
            Utilities.toggleBreakpoint(eo, 64);

            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Line MemoryView.java:64", jTableOperator.getValueAt(1, 0).toString());
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(1, 0)).pushMenuNoBlock("Properties");
            NbDialogOperator dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);
            new JCheckBoxOperator(dialog, 0).changeSelection(true);
            new JEditorPaneOperator(dialog, 0).setText("i > 0");
            dialog.ok();
            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:63");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:63");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:64");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testLineBreakpointActions() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 102);
            
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Line MemoryView.java:102", jTableOperator.getValueAt(0, 0).toString());
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenuNoBlock("Properties");
            NbDialogOperator dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);

            String nothread = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_CB_Actions_Panel_Suspend_None");
            new JComboBoxOperator(dialog, 1).selectItem(nothread);
            String breakpointHitText = "Line breakpoint hit on {className}:{lineNumber}"; //noi18n
            new JTextFieldOperator(dialog, 4).setText(breakpointHitText);
            dialog.ok();
            Utilities.toggleBreakpoint(eo, 104);
            Utilities.startDebugger();            
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:104");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    public void testLineBreakpointsValidation() throws Throwable {
        try {
            int[] prelines = new int[]{33, 34, 37, 43, 49};
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //start debugging
            Utilities.startDebugger();
            Utilities.waitStatusText(Utilities.runningStatusBarText);
            //toggle breakpoints
            for (int i = 0; i < prelines.length; i++) {
                Utilities.toggleBreakpoint(eo, prelines[i]);
                Utilities.waitStatusText("Invalid LineBreakpoint MemoryView.java : " + prelines[i]);
            }
            int[] debuglines = new int[]{72, 81, 83, 95, 96, 105, 108, 125, 153};
            //toggle breakpoints
            for (int i = 0; i < debuglines.length; i++) {
                Utilities.toggleBreakpoint(eo, debuglines[i]);
                Utilities.waitStatusText("Invalid LineBreakpoint MemoryView.java : " + debuglines[i]);
            }
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
