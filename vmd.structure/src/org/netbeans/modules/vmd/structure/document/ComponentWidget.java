/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.util.ImageUtilities;
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
        Image image = paletteDescriptor != null ? ImageUtilities.loadImage(paletteDescriptor.getSmallIcon()) : null;

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

        LabelWidget idWidget = new LabelWidget (scene, component.getComponentID () + " : " + (descriptor != null ? descriptor.getTypeDescriptor ().getThisType ().toString () : "<Unknown>")); // NOI18N
        idWidget.setFont (scene.getDefaultFont ().deriveFont (Font.BOLD));
        descriptionWidget.addChild (idWidget);

        if (descriptor != null)
            for (PropertyDescriptor propertyDescriptor : descriptor.getPropertyDescriptors ()) {
                String name = propertyDescriptor.getName ();
                PropertyValue value = component.readProperty (name);
                descriptionWidget.addChild (new LabelWidget (scene, name + "=" + value.serialize ())); // NOI18N
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
