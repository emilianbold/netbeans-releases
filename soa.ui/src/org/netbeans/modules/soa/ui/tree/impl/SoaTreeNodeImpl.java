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

package org.netbeans.modules.soa.ui.tree.impl;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Icon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.DataObjectHolder;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;

/**
 * The unified tree node for the SoaTreeModelImpl. This node 
 * is used for any kind of tree item. It uses caching of a data object, 
 * icon and display name. 
 * 
 * @author nk160297
 */
public final class SoaTreeNodeImpl implements DataObjectHolder<Object> {
    
    private Object mDataObject;
    private String mDisplayName; 
    private Icon mIcon;
    private SoaTreeNodeImpl mParent;
    private List<SoaTreeNodeImpl> mChildren;
    private Boolean mIsLeaf;
    
    private AtomicReference<TreeItem> mTreeItem = new AtomicReference<TreeItem>();
    
    public SoaTreeNodeImpl(SoaTreeNodeImpl parent, Object dataObject) {
        assert dataObject != null;
        mParent = parent;
        mDataObject = dataObject;
    }
    
    public Object getDataObject() {
        return mDataObject;
    }

    /** 
     * Force the node to reload 
     */
    public void discardCachedData() {
        mDisplayName = null;
        mIcon = null;
//        mChildren = null;
        mIsLeaf = null;
    }
    
    public SoaTreeNodeImpl getParent() {
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
    public List<SoaTreeNodeImpl> getChildren() {
        return mChildren;
    }
    
    public boolean removeChild(Object child) {
        if (mChildren != null) {
            return mChildren.remove(child);
        }
        return false;
    }
    
    public void setChildren(List<SoaTreeNodeImpl> newChildren) {
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
        SoaTreeNodeImpl parentNode = getParent();
        if (parentNode == null) {
            result = new TreePath(this);
        } else {
            TreePath parentPath = parentNode.getTreePath();
            assert parentPath != null;
            result = parentPath.pathByAddingChild(this);
        }
        return result;
    }
    
    public TreeItem getTreeItem() {
        if (mTreeItem.get() == null) {
            TreeItem newTreeItem = new MyTreeItem(this);
            mTreeItem.compareAndSet(null, newTreeItem);
        }
        return mTreeItem.get();
    }
    
    protected static class MyTreeItem implements TreeItem {

        private SoaTreeNodeImpl mTreeNode;
        
        public MyTreeItem(SoaTreeNodeImpl treeNode) {
            mTreeNode = treeNode;
        }
        
        public Iterator<Object> iterator() {
            return new Iterator() {

                private SoaTreeNodeImpl mNextNode = mTreeNode;

                public boolean hasNext() {
                    return mNextNode != null;
                }

                public Object next() {
                    assert mNextNode != null;
                    Object result = mNextNode.getDataObject();
                    mNextNode = mNextNode.getParent();
                    return result;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
        }

        public Object getDataObject() {
            return mTreeNode.getDataObject();
        }
    
        @Override
        public String toString() {
            return SoaTreeModel.MyUtils.toString(this);
        }
    }
    
}