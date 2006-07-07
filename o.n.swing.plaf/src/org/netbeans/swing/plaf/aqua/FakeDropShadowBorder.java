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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ErrorManager;
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
    
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D gg = (Graphics2D) g;
        g.setColor (Color.WHITE);
        g.fillRect (x + 1, y + 1, w - 2, h - 2);
        
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
//        g.clearRect(x, y, b.getWidth(), b.getHeight());
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
