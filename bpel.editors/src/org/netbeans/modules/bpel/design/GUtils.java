/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.design;


import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import org.netbeans.modules.bpel.design.geom.Edge;
import org.netbeans.modules.bpel.design.geom.FEllipse;
import org.netbeans.modules.bpel.design.geom.FPath;
import org.netbeans.modules.bpel.design.geom.FPoint;
import org.netbeans.modules.bpel.design.geom.FRectangle;
import org.netbeans.modules.bpel.design.geom.FRoumb;
import org.netbeans.modules.bpel.design.geom.FRoundRectangle;
import org.netbeans.modules.bpel.design.geom.FShape;
import org.netbeans.modules.bpel.design.geom.Roumb2D;

public class GUtils {
    
    
    private static final JLabel LABEL = new JLabel();

    
    public static Graphics2D createGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
//                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        
        return g2;
    }

    
    public static void draw(Graphics2D g2, Shape shape, boolean normalized) {
        if (normalized) {
            g2.draw(shape);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            g2.draw(shape);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_NORMALIZE);
        }
    }

    
    public static float getScale(AffineTransform at) {
        if (at == null) return 1;
        return (float) Math.hypot(at.getScaleX(), at.getShearY());
    }
    
    
    public static void fill(Graphics2D g2, Shape shape) {
        g2.fill(shape);
    }
    
    
    public static Point2D getNormalizedCenter(Graphics2D g, Shape s) {
//        Rectangle2D r = s.getBounds2D();
//        return new Point2D.Double(r.getCenterX(), r.getCenterY());
        
        AffineTransform at = g.getTransform();
        
        Rectangle2D bounds = s.getBounds2D();
        
        float x = (float) bounds.getX();
        float y = (float) bounds.getY();
        float w = (float) bounds.getWidth();
        float h = (float) bounds.getHeight();
                
        float[] coords = { x, y, x + w, y + h };
        
        Point2D center = null;
        
        try {
            at.transform(coords, 0, coords, 0, 2);
            float cx = 0.5f * (1 + ((int) coords[0]) + ((int) coords[2]));
            float cy = 0.5f * (1 + ((int) coords[1]) + ((int) coords[3]));
            
            center = new Point2D.Float(cx, cy);
            
            at.inverseTransform(center, center);
        } catch (Exception e) {
            center = new Point2D.Double(bounds.getCenterX(), 
                    bounds.getCenterY());
        }

        return center;
    }

    
    public static void setPaint(Graphics2D g, Paint paint) {
        g.setPaint(paint);
    }
    
    
    public static void setSolidStroke(Graphics2D g, double width) {
        double scale = getScale(g.getTransform());

        if (scale * width < 1) {
            width = 1.0 / scale;
        } 
        
        g.setStroke(new BasicStroke((float) width, BasicStroke.CAP_ROUND, 
                BasicStroke.JOIN_ROUND));
    }


    public static void setDashedStroke(Graphics2D g, double width, double dash, 
            double space) 
    {
        double scale = getScale(g.getTransform());

        if (scale * width < 1) {
            width = 1.0 / scale;
        } 
        
        g.setStroke(new BasicStroke((float) width, 
                BasicStroke.CAP_ROUND, 
                BasicStroke.JOIN_ROUND, 1, 
                new float[] { (float) dash, (float) (space + width) }, 0));
    }

    
    public static void setDashedStroke(Graphics2D g, double width, double dash) 
    {
        setDashedStroke(g, width, dash, dash);
    }
    
    
    public static BufferedImage createVerticalGradient(double[] percents, 
            int[] rgbColors) 
    {
        assert (percents != null) && (rgbColors != null) : "Argument is null"; // NOI18N
        assert (percents.length == rgbColors.length) 
                : "Arrays should have same size"; // NOI18N
        assert (percents.length >= 2) : "Array size shoud be greate then 1"; // NOI18N
            
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
        
        return image;
    }
    
    
    public static Shape convert(FShape fShape) {
        return createShape(fShape);
    }

    
    public static Shape convert(FPath path) {
        GeneralPath shape = new GeneralPath();
        
        if (path == null) { 
            return shape;
        }
        
        FPoint last = null;
        
        for (Edge edge : path) {
            FPoint start = edge.getStartPoint();
            FPoint end = edge.getEndPoint();
            
            if ((last == null) || !last.equals(start)) {
                shape.moveTo(start.x, start.y);
            }
            
            shape.lineTo(end.x, end.y);
            last = end;
        }
        
        return shape;
    }
    
    
    public static Shape getTriangle(double x1, double y1, double x2, double y2,
            double x3, double y3) 
    {
        GeneralPath path = new GeneralPath();
        path.moveTo((float) x1, (float) y1);
        path.lineTo((float) x2, (float) y2);
        path.lineTo((float) x3, (float) y3);
        path.closePath();
        
        return path;
    }

    
    public static FRectangle drawString(Graphics2D g2, String string, 
            double x, double y, float width) 
    {
        float zoom = getScale(g2.getTransform());
        
        Font oldFont = g2.getFont();
        Font font = oldFont.deriveFont(oldFont.getSize2D() * zoom);

        g2.translate(x, y);
        g2.scale(1 / zoom, 1 / zoom);
        
        LABEL.setHorizontalAlignment(JLabel.LEFT);
        LABEL.setText(string);
        LABEL.setForeground(g2.getColor());
        LABEL.setFont(font);

        Dimension d = LABEL.getPreferredSize();

        int w = Math.min((int) Math.round(width * zoom), d.width);
        int h = d.height;

        LABEL.setBounds(0, 0, w, h);
        LABEL.paint(g2);
        
        g2.scale(zoom, zoom);
        g2.translate(-x, -y);

        g2.setFont(oldFont);
        
        return new FRectangle((float) x, (float) y, 
                (float) (w / zoom), (float) (h / zoom));
    }
    

    public static FRectangle drawXCenteredString(Graphics2D g2, String text, 
            double cx, double y, double width) 
    {
        float zoom = getScale(g2.getTransform());
        
        Font oldFont = g2.getFont();
        Font font = oldFont.deriveFont(oldFont.getSize2D() * zoom);
        
        LABEL.setHorizontalAlignment(JLabel.CENTER);
        LABEL.setText(text.trim());
        LABEL.setForeground(g2.getColor());
        LABEL.setFont(font);
        
        Dimension d = LABEL.getPreferredSize();
        
        int w = (int) Math.min(Math.round(width * zoom), d.width);
        int h = d.height;

        double x = cx - ((double) w / 2 / zoom);
        
        g2.translate(x, y);
        g2.scale(1 / zoom, 1 / zoom);
        
        LABEL.setBounds(0, 0, w, h);
        LABEL.paint(g2);
        
        g2.scale(zoom, zoom);
        g2.translate(-x, -y);
        g2.setFont(oldFont);
        

        return new FRectangle((float) x, (float) y, 
                (float) (w / zoom), (float) (h / zoom));
    }
    
    

    public static FRectangle drawCenteredString(Graphics2D g2, String string, 
            double cx, double cy, double width) 
    {
        float zoom = getScale(g2.getTransform());

        Font oldFont = g2.getFont();
        Font font = oldFont.deriveFont(oldFont.getSize2D() * zoom);
        
        LABEL.setHorizontalAlignment(JLabel.CENTER);
        LABEL.setText(string);
        LABEL.setFont(font);
        LABEL.setForeground(g2.getColor());
        
        Dimension d = LABEL.getPreferredSize();

        int w = (int) Math.min(Math.round(width * zoom), d.width);
        int h = Math.round(d.height);

        double x = cx - 0.5 * w / zoom;
        double y = cy - 0.5 * h / zoom;
        
        g2.translate(x, y);
        g2.scale(1.0 / zoom, 1.0 / zoom);
        
        LABEL.setBounds(0, 0, w, h);
        LABEL.paint(g2);
        
        g2.scale(zoom, zoom);
        g2.translate(-x, -y);
        g2.setFont(oldFont);
        
        return new FRectangle((float) x, (float) y, 
                (float) (w / zoom), (float) (h / zoom));
    }
    
    
    public static Shape createShape(FShape fShape) {
        if (fShape instanceof FRoundRectangle) {
            FRoundRectangle frr = (FRoundRectangle) fShape;
            return new RoundRectangle2D.Float(frr.x, frr.y, frr.width, frr.height, 
                    frr.rx * 2, frr.ry * 2);
        } 
        
        if (fShape instanceof FEllipse) {
            FEllipse fe = (FEllipse) fShape;
            return new Ellipse2D.Float(fe.x, fe.y, fe.width, fe.height);
        } 
        
        if (fShape instanceof FRoumb) {
            FRoumb fr = (FRoumb) fShape;
            return new Roumb2D.Float(fr.x, fr.y, fr.width, fr.height);
        } 

        if (fShape instanceof FRectangle) {
            FRectangle fr = (FRectangle) fShape;
            return new Rectangle2D.Float(fr.x, fr.y, fr.width, fr.height);
        } 
        
        return null;
    }

    
    public static Area createArea(FShape fShape) {
        return new Area(createShape(fShape));
    }
    
    
    public static void drawGlow(Graphics2D g2, Area area, double width, Color color) {
        Shape oldClip = g2.getClip();
        Stroke oldStroke = g2.getStroke();
        Composite oldComposite = g2.getComposite();
        Paint oldPaint = g2.getPaint();
        
        AffineTransform at = g2.getTransform();
        
        double pixelWidth = getScale(g2.getTransform()) * width;
        
        int steps = Math.min(Math.max(2, (int) Math.floor(pixelWidth / 1.4)), 8);
        
        Area newClip = new Area(oldClip);
        newClip.subtract(area);
        
        g2.setClip(newClip);
        g2.setPaint(color);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                0.25f / steps));

        width *= 2;
        
        for (int i = steps - 1; i >= 0; i--) {
            double t = ((double) (i + 1)) / steps;
            g2.setStroke(new BasicStroke((float) ((Math.pow(2, t) - 1) * width)));
            g2.draw(area);
        }
        
        g2.setComposite(oldComposite);
        g2.setStroke(oldStroke);
        g2.setClip(oldClip);
        g2.setPaint(oldPaint);
    }
}
