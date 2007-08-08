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

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteDependencyPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.points.EntryPointCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;

import java.util.Arrays;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * TODO - CodeClassLevelPresenter
 * @author David Kaspar
 */
public final class EntryStartEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#EntryPointStartEventSource"); // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventSourceCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return null;
    }

    public static DesignComponent getEntryPointComponent (DesignComponent entryPointStartEventSourceComponent) {
        return entryPointStartEventSourceComponent.getParentComponent ();
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.createStatic (NbBundle.getMessage(EntryStartEventSourceCD.class, "NAME_EntryStartEventSource_Entry_Called"), NbBundle.getMessage(EntryStartEventSourceCD.class, "TYPE_Event"), EntryPointCD.ICON_PATH), // NOI18N
            // flow
            new FlowEventSourcePinPresenter() {
                protected DesignComponent getComponentForAttachingPin () {
                    return getEntryPointComponent (getComponent ());
                }

                protected String getDisplayName () {
                    return NbBundle.getMessage(EntryStartEventSourceCD.class, "DISP_EntryStartEventSource_FlowPin"); // NOI18N
                }

                protected String getOrder () {
                    return null; // TODO
                }

                @Override
                protected DesignEventFilter getEventFilter () {
                    return new DesignEventFilter ().addHierarchyFilter (getComponent (), false);
                }
            },
            // delete
            DeletePresenter.createUserIndeliblePresenter (),
            DeleteDependencyPresenter.createDependentOnParentComponentPresenter ()
        
        );
    }

}
