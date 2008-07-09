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

package org.netbeans.modules.bpel.mapper.tree.actions;

import java.awt.event.ActionEvent;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.predicates.editor.PredicateExprTreeModel;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.openide.util.NbBundle;

/**
 * Adds a new predicate condition to the right tree of the predicate editor dialog.
 *
 * @author nk160297
 */
public class AddPredicateConditionAction extends MapperAction<TreePath> {
    
    private static final long serialVersionUID = 1L;
    
    public AddPredicateConditionAction(MapperTcContext context, TreePath treePath) {
        super(context, treePath);
        postInit();
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MapperAction.class, "ADD_PREDICATE"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        //
        MapperModel mm = mMapperTcContext.getMapper().getModel();
        assert mm instanceof BpelMapperModel;
        MapperSwingTreeModel treeModel = ((BpelMapperModel)mm).getRightTreeModel();
        SoaTreeModel sourceTreeModel = treeModel.getSourceModel();
        assert sourceTreeModel instanceof PredicateExprTreeModel;
        Object dataObject = ((PredicateExprTreeModel)sourceTreeModel).addPredicateExpr();
        //
        TreePath treePath = getActionSubject();
        TreePath parentPath = treePath.getParentPath();
        int childIndex = treeModel.getIndexOfChild(
                parentPath.getLastPathComponent(), 
                treePath.getLastPathComponent());
        treeModel.insertChild(parentPath, childIndex + 1, dataObject);
    }
    
}
