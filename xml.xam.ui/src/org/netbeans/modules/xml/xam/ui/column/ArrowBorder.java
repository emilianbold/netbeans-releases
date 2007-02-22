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

package org.netbeans.modules.xml.xam.ui.column;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.Border;

/**
 * Border that draws an arrow on the right side, pointing to the right.
 *
 * @author  Nathan Fiedler
 */
public class ArrowBorder implements Border {

    /**
     * Creates a new instance of ArrowBorder.
     */
    /**
     * If true arrow will be black, grey otherwise
     */
    private boolean enabled;
    public ArrowBorder(boolean enabled) {
        this.enabled=enabled;
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public Insets getBorderInsets(Component c) {
        // 4 for arrow, plus 8 for padding on either side
        return new Insets(0, 0, 0, 12);
    }

    public void paintBorder(Component c, Graphics g, int x, int y,
            int width, int height) {
        // 4 for arrow, plus 4 for padding on right
        int tx = width - 8;
        int ty = (height - 8) / 2;
        g.translate(tx, ty);
        g.setColor(enabled?Color.BLACK:Color.LIGHT_GRAY);
        g.drawLine(0, 0, 0, 7);
        g.drawLine(1, 1, 1, 6);
        g.drawLine(2, 2, 2, 5);
        g.drawLine(3, 3, 3, 4);
        g.translate(-tx, -ty);
    }
}
