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
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemInfoProvider;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.mapper.tree.images.NodeIcons;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;

/**
 * The implementation of the TreeItemInfoProvider for target tree 
 * with simple content (Wait, For, If, ...).
 * 
 * @author nk160297
 */
public class SimpleTreeInfoProvider implements TreeItemInfoProvider {

    private static SimpleTreeInfoProvider singleton = new SimpleTreeInfoProvider();
    
    public static SimpleTreeInfoProvider getInstance() {
        return singleton;
    }
    
    public String getDisplayName(Object treeItem) {
        if (treeItem instanceof BpelEntity) {
            Class<? extends BpelEntity> bpelInterface = 
                    ((BpelEntity)treeItem).getElementType();
            NodeType nodeType = EditorUtil.getBasicNodeType(bpelInterface);
            if (nodeType != null && nodeType != NodeType.UNKNOWN_TYPE) {
                return nodeType.getDisplayName();
            }
        }
        return treeItem.toString();
    }

    public Icon getIcon(Object treeItem) {
        if (treeItem instanceof BpelEntity) {
            Class<? extends BpelEntity> bpelInterface = 
                    ((BpelEntity)treeItem).getElementType();
            NodeType nodeType = EditorUtil.getBasicNodeType(bpelInterface);
            if (nodeType != null && nodeType != NodeType.UNKNOWN_TYPE) {
                Icon icon = nodeType.getIcon();
                if (icon != null) {
                    return icon;
                }
            }
        }
        //
        if (treeItem instanceof String) {
            return NodeIcons.RESULT.getIcon();
        }
        //
        return NodeIcons.UNKNOWN_IMAGE;
    }

    public List<Action> getMenuActions(MapperTcContext mapperTcContext, 
            boolean inLeftTree, TreePath treePath, 
            RestartableIterator<Object> dataObjectPathItr) {
        return null;
    }

    public String getToolTipText(RestartableIterator<Object> dataObjectPathItr) {
        return null;
    }
}
