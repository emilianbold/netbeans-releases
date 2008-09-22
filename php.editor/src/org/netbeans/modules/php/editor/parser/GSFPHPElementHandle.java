/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public abstract class GSFPHPElementHandle implements ElementHandle {
    
    final private CompilationInfo info;
    
    GSFPHPElementHandle(CompilationInfo info) {
        this.info = info;
    }

    public FileObject getFileObject() {
        return info.getFileObject();
    }

    public String getMimeType() {
        return PHPLanguage.PHP_MIME_TYPE;
    }

    // TODO what is about?
    public String getIn() {
        return null;
    }

    public boolean signatureEquals(ElementHandle handle) {
        // TODO needs to be done
        return false;
    }
    
    public abstract ASTNode getASTNode();

    
    public static class ClassDeclarationHandle extends GSFPHPElementHandle {

        private ClassDeclaration declaration;
        
        public ClassDeclarationHandle (CompilationInfo info, ClassDeclaration declaration) {
            super (info);
            this.declaration = declaration;
        }
        
        public String getName() {
            String name = "";
            if (declaration.getName() != null) {
                name = declaration.getName().getName();
            }
            return name;
        }

        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        public Set<Modifier> getModifiers() {
            //TODO - the Modifier enum doesn't define abstract and final. 
            return Collections.emptySet();
        }

        @Override
        public ASTNode getASTNode() {
            return declaration;
        }
        
    }
        
    public static class InterfaceDeclarationHandle extends GSFPHPElementHandle {

        private InterfaceDeclaration declaration;
        
        public InterfaceDeclarationHandle (CompilationInfo info, InterfaceDeclaration declaration) {
            super (info);
            this.declaration = declaration;
        }

        @Override
        public ASTNode getASTNode() {
            return declaration;
        }

        public String getName() {
            String name = "";
            if (declaration.getName() != null) {
                name = declaration.getName().getName();
            }
            return name;
        }

        public ElementKind getKind() {
            //TODO thre should be interface
            return ElementKind.CLASS;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
    }
    
    public static class FunctionDeclarationHandle extends GSFPHPElementHandle {

        private FunctionDeclaration declaration;
        
        public FunctionDeclarationHandle (CompilationInfo info, FunctionDeclaration declaration) {
            super (info);
            this.declaration = declaration;
        }
        
        @Override
        public ASTNode getASTNode() {
            return declaration;
        }

        public String getName() {
            String name = "";
            if (declaration.getFunctionName() != null) {
                name = declaration.getFunctionName().getName();
            }
            return name;
        }

        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }    
    }
    
    public static class ClassConstantDeclarationHandle extends GSFPHPElementHandle {

        private ClassConstantDeclaration declaration;
        
        public ClassConstantDeclarationHandle (CompilationInfo info, ClassConstantDeclaration declaration) {
            super (info);
            this.declaration = declaration;
        }
        
        @Override
        public ASTNode getASTNode() {
            return declaration;
        }

        public String getName() {
            String name = "";
            List<Identifier> names = declaration.getNames();
            
            for (Identifier identifier : names) {
                name = name + identifier;
            }
            return name;
        }

        public ElementKind getKind() {
            return ElementKind.CONSTANT;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }    
    }
    
    public static class MethodDeclarationHandle extends FunctionDeclarationHandle {
        
        private MethodDeclaration declaration;
        
        public MethodDeclarationHandle (CompilationInfo info, MethodDeclaration declaration) {
            super (info, declaration.getFunction());
            this.declaration = declaration;
        }

        @Override
        public ASTNode getASTNode() {
            return declaration;
        }

        public Set<Modifier> getModifiers() {
            return translateModifiers(declaration.getModifier());
        }
    }
    
    /**
     * Handle for global definitions define('CONSTANT', 10);
     */
    public static class GlobalConstant extends GSFPHPElementHandle {
        
        private FunctionInvocation invocation;
        private String name = null;
        
        public GlobalConstant (CompilationInfo info, FunctionInvocation invocation) {
            super (info);
            this.invocation = invocation;
        }

        @Override
        public ASTNode getASTNode() {
            return invocation;
        }

        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        public String getName() {
            if (name != null) {
                List<Expression> parameters = invocation.getParameters();
                if (parameters.size() == 2 && parameters.get(0) instanceof Scalar && parameters.get(1) instanceof Scalar) {
                    Scalar value = (Scalar)parameters.get(0);
                    if (value.getScalarType() == Scalar.Type.STRING) {
                        name = value.getStringValue();
                    }
                }
            }
            return name;
        }

        public ElementKind getKind() {
            return ElementKind.CONSTANT;
        }
    }
    
    public static class FieldsDeclarationHandle extends GSFPHPElementHandle {

        private FieldsDeclaration declaration;
        
        public FieldsDeclarationHandle (CompilationInfo info, FieldsDeclaration declaration) {
            super (info);
            this.declaration = declaration;
        }
        
        @Override
        public ASTNode getASTNode() {
            return declaration;
        }

        public String getName() {
            String name = "";
            Variable[] variables = declaration.getVariableNames();
            for (Variable variable : variables) {
                String hName = Utils.resolveVariableName(variable);
                if (hName != null) {
                    name = (variable.isDollared() ? name + "$" + hName : name + hName); //NOI18N
                }
            }
            return name;
        }

        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        public Set<Modifier> getModifiers() {
            return translateModifiers(declaration.getModifier());
        }    
    }

    public static class FieldsFromTagProperty extends GSFPHPElementHandle {

        private PHPDocPropertyTag declaration;

        public FieldsFromTagProperty (CompilationInfo info, PHPDocPropertyTag declaration) {
            super (info);
            this.declaration = declaration;
        }

        @Override
        public ASTNode getASTNode() {
            return declaration;
        }

        public String getName() {
            return "$" + declaration.getFieldName(); //NOI18N
        }

        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        public Set<Modifier> getModifiers() {
            return translateModifiers(BodyDeclaration.Modifier.PUBLIC);
        }


    }
    
    private static Set<Modifier> translateModifiers (int modifier) {
            Set<Modifier> modifiers = new HashSet<Modifier> ();
            if (BodyDeclaration.Modifier.isPrivate(modifier)) {
                modifiers.add(Modifier.PRIVATE);
            }
            if (BodyDeclaration.Modifier.isProtected(modifier)) {
                modifiers.add(Modifier.PROTECTED);
            }
            if (BodyDeclaration.Modifier.isPublic(modifier)) {
                modifiers.add(Modifier.PUBLIC);
            }
            if (BodyDeclaration.Modifier.isStatic(modifier)) {
                modifiers.add(Modifier.STATIC);
            }
            return modifiers;
    }

}
