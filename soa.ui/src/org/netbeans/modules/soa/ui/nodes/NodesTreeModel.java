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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.ui.nodes;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * The read only tree model based on the Nodes. 
 *
 * @author nk160297
 */
public class NodesTreeModel implements TreeModel {
    
    protected Node myRootNode;
    protected EventListenerList listenerList = new EventListenerList();
    
    public NodesTreeModel() {
    }
    
    public NodesTreeModel(Node rootNode) {
        myRootNode = rootNode;
    }
    
    public void setRootNode(Node newRootNode) {
        myRootNode = newRootNode;
    }

    public Object getRoot() {
        return myRootNode;
    }

    public int getIndexOfChild(Object parent, Object requiredChild) {
        assert parent instanceof Node;
        assert requiredChild instanceof Node;
        //
        Children children = ((Node)parent).getChildren();
        Node[] childNodesArr = children.getNodes();
        for (int index = 0; index < childNodesArr.length; index++) {
            Node child = childNodesArr[index];
            if (requiredChild.equals(child)) {
                return index;
            }
        }
        //
        return -1;
    }

    public Object getChild(Object parent, int index) {
        assert parent instanceof Node;
        //
        Children children = ((Node)parent).getChildren();
        Node[] childNodesArr = children.getNodes();
        if (index > childNodesArr.length) {
            return null;
        } else {
            return childNodesArr[index];
        }
    }

    public boolean isLeaf(Object node) {
        assert node instanceof Node;
        //
        return ((Node)node).getChildren().getNodesCount() == 0;
    }

    public int getChildCount(Object parent) {
        assert parent instanceof Node;
        //
        return ((Node)parent).getChildren().getNodesCount();
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // do nothing for a while
    }
    
}
