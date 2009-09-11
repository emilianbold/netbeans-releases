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



/**
 *
 * @author ehucka, Revision Petr Cyhelsky
 */
public class MethodBreakpointsTest extends DebuggerTestCase {

    private static String[] tests = new String[]{
        "testMethodBreakpointCreation",
       /* "testMethodBreakpointPrefilledConstructor",
        "testMethodBreakpointPrefilledMethod",
        "testMethodBreakpointFunctionalityInPrimaryClass",
        "testMethodBreakpointFunctionalityInSecondClass",*/
        "testMethodBreakpointFunctionalityOnAllMethods",
        /*"testMethodBreakpointFunctionalityOnExit",
        "testConditionalMethodBreakpointFunctionality",
        "testMethodBreakpointsValidation"*/
    };
    
    //MainWindowOperator.StatusTextTracer stt = null;
    /**
     *
     * @param name
     */
    public MethodBreakpointsTest(String name) {
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
        return createModuleTest(MethodBreakpointsTest.class, tests);
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
    public void testMethodBreakpointCreation() throws Throwable {
        try {
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java");
            try {
                eo.clickMouse(50,50,1);
            } catch (Throwable t) {
                System.err.println(t.getMessage());
            }
            NbDialogOperator dialog = Utilities.newBreakpoint(92);
            setBreakpointType(dialog, "Method");
            new JTextFieldOperator(dialog, 1).setText("examples.advanced.MemoryView");
            new JTextFieldOperator(dialog, 0).setText("updateStatus()");
            dialog.ok();
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Method MemoryView.updateStatus", jTableOperator.getValueAt(0, 0).toString());
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }
    
    /**
     *
     */
    public void testMethodBreakpointPrefilledConstructor() throws Throwable {
        try {
            //open source
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java");
            NbDialogOperator dialog = Utilities.newBreakpoint(53);            
            setBreakpointType(dialog, "Method");
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JTextFieldOperator(dialog, 1).getText());
            assertEquals("Method Name was not set to correct value.", "MemoryView ()", new JTextFieldOperator(dialog, 0).getText());
            dialog.cancel();
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testMethodBreakpointPrefilledMethod() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(92);
            setBreakpointType(dialog, "Method");
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JTextFieldOperator(dialog, 1).getText());
            assertEquals("Method Name was not set to correct value.", "updateStatus ()", new JTextFieldOperator(dialog, 0).getText());
            dialog.cancel();
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testMethodBreakpointFunctionalityInPrimaryClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(92);
            setBreakpointType(dialog, "Method");            
            dialog.ok();
            try {
                Utilities.startDebugger();
            } catch (Throwable th) {
                new EventTool().waitNoEvent(500);
                dialog.ok();
                new EventTool().waitNoEvent(1500);
                Utilities.startDebugger();
            }
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:92");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testMethodBreakpointFunctionalityInSecondClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(154);
            setBreakpointType(dialog, "Method");
            dialog.ok();
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
    public void testMethodBreakpointFunctionalityOnAllMethods() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(37);
            setBreakpointType(dialog, "Method");
            dialog.ok();
            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:39");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:114");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:50");            
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:121");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:32");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:79");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:92");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    public void testMethodBreakpointFunctionalityOnExit() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(54);
            setBreakpointType(dialog, "Method");
            new JComboBoxOperator(dialog, 2).setSelectedItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Method_Breakpoint_Type_Entry_or_Exit")); //method entry
            dialog.ok();

            dialog = Utilities.newBreakpoint(80);
            setBreakpointType(dialog, "Method");
            new JComboBoxOperator(dialog, 2).setSelectedItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Method_Breakpoint_Type_Entry")); //method entry
            dialog.ok();

            dialog = Utilities.newBreakpoint(102);
            setBreakpointType(dialog, "Method");
            new JComboBoxOperator(dialog, 2).setSelectedItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Method_Breakpoint_Type_Exit")); //method entry
            dialog.ok();

            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:50");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:76");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:79");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:109");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    public void testConditionalMethodBreakpointFunctionality() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(104);
            setBreakpointType(dialog, "Method");
            new JComboBoxOperator(dialog, 2).setSelectedItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Method_Breakpoint_Type_Entry")); //method entry
            new JCheckBoxOperator(dialog, 1).changeSelection(true);
            new JEditorPaneOperator(dialog, 0).setText("UPDATE_TIME >= 1001");

            dialog.ok();
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle control line breakpoint
            Utilities.toggleBreakpoint(eo, 104);

            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:104");
            new ContinueAction().perform();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:92");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    public void testMethodBreakpointsValidation() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(104);
            setBreakpointType(dialog, "Method");
            String wrongname = "wrong";
            new JTextFieldOperator(dialog, 0).setText(wrongname);
            dialog.ok();

            Utilities.startDebugger();
            Utilities.waitStatusText("Not able to submit breakpoint MethodBreakpoint [examples.advanced.MemoryView]." + wrongname);
            dialog = Utilities.newBreakpoint(104);
            setBreakpointType(dialog, "Method");
            wrongname = "wrong2";
            new JTextFieldOperator(dialog, 0).setText(wrongname);
            dialog.ok();
            Utilities.waitStatusText("Not able to submit breakpoint MethodBreakpoint [examples.advanced.MemoryView]." + wrongname);
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
