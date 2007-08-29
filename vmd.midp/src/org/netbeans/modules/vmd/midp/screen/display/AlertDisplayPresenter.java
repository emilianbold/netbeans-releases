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
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.items.GaugeCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ResourcePropertyEditor;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

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
    private GaugeDisplayPresenterElement gauge;
    private JPanel panel;
    private GridBagConstraints constraints;

    public AlertDisplayPresenter() {
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        stringLabel = new JLabel();
        stringLabel.setHorizontalAlignment(JLabel.CENTER);
        JPanel contentPanel = getPanel().getContentPanel();
        contentPanel.setLayout(new GridBagLayout());

        constraints = new GridBagConstraints();
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

        PropertyValue value = getComponent().readProperty(AlertCD.PROP_STRING);
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            String text = MidpTypes.getString(value);
            if (text == null) {
                stringLabel.setText(NbBundle.getMessage(AlertDisplayPresenter.class, "DISP_text_not_specified")); // NOI18N
            } else if (text.length() == 0) {
                stringLabel.setText(NbBundle.getMessage(AlertDisplayPresenter.class, "DISP_text_is_empty")); // NOI18N
            } else {
                stringLabel.setText(text);
            }
        } else {
            stringLabel.setText(NbBundle.getMessage(AlertDisplayPresenter.class, "DISP_text_is_usercode")); // NOI18N
        }

        value = getComponent().readProperty(AlertCD.PROP_IMAGE);
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            DesignComponent imageComponent = value.getComponent();
            String path = null;
            if (imageComponent != null) {
                path = (String) imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH).getPrimitiveValue();
            }
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
                imageLabel.setText(NbBundle.getMessage(AlertDisplayPresenter.class, "DISP_image_not_specified")); // NOI18N
            }
        } else {
            imageLabel.setText(NbBundle.getMessage(AlertDisplayPresenter.class, "DISP_image_is_usercode")); // NOI18N
        }

        DesignComponent indicator = getComponent().readProperty(AlertCD.PROP_INDICATOR).getComponent();
        if (indicator != null) {
            gauge = new GaugeDisplayPresenterElement();
            if (panel == null) {
                panel = new JPanel() {

                    @Override
                    public void paint(Graphics g) {
                        super.paint(g);
                        gauge.setPanel(this);
                        gauge.paintGauge(g);
                    }
                };
                panel.setOpaque(false);
                panel.setPreferredSize(new Dimension(200, 40)); // TODO compute it from fontSize
                panel.repaint();
                panel.revalidate();

                constraints.anchor = GridBagConstraints.CENTER;
                getPanel().getContentPanel().add(panel, constraints);
            }
            gauge.setSize(panel.getSize());
            gauge.setInteractive(MidpTypes.getBoolean(indicator.readProperty(GaugeCD.PROP_INTERACTIVE)));
            int maxValue = MidpTypes.getInteger(indicator.readProperty(GaugeCD.PROP_MAX_VALUE));
            if (maxValue < 0) {
                maxValue = 1;
            }
            gauge.setMaxValue(maxValue);
            int intValue = MidpTypes.getInteger(indicator.readProperty(GaugeCD.PROP_VALUE));
            if (intValue < 0) {
                intValue = 0;
            } else if (intValue > maxValue) {
                intValue = maxValue;
            }
            gauge.setValue(intValue);
            panel.repaint();
        } else if (panel != null) {
            getPanel().getContentPanel().remove(panel);
            panel = null;
            gauge = null;
        }
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        ArrayList<ScreenPropertyDescriptor> descriptors = new ArrayList<ScreenPropertyDescriptor>(super.getPropertyDescriptors());
        ResourcePropertyEditor imagePropertyEditor = new ResourcePropertyEditor(AlertCD.PROP_IMAGE, getComponent());
        descriptors.add(new ScreenPropertyDescriptor(getComponent(), imageLabel, imagePropertyEditor));
        descriptors.add(new ScreenPropertyDescriptor(getComponent(), stringLabel, new ScreenStringPropertyEditor(AlertCD.PROP_STRING)));
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