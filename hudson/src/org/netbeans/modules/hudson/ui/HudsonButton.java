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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.ui;

import java.awt.Cursor;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/**
 * Decorated button
 * 
 * @author Michal Mocnak
 */
public class HudsonButton extends JButton {
    
    private ImageIcon ICON_ON;
    private ImageIcon ICON_OFF;
    
    private boolean active = false;
    
    public HudsonButton(String iconBaseOn, String iconBaseOff) {
        ICON_ON = new ImageIcon(getClass().getResource(iconBaseOn));
        ICON_OFF = new ImageIcon(getClass().getResource(iconBaseOff));
        
        setBorder(new EmptyBorder(1, 1, 1, 1));
        setMargin(new Insets(0, 0, 0, 0));
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                active = true;
                if (isEnabled())
                    repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                active = false;
                if (isEnabled())
                    repaint();
            }
        });
    }
    
    @Override
    public void paint(Graphics g) {
        if (!isEnabled())
            return;
        
        Graphics2D g2 = org.netbeans.modules.hudson.util.Utilities.prepareGraphics( g );
        
        // Select image
        ImageIcon i = (active) ? ICON_ON : ICON_OFF;
        
        // Draw image
        g2.drawImage(i.getImage(), 0, 0, i.getIconWidth(), i.getIconHeight(), null);
        
        // Paint components
        super.paint(g);
    }
}