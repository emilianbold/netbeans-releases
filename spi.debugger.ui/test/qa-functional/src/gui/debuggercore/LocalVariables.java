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
        Utilities.sleep(2000);
        EditorOperator editorOperator = new EditorOperator("Variables.java");
        
        // create new line breakpoint
        editorOperator.setCaretPosition(53, 1);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.toggleBreakpointItem).toString(), null).perform();
        new Action(null, null, Utilities.toggleBreakpointShortcut).performShortcut();
        
        // start debugging
        editorOperator.setCaretPosition(28, 1);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runToCursorItem).toString(), null).perform();
        new Action(null, null, Utilities.runToCursorShortcut).performShortcut();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.waitStatusText("Thread main stopped at Variables.java:28.");
        
        // show local variables view and check values
        Utilities.showLocalVariablesView();
        TopComponentOperator localVarsOper = new TopComponentOperator(Utilities.localVarsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(localVarsOper);
        
        TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this").expand();
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static").expand();
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Inherited").expand();
        
        int count = 1;
        CheckTTVLine(jTableOperator, count++, "Vpublic", "String", "\"Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "Vprotected", "String", "\"Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "Vprivate", "String", "\"Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "VpackagePrivate", "String", "\"Package-private Variable\"");
        count++; // skip line Static
        CheckTTVLine(jTableOperator, count++, "Spublic", "String", "\"Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "Sprotected", "String", "\"Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "Sprivate", "String", "\"Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "SpackagePrivate", "String", "\"Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "class$java$lang$Runtime", null, "null");
        CheckTTVLine(jTableOperator, count++, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        count++;
        CheckTTVLine(jTableOperator, count++, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        
        // continue to breakpoint
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.continueItem).toString(), null).perform();
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        mwo.waitStatusText("Thread main stopped at Variables.java:53.");
        
        count = 1;
        CheckTTVLine(jTableOperator, count++, "Vpublic", "String", "\"Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "Vprotected", "String", "\"Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "Vprivate", "String", "\"Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "VpackagePrivate", "String", "\"Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "Static", null, null);
        CheckTTVLine(jTableOperator, count++, "Spublic", "String", "\"Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "Sprotected", "String", "\"Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "Sprivate", "String", "\"Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "SpackagePrivate", "String", "\"Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "class$java$lang$Runtime", "Class", "class java.lang.Runtime");
        CheckTTVLine(jTableOperator, count++, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "Inherited", null, null);
        CheckTTVLine(jTableOperator, count++, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "clazz", "Class", "class java.lang.Runtime");
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "clazz").isLeaf())
            assertTrue("Node clazz has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "string", "String", "\"Hi!\"");
        CheckTTVLine(jTableOperator, count++, "n", "int", "50");
        CheckTTVLine(jTableOperator, count++, "llist", "LinkedList", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "llist").isLeaf())
            assertTrue("Node llist has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "alist", "ArrayList", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "alist").isLeaf())
            assertTrue("Node alist has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "vec", "Vector", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "vec").isLeaf())
            assertTrue("Node vec has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "hmap", "HashMap", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "hmap").isLeaf())
            assertTrue("Node hmap has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "htab", "Hashtable", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "htab").isLeaf())
            assertTrue("Node htab has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "tmap", "TreeMap", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "tmap").isLeaf())
            assertTrue("Node tmap has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "hset", "HashSet", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "hset").isLeaf())
            assertTrue("Node hset has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "tset", "TreeSet", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "tset").isLeaf())
            assertTrue("Node tset has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "policko", "int[]", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "policko").isLeaf())
            assertTrue("Node policko has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "pole", "int[]", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "pole").isLeaf())
            assertTrue("Node pole has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "d2", "int[][]", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "d2").isLeaf())
            assertTrue("Node d2 has no child nodes", false);
        
        // continue = end of application
        new Action(null, null, Utilities.continueShortcut).performShortcut();
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            mwo.waitStatusText(Utilities.finishedStatusBarText);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Debugging session did not finished.");
            throw(tee);
        }
    }
    
    // check values in TreeTable line
    public void CheckTTVLine(JTableOperator table, int lineNumber, String name, String type, String value) {
        try {
            Node.Property property;
            if (!(name.equals(table.getValueAt(lineNumber,0).toString())))
                assertTrue("Node " + name + " not displayed in Local Variables view", false);
            property = (Node.Property)table.getValueAt(lineNumber,1);
            if ((type!= null)&&(!(type.equals(property.getValue()))))
                assertTrue("Node " + name + " has wrong type in Local Variables view", false);
            property = (Node.Property)table.getValueAt(lineNumber,2);
            if ((value!= null)&&(!(value.equals(property.getValue()))))
                assertTrue("Node " + name + " has wrong value in Local Variables view", false);
        }
        catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        }
        catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }
}
