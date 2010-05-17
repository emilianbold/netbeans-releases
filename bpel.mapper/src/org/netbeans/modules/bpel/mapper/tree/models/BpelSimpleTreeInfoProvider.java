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

import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.xpath.mapper.tree.models.SimpleTreeInfoProvider;

/**
 * The implementation of the TreeItemInfoProvider for target tree 
 * with simple content (Wait, For, If, ...).
 * 
 * @author nk160297
 */
public class BpelSimpleTreeInfoProvider
        implements TreeItemInfoProvider, TreeItemActionsProvider {

    private static BpelSimpleTreeInfoProvider singleton = new BpelSimpleTreeInfoProvider();
    
    public static BpelSimpleTreeInfoProvider getInstance() {
        return singleton;
    }
    
    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof BpelEntity) {
            Class<? extends BpelEntity> bpelInterface = 
                    ((BpelEntity)dataObj).getElementType();
            NodeType nodeType = EditorUtil.getBasicNodeType(bpelInterface);
            if (nodeType != null && nodeType != NodeType.UNKNOWN_TYPE) {
                return nodeType.getDisplayName();
            }
        }
        return dataObj.toString();
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof BpelEntity) {
            Class<? extends BpelEntity> bpelInterface = 
                    ((BpelEntity)dataObj).getElementType();
            NodeType nodeType = EditorUtil.getBasicNodeType(bpelInterface);
            if (nodeType != null && nodeType != NodeType.UNKNOWN_TYPE) {
                Icon icon = nodeType.getIcon();
                if (icon != null) {
                    return icon;
                }
            }
        }
        //
        if (dataObj instanceof String) {
            return SimpleTreeInfoProvider.RESULT_IMAGE;
        }
        //
        return TreeItemInfoProvider.UNKNOWN_IMAGE;
    }

    public List<Action> getMenuActions(TreeItem treeItem, 
            Object context, TreePath treePath) {
        return null;
    }

    public String getToolTipText(TreeItem treeItem) {
        return null;
    }
}
