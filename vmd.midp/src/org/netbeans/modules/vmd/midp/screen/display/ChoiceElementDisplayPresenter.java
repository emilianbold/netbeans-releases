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

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.elements.ChoiceElementCD;
import org.openide.util.Utilities;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class ChoiceElementDisplayPresenter extends ScreenDisplayPresenter {
    
    private JPanel panel;
    private JCheckBox checkBox;
    
    public ChoiceElementDisplayPresenter() {
        panel = new JPanel() {
            public JPopupMenu getComponentPopupMenu() {
                return Utilities.actionsToPopup(ActionsSupport.createActionsArray(getRelatedComponent()), this);
            }
        };
        panel.setLayout(new BorderLayout());
        checkBox = new JCheckBox();
        panel.add(checkBox, BorderLayout.NORTH);
    }
    
    public boolean isTopLevelDisplay() {
        return false;
    }
    
    public Collection<DesignComponent> getChildren() {
        return Collections.emptyList();
    }
    
    public JComponent getView() {
        return panel;
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        panel.setBorder(deviceInfo.getDeviceTheme().getBorder(getComponent().getDocument().getSelectedComponents().contains(getComponent())));
        checkBox.setText(MidpValueSupport.getHumanReadableString(getComponent().readProperty(ChoiceElementCD.PROP_STRING)));
        checkBox.setSelected(MidpTypes.getBoolean(getComponent().readProperty(ChoiceElementCD.PROP_SELECTED)));
        DesignComponent font = getComponent().readProperty(ChoiceElementCD.PROP_FONT).getComponent();
        if (font != null) {
            checkBox.setFont(ScreenSupport.getFont(deviceInfo, font));
        }
    }
    
    public Shape getSelectionShape() {
        return new Rectangle(panel.getSize());
    }
    
}
