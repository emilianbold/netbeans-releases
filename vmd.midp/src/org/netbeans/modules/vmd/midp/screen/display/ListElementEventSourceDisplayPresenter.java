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
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayDataFlavorSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.displayables.ListCD;
import org.netbeans.modules.vmd.midp.components.elements.ChoiceElementCD;
import org.netbeans.modules.vmd.midp.components.items.ChoiceSupport;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenBooleanPropertyEditor;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Anton Chechel
 */
public class ListElementEventSourceDisplayPresenter extends ScreenDisplayPresenter {

    private static final String ICON_BROKEN_PATH = "org/netbeans/modules/vmd/midp/resources/screen/broken-image.png"; // NOI18N
    private static final Icon ICON_BROKEN = new ImageIcon(ImageUtilities.loadImage(ICON_BROKEN_PATH));
    private JPanel view;
    private JLabel state;
    private JLabel image;
    private JLabel label;
    private ScreenFileObjectListener imageFileListener;
    private FileObject imageFileObject;

    public ListElementEventSourceDisplayPresenter() {
        view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.X_AXIS));
        view.setOpaque(false);

        state = new JLabel();
        view.add(state);
        image = new JLabel();
        view.add(image);
        label = new JLabel();
        view.add(label);

        view.add(Box.createHorizontalGlue());
    }

    public boolean isTopLevelDisplay() {
        return false;
    }

    public Collection<DesignComponent> getChildren() {
        return Collections.emptyList();
    }

    public JComponent getView() {
        return view;
    }

    public void reload(ScreenDeviceInfo deviceInfo) {
        PropertyValue value = getComponent().getParentComponent().readProperty(ListCD.PROP_LIST_TYPE);
        int type;
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            type = MidpTypes.getInteger(value);
        } else {
            type = ChoiceSupport.VALUE_EXCLUSIVE;
        }

        PropertyValue selectedValue = getComponent().readProperty(ListElementEventSourceCD.PROP_SELECTED);
        boolean selected = selectedValue.getKind() == PropertyValue.Kind.VALUE && MidpTypes.getBoolean(selectedValue);
        switch (type) {
            case ChoiceSupport.VALUE_EXCLUSIVE:
                state.setIcon(selected ? ChoiceElementDisplayPresenter.ICON_RADIOBUTTON : ChoiceElementDisplayPresenter.ICON_EMPTY_RADIOBUTTON);
                break;
            case ChoiceSupport.VALUE_MULTIPLE:
                state.setIcon(selected ? ChoiceElementDisplayPresenter.ICON_CHECKBOX : ChoiceElementDisplayPresenter.ICON_EMPTY_CHECKBOX);
                break;
            default:
                state.setIcon(null);
                break;
        }

        DesignComponent imageComponent = null;
        String path = null;
        value = getComponent().readProperty(ChoiceElementCD.PROP_IMAGE);
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            imageComponent = value.getComponent();
        }
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
            image.setIcon(icon);
        } else if (path != null) {
            image.setIcon(ICON_BROKEN);
        } else {
            image.setIcon(null);
        }
        String text = MidpValueSupport.getHumanReadableString(getComponent().readProperty(ListElementEventSourceCD.PROP_STRING));
        label.setText(text);

        value = getComponent().readProperty(ListElementEventSourceCD.PROP_FONT);
        if (!PropertyValue.Kind.USERCODE.equals(value.getKind())) {
            DesignComponent font = value.getComponent();
            label.setFont(ScreenSupport.getFont(deviceInfo, font));
        }
    }

    public Shape getSelectionShape() {
        return new Rectangle(view.getSize());
    }

    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        return Arrays.asList(new ScreenPropertyDescriptor(getComponent(), state, new ScreenBooleanPropertyEditor(ChoiceElementCD.PROP_SELECTED)), new ScreenPropertyDescriptor(getComponent(), label, new ScreenStringPropertyEditor(ChoiceElementCD.PROP_STRING)));
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    @Override
    public AcceptSuggestion createSuggestion(Transferable transferable) {
        if (!(transferable.isDataFlavorSupported(ScreenDisplayDataFlavorSupport.HORIZONTAL_POSITION_DATA_FLAVOR))) {
            return null;
        }
        if (!(transferable.isDataFlavorSupported(ScreenDisplayDataFlavorSupport.VERTICAL_POSITION_DATA_FLAVOR))) {
            return null;
        }
        ScreenDeviceInfo.Edge horizontalPosition = null;
        ScreenDeviceInfo.Edge verticalPosition = null;

        try {
            horizontalPosition = (ScreenDeviceInfo.Edge) transferable.getTransferData(ScreenDisplayDataFlavorSupport.HORIZONTAL_POSITION_DATA_FLAVOR);
            verticalPosition = (ScreenDeviceInfo.Edge) transferable.getTransferData(ScreenDisplayDataFlavorSupport.VERTICAL_POSITION_DATA_FLAVOR);
        } catch (UnsupportedFlavorException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new ScreenMoveArrayAcceptSuggestion(horizontalPosition, verticalPosition);
    }
}