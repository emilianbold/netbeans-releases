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
/*
 * AquaEditorTabControlBorder.java
 *
 * Created on March 14, 2004, 7:34 PM
 */

package org.netbeans.swing.plaf.aqua;

import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;


/** Border for the embedded control which displays tabs in the tab control.
 * This comprises the upper border of the control.
 *
 * @author  Tim Boudreau
 */
public class AquaEditorTabControlBorder implements Border {
    static int ARCSIZE = 16;

    /** Creates a new instance of AquaViewTabControlBorder */
    public AquaEditorTabControlBorder() {
    }
    
    public Insets getBorderInsets(Component component) {
        return new Insets (1,1,1,1);
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        UIUtils.configureRenderingHints(g);

        Color col = UIUtils.getMiddle(UIManager.getColor("controlShadow"), 
            UIManager.getColor("control"));

        g.setColor(col);

        Graphics2D g2d = (Graphics2D) g;
        int ytop = y + (h / 2) - 1;

        drawLines (g, x, y, ytop, w, h);
        x++;
        ytop++;
        w-=2;
        h-=1;
        g.setColor (UIUtils.getMiddle (col, UIManager.getColor("control"))); //NOI18N
        drawLines (g, x, y, ytop, w, h);
    }

    private void drawLines (Graphics g, int x, int y, int ytop, int w, int h) {
        g.drawArc (x, ytop, ARCSIZE, ARCSIZE, 90, 90);
        g.drawLine(x, ytop+(ARCSIZE/2), x, y+h);

        g.drawArc (x+w-(ARCSIZE+1), ytop, ARCSIZE, ARCSIZE, 90, -90);
        g.drawLine(x+w-1, ytop+(ARCSIZE/2), x+w-1, y+h);
    }
}
