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
 * "Portions Copyrighted [year] [name of copyright owner]" */
package org.netbeans.swing.plaf.aqua;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.border.Border;

/**
 * Replacement for original DropShadowBorder - uses a set of backing bitmaps
 * to draw shadows instead of allocating a huge raster.
 *
 * @author Tim Boudreau
 */
public class FakeDropShadowBorder implements Border {
    
    public FakeDropShadowBorder() {
    }
    
    private static final int WIDTH = 17;
    private static final int HEIGHT = 17;
    public static final int ARC = 12;
    public Insets getBorderInsets(Component c) {
        return new Insets(1, 13, 25, 13);
    }

    /**
     * Fill the area we are *not* going to paint with the translucent shadow,
     * so the windows behind the popup window do not show through the gaps
     * between the rectangle of the image and the shaped, rounded perimeter  
     * where the shadow is drawn.
     */
    public void fillBackground (Component c, Graphics2D gg,int x, int y, int w, int h) {
        Shape clip = gg.getClip();
        gg.setColor (Color.WHITE);
        Insets ins = getBorderInsets(c);        
        //y offset for the bottom of the window
        int bottom = h - ins.bottom + 6;  //517
        //y offset for the end of the curves around the bottom, from which
        //the edges ascend
        int bottomOffCurve = bottom - 20; //497
        //The top of the inner part of the border
        int top = ins.top + 3; //34
        //The left edge
        int left = ins.left - 2; //41
        //The level of the "shoulders" in the border shape
        int shoulderTop = top + 16; //50
        //The y coordinate at which the edge segment stops and the shoulder
        //curve stops
        int shoulderTopOffCurve = shoulderTop + 9; //59
        //the x position it which the top right curve begins to curl around
        //the right corner
        int rightOffCurve = x + w - 34; //329
        //the right edge of the perimeter
        int right = rightOffCurve + 24; //352
        
        //Calculate a shape to fill, which matches the perceived perimeter of
        //the border (larger than the actual component displayed to make room
        //for our rounded borders
        GeneralPath gp = new GeneralPath();
        //start at the bottom after the left edge curve - first segment is a line
        //upward - the left edge
        gp.moveTo (left, bottomOffCurve);
        gp.lineTo (left, shoulderTopOffCurve);
        //relatively flat bezier curve making the left shoulder of the window border
        gp.curveTo (left, shoulderTop, left + 6, shoulderTop, left + 8, shoulderTop + 1);
        //and steeper, up and around to the top edge
        gp.curveTo (left + 11, top, left + 19, top + 1, left + 25, top);
        //top edge
        gp.lineTo (rightOffCurve, top);
        //steep curve back down to the right shoulder
        gp.curveTo (rightOffCurve + 6, top, rightOffCurve + 17, top + 5, rightOffCurve + 16, shoulderTop);
        //shallower curve out to the right to make the left shoulder
        gp.curveTo (right - 4, shoulderTop + 1, right, shoulderTop, right, shoulderTop + 9);
        //the right edge of the window border
        gp.lineTo (right, bottomOffCurve);
        //curve around to the left to the bottom edge
        gp.curveTo (right + 1, bottom, right - 1, bottom + 1, right - 12, bottom);
        //the bottom edge
        gp.lineTo (left + 14, bottom);
        //curve to the left and up to come back to where we started
        gp.curveTo (left + 1, bottom, left - 1, bottom, left, bottomOffCurve);
        gp.closePath();
        //fill this with white;  the window will overpaint it;  this fills in so
        //we don't see the windows underneath it peeking between the rounded 
        //border's edges and the smaller rectangle of the component inside it
        gg.fill(gp);
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D gg = (Graphics2D) g;
        //Fill in the space between the component rect and the border
        //perimeter
        fillBackground (c, gg, x, y, w, h);
        //Tile the shadow pngs around the shape
        BufferedImage b = getImage(upLeft);
        int yoff = b.getHeight();
        int topL = b.getWidth();
        draw(gg, b, x, y);
        
        b = getImage(downRight);
        draw(gg, b, x + w - b.getWidth(), y + h - b.getHeight());
        int woff = b.getWidth();
        
        b = getImage(upRight);
        draw(gg, b, x + w - b.getWidth(), y);
        int topR = b.getWidth();
        
        b = getImage(downLeft);
        int hoff = b.getHeight();
        int xoff = b.getWidth();
        draw(gg, b, x, y + h - b.getHeight());
        
        b = getImage (leftEdge);
        tileVertical (x, y, yoff, hoff, h, b, gg);
        
        b = getImage (rightEdge);
        tileVertical (x + w - (b.getWidth()), y, yoff, hoff, h, b, gg);
        
        b = getImage (bottom);
        tileHorizontal(x, y + h - (b.getHeight() + 0), xoff, woff, w, b, gg);
        
        b = getImage (top);
        tileHorizontal(x, y, xoff, woff, w, b, gg);
        
    }
    
    private final Color xpar = new Color (255, 255, 255, 0);
    private void draw(Graphics2D g, BufferedImage b, int x, int y) {
        g.setColor (xpar);
        g.fillRect (x, y, b.getWidth(), b.getHeight());
        g.drawRenderedImage(b, AffineTransform.getTranslateInstance(x,y));
    }
    
    private void tileVertical (int x, int y, int yoff, int hoff, int h, BufferedImage img, Graphics2D g) {
        h -= (hoff + yoff);
        int times = h / img.getHeight();
        int rem = h % img.getHeight();
        y = y + yoff;
        
        for (int i=0; i < times; i++) {
            g.drawRenderedImage (img, AffineTransform.getTranslateInstance(x, y));
            y += img.getHeight();
        }
        if (rem > 0) {
            img = img.getSubimage(0, 0, img.getWidth(), rem);
            g.drawRenderedImage(img, AffineTransform.getTranslateInstance(x,y));
        }
    }
    
    private void tileHorizontal (int x, int y, int xoff, int woff, int w, BufferedImage img, Graphics2D g) {
        w -= (woff + xoff);
        int times = w / img.getWidth();
        int rem = w % img.getWidth();
        x += xoff;
        
        for (int i=0; i < times; i++) {
            draw (g, img, x, y);
            x += img.getWidth();
        }
        if (rem > 0) {
            img = img.getSubimage(0, 0, rem, img.getHeight());
            draw (g, img, x, y);
        }
    }
    
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    private static final String upLeft = "upLeft.png"; //NOI18N
    private static final String downRight = "downRight.png"; //NOI18N
    private static final String downLeft = "upRight.png"; //NOI18N
    private static final String upRight = "downLeft.png"; //NOI18N
    private static final String bottom = "bottom.png"; //NOI18N
    private static final String leftEdge = "leftEdge.png"; //NOI18N
    private static final String rightEdge = "rightEdge.png"; //NOI18N
    private static final String top = "top.png";
    
    //Only one instance in VM, so perfectly safe to use instance cache - won't
    //be populated unless used
    private static Map imgs = new HashMap();
    private static BufferedImage getImage(String s) {
        BufferedImage result = (BufferedImage) imgs.get(s);
        if (result == null) {
            Exception e1 = null;
            try {
                result = ImageIO.read(
                        FakeDropShadowBorder.class.getResourceAsStream(s));
            } catch (Exception e) {
                result = new BufferedImage (1, 1, BufferedImage.TYPE_INT_ARGB);
                e1 = e;
            }
            imgs.put (s, result);
            if (e1 != null) {
                throw new IllegalStateException (e1);
            }
        }
        return result;
    }
}
