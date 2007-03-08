/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Implements CsmClass
 * @author Vladimir Kvashin
 */
public class ClassImpl extends ClassEnumBase<CsmClass> implements CsmClass, CsmMember<CsmClass> {

    private final CsmDeclaration.Kind kind;
    
    private final List/*<CsmMember>*/ membersOLD = new ArrayList/*<CsmMember>*/();
    private final List<CsmUID<CsmMember>> members = new ArrayList<CsmUID<CsmMember>>();
    
    private final List<CsmInheritance> inheritances = new ArrayList<CsmInheritance>();
    private boolean template;
    
    private final int leftBracketPos;
    
    private class ClassAstRenderer extends AstRenderer {
        
        private CsmVisibility curentVisibility = CsmVisibility.PRIVATE;
        
        public ClassAstRenderer() {
            super((FileImpl) ClassImpl.this.getContainingFile());
        }
        
        protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static) {
            FieldImpl field = new FieldImpl(offsetAst, file, type, name, ClassImpl.this, curentVisibility);
            ClassImpl.this.addMember(field);
            return field;
        }
        
        public void render(AST ast) {
	    boolean rcurlyFound = false;
            CsmTypedef[] typedefs;
            for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
                switch( token.getType() ) {
                    //case CPPTokenTypes.CSM_TEMPLATE_PARMLIST:
                    case CPPTokenTypes.LITERAL_template:
                        template = true;
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
                        ClassImpl innerClass = new ClassImpl(token, getContainingNamespaceImpl(), getContainingFile(), ClassImpl.this);
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
                        EnumImpl innerEnum = new EnumImpl(token, getContainingNamespaceImpl(), getContainingFile(), ClassImpl.this);
                        innerEnum.setVisibility(curentVisibility);
                        addMember(innerEnum);
                        renderVariableInClassifier(token, innerEnum, null, null);
                        break;

                    // other members
                    case CPPTokenTypes.CSM_CTOR_DEFINITION:
                    case CPPTokenTypes.CSM_CTOR_DECLARATION:
                        addMember(new ConstructorImpl(token, ClassImpl.this, curentVisibility));
                        break;
                    case CPPTokenTypes.CSM_DTOR_DEFINITION:
                        addMember(new DestructorDDImpl(token, ClassImpl.this, curentVisibility));
                        break;
                    case CPPTokenTypes.CSM_DTOR_DECLARATION:
                        addMember(new DestructorImpl(token, ClassImpl.this, curentVisibility));
                        break;
                    case CPPTokenTypes.CSM_FIELD:
                        //addMember(new FieldImpl(token, this, curentVisibility));
                        if( ! renderVariable(token, null, null) ) {
                            typedefs = renderTypedef(token, (FileImpl) getContainingFile(), ClassImpl.this);
                            if( typedefs != null && typedefs.length > 0 ) {
                                for (int i = 0; i < typedefs.length; i++) {
                                    // It could be important to register in project before add as member...
                                    ((FileImpl)getContainingFile()).getProjectImpl().registerDeclaration(typedefs[i]);
                                    addMember((MemberTypedef) typedefs[i]);
                                }
                            }
                        }
                        break;
                    case CPPTokenTypes.CSM_FUNCTION_DECLARATION:
                        addMember(new MethodImpl(token, ClassImpl.this, curentVisibility));
                        break;
                    case CPPTokenTypes.CSM_FUNCTION_DEFINITION:
                        addMember(new MethodDDImpl(token, ClassImpl.this, curentVisibility));
                        break;
                    case CPPTokenTypes.CSM_USER_TYPE_CAST:
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

        protected CsmTypedef createTypedef(AST ast, FileImpl file, CsmObject container, CsmType type, String name) {
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

    public ClassImpl(CsmDeclaration.Kind kind, String name, NamespaceImpl namespace, CsmFile file) {
        this(kind, name, namespace, file, null);
    }
    
    public ClassImpl(CsmDeclaration.Kind kind, String name, NamespaceImpl namespace, CsmFile file, CsmClass containingClass) {
        super(name, namespace, file, containingClass, null);
        leftBracketPos = 0;
        this.kind = CsmDeclaration.Kind.CLASS;
        register();
    }

    public ClassImpl(AST ast, NamespaceImpl namespace, CsmFile file) { 
        this(ast, namespace, file, null);
    }
    
    public ClassImpl(AST ast, NamespaceImpl namespace, CsmFile file, CsmClass containingClass) {
        super(AstUtil.findId(ast, CPPTokenTypes.RCURLY), namespace, file, containingClass, ast);
        leftBracketPos = initLeftBracketPos(ast);
        this.kind = findKind(ast);
        if (TraceFlags.USE_REPOSITORY) {
            RepositoryUtils.hang(this); // "hang" now and then "put" in "register()"
        }
        new ClassAstRenderer().render(ast);
        register();
    }

    public CsmDeclaration.Kind getKind() {
        return this.kind;
    }
    
    public List/*<CsmMember>*/ getMembers() {
        if (TraceFlags.USE_REPOSITORY) {
            List<CsmMember> out = UIDCsmConverter.UIDsToDeclarations(new ArrayList<CsmUID<CsmMember>>(members));
            return out;
        } else {
            return membersOLD;
        }
    }
    
    public List/*<CsmInheritance>*/ getBaseClasses() {
        return inheritances;
    }
    
    public boolean isTemplate() {
        return template;
    }

    private void addMember(CsmMember member) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmMember> uid = RepositoryUtils.put(member);
            assert uid != null;
            members.add(uid);            
        } else {
            membersOLD.add(member);
        }
    }
    
    private int initLeftBracketPos(AST node) {
        AST lcurly = AstUtil.findChildOfType(node, CPPTokenTypes.LCURLY);
        return (lcurly instanceof CsmAST)  ? ((CsmAST) lcurly).getOffset() : getStartOffset();
    }
   
    public int getLeftBracketOffset() {
        return leftBracketPos;
    }

    public List getScopeElements() {
        return getMembers();
    }
    
    public void dispose() {
        _clearMembers();
        super.dispose();
    }    
    
    private void _clearMembers() {
        if (TraceFlags.USE_REPOSITORY) {
            RepositoryUtils.remove(members);
        } else {
            membersOLD.clear();
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
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

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
        output.writeInt(this.leftBracketPos);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.members, output, false);
        PersistentUtils.writeInheritances(this.inheritances, output);
    }
    
    public ClassImpl(DataInput input) throws IOException {
        super(input);
        this.kind = readKind(input);
        this.template = input.readBoolean();
        this.leftBracketPos = input.readInt();
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.readUIDCollection(this.members, input);
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

