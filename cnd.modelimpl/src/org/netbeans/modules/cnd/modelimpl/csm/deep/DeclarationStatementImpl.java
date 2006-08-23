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

package org.netbeans.modules.cnd.modelimpl.csm.deep;

import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.antlr2.CsmAST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

/**
 * Implementation of CsmDeclarationStatement
 * @author Vladimir Kvashin
 */
public class DeclarationStatementImpl extends StatementBase implements CsmDeclarationStatement {

    private List/*<CsmVariableImpl>*/ declarators;
    
    public DeclarationStatementImpl(AST ast, CsmFile file) {
            super(ast, file);
    }

    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.DECLARATION;
    }

    public List getDeclarators() {
        if( declarators == null ) {
            declarators = new ArrayList/*<CsmVariableImpl>*/();
            render();
        }
        return declarators;
    }
       
    private void render() {
        AstRenderer renderer = new AstRenderer((FileImpl) getContainingFile()) {
            protected VariableImpl createVariable(AST offsetAst, CsmFile file, CsmType type, String name, boolean _static) {
                VariableImpl var = super.createVariable(offsetAst, file, type, name, _static);
                declarators.add(var);
                return var;
            }
        };
        if( ! renderer.renderVariable(getAst(), null, null) ) {
            AST tree = getAst();
            if( tree != null ) {
                for( AST token = tree.getFirstChild(); token != null; token = token.getNextSibling() ) {
                    switch( token.getType() ) {
                        case CPPTokenTypes.CSM_NAMESPACE_ALIAS:
                            declarators.add(new NamespaceAliasImpl(token, getContainingFile()));
                            break;
                        case CPPTokenTypes.CSM_USING_DIRECTIVE:
                            declarators.add(new UsingDirectiveImpl(token, getContainingFile()));
                            break;
                        case CPPTokenTypes.CSM_USING_DECLARATION:
                            declarators.add(new UsingDeclarationImpl(token, getContainingFile()));
                            break;
                    }
                }
            }
        }
        //TODO: process other kinds of declarations
    }
}
