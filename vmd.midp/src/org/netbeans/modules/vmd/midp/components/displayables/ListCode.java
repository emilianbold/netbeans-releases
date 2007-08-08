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
package org.netbeans.modules.vmd.midp.components.displayables;

import org.netbeans.modules.vmd.api.codegen.*;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.commands.ListSelectCommandCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.items.ChoiceSupport;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;

import javax.swing.text.StyledDocument;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public class ListCode {

    private static final String LIST_ACTION_METHOD_SUFFIX = "Action";

    public static final String PARAM_STRING = "elementString"; // NOI18N
    public static final String PARAM_IMAGE = "elementImage"; // NOI18N
    static final String PARAM_FONT = "elementFont"; // NOI18N
    static final String PARAM_SELECTED_ARRAY = "elementSelectedArray"; // NOI18N
    static final String PARAM_STRING_ARRAY = "elementStringArray"; // NOI18N
    static final String PARAM_IMAGE_ARRAY = "elementImageArray"; // NOI18N
    static final String PARAM_SELECT_COMMAND = "selectCommand"; // NOI18N
    static final String PARAM_LIST_TYPE = "listType"; // NOI18N
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

    public static String getListActionMethodAccessCode (DesignComponent list) {
        if (list == null  ||  ! list.getDocument ().getDescriptorRegistry ().isInHierarchy (ListCD.TYPEID, list.getType ()))
            return null;
        return MidpTypes.getString (list.readProperty (ClassCD.PROP_INSTANCE_NAME)) + LIST_ACTION_METHOD_SUFFIX;
    }

    public static Presenter createListActionCodeNamePresenter () {
        return new ListActionCodeNamePresenter ();
    }

    public static Presenter createListActionCodeClassLevelPresenter () {
        return new ListActionCodeClassLevelPresenter ();
    }

    public static Parameter createSelectCommandParameter () {
        return new SelectCommandParameter ();
    }

    public static Parameter createListCommandParameter () {
        return new ListCommandParameter ();
    }

    public static Parameter createListTypeParameter () {
        return new ListTypeParameter ();
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
            PropertyValue propertyValue = component.readProperty(ListCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            DesignComponent listElement = array.get (index).getComponent ();
            PropertyValue string = listElement.readProperty(ListElementEventSourceCD.PROP_STRING);
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), string);
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ListCD.PROP_ELEMENTS);
        }

        public int getCount (DesignComponent component) {
            PropertyValue propertyValue = component.readProperty (ListCD.PROP_ELEMENTS);
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
            PropertyValue propertyValue = component.readProperty(ListCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            DesignComponent listElement = array.get (index).getComponent ();
            PropertyValue string = listElement.readProperty(ListElementEventSourceCD.PROP_IMAGE);
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), string);
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ListCD.PROP_ELEMENTS);
        }

        public int getCount (DesignComponent component) {
            PropertyValue propertyValue = component.readProperty (ListCD.PROP_ELEMENTS);
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
            PropertyValue propertyValue = component.readProperty(ListCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            DesignComponent listElement = array.get (index).getComponent ();
            PropertyValue string = listElement.readProperty(ListElementEventSourceCD.PROP_FONT);
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), string);
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ListCD.PROP_ELEMENTS);
        }

        public int getCount (DesignComponent component) {
            PropertyValue propertyValue = component.readProperty (ListCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            return array.size ();
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            PropertyValue propertyValue = component.readProperty (ListCD.PROP_ELEMENTS);
            List<PropertyValue> array = propertyValue.getArray ();
            DesignComponent listElement = array.get (index).getComponent ();
            PropertyValue string = listElement.readProperty (ListElementEventSourceCD.PROP_FONT);
            return string.getKind () != PropertyValue.Kind.NULL;
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
            PropertyValue propertyValue = component.readProperty(ListCD.PROP_ELEMENTS);
            List<PropertyValue> elementsArray = propertyValue.getArray ();
            CodeWriter writer = section.getWriter ();

            writer.write ("new boolean[] { "); // NOI18N

            for (int i = 0; i < elementsArray.size(); i ++) {
                if (i > 0) {
                    writer.write (", "); // NOI18N
                }
                PropertyValue listElementValue = elementsArray.get (i);
                DesignComponent listElement = listElementValue.getComponent ();
                PropertyValue string = listElement.readProperty(ListElementEventSourceCD.PROP_SELECTED);
                MidpCodeSupport.generateCodeForPropertyValue (writer, string);
            }

            writer.write (" }"); // NOI18N
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ListCD.PROP_ELEMENTS);
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
            PropertyValue propertyValue = component.readProperty(ListCD.PROP_ELEMENTS);
            List<PropertyValue> elementsArray = propertyValue.getArray ();
            CodeWriter writer = section.getWriter ();

            writer.write ("new String[] { "); // NOI18N

            for (int i = 0; i < elementsArray.size(); i ++) {
                if (i > 0) {
                    writer.write (", "); // NOI18N
                }
                PropertyValue listElementValue = elementsArray.get (i);
                DesignComponent listElement = listElementValue.getComponent ();
                PropertyValue string = listElement.readProperty(ListElementEventSourceCD.PROP_STRING);
                MidpCodeSupport.generateCodeForPropertyValue (writer, string);
            }

            writer.write (" }"); // NOI18N
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ListCD.PROP_ELEMENTS);
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
            PropertyValue propertyValue = component.readProperty(ListCD.PROP_ELEMENTS);
            List<PropertyValue> elementsArray = propertyValue.getArray ();
            CodeWriter writer = section.getWriter ();

            writer.write ("new Image[] { "); // NOI18N

            for (int i = 0; i < elementsArray.size(); i ++) {
                if (i > 0) {
                    writer.write (", "); // NOI18N
                }
                PropertyValue listElementValue = elementsArray.get (i);
                DesignComponent listElement = listElementValue.getComponent ();
                PropertyValue image = listElement.readProperty(ListElementEventSourceCD.PROP_IMAGE);
                MidpCodeSupport.generateCodeForPropertyValue (writer, image);
            }

            writer.write (" }"); // NOI18N
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ListCD.PROP_ELEMENTS);
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            throw Debug.illegalState();
        }

    }

    private static class SelectCommandParameter implements Parameter {

        public String getParameterName () {
            return PARAM_SELECT_COMMAND;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), getSelectCommand (component));
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            DesignComponent selectCommand = getSelectCommand (component).getComponent ();
            DescriptorRegistry descriptorRegistry = component.getDocument ().getDescriptorRegistry ();
            return selectCommand == null  ||  ! descriptorRegistry.isInHierarchy (ListSelectCommandCD.TYPEID, selectCommand.getType ());
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            throw Debug.illegalState ();
        }

        private PropertyValue getSelectCommand (DesignComponent component) {
            DesignComponent source = component.readProperty (ListCD.PROP_SELECT_COMMAND).getComponent ();
            return source != null ? source.readProperty (CommandEventSourceCD.PROP_COMMAND) : PropertyValue.createNull ();
        }

    }

    private static final class ListCommandParameter extends DisplayableCode.CommandParameter {

        @Override
        public int getParameterPriority () {
            return super.getParameterPriority () + 1;
        }

        @Override
        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            DescriptorRegistry descriptorRegistry = component.getDocument ().getDescriptorRegistry ();
            List<PropertyValue> array = component.readProperty (DisplayableCD.PROP_COMMANDS).getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            DesignComponent command = commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ();
            if (command != null  &&  descriptorRegistry.isInHierarchy (ListSelectCommandCD.TYPEID, command.getType ()))
                return false;
            return super.isRequiredToBeSet (command, index);
        }

    }

    private static final class ListActionCodeNamePresenter extends CodeNamePresenter {

        public List<String> getReservedNames () {
            return getReservedNamesFor (MidpTypes.getString (getComponent ().readProperty (ClassCD.PROP_INSTANCE_NAME)));
        }

        public List<String> getReservedNamesFor (String suggestedMainName) {
            return Arrays.asList (suggestedMainName + LIST_ACTION_METHOD_SUFFIX);
        }

    }

    private static final class ListActionCodeClassLevelPresenter extends CodeClassLevelPresenter.Adapter {

        @Override
        protected void generateClassBodyCode (StyledDocument document) {
            DesignComponent list = getComponent ();
            List<PropertyValue> array = list.readProperty (ListCD.PROP_ELEMENTS).getArray ();

            MultiGuardedSection section = MultiGuardedSection.create (document, list.getComponentID () + "-action"); // NOI18N
            String listName = CodeReferencePresenter.generateDirectAccessCode (list);
            String methodName = listName + LIST_ACTION_METHOD_SUFFIX;
            section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: " + methodName + " \">\n"); // NOI18N
            section.getWriter ().write ("/**\n * Performs an action assigned to the selected list element in the " + listName + " component.\n */\n"); // NOI18N
            section.getWriter ().write ("public void " + methodName + " () {\n").commit (); // NOI18N
            section.switchToEditable (list.getComponentID () + "-preAction"); // NOI18N
            section.getWriter ().write (" // enter pre-action user code here\n").commit (); // NOI18N
            section.switchToGuarded ();

            String listInstanceName = CodeReferencePresenter.generateAccessCode (list);
            boolean indexBased = MidpTypes.getBoolean (list.readProperty (ListCD.PROP_INDEX_BASED_SWITCH));
            if (! indexBased) {
                section.getWriter ().write ("String __selectedString = " + listInstanceName + ".getString (" + listInstanceName + ".getSelectedIndex ());\n"); // NOI18N
            }

            if (array.size () > 0) {
                if (indexBased) {
                    section.getWriter ().write ("switch (" + listInstanceName + ".getSelectedIndex ()) {\n"); // NOI18N
                } else {
                    section.getWriter ().write ("if (__selectedString != null) {\n"); // NOI18N
                }

                for (int i = 0; i < array.size (); i ++) {
                    PropertyValue value = array.get (i);
                    DesignComponent source = value.getComponent ();

                    if (indexBased) {
                        section.getWriter ().write ("case " + i + ":\n"); // NOI18N
                    } else {
                        if (i > 0)
                            section.getWriter ().write ("} else "); // NOI18N
                        section.getWriter ().write ("if (__selectedString.equals ("); // NOI18N
                        MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), source.readProperty (ListElementEventSourceCD.PROP_STRING));
                        section.getWriter ().write (")) {\n"); // NOI18N
                    }

                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, source);

                    if (indexBased)
                        section.getWriter ().write ("break;\n"); // NOI18N
                }

                if (! indexBased)
                    section.getWriter ().write ("}\n"); // NOI18N
                section.getWriter ().write ("}\n"); // NOI18N
            }

            section.getWriter ().commit ();
            section.switchToEditable (list.getComponentID () + "-postAction"); // NOI18N
            section.getWriter ().write (" // enter post-action user code here\n").commit (); // NOI18N
            section.switchToGuarded ();
            section.getWriter ().write ("}\n"); // NOI18N
            section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N
            section.close ();
        }

    }

    private static class ListTypeParameter extends MidpParameter {

        protected ListTypeParameter () {
            super (PARAM_LIST_TYPE);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (ListCD.PROP_LIST_TYPE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                int value = MidpTypes.getInteger (propertyValue);
                switch (value) {
                    case ChoiceSupport.VALUE_IMPLICIT:
                        section.getWriter ().write ("Choice.IMPLICIT"); // NOI18N
                        break;
                    case ChoiceSupport.VALUE_EXCLUSIVE:
                        section.getWriter ().write ("Choice.EXCLUSIVE"); // NOI18N
                        break;
                    case ChoiceSupport.VALUE_MULTIPLE:
                        section.getWriter ().write ("Choice.MULTIPLE"); // NOI18N
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
            PropertyValue propertyValue = component.readProperty (ListCD.PROP_FIT_POLICY);
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
