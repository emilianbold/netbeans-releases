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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphListener;
import org.netbeans.modules.soa.mappercore.graphics.XRange;
import org.netbeans.modules.soa.mappercore.utils.MapperTreePath;
import org.netbeans.modules.soa.mappercore.utils.Utils;


class MapperNode implements GraphListener {

    private Mapper mapper;
    
    private boolean collapsed;
    private boolean leaf;

    private Object value;

    private MapperNode parent;
    private List<MapperNode> children;

    private int y;
    private int indent;
    private int height;
    private int contentHeight;
    private int labelWidth;
    private int labelHeight;

    private String text;
    private Icon icon;
    
    private boolean valid = false;

    private Graph graph;
    private boolean graphCollapsed = false;
    private Set<Graph> childGraphs;
    
    private TreePath treePath;
    
    public MapperNode(Mapper mapper, MapperNode parent, Object value) {
        this.value = value;
        this.mapper = mapper;
        this.parent = parent;
        this.collapsed = (parent != null);
        
        if (parent == null) {
            treePath = new TreePath(value);
        } else {
            treePath = new MapperTreePath(parent.getTreePath(), value);
        }
        
        updateNode();
        updateChildGraphs();
    }

    
    private Mapper getMapper() { return mapper; }
    private RightTree getRightTree() { return mapper.getRightTree(); }

    private MapperModel getModel() { return mapper.getFilteredModel(); }

    MapperNode getParent() { return parent; }
    
    Object getValue() { return value; }
    
    boolean isDnDSelected() { return Utils.equal(mapper.getSelectedDndPath(), treePath); }
    boolean isSelected() { return Utils.equal(mapper.getSelected(), treePath); }
    boolean isLeaf() { return leaf; }
    boolean isCollapsed() { return collapsed; }
    boolean isExpanded() { return !collapsed; }

    boolean isGraphCollapsed() { return graphCollapsed; }
    boolean isGraphExpanded() { return !graphCollapsed; }
    
    int getY() { return y; }
    int getHeight() { return height; }
    int getIndent() { return indent; }
    int getContentHeight() { return contentHeight; }
    int getContentCenterY() { return (contentHeight - 1) / 2; }

    boolean isValid() { return valid; }
    boolean isLoaded() { return leaf || children != null; }
    
    int getLabelWidth() { return labelWidth; }
    int getLabelHeight() { return labelHeight; }

    Graph getGraph() { return graph; }
    
    Set<Graph> getChildGraphs() { 
        return getChildGraphs(null); 
    }
    
    Set<Graph> getChildGraphs(Set<Graph> result) {
        if (result == null) {
            result = new HashSet<Graph>();
        }
        
        if (childGraphs != null) {
            result.addAll(childGraphs);
        }
        
        return result;
    }
    
    MapperNode getNode(int y) {
        if (y < 0 || y >= height) return null;
        
        if (isLeaf()) {
            return this;
        } else {
            if (y >= contentHeight) {
                if (!isCollapsed()) {
                    for (int i = getChildCount() - 1; i >= 0; i--) {
                        MapperNode child = getChild(i);
                        MapperNode result = child.getNode(y - child.getY());
                        if (result != null) return result;
                    }
                }
            }
        }
        
        return this;
    }
    
    
//    public int getGraphY() {
//        int y = ;
//        for (MapperNode n = this; n != null; n = n.getParent()) {
//            y += n.getY();
//        }
//    }
    
    
    TreePath getTreePath() {
        return treePath;
    }
    
    
    int yToNode(int viewY) {
        for (MapperNode n = this; n != null; n = n.getParent()) {
            viewY -= n.getY();
        }
        return viewY;
    }
    
    
    int yToView(int nodeY) {
        for (MapperNode n = this; n != null; n = n.getParent()) {
            nodeY += n.getY();
        }
        return nodeY;
    }
    

    void setCollapsed(boolean collapsed) {
        if (this.collapsed != collapsed) {
            this.collapsed = collapsed;
            updateChildGraphs();
            invalidate();
            //repaint();
            // If link go from LeftTree to RightTree It can change color
            getMapper().repaint();
        }
    }
    
    
    void setExpanded(boolean expanded) {
        setCollapsed(!expanded);
    }
    
    
    void setGraphCollapsed(boolean graphCollapsed) {
        if (this.graphCollapsed != graphCollapsed) {
            this.graphCollapsed = graphCollapsed;
            invalidate();
            //repaint();
            // If link go from LeftTree to RightTree It can change color
            getMapper().repaint();
        }
    }
    
    
    void setGraphExpanded(boolean graphExpanded) {
        setGraphCollapsed(!graphExpanded);
    }
    
    
    int getChildCount() {
        loadChildren();
        return (leaf) ? 0 : children.size();
    }


    MapperNode getChild(int i) {
        loadChildren();
        return children.get(i);
    }
    
    
    int getChildIndex(MapperNode child) {
        if (!isLoaded()) return -1;
        return children.indexOf(child);
    }


    private void loadChildren() {
        if (isLoaded()) return;
        updateChildren();
    }
    
    
    void updateNode() {
        MapperModel model = getModel();
        
        boolean oldLeaf = leaf;
        leaf = model.isLeaf(value);
        
        if (oldLeaf != leaf) {
            removeAllChildren();
        }
        
        Graph oldGraph = graph;
        graph = model.getGraph(getTreePath());
        
        if (oldGraph != graph) {
            if (oldGraph != null) oldGraph.removeGraphListener(this);
            if (graph != null) graph.addGraphListener(this);
        }
    }
    
    
    void updateChildGraphs() {
        MapperModel model = getModel();
        
        Set<Graph> oldChildGraphs = new HashSet<Graph>();
        Set<Graph> newChildGraphs = new HashSet<Graph>();

        if (childGraphs != null) {
            oldChildGraphs.addAll(childGraphs);
        }
        
        if ((!leaf && collapsed) || (parent == null && !leaf)) {
            newChildGraphs = Utils.findGraphs(model, getTreePath(), 
                    newChildGraphs);
        } 
        
        if (newChildGraphs != null && !newChildGraphs.isEmpty()) {
            if (childGraphs != null) {
                childGraphs.clear();
            } else {
                childGraphs = new HashSet<Graph>();
            }
            childGraphs.addAll(newChildGraphs);
        } else {
            childGraphs = null;
        }
        
        Set<Graph> common = new HashSet<Graph>(oldChildGraphs);
        common.retainAll(newChildGraphs);
        
        oldChildGraphs.removeAll(common);
        newChildGraphs.removeAll(common);
        
        for (Graph childGraph : oldChildGraphs) {
            childGraph.removeGraphListener(this);
        }
        
        for (Graph childGraph : newChildGraphs) {
            childGraph.addGraphListener(this);
        }
        
        if (!oldChildGraphs.isEmpty() || !newChildGraphs.isEmpty()) {
            invalidate();
            repaint();
        }
    }
    

    private void removeAllChildren() {
        if (children != null) {
            for (MapperNode child : children) {
                child.removeNode();
            }
        }
    }
    
    
    private void removeNode() {
        if (graph != null) {
            graph.removeGraphListener(this);
            graph = null;
        }
        
        if (childGraphs != null) {
            for (Graph childGraph : childGraphs) {
                childGraph.removeGraphListener(this);
            }
            childGraphs.clear();
            childGraphs = null;
        }
        
        removeAllChildren();
    }


    void updateChildren() {
        MapperModel model = mapper.getFilteredModel();
        
        List<MapperNode> oldChildren = Collections.emptyList();
        if (children != null) oldChildren = children;

        int count = model.getChildCount(value);

        List<MapperNode> newChildren = new ArrayList<MapperNode>(count);

        for (int i = 0; i < count; i++) {
            Object childValue = model.getChild(value, i);
            MapperNode childNode = null;

            for (int j = 0; j < oldChildren.size(); j++) {
                if (oldChildren.get(j).getValue() == childValue) {
                    childNode = oldChildren.remove(j);
                    break;
                }
            }

            if (childNode == null) childNode = new MapperNode(mapper, this, childValue);
            newChildren.add(i, childNode);
        }

        this.children = newChildren;
    }


    void insertChildren(TreeModelEvent e) {
        if (isLoaded()) {
            MapperModel model = getModel();
            
            int[] indeces = e.getChildIndices();
            Arrays.sort(indeces);

            for (int index : indeces) {
                if (children == null) {
                    children = new ArrayList<MapperNode>();
                }
                
                children.add(index, new MapperNode(mapper, this, 
                        model.getChild(value, index)));
            }
        }
    }


    void removeChildren(TreeModelEvent e) {
        if (isLoaded()) {
            int[] indeces = e.getChildIndices();
            Arrays.sort(indeces);

            for (int index : indeces) {
                children.remove(index);
            }
        }
    }


    void validate() {
        if (valid) return;

        layout();

        if (isLoaded()) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                getChild(i).validate();
            }
        }

        valid = true;
    }


    void invalidate() {
        if (valid) {
            valid = false;
            MapperNode parent = getParent();
            
            if (parent != null) {
                parent.invalidate();
            } else {
                mapper.invalidateNodes();
            }
        }
    }
    
    
    void invalidateTree() {
        invalidate();
        if (isLoaded()) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                getChild(i).invalidate();
            }
        }
    }
    
    
    void repaint() {
        mapper.repaintNodes();
    }


    void layout() {
        int height = getHeight();
        int indent = getIndent();
        int childIndent = indent + mapper.getTotalIndent();

        Dimension labelSize = getLabelSize();

        if (parent == null) {
            this.contentHeight = 0;
        } else {
            this.contentHeight = Math.max(labelSize.height + 4, getGraphHeight()) + 1;
            this.labelHeight = labelSize.height;
            this.labelWidth = labelSize.width;
        }

        int y = contentHeight;

        if (isLeaf()) {
            // leaf
        } else if (isExpanded()) {
            // expanded
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                MapperNode child = getChild(i);
                int childHeight = child.getPreferredHeight();

                child.setBounds(y, childHeight, childIndent);

                y += childHeight;
            }
        } else { 
            // collapsed
        }
    }


    int getPreferredHeight() {
        return getPreferredSize().height;
    }


    Dimension getPreferredSize() {
        Dimension labelSize = getLabelSize();

        int w = labelSize.width;
        int h = (getParent() == null) ? 0 
                : Math.max(getGraphHeight(), labelSize.height + 4) + 1;

        int indent = mapper.getTotalIndent();

        if (isLeaf()) {
            // leaf
        } else if (isExpanded()) {
            // expanded
            for (int i = getChildCount() - 1; i >= 0; i--) {
                Dimension size = getChild(i).getPreferredSize();
                w = Math.max(w, size.width + indent);
                h += size.height;
            }
        } else { 
            // collapsed
            if (hasNonEmptyChildGraphs()) {
                Dimension childrenLabelSize = getChildrenLabelSize();
                h += childrenLabelSize.height + 3;
                w = Math.max(w, childrenLabelSize.width + indent);
            }
        }

        return new Dimension(w, h);
    }
    
    
    private boolean hasNonEmptyChildGraphs() {
        if (childGraphs != null) {
            for (Graph childGraph : childGraphs) {
                if (!childGraph.isEmpty()) return true;
            }
        }
        return false;
    }
    
    
    XRange getGraphXRange() {
        int x1 = Integer.MAX_VALUE;
        int x2 = Integer.MIN_VALUE;
        
        if (graph != null && isGraphExpanded()) {
            x1 = graph.getX();
            x2 = x1 + graph.getWidth();
        }
        
        if (!isLeaf()) {
            if (isExpanded()) {
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    XRange graphXRange = getChild(i).getGraphXRange();
                    if (graphXRange != null) {
                        x1 = Math.min(x1, graphXRange.x);
                        x2 = Math.max(x2, graphXRange.x + graphXRange.width);
                    }
                }
            }
        }
        
        return (x1 <= x2) ? new XRange(x1, x2 - x1) : null;
    }
    
    boolean isVisibleGraph() {
        if (graph == null) return false;
        if (graph.isEmpty()) return false;
        if (!isVisible()) return false;
        return isGraphExpanded();
    }
    
  //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  
    private boolean isVisible() {
        if (this == getMapper().getRoot()) return true;
        if (getParent().isCollapsed()) return false;
        return getParent().isVisible();
    }
    
    int getGraphHeight() {
        int step = mapper.getStepSize();
        int size = step - 1;
        int topInset = size / 2;
        int bottomInset = size - topInset;

        return (isVisibleGraph() && !getGraph().isEmptyOrOneLink()) 
                ? Math.max(2, graph.getHeight()) * step + size + 2
                : 0;
    }


    Dimension getLabelSize() {
        Dimension d = getRightTree().getCellRendererComponent(this).getPreferredSize();
        return new Dimension(Math.max(16, d.width), Math.max(16, d.height));
    }
    
    
    Dimension getChildrenLabelSize() {
        return mapper.getRightTree().getChildrenLabel().getPreferredSize();
    }


    void setBounds(int y, int height, int indent) {
        this.y = y;

        if (this.height != height || this.indent != indent) {
            this.indent = indent;
            this.height = height;
            invalidate();
            repaint();
        }
    }
    
    public boolean mustDrawLine() {
        if (getParent() == null) {
            // Root node is never visible. 
            // We should not paint line for invisible node
            return false;
        }
        
        MapperNode nextNode = getNextVisibleNode(this);
        // graph or nextGraph is not Empty or One Link
        if (this.getGraph() != null && !this.getGraph().isEmptyOrOneLink() ||
                    (nextNode != null && nextNode.getGraph() != null &&
                    !nextNode.getGraph().isEmptyOrOneLink() )) 
        {
            return true; 
        }
        // node or nextNode is Selected or dndSelected
        if (this.isSelected() || (nextNode != null &&
                    nextNode.isSelected()) ||
                    (this.isDnDSelected() || (nextNode != null &&
                    nextNode.isDnDSelected()))) 
        {
            return true;
        }
        // node or nextNode have [...]
        if (isCollapsed() && getHeight() != getContentHeight() ||
                nextNode != null && nextNode.isCollapsed() && 
                nextNode.getHeight() != nextNode.getContentHeight())
        {
            return true;
        }
        return false;
    }
    
    public boolean mustDrawDottedLine() {
        return  isCollapsed() && getHeight() != getContentHeight(); 
    }
    
    public MapperNode getNextVisibleNode() {
        return  getNextVisibleNode(this);
    }
    
    public MapperNode getPrevVisibleNode() {
        return getPrevVisibleNode(this); 
    }
    
    private MapperNode getPrevVisibleNode(MapperNode node) {
        if (node == mapper.getRoot()) {return null;}
        
        if (node == this) {
            int index = node.getParent().getChildIndex(node);
            if (index > 0) {
                if (node.getParent().getChild(index - 1).isCollapsed() 
                        || node.getParent().getChild(index - 1).isLeaf()) 
                {
                    return  node.getParent().getChild(index - 1);
                } else {
                    return getPrevVisibleNode(node.getParent().getChild(index - 1));
                }
            }  else {
                return node.getParent();
            }    
        } else {
            if (node.getChild(node.getChildCount() - 1).isLeaf() 
                    || node.getChild(node.getChildCount() - 1).isCollapsed()) 
            {
                return node.getChild(node.getChildCount() - 1);
            } else {
                return getPrevVisibleNode(node.getChild(node.getChildCount() - 1));
            }
        }
    }
    
    private MapperNode getNextVisibleNode(MapperNode node) {
        MapperNode root = mapper.getRoot();
        if (node == root && (node != this || root.isLeaf() 
                || root.isCollapsed() || root.getChildCount() < 1)) 
        {
            return null;
        }
        MapperNode result = null;
        if (node.isCollapsed() || node.isLeaf()) {
            int index = node.getParent().getChildIndex(node);
            if (index + 1 < node.getParent().getChildCount()) {
                result = node.getParent().getChild(index + 1);
            } else {
                result = getNextVisibleNode(node.getParent());
            }
        } else if (node == this && node.getChildCount() > 0) {
            result = node.getChild(0);
        } else {
            int index = node.getParent().getChildIndex(node);
            if (index + 1 < node.getParent().getChildCount()) {
                result = node.getParent().getChild(index + 1);
            } else {
                result = getNextVisibleNode(node.getParent());
            }
        }
        return result;
    }
    
    public void graphBoundsChanged(Graph graph) {
        invalidate();
        repaint();
    }
    

    public void graphLinksChanged(Graph graph) {
        getRightTree().getLeftTree().repaint();
        repaint();
    }

    public void graphContentChanged(Graph graph) {
        repaint();
    }
}

