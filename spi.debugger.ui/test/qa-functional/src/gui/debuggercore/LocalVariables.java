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
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;

public class LocalVariables extends JellyTestCase {
    
    String projectPropertiesTitle;
    
    public LocalVariables(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new LocalVariables("testLocalVariables"));
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
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        org.netbeans.jellytools.nodes.Node projectNode = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        
        projectNode.performPopupActionNoBlock(Utilities.projectPropertiesAction);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.projectPropertiesTitle + Utilities.testProjectName);
        org.netbeans.jellytools.nodes.Node helper = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(dialog), "Run|" + Utilities.runningProjectTreeItem);
        helper.select();
        new JTextFieldOperator(dialog, 0).setText("examples.advanced.MemoryView");
        dialog.ok();
    }
    
    public void testLocalVariables() {
        ProjectsTabOperator projectsTabOper = new ProjectsTabOperator();
        org.netbeans.jellytools.nodes.Node projectNode = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(projectsTabOper), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        
        projectNode.performPopupActionNoBlock(Utilities.projectPropertiesAction);
        NbDialogOperator dialog = new NbDialogOperator(Utilities.projectPropertiesTitle + Utilities.testProjectName);
        org.netbeans.jellytools.nodes.Node helper = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(dialog), "Run|Running Project");
        helper.select();
        new JTextFieldOperator(dialog, 0).setText("examples.advanced.Variables");
        dialog.ok();
        
        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|Variables.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        
        EditorOperator editorOperator = new EditorOperator("Variables.java");
        
        // create new line breakpoint
        editorOperator.setCaretPosition(53, 1);
        new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.toggleBreakpointItem).toString(), null).perform();
        
        // start debugging
        editorOperator.setCaretPosition(28, 1);
        new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runToCursorItem).toString(), null).perform();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Thread main stopped at Variables.java:28.");
        
        // show local variables view and check values
        try {
            Utilities.showLocalVariablesView();
            TopComponentOperator localVarsOper = new TopComponentOperator(Utilities.localVarsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(localVarsOper);
            
            TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
            new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this").expand();
            new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static").expand();
            
            String [] expectedLocalVariables_0 = {"this", "Vpublic", "Vprotected","Vprivate","VpackagePrivate","Static","Spublic","Sprotected","Sprivate","SpackagePrivate","class$java$lang$Runtime" };
            String [] expectedLocalVariables_1 = {"Variables","String","String","String","String","String","String","String","String","String","Class"};
            String [] expectedLocalVariables_2 = {"\"Public Variable\"", "\"Protected Variable\"", "\"Private Variable\"", "\"Package-private Variable\""};
            Node.Property property;
            int count = 0;
            if (!("this".equals(jTableOperator.getValueAt(count++,0).toString())))
                assertTrue("Node this not displayed in Local Variables view", false);
            for (int i = 0; i < expectedLocalVariables_2.length; i++) {
                if (!(expectedLocalVariables_0[count].equals(jTableOperator.getValueAt(count,0).toString())))
                    assertTrue("Node " + expectedLocalVariables_0[count] + " not displayed in Local Variables view", false);
                property = (Node.Property)jTableOperator.getValueAt(count++,2);
                if (!(expectedLocalVariables_2[i].equals(property.getValue())))
                    assertTrue("Value of node " + expectedLocalVariables_0[count-1] + " is " + property.getValue() + ", should be " + expectedLocalVariables_2[i], false);
            }
            if (!("Static".equals(jTableOperator.getValueAt(count++,0).toString())))
                assertTrue("Node Static not displayed in Local Variables view", false);
            for (int i = 0; i < expectedLocalVariables_2.length; i++) {
                if (!(expectedLocalVariables_0[count].equals(jTableOperator.getValueAt(count,0).toString())))
                    assertTrue("Node " + expectedLocalVariables_0[count] + " not displayed in Local Variables view", false);
                property = (Node.Property)jTableOperator.getValueAt(count++,2);
                if (!(expectedLocalVariables_2[i].equals(property.getValue())))
                    assertTrue("Value of node " + expectedLocalVariables_0[count-1] + " is " + property.getValue() + ", should be " + expectedLocalVariables_2[i], false);
            }
            if (!("class$java$lang$Runtime".equals(jTableOperator.getValueAt(count++,0).toString())))
                assertTrue("Node class$java$lang$Runtime not displayed in Local Variables view", false);
            
            // continue to breakpoint
            new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
            mwo.waitStatusText("Thread main stopped at Variables.java:53.");
            
            // show toString() column and check variable types, values and to string representation
            new JPopupMenuOperator(jTableOperator.callPopupOnCell(0, 0)).pushMenuNoBlock("List Options|Change Visible Columns...");
            dialog = new NbDialogOperator("LocalsView - Change Visible Columns");
            new JCheckBoxOperator(dialog, 3).push();
            dialog.ok();
            
            new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this").expand();
            new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static").expand();
            
            count = 0;
            if (!("this".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node this not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("Variables".equals(property.getValue())))
                assertTrue("Node this has wrong type in Local Variables view", false);
            
            for (int i = 0; i < expectedLocalVariables_2.length; i++) {
                if (!(expectedLocalVariables_0[count].equals(jTableOperator.getValueAt(count,0).toString())))
                    assertTrue("Node " + expectedLocalVariables_0[count] + " not displayed in Local Variables view", false);
                property = (Node.Property)jTableOperator.getValueAt(count,1);
                if (!(expectedLocalVariables_1[count].equals(property.getValue())))
                    assertTrue("Node " + expectedLocalVariables_0[count] + " has wrong type in Local Variables view", false);
                property = (Node.Property)jTableOperator.getValueAt(count,2);
                if (!(expectedLocalVariables_2[i].equals(property.getValue())))
                    assertTrue("Value of node " + expectedLocalVariables_0[count] + " is " + property.getValue() + ", should be " + expectedLocalVariables_2[i], false);
                property = (Node.Property)jTableOperator.getValueAt(count++,3);
                if (!(expectedLocalVariables_2[i].equals(property.getValue())))
                    assertTrue("String representation of node " + expectedLocalVariables_0[count-1] + " is " + property.getValue() + ", should be " + expectedLocalVariables_2[i], false);
            }
            
            if (!("Static".equals(jTableOperator.getValueAt(count++,0).toString())))
                assertTrue("Node Static not displayed in Local Variables view", false);
            
            for (int i = 0; i < expectedLocalVariables_2.length; i++) {
                if (!(expectedLocalVariables_0[count].equals(jTableOperator.getValueAt(count,0).toString())))
                    assertTrue("Node " + expectedLocalVariables_0[count] + " not displayed in Local Variables view", false);
                property = (Node.Property)jTableOperator.getValueAt(count,1);
                if (!(expectedLocalVariables_1[count].equals(property.getValue())))
                    assertTrue("Node " + expectedLocalVariables_0[count] + " has wrong type in Local Variables view", false);
                property = (Node.Property)jTableOperator.getValueAt(count,2);
                if (!(expectedLocalVariables_2[i].equals(property.getValue())))
                    assertTrue("Value of node " + expectedLocalVariables_0[count] + " is " + property.getValue() + ", should be " + expectedLocalVariables_2[i], false);
                property = (Node.Property)jTableOperator.getValueAt(count++,3);
                if (!(expectedLocalVariables_2[i].equals(property.getValue())))
                    assertTrue("String representation of node " + expectedLocalVariables_0[count-1] + " is " + property.getValue() + ", should be " + expectedLocalVariables_2[i], false);
            }
            
            if (!("class$java$lang$Runtime".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node class$java$lang$Runtime not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("Class".equals(property.getValue())))
                assertTrue("Node class$java$lang$Runtime has wrong type in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count,2);
            if (!("class java.lang.Runtime".equals(property.getValue())))
                assertTrue("Value of node class$java$lang$Runtime is" + property.getValue() + ", should be class java.lang.Runtime", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,3);
            if (!("class java.lang.Runtime".equals(property.getValue())))
                assertTrue("String representation of node class$java$lang$Runtime is" + property.getValue() + ", should be class java.lang.Runtime", false);
            
            if (!("clazz".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node clazz not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("Class".equals(property.getValue())))
                assertTrue("Node clazz has wrong type in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count,2);
            if (!("class java.lang.Runtime".equals(property.getValue())))
                assertTrue("Value of node clazz is" + property.getValue() + ", should be class java.lang.Runtime", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,3);
            if (!("class java.lang.Runtime".equals(property.getValue())))
                assertTrue("String representation of node clazz is" + property.getValue() + ", should be class java.lang.Runtime", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "clazz").isLeaf())
                assertTrue("Node clazz has no child nodes", false);
            
            if (!("string".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node string not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("String".equals(property.getValue())))
                assertTrue("Node string has wrong type in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count,2);
            if (!("\"Hi!\"".equals(property.getValue())))
                assertTrue("Value of node string is " + property.getValue() + ", should be Hi!", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,3);
            if (!("\"Hi!\"".equals(property.getValue())))
                assertTrue("String representation of node string is" + property.getValue() + ", should be Hi!", false);
            
            if (!("n".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node n not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count,1);
            if (!("int".equals(property.getValue())))
                assertTrue("Node n has wrong type in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count,2);
            if (!("50".equals(property.getValue())))
                assertTrue("Value of node string is" + property.getValue() + ", should be 50", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,3);
            if (!("50".equals(property.getValue())))
                assertTrue("String representation of node string is" + property.getValue() + ", should be 50", false);
            
            if (!("llist".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node llist not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("LinkedList".equals(property.getValue())))
                assertTrue("Node llist has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "llist").isLeaf())
                assertTrue("Node llist has no child nodes", false);
            
            if (!("alist".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node alist not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("ArrayList".equals(property.getValue())))
                assertTrue("Node alist has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "alist").isLeaf())
                assertTrue("Node alist has no child nodes", false);
            
            if (!("vec".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node vec not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("Vector".equals(property.getValue())))
                assertTrue("Node vec has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "vec").isLeaf())
                assertTrue("Node vec has no child nodes", false);
            
            if (!("hmap".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node hmap not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("HashMap".equals(property.getValue())))
                assertTrue("Node hmap has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "hmap").isLeaf())
                assertTrue("Node hmap has no child nodes", false);
            
            if (!("htab".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node htab not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("Hashtable".equals(property.getValue())))
                assertTrue("Node htab has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "htab").isLeaf())
                assertTrue("Node htab has no child nodes", false);
            
            if (!("tmap".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node tmap not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("TreeMap".equals(property.getValue())))
                assertTrue("Node tmap has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "tmap").isLeaf())
                assertTrue("Node tmap has no child nodes", false);
            
            if (!("hset".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node hset not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("HashSet".equals(property.getValue())))
                assertTrue("Node hset has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "hset").isLeaf())
                assertTrue("Node hset has no child nodes", false);
            
            if (!("tset".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node tset not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("TreeSet".equals(property.getValue())))
                assertTrue("Node tset has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "tset").isLeaf())
                assertTrue("Node tset has no child nodes", false);
            
            if (!("policko".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node policko not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("int[]".equals(property.getValue())))
                assertTrue("Node policko has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "policko").isLeaf())
                assertTrue("Node policko has no child nodes", false);
            
            if (!("pole".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node pole not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("int[]".equals(property.getValue())))
                assertTrue("Node hset has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "pole").isLeaf())
                assertTrue("Node pole has no child nodes", false);
            
            if (!("d2".equals(jTableOperator.getValueAt(count,0).toString())))
                assertTrue("Node d2 not displayed in Local Variables view", false);
            property = (Node.Property)jTableOperator.getValueAt(count++,1);
            if (!("int[][]".equals(property.getValue())))
                assertTrue("Node d2 has wrong type in Local Variables view", false);
            if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "d2").isLeaf())
                assertTrue("Node d2 has no child nodes", false);
        }
        catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        }
        catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
        
        new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText(Utilities.finishedStatusBarText);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session did not finished.");
            throw(tee);
        }
    }
}
