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

package org.netbeans.modules.soa.ui.tnv.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * This class is a wrapper over a standard Graphics2D.
 * In most cases it do nothing self and pass method calls to
 * the wrapped graphics object.
 *
 * It overrides some setters to make painting faster.
 * For example, it prevent drawing dashed lines and draw solid lines instead.
 *
 * // TODO prevent the gradient fill. 
 *
 * @author nk160297
 */
public class ThumbnailGraphics extends Graphics2D {
    
    private Graphics2D graphicsDelegate;
    
    /** Creates a new instance of ThumbnailGraphics */
    public ThumbnailGraphics(Graphics2D g) {
        graphicsDelegate = g;
    }
    
    public Graphics create() {
        return new ThumbnailGraphics((Graphics2D)graphicsDelegate.create());
    }
    
    public void setStroke(Stroke newStroke) {
        graphicsDelegate.setStroke(simplifyStroke(newStroke));
    }
    
    private Stroke simplifyStroke(Stroke stroke)  {
        Stroke result = stroke;
        //
        if (stroke instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke)stroke;
            float[] da = bs.getDashArray();
            if (da != null) {
                Stroke simpleStroke = new BasicStroke(
                        bs.getLineWidth(), bs.getEndCap(),
                        bs.getLineJoin(), bs.getMiterLimit());
                result = simpleStroke;
            }
        }
        return result;
    }
    
    //================================================================
    
    public Stroke getStroke() {
        return graphicsDelegate.getStroke();
    }
    
    public void draw(Shape arg0) {
        graphicsDelegate.draw(arg0);
    }
    
    public boolean drawImage(Image arg0, AffineTransform arg1,
            ImageObserver arg2) {
        return graphicsDelegate.drawImage(arg0, arg1, arg2);
    }
    
    public void drawImage(BufferedImage arg0, BufferedImageOp arg1, int arg2,
            int arg3) {
        graphicsDelegate.drawImage(arg0, arg1, arg2, arg3);
    }
    
    public void drawRenderedImage(RenderedImage arg0, AffineTransform arg1) {
        graphicsDelegate.drawRenderedImage(arg0, arg1);
    }
    
    public void drawRenderableImage(RenderableImage arg0, AffineTransform arg1) {
        graphicsDelegate.drawRenderableImage(arg0, arg1);
    }
    
    public void drawString(String arg0, int arg1, int arg2) {
        graphicsDelegate.drawString(arg0, arg1, arg2);
    }
    
    public void drawString(String arg0, float arg1, float arg2) {
        graphicsDelegate.drawString(arg0, arg1, arg2);
    }
    
    public void drawString(AttributedCharacterIterator arg0, int arg1, int arg2) {
        graphicsDelegate.drawString(arg0, arg1, arg2);
    }
    
    public void drawString(AttributedCharacterIterator arg0, float arg1, float arg2) {
        graphicsDelegate.drawString(arg0, arg1, arg2);
    }
    
    public void drawGlyphVector(GlyphVector arg0, float arg1, float arg2) {
        graphicsDelegate.drawGlyphVector(arg0, arg1, arg2);
    }
    
    public void fill(Shape arg0) {
        graphicsDelegate.fill(arg0);
    }
    
    public boolean hit(Rectangle arg0, Shape arg1, boolean arg2) {
        return graphicsDelegate.hit(arg0, arg1, arg2);
    }
    
    public GraphicsConfiguration getDeviceConfiguration() {
        return graphicsDelegate.getDeviceConfiguration();
    }
    
    public void setComposite(Composite arg0) {
        graphicsDelegate.setComposite(arg0);
    }
    
    public void setPaint(Paint arg0) {
        graphicsDelegate.setPaint(arg0);
    }
    
    public void setRenderingHint(Key arg0, Object arg1) {
        graphicsDelegate.setRenderingHint(arg0, arg1);
    }
    
    public Object getRenderingHint(Key arg0) {
        return graphicsDelegate.getRenderingHint(arg0);
    }
    
    public void setRenderingHints(Map<?, ?> arg0) {
        graphicsDelegate.setRenderingHints(arg0);
    }
    
    public void addRenderingHints(Map<?, ?> arg0) {
        graphicsDelegate.addRenderingHints(arg0);
    }
    
    public RenderingHints getRenderingHints() {
        return graphicsDelegate.getRenderingHints();
    }
    
    public void translate(int arg0, int arg1) {
        graphicsDelegate.translate(arg0, arg1);
    }
    
    public void translate(double arg0, double arg1) {
        graphicsDelegate.translate(arg0, arg1);
    }
    
    public void rotate(double arg0) {
        graphicsDelegate.rotate(arg0);
    }
    
    public void rotate(double arg0, double arg1, double arg2) {
        graphicsDelegate.rotate(arg0, arg1, arg2);
    }
    
    public void scale(double arg0, double arg1) {
        graphicsDelegate.scale(arg0, arg1);
    }
    
    public void shear(double arg0, double arg1) {
        graphicsDelegate.shear(arg0, arg1);
    }
    
    public void transform(AffineTransform arg0) {
        graphicsDelegate.transform(arg0);
    }
    
    public void setTransform(AffineTransform arg0) {
        graphicsDelegate.setTransform(arg0);
    }
    
    public AffineTransform getTransform() {
        return graphicsDelegate.getTransform();
    }
    
    public Paint getPaint() {
        return graphicsDelegate.getPaint();
    }
    
    public Composite getComposite() {
        return graphicsDelegate.getComposite();
    }
    
    public void setBackground(Color arg0) {
        graphicsDelegate.setBackground(arg0);
    }
    
    public Color getBackground() {
        return graphicsDelegate.getBackground();
    }
    
    public void clip(Shape arg0) {
        graphicsDelegate.clip(arg0);
    }
    
    public FontRenderContext getFontRenderContext() {
        return graphicsDelegate.getFontRenderContext();
    }
    
    public Color getColor() {
        return graphicsDelegate.getColor();
    }
    
    public void setColor(Color arg0) {
        graphicsDelegate.setColor(arg0);
    }
    
    public void setPaintMode() {
        graphicsDelegate.setPaintMode();
    }
    
    public void setXORMode(Color arg0) {
        graphicsDelegate.setXORMode(arg0);
    }
    
    public Font getFont() {
        return graphicsDelegate.getFont();
    }
    
    public void setFont(Font arg0) {
        graphicsDelegate.setFont(arg0);
    }
    
    public FontMetrics getFontMetrics(Font arg0) {
        return graphicsDelegate.getFontMetrics(arg0);
    }
    
    public Rectangle getClipBounds() {
        return graphicsDelegate.getClipBounds();
    }
    
    public void clipRect(int arg0, int arg1, int arg2, int arg3) {
        graphicsDelegate.clipRect(arg0, arg1, arg2, arg3);
    }
    
    public void setClip(int arg0, int arg1, int arg2, int arg3) {
        graphicsDelegate.setClip(arg0, arg1, arg2, arg3);
    }
    
    public Shape getClip() {
        return graphicsDelegate.getClip();
    }
    
    public void setClip(Shape arg0) {
        graphicsDelegate.setClip(arg0);
    }
    
    public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5) {
        graphicsDelegate.copyArea(arg0, arg1, arg2, arg3, arg4, arg5);
    }
    
    public void drawLine(int arg0, int arg1, int arg2, int arg3) {
        graphicsDelegate.drawLine(arg0, arg1, arg2, arg3);
    }
    
    public void fillRect(int arg0, int arg1, int arg2, int arg3) {
        graphicsDelegate.fillRect(arg0, arg1, arg2, arg3);
    }
    
    public void clearRect(int arg0, int arg1, int arg2, int arg3) {
        graphicsDelegate.clearRect(arg0, arg1, arg2, arg3);
    }
    
    public void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5) {
        graphicsDelegate.drawRoundRect(arg0, arg1, arg2, arg3, arg4, arg5);
    }
    
    public void fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5) {
        graphicsDelegate.fillRoundRect(arg0, arg1, arg2, arg3, arg4, arg5);
    }
    
    public void drawOval(int arg0, int arg1, int arg2, int arg3) {
        graphicsDelegate.drawOval(arg0, arg1, arg2, arg3);
    }
    
    public void fillOval(int arg0, int arg1, int arg2, int arg3) {
        graphicsDelegate.fillOval(arg0, arg1, arg2, arg3);
    }
    
    public void drawArc(int arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5) {
        graphicsDelegate.drawArc(arg0, arg1, arg2, arg3, arg4, arg5);
    }
    
    public void fillArc(int arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5) {
        graphicsDelegate.fillArc(arg0, arg1, arg2, arg3, arg4, arg5);
    }
    
    public void drawPolyline(int[] arg0, int[] arg1, int arg2) {
        graphicsDelegate.drawPolyline(arg0, arg1, arg2);
    }
    
    public void drawPolygon(int[] arg0, int[] arg1, int arg2) {
        graphicsDelegate.drawPolygon(arg0, arg1, arg2);
    }
    
    public void fillPolygon(int[] arg0, int[] arg1, int arg2) {
        graphicsDelegate.fillPolygon(arg0, arg1, arg2);
    }
    
    public boolean drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
        return graphicsDelegate.drawImage(arg0, arg1, arg2, arg3);
    }
    
    public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4,
            ImageObserver arg5) {
        return graphicsDelegate.drawImage(arg0, arg1, arg2, arg3, arg4, arg5);
    }
    
    public boolean drawImage(Image arg0, int arg1, int arg2, Color arg3,
            ImageObserver arg4) {
        return graphicsDelegate.drawImage(arg0, arg1, arg2, arg3, arg4);
    }
    
    public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4,
            Color arg5, ImageObserver arg6) {
        return graphicsDelegate.drawImage(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
    }
    
    public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5, int arg6, int arg7, int arg8,
            ImageObserver arg9) {
        return graphicsDelegate.drawImage(arg0, arg1, arg2, arg3, arg4,
                arg5, arg6, arg7, arg8, arg9);
    }
    
    public boolean drawImage(Image arg0, int arg1, int arg2, int arg3, int arg4,
            int arg5, int arg6, int arg7, int arg8, Color arg9,
            ImageObserver arg10) {
        return graphicsDelegate.drawImage(arg0, arg1, arg2, arg3, arg4,
                arg5, arg6, arg7, arg8, arg9, arg10);
    }
    
    public void dispose() {
        graphicsDelegate.dispose();
    }
    
    
}
