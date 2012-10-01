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

import java.util.* ;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl.ClassBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.EnumeratorImpl.EnumeratorBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Implements CsmEnum
 * @author Vladimir Kvashin
 */
public class EnumImpl extends ClassEnumBase<CsmEnum> implements CsmEnum {
    private final boolean stronglyTyped;
    private final List<CsmUID<CsmEnumerator>> enumerators;
    
    private EnumImpl(AST ast, NameHolder name, CsmFile file) {
        super(name, file, ast);
        this.stronglyTyped = isStronglyTypedEnum(ast);
        enumerators = new ArrayList<CsmUID<CsmEnumerator>>();
    }

    protected EnumImpl(CharSequence name, String qName, boolean stronglyTyped, CsmFile file, int startOffset, int endOffset) {
        super(name, qName, file, startOffset, endOffset);
        this.stronglyTyped = stronglyTyped;
        enumerators = new ArrayList<CsmUID<CsmEnumerator>>();
    }
    
    public void init(CsmScope scope, AST ast, final CsmFile file, boolean register) {
	initScope(scope);
//        initEnumeratorList(ast, file, register);
        if (register) {
            register(scope, true);
        }
    }
    
    public static EnumImpl create(AST ast, CsmScope scope, final CsmFile file, FileContent fileContent, boolean register) {
        NameHolder nameHolder = NameHolder.createEnumName(ast);
	EnumImpl impl = new EnumImpl(ast, nameHolder, file);
	impl.init2(scope, ast, file, fileContent, register);
        nameHolder.addReference(fileContent, impl);
	return impl;
    }
    
    void init2(CsmScope scope, AST ast, final CsmFile file, FileContent fileContent, boolean register) {
	initScope(scope);
        temporaryRepositoryRegistration(register, this);
        initEnumDefinition(scope);
        initEnumeratorList(ast, file, fileContent, register);
        if (register) {
            register(scope, true);
        }
    }    

    private void initEnumDefinition(CsmScope scope) {
        ClassImpl.MemberForwardDeclaration mfd = findMemberForwardDeclaration(scope);
        if (mfd instanceof ClassImpl.EnumMemberForwardDeclaration && CsmKindUtilities.isEnum(this)) {
            ClassImpl.EnumMemberForwardDeclaration fd = (ClassImpl.EnumMemberForwardDeclaration) mfd;
            fd.setCsmEnum((CsmEnum) this);
            CsmClass containingClass = fd.getContainingClass();
            if (containingClass != null) {
                // this is our real scope, not current namespace
                initScope(containingClass);
            }
        }
    }

    void addEnumerator(String name, int startOffset, int endOffset, boolean register) {
        EnumeratorImpl ei = EnumeratorImpl.create(this, name, startOffset, endOffset, register);
        CsmUID<CsmEnumerator> uid = UIDCsmConverter.<CsmEnumerator>objectToUID(ei);
        enumerators.add(uid);
    }

    void addEnumerator(EnumeratorImpl ei) {
        CsmUID<CsmEnumerator> uid = UIDCsmConverter.<CsmEnumerator>objectToUID(ei);
        enumerators.add(uid);
    }

    @Override
    public String toString() {
        if (stronglyTyped) {
            return "[Strongly Typed]" + super.toString(); // NOI18N
        } else {
            return super.toString();
        }
    }

    public final void fixFakeRender(FileContent fileContent, AST ast, boolean localClass) {
        initEnumeratorList(ast, fileContent.getFile(), fileContent, !localClass);
    }
    
    private void initEnumeratorList(AST ast, final CsmFile file, FileContent fileContent, boolean global){
        //enum A { a, b, c };
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST ) {
                addList(token, file, fileContent, global);
                return;
            }
        }
        AST token = ast.getNextSibling();
        if( token != null) {
            AST enumList = null;
            if (token.getType() == CPPTokenTypes.IDENT) {
                //typedef enum C { a2, b2, c2 } D;
                token = token.getNextSibling();
            }
            if (token.getType() == CPPTokenTypes.LCURLY ) {
                //typedef enum { a1, b1, c1 } B;
                enumList = token.getNextSibling();
            }
            if (enumList != null && enumList.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST) {
                addList(enumList, file, fileContent, global);
            }
        }
    }
    
    private void addList(AST token, final CsmFile file, FileContent fileContent, boolean global){
        for( AST t = token.getFirstChild(); t != null; t = t.getNextSibling() ) {
            if( t.getType() == CPPTokenTypes.IDENT ) {
                EnumeratorImpl ei = EnumeratorImpl.create(t, file, fileContent, this, global);
                CsmUID<CsmEnumerator> uid = UIDCsmConverter.<CsmEnumerator>objectToUID(ei);
                enumerators.add(uid);
            }
        }
    }

    @Override
    public boolean isStronglyTyped() {
        return stronglyTyped;
    }

    @Override
    public Collection<CsmEnumerator> getEnumerators() {
        Collection<CsmEnumerator> out = UIDCsmConverter.UIDsToDeclarations(enumerators);
        return out;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return (Collection)getEnumerators();
    }
    
    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUM;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        _clearEnumerators();
    }
    
    private void _clearEnumerators() {
        Collection<CsmEnumerator> enumers = getEnumerators();
        Utils.disposeAll(enumers);
        RepositoryUtils.remove(enumerators);
    }

    static boolean isStronglyTypedEnum(AST ast) {
        assert ast.getType() == CPPTokenTypes.CSM_ENUM_DECLARATION ||
               ast.getType() == CPPTokenTypes.CSM_ENUM_FWD_DECLARATION ||
                ast.getType() == CPPTokenTypes.LITERAL_enum : ast;
        if (ast.getType() == CPPTokenTypes.CSM_ENUM_DECLARATION ||
            ast.getType() == CPPTokenTypes.CSM_ENUM_FWD_DECLARATION) {
            ast = ast.getFirstChild();
        }
        while (ast.getType() != CPPTokenTypes.LITERAL_enum) {
            ast = ast.getNextSibling();
        }
        assert ast.getType() == CPPTokenTypes.LITERAL_enum : ast;
        if (ast.getType() == CPPTokenTypes.LITERAL_enum) {
            AST nextSibling = ast.getNextSibling();
            if (nextSibling != null) {
                switch (nextSibling.getType()) {
                    case CPPTokenTypes.LITERAL_struct:
                    case CPPTokenTypes.LITERAL_class:
                        return true;
                }
            }
        }
        return false;
    }
    
    public static class EnumBuilder implements CsmObjectBuilder {
        
        private CharSequence name;
        private String qName;
        private CsmFile file;
        private int startOffset;
        private int endOffset;
        private boolean stronglyTyped = false;
        private final FileContent fileContent;
        private CsmObjectBuilder parent;
        
        private EnumImpl instance;
        private CsmScope scope;
        
        public EnumBuilder(FileContent fileContent) {
            this.fileContent = fileContent;
        }

        List<EnumeratorBuilder> enumeratorBuilders = new ArrayList<EnumeratorBuilder>();
        
        public void setName(CharSequence name) {
            this.name = name;
            // for now without scope
            qName = name.toString();
        }

        public void setFile(CsmFile file) {
            this.file = file;
        }

        public void setStronglyTyped() {
            this.stronglyTyped = true;
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

        public void addEnumerator(EnumeratorBuilder eb) {
            enumeratorBuilders.add(eb);
        }
        
        public EnumImpl getEnumDefinitionInstance() {
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
                CsmOffsetableDeclaration decl = container.findExistingDeclaration(startOffset, name, Kind.ENUM);
                if (decl != null && EnumImpl.class.equals(decl.getClass())) {
                    instance = (EnumImpl) decl;
                }
            }
            return instance;
        }
        
        public void setScope(CsmScope scope) {
            assert scope != null;
            this.scope = scope;
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
        
        public EnumImpl create(boolean register) {
            EnumImpl impl = getEnumDefinitionInstance();
            if(impl == null) {
                NameHolder nameHolder = NameHolder.createName(name);
                
                if(name == null) {
                    name = nameHolder.getName();
                    qName = name.toString();
                }
                impl = new EnumImpl(name, qName, stronglyTyped, file, startOffset, endOffset);
                impl.initScope(getScope());
                impl.register(getScope(), true);
                nameHolder.addReference(fileContent, impl);
                OffsetableDeclarationBase.temporaryRepositoryRegistration(register, impl);
                
                for (EnumeratorBuilder enumeratorBuilder : enumeratorBuilders) {
                    enumeratorBuilder.setEnum(impl);
                    EnumeratorImpl ei = enumeratorBuilder.create(register);
                    impl.addEnumerator(ei);
                }
                
                if(parent != null) {
                    if(parent instanceof ClassBuilder) {
                        ((ClassBuilder)parent).getClassDefinitionInstance().addMember(impl, true);
                    } else if(parent instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                        ((NamespaceDefinitionImpl.NamespaceBuilder)parent).addDeclaration(impl);
                    }
                } else {
                    fileContent.addDeclaration(impl);
                }
            }
            return impl;
        }
    }
    
////////////////////////////////////////////////////////////////////////////
// impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        output.writeBoolean(stronglyTyped);
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(this.enumerators, output, false);
    }
    
    public EnumImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.stronglyTyped = input.readBoolean();
        int collSize = input.readInt();
        if (collSize < 0) {
            enumerators = new ArrayList<CsmUID<CsmEnumerator>>(0);
        } else {
            enumerators = new ArrayList<CsmUID<CsmEnumerator>>(collSize);
        }
        UIDObjectFactory.getDefaultFactory().readUIDCollection(this.enumerators, input, collSize);
    }
}
