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

import org.openide.nodes.Node;
import org.openide.util.*;

import java.beans.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;


/** Model for displaying the nodes in tree.
*
* @author Jaroslav Tulach
*/
public class NodeTreeModel extends DefaultTreeModel {
    static final long serialVersionUID = 1900670294524747212L;

    /** listener used to listen to changes in trees */
    private transient Listener listener;

    /** Creates new NodeTreeModel
    */
    public NodeTreeModel() {
        super(VisualizerNode.EMPTY, true);
    }

    /** Creates new NodeTreeModel
    * @param root the root of the model
    */
    public NodeTreeModel(Node root) {
        super(VisualizerNode.EMPTY, true);
        setNode(root);
    }

    /** Changes the root of the model. This is thread safe method.
    * @param root the root of the model
    */
    public void setNode(final Node root) {
        Mutex.EVENT.readAccess(
            new Runnable() {
                public void run() {
                    VisualizerNode v = (VisualizerNode) getRoot();
                    VisualizerNode nr = VisualizerNode.getVisualizer(null, root);

                    if (v == nr) {
                        // no change
                        return;
                    }

                    v.removeNodeModel(listener());

                    nr.addNodeModel(listener());
                    setRoot(nr);
                }
            }
        );
    }

    /** Getter for the listener. Only from AWT-QUEUE.
    */
    private Listener listener() {
        if (listener == null) {
            listener = new Listener(this);
        }

        return listener;
    }

    /**
    * This sets the user object of the TreeNode identified by path
    * and posts a node changed.  If you use custom user objects in
    * the TreeModel you'returngoing to need to subclass this and
    * set the user object of the changed node to something meaningful.
    */
    public void valueForPathChanged(TreePath path, Object newValue) {
        if (path == null) {
            return;
        }

        Object o = path.getLastPathComponent();

        if (o instanceof VisualizerNode) {
            nodeChanged((VisualizerNode) o);

            return;
        }

        MutableTreeNode aNode = (MutableTreeNode) o;

        aNode.setUserObject(newValue);
        nodeChanged(aNode);
    }

    /** The listener */
    private static final class Listener implements NodeModel {
        /** weak reference to the model */
        private Reference<NodeTreeModel> model;

        /** Constructor.
        */
        public Listener(NodeTreeModel m) {
            model = new WeakReference<NodeTreeModel>(m);
        }

        /** Getter for the model or null.
        */
        private NodeTreeModel get(VisualizerEvent ev) {
            NodeTreeModel m = model.get();

            if ((m == null) && (ev != null)) {
                ev.getVisualizer().removeNodeModel(this);

                return null;
            }

            return m;
        }

        /** Notification of children addded event. Modifies the list of nodes
        * and fires info to all listeners.
        */
        public void added(VisualizerEvent.Added ev) {
            NodeTreeModel m = get(ev);

            if (m == null) {
                return;
            }

            m.nodesWereInserted(ev.getVisualizer(), ev.getArray());
        }

        /** Notification that children has been removed. Modifies the list of nodes
        * and fires info to all listeners.
        */
        public void removed(VisualizerEvent.Removed ev) {
            NodeTreeModel m = get(ev);

            if (m == null) {
                return;
            }

            m.nodesWereRemoved(ev.getVisualizer(), ev.getArray(), ev.removed.toArray());
        }

        /** Notification that children has been reordered. Modifies the list of nodes
        * and fires info to all listeners.
        */
        public void reordered(VisualizerEvent.Reordered ev) {
            NodeTreeModel m = get(ev);

            if (m == null) {
                return;
            }

            m.nodeStructureChanged(ev.getVisualizer());
        }

        /** Update a visualizer (change of name, icon, description, etc.)
        */
        public void update(VisualizerNode v) {
            NodeTreeModel m = get(null);

            if (m == null) {
                return;
            }

            m.nodeChanged(v);
        }

        /** Notification about large change in the sub tree
         */
        public void structuralChange(VisualizerNode v) {
            NodeTreeModel m = get(null);

            if (m == null) {
                return;
            }

            m.nodeStructureChanged(v);
        }
    }
}
