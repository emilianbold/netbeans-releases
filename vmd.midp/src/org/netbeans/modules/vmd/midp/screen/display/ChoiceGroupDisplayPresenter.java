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

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.items.ChoiceGroupCD;
import org.netbeans.modules.vmd.midp.components.items.ChoiceSupport;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class ChoiceGroupDisplayPresenter extends ItemDisplayPresenter {
    
    private static final Border POPUP_BORDER = BorderFactory.createBevelBorder(BevelBorder.RAISED);
    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();
    
    private JPanel panel;
    
    public ChoiceGroupDisplayPresenter() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        setContentComponent(panel);
    }
    
    @Override
    public Collection<DesignComponent> getChildren() {
        PropertyValue elementsValue = getComponent().readProperty(ChoiceGroupCD.PROP_ELEMENTS);
        int type = (Integer) getComponent().readProperty(ChoiceGroupCD.PROP_CHOICE_TYPE).getPrimitiveValue();
        ArrayList<DesignComponent> elements = new ArrayList<DesignComponent>();
            Debug.collectAllComponentReferences(elementsValue, elements);
        if (type == ChoiceSupport.VALUE_POPUP && elements.size() > 0) {
            return elements.subList(0, 1);
        }
        return elements;
    }
    
    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        PropertyValue value = getComponent().readProperty(ChoiceGroupCD.PROP_CHOICE_TYPE);
        panel.setBorder(value.getKind() == PropertyValue.Kind.VALUE  &&  MidpTypes.getInteger(value) == ChoiceSupport.VALUE_POPUP
                ? POPUP_BORDER : EMPTY_BORDER);
        
        panel.removeAll();
        for (DesignComponent item : getChildren()) {
            ChoiceElementDisplayPresenter presenter = item.getPresenter(ChoiceElementDisplayPresenter.class);
            if (presenter == null) {
                continue;
            }
            panel.add(presenter.getView());
            presenter.reload(deviceInfo);
        }
        panel.add(Box.createVerticalGlue());
    }
    
}
