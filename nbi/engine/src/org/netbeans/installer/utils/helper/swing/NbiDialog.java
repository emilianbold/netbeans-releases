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
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.net.URL;
import javax.swing.JDialog;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiDialog extends JDialog {
    protected NbiFrame owner;
    
    protected int dialogWidth;
    protected int dialogHeight;
    protected URL dialogIcon;
    
    protected NbiDialogContentPane contentPane;
    
    public NbiDialog() {
        super();
        
        initComponents();
    }
    
    public NbiDialog(NbiFrame owner) {
        super(owner);
        
        this.owner = owner;
        
        initComponents();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        contentPane = new NbiDialogContentPane();
        setContentPane(contentPane);
        
        setSize(DEFAULT_DIALOG_WIDTH, DEFAULT_DIALOG_HEIGHT);
    }
    
    public void setVisible(boolean visible) {
        if (owner == null) {
            final GraphicsDevice screen = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().
                    getScreenDevices()[0];
            final GraphicsConfiguration config = screen.getDefaultConfiguration();
            
            final int screenWidth  = config.getBounds().width;
            final int screenHeight = config.getBounds().height;
            
            setLocation(
                    (screenWidth - getSize().width) / 2,
                    (screenHeight - getSize().height) / 2);
        } else {
            setLocation(
                    owner.getLocation().x + DIALOG_FRAME_WIDTH_DELTA,
                    owner.getLocation().y + DIALOG_FRAME_WIDTH_DELTA);
        }
        
        super.setVisible(visible);
    }
    
    public class NbiDialogContentPane extends NbiPanel {
        private Image backgroundImage;
        
        public NbiDialogContentPane() {
            if (NbiDialog.this.owner != null) {
                backgroundImage = NbiDialog.this.owner.getBackgroundImage();
            }
        }
        
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            
            Graphics2D graphics2d = (Graphics2D) graphics;
            
            if (backgroundImage != null) {
                graphics2d.drawImage(backgroundImage, 0, 0, this);
                
                Composite oldComposite = graphics2d.getComposite();
                
                graphics2d.setComposite(
                        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                graphics2d.setColor(Color.WHITE);
                graphics2d.fillRect(0, 0, this.getWidth(), this.getHeight());
                
                graphics2d.setComposite(oldComposite);
            }
        }
    }
    
    public static final int DIALOG_FRAME_WIDTH_DELTA = 100;
    
    public static final int DIALOG_FRAME_HEIGHT_DELTA = 100;
    
    public static final int DEFAULT_DIALOG_WIDTH =
            NbiFrame.DEFAULT_FRAME_WIDTH - DIALOG_FRAME_WIDTH_DELTA;
    
    public static final int DEFAULT_DIALOG_HEIGHT =
            NbiFrame.DEFAULT_FRAME_HEIGHT - DIALOG_FRAME_HEIGHT_DELTA;
    
    public static final URL DEFAULT_DIALOG_ICON = NbiDialog.class.
            getClassLoader().
            getResource("org/netbeans/installer/wizard/wizard-icon.png");
}
