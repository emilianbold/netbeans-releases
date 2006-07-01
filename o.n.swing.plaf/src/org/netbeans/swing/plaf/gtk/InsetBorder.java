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
