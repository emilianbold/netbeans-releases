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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.netbeans.installer.wizard.WizardFrame;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiDialog extends JDialog {
    private NbiFrame owner;
    
    public NbiDialog(NbiFrame owner) {
        super(owner);
        
        this.owner = owner;
        
        initComponents();
    }
    
    private void initComponents() {
        setContentPane(new ContentPane());
    }
    
    public void setVisible(boolean visible) {
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        
        setLocation(((screenWidth - getSize().width) / 2) + 50, ((screenHeight - getSize().height) / 2) + 50);
        
        super.setVisible(visible);
    }
    
    public class ContentPane extends JPanel {
        private Image backgroundImage;
        
        public ContentPane() {
            backgroundImage = org.netbeans.installer.utils.helper.swing.NbiDialog.this.owner.getBackgroundImage();
        }
        
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            
            Graphics2D graphics2d = (Graphics2D) graphics;
            
            if (backgroundImage != null) {
                graphics2d.drawImage(backgroundImage, 0, 0, this);
            }
            
            Composite oldComposite = graphics2d.getComposite();
            
            graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            graphics2d.setColor(Color.WHITE);
            graphics2d.fillRect(0, 0, this.getWidth(), this.getHeight());
            
            graphics2d.setComposite(oldComposite);
        }
    }
}
