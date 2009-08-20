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
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
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
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.OutlineNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author ehucka, Jiri Vagner, cyhelsky
 */
public class LocalVariablesTest extends JellyTestCase {
    
    public final String version;
    
    String projectPropertiesTitle;
    
    /**
     *
     * @param name
     */
    public LocalVariablesTest(String name) {
       super(name);
        version  = getJDKVersionCode();
    }
    
    private String getJDKVersionCode() {
        String specVersion = System.getProperty("java.version");
        
        if (specVersion.startsWith("1.4"))
            return "jdk14";
        
        if (specVersion.startsWith("1.5"))
            return "jdk15";
        
        if (specVersion.startsWith("1.6"))
            return "jdk16";
        
        throw new IllegalStateException("Specification version: " + specVersion + " not recognized.");
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
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(LocalVariablesTest.class).addTest(
                "testLocalVariablesThisNode",
                "testLocalVariablesStaticNode",
                "testLocalVariablesStaticInherited",
                "testLocalVariablesInheritedNode",
                "testLocalVariablesExtended",
                "testLocalVariablesValues",
                "testLocalVariablesSubExpressions"
                )
                .enableModules(".*")
                .clusters(".*"));
    }
    
    /**
     *
     */
    public void setUp() throws IOException {
        openDataProjects(Utilities.testProjectName);
        new Action(null, Utilities.setMainProjectAction).perform(new ProjectsTabOperator().getProjectRootNode(Utilities.testProjectName));
        System.out.println("########  " + getName() + "  #######");        
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
    protected void expandNodes() {
        Utilities.showDebuggerView(Utilities.localVarsViewTitle);
        
        OutlineOperator lrOutlineOp = new OutlineOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
        
        OutlineNode lrThisNode = lrOutlineOp.getRootNode("this");
        lrThisNode.expand();
        //Utilities.sleep(500);
        new OutlineNode(lrThisNode, "Static").expand();
        //Utilities.sleep(500);
        new OutlineNode(lrThisNode, "Inherited").expand();
        //Utilities.sleep(500);
    }
    
    /**
     *
     */
    public void testLocalVariablesThisNode() throws Throwable {
        try {
            Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
            new EventTool().waitNoEvent(500);
            try {
                eo.clickMouse(50,50,1);
            } catch (Throwable t) {
                System.err.println(t.getMessage());
            }
            Utilities.toggleBreakpoint(eo, 52);
            new EventTool().waitNoEvent(500);
            try {
                Utilities.startDebugger();
            } catch (Throwable th) {
                new DebugProjectAction().perform(projectNode);
            }
            try {
                Utilities.waitStatusText("Thread main stopped at MemoryView.java:52.");
            } catch (TimeoutExpiredException e) {
                if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:52.")) {
                    System.err.println(e.getMessage());
                    throw e;
                }
            }
            expandNodes();
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            checkTreeTableLine(jTableOperator, 2, "Vpublic", "String", "\"Public Variable\"");
            checkTreeTableLine(jTableOperator, 3, "Vprotected", "String", "\"Protected Variable\"");
            checkTreeTableLine(jTableOperator, 4, "Vprivate", "String", "\"Private Variable\"");
            checkTreeTableLine(jTableOperator, 5, "VpackagePrivate", "String", "\"Package-private Variable\"");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }
    
    /**
     *
     */
    public void testLocalVariablesStaticNode() throws Throwable {
        try {
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
            new EventTool().waitNoEvent(500);
            Utilities.toggleBreakpoint(eo, 52);
            new EventTool().waitNoEvent(500);
            Utilities.startDebugger();
            try {
                Utilities.waitStatusText("Thread main stopped at MemoryView.java:52.");
            } catch (TimeoutExpiredException e) {
                if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:52.")) {
                    System.err.println(e.getMessage());
                    throw e;
                }
            }
            expandNodes();
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            checkTreeTableLine(jTableOperator, 10, "Spublic", "String", "\"Public Variable\"");
            checkTreeTableLine(jTableOperator, 11, "Sprotected", "String", "\"Protected Variable\"");
            checkTreeTableLine(jTableOperator, 12, "Sprivate", "String", "\"Private Variable\"");
            checkTreeTableLine(jTableOperator, 13, "SpackagePrivate", "String", "\"Package-private Variable\"");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }
    
    /**
     *
     */
    public void testLocalVariablesStaticInherited() throws Throwable {
        try {
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
            new EventTool().waitNoEvent(1500);
            Utilities.toggleBreakpoint(eo, 52);
            new EventTool().waitNoEvent(1500);
            Utilities.startDebugger();
            try {
                Utilities.waitStatusText("Thread main stopped at MemoryView.java:52.");
            } catch (TimeoutExpiredException e) {
                if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:52.")) {
                    System.err.println(e.getMessage());
                    throw e;
                }
            }
            expandNodes();
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            checkTreeTableLine(jTableOperator, 15, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
            checkTreeTableLine(jTableOperator, 16, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
            checkTreeTableLine(jTableOperator, 17, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
            checkTreeTableLine(jTableOperator, 18, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }
    
    /**
     *
     */
    public void testLocalVariablesInheritedNode() throws Throwable {
        try {Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
            new EventTool().waitNoEvent(500);
            Utilities.toggleBreakpoint(eo, 52);
            new EventTool().waitNoEvent(500);
            Utilities.startDebugger();
            try {
                Utilities.waitStatusText("Thread main stopped at MemoryView.java:52.");
            } catch (TimeoutExpiredException e) {
                if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:52.")) {
                    System.err.println(e.getMessage());
                    throw e;
                }
            }
            expandNodes();
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            checkTreeTableLine(jTableOperator, 20, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
            checkTreeTableLine(jTableOperator, 21, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
            checkTreeTableLine(jTableOperator, 22, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
            checkTreeTableLine(jTableOperator, 23, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }
    
    /**
     *
     */
    public void testLocalVariablesExtended() throws Throwable {
        try {
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
            new EventTool().waitNoEvent(500);
            Utilities.toggleBreakpoint(eo, 76);
            new EventTool().waitNoEvent(500);
            Utilities.startDebugger();
            try {
                Utilities.waitStatusText("Thread main stopped at MemoryView.java:76.");
            } catch (TimeoutExpiredException e) {
                if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:76.")) {
                    System.err.println(e.getMessage());
                    throw e;
                }
            }
            expandNodes();
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
            Utilities.captureScreen(this);
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
            EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
            new EventTool().waitNoEvent(500);
            Utilities.toggleBreakpoint(eo, 104);
            new EventTool().waitNoEvent(500);
            Utilities.startDebugger();
            try {
                Utilities.waitStatusText("Thread main stopped at MemoryView.java:104.");
            } catch (TimeoutExpiredException e) {
                if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104.")) {
                    System.err.println(e.getMessage());
                    throw e;
                }
            }
            expandNodes();
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
            Utilities.captureScreen(this);
            throw th;
        }
    }
    
    /**
     *
     */
    public void testLocalVariablesSubExpressions() throws Throwable {
        try {
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode);
            EditorOperator eo = new EditorOperator("MemoryView.java"); //NOI18N
            new EventTool().waitNoEvent(500);
            Utilities.toggleBreakpoint(eo, 104);
            new EventTool().waitNoEvent(500);
            Utilities.startDebugger();
            try {
                Utilities.waitStatusText("Thread main stopped at MemoryView.java:104.");
            } catch (TimeoutExpiredException e) {
                if (!Utilities.checkConsoleLastLineForText("Thread main stopped at MemoryView.java:104.")) {
                    System.err.println(e.getMessage());
                    throw e;
                }
            }
            expandNodes();
            new EventTool().waitNoEvent(500);
            Utilities.getStepOverExpressionAction().performShortcut();
            new EventTool().waitNoEvent(500);
            Utilities.getStepOverExpressionAction().performShortcut();
            new EventTool().waitNoEvent(500);
            Utilities.getStepOverExpressionAction().performShortcut();
            new EventTool().waitNoEvent(500);
            Utilities.getStepOverExpressionAction().performShortcut();
            new EventTool().waitNoEvent(500);
            Utilities.getStepOverExpressionAction().performShortcut();
            new EventTool().waitNoEvent(500);
            
            // TODO: Enable after fix of issue 132886 
            //new Action(Utilities.runMenu+"|"+Utilities.stepOverExpresItem, null).perform();
            
            Utilities.showDebuggerView(Utilities.localVarsViewTitle);
            JTableOperator jTableOperator = new JTableOperator(new TopComponentOperator(Utilities.localVarsViewTitle));
            int count = 0;
            checkTreeTableLine(jTableOperator, count++, "Before call to 'println()'", null, null);
            checkTreeTableLine(jTableOperator, count++, "Arguments", null, null);

            if (version.equals("jdk16")) {
                checkTreeTableLine(jTableOperator, count++, "Return values history", null, null);            
                checkTreeTableLine(jTableOperator, count++, "return <init>()", null, null);
                checkTreeTableLine(jTableOperator, count++, "return <init>()", null, null);
                checkTreeTableLine(jTableOperator, count++, "return <init>()", null, null);
                checkTreeTableLine(jTableOperator, count++, "return format()", "String", null);

                // TODO: Enable after fix of issue 132886 
                //checkTreeTableLine(jTableOperator, count++, "return println()", "String", null);
            }
            
            
        } catch (Throwable th) {
            Utilities.captureScreen(this);
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
