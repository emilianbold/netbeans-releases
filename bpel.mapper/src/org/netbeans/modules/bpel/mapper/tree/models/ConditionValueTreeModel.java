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

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemInfoProvider;
import org.netbeans.modules.bpel.model.api.ConditionHolder;
import org.openide.util.NbBundle;

/**
 * The implementation of the MapperTreeModel for target tree for
 * the following elements: 
 * -- If
 * -- ElseIf
 * -- While
 * -- RepeatUntil
 * 
 * The tree has very simple appearance. It has only one element. 
 *
 * @author nk160297
 */
public class ConditionValueTreeModel implements MapperTreeModel<Object> {

    public static final String BOOLEAN_CONDITION = 
            NbBundle.getMessage(ConditionValueTreeModel.class,
            "BOOLEAN_CONDITION"); // NOI18N
    
    private BpelEntity mContextEntity;

    public ConditionValueTreeModel(BpelEntity contextEntity) {
        mContextEntity = contextEntity;
    }
    
    public Object getRoot() {
        return MapperTreeModel.TREE_ROOT;
    }

    public List getChildren(Iterable<Object> dataObjectPathItrb) {
        Object parent = dataObjectPathItrb.iterator().next();
        if (parent == TREE_ROOT) {
            return Collections.singletonList(mContextEntity);
        }
        if (parent instanceof ConditionHolder) {
            return Collections.singletonList(BOOLEAN_CONDITION); 
        }
        //
        return null;
    }

    public Boolean isLeaf(Object node) {
        if (node == BOOLEAN_CONDITION) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean isConnectable(Object node) {
        return isLeaf(node);
    }

    public List getExtensionModelList() {
        return null;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return SimpleTreeInfoProvider.getInstance();
    }

}
