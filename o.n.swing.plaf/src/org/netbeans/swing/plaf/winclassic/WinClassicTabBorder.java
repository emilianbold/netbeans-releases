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
 * WinClassicTabBorder.java
 *
 * Created on March 14, 2004, 8:32 PM
 */

package org.netbeans.swing.plaf.winclassic;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/** Border for the tabs portion of the tab control
 *
 * @author  Dafe Simonek
 */
public class WinClassicTabBorder implements Border {

    private static final Insets insets = new Insets(1, 1, 0, 1);

    public Insets getBorderInsets(Component c) {
        return insets;
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.translate(x, y);
        g.setColor(UIManager.getColor("InternalFrame.borderShadow")); //NOI18N
        g.drawLine(0, 0, width - 2, 0);
        g.drawLine(0, 1, 0, height - 1);
        g.setColor(UIManager.getColor("InternalFrame.borderHighlight")); //NOI18N
        g.drawLine(width - 1, 0, width - 1, height - 1);
        g.translate(-x, -y);
    }
}
