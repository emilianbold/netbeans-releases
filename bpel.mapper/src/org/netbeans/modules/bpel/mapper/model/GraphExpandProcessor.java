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
import java.util.List;
import java.util.Map;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.CopyToProcessor.CopyToForm;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.tree.models.ConditionValueTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.DateValueTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.ForEachConditionsTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.ResultNodeFinder;
import org.netbeans.modules.soa.ui.tree.impl.SimpleFinder;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.model.api.TimeEventHolder;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;

/**
 *
 * @author nk160297
 */
public class GraphExpandProcessor {
    
    // TODO m add graphEntity support
    public static void expandGraph(
            MapperTcContext mapperTcContext, BpelDesignContext context) {
        if (mapperTcContext == null || context == null) {
            return;
        }
        
        //
        Mapper mapper = mapperTcContext.getMapper();
        MapperModel mModel = mapper.getModel();
        //
        BpelEntity contextEntity = context.getContextEntity();
        expandVariablesNode(mapper, contextEntity.getBpelModel(), true, true);
        //
        BpelEntity graphEntity = context.getGraphEntity();
        List<TreePath> tPathList = getTargetPathes(mapperTcContext, graphEntity);
        boolean expandAll = contextEntity instanceof ForEach;
        boolean isFirst = true;
        for (TreePath tPath : tPathList) {
            //
            Graph graph = ((BpelMapperModel)mModel).getGraphsInside(null).get(tPath);
            //
            mapper.setExpandedState(tPath, true);
            if (graph != null) {
                mapper.setExpandedGraphState(tPath, true);
            }
            //
            if (expandAll || isFirst) {
                mapper.setSelected(tPath);
                //
                if (graph != null) {
                    mapper.getRightTree().scrollRectToVisible(graph.getBounds());
                    //
                    // Expand source tree
                    LeftTree leftTree = mapper.getLeftTree();
                    expandIngoingLinks(graph, leftTree);
                }
            }
            //
            isFirst = false;
        }
    }
    
    public static void expandAllGraphs(Mapper mapper, MapperModel mModel)  {
        Map<TreePath, Graph> graphMap = ((BpelMapperModel)mModel).getGraphsInside(null);
        for (TreePath tPath : graphMap.keySet()) {
            Graph graph = graphMap.get(tPath);
            //
            mapper.setExpandedState(tPath, true);
            if (graph != null) {
                mapper.setExpandedGraphState(tPath, true);
                //
                // Expand source tree
                LeftTree leftTree = mapper.getLeftTree();
                expandIngoingLinks(graph, leftTree);
            }
        }
    }
    
    public static void selectGraph(Mapper mapper, TreePath tPath, Graph graph)  {
        //
        mapper.setSelected(tPath);
        //
        if (graph != null) {
            mapper.getRightTree().scrollRectToVisible(graph.getBounds());
        }
    }
    
    /**
     * Returns the list of TreePath for the specified entity which have to be 
     * expanded.
     * @param mapperTcContext
     * @param bpelEntity
     * @return
     */
    private static List<TreePath> getTargetPathes(
            MapperTcContext mapperTcContext, BpelEntity bpelEntity) {
        //
        Mapper mapper = mapperTcContext.getMapper();
        MapperModel mm = mapper.getModel();
        assert mm instanceof BpelMapperModel;
        BpelMapperModel mModel = ((BpelMapperModel)mm);
        //
        ArrayList<TreePath> result = new ArrayList<TreePath>();
        //
        TreeFinderProcessor fProcessor = new TreeFinderProcessor(
                mModel.getRightTreeModel());
        //
        Class<? extends BpelEntity> entityType = bpelEntity.getElementType();
        if (entityType == Copy.class) {
            Copy copy = (Copy)bpelEntity;
            // Expand target tree
            To copyTo = copy.getTo();
            CopyToForm form = CopyToProcessor.getCopyToForm(copyTo);
            ArrayList<TreeItemFinder> toNodeFinderList = CopyToProcessor.
                    constructFindersList(form, copy, copyTo, null, null, null, null);
            TreePath targetTreePath = fProcessor.findFirstNode(toNodeFinderList);
//            TreePath targetTreePath = mModel.getRightTreeModel().
//                    findFirstNode(toNodeFinderList);
            if (targetTreePath != null) {
                result.add(targetTreePath);
            }
        } else if (entityType == Assign.class) {
            Assign assign = (Assign)bpelEntity;
            List<Copy> copyList = assign.getChildren(Copy.class);
            if (!copyList.isEmpty()) {
                Copy firstCopy = copyList.iterator().next();
                return getTargetPathes(mapperTcContext, firstCopy);
            }
            //
        } else if (entityType == Wait.class || 
                entityType == OnAlarmPick.class || 
                entityType == OnAlarmEvent.class) {
            //
            TimeEventHolder timeEH = (TimeEventHolder)bpelEntity;
            TimeEvent timeEvent = timeEH.getTimeEvent();
            if (timeEvent != null) {
                String targetNodeName = null;
                if (timeEvent instanceof For) {
                    targetNodeName = DateValueTreeModel.DURATION_CONDITION;
                } else if (timeEvent instanceof DeadlineExpression) {
                    targetNodeName = DateValueTreeModel.DEADLINE_CONDITION;
                }
                //
                List<TreeItemFinder> finderList = Collections.singletonList(
                        (TreeItemFinder)new ResultNodeFinder(targetNodeName));
                TreePath targetTreePath = fProcessor.findFirstNode(finderList);
                // TreePath targetTreePath = mModel.getRightTreeModel().
                //         findFirstNode(finderList);
                if (targetTreePath != null) {
                    result.add(targetTreePath);
                }
            }
            //
        } else if (entityType == If.class ||
                entityType == ElseIf.class || 
                entityType == While.class || 
                entityType == RepeatUntil.class) {
            //
            List<TreeItemFinder> finderList = Collections.singletonList(
                    (TreeItemFinder)new ResultNodeFinder(
                    ConditionValueTreeModel.BOOLEAN_CONDITION));
            TreePath targetTreePath = fProcessor.findFirstNode(finderList);
//            TreePath targetTreePath = mModel.getRightTreeModel().
//                    findFirstNode(finderList);
            if (targetTreePath != null) {
                result.add(targetTreePath);
            }
        } else if (entityType == ForEach.class) {
            //
            List<TreeItemFinder> finderList = Collections.singletonList(
                    (TreeItemFinder)new ResultNodeFinder(
                    ForEachConditionsTreeModel.START_VALUE));
            TreePath targetTreePath = fProcessor.findFirstNode(finderList);
//            TreePath targetTreePath = mModel.getRightTreeModel().
//                    findFirstNode(finderList);
            if (targetTreePath != null) {
                result.add(targetTreePath);
            }
            //
            finderList = Collections.singletonList(
                    (TreeItemFinder)new ResultNodeFinder(
                    ForEachConditionsTreeModel.FINAL_VALUE));
            targetTreePath = fProcessor.findFirstNode(finderList);
//            targetTreePath = mModel.getRightTreeModel().
//                    findFirstNode(finderList);
            if (targetTreePath != null) {
                result.add(targetTreePath);
            }
            //
            finderList = Collections.singletonList(
                    (TreeItemFinder)new ResultNodeFinder(
                    ForEachConditionsTreeModel.COMPLETION_CONDITION));
            targetTreePath = fProcessor.findFirstNode(finderList);
//            targetTreePath = mModel.getRightTreeModel().
//                    findFirstNode(finderList);
            if (targetTreePath != null) {
                result.add(targetTreePath);
            }
        } else if (entityType == StartCounterValue.class) {
            List<TreeItemFinder> finderList = Collections.singletonList(
                    (TreeItemFinder)new ResultNodeFinder(
                    ForEachConditionsTreeModel.START_VALUE));
            TreePath targetTreePath = fProcessor.findFirstNode(finderList);
//            TreePath targetTreePath = mModel.getRightTreeModel().
//                    findFirstNode(finderList);
            if (targetTreePath != null) {
                result.add(targetTreePath);
            }
        } else if (entityType == FinalCounterValue.class) {
            List<TreeItemFinder> finderList = Collections.singletonList(
                    (TreeItemFinder)new ResultNodeFinder(
                    ForEachConditionsTreeModel.FINAL_VALUE));
            TreePath targetTreePath = fProcessor.findFirstNode(finderList);
//            TreePath targetTreePath = mModel.getRightTreeModel().
//                    findFirstNode(finderList);
            if (targetTreePath != null) {
                result.add(targetTreePath);
            }
        } else if (entityType == CompletionCondition.class) {
            List<TreeItemFinder> finderList = Collections.singletonList(
                    (TreeItemFinder)new ResultNodeFinder(
                    ForEachConditionsTreeModel.COMPLETION_CONDITION));
            TreePath targetTreePath = fProcessor.findFirstNode(finderList);
//            TreePath targetTreePath = mModel.getRightTreeModel().
//                    findFirstNode(finderList);
            if (targetTreePath != null) {
                result.add(targetTreePath);
            }
        } 
        //
        return result;
    }
    
    /**
     * Expands all tree nodes in the source tree to which ingoing links 
     * of the graph are connected. 
     * @param graph
     * @param leftTree
     */
    public static void expandIngoingLinks(Graph graph, LeftTree leftTree) {
        List<Link> ingoingLinks = graph.getIngoingLinks();
        for (Link link : ingoingLinks) {
            SourcePin sourcePin = link.getSource();
            if (sourcePin != null && sourcePin instanceof TreeSourcePin) {
                TreePath sourcePath = ((TreeSourcePin)sourcePin).getTreePath();
                if (sourcePath != null) {
                    leftTree.expandPath(sourcePath.getParentPath());
                }
            }
        } 
    }
    
    
    /**
     * Expands the tree node "Variables" in the source and (or) the target trees.
     * @param mapper
     * @param bpelModel
     * @param leftTree
     * @param rightTree
     */
    public static void expandVariablesNode(Mapper mapper, 
            final BpelModel bpelModel, boolean leftTree, boolean rightTree) {
        //
        TreeItemFinder finder = new SimpleFinder() {
             protected boolean isFit(Object treeItem) {
                if (treeItem instanceof Process && 
                        bpelModel.getProcess().equals(treeItem)) {
                     // found!!!
                    return true;
                }
                return false;
            }
            protected boolean drillDeeper(Object treeItem) {
                if (treeItem == SoaTreeModel.TREE_ROOT) {
                    return true;
                }
                return false;
            }
        };
        //
        MapperModel mm = mapper.getModel();
        assert mm instanceof BpelMapperModel;
        BpelMapperModel mModel = (BpelMapperModel)mm;
        //
        if (leftTree) {
            TreeFinderProcessor fProcessor = new TreeFinderProcessor(
                    mModel.getLeftTreeModel());
            TreePath targetTreePath = fProcessor.findFirstNode(
                    Collections.singletonList(finder));
//            TreePath tPath = mModel.getLeftTreeModel().findFirstNode(
//                    Collections.singletonList(finder));
            if (targetTreePath != null) {
                mapper.getLeftTree().expandPath(targetTreePath);
            }
        }
        //
        if (rightTree) {
            TreeFinderProcessor fProcessor = new TreeFinderProcessor(
                    mModel.getRightTreeModel());
            TreePath targetTreePath = fProcessor.findFirstNode(
                    Collections.singletonList(finder));
//            TreePath tPath = mModel.getRightTreeModel().findFirstNode(
//                    Collections.singletonList(finder));
            if (targetTreePath != null) {
                mapper.setExpandedGraphState(targetTreePath, true);
            }
        }
        //
        
    }
}
