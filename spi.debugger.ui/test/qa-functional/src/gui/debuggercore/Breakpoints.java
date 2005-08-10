/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * The Original Code is NetBeans.
 * The Initial Developer of the Original Code is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

package gui.debuggercore;

import java.awt.event.KeyEvent;

import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.NbTestSuite;

public class Breakpoints extends JellyTestCase {
    
    public Breakpoints(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Breakpoints("setupBreakpointsTests"));
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
        suite.addTest(new Breakpoints("testMethodBreakpointPrefilledInitializer"));
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
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** setUp method  */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }
    
    /** tearDown method */
    public void tearDown() {
        try {
            PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+java.io.File.separator+"screenBeforeTearDown.png");
        } catch (java.io.IOException ex) {}
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        new Action(null, null, Utilities.killSessionShortcut).performShortcut();
        Utilities.deleteAllBreakpoints();
    }
    
    public void setupBreakpointsTests() {
        Utilities.sleep(1000);
        Node projectNode = new Node(new JTreeOperator(new ProjectsTabOperator()), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        
        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        
        new Action(null, null, Utilities.buildProjectShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.buildCompleteStatusBarText);

        Utilities.showBreakpointsView();
    }
    
    public void testLineBreakpointCreation() {
        Utilities.toggleBreakpoint(73);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        if (!"Line MemoryView.java:73".equals(jTableOperator.getValueAt(0, 0).toString()) )
            assertTrue("Line breakpoint was not created.", false);
    }
    
    public void testLineBreakpointFunctionality() {
        Utilities.toggleBreakpoint(73);
        Utilities.startDebugger("Breakpoint hit at line 73 in class examples.advanced.MemoryView by thread main.");
    }
    
    public void testLineBreakpointFunctionalityAfterContinue() {
        Utilities.toggleBreakpoint(73);
        Utilities.startDebugger("Breakpoint hit at line 73 in class examples.advanced.MemoryView by thread main.");
        Utilities.toggleBreakpoint(91);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Breakpoint hit at line 91 in class examples.advanced.MemoryView by thread main.");
    }
    
    public void testLineBreakpointFunctionalityInStaticMethod() {
        Utilities.toggleBreakpoint(107);
        Utilities.startDebugger("Breakpoint hit at line 107 in class examples.advanced.MemoryView by thread main.");
    }
    
    public void testLineBreakpointFunctionalityInInitializer() {
        Utilities.toggleBreakpoint(38);
        Utilities.startDebugger("Breakpoint hit at line 38 in class examples.advanced.MemoryView by thread main.");
    }
    
    public void testLineBreakpointFunctionalityInConstructor() {
        Utilities.toggleBreakpoint(45);
        Utilities.startDebugger("Breakpoint hit at line 45 in class examples.advanced.MemoryView by thread main.");
    }
    
    public void testLineBreakpointFunctionalityInInnerClass() {
        Utilities.toggleBreakpoint(116);
        Utilities.startDebugger("Breakpoint hit at line 116 in class examples.advanced.MemoryView$1 by thread Thread-0.");
    }
    
    public void testLineBreakpointFunctionalityInSecondaryClass() {
        Utilities.toggleBreakpoint(147);
        Utilities.startDebugger("Breakpoint hit at line 147 in class examples.advanced.Helper by thread main.");
    }
    
    public void testConditionalLineBreakpointFunctionality() {
        Utilities.toggleBreakpoint(62);
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        if ("Line MemoryView.java:62".equals(jTableOperator.getValueAt(0, 0).toString()) ) {
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenuNoBlock("Customize");
            NbDialogOperator dialog = new NbDialogOperator("Customize Breakpoint");
            new JTextFieldOperator(dialog, 0).setText("i > 10");
            dialog.ok();
            Utilities.sleep(1000);
        } else
            assertTrue("Line breakpoint was not created.", false);
        Utilities.startDebugger("Thread main stopped at MemoryView.java:62.");
    }

    public void testMethodBreakpointPrefilledConstructor() {
        NbDialogOperator dialog = Utilities.newBreakpoint(45, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Method");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 1).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 2).getText()));
        assertTrue("Method Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 3).getText()));
        dialog.cancel();
    }
    
    public void testMethodBreakpointPrefilledInitializer() {
        NbDialogOperator dialog = Utilities.newBreakpoint(38, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Method");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 1).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 2).getText()));
        assertTrue("Method Name was not set to correct value.", "<init>".equals(new JTextFieldOperator(dialog, 3).getText()));
        dialog.cancel();
    }
    
    public void testMethodBreakpointPrefilledMethod() {
        NbDialogOperator dialog = Utilities.newBreakpoint(89, 1);
        Utilities.sleep(1000);
        new JComboBoxOperator(dialog, 0).selectItem("Method");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 1).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 2).getText()));
        assertTrue("Method Name was not set to correct value.", "updateStatus".equals(new JTextFieldOperator(dialog, 3).getText()));
        dialog.cancel();
    }
    
    public void testMethodBreakpointCreation() {
        NbDialogOperator dialog = Utilities.newBreakpoint(89, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Method");
        Utilities.sleep(1000);
        dialog.ok();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        if (!"Method MemoryView.updateStatus".equals(jTableOperator.getValueAt(0, 0).toString()) )
            assertTrue("Method breakpoint was not created.", false);
    }

    public void testMethodBreakpointFunctionalityInPrimaryClass() {
        NbDialogOperator dialog = Utilities.newBreakpoint(89, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Method");
        Utilities.sleep(1000);
        dialog.ok();
        Utilities.startDebugger("Method breakpoint hit in examples.advanced.MemoryView.updateStatus at line 86 by thread main.");
    }
    
    public void testMethodBreakpointFunctionalityInSecondClass() {
        NbDialogOperator dialog = Utilities.newBreakpoint(147, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Method");
        Utilities.sleep(1000);
        dialog.ok();
        Utilities.sleep(1000);
        Utilities.startDebugger("Method breakpoint hit in examples.advanced.Helper.test at line 147 by thread main.");
    }
    
    public void testMethodBreakpointFunctionalityOnAllMethods() {
        NbDialogOperator dialog = Utilities.newBreakpoint(77, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Method");
        Utilities.sleep(1000);
        new JCheckBoxOperator(dialog, 1).setSelected(true);
        Utilities.sleep(1000);
        dialog.ok();
        Utilities.sleep(1000);
        Utilities.startDebugger("Method breakpoint hit in examples.advanced.MemoryView.<clinit> at line 33 by thread main.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Method breakpoint hit in examples.advanced.MemoryView.main at line 107 by thread main.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Method breakpoint hit in examples.advanced.MemoryView.<init> at line 44 by thread main.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Method breakpoint hit in examples.advanced.MemoryView.class$ at line 45 by thread main.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Method breakpoint hit in examples.advanced.MemoryView.inner at line 114 by thread main.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Method breakpoint hit in examples.advanced.MemoryView.updateConsumption at line 73 by thread main.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Method breakpoint hit in examples.advanced.MemoryView.updateStatus at line 86 by thread main.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Method breakpoint hit in examples.advanced.MemoryView.updateStatus at line 86 by thread main.");
    }

    public void testClassBreakpointPrefilledInClass() {
        NbDialogOperator dialog = Utilities.newBreakpoint(27, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 0).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 1).getText()));
        dialog.cancel();
    }
        
    public void testClassBreakpointPrefilledInInitializer() {
        NbDialogOperator dialog = Utilities.newBreakpoint(38, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 0).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 1).getText()));
        dialog.cancel();
    }

    public void testClassBreakpointPrefilledInConstructor() {
        NbDialogOperator dialog = Utilities.newBreakpoint(45, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 0).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 1).getText()));
        dialog.cancel();
    }

    public void testClassBreakpointPrefilledInMethod() {
        NbDialogOperator dialog = Utilities.newBreakpoint(73, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 0).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 1).getText()));
        dialog.cancel();
    }

    public void testClassBreakpointPrefilledInSecondClass() {
        NbDialogOperator dialog = Utilities.newBreakpoint(137, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 0).getText()));
        assertTrue("Class Name was not set to correct value.", "Helper".equals(new JTextFieldOperator(dialog, 1).getText()));
        dialog.cancel();
    }
    
    public void testClassBreakpointCreation() {
        NbDialogOperator dialog = Utilities.newBreakpoint(73, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        dialog.ok();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        if (!"Class MemoryView prepare / unload".equals(jTableOperator.getValueAt(0, 0).toString()) )
            assertTrue("Class breakpoint was not created.", false);
    }

    public void testClassBreakpointFunctionalityOnPrimaryClass() {
        NbDialogOperator dialog = Utilities.newBreakpoint(73, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        dialog.ok();
        Utilities.sleep(1000);
        Utilities.startDebugger("Class breakpoint hit for class examples.advanced.MemoryView");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.runningStatusBarText);
    }
    
    public void testClassBreakpointFunctionalityOnSecondClass() {
        NbDialogOperator dialog = Utilities.newBreakpoint(147, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        dialog.ok();
        Utilities.sleep(1000);
        Utilities.startDebugger("Class breakpoint hit for class examples.advanced.Helper");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.runningStatusBarText);
    }
    
    public void testClassBreakpointFunctionalityWithFilter() {
        NbDialogOperator dialog = Utilities.newBreakpoint(73, 1);
        new JComboBoxOperator(dialog, 0).selectItem("Class");
        Utilities.sleep(1000);
        new JTextFieldOperator(dialog, 1).setText("*");
        dialog.ok();
        Utilities.sleep(1000);
        Utilities.startDebugger("Class breakpoint hit for class examples.advanced.Helper.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Class breakpoint hit for class examples.advanced.MemoryView.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Class breakpoint hit for class examples.advanced.MemoryView$1.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.runningStatusBarText);
    }
    
    public void testVariableBreakpointPrefilledValues() {
        NbDialogOperator dialog = Utilities.newBreakpoint(35, 19);
        new JComboBoxOperator(dialog, 0).selectItem("Variable");
        Utilities.sleep(1000);
        assertTrue("Package Name was not set to correct value.", "examples.advanced".equals(new JTextFieldOperator(dialog, 1).getText()));
        assertTrue("Class Name was not set to correct value.", "MemoryView".equals(new JTextFieldOperator(dialog, 2).getText()));
        assertTrue("Variable Name was not set to correct value.", "timer".equals(new JTextFieldOperator(dialog, 3).getText()));
        dialog.cancel();
    }
    
    public void testVariableBreakpointCreation() {
        NbDialogOperator dialog = Utilities.newBreakpoint(35, 19);
        new JComboBoxOperator(dialog, 0).selectItem("Variable");
        Utilities.sleep(1000);
        new JComboBoxOperator(dialog, 1).selectItem("Variable Access");
        dialog.ok();
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        if (!"Variable MemoryView.timer access".equals(jTableOperator.getValueAt(0, 0).toString()) )
            assertTrue("Variable breakpoint was not created.", false);
    }
    
    public void testVariableBreakpointFunctionalityAccess() {
        NbDialogOperator dialog = Utilities.newBreakpoint(30, 34);
        new JComboBoxOperator(dialog, 0).selectItem("Variable");
        Utilities.sleep(1000);
        new JComboBoxOperator(dialog, 1).selectItem("Variable Access");
        dialog.ok();
        Utilities.sleep(1000);
        Utilities.startDebugger("Field breakpoint hit at line 98 in class examples.advanced.MemoryView by thread main.");
    }
    
    public void testVariableBreakpointFunctionalityModification() {
        NbDialogOperator dialog = Utilities.newBreakpoint(35, 19);
        new JComboBoxOperator(dialog, 0).selectItem("Variable");
        Utilities.sleep(1000);
        new JComboBoxOperator(dialog, 1).selectItem("Variable Modification");
        dialog.ok();
        Utilities.startDebugger("Field breakpoint hit at line 40 in class examples.advanced.MemoryView by thread main.");
    }
    
    public void testThreadBreakpointCreation() {
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Thread");
        Utilities.sleep(1000);
        dialog.ok();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        if (!"Thread started".equals(jTableOperator.getValueAt(0, 0).toString()) )
            assertTrue("Thread breakpoint was not created.", false);
    }
    
    public void testThreadBreakpointFunctionality() {
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        new JComboBoxOperator(dialog, 0).selectItem("Thread");
        Utilities.sleep(1000);
        dialog.ok();
        Utilities.sleep(1000);
        if (System.getProperty("os.name").startsWith("Windows")) {
            Utilities.startDebugger("Thread breakpoint hit by thread Signal Dispatcher.");
            //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
            new Action(null, null, Utilities.continueShortcut).performShortcut();
            MainWindowOperator.getDefault().waitStatusText("Thread breakpoint hit by thread main.");
        } else {
            Utilities.startDebugger("Thread breakpoint hit by thread main.");
            //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
            new Action(null, null, Utilities.continueShortcut).performShortcut();
            MainWindowOperator.getDefault().waitStatusText("Thread breakpoint hit by thread Signal Dispatcher.");
        }
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread breakpoint hit by thread Thread-0.");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText(Utilities.runningStatusBarText);
    }
        
    public void testExceptionBreakpointCreation() {
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        Utilities.sleep(1000);
        new JComboBoxOperator(dialog, 0).selectItem("Exception");
        Utilities.sleep(1000);
        new JTextFieldOperator(dialog, 2).setText("java.lang");
        new JComboBoxOperator(dialog, 2).typeText("NullPointerException");
        new JComboBoxOperator(dialog, 1).selectItem("Caught or Uncaught");
        dialog.ok();
        Utilities.sleep(1000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.breakpointsViewTitle));
        if (!"Exception NullPointerException".equals(jTableOperator.getValueAt(0, 0).toString()) )
            assertTrue("Thread breakpoint was not created.", false);
    }
    
    public void testExceptionBreakpointFunctionality() {
        //new ActionNoBlock(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.newBreakpointItem).toString(), null).perform();
        new ActionNoBlock(null, null, Utilities.newBreakpointShortcut).performShortcut();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newBreakpointTitle);
        Utilities.sleep(1000);
        new JComboBoxOperator(dialog, 0).selectItem("Exception");
        Utilities.sleep(1000);
        new JTextFieldOperator(dialog, 2).setText("java.lang");
        new JComboBoxOperator(dialog, 2).typeText("ClassNotFoundException");
        new JComboBoxOperator(dialog, 1).selectItem("Caught or Uncaught");
        dialog.ok();
        Utilities.sleep(1000);
        Utilities.startDebugger("Exception breakpoint hit in java.lang.ClassLoader");
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Exception breakpoint hit in java.net.URLClassLoader$1");
    }
}
