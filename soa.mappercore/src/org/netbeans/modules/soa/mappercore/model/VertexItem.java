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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import javax.swing.JLabel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.CanvasRendererContext;
import org.netbeans.modules.soa.mappercore.MapperStyle;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 *
 * @author anjeleevich
 */
public class VertexItem implements TargetPin, GraphItem {

    private Vertex vertex;

    private Link ingoingLink;

    private int x;
    private int y;
    private int width;
    private int height;

    private boolean hairline;

    private Object dataObject;

    private Object value;
    private Class valueType;
    private String shortDescription;


    public VertexItem(Vertex vertex) {
        this(vertex, null);
    }


    public VertexItem(Vertex vertex, Object dataObject) {
        this(vertex, dataObject, null, null, null, true);
    }


    public VertexItem(Vertex vertex, String text) {
        this(vertex, null, text, String.class, null, false);
    }


    public VertexItem(Vertex vertex, Object value, Class valueType)
    {
        this(vertex, null, value, valueType);
    }


    public VertexItem(Vertex vertex, Object dataObject, Object value,
            Class valueType)
    {
        this(vertex, dataObject, value, valueType, null, false);
    }


    public VertexItem(Vertex vertex, Object dataObject, Object value,
            Class valueType, String shortDescription, boolean hairline)
    {
        if (vertex == null) throw new IllegalArgumentException();

        this.vertex = vertex;
        this.hairline = hairline;

        this.value = value;
        updateSize(value);

        this.valueType = valueType;
        this.shortDescription = shortDescription;

        this.dataObject = dataObject;
    }


    public Object getDataObject() {
        return dataObject;
    }


    public void setDataObject(Object dataObject) {
        this.dataObject = dataObject;
    }


    public Object getValue() {
        return value;
    }


    public String getText() {
        return (value == null) ? null : value.toString();
    }


    public void setText(String text) {
        setValue(text);
    }


    public Class getValueType() {
        return valueType;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setValue(Object value) {
        setValueAndType(value, getValueType());
    }


    public void setValueType(Class valueType) {
        setValueAndType(getValue(), valueType);
    }


    public void setValueAndType(Object value, Class valueType) {
        boolean changed = false;

        if (!Utils.equal(this, value)) {
            this.value = value;
            updateSize(value);
            changed = true;
        }

        if (!Utils.equal(this.valueType, valueType)) {
            this.valueType = valueType;
            changed = true;
        }

        if (changed) {
            getVertex().fireGraphContentChanged();
        }
    }

    public boolean isHairline() {
        return hairline;
    }


    public Link getIngoingLink() {
        return ingoingLink;
    }


    public void setIngoingLink(Link link) {
        if (this.ingoingLink != link) {
            this.ingoingLink = link;
        }
    }


    public Vertex getVertex() {
        return vertex;
    }


    public void moveOnTop() {
        getVertex().moveOnTop();
    }


    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getAnchorX() { return vertex.getWidth(); }
    public int getAnchorY() { return y + height / 2; }

    public int getGlobalX() { return vertex.getX() + x; }
    public int getGlobalY() { return vertex.getY() + y; }

    public int getPinX() { return 0; }
    public int getPinY() { return y + height / 2; }

    public int getPinGlobalX() { return vertex.getX() + getPinX(); }
    public int getPinGlobalY() { return vertex.getY() + getPinY(); }


    public boolean contains(int graphX, int graphY, int step) {
        int px = graphX - getGlobalX() * step;
        int py = graphY - getGlobalY() * step;
        return 0 <= px && 0 <= py && px <= width * step && py <= height * step;
    }


    public boolean targetPinContains(int graphX, int graphY, int step) {
        int px = graphX - (getPinGlobalX() - 1) * step;
        int py = graphY - (getPinGlobalY() * step - step / 2);
        return 0 <= px && 0 <= py && px <= step && py <= step;
    }



    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }


    public Rectangle getBounds(int step) {
        return new Rectangle(x * step, y * step, width * step, height * step);
    }


    public void paint(Graphics2D g2, TreePath treePath,
            CanvasRendererContext rendererContext, int graphY)
    {
        int step = rendererContext.getStep();
        int x0 = rendererContext.getGraphX() + getGlobalX() * step;
        int y0 = graphY + getGlobalY() * step;
        int width = getWidth() * step;
        int height = getHeight() * step;

        int labelX = x0 + 3;
        int labelY = y0 + 1;
        int labelHeight = height - 1;
        int labelWidth = width - 5;

        if (labelWidth > 0) {
            rendererContext.getCanvas().paintVertexItemText(treePath, this,
                    g2, labelX, labelY, labelWidth, labelHeight);
        }
//                label.setText(text);
//                label.setHorizontalAlignment(JLabel.LEFT);
//                label.setForeground(MapperStyle.VERTEX_ITEM_TEXT_COLOR);
//                label.setFont(label.getFont().deriveFont(Font.PLAIN));
//                label.setBounds(0, 0, labelWidth, labelHeight);
//
//                g2.translate(labelX, labelY);
//                label.paint(g2);
//                g2.translate(-labelX, -labelY);


        if (rendererContext.isSelected(treePath, this)) {
            Stroke oldStroke = g2.getStroke();
            g2.setPaint(MapperStyle.SELECTION_COLOR);
            g2.setStroke(MapperStyle.FOCUS_STROKE);
            g2.drawRect(x0, y0, width, height);
            g2.setStroke(oldStroke);
        }
    }


    public void paintTargetPin(Graphics2D g2, TreePath treePath,
            CanvasRendererContext rendererContext, int graphY)
    {
        int step = rendererContext.getStep();
        int x0 = rendererContext.getGraphX() + getPinGlobalX() * step;
        int y0 = graphY + getPinGlobalY() * step;

        Link link = getIngoingLink();
        boolean selected = rendererContext.isSelected(treePath, this);

        if (rendererContext.paintVertexItemPin(treePath, this)) {
            int x1 = x0 - step;
            int x2 = x0 - 1;

            int size = step - 1;

            int y1 = y0 - size / 2;
            int y2 = y1 + size;

            int off = (x2 - x1) / 3;
            int d = Math.min(off, 2) * 2;

            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            g2.setPaint((selected)
                    ? MapperStyle.SELECTION_COLOR
                    : MapperStyle.PIN_BACKGROUND_COLOR);
            g2.fillRoundRect(x1, y1, x2 - x1, y2 - y1, d, d);

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

            Link.paintTargetDecoration(g2, new Point(x0, y0), color, step);
        }
    }


    public Point getTargetPinPoint(int graphX, int graphY, int step) {
        return new Point(
                graphX + getPinGlobalX() * step,
                graphY + getPinGlobalY() * step);
    }


    void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


//    public Navigable navigate(int direction, int modifier) {
//        Vertex vertex = getVertex();
//        int index = vertex.getItemIndex(this);
//        int count = vertex.getItemCount();
//
//        if (index < 0) return null;
//
//        if (direction == DIRECTION_DOWN) {
//            return (index + 1 == count) ? vertex : vertex.getItem(index + 1);
//        }
//
//        if (direction == DIRECTION_UP) {
//            return (index == 0) ? vertex : vertex.getItem(index - 1);
//        }
//
//        if (direction == DIRECTION_LEFT || direction == DIRECTION_RIGHT) {
//            return vertex.navigate(direction, modifier);
//        }
//
//        throw new IllegalArgumentException();
//

    private void updateSize(Object value) {
        if (value == null) {return;}
        int w = 2;
        if (vertex instanceof Constant) { w = w + 3; }

        JLabel lab = new JLabel();
        FontMetrics metrics = lab.getFontMetrics(lab.getFont());

        int nw = metrics.stringWidth(value.toString());
        int step = Math.max((metrics.getHeight() + 2) / 2 + 1, 9);

        vertex.setWidth(Math.max(vertex.getWidth(), nw / step + w));
    }
}
