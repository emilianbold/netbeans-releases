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

import org.netbeans.modules.vmd.api.codegen.*;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;

import java.util.*;

/**
 * @author David Kaspar
 */

public final class FontCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "javax.microedition.lcdui.Font"); // NOI18N

    public static final int VALUE_FACE_SYSTEM = 0;
    public static final int VALUE_FACE_MONOSPACE = 32;
    public static final int VALUE_FACE_PROPORTIONAL = 64;

    public static final int VALUE_FONT_STATIC_TEXT = 0;
    public static final int VALUE_FONT_INPUT_TEXT = 1;

    public static final int VALUE_SIZE_MEDIUM = 0;
    public static final int VALUE_SIZE_SMALL = 8;
    public static final int VALUE_SIZE_LARGE = 16;

    public static final int VALUE_STYLE_PLAIN = 0;
    public static final int VALUE_STYLE_BOLD = 1;
    public static final int VALUE_STYLE_ITALIC = 2;
    public static final int VALUE_STYLE_UNDERLINED = 4;

    public static final int VALUE_KIND_DEFAULT = 0;
    public static final int VALUE_KIND_CUSTOM = 1;
    public static final int VALUE_KIND_STATIC = 2;
    public static final int VALUE_KIND_INPUT = 3;

    public static final String PROP_FACE = "face"; // NOI18N
    public static final String PROP_STYLE = "style"; // NOI18N
    public static final String PROP_SIZE = "bold"; // NOI18N
    public static final String PROP_FONT_KIND = "fontKind"; // NOI18N
    
    public static final String LABEL_KIND_DEFAULT = "Default"; // NOI18N
    public static final String LABEL_KIND_CUSTOM = "Custom"; // NOI18N
    public static final String LABEL_KIND_STATIC = "Static"; // NOI18N
    public static final String LABEL_KIND_INPUT = "Input"; // NOI18N

    private static Map<String, PropertyValue> kindTypes;
    private static Map<String, PropertyValue> faceTypes;
    private static Map<String, PropertyValue> sizeTypes;
    private static Map<String, PropertyValue> styleTypes;

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (ResourceCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return  Arrays.asList(
            new PropertyDescriptor(PROP_FONT_KIND, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(VALUE_KIND_DEFAULT), false, false, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_FACE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(VALUE_FACE_SYSTEM), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_SIZE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(VALUE_SIZE_MEDIUM), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_STYLE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(VALUE_STYLE_PLAIN), false, true, MidpVersionable.MIDP)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty("Kind", PropertyEditorComboBox.createInstance(getKindTypes(), TYPEID), PROP_FONT_KIND)
                    .addProperty("Face", PropertyEditorComboBox.createInstance(getFaceTypes(), TYPEID, LABEL_KIND_CUSTOM), PROP_FACE)
                    .addProperty("Size", PropertyEditorComboBox.createInstance(getSizeTypes(), TYPEID, LABEL_KIND_CUSTOM), PROP_SIZE)
                    .addProperty("Style", PropertyEditorComboBox.createInstance(getStyleTypes(), TYPEID, LABEL_KIND_CUSTOM), PROP_STYLE);
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addSetters (new FontSetter ());
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter()
        );
    }

    public static Map<String, PropertyValue> getKindTypes() {
        if (kindTypes == null) {
            kindTypes = new TreeMap<String, PropertyValue>();
            kindTypes.put(LABEL_KIND_DEFAULT,MidpTypes.createIntegerValue(VALUE_KIND_DEFAULT)); // NOI18N
            kindTypes.put(LABEL_KIND_CUSTOM, MidpTypes.createIntegerValue(VALUE_KIND_CUSTOM)); // NOI18N
            kindTypes.put(LABEL_KIND_STATIC, MidpTypes.createIntegerValue(VALUE_KIND_STATIC)); // NOI18N
            kindTypes.put(LABEL_KIND_INPUT, MidpTypes.createIntegerValue(VALUE_KIND_INPUT)); // NOI18N
        }
        return kindTypes;
    }
    
    public static Map<String, PropertyValue> getFaceTypes() {
        if (faceTypes == null) {
            faceTypes = new TreeMap<String, PropertyValue>();
            faceTypes.put("SYSTEM", MidpTypes.createIntegerValue(VALUE_FACE_SYSTEM));             // NOI18N
            faceTypes.put("MONOSPACE", MidpTypes.createIntegerValue(VALUE_FACE_MONOSPACE));       // NOI18N
            faceTypes.put("PROPORTIONAL", MidpTypes.createIntegerValue(VALUE_FACE_PROPORTIONAL)); // NOI18N
        }
        return faceTypes;
    }

    public static Map<String, PropertyValue>  getSizeTypes() {
        if (sizeTypes == null) {
            sizeTypes = new TreeMap<String, PropertyValue>();
            sizeTypes.put("MEDIUM", MidpTypes.createIntegerValue(VALUE_SIZE_MEDIUM)); // NOI18N
            sizeTypes.put("SMALL", MidpTypes.createIntegerValue(VALUE_SIZE_SMALL));   // NOI18N
            sizeTypes.put("LARGE", MidpTypes.createIntegerValue(VALUE_SIZE_LARGE));   // NOI18N
        }
        return sizeTypes;
    }

    public static Map<String, PropertyValue>  getStyleTypes() {
        if (styleTypes == null) {
            styleTypes = new TreeMap<String, PropertyValue>();
            styleTypes.put("PLAIN", MidpTypes.createIntegerValue(VALUE_STYLE_PLAIN));          // NOI18N
            styleTypes.put("BOLD", MidpTypes.createIntegerValue(VALUE_STYLE_BOLD));            // NOI18N
            styleTypes.put("ITALIC", MidpTypes.createIntegerValue(VALUE_STYLE_ITALIC));        // NOI18N
            styleTypes.put("UNDERLINE", MidpTypes.createIntegerValue(VALUE_STYLE_UNDERLINED)); // NOI18N
        }
        return styleTypes;
    }

    private static class FontSetter implements Setter {

        public boolean isConstructor () {
            return true;
        }

        public TypeID getConstructorRelatedTypeID () {
            return TYPEID;
        }

        public String getSetterName () {
            return "Font.getFont"; // NOI18N
        }

        public Versionable getVersionable () {
            return MidpVersionable.MIDP;
        }

        public void generateSetterCode (MultiGuardedSection section, DesignComponent component, Map<String, Parameter> name2parameter) {
            CodeWriter writer = section.getWriter ();
            writer.write (CodeReferencePresenter.generateDirectAccessCode (component)).write (" = "); // NOI18N

            PropertyValue propertyValue = component.readProperty (PROP_FONT_KIND);
            switch (MidpTypes.getInteger (propertyValue)) {
                case VALUE_KIND_DEFAULT:
                    writer.write ("Font.getDefaultFont ()"); // NOI18N
                    break;
                case VALUE_KIND_INPUT:
                    writer.write ("Font.getFont (Font.FONT_INPUT_TEXT)"); // NOI18N
                    break;
                case VALUE_KIND_STATIC:
                    writer.write ("Font.getFont (Font.FONT_STATIC_TEXT)"); // NOI18N
                    break;
                case VALUE_KIND_CUSTOM:

                    propertyValue = component.readProperty (PROP_FACE);
                    if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                        switch (MidpTypes.getInteger (propertyValue)) {
                            case VALUE_FACE_SYSTEM: writer.write ("Font.FACE_SYSTEM"); break; // NOI18N
                            case VALUE_FACE_MONOSPACE: writer.write ("Font.FACE_MONOSPACE"); break; // NOI18N
                            case VALUE_FACE_PROPORTIONAL: writer.write ("Font.FACE_PROPPORTIONAL"); break; // NOI18N
                            default: throw Debug.illegalState ();
                        }
                    } else
                        MidpCodeSupport.generateCodeForPropertyValue (writer, propertyValue);

                    writer.write (", "); // NOI18N

                    propertyValue = component.readProperty (PROP_STYLE);
                    if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                        switch (MidpTypes.getInteger (propertyValue)) {
                            case VALUE_STYLE_PLAIN: writer.write ("Font.STYLE_PLAIN"); break; // NOI18N
                            case VALUE_STYLE_BOLD: writer.write ("Font.STYLE_BOLD"); break; // NOI18N
                            case VALUE_STYLE_ITALIC: writer.write ("Font.STYLE_ITALIC"); break; // NOI18N
                            case VALUE_STYLE_UNDERLINED: writer.write ("Font.STYLE_UNDERLINED"); break; // NOI18N
                            default: throw Debug.illegalState ();
                        }
                    } else
                        MidpCodeSupport.generateCodeForPropertyValue (writer, propertyValue);

                    writer.write (", "); // NOI18N

                    propertyValue = component.readProperty (PROP_SIZE);
                    if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                        switch (MidpTypes.getInteger (propertyValue)) {
                            case VALUE_SIZE_SMALL: writer.write ("Font.SIZE_SMALL"); break; // NOI18N
                            case VALUE_SIZE_MEDIUM: writer.write ("Font.SIZE_MEDIUM"); break; // NOI18N
                            case VALUE_SIZE_LARGE: writer.write ("Font.SIZE_LARGE"); break; // NOI18N
                            default: throw Debug.illegalState ();
                        }
                    } else
                        MidpCodeSupport.generateCodeForPropertyValue (writer, propertyValue);

                    writer.write (")"); // NOI18N
                    break;
                default:
                    throw Debug.illegalState ();
            }

            writer.write (";\n"); // NOI18N
        }

        public List<String> getParameters () {
            return Collections.emptyList ();
        }
    }

}
