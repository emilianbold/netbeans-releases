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
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.PropertyEditorResource;
import org.netbeans.modules.vmd.midp.screen.display.StringItemDisplayPresenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */

public class StringItemCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.StringItem"); // NOI18N

    public static final String PROP_TEXT = "text"; // NOI18N
    public static final String PROP_FONT = "font"; // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
         return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_TEXT, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_FONT, FontCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(ItemCD.PROP_APPEARANCE_MODE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (ItemCD.VALUE_PLAIN), false, true, MidpVersionable.MIDP_2)
        );
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, ScreenDisplayPresenter.class);
        super.gatherPresenters (presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(StringItemCD.class, "DISP_StringItem_Text"), // NOI18N
                    PropertyEditorString.createInstance(NbBundle.getMessage(StringItemCD.class, "LBL_StringItem_Text")), PROP_TEXT) // NOI18N
                .addProperty(NbBundle.getMessage(StringItemCD.class, "DISP_StringItem_Appearance"), // NOI18N
                    PropertyEditorComboBox.createInstance(ImageItemCD.getAppearanceValues(), TYPEID,
                        NbBundle.getMessage(StringItemCD.class, "DISP_StringItem_Appearance_RB_LABEL"), // NOI18N
                        NbBundle.getMessage(StringItemCD.class, "DISP_StringItem_Appearance_UCLABEL")), ItemCD.PROP_APPEARANCE_MODE) // NOI18N
                .addProperty(NbBundle.getMessage(StringItemCD.class, "DISP_StringItem_Font"), PropertyEditorResource.createFontPropertyEditor(), PROP_FONT); // NOI18N
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_TEXT, PROP_FONT))
                .addParameters (ItemCode.createAppearanceModeParameter ())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, PROP_TEXT))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP_2).addParameters(ItemCD.PROP_LABEL, PROP_TEXT, ItemCode.PARAM_APPEARANCE_MODE))
                .addSetters(MidpSetter.createSetter("setText", MidpVersionable.MIDP).addParameters(PROP_TEXT)) // NOI18N
                .addSetters(MidpSetter.createSetter("setFont", MidpVersionable.MIDP_2).addParameters(PROP_FONT)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter(),
            // delete
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_FONT),
            // screen
            new StringItemDisplayPresenter(),
            // accept
            new MidpAcceptProducerKindPresenter().addType(FontCD.TYPEID, PROP_FONT),
            MidpAcceptTrensferableKindPresenter.createFontAcceptPresenter()
       );   
    }
    
}
