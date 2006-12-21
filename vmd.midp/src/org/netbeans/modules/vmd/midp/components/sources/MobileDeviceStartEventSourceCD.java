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

import javax.swing.text.StyledDocument;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public final class MobileDeviceStartEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#MobileDeviceStartEventSource"); // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventSourceCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return null;
    }

    public static DesignComponent getMobileDeviceComponent (DesignComponent mobileDeviceStartEventSourceComponent) {
        return mobileDeviceStartEventSourceComponent.getParentComponent ();
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.createStatic ("MIDlet Started", "Event", MobileDeviceCD.ICON_PATH),
            // flow
            new FlowEventSourcePinPresenter () {
                protected DesignComponent getComponentForAttachingPin () {
                    return getMobileDeviceComponent (getComponent ());
                }
                protected String getDisplayName () {
                    return "Started";
                }
                protected String getOrder () {
                    return FlowMobileDevicePinOrderPresenter.CATEGORY_ID;
                }
            },
            // code
            new CodeClassLevelPresenter.Adapter() {
                protected void generateClassBodyCode (StyledDocument document) {
                    MultiGuardedSection section = MultiGuardedSection.create (document, getComponent ().getComponentID () + "-startMIDlet"); // NOI18N

                    section.getWriter ().write ("public void startMIDlet () {\n"); // NOI18N

                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, getComponent ());

                    assert section.isGuarded ();
                    section.getWriter ().write ("}\n").commit (); // NOI18N

                    section.close ();
                }
            },
            CodeNamePresenter.fixed ("startMIDlet"),
            // delete
            DeletePresenter.createIndeliblePresenter (),
            DeleteDependencyPresenter.createDependentOnParentComponentPresenter ()
        );
    }

}
