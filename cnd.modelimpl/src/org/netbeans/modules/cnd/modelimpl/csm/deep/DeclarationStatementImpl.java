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

package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Implementation of CsmDeclarationStatement
 * @author Vladimir Kvashin
 */
public class DeclarationStatementImpl extends StatementBase implements CsmDeclarationStatement {

    private List<CsmDeclaration> declarators;
    
    public DeclarationStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }

    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.DECLARATION;
    }

    public List<CsmDeclaration> getDeclarators() {
        if( declarators == null ) {
            declarators = new ArrayList<CsmDeclaration>();
            render();
        }
        return declarators;
    }
    
    public String toString() {
        return "" + getKind() + ' ' + getOffsetString() + '[' + declarators + ']'; // NOI18N
    }
    
    private void render() {
        AstRenderer renderer = new DSRenderer();
        renderer.render(getAst(), null, null);
    }
    
       
    private class DSRenderer extends AstRenderer {
	
	public DSRenderer() {
	    super((FileImpl) getContainingFile());
	}
	
	protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static, MutableDeclarationsContainer container1, MutableDeclarationsContainer container2,CsmScope scope) {
	    VariableImpl var = super.createVariable(offsetAst, file, type, name, _static, container1, container2, getScope());
	    declarators.add(var);
	    return var;
	}

	public void render(AST tree, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
	    if( tree != null ) {
		AST token = tree;
		switch( token.getType() ) {
		    case CPPTokenTypes.CSM_FOR_INIT_STATEMENT:
		    case CPPTokenTypes.CSM_DECLARATION_STATEMENT:
			if (!renderVariable(token, currentNamespace, container)){
			    render(token.getFirstChild(), currentNamespace, container);
			}
			break;
		    case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
			declarators.add(new NamespaceAliasImpl(token, getContainingFile()));
			break;
		    case CPPTokenTypes.CSM_USING_DIRECTIVE:
			declarators.add(new UsingDirectiveImpl(token, getContainingFile()));
			break;
		    case CPPTokenTypes.CSM_USING_DECLARATION:
			declarators.add(new UsingDeclarationImpl(token, getContainingFile()));
			break;

		    case CPPTokenTypes.CSM_CLASS_DECLARATION:
		    {
			ClassImpl cls = ClassImpl.create(token, null, getContainingFile());
			declarators.add(cls);
			addTypedefs(renderTypedef(token, cls, currentNamespace), currentNamespace, container);
			renderVariableInClassifier(token, cls, currentNamespace, container);
			break;
		    }
		    case CPPTokenTypes.CSM_ENUM_DECLARATION:
		    {
			CsmEnum csmEnum = EnumImpl.create(token, currentNamespace, getContainingFile());
			declarators.add(csmEnum);
			renderVariableInClassifier(token, csmEnum, currentNamespace, container);
			break;
		    }
		    case CPPTokenTypes.CSM_TYPE_BUILTIN:
		    case CPPTokenTypes.CSM_TYPE_COMPOUND:
			AST typeToken = token;
			AST next = token.getNextSibling();
			if( next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
			    do {
				TypeImpl type;
				if( typeToken.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
				    type = TypeFactory.createBuiltinType(typeToken.getText(), null, 0, typeToken, getContainingFile());
				}
				else {
				    type = TypeFactory.createType(typeToken, getContainingFile(), null, 0);
				}
				String name = next.getText();
				VariableImpl var = createVariable(next, getContainingFile(), type, name, false, currentNamespace, container, getScope());
				// we ignore both currentNamespace and container; <= WHY?
				// eat all tokens up to the comma that separates the next decl
				next = next.getNextSibling();
				if( next != null && next.getType() == CPPTokenTypes.CSM_PARMLIST ) {
				    next = next.getNextSibling();
				}
				if( next != null && next.getType() == CPPTokenTypes.COMMA ) {
				    next = next.getNextSibling();
				}
			    }
			    while( next != null && next.getType() ==  CPPTokenTypes.CSM_QUALIFIED_ID );
			}
			break;
		}
	    }
	}

// Never used 
//	/**
//	 * Creates a variable for declaration like int x(y);
//	 * Returns a token that follows this declaration or null
//	 */
//	private AST createVarWithCtor(AST token) {
//	    assert(token.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN || token.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND);
//	    AST typeToken = token;
//	    AST next = token.getNextSibling();
//	    if( next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
//		TypeImpl type;
//		if( typeToken.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
//		    type = TypeFactory.createBuiltinType(typeToken.getText(), null, 0, typeToken, getContainingFile());
//		}
//		else {
//		    type = TypeFactory.createType(typeToken, getContainingFile(), null, 0);
//		}
//		String name = next.getText();
//		VariableImpl var = new VariableImpl(next, getContainingFile(), type, name, true);
//		// we ignore both currentNamespace and container
//		declarators.add(var);
//		// eat all tokens up to the comma that separates the next decl
//		next = next.getNextSibling();
//		if( next != null && next.getType() == CPPTokenTypes.CSM_PARMLIST ) {
//		    next = next.getNextSibling();
//		}
//		if( next != null && next.getType() == CPPTokenTypes.COMMA ) {
//		    next = next.getNextSibling();
//		}
//		return next;
//	    }
//	    return null;
//	}
    }
	    
}
