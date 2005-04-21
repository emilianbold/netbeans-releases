/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.explorer.view;

import org.openide.explorer.*;
import org.openide.nodes.Node;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;


/** Context tree view class.
* @author   Petr Hamernik
*/
public class ContextTreeView extends TreeView {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8282594827988436813L;

    /** Constructor.
    */
    public ContextTreeView() {
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /* @return true if this TreeView accept the selected beans.
    */
    protected boolean selectionAccept(Node[] nodes) {
        if (nodes.length == 0) {
            return true;
        }

        Node parent = nodes[0].getParentNode();

        for (int i = 1; i < nodes.length; i++) {
            if (nodes[i].getParentNode() != parent) {
                return false;
            }
        }

        return true;
    }

    /* Called whenever the value of the selection changes.
    * @param listSelectionEvent the event that characterizes the change.
    */
    protected void selectionChanged(Node[] nodes, ExplorerManager man)
    throws PropertyVetoException {
        if (nodes.length > 0) {
            man.setExploredContext(nodes[0]);
        }

        man.setSelectedNodes(nodes);
    }

    /** Expand the given path and makes it visible.
    * @param path the path
    */
    protected void showPath(TreePath path) {
        tree.makeVisible(path);

        Rectangle rect = tree.getPathBounds(path);

        if (rect != null) {
            rect.width += rect.x;
            rect.x = 0;
            tree.scrollRectToVisible(rect);
        }

        tree.setSelectionPath(path);
    }

    /** Shows selection to reflect the current state of the selection in the explorer.
    *
    * @param paths array of paths that should be selected
    */
    protected void showSelection(TreePath[] paths) {
        if (paths.length == 0) {
            tree.setSelectionPaths(new TreePath[0]);
        } else {
            tree.setSelectionPath(paths[0].getParentPath());
        }
    }

    /** Permit use of explored contexts.
    *
    * @return <code>true</code> always
    */
    protected boolean useExploredContextMenu() {
        return true;
    }

    /** Create model.
    */
    protected NodeTreeModel createModel() {
        return new NodeContextModel();
    }

    /** Excludes leafs from the model.
     */
    static final class NodeContextModel extends NodeTreeModel {
        //
        // Event filtering
        //
        private int[] newIndices;
        private Object[] newChildren;

        public java.lang.Object getChild(java.lang.Object parent, int index) {
            int superCnt = super.getChildCount(parent);
            int myCnt = 0;

            for (int i = 0; i < superCnt; i++) {
                Object origChild = super.getChild(parent, i);
                Node n = Visualizer.findNode(origChild);

                if (!n.isLeaf()) {
                    if (myCnt++ == index) {
                        return origChild;
                    }
                }
            }

            return null;
        }

        public int getChildCount(java.lang.Object parent) {
            int superCnt = super.getChildCount(parent);
            int myCnt = 0;

            for (int i = 0; i < superCnt; i++) {
                Node n = Visualizer.findNode(super.getChild(parent, i));

                if (!n.isLeaf()) {
                    myCnt++;
                }
            }

            return myCnt;
        }

        public int getIndexOfChild(java.lang.Object parent, java.lang.Object child) {
            int superCnt = super.getChildCount(parent);
            int myCnt = 0;

            for (int i = 0; i < superCnt; i++) {
                Object origChild = super.getChild(parent, i);

                if (child.equals(origChild)) {
                    return myCnt;
                }

                Node n = Visualizer.findNode(origChild);

                if (!n.isLeaf()) {
                    myCnt++;
                }
            }

            return -1;
        }

        public boolean isLeaf(java.lang.Object node) {
            return false;
        }

        /** Filters given childIndices and children to contain only non-leafs
         * return true if there is still something changed.
         */
        private boolean filterEvent(Object[] path, int[] childIndices, Object[] children) {
            assert (childIndices != null) && (children != null) : " ch: " + children + " indices: " + childIndices; // NOI18N
            assert children.length == childIndices.length : "They should be the same: " + children.length + " == " +
            childIndices.length; // NOI18N
            assert newChildren == null : "Children should be cleared: " + newChildren; // NOI18N
            assert newIndices == null : "indices should be cleared: " + newIndices; // NOI18N
            assert path.length > 0 : "Path has to be greater than zero " + path.length; // NOI18N

            VisualizerNode parent = (VisualizerNode) path[path.length - 1];

            int[] filter = new int[childIndices.length];
            int accepted = 0;

            for (int i = 0; i < childIndices.length; i++) {
                VisualizerNode n = (VisualizerNode) children[i];

                if (!n.isLeaf()) {
                    filter[accepted++] = i;
                }
            }

            if (accepted == 0) {
                return false;
            }

            newIndices = new int[accepted];
            newChildren = new Object[accepted];

            for (int i = 0; i < accepted; i++) {
                newChildren[i] = children[filter[i]];
                newIndices[i] = getIndexOfChild(parent, newChildren[i]);
            }

            return true;
        }

        /** Filters given childIndices and children to contain only non-leafs
         * return true if there is still something changed.
         */
        private boolean removalEvent(Object[] path, int[] childIndices, Object[] children) {
            assert (childIndices != null) && (children != null) : " ch: " + children + " indices: " + childIndices; // NOI18N
            assert children.length == childIndices.length : "They should be the same: " + children.length + " == " +
            childIndices.length; // NOI18N
            assert newChildren == null : "Children should be cleared: " + newChildren; // NOI18N
            assert newIndices == null : "indices should be cleared: " + newIndices; // NOI18N
            assert path.length > 0 : "Path has to be greater than zero " + path.length; // NOI18N

            VisualizerNode parent = (VisualizerNode) path[path.length - 1];

            int[] filter = new int[childIndices.length];
            int accepted = 0;

            for (int i = 0; i < childIndices.length; i++) {
                VisualizerNode n = (VisualizerNode) children[i];

                if (!n.isLeaf()) {
                    filter[accepted++] = i;
                }
            }

            if (accepted == 0) {
                return false;
            }

            newIndices = new int[accepted];
            newChildren = new Object[accepted];

            int size = getChildCount(parent);
            int index = 0;
            int myPos = 0;
            int actualI = 0;
            int i = 0;

            for (int pos = 0; pos < accepted;) {
                if (childIndices[index] <= i) {
                    VisualizerNode n = (VisualizerNode) children[index];

                    if (!n.isLeaf()) {
                        newIndices[pos] = myPos++;
                        newChildren[pos] = n;
                        pos++;
                    }

                    index++;
                } else {
                    VisualizerNode n = (VisualizerNode) getChild(parent, actualI++);

                    if ((n != null) && !n.isLeaf()) {
                        myPos++;
                    }
                }

                i++;
            }

            return true;
        }

        /* sends childIndices and children == null, no tranformation
        protected void fireTreeStructureChanged (Object source, Object[] path, int[] childIndices, Object[] children) {
            if (!filterEvent (childIndices, children)) return;
            super.fireTreeStructureChanged(source, path, newIndices, newChildren);
            newIndices = null;
            newChildren = null;
        }
         */
        protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
            if (!removalEvent(path, childIndices, children)) {
                return;
            }

            super.fireTreeNodesRemoved(source, path, newIndices, newChildren);
            newIndices = null;
            newChildren = null;
        }

        protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
            if (!filterEvent(path, childIndices, children)) {
                return;
            }

            super.fireTreeNodesInserted(source, path, newIndices, newChildren);
            newIndices = null;
            newChildren = null;
        }

        protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
            if (!filterEvent(path, childIndices, children)) {
                return;
            }

            super.fireTreeNodesChanged(source, path, newIndices, newChildren);
            newIndices = null;
            newChildren = null;
        }
    }
     // end of NodeContextModel
}
