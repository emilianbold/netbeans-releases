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

import java.util.ArrayList;
import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.codegen.Parameter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.common.ArrayPropertyOrderingController;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.items.ChoiceSupport;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowListElementPinOrderPresenter;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorComboBox;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorListSelectCommand;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.screen.display.ListDisplayPresenter;
import org.openide.util.NbBundle;

/**
 * @author Karol Harezlak
 */

public final class ListCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "javax.microedition.lcdui.List"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/list_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midp/resources/components/list_32.png"; // NOI18N

    public static final String PROP_ELEMENTS = "elements"; // NOI18N
    public static final String PROP_LIST_TYPE = "listType"; // NOI18N
    public static final String PROP_FIT_POLICY = "fitPolicy"; // NOI18N
    public static final String PROP_SELECT_COMMAND = "selectCommand";  // NOI18N
    public static final String PROP_INDEX_BASED_SWITCH = "indexBasedSwitch";  // NOI18N

    static {
        MidpTypes.registerIconResource (TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ScreenCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_ELEMENTS, ListElementEventSourceCD.TYPEID.getArrayType(), PropertyValue.createEmptyArray(ListElementEventSourceCD.TYPEID), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_LIST_TYPE, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_IMPLICIT), false, true, MidpVersionable.MIDP),
                new PropertyDescriptor(PROP_FIT_POLICY, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (ChoiceSupport.VALUE_TEXT_WRAP_DEFAULT), false, true, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_SELECT_COMMAND, CommandEventSourceCD.TYPEID, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
                new PropertyDescriptor(PROP_INDEX_BASED_SWITCH, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (false), false, false, Versionable.FOREVER)
        );
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, ScreenDisplayPresenter.class);
        super.gatherPresenters(presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                    .addProperty(NbBundle.getMessage(ListCD.class, "DISP_List_Type"), // NOI18N
                        PropertyEditorComboBox.createInstance(ChoiceSupport.getListTypes(), TYPEID,
                            NbBundle.getMessage(ListCD.class, "DISP_List_Type_RB_LABEL"), // NOI18N
                            NbBundle.getMessage(ListCD.class, "DISP_List_Type_UCLABEL")), PROP_LIST_TYPE) // NOI18N
                    .addProperty(NbBundle.getMessage(ListCD.class, "DISP_List_Fit_Policy"), // NOI18N
                        PropertyEditorComboBox.createInstance(ChoiceSupport.getFitPolicyValues(), TYPEID,
                            NbBundle.getMessage(ListCD.class, "DISP_List_Fit_Policy_RB_LABEL"), // NOI18N
                            NbBundle.getMessage(ListCD.class, "DISP_List_Fit_Policy_UCLABEL")), PROP_FIT_POLICY) // NOI18N
                    .addProperty(NbBundle.getMessage(ListCD.class, "DISP_List_Select_Command"), PropertyEditorListSelectCommand.createInstance(), PROP_SELECT_COMMAND) // NOI18N
                .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                    .addProperty(NbBundle.getMessage(ListCD.class, "DISP_List_Index_Based_Switch"), PropertyEditorBooleanUC.createInstance(), PROP_INDEX_BASED_SWITCH); // NOI18N
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                // TODO - clashing StringArray vs. String paremeter and ImageArray vs. Image parameter - both of then thinks that they are modified but only one of them should be
                .addParameters(ListCode.createStringParameter(), ListCode.createImageParameter(), ListCode.createFontParameter())
                .addParameters (ListCode.createSelectArrayParameter ())
//                .addParameters (ListCode.createStringArrayParameter (), ListCode.createImageArrayParameter ())
                .addParameters (ListCode.createSelectCommandParameter ())
                .addParameters (ListCode.createListCommandParameter ())
                .addParameters (ListCode.createListTypeParameter (), ListCode.createFitPolicyParameter ())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(DisplayableCD.PROP_TITLE, ListCode.PARAM_LIST_TYPE))
//                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP).addParameters(DisplayableCD.PROP_TITLE, ListCode.PARAM_LIST_TYPE, ListCode.PARAM_STRING_ARRAY, ListCode.PARAM_IMAGE_ARRAY)) // HINT - not used because of more J2ME Polish friendly
                .addSetters(MidpSetter.createSetter("insert", MidpVersionable.MIDP).setArrayParameter(ListCode.PARAM_STRING).addParameters(Parameter.PARAM_INDEX, ListCode.PARAM_STRING, ListCode.PARAM_IMAGE)) //NOI18N
                .addSetters(MidpSetter.createSetter("append", MidpVersionable.MIDP).setArrayParameter(ListCode.PARAM_STRING).addParameters(ListCode.PARAM_STRING, ListCode.PARAM_IMAGE)) //NOI18N
                .addSetters(MidpSetter.createSetter("setFitPolicy", MidpVersionable.MIDP_2).addParameters(ListCode.PARAM_FIT_POLICY)) //NOI18N
                .addSetters(MidpSetter.createSetter("setSelectCommand", MidpVersionable.MIDP_2).addParameters(ListCode.PARAM_SELECT_COMMAND)) //NOI18N
                .addSetters(MidpSetter.createSetter("setFont", MidpVersionable.MIDP_2).setArrayParameter(ListCode.PARAM_FONT).addParameters(Parameter.PARAM_INDEX, ListCode.PARAM_FONT)) //NOI18N
                .addSetters(MidpSetter.createSetter("setSelectedFlags", MidpVersionable.MIDP).addParameters(ListCode.PARAM_SELECTED_ARRAY)); //NOI18N
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
                // accept
                new AcceptTypePresenter(ListElementEventSourceCD.TYPEID) {
                    @Override
                    protected void notifyCreated (DesignComponent component) {
                        super.notifyCreated (component);
                        ArraySupport.append (getComponent (), ListCD.PROP_ELEMENTS, component);
                        if (component.isDefaultValue(ListElementEventSourceCD.PROP_STRING)) {
                            PropertyValue value = getComponent ().readProperty(ListCD.PROP_ELEMENTS);
                            List<PropertyValue> list = value.getArray ();
                            component.writeProperty (ListElementEventSourceCD.PROP_STRING, MidpTypes.createStringValue (NbBundle.getMessage(ListCD.class, "NAME_New_List_Element", list.size()))); // NOI18N
                        }
                    }
                },
                // properties
                createPropertiesPresenter(),
                // flow
                new FlowListElementPinOrderPresenter (),
                // actions
                AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, ListElementEventSourceCD.TYPEID),
                // inspector
                MidpInspectorSupport.createComponentElementsCategory(NbBundle.getMessage (ListCD.class, "DISP_InspectorCategory_Elements"), getInspectorOrderingControllers(), ListElementEventSourceCD.TYPEID), //NOI18N
                // code
                createSetterPresenter(),
                ListCode.createListActionCodeNamePresenter (),
                ListCode.createListActionCodeClassLevelPresenter (),
                // delete
                DeleteDependencyPresenter.createNullableComponentReferencePresenter(PROP_SELECT_COMMAND),
                // screen
                new ListDisplayPresenter()
        );

    }
    
    private List<InspectorOrderingController> getInspectorOrderingControllers() {
        return Collections.<InspectorOrderingController>singletonList(new ArrayPropertyOrderingController(PROP_ELEMENTS, 0, ListElementEventSourceCD.TYPEID));
    }

}
