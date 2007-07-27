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

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;


/** Model for displaying the nodes in list and choice.
*
* @author Jaroslav Tulach
*/
public class NodeListModel extends AbstractListModel implements ComboBoxModel {
    static final long serialVersionUID = -1926931095895356820L;

    /** listener used to listen to changes in trees */
    private transient Listener listener;

    /** parent node */
    private transient VisualizerNode parent;

    /** originally selected item */
    private transient Object selectedObject;

    /** previous size */
    private transient int size;

    /** depth to display */
    private int depth = 1;

    /** map that assignes to each visualizer number of its children till
    * the specified depth.
    */
    private Map<VisualizerNode, Info> childrenCount;

    /** Creates new model.
    */
    public NodeListModel() {
        parent = VisualizerNode.EMPTY;
        selectedObject = VisualizerNode.EMPTY;
        clearChildrenCount();
    }

    /** Creates new model.
    * @param root the root of the model
    */
    public NodeListModel(Node root) {
        this();
        setNode(root);
    }

    /** Changes the root of the model. This is thread safe method.
    * @param root the root of the model
    */
    public void setNode(final Node root) {
        Mutex.EVENT.readAccess(
            new Runnable() {
                public void run() {
                    VisualizerNode v = VisualizerNode.getVisualizer(null, root);

                    if (v == parent) {
                        // no change
                        return;
                    }

                    removeAll();
                    parent.removeNodeModel(listener());

                    parent = v;
                    selectedObject = v;
                    clearChildrenCount();

                    addAll();
                    parent.addNodeModel(listener());
                }
            }
        );
    }

    /** Depth of nodes to display.
    * @param depth the depth
    */
    public void setDepth(int depth) {
        if (depth != this.depth) {
            this.depth = depth;
            clearChildrenCount();

            Mutex.EVENT.readAccess(
                new Runnable() {
                    public void run() {
                        removeAll();
                        addAll();
                    }
                }
            );
        }
    }

    /** Getter for depth.
    * @return number of levels to display
    */
    public int getDepth() {
        return depth;
    }

    /** Getter for the listener. Only from AWT-QUEUE.
    */
    private Listener listener() {
        if (listener == null) {
            listener = new Listener(this);
        }

        return listener;
    }

    //
    // model methods
    //

    /** Number of elements in the model.
    */
    public int getSize() {
        int s = findSize(parent, -1, depth);

        return s;
    }

    /** Child at given index.
    */
    public Object getElementAt(int i) {
        return findElementAt(parent, i, -1, depth);
    }

    /** Finds index of given object.
    * @param o object produced by this model
    * @return index, or -1 if the object is not in the list
    */
    public int getIndex(Object o) {
        getSize();

        Info i = childrenCount.get(o);

        return (i == null) ? (-1) : i.index;
    }

    /** Currently selected item.
    */
    public void setSelectedItem(Object anObject) {
        if (selectedObject != anObject) {
            selectedObject = anObject;
            fireContentsChanged(this, -1, -1);
        }
    }

    public Object getSelectedItem() {
        return selectedObject;
    }

    //
    // modification of the counting model
    //
    private void clearChildrenCount() {
        childrenCount = new HashMap<VisualizerNode, Info>(17);
    }

    /** Finds size of sub children excluding vis node.
    *
    * @param vis the visualizer to find the size for
    * @param index the index that should be assigned to vis
    * @param depth the depth to scan
    * @return number of children
    */
    private int findSize(VisualizerNode vis, int index, int depth) {
        Info info = childrenCount.get(vis);

        if (info != null) {
            return info.childrenCount;
        }

        // only my children
        int size = 0;

        info = new Info();
        info.depth = depth;
        info.index = index;

        if (depth-- > 0) {
            Iterator it = vis.getChildren().iterator();

            while (it.hasNext()) {
                VisualizerNode v = (VisualizerNode) it.next();

                // count node v
                size++;

                // now count children of node v
                size += findSize(v, index + size, depth);
            }
        }

        info.childrenCount = size;
        childrenCount.put(vis, info);

        return size;
    }

    /** Finds the child with requested index.
    *
    * @param vis the visualizer to find the size for
    * @param indx the index of requested child
    * @param depth the depth to scan
    * @return the children
    */
    private VisualizerNode findElementAt(VisualizerNode vis, int indx, int realIndx, int depth) {
        if (--depth == 0) {
            // last depth is handled in special way
            return (VisualizerNode) vis.getChildAt(indx);
        }

        Iterator it = vis.getChildren().iterator();

        while (it.hasNext()) {
            VisualizerNode v = (VisualizerNode) it.next();

            if (indx-- == 0) {
                return v;
            }

            int s = findSize(v, ++realIndx, depth);

            if (indx < s) {
                // search this child
                return findElementAt(v, indx, realIndx, depth);
            }

            // go to next child
            indx -= s;
            realIndx += s;
        }

        return vis;
    }

    /** Finds a depth for given model & object. Used from renderer.
    * @param m model
    * @param o the visualizer node
    * @return depth or 0 if not found
    */
    static int findVisualizerDepth(ListModel m, VisualizerNode o) {
        if (m instanceof NodeListModel) {
            NodeListModel n = (NodeListModel) m;
            Info i = n.childrenCount.get(o);

            if (i != null) {
                return n.depth - i.depth - 1;
            }
        }

        return 0;
    }

    //
    // Modifications
    //
    final void addAll() {
        size = getSize();

        if (size > 0) {
            fireIntervalAdded(this, 0, size - 1);
        }
    }

    final void removeAll() {
        if (size > 0) {
            fireIntervalRemoved(this, 0, size - 1);
        }
    }

    final void changeAll() {
        size = getSize();

        if (size > 0) {
            fireContentsChanged(this, 0, size - 1);
        }

        clearChildrenCount();
    }

    final void added(VisualizerEvent.Added ev) {
        VisualizerNode v = ev.getVisualizer();
        int[] indices = ev.getArray();

        //fire that model has been changed only when event source's (visualizer)
        //children are shown in the list
        if ((cachedDepth(v) <= 0) || (indices.length == 0)) {
            return;
        }

        clearChildrenCount();
        size = getSize();

        int seg = (parent == v) ? 0 : getIndex(v);
        fireIntervalAdded(this, indices[0] + seg, indices[indices.length - 1] + seg);
    }

    final void removed(VisualizerEvent.Removed ev) {
        VisualizerNode v = ev.getVisualizer();
        int[] indices = ev.getArray();

        //fire that model has been changed only when event source's (visualizer)
        //children are shown in the list
        if ((cachedDepth(v) <= 0) || (indices.length == 0)) {
            return;
        }

        clearChildrenCount();

        int seg = (parent == v) ? 0 : getIndex(v);
        fireIntervalRemoved(this, indices[0] + seg, indices[indices.length - 1] + seg);
    }

    final void update(VisualizerNode v) {
        // ensure the model is computed
        getSize();

        Info i = childrenCount.get(v);

        if (i != null) {
            fireContentsChanged(this, i.index, i.index);
        }
    }

    private int cachedDepth(VisualizerNode v) {
        getSize();

        Info i = childrenCount.get(v);

        if (i != null) {
            return i.depth;
        }

        // v is not in the model
        return -1;
    }

    /** The listener */
    private static final class Listener implements NodeModel {
        /** weak reference to the model */
        private Reference<NodeListModel> model;

        /** Constructor.
        */
        public Listener(NodeListModel m) {
            model = new WeakReference<NodeListModel>(m);
        }

        /** Getter for the model or null.
        */
        private NodeListModel get(VisualizerEvent ev) {
            NodeListModel m = model.get();

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
            NodeListModel m = get(ev);

            if (m == null) {
                return;
            }

            m.added(ev);
        }

        /** Notification that children has been removed. Modifies the list of nodes
        * and fires info to all listeners.
        */
        public void removed(VisualizerEvent.Removed ev) {
            NodeListModel m = get(ev);

            if (m == null) {
                return;
            }

            m.removed(ev);
        }

        /** Notification that children has been reordered. Modifies the list of nodes
        * and fires info to all listeners.
        */
        public void reordered(VisualizerEvent.Reordered ev) {
            NodeListModel m = get(ev);

            if (m == null) {
                return;
            }

            m.changeAll();
        }

        /** Update a visualizer (change of name, icon, description, etc.)
        */
        public void update(VisualizerNode v) {
            NodeListModel m = get(null);

            if (m == null) {
                return;
            }

            m.update(v);
        }

        /** Notification about big change in children
        */
        public void structuralChange(VisualizerNode v) {
            NodeListModel m = get(null);

            if (m == null) {
                return;
            }

            m.changeAll();
        }
    }

    /** Info for a component in model
    */
    private static final class Info extends Object {
        public int childrenCount;
        public int depth;
        public int index;

        Info() {
        }

        public String toString() {
            return "Info[childrenCount=" + childrenCount + ", depth=" + depth + // NOI18N
            ", index=" + index; // NOI18N
        }
    }
}
