/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author anjeleevich
 */
public class BgBorder implements Border {

    private Insets borderWidth;
    private Insets padding;
    
    private Color borderColor;
    private Color fillColor;
    
    public BgBorder(int vBorderWidth, int hBorderWidth, 
            int vPadding, int hPadding, Color borderColor, Color fillColor) 
    {
        this.borderWidth = new Insets(vBorderWidth, hBorderWidth, 
                vBorderWidth, hBorderWidth);
        this.padding = new Insets(vPadding, hPadding, vPadding, hPadding);
        
        this.borderColor = borderColor;
        this.fillColor = fillColor;
    }
    
    
    public BgBorder(Insets borderWidth, Insets padding, 
            Color borderColor, Color fillColor) 
    {
        this.borderWidth = (Insets) borderWidth.clone();
        this.padding = (Insets) padding.clone();
        
        this.borderColor = borderColor;
        this.fillColor = fillColor;
    }

    
    public Insets getInsets() {
        return new Insets(
                borderWidth.top    + padding.top, 
                borderWidth.left   + padding.left,
                borderWidth.bottom + padding.bottom, 
                borderWidth.right  + padding.right);
    }

    public void paint(Graphics2D g2, Rectangle rect) {
        Paint oldPaint = g2.getPaint();
        
        if (borderColor != null) {
            g2.setPaint(borderColor);
            g2.fill(rect);
        }
        
        if (fillColor != null) {
            g2.setPaint(fillColor);
            g2.fillRect(rect.x + borderWidth.left, rect.y + borderWidth.top,
                    rect.width - borderWidth.left - borderWidth.right,
                    rect.height - borderWidth.top - borderWidth.bottom);
        }
        
        g2.setPaint(oldPaint);
    }
    
    public boolean isOpaque() {
        return true;
    }
}
