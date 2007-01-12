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
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.date.PropertyEditorDate;
import org.netbeans.modules.vmd.midp.propertyeditors.timezone.PropertyEditorTimeZone;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Karol Harezlak
 */

public class DateFieldCD extends ComponentDescriptor {

    private static Map<String, PropertyValue> inputModeValues;

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.DateField"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/item_16.png"; // NOI18N

    public static final int VALUE_DATE = 1;
    public static final int VALUE_TIME = 2;
    public static final int VALUE_DATE_TIME = 3;

    public static final String PROP_INPUT_MODE = "inputMode";  // NOI18N
    public static final String PROP_DATE = "date";  // NOI18N
    public static final String PROP_TIME_ZONE = "timeZone";  // NOI18N

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_DATE, PropertyValue.createUserCode ("new java.util.Date (System.currentTimeMillis ())"));
        component.writeProperty (PROP_INPUT_MODE, MidpTypes.createIntegerValue (VALUE_DATE_TIME));
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_DATE, MidpTypes.TYPEID_LONG, PropertyValue.createNull (), true, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_TIME_ZONE, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_INPUT_MODE, MidpTypes.TYPEID_INT, PropertyValue.createNull (), false, true, MidpVersionable.MIDP)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty("Date", "dd.mm.yyyy hh:mm:ss", PropertyEditorDate.createInstance(), PROP_DATE)
                .addProperty("Time Zone", PropertyEditorTimeZone.createInstance(), PROP_TIME_ZONE)
                .addProperty("Input Mode", PropertyEditorComboBox.createInstance(getInputModeValues(), TYPEID), PROP_INPUT_MODE);
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(MidpParameter.create(PROP_DATE))
                .addParameters (new TimeZoneParameter ())
                .addParameters (new InputModeParameter ())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, InputModeParameter.PARAM_INPUT_MODE))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, InputModeParameter.PARAM_INPUT_MODE, TimeZoneParameter.PARAM_TIME_ZONE))
                .addSetters(MidpSetter.createSetter("setDate", MidpVersionable.MIDP).addParameters(PROP_DATE))
                .addSetters(MidpSetter.createSetter("setInputMode", MidpVersionable.MIDP).addParameters(PROP_INPUT_MODE));
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter()
        );
    }

    public static Map<String, PropertyValue> getInputModeValues() {
        if (inputModeValues == null) {
            inputModeValues = new TreeMap<String, PropertyValue>();
            inputModeValues.put("DATE", MidpTypes.createIntegerValue(DateFieldCD.VALUE_DATE));           // NOI18N
            inputModeValues.put("DATE_TIME", MidpTypes.createIntegerValue(DateFieldCD.VALUE_DATE_TIME)); // NOI18N
            inputModeValues.put("TIME", MidpTypes.createIntegerValue(DateFieldCD.VALUE_TIME));            // NOI18N
        }

        return inputModeValues;
    }

    private static class InputModeParameter extends MidpParameter {

        public static final String PARAM_INPUT_MODE = "inputMode"; // NOI18N

        public InputModeParameter () {
            super (PARAM_INPUT_MODE);
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (PROP_INPUT_MODE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                int value = MidpTypes.getInteger (propertyValue);
                switch (value) {
                    case VALUE_DATE:
                        section.getWriter ().write ("DateField.DATE");
                        return;
                    case VALUE_DATE_TIME:
                        section.getWriter ().write ("DateField.DATE_TIME");
                        return;
                    case VALUE_TIME:
                        section.getWriter ().write ("DateField.TIME");
                        return;
                    default:
                        throw Debug.illegalState ();
                }
            }
            super.generateParameterCode (component, section, index);
        }

    }

    private static class TimeZoneParameter extends MidpParameter {

        public static final String PARAM_TIME_ZONE = "timeZone"; // NOI18N

        public TimeZoneParameter () {
            super (PROP_TIME_ZONE);
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (PROP_TIME_ZONE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                String value = MidpTypes.getString (propertyValue);
                section.getWriter ().write ("TimeZone.getTimeZone (" + value + ")");
            } else
                super.generateParameterCode (component, section, index);
        }

    }

}
