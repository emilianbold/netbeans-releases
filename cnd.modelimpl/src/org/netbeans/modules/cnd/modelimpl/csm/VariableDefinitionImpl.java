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

import antlr.collections.AST;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;

/**
 *
 * @author Alexander Simon
 */
public class VariableDefinitionImpl extends VariableImpl<CsmVariableDefinition> implements CsmVariableDefinition {
    private CsmVariable declaration;
    private String qualifiedName;
    private final String[] classOrNspNames;

    /** Creates a new instance of VariableDefinitionImpl */
    public VariableDefinitionImpl(AST ast, CsmFile file, CsmType type, String name) {
        super(ast, file, type, getLastname(name), true);
        classOrNspNames = getClassOrNspNames(ast);
    }

    private static String getLastname(String name){
        int i = name.lastIndexOf("::"); // NOI18N
        if (i >=0){
            name = name.substring(i+2);
        }
        return name;
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.VARIABLE_DEFINITION;
    }
    
    public CsmVariable getDeclaration() {
	if( declaration == null ) {
	    declaration = findDeclaration();
	}
	return declaration;
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
	    return ((CsmQualifiedNamedElement) owner).getQualifiedName() + "::" + getQualifiedNamePostfix(); // NOI18N
	}
	else {
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

    private CsmVariable findDeclaration() {
        String uname = CsmDeclaration.Kind.VARIABLE.toString() + UNIQUE_NAME_SEPARATOR + getQualifiedName();
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
        return (CsmVariable) def;
    }

    private CsmVariable findByName(Collection/*CsmDeclaration*/ declarations, String name) {
	for (Iterator it = declarations.iterator(); it.hasNext();) {
	    CsmDeclaration decl = (CsmDeclaration) it.next();
	    if( decl.getName().equals(name) ) {
		if( decl instanceof  CsmVariable ) { // paranoja
		    return (CsmVariable) decl;
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
    
    private static String[] getClassOrNspNames(AST ast) {
        AST qid = getQialifiedId(ast);
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
    
    private static AST getQialifiedId(AST ast){
        AST varAst = ast;
        for( AST token = varAst.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                    return token = token.getFirstChild();
                case CPPTokenTypes.CSM_QUALIFIED_ID:
                case CPPTokenTypes.ID:
                    return token;
            }
        }
        return null;
    }
}
