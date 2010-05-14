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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.CanvasRendererContext;
import org.netbeans.modules.soa.mappercore.MapperStyle;
import org.netbeans.modules.soa.mappercore.graphics.RRectangle;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 *
 * @author anjeleevich
 */
public abstract class Vertex implements SourcePin, GraphItem {

    private Graph graph;
    
    private List<VertexItem> items;
    private Link outgoingLink;
    
    private int x;
    private int y;
    private int width;
    private int height = 2;
    
    private Icon icon;
    private String name;
    private String resultText;
    
    private Object dataObject;

    final long uid;
    
    
    Vertex(Object dataObject, Icon icon) {
        this(dataObject, icon, null, null);
    }
    
    
    Vertex(Object dataObject, Icon icon, String name, String resultText) {
        this.icon = icon;
        this.name = name;
        this.resultText = resultText;
        this.uid = nextUID++;
        
        this.dataObject = dataObject;
        
        setWidth(10);
        layout();
    }
    
    
    public Object getDataObject() {
        return dataObject;
    }
    
    
    public String getResultText() {
        return resultText;
    }
    
    
    public void setResultText(String resultText) {
        if (!Utils.equal(this.resultText, resultText)) {
            this.resultText = resultText;
            fireGraphContentChanged();
        }
    }
    
    
    public void moveOnTop() {
        graph.moveOnTop(this);
    }
    

    public int getPinX() { return width; }
    public int getPinY() { return height / 2; }
    
    public int getPinGlobalX() { return getX() + getPinX(); }
    public int getPinGlobalY() { return getY() + getPinY(); }
    
    
    public Link getOutgoingLink() {
        return outgoingLink;
    }
    
    
    public void setOutgoingLink(Link outgoingLink) {
        if (this.outgoingLink != outgoingLink) {
            this.outgoingLink = outgoingLink;
        }
    }
    
    
    public String getName() {
        return name;
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
    

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    
    public int getMinimumWidth() { return 6; }
    public int getMaximumWidth() { return 20; }
    
    
    public boolean dontContains(int graphX, int graphY, int step) {
        int y1 = y * step;
        int y2 = y1 + height * step + 1;
        
        if (graphY < y1 || y2 < graphY) return true;
        
        int x1 = x * step;
        int x2 = x1 + width * step + 1;
        
        x1 -= step;
        x2 += step;
        
        return graphX < x1 || x2 < graphX;
    }
    
    
    public boolean contains(int graphX, int graphY, int step) {
        return createShape(step).contains(0.5 + graphX - x * step, 0.5 + graphY 
                - y * step);
    }
    
    
    public boolean sourcePinContains(int px, int py, int step) {
        px -= (x + width) * step;
        py -= getPinGlobalY() * step - step / 2;
        return 0 <= px && px <= step && 0 <= py && py <= step;
    }
    
    
    public abstract RRectangle createShape(int step);
    
    
    public void setLocation(int x, int y) {
        y = Math.max(0, y);
        if (this.x != x || this.y != y) {
            this.x = x;
            this.y = y;
            invalidateGraphBounds();
        }
    }
    
    
    public void setWidth(int width) {
        width = Math.max(getMinimumWidth(), Math.min(width, getMaximumWidth()));
        if (this.width != width) {
            this.width = width;
            layout();
            invalidateGraphBounds();
        }
    }
    

    void setHeight(int height) {
        if (this.height != height) {
            this.height = height;
            invalidateGraphBounds();
        }
    }
    
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    
    public Rectangle getBounds(int step) {
        return new Rectangle(x * step, y * step, width * step, height * step);
    }    
    
    
    public Icon getIcon() {
        return icon;
    }
    
    
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }
    
    
    public VertexItem getItem(int i) {
        return items.get(i);
    }
    
    
    public int getItemIndex(VertexItem item) {
        return (items == null) ? -1 : items.indexOf(item);
    }
    
    
    public void addItem(VertexItem item) {
        addItem(item, -1);
    }
    
    
    public void addItem(VertexItem item, int i) {
        checkItem(item);
        
        if (items == null) {
            items = new ArrayList<VertexItem>();
            items.add(item);
        } else {
            int count = items.size();

            if (i < 0 || i >= count) {
                items.add(item);
                i = count;
            } else {
                items.add(i, item);
            }
            
            for (int j = items.size() - 1; j >= 0; j--) {
                if (j != i && items.get(j) == item) {
                    items.remove(j);
                    break;
                }
            }
        }
        
        layout();
    }
            
    
    public void removeItem(VertexItem item) {
        checkItem(item);
        
        int index = getItemIndex(item);
        
        if (index >= 0) {
            items.remove(index);
            if (items.isEmpty()) items = null;
        }

        layout();
    }
    
            
    private void checkItem(VertexItem item) {
        if (item == null) throw new IllegalArgumentException();
        if (item.getVertex() != this) throw new IllegalArgumentException();
    }
    
    
    public abstract void paint(Graphics2D g2, TreePath treePath,
            CanvasRendererContext rendererContext, int graphY);
    
    
    public void paintSourcePin(Graphics2D g2, TreePath treePath,
            CanvasRendererContext rendererContext, int graphY) 
    {
        Link link = getOutgoingLink();
        
        int step = rendererContext.getStep();
        int x0 = rendererContext.getGraphX() + getPinGlobalX() * step;
        int y0 = graphY + getPinGlobalY() * step;
                
        if (rendererContext.paintVertexPin(treePath, this)) {
            int x1 = x0 + 2;
            int x2 = x0 + step + 1;

            int size = step - 1;

            int y1 = y0 - size / 2;
            int y2 = y1 + size;

            int off = (x2 - x1) / 3;
            int d = Math.min(off, 2) * 2;

            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            g2.setPaint((rendererContext.isSelected(treePath, this)) 
                    ? MapperStyle.SELECTION_COLOR
                    : MapperStyle.ICON_COLOR);
             g2.fillRoundRect(x1, y1, x2 - x1, y2 - y1, d, d);
             //g2.fillOval(x1, y1, x2 - x1, y2 - y1);

            GeneralPath gp = new GeneralPath();
            gp.moveTo(x1 + off, y1 + 0.5f);
            gp.lineTo(x2 - off, 0.5f * (y1 + y2));
            gp.lineTo(x1 + off, y2 - 0.5f);

            g2.setPaint(MapperStyle.PIN_FOREGROUND_COLOR);
            g2.draw(gp);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_NORMALIZE);
        } else if (link != null && rendererContext.paintLink(treePath, link)) {
            Color color = MapperStyle.LINK_COLOR_UNSELECTED_NODE;
            
            if (rendererContext.isSelected(treePath)) {
                color = (rendererContext.isSelected(treePath, link))
                        ? MapperStyle.SELECTION_COLOR
                        : MapperStyle.LINK_COLOR_SELECTED_NODE;
            }
            
            Link.paintSourceDecoration(g2, new Point(x0, y0), color, step);
        }
    }
    
    
    public Point getSourcePinPoint(int graphX, int graphY, int step) {
        return new Point(
                graphX + getPinGlobalX() * step, 
                graphY + getPinGlobalY() * step);
    }
    
    
    public void layout() {
        fireGraphContentChanged();
    }
    
    
    void invalidateGraphBounds() {
        Graph graph = getGraph();
        if (graph != null) {
            graph.invalidateBounds();
        }
    }

    
    void invalidateGraphLinks() {
        Graph graph = getGraph();
        if (graph != null) {
            graph.invalidateLinks();
        }
    }
    
    
    void fireGraphContentChanged() {
        Graph graph = getGraph();
        if (graph != null) {
            graph.fireGraphContentChanged();
        }
    }
    
    
//    public Navigable navigate(int direction, int modifier) {
//        if (direction == DIRECTION_LEFT) {
//            return getGraph().getPrevVertex(this);
//        }
//        
//        if (direction == DIRECTION_RIGHT) {
//            return getGraph().getNextVertex(this);
//        }
//        
//        if (direction == DIRECTION_UP) {
//            int itemCount = getItemCount();
//            return (itemCount > 0) ? getItem(itemCount - 1) : this;
//        }
//        
//        if (direction == DIRECTION_DOWN) {
//            return (getItemCount() > 0) ? getItem(0) : this;
//        }
//        
//        throw new IllegalArgumentException();
//    }
    
    
//    public static final Color VERTEX_BACKGROUND_COLOR = Color.WHITE;
//    public static final Color VERTEX_BORDER_COLOR = new Color(0xA7A2A7);
//    
//    public static final BufferedImage GRADIENT_TEXTURE;
//    public static final Color ICON_COLOR = new Color(0x5668CA);
//    
//    
//    static {
//        double[] percents = { 0, 0.0843, 0.1798, 0.7416, 0.9045, 1.0674 };
//        int[] rgbColors = { 0xA9CDE8, 0xDDEBF6, 0xFFFFFF, 0xDCE3EF, 0xE7F1F9, 
//                0xDCE3EF };
//        
//        final int height = 100;
//        
//        BufferedImage image = new BufferedImage(1, height, 
//                BufferedImage.TYPE_INT_RGB);
//        
//        Graphics2D g2 = (Graphics2D) image.getGraphics();
//        
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
//                RenderingHints.VALUE_ANTIALIAS_ON);
//        
//        Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 1, height);
//        
//        for (int i = 1; i < percents.length; i++) {
//            float y0 = (float) (percents[i - 1] * height - 0.5);
//            float y1 = (float) (percents[i] * height + 0.5);
//            
//            rect.y = y0;
//            rect.height = y1 - y0;
//            
//            Paint p = new GradientPaint(
//                    0, y0, new Color(rgbColors[i - 1]), 
//                    0, y1, new Color(rgbColors[i]));
//            
//            g2.setPaint(p);
//            g2.fill(rect);
//        }
//        
//        GRADIENT_TEXTURE = image;
//    }     
    
    
    private static long nextUID = 0;
}
