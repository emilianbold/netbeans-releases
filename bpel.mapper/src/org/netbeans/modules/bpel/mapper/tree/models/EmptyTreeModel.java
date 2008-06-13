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
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeExtensionModel;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemInfoProvider;

/**
 * An implementation of the MapperTreeModel whithout its own elements 
 * (only the Root). Any extended tree models can be attached. 
 *
 * @author nk160297
 */
public class EmptyTreeModel implements MapperTreeModel<Object>, TreeItemInfoProvider {

    private ArrayList<MapperTreeExtensionModel> mExtModelList = 
            new ArrayList<MapperTreeExtensionModel>();

    public EmptyTreeModel() {
    }
    
    public Object getRoot() {
        return TREE_ROOT;
    }

    public List getChildren(Iterable<Object> dataObjectPathItrb) {
        List<Object> resultList = new ArrayList<Object>();
        for (MapperTreeExtensionModel extModel : mExtModelList) {
            List<Object> childredDataObjectList = 
                    extModel.getChildren(dataObjectPathItrb);
            if (childredDataObjectList != null) {
                resultList.addAll(childredDataObjectList);
            }
        }
        //
        return resultList;
    }

    public Boolean isLeaf(Object node) {
        for (MapperTreeExtensionModel extModel : mExtModelList) {
            Boolean result = extModel.isLeaf(node);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

    public Boolean isConnectable(Object node) {
        for (MapperTreeExtensionModel extModel : mExtModelList) {
            Boolean result = extModel.isConnectable(node);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

    public List<MapperTreeExtensionModel> getExtensionModelList() {
        return mExtModelList;
    }
    
    public void addExtensionModel(MapperTreeExtensionModel extModel) {
        mExtModelList.add(extModel);
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return this;
    }
    
    public String getDisplayName(Object treeItem) {
        for (MapperTreeExtensionModel extModel : mExtModelList) {
            TreeItemInfoProvider provider = extModel.getTreeItemInfoProvider();
            String result = provider.getDisplayName(treeItem);
            if (result != null) {
                return result;
            }
        }
        //
        return treeItem.toString();
    }

    public Icon getIcon(Object treeItem) {
        for (MapperTreeExtensionModel extModel : mExtModelList) {
            TreeItemInfoProvider provider = extModel.getTreeItemInfoProvider();
            Icon result = provider.getIcon(treeItem);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

    public List<Action> getMenuActions(MapperTcContext mapperTcContext, 
            boolean inLeftTree, TreePath treePath, 
            Iterable<Object> dataObjectPathItrb) {
        //
        for (MapperTreeExtensionModel extModel : mExtModelList) {
            TreeItemInfoProvider provider = extModel.getTreeItemInfoProvider();
            List<Action> result = provider.getMenuActions(
                    mapperTcContext, inLeftTree, treePath, dataObjectPathItrb);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

    public String getToolTipText(Iterable<Object> dataObjectPathItr) {
        for (MapperTreeExtensionModel extModel : mExtModelList) {
            TreeItemInfoProvider provider = extModel.getTreeItemInfoProvider();
            String result = provider.getToolTipText(dataObjectPathItr);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

}
