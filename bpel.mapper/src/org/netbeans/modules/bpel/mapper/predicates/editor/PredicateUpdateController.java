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
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelExtManagerHolder;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.predicates.BpelMapperPredicate;
import org.netbeans.modules.bpel.mapper.predicates.BpelPredicateManager;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.predicates.BpelPredicatesModificator;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.xpath.mapper.lsm.ExtRegistrationException;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 * Controls different operation with predicates in BPEL model
 * - adding
 * - modifying
 * - deletion
 * 
 * @author nk160297
 */
public class PredicateUpdateController  {

    private MapperTcContext mTcContext;
    private TreePath mSubjectTPath;
    private boolean mInLeftTree;

    public PredicateUpdateController(TreePath subjectTPath, boolean inLeftTree,
            MapperTcContext mapperTcContext) {
        //
        mTcContext = mapperTcContext;
        //
        mInLeftTree = inLeftTree;
        mSubjectTPath = subjectTPath;
    }

    public TreePath addPredicate(BpelMapperPredicate newPred)
            throws ExtRegistrationException {
        //
        // Add the new predicate to the BpelPredicateManager
        BpelMapperModel mapperModel = mTcContext.getMapperModel();
        MapperSwingTreeModel treeModel = null;
        if (mInLeftTree) {
            treeModel = mapperModel.getLeftTreeModel();
        } else {
            treeModel = mapperModel.getRightTreeModel();
        }
        //
        BpelExtManagerHolder bemh = (BpelExtManagerHolder)treeModel.getExtManagerHolder();
        BpelPredicateManager predManager = bemh.getPredicateManager();
        if (predManager == null) {
            return null;
        }
        //
        LocationStepModifier newBpelPredicate =
                predManager.registerNewPredicate(mTcContext, newPred);
        if (newBpelPredicate == null) {
            // The predicate hasn't been added
            return null;
        }
        //
        // Update tree
        TreePath parentPath = mSubjectTPath.getParentPath();
        int childIndex = treeModel.getIndexOfChild(
                parentPath.getLastPathComponent(),
                mSubjectTPath.getLastPathComponent());
        treeModel.insertChild(parentPath, childIndex + 1, newPred);
        //
        TreeFinderProcessor findProc = new TreeFinderProcessor(treeModel);
        TreePath newPredPath = findProc.findChildByDataObj(parentPath, newPred);
        //
        // Set selection to the added predicate item
        Mapper mapper = mTcContext.getMapper();
        if (mapper != null) {
            if (mInLeftTree) {
                LeftTree leftTree = mapper.getLeftTree();
                leftTree.setSelectionPath(newPredPath);
            } else {
                mapper.expandGraphs(Collections.singletonList(newPredPath));
                mapper.setSelected(newPredPath);
            }
        }
        //
        return newPredPath;
    }

    /**
     * Modifies the old predicate by specifying the new predicates' array.
     *
     * @param oldPred
     * @param newPredArr
     * @return success flag
     * @throws java.lang.Exception
     */
    public boolean modifyPredicate(final BpelMapperPredicate oldPred,
            final XPathPredicateExpression[] newPredArr) throws Exception {
        //
        // Modify the predicate in memory and in the BPEL file
        BpelModel bpelModel = mTcContext.getDesignContextController().
                getContext().getSelectedEntity().getBpelModel();
        return bpelModel.invoke(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return modifyPredicateImpl(oldPred, newPredArr);
            }
        }, mTcContext);
    }

    /**
     *
     * @param oldPred
     * @return success flag
     */
    public boolean deletePredicate(BpelMapperPredicate oldPred) {
        BpelMapperModel mModel = mTcContext.getMapperModel();
        MapperSwingTreeModel treeModel = null;
        if (mInLeftTree) {
            treeModel = mModel.getLeftTreeModel();
        } else {
            treeModel = mModel.getRightTreeModel();
        }
        SoaTreeModel sourceModel = treeModel.getSourceModel();
        //
        // Calculate predicate location index
        // int predIndex = treeModel.getChildIndex(mTreePath.getParentPath(), mPred);
        //
        VariableTreeModel varTreeModel =
                SoaTreeModel.MyUtils.findExtensionModel(sourceModel,
                VariableTreeModel.class);
        if (varTreeModel != null) {
            BpelPredicateManager predManager = varTreeModel.getPredicateManager();
            if (predManager != null) {
                if (predManager.deletePredicate(oldPred, mModel, mSubjectTPath)) {
                    //
                    // Remove node from the tree
                    treeModel.remove(mSubjectTPath);
                    return true;
                }
            }
        }
        //
        return false;
    }

    /*
     * It is implied to be called in BPEL transaction.
     */
    private boolean modifyPredicateImpl(BpelMapperPredicate oldPred,
            XPathPredicateExpression[] newPredArr) throws Exception {
        //
        // Preparation
        //
        // Construct old predicate
        BpelMapperPredicate oldPredicate = oldPred.clone();
        if (oldPredicate == null) {
            return false;
        }
        //
        // Resolve Predicate Manager
        final BpelMapperModel mapperModel = mTcContext.getMapperModel();
        MapperSwingTreeModel treeModel = null;
        if (mInLeftTree) {
            treeModel = mapperModel.getLeftTreeModel();
        } else {
            treeModel = mapperModel.getRightTreeModel();
        }
        //
        BpelExtManagerHolder bemh = (BpelExtManagerHolder)treeModel.getExtManagerHolder();
        final BpelPredicateManager predManager = bemh.getPredicateManager();
        assert predManager != null;
        //
        final BpelEntity selectedBpelEntity = mTcContext.
                getDesignContextController().getContext().getSelectedEntity();
        assert selectedBpelEntity != null;
        //
        final VariableDeclaration varDecl = oldPred.getBaseBpelVariable();
        assert (varDecl != null && varDecl instanceof ExtensibleElements);
        //
        XPathSchemaContext sContext = oldPred.getSchemaContext();
        assert sContext != null && sContext instanceof PredicatedSchemaContext;
        if (sContext == null || !(sContext instanceof PredicatedSchemaContext)) {
            return false;
        }
        final PredicatedSchemaContext predSContext = (PredicatedSchemaContext)sContext;
        //
        // Modify BPEL first. It helps in case of a problem with BPEL modification.
        BpelPredicatesModificator bpelModificator =
                new BpelPredicatesModificator(mTcContext);
        //
        // Modify Variables' area
        bpelModificator.modify((ExtensibleElements)varDecl,
                predSContext, mInLeftTree, newPredArr);
        //
        if (mInLeftTree) {
            //
            // Modify all dependant From expressions.
            List<TreePath> dependentGraphs =
                    mapperModel.getLeftChangeAffectedGraphs(oldPred);
            //
            // Construct new predicate
            oldPred.setPredicates(newPredArr);
            //
            // Modify XxxxManagers
            predManager.modifyPredicateEverywhere(varDecl, oldPredicate, oldPred, newPredArr);
            //
            // Regenerate graphs
            mapperModel.fireGraphsChanged(dependentGraphs);
            //
            // Update left tree
            mapperModel.getLeftTreeModel().fireTreeChanged(this, mSubjectTPath);
        } else {
            //
            oldPred.setPredicates(newPredArr);
            //
            // Regenerate all affected graphs.
            // Actually all graphs below the changed one are affected
            Map<TreePath, Graph> affectedRightGraphsMap =
                    mapperModel.getGraphsInside(mSubjectTPath);
            List<TreePath> treePathList =
                    new ArrayList<TreePath>(affectedRightGraphsMap.keySet());
            mapperModel.fireGraphsChanged(treePathList);
            //
            // Modify XxxxManagers
            predManager.modifyPredicateEverywhere(varDecl, oldPredicate, oldPred, newPredArr);
            //
            // Update right tree
            mapperModel.getRightTreeModel().fireTreeChanged(this, mSubjectTPath);
        }
        //
        return true;
    }

}
