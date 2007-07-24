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
package org.netbeans.modules.vmd.midp.components.handlers;

import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.points.MobileDeviceCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventHandlerEdgePresenter;
import org.netbeans.modules.vmd.midp.general.AbstractEventHandlerCreatorPresenter;
import org.openide.util.NbBundle;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */

public final class ExitMidletEventHandlerCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#ExitMidletEventHandler"); // NOI18N

    public static final String PROP_MOBILE_DEVICE = "mobileDevice"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventHandlerCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
            new PropertyDescriptor (PROP_MOBILE_DEVICE, MobileDeviceCD.TYPEID, PropertyValue.createNull (), false, false, MidpVersionable.MIDP)
        );
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.createStatic (NbBundle.getMessage(ExitMidletEventHandlerCD.class, "NAME_ExitMIDletEventHandler"), NbBundle.getMessage(ExitMidletEventHandlerCD.class, "TYPE_Action"), MobileDeviceCD.ICON_PATH), // NOI18N
            // flow
            new FlowEventHandlerEdgePresenter () {
                protected DesignComponent getTargetComponent () {
                    return getComponent ().readProperty (PROP_MOBILE_DEVICE).getComponent ();
                }
            },
            // code
            new CodeMultiGuardedLevelPresenter() {
                protected void generateMultiGuardedSectionCode (MultiGuardedSection section) {
                    section.getWriter ().write ("exitMIDlet ();\n"); // NOI18N
                }
            },
            // delete
            DeleteDependencyPresenter.createDependentOnPropertyPresenter (PROP_MOBILE_DEVICE)
        );
    }

    public static AbstractEventHandlerCreatorPresenter createExitPointEventHandlerCreatorPresenter () {
        return new AbstractEventHandlerCreatorPresenter() {
            public DesignComponent createReuseEventHandler (DesignComponent eventSource, DesignComponent currentEventHandler, DesignComponent targetComponent) {
                if (currentEventHandler == null || ! getComponent ().getDocument ().getDescriptorRegistry ().isInHierarchy (ExitMidletEventHandlerCD.TYPEID, currentEventHandler.getType ()))
                    currentEventHandler = getComponent ().getDocument ().createComponent (ExitMidletEventHandlerCD.TYPEID);
                currentEventHandler.writeProperty (ExitMidletEventHandlerCD.PROP_MOBILE_DEVICE, PropertyValue.createComponentReference (targetComponent));
                return currentEventHandler;
            }
        };
    }

}
