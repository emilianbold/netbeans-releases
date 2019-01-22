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

package org.netbeans.modules.vmd.midpnb.components.displayables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
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
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.displayables.CanvasCD;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorColorChooser;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorNumber;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midp.screen.display.DisplayableDisplayPresenter;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;
import org.netbeans.modules.vmd.midpnb.propertyeditors.PropertyEditorPhoneNumber;
import org.netbeans.modules.vmd.midpnb.screen.display.SMSComposerDisplayPresenter;
import org.openide.util.NbBundle;

/**
 * 
 */

public final class SMSComposerCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.lcdui.wma.SMSComposer"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/sms_composer_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/sms_composer_32.png"; // NOI18N

    //private static final String ICON_PATH_SD = "org/netbeans/modules/vmd/midpnb/resources/sms_composer_sd.png"; // NOI18N

    public static final String PROP_PHONE_NUMBER = "phoneNumber"; //NOI18N
    public static final String PROP_MESSAGE = "message"; //NOI18N
    public static final String PROP_PORT_NUMBER = "portNumber"; //NOI18N
    public static final String PROP_BGK_COLOR = "backgroundColor"; //NOI18N
    public static final String PROP_FRG_COLOR = "foregroungColor"; //NOI18N
    public static final String PROP_SEND_AUTOMATICALLY = "sendAutomatically"; //NOI18N
    public static final String PROP_PHONE_NUMEBR_LABEL = "phoneNumberLabel"; //NOI18N
    public static final String PROP_MESSAGE_LABEL = "messageLabel"; //NOI18N
    
    public static final String PHONE_NUMBER_LABEL = NbBundle.getMessage(SMSComposerCD.class, "DISP_SMSComposer_PhoneNumberLabel"); //NOI18N 
    public static final String MESSAGE_LABEL = NbBundle.getMessage(SMSComposerCD.class, "DISP_SMSComposer_MessageLabel"); //NOI18N 
    
    private static final String CATEGORIES_SMS = "SMS Properties"; //NOI18N
    
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
            new PropertyDescriptor(PROP_BGK_COLOR, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(-3355444), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_FRG_COLOR, MidpTypes.TYPEID_INT, MidpTypes.createIntegerValue(-16777216), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_SEND_AUTOMATICALLY, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue(true), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_PHONE_NUMEBR_LABEL, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2),
            new PropertyDescriptor(PROP_MESSAGE_LABEL, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
       return new DefaultPropertiesPresenter(DesignEventFilterResolver.THIS_COMPONENT)
               .addPropertiesCategory(CATEGORIES_SMS) 
                   .addProperty(NbBundle.getMessage(SMSComposerCD.class, "DISP_SMSComposer_PhoneNumber"), //NOI18N
                        PropertyEditorPhoneNumber.createInstance(NbBundle.getMessage(SMSComposerCD.class,
                            "LBL_SMSComposer_PhoneNumber"), "testing"), PROP_PHONE_NUMBER) //NOI18N
                   .addProperty(NbBundle.getMessage(SMSComposerCD.class, "DISP_SMSComposer_Message"), //NOI18N
                        PropertyEditorString.createInstance(NbBundle.getMessage(SMSComposerCD.class,
                            "LBL_SMSComposer_Message")), PROP_MESSAGE) //NOI18N
                   .addProperty(NbBundle.getMessage(SMSComposerCD.class, "DISP_SMSComposer_Port_Number"), //NOI18N
                        PropertyEditorNumber.createIntegerInstance(true, NbBundle.getMessage(SMSComposerCD.class, "LBL_SMSComposer_Port_Number")), PROP_PORT_NUMBER) //NOI18N
                   .addProperty(NbBundle.getMessage(SMSComposerCD.class, "DISP_SMSComposer_AutomaticallySend"), //NOI18N
                        PropertyEditorBooleanUC.createInstance(NbBundle.getMessage(SMSComposerCD.class, "LBL_SMSComposer_AutomaticallySend")), PROP_SEND_AUTOMATICALLY) //NOI18N
               .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                   .addProperty(NbBundle.getMessage(SMSComposerCD.class, "DISP_SMSComposer_BackgroundColor"), //NOI18N
                        new PropertyEditorColorChooser(true), PROP_BGK_COLOR)
                   .addProperty(NbBundle.getMessage(SMSComposerCD.class, "DISP_SMSComposer_ForegroundColor"), //NOI18N
                        new PropertyEditorColorChooser(true), PROP_FRG_COLOR)
                   .addProperty(NbBundle.getMessage(SMSComposerCD.class, "DISP_SMSComposer_PhoneNumberLabel"), //NOI18N
                        PropertyEditorString.createInstanceWithDefaultValue(PHONE_NUMBER_LABEL,
                        NbBundle.getMessage(SMSComposerCD.class, "LBL_SMSComposer_PhoneNumberLabel")), PROP_PHONE_NUMEBR_LABEL) //NOI18N
                   .addProperty(NbBundle.getMessage(SMSComposerCD.class, "DISP_SMSComposer_MessageLabel"), //NOI18N
                        PropertyEditorString.createInstanceWithDefaultValue(MESSAGE_LABEL, 
                        NbBundle.getMessage(SMSComposerCD.class, "LBL_SMSComposer_MessageLabel")), PROP_MESSAGE_LABEL); //NOI18N
    }
     
    private Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addParameters (MidpCustomCodePresenterSupport.createDisplayParameter ())
            .addParameters(MidpParameter.create(PROP_MESSAGE, PROP_PHONE_NUMBER, PROP_PORT_NUMBER, PROP_BGK_COLOR, PROP_FRG_COLOR, PROP_SEND_AUTOMATICALLY, PROP_MESSAGE_LABEL, PROP_PHONE_NUMEBR_LABEL))
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2)
            .addParameters(MidpCustomCodePresenterSupport.PARAM_DISPLAY))
            .addSetters(MidpSetter.createSetter("setBGColor", MidpVersionable.MIDP).addParameters(PROP_BGK_COLOR)) //NOI18N
            .addSetters(MidpSetter.createSetter("setFGColor", MidpVersionable.MIDP).addParameters(PROP_FRG_COLOR)) //NOI18N
            .addSetters(MidpSetter.createSetter("setPhoneNumber", MidpVersionable.MIDP).addParameters(PROP_PHONE_NUMBER)) //NOI18N
            .addSetters(MidpSetter.createSetter("setPort", MidpVersionable.MIDP).addParameters(PROP_PORT_NUMBER)) //NOI18N
            .addSetters(MidpSetter.createSetter("setMessage", MidpVersionable.MIDP).addParameters(PROP_MESSAGE)) //NOI18N
            .addSetters(MidpSetter.createSetter("setSendAutomatically", MidpVersionable.MIDP).addParameters(PROP_SEND_AUTOMATICALLY)) //NOI18N
            .addSetters(MidpSetter.createSetter("setPhoneNumberLabel", MidpVersionable.MIDP).addParameters(PROP_PHONE_NUMEBR_LABEL)) //NOI18N
            .addSetters(MidpSetter.createSetter("setMessageLabel", MidpVersionable.MIDP).addParameters(PROP_MESSAGE_LABEL)); //NOI18N
        }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList (
            // properties
            createPropertiesPresenter(),
            // code
            createSetterPresenter(),
            MidpCodePresenterSupport.createAddImportPresenter(),
            // actions
            AddActionPresenter.create(AddActionPresenter.ADD_ACTION, 10, CommandCD.TYPEID),
            // screen
            new SMSComposerDisplayPresenter()
        );
    }

    @Override
    public void postInitialize(DesignComponent component) {
        super.postInitialize(component);
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY_BASIC);
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY_WMA);
    }

    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass(presenters, AddActionPresenter.class);
        DocumentSupport.removePresentersOfClass(presenters, DisplayableDisplayPresenter.class);
        MidpActionsSupport.addUnusedCommandsAddActionForDisplayable(presenters);
        super.gatherPresenters(presenters);
    }

}
