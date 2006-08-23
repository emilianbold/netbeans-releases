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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.*;

import antlr.collections.AST;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.antlr2.CsmAST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.deep.*;

/**
 * @author Vladimir Kvasihn
 */
public class AstRenderer {

    private FileImpl file;

    //private StringBuffer currName
    
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
                    ClassImpl cls = new ClassImpl(token, currentNamespace, file);
                    container.addDeclaration(cls);
                    addTypedefs(renderTypedef(token, cls, currentNamespace), currentNamespace, container);
                    break;
                case CPPTokenTypes.CSM_ENUM_DECLARATION:
                    container.addDeclaration(new EnumImpl(token, currentNamespace, file));
                    break;
                case CPPTokenTypes.CSM_FUNCTION_DECLARATION:
                    FunctionImpl fi = new FunctionImpl(token, file, currentNamespace);
                    //fi.setScope(currentNamespace);
                    container.addDeclaration(fi);
                    currentNamespace.addDeclaration(fi);
                    break;
                case CPPTokenTypes.CSM_CTOR_DEFINITION:
                    container.addDeclaration(new ConstructorDefinitionImpl(token, file, null));
                    break;
                case CPPTokenTypes.CSM_DTOR_DEFINITION:
                    container.addDeclaration(new DestructorDefinitionImpl(token, file));
                    break;
                case CPPTokenTypes.CSM_FUNCTION_DEFINITION:
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
                    renderForwardClassDeclaration(token, currentNamespace, container, file);
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
                container.addDeclaration(typedefs[i]);
                currentNamespace.addDeclaration(typedefs[i]);
                file.getProjectImpl().registerDeclaration(typedefs[i]);
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
                                TypeImpl typeImpl = TypeImpl.createType(cls, ptrOperator, arrayDepth, ast, file);
                                CsmTypedef typedef = createTypedef((nameToken == null) ? ast : nameToken, file, container, typeImpl, name);
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
                                    ei = new EnumImpl(curr, nsp, file);
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
                                ptrOperator = ast;
                            }
                            break;
                        case CPPTokenTypes.CSM_QUALIFIED_ID:    
                            // now token corresponds the name, since the case "struct S" is processed before
                            nameToken = curr;
                            name = AstUtil.findId(nameToken);
                            break;
                        case CPPTokenTypes.LSQUARE:
                            arrayDepth++;
                        case CPPTokenTypes.COMMA:
                        case CPPTokenTypes.SEMICOLON:
                            TypeImpl typeImpl = null;
                            if( classifier != null ) {
                                typeImpl = TypeImpl.createType(classifier, file, ptrOperator, arrayDepth);
                            }
                            else if( ei != null ) {
                                typeImpl = TypeImpl.createType(ei, ptrOperator, arrayDepth, ast, file);
                            }
                            if( typeImpl != null) {
                                CsmTypedef typedef = createTypedef(ast/*nameToken*/, file, container, typeImpl, name);
                                if( typedef != null ) {
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
    
    protected CsmTypedef createTypedef(AST ast, FileImpl file, CsmObject container) {
        return new TypedefImpl(ast, file, container);
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
            case CPPTokenTypes.LITERAL_enum:
                ClassForwardDeclarationImpl cfdi = new ClassForwardDeclarationImpl(ast, file);
                if( container != null ) {
                    container.addDeclaration(cfdi);
                    return true;
                }
                break;
        }
                
        return false;
    }
    
    public static String getQualifiedName(AST qid) {
        if( qid != null && qid.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
            if ( qid.getFirstChild() != null ) {
                StringBuffer sb = new StringBuffer();
                for( AST namePart = qid.getFirstChild(); namePart != null; namePart = namePart.getNextSibling() ) {
                    assert( namePart.getType() == CPPTokenTypes.ID || namePart.getType() == CPPTokenTypes.SCOPE);
                    sb.append(namePart.getText());
                }
                return sb.toString();
            }
        }
        return "";
    }

    public static String[] getNameTokens(AST qid) {
        if( qid != null && qid.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
            if ( qid.getNextSibling() != null ) {
                List/*<String>*/ l = new ArrayList/*<String>*/();
                for( AST namePart = qid.getFirstChild(); namePart != null; namePart = namePart.getNextSibling() ) {
                    if( namePart.getType() == CPPTokenTypes.ID ) {
                        l.add(namePart.getText());
                    }
                    else {
                        assert namePart.getType() == CPPTokenTypes.SCOPE;
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
        return TypeImpl.createType(typeAST/*tokType*/, file, ptrOperator, 0); 
    }

    /**
     * Returns first sibling (or just passed ast), skipps cv-qualifiers and storage class specifiers
     */
    public static AST getFirstSiblingSkipQualifiers(AST ast) {
        while( ast != null && isQialifier(ast.getType()) ) {
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
    
    public static boolean isQialifier(int tokenType) {
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
    public boolean renderVariable(AST ast, MutableDeclarationsContainer container1, MutableDeclarationsContainer container2) {
        boolean _static = AstUtil.hasChildOfType(ast, CPPTokenTypes.LITERAL_static);
        AST typeAST = ast.getFirstChild();
        AST tokType = getFirstChildSkipQualifiers(ast);
        if( tokType == null ) {
            return false;
        }
        if( tokType != null && isConstQualifier(tokType.getType())) {
            assert (false): "must be skipped above";
            tokType = tokType.getNextSibling();
        }
        
        if( tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ) {
            
            AST nextToken = tokType.getNextSibling();
            while( nextToken != null && 
                    (nextToken.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ||
                     isCVQualifier(nextToken.getType()))) {
                nextToken = nextToken.getNextSibling(); 
            }
            
//            if( nextToken != null && 
//                (nextToken.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION || 
//                nextToken.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION) ) {
            if( nextToken == null || 
                nextToken.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION || 
                nextToken.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION ) {
                
                /*
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
                */
                
                AST ptrOperator = null;
                boolean theOnly = true;
                boolean hasVariables = false;
                for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
                    switch( token.getType() ) {
                        case CPPTokenTypes.CSM_PTR_OPERATOR:
                            // store only 1-static one - the others (if any) follows,
                            // so it's TypeImpl.createType() responsibility to process them all
                            if( ptrOperator == null ) {
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
                            processVariable(token, ptrOperator, (theOnly ? ast : token), typeAST/*tokType*/, container1, container2, file, _static);
                            ptrOperator = null;
                            break;
                    }
                }
                if( ! hasVariables ) {
                    // unnamed parameter
                    processVariable(ast, ptrOperator, ast, typeAST/*tokType*/, container1, container2, file, _static);
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
        for( AST token = varAst.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LSQUARE:
                    arrayDepth++;
                    break;
                case CPPTokenTypes.CSM_QUALIFIED_ID:
                case CPPTokenTypes.ID:
                    name = token.getText();
                    break;
            }
        }
        CsmType type = TypeImpl.createType(classifier, file, ptrOperator, arrayDepth);
        VariableImpl var = createVariable(offsetAst, file, type, name, _static);
        if( container2 != null ) {
            container2.addDeclaration(var);
        }
        // TODO! don't add to namespace if....
        if( container1 != null ) {
            container1.addDeclaration(var);
        }
    }
    
    protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static) {
        VariableImpl var = new VariableImpl(offsetAst, file, type, name);
        var.setStatic(_static);
        return var;
    }
    
    public static List/*<CsmParameter>*/  renderParameters(AST ast, final CsmFile file) {
        List parameters = new ArrayList();
        if( ast != null && ast.getType() ==  CPPTokenTypes.CSM_PARMLIST ) {
            for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( token.getType() == CPPTokenTypes.CSM_PARAMETER_DECLARATION ) {
                    ParameterImpl param = AstRenderer.renderParameter(token, file);
                    if( param != null ) {
                        parameters.add(param);
                    }
                }
            }
        }
        return parameters;
    }
    
    public static ParameterImpl renderParameter(AST ast, final CsmFile file) {
        AST firstChild = ast.getFirstChild();
        if( firstChild != null && firstChild.getType() == CPPTokenTypes.ELLIPSIS ) {
            return new ParameterImpl(ast.getFirstChild(), file, null, "...");
        }
        else {
            class AstRendererEx extends AstRenderer {
                public ParameterImpl parameter;
                public AstRendererEx() {
                    super((FileImpl) file);
                }
                protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static) {
                    parameter = new ParameterImpl(offsetAst, file, type, name);
                    return parameter;
                }
            }
            AstRendererEx renderer = new AstRendererEx();
            renderer.renderVariable(ast, null, null);
            return renderer.parameter;
        }
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

    private boolean isMemberDefinition(AST ast) {
        AST type = ast.getFirstChild(); // type
        if( type != null && (type.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || type.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) ) {
            AST id = type.getNextSibling();
	    // skip ptr operators
	    while(id != null && id.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {
		id = id.getNextSibling();
	    };
            if( id != null ) {
                if( id.getType() == CPPTokenTypes.ID ) {
                    AST scope = id.getNextSibling();
                    if( scope != null && scope.getType() == CPPTokenTypes.SCOPE ) {
                        return true;
                    }
                }
                else if( id.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
                    if( id.getNumberOfChildren() > 1 ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static CsmCompoundStatement findCompoundStatement(AST ast, CsmFile file) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == CPPTokenTypes.CSM_COMPOUND_STATEMENT ) {
                return new CompoundStatementImpl(token, file);
            }
        }
        return null;
    }
    
    public static StatementBase renderStatement(AST ast, CsmFile file) {
        switch( ast.getType() ) {
            case CPPTokenTypes.CSM_LABELED_STATEMENT:
                return new LabelImpl(ast, file);
            case CPPTokenTypes.CSM_CASE_STATEMENT:
                return new CaseStatementImpl(ast, file);
            case CPPTokenTypes.CSM_DEFAULT_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.DEFAULT);
            case CPPTokenTypes.CSM_EXPRESSION_STATEMENT:
                return new ExpressionStatementImpl(ast, file);
            case CPPTokenTypes.CSM_DECLARATION_STATEMENT:
                return new DeclarationStatementImpl(ast, file);
            case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                return new CompoundStatementImpl(ast, file);
            case CPPTokenTypes.CSM_IF_STATEMENT:
                return new IfStatementImpl(ast, file);
            case CPPTokenTypes.CSM_SWITCH_STATEMENT:
                return new SwitchStatementImpl(ast, file);
            case CPPTokenTypes.CSM_WHILE_STATEMENT:
                return new LoopStatementImpl(ast, file, false);
            case CPPTokenTypes.CSM_DO_WHILE_STATEMENT:
                return new LoopStatementImpl(ast, file, true);
            case CPPTokenTypes.CSM_FOR_STATEMENT:
                return new ForStatementImpl(ast, file);
            case CPPTokenTypes.CSM_GOTO_STATEMENT:
                return new GotoStatementImpl(ast, file);
            case CPPTokenTypes.CSM_CONTINUE_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.CONTINUE);
            case CPPTokenTypes.CSM_BREAK_STATEMENT:
                return new UniversalStatement(ast, file, CsmStatement.Kind.BREAK);
            case CPPTokenTypes.CSM_RETURN_STATEMENT:
                return new ReturnStatementImpl(ast, file);
            case CPPTokenTypes.CSM_TRY_STATEMENT:
                return new TryCatchStatementImpl(ast, file);
            case CPPTokenTypes.CSM_CATCH_CLAUSE:
                // TODO: isn't it in TryCatch ??
                return new UniversalStatement(ast, file, CsmStatement.Kind.CATCH);
            case CPPTokenTypes.CSM_THROW_STATEMENT:
                // TODO: throw
                return new UniversalStatement(ast, file, CsmStatement.Kind.THROW);
            case CPPTokenTypes.CSM_ASM_BLOCK:
                // just ignore
        }
        return null;
    }
    
    public ExpressionBase renderExpression(AST ast) {
        return isExpression(ast) ? new ExpressionBase(ast, file, null) : null;
    }
    
    public CsmCondition renderCondition(AST ast) {
        if( ast != null && ast.getType() == CPPTokenTypes.CSM_CONDITION ) {
            AST first = ast.getFirstChild();
            if( first != null ) {
                int type = first.getType();
                if( isExpression(type) ) {
                    return new ConditionExpressionImpl(first, file);
                }
                else if( type == CPPTokenTypes.CSM_TYPE_BUILTIN || type == CPPTokenTypes.CSM_TYPE_COMPOUND ) {
                    return new ConditionDeclarationImpl(ast, file);
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
    
