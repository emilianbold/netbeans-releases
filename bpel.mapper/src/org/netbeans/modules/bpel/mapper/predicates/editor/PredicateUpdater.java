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
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.AbstractBpelModelUpdater;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.GraphInfoCollector;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.predicates.PredicateManager;
import org.netbeans.modules.bpel.mapper.predicates.SyntheticPredicate;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.ToRelativePathConverter;
import org.netbeans.modules.xml.xpath.ext.spi.SchemaContextBasedCastResolver;

/**
 * Save predicates to the BPEL model.
 * 
 * @author nk160297
 */
public class PredicateUpdater extends AbstractBpelModelUpdater {

    private BpelMapperModel mPredMapperModel;
    private AbstractPredicate mPred;
    private XPathSchemaContext mSContext;
    private TreePath mTreePath;
    private boolean mInLeftTree;

    public PredicateUpdater(MapperTcContext mapperTcContext,
            BpelMapperModel predMapperModel, // can be null in case of adding a new predicate or deleting onse
            AbstractPredicate pred, // can be null in case of adding a new predicate 
            XPathSchemaContext sContext,
            boolean inLeftTree,
            TreePath treePath) {
        //
        super(mapperTcContext);
        //
        mPredMapperModel = predMapperModel;
        mPred = pred;
        mSContext = sContext;
        mInLeftTree = inLeftTree;
        mTreePath = treePath;
    }

    public void addPredicate(Iterable<Object> itrb) {
        //
        // Create a new predicate and populate it
        mPred = new SyntheticPredicate(mSContext, null);
        recalculatePredicates();
        if (mPred.getPredicates().length == 0) {
            return;
        }
        //
        // Add the new predicate to the PredicateManager
        BpelMapperModel mapperModel = getMapperModel();
        MapperSwingTreeModel treeModel = null;
        if (mInLeftTree) {
            treeModel = mapperModel.getLeftTreeModel();
        } else {
            treeModel = mapperModel.getRightTreeModel();
        }
        //
        MapperTreeModel sourceModel = treeModel.getSourceModel();
        PredicateManager predManager =
                PredicateManager.getPredicateManager(sourceModel);
        boolean predicateAdded = false;
        if (predManager != null) {
            predicateAdded = predManager.addPredicate(itrb, mPred);
        }
        //
        if (!predicateAdded) {
            return;
        }
        //
        // Update tree
        TreePath parentPath = mTreePath.getParentPath();
        int childIndex = treeModel.getIndexOfChild(
                parentPath.getLastPathComponent(),
                mTreePath.getLastPathComponent());
        treeModel.insertChild(parentPath, childIndex + 1, mPred);
        //
        // Set selection to the added predicate item
        TreeFinderProcessor findProc = new TreeFinderProcessor(treeModel);
        TreePath newPredPath = findProc.findChildByDataObj(parentPath, mPred);
        if (mInLeftTree) {
            LeftTree leftTree = mMapperTcContext.getMapper().getLeftTree();
            leftTree.setSelectionPath(newPredPath);
        } else {
            Mapper mapper = mMapperTcContext.getMapper();
            mapper.setSelected(newPredPath);
        }
    }

    public void updatePredicate() {
        recalculatePredicates();
        //
        BpelMapperModel mModel = getMapperModel();
        if (mInLeftTree) {
            //
            // Update BPEL model
            List<TreePath> dependentGraphs = mModel.getDependentGraphs(mPred);
            mModel.fireGraphsChanged(dependentGraphs);
            //
            // Update left tree
            BpelMapperModel mapperModel = getMapperModel();
            mapperModel.getLeftTreeModel().fireTreeChanged(this, mTreePath);
        } else {
            //
            // Update BPEL model
            mModel.fireGraphChanged(mTreePath);
            //
            // Update right tree
            BpelMapperModel mapperModel = getMapperModel();
            mapperModel.getRightTreeModel().fireTreeChanged(this, mTreePath);
        }
    }

    public void deletePredicate() {
        BpelMapperModel mModel = getMapperModel();
        MapperSwingTreeModel treeModel = null;
        if (mInLeftTree) {
            treeModel = mModel.getLeftTreeModel();
        } else {
            treeModel = mModel.getRightTreeModel();
        }
        MapperTreeModel sourceModel = treeModel.getSourceModel();
        //
        // Calculate predicate location index
        // int predIndex = treeModel.getChildIndex(mTreePath.getParentPath(), mPred);
        //
        VariableTreeModel varTreeModel =
                MapperTreeModel.Utils.findExtensionModel(sourceModel,
                VariableTreeModel.class);
        if (varTreeModel != null) {
            PredicateManager predManager = varTreeModel.getPredicateManager();
            if (predManager != null) {
                predManager.removePredicate(mPred);
            }
        }
        //
        // Update BPEL model
        if (mInLeftTree) {
            List<TreePath> dependentGraphs = mModel.getDependentGraphs(mPred);
            for (TreePath graphPath : dependentGraphs) {
                mModel.removeIngoingLinks(graphPath, mTreePath);
            }
            //
            // Modify BPEL model for all changed graphs in one transaction.
            mModel.fireGraphsChanged(dependentGraphs);
        } else {
            mModel.removeNestedGraphs(mTreePath);
        }
        //
        // Remove node from the tree
        treeModel.remove(mTreePath);
//        //
//        // Set selection to the added predicate item
//        TreePath newSelection = treeModel.findChildByIndex(
//                mTreePath.getParentPath(), predIndex - 1);
//        LeftTree leftTree = mMapperTcContext.getMapper().getLeftTree();
//        leftTree.setSelectionPath(newSelection);
    }

    public void recalculatePredicates() {
        XPathModel xPathModel = getXPathModel();
        if (xPathModel == null) {
            return;
        }
        //
        Map<TreePath, Graph> graphsMap = mPredMapperModel.getGraphsInside(null);
        MapperSwingTreeModel rightTreeModel =
                mPredMapperModel.getRightTreeModel();
        Set<TreePath> unsorted = graphsMap.keySet();
        List<TreePath> sorted = rightTreeModel.sortByLocation(unsorted);
        //
        ArrayList<XPathPredicateExpression> predicateList =
                new ArrayList<XPathPredicateExpression>();
        for (TreePath treePath : sorted) {
            Graph graph = graphsMap.get(treePath);
            XPathPredicateExpression pExpr = constructPredicate(xPathModel,
                    graph);
            if (pExpr != null) {
                predicateList.add(pExpr);
            }
        }
        //
        xPathModel.discardResolvedStatus();
        //
        XPathPredicateExpression[] predArr = predicateList.toArray(
                new XPathPredicateExpression[predicateList.size()]);
        mPred.setPredicates(predArr);
    }

    private XPathPredicateExpression constructPredicate(
            XPathModel xPathModel, Graph graph) {
        //
        GraphInfoCollector graphInfo = new GraphInfoCollector(graph);
        //
        XPathExprList xPathExprList = buildXPathExprList(
                xPathModel, graphInfo, null, null);
        //
        XPathExpression expr = xPathExprList.getConnectedExpression();
        if (expr != null) {
            //
            // If the following method isn't called the expression 
            // is not resolved and doesn't have a schema context!
            xPathModel.resolveExpressionExtReferences(expr);
            //
            ToRelativePathConverter converter =
                    new ToRelativePathConverter(expr, mSContext);
            expr = converter.convert();
            //
            return xPathModel.getFactory().newXPathPredicateExpression(expr);
        }
        //
        return null;
    }

    private XPathModel getXPathModel() {
        //
        // Try use existing model first
        if (mPred != null) {
            XPathPredicateExpression[] predArr = mPred.getPredicates();
            if (predArr != null && predArr.length != 0) {
                XPathPredicateExpression firstPred = predArr[0];
                if (firstPred != null) {
                    XPathModel model = firstPred.getModel();
                    if (model != null) {
                        return model;
                    }
                }
            }
        }
        //
        // Create a new model
        BpelEntity bpelEntity = getDesignContext().getSelectedEntity();
        //
        if (bpelEntity == null) {
            return null;
        }
        //
        XPathModel xPathModel = BpelXPathModelFactory.create(bpelEntity);
        xPathModel.setSchemaContext(mSContext);
        //
        SchemaContextBasedCastResolver castResolver =
                new SchemaContextBasedCastResolver(mSContext);
        xPathModel.setXPathCastResolver(castResolver);
        //
        return xPathModel;
    }
}
