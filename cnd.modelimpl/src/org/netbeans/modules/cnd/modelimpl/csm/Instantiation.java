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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 *
 * @author eu155513
 */
public class Instantiation {
    private final Map<CsmTemplateParameter,CsmType> types = new HashMap<CsmTemplateParameter,CsmType>();

    private Instantiation(CsmTemplate orig, CsmType type) {
        if (type.isInstantiation()) {
            List<CsmType> specs = type.getInstantiationParams();
            for (Iterator paramIter = orig.getTemplateParameters().iterator(),typeIter = specs.iterator();
                    paramIter.hasNext() && typeIter.hasNext();) {
                types.put((CsmTemplateParameter)paramIter.next(), (CsmType)typeIter.next());
            }
        }
    }
    
    private CsmType getInstantiatedType(CsmType type) {
        if (CsmKindUtilities.isTemplateParameterType(type)) {
            CsmType res = types.get(((CsmTemplateParameterType)type).getParameter());
            if (res != null) {
                return new Type(((CsmTemplateParameterType)type).getTemplateType(), res);
            }
        }
        return type;
    }
    
    public static CsmObject create(CsmTemplate object, CsmType type) {
        Instantiation instantiation = new Instantiation(object, type);
        return instantiation.createTemplate(object);
    }
    
    private CsmMember createMember(CsmMember member) {
        if (member instanceof CsmField) {
            return new Field((CsmField)member);
        } else if (member instanceof CsmMethod) {
            return new Method((CsmMethod)member);
        }
        assert true : "Unknown class for member instantiation:" + member;
        return member;
    }
    
    private CsmObject createTemplate(CsmTemplate template) {
        if (template instanceof CsmClass) {
            return new Class((CsmClass)template);
        }
        assert true : "Unknown class for template instantiation:" + template;
        return template;
    }
    
    // All wrappers will implement this interface just in case 
    public static interface Instantiated {
        
    }
    
    // Specializations
    public class Class implements Instantiated, CsmClass {
        private final CsmClass clazz;

        public Class(CsmClass clazz) {
            this.clazz = clazz;
        }

        public boolean isValid() {
            return clazz.isValid();
        }

        public CharSequence getText() {
            return clazz.getText();
        }

        public Position getStartPosition() {
            return clazz.getStartPosition();
        }

        public int getStartOffset() {
            return clazz.getStartOffset();
        }

        public Position getEndPosition() {
            return clazz.getEndPosition();
        }

        public int getEndOffset() {
            return clazz.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return clazz.getContainingFile();
        }

        public Collection<CsmScopeElement> getScopeElements() {
            return clazz.getScopeElements();
        }

        public CsmUID<CsmClass> getUID() {
            return clazz.getUID();
        }

        public CsmScope getScope() {
            return clazz.getScope();
        }

        public CharSequence getName() {
            return clazz.getName();
        }

        public CharSequence getQualifiedName() {
            return clazz.getQualifiedName();
        }

        public CharSequence getUniqueName() {
            return clazz.getUniqueName();
        }

        public Kind getKind() {
            return clazz.getKind();
        }

        public Collection<CsmTypedef> getEnclosingTypedefs() {
            return clazz.getEnclosingTypedefs();
        }

        public boolean isTemplate() {
            return clazz.isTemplate();
        }

        public Collection<CsmMember> getMembers() {
            Collection<CsmMember> res = new ArrayList<CsmMember>();
            for (CsmMember member : clazz.getMembers()) {
                res.add(createMember(member));
            }
            return res;
        }

        public int getLeftBracketOffset() {
            return clazz.getLeftBracketOffset();
        }

        public Collection<CsmFriend> getFriends() {
            return clazz.getFriends();
        }

        public Collection<CsmInheritance> getBaseClasses() {
            return clazz.getBaseClasses();
        }
    }
    
    public class Field implements Instantiated, CsmField {
        private final CsmField field;
        private final CsmType type;

        public Field(CsmField field) {
            this.field = field;
            this.type = getInstantiatedType(field.getType());
        }

        public CharSequence getText() {
            return field.getText();
        }

        public Position getStartPosition() {
            return field.getStartPosition();
        }

        public int getStartOffset() {
            return field.getStartOffset();
        }

        public Position getEndPosition() {
            return field.getEndPosition();
        }

        public int getEndOffset() {
            return field.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return field.getContainingFile();
        }

        public CsmUID<CsmField> getUID() {
            return field.getUID();
        }

        public CsmScope getScope() {
            return field.getScope();
        }

        public CharSequence getName() {
            return field.getName();
        }

        public CharSequence getQualifiedName() {
            return field.getQualifiedName();
        }

        public CharSequence getUniqueName() {
            return field.getUniqueName();
        }

        public Kind getKind() {
            return field.getKind();
        }

        public boolean isExtern() {
            return field.isExtern();
        }

        public CsmType getType() {
            return type;
        }

        public CsmExpression getInitialValue() {
            return field.getInitialValue();
        }

        public CharSequence getDisplayText() {
            return field.getDisplayText();
        }

        public CsmVariableDefinition getDefinition() {
            return field.getDefinition();
        }

        public CharSequence getDeclarationText() {
            return field.getDeclarationText();
        }

        public boolean isStatic() {
            return field.isStatic();
        }

        public CsmVisibility getVisibility() {
            return field.getVisibility();
        }

        public CsmClass getContainingClass() {
            return field.getContainingClass();
        }
    }
    
    public class Method implements Instantiated, CsmMethod {
        private final CsmMethod method;
        private final CsmType retType;

        public Method(CsmMethod method) {
            this.method = method;
            this.retType = getInstantiatedType(method.getReturnType());
        }

        public CharSequence getText() {
            return method.getText();
        }

        public Position getStartPosition() {
            return method.getStartPosition();
        }

        public int getStartOffset() {
            return method.getStartOffset();
        }

        public Position getEndPosition() {
            return method.getEndPosition();
        }

        public int getEndOffset() {
            return method.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return method.getContainingFile();
        }

        public Collection<CsmScopeElement> getScopeElements() {
            return method.getScopeElements();
        }

        public CsmUID getUID() {
            return method.getUID();
        }

        public CsmScope getScope() {
            return method.getScope();
        }

        public CharSequence getName() {
            return method.getName();
        }

        public CharSequence getQualifiedName() {
            return method.getQualifiedName();
        }

        public CharSequence getUniqueName() {
            return method.getUniqueName();
        }

        public Kind getKind() {
            return method.getKind();
        }

        public boolean isStatic() {
            return method.isStatic();
        }

        public CsmVisibility getVisibility() {
            return method.getVisibility();
        }

        public CsmClass getContainingClass() {
            return method.getContainingClass();
        }

        public boolean isTemplate() {
            return method.isTemplate();
        }

        public boolean isInline() {
            return method.isInline();
        }

        public CharSequence getSignature() {
            return method.getSignature();
        }

        public CsmType getReturnType() {
            return retType;
        }

        public Collection<CsmParameter> getParameters() {
            Collection<CsmParameter> res = new ArrayList<CsmParameter>();
            Collection<CsmParameter> parameters = method.getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param));
            }
            return res;
        }

        public CsmFunctionDefinition getDefinition() {
            return method.getDefinition();
        }

        public CharSequence getDeclarationText() {
            return method.getDeclarationText();
        }

        public boolean isVirtual() {
            return method.isVirtual();
        }

        public boolean isExplicit() {
            return method.isExplicit();
        }

        public boolean isConst() {
            return method.isConst();
        }

        public boolean isAbstract() {
            return method.isAbstract();
        }
    }
    
    public class Parameter implements Instantiated, CsmParameter {
        private final CsmParameter parameter;
        private final CsmType type;

        public Parameter(CsmParameter parameter) {
            this.parameter = parameter;
            this.type = getInstantiatedType(parameter.getType());
        }

        public CharSequence getText() {
            return parameter.getText();
        }

        public Position getStartPosition() {
            return parameter.getStartPosition();
        }

        public int getStartOffset() {
            return parameter.getStartOffset();
        }

        public Position getEndPosition() {
            return parameter.getEndPosition();
        }

        public int getEndOffset() {
            return parameter.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return parameter.getContainingFile();
        }

        public CsmUID<CsmParameter> getUID() {
            return parameter.getUID();
        }

        public CsmScope getScope() {
            return parameter.getScope();
        }

        public CharSequence getName() {
            return parameter.getName();
        }

        public CharSequence getQualifiedName() {
            return parameter.getQualifiedName();
        }

        public CharSequence getUniqueName() {
            return parameter.getUniqueName();
        }

        public Kind getKind() {
            return parameter.getKind();
        }

        public boolean isExtern() {
            return parameter.isExtern();
        }

        public CsmType getType() {
            return type;
        }

        public CsmExpression getInitialValue() {
            return parameter.getInitialValue();
        }

        public CharSequence getDisplayText() {
            return parameter.getDisplayText();
        }

        public CsmVariableDefinition getDefinition() {
            return parameter.getDefinition();
        }

        public CharSequence getDeclarationText() {
            return parameter.getDeclarationText();
        }

        public boolean isVarArgs() {
            return parameter.isVarArgs();
        }
    }
    
    public static class Type implements Instantiated, CsmType {
        private final CsmType template;
        private final CsmType instance;

        public Type(CsmType template, CsmType instance) {
            this.template = template;
            this.instance = instance;
        }

        public CharSequence getClassifierText() {
            return instance.getClassifierText();
        }

        public CharSequence getText() {
            if (template instanceof TypeImpl) {
                return ((TypeImpl)template).decorateText(instance.getClassifierText(), this, false, null);
            }
            return template.getText();
        }

        public Position getStartPosition() {
            return instance.getStartPosition();
        }

        public int getStartOffset() {
            return instance.getStartOffset();
        }

        public Position getEndPosition() {
            return instance.getEndPosition();
        }

        public int getEndOffset() {
            return instance.getEndOffset();
        }

        public CsmFile getContainingFile() {
            return instance.getContainingFile();
        }

        public boolean isInstantiation() {
            return instance.isInstantiation();
        }

        public boolean isReference() {
            return template.isReference() || instance.isReference();
        }

        public boolean isPointer() {
            return template.isPointer() || instance.isPointer();
        }

        public boolean isConst() {
            return template.isConst() || instance.isConst();
        }

        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return instance.isBuiltInBased(resolveTypeChain);
        }

        public List<CsmType> getInstantiationParams() {
            return instance.getInstantiationParams();
        }

        public int getPointerDepth() {
            return template.getPointerDepth() + instance.getPointerDepth();
        }

        public CsmClassifier getClassifier() {
            return instance.getClassifier();
        }

        public CharSequence getCanonicalText() {
            return template.getCanonicalText();
        }

        public int getArrayDepth() {
            return template.getArrayDepth() + instance.getArrayDepth();
        }
    }
}
