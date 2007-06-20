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
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2005
 * All Rights Reserved.
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

package gui.debuggercore;

import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.NewBreakpointAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.junit.NbTestSuite;



/**
 *
 * @author ehucka
 */
public class Breakpoints extends JellyTestCase {

    //MainWindowOperator.StatusTextTracer stt = null;
    /**
     *
     * @param name
     */
    public Breakpoints(String name) {
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
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Breakpoints("testLineBreakpointCreation"));
        suite.addTest(new Breakpoints("testLineBreakpointFunctionality"));
        suite.addTest(new Breakpoints("testLineBreakpointFunctionalityAfterContinue"));
        suite.addTest(new Breakpoints("testLineBreakpointFunctionalityInStaticMethod"));
        suite.addTest(new Breakpoints("testLineBreakpointFunctionalityInInitializer"));
        suite.addTest(new Breakpoints("testLineBreakpointFunctionalityInConstructor"));
        suite.addTest(new Breakpoints("testLineBreakpointFunctionalityInInnerClass"));
        suite.addTest(new Breakpoints("testLineBreakpointFunctionalityInSecondaryClass"));
        suite.addTest(new Breakpoints("testConditionalLineBreakpointFunctionality"));
        suite.addTest(new Breakpoints("testLineBreakpointActions"));
        suite.addTest(new Breakpoints("testLineBreakpointsValidation"));
        suite.addTest(new Breakpoints("testLineBreakpointsHitCount"));

        suite.addTest(new Breakpoints("testMethodBreakpointPrefilledConstructor"));
        suite.addTest(new Breakpoints("testMethodBreakpointPrefilledMethod"));
        suite.addTest(new Breakpoints("testMethodBreakpointCreation"));
        suite.addTest(new Breakpoints("testMethodBreakpointFunctionalityInPrimaryClass"));
        suite.addTest(new Breakpoints("testMethodBreakpointFunctionalityInSecondClass"));
        suite.addTest(new Breakpoints("testMethodBreakpointFunctionalityOnAllMethods"));
        suite.addTest(new Breakpoints("testMethodBreakpointFunctionalityOnExit"));
        suite.addTest(new Breakpoints("testConditionalMethodBreakpointFunctionality"));
        suite.addTest(new Breakpoints("testMethodBreakpointsValidation"));

        suite.addTest(new Breakpoints("testClassBreakpointPrefilledInClass"));
        suite.addTest(new Breakpoints("testClassBreakpointPrefilledInInitializer"));
        suite.addTest(new Breakpoints("testClassBreakpointPrefilledInConstructor"));
        suite.addTest(new Breakpoints("testClassBreakpointPrefilledInMethod"));
        suite.addTest(new Breakpoints("testClassBreakpointPrefilledInSecondClass"));
        suite.addTest(new Breakpoints("testClassBreakpointCreation"));
        suite.addTest(new Breakpoints("testClassBreakpointFunctionalityOnPrimaryClass"));
        suite.addTest(new Breakpoints("testClassBreakpointFunctionalityOnSecondClass"));
        suite.addTest(new Breakpoints("testClassBreakpointFunctionalityWithFilter"));

        suite.addTest(new Breakpoints("testFieldBreakpointPrefilledValues"));
        suite.addTest(new Breakpoints("testFieldBreakpointCreation"));
        suite.addTest(new Breakpoints("testFieldBreakpointFunctionalityAccess"));
        suite.addTest(new Breakpoints("testFieldBreakpointFunctionalityModification"));
        suite.addTest(new Breakpoints("testConditionalFieldBreakpointFunctionality"));
        suite.addTest(new Breakpoints("testFieldBreakpointsValidation"));

        suite.addTest(new Breakpoints("testThreadBreakpointCreation"));
        suite.addTest(new Breakpoints("testThreadBreakpointFunctionality"));
        suite.addTest(new Breakpoints("testThreadBreakpointFunctionalityHitCount"));

        suite.addTest(new Breakpoints("testExceptionBreakpointCreation"));
        suite.addTest(new Breakpoints("testExceptionBreakpointFunctionality"));
        suite.addTest(new Breakpoints("testExceptionBreakpointMatchClasses"));
        suite.addTest(new Breakpoints("testExceptionBreakpointExcludeClasses"));
        suite.addTest(new Breakpoints("testExceptionBreakpointHitCount"));
        suite.addTest(new Breakpoints("testConditionalExceptionBreakpoint"));
        return suite;
    }

    /**
     *
     */
    public void setUp() {
        System.out.println("########  " + getName() + "  ####### ");
    }

    /**
     *
     */
    public void tearDown() {
        JemmyProperties.getCurrentOutput().printTrace("\nteardown\n");
        Utilities.endAllSessions();
        Utilities.deleteAllBreakpoints();
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
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:73", 0);
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
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:52", 0);
            eo = new EditorOperator("MemoryView.java");
            Utilities.toggleBreakpoint(eo, 74);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:74", 0);
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
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:114", 0);
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
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:45", 0);
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
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:54", 0);
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
            Utilities.waitDebuggerConsole("Thread Thread-0 stopped at MemoryView.java:123", 0);
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
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:154", 0);
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
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(1, 0)).pushMenuNoBlock("Customize");
            NbDialogOperator dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);
            new JCheckBoxOperator(dialog, 0).changeSelection(true);
            new JEditorPaneOperator(dialog, 0).setText("i > 0");
            dialog.ok();
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:63", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:63", lines + 1);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:64", lines + 1);
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
            Utilities.toggleBreakpoint(eo, 104);

            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Line MemoryView.java:102", jTableOperator.getValueAt(0, 0).toString());
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenuNoBlock("Customize");
            NbDialogOperator dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);

            String nothread = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_CB_Actions_Panel_Suspend_None");
            new JComboBoxOperator(dialog, 1).selectItem(nothread);
            String breakpointHitText = "Line breakpoint hit on {className}:{lineNumber}"; //noi18n
            new JTextFieldOperator(dialog, 4).setText(breakpointHitText);
            dialog.ok();
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Line breakpoint hit on examples.advanced.MemoryView:102", 0);
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:104", lines + 1);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    public void testLineBreakpointsValidation() throws Throwable {
        try {
            int[] prelines = new int[]{33, 34, 37, 43, 49};
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            for (int i = 0; i < prelines.length; i++) {
                Utilities.toggleBreakpoint(eo, prelines[i]);
            }
            //start debugging
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("User program running", 0);
            for (int i = 0; i < prelines.length; i++) {
                Utilities.waitDebuggerConsole("Invalid LineBreakpoint MemoryView.java : " + prelines[i], lines + 1);
            }
            int[] debuglines = new int[]{72, 81, 83, 95, 96, 105, 108, 122, 125, 153};
            //toggle breakpoints
            for (int i = 0; i < debuglines.length; i++) {
                Utilities.toggleBreakpoint(eo, debuglines[i]);
                Utilities.waitDebuggerConsole("Invalid LineBreakpoint MemoryView.java : " + debuglines[i], lines + 1);
            }
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    public void testLineBreakpointsHitCount() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 64);
            Utilities.toggleBreakpoint(eo, 65);
            Utilities.toggleBreakpoint(eo, 66);
            //set hit conditions
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenuNoBlock("Customize");
            NbDialogOperator dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);
            new JCheckBoxOperator(dialog, 1).changeSelection(true);
            new JComboBoxOperator(dialog, 0).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "ConditionsPanel.cbWhenHitCount.equals"));
            new JTextFieldOperator(dialog, 2).setText("45");
            dialog.ok();

            new JPopupMenuOperator(jTableOperator.callPopupOnCell(1, 0)).pushMenuNoBlock("Customize");
            dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);
            new JCheckBoxOperator(dialog, 1).changeSelection(true);
            new JComboBoxOperator(dialog, 0).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "ConditionsPanel.cbWhenHitCount.greater"));
            new JTextFieldOperator(dialog, 2).setText("48");
            dialog.ok();

            new JPopupMenuOperator(jTableOperator.callPopupOnCell(2, 0)).pushMenuNoBlock("Customize");
            dialog = new NbDialogOperator(Utilities.customizeBreakpointTitle);
            new JCheckBoxOperator(dialog, 1).changeSelection(true);
            new JComboBoxOperator(dialog, 0).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "ConditionsPanel.cbWhenHitCount.multiple"));
            new JTextFieldOperator(dialog, 2).setText("47");
            dialog.ok();

            //start debugging
            Utilities.startDebugger();
            //check values
            StringComparator comp = new StringComparator() {

                public boolean equals(String arg0, String arg1) {
                    return arg0.equals(arg1);
                }
            };
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:64", 0);
            Utilities.showDebuggerView(Utilities.localVarsViewTitle);
            jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
            int row = treeTableOperator.findCellRow("i", comp);
            org.openide.nodes.Node.Property property = (org.openide.nodes.Node.Property) treeTableOperator.getValueAt(row, 2);
            assertEquals("44", property.getValue());
            new ContinueAction().perform();

            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:66", lines + 1);
            Utilities.showDebuggerView(Utilities.localVarsViewTitle);
            jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
            row = treeTableOperator.findCellRow("i", comp);
            property = (org.openide.nodes.Node.Property) treeTableOperator.getValueAt(row, 2);
            assertEquals("46", property.getValue());
            new ContinueAction().perform();

            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:65", lines + 1);
            Utilities.showDebuggerView(Utilities.localVarsViewTitle);
            jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
            row = treeTableOperator.findCellRow("i", comp);
            property = (org.openide.nodes.Node.Property) treeTableOperator.getValueAt(row, 2);
            assertEquals("47", property.getValue());
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
            NbDialogOperator dialog = Utilities.newBreakpoint(53);
            setBreakpointType(dialog, "Method");
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JEditorPaneOperator(dialog, 0).getText());
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
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JEditorPaneOperator(dialog, 0).getText());
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
    public void testMethodBreakpointCreation() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(92);
            setBreakpointType(dialog, "Method");
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
    public void testMethodBreakpointFunctionalityInPrimaryClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(92);
            setBreakpointType(dialog, "Method");
            dialog.ok();
            Utilities.startDebugger();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:92", 0);
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
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:154", 0);
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
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:39", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:114", lines + 1);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:50", lines + 1);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:51", lines + 1);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:121", lines + 1);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:79", lines + 1);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:92", lines + 1);
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
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:50", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:76", lines + 1);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:79", lines + 1);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:109", lines + 1);
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
            new JEditorPaneOperator(dialog, 1).setText("UPDATE_TIME >= 1001");

            dialog.ok();
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle control line breakpoint
            Utilities.toggleBreakpoint(eo, 104);

            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:104", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:92", lines + 1);
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
            int lines = Utilities.waitDebuggerConsole("Not able to submit breakpoint MethodBreakpoint [examples.advanced.MemoryView]." + wrongname, 0);
            dialog = Utilities.newBreakpoint(104);
            setBreakpointType(dialog, "Method");
            wrongname = "wrong2";
            new JTextFieldOperator(dialog, 0).setText(wrongname);
            dialog.ok();
            lines = Utilities.waitDebuggerConsole("Not able to submit breakpoint MethodBreakpoint [examples.advanced.MemoryView]." + wrongname, lines + 1);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testClassBreakpointPrefilledInClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(37);
            assertTrue("Class breakpoint is not pre-selected", new JComboBoxOperator(dialog, 1).getSelectedItem().equals("Class"));
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JTextFieldOperator(dialog, 0).getText());
            dialog.cancel();
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testClassBreakpointPrefilledInInitializer() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(45);
            assertTrue("Class breakpoint is not pre-selected", new JComboBoxOperator(dialog, 1).getSelectedItem().equals("Class"));
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JTextFieldOperator(dialog, 0).getText());
            dialog.cancel();
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testClassBreakpointPrefilledInConstructor() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(51);
            setBreakpointType(dialog, "Class");
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JTextFieldOperator(dialog, 0).getText());
            dialog.cancel();
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testClassBreakpointPrefilledInMethod() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(80);
            setBreakpointType(dialog, "Class");
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JTextFieldOperator(dialog, 0).getText());
            dialog.cancel();
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testClassBreakpointPrefilledInSecondClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(154);
            setBreakpointType(dialog, "Class");
            assertEquals("Class Name was not set to correct value.", "examples.advanced.Helper", new JTextFieldOperator(dialog, 0).getText());
            dialog.cancel();
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testClassBreakpointCreation() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(73);
            setBreakpointType(dialog, "Class");
            dialog.ok();
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Class breakpoint was not created.", "Class MemoryView prepare / unload", jTableOperator.getValueAt(0, 0).toString());
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testClassBreakpointFunctionalityOnPrimaryClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(73);
            setBreakpointType(dialog, "Class");
            dialog.ok();
            new EventTool().waitNoEvent(500);
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped.", 0);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, lines + 1);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testClassBreakpointFunctionalityOnSecondClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(154);
            setBreakpointType(dialog, "Class");
            dialog.ok();
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped.", 0);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, lines + 1);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testClassBreakpointFunctionalityWithFilter() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(73);
            setBreakpointType(dialog, "Class");
            new JTextFieldOperator(dialog, 0).setText("examples.advanced.*");
            new JCheckBoxOperator(dialog, 0).changeSelection(true);
            new JTextFieldOperator(dialog, 1).setText("*.MemoryView");
            dialog.ok();

            new DebugProjectAction().perform();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            //Class breakpoint hit for class examples.advanced.Helper.");
            int lines = Utilities.waitDebuggerConsole("Class breakpoint hit for class examples.advanced.Helper", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Class breakpoint hit for class examples.advanced.MemoryView$1.", lines + 1);
            //Class breakpoint hit for class examples.advanced.MemoryView$1
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:121.", lines + 1);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, lines + 1);
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
            assertEquals("Class Name was not set to correct value.", "examples.advanced.MemoryView", new JTextFieldOperator(dialog, 0).getText());
            assertEquals("Field Name was not set to correct value.", "msgMemory", new JTextFieldOperator(dialog, 1).getText());
            dialog.cancel();
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testFieldBreakpointCreation() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
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
    public void testFieldBreakpointFunctionalityAccess() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Field_Breakpoint_Type_Access"));
            dialog.ok();
            Utilities.startDebugger();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:104.", 0);
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
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:45", 0);
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
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:109", 0);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:104", lines + 1);
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
            new JTextFieldOperator(dialog, 1).setText(wrongname);
            dialog.ok();

            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Not able to submit breakpoint FieldBreakpoint examples.advanced.MemoryView." + wrongname, 0);
            dialog = Utilities.newBreakpoint(36, 36);
            setBreakpointType(dialog, "Field");
            wrongname = "wrongname2";
            new JTextFieldOperator(dialog, 1).setText(wrongname);
            dialog.ok();
            Utilities.waitDebuggerConsole("Not able to submit breakpoint FieldBreakpoint examples.advanced.MemoryView." + wrongname, lines + 1);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testThreadBreakpointCreation() throws Throwable {
        try {
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
            int lines = Utilities.waitDebuggerConsole("Thread breakpoint hit by thread ", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread breakpoint hit by thread ", lines + 1);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, lines + 1);
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
            int lines = Utilities.waitDebuggerConsole("Thread breakpoint hit by thread ", 0);
            new ContinueAction().perform();
            int backline = lines;
            lines = Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, lines + 1);
            assertEquals("There were more than one hit of the breakpoint", backline, lines - 2);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testExceptionBreakpointCreation() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            setBreakpointType(dialog, "Exception");

            new JTextFieldOperator(dialog, 0).setText("java.lang.NullPointerException");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
            dialog.ok();
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Thread breakpoint was not created.", "Exception NullPointerException caught", jTableOperator.getValueAt(0, 0).toString());
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testExceptionBreakpointFunctionality() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            setBreakpointType(dialog, "Exception");
            new JTextFieldOperator(dialog, 0).setText("java.lang.ClassNotFoundException");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
            dialog.ok();
            Utilities.startDebugger();
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole("Thread main stopped at URLClassLoader.java", 0);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testExceptionBreakpointMatchClasses() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            setBreakpointType(dialog, "Exception");
            new JTextFieldOperator(dialog, 0).setText("java.lang.ClassNotFoundException");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
            new JCheckBoxOperator(dialog, 0).changeSelection(true);
            new JTextFieldOperator(dialog, 1).setText("java.lang.ClassLoader");
            dialog.ok();
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at ClassLoader.java", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at ClassLoader.java", lines + 1);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at ClassLoader.java", lines + 1);
            assertFalse("The debugger hit disabled breakpoint", Utilities.checkConsoleForText("Thread main stopped at URLClassLoader.java", 0));
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testExceptionBreakpointExcludeClasses() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            setBreakpointType(dialog, "Exception");
            new JTextFieldOperator(dialog, 0).setText("java.lang.ClassNotFoundException");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
            new JCheckBoxOperator(dialog, 0).changeSelection(true);
            new JTextFieldOperator(dialog, 2).setText("java.net.URLClassLoader");
            dialog.ok();
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at ClassLoader.java", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at ClassLoader.java", lines + 1);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at ClassLoader.java", lines + 1);
            assertFalse("The debugger hit disabled breakpoint", Utilities.checkConsoleForText("Thread main stopped at URLClassLoader.java", 0));
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testExceptionBreakpointHitCount() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            setBreakpointType(dialog, "Exception");
            new JTextFieldOperator(dialog, 0).setText("java.lang.ClassNotFoundException");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
            new JCheckBoxOperator(dialog, 2).changeSelection(true);
            new JComboBoxOperator(dialog, 3).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "ConditionsPanel.cbWhenHitCount.equals"));
            new JTextFieldOperator(dialog, 3).setText("1");
            dialog.ok();
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at ClassLoader.java", 0);
            new ContinueAction().perform();
            assertFalse("The debugger hit disabled breakpoint", Utilities.checkConsoleForText("Thread main stopped", lines + 1));
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }

    /**
     *
     */
    public void testConditionalExceptionBreakpoint() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            setBreakpointType(dialog, "Exception");
            new JTextFieldOperator(dialog, 0).setText("java.lang.ClassNotFoundException");
            new JComboBoxOperator(dialog, 2).selectItem(Bundle.getString("org.netbeans.modules.debugger.jpda.ui.breakpoints.Bundle", "LBL_Exception_Breakpoint_Type_Catched"));
            new JCheckBoxOperator(dialog, 1).changeSelection(true);
            new JEditorPaneOperator(dialog, 0).setText("false");
            dialog.ok();
            Utilities.startDebugger();
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, 0);
            assertFalse("The debugger hit disabled breakpoint", Utilities.checkConsoleForText("Thread main stopped", 0));
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
