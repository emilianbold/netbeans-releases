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

import java.util.Collections;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.context.XPathDesignContext;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.specstep.MapperSpecialStep;
import org.netbeans.modules.soa.xpath.mapper.specstep.SpecialStepManager;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep.SsType;
import org.openide.util.NbBundle;

/**
 * Shows the Expression editor dialog in order to create a new predicate.
 *
 * @author nk160297
 */
public class AddSpecialStepAction extends MapperAction<TreeItem> {
    
    private static final long serialVersionUID = 1L;
    
    private SsType mStepType;
    private TreePath mTreePath;
    
    public AddSpecialStepAction(SsType stepType,
            MapperStaticContext mapperTcContext,
            boolean inLeftTree, TreePath treePath,
            TreeItem treeItem) {
        super(mapperTcContext, treeItem, inLeftTree);
        mStepType = stepType;
        mTreePath = treePath;
        postInit();
        // putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    }

    @Override
    public String getDisplayName() {
        switch (mStepType) {
            case COMMENT:
                return NbBundle.getMessage(MapperAction.class, "ADD_STEP_COMMENT"); // NOI18N
            case NODE:
                return NbBundle.getMessage(MapperAction.class, "ADD_STEP_NODE"); // NOI18N
            case PROCESSING_INSTR:
                return NbBundle.getMessage(MapperAction.class, "ADD_STEP_PI"); // NOI18N
            case TEXT:
                return NbBundle.getMessage(MapperAction.class, "ADD_STEP_TEXT"); // NOI18N
            default:
                return NbBundle.getMessage(MapperAction.class, "ADD_XXX") + 
                        " " + mStepType.getDisplayName();
        }
    }
    
    @Override
    protected void actionPerformed(XPathMapperModel mapperModel,
            MapperSwingTreeModel treeModel, SoaTreeModel sourceMModel,
            XPathDesignContext dContext) {
        //
        TreeItem treeItem = getActionSubject();
        //
        // Construct a new Schema Context by the current element
        XPathSchemaContext parentSContext = treeModel.getPathConverter().
                constructContext(treeItem, false);
        if (parentSContext == null) {
            return;
        }
        //
        MapperSpecialStep newSs = new MapperSpecialStep(mStepType, parentSContext);
        //
        // Add the new predicate to the PredicateManager
        SpecialStepManager sStepManager =
                treeModel.getExtManagerHolder().getSpecialStepManager();
        if (sStepManager != null) {
            sStepManager.addStep(treeItem, newSs);
        }
        //
        // Update tree
        treeModel.insertChild(mTreePath, 0, newSs);
        //
        // Set selection to the added predicate item
        Mapper mapper = getSContext().getMapper();
        if (mapper != null) {
            TreeFinderProcessor findProc = new TreeFinderProcessor(treeModel);
            TreePath newPredPath = findProc.findChildByDataObj(mTreePath, newSs);
            if (mInLeftTree) {
                LeftTree leftTree = mapper.getLeftTree();
                leftTree.setSelectionPath(newPredPath);
            } else {
                mapper.expandGraphs(Collections.singletonList(newPredPath));
                mapper.setSelected(newPredPath);
            }
        }
        
    }
    
    
}
