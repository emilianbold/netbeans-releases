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
 * EditorToolbarBorder.java
 *
 * Created on March 14, 2004, 4:38 AM
 */

package org.netbeans.swing.plaf.winclassic;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 *
 * @author  David Simonek
 */
class EditorToolbarBorder extends AbstractBorder {
    private static final Insets insets = new Insets(1, 0, 2, 0);

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        g.setColor(UIManager.getColor("InternalFrame.borderShadow")); //NOI18N
        g.drawLine(x, y + h - 2, x + w - 1, y + h - 2);
        g.setColor(UIManager.getColor("InternalFrame.borderDarkShadow")); //NOI18N
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
    }

    public Insets getBorderInsets(Component c) {
        return insets;
    }
}
