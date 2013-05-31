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

import java.util.Set;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableIdentifiableBase;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver.SafeTemplateBasedProvider;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.MemberResolverImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.SelectImpl;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Instantiations.
 *
 * @author eu155513, Nikolay Krasilnikov (nnnnnk@netbeans.org)
 */
public /*abstract*/ class Instantiation<T extends CsmOffsetableDeclaration> extends OffsetableIdentifiableBase<CsmInstantiation> implements CsmOffsetableDeclaration, CsmInstantiation, CsmIdentifiable {
    private static final int MAX_INHERITANCE_DEPTH = 20;

    protected final T declaration;
    protected final Map<CsmTemplateParameter, CsmSpecializationParameter> mapping;

    private Instantiation(T declaration, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        super(declaration.getContainingFile(), declaration.getStartOffset(), declaration.getEndOffset());
        this.declaration = declaration;
        this.mapping = mapping;
    }

//    @Override
//    public int getStartOffset() {
//        return declaration.getStartOffset();
//    }
//    
//    @Override
//    public int getEndOffset() {
//        return declaration.getEndOffset();
//    }

    // FIX for 146522, we compare toString value until better solution is found
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CsmObject)) {
            return false;
        }
        CsmObject csmobj = (CsmObject) obj;
        if (!CsmKindUtilities.isInstantiation(csmobj)) {
            return false;
        }
        CsmInstantiation inst = (CsmInstantiation) csmobj;
        Map<CsmTemplateParameter, CsmSpecializationParameter> mapping1 = this.getMapping();
        Map<CsmTemplateParameter, CsmSpecializationParameter> mapping2 = inst.getMapping();
        if(mapping1.size() != mapping2.size()) {
            return false;
        }
        for (CsmTemplateParameter csmTemplateParameter : mapping1.keySet()) {
            if(!this.getMapping().get(csmTemplateParameter).equals(mapping2.get(csmTemplateParameter))) {
                return false;
            }
        }
        return this.getTemplateDeclaration().equals(inst.getTemplateDeclaration());

//        if (CsmKindUtilities.isInstantiation(csmobj)) {
//            return getFullName().equals(((Instantiation)csmobj).getFullName());
//        } else if (CsmKindUtilities.isTemplate(csmobj) ||
//                   CsmKindUtilities.isTemplateInstantiation(csmobj)) {
//            return this.getUniqueName().equals(((CsmDeclaration)csmobj).getUniqueName());
//        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + (this.declaration != null ? this.declaration.hashCode() : 0);
        hash = 31 * hash + (this.mapping != null ? this.mapping.hashCode() : 0);
        return hash;
    }

    private CsmClassForwardDeclaration findCsmClassForwardDeclaration(CsmScope scope, CsmClass cls) {
        if (scope != null) {
            if (CsmKindUtilities.isFile(scope)) {
                CsmFile file = (CsmFile) scope;
                CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION);
                Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(file, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    if (((CsmClassForwardDeclaration) decl).getCsmClass().equals(cls)) {
                        return (CsmClassForwardDeclaration) decl;
                    }
                }
                filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION);
                declarations = CsmSelect.getDeclarations(file, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    CsmClassForwardDeclaration fdecl = findCsmClassForwardDeclaration((CsmNamespaceDefinition) decl, cls);
                    if (fdecl != null) {
                        return fdecl;
                    }
                }
            }
            if (CsmKindUtilities.isNamespaceDefinition(scope)) {
                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) scope;
                CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION);
                Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(nsd, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    if (((CsmClassForwardDeclaration) decl).getCsmClass().equals(cls)) {
                        return (CsmClassForwardDeclaration) decl;
                    }
                }
                filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION);
                declarations = CsmSelect.getDeclarations(nsd, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    CsmClassForwardDeclaration fdecl = findCsmClassForwardDeclaration((CsmNamespaceDefinition) decl, cls);
                    if (fdecl != null) {
                        return fdecl;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public T getTemplateDeclaration() {
        return declaration;
    }

    @Override
    public Map<CsmTemplateParameter, CsmSpecializationParameter> getMapping() {
        return mapping;
    }

    @Override
    public boolean isValid() {
        return CsmBaseUtilities.isValid(declaration);
    }

    public static CsmObject create(CsmTemplate template, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
//        System.err.println("Instantiation.create for " + template + " with mapping " + mapping);
        if (template instanceof CsmClass) {
            Class newClass = new Class((CsmClass)template, mapping);
            if(UIDProviderIml.isPersistable(newClass.getUID())) {
                CsmFile file = newClass.getContainingFile();
                if(file instanceof FileImpl) {
                    ((FileImpl)file).addInstantiation(newClass);
                }
            }
            return newClass;
        } else if (template instanceof CsmFunction) {
            return new Function((CsmFunction)template, mapping);
        } else {
            if (CndUtils.isDebugMode()) {
                CndUtils.assertTrueInConsole(false, "Unknown class " + template.getClass() + " for template instantiation:" + template); // NOI18N
            }
        }
        return template;
    }

    @Override
    public CsmFile getContainingFile() {
        return getTemplateDeclaration().getContainingFile();
    }

    @Override
    public CharSequence getText() {
        return getTemplateDeclaration().getText();
    }

    @Override
    public Kind getKind() {
        return getTemplateDeclaration().getKind();
    }

    @Override
    public CharSequence getUniqueName() {
        return getTemplateDeclaration().getUniqueName();
    }

    @Override
    public CharSequence getQualifiedName() {
        return getTemplateDeclaration().getQualifiedName();
    }

    @Override
    public CharSequence getName() {
        return getTemplateDeclaration().getName();
    }

    @Override
    public CsmScope getScope() {
        return getTemplateDeclaration().getScope();
    }

    @Override
    protected CsmUID<?> createUID() {
        return createInstantiationUID(this);
    }

    public static <T extends CsmInstantiation> CsmUID<?> createInstantiationUID(CsmInstantiation inst) {
        if(CsmKindUtilities.isClass(inst) && inst.getTemplateDeclaration() instanceof ClassImpl) {
            final Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = inst.getMapping();        
            boolean persistable = !mapping.keySet().isEmpty();
            for (CsmTemplateParameter param : mapping.keySet()) {
                CsmSpecializationParameter specParam = mapping.get(param);
                if(CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam)) {
                    if(!PersistentUtils.isPersistable(((CsmTypeBasedSpecializationParameter)specParam).getType())) {
                        persistable = false;
                    }
                } else {
                    persistable = false;
                }
            }
            if(persistable) {
                return UIDUtilities.createInstantiationUID(inst);
            }
        }
        return new InstantiationSelfUID((Instantiation)inst);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert (declaration instanceof ClassImpl);
        
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUID(UIDCsmConverter.declarationToUID(declaration), output);

        List<CsmUID<CsmTemplateParameter>> keys = new ArrayList<CsmUID<CsmTemplateParameter>>();
        List<CsmSpecializationParameter> vals = new ArrayList<CsmSpecializationParameter>();
        for (CsmTemplateParameter key : mapping.keySet()) {
            keys.add(UIDCsmConverter.declarationToUID(key));
            vals.add(mapping.get(key));
        }
        factory.writeUIDCollection(keys, output, true);
        PersistentUtils.writeSpecializationParameters(vals, output);
    }

    public Instantiation(RepositoryDataInput input) throws IOException {
        super(input);

        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        
        CsmUID<T> declUID = factory.readUID(input);
        declaration = declUID.getObject();
        
        List<CsmUID<CsmTemplateParameter>> keys = new ArrayList<CsmUID<CsmTemplateParameter>>();
        List<CsmSpecializationParameter> vals = new ArrayList<CsmSpecializationParameter>();
        
        factory.readUIDCollection(keys, input);
        PersistentUtils.readSpecializationParameters(vals, input);
        
        mapping = new HashMap<CsmTemplateParameter, CsmSpecializationParameter>();
        for (int i = 0; i < keys.size() && i < vals.size(); i++) {
            mapping.put(keys.get(i).getObject(), vals.get(i));
        }
    }            
    
    //////////////////////////////
    ////////////// STATIC MEMBERS
    public static class Class extends Instantiation<CsmClass> implements CsmClass, CsmMember, CsmTemplate,
                                    SelectImpl.FilterableMembers {
        public Class(CsmClass clazz, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(clazz, mapping);
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return declaration.getScopeElements();
        }

        @Override
        public Collection<CsmTypedef> getEnclosingTypedefs() {
            return declaration.getEnclosingTypedefs();
        }

        @Override
        public Collection<CsmVariable> getEnclosingVariables() {
            return declaration.getEnclosingVariables();
        }

        @Override
        public boolean isTemplate() {
            return ((CsmTemplate)declaration).isTemplate();
        }

        @Override
        public boolean isSpecialization() {
            return ((CsmTemplate) declaration).isSpecialization();
        }

        @Override
        public boolean isExplicitSpecialization() {
            return ((CsmTemplate) declaration).isExplicitSpecialization();
        }
        
        private boolean isRecursion(CsmTemplate type, int i){
            if (i == 0) {
                return true;
            }
            if (type instanceof Class) {
                Class t = (Class) type;
                return isRecursion((CsmTemplate)t.declaration, i-1);
            }
            return false;
        }

        private CsmMember createMember(CsmMember member) {
            if (member instanceof CsmField) {
                return new Field((CsmField)member, this);
            } else if (member instanceof CsmMethod) {
                return new Method((CsmMethod)member, this);
            } else if (member instanceof CsmTypedef) {
                return new Typedef((CsmTypedef)member, this);
            } else if (member instanceof CsmClass) {
                Class newClass = new Class((CsmClass)member, getMapping());
                if(UIDProviderIml.isPersistable(newClass.getUID())) {
                    CsmFile file = newClass.getContainingFile();
                    if(file instanceof FileImpl) {
                        ((FileImpl)file).addInstantiation(newClass);
                    }
                }
                return newClass;
            } else if (member instanceof CsmClassForwardDeclaration) {
                return new ClassForward((CsmClassForwardDeclaration)member, getMapping());
            } else if (member instanceof CsmEnumForwardDeclaration) {
                return new EnumForward((CsmEnumForwardDeclaration)member, getMapping());
            } else if (member instanceof CsmEnum) {
                // no need to instantiate enums?
                return member;
            } else if (member instanceof CsmUsingDeclaration) {
                // no need to instantiate usings?
                return member;
            }
            assert false : "Unknown class for member instantiation:" + member + " of class:" + member.getClass(); // NOI18N
            return member;
        }

        @Override
        public Collection<CsmMember> getMembers() {
            Collection<CsmMember> res = new ArrayList<CsmMember>();
            for (CsmMember member : declaration.getMembers()) {
                res.add(createMember(member));
            }
            return res;
        }

        @Override
        public Iterator<CsmMember> getMembers(CsmFilter filter) {
            Collection<CsmMember> res = new ArrayList<CsmMember>();
            Iterator<CsmMember> it = CsmSelect.getClassMembers(declaration, filter);
            while(it.hasNext()){
                res.add(createMember(it.next()));
            }
            return res.iterator();
        }

        @Override
        public int getLeftBracketOffset() {
            return declaration.getLeftBracketOffset();
        }

        @Override
        public Collection<CsmFriend> getFriends() {
            return declaration.getFriends();
        }

        @Override
        public Collection<CsmInheritance> getBaseClasses() {
            Collection<CsmInheritance> res = new ArrayList<CsmInheritance>();
            for (CsmInheritance inh : declaration.getBaseClasses()) {
                res.add(new Inheritance(inh, this));
            }
            return res;
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF CLASS: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }

        @Override
        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        @Override
        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        @Override
        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }

        @Override
        public CharSequence getDisplayName() {
            return ((CsmTemplate)declaration).getDisplayName();
        }

        @Override
        public List<CsmTemplateParameter> getTemplateParameters() {
            return ((CsmTemplate)declaration).getTemplateParameters();
        }
        
        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
        }

        public Class(RepositoryDataInput input) throws IOException {
            super(input);
        }        
        
    }

    private static class Inheritance implements CsmInheritance {
        private final CsmInheritance inheritance;
        private final CsmType type;
        private CsmClassifier resolvedClassifier;

        public Inheritance(CsmInheritance inheritance, Instantiation instantiation) {
            this.inheritance = inheritance;
            this.type = createType(inheritance.getAncestorType(), instantiation);
        }

        @Override
        public CsmType getAncestorType() {
            return type;
        }

        @Override
        public CharSequence getText() {
            return inheritance.getText();
        }

        @Override
        public Position getStartPosition() {
            return inheritance.getStartPosition();
        }

        @Override
        public int getStartOffset() {
            return inheritance.getStartOffset();
        }

        @Override
        public Position getEndPosition() {
            return inheritance.getEndPosition();
        }

        @Override
        public int getEndOffset() {
            return inheritance.getEndOffset();
        }

        @Override
        public CsmFile getContainingFile() {
            return inheritance.getContainingFile();
        }

        @Override
        public boolean isVirtual() {
            return inheritance.isVirtual();
        }

        @Override
        public CsmVisibility getVisibility() {
            return inheritance.getVisibility();
        }

        @Override
        public CsmClassifier getClassifier() {
            if (resolvedClassifier == null) {
                CsmType t= getAncestorType();
                resolvedClassifier = t.getClassifier();                        
            }
            return resolvedClassifier;
        }

        @Override
        public CsmScope getScope() {
            return inheritance.getScope();
        }
        
        @Override
        public String toString() {
            return "INSTANTION OF INHERITANCE: " + inheritance + " with " + type; // NOI18N
        }
    }

    private static class Function extends Instantiation<CsmFunction> implements CsmFunction {
        private final CsmType retType;

        public Function(CsmFunction function, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(function, mapping);
            this.retType = createType(function.getReturnType(), Function.this);
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return declaration.getScopeElements();
        }

        public boolean isTemplate() {
            return ((CsmTemplate)declaration).isTemplate();
        }

        @Override
        public boolean isInline() {
            return declaration.isInline();
        }

        @Override
        public boolean isOperator() {
            return declaration.isOperator();
        }

        @Override
        public OperatorKind getOperatorKind() {
            return declaration.getOperatorKind();
        }

        @Override
        public CharSequence getSignature() {
            return declaration.getSignature();
        }

        @Override
        public CsmType getReturnType() {
            return retType;
        }

        @Override
        public CsmFunctionParameterList getParameterList() {
            ArrayList<CsmParameter> res = new ArrayList<CsmParameter>();
            Collection<CsmParameter> parameters = declaration.getParameterList().getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, this));
            }
            res.trimToSize();
            return FunctionParameterListImpl.create(declaration.getParameterList(), res);
        }

        @Override
        public Collection<CsmParameter> getParameters() {
            Collection<CsmParameter> res = new ArrayList<CsmParameter>();
            Collection<CsmParameter> parameters = declaration.getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, this));
            }
            return res;
        }

        @Override
        public CsmFunctionDefinition getDefinition() {
            return declaration.getDefinition();
        }

        @Override
        public CsmFunction getDeclaration() {
            return declaration.getDeclaration();
        }

        @Override
        public CharSequence getDeclarationText() {
            return declaration.getDeclarationText();
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF FUNCTION: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }

        @Override
        public boolean isStatic() {
            return false;
        }
    }

    private static class Field extends Instantiation<CsmField> implements CsmField {
        private final CsmType type;

        public Field(CsmField field, CsmInstantiation instantiation) {
            super(field, instantiation.getMapping());
            this.type = createType(field.getType(), instantiation);
        }

        @Override
        public boolean isExtern() {
            return declaration.isExtern();
        }

        @Override
        public CsmType getType() {
            return type;
        }

        @Override
        public CsmExpression getInitialValue() {
            return declaration.getInitialValue();
        }

        @Override
        public CharSequence getDisplayText() {
            return declaration.getDisplayText();
        }

        @Override
        public CsmVariableDefinition getDefinition() {
            return declaration.getDefinition();
        }

        @Override
        public CharSequence getDeclarationText() {
            return declaration.getDeclarationText();
        }

        @Override
        public boolean isStatic() {
            return declaration.isStatic();
        }

        @Override
        public CsmVisibility getVisibility() {
            return declaration.getVisibility();
        }

        @Override
        public CsmClass getContainingClass() {
            return declaration.getContainingClass();
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF FIELD: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }

    private static class Typedef extends Instantiation<CsmTypedef> implements CsmTypedef, CsmMember {
        private final CsmType type;

        public Typedef(CsmTypedef typedef, CsmInstantiation instantiation) {
            super(typedef, instantiation.getMapping());
            this.type = createType(typedef.getType(), instantiation);
        }

        @Override
        public boolean isTypeUnnamed() {
            return declaration.isTypeUnnamed();
        }

        @Override
        public CsmType getType() {
            return type;
        }

        @Override
        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        @Override
        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        @Override
        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF TYPEDEF: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }

    private static class ClassForward extends Instantiation<CsmClassForwardDeclaration> implements CsmClassForwardDeclaration, CsmMember {
        private CsmClass csmClass = null;

        public ClassForward(CsmClassForwardDeclaration forward, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(forward, mapping);
        }

        @Override
        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        @Override
        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        @Override
        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }

        @Override
        public CsmClass getCsmClass() {
            if (csmClass == null) {
                CsmClass declClassifier = declaration.getCsmClass();
                if (CsmKindUtilities.isTemplate(declClassifier)) {
                    csmClass = (CsmClass)Instantiation.create((CsmTemplate)declClassifier, getMapping());
                } else {
                    csmClass = declClassifier;
                }
            }
            return csmClass;
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF CLASS FORWARD: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }

    private static class EnumForward extends Instantiation<CsmEnumForwardDeclaration> implements CsmEnumForwardDeclaration, CsmMember {

        private CsmEnum csmEnum = null;

        public EnumForward(CsmEnumForwardDeclaration forward, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(forward, mapping);
        }

        @Override
        public CsmClass getContainingClass() {
            return ((CsmMember) declaration).getContainingClass();
        }

        @Override
        public CsmVisibility getVisibility() {
            return ((CsmMember) declaration).getVisibility();
        }

        @Override
        public boolean isStatic() {
            return ((CsmMember) declaration).isStatic();
        }

        @Override
        public CsmEnum getCsmEnum() {
            if (csmEnum == null) {
                CsmEnum declClassifier = declaration.getCsmEnum();
                if (CsmKindUtilities.isTemplate(declClassifier)) {
                    csmEnum = (CsmEnum) Instantiation.create((CsmTemplate) declClassifier, getMapping());
                } else {
                    csmEnum = declClassifier;
                }
            }
            return csmEnum;
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF ENUM FORWARD: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }

    private static class Method extends Instantiation<CsmMethod> implements CsmMethod, CsmFunctionDefinition {
        private final CsmInstantiation instantiation;
        private final CsmType retType;
        private CsmFunctionDefinition definition = null;
        private CsmClass containingClass = null;

        public Method(CsmMethod method, CsmInstantiation instantiation) {
            super(method, instantiation.getMapping());
            this.instantiation = instantiation;
            this.retType = createType(method.getReturnType(), instantiation);
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return declaration.getScopeElements();
        }

        @Override
        public boolean isStatic() {
            return declaration.isStatic();
        }

        @Override
        public CsmVisibility getVisibility() {
            return declaration.getVisibility();
        }

        @Override
        public CsmClass getContainingClass() {
            if(containingClass == null) {
                containingClass = _getContainingClass();
            }
            return containingClass;
        }
        
        public CsmClass _getContainingClass() {
            CsmClass containingClass = declaration.getContainingClass();
            if(CsmKindUtilities.isTemplate(containingClass)) {
                CsmInstantiationProvider p = CsmInstantiationProvider.getDefault();
                if (p instanceof InstantiationProviderImpl) {
                    CsmObject inst = ((InstantiationProviderImpl) p).instantiate((CsmTemplate)containingClass, instantiation);
                    if (inst instanceof CsmClass) {
                        return (CsmClass) inst;
                    }
                }
            }
            return containingClass;
        }

        public boolean isTemplate() {
            return ((CsmTemplate)declaration).isTemplate();
        }

        @Override
        public boolean isInline() {
            return declaration.isInline();
        }

        @Override
        public CharSequence getSignature() {
            return declaration.getSignature();
        }

        @Override
        public CsmType getReturnType() {
            return retType;
        }

        @Override
        public CsmFunctionParameterList getParameterList() {
            Collection<CsmParameter> res = new ArrayList<CsmParameter>();
            Collection<CsmParameter> parameters = ((CsmFunction) declaration).getParameterList().getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, this));
            }
            return FunctionParameterListImpl.create(((CsmFunction) declaration).getParameterList(), res);
        }

        @Override
        public Collection<CsmParameter> getParameters() {
            Collection<CsmParameter> res = new ArrayList<CsmParameter>();
            Collection<CsmParameter> parameters = declaration.getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, instantiation));
            }
            return res;
        }

        @Override
        public CsmFunctionDefinition getDefinition() {
            if(definition == null) {
                definition = _getDefinition();
            }
            return definition;
        }

        public CsmFunctionDefinition _getDefinition() {
            CsmClass cls = getContainingClass();
            if (CsmKindUtilities.isSpecialization(cls) && declaration instanceof FunctionImpl) {
                FunctionImpl decl = (FunctionImpl) declaration;
                return decl.getDefinition(cls);
            }
            return declaration.getDefinition();
        }

        @Override
        public CharSequence getDeclarationText() {
            return declaration.getDeclarationText();
        }

        @Override
        public boolean isVirtual() {
            return declaration.isVirtual();
        }

        @Override
        public boolean isExplicit() {
            return declaration.isExplicit();
        }

        @Override
        public boolean isConst() {
            return declaration.isConst();
        }

        @Override
        public boolean isAbstract() {
            return declaration.isAbstract();
        }

        @Override
        public boolean isOperator() {
            return declaration.isOperator();
        }

        @Override
        public OperatorKind getOperatorKind() {
            return declaration.getOperatorKind();
        }

        @Override
        public CsmCompoundStatement getBody() {
            if (CsmKindUtilities.isFunctionDefinition(declaration)) {
                return ((CsmFunctionDefinition)declaration).getBody();
            }
            return null;
        }

        @Override
        public CsmFunction getDeclaration() {
            if (CsmKindUtilities.isFunctionDefinition(declaration)) {
                return ((CsmFunctionDefinition)declaration).getDeclaration();
            }
            return this;
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF METHOD: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }

    private static class Parameter extends Instantiation<CsmParameter> implements CsmParameter {
        private final CsmType type;

        public Parameter(CsmParameter parameter, CsmInstantiation instantiation) {
            super(parameter, instantiation.getMapping());
            this.type = parameter.isVarArgs() ? TypeFactory.getVarArgType() : createType(parameter.getType(), instantiation);
        }

        @Override
        public boolean isExtern() {
            return declaration.isExtern();
        }

        @Override
        public CsmType getType() {
            return type;
        }

        @Override
        public CsmExpression getInitialValue() {
            return declaration.getInitialValue();
        }

        @Override
        public CharSequence getDisplayText() {
            return declaration.getDisplayText();
        }

        @Override
        public CsmVariableDefinition getDefinition() {
            return declaration.getDefinition();
        }

        @Override
        public CharSequence getDeclarationText() {
            return declaration.getDeclarationText();
        }

        @Override
        public boolean isVarArgs() {
            return declaration.isVarArgs();
        }

        @Override
        public String toString() {
            return "INSTANTIATION OF FUN PARAM: " + getTemplateDeclaration() + " with types (" + mapping + ")"; // NOI18N
        }
    }
    
    public static CsmType createType(CsmType type, CsmInstantiation instantiation) {
        if (type == null) {
            throw new NullPointerException("no type for " + instantiation); // NOI18N
        }
//        System.err.println("Instantiation.createType for " + type + " with instantiation " + instantiation);
        if (CsmKindUtilities.isTemplateParameterType(type)) {
            CsmType instantiatedType = resolveTemplateParameterType(type, instantiation);
            if (instantiatedType == null || CsmKindUtilities.isTemplateParameterType(instantiatedType)) {
                return new TemplateParameterType(type, instantiation);
            }
        }
        if (type instanceof org.netbeans.modules.cnd.modelimpl.csm.NestedType ||
                type instanceof NestedType) {
            return new NestedType(type, instantiation);
        }
        return new Type(type, instantiation);       
    }

    private static CsmType resolveTemplateParameterType(CsmType type, CsmInstantiation instantiation) {
        if (CsmKindUtilities.isTemplateParameterType(type)) {
            Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = TemplateUtils.gatherMapping(instantiation);
            CsmSpecializationParameter instantiatedType = mapping.get(((CsmTemplateParameterType) type).getParameter());
            int iteration = MAX_INHERITANCE_DEPTH;
            while (CsmKindUtilities.isTypeBasedSpecalizationParameter(instantiatedType) &&
                    CsmKindUtilities.isTemplateParameterType(((CsmTypeBasedSpecializationParameter) instantiatedType).getType()) && iteration != 0) {
                CsmSpecializationParameter nextInstantiatedType = mapping.get(((CsmTemplateParameterType) ((CsmTypeBasedSpecializationParameter) instantiatedType).getType()).getParameter());
                if (nextInstantiatedType != null) {
                    instantiatedType = nextInstantiatedType;
                } else {
                    break;
                }
                iteration--;
            }
            if (instantiatedType != null && instantiatedType instanceof CsmTypeBasedSpecializationParameter) {
                return ((CsmTypeBasedSpecializationParameter) instantiatedType).getType();
            }
        }
        return type;
    }
   
    private static class TemplateParameterType extends Type implements CsmTemplateParameterType {
        public TemplateParameterType(CsmType type, CsmInstantiation instantiation) {
            super(type, instantiation);
        }

        @Override
        public CsmTemplateParameter getParameter() {
            return ((CsmTemplateParameterType)instantiatedType).getParameter();
        }

        @Override
        public CsmType getTemplateType() {
            return ((CsmTemplateParameterType)instantiatedType).getTemplateType();
        }
    }

    private static class Type implements CsmType, Resolver.SafeTemplateBasedProvider {
        protected final CsmType originalType;
        protected final CsmInstantiation instantiation;
        protected final CsmType instantiatedType;
        protected final boolean inst;
        protected CsmClassifier resolved;
        protected CsmTemplateParameter parameter;

        private Type(CsmType type, CsmInstantiation instantiation) {
            this.instantiation = instantiation;
            inst = type.isInstantiation();
            CsmType origType = type;
            CsmType newType = type;
            parameter = null;

            if (CsmKindUtilities.isTemplateParameterType(type)) {
                CsmTemplateParameterType paramType = (CsmTemplateParameterType)type;
                parameter = paramType.getParameter();
                newType = Instantiation.resolveTemplateParameterType(type, instantiation);
                if (newType != null) {
                    int pointerDepth = (newType != origType ? newType.getPointerDepth() + origType.getPointerDepth() : origType.getPointerDepth());
                    int arrayDepth = (newType != origType ? newType.getArrayDepth() + origType.getArrayDepth() : origType.getArrayDepth());
                    
                    newType = TypeFactory.createType(
                            newType, 
                            pointerDepth, 
                            TypeFactory.getReferenceValue(origType), 
                            arrayDepth,
                            origType.isConst()
                    );             
                    
                    CsmTemplateParameter p = paramType.getParameter();
                    if (CsmKindUtilities.isTemplate(p)) {
                        CsmType paramTemplateType = paramType.getTemplateType();
                        if (paramTemplateType != null) {
                            List<CsmSpecializationParameter> paramInstParams = paramTemplateType.getInstantiationParams();
                            if (!paramInstParams.isEmpty()) {
                                List<CsmSpecializationParameter> newInstParams = new ArrayList<CsmSpecializationParameter>(newType.getInstantiationParams());
                                boolean updateInstParams = false;
                                for (CsmSpecializationParameter param : paramInstParams) {
                                    if (!newInstParams.contains(param)) {
                                        newInstParams.add(param);
                                        updateInstParams = true;
                                    }
                                }
                                if(updateInstParams) {
                                    newType = TypeFactory.createType(newType, newInstParams);
                                }
                            }
                        }
                    }
                    origType = paramType.getTemplateType();
                } else {
                    newType = type;
                }
            }

            if(!isRecursion(Type.this, MAX_INHERITANCE_DEPTH)) {
                this.originalType = origType;
                this.instantiatedType = newType;
            } else {
                CndUtils.assertTrueInConsole(false, "Infinite recursion in file " + Type.this.getContainingFile() + " type " + Type.this.toString()); //NOI18N
                this.originalType = origType;
                this.instantiatedType = origType;
            }
        }

        public boolean instantiationHappened() {
            return originalType != instantiatedType;
        }

        @Override
        public CharSequence getClassifierText() {
            return instantiatedType.getClassifierText().toString() + TypeImpl.getInstantiationText(this);
        }

        private CharSequence getInstantiatedText() {
            return getTextImpl(true);
        }

        @Override
        public CharSequence getText() {
            return getTextImpl(false);
        }
        
        private CharSequence getTextImpl(boolean instantiate) {
            if (originalType instanceof TypeImpl) {
                // try to instantiate original classifier
                CsmClassifier classifier = null;
                if (instantiate) {
                    classifier = getClassifier();
                    if (classifier != null) {
                        classifier = CsmBaseUtilities.getOriginalClassifier(classifier, getContainingFile());
                    }
                }
                CharSequence clsText;
                if (classifier == null || CsmKindUtilities.isInstantiation(classifier)) {
                    clsText = getClassifierText();
                } else {
                    clsText = classifier.getName();
                }
                return ((TypeImpl)originalType).decorateText( clsText, this, false, null);
            }
            if (originalType instanceof NestedType) {
                return ((NestedType)originalType).getOwnText();
            }
            return originalType.getText();
        }

        public CharSequence getOwnText() {
            if (originalType instanceof TypeImpl) {
                return ((TypeImpl) originalType).getOwnText();
            }
            return originalType.getText();
        }

        @Override
        public Position getStartPosition() {
            return instantiatedType.getStartPosition();
        }

        @Override
        public int getStartOffset() {
            return instantiatedType.getStartOffset();
        }

        @Override
        public Position getEndPosition() {
            return instantiatedType.getEndPosition();
        }

        @Override
        public int getEndOffset() {
            return instantiatedType.getEndOffset();
        }

        @Override
        public CsmFile getContainingFile() {
            return instantiatedType.getContainingFile();
        }

        @Override
        public boolean isInstantiation() {
            return instantiatedType.isInstantiation();
        }

        private boolean isRecursion(CsmType type, int i){
            if (i == 0) {
                return true;
            }
            if (type instanceof Instantiation.NestedType) {
                Instantiation.NestedType t = (NestedType) type;
                if (t.parentType != null) {
                    return isRecursion(t.parentType, i-1);
                } else {
                    return isRecursion(t.instantiatedType, i-1);
                }
            } else if (type instanceof Type) {
                return isRecursion(((Type)type).instantiatedType, i-1);
            } else if (type instanceof org.netbeans.modules.cnd.modelimpl.csm.NestedType) {
                org.netbeans.modules.cnd.modelimpl.csm.NestedType t = (org.netbeans.modules.cnd.modelimpl.csm.NestedType) type;
                if (t.getParent() != null) {
                    return isRecursion(t.getParent(), i-1);
                } else {
                    return false;
                }
            } else if (type instanceof TypeImpl){
                return false;
            } else if (type instanceof TemplateParameterTypeImpl){
                return isRecursion(((TemplateParameterTypeImpl)type).getTemplateType(), i-1);
            }
            return false;
        }


        @Override
        public boolean isTemplateBased() {
            return isTemplateBased(new HashSet<CsmType>());
        }

        @Override
        public boolean isTemplateBased(Set<CsmType> visited) {
            if (instantiatedType == null) {
                return true;
            }
            if (visited.contains(this)) {
                return false;
            }
            visited.add(this);
            if (instantiatedType instanceof SafeTemplateBasedProvider) {
                return ((SafeTemplateBasedProvider)instantiatedType).isTemplateBased(visited);
            }
            return instantiatedType.isTemplateBased();
        }

        @Override
        public boolean isReference() {
            if(instantiationHappened()) {
                return originalType.isReference() || instantiatedType.isReference();
            } else {
                return originalType.isReference();
            }
        }

        @Override
        public boolean isRValueReference() {
            if (instantiationHappened()) {
                return originalType.isRValueReference() || instantiatedType.isRValueReference();
            } else {
                return originalType.isRValueReference();
            }
        }
        
        @Override
        public boolean isPointer() {
            if(instantiationHappened()) {
                return originalType.isPointer() || instantiatedType.isPointer();
            } else {
                return originalType.isPointer();
            }
        }

        @Override
        public boolean isConst() {
            if(instantiationHappened()) {
                return originalType.isConst() || instantiatedType.isConst();
            } else {
                return originalType.isConst();
            }
        }

        @Override
        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return instantiatedType.isBuiltInBased(resolveTypeChain);
        }

        @Override
        public List<CsmSpecializationParameter> getInstantiationParams() {
            if (!originalType.isInstantiation()) {
                return Collections.emptyList();
            }
            List<CsmSpecializationParameter> res = new ArrayList<CsmSpecializationParameter>();
            for (CsmSpecializationParameter instParam : originalType.getInstantiationParams()) {
                if (CsmKindUtilities.isTypeBasedSpecalizationParameter(instParam) &&
                        CsmKindUtilities.isTemplateParameterType(((CsmTypeBasedSpecializationParameter) instParam).getType())) {
                    CsmTemplateParameterType paramType = (CsmTemplateParameterType) ((CsmTypeBasedSpecializationParameter) instParam).getType();
                    CsmSpecializationParameter newTp = instantiation.getMapping().get(paramType.getParameter());
                    if (newTp != null && newTp != instParam) {
                        res.add(newTp);
                    } else {
                        res.add(instParam);
                    }
                } else {
                    res.add(instParam);
                }
            }
            return res;
        }

        @Override
        public int getPointerDepth() {
            if (instantiationHappened()) {
                return instantiatedType.getPointerDepth();
            } else {
                return originalType.getPointerDepth();
            }
        }

        @Override
        public CsmClassifier getClassifier() {
            return  getClassifier(new ArrayList<CsmInstantiation>(), false);
        }
        
        public CsmClassifier getClassifier(List<CsmInstantiation> instantiations, boolean specialize) {
            instantiations.add(instantiation);
            if (resolved == null) {
                if (!instantiationHappened()) {
                    CsmClassifier classifier;
                    if(originalType instanceof TypeImpl) {
                        classifier = ((TypeImpl)originalType).getClassifier(instantiations, false);
                    } else if(originalType instanceof Type) {
                        classifier = ((Type)originalType).getClassifier(instantiations, false);                               
                    } else {
                        classifier = originalType.getClassifier();                        
                    }
                    if (inst && CsmKindUtilities.isTemplate(classifier)) {
                        CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
                        CsmObject obj = null;
                        if(ip instanceof InstantiationProviderImpl) {
                            Resolver resolver = ResolverFactory.createResolver(this);
                            try {
                                if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                                    if(!isTemplateParameterTypeBased() || !instantiation.getMapping().keySet().contains(getResolvedTemplateParameter())) {
                                        obj = ((InstantiationProviderImpl)ip).instantiate((CsmTemplate) classifier, instantiation, specialize);
                                    } else {
//                                        final Map<CsmTemplateParameter, CsmSpecializationParameter> mapping1 = new HashMap<CsmTemplateParameter, CsmSpecializationParameter>(instantiation.getMapping());
//                                        mapping1.remove(getResolvedTemplateParameter());
//                                        obj = ((InstantiationProviderImpl)ip).instantiate((CsmTemplate) classifier, mapping1);
                                    }
                                } else {
                                    return null;
                                }
                            } finally {
                                ResolverFactory.releaseResolver(resolver);
                            }
                        } else {
                            obj = ip.instantiate((CsmTemplate) classifier, instantiation);
                        }
                        if (CsmKindUtilities.isClassifier(obj)) {
                            resolved = (CsmClassifier) obj;
                            return resolved;
                        }
                    }
                    resolved = classifier;
                } else {
                    if(instantiatedType instanceof TypeImpl) {
                        resolved = ((TypeImpl)instantiatedType).getClassifier(instantiations, false);
                    } else if(instantiatedType instanceof Type) {
                        resolved = ((Type)instantiatedType).getClassifier(instantiations, false);                               
                    } else {
                        resolved = instantiatedType.getClassifier();                        
                    }
                } 
                
                if (CsmKindUtilities.isTypedef(resolved) && CsmKindUtilities.isClassMember(resolved)) {
                    CsmMember tdMember = (CsmMember)resolved;
                    if (CsmKindUtilities.isTemplate(tdMember.getContainingClass())) {
                        resolved = new Typedef((CsmTypedef)resolved, instantiation);
                        return resolved;
                    }
                }
                if (CsmKindUtilities.isClass(resolved) && CsmKindUtilities.isClassMember(resolved)) {
                    CsmMember tdMember = (CsmMember)resolved;
                    if (CsmKindUtilities.isTemplate(tdMember.getContainingClass())) {
                        resolved = new Class((CsmClass)resolved, instantiation.getMapping());
                        return resolved;
                    }
                }
            }
            return resolved;
        }

        @Override
        public CharSequence getCanonicalText() {
            return originalType.getCanonicalText();
        }

        @Override
        public int getArrayDepth() {
            if (instantiationHappened()) {
                return originalType.getArrayDepth() + instantiatedType.getArrayDepth();
            } else {
                return originalType.getArrayDepth();
            }
        }

        public CsmInstantiation getInstantiation() {
            return instantiation;
        }
        
        public boolean isTemplateParameterTypeBased() {
            CsmType baseType = originalType;
            while(baseType instanceof Type) {
                if(((Type)baseType).instantiationHappened()) {
                    return true;
                }
                baseType = ((Type)baseType).originalType;
            }
            return false;
        }        
        
        public CsmTemplateParameter getResolvedTemplateParameter() {
            CsmType baseType = originalType;
            while(baseType instanceof Type) {
                if(((Type)baseType).parameter != null) {
                    return ((Type)baseType).parameter;
                }
                baseType = ((Type)baseType).originalType;
            }
            return null;
        }         
        
        @Override
        public String toString() {
            String res = "INSTANTIATION OF TYPE: " + originalType + " with types (" + instantiation.getMapping() + ")"; // NOI18N
            if (instantiationHappened()) {
                res += " becomes " + instantiatedType; // NOI18N
            }
            return res;
        }
    }

    private static class NestedType extends Type {

        private final CsmType parentType;

        private NestedType(CsmType type, CsmInstantiation instantiation) {
            super(type, instantiation);

            if (type instanceof org.netbeans.modules.cnd.modelimpl.csm.NestedType) {
                org.netbeans.modules.cnd.modelimpl.csm.NestedType t = (org.netbeans.modules.cnd.modelimpl.csm.NestedType) type;
                CsmType parent = t.getParent();
                if (parent != null) {
                    parentType = createType(parent, instantiation);
                } else {
                    parentType = null;
                }
            } else if (type instanceof NestedType) {
                NestedType t = (NestedType) type;
                CsmType parent = t.parentType;
                if (parent != null) {
                    parentType = createType(parent, instantiation);
                } else {
                    parentType = null;
                }
            } else {
                parentType = null;
            }
        }

        @Override
        public CsmClassifier getClassifier() {
            return getClassifier(new ArrayList<CsmInstantiation>(), false);
        }
        
        @Override
        public CsmClassifier getClassifier(List<CsmInstantiation> instantiations, boolean specialize) {
            instantiations.add(instantiation);
            if (resolved == null) {
                if(!instantiationHappened()) {
                    if (parentType != null) {
                        CsmClassifier parentClassifier;
                        if(parentType instanceof TypeImpl) {
                            parentClassifier = ((TypeImpl)parentType).getClassifier(instantiations, false);
                        } else if(parentType instanceof Type) {
                            parentClassifier = ((Type)parentType).getClassifier(instantiations, false);
                        } else {
                            parentClassifier = parentType.getClassifier();                        
                        }
                        if (CsmBaseUtilities.isValid(parentClassifier)) {
                            MemberResolverImpl memberResolver = new MemberResolverImpl();
                            if (instantiatedType instanceof org.netbeans.modules.cnd.modelimpl.csm.NestedType) {
                                resolved = getNestedClassifier(memberResolver, parentClassifier, ((org.netbeans.modules.cnd.modelimpl.csm.NestedType) instantiatedType).getOwnText());
                            } else if (instantiatedType instanceof NestedType) {
                                resolved = getNestedClassifier(memberResolver, parentClassifier, ((NestedType) instantiatedType).getOwnText());
                            }
                        }
                    } 
                    if (isInstantiation() && CsmKindUtilities.isTemplate(resolved) && !((CsmTemplate) resolved).getTemplateParameters().isEmpty()) {
                        CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
                        CsmObject obj = null;
                        if (ip instanceof InstantiationProviderImpl) {
                            Resolver resolver = ResolverFactory.createResolver(this);
                            try {
                                if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                                    obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) resolved, this, specialize);
                                } else {
                                    return null;
                                }
                            } finally {
                                ResolverFactory.releaseResolver(resolver);
                            }
                        } else {
                            obj = ip.instantiate((CsmTemplate) resolved, this);
                        }
                        if (CsmKindUtilities.isClassifier(obj)) {
                            resolved = (CsmClassifier) obj;
                        }
                    }
                } 
                if (resolved == null) {
                    if(instantiatedType instanceof TypeImpl) {
                        resolved = ((TypeImpl)instantiatedType).getClassifier(instantiations, false);
                    } else if(instantiatedType instanceof Type) {
                        resolved = ((Type)instantiatedType).getClassifier(instantiations, false);                               
                    } else {
                        resolved = instantiatedType.getClassifier();                        
                    }
                    if (isInstantiation() && CsmKindUtilities.isTemplate(resolved) && !((CsmTemplate) resolved).getTemplateParameters().isEmpty()) {
                        CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
                        CsmObject obj = null;
                        if (ip instanceof InstantiationProviderImpl) {
                            Resolver resolver = ResolverFactory.createResolver(this);
                            try {
                                if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                                    obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) resolved, instantiation, specialize);
                                } else {
                                    return null;
                                }
                            } finally {
                                ResolverFactory.releaseResolver(resolver);
                            }
                        } else {
                            obj = ip.instantiate((CsmTemplate) resolved, this);
                        }
                        if (CsmKindUtilities.isClassifier(obj)) {
                            resolved = (CsmClassifier) obj;
                        }
                    }
                }
                if (CsmKindUtilities.isTypedef(resolved) && CsmKindUtilities.isClassMember(resolved)) {
                    CsmMember tdMember = (CsmMember) resolved;
                    if (CsmKindUtilities.isTemplate(tdMember.getContainingClass())) {
                        resolved = new Typedef((CsmTypedef) resolved, instantiation);
                        return resolved;
                    }
                }
                if (CsmKindUtilities.isClass(resolved) && CsmKindUtilities.isClassMember(resolved)) {
                    CsmMember tdMember = (CsmMember)resolved;
                    if (CsmKindUtilities.isTemplate(tdMember.getContainingClass())) {
                        resolved = new Class((CsmClass)resolved, instantiation.getMapping());
                        return resolved;
                    }
                }                
            }
            return resolved;
        }

        @Override
        public boolean isInstantiation() {
            return (parentType != null && parentType.isInstantiation()) || super.isInstantiation();
        }

    }

    private static CsmClassifier getNestedClassifier(MemberResolverImpl memberResolver, CsmClassifier parentClassifier, CharSequence ownText) {
        return org.netbeans.modules.cnd.modelimpl.csm.NestedType.getNestedClassifier(memberResolver, parentClassifier, ownText);
    }

    public final static class InstantiationSelfUID implements CsmUID<CsmInstantiation>, SelfPersistent {
        private final Instantiation ref;
        private InstantiationSelfUID(Instantiation ref) {
            this.ref = ref;
        }

        @Override
        public Instantiation getObject() {
            return this.ref;
        }
        ////////////////////////////////////////////////////////////////////////////
        // impl for Persistent

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            // write nothing
        }

        public InstantiationSelfUID(RepositoryDataInput input) throws IOException {
            this.ref = null;
        }
    }
    
    public static CharSequence getInstantiatedText(CsmType type) {
        if (type instanceof Type) {
            return ((Type)type).getInstantiatedText();
        } else if (false && type.isInstantiation() && type.getClassifier() != null) {
            StringBuilder sb = new StringBuilder(type.getClassifier().getQualifiedName());
            sb.append(Instantiation.getInstantiationCanonicalText(type.getInstantiationParams()));
            return sb;
        } else {
            return type.getText();
        }
    }

    public static CharSequence getInstantiationCanonicalText(List<CsmSpecializationParameter> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append('<');
        boolean first = true;
        for (CsmSpecializationParameter param : params) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            if(CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                sb.append(TypeImpl.getCanonicalText(((CsmTypeBasedSpecializationParameter) param).getType()));
            }
            if(CsmKindUtilities.isExpressionBasedSpecalizationParameter(param)) {
                sb.append(param.getText());
            }
            if(CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
                sb.append(param.getText());
            }
        }
        TemplateUtils.addGREATERTHAN(sb);
        return sb;
    }
}
