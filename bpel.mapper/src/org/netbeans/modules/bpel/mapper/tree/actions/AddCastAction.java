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
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.cast.CastManager;
import org.netbeans.modules.bpel.mapper.cast.SubtypeChooser;
import org.netbeans.modules.bpel.mapper.cast.SyntheticTypeCast;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.openide.util.NbBundle;

/**
 * Shows the Subtype chooser dialog to create a new Custed item. 
 *
 * @author nk160297
 */
public class AddCastAction extends MapperAction<TreeItem> {
    
    private static final long serialVersionUID = 1L;
    
    private boolean mInLeftTree;
    private TreePath mTreePath;
    
    private GlobalType mGType;
    
    public AddCastAction(GlobalType gType, MapperTcContext mapperTcContext,
            boolean inLeftTree, TreePath treePath, 
            TreeItem treeItem) {
        super(mapperTcContext, treeItem);
        mTreePath = treePath;
        mInLeftTree = inLeftTree;
        mGType = gType;
        postInit();
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MapperAction.class, "ADD_CAST"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        //
        // Add the new type cast to the CastManager
        MapperModel mm = mMapperTcContext.getMapper().getModel();
        assert mm instanceof BpelMapperModel;
        BpelMapperModel mapperModel = (BpelMapperModel)mm;

        MapperSwingTreeModel treeModel = null;
        if (mInLeftTree) {
            treeModel = mapperModel.getLeftTreeModel();
        } else {
            treeModel = mapperModel.getRightTreeModel();
        }
        //
        SoaTreeModel sourceModel = treeModel.getSourceModel();
        CastManager castManager = CastManager.getCastManager(sourceModel);
        if (castManager == null) {
            return ;
        }
        //
        BpelDesignContext bdContext = 
                mMapperTcContext.getDesignContextController().getContext();
        assert bdContext != null;
        SubtypeChooser chooser = new SubtypeChooser(mGType, bdContext.getBpelModel());
        if (!SubtypeChooser.showDlg(chooser)) {
            return; // The cancel is pressed
        }
        //
        GlobalType subtype = chooser.getSelectedValue();
        //
        // The  iterator points to the casted component
        TreeItem treeItem = getActionSubject();
        SyntheticTypeCast newTypeCast = new SyntheticTypeCast(treeItem, subtype);
        boolean castAdded = castManager.addTypeCast(treeItem, newTypeCast);
        //
        TreePath parentPath = mTreePath.getParentPath();
        if (castAdded) {
            //
            // Update tree
            int childIndex = treeModel.getIndexOfChild(
                    parentPath.getLastPathComponent(), 
                    mTreePath.getLastPathComponent());
            treeModel.insertChild(parentPath, childIndex + 1, newTypeCast);
        }
        //
        // Set selection to the added predicate item
        TreeFinderProcessor findProc = new TreeFinderProcessor(treeModel);
        TreePath newPredPath = findProc.findChildByDataObj(parentPath, newTypeCast);
        if (mInLeftTree) {
            LeftTree leftTree = mMapperTcContext.getMapper().getLeftTree();
            leftTree.setSelectionPath(newPredPath);
        } else {
            Mapper mapper = mMapperTcContext.getMapper();
            mapper.setSelected(newPredPath);
        }
    }
    
}
