/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.options.ui;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.border.AbstractBorder;


/**
 *
 * @author Jan Jancura
 */
public class LoweredBorder extends AbstractBorder {

    private Color darker = new JLabel ().getBackground ().darker ().darker ();
    private Color brighter = new JLabel ().getBackground ().brighter ().brighter ();
    
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
}
