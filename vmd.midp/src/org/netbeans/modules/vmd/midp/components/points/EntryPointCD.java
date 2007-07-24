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
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.sources.EntryStartEventSourceCD;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public class EntryPointCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#EntryPoint"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/entry_point_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/entry_point_32.png"; // NOI18N

    public static final String PROP_START = "start"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (MethodPointCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
                new PropertyDescriptor (PROP_START, EntryStartEventSourceCD.TYPEID, PropertyValue.createNull (), false, false, Versionable.FOREVER)
        );
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonActionsPresenters (presenters, false, true, true, true, true);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // general
            InfoPresenter.create (PointSupport.createInfoResolver (Utilities.loadImage (ICON_PATH), MethodPointCD.PROP_METHOD_NAME, NbBundle.getMessage (EntryPointCD.class, "TYPE_EntryPoint"))), // NOI18N
            // code
            new CodeClassLevelPresenter.Adapter () {
                @Override
                protected void generateClassBodyCode (StyledDocument document) {
                    DesignComponent component = getComponent ();
                    MultiGuardedSection section = MultiGuardedSection.create (document, component.getComponentID () + "-entry"); // NOI18N
                    String methodName = CodeReferencePresenter.generateDirectAccessCode (component);
                    section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: " + methodName + " \">\n"); // NOI18N
                    section.getWriter ().write ("/**\n * Performs an action assigned to the " + methodName + " entry-point.\n */\n"); // NOI18N
                    section.getWriter ().write ("public void " + methodName + " () {\n"); // NOI18N
                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, component.readProperty (PROP_START).getComponent ());
                    section.getWriter ().write ("}\n"); // NOI18N
                    section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N
                    section.close ();
                }
            },
            // delete
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_START)
        );
    }

}
