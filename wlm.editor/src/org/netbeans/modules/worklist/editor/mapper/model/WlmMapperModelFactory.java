/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.utils.GraphLayout;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.wlm.model.api.TAction;
import org.netbeans.modules.wlm.model.api.TChangeVariables;
import org.netbeans.modules.wlm.model.api.TCopy;
import org.netbeans.modules.wlm.model.api.TExpression;
import org.netbeans.modules.wlm.model.api.TFrom;
import org.netbeans.modules.wlm.model.api.TInit;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.TTo;
import org.netbeans.modules.wlm.model.api.VariableInit;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.xpath.WlmXPathModelFactory;
import org.netbeans.modules.worklist.editor.mapper.lsm.MapperLsmProcessor;
import org.netbeans.modules.worklist.editor.mapper.lsm.MapperLsmProcessor.MapperLsmContainer;
import org.netbeans.modules.worklist.editor.mapper.MapperTcContext;
import org.netbeans.modules.worklist.editor.mapper.WlmChangeProcessor;
import org.netbeans.modules.worklist.editor.mapper.WlmDesignContext;
import org.netbeans.modules.worklist.editor.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.worklist.editor.mapper.tree.model.VariableTreeModel;
import org.netbeans.modules.worklist.editor.mapper.tree.search.ResultNodeFinder;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;

/**
 * Implementation of the MapperModelFactory for the WLM mapper.
 * 
 * @author nk160297
 */
public class WlmMapperModelFactory implements MapperModelFactory {
//    private static Set<Class> mMappableObjects = new HashSet<Class>();
//
//    static {
//        mMappableObjects.add(ForEach.class);
//    }
//
//    public static boolean needShowMapper(Object source) {
//        if (source instanceof WLMComponent) {
//            Class bpelClass = ((WLMComponent)source).getElementType();
//            return mMappableObjects.contains(bpelClass);
//        }
//        //
//        return false;
//    }
    
    /**
     * Holds all preprocessed XPath expressions
     */
    protected ArrayList<PreprocessedExpression> mPreprExprList = 
            new ArrayList<PreprocessedExpression>();

    protected ArrayList<PreprocessedGraphLocation> mPreprGraphLocationList = 
            new ArrayList<PreprocessedGraphLocation>();
  
    protected MapperLsmProcessor lsmProcessor;
    
    protected MapperTcContext currentMapperTcContext;
    protected WlmDesignContext currentWlmDesignContext;
    
    public WlmMapperModelFactory(MapperTcContext mapperTcContext, WlmDesignContext context) {
        currentMapperTcContext = mapperTcContext;
        currentWlmDesignContext = context;
    }

    public WlmDesignContext getCurrentWlmDesignContext() {
        return currentWlmDesignContext;
    }

    public MapperTcContext getCurrentMapperTcContext() {
        return currentMapperTcContext;
    }

    public MapperModel constructModel() {
        //
        Mapper mapper = currentMapperTcContext.getMapper();
        WlmChangeProcessor changeProcessor = new WlmChangeProcessor(
                currentMapperTcContext.getDesignContextController(),
                new WlmModelUpdater(currentMapperTcContext));
        //
        WLMComponent wlmComponent = currentWlmDesignContext.getContextEntity();
        List<TCopy> copyList = null;
        if (wlmComponent instanceof TTask) {
            TTask task = (TTask)wlmComponent;
            TInit init = task.getInit();
            if (init != null) {
                VariableInit varInit = init.getVariableInit();
                if (varInit != null) {
                    copyList = varInit.getCopyList();
                }
            }
        } else if (wlmComponent instanceof TAction) {
            TAction action = (TAction)wlmComponent;
            TChangeVariables chvar = action.getChangeVariables();
            if (chvar != null) {
                copyList = chvar.getCopyList();
            }
        }
        //
        VariableTreeModel sourceVariableModel =
                new VariableTreeModel(currentWlmDesignContext, true);
        VariableTreeModel targetVariableModel =
                new VariableTreeModel(currentWlmDesignContext, false);
        //
        WlmMapperModel newMapperModel = new WlmMapperModel(
                currentMapperTcContext, changeProcessor,
                sourceVariableModel, targetVariableModel);
        //
        lsmProcessor = new MapperLsmProcessor(
                newMapperModel, currentWlmDesignContext);
        lsmProcessor.processVariables();
        //
        if (copyList != null && !copyList.isEmpty()) {
            for (TCopy copy : copyList) {
                MapperLsmContainer fromLsmCont = null;
                TFrom from = copy.getFrom();
                if (from != null) {
                    fromLsmCont = lsmProcessor.collectsLsm(
                            null, from, from, true, false);
                    lsmProcessor.registerAll(fromLsmCont);
                }
                //
                MapperLsmContainer toLsmCont = null;
                TTo to = copy.getTo();
                if (to != null) {
                    toLsmCont = lsmProcessor.collectsLsm(
                            null, to, to, false, true);
                    lsmProcessor.registerAll(toLsmCont);
                }
                //

                addCopyGraph(copy, newMapperModel, fromLsmCont, toLsmCont);
            }
        }
        //
        postProcess(newMapperModel);
        return newMapperModel;
    }

    private void addCopyGraph(TCopy copy, WlmMapperModel newMapperModel,
            MapperLsmContainer fromLsmCont, MapperLsmContainer toLsmCont) {
        //
        TFrom copyFrom = copy.getFrom();
        if (copyFrom == null) {
            return;
        }
        //
        Graph newGraph = new Graph(newMapperModel, copy);
        //
        FromProcessor fromProcessor = new FromProcessor(this, copy);
        MapperSwingTreeModel leftTreeModel = newMapperModel.getLeftTreeModel();
        newGraph = fromProcessor.populateGraph(newGraph, leftTreeModel, fromLsmCont);
        if (newGraph == null) {
            return;
        }
        //
        MapperSwingTreeModel rightTreeModel = newMapperModel.getRightTreeModel();
        TTo copyTo = copy.getTo();
        if (copyTo == null) {
            return;
        }
        XPathExpression toExpr = CopyToProcessor.constructExpression(copy, copyTo, toLsmCont, this);
        //
        // Populate predicate manager
        if (toExpr != null) {
            WlmMapperModelFactory.collectPredicates(toExpr, rightTreeModel);
        }
        List<TreeItemFinder> toNodeFinderList = CopyToProcessor.
                constructFindersList(copy, copyTo, toExpr, toLsmCont, this);
        
        if (toNodeFinderList.isEmpty() && (currentWlmDesignContext != null)) {
            // add warning message about wrong "toExpr"
            
//            if (toExpr != null) {
//                DesignContextControllerImpl.addErrMessage(
//                    currentBpelDesignContext.getValidationErrMsgBuffer(),
//                    toExpr.getExpressionString(), "to");
//            }
        }
        //
        PreprocessedGraphLocation graphLocation = 
                new PreprocessedGraphLocation(newGraph, toNodeFinderList);
        mPreprGraphLocationList.add(graphLocation);
    }
    
    private void addExpressionGraph(TExpression expr,
            WlmMapperModel newMapperModel,
            String targetNodeName, 
            WLMComponent contextEntity,
            MapperLsmContainer lsmCont) {
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
    public void postProcess(WlmMapperModel mapperModel) {
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
            WLMComponent contextEntity, TExpression expr,
            MapperLsmContainer lsmCont) {
        //
        String exprLang = expr.getExpressionLanguage();
        String exprText = expr.getContent();
        boolean isXPathExpr = (exprLang == null || exprLang.length() == 0 ||
                WlmXPathModelFactory.DEFAULT_EXPR_LANGUAGE.equals(exprLang));
        //
        ArrayList<XPathExpression> exprList = new ArrayList<XPathExpression>();
        boolean hasConnectedExpr = exprText != null && !exprText.trim()
                .startsWith(WlmXPathModelFactory.XPATH_EXPR_DELIMITER);
        if (isXPathExpr && exprText != null && exprText.length() != 0) {
            String[] partsArr = WlmXPathModelFactory.split(exprText);
            for (String anExprText : partsArr) {
                XPathExpression newXPathExpr = parseExpression(
                        contextEntity, anExprText, lsmCont, true);
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
    
    private XPathExpression parseExpression(WLMComponent contextEntity,
            String exprText, MapperLsmContainer lsmCont, boolean useFrom) {
        //
        XPathExpression expr = null;
        try {
            XPathCastResolver castResolver = null;
//            XPathCastResolver castResolver = new XPathCastResolverImpl(lsmCont, useFrom);
            XPathModel newXPathModel = WlmXPathModelFactory.create(
                    contextEntity, castResolver);
            //
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
        SoaTreeModel sourceTreeModel = treeModel.getSourceModel();
        //
        // Look for the VariableTreeModel tree extension
        VariableTreeModel varTreeModel = SoaTreeModel.MyUtils.findExtensionModel(
                sourceTreeModel, VariableTreeModel.class);
        //    
        if (varTreeModel != null) {
//            PredicateManager predManager = varTreeModel.getPredicateManager();
//            SpecialStepManager sStepManager = varTreeModel.getSStepManager();
//            if (predManager != null) {
//                PredicateFinderVisitor predFinderVisitor =
//                        new PredicateFinderVisitor(predManager, sStepManager);
//                expr.accept(predFinderVisitor);
//            }
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
            GraphBuilderVisitor graphBuilderVisitor = new GraphBuilderVisitor(mGraph, 
                leftTreeModel, mConnectToTargetTree, currentWlmDesignContext);
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
        
        public void bindGraph(WlmMapperModel newMapperModel) {
            TreeFinderProcessor fProcessor = new TreeFinderProcessor(
                    newMapperModel.getRightTreeModel());
            TreePath targetTreePath = fProcessor.findFirstNode(mFindersList);
            // TreePath targetTreePath = newMapperModel.getRightTreeModel().
            //        findFirstNode(mFindersList);
            if (targetTreePath != null) {
                newMapperModel.addGraph(mGraph, targetTreePath);
            }
        }
        
    }
    
}
