/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * 
 */

package org.openide.explorer.view;

import java.lang.ref.*;
import java.awt.event.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeSelectionModel;

import org.openide.*;
import org.openide.explorer.*;
import org.openide.nodes.*;
import org.openide.util.*;
import junit.framework.*;
import junit.textui.TestRunner;
import org.netbeans.junit.*;

/**
 * Tests for control selection mode on TreeView (test on BeanTreeView).
 * Note: here are used TreeView's method which hasn't been introduced yet,
 * will be introduced when the enh 11928 will be implemented.
 * Run test with attached patch in issue 11928.
 *
 * @author Jiri Rechtacek
 * @see "#11928"
 */
public class SelectionModeTest extends NbTestCase {
    
    ExplorerManager mgr;
    TreeView tree;
    Node[] singleSelection, contiguousSelection, discontiguousSelection;
    
    public SelectionModeTest(String name) {
        super(name);
    }
   
    public static void main (String args[]) {
        TestRunner.run (new NbTestSuite (SelectionModeTest.class));
        System.exit(0);
    }


    protected boolean runInEQ() {
        return true;
    }
    
    /** Create tree and a selection of nodes for test.
     */
    protected void setUp () {
        // disable any lookup, to isolate the test from other registered 
        // subsystems like core/windows
        System.setProperty ("org.openide.util.Lookup", "-");
        
        // create tree:
        // root +--- parent_one +--- one1
        //                      |--- one2
        //      |--- parent_two +--- two1
        //      |--- leaf
        
        final Children parents = new Children.Array ();
        Node root = new AbstractNode (parents);
        root.setName ("root");
        
        tree = new BeanTreeView ();
        //tree = new ContextTreeView ();
        
        final ExplorerPanel p = new ExplorerPanel ();
        p.setName ("SelectionModeTest");
        
        p.add (tree, BorderLayout.CENTER);
        p.getExplorerManager ().setRootContext (root);
        p.open ();

        final Children ones = new Children.Array ();
        Node parent_one = new AbstractNode (ones);
        parent_one.setName ("parent_one");
        final Children twos = new Children.Array ();
        Node parent_two = new AbstractNode (twos);
        parent_two.setName ("parent_two");

        final Node one1 = new AbstractNode (Children.LEAF);
        one1.setName("one1");

        final Node one2 = new AbstractNode (Children.LEAF);
        one2.setName("one2");

        ones.add(new Node[] { one1, one2 });
        
        final Node two1 = new AbstractNode (Children.LEAF);
        two1.setName("two1");

        twos.add (new Node[] { two1 });
        
        parents.add (new Node[] { parent_one, parent_two });
        
        
        // the test selections
        singleSelection = new Node[] {parent_two};
        contiguousSelection = new Node[] {one1, one2};
        discontiguousSelection = new Node[] {one2, two1};
        
        mgr = p.getExplorerManager();
    }

    /** Test set all nodes selections if the mode SINGLE_TREE_SELECTION is set.
     * @throws Exception  */    
    public void testSingleSelectionMode () throws Exception {
        // try setSelectionMode; if not present then fail
        setSelectionMode (tree, TreeSelectionModel.SINGLE_TREE_SELECTION);
        PropertyVetoException exp = null;
        
        // single
        try {
            // have to be equal
            assertTrue ("[MODE: SINGLE_TREE_SELECTION][NODES: single node] getSelectedNodes is NOT equal setSelectedNodes.", // NO18N
                trySelection (mgr, singleSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: SINGLE_TREE_SELECTION][NODES: single node] PropertyVetoException can't be thrown."); // NO18N
        }
        
        // contiguous
        try {
            exp = null;
            // cont' be equal
            assertTrue ("[MODE: SINGLE_TREE_SELECTION][NODES: two nodes contiguous] Can't be getSelectedNodes equal setSelectedNodes.", // NO18N
                !trySelection (mgr, contiguousSelection));
        } catch (PropertyVetoException e) {
            // exp should be thrown
            exp = e;
        } finally {
            if (exp==null)
                fail ("[MODE: SINGLE_TREE_SELECTION][NODES: two nodes contiguous] PropertyVetoException was NOT thrown."); // NO18N
        }
        
        // discontiguous
        try {
            exp = null;
            assertTrue ("[MODE: SINGLE_TREE_SELECTION][NODES: two nodes discontiguous] Can't be getSelectedNodes equal setSelectedNodes.", // NO18N
                trySelection (mgr, discontiguousSelection));
        } catch (PropertyVetoException e) {
            // exp should be thrown
            exp = e;
        } finally {
            if (exp==null)
                fail ("[MODE: SINGLE_TREE_SELECTION][NODES: two nodes discontiguous] PropertyVetoException was NOT thrown."); // NO18N
        }
        
    }

    /** Test set all nodes selections if the mode CONTIGUOUS_TREE_SELECTION is set.
     * @throws Exception  */    
    public void testContigousSelection () throws Exception {
        // try setSelectionMode; if not present then fail
        setSelectionMode (tree, TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        PropertyVetoException exp = null;
        
        // single
        try {
            // have to be equal
            assertTrue ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: single node] getSelectedNodes is NOT equal setSelectedNodes.", // NO18N
                trySelection (mgr, singleSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: single node] PropertyVetoException can't be thrown."); // NO18N
        }
        
        // contiguous
        try {
            exp = null;
            // have to be equal
            assertTrue ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: two nodes contiguous] getSelectedNodes is NOT equal setSelectedNodes.", // NO18N
                trySelection (mgr, contiguousSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: two nodes contiguous] PropertyVetoException can't be thrown."); // NO18N
        }
        
        // discontiguous
        try {
            // cont' be equal
            exp = null;
            assertTrue ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: two nodes discontiguous] Can't be getSelectedNodes equal setSelectedNodes.", // NO18N
                trySelection (mgr, discontiguousSelection));
        } catch (PropertyVetoException e) {
            // exp should be thrown
            exp = e;
        } finally {
            if (exp==null)
                fail ("[MODE: CONTIGUOUS_TREE_SELECTION][NODES: two nodes discontiguous] PropertyVetoException was NOT thrown."); // NO18N
        }
        
    }

    /** Test set all nodes selections if the mode DISCONTIGUOUS_TREE_SELECTION is set.
     * @throws Exception  */    
    public void testDiscontigousSelection () throws Exception {
        // try setSelectionMode; if not present then fail
        setSelectionMode (tree, TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        PropertyVetoException exp = null;
        
        // single
        try {
            // have to be equal
            assertTrue ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: single node] getSelectedNodes is NOT equal setSelectedNodes.", // NO18N
                trySelection (mgr, singleSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: single node] PropertyVetoException can't be thrown."); // NO18N
        }
        
        // contiguous
        try {
            // have to be equal
            assertTrue ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: two nodes contiguous] getSelectedNodes is NOT equal setSelectedNodes.", // NO18N
                trySelection (mgr, contiguousSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: two nodes contiguous] PropertyVetoException can't be thrown."); // NO18N
        }
        
        // discontiguous
        try {
            // have to be equal
            assertTrue ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: two nodes discontiguous] Can't be getSelectedNodes equal setSelectedNodes.", // NO18N
                trySelection (mgr, discontiguousSelection));
        } catch (PropertyVetoException e) {
            // no exp should be thrown
            fail ("[MODE: DISCONTIGUOUS_TREE_SELECTION][NODES: two nodes discontiguous] PropertyVetoException was NOT thrown."); // NO18N
        }
        
    }
    
    
    /** Try set array of nodes and check a array which is get back.
     * @param mgr Explorer manager
     * @param selected arrar of nodes which will be set
     * @throws PropertyVetoException may be thrown from setSelecedNodes
     * @return  true if Explorer Manager returned same array as was set.*/    
    private boolean trySelection (ExplorerManager mgr, Node[] selected) throws PropertyVetoException {
        mgr.setSelectedNodes (selected);
        if (selected!=null) {
            return Arrays.equals (selected, mgr.getSelectedNodes ());
        }
        return true;
    }
    
    /** Set selection on TreeView if the method is present. If not then the test failed.
     * @param TreeView tree instance TreeView
     * @param int mode selection mode */    
    private void setSelectionMode (TreeView tree, int mode) {
        try {
            Class c = tree.getClass ();
            Method m = c.getMethod ("setSelectionMode", new Class[] {Integer.TYPE});
            m.invoke (tree, new Object[] {new Integer (mode)});
        } catch (NoSuchMethodException nsme) {
            fail ("The method setSelectionMode can't be called on this object. See enh #11928.");
        } catch (IllegalAccessException iae) {
            fail ("IllegalAccessException thrown from setSelectionMode.");
        } catch (InvocationTargetException ite) {
            fail ("InvocationTargetException thrown from setSelectionMode.");
        }
    }

}
