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

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.date.PropertyEditorDate;
import org.netbeans.modules.vmd.midp.propertyeditors.timezone.PropertyEditorTimeZone;
import org.netbeans.modules.vmd.midp.screen.display.DateFieldDisplayPresenter;
import org.openide.util.NbBundle;

import java.util.*;
import org.netbeans.modules.vmd.midp.codegen.MidpDatabindingCodeSupport;

/**
 *
 * @author Karol Harezlak
 */

public class DateFieldCD extends ComponentDescriptor {

    private static Map<String, PropertyValue> inputModeValues;

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.DateField"); // NOI18N

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

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_DATE, PropertyValue.createUserCode ("new java.util.Date (System.currentTimeMillis ())")); // NOI18N
        component.writeProperty (PROP_INPUT_MODE, MidpTypes.createIntegerValue (VALUE_DATE_TIME));
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_DATE, MidpTypes.TYPEID_LONG, PropertyValue.createNull (), true, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_TIME_ZONE, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_INPUT_MODE, MidpTypes.TYPEID_INT, PropertyValue.createNull (), false, true, MidpVersionable.MIDP)
        );
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, ScreenDisplayPresenter.class);
        presenters.addAll(MidpDatabindingCodeSupport.createDatabindingPresenters(PROP_DATE
                                                                                 ,"getDate()" //NOI18N
                                                                                 , TYPEID
                                                                                 , MidpDatabindingCodeSupport.FeatureType.DateField_FEATURE_DATETIME));
        super.gatherPresenters (presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(DateFieldCD.class, "DISP_DateField_Date"), "dd.mm.yyyy hh:mm:ss", PropertyEditorDate.createInstanceWithDatabinding(), PROP_DATE) // NOI18N
                .addProperty(NbBundle.getMessage(DateFieldCD.class, "DISP_DateField_Time_Zone"), PropertyEditorTimeZone.createInstance(), PROP_TIME_ZONE) // NOI18N
                .addProperty(NbBundle.getMessage(DateFieldCD.class, "DISP_DateField_Input_Mode"), // NOI18N
                    PropertyEditorComboBox.createInstance(getInputModeValues(), TYPEID,
                        NbBundle.getMessage(DateFieldCD.class, "DISP_DateField_Input_Mode_RB_LABEL"), // NOI18N
                        NbBundle.getMessage(DateFieldCD.class, "DISP_DateField_Input_Mode_UCLABEL")), PROP_INPUT_MODE); // NOI18N
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters (new DateParameter ())
                .addParameters (new TimeZoneParameter ())
                .addParameters (new InputModeParameter ())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, InputModeParameter.PARAM_INPUT_MODE))
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, InputModeParameter.PARAM_INPUT_MODE, TimeZoneParameter.PARAM_TIME_ZONE))
                .addSetters(MidpSetter.createSetter("setDate", MidpVersionable.MIDP).addParameters(PROP_DATE)) // NOI18N
                .addSetters(MidpSetter.createSetter("setInputMode", MidpVersionable.MIDP).addParameters(PROP_INPUT_MODE)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter(),
            // screen
            new DateFieldDisplayPresenter(),
            //accept
            DatabindingItemAcceptPresenter.create(PROP_DATE, ItemCD.PROP_LABEL)
        );
    }

    public static Map<String, PropertyValue> getInputModeValues() {
        if (inputModeValues == null) {
            inputModeValues = new TreeMap<String, PropertyValue>();
            inputModeValues.put("DATE", MidpTypes.createIntegerValue(DateFieldCD.VALUE_DATE)); // NOI18N
            inputModeValues.put("DATE_TIME", MidpTypes.createIntegerValue(DateFieldCD.VALUE_DATE_TIME)); // NOI18N
            inputModeValues.put("TIME", MidpTypes.createIntegerValue(DateFieldCD.VALUE_TIME)); // NOI18N
        }

        return inputModeValues;
    }

    private static class DateParameter extends MidpParameter {

        public static final String PARAM_DATE = "date"; // NOI18N

        protected DateParameter () {
            super (PARAM_DATE);
        }


        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (PROP_DATE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                long date = MidpTypes.getLong (propertyValue);
                section.getWriter ().write ("new java.util.Date (" + date + "l)"); // NOI18N
            } else
                super.generateParameterCode (component, section, index);
        }
    }

    private static class InputModeParameter extends MidpParameter {

        public static final String PARAM_INPUT_MODE = "inputMode"; // NOI18N

        public InputModeParameter () {
            super (PARAM_INPUT_MODE);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (PROP_INPUT_MODE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                int value = MidpTypes.getInteger (propertyValue);
                switch (value) {
                    case VALUE_DATE:
                        section.getWriter ().write ("DateField.DATE"); // NOI18N
                        return;
                    case VALUE_DATE_TIME:
                        section.getWriter ().write ("DateField.DATE_TIME"); // NOI18N
                        return;
                    case VALUE_TIME:
                        section.getWriter ().write ("DateField.TIME"); // NOI18N
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
            super (PARAM_TIME_ZONE);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (PROP_TIME_ZONE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                section.getWriter ().write ("java.util.TimeZone.getTimeZone ("); // NOI18N
                MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), propertyValue);
                section.getWriter ().write (")"); // NOI18N
            } else
                super.generateParameterCode (component, section, index);
        }

    }

}
