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

package org.netbeans.modules.soa.mappercore.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import javax.swing.JComponent;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.Canvas;
import org.netbeans.modules.soa.mappercore.CanvasRendererContext;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.MapperStyle;
import org.netbeans.modules.soa.mappercore.graphics.Triangle;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 *
 * @author anjeleevich
 */
public class Link implements GraphItem {
    
    private Graph graph;
    private SourcePin source;
    private TargetPin target;

    
    public Link() {
        
    }
    
    
    public Link(SourcePin source, TargetPin target) {
        setSource(source);
        setTarget(target);
    }
    
    
    public void moveOnTop() {
        graph.moveOnTop(this);
    }
    
    
    public Graph getGraph() {
        return graph;
    }
    
    
    void setGraph(Graph graph) {
        this.graph = graph;
    }
    
    
    public boolean isInGraph() {
        return graph != null;
    }    
    
    
    public void setSource(SourcePin source) {
        SourcePin oldSource = this.source;
        
        if (oldSource != source) {
            if (oldSource instanceof Vertex) {
                ((Vertex) oldSource).setOutgoingLink(null);
            }
            
            if (source instanceof Vertex) {
                ((Vertex) source).setOutgoingLink(this);
            }
            
            this.source = source;
            fireChanged();
        }
    }
    
    
    public SourcePin getSource() {
        return source;
    }
    
    
    public TargetPin getTarget() {
        return target;
    }
    
    
    public void connect(SourcePin source, TargetPin target) {
        setSource(source);
        setTarget(target);
    }
    
    public void disconnect() {
        setSource(null);
        setTarget(null);
        getGraph().removeLink(this);
    }
    
    public void setTarget(TargetPin target) {
        TargetPin oldTarget = this.target;
        
        if (oldTarget != target) {
            if (oldTarget instanceof VertexItem) {
                ((VertexItem) oldTarget).setIngoingLink(null);
            }
            
            if (target instanceof VertexItem) {
                ((VertexItem) target).setIngoingLink(this);
            }
            
            this.target = target;
            fireChanged();
        }
    }
    
    
    private void fireChanged() {
        Graph graph = getGraph();
        if (graph != null) {
            graph.fireGraphLinksChanges();
        }
    }
    
    
    public Point getSourcePoint(CanvasRendererContext rendererContext, 
            int graphY) 
    {
        if (source instanceof Vertex) {
            return ((Vertex) source).getSourcePinPoint(
                    rendererContext.getGraphX(), graphY, 
                    rendererContext.getStep());
        } 
        
        if (source instanceof TreeSourcePin) {
            LeftTree leftTree = rendererContext.getLeftTree();
            Canvas canvas = rendererContext.getCanvas();
            
            TreePath treePath = ((TreeSourcePin) source).getTreePath();
            
            int y = leftTree.getCenterY(treePath);
            y = leftTree.yToMapper(y);
            y = canvas.yFromMapper(y);

            return new Point(Integer.MIN_VALUE, y);
        }
        
        return null;
    }
    
    
    public Point getTargetPoint(CanvasRendererContext rendererContext, 
            int graphY) 
    {
        int step = rendererContext.getStep();
        
        if (target instanceof VertexItem) {
            return ((VertexItem) target).getTargetPinPoint(
                    rendererContext.getGraphX(), graphY, step);
        }
        
        if (target instanceof Graph) {
            if (getGraph().isEmptyOrOneLink()) {
                JComponent tree = rendererContext.getRightTree();
                int h = tree.getFontMetrics(tree.getFont()).getHeight();
                h = Math.max(h, 16) + 4;
                int size = step - 1;
                int topInset = size / 2 + 1;
                return new Point(Integer.MAX_VALUE, graphY + h / 2 - topInset); 
            } else {
                return ((Graph) target).getTargetPinPoint(
                        rendererContext.getGraphX(), graphY, step);
            }
        }
        
        return null;
    }
    
    
    public void paint(Graphics2D g2, TreePath treePath, 
            CanvasRendererContext rendererContext, int graphY)
    {
        if (source == null) return;
        if (target == null) return;
        
        if (!rendererContext.paintLink(treePath, this)) return;
        
        Mapper mapper = rendererContext.getMapper();
        int step = rendererContext.getStep();
        
        boolean selectedLink = rendererContext.isSelected(treePath, this);
        boolean selectedPath = (selectedLink) ? true 
                : rendererContext.isSelected(treePath);
        
        Point sourcePoint = getSourcePoint(rendererContext, graphY);
        Point targetPoint = getTargetPoint(rendererContext, graphY);
        
        Color color;
        Stroke stroke;
        
        if (selectedLink) {
            color = MapperStyle.SELECTION_COLOR;
            stroke = MapperStyle.SELECTION_STROKE;
        } else if (selectedPath) {
            color = MapperStyle.LINK_COLOR_SELECTED_NODE;
            stroke = MapperStyle.LINK_STROKE;
        } else {
            color = MapperStyle.LINK_COLOR_UNSELECTED_NODE;
            stroke = MapperStyle.LINK_STROKE;
        }
        
        paintLine(g2, color, stroke, sourcePoint, targetPoint, step, 
                rendererContext.getCanvasVisibleMinX(), 
                rendererContext.getCanvasVisibleMaxX());
    }
    
    
    public double distance(int px, int py, 
            CanvasRendererContext rendererContext, int graphY) 
    {
        if (source == null) return Double.POSITIVE_INFINITY;
        if (target == null) return Double.POSITIVE_INFINITY;
        
        Point sourcePoint = getSourcePoint(rendererContext, graphY);
        Point targetPoint = getTargetPoint(rendererContext, graphY);

        int step = rendererContext.getStep();
        
        int startX = (sourcePoint.x == Integer.MIN_VALUE) 
                ? rendererContext.getCanvasVisibleMinX()
                : sourcePoint.x + step / 2;
        
        int endX = (targetPoint.x == Integer.MAX_VALUE) 
                ? rendererContext.getCanvasVisibleMaxX()
                : targetPoint.x - step / 2;
        
        Shape shape = createShape(
                startX, sourcePoint.y, 
                endX, targetPoint.y, 
                rendererContext.getStep(), 
                rendererContext.getCanvasVisibleMinX(), 
                rendererContext.getCanvasVisibleMaxX());
        
        return Utils.distance(shape, px, py);
    }
    
    
    public static void paintLine(Graphics2D g2, Paint paint, Stroke stroke, 
            Point sourcePoint, Point targetPoint, int step, int minX, int maxX) 
    {
        if (sourcePoint == null) return;
        if (targetPoint == null) return;
        
        int startX = (sourcePoint.x == Integer.MIN_VALUE) ? minX
                : sourcePoint.x + step / 2;
        
        int endX = (targetPoint.x == Integer.MAX_VALUE) ? maxX 
                : targetPoint.x - step / 2;
        
        paintLine(g2, paint, stroke, startX, sourcePoint.y, endX, 
                targetPoint.y, step, minX, maxX);
    }
    
    
    
    public static Shape createShape(int startX, int startY, 
            int endX, int endY, int step, int minX, int maxX) 
    {
        int x1, x2, x6, x5;

        x1 = startX;
        x2 = x1 + step;
        
        if (endX == Integer.MAX_VALUE) {
            x6 = maxX;
        } else {
            x6 = endX;
        }

        x5 = x6 - step;
        
        int d = Math.max((x5 - x2) / 4, step * 2);

        int x3 = x2 + d;
        int x4 = x5 - d;

        int y1 = startY;
        int y2 = endY;

        float cx = (x3 + x4) / 2f;
        float cy = (y1 + y2) / 2f;

        GeneralPath gp = new GeneralPath();
        gp.moveTo(0.5f + x1, 0.5f + y1);
        gp.lineTo(0.5f + x2, 0.5f + y1);
        gp.quadTo(0.5f + x3, 0.5f + y1, 0.5f + cx, 0.5f + cy);
        gp.quadTo(0.5f + x4, 0.5f + y2, 0.5f + x5, 0.5f + y2);
        gp.lineTo(0.5f + x6, 0.5f + y2);
        
        return gp;
    }
    

    public static void paintLine(Graphics2D g2, Paint paint, Stroke stroke,
            int startX, int startY, int endX, int endY, 
            int step, int minX, int maxX) 
    {
        Stroke oldStroke = g2.getStroke();
        if (stroke != null) {
            g2.setStroke(stroke);
        }
        g2.setPaint(paint);
        g2.draw(createShape(startX, startY, endX, endY, step, minX, maxX));
        g2.setStroke(oldStroke);
    }
    
    
    public static void paintTargetDecoration(Graphics2D g2, 
            Point targetPoint, Color color, int step) 
    {
        if (color == null) {
            color = MapperStyle.LINK_COLOR_SELECTED_NODE;
        }
        
        int x2 = targetPoint.x - 1;
        int y2 = targetPoint.y;
        
        int x1 = x2 - step + 1;
        
        int d = Math.max(1, (step - 1) / 2 - 1);
        
        g2.translate(0.5, 0.5);
        g2.setPaint(color);
        g2.fill(new Triangle(x2, y2, x1, y2 - d, x1, y2 + d));
        g2.translate(-0.5, -0.5);
    }
    
    
    public static void paintSourceDecoration(Graphics2D g2, 
            Point sourcePoint, Color color, int step) 
    {
        if (color == null) {
            color = MapperStyle.LINK_COLOR_SELECTED_NODE;
        }
        
        int x = sourcePoint.x + step / 2;
        int y = sourcePoint.y;
        
        int r = Math.max(1, (step - 1) / 2 - 1);
        int d = r * 2;
        
        g2.translate(0.5, 0.5);
        g2.setPaint(color);
        g2.fillOval(x - r, y - r, d, d);
        g2.translate(-0.5, -0.5);
    }

    
//    public Navigable navigate(int direction, int modifier) {
//        if (direction == DIRECTION_UP || direction == DIRECTION_RIGHT) {
//            TargetPin target = getTarget();
//            return (target instanceof VertexItem) ? (VertexItem) target : null;
//        }
//
//        if (direction == DIRECTION_DOWN || direction == DIRECTION_LEFT) {
//            SourcePin source = getSource();
//            return (source instanceof Vertex) ? (Vertex) source : null;
//        }
//        
//        throw new IllegalArgumentException();
//    }
//    
//    
//    public static final Color EDGE_COLOR_SELECTED_NODE = new Color(0xE68B2C);
//    public static final Color EDGE_COLOR_UNSELECTED_NODE = new Color(0xE68B2C);
//    public static final Color EDGE_TRANSPARENT_COLOR = new Color(0x00E68B2C, true);
}
