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

package org.netbeans.modules.soa.mappercore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author anjeleevich
 */
public abstract class FilteredTreeModel implements TreeModel, TreeModelListener {
    private TreeModel treeModel;
    
    private Map<Object, List<Object>> childrenMap = new HashMap<Object, 
            List<Object>>();
    
    private List<TreeModelListener> listeners;

    private HashMap<Object, Set<Object>> insertedChildrenMap 
            = new HashMap<Object, Set<Object>>();
    
    private boolean filter;
    
    public FilteredTreeModel(TreeModel treeModel, boolean filter) {
        this.filter = filter;
        this.treeModel = treeModel;
        treeModel.addTreeModelListener(this);
    }

    public Object getRoot() {
        return (treeModel == null) ? null : treeModel.getRoot();
    }
    
    protected abstract boolean accept(Object parent, Object child);
    
    private boolean isInsertedChild(Object parent, Object child) {
        Set<Object> children = insertedChildrenMap.get(parent);
        return (children != null) && children.contains(child);
    }
    
    private List<Object> getChildren(Object parent) {
        List<Object> children = childrenMap.get(parent);
        
        if (children == null) {
            children = new ArrayList<Object>();
            int count = treeModel.getChildCount(parent);
            
            for (int i = 0; i < count; i++) {
                Object child = treeModel.getChild(parent, i);
                if (accept(parent, child) || isInsertedChild(parent, child)) {
                    children.add(child);
                }
            }
            
            childrenMap.put(parent, children);
        }
        
        return children;
    }
    
    private void registerInsertedChildren(TreeModelEvent event) {
        Object[] children = event.getChildren();
        
        if (children == null) return;
        
        TreePath treePath = event.getTreePath();
        
        Object node = treePath.getLastPathComponent();
        
        Set<Object> insertedChildren = insertedChildrenMap.get(node);
        if (insertedChildren == null) {
            insertedChildren = new HashSet<Object>();
            insertedChildrenMap.put(node, insertedChildren);
        }
        
        for (Object child : children) {
            insertedChildren.add(child);
        }
    }
    
    private void resetChildren(Object parent) {
        childrenMap.remove(parent);
    }

    public Object getChild(Object parent, int index) {
        return (filter) 
                ? getChildren(parent).get(index)
                : treeModel.getChild(parent, index);
    }

    public int getChildCount(Object parent) {
        if (filter) {
            List<Object> children = getChildren(parent);
            return (children == null) ? 0 : children.size();
        } 
        return treeModel.getChildCount(parent);
    }

    public boolean isLeaf(Object node) {
        return treeModel.isLeaf(node);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        if (treeModel != null) {
            treeModel.valueForPathChanged(path, newValue);
        }
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (filter) {
            List<Object> children = getChildren(parent);
            return (children == null) ? -1 : children.indexOf(child);
        }
        return treeModel.getIndexOfChild(parent, child);
    }

    public void addTreeModelListener(TreeModelListener l) {
        if (listeners == null) {
            listeners = new ArrayList<TreeModelListener>();
        }
        listeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        if (listeners == null) return;
        int index = listeners.lastIndexOf(l);
        if (index >= 0) {
            listeners.remove(index);
            if (listeners.isEmpty()) listeners = null;
        }
    }
            
    
    private boolean hasListeners() {
        return listeners != null;
    }
     
    private TreeModelListener[] getListeners() {
        return (listeners == null) ? null : listeners
                .toArray(new TreeModelListener[listeners.size()]);
    }
    
    private TreeModelEvent filterTreeModelEvent(TreeModelEvent e) {
        if (!filter) {
            return new TreeModelEvent(this, e.getTreePath(), 
                    e.getChildIndices(), e.getChildren());
        }
        
        Object[] eventChildren = e.getChildren();
        if (eventChildren == null) {
            return null; // TODO
        }
        
        Object[] path = e.getPath();
        if (path == null) return null;
        
        List<Object> filteredChildren = null;
        
        for (int i = 0; i < path.length; i++) {
            Object node = path[i];
            if (isLeaf(node)) return null;
            if (filteredChildren != null && !filteredChildren.contains(node)) {
                return null;
            }
            filteredChildren = getChildren(node);
        }
        
        List<Object> children = new ArrayList<Object>();
        for (int i = 0; i < eventChildren.length; i++) {
            Object eventChild = eventChildren[i];
            if (filteredChildren.contains(eventChild)) {
                children.add(eventChild);
            }
        }
        
        if (children.isEmpty()) return null;
        
        int childrenCount = children.size();
        
        int[] indeces = new int[childrenCount];
        for (int i = 0; i < childrenCount; i++) {
            indeces[i] = filteredChildren.indexOf(children.get(i));
        }
        
        return new TreeModelEvent(this, e.getTreePath(), indeces,
                children.toArray(new Object[childrenCount]));
    }

    public void treeNodesChanged(TreeModelEvent e) {
        if (!hasListeners()) return;
        TreeModelEvent filteredEvent = filterTreeModelEvent(e);
        if (filteredEvent != null) {
            TreeModelListener[] listeners = getListeners();
            for (int i = listeners.length - 1; i >= 0; i--) {
                listeners[i].treeNodesChanged(filteredEvent);
            }
        }
    }

    public void treeNodesInserted(TreeModelEvent e) {
        resetChildren(e.getTreePath().getLastPathComponent());
        registerInsertedChildren(e);
        
        if (!hasListeners()) return;
        TreeModelEvent filteredEvent = filterTreeModelEvent(e);
        if (filteredEvent != null) {
            TreeModelListener[] listeners = getListeners();
            for (int i = listeners.length - 1; i >= 0; i--) {
                listeners[i].treeNodesInserted(filteredEvent);
            }
        }
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        if (!hasListeners()) return;
        TreeModelEvent filteredEvent = filterTreeModelEvent(e);
        if (filteredEvent != null) {
            TreeModelListener[] listeners = getListeners();
            for (int i = listeners.length - 1; i >= 0; i--) {
                listeners[i].treeNodesRemoved(filteredEvent);
            }
        }
    }

    public void treeStructureChanged(TreeModelEvent e) {
        if (!hasListeners()) return;
        
        if (filter) {
            Object[] path = e.getPath();
            List<Object> filteredChildren = null;
            for (int i = 0; i < path.length; i++) {
                Object node = path[i];
                if (filteredChildren != null) {
                    if (!filteredChildren.contains(node)) {
                        return;
                    }
                }
                filteredChildren = getChildren(node);
            }
        }
        
        TreeModelEvent filteredEvent = new TreeModelEvent(this, e.getTreePath());
        
        TreeModelListener[] listeners = getListeners();
        for (int i = listeners.length - 1; i >= 0; i--) {
            listeners[i].treeStructureChanged(filteredEvent);
        }
    }
    
    public void dispose() {
        treeModel.removeTreeModelListener(this);
    }
}
