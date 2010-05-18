/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
