/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.worklist.editor.mapper.tree.model;

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
            }
            //
            Boolean result = ((MapperConnectabilityProvider)extModel).
                    isConnectable(treeItem);
            if (result != null) {
                return result;
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
