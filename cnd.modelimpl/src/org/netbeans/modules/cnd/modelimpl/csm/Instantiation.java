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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 *
 * @author eu155513
 */
public abstract class Instantiation {
    protected final CsmTemplate template;
    protected final CsmType instantiation;
    
    private Instantiation(CsmTemplate template, CsmType instantiation) {
        assert template instanceof CsmTemplate : "Instantiated class is not a CsmTemplate"; // NOI18N
        this.template = template;
        assert instantiation.isInstantiation() : "Instantiation without parameters"; // NOI18N
        this.instantiation = instantiation;
    }
    
    public static CsmObject create(CsmTemplate template, CsmType type) {
        if (template instanceof CsmClass) {
            return new Class(template, type);
        } else if (template instanceof CsmFunction) {
            return new Function(template, type);
        }
        assert true : "Unknown class for template instantiation:" + template; // NOI18N
        return template;
    }

    protected CsmType getInstantiatedType(CsmType type) {
        if (CsmKindUtilities.isTemplateParameterType(type)) {
            CsmTemplateParameterType paramType = (CsmTemplateParameterType)type;
            int paramIdx = ((CsmTemplate)template).getTemplateParameters().indexOf(paramType.getParameter());
            if (paramIdx != -1) {
                try {
                    return new Type(paramType.getTemplateType(), instantiation.getInstantiationParams().get(paramIdx));
                } catch (IndexOutOfBoundsException e) {
                    // parameter does not exist
                }
            }
        }
        return type;
    }
    
    private static class Class extends Instantiation implements CsmClass, CsmInstantiation {
        public Class(CsmTemplate clazz, CsmType type) {
            super(clazz, type);
        }

        public boolean isValid() {
            return ((CsmClass)template).isValid();
        }

        public CharSequence getText() {
            return ((CsmClass)template).getText();
        }

        public Position getStartPosition() {
            return ((CsmClass)template).getStartPosition();
        }

        public int getStartOffset() {
            return ((CsmClass)template).getStartOffset();
        }

        public Position getEndPosition() {
            return ((CsmClass)template).getEndPosition();
        }

        public int getEndOffset() {
            return ((CsmClass)template).getEndOffset();
        }

        public CsmFile getContainingFile() {
            return ((CsmClass)template).getContainingFile();
        }

        public Collection<CsmScopeElement> getScopeElements() {
            return ((CsmClass)template).getScopeElements();
        }

        public CsmUID<CsmClass> getUID() {
            assert true : "Getting UID of instantiated class is not supported yet";
            return ((CsmClass)template).getUID();
        }

        public CsmScope getScope() {
            return ((CsmClass)template).getScope();
        }

        public CharSequence getName() {
            return ((CsmClass)template).getName();
        }

        public CharSequence getQualifiedName() {
            return ((CsmClass)template).getQualifiedName();
        }

        public CharSequence getUniqueName() {
            return ((CsmClass)template).getUniqueName();
        }

        public Kind getKind() {
            return ((CsmClass)template).getKind();
        }

        public Collection<CsmTypedef> getEnclosingTypedefs() {
            return ((CsmClass)template).getEnclosingTypedefs();
        }

        public boolean isTemplate() {
            return ((CsmClass)template).isTemplate();
        }

        private CsmMember createMember(CsmMember member) {
            if (member instanceof CsmField) {
                return new Field((CsmField)member, this);
            } else if (member instanceof CsmMethod) {
                return new Method((CsmMethod)member, this);
            }
            assert true : "Unknown class for member instantiation:" + member; // NOI18N
            return member;
        }

        public Collection<CsmMember> getMembers() {
            Collection<CsmMember> res = new ArrayList<CsmMember>();
            for (CsmMember member : ((CsmClass)template).getMembers()) {
                res.add(createMember(member));
            }
            return res;
        }

        public int getLeftBracketOffset() {
            return ((CsmClass)template).getLeftBracketOffset();
        }

        public Collection<CsmFriend> getFriends() {
            return ((CsmClass)template).getFriends();
        }

        public Collection<CsmInheritance> getBaseClasses() {
            return ((CsmClass)template).getBaseClasses();
        }

        public CsmOffsetableDeclaration getTemplateDeclaration() {
            return (CsmClass)template;
        }

        public CsmType getInstantiationType() {
            return instantiation;
        }
        
        @Override
        public String toString() {
            return "INSTANTIATION OF CLASS: " + getTemplateDeclaration() + " with type " + getInstantiationType();
        }
    }
    
    private static class Function extends Instantiation implements CsmFunction, CsmInstantiation {
        
        public Function(CsmTemplate template, CsmType instantiation) {
            super(template, instantiation);
        }

        public CharSequence getText() {
            return ((CsmFunction)template).getText();
        }

        public Position getStartPosition() {
            return ((CsmFunction)template).getStartPosition();
        }

        public int getStartOffset() {
            return ((CsmFunction)template).getStartOffset();
        }

        public Position getEndPosition() {
            return ((CsmFunction)template).getEndPosition();
        }

        public int getEndOffset() {
            return ((CsmFunction)template).getEndOffset();
        }

        public CsmFile getContainingFile() {
            return ((CsmFunction)template).getContainingFile();
        }

        public Collection<CsmScopeElement> getScopeElements() {
            return ((CsmFunction)template).getScopeElements();
        }

        public CsmUID getUID() {
            return ((CsmFunction)template).getUID();
        }

        public CsmScope getScope() {
            return ((CsmFunction)template).getScope();
        }

        public CharSequence getName() {
            return ((CsmFunction)template).getName();
        }

        public CharSequence getQualifiedName() {
            return ((CsmFunction)template).getQualifiedName();
        }

        public CharSequence getUniqueName() {
            return ((CsmFunction)template).getUniqueName();
        }

        public Kind getKind() {
            return ((CsmFunction)template).getKind();
        }

        public boolean isTemplate() {
            return ((CsmFunction)template).isTemplate();
        }

        public boolean isInline() {
            return ((CsmFunction)template).isInline();
        }

        public boolean isOperator() {
            return ((CsmFunction)template).isOperator();
        }
        
        public OperatorKind getOperatorKind() {
            return ((CsmFunction)template).getOperatorKind();
        }
        
        public CharSequence getSignature() {
            return ((CsmFunction)template).getSignature();
        }

        public CsmType getReturnType() {
            return ((CsmFunction)template).getReturnType();
        }

        public Collection getParameters() {
            Collection<CsmParameter> res = new ArrayList<CsmParameter>();
            Collection<CsmParameter> parameters = ((CsmFunction)template).getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, this));
            }
            return res;
        }

        public CsmFunctionDefinition getDefinition() {
            return ((CsmFunction)template).getDefinition();
        }

        public CsmFunction getDeclaration() {
            return ((CsmFunction)template).getDeclaration();
        }
        
        public CharSequence getDeclarationText() {
            return ((CsmFunction)template).getDeclarationText();
        }

        public CsmOffsetableDeclaration getTemplateDeclaration() {
            return (CsmFunction)template;
        }

        public CsmType getInstantiationType() {
            return instantiation;
        }
        
        @Override
        public String toString() {
            return "INSTANTIATION OF FUNCTION: " + getTemplateDeclaration() + " with type " + getInstantiationType();
        }
    }

    private static class Field implements CsmField, CsmInstantiation {
        private final CsmField fieldRef;
        private final Instantiation clazzRef;
        private final CsmType type;

        public Field(CsmField field, Instantiation clazz) {
            this.fieldRef = field;
            this.clazzRef = clazz;
            this.type = clazz.getInstantiatedType(field.getType());
        }

        public CharSequence getText() {
            return fieldRef.getText();
        }

        public Position getStartPosition() {
            return fieldRef.getStartPosition();
        }

        public int getStartOffset() {
            return fieldRef.getStartOffset();
        }

        public Position getEndPosition() {
            return fieldRef.getEndPosition();
        }

        public int getEndOffset() {
            return fieldRef.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return fieldRef.getContainingFile();
        }

        public CsmUID<CsmField> getUID() {
            assert true : "Getting UID of instantiated class is not supported yet";
            return fieldRef.getUID();
        }

        public CsmScope getScope() {
            return fieldRef.getScope();
        }

        public CharSequence getName() {
            return fieldRef.getName();
        }

        public CharSequence getQualifiedName() {
            return fieldRef.getQualifiedName();
        }

        public CharSequence getUniqueName() {
            return fieldRef.getUniqueName();
        }

        public Kind getKind() {
            return fieldRef.getKind();
        }

        public boolean isExtern() {
            return fieldRef.isExtern();
        }

        public CsmType getType() {
            return type;
        }

        public CsmExpression getInitialValue() {
            return fieldRef.getInitialValue();
        }

        public CharSequence getDisplayText() {
            return fieldRef.getDisplayText();
        }

        public CsmVariableDefinition getDefinition() {
            return fieldRef.getDefinition();
        }

        public CharSequence getDeclarationText() {
            return fieldRef.getDeclarationText();
        }

        public boolean isStatic() {
            return fieldRef.isStatic();
        }

        public CsmVisibility getVisibility() {
            return fieldRef.getVisibility();
        }

        public CsmClass getContainingClass() {
            return fieldRef.getContainingClass();
        }

        public CsmOffsetableDeclaration getTemplateDeclaration() {
            return fieldRef;
        }

        public CsmType getInstantiationType() {
            return clazzRef.instantiation;
        }
        
        @Override
        public String toString() {
            return "INSTANTIATION OF FIELD: " + getTemplateDeclaration() + " with type " + getInstantiationType();
        }
    }
    
    private static class Method implements CsmMethod, CsmFunctionDefinition, CsmInstantiation {
        private final CsmMethod methodRef;
        private final Class clazzRef;
        private final CsmType retType;

        public Method(CsmMethod method, Class clazz) {
            this.methodRef = method;
            this.clazzRef = clazz;
            this.retType = clazz.getInstantiatedType(method.getReturnType());
        }

        public CharSequence getText() {
            return methodRef.getText();
        }

        public Position getStartPosition() {
            return methodRef.getStartPosition();
        }

        public int getStartOffset() {
            return methodRef.getStartOffset();
        }

        public Position getEndPosition() {
            return methodRef.getEndPosition();
        }

        public int getEndOffset() {
            return methodRef.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return methodRef.getContainingFile();
        }

        public Collection<CsmScopeElement> getScopeElements() {
            return methodRef.getScopeElements();
        }

        public CsmUID getUID() {
            assert true : "Getting UID of instantiated class is not supported yet";
            return methodRef.getUID();
        }

        public CsmScope getScope() {
            return methodRef.getScope();
        }

        public CharSequence getName() {
            return methodRef.getName();
        }

        public CharSequence getQualifiedName() {
            return methodRef.getQualifiedName();
        }

        public CharSequence getUniqueName() {
            return methodRef.getUniqueName();
        }

        public Kind getKind() {
            return methodRef.getKind();
        }

        public boolean isStatic() {
            return methodRef.isStatic();
        }

        public CsmVisibility getVisibility() {
            return methodRef.getVisibility();
        }

        public CsmClass getContainingClass() {
            return methodRef.getContainingClass();
        }

        public boolean isTemplate() {
            return methodRef.isTemplate();
        }

        public boolean isInline() {
            return methodRef.isInline();
        }

        public CharSequence getSignature() {
            return methodRef.getSignature();
        }

        public CsmType getReturnType() {
            return retType;
        }

        public Collection<CsmParameter> getParameters() {
            Collection<CsmParameter> res = new ArrayList<CsmParameter>();
            Collection<CsmParameter> parameters = methodRef.getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, clazzRef));
            }
            return res;
        }

        public CsmFunctionDefinition getDefinition() {
            return methodRef.getDefinition();
        }

        public CharSequence getDeclarationText() {
            return methodRef.getDeclarationText();
        }

        public boolean isVirtual() {
            return methodRef.isVirtual();
        }

        public boolean isExplicit() {
            return methodRef.isExplicit();
        }

        public boolean isConst() {
            return methodRef.isConst();
        }

        public boolean isAbstract() {
            return methodRef.isAbstract();
        }

        public boolean isOperator() {
            return methodRef.isOperator();
        }

        public OperatorKind getOperatorKind() {
            return methodRef.getOperatorKind();
        }
        
        public CsmCompoundStatement getBody() {
            return ((CsmFunctionDefinition)methodRef).getBody();
        }

        public CsmFunction getDeclaration() {
            return ((CsmFunctionDefinition)methodRef).getDeclaration();
        }
        
        public CsmOffsetableDeclaration getTemplateDeclaration() {
            return methodRef;
        }

        public CsmType getInstantiationType() {
            return clazzRef.getInstantiationType();
        }
        
        @Override
        public String toString() {
            return "INSTANTIATION OF METHOD: " + getTemplateDeclaration() + " with type " + getInstantiationType();
        }
    }
    
    private static class Parameter implements CsmParameter, CsmInstantiation {
        private final CsmParameter parameterRef;
        private final Instantiation clazzRef;
        private final CsmType type;

        public Parameter(CsmParameter parameter, Instantiation clazz) {
            this.parameterRef = parameter;
            this.clazzRef = clazz;
            this.type = clazz.getInstantiatedType(parameter.getType());
        }
        
        public CharSequence getText() {
            return parameterRef.getText();
        }

        public Position getStartPosition() {
            return parameterRef.getStartPosition();
        }

        public int getStartOffset() {
            return parameterRef.getStartOffset();
        }

        public Position getEndPosition() {
            return parameterRef.getEndPosition();
        }

        public int getEndOffset() {
            return parameterRef.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return parameterRef.getContainingFile();
        }

        public CsmUID<CsmParameter> getUID() {
            assert true : "Getting UID of instantiated class is not supported yet";
            return parameterRef.getUID();
        }

        public CsmScope getScope() {
            return parameterRef.getScope();
        }

        public CharSequence getName() {
            return parameterRef.getName();
        }

        public CharSequence getQualifiedName() {
            return parameterRef.getQualifiedName();
        }

        public CharSequence getUniqueName() {
            return parameterRef.getUniqueName();
        }

        public Kind getKind() {
            return parameterRef.getKind();
        }

        public boolean isExtern() {
            return parameterRef.isExtern();
        }

        public CsmType getType() {
            return type;
        }

        public CsmExpression getInitialValue() {
            return parameterRef.getInitialValue();
        }

        public CharSequence getDisplayText() {
            return parameterRef.getDisplayText();
        }

        public CsmVariableDefinition getDefinition() {
            return parameterRef.getDefinition();
        }

        public CharSequence getDeclarationText() {
            return parameterRef.getDeclarationText();
        }

        public boolean isVarArgs() {
            return parameterRef.isVarArgs();
        }

        public CsmOffsetableDeclaration getTemplateDeclaration() {
            return parameterRef;
        }

        public CsmType getInstantiationType() {
            return clazzRef.instantiation;
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF FUN PARAM: " + getTemplateDeclaration() + " with type " + getInstantiationType();
        }
    }
    
    private static class Type implements CsmType {
        private final CsmType template;
        private final CsmType instantiation;

        public Type(CsmType template, CsmType instance) {
            this.template = template;
            this.instantiation = instance;
        }

        public CharSequence getClassifierText() {
            return instantiation.getClassifierText();
        }

        public CharSequence getText() {
            if (template instanceof TypeImpl) {
                return ((TypeImpl)template).decorateText(instantiation.getClassifierText(), this, false, null);
            }
            return template.getText();
        }

        public Position getStartPosition() {
            return instantiation.getStartPosition();
        }

        public int getStartOffset() {
            return instantiation.getStartOffset();
        }

        public Position getEndPosition() {
            return instantiation.getEndPosition();
        }

        public int getEndOffset() {
            return instantiation.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return instantiation.getContainingFile();
        }

        public boolean isInstantiation() {
            return instantiation.isInstantiation();
        }

        public boolean isReference() {
            return template.isReference() || instantiation.isReference();
        }

        public boolean isPointer() {
            return template.isPointer() || instantiation.isPointer();
        }

        public boolean isConst() {
            return template.isConst() || instantiation.isConst();
        }

        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return instantiation.isBuiltInBased(resolveTypeChain);
        }

        public List<CsmType> getInstantiationParams() {
            return instantiation.getInstantiationParams();
        }

        public int getPointerDepth() {
            return template.getPointerDepth() + instantiation.getPointerDepth();
        }

        public CsmClassifier getClassifier() {
            return instantiation.getClassifier();
        }

        public CharSequence getCanonicalText() {
            return template.getCanonicalText();
        }

        public int getArrayDepth() {
            return template.getArrayDepth() + instantiation.getArrayDepth();
        }
    }
}
