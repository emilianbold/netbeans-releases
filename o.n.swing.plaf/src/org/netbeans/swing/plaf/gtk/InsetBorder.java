/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * InsetBorder.java
 *
 * Created on May 5, 2004, 8:11 PM
 */

package org.netbeans.swing.plaf.gtk;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.swing.plaf.util.UIUtils;

/**
 * A trivial border class which centered dividers shorter than the component
 *
 * @author  Tim Boudreau
 */
public class InsetBorder implements Border {
    private boolean left;
    private boolean right;
    
    /** Creates a new instance of InsetBorder */
    public InsetBorder(boolean left, boolean right) {
        this.left = left;
        this.right = right;
    }
    
    public java.awt.Insets getBorderInsets(java.awt.Component c) {
        return new Insets (2, left ? 6 : 2, 0, right ? 6 : 2);
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int h = c.getHeight();
        Color col = g.getColor();
        g.setColor (UIManager.getColor("controlShadow")); //NOI18N
        if (left) {
            g.drawLine (x + 3, y + (h/4), x + 3, y + h - (h/4));
        }
        if (right) {
            g.drawLine (x + width - 3, y + (h/4), x + width - 3, y + h - (h/4));
        }
//        g.setColor (UIUtils.getMiddle(g.getColor(), c.getBackground()));
//        g.drawLine (x, y, x + width, y);
    }
    
}
