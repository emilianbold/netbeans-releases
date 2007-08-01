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

import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.items.TextFieldCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class TextFieldDisplayPresenter extends ItemDisplayPresenter {
    
    private static final Border LABEL_BORDER = BorderFactory.createLineBorder(Color.GRAY);
    
    private JLabel label;

    public TextFieldDisplayPresenter() {
        label = new JLabel();
        label.setBorder(LABEL_BORDER);
        setContentComponent(label);
    }
    
    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        String text = MidpValueSupport.getHumanReadableString(getComponent().readProperty(TextFieldCD.PROP_TEXT));
        label.setText(text);
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors () {
        ArrayList<ScreenPropertyDescriptor> list = new ArrayList<ScreenPropertyDescriptor> (super.getPropertyDescriptors ());
        list.add (new ScreenPropertyDescriptor (getComponent (), label, new ScreenStringPropertyEditor (TextFieldCD.PROP_TEXT)));
        return list;
    }

}
