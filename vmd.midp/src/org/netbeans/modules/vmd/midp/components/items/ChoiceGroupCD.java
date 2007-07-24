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
    
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, ScreenDisplayPresenter.class);
        super.gatherPresenters(presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty("Type", PropertyEditorComboBox.createInstance(ChoiceSupport.getChoiceGroupTypes(), TYPEID), PROP_CHOICE_TYPE)
                    .addProperty("Fit Policy", "Sets the application's preferred policy for fitting Choice element contents to the available screen space.",
                                  PropertyEditorComboBox.createInstance(ChoiceSupport.getFitPolicyValues(), TYPEID), PROP_FIT_POLICY);
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
                .addSetters(MidpSetter.createSetter("append", MidpVersionable.MIDP).setArrayParameter(ChoiceGroupCode.PARAM_STRING).addParameters(ChoiceGroupCode.PARAM_STRING, ChoiceGroupCode.PARAM_IMAGE))
                .addSetters(MidpSetter.createSetter("setFitPolicy", MidpVersionable.MIDP_2).addParameters(ChoiceGroupCode.PARAM_FIT_POLICY))
                .addSetters(MidpSetter.createSetter("setFont", MidpVersionable.MIDP_2).setArrayParameter (ChoiceGroupCode.PARAM_FONT).addParameters (Parameter.PARAM_INDEX, ChoiceGroupCode.PARAM_FONT))
                .addSetters(MidpSetter.createSetter("setSelectedFlags", MidpVersionable.MIDP).addParameters(ChoiceGroupCode.PARAM_SELECTED_ARRAY));
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // properties
                createPropertiesPresenter(),
                // code
                createSetterPresenter(),MidpInspectorSupport.createComponentElementsCategory("Elements",getInspectorOrderingControllers(), ChoiceElementCD.TYPEID), //NOI18N
                //actions
                AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, ChoiceElementCD.TYPEID),
                //accept
                new AcceptTypePresenter(ChoiceElementCD.TYPEID) {
                    protected void notifyCreated (DesignComponent choiceElement) {
                        super.notifyCreated (choiceElement);
                        DesignComponent choiceGroup = getComponent ();
                        ArraySupport.append (choiceGroup, PROP_ELEMENTS, choiceElement);
                        if (choiceElement.isDefaultValue(ChoiceElementCD.PROP_STRING)) {
                            PropertyValue value = getComponent ().readProperty(PROP_ELEMENTS);
                            List<PropertyValue> list = value.getArray ();
                            choiceElement.writeProperty (ChoiceElementCD.PROP_STRING, MidpTypes.createStringValue ("Choice Element " + list.size())); //NOI18N
                        }
                    }
                },
                // screen
                new ChoiceGroupDisplayPresenter()
        
        );
    }
     
    private List<InspectorOrderingController> getInspectorOrderingControllers() {
        return Collections.<InspectorOrderingController>singletonList(new ArrayPropertyOrderingController(PROP_ELEMENTS, 0, ChoiceElementCD.TYPEID));
    }
    
    
}
