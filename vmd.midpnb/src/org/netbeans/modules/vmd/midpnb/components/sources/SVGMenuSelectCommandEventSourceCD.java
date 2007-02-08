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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midpnb.components.sources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;

/**
 *
 * @author Anton Chechel
 */
public class SVGMenuSelectCommandEventSourceCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#SVGMenuSelectCommandEventSource"); // NOI18N

    public static final String PROP_SHOW_SELECT_COMMAND = "showSelectCommand"; // NOI18N

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (CommandEventSourceCD.TYPEID, SVGMenuSelectCommandEventSourceCD.TYPEID, true, false);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
                new PropertyDescriptor (PROP_SHOW_SELECT_COMMAND, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (false), false, false, MidpVersionable.MIDP_2)
        );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
            .addPropertiesCategory (PropertiesCategories.CATEGORY_PROPERTIES)
            .addPropertiesCategory (PropertiesCategories.CATEGORY_CODE_PROPERTIES)
                .addProperty ("Show Select Command", PropertyEditorBooleanUC.createInstance(), PROP_SHOW_SELECT_COMMAND);
    }

    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        DocumentSupport.removePresentersOfClass (presenters, InfoPresenter.class);
        DocumentSupport.removePresentersOfClass (presenters, CommandEventSourceCD.CommandEventSourceFlowPinPresenter.class);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            InfoPresenter.createStatic ("SVGMenu.SELECT", "Command", CommandCD.ICON_PATH),
            // flow
            new CommandEventSourceCD.CommandEventSourceFlowPinPresenter () {
                protected DesignComponent getComponentForAttachingPin () {
                    if (! MidpTypes.getBoolean (getComponent ().readProperty (PROP_SHOW_SELECT_COMMAND)))
                        return null;
                    return super.getComponentForAttachingPin ();
                }
            },
            // properties
            createPropertiesPresenter (),
            // delete
            DeletePresenter.createUserIndeliblePresenter ()
        );
    }

}
