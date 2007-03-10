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

package org.netbeans.modules.vmd.midpnb.screen.display;

import java.awt.BorderLayout;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midpnb.components.displayables.AbstractInfoScreenCD;
import org.openide.util.Utilities;

/**
 *
 * @author Anton Chechel
 */
public class AbstractInfoDisplayPresenter extends DisplayableDisplayPresenter {
    
    private JLabel imageLabel;
    private JLabel stringLabel;
    
    public AbstractInfoDisplayPresenter() {
        imageLabel = new JLabel();
        stringLabel = new JLabel();
        JPanel contentPanel = getPanel().getContentPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(imageLabel, BorderLayout.CENTER);
        contentPanel.add(stringLabel, BorderLayout.SOUTH);
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        DesignComponent imageComponent = getComponent().readProperty(AbstractInfoScreenCD.PROP_IMAGE).getComponent();
        String iconPath = MidpTypes.getString(imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH));
        Image image = Utilities.loadImage(iconPath);
        if (image != null) {
            Icon icon = new ImageIcon(image);
            imageLabel.setIcon(icon);
        } else {
            Debug.warning("Can't load image for info screen " + getComponent());
        }
        
        String text = MidpTypes.getString(getComponent().readProperty(AbstractInfoScreenCD.PROP_TEXT));
        stringLabel.setText(text);
    }
    
}
