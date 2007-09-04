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

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midp.screen.display.ScreenSupport;
import org.netbeans.modules.vmd.midpnb.components.displayables.AbstractInfoScreenCD;
import org.openide.util.Utilities;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.screen.display.ScreenFileObjectListener;
import org.netbeans.modules.vmd.midp.screen.display.property.ResourcePropertyEditor;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class AbstractInfoDisplayPresenter extends DisplayableDisplayPresenter {

    private static final String ICON_BROKEN_PATH = "org/netbeans/modules/vmd/midpnb/resources/broken-image.png"; // NOI18N
    private static final Icon ICON_BROKEN = new ImageIcon(Utilities.loadImage(ICON_BROKEN_PATH));
    private JLabel imageLabel;
    private JLabel stringLabel;
    private ScreenFileObjectListener imageFileListener;
    private FileObject imageFileObject;

    public AbstractInfoDisplayPresenter() {
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

        PropertyValue value = getComponent().readProperty(AbstractInfoScreenCD.PROP_IMAGE);
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            DesignComponent imageComponent = value.getComponent();
            String path = null;
            if (imageComponent != null) {
                value = imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH);
                if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
                    path = MidpTypes.getString(value);
                }
            }

            Icon icon = ScreenSupport.getIconFromImageComponent(imageComponent);
            imageFileObject = ScreenSupport.getFileObjectFromImageComponent(imageComponent);
            if (imageFileObject != null) {
                imageFileObject.removeFileChangeListener(imageFileListener);
                imageFileListener = new ScreenFileObjectListener(getRelatedComponent(), imageComponent, ImageCD.PROP_RESOURCE_PATH);
                imageFileObject.addFileChangeListener(imageFileListener);
            }
            if (icon != null) {
                imageLabel.setText(null);
                imageLabel.setIcon(icon);
            } else if (path != null) {
                imageLabel.setText(path);
                imageLabel.setIcon(ICON_BROKEN);
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText(NbBundle.getMessage(AbstractInfoDisplayPresenter.class, "DISP_image_not_specified")); // NOI18N
            }
        } else {
            imageLabel.setText(NbBundle.getMessage(AbstractInfoDisplayPresenter.class, "DISP_image_is_usercode")); // NOI18N
        }

        value = getComponent().readProperty(AbstractInfoScreenCD.PROP_TEXT);
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            String text = MidpTypes.getString(getComponent().readProperty(AbstractInfoScreenCD.PROP_TEXT));
            if (text == null) {
                stringLabel.setText(NbBundle.getMessage(AbstractInfoDisplayPresenter.class, "DISP_text_not_specified")); // NOI18N
            } else if (text.length() == 0) {
                stringLabel.setText(NbBundle.getMessage(AbstractInfoDisplayPresenter.class, "DISP_text_is_empty")); // NOI18N
            } else {
                stringLabel.setText(text);
                value = getComponent().readProperty(AbstractInfoScreenCD.PROP_TEXT_FONT);
                if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
                    DesignComponent font = value.getComponent();
                    stringLabel.setFont(ScreenSupport.getFont(deviceInfo, font));
                }
            }
        } else {
            stringLabel.setText(NbBundle.getMessage(AbstractInfoDisplayPresenter.class, "DISP_text_is_usercode")); // NOI18N
        }
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        ArrayList<ScreenPropertyDescriptor> descriptors = new ArrayList<ScreenPropertyDescriptor>(super.getPropertyDescriptors());
        ResourcePropertyEditor imagePropertyEditor = new ResourcePropertyEditor(AbstractInfoScreenCD.PROP_IMAGE, getComponent());
        descriptors.add(new ScreenPropertyDescriptor(getComponent(), imageLabel, imagePropertyEditor));
        descriptors.add(new ScreenPropertyDescriptor(getComponent(), stringLabel, new ScreenStringPropertyEditor(AbstractInfoScreenCD.PROP_TEXT)));
        return descriptors;
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