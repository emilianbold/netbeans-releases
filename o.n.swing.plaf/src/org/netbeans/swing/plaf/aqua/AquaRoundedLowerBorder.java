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
 * AquaRoundedLowerBorder.java
 *
 * Created on March 14, 2004, 7:50 PM
 */

package org.netbeans.swing.plaf.aqua;

import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/** Border for editor and view tab controls with rounded corners
 *
 * @author  Tim Boudreau
 */
public class AquaRoundedLowerBorder implements Border {
    static int ARCSIZE = AquaEditorTabControlBorder.ARCSIZE;
    
    /** Creates a new instance of AquaRoundedLowerBorder */
    public AquaRoundedLowerBorder() {
    }

    public Insets getBorderInsets(Component component) {
        return new Insets (1,2,3,2);
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        UIUtils.configureRenderingHints(g);

        Color col = UIUtils.getMiddle(UIManager.getColor("controlShadow"), 
            UIManager.getColor("control"));

        g.setColor(col);
        g.drawLine(x, y, x, y+h-(ARCSIZE / 2));
        g.drawLine(x+w-1, y, x+w-1, y+h-(ARCSIZE / 2));

        g.drawArc (x, y+h-ARCSIZE, ARCSIZE, ARCSIZE, 180, 90);
        g.drawArc (x+w-(ARCSIZE+1), y+h-(ARCSIZE+1), ARCSIZE, ARCSIZE, 270, 90);

        g.drawLine (x+(ARCSIZE/2)-3, y+h-1, x+w-(ARCSIZE/2), y+h-1);
    }
}
