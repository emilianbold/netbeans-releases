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
 * MetalScrollPaneBorder.java
 *
 * Created on March 14, 2004, 4:33 AM
 */

package org.netbeans.swing.plaf.metal;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/** Scroll pane border for Metal look and feel 
 *
 * @author  Dafe Simonek
 */
class MetalScrollPaneBorder extends AbstractBorder {
        
    private static final Insets insets = new Insets(1, 1, 2, 2);

    public void paintBorder(Component c, Graphics g, int x, int y,
    int w, int h) {
        g.translate(x, y);

        Color color = UIManager.getColor("controlShadow");
        g.setColor(color == null ? Color.darkGray : color);
        g.drawRect(0, 0, w-2, h-2);
        color = UIManager.getColor("controlHighlight");
        g.setColor(color == null ? Color.white : color);
        g.drawLine(w-1, 1, w-1, h-1);
        g.drawLine(1, h-1, w-1, h-1);

        g.translate(-x, -y);
    }

    public Insets getBorderInsets(Component c) {
        return insets;
    }
}
