/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.ui;


import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.SystemColor;
import javax.swing.border.Border;


/**
 *
 * @author Jan Jancura
 */
public class DashedBorder implements Border {
    
    private Insets insets = new Insets (1, 1, 1, 1);
    
    
    public void paintBorder (
        Component c, 
        Graphics g, 
        int x, int y, int width, int height
    ) {
        int vx,vy;
        Color old = g.getColor ();
        g.setColor (Color.black);

        // draw upper and lower horizontal dashes
        for (vx = x; vx < (x + width); vx+=2) {
            g.fillRect(vx, y, 1, 1);
            g.fillRect(vx, y + height-1, 1, 1);
        }

        // draw left and right vertical dashes
        for (vy = y; vy < (y + height); vy+=2) {
	    g.fillRect(x, vy, 1, 1);
            g.fillRect(x+width-1, vy, 1, 1);
        }
        g.setColor (old);
    }

    public Insets getBorderInsets (Component c) {
        return insets;
    }

    public boolean isBorderOpaque () {
        return true;
    }
}
