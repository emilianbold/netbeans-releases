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

package org.netbeans.modules.vmd.midpnb.components.displayables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.actions.AddActionPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.inspector.controllers.DisplayablePC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;

/**
 * @author Karol Harezlak
 */

public final class SMSComposerCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.SMSComposer"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/sms_composer_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = null;

    public static final String PROP_PHONE_NUMBER = "phoneNumber"; //NOI18N
    public static final String PROP_MESSAGE = "message"; //NOI18N
    public static final String PROP_PORT_NUMBER = "portNumber"; //NOI18N
    public static final String PROP_BGK_COLOR = "backgroundColor"; //NOI18N
    public static final String PROP_FRG_COLOR = "foregroungColor"; //NOI18N
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CanvasCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor(PROP_MESSAGE, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_PHONE_NUMBER, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_PORT_NUMBER, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(50000), true, true, MidpVersionable.MIDP_2),            
            new PropertyDescriptor(PROP_BGK_COLOR, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(0xCCCCCC), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_FRG_COLOR, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(0x00), true, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
       return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
               .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES) 
                   .addProperty("Phone Number", PropertyEditorString.createInstance(), PROP_PHONE_NUMBER)
                   .addProperty("Message", PropertyEditorString.createInstance(), PROP_MESSAGE)
                   .addProperty("Port Number", PropertyEditorNumber.createIntegerInstance(), PROP_PORT_NUMBER)
                   .addProperty("Background Color", PropertyEditorNumber.createIntegerInstance(), PROP_BGK_COLOR)
                   .addProperty("Foreground Color", PropertyEditorNumber.createIntegerInstance(), PROP_FRG_COLOR);
    }
     
    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpCustomCodePresenterSupport.createDisplayParameter ())
            .addParameters(MidpParameter.create(PROP_MESSAGE, PROP_PHONE_NUMBER, PROP_PORT_NUMBER, PROP_BGK_COLOR, PROP_FRG_COLOR))
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2)
            .addParameters(MidpCustomCodePresenterSupport.PARAM_DISPLAY))
            
            .addSetters(MidpSetter.createSetter("setBGColor", MidpVersionable.MIDP).addParameters(PROP_BGK_COLOR))
            .addSetters(MidpSetter.createSetter("setFGColor", MidpVersionable.MIDP).addParameters(PROP_FRG_COLOR))
            .addSetters(MidpSetter.createSetter("setPhoneNumber", MidpVersionable.MIDP).addParameters(PROP_PHONE_NUMBER))
            .addSetters(MidpSetter.createSetter("setPort", MidpVersionable.MIDP).addParameters(PROP_PORT_NUMBER))
            .addSetters(MidpSetter.createSetter("setMessage", MidpVersionable.MIDP).addParameters(PROP_MESSAGE));
        }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList (
            //properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter(),
            // actions
            AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, CommandCD.TYPEID),
            //inspector
            InspectorPositionPresenter.create(new DisplayablePC()),
            MidpCodePresenterSupport.createAddImportPresenter()
        );
    }

    public void postInitialize(DesignComponent component) {
        super.postInitialize(component);
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY);
    }

    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, AddActionPresenter.class);
        super.gatherPresenters(presenters);
    }

}
