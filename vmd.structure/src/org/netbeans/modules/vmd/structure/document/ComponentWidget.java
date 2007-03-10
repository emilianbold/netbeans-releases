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
package org.netbeans.modules.vmd.structure.document;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.vmd.api.model.*;
import org.openide.util.Utilities;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class ComponentWidget extends Widget {

    private static final Layout LAYOUT = LayoutFactory.createHorizontalFlowLayout ();
    private static final Layout LAYOUT_NODES = LayoutFactory.createVerticalFlowLayout (LayoutFactory.SerialAlignment.JUSTIFY, 10);
    private static final Layout LAYOUT_LABEL = LayoutFactory.createVerticalFlowLayout ();

    private static final Border BORDER_SEPARATOR = BorderFactory.createEmptyBorder (8, 0);
    private static final Border BORDER_IMAGE = BorderFactory.createEmptyBorder (4, 8, 4, 0);
    private static final Border BORDER_LABEL = BorderFactory.createEmptyBorder (8, 4);

    private Widget components;
    private DesignComponent component;

    public ComponentWidget (DocumentScene scene, DesignComponent component, boolean selected) {
        super (scene);
        this.component = component;

        ComponentDescriptor descriptor = component.getComponentDescriptor ();
        Color validityColor = descriptor != null ? Color.WHITE : Color.MAGENTA;
        PaletteDescriptor paletteDescriptor = descriptor != null ? descriptor.getPaletteDescriptor () : null;
        Image image = paletteDescriptor != null ? Utilities.loadImage(paletteDescriptor.getSmallIcon()) : null;

        setLayout (org.netbeans.modules.vmd.structure.document.ComponentWidget.LAYOUT);

        SeparatorWidget separator = new SeparatorWidget (scene, SeparatorWidget.Orientation.VERTICAL);
        separator.setBorder (BORDER_SEPARATOR);
        addChild (separator);

        if (image != null) {
            ImageWidget imageWidget = new ImageWidget (scene, image);
            imageWidget.setBorder (BORDER_IMAGE);
            imageWidget.setOpaque (true);
            imageWidget.setBackground (selected ? DocumentScene.COLOR_SELECTED : validityColor);
            addChild (imageWidget);
        }

        Widget descriptionWidget = new Widget (scene);
        descriptionWidget.setBorder (BORDER_LABEL);
        descriptionWidget.setOpaque (true);
        descriptionWidget.setBackground (selected ? DocumentScene.COLOR_SELECTED : validityColor);
        descriptionWidget.setLayout (LAYOUT_LABEL);
        addChild (descriptionWidget);

        LabelWidget idWidget = new LabelWidget (scene, component.getComponentID () + " : " + (descriptor != null ? descriptor.getTypeDescriptor ().getThisType ().toString () : "<Unknown>"));
        idWidget.setFont (scene.getDefaultFont ().deriveFont (Font.BOLD));
        descriptionWidget.addChild (idWidget);

        if (descriptor != null)
            for (PropertyDescriptor propertyDescriptor : descriptor.getPropertyDescriptors ()) {
                String name = propertyDescriptor.getName ();
                PropertyValue value = component.readProperty (name);
                descriptionWidget.addChild (new LabelWidget (scene, name + "=" + value.serialize ()));
            }

        components = new Widget (scene);
        components.setLayout (LAYOUT_NODES);
        addChild (components);

        descriptionWidget.getActions ().addAction (scene.getHoverAction ());
        getActions ().addAction (scene.getSelectAction ());
    }

    public void addChildComponentWidget (ComponentWidget widget) {
        components.addChild (widget);
        ((ObjectScene) widget.getScene ()).addObject (widget.getComponent (), widget);
    }

    public DesignComponent getComponent () {
        return component;
    }

}
