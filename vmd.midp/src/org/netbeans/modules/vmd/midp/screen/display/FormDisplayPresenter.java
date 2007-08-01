/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.components.displayables.FormCD;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.awt.*;

/**
 *
 * @author breh
 */
public class FormDisplayPresenter extends DisplayableDisplayPresenter {
    
    private JPanel fillPanel;
    
    public FormDisplayPresenter() {
        getPanel().getContentPanel().setLayout(new GridBagLayout());
        fillPanel = new JPanel();
        fillPanel.setOpaque(false);
    }
    
    @Override
    public Collection<DesignComponent> getChildren() {
        PropertyValue itemsValue = getComponent().readProperty(FormCD.PROP_ITEMS);
        ArrayList<DesignComponent> items = new ArrayList<DesignComponent>();
        Debug.collectAllComponentReferences(itemsValue, items);
        return items;
    }
    
    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        JPanel contentPanel = getPanel().getContentPanel();
        contentPanel.removeAll();
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = GridBagConstraints.REMAINDER;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        
        for (DesignComponent item : getChildren()) {
            ScreenDisplayPresenter presenter = item.getPresenter(ScreenDisplayPresenter.class);
            if (presenter == null) {
                continue;
            }
            
            contentPanel.add(presenter.getView(), constraints);
            presenter.reload(deviceInfo);
        }
        
        constraints.weighty = 1.0;
        constraints.anchor = GridBagConstraints.CENTER;
        contentPanel.add(fillPanel, constraints);
    }
    
}
