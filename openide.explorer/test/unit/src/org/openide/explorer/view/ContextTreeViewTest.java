/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for class ContextTreeViewTest
 */
public class ContextTreeViewTest extends NbTestCase {
    
    private static final int NO_OF_NODES = 3;
    
    
    public ContextTreeViewTest(String name) {
        super(name);
    }
    
    public void testLeafNodeReallyNotDisplayed() throws Throwable {
        final AbstractNode root = new AbstractNode(new Children.Array());
        root.setName("test root");
        
        
        
        root.getChildren().add(new Node[] {
            createLeaf("kuk"),
            createLeaf("huk"),
        });
        
        Panel p = new Panel();
        p.getExplorerManager().setRootContext(root);
        
        ContextTreeView ctv = new ContextTreeView();
        p.add(BorderLayout.CENTER, ctv);
        
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
        f.getContentPane().add(BorderLayout.CENTER, p);
        f.setVisible(true);
        
        final JTree tree = ctv.tree;
        
        class AWTTest implements Runnable {
            public void run() {
                // wait a while till the frame is realized and ctv.addNotify called
                Object r = tree.getModel().getRoot();
                assertEquals("There is root", Visualizer.findVisualizer(root), r);
                
                int cnt = tree.getModel().getChildCount(r);
                if (cnt != 0) {
                    fail("Should be zero " + cnt + " but there was:  " +
                            tree.getModel().getChild(r, 0) + " and " +
                            tree.getModel().getChild(r, 1)
                            );
                }
                assertEquals("No children as they are leaves", 0, cnt);
            }
        }
        AWTTest awt = new AWTTest();
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
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
