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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Line2D;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.Border;

/**
 * VariableBorder is a simple Border that only draw a line if a color is given.
 *
 * @author Christopher Atlan
 */
public class VariableBorder implements Border {
    private Color topColor;
    private Color leftColor;
    private Color bottomColor;
    private Color rightColor;
    
    /** Creates a new instance of VariableBorder */
    public VariableBorder(final Color topColor,
            final Color leftColor,
            final Color bottomColor,
            final Color rightColor) {
        this.topColor = topColor;
        this.leftColor = leftColor;
        this.bottomColor = bottomColor;
        this.rightColor = rightColor;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D)g;
        Shape s;
        
        if(topColor != null) {
            s = new Line2D.Double(x,y,x+width, y);
            g2d.setColor(topColor);
            g2d.fill(s);
        }
        
        if(leftColor != null) {
            s = new Line2D.Double(x,y,x, y+height);
            g2d.setColor(leftColor);
            g2d.fill(s);
        }
        
        if(bottomColor != null) {
            s = new Line2D.Double(x,y+height-1,x+width, y+height-1);
            g2d.setColor(bottomColor);
            g2d.fill(s);
        }
        
        if(rightColor != null) {
            s = new Line2D.Double(x+width-1,y,x+width-1, y+height);
            g2d.setColor(rightColor);
            g2d.fill(s);
        }
        

    }
    
    public Insets getBorderInsets(Component c) {
        Insets i = new Insets(0, 0, 0, 0);
        
        if(topColor != null)
            i.top = 1;
        
        if(leftColor != null)
            i.left = 1;
        
        if(bottomColor != null)
            i.bottom = 1;
        
        if(rightColor != null)
            i.right = 1;
        
        if (c instanceof JToolBar) {
            Insets toolBarInsets = ((JToolBar)c).getMargin();
            i.top += toolBarInsets.top;
            i.left += toolBarInsets.left;
            i.right += toolBarInsets.right;
            i.bottom += toolBarInsets.bottom;
        }
        
        if (c instanceof JToggleButton) {
            Insets buttonInsets = ((JToggleButton)c).getMargin();
            i.top += buttonInsets.top;
            i.left += buttonInsets.left;
            i.right += buttonInsets.right;
            i.bottom += buttonInsets.bottom;
        }
        
        return i;
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
}
