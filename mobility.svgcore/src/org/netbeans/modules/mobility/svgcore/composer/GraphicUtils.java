/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */package org.netbeans.modules.mobility.svgcore.composer;

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

    public static boolean isNearLine(float px, float py, 
            float x0, float y0, float x1, float y1, float dist)
    {
        double dsqrt = Math.sqrt(dist);
        if (px + dsqrt < Math.min(x0, x1) || px - dsqrt > Math.max(x0, x1)){
            return false;
        }
        if (py + dsqrt < Math.min(y0, y1) || py - dsqrt > Math.max(y0, y1)){
            return false;
        }
        double d = getPointToLineDistance(px, py, x0, y0, x1, y1);
        return  d*d <= dsqrt;
    }
    
    private static double getPointToLineDistance(float px, float py, 
            float x0, float y0, float x1, float y1)
    {
        float numerator = (y0-y1)*px + (x1-x0)*py + (x0*y1 - x1*y0);
        double denominator = Math.sqrt((x1-x0)*(x1-x0) + (y1-y0)*(y1-y0) );
        return Math.abs(numerator / denominator);
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
