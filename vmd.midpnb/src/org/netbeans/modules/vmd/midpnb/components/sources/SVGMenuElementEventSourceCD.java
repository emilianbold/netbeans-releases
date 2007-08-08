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
 *
 */

package org.netbeans.modules.vmd.midpnb.components.sources;

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
import org.netbeans.modules.vmd.midp.components.sources.EventSourceCD;
import org.netbeans.modules.vmd.midp.flow.FlowEventSourcePinPresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorString;
import org.netbeans.modules.vmd.midpnb.components.items.ItemSupport;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.flow.FlowSVGMenuElementPinOrderPresenter;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Anton Chechel
 */
public class SVGMenuElementEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#SVGMenuElementEventSource"); // NOI18N

    public static final String PROP_STRING = "string"; // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_element_16.png"; // NOI18N
    public static final String ICON_LARGE_PATH = "org/netbeans/modules/vmd/midpnb/resources/svg_menu_element_32.png"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (EventSourceCD.TYPEID, TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP_2;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
            new PropertyDescriptor(PROP_STRING, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull (), false, true, MidpVersionable.MIDP_2)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addProperty(NbBundle.getMessage(SVGMenuElementEventSourceCD.class, "DISP_SVGMenuElementEventSource_String"), // NOI18N
                    PropertyEditorString.createInstance(NbBundle.getMessage(SVGMenuElementEventSourceCD.class,
                    "DISP_SVGMenuElementEventSource_String_UCLABEL")), PROP_STRING); // NOI18N
    }

    @Override
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, InspectorPositionPresenter.class);
        DocumentSupport.removePresentersOfClass (presenters, ActionsPresenter.class);

        MidpActionsSupport.addCommonActionsPresenters(presenters, false, true, false, true, false);
        MidpActionsSupport.addMoveActionPresenter(presenters, SVGMenuCD.PROP_ELEMENTS);
        presenters.addAll(ActionsSupport.createByParent(DeleteAction.class, RenameAction.class));

        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.create (ItemSupport.createSVGMenuElementInfoResolver ()),
            // properties
            createPropertiesPresenter (),
            // inspector
//            InspectorPositionPresenter.create(new ComponentsCategoryPC(MidpInspectorSupport.TYPEID_ELEMENTS)),
            // delete
            DeleteDependencyPresenter.createDependentOnParentComponentPresenter(),
            new DeletePresenter () {
                protected void delete () {
                    DesignComponent component = getComponent ();
                    DesignComponent menu = component.getParentComponent();
                    ArraySupport.remove (menu, SVGMenuCD.PROP_ELEMENTS, component);
                }
            },
            // flow
            new FlowEventSourcePinPresenter () { // TODO - move this anonymous class to vmd.midpnb.flow package
                protected DesignComponent getComponentForAttachingPin () {
                    return getComponent ().getParentComponent ();
                }

                protected String getDisplayName () {
                    return MidpValueSupport.getHumanReadableString (getComponent ().readProperty (PROP_STRING));
                }

                protected String getOrder () {
                    return FlowSVGMenuElementPinOrderPresenter.CATEGORY_ID;
                }

                @Override
                protected boolean canRename () {
                    return getComponent () != null;
                }

                @Override
                protected String getRenameName () {
                    return (String) getComponent ().readProperty (PROP_STRING).getPrimitiveValue ();
                }

                @Override
                protected void setRenameName (String name) {
                    getComponent ().writeProperty (PROP_STRING, MidpTypes.createStringValue (name));
                }

                @Override
                protected DesignEventFilter getEventFilter () {
                    return super.getEventFilter ().addParentFilter (getComponent (), 1, false);
                }
            },
            // delete
            DeleteDependencyPresenter.createDependentOnParentComponentPresenter (),
            new DeletePresenter() {
                protected void delete () {
                    DesignComponent component = getComponent ();
                    DesignComponent menu = component.getParentComponent ();
                    ArraySupport.remove (menu, SVGMenuCD.PROP_ELEMENTS, component);
                }
            }        
        );
    }

}
