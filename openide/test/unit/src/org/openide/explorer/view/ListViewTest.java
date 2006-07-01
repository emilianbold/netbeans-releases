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

/*
 *
 */
package org.openide.explorer.view;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JList;
import javax.swing.SwingUtilities;

import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.explorer.ExplorerPanel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Children.Array;

/**
 * Tests for class ListView
 */
public class ListViewTest extends NbTestCase {
    
    private static final int NO_OF_NODES = 3;

    
    public ListViewTest(String name) {
        super(name);
    }
   
    public static void main(String args[]) {
         TestRunner.run(new NbTestSuite(ListViewTest.class));
    }
    
    /**
     * 1. selects a node in a ListView
     * 2. removes the node
     * 3. Shift-Click another node by java.awt.Robot
     */
    public void testNodeSelectionByRobot() {
        final Children c = new Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();
        Node[] children = new Node[NO_OF_NODES];

        for (int i = 0; i < NO_OF_NODES; i++) {
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
        }
        //Thread.sleep(2000);
        
        for (int i = NO_OF_NODES-1; i >= 0; i--) {
            //Thread.sleep(500);
            
            // Waiting for until the view is updated.
            // This should not be necessary [HREBEJK]
            try {
                SwingUtilities.invokeAndWait( new EmptyRunnable() );
            } catch (InterruptedException ie) {
                fail ("Caught InterruptedException:" + ie.getMessage ());
            } catch (InvocationTargetException ite) {
                fail ("Caught InvocationTargetException: " + ite.getMessage ());
            }
       
            try {
                p.getExplorerManager().setSelectedNodes(new Node[] {children[i]} );
            } catch (PropertyVetoException  pve) {
                fail ("Caught the PropertyVetoException when set selected node " + children[i].getName ()+ ".");
            }
            
            //Thread.sleep(500);
            c.remove(new Node[] { children[i] });
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                fail ("Caught InterruptedException:" + ie.getMessage ());
            }
            
            if (lv.isShowing()) {
                Robot r = null;
                
                try {
                    r = new Robot();
                } catch (AWTException ae) {
                    fail ("Caught AWTException: " + ae.getMessage ());
                }
                
                r.keyPress(KeyEvent.VK_SHIFT);
                r.mouseMove(lv.getLocationOnScreen().x + 10,lv.getLocationOnScreen().y + 10);
                r.mousePress(InputEvent.BUTTON1_MASK);
                r.keyRelease(KeyEvent.VK_SHIFT);
                r.mouseRelease(InputEvent.BUTTON1_MASK);
            } else {
                fail();
            }
        }
    }
    
    /**
     * Removes selected node by calling destroy
     */
    public void testDestroySelectedNodes() {
        final Children c = new Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();
        Node[] children = new Node[NO_OF_NODES];

        for (int i = 0; i < NO_OF_NODES; i++) {
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
        }
        //Thread.sleep(2000);
        
        for (int i = NO_OF_NODES-1; i >= 0; i--) {     
            // Waiting for until the view is updated.
            // This should not be necessary
            try {
                SwingUtilities.invokeAndWait( new EmptyRunnable() );
            } catch (InterruptedException ie) {
                fail ("Caught InterruptedException:" + ie.getMessage ());
            } catch (InvocationTargetException ite) {
                fail ("Caught InvocationTargetException: " + ite.getMessage ());
            }
            try {
                p.getExplorerManager().setSelectedNodes(new Node[] {children[i]} );
            } catch (PropertyVetoException  pve) {
                fail ("Caught the PropertyVetoException when set selected node " + children[i].getName ()+ ".");
            }
            //Thread.sleep(500);
            try {
                children[i].destroy();
            } catch (IOException ioe) {
                fail ("Caught the IOException when destroy the node " + children[i].getName ()+ ".");
            }
        }
    }
    
    /**
     * Removes selected node by calling Children.Array.remove
     */
    public void testRemoveAndAddNodes() {
        final Children c = new Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();
        Node[] children = new Node[NO_OF_NODES];

        for (int i = 0; i < NO_OF_NODES; i++) {
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
        }
        //Thread.sleep(2000);
        
        try {
            // Waiting for until the view is updated.
            // This should not be necessary
            try {
                SwingUtilities.invokeAndWait( new EmptyRunnable() );
            } catch (InterruptedException ie) {
                fail ("Caught InterruptedException:" + ie.getMessage ());
            } catch (InvocationTargetException ite) {
                fail ("Caught InvocationTargetException: " + ite.getMessage ());
            }
            p.getExplorerManager().setSelectedNodes(new Node[] {children[0]} );
        } catch (PropertyVetoException  pve) {
            fail ("Caught the PropertyVetoException when set selected node " + children[0].getName ()+ ".");
        }
        
        for (int i = 0; i < NO_OF_NODES; i++) {
            c.remove(new Node [] { children[i] } );
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
            //Thread.sleep(350);
        }
        assertEquals(NO_OF_NODES, c.getNodesCount());
    }
    
    /**
     * Creates two nodes. Selects one and tries to remove it
     * and replace with the other one (several times).
     */
    public void testNodeAddingAndRemoving() {
        final Children c = new Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();

        final Node c1 = new AbstractNode(Children.LEAF);
        c1.setDisplayName("First");
        c1.setName("First");
        c.add(new Node[] { c1 });
        Node c2 = new AbstractNode(Children.LEAF);
        c2.setDisplayName("Second");
        c2.setName("Second");
        //Thread.sleep(500);

        for (int i = 0; i < 5; i++) {
            c.remove(new Node[] { c1 });
            c.add(new Node[] { c2 });
            
            // Waiting for until the view is updated.
            // This should not be necessary
            try {
                SwingUtilities.invokeAndWait( new EmptyRunnable() );
            } catch (InterruptedException ie) {
                fail ("Caught InterruptedException:" + ie.getMessage ());
            } catch (InvocationTargetException ite) {
                fail ("Caught InvocationTargetException: " + ite.getMessage ());
            }
            
            try {
                p.getExplorerManager().setSelectedNodes(new Node[] {c2} );
            } catch (PropertyVetoException  pve) {
                fail ("Caught the PropertyVetoException when set selected node " + c2.getName ()+ ".");
            }
            
            c.remove(new Node[] { c2 });
            c.add(new Node[] { c1 });
            
            //Thread.sleep(350);
        }
    }
    
    private static class PListView extends ListView {
        JList getJList() {
            return list;
        }
    }

    
    private class EmptyRunnable extends Object implements Runnable {

	public void run() {
	}

    }
    

}
