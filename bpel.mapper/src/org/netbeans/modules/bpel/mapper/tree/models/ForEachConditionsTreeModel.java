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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.openide.util.NbBundle;

/**
 * The implementation of the MapperTreeModel for target tree for
 * the FroEach element.
 *
 * @author nk160297
 */
public class ForEachConditionsTreeModel implements SoaTreeModel, 
        TreeStructureProvider, MapperConnectabilityProvider {

    public static final String START_VALUE = 
            NbBundle.getMessage(ForEachConditionsTreeModel.class, 
            "START_VALUE"); // NOI18N
    public static final String FINAL_VALUE = 
            NbBundle.getMessage(ForEachConditionsTreeModel.class, 
            "FINAL_VALUE"); // NOI18N
    public static final String COMPLETION_CONDITION = 
            NbBundle.getMessage(ForEachConditionsTreeModel.class, 
            "COMPLETION_CONDITION"); // NOI18N

    
    private ForEach mContextEntity;

    public ForEachConditionsTreeModel(ForEach contextEntity) {
        mContextEntity = contextEntity;
    }
    
    public List getExtensionModelList() {
        return null;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return SimpleTreeInfoProvider.getInstance();
    }

    public TreeStructureProvider getTreeStructureProvider() {
        return this;
    }

    public TreeItemActionsProvider getTreeItemActionsProvider() {
        return SimpleTreeInfoProvider.getInstance();
    }

    public Object getRoot() {
        return TREE_ROOT;
    }

    public List getChildren(TreeItem treeItem) {
        Object parent = treeItem.getDataObject();
        if (parent == TREE_ROOT) {
            return Collections.singletonList(mContextEntity);
        }
        if (parent instanceof ForEach) {
            return Arrays.asList(START_VALUE, FINAL_VALUE, COMPLETION_CONDITION);
        }
        //
        return null;
    }

    public Boolean isLeaf(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj == START_VALUE || 
                dataObj == FINAL_VALUE ||
                dataObj == COMPLETION_CONDITION) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean isConnectable(TreeItem treeItem) {
        return isLeaf(treeItem);
    }

}
