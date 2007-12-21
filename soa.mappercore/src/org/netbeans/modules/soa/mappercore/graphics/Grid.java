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
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.netbeans.modules.soa.mappercore.*;

/**
 *
 * @author anjeleevich
 */
public class Grid {

    private Image opaqueImage = null;
    private Image image = null;
    private Color background = null;
    
    private int step;
    
    private Color gridColor;
    private int texturePreferredSize;
    
    public Grid() {
        this(Mapper.CANVAS_GRID_COLOR, 160);
    }
    
    
    public Grid(Color gridColor, int texturePreferredSize) {
        this.gridColor = gridColor;
        this.texturePreferredSize = texturePreferredSize;
    }
    
    
    public void paintGrid(Component c, Graphics g, int x0, int y0, int width, 
            int height, int step, boolean opaque) 
    {
        int x2 = x0 + width;
        int y2 = y0 + height;

        Rectangle oldClip = g.getClipBounds();
        int clipX1 = Math.max(x0, oldClip.x);
        int clipY1 = Math.max(y0, oldClip.y);
        int clipX2 = Math.min(x2, oldClip.x + oldClip.width);
        int clipY2 = Math.min(y2, oldClip.y + oldClip.height);
        
        if (clipX1 < clipX2 && clipY1 < clipY2) {
            g.setClip(clipX1, clipY1, clipX2 - clipX1, clipY2 - clipY1);
            
            prepareGridImages(c, step);

            Image image = (opaque) ? this.opaqueImage : this.image;

            int x1 = (int) Math.floor((double) x0 / step) * step; 

            int size = image.getWidth(null);

            for (int y = y0; y < y2; y += size) {
                for (int x = x1; x < x2; x += size) {
                    g.drawImage(image, x, y, null);
                }
            }
            
            g.setClip(oldClip);
        }
    }
    
    
    private void prepareGridImages(Component c, int step) {
        Color background = c.getBackground();
        
        if (image == null || opaqueImage == null || this.step != step ||
                !equal(background, this.background)) {
            
            this.step = step;
            this.background = background;
            
            int size = (texturePreferredSize + step - 1) / step * step;
            
            image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            opaqueImage = c.createImage(size, size);
            
            Graphics g = image.getGraphics();
            g.setColor(gridColor);
            for (int x = 0; x < size; x += step) {
                for (int y = 0; y < size; y += step) {
                    g.drawLine(x, y, x, y);
                }
            }
            g.dispose();
            
            g = opaqueImage.getGraphics();
            g.setColor(background);
            g.fillRect(0, 0, size, size);
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }
    }
    
    
    private boolean equal(Color c1, Color c2) {
        if (c1 == c2) return true;
        if (c1 == null) return false;
        if (c2 == null) return false;
        return c1.equals(c2);
    }
}
