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

package org.openide.explorer.view;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for the quick search feature in the treeview.
 */
public class TreeViewQuickSearchTest extends NbTestCase {
    
    private static final int NO_OF_NODES = 3;
    
    
    public TreeViewQuickSearchTest(String name) {
        super(name);
    }
    
    public void testSubstringQuickSearch() throws Throwable {
        doQuickSearchTest(true);
    }
    public void testPrefixQuickSearch() throws Throwable {
        doQuickSearchTest(false);
    }
    
    private void doQuickSearchTest(final boolean substringSearch) throws Throwable {
        final AbstractNode root = new AbstractNode(new Children.Array());
        root.setName("test root");
        
        final Node[] children = {
            createLeaf("foo1"),
            createLeaf("foo2"),
            createLeaf("bar1"),
            createLeaf("bar2"),
            createLeaf("alpha"),
        };
        
        root.getChildren().add(children);
        
        final Panel p = new Panel();
        p.getExplorerManager().setRootContext(root);
        
        final BeanTreeView btv = new BeanTreeView();
        p.add(BorderLayout.CENTER, btv);
        btv.setUseSubstringInQuickSearch(substringSearch);
        
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
        f.getContentPane().add(BorderLayout.CENTER, p);
        f.pack();
        f.setVisible(true);
        
        final JTree tree = btv.tree;
        final Exception[]problem = new Exception[1];
        final Integer[] phase = new Integer[1];
        phase[0] = 0;
        class AWTTst implements Runnable {
            public void run() {
                try {
                    if (phase[0] == 0) {
                        btv.tree.requestFocus();
                        try {
                            p.getExplorerManager().setSelectedNodes(new Node[] { root });
                        } catch (PropertyVetoException e) {
                            fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                        }
                    }
                    if (phase[0] == 1) {
                        Robot robot = new Robot();
                        Point p = btv.tree.getLocationOnScreen();
                        robot.mouseMove(p.x + 10, p.y + 10);
                        robot.mousePress(0);
                        robot.mouseRelease(0);
                        robot.keyPress(KeyEvent.VK_A);
                        robot.keyRelease(KeyEvent.VK_A);
                    }
                    
                    if (phase[0] == 2) {
                        Node operateOn = substringSearch ? children[2] : children[4];
                        TreePath[] paths = tree.getSelectionPaths();
                        assertNotNull("One node should be selected, but there are none.", paths);
                        assertEquals("One node should be selected, but there are none.", 1, paths.length);
                        assertEquals("Wrong node selected.", operateOn, Visualizer.findNode(paths[0].getLastPathComponent()));
                    }
                } catch (AWTException ex) {
                    problem[0] = ex;
                }
            }
        }
        AWTTst awt = new AWTTst();
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        if (problem[0] != null) {
            throw problem[0];
        }
        Thread.sleep(1000);
        phase[0] = 1;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        if (problem[0] != null) {
            throw problem[0];
        }
        phase[0] = 2;
        Thread.sleep(1000);
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        if (problem[0] != null) {
            throw problem[0];
        }
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
