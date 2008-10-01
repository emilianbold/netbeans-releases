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

package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.items.ImageItemCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ResourcePropertyEditor;
import org.openide.util.ImageUtilities;
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
    private static final Icon ICON_BROKEN = new ImageIcon(ImageUtilities.loadImage(ICON_BROKEN_PATH));
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
            imageComponent = value.getComponent();
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