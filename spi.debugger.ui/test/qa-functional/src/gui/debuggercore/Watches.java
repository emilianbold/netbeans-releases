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

import junit.textui.TestRunner;
import org.openide.nodes.Node;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;

public class Watches extends JellyTestCase {
    
    public Watches(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Watches("testAddWatch"));
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
        Utilities.deleteAllBreakpoints();
        Utilities.deleteAllWatches();
        Utilities.closeZombieSessions();
    }
    
    /**
     *
     */
    public void testAddWatch() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        org.netbeans.jellytools.nodes.Node projectNode = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);

        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        
        EditorOperator editorOperator = new EditorOperator("MemoryView.java");
        editorOperator.setCaretPosition(103, 1);
        
        new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.toggleBreakpointItem).toString(), null).perform();
        
        // create new watches
        new ActionNoBlock(Utilities.runMenu + "|" + Utilities.newWatchItem, null).perform();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText("free");
        dialog.ok();
        
        new ActionNoBlock(Utilities.runMenu + "|" + Utilities.newWatchItem, null).perform();
        dialog = new NbDialogOperator(Utilities.newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText("taken");
        dialog.ok();
        
        new ActionNoBlock(Utilities.runMenu + "|" + Utilities.newWatchItem, null).perform();
        dialog = new NbDialogOperator(Utilities.newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText("total");
        dialog.ok();

        new ActionNoBlock(Utilities.runMenu + "|" + Utilities.newWatchItem, null).perform();
        dialog = new NbDialogOperator(Utilities.newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText("this");
        dialog.ok();

        // start debugger and wait till breakpoint is hit
        new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runInDebuggerItem).toString(), null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Thread main stopped at MemoryView.java:103.");
        
        // check watches and values
        Utilities.showWatchesView();
        TopComponentOperator watchesOper = new TopComponentOperator(Utilities.watchesViewTitle);
        TreeTableOperator jTableOperator = new TreeTableOperator(watchesOper);
        Node.Property property;
        int count = 0;
        
        try {
            if (!("free".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'free\' was not created", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("long".equals(property.getValue())))
                assertTrue("Watch type for expression \'free\' is " + property.getValue() + ", should be long", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,2);
            long free = Long.parseLong(property.getValue().toString());
            
            if (!("taken".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'taken\' was not created", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("int".equals(property.getValue())))
                assertTrue("Watch type for expression \'taken\' is " + property.getValue() + ", should be long", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,2);
            long taken = Long.parseLong(property.getValue().toString());
            
            if (!("total".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'total\' was not created", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("long".equals(property.getValue())))
                assertTrue("Watch type for expression \'total\' is " + property.getValue() + ", should be long", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,2);
            long total = Long.parseLong(property.getValue().toString());
            
            assertTrue("Watches values does not seem to be correct (total != free + taken)", total == free + taken);
            
            if (!("this".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Watch for expression \'this\' was not created", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("MemoryView".equals(property.getValue())))
                assertTrue("Watch type for expression \'this\' is " + property.getValue() + ", should be MemoryView", false);
            if (new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), "this").isLeaf())
                assertTrue("Watch this has no child nodes", false);
        }
        catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        }
        catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
       
        // finnish bedugging session
        new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.killSessionsItem).toString(), null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText(Utilities.finishedStatusBarText);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session was not killed.");
            throw(tee);
        }
    }
}
