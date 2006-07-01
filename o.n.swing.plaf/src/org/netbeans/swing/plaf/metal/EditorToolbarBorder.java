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
 * EditorToolbarBorder.java
 *
 * Created on March 14, 2004, 4:38 AM
 */

package org.netbeans.swing.plaf.metal;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 *
 * @author  David Simonek
 */
public class EditorToolbarBorder extends AbstractBorder {
    private static final Insets insets = new Insets(0, 0, 1, 0);

    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Color borderC = UIManager.getColor("controlDarkShadow");
        g.setColor(borderC);
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
    }

    public Insets getBorderInsets(Component c) {
        return insets;
    }    
}
