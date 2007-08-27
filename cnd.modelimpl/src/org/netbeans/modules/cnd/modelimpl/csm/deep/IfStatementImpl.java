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
 * CsmIfStatement implementation
 * @author Vladimir Kvashin
 */
public class IfStatementImpl extends StatementBase implements CsmIfStatement {
    
    private CsmCondition condition;
    private CsmStatement thenStmt;
    private CsmStatement elseStmt;
    
    public IfStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }
    
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.IF;
    }

    public CsmCondition getCondition() {
        renderIfNeed(); // be lazy! ;-)
        return condition;
    }

    public CsmStatement getThen() {
        renderIfNeed(); // lazyness is good ;-)
        return thenStmt;
    }

    public CsmStatement getElse() {
        renderIfNeed(); // long live lazyness! ;-))
        return elseStmt;
    }
    
    private void renderIfNeed() {
        if( condition == null ) { // should never be null, so used as a flag
            render(getAst());
        }
    }
    
    private void render(AST ast) {
        AstRenderer renderer = new AstRenderer((FileImpl) getContainingFile());
        boolean inElse = false;
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.CSM_CONDITION:
                    condition = renderer.renderCondition(token, this);
                    break;
                case CPPTokenTypes.LITERAL_else:
                    inElse = true;
                    break;
                default:
                    //if( AstRenderer.isStatement(token) ) {
                    CsmStatement stmt = AstRenderer.renderStatement(token, getContainingFile(), this);
                    if( stmt != null ) {
                        if( inElse ) {
                            elseStmt = stmt;
                        }
                        else {
                            thenStmt = stmt;
                        }
                    }
                    //}
            }
        }
    }

    public List<CsmScopeElement> getScopeElements() {
        return DeepUtil.merge(getCondition(), getThen(), getElse());
    }
    
    
}
