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

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.RunToCursorAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author ehucka
 */
public class LocalVariables extends JellyTestCase {
    
    static int consoleLineNumber = 0;
    
    String projectPropertiesTitle;
    
    /**
     *
     * @param name
     */
    public LocalVariables(String name) {
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
        suite.addTest(new LocalVariables("testLocalVariablesThisNode"));
        suite.addTest(new LocalVariables("testLocalVariablesStaticNode"));
        suite.addTest(new LocalVariables("testLocalVariablesStaticInherited"));
        suite.addTest(new LocalVariables("testLocalVariablesInheritedNode"));
        suite.addTest(new LocalVariables("testLocalVariablesExtended"));
        suite.addTest(new LocalVariables("testLocalVariablesValues"));
        return suite;
    }
    
    /**
     *
     */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
        if (getName().equals("testLocalVariablesThisNode")) {
            //open source
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.setCaret(eo, 52);
            new RunToCursorAction().perform();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            consoleLineNumber = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:52.", 0);
            new EventTool().waitNoEvent(1000);
        }
        expandNodes();
    }
    
    /**
     *
     */
    public void tearDown() {
        JemmyProperties.getCurrentOutput().printTrace("\nteardown\n");
        if (getName().equals("testLocalVariablesValues")) {//last
            Utilities.endAllSessions();
        }
    }
    
    /**
     *
     */
    protected void expandNodes() {
        Utilities.showDebuggerView(Utilities.localVarsViewTitle);
        JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        TreeTableOperator treeTableOperator = new TreeTableOperator((javax.swing.JTable) jTableOperator.getSource());
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this").expand();
        //Utilities.sleep(500);
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Static").expand();
        //Utilities.sleep(500);
        new org.netbeans.jellytools.nodes.Node(treeTableOperator.tree(), "this|Inherited").expand();
        //Utilities.sleep(500);
    }
    
    /**
     *
     */
    public void testLocalVariablesThisNode() throws Throwable {
        try {
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            checkTreeTableLine(jTableOperator, 2, "Vpublic", "String", "\"Public Variable\"");
            checkTreeTableLine(jTableOperator, 3, "Vprotected", "String", "\"Protected Variable\"");
            checkTreeTableLine(jTableOperator, 4, "Vprivate", "String", "\"Private Variable\"");
            checkTreeTableLine(jTableOperator, 5, "VpackagePrivate", "String", "\"Package-private Variable\"");
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
    public void testLocalVariablesStaticNode() throws Throwable {
        try {
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            checkTreeTableLine(jTableOperator, 10, "Spublic", "String", "\"Public Variable\"");
            checkTreeTableLine(jTableOperator, 11, "Sprotected", "String", "\"Protected Variable\"");
            checkTreeTableLine(jTableOperator, 12, "Sprivate", "String", "\"Private Variable\"");
            checkTreeTableLine(jTableOperator, 13, "SpackagePrivate", "String", "\"Package-private Variable\"");
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
    public void testLocalVariablesStaticInherited() throws Throwable {
        try {
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            checkTreeTableLine(jTableOperator, 15, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
            checkTreeTableLine(jTableOperator, 16, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
            checkTreeTableLine(jTableOperator, 17, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
            checkTreeTableLine(jTableOperator, 18, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
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
    public void testLocalVariablesInheritedNode() throws Throwable {
        try {
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            checkTreeTableLine(jTableOperator, 20, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
            checkTreeTableLine(jTableOperator, 21, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
            checkTreeTableLine(jTableOperator, 22, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
            checkTreeTableLine(jTableOperator, 23, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
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
    public void testLocalVariablesExtended() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.setCaret(eo, 76);
            new RunToCursorAction().perform();
            consoleLineNumber = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:76.", consoleLineNumber);
            new EventTool().waitNoEvent(1000);
            
            Utilities.showDebuggerView(Utilities.localVarsViewTitle);
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
            assertTrue("Node bundle has no child nodes", hasChildNodes("this|Static|bundle", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "msgMemory", "MessageFormat", null);
            assertTrue("Node msgMemory has no child nodes", hasChildNodes("this|Static|msgMemory", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "UPDATE_TIME", "int", "1000");
            checkTreeTableLine(jTableOperator, count++, "Spublic", "String", "\"Public Variable\"");
            checkTreeTableLine(jTableOperator, count++, "Sprotected", "String", "\"Protected Variable\"");
            checkTreeTableLine(jTableOperator, count++, "Sprivate", "String", "\"Private Variable\"");
            checkTreeTableLine(jTableOperator, count++, "SpackagePrivate", "String", "\"Package-private Variable\"");
            checkTreeTableLine(jTableOperator, count++, "class$java$lang$Runtime", "Class", "class java.lang.Runtime");
            assertTrue("Node class$java$lang$Runtime has no child nodes", hasChildNodes("this|Static|class$java$lang$Runtime", treeTableOperator));
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
            assertTrue("Node clazz has no child nodes", hasChildNodes("clazz", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "string", "String", "\"Hi!\"");
            checkTreeTableLine(jTableOperator, count++, "n", "int", "50");
            checkTreeTableLine(jTableOperator, count++, "llist", "LinkedList", null);
            assertTrue("Node llist has no child nodes", hasChildNodes("llist", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "alist", "ArrayList", null);
            assertTrue("Node alist has no child nodes", hasChildNodes("alist", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "vec", "Vector", null);
            assertTrue("Node vec has no child nodes", hasChildNodes("vec", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "hmap", "HashMap", null);
            assertTrue("Node hmap has no child nodes", hasChildNodes("hmap", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "htab", "Hashtable", null);
            assertTrue("Node htab has no child nodes", hasChildNodes("htab", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "tmap", "TreeMap", null);
            assertTrue("Node tmap has no child nodes", hasChildNodes("tmap", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "hset", "HashSet", null);
            assertTrue("Node hset has no child nodes", hasChildNodes("hset", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "tset", "TreeSet", null);
            assertTrue("Node tset has no child nodes", hasChildNodes("tset", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "policko", "int[]", null);
            assertTrue("Node policko has no child nodes", hasChildNodes("policko", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "pole", "int[]", null);
            assertTrue("Node pole has no child nodes", hasChildNodes("pole", treeTableOperator));
            checkTreeTableLine(jTableOperator, count++, "d2", "int[][]", null);
            assertTrue("Node d2 has no child nodes", hasChildNodes("d2", treeTableOperator));
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
    public void testLocalVariablesValues() throws Throwable {
        try {
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.setCaret(eo, 104);
            new EventTool().waitNoEvent(500);
            new RunToCursorAction().performMenu();
            new EventTool().waitNoEvent(500);
            consoleLineNumber = Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:104.", consoleLineNumber);
            
            Utilities.showDebuggerView(Utilities.localVarsViewTitle);
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
     * check values in TreeTable line
     * @param table
     * @param lineNumber
     * @param name
     * @param type
     * @param value
     */
    protected void checkTreeTableLine(JTableOperator table, int lineNumber, String name, String type, String value) {
        try {
            table.scrollToCell(lineNumber, 0);
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
    
    protected boolean hasChildNodes(String nodePath, TreeTableOperator jTableOperator) {
        org.netbeans.jellytools.nodes.Node node = new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), nodePath);
        node.select();
        return !node.isLeaf();
    }
}
