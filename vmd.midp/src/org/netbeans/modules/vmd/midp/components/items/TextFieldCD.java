/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.vmd.api.codegen.CodeWriter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.propertyeditors.*;
import org.netbeans.modules.vmd.midp.screen.display.TextFieldDisplayPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.midp.codegen.MidpDatabindingCodeSupport;

/**
 *
 * 
 */
public class TextFieldCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.TextField"); // NOI18N
    public static final int VALUE_ANY = 0;
    public static final int VALUE_EMAILADDR = 1;
    public static final int VALUE_NUMERIC = 2;
    public static final int VALUE_PHONENUMBER = 3;
    public static final int VALUE_URL = 4;
    public static final int VALUE_DECIMAL = 5;
    public static final int VALUE_CONSTRAINT_MASK = 0xFFFF;
    public static final int VALUE_PASSWORD = 0x10000;
    public static final int VALUE_UNEDITABLE = 0x20000;
    public static final int VALUE_SENSITIVE = 0x40000;
    public static final int VALUE_NON_PREDICTIVE = 0x80000;
    public static final int VALUE_INITIAL_CAPS_WORD = 0x100000;
    public static final int VALUE_INITIAL_CAPS_SENTENCE = 0x200000;
    
    public static final String PROP_TEXT = "text"; // NOI18N
    public static final String PROP_MAX_SIZE = "maxSize"; // NOI18N
    public static final String PROP_CONSTRAINTS = "constraints"; // NOI18N
    public static final String PROP_INITIAL_INPUT_MODE = "initialInputMode"; // NOI18N
    //Databinding

    

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    @Override
    public void postInitialize(DesignComponent component) {
        component.writeProperty(PROP_MAX_SIZE, MidpTypes.createIntegerValue(32));
        component.writeProperty(PROP_CONSTRAINTS, MidpTypes.createIntegerValue(VALUE_ANY));
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_TEXT, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_MAX_SIZE, MidpTypes.TYPEID_INT, PropertyValue.createNull(), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_CONSTRAINTS, MidpTypes.TYPEID_INT, PropertyValue.createNull(), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_INITIAL_INPUT_MODE, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }

    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, ScreenDisplayPresenter.class);
        presenters.addAll(MidpDatabindingCodeSupport.createDatabindingPresenters(PROP_TEXT,
                                                                                 "getString()",
                                                                                 TYPEID,
                                                                                 MidpDatabindingCodeSupport.FeatureType.TextField_FeatureText));
        
        super.gatherPresenters(presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES).addProperty(NbBundle.getMessage(TextFieldCD.class, "DISP_TextField_Maximum_Size"), // NOI18N
                    PropertyEditorNumber.createPositiveIntegerInstance(false, NbBundle.getMessage(TextFieldCD.class, "LBL_TextField_Maximum_Size")), PROP_MAX_SIZE) // NOI18N
                .addProperty(NbBundle.getMessage(TextFieldCD.class, "DISP_TextField_Text"), // NOI18N
                    PropertyEditorString.createInstanceWithDatabinding(PropertyEditorString.DEPENDENCE_TEXT_FIELD, NbBundle.getMessage(TextFieldCD.class, "LBL_TextField_Text")), PROP_TEXT) // NOI18N
                .addProperty(NbBundle.getMessage(TextFieldCD.class, "DISP_TextField_Initial_Input_Mode"), PropertyEditorInputMode.createInstance(), PROP_INITIAL_INPUT_MODE) // NOI18N
                .addProperty(NbBundle.getMessage(TextFieldCD.class, "DISP_TextField_Input_Constraints"), PropertyEditorConstraints.createInstance(), PROP_CONSTRAINTS); // NOI18N
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter().addParameters(MidpParameter.create(PROP_TEXT, PROP_MAX_SIZE, PROP_INITIAL_INPUT_MODE))
                .addParameters(new TextFieldConstraintsParameter()).addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP)
                .addParameters(ItemCD.PROP_LABEL, PROP_TEXT, PROP_MAX_SIZE, TextFieldConstraintsParameter.PARAM_CONSTRAINTS)).addSetters(MidpSetter.createSetter("setString", MidpVersionable.MIDP).addParameters(PROP_TEXT)) // NOI18N
                .addSetters(MidpSetter.createSetter("setConstraints", MidpVersionable.MIDP).addParameters(TextFieldConstraintsParameter.PARAM_CONSTRAINTS)) // NOI18N
                .addSetters(MidpSetter.createSetter("setInitialInputMode", MidpVersionable.MIDP_2).addParameters(PROP_INITIAL_INPUT_MODE)) // NOI18N
                .addSetters(MidpSetter.createSetter("setMaxSize", MidpVersionable.MIDP).addParameters(PROP_MAX_SIZE)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),
                // screen
                new TextFieldDisplayPresenter(),
                //accept
                DatabindingItemAcceptPresenter.create(PROP_TEXT, ItemCD.PROP_LABEL)
                );
    }

    public static class TextFieldConstraintsParameter extends MidpParameter {

        public static final String PARAM_CONSTRAINTS = "constraints"; // NOI18N

        public TextFieldConstraintsParameter() {
            super(PARAM_CONSTRAINTS);
        }

        @Override
        public void generateParameterCode(DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue value = component.readProperty(PROP_CONSTRAINTS);
            if (value.getKind() == PropertyValue.Kind.VALUE) {
                int constraint = MidpTypes.getInteger(value);
                int core = constraint & TextFieldCD.VALUE_CONSTRAINT_MASK;
                CodeWriter writer = section.getWriter();
                switch (core) {
                    case TextFieldCD.VALUE_ANY:
                        writer.write("TextField.ANY"); // NOI18N
                        break;
                    case TextFieldCD.VALUE_EMAILADDR:
                        writer.write("TextField.EMAILADDR"); // NOI18N
                        break;
                    case TextFieldCD.VALUE_NUMERIC:
                        writer.write("TextField.NUMERIC"); // NOI18N
                        break;
                    case TextFieldCD.VALUE_PHONENUMBER:
                        writer.write("TextField.PHONENUMBER"); // NOI18N
                        break;
                    case TextFieldCD.VALUE_URL:
                        writer.write("TextField.URL"); // NOI18N
                        break;
                    case TextFieldCD.VALUE_DECIMAL:
                        writer.write("TextField.DECIMAL"); // NOI18N
                        break;
                    default:
                        writer.write(Integer.toString(core));
                }
                if ((constraint & TextFieldCD.VALUE_PASSWORD) != 0) {
                    writer.write(" | TextField.PASSWORD"); // NOI18N
                }
                if ((constraint & TextFieldCD.VALUE_UNEDITABLE) != 0) {
                    writer.write(" | TextField.UNEDITABLE"); // NOI18N
                }
                if ((constraint & TextFieldCD.VALUE_SENSITIVE) != 0) {
                    writer.write(" | TextField.SENSITIVE"); // NOI18N
                }
                if ((constraint & TextFieldCD.VALUE_NON_PREDICTIVE) != 0) {
                    writer.write(" | TextField.NON_PREDICTIVE"); // NOI18N
                }
                if ((constraint & TextFieldCD.VALUE_INITIAL_CAPS_WORD) != 0) {
                    writer.write(" | TextField.INITIAL_CAPS_WORD"); // NOI18N
                }
                if ((constraint & TextFieldCD.VALUE_INITIAL_CAPS_SENTENCE) != 0) {
                    writer.write(" | TextField.INITIAL_CAPS_SENTENCE"); // NOI18N
                }
                return;
            }
            super.generateParameterCode(component, section, index);
        }
    }
    
    
}
