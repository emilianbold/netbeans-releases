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
package org.netbeans.modules.options.ui;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

/**
 *
 * @author Jan Jancura
 */
public class LoweredBorder extends AbstractBorder {

    private Color darker = getLabelBackgroundColor().darker ().darker ();
    private Color brighter = getLabelBackgroundColor().brighter ().brighter ();

    public void paintBorder (
        Component c,
        Graphics g,
        int x, int y, int w, int h
    ) {
        Color oldColor = g.getColor ();
        g.translate (x, y);
        g.setColor (darker);
        g.drawLine (0, 0, 0, h - 1);
        g.drawLine (1, 0, w - 1, 0);
        g.setColor (brighter);
        g.drawLine (1, h - 1, w - 1, h - 1);
        g.drawLine (w - 1, 1, w - 1, h - 2);
        g.translate (-x, -y);
        g.setColor (oldColor);
    }

    public Insets getBorderInsets (Component c) {
	return new Insets (1, 1, 1, 1);
    }

    public boolean isBorderOpaque () { 
        return true; 
    }
    
    private static Color getLabelBackgroundColor() {
        Color retval = new JLabel ().getBackground ();
        return (retval != null) ? retval : UIManager.getDefaults().getColor("Label.background");//NOI18N;
    }
}
