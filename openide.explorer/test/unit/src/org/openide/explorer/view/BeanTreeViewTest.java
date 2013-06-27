/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for class BeanTreeViewTest
 */
public class BeanTreeViewTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(BeanTreeViewTest.class);
    }

    private static final int NO_OF_NODES = 3;
    static {
        System.setProperty("netbeans.debug.heap", "no wait");
    }
    
    
    public BeanTreeViewTest(String name) {
        super(name);
    }
    
    public void testOnlyChildRemoveCausesSelectionOfParent() throws Throwable {
        ExplorerManager em = doChildRemovalTest("one", "one");
        final List<Node> arr = Arrays.asList(em.getSelectedNodes());
        assertEquals("One selected: " + arr, 1, arr.size());
        assertEquals("Root selected", em.getRootContext(), arr.get(0));
    }
    
    public void testFirstChildRemovalCausesSelectionOfSibling() throws Throwable {
        doChildRemovalTest("foo");
    }
    public void testSecondChildRemovalCausesSelectionOfSibling() throws Throwable {
        doChildRemovalTest("bar");
    }
    public void testThirdChildRemovalCausesSelectionOfSibling() throws Throwable {
        doChildRemovalTest("bla");
    }

    private static Object holder;
    
    private void doChildRemovalTest(final String name) throws Throwable {
        doChildRemovalTest(name, "foo", "bar", "bla");
    }
    private ExplorerManager doChildRemovalTest(final String name, final String... childrenNames) throws Throwable {

        class AWTTst implements Runnable {
            AbstractNode root = new AbstractNode(new Children.Array());
            Node[] children;
            {
                List<Node> arr = new ArrayList<Node>();
                for (String s : childrenNames) {
                    arr.add(createLeaf(s));
                }
                children = arr.toArray(new Node[0]);
            }
            Panel p = new Panel();
            BeanTreeView btv = new BeanTreeView();
            JFrame f = new JFrame();
            JTree tree = btv.tree;
            Node operateOn;

            {
                root.setName("test root");
                root.getChildren().add(children);
                p.getExplorerManager().setRootContext(root);
                p.add(BorderLayout.CENTER, btv);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.getContentPane().add(BorderLayout.CENTER, p);
                f.setVisible(true);
            }

            @Override
            public void run() {

                for (int i = 0;; i++) {
                    if (name.equals(children[i].getName())) {
                        // this should select a sibling of the removed node
                        operateOn = children[i];
                        break;
                    }
                }

                try {
                    p.getExplorerManager().setSelectedNodes(new Node[]{operateOn});
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }

                TreePath[] paths = tree.getSelectionPaths();
                assertNotNull("Before removal: one node should be selected, but there are none.", paths);
                assertEquals("Before removal: one node should be selected, but there are none.", 1, paths.length);
                assertEquals("Before removal: one node should be selected, but there are none.", operateOn, Visualizer.findNode(paths[0].getLastPathComponent()));
                assertEquals("Before removal: one node should be selected, but there are none.", operateOn, Visualizer.findNode(tree.getAnchorSelectionPath().getLastPathComponent()));

                // this should select a sibling of the removed node
                root.getChildren().remove(new Node[]{operateOn});
                assertNotNull("After removal: one node should be selected, but there are none.", tree.getSelectionPath());
                children = null;
            }

            public void tryGc() {
                WeakReference<Node> wref = new WeakReference<Node>(operateOn);
                operateOn = null;
                EQFriendlyGC.assertGC("Node should be released.", wref);    
            }
        }
        AWTTst awt = new AWTTst();
        holder = awt;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        awt.tryGc();
        return awt.p.getExplorerManager();
    }
    
    public void testVisibleVisNodesAreNotGCed() throws InterruptedException, Throwable {
        doTestVisibleVisNodesAreNotGCed(false);
    }
    public void testVisibleVisNodesAreNotGCedAfterCollapseExpand() throws InterruptedException, Throwable {
        doTestVisibleVisNodesAreNotGCed(true);
    }

    public void doTestVisibleVisNodesAreNotGCed(final boolean collapseAndExpand) throws InterruptedException, Throwable {
        class AWTTst implements Runnable {

            AbstractNode root = new AbstractNode(new Children.Array());
            Node[] children = {
                createLeaf("foo"),
                createLeaf("bar"),
                createLeaf("bla")
            };
            VisualizerNode[] visNodes;
            Panel p = new Panel();
            BeanTreeView btv = new BeanTreeView();
            JFrame f = new JFrame();
            JTree tree = btv.tree;

            {
                root.setName("test root");
                root.getChildren().add(children);
                p.getExplorerManager().setRootContext(root);
                p.add(BorderLayout.CENTER, btv);
                f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
                f.getContentPane().add(BorderLayout.CENTER, p);
                f.setVisible(true);
            }

            public void run() {


                try {
                    p.getExplorerManager().setSelectedNodes(children);
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }

                TreePath[] paths = tree.getSelectionPaths();
                assertEquals("3 nodes should be selected.", 3, paths.length);
                visNodes = new VisualizerNode[NO_OF_NODES];
                for (int i = 0; i < visNodes.length; i++) {
                    visNodes[i] = (VisualizerNode) paths[i].getLastPathComponent();
                }

                try {
                    p.getExplorerManager().setSelectedNodes(new Node[0]);
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }

                paths = tree.getSelectionPaths();
                if (paths != null && paths.length == 0) {
                    paths = null;
                }
                assertNull("Nothing should be selected: " + Arrays.toString(paths), paths);
                
                if (collapseAndExpand) {
                    btv.collapseNode(root);
                    btv.expandNode(root);
                }
            }

            public void checkNotGc() {
                WeakReference<VisualizerNode> wref = new WeakReference<VisualizerNode>(visNodes[1]);
                visNodes = null;
                try {
                    EQFriendlyGC.assertGC("Node should be released.", wref);
                } catch (AssertionFailedError e) {
                    return;
                }
                fail("should not be GC");
            }
        }
        AWTTst awt = new AWTTst();
        holder = awt;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        awt.checkNotGc();
    }
    
    public void testSelectingRootDoesNotClearExploredContext() throws InterruptedException, Throwable {
        class AWTTst implements Runnable {

            AbstractNode root = new AbstractNode(new Children.Array());
            VisualizerNode visNode;
            Panel p = new Panel();
            BeanTreeView btv = new BeanTreeView();
            JFrame f = new JFrame();
            JTree tree = btv.tree;

            {
                root.setName("test root");
                p.getExplorerManager().setRootContext(root);
                p.add(BorderLayout.CENTER, btv);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.getContentPane().add(BorderLayout.CENTER, p);
                f.setVisible(true);
            }

            @Override
            public void run() {
                try {
                    btv.selectionChanged(new Node[] { root }, p.getExplorerManager());
                } catch (PropertyVetoException ex) {
                    fail(ex.getMessage());
                }
                
                assertSame("Root is explored", root, p.getExplorerManager().getExploredContext());
            }
            
        }
        AWTTst awt = new AWTTst();
        holder = awt;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }    
    
    public void testVisibleCollapsedNodesAreGCed() throws InterruptedException, Throwable {
        class AWTTst implements Runnable {

            AbstractNode root = new AbstractNode(new Children.Array());
            Node[] children = {
                createLeaf("foo"),
                createLeaf("bar"),
                createLeaf("bla")
            };
            VisualizerNode visNode;
            Panel p = new Panel();
            BeanTreeView btv = new BeanTreeView();
            JFrame f = new JFrame();
            JTree tree = btv.tree;

            {
                root.setName("test root");
                root.getChildren().add(children);
                p.getExplorerManager().setRootContext(root);
                p.add(BorderLayout.CENTER, btv);
                f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
                f.getContentPane().add(BorderLayout.CENTER, p);
                f.setVisible(true);
            }

            public void run() {


                try {
                    p.getExplorerManager().setSelectedNodes(new Node[] {children[0]});
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }

                TreePath[] paths = tree.getSelectionPaths();
                assertEquals("one node should be selected.", 1, paths.length);
                visNode = (VisualizerNode) paths[0].getLastPathComponent();

                try {
                    p.getExplorerManager().setSelectedNodes(new Node[0]);
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }
                paths = tree.getSelectionPaths();
                if (paths != null && paths.length == 0) {
                    paths = null;
                }
                assertNull("Nothing should be selected: " + Arrays.toString(paths), paths);

                btv.collapseNode(children[0].getParentNode());
            }
            
            public void checkGc() {
                WeakReference<VisualizerNode> wref = new WeakReference<VisualizerNode>(visNode);
                visNode = null;
                EQFriendlyGC.assertGC("Collapsed - should be GCed.", wref);
            }            
        }
        AWTTst awt = new AWTTst();
        holder = awt;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        awt.checkGc();
    }    
    
    private static Node createLeaf(String name) {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(name);
        return n;
    }
    
    private static class Panel extends JPanel
            implements ExplorerManager.Provider {
        private ExplorerManager em = new ExplorerManager();
        
        public ExplorerManager getExplorerManager() {
            return em;
        }
    }
}
