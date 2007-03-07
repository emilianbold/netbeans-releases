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
 * @author breh
 *
 */
public class FormDisplayPresenter extends DisplayableDisplayPresenter {

    private ArrayList<ScreenDisplayPresenter> presenters;
    private JPanel fillPanel;


    public FormDisplayPresenter () {
        getPanel ().getContentPanel ().setLayout (new GridBagLayout ());
        fillPanel = new JPanel ();
        fillPanel.setOpaque (false);
    }

    public Collection<DesignComponent> getChildren () {
        PropertyValue itemsValue = getComponent ().readProperty (FormCD.PROP_ITEMS);
        ArrayList<DesignComponent> items = new ArrayList<DesignComponent> ();
        Debug.collectAllComponentReferences (itemsValue, items);
        return items;
    }

    public void reload (ScreenDeviceInfo deviceInfo) {
        super.reload (deviceInfo);

        JPanel contentPanel = getPanel ().getContentPanel ();
        contentPanel.removeAll ();

        presenters = new ArrayList<ScreenDisplayPresenter> ();
        int y = 0;
        for (DesignComponent item : getChildren ()) {
            ScreenDisplayPresenter presenter = item.getPresenter (ScreenDisplayPresenter.class);
            if (presenter == null)
                continue;
            presenters.add (presenter);
            GridBagConstraints gbc = new GridBagConstraints ();
            gbc.gridx = 0;
            gbc.gridy = y ++;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.BOTH;
            contentPanel.add (presenter.getView (), gbc);
            presenter.reload (deviceInfo);
        }
        contentPanel.add (fillPanel, new GridBagConstraints (0, y, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets (0, 0, 0, 0), 0, 0));
    }

}
