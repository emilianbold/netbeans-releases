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
 */
package org.netbeans.modules.vmd.structure.registry;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.*;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class RegistryWidget extends Widget {

    private static final Border BORDER_SEPARATOR = BorderFactory.createEmptyBorder (8, 0);
    private static final Border BORDER_IMAGE = BorderFactory.createEmptyBorder (4, 8, 4, 0);
    private static final Border BORDER_LABEL = BorderFactory.createEmptyBorder (8, 4);
    private static final Layout LAYOUT = LayoutFactory.createHorizontalLayout ();
    private static final Layout LAYOUT_NODES = LayoutFactory.createVerticalLayout (LayoutFactory.SerialAlignment.JUSTIFY, 10);

    private Widget nodes;

    public RegistryWidget (Scene scene, boolean visible, Image image, String label) {
        super (scene);

        setLayout (LAYOUT);

        SeparatorWidget separator = new SeparatorWidget (scene, SeparatorWidget.Orientation.VERTICAL);
        separator.setBorder (BORDER_SEPARATOR);
        addChild (separator);

        if (image != null) {
            ImageWidget imageWidget = new ImageWidget (scene, image);
            imageWidget.setBorder (BORDER_IMAGE);
            imageWidget.setOpaque (visible);
            imageWidget.setBackground (Color.WHITE);
            addChild (imageWidget);
        }

        LabelWidget labelWidget = new LabelWidget (scene, label);
        labelWidget.setBorder (BORDER_LABEL);
        labelWidget.setOpaque (visible);
        labelWidget.setBackground (Color.WHITE);
        if (visible)
            labelWidget.setFont (scene.getDefaultFont ().deriveFont (Font.BOLD));
        addChild (labelWidget);

        nodes = new Widget (scene);
        nodes.setLayout (LAYOUT_NODES);
        addChild (nodes);
    }

    public void addSub (String id, Widget widget) {
        nodes.addChild (widget);
        ((ObjectScene) widget.getScene ()).addObject (id, widget);
    }

}
