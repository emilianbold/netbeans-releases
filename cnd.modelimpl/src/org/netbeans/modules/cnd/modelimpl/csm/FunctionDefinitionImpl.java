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

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import java.util.*;
import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * @author Vladimir Kvasihn
 */
public class FunctionDefinitionImpl extends FunctionImpl<CsmFunctionDefinition> implements CsmFunctionDefinition {

    private CsmFunction declaration;
    private String qualifiedName;
    //private String name;
    private final CsmCompoundStatement body;
    private List/*<CsmParameter>*/  parameters;
    private boolean qualifiedNameIsFake = false;
    private String[] classOrNspNames;
    
    public FunctionDefinitionImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
        body = AstRenderer.findCompoundStatement(ast, getContainingFile());
    }
    
    protected void initBeforeRegister(AST ast) {
        super.initBeforeRegister(ast);
        classOrNspNames = initClassOrNspNames(ast);
    }

    
    public CsmCompoundStatement getBody() {
        return body;
    }

    public CsmFunction getDeclaration() {
	if( declaration == null ) {
	    declaration = findDeclaration();
	}
	return declaration;
    }
    
    private CsmFunction findDeclaration() {
        String uname = CsmDeclaration.Kind.FUNCTION.toString() + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
	if( def == null ) {
	    CsmObject owner = findOwner();
	    if( owner instanceof CsmClass ) {
		def = findByName(((CsmClass) owner).getMembers(), getName());
	    }
	    else if( owner instanceof CsmNamespace ) {
		def = findByName(((CsmNamespace) owner).getDeclarations(), getName());
	    }
	}
        return (CsmFunction) def;
    }
    
    private static CsmFunction findByName(Collection/*CsmDeclaration*/ declarations, String name) {
	for (Iterator it = declarations.iterator(); it.hasNext();) {
	    CsmDeclaration decl = (CsmDeclaration) it.next();
	    if( decl.getName().equals(name) ) {
		if( decl instanceof  CsmFunction ) { // paranoja
		    return (CsmFunction) decl;
		}
	    }	
	}
	return null;
    }
    
    /** @return either class or namespace */
    private CsmObject findOwner() {
	String[] cnn = classOrNspNames;
	if( cnn != null ) {
	    CsmObject obj = ResolverFactory.createResolver(this).resolve(cnn);
	    if( obj instanceof CsmClass ) {
		if( !( obj instanceof Unresolved.UnresolvedClass) ) {
		    return (CsmClass) obj;
		}
	    }
	    else if( obj instanceof CsmNamespace ) {
		return (CsmNamespace) obj;
	    }
	}
	return null;
    }    
    
    private static String[] initClassOrNspNames(AST node) {
        //qualified id
        AST qid = AstUtil.findMethodName(node);
        if( qid == null ) {
            return null;
        }
        int cnt = qid.getNumberOfChildren();
        if( cnt >= 1 ) {
            List/*<String>*/ l = new ArrayList/*<String>*/();
            for( AST token = qid.getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( token.getType() == CPPTokenTypes.ID ) {
                    if( token.getNextSibling() != null ) {
                        l.add(token.getText());
                    }
                }
            }
            return (String[]) l.toArray(new String[l.size()]);
        }
        return null;
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }

    public String getQualifiedName() {
	if( qualifiedName == null ) {
	    qualifiedName = findQualifiedName();
	}
	return qualifiedName;
    }
    
    private String findQualifiedName() {
	if( declaration != null ) {
	    return declaration.getQualifiedName();
	}
	CsmObject owner = findOwner();
	if( owner instanceof CsmQualifiedNamedElement  ) {
	    qualifiedNameIsFake = false;
	    return ((CsmQualifiedNamedElement) owner).getQualifiedName() + "::" + getQualifiedNamePostfix(); // NOI18N
	}
	else {
	    qualifiedNameIsFake = true;
	    String[] cnn = classOrNspNames;
	    CsmNamespaceDefinition nsd = findNamespaceDefinition();
	    StringBuffer sb = new StringBuffer();
	    if( nsd != null ) {
		sb.append(nsd.getQualifiedName());
	    }
	    if( cnn != null ) {
		for (int i = 0; i < cnn.length; i++) {
		    if( sb.length() > 0 ) {
			sb.append("::"); // NOI18N
		    }
		    sb.append(cnn[i]);
		}
	    }
	    if( sb.length() == 0 ) {
		sb.append("unknown>"); // NOI18N
	    }
	    sb.append("::"); // NOI18N
	    sb.append(getQualifiedNamePostfix());
	    return sb.toString();
	}
    }
    
    protected void registerInProject() {
	super.registerInProject();
	if( qualifiedNameIsFake ) {
	    ((FileImpl) getContainingFile()).onFakeRegisration(this);
	}
    }
    
    public void fixFakeRegistration() {
	String newQname = findQualifiedName();
	if( ! newQname.equals(qualifiedName) ) {
	    ((FileImpl) getContainingFile()).getProjectImpl().unregisterDeclaration(this);
            this.cleanUID();
	    qualifiedName = newQname;
	    ((FileImpl) getContainingFile()).getProjectImpl().registerDeclaration(this);
	}
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition() {
	return findNamespaceDefinition(getContainingFile().getDeclarations());
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition(Collection/*<CsmOffsetableDeclaration>*/ declarations) {
	for (Iterator it = declarations.iterator(); it.hasNext();) {
	    CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) it.next();
	    if( decl.getStartOffset() > this.getStartOffset() ) {
		break;
	    }
	    if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
		if( this.getEndOffset() < decl.getEndOffset() ) {
		    CsmNamespaceDefinition nsdef = (CsmNamespaceDefinition) decl;
		    CsmNamespaceDefinition inner = findNamespaceDefinition(nsdef.getDeclarations());
		    return (inner == null) ? nsdef : inner;
		}
	    }
	}
	return null;
    }
/*
    public String getName() {
        if( name == null ) {
            AST qid = getQialifiedId();
            if( qid != null ) {
                for( AST n = qid.getFirstChild(); n != null; n = n.getNextSibling() ) {
                    int type = n.getType();
                    if( type == CPPTokenTypes.ID ) {
                        name = n.getText();
                    } else if( type == CPPTokenTypes.LITERAL_OPERATOR ) {
                        StringBuffer sb = new StringBuffer(n.getText());
                        sb.append(' ');
                        AST next = n.getNextSibling();
                        if( next != null ) {
                            sb.append(next.getText());
                            n = next;
                            name = sb.toString();
                        }
                    }
                }
            }
            if( name == null ) {
                name = "<null>"; // just to avoid NPE
            }
        }
        return name;
    }
*/    

    public CsmScope getScope() {
        return getContainingFile();
    }

//    public List/*<CsmParameter>*/  getParameters() {
//        if( parameters == null ) {
//            AST ast = AstUtil.findChildOfType(getAst(), CPPTokenTypes.CSM_PARMLIST);
//            parameters = AstRenderer.renderParameters(ast, getContainingFile());
//        }
//        return parameters;
//    }

    public List getScopeElements() {
        List l = new ArrayList();
        l.addAll(getParameters());
        l.add(getBody());
        return l;
    }

    public CsmFunctionDefinition getDefinition() {
        return this;
    }
  
}
