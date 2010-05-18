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
import java.util.List;
import java.util.Map;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.cast.BpelCastManager;
import org.netbeans.modules.bpel.mapper.cast.BpelPseudoCompManager;
import org.netbeans.modules.bpel.mapper.model.CopyToProcessor.CopyToForm;
import org.netbeans.modules.bpel.mapper.model.BpelMapperLsmProcessor.MapperLsmContainer;
import org.netbeans.modules.bpel.mapper.predicates.PredicateFinderVisitor;
import org.netbeans.modules.bpel.mapper.predicates.BpelPredicateManager;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.multiview.BpelMapperDcc;
import org.netbeans.modules.bpel.mapper.tree.models.ConditionValueTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.DateValueTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.EmptyTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.ForEachConditionsTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.PartnerLinkTreeExtModel;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.ResultNodeFinder;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.bpel.model.api.Assign;
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
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.model.api.TimeEventHolder;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.api.support.BpelXPathExtFunctionMetadata;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.bpel.model.ext.editor.api.LsmProcessor.XPathCastResolverImpl;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.utils.GraphLayout;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.soa.xpath.mapper.specstep.SpecialStepManager;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperTreeNode;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaUtils;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;

/**
 * Implementation of the MapperModelFactory for the BPEL mapper.
 * 
 * @author Nikita Krjukov
 */
public class BpelMapperModelFactory implements MapperModelFactory {
    
    /**
     * Holds all preprocessed XPath expressions
     */
    private ArrayList<PreprocessedExpression> mPreprExprList = 
            new ArrayList<PreprocessedExpression>();

    protected ArrayList<PreprocessedGraphLocation> mPreprGraphLocationList = 
            new ArrayList<PreprocessedGraphLocation>();
  
    protected BpelMapperLsmProcessor lsmProcessor;
    
    protected MapperTcContext currentMapperTcContext;
    protected BpelDesignContext currentBpelDesignContext;   
    
    public BpelMapperModelFactory(MapperTcContext mapperTcContext, BpelDesignContext context) {
        currentMapperTcContext = mapperTcContext;
        currentBpelDesignContext = context;
    }

    public BpelDesignContext getCurrentBpelDesignContext() {
        return currentBpelDesignContext;
    }

    public MapperTcContext getCurrentMapperTcContext() {
        return currentMapperTcContext;
    }

    public MapperModel constructModel () {
        //
        Object synchObj = currentMapperTcContext;
        BpelChangeProcessor changeProcessor = new BpelChangeProcessor(
                synchObj, new BpelModelUpdater(currentMapperTcContext));
        //
        BpelEntity bpelEntity = currentBpelDesignContext.getContextEntity();
        if (bpelEntity instanceof Assign) {
            Assign assign = (Assign)bpelEntity;
            //
            BpelExtManagerHolder leftEmh = new BpelExtManagerHolderImpl(synchObj);
            EmptyTreeModel sourceModel = new EmptyTreeModel();
            VariableTreeModel sourceVariableModel = 
                    new VariableTreeModel(currentBpelDesignContext, leftEmh, true);
            sourceModel.addExtensionModel(sourceVariableModel);
            PartnerLinkTreeExtModel pLinkExtModel = 
                    new PartnerLinkTreeExtModel(assign, true);
            sourceModel.addExtensionModel(pLinkExtModel);
            //
            BpelExtManagerHolder rightEmh = new BpelExtManagerHolderImpl(synchObj);
            EmptyTreeModel targetModel = new EmptyTreeModel();
            VariableTreeModel targetVariableModel = 
                    new VariableTreeModel(currentBpelDesignContext, rightEmh, false);
            targetModel.addExtensionModel(targetVariableModel);
            pLinkExtModel = new PartnerLinkTreeExtModel(assign, false);
            targetModel.addExtensionModel(pLinkExtModel);
            //
            BpelMapperModel newMapperModel = new BpelMapperModel(
                    currentMapperTcContext, changeProcessor, 
                    sourceModel, leftEmh, targetModel, rightEmh);
            //
            lsmProcessor = new BpelMapperLsmProcessor(
                    newMapperModel, currentBpelDesignContext);
            lsmProcessor.processVariables();
            for (Copy copy : assign.getChildren(Copy.class)) {
                //
                if (Thread.currentThread().isInterrupted()) return null;
                //
                MapperLsmContainer toLsmCont = null;
                To to = copy.getTo();
                XPathExpression toExpr = null;
                if (to != null) {
                    toLsmCont = lsmProcessor.collectsLsm(
                            null, to, to, false, true);
                    //
                    // Checks if the copy for @xsi:type assignment.
                    toExpr = CopyToProcessor.constructExpression(
                            copy, to, toLsmCont, this);
                    if (SchemaUtils.checkIfXsiType(toExpr)) {
                        // Skip the copy with a @xsi:type assignment
                        continue;
                    }
                    //
                    // Register collected LSMs only after checking that
                    // the copy isn't an @xsi:type assignment!
                    lsmProcessor.registerAll(toLsmCont);
                }
                //
                MapperLsmContainer fromLsmCont = null;
                From from = copy.getFrom();
                if (from != null) {
                    fromLsmCont = lsmProcessor.collectsLsm(
                            null, from, from, true, false);
                    lsmProcessor.registerAll(fromLsmCont);
                }
                //
                if (Thread.currentThread().isInterrupted()) return null;
                //
                addCopyGraph(copy, newMapperModel, fromLsmCont, toLsmCont, toExpr);
            }
            //
            if (Thread.currentThread().isInterrupted()) return null;
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
            BpelExtManagerHolder leftEmh = new BpelExtManagerHolderImpl(synchObj);
            EmptyTreeModel sourceModel = new EmptyTreeModel();
            VariableTreeModel variableModel = new VariableTreeModel(
                    currentBpelDesignContext, leftEmh, true);
            sourceModel.addExtensionModel(variableModel);
            //
            DateValueTreeModel targetTreeModel = new DateValueTreeModel(timeEH);
            // SimpleTreeInfoProvider targetIP = new SimpleTreeInfoProvider();
            //
            BpelMapperModel newMapperModel = new BpelMapperModel(
                    currentMapperTcContext, changeProcessor,
                    sourceModel, leftEmh, targetTreeModel, null);
            lsmProcessor = new BpelMapperLsmProcessor(
                    newMapperModel, currentBpelDesignContext);
            lsmProcessor.processVariables();
            //
            if (Thread.currentThread().isInterrupted()) return null;
            //
            TimeEvent timeEvent = timeEH.getTimeEvent();
            if (timeEvent != null) {
                if (timeEvent instanceof For) {
                    For expr = (For)timeEvent;
                    MapperLsmContainer lsmCont = lsmProcessor.
                            collectsLsm(null, expr, expr, true, false);
                    lsmProcessor.registerAll(lsmCont);
                    addExpressionGraph(expr, newMapperModel, 
                            DateValueTreeModel.DURATION_CONDITION, 
                            timeEH, lsmCont);
                } else if (timeEvent instanceof DeadlineExpression) {
                    DeadlineExpression expr = (DeadlineExpression)timeEvent;
                    MapperLsmContainer lsmCont = lsmProcessor.
                            collectsLsm(null, expr, expr, true, false);
                    lsmProcessor.registerAll(lsmCont);
                    addExpressionGraph(expr, newMapperModel,
                            DateValueTreeModel.DEADLINE_CONDITION,
                            timeEH, lsmCont);
                }
            }
            //
            if (Thread.currentThread().isInterrupted()) return null;
            //
            postProcess(newMapperModel);
            return newMapperModel;
            //
        } else if (bpelEntity instanceof If ||
                bpelEntity instanceof ElseIf || 
                bpelEntity instanceof While || 
                bpelEntity instanceof RepeatUntil) {
            //
            BpelExtManagerHolder leftEmh = new BpelExtManagerHolderImpl(synchObj);
            EmptyTreeModel sourceModel = new EmptyTreeModel();
            VariableTreeModel variableModel = new VariableTreeModel(
                    currentBpelDesignContext, leftEmh, true);
            sourceModel.addExtensionModel(variableModel);
            //
            ConditionValueTreeModel targetTreeModel = 
                    new ConditionValueTreeModel(bpelEntity);
            //
            BpelMapperModel newMapperModel = new BpelMapperModel(
                    currentMapperTcContext, changeProcessor,
                    sourceModel, leftEmh, targetTreeModel, null);
            lsmProcessor = new BpelMapperLsmProcessor(
                    newMapperModel, currentBpelDesignContext);
            lsmProcessor.processVariables();
            //
            if (Thread.currentThread().isInterrupted()) return null;
            //
            // Add Graphs
            assert bpelEntity instanceof ConditionHolder;
            Expression expr = ((ConditionHolder)bpelEntity).getCondition();
            if (expr != null) {
                MapperLsmContainer lsmCont = lsmProcessor.
                        collectsLsm(null, expr, expr, true, false);
                lsmProcessor.registerAll(lsmCont);
                addExpressionGraph(expr, newMapperModel,
                        ConditionValueTreeModel.BOOLEAN_CONDITION,
                        expr, lsmCont);
            }
            //
            postProcess(newMapperModel);
            return newMapperModel;
            //
        } else if (bpelEntity instanceof ForEach) {
            ForEach forEach = (ForEach)bpelEntity;
            //
            BpelExtManagerHolder leftEmh = new BpelExtManagerHolderImpl(synchObj);
            EmptyTreeModel sourceModel = new EmptyTreeModel();
            VariableTreeModel variableModel = 
                    new VariableTreeModel(currentBpelDesignContext, leftEmh, true);
            sourceModel.addExtensionModel(variableModel);
            //
            ForEachConditionsTreeModel targetTreeModel = 
                    new ForEachConditionsTreeModel(forEach);
            //
            BpelMapperModel newMapperModel = new BpelMapperModel(
                    currentMapperTcContext, changeProcessor,
                    sourceModel, leftEmh, targetTreeModel,  null);
            lsmProcessor = new BpelMapperLsmProcessor(
                    newMapperModel, currentBpelDesignContext);
            lsmProcessor.processVariables();
            //
            if (Thread.currentThread().isInterrupted()) return null;
            //
            // Add Graphs
            MapperLsmContainer lsmCont = null;
            Expression expr = forEach.getStartCounterValue();
            if (expr != null) {
                lsmCont = lsmProcessor.collectsLsm(null, expr, expr, true, false);
                lsmProcessor.registerAll(lsmCont);
                //
                addExpressionGraph(expr, newMapperModel, 
                        ForEachConditionsTreeModel.START_VALUE, 
                        forEach, lsmCont);
            }
            //
            if (Thread.currentThread().isInterrupted()) return null;
            //
            expr = forEach.getFinalCounterValue();
            if (expr != null) {
                lsmCont = lsmProcessor.collectsLsm(null, expr, expr, true, false);
                lsmProcessor.registerAll(lsmCont);
                //
                addExpressionGraph(expr, newMapperModel, 
                        ForEachConditionsTreeModel.FINAL_VALUE, 
                        forEach, lsmCont);
            }
            //
            if (Thread.currentThread().isInterrupted()) return null;
            //
            CompletionCondition cc = forEach.getCompletionCondition();
            if (cc != null) {
                expr = cc.getBranches();
                if (expr != null) {
                    lsmCont = lsmProcessor.collectsLsm(null, expr, expr, true, false);
                    lsmProcessor.registerAll(lsmCont);
                    //
                    addExpressionGraph(expr, newMapperModel, 
                            ForEachConditionsTreeModel.COMPLETION_CONDITION, 
                            forEach, lsmCont);
                }
            }
            //
            if (Thread.currentThread().isInterrupted()) return null;
            //
            postProcess(newMapperModel);
            return newMapperModel;
            //
        }
        //
        return null;
    }

    private void addCopyGraph(Copy copy, BpelMapperModel newMapperModel, 
            MapperLsmContainer fromLsmCont, MapperLsmContainer toLsmCont,
            XPathExpression toExpr) {
        //
        From copyFrom = copy.getFrom();
        if (copyFrom == null) {
            return;
        }
        //
        MapperSwingTreeModel rightTreeModel = newMapperModel.getRightTreeModel();
        To copyTo = copy.getTo();
        if (copyTo == null) {
            return;
        }
        //
        CopyToForm form = CopyToProcessor.getCopyToForm(copyTo);
        if (form == CopyToForm.EXPRESSION) {
            //
            // If the TO expression hasn't constructed before...
            if (toExpr == null) {
                toExpr = CopyToProcessor.constructExpression(copy, copyTo, toLsmCont, this);
                //
                // Checks if the copy for @xsi:type assignment.
                if (SchemaUtils.checkIfXsiType(toExpr)) {
                    return;
                }
            }
            //
            // Populate predicate manager
            if (toExpr != null) {
                BpelMapperModelFactory.collectPredicates(toExpr, rightTreeModel);
            }
        }
        //
        FromProcessor fromProcessor = new FromProcessor(this, copy);
        BpelMapperSwingTreeModel leftTreeModel = newMapperModel.getLeftTreeModel();
        Graph newGraph = new Graph(newMapperModel, copy);
        newGraph = fromProcessor.populateGraph(newGraph, leftTreeModel, fromLsmCont);
//System.out.println("!!!! newGraph: " + newGraph);
        if (newGraph == null) {
            return;
        }
        //
        List<TreeItemFinder> toNodeFinderList = CopyToProcessor.
                constructFindersList(form, copy, copyTo, toExpr, toLsmCont, this);
        
        if (toNodeFinderList.isEmpty() && (currentBpelDesignContext != null)) {
            // add warning message about wrong "toExpr"
            
            if (toExpr != null) {
                BpelMapperDcc.addErrMessage(
                    currentBpelDesignContext.getValidationErrMsgBuffer(), 
                    toExpr.getExpressionString(), "to");
            }
        }
        //
        PreprocessedGraphLocation graphLocation = 
                new PreprocessedGraphLocation(newGraph, toNodeFinderList);
        mPreprGraphLocationList.add(graphLocation);
    }
    
    private void addExpressionGraph(Expression expr, 
            BpelMapperModel newMapperModel, 
            String targetNodeName, 
            BpelEntity contextEntity, 
            MapperLsmContainer lsmCont) {
        //
        if (Thread.currentThread().isInterrupted()) return;
        assert expr != null && newMapperModel != null && targetNodeName != null;
        //
        Graph newGraph = new Graph(newMapperModel, expr);
        //
        MapperSwingTreeModel leftTreeModel = newMapperModel.getLeftTreeModel();
        populateGraph(newGraph, leftTreeModel, contextEntity, expr, lsmCont);
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
        Map<Graph, TreePath> graphTargetMap = new HashMap<Graph, TreePath>();
        //
        // Add Graphs according its locations
        for (PreprocessedGraphLocation graphLocation : mPreprGraphLocationList) {
            TreePath targetTreePath = graphLocation.bindGraph(mapperModel);
            if (targetTreePath != null) {
                graphTargetMap.put(graphLocation.getGraph(), targetTreePath);
            }
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
                    expr.populateGraph(leftTreeModel, graphTargetMap.get(graph));
                }
                //
                GraphLayout.layout(graph);            
            }
        }
    }
    
    public void populateGraph(Graph graph, 
            MapperSwingTreeModel leftTreeModel,
            BpelEntity contextEntity, Expression expr, 
            MapperLsmContainer lsmCont) {
        //
        String exprLang = expr.getExpressionLanguage();
        String exprText = expr.getContent();
        boolean isXPathExpr = (exprLang == null || exprLang.length() == 0 ||
                BpelXPathModelFactory.DEFAULT_EXPR_LANGUAGE.equals(exprLang));
        //
        ArrayList<XPathExpression> exprList = new ArrayList<XPathExpression>();
        boolean hasConnectedExpr = exprText != null && !exprText.trim()
                .startsWith(BpelXPathModelFactory.XPATH_EXPR_DELIMITER);

        if (isXPathExpr && exprText != null && exprText.length() != 0) {
          String[] partsArr = BpelXPathModelFactory.split(exprText);
//System.out.println();
//System.out.println();
//System.out.println("-------------------------------------");
            for (String anExprText : partsArr) {
//System.out.println("1: anExprText: " + anExprText);
                XPathExpression newXPathExpr = parseExpression(contextEntity, anExprText, lsmCont, true);
//System.out.println("2: newXPathExpr: " + newXPathExpr);
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
                PreprocessedExpression newPreprExpr = new PreprocessedExpression(anExpr, graph, connectToTargetTree);
//System.out.println("3: newPreprExpr: " + newPreprExpr);
                mPreprExprList.add(newPreprExpr);
                //
                // Only first expression can be connected
                connectToTargetTree = false;
            }
        }
    }

    private XPathExpression parseExpression(BpelEntity contextEntity, 
            String exprText, MapperLsmContainer lsmCont, boolean useFrom) {
        //
        XPathExpression expr = null;
        try {
            XPathCastResolver castResolver = new XPathCastResolverImpl(lsmCont, useFrom);
            XPathModel newXPathModel = BpelXPathModelFactory.create(
                    contextEntity, castResolver);
            //
            // NOT NEED to specify schema context because of an 
            // expression with variable is implied here. 
            //
            expr = newXPathModel.parseExpression(exprText);
//System.out.println();
//System.out.println("1: " + expr);
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
        SoaTreeModel sourceTreeModel = treeModel.getSourceModel();
        //
        // Look for the VariableTreeModel tree extension
        VariableTreeModel varTreeModel = SoaTreeModel.MyUtils.findExtensionModel(
                sourceTreeModel, VariableTreeModel.class);
        //    
        if (varTreeModel != null) {
            BpelPredicateManager predManager = varTreeModel.getPredicateManager();
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

        // todo m
        private boolean hideDplWrapper() {
            return true;
        }

        private boolean isDplWrapper(TreePath targetTreePath) {
            boolean result = false;
            if (targetTreePath == null) {
                return result;
            }

            if (mConnectToTargetTree && mExpr instanceof XPathExtensionFunction) {
                XPathExtensionFunction extFunction = (XPathExtensionFunction)mExpr;
                ExtFunctionMetadata metadata = extFunction.getMetadata();
                if (metadata.equals(BpelXPathExtFunctionMetadata.DO_XSL_TRANSFORM_METADATA)
                        && extFunction.getChildCount() == 2
                        && extFunction.getChild(1) instanceof XPathVariableReference)
                {
                    Object treeNode = targetTreePath.getLastPathComponent();
                    if (treeNode instanceof MapperTreeNode) {
                        Object dObj = ((MapperTreeNode)treeNode).getDataObject();
                        result = dObj instanceof PartnerLink;
                    }
                }
            }

            return result;
        }

        /**
         * Populate graph with the XPath expression and link graph's vertices 
         * with the source tree.
         * @param leftTreeModel
         */
        public void populateGraph(MapperSwingTreeModel leftTreeModel,
                TreePath targetTreePath) {
            //temporary (151676) use DPL link builder until special type of links will be created
            GraphBuilderVisitor graphBuilderVisitor = hideDplWrapper()
                    ? new DplWrapperLinkBuilder(mGraph,
                        leftTreeModel, mConnectToTargetTree, currentBpelDesignContext, isDplWrapper(targetTreePath))
                    : new GraphBuilderVisitor(mGraph,
                        leftTreeModel, mConnectToTargetTree, currentBpelDesignContext);
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

        public Graph getGraph() {
            return mGraph;
        }

        public TreePath bindGraph(BpelMapperModel newMapperModel) {
            TreeFinderProcessor fProcessor = new TreeFinderProcessor(
                    newMapperModel.getRightTreeModel());
            TreePath targetTreePath = fProcessor.findFirstNode(mFindersList);
            // TreePath targetTreePath = newMapperModel.getRightTreeModel().
            //        findFirstNode(mFindersList);
            if (targetTreePath != null) {
                newMapperModel.addGraph(mGraph, targetTreePath);
            }
            return targetTreePath;
        }
    }

    protected class BpelExtManagerHolderImpl extends BpelExtManagerHolder.Default {

        public BpelExtManagerHolderImpl(Object synchSource) {
            setCastManager(new BpelCastManager());
            setPseudoCompManager(new BpelPseudoCompManager());
            setPredicateManager(new BpelPredicateManager(synchSource));
            setSpecialStepManager(new SpecialStepManager());
        }

    }

}
