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

package org.netbeans.modules.bpel.mapper.tree.models;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.netbeans.modules.soa.xpath.mapper.tree.models.MapperConnectabilityProvider;

/**
 * An implementation of the MapperTreeModel whithout its own elements 
 * (only the Root). Any extended tree models can be attached. 
 *
 * @author nk160297
 */
public class EmptyTreeModel implements SoaTreeModel, 
        TreeStructureProvider, TreeItemInfoProvider, 
        TreeItemActionsProvider, MapperConnectabilityProvider {

    private ArrayList<SoaTreeExtensionModel> mExtModelList = 
            new ArrayList<SoaTreeExtensionModel>();

    public EmptyTreeModel() {
    }
    
    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return this;
    }
    
    public TreeStructureProvider getTreeStructureProvider() {
        return this;
    }

    public TreeItemActionsProvider getTreeItemActionsProvider() {
        return this;
    }

    public void addExtensionModel(SoaTreeExtensionModel extModel) {
        mExtModelList.add(extModel);
    }

    public List<SoaTreeExtensionModel> getExtensionModelList() {
        return mExtModelList;
    }

    public Object getRoot() {
        return TREE_ROOT;
    }

    public List<Object> getChildren(TreeItem treeItem) {
        List<Object> resultList = new ArrayList<Object>();
        for (SoaTreeExtensionModel extModel : mExtModelList) {
            List<Object> childredDataObjectList = 
                    extModel.getTreeStructureProvider().
                    getChildren(treeItem);
            if (childredDataObjectList != null) {
                resultList.addAll(childredDataObjectList);
            }
        }
        //
        return resultList;
    }

    public Boolean isLeaf(TreeItem treeItem) {
        for (SoaTreeExtensionModel extModel : mExtModelList) {
            Boolean result = extModel.getTreeStructureProvider().isLeaf(treeItem);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

    public Boolean isConnectable(TreeItem treeItem) {
        for (SoaTreeExtensionModel extModel : mExtModelList) {
            if (extModel instanceof MapperConnectabilityProvider) {
                //
                Boolean result = ((MapperConnectabilityProvider)extModel).
                        isConnectable(treeItem);
                if (result != null) {
                    return result;
                }
            }
        }
        //
        return null;
    }

    public String getDisplayName(TreeItem treeItem) {
        for (SoaTreeExtensionModel extModel : mExtModelList) {
            TreeItemInfoProvider provider = extModel.getTreeItemInfoProvider();
            String result = provider.getDisplayName(treeItem);
            if (result != null) {
                return result;
            }
        }
        //
        return treeItem.getDataObject().toString();
    }

    public Icon getIcon(TreeItem treeItem) {
        for (SoaTreeExtensionModel extModel : mExtModelList) {
            TreeItemInfoProvider provider = extModel.getTreeItemInfoProvider();
            Icon result = provider.getIcon(treeItem);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

    public List<Action> getMenuActions(TreeItem treeItem, 
            Object context, TreePath treePath) {
        //
        for (SoaTreeExtensionModel extModel : mExtModelList) {
            TreeItemActionsProvider provider = extModel.getTreeItemActionsProvider();
            List<Action> result = provider.getMenuActions(treeItem, 
                    context, treePath);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

    public String getToolTipText(TreeItem treeItem) {
        for (SoaTreeExtensionModel extModel : mExtModelList) {
            TreeItemInfoProvider provider = extModel.getTreeItemInfoProvider();
            String result = provider.getToolTipText(treeItem);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

}
