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

package org.netbeans.modules.soa.xpath.mapper.tree.actions;

import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.context.XPathDesignContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.CastManager;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperTypeCast;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class DeleteCastAction extends MapperAction<TreeItem> {
    
    private static final long serialVersionUID = 1L;
    private TreePath mTreePath;
    
    public DeleteCastAction(MapperStaticContext stContext,
            boolean inLeftTree, TreePath treePath, 
            TreeItem treeItem) {
        super(stContext, treeItem, inLeftTree);
        mTreePath = treePath;
        postInit();
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MapperAction.class, "DELETE_CAST"); // NOI18N
    }
    
    @Override
    protected void actionPerformed(XPathMapperModel mapperModel,
            MapperSwingTreeModel treeModel, SoaTreeModel sourceMModel,
            XPathDesignContext dContext) {
        //
        TreeItem treeItem = getActionSubject();
        Object dataObj = treeItem.getDataObject();
        assert dataObj instanceof MapperTypeCast;
        MapperTypeCast typeCast = (MapperTypeCast)dataObj;
        //
        CastManager castManager = treeModel.getExtManagerHolder().getCastManager();
        if (castManager == null) {
            return;
        }
        //
        castManager.deleteCastCmd(typeCast, treeItem,
                mTreePath, mInLeftTree, mStaticContext);
    }
    
}
