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

import org.netbeans.modules.vmd.api.codegen.CodeWriter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.codegen.Parameter;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.elements.ChoiceElementCD;

import java.util.List;

/**
 * @author David Kaspar
 */
public class ChoiceGroupCode {

    public static final String PARAM_STRING = "elementString"; // NOI18N
    public static final String PARAM_IMAGE = "elementImage"; // NOI18N
    static final String PARAM_FONT = "elementFont"; // NOI18N
    static final String PARAM_SELECTED_ARRAY = "elementSelectedArray"; // NOI18N
    static final String PARAM_STRING_ARRAY = "elementStringArray"; // NOI18N
    static final String PARAM_IMAGE_ARRAY = "elementImageArray"; // NOI18N
    static final String PARAM_CHOICE_TYPE = "choiceType"; // NOI18N
    static final String PARAM_FIT_POLICY = "fitPolicy"; // NOI18N

    public static Parameter createStringParameter () {
        return new StringParameter();
    }

    public static Parameter createImageParameter () {
        return new ImageParameter();
    }

    public static Parameter createFontParameter() {
        return new FontParameter();
    }

    public static Parameter createSelectArrayParameter () {
        return new SelectedArrayParameter ();
    }

    public static Parameter createStringArrayParameter () {
        return new StringArrayParameter ();
    }

    public static Parameter createImageArrayParameter () {
        return new ImageArrayParameter ();
    }

    public static Parameter createChoiceTypeParameter () {
        return new ChoiceTypeParameter ();
    }

    public static Parameter createFitPolicyParameter () {
        return new FitPolicyParameter ();
    }

    private static class StringParameter implements Parameter {

        public String getParameterName () {
            return PARAM_STRING;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty(ChoiceGroupCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            DesignComponent listElement = array.get (index).getComponent ();
            PropertyValue string = listElement.readProperty(ChoiceElementCD.PROP_STRING);
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), string);
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ChoiceGroupCD.PROP_ELEMENTS);
        }

        public int getCount (DesignComponent component) {
            PropertyValue propertyValue = component.readProperty (ChoiceGroupCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            return array.size ();
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return true;
        }

    }

    private static class ImageParameter implements Parameter {

        public String getParameterName () {
            return PARAM_IMAGE;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty(ChoiceGroupCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            DesignComponent choiceElement = array.get (index).getComponent ();
            PropertyValue string = choiceElement.readProperty(ChoiceElementCD.PROP_IMAGE);
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), string);
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ChoiceGroupCD.PROP_ELEMENTS);
        }

        public int getCount (DesignComponent component) {
            PropertyValue propertyValue = component.readProperty (ChoiceGroupCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            return array.size ();
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return true;
        }

    }

    private static class FontParameter implements Parameter {

        public String getParameterName () {
            return PARAM_FONT;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty(ChoiceGroupCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            DesignComponent choiceElement = array.get (index).getComponent ();
            PropertyValue string = choiceElement.readProperty(ChoiceElementCD.PROP_FONT);
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), string);
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ChoiceGroupCD.PROP_ELEMENTS);
        }

        public int getCount (DesignComponent component) {
            PropertyValue propertyValue = component.readProperty (ChoiceGroupCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            return array.size ();
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return true;
        }

    }

    private static class SelectedArrayParameter implements Parameter {

        public String getParameterName () {
            return PARAM_SELECTED_ARRAY;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty(ChoiceGroupCD.PROP_ELEMENTS);
            List<PropertyValue> elementsArray = propertyValue.getArray ();
            CodeWriter writer = section.getWriter ();

            writer.write ("new boolean[] { "); // NOI18N

            for (int i = 0; i < elementsArray.size(); i ++) {
                if (i > 0) {
                    writer.write (", "); // NOI18N
                }
                PropertyValue choiceElementValue = elementsArray.get (i);
                DesignComponent choiceElement = choiceElementValue.getComponent ();
                PropertyValue string = choiceElement.readProperty(ChoiceElementCD.PROP_SELECTED);
                MidpCodeSupport.generateCodeForPropertyValue (writer, string);
            }

            writer.write (" }"); // NOI18N
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ChoiceGroupCD.PROP_ELEMENTS);
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            throw Debug.illegalState();
        }

    }

    private static class StringArrayParameter implements Parameter {

        public String getParameterName () {
            return PARAM_STRING_ARRAY;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty(ChoiceGroupCD.PROP_ELEMENTS);
            List<PropertyValue> elementsArray = propertyValue.getArray ();
            CodeWriter writer = section.getWriter ();

            writer.write ("new String[] { "); // NOI18N

            for (int i = 0; i < elementsArray.size(); i ++) {
                if (i > 0) {
                    writer.write (", "); // NOI18N
                }
                PropertyValue choiceElementValue = elementsArray.get (i);
                DesignComponent choiceElement = choiceElementValue.getComponent ();
                PropertyValue string = choiceElement.readProperty(ChoiceElementCD.PROP_STRING);
                MidpCodeSupport.generateCodeForPropertyValue (writer, string);
            }

            writer.write (" }"); // NOI18N
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ChoiceGroupCD.PROP_ELEMENTS);
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            throw Debug.illegalState();
        }

    }

    private static class ImageArrayParameter implements Parameter {

        public String getParameterName () {
            return PARAM_IMAGE_ARRAY;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty(ChoiceGroupCD.PROP_ELEMENTS);
            List<PropertyValue> elementsArray = propertyValue.getArray ();
            CodeWriter writer = section.getWriter ();

            writer.write ("new Image[] { "); // NOI18N

            for (int i = 0; i < elementsArray.size(); i ++) {
                if (i > 0) {
                    writer.write (", "); // NOI18N
                }
                PropertyValue choiceElementValue = elementsArray.get (i);
                DesignComponent choiceElement = choiceElementValue.getComponent ();
                PropertyValue image = choiceElement.readProperty(ChoiceElementCD.PROP_IMAGE);
                MidpCodeSupport.generateCodeForPropertyValue (writer, image);
            }

            writer.write (" }"); // NOI18N
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ChoiceGroupCD.PROP_ELEMENTS);
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            throw Debug.illegalState();
        }

    }

    private static class ChoiceTypeParameter extends MidpParameter {

        protected ChoiceTypeParameter () {
            super (PARAM_CHOICE_TYPE);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (ChoiceGroupCD.PROP_CHOICE_TYPE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                int value = MidpTypes.getInteger (propertyValue);
                switch (value) {
                    case ChoiceSupport.VALUE_EXCLUSIVE:
                        section.getWriter ().write ("Choice.EXCLUSIVE"); // NOI18N
                        break;
                    case ChoiceSupport.VALUE_MULTIPLE:
                        section.getWriter ().write ("Choice.MULTIPLE"); // NOI18N
                        break;
                    case ChoiceSupport.VALUE_POPUP:
                        section.getWriter ().write ("Choice.POPUP"); // NOI18N
                        break;
                    default:
                        throw Debug.illegalState ();
                }
                return;
            }
            super.generateParameterCode (component, section, index);
        }

    }

    private static class FitPolicyParameter extends MidpParameter {

        protected FitPolicyParameter () {
            super (PARAM_FIT_POLICY);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (ChoiceGroupCD.PROP_FIT_POLICY);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                int value = MidpTypes.getInteger (propertyValue);
                switch (value) {
                    case ChoiceSupport.VALUE_TEXT_WRAP_DEFAULT:
                        section.getWriter ().write ("Choice.TEXT_WRAP_DEFAULT"); // NOI18N
                        break;
                    case ChoiceSupport.VALUE_TEXT_WRAP_ON:
                        section.getWriter ().write ("Choice.TEXT_WRAP_ON"); // NOI18N
                        break;
                    case ChoiceSupport.VALUE_TEXT_WRAP_OFF:
                        section.getWriter ().write ("Choice.TEXT_WRAP_OFF"); // NOI18N
                        break;
                    default:
                        throw Debug.illegalState ();
                }
                return;
            }
            super.generateParameterCode (component, section, index);
        }

    }

}

