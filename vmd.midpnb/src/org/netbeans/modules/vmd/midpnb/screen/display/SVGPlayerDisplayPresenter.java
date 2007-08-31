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

import org.netbeans.modules.mobility.svgcore.util.Util;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midp.screen.display.ScreenFileObjectListener;
import org.netbeans.modules.vmd.midp.screen.display.property.ResourcePropertyEditor;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGPlayerCD;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import javax.microedition.m2g.SVGImage;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class SVGPlayerDisplayPresenter extends DisplayableDisplayPresenter {

    private JLabel stringLabel;
    private SVGImageComponent imageView = new SVGImageComponent();
    private ScreenFileObjectListener imageFileListener;
    private FileObject svgFileObject;

    public SVGPlayerDisplayPresenter () {
        JPanel contentPanel = getPanel().getContentPanel();
        contentPanel.setLayout(new BorderLayout());
        stringLabel = new JLabel();
        stringLabel.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        JPanel contentPanel = getPanel().getContentPanel();
        contentPanel.removeAll();

        final DesignComponent animatorComponent = getComponent();
        PropertyValue value = animatorComponent.readProperty(SVGPlayerCD.PROP_SVG_IMAGE);
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            DesignComponent svgImageComponent = value.getComponent();

            SVGImage svgImage = null;
            if (svgImageComponent != null) {
                PropertyValue propertyValue = svgImageComponent.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
                if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
                    Map<FileObject, FileObject> images = MidpProjectSupport.getFileObjectsForRelativeResourcePath(animatorComponent.getDocument(), MidpTypes.getString(propertyValue));
                    Iterator<FileObject> iterator = images.keySet().iterator();
                    svgFileObject = iterator.hasNext() ? iterator.next() : null;
                    if (svgFileObject != null) {
                        try {
                            svgImage = Util.createSVGImage(svgFileObject, true);
                            if (svgFileObject != null) {
                                svgFileObject.removeFileChangeListener(imageFileListener);
                                imageFileListener = new ScreenFileObjectListener(getRelatedComponent(), svgImageComponent, SVGImageCD.PROP_RESOURCE_PATH);
                                svgFileObject.addFileChangeListener(imageFileListener);
                            }
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            }
            imageView.setImage(svgImage);
            if (svgImage != null) {
                contentPanel.add(imageView, BorderLayout.CENTER);
            } else {
                stringLabel.setText(NbBundle.getMessage(SVGPlayerDisplayPresenter.class, "DISP_svg_image_not_specified")); // NOI18N
                contentPanel.add(stringLabel, BorderLayout.CENTER);
            }
        } else {
            stringLabel.setText(NbBundle.getMessage(SVGPlayerDisplayPresenter.class, "DISP_svg_image_is_usercode")); // NOI18N
            contentPanel.add(stringLabel, BorderLayout.CENTER);
        }
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        ArrayList<ScreenPropertyDescriptor> descriptors = new ArrayList<ScreenPropertyDescriptor> (super.getPropertyDescriptors());
        ResourcePropertyEditor imagePropertyEditor = new ResourcePropertyEditor (SVGPlayerCD.PROP_SVG_IMAGE, getComponent());
        if (stringLabel.getParent () != null)
            descriptors.add(new ScreenPropertyDescriptor(getComponent(), stringLabel, imagePropertyEditor));
        else
            descriptors.add(new ScreenPropertyDescriptor(getComponent(), imageView, imagePropertyEditor));
        return descriptors;
    }

    @Override
    protected void notifyDetached(DesignComponent component) {
        if (svgFileObject != null && imageFileListener != null) {
            svgFileObject.removeFileChangeListener(imageFileListener);
        }
        svgFileObject = null;
        imageFileListener = null;
    }

}
