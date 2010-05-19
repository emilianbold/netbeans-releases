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
package org.netbeans.modules.bpel.mapper.logging.tree.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.mapper.logging.tree.LoggingTreeItem;
import org.netbeans.modules.bpel.mapper.logging.tree.TraceItem;
import org.netbeans.modules.soa.xpath.mapper.tree.models.MapperConnectabilityProvider;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class LoggingAlertingTreeModel implements SoaTreeModel, 
        TreeStructureProvider, MapperConnectabilityProvider {

    private ExtensibleElements myContext;
    public LoggingAlertingTreeModel(ExtensibleElements context) {
        myContext = context;
    }
    
    public TreeStructureProvider getTreeStructureProvider() {
        return this;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return LoggingAlertingTreeInfoProvider.getInstance();
    }

    public TreeItemActionsProvider getTreeItemActionsProvider() {
        return LoggingAlertingTreeInfoProvider.getInstance();
    }

    public Object getRoot() {
        return TREE_ROOT;
    }

    public List<SoaTreeExtensionModel> getExtensionModelList() {
        return null;
    }

    public List getChildren(TreeItem treeItem) {
        Object parent = treeItem.getDataObject();
        if (parent == TREE_ROOT) {
            return Collections.singletonList( myContext);
        }
        if (parent instanceof ExtensibleElements) {
            return Arrays.asList(TraceItem.TRACE_ITEMS);
        }
        if (parent instanceof TraceItem) {
            return ((TraceItem)parent).getChildren();
        }
        //
        return null;
    }

    public Boolean isLeaf(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        return dataObj instanceof LoggingTreeItem 
                && ((LoggingTreeItem)dataObj).isLeaf();
    }

    public Boolean isConnectable(TreeItem treeItem) {
        return isLeaf(treeItem);
    }

}
