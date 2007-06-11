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

import java.awt.datatransfer.Transferable;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.elements.ChoiceElementCD;
import org.netbeans.modules.vmd.midp.components.items.ChoiceGroupCD;
import org.netbeans.modules.vmd.midp.components.items.ChoiceSupport;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenBooleanPropertyEditor;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class ChoiceElementDisplayPresenter extends ScreenDisplayPresenter {
    
    public static final String ICON_EMPTY_CHECKBOX_PATH = "org/netbeans/modules/vmd/midp/resources/components/empty_element_16.png"; // NOI18N
    public static final String ICON_CHECKBOX_PATH = "org/netbeans/modules/vmd/midp/resources/components/element_16.png"; // NOI18N
    public static final String ICON_EMPTY_RADIOBUTTON_PATH = "org/netbeans/modules/vmd/midp/resources/components/empty_radio_16.png"; // NOI18N
    public static final String ICON_RADIOBUTTON_PATH = "org/netbeans/modules/vmd/midp/resources/components/radio_16.png"; // NOI18N
    public static final String ICON_POPUP_PATH = "org/netbeans/modules/vmd/midp/resources/components/down.png"; // NOI18N
    public static final String ICON_BROKEN_PATH = "org/netbeans/modules/vmd/midp/resources/components/broken.png"; // NOI18N
    
    public static final Icon ICON_EMPTY_CHECKBOX = new ImageIcon(Utilities.loadImage(ICON_EMPTY_CHECKBOX_PATH));
    public static final Icon ICON_CHECKBOX = new ImageIcon(Utilities.loadImage(ICON_CHECKBOX_PATH));
    public static final Icon ICON_EMPTY_RADIOBUTTON = new ImageIcon(Utilities.loadImage(ICON_EMPTY_RADIOBUTTON_PATH));
    public static final Icon ICON_RADIOBUTTON = new ImageIcon(Utilities.loadImage(ICON_RADIOBUTTON_PATH));
    public static final Icon ICON_POPUP = new ImageIcon(Utilities.loadImage(ICON_POPUP_PATH));
    public static final Icon ICON_BROKEN = new ImageIcon(Utilities.loadImage(ICON_BROKEN_PATH));
    
    private JPanel view;
    private JLabel state;
    private JLabel image;
    private JLabel label;
    
    public ChoiceElementDisplayPresenter() {
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
        int type = (Integer) getComponent().getParentComponent().readProperty(ChoiceGroupCD.PROP_CHOICE_TYPE).getPrimitiveValue();
        PropertyValue selectedValue = getComponent().readProperty(ChoiceElementCD.PROP_SELECTED);
        boolean selected = selectedValue.getKind() == PropertyValue.Kind.VALUE  &&  MidpTypes.getBoolean(selectedValue);
        switch (type) {
            case ChoiceSupport.VALUE_EXCLUSIVE:
                state.setIcon(selected ? ChoiceElementDisplayPresenter.ICON_RADIOBUTTON : ChoiceElementDisplayPresenter.ICON_EMPTY_RADIOBUTTON);
                break;
            case ChoiceSupport.VALUE_MULTIPLE:
                state.setIcon(selected ? ChoiceElementDisplayPresenter.ICON_CHECKBOX : ChoiceElementDisplayPresenter.ICON_EMPTY_CHECKBOX);
                break;
            case ChoiceSupport.VALUE_POPUP:
                state.setIcon(ChoiceElementDisplayPresenter.ICON_POPUP);
                break;
            default:
                state.setIcon(null);
                break;
        }
        
        DesignComponent imageComponent = getComponent().readProperty(ChoiceElementCD.PROP_IMAGE).getComponent();
        String path = null;
        if (imageComponent != null)
            path = (String) imageComponent.readProperty(ImageCD.PROP_RESOURCE_PATH).getPrimitiveValue();
        Icon icon = ScreenSupport.getIconFromImageComponent(imageComponent);
        if (icon != null) {
            image.setIcon(icon);
        } else if (path != null) {
            image.setIcon(ICON_BROKEN);
        } else {
            image.setIcon(null);
        }
        
        String text = MidpValueSupport.getHumanReadableString(getComponent().readProperty(ChoiceElementCD.PROP_STRING));
        label.setText(ScreenSupport.wrapWithHtml(text));
        
        DesignComponent font = getComponent().readProperty(ChoiceElementCD.PROP_FONT).getComponent();
        label.setFont(ScreenSupport.getFont(deviceInfo, font));
    }
    
    public Shape getSelectionShape() {
        return new Rectangle(view.getSize());
    }
    
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        return Arrays.asList(
                new ScreenPropertyDescriptor(getComponent(), state, new ScreenBooleanPropertyEditor(ChoiceElementCD.PROP_SELECTED)),
                new ScreenPropertyDescriptor(getComponent(), label, new ScreenStringPropertyEditor(ChoiceElementCD.PROP_STRING))
                );
    }

}
