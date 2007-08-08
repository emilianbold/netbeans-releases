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
package org.netbeans.modules.vmd.midp.components.sources;

import org.netbeans.modules.vmd.api.codegen.CodeClassLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.points.MobileDeviceCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowMobileDevicePinOrderPresenter;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.api.editor.guards.GuardedSection;
import org.openide.util.NbBundle;

import javax.swing.text.StyledDocument;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * @author David Kaspar
 */

public final class MobileDeviceResumeEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#MobileDeviceResumeEventSource"); // NOI18N

    public static final String PROP_MOBILE_DEVICE = "mobileDevice"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventSourceCD.TYPEID, MobileDeviceResumeEventSourceCD.TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
            new PropertyDescriptor (MobileDeviceResumeEventSourceCD.PROP_MOBILE_DEVICE, MobileDeviceCD.TYPEID, PropertyValue.createNull (), true, false, Versionable.FOREVER)
        );
    }

    public static DesignComponent getMobileDeviceComponent (DesignComponent mobileDeviceResumeEventSourceComponent) {
        return mobileDeviceResumeEventSourceComponent.getParentComponent ();
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonActionsPresenters (presenters, false, true, false, false, false);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.createStatic (NbBundle.getMessage (MobileDeviceResumeEventSourceCD.class, "DISP_MIDletResumed"), NbBundle.getMessage (MobileDeviceResumeEventSourceCD.class, "DISP_Event"), MobileDeviceCD.ICON_PATH), // NOI18N
            // general
            new GoToSourcePresenter() {
                protected boolean matches (GuardedSection section) {
                    return MultiGuardedSection.matches (section, getComponent ().getComponentID () + "-resumeMIDlet", 0); // NOI18N
                }
            },
            // flow
            new FlowEventSourcePinPresenter() {
                protected DesignComponent getComponentForAttachingPin () {
                    return getMobileDeviceComponent (getComponent ());
                }
                protected String getDisplayName () {
                    return NbBundle.getMessage (MobileDeviceResumeEventSourceCD.class, "DISP_FlowPin_MIDletResumed"); // NOI18N
                }
                protected String getOrder () {
                    return FlowMobileDevicePinOrderPresenter.CATEGORY_ID;
                }
            },
            // code
            new CodeClassLevelPresenter.Adapter() {
                @Override
                protected void generateClassBodyCode (StyledDocument document) {
                    MultiGuardedSection section = MultiGuardedSection.create (document, getComponent ().getComponentID () + "-resumeMIDlet"); // NOI18N

                    section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: resumeMIDlet \">\n"); // NOI18N
                    section.getWriter ().write ("/**\n * Performs an action assigned to the Mobile Device - MIDlet Resumed point.\n */\n"); // NOI18N
                    section.getWriter ().write ("public void resumeMIDlet () {\n"); // NOI18N

                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, getComponent ());

                    assert section.isGuarded ();
                    section.getWriter ().write ("}\n"); // NOI18N
                    section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N

                    section.close ();
                }
            },
            CodeNamePresenter.fixed ("resumeMIDlet"), // NOI18N
            // delete
            DeletePresenter.createIndeliblePresenter (),
            DeleteDependencyPresenter.createDependentOnParentComponentPresenter ()
        
        );
    }

}
