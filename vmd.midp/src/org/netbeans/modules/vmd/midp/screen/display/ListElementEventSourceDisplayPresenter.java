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
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Anton Chechel
 */
public class ListElementEventSourceDisplayPresenter extends ScreenDisplayPresenter {
    
    private JLabel label;
    
    public ListElementEventSourceDisplayPresenter() {
        label = new JLabel();
    }
    
    public boolean isTopLevelDisplay() {
        return false;
    }
    
    public Collection<DesignComponent> getChildren() {
        return Collections.emptyList();
    }
    
    public JComponent getView() {
        return label;
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        String text = MidpValueSupport.getHumanReadableString(getComponent().readProperty(ListElementEventSourceCD.PROP_STRING));
        label.setText(ScreenSupport.wrapWithHtml(text));
        
        DesignComponent font = getComponent().readProperty(ListElementEventSourceCD.PROP_FONT).getComponent();
        if (font != null) {
            label.setFont(ScreenSupport.getFont(deviceInfo, font));
        }
    }
    
    public Shape getSelectionShape() {
        return new Rectangle(label.getSize());
    }
}
