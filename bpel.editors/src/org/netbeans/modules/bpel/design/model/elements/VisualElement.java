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
package org.netbeans.modules.bpel.design.model.elements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.ViewProperties;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.TextstyleDescriptor;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

/**
 *
 * @author anjeleevich
 */
public abstract class VisualElement {

    protected FShape shape;
    private FBounds textBounds;
    private List<Connection> inputConnections = new ArrayList<Connection>();
    private List<Connection> outputConnections = new ArrayList<Connection>();
    private String text;
    private Pattern pattern;

    public VisualElement(FShape shape) {
        this.shape = shape;
    }

    public void setPattern(Pattern newPattern) {
        pattern = newPattern;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public boolean hasPattern() {
        return pattern != null;
    }

    public void addInputConnection(Connection c) {
        inputConnections.add(c);
    }

    public void addOutputConnection(Connection c) {
        outputConnections.add(c);
    }

    public void removeInputConnection(Connection c) {
        inputConnections.remove(c);
    }

    public void removeOutputConnection(Connection c) {
        outputConnections.remove(c);
    }

    public List<Connection> getIncomingConnections() {
        return inputConnections;
    }

    public List<Connection> getOutcomingConnections() {
        return outputConnections;
    }

    public void setLabelText(String text) {
        this.text = text;
    }

    public String getLabelText() {
        return text;
    }

    public abstract void paint(Graphics2D g2);

    public abstract void paintThumbnail(Graphics2D g2);

    public FShape getShape() {
        return shape;
    }

    public FBounds getBounds() {
        return shape;
    }

    public void translate(double tx, double ty) {
        shape = shape.translate(tx, ty);
    }

    public void setLocation(double x0, double y0) {
        shape = shape.move(x0, y0);
    }

    public void setCenter(double cx, double cy) {
        shape = shape.moveCenter(cx, cy);
    }

    public double getX() {
        return shape.getX();
    }

    public double getY() {
        return shape.getY();
    }

    public double getCenterX() {
        return shape.getCenterX();
    }

    public double getCenterY() {
        return shape.getCenterY();
    }

    public double getMinX() {
        return shape.getMinX();
    }

    public double getMinY() {
        return shape.getMinY();
    }

    public double getMaxX() {
        return shape.getMaxX();
    }

    public double getMaxY() {
        return shape.getMaxY();
    }

    public double getWidth() {
        return shape.getWidth();
    }

    public double getHeight() {
        return shape.getHeight();
    }

    public boolean textContains(double x, double y) {
        return (textBounds != null) && textBounds.contains(x, y);
    }

    public boolean contains(double x, double y) {
        return textContains(x, y) || shape.contains(x, y);
    }

    public String getText() {
        return text;
    }

    public void setText(String newText) {
        text = (newText != null) ? newText.trim() : null;
    }

    public boolean isEmptyText() {
        return (text == null) || (text.length() == 0);
    }

    public boolean isTextElement() {
        return (getPattern() == null) ? false : getPattern().isTextElement(this);
    }

    public boolean isPaintText() {
        return !isEmptyText();
    }

    public Color getTextColor() {
        Pattern p = getPattern();
        Decoration decoration = p.getModel().getView().getDecoration(p);

        TextstyleDescriptor textStyle = (decoration == null) ? null
                : decoration.getTextstyle();

        boolean editable = isTextElement();

        if (textStyle == null) {
            return (editable)
                    ? ViewProperties.EDITABLE_TEXT_COLOR
                    : ViewProperties.UNEDITABLE_TEXT_COLOR;
        } else {
            return (editable)
                    ? textStyle.getEditableTextColor()
                    : textStyle.getNotEditableTextColor();
        }
    }

    protected void drawString(Graphics2D g2, String string,
            double x, double y, double width) {
        if (string == null || string.length() == 0) {
            setTextBounds(null);
        }

        string = clipString(string, g2, width);

        Rectangle2D bounds = g2.getFont().getStringBounds(string,
                g2.getFontRenderContext());

        double w = bounds.getWidth();
        double h = bounds.getHeight();

        double tx = x - bounds.getX();
        double ty = y - bounds.getY();

        g2.translate(tx, ty);
        g2.drawString(string, 0, 0);
        g2.translate(-tx, -ty);

        setTextBounds(new FBounds(tx, ty + bounds.getY(), w, h));
    }

    protected void drawXCenteredString(Graphics2D g2, String string,
            double cx, double y, double width) {
        if (string == null || string.length() == 0) {
            setTextBounds(null);
        }

        string = clipString(string, g2, width);

        Rectangle2D bounds = g2.getFont().getStringBounds(string,
                g2.getFontRenderContext());

        double w = bounds.getWidth();
        double h = bounds.getHeight();

        double tx = cx - w / 2.0 - bounds.getX();
        double ty = y - bounds.getY();

        g2.translate(tx, ty);
        g2.drawString(string, 0, 0);
        g2.translate(-tx, -ty);

        setTextBounds(new FBounds(tx, ty + bounds.getY(), w, h));
    }

    protected void drawCenteredString(Graphics2D g2, String string,
            double cx, double cy, double width) {
        if (string == null || string.length() == 0) {
            setTextBounds(null);
        }

        string = clipString(string, g2, width);

        Rectangle2D bounds = g2.getFont().getStringBounds(string,
                g2.getFontRenderContext());

        double w = bounds.getWidth();
        double h = bounds.getHeight();

        double tx = cx - w / 2.0 - bounds.getX();
        double ty = cy - h / 2.0 - bounds.getY();

        g2.translate(tx, ty);
        g2.drawString(string, 0, 0);
        g2.translate(-tx, -ty);

        setTextBounds(new FBounds(tx, ty + bounds.getY(), w, h));
    }

    private static String clipString(String string, Graphics2D g2,
            double clipWidth) {
        Font font = g2.getFont();
        FontRenderContext frc = g2.getFontRenderContext();

        double stringWidth = font.getStringBounds(string, frc).getWidth();

        if (stringWidth <= clipWidth) {
            return string;
        }

        clipWidth -= font.getStringBounds("...", frc).getWidth();

        if (clipWidth <= 0.0) {
            return "...";
        }

        int i = string.length();

        do {
            i--;
            stringWidth -= font.getStringBounds(string.substring(i, i + 1), frc).getWidth();
        } while (stringWidth > clipWidth && i > 0);

        return string.substring(0, i) + "...";
    }

    public static double getScale(AffineTransform at) {
        if (at == null) {
            return 1.0;
        }

        double coords[] = {1, 1};
        at.deltaTransform(coords, 0, coords, 0, 1);

        double x = coords[0];
        double y = coords[1];

        return Math.sqrt(x * x + y * y) / 2.0;
    }
    private static final double COS_45 = Math.sqrt(2.0) / 2.0;
    public static final Color GRADIENT_TEXTURE_COLOR = new Color(0xE7ECF4);
    public static final BufferedImage GRADIENT_TEXTURE;
    static {
        double[] percents = {0, 0.0843, 0.1798, 0.7416, 0.9045, 1.0674};
        int[] rgbColors = {0xA9CDE8, 0xDDEBF6, 0xFFFFFF, 0xDCE3EF, 0xE7F1F9,
                0xDCE3EF};

        final int height = 100;

        BufferedImage image = new BufferedImage(1, height,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = (Graphics2D) image.getGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 1, height);

        for (int i = 1; i < percents.length; i++) {
            float y0 = (float) (percents[i - 1] * height - 0.5);
            float y1 = (float) (percents[i] * height + 0.5);

            rect.y = y0;
            rect.height = y1 - y0;

            Paint p = new GradientPaint(0, y0, new Color(rgbColors[i - 1]),
                    0, y1, new Color(rgbColors[i]));

            g2.setPaint(p);
            g2.fill(rect);
        }

        GRADIENT_TEXTURE = image;
    }

    public FBounds getTextBounds() {
        if (isEmptyText()) {
            return null;
        }
        return textBounds;
    }

    protected void setTextBounds(FBounds textBounds) {
        this.textBounds = textBounds;
    }

    public List<Connection> getAllConnections() {
        List<Connection> result = new ArrayList<Connection>();
        result.addAll(inputConnections);
        result.addAll(outputConnections);
        return result;
    }
/*
    public void scrollTo() {
        DesignView view = getPattern().getModel().getView();
        FBounds bounds = getTextBounds();
        Rectangle rect;
        Point p1,
         p2;

        if (bounds == null) {
            FShape shape = getShape();
            rect = view.getVisibleRect();
            double y0;
            double y1;
            double x;

            if (this instanceof ContentElement) {
                y0 = shape.getMaxY();
                y1 = y0 + 24;
                x = shape.getCenterX();
                p1 = view.convertDiagramToScreen(new FPoint(x, y0));
                p2 = view.convertDiagramToScreen(new FPoint(x, y1));
                rect.x = p1.x - rect.width / 2;
                rect.y = p1.y;
                rect.height = p2.y - p1.y;
            } else if (this instanceof ProcessBorder) {
                y0 = shape.getY();
                y1 = y0 + 32;
                x = shape.getCenterX();
                p1 = view.convertDiagramToScreen(new FPoint(x, y0));
                p2 = view.convertDiagramToScreen(new FPoint(x, y1));
                rect.x = p1.x - rect.width / 2;
                rect.y = p1.y;
                rect.height = p2.y - p1.y;
            } else {
                y0 = shape.getY();
                y1 = y0 + 24;
                x = shape.getX();
                p1 = view.convertDiagramToScreen(new FPoint(x, y0));
                p2 = view.convertDiagramToScreen(new FPoint(x, y1));
                rect.x = p1.x - 24;
                rect.y = p1.y;
                rect.height = p2.y - p1.y;
            }
        } else {
            p1 = view.convertDiagramToScreen(bounds.getTopLeft());
            p2 = view.convertDiagramToScreen(bounds.getBottomRight());
            int width = p2.x - p1.x + 48;
            int height = p2.y - p1.y;
            rect = new Rectangle(p1.x - 24, p1.y, width, height);
        }
        rect.y -= 24;
        rect.height += 48;
        view.scrollRectToVisible(rect);
    }
  */
}
