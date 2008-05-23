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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionEvent;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionListener;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.utils.Utils;
import org.netbeans.modules.soa.mappercore.graphics.VerticalGradient;
import org.netbeans.modules.soa.mappercore.graphics.XRange;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author anjeleevich
 */
public class Mapper extends JPanel {

    private MapperModel model;
    private MapperModel filteredModel;
    
    private MapperNode root;
    private TreeModelListener treeModelListener = new TreeModelListenerImpl();
    private MapperSelectionListener selectionListener;
    private int leftDividerPosition = -1;
    private int rightDividerPosition = -1;
    private JPanel leftDivider;
    private JPanel rightDivider;
    private LeftTree leftTree;
    private RightTree rightTree;
    private Canvas canvas;
    // L&F
    private int leftChildIndent;
    private int rightChildIndent;
    private Icon openIcon;
    private Icon closedIcon;
    private Icon leafIcon;
    private Icon expandedIcon;
    private Icon collapsedIcon;
    private Color treeLineColor;
    private Dimension preferredTreeSize = null;
    private XRange graphXRange = null;
    private boolean validNodes = false;
    private boolean repaintSceduled = false;
    private MapperContext context = new DefaultMapperContext();
    private LinkTool linkTool;
    private MoveTool moveTool;
    private EventListenerList listenersList = new EventListenerList();
    private SelectionModel selectionModel;
    private TreePath pathDndselect = null;

    private FiltersToolBar filtersToolBar;

    private boolean filterLeft = false;
    private boolean filterRight = false;
   
    /** Creates a new instance of RightTree */
    public Mapper(MapperModel model) {
        setLayout(new MapperLayout());

        selectionModel = new SelectionModel(this);

        leftTree = new LeftTree(this);
        rightTree = new RightTree(this);
        canvas = new Canvas(this);

        leftDivider = new MapperDivider();
        leftDivider.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));

        rightDivider = new MapperDivider();
        rightDivider.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));

        filtersToolBar = new FiltersToolBar(this);
        
        new MapperDividersController(this, leftDivider, rightDivider);

        add(leftTree.getView(), MapperLayout.LEFT_SCROLL);
        add(leftDivider, MapperLayout.LEFT_DIVIDER);
        add(canvas.getView(), MapperLayout.CENTER_SCROLL);
        add(rightDivider, MapperLayout.RIGHT_DIVIDER);
        add(rightTree.getView(), MapperLayout.RIGHT_SCROLL);
        add(filtersToolBar, MapperLayout.TOOL_BAR);

        new ScrollPaneYSyncronizer(canvas.getScrollPane(),
                rightTree.getScrollPane());

        linkTool = new LinkTool(this);
        moveTool = new MoveTool(this);

        setModel(model);

        selectionModel.addSelectionListener(new MapperSelectionListener() {

            public void mapperSelectionChanged(MapperSelectionEvent event) {
                repaint();
            }
        });
        
        setSelected();  //[Issue 125764]
        
        InputMap iMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap aMap = getActionMap();
        
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK),
                "mapper-select-all-action");
        aMap.put("mapper-select-all-action", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                SelectionModel selectionModel = getSelectionModel();
                TreePath selectedPath = selectionModel.getSelectedPath();
                if (selectedPath != null) {
                    selectionModel.selectAll(selectedPath);
                }
            }
        });
        
        aMap.put(DefaultEditorKit.copyAction, new CopyMapperAction(canvas));
        aMap.put(DefaultEditorKit.cutAction, new CutMapperAction(canvas));
        aMap.put(DefaultEditorKit.pasteAction, new PasteMapperAction(canvas));
//        actionMap.put("delete", ExplorerUtils.actionDelete(manager, false));

    
        getAccessibleContext().setAccessibleName(NbBundle
                .getMessage(Mapper.class, "ACSN_Mapper")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle
                .getMessage(Mapper.class, "ACSD_Mapper")); // NOI18N
    }
    
    public boolean isFilterLeft() {
        return filterLeft;
    }
    
    public boolean isFilterRight() {
        return filterRight;
    }
    
    public void setFilter(boolean filterLeft, boolean filterRight) {
        if (this.filterLeft == filterLeft && this.filterRight == filterRight) {
            return;
        }
        
        this.filterLeft = filterLeft;
        this.filterRight = filterRight;
        
        filtersToolBar.updateButtonsState();

        if (model == null) return;

        TreePath leftTreeSelection = leftTree.getSelectionPath();
        TreePath rightTreeSelection = rightTree.getSelectionModel()
                .getSelectedPath();
        GraphSubset selectedGraphSubset = getSelectionModel()
                .getSelectedSubset();
        
        VertexItem selectedVertexItem = getSelectionModel()
                .getSelectedVertexItem();
        
        Enumeration<TreePath> expandedLeftPathesEnumeration = leftTree
                .getExpandedDescendants(new TreePath(leftTree.getModel()
                .getRoot()));
        
        List<TreePath> expandedLeftPathes = new ArrayList<TreePath>();

        while (expandedLeftPathesEnumeration.hasMoreElements()) {
            expandedLeftPathes.add(expandedLeftPathesEnumeration.nextElement());
        }
        
        List<TreePath> expandedRightPathes = getExpandedPathes();
        List<TreePath> expandedGraphPathes = getExpandedGraphsPathes();
        
        MapperModel oldFilteredModel = this.filteredModel;
        MapperModel newFilteredModel = (filterLeft || filterRight) 
                ? new FilteredMapperModel(model, filterLeft, filterRight)
                : model;
        
        TreeModel oldLeftTreeModel = leftTree.getModel();
        TreeModel newLeftTreeModel = (model != null) 
                ? newFilteredModel.getLeftTreeModel() : null;
        
        if (oldLeftTreeModel != newLeftTreeModel) {
            leftTree.setModel(newLeftTreeModel);
        }
        
        oldFilteredModel.removeTreeModelListener(treeModelListener);
        newFilteredModel.addTreeModelListener(treeModelListener);
        
        this.filteredModel = newFilteredModel;
        
        if (oldFilteredModel instanceof FilteredMapperModel) {
            ((FilteredMapperModel) oldFilteredModel).dispose();
        }
        
        root = new MapperNode(this, null, filteredModel.getRoot());

        invalidateNodes();
        
//        for (int i = expandedRightPathes.size() - 1; i >= 0; i--) {
//            TreePath treePath = expandedRightPathes.get(i);
//            if (!Utils.isTreePathExpandable(newFilteredModel, treePath)) {
//                expandedRightPathes.remove(i);
//            }
//        }
//        
//        for (int i = expandedGraphPathes.size() - 1; i >= 0; i--) {
//            TreePath treePath = expandedGraphPathes.get(i);
//            if (!Utils.isTreePathInModel(newFilteredModel, treePath)) {
//                expandedGraphPathes.remove(i);
//            }
//        }
        
        for (int i = expandedLeftPathes.size() - 1; i >= 0; i--) {
            TreePath treePath = expandedLeftPathes.get(i);
            if (!Utils.isTreePathExpandable(newLeftTreeModel, treePath)) {
                expandedLeftPathes.remove(i);
            }
        }
        
        applyExpandedPathes(expandedRightPathes);
        applyExpandedGraphsPathes(expandedGraphPathes);
        
        for (TreePath treePath : expandedLeftPathes) {
            leftTree.expandPath(treePath);
        }

        if (leftTreeSelection != null && Utils
                .isTreePathInModel(newLeftTreeModel, leftTreeSelection))
        {   
            leftTree.setSelectionPath(leftTreeSelection);
        }

        firePropertyChange(MODEL_PROPERTY, oldFilteredModel, filteredModel);
        
        if (rightTreeSelection != null && Utils
                .isTreePathInModel(newFilteredModel, rightTreeSelection))
        {
            SelectionModel selectionModel = getSelectionModel();
            selectionModel.setSelected(rightTreeSelection);
            
            if (selectedVertexItem != null) {
                selectionModel.setSelected(rightTreeSelection, 
                        selectedVertexItem);
            } else if (selectedGraphSubset != null) {
                for (int i = selectedGraphSubset.getLinkCount() - 1; i >= 0; 
                    i--) 
                {
                    selectionModel.switchSelected(rightTreeSelection, 
                            selectedGraphSubset.getLink(i));
                }

                for (int i = selectedGraphSubset.getVertexCount() - 1; 
                    i >= 0; i--) 
                {
                    selectionModel.switchSelected(rightTreeSelection, 
                            selectedGraphSubset.getVertex(i));
                }
            }
        }
        
        repaintNodes();
        revalidate();
        repaint();
    }

    public void addRightTreeExpansionListener(TreeExpansionListener listener) {
        listenersList.add(TreeExpansionListener.class, listener);
    }

    public void removeRightTreeExpansionListener(TreeExpansionListener listener) {
        listenersList.remove(TreeExpansionListener.class, listener);
    }

    public void addSelectionListener(MapperSelectionListener listener) {
        getSelectionModel().addSelectionListener(listener);
    }

    public void removeSelectionListener(MapperSelectionListener listener) {
        getSelectionModel().removeSelectionListener(listener);
    }
    
    public TreePath getSelectedDndPath() {
        return pathDndselect;
    }

    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    LinkTool getLinkTool() {
        return linkTool;
    }

    MoveTool getMoveTool() {
        return moveTool;
    }

    public MapperContext getContext() {
        return context;
    }
    
    public void setSelectedDndPath(TreePath path) {
        pathDndselect = path;
    }

    public void setContext(MapperContext context) {
        if (context == null) {
            context = new DefaultMapperContext();
        }

        if (this.context != context) {
            this.context = context;

            TreeCellRenderer oldCellRenderer = leftTree.getCellRenderer();
            leftTree.setCellRenderer(new DefaultTreeCellRenderer());
            leftTree.setCellRenderer(oldCellRenderer);
            leftTree.revalidate();
            leftTree.repaint();

            MapperNode root = getRoot();

            if (root != null) {
                root.invalidateTree();
                root.repaint();
            }
        }
    }

    public TreePath getSelected() {
        return getSelectionModel().getSelectedPath();
    }

    public TreePath getSelectedPath() {
        return getSelectionModel().getSelectedPath();
    }

    public void setSelected(TreePath treePath) {
        getSelectionModel().setSelected(treePath);
    }

    void resetRepaintSceduled() {
        repaintSceduled = false;
    }

    void setSelectedNode(MapperNode selectedNode) {
        setSelected(selectedNode.getTreePath());
    }

    public void setExpandedState(TreePath treePath, boolean state) {
        if (state) {
            MapperNode node = getNode(treePath, true);
            TreePath expandedTreePath = null;
            while (node != null) {
                if (!node.isLeaf() && node.isCollapsed()) {
                    node.setExpanded(true);
                    if (expandedTreePath == null) {
                        expandedTreePath = node.getTreePath();
                    }
                }
                node = node.getParent();
            }

            if (expandedTreePath != null) {
                fireNodeExpanded(expandedTreePath);
            }
        } else {
            MapperNode node = getNode(treePath, false);
            if (node != null && !node.isLeaf() && node.isExpanded()) {
                node.setCollapsed(true);
                fireNodeCollapsed(treePath);
            }
        }
    }

    public void setExpandedGraphState(TreePath treePath, boolean state) {
        if (model == null) {
            return;
        }

        Graph graph = model.getGraph(treePath);

        if (graph == null || graph.isEmpty()) {
            return;
        }

        if (state) {
            MapperNode node = getNode(treePath, true);
            node.setGraphExpanded(true);
        } else {
            MapperNode node = getNode(treePath, false);
            if (node != null) {
                node.setGraphExpanded(false);
            }
        }
    }

    private void fireNodeCollapsed(TreePath treePath) {
        if (treePath == null) {
            return;
        }
        TreeExpansionListener[] listeners = listenersList.getListeners(TreeExpansionListener.class);
        if (listeners != null && listeners.length > 0) {
            TreeExpansionEvent event = new TreeExpansionEvent(this, treePath);
            for (TreeExpansionListener l : listeners) {
                l.treeCollapsed(event);
            }
        }
    }

    private void fireNodeExpanded(TreePath treePath) {
        if (treePath == null) {
            return;
        }
        TreeExpansionListener[] listeners = listenersList.getListeners(TreeExpansionListener.class);
        if (listeners != null && listeners.length > 0) {
            TreeExpansionEvent event = new TreeExpansionEvent(this, treePath);
            for (TreeExpansionListener l : listeners) {
                l.treeExpanded(event);
            }
        }
    }

    void collapseNode(MapperNode node) {
        setExpandedState(node.getTreePath(), false);
    }

    void expandNode(MapperNode node) {
        setExpandedState(node.getTreePath(), true);
    }

    void switchCollapsedExpandedState(MapperNode node) {
        if (node.isLeaf()) {
            return;
        }
        if (node.isExpanded()) {
            collapseNode(node);
        } else {
            expandNode(node);
        }
    }

    public int getLeftDividerPosition() {
        return leftDividerPosition;
    }

    public int getRightDividerPosition() {
        return rightDividerPosition;
    }

    void setDividerPositions(
            int leftDividerPosition,
            int rightDividerPosition) {
        this.leftDividerPosition = leftDividerPosition;
        this.rightDividerPosition = rightDividerPosition;
    }
    
    public void setModel(MapperModel model) {
        MapperModel oldModel = this.model;
        MapperModel oldFilteredModel = this.filteredModel;
        
        if (oldModel != model) {
            this.model = model;
            this.filteredModel = ((filterLeft || filterRight) && model != null)
                    ? new FilteredMapperModel(model, filterLeft, filterRight)
                    : model;

            TreeModel oldLeftTreeModel = (oldModel != null) 
                    ? leftTree.getModel() : null;
            TreeModel newLeftTreeModel = (model != null) 
                    ? filteredModel.getLeftTreeModel() : null;
            
            if (oldLeftTreeModel != newLeftTreeModel) {
                leftTree.setModel(newLeftTreeModel);
            }
            
            if (oldFilteredModel != null) {
                oldFilteredModel.removeTreeModelListener(treeModelListener);
            }

            if (filteredModel != null) {
                filteredModel.addTreeModelListener(treeModelListener);
                root = new MapperNode(this, null, filteredModel.getRoot());
//                root.getChildCount();
            } else {
                root = null;
            }

            invalidateNodes();
            repaintNodes();
            
            revalidate();
            repaint();

            firePropertyChange(MODEL_PROPERTY, oldFilteredModel, filteredModel);
            
            if (oldFilteredModel instanceof FilteredMapperModel) {
                ((FilteredMapperModel) oldFilteredModel).dispose();
            } 
        }
    }
    
    public MapperModel getModel() {
        return model;
    }
    
    public MapperModel getFilteredModel() {
        return filteredModel;
    }

    MapperNode getRoot() {
        return root;
    }

    public LeftTree getLeftTree() {
        return leftTree;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public RightTree getRightTree() {
        return rightTree;
    }
    
    
    public void expandNonEmptyGraphs() {
        expandGraphs(Utils.getNonEmptyGraphs(getFilteredModel()));
    }
    
    
    public void expandGraphs(List<TreePath> treePathes) {
        if (treePathes == null) {
            return;
        }

        Set<TreePath> parentTreePathes = new HashSet<TreePath>();
        for (TreePath treePath : treePathes) {
            TreePath parentTreePath = treePath.getParentPath();
            if (parentTreePath != null) {
                parentTreePathes.add(parentTreePath);
            }
        }

        for (TreePath parentTreePath : parentTreePathes) {
            setExpandedState(parentTreePath, true);
        }

        for (TreePath treePath : treePathes) {
            setExpandedGraphState(treePath, true);
        }
    }

    public List<TreePath> getExpandedPathes() {
        List<TreePath> result = new ArrayList<TreePath>();
        MapperNode rootNode = getRoot();
        
        if (root != null) {
            collectExpandedPathes(rootNode, result);
        }
        
        return result;
    }
    
    private void collectExpandedPathes(MapperNode node, List<TreePath> result) {
        if (node.isLeaf()) return;
        
        if (node.isExpanded()) {
            result.add(node.getTreePath());
        }
        
        if (node.isLoaded()) {
            for (int i = node.getChildCount() - 1; i >= 0; i--) {
                collectExpandedPathes(node.getChild(i), result);
            }
        }
    }
    
    public List<TreePath> getExpandedGraphsPathes() {
        List<TreePath> result = new ArrayList<TreePath>();
        
        MapperModel model = getFilteredModel();
        MapperNode rootNode = getRoot();
        
        if (model != null && root != null) {
            collectExpandedGraphsPathes(model, rootNode, result);
        }
        
        return result;
    }
    
    private void collectExpandedGraphsPathes(MapperModel model, MapperNode node, 
            List<TreePath> result) 
    {
        Graph graph = node.getGraph();
        if (graph != null && !graph.isEmpty() && node.isGraphExpanded()) {
            result.add(node.getTreePath());
        }
        
        if (node.isLeaf()) return;
        if (!model.searchGraphsInside(node.getTreePath())) return;
        
        if (node.isLoaded()) {
            for (int i = node.getChildCount() - 1; i >= 0; i--) {
                collectExpandedGraphsPathes(model, node.getChild(i), result);
            }
        }
    }
    
    
    public void applyExpandedPathes(List<TreePath> rightTreePathes) {
        if (rightTreePathes == null || rightTreePathes.isEmpty()) return;

        MapperModel model = getFilteredModel();
        if (model == null) return;
                    
        for (TreePath treePath : rightTreePathes) {
            if (Utils.isTreePathExpandable(model, treePath)) {
                MapperNode node = getNode(treePath, true);
                if (node != null && !node.isLeaf() && node.isCollapsed()) {
                    node.setExpanded(true);
                    fireNodeExpanded(treePath);
                }
            }
        }
    }
    
    public void applyExpandedGraphsPathes(List<TreePath> rightTreePathes) {
        if (rightTreePathes == null || rightTreePathes.isEmpty()) return;

        MapperModel model = getFilteredModel();
        
        if (model == null) return;
        
        for (TreePath treePath : rightTreePathes) {
            if (Utils.isTreePathInModel(model, treePath)) {
                Graph graph = model.getGraph(treePath);
                if (graph != null && !graph.isEmpty()) {
                    MapperNode node = getNode(treePath, true);
                    if (node != null && node.isGraphCollapsed()) {
                        node.setGraphExpanded(true);
                    }
                }
            }
        }
    }    

    public void hideOtherPathes(int expandedLevel) {
        if (model == null) return;
        if (root == null) return;
        
        TreePath selectedPath = getSelectedPath();
        if (selectedPath == null) return;
        
        collapseAll(root, 0, expandedLevel, selectedPath);
    }
    

    public void collapseAll(int expandedLevel) {
        if (root == null) return;
        collapseAll(root, 0, expandedLevel, null);
    }


    private void collapseAll(MapperNode node, int level,
            int expandedLevel, TreePath skipPath) {
        TreePath treePath = node.getTreePath();
        
        boolean skipCollapse = false;
        boolean skipCollapseGraph = false;
        
        if (skipPath != null) {
            skipCollapseGraph = treePath.equals(skipPath);
            skipCollapse = treePath.isDescendant(skipPath) && !skipCollapseGraph;
        }
        
        if (!node.isLeaf()) {
            if (level >= expandedLevel && node.isExpanded() && !skipCollapse) {
                setExpandedState(treePath, false);
            }

            if (node.isLoaded()) {
                for (int i = node.getChildCount() - 1; i >= 0; i--) {
                    collapseAll(node.getChild(i), level + 1, 
                            expandedLevel, skipPath);
                }
            }
        }
        
        Graph graph = node.getGraph();
        if (graph != null && !skipCollapseGraph && node.isGraphExpanded()) {
            setExpandedGraphState(treePath, false);
        }
    }

    
    public RightTreeCellRenderer getRightTreeCellRenderer() {
        return rightTree.getTreeCellRenderer();
    }
    
    public TreePath getRightTreePathForLink(Link link) {
        return getRightTreePathForLink(link, getRoot().getTreePath());
    }
    
    private TreePath getRightTreePathForLink(Link link, TreePath initialTreePath) {
        if (link == null || initialTreePath == null) return null; 
        
        MapperNode node = getNode(initialTreePath, true);
        
        if (link.getGraph() == node.getGraph()) {
            return initialTreePath;
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            MapperNode childNode = node.getChild(i);
            if (childNode.isLeaf()) {
                if (link.getGraph() == childNode.getGraph()) {
                    return childNode.getTreePath();
                }
            } else {
                if (getRightTreePathForLink(link, childNode.getTreePath()) != null) {
                    return getRightTreePathForLink(link, childNode.getTreePath());
                }
            }
        }
        return null;
    }

    
    public Link getPrevIngoingLink(Link link) {
        Set<Graph> graphs = canvas.getMapper().getRoot().getChildGraphs();

        List<Link> ingoingLinks = new ArrayList<Link>();
        for (Graph g : graphs) {
            ingoingLinks.addAll(g.getIngoingLinks());
        }
        if (!ingoingLinks.contains(link)) return null;

        TreePath leftPath;
        leftPath = ((TreeSourcePin) link.getSource()).getTreePath();
        int currentRow = getLeftTree().getParentsRowForPath(leftPath);
        Graph currentGraph = null;
        if (link.getTarget() instanceof Graph) currentGraph = (Graph) link.getTarget();
        if (link.getTarget() instanceof VertexItem) currentGraph = ((VertexItem) link.getTarget()).getVertex().getGraph();

        List<Link> linksCandidateRow = new ArrayList<Link>();
        List<Link> linksCandidateGraph = new ArrayList<Link>();
        // find links with rows == currentRow
        for (Link l : ingoingLinks) {
            //if (l != link) {
                leftPath = ((TreeSourcePin) l.getSource()).getTreePath();
                int row = getLeftTree().getParentsRowForPath(leftPath);
                if (row == currentRow) {
                    linksCandidateRow.add(l);
                }
          //  }
        }
//        if (linksCandidateRow.size() == 1) {
//            return linksCandidateRow.get(0);
//        }
        if (linksCandidateRow.size() > 1) {
            // find prevLinks in own Row with nearest Graph
            linksCandidateGraph.clear();
            Graph maxGraph = null;
            Graph graph = null;
            for (Link l : linksCandidateRow) {
                if (l.getTarget() instanceof Graph) graph = (Graph) l.getTarget();
                if (l.getTarget() instanceof VertexItem) graph = ((VertexItem) l.getTarget()).getVertex().getGraph();
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!            
                // find graph, which is the ups the current Graph
                if (compare(currentGraph, graph, getRoot().getTreePath()) >= 0) {
                    if (maxGraph == null) {
                        maxGraph = graph;
                        linksCandidateGraph.add(l);
                    } else {
                        if (compare(maxGraph, graph, getRoot().getTreePath()) < 0) {
                            linksCandidateGraph.clear();
                            maxGraph = graph;
                            linksCandidateGraph.add(l);
                        }
                        if (compare(maxGraph, graph, getRoot().getTreePath()) == 0) {
                            linksCandidateGraph.add(l);
                        }
                    }
                }
            }
            // find links in own Graph
            if (linksCandidateGraph.size() == 1) {
                return linksCandidateGraph.get(0);
            } 
            if (linksCandidateGraph.size() > 1) {
                // find prevLink in one graph
                if (linksCandidateGraph.contains(link)) {
                    Link prevLink = maxGraph.getPrevLink(link, linksCandidateGraph);
                    if (prevLink != null) return prevLink;
                    if (maxGraph.getPrevLink(link) == null) {
                        // find prevLinks in own Row with nearest Graph
                        linksCandidateGraph.clear();
                        maxGraph = null;
                        graph = null;
                        for (Link l : linksCandidateRow) {
                            if (l.getTarget() instanceof Graph) graph = (Graph) l.getTarget();
                            if (l.getTarget() instanceof VertexItem) graph = ((VertexItem) l.getTarget()).getVertex().getGraph();
                // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!            
                            // 
                            if (compare(currentGraph, graph, getRoot().getTreePath()) > 0) {
                                if (maxGraph == null) {
                                    maxGraph = graph;
                                    linksCandidateGraph.add(l);
                                } else {
                                    if (compare(maxGraph, graph, getRoot().getTreePath()) < 0) {
                                        linksCandidateGraph.clear();
                                        maxGraph = graph;
                                        linksCandidateGraph.add(l);
                                    }
                                    if (compare(maxGraph, graph, getRoot().getTreePath()) == 0) {
                                        linksCandidateGraph.add(l);
                                    }
                                }
                            }
                        }
                        if (maxGraph == null) return null;
                        prevLink = maxGraph.getPrevLink(null, linksCandidateGraph);
                        return prevLink;
                    }    
                } else {
                    Link prevLink = maxGraph.getPrevLink(null, linksCandidateGraph);
                    if (prevLink != null) return prevLink;
                }
            }
        } 
        //find links with nearest Row < currentRow
        linksCandidateRow.clear();        
        int maxRow = 0;
        for (Link l : ingoingLinks) {
            leftPath = ((TreeSourcePin) l.getSource()).getTreePath();
            int row = getLeftTree().getParentsRowForPath(leftPath);
            if (row < currentRow && row > maxRow) {
                linksCandidateRow.clear();
                maxRow = row;
                linksCandidateRow.add(l);
            }
            if (row < currentRow && row == maxRow) {
                linksCandidateRow.add(l);
            }
        }
        if (maxRow == 0) {
            return null;
        }
        if (linksCandidateRow.size() == 1) {
            return linksCandidateRow.get(0);
        }
        // find prevLinks in own Row with nearest Graph
        linksCandidateGraph.clear();
        Graph maxGraph = null;
        Graph graph = null;
        for (Link l : linksCandidateRow) {
            if (l.getTarget() instanceof Graph) graph = (Graph) l.getTarget();
            if (l.getTarget() instanceof VertexItem) graph = ((VertexItem) l.getTarget()).getVertex().getGraph();
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!            
            //
            if (compare(currentGraph, graph, getRoot().getTreePath()) >= 0) {
                if (maxGraph == null) {
                    maxGraph = graph;
                    linksCandidateGraph.add(l);
                } else {
                    if (compare(maxGraph, graph, getRoot().getTreePath()) < 0) {
                        linksCandidateGraph.clear();
                        maxGraph = graph;
                        linksCandidateGraph.add(l);
                    }
                    if (compare(maxGraph, graph, getRoot().getTreePath()) == 0) {
                        linksCandidateGraph.add(l);
                    }
                }
            }
        }
        if (linksCandidateGraph.size() == 1) {
            return linksCandidateGraph.get(0);
        } 
        if (linksCandidateGraph.size() > 1) {
            // find prevLink in one graph
            if (linksCandidateGraph.contains(link)) {
                Link prevLink = maxGraph.getPrevLink(link, linksCandidateGraph);
                if (prevLink != null) return prevLink;
                
                prevLink =maxGraph.getPrevLink(link);
                return prevLink;
            } else {
                return maxGraph.getPrevLink(null, linksCandidateGraph);
            }
        }
        return null;
    }
    
    public Link getNextOutgoingLink(Link link) {
        return null;
    }
        
    

    int getTextHeight() {
        return getFontMetrics(getFont()).getHeight();
    }

    int getTextWidth(String string) {
        return getFontMetrics(getFont()).stringWidth(string);
    }

    int getStepSize() {
        return Math.max((getTextHeight() + 2) / 2 + 1, 9);
    }

    MapperNode getNode(TreePath treePath, boolean load) {
        return getNode(treePath.getPath(), load);
    }

    MapperNode getNode(Object[] path, boolean load) {
        if (path == null) {
            return null;
        }
        if (path.length == 0) {
            return null;
        }
        if (root == null) {
            return null;
        }
        if (root.getValue() != path[0]) {
            throw new IllegalStateException();
        }

        MapperNode node = root;
        for (int i = 1; i < path.length; i++) {
            if (!load && !node.isLoaded()) {
                return null;
            }
            if (filteredModel.getIndexOfChild(node.getValue(), path[i]) == -1) {
                
            }
            node = node.getChild(filteredModel.getIndexOfChild(node.getValue(), path[i]));
        }

        return node;
    }

    MapperNode getClosestLoadedNode(Object[] path) {
        if (path == null) {
            return null;
        }
        if (path.length == 0) {
            return null;
        }
        if (root == null) {
            return null;
        }
        if (root.getValue() != path[0]) {
            throw new IllegalStateException();
        }

        MapperNode node = root;

        for (int i = 1; i < path.length; i++) {
            if (!node.isLoaded()) {
                break;
            }
            node = node.getChild(filteredModel.getIndexOfChild(node.getValue(), path[i]));
        }

        return node;
    }

    MapperNode getNodeAt(int y) {
        return (root == null) ? null : root.getNode(y);
    }

    void invalidateNodes() {
        if (validNodes) {
            rightTree.revalidate();

            canvas.revalidate();

            JComponent component = (JComponent) rightTree.getScrollPane().getRowHeader().getView();
            component.revalidate();

            preferredTreeSize = null;
            graphXRange = null;

            validNodes = false;
        }
    }

    void repaintNodes() {
        if (!repaintSceduled) {
            rightTree.repaint();
            canvas.repaint();

            JComponent component = (JComponent) rightTree.getScrollPane().getRowHeader().getView();
            component.repaint();

            repaintSceduled = true;
        }
    }

    void validateNodes() {
        if (!validNodes && root != null) {
            preferredTreeSize = root.getPreferredSize();
            graphXRange = root.getGraphXRange();

            root.setBounds(0, preferredTreeSize.height, 0);
            root.validate();

            validNodes = true;
        }
    }
    
    @Override
    public void doLayout() {
        validateNodes();
        super.doLayout();
    }
   
    @Override
    public Dimension getPreferredSize() {
        validateNodes();
        return super.getPreferredSize();
    }

    Dimension getPreferredTreeSize() {
        validateNodes();
        return (preferredTreeSize == null) ? null
                : new Dimension(preferredTreeSize.width, preferredTreeSize.height - 1);
    }

    XRange getGraphXRange() {
        validateNodes();
        return (graphXRange == null) ? null
                : new XRange(graphXRange);
    }

    int getLeftIndent() {
        return leftChildIndent;
    }

    int getRightIndent() {
        return rightChildIndent;
    }

    int getTotalIndent() {
        return leftChildIndent + rightChildIndent;
    }

    Icon getOpenIcon() {
        return openIcon;
    }

    Icon getClosedIcon() {
        return closedIcon;
    }

    Icon getLeafIcon() {
        return leafIcon;
    }

    Icon getExpandedIcon() {
        return expandedIcon;
    }

    Icon getCollapsedIcon() {
        return collapsedIcon;
    }

    Color getTreeLineColor() {
        return treeLineColor;
    }

    void updateChildGraphs(TreePath treePath) {
        MapperNode node = getClosestLoadedNode(treePath.getPath());

        while (node != null) {
            node.updateChildGraphs();
            node.invalidate();
            node.repaint();
            node = node.getParent();
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        treeLineColor = UIManager.getColor("Tree.hash");

        leftChildIndent = UIManager.getInt("Tree.rightChildIndent");
        rightChildIndent = UIManager.getInt("Tree.leftChildIndent");

        openIcon = UIManager.getIcon("Tree.openIcon");
        closedIcon = UIManager.getIcon("Tree.closedIcon");
        leafIcon = UIManager.getIcon("Tree.leafIcon");

        expandedIcon = UIManager.getIcon("Tree.expandedIcon");
        collapsedIcon = UIManager.getIcon("Tree.collapsedIcon");
    }

    private int compare(Graph graph1, Graph graph2, TreePath treePath) {
        if (graph1 == graph2) return 0;
        
        MapperNode node = getNode(treePath, true);
        if (node.getGraph() == graph1) return -1;
        if (node.getGraph() == graph2) return 1;
        
        for (int i = 0; i < node.getChildCount(); i++) {
            MapperNode nodeChild = node.getChild(i);
            if (nodeChild.isLeaf()) {
                if (nodeChild.getGraph() == graph1) return -1;
                if (nodeChild.getGraph() == graph2) return 1;
            } else {
                int r = compare(graph1, graph2, nodeChild.getTreePath());
                if (r != 0) return r;
            }
        }
        return 0;
    }

    private void setSelected() {
        MapperNode root = getRoot();

        if (root != null && root.getChildCount() == 1 &&
                root.getChild(0).getChildCount() < 1) {
            getSelectionModel().setSelected(root.getChild(0).getTreePath());
        }
    }

    private class TreeModelListenerImpl implements TreeModelListener {

        public void treeNodesChanged(TreeModelEvent e) {
            TreePath treePath = e.getTreePath();
            int[] indeces = e.getChildIndices();

            if (indeces == null || treePath == null) {
                root.updateChildGraphs();
                root.updateNode();
                root.invalidate();
                root.repaint();
            } else {
                updateChildGraphs(treePath);

                MapperNode node = getNode(treePath, false);
                if (node != null) {
                    for (int i : indeces) {
                        MapperNode child = node.getChild(i);
                        child.updateNode();
                        child.invalidate();
                        child.repaint();
                    }
                }
            }
        }

        public void treeNodesInserted(TreeModelEvent e) {
            updateChildGraphs(e.getTreePath());
            MapperNode node = getNode(e.getPath(), false);
            if (node != null) {
//                node.insertChildren(e);
                node.updateNode();
                node.updateChildren();
                node.invalidate();
                node.repaint();
            }
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            updateChildGraphs(e.getTreePath());
            MapperNode node = getNode(e.getPath(), false);
            if (node != null) {
                node.updateNode();
                node.updateChildren();
                node.invalidate();
                node.repaint();
//                node.removeChildren(e);
            }
        }

        public void treeStructureChanged(TreeModelEvent e) {
            MapperModel mapperModel = model;
            setModel(null);
            setModel(mapperModel);
        }
    }

    private class ScrollPaneYSyncronizer implements ChangeListener {

        private JViewport viewport1;
        private JViewport viewport2;

        public ScrollPaneYSyncronizer(
                JScrollPane scrollPane1,
                JScrollPane scrollPane2) {
            viewport1 = scrollPane1.getViewport();
            viewport2 = scrollPane2.getViewport();

            viewport1.addChangeListener(this);
            viewport2.addChangeListener(this);
        }

        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == viewport1) {
                Point position = viewport2.getViewPosition();
                position.y = viewport1.getViewPosition().y;
                viewport2.setViewPosition(position);
            } else {
                Point position = viewport1.getViewPosition();
                position.y = viewport2.getViewPosition().y;
                viewport1.setViewPosition(position);
            }
        }
    }
    public static final String MODEL_PROPERTY = "mapper-model-property";
    public static final String BUFFER_PROPERTY = "mapper-buffer-property";
    public static final Stroke DASHED_ROW_SEPARATOR_STROKE = new BasicStroke(1,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            1, new float[]{4, 2}, 0);
    public static final Stroke DASHED_STROKE = new BasicStroke(1,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            1, new float[]{4, 4}, 0);
    public static final Color CANVAS_BACKGROUND_COLOR = new Color(0xFCFAF5);
    public static final Color CANVAS_GRID_COLOR = new Color(0xC0C0C0);
    public static final Color ROW_SEPARATOR_COLOR = new Color(0xBBD3E9); //new Color(0x99B7D3);
    public static final Color SELECTED_BACKGROUND_COLOR_TOP = new Color(0xF0F9FF);
    public static final Color SELECTED_BACKGROUND_COLOR_BOTTOM = new Color(0xD0E0F0);
    public static final Color RIGHT_TREE_HEADER_COLOR = new Color(0x999999);
    public static final VerticalGradient SELECTED_BACKGROUND_IN_FOCUS = new VerticalGradient(
            Mapper.SELECTED_BACKGROUND_COLOR_TOP,
            Mapper.SELECTED_BACKGROUND_COLOR_BOTTOM);
    public static final VerticalGradient SELECTED_BACKGROUND_NOT_IN_FOCUS = new VerticalGradient(
            Utils.gray(Mapper.SELECTED_BACKGROUND_COLOR_TOP, 75),
            Utils.gray(Mapper.SELECTED_BACKGROUND_COLOR_BOTTOM, 75));
    
    private static final Comparator<Graph> GRAPH_COMPARATOR = new Comparator<Graph>() {
        public int compare(Graph graph1, Graph graph2) {
            return 0;
        }
    };
}
