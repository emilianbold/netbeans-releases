/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils.helper.swing;

import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.exceptions.DownloadException;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiPanel extends JPanel {
    private HashMap<Integer, Image> imagesMap ;
    public static final int ANCHOR_TOP_LEFT  = 1;
    public static final int ANCHOR_TOP_RIGHT = 2;
    public static final int ANCHOR_BOTTON_LEFT = 3;
    public static final int ANCHOR_BOTTON_RIGHT = 4;
    
    public NbiPanel() {
        super();
        
        setLayout(new GridBagLayout());
        imagesMap = new HashMap <Integer, Image> ();
    }
    public void setBackgroundImage(String backgroundImageURI, int anchor) {
        if (backgroundImageURI != null) {
            try {
                File file = FileProxy.getInstance().getFile(backgroundImageURI);
                Image backgroundImage = new ImageIcon(file.getAbsolutePath()).
                        getImage();
                imagesMap.put(anchor,backgroundImage);
            } catch (DownloadException e) {
                LogManager.log(e);
            }
        }
    }
    
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        
        for(Integer anchor : imagesMap.keySet()){
            Image backgroundImage = imagesMap.get(anchor);
            if (backgroundImage != null) {
                switch(anchor.intValue()) {
                    case ANCHOR_TOP_LEFT :
                        graphics.drawImage(backgroundImage,
                                0,
                                0,
                                this);
                        break;
                    case ANCHOR_TOP_RIGHT:
                        graphics.drawImage(backgroundImage,
                                this.getWidth() - backgroundImage.getWidth(this),
                                0,
                                this);
                        break;
                    case ANCHOR_BOTTON_LEFT:
                        graphics.drawImage(backgroundImage,
                                0,
                                this.getHeight() - backgroundImage.getHeight(this),
                                this);
                        break;
                    case ANCHOR_BOTTON_RIGHT:
                        graphics.drawImage(backgroundImage,
                                this.getWidth() - backgroundImage.getWidth(this),
                                this.getHeight() - backgroundImage.getHeight(this),
                                this);
                        break;
                }
            }
        }
    }
}
