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
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.FinishDebuggerAction;
import org.netbeans.jellytools.modules.debugger.actions.RunToCursorAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;

public class LocalVariables extends JellyTestCase {
    
    String projectPropertiesTitle;
    MainWindowOperator.StatusTextTracer stt = null;
    
    public LocalVariables(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new LocalVariables("testLocalVariablesExpand"));
        suite.addTest(new LocalVariables("testLocalVariablesThisNode"));
        suite.addTest(new LocalVariables("testLocalVariablesStaticNode"));
        suite.addTest(new LocalVariables("testLocalVariablesStaticInherited"));
        suite.addTest(new LocalVariables("testLocalVariablesInheritedNode"));
        suite.addTest(new LocalVariables("testLocalVariablesExtended"));
        suite.addTest(new LocalVariables("testLocalVariablesValues"));
        return suite;
    }
    
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
        stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
    }
    
    public void tearDown() {
        if (getName().equals("testLocalVariablesValues")) //last
            new FinishDebuggerAction().perform();
    }
    
    public void testLocalVariablesExpand() {
        //open source
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode);
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.setCaret(eo, 52);
        new RunToCursorAction().perform();
        stt.waitText("Thread main stopped at MemoryView.java:52.");
        new EventTool().waitNoEvent(1000);
        
        Utilities.showLocalVariablesView();
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
        checkTreeTableLine(jTableOperator, 2, "Vpublic", "String", "\"Public Variable\"");
        checkTreeTableLine(jTableOperator, 3, "Vprotected", "String", "\"Protected Variable\"");
        checkTreeTableLine(jTableOperator, 4, "Vprivate", "String", "\"Private Variable\"");
        checkTreeTableLine(jTableOperator, 5, "VpackagePrivate", "String", "\"Package-private Variable\"");
    }
    
    public void testLocalVariablesStaticNode() {
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        checkTreeTableLine(jTableOperator, 10, "Spublic", "String", "\"Public Variable\"");
        checkTreeTableLine(jTableOperator, 11, "Sprotected", "String", "\"Protected Variable\"");
        checkTreeTableLine(jTableOperator, 12, "Sprivate", "String", "\"Private Variable\"");
        checkTreeTableLine(jTableOperator, 13, "SpackagePrivate", "String", "\"Package-private Variable\"");
    }
    
    public void testLocalVariablesStaticInherited() {
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        checkTreeTableLine(jTableOperator, 15, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
        checkTreeTableLine(jTableOperator, 16, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
        checkTreeTableLine(jTableOperator, 17, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
        checkTreeTableLine(jTableOperator, 18, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
    }
    
    public void testLocalVariablesInheritedNode() {
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        checkTreeTableLine(jTableOperator, 20, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
        checkTreeTableLine(jTableOperator, 21, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
        checkTreeTableLine(jTableOperator, 22, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
        checkTreeTableLine(jTableOperator, 23, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
    }
    
    public void testLocalVariablesExtended() {
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode);
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.setCaret(eo, 76);
        new RunToCursorAction().perform();
        stt.waitText("Thread main stopped at MemoryView.java:76.");
        new EventTool().waitNoEvent(1000);
        
        Utilities.showLocalVariablesView();
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        int count = 0;
        checkTreeTableLine(jTableOperator, count++, "this", "MemoryView", null);
        checkTreeTableLine(jTableOperator, count++, "timer", null, "null");
        checkTreeTableLine(jTableOperator, count++, "Vpublic", "String", "\"Public Variable\"");
        checkTreeTableLine(jTableOperator, count++, "Vprotected", "String", "\"Protected Variable\"");
        checkTreeTableLine(jTableOperator, count++, "Vprivate", "String", "\"Private Variable\"");
        checkTreeTableLine(jTableOperator, count++, "VpackagePrivate", "String", "\"Package-private Variable\"");
        checkTreeTableLine(jTableOperator, count++, "Static", null, null);
        checkTreeTableLine(jTableOperator, count++, "bundle", "PropertyResourceBundle", null);
        assertFalse("Node bundle has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static|bundle").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "msgMemory", "MessageFormat", null);
        assertFalse("Node msgMemory has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static|msgMemory").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "UPDATE_TIME", "int", "1000");
        checkTreeTableLine(jTableOperator, count++, "Spublic", "String", "\"Public Variable\"");
        checkTreeTableLine(jTableOperator, count++, "Sprotected", "String", "\"Protected Variable\"");
        checkTreeTableLine(jTableOperator, count++, "Sprivate", "String", "\"Private Variable\"");
        checkTreeTableLine(jTableOperator, count++, "SpackagePrivate", "String", "\"Package-private Variable\"");
        checkTreeTableLine(jTableOperator, count++, "class$java$lang$Runtime", "Class", "class java.lang.Runtime");
        assertFalse("Node class$java$lang$Runtime has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static|class$java$lang$Runtime").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
        checkTreeTableLine(jTableOperator, count++, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
        checkTreeTableLine(jTableOperator, count++, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
        checkTreeTableLine(jTableOperator, count++, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        checkTreeTableLine(jTableOperator, count++, "Inherited", null, null);
        checkTreeTableLine(jTableOperator, count++, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
        checkTreeTableLine(jTableOperator, count++, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
        checkTreeTableLine(jTableOperator, count++, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
        checkTreeTableLine(jTableOperator, count++, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        checkTreeTableLine(jTableOperator, count++, "clazz", "Class", "class java.lang.Runtime");
        assertFalse("Node clazz has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "clazz").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "string", "String", "\"Hi!\"");
        checkTreeTableLine(jTableOperator, count++, "n", "int", "50");
        checkTreeTableLine(jTableOperator, count++, "llist", "LinkedList", null);
        assertFalse("Node llist has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "llist").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "alist", "ArrayList", null);
        assertFalse("Node alist has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "alist").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "vec", "Vector", null);
        assertFalse("Node vec has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "vec").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "hmap", "HashMap", null);
        assertFalse("Node hmap has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "hmap").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "htab", "Hashtable", null);
        assertFalse("Node htab has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "htab").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "tmap", "TreeMap", null);
        assertFalse("Node tmap has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "tmap").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "hset", "HashSet", null);
        assertFalse("Node hset has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "hset").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "tset", "TreeSet", null);
        assertFalse("Node tset has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "tset").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "policko", "int[]", null);
        assertFalse("Node policko has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "policko").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "pole", "int[]", null);
        assertFalse("Node pole has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "pole").isLeaf());
        checkTreeTableLine(jTableOperator, count++, "d2", "int[][]", null);
        assertFalse("Node d2 has no child nodes", new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "d2").isLeaf());
    }
    
    public void testLocalVariablesValues() {
        Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
        new OpenAction().performAPI(beanNode);
        EditorOperator eo = new EditorOperator("MemoryView.java");
        Utilities.setCaret(eo, 104);
        new RunToCursorAction().perform();
        stt.waitText("Thread main stopped at MemoryView.java:104.");
        new EventTool().waitNoEvent(1000);
        
        Utilities.showLocalVariablesView();
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        try {
            org.openide.nodes.Node.Property property;
            property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(25, 2);
            long free = Long.parseLong(property.getValue().toString());
            property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(26, 2);
            long total = Long.parseLong(property.getValue().toString());
            property = (org.openide.nodes.Node.Property)jTableOperator.getValueAt(27, 2);
            long taken = Long.parseLong(property.getValue().toString());
            assertTrue("Local varaibles values does not seem to be correct (total != free + taken) - "+total+" != "+free+" + "+taken, (total == free + taken));
            
        } catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        } catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }
    
    // check values in TreeTable line
    public void checkTreeTableLine(JTableOperator table, int lineNumber, String name, String type, String value) {
        try {
            org.openide.nodes.Node.Property property;
            String string = null;
            assertTrue("Node " + name + " not displayed in Local Variables view", name.equals(table.getValueAt(lineNumber, 0).toString()));
            property = (org.openide.nodes.Node.Property)table.getValueAt(lineNumber, 1);
            string = property.getValue().toString();
            int maxWait = 100;
            while (string.equals(Utilities.evaluatingPropertyText) && maxWait > 0) {
                new EventTool().waitNoEvent(300);
                maxWait--;
            }
            assertTrue("Node " + name + " has wrong type in Local Variables view (displayed: " + string + ", expected: " + type + ")",
                    (type == null) || type.equals(string));
            property = (org.openide.nodes.Node.Property)table.getValueAt(lineNumber, 2);
            string = property.getValue().toString();
            maxWait = 100;
            while (string.equals(Utilities.evaluatingPropertyText) && maxWait > 0) {
                new EventTool().waitNoEvent(300);
                maxWait--;
            }
            assertTrue("Node " + name + " has wrong value in Local Variables view (displayed: " + string + ", expected: " + value + ")",
                    (type == null) || !type.equals(string));
        } catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        } catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }
}
