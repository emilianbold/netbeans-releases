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
package org.netbeans.modules.vmd.midp.components.commands;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.FolderPositionControllerFactory;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.actions.EditDependencyPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author David Kaspar
 */

public final class CommandCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Command"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/command_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/command_32.png"; // NOI18N
    
    public static final int VALUE_SCREEN = 1;
    public static final int VALUE_BACK = 2;
    public static final int VALUE_CANCEL = 3;
    public static final int VALUE_OK = 4;
    public static final int VALUE_HELP = 5;
    public static final int VALUE_STOP = 6;
    public static final int VALUE_EXIT = 7;
    public static final int VALUE_ITEM = 8;
    
    public static final String PROP_LABEL = "label"; // NOI18N
    public static final String PROP_LONG_LABEL = "longLabel"; // NOI18N
    public static final String PROP_TYPE = "type"; // NOI18N
    public static final String PROP_PRIORITY = "priority"; // NOI18N
    public static final String PROP_ORDINARY = "ordinary"; // NOI18N

    private static Map<String, PropertyValue> typeValues;

    static {
        MidpTypes.registerIconResource (TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (ClassCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return  Arrays.asList (
            new PropertyDescriptor (PROP_LABEL, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor (PROP_LONG_LABEL, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull (), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor (PROP_TYPE, MidpTypes.TYPEID_INT, PropertyValue.createNull (), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor (PROP_PRIORITY, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(0), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor (PROP_ORDINARY, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(true), false, false, Versionable.FOREVER)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter() 
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty("Label", PropertyEditorString.createInstance(), PROP_LABEL) 
                    .addProperty("Long Label", PropertyEditorString.createInstance(), PROP_LONG_LABEL) 
                    .addProperty("Type", PropertyEditorComboBox.createInstance(getTypes(), TYPEID), PROP_TYPE)
                    .addProperty("Priority", PropertyEditorNumber.createIntegerInstance(), PROP_PRIORITY); 
    }

    public static Map<String, PropertyValue> getTypes() {
        if (typeValues == null) {
            typeValues = new TreeMap<String, PropertyValue>();
            typeValues.put("SCREEN", MidpTypes.createIntegerValue(VALUE_SCREEN)); // NOI18N
            typeValues.put("BACK", MidpTypes.createIntegerValue(VALUE_BACK)); // NOI18N
            typeValues.put("CANCEL", MidpTypes.createIntegerValue(VALUE_CANCEL)); // NOI18N
            typeValues.put("OK", MidpTypes.createIntegerValue(VALUE_OK)); // NOI18N
            typeValues.put("HELP", MidpTypes.createIntegerValue(VALUE_HELP)); // NOI18N
            typeValues.put("STOP", MidpTypes.createIntegerValue(VALUE_STOP)); // NOI18N
            typeValues.put("EXIT", MidpTypes.createIntegerValue(VALUE_EXIT)); // NOI18N
            typeValues.put("ITEM", MidpTypes.createIntegerValue(VALUE_ITEM)); // NOI18N
        }
        
        return typeValues;
    }

    private static Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
                .addParameters (MidpParameter.create (PROP_LABEL, PROP_LONG_LABEL, PROP_PRIORITY))
                .addParameters (new CodeCommandTypeParameter ())
                .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP).addParameters (PROP_LABEL, PROP_TYPE, PROP_PRIORITY))
                .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2).addParameters (PROP_LABEL, PROP_LONG_LABEL, PROP_TYPE, PROP_PRIORITY));
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // properties
            createPropertiesPresenter(),
            // action
            EditDependencyPresenter.createEditablePresenter(),
            // inspector
            new InspectorFolderComponentPresenter(true),
            InspectorPositionPresenter.create(FolderPositionControllerFactory.createHierarchical()),
            // code
            createSetterPresenter ()
        );
    }

    private static class CodeCommandTypeParameter extends MidpParameter {

        public static final String PARAM = "type"; // NOI18N

        protected CodeCommandTypeParameter () {
            super (PARAM);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (PROP_TYPE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                int i = MidpTypes.getInteger (propertyValue);
                switch (i) {
                    case VALUE_SCREEN: section.getWriter ().write ("Command.SCREEN"); return; // NOI18N
                    case VALUE_BACK: section.getWriter ().write ("Command.BACK"); return; // NOI18N
                    case VALUE_CANCEL: section.getWriter ().write ("Command.CANCEL"); return; // NOI18N
                    case VALUE_OK: section.getWriter ().write ("Command.OK"); return; // NOI18N
                    case VALUE_HELP: section.getWriter ().write ("Command.HELP"); return; // NOI18N
                    case VALUE_STOP: section.getWriter ().write ("Command.STOP"); return; // NOI18N
                    case VALUE_EXIT: section.getWriter ().write ("Command.EXIT"); return; // NOI18N
                    case VALUE_ITEM: section.getWriter ().write ("Command.ITEM"); return; // NOI18N
                }
            }
            super.generateParameterCode (component, section, index);
        }

        @Override
        public boolean isRequiredToBeSet (DesignComponent component) {
            return true;
        }

    }

}
