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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.vmd.screen.resource;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoryDescriptor;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceItemPresenter;
import org.netbeans.modules.vmd.screen.ScreenAccessController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author David Kaspar
 */
public class ResourcePanel extends JPanel {

    public static final Color BACKGROUND_COLOR = new Color (0xFCFAF5);
    private ScreenAccessController controller;
    private JPanel fillPanel;

    public ResourcePanel (ScreenAccessController controller) {
        this.controller = controller;
        setBackground (BACKGROUND_COLOR);
        setLayout (new GridBagLayout ());
        fillPanel = new JPanel ();
        fillPanel.setBackground (BACKGROUND_COLOR);
    }
    
    // called in AWT and document transation
    public void reload () {
        DesignComponent editedScreen = controller.getEditedScreen ();
        Map<ScreenResourceCategoryDescriptor, ArrayList<ScreenResourceItemPresenter>> categories = ResourcePanelSupport.getCategoryDescriptors (editedScreen);
        ResourcePanelSupport.resolveResources (controller.getDocument (), editedScreen, categories);
        List<ScreenResourceCategoryDescriptor> sortedCategories = ResourcePanelSupport.getSortedCategories (categories.keySet ());

        removeAll ();
        int y = 0;
        for (ScreenResourceCategoryDescriptor category : sortedCategories) {
            ResourceCategoryPanel categoryPanel = new ResourceCategoryPanel (category); // TODO - cache ResourceCategoryPanel instances
            categoryPanel.reload (categories.get (category));
            add (categoryPanel, new GridBagConstraints (0, y ++, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets (0, 0, 0, 0), 0, 0));
        }
        add (fillPanel, new GridBagConstraints (0, y, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets (0, 0, 0, 0), 0, 0));
    }

}
