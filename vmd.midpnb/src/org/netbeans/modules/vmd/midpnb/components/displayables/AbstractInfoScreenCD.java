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

package org.netbeans.modules.vmd.midpnb.components.displayables;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.properties.common.TextFieldBC;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorResourcesComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public class AbstractInfoScreenCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.AbstractInfoScreen"); // NOI18N

    private static final String PROP_IMAGE = "image"; //NOI18N
    private static final String PROP_TEXT = "text"; //NOI18N
    private static final String PROP_TEXT_FONT = "textFont"; //NOI18N

    public static final String[] MIDP_NB_LIBRARY = {"NetBeans MIDP Components"}; //NOI18N

    public AbstractInfoScreenCD () {
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CanvasCD.TYPEID, TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public void postInitialize (DesignComponent component) {
        MidpProjectSupport.addLibraryToProject (component.getDocument (), MIDP_NB_LIBRARY);
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList (
            new PropertyDescriptor(PROP_IMAGE, ImageCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_TEXT, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_TEXT_FONT, FontCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
            .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty("Text", PropertyEditorString.createInstance(), new TextFieldBC(), PROP_TEXT)
                .addProperty("Image", PropertyEditorResourcesComboBox.createImagePropertyEditor(), PROP_IMAGE)
                .addProperty("Text Font", PropertyEditorResourcesComboBox.createFontPropertyEditor(), PROP_TEXT_FONT);
    }

    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpParameter.create (PROP_TEXT, PROP_IMAGE, PROP_TEXT_FONT))
            .addSetters (MidpSetter.createSetter ("setImage", MidpVersionable.MIDP_2).addParameters (PROP_IMAGE))
            .addSetters (MidpSetter.createSetter ("setText", MidpVersionable.MIDP_2).addParameters (PROP_TEXT))
            .addSetters (MidpSetter.createSetter ("setTextFont", MidpVersionable.MIDP_2).addParameters (PROP_TEXT_FONT));
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList (
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter (),
            MidpCodePresenterSupport.createAddImportPresenter ()
        );
    }

}
