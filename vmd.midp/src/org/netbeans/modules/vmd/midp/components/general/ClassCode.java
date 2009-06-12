/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.midp.components.general;

import org.netbeans.modules.vmd.api.codegen.*;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.codegen.CodeClassInitHeaderFooterPresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import javax.swing.text.StyledDocument;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author David Kaspar
 */
public class ClassCode {
    
    private static final String ARRAY_SUFFIX = "Array"; // NOI18N
    private static final String GETTER_PREFIX = "get"; // NOI18N

    public static class GeneratedCodePresenter extends ModelUpdatePresenter {

        @Override
        public void modelUpdated() {
             getComponent().writeProperty( ClassCD.PROP_CODE_GENERATED, 
                     MidpTypes.createBooleanValue (Boolean.TRUE));
        }

        public boolean isCodeGenerated(){
            return Boolean.TRUE.equals( getComponent().readProperty(
                    ClassCD.PROP_CODE_GENERATED).getPrimitiveValue()) ;
        }
    }
    
    static final class ClassCodeReferencePresenter extends CodeReferencePresenter {
        
        ClassCodeReferencePresenter() {
        }
        
        protected String generateAccessCode() {
            DesignComponent component = getComponent();
            String instanceName = MidpTypes.getString(component.readProperty(ClassCD.PROP_INSTANCE_NAME));
            return generateAccessCode(instanceName);
        }
        
        protected String generateDirectAccessCode() {
            return MidpTypes.getString(getComponent().readProperty(ClassCD.PROP_INSTANCE_NAME));
        }

        protected String generateAccessCode( String newName ){
            DesignComponent component = getComponent();
            boolean lazyInit = MidpTypes.getBoolean(component.readProperty(ClassCD.PROP_LAZY_INIT));
            return lazyInit ? createGetterNameFromInstanceName(newName) + " ()" : newName; // NOI18N
        }
        
        protected String generateTypeCode() {
            return MidpTypes.getSimpleClassName(getComponent().getType());
        }
        
    }
    
    /**
     * Requires CodeReferencePresenter on the same component.
     */
    static final class CodeLazyInitPresenter extends CodeClassLevelPresenter {
        
        CodeLazyInitPresenter() {
        }
        
        protected void generateFieldSectionCode(MultiGuardedSection section) {
            section.getWriter().write("private " + CodeReferencePresenter.generateTypeCode(getComponent()) + " " + CodeReferencePresenter.generateDirectAccessCode(getComponent()) + ";\n"); // NOI18N
        }
        
        protected void generateMethodSectionCode(MultiGuardedSection section) {
        }
        
        public void generateInitializeSectionCode(MultiGuardedSection section) {
            if (ClassSupport.isLazyInitialized(getComponent()))
                return;
            InitCodeGenerator.generateInitializationCode(section, getComponent());
        }
        
        protected void generateClassBodyCode(StyledDocument document) {
            if (! ClassSupport.isLazyInitialized(getComponent()))
                return;

            MultiGuardedSection section = MultiGuardedSection.create(document, getComponent().getComponentID() + "-getter");// NOI18N
            String directAccess = CodeReferencePresenter.generateDirectAccessCode (getComponent ());

            section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Getter: " + directAccess + " \">\n"); // NOI18N
            section.getWriter ().write ("/**\n * Returns an initiliazed instance of " + directAccess + " component.\n * @return the initialized component instance\n */\n"); // NOI18N
            section.getWriter ().write ("public " + CodeReferencePresenter.generateTypeCode(getComponent()) + " " + CodeReferencePresenter.generateAccessCode(getComponent()) + " {\n"); // NOI18N
            section.getWriter ().write ("if (" + directAccess + " == null) {\n").commit(); // NOI18N
            
            section.switchToEditable(getComponent().getComponentID() + "-preInit"); // NOI18N
            section.getWriter().write(" // write pre-init user code here\n").commit(); // NOI18N
            
            section.switchToGuarded();
            Collection<? extends CodeClassInitHeaderFooterPresenter> headersFooters = getComponent().getPresenters(CodeClassInitHeaderFooterPresenter.class);
            for (CodeClassInitHeaderFooterPresenter header : headersFooters)
                header.generateClassInitializationHeader(section);
            InitCodeGenerator.generateInitializationCode(section, getComponent());
            for (CodeClassInitHeaderFooterPresenter footer : headersFooters)
                footer.generateClassInitializationFooter(section);
            section.getWriter().commit();
            
            section.switchToEditable(getComponent().getComponentID() + "-postInit"); // NOI18N
            section.getWriter().write(" // write post-init user code here\n").commit(); // NOI18N
            
            section.switchToGuarded();
            section.getWriter().write("}\n"); // NOI18N
            section.getWriter().write("return " + directAccess + ";\n"); // NOI18N
            section.getWriter().write("}\n"); // NOI18N
            section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N

            section.close();
        }
        
    }
    
    static class CodeClassComponentDependencyPresenter extends RootCode.CodeComponentDependencyPresenter {
        
        protected void collectRequiredComponents(Collection<DesignComponent> requiredComponents) {
            DesignComponent component = getComponent();
            ComponentDescriptor descriptor = component.getComponentDescriptor();
            if (descriptor != null)
                for (PropertyDescriptor property : descriptor.getPropertyDescriptors())
                    Debug.collectAllComponentReferences(component.readProperty(property.getName()), requiredComponents);
        }
    }
    
    static class CodeClassNamePresenter extends CodeNamePresenter {
        
        public List<String> getReservedNames() {
            return getReservedNamesFor(MidpTypes.getString(getComponent().readProperty(ClassCD.PROP_INSTANCE_NAME)));
        }
        
        public List<String> getReservedNamesFor(String suggestedMainName) {
            return Arrays.asList(suggestedMainName, createGetterNameFromInstanceName(suggestedMainName));
        }
        
    }
    
    private static String createGetterNameFromInstanceName(String instanceName) {
        if (instanceName == null || instanceName.length() < 0)
            return null;
        return GETTER_PREFIX + Character.toUpperCase(instanceName.charAt(0)) + instanceName.substring(1);
    }
    
    public static String getSuggestedMainName(TypeID type) {
        String instanceName = type.getString();
        int index = instanceName.lastIndexOf('.'); // NOI18N
        if (index >= 0)
            instanceName = instanceName.substring(index + 1);
        if (type.getDimension() > 0)
            instanceName += ARRAY_SUFFIX;
        if (instanceName.length () <= 0)
            instanceName = "object"; // NOI18N
        char[] chars = instanceName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                if (Character.isUpperCase(chars[i])) {
                    if (i == 0)
                        chars[i] = Character.toLowerCase(chars[i]);
                    else if (i + 1 < chars.length) {
                        if (Character.isUpperCase(chars[i+1]))
                            chars[i] = Character.toLowerCase(chars[i]);
                        else
                            break;
                    } else
                        chars[i] = Character.toLowerCase(chars[i]);
                } else
                    break;
            }
        }
        return new String (chars);
    }
    
}
