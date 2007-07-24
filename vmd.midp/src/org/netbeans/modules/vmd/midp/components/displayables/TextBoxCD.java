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

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.items.TextFieldCD;
import org.netbeans.modules.vmd.midp.propertyeditors.*;
import org.netbeans.modules.vmd.midp.screen.display.TextBoxDisplayPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public class TextBoxCD extends ComponentDescriptor{

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "javax.microedition.lcdui.TextBox"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/textbox_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midp/resources/components/textbox_32.png"; // NOI18N

    public static final String PROP_STRING = "string"; // NOI18N
    public static final String PROP_MAX_SIZE = "maxSize"; // NOI18N
    public static final String PROP_CONSTRAINTS = "constraints"; //NOI18N
    public static final String PROP_INITIAL_INPUT_MODE = "initialInputMode"; // NOI18N

    static {
        MidpTypes.registerIconResource (TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor() {
         return new TypeDescriptor (ScreenCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_MAX_SIZE, MidpTypes.createIntegerValue (100));
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
         return Arrays.asList(
            new PropertyDescriptor(PROP_STRING, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_MAX_SIZE, MidpTypes.TYPEID_INT, PropertyValue.createNull (), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_CONSTRAINTS, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue (TextFieldCD.VALUE_ANY), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor(PROP_INITIAL_INPUT_MODE, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
         );
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, ScreenDisplayPresenter.class);
        super.gatherPresenters (presenters);
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(TextBoxCD.class, "DISP_TextBox_Text"), PropertyEditorString.createInstance(PropertyEditorString.DEPENDENCE_TEXT_BOX), PROP_STRING) // NOI18N
                .addProperty(NbBundle.getMessage(TextBoxCD.class, "DISP_TextBox_Maximum_Size"), PropertyEditorNumber.createIntegerInstance(), PROP_MAX_SIZE) // NOI18N
                .addProperty(NbBundle.getMessage(TextBoxCD.class, "DISP_TextBox_Input_Constraints"), PropertyEditorConstraints.createInstance(), PROP_CONSTRAINTS) // NOI18N
                .addProperty(NbBundle.getMessage(TextBoxCD.class, "DISP_TextBox_Initial_Input_Mode"), PropertyEditorInputMode.createInstance(), PROP_INITIAL_INPUT_MODE); // NOI18N
    }

    private static Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
                .addParameters (MidpParameter.create (PROP_STRING, PROP_MAX_SIZE, PROP_INITIAL_INPUT_MODE))
                .addParameters (new TextFieldCD.TextFieldConstraintsParameter ())
                .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP).addParameters (DisplayableCD.PROP_TITLE, PROP_STRING, PROP_MAX_SIZE, TextFieldCD.TextFieldConstraintsParameter.PARAM_CONSTRAINTS))
                .addSetters (MidpSetter.createSetter ("setConstraint", MidpVersionable.MIDP).addParameters (PROP_CONSTRAINTS)) // NOI18N
                .addSetters (MidpSetter.createSetter ("setInitialInputMode", MidpVersionable.MIDP_2).addParameters (PROP_INITIAL_INPUT_MODE)) // NOI18N
                .addSetters (MidpSetter.createSetter ("setString", MidpVersionable.MIDP).addParameters (PROP_STRING)) // NOI18N
                .addSetters (MidpSetter.createSetter ("setMaxSize", MidpVersionable.MIDP).addParameters (PROP_MAX_SIZE)); // NOI18N
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter (),
            // screen
            new TextBoxDisplayPresenter()
        );
    }

}
