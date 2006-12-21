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

package org.netbeans.modules.vmd.midp.components.items;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorResourcesComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Karol Harezlak
 */
public class ImageItemCD extends ComponentDescriptor {
    
    private static Map<String, PropertyValue> appearanceValues;
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.ImageItem"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/item_16.png"; // NOI18N
    
    public static final String PROP_ALT_TEXT = "altText"; // NOI18N
    public static final String PROP_IMAGE = ImageCD.PROP_IMAGE; // NOI18N

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_ALT_TEXT, MidpTypes.createStringValue ("<Missing Image>")); // TODO - should it be here?
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_IMAGE, ImageCD.TYPEID, PropertyValue.createNull(), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor (PROP_ALT_TEXT, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull (), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor(ItemCD.PROP_APPEARANCE_MODE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (ItemCD.VALUE_PLAIN), false, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty("Alternate Text", PropertyEditorString.createInstance(), PROP_ALT_TEXT)
                .addProperty("Appearance", PropertyEditorComboBox.createInstance(getAppearanceValues(), TYPEID), ItemCD.PROP_APPEARANCE_MODE)
                .addProperty("Image", PropertyEditorResourcesComboBox.createImagePropertyEditor(), PROP_IMAGE);
    }
    
    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_ALT_TEXT, PROP_IMAGE))
                .addParameters (ItemCode.createAppearanceModeParameter ())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, PROP_IMAGE, ItemCD.PROP_LAYOUT, PROP_ALT_TEXT))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP_2).addParameters(ItemCD.PROP_LABEL, PROP_IMAGE, ItemCD.PROP_LAYOUT, PROP_ALT_TEXT, ItemCode.PARAM_APPEARANCE_MODE))
                .addSetters(MidpSetter.createSetter("setAltText", MidpVersionable.MIDP).addParameters(PROP_ALT_TEXT))
                .addSetters(MidpSetter.createSetter("setImage", MidpVersionable.MIDP).addParameters(PROP_IMAGE));
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),
                // delete
                DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_IMAGE)
        );
    }
    
    public static final Map<String, PropertyValue> getAppearanceValues() {
        if (appearanceValues == null) {
            appearanceValues = new TreeMap<String, PropertyValue>();
            appearanceValues.put("PLAIN", MidpTypes.createIntegerValue(ItemCD.VALUE_PLAIN));         // NOI18N
            appearanceValues.put("HYPERLINK", MidpTypes.createIntegerValue(ItemCD.VALUE_HYPERLINK)); // NOI18N
            appearanceValues.put("BUTTON", MidpTypes.createIntegerValue(ItemCD.VALUE_BUTTON));       // NOI18N
        }
        return appearanceValues;
    }

}
