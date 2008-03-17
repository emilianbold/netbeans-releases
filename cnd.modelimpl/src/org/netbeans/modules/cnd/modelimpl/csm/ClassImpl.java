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
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * Implements CsmClass
 * @author Vladimir Kvashin
 */
public class ClassImpl extends ClassEnumBase<CsmClass> implements CsmClass, CsmMember<CsmClass>, CsmTemplate {

    private final CsmDeclaration.Kind kind;
    
    private final List<CsmUID<CsmMember>> members = new ArrayList<CsmUID<CsmMember>>();

    private final List<CsmUID<CsmFriend>> friends = new ArrayList<CsmUID<CsmFriend>>();
    
    private final List<CsmInheritance> inheritances = new ArrayList<CsmInheritance>();
    
    private List<CsmTemplateParameter> templateParams = Collections.emptyList();
    private CharSequence templateSuffix;
    private boolean template;
    
    private /*final*/ int leftBracketPos;
    
    private class ClassAstRenderer extends AstRenderer {
        
        private CsmVisibility curentVisibility = CsmVisibility.PRIVATE;
        
        public ClassAstRenderer() {
            super((FileImpl) ClassImpl.this.getContainingFile());
        }
        
        @Override
        protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, 
		MutableDeclarationsContainer container1, MutableDeclarationsContainer container2,CsmScope scope) {
	    type = TemplateUtils.checkTemplateType(type, ClassImpl.this);
            FieldImpl field = new FieldImpl(offsetAst, file, type, name, ClassImpl.this, curentVisibility);
            ClassImpl.this.addMember(field);
            return field;
        }
        
        @Override
        public void render(AST ast) {
	    boolean rcurlyFound = false;
            CsmTypedef[] typedefs;
            AST child;
            for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
                switch( token.getType() ) {
                    //case CPPTokenTypes.CSM_TEMPLATE_PARMLIST:
                    case CPPTokenTypes.LITERAL_template:
                        template = true;
                        templateSuffix = '<' + TemplateUtils.getClassSpecializationSuffix(token) + '>';
                        templateParams = TemplateUtils.getTemplateParameters(token, ClassImpl.this);
                        break;
                    case CPPTokenTypes.CSM_BASE_SPECIFIER:
                        inheritances.add(new InheritanceImpl(token, getContainingFile()));
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
			ClassImpl innerClass = TemplateUtils.isPartialClassSpecialization(token) ? 
			    ClassImplSpecialization.create(token, ClassImpl.this, getContainingFile()) :
			    ClassImpl.create(token, ClassImpl.this, getContainingFile());
                        innerClass.setVisibility(curentVisibility);
                        addMember(innerClass);
                        typedefs = renderTypedef(token, innerClass, ClassImpl.this);
                        if( typedefs != null && typedefs.length > 0 ) {
                            for (int i = 0; i < typedefs.length; i++) {
                                // It could be important to register in project before add as member...
                                ((FileImpl)getContainingFile()).getProjectImpl().registerDeclaration(typedefs[i]);
                                addMember((MemberTypedef) typedefs[i]);
                            }
                        }
                        renderVariableInClassifier(token, innerClass, null, null);
                        break;
                    case CPPTokenTypes.CSM_ENUM_DECLARATION:
                        EnumImpl innerEnum = EnumImpl.create(token, ClassImpl.this, getContainingFile());
                        innerEnum.setVisibility(curentVisibility);
                        addMember(innerEnum);
                        renderVariableInClassifier(token, innerEnum, null, null);
                        break;

                    // other members
                    case CPPTokenTypes.CSM_CTOR_DEFINITION:
                    case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:
                        addMember(new ConstructorDDImpl(token, ClassImpl.this, curentVisibility));
                        break;
                    case CPPTokenTypes.CSM_CTOR_DECLARATION:
                    case CPPTokenTypes.CSM_CTOR_TEMPLATE_DECLARATION:
                        addMember(new ConstructorImpl(token, ClassImpl.this, curentVisibility));
                        break;
                    case CPPTokenTypes.CSM_DTOR_DEFINITION:
                    case CPPTokenTypes.CSM_DTOR_TEMPLATE_DEFINITION:
                        addMember(new DestructorDDImpl(token, ClassImpl.this, curentVisibility));
                        break;
                    case CPPTokenTypes.CSM_DTOR_DECLARATION:
                        addMember(new DestructorImpl(token, ClassImpl.this, curentVisibility));
                        break;
                    case CPPTokenTypes.CSM_FIELD:
			child = token.getFirstChild();
			if( child != null && child.getType() == CPPTokenTypes.LITERAL_friend) {
                            addFriend(new FriendClassImpl(child, (FileImpl) getContainingFile(),ClassImpl.this));
                        } else {
                            if( renderVariable(token, null, null) ) {
                                break;
                            }
                            typedefs = renderTypedef(token, (FileImpl) getContainingFile(), ClassImpl.this);
                            if( typedefs != null && typedefs.length > 0 ) {
                                for (int i = 0; i < typedefs.length; i++) {
                                    // It could be important to register in project before add as member...
                                    ((FileImpl)getContainingFile()).getProjectImpl().registerDeclaration(typedefs[i]);
                                    addMember((MemberTypedef) typedefs[i]);
                                }
                                break;
                            }
                            if( renderBitField(token) ) {
                                break;
                            }
                        }
                        break;
                    case CPPTokenTypes.CSM_FUNCTION_DECLARATION:
                    case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION:
                    case CPPTokenTypes.CSM_USER_TYPE_CAST:
			child = token.getFirstChild();
			if( child != null) {
                            if (child.getType() == CPPTokenTypes.LITERAL_friend) {
                                CsmScope scope = ClassImpl.this.getScope();
                                CsmFriendFunction friend;
                                CsmFunction func;
                                if (isMemberDefinition(token)) {
                                    FriendFunctionImplEx impl = new FriendFunctionImplEx(token, ClassImpl.this, scope);
                                    func = impl;
                                    friend = impl;
                                } else {
                                    FriendFunctionImpl impl = new FriendFunctionImpl(token, ClassImpl.this, scope);
                                    friend = impl;
                                    func = impl;
                                    if (scope instanceof NamespaceImpl) {
                                        ((NamespaceImpl)scope).addDeclaration(func);
                                    } else {
                                        ((NamespaceImpl)getContainingFile().getProject().getGlobalNamespace()).addDeclaration(func);
                                    }
                                }
                                //((FileImpl)getContainingFile()).addDeclaration(func);
                                addFriend(friend);
                            } else {
                                addMember(new MethodImpl(token, ClassImpl.this, curentVisibility));
                            }
			}
                        break;
                    case CPPTokenTypes.CSM_FUNCTION_DEFINITION:
                    case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION:
		    case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION:
			child = token.getFirstChild();
			if( child != null && child.getType() == CPPTokenTypes.LITERAL_friend) {
                            CsmScope scope = ClassImpl.this.getScope();
                            CsmFriendFunction friend;
                            CsmFunction func;
                            if (isMemberDefinition(token)) {
                                FriendFunctionDefinitionImpl impl = new FriendFunctionDefinitionImpl(token, ClassImpl.this, null);
                                func = impl;
                                friend = impl;
                            } else {
                                FriendFunctionDDImpl impl = new FriendFunctionDDImpl(token, ClassImpl.this, scope);
                                friend = impl;
                                func = impl;
                                if (scope instanceof NamespaceImpl) {
                                    ((NamespaceImpl)scope).addDeclaration(func);
                                } else {
                                    ((NamespaceImpl)getContainingFile().getProject().getGlobalNamespace()).addDeclaration(func);
                                }
                            }
                            //((FileImpl)getContainingFile()).addDeclaration(func);
                            addFriend(friend);
                        } else {
                            addMember(new MethodDDImpl(token, ClassImpl.this, curentVisibility));
                        }
                        break;
                    case CPPTokenTypes.CSM_VISIBILITY_REDEF:
                        break;
		    case CPPTokenTypes.RCURLY:
			rcurlyFound = true;
			break;
		    case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
			//new VariableImpl(
			break;
                }
            }
        }

	private boolean renderBitField(AST token) {
	    
	    AST typeAST = token.getFirstChild();
	    if( typeAST == null || 
                    (typeAST.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN &&
                     typeAST.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND)) {
		return false;
	    }
	    
	    AST idAST = typeAST.getNextSibling();
	    if( idAST == null || idAST.getType() != CPPTokenTypes.ID ) {
		return false;
	    }
	    
	    AST colonAST = idAST.getNextSibling();
	    if( colonAST == null || colonAST.getType() != CPPTokenTypes.COLON ) {
		return false;
	    }
		    
	    CsmType type = TypeFactory.createType(typeAST, getContainingFile(), null, 0);
            FieldImpl field = new FieldImpl(token, getContainingFile(), type, idAST.getText(), ClassImpl.this, curentVisibility);
            ClassImpl.this.addMember(field);
	    
	    return true;
	}
	
        @Override
        protected CsmTypedef createTypedef(AST ast, FileImpl file, CsmObject container, CsmType type, String name) {
            type = TemplateUtils.checkTemplateType(type, ClassImpl.this);
            return new MemberTypedef(ClassImpl.this, ast, type, name, curentVisibility);
        }
    }
    
    public static class MemberTypedef extends TypedefImpl implements CsmMember<CsmTypedef> {
        private CsmVisibility visibility;

        public MemberTypedef(CsmClass containingClass, AST ast, CsmType type, String name, CsmVisibility curentVisibility) {
            super(ast, containingClass.getContainingFile(), containingClass, type, name);
            visibility = curentVisibility;
        }

        public boolean isStatic() {
            return false;
        }

        public CsmVisibility getVisibility() {
            return visibility;
        }

        public CsmClass getContainingClass() {
            return  (CsmClass)getScope();
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

    protected ClassImpl(AST ast, CsmFile file) {
	// we call findId(..., true) because there might be qualified name - in the case of nested class template specializations
        super(AstUtil.findId(ast, CPPTokenTypes.RCURLY, true), file, ast); 
	kind = findKind(ast);
    }

    @Override
    protected void init(CsmScope scope, AST ast) {
	super.init(scope, ast);
        RepositoryUtils.hang(this); // "hang" now and then "put" in "register()"
        new ClassAstRenderer().render(ast);
        leftBracketPos = initLeftBracketPos(ast);
        register(scope);
    }
    
    public static ClassImpl create(AST ast, CsmScope scope, CsmFile file) {
	ClassImpl impl = new ClassImpl(ast, file);
	impl.init(scope, ast);
	return impl;
    }

    public CsmDeclaration.Kind getKind() {
        return this.kind;
    }
    
    public Collection<CsmMember> getMembers() {
        Collection<CsmMember> out;
        synchronized (members) {
            out = UIDCsmConverter.UIDsToDeclarations(members);
        }
        return out;
    }

    public Collection<CsmFriend> getFriends() {
        Collection<CsmFriend> out;
        synchronized (friends) {
            out = UIDCsmConverter.UIDsToDeclarations(friends);
        }
        return out;
    }
    
    public List<CsmInheritance> getBaseClasses() {
        return inheritances;
    }
    
    public boolean isTemplate() {
        return template;
    }

    private void addMember(CsmMember member) {
        CsmUID<CsmMember> uid = RepositoryUtils.put(member);
        assert uid != null;
        synchronized (members) {
            members.add(uid);
        }
    }

    private void addFriend(CsmFriend friend) {
        CsmUID<CsmFriend> uid = RepositoryUtils.put(friend);
        assert uid != null;
        synchronized (friends) {
            friends.add(uid);
        }
    }
    
    private int initLeftBracketPos(AST node) {
        AST lcurly = AstUtil.findChildOfType(node, CPPTokenTypes.LCURLY);
        return (lcurly instanceof CsmAST)  ? ((CsmAST) lcurly).getOffset() : getStartOffset();
    }
   
    public int getLeftBracketOffset() {
        return leftBracketPos;
    }

    public Collection<CsmScopeElement> getScopeElements() {
        return (Collection)getMembers();
    }
    
    @Override
    public void dispose() {
        _clearMembers();
        _clearFriends();
        super.dispose();
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
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
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

    public CharSequence getDisplayName() {
	return isTemplate() ? CharSequenceKey.create((getName().toString() + templateSuffix)) : getName(); // NOI18N
    }

    public List<CsmTemplateParameter> getTemplateParameters() {
	return templateParams;
    }
	
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
//    private final CsmDeclaration.Kind kind;
//    
//    private final List/*<CsmMember>*/ membersOLD = new ArrayList/*<CsmMember>*/();
//    private final List<CsmUID<CsmMember>> members = new ArrayList<CsmUID<CsmMember>>();
//    
//    private final List/*<CsmInheritance>*/ inheritances = new ArrayList/*<CsmInheritance>*/();
//    private boolean template;
//    
//    private final int leftBracketPos;        
        assert this.kind != null;
        writeKind(this.kind, output);
        output.writeBoolean(this.template);
        PersistentUtils.writeTemplateParameters(templateParams, output);
        if (this.template) {
            output.writeUTF(this.templateSuffix.toString());
        }
        output.writeInt(this.leftBracketPos);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.members, output, true);
        factory.writeUIDCollection(this.friends, output, true);
        PersistentUtils.writeInheritances(this.inheritances, output);
    }
    
    public ClassImpl(DataInput input) throws IOException {
        super(input);
        this.kind = readKind(input);
        this.template = input.readBoolean();
        this.templateParams = PersistentUtils.readTemplateParameters(input);
        if (this.template) {
            this.templateSuffix = NameCache.getManager().getString(input.readUTF());
        }
        this.leftBracketPos = input.readInt();
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.readUIDCollection(this.members, input);
        factory.readUIDCollection(this.friends, input);
        PersistentUtils.readInheritances(this.inheritances, input);        
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

