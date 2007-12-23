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

package org.netbeans.modules.bpel.mapper.tree;

import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.tree.TreePath;

/**
 * The unified tree node for the MapperSwingTreeModel. This node 
 * is used for any kind of tree item. It uses caching of a data object, 
 * icon and display name. 
 * 
 * @author nk160297
 */
public final class MapperTreeNode<DataObject> {
    
    private DataObject mDataObject;
    private String mDisplayName; 
    private Icon mIcon;
    private MapperTreeNode mParent;
    private List<MapperTreeNode> mChildren;
    private Boolean mIsLeaf;
    
    public MapperTreeNode(MapperTreeNode parent, DataObject dataObject) {
        assert dataObject != null;
        mParent = parent;
        mDataObject = dataObject;
    }
    
    public DataObject getDataObject() {
        return mDataObject;
    }

    /** 
     * Force the node to reload 
     */
    public void discardCachedData() {
        mDisplayName = null;
        mIcon = null;
        mChildren = null;
        mIsLeaf = null;
    }
    
    public MapperTreeNode getParent() {
        return mParent;
    }
    
    /**
     * This method is intended to be used only by the tree model. 
     * Use the method treeModel.getDisplayName(node) instead.
     * @return
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String newName) {
        mDisplayName = newName;
    }

    /**
     * This method is intended to be used only by the tree model. 
     * Use the method treeModel.getChildren(node) instead.
     * @return
     */
    public List<MapperTreeNode> getChildren() {
        return mChildren;
    }
    
    public boolean removeChild(Object child) {
        if (mChildren != null) {
            return mChildren.remove(child);
        }
        return false;
    }
    
    public void setChildren(List<MapperTreeNode> newChildren) {
        mChildren = newChildren;
    }
    
    /**
     * This method is intended to be used only by the tree model. 
     * Use the method treeModel.getIcon(node) instead.
     * @return
     */
    public Icon getIcon() {
        return mIcon;
    }
    
    public void setIcon(Icon newIcon) {
        mIcon = newIcon;
    }
    
    /**
     * This method is intended to be used only by the tree model. 
     * Use the method treeModel.getIcon(node) instead.
     * @return
     */
    public Boolean isLeaf() {
        return mIsLeaf;
    }
    
    public void setIsLeaf(Boolean newIsLeaf) {
        mIsLeaf = newIsLeaf;
    }
    
    @Override
    public String toString() {
        Object dataObject = getDataObject();
        if (dataObject != null) {
            return "TreeNode[" + dataObject.toString() + "]";
        } else {
            return super.toString();
        }
    }
    
    public TreePath getTreePath() {
        TreePath result = null;
        MapperTreeNode parentNode = getParent();
        if (parentNode == null) {
            result = new TreePath(this);
        } else {
            TreePath parentPath = parentNode.getTreePath();
            assert parentPath != null;
            result = parentPath.pathByAddingChild(this);
        }
        return result;
    }
}