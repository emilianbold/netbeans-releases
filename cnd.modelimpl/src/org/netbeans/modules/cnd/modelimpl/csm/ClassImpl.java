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

import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl.EnumBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.FieldImpl.FieldBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.MethodImpl.MethodBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDeclarationImpl.UsingDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.SelectImpl;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Implements CsmClass
 * @author Vladimir Kvashin
 */
public class ClassImpl extends ClassEnumBase<CsmClass> implements CsmClass, CsmTemplate, SelectImpl.FilterableMembers,
        DeclarationsContainer {

    private final CsmDeclaration.Kind kind;
    private final List<CsmUID<CsmMember>> members;
    private final List<CsmUID<CsmFriend>> friends;
    private final ArrayList<CsmUID<CsmInheritance>> inheritances;
    private TemplateDescriptor templateDescriptor = null;
    private /*final*/ int leftBracketPos;

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
    protected ClassImpl(NameHolder name, AST ast, CsmFile file) {
        // we call findId(..., true) because there might be qualified name - in the case of nested class template specializations
        this(name, ast, file, getStartOffset(ast), getEndOffset(ast));
    }

    protected ClassImpl(NameHolder name, AST ast, CsmFile file, int start, int end) {
        // we call findId(..., true) because there might be qualified name - in the case of nested class template specializations
        super(name, file, start, end);
        members = new ArrayList<CsmUID<CsmMember>>();
        friends = new ArrayList<CsmUID<CsmFriend>>(0);
        inheritances = new ArrayList<CsmUID<CsmInheritance>>(0);
        kind = findKind(ast);
    }

    private ClassImpl(NameHolder name, CsmDeclaration.Kind kind, CsmFile file, int startOffset, int endOffset) {
        super(name, file, startOffset, endOffset);
        members = new ArrayList<CsmUID<CsmMember>>();
        friends = new ArrayList<CsmUID<CsmFriend>>(0);
        inheritances = new ArrayList<CsmUID<CsmInheritance>>(0);
        this.kind = kind;
    }
    
    private ClassImpl(CsmFile file, CsmScope scope, String name, CsmDeclaration.Kind kind, int startOffset, int endOffset) {
        super(name, name, file, startOffset, endOffset);
        members = new ArrayList<CsmUID<CsmMember>>();
        friends = new ArrayList<CsmUID<CsmFriend>>(0);
        inheritances = new ArrayList<CsmUID<CsmInheritance>>(0);
        this.kind = kind;
        initScope(scope);
    }

    public static ClassImpl create(CsmFile file, CsmScope scope, String name, CsmDeclaration.Kind kind, int startOffset, int endOffset, boolean register) {
        ClassImpl classImpl = new ClassImpl(file, scope, name, kind, startOffset, endOffset);
        temporaryRepositoryRegistration(register, classImpl);
        if (register) {
            classImpl.register(classImpl.getScope(), false);
        }
        return classImpl;
    }

    public void init(CsmScope scope, AST ast, CsmFile file, FileContent fileContent, boolean register) throws AstRendererException {
        initScope(scope);
        temporaryRepositoryRegistration(register, this);
        initClassDefinition(scope);
        render(ast, file, fileContent, !register);
        if (register) {
            register(getScope(), false);
        }
    }
//
//    public void init2(CsmScope scope, AST ast, boolean register) {
//        initScope(scope);
////        temporaryRepositoryRegistration(register, this);
//        initClassDefinition(scope);
//        render(ast, !register);
//        if (register) {
//            register(getScope(), false);
//        }
//    }
    
    public void init3(CsmScope scope, boolean register) {
        initScope(scope);
        temporaryRepositoryRegistration(register, this);
        initClassDefinition(scope);
        if (register) {
            register(getScope(), false);
        }
    }
    
    private void initClassDefinition(CsmScope scope) {
        ClassImpl.MemberForwardDeclaration mfd = findMemberForwardDeclaration(scope);
        if (mfd instanceof ClassImpl.ClassMemberForwardDeclaration && CsmKindUtilities.isClass(this)) {
            ClassImpl.ClassMemberForwardDeclaration fd = (ClassImpl.ClassMemberForwardDeclaration) mfd;
            fd.setCsmClass((CsmClass) this);
            CsmClass containingClass = fd.getContainingClass();
            if (containingClass != null) {
                // this is our real scope, not current namespace
                initScope(containingClass);
            }
        }
    }

    public final void render(AST ast, CsmFile file, FileContent fileContent, boolean localClass) {
        new ClassAstRenderer(file, fileContent, CsmVisibility.PRIVATE, localClass).render(ast);
        leftBracketPos = initLeftBracketPos(ast);
    }

    public final void fixFakeRender(FileContent fileContent, CsmVisibility visibility, AST ast, boolean localClass) {
        new ClassAstRenderer(fileContent.getFile(), fileContent, visibility, localClass).render(ast);
    }

    protected static ClassImpl findExistingClassImplInContainer(DeclarationsContainer container, AST ast) {
        ClassImpl out = null;
        if (container != null) {
            CharSequence name = CharSequences.create(AstUtil.findId(ast, CPPTokenTypes.RCURLY, true));
            name = (name == null) ? CharSequences.empty() : name;
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
    
    public static ClassImpl create(AST ast, CsmScope scope, CsmFile file, FileContent fileContent, boolean register, DeclarationsContainer container) throws AstRendererException {
        ClassImpl impl = findExistingClassImplInContainer(container, ast);
        if (impl != null && !(ClassImpl.class.equals(impl.getClass()))) {
            // not our instance
            impl = null;
        }
        NameHolder nameHolder = null;
        if (impl == null) {
            nameHolder = NameHolder.createClassName(ast);
            impl = new ClassImpl(nameHolder, ast, file);
        }
        // fix for Bug 215225 - Infinite loop in TemplateUtils.checkTemplateType
        if(scope != null && scope instanceof ClassImpl) {
            ClassImpl scopeCls = (ClassImpl)scope;
            if(impl.getStartOffset() == scopeCls.getStartOffset() &&
                    impl.getEndOffset() == scopeCls.getEndOffset() &&
                    impl.getKind().equals(scopeCls.getKind()) &&
                    impl.getName().equals(scopeCls.getName())) {
                return null;
            }
        }
        impl.init(scope, ast, file, fileContent, register);
        if (nameHolder != null) {
            nameHolder.addReference(fileContent, impl);
        }
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
    public Collection<CsmInheritance> getBaseClasses() {
        Collection<CsmInheritance> out;
        synchronized (inheritances) {
            out = UIDCsmConverter.UIDsToInheritances(inheritances);
        }
        return out;
    }

    @Override
    public boolean isTemplate() {
        return templateDescriptor != null;
    }

    @Override
    public boolean isSpecialization() {
        return false;
    }
    
    @Override
    public boolean isExplicitSpecialization() {
        return false;
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

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int start, CharSequence name, CsmDeclaration.Kind kind) {
        CsmUID<? extends CsmOffsetableDeclaration> out = null;
        if(kind != CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION &&
            kind != CsmDeclaration.Kind.FUNCTION_FRIEND &&
            kind != CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
            synchronized (members) {
                out = UIDUtilities.findExistingUIDInList(members, start, name, kind);
            }
        } else {
            // check friends
            synchronized (friends) {
                out = UIDUtilities.findExistingUIDInList(friends, start, name, kind);
            }
        }
        return UIDCsmConverter.UIDtoDeclaration(out);
    }

    protected void addMember(CsmMember member, boolean global) {
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

    private void addInheritance(CsmInheritance inheritance, boolean global) {
        if (global) {
            RepositoryUtils.put(inheritance);
        }
        CsmUID<CsmInheritance> uid = UIDCsmConverter.inheritanceToUID(inheritance);
        assert uid != null;
        synchronized (inheritances) {
            UIDUtilities.insertIntoSortedUIDList(uid, inheritances);
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
        _clearInheritances();
    }

    private void _clearMembers() {
        Collection<CsmMember> members2dispose = getMembers();
        Utils.disposeAll(members2dispose);
        synchronized (members) {
            RepositoryUtils.remove(this.members);
        }
    }

    private void _clearInheritances() {
        Collection<CsmInheritance> inheritances2dispose = getBaseClasses();
        Utils.disposeAll(inheritances2dispose);
        synchronized (inheritances) {
            RepositoryUtils.remove(this.inheritances);
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
        return (templateDescriptor != null) ? CharSequences.create((getName().toString() + templateDescriptor.getTemplateSuffix())) : getName(); // NOI18N
    }

    @Override
    public List<CsmTemplateParameter> getTemplateParameters() {
        return (templateDescriptor != null) ? templateDescriptor.getTemplateParameters() : Collections.<CsmTemplateParameter>emptyList();
    }
    
    
    public static class ClassBuilder implements CsmObjectBuilder {
        
        private CharSequence name;// = CharSequences.empty();
        private int nameStartOffset;
        private int nameEndOffset;
        private CsmDeclaration.Kind kind = CsmDeclaration.Kind.CLASS;
        private CsmFile file;
        private final FileContent fileContent;
        private int startOffset = 0;
        private int endOffset = 0;
        private CsmObjectBuilder parent;

        private NamespaceImpl namespace;
        private CsmScope scope;
        private ClassImpl instance;
        private NameHolder nameHolder;
        
        private List<CsmObjectBuilder> children = new ArrayList<CsmObjectBuilder>();

        public ClassBuilder(FileContent fileContent) {
            assert fileContent != null;
            this.fileContent = fileContent;
        }
        
        public void setKind(Kind kind) {
            this.kind = kind;
        }
        
        public void setName(CharSequence name, int startOffset, int endOffset) {
            if(this.name == null) {
                this.name = name;
                this.nameStartOffset = startOffset;
                this.nameEndOffset = endOffset;
            }
        }

        public CharSequence getName() {
            return name;
        }
        
        public void setFile(CsmFile file) {
            this.file = file;
        }
        
        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }

        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }

        public void setParent(CsmObjectBuilder parent) {
            this.parent = parent;
        }

        public void addChild(CsmObjectBuilder builder) {
            this.children.add(builder);
        }
        
        public void addMember(CsmMember member) {
            this.instance.addMember(member, true);
        }        
        
        public ClassImpl getClassDefinitionInstance() {
            if(instance != null) {
                return instance;
            }
            MutableDeclarationsContainer container = null;
            if (parent == null) {
                container = fileContent;
            } else {
                if(parent instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                    container = ((NamespaceDefinitionImpl.NamespaceBuilder)parent).getNamespaceDefinitionInstance();
                }
            }
            if(container != null && name != null) {
                CsmOffsetableDeclaration decl = container.findExistingDeclaration(startOffset, name, kind);
                if (decl != null && ClassImpl.class.equals(decl.getClass())) {
                    instance = (ClassImpl) decl;
                }
            }
            return instance;
        }
        
        public CsmScope getScope() {
            if(scope != null) {
                return scope;
            }
            if (parent == null) {
                scope = (NamespaceImpl) file.getProject().getGlobalNamespace();
            } else {
                if(parent instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                    scope = ((NamespaceDefinitionImpl.NamespaceBuilder)parent).getNamespace();
                }
            }
            return scope;
        }

        public void setScope(CsmScope scope) {
            assert scope != null;
            this.scope = scope;
        }
        
        public ClassImpl create() {
            ClassImpl cls = getClassDefinitionInstance();
            CsmScope s = getScope();
            if (cls == null && s != null && name != null && endOffset != 0) {
                nameHolder = NameHolder.createName(name, nameStartOffset, nameEndOffset);
                cls = new ClassImpl(nameHolder, kind, file, startOffset, endOffset);
                cls.init3(s, true);
                instance = cls;
                if (nameHolder != null) {
                    nameHolder.addReference(fileContent, cls);
                }                
                if(parent != null) {
                    if(parent instanceof ClassBuilder) {
                        ((ClassBuilder)parent).getClassDefinitionInstance().addMember(cls, true);
                    } else if(parent instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                        ((NamespaceDefinitionImpl.NamespaceBuilder)parent).addDeclaration(cls);
                    }
                } else {
                    fileContent.addDeclaration(cls);
                }
                for (CsmObjectBuilder builder : children) {
                    if(builder instanceof ClassBuilder) {
                        ((ClassBuilder)builder).setScope(cls);
                        ((ClassBuilder)builder).create();
                    }
                    if(builder instanceof EnumBuilder) {
                        ((EnumBuilder)builder).setScope(cls);
                        ((EnumBuilder)builder).create(true);
                    }
                    if(builder instanceof UsingDeclarationBuilder) {
                        ((UsingDeclarationBuilder)builder).setScope(cls);
                        ((UsingDeclarationBuilder)builder).create();
                    }
                    if(builder instanceof FieldBuilder) {
                        ((FieldBuilder)builder).setScope(cls);
                        ((FieldBuilder)builder).create();
                    }
                    if(builder instanceof MethodBuilder) {
                        ((MethodBuilder)builder).setScope(cls);
                        ((MethodBuilder)builder).create();
                    }
                }                
            }
            return cls;
        }
        
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.kind != null;
        writeKind(this.kind, output);
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
        output.writeInt(this.leftBracketPos);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.members, output, true);
        factory.writeUIDCollection(this.friends, output, true);
        factory.writeUIDCollection(this.inheritances, output, true);
    }

    public ClassImpl(RepositoryDataInput input) throws IOException {
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

        collSize = input.readInt();
        if (collSize <= 0) {
            inheritances = new ArrayList<CsmUID<CsmInheritance>>(0);
        } else {
            inheritances = new ArrayList<CsmUID<CsmInheritance>>(collSize);
        }
        factory.readUIDCollection(this.inheritances, input, collSize);
    }
    private static final int CLASS_KIND = 1;
    private static final int UNION_KIND = 2;
    private static final int STRUCT_KIND = 3;

    private static void writeKind(CsmDeclaration.Kind kind, RepositoryDataOutput output) throws IOException {
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

    private static CsmDeclaration.Kind readKind(RepositoryDataInput input) throws IOException {
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

    private class ClassAstRenderer extends AstRenderer {
        private final boolean renderingLocalContext;
        private CsmVisibility curentVisibility;

        public ClassAstRenderer(CsmFile containingFile, FileContent fileContent, CsmVisibility curentVisibility, boolean renderingLocalContext) {
            super((FileImpl) containingFile, fileContent, null);
            this.renderingLocalContext = renderingLocalContext;
            this.curentVisibility = curentVisibility;
        }

        @Override
        protected boolean isRenderingLocalContext() {
            return renderingLocalContext;
        }

        @Override
        protected VariableImpl<CsmField> createVariable(AST offsetAst, CsmFile file, CsmType type, NameHolder name, boolean _static, boolean _extern,
                MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope) {
            type = TemplateUtils.checkTemplateType(type, ClassImpl.this);
            FieldImpl field = FieldImpl.create(offsetAst, file, fileContent, type, name, ClassImpl.this, curentVisibility, _static, _extern, !isRenderingLocalContext());
            ClassImpl.this.addMember(field,!isRenderingLocalContext());
            return field;
        }

        @Override
        public void render(AST ast) {
            Pair typedefs;
            AST child;
            for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                try {
                    switch (token.getType()) {
                        //case CPPTokenTypes.CSM_TEMPLATE_PARMLIST:
                        case CPPTokenTypes.LITERAL_template:{
                            List<CsmTemplateParameter> params = TemplateUtils.getTemplateParameters(token, getContainingFile(), ClassImpl.this, !isRenderingLocalContext());
                            final String classSpecializationSuffix = TemplateUtils.getClassSpecializationSuffix(token, null);
                            String name = "<" + classSpecializationSuffix + ">"; // NOI18N
                            setTemplateDescriptor(params, name, !classSpecializationSuffix.isEmpty());
                            break;
                        }
                        case CPPTokenTypes.CSM_BASE_SPECIFIER:
                            addInheritance(InheritanceImpl.create(token, getContainingFile(), ClassImpl.this, !isRenderingLocalContext()), !isRenderingLocalContext());
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
                                    ? ClassImplSpecialization.create(token, ClassImpl.this, getContainingFile(), getFileContent(), !isRenderingLocalContext(), ClassImpl.this)
                                    : ClassImpl.create(token, ClassImpl.this, getContainingFile(), getFileContent(), !isRenderingLocalContext(), ClassImpl.this);

                            boolean created = false; 
                            if(TraceFlags.CPP_PARSER_ACTION) {
                                for (CsmMember member : ClassImpl.this.getMembers()) {
                                    if(CsmKindUtilities.isClass(member) && member.getStartOffset() == innerClass.getStartOffset()) {
                                        innerClass = (ClassImpl)member;
                                        created = true;
                                        break;
                                    }
                                }
                            }
                            if(innerClass != null) {
                                innerClass.setVisibility(curentVisibility);
                                if(!created) {
                                    addMember(innerClass,!isRenderingLocalContext());
                                }
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
                            }
                            break;
                        case CPPTokenTypes.CSM_ENUM_DECLARATION:
                            EnumImpl innerEnum = EnumImpl.create(token, ClassImpl.this, getContainingFile(), fileContent, !isRenderingLocalContext());
                            boolean enumCreated = false; 
                            if(TraceFlags.CPP_PARSER_ACTION) {
                                for (CsmMember member : ClassImpl.this.getMembers()) {
                                    if(CsmKindUtilities.isEnum(member) && member.getStartOffset() == innerEnum.getStartOffset()) {
                                        innerEnum = (EnumImpl)member;
                                        enumCreated = true;
                                        break;
                                    }
                                }
                            }
                            innerEnum.setVisibility(curentVisibility);
                            if(!enumCreated) {
                                addMember(innerEnum,!isRenderingLocalContext());
                            }
                            renderVariableInClassifier(token, innerEnum, null, null);
                            checkInnerIncludes(innerEnum, Collections.<CsmObject>emptyList());
                            break;

                        case CPPTokenTypes.CSM_ENUM_FWD_DECLARATION:
                        {
                            EnumMemberForwardDeclaration fd = renderEnumForwardDeclaration(token);
                            if (fd != null) {
                                addMember(fd, !isRenderingLocalContext());
                                fd.init(token, ClassImpl.this, !isRenderingLocalContext());
                                break;
                            }
                            break;
                        }
                        // other members
                        case CPPTokenTypes.CSM_CTOR_DEFINITION:
                        case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:
                            addMember(ConstructorDDImpl.createConstructor(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()), !isRenderingLocalContext());
                            break;
                        case CPPTokenTypes.CSM_CTOR_DECLARATION:
                        case CPPTokenTypes.CSM_CTOR_TEMPLATE_DECLARATION:
                            addMember(ConstructorImpl.createConstructor(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                            break;
                        case CPPTokenTypes.CSM_DTOR_DEFINITION:
                        case CPPTokenTypes.CSM_DTOR_TEMPLATE_DEFINITION:
                            addMember(DestructorDDImpl.createDestructor(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                            break;
                        case CPPTokenTypes.CSM_DTOR_DECLARATION:
                            addMember(DestructorImpl.createDestructor(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                            break;
                        case CPPTokenTypes.CSM_FIELD:
                            child = token.getFirstChild();
                            if (hasFriendPrefix(child)) {
                                addFriend(renderFriendClass(token), !isRenderingLocalContext());
                            } else {
                                if (renderVariable(token, null, null, ClassImpl.this.getContainingNamespaceImpl(), false)) {
                                    break;
                                }
                                typedefs = renderTypedef(token, (FileImpl) getContainingFile(), fileContent, ClassImpl.this, null);
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
                            UsingDeclarationImpl using = UsingDeclarationImpl.create(token, getContainingFile(), ClassImpl.this, !isRenderingLocalContext(), curentVisibility);
                            addMember(using, !isRenderingLocalContext());
                            break;
                        }
                        case CPPTokenTypes.CSM_TEMPL_FWD_CL_OR_STAT_MEM:
                            {
                                child = token.getFirstChild();
                                if (hasFriendPrefix(child)) {
                                    addFriend(renderFriendClass(token), !isRenderingLocalContext());
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
                                    CsmScope scope = getFriendScope();
                                    CsmFriendFunction friend;
                                    CsmFunction func;
                                    if (isMemberDefinition(token)) {
                                        FriendFunctionImplEx impl = FriendFunctionImplEx.create(token, getContainingFile(), fileContent, ClassImpl.this, scope, !isRenderingLocalContext());
                                        func = impl;
                                        friend = impl;
                                    } else {
                                        FriendFunctionImpl impl = FriendFunctionImpl.create(token, getContainingFile(), fileContent, ClassImpl.this, scope, !isRenderingLocalContext());
                                        friend = impl;
                                        func = impl;
                                        if(!isRenderingLocalContext()) {
                                            if (scope instanceof NamespaceImpl) {
                                                ((NamespaceImpl) scope).addDeclaration(func);
                                            } else {
                                                ((NamespaceImpl) getContainingFile().getProject().getGlobalNamespace()).addDeclaration(func);
                                            }
                                        }
                                    }
                                    //((FileImpl)getContainingFile()).addDeclaration(func);
                                    addFriend(friend,!isRenderingLocalContext());
                                } else {
                                    addMember(MethodImpl.create(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()), !isRenderingLocalContext());
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
                                CsmScope scope = getFriendScope();
                                CsmFriendFunction friend;
                                CsmFunction func;
                                if (isMemberDefinition(token)) {
                                    FriendFunctionDefinitionImpl impl = FriendFunctionDefinitionImpl.create(token, getContainingFile(), fileContent, ClassImpl.this, null, !isRenderingLocalContext());
                                    func = impl;
                                    friend = impl;
                                } else {
                                    FriendFunctionDDImpl impl = FriendFunctionDDImpl.create(token, getContainingFile(), fileContent, ClassImpl.this, scope, !isRenderingLocalContext());
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
                            } else {
                                addMember( MethodDDImpl.<CsmMethod>create(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                            }
                            break;
                        case CPPTokenTypes.CSM_VISIBILITY_REDEF:
                            UsingDeclarationImpl using = UsingDeclarationImpl.create(token, getContainingFile(), ClassImpl.this, !isRenderingLocalContext(), curentVisibility);
                            addMember(using, !isRenderingLocalContext());
                            break;
                        case CPPTokenTypes.RCURLY:
                            break;
                        case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                        case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                            //new VariableImpl(
                            break;
                    }
                } catch (AstRendererException e) {
                    DiagnosticExceptoins.register(e);
                }
            }
            checkInnerIncludes(ClassImpl.this, ClassImpl.this.getMembers());
        }

        private CsmScope getFriendScope() {
            CsmScope scope = ClassImpl.this.getScope();
            while (CsmKindUtilities.isClass(scope)) {
               CsmScope newScope = ((CsmClass)scope).getScope(); 
               if (newScope != null) {
                   scope = newScope;
               } else {
                   break;
               }
            }
            return scope;
        }

        private void setTemplateDescriptor(List<CsmTemplateParameter> params, String name, boolean specialization) {
            templateDescriptor = new TemplateDescriptor(params, name, specialization, !isRenderingLocalContext());
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
            return FriendClassImpl.create(firstChild, qid, cfd, (FileImpl) getContainingFile(), ClassImpl.this, !isRenderingLocalContext());
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
            return ClassMemberForwardDeclaration.create(getContainingFile(), ClassImpl.this, token, curentVisibility, !isRenderingLocalContext());
        }

        private EnumMemberForwardDeclaration renderEnumForwardDeclaration(AST token) {
            AST typeAST = token.getFirstChild();
            if (typeAST == null) {
                return null;
            }
            if (typeAST.getType() == CPPTokenTypes.LITERAL_template) {
                typeAST = typeAST.getNextSibling();
            }
            if (typeAST == null || (typeAST.getType() != CPPTokenTypes.LITERAL_enum)) {
                return null;
            }
            AST idAST = typeAST.getNextSibling();
            if (idAST != null &&
                    (idAST.getType() == CPPTokenTypes.LITERAL_struct ||
                     idAST.getType() == CPPTokenTypes.LITERAL_class)) {
                idAST = idAST.getNextSibling();
            }
            if (idAST == null || (idAST.getType() != CPPTokenTypes.CSM_QUALIFIED_ID &&
                                  idAST.getType() != CPPTokenTypes.IDENT)) {
                return null;
            }
            return EnumMemberForwardDeclaration.create(getContainingFile(), ClassImpl.this, token, curentVisibility, !isRenderingLocalContext());
        }

        private boolean renderBitField(AST token) {

            AST typeAST = token.getFirstChild();
            if (typeAST == null) {
                return false;
            }
            typeAST = getFirstSiblingSkipQualifiers(typeAST);
            if (typeAST == null) {
                return false;
            }
            if (typeAST.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN) {
                if (typeAST.getType() == CPPTokenTypes.LITERAL_enum) {
                    typeAST = typeAST.getNextSibling();
                }
                if (typeAST == null || (typeAST.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND)) {
                    return false;
                }
            }

            // common type for all bit fields
            CsmType type = TypeFactory.createType(typeAST, getContainingFile(), null, 0);
            typeAST = getFirstSiblingSkipQualifiers(typeAST.getNextSibling());
            if (typeAST == null) {
                return false;
            }
            boolean bitFieldAdded = renderBitFieldImpl(token, typeAST, type, null);
            return bitFieldAdded;
        }

        @Override
        protected boolean renderBitFieldImpl(AST startOffsetAST, AST idAST, CsmType type, ClassEnumBase<?> classifier) {
            boolean cont = true;
            boolean added = false;
            AST start = startOffsetAST;
            AST prev = idAST;
            while (cont) {
                boolean unnamed = false;
                AST colonAST;
                if (idAST == null) {
                    break;
                } else if (idAST.getType() == CPPTokenTypes.IDENT) {
                    colonAST = idAST.getNextSibling();
                } else if (idAST.getType() == CPPTokenTypes.COLON){
                    colonAST = idAST;
                    unnamed = true;
                } else {
                    break;
                }

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
                if(!unnamed) {
                    NameHolder nameHolder = NameHolder.createSimpleName(idAST);
                    FieldImpl field = FieldImpl.create(start, getContainingFile(), fileContent, type, nameHolder, ClassImpl.this, curentVisibility, !isRenderingLocalContext());
                    ClassImpl.this.addMember(field,!isRenderingLocalContext());
                    if (classifier != null) {
                        classifier.addEnclosingVariable(field);
                    }
                }
                added = true;
                if (cont) {
                    idAST = prev.getNextSibling();
                }
            }
            return added;
        }
        
        @Override
        protected CsmTypedef createTypedef(AST ast, FileImpl file, CsmObject container, CsmType type, CharSequence name) {
            type = TemplateUtils.checkTemplateType(type, ClassImpl.this);
            return MemberTypedef.create(getContainingFile(), ClassImpl.this, ast, type, name, curentVisibility, !isRenderingLocalContext());
        }

        @Override
        protected CsmClassForwardDeclaration createForwardClassDeclaration(AST ast, MutableDeclarationsContainer container, FileImpl file, CsmScope scope) {
            ClassMemberForwardDeclaration fd = ClassMemberForwardDeclaration.create(getContainingFile(), ClassImpl.this, ast, curentVisibility, !isRenderingLocalContext());
            addMember(fd,!isRenderingLocalContext());
            fd.init(ast, ClassImpl.this, !isRenderingLocalContext());
            return fd;
        }
    }

    public static final class MemberTypedef extends TypedefImpl implements CsmMember {

        private final CsmVisibility visibility;

        private MemberTypedef(CsmFile file, CsmClass containingClass, AST ast, CsmType type, CharSequence name, CsmVisibility curentVisibility) {
            super(ast, file, containingClass, type, name);
            visibility = curentVisibility;
        }

        public static MemberTypedef create(CsmFile file, CsmClass containingClass, AST ast, CsmType type, CharSequence name, CsmVisibility curentVisibility, boolean global) {
            MemberTypedef memberTypedef = new MemberTypedef(file, containingClass, ast, type, name, curentVisibility);
            if (!global) {
                Utils.setSelfUID(memberTypedef);
            }
            return memberTypedef;
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
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
            assert this.visibility != null;
            PersistentUtils.writeVisibility(this.visibility, output);
        }

        public MemberTypedef(RepositoryDataInput input) throws IOException {
            super(input);
            this.visibility = PersistentUtils.readVisibility(input);
            assert this.visibility != null;
        }
    }

    public static interface MemberForwardDeclaration {}

    public static final class ClassMemberForwardDeclaration extends ClassForwardDeclarationImpl
            implements CsmMember, CsmClassifier, MemberForwardDeclaration {

        private final CsmVisibility visibility;
        private CsmUID<CsmClass> classDefinition;
        private final CsmUID<CsmClass> containerUID;
        private CsmClass containerRef;

        private ClassMemberForwardDeclaration(CsmFile file, CsmClass containingClass, AST ast, CsmVisibility curentVisibility, boolean register) {
            super(ast, file, register);
            visibility = curentVisibility;
            containerUID = UIDCsmConverter.declarationToUID(containingClass);
        }

        public static ClassMemberForwardDeclaration create(CsmFile file, CsmClass containingClass, AST ast, CsmVisibility curentVisibility, boolean register) {
            ClassMemberForwardDeclaration res = new ClassMemberForwardDeclaration(file, containingClass, ast, curentVisibility, register);
            postObjectCreateRegistration(register, res);
            return res;
        }

        @Override
        protected final boolean registerInProject() {
            CsmProject project = getContainingFile().getProject();
            if (project instanceof ProjectBase) {
                return ((ProjectBase) project).registerDeclaration(this);
            }
            return false;
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

        private synchronized void onDispose() {
            if (containerRef == null) {
                containerRef = UIDCsmConverter.UIDtoClass(containerUID);
            }
        }

        @Override
        public synchronized CsmClass getContainingClass() {
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
        protected ForwardClass createForwardClassIfNeed(AST ast, CsmScope scope, boolean registerInProject) {
            ForwardClass cls = super.createForwardClassIfNeed(ast, scope, registerInProject);
            classDefinition = UIDCsmConverter.declarationToUID((CsmClass)cls);
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
            return CharSequences.create(cls.getQualifiedName() + "::" + getName()); // NOI18N
        }

        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
            assert visibility != null;
            PersistentUtils.writeVisibility(visibility, output);
            assert containerUID != null;
            UIDObjectFactory.getDefaultFactory().writeUID(containerUID, output);
            UIDObjectFactory.getDefaultFactory().writeUID(classDefinition, output);
        }

        public ClassMemberForwardDeclaration(RepositoryDataInput input) throws IOException {
            super(input);
            visibility = PersistentUtils.readVisibility(input);
            assert visibility != null;
            containerUID = UIDObjectFactory.getDefaultFactory().readUID(input);
            assert containerUID != null;
            classDefinition = UIDObjectFactory.getDefaultFactory().readUID(input);
        }
    }

    public static final class EnumMemberForwardDeclaration extends EnumForwardDeclarationImpl
            implements CsmMember, CsmClassifier, MemberForwardDeclaration {

        private final CsmVisibility visibility;
        private CsmUID<CsmEnum> enumDefinition;
        private final CsmUID<CsmClass> containerUID;
        private CsmClass containerRef;

        private EnumMemberForwardDeclaration(CsmFile file, CsmClass containingClass, AST ast, CsmVisibility curentVisibility, boolean register) {
            super(ast, file, register);
            visibility = curentVisibility;
            containerUID = UIDCsmConverter.declarationToUID(containingClass);
        }

        public static EnumMemberForwardDeclaration create(CsmFile file, CsmClass containingClass, AST ast, CsmVisibility curentVisibility, boolean register) {
            EnumMemberForwardDeclaration res = new EnumMemberForwardDeclaration(file, containingClass, ast, curentVisibility, register);
            postObjectCreateRegistration(register, res);
            return res;
        }

        @Override
        protected final boolean registerInProject() {
            CsmProject project = getContainingFile().getProject();
            if (project instanceof ProjectBase) {
                return ((ProjectBase) project).registerDeclaration(this);
            }
            return false;
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

        private synchronized void onDispose() {
            if (containerRef == null) {
                containerRef = UIDCsmConverter.UIDtoClass(containerUID);
            }
        }

        @Override
        public synchronized CsmClass getContainingClass() {
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
        public CsmEnum getCsmEnum() {
            CsmEnum enm = UIDCsmConverter.UIDtoDeclaration(enumDefinition);
            // we need to replace i.e. ForwardEnum stub
            if (enm != null && enm.isValid() && !ForwardEnum.isForwardEnum(enm)) {
                return enm;
            } else {
                enm = super.getCsmEnum();
                setCsmEnum(enm);
            }
            return enm;
        }

        @Override
        protected ForwardEnum createForwardEnumIfNeed(AST ast, CsmScope scope, boolean registerInProject) {
            ForwardEnum enm = super.createForwardEnumIfNeed(ast, scope, registerInProject);
            enumDefinition = UIDCsmConverter.declarationToUID((CsmEnum) enm);
            if (enm != null) {
                RepositoryUtils.put(this);
            }
            return enm;
        }

        public void setCsmEnum(CsmEnum cls) {
            enumDefinition = UIDCsmConverter.declarationToUID(cls);
        }

        @Override
        public CharSequence getQualifiedName() {
            CsmClass cls = getContainingClass();
            if (cls == null) {
                cls = getContainingClass();
            }
            return CharSequences.create(cls.getQualifiedName() + "::" + getName()); // NOI18N
        }

        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
            assert visibility != null;
            PersistentUtils.writeVisibility(visibility, output);
            assert containerUID != null;
            UIDObjectFactory.getDefaultFactory().writeUID(containerUID, output);
            UIDObjectFactory.getDefaultFactory().writeUID(enumDefinition, output);
        }

        public EnumMemberForwardDeclaration(RepositoryDataInput input) throws IOException {
            super(input);
            visibility = PersistentUtils.readVisibility(input);
            assert visibility != null;
            containerUID = UIDObjectFactory.getDefaultFactory().readUID(input);
            assert containerUID != null;
            enumDefinition = UIDObjectFactory.getDefaultFactory().readUID(input);
        }
    }
}
