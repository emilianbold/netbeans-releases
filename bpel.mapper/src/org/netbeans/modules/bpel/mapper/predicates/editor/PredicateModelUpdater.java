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
import org.netbeans.modules.bpel.mapper.model.DefaultBpelModelUpdater;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.BpelGraphInfoCollector;
import org.netbeans.modules.bpel.mapper.predicates.BpelMapperPredicate;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperLsmTree;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.ToRelativePathConverter;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.SchemaContextBasedCastResolver;

/**
 * The Updater for the Predicate Mapper Model.
 * Updater is responsible for calculation of the new predicate according to 
 * the graph from the Predicate Mapper.
 * 
 * @author nk160297
 */
public class PredicateModelUpdater extends DefaultBpelModelUpdater {

    private BpelMapperModel mPredMapperModel;
    private XPathSchemaContext mBaseSContext;

    public PredicateModelUpdater(MapperTcContext mapperTcContext,
            BpelMapperModel predMapperModel, // can be null in case of adding a new predicate or deleting onse
            XPathSchemaContext baseSContext) {
        //
        super(mapperTcContext);
        //
        mPredMapperModel = predMapperModel;
        mBaseSContext = baseSContext;
    }

    public XPathPredicateExpression[] recalculatePredicates() {
        XPathModel xPathModel = getXPathModel();
        if (xPathModel == null) {
            return null;
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
        return predArr;
    }

    /**
     * Creates a new predicate and populate it.
     * @param baseTreeItem
     * @return
     * @throws org.netbeans.modules.bpel.mapper.cast.ExtRegistrationException
     */
    public BpelMapperPredicate createNewPredicate() {
        //
        XPathPredicateExpression[] newPredArr = recalculatePredicates();
        if (newPredArr == null || newPredArr.length == 0) {
            return null;
        }
        PredicatedSchemaContext pSContext =
                new PredicatedSchemaContext(mBaseSContext, newPredArr);
        BpelMapperPredicate newPred = new BpelMapperPredicate(pSContext);
        return newPred;
    }

    private XPathPredicateExpression constructPredicate(
            XPathModel xPathModel, Graph graph) {
        //
        BpelGraphInfoCollector graphInfo = new BpelGraphInfoCollector(graph);
        //
        MapperLsmTree lsmTree = new MapperLsmTree(getTcContext(), true); // left tree
        XPathExprList xPathExprList =
                buildXPathExprList(xPathModel, graphInfo, lsmTree);
        //
        XPathExpression expr = xPathExprList.getConnectedExpression();
        if (expr != null) {
            //
            // If the following method isn't called the expression 
            // is not resolved and doesn't have a schema context!
            if (xPathModel.resolveExpressionExtReferences(expr)) {
                //
                //
                ToRelativePathConverter converter =
                        new ToRelativePathConverter(expr, mBaseSContext);
                expr = converter.convert();
                //
                return xPathModel.getFactory().newXPathPredicateExpression(expr);
            }
        }
        //
        return null;
    }

    private XPathModel getXPathModel() {
        //
        // Create a new model
        BpelEntity bpelEntity = getDesignContext().getSelectedEntity();
        //
        if (bpelEntity == null) {
            return null;
        }
        //
        SchemaContextBasedCastResolver castResolver =
                new SchemaContextBasedCastResolver(mBaseSContext);
        XPathModel xPathModel = BpelXPathModelFactory.create(bpelEntity, castResolver);
        xPathModel.setSchemaContext(mBaseSContext);
        //
        return xPathModel;
    }

}
