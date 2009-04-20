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
import org.netbeans.modules.vmd.api.codegen.Parameter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.common.ArrayPropertyOrderingController;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.elements.ChoiceElementCD;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;
import org.netbeans.modules.vmd.midp.screen.display.ChoiceGroupDisplayPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */

public class ChoiceGroupCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.ChoiceGroup"); // NOI18N
    
    public static final String PROP_CHOICE_TYPE = "choiceType"; // NOI18N
    public static final String PROP_FIT_POLICY = "fitPolicy"; // NOI18N
    public static final String PROP_ELEMENTS = "elements"; // NOI18N
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ItemCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_CHOICE_TYPE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_MULTIPLE), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_ELEMENTS, ChoiceElementCD.TYPEID.getArrayType(), PropertyValue.createEmptyArray(ChoiceElementCD.TYPEID), false, false, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_FIT_POLICY, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_TEXT_WRAP_DEFAULT), false, true, MidpVersionable.MIDP_2)
        );
    }
    
    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, ScreenDisplayPresenter.class);
        super.gatherPresenters(presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty(NbBundle.getMessage(ChoiceGroupCD.class, "DISP_ChoiceGroup_Type"), // NOI18N
                        PropertyEditorComboBox.createInstance(ChoiceSupport.getChoiceGroupTypes(), TYPEID,
                            NbBundle.getMessage(ChoiceGroupCD.class, "DISP_ChoiceGroup_Type_RB_LABEL"), // NOI18N
                            NbBundle.getMessage(ChoiceGroupCD.class, "DISP_ChoiceGroup_Type_UCLABEL")), PROP_CHOICE_TYPE) // NOI18N
                    .addProperty(NbBundle.getMessage(ChoiceGroupCD.class, "DISP_ChoiceGroup_Fit_Policy"), // NOI18N
                        NbBundle.getMessage(ChoiceGroupCD.class, "TTIP_ChoiceGroup_Fit_Policy"), // NOI18N
                        PropertyEditorComboBox.createInstance(ChoiceSupport.getFitPolicyValues(), TYPEID,
                            NbBundle.getMessage(ChoiceGroupCD.class, "DISP_ChoiceGroup_Fit_Policy_RB_LABEL"), // NOI18N
                            NbBundle.getMessage(ChoiceGroupCD.class, "DISP_ChoiceGroup_Fit_Policy_UCLABEL")), PROP_FIT_POLICY); // NOI18N
    }
    
    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters (ChoiceGroupCode.createChoiceTypeParameter (), ChoiceGroupCode.createFitPolicyParameter ())
//                .addParameters (ChoiceGroupCode.createStringArrayParameter (), ChoiceGroupCode.createImageArrayParameter ())
                .addParameters (ChoiceGroupCode.createSelectArrayParameter ())
                .addParameters(ChoiceGroupCode.createStringParameter(), ChoiceGroupCode.createImageParameter(), ChoiceGroupCode.createFontParameter ())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, ChoiceGroupCode.PARAM_CHOICE_TYPE))
//                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(ItemCD.PROP_LABEL, ChoiceGroupCode.PARAM_CHOICE_TYPE, ChoiceGroupCode.PARAM_STRING_ARRAY, ChoiceGroupCode.PARAM_IMAGE_ARRAY))
//                .addSetters(MidpSetter.createSetter("insert", MidpVersionable.MIDP).setArrayParameter(ChoiceGroupCode.PARAM_STRING).addParameters(Parameter.PARAM_INDEX, ChoiceGroupCode.PARAM_STRING, ChoiceGroupCode.PARAM_IMAGE))
                .addSetters(MidpSetter.createSetter("append", MidpVersionable.MIDP).setArrayParameter(ChoiceGroupCode.PARAM_STRING).addParameters(ChoiceGroupCode.PARAM_STRING, ChoiceGroupCode.PARAM_IMAGE)) // NOI18N
                .addSetters(MidpSetter.createSetter("setFitPolicy", MidpVersionable.MIDP_2).addParameters(ChoiceGroupCode.PARAM_FIT_POLICY)) // NOI18N
                .addSetters(MidpSetter.createSetter("setFont", MidpVersionable.MIDP_2).setArrayParameter (ChoiceGroupCode.PARAM_FONT).addParameters (Parameter.PARAM_INDEX, ChoiceGroupCode.PARAM_FONT)) // NOI18N
                .addSetters(MidpSetter.createSetter("setSelectedFlags", MidpVersionable.MIDP).addParameters(ChoiceGroupCode.PARAM_SELECTED_ARRAY)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),
                MidpInspectorSupport.createComponentElementsCategory(NbBundle.getMessage (ChoiceGroupCD.class, "DISP_InspectorCategory_ChoiceElements"),getInspectorOrderingControllers(), ChoiceElementCD.TYPEID), //NOI18N
                //actions
                AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, ChoiceElementCD.TYPEID),
                //accept
                new AcceptTypePresenter(ChoiceElementCD.TYPEID) {
                    @Override
                    protected void notifyCreated (DesignComponent choiceElement) {
                        super.notifyCreated (choiceElement);
                        DesignComponent choiceGroup = getComponent ();
                        ArraySupport.append (choiceGroup, PROP_ELEMENTS, choiceElement);
                        if (choiceElement.isDefaultValue(ChoiceElementCD.PROP_STRING)) {
                            PropertyValue value = getComponent ().readProperty(PROP_ELEMENTS);
                            List<PropertyValue> list = value.getArray ();
                            choiceElement.writeProperty (ChoiceElementCD.PROP_STRING, MidpTypes.createStringValue (NbBundle.getMessage(ChoiceGroupCD.class, "DISP_New_Choice_Element", list.size()))); // NOI18N
                        }
                    }
                },
                DatabindingItemAcceptPresenter.create(ItemCD.PROP_LABEL),
                // screen
                new ChoiceGroupDisplayPresenter()
        );
    }
     
    private List<InspectorOrderingController> getInspectorOrderingControllers() {
        return Collections.<InspectorOrderingController>singletonList(new ArrayPropertyOrderingController(PROP_ELEMENTS, 0, ChoiceElementCD.TYPEID));
    }
    
    
}
