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
 * AquaRoundedLowerBorder.java
 *
 * Created on March 14, 2004, 7:50 PM
 */

package org.netbeans.swing.plaf.aqua;

import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

/** Border for editor and view tab controls with rounded corners.  Cooperates
 * with DropShadowBorder for floating panels.
 *
 * @author  Tim Boudreau
 */
public class AquaRoundedLowerBorder implements Border {
    static int ARCSIZE = AquaEditorTabControlBorder.ARCSIZE;
    
    /** Creates a new instance of AquaRoundedLowerBorder */
    public AquaRoundedLowerBorder() {
    }

    public Insets getBorderInsets(Component component) {
        return isFloating(component) ? new Insets (0,0,0,0) : new Insets (0,2,3,2);
    }

    public boolean isBorderOpaque() {
        return true;
    }
    
    private boolean isFloating (Component c) {
        boolean result = c.getParent() != null;
        if (result) {
            result = c.getParent().getParent() != null;
        }
        if (result) {
            result = c.getParent().getParent() instanceof JLayeredPane;
        }
        return result;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        if (isFloating(c)) {
            return;
        }
        UIUtils.configureRenderingHints(g);
        int halfArc = ARCSIZE/2;
        
        Color col = UIUtils.getMiddle(UIManager.getColor("controlShadow"), 
            UIManager.getColor("control"));

        g.setColor(col);
        g.drawLine(x, y, x, y+h-halfArc);
        g.drawLine(x+w-1, y, x+w-1, y+h-halfArc);

        g.drawArc (x, y+h-(ARCSIZE+1), ARCSIZE, ARCSIZE, 180, 90);
        g.drawArc (x+w-(ARCSIZE+1), y+h-(ARCSIZE+1), ARCSIZE, ARCSIZE, 270, 90);

        g.drawLine (x+(ARCSIZE/2)-3, y+h-1, x+w-(ARCSIZE/2), y+h-1);
        
    }
}
