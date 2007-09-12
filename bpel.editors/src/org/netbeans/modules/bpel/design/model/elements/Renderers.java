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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
