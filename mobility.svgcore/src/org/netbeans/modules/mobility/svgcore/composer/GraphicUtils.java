/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */
package org.netbeans.modules.mobility.svgcore.composer;

import com.sun.perseus.j2d.Box;
import java.awt.Color;
import java.awt.Graphics;
import org.w3c.dom.svg.SVGRect;

/**
 *
 * @author Pavel Benes
 */
public abstract class GraphicUtils {
    
    public static void drawRoundSelectorCorner(Graphics g, Color outline, Color body,
                                               int x, int y, int size) {
        g.setColor(outline);
        g.fillOval(x - size,y - size,size * 2 + 1,size * 2 + 1);
        g.setColor(body);
        g.drawOval(x - (size - 1),y - (size - 1),(size - 1) * 2,(size - 1) * 2);
    }

    public static void drawDiamondSelectorCorner(Graphics g, Color outline, Color body,
                                               int x, int y, int size) {
        int [] xs = new int[] {x, x + size, x, x - size};
        int [] ys = new int[] {y - size, y, y + size, y};
        
        g.setColor(body);
        g.fillPolygon(xs, ys, xs.length);
        g.setColor(outline);
        g.drawPolygon(xs, ys, xs.length);
    }
    
    public static boolean areNear(float x1, float y1, float x2, float y2, float dist) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return  dx*dx + dy*dy <= dist;
    }

    public static boolean areEqual(SVGRect rect1, SVGRect rect2) {
        if (rect1 == rect2) {
            return true;
        } else if ( rect1 == null || rect2 == null) {
            return false;
        } else {
            return rect1.getX() == rect2.getX() &&
                   rect1.getY() == rect2.getY() &&
                   rect1.getWidth() == rect2.getWidth() &&
                   rect1.getHeight() == rect2.getHeight();
        }
    }
    
    public static SVGRect scale(SVGRect rect, float scale) {
        return new Box( rect.getX() * scale, rect.getY() * scale,
                            rect.getWidth() * scale, rect.getHeight() * scale);
    }
    public static float calcAngle(float x1, float y1, float x2, float y2) {
        float dx = x2-x1;
        float dy = y2-y1;
        double angle=0.0d;
 
        // Calculate angle
        if (dx == 0.0)
        {
            if (dy == 0.0)
                angle = 0.0;
            else if (dy > 0.0)
                angle = Math.PI / 2.0;
            else
                angle = Math.PI * 3.0 / 2.0;
        }
        else if (dy == 0.0)
        {
            if  (dx > 0.0)
                angle = 0.0;
            else
                angle = Math.PI;
        }
        else
        {
            if  (dx < 0.0)
                angle = Math.atan(dy/dx) + Math.PI;
            else if (dy < 0.0)
                angle = Math.atan(dy/dx) + (2*Math.PI);
            else
                angle = Math.atan(dy/dx);
        }
 
        // Convert to degrees
        angle = angle * 180 / Math.PI;
 
        // Return
        return (float) angle;
    }    
}
