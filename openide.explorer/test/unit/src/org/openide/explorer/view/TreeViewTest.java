/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.explorer.view;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import junit.framework.*;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Marian Petras
 */
public final class TreeViewTest extends TestCase {
    
    private TestTreeView treeView;
    private JFrame testWindow;
    private volatile boolean isScrolledDown;
    private final Object semaphore = new Object();
    
    public TreeViewTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests whether <code>JTree</code>'s property <code>scrollsOnExpand</code>
     * is taken into account in
     * <code>TreeView.TreePropertyListener.treeExpanded(...)</code>.
     */
    public void testAutoscrollOnOff() throws InterruptedException {
        assert !EventQueue.isDispatchThread();
        
        testWindow = new ExplorerWindow();
        testWindow.getContentPane().add(treeView = new TestTreeView());
        
        class WindowDisplayer implements Runnable {
            public void run() {
                testWindow.pack();
                testWindow.setVisible(true);
            }
        }
        
        class Detector implements Runnable {
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    EventQueue.invokeLater(this);
                    return;
                }
                
                isScrolledDown = !treeView.isUp();
                
                synchronized (semaphore) {
                    semaphore.notify();
                }
            }
        }

        class Tester implements Runnable {
            private final boolean autoscroll;
            private final int part;
            Tester(boolean autoscroll, int part) {
                this.autoscroll = autoscroll;
                this.part = part;
            }
            public void run() {
                assert (part == 1) || (part == 2);
                if (part == 1) {
                    treeView.collapse();
                    treeView.scrollUp();
                    assert treeView.isUp();
                } else {
                    treeView.setAutoscroll(autoscroll);
                    treeView.expand(); //<-- posts a request to the RequestProcessor
                    RequestProcessor.getDefault().post(new Detector(), 1000 /*ms*/);
                }
            }
        }

        try {
            EventQueue.invokeAndWait(new WindowDisplayer());
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
        
        //Wait for the AWT thread to actually display the dialog:
        Thread.currentThread().sleep(5000);
        
        EventQueue.invokeLater(new Tester(true, 1));
        Thread.currentThread().sleep(2000);      //wait for update of the screen
        EventQueue.invokeLater(new Tester(true, 2));
        synchronized (semaphore) {
            semaphore.wait();
        }
        assertTrue("Check the view has scrolled", isScrolledDown);

        EventQueue.invokeLater(new Tester(false, 1));
        Thread.currentThread().sleep(2000);      //wait for update of the screen
        EventQueue.invokeLater(new Tester(false, 2));
        synchronized (semaphore) {
            semaphore.wait();
        }
        assertTrue("Check the view has not scrolled", !isScrolledDown);

        EventQueue.invokeLater(new Tester(true, 1));    //just collapse the tree
        Thread.currentThread().sleep(2000);
    }
    
    
    private static final class TestTreeView extends BeanTreeView {
        
        private final Node rootNode;
        final JScrollBar verticalScrollBar;
        private transient ExplorerManager explManager;
        
        TestTreeView() {
            super();
            tree.setAutoscrolls(true);

            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            verticalScrollBar = getVerticalScrollBar();
            
            rootNode = new AbstractNode(new TreeChildren());
            rootNode.setDisplayName("Root node");
            
            tree.setRowHeight(20);
            
            Dimension prefSize = new Dimension(200, 6 * tree.getRowHeight() + 8);
            prefSize.width = (int) (prefSize.width * 1.25f)
                             + verticalScrollBar.getWidth();
            setPreferredSize(prefSize);
        }
        
        public void addNotify() {
            super.addNotify();
            explManager = ExplorerManager.find(this);
            explManager.setRootContext(rootNode);
            collapse();
        }
        
        void setAutoscroll(boolean autoscroll) {
            tree.setScrollsOnExpand(autoscroll);
        }
        
        void scrollUp() {
            verticalScrollBar.setValue(verticalScrollBar.getMinimum());
        }
        
        boolean isUp() {
            return verticalScrollBar.getValue()
                   == verticalScrollBar.getMinimum();
        }
        
        void expand() {
            tree.expandRow(4);
        }
        
        void collapse() {
            tree.collapseRow(4);
        }
        
        final static class TreeChildren extends Children.Array {
            
            private static final char[] letters
                    = new char[] {'A', 'B', 'C', 'D', 'E'};
            
            TreeChildren() {
                this(-1);
            }
            
            TreeChildren(final int first) {
                super();
                
                Node[] childNodes = new Node[5];
                int i;
                if (first == -1) {
                    for (i = 0; i < childNodes.length; i++) {
                        AbstractNode childNode = new AbstractNode(new TreeChildren(i));
                        childNode.setDisplayName("Child node " + i);
                        childNodes[i] = childNode;
                    }
                } else {
                    for (i = 0; i < childNodes.length; i++) {
                        AbstractNode childNode = new AbstractNode(Children.LEAF);
                        StringBuffer buf = new StringBuffer(3);
                        childNode.setDisplayName(buf.append(first)
                                                    .append('.')
                                                    .append(letters[i])
                                                    .toString());
                        childNodes[i] = childNode;
                    }
                }
                add(childNodes);
            }
            
        }
        
        
    }
    
    
    private static final class ExplorerWindow extends JFrame
                               implements ExplorerManager.Provider {
        
        private final ExplorerManager explManager = new ExplorerManager();
        
        ExplorerWindow() {
            super("TreeView test");                                     //NOI18N
        }
        
        public ExplorerManager getExplorerManager() {
            return explManager;
        }
        
    }
    
}
