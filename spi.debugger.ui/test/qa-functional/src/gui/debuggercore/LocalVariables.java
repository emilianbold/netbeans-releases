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
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

package gui.debuggercore;

import junit.textui.TestRunner;
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
        suite.addTest(new LocalVariables("setupLocalVariablesTests"));
        suite.addTest(new LocalVariables("testLocalVariablesExpand"));
        suite.addTest(new LocalVariables("testLocalVariablesThisNode"));
        suite.addTest(new LocalVariables("testLocalVariablesStaticNode"));
        suite.addTest(new LocalVariables("testLocalVariablesStaticInherited"));
        suite.addTest(new LocalVariables("testLocalVariablesInheritedNode"));
        suite.addTest(new LocalVariables("testLocalVariablesExtended"));
        suite.addTest(new LocalVariables("testLocalVariablesValues"));
        suite.addTest(new LocalVariables("finishLocalVariablesTests"));
        return suite;
    }
    
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }
    
    public void setupLocalVariablesTests() {
        Utilities.sleep(1000);
        org.netbeans.jellytools.nodes.Node projectNode = new org.netbeans.jellytools.nodes.Node(new JTreeOperator(new ProjectsTabOperator()), Utilities.testProjectName);
        projectNode.select();
        projectNode.performPopupAction(Utilities.setMainProjectAction);
        
        JavaNode javaNode = new JavaNode(projectNode, "Source Packages|examples.advanced|MemoryView.java");
        javaNode.select();
        javaNode.performPopupAction(Utilities.openSourceAction);
        Utilities.sleep(2000);
        //Utilities.setCaret(45, 1);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runToCursorItem).toString(), null).perform();
        new Action(null, null, Utilities.runToCursorShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:45.");
    }
    
    public void testLocalVariablesExpand() {
        Utilities.showLocalVariablesView();
        Utilities.sleep(2000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));        
        TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this").expand();
        Utilities.sleep(500);
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static").expand();
        Utilities.sleep(500);
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Inherited").expand();
        Utilities.sleep(500);
    }
    
    public void testLocalVariablesThisNode() {
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        CheckTTVLine(jTableOperator, 2, "Vpublic", "String", "\"Public Variable\"");
        CheckTTVLine(jTableOperator, 3, "Vprotected", "String", "\"Protected Variable\"");
        CheckTTVLine(jTableOperator, 4, "Vprivate", "String", "\"Private Variable\"");
        CheckTTVLine(jTableOperator, 5, "VpackagePrivate", "String", "\"Package-private Variable\"");
    }
    
    public void testLocalVariablesStaticNode() {
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        CheckTTVLine(jTableOperator, 10, "Spublic", "String", "\"Public Variable\"");
        CheckTTVLine(jTableOperator, 11, "Sprotected", "String", "\"Protected Variable\"");
        CheckTTVLine(jTableOperator, 12, "Sprivate", "String", "\"Private Variable\"");
        CheckTTVLine(jTableOperator, 13, "SpackagePrivate", "String", "\"Package-private Variable\"");
    }
    
    public void testLocalVariablesStaticInherited() {
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        CheckTTVLine(jTableOperator, 15, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
        CheckTTVLine(jTableOperator, 16, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
        CheckTTVLine(jTableOperator, 17, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
        CheckTTVLine(jTableOperator, 18, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
    }
    
    public void testLocalVariablesInheritedNode() {
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        CheckTTVLine(jTableOperator, 20, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
        CheckTTVLine(jTableOperator, 21, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
        CheckTTVLine(jTableOperator, 22, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
        CheckTTVLine(jTableOperator, 23, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
    }
        
    public void testLocalVariablesExtended() {
        //Utilities.setCaret(70, 1);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runToCursorItem).toString(), null).perform();
        new Action(null, null, Utilities.runToCursorShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:70.");
        
        Utilities.sleep(10000);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        int count = 0;
        CheckTTVLine(jTableOperator, count++, "this", "MemoryView", null);
        CheckTTVLine(jTableOperator, count++, "timer", null, "null");
        CheckTTVLine(jTableOperator, count++, "Vpublic", "String", "\"Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "Vprotected", "String", "\"Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "Vprivate", "String", "\"Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "VpackagePrivate", "String", "\"Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "Static", null, null);
        CheckTTVLine(jTableOperator, count++, "bundle", "PropertyResourceBundle", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static|bundle").isLeaf())
            assertTrue("Node bundle has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "msgMemory", "MessageFormat", null);
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static|msgMemory").isLeaf())
            assertTrue("Node msgMemory has no child nodes", false);
        CheckTTVLine(jTableOperator, count++, "UPDATE_TIME", "int", "1000");
        CheckTTVLine(jTableOperator, count++, "Spublic", "String", "\"Public Variable\"");
        CheckTTVLine(jTableOperator, count++, "Sprotected", "String", "\"Protected Variable\"");
        CheckTTVLine(jTableOperator, count++, "Sprivate", "String", "\"Private Variable\"");
        CheckTTVLine(jTableOperator, count++, "SpackagePrivate", "String", "\"Package-private Variable\"");
        CheckTTVLine(jTableOperator, count++, "class$java$lang$Runtime", "Class", "class java.lang.Runtime");
        if (new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static|class$java$lang$Runtime").isLeaf())
            assertTrue("Node class$java$lang$Runtime has no child nodes", false);
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
    }
    
    public void testLocalVariablesValues() {
        //Utilities.setCaret(98, 1);
        //new Action(new StringBuffer(Utilities.runMenu).append("|").append(Utilities.runToCursorItem).toString(), null).perform();
        new Action(null, null, Utilities.runToCursorShortcut).performShortcut();
        MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:98.");
        
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        try {
            org.openide.nodes.Node.Property property;
            property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(25, 2);
            long free = Long.parseLong(property.getValue().toString());
            property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(26, 2);
            long total = Long.parseLong(property.getValue().toString());
            property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(27, 2);
            long taken = Long.parseLong(property.getValue().toString());
            assertTrue("Local VAraibles values does not seem to be correct (total != free + taken)", total == free + taken);

        }
        catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        }
        catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }

    public void finishLocalVariablesTests() {
        Utilities.endSession();
    }
    
    // check values in TreeTable line
    public void CheckTTVLine(JTableOperator table, int lineNumber, String name, String type, String value) {
        try {
            org.openide.nodes.Node.Property property;
            String string = null;
            if (!(name.equals(table.getValueAt(lineNumber, 0).toString())))
                assertTrue("Node " + name + " not displayed in Local Variables view", false);
            property = (org.openide.nodes.Node.Property)table.getValueAt(lineNumber, 1);
            string = property.getValue().toString();
            if ((type!= null)&&(!(type.equals(string))))
                assertTrue("Node " + name + " has wrong type in Local Variables view (displayed: " + string + ", expected: " + type + ")", false);
            property = (org.openide.nodes.Node.Property)table.getValueAt(lineNumber, 2);
            string = property.getValue().toString();
            if ((value!= null)&&(!(value.equals(string))))
                assertTrue("Node " + name + " has wrong value in Local Variables view (displayed: " + string + ", expected: " + value + ")", false);
        }
        catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        }
        catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }
}
