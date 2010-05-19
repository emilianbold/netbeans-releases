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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ui.tree.impl;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.ExtTreeModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;

/**
 *
 * @author nk160297
 */
public class SoaTreeModelImpl implements ExtTreeModel<SoaTreeNodeImpl> {

    protected EventListenerList listenerList = new EventListenerList();
    private SoaTreeModel mSourceModel;
    private SoaTreeNodeImpl mRootNode;

    public SoaTreeModelImpl(SoaTreeModel sourceModel) {
        assert sourceModel != null;
        mSourceModel = sourceModel;
    }
    
    public List<SoaTreeNodeImpl> getChildren(SoaTreeNodeImpl parent) {
        List<SoaTreeNodeImpl> childrenList = parent.getChildren();
        if (childrenList == null) {
            //
            // Construct children nodes here
            childrenList = new ArrayList<SoaTreeNodeImpl>();
            List<Object> childrenDataObjectList = 
                    mSourceModel.getTreeStructureProvider().
                    getChildren(parent.getTreeItem());
            //
            if (childrenDataObjectList != null) {
                for (Object childDataObject : childrenDataObjectList) {
                    SoaTreeNodeImpl newNode = 
                            new SoaTreeNodeImpl(parent, childDataObject);
                    childrenList.add(newNode);
                }
            }
        }
        //
        parent.setChildren(childrenList);
        return childrenList;
    }

    public Object getRoot() {
        if (mRootNode == null) {
            Object dataObject = mSourceModel.getRoot();
            mRootNode = new SoaTreeNodeImpl(null, dataObject);
        }
        return mRootNode;
    }

    public Object getChild(Object parent, int index) {
        assert parent instanceof SoaTreeNodeImpl;
        SoaTreeNodeImpl mNode = (SoaTreeNodeImpl)parent;
        List<SoaTreeNodeImpl> childrenNodes = getChildren(mNode);
        //
        return childrenNodes.get(index);
    }

    public int getChildCount(Object parent) {
        assert parent instanceof SoaTreeNodeImpl;
        SoaTreeNodeImpl mNode = (SoaTreeNodeImpl)parent;
        List<SoaTreeNodeImpl> childrenNodes = getChildren(mNode);
        //
        return childrenNodes.size();
    }

    public boolean isLeaf(Object node) {
        assert node instanceof SoaTreeNodeImpl;
        SoaTreeNodeImpl mNode = (SoaTreeNodeImpl)node;
        Object dataObject = mNode.getDataObject();
        //
        Boolean isLeafObj = mNode.isLeaf();
        if (isLeafObj == null) {
            isLeafObj = mSourceModel.getTreeStructureProvider().isLeaf(mNode.getTreeItem());
        }
        //
        if (isLeafObj == null) {
            List<SoaTreeNodeImpl> childrenNodes = getChildren(mNode);
            isLeafObj = childrenNodes.isEmpty();
        }
        //
        mNode.setIsLeaf(isLeafObj);
        return isLeafObj;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getIndexOfChild(Object parent, Object child) {
        assert parent instanceof SoaTreeNodeImpl;
        SoaTreeNodeImpl mNode = (SoaTreeNodeImpl)parent;
        List<SoaTreeNodeImpl> childrenNodes = getChildren(mNode);
        //
        return childrenNodes.indexOf(child);
    }

    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    //------------------------------------------------------------------
    
    public String getDisplayName(Object node) {
        assert node instanceof SoaTreeNodeImpl;
        SoaTreeNodeImpl mNode = (SoaTreeNodeImpl)node;
        String displayName = mNode.getDisplayName();
        if (displayName == null) {
            if (mSourceModel != null) {
                TreeItemInfoProvider infoProvider = 
                        mSourceModel.getTreeItemInfoProvider();
                if (infoProvider != null) {
                    TreeItem treeItem = mNode.getTreeItem();
                    displayName = infoProvider.getDisplayName(treeItem);
                    mNode.setDisplayName(displayName);
                }
            }
        }
        return displayName;
    }
    
    public String getToolTipText(Object node) {
        assert node instanceof SoaTreeNodeImpl;
        SoaTreeNodeImpl mNode = (SoaTreeNodeImpl)node;
        String toolTipText = null;
        if (mSourceModel != null) {
            TreeItemInfoProvider infoProvider =
                    mSourceModel.getTreeItemInfoProvider();
            if (infoProvider != null) {
                TreeItem treeItem = mNode.getTreeItem();
                toolTipText = infoProvider.getToolTipText(treeItem);
            }
        }
        return toolTipText;
    }
    
    public Icon getIcon(Object node) {
        assert node instanceof SoaTreeNodeImpl;
        SoaTreeNodeImpl mNode = (SoaTreeNodeImpl)node;
        Icon icon = mNode.getIcon();
        if (icon == null) {
            if (mSourceModel != null) {
                TreeItemInfoProvider infoProvider = 
                        mSourceModel.getTreeItemInfoProvider();
                if (infoProvider != null) {
                    TreeItem treeItem = mNode.getTreeItem();
                    icon = infoProvider.getIcon(treeItem);
                    mNode.setIcon(icon);
                }
            }
        }
        return icon;
    }
    
    //------------------------------------------------------------------
    
    
}
