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


package org.openide.explorer;


import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import javax.swing.Action;

import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;


/**
 * Tests for <code>ExplorerPanel</code>.
 *
 * @author Peter Zavadsky
 */
public class ExplorerPanelTest extends NbTestCase {
    
    public ExplorerPanelTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ExplorerPanelTest.class);
        return suite;
    }

    /** Tests whether the cut, copy (callback) actions are enabled/disabled
     * in the right time, see # */
    public void testCutCopyActionsEnabling() throws Exception {
        // XXX ExplorerManager when updating selected nodes
        // replanes all firing into AWT thread, therefore the test
        // has to run in AWT.
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                implCutCopyActionsEnabling();
            }
        });
        
        if(exception != null) {
            throw exception;
        }
    }

    private Exception exception;
    
    private void implCutCopyActionsEnabling() {
        try {
            ExplorerPanel panel = new ExplorerPanel();

            Lookup context = panel.getLookup();

            Node enabledNode = new TestNode1();
            Node disabledNode = new TestNode2();

            panel.getExplorerManager().setRootContext(new TestRoot(
                new Node[] {enabledNode, disabledNode}));

            Action copy = ((ContextAwareAction)SystemAction.get(CopyAction.class)).createContextAwareInstance(context);
            Action cut = ((ContextAwareAction)SystemAction.get(CutAction.class)).createContextAwareInstance(context);

            assertTrue("Copy action has to be disabled", !copy.isEnabled());
            assertTrue("Cut action has to be disabled", !cut.isEnabled());

            panel.getExplorerManager().setSelectedNodes(new Node[] {enabledNode});

            assertTrue("Copy action has to be enabled", copy.isEnabled());
            assertTrue("Cut action has to be enabled", cut.isEnabled());

            panel.getExplorerManager().setSelectedNodes(new Node[] {disabledNode});

            assertTrue("Copy action has to be disabled", !copy.isEnabled());
            assertTrue("Cut action has to be disabled", !cut.isEnabled());
        } catch(Exception e) {
            exception = e;
        }
    }

    /** Test root node. */
    private static class TestRoot extends AbstractNode {
        public TestRoot(Node[] children) {
            super(new Children.Array());
            getChildren().add(children);
        }
    }
    
    /** Node which enables both cut and copy actions. */
    private static class TestNode1 extends AbstractNode {
        public TestNode1() {
            super(Children.LEAF);
        }
        
        public boolean canCopy() {
            return true;
        }
        public boolean canCut() {
            return true;
        }
    }
    
    /** Node which disables both cut and copy actions. */
    private static class TestNode2 extends AbstractNode {
        public TestNode2() {
            super(Children.LEAF);
        }
        
        public boolean canCopy() {
            return false;
        }
        public boolean canCut() {
            return false;
        }
    }
}
