/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * 
 */
package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.explorer.ExplorerPanel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Test DefaulAction of node selected in a view.
 * @author Jiri Rechtacek
 */
public class DefaultActionTest extends NbTestCase {
    
    
    public DefaultActionTest (String name) {
        super(name);
    }
   
    public static void main (String args[]) {
         TestRunner.run (new NbTestSuite (DefaultActionTest.class));
    }
    
    /** Run all tests in AWT thread */
    public final void run (final junit.framework.TestResult result) {
        try {
            // XXX ExplorerManager when updating selected nodes
            // replanes all firing into AWT thread, therefore the test
            // has to run in AWT.
            javax.swing.SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {
                    DefaultActionTest.super.run (result);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace ();
            throw new IllegalStateException ();
        }
    }
    
    private boolean performed;
    private Node root;
    private List fails = new ArrayList ();
    
    protected void setUp () {
        final Children children = new Children.Array ();
        root = new AbstractNode (children);
        final NodeAction a = new NodeAction () {
            protected void performAction (Node[] activatedNodes) {
                log ("Action performed.");
                performed = true;
            }                 
            
            public boolean asynchronous () {
                return false;
            }

            protected boolean enable (Node[] activatedNodes) {
                return true;
            }
            
            
            public HelpCtx getHelpCtx () {
                return null;
            }
            
            public String getName () {
                return "Test default action";
            }
        };
        children.add (new Node[] {new AbstractNode (Children.LEAF) {
            public String getName () {
                return "Node with default action";
            }
            public Action getPreferredAction () {
                return a;
            }
        }});
    }
    
    private TopComponent prepareExplorerPanel (JScrollPane view) {
        final ExplorerPanel p = new ExplorerPanel ();
        p.setSize (200, 200);
        p.add (view, BorderLayout.CENTER);
        p.getExplorerManager ().setRootContext (root);

        try {
            p.getExplorerManager ().setSelectedNodes (root.getChildren().getNodes ());
        } catch (PropertyVetoException pve) {
            fail (pve.getMessage ());
        }
        
        return p;
    }

    private void invokeDefaultAction (final TopComponent tc) {
        performed = false;
        try {
            Node[] nodes = tc.getActivatedNodes ();
            assertNotNull ("View has the active nodes.", nodes);
            Node n = nodes.length > 0 ? nodes[0] : null;
            assertNotNull ("View has a active node.", n);
            
            final Action action = n.getPreferredAction ();
            action.actionPerformed (new ActionEvent (n, ActionEvent.ACTION_PERFORMED, ""));
            
            // wait to invoke action is propagated
            Thread.sleep (300);
        } catch (Exception x) {
            fail (x.getMessage ());
        }
    }
    
    public void testListView () {
        TopComponent tc = prepareExplorerPanel (new ListView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("ListView");
        tc.close ();
    }
    
    public void testBeanTreeView () {
        TopComponent tc = prepareExplorerPanel (new BeanTreeView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("BeanTreeView");
    }
    
    public void testTreeTableView () {
        TopComponent tc = prepareExplorerPanel (new TreeTableView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("TreeTableView");
    }
    
    public void testTreeContextView () {
        TopComponent tc = prepareExplorerPanel (new ContextTreeView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("ContextTreeView");
    }
    
    void assertDefaultActionWasPerformed (String nameOfView) {
        assertTrue ("[" + nameOfView + "] DefaultAction was preformed.", performed);
    }
    
}
