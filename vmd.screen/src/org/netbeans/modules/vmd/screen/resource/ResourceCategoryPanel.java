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

import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoryDescriptor;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceItemPresenter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public class ResourceCategoryPanel extends JPanel {

    private JPanel componentPanel;

    // this class will need to have a listener to document changes
    // the interest is in components added/removed and
    // also in the name changes of the individual components

    public ResourceCategoryPanel (ScreenResourceCategoryDescriptor category) {
        setLayout (new BorderLayout());
        setBackground (ResourcePanel.BACKGROUND_COLOR);

        Image image = category.getIcon ();
        JLabel label = new JLabel (category.getTitle (), image != null ? new ImageIcon (image) : null, SwingConstants.LEFT);
        label.setFont (getFont ().deriveFont (Font.BOLD));
        label.setToolTipText(category.getToolTip ());
        add (label,BorderLayout.NORTH);

        componentPanel = new JPanel();
        componentPanel.setBackground (ResourcePanel.BACKGROUND_COLOR);
//        componentPanel.setLayout (new FlowLayout(FlowLayout.LEFT, 5, 5));
        componentPanel.setLayout (new GridLayout(0, 1, 5, 5));
        add (componentPanel,BorderLayout.CENTER);
    }

    public void reload (ArrayList<ScreenResourceItemPresenter> list) {
        for (ScreenResourceItemPresenter presenter : list) {
            ResourceItemPanel item = new ResourceItemPanel (presenter.getRelatedComponent ()); // TODO - cache ResourceItemPanels
            item.reload ();
            componentPanel.add (item);
        }
    }
}
