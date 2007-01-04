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
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Implements CsmForStatement statements
 * @author Vladimir Kvashin
 */
public class ForStatementImpl extends StatementBase implements CsmForStatement {
    
    private StatementBase init;
    private CsmCondition condition;
    private ExpressionBase iteration;
    private StatementBase body;
    private boolean rendered = false;
    
    public ForStatementImpl(AST ast, CsmFile file) {
        super(ast, file);
    }
    
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.FOR;
    }

    public boolean isPostCheck() {
        return true;
    }

    public CsmExpression getIterationExpression() {
        renderIfNeed();
        return iteration;
    }

    public CsmStatement getInitStatement() {
        renderIfNeed();
        return init;
    }

    public CsmCondition getCondition() {
        renderIfNeed();
        return condition;
    }

    public CsmStatement getBody() {
        renderIfNeed();
        return body;
    }
    
    private void renderIfNeed() {
        if( ! rendered ) {
            rendered = true;
            render();
        }
    }
    
    private void render() {
        
        AstRenderer renderer = new AstRenderer((FileImpl) getContainingFile());
        
        for( AST token = getAst().getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
            case CPPTokenTypes.CSM_FOR_INIT_STATEMENT:
                AST child = token.getFirstChild();
                if( child != null ) {
                    switch( child.getType() ) {
                        case CPPTokenTypes.SEMICOLON:
                            init = null;
                            break;
                        case CPPTokenTypes.CSM_TYPE_BUILTIN:
                        case CPPTokenTypes.CSM_TYPE_COMPOUND:
                            //renderer.renderVariable(token, null, null);
                            init = new DeclarationStatementImpl(token, getContainingFile());
                            break;
                        default:
                            if( AstRenderer.isExpression(child) ) {
                                init = new ExpressionStatementImpl(token, getContainingFile());
                            }
                            break;
                    }
                }
                break;
            case CPPTokenTypes.CSM_CONDITION:
                condition = renderer.renderCondition(token);
                break;
            default:
                if( AstRenderer.isStatement(token) ) {
                    body = body = AstRenderer.renderStatement(token, getContainingFile());
                }
                else if( AstRenderer.isExpression(token) ) {
                    iteration = renderer.renderExpression(token);
                }
            }
        }
    }

    public List getScopeElements() {
        //return DeepUtil.merge(getInitStatement(), getCondition(), getBody());
        List l = new ArrayList();
        CsmStatement stmt = getInitStatement();
        if( stmt != null ) {
            l.add(stmt);
        }
        CsmCondition cond = getCondition();
        if( cond != null ) {
            CsmDeclaration decl = cond.getDeclaration();
            if( decl != null ) {
                l.add(cond);
            }
        }
        stmt = getBody();
        if( stmt != null ) {
            l.add(stmt);
        }
        return l;
    }
    
    
}
