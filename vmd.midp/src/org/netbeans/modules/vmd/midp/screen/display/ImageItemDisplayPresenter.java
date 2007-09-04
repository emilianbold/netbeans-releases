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
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.items.ImageItemCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ResourcePropertyEditor;
import org.openide.util.Utilities;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class ImageItemDisplayPresenter extends ItemDisplayPresenter {

    private static final String ICON_BROKEN_PATH = "org/netbeans/modules/vmd/midp/resources/screen/broken-image.png"; // NOI18N
    private static final Icon ICON_BROKEN = new ImageIcon(Utilities.loadImage(ICON_BROKEN_PATH));
    private JLabel label;
    private ScreenFileObjectListener imageFileListener;
    private FileObject imageFileObject;

    public ImageItemDisplayPresenter() {
        label = new JLabel();
        setContentComponent(label);
    }

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);

        PropertyValue value = getComponent().readProperty(ImageItemCD.PROP_IMAGE);
        DesignComponent imageComponent = null;
        String path = null;
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            value.getComponent();
            if (imageComponent != null) {
                path = (String) imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH).getPrimitiveValue();
            }
        }

        value = getComponent().readProperty(ImageItemCD.PROP_ALT_TEXT);
        String alternText = null;
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            alternText = MidpTypes.getString(value);
        }
        Icon icon = ScreenSupport.getIconFromImageComponent(imageComponent);
        imageFileObject = ScreenSupport.getFileObjectFromImageComponent(imageComponent);
        if (imageFileObject != null) {
            imageFileObject.removeFileChangeListener(imageFileListener);
            imageFileListener = new ScreenFileObjectListener(getRelatedComponent(), imageComponent, ImageCD.PROP_RESOURCE_PATH);
            imageFileObject.addFileChangeListener(imageFileListener);
        }
        if (icon != null) {
            label.setText(null);
            label.setIcon(icon);
        } else if (icon == null && path != null) {
            label.setIcon(ICON_BROKEN);
        } else if (alternText != null) {
            label.setText(alternText);
            label.setIcon(null);
        }
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        List<ScreenPropertyDescriptor> descriptors = new ArrayList<ScreenPropertyDescriptor>(super.getPropertyDescriptors());
        ResourcePropertyEditor imagePropertyEditor = new ResourcePropertyEditor(ImageItemCD.PROP_IMAGE, getComponent());
        descriptors.add(new ScreenPropertyDescriptor(getComponent(), label, imagePropertyEditor));
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