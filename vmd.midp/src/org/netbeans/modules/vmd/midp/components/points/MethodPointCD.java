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

import org.netbeans.modules.vmd.api.codegen.CodeNamePresenter;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.handlers.MethodPointEventHandlerCD;
import org.netbeans.modules.vmd.midp.flow.FlowInfoNodePresenter;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorInstanceName;
import org.openide.util.NbBundle;

import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public class MethodPointCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#MethodPoint"); // NOI18N

    public static final String ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/method_point_16.png"; // NOI18N
    public static final String LARGE_ICON_PATH = "org/netbeans/modules/vmd/midp/resources/components/method_point_32.png"; // NOI18N

    public static final String PROP_METHOD_NAME = "methodName"; // NOI18N

    static {
        MidpTypes.registerIconResource (TYPEID, ICON_PATH);
    }

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (PointCD.TYPEID, MethodPointCD.TYPEID, false, true);
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    @Override
    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_METHOD_NAME, InstanceNameResolver.createFromSuggested (component, "method")); // NOI18N
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
        return Arrays.asList (
                new PropertyDescriptor (PROP_METHOD_NAME, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull (), false, false, Versionable.FOREVER)
        );
    }

    public DefaultPropertiesPresenter createPropertiesPresenter () {
        return new DefaultPropertiesPresenter ()
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_PROPERTIES)
                .addPropertiesCategory (MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                    .addProperty (NbBundle.getMessage(MethodPointCD.class, "DISP_MethodPoint_Method_Name"), PropertyEditorInstanceName.createInstance(TYPEID), PROP_METHOD_NAME); // NOI18N
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // general
            MethodPointEventHandlerCD.createMethodPointEventHandlerCreatorPresenter (),
            // flow
            new FlowInfoNodePresenter (),
            // properties
            createPropertiesPresenter (),
            // code
            new CodeReferencePresenter () {
                protected String generateAccessCode () { return generateDirectAccessCode () + " ()"; } // NOI18N
                protected String generateDirectAccessCode () { return MidpTypes.getString (getComponent ().readProperty (PROP_METHOD_NAME)); }
                protected String generateTypeCode () { throw Debug.illegalState (); }
            },
            new CodeNamePresenter() {
                public List<String> getReservedNames () {
                    return Arrays.asList (MidpTypes.getString (getComponent ().readProperty (PROP_METHOD_NAME)));
                }
                public List<String> getReservedNamesFor (String suggestedMainName) {
                    return Arrays.asList (suggestedMainName);
                }
            }
        );
    }

}
