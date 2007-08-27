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
 * Common base for for/while statements implementation
 * @author Vladimir Kvashin
 */
public class LoopStatementImpl extends StatementBase implements CsmLoopStatement {
    
    private CsmCondition condition;
    private CsmStatement body;
    private boolean postCheck;
    
    public LoopStatementImpl(AST ast, CsmFile file, boolean postCheck, CsmScope scope) {
        super(ast, file, scope);
        this.postCheck = postCheck;
    }
   
    public CsmCondition getCondition() {
        renderIfNeed();
        return condition;
    }
    
    public CsmStatement getBody() {
        renderIfNeed();
        return body;
    }

    public boolean isPostCheck() {
        return postCheck;
    }

    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.WHILE;
    }
    
    private void renderIfNeed() {
        if( condition == null ) {
            render();
        }
    }
        
    private void render() {
        AstRenderer renderer = new AstRenderer((FileImpl) getContainingFile());
        for( AST token = getAst().getFirstChild(); token != null; token = token.getNextSibling() ) {
            int type = token.getType();
            if( type == CPPTokenTypes.CSM_CONDITION ) {
                condition = renderer.renderCondition(token, this);
            }
            else if( AstRenderer.isExpression(type) ) {
                condition = new ConditionExpressionImpl(token, getContainingFile());
            }
            else if( AstRenderer.isStatement(type) ) {
                body = AstRenderer.renderStatement(token, getContainingFile(), this);
            }
        }
    }

    public List<CsmScopeElement> getScopeElements() {
        return DeepUtil.merge(getCondition(), getBody());
    }
    
}
