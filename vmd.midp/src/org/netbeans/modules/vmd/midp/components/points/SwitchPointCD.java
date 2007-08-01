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
package org.netbeans.modules.vmd.midp.components.points;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.modules.vmd.api.codegen.CodeClassLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingPresenter;
import org.netbeans.modules.vmd.api.inspector.common.ArrayPropertyOrderingController;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.sources.SwitchCaseEventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowSwitchPointPinOrderPresenter;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorJavaString;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public class SwitchPointCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#SwitchPoint"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/switch_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/switch_32.png"; // NOI18N

    public static final String PROP_OPERAND = "operand"; // NOI18N
    public static final String PROP_CASES = "cases"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (MethodPointCD.TYPEID, SwitchPointCD.TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_OPERAND, MidpTypes.createJavaCodeValue ("0")); // NOI18N
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
            new PropertyDescriptor (PROP_OPERAND, MidpTypes.TYPEID_JAVA_CODE, PropertyValue.createNull (), false, false, Versionable.FOREVER),
            new PropertyDescriptor (PROP_CASES, SwitchCaseEventSourceCD.TYPEID.getArrayType (), PropertyValue.createEmptyArray (SwitchCaseEventSourceCD.TYPEID), false, false, Versionable.FOREVER)
        );
    }

    @Override
    public PaletteDescriptor getPaletteDescriptor () {
        return new PaletteDescriptor (MidpPaletteProvider.CATEGORY_PROCESS_FLOW, NbBundle.getMessage(SwitchPointCD.class, "DISP_SwitchPoint"), NbBundle.getMessage(SwitchPointCD.class, "TTIP_SwitchPoint"), ICON_PATH, LARGE_ICON_PATH); // NOI18N
    }

    public DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                    .addProperty (NbBundle.getMessage(SwitchPointCD.class, "DISP_SwitchPoint_Switch_Operand"), PropertyEditorJavaString.createInstance(TYPEID), PROP_OPERAND); // NOI18N
    }

    
    @Override
    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        MidpActionsSupport.addNewActionPresenter(presenters, SwitchCaseEventSourceCD.TYPEID);
        MidpActionsSupport.addCommonActionsPresenters (presenters, false, true, true, true, true);
        super.gatherPresenters(presenters);
    }
    
    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // general
            InfoPresenter.create (PointSupport.createInfoResolver (Utilities.loadImage (ICON_PATH), MethodPointCD.PROP_METHOD_NAME, "Switch")),
            new GoToSourcePresenter() {
                protected boolean matches (GuardedSection section) {
                    return MultiGuardedSection.matches(section, getComponent ().getComponentID () + "-switch", 1); // NOI18N
                }
            },
            //inspector
            InspectorOrderingPresenter.create(createOrderingArrayController()),
            // accept
            new AcceptTypePresenter(SwitchCaseEventSourceCD.TYPEID) {
                @Override
                protected void notifyCreated (DesignComponent switchCase) {
                    super.notifyCreated (switchCase);
                    DesignComponent switchComponent = getComponent ();
                    ArraySupport.append (switchComponent, PROP_CASES, switchCase);
                    PropertyValue propertyValue = switchCase.readProperty (SwitchCaseEventSourceCD.PROP_OPTION);
                    if (propertyValue.getKind () == PropertyValue.Kind.NULL) {
                        int size = switchComponent.readProperty (PROP_CASES).getArray ().size ();
                        switchCase.writeProperty (SwitchCaseEventSourceCD.PROP_OPTION, MidpTypes.createJavaCodeValue (Integer.toString (size)));
                    }
                }
            },
            // properties
            createPropertiesPresenter (),
            // flow
            new FlowSwitchPointPinOrderPresenter (),
            // code
            new CodeClassLevelPresenter.Adapter () {
                @Override
                protected void generateClassBodyCode (StyledDocument document) {
                    DesignComponent component = getComponent ();
                    MultiGuardedSection section = MultiGuardedSection.create (document, component.getComponentID () + "-switch"); // NOI18N
                    String methodName = CodeReferencePresenter.generateDirectAccessCode (component);
                    section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: " + methodName + " \">\n"); // NOI18N
                    section.getWriter ().write ("/**\n * Performs an action assigned to the " + methodName + " switch-point.\n */\n"); // NOI18N
                    section.getWriter ().write ("public void " + methodName + " () {\n").commit (); // NOI18N
                    section.switchToEditable (component.getComponentID () + "-preSwitch"); // NOI18N
                    section.getWriter ().write (" // enter pre-switch user code here\n").commit (); // NOI18N
                    section.switchToGuarded ();

                    section.getWriter ().write ("switch ("); // NOI18N
                    MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), component.readProperty (PROP_OPERAND));
                    section.getWriter ().write (") {\n"); // NOI18N
                    List<PropertyValue> cases = component.readProperty (PROP_CASES).getArray ();
                    for (PropertyValue value : cases) {
                        DesignComponent switchCase = value.getComponent ();
                        section.getWriter ().write ("case "); // NOI18N
                        MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), switchCase.readProperty (SwitchCaseEventSourceCD.PROP_OPTION));
                        section.getWriter ().write (":\n"); // NOI18N
                        CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, switchCase);
                        section.getWriter ().write ("break;\n"); // NOI18N
                    }
                    section.getWriter ().write ("}\n").commit (); // NOI18N

                    section.switchToEditable (component.getComponentID () + "-postSwitch"); // NOI18N
                    section.getWriter ().write (" // enter post-switch user code here\n").commit (); // NOI18N
                    section.switchToGuarded ();
                    section.getWriter ().write ("}\n"); // NOI18N
                    section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N
                    section.close ();
                }
            }
        
        
        
        );
    }
    
    private InspectorOrderingController[] createOrderingArrayController() {
        return new InspectorOrderingController[] { new ArrayPropertyOrderingController(PROP_CASES, 0, SwitchCaseEventSourceCD.TYPEID)};
    }
}
