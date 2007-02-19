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

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.NewBreakpointAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.util.PNGEncoder;
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
        suite.addTest(new Breakpoints("testMethodBreakpointPrefilledConstructor"));
        //suite.addTest(new Breakpoints("testMethodBreakpointPrefilledInitializer"));
        suite.addTest(new Breakpoints("testMethodBreakpointPrefilledMethod"));
        suite.addTest(new Breakpoints("testMethodBreakpointCreation"));
        suite.addTest(new Breakpoints("testMethodBreakpointFunctionalityInPrimaryClass"));
        suite.addTest(new Breakpoints("testMethodBreakpointFunctionalityInSecondClass"));
        suite.addTest(new Breakpoints("testMethodBreakpointFunctionalityOnAllMethods"));
        suite.addTest(new Breakpoints("testClassBreakpointPrefilledInClass"));
        suite.addTest(new Breakpoints("testClassBreakpointPrefilledInInitializer"));
        suite.addTest(new Breakpoints("testClassBreakpointPrefilledInConstructor"));
        suite.addTest(new Breakpoints("testClassBreakpointPrefilledInMethod"));
        suite.addTest(new Breakpoints("testClassBreakpointPrefilledInSecondClass"));
        suite.addTest(new Breakpoints("testClassBreakpointCreation"));
        suite.addTest(new Breakpoints("testClassBreakpointFunctionalityOnPrimaryClass"));
        suite.addTest(new Breakpoints("testClassBreakpointFunctionalityOnSecondClass"));
        suite.addTest(new Breakpoints("testClassBreakpointFunctionalityWithFilter"));
        suite.addTest(new Breakpoints("testVariableBreakpointPrefilledValues"));
        suite.addTest(new Breakpoints("testVariableBreakpointCreation"));
        suite.addTest(new Breakpoints("testVariableBreakpointFunctionalityAccess"));
        suite.addTest(new Breakpoints("testVariableBreakpointFunctionalityModification"));
        suite.addTest(new Breakpoints("testThreadBreakpointCreation"));
        suite.addTest(new Breakpoints("testThreadBreakpointFunctionality"));
        suite.addTest(new Breakpoints("testExceptionBreakpointCreation"));
        suite.addTest(new Breakpoints("testExceptionBreakpointFunctionality"));
        return suite;
    }
    
    /**
     *
     */
    public void setUp() {
        System.out.print("########  " + getName() + "  ####### ");
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
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
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
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
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
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
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
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
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
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
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
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
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
            Utilities.toggleBreakpoint(eo, 122);
            Utilities.startDebugger();
            Utilities.waitDebuggerConsole("Thread Thread-0 stopped at MemoryView.java:122", 0);
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
    
    /**
     *
     */
    public void testLineBreakpointFunctionalityInSecondaryClass() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            //toggle breakpoints
            Utilities.toggleBreakpoint(eo, 153);
            Utilities.startDebugger();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:153", 0);
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
            new JEditorPaneOperator(dialog, 0).setText("i > 0");
            dialog.ok();
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:63", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:63", lines);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:64", lines);
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
    
    /**
     *
     */
    public void testMethodBreakpointPrefilledConstructor() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(53);
            new JComboBoxOperator(dialog, 0).selectItem("Method");
            assertEquals("Package Name was not set to correct value.", "examples.advanced", new JTextFieldOperator(dialog, 1).getText());
            assertEquals("Class Name was not set to correct value.", "MemoryView", new JTextFieldOperator(dialog, 2).getText());
            assertEquals("Method Name was not set to correct value.", "<init>", new JTextFieldOperator(dialog, 3).getText());
            dialog.cancel();
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
    
    /**
     *
     */
    public void testMethodBreakpointPrefilledMethod() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(92);
            new JComboBoxOperator(dialog, 0).selectItem("Method");
            assertEquals("Package Name was not set to correct value.", "examples.advanced", new JTextFieldOperator(dialog, 1).getText());
            assertEquals("Class Name was not set to correct value.", "MemoryView", new JTextFieldOperator(dialog, 2).getText());
            assertEquals("Method Name was not set to correct value.", "updateStatus", new JTextFieldOperator(dialog, 3).getText());
            dialog.cancel();
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
    
    /**
     *
     */
    public void testMethodBreakpointCreation() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(92);
            new JComboBoxOperator(dialog, 0).selectItem("Method");
            dialog.ok();
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Method MemoryView.updateStatus", jTableOperator.getValueAt(0, 0).toString());
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
    
    /**
     *
     */
    public void testMethodBreakpointFunctionalityInPrimaryClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(92);
            new JComboBoxOperator(dialog, 0).selectItem("Method");
            dialog.ok();
            Utilities.startDebugger();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:92", 0);
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
    
    /**
     *
     */
    public void testMethodBreakpointFunctionalityInSecondClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(153);
            new JComboBoxOperator(dialog, 0).selectItem("Method");
            dialog.ok();
            Utilities.startDebugger();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:153", 0);
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
    
    /**
     *
     */
    public void testMethodBreakpointFunctionalityOnAllMethods() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(37);
            new JComboBoxOperator(dialog, 0).selectItem("Method");
            dialog.ok();
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:39", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:113", lines);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:50", lines);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:51", lines);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:120", lines);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:79", lines);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:92", lines);
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
    
    /**
     *
     */
    public void testClassBreakpointPrefilledInClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(27);
            if (!new JComboBoxOperator(dialog, 0).getSelectedItem().equals("Class"))
                new JComboBoxOperator(dialog, 0).selectItem("Class");
            assertEquals("Package Name was not set to correct value.", "", new JTextFieldOperator(dialog, 0).getText());
            assertEquals("Class Name was not set to correct value.", "", new JTextFieldOperator(dialog, 1).getText());
            dialog.cancel();
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
    
    /**
     *
     */
    public void testClassBreakpointPrefilledInInitializer() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(38);
            if (!new JComboBoxOperator(dialog, 0).getSelectedItem().equals("Class"))
                new JComboBoxOperator(dialog, 0).selectItem("Class");
            assertEquals("Package Name was not set to correct value.", "examples.advanced", new JTextFieldOperator(dialog, 0).getText());
            assertEquals("Class Name was not set to correct value.", "MemoryView", new JTextFieldOperator(dialog, 1).getText());
            dialog.cancel();
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
    
    /**
     *
     */
    public void testClassBreakpointPrefilledInConstructor() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(45);
            if (!new JComboBoxOperator(dialog, 0).getSelectedItem().equals("Class"))
                new JComboBoxOperator(dialog, 0).selectItem("Class");
            assertEquals("Package Name was not set to correct value.", "examples.advanced", new JTextFieldOperator(dialog, 0).getText());
            assertEquals("Class Name was not set to correct value.", "MemoryView", new JTextFieldOperator(dialog, 1).getText());
            dialog.cancel();
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
    
    /**
     *
     */
    public void testClassBreakpointPrefilledInMethod() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(80);
            if (!new JComboBoxOperator(dialog, 0).getSelectedItem().equals("Class"))
                new JComboBoxOperator(dialog, 0).selectItem("Class");
            assertEquals("Package Name was not set to correct value.", "examples.advanced", new JTextFieldOperator(dialog, 0).getText());
            assertEquals("Class Name was not set to correct value.", "MemoryView", new JTextFieldOperator(dialog, 1).getText());
            dialog.cancel();
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
    
    /**
     *
     */
    public void testClassBreakpointPrefilledInSecondClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(153);
            if (!new JComboBoxOperator(dialog, 0).getSelectedItem().equals("Class"))
                new JComboBoxOperator(dialog, 0).selectItem("Class");
            assertEquals("Package Name was not set to correct value.", "examples.advanced", new JTextFieldOperator(dialog, 0).getText());
            assertEquals("Class Name was not set to correct value.", "Helper", new JTextFieldOperator(dialog, 1).getText());
            dialog.cancel();
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
    
    /**
     *
     */
    public void testClassBreakpointCreation() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(73);
            new JComboBoxOperator(dialog, 0).selectItem("Class");
            dialog.ok();
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Class breakpoint was not created.", "Class MemoryView prepare / unload", jTableOperator.getValueAt(0, 0).toString());
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
    
    /**
     *
     */
    public void testClassBreakpointFunctionalityOnPrimaryClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(73);
            new JComboBoxOperator(dialog, 0).selectItem("Class");
            dialog.ok();
            new EventTool().waitNoEvent(500);
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped.", 0);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, lines);
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
    
    /**
     *
     */
    public void testClassBreakpointFunctionalityOnSecondClass() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(153);
            new JComboBoxOperator(dialog, 0).selectItem("Class");
            dialog.ok();
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread main stopped.", 0);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, lines);
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
    
    /**
     *
     */
    public void testClassBreakpointFunctionalityWithFilter() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(73);
            new JComboBoxOperator(dialog, 0).selectItem("Class");
            new JTextFieldOperator(dialog, 1).setText("*");
            dialog.ok();
            
            new DebugProjectAction().perform();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            //Class breakpoint hit for class examples.advanced.Helper.");
            int lines = Utilities.waitDebuggerConsole("Thread main stopped.", 0);
            new EventTool().waitNoEvent(500);
            new ContinueAction().perform();
            //Class breakpoint hit for class examples.advanced.MemoryView
            lines = Utilities.waitDebuggerConsole("Thread main stopped.", lines);
            new ContinueAction().perform();
            //Class breakpoint hit for class examples.advanced.MemoryView$1
            lines = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:120.", lines);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, lines);
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
    
    /**
     *
     */
    public void testVariableBreakpointPrefilledValues() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            new JComboBoxOperator(dialog, 0).selectItem("Variable");
            assertEquals("Package Name was not set to correct value.", "examples.advanced", new JTextFieldOperator(dialog, 1).getText());
            assertEquals("Class Name was not set to correct value.", "MemoryView", new JTextFieldOperator(dialog, 2).getText());
            assertEquals("Variable Name was not set to correct value.", "msgMemory", new JTextFieldOperator(dialog, 3).getText());
            dialog.cancel();
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
    
    /**
     *
     */
    public void testVariableBreakpointCreation() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            new JComboBoxOperator(dialog, 0).selectItem("Variable");
            new JComboBoxOperator(dialog, 1).selectItem("Variable Access");
            dialog.ok();
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Variable breakpoint was not created.", "Variable MemoryView.msgMemory access", jTableOperator.getValueAt(0, 0).toString());
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
    
    /**
     *
     */
    public void testVariableBreakpointFunctionalityAccess() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            new JComboBoxOperator(dialog, 0).selectItem("Variable");
            new JComboBoxOperator(dialog, 1).selectItem("Variable Access");
            dialog.ok();
            Utilities.startDebugger();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:104.", 0);
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
    
    /**
     *
     */
    public void testVariableBreakpointFunctionalityModification() throws Throwable {
        try {
            NbDialogOperator dialog = Utilities.newBreakpoint(36, 36);
            new JComboBoxOperator(dialog, 0).selectItem("Variable");
            new JComboBoxOperator(dialog, 1).selectItem("Variable Modification");
            dialog.ok();
            Utilities.startDebugger();
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:45", 0);
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
    
    /**
     *
     */
    public void testThreadBreakpointCreation() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            new JComboBoxOperator(dialog, 0).selectItem("Thread");
            dialog.ok();
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Thread breakpoint was not created.", "Thread started", jTableOperator.getValueAt(0, 0).toString());
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
    
    /**
     *
     */
    public void testThreadBreakpointFunctionality() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            new JComboBoxOperator(dialog, 0).selectItem("Thread");
            dialog.ok();
            
            Utilities.startDebugger();
            int lines = Utilities.waitDebuggerConsole("Thread breakpoint hit by thread ", 0);
            new ContinueAction().perform();
            lines = Utilities.waitDebuggerConsole("Thread breakpoint hit by thread ", lines);
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, lines);
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
    
    /**
     *
     */
    public void testExceptionBreakpointCreation() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            new JComboBoxOperator(dialog, 0).selectItem("Exception");
            
            new JTextFieldOperator(dialog, 2).setText("java.lang");
            new JComboBoxOperator(dialog, 2).typeText("NullPointerException");
            new JComboBoxOperator(dialog, 1).selectItem("Caught or Uncaught");
            dialog.ok();
            Utilities.showDebuggerView(Utilities.breakpointsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
            assertEquals("Thread breakpoint was not created.", "Exception NullPointerException", jTableOperator.getValueAt(0, 0).toString());
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
    
    /**
     *
     */
    public void testExceptionBreakpointFunctionality() throws Throwable {
        try {
            new NewBreakpointAction().perform();
            NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
            new JComboBoxOperator(dialog, 0).selectItem("Exception");
            new JTextFieldOperator(dialog, 2).setText("java.lang");
            new JComboBoxOperator(dialog, 2).typeText("ClassNotFoundException");
            new JComboBoxOperator(dialog, 1).selectItem("Caught or Uncaught");
            dialog.ok();
            Utilities.startDebugger();
            new ContinueAction().perform();
            Utilities.waitDebuggerConsole("Thread main stopped at URLClassLoader.java", 0);
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
