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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiFrame extends JFrame {
    protected int frameWidth;
    protected int frameHeight;
    protected URL frameBackground;
    protected URL frameIcon;
    
    protected ContentPane contentPane;
    
    public NbiFrame() {
        super();
        
        frameWidth      = DEFAULT_WIZARD_FRAME_WIDTH;
        frameHeight     = DEFAULT_WIZARD_FRAME_HEIGHT;
        frameBackground = DEFAULT_WIZARD_FRAME_BACKGROUND;
        frameIcon       = DEFAULT_WIZARD_FRAME_ICON;
        
        initComponents();
    }
    
    private void initComponents() {
        // the frame itself
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(frameWidth, frameHeight);
        setIconImage(new ImageIcon(frameIcon).getImage());
        setResizable(false);
        
        // initialize the content pane
        contentPane = new ContentPane();
        contentPane.setBackgroundImage(frameBackground);
        
        // update the content pane
        setContentPane(contentPane);
    }
    
    public void setVisible(boolean visible) {
        int screenWidth  = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        
        setLocation((screenWidth - frameWidth) / 2, (screenHeight - frameHeight) / 2);
        
        super.setVisible(visible);
    }
    
    public ContentPane getContentPane() {
        return contentPane;
    }
    
    public Image getBackgroundImage() {
        return getContentPane().getBackgroundImage();
    }
    
    public void setBackgroundImage(URL backgroundImageUrl) {
        getContentPane().setBackgroundImage(backgroundImageUrl);
    }
    
    public class ContentPane extends JPanel {
        private Image backgroundImage;
        
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            
            if (backgroundImage != null) {
                graphics.drawImage(backgroundImage, 0, 0, this);
            }
        }
        
        public Image getBackgroundImage() {
            return backgroundImage;
        }
        
        public void setBackgroundImage(URL backgroundImageURL) {
            if (backgroundImageURL != null) {
                backgroundImage = new ImageIcon(backgroundImageURL).getImage();
            }  else {
                backgroundImage = null;
            }
        }
    }
    
    public static final int DEFAULT_WIZARD_FRAME_WIDTH      = 600;
    public static final int DEFAULT_WIZARD_FRAME_HEIGHT     = 500;
    public static final URL DEFAULT_WIZARD_FRAME_BACKGROUND = NbiFrame.class.getClassLoader().getResource("org/netbeans/installer/wizard/wizard-background.png");
    public static final URL DEFAULT_WIZARD_FRAME_ICON       = NbiFrame.class.getClassLoader().getResource("org/netbeans/installer/wizard/wizard-icon.png");
    
    public static final String WIZARD_FRAME_WIDTH_PROPERTY      = "nbi.wizard.frame.width";
    public static final String WIZARD_FRAME_HEIGHT_PROPERTY     = "nbi.wizard.frame.height";
    public static final String WIZARD_FRAME_BACKGROUND_PROPERTY = "nbi.wizard.frame.background";
    public static final String WIZARD_FRAME_ICON_PROPERTY       = "nbi.wizard.frame.icon";
}
