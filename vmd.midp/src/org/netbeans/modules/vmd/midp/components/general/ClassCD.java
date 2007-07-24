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
package org.netbeans.modules.vmd.midp.components.general;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.midp.actions.GoToSourcePresenter;
import org.netbeans.modules.vmd.midp.actions.MidpActionsSupport;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorInstanceName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorBooleanUC;

/**
 * @author David Kaspar
 */

public final class ClassCD extends ComponentDescriptor {

    public static final TypeID TYPEID = new TypeID (TypeID.Kind.COMPONENT, "#Class"); // NOI18N

    public static final String PROP_INSTANCE_NAME = "instanceName"; // NOI18N
    public static final String PROP_LAZY_INIT = "lazyInit";  // NOI18N

    static {
        MidpTypes.registerIcon (TYPEID, null); // TODO - use an empty icon here
    }

    public TypeDescriptor getTypeDescriptor () {
        return new TypeDescriptor (null, TYPEID, false, true);
    }

    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_INSTANCE_NAME, InstanceNameResolver.createFromSuggested (component, ClassCode.getSuggestedMainName (component)));
    }

    public VersionDescriptor getVersionDescriptor () {
        return MidpVersionDescriptor.FOREVER;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors () {
       return Arrays.asList(
            new PropertyDescriptor (PROP_INSTANCE_NAME, MidpTypes.TYPEID_JAVA_LANG_STRING, PropertyValue.createNull (), false, false, Versionable.FOREVER),
            new PropertyDescriptor (PROP_LAZY_INIT, MidpTypes.TYPEID_BOOLEAN, MidpTypes.createBooleanValue (Boolean.TRUE), false, false, Versionable.FOREVER)
       );
    }

    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter()
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_CODE_PROPERTIES)
                .addProperty("Instance Name", PropertyEditorInstanceName.createInstance(TYPEID), PROP_INSTANCE_NAME)
                .addProperty("Is Lazy Initialized", PropertyEditorBooleanUC.createInstance(false), PROP_LAZY_INIT); //NOI18N
                
    }

    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        MidpActionsSupport.addCommonActionsPresenters (presenters, true, true, true, true, true);
        super.gatherPresenters (presenters);
    }

    protected List<? extends Presenter> createPresenters () {
        return Arrays.asList (
            // info
            ClassSupport.createInfoPresenter (),
            // general
            new GoToSourcePresenter () {
                protected boolean matches (GuardedSection section) {
                    boolean lazyInit = MidpTypes.getBoolean (getComponent ().readProperty (PROP_LAZY_INIT));
                    return MultiGuardedSection.matches(section, lazyInit ? getComponent().getComponentID() + "-getter" : getComponent ().getDocument ().getRootComponent ().getComponentID () + "-initialize", 1); // NOI18N
                }
            },
            // properties
            createPropertiesPresenter(),
            // codegen
            new ClassCode.ClassCodeReferencePresenter (),
            new ClassCode.CodeLazyInitPresenter (),
            new ClassCode.CodeClassNamePresenter ()
        );
    }

}
