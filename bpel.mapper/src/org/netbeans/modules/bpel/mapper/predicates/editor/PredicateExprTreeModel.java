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

package org.netbeans.modules.bpel.mapper.predicates.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.tree.actions.AddPredicateConditionAction;
import org.netbeans.modules.bpel.mapper.tree.models.MapperConnectabilityProvider;
import org.netbeans.modules.bpel.mapper.tree.models.SimpleTreeInfoProvider;
import org.netbeans.modules.bpel.mapper.model.MapperTreeContext;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.openide.util.NbBundle;

/**
 * The implementation of the MapperTreeModel for target tree which 
 * is used in the predicates' editor
 *
 * @author nk160297
 */
public class PredicateExprTreeModel implements SoaTreeModel, 
        TreeStructureProvider, TreeItemActionsProvider, 
        MapperConnectabilityProvider {

    private int mSize = 1;
    public static final String PREDICATE_EXPRESSION = 
            NbBundle.getMessage(PredicateExprTreeModel.class,
            "PREDICATE_EXPRESSION"); // NOI18N
    
    public PredicateExprTreeModel(int initialSize) {
        mSize = initialSize;
    }
    
    public TreeStructureProvider getTreeStructureProvider() {
        return this;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return SimpleTreeInfoProvider.getInstance();
    }
    
    public TreeItemActionsProvider getTreeItemActionsProvider() {
        return this;
    }

    public Object getRoot() {
        return TREE_ROOT;
    }

    public Object addPredicateExpr() {
        mSize++;
        return PREDICATE_EXPRESSION;
    }
    
    public List getChildren(TreeItem treeItem) {
        Object parent = treeItem.getDataObject();
        if (parent == TREE_ROOT) {
            ArrayList children = new ArrayList(mSize);
            // Fill the array list with the same element mSize times
            for (int index = 0; index < mSize; index++) {
                children.add(PREDICATE_EXPRESSION);
            }
            return children; 
        }
        //
        return null;
    }

    public Boolean isLeaf(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj == PREDICATE_EXPRESSION) {
            return true;
        } else {
            return false;
        }
    }

    public List<Action> getMenuActions(TreeItem treeItem, Object context, 
            TreePath treePath) {
        //
        if (context instanceof MapperTreeContext) {
            MapperTreeContext mapperContext = (MapperTreeContext)context;
            Action action = new AddPredicateConditionAction(
                    mapperContext.getMapperTcContext(), treePath);
            return Collections.singletonList(action);
        }
        return null;
    }

    public Boolean isConnectable(TreeItem treeItem) {
        return isLeaf(treeItem);
    }

    public List getExtensionModelList() {
        return null;
    }

}
