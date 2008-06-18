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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.CellRendererPane;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionEvent;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionListener;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.graphics.Grid;
import org.netbeans.modules.soa.mappercore.graphics.VerticalGradient;
import org.netbeans.modules.soa.mappercore.graphics.XRange;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Operation;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.mappercore.utils.ScrollPaneWrapper;
import org.netbeans.modules.soa.mappercore.utils.Utils;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author anjeleevich
 * @author AlexanderPermyakov
 */
public class Canvas extends MapperPanel implements VertexCanvas,
        FocusListener, MapperSelectionListener,
        Autoscroll 
{
    private CanvasEventHandler eventHandler;
    private double graphViewPositionX = 0;
    private CanvasScrollPane scrollPane;
    private ScrollPaneWrapper scrollPaneWrapper;
    private Grid grid = new Grid();
    private JLabel textRenderer;
    private CellRendererPane cellRendererPane;
    
    private VertexItemRenderer vertexItemRenderer 
            = new DefaultVertexItemRenderer();

    private InplaceEditor inplaceEditor;
    private GraphSubset bufferCopyPaste;
    private boolean printMode = false;
        
    public Canvas(Mapper mapper) {
        super(mapper);

        // vlv: print
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        putClientProperty("print.weight", new Integer(1)); // NOI18N
        
        setBackground(Mapper.CANVAS_BACKGROUND_COLOR);

        scrollPane = new CanvasScrollPane();
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        scrollPaneWrapper = new ScrollPaneWrapper(scrollPane);

        addFocusListener(this);

        textRenderer = new JLabel();

        cellRendererPane = new CellRendererPane();
        cellRendererPane.add(textRenderer);

        add(cellRendererPane);
        eventHandler = new CanvasEventHandler(this);
        inplaceEditor = new InplaceEditor(this);
        getSelectionModel().addSelectionListener(this);
                   
        registerAction(new StartInplaceEditor(this));
        
        ToolTipManager.sharedInstance().registerComponent(this);
        
        registerAction(new MoveRightCanvasAction(this));
        registerAction(new MoveLeftCanvasAction(this));
        registerAction(new MoveUpCanvasAction(this));
        registerAction(new MoveDownCanvasAction(this));
        registerAction(new LinkConnectAction(this));
        registerAction(new CopyMapperAction(this));
        registerAction(new PasteMapperAction(this));
        registerAction(new CutMapperAction(this));
        registerAction(new DeleteMapperAction(this));
        registerAction(new ShowPopapMenuAction(this));
    
        getAccessibleContext().setAccessibleName(NbBundle
                .getMessage(Canvas.class, "ACSN_Canvas")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle
                .getMessage(Canvas.class, "ACSD_Canvas")); // NOI18N
    }
    
    @Override
    public String getToolTipText(MouseEvent event) {
        CanvasSearchResult searchResult = find(event.getX(), event.getY());
        
        if (searchResult == null) return null;
        if (searchResult.getPinItem() != null) return null;
        
        GraphItem graphItem = searchResult.getGraphItem();
        
        if (graphItem instanceof Vertex) {
            return ((Vertex) graphItem).getName();
        }
        
        if (graphItem instanceof VertexItem) {
            String str = ((VertexItem) graphItem).getText();
            if (str != null && str.length() > 0) {
                return str;
            }
        }
        
        if (graphItem instanceof Link) {
            return "Link";
        }
        return null;
    }
     
    public void registerAction(MapperKeyboardAction action) {
        InputMap iMap = getInputMap();
        ActionMap aMap = getActionMap();
        
        String actionKey = action.getActionKey();
        aMap.put(actionKey, action);
                
        KeyStroke[] shortcuts = action.getShortcuts();
        if (shortcuts != null) {
            for (KeyStroke s : shortcuts) {
                iMap.put(s, actionKey);
            }
        }
    }

    public CanvasRendererContext getDefaultRendererContext() {
        return new DefaultCanvasRendererContext(getMapper());
    }

    public CanvasRendererContext getRendererContext() {
        LinkTool linkTool = getLinkTool();
        return (linkTool.isActive())
                ? linkTool.getCanvasRendererContext()
                : getDefaultRendererContext();
    }
    
    public GraphSubset getBufferCopyPaste() {
        return bufferCopyPaste;
    }
    
    JScrollPane getScrollPane() {
        return scrollPane;
    }

    JComponent getView() {
        return scrollPaneWrapper;
    }

    private int getGraphViewPositionX() {
        return getGraphViewPositionX(getStep());
    }

    private int getGraphViewPositionX(int step) {
        return (int) Math.round(graphViewPositionX * step);
    }

    private void setGraphViewPositionX(int graphViewPositionX) {
        setGraphViewPositionX(graphViewPositionX, getStep());
    }

    private void setGraphViewPositionX(int graphViewPositionX, int step) {
        this.graphViewPositionX = (double) graphViewPositionX / step;
    }

    JViewport getViewport() {
        return scrollPane.getViewport();
    }
    
    @Override
    public void print(Graphics g) {
        LeftTree leftTree = getLeftTree();

        leftTree.setPrintMode(true);
        printMode = true;
        super.print(g);
        printMode = false;
        leftTree.setPrintMode(false);
    }
    @Override
    protected void printComponent(Graphics g) {
       // super.paintComponent(g);
        Mapper mapper = getMapper();

        //mapper.resetRepaintSceduled();

        MapperNode root = getRoot();

        if (root != null) {
            int step = getStep();
            int graphX0 = toGraph(0);

            Graphics2D g2 = (Graphics2D) g.create();
            
            CanvasRendererContext rendererContext = new DefaultCanvasRendererPrintContext(mapper);
            paintNodeBackground(root, 0, g2, rendererContext);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            paintNodeLinks(root, false, 0, g2, rendererContext);
            paintNodeVerteces(root, false, 0, g2, rendererContext);
            paintNodeLinks(root, true, 0, g2, rendererContext);
            paintNodeVerteces(root, true, 0, g2, rendererContext);

            g2.dispose();
        }
    }
    
    @Override
    public void setLocation(int x, int y) {
        int step = getStep();

        setGraphViewPositionX(getGraphViewPositionX(step) - x + getX(), step);

        super.setLocation(x, y);
    }
    
    
    public void setVertexItemRenderer(VertexItemRenderer renderer) {
        if (renderer == null) {
            renderer = new DefaultVertexItemRenderer();
        }
        
        if (renderer != vertexItemRenderer) {
            this.vertexItemRenderer = renderer;
        }
    }
    
    public void setBufferCopyPaste(GraphSubset graphSubset) {
        GraphSubset oldBuffer = bufferCopyPaste;
        bufferCopyPaste = new GraphSubset(graphSubset);
        firePropertyChange(Mapper.BUFFER_PROPERTY, oldBuffer, bufferCopyPaste);
    }
    
    
    public VertexItemRenderer getVertexItemRenderer() {
        return vertexItemRenderer;
    }
    
    
    public void paintVertexItemText(TreePath treePath, VertexItem vertexItem, 
            Graphics2D g2, int x, int y, int w, int h) 
    {
        Component c = vertexItemRenderer.getVertexItemRendererComponent(
                getMapper(), treePath, vertexItem);
        cellRendererPane.add(c);
        
        g2.translate(x, y);
        c.setBounds(0, 0, w, h);
        c.paint(g2);
        g2.translate(-x, -y);
        
        cellRendererPane.remove(c);
    }
    
    
    
    public void setVertexItemEditor(Class valueType, VertexItemEditor editor) {
        inplaceEditor.setVertexItemEditor(valueType, editor);
    }
    
    public void setCustomVertexItemEditor(Class valueType, 
            CustomVertexItemEditor editor)
    {
        inplaceEditor.setCustomVertexItemEditor(valueType, editor);
    }
    
    public VertexItemEditor getVertexItemEditor(Class valueType) {
        return inplaceEditor.getVertexItemEditor(valueType);
    }
    
    public CustomVertexItemEditor getCustomVertexItemEditor(Class valueType) {
        return inplaceEditor.getCustomVertexItemEditor(valueType);
    }
    
    public void startEdit(TreePath treePath, VertexItem vertexItem) {
        inplaceEditor.startEdit(treePath, vertexItem);
    }
    

    public int toGraph(int canvasX) {
        Rectangle viewRect = scrollPane.getViewport().getViewRect();
        return canvasX + getGraphViewPositionX() - viewRect.x - viewRect.width;
    }

    public int toCanvas(int graphX) {
        Rectangle viewRect = scrollPane.getViewport().getViewRect();
        return graphX - getGraphViewPositionX() + viewRect.x + viewRect.width;
    }
    
    public int toGraphY(int canvasY) {
        MapperNode node = getNodeAt(canvasY);
                
        int graphY = node.getY();
        while (node.getParent() != null) {
            node = node.getParent();
            graphY = graphY + node.getY();
        }
        return graphY;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Mapper mapper = getMapper();

        mapper.resetRepaintSceduled();

        MapperNode root = getRoot();

        if (root != null) {
            int step = getStep();
            int graphX0 = toGraph(0);

            Graphics2D g2 = (Graphics2D) g.create();

            CanvasRendererContext rendererContext = getRendererContext();
            paintNodeBackground(root, 0, g2, rendererContext);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            paintNodeLinks(root, false, 0, g2, rendererContext);
            paintNodeVerteces(root, false, 0, g2, rendererContext);
            paintNodeLinks(root, true, 0, g2, rendererContext);
            paintNodeVerteces(root, true, 0, g2, rendererContext);

            g2.dispose();
        }

        getLinkTool().paintCanvas(this, g);
    }

    private void paintNodeBackground(MapperNode node, int nodeY,
            Graphics2D g2, CanvasRendererContext rendererContext) {
        int contentHeight = node.getContentHeight();
        int height = node.getHeight();
        boolean leaf = node.isLeaf();
        boolean expanded = node.isExpanded();

        int step = rendererContext.getStep();
        int minX = rendererContext.getCanvasVisibleMinX();
        int maxX = rendererContext.getCanvasVisibleMaxX();
        int graphX = rendererContext.getGraphX();

        if (rendererContext.isSelected(node.getTreePath())) {
            VerticalGradient gradient = (hasFocus())
                    ? Mapper.SELECTED_BACKGROUND_IN_FOCUS
                    : Mapper.SELECTED_BACKGROUND_NOT_IN_FOCUS;
            gradient.paintGradient(this, g2, minX, nodeY, maxX - minX,
                    ((leaf) ? height : ((expanded) ? contentHeight : height)) - 1);
        }

        if (leaf) {
            // leaf
            if (node.mustDrawLine()) {
                int lineY = nodeY + contentHeight - 1;

                g2.setPaint(Mapper.ROW_SEPARATOR_COLOR);
                g2.drawLine(minX, lineY, maxX - 1, lineY);
            }    
        } else if (expanded) {
            // expanded
            if (node.mustDrawLine()) {
                int lineY = nodeY + contentHeight - 1;

                g2.setPaint(Mapper.ROW_SEPARATOR_COLOR);
                g2.drawLine(minX, lineY, maxX - 1, lineY);
            }   

            int count = node.getChildCount();
            for (int i = 0; i < count; i++) {
                MapperNode child = node.getChild(i);
                paintNodeBackground(child, nodeY + child.getY(), g2,
                        rendererContext);
            }
        } else {
            // collapsed
            if (node.mustDrawLine()) {
                int lineY1 = nodeY + contentHeight - 1;
                int lineY2 = nodeY + node.getHeight() - 1;

                if (node.mustDrawDottedLine()) 
                {
                    Stroke oldStroke = g2.getStroke();
                    g2.setPaint(Mapper.ROW_SEPARATOR_COLOR);
                    g2.setStroke(Mapper.DASHED_ROW_SEPARATOR_STROKE);
                    g2.drawLine(minX, lineY1, maxX - 1, lineY1);
                    g2.setStroke(oldStroke);
                }
            
                g2.setPaint(Mapper.ROW_SEPARATOR_COLOR);
                g2.drawLine(minX, lineY2, maxX - 1, lineY2);
            }
        }

        if (node.isVisibleGraph() && !node.getGraph().isEmptyOrOneLink()) {
            int size = step - 1;
            int topInset = size / 2;
            int bottomInset = size - topInset;

            g2.translate(graphX, 0);
            grid.paintGrid(this, g2, -graphX, nodeY + topInset + 1, getWidth(),
                    contentHeight - size - 2, step,
                    !node.isSelected() && !printMode);
            g2.translate(-graphX, 0);
        }
    }

    void paintNodeLinks(MapperNode node, boolean selectedFilter,
            int nodeY, Graphics2D g2, CanvasRendererContext rendererContext) {
        boolean nodeIsSelected = rendererContext.isSelected(node.getTreePath());
        int step = rendererContext.getStep();

        Mapper mapper = getMapper();

        final boolean leaf = node.isLeaf();
        final boolean expanded = node.isExpanded();

        Graph graph = node.getGraph();

        int graphY = nodeY + (step - 1) / 2 + 1;

        int minX = rendererContext.getCanvasVisibleMinX();
        int maxX = rendererContext.getCanvasVisibleMaxX();
        int centerX = rendererContext.getCanvasVisibleCenterX();

        Color linkColor;
        Color linkTColor;

        if (nodeIsSelected) {
            linkColor = MapperStyle.LINK_COLOR_SELECTED_NODE;
            linkTColor = MapperStyle.LINK_TCOLOR_SELECTED_NODE;
        } else {
            linkColor = MapperStyle.LINK_COLOR_UNSELECTED_NODE;
            linkTColor = MapperStyle.LINK_TCOLOR_UNSELECTED_NODE;
        }

        if (graph != null && nodeIsSelected == selectedFilter) {
            if (node.isGraphExpanded()) {
                graph.paintLinks(g2, node.getTreePath(), rendererContext,
                        graphY);
            } else {
                int y2 = nodeY + node.getContentCenterY();

                if (graph.hasOutgoingLinks()) {
                    Paint paint = (graph.hasConnectedOutgoingLinks())
                            ? linkColor
                            : new GradientPaint(
                            centerX, 0, linkTColor,
                            maxX, 0, linkColor);

                    Link.paintLine(g2, paint, null, centerX, y2, maxX, y2, step,
                            minX, maxX);
                }

                if (graph.hasIngoingLinks()) {
                    List<Link> ingoingEdges = graph.getIngoingLinks();
                    Map<Integer, Boolean> startYs = new HashMap<Integer, Boolean>();

                    LeftTree leftTree = mapper.getLeftTree();

                    for (Link edge : ingoingEdges) {
                        TreePath treePath = ((TreeSourcePin) edge.getSource())
                                .getTreePath();

                        int y = yFromMapper(leftTree.yToMapper(leftTree
                                .getCenterY(treePath)));

                        Boolean oldValue = startYs.get(y);
                        if (oldValue == null) {
                            startYs.put(y, graph.isConnectedIngoingLink(edge));
                        } else if (!oldValue.booleanValue() && graph.isConnectedIngoingLink(edge)) {
                            startYs.put(y, true);
                        }
                    }

                    for (Map.Entry<Integer, Boolean> pair : startYs.entrySet()) {
                        Paint paint = (pair.getValue()) ? linkColor
                                : new GradientPaint(
                                minX, 0, linkColor,
                                centerX, 0, linkTColor);

                        Link.paintLine(g2, paint, null, minX, pair.getKey(),
                                centerX, y2, step, minX, maxX);
                    }
                }
            }
        }


        if (leaf) {
        // leaf;
        } else if (expanded) {
            int count = node.getChildCount();
            for (int i = 0; i < count; i++) {
                MapperNode child = node.getChild(i);
                paintNodeLinks(child, selectedFilter, nodeY + child.getY(), g2,
                        rendererContext);
            }
        } else if (selectedFilter == nodeIsSelected) {
            int height = node.getHeight();
            int contentHeight = node.getContentHeight();

            if (contentHeight < height) {
                Set<Graph> childGraphs = node.getChildGraphs();
                LeftTree leftTree = mapper.getLeftTree();

                List<Link> ingoingEdges = new ArrayList<Link>();
                Map<Integer, Boolean> startYs = new HashMap<Integer, Boolean>();
                int outgoingEdge = 0;

                for (Graph childGraph : childGraphs) {
                    if (childGraph.hasConnectedOutgoingLinks()) {
                        outgoingEdge |= 3;
                    } else if (childGraph.hasOutgoingLinks()) {
                        outgoingEdge |= 1;
                    }

                    childGraph.getIngoingLinks(ingoingEdges);

                    for (Link edge : ingoingEdges) {
                        TreePath treePath = ((TreeSourcePin) edge.getSource()).getTreePath();

                        int y = yFromMapper(leftTree.yToMapper(leftTree
                                .getCenterY(treePath)));

                        Boolean oldValue = startYs.get(y);
                        if (oldValue == null) {
                            startYs.put(y, childGraph.isConnectedIngoingLink(edge));
                        } else if (!oldValue.booleanValue() && childGraph.isConnectedIngoingLink(edge)) {
                            startYs.put(y, true);
                        }
                    }

                    ingoingEdges.clear();
                }

                int y2 = nodeY + (contentHeight + height - 1) / 2;

                if (outgoingEdge != 0) {
                    Paint paint = (outgoingEdge == 3) ? linkColor
                            : new GradientPaint(centerX, 0, linkTColor,
                            maxX, 0, linkColor);

                    Link.paintLine(g2, paint, null,
                            centerX, y2, maxX, y2, step, minX, maxX);
                }

                for (Map.Entry<Integer, Boolean> pair : startYs.entrySet()) {
                    Paint paint = (pair.getValue()) ? linkColor
                            : new GradientPaint(minX, 0, linkColor,
                            centerX, 0, linkTColor);

                    Link.paintLine(g2, paint, null,
                            minX, pair.getKey(), centerX, y2, step, minX, maxX);
                }
            }
        }
    }

    public CanvasSearchResult find(int px, int py) { 
        MapperNode rootNode = getRoot();
        
        if (rootNode == null) return null;

        CanvasRendererContext rendererContext = getRendererContext();
        
        CanvasSearchResult result = null;
        
        result = findVertexOrVertexItem(px, py, rootNode, true, 0, rendererContext);
        if (result != null) return result;
        
        result = findLink(px, py, rootNode, true, 0, rendererContext);
        if (result != null) return result;
        
        result = findVertexOrVertexItem(px, py, rootNode, false, 0, rendererContext);
        if (result != null) return result;
        
        result = findLink(px, py, rootNode, false, 0, rendererContext);
        if (result != null) return result;
        
        MapperNode node = getNodeAt(py);
        if (node != null) {
            result = new CanvasSearchResult(node.getTreePath(), node.getGraph(), 
                    null, null);
        }
        
        return result;
    }
    
    
    private CanvasSearchResult findLink(int px, int py, MapperNode node, 
            boolean selectedFilter, int nodeY, 
            CanvasRendererContext rendererContext)
    {
        Mapper mapper = getMapper();
        
        CanvasSearchResult result = null;
        
        int step = rendererContext.getStep();
        int graphY = nodeY + (step - 1) / 2 + 1;
        
        double distance = 0.5 * step;
        
        if (node.isLeaf()) {
            // node is leaf
        } else if (node.isExpanded()) {
            // node is expanded
            int count = node.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                MapperNode child = node.getChild(i);
                result = findLink(px, py, child, selectedFilter, 
                        nodeY + child.getY(), rendererContext);
                if (result != null) {
                    break;
                }
            }
        } else {
            // node is collapsed
        }

        if (result == null && node.isSelected() == selectedFilter) {
            Graph graph = node.getGraph();
            if (graph != null) {
                if (node.isGraphExpanded()) {
                    for (int i = graph.getLinkCount() - 1; i >= 0; i--) {
                        Link link = graph.getLink(i);
                        if (link.distance(px, py, rendererContext, graphY) 
                                <= distance)
                        {
                            result = new CanvasSearchResult(node.getTreePath(), 
                                    graph, link, null);
                            break;
                        }
                    }
                }
            }
        }
        
        return result;
    }
    

    private CanvasSearchResult findVertexOrVertexItem(int px, int py, 
            MapperNode node, boolean selectedFilter, int nodeY, 
            CanvasRendererContext rendererContext)
    {
        TreePath treePath = node.getTreePath();
        CanvasSearchResult result = null;
        
        int step = rendererContext.getStep();
        int graphY = nodeY + (step - 1) / 2 + 1;
        
        Graph graph = node.getGraph();
        
        if (graph != null && node.isGraphExpanded() 
                && (node.isSelected() == selectedFilter)) 
        {
            int gx = px - rendererContext.getGraphX();
            int gy = py - graphY;
            
            for (int i = graph.getVertexCount() - 1; i >= 0; i--) {
                Vertex vertex = graph.getVertex(i);
                if (vertex.dontContains(gx, gy, step)) continue;

                if (vertex instanceof Constant) {
                    for (int j = vertex.getItemCount() - 1; j >= 0; j--) {
                        VertexItem item = vertex.getItem(j);
                        if (!item.isHairline() 
                                && item.contains(gx, gy, step)) 
                        {
                            result = new CanvasSearchResult(treePath, graph, 
                                    item, null);
                            break;
                        }
                    }
                } else if (vertex instanceof Operation) {
                    for (int j = vertex.getItemCount() - 1; j >= 0; j--) {
                        VertexItem item = vertex.getItem(j);

                        if (item.targetPinContains(gx, gy, step)) {
                            Link link = item.getIngoingLink();
                            result = new CanvasSearchResult(treePath, graph, 
                                    (link != null) ? link : item, item);
                            break;
                        }
                    }
                } else { // vertex iof Function
                    for (int j = vertex.getItemCount() - 1; j >= 0; j--) {
                        VertexItem item = vertex.getItem(j);

                        if (item.targetPinContains(gx, gy, step)) {
                            Link link = item.getIngoingLink();
                            result = new CanvasSearchResult(treePath, graph, 
                                    (link != null) ? link : item, item);
                            break;
                        }

                        if (!item.isHairline() && item.contains(gx, gy, step)) 
                        {
                            result = new CanvasSearchResult(treePath, graph, 
                                    item, null);
                            break;
                        }
                    }
                }
                
                if (result != null) {
                    break;
                }
                
                if (vertex.contains(gx, gy, step)) {
                    result = new CanvasSearchResult(treePath, graph, vertex, null);
                } else if (vertex.sourcePinContains(gx, gy, step)) {
                    Link link = vertex.getOutgoingLink();
                    result = new CanvasSearchResult(treePath, graph, 
                            (link != null) ? link : vertex, vertex);
                }
                
                if (result != null) {
                    break;
                }
            }
        }
        
        if (result == null && !node.isLeaf() && node.isExpanded()) {
            for (int i = node.getChildCount() - 1; i >= 0; i--) {
                MapperNode child = node.getChild(i);
                result = findVertexOrVertexItem(px, py, child, selectedFilter, 
                        nodeY + child.getY(), rendererContext);
                if (result != null) {
                    break;
                }
            }
        }
        
        return result;
    }
    

    void paintNodeVerteces(MapperNode node, boolean selectedFilter,
            int nodeY, Graphics2D g2, CanvasRendererContext rendererContext) {
        boolean nodeIsSelected = Utils.equal(getSelectionModel().getSelectedPath(), node.getTreePath());

        final int step = rendererContext.getStep();
        final boolean leaf = node.isLeaf();
        final boolean expanded = node.isExpanded();

        Graph graph = node.getGraph();
        int graphY = nodeY + (step - 1) / 2 + 1;

        if (graph != null && node.isGraphExpanded() && selectedFilter == nodeIsSelected) {
            graph.paintVerteces(g2, node.getTreePath(), rendererContext, graphY);
        }

        if (leaf) {
        // leaf;
        } else if (expanded) {
            int count = node.getChildCount();
            for (int i = 0; i < count; i++) {
                MapperNode child = node.getChild(i);
                paintNodeVerteces(child, selectedFilter, nodeY + child.getY(),
                        g2, rendererContext);
            }
        }
    }

    public Rectangle getPreferredGraphBounds() {
        JViewport viewport = getViewport();
        Mapper mapper = getMapper();

        int step = getStep();
        int graphViewPositionX = getGraphViewPositionX(step);

        Dimension treeSize = mapper.getPreferredTreeSize();
        if (treeSize == null) {
            return new Rectangle(graphViewPositionX - 10, 0, 10, 10);
        }

        XRange range = mapper.getGraphXRange();

        if (range == null) {
            return new Rectangle(graphViewPositionX - 10, 0, 10, treeSize.height);
        }

        int graphX = range.x * step;
        int graphWidth = range.width * step;

        int visibleWidth = viewport.getExtentSize().width;

        int inset = Math.max(100, visibleWidth - graphWidth);

        int minX = Math.min(graphX - inset,
                graphViewPositionX - visibleWidth - 100);
        int maxX = Math.max(graphX + graphWidth + inset,
                graphViewPositionX + 100);

        return new Rectangle(minX, 0, maxX - minX, treeSize.height);
    }

    public void focusGained(FocusEvent e) {
        repaint();
    }

    public void focusLost(FocusEvent e) {
        repaint();
    }

    private MapperNode getNode(int y) {
        MapperNode root = getRoot();
        return (root == null) ? null : root.getNode(y);
    }

    @Override
    public void doLayout() {
        cellRendererPane.setBounds(0, 0, getWidth(), getHeight());
        inplaceEditor.layoutEditor();
    }

    public JLabel getTextRenderer() {
        return textRenderer;
    }

   @Override
    public int getY() {
        if (printMode) {
            return 0; 
        }
        
        return super.getY();
    }
   
    private class CanvasScrollPane extends JScrollPane implements AdjustmentListener {

        public CanvasScrollPane() {
            super(Canvas.this,
                    VERTICAL_SCROLLBAR_NEVER,
                    HORIZONTAL_SCROLLBAR_ALWAYS);
            getViewport().setLayout(new CanvasViewportLayout());
            getHorizontalScrollBar().addAdjustmentListener(this);
        }

        @Override
        public void doLayout() {
            int step = getStep();

            JScrollBar hsb = getHorizontalScrollBar();
            JScrollBar vsb = getVerticalScrollBar();
            JViewport viewport = getViewport();

            hsb.setVisible(true);
            vsb.setVisible(false);

            Insets insets = getInsets();

            int graphViewPositionX = getGraphViewPositionX(step);

            int x = insets.left;
            int y = insets.top;

            int w = Math.max(0, getWidth() - insets.left - insets.right);
            int h = Math.max(0, getHeight() - insets.top - insets.bottom);

            int hsbHeight = Math.min(hsb.getPreferredSize().height, h);

            h -= hsbHeight;

            hsb.setBounds(x, y + h, w, hsbHeight);

            Border border = getViewportBorder();
            if (border != null) {
                insets = border.getBorderInsets(this);
                x += insets.left;
                y += insets.top;

                w = Math.max(0, w - insets.left - insets.right);
                h = Math.max(0, h - insets.top - insets.bottom);
            }

            viewport.setBounds(x, y, w, h);
            setGraphViewPositionX(graphViewPositionX, step);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(32, 32);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(32, 32);
        }

        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (!e.getValueIsAdjusting()) {
                Canvas.this.revalidate();
                Canvas.this.repaint();
            }
        }
    }

    private class CanvasViewportLayout implements LayoutManager {
        public void addLayoutComponent(String name, Component comp) {}
        public void removeLayoutComponent(Component comp) {}

        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(32, 32);
        }

        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(32, 32);
        }

        public void layoutContainer(Container parent) {
            JViewport viewport = (JViewport) parent;

            int step = getStep();

            Rectangle graphBounds = Canvas.this.getPreferredGraphBounds();
            Dimension size = graphBounds.getSize();

            int w = viewport.getWidth();
            int h = viewport.getHeight();

            int graphViewPositionX = getGraphViewPositionX(step);
            
            size.width = Math.max(size.width, w);
            size.height = Math.max(size.height, h);

            Point position = viewport.getViewPosition();

            position.x = Math.max(0, Math.min(graphViewPositionX - w - graphBounds.x, size.width - w));
            position.y = Math.max(0, Math.min(position.y, size.height - h));

            viewport.setViewSize(size);
            viewport.setViewPosition(position);

            setGraphViewPositionX(graphViewPositionX, step);
        }
    }

    public Insets getAutoscrollInsets() {
        Rectangle rect = scrollPane.getViewport().getViewRect();
        return new Insets(rect.y + 16, rect.x + 16,
                getHeight() - rect.y - rect.height + 16,
                getWidth() - rect.x - rect.width + 16);
    }

    public void autoscroll(Point cursorLocn) {
        if (scrollPane.getViewport() == null) {
            return;
        }

        Insets insets = getAutoscrollInsets();

        Rectangle r = new Rectangle(cursorLocn.x, cursorLocn.y, 1, 1);
        if (cursorLocn.y > getHeight() - insets.bottom) {
            r.y = getHeight() - insets.bottom + 16 +
                    2 * scrollPane.getVerticalScrollBar().getUnitIncrement();
        }
        if (cursorLocn.y < insets.top) {
            r.y = insets.top - 16 -
                    2 * scrollPane.getVerticalScrollBar().getUnitIncrement();
        }
        if (cursorLocn.x > getWidth() - insets.right) {
            r.x = getWidth() - insets.right + 16 +
                    2 * scrollPane.getHorizontalScrollBar().getUnitIncrement();
        }
        if (cursorLocn.x < insets.left) {
            r.x = insets.left - 16 -
                    2 * scrollPane.getHorizontalScrollBar().getUnitIncrement();
        }
        scrollRectToVisible(r);
    }

    public void mapperSelectionChanged(MapperSelectionEvent event) {
        List<Vertex> vertexes = getSelectionModel().getSelectedVerteces();
        if (vertexes == null || vertexes.size() == 0) return; 
        
        Vertex vertex = vertexes.get(0);
        
        int oldGraphX = getGraphViewPositionX();
        int graphW = getScrollPane().getViewport().getWidth();
        int graphX2 = oldGraphX;
        int graphX1 = oldGraphX - graphW;
        
        int step = getStep();

        int w = vertex.getWidth() * step;
        int x1 = vertex.getX() * step;
        int x2 = x1 + w;
        
        x1 -= 2 * step;
        x2 += 2 * step;
        
        if (x2 > graphX2) {
            graphX2 = x2;
            graphX1 = x2 - graphW;
        }
        
        if (x1 < graphX1) {
            graphX1 = x1;
            graphX2 = x1 + graphW;
        }
        
        if (graphX2 != oldGraphX) {
            setGraphViewPositionX(graphX2);
            invalidate();
            getScrollPane().validate();
            repaint();
        }
    }
    
    private class Copy extends CopyAction {

        @Override
        public void actionPerformed(ActionEvent ev) {
            super.actionPerformed(ev);
            System.out.println("123445**************");
        }
        
            
        
    }
}