/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.tree;

import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.worklist.editor.mapper.model.PathConverter;

/**
 * The unified tree node for the MapperSwingTreeModel. This node 
 * is used for any kind of tree item. It uses caching of a data object, 
 * icon and display name. 
 * 
 * @author nk160297
 */
public final class MapperTreeNode<DataObject> implements TreeItem {
    
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
//        mChildren = null;
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
    
//    @Override
//    public String toString() {
//        Object dataObject = getDataObject();
//        if (dataObject != null) {
//            return "TreeNode[" + dataObject.toString() + "]";
//        } else {
//            return super.toString();
//        }
//    }
    
    @Override
    public String toString() {
        return PathConverter.toString(MapperTreeNode.this);
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

    public Iterator<Object> iterator() {
        return new Iterator() {

            private MapperTreeNode mNextNode = MapperTreeNode.this;

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
    
    
}