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

import org.netbeans.modules.vmd.api.codegen.CodeMultiGuardedLevelPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionController;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.handlers.EventHandlerCD;
import org.netbeans.modules.vmd.midp.components.points.MobileDeviceCD;
import org.netbeans.modules.vmd.midp.components.points.PointCD;
import org.netbeans.modules.vmd.midp.general.AcceptTypePresenter;
import org.netbeans.modules.vmd.midp.inspector.controllers.ChildrenByTypePC;
import org.netbeans.modules.vmd.midp.inspector.controllers.ComponentsCategoryPC;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.eventhandler.PropertyEditorEventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderComponentPresenter;

/**
 * @author David Kaspar
 */

public final class EventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#EventSource"); // NOI18N

    public static final String PROP_EVENT_HANDLER = "eventHandler"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (null, EventSourceCD.TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList(
            new PropertyDescriptor (PROP_EVENT_HANDLER, EventHandlerCD.TYPEID, PropertyValue.createNull (), true, false, Versionable.FOREVER)
        );
    }

    public DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_ACTION_PROPERTIES)
                .addProperty("Action", PropertyEditorEventHandler.createInstance(), PROP_EVENT_HANDLER)
            .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
            .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES);
    }

    private InspectorPositionController[] createPositionControllers() {
        return new InspectorPositionController[]{ new ComponentsCategoryPC(MidpInspectorSupport.TYPEID_COMMANDS),
                                                  new ChildrenByTypePC(PointCD.TYPEID, MobileDeviceCD.TYPEID, ListElementEventSourceCD.TYPEID),
                                                };
    }

    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonActionsPresenters (presenters, false, false, false, true, true);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // properties
            createPropertiesPresenter (),
            //inspector
            new InspectorFolderComponentPresenter(true),
            InspectorPositionPresenter.create(createPositionControllers()),
            // accept
            new AcceptTypePresenter(EventHandlerCD.TYPEID) {
                protected void notifyCreated (DesignComponent component) {
                    MidpDocumentSupport.updateEventHandlerWithNew (getComponent (), component);
                }
            },
            // code
            new CodeMultiGuardedLevelPresenter() {
                protected void generateMultiGuardedSectionCode (MultiGuardedSection section) {
                    section.getWriter ().commit ();
                    section.switchToEditable (getComponent ().getComponentID () + "-preAction"); // NOI18N
                    section.getWriter ().write (" // write pre-action user code here\n").commit (); // NOI18N
                    section.switchToGuarded ();

                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, EventSourceCD.getEventHandler (getComponent ()));

                    section.getWriter ().commit ();
                    section.switchToEditable (getComponent ().getComponentID () + "-postAction"); // NOI18N
                    section.getWriter ().write (" // write post-action user code here\n").commit (); // NOI18N
                    section.switchToGuarded ();
                }
            }
        );
    }

    public static DesignComponent getEventHandler (DesignComponent eventSource) {
        return eventSource.readProperty (PROP_EVENT_HANDLER).getComponent ();
    }

}
