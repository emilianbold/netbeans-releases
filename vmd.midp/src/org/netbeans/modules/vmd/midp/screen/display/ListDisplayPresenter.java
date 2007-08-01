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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.components.displayables.ListCD;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Anton Chechel
 */
public class ListDisplayPresenter extends DisplayableDisplayPresenter {
    
    public ListDisplayPresenter() {
        JPanel panel = getPanel().getContentPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    }
    
    @Override
    public Collection<DesignComponent> getChildren() {
        PropertyValue elementsValue = getComponent().readProperty(ListCD.PROP_ELEMENTS);
        List<DesignComponent> elements = new ArrayList<DesignComponent>();
        Debug.collectAllComponentReferences(elementsValue, elements);
        return elements;
    }
    
    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        JPanel contentPanel = getPanel().getContentPanel();
        contentPanel.removeAll();
        
        for (DesignComponent elements : getChildren()) {
            ScreenDisplayPresenter presenter = elements.getPresenter(ScreenDisplayPresenter.class);
            if (presenter == null) {
                continue;
            }
            
            contentPanel.add(presenter.getView());
            presenter.reload(deviceInfo);
        }
        contentPanel.add(Box.createVerticalGlue());
    }
    
}
