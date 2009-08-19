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
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;



/**
 *
 * @author ehucka, Revision Petr Cyhelsky
 */
public class FieldBreakpointsTest extends JellyTestCase {

    private static String[] tests = new String[] {
        "testFieldBreakpointCreation",
        "testFieldBreakpointPrefilledValues",
        "testFieldBreakpointFunctionalityAccess",
        "testFieldBreakpointFunctionalityModification",
        "testConditionalFieldBreakpointFunctionality",
        "testFieldBreakpointsValidation" 
    };

    private static boolean initialized = false;

    /**
     *
     * @param name
     */
    public FieldBreakpointsTest(String name) {
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
        return createModuleTest(FieldBreakpointsTest.class, tests);
    }

    /**
     *
     */
    @Override
    public void setUp() throws IOException {        
        System.out.println("########  " + getName() + "  ####### ");
        if (!initialized)
        {
            openDataProjects(Utilities.testProjectName);
            Utilities.cleanBuildTestProject();
            initialized = true;
        }
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
    public void testFieldBreakpointCreation() throws Throwable {
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
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
            new JTextFieldOperator(dialog, 1).setText("examples.advanced.MemoryView");
            new JTextFieldOperator(dialog, 0).setText("msgMemory");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Field_Breakpoint_Type_Access"));
            dialog.ok();
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Field breakpoint was not created.", "Field MemoryView.msgMemory access", jTableOperator.getValueAt(0, 0).toString());
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testFieldBreakpointPrefilledValues() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
            new EventTool().waitNoEvent(500);
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JTextFieldOperator(dialog, 1).getText());
            assertEquals("Field Name was not set to correct value.", "msgMemory", new JTextFieldOperator(dialog, 0).getText());
            dialog.cancel();
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testFieldBreakpointFunctionalityAccess() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Field_Breakpoint_Type_Access"));
            new EventTool().waitNoEvent(500);
            dialog.ok();
            new EventTool().waitNoEvent(1500);
            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:104.");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testFieldBreakpointFunctionalityModification() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Field_Breakpoint_Type_Modification"));
            dialog.ok();
            Utilities.startDebugger();
            Utilities.waitStatusText("Thread main stopped at MemoryView.java:45");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    public void testConditionalFieldBreakpointFunctionality() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Field_Breakpoint_Type_Access"));
            new JCheckBoxOperator(dialog, 0).changeSelection(true);
            new JEditorPaneOperator(dialog, 0).setText("UPDATE_TIME >= 1001");
            dialog.ok();

            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 109);

            Utilities.startDebugger();
            try {
                Utilities.waitStatusText("Thread main stopped at MemoryView.java:109");
            } catch (Throwable e) {
                if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:109")) {
                    System.err.println(e.getMessage());
                    throw e;
                }
            }
            new ContinueAction().perform();
            try {
                Utilities.waitStatusText("Thread main stopped at MemoryView.java:104");
            } catch (Throwable e) {
                if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104")) {
                    System.err.println(e.getMessage());
                    throw e;
                }
            }
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    public void testFieldBreakpointsValidation() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
            String wrongname = "wrongname";
            new JTextFieldOperator(dialog, 0).setText(wrongname);
            dialog.ok();

            Utilities.startDebugger();
            Utilities.waitStatusText("Not able to submit breakpoint FieldBreakpoint examples.advanced.MemoryView.");
            dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
            wrongname = "wrongname2";
            new JTextFieldOperator(dialog, 0).setText(wrongname);
            dialog.ok();
            Utilities.waitStatusText("Not able to submit breakpoint FieldBreakpoint examples.advanced.MemoryView.");
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
