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

import org.netbeans.modules.vmd.api.codegen.CodeClassLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.sources.IfFalseEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.IfTrueEventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowIfPointPinOrderPresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorJavaString;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author David Kaspar
 */
public class IfPointCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#IfPoint"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/if_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/if_32.png"; // NOI18N

    public static final String PROP_CONDITION = "condition"; // NOI18N
    public static final String PROP_TRUE = "true"; // NOI18N
    public static final String PROP_FALSE = "false"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (MethodPointCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    public java.util.List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
            new PropertyDescriptor (PROP_CONDITION, MidpTypes.TYPEID_JAVA_CODE, MidpTypes.createJavaCodeValue ("true"), false, false, Versionable.FOREVER), // NOI18N
            new PropertyDescriptor (PROP_TRUE, IfTrueEventSourceCD.TYPEID, PropertyValue.createNull (), false, false, Versionable.FOREVER),
            new PropertyDescriptor (PROP_FALSE, IfFalseEventSourceCD.TYPEID, PropertyValue.createNull (), false, false, Versionable.FOREVER)
        );
    }

    public DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                    .addProperty (NbBundle.getMessage(IfPointCD.class, "DISP_IfPoint_Condition_Code"), PropertyEditorJavaString.createInstance(TYPEID), PROP_CONDITION);
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonActionsPresenters (presenters, false, true, true, true, true);
        super.gatherPresenters (presenters);
    }

    protected java.util.List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // flow
            
            // flow
            new FlowIfPointPinOrderPresenter (),
            // general
            InfoPresenter.create (PointSupport.createInfoResolver (Utilities.loadImage (ICON_PATH), MethodPointCD.PROP_METHOD_NAME, NbBundle.getMessage(IfPointCD.class, "TYPE_IfPoint"))),
            // properties
            createPropertiesPresenter (),
            // code
            new CodeClassLevelPresenter.Adapter () {
                @Override
                protected void generateClassBodyCode (StyledDocument document) {
                    DesignComponent component = getComponent ();
                    MultiGuardedSection section = MultiGuardedSection.create (document, component.getComponentID () + "-if"); // NOI18N
                    String methodName = CodeReferencePresenter.generateDirectAccessCode (component);
                    section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: " + methodName + " \">\n"); // NOI18N
                    section.getWriter ().write ("/**\n * Performs an action assigned to the " + methodName + " if-point.\n */\n"); // NOI18N
                    section.getWriter ().write ("public void " + methodName + " () {\n").commit (); // NOI18N
                    section.switchToEditable (component.getComponentID () + "-preIf"); // NOI18N
                    section.getWriter ().write (" // enter pre-if user code here\n").commit (); // NOI18N
                    section.switchToGuarded ();

                    section.getWriter ().write ("if ("); // NOI18N
                    MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), component.readProperty (PROP_CONDITION));
                    section.getWriter ().write (") {\n"); // NOI18N
                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, component.readProperty (PROP_TRUE).getComponent ());
                    section.getWriter ().write ("} else {\n"); // NOI18N
                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, component.readProperty (PROP_FALSE).getComponent ());
                    section.getWriter ().write ("}\n").commit (); // NOI18N

                    section.switchToEditable (component.getComponentID () + "-postIf"); // NOI18N
                    section.getWriter ().write (" // enter post-if user code here\n").commit (); // NOI18N
                    section.switchToGuarded ();
                    section.getWriter ().write ("}\n"); // NOI18N
                    section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N
                    section.close ();
                }
            },
            // delete
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_TRUE),
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_FALSE)
        );
    }

}
