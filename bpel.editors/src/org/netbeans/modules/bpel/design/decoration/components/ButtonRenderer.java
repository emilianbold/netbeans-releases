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
