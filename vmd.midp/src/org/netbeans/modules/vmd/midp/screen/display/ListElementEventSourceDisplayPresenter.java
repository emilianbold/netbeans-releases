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
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.elements.ChoiceElementCD;
import org.netbeans.modules.vmd.midp.components.displayables.ListCD;
import org.netbeans.modules.vmd.midp.components.items.ChoiceSupport;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenBooleanPropertyEditor;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

/**
 *
 * @author Anton Chechel
 */
public class ListElementEventSourceDisplayPresenter extends ScreenDisplayPresenter {

    private JPanel view;
    private JLabel state;
    private JLabel image;
    private JLabel label;
    
    public ListElementEventSourceDisplayPresenter() {
        view = new JPanel ();
        view.setLayout(new BoxLayout(view, BoxLayout.X_AXIS));
        view.setOpaque (false);

        state = new JLabel ();
        view.add (state);
        image = new JLabel ();
        view.add (image);
        label = new JLabel();
        view.add (label);

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
        int type = (Integer) getComponent ().getParentComponent ().readProperty (ListCD.PROP_LIST_TYPE).getPrimitiveValue ();
        PropertyValue selectedValue = getComponent ().readProperty (ListElementEventSourceCD.PROP_SELECTED);
        boolean selected = selectedValue.getKind () == PropertyValue.Kind.VALUE  &&  MidpTypes.getBoolean (selectedValue);
        switch (type) {
            case ChoiceSupport.VALUE_EXCLUSIVE:
                state.setIcon (selected ? ChoiceElementDisplayPresenter.ICON_RADIOBUTTON : ChoiceElementDisplayPresenter.ICON_EMPTY_RADIOBUTTON);
                break;
            case ChoiceSupport.VALUE_MULTIPLE:
                state.setIcon (selected ? ChoiceElementDisplayPresenter.ICON_CHECKBOX : ChoiceElementDisplayPresenter.ICON_EMPTY_CHECKBOX);
                break;
            default:
                state.setIcon (null);
                break;
        }

        PropertyValue imageValue = getComponent ().readProperty (ListElementEventSourceCD.PROP_IMAGE);
        Icon imageIcon = ScreenSupport.getIconFromImageComponent (imageValue.getComponent ());
        image.setIcon (imageIcon);

        String text = MidpValueSupport.getHumanReadableString(getComponent().readProperty(ListElementEventSourceCD.PROP_STRING));
        label.setText(ScreenSupport.wrapWithHtml(text));
        
        DesignComponent font = getComponent().readProperty(ListElementEventSourceCD.PROP_FONT).getComponent();
        label.setFont(ScreenSupport.getFont(deviceInfo, font));
    }
    
    public Shape getSelectionShape() {
        return new Rectangle(view.getSize());
    }

    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors () {
        return Arrays.asList (
                new ScreenPropertyDescriptor (getComponent (), state, new ScreenBooleanPropertyEditor (ChoiceElementCD.PROP_SELECTED)),
                new ScreenPropertyDescriptor(getComponent(), label, new ScreenStringPropertyEditor(ChoiceElementCD.PROP_STRING))
        );
    }

}
