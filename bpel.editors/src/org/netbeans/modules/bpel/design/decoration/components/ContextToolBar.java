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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;
import javax.swing.border.Border;


public class ContextToolBar extends JPanel implements
        DecorationComponent {
    
    public ContextToolBar() {
        super(new GridLayout(1, 0, 2, 0));
        setBorder(new MyBorder());
        setBackground(null);
        setOpaque(false);
        
       super.toString();
    }
    private class MyBorder implements Border {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            
            float arcSize = Math.max(0, ARC_SIZE - STROKE_WIDTH);
            float strokeHalf = STROKE_WIDTH / 2;
            
            Shape shape = new RoundRectangle2D.Float(x + strokeHalf, y + strokeHalf,
                    w - STROKE_WIDTH, h - STROKE_WIDTH, arcSize, arcSize);
            
            if (FILL_PAINT != null) {
                g2.setPaint(FILL_PAINT);
                g2.fill(shape);
            }
            
            if (STROKE_PAINT != null) {
                g2.setPaint(STROKE_PAINT);
                g2.draw(shape);
            }
            
            g2.dispose();
        }
        
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 3, 2, 3);
        }
        
        public boolean isBorderOpaque() {
            return false;
        }
        
    }
    
    

    private static final float ARC_SIZE = 8;
    private static final float STROKE_WIDTH = 1;
    private static final Paint STROKE_PAINT = new Color(0xCCCCCC);
    private static final Paint FILL_PAINT = new Color(0xCCFFFFFF, true);
}
