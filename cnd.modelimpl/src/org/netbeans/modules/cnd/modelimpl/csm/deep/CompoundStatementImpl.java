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

import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Common ancestor for all ... statements
 * @author Vladimir Kvashin
 */
public class CompoundStatementImpl extends StatementBase implements CsmCompoundStatement {
    
    private List/*<CsmStatement>*/ statements;
    
    public CompoundStatementImpl(AST ast, CsmFile file) {
        super(ast, file);
    }
    
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.COMPOUND;
    }
    
    public List/*<CsmStatement>*/ getStatements() {
        if( statements == null ) {
            statements = new ArrayList();
            renderStatements(getAst());
        }
        return statements;
    }
    
    protected void renderStatements(AST ast) {
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            CsmStatement stmt = AstRenderer.renderStatement(token, getContainingFile());
            if( stmt != null ) {
                statements.add(stmt);
            }
        }
    }

    public List getScopeElements() {
        return getStatements();
    }

}
