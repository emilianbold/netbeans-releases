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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;



public class Renderers {

    public static void renderGradient(Graphics2D g, Shape s) {
        Shape oldClip = g.getClip();
        Paint oldPaint = g.getPaint();

        Rectangle2D bounds = s.getBounds2D();

        Rectangle2D oldClipBounds = oldClip.getBounds2D();

        double x = bounds.getX();
        double y = bounds.getY();
        double h = bounds.getHeight();
        double w = bounds.getWidth();
        
        Rectangle2D newClip = new Rectangle2D.Double();
        
        Rectangle2D gradientClip = new Rectangle2D.Double();
        
        for (int i = 1; i < GRADIENT_Y.length; i++) {
            double y1 = y + h * GRADIENT_Y[i - 1];
            double y2 = y + h * GRADIENT_Y[i];

            gradientClip.setRect(x, y1, w, y2 - y1);
            Rectangle2D.intersect(oldClipBounds, gradientClip, newClip);
            
            g.setClip(newClip);
            g.setPaint(new GradientPaint(
                    (float) x, (float) y1, GRADIENT_COLOR[i - 1],
                    (float) x, (float) y2, GRADIENT_COLOR[i]));
            g.fill(s);
        }
        
        g.setClip(oldClip);
        g.setPaint(oldPaint);
    }
    
    
    public static void renderString(Graphics2D g, String text, 
            float cx, float cy, float height, float maxWidth)
    {
//        g.setFont(FONT);
//        FontMetrics fm = g.getFontMetrics();
//        
//        Rectangle2D stringBounds = fm.getStringBounds(text, g);
//        
//        double scale = height / stringBounds.getHeight();
//        
//        double scaledMaxWidth = maxWidth / scale;
//        if (scaledMaxWidth > maxWidth) {
//        }
//        String end = "...";
//        
//        
    }
    
    
    private static final double[] GRADIENT_Y = {0, 0.0843, 0.1798, 0.7416,
            0.9045, 1.0674};
    
    
    private static final Color[] GRADIENT_COLOR = {
            new Color(0xA9CDE8),
            new Color(0xDDEBF6),
            new Color(0xFFFFFF),
            new Color(0xDCE3EF),
            new Color(0xE7F1F9),
            new Color(0xDCE3EF)
    };    
    
    
    private static final Font FONT = new Font("sans-serif", Font.PLAIN, 20);
}
