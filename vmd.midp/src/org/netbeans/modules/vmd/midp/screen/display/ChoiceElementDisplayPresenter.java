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

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Collection;
import java.util.Collections;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.elements.ChoiceElementCD;
import org.netbeans.modules.vmd.midp.components.items.ChoiceCD;
import org.netbeans.modules.vmd.midp.components.items.ChoiceGroupCD;
import org.openide.util.Utilities;

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
    
    private static final Icon ICON_EMPTY_CHECKBOX = new ImageIcon(Utilities.loadImage(ICON_EMPTY_CHECKBOX_PATH));
    private static final Icon ICON_CHECKBOX = new ImageIcon(Utilities.loadImage(ICON_CHECKBOX_PATH));
    private static final Icon ICON_EMPTY_RADIOBUTTON = new ImageIcon(Utilities.loadImage(ICON_EMPTY_RADIOBUTTON_PATH));
    private static final Icon ICON_RADIOBUTTON = new ImageIcon(Utilities.loadImage(ICON_RADIOBUTTON_PATH));
    
    private JPanel panel;
    private JLabel label;
    
    public ChoiceElementDisplayPresenter() {
        label = new JLabel();
        
        panel = new JPanel() {
            public JPopupMenu getComponentPopupMenu() {
                return Utilities.actionsToPopup(ActionsSupport.createActionsArray(getRelatedComponent()), this);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(label);
        panel.add(Box.createHorizontalGlue());
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
        label.setText(MidpValueSupport.getHumanReadableString(getComponent().readProperty(ChoiceElementCD.PROP_STRING)));
        
        boolean isSelected = MidpTypes.getBoolean(getComponent().readProperty(ChoiceElementCD.PROP_SELECTED));
        int choiceType = MidpTypes.getInteger(getComponent().getParentComponent().readProperty(ChoiceGroupCD.PROP_CHOICE_TYPE));
        if (choiceType == ChoiceCD.VALUE_EXCLUSIVE) {
            label.setIcon(isSelected ? ICON_RADIOBUTTON : ICON_EMPTY_RADIOBUTTON);
        } else if (choiceType == ChoiceCD.VALUE_POPUP) {
            label.setIcon(null);
        } else {
            label.setIcon(isSelected ? ICON_CHECKBOX : ICON_EMPTY_CHECKBOX);
        }
        
        DesignComponent font = getComponent().readProperty(ChoiceElementCD.PROP_FONT).getComponent();
        if (font != null) {
            label.setFont(ScreenSupport.getFont(deviceInfo, font));
        }
    }
    
    public Shape getSelectionShape() {
        return new Rectangle(panel.getSize());
    }
    
}
