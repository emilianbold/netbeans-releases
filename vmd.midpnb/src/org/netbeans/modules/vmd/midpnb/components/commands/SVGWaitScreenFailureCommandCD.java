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

package org.netbeans.modules.vmd.midpnb.components.commands;

import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeletePresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Anton Chechel
 */
public class SVGWaitScreenFailureCommandCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#SVGWaitScreenFailureCommand"); // NOI18N

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CommandCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP_2;
    }

    public void postInitialize(DesignComponent component) {
        component.writeProperty(CommandCD.PROP_LABEL, MidpTypes.createStringValue ("Failure")); // NOI18N
        component.writeProperty(CommandCD.PROP_TYPE, MidpTypes.createIntegerValue (CommandCD.VALUE_OK));
        component.writeProperty(CommandCD.PROP_PRIORITY, MidpTypes.createIntegerValue (0));
        component.writeProperty(CommandCD.PROP_ORDINARY, MidpTypes.createBooleanValue (false));
        component.writeProperty (ClassCD.PROP_INSTANCE_NAME, PropertyValue.createNull ());
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return null;
    }

    protected void gatherPresenters(ArrayList<Presenter> presenters) {
        presenters.clear();
        super.gatherPresenters(presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // general
            InfoPresenter.createStatic("FAILURE_COMMAND", "Command", "SVGWaitScreen.FAILURE_COMMAND", CommandCD.ICON_PATH), // NOI18N
            // code
            new CodeReferencePresenter() {
                protected String generateAccessCode() { return generateDirectAccessCode (); }
                protected String generateDirectAccessCode() { return "SVGWaitScreen.FAILURE_COMMAND"; } // NOI18N
                protected String generateTypeCode() { return null; }
            },
            CodeNamePresenter.fixed (),
            // delete
            DeletePresenter.createIndeliblePresenter()
        );
    }

}
