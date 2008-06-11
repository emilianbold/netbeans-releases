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
import org.netbeans.modules.bpel.mapper.tree.models.SimpleTreeInfoProvider;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemInfoProvider;
import org.openide.util.NbBundle;

/**
 * The implementation of the MapperTreeModel for target tree which 
 * is used in the predicates' editor
 *
 * @author nk160297
 */
public class PredicateExprTreeModel implements MapperTreeModel<Object> {

    private int mSize = 1;
    public static final String PREDICATE_EXPRESSION = 
            NbBundle.getMessage(PredicateExprTreeModel.class,
            "PREDICATE_EXPRESSION"); // NOI18N
    
    private static MyTreeInfoProvider mTreeInfoProvider = new MyTreeInfoProvider();
    
    public PredicateExprTreeModel(int initialSize) {
        mSize = initialSize;
    }
    
    public Object addPredicateExpr() {
        mSize++;
        return PREDICATE_EXPRESSION;
    }
    
    public Object getRoot() {
        return MapperTreeModel.TREE_ROOT;
    }

    public List getChildren(Iterable<Object> dataObjectPathItr) {
        Object parent = dataObjectPathItr.iterator().next();
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

    public Boolean isLeaf(Object node) {
        if (node == PREDICATE_EXPRESSION) {
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
        return mTreeInfoProvider;
    }
    
    private static class MyTreeInfoProvider extends SimpleTreeInfoProvider {
        
        @Override
        public List<Action> getMenuActions(MapperTcContext mapperTcContext, 
                boolean inLeftTree, TreePath treePath, 
                Iterable<Object> dataObjectPathItrb) {
            Action action = new AddPredicateConditionAction(mapperTcContext, treePath);
            return Collections.singletonList(action);
        }

    }

}
