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

package org.netbeans.modules.soa.mappercore.graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

/**
 *
 * @author anjeleevich
 */
public class VerticalGradient {
    
    private Color topColor;
    private Color bottomColor;
    
    private Image image;
    
    
    public VerticalGradient(Color topColor, Color bottomColor) {
        this.topColor = topColor;
        this.bottomColor = bottomColor;
    }
    
    
    public void paintGradient(Component c, Graphics g, int x0, int y0, 
            int width, int height) 
    {
        int x2 = x0 + width;
        int y2 = y0 + height;
        
        Rectangle clip = g.getClipBounds();
        if (clip != null) {   // I have case when clip is equal null. In case ToolTip.
            if (Math.max(x0, clip.x) >= Math.min(x2, clip.x + clip.width)) return;
            if (Math.max(y0, clip.y) >= Math.min(y2, clip.y + clip.height)) return;
        }
        
        if (image == null) {
            image = c.createImage(1, 512);
            Graphics2D g2 = (Graphics2D) image.getGraphics();
            g2.setPaint(new GradientPaint(0, 0, topColor, 0, 511, bottomColor));
            g2.fillRect(0, 0, 1, 512);
            g2.dispose();
        }
        
        g.drawImage(image, x0, y0, width, height, null);
    }
}
