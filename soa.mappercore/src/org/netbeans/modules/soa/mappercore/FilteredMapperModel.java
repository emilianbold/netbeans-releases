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

package org.netbeans.modules.soa.mappercore;

import java.awt.datatransfer.Transferable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.FilterableMapperModel;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.VertexItem;

/**
 *
 * @author anjeleevich
 */
public class FilteredMapperModel extends FilteredTreeModel 
        implements MapperModel 
{
    private MapperModel mapperModel;
    private FilteredTreeModel filteredLeftTreeModel;
    
    private Set<Graph> nonEmptyGraphsSet = new HashSet<Graph>();
    private Map<TreePath, Graph> nonEmptyGraphsMap 
            = new HashMap<TreePath, Graph>();
    
    private Map<Object, Set<Object>> leftAcceptedChildren 
            = new HashMap<Object, Set<Object>>();
    private Map<Object, Set<Object>> rightAcceptedChildren 
            = new HashMap<Object, Set<Object>>();
    
    private boolean filterLeft;
    private boolean filterRight;
    
    public FilteredMapperModel(MapperModel mapperModel, 
            boolean filterLeft, 
            boolean filterRight) 
    {
        super(mapperModel, filterRight);
        
        this.filterLeft = filterLeft;
        this.filterRight = filterRight;
        
        this.mapperModel = mapperModel;
        
        Object root = mapperModel.getRoot();
        if (root != null) {
            collectGraphs(mapperModel, new TreePath(root), 
                    nonEmptyGraphsMap, nonEmptyGraphsSet);
        }
        
        if (filterLeft) {
            Set<TreePath> leftTreePathes = new HashSet<TreePath>();
            collectTreeSourcePinPathes(nonEmptyGraphsSet, leftTreePathes);
            fillAcceptedChildrenMap(leftTreePathes, leftAcceptedChildren);
            filteredLeftTreeModel = new FilteredTreeModel(
                    mapperModel.getLeftTreeModel(), true) 
            {
                protected boolean accept(Object parent, Object child) {
                    Set<Object> acceptedChildren = leftAcceptedChildren
                            .get(parent);
                    if ((acceptedChildren != null) 
                            && acceptedChildren.contains(child)) {
                        return true;
                    }
                    return showLeft(parent, child);
                }
            };
        }
        
        if (filterRight) {
            fillAcceptedChildrenMap(nonEmptyGraphsMap.keySet(), 
                    rightAcceptedChildren);
        }
    }
    
    private boolean showLeft(Object parent, Object node) {
        if (mapperModel instanceof FilterableMapperModel) {
            return ((FilterableMapperModel) mapperModel).showLeft(parent, node);
        }
        return false;
    }
    
    private boolean showRight(Object parent, Object node) {
        if (mapperModel instanceof FilterableMapperModel) {
            return ((FilterableMapperModel) mapperModel).showRight(parent, node);
        }
        return false;
    }
    
    
    public boolean isFilterLeft() {
        return filterLeft;
    }
    
    public boolean isFilterRight() {
        return filterRight;
    }
    
    public MapperModel getOriginalMapperModel() {
        return mapperModel;
    }
    
    protected boolean accept(Object parent, Object child) {
        Set<Object> acceptedChildren = rightAcceptedChildren.get(parent);
        if ((acceptedChildren != null) && acceptedChildren.contains(child)) {
            return true;
        }
        return showRight(parent, child);
    }
    
    public TreeModel getLeftTreeModel() {
        return (filterLeft) 
                ? filteredLeftTreeModel
                : mapperModel.getLeftTreeModel();
    }

    public TreeSourcePin getTreeSourcePin(TreePath treePath) {
        return mapperModel.getTreeSourcePin(treePath);
    }

    public Graph getGraph(TreePath treePath) {
        return mapperModel.getGraph(treePath);
    }

    public boolean searchGraphsInside(TreePath treePath) {
        return mapperModel.searchGraphsInside(treePath);
    }

    public boolean canConnect(TreePath treePath, SourcePin source, 
            TargetPin target, TreePath oldTreePath, Link oldLink) 
    {
        return mapperModel.canConnect(treePath, source, target, 
                oldTreePath, oldLink);
    }

    public void connect(TreePath treePath, SourcePin source, TargetPin target, 
            TreePath oldTreePath, Link oldLink) 
    {
        mapperModel.connect(treePath, source, target, 
                oldTreePath, oldLink);
    }

    public GraphSubset getGraphSubset(Transferable transferable) {
        return mapperModel.getGraphSubset(transferable);
    }

    public boolean canCopy(TreePath treePath, GraphSubset graphSubset) {
        return mapperModel.canCopy(treePath, graphSubset);
    }

    public boolean canMove(TreePath treePath, GraphSubset graphSubset) {
        return mapperModel.canMove(treePath, graphSubset);
    }

    public GraphSubset copy(TreePath treePath, GraphSubset graphSubset, 
            int x, int y) 
    {
        return mapperModel.copy(treePath, graphSubset, x, y);
    }

    public void move(TreePath treePath, GraphSubset graphSubset, int x, int y) {
        mapperModel.move(treePath, graphSubset, x, y);
    }

    public void valueChanged(TreePath treePath, VertexItem vertexItem, Object newValue) {
        mapperModel.valueChanged(treePath, vertexItem, newValue);
    }

    public boolean canEditInplace(VertexItem vItem) {
        return mapperModel.canEditInplace(vItem);
    }
    
    public void delete(TreePath currentTreePath, GraphSubset graphGroup) {
        mapperModel.delete(currentTreePath, graphGroup);
    }
    
    private void collectGraphs(MapperModel mapperModel, 
            TreePath currentTreePath, 
            Map<TreePath, Graph> resultMap, 
            Set<Graph> resultSet) 
    {
        Graph currentGraph = mapperModel.getGraph(currentTreePath);
        if (currentGraph != null && !currentGraph.isEmpty()) {
            resultMap.put(currentTreePath, currentGraph);
            resultSet.add(currentGraph);
        }
        
        Object node = currentTreePath.getLastPathComponent();
        
        if (mapperModel.isLeaf(node)) return;
        if (!mapperModel.searchGraphsInside(currentTreePath)) return;
        
        int childCount = mapperModel.getChildCount(node);
        
        for (int i = 0; i < childCount; i++) {
            Object child = mapperModel.getChild(node, i);
            collectGraphs(mapperModel, currentTreePath.pathByAddingChild(child), 
                    resultMap, resultSet);
        }
    }
    
    private void collectTreeSourcePinPathes(Set<Graph> graphsSet, 
            Set<TreePath> resultSet) 
    {
        for (Graph graph : graphsSet) {
            List<Link> ingoingLinks = graph.getIngoingLinks();
            if (ingoingLinks != null) {
                for (Link link : ingoingLinks) {
                    SourcePin sourcePin = link.getSource();
                    if (sourcePin instanceof TreeSourcePin) {
                        TreePath treePath = ((TreeSourcePin) sourcePin)
                                .getTreePath();
                        if (treePath != null) {
                            resultSet.add(treePath);
                        }
                    }
                }
            }
        }
    }
    
    private void fillAcceptedChildrenMap(Set<TreePath> treePathes, 
            Map<Object, Set<Object>> acceptedChildrenMap)
    {
        for (TreePath treePath : treePathes) {
            TreePath parentTreePath = treePath.getParentPath();
            Object node = treePath.getLastPathComponent();
            
            while (parentTreePath != null) {
                Object parent = parentTreePath.getLastPathComponent();
                
                Set<Object> acceptedChildren = acceptedChildrenMap.get(parent);
                if (acceptedChildren == null) {
                    acceptedChildren = new HashSet<Object>();
                    acceptedChildrenMap.put(parent, acceptedChildren);
                }
                
                acceptedChildren.add(node);
                
                treePath = parentTreePath;
                parentTreePath = treePath.getParentPath();
                node = parent;
            }
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (filteredLeftTreeModel != null) {
            filteredLeftTreeModel.dispose();
        }
    }

    public List<TreePath> findInLeftTree(String value) {
        return mapperModel.findInLeftTree(value);
    }

    public List<TreePath> findInRightTree(String value) {
        return mapperModel.findInRightTree(value);
    }
}
