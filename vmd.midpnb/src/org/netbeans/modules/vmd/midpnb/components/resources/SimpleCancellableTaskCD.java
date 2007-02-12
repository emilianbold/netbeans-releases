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

import org.netbeans.modules.vmd.api.codegen.CodeSetterPresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.codegen.Parameter;
import org.netbeans.modules.vmd.api.codegen.CodeWriter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertiesCategories;
import org.netbeans.modules.vmd.midpnb.components.displayables.AbstractInfoScreenCD;
import org.netbeans.modules.vmd.midpnb.components.properteditors.PropertyEditorExecutableUserCode;
import org.netbeans.modules.vmd.midpnb.codegen.MidpCustomCodePresenterSupport;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorPositionPresenter;
import org.netbeans.modules.vmd.api.inspector.common.FolderPositionControllerFactory;
import org.netbeans.modules.vmd.midp.components.resources.ResourceCD;
import org.netbeans.modules.vmd.midp.inspector.controllers.CategoryPC;
import org.netbeans.modules.vmd.midp.inspector.controllers.ResourcePC;

/**
 *
 * @author Karol Harezlak
 */
public class SimpleCancellableTaskCD extends ComponentDescriptor {
   
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.util.SimpleCancellableTask"); // NOI18N
    
    public static final String ICON_PATH = "org/netbeans/modules/vmd/midpnb/resources/resource_16.png"; // NOI18N
    
    private static final String PROP_CODE = "executableMethodBody"; //NOI18N
    
    static {
        MidpTypes.registerIconResource(TYPEID, ICON_PATH);
    }
    
    public SimpleCancellableTaskCD() {
    }

    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(ClassCD.TYPEID, TYPEID, true, true);
        //return new TypeDescriptor(ResourceCD.TYPEID, TYPEID, true, true);
    }

    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }

    public void postInitialize (DesignComponent component) {
        component.writeProperty (PROP_CODE, MidpTypes.createJavaCodeValue ("")); // NOI18N
        MidpProjectSupport.addLibraryToProject (component.getDocument (), AbstractInfoScreenCD.MIDP_NB_LIBRARY);
    }

    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
            new PropertyDescriptor (PROP_CODE, MidpTypes.TYPEID_JAVA_CODE, PropertyValue.createNull(), false, true, MidpVersionable.MIDP_2)
        );
    }

     private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter (DesignEventFilterResolver.THIS_COMPONENT)
                .addPropertiesCategory(PropertiesCategories.CATEGORY_PROPERTIES)
                     .addProperty("Executable Code", new PropertyEditorExecutableUserCode(), PROP_CODE);
    }

    private static Presenter createSetterPresenter () {
        return new CodeSetterPresenter ()
            .addSetters (MidpSetter.createConstructor (TYPEID, MidpVersionable.MIDP_2))
            .addSetters (MidpSetter.createSetter ("setExecutable", MidpVersionable.MIDP_2).addParameters (ExecutableParameter.PARAM_EXECUTABLE));
    }

    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // setter
            createSetterPresenter (),
            MidpCustomCodePresenterSupport.createAddImportPresenter (),
             // inspector
            InspectorFolderPresenter.create(true),
            InspectorPositionPresenter.create(new ResourcePC(), FolderPositionControllerFactory.createHierarchical()),
            InspectorPositionPresenter.create(new CategoryPC())
                
        );
    }

    private static class ExecutableParameter implements Parameter {

        public static final String PARAM_EXECUTABLE = "executable"; // NOI18N

        public String getParameterName () {
            return PARAM_EXECUTABLE;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            CodeWriter writer = section.getWriter ();
            writer.write ("new Executable() {\n"); // NOI18N
            writer.write ("public void execute () throws Exception {\n"); // NOI18N
            String code = MidpTypes.getJavaCode (component.readProperty (PROP_CODE));
            writer.write (code);
            if (! code.endsWith ("\n")) // NOI18N
                writer.write ("\n"); // NOI18N
            writer.write ("}\n"); // NOI18N
            writer.write ("}"); // NOI18N
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return true;
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return false;
        }

    }

}
