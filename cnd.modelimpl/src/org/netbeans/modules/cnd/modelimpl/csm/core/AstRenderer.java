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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.*;

import antlr.collections.AST;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.modelimpl.csm.deep.EmptyCompoundStatementImpl;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.deep.*;

/**
 * @author Vladimir Kvasihn
 */
public class AstRenderer {

    private FileImpl file;

    //private StringBuilder currName
    
    public AstRenderer(FileImpl fileImpl) {
        this.file = fileImpl;
    }
    
    public void render(AST root) {
        render(root, (NamespaceImpl) file.getProject().getGlobalNamespace(), file);
    }

    public void render(AST tree, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if( tree  == null ) return; // paranoia
        for( AST token = tree.getFirstChild(); token != null; token = token.getNextSibling() ) {
            int type = token.getType();
            switch( type ) {
                case CPPTokenTypes.CSM_LINKAGE_SPECIFICATION:
                    render(token, currentNamespace, container);
                    break;
                case CPPTokenTypes.CSM_NAMESPACE_DECLARATION:
                    NamespaceDefinitionImpl ns = new NamespaceDefinitionImpl(token, file, currentNamespace);
                    container.addDeclaration(ns);
                    render(token, (NamespaceImpl) ns.getNamespace(), ns);
                    break;
                case CPPTokenTypes.CSM_CLASS_DECLARATION:
                case CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION: 
                {
                    ClassImpl cls = TemplateUtils.isPartialClassSpecialization(token) ? 
			ClassImplSpecialization.create(token, currentNamespace, file) :
			ClassImpl.create(token, currentNamespace, file);
                    container.addDeclaration(cls);
                    addTypedefs(renderTypedef(token, cls, currentNamespace), currentNamespace, container);
                    renderVariableInClassifier(token, cls, currentNamespace, container);
                    break;
                }
                case CPPTokenTypes.CSM_ENUM_DECLARATION:
                {
                    CsmEnum csmEnum = EnumImpl.create(token, currentNamespace, file);
                    container.addDeclaration(csmEnum);
                    renderVariableInClassifier(token, csmEnum, currentNamespace, container);
                    break;
                }
                case CPPTokenTypes.CSM_FUNCTION_DECLARATION:
                case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION:
                case CPPTokenTypes.CSM_USER_TYPE_CAST:
                    FunctionImpl fi = new FunctionImpl(token, file, currentNamespace);
                    //fi.setScope(currentNamespace);
                    container.addDeclaration(fi);
                    currentNamespace.addDeclaration(fi);
                    break;
                case CPPTokenTypes.CSM_CTOR_DEFINITION:
                case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:
                    container.addDeclaration(new ConstructorDefinitionImpl(token, file, null));
                    break;
                case CPPTokenTypes.CSM_DTOR_DEFINITION:
                    container.addDeclaration(new DestructorDefinitionImpl(token, file));
                    break;
                case CPPTokenTypes.CSM_FUNCTION_DEFINITION:
                case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION:
		case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION:
                    if( isMemberDefinition(token) ) {
                        container.addDeclaration(new FunctionDefinitionImpl(token, file, null));
                    }
                    else {
                        FunctionDDImpl fddi = new FunctionDDImpl(token, file, currentNamespace);
			//fddi.setScope(currentNamespace);
                        container.addDeclaration(fddi);
                        currentNamespace.addDeclaration(fddi);
                    }
                    break;
                case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_SPECIALIZATION:
                    if( isClassSpecialization(token) ) {
                        ClassImpl spec = ClassImplSpecialization.create(token, currentNamespace, file);
                        container.addDeclaration(spec);
                        addTypedefs(renderTypedef(token, spec, currentNamespace), currentNamespace, container);
                    }
                    else {
			if( isMemberDefinition(token) ) {
			    // this is a template method specialization declaration (without a definition)
			    container.addDeclaration(new FunctionImplEx(token, file, null));
			}
			else {
			    FunctionImpl funct = new FunctionImpl(token, file, currentNamespace);
			    container.addDeclaration(funct);
			    currentNamespace.addDeclaration(funct);
			}
                    }
                    break; 
                case CPPTokenTypes.CSM_TEMPLATE_FUNCTION_DEFINITION_EXPLICIT_SPECIALIZATION:
                    if( isMemberDefinition(token) ) {
                        container.addDeclaration(new FunctionDefinitionImpl(token, file, null));
                    } else {
                        FunctionDDImpl fddit = new FunctionDDImpl(token, file, currentNamespace);
                        container.addDeclaration(fddit);
                        currentNamespace.addDeclaration(fddit);
                    }
                    break;
                case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                    container.addDeclaration(new NamespaceAliasImpl(token, file));
                    break;
                case CPPTokenTypes.CSM_USING_DIRECTIVE:
                    container.addDeclaration(new UsingDirectiveImpl(token, file));
                    break;
                case CPPTokenTypes.CSM_USING_DECLARATION:
                    container.addDeclaration(new UsingDeclarationImpl(token, file));
                    break;
                case CPPTokenTypes.CSM_TEMPL_FWD_CL_OR_STAT_MEM:
                    if (renderForwardClassDeclaration(token, currentNamespace, container, file)){
                        break;
                    } else {
                        renderForwardMemberDeclaration(token, currentNamespace, container, file);
                    }
                    break;
                case CPPTokenTypes.CSM_GENERIC_DECLARATION:
                    if( renderNSP(token, currentNamespace, container, file) ) {
                        break;
                    }
                    if( renderVariable(token, currentNamespace, container) ) {
                        break;
                    }
                    if( renderForwardClassDeclaration(token, currentNamespace, container, file) ) {
                        break;
                    }
                    if( renderLinkageSpec(token, file, currentNamespace, container) ) {
                        break;
                    }
                    CsmTypedef[] typedefs = renderTypedef(token, file, currentNamespace);
                    if( typedefs != null && typedefs.length > 0 ) {
                        addTypedefs(typedefs, currentNamespace, container);
                        break;
                    }
                default:
                    renderNSP(token, currentNamespace, container, file);
            }
        }
    }
    
    protected void addTypedefs(CsmTypedef[] typedefs, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if( typedefs != null ) {
            for (int i = 0; i < typedefs.length; i++) {
                // It could be important to register in project before add as member...
                file.getProjectImpl().registerDeclaration(typedefs[i]);
                if (container != null) {
                    container.addDeclaration(typedefs[i]);
                }
                if (currentNamespace != null) {
                    // Note: DeclarationStatementImpl.DSRenderer can call with null namespace
                    currentNamespace.addDeclaration(typedefs[i]);
                }
            }
        }
    }
    
    
    private boolean renderLinkageSpec(AST ast, FileImpl file, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if( ast != null ) {
            AST token = ast.getFirstChild();
            if( token.getType() == CPPTokenTypes.CSM_LINKAGE_SPECIFICATION ) {
                render(token, currentNamespace, container);
                return true;
            }
        }
        return false;
    }

    protected void renderVariableInClassifier(AST ast, CsmClassifier classifier,
            MutableDeclarationsContainer container1, MutableDeclarationsContainer container2){
        AST token = ast.getFirstChild();
        for (; token != null; token = token.getNextSibling()){
            if (token.getType() == CPPTokenTypes.RCURLY){
                break;
            }
        }
        if (token != null){
            AST ptrOperator = null;
            for (; token != null; token = token.getNextSibling()){
                switch( token.getType() ) {
                    case CPPTokenTypes.CSM_PTR_OPERATOR:
                        if( ptrOperator == null ) {
                            ptrOperator = token;
                        }
                        break;
                    case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                    case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                    {
                        int arrayDepth = 0;
                        String name = null;
                        for( AST varNode = token.getFirstChild(); varNode != null; varNode = varNode.getNextSibling() ) {
                            switch( varNode.getType() ) {
                                case CPPTokenTypes.LSQUARE:
                                    arrayDepth++;
                                    break;
                                case CPPTokenTypes.CSM_QUALIFIED_ID:
                                case CPPTokenTypes.ID:
                                    name = varNode.getText();
                                    break;
                            }
                        }
                        if (name != null) {
                            CsmType type = TypeFactory.createType(classifier, ptrOperator, arrayDepth, token, file);
                            VariableImpl var = createVariable(token, file, type, name, false, container1, container2, null);
                            if( container2 != null ) {
                                container2.addDeclaration(var);
                            }
                            // TODO! don't add to namespace if....
                            if( container1 != null ) {
                                container1.addDeclaration(var);
                            }
                            ptrOperator = null;
                        }
                    }
                }
            }
        }
    }
    

    protected CsmTypedef[] renderTypedef(AST ast, CsmClass cls, CsmObject container) {
        
        List results = new ArrayList();
        
        AST typedefNode = ast.getFirstChild();
        
        if( typedefNode != null && typedefNode.getType() == CPPTokenTypes.LITERAL_typedef ) {
            
            AST classNode = typedefNode.getNextSibling();
            
            switch ( classNode.getType() ) {

                case CPPTokenTypes.LITERAL_class:
                case CPPTokenTypes.LITERAL_union:
                case CPPTokenTypes.LITERAL_struct:

                    AST curr = AstUtil.findSiblingOfType(classNode, CPPTokenTypes.RCURLY);
                    if( curr == null ) {
                        return new CsmTypedef[0];
                    }

                    int arrayDepth = 0;
                    AST nameToken = null;
                    AST ptrOperator = null;
                    String name = "";
                    for( curr = curr.getNextSibling(); curr != null; curr = curr.getNextSibling() ) {
                        switch( curr.getType() ) {
                            case CPPTokenTypes.CSM_PTR_OPERATOR:
                                // store only 1-st one - the others (if any) follows,
                                // so it's TypeImpl.createType() responsibility to process them all
                                if( ptrOperator == null ) {
                                    ptrOperator = ast;
                                }
                                break;
                            case CPPTokenTypes.CSM_QUALIFIED_ID:
                                nameToken = curr;
                                //token t = nameToken.
                                name = AstUtil.findId(nameToken);
                                //name = token.getText();
                                break;
                            case CPPTokenTypes.LSQUARE:
                                arrayDepth++;
                            case CPPTokenTypes.COMMA:
                            case CPPTokenTypes.SEMICOLON:
                                TypeImpl typeImpl = TypeFactory.createType(cls, ptrOperator, arrayDepth, ast, file);
                                CsmTypedef typedef = createTypedef((nameToken == null) ? ast : nameToken, file, container, typeImpl, name);
                                if (cls != null && cls.getName().length()==0){
                                    ((TypedefImpl)typedef).setTypeUnnamed();
                                }
                                if( typedef != null ) {
                                    results.add(typedef);
                                }
                                ptrOperator = null;
                                name = "";
                                nameToken = null;
                                arrayDepth = 0;
                                break;
                        }

                    }
                    break;
                default:
                    // error message??
            }
        }
        return (CsmTypedef[]) results.toArray(new CsmTypedef[results.size()]);
    }
    
    protected CsmTypedef[] renderTypedef(AST ast, FileImpl file, CsmObject container) {
        List results = new ArrayList();
        if( ast != null ) {
            AST firstChild = ast.getFirstChild();
            if( firstChild.getType() == CPPTokenTypes.LITERAL_typedef ) {
                //return createTypedef(ast, file, container);

                AST classifier = null;
                int arrayDepth = 0;
                AST nameToken = null;
                AST ptrOperator = null;
                String name = "";
                
                EnumImpl ei = null;
                    
                for( AST curr = ast.getFirstChild(); curr != null; curr = curr.getNextSibling() ) {
                    switch( curr.getType() ) {
                        case CPPTokenTypes.CSM_TYPE_COMPOUND:
                        case CPPTokenTypes.CSM_TYPE_BUILTIN:
                            classifier = curr;
                            break;
                        case CPPTokenTypes.LITERAL_enum:
                            if( AstUtil.findSiblingOfType(curr, CPPTokenTypes.RCURLY) != null ) {
                                NamespaceImpl nsp = null;
                                if( container instanceof NamespaceImpl ) {
                                    nsp = (NamespaceImpl) container;
                                }
                                else if( container instanceof ClassImpl ) {
                                    nsp = ((ClassImpl) container).getContainingNamespaceImpl();
                                }
                                if( nsp != null ) {
                                    ei = EnumImpl.create(curr, nsp, file);
                                    file.addDeclaration(ei);
                                    if( container instanceof  MutableDeclarationsContainer )
                                    ((MutableDeclarationsContainer) container).addDeclaration(ei);
                                }
                                break;
                            }
                            // else fall through!
                        case CPPTokenTypes.LITERAL_struct:
                        case CPPTokenTypes.LITERAL_union:
                        case CPPTokenTypes.LITERAL_class:
                            AST next = curr.getNextSibling();
                            if( next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
                                classifier = next;
                            }
                            break;
                        case CPPTokenTypes.CSM_PTR_OPERATOR:
                            // store only 1-st one - the others (if any) follows,
                            // so it's TypeImpl.createType() responsibility to process them all
                            if( ptrOperator == null ) {
                                ptrOperator = curr;
                            }
                            break;
                        case CPPTokenTypes.CSM_QUALIFIED_ID:    
                            // now token corresponds the name, since the case "struct S" is processed before
                            nameToken = curr;
                            name = AstUtil.findId(nameToken);
                            break;
                        case CPPTokenTypes.LSQUARE:
                            arrayDepth++;
                            break;
                        case CPPTokenTypes.COMMA:
                        case CPPTokenTypes.SEMICOLON:
                            TypeImpl typeImpl = null;
                            if( classifier != null ) {
                                typeImpl = TypeFactory.createType(classifier, file, ptrOperator, arrayDepth);
                            }
                            else if( ei != null ) {
                                typeImpl = TypeFactory.createType(ei, ptrOperator, arrayDepth, ast, file);
                            }
                            if( typeImpl != null) {
                                CsmTypedef typedef = createTypedef(ast/*nameToken*/, file, container, typeImpl, name);
                                if( typedef != null ) {
                                    if (ei != null && ei.getName().length()==0){
                                        ((TypedefImpl)typedef).setTypeUnnamed();
                                    }
                                    results.add(typedef);
                                }
                            }
                            ptrOperator = null;
                            name = "";
                            nameToken = null;
                            arrayDepth = 0;
                            break;
                    }
                }
            }
        }
        return (CsmTypedef[]) results.toArray(new CsmTypedef[results.size()]);
    }
    
    protected CsmTypedef createTypedef(AST ast, FileImpl file, CsmObject container, CsmType type, String name) {
        return new TypedefImpl(ast, file, container, type, name);
    }
    
    
    public static boolean renderForwardClassDeclaration(
            AST ast, 
            NamespaceImpl currentNamespace, MutableDeclarationsContainer container, 
            FileImpl file) {
        
        AST child = ast.getFirstChild();
        if( child == null ) {
            return false;
        }
        if (child.getType() == CPPTokenTypes.LITERAL_template) {
            child = child.getNextSibling();
            if( child == null ) {
                return false;
            }
        }
        
        switch( child.getType() ) {
            case CPPTokenTypes.LITERAL_class:
            case CPPTokenTypes.LITERAL_struct:
            case CPPTokenTypes.LITERAL_union:
                ClassForwardDeclarationImpl cfdi = new ClassForwardDeclarationImpl(ast, file);
                if( container != null ) {
                    container.addDeclaration(cfdi);
                }
                return true;
        }
                
        return false;
    }

    public static boolean renderForwardMemberDeclaration(
            AST ast, 
            NamespaceImpl currentNamespace, MutableDeclarationsContainer container, 
            FileImpl file) {
        
        AST child = ast.getFirstChild();
        while(child != null){
            switch(child.getType()){
                case CPPTokenTypes.LITERAL_template:
                case CPPTokenTypes.LITERAL_inline:
                    child = child.getNextSibling();
                    continue;
            }
            break;
        }
        if( child == null ) {
            return false;
        }
        if (child.getType() == CPPTokenTypes.LITERAL_template) {
            child = child.getNextSibling();
            if( child == null ) {
                return false;
            }
        }
        
        switch( child.getType() ) {
            case CPPTokenTypes.CSM_TYPE_COMPOUND:
            case CPPTokenTypes.CSM_TYPE_BUILTIN:
                child = child.getNextSibling();
                if (child != null){
                    if (child.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION){
                        //static variable definition
                    } else {
                        //method forward declaratin
                        FunctionImpl ftdecl = new FunctionImpl(ast, file, currentNamespace);
                        if( container != null ) {
                            container.addDeclaration(ftdecl);
                        }
                        return true;
                    }
                }
                break;
        }
                
        return false;
    }
    
    
    public static String getQualifiedName(AST qid) {
        if( qid != null && qid.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
            if ( qid.getFirstChild() != null ) {
                StringBuilder sb = new StringBuilder();
                for( AST namePart = qid.getFirstChild(); namePart != null; namePart = namePart.getNextSibling() ) {
                    // TODO: update this assert it should accept names like: allocator<char, typename A>
//                    if( ! ( namePart.getType() == CPPTokenTypes.ID || namePart.getType() == CPPTokenTypes.SCOPE ||
//                            namePart.getType() == CPPTokenTypes.LESSTHAN || namePart.getType() == CPPTokenTypes.GREATERTHAN ||
//                            namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
//                            namePart.getType() == CPPTokenTypes.COMMA) ) {
//			new Exception("Unexpected token type " + namePart).printStackTrace(System.err);
//		    }
                    if (namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                        AST builtInType = namePart.getFirstChild();
                        sb.append(builtInType != null ? builtInType.getText() : "");
                    } else {
                        sb.append(namePart.getText());
                    }
                }
                return sb.toString();
            }
        }
        return "";
    }

    public static String[] getNameTokens(AST qid) {
        if( qid != null && (qid.getType() == CPPTokenTypes.CSM_QUALIFIED_ID || qid.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND)) {
            int templateDepth = 0;
            if ( qid.getNextSibling() != null ) {
                List/*<String>*/ l = new ArrayList/*<String>*/();
                for( AST namePart = qid.getFirstChild(); namePart != null; namePart = namePart.getNextSibling() ) {
                    if( templateDepth == 0 && namePart.getType() == CPPTokenTypes.ID ) {
                        l.add(namePart.getText());
                    }
                    else if( namePart.getType() == CPPTokenTypes.LESSTHAN ) {
                        // the beginning of template parameters
                        templateDepth++;
                    }
                    else if( namePart.getType() == CPPTokenTypes.GREATERTHAN ) {
                        // the beginning of template parameters
                        templateDepth--;
                    }
                    else {
                        //assert namePart.getType() == CPPTokenTypes.SCOPE;
                        if( templateDepth == 0 && namePart.getType() != CPPTokenTypes.SCOPE ) {
                            StringBuilder tokenText = new StringBuilder();
                            tokenText.append('[').append(namePart.getText());
                            if (namePart.getNumberOfChildren() == 0) {
                                tokenText.append(", line=").append(namePart.getLine()); // NOI18N
                                tokenText.append(", column=").append(namePart.getColumn()); // NOI18N
                            }
                            tokenText.append(']');
                            System.err.println("Incorect token: expected '::', found " + tokenText.toString());
                        }
                    }
                }
                return (String[]) l.toArray(new String[l.size()]);
            }
        }
        return new String[0];
    }
    
  
    public static TypeImpl renderType(AST tokType, CsmFile file) {
        
        AST typeAST = tokType;
        tokType = getFirstSiblingSkipQualifiers(tokType);
                
        if( tokType == null ||  
            (tokType.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN && 
            tokType.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND) ) {
            return null;
        }
         
        /**
        CsmClassifier classifier = null;
        if( tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
            classifier = BuiltinTypes.getBuiltIn(tokType);
        }
        else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
            try {
                Resolver resolver = new Resolver(file, ((CsmAST) tokType.getFirstChild()).getOffset());
                // gather name components into string array 
                // for example, for std::vector new String[] { "std", "vector" }
                List l = new ArrayList();
                for( AST namePart = tokType.getFirstChild(); namePart != null; namePart = namePart.getNextSibling() ) {
                    if( namePart.getType() == CPPTokenTypes.ID ) {
                        l.add(namePart.getText());
                    }
                    else {
                        assert namePart.getType() == CPPTokenTypes.SCOPE;
                    }
                }
                CsmObject o = resolver.resolve((String[]) l.toArray(new String[l.size()]));
                if( o instanceof CsmClassifier ) {
                    classifier = (CsmClassifier) o;
                }
            }
            catch( Exception e ) {
                e.printStackTrace(System.err);
            }
        }

        if( classifier != null ) {
            AST next = tokType.getNextSibling();
            AST ptrOperator =  (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) ? next : null;
            return TypeImpl.createType(classifier, ptrOperator, 0);
        }
        
        return null;
         */
        AST next = tokType.getNextSibling();
        AST ptrOperator =  (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) ? next : null;
        return TypeFactory.createType(typeAST/*tokType*/, file, ptrOperator, 0); 
    }

    /**
     * Returns first sibling (or just passed ast), skipps cv-qualifiers and storage class specifiers
     */
    public static AST getFirstSiblingSkipQualifiers(AST ast) {
        while( ast != null && isQualifier(ast.getType()) ) {
            ast = ast.getNextSibling();
        }
        return ast;
    }
    
    /**
     * Returns first child, skipps cv-qualifiers and storage class specifiers
     */
    public static AST getFirstChildSkipQualifiers(AST ast) {
        return getFirstSiblingSkipQualifiers(ast.getFirstChild());
    }
    
    public static boolean isQualifier(int tokenType) {
        return isCVQualifier(tokenType) || isStorageClassSpecifier(tokenType);
    }
    
    public static boolean isCVQualifier(int tokenType) {
        return isConstQualifier(tokenType) || isVolatileQualifier(tokenType);
    }

    public static boolean isConstQualifier(int tokenType) {
        switch( tokenType ) {
            case CPPTokenTypes.LITERAL_const:       return true;
            case CPPTokenTypes.LITERAL___const:     return true;
            default:                                return false;
        }
    }   

    public static boolean isVolatileQualifier(int tokenType) {
        switch( tokenType ) {
            case CPPTokenTypes.LITERAL_volatile:    return true;
            case CPPTokenTypes.LITERAL___volatile__:return true;
            default:                                return false;
        }
    }
    
    public static boolean isStorageClassSpecifier(int tokenType) {
        switch( tokenType ) {
            case CPPTokenTypes.LITERAL_auto:        return true;
            case CPPTokenTypes.LITERAL_register:    return true;
            case CPPTokenTypes.LITERAL_static:      return true;
            case CPPTokenTypes.LITERAL_extern:      return true;
            case CPPTokenTypes.LITERAL_mutable:     return true;
            default:                                return false;
        }
    }

    /**
     * Checks whether the given AST is a variable declaration(s), 
     * if yes, creates variable(s), adds to conteiner(s), returns true,
     * otherwise returns false;
     *
     * There might be two containers, in which the given variable should be added.
     * For example, global variables should beadded both to file and to global namespace;
     * variables, declared in some namespace definition, should be added to both this definition and correspondent namespace as well.
     *
     * On the other hand, local variables are added only to it's containing scope, so either container1 or container2 might be null.
     *
     * @param ast AST to process
     * @param container1 container to add created variable into (may be null)
     * @param container2 container to add created variable into (may be null)
     */
    public boolean renderVariable(AST ast, MutableDeclarationsContainer namespaceContainer, MutableDeclarationsContainer container2) {
        boolean _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static);
        AST typeAST = ast.getFirstChild();
        AST tokType = getFirstChildSkipQualifiers(ast);
        if( tokType == null ) {
            return false;
        }
        boolean isThisReference = false;
        if (tokType != null &&
            tokType.getType() == CPPTokenTypes.LITERAL_struct ||
            tokType.getType() == CPPTokenTypes.LITERAL_union ||
            tokType.getType() == CPPTokenTypes.LITERAL_enum ||
            tokType.getType() == CPPTokenTypes.LITERAL_class){
            // This is struct/class word for reference on containing struct/class
            tokType = tokType.getNextSibling();
            typeAST = tokType;
            if( tokType == null ) {
                return false;
            }
            isThisReference = true;
        }
        if( tokType != null && isConstQualifier(tokType.getType())) {
            assert (false): "must be skipped above";
            tokType = tokType.getNextSibling();
        }
        
        if( tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
            tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
            tokType.getType() == CPPTokenTypes.CSM_QUALIFIED_ID && isThisReference) {
            
            AST nextToken = tokType.getNextSibling();
            while( nextToken != null && 
                    (nextToken.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ||
                     isQualifier(nextToken.getType()) ||
                     nextToken.getType() == CPPTokenTypes.LPAREN)) {
                nextToken = nextToken.getNextSibling(); 
            }
            
            if( nextToken == null || 
                nextToken.getType() == CPPTokenTypes.LSQUARE ||
                nextToken.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION || 
                nextToken.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION ||
		nextToken.getType() == CPPTokenTypes.ASSIGNEQUAL ) {
                
                AST ptrOperator = null;
                boolean theOnly = true;
                boolean hasVariables = false;
		int inParamsLevel = 0;

                for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
                    switch( token.getType() ) {
			case CPPTokenTypes.LPAREN:
			    inParamsLevel++;
			    break;
			case CPPTokenTypes.RPAREN:
			    inParamsLevel--;
			    break;		    
                        case CPPTokenTypes.CSM_PTR_OPERATOR:
                            // store only 1-st one - the others (if any) follows,
                            // so it's TypeImpl.createType() responsibility to process them all
                            if( ptrOperator == null && inParamsLevel == 0) {
                                ptrOperator = token;
                            }
                            break;
                        case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                        case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                            hasVariables = true;
                            if( theOnly ) {
                                for( AST next = token.getNextSibling(); next != null; next = next.getNextSibling() ) {
                                    int type = next.getType();
                                    if( type == CPPTokenTypes.CSM_VARIABLE_DECLARATION || type == CPPTokenTypes.CSM_ARRAY_DECLARATION ) {
                                        theOnly = false;
                                    }
                                }
                            }
                            processVariable(token, ptrOperator, (theOnly ? ast : token), typeAST/*tokType*/, namespaceContainer, container2, file, _static);
                            ptrOperator = null;
                            break;
                    }
                }
                if( ! hasVariables ) {
                    // unnamed parameter
                    processVariable(ast, ptrOperator, ast, typeAST/*tokType*/, namespaceContainer, container2, file, _static);
                }
                return true;
            }
        }
        return false;
    }
    
    protected void processVariable(AST varAst, AST ptrOperator, AST offsetAst,  AST classifier, 
                                MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, 
                                FileImpl file, boolean _static) {
        int arrayDepth = 0;
        String name = "";
            AST qn = null;
	int inParamsLevel = 0;
        for( AST token = varAst.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
		case CPPTokenTypes.LPAREN:
		    inParamsLevel++;
		    break;
		case CPPTokenTypes.RPAREN:
		    inParamsLevel--;
		    break;
                case CPPTokenTypes.LSQUARE:
		    if( inParamsLevel == 0 ) {
			arrayDepth++;
		    }
                    break;
                case CPPTokenTypes.LITERAL_struct:
                case CPPTokenTypes.LITERAL_union:
                case CPPTokenTypes.LITERAL_enum:
                case CPPTokenTypes.LITERAL_class:
		    // skip both this and next
                    token = token.getNextSibling();
                    continue;
                case CPPTokenTypes.CSM_QUALIFIED_ID:
		    if( inParamsLevel == 0 ) {
			qn = token;
		    }
                    // no break;
                case CPPTokenTypes.ID:
		    if( inParamsLevel == 0 ) {
			name = token.getText();
		    }
                    break;
            }
        }
        CsmType type = TypeFactory.createType(classifier, file, ptrOperator, arrayDepth);
        if (isScopedId(qn)){
            // This is definition of global namespace variable or definition of static class variable
            // TODO What about global variable definitions:
            // extern int i; - declaration
            // int i; - definition
            VariableDefinitionImpl var = new VariableDefinitionImpl(offsetAst, file, type, name);
            var.setStatic(_static);
            if( container2 != null ) {
                container2.addDeclaration(var);
            }
        } else {
            VariableImpl var = createVariable(offsetAst, file, type, name, _static, container1, container2, null);
            if( container2 != null ) {
                container2.addDeclaration(var);
            }
            // TODO! don't add to namespace if....
            if( container1 != null ) {
                container1.addDeclaration(var);
            }
        }
    }
    
    protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, 
	    MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope) {
	
        VariableImpl var = new VariableImpl(offsetAst, file, type, name, scope, (container1 != null) || (container2 != null));
        var.setStatic(_static);
        return var;
    }
    
    public static List<CsmParameter>  renderParameters(AST ast, final CsmFile file, CsmScope scope) {
        List<CsmParameter> parameters = new ArrayList<CsmParameter>();
        if( ast != null && ast.getType() ==  CPPTokenTypes.CSM_PARMLIST ) {
            for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( token.getType() == CPPTokenTypes.CSM_PARAMETER_DECLARATION ) {
                    ParameterImpl param = AstRenderer.renderParameter(token, file, scope);
                    if( param != null ) {
                        parameters.add(param);
                    }
                }
            }
        }
        return parameters;
    }
    
    public static boolean isVoidParameter(AST ast) {
        if( ast != null && ast.getType() ==  CPPTokenTypes.CSM_PARMLIST ) {
            AST token = ast.getFirstChild();
            if( token != null && token.getType() == CPPTokenTypes.CSM_PARAMETER_DECLARATION ) {
                AST firstChild = token.getFirstChild();
                if( firstChild != null ) {
                    if( firstChild.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN && firstChild.getNextSibling() == null ) {
                        AST grandChild = firstChild.getFirstChild();
                        if( grandChild != null && grandChild.getType() == CPPTokenTypes.LITERAL_void ) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public static ParameterImpl renderParameter(AST ast, final CsmFile file, final CsmScope scope1) {
        AST firstChild = ast.getFirstChild();
        if( firstChild != null ) {
	    if( firstChild.getType() == CPPTokenTypes.ELLIPSIS ) {
		ParameterImpl parameter = new ParameterImpl(ast.getFirstChild(), file, null, "...", scope1); // NOI18N
		return parameter;
	    }
	    if( firstChild.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN && firstChild.getNextSibling() == null ) {
		AST grandChild = firstChild.getFirstChild();
		if( grandChild != null && grandChild.getType() == CPPTokenTypes.LITERAL_void ) {
		    return null;
		}
	    }
        }
	class AstRendererEx extends AstRenderer {
	    public ParameterImpl parameter;
	    public AstRendererEx() {
		super((FileImpl) file);
	    }
	    protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope2) {
		parameter = new ParameterImpl(offsetAst, file, type, name, scope1);
		return parameter;
	    }
	}
	AstRendererEx renderer = new AstRendererEx();
	renderer.renderVariable(ast, null, null);
	return renderer.parameter;
    }
    
    
    
//    public static boolean isCsmType(AST token) {
//        if( token != null ) {
//            int type = token.getType();
//            return type == CPPTokenTypes.CSM_TYPE_BUILTIN || type == CPPTokenTypes.CSM_TYPE_COMPOUND;
//        }
//        return false;
//    }
    
    public static int getType(AST token) {
        return (token == null) ? -1 : token.getType();
    }
    
    public static int getFirstChildType(AST token) {
        AST child = token.getFirstChild();
        return (child == null) ? -1 : child.getType();
    }

//    public static int getNextSiblingType(AST token) {
//        AST sibling = token.getNextSibling();
//        return (sibling == null) ? -1 : sibling.getType();
//    }
    
    public static boolean renderNSP(AST token, NamespaceImpl currentNamespace, MutableDeclarationsContainer container, FileImpl file) {
        token = token.getFirstChild();
        if( token == null ) return false;
        switch( token.getType() ) {
            case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                container.addDeclaration(new NamespaceAliasImpl(token, file));
                return true;
            case CPPTokenTypes.CSM_USING_DIRECTIVE:
                container.addDeclaration(new UsingDirectiveImpl(token, file));
                return true;
            case CPPTokenTypes.CSM_USING_DECLARATION:
                container.addDeclaration(new UsingDeclarationImpl(token, file));
                return true;
        }
        return false;
    }

    private boolean isClassSpecialization(AST ast){
        AST type = ast.getFirstChild(); // type
        if (type != null){
            AST child = type;
            while((child=child.getNextSibling())!=null){
                if (child.getType() == CPPTokenTypes.GREATERTHAN){
                    child = child=child.getNextSibling();
                    if (child != null && (child.getType() == CPPTokenTypes.LITERAL_class ||
                            child.getType() == CPPTokenTypes.LITERAL_struct)){
                        return true;
                    }
                    return false;
                }
            }
        }
        return true;
    }
     
    protected boolean isMemberDefinition(AST ast) {
	if( CastUtils.isCast(ast) ) {
	    return CastUtils.isMemberDefinition(ast);
	}
        AST id = AstUtil.findMethodName(ast);
        return isScopedId(id);
    }

    private boolean isScopedId(AST id){
	if( id == null ) {
	    return false;
	}
        if( id.getType() == CPPTokenTypes.ID ) {
            AST scope = id.getNextSibling();
            if( scope != null && scope.getType() == CPPTokenTypes.SCOPE ) {
                return true;
            }
        } else if( id.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
            int i = 0;
            AST q = id.getFirstChild();
            while(q!=null){
                if (q.getType() == CPPTokenTypes.SCOPE){
                    return true;
                }
                q = q.getNextSibling();
            }
        }
        return false;
    }
    
    public static CsmCompoundStatement findCompoundStatement(AST ast, CsmFile file, CsmFunction owner) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
	    switch( token.getType() ) {
		case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
		    return new CompoundStatementImpl(token, file, owner);
		case CPPTokenTypes.CSM_COMPOUND_STATEMENT_LAZY:
		    return new LazyCompoundStatementImpl(token, file, owner);
	    }
        }
        // prevent null bodies
        return new EmptyCompoundStatementImpl(ast, file, owner);
    }
    
    public static StatementBase renderStatement(AST ast, CsmFile file, CsmScope scope) {
        switch( ast.getType() ) {
            case CPPTokenTypes.CSM_LABELED_STATEMENT:
                return new LabelImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CASE_STATEMENT:
                return new CaseStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_DEFAULT_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.DEFAULT, scope);
            case CPPTokenTypes.CSM_EXPRESSION_STATEMENT:
                return new ExpressionStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CLASS_DECLARATION:
            case CPPTokenTypes.CSM_ENUM_DECLARATION:
            case CPPTokenTypes.CSM_DECLARATION_STATEMENT:
                return new DeclarationStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                return new CompoundStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_IF_STATEMENT:
                return new IfStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_SWITCH_STATEMENT:
                return new SwitchStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_WHILE_STATEMENT:
                return new LoopStatementImpl(ast, file, false, scope);
            case CPPTokenTypes.CSM_DO_WHILE_STATEMENT:
                return new LoopStatementImpl(ast, file, true, scope);
            case CPPTokenTypes.CSM_FOR_STATEMENT:
                return new ForStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_GOTO_STATEMENT:
                return new GotoStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CONTINUE_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.CONTINUE, scope);
            case CPPTokenTypes.CSM_BREAK_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.BREAK, scope);
            case CPPTokenTypes.CSM_RETURN_STATEMENT:
                return new ReturnStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_TRY_STATEMENT:
                return new TryCatchStatementImpl(ast, file, scope);
            case CPPTokenTypes.CSM_CATCH_CLAUSE:
                // TODO: isn't it in TryCatch ??
                return new UniversalStatement(ast, file, CsmStatement.Kind.CATCH, scope);
            case CPPTokenTypes.CSM_THROW_STATEMENT:
                // TODO: throw
                return new UniversalStatement(ast, file, CsmStatement.Kind.THROW, scope);
            case CPPTokenTypes.CSM_ASM_BLOCK:
                // just ignore
                break;
//            case CPPTokenTypes.SEMICOLON:
//            case CPPTokenTypes.LCURLY:
//            case CPPTokenTypes.RCURLY:
//                break;
//            default:
//                System.out.println("unexpected statement kind="+ast.getType());
//                break;
        }
        return null;
    }
    
    public ExpressionBase renderExpression(AST ast) {
        return isExpression(ast) ? new ExpressionBase(ast, file, null) : null;
    }
    
    public CsmCondition renderCondition(AST ast, CsmScope scope) {
        if( ast != null && ast.getType() == CPPTokenTypes.CSM_CONDITION ) {
            AST first = ast.getFirstChild();
            if( first != null ) {
                int type = first.getType();
                if( isExpression(type) ) {
                    return new ConditionExpressionImpl(first, file);
                }
                else if( type == CPPTokenTypes.CSM_TYPE_BUILTIN || type == CPPTokenTypes.CSM_TYPE_COMPOUND ) {
                    return new ConditionDeclarationImpl(ast, file, scope);
                }
            }
        }
        return null;
    }
    
    public static boolean isExpression(AST ast) {
        return ast != null && isExpression(ast.getType());
    }
    
    public static boolean isExpression(int tokenType) {
        return 
            CPPTokenTypes.CSM_EXPRESSIONS_START < tokenType &&
            tokenType < CPPTokenTypes.CSM_EXPRESSIONS_END;
    }
    
    public static boolean isStatement(AST ast) {
        return ast != null && isStatement(ast.getType());
    }
    
    public static boolean isStatement(int tokenType) {
        return 
            CPPTokenTypes.CSM_STATEMENTS_START < tokenType && 
            tokenType < CPPTokenTypes.CSM_STATEMENTS_END;
    }
    
//    public ExpressionBase renderExpression(ExpressionBase parent) {
//        
//    }
    
}
    
