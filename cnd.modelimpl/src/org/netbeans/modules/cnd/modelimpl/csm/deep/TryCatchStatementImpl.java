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
 * CsmTryCatchStatement implementation
 * @author Vladimir Kvashin
 */
public class TryCatchStatementImpl extends StatementBase implements CsmTryCatchStatement, CsmScope {
    
    private StatementBase tryStatement;
    private List<CsmExceptionHandler> handlers;
    
    public TryCatchStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }
    
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.TRY_CATCH;
    }

    public CsmStatement getTryStatement() {
        if( tryStatement == null ) {
            render();
        }
        return tryStatement;
    }
    
    public List<CsmExceptionHandler> getHandlers() {
        if( handlers == null ) {
            render();
        }
        return handlers;
    }
    
    private void render() {
        handlers = new ArrayList<CsmExceptionHandler>();
        for( AST token = getAst().getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.CSM_COMPOUND_STATEMENT:
                    tryStatement = AstRenderer.renderStatement(token, getContainingFile(), this);
                    break;
                case CPPTokenTypes.CSM_CATCH_CLAUSE:
                    handlers.add(new ExceptionHandlerImpl(token, getContainingFile(), this));
                    break;
            }
        }
    }

    public List<CsmScopeElement> getScopeElements() {
	List<CsmScopeElement> elements = new ArrayList<CsmScopeElement>();
	elements.add(tryStatement);
	elements.addAll(handlers);
	return elements;
    }
    
}
