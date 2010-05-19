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

package org.netbeans.modules.bpel.design.decoration.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;


public class ButtonRenderer {
    
    public static void paintButton(Component c, Graphics g, Color fillColor, 
            boolean gradient, Color strokeColor, float strokeWidth, Icon icon)
    {
        if ((fillColor == null) && (strokeColor == null) && (icon == null)) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        float arcSize = Math.max(0, ARC_SIZE - strokeWidth);
        float strokeHalf = strokeWidth / 2;
        
        int w = c.getWidth();
        int h = c.getHeight();
        
        Shape shape = new RoundRectangle2D.Float(strokeHalf, strokeHalf, 
                w - strokeWidth, h - strokeWidth, arcSize, arcSize);
        
        if (fillColor != null) {
            if (gradient && strokeColor != null) {
                g2.setPaint(fillColor);
                g2.fill(shape);
                
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                        RenderingHints.VALUE_STROKE_NORMALIZE);
                
                int rv = strokeColor.getRed();
                int gv = strokeColor.getGreen();
                int bv = strokeColor.getBlue();
                
                g2.setPaint(new Color(rv, gv, bv, 255 * 3 / 8));
                g2.drawLine(1, h - 2, w - 2, h - 2);
                
                g2.setPaint(new Color(rv, gv, bv, 255 / 4));
                g2.drawLine(2, h - 3, w - 3, h - 3);
                
                g2.setPaint(new Color(rv, gv, bv, 255 / 8));
                g2.drawLine(3, h - 4, w - 4, h - 4);

                g2.setPaint(new Color(rv, gv, bv, 255 / 4));
                g2.drawLine(w - 2, 1, w - 2, h - 2);
                
                g2.setPaint(new Color(rv, gv, bv, 255 / 8));
                g2.drawLine(w - 3, 2, w - 3, h - 3);

                g2.setPaint(new Color(rv, gv, bv, 255 / 16));
                g2.drawLine(w - 4, 3, w - 4, h - 4);
                
                g2.setPaint(new Color(255, 255, 255, 255 * 3 / 4));
                g2.drawLine(1, 1, w - 2, 1);
                g2.drawLine(1, 1, 1, h - 2);

                g2.setPaint(new Color(255, 255, 255, 255 / 2));
                g2.drawLine(2, 2, w - 3, 2);
                g2.drawLine(2, 2, 2, h - 3);

                g2.setPaint(new Color(255, 255, 255, 255 / 4));
                g2.drawLine(3, 3, w - 4, 3);
                g2.drawLine(3, 3, 3, h - 4);
                
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                        RenderingHints.VALUE_STROKE_PURE);
            } else {
                g2.setPaint(fillColor);
                g2.fill(shape);
            }
        }
        
        if (strokeColor != null) {
            g2.setStroke(new BasicStroke(strokeWidth));
            g2.setPaint(strokeColor);
            g2.draw(shape);
        }
        
        if (icon != null) {
            int x = (w - icon.getIconWidth()) / 2;
            int y = (h - icon.getIconHeight()) / 2;
            
            icon.paintIcon(c, g, x, y);
        }
        
        g2.dispose();
    }

    
    public static Icon createDisabledIcon(Component c, Icon icon) {
        Image iconImage;
        
        if (icon instanceof ImageIcon) {
            iconImage = ((ImageIcon) icon).getImage();
        } else {
            iconImage = new BufferedImage(icon.getIconWidth(), 
                    icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            icon.paintIcon(c, iconImage.getGraphics(), 0, 0);
        }
        
        return new ImageIcon(GrayFilter.createDisabledImage(iconImage));
    }
    
    
    public static final float ARC_SIZE = 8;
    
    public static final Color NORMAL_BORDER_COLOR = new Color(0x999999);
    public static final Color NORMAL_FILL_COLOR = null;
    public static final float NORMAL_STROKE_WIDTH = 1;

    public static final Color DISABLED_BORDER_COLOR = new Color(0xCCCCCC);
    public static final Color DISABLED_FILL_COLOR = null;
    public static final float DISABLED_STROKE_WIDTH = 1;
    
    public static final Color PRESSED_BORDER_COLOR = new Color(0x999999);
    public static final Color PRESSED_FILL_COLOR = new Color(0xDDDDDD);
    public static final float PRESSED_STROKE_WIDTH = 1.3f;

    public static final Color ROLLOVER_BORDER_COLOR = new Color(0x999999);
    public static final Color ROLLOVER_FILL_COLOR = new Color(0xF4F4F4);
    public static final float ROLLOVER_STROKE_WIDTH = 1f;    
}
