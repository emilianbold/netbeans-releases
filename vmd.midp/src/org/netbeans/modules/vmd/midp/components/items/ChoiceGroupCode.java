/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.vmd.midp.components.items;

import java.util.Collection;
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
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;

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
            Collection<DesignComponent> children = DocumentSupport.gatherSubComponentsOfType(component, ChoiceElementCD.TYPEID);
            if (children == null) {
                return false;
            }
            for (DesignComponent child : children) {
                if (!child.isDefaultValue(ChoiceElementCD.PROP_FONT)) {
                    return true;
                }
            }
            return false;
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

