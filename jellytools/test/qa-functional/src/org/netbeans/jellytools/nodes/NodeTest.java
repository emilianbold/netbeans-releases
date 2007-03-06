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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.nodes;

import java.io.PrintStream;
import java.util.Arrays;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.PasteActionNoBlock;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;

/** Test of org.netbeans.jellytools.nodes.
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class NodeTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public NodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NodeTest("testConstructor"));
        suite.addTest(new NodeTest("testSelect"));
        suite.addTest(new NodeTest("testSelectDoubleClick"));
        suite.addTest(new NodeTest("testPerformAPIAction"));
        suite.addTest(new NodeTest("testPerformAPIActionNoBlock"));
        suite.addTest(new NodeTest("testPerformMenuAction"));
        suite.addTest(new NodeTest("testPerformMenuActionNoBlock"));
        suite.addTest(new NodeTest("testPerformPopupAction"));
        suite.addTest(new NodeTest("testPerformPopupActionNoBlock"));
        suite.addTest(new NodeTest("testGetPath"));
        suite.addTest(new NodeTest("testGetParentPath"));
        suite.addTest(new NodeTest("testGetChildren"));
        suite.addTest(new NodeTest("testIsLeaf"));
        suite.addTest(new NodeTest("testIsPresent"));
        suite.addTest(new NodeTest("testVerifyPopup"));
        suite.addTest(new NodeTest("testWaitNotPresent"));
        suite.addTest(new NodeTest("testWaitChildNotPresent"));
        suite.addTest(new NodeTest("testIsChildPresent"));
        suite.addTest(new NodeTest("testNodeRecreated"));
        return suite;
    }
    
    private static Node projectRootNode;
    private static Node sourcePackagesNode;
    private static Node sample1Node;
    private static Node sampleClass1Node;
    
    /** method called before each testcase */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        if(projectRootNode == null) {
            projectRootNode = new ProjectsTabOperator().getProjectRootNode("SampleProject");
        }
        if(sourcePackagesNode == null) {
            sourcePackagesNode = new SourcePackagesNode("SampleProject");
        }
        if(sample1Node == null) {
            sample1Node = new Node(sourcePackagesNode, "sample1"); // NOI18N
        }
        if(sampleClass1Node == null) {
            sampleClass1Node = new Node(sample1Node, "SampleClass1.java"); // NOI18N
        }
    }
    
    /** method called after each testcase */
    protected void tearDown() {
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    // "Confirm Object Deletion"
    private static String confirmTitle = Bundle.getString("org.openide.explorer.Bundle",
                                               "MSG_ConfirmDeleteObjectTitle"); // NOI18N

    // "Runtime"
    private static final String runtimeLabel = Bundle.getString("org.netbeans.core.Bundle",
                                                                "UI/Runtime"); // NOI18N
    
    /** Test constructor  */
    public void testConstructor() {
        Node n = new Node(RuntimeTabOperator.invoke().tree(), "");
        assertEquals(n.getText(), runtimeLabel);
        
        Node sample1Node = new Node(sourcePackagesNode, "sample1"); // NOI18N
        Node node = new Node(sample1Node, "SampleClass1.java"); // NOI18N
        assertEquals(node.getText(), "SampleClass1.java");
        
        String children[] = sample1Node.getChildren();
        assertTrue(children.length>0);
        for (int i=0; i<children.length; i++) {
            assertEquals(new Node(sample1Node, i).getText(), children[i]);
        }
    }
    
    /** Test select */
    public void testSelect() {
        new ProjectsTabOperator().makeComponentVisible();
        sourcePackagesNode.select();
        TreePath[] tp = sourcePackagesNode.tree().getSelectionPaths();
        assertNotNull(tp);
        assertEquals(tp.length, 1);
        assertEquals(tp[0].getLastPathComponent().toString(), sourcePackagesNode.getText());

        Node n2 = new Node(sourcePackagesNode, "|");
        n2.addSelectionPath();
        tp = n2.tree().getSelectionPaths();
        assertNotNull(tp);
        assertEquals(tp.length, 2);
        assertEquals(tp[0].getLastPathComponent().toString(), sourcePackagesNode.getText());
        assertEquals(tp[1].getLastPathComponent().toString(), n2.getText());
    }
    
    /** Test whether select() called twice is not considered as double click. */
    public void testSelectDoubleClick() {
        boolean expanded = sourcePackagesNode.isExpanded();
        sourcePackagesNode.select();
        sourcePackagesNode.select();
        assertEquals("Not wanted double click. Wrong result of isExpanded().", expanded, sourcePackagesNode.isExpanded());  // NOI18N
    }
    
    /** Test performAPIAction */
    public void testPerformAPIAction() {
        sampleClass1Node.performAPIAction("org.openide.actions.PropertiesAction");  // NOI18N
        Utils.closeProperties("SampleClass1.java"); // NOI18N
    }
    
    /** Test performAPIActionNoBlock */
    public void testPerformAPIActionNoBlock() {
        Node node = new SourcePackagesNode("SampleProject"); // NOI18N
        node.performAPIActionNoBlock("org.openide.actions.FindAction");    // NOI18N
        new FindInFilesOperator().close();
    }
    
    /** Test performMenuAction */
    public void testPerformMenuAction() {
        // Window|Properties
        String propertiesPath = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window")+
                                "|"+
                                Bundle.getStringTrimmed("org.openide.actions.Bundle", "Properties");
        sampleClass1Node.performMenuAction(propertiesPath);
        Utils.closeProperties("SampleClass1.java"); // NOI18N
    }
    
    /** Test performMenuActionNoBlock */
    public void testPerformMenuActionNoBlock() {
        String helpItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Help");
        String aboutItem = Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle", "About");
        sourcePackagesNode.performMenuActionNoBlock(helpItem+"|"+aboutItem);    // NOI18N
        String aboutTitle = Bundle.getString("org.netbeans.core.startup.Bundle", "CTL_About_Title");
        new JDialogOperator(aboutTitle).close();
    }
    
    /** Test performPopupAction  */
    public void testPerformPopupAction() {
        // "Open"
        String openItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        sampleClass1Node.performPopupAction(openItem);
        new EditorOperator("SampleClass1.java").close();    // NOI18N
    }
    
    /** Test performPopupActionNoBlock */
    public void testPerformPopupActionNoBlock() {
        // "Delete"
        String deleteItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");
        sampleClass1Node.performPopupActionNoBlock(deleteItem);
        Utils.closeConfirmDialog();
    }
    
    /** Test getPath */
    public void testGetPath() {
        JTreeOperator tree = RuntimeTabOperator.invoke().tree();
        assertEquals(new Node(tree, "").getPath(), "");
        new ProjectsTabOperator().makeComponentVisible();
        assertTrue("Path ends with sample1", sample1Node.getPath().endsWith("sample1"));// NOI18N
        assertTrue("Paths ends with sample1|SampleClass1.java", sampleClass1Node.getPath().endsWith("sample1|SampleClass1.java")); // NOI18N
    }
    
    /** Test getParentPath */
    public void testGetParentPath() {
        JTreeOperator tree = RuntimeTabOperator.invoke().tree();
        assertNull(new Node(tree, "").getParentPath());
        new ProjectsTabOperator().makeComponentVisible();
        assertEquals(projectRootNode.getParentPath(), "");
        assertTrue("Parent path ends with sample1", sampleClass1Node.getParentPath().endsWith("sample1")); // NOI18N
    }
    
    /** golden test case of getChildren  */
    public void testGetChildren() {
        String children[] = sample1Node.getChildren();
        assertNotNull(children);
        Arrays.sort(children);
        PrintStream ref = getRef();
        for (int i=0; i<children.length; i++) {
            ref.println(children[i]);
        }
        ref.close();
        compareReferenceFiles();
    }
    
    /** Test isLeaf */
    public void testIsLeaf() {
        assertFalse(sample1Node.isLeaf());
        assertTrue(sampleClass1Node.isLeaf());
    }
    
    /** Test isPresent */
    public void testIsPresent() {
        new CopyAction().performAPI(sampleClass1Node);
        performPaste(sample1Node);
        Node node = new Node(sample1Node, "SampleClass11"); // NOI18N
        assertTrue(node.isPresent());
        new DeleteAction().perform(node);
        new NbDialogOperator(confirmTitle).yes();
        try {
            Thread.sleep(1000);
        } catch (Exception e){};
        assertTrue(!node.isPresent());
    }
    
    /** Test verifyPopup */
    public void testVerifyPopup() {
        // "Find..."
        String findItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Find");
        // "Delete"
        String deleteItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");
        sample1Node.verifyPopup(new String[] {findItem, deleteItem});
    }
    
    /** Test of waitNotPresent() method. */
    public void testWaitNotPresent() {
        new CopyAction().performAPI(sampleClass1Node);    // NOI18N
        performPaste(sample1Node);
        final Node duplNode = new Node(sample1Node, "SampleClass11"); // NOI18N
        new Thread(new Runnable() {
            public void run() {
                new DeleteAction().performAPI(duplNode);
                new NbDialogOperator(confirmTitle).yes();
            }
        }, "thread performing action through API").start(); // NOI18N
        duplNode.waitNotPresent();
    }
    
    /** Test of waitChildNotPresent() method. */
    public void testWaitChildNotPresent() {
        new CopyAction().performAPI(sampleClass1Node);// NOI18N
        performPaste(sample1Node);
        final Node duplNode = new Node(sample1Node, "SampleClass11");// NOI18N
        new Thread(new Runnable() {
            public void run() {
                new DeleteAction().performAPI(duplNode);
                new NbDialogOperator(confirmTitle).yes();
            }
        }, "thread performing action through API").start(); // NOI18N
        sample1Node.waitChildNotPresent("SampleClass11");// NOI18N
    }

    /** Test of isChildPresent() method. */
    public void testIsChildPresent() {
        assertTrue("Child SampleClass1.java should be present in sample1 package.", sample1Node.isChildPresent("SampleClass1.java"));    // NOI18N
    }
    
    /** Test functionality when node is re-created. */
    public void testNodeRecreated() throws Exception {
        new CopyAction().performAPI(sampleClass1Node);
        performPaste(sample1Node);
        final Node duplNode = new Node(sample1Node, "SampleClass11"); // NOI18N
        TreePath tp1 = duplNode.getTreePath();
        new DeleteAction().perform(duplNode);
        new NbDialogOperator(confirmTitle).yes();
        new CopyAction().performAPI(sampleClass1Node);
        performPaste(sample1Node);
        try {
            assertFalse("Original TreePath should be invalid.", duplNode.tree().getRowForPath(tp1) > 0);
            assertTrue("TreePath should be valid after re-creation.", duplNode.tree().getRowForPath(duplNode.getTreePath()) > 0);
            // test if NoSuchPathException is not thrown when performing action
            ImmutableNode immutableNode = new ImmutableNode(duplNode.tree(), tp1);
            new DeleteAction().perform(immutableNode);
            new NbDialogOperator(confirmTitle).no();
            assertTrue("ImmutableNode.getTreePath method should be called exactly 2 times to test it properly",
                       immutableNode.checkTest());
        } finally {
            new DeleteAction().perform(new Node(sample1Node, "SampleClass11"));
            new NbDialogOperator(confirmTitle).yes();
        }
    }

    /** Perform paste action and confirm refactoring dialog. */
    private static void performPaste(Node node) {
        new PasteActionNoBlock().performAPI(node);
        String copyClassTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_CopyClass");
        NbDialogOperator copyClassOper = new NbDialogOperator(copyClassTitle);
        // "Refactor"
        String refactorLabel = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_Finish");
        new JButtonOperator(copyClassOper, refactorLabel).push();
        copyClassOper.waitClosed();
    }
    
    /** Simulates wrong behaviour for action performing. */
    private static class ImmutableNode extends Node {
        
        public ImmutableNode(JTreeOperator treeOperator, TreePath treePath) {
            super(treeOperator, treePath);
        }
        public ImmutableNode(Node node) {
            super(node.treeOperator, node.treePath);
        }
        
        private int count = 0;
        
        /** It is called from Action.callPopup(). First time it should return
         * treePath which is currently invalid and it throws NoSuchPathException
         * in Action.callPopup(). Then it is called second time and this time
         * it is delegated to super class which should retturn correct TreePath.
         */
        public TreePath getTreePath() {
            count++;
            if(count > 1) {
                return super.getTreePath();
            }
            return treePath;
        }
        
        /** Should be called exactly 2 times to test it properly. */
        public boolean checkTest() {
            return count == 2;
        }
    }
}
