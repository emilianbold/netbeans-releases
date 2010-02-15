/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.impl.services.SelectImpl;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;

/**
 * Implements CsmClass
 * @author Vladimir Kvashin
 */
public class ClassImpl extends ClassEnumBase<CsmClass> implements CsmClass, CsmTemplate, SelectImpl.FilterableMembers,
        DeclarationsContainer {

    private final CsmDeclaration.Kind kind;
    private final List<CsmUID<CsmMember>> members;
    private final List<CsmUID<CsmFriend>> friends;
    private final ArrayList<CsmInheritance> inheritances = new ArrayList<CsmInheritance>(0);
    private TemplateDescriptor templateDescriptor = null;
    private /*final*/ int leftBracketPos;

    private class ClassAstRenderer extends AstRenderer {
        private final boolean renderingLocalContext;
        private CsmVisibility curentVisibility = CsmVisibility.PRIVATE;

        public ClassAstRenderer(boolean renderingLocalContext) {
            super((FileImpl) ClassImpl.this.getContainingFile());
            this.renderingLocalContext = renderingLocalContext;
        }

        @Override
        protected boolean isRenderingLocalContext() {
            return renderingLocalContext;
        }

        @Override
        protected VariableImpl<CsmField> createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, boolean _extern,
                MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope) {
            type = TemplateUtils.checkTemplateType(type, ClassImpl.this);
            FieldImpl field = new FieldImpl(offsetAst, file, type, name, ClassImpl.this, curentVisibility, !isRenderingLocalContext());
            field.setStatic(_static);
            field.setExtern(_extern);
            ClassImpl.this.addMember(field,!isRenderingLocalContext());
            return field;
        }

        @Override
        public void render(AST ast) {
            Pair typedefs;
            AST child;
            for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                switch (token.getType()) {
                    //case CPPTokenTypes.CSM_TEMPLATE_PARMLIST:
                    case CPPTokenTypes.LITERAL_template:{
                        List<CsmTemplateParameter> params = TemplateUtils.getTemplateParameters(token, ClassImpl.this.getContainingFile(), ClassImpl.this, !isRenderingLocalContext());
                        String name = "<" + TemplateUtils.getClassSpecializationSuffix(token, null) + ">"; // NOI18N
                        setTemplateDescriptor(params, name);
                        break;
                    }
                    case CPPTokenTypes.CSM_BASE_SPECIFIER:
                        addInheritance(new InheritanceImpl(token, getContainingFile(), ClassImpl.this));
                        break;
                    // class / struct / union
                    case CPPTokenTypes.LITERAL_class:
                        break;
                    case CPPTokenTypes.LITERAL_union:
                        curentVisibility = CsmVisibility.PUBLIC;
                        break;
                    case CPPTokenTypes.LITERAL_struct:
                        curentVisibility = CsmVisibility.PUBLIC;
                        break;

                    // visibility
                    case CPPTokenTypes.LITERAL_public:
                        curentVisibility = CsmVisibility.PUBLIC;
                        break;
                    case CPPTokenTypes.LITERAL_private:
                        curentVisibility = CsmVisibility.PRIVATE;
                        break;
                    case CPPTokenTypes.LITERAL_protected:
                        curentVisibility = CsmVisibility.PROTECTED;
                        break;

                    // inner classes and enums
                    case CPPTokenTypes.CSM_CLASS_DECLARATION:
                    case CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION:
                        ClassImpl innerClass = TemplateUtils.isPartialClassSpecialization(token) 
                                ? ClassImplSpecialization.create(token, ClassImpl.this, getContainingFile(), !isRenderingLocalContext(), ClassImpl.this)
                                : ClassImpl.create(token, ClassImpl.this, getContainingFile(), !isRenderingLocalContext(), ClassImpl.this);
                        innerClass.setVisibility(curentVisibility);
                        addMember(innerClass,!isRenderingLocalContext());
                        typedefs = renderTypedef(token, innerClass, ClassImpl.this);
                        if (!typedefs.getTypesefs().isEmpty()) {
                            for (CsmTypedef typedef : typedefs.getTypesefs()) {
                                // It could be important to register in project before add as member...
                                if (!isRenderingLocalContext()) {
                                    ((FileImpl) getContainingFile()).getProjectImpl(true).registerDeclaration(typedef);
                                }
                                addMember((MemberTypedef) typedef,!isRenderingLocalContext());
                                if (typedefs.getEnclosingClassifier() != null){
                                    typedefs.getEnclosingClassifier().addEnclosingTypedef(typedef);
                                }
                            }
                            if (typedefs.getEnclosingClassifier() != null && !ForwardClass.isForwardClass(typedefs.getEnclosingClassifier())) {
                                addMember(typedefs.getEnclosingClassifier(), !isRenderingLocalContext());
                            }
                        }
                        renderVariableInClassifier(token, innerClass, null, null);
                        break;
                    case CPPTokenTypes.CSM_ENUM_DECLARATION:
                        EnumImpl innerEnum = EnumImpl.create(token, ClassImpl.this, getContainingFile(), !isRenderingLocalContext());
                        innerEnum.setVisibility(curentVisibility);
                        addMember(innerEnum,!isRenderingLocalContext());
                        renderVariableInClassifier(token, innerEnum, null, null);
                        break;

                    // other members
                    case CPPTokenTypes.CSM_CTOR_DEFINITION:
                    case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:
                        try {
                            addMember(new ConstructorDDImpl(token, ClassImpl.this, curentVisibility, !isRenderingLocalContext()), !isRenderingLocalContext());
                        } catch (AstRendererException e) {
                            DiagnosticExceptoins.register(e);
                        }
                        break;
                    case CPPTokenTypes.CSM_CTOR_DECLARATION:
                    case CPPTokenTypes.CSM_CTOR_TEMPLATE_DECLARATION:
                        try {
                            addMember(new ConstructorImpl(token, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                        } catch (AstRendererException e) {
                            DiagnosticExceptoins.register(e);
                        }
                        break;
                    case CPPTokenTypes.CSM_DTOR_DEFINITION:
                    case CPPTokenTypes.CSM_DTOR_TEMPLATE_DEFINITION:
                        try {
                            addMember(new DestructorDDImpl(token, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                        } catch (AstRendererException e) {
                            DiagnosticExceptoins.register(e);
                        }
                        break;
                    case CPPTokenTypes.CSM_DTOR_DECLARATION:
                        try {
                            addMember(new DestructorImpl(token, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                        } catch (AstRendererException e) {
                            DiagnosticExceptoins.register(e);
                        }
                        break;
                    case CPPTokenTypes.CSM_FIELD:
                        child = token.getFirstChild();
                        if (hasFriendPrefix(child)) {
                            try {
                                addFriend(renderFriendClass(token), !isRenderingLocalContext());
                            } catch (AstRendererException e) {
                                DiagnosticExceptoins.register(e);
                            }
                        } else {
                            if (renderVariable(token, null, null, false)) {
                                break;
                            }
                            typedefs = renderTypedef(token, (FileImpl) getContainingFile(), ClassImpl.this, null);
                            if (!typedefs.getTypesefs().isEmpty()) {
                                for (CsmTypedef typedef : typedefs.getTypesefs()) {
                                    // It could be important to register in project before add as member...
                                    if (!isRenderingLocalContext()) {
                                        ((FileImpl) getContainingFile()).getProjectImpl(true).registerDeclaration(typedef);
                                    }
                                    addMember((MemberTypedef) typedef,!isRenderingLocalContext());
                                    if (typedefs.getEnclosingClassifier() != null) {
                                        typedefs.getEnclosingClassifier().addEnclosingTypedef(typedef);
                                    }
                                }
                                if (typedefs.getEnclosingClassifier() != null && !ForwardClass.isForwardClass(typedefs.getEnclosingClassifier())) {
                                    addMember(typedefs.getEnclosingClassifier(), !isRenderingLocalContext());
                                }
                                break;
                            }
                            if (renderBitField(token)) {
                                break;
                            }
                            ClassMemberForwardDeclaration fd = renderClassForwardDeclaration(token);
                            if (fd != null) {
                                addMember(fd,!isRenderingLocalContext());
                                fd.init(token, ClassImpl.this, !isRenderingLocalContext());
                                break;
                            }
                        }
                        break;
                    case CPPTokenTypes.CSM_USING_DECLARATION: {
                        UsingDeclarationImpl using = new UsingDeclarationImpl(token, getContainingFile(), ClassImpl.this, !isRenderingLocalContext(), curentVisibility);
                        addMember(using, !isRenderingLocalContext());
                        break;
                    }
                    case CPPTokenTypes.CSM_TEMPL_FWD_CL_OR_STAT_MEM:
                        {
                            child = token.getFirstChild();
                            if (hasFriendPrefix(child)) {
                                try {
                                    addFriend(renderFriendClass(token), !isRenderingLocalContext());
                                } catch (AstRendererException e) {
                                    DiagnosticExceptoins.register(e);
                                }
                            } else {
                                ClassMemberForwardDeclaration fd = renderClassForwardDeclaration(token);
                                if (fd != null) {
                                    addMember(fd, !isRenderingLocalContext());
                                    fd.init(token, ClassImpl.this, !isRenderingLocalContext());
                                    break;
                                }
                            }
                        }
                        break;
                    case CPPTokenTypes.CSM_FUNCTION_DECLARATION:
                    case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DECLARATION:
                    case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION:
                    case CPPTokenTypes.CSM_USER_TYPE_CAST_DECLARATION:
                    case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DECLARATION:
                        child = token.getFirstChild();
                        if (child != null) {
                            if (hasFriendPrefix(child)) {
                                try {
                                    CsmScope scope = ClassImpl.this.getScope();
                                    CsmFriendFunction friend;
                                    CsmFunction func;
                                    if (isMemberDefinition(token)) {
                                        FriendFunctionImplEx impl = new FriendFunctionImplEx(token, ClassImpl.this, scope, !isRenderingLocalContext());
                                        func = impl;
                                        friend = impl;
                                    } else {
                                        FriendFunctionImpl impl = new FriendFunctionImpl(token, ClassImpl.this, scope, !isRenderingLocalContext());
                                        friend = impl;
                                        func = impl;
                                        if (scope instanceof NamespaceImpl) {
                                            ((NamespaceImpl) scope).addDeclaration(func);
                                        } else {
                                            ((NamespaceImpl) getContainingFile().getProject().getGlobalNamespace()).addDeclaration(func);
                                        }
                                    }
                                    //((FileImpl)getContainingFile()).addDeclaration(func);
                                    addFriend(friend,!isRenderingLocalContext());
                                } catch (AstRendererException e) {
                                    DiagnosticExceptoins.register(e);
                                }
                            } else {
                                try {
                                    addMember(new MethodImpl<CsmMethod>(token, ClassImpl.this, curentVisibility),!isRenderingLocalContext());
                                } catch (AstRendererException e) {
                                    DiagnosticExceptoins.register(e);
                                }
                            }
                        }
                        break;
                    case CPPTokenTypes.CSM_FUNCTION_DEFINITION:
                    case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DEFINITION:
                    case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION:
                    case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION:
                    case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DEFINITION:
                        child = token.getFirstChild();
                        if (hasFriendPrefix(child)) {
                            try {
                                CsmScope scope = ClassImpl.this.getScope();
                                CsmFriendFunction friend;
                                CsmFunction func;
                                if (isMemberDefinition(token)) {
                                    FriendFunctionDefinitionImpl impl = new FriendFunctionDefinitionImpl(token, ClassImpl.this, null, !isRenderingLocalContext());
                                    func = impl;
                                    friend = impl;
                                } else {
                                    FriendFunctionDDImpl impl = new FriendFunctionDDImpl(token, ClassImpl.this, scope, !isRenderingLocalContext());
                                    friend = impl;
                                    func = impl;
                                    if (scope instanceof NamespaceImpl) {
                                        ((NamespaceImpl) scope).addDeclaration(func);
                                    } else {
                                        ((NamespaceImpl) getContainingFile().getProject().getGlobalNamespace()).addDeclaration(func);
                                    }
                                }
                                //((FileImpl)getContainingFile()).addDeclaration(func);
                                addFriend(friend,!isRenderingLocalContext());
                            } catch (AstRendererException e) {
                                DiagnosticExceptoins.register(e);
                            }
                        } else {
                            try {
                                addMember(new MethodDDImpl<CsmMethod>(token, ClassImpl.this, curentVisibility, !isRenderingLocalContext(),!isRenderingLocalContext()),!isRenderingLocalContext());
                            } catch (AstRendererException e) {
                                DiagnosticExceptoins.register(e);
                            }
                        }
                        break;
                    case CPPTokenTypes.CSM_VISIBILITY_REDEF:
                        UsingDeclarationImpl using = new UsingDeclarationImpl(token, getContainingFile(), ClassImpl.this, !isRenderingLocalContext(), curentVisibility);
                        addMember(using, !isRenderingLocalContext());
                        break;
                    case CPPTokenTypes.RCURLY:
                        break;
                    case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                    case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                        //new VariableImpl(
                        break;
                }
            }
        }

        private void setTemplateDescriptor(List<CsmTemplateParameter> params, String name) {
            templateDescriptor = new TemplateDescriptor(params, name, !isRenderingLocalContext());
        }

        private boolean hasFriendPrefix(AST child) {
            if (child == null) {
                return false;
            }
            if (child.getType() == CPPTokenTypes.LITERAL_friend) {
                return true;
            } else if (child.getType() == CPPTokenTypes.LITERAL_template) {
                final AST nextSibling = child.getNextSibling();
                if (nextSibling != null && nextSibling.getType() == CPPTokenTypes.LITERAL_friend) {
                    // friend template declaration
                    return true;
                }
            }
            return false;
        }

        private CsmFriend renderFriendClass(AST ast) throws AstRendererException {
            AST firstChild = ast.getFirstChild();
            AST child = firstChild;
            if (child.getType() == CPPTokenTypes.LITERAL_friend) {
                child = child.getNextSibling();
            }
            if (child != null && child.getType() == CPPTokenTypes.LITERAL_template) {
                child = child.getNextSibling();
            }
            CsmClassForwardDeclaration cfd = null;
            AST qid = null;
            if (child != null &&
                    (child.getType() == CPPTokenTypes.LITERAL_struct ||
                    child.getType() == CPPTokenTypes.LITERAL_class)) {
                // check if we want to have class forward
                // we don't want for AA::BB names
                qid = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
                CharSequence[] nameParts = AstRenderer.getNameTokens(qid);
                if (nameParts != null && nameParts.length == 1) {
                    // also we don't want for templates references
                    AST templStart = TemplateUtils.getTemplateStart(ast.getFirstChild());
                    if (templStart == null) {
                        CsmScope scope = ClassImpl.this.getScope();
                        while (!CsmKindUtilities.isNamespace(scope) && CsmKindUtilities.isScopeElement(scope)) {
                            scope = ((CsmScopeElement)scope).getScope();
                        }
                        if (!CsmKindUtilities.isNamespace(scope)) {
                            scope = getContainingFile().getProject().getGlobalNamespace();
                        }
                        cfd = super.createForwardClassDeclaration(ast, null, (FileImpl) getContainingFile(), scope);
                        if (true) { // always put in repository, because it's an element of global NS
                            RepositoryUtils.put(cfd);
                        }
                        ((NamespaceImpl) scope).addDeclaration(cfd);
                    }
                }
            } else if (child != null && (child.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND)) {
                //class G;
                //class X {
                //  friend G;
                //};
                qid = child;
            } else {
                // FIXME: is it valid or exceptional branch?
                qid = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
            }
            return new FriendClassImpl(firstChild, qid, cfd, (FileImpl) getContainingFile(), ClassImpl.this, !isRenderingLocalContext());
        }

        private ClassMemberForwardDeclaration renderClassForwardDeclaration(AST token) {
            AST typeAST = token.getFirstChild();
            if (typeAST == null) {
                return null;
            }
            if (typeAST.getType() == CPPTokenTypes.LITERAL_template) {
                typeAST = typeAST.getNextSibling();
            }
            if (typeAST == null ||
                    (typeAST.getType() != CPPTokenTypes.LITERAL_struct &&
                    typeAST.getType() != CPPTokenTypes.LITERAL_class)) {
                return null;
            }
            AST idAST = typeAST.getNextSibling();
            if (idAST == null || idAST.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) {
                return null;
            }
            return new ClassMemberForwardDeclaration(ClassImpl.this, token, curentVisibility, !isRenderingLocalContext());
        }

        private boolean renderBitField(AST token) {

            AST typeAST = token.getFirstChild();
            if (typeAST == null ||
                    (typeAST.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN &&
                    typeAST.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND)) {
                return false;
            }

            // common type for all bit fields
            CsmType type = TypeFactory.createType(typeAST, getContainingFile(), null, 0);

            boolean cont = true;
            boolean added = false;
            AST start = token;
            AST prev = typeAST;
            while (cont) {
                AST idAST = prev.getNextSibling();
                if (idAST == null || idAST.getType() != CPPTokenTypes.ID) {
                    break;
                }

                AST colonAST = idAST.getNextSibling();
                if (colonAST == null || colonAST.getType() != CPPTokenTypes.COLON) {
                    break;
                }

                AST expAST = colonAST.getNextSibling();
                if (expAST == null || expAST.getType() != CPPTokenTypes.CSM_EXPRESSION) {
                    break;
                }
                prev = expAST.getNextSibling();

                // there could be next bit fields as well
                if (prev != null && prev.getType() == CPPTokenTypes.COMMA) {
                    // bit fields separated by comma
                    // byte f:1, g:2, h:5;
                    start = idAST;
                } else {
                    cont = false;
                    if (added) {
                        start = idAST;
                    }
                }
                FieldImpl field = new FieldImpl(start, getContainingFile(), type, idAST.getText(), ClassImpl.this, curentVisibility, !isRenderingLocalContext());
                ClassImpl.this.addMember(field,!isRenderingLocalContext());
                added = true;
            }
            return added;
        }

        @Override
        protected CsmTypedef createTypedef(AST ast, FileImpl file, CsmObject container, CsmType type, String name) {
            type = TemplateUtils.checkTemplateType(type, ClassImpl.this);
            return new MemberTypedef(ClassImpl.this, ast, type, name, curentVisibility, !isRenderingLocalContext());
        }

        @Override
        protected CsmClassForwardDeclaration createForwardClassDeclaration(AST ast, MutableDeclarationsContainer container, FileImpl file, CsmScope scope) {
            ClassMemberForwardDeclaration fd = new ClassMemberForwardDeclaration(ClassImpl.this, ast, curentVisibility, !isRenderingLocalContext());
            addMember(fd,!isRenderingLocalContext());
            fd.init(ast, ClassImpl.this, !isRenderingLocalContext());
            return fd;
        }
    }

    public static class MemberTypedef extends TypedefImpl implements CsmMember {

        private CsmVisibility visibility;

        public MemberTypedef(CsmClass containingClass, AST ast, CsmType type, String name, CsmVisibility curentVisibility, boolean global) {
            super(ast, containingClass.getContainingFile(), containingClass, type, name, global);
            visibility = curentVisibility;
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public CsmVisibility getVisibility() {
            return visibility;
        }

        @Override
        public CsmClass getContainingClass() {
            return (CsmClass) getScope();
        }

        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        @Override
        public void write(DataOutput output) throws IOException {
            super.write(output);
            assert this.visibility != null;
            PersistentUtils.writeVisibility(this.visibility, output);
        }

        public MemberTypedef(DataInput input) throws IOException {
            super(input);
            this.visibility = PersistentUtils.readVisibility(input);
            assert this.visibility != null;
        }
    }

    public static class ClassMemberForwardDeclaration extends ClassForwardDeclarationImpl
            implements CsmMember, CsmClassifier {

        private CsmVisibility visibility;
        private CsmUID<CsmClass> classDefinition;
        private final CsmUID<CsmClass> containerUID;
        private CsmClass containerRef;

        public ClassMemberForwardDeclaration(CsmClass containingClass, AST ast, CsmVisibility curentVisibility, boolean register) {
            super(ast, containingClass.getContainingFile(), register);
            visibility = curentVisibility;
            containerUID = UIDCsmConverter.declarationToUID(containingClass);
            if (register) {
                registerInProject();
            }
        }

        protected final void registerInProject() {
            CsmProject project = getContainingFile().getProject();
            if (project instanceof ProjectBase) {
                ((ProjectBase) project).registerDeclaration(this);
            }
        }

        private void unregisterInProject() {
            CsmProject project = getContainingFile().getProject();
            if (project instanceof ProjectBase) {
                ((ProjectBase) project).unregisterDeclaration(this);
                this.cleanUID();
            }
        }

        @Override
        public void dispose() {
            super.dispose();
            onDispose();
            CsmScope scope = getScope();
            if (scope instanceof MutableDeclarationsContainer) {
                ((MutableDeclarationsContainer) scope).removeDeclaration(this);
            }
            unregisterInProject();
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public CsmVisibility getVisibility() {
            return visibility;
        }

        private void onDispose() {
            if (containerRef == null) {
                containerRef = UIDCsmConverter.UIDtoClass(containerUID);
            }
        }

        @Override
        public CsmClass getContainingClass() {
            CsmClass out = containerRef;
            if (out == null) {
                out = containerRef = UIDCsmConverter.UIDtoClass(containerUID);
            }
            return out;
        }

        @Override
        public CsmScope getScope() {
            return getContainingClass();
        }

        @Override
        public CsmClass getCsmClass() {
            CsmClass cls = UIDCsmConverter.UIDtoClass(classDefinition);
            // we need to replace i.e. ForwardClass stub
            if (cls != null && cls.isValid() && !ForwardClass.isForwardClass(cls)) {
                return cls;
            } else {
                cls = super.getCsmClass();
                setCsmClass(cls);
            }
            return cls;
        }

        @Override
        protected CsmClass createForwardClassIfNeed(AST ast, CsmScope scope, boolean registerInProject) {
            CsmClass cls = super.createForwardClassIfNeed(ast, scope, registerInProject);
            classDefinition = UIDCsmConverter.declarationToUID(cls);
            if (cls != null) {
                RepositoryUtils.put(this);
            }
            return cls;
        }

        public void setCsmClass(CsmClass cls) {
            classDefinition = UIDCsmConverter.declarationToUID(cls);
        }

        @Override
        public CharSequence getQualifiedName() {
            CsmClass cls = getContainingClass();
            if (cls == null) {
                cls = getContainingClass();
            }
            return CharSequenceKey.create(cls.getQualifiedName() + "::" + getName()); // NOI18N
        }

        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        @Override
        public void write(DataOutput output) throws IOException {
            super.write(output);
            assert visibility != null;
            PersistentUtils.writeVisibility(visibility, output);
            assert containerUID != null;
            UIDObjectFactory.getDefaultFactory().writeUID(containerUID, output);
            UIDObjectFactory.getDefaultFactory().writeUID(classDefinition, output);
        }

        public ClassMemberForwardDeclaration(DataInput input) throws IOException {
            super(input);
            visibility = PersistentUtils.readVisibility(input);
            assert visibility != null;
            containerUID = UIDObjectFactory.getDefaultFactory().readUID(input);
            assert containerUID != null;
            classDefinition = UIDObjectFactory.getDefaultFactory().readUID(input);
        }
    }

//    public ClassImpl(CsmDeclaration.Kind kind, String name, NamespaceImpl namespace, CsmFile file) {
//        this(kind, name, namespace, file, null);
//    }
//
//    public ClassImpl(CsmDeclaration.Kind kind, String name, NamespaceImpl namespace, CsmFile file, CsmClass containingClass) {
//        super(name, namespace, file, containingClass, null);
//        leftBracketPos = 0;
//        this.kind = CsmDeclaration.Kind.CLASS;
//        register();
//    }
    protected ClassImpl(String name, AST ast, CsmFile file) {
        // we call findId(..., true) because there might be qualified name - in the case of nested class template specializations
        super((name != null ? name : AstUtil.findId(ast, CPPTokenTypes.RCURLY, true)), file, ast);
        members = new ArrayList<CsmUID<CsmMember>>();
        friends = new ArrayList<CsmUID<CsmFriend>>(0);
        kind = findKind(ast);
    }

    protected void init(CsmScope scope, AST ast, boolean register) {
        initScope(scope, ast);
        initQualifiedName(scope, ast);
        if (register) {
            RepositoryUtils.hang(this); // "hang" now and then "put" in "register()"
        } else {
            Utils.setSelfUID(this);
        }
        render(ast, !register);
        leftBracketPos = initLeftBracketPos(ast);
        if (register) {
            register(getScope(), false);
        }
    }

    protected final void render(AST ast, boolean localClass) {
        new ClassAstRenderer(localClass).render(ast);
        leftBracketPos = initLeftBracketPos(ast);
    }

    protected static ClassImpl findExistingClassImplInContainer(DeclarationsContainer container, AST ast) {
        ClassImpl out = null;
        if (container != null) {
            CharSequence name = CharSequenceKey.create(AstUtil.findId(ast, CPPTokenTypes.RCURLY, true));
            name = (name == null) ? CharSequenceKey.empty() : name;
            int start = getStartOffset(ast);
            int end = getEndOffset(ast);
            CsmOffsetableDeclaration existing = container.findExistingDeclaration(start, end, name);
            if (existing instanceof ClassImpl) {
                out = (ClassImpl) existing;
//                System.err.printf("found existing %s in %s\n", existing, container); // NOI18N
//            } else {
//                System.err.printf("not found %s [%d-%d] in %s\n", name, start, end, container); // NOI18N
            }
        }
        return out;
    }
    
    public static ClassImpl create(AST ast, CsmScope scope, CsmFile file, boolean register, DeclarationsContainer container) {
        ClassImpl impl = findExistingClassImplInContainer(container, ast);
        if (impl != null && !(ClassImpl.class.equals(impl.getClass()))) {
            // not our instance
            impl = null;
        }
        if (impl == null) {
            impl = new ClassImpl(null, ast, file);
        }
        impl.init(scope, ast, register);
        return impl;
    }

    protected void setTemplateDescriptor(TemplateDescriptor td) {
        templateDescriptor = td;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return this.kind;
    }

    @Override
    public Collection<CsmMember> getMembers() {
        Collection<CsmMember> out;
        synchronized (members) {
            out = UIDCsmConverter.UIDsToDeclarations(members);
        }
        return out;
    }

    @Override
    public Iterator<CsmMember> getMembers(CsmFilter filter) {
        Collection<CsmUID<CsmMember>> uids = new ArrayList<CsmUID<CsmMember>>();
        synchronized (members) {
            uids.addAll(members);
        }
        return UIDCsmConverter.UIDsToDeclarations(uids, filter);
    }

    @Override
    public Collection<CsmFriend> getFriends() {
        Collection<CsmFriend> out;
        synchronized (friends) {
            out = UIDCsmConverter.UIDsToDeclarations(friends);
        }
        return out;
    }

    @Override
    public List<CsmInheritance> getBaseClasses() {
        synchronized (inheritances) {
            return new ArrayList<CsmInheritance>(inheritances);
        }
    }

    @Override
    public boolean isTemplate() {
        return templateDescriptor != null;
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int start, int end, CharSequence name) {
        CsmUID<? extends CsmOffsetableDeclaration> out = null;
        synchronized (members) {
            out = UIDUtilities.findExistingUIDInList(members, start, end, name);
//            if (FileImpl.traceFile(getContainingFile().getAbsolutePath())) {
//                System.err.printf("%s found %s [%d-%d] in \n\t%s\n", (out == null) ? "NOT " : "", name, start, end, members);
//            }
        }
        if (out == null) {
            // check friends
            synchronized (friends) {
                out = UIDUtilities.findExistingUIDInList(friends, start, end, name);
            }
        }
        return UIDCsmConverter.UIDtoDeclaration(out);
    }

    private void addMember(CsmMember member, boolean global) {
        if (global) {
            RepositoryUtils.put(member);
        }
        CsmUID<CsmMember> uid = UIDCsmConverter.declarationToUID(member);
        assert uid != null;
        synchronized (members) {
//            members.add(uid);
            UIDUtilities.insertIntoSortedUIDList(uid, members);
        }
    }

    private void addInheritance(CsmInheritance inheritance) {
        synchronized (inheritances) {
            if (!inheritances.contains(inheritance)) {
                inheritances.add(inheritance);
            }
        }
    }

    private void addFriend(CsmFriend friend, boolean global) {
        if (global) {
            RepositoryUtils.put(friend);
        }
        CsmUID<CsmFriend> uid = UIDCsmConverter.declarationToUID(friend);
        assert uid != null;
        synchronized (friends) {
//            friends.add(uid);
            UIDUtilities.insertIntoSortedUIDList(uid, friends);
        }
    }

    private int initLeftBracketPos(AST node) {
        AST lcurly = AstUtil.findChildOfType(node, CPPTokenTypes.LCURLY);
        return (lcurly instanceof CsmAST) ? ((CsmAST) lcurly).getOffset() : getStartOffset();
    }

    @Override
    public int getLeftBracketOffset() {
        return leftBracketPos;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return (Collection<CsmScopeElement>) (Collection<?>) getMembers();
    }

    @Override
    public void dispose() {
        super.dispose();
        _clearMembers();
        _clearFriends();
    }

    private void _clearMembers() {
        Collection<CsmMember> members2dispose = getMembers();
        Utils.disposeAll(members2dispose);
        synchronized (members) {
            RepositoryUtils.remove(this.members);
        }
    }

    private void _clearFriends() {
        Collection<CsmFriend> friends2dispose = getFriends();
        Utils.disposeAll(friends2dispose);
        synchronized (friends) {
            RepositoryUtils.remove(this.friends);
        }
    }

    private CsmDeclaration.Kind findKind(AST ast) {
        for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
            switch (token.getType()) {
                // class / struct / union
                case CPPTokenTypes.LITERAL_class:
                    return CsmDeclaration.Kind.CLASS;
                case CPPTokenTypes.LITERAL_union:
                    return CsmDeclaration.Kind.UNION;
                case CPPTokenTypes.LITERAL_struct:
                    return CsmDeclaration.Kind.STRUCT;
            }
        }
        return CsmDeclaration.Kind.CLASS;
    }

    @Override
    public CharSequence getDisplayName() {
        return (templateDescriptor != null) ? CharSequenceKey.create((getName().toString() + templateDescriptor.getTemplateSuffix())) : getName(); // NOI18N
    }

    @Override
    public List<CsmTemplateParameter> getTemplateParameters() {
        return (templateDescriptor != null) ? templateDescriptor.getTemplateParameters() : Collections.<CsmTemplateParameter>emptyList();
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.kind != null;
        writeKind(this.kind, output);
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
        output.writeInt(this.leftBracketPos);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.members, output, true);
        factory.writeUIDCollection(this.friends, output, true);
        Collection<CsmInheritance> baseClasses = getBaseClasses();
        PersistentUtils.writeInheritances(baseClasses, output);
    }

    public ClassImpl(DataInput input) throws IOException {
        super(input);
        this.kind = readKind(input);
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
        this.leftBracketPos = input.readInt();
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        int collSize = input.readInt();
        if (collSize <= 0) {
            members = new ArrayList<CsmUID<CsmMember>>(0);
        } else {
            members = new ArrayList<CsmUID<CsmMember>>(collSize);
        }
        factory.readUIDCollection(this.members, input, collSize);
        collSize = input.readInt();
        if (collSize <= 0) {
            friends = new ArrayList<CsmUID<CsmFriend>>(0);
        } else {
            friends = new ArrayList<CsmUID<CsmFriend>>(collSize);
        }
        factory.readUIDCollection(this.friends, input, collSize);
        Collection<CsmInheritance> baseClasses = new ArrayList<CsmInheritance>();
        PersistentUtils.readInheritances(baseClasses, input);
        synchronized (this.inheritances) {
            this.inheritances.clear();
            this.inheritances.addAll(baseClasses);
            inheritances.trimToSize();
        }
    }
    private static final int CLASS_KIND = 1;
    private static final int UNION_KIND = 2;
    private static final int STRUCT_KIND = 3;

    private static void writeKind(CsmDeclaration.Kind kind, DataOutput output) throws IOException {
        int kindHandler;
        if (kind == CsmDeclaration.Kind.CLASS) {
            kindHandler = CLASS_KIND;
        } else if (kind == CsmDeclaration.Kind.UNION) {
            kindHandler = UNION_KIND;
        } else {
            assert kind == CsmDeclaration.Kind.STRUCT;
            kindHandler = STRUCT_KIND;
        }
        output.writeByte(kindHandler);
    }

    private static CsmDeclaration.Kind readKind(DataInput input) throws IOException {
        int kindHandler = input.readByte();
        CsmDeclaration.Kind kind;
        switch (kindHandler) {
            case CLASS_KIND:
                kind = CsmDeclaration.Kind.CLASS;
                break;
            case UNION_KIND:
                kind = CsmDeclaration.Kind.UNION;
                break;
            case STRUCT_KIND:
                kind = CsmDeclaration.Kind.STRUCT;
                break;
            default:
                throw new IllegalArgumentException("illegal handler " + kindHandler); // NOI18N
        }
        return kind;
    }
}

