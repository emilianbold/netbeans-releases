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
package org.netbeans.modules.vmd.midp.components.elements;

import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.items.ChoiceGroupCD;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.inspector.controllers.ComponentsCategoryPC;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorResourcesComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;

/**
 * @author David Kaspar
 */

public final class ChoiceElementCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#ChoiceElement"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/item_16.png"; // NOI18N

    public static final String PROP_STRING = "string"; // NOI18N
    public static final String PROP_IMAGE = ImageCD.PROP_IMAGE;
    public static final String PROP_SELECTED = "selected"; // NOI18N
    public static final String PROP_FONT = "font" ;  // NOI18N

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor (null, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_STRING, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_IMAGE, ImageCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_SELECTED, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(false), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_FONT, FontCD.TYPEID, PropertyValue.createNull (), true, true, MidpVersionable.MIDP_2)
        );
    }

    public PaletteDescriptor getPaletteDescriptor() {
        return new PaletteDescriptor (MidpPaletteProvider.CATEGORY_ELEMENTS, "Choice Element", "Choice Element", ICON_PATH, null); // NOI18N
    }
    
     private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty("String", PropertyEditorString.createInstance(), PROP_STRING)
                .addProperty("Image", PropertyEditorResourcesComboBox.createImagePropertyEditor(), PROP_IMAGE)
                .addProperty("Selected", PropertyEditorBooleanUC.createInstance(), PROP_SELECTED)
                .addProperty("Font", PropertyEditorResourcesComboBox.createFontPropertyEditor(), PROP_FONT);
    }
    
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonActionsPresenters(presenters, true, true, false, true, true);
        MidpActionsSupport.addMoveActionPresenter(presenters, ChoiceGroupCD.PROP_ELEMENTS);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // general
            InfoPresenter.create(ElementSupport.createChoiceElementInfoResolver()),
            //inspector
            InspectorFolderPresenter.create(false),
            InspectorPositionPresenter.create(new ComponentsCategoryPC(MidpInspectorSupport.TYPEID_ELEMENTS)),
            //properties
            createPropertiesPresenter(),
            // delete
            DeleteDependencyPresenter.createDependentOnParentComponentPresenter (),
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_IMAGE),
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_FONT),
            new DeletePresenter() {
                protected void delete () {
                    DesignComponent component = getComponent ();
                    DesignComponent list = component.getParentComponent ();
                    ArraySupport.remove (list, ChoiceGroupCD.PROP_ELEMENTS, component);
                }
            }
        
        );
    }

}
