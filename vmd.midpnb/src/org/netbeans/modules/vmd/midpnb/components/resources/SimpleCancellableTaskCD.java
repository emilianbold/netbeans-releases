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
import org.netbeans.modules.vmd.api.codegen.CodeWriter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.codegen.Parameter;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.codegen.MidpCodePresenterSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpSetter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpVersionDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpVersionable;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.properties.DefaultPropertiesPresenter;
import org.netbeans.modules.vmd.api.properties.DesignEventFilterResolver;
import org.netbeans.modules.vmd.midp.propertyeditors.MidpPropertiesCategories;
import org.netbeans.modules.vmd.midp.propertyeditors.PropertyEditorGoToSource;
import org.netbeans.modules.vmd.midpnb.propertyeditors.MidpNbPropertiesCategories;

/**
 *
 * @author Karol Harezlak
 */
public class SimpleCancellableTaskCD extends ComponentDescriptor {
    
    public static final TypeID TYPEID = new TypeID(TypeID.Kind.COMPONENT, "org.netbeans.microedition.util.SimpleCancellableTask"); // NOI18N
    
    public static final String PROP_CODE = "executableMethodBody"; //NOI18N
    
    public SimpleCancellableTaskCD() {
    }
    
    public TypeDescriptor getTypeDescriptor() {
        return new TypeDescriptor(CancellableTaskCD.TYPEID, TYPEID, true, true);
    }
    
    public VersionDescriptor getVersionDescriptor() {
        return MidpVersionDescriptor.MIDP;
    }
    
    @Override
    public void postInitialize(DesignComponent component) {
        component.writeProperty(PROP_CODE, MidpTypes.createJavaCodeValue("// write task-execution user code here")); // NOI18N
    }
    
    public List<PropertyDescriptor> getDeclaredPropertyDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(PROP_CODE, MidpTypes.TYPEID_JAVA_CODE, PropertyValue.createNull(), false, true, MidpVersionable.MIDP_2, false, false)
                );
    }
    
    private static DefaultPropertiesPresenter createPropertiesPresenter() {
        return new DefaultPropertiesPresenter (DesignEventFilterResolver.THIS_COMPONENT)
            .addPropertiesCategory(MidpPropertiesCategories.CATEGORY_PROPERTIES)
                 .addProperty("Executable Code", PropertyEditorGoToSource.createInstance(), PROP_CODE); // NOI18N
    }

    private static Presenter createSetterPresenter() {
        return new CodeSetterPresenter()
                .addParameters(new ExecutableParameter())
                .addSetters(MidpSetter.createConstructor(TYPEID, MidpVersionable.MIDP_2))
                .addSetters(MidpSetter.createSetter("setExecutable", MidpVersionable.MIDP_2).addParameters(ExecutableParameter.PARAM_EXECUTABLE)); // NOI18N
    }
    
    protected List<? extends Presenter> createPresenters() {
        return Arrays.asList(
            // properties
            createPropertiesPresenter(),
            // setter
            createSetterPresenter(),
            MidpCodePresenterSupport.createAddImportPresenter()
        );
    }
    
    private static class ExecutableParameter implements Parameter {
        
        public static final String PARAM_EXECUTABLE = "executable"; // NOI18N
        
        public String getParameterName() {
            return PARAM_EXECUTABLE;
        }
        
        public int getParameterPriority() {
            return 0;
        }
        
        public void generateParameterCode(DesignComponent component, MultiGuardedSection section, int index) {
            CodeWriter writer = section.getWriter();
            writer.write("new org.netbeans.microedition.util.Executable() {\n"); // NOI18N
            writer.write("public void execute () throws Exception {\n"); // NOI18N
            writer.commit();
            
            section.switchToEditable(component.getComponentID() + "-execute"); // NOI18N
            writer = section.getWriter();
            String code = MidpTypes.getJavaCode(component.readProperty(PROP_CODE));
            if (code != null)
                writer.write(code);
            if (code == null  ||  ! code.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
            writer.commit();
            
            section.switchToGuarded();
            writer = section.getWriter();
            writer.write("}\n"); // NOI18N
            writer.write("}"); // NOI18N
        }
        
        public boolean isRequiredToBeSet(DesignComponent component) {
            return true;
        }
        
        public int getCount(DesignComponent component) {
            return -1;
        }
        
        public boolean isRequiredToBeSet(DesignComponent component, int index) {
            return false;
        }
        
    }
    
}