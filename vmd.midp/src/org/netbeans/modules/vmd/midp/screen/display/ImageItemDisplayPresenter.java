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

package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.midp.components.items.ImageItemCD;

import javax.swing.*;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.openide.util.Utilities;


/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class ImageItemDisplayPresenter extends ItemDisplayPresenter {
    
    private static final String ICON_BROKEN_PATH = "org/netbeans/modules/vmd/midp/resources/components/broken.png"; // NOI18N
    private static final Icon ICON_BROKEN = new ImageIcon(Utilities.loadImage(ICON_BROKEN_PATH));
    
    private JLabel label;
    
    public ImageItemDisplayPresenter() {
        label = new JLabel();
        setContentComponent(label);
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        DesignComponent imageComponent = getComponent().readProperty(ImageItemCD.PROP_IMAGE).getComponent();
        String path = null;
        if (imageComponent != null)
            path = (String) imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH).getPrimitiveValue();
        String alternText = (String) getComponent().readProperty(ImageItemCD.PROP_ALT_TEXT).getPrimitiveValue();
        Icon icon = ScreenSupport.getIconFromImageComponent(imageComponent);
        
        if (icon != null) {
            label.setText(null);
            label.setIcon(icon);
        } else if (icon == null && path != null) {
            label.setIcon(ICON_BROKEN);
        }  else if (alternText != null) {
            label.setText(alternText); //NOI18N
            label.setIcon(null);
        }
    }
}
