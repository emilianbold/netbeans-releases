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

package org.openide.explorer;

import java.util.logging.Level;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.explorer.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import java.util.Collections;
import java.util.Arrays;
import java.awt.BorderLayout;
import org.openide.explorer.view.BeanTreeView;
import javax.swing.JLabel;
import org.openide.util.HelpCtx;
import org.openide.util.io.NbMarshalledObject;

/** Testing behaviour of ExplorerActions in order to fix 33566
 */
public class ExplorerActionsTest extends NbTestCase {
    static {
        // initialize special TopComponent.Registry
        Object x = ActionsInfraHid.UT;
    }
    
    private static javax.swing.Action delete = org.openide.util.actions.SystemAction.get (
        org.openide.actions.DeleteAction.class
    );
    
    public ExplorerActionsTest (String name) {
        super(name);
    }
    
    protected Level logLevel() {
        return Level.FINER;
    }

    protected boolean runInEQ() {
        return true;
    }

    public void testGlobalStateInExplorerActionsIsImportant () throws Exception {
        EP panel = new EP (null);
        ExplorerPanel.setConfirmDelete(false);
        
        doDelete (panel);
    }
    
    public void testGlobalStateCanBeOverriden () throws Exception {
        ExplorerActions actions = new ExplorerActions ();
        actions.setConfirmDelete (false);
        
        ExplorerPanel.setConfirmDelete(true);
        EP panel = new EP (actions);

        doDelete (panel);
    }
    
    public void testGlobalStateOnDeserializedPanel () throws Exception {
        EP panel = new EP (null);
        ExplorerPanel.setConfirmDelete(false);
        setupExplorerManager (panel.getExplorerManager());
        
        NbMarshalledObject mar = new NbMarshalledObject (panel);
        Object obj = mar.get ();
        EP deserializedPanel = (EP) obj;
        
        // activate the actions
        ActionsInfraHid.UT.setActivated (deserializedPanel);
        deserializedPanel.componentActivated();
        
        ActionsInfraHid.UT.setCurrentNodes (deserializedPanel.getExplorerManager().getRootContext ().getChildren ().getNodes ());
        
        // deletes without asking a question, if the question appears something
        // is wrong
        delete.actionPerformed(new java.awt.event.ActionEvent (this, 0, ""));
    }
    
    /** Performs a delete */
    
    private void doDelete (EP panel) throws Exception {
        setupExplorerManager (panel.getExplorerManager());
        // activate the actions
        ActionsInfraHid.UT.setActivated (panel);
        panel.componentActivated();
        
        ActionsInfraHid.UT.setCurrentNodes (panel.getExplorerManager().getSelectedNodes());
        assertTrue ("Delete is allowed", delete.isEnabled());
        
        // deletes without asking a question, if the question appears something
        // is wrong
        delete.actionPerformed(new java.awt.event.ActionEvent (this, 0, ""));
    }
    
    private static class RootNode extends AbstractNode {
        public RootNode () {
            super (new Children.Array ());
        }
        public Node.Handle getHandle () {
            return new H ();
        }
        private static class H implements Node.Handle {
            H() {}
            static final long serialVersionUID = -5158460093499159177L;
            public Node getNode () throws java.io.IOException {
                Node n = new RootNode ();
                n.getChildren().add (new Node[] {
                    new Del ("H1"), new Del ("H2")
                });
                return n;
            }
        }
    }

    private static class Del extends AbstractNode {
        public Del (String name) {
            super (Children.LEAF);
            setName (name);
        }
        public boolean canDestroy () {
            return true;
        }
    }
    
    /** Setups an explorer manager to be ready to delete something.
     * @param em manager 
     */
    private static void setupExplorerManager (ExplorerManager em) throws Exception {
        AbstractNode root = new RootNode ();
        Node[] arr = new Node[] {
            new Del ("1"), new Del ("2")
        };
        root.getChildren().add (arr);
        
        em.setRootContext(root);
        em.setSelectedNodes(root.getChildren().getNodes());
        
        assertEquals (
            "Same nodes selected", 
            Arrays.asList (arr),
            Arrays.asList (root.getChildren ().getNodes ())
        );
    }
    
    /** Special ExplorerPanel that has method how to actiavate itself.
     */
    private static class EP extends ExplorerPanel {
        private ExplorerActions actions;
        Node rootNode = null;
        
        public EP () {
        }

        public EP (ExplorerActions actions) {
            this.actions = actions;
        }
        
        public void componentActivated () {
            super.componentActivated ();
            if (actions != null) {
                actions.attach(getExplorerManager ());
            }
        }
        
    }
}
