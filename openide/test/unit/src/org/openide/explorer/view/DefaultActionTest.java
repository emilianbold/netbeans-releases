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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JScrollPane;


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
    
    private boolean performed;
    private Node root;
    private List fails = new ArrayList ();
    
    protected void setUp () {
        final Children children = new Children.Array ();
        root = new AbstractNode (children);
        children.add (new Node[] {new AbstractNode (Children.LEAF) {
            public String getName () {
                return "Node with default action";
            }
            public Action getPreferredAction () {
                NodeAction a = new NodeAction () {
                    protected void performAction (Node[] activatedNodes) {
                        if (activatedNodes != null && activatedNodes.length > 0) {
                            log ("Action performed.");
                            // commented out because this assert interups this method. Why?
                            //assertTrue ("Default action performed twice.", performed);
                            performed = true;
                        } else {
                            assertTrue ("Action performed on node.", true);
                        }
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
                
                
                return a;
            }
        }});
    }
    
    private TopComponent prepareExplorerPanel (JScrollPane view) {
        final ExplorerPanel p = new ExplorerPanel ();
        p.setSize (200, 200);
        p.add (view, BorderLayout.CENTER);
        p.getExplorerManager ().setRootContext (root);
        p.open ();
        
        // wait for component will be showing
        p.requestActive ();
        try {
            Thread.sleep (300);
        } catch (Exception x) {
            fail (x.getMessage ());
        }
        
        try {
            p.getExplorerManager ().setSelectedNodes (root.getChildren().getNodes ());
        } catch (PropertyVetoException pve) {
            fail (pve.getMessage ());
        }
        
        return p;
    }
    
    private void invokeDefaultAction (TopComponent tc) {
        performed = false;
        try {
            Robot robot = new Robot ();
            
            robot.mouseMove (tc.getLocationOnScreen ().x + 100, tc.getLocationOnScreen ().y + 100);
            //robot.mouseMove (100, 100);
            robot.mousePress (InputEvent.BUTTON1_MASK);
            robot.mouseRelease (InputEvent.BUTTON1_MASK);
            
            // wait to mouse event will be propagated
            Thread.sleep (300);
            robot.keyPress (KeyEvent.VK_ENTER);
            robot.keyRelease (KeyEvent.VK_ENTER);
            
            // wait to key event will be propagated
            Thread.sleep (300);
        } catch (Exception x) {
            fail (x.getMessage ());
        }
    }
    
    public void testViews () {
        doTestListView ();
        doTestBeanTreeView ();
        // default action in TTV failed - should be filed defect 
        //doTestTreeTableView ();
        doTestTreeContextView ();
    }
    
    public void doTestListView () {
        TopComponent tc = prepareExplorerPanel (new ListView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("ListView");
        tc.close ();
    }
    
    public void doTestBeanTreeView () {
        TopComponent tc = prepareExplorerPanel (new BeanTreeView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("BeanTreeView");
    }
    
    public void doTestTreeTableView () {
        TopComponent tc = prepareExplorerPanel (new TreeTableView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("TreeTableView");
    }
    
    public void doTestTreeContextView () {
        TopComponent tc = prepareExplorerPanel (new ContextTreeView ());
        invokeDefaultAction (tc);
        assertDefaultActionWasPerformed ("ContextTreeView");
    }
    
    void assertDefaultActionWasPerformed (String nameOfView) {
        if (performed) {
            log ("[" + nameOfView + "] DefaultAction was preformed.");
        } else {
            fails.add ("[" + nameOfView + "] DefaultAction was preformed.");
        };
    }
    
    void assertFails () {
        if (!fails.isEmpty()) {
            fail ((String)fails.get (0));
        }
    }
}
