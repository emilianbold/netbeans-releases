/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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