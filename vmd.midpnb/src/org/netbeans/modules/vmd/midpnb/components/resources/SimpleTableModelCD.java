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

package org.netbeans.modules.vmd.midpnb.components.resources;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midpnb.components.displayables.AbstractInfoScreenCD;
import org.netbeans.modules.vmd.midpnb.components.properteditors.PropertyEditorTableModel;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodeSupport;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public class SimpleTableModelCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.SimpleTableModel"); // NOI18N

    public static final TypeID TYPEID_VALUES = MidpTypes.TYPEID_JAVA_LANG_STRING.getArrayType ().getArrayType ();
    public static final TypeID TYPEID_COLUMN_NAMES = MidpTypes.TYPEID_JAVA_LANG_STRING.getArrayType ();

    public static final String PROP_VALUES = "values"; // NOI18N
    public static final String PROP_COLUMN_NAMES = "columnNames"; // NOI18N

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ClassCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public void postInitialize (DesignComponent component) {
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY);
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_VALUES, TYPEID_VALUES, PropertyValue.createNull (), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor (PROP_COLUMN_NAMES, TYPEID_COLUMN_NAMES, PropertyValue.createNull (), true, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter (DesignEventFilterResolver.THIS_COMPONENT)
            .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                 .addProperty("Values", new PropertyEditorTableModel(), PROP_VALUES); // TODO - Column Names
    }

    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpParameter.create (PROP_VALUES, PROP_COLUMN_NAMES))
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (PROP_VALUES, PROP_COLUMN_NAMES))
            .addSetters (MidpSetter.createSetter ("setValues", MidpVersionable.MIDP_2).addParameters (PROP_VALUES))
            .addSetters (MidpSetter.createSetter ("setColumnNames", MidpVersionable.MIDP_2).addParameters (PROP_COLUMN_NAMES));
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList (
            // properties
            createPropertiesPresenter (),
            // code
            createSetterPresenter (),
            MidpCustomCodeSupport.createAddImportPresenter ()
        );
    }
    
}
