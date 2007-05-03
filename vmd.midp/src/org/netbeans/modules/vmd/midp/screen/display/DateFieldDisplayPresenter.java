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

import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.items.DateFieldCD;

import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;

/**
 *
 * @author David Kaspar
 */
public class DateFieldDisplayPresenter extends ItemDisplayPresenter {
    
    private JLabel label;

    public DateFieldDisplayPresenter() {
        label = new JLabel();
        setContentComponent(label);
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);

        PropertyValue inputModeValue = getComponent ().readProperty (DateFieldCD.PROP_INPUT_MODE);
        String text = "<user input mode>";
        DateFormat format = DateFormat.getDateTimeInstance (DateFormat.MEDIUM, DateFormat.MEDIUM);
        if (inputModeValue.getKind () == PropertyValue.Kind.VALUE) {
            int inputMode = MidpTypes.getInteger (inputModeValue);
            switch (inputMode) {
                case DateFieldCD.VALUE_DATE:
                    text = "<user date>";
                    format = DateFormat.getDateInstance (DateFormat.MEDIUM);
                    break;
                case DateFieldCD.VALUE_DATE_TIME:
                    text = "<user date time>";
                    break;
                case DateFieldCD.VALUE_TIME:
                    text = "<user time>";
                    format = DateFormat.getTimeInstance (DateFormat.MEDIUM);
                    break;
            }
        }
        PropertyValue dateValue = getComponent().readProperty(DateFieldCD.PROP_DATE);
        if (dateValue.getKind() == PropertyValue.Kind.VALUE)
            text = format.format (new Date (MidpTypes.getLong (dateValue)));
        label.setText(text);
    }

}
