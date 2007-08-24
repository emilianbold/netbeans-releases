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
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.components.items.StringItemCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class StringItemDisplayPresenter extends ItemDisplayPresenter {

    private JLabel label;

    public StringItemDisplayPresenter() {
        label = new JLabel();
        setContentComponent(label);
    }

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);

        String text = MidpValueSupport.getHumanReadableString(getComponent().readProperty(StringItemCD.PROP_TEXT));
        int appearanceMode = MidpTypes.getInteger(getComponent().readProperty(ItemCD.PROP_APPEARANCE_MODE));
        label.setBorder(appearanceMode == ItemCD.VALUE_BUTTON ? BorderFactory.createRaisedBevelBorder() : null);
        label.setForeground(appearanceMode == ItemCD.VALUE_HYPERLINK ? Color.BLUE : UIManager.getDefaults().getColor("Label.foreground")); // NOI18N
        label.setText(text);

        DesignComponent font = getComponent().readProperty(StringItemCD.PROP_FONT).getComponent();
        label.setFont(ScreenSupport.getFont(deviceInfo, font));
    }


    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        ArrayList<ScreenPropertyDescriptor> list = new ArrayList<ScreenPropertyDescriptor>(super.getPropertyDescriptors());
        list.add(new ScreenPropertyDescriptor(getComponent(), label, new ScreenStringPropertyEditor(StringItemCD.PROP_TEXT)));
        return list;
    }
}
