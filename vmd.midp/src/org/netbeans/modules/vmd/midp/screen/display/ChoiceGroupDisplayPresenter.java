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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.midp.components.items.ChoiceGroupCD;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class ChoiceGroupDisplayPresenter extends ItemDisplayPresenter {
    
    private JPanel panel;
    private JPanel fillPanel;
    
    public ChoiceGroupDisplayPresenter() {
        fillPanel = new JPanel();
        fillPanel.setOpaque(false);
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        getPanel().add(panel, BorderLayout.CENTER);
    }
    
    public Collection<DesignComponent> getChildren() {
        PropertyValue elementsValue = getComponent().readProperty(ChoiceGroupCD.PROP_ELEMENTS);
        ArrayList<DesignComponent> elements = new ArrayList<DesignComponent>();
        Debug.collectAllComponentReferences(elementsValue, elements);
        return elements;
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        panel.removeAll();
        
        int y = 0;
        for (DesignComponent item : getChildren()) {
            ChoiceElementDisplayPresenter presenter = item.getPresenter(ChoiceElementDisplayPresenter.class);
            if (presenter == null) {
                continue;
            }
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = y ++;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(presenter.getView(), gbc);
            presenter.reload(deviceInfo);
        }
        panel.add(fillPanel, new GridBagConstraints(0, y, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }
    
}
