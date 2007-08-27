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
 * CsmSwitchStatement implementation
 * @author Vladimir Kvashin
 */
public class SwitchStatementImpl extends StatementBase implements CsmSwitchStatement {
    
    private CsmCondition condition;
    private StatementBase body;
    
    public SwitchStatementImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope);
    }
    
    public CsmStatement.Kind getKind() {
        return CsmStatement.Kind.SWITCH;
    }

    public CsmCondition getCondition() {
        if( condition == null ) {
            AST token = AstUtil.findChildOfType(getAst(), CPPTokenTypes.CSM_CONDITION);
            if( token != null ) {
                condition = new AstRenderer((FileImpl) getContainingFile()).renderCondition(token, this);
            }
        }
        //renderIfNeed();
        return condition;
    }
    
    public CsmStatement getBody() {
        //renderIfNeed();
        if( body == null ) {
            for( AST token = getAst().getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( AstRenderer.isStatement(token) ) {
                    body = AstRenderer.renderStatement(token, getContainingFile(), this);
                    break;
                }
            }
        }
        return body;
    }

    public List<CsmScopeElement> getScopeElements() {
        return DeepUtil.merge(getCondition(), getBody());
    }
    
    
}
