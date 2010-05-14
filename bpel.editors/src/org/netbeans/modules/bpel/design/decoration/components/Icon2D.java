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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 *
 * @author aa160298
 */
public abstract class Icon2D {


    protected double getDesignOriginX() { return 0; }
    protected double getDesignOriginY() { return 0; }

    protected abstract double getDesignWidth();
    protected abstract double getDesignHeight();


    public final void paint(Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        
        Object oldAntialiasing = g2.getRenderingHint(
                RenderingHints.KEY_ANTIALIASING);
        Object oldStrokeControl = g2.getRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL);

        Stroke oldStroke = g2.getStroke();
        Paint oldPaint = g2.getPaint();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        double desWidth = getDesignWidth();
        double desHeight = getDesignHeight();
        
        double cx = x + 0.5 * w;
        double cy = y + 0.5 * h;
        
        double scale = Math.min((double) w / desWidth, (double) h / desHeight);
        double backScale = 1.0 / scale;
        
        double tx = -desWidth / 2 + getDesignOriginX();
        double ty = -desHeight / 2 + getDesignOriginY();
        
        g2.translate(cx, cy);
        g2.scale(scale, scale);
        g2.translate(tx, ty);
        
        paint(g2);

        g2.translate(-tx, -ty);
        g2.scale(backScale, backScale);
        g2.translate(-cx, -cy);
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                oldAntialiasing);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                oldStrokeControl);
        g2.setStroke(oldStroke);
        g2.setPaint(oldPaint);
    }
    
    
    protected abstract void paint(Graphics2D g2);
}
