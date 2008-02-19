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

package org.netbeans.modules.bpel.mapper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.CopyToProcessor.CopyToForm;
import org.netbeans.modules.bpel.mapper.predicates.PredicateFinderVisitor;
import org.netbeans.modules.bpel.mapper.predicates.PredicateManager;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.predicates.SpecialStepManager;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.ConditionValueTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.DateValueTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.EmptyTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.ForEachConditionsTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.PartnerLinkTreeExtModel;
import org.netbeans.modules.bpel.mapper.tree.models.SimpleTreeInfoProvider;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.ResultNodeFinder;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperModelFactory;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemFinder;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.AssignChild;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.ConditionHolder;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.Expression;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.model.api.TimeEventHolder;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.api.support.XPathModelFactory;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.utils.GraphLayout;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;

/**
 * Implementaiton of the MapperModelFactory for the BPEL mapper.
 * 
 * @author nk160297
 */
public class BpelMapperModelFactory implements MapperModelFactory {

    private static Set<Class> mMappableObjects = new HashSet<Class>();
    
    static {
        mMappableObjects.add(Assign.class);
        mMappableObjects.add(Copy.class);
        //
        mMappableObjects.add(Wait.class);
        mMappableObjects.add(OnAlarmPick.class);
        mMappableObjects.add(OnAlarmEvent.class);
        //
        mMappableObjects.add(If.class);
        mMappableObjects.add(ElseIf.class);
        mMappableObjects.add(While.class);
        mMappableObjects.add(RepeatUntil.class);
        //
        mMappableObjects.add(ForEach.class);
    }
   
    public static boolean needShowMapper(Object source) {
        if (source instanceof BpelEntity) {
            Class bpelClass = ((BpelEntity)source).getElementType();
            return mMappableObjects.contains(bpelClass);
        }
        //
        return false;
    }
    
    /**
     * Holds all preprocessed XPath expressions
     */
    protected ArrayList<PreprocessedExpression> mPreprExprList = 
            new ArrayList<PreprocessedExpression>();

    protected ArrayList<PreprocessedGraphLocation> mPreprGraphLocationList = 
            new ArrayList<PreprocessedGraphLocation>();
    
    public BpelMapperModelFactory() {
    }
    
    public MapperModel constructModel(
            MapperTcContext mapperTcContext, BpelDesignContext context) {
        //
        BpelChangeProcessor changeProcessor = 
                new BpelChangeProcessor(new BpelModelUpdater(mapperTcContext));
        mapperTcContext.getDesignContextController().
                setBpelModelUpdateSource(changeProcessor);
        //
        BpelEntity bpelEntity = context.getContextEntity();
        if (bpelEntity instanceof Assign) {
            Assign assign = (Assign)bpelEntity;
            //
            EmptyTreeModel sourceModel = new EmptyTreeModel();
            VariableTreeModel sourceVariableModel = new VariableTreeModel(context);
            sourceModel.addExtensionModel(sourceVariableModel);
            PartnerLinkTreeExtModel pLinkExtModel = 
                    new PartnerLinkTreeExtModel(assign, true);
            //sourceModel.addExtensionModel(pLinkExtModel);   [Issue 125124]
            //
            EmptyTreeModel targetModel = new EmptyTreeModel();
            VariableTreeModel targetVariableModel = new VariableTreeModel(context);
            targetModel.addExtensionModel(targetVariableModel);
            pLinkExtModel = new PartnerLinkTreeExtModel(assign, false);
            targetModel.addExtensionModel(pLinkExtModel);
            //
            BpelMapperModel newMapperModel = new BpelMapperModel(
                    mapperTcContext, changeProcessor, sourceModel, targetModel);
            for (AssignChild assignChild : assign.getAssignChildren()) {
                if (assignChild instanceof Copy) {
                    addCopyGraph((Copy)assignChild, newMapperModel);
                }
            }
            //
            postProcess(newMapperModel);
            return newMapperModel;
            //
        } else if (bpelEntity instanceof Wait || 
                bpelEntity instanceof OnAlarmPick || 
                bpelEntity instanceof OnAlarmEvent) {
            //
            TimeEventHolder timeEH = (TimeEventHolder)bpelEntity;
            //
            EmptyTreeModel sourceModel = new EmptyTreeModel();
            VariableTreeModel variableModel = new VariableTreeModel(context);
            sourceModel.addExtensionModel(variableModel);
            //
            DateValueTreeModel targetTreeModel = new DateValueTreeModel(timeEH);
            SimpleTreeInfoProvider targetIP = new SimpleTreeInfoProvider();
            //
            BpelMapperModel newMapperModel = new BpelMapperModel(
                    mapperTcContext, changeProcessor, sourceModel, targetTreeModel);
            //
            TimeEvent timeEvent = timeEH.getTimeEvent();
            if (timeEvent != null) {
                if (timeEvent instanceof For) {
                    Expression expr = (For)timeEvent;
                    addExpressionGraph(expr, newMapperModel, 
                            DateValueTreeModel.DURATION_CONDITION, timeEH);
                } else if (timeEvent instanceof DeadlineExpression) {
                    Expression expr = (DeadlineExpression)timeEvent;
                    addExpressionGraph(expr, newMapperModel, 
                            DateValueTreeModel.DEADLINE_CONDITION, timeEH);
                }
            }
            //
            postProcess(newMapperModel);
            return newMapperModel;
            //
        } else if (bpelEntity instanceof If ||
                bpelEntity instanceof ElseIf || 
                bpelEntity instanceof While || 
                bpelEntity instanceof RepeatUntil) {
            //
            EmptyTreeModel sourceModel = new EmptyTreeModel();
            VariableTreeModel variableModel = new VariableTreeModel(context);
            sourceModel.addExtensionModel(variableModel);
            //
            ConditionValueTreeModel targetTreeModel = 
                    new ConditionValueTreeModel(bpelEntity);
            //
            BpelMapperModel newMapperModel = new BpelMapperModel(
                    mapperTcContext, changeProcessor, sourceModel, targetTreeModel);
            //
            // Add Graphs
            assert bpelEntity instanceof ConditionHolder;
            Expression expr = ((ConditionHolder)bpelEntity).getCondition();
            if (expr != null) {
                addExpressionGraph(expr, newMapperModel, 
                        ConditionValueTreeModel.BOOLEAN_CONDITION, bpelEntity);
            }
            //
            postProcess(newMapperModel);
            return newMapperModel;
            //
        } else if (bpelEntity instanceof ForEach) {
            ForEach forEach = (ForEach)bpelEntity;
            //
            EmptyTreeModel sourceModel = new EmptyTreeModel();
            VariableTreeModel variableModel = new VariableTreeModel(context);
            sourceModel.addExtensionModel(variableModel);
            //
            ForEachConditionsTreeModel targetTreeModel = 
                    new ForEachConditionsTreeModel(forEach);
            //
            BpelMapperModel newMapperModel = new BpelMapperModel(
                    mapperTcContext, changeProcessor, sourceModel, targetTreeModel);
            //
            // Add Graphs
            Expression expr = forEach.getStartCounterValue();
            if (expr != null) {
                addExpressionGraph(expr, newMapperModel, 
                        ForEachConditionsTreeModel.START_VALUE, forEach);
            }
            //
            expr = forEach.getFinalCounterValue();
            if (expr != null) {
                addExpressionGraph(expr, newMapperModel, 
                        ForEachConditionsTreeModel.FINAL_VALUE, forEach);
            }
            //
            CompletionCondition cc = forEach.getCompletionCondition();
            if (cc != null) {
                expr = cc.getBranches();
                if (expr != null) {
                    addExpressionGraph(expr, newMapperModel, 
                            ForEachConditionsTreeModel.COMPLETION_CONDITION, 
                            forEach);
                }
            }
            //
            postProcess(newMapperModel);
            return newMapperModel;
            //
        }
        //
        return null;
    }

    private void addCopyGraph(Copy copy, BpelMapperModel newMapperModel) {
        //
        From copyFrom = copy.getFrom();
        if (copyFrom == null) {
            return;
        }
        //
        Graph newGraph = new Graph(newMapperModel, copy);
        //
        CopyFromProcessor fromProcessor = new CopyFromProcessor(this, copy);
        MapperSwingTreeModel leftTreeModel = newMapperModel.getLeftTreeModel();
        newGraph = fromProcessor.populateGraph(newGraph, leftTreeModel);
        if (newGraph == null) {
            return;
        }
        //
        MapperSwingTreeModel rightTreeModel = newMapperModel.getRightTreeModel();
        To copyTo = copy.getTo();
        CopyToForm form = CopyToProcessor.getCopyToForm(copyTo);
        XPathExpression toExpr = null;
        if (form == CopyToForm.EXPRESSION) {
            toExpr = CopyToProcessor.constructExpression(copy, copyTo);
            //
            // Populate predicate manager  
            if (toExpr != null) {
                BpelMapperModelFactory.collectPredicates(toExpr, rightTreeModel);
            }
        }
        ArrayList<TreeItemFinder> toNodeFinderList = CopyToProcessor.
                constructFindersList(form, copy, copyTo, toExpr);
        //
        PreprocessedGraphLocation graphLocation = 
                new PreprocessedGraphLocation(newGraph, toNodeFinderList);
        mPreprGraphLocationList.add(graphLocation);
    }
    
    private void addExpressionGraph(Expression expr, 
            BpelMapperModel newMapperModel, 
            String targetNodeName, 
            BpelEntity contextEntity) {
        assert expr != null && newMapperModel != null && targetNodeName != null;
        //
        Graph newGraph = new Graph(newMapperModel);
        //
        MapperSwingTreeModel leftTreeModel = newMapperModel.getLeftTreeModel();
        populateGraph(newGraph, leftTreeModel, contextEntity, expr);
        //
        List<TreeItemFinder> finderList = Collections.singletonList(
                (TreeItemFinder)new ResultNodeFinder(targetNodeName));
        //
        PreprocessedGraphLocation graphLocation = 
                new PreprocessedGraphLocation(newGraph, finderList);
        mPreprGraphLocationList.add(graphLocation);
    }
    
    //==========================================================================
    // Common methods to populate a graph
    
    /**
     * Run second stage of 2 stage graph loading. 
     * It has to be called at the end of the mapper model's creation 
     * @param mapperModel
     */
    public void postProcess(BpelMapperModel mapperModel) {
        //
        // Add Graphs according its locations
        for (PreprocessedGraphLocation graphLocation : mPreprGraphLocationList) {
            graphLocation.bindGraph(mapperModel);
        }
        // 
        // Sort expressions by Graphs
        HashMap<Graph, List<PreprocessedExpression>> map = 
                new HashMap<Graph, List<PreprocessedExpression>>();
        //
        for (PreprocessedExpression expr : mPreprExprList) {
            Graph graph = expr.getGraph();
            List<PreprocessedExpression> exprList = map.get(graph);
            if (exprList == null) {
                exprList = new ArrayList<PreprocessedExpression>();
                map.put(graph, exprList);
            }
            exprList.add(expr);
        }
        //
        MapperSwingTreeModel leftTreeModel = mapperModel.getLeftTreeModel();
        //
        // Add prepared XPath expressions to graphs one by one
        for (Graph graph : map.keySet()) {
            List<PreprocessedExpression> exprList = map.get(graph);
            if (exprList != null && !exprList.isEmpty()) {
                for (PreprocessedExpression expr : exprList) {
                    expr.populateGraph(leftTreeModel);
                }
                //
                GraphLayout.layout(graph);            
            }
        }
    }
    
    public void populateGraph(Graph graph, 
            MapperSwingTreeModel leftTreeModel, 
            BpelEntity contextEntity, Expression expr) {
        //
        String exprLang = expr.getExpressionLanguage();
        String exprText = expr.getContent();
        boolean isXPathExpr = (exprLang == null || exprLang.length() == 0 ||
                XPathModelFactory.DEFAULT_EXPR_LANGUAGE.equals(exprLang));
        //
        ArrayList<XPathExpression> exprList = new ArrayList<XPathExpression>();
        boolean hasConnectedExpr = exprText != null && !exprText.trim()
                .startsWith(XPathModelFactory.XPATH_EXPR_DELIMITER);
        if (isXPathExpr && exprText != null && exprText.length() != 0) {
            String[] partsArr = XPathModelFactory.split(exprText);
            for (String anExprText : partsArr) {
                XPathExpression newXPathExpr = 
                        parseExpression(contextEntity, anExprText);
                if (newXPathExpr != null) {
                    exprList.add(newXPathExpr);
                }
            }
            //
            // Collecting all predicates first!
            for (XPathExpression anExpr : exprList) {
                collectPredicates(anExpr, leftTreeModel);
            }
            //
            // Preprocess XPath expression
            boolean connectToTargetTree = hasConnectedExpr;
            for (XPathExpression anExpr : exprList) {
                PreprocessedExpression newPreprExpr = new PreprocessedExpression(
                        anExpr, graph, connectToTargetTree);
                mPreprExprList.add(newPreprExpr);
                //
                // Only first expression can be connected
                connectToTargetTree = false;
            }
        }
    }
    
    private XPathExpression parseExpression(
            BpelEntity contextEntity, String exprText) {
        //
        XPathExpression expr = null;
        try {
            XPathModel newXPathModel = XPathModelFactory.create(contextEntity);
            // NOT NEED to specify schema context because of an 
            // expression with variable is implied here. 
            //
            expr = newXPathModel.parseExpression(exprText);
            //
        } catch (XPathException ex) {
            // Do nothing
            // ErrorManager.getDefault().notify(ex);
        }
        return expr;
    }
    
    // special steps also are collected here!
    public static void collectPredicates(
            XPathExpression expr, MapperSwingTreeModel treeModel) {
        //
        MapperTreeModel sourceTreeModel = treeModel.getSourceModel();
        //
        // Look for the VariableTreeModel tree extension
        VariableTreeModel varTreeModel = MapperTreeModel.Utils.findExtensionModel(
                sourceTreeModel, VariableTreeModel.class);
        //    
        if (varTreeModel != null) {
            PredicateManager predManager = varTreeModel.getPredicateManager();
            SpecialStepManager sStepManager = varTreeModel.getSStepManager();
            if (predManager != null) {
                PredicateFinderVisitor predFinderVisitor = 
                        new PredicateFinderVisitor(predManager, sStepManager);
                expr.accept(predFinderVisitor);
            }
        }
    }
    
    /** 
     * Holds the data about an XPath expression and a target Graph. 
     * 
     * This class is a part of the framework, which populates graphs 
     * in 2 stages: 
     * -- At first stage the expressions are created and the predicates are loaded. 
     * -- At second stage the graphs are populated, and connected to the source 
     * and target tree.
     */
    protected class PreprocessedExpression {
        private XPathExpression mExpr;
        private Graph mGraph; 
        private boolean mConnectToTargetTree;
        
        public PreprocessedExpression(XPathExpression expr, Graph graph, 
                boolean connectToTargetTree) {
            //
            mExpr = expr;
            mGraph = graph;
            mConnectToTargetTree = connectToTargetTree;
        }
        
        public Graph getGraph() {
            return mGraph;
        }
        
        /**
         * Populate graph with the XPath expression and link graph's vertices 
         * with the source tree.
         * @param leftTreeModel
         */
        public void populateGraph(MapperSwingTreeModel leftTreeModel) {
            GraphBuilderVisitor graphBuilderVisitor = 
                    new GraphBuilderVisitor(mGraph, leftTreeModel, 
                    mConnectToTargetTree);
            mExpr.accept(graphBuilderVisitor);
        }
    }
    
    /** 
     * Holds the data about a Graph and its location in target tree. 
     * 
     * This class is a part of the framework, which populates graphs 
     * in 2 stages: 
     * -- At first stage the expressions are created and the predicates are loaded. 
     * -- At second stage the graphs are populated, and connected to the source 
     * and target tree.
     */
    protected class PreprocessedGraphLocation {
        private Graph mGraph;
        private List<TreeItemFinder> mFindersList;
        
        public PreprocessedGraphLocation(Graph graph, 
                List<TreeItemFinder> findersList) {
            mGraph = graph;
            mFindersList = findersList;
        }
        
        public void bindGraph(BpelMapperModel newMapperModel) {
            TreePath targetTreePath = newMapperModel.getRightTreeModel().
                    findFirstNode(mFindersList);
            if (targetTreePath != null) {
                newMapperModel.addGraph(mGraph, targetTreePath);
            }
        }
        
    }
    
    
}
