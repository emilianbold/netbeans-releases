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
