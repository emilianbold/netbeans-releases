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

import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.RenameAction;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.*;
import org.netbeans.modules.vmd.api.model.support.ArraySupport;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.components.*;
import org.netbeans.modules.vmd.midp.components.displayables.ListCD;
import org.netbeans.modules.vmd.midp.components.elements.ElementSupport;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageFileAcceptPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midp.flow.FlowListElementPinOrderPresenter;
import org.netbeans.modules.vmd.midp.inspector.controllers.ComponentsCategoryPC;
import org.netbeans.modules.vmd.midp.inspector.folders.MidpInspectorSupport;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.PropertyEditorResource;
import org.netbeans.modules.vmd.midp.screen.display.ListElementEventSourceDisplayPresenter;
import org.netbeans.modules.vmd.midp.screen.display.ScreenMoveArrayAcceptPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */

public final class ListElementEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#ListElementEventSource"); // NOI18N

    public static final String PROP_STRING = "string"; // NOI18N
    public static final String PROP_IMAGE = ImageCD.PROP_IMAGE;
    public static final String PROP_FONT = "font";  // NOI18N
    public static final String PROP_SELECTED = "selected"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventSourceCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
            new PropertyDescriptor (PROP_STRING, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull (), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor (PROP_IMAGE, ImageCD.TYPEID, PropertyValue.createNull (), true, true, MidpVersionable.MIDP),
            new PropertyDescriptor (PROP_SELECTED, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (false), false, true, MidpVersionable.MIDP),
            new PropertyDescriptor (PROP_FONT, FontCD.TYPEID, PropertyValue.createNull (), true, true, MidpVersionable.MIDP_2)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
            .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
            .addProperty (NbBundle.getMessage(ListElementEventSourceCD.class, "DISP_ListElementEventSource_String"), // NOI18N
                PropertyEditorString.createInstance(NbBundle.getMessage(ListElementEventSourceCD.class, "DISP_ListElementEventSource_String_UCLABEL")), PROP_STRING) // NOI18N
            .addProperty (NbBundle.getMessage(ListElementEventSourceCD.class, "DISP_ListElementEventSource_Image"), PropertyEditorResource.createImagePropertyEditor(), PROP_IMAGE) // NOI18N
            .addProperty(NbBundle.getMessage(ListElementEventSourceCD.class, "DISP_ListElementEventSource_Font"), PropertyEditorResource.createFontPropertyEditor(), PROP_FONT) // NOI18N
            .addProperty (NbBundle.getMessage(ListElementEventSourceCD.class, "DISP_ListElementEventSource_Selected"), PropertyEditorBooleanUC.createInstance(), PROP_SELECTED); // NOI18N
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, InspectorPositionPresenter.class);
        DocumentSupport.removePresentersOfClass (presenters, ActionsPresenter.class);
        MidpActionsSupport.addCommonActionsPresenters(presenters, false, true, false, true, true);
        MidpActionsSupport.addMoveActionPresenter(presenters, ListCD.PROP_ELEMENTS);
        presenters.addAll(ActionsSupport.createByParent(DeleteAction.class, RenameAction.class));
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.create (ElementSupport.createListElementInfoResolver ()),
            // properties
            createPropertiesPresenter (),
            // inspector
            InspectorPositionPresenter.create(new ComponentsCategoryPC(MidpInspectorSupport.TYPEID_ELEMENTS)),new ScreenMoveArrayAcceptPresenter(ListCD.PROP_ELEMENTS, ListElementEventSourceCD.TYPEID),
            // accept
            new ImageFileAcceptPresenter(ImageCD.PROP_IMAGE, ImageCD.TYPEID, "jpg", "png", "gif"), // NOI18N
            MidpAcceptTrensferableKindPresenter.createFontAcceptPresenter(),
            MidpAcceptTrensferableKindPresenter.createImageAcceptPresenter(),
            // flow
            new FlowEventSourcePinPresenter () {
                protected DesignComponent getComponentForAttachingPin () {
                    return getComponent ().getParentComponent ();
                }
                protected String getDisplayName () {
                    return MidpValueSupport.getHumanReadableString (getComponent ().readProperty (PROP_STRING));
                }
                protected String getOrder () {
                    return FlowListElementPinOrderPresenter.CATEGORY_ID;
                }
                protected boolean canRename () {
                    return getComponent () != null;
                }
                protected String getRenameName () {
                    return (String) getComponent ().readProperty (PROP_STRING).getPrimitiveValue ();
                }
                protected void setRenameName (String name) {
                    getComponent ().writeProperty (PROP_STRING, MidpTypes.createStringValue (name));
                }
                protected DesignEventFilter getEventFilter () {
                    return super.getEventFilter ().addParentFilter (getComponent (), 1, false);
                }
            },
            // delete
            DeleteDependencyPresenter.createDependentOnParentComponentPresenter (),
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_IMAGE),
            DeleteDependencyPresenter.createNullableComponentReferencePresenter (PROP_FONT),
            new DeletePresenter() {
                protected void delete () {
                    DesignComponent component = getComponent ();
                    DesignComponent list = component.getParentComponent ();
                    ArraySupport.remove (list, ListCD.PROP_ELEMENTS, component);
                }
            },
            // screen
            new ListElementEventSourceDisplayPresenter()
        );
    }

}
