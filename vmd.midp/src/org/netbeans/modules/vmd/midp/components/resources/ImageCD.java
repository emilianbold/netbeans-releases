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
package org.netbeans.modules.vmd.midp.components.resources;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.imagechooser.PropertyEditorImageChooser;

import java.util.Arrays;
import java.util.List;


/**
 * @author David Kaspar
 */

public final class ImageCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Image"); // NOI18N

    public static final String PROP_RESOURCE_PATH = "resourcePath";  // NOI18N
    // TODO - should be inlined to all CDs directly and should not be here
    public static final String PROP_IMAGE = "image";  // NOI18N

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor (ResourceCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList (
                new PropertyDescriptor(PROP_RESOURCE_PATH, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), false, true, MidpVersionable.MIDP)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty("Resource Path", new PropertyEditorImageChooser("png","jpg","gif"), PROP_RESOURCE_PATH);
    }

    private static Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
                .addParameters (new ImageResourceParameter ())
                .addParameters (new NullImageResourceParameter ())
                .addSetters (MidpSetter.createFactoryMethod (TYPEID, null, "Image", "createImage", MidpVersionable.MIDP).addParameters (ImageResourceParameter.PARAM_RESOURCE_PATH).addExceptions ("java.io.IOException")) // NOI18N
                .addSetters (MidpSetter.createFactoryMethod (TYPEID, null, "Image", "createImage", MidpVersionable.MIDP).addParameters (NullImageResourceParameter.PARAM_NULL_RESOURCE_PATH)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter ()
        );
    }

    private static class ImageResourceParameter extends MidpParameter {

        static final String PARAM_RESOURCE_PATH = "resourcePath"; // NOI18N

        protected ImageResourceParameter () {
            super (PARAM_RESOURCE_PATH);
        }

        @Override
        public boolean isRequiredToBeSet (DesignComponent component) {
            return component.readProperty (PROP_RESOURCE_PATH).getKind () != PropertyValue.Kind.NULL;
        }

    }

    private static class NullImageResourceParameter extends MidpParameter {

        static final String PARAM_NULL_RESOURCE_PATH = "nullResourcePath"; // NOI18N

        protected NullImageResourceParameter () {
            super (PARAM_NULL_RESOURCE_PATH);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            section.getWriter ().write ("0, 0"); // NOI18N
        }

        @Override
        public boolean isRequiredToBeSet (DesignComponent component) {
            return component.readProperty (PROP_RESOURCE_PATH).getKind () == PropertyValue.Kind.NULL;
        }

    }

}
