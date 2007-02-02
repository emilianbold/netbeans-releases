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

package org.netbeans.modules.vmd.midpnb.components.resources;

import java.util.Arrays;
import java.util.List;

import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.resources.ResourceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midpnb.components.displayables.AbstractScreenCD;
import org.netbeans.modules.vmd.midpnb.components.properteditors.PropertyEditorExecutableUserCode;

/**
 *
 * @author Karol Harezlak
 */
public class SimpleCancellableTaskCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.util.SimpleCancellableTask"); // NOI18N

    private static final String PROP_EXECUTABLE_METHOD_BODY = "executableMethodBody"; //NOI18N
    
    public SimpleCancellableTaskCD() {
        
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ResourceCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor (PROP_EXECUTABLE_METHOD_BODY, MidpTypes.TYPEID_JAVA_CODE, PropertyValue.createNull(), true, true, MidpVersionable.MIDP_2)
        );
    }
    
     private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter (DesignEventFilterResolver.THIS_COMPONENT)
                .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                     .addProperty("Executable Method Body", new PropertyEditorExecutableUserCode(), PROP_EXECUTABLE_METHOD_BODY);
    }
    
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            createPropertiesPresenter()
        );
    }
    
    public void postInitialize (DesignComponent component) {
        MidpProjectSupport.addLibraryToProject(component.getDocument(), AbstractScreenCD.MIDP_NB_LIBRARY); 
    }

}
