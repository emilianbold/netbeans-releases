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

package org.netbeans.modules.soa.mappercore;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author anjeleevich
 */
public class MapperDivider extends JPanel {
    
    /** Creates a new instance of MapperDivider */
    public MapperDivider() {
        setFocusable(false);
    }
    
    protected void paintComponent(Graphics g) {
        Color c = getBackground();
        
        Color background = c;
        Color outerLine = c.darker().darker();
        Color innerLine = mix(background, 2, outerLine, 1);
        
        int w = getWidth();
        int h = getHeight();

        int y2 = h - 1;
        int x1 = 0;
        int x2 = w - 1;
        
        g.setColor(background);
        g.fillRect(0, 0, w, h);
        
        g.setColor(outerLine);
        g.drawLine(x1, 0, x1, y2);
        g.drawLine(x2, 0, x2, y2);

//        x1++;
//        x2--;
//        
//        g.setColor(innerLine);
//        g.drawLine(x1, 0, x1, y2);
//        g.drawLine(x2, 0, x2, y2);
    }
    
    
    private static Color mix(Color c1, int w1, Color c2, int w2) {
        w1 = Math.max(1, w1);
        w2 = Math.max(1, w2);
        
        int count = w1 + w2;
        int half = count >> 1;
        
        int r = (c1.getRed() * w1 + c2.getRed() * w2 + half) / count;
        int g = (c1.getGreen() * w1 + c2.getGreen() * w2 + half) / count;
        int b = (c1.getBlue() * w1 + c2.getBlue() * w2 + half) / count;
        
        if (r > 255) r = 255; else if (r < 0) r = 0;
        if (g > 255) g = 255; else if (g < 0) g = 0;
        if (b > 255) b = 255; else if (b < 0) b = 0;
        
        return new Color(r, g, b);
    }
    
    
    public void doLayout() {}
    protected void paintChildren(Graphics g) {}
    protected void paintBorder(Graphics g) {}
}
