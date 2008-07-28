/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.vmd.midpnb.screen.display;

import org.netbeans.modules.mobility.svgcore.util.Util;
import org.netbeans.modules.vmd.api.model.Debug;
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
    private boolean useFileListener;

    public SVGPlayerDisplayPresenter() {
        JPanel contentPanel = getPanel().getContentPanel();
        contentPanel.setLayout(new BorderLayout());
        stringLabel = new JLabel();
        stringLabel.setHorizontalAlignment(JLabel.CENTER);
    }
    
    public SVGPlayerDisplayPresenter(boolean useFilelistener) {
        this();
        this.useFileListener = useFilelistener;
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
            boolean notSVGTiny = false;
            if (svgImageComponent != null) {
                PropertyValue propertyValue = svgImageComponent.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
                if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
                    Map<FileObject, FileObject> images = MidpProjectSupport.getFileObjectsForRelativeResourcePath(animatorComponent.getDocument(), MidpTypes.getString(propertyValue));
                    Iterator<FileObject> iterator = images.keySet().iterator();
                    svgFileObject = iterator.hasNext() ? iterator.next() : null;
                    if (svgFileObject != null) {
                        try {
                            svgImage = Util.createSVGImage(svgFileObject, true);
                            if (svgFileObject != null && useFileListener) {
                                svgFileObject.removeFileChangeListener(imageFileListener);
                                imageFileListener = new ScreenFileObjectListener(getRelatedComponent(), svgImageComponent, SVGImageCD.PROP_RESOURCE_PATH);
                                svgFileObject.addFileChangeListener(imageFileListener);
                            }
                        } catch (IOException e) {
                            Debug.warning(e);
                            notSVGTiny = true;
                        }
                    }
                }
            }
            imageView.setImage(svgImage);
            if (svgImage != null) {
                contentPanel.add(imageView, BorderLayout.CENTER);
            } else {
                if (notSVGTiny) {
                    stringLabel.setText(NbBundle.getMessage(SVGPlayerDisplayPresenter.class, "DISP_svg_image_not_svg_tiny")); // NOI18N
                    contentPanel.add(stringLabel, BorderLayout.CENTER);
                } else {
                    stringLabel.setText(NbBundle.getMessage(SVGPlayerDisplayPresenter.class, "DISP_svg_image_not_specified")); // NOI18N
                    contentPanel.add(stringLabel, BorderLayout.CENTER);
                }
            }
        } else {
            stringLabel.setText(NbBundle.getMessage(SVGPlayerDisplayPresenter.class, "DISP_svg_image_is_usercode")); // NOI18N
            contentPanel.add(stringLabel, BorderLayout.CENTER);
        }
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        ArrayList<ScreenPropertyDescriptor> descriptors = new ArrayList<ScreenPropertyDescriptor>(super.getPropertyDescriptors());
        ResourcePropertyEditor imagePropertyEditor = new ResourcePropertyEditor(SVGPlayerCD.PROP_SVG_IMAGE, getComponent());
        if (stringLabel.getParent() != null) {
            descriptors.add(new ScreenPropertyDescriptor(getComponent(), stringLabel, imagePropertyEditor));
        } else {
            descriptors.add(new ScreenPropertyDescriptor(getComponent(), imageView, imagePropertyEditor));
        }
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