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
 * WinClassicCompBorder.java
 *
 * Created on March 14, 2004, 8:34 PM
 */

package org.netbeans.swing.plaf.winclassic;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/** Lower border for the tab control
 *
 * @author  Dafe Simonek
 */
public class WinClassicCompBorder implements Border {

    private static final Insets insets = new Insets(0, 2, 2, 2);

    public Insets getBorderInsets(Component c) {
        return insets;
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int topOffset = 0;
        // TODO - other option is to let the painting of the dark border to the inner component and make this border's insets smaller.
        if (c instanceof JComponent) {
            JComponent jc = (JComponent)c;
            Integer in = (Integer)jc.getClientProperty("MultiViewBorderHack.topOffset");
            topOffset = in == null ? topOffset : in.intValue();
        }
        g.translate(x, y);
        g.setColor(UIManager.getColor("InternalFrame.borderShadow")); //NOI18N
        g.drawLine(0, 0, 0, height - 1); 
        if (topOffset != 0) {
            g.drawLine(1, topOffset - 1, 1, topOffset);
        }
        g.setColor(UIManager.getColor("InternalFrame.borderDarkShadow")); //NOI18N
        g.drawLine(1, topOffset, 1, height - 2);
        g.setColor(UIManager.getColor("InternalFrame.borderHighlight")); //NOI18N
        g.drawLine(1, height - 1, width - 1, height - 1);
        g.drawLine(width - 1, height - 2, width - 1, 0);
        g.setColor(UIManager.getColor("InternalFrame.borderLight")); //NOI18N
        g.drawLine(2, height - 2, width - 2, height - 2);
        g.drawLine(width - 2, height - 3, width - 2, 0);
        g.translate(-x, -y);
    }
    
}
