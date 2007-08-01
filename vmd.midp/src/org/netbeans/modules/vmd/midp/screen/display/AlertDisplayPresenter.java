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
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.AlertCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Anton Chechel
 */
public class AlertDisplayPresenter extends DisplayableDisplayPresenter {
    
    private static final String ICON_BROKEN_PATH = "org/netbeans/modules/vmd/midp/resources/screen/broken-image.png"; // NOI18N
    private static final Icon ICON_BROKEN = new ImageIcon(Utilities.loadImage(ICON_BROKEN_PATH));
    
    private JLabel imageLabel;
    private JLabel stringLabel;
    private ScreenFileObjectListener imageFileListener;
    private FileObject imageFileObject;
    
    public AlertDisplayPresenter() {
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        stringLabel = new JLabel();
        stringLabel.setHorizontalAlignment(JLabel.CENTER);
        JPanel contentPanel = getPanel().getContentPanel();
        contentPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = GridBagConstraints.REMAINDER;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.CENTER;
        contentPanel.add(imageLabel, constraints);
        
        constraints.anchor = GridBagConstraints.NORTHWEST;
        contentPanel.add(stringLabel, constraints);
    }
    
    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        String text = MidpTypes.getString(getComponent().readProperty(AlertCD.PROP_STRING));
        stringLabel.setText(text);
        
        DesignComponent imageComponent = getComponent().readProperty(AlertCD.PROP_IMAGE).getComponent();
        String path = null;
        if (imageComponent != null)
            path = (String) imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH).getPrimitiveValue();
        Icon icon = ScreenSupport.getIconFromImageComponent(imageComponent);
        imageFileObject = ScreenSupport.getFileObjectFromImageComponent(imageComponent);
        if (imageFileObject != null) {
            imageFileObject.removeFileChangeListener(imageFileListener);
            imageFileListener = new ScreenFileObjectListener(getRelatedComponent(), imageComponent, ImageCD.PROP_RESOURCE_PATH);
            imageFileObject.addFileChangeListener(imageFileListener);
        }
        if (icon != null) {
            imageLabel.setIcon(icon);
        } else if (path != null) {
            imageLabel.setIcon(ICON_BROKEN);
        } else {
            imageLabel.setIcon(null);
        }
    }
    
    @Override
    protected void notifyDetached(DesignComponent component) {
        if (imageFileObject != null && imageFileListener != null) {
            imageFileObject.removeFileChangeListener(imageFileListener);
        }
        imageFileObject = null;
        imageFileListener = null;
    }
}
